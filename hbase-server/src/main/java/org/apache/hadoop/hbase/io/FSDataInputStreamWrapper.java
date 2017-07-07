begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
end_comment

begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|io
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|Closeable
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|IOException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|InputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|reflect
operator|.
name|InvocationTargetException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|reflect
operator|.
name|Method
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|io
operator|.
name|IOUtils
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|Log
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|LogFactory
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|fs
operator|.
name|FSDataInputStream
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|fs
operator|.
name|FileSystem
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|fs
operator|.
name|Path
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|classification
operator|.
name|InterfaceAudience
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|fs
operator|.
name|HFileSystem
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|shaded
operator|.
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|annotations
operator|.
name|VisibleForTesting
import|;
end_import

begin_comment
comment|/**  * Wrapper for input stream(s) that takes care of the interaction of FS and HBase checksums,  * as well as closing streams. Initialization is not thread-safe, but normal operation is;  * see method comments.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|FSDataInputStreamWrapper
implements|implements
name|Closeable
block|{
specifier|private
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|FSDataInputStreamWrapper
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|boolean
name|isLogTraceEnabled
init|=
name|LOG
operator|.
name|isTraceEnabled
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|HFileSystem
name|hfs
decl_stmt|;
specifier|private
specifier|final
name|Path
name|path
decl_stmt|;
specifier|private
specifier|final
name|FileLink
name|link
decl_stmt|;
specifier|private
specifier|final
name|boolean
name|doCloseStreams
decl_stmt|;
specifier|private
specifier|final
name|boolean
name|dropBehind
decl_stmt|;
specifier|private
specifier|final
name|long
name|readahead
decl_stmt|;
comment|/** Two stream handles, one with and one without FS-level checksum.    * HDFS checksum setting is on FS level, not single read level, so you have to keep two    * FS objects and two handles open to interleave different reads freely, which is very sad.    * This is what we do:    * 1) First, we need to read the trailer of HFile to determine checksum parameters.    *  We always use FS checksum to do that, so ctor opens {@link #stream}.    * 2.1) After that, if HBase checksum is not used, we'd just always use {@link #stream};    * 2.2) If HBase checksum can be used, we'll open {@link #streamNoFsChecksum},    *  and close {@link #stream}. User MUST call prepareForBlockReader for that to happen;    *  if they don't, (2.1) will be the default.    * 3) The users can call {@link #shouldUseHBaseChecksum()}, and pass its result to    *  {@link #getStream(boolean)} to get stream (if Java had out/pointer params we could    *  return both in one call). This stream is guaranteed to be set.    * 4) The first time HBase checksum fails, one would call {@link #fallbackToFsChecksum(int)}.    * That will take lock, and open {@link #stream}. While this is going on, others will    * continue to use the old stream; if they also want to fall back, they'll also call    * {@link #fallbackToFsChecksum(int)}, and block until {@link #stream} is set.    * 5) After some number of checksumOk() calls, we will go back to using HBase checksum.    * We will have 2 handles; however we presume checksums fail so rarely that we don't care.    */
specifier|private
specifier|volatile
name|FSDataInputStream
name|stream
init|=
literal|null
decl_stmt|;
specifier|private
specifier|volatile
name|FSDataInputStream
name|streamNoFsChecksum
init|=
literal|null
decl_stmt|;
specifier|private
name|Object
name|streamNoFsChecksumFirstCreateLock
init|=
operator|new
name|Object
argument_list|()
decl_stmt|;
comment|// The configuration states that we should validate hbase checksums
specifier|private
name|boolean
name|useHBaseChecksumConfigured
decl_stmt|;
comment|// Record the current state of this reader with respect to
comment|// validating checkums in HBase. This is originally set the same
comment|// value as useHBaseChecksumConfigured, but can change state as and when
comment|// we encounter checksum verification failures.
specifier|private
specifier|volatile
name|boolean
name|useHBaseChecksum
decl_stmt|;
comment|// In the case of a checksum failure, do these many succeeding
comment|// reads without hbase checksum verification.
specifier|private
specifier|volatile
name|int
name|hbaseChecksumOffCount
init|=
operator|-
literal|1
decl_stmt|;
specifier|private
name|Boolean
name|instanceOfCanUnbuffer
init|=
literal|null
decl_stmt|;
comment|// Using reflection to get org.apache.hadoop.fs.CanUnbuffer#unbuffer method to avoid compilation
comment|// errors against Hadoop pre 2.6.4 and 2.7.1 versions.
specifier|private
name|Method
name|unbuffer
init|=
literal|null
decl_stmt|;
specifier|public
name|FSDataInputStreamWrapper
parameter_list|(
name|FileSystem
name|fs
parameter_list|,
name|Path
name|path
parameter_list|)
throws|throws
name|IOException
block|{
name|this
argument_list|(
name|fs
argument_list|,
name|path
argument_list|,
literal|false
argument_list|,
operator|-
literal|1L
argument_list|)
expr_stmt|;
block|}
specifier|public
name|FSDataInputStreamWrapper
parameter_list|(
name|FileSystem
name|fs
parameter_list|,
name|Path
name|path
parameter_list|,
name|boolean
name|dropBehind
parameter_list|,
name|long
name|readahead
parameter_list|)
throws|throws
name|IOException
block|{
name|this
argument_list|(
name|fs
argument_list|,
literal|null
argument_list|,
name|path
argument_list|,
name|dropBehind
argument_list|,
name|readahead
argument_list|)
expr_stmt|;
block|}
specifier|public
name|FSDataInputStreamWrapper
parameter_list|(
name|FileSystem
name|fs
parameter_list|,
name|FileLink
name|link
parameter_list|,
name|boolean
name|dropBehind
parameter_list|,
name|long
name|readahead
parameter_list|)
throws|throws
name|IOException
block|{
name|this
argument_list|(
name|fs
argument_list|,
name|link
argument_list|,
literal|null
argument_list|,
name|dropBehind
argument_list|,
name|readahead
argument_list|)
expr_stmt|;
block|}
specifier|private
name|FSDataInputStreamWrapper
parameter_list|(
name|FileSystem
name|fs
parameter_list|,
name|FileLink
name|link
parameter_list|,
name|Path
name|path
parameter_list|,
name|boolean
name|dropBehind
parameter_list|,
name|long
name|readahead
parameter_list|)
throws|throws
name|IOException
block|{
assert|assert
operator|(
name|path
operator|==
literal|null
operator|)
operator|!=
operator|(
name|link
operator|==
literal|null
operator|)
assert|;
name|this
operator|.
name|path
operator|=
name|path
expr_stmt|;
name|this
operator|.
name|link
operator|=
name|link
expr_stmt|;
name|this
operator|.
name|doCloseStreams
operator|=
literal|true
expr_stmt|;
name|this
operator|.
name|dropBehind
operator|=
name|dropBehind
expr_stmt|;
name|this
operator|.
name|readahead
operator|=
name|readahead
expr_stmt|;
comment|// If the fs is not an instance of HFileSystem, then create an instance of HFileSystem
comment|// that wraps over the specified fs. In this case, we will not be able to avoid
comment|// checksumming inside the filesystem.
name|this
operator|.
name|hfs
operator|=
operator|(
name|fs
operator|instanceof
name|HFileSystem
operator|)
condition|?
operator|(
name|HFileSystem
operator|)
name|fs
else|:
operator|new
name|HFileSystem
argument_list|(
name|fs
argument_list|)
expr_stmt|;
comment|// Initially we are going to read the tail block. Open the reader w/FS checksum.
name|this
operator|.
name|useHBaseChecksumConfigured
operator|=
name|this
operator|.
name|useHBaseChecksum
operator|=
literal|false
expr_stmt|;
name|this
operator|.
name|stream
operator|=
operator|(
name|link
operator|!=
literal|null
operator|)
condition|?
name|link
operator|.
name|open
argument_list|(
name|hfs
argument_list|)
else|:
name|hfs
operator|.
name|open
argument_list|(
name|path
argument_list|)
expr_stmt|;
name|setStreamOptions
argument_list|(
name|stream
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|setStreamOptions
parameter_list|(
name|FSDataInputStream
name|in
parameter_list|)
block|{
try|try
block|{
name|this
operator|.
name|stream
operator|.
name|setDropBehind
argument_list|(
name|dropBehind
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
comment|// Skipped.
block|}
if|if
condition|(
name|readahead
operator|>=
literal|0
condition|)
block|{
try|try
block|{
name|this
operator|.
name|stream
operator|.
name|setReadahead
argument_list|(
name|readahead
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
comment|// Skipped.
block|}
block|}
block|}
comment|/**    * Prepares the streams for block reader. NOT THREAD SAFE. Must be called once, after any    * reads finish and before any other reads start (what happens in reality is we read the    * tail, then call this based on what's in the tail, then read blocks).    * @param forceNoHBaseChecksum Force not using HBase checksum.    */
specifier|public
name|void
name|prepareForBlockReader
parameter_list|(
name|boolean
name|forceNoHBaseChecksum
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|hfs
operator|==
literal|null
condition|)
return|return;
assert|assert
name|this
operator|.
name|stream
operator|!=
literal|null
operator|&&
operator|!
name|this
operator|.
name|useHBaseChecksumConfigured
assert|;
name|boolean
name|useHBaseChecksum
init|=
operator|!
name|forceNoHBaseChecksum
operator|&&
name|hfs
operator|.
name|useHBaseChecksum
argument_list|()
operator|&&
operator|(
name|hfs
operator|.
name|getNoChecksumFs
argument_list|()
operator|!=
name|hfs
operator|)
decl_stmt|;
if|if
condition|(
name|useHBaseChecksum
condition|)
block|{
name|FileSystem
name|fsNc
init|=
name|hfs
operator|.
name|getNoChecksumFs
argument_list|()
decl_stmt|;
name|this
operator|.
name|streamNoFsChecksum
operator|=
operator|(
name|link
operator|!=
literal|null
operator|)
condition|?
name|link
operator|.
name|open
argument_list|(
name|fsNc
argument_list|)
else|:
name|fsNc
operator|.
name|open
argument_list|(
name|path
argument_list|)
expr_stmt|;
name|setStreamOptions
argument_list|(
name|streamNoFsChecksum
argument_list|)
expr_stmt|;
name|this
operator|.
name|useHBaseChecksumConfigured
operator|=
name|this
operator|.
name|useHBaseChecksum
operator|=
name|useHBaseChecksum
expr_stmt|;
comment|// Close the checksum stream; we will reopen it if we get an HBase checksum failure.
name|this
operator|.
name|stream
operator|.
name|close
argument_list|()
expr_stmt|;
name|this
operator|.
name|stream
operator|=
literal|null
expr_stmt|;
block|}
block|}
comment|/** For use in tests. */
annotation|@
name|VisibleForTesting
specifier|public
name|FSDataInputStreamWrapper
parameter_list|(
name|FSDataInputStream
name|fsdis
parameter_list|)
block|{
name|this
argument_list|(
name|fsdis
argument_list|,
name|fsdis
argument_list|)
expr_stmt|;
block|}
comment|/** For use in tests. */
annotation|@
name|VisibleForTesting
specifier|public
name|FSDataInputStreamWrapper
parameter_list|(
name|FSDataInputStream
name|fsdis
parameter_list|,
name|FSDataInputStream
name|noChecksum
parameter_list|)
block|{
name|doCloseStreams
operator|=
literal|false
expr_stmt|;
name|stream
operator|=
name|fsdis
expr_stmt|;
name|streamNoFsChecksum
operator|=
name|noChecksum
expr_stmt|;
name|path
operator|=
literal|null
expr_stmt|;
name|link
operator|=
literal|null
expr_stmt|;
name|hfs
operator|=
literal|null
expr_stmt|;
name|useHBaseChecksumConfigured
operator|=
name|useHBaseChecksum
operator|=
literal|false
expr_stmt|;
name|dropBehind
operator|=
literal|false
expr_stmt|;
name|readahead
operator|=
literal|0
expr_stmt|;
block|}
comment|/**    * @return Whether we are presently using HBase checksum.    */
specifier|public
name|boolean
name|shouldUseHBaseChecksum
parameter_list|()
block|{
return|return
name|this
operator|.
name|useHBaseChecksum
return|;
block|}
comment|/**    * Get the stream to use. Thread-safe.    * @param useHBaseChecksum must be the value that shouldUseHBaseChecksum has returned    *  at some point in the past, otherwise the result is undefined.    */
specifier|public
name|FSDataInputStream
name|getStream
parameter_list|(
name|boolean
name|useHBaseChecksum
parameter_list|)
block|{
return|return
name|useHBaseChecksum
condition|?
name|this
operator|.
name|streamNoFsChecksum
else|:
name|this
operator|.
name|stream
return|;
block|}
comment|/**    * Read from non-checksum stream failed, fall back to FS checksum. Thread-safe.    * @param offCount For how many checksumOk calls to turn off the HBase checksum.    */
specifier|public
name|FSDataInputStream
name|fallbackToFsChecksum
parameter_list|(
name|int
name|offCount
parameter_list|)
throws|throws
name|IOException
block|{
comment|// checksumOffCount is speculative, but let's try to reset it less.
name|boolean
name|partOfConvoy
init|=
literal|false
decl_stmt|;
if|if
condition|(
name|this
operator|.
name|stream
operator|==
literal|null
condition|)
block|{
synchronized|synchronized
init|(
name|streamNoFsChecksumFirstCreateLock
init|)
block|{
name|partOfConvoy
operator|=
operator|(
name|this
operator|.
name|stream
operator|!=
literal|null
operator|)
expr_stmt|;
if|if
condition|(
operator|!
name|partOfConvoy
condition|)
block|{
name|this
operator|.
name|stream
operator|=
operator|(
name|link
operator|!=
literal|null
operator|)
condition|?
name|link
operator|.
name|open
argument_list|(
name|hfs
argument_list|)
else|:
name|hfs
operator|.
name|open
argument_list|(
name|path
argument_list|)
expr_stmt|;
block|}
block|}
block|}
if|if
condition|(
operator|!
name|partOfConvoy
condition|)
block|{
name|this
operator|.
name|useHBaseChecksum
operator|=
literal|false
expr_stmt|;
name|this
operator|.
name|hbaseChecksumOffCount
operator|=
name|offCount
expr_stmt|;
block|}
return|return
name|this
operator|.
name|stream
return|;
block|}
comment|/** Report that checksum was ok, so we may ponder going back to HBase checksum. */
specifier|public
name|void
name|checksumOk
parameter_list|()
block|{
if|if
condition|(
name|this
operator|.
name|useHBaseChecksumConfigured
operator|&&
operator|!
name|this
operator|.
name|useHBaseChecksum
operator|&&
operator|(
name|this
operator|.
name|hbaseChecksumOffCount
operator|--
operator|<
literal|0
operator|)
condition|)
block|{
comment|// The stream we need is already open (because we were using HBase checksum in the past).
assert|assert
name|this
operator|.
name|streamNoFsChecksum
operator|!=
literal|null
assert|;
name|this
operator|.
name|useHBaseChecksum
operator|=
literal|true
expr_stmt|;
block|}
block|}
comment|/** Close stream(s) if necessary. */
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|()
block|{
if|if
condition|(
operator|!
name|doCloseStreams
condition|)
block|{
return|return;
block|}
comment|// we do not care about the close exception as it is for reading, no data loss issue.
name|IOUtils
operator|.
name|closeQuietly
argument_list|(
name|streamNoFsChecksum
argument_list|)
expr_stmt|;
name|IOUtils
operator|.
name|closeQuietly
argument_list|(
name|stream
argument_list|)
expr_stmt|;
block|}
specifier|public
name|HFileSystem
name|getHfs
parameter_list|()
block|{
return|return
name|this
operator|.
name|hfs
return|;
block|}
comment|/**    * This will free sockets and file descriptors held by the stream only when the stream implements    * org.apache.hadoop.fs.CanUnbuffer. NOT THREAD SAFE. Must be called only when all the clients    * using this stream to read the blocks have finished reading. If by chance the stream is    * unbuffered and there are clients still holding this stream for read then on next client read    * request a new socket will be opened by Datanode without client knowing about it and will serve    * its read request. Note: If this socket is idle for some time then the DataNode will close the    * socket and the socket will move into CLOSE_WAIT state and on the next client request on this    * stream, the current socket will be closed and a new socket will be opened to serve the    * requests.    */
annotation|@
name|SuppressWarnings
argument_list|(
block|{
literal|"rawtypes"
block|}
argument_list|)
specifier|public
name|void
name|unbuffer
parameter_list|()
block|{
name|FSDataInputStream
name|stream
init|=
name|this
operator|.
name|getStream
argument_list|(
name|this
operator|.
name|shouldUseHBaseChecksum
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|stream
operator|!=
literal|null
condition|)
block|{
name|InputStream
name|wrappedStream
init|=
name|stream
operator|.
name|getWrappedStream
argument_list|()
decl_stmt|;
comment|// CanUnbuffer interface was added as part of HDFS-7694 and the fix is available in Hadoop
comment|// 2.6.4+ and 2.7.1+ versions only so check whether the stream object implements the
comment|// CanUnbuffer interface or not and based on that call the unbuffer api.
specifier|final
name|Class
argument_list|<
name|?
extends|extends
name|InputStream
argument_list|>
name|streamClass
init|=
name|wrappedStream
operator|.
name|getClass
argument_list|()
decl_stmt|;
if|if
condition|(
name|this
operator|.
name|instanceOfCanUnbuffer
operator|==
literal|null
condition|)
block|{
comment|// To ensure we compute whether the stream is instance of CanUnbuffer only once.
name|this
operator|.
name|instanceOfCanUnbuffer
operator|=
literal|false
expr_stmt|;
name|Class
argument_list|<
name|?
argument_list|>
index|[]
name|streamInterfaces
init|=
name|streamClass
operator|.
name|getInterfaces
argument_list|()
decl_stmt|;
for|for
control|(
name|Class
name|c
range|:
name|streamInterfaces
control|)
block|{
if|if
condition|(
name|c
operator|.
name|getCanonicalName
argument_list|()
operator|.
name|toString
argument_list|()
operator|.
name|equals
argument_list|(
literal|"org.apache.hadoop.fs.CanUnbuffer"
argument_list|)
condition|)
block|{
try|try
block|{
name|this
operator|.
name|unbuffer
operator|=
name|streamClass
operator|.
name|getDeclaredMethod
argument_list|(
literal|"unbuffer"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|NoSuchMethodException
decl||
name|SecurityException
name|e
parameter_list|)
block|{
if|if
condition|(
name|isLogTraceEnabled
condition|)
block|{
name|LOG
operator|.
name|trace
argument_list|(
literal|"Failed to find 'unbuffer' method in class "
operator|+
name|streamClass
operator|+
literal|" . So there may be a TCP socket connection "
operator|+
literal|"left open in CLOSE_WAIT state."
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
return|return;
block|}
name|this
operator|.
name|instanceOfCanUnbuffer
operator|=
literal|true
expr_stmt|;
break|break;
block|}
block|}
block|}
if|if
condition|(
name|this
operator|.
name|instanceOfCanUnbuffer
condition|)
block|{
try|try
block|{
name|this
operator|.
name|unbuffer
operator|.
name|invoke
argument_list|(
name|wrappedStream
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalAccessException
decl||
name|IllegalArgumentException
decl||
name|InvocationTargetException
name|e
parameter_list|)
block|{
if|if
condition|(
name|isLogTraceEnabled
condition|)
block|{
name|LOG
operator|.
name|trace
argument_list|(
literal|"Failed to invoke 'unbuffer' method in class "
operator|+
name|streamClass
operator|+
literal|" . So there may be a TCP socket connection left open in CLOSE_WAIT state."
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
block|}
else|else
block|{
if|if
condition|(
name|isLogTraceEnabled
condition|)
block|{
name|LOG
operator|.
name|trace
argument_list|(
literal|"Failed to find 'unbuffer' method in class "
operator|+
name|streamClass
operator|+
literal|" . So there may be a TCP socket connection "
operator|+
literal|"left open in CLOSE_WAIT state. For more details check "
operator|+
literal|"https://issues.apache.org/jira/browse/HBASE-9393"
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
block|}
end_class

end_unit

