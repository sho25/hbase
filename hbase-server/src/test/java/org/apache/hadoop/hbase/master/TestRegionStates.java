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
name|master
package|;
end_package

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
name|HRegionInfo
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
name|Server
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
name|client
operator|.
name|ClusterConnection
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
name|master
operator|.
name|RegionState
operator|.
name|State
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
name|MasterTests
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
name|SmallTests
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
name|Map
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|UUID
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
name|BrokenBarrierException
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
name|CyclicBarrier
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
name|mockito
operator|.
name|invocation
operator|.
name|InvocationOnMock
import|;
end_import

begin_import
import|import
name|org
operator|.
name|mockito
operator|.
name|stubbing
operator|.
name|Answer
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
name|junit
operator|.
name|framework
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
name|mockito
operator|.
name|Matchers
operator|.
name|isA
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
name|when
import|;
end_import

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|MasterTests
operator|.
name|class
block|,
name|SmallTests
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|TestRegionStates
block|{
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|10000
argument_list|)
specifier|public
name|void
name|testCanMakeProgressThoughMetaIsDown
parameter_list|()
throws|throws
name|IOException
throws|,
name|InterruptedException
throws|,
name|BrokenBarrierException
block|{
name|Server
name|server
init|=
name|mock
argument_list|(
name|Server
operator|.
name|class
argument_list|)
decl_stmt|;
name|when
argument_list|(
name|server
operator|.
name|getServerName
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|ServerName
operator|.
name|valueOf
argument_list|(
literal|"master,1,1"
argument_list|)
argument_list|)
expr_stmt|;
name|Connection
name|connection
init|=
name|mock
argument_list|(
name|ClusterConnection
operator|.
name|class
argument_list|)
decl_stmt|;
comment|// Set up a table that gets 'stuck' when we try to fetch a row from the meta table.
comment|// It is stuck on a CyclicBarrier latch. We use CyclicBarrier because it will tell us when
comment|// thread is waiting on latch.
name|Table
name|metaTable
init|=
name|Mockito
operator|.
name|mock
argument_list|(
name|Table
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|final
name|CyclicBarrier
name|latch
init|=
operator|new
name|CyclicBarrier
argument_list|(
literal|2
argument_list|)
decl_stmt|;
name|when
argument_list|(
name|metaTable
operator|.
name|get
argument_list|(
operator|(
name|Get
operator|)
name|Mockito
operator|.
name|any
argument_list|()
argument_list|)
argument_list|)
operator|.
name|thenAnswer
argument_list|(
operator|new
name|Answer
argument_list|<
name|Result
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Result
name|answer
parameter_list|(
name|InvocationOnMock
name|invocation
parameter_list|)
throws|throws
name|Throwable
block|{
name|latch
operator|.
name|await
argument_list|()
expr_stmt|;
throw|throw
operator|new
name|java
operator|.
name|net
operator|.
name|ConnectException
argument_list|(
literal|"Connection refused"
argument_list|)
throw|;
block|}
block|}
argument_list|)
expr_stmt|;
name|when
argument_list|(
name|connection
operator|.
name|getTable
argument_list|(
name|TableName
operator|.
name|META_TABLE_NAME
argument_list|)
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|metaTable
argument_list|)
expr_stmt|;
name|when
argument_list|(
name|server
operator|.
name|getConnection
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
operator|(
name|ClusterConnection
operator|)
name|connection
argument_list|)
expr_stmt|;
name|Configuration
name|configuration
init|=
name|mock
argument_list|(
name|Configuration
operator|.
name|class
argument_list|)
decl_stmt|;
name|when
argument_list|(
name|server
operator|.
name|getConfiguration
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|configuration
argument_list|)
expr_stmt|;
name|TableStateManager
name|tsm
init|=
name|mock
argument_list|(
name|TableStateManager
operator|.
name|class
argument_list|)
decl_stmt|;
name|ServerManager
name|sm
init|=
name|mock
argument_list|(
name|ServerManager
operator|.
name|class
argument_list|)
decl_stmt|;
name|when
argument_list|(
name|sm
operator|.
name|isServerOnline
argument_list|(
name|isA
argument_list|(
name|ServerName
operator|.
name|class
argument_list|)
argument_list|)
argument_list|)
operator|.
name|thenReturn
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|RegionStateStore
name|rss
init|=
name|mock
argument_list|(
name|RegionStateStore
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|final
name|RegionStates
name|regionStates
init|=
operator|new
name|RegionStates
argument_list|(
name|server
argument_list|,
name|tsm
argument_list|,
name|sm
argument_list|,
name|rss
argument_list|)
decl_stmt|;
specifier|final
name|ServerName
name|sn
init|=
name|mockServer
argument_list|(
literal|"one"
argument_list|,
literal|1
argument_list|)
decl_stmt|;
name|regionStates
operator|.
name|updateRegionState
argument_list|(
name|HRegionInfo
operator|.
name|FIRST_META_REGIONINFO
argument_list|,
name|State
operator|.
name|SPLITTING_NEW
argument_list|,
name|sn
argument_list|)
expr_stmt|;
name|Thread
name|backgroundThread
init|=
operator|new
name|Thread
argument_list|(
literal|"Get stuck setting server offline"
argument_list|)
block|{
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
name|regionStates
operator|.
name|serverOffline
argument_list|(
name|sn
argument_list|)
expr_stmt|;
block|}
block|}
decl_stmt|;
name|assertTrue
argument_list|(
name|latch
operator|.
name|getNumberWaiting
argument_list|()
operator|==
literal|0
argument_list|)
expr_stmt|;
name|backgroundThread
operator|.
name|start
argument_list|()
expr_stmt|;
while|while
condition|(
name|latch
operator|.
name|getNumberWaiting
argument_list|()
operator|==
literal|0
condition|)
empty_stmt|;
comment|// Verify I can do stuff with synchronized RegionStates methods, that I am not locked out.
comment|// Below is a call that is synchronized.  Can I do it and not block?
name|regionStates
operator|.
name|getRegionServerOfRegion
argument_list|(
name|HRegionInfo
operator|.
name|FIRST_META_REGIONINFO
argument_list|)
expr_stmt|;
comment|// Done. Trip the barrier on the background thread.
name|latch
operator|.
name|await
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testWeDontReturnDrainingServersForOurBalancePlans
parameter_list|()
throws|throws
name|Exception
block|{
name|Server
name|server
init|=
name|mock
argument_list|(
name|Server
operator|.
name|class
argument_list|)
decl_stmt|;
name|when
argument_list|(
name|server
operator|.
name|getServerName
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|ServerName
operator|.
name|valueOf
argument_list|(
literal|"master,1,1"
argument_list|)
argument_list|)
expr_stmt|;
name|Configuration
name|configuration
init|=
name|mock
argument_list|(
name|Configuration
operator|.
name|class
argument_list|)
decl_stmt|;
name|when
argument_list|(
name|server
operator|.
name|getConfiguration
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|configuration
argument_list|)
expr_stmt|;
name|TableStateManager
name|tsm
init|=
name|mock
argument_list|(
name|TableStateManager
operator|.
name|class
argument_list|)
decl_stmt|;
name|ServerManager
name|sm
init|=
name|mock
argument_list|(
name|ServerManager
operator|.
name|class
argument_list|)
decl_stmt|;
name|when
argument_list|(
name|sm
operator|.
name|isServerOnline
argument_list|(
name|isA
argument_list|(
name|ServerName
operator|.
name|class
argument_list|)
argument_list|)
argument_list|)
operator|.
name|thenReturn
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|RegionStateStore
name|rss
init|=
name|mock
argument_list|(
name|RegionStateStore
operator|.
name|class
argument_list|)
decl_stmt|;
name|RegionStates
name|regionStates
init|=
operator|new
name|RegionStates
argument_list|(
name|server
argument_list|,
name|tsm
argument_list|,
name|sm
argument_list|,
name|rss
argument_list|)
decl_stmt|;
name|ServerName
name|one
init|=
name|mockServer
argument_list|(
literal|"one"
argument_list|,
literal|1
argument_list|)
decl_stmt|;
name|ServerName
name|two
init|=
name|mockServer
argument_list|(
literal|"two"
argument_list|,
literal|1
argument_list|)
decl_stmt|;
name|ServerName
name|three
init|=
name|mockServer
argument_list|(
literal|"three"
argument_list|,
literal|1
argument_list|)
decl_stmt|;
name|when
argument_list|(
name|sm
operator|.
name|getDrainingServersList
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|three
argument_list|)
argument_list|)
expr_stmt|;
name|regionStates
operator|.
name|regionOnline
argument_list|(
name|createFakeRegion
argument_list|()
argument_list|,
name|one
argument_list|)
expr_stmt|;
name|regionStates
operator|.
name|regionOnline
argument_list|(
name|createFakeRegion
argument_list|()
argument_list|,
name|two
argument_list|)
expr_stmt|;
name|regionStates
operator|.
name|regionOnline
argument_list|(
name|createFakeRegion
argument_list|()
argument_list|,
name|three
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|TableName
argument_list|,
name|Map
argument_list|<
name|ServerName
argument_list|,
name|List
argument_list|<
name|HRegionInfo
argument_list|>
argument_list|>
argument_list|>
name|result
init|=
name|regionStates
operator|.
name|getAssignmentsByTable
argument_list|()
decl_stmt|;
for|for
control|(
name|Map
argument_list|<
name|ServerName
argument_list|,
name|List
argument_list|<
name|HRegionInfo
argument_list|>
argument_list|>
name|map
range|:
name|result
operator|.
name|values
argument_list|()
control|)
block|{
name|assertFalse
argument_list|(
name|map
operator|.
name|keySet
argument_list|()
operator|.
name|contains
argument_list|(
name|three
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|HRegionInfo
name|createFakeRegion
parameter_list|()
block|{
name|HRegionInfo
name|info
init|=
name|mock
argument_list|(
name|HRegionInfo
operator|.
name|class
argument_list|)
decl_stmt|;
name|when
argument_list|(
name|info
operator|.
name|getEncodedName
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|UUID
operator|.
name|randomUUID
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|info
return|;
block|}
specifier|private
name|ServerName
name|mockServer
parameter_list|(
name|String
name|fakeHost
parameter_list|,
name|int
name|fakePort
parameter_list|)
block|{
name|ServerName
name|serverName
init|=
name|mock
argument_list|(
name|ServerName
operator|.
name|class
argument_list|)
decl_stmt|;
name|when
argument_list|(
name|serverName
operator|.
name|getHostname
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|fakeHost
argument_list|)
expr_stmt|;
name|when
argument_list|(
name|serverName
operator|.
name|getPort
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|fakePort
argument_list|)
expr_stmt|;
return|return
name|serverName
return|;
block|}
block|}
end_class

end_unit

