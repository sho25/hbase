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
name|util
operator|.
name|ArrayList
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Arrays
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collection
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
name|io
operator|.
name|FileNotFoundException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|List
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
name|yetus
operator|.
name|audience
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
name|fs
operator|.
name|CanSetDropBehind
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
name|CanSetReadahead
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
name|FileStatus
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
name|fs
operator|.
name|PositionedReadable
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
name|Seekable
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
name|util
operator|.
name|FSUtils
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
name|ipc
operator|.
name|RemoteException
import|;
end_import

begin_comment
comment|/**  * The FileLink is a sort of hardlink, that allows access to a file given a set of locations.  *  *<p><b>The Problem:</b>  *<ul>  *<li>  *    HDFS doesn't have support for hardlinks, and this make impossible to referencing  *    the same data blocks using different names.  *</li>  *<li>  *    HBase store files in one location (e.g. table/region/family/) and when the file is not  *    needed anymore (e.g. compaction, region deletion, ...) moves it to an archive directory.  *</li>  *</ul>  * If we want to create a reference to a file, we need to remember that it can be in its  * original location or in the archive folder.  * The FileLink class tries to abstract this concept and given a set of locations  * it is able to switch between them making this operation transparent for the user.  * {@link HFileLink} is a more concrete implementation of the {@code FileLink}.  *  *<p><b>Back-references:</b>  * To help the {@link org.apache.hadoop.hbase.master.cleaner.CleanerChore} to keep track of  * the links to a particular file, during the {@code FileLink} creation, a new file is placed  * inside a back-reference directory. There's one back-reference directory for each file that  * has links, and in the directory there's one file per link.  *  *<p>HFileLink Example  *<ul>  *<li>  *      /hbase/table/region-x/cf/file-k  *      (Original File)  *</li>  *<li>  *      /hbase/table-cloned/region-y/cf/file-k.region-x.table  *     (HFileLink to the original file)  *</li>  *<li>  *      /hbase/table-2nd-cloned/region-z/cf/file-k.region-x.table  *      (HFileLink to the original file)  *</li>  *<li>  *      /hbase/.archive/table/region-x/.links-file-k/region-y.table-cloned  *      (Back-reference to the link in table-cloned)  *</li>  *<li>  *      /hbase/.archive/table/region-x/.links-file-k/region-z.table-2nd-cloned  *      (Back-reference to the link in table-2nd-cloned)  *</li>  *</ul>  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|FileLink
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
name|FileLink
operator|.
name|class
argument_list|)
decl_stmt|;
comment|/** Define the Back-reference directory name prefix: .links-&lt;hfile&gt;/ */
specifier|public
specifier|static
specifier|final
name|String
name|BACK_REFERENCES_DIRECTORY_PREFIX
init|=
literal|".links-"
decl_stmt|;
comment|/**    * FileLink InputStream that handles the switch between the original path    * and the alternative locations, when the file is moved.    */
specifier|private
specifier|static
class|class
name|FileLinkInputStream
extends|extends
name|InputStream
implements|implements
name|Seekable
implements|,
name|PositionedReadable
implements|,
name|CanSetDropBehind
implements|,
name|CanSetReadahead
block|{
specifier|private
name|FSDataInputStream
name|in
init|=
literal|null
decl_stmt|;
specifier|private
name|Path
name|currentPath
init|=
literal|null
decl_stmt|;
specifier|private
name|long
name|pos
init|=
literal|0
decl_stmt|;
specifier|private
specifier|final
name|FileLink
name|fileLink
decl_stmt|;
specifier|private
specifier|final
name|int
name|bufferSize
decl_stmt|;
specifier|private
specifier|final
name|FileSystem
name|fs
decl_stmt|;
specifier|public
name|FileLinkInputStream
parameter_list|(
specifier|final
name|FileSystem
name|fs
parameter_list|,
specifier|final
name|FileLink
name|fileLink
parameter_list|)
throws|throws
name|IOException
block|{
name|this
argument_list|(
name|fs
argument_list|,
name|fileLink
argument_list|,
name|FSUtils
operator|.
name|getDefaultBufferSize
argument_list|(
name|fs
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|public
name|FileLinkInputStream
parameter_list|(
specifier|final
name|FileSystem
name|fs
parameter_list|,
specifier|final
name|FileLink
name|fileLink
parameter_list|,
name|int
name|bufferSize
parameter_list|)
throws|throws
name|IOException
block|{
name|this
operator|.
name|bufferSize
operator|=
name|bufferSize
expr_stmt|;
name|this
operator|.
name|fileLink
operator|=
name|fileLink
expr_stmt|;
name|this
operator|.
name|fs
operator|=
name|fs
expr_stmt|;
name|this
operator|.
name|in
operator|=
name|tryOpen
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|read
parameter_list|()
throws|throws
name|IOException
block|{
name|int
name|res
decl_stmt|;
try|try
block|{
name|res
operator|=
name|in
operator|.
name|read
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|FileNotFoundException
name|e
parameter_list|)
block|{
name|res
operator|=
name|tryOpen
argument_list|()
operator|.
name|read
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|NullPointerException
name|e
parameter_list|)
block|{
comment|// HDFS 1.x - DFSInputStream.getBlockAt()
name|res
operator|=
name|tryOpen
argument_list|()
operator|.
name|read
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|AssertionError
name|e
parameter_list|)
block|{
comment|// assert in HDFS 1.x - DFSInputStream.getBlockAt()
name|res
operator|=
name|tryOpen
argument_list|()
operator|.
name|read
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|res
operator|>
literal|0
condition|)
name|pos
operator|+=
literal|1
expr_stmt|;
return|return
name|res
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|read
parameter_list|(
name|byte
index|[]
name|b
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|read
argument_list|(
name|b
argument_list|,
literal|0
argument_list|,
name|b
operator|.
name|length
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|read
parameter_list|(
name|byte
index|[]
name|b
parameter_list|,
name|int
name|off
parameter_list|,
name|int
name|len
parameter_list|)
throws|throws
name|IOException
block|{
name|int
name|n
decl_stmt|;
try|try
block|{
name|n
operator|=
name|in
operator|.
name|read
argument_list|(
name|b
argument_list|,
name|off
argument_list|,
name|len
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|FileNotFoundException
name|e
parameter_list|)
block|{
name|n
operator|=
name|tryOpen
argument_list|()
operator|.
name|read
argument_list|(
name|b
argument_list|,
name|off
argument_list|,
name|len
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|NullPointerException
name|e
parameter_list|)
block|{
comment|// HDFS 1.x - DFSInputStream.getBlockAt()
name|n
operator|=
name|tryOpen
argument_list|()
operator|.
name|read
argument_list|(
name|b
argument_list|,
name|off
argument_list|,
name|len
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|AssertionError
name|e
parameter_list|)
block|{
comment|// assert in HDFS 1.x - DFSInputStream.getBlockAt()
name|n
operator|=
name|tryOpen
argument_list|()
operator|.
name|read
argument_list|(
name|b
argument_list|,
name|off
argument_list|,
name|len
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|n
operator|>
literal|0
condition|)
name|pos
operator|+=
name|n
expr_stmt|;
assert|assert
operator|(
name|in
operator|.
name|getPos
argument_list|()
operator|==
name|pos
operator|)
assert|;
return|return
name|n
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|read
parameter_list|(
name|long
name|position
parameter_list|,
name|byte
index|[]
name|buffer
parameter_list|,
name|int
name|offset
parameter_list|,
name|int
name|length
parameter_list|)
throws|throws
name|IOException
block|{
name|int
name|n
decl_stmt|;
try|try
block|{
name|n
operator|=
name|in
operator|.
name|read
argument_list|(
name|position
argument_list|,
name|buffer
argument_list|,
name|offset
argument_list|,
name|length
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|FileNotFoundException
name|e
parameter_list|)
block|{
name|n
operator|=
name|tryOpen
argument_list|()
operator|.
name|read
argument_list|(
name|position
argument_list|,
name|buffer
argument_list|,
name|offset
argument_list|,
name|length
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|NullPointerException
name|e
parameter_list|)
block|{
comment|// HDFS 1.x - DFSInputStream.getBlockAt()
name|n
operator|=
name|tryOpen
argument_list|()
operator|.
name|read
argument_list|(
name|position
argument_list|,
name|buffer
argument_list|,
name|offset
argument_list|,
name|length
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|AssertionError
name|e
parameter_list|)
block|{
comment|// assert in HDFS 1.x - DFSInputStream.getBlockAt()
name|n
operator|=
name|tryOpen
argument_list|()
operator|.
name|read
argument_list|(
name|position
argument_list|,
name|buffer
argument_list|,
name|offset
argument_list|,
name|length
argument_list|)
expr_stmt|;
block|}
return|return
name|n
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|readFully
parameter_list|(
name|long
name|position
parameter_list|,
name|byte
index|[]
name|buffer
parameter_list|)
throws|throws
name|IOException
block|{
name|readFully
argument_list|(
name|position
argument_list|,
name|buffer
argument_list|,
literal|0
argument_list|,
name|buffer
operator|.
name|length
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|readFully
parameter_list|(
name|long
name|position
parameter_list|,
name|byte
index|[]
name|buffer
parameter_list|,
name|int
name|offset
parameter_list|,
name|int
name|length
parameter_list|)
throws|throws
name|IOException
block|{
try|try
block|{
name|in
operator|.
name|readFully
argument_list|(
name|position
argument_list|,
name|buffer
argument_list|,
name|offset
argument_list|,
name|length
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|FileNotFoundException
name|e
parameter_list|)
block|{
name|tryOpen
argument_list|()
operator|.
name|readFully
argument_list|(
name|position
argument_list|,
name|buffer
argument_list|,
name|offset
argument_list|,
name|length
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|NullPointerException
name|e
parameter_list|)
block|{
comment|// HDFS 1.x - DFSInputStream.getBlockAt()
name|tryOpen
argument_list|()
operator|.
name|readFully
argument_list|(
name|position
argument_list|,
name|buffer
argument_list|,
name|offset
argument_list|,
name|length
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|AssertionError
name|e
parameter_list|)
block|{
comment|// assert in HDFS 1.x - DFSInputStream.getBlockAt()
name|tryOpen
argument_list|()
operator|.
name|readFully
argument_list|(
name|position
argument_list|,
name|buffer
argument_list|,
name|offset
argument_list|,
name|length
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|long
name|skip
parameter_list|(
name|long
name|n
parameter_list|)
throws|throws
name|IOException
block|{
name|long
name|skipped
decl_stmt|;
try|try
block|{
name|skipped
operator|=
name|in
operator|.
name|skip
argument_list|(
name|n
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|FileNotFoundException
name|e
parameter_list|)
block|{
name|skipped
operator|=
name|tryOpen
argument_list|()
operator|.
name|skip
argument_list|(
name|n
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|NullPointerException
name|e
parameter_list|)
block|{
comment|// HDFS 1.x - DFSInputStream.getBlockAt()
name|skipped
operator|=
name|tryOpen
argument_list|()
operator|.
name|skip
argument_list|(
name|n
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|AssertionError
name|e
parameter_list|)
block|{
comment|// assert in HDFS 1.x - DFSInputStream.getBlockAt()
name|skipped
operator|=
name|tryOpen
argument_list|()
operator|.
name|skip
argument_list|(
name|n
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|skipped
operator|>
literal|0
condition|)
name|pos
operator|+=
name|skipped
expr_stmt|;
return|return
name|skipped
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|available
parameter_list|()
throws|throws
name|IOException
block|{
try|try
block|{
return|return
name|in
operator|.
name|available
argument_list|()
return|;
block|}
catch|catch
parameter_list|(
name|FileNotFoundException
name|e
parameter_list|)
block|{
return|return
name|tryOpen
argument_list|()
operator|.
name|available
argument_list|()
return|;
block|}
catch|catch
parameter_list|(
name|NullPointerException
name|e
parameter_list|)
block|{
comment|// HDFS 1.x - DFSInputStream.getBlockAt()
return|return
name|tryOpen
argument_list|()
operator|.
name|available
argument_list|()
return|;
block|}
catch|catch
parameter_list|(
name|AssertionError
name|e
parameter_list|)
block|{
comment|// assert in HDFS 1.x - DFSInputStream.getBlockAt()
return|return
name|tryOpen
argument_list|()
operator|.
name|available
argument_list|()
return|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|seek
parameter_list|(
name|long
name|pos
parameter_list|)
throws|throws
name|IOException
block|{
try|try
block|{
name|in
operator|.
name|seek
argument_list|(
name|pos
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|FileNotFoundException
name|e
parameter_list|)
block|{
name|tryOpen
argument_list|()
operator|.
name|seek
argument_list|(
name|pos
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|NullPointerException
name|e
parameter_list|)
block|{
comment|// HDFS 1.x - DFSInputStream.getBlockAt()
name|tryOpen
argument_list|()
operator|.
name|seek
argument_list|(
name|pos
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|AssertionError
name|e
parameter_list|)
block|{
comment|// assert in HDFS 1.x - DFSInputStream.getBlockAt()
name|tryOpen
argument_list|()
operator|.
name|seek
argument_list|(
name|pos
argument_list|)
expr_stmt|;
block|}
name|this
operator|.
name|pos
operator|=
name|pos
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getPos
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|pos
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|seekToNewSource
parameter_list|(
name|long
name|targetPos
parameter_list|)
throws|throws
name|IOException
block|{
name|boolean
name|res
decl_stmt|;
try|try
block|{
name|res
operator|=
name|in
operator|.
name|seekToNewSource
argument_list|(
name|targetPos
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|FileNotFoundException
name|e
parameter_list|)
block|{
name|res
operator|=
name|tryOpen
argument_list|()
operator|.
name|seekToNewSource
argument_list|(
name|targetPos
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|NullPointerException
name|e
parameter_list|)
block|{
comment|// HDFS 1.x - DFSInputStream.getBlockAt()
name|res
operator|=
name|tryOpen
argument_list|()
operator|.
name|seekToNewSource
argument_list|(
name|targetPos
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|AssertionError
name|e
parameter_list|)
block|{
comment|// assert in HDFS 1.x - DFSInputStream.getBlockAt()
name|res
operator|=
name|tryOpen
argument_list|()
operator|.
name|seekToNewSource
argument_list|(
name|targetPos
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|res
condition|)
name|pos
operator|=
name|targetPos
expr_stmt|;
return|return
name|res
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|()
throws|throws
name|IOException
block|{
name|in
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
specifier|synchronized
name|void
name|mark
parameter_list|(
name|int
name|readlimit
parameter_list|)
block|{     }
annotation|@
name|Override
specifier|public
specifier|synchronized
name|void
name|reset
parameter_list|()
throws|throws
name|IOException
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"mark/reset not supported"
argument_list|)
throw|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|markSupported
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
comment|/**      * Try to open the file from one of the available locations.      *      * @return FSDataInputStream stream of the opened file link      * @throws IOException on unexpected error, or file not found.      */
specifier|private
name|FSDataInputStream
name|tryOpen
parameter_list|()
throws|throws
name|IOException
block|{
for|for
control|(
name|Path
name|path
range|:
name|fileLink
operator|.
name|getLocations
argument_list|()
control|)
block|{
if|if
condition|(
name|path
operator|.
name|equals
argument_list|(
name|currentPath
argument_list|)
condition|)
continue|continue;
try|try
block|{
name|in
operator|=
name|fs
operator|.
name|open
argument_list|(
name|path
argument_list|,
name|bufferSize
argument_list|)
expr_stmt|;
if|if
condition|(
name|pos
operator|!=
literal|0
condition|)
name|in
operator|.
name|seek
argument_list|(
name|pos
argument_list|)
expr_stmt|;
assert|assert
operator|(
name|in
operator|.
name|getPos
argument_list|()
operator|==
name|pos
operator|)
operator|:
literal|"Link unable to seek to the right position="
operator|+
name|pos
assert|;
if|if
condition|(
name|LOG
operator|.
name|isTraceEnabled
argument_list|()
condition|)
block|{
if|if
condition|(
name|currentPath
operator|==
literal|null
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"link open path="
operator|+
name|path
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|LOG
operator|.
name|trace
argument_list|(
literal|"link switch from path="
operator|+
name|currentPath
operator|+
literal|" to path="
operator|+
name|path
argument_list|)
expr_stmt|;
block|}
block|}
name|currentPath
operator|=
name|path
expr_stmt|;
return|return
operator|(
name|in
operator|)
return|;
block|}
catch|catch
parameter_list|(
name|FileNotFoundException
name|e
parameter_list|)
block|{
comment|// Try another file location
block|}
catch|catch
parameter_list|(
name|RemoteException
name|re
parameter_list|)
block|{
name|IOException
name|ioe
init|=
name|re
operator|.
name|unwrapRemoteException
argument_list|(
name|FileNotFoundException
operator|.
name|class
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
operator|(
name|ioe
operator|instanceof
name|FileNotFoundException
operator|)
condition|)
throw|throw
name|re
throw|;
block|}
block|}
throw|throw
operator|new
name|FileNotFoundException
argument_list|(
literal|"Unable to open link: "
operator|+
name|fileLink
argument_list|)
throw|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setReadahead
parameter_list|(
name|Long
name|readahead
parameter_list|)
throws|throws
name|IOException
throws|,
name|UnsupportedOperationException
block|{
name|in
operator|.
name|setReadahead
argument_list|(
name|readahead
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setDropBehind
parameter_list|(
name|Boolean
name|dropCache
parameter_list|)
throws|throws
name|IOException
throws|,
name|UnsupportedOperationException
block|{
name|in
operator|.
name|setDropBehind
argument_list|(
name|dropCache
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|Path
index|[]
name|locations
init|=
literal|null
decl_stmt|;
specifier|protected
name|FileLink
parameter_list|()
block|{
name|this
operator|.
name|locations
operator|=
literal|null
expr_stmt|;
block|}
comment|/**    * @param originPath Original location of the file to link    * @param alternativePaths Alternative locations to look for the linked file    */
specifier|public
name|FileLink
parameter_list|(
name|Path
name|originPath
parameter_list|,
name|Path
modifier|...
name|alternativePaths
parameter_list|)
block|{
name|setLocations
argument_list|(
name|originPath
argument_list|,
name|alternativePaths
argument_list|)
expr_stmt|;
block|}
comment|/**    * @param locations locations to look for the linked file    */
specifier|public
name|FileLink
parameter_list|(
specifier|final
name|Collection
argument_list|<
name|Path
argument_list|>
name|locations
parameter_list|)
block|{
name|this
operator|.
name|locations
operator|=
name|locations
operator|.
name|toArray
argument_list|(
operator|new
name|Path
index|[
name|locations
operator|.
name|size
argument_list|()
index|]
argument_list|)
expr_stmt|;
block|}
comment|/**    * @return the locations to look for the linked file.    */
specifier|public
name|Path
index|[]
name|getLocations
parameter_list|()
block|{
return|return
name|locations
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
name|StringBuilder
name|str
init|=
operator|new
name|StringBuilder
argument_list|(
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
name|str
operator|.
name|append
argument_list|(
literal|" locations=["
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|locations
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
if|if
condition|(
name|i
operator|>
literal|0
condition|)
name|str
operator|.
name|append
argument_list|(
literal|", "
argument_list|)
expr_stmt|;
name|str
operator|.
name|append
argument_list|(
name|locations
index|[
name|i
index|]
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|str
operator|.
name|append
argument_list|(
literal|"]"
argument_list|)
expr_stmt|;
return|return
name|str
operator|.
name|toString
argument_list|()
return|;
block|}
comment|/**    * @return true if the file pointed by the link exists    */
specifier|public
name|boolean
name|exists
parameter_list|(
specifier|final
name|FileSystem
name|fs
parameter_list|)
throws|throws
name|IOException
block|{
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|locations
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
if|if
condition|(
name|fs
operator|.
name|exists
argument_list|(
name|locations
index|[
name|i
index|]
argument_list|)
condition|)
block|{
return|return
literal|true
return|;
block|}
block|}
return|return
literal|false
return|;
block|}
comment|/**    * @return the path of the first available link.    */
specifier|public
name|Path
name|getAvailablePath
parameter_list|(
name|FileSystem
name|fs
parameter_list|)
throws|throws
name|IOException
block|{
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|locations
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
if|if
condition|(
name|fs
operator|.
name|exists
argument_list|(
name|locations
index|[
name|i
index|]
argument_list|)
condition|)
block|{
return|return
name|locations
index|[
name|i
index|]
return|;
block|}
block|}
throw|throw
operator|new
name|FileNotFoundException
argument_list|(
literal|"Unable to open link: "
operator|+
name|this
argument_list|)
throw|;
block|}
comment|/**    * Get the FileStatus of the referenced file.    *    * @param fs {@link FileSystem} on which to get the file status    * @return InputStream for the hfile link.    * @throws IOException on unexpected error.    */
specifier|public
name|FileStatus
name|getFileStatus
parameter_list|(
name|FileSystem
name|fs
parameter_list|)
throws|throws
name|IOException
block|{
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|locations
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
try|try
block|{
return|return
name|fs
operator|.
name|getFileStatus
argument_list|(
name|locations
index|[
name|i
index|]
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|FileNotFoundException
name|e
parameter_list|)
block|{
comment|// Try another file location
block|}
block|}
throw|throw
operator|new
name|FileNotFoundException
argument_list|(
literal|"Unable to open link: "
operator|+
name|this
argument_list|)
throw|;
block|}
comment|/**    * Open the FileLink for read.    *<p>    * It uses a wrapper of FSDataInputStream that is agnostic to the location    * of the file, even if the file switches between locations.    *    * @param fs {@link FileSystem} on which to open the FileLink    * @return InputStream for reading the file link.    * @throws IOException on unexpected error.    */
specifier|public
name|FSDataInputStream
name|open
parameter_list|(
specifier|final
name|FileSystem
name|fs
parameter_list|)
throws|throws
name|IOException
block|{
return|return
operator|new
name|FSDataInputStream
argument_list|(
operator|new
name|FileLinkInputStream
argument_list|(
name|fs
argument_list|,
name|this
argument_list|)
argument_list|)
return|;
block|}
comment|/**    * Open the FileLink for read.    *<p>    * It uses a wrapper of FSDataInputStream that is agnostic to the location    * of the file, even if the file switches between locations.    *    * @param fs {@link FileSystem} on which to open the FileLink    * @param bufferSize the size of the buffer to be used.    * @return InputStream for reading the file link.    * @throws IOException on unexpected error.    */
specifier|public
name|FSDataInputStream
name|open
parameter_list|(
specifier|final
name|FileSystem
name|fs
parameter_list|,
name|int
name|bufferSize
parameter_list|)
throws|throws
name|IOException
block|{
return|return
operator|new
name|FSDataInputStream
argument_list|(
operator|new
name|FileLinkInputStream
argument_list|(
name|fs
argument_list|,
name|this
argument_list|,
name|bufferSize
argument_list|)
argument_list|)
return|;
block|}
comment|/**    * NOTE: This method must be used only in the constructor!    * It creates a List with the specified locations for the link.    */
specifier|protected
name|void
name|setLocations
parameter_list|(
name|Path
name|originPath
parameter_list|,
name|Path
modifier|...
name|alternativePaths
parameter_list|)
block|{
assert|assert
name|this
operator|.
name|locations
operator|==
literal|null
operator|:
literal|"Link locations already set"
assert|;
name|List
argument_list|<
name|Path
argument_list|>
name|paths
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|alternativePaths
operator|.
name|length
operator|+
literal|1
argument_list|)
decl_stmt|;
if|if
condition|(
name|originPath
operator|!=
literal|null
condition|)
block|{
name|paths
operator|.
name|add
argument_list|(
name|originPath
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|alternativePaths
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|alternativePaths
index|[
name|i
index|]
operator|!=
literal|null
condition|)
block|{
name|paths
operator|.
name|add
argument_list|(
name|alternativePaths
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
block|}
name|this
operator|.
name|locations
operator|=
name|paths
operator|.
name|toArray
argument_list|(
operator|new
name|Path
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
block|}
comment|/**    * Get the directory to store the link back references    *    *<p>To simplify the reference count process, during the FileLink creation    * a back-reference is added to the back-reference directory of the specified file.    *    * @param storeDir Root directory for the link reference folder    * @param fileName File Name with links    * @return Path for the link back references.    */
specifier|public
specifier|static
name|Path
name|getBackReferencesDir
parameter_list|(
specifier|final
name|Path
name|storeDir
parameter_list|,
specifier|final
name|String
name|fileName
parameter_list|)
block|{
return|return
operator|new
name|Path
argument_list|(
name|storeDir
argument_list|,
name|BACK_REFERENCES_DIRECTORY_PREFIX
operator|+
name|fileName
argument_list|)
return|;
block|}
comment|/**    * Get the referenced file name from the reference link directory path.    *    * @param dirPath Link references directory path    * @return Name of the file referenced    */
specifier|public
specifier|static
name|String
name|getBackReferenceFileName
parameter_list|(
specifier|final
name|Path
name|dirPath
parameter_list|)
block|{
return|return
name|dirPath
operator|.
name|getName
argument_list|()
operator|.
name|substring
argument_list|(
name|BACK_REFERENCES_DIRECTORY_PREFIX
operator|.
name|length
argument_list|()
argument_list|)
return|;
block|}
comment|/**    * Checks if the specified directory path is a back reference links folder.    *    * @param dirPath Directory path to verify    * @return True if the specified directory is a link references folder    */
specifier|public
specifier|static
name|boolean
name|isBackReferencesDir
parameter_list|(
specifier|final
name|Path
name|dirPath
parameter_list|)
block|{
if|if
condition|(
name|dirPath
operator|==
literal|null
condition|)
return|return
literal|false
return|;
return|return
name|dirPath
operator|.
name|getName
argument_list|()
operator|.
name|startsWith
argument_list|(
name|BACK_REFERENCES_DIRECTORY_PREFIX
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|equals
parameter_list|(
name|Object
name|obj
parameter_list|)
block|{
if|if
condition|(
name|obj
operator|==
literal|null
condition|)
block|{
return|return
literal|false
return|;
block|}
comment|// Assumes that the ordering of locations between objects are the same. This is true for the
comment|// current subclasses already (HFileLink, WALLink). Otherwise, we may have to sort the locations
comment|// or keep them presorted
if|if
condition|(
name|this
operator|.
name|getClass
argument_list|()
operator|.
name|equals
argument_list|(
name|obj
operator|.
name|getClass
argument_list|()
argument_list|)
condition|)
block|{
return|return
name|Arrays
operator|.
name|equals
argument_list|(
name|this
operator|.
name|locations
argument_list|,
operator|(
operator|(
name|FileLink
operator|)
name|obj
operator|)
operator|.
name|locations
argument_list|)
return|;
block|}
return|return
literal|false
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
return|return
name|Arrays
operator|.
name|hashCode
argument_list|(
name|locations
argument_list|)
return|;
block|}
block|}
end_class

end_unit

