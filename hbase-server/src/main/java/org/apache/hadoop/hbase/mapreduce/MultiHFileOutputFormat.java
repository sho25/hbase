begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one or more contributor license  * agreements. See the NOTICE file distributed with this work for additional information regarding  * copyright ownership. The ASF licenses this file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance with the License. You may obtain a  * copy of the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable  * law or agreed to in writing, software distributed under the License is distributed on an "AS IS"  * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License  * for the specific language governing permissions and limitations under the License.  */
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
name|HashMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
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
name|hbase
operator|.
name|classification
operator|.
name|InterfaceStability
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
name|ImmutableBytesWritable
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
name|Bytes
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
name|RecordWriter
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
name|output
operator|.
name|FileOutputCommitter
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
name|output
operator|.
name|FileOutputFormat
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
name|mapreduce
operator|.
name|HFileOutputFormat2
import|;
end_import

begin_import
import|import
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
comment|/**  * Create 3 level tree directory, first level is using table name as parent directory and then use  * family name as child directory, and all related HFiles for one family are under child directory  * -tableName1  *   -columnFamilyName1  *   -columnFamilyName2  *     -HFiles  * -tableName2  *   -columnFamilyName1  *     -HFiles  *   -columnFamilyName2  *<p>  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Public
annotation|@
name|InterfaceStability
operator|.
name|Evolving
annotation|@
name|VisibleForTesting
specifier|public
class|class
name|MultiHFileOutputFormat
extends|extends
name|FileOutputFormat
argument_list|<
name|ImmutableBytesWritable
argument_list|,
name|Cell
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
name|MultiHFileOutputFormat
operator|.
name|class
argument_list|)
decl_stmt|;
annotation|@
name|Override
specifier|public
name|RecordWriter
argument_list|<
name|ImmutableBytesWritable
argument_list|,
name|Cell
argument_list|>
name|getRecordWriter
parameter_list|(
specifier|final
name|TaskAttemptContext
name|context
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
return|return
name|createMultiHFileRecordWriter
argument_list|(
name|context
argument_list|)
return|;
block|}
specifier|static
parameter_list|<
name|V
extends|extends
name|Cell
parameter_list|>
name|RecordWriter
argument_list|<
name|ImmutableBytesWritable
argument_list|,
name|V
argument_list|>
name|createMultiHFileRecordWriter
parameter_list|(
specifier|final
name|TaskAttemptContext
name|context
parameter_list|)
throws|throws
name|IOException
block|{
comment|// Get the path of the output directory
specifier|final
name|Path
name|outputPath
init|=
name|FileOutputFormat
operator|.
name|getOutputPath
argument_list|(
name|context
argument_list|)
decl_stmt|;
specifier|final
name|Path
name|outputDir
init|=
operator|new
name|FileOutputCommitter
argument_list|(
name|outputPath
argument_list|,
name|context
argument_list|)
operator|.
name|getWorkPath
argument_list|()
decl_stmt|;
specifier|final
name|Configuration
name|conf
init|=
name|context
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
specifier|final
name|FileSystem
name|fs
init|=
name|outputDir
operator|.
name|getFileSystem
argument_list|(
name|conf
argument_list|)
decl_stmt|;
comment|// Map of tables to writers
specifier|final
name|Map
argument_list|<
name|ImmutableBytesWritable
argument_list|,
name|RecordWriter
argument_list|<
name|ImmutableBytesWritable
argument_list|,
name|V
argument_list|>
argument_list|>
name|tableWriters
init|=
operator|new
name|HashMap
argument_list|<
name|ImmutableBytesWritable
argument_list|,
name|RecordWriter
argument_list|<
name|ImmutableBytesWritable
argument_list|,
name|V
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
return|return
operator|new
name|RecordWriter
argument_list|<
name|ImmutableBytesWritable
argument_list|,
name|V
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|write
parameter_list|(
name|ImmutableBytesWritable
name|tableName
parameter_list|,
name|V
name|cell
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|RecordWriter
argument_list|<
name|ImmutableBytesWritable
argument_list|,
name|V
argument_list|>
name|tableWriter
init|=
name|tableWriters
operator|.
name|get
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
comment|// if there is new table, verify that table directory exists
if|if
condition|(
name|tableWriter
operator|==
literal|null
condition|)
block|{
comment|// using table name as directory name
specifier|final
name|Path
name|tableOutputDir
init|=
operator|new
name|Path
argument_list|(
name|outputDir
argument_list|,
name|Bytes
operator|.
name|toString
argument_list|(
name|tableName
operator|.
name|copyBytes
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|fs
operator|.
name|mkdirs
argument_list|(
name|tableOutputDir
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Writing Table '"
operator|+
name|tableName
operator|.
name|toString
argument_list|()
operator|+
literal|"' data into following directory"
operator|+
name|tableOutputDir
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
comment|// Create writer for one specific table
name|tableWriter
operator|=
operator|new
name|HFileOutputFormat2
operator|.
name|HFileRecordWriter
argument_list|<
name|V
argument_list|>
argument_list|(
name|context
argument_list|,
name|tableOutputDir
argument_list|)
expr_stmt|;
comment|// Put table into map
name|tableWriters
operator|.
name|put
argument_list|(
name|tableName
argument_list|,
name|tableWriter
argument_list|)
expr_stmt|;
block|}
comment|// Write<Row, Cell> into tableWriter
comment|// in the original code, it does not use Row
name|tableWriter
operator|.
name|write
argument_list|(
literal|null
argument_list|,
name|cell
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|(
name|TaskAttemptContext
name|c
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
for|for
control|(
name|RecordWriter
argument_list|<
name|ImmutableBytesWritable
argument_list|,
name|V
argument_list|>
name|writer
range|:
name|tableWriters
operator|.
name|values
argument_list|()
control|)
block|{
name|writer
operator|.
name|close
argument_list|(
name|c
argument_list|)
expr_stmt|;
block|}
block|}
block|}
return|;
block|}
block|}
end_class

end_unit

