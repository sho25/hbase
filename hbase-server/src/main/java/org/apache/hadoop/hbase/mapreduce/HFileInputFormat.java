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
name|List
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
name|fs
operator|.
name|PathFilter
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
name|Cell
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
name|io
operator|.
name|hfile
operator|.
name|HFile
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
name|io
operator|.
name|hfile
operator|.
name|HFile
operator|.
name|Reader
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
name|io
operator|.
name|hfile
operator|.
name|HFileScanner
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
name|NullWritable
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
name|lib
operator|.
name|input
operator|.
name|FileInputFormat
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
name|lib
operator|.
name|input
operator|.
name|FileSplit
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|LoggerFactory
import|;
end_import

begin_comment
comment|/**  * Simple MR input format for HFiles.  * This code was borrowed from Apache Crunch project.  * Updated to the recent version of HBase.  */
end_comment

begin_class
specifier|public
class|class
name|HFileInputFormat
extends|extends
name|FileInputFormat
argument_list|<
name|NullWritable
argument_list|,
name|Cell
argument_list|>
block|{
specifier|private
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|HFileInputFormat
operator|.
name|class
argument_list|)
decl_stmt|;
comment|/**    * File filter that removes all "hidden" files. This might be something worth removing from    * a more general purpose utility; it accounts for the presence of metadata files created    * in the way we're doing exports.    */
specifier|static
specifier|final
name|PathFilter
name|HIDDEN_FILE_FILTER
init|=
operator|new
name|PathFilter
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|boolean
name|accept
parameter_list|(
name|Path
name|p
parameter_list|)
block|{
name|String
name|name
init|=
name|p
operator|.
name|getName
argument_list|()
decl_stmt|;
return|return
operator|!
name|name
operator|.
name|startsWith
argument_list|(
literal|"_"
argument_list|)
operator|&&
operator|!
name|name
operator|.
name|startsWith
argument_list|(
literal|"."
argument_list|)
return|;
block|}
block|}
decl_stmt|;
comment|/**    * Record reader for HFiles.    */
specifier|private
specifier|static
class|class
name|HFileRecordReader
extends|extends
name|RecordReader
argument_list|<
name|NullWritable
argument_list|,
name|Cell
argument_list|>
block|{
specifier|private
name|Reader
name|in
decl_stmt|;
specifier|protected
name|Configuration
name|conf
decl_stmt|;
specifier|private
name|HFileScanner
name|scanner
decl_stmt|;
comment|/**      * A private cache of the key value so it doesn't need to be loaded twice from the scanner.      */
specifier|private
name|Cell
name|value
init|=
literal|null
decl_stmt|;
specifier|private
name|long
name|count
decl_stmt|;
specifier|private
name|boolean
name|seeked
init|=
literal|false
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
name|FileSplit
name|fileSplit
init|=
operator|(
name|FileSplit
operator|)
name|split
decl_stmt|;
name|conf
operator|=
name|context
operator|.
name|getConfiguration
argument_list|()
expr_stmt|;
name|Path
name|path
init|=
name|fileSplit
operator|.
name|getPath
argument_list|()
decl_stmt|;
name|FileSystem
name|fs
init|=
name|path
operator|.
name|getFileSystem
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Initialize HFileRecordReader for {}"
argument_list|,
name|path
argument_list|)
expr_stmt|;
name|this
operator|.
name|in
operator|=
name|HFile
operator|.
name|createReader
argument_list|(
name|fs
argument_list|,
name|path
argument_list|,
name|conf
argument_list|)
expr_stmt|;
comment|// The file info must be loaded before the scanner can be used.
comment|// This seems like a bug in HBase, but it's easily worked around.
name|this
operator|.
name|in
operator|.
name|loadFileInfo
argument_list|()
expr_stmt|;
name|this
operator|.
name|scanner
operator|=
name|in
operator|.
name|getScanner
argument_list|(
literal|false
argument_list|,
literal|false
argument_list|)
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
name|boolean
name|hasNext
decl_stmt|;
if|if
condition|(
operator|!
name|seeked
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Seeking to start"
argument_list|)
expr_stmt|;
name|hasNext
operator|=
name|scanner
operator|.
name|seekTo
argument_list|()
expr_stmt|;
name|seeked
operator|=
literal|true
expr_stmt|;
block|}
else|else
block|{
name|hasNext
operator|=
name|scanner
operator|.
name|next
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|hasNext
condition|)
block|{
return|return
literal|false
return|;
block|}
name|value
operator|=
name|scanner
operator|.
name|getCell
argument_list|()
expr_stmt|;
name|count
operator|++
expr_stmt|;
return|return
literal|true
return|;
block|}
annotation|@
name|Override
specifier|public
name|NullWritable
name|getCurrentKey
parameter_list|()
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
return|return
name|NullWritable
operator|.
name|get
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|Cell
name|getCurrentValue
parameter_list|()
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
return|return
name|value
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
comment|// This would be inaccurate if KVs are not uniformly-sized or we have performed a seek to
comment|// the start row, but better than nothing anyway.
return|return
literal|1.0f
operator|*
name|count
operator|/
name|in
operator|.
name|getEntries
argument_list|()
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
if|if
condition|(
name|in
operator|!=
literal|null
condition|)
block|{
name|in
operator|.
name|close
argument_list|()
expr_stmt|;
name|in
operator|=
literal|null
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|Override
specifier|protected
name|List
argument_list|<
name|FileStatus
argument_list|>
name|listStatus
parameter_list|(
name|JobContext
name|job
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
comment|// Explode out directories that match the original FileInputFormat filters
comment|// since HFiles are written to directories where the
comment|// directory name is the column name
for|for
control|(
name|FileStatus
name|status
range|:
name|super
operator|.
name|listStatus
argument_list|(
name|job
argument_list|)
control|)
block|{
if|if
condition|(
name|status
operator|.
name|isDirectory
argument_list|()
condition|)
block|{
name|FileSystem
name|fs
init|=
name|status
operator|.
name|getPath
argument_list|()
operator|.
name|getFileSystem
argument_list|(
name|job
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|FileStatus
name|match
range|:
name|fs
operator|.
name|listStatus
argument_list|(
name|status
operator|.
name|getPath
argument_list|()
argument_list|,
name|HIDDEN_FILE_FILTER
argument_list|)
control|)
block|{
name|result
operator|.
name|add
argument_list|(
name|match
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|result
operator|.
name|add
argument_list|(
name|status
argument_list|)
expr_stmt|;
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
name|NullWritable
argument_list|,
name|Cell
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
name|HFileRecordReader
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|protected
name|boolean
name|isSplitable
parameter_list|(
name|JobContext
name|context
parameter_list|,
name|Path
name|filename
parameter_list|)
block|{
comment|// This file isn't splittable.
return|return
literal|false
return|;
block|}
block|}
end_class

end_unit
