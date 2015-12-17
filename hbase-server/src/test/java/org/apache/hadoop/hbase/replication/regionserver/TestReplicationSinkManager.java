begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
operator|.
name|regionserver
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
name|testclassification
operator|.
name|SmallTests
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
name|HConnection
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
name|AdminProtos
operator|.
name|AdminService
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
name|HBaseReplicationEndpoint
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
name|ReplicationPeers
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
name|regionserver
operator|.
name|ReplicationSinkManager
operator|.
name|SinkPeer
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
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Lists
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
name|SmallTests
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|TestReplicationSinkManager
block|{
specifier|private
specifier|static
specifier|final
name|String
name|PEER_CLUSTER_ID
init|=
literal|"PEER_CLUSTER_ID"
decl_stmt|;
specifier|private
name|ReplicationPeers
name|replicationPeers
decl_stmt|;
specifier|private
name|HBaseReplicationEndpoint
name|replicationEndpoint
decl_stmt|;
specifier|private
name|ReplicationSinkManager
name|sinkManager
decl_stmt|;
annotation|@
name|Before
specifier|public
name|void
name|setUp
parameter_list|()
block|{
name|replicationPeers
operator|=
name|mock
argument_list|(
name|ReplicationPeers
operator|.
name|class
argument_list|)
expr_stmt|;
name|replicationEndpoint
operator|=
name|mock
argument_list|(
name|HBaseReplicationEndpoint
operator|.
name|class
argument_list|)
expr_stmt|;
name|sinkManager
operator|=
operator|new
name|ReplicationSinkManager
argument_list|(
name|mock
argument_list|(
name|HConnection
operator|.
name|class
argument_list|)
argument_list|,
name|PEER_CLUSTER_ID
argument_list|,
name|replicationEndpoint
argument_list|,
operator|new
name|Configuration
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testChooseSinks
parameter_list|()
block|{
name|List
argument_list|<
name|ServerName
argument_list|>
name|serverNames
init|=
name|Lists
operator|.
name|newArrayList
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
literal|20
condition|;
name|i
operator|++
control|)
block|{
name|serverNames
operator|.
name|add
argument_list|(
name|mock
argument_list|(
name|ServerName
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|when
argument_list|(
name|replicationEndpoint
operator|.
name|getRegionServers
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|serverNames
argument_list|)
expr_stmt|;
name|sinkManager
operator|.
name|chooseSinks
argument_list|()
expr_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|sinkManager
operator|.
name|getNumSinks
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testChooseSinks_LessThanRatioAvailable
parameter_list|()
block|{
name|List
argument_list|<
name|ServerName
argument_list|>
name|serverNames
init|=
name|Lists
operator|.
name|newArrayList
argument_list|(
name|mock
argument_list|(
name|ServerName
operator|.
name|class
argument_list|)
argument_list|,
name|mock
argument_list|(
name|ServerName
operator|.
name|class
argument_list|)
argument_list|)
decl_stmt|;
name|when
argument_list|(
name|replicationEndpoint
operator|.
name|getRegionServers
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|serverNames
argument_list|)
expr_stmt|;
name|sinkManager
operator|.
name|chooseSinks
argument_list|()
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|sinkManager
operator|.
name|getNumSinks
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testReportBadSink
parameter_list|()
block|{
name|ServerName
name|serverNameA
init|=
name|mock
argument_list|(
name|ServerName
operator|.
name|class
argument_list|)
decl_stmt|;
name|ServerName
name|serverNameB
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
name|replicationEndpoint
operator|.
name|getRegionServers
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|Lists
operator|.
name|newArrayList
argument_list|(
name|serverNameA
argument_list|,
name|serverNameB
argument_list|)
argument_list|)
expr_stmt|;
name|sinkManager
operator|.
name|chooseSinks
argument_list|()
expr_stmt|;
comment|// Sanity check
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|sinkManager
operator|.
name|getNumSinks
argument_list|()
argument_list|)
expr_stmt|;
name|SinkPeer
name|sinkPeer
init|=
operator|new
name|SinkPeer
argument_list|(
name|serverNameA
argument_list|,
name|mock
argument_list|(
name|AdminService
operator|.
name|BlockingInterface
operator|.
name|class
argument_list|)
argument_list|)
decl_stmt|;
name|sinkManager
operator|.
name|reportBadSink
argument_list|(
name|sinkPeer
argument_list|)
expr_stmt|;
comment|// Just reporting a bad sink once shouldn't have an effect
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|sinkManager
operator|.
name|getNumSinks
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**    * Once a SinkPeer has been reported as bad more than BAD_SINK_THRESHOLD times, it should not    * be replicated to anymore.    */
annotation|@
name|Test
specifier|public
name|void
name|testReportBadSink_PastThreshold
parameter_list|()
block|{
name|List
argument_list|<
name|ServerName
argument_list|>
name|serverNames
init|=
name|Lists
operator|.
name|newArrayList
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
literal|30
condition|;
name|i
operator|++
control|)
block|{
name|serverNames
operator|.
name|add
argument_list|(
name|mock
argument_list|(
name|ServerName
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|when
argument_list|(
name|replicationEndpoint
operator|.
name|getRegionServers
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|serverNames
argument_list|)
expr_stmt|;
name|sinkManager
operator|.
name|chooseSinks
argument_list|()
expr_stmt|;
comment|// Sanity check
name|assertEquals
argument_list|(
literal|3
argument_list|,
name|sinkManager
operator|.
name|getNumSinks
argument_list|()
argument_list|)
expr_stmt|;
name|ServerName
name|serverName
init|=
name|sinkManager
operator|.
name|getSinksForTesting
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|SinkPeer
name|sinkPeer
init|=
operator|new
name|SinkPeer
argument_list|(
name|serverName
argument_list|,
name|mock
argument_list|(
name|AdminService
operator|.
name|BlockingInterface
operator|.
name|class
argument_list|)
argument_list|)
decl_stmt|;
name|sinkManager
operator|.
name|reportSinkSuccess
argument_list|(
name|sinkPeer
argument_list|)
expr_stmt|;
comment|// has no effect, counter does not go negative
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<=
name|ReplicationSinkManager
operator|.
name|DEFAULT_BAD_SINK_THRESHOLD
condition|;
name|i
operator|++
control|)
block|{
name|sinkManager
operator|.
name|reportBadSink
argument_list|(
name|sinkPeer
argument_list|)
expr_stmt|;
block|}
comment|// Reporting a bad sink more than the threshold count should remove it
comment|// from the list of potential sinks
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|sinkManager
operator|.
name|getNumSinks
argument_list|()
argument_list|)
expr_stmt|;
comment|//
comment|// now try a sink that has some successes
comment|//
name|serverName
operator|=
name|sinkManager
operator|.
name|getSinksForTesting
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|sinkPeer
operator|=
operator|new
name|SinkPeer
argument_list|(
name|serverName
argument_list|,
name|mock
argument_list|(
name|AdminService
operator|.
name|BlockingInterface
operator|.
name|class
argument_list|)
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
operator|<=
name|ReplicationSinkManager
operator|.
name|DEFAULT_BAD_SINK_THRESHOLD
operator|-
literal|1
condition|;
name|i
operator|++
control|)
block|{
name|sinkManager
operator|.
name|reportBadSink
argument_list|(
name|sinkPeer
argument_list|)
expr_stmt|;
block|}
name|sinkManager
operator|.
name|reportSinkSuccess
argument_list|(
name|sinkPeer
argument_list|)
expr_stmt|;
comment|// one success
name|sinkManager
operator|.
name|reportBadSink
argument_list|(
name|sinkPeer
argument_list|)
expr_stmt|;
comment|// did not remove the sink, since we had one successful try
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|sinkManager
operator|.
name|getNumSinks
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
operator|<=
name|ReplicationSinkManager
operator|.
name|DEFAULT_BAD_SINK_THRESHOLD
operator|-
literal|2
condition|;
name|i
operator|++
control|)
block|{
name|sinkManager
operator|.
name|reportBadSink
argument_list|(
name|sinkPeer
argument_list|)
expr_stmt|;
block|}
comment|// still not remove, since the success reset the counter
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|sinkManager
operator|.
name|getNumSinks
argument_list|()
argument_list|)
expr_stmt|;
name|sinkManager
operator|.
name|reportBadSink
argument_list|(
name|sinkPeer
argument_list|)
expr_stmt|;
comment|// but we exhausted the tries
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|sinkManager
operator|.
name|getNumSinks
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testReportBadSink_DownToZeroSinks
parameter_list|()
block|{
name|List
argument_list|<
name|ServerName
argument_list|>
name|serverNames
init|=
name|Lists
operator|.
name|newArrayList
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
literal|20
condition|;
name|i
operator|++
control|)
block|{
name|serverNames
operator|.
name|add
argument_list|(
name|mock
argument_list|(
name|ServerName
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|when
argument_list|(
name|replicationEndpoint
operator|.
name|getRegionServers
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|serverNames
argument_list|)
expr_stmt|;
name|sinkManager
operator|.
name|chooseSinks
argument_list|()
expr_stmt|;
comment|// Sanity check
name|List
argument_list|<
name|ServerName
argument_list|>
name|sinkList
init|=
name|sinkManager
operator|.
name|getSinksForTesting
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|sinkList
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|ServerName
name|serverNameA
init|=
name|sinkList
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|ServerName
name|serverNameB
init|=
name|sinkList
operator|.
name|get
argument_list|(
literal|1
argument_list|)
decl_stmt|;
name|SinkPeer
name|sinkPeerA
init|=
operator|new
name|SinkPeer
argument_list|(
name|serverNameA
argument_list|,
name|mock
argument_list|(
name|AdminService
operator|.
name|BlockingInterface
operator|.
name|class
argument_list|)
argument_list|)
decl_stmt|;
name|SinkPeer
name|sinkPeerB
init|=
operator|new
name|SinkPeer
argument_list|(
name|serverNameB
argument_list|,
name|mock
argument_list|(
name|AdminService
operator|.
name|BlockingInterface
operator|.
name|class
argument_list|)
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<=
name|ReplicationSinkManager
operator|.
name|DEFAULT_BAD_SINK_THRESHOLD
condition|;
name|i
operator|++
control|)
block|{
name|sinkManager
operator|.
name|reportBadSink
argument_list|(
name|sinkPeerA
argument_list|)
expr_stmt|;
name|sinkManager
operator|.
name|reportBadSink
argument_list|(
name|sinkPeerB
argument_list|)
expr_stmt|;
block|}
comment|// We've gone down to 0 good sinks, so the replication sinks
comment|// should have been refreshed now
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|sinkManager
operator|.
name|getNumSinks
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

