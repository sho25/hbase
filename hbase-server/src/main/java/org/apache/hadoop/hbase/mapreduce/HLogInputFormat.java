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
name|mapreduce
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataInput
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataOutput
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|EOFException
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
name|Collections
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
name|HLogFactory
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
name|HLogKey
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
name|WALEdit
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
name|Writable
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
name|mapreduce
operator|.
name|InputFormat
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
name|mapreduce
operator|.
name|InputSplit
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
name|mapreduce
operator|.
name|JobContext
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
name|mapreduce
operator|.
name|RecordReader
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
name|mapreduce
operator|.
name|TaskAttemptContext
import|;
end_import

begin_comment
comment|/**  * Simple {@link InputFormat} for {@link HLog} files.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Public
specifier|public
class|class
name|HLogInputFormat
extends|extends
name|InputFormat
argument_list|<
name|HLogKey
argument_list|,
name|WALEdit
argument_list|>
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
name|HLogInputFormat
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|START_TIME_KEY
init|=
literal|"hlog.start.time"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|END_TIME_KEY
init|=
literal|"hlog.end.time"
decl_stmt|;
comment|/**    * {@link InputSplit} for {@link HLog} files. Each split represent    * exactly one log file.    */
specifier|static
class|class
name|HLogSplit
extends|extends
name|InputSplit
implements|implements
name|Writable
block|{
specifier|private
name|String
name|logFileName
decl_stmt|;
specifier|private
name|long
name|fileSize
decl_stmt|;
specifier|private
name|long
name|startTime
decl_stmt|;
specifier|private
name|long
name|endTime
decl_stmt|;
comment|/** for serialization */
specifier|public
name|HLogSplit
parameter_list|()
block|{}
comment|/**      * Represent an HLogSplit, i.e. a single HLog file.      * Start- and EndTime are managed by the split, so that HLog files can be      * filtered before WALEdits are passed to the mapper(s).      * @param logFileName      * @param fileSize      * @param startTime      * @param endTime      */
specifier|public
name|HLogSplit
parameter_list|(
name|String
name|logFileName
parameter_list|,
name|long
name|fileSize
parameter_list|,
name|long
name|startTime
parameter_list|,
name|long
name|endTime
parameter_list|)
block|{
name|this
operator|.
name|logFileName
operator|=
name|logFileName
expr_stmt|;
name|this
operator|.
name|fileSize
operator|=
name|fileSize
expr_stmt|;
name|this
operator|.
name|startTime
operator|=
name|startTime
expr_stmt|;
name|this
operator|.
name|endTime
operator|=
name|endTime
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getLength
parameter_list|()
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
return|return
name|fileSize
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
index|[]
name|getLocations
parameter_list|()
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
comment|// TODO: Find the data node with the most blocks for this HLog?
return|return
operator|new
name|String
index|[]
block|{}
return|;
block|}
specifier|public
name|String
name|getLogFileName
parameter_list|()
block|{
return|return
name|logFileName
return|;
block|}
specifier|public
name|long
name|getStartTime
parameter_list|()
block|{
return|return
name|startTime
return|;
block|}
specifier|public
name|long
name|getEndTime
parameter_list|()
block|{
return|return
name|endTime
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|readFields
parameter_list|(
name|DataInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|logFileName
operator|=
name|in
operator|.
name|readUTF
argument_list|()
expr_stmt|;
name|fileSize
operator|=
name|in
operator|.
name|readLong
argument_list|()
expr_stmt|;
name|startTime
operator|=
name|in
operator|.
name|readLong
argument_list|()
expr_stmt|;
name|endTime
operator|=
name|in
operator|.
name|readLong
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|write
parameter_list|(
name|DataOutput
name|out
parameter_list|)
throws|throws
name|IOException
block|{
name|out
operator|.
name|writeUTF
argument_list|(
name|logFileName
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeLong
argument_list|(
name|fileSize
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeLong
argument_list|(
name|startTime
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeLong
argument_list|(
name|endTime
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
name|logFileName
operator|+
literal|" ("
operator|+
name|startTime
operator|+
literal|":"
operator|+
name|endTime
operator|+
literal|") length:"
operator|+
name|fileSize
return|;
block|}
block|}
comment|/**    * {@link RecordReader} for an {@link HLog} file.    */
specifier|static
class|class
name|HLogRecordReader
extends|extends
name|RecordReader
argument_list|<
name|HLogKey
argument_list|,
name|WALEdit
argument_list|>
block|{
specifier|private
name|HLog
operator|.
name|Reader
name|reader
init|=
literal|null
decl_stmt|;
specifier|private
name|HLog
operator|.
name|Entry
name|currentEntry
init|=
operator|new
name|HLog
operator|.
name|Entry
argument_list|()
decl_stmt|;
specifier|private
name|long
name|startTime
decl_stmt|;
specifier|private
name|long
name|endTime
decl_stmt|;
annotation|@
name|Override
specifier|public
name|void
name|initialize
parameter_list|(
name|InputSplit
name|split
parameter_list|,
name|TaskAttemptContext
name|context
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|HLogSplit
name|hsplit
init|=
operator|(
name|HLogSplit
operator|)
name|split
decl_stmt|;
name|Path
name|logFile
init|=
operator|new
name|Path
argument_list|(
name|hsplit
operator|.
name|getLogFileName
argument_list|()
argument_list|)
decl_stmt|;
name|Configuration
name|conf
init|=
name|context
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Opening reader for "
operator|+
name|split
argument_list|)
expr_stmt|;
try|try
block|{
name|this
operator|.
name|reader
operator|=
name|HLogFactory
operator|.
name|createReader
argument_list|(
name|logFile
operator|.
name|getFileSystem
argument_list|(
name|conf
argument_list|)
argument_list|,
name|logFile
argument_list|,
name|conf
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|EOFException
name|x
parameter_list|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Ignoring corrupted HLog file: "
operator|+
name|logFile
operator|+
literal|" (This is normal when a RegionServer crashed.)"
argument_list|)
expr_stmt|;
block|}
name|this
operator|.
name|startTime
operator|=
name|hsplit
operator|.
name|getStartTime
argument_list|()
expr_stmt|;
name|this
operator|.
name|endTime
operator|=
name|hsplit
operator|.
name|getEndTime
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|nextKeyValue
parameter_list|()
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
if|if
condition|(
name|reader
operator|==
literal|null
condition|)
return|return
literal|false
return|;
name|HLog
operator|.
name|Entry
name|temp
decl_stmt|;
name|long
name|i
init|=
operator|-
literal|1
decl_stmt|;
do|do
block|{
comment|// skip older entries
try|try
block|{
name|temp
operator|=
name|reader
operator|.
name|next
argument_list|(
name|currentEntry
argument_list|)
expr_stmt|;
name|i
operator|++
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|EOFException
name|x
parameter_list|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Corrupted entry detected. Ignoring the rest of the file."
operator|+
literal|" (This is normal when a RegionServer crashed.)"
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
block|}
do|while
condition|(
name|temp
operator|!=
literal|null
operator|&&
name|temp
operator|.
name|getKey
argument_list|()
operator|.
name|getWriteTime
argument_list|()
operator|<
name|startTime
condition|)
do|;
if|if
condition|(
name|temp
operator|==
literal|null
condition|)
block|{
if|if
condition|(
name|i
operator|>
literal|0
condition|)
name|LOG
operator|.
name|info
argument_list|(
literal|"Skipped "
operator|+
name|i
operator|+
literal|" entries."
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Reached end of file."
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
elseif|else
if|if
condition|(
name|i
operator|>
literal|0
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Skipped "
operator|+
name|i
operator|+
literal|" entries, until ts: "
operator|+
name|temp
operator|.
name|getKey
argument_list|()
operator|.
name|getWriteTime
argument_list|()
operator|+
literal|"."
argument_list|)
expr_stmt|;
block|}
name|boolean
name|res
init|=
name|temp
operator|.
name|getKey
argument_list|()
operator|.
name|getWriteTime
argument_list|()
operator|<=
name|endTime
decl_stmt|;
if|if
condition|(
operator|!
name|res
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Reached ts: "
operator|+
name|temp
operator|.
name|getKey
argument_list|()
operator|.
name|getWriteTime
argument_list|()
operator|+
literal|" ignoring the rest of the file."
argument_list|)
expr_stmt|;
block|}
return|return
name|res
return|;
block|}
annotation|@
name|Override
specifier|public
name|HLogKey
name|getCurrentKey
parameter_list|()
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
return|return
name|currentEntry
operator|.
name|getKey
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|WALEdit
name|getCurrentValue
parameter_list|()
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
return|return
name|currentEntry
operator|.
name|getEdit
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|float
name|getProgress
parameter_list|()
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
comment|// N/A depends on total number of entries, which is unknown
return|return
literal|0
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
name|LOG
operator|.
name|info
argument_list|(
literal|"Closing reader"
argument_list|)
expr_stmt|;
if|if
condition|(
name|reader
operator|!=
literal|null
condition|)
name|this
operator|.
name|reader
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|InputSplit
argument_list|>
name|getSplits
parameter_list|(
name|JobContext
name|context
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|Configuration
name|conf
init|=
name|context
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
name|Path
name|inputDir
init|=
operator|new
name|Path
argument_list|(
name|conf
operator|.
name|get
argument_list|(
literal|"mapreduce.input.fileinputformat.inputdir"
argument_list|)
argument_list|)
decl_stmt|;
name|long
name|startTime
init|=
name|conf
operator|.
name|getLong
argument_list|(
name|START_TIME_KEY
argument_list|,
name|Long
operator|.
name|MIN_VALUE
argument_list|)
decl_stmt|;
name|long
name|endTime
init|=
name|conf
operator|.
name|getLong
argument_list|(
name|END_TIME_KEY
argument_list|,
name|Long
operator|.
name|MAX_VALUE
argument_list|)
decl_stmt|;
name|FileSystem
name|fs
init|=
name|inputDir
operator|.
name|getFileSystem
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|FileStatus
argument_list|>
name|files
init|=
name|getFiles
argument_list|(
name|fs
argument_list|,
name|inputDir
argument_list|,
name|startTime
argument_list|,
name|endTime
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|InputSplit
argument_list|>
name|splits
init|=
operator|new
name|ArrayList
argument_list|<
name|InputSplit
argument_list|>
argument_list|(
name|files
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|FileStatus
name|file
range|:
name|files
control|)
block|{
name|splits
operator|.
name|add
argument_list|(
operator|new
name|HLogSplit
argument_list|(
name|file
operator|.
name|getPath
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|,
name|file
operator|.
name|getLen
argument_list|()
argument_list|,
name|startTime
argument_list|,
name|endTime
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|splits
return|;
block|}
specifier|private
name|List
argument_list|<
name|FileStatus
argument_list|>
name|getFiles
parameter_list|(
name|FileSystem
name|fs
parameter_list|,
name|Path
name|dir
parameter_list|,
name|long
name|startTime
parameter_list|,
name|long
name|endTime
parameter_list|)
throws|throws
name|IOException
block|{
name|List
argument_list|<
name|FileStatus
argument_list|>
name|result
init|=
operator|new
name|ArrayList
argument_list|<
name|FileStatus
argument_list|>
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Scanning "
operator|+
name|dir
operator|.
name|toString
argument_list|()
operator|+
literal|" for HLog files"
argument_list|)
expr_stmt|;
name|FileStatus
index|[]
name|files
init|=
name|fs
operator|.
name|listStatus
argument_list|(
name|dir
argument_list|)
decl_stmt|;
if|if
condition|(
name|files
operator|==
literal|null
condition|)
return|return
name|Collections
operator|.
name|emptyList
argument_list|()
return|;
for|for
control|(
name|FileStatus
name|file
range|:
name|files
control|)
block|{
if|if
condition|(
name|file
operator|.
name|isDirectory
argument_list|()
condition|)
block|{
comment|// recurse into sub directories
name|result
operator|.
name|addAll
argument_list|(
name|getFiles
argument_list|(
name|fs
argument_list|,
name|file
operator|.
name|getPath
argument_list|()
argument_list|,
name|startTime
argument_list|,
name|endTime
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|String
name|name
init|=
name|file
operator|.
name|getPath
argument_list|()
operator|.
name|toString
argument_list|()
decl_stmt|;
name|int
name|idx
init|=
name|name
operator|.
name|lastIndexOf
argument_list|(
literal|'.'
argument_list|)
decl_stmt|;
if|if
condition|(
name|idx
operator|>
literal|0
condition|)
block|{
try|try
block|{
name|long
name|fileStartTime
init|=
name|Long
operator|.
name|parseLong
argument_list|(
name|name
operator|.
name|substring
argument_list|(
name|idx
operator|+
literal|1
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|fileStartTime
operator|<=
name|endTime
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Found: "
operator|+
name|name
argument_list|)
expr_stmt|;
name|result
operator|.
name|add
argument_list|(
name|file
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|NumberFormatException
name|x
parameter_list|)
block|{
name|idx
operator|=
literal|0
expr_stmt|;
block|}
block|}
if|if
condition|(
name|idx
operator|==
literal|0
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"File "
operator|+
name|name
operator|+
literal|" does not appear to be an HLog file. Skipping..."
argument_list|)
expr_stmt|;
block|}
block|}
block|}
return|return
name|result
return|;
block|}
annotation|@
name|Override
specifier|public
name|RecordReader
argument_list|<
name|HLogKey
argument_list|,
name|WALEdit
argument_list|>
name|createRecordReader
parameter_list|(
name|InputSplit
name|split
parameter_list|,
name|TaskAttemptContext
name|context
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
return|return
operator|new
name|HLogRecordReader
argument_list|()
return|;
block|}
block|}
end_class

end_unit

