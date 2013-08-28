begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|regionserver
operator|.
name|wal
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|FilterInputStream
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
name|lang
operator|.
name|reflect
operator|.
name|Field
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
name|java
operator|.
name|util
operator|.
name|NavigableMap
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
name|conf
operator|.
name|Configuration
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
name|regionserver
operator|.
name|wal
operator|.
name|HLog
operator|.
name|Entry
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
name|io
operator|.
name|SequenceFile
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
name|io
operator|.
name|SequenceFile
operator|.
name|Metadata
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
name|io
operator|.
name|Text
import|;
end_import

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|SequenceFileLogReader
extends|extends
name|ReaderBase
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
name|SequenceFileLogReader
operator|.
name|class
argument_list|)
decl_stmt|;
comment|// Legacy stuff from pre-PB WAL metadata.
specifier|private
specifier|static
specifier|final
name|Text
name|WAL_VERSION_KEY
init|=
operator|new
name|Text
argument_list|(
literal|"version"
argument_list|)
decl_stmt|;
comment|// Let the version be 1.  Let absence of a version meta tag be old, version 0.
comment|// Set this version '1' to be the version that introduces compression,
comment|// the COMPRESSION_VERSION.
specifier|private
specifier|static
specifier|final
name|int
name|COMPRESSION_VERSION
init|=
literal|1
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|Text
name|WAL_COMPRESSION_TYPE_KEY
init|=
operator|new
name|Text
argument_list|(
literal|"compression.type"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|Text
name|DICTIONARY_COMPRESSION_TYPE
init|=
operator|new
name|Text
argument_list|(
literal|"dictionary"
argument_list|)
decl_stmt|;
comment|/**    * Hack just to set the correct file length up in SequenceFile.Reader.    * See HADOOP-6307.  The below is all about setting the right length on the    * file we are reading.  fs.getFileStatus(file).getLen() is passed down to    * a private SequenceFile.Reader constructor.  This won't work.  Need to do    * the available on the stream.  The below is ugly.  It makes getPos, the    * first time its called, return length of the file -- i.e. tell a lie -- just    * so this line up in SF.Reader's constructor ends up with right answer:    *    *         this.end = in.getPos() + length;    *    */
specifier|private
specifier|static
class|class
name|WALReader
extends|extends
name|SequenceFile
operator|.
name|Reader
block|{
name|WALReader
parameter_list|(
specifier|final
name|FileSystem
name|fs
parameter_list|,
specifier|final
name|Path
name|p
parameter_list|,
specifier|final
name|Configuration
name|c
parameter_list|)
throws|throws
name|IOException
block|{
name|super
argument_list|(
name|fs
argument_list|,
name|p
argument_list|,
name|c
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|FSDataInputStream
name|openFile
parameter_list|(
name|FileSystem
name|fs
parameter_list|,
name|Path
name|file
parameter_list|,
name|int
name|bufferSize
parameter_list|,
name|long
name|length
parameter_list|)
throws|throws
name|IOException
block|{
return|return
operator|new
name|WALReaderFSDataInputStream
argument_list|(
name|super
operator|.
name|openFile
argument_list|(
name|fs
argument_list|,
name|file
argument_list|,
name|bufferSize
argument_list|,
name|length
argument_list|)
argument_list|,
name|length
argument_list|)
return|;
block|}
comment|/**      * Override just so can intercept first call to getPos.      */
specifier|static
class|class
name|WALReaderFSDataInputStream
extends|extends
name|FSDataInputStream
block|{
specifier|private
name|boolean
name|firstGetPosInvocation
init|=
literal|true
decl_stmt|;
specifier|private
name|long
name|length
decl_stmt|;
name|WALReaderFSDataInputStream
parameter_list|(
specifier|final
name|FSDataInputStream
name|is
parameter_list|,
specifier|final
name|long
name|l
parameter_list|)
throws|throws
name|IOException
block|{
name|super
argument_list|(
name|is
argument_list|)
expr_stmt|;
name|this
operator|.
name|length
operator|=
name|l
expr_stmt|;
block|}
comment|// This section can be confusing.  It is specific to how HDFS works.
comment|// Let me try to break it down.  This is the problem:
comment|//
comment|//  1. HDFS DataNodes update the NameNode about a filename's length
comment|//     on block boundaries or when a file is closed. Therefore,
comment|//     if an RS dies, then the NN's fs.getLength() can be out of date
comment|//  2. this.in.available() would work, but it returns int&
comment|//     therefore breaks for files> 2GB (happens on big clusters)
comment|//  3. DFSInputStream.getFileLength() gets the actual length from the DNs
comment|//  4. DFSInputStream is wrapped 2 levels deep : this.in.in
comment|//
comment|// So, here we adjust getPos() using getFileLength() so the
comment|// SequenceFile.Reader constructor (aka: first invocation) comes out
comment|// with the correct end of the file:
comment|//         this.end = in.getPos() + length;
annotation|@
name|Override
specifier|public
name|long
name|getPos
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|this
operator|.
name|firstGetPosInvocation
condition|)
block|{
name|this
operator|.
name|firstGetPosInvocation
operator|=
literal|false
expr_stmt|;
name|long
name|adjust
init|=
literal|0
decl_stmt|;
try|try
block|{
name|Field
name|fIn
init|=
name|FilterInputStream
operator|.
name|class
operator|.
name|getDeclaredField
argument_list|(
literal|"in"
argument_list|)
decl_stmt|;
name|fIn
operator|.
name|setAccessible
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|Object
name|realIn
init|=
name|fIn
operator|.
name|get
argument_list|(
name|this
operator|.
name|in
argument_list|)
decl_stmt|;
comment|// In hadoop 0.22, DFSInputStream is a standalone class.  Before this,
comment|// it was an inner class of DFSClient.
if|if
condition|(
name|realIn
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
operator|.
name|endsWith
argument_list|(
literal|"DFSInputStream"
argument_list|)
condition|)
block|{
name|Method
name|getFileLength
init|=
name|realIn
operator|.
name|getClass
argument_list|()
operator|.
name|getDeclaredMethod
argument_list|(
literal|"getFileLength"
argument_list|,
operator|new
name|Class
argument_list|<
name|?
argument_list|>
index|[]
block|{}
block|)
empty_stmt|;
name|getFileLength
operator|.
name|setAccessible
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|long
name|realLength
init|=
operator|(
operator|(
name|Long
operator|)
name|getFileLength
operator|.
name|invoke
argument_list|(
name|realIn
argument_list|,
operator|new
name|Object
index|[]
block|{}
argument_list|)
operator|)
operator|.
name|longValue
argument_list|()
decl_stmt|;
assert|assert
operator|(
name|realLength
operator|>=
name|this
operator|.
name|length
operator|)
assert|;
name|adjust
operator|=
name|realLength
operator|-
name|this
operator|.
name|length
expr_stmt|;
block|}
else|else
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Input stream class: "
operator|+
name|realIn
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
operator|+
literal|", not adjusting length"
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|SequenceFileLogReader
operator|.
name|LOG
operator|.
name|warn
argument_list|(
literal|"Error while trying to get accurate file length.  "
operator|+
literal|"Truncation / data loss may occur if RegionServers die."
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
return|return
name|adjust
operator|+
name|super
operator|.
name|getPos
argument_list|()
return|;
block|}
return|return
name|super
operator|.
name|getPos
argument_list|()
return|;
block|}
block|}
block|}
end_class

begin_comment
comment|// Protected for tests.
end_comment

begin_decl_stmt
specifier|protected
name|SequenceFile
operator|.
name|Reader
name|reader
decl_stmt|;
end_decl_stmt

begin_decl_stmt
name|long
name|entryStart
init|=
literal|0
decl_stmt|;
end_decl_stmt

begin_comment
comment|// needed for logging exceptions
end_comment

begin_constructor
specifier|public
name|SequenceFileLogReader
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
block|}
end_constructor

begin_function
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|()
throws|throws
name|IOException
block|{
try|try
block|{
if|if
condition|(
name|reader
operator|!=
literal|null
condition|)
block|{
name|this
operator|.
name|reader
operator|.
name|close
argument_list|()
expr_stmt|;
name|this
operator|.
name|reader
operator|=
literal|null
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|ioe
parameter_list|)
block|{
throw|throw
name|addFileInfoToException
argument_list|(
name|ioe
argument_list|)
throw|;
block|}
block|}
end_function

begin_function
annotation|@
name|Override
specifier|public
name|long
name|getPosition
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|reader
operator|!=
literal|null
condition|?
name|reader
operator|.
name|getPosition
argument_list|()
else|:
literal|0
return|;
block|}
end_function

begin_function
annotation|@
name|Override
specifier|public
name|void
name|reset
parameter_list|()
throws|throws
name|IOException
block|{
comment|// Resetting the reader lets us see newly added data if the file is being written to
comment|// We also keep the same compressionContext which was previously populated for this file
name|reader
operator|=
operator|new
name|WALReader
argument_list|(
name|fs
argument_list|,
name|path
argument_list|,
name|conf
argument_list|)
expr_stmt|;
block|}
end_function

begin_function
annotation|@
name|Override
specifier|protected
name|void
name|initReader
parameter_list|(
name|FSDataInputStream
name|stream
parameter_list|)
throws|throws
name|IOException
block|{
comment|// We don't use the stream because we have to have the magic stream above.
if|if
condition|(
name|stream
operator|!=
literal|null
condition|)
block|{
name|stream
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
name|reset
argument_list|()
expr_stmt|;
block|}
end_function

begin_function
annotation|@
name|Override
specifier|protected
name|void
name|initAfterCompression
parameter_list|()
throws|throws
name|IOException
block|{
comment|// Nothing to do here
block|}
end_function

begin_function
annotation|@
name|Override
specifier|protected
name|boolean
name|hasCompression
parameter_list|()
block|{
return|return
name|isWALCompressionEnabled
argument_list|(
name|reader
operator|.
name|getMetadata
argument_list|()
argument_list|)
return|;
block|}
end_function

begin_comment
comment|/**    * Call this method after init() has been executed    * @return whether WAL compression is enabled    */
end_comment

begin_function
specifier|static
name|boolean
name|isWALCompressionEnabled
parameter_list|(
specifier|final
name|Metadata
name|metadata
parameter_list|)
block|{
comment|// Check version is>= VERSION?
name|Text
name|txt
init|=
name|metadata
operator|.
name|get
argument_list|(
name|WAL_VERSION_KEY
argument_list|)
decl_stmt|;
if|if
condition|(
name|txt
operator|==
literal|null
operator|||
name|Integer
operator|.
name|parseInt
argument_list|(
name|txt
operator|.
name|toString
argument_list|()
argument_list|)
operator|<
name|COMPRESSION_VERSION
condition|)
block|{
return|return
literal|false
return|;
block|}
comment|// Now check that compression type is present.  Currently only one value.
name|txt
operator|=
name|metadata
operator|.
name|get
argument_list|(
name|WAL_COMPRESSION_TYPE_KEY
argument_list|)
expr_stmt|;
return|return
name|txt
operator|!=
literal|null
operator|&&
name|txt
operator|.
name|equals
argument_list|(
name|DICTIONARY_COMPRESSION_TYPE
argument_list|)
return|;
block|}
end_function

begin_function
annotation|@
name|Override
specifier|protected
name|boolean
name|readNext
parameter_list|(
name|Entry
name|e
parameter_list|)
throws|throws
name|IOException
block|{
try|try
block|{
name|boolean
name|hasNext
init|=
name|this
operator|.
name|reader
operator|.
name|next
argument_list|(
name|e
operator|.
name|getKey
argument_list|()
argument_list|,
name|e
operator|.
name|getEdit
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|hasNext
condition|)
return|return
literal|false
return|;
comment|// Scopes are probably in WAL edit, move to key
name|NavigableMap
argument_list|<
name|byte
index|[]
argument_list|,
name|Integer
argument_list|>
name|scopes
init|=
name|e
operator|.
name|getEdit
argument_list|()
operator|.
name|getAndRemoveScopes
argument_list|()
decl_stmt|;
if|if
condition|(
name|scopes
operator|!=
literal|null
condition|)
block|{
name|e
operator|.
name|getKey
argument_list|()
operator|.
name|readOlderScopes
argument_list|(
name|scopes
argument_list|)
expr_stmt|;
block|}
return|return
literal|true
return|;
block|}
catch|catch
parameter_list|(
name|IOException
name|ioe
parameter_list|)
block|{
throw|throw
name|addFileInfoToException
argument_list|(
name|ioe
argument_list|)
throw|;
block|}
block|}
end_function

begin_function
annotation|@
name|Override
specifier|protected
name|void
name|seekOnFs
parameter_list|(
name|long
name|pos
parameter_list|)
throws|throws
name|IOException
block|{
try|try
block|{
name|reader
operator|.
name|seek
argument_list|(
name|pos
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|ioe
parameter_list|)
block|{
throw|throw
name|addFileInfoToException
argument_list|(
name|ioe
argument_list|)
throw|;
block|}
block|}
end_function

begin_function
specifier|protected
name|IOException
name|addFileInfoToException
parameter_list|(
specifier|final
name|IOException
name|ioe
parameter_list|)
throws|throws
name|IOException
block|{
name|long
name|pos
init|=
operator|-
literal|1
decl_stmt|;
try|try
block|{
name|pos
operator|=
name|getPosition
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Failed getting position to add to throw"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
comment|// See what SequenceFile.Reader thinks is the end of the file
name|long
name|end
init|=
name|Long
operator|.
name|MAX_VALUE
decl_stmt|;
try|try
block|{
name|Field
name|fEnd
init|=
name|SequenceFile
operator|.
name|Reader
operator|.
name|class
operator|.
name|getDeclaredField
argument_list|(
literal|"end"
argument_list|)
decl_stmt|;
name|fEnd
operator|.
name|setAccessible
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|end
operator|=
name|fEnd
operator|.
name|getLong
argument_list|(
name|this
operator|.
name|reader
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
comment|/* reflection fail. keep going */
block|}
name|String
name|msg
init|=
operator|(
name|this
operator|.
name|path
operator|==
literal|null
condition|?
literal|""
else|:
name|this
operator|.
name|path
operator|.
name|toString
argument_list|()
operator|)
operator|+
literal|", entryStart="
operator|+
name|entryStart
operator|+
literal|", pos="
operator|+
name|pos
operator|+
operator|(
operator|(
name|end
operator|==
name|Long
operator|.
name|MAX_VALUE
operator|)
condition|?
literal|""
else|:
literal|", end="
operator|+
name|end
operator|)
operator|+
literal|", edit="
operator|+
name|this
operator|.
name|edit
decl_stmt|;
comment|// Enhance via reflection so we don't change the original class type
try|try
block|{
return|return
operator|(
name|IOException
operator|)
name|ioe
operator|.
name|getClass
argument_list|()
operator|.
name|getConstructor
argument_list|(
name|String
operator|.
name|class
argument_list|)
operator|.
name|newInstance
argument_list|(
name|msg
argument_list|)
operator|.
name|initCause
argument_list|(
name|ioe
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
comment|/* reflection fail. keep going */
block|}
return|return
name|ioe
return|;
block|}
end_function

unit|}
end_unit

