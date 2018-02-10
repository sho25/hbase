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
name|assertNotNull
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
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|fail
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
name|List
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
name|CompletableFuture
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
name|ExecutionException
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
name|DoNotRetryIOException
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
name|HBaseClassTestRule
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
name|filter
operator|.
name|BinaryComparator
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
name|ipc
operator|.
name|HBaseRpcController
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
name|ClientTests
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
name|MediumTests
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
name|AfterClass
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
name|BeforeClass
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|ClassRule
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
name|mockito
operator|.
name|Mockito
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
name|protobuf
operator|.
name|ProtobufUtil
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
name|protobuf
operator|.
name|RequestConverter
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
name|protobuf
operator|.
name|generated
operator|.
name|ClientProtos
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
name|protobuf
operator|.
name|generated
operator|.
name|HBaseProtos
import|;
end_import

begin_comment
comment|/**  * The purpose of this test is to ensure whether rs deals with the malformed cells correctly.  */
end_comment

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|MediumTests
operator|.
name|class
block|,
name|ClientTests
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|TestMalformedCellFromClient
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
name|TestMalformedCellFromClient
operator|.
name|class
argument_list|)
decl_stmt|;
annotation|@
name|ClassRule
specifier|public
specifier|static
specifier|final
name|HBaseClassTestRule
name|CLASS_RULE
init|=
name|HBaseClassTestRule
operator|.
name|forClass
argument_list|(
name|TestMalformedCellFromClient
operator|.
name|class
argument_list|)
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
name|byte
index|[]
name|FAMILY
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"testFamily"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|CELL_SIZE
init|=
literal|100
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|TableName
name|TABLE_NAME
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"TestMalformedCellFromClient"
argument_list|)
decl_stmt|;
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
comment|// disable the retry
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setInt
argument_list|(
name|HConstants
operator|.
name|HBASE_CLIENT_RETRIES_NUMBER
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|startMiniCluster
argument_list|(
literal|1
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Before
specifier|public
name|void
name|before
parameter_list|()
throws|throws
name|Exception
block|{
name|TableDescriptor
name|desc
init|=
name|TableDescriptorBuilder
operator|.
name|newBuilder
argument_list|(
name|TABLE_NAME
argument_list|)
operator|.
name|addColumnFamily
argument_list|(
name|ColumnFamilyDescriptorBuilder
operator|.
name|of
argument_list|(
name|FAMILY
argument_list|)
argument_list|)
operator|.
name|setValue
argument_list|(
name|HRegion
operator|.
name|HBASE_MAX_CELL_SIZE_KEY
argument_list|,
name|String
operator|.
name|valueOf
argument_list|(
name|CELL_SIZE
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|TEST_UTIL
operator|.
name|getConnection
argument_list|()
operator|.
name|getAdmin
argument_list|()
operator|.
name|createTable
argument_list|(
name|desc
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
for|for
control|(
name|TableDescriptor
name|htd
range|:
name|TEST_UTIL
operator|.
name|getAdmin
argument_list|()
operator|.
name|listTableDescriptors
argument_list|()
control|)
block|{
name|TEST_UTIL
operator|.
name|deleteTable
argument_list|(
name|htd
operator|.
name|getTableName
argument_list|()
argument_list|)
expr_stmt|;
block|}
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
comment|/**    * The purpose of this ut is to check the consistency between the exception and results.    * If the RetriesExhaustedWithDetailsException contains the whole batch,    * each result should be of IOE. Otherwise, the row operation which is not in the exception    * should have a true result.    */
annotation|@
name|Test
specifier|public
name|void
name|testRegionException
parameter_list|()
throws|throws
name|InterruptedException
throws|,
name|IOException
block|{
name|List
argument_list|<
name|Row
argument_list|>
name|batches
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|batches
operator|.
name|add
argument_list|(
operator|new
name|Put
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"good"
argument_list|)
argument_list|)
operator|.
name|addColumn
argument_list|(
name|FAMILY
argument_list|,
literal|null
argument_list|,
operator|new
name|byte
index|[
literal|10
index|]
argument_list|)
argument_list|)
expr_stmt|;
comment|// the rm is used to prompt the region exception.
comment|// see RSRpcServices#multi
name|RowMutations
name|rm
init|=
operator|new
name|RowMutations
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"fail"
argument_list|)
argument_list|)
decl_stmt|;
name|rm
operator|.
name|add
argument_list|(
operator|new
name|Put
argument_list|(
name|rm
operator|.
name|getRow
argument_list|()
argument_list|)
operator|.
name|addColumn
argument_list|(
name|FAMILY
argument_list|,
literal|null
argument_list|,
operator|new
name|byte
index|[
name|CELL_SIZE
index|]
argument_list|)
argument_list|)
expr_stmt|;
name|batches
operator|.
name|add
argument_list|(
name|rm
argument_list|)
expr_stmt|;
name|Object
index|[]
name|results
init|=
operator|new
name|Object
index|[
name|batches
operator|.
name|size
argument_list|()
index|]
decl_stmt|;
try|try
init|(
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
name|TABLE_NAME
argument_list|)
init|)
block|{
name|Throwable
name|exceptionByCaught
init|=
literal|null
decl_stmt|;
try|try
block|{
name|table
operator|.
name|batch
argument_list|(
name|batches
argument_list|,
name|results
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"Where is the exception? We put the malformed cells!!!"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|RetriesExhaustedWithDetailsException
name|e
parameter_list|)
block|{
for|for
control|(
name|Throwable
name|throwable
range|:
name|e
operator|.
name|getCauses
argument_list|()
control|)
block|{
name|assertNotNull
argument_list|(
name|throwable
argument_list|)
expr_stmt|;
block|}
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|e
operator|.
name|getNumExceptions
argument_list|()
argument_list|)
expr_stmt|;
name|exceptionByCaught
operator|=
name|e
operator|.
name|getCause
argument_list|(
literal|0
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|Object
name|obj
range|:
name|results
control|)
block|{
name|assertNotNull
argument_list|(
name|obj
argument_list|)
expr_stmt|;
block|}
name|assertEquals
argument_list|(
name|Result
operator|.
name|class
argument_list|,
name|results
index|[
literal|0
index|]
operator|.
name|getClass
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|exceptionByCaught
operator|.
name|getClass
argument_list|()
argument_list|,
name|results
index|[
literal|1
index|]
operator|.
name|getClass
argument_list|()
argument_list|)
expr_stmt|;
name|Result
name|result
init|=
name|table
operator|.
name|get
argument_list|(
operator|new
name|Get
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"good"
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|result
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|Cell
name|cell
init|=
name|result
operator|.
name|getColumnLatestCell
argument_list|(
name|FAMILY
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|Bytes
operator|.
name|equals
argument_list|(
name|CellUtil
operator|.
name|cloneValue
argument_list|(
name|cell
argument_list|)
argument_list|,
operator|new
name|byte
index|[
literal|10
index|]
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * This test verifies region exception doesn't corrupt the results of batch. The prescription is    * shown below. 1) honor the action result rather than region exception. If the action have both    * of true result and region exception, the action is fine as the exception is caused by other    * actions which are in the same region. 2) honor the action exception rather than region    * exception. If the action have both of action exception and region exception, we deal with the    * action exception only. If we also handle the region exception for the same action, it will    * introduce the negative count of actions in progress. The AsyncRequestFuture#waitUntilDone will    * block forever. If the RetriesExhaustedWithDetailsException contains the whole batch, each    * result should be of IOE. Otherwise, the row operation which is not in the exception should have    * a true result. The no-cluster test is in TestAsyncProcessWithRegionException.    */
annotation|@
name|Test
specifier|public
name|void
name|testRegionExceptionByAsync
parameter_list|()
throws|throws
name|Exception
block|{
name|List
argument_list|<
name|Row
argument_list|>
name|batches
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|batches
operator|.
name|add
argument_list|(
operator|new
name|Put
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"good"
argument_list|)
argument_list|)
operator|.
name|addColumn
argument_list|(
name|FAMILY
argument_list|,
literal|null
argument_list|,
operator|new
name|byte
index|[
literal|10
index|]
argument_list|)
argument_list|)
expr_stmt|;
comment|// the rm is used to prompt the region exception.
comment|// see RSRpcServices#multi
name|RowMutations
name|rm
init|=
operator|new
name|RowMutations
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"fail"
argument_list|)
argument_list|)
decl_stmt|;
name|rm
operator|.
name|add
argument_list|(
operator|new
name|Put
argument_list|(
name|rm
operator|.
name|getRow
argument_list|()
argument_list|)
operator|.
name|addColumn
argument_list|(
name|FAMILY
argument_list|,
literal|null
argument_list|,
operator|new
name|byte
index|[
name|CELL_SIZE
index|]
argument_list|)
argument_list|)
expr_stmt|;
name|batches
operator|.
name|add
argument_list|(
name|rm
argument_list|)
expr_stmt|;
try|try
init|(
name|AsyncConnection
name|asyncConnection
init|=
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
init|)
block|{
name|AsyncTable
argument_list|<
name|AdvancedScanResultConsumer
argument_list|>
name|table
init|=
name|asyncConnection
operator|.
name|getTable
argument_list|(
name|TABLE_NAME
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|CompletableFuture
argument_list|<
name|AdvancedScanResultConsumer
argument_list|>
argument_list|>
name|results
init|=
name|table
operator|.
name|batch
argument_list|(
name|batches
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|results
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
try|try
block|{
name|results
operator|.
name|get
argument_list|(
literal|1
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|fail
argument_list|(
literal|"Where is the exception? We put the malformed cells!!!"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ExecutionException
name|e
parameter_list|)
block|{
comment|// pass
block|}
name|Result
name|result
init|=
name|table
operator|.
name|get
argument_list|(
operator|new
name|Get
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"good"
argument_list|)
argument_list|)
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|result
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|Cell
name|cell
init|=
name|result
operator|.
name|getColumnLatestCell
argument_list|(
name|FAMILY
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|Bytes
operator|.
name|equals
argument_list|(
name|CellUtil
operator|.
name|cloneValue
argument_list|(
name|cell
argument_list|)
argument_list|,
operator|new
name|byte
index|[
literal|10
index|]
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * The invalid cells is in rm. The rm should fail but the subsequent mutations should succeed.    * Currently, we have no client api to submit the request consisting of condition-rm and mutation.    * Hence, this test build the request manually.    */
annotation|@
name|Test
specifier|public
name|void
name|testAtomicOperations
parameter_list|()
throws|throws
name|Exception
block|{
name|RowMutations
name|rm
init|=
operator|new
name|RowMutations
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"fail"
argument_list|)
argument_list|)
decl_stmt|;
name|rm
operator|.
name|add
argument_list|(
operator|new
name|Put
argument_list|(
name|rm
operator|.
name|getRow
argument_list|()
argument_list|)
operator|.
name|addColumn
argument_list|(
name|FAMILY
argument_list|,
literal|null
argument_list|,
operator|new
name|byte
index|[
name|CELL_SIZE
index|]
argument_list|)
argument_list|)
expr_stmt|;
name|rm
operator|.
name|add
argument_list|(
operator|new
name|Put
argument_list|(
name|rm
operator|.
name|getRow
argument_list|()
argument_list|)
operator|.
name|addColumn
argument_list|(
name|FAMILY
argument_list|,
literal|null
argument_list|,
operator|new
name|byte
index|[
literal|10
index|]
argument_list|)
argument_list|)
expr_stmt|;
name|Put
name|put
init|=
operator|new
name|Put
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"good"
argument_list|)
argument_list|)
operator|.
name|addColumn
argument_list|(
name|FAMILY
argument_list|,
literal|null
argument_list|,
operator|new
name|byte
index|[
literal|10
index|]
argument_list|)
decl_stmt|;
comment|// build the request
name|HRegion
name|r
init|=
name|TEST_UTIL
operator|.
name|getMiniHBaseCluster
argument_list|()
operator|.
name|getRegions
argument_list|(
name|TABLE_NAME
argument_list|)
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|ClientProtos
operator|.
name|MultiRequest
name|request
init|=
name|ClientProtos
operator|.
name|MultiRequest
operator|.
name|newBuilder
argument_list|(
name|createRequest
argument_list|(
name|rm
argument_list|,
name|r
operator|.
name|getRegionInfo
argument_list|()
operator|.
name|getRegionName
argument_list|()
argument_list|)
argument_list|)
operator|.
name|addRegionAction
argument_list|(
name|ClientProtos
operator|.
name|RegionAction
operator|.
name|newBuilder
argument_list|()
operator|.
name|setRegion
argument_list|(
name|RequestConverter
operator|.
name|buildRegionSpecifier
argument_list|(
name|HBaseProtos
operator|.
name|RegionSpecifier
operator|.
name|RegionSpecifierType
operator|.
name|REGION_NAME
argument_list|,
name|r
operator|.
name|getRegionInfo
argument_list|()
operator|.
name|getRegionName
argument_list|()
argument_list|)
argument_list|)
operator|.
name|addAction
argument_list|(
name|ClientProtos
operator|.
name|Action
operator|.
name|newBuilder
argument_list|()
operator|.
name|setMutation
argument_list|(
name|ProtobufUtil
operator|.
name|toMutationNoData
argument_list|(
name|ClientProtos
operator|.
name|MutationProto
operator|.
name|MutationType
operator|.
name|PUT
argument_list|,
name|put
argument_list|)
argument_list|)
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|Cell
argument_list|>
name|cells
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|Mutation
name|m
range|:
name|rm
operator|.
name|getMutations
argument_list|()
control|)
block|{
name|cells
operator|.
name|addAll
argument_list|(
name|m
operator|.
name|getCellList
argument_list|(
name|FAMILY
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|cells
operator|.
name|addAll
argument_list|(
name|put
operator|.
name|getCellList
argument_list|(
name|FAMILY
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|3
argument_list|,
name|cells
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|HBaseRpcController
name|controller
init|=
name|Mockito
operator|.
name|mock
argument_list|(
name|HBaseRpcController
operator|.
name|class
argument_list|)
decl_stmt|;
name|Mockito
operator|.
name|when
argument_list|(
name|controller
operator|.
name|cellScanner
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|CellUtil
operator|.
name|createCellScanner
argument_list|(
name|cells
argument_list|)
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
name|TEST_UTIL
operator|.
name|getMiniHBaseCluster
argument_list|()
operator|.
name|getServerHoldingRegion
argument_list|(
name|TABLE_NAME
argument_list|,
name|r
operator|.
name|getRegionInfo
argument_list|()
operator|.
name|getRegionName
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|ClientProtos
operator|.
name|MultiResponse
name|response
init|=
name|rs
operator|.
name|getRSRpcServices
argument_list|()
operator|.
name|multi
argument_list|(
name|controller
argument_list|,
name|request
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|response
operator|.
name|getRegionActionResultCount
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|response
operator|.
name|getRegionActionResultList
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|hasException
argument_list|()
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|response
operator|.
name|getRegionActionResultList
argument_list|()
operator|.
name|get
argument_list|(
literal|1
argument_list|)
operator|.
name|hasException
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|response
operator|.
name|getRegionActionResultList
argument_list|()
operator|.
name|get
argument_list|(
literal|1
argument_list|)
operator|.
name|getResultOrExceptionCount
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|response
operator|.
name|getRegionActionResultList
argument_list|()
operator|.
name|get
argument_list|(
literal|1
argument_list|)
operator|.
name|getResultOrExceptionList
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|hasResult
argument_list|()
argument_list|)
expr_stmt|;
try|try
init|(
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
name|TABLE_NAME
argument_list|)
init|)
block|{
name|Result
name|result
init|=
name|table
operator|.
name|get
argument_list|(
operator|new
name|Get
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"good"
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|result
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|Cell
name|cell
init|=
name|result
operator|.
name|getColumnLatestCell
argument_list|(
name|FAMILY
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|Bytes
operator|.
name|equals
argument_list|(
name|CellUtil
operator|.
name|cloneValue
argument_list|(
name|cell
argument_list|)
argument_list|,
operator|new
name|byte
index|[
literal|10
index|]
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
specifier|static
name|ClientProtos
operator|.
name|MultiRequest
name|createRequest
parameter_list|(
name|RowMutations
name|rm
parameter_list|,
name|byte
index|[]
name|regionName
parameter_list|)
throws|throws
name|IOException
block|{
name|ClientProtos
operator|.
name|RegionAction
operator|.
name|Builder
name|builder
init|=
name|RequestConverter
operator|.
name|getRegionActionBuilderWithRegion
argument_list|(
name|ClientProtos
operator|.
name|RegionAction
operator|.
name|newBuilder
argument_list|()
argument_list|,
name|regionName
argument_list|)
decl_stmt|;
name|builder
operator|.
name|setAtomic
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|ClientProtos
operator|.
name|Action
operator|.
name|Builder
name|actionBuilder
init|=
name|ClientProtos
operator|.
name|Action
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
name|ClientProtos
operator|.
name|MutationProto
operator|.
name|Builder
name|mutationBuilder
init|=
name|ClientProtos
operator|.
name|MutationProto
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
name|ClientProtos
operator|.
name|Condition
name|condition
init|=
name|RequestConverter
operator|.
name|buildCondition
argument_list|(
name|rm
operator|.
name|getRow
argument_list|()
argument_list|,
name|FAMILY
argument_list|,
literal|null
argument_list|,
operator|new
name|BinaryComparator
argument_list|(
operator|new
name|byte
index|[
literal|10
index|]
argument_list|)
argument_list|,
name|HBaseProtos
operator|.
name|CompareType
operator|.
name|EQUAL
argument_list|)
decl_stmt|;
for|for
control|(
name|Mutation
name|mutation
range|:
name|rm
operator|.
name|getMutations
argument_list|()
control|)
block|{
name|ClientProtos
operator|.
name|MutationProto
operator|.
name|MutationType
name|mutateType
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|mutation
operator|instanceof
name|Put
condition|)
block|{
name|mutateType
operator|=
name|ClientProtos
operator|.
name|MutationProto
operator|.
name|MutationType
operator|.
name|PUT
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|mutation
operator|instanceof
name|Delete
condition|)
block|{
name|mutateType
operator|=
name|ClientProtos
operator|.
name|MutationProto
operator|.
name|MutationType
operator|.
name|DELETE
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|DoNotRetryIOException
argument_list|(
literal|"RowMutations supports only put and delete, not "
operator|+
name|mutation
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
throw|;
block|}
name|mutationBuilder
operator|.
name|clear
argument_list|()
expr_stmt|;
name|ClientProtos
operator|.
name|MutationProto
name|mp
init|=
name|ProtobufUtil
operator|.
name|toMutationNoData
argument_list|(
name|mutateType
argument_list|,
name|mutation
argument_list|,
name|mutationBuilder
argument_list|)
decl_stmt|;
name|actionBuilder
operator|.
name|clear
argument_list|()
expr_stmt|;
name|actionBuilder
operator|.
name|setMutation
argument_list|(
name|mp
argument_list|)
expr_stmt|;
name|builder
operator|.
name|addAction
argument_list|(
name|actionBuilder
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|ClientProtos
operator|.
name|MultiRequest
name|request
init|=
name|ClientProtos
operator|.
name|MultiRequest
operator|.
name|newBuilder
argument_list|()
operator|.
name|addRegionAction
argument_list|(
name|builder
operator|.
name|build
argument_list|()
argument_list|)
operator|.
name|setCondition
argument_list|(
name|condition
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
return|return
name|request
return|;
block|}
comment|/**    * This test depends on how regionserver process the batch ops.    * 1) group the put/delete until meeting the increment    * 2) process the batch of put/delete    * 3) process the increment    * see RSRpcServices#doNonAtomicRegionMutation    */
annotation|@
name|Test
specifier|public
name|void
name|testNonAtomicOperations
parameter_list|()
throws|throws
name|InterruptedException
throws|,
name|IOException
block|{
name|Increment
name|inc
init|=
operator|new
name|Increment
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"good"
argument_list|)
argument_list|)
operator|.
name|addColumn
argument_list|(
name|FAMILY
argument_list|,
literal|null
argument_list|,
literal|100
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Row
argument_list|>
name|batches
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
comment|// the first and second puts will be group by regionserver
name|batches
operator|.
name|add
argument_list|(
operator|new
name|Put
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"fail"
argument_list|)
argument_list|)
operator|.
name|addColumn
argument_list|(
name|FAMILY
argument_list|,
literal|null
argument_list|,
operator|new
name|byte
index|[
name|CELL_SIZE
index|]
argument_list|)
argument_list|)
expr_stmt|;
name|batches
operator|.
name|add
argument_list|(
operator|new
name|Put
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"fail"
argument_list|)
argument_list|)
operator|.
name|addColumn
argument_list|(
name|FAMILY
argument_list|,
literal|null
argument_list|,
operator|new
name|byte
index|[
name|CELL_SIZE
index|]
argument_list|)
argument_list|)
expr_stmt|;
comment|// this Increment should succeed
name|batches
operator|.
name|add
argument_list|(
name|inc
argument_list|)
expr_stmt|;
comment|// this put should succeed
name|batches
operator|.
name|add
argument_list|(
operator|new
name|Put
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"good"
argument_list|)
argument_list|)
operator|.
name|addColumn
argument_list|(
name|FAMILY
argument_list|,
literal|null
argument_list|,
operator|new
name|byte
index|[
literal|1
index|]
argument_list|)
argument_list|)
expr_stmt|;
name|Object
index|[]
name|objs
init|=
operator|new
name|Object
index|[
name|batches
operator|.
name|size
argument_list|()
index|]
decl_stmt|;
try|try
init|(
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
name|TABLE_NAME
argument_list|)
init|)
block|{
name|table
operator|.
name|batch
argument_list|(
name|batches
argument_list|,
name|objs
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"Where is the exception? We put the malformed cells!!!"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|RetriesExhaustedWithDetailsException
name|e
parameter_list|)
block|{
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|e
operator|.
name|getNumExceptions
argument_list|()
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
operator|!=
name|e
operator|.
name|getNumExceptions
argument_list|()
condition|;
operator|++
name|i
control|)
block|{
name|assertNotNull
argument_list|(
name|e
operator|.
name|getCause
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|DoNotRetryIOException
operator|.
name|class
argument_list|,
name|e
operator|.
name|getCause
argument_list|(
name|i
argument_list|)
operator|.
name|getClass
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"fail"
argument_list|,
name|Bytes
operator|.
name|toString
argument_list|(
name|e
operator|.
name|getRow
argument_list|(
name|i
argument_list|)
operator|.
name|getRow
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
finally|finally
block|{
name|assertObjects
argument_list|(
name|objs
argument_list|,
name|batches
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|objs
index|[
literal|0
index|]
operator|instanceof
name|IOException
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|objs
index|[
literal|1
index|]
operator|instanceof
name|IOException
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|Result
operator|.
name|class
argument_list|,
name|objs
index|[
literal|2
index|]
operator|.
name|getClass
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|Result
operator|.
name|class
argument_list|,
name|objs
index|[
literal|3
index|]
operator|.
name|getClass
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testRowMutations
parameter_list|()
throws|throws
name|InterruptedException
throws|,
name|IOException
block|{
name|Put
name|put
init|=
operator|new
name|Put
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"good"
argument_list|)
argument_list|)
operator|.
name|addColumn
argument_list|(
name|FAMILY
argument_list|,
literal|null
argument_list|,
operator|new
name|byte
index|[
literal|1
index|]
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Row
argument_list|>
name|batches
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|RowMutations
name|mutations
init|=
operator|new
name|RowMutations
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"fail"
argument_list|)
argument_list|)
decl_stmt|;
comment|// the first and second puts will be group by regionserver
name|mutations
operator|.
name|add
argument_list|(
operator|new
name|Put
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"fail"
argument_list|)
argument_list|)
operator|.
name|addColumn
argument_list|(
name|FAMILY
argument_list|,
literal|null
argument_list|,
operator|new
name|byte
index|[
name|CELL_SIZE
index|]
argument_list|)
argument_list|)
expr_stmt|;
name|mutations
operator|.
name|add
argument_list|(
operator|new
name|Put
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"fail"
argument_list|)
argument_list|)
operator|.
name|addColumn
argument_list|(
name|FAMILY
argument_list|,
literal|null
argument_list|,
operator|new
name|byte
index|[
name|CELL_SIZE
index|]
argument_list|)
argument_list|)
expr_stmt|;
name|batches
operator|.
name|add
argument_list|(
name|mutations
argument_list|)
expr_stmt|;
comment|// this bm should succeed
name|mutations
operator|=
operator|new
name|RowMutations
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"good"
argument_list|)
argument_list|)
expr_stmt|;
name|mutations
operator|.
name|add
argument_list|(
name|put
argument_list|)
expr_stmt|;
name|mutations
operator|.
name|add
argument_list|(
name|put
argument_list|)
expr_stmt|;
name|batches
operator|.
name|add
argument_list|(
name|mutations
argument_list|)
expr_stmt|;
name|Object
index|[]
name|objs
init|=
operator|new
name|Object
index|[
name|batches
operator|.
name|size
argument_list|()
index|]
decl_stmt|;
try|try
init|(
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
name|TABLE_NAME
argument_list|)
init|)
block|{
name|table
operator|.
name|batch
argument_list|(
name|batches
argument_list|,
name|objs
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"Where is the exception? We put the malformed cells!!!"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|RetriesExhaustedWithDetailsException
name|e
parameter_list|)
block|{
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|e
operator|.
name|getNumExceptions
argument_list|()
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
operator|!=
name|e
operator|.
name|getNumExceptions
argument_list|()
condition|;
operator|++
name|i
control|)
block|{
name|assertNotNull
argument_list|(
name|e
operator|.
name|getCause
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|e
operator|.
name|getCause
argument_list|(
name|i
argument_list|)
operator|instanceof
name|IOException
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"fail"
argument_list|,
name|Bytes
operator|.
name|toString
argument_list|(
name|e
operator|.
name|getRow
argument_list|(
name|i
argument_list|)
operator|.
name|getRow
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
finally|finally
block|{
name|assertObjects
argument_list|(
name|objs
argument_list|,
name|batches
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|objs
index|[
literal|0
index|]
operator|instanceof
name|IOException
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|Result
operator|.
name|class
argument_list|,
name|objs
index|[
literal|1
index|]
operator|.
name|getClass
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
specifier|static
name|void
name|assertObjects
parameter_list|(
name|Object
index|[]
name|objs
parameter_list|,
name|int
name|expectedSize
parameter_list|)
block|{
name|int
name|count
init|=
literal|0
decl_stmt|;
for|for
control|(
name|Object
name|obj
range|:
name|objs
control|)
block|{
name|assertNotNull
argument_list|(
name|obj
argument_list|)
expr_stmt|;
operator|++
name|count
expr_stmt|;
block|}
name|assertEquals
argument_list|(
name|expectedSize
argument_list|,
name|count
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

