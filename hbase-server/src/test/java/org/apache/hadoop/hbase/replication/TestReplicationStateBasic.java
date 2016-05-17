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
name|*
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
name|SortedMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|SortedSet
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
name|zookeeper
operator|.
name|ZKConfig
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

begin_comment
comment|/**  * White box testing for replication state interfaces. Implementations should extend this class, and  * initialize the interfaces properly.  */
end_comment

begin_class
specifier|public
specifier|abstract
class|class
name|TestReplicationStateBasic
block|{
specifier|protected
name|ReplicationQueues
name|rq1
decl_stmt|;
specifier|protected
name|ReplicationQueues
name|rq2
decl_stmt|;
specifier|protected
name|ReplicationQueues
name|rq3
decl_stmt|;
specifier|protected
name|ReplicationQueuesClient
name|rqc
decl_stmt|;
specifier|protected
name|String
name|server1
init|=
name|ServerName
operator|.
name|valueOf
argument_list|(
literal|"hostname1.example.org"
argument_list|,
literal|1234
argument_list|,
operator|-
literal|1L
argument_list|)
operator|.
name|toString
argument_list|()
decl_stmt|;
specifier|protected
name|String
name|server2
init|=
name|ServerName
operator|.
name|valueOf
argument_list|(
literal|"hostname2.example.org"
argument_list|,
literal|1234
argument_list|,
operator|-
literal|1L
argument_list|)
operator|.
name|toString
argument_list|()
decl_stmt|;
specifier|protected
name|String
name|server3
init|=
name|ServerName
operator|.
name|valueOf
argument_list|(
literal|"hostname3.example.org"
argument_list|,
literal|1234
argument_list|,
operator|-
literal|1L
argument_list|)
operator|.
name|toString
argument_list|()
decl_stmt|;
specifier|protected
name|ReplicationPeers
name|rp
decl_stmt|;
specifier|protected
specifier|static
specifier|final
name|String
name|ID_ONE
init|=
literal|"1"
decl_stmt|;
specifier|protected
specifier|static
specifier|final
name|String
name|ID_TWO
init|=
literal|"2"
decl_stmt|;
specifier|protected
specifier|static
name|String
name|KEY_ONE
decl_stmt|;
specifier|protected
specifier|static
name|String
name|KEY_TWO
decl_stmt|;
comment|// For testing when we try to replicate to ourself
specifier|protected
name|String
name|OUR_ID
init|=
literal|"3"
decl_stmt|;
specifier|protected
name|String
name|OUR_KEY
decl_stmt|;
specifier|protected
specifier|static
name|int
name|zkTimeoutCount
decl_stmt|;
specifier|protected
specifier|static
specifier|final
name|int
name|ZK_MAX_COUNT
init|=
literal|300
decl_stmt|;
specifier|protected
specifier|static
specifier|final
name|int
name|ZK_SLEEP_INTERVAL
init|=
literal|100
decl_stmt|;
comment|// millis
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
name|TestReplicationStateBasic
operator|.
name|class
argument_list|)
decl_stmt|;
annotation|@
name|Before
specifier|public
name|void
name|setUp
parameter_list|()
block|{
name|zkTimeoutCount
operator|=
literal|0
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testReplicationQueuesClient
parameter_list|()
throws|throws
name|ReplicationException
throws|,
name|KeeperException
block|{
name|rqc
operator|.
name|init
argument_list|()
expr_stmt|;
comment|// Test methods with empty state
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|rqc
operator|.
name|getListOfReplicators
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|rqc
operator|.
name|getLogsInQueue
argument_list|(
name|server1
argument_list|,
literal|"qId1"
argument_list|)
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|rqc
operator|.
name|getAllQueues
argument_list|(
name|server1
argument_list|)
argument_list|)
expr_stmt|;
comment|/*      * Set up data Two replicators: -- server1: three queues with 0, 1 and 2 log files each --      * server2: zero queues      */
name|rq1
operator|.
name|init
argument_list|(
name|server1
argument_list|)
expr_stmt|;
name|rq2
operator|.
name|init
argument_list|(
name|server2
argument_list|)
expr_stmt|;
name|rq1
operator|.
name|addLog
argument_list|(
literal|"qId1"
argument_list|,
literal|"trash"
argument_list|)
expr_stmt|;
name|rq1
operator|.
name|removeLog
argument_list|(
literal|"qId1"
argument_list|,
literal|"trash"
argument_list|)
expr_stmt|;
name|rq1
operator|.
name|addLog
argument_list|(
literal|"qId2"
argument_list|,
literal|"filename1"
argument_list|)
expr_stmt|;
name|rq1
operator|.
name|addLog
argument_list|(
literal|"qId3"
argument_list|,
literal|"filename2"
argument_list|)
expr_stmt|;
name|rq1
operator|.
name|addLog
argument_list|(
literal|"qId3"
argument_list|,
literal|"filename3"
argument_list|)
expr_stmt|;
name|rq2
operator|.
name|addLog
argument_list|(
literal|"trash"
argument_list|,
literal|"trash"
argument_list|)
expr_stmt|;
name|rq2
operator|.
name|removeQueue
argument_list|(
literal|"trash"
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|reps
init|=
name|rqc
operator|.
name|getListOfReplicators
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|reps
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|server1
argument_list|,
name|reps
operator|.
name|contains
argument_list|(
name|server1
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|server2
argument_list|,
name|reps
operator|.
name|contains
argument_list|(
name|server2
argument_list|)
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|rqc
operator|.
name|getLogsInQueue
argument_list|(
literal|"bogus"
argument_list|,
literal|"bogus"
argument_list|)
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|rqc
operator|.
name|getLogsInQueue
argument_list|(
name|server1
argument_list|,
literal|"bogus"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|rqc
operator|.
name|getLogsInQueue
argument_list|(
name|server1
argument_list|,
literal|"qId1"
argument_list|)
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|rqc
operator|.
name|getLogsInQueue
argument_list|(
name|server1
argument_list|,
literal|"qId2"
argument_list|)
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"filename1"
argument_list|,
name|rqc
operator|.
name|getLogsInQueue
argument_list|(
name|server1
argument_list|,
literal|"qId2"
argument_list|)
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|rqc
operator|.
name|getAllQueues
argument_list|(
literal|"bogus"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|rqc
operator|.
name|getAllQueues
argument_list|(
name|server2
argument_list|)
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|list
init|=
name|rqc
operator|.
name|getAllQueues
argument_list|(
name|server1
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|3
argument_list|,
name|list
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|list
operator|.
name|contains
argument_list|(
literal|"qId2"
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|list
operator|.
name|contains
argument_list|(
literal|"qId3"
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testReplicationQueues
parameter_list|()
throws|throws
name|ReplicationException
block|{
name|rq1
operator|.
name|init
argument_list|(
name|server1
argument_list|)
expr_stmt|;
name|rq2
operator|.
name|init
argument_list|(
name|server2
argument_list|)
expr_stmt|;
name|rq3
operator|.
name|init
argument_list|(
name|server3
argument_list|)
expr_stmt|;
comment|//Initialize ReplicationPeer so we can add peers (we don't transfer lone queues)
name|rp
operator|.
name|init
argument_list|()
expr_stmt|;
comment|// 3 replicators should exist
name|assertEquals
argument_list|(
literal|3
argument_list|,
name|rq1
operator|.
name|getListOfReplicators
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|rq1
operator|.
name|removeQueue
argument_list|(
literal|"bogus"
argument_list|)
expr_stmt|;
name|rq1
operator|.
name|removeLog
argument_list|(
literal|"bogus"
argument_list|,
literal|"bogus"
argument_list|)
expr_stmt|;
name|rq1
operator|.
name|removeAllQueues
argument_list|()
expr_stmt|;
name|assertNull
argument_list|(
name|rq1
operator|.
name|getAllQueues
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|rq1
operator|.
name|getLogPosition
argument_list|(
literal|"bogus"
argument_list|,
literal|"bogus"
argument_list|)
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|rq1
operator|.
name|getLogsInQueue
argument_list|(
literal|"bogus"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|rq1
operator|.
name|claimQueues
argument_list|(
name|ServerName
operator|.
name|valueOf
argument_list|(
literal|"bogus"
argument_list|,
literal|1234
argument_list|,
operator|-
literal|1L
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|)
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|rq1
operator|.
name|setLogPosition
argument_list|(
literal|"bogus"
argument_list|,
literal|"bogus"
argument_list|,
literal|5L
argument_list|)
expr_stmt|;
name|populateQueues
argument_list|()
expr_stmt|;
name|assertEquals
argument_list|(
literal|3
argument_list|,
name|rq1
operator|.
name|getListOfReplicators
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|rq2
operator|.
name|getLogsInQueue
argument_list|(
literal|"qId1"
argument_list|)
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|5
argument_list|,
name|rq3
operator|.
name|getLogsInQueue
argument_list|(
literal|"qId5"
argument_list|)
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|rq3
operator|.
name|getLogPosition
argument_list|(
literal|"qId1"
argument_list|,
literal|"filename0"
argument_list|)
argument_list|)
expr_stmt|;
name|rq3
operator|.
name|setLogPosition
argument_list|(
literal|"qId5"
argument_list|,
literal|"filename4"
argument_list|,
literal|354L
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|354L
argument_list|,
name|rq3
operator|.
name|getLogPosition
argument_list|(
literal|"qId5"
argument_list|,
literal|"filename4"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|5
argument_list|,
name|rq3
operator|.
name|getLogsInQueue
argument_list|(
literal|"qId5"
argument_list|)
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|rq2
operator|.
name|getLogsInQueue
argument_list|(
literal|"qId1"
argument_list|)
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|rq1
operator|.
name|getAllQueues
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|rq2
operator|.
name|getAllQueues
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|5
argument_list|,
name|rq3
operator|.
name|getAllQueues
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|rq3
operator|.
name|claimQueues
argument_list|(
name|server1
argument_list|)
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|rq3
operator|.
name|getListOfReplicators
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|SortedMap
argument_list|<
name|String
argument_list|,
name|SortedSet
argument_list|<
name|String
argument_list|>
argument_list|>
name|queues
init|=
name|rq2
operator|.
name|claimQueues
argument_list|(
name|server3
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|5
argument_list|,
name|queues
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|rq2
operator|.
name|getListOfReplicators
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
comment|// Try to claim our own queues
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|rq2
operator|.
name|claimQueues
argument_list|(
name|server2
argument_list|)
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|6
argument_list|,
name|rq2
operator|.
name|getAllQueues
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|rq2
operator|.
name|removeAllQueues
argument_list|()
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|rq2
operator|.
name|getListOfReplicators
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testInvalidClusterKeys
parameter_list|()
throws|throws
name|ReplicationException
throws|,
name|KeeperException
block|{
name|rp
operator|.
name|init
argument_list|()
expr_stmt|;
try|try
block|{
name|rp
operator|.
name|addPeer
argument_list|(
name|ID_ONE
argument_list|,
operator|new
name|ReplicationPeerConfig
argument_list|()
operator|.
name|setClusterKey
argument_list|(
literal|"hostname1.example.org:1234:hbase"
argument_list|)
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"Should throw an IllegalArgumentException because "
operator|+
literal|"zookeeper.znode.parent is missing leading '/'."
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|e
parameter_list|)
block|{
comment|// Expected.
block|}
try|try
block|{
name|rp
operator|.
name|addPeer
argument_list|(
name|ID_ONE
argument_list|,
operator|new
name|ReplicationPeerConfig
argument_list|()
operator|.
name|setClusterKey
argument_list|(
literal|"hostname1.example.org:1234:/"
argument_list|)
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"Should throw an IllegalArgumentException because zookeeper.znode.parent is missing."
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|e
parameter_list|)
block|{
comment|// Expected.
block|}
try|try
block|{
name|rp
operator|.
name|addPeer
argument_list|(
name|ID_ONE
argument_list|,
operator|new
name|ReplicationPeerConfig
argument_list|()
operator|.
name|setClusterKey
argument_list|(
literal|"hostname1.example.org::/hbase"
argument_list|)
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"Should throw an IllegalArgumentException because "
operator|+
literal|"hbase.zookeeper.property.clientPort is missing."
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|e
parameter_list|)
block|{
comment|// Expected.
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testHfileRefsReplicationQueues
parameter_list|()
throws|throws
name|ReplicationException
throws|,
name|KeeperException
block|{
name|rp
operator|.
name|init
argument_list|()
expr_stmt|;
name|rq1
operator|.
name|init
argument_list|(
name|server1
argument_list|)
expr_stmt|;
name|rqc
operator|.
name|init
argument_list|()
expr_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|files1
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|(
literal|3
argument_list|)
decl_stmt|;
name|files1
operator|.
name|add
argument_list|(
literal|"file_1"
argument_list|)
expr_stmt|;
name|files1
operator|.
name|add
argument_list|(
literal|"file_2"
argument_list|)
expr_stmt|;
name|files1
operator|.
name|add
argument_list|(
literal|"file_3"
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|rqc
operator|.
name|getReplicableHFiles
argument_list|(
name|ID_ONE
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|rqc
operator|.
name|getAllPeersFromHFileRefsQueue
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|rp
operator|.
name|addPeer
argument_list|(
name|ID_ONE
argument_list|,
operator|new
name|ReplicationPeerConfig
argument_list|()
operator|.
name|setClusterKey
argument_list|(
name|KEY_ONE
argument_list|)
argument_list|)
expr_stmt|;
name|rq1
operator|.
name|addHFileRefs
argument_list|(
name|ID_ONE
argument_list|,
name|files1
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|rqc
operator|.
name|getAllPeersFromHFileRefsQueue
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|3
argument_list|,
name|rqc
operator|.
name|getReplicableHFiles
argument_list|(
name|ID_ONE
argument_list|)
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|files2
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|files1
argument_list|)
decl_stmt|;
name|String
name|removedString
init|=
name|files2
operator|.
name|remove
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|rq1
operator|.
name|removeHFileRefs
argument_list|(
name|ID_ONE
argument_list|,
name|files2
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|rqc
operator|.
name|getReplicableHFiles
argument_list|(
name|ID_ONE
argument_list|)
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|files2
operator|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|files2
operator|.
name|add
argument_list|(
name|removedString
argument_list|)
expr_stmt|;
name|rq1
operator|.
name|removeHFileRefs
argument_list|(
name|ID_ONE
argument_list|,
name|files2
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|rqc
operator|.
name|getReplicableHFiles
argument_list|(
name|ID_ONE
argument_list|)
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|rp
operator|.
name|removePeer
argument_list|(
name|ID_ONE
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testRemovePeerForHFileRefs
parameter_list|()
throws|throws
name|ReplicationException
throws|,
name|KeeperException
block|{
name|rq1
operator|.
name|init
argument_list|(
name|server1
argument_list|)
expr_stmt|;
name|rqc
operator|.
name|init
argument_list|()
expr_stmt|;
name|rp
operator|.
name|init
argument_list|()
expr_stmt|;
name|rp
operator|.
name|addPeer
argument_list|(
name|ID_ONE
argument_list|,
operator|new
name|ReplicationPeerConfig
argument_list|()
operator|.
name|setClusterKey
argument_list|(
name|KEY_ONE
argument_list|)
argument_list|)
expr_stmt|;
name|rp
operator|.
name|addPeer
argument_list|(
name|ID_TWO
argument_list|,
operator|new
name|ReplicationPeerConfig
argument_list|()
operator|.
name|setClusterKey
argument_list|(
name|KEY_TWO
argument_list|)
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|files1
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|(
literal|3
argument_list|)
decl_stmt|;
name|files1
operator|.
name|add
argument_list|(
literal|"file_1"
argument_list|)
expr_stmt|;
name|files1
operator|.
name|add
argument_list|(
literal|"file_2"
argument_list|)
expr_stmt|;
name|files1
operator|.
name|add
argument_list|(
literal|"file_3"
argument_list|)
expr_stmt|;
name|rq1
operator|.
name|addHFileRefs
argument_list|(
name|ID_ONE
argument_list|,
name|files1
argument_list|)
expr_stmt|;
name|rq1
operator|.
name|addHFileRefs
argument_list|(
name|ID_TWO
argument_list|,
name|files1
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|rqc
operator|.
name|getAllPeersFromHFileRefsQueue
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|3
argument_list|,
name|rqc
operator|.
name|getReplicableHFiles
argument_list|(
name|ID_ONE
argument_list|)
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|3
argument_list|,
name|rqc
operator|.
name|getReplicableHFiles
argument_list|(
name|ID_TWO
argument_list|)
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|rp
operator|.
name|removePeer
argument_list|(
name|ID_ONE
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|rqc
operator|.
name|getAllPeersFromHFileRefsQueue
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|rqc
operator|.
name|getReplicableHFiles
argument_list|(
name|ID_ONE
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|3
argument_list|,
name|rqc
operator|.
name|getReplicableHFiles
argument_list|(
name|ID_TWO
argument_list|)
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|rp
operator|.
name|removePeer
argument_list|(
name|ID_TWO
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|rqc
operator|.
name|getAllPeersFromHFileRefsQueue
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|rqc
operator|.
name|getReplicableHFiles
argument_list|(
name|ID_TWO
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testReplicationPeers
parameter_list|()
throws|throws
name|Exception
block|{
name|rp
operator|.
name|init
argument_list|()
expr_stmt|;
comment|// Test methods with non-existent peer ids
try|try
block|{
name|rp
operator|.
name|removePeer
argument_list|(
literal|"bogus"
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"Should have thrown an IllegalArgumentException when passed a bogus peerId"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|e
parameter_list|)
block|{     }
try|try
block|{
name|rp
operator|.
name|enablePeer
argument_list|(
literal|"bogus"
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"Should have thrown an IllegalArgumentException when passed a bogus peerId"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|e
parameter_list|)
block|{     }
try|try
block|{
name|rp
operator|.
name|disablePeer
argument_list|(
literal|"bogus"
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"Should have thrown an IllegalArgumentException when passed a bogus peerId"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|e
parameter_list|)
block|{     }
try|try
block|{
name|rp
operator|.
name|getStatusOfPeer
argument_list|(
literal|"bogus"
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"Should have thrown an IllegalArgumentException when passed a bogus peerId"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|e
parameter_list|)
block|{     }
name|assertFalse
argument_list|(
name|rp
operator|.
name|peerAdded
argument_list|(
literal|"bogus"
argument_list|)
argument_list|)
expr_stmt|;
name|rp
operator|.
name|peerRemoved
argument_list|(
literal|"bogus"
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|rp
operator|.
name|getPeerConf
argument_list|(
literal|"bogus"
argument_list|)
argument_list|)
expr_stmt|;
name|assertNumberOfPeers
argument_list|(
literal|0
argument_list|)
expr_stmt|;
comment|// Add some peers
name|rp
operator|.
name|addPeer
argument_list|(
name|ID_ONE
argument_list|,
operator|new
name|ReplicationPeerConfig
argument_list|()
operator|.
name|setClusterKey
argument_list|(
name|KEY_ONE
argument_list|)
argument_list|)
expr_stmt|;
name|assertNumberOfPeers
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|rp
operator|.
name|addPeer
argument_list|(
name|ID_TWO
argument_list|,
operator|new
name|ReplicationPeerConfig
argument_list|()
operator|.
name|setClusterKey
argument_list|(
name|KEY_TWO
argument_list|)
argument_list|)
expr_stmt|;
name|assertNumberOfPeers
argument_list|(
literal|2
argument_list|)
expr_stmt|;
comment|// Test methods with a peer that is added but not connected
try|try
block|{
name|rp
operator|.
name|getStatusOfPeer
argument_list|(
name|ID_ONE
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"There are no connected peers, should have thrown an IllegalArgumentException"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|e
parameter_list|)
block|{     }
name|assertEquals
argument_list|(
name|KEY_ONE
argument_list|,
name|ZKConfig
operator|.
name|getZooKeeperClusterKey
argument_list|(
name|rp
operator|.
name|getPeerConf
argument_list|(
name|ID_ONE
argument_list|)
operator|.
name|getSecond
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|rp
operator|.
name|removePeer
argument_list|(
name|ID_ONE
argument_list|)
expr_stmt|;
name|rp
operator|.
name|peerRemoved
argument_list|(
name|ID_ONE
argument_list|)
expr_stmt|;
name|assertNumberOfPeers
argument_list|(
literal|1
argument_list|)
expr_stmt|;
comment|// Add one peer
name|rp
operator|.
name|addPeer
argument_list|(
name|ID_ONE
argument_list|,
operator|new
name|ReplicationPeerConfig
argument_list|()
operator|.
name|setClusterKey
argument_list|(
name|KEY_ONE
argument_list|)
argument_list|)
expr_stmt|;
name|rp
operator|.
name|peerAdded
argument_list|(
name|ID_ONE
argument_list|)
expr_stmt|;
name|assertNumberOfPeers
argument_list|(
literal|2
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|rp
operator|.
name|getStatusOfPeer
argument_list|(
name|ID_ONE
argument_list|)
argument_list|)
expr_stmt|;
name|rp
operator|.
name|disablePeer
argument_list|(
name|ID_ONE
argument_list|)
expr_stmt|;
name|assertConnectedPeerStatus
argument_list|(
literal|false
argument_list|,
name|ID_ONE
argument_list|)
expr_stmt|;
name|rp
operator|.
name|enablePeer
argument_list|(
name|ID_ONE
argument_list|)
expr_stmt|;
name|assertConnectedPeerStatus
argument_list|(
literal|true
argument_list|,
name|ID_ONE
argument_list|)
expr_stmt|;
comment|// Disconnect peer
name|rp
operator|.
name|peerRemoved
argument_list|(
name|ID_ONE
argument_list|)
expr_stmt|;
name|assertNumberOfPeers
argument_list|(
literal|2
argument_list|)
expr_stmt|;
try|try
block|{
name|rp
operator|.
name|getStatusOfPeer
argument_list|(
name|ID_ONE
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"There are no connected peers, should have thrown an IllegalArgumentException"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|e
parameter_list|)
block|{     }
block|}
specifier|protected
name|void
name|assertConnectedPeerStatus
parameter_list|(
name|boolean
name|status
parameter_list|,
name|String
name|peerId
parameter_list|)
throws|throws
name|Exception
block|{
comment|// we can first check if the value was changed in the store, if it wasn't then fail right away
if|if
condition|(
name|status
operator|!=
name|rp
operator|.
name|getStatusOfPeerFromBackingStore
argument_list|(
name|peerId
argument_list|)
condition|)
block|{
name|fail
argument_list|(
literal|"ConnectedPeerStatus was "
operator|+
operator|!
name|status
operator|+
literal|" but expected "
operator|+
name|status
operator|+
literal|" in ZK"
argument_list|)
expr_stmt|;
block|}
while|while
condition|(
literal|true
condition|)
block|{
if|if
condition|(
name|status
operator|==
name|rp
operator|.
name|getStatusOfPeer
argument_list|(
name|peerId
argument_list|)
condition|)
block|{
return|return;
block|}
if|if
condition|(
name|zkTimeoutCount
operator|<
name|ZK_MAX_COUNT
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"ConnectedPeerStatus was "
operator|+
operator|!
name|status
operator|+
literal|" but expected "
operator|+
name|status
operator|+
literal|", sleeping and trying again."
argument_list|)
expr_stmt|;
name|Thread
operator|.
name|sleep
argument_list|(
name|ZK_SLEEP_INTERVAL
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|fail
argument_list|(
literal|"Timed out waiting for ConnectedPeerStatus to be "
operator|+
name|status
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|protected
name|void
name|assertNumberOfPeers
parameter_list|(
name|int
name|total
parameter_list|)
block|{
name|assertEquals
argument_list|(
name|total
argument_list|,
name|rp
operator|.
name|getAllPeerConfigs
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|total
argument_list|,
name|rp
operator|.
name|getAllPeerIds
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|total
argument_list|,
name|rp
operator|.
name|getAllPeerIds
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/*    * three replicators: rq1 has 0 queues, rq2 has 1 queue with no logs, rq3 has 5 queues with 1, 2,    * 3, 4, 5 log files respectively    */
specifier|protected
name|void
name|populateQueues
parameter_list|()
throws|throws
name|ReplicationException
block|{
name|rq1
operator|.
name|addLog
argument_list|(
literal|"trash"
argument_list|,
literal|"trash"
argument_list|)
expr_stmt|;
name|rq1
operator|.
name|removeQueue
argument_list|(
literal|"trash"
argument_list|)
expr_stmt|;
name|rq2
operator|.
name|addLog
argument_list|(
literal|"qId1"
argument_list|,
literal|"trash"
argument_list|)
expr_stmt|;
name|rq2
operator|.
name|removeLog
argument_list|(
literal|"qId1"
argument_list|,
literal|"trash"
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|1
init|;
name|i
operator|<
literal|6
condition|;
name|i
operator|++
control|)
block|{
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
name|i
condition|;
name|j
operator|++
control|)
block|{
name|rq3
operator|.
name|addLog
argument_list|(
literal|"qId"
operator|+
name|i
argument_list|,
literal|"filename"
operator|+
name|j
argument_list|)
expr_stmt|;
block|}
comment|//Add peers for the corresponding queues so they are not orphans
name|rp
operator|.
name|addPeer
argument_list|(
literal|"qId"
operator|+
name|i
argument_list|,
operator|new
name|ReplicationPeerConfig
argument_list|()
operator|.
name|setClusterKey
argument_list|(
literal|"localhost:2818:/bogus"
operator|+
name|i
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

