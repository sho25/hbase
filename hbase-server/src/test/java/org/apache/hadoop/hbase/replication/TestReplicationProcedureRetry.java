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
name|replication
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
name|spy
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
name|master
operator|.
name|HMaster
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
name|replication
operator|.
name|ReplicationPeerManager
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
name|ReplicationTests
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
name|invocation
operator|.
name|InvocationOnMock
import|;
end_import

begin_comment
comment|/**  * All the modification method will fail once in the test and should finally succeed.  */
end_comment

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|ReplicationTests
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
name|TestReplicationProcedureRetry
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
name|TestReplicationProcedureRetry
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|HBaseTestingUtility
name|UTIL
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
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
name|UTIL
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setClass
argument_list|(
name|HConstants
operator|.
name|MASTER_IMPL
argument_list|,
name|MockHMaster
operator|.
name|class
argument_list|,
name|HMaster
operator|.
name|class
argument_list|)
expr_stmt|;
name|UTIL
operator|.
name|startMiniCluster
argument_list|(
literal|3
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
name|Exception
block|{
name|UTIL
operator|.
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
block|}
annotation|@
name|After
specifier|public
name|void
name|tearDownAfterTest
parameter_list|()
throws|throws
name|IOException
block|{
for|for
control|(
name|ReplicationPeerDescription
name|desc
range|:
name|UTIL
operator|.
name|getAdmin
argument_list|()
operator|.
name|listReplicationPeers
argument_list|()
control|)
block|{
name|UTIL
operator|.
name|getAdmin
argument_list|()
operator|.
name|removeReplicationPeer
argument_list|(
name|desc
operator|.
name|getPeerId
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|doTest
parameter_list|()
throws|throws
name|IOException
block|{
name|Admin
name|admin
init|=
name|UTIL
operator|.
name|getAdmin
argument_list|()
decl_stmt|;
name|String
name|peerId
init|=
literal|"1"
decl_stmt|;
name|ReplicationPeerConfig
name|peerConfig
init|=
name|ReplicationPeerConfig
operator|.
name|newBuilder
argument_list|()
operator|.
name|setClusterKey
argument_list|(
literal|"localhost:"
operator|+
name|UTIL
operator|.
name|getZkCluster
argument_list|()
operator|.
name|getClientPort
argument_list|()
operator|+
literal|":/hbase2"
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|admin
operator|.
name|addReplicationPeer
argument_list|(
name|peerId
argument_list|,
name|peerConfig
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|peerConfig
operator|.
name|getClusterKey
argument_list|()
argument_list|,
name|admin
operator|.
name|getReplicationPeerConfig
argument_list|(
name|peerId
argument_list|)
operator|.
name|getClusterKey
argument_list|()
argument_list|)
expr_stmt|;
name|ReplicationPeerConfig
name|newPeerConfig
init|=
name|ReplicationPeerConfig
operator|.
name|newBuilder
argument_list|(
name|peerConfig
argument_list|)
operator|.
name|setBandwidth
argument_list|(
literal|123456
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|admin
operator|.
name|updateReplicationPeerConfig
argument_list|(
name|peerId
argument_list|,
name|newPeerConfig
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|newPeerConfig
operator|.
name|getBandwidth
argument_list|()
argument_list|,
name|admin
operator|.
name|getReplicationPeerConfig
argument_list|(
name|peerId
argument_list|)
operator|.
name|getBandwidth
argument_list|()
argument_list|)
expr_stmt|;
name|admin
operator|.
name|disableReplicationPeer
argument_list|(
name|peerId
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|admin
operator|.
name|listReplicationPeers
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|isEnabled
argument_list|()
argument_list|)
expr_stmt|;
name|admin
operator|.
name|enableReplicationPeer
argument_list|(
name|peerId
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|admin
operator|.
name|listReplicationPeers
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|isEnabled
argument_list|()
argument_list|)
expr_stmt|;
name|admin
operator|.
name|removeReplicationPeer
argument_list|(
name|peerId
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|admin
operator|.
name|listReplicationPeers
argument_list|()
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
comment|// make sure that we have run into the mocked method
name|MockHMaster
name|master
init|=
operator|(
name|MockHMaster
operator|)
name|UTIL
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|getMaster
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
name|master
operator|.
name|addPeerCalled
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|master
operator|.
name|removePeerCalled
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|master
operator|.
name|updatePeerConfigCalled
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|master
operator|.
name|enablePeerCalled
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|master
operator|.
name|disablePeerCalled
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testErrorBeforeUpdate
parameter_list|()
throws|throws
name|IOException
throws|,
name|ReplicationException
block|{
operator|(
operator|(
name|MockHMaster
operator|)
name|UTIL
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|getMaster
argument_list|()
operator|)
operator|.
name|reset
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|doTest
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testErrorAfterUpdate
parameter_list|()
throws|throws
name|IOException
throws|,
name|ReplicationException
block|{
operator|(
operator|(
name|MockHMaster
operator|)
name|UTIL
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|getMaster
argument_list|()
operator|)
operator|.
name|reset
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|doTest
argument_list|()
expr_stmt|;
block|}
specifier|public
specifier|static
specifier|final
class|class
name|MockHMaster
extends|extends
name|HMaster
block|{
specifier|volatile
name|boolean
name|addPeerCalled
decl_stmt|;
specifier|volatile
name|boolean
name|removePeerCalled
decl_stmt|;
specifier|volatile
name|boolean
name|updatePeerConfigCalled
decl_stmt|;
specifier|volatile
name|boolean
name|enablePeerCalled
decl_stmt|;
specifier|volatile
name|boolean
name|disablePeerCalled
decl_stmt|;
specifier|private
name|ReplicationPeerManager
name|manager
decl_stmt|;
specifier|public
name|MockHMaster
parameter_list|(
name|Configuration
name|conf
parameter_list|)
throws|throws
name|IOException
throws|,
name|KeeperException
block|{
name|super
argument_list|(
name|conf
argument_list|)
expr_stmt|;
block|}
specifier|private
name|Object
name|invokeWithError
parameter_list|(
name|InvocationOnMock
name|invocation
parameter_list|,
name|boolean
name|errorBeforeUpdate
parameter_list|)
throws|throws
name|Throwable
block|{
if|if
condition|(
name|errorBeforeUpdate
condition|)
block|{
throw|throw
operator|new
name|ReplicationException
argument_list|(
literal|"mock error before update"
argument_list|)
throw|;
block|}
name|invocation
operator|.
name|callRealMethod
argument_list|()
expr_stmt|;
throw|throw
operator|new
name|ReplicationException
argument_list|(
literal|"mock error after update"
argument_list|)
throw|;
block|}
specifier|public
name|void
name|reset
parameter_list|(
name|boolean
name|errorBeforeUpdate
parameter_list|)
throws|throws
name|ReplicationException
block|{
name|addPeerCalled
operator|=
literal|false
expr_stmt|;
name|removePeerCalled
operator|=
literal|false
expr_stmt|;
name|updatePeerConfigCalled
operator|=
literal|false
expr_stmt|;
name|enablePeerCalled
operator|=
literal|false
expr_stmt|;
name|disablePeerCalled
operator|=
literal|false
expr_stmt|;
name|ReplicationPeerManager
name|m
init|=
name|super
operator|.
name|getReplicationPeerManager
argument_list|()
decl_stmt|;
name|manager
operator|=
name|spy
argument_list|(
name|m
argument_list|)
expr_stmt|;
name|doAnswer
argument_list|(
name|invocation
lambda|->
block|{
if|if
condition|(
operator|!
name|addPeerCalled
condition|)
block|{
name|addPeerCalled
operator|=
literal|true
expr_stmt|;
return|return
name|invokeWithError
argument_list|(
name|invocation
argument_list|,
name|errorBeforeUpdate
argument_list|)
return|;
block|}
else|else
block|{
return|return
name|invocation
operator|.
name|callRealMethod
argument_list|()
return|;
block|}
block|}
argument_list|)
operator|.
name|when
argument_list|(
name|manager
argument_list|)
operator|.
name|addPeer
argument_list|(
name|anyString
argument_list|()
argument_list|,
name|any
argument_list|(
name|ReplicationPeerConfig
operator|.
name|class
argument_list|)
argument_list|,
name|anyBoolean
argument_list|()
argument_list|)
expr_stmt|;
name|doAnswer
argument_list|(
name|invocation
lambda|->
block|{
if|if
condition|(
operator|!
name|removePeerCalled
condition|)
block|{
name|removePeerCalled
operator|=
literal|true
expr_stmt|;
return|return
name|invokeWithError
argument_list|(
name|invocation
argument_list|,
name|errorBeforeUpdate
argument_list|)
return|;
block|}
else|else
block|{
return|return
name|invocation
operator|.
name|callRealMethod
argument_list|()
return|;
block|}
block|}
argument_list|)
operator|.
name|when
argument_list|(
name|manager
argument_list|)
operator|.
name|removePeer
argument_list|(
name|anyString
argument_list|()
argument_list|)
expr_stmt|;
name|doAnswer
argument_list|(
name|invocation
lambda|->
block|{
if|if
condition|(
operator|!
name|updatePeerConfigCalled
condition|)
block|{
name|updatePeerConfigCalled
operator|=
literal|true
expr_stmt|;
return|return
name|invokeWithError
argument_list|(
name|invocation
argument_list|,
name|errorBeforeUpdate
argument_list|)
return|;
block|}
else|else
block|{
return|return
name|invocation
operator|.
name|callRealMethod
argument_list|()
return|;
block|}
block|}
argument_list|)
operator|.
name|when
argument_list|(
name|manager
argument_list|)
operator|.
name|updatePeerConfig
argument_list|(
name|anyString
argument_list|()
argument_list|,
name|any
argument_list|(
name|ReplicationPeerConfig
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|doAnswer
argument_list|(
name|invocation
lambda|->
block|{
if|if
condition|(
operator|!
name|enablePeerCalled
condition|)
block|{
name|enablePeerCalled
operator|=
literal|true
expr_stmt|;
return|return
name|invokeWithError
argument_list|(
name|invocation
argument_list|,
name|errorBeforeUpdate
argument_list|)
return|;
block|}
else|else
block|{
return|return
name|invocation
operator|.
name|callRealMethod
argument_list|()
return|;
block|}
block|}
argument_list|)
operator|.
name|when
argument_list|(
name|manager
argument_list|)
operator|.
name|enablePeer
argument_list|(
name|anyString
argument_list|()
argument_list|)
expr_stmt|;
name|doAnswer
argument_list|(
name|invocation
lambda|->
block|{
if|if
condition|(
operator|!
name|disablePeerCalled
condition|)
block|{
name|disablePeerCalled
operator|=
literal|true
expr_stmt|;
return|return
name|invokeWithError
argument_list|(
name|invocation
argument_list|,
name|errorBeforeUpdate
argument_list|)
return|;
block|}
else|else
block|{
return|return
name|invocation
operator|.
name|callRealMethod
argument_list|()
return|;
block|}
block|}
argument_list|)
operator|.
name|when
argument_list|(
name|manager
argument_list|)
operator|.
name|disablePeer
argument_list|(
name|anyString
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|ReplicationPeerManager
name|getReplicationPeerManager
parameter_list|()
block|{
return|return
name|manager
return|;
block|}
block|}
block|}
end_class

end_unit

