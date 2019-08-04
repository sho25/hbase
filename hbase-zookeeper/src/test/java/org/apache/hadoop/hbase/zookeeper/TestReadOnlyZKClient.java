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
name|zookeeper
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
name|assertNotEquals
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
name|assertNull
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
name|assertSame
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
name|fail
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|mockito
operator|.
name|ArgumentMatchers
operator|.
name|any
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|mockito
operator|.
name|ArgumentMatchers
operator|.
name|anyBoolean
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|mockito
operator|.
name|ArgumentMatchers
operator|.
name|anyString
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|mockito
operator|.
name|Mockito
operator|.
name|doAnswer
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|mockito
operator|.
name|Mockito
operator|.
name|mock
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|mockito
operator|.
name|Mockito
operator|.
name|never
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|mockito
operator|.
name|Mockito
operator|.
name|times
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|mockito
operator|.
name|Mockito
operator|.
name|verify
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|mockito
operator|.
name|Mockito
operator|.
name|when
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
name|Exchanger
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
name|ThreadLocalRandom
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
name|HBaseZKTestingUtility
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
name|Waiter
operator|.
name|ExplainingPredicate
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
name|testclassification
operator|.
name|ZKTests
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|zookeeper
operator|.
name|AsyncCallback
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|zookeeper
operator|.
name|CreateMode
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|zookeeper
operator|.
name|KeeperException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|zookeeper
operator|.
name|KeeperException
operator|.
name|Code
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|zookeeper
operator|.
name|ZooDefs
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|zookeeper
operator|.
name|ZooKeeper
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
name|ZKTests
operator|.
name|class
block|,
name|MediumTests
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|TestReadOnlyZKClient
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
name|TestReadOnlyZKClient
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|HBaseZKTestingUtility
name|UTIL
init|=
operator|new
name|HBaseZKTestingUtility
argument_list|()
decl_stmt|;
specifier|private
specifier|static
name|String
name|PATH
init|=
literal|"/test"
decl_stmt|;
specifier|private
specifier|static
name|byte
index|[]
name|DATA
decl_stmt|;
specifier|private
specifier|static
name|int
name|CHILDREN
init|=
literal|5
decl_stmt|;
specifier|private
specifier|static
name|ReadOnlyZKClient
name|RO_ZK
decl_stmt|;
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|setUp
parameter_list|()
throws|throws
name|Exception
block|{
specifier|final
name|int
name|port
init|=
name|UTIL
operator|.
name|startMiniZKCluster
argument_list|()
operator|.
name|getClientPort
argument_list|()
decl_stmt|;
name|ZooKeeper
name|zk
init|=
name|ZooKeeperHelper
operator|.
name|getConnectedZooKeeper
argument_list|(
literal|"localhost:"
operator|+
name|port
argument_list|,
literal|10000
argument_list|)
decl_stmt|;
name|DATA
operator|=
operator|new
name|byte
index|[
literal|10
index|]
expr_stmt|;
name|ThreadLocalRandom
operator|.
name|current
argument_list|()
operator|.
name|nextBytes
argument_list|(
name|DATA
argument_list|)
expr_stmt|;
name|zk
operator|.
name|create
argument_list|(
name|PATH
argument_list|,
name|DATA
argument_list|,
name|ZooDefs
operator|.
name|Ids
operator|.
name|OPEN_ACL_UNSAFE
argument_list|,
name|CreateMode
operator|.
name|PERSISTENT
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
name|CHILDREN
condition|;
name|i
operator|++
control|)
block|{
name|zk
operator|.
name|create
argument_list|(
name|PATH
operator|+
literal|"/c"
operator|+
name|i
argument_list|,
operator|new
name|byte
index|[
literal|0
index|]
argument_list|,
name|ZooDefs
operator|.
name|Ids
operator|.
name|OPEN_ACL_UNSAFE
argument_list|,
name|CreateMode
operator|.
name|PERSISTENT
argument_list|)
expr_stmt|;
block|}
name|zk
operator|.
name|close
argument_list|()
expr_stmt|;
name|Configuration
name|conf
init|=
name|UTIL
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
name|conf
operator|.
name|set
argument_list|(
name|HConstants
operator|.
name|ZOOKEEPER_QUORUM
argument_list|,
literal|"localhost:"
operator|+
name|port
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setInt
argument_list|(
name|ReadOnlyZKClient
operator|.
name|RECOVERY_RETRY
argument_list|,
literal|3
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setInt
argument_list|(
name|ReadOnlyZKClient
operator|.
name|RECOVERY_RETRY_INTERVAL_MILLIS
argument_list|,
literal|100
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setInt
argument_list|(
name|ReadOnlyZKClient
operator|.
name|KEEPALIVE_MILLIS
argument_list|,
literal|3000
argument_list|)
expr_stmt|;
name|RO_ZK
operator|=
operator|new
name|ReadOnlyZKClient
argument_list|(
name|conf
argument_list|)
expr_stmt|;
comment|// only connect when necessary
name|assertNull
argument_list|(
name|RO_ZK
operator|.
name|zookeeper
argument_list|)
expr_stmt|;
block|}
annotation|@
name|AfterClass
specifier|public
specifier|static
name|void
name|tearDown
parameter_list|()
throws|throws
name|IOException
block|{
name|RO_ZK
operator|.
name|close
argument_list|()
expr_stmt|;
name|UTIL
operator|.
name|shutdownMiniZKCluster
argument_list|()
expr_stmt|;
name|UTIL
operator|.
name|cleanupTestDir
argument_list|()
expr_stmt|;
block|}
specifier|private
name|void
name|waitForIdleConnectionClosed
parameter_list|()
throws|throws
name|Exception
block|{
comment|// The zookeeper client should be closed finally after the keep alive time elapsed
name|UTIL
operator|.
name|waitFor
argument_list|(
literal|10000
argument_list|,
operator|new
name|ExplainingPredicate
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
block|{
return|return
name|RO_ZK
operator|.
name|zookeeper
operator|==
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|explainFailure
parameter_list|()
block|{
return|return
literal|"Connection to zookeeper is still alive"
return|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testRead
parameter_list|()
throws|throws
name|Exception
block|{
name|assertArrayEquals
argument_list|(
name|DATA
argument_list|,
name|RO_ZK
operator|.
name|get
argument_list|(
name|PATH
argument_list|)
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|CHILDREN
argument_list|,
name|RO_ZK
operator|.
name|exists
argument_list|(
name|PATH
argument_list|)
operator|.
name|get
argument_list|()
operator|.
name|getNumChildren
argument_list|()
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|children
init|=
name|RO_ZK
operator|.
name|list
argument_list|(
name|PATH
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
name|CHILDREN
argument_list|,
name|children
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|Collections
operator|.
name|sort
argument_list|(
name|children
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
name|CHILDREN
condition|;
name|i
operator|++
control|)
block|{
name|assertEquals
argument_list|(
literal|"c"
operator|+
name|i
argument_list|,
name|children
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|assertNotNull
argument_list|(
name|RO_ZK
operator|.
name|zookeeper
argument_list|)
expr_stmt|;
name|waitForIdleConnectionClosed
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testNoNode
parameter_list|()
throws|throws
name|InterruptedException
throws|,
name|ExecutionException
block|{
name|String
name|pathNotExists
init|=
name|PATH
operator|+
literal|"_whatever"
decl_stmt|;
try|try
block|{
name|RO_ZK
operator|.
name|get
argument_list|(
name|pathNotExists
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|fail
argument_list|(
literal|"should fail because of "
operator|+
name|pathNotExists
operator|+
literal|" does not exist"
argument_list|)
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
name|KeeperException
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|KeeperException
name|ke
init|=
operator|(
name|KeeperException
operator|)
name|e
operator|.
name|getCause
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
name|Code
operator|.
name|NONODE
argument_list|,
name|ke
operator|.
name|code
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|pathNotExists
argument_list|,
name|ke
operator|.
name|getPath
argument_list|()
argument_list|)
expr_stmt|;
block|}
try|try
block|{
name|RO_ZK
operator|.
name|list
argument_list|(
name|pathNotExists
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|fail
argument_list|(
literal|"should fail because of "
operator|+
name|pathNotExists
operator|+
literal|" does not exist"
argument_list|)
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
name|KeeperException
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|KeeperException
name|ke
init|=
operator|(
name|KeeperException
operator|)
name|e
operator|.
name|getCause
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
name|Code
operator|.
name|NONODE
argument_list|,
name|ke
operator|.
name|code
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|pathNotExists
argument_list|,
name|ke
operator|.
name|getPath
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|// exists will not throw exception.
name|assertNull
argument_list|(
name|RO_ZK
operator|.
name|exists
argument_list|(
name|pathNotExists
argument_list|)
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testSessionExpire
parameter_list|()
throws|throws
name|Exception
block|{
name|assertArrayEquals
argument_list|(
name|DATA
argument_list|,
name|RO_ZK
operator|.
name|get
argument_list|(
name|PATH
argument_list|)
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
name|ZooKeeper
name|zk
init|=
name|RO_ZK
operator|.
name|zookeeper
decl_stmt|;
name|long
name|sessionId
init|=
name|zk
operator|.
name|getSessionId
argument_list|()
decl_stmt|;
name|UTIL
operator|.
name|getZkCluster
argument_list|()
operator|.
name|getZooKeeperServers
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|closeSession
argument_list|(
name|sessionId
argument_list|)
expr_stmt|;
comment|// should not reach keep alive so still the same instance
name|assertSame
argument_list|(
name|zk
argument_list|,
name|RO_ZK
operator|.
name|zookeeper
argument_list|)
expr_stmt|;
name|byte
index|[]
name|got
init|=
name|RO_ZK
operator|.
name|get
argument_list|(
name|PATH
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|assertArrayEquals
argument_list|(
name|DATA
argument_list|,
name|got
argument_list|)
expr_stmt|;
name|assertNotNull
argument_list|(
name|RO_ZK
operator|.
name|zookeeper
argument_list|)
expr_stmt|;
name|assertNotSame
argument_list|(
name|zk
argument_list|,
name|RO_ZK
operator|.
name|zookeeper
argument_list|)
expr_stmt|;
name|assertNotEquals
argument_list|(
name|sessionId
argument_list|,
name|RO_ZK
operator|.
name|zookeeper
operator|.
name|getSessionId
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testNotCloseZkWhenPending
parameter_list|()
throws|throws
name|Exception
block|{
name|ZooKeeper
name|mockedZK
init|=
name|mock
argument_list|(
name|ZooKeeper
operator|.
name|class
argument_list|)
decl_stmt|;
name|Exchanger
argument_list|<
name|AsyncCallback
operator|.
name|DataCallback
argument_list|>
name|exchanger
init|=
operator|new
name|Exchanger
argument_list|<>
argument_list|()
decl_stmt|;
name|doAnswer
argument_list|(
name|i
lambda|->
block|{
name|exchanger
operator|.
name|exchange
argument_list|(
name|i
operator|.
name|getArgument
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
argument_list|)
operator|.
name|when
argument_list|(
name|mockedZK
argument_list|)
operator|.
name|getData
argument_list|(
name|anyString
argument_list|()
argument_list|,
name|anyBoolean
argument_list|()
argument_list|,
name|any
argument_list|(
name|AsyncCallback
operator|.
name|DataCallback
operator|.
name|class
argument_list|)
argument_list|,
name|any
argument_list|()
argument_list|)
expr_stmt|;
name|doAnswer
argument_list|(
name|i
lambda|->
literal|null
argument_list|)
operator|.
name|when
argument_list|(
name|mockedZK
argument_list|)
operator|.
name|close
argument_list|()
expr_stmt|;
name|when
argument_list|(
name|mockedZK
operator|.
name|getState
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|ZooKeeper
operator|.
name|States
operator|.
name|CONNECTED
argument_list|)
expr_stmt|;
name|RO_ZK
operator|.
name|zookeeper
operator|=
name|mockedZK
expr_stmt|;
name|CompletableFuture
argument_list|<
name|byte
index|[]
argument_list|>
name|future
init|=
name|RO_ZK
operator|.
name|get
argument_list|(
name|PATH
argument_list|)
decl_stmt|;
name|AsyncCallback
operator|.
name|DataCallback
name|callback
init|=
name|exchanger
operator|.
name|exchange
argument_list|(
literal|null
argument_list|)
decl_stmt|;
comment|// 2 * keep alive time to ensure that we will not close the zk when there are pending requests
name|Thread
operator|.
name|sleep
argument_list|(
literal|6000
argument_list|)
expr_stmt|;
name|assertNotNull
argument_list|(
name|RO_ZK
operator|.
name|zookeeper
argument_list|)
expr_stmt|;
name|verify
argument_list|(
name|mockedZK
argument_list|,
name|never
argument_list|()
argument_list|)
operator|.
name|close
argument_list|()
expr_stmt|;
name|callback
operator|.
name|processResult
argument_list|(
name|Code
operator|.
name|OK
operator|.
name|intValue
argument_list|()
argument_list|,
name|PATH
argument_list|,
literal|null
argument_list|,
name|DATA
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|assertArrayEquals
argument_list|(
name|DATA
argument_list|,
name|future
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
comment|// now we will close the idle connection.
name|waitForIdleConnectionClosed
argument_list|()
expr_stmt|;
name|verify
argument_list|(
name|mockedZK
argument_list|,
name|times
argument_list|(
literal|1
argument_list|)
argument_list|)
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

