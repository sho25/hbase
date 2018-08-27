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
name|stream
operator|.
name|Stream
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
name|replication
operator|.
name|ReplicationPeerConfig
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
name|replication
operator|.
name|ReplicationPeerStorage
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
name|replication
operator|.
name|ReplicationQueueStorage
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
name|replication
operator|.
name|ReplicationStorageFactory
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
name|replication
operator|.
name|SyncReplicationState
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
name|hadoop
operator|.
name|hbase
operator|.
name|util
operator|.
name|HBaseFsck
operator|.
name|ErrorReporter
operator|.
name|ERROR_CODE
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
name|hbck
operator|.
name|HbckTestingUtil
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
name|TestHBaseFsckReplication
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
name|TestHBaseFsckReplication
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
name|setBoolean
argument_list|(
literal|"hbase.write.hbck1.lock.file"
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|UTIL
operator|.
name|startMiniCluster
argument_list|(
literal|1
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
name|Test
specifier|public
name|void
name|test
parameter_list|()
throws|throws
name|Exception
block|{
name|ReplicationPeerStorage
name|peerStorage
init|=
name|ReplicationStorageFactory
operator|.
name|getReplicationPeerStorage
argument_list|(
name|UTIL
operator|.
name|getZooKeeperWatcher
argument_list|()
argument_list|,
name|UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
name|ReplicationQueueStorage
name|queueStorage
init|=
name|ReplicationStorageFactory
operator|.
name|getReplicationQueueStorage
argument_list|(
name|UTIL
operator|.
name|getZooKeeperWatcher
argument_list|()
argument_list|,
name|UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
name|String
name|peerId1
init|=
literal|"1"
decl_stmt|;
name|String
name|peerId2
init|=
literal|"2"
decl_stmt|;
name|peerStorage
operator|.
name|addPeer
argument_list|(
name|peerId1
argument_list|,
name|ReplicationPeerConfig
operator|.
name|newBuilder
argument_list|()
operator|.
name|setClusterKey
argument_list|(
literal|"key"
argument_list|)
operator|.
name|build
argument_list|()
argument_list|,
literal|true
argument_list|,
name|SyncReplicationState
operator|.
name|NONE
argument_list|)
expr_stmt|;
name|peerStorage
operator|.
name|addPeer
argument_list|(
name|peerId2
argument_list|,
name|ReplicationPeerConfig
operator|.
name|newBuilder
argument_list|()
operator|.
name|setClusterKey
argument_list|(
literal|"key"
argument_list|)
operator|.
name|build
argument_list|()
argument_list|,
literal|true
argument_list|,
name|SyncReplicationState
operator|.
name|NONE
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
literal|10
condition|;
name|i
operator|++
control|)
block|{
name|queueStorage
operator|.
name|addWAL
argument_list|(
name|ServerName
operator|.
name|valueOf
argument_list|(
literal|"localhost"
argument_list|,
literal|10000
operator|+
name|i
argument_list|,
literal|100000
operator|+
name|i
argument_list|)
argument_list|,
name|peerId1
argument_list|,
literal|"file-"
operator|+
name|i
argument_list|)
expr_stmt|;
block|}
name|queueStorage
operator|.
name|addWAL
argument_list|(
name|ServerName
operator|.
name|valueOf
argument_list|(
literal|"localhost"
argument_list|,
literal|10000
argument_list|,
literal|100000
argument_list|)
argument_list|,
name|peerId2
argument_list|,
literal|"file"
argument_list|)
expr_stmt|;
name|HBaseFsck
name|fsck
init|=
name|HbckTestingUtil
operator|.
name|doFsck
argument_list|(
name|UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
literal|true
argument_list|)
decl_stmt|;
name|HbckTestingUtil
operator|.
name|assertNoErrors
argument_list|(
name|fsck
argument_list|)
expr_stmt|;
comment|// should not remove anything since the replication peer is still alive
name|assertEquals
argument_list|(
literal|10
argument_list|,
name|queueStorage
operator|.
name|getListOfReplicators
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|peerStorage
operator|.
name|removePeer
argument_list|(
name|peerId1
argument_list|)
expr_stmt|;
comment|// there should be orphan queues
name|assertEquals
argument_list|(
literal|10
argument_list|,
name|queueStorage
operator|.
name|getListOfReplicators
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|fsck
operator|=
name|HbckTestingUtil
operator|.
name|doFsck
argument_list|(
name|UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|HbckTestingUtil
operator|.
name|assertErrors
argument_list|(
name|fsck
argument_list|,
name|Stream
operator|.
name|generate
argument_list|(
parameter_list|()
lambda|->
block|{
return|return
name|ERROR_CODE
operator|.
name|UNDELETED_REPLICATION_QUEUE
return|;
block|}
argument_list|)
operator|.
name|limit
argument_list|(
literal|10
argument_list|)
operator|.
name|toArray
argument_list|(
name|ERROR_CODE
index|[]
operator|::
operator|new
argument_list|)
argument_list|)
expr_stmt|;
comment|// should not delete anything when fix is false
name|assertEquals
argument_list|(
literal|10
argument_list|,
name|queueStorage
operator|.
name|getListOfReplicators
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|fsck
operator|=
name|HbckTestingUtil
operator|.
name|doFsck
argument_list|(
name|UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|HbckTestingUtil
operator|.
name|assertErrors
argument_list|(
name|fsck
argument_list|,
name|Stream
operator|.
name|generate
argument_list|(
parameter_list|()
lambda|->
block|{
return|return
name|ERROR_CODE
operator|.
name|UNDELETED_REPLICATION_QUEUE
return|;
block|}
argument_list|)
operator|.
name|limit
argument_list|(
literal|10
argument_list|)
operator|.
name|toArray
argument_list|(
name|ERROR_CODE
index|[]
operator|::
operator|new
argument_list|)
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|ServerName
argument_list|>
name|replicators
init|=
name|queueStorage
operator|.
name|getListOfReplicators
argument_list|()
decl_stmt|;
comment|// should not remove the server with queue for peerId2
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|replicators
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|ServerName
operator|.
name|valueOf
argument_list|(
literal|"localhost"
argument_list|,
literal|10000
argument_list|,
literal|100000
argument_list|)
argument_list|,
name|replicators
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
for|for
control|(
name|String
name|queueId
range|:
name|queueStorage
operator|.
name|getAllQueues
argument_list|(
name|replicators
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
control|)
block|{
name|assertEquals
argument_list|(
name|peerId2
argument_list|,
name|queueId
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

