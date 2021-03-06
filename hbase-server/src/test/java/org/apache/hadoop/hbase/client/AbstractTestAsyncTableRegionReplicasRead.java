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
name|client
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
name|Arrays
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
name|Optional
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|ConcurrentHashMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|ConcurrentMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|ForkJoinPool
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|atomic
operator|.
name|AtomicInteger
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|function
operator|.
name|Supplier
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
name|coprocessor
operator|.
name|ObserverContext
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
name|coprocessor
operator|.
name|RegionCoprocessor
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
name|coprocessor
operator|.
name|RegionCoprocessorEnvironment
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
name|coprocessor
operator|.
name|RegionObserver
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
name|HRegion
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
name|JVMClusterUtil
operator|.
name|RegionServerThread
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
name|Rule
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
name|rules
operator|.
name|TestName
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
name|Parameter
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

begin_class
specifier|public
specifier|abstract
class|class
name|AbstractTestAsyncTableRegionReplicasRead
block|{
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
specifier|static
name|TableName
name|TABLE_NAME
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"async"
argument_list|)
decl_stmt|;
specifier|protected
specifier|static
name|byte
index|[]
name|FAMILY
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"cf"
argument_list|)
decl_stmt|;
specifier|protected
specifier|static
name|byte
index|[]
name|QUALIFIER
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"cq"
argument_list|)
decl_stmt|;
specifier|protected
specifier|static
name|byte
index|[]
name|ROW
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"row"
argument_list|)
decl_stmt|;
specifier|protected
specifier|static
name|byte
index|[]
name|VALUE
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"value"
argument_list|)
decl_stmt|;
specifier|protected
specifier|static
name|int
name|REPLICA_COUNT
init|=
literal|3
decl_stmt|;
specifier|protected
specifier|static
name|AsyncConnection
name|ASYNC_CONN
decl_stmt|;
annotation|@
name|Rule
specifier|public
name|TestName
name|testName
init|=
operator|new
name|TestName
argument_list|()
decl_stmt|;
annotation|@
name|Parameter
specifier|public
name|Supplier
argument_list|<
name|AsyncTable
argument_list|<
name|?
argument_list|>
argument_list|>
name|getTable
decl_stmt|;
specifier|private
specifier|static
name|AsyncTable
argument_list|<
name|?
argument_list|>
name|getRawTable
parameter_list|()
block|{
return|return
name|ASYNC_CONN
operator|.
name|getTable
argument_list|(
name|TABLE_NAME
argument_list|)
return|;
block|}
specifier|private
specifier|static
name|AsyncTable
argument_list|<
name|?
argument_list|>
name|getTable
parameter_list|()
block|{
return|return
name|ASYNC_CONN
operator|.
name|getTable
argument_list|(
name|TABLE_NAME
argument_list|,
name|ForkJoinPool
operator|.
name|commonPool
argument_list|()
argument_list|)
return|;
block|}
annotation|@
name|Parameters
specifier|public
specifier|static
name|List
argument_list|<
name|Object
index|[]
argument_list|>
name|params
parameter_list|()
block|{
return|return
name|Arrays
operator|.
name|asList
argument_list|(
operator|new
name|Supplier
argument_list|<
name|?
argument_list|>
index|[]
block|{
name|AbstractTestAsyncTableRegionReplicasRead
operator|::
name|getRawTable
block|}
operator|,
operator|new
name|Supplier
argument_list|<
name|?
argument_list|>
index|[]
block|{
name|AbstractTestAsyncTableRegionReplicasRead
operator|::
name|getTable
block|}
block|)
function|;
block|}
end_class

begin_decl_stmt
specifier|protected
specifier|static
specifier|volatile
name|boolean
name|FAIL_PRIMARY_GET
init|=
literal|false
decl_stmt|;
end_decl_stmt

begin_decl_stmt
specifier|protected
specifier|static
name|ConcurrentMap
argument_list|<
name|Integer
argument_list|,
name|AtomicInteger
argument_list|>
name|REPLICA_ID_TO_COUNT
init|=
operator|new
name|ConcurrentHashMap
argument_list|<>
argument_list|()
decl_stmt|;
end_decl_stmt

