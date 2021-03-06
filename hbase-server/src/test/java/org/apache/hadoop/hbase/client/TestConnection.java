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
name|assertTrue
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|ServiceException
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
name|List
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Set
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
name|ThreadLocalRandom
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
name|concurrent
operator|.
name|atomic
operator|.
name|AtomicReference
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|stream
operator|.
name|Collectors
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|stream
operator|.
name|IntStream
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
name|HRegionLocation
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
name|ServerName
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
name|Waiter
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
name|coprocessor
operator|.
name|Batch
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
name|MultiRowMutationEndpoint
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
name|RpcClient
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
name|protobuf
operator|.
name|generated
operator|.
name|MultiRowMutationProtos
operator|.
name|MultiRowMutationService
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
name|protobuf
operator|.
name|generated
operator|.
name|MultiRowMutationProtos
operator|.
name|MutateRowsResponse
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
name|EnvironmentEdgeManager
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
name|ManualEnvironmentEdge
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
name|Assert
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
name|rules
operator|.
name|TestName
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
name|hbase
operator|.
name|thirdparty
operator|.
name|io
operator|.
name|netty
operator|.
name|util
operator|.
name|ResourceLeakDetector
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hbase
operator|.
name|thirdparty
operator|.
name|io
operator|.
name|netty
operator|.
name|util
operator|.
name|ResourceLeakDetector
operator|.
name|Level
import|;
end_import

