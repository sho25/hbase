begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2009 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertEquals
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertFalse
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertNotSame
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertTrue
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
name|Arrays
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Random
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
name|HBaseConfiguration
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
name|HBaseTestingUtility
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
name|HConstants
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
name|KeyValue
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
name|PerformanceEvaluation
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
name|client
operator|.
name|HBaseAdmin
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
name|client
operator|.
name|HTable
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
name|client
operator|.
name|Result
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
name|client
operator|.
name|ResultScanner
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
name|client
operator|.
name|Scan
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
name|hbase
operator|.
name|util
operator|.
name|Threads
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
name|Job
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
name|Mapper
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
name|TaskAttemptID
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
name|junit
operator|.
name|Before
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Test
import|;
end_import

begin_import
import|import
name|org
operator|.
name|mockito
operator|.
name|Mockito
import|;
end_import

begin_comment
comment|/**  * Simple test for {@link KeyValueSortReducer} and {@link HFileOutputFormat}.  * Sets up and runs a mapreduce job that writes hfile output.  * Creates a few inner classes to implement splits and an inputformat that  * emits keys and values like those of {@link PerformanceEvaluation}.  Makes  * as many splits as "mapred.map.tasks" maps.  */
end_comment

begin_class
specifier|public
class|class
name|TestHFileOutputFormat
block|{
specifier|private
specifier|final
specifier|static
name|int
name|ROWSPERSPLIT
init|=
literal|1024
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
index|[]
name|FAMILIES
init|=
block|{
name|Bytes
operator|.
name|add
argument_list|(
name|PerformanceEvaluation
operator|.
name|FAMILY_NAME
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"-A"
argument_list|)
argument_list|)
block|,
name|Bytes
operator|.
name|add
argument_list|(
name|PerformanceEvaluation
operator|.
name|FAMILY_NAME
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"-B"
argument_list|)
argument_list|)
block|}
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|TABLE_NAME
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"TestTable"
argument_list|)
decl_stmt|;
specifier|private
name|HBaseTestingUtility
name|util
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
specifier|private
specifier|static
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|TestHFileOutputFormat
operator|.
name|class
argument_list|)
decl_stmt|;
comment|/**    * Simple mapper that makes KeyValue output.    */
specifier|static
class|class
name|RandomKVGeneratingMapper
extends|extends
name|Mapper
argument_list|<
name|NullWritable
argument_list|,
name|NullWritable
argument_list|,
name|ImmutableBytesWritable
argument_list|,
name|KeyValue
argument_list|>
block|{
specifier|private
name|int
name|keyLength
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|KEYLEN_DEFAULT
init|=
literal|10
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|KEYLEN_CONF
init|=
literal|"randomkv.key.length"
decl_stmt|;
specifier|private
name|int
name|valLength
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|VALLEN_DEFAULT
init|=
literal|10
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|VALLEN_CONF
init|=
literal|"randomkv.val.length"
decl_stmt|;
annotation|@
name|Override
specifier|protected
name|void
name|setup
parameter_list|(
name|Context
name|context
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|super
operator|.
name|setup
argument_list|(
name|context
argument_list|)
expr_stmt|;
name|Configuration
name|conf
init|=
name|context
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
name|keyLength
operator|=
name|conf
operator|.
name|getInt
argument_list|(
name|KEYLEN_CONF
argument_list|,
name|KEYLEN_DEFAULT
argument_list|)
expr_stmt|;
name|valLength
operator|=
name|conf
operator|.
name|getInt
argument_list|(
name|VALLEN_CONF
argument_list|,
name|VALLEN_DEFAULT
argument_list|)
expr_stmt|;
block|}
specifier|protected
name|void
name|map
parameter_list|(
name|NullWritable
name|n1
parameter_list|,
name|NullWritable
name|n2
parameter_list|,
name|Mapper
argument_list|<
name|NullWritable
argument_list|,
name|NullWritable
argument_list|,
name|ImmutableBytesWritable
argument_list|,
name|KeyValue
argument_list|>
operator|.
name|Context
name|context
parameter_list|)
throws|throws
name|java
operator|.
name|io
operator|.
name|IOException
throws|,
name|InterruptedException
block|{
name|byte
name|keyBytes
index|[]
init|=
operator|new
name|byte
index|[
name|keyLength
index|]
decl_stmt|;
name|byte
name|valBytes
index|[]
init|=
operator|new
name|byte
index|[
name|valLength
index|]
decl_stmt|;
name|int
name|taskId
init|=
name|context
operator|.
name|getTaskAttemptID
argument_list|()
operator|.
name|getTaskID
argument_list|()
operator|.
name|getId
argument_list|()
decl_stmt|;
assert|assert
name|taskId
operator|<
name|Byte
operator|.
name|MAX_VALUE
operator|:
literal|"Unit tests dont support> 127 tasks!"
assert|;
name|Random
name|random
init|=
operator|new
name|Random
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|ROWSPERSPLIT
condition|;
name|i
operator|++
control|)
block|{
name|random
operator|.
name|nextBytes
argument_list|(
name|keyBytes
argument_list|)
expr_stmt|;
comment|// Ensure that unique tasks generate unique keys
name|keyBytes
index|[
name|keyLength
operator|-
literal|1
index|]
operator|=
call|(
name|byte
call|)
argument_list|(
name|taskId
operator|&
literal|0xFF
argument_list|)
expr_stmt|;
name|random
operator|.
name|nextBytes
argument_list|(
name|valBytes
argument_list|)
expr_stmt|;
name|ImmutableBytesWritable
name|key
init|=
operator|new
name|ImmutableBytesWritable
argument_list|(
name|keyBytes
argument_list|)
decl_stmt|;
for|for
control|(
name|byte
index|[]
name|family
range|:
name|TestHFileOutputFormat
operator|.
name|FAMILIES
control|)
block|{
name|KeyValue
name|kv
init|=
operator|new
name|KeyValue
argument_list|(
name|keyBytes
argument_list|,
name|family
argument_list|,
name|PerformanceEvaluation
operator|.
name|QUALIFIER_NAME
argument_list|,
name|valBytes
argument_list|)
decl_stmt|;
name|context
operator|.
name|write
argument_list|(
name|key
argument_list|,
name|kv
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
annotation|@
name|Before
specifier|public
name|void
name|cleanupDir
parameter_list|()
throws|throws
name|IOException
block|{
name|util
operator|.
name|cleanupTestDir
argument_list|()
expr_stmt|;
block|}
specifier|private
name|void
name|setupRandomGeneratorMapper
parameter_list|(
name|Job
name|job
parameter_list|)
block|{
name|job
operator|.
name|setInputFormatClass
argument_list|(
name|NMapInputFormat
operator|.
name|class
argument_list|)
expr_stmt|;
name|job
operator|.
name|setMapperClass
argument_list|(
name|RandomKVGeneratingMapper
operator|.
name|class
argument_list|)
expr_stmt|;
name|job
operator|.
name|setMapOutputKeyClass
argument_list|(
name|ImmutableBytesWritable
operator|.
name|class
argument_list|)
expr_stmt|;
name|job
operator|.
name|setMapOutputValueClass
argument_list|(
name|KeyValue
operator|.
name|class
argument_list|)
expr_stmt|;
block|}
comment|/**    * Test that {@link HFileOutputFormat} RecordWriter amends timestamps if    * passed a keyvalue whose timestamp is {@link HConstants#LATEST_TIMESTAMP}.    * @see<a href="https://issues.apache.org/jira/browse/HBASE-2615">HBASE-2615</a>    */
annotation|@
name|Test
specifier|public
name|void
name|test_LATEST_TIMESTAMP_isReplaced
parameter_list|()
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|Configuration
name|conf
init|=
operator|new
name|Configuration
argument_list|(
name|this
operator|.
name|util
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
name|RecordWriter
argument_list|<
name|ImmutableBytesWritable
argument_list|,
name|KeyValue
argument_list|>
name|writer
init|=
literal|null
decl_stmt|;
name|TaskAttemptContext
name|context
init|=
literal|null
decl_stmt|;
name|Path
name|dir
init|=
name|HBaseTestingUtility
operator|.
name|getTestDir
argument_list|(
literal|"test_LATEST_TIMESTAMP_isReplaced"
argument_list|)
decl_stmt|;
try|try
block|{
name|Job
name|job
init|=
operator|new
name|Job
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|FileOutputFormat
operator|.
name|setOutputPath
argument_list|(
name|job
argument_list|,
name|dir
argument_list|)
expr_stmt|;
name|context
operator|=
operator|new
name|TaskAttemptContext
argument_list|(
name|job
operator|.
name|getConfiguration
argument_list|()
argument_list|,
operator|new
name|TaskAttemptID
argument_list|()
argument_list|)
expr_stmt|;
name|HFileOutputFormat
name|hof
init|=
operator|new
name|HFileOutputFormat
argument_list|()
decl_stmt|;
name|writer
operator|=
name|hof
operator|.
name|getRecordWriter
argument_list|(
name|context
argument_list|)
expr_stmt|;
specifier|final
name|byte
index|[]
name|b
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"b"
argument_list|)
decl_stmt|;
comment|// Test 1.  Pass a KV that has a ts of LATEST_TIMESTAMP.  It should be
comment|// changed by call to write.  Check all in kv is same but ts.
name|KeyValue
name|kv
init|=
operator|new
name|KeyValue
argument_list|(
name|b
argument_list|,
name|b
argument_list|,
name|b
argument_list|)
decl_stmt|;
name|KeyValue
name|original
init|=
name|kv
operator|.
name|clone
argument_list|()
decl_stmt|;
name|writer
operator|.
name|write
argument_list|(
operator|new
name|ImmutableBytesWritable
argument_list|()
argument_list|,
name|kv
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|original
operator|.
name|equals
argument_list|(
name|kv
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|Bytes
operator|.
name|equals
argument_list|(
name|original
operator|.
name|getRow
argument_list|()
argument_list|,
name|kv
operator|.
name|getRow
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|original
operator|.
name|matchingColumn
argument_list|(
name|kv
operator|.
name|getFamily
argument_list|()
argument_list|,
name|kv
operator|.
name|getQualifier
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertNotSame
argument_list|(
name|original
operator|.
name|getTimestamp
argument_list|()
argument_list|,
name|kv
operator|.
name|getTimestamp
argument_list|()
argument_list|)
expr_stmt|;
name|assertNotSame
argument_list|(
name|HConstants
operator|.
name|LATEST_TIMESTAMP
argument_list|,
name|kv
operator|.
name|getTimestamp
argument_list|()
argument_list|)
expr_stmt|;
comment|// Test 2. Now test passing a kv that has explicit ts.  It should not be
comment|// changed by call to record write.
name|kv
operator|=
operator|new
name|KeyValue
argument_list|(
name|b
argument_list|,
name|b
argument_list|,
name|b
argument_list|,
name|kv
operator|.
name|getTimestamp
argument_list|()
operator|-
literal|1
argument_list|,
name|b
argument_list|)
expr_stmt|;
name|original
operator|=
name|kv
operator|.
name|clone
argument_list|()
expr_stmt|;
name|writer
operator|.
name|write
argument_list|(
operator|new
name|ImmutableBytesWritable
argument_list|()
argument_list|,
name|kv
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|original
operator|.
name|equals
argument_list|(
name|kv
argument_list|)
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
if|if
condition|(
name|writer
operator|!=
literal|null
operator|&&
name|context
operator|!=
literal|null
condition|)
name|writer
operator|.
name|close
argument_list|(
name|context
argument_list|)
expr_stmt|;
name|dir
operator|.
name|getFileSystem
argument_list|(
name|conf
argument_list|)
operator|.
name|delete
argument_list|(
name|dir
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Run small MR job.    */
annotation|@
name|Test
specifier|public
name|void
name|testWritingPEData
parameter_list|()
throws|throws
name|Exception
block|{
name|Configuration
name|conf
init|=
name|util
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
name|Path
name|testDir
init|=
name|HBaseTestingUtility
operator|.
name|getTestDir
argument_list|(
literal|"testWritingPEData"
argument_list|)
decl_stmt|;
name|FileSystem
name|fs
init|=
name|testDir
operator|.
name|getFileSystem
argument_list|(
name|conf
argument_list|)
decl_stmt|;
comment|// Set down this value or we OOME in eclipse.
name|conf
operator|.
name|setInt
argument_list|(
literal|"io.sort.mb"
argument_list|,
literal|20
argument_list|)
expr_stmt|;
comment|// Write a few files.
name|conf
operator|.
name|setLong
argument_list|(
literal|"hbase.hregion.max.filesize"
argument_list|,
literal|64
operator|*
literal|1024
argument_list|)
expr_stmt|;
name|Job
name|job
init|=
operator|new
name|Job
argument_list|(
name|conf
argument_list|,
literal|"testWritingPEData"
argument_list|)
decl_stmt|;
name|setupRandomGeneratorMapper
argument_list|(
name|job
argument_list|)
expr_stmt|;
comment|// This partitioner doesn't work well for number keys but using it anyways
comment|// just to demonstrate how to configure it.
name|byte
index|[]
name|startKey
init|=
operator|new
name|byte
index|[
name|RandomKVGeneratingMapper
operator|.
name|KEYLEN_DEFAULT
index|]
decl_stmt|;
name|byte
index|[]
name|endKey
init|=
operator|new
name|byte
index|[
name|RandomKVGeneratingMapper
operator|.
name|KEYLEN_DEFAULT
index|]
decl_stmt|;
name|Arrays
operator|.
name|fill
argument_list|(
name|startKey
argument_list|,
operator|(
name|byte
operator|)
literal|0
argument_list|)
expr_stmt|;
name|Arrays
operator|.
name|fill
argument_list|(
name|endKey
argument_list|,
operator|(
name|byte
operator|)
literal|0xff
argument_list|)
expr_stmt|;
name|job
operator|.
name|setPartitionerClass
argument_list|(
name|SimpleTotalOrderPartitioner
operator|.
name|class
argument_list|)
expr_stmt|;
comment|// Set start and end rows for partitioner.
name|SimpleTotalOrderPartitioner
operator|.
name|setStartKey
argument_list|(
name|job
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|startKey
argument_list|)
expr_stmt|;
name|SimpleTotalOrderPartitioner
operator|.
name|setEndKey
argument_list|(
name|job
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|endKey
argument_list|)
expr_stmt|;
name|job
operator|.
name|setReducerClass
argument_list|(
name|KeyValueSortReducer
operator|.
name|class
argument_list|)
expr_stmt|;
name|job
operator|.
name|setOutputFormatClass
argument_list|(
name|HFileOutputFormat
operator|.
name|class
argument_list|)
expr_stmt|;
name|job
operator|.
name|setNumReduceTasks
argument_list|(
literal|4
argument_list|)
expr_stmt|;
name|FileOutputFormat
operator|.
name|setOutputPath
argument_list|(
name|job
argument_list|,
name|testDir
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|job
operator|.
name|waitForCompletion
argument_list|(
literal|false
argument_list|)
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
name|testDir
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|files
operator|.
name|length
operator|>
literal|0
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testJobConfiguration
parameter_list|()
throws|throws
name|Exception
block|{
name|Job
name|job
init|=
operator|new
name|Job
argument_list|()
decl_stmt|;
name|HTable
name|table
init|=
name|Mockito
operator|.
name|mock
argument_list|(
name|HTable
operator|.
name|class
argument_list|)
decl_stmt|;
name|byte
index|[]
index|[]
name|mockKeys
init|=
operator|new
name|byte
index|[]
index|[]
block|{
name|HConstants
operator|.
name|EMPTY_BYTE_ARRAY
block|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"aaa"
argument_list|)
block|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"ggg"
argument_list|)
block|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"zzz"
argument_list|)
block|}
decl_stmt|;
name|Mockito
operator|.
name|doReturn
argument_list|(
name|mockKeys
argument_list|)
operator|.
name|when
argument_list|(
name|table
argument_list|)
operator|.
name|getStartKeys
argument_list|()
expr_stmt|;
name|HFileOutputFormat
operator|.
name|configureIncrementalLoad
argument_list|(
name|job
argument_list|,
name|table
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|job
operator|.
name|getNumReduceTasks
argument_list|()
argument_list|,
literal|4
argument_list|)
expr_stmt|;
block|}
specifier|private
name|byte
index|[]
index|[]
name|generateRandomStartKeys
parameter_list|(
name|int
name|numKeys
parameter_list|)
block|{
name|Random
name|random
init|=
operator|new
name|Random
argument_list|()
decl_stmt|;
name|byte
index|[]
index|[]
name|ret
init|=
operator|new
name|byte
index|[
name|numKeys
index|]
index|[]
decl_stmt|;
comment|// first region start key is always empty
name|ret
index|[
literal|0
index|]
operator|=
name|HConstants
operator|.
name|EMPTY_BYTE_ARRAY
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|1
init|;
name|i
operator|<
name|numKeys
condition|;
name|i
operator|++
control|)
block|{
name|ret
index|[
name|i
index|]
operator|=
name|PerformanceEvaluation
operator|.
name|generateValue
argument_list|(
name|random
argument_list|)
expr_stmt|;
block|}
return|return
name|ret
return|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testMRIncrementalLoad
parameter_list|()
throws|throws
name|Exception
block|{
name|doIncrementalLoadTest
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testMRIncrementalLoadWithSplit
parameter_list|()
throws|throws
name|Exception
block|{
name|doIncrementalLoadTest
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|doIncrementalLoadTest
parameter_list|(
name|boolean
name|shouldChangeRegions
parameter_list|)
throws|throws
name|Exception
block|{
name|Configuration
name|conf
init|=
name|util
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
name|Path
name|testDir
init|=
name|HBaseTestingUtility
operator|.
name|getTestDir
argument_list|(
literal|"testLocalMRIncrementalLoad"
argument_list|)
decl_stmt|;
name|byte
index|[]
index|[]
name|startKeys
init|=
name|generateRandomStartKeys
argument_list|(
literal|5
argument_list|)
decl_stmt|;
try|try
block|{
name|util
operator|.
name|startMiniCluster
argument_list|()
expr_stmt|;
name|HBaseAdmin
name|admin
init|=
operator|new
name|HBaseAdmin
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|HTable
name|table
init|=
name|util
operator|.
name|createTable
argument_list|(
name|TABLE_NAME
argument_list|,
name|FAMILIES
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"Should start with empty table"
argument_list|,
literal|0
argument_list|,
name|util
operator|.
name|countRows
argument_list|(
name|table
argument_list|)
argument_list|)
expr_stmt|;
name|int
name|numRegions
init|=
name|util
operator|.
name|createMultiRegions
argument_list|(
name|util
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|table
argument_list|,
name|FAMILIES
index|[
literal|0
index|]
argument_list|,
name|startKeys
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"Should make 5 regions"
argument_list|,
name|numRegions
argument_list|,
literal|5
argument_list|)
expr_stmt|;
comment|// Generate the bulk load files
name|util
operator|.
name|startMiniMapReduceCluster
argument_list|()
expr_stmt|;
name|runIncrementalPELoad
argument_list|(
name|conf
argument_list|,
name|table
argument_list|,
name|testDir
argument_list|)
expr_stmt|;
comment|// This doesn't write into the table, just makes files
name|assertEquals
argument_list|(
literal|"HFOF should not touch actual table"
argument_list|,
literal|0
argument_list|,
name|util
operator|.
name|countRows
argument_list|(
name|table
argument_list|)
argument_list|)
expr_stmt|;
comment|// Make sure that a directory was created for every CF
name|int
name|dir
init|=
literal|0
decl_stmt|;
for|for
control|(
name|FileStatus
name|f
range|:
name|testDir
operator|.
name|getFileSystem
argument_list|(
name|conf
argument_list|)
operator|.
name|listStatus
argument_list|(
name|testDir
argument_list|)
control|)
block|{
for|for
control|(
name|byte
index|[]
name|family
range|:
name|FAMILIES
control|)
block|{
if|if
condition|(
name|Bytes
operator|.
name|toString
argument_list|(
name|family
argument_list|)
operator|.
name|equals
argument_list|(
name|f
operator|.
name|getPath
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
condition|)
block|{
operator|++
name|dir
expr_stmt|;
block|}
block|}
block|}
name|assertEquals
argument_list|(
literal|"Column family not found in FS."
argument_list|,
name|FAMILIES
operator|.
name|length
argument_list|,
name|dir
argument_list|)
expr_stmt|;
comment|// handle the split case
if|if
condition|(
name|shouldChangeRegions
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Changing regions in table"
argument_list|)
expr_stmt|;
name|admin
operator|.
name|disableTable
argument_list|(
name|table
operator|.
name|getTableName
argument_list|()
argument_list|)
expr_stmt|;
while|while
condition|(
name|util
operator|.
name|getMiniHBaseCluster
argument_list|()
operator|.
name|getMaster
argument_list|()
operator|.
name|getAssignmentManager
argument_list|()
operator|.
name|isRegionsInTransition
argument_list|()
condition|)
block|{
name|Threads
operator|.
name|sleep
argument_list|(
literal|1000
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Waiting on table to finish disabling"
argument_list|)
expr_stmt|;
block|}
name|byte
index|[]
index|[]
name|newStartKeys
init|=
name|generateRandomStartKeys
argument_list|(
literal|15
argument_list|)
decl_stmt|;
name|util
operator|.
name|createMultiRegions
argument_list|(
name|util
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|table
argument_list|,
name|FAMILIES
index|[
literal|0
index|]
argument_list|,
name|newStartKeys
argument_list|)
expr_stmt|;
name|admin
operator|.
name|enableTable
argument_list|(
name|table
operator|.
name|getTableName
argument_list|()
argument_list|)
expr_stmt|;
while|while
condition|(
name|table
operator|.
name|getRegionsInfo
argument_list|()
operator|.
name|size
argument_list|()
operator|!=
literal|15
operator|||
operator|!
name|admin
operator|.
name|isTableAvailable
argument_list|(
name|table
operator|.
name|getTableName
argument_list|()
argument_list|)
condition|)
block|{
name|Thread
operator|.
name|sleep
argument_list|(
literal|1000
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Waiting for new region assignment to happen"
argument_list|)
expr_stmt|;
block|}
block|}
comment|// Perform the actual load
operator|new
name|LoadIncrementalHFiles
argument_list|(
name|conf
argument_list|)
operator|.
name|doBulkLoad
argument_list|(
name|testDir
argument_list|,
name|table
argument_list|)
expr_stmt|;
comment|// Ensure data shows up
name|int
name|expectedRows
init|=
name|conf
operator|.
name|getInt
argument_list|(
literal|"mapred.map.tasks"
argument_list|,
literal|1
argument_list|)
operator|*
name|ROWSPERSPLIT
decl_stmt|;
name|assertEquals
argument_list|(
literal|"LoadIncrementalHFiles should put expected data in table"
argument_list|,
name|expectedRows
argument_list|,
name|util
operator|.
name|countRows
argument_list|(
name|table
argument_list|)
argument_list|)
expr_stmt|;
name|Scan
name|scan
init|=
operator|new
name|Scan
argument_list|()
decl_stmt|;
name|ResultScanner
name|results
init|=
name|table
operator|.
name|getScanner
argument_list|(
name|scan
argument_list|)
decl_stmt|;
name|int
name|count
init|=
literal|0
decl_stmt|;
for|for
control|(
name|Result
name|res
range|:
name|results
control|)
block|{
name|count
operator|++
expr_stmt|;
name|assertEquals
argument_list|(
name|FAMILIES
operator|.
name|length
argument_list|,
name|res
operator|.
name|raw
argument_list|()
operator|.
name|length
argument_list|)
expr_stmt|;
name|KeyValue
name|first
init|=
name|res
operator|.
name|raw
argument_list|()
index|[
literal|0
index|]
decl_stmt|;
for|for
control|(
name|KeyValue
name|kv
range|:
name|res
operator|.
name|raw
argument_list|()
control|)
block|{
name|assertTrue
argument_list|(
name|KeyValue
operator|.
name|COMPARATOR
operator|.
name|matchingRows
argument_list|(
name|first
argument_list|,
name|kv
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|Bytes
operator|.
name|equals
argument_list|(
name|first
operator|.
name|getValue
argument_list|()
argument_list|,
name|kv
operator|.
name|getValue
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
name|results
operator|.
name|close
argument_list|()
expr_stmt|;
name|String
name|tableDigestBefore
init|=
name|util
operator|.
name|checksumRows
argument_list|(
name|table
argument_list|)
decl_stmt|;
comment|// Cause regions to reopen
name|admin
operator|.
name|disableTable
argument_list|(
name|TABLE_NAME
argument_list|)
expr_stmt|;
while|while
condition|(
operator|!
name|admin
operator|.
name|isTableDisabled
argument_list|(
name|TABLE_NAME
argument_list|)
condition|)
block|{
name|Thread
operator|.
name|sleep
argument_list|(
literal|1000
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Waiting for table to disable"
argument_list|)
expr_stmt|;
block|}
name|admin
operator|.
name|enableTable
argument_list|(
name|TABLE_NAME
argument_list|)
expr_stmt|;
name|util
operator|.
name|waitTableAvailable
argument_list|(
name|TABLE_NAME
argument_list|,
literal|30000
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"Data should remain after reopening of regions"
argument_list|,
name|tableDigestBefore
argument_list|,
name|util
operator|.
name|checksumRows
argument_list|(
name|table
argument_list|)
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|util
operator|.
name|shutdownMiniMapReduceCluster
argument_list|()
expr_stmt|;
name|util
operator|.
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|runIncrementalPELoad
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|HTable
name|table
parameter_list|,
name|Path
name|outDir
parameter_list|)
throws|throws
name|Exception
block|{
name|Job
name|job
init|=
operator|new
name|Job
argument_list|(
name|conf
argument_list|,
literal|"testLocalMRIncrementalLoad"
argument_list|)
decl_stmt|;
name|setupRandomGeneratorMapper
argument_list|(
name|job
argument_list|)
expr_stmt|;
name|HFileOutputFormat
operator|.
name|configureIncrementalLoad
argument_list|(
name|job
argument_list|,
name|table
argument_list|)
expr_stmt|;
name|FileOutputFormat
operator|.
name|setOutputPath
argument_list|(
name|job
argument_list|,
name|outDir
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|table
operator|.
name|getRegionsInfo
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
name|job
operator|.
name|getNumReduceTasks
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|job
operator|.
name|waitForCompletion
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
name|void
name|main
parameter_list|(
name|String
name|args
index|[]
parameter_list|)
throws|throws
name|Exception
block|{
operator|new
name|TestHFileOutputFormat
argument_list|()
operator|.
name|manualTest
argument_list|(
name|args
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|manualTest
parameter_list|(
name|String
name|args
index|[]
parameter_list|)
throws|throws
name|Exception
block|{
name|Configuration
name|conf
init|=
name|HBaseConfiguration
operator|.
name|create
argument_list|()
decl_stmt|;
name|util
operator|=
operator|new
name|HBaseTestingUtility
argument_list|(
name|conf
argument_list|)
expr_stmt|;
if|if
condition|(
literal|"newtable"
operator|.
name|equals
argument_list|(
name|args
index|[
literal|0
index|]
argument_list|)
condition|)
block|{
name|byte
index|[]
name|tname
init|=
name|args
index|[
literal|1
index|]
operator|.
name|getBytes
argument_list|()
decl_stmt|;
name|HTable
name|table
init|=
name|util
operator|.
name|createTable
argument_list|(
name|tname
argument_list|,
name|FAMILIES
argument_list|)
decl_stmt|;
name|HBaseAdmin
name|admin
init|=
operator|new
name|HBaseAdmin
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|admin
operator|.
name|disableTable
argument_list|(
name|tname
argument_list|)
expr_stmt|;
name|byte
index|[]
index|[]
name|startKeys
init|=
name|generateRandomStartKeys
argument_list|(
literal|5
argument_list|)
decl_stmt|;
name|util
operator|.
name|createMultiRegions
argument_list|(
name|conf
argument_list|,
name|table
argument_list|,
name|FAMILIES
index|[
literal|0
index|]
argument_list|,
name|startKeys
argument_list|)
expr_stmt|;
name|admin
operator|.
name|enableTable
argument_list|(
name|tname
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"incremental"
operator|.
name|equals
argument_list|(
name|args
index|[
literal|0
index|]
argument_list|)
condition|)
block|{
name|byte
index|[]
name|tname
init|=
name|args
index|[
literal|1
index|]
operator|.
name|getBytes
argument_list|()
decl_stmt|;
name|HTable
name|table
init|=
operator|new
name|HTable
argument_list|(
name|conf
argument_list|,
name|tname
argument_list|)
decl_stmt|;
name|Path
name|outDir
init|=
operator|new
name|Path
argument_list|(
literal|"incremental-out"
argument_list|)
decl_stmt|;
name|runIncrementalPELoad
argument_list|(
name|conf
argument_list|,
name|table
argument_list|,
name|outDir
argument_list|)
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"usage: TestHFileOutputFormat newtable | incremental"
argument_list|)
throw|;
block|}
block|}
block|}
end_class

end_unit