begin_class
specifier|public
specifier|static
specifier|final
class|class
name|FailPrimaryGetCP
implements|implements
name|RegionObserver
implements|,
name|RegionCoprocessor
block|{
annotation|@
name|Override
specifier|public
name|Optional
argument_list|<
name|RegionObserver
argument_list|>
name|getRegionObserver
parameter_list|()
block|{
return|return
name|Optional
operator|.
name|of
argument_list|(
name|this
argument_list|)
return|;
block|}
specifier|private
name|void
name|recordAndTryFail
parameter_list|(
name|ObserverContext
argument_list|<
name|RegionCoprocessorEnvironment
argument_list|>
name|c
parameter_list|)
throws|throws
name|IOException
block|{
name|RegionInfo
name|region
init|=
name|c
operator|.
name|getEnvironment
argument_list|()
operator|.
name|getRegionInfo
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|region
operator|.
name|getTable
argument_list|()
operator|.
name|equals
argument_list|(
name|TABLE_NAME
argument_list|)
condition|)
block|{
return|return;
block|}
name|REPLICA_ID_TO_COUNT
operator|.
name|computeIfAbsent
argument_list|(
name|region
operator|.
name|getReplicaId
argument_list|()
argument_list|,
name|k
lambda|->
operator|new
name|AtomicInteger
argument_list|()
argument_list|)
operator|.
name|incrementAndGet
argument_list|()
expr_stmt|;
if|if
condition|(
name|region
operator|.
name|getReplicaId
argument_list|()
operator|==
name|RegionReplicaUtil
operator|.
name|DEFAULT_REPLICA_ID
operator|&&
name|FAIL_PRIMARY_GET
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Inject error"
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|preGetOp
parameter_list|(
name|ObserverContext
argument_list|<
name|RegionCoprocessorEnvironment
argument_list|>
name|c
parameter_list|,
name|Get
name|get
parameter_list|,
name|List
argument_list|<
name|Cell
argument_list|>
name|result
parameter_list|)
throws|throws
name|IOException
block|{
name|recordAndTryFail
argument_list|(
name|c
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|preScannerOpen
parameter_list|(
name|ObserverContext
argument_list|<
name|RegionCoprocessorEnvironment
argument_list|>
name|c
parameter_list|,
name|Scan
name|scan
parameter_list|)
throws|throws
name|IOException
block|{
name|recordAndTryFail
argument_list|(
name|c
argument_list|)
expr_stmt|;
block|}
block|}
end_class

begin_function
specifier|private
specifier|static
name|boolean
name|allReplicasHaveRow
parameter_list|(
name|byte
index|[]
name|row
parameter_list|)
throws|throws
name|IOException
block|{
for|for
control|(
name|RegionServerThread
name|t
range|:
name|TEST_UTIL
operator|.
name|getMiniHBaseCluster
argument_list|()
operator|.
name|getRegionServerThreads
argument_list|()
control|)
block|{
for|for
control|(
name|HRegion
name|region
range|:
name|t
operator|.
name|getRegionServer
argument_list|()
operator|.
name|getRegions
argument_list|(
name|TABLE_NAME
argument_list|)
control|)
block|{
if|if
condition|(
name|region
operator|.
name|get
argument_list|(
operator|new
name|Get
argument_list|(
name|row
argument_list|)
argument_list|,
literal|false
argument_list|)
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
return|return
literal|false
return|;
block|}
block|}
block|}
return|return
literal|true
return|;
block|}
end_function

begin_function
specifier|protected
specifier|static
name|void
name|startClusterAndCreateTable
parameter_list|()
throws|throws
name|Exception
block|{
name|TEST_UTIL
operator|.
name|startMiniCluster
argument_list|(
literal|3
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|getAdmin
argument_list|()
operator|.
name|createTable
argument_list|(
name|TableDescriptorBuilder
operator|.
name|newBuilder
argument_list|(
name|TABLE_NAME
argument_list|)
operator|.
name|setColumnFamily
argument_list|(
name|ColumnFamilyDescriptorBuilder
operator|.
name|of
argument_list|(
name|FAMILY
argument_list|)
argument_list|)
operator|.
name|setRegionReplication
argument_list|(
name|REPLICA_COUNT
argument_list|)
operator|.
name|setCoprocessor
argument_list|(
name|FailPrimaryGetCP
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|waitUntilAllRegionsAssigned
argument_list|(
name|TABLE_NAME
argument_list|)
expr_stmt|;
name|ASYNC_CONN
operator|=
name|ConnectionFactory
operator|.
name|createAsyncConnection
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
block|}
end_function

begin_function
specifier|protected
specifier|static
name|void
name|waitUntilAllReplicasHaveRow
parameter_list|(
name|byte
index|[]
name|row
parameter_list|)
throws|throws
name|IOException
block|{
comment|// this is the fastest way to let all replicas have the row
name|TEST_UTIL
operator|.
name|getAdmin
argument_list|()
operator|.
name|disableTable
argument_list|(
name|TABLE_NAME
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|getAdmin
argument_list|()
operator|.
name|enableTable
argument_list|(
name|TABLE_NAME
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|waitFor
argument_list|(
literal|30000
argument_list|,
parameter_list|()
lambda|->
name|allReplicasHaveRow
argument_list|(
name|row
argument_list|)
argument_list|)
expr_stmt|;
block|}
end_function

begin_function
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
name|IOUtils
operator|.
name|closeQuietly
argument_list|(
name|ASYNC_CONN
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
block|}
end_function

begin_function
specifier|protected
specifier|static
name|int
name|getSecondaryGetCount
parameter_list|()
block|{
return|return
name|REPLICA_ID_TO_COUNT
operator|.
name|entrySet
argument_list|()
operator|.
name|stream
argument_list|()
operator|.
name|filter
argument_list|(
name|e
lambda|->
name|e
operator|.
name|getKey
argument_list|()
operator|.
name|intValue
argument_list|()
operator|!=
name|RegionReplicaUtil
operator|.
name|DEFAULT_REPLICA_ID
argument_list|)
operator|.
name|mapToInt
argument_list|(
name|e
lambda|->
name|e
operator|.
name|getValue
argument_list|()
operator|.
name|get
argument_list|()
argument_list|)
operator|.
name|sum
argument_list|()
return|;
block|}
end_function

begin_function
specifier|protected
specifier|static
name|int
name|getPrimaryGetCount
parameter_list|()
block|{
name|AtomicInteger
name|primaryGetCount
init|=
name|REPLICA_ID_TO_COUNT
operator|.
name|get
argument_list|(
name|RegionReplicaUtil
operator|.
name|DEFAULT_REPLICA_ID
argument_list|)
decl_stmt|;
return|return
name|primaryGetCount
operator|!=
literal|null
condition|?
name|primaryGetCount
operator|.
name|get
argument_list|()
else|:
literal|0
return|;
block|}
end_function

begin_comment
comment|// replicaId = -1 means do not set replica
end_comment

begin_function_decl
specifier|protected
specifier|abstract
name|void
name|readAndCheck
parameter_list|(
name|AsyncTable
argument_list|<
name|?
argument_list|>
name|table
parameter_list|,
name|int
name|replicaId
parameter_list|)
throws|throws
name|Exception
function_decl|;
end_function_decl

begin_function
annotation|@
name|Test
specifier|public
name|void
name|testNoReplicaRead
parameter_list|()
throws|throws
name|Exception
block|{
name|FAIL_PRIMARY_GET
operator|=
literal|false
expr_stmt|;
name|REPLICA_ID_TO_COUNT
operator|.
name|clear
argument_list|()
expr_stmt|;
name|AsyncTable
argument_list|<
name|?
argument_list|>
name|table
init|=
name|getTable
operator|.
name|get
argument_list|()
decl_stmt|;
name|readAndCheck
argument_list|(
name|table
argument_list|,
operator|-
literal|1
argument_list|)
expr_stmt|;
comment|// the primary region is fine and the primary timeout is 1 second which is long enough, so we
comment|// should not send any requests to secondary replicas even if the consistency is timeline.
name|Thread
operator|.
name|sleep
argument_list|(
literal|5000
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|getSecondaryGetCount
argument_list|()
argument_list|)
expr_stmt|;
block|}
end_function

begin_function
annotation|@
name|Test
specifier|public
name|void
name|testReplicaRead
parameter_list|()
throws|throws
name|Exception
block|{
comment|// fail the primary get request
name|FAIL_PRIMARY_GET
operator|=
literal|true
expr_stmt|;
name|REPLICA_ID_TO_COUNT
operator|.
name|clear
argument_list|()
expr_stmt|;
comment|// make sure that we could still get the value from secondary replicas
name|AsyncTable
argument_list|<
name|?
argument_list|>
name|table
init|=
name|getTable
operator|.
name|get
argument_list|()
decl_stmt|;
name|readAndCheck
argument_list|(
name|table
argument_list|,
operator|-
literal|1
argument_list|)
expr_stmt|;
comment|// make sure that the primary request has been canceled
name|Thread
operator|.
name|sleep
argument_list|(
literal|5000
argument_list|)
expr_stmt|;
name|int
name|count
init|=
name|getPrimaryGetCount
argument_list|()
decl_stmt|;
name|Thread
operator|.
name|sleep
argument_list|(
literal|10000
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|count
argument_list|,
name|getPrimaryGetCount
argument_list|()
argument_list|)
expr_stmt|;
block|}
end_function

begin_function
annotation|@
name|Test
specifier|public
name|void
name|testReadSpecificReplica
parameter_list|()
throws|throws
name|Exception
block|{
name|FAIL_PRIMARY_GET
operator|=
literal|false
expr_stmt|;
name|REPLICA_ID_TO_COUNT
operator|.
name|clear
argument_list|()
expr_stmt|;
name|AsyncTable
argument_list|<
name|?
argument_list|>
name|table
init|=
name|getTable
operator|.
name|get
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|replicaId
init|=
literal|0
init|;
name|replicaId
operator|<
name|REPLICA_COUNT
condition|;
name|replicaId
operator|++
control|)
block|{
name|readAndCheck
argument_list|(
name|table
argument_list|,
name|replicaId
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|REPLICA_ID_TO_COUNT
operator|.
name|get
argument_list|(
name|replicaId
argument_list|)
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_function

unit|}
end_unit

