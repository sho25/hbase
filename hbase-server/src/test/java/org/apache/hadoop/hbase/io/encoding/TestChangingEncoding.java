begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one or more  * contributor license agreements. See the NOTICE file distributed with this  * work for additional information regarding copyright ownership. The ASF  * licenses this file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the  * License for the specific language governing permissions and limitations  * under the License.  */
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
operator|.
name|encoding
package|;
end_package

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
name|CellUtil
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
name|HColumnDescriptor
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
name|HTableDescriptor
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
name|TableName
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
name|Admin
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
name|Connection
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
name|ConnectionFactory
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
name|Durability
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
name|Get
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
name|Put
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
name|Table
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
name|HRegionServer
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
name|testclassification
operator|.
name|IOTests
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
name|testclassification
operator|.
name|LargeTests
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
name|junit
operator|.
name|AfterClass
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|BeforeClass
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
name|junit
operator|.
name|experimental
operator|.
name|categories
operator|.
name|Category
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
name|Arrays
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
name|java
operator|.
name|util
operator|.
name|Random
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

begin_comment
comment|/**  * Tests changing data block encoding settings of a column family.  */
end_comment

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|IOTests
operator|.
name|class
block|,
name|LargeTests
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|TestChangingEncoding
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
name|TestChangingEncoding
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|static
specifier|final
name|String
name|CF
init|=
literal|"EncodingTestCF"
decl_stmt|;
specifier|static
specifier|final
name|byte
index|[]
name|CF_BYTES
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|CF
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|NUM_ROWS_PER_BATCH
init|=
literal|100
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|NUM_COLS_PER_ROW
init|=
literal|20
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|HBaseTestingUtility
name|TEST_UTIL
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|Configuration
name|conf
init|=
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|TIMEOUT_MS
init|=
literal|600000
decl_stmt|;
specifier|private
name|HColumnDescriptor
name|hcd
decl_stmt|;
specifier|private
name|TableName
name|tableName
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|List
argument_list|<
name|DataBlockEncoding
argument_list|>
name|ENCODINGS_TO_ITERATE
init|=
name|createEncodingsToIterate
argument_list|()
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|List
argument_list|<
name|DataBlockEncoding
argument_list|>
name|createEncodingsToIterate
parameter_list|()
block|{
name|List
argument_list|<
name|DataBlockEncoding
argument_list|>
name|encodings
init|=
operator|new
name|ArrayList
argument_list|<
name|DataBlockEncoding
argument_list|>
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|DataBlockEncoding
operator|.
name|values
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|encodings
operator|.
name|add
argument_list|(
name|DataBlockEncoding
operator|.
name|NONE
argument_list|)
expr_stmt|;
return|return
name|Collections
operator|.
name|unmodifiableList
argument_list|(
name|encodings
argument_list|)
return|;
block|}
comment|/** A zero-based index of the current batch of test data being written */
specifier|private
name|int
name|numBatchesWritten
decl_stmt|;
specifier|private
name|void
name|prepareTest
parameter_list|(
name|String
name|testId
parameter_list|)
throws|throws
name|IOException
block|{
name|tableName
operator|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"test_table_"
operator|+
name|testId
argument_list|)
expr_stmt|;
name|HTableDescriptor
name|htd
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
name|hcd
operator|=
operator|new
name|HColumnDescriptor
argument_list|(
name|CF
argument_list|)
expr_stmt|;
name|htd
operator|.
name|addFamily
argument_list|(
name|hcd
argument_list|)
expr_stmt|;
try|try
init|(
name|Admin
name|admin
init|=
name|TEST_UTIL
operator|.
name|getConnection
argument_list|()
operator|.
name|getAdmin
argument_list|()
init|)
block|{
name|admin
operator|.
name|createTable
argument_list|(
name|htd
argument_list|)
expr_stmt|;
block|}
name|numBatchesWritten
operator|=
literal|0
expr_stmt|;
block|}
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|setUpBeforeClass
parameter_list|()
throws|throws
name|Exception
block|{
comment|// Use a small flush size to create more HFiles.
name|conf
operator|.
name|setInt
argument_list|(
name|HConstants
operator|.
name|HREGION_MEMSTORE_FLUSH_SIZE
argument_list|,
literal|1024
operator|*
literal|1024
argument_list|)
expr_stmt|;
comment|// ((Log4JLogger)RpcServerImplementation.LOG).getLogger().setLevel(Level.TRACE);
comment|// ((Log4JLogger)RpcClient.LOG).getLogger().setLevel(Level.TRACE);
name|conf
operator|.
name|setBoolean
argument_list|(
literal|"hbase.online.schema.update.enable"
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|startMiniCluster
argument_list|()
expr_stmt|;
block|}
annotation|@
name|AfterClass
specifier|public
specifier|static
name|void
name|tearDownAfterClass
parameter_list|()
throws|throws
name|Exception
block|{
name|TEST_UTIL
operator|.
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
block|}
specifier|private
specifier|static
name|byte
index|[]
name|getRowKey
parameter_list|(
name|int
name|batchId
parameter_list|,
name|int
name|i
parameter_list|)
block|{
return|return
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"batch"
operator|+
name|batchId
operator|+
literal|"_row"
operator|+
name|i
argument_list|)
return|;
block|}
specifier|private
specifier|static
name|byte
index|[]
name|getQualifier
parameter_list|(
name|int
name|j
parameter_list|)
block|{
return|return
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"col"
operator|+
name|j
argument_list|)
return|;
block|}
specifier|private
specifier|static
name|byte
index|[]
name|getValue
parameter_list|(
name|int
name|batchId
parameter_list|,
name|int
name|i
parameter_list|,
name|int
name|j
parameter_list|)
block|{
return|return
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"value_for_"
operator|+
name|Bytes
operator|.
name|toString
argument_list|(
name|getRowKey
argument_list|(
name|batchId
argument_list|,
name|i
argument_list|)
argument_list|)
operator|+
literal|"_col"
operator|+
name|j
argument_list|)
return|;
block|}
specifier|static
name|void
name|writeTestDataBatch
parameter_list|(
name|TableName
name|tableName
parameter_list|,
name|int
name|batchId
parameter_list|)
throws|throws
name|Exception
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Writing test data batch "
operator|+
name|batchId
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|Put
argument_list|>
name|puts
init|=
operator|new
name|ArrayList
argument_list|<>
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
name|NUM_ROWS_PER_BATCH
condition|;
operator|++
name|i
control|)
block|{
name|Put
name|put
init|=
operator|new
name|Put
argument_list|(
name|getRowKey
argument_list|(
name|batchId
argument_list|,
name|i
argument_list|)
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
name|NUM_COLS_PER_ROW
condition|;
operator|++
name|j
control|)
block|{
name|put
operator|.
name|addColumn
argument_list|(
name|CF_BYTES
argument_list|,
name|getQualifier
argument_list|(
name|j
argument_list|)
argument_list|,
name|getValue
argument_list|(
name|batchId
argument_list|,
name|i
argument_list|,
name|j
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|put
operator|.
name|setDurability
argument_list|(
name|Durability
operator|.
name|SKIP_WAL
argument_list|)
expr_stmt|;
name|puts
operator|.
name|add
argument_list|(
name|put
argument_list|)
expr_stmt|;
block|}
try|try
init|(
name|Connection
name|conn
init|=
name|ConnectionFactory
operator|.
name|createConnection
argument_list|(
name|conf
argument_list|)
init|;
name|Table
name|table
operator|=
name|conn
operator|.
name|getTable
argument_list|(
name|tableName
argument_list|)
init|)
block|{
name|table
operator|.
name|put
argument_list|(
name|puts
argument_list|)
expr_stmt|;
block|}
block|}
specifier|static
name|void
name|verifyTestDataBatch
parameter_list|(
name|TableName
name|tableName
parameter_list|,
name|int
name|batchId
parameter_list|)
throws|throws
name|Exception
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Verifying test data batch "
operator|+
name|batchId
argument_list|)
expr_stmt|;
name|Table
name|table
init|=
name|TEST_UTIL
operator|.
name|getConnection
argument_list|()
operator|.
name|getTable
argument_list|(
name|tableName
argument_list|)
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
name|NUM_ROWS_PER_BATCH
condition|;
operator|++
name|i
control|)
block|{
name|Get
name|get
init|=
operator|new
name|Get
argument_list|(
name|getRowKey
argument_list|(
name|batchId
argument_list|,
name|i
argument_list|)
argument_list|)
decl_stmt|;
name|Result
name|result
init|=
name|table
operator|.
name|get
argument_list|(
name|get
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
name|NUM_COLS_PER_ROW
condition|;
operator|++
name|j
control|)
block|{
name|Cell
name|kv
init|=
name|result
operator|.
name|getColumnLatestCell
argument_list|(
name|CF_BYTES
argument_list|,
name|getQualifier
argument_list|(
name|j
argument_list|)
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|CellUtil
operator|.
name|matchingValue
argument_list|(
name|kv
argument_list|,
name|getValue
argument_list|(
name|batchId
argument_list|,
name|i
argument_list|,
name|j
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
name|table
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
specifier|private
name|void
name|writeSomeNewData
parameter_list|()
throws|throws
name|Exception
block|{
name|writeTestDataBatch
argument_list|(
name|tableName
argument_list|,
name|numBatchesWritten
argument_list|)
expr_stmt|;
operator|++
name|numBatchesWritten
expr_stmt|;
block|}
specifier|private
name|void
name|verifyAllData
parameter_list|()
throws|throws
name|Exception
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
name|numBatchesWritten
condition|;
operator|++
name|i
control|)
block|{
name|verifyTestDataBatch
argument_list|(
name|tableName
argument_list|,
name|i
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|setEncodingConf
parameter_list|(
name|DataBlockEncoding
name|encoding
parameter_list|,
name|boolean
name|onlineChange
parameter_list|)
throws|throws
name|Exception
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Setting CF encoding to "
operator|+
name|encoding
operator|+
literal|" (ordinal="
operator|+
name|encoding
operator|.
name|ordinal
argument_list|()
operator|+
literal|"), onlineChange="
operator|+
name|onlineChange
argument_list|)
expr_stmt|;
name|hcd
operator|.
name|setDataBlockEncoding
argument_list|(
name|encoding
argument_list|)
expr_stmt|;
try|try
init|(
name|Admin
name|admin
init|=
name|TEST_UTIL
operator|.
name|getConnection
argument_list|()
operator|.
name|getAdmin
argument_list|()
init|)
block|{
if|if
condition|(
operator|!
name|onlineChange
condition|)
block|{
name|admin
operator|.
name|disableTable
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
block|}
name|admin
operator|.
name|modifyColumnFamily
argument_list|(
name|tableName
argument_list|,
name|hcd
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|onlineChange
condition|)
block|{
name|admin
operator|.
name|enableTable
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
block|}
block|}
comment|// This is a unit test, not integration test. So let's
comment|// wait for regions out of transition. Otherwise, for online
comment|// encoding change, verification phase may be flaky because
comment|// regions could be still in transition.
name|TEST_UTIL
operator|.
name|waitUntilNoRegionsInTransition
argument_list|(
name|TIMEOUT_MS
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
name|TIMEOUT_MS
argument_list|)
specifier|public
name|void
name|testChangingEncoding
parameter_list|()
throws|throws
name|Exception
block|{
name|prepareTest
argument_list|(
literal|"ChangingEncoding"
argument_list|)
expr_stmt|;
for|for
control|(
name|boolean
name|onlineChange
range|:
operator|new
name|boolean
index|[]
block|{
literal|false
block|,
literal|true
block|}
control|)
block|{
for|for
control|(
name|DataBlockEncoding
name|encoding
range|:
name|ENCODINGS_TO_ITERATE
control|)
block|{
name|setEncodingConf
argument_list|(
name|encoding
argument_list|,
name|onlineChange
argument_list|)
expr_stmt|;
name|writeSomeNewData
argument_list|()
expr_stmt|;
name|verifyAllData
argument_list|()
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
name|TIMEOUT_MS
argument_list|)
specifier|public
name|void
name|testChangingEncodingWithCompaction
parameter_list|()
throws|throws
name|Exception
block|{
name|prepareTest
argument_list|(
literal|"ChangingEncodingWithCompaction"
argument_list|)
expr_stmt|;
for|for
control|(
name|boolean
name|onlineChange
range|:
operator|new
name|boolean
index|[]
block|{
literal|false
block|,
literal|true
block|}
control|)
block|{
for|for
control|(
name|DataBlockEncoding
name|encoding
range|:
name|ENCODINGS_TO_ITERATE
control|)
block|{
name|setEncodingConf
argument_list|(
name|encoding
argument_list|,
name|onlineChange
argument_list|)
expr_stmt|;
name|writeSomeNewData
argument_list|()
expr_stmt|;
name|verifyAllData
argument_list|()
expr_stmt|;
name|compactAndWait
argument_list|()
expr_stmt|;
name|verifyAllData
argument_list|()
expr_stmt|;
block|}
block|}
block|}
specifier|private
name|void
name|compactAndWait
parameter_list|()
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Compacting table "
operator|+
name|tableName
argument_list|)
expr_stmt|;
name|HRegionServer
name|rs
init|=
name|TEST_UTIL
operator|.
name|getMiniHBaseCluster
argument_list|()
operator|.
name|getRegionServer
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|HBaseAdmin
name|admin
init|=
name|TEST_UTIL
operator|.
name|getHBaseAdmin
argument_list|()
decl_stmt|;
name|admin
operator|.
name|majorCompact
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
comment|// Waiting for the compaction to start, at least .5s.
specifier|final
name|long
name|maxWaitime
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|+
literal|500
decl_stmt|;
name|boolean
name|cont
decl_stmt|;
do|do
block|{
name|cont
operator|=
name|rs
operator|.
name|compactSplitThread
operator|.
name|getCompactionQueueSize
argument_list|()
operator|==
literal|0
expr_stmt|;
name|Threads
operator|.
name|sleep
argument_list|(
literal|1
argument_list|)
expr_stmt|;
block|}
do|while
condition|(
name|cont
operator|&&
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|<
name|maxWaitime
condition|)
do|;
while|while
condition|(
name|rs
operator|.
name|compactSplitThread
operator|.
name|getCompactionQueueSize
argument_list|()
operator|>
literal|0
condition|)
block|{
name|Threads
operator|.
name|sleep
argument_list|(
literal|1
argument_list|)
expr_stmt|;
block|}
name|LOG
operator|.
name|debug
argument_list|(
literal|"Compaction queue size reached 0, continuing"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testCrazyRandomChanges
parameter_list|()
throws|throws
name|Exception
block|{
name|prepareTest
argument_list|(
literal|"RandomChanges"
argument_list|)
expr_stmt|;
name|Random
name|rand
init|=
operator|new
name|Random
argument_list|(
literal|2934298742974297L
argument_list|)
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
literal|20
condition|;
operator|++
name|i
control|)
block|{
name|int
name|encodingOrdinal
init|=
name|rand
operator|.
name|nextInt
argument_list|(
name|DataBlockEncoding
operator|.
name|values
argument_list|()
operator|.
name|length
argument_list|)
decl_stmt|;
name|DataBlockEncoding
name|encoding
init|=
name|DataBlockEncoding
operator|.
name|values
argument_list|()
index|[
name|encodingOrdinal
index|]
decl_stmt|;
name|setEncodingConf
argument_list|(
name|encoding
argument_list|,
name|rand
operator|.
name|nextBoolean
argument_list|()
argument_list|)
expr_stmt|;
name|writeSomeNewData
argument_list|()
expr_stmt|;
name|verifyAllData
argument_list|()
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

