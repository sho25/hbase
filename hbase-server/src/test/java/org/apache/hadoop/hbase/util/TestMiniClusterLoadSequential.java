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
name|util
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
name|Collection
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|EnumSet
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
name|TableNotFoundException
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
name|ClusterStatus
operator|.
name|Option
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
name|io
operator|.
name|compress
operator|.
name|Compression
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
name|encoding
operator|.
name|DataBlockEncoding
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
name|testclassification
operator|.
name|MiscTests
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
name|test
operator|.
name|LoadTestDataGenerator
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|After
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
name|org
operator|.
name|junit
operator|.
name|runner
operator|.
name|RunWith
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|runners
operator|.
name|Parameterized
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|runners
operator|.
name|Parameterized
operator|.
name|Parameters
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
comment|/**  * A write/read/verify load test on a mini HBase cluster. Tests reading  * and then writing.  */
end_comment

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|MiscTests
operator|.
name|class
block|,
name|LargeTests
operator|.
name|class
block|}
argument_list|)
annotation|@
name|RunWith
argument_list|(
name|Parameterized
operator|.
name|class
argument_list|)
specifier|public
class|class
name|TestMiniClusterLoadSequential
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
name|TestMiniClusterLoadSequential
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|protected
specifier|static
specifier|final
name|TableName
name|TABLE
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"load_test_tbl"
argument_list|)
decl_stmt|;
specifier|protected
specifier|static
specifier|final
name|byte
index|[]
name|CF
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"load_test_cf"
argument_list|)
decl_stmt|;
specifier|protected
specifier|static
specifier|final
name|int
name|NUM_THREADS
init|=
literal|8
decl_stmt|;
specifier|protected
specifier|static
specifier|final
name|int
name|NUM_RS
init|=
literal|2
decl_stmt|;
specifier|protected
specifier|static
specifier|final
name|int
name|TIMEOUT_MS
init|=
literal|180000
decl_stmt|;
specifier|protected
specifier|static
specifier|final
name|HBaseTestingUtility
name|TEST_UTIL
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
specifier|protected
specifier|final
name|Configuration
name|conf
init|=
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
specifier|protected
specifier|final
name|boolean
name|isMultiPut
decl_stmt|;
specifier|protected
specifier|final
name|DataBlockEncoding
name|dataBlockEncoding
decl_stmt|;
specifier|protected
name|MultiThreadedWriter
name|writerThreads
decl_stmt|;
specifier|protected
name|MultiThreadedReader
name|readerThreads
decl_stmt|;
specifier|protected
name|int
name|numKeys
decl_stmt|;
specifier|protected
name|Compression
operator|.
name|Algorithm
name|compression
init|=
name|Compression
operator|.
name|Algorithm
operator|.
name|NONE
decl_stmt|;
specifier|public
name|TestMiniClusterLoadSequential
parameter_list|(
name|boolean
name|isMultiPut
parameter_list|,
name|DataBlockEncoding
name|dataBlockEncoding
parameter_list|)
block|{
name|this
operator|.
name|isMultiPut
operator|=
name|isMultiPut
expr_stmt|;
name|this
operator|.
name|dataBlockEncoding
operator|=
name|dataBlockEncoding
expr_stmt|;
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
comment|// We don't want any region reassignments by the load balancer during the test.
name|conf
operator|.
name|setFloat
argument_list|(
name|HConstants
operator|.
name|LOAD_BALANCER_SLOP_KEY
argument_list|,
literal|10.0f
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Parameters
specifier|public
specifier|static
name|Collection
argument_list|<
name|Object
index|[]
argument_list|>
name|parameters
parameter_list|()
block|{
name|List
argument_list|<
name|Object
index|[]
argument_list|>
name|parameters
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|boolean
name|multiPut
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
name|dataBlockEncoding
range|:
operator|new
name|DataBlockEncoding
index|[]
block|{
name|DataBlockEncoding
operator|.
name|NONE
block|,
name|DataBlockEncoding
operator|.
name|PREFIX
block|}
control|)
block|{
name|parameters
operator|.
name|add
argument_list|(
operator|new
name|Object
index|[]
block|{
name|multiPut
block|,
name|dataBlockEncoding
block|}
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|parameters
return|;
block|}
annotation|@
name|Before
specifier|public
name|void
name|setUp
parameter_list|()
throws|throws
name|Exception
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Test setup: isMultiPut="
operator|+
name|isMultiPut
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|startMiniCluster
argument_list|(
literal|1
argument_list|,
name|NUM_RS
argument_list|)
expr_stmt|;
block|}
annotation|@
name|After
specifier|public
name|void
name|tearDown
parameter_list|()
throws|throws
name|Exception
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Test teardown: isMultiPut="
operator|+
name|isMultiPut
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
block|}
specifier|protected
name|MultiThreadedReader
name|prepareReaderThreads
parameter_list|(
name|LoadTestDataGenerator
name|dataGen
parameter_list|,
name|Configuration
name|conf
parameter_list|,
name|TableName
name|tableName
parameter_list|,
name|double
name|verifyPercent
parameter_list|)
throws|throws
name|IOException
block|{
name|MultiThreadedReader
name|reader
init|=
operator|new
name|MultiThreadedReader
argument_list|(
name|dataGen
argument_list|,
name|conf
argument_list|,
name|tableName
argument_list|,
name|verifyPercent
argument_list|)
decl_stmt|;
return|return
name|reader
return|;
block|}
specifier|protected
name|MultiThreadedWriter
name|prepareWriterThreads
parameter_list|(
name|LoadTestDataGenerator
name|dataGen
parameter_list|,
name|Configuration
name|conf
parameter_list|,
name|TableName
name|tableName
parameter_list|)
throws|throws
name|IOException
block|{
name|MultiThreadedWriter
name|writer
init|=
operator|new
name|MultiThreadedWriter
argument_list|(
name|dataGen
argument_list|,
name|conf
argument_list|,
name|tableName
argument_list|)
decl_stmt|;
name|writer
operator|.
name|setMultiPut
argument_list|(
name|isMultiPut
argument_list|)
expr_stmt|;
return|return
name|writer
return|;
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
name|loadTest
parameter_list|()
throws|throws
name|Exception
block|{
name|prepareForLoadTest
argument_list|()
expr_stmt|;
name|runLoadTestOnExistingTable
argument_list|()
expr_stmt|;
block|}
specifier|protected
name|void
name|runLoadTestOnExistingTable
parameter_list|()
throws|throws
name|IOException
block|{
name|writerThreads
operator|.
name|start
argument_list|(
literal|0
argument_list|,
name|numKeys
argument_list|,
name|NUM_THREADS
argument_list|)
expr_stmt|;
name|writerThreads
operator|.
name|waitForFinish
argument_list|()
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|writerThreads
operator|.
name|getNumWriteFailures
argument_list|()
argument_list|)
expr_stmt|;
name|readerThreads
operator|.
name|start
argument_list|(
literal|0
argument_list|,
name|numKeys
argument_list|,
name|NUM_THREADS
argument_list|)
expr_stmt|;
name|readerThreads
operator|.
name|waitForFinish
argument_list|()
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|readerThreads
operator|.
name|getNumReadFailures
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|readerThreads
operator|.
name|getNumReadErrors
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|numKeys
argument_list|,
name|readerThreads
operator|.
name|getNumKeysVerified
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|protected
name|void
name|createPreSplitLoadTestTable
parameter_list|(
name|HTableDescriptor
name|htd
parameter_list|,
name|HColumnDescriptor
name|hcd
parameter_list|)
throws|throws
name|IOException
block|{
name|HBaseTestingUtility
operator|.
name|createPreSplitLoadTestTable
argument_list|(
name|conf
argument_list|,
name|htd
argument_list|,
name|hcd
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|waitUntilAllRegionsAssigned
argument_list|(
name|htd
operator|.
name|getTableName
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|protected
name|void
name|prepareForLoadTest
parameter_list|()
throws|throws
name|IOException
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Starting load test: dataBlockEncoding="
operator|+
name|dataBlockEncoding
operator|+
literal|", isMultiPut="
operator|+
name|isMultiPut
argument_list|)
expr_stmt|;
name|numKeys
operator|=
name|numKeys
argument_list|()
expr_stmt|;
name|Admin
name|admin
init|=
name|TEST_UTIL
operator|.
name|getAdmin
argument_list|()
decl_stmt|;
while|while
condition|(
name|admin
operator|.
name|getClusterStatus
argument_list|(
name|EnumSet
operator|.
name|of
argument_list|(
name|Option
operator|.
name|LIVE_SERVERS
argument_list|)
argument_list|)
operator|.
name|getServers
argument_list|()
operator|.
name|size
argument_list|()
operator|<
name|NUM_RS
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Sleeping until "
operator|+
name|NUM_RS
operator|+
literal|" RSs are online"
argument_list|)
expr_stmt|;
name|Threads
operator|.
name|sleepWithoutInterrupt
argument_list|(
literal|1000
argument_list|)
expr_stmt|;
block|}
name|admin
operator|.
name|close
argument_list|()
expr_stmt|;
name|HTableDescriptor
name|htd
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|TABLE
argument_list|)
decl_stmt|;
name|HColumnDescriptor
name|hcd
init|=
operator|new
name|HColumnDescriptor
argument_list|(
name|CF
argument_list|)
operator|.
name|setCompressionType
argument_list|(
name|compression
argument_list|)
operator|.
name|setDataBlockEncoding
argument_list|(
name|dataBlockEncoding
argument_list|)
decl_stmt|;
name|createPreSplitLoadTestTable
argument_list|(
name|htd
argument_list|,
name|hcd
argument_list|)
expr_stmt|;
name|LoadTestDataGenerator
name|dataGen
init|=
operator|new
name|MultiThreadedAction
operator|.
name|DefaultDataGenerator
argument_list|(
name|CF
argument_list|)
decl_stmt|;
name|writerThreads
operator|=
name|prepareWriterThreads
argument_list|(
name|dataGen
argument_list|,
name|conf
argument_list|,
name|TABLE
argument_list|)
expr_stmt|;
name|readerThreads
operator|=
name|prepareReaderThreads
argument_list|(
name|dataGen
argument_list|,
name|conf
argument_list|,
name|TABLE
argument_list|,
literal|100
argument_list|)
expr_stmt|;
block|}
specifier|protected
name|int
name|numKeys
parameter_list|()
block|{
return|return
literal|1000
return|;
block|}
specifier|protected
name|HColumnDescriptor
name|getColumnDesc
parameter_list|(
name|Admin
name|admin
parameter_list|)
throws|throws
name|TableNotFoundException
throws|,
name|IOException
block|{
return|return
name|admin
operator|.
name|getTableDescriptor
argument_list|(
name|TABLE
argument_list|)
operator|.
name|getFamily
argument_list|(
name|CF
argument_list|)
return|;
block|}
block|}
end_class

end_unit

