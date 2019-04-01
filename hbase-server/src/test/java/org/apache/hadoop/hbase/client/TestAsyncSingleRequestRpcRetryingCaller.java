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
name|hamcrest
operator|.
name|CoreMatchers
operator|.
name|instanceOf
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
name|assertArrayEquals
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
name|assertThat
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
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|TimeUnit
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
name|AtomicBoolean
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
name|security
operator|.
name|User
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
name|TestAsyncSingleRequestRpcRetryingCaller
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
name|TestAsyncSingleRequestRpcRetryingCaller
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
specifier|private
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
specifier|private
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
specifier|private
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
specifier|private
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
specifier|private
specifier|static
name|AsyncConnectionImpl
name|CONN
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
name|TEST_UTIL
operator|.
name|startMiniCluster
argument_list|(
literal|2
argument_list|)
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
name|TEST_UTIL
operator|.
name|createTable
argument_list|(
name|TABLE_NAME
argument_list|,
name|FAMILY
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|waitTableAvailable
argument_list|(
name|TABLE_NAME
argument_list|)
expr_stmt|;
name|AsyncRegistry
name|registry
init|=
name|AsyncRegistryFactory
operator|.
name|getRegistry
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
name|CONN
operator|=
operator|new
name|AsyncConnectionImpl
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|registry
argument_list|,
name|registry
operator|.
name|getClusterId
argument_list|()
operator|.
name|get
argument_list|()
argument_list|,
name|User
operator|.
name|getCurrent
argument_list|()
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
name|IOUtils
operator|.
name|closeQuietly
argument_list|(
name|CONN
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testRegionMove
parameter_list|()
throws|throws
name|InterruptedException
throws|,
name|ExecutionException
throws|,
name|IOException
block|{
comment|// This will leave a cached entry in location cache
name|HRegionLocation
name|loc
init|=
name|CONN
operator|.
name|getRegionLocator
argument_list|(
name|TABLE_NAME
argument_list|)
operator|.
name|getRegionLocation
argument_list|(
name|ROW
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|int
name|index
init|=
name|TEST_UTIL
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|getServerWith
argument_list|(
name|loc
operator|.
name|getRegion
argument_list|()
operator|.
name|getRegionName
argument_list|()
argument_list|)
decl_stmt|;
name|TEST_UTIL
operator|.
name|getAdmin
argument_list|()
operator|.
name|move
argument_list|(
name|loc
operator|.
name|getRegion
argument_list|()
operator|.
name|getEncodedNameAsBytes
argument_list|()
argument_list|,
name|TEST_UTIL
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|getRegionServer
argument_list|(
literal|1
operator|-
name|index
argument_list|)
operator|.
name|getServerName
argument_list|()
argument_list|)
expr_stmt|;
name|AsyncTable
argument_list|<
name|?
argument_list|>
name|table
init|=
name|CONN
operator|.
name|getTableBuilder
argument_list|(
name|TABLE_NAME
argument_list|)
operator|.
name|setRetryPause
argument_list|(
literal|100
argument_list|,
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
operator|.
name|setMaxRetries
argument_list|(
literal|30
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|table
operator|.
name|put
argument_list|(
operator|new
name|Put
argument_list|(
name|ROW
argument_list|)
operator|.
name|addColumn
argument_list|(
name|FAMILY
argument_list|,
name|QUALIFIER
argument_list|,
name|VALUE
argument_list|)
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
comment|// move back
name|TEST_UTIL
operator|.
name|getAdmin
argument_list|()
operator|.
name|move
argument_list|(
name|loc
operator|.
name|getRegion
argument_list|()
operator|.
name|getEncodedNameAsBytes
argument_list|()
argument_list|,
name|loc
operator|.
name|getServerName
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
name|ROW
argument_list|)
operator|.
name|addColumn
argument_list|(
name|FAMILY
argument_list|,
name|QUALIFIER
argument_list|)
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|assertArrayEquals
argument_list|(
name|VALUE
argument_list|,
name|result
operator|.
name|getValue
argument_list|(
name|FAMILY
argument_list|,
name|QUALIFIER
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|private
parameter_list|<
name|T
parameter_list|>
name|CompletableFuture
argument_list|<
name|T
argument_list|>
name|failedFuture
parameter_list|()
block|{
name|CompletableFuture
argument_list|<
name|T
argument_list|>
name|future
init|=
operator|new
name|CompletableFuture
argument_list|<>
argument_list|()
decl_stmt|;
name|future
operator|.
name|completeExceptionally
argument_list|(
operator|new
name|RuntimeException
argument_list|(
literal|"Inject error!"
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|future
return|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testMaxRetries
parameter_list|()
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
try|try
block|{
name|CONN
operator|.
name|callerFactory
operator|.
name|single
argument_list|()
operator|.
name|table
argument_list|(
name|TABLE_NAME
argument_list|)
operator|.
name|row
argument_list|(
name|ROW
argument_list|)
operator|.
name|operationTimeout
argument_list|(
literal|1
argument_list|,
name|TimeUnit
operator|.
name|DAYS
argument_list|)
operator|.
name|maxAttempts
argument_list|(
literal|3
argument_list|)
operator|.
name|pause
argument_list|(
literal|10
argument_list|,
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
operator|.
name|action
argument_list|(
parameter_list|(
name|controller
parameter_list|,
name|loc
parameter_list|,
name|stub
parameter_list|)
lambda|->
name|failedFuture
argument_list|()
argument_list|)
operator|.
name|call
argument_list|()
operator|.
name|get
argument_list|()
expr_stmt|;
name|fail
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ExecutionException
name|e
parameter_list|)
block|{
name|assertThat
argument_list|(
name|e
operator|.
name|getCause
argument_list|()
argument_list|,
name|instanceOf
argument_list|(
name|RetriesExhaustedException
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testOperationTimeout
parameter_list|()
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|long
name|startNs
init|=
name|System
operator|.
name|nanoTime
argument_list|()
decl_stmt|;
try|try
block|{
name|CONN
operator|.
name|callerFactory
operator|.
name|single
argument_list|()
operator|.
name|table
argument_list|(
name|TABLE_NAME
argument_list|)
operator|.
name|row
argument_list|(
name|ROW
argument_list|)
operator|.
name|operationTimeout
argument_list|(
literal|1
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
operator|.
name|pause
argument_list|(
literal|100
argument_list|,
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
operator|.
name|maxAttempts
argument_list|(
name|Integer
operator|.
name|MAX_VALUE
argument_list|)
operator|.
name|action
argument_list|(
parameter_list|(
name|controller
parameter_list|,
name|loc
parameter_list|,
name|stub
parameter_list|)
lambda|->
name|failedFuture
argument_list|()
argument_list|)
operator|.
name|call
argument_list|()
operator|.
name|get
argument_list|()
expr_stmt|;
name|fail
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ExecutionException
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|e
operator|.
name|getCause
argument_list|()
argument_list|,
name|instanceOf
argument_list|(
name|RetriesExhaustedException
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|long
name|costNs
init|=
name|System
operator|.
name|nanoTime
argument_list|()
operator|-
name|startNs
decl_stmt|;
name|assertTrue
argument_list|(
name|costNs
operator|>=
name|TimeUnit
operator|.
name|SECONDS
operator|.
name|toNanos
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|costNs
operator|<
name|TimeUnit
operator|.
name|SECONDS
operator|.
name|toNanos
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testLocateError
parameter_list|()
throws|throws
name|IOException
throws|,
name|InterruptedException
throws|,
name|ExecutionException
block|{
name|AtomicBoolean
name|errorTriggered
init|=
operator|new
name|AtomicBoolean
argument_list|(
literal|false
argument_list|)
decl_stmt|;
name|AtomicInteger
name|count
init|=
operator|new
name|AtomicInteger
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|HRegionLocation
name|loc
init|=
name|CONN
operator|.
name|getRegionLocator
argument_list|(
name|TABLE_NAME
argument_list|)
operator|.
name|getRegionLocation
argument_list|(
name|ROW
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|AsyncRegionLocator
name|mockedLocator
init|=
operator|new
name|AsyncRegionLocator
argument_list|(
name|CONN
argument_list|,
name|AsyncConnectionImpl
operator|.
name|RETRY_TIMER
argument_list|)
block|{
annotation|@
name|Override
name|CompletableFuture
argument_list|<
name|HRegionLocation
argument_list|>
name|getRegionLocation
parameter_list|(
name|TableName
name|tableName
parameter_list|,
name|byte
index|[]
name|row
parameter_list|,
name|int
name|replicaId
parameter_list|,
name|RegionLocateType
name|locateType
parameter_list|,
name|long
name|timeoutNs
parameter_list|)
block|{
if|if
condition|(
name|tableName
operator|.
name|equals
argument_list|(
name|TABLE_NAME
argument_list|)
condition|)
block|{
name|CompletableFuture
argument_list|<
name|HRegionLocation
argument_list|>
name|future
init|=
operator|new
name|CompletableFuture
argument_list|<>
argument_list|()
decl_stmt|;
if|if
condition|(
name|count
operator|.
name|getAndIncrement
argument_list|()
operator|==
literal|0
condition|)
block|{
name|errorTriggered
operator|.
name|set
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|future
operator|.
name|completeExceptionally
argument_list|(
operator|new
name|RuntimeException
argument_list|(
literal|"Inject error!"
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|future
operator|.
name|complete
argument_list|(
name|loc
argument_list|)
expr_stmt|;
block|}
return|return
name|future
return|;
block|}
else|else
block|{
return|return
name|super
operator|.
name|getRegionLocation
argument_list|(
name|tableName
argument_list|,
name|row
argument_list|,
name|replicaId
argument_list|,
name|locateType
argument_list|,
name|timeoutNs
argument_list|)
return|;
block|}
block|}
annotation|@
name|Override
name|void
name|updateCachedLocationOnError
parameter_list|(
name|HRegionLocation
name|loc
parameter_list|,
name|Throwable
name|exception
parameter_list|)
block|{         }
block|}
decl_stmt|;
try|try
init|(
name|AsyncConnectionImpl
name|mockedConn
init|=
operator|new
name|AsyncConnectionImpl
argument_list|(
name|CONN
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|CONN
operator|.
name|registry
argument_list|,
name|CONN
operator|.
name|registry
operator|.
name|getClusterId
argument_list|()
operator|.
name|get
argument_list|()
argument_list|,
name|User
operator|.
name|getCurrent
argument_list|()
argument_list|)
block|{
annotation|@
name|Override
name|AsyncRegionLocator
name|getLocator
parameter_list|()
block|{
return|return
name|mockedLocator
return|;
block|}
block|}
init|)
block|{
name|AsyncTable
argument_list|<
name|?
argument_list|>
name|table
init|=
name|mockedConn
operator|.
name|getTableBuilder
argument_list|(
name|TABLE_NAME
argument_list|)
operator|.
name|setRetryPause
argument_list|(
literal|100
argument_list|,
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
operator|.
name|setMaxRetries
argument_list|(
literal|5
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|table
operator|.
name|put
argument_list|(
operator|new
name|Put
argument_list|(
name|ROW
argument_list|)
operator|.
name|addColumn
argument_list|(
name|FAMILY
argument_list|,
name|QUALIFIER
argument_list|,
name|VALUE
argument_list|)
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|assertTrue
argument_list|(
name|errorTriggered
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
name|errorTriggered
operator|.
name|set
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|count
operator|.
name|set
argument_list|(
literal|0
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
name|ROW
argument_list|)
operator|.
name|addColumn
argument_list|(
name|FAMILY
argument_list|,
name|QUALIFIER
argument_list|)
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|assertArrayEquals
argument_list|(
name|VALUE
argument_list|,
name|result
operator|.
name|getValue
argument_list|(
name|FAMILY
argument_list|,
name|QUALIFIER
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|errorTriggered
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