begin_comment
comment|/**  * This class is for testing {@link Connection}.  */
end_comment

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|LargeTests
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|TestConnection
block|{
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
name|TestConnection
operator|.
name|class
argument_list|)
decl_stmt|;
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
name|TestConnection
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
specifier|static
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
name|FAM_NAM
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"f"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|ROW
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"bbb"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|RPC_RETRY
init|=
literal|5
decl_stmt|;
annotation|@
name|Rule
specifier|public
name|TestName
name|name
init|=
operator|new
name|TestName
argument_list|()
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
name|ResourceLeakDetector
operator|.
name|setLevel
argument_list|(
name|Level
operator|.
name|PARANOID
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setBoolean
argument_list|(
name|HConstants
operator|.
name|STATUS_PUBLISHED
argument_list|,
literal|true
argument_list|)
expr_stmt|;
comment|// Up the handlers; this test needs more than usual.
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setInt
argument_list|(
name|HConstants
operator|.
name|REGION_SERVER_HIGH_PRIORITY_HANDLER_COUNT
argument_list|,
literal|10
argument_list|)
expr_stmt|;
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
name|RPC_RETRY
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setInt
argument_list|(
name|HConstants
operator|.
name|REGION_SERVER_HANDLER_COUNT
argument_list|,
literal|3
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|startMiniCluster
argument_list|(
literal|2
argument_list|)
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
annotation|@
name|After
specifier|public
name|void
name|tearDown
parameter_list|()
throws|throws
name|IOException
block|{
name|TEST_UTIL
operator|.
name|getAdmin
argument_list|()
operator|.
name|balancerSwitch
argument_list|(
literal|true
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
comment|/**    * Naive test to check that Connection#getAdmin returns a properly constructed HBaseAdmin object    * @throws IOException Unable to construct admin    */
annotation|@
name|Test
specifier|public
name|void
name|testAdminFactory
parameter_list|()
throws|throws
name|IOException
block|{
name|Connection
name|con1
init|=
name|ConnectionFactory
operator|.
name|createConnection
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
name|Admin
name|admin
init|=
name|con1
operator|.
name|getAdmin
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
name|admin
operator|.
name|getConnection
argument_list|()
operator|==
name|con1
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|admin
operator|.
name|getConfiguration
argument_list|()
operator|==
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
expr_stmt|;
name|con1
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
comment|/**    * Test that we can handle connection close: it will trigger a retry, but the calls will finish.    */
annotation|@
name|Test
specifier|public
name|void
name|testConnectionCloseAllowsInterrupt
parameter_list|()
throws|throws
name|Exception
block|{
name|testConnectionClose
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testConnectionNotAllowsInterrupt
parameter_list|()
throws|throws
name|Exception
block|{
name|testConnectionClose
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|testConnectionClose
parameter_list|(
name|boolean
name|allowsInterrupt
parameter_list|)
throws|throws
name|Exception
block|{
name|TableName
name|tableName
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"HCM-testConnectionClose"
operator|+
name|allowsInterrupt
argument_list|)
decl_stmt|;
name|TEST_UTIL
operator|.
name|createTable
argument_list|(
name|tableName
argument_list|,
name|FAM_NAM
argument_list|)
operator|.
name|close
argument_list|()
expr_stmt|;
name|TEST_UTIL
operator|.
name|getAdmin
argument_list|()
operator|.
name|balancerSwitch
argument_list|(
literal|false
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|Configuration
name|c2
init|=
operator|new
name|Configuration
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
comment|// We want to work on a separate connection.
name|c2
operator|.
name|set
argument_list|(
name|HConstants
operator|.
name|HBASE_CLIENT_INSTANCE_ID
argument_list|,
name|String
operator|.
name|valueOf
argument_list|(
operator|-
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|c2
operator|.
name|setInt
argument_list|(
name|HConstants
operator|.
name|HBASE_CLIENT_RETRIES_NUMBER
argument_list|,
literal|100
argument_list|)
expr_stmt|;
comment|// retry a lot
name|c2
operator|.
name|setInt
argument_list|(
name|HConstants
operator|.
name|HBASE_CLIENT_PAUSE
argument_list|,
literal|1
argument_list|)
expr_stmt|;
comment|// don't wait between retries.
name|c2
operator|.
name|setInt
argument_list|(
name|RpcClient
operator|.
name|FAILED_SERVER_EXPIRY_KEY
argument_list|,
literal|0
argument_list|)
expr_stmt|;
comment|// Server do not really expire
name|c2
operator|.
name|setBoolean
argument_list|(
name|RpcClient
operator|.
name|SPECIFIC_WRITE_THREAD
argument_list|,
name|allowsInterrupt
argument_list|)
expr_stmt|;
comment|// to avoid the client to be stuck when do the Get
name|c2
operator|.
name|setInt
argument_list|(
name|HConstants
operator|.
name|HBASE_CLIENT_META_OPERATION_TIMEOUT
argument_list|,
literal|10000
argument_list|)
expr_stmt|;
name|c2
operator|.
name|setInt
argument_list|(
name|HConstants
operator|.
name|HBASE_CLIENT_OPERATION_TIMEOUT
argument_list|,
literal|10000
argument_list|)
expr_stmt|;
name|c2
operator|.
name|setInt
argument_list|(
name|HConstants
operator|.
name|HBASE_RPC_TIMEOUT_KEY
argument_list|,
literal|5000
argument_list|)
expr_stmt|;
name|Connection
name|connection
init|=
name|ConnectionFactory
operator|.
name|createConnection
argument_list|(
name|c2
argument_list|)
decl_stmt|;
specifier|final
name|Table
name|table
init|=
name|connection
operator|.
name|getTable
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
name|Put
name|put
init|=
operator|new
name|Put
argument_list|(
name|ROW
argument_list|)
decl_stmt|;
name|put
operator|.
name|addColumn
argument_list|(
name|FAM_NAM
argument_list|,
name|ROW
argument_list|,
name|ROW
argument_list|)
expr_stmt|;
name|table
operator|.
name|put
argument_list|(
name|put
argument_list|)
expr_stmt|;
comment|// 4 steps: ready=0; doGets=1; mustStop=2; stopped=3
specifier|final
name|AtomicInteger
name|step
init|=
operator|new
name|AtomicInteger
argument_list|(
literal|0
argument_list|)
decl_stmt|;
specifier|final
name|AtomicReference
argument_list|<
name|Throwable
argument_list|>
name|failed
init|=
operator|new
name|AtomicReference
argument_list|<>
argument_list|(
literal|null
argument_list|)
decl_stmt|;
name|Thread
name|t
init|=
operator|new
name|Thread
argument_list|(
literal|"testConnectionCloseThread"
argument_list|)
block|{
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
name|int
name|done
init|=
literal|0
decl_stmt|;
try|try
block|{
name|step
operator|.
name|set
argument_list|(
literal|1
argument_list|)
expr_stmt|;
while|while
condition|(
name|step
operator|.
name|get
argument_list|()
operator|==
literal|1
condition|)
block|{
name|Get
name|get
init|=
operator|new
name|Get
argument_list|(
name|ROW
argument_list|)
decl_stmt|;
name|table
operator|.
name|get
argument_list|(
name|get
argument_list|)
expr_stmt|;
name|done
operator|++
expr_stmt|;
if|if
condition|(
name|done
operator|%
literal|100
operator|==
literal|0
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"done="
operator|+
name|done
argument_list|)
expr_stmt|;
block|}
comment|// without the sleep, will cause the exception for too many files in
comment|// org.apache.hadoop.hdfs.server.datanode.DataXceiver
name|Thread
operator|.
name|sleep
argument_list|(
literal|100
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
name|failed
operator|.
name|set
argument_list|(
name|t
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|error
argument_list|(
name|t
operator|.
name|toString
argument_list|()
argument_list|,
name|t
argument_list|)
expr_stmt|;
block|}
name|step
operator|.
name|set
argument_list|(
literal|3
argument_list|)
expr_stmt|;
block|}
block|}
decl_stmt|;
name|t
operator|.
name|start
argument_list|()
expr_stmt|;
name|TEST_UTIL
operator|.
name|waitFor
argument_list|(
literal|20000
argument_list|,
operator|new
name|Waiter
operator|.
name|Predicate
argument_list|<
name|Exception
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|boolean
name|evaluate
parameter_list|()
throws|throws
name|Exception
block|{
return|return
name|step
operator|.
name|get
argument_list|()
operator|==
literal|1
return|;
block|}
block|}
argument_list|)
expr_stmt|;
name|ServerName
name|sn
decl_stmt|;
try|try
init|(
name|RegionLocator
name|rl
init|=
name|connection
operator|.
name|getRegionLocator
argument_list|(
name|tableName
argument_list|)
init|)
block|{
name|sn
operator|=
name|rl
operator|.
name|getRegionLocation
argument_list|(
name|ROW
argument_list|)
operator|.
name|getServerName
argument_list|()
expr_stmt|;
block|}
name|RpcClient
name|rpcClient
init|=
operator|(
operator|(
name|AsyncConnectionImpl
operator|)
name|connection
operator|.
name|toAsyncConnection
argument_list|()
operator|)
operator|.
name|rpcClient
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Going to cancel connections. connection="
operator|+
name|connection
operator|.
name|toString
argument_list|()
operator|+
literal|", sn="
operator|+
name|sn
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
literal|500
condition|;
name|i
operator|++
control|)
block|{
name|rpcClient
operator|.
name|cancelConnections
argument_list|(
name|sn
argument_list|)
expr_stmt|;
name|Thread
operator|.
name|sleep
argument_list|(
literal|50
argument_list|)
expr_stmt|;
block|}
name|step
operator|.
name|compareAndSet
argument_list|(
literal|1
argument_list|,
literal|2
argument_list|)
expr_stmt|;
comment|// The test may fail here if the thread doing the gets is stuck. The way to find
comment|// out what's happening is to look for the thread named 'testConnectionCloseThread'
name|TEST_UTIL
operator|.
name|waitFor
argument_list|(
literal|40000
argument_list|,
operator|new
name|Waiter
operator|.
name|Predicate
argument_list|<
name|Exception
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|boolean
name|evaluate
parameter_list|()
throws|throws
name|Exception
block|{
return|return
name|step
operator|.
name|get
argument_list|()
operator|==
literal|3
return|;
block|}
block|}
argument_list|)
expr_stmt|;
name|table
operator|.
name|close
argument_list|()
expr_stmt|;
name|connection
operator|.
name|close
argument_list|()
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
literal|"Unexpected exception is "
operator|+
name|failed
operator|.
name|get
argument_list|()
argument_list|,
name|failed
operator|.
name|get
argument_list|()
operator|==
literal|null
argument_list|)
expr_stmt|;
block|}
comment|/**    * Test that connection can become idle without breaking everything.    */
annotation|@
name|Test
specifier|public
name|void
name|testConnectionIdle
parameter_list|()
throws|throws
name|Exception
block|{
specifier|final
name|TableName
name|tableName
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
name|name
operator|.
name|getMethodName
argument_list|()
argument_list|)
decl_stmt|;
name|TEST_UTIL
operator|.
name|createTable
argument_list|(
name|tableName
argument_list|,
name|FAM_NAM
argument_list|)
operator|.
name|close
argument_list|()
expr_stmt|;
name|int
name|idleTime
init|=
literal|20000
decl_stmt|;
name|boolean
name|previousBalance
init|=
name|TEST_UTIL
operator|.
name|getAdmin
argument_list|()
operator|.
name|balancerSwitch
argument_list|(
literal|false
argument_list|,
literal|true
argument_list|)
decl_stmt|;
name|Configuration
name|c2
init|=
operator|new
name|Configuration
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
comment|// We want to work on a separate connection.
name|c2
operator|.
name|set
argument_list|(
name|HConstants
operator|.
name|HBASE_CLIENT_INSTANCE_ID
argument_list|,
name|String
operator|.
name|valueOf
argument_list|(
operator|-
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|c2
operator|.
name|setInt
argument_list|(
name|HConstants
operator|.
name|HBASE_CLIENT_RETRIES_NUMBER
argument_list|,
literal|1
argument_list|)
expr_stmt|;
comment|// Don't retry: retry = test failed
name|c2
operator|.
name|setInt
argument_list|(
name|RpcClient
operator|.
name|IDLE_TIME
argument_list|,
name|idleTime
argument_list|)
expr_stmt|;
name|Connection
name|connection
init|=
name|ConnectionFactory
operator|.
name|createConnection
argument_list|(
name|c2
argument_list|)
decl_stmt|;
specifier|final
name|Table
name|table
init|=
name|connection
operator|.
name|getTable
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
name|Put
name|put
init|=
operator|new
name|Put
argument_list|(
name|ROW
argument_list|)
decl_stmt|;
name|put
operator|.
name|addColumn
argument_list|(
name|FAM_NAM
argument_list|,
name|ROW
argument_list|,
name|ROW
argument_list|)
expr_stmt|;
name|table
operator|.
name|put
argument_list|(
name|put
argument_list|)
expr_stmt|;
name|ManualEnvironmentEdge
name|mee
init|=
operator|new
name|ManualEnvironmentEdge
argument_list|()
decl_stmt|;
name|mee
operator|.
name|setValue
argument_list|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|)
expr_stmt|;
name|EnvironmentEdgeManager
operator|.
name|injectEdge
argument_list|(
name|mee
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"first get"
argument_list|)
expr_stmt|;
name|table
operator|.
name|get
argument_list|(
operator|new
name|Get
argument_list|(
name|ROW
argument_list|)
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"first get - changing the time& sleeping"
argument_list|)
expr_stmt|;
name|mee
operator|.
name|incValue
argument_list|(
name|idleTime
operator|+
literal|1000
argument_list|)
expr_stmt|;
name|Thread
operator|.
name|sleep
argument_list|(
literal|1500
argument_list|)
expr_stmt|;
comment|// we need to wait a little for the connection to be seen as idle.
comment|// 1500 = sleep time in RpcClient#waitForWork + a margin
name|LOG
operator|.
name|info
argument_list|(
literal|"second get - connection has been marked idle in the middle"
argument_list|)
expr_stmt|;
comment|// To check that the connection actually became idle would need to read some private
comment|// fields of RpcClient.
name|table
operator|.
name|get
argument_list|(
operator|new
name|Get
argument_list|(
name|ROW
argument_list|)
argument_list|)
expr_stmt|;
name|mee
operator|.
name|incValue
argument_list|(
name|idleTime
operator|+
literal|1000
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"third get - connection is idle, but the reader doesn't know yet"
argument_list|)
expr_stmt|;
comment|// We're testing here a special case:
comment|// time limit reached BUT connection not yet reclaimed AND a new call.
comment|// in this situation, we don't close the connection, instead we use it immediately.
comment|// If we're very unlucky we can have a race condition in the test: the connection is already
comment|// under closing when we do the get, so we have an exception, and we don't retry as the
comment|// retry number is 1. The probability is very very low, and seems acceptable for now. It's
comment|// a test issue only.
name|table
operator|.
name|get
argument_list|(
operator|new
name|Get
argument_list|(
name|ROW
argument_list|)
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"we're done - time will change back"
argument_list|)
expr_stmt|;
name|table
operator|.
name|close
argument_list|()
expr_stmt|;
name|connection
operator|.
name|close
argument_list|()
expr_stmt|;
name|EnvironmentEdgeManager
operator|.
name|reset
argument_list|()
expr_stmt|;
name|TEST_UTIL
operator|.
name|getAdmin
argument_list|()
operator|.
name|balancerSwitch
argument_list|(
name|previousBalance
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testClosing
parameter_list|()
throws|throws
name|Exception
block|{
name|Configuration
name|configuration
init|=
operator|new
name|Configuration
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
name|configuration
operator|.
name|set
argument_list|(
name|HConstants
operator|.
name|HBASE_CLIENT_INSTANCE_ID
argument_list|,
name|String
operator|.
name|valueOf
argument_list|(
name|ThreadLocalRandom
operator|.
name|current
argument_list|()
operator|.
name|nextInt
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
comment|// as connection caching is going away, now we're just testing
comment|// that closed connection does actually get closed.
name|Connection
name|c1
init|=
name|ConnectionFactory
operator|.
name|createConnection
argument_list|(
name|configuration
argument_list|)
decl_stmt|;
name|Connection
name|c2
init|=
name|ConnectionFactory
operator|.
name|createConnection
argument_list|(
name|configuration
argument_list|)
decl_stmt|;
comment|// no caching, different connections
name|assertTrue
argument_list|(
name|c1
operator|!=
name|c2
argument_list|)
expr_stmt|;
comment|// closing independently
name|c1
operator|.
name|close
argument_list|()
expr_stmt|;
name|assertTrue
argument_list|(
name|c1
operator|.
name|isClosed
argument_list|()
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|c2
operator|.
name|isClosed
argument_list|()
argument_list|)
expr_stmt|;
name|c2
operator|.
name|close
argument_list|()
expr_stmt|;
name|assertTrue
argument_list|(
name|c2
operator|.
name|isClosed
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**    * Trivial test to verify that nobody messes with    * {@link ConnectionFactory#createConnection(Configuration)}    */
annotation|@
name|Test
specifier|public
name|void
name|testCreateConnection
parameter_list|()
throws|throws
name|Exception
block|{
name|Configuration
name|configuration
init|=
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
name|Connection
name|c1
init|=
name|ConnectionFactory
operator|.
name|createConnection
argument_list|(
name|configuration
argument_list|)
decl_stmt|;
name|Connection
name|c2
init|=
name|ConnectionFactory
operator|.
name|createConnection
argument_list|(
name|configuration
argument_list|)
decl_stmt|;
comment|// created from the same configuration, yet they are different
name|assertTrue
argument_list|(
name|c1
operator|!=
name|c2
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|c1
operator|.
name|getConfiguration
argument_list|()
operator|==
name|c2
operator|.
name|getConfiguration
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/*   ====> With MasterRegistry, connections cannot outlast the masters' lifetime.   @Test   public void testConnectionRideOverClusterRestart() throws IOException, InterruptedException {     Configuration config = new Configuration(TEST_UTIL.getConfiguration());      final TableName tableName = TableName.valueOf(name.getMethodName());     TEST_UTIL.createTable(tableName, new byte[][] { FAM_NAM }).close();      Connection connection = ConnectionFactory.createConnection(config);     Table table = connection.getTable(tableName);      // this will cache the meta location and table's region location     table.get(new Get(Bytes.toBytes("foo")));      // restart HBase     TEST_UTIL.shutdownMiniHBaseCluster();     TEST_UTIL.restartHBaseCluster(2);     // this should be able to discover new locations for meta and table's region     table.get(new Get(Bytes.toBytes("foo")));     TEST_UTIL.deleteTable(tableName);     table.close();     connection.close();   }    */
annotation|@
name|Test
specifier|public
name|void
name|testLocateRegionsWithRegionReplicas
parameter_list|()
throws|throws
name|IOException
block|{
name|int
name|regionReplication
init|=
literal|3
decl_stmt|;
name|byte
index|[]
name|family
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"cf"
argument_list|)
decl_stmt|;
name|TableName
name|tableName
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
name|name
operator|.
name|getMethodName
argument_list|()
argument_list|)
decl_stmt|;
comment|// Create a table with region replicas
name|TableDescriptorBuilder
name|builder
init|=
name|TableDescriptorBuilder
operator|.
name|newBuilder
argument_list|(
name|tableName
argument_list|)
operator|.
name|setRegionReplication
argument_list|(
name|regionReplication
argument_list|)
operator|.
name|setColumnFamily
argument_list|(
name|ColumnFamilyDescriptorBuilder
operator|.
name|of
argument_list|(
name|family
argument_list|)
argument_list|)
decl_stmt|;
name|TEST_UTIL
operator|.
name|getAdmin
argument_list|()
operator|.
name|createTable
argument_list|(
name|builder
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
try|try
init|(
name|Connection
name|conn
init|=
name|ConnectionFactory
operator|.
name|createConnection
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
init|;
name|RegionLocator
name|locator
operator|=
name|conn
operator|.
name|getRegionLocator
argument_list|(
name|tableName
argument_list|)
init|)
block|{
comment|// Get locations of the regions of the table
name|List
argument_list|<
name|HRegionLocation
argument_list|>
name|locations
init|=
name|locator
operator|.
name|getAllRegionLocations
argument_list|()
decl_stmt|;
comment|// The size of the returned locations should be 3
name|assertEquals
argument_list|(
name|regionReplication
argument_list|,
name|locations
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
comment|// The replicaIds of the returned locations should be 0, 1 and 2
name|Set
argument_list|<
name|Integer
argument_list|>
name|expectedReplicaIds
init|=
name|IntStream
operator|.
name|range
argument_list|(
literal|0
argument_list|,
name|regionReplication
argument_list|)
operator|.
name|boxed
argument_list|()
operator|.
name|collect
argument_list|(
name|Collectors
operator|.
name|toSet
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|HRegionLocation
name|location
range|:
name|locations
control|)
block|{
name|assertTrue
argument_list|(
name|expectedReplicaIds
operator|.
name|remove
argument_list|(
name|location
operator|.
name|getRegion
argument_list|()
operator|.
name|getReplicaId
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
finally|finally
block|{
name|TEST_UTIL
operator|.
name|deleteTable
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
argument_list|(
name|expected
operator|=
name|DoNotRetryIOException
operator|.
name|class
argument_list|)
specifier|public
name|void
name|testClosedConnection
parameter_list|()
throws|throws
name|ServiceException
throws|,
name|Throwable
block|{
name|byte
index|[]
name|family
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"cf"
argument_list|)
decl_stmt|;
name|TableName
name|tableName
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
name|name
operator|.
name|getMethodName
argument_list|()
argument_list|)
decl_stmt|;
name|TableDescriptorBuilder
name|builder
init|=
name|TableDescriptorBuilder
operator|.
name|newBuilder
argument_list|(
name|tableName
argument_list|)
operator|.
name|setCoprocessor
argument_list|(
name|MultiRowMutationEndpoint
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
operator|.
name|setColumnFamily
argument_list|(
name|ColumnFamilyDescriptorBuilder
operator|.
name|of
argument_list|(
name|family
argument_list|)
argument_list|)
decl_stmt|;
name|TEST_UTIL
operator|.
name|getAdmin
argument_list|()
operator|.
name|createTable
argument_list|(
name|builder
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
name|Connection
name|conn
init|=
name|ConnectionFactory
operator|.
name|createConnection
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
comment|// cache the location
try|try
init|(
name|Table
name|table
init|=
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
name|get
argument_list|(
operator|new
name|Get
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|0
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|conn
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
name|Batch
operator|.
name|Call
argument_list|<
name|MultiRowMutationService
argument_list|,
name|MutateRowsResponse
argument_list|>
name|callable
init|=
name|service
lambda|->
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Should not arrive here"
argument_list|)
throw|;
block|}
decl_stmt|;
name|conn
operator|.
name|getTable
argument_list|(
name|tableName
argument_list|)
operator|.
name|coprocessorService
argument_list|(
name|MultiRowMutationService
operator|.
name|class
argument_list|,
name|HConstants
operator|.
name|EMPTY_START_ROW
argument_list|,
name|HConstants
operator|.
name|EMPTY_END_ROW
argument_list|,
name|callable
argument_list|)
expr_stmt|;
block|}
comment|// There is no assertion, but you need to confirm that there is no resource leak output from netty
annotation|@
name|Test
specifier|public
name|void
name|testCancelConnectionMemoryLeak
parameter_list|()
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|TableName
name|tableName
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
name|name
operator|.
name|getMethodName
argument_list|()
argument_list|)
decl_stmt|;
name|TEST_UTIL
operator|.
name|createTable
argument_list|(
name|tableName
argument_list|,
name|FAM_NAM
argument_list|)
operator|.
name|close
argument_list|()
expr_stmt|;
name|TEST_UTIL
operator|.
name|getAdmin
argument_list|()
operator|.
name|balancerSwitch
argument_list|(
literal|false
argument_list|,
literal|true
argument_list|)
expr_stmt|;
try|try
init|(
name|Connection
name|connection
init|=
name|ConnectionFactory
operator|.
name|createConnection
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
init|;
name|Table
name|table
operator|=
name|connection
operator|.
name|getTable
argument_list|(
name|tableName
argument_list|)
init|)
block|{
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
literal|"1"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|ServerName
name|sn
init|=
name|TEST_UTIL
operator|.
name|getRSForFirstRegionInTable
argument_list|(
name|tableName
argument_list|)
operator|.
name|getServerName
argument_list|()
decl_stmt|;
name|RpcClient
name|rpcClient
init|=
operator|(
operator|(
name|AsyncConnectionImpl
operator|)
name|connection
operator|.
name|toAsyncConnection
argument_list|()
operator|)
operator|.
name|rpcClient
decl_stmt|;
name|rpcClient
operator|.
name|cancelConnections
argument_list|(
name|sn
argument_list|)
expr_stmt|;
name|Thread
operator|.
name|sleep
argument_list|(
literal|1000
argument_list|)
expr_stmt|;
name|System
operator|.
name|gc
argument_list|()
expr_stmt|;
name|Thread
operator|.
name|sleep
argument_list|(
literal|1000
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

