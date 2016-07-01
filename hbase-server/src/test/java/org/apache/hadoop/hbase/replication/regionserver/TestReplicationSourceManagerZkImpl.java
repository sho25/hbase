begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/* * * Licensed to the Apache Software Foundation (ASF) under one * or more contributor license agreements.  See the NOTICE file * distributed with this work for additional information * regarding copyright ownership.  The ASF licenses this file * to you under the Apache License, Version 2.0 (the * "License"); you may not use this file except in compliance * with the License.  You may obtain a copy of the License at * *     http://www.apache.org/licenses/LICENSE-2.0 * * Unless required by applicable law or agreed to in writing, software * distributed under the License is distributed on an "AS IS" BASIS, * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. * See the License for the specific language governing permissions and * limitations under the License. */
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
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|HBaseConfiguration
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
name|replication
operator|.
name|ReplicationFactory
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
name|ReplicationQueueInfo
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
name|ReplicationQueues
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
name|ReplicationQueuesArguments
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
name|ReplicationQueuesClientArguments
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
name|ReplicationQueuesClientZKImpl
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
name|ReplicationSourceDummy
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
name|Set
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
name|assertTrue
import|;
end_import

begin_comment
comment|/**  * Tests the ReplicationSourceManager with ReplicationQueueZkImpl's and  * ReplicationQueuesClientZkImpl. Also includes extra tests outside of those in  * TestReplicationSourceManager that test ReplicationQueueZkImpl-specific behaviors.  */
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
name|TestReplicationSourceManagerZkImpl
extends|extends
name|TestReplicationSourceManager
block|{
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
name|conf
operator|=
name|HBaseConfiguration
operator|.
name|create
argument_list|()
expr_stmt|;
name|conf
operator|.
name|set
argument_list|(
literal|"replication.replicationsource.implementation"
argument_list|,
name|ReplicationSourceDummy
operator|.
name|class
operator|.
name|getCanonicalName
argument_list|()
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setLong
argument_list|(
literal|"replication.sleep.before.failover"
argument_list|,
literal|2000
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setInt
argument_list|(
literal|"replication.source.maxretriesmultiplier"
argument_list|,
literal|10
argument_list|)
expr_stmt|;
name|utility
operator|=
operator|new
name|HBaseTestingUtility
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|utility
operator|.
name|startMiniZKCluster
argument_list|()
expr_stmt|;
name|setupZkAndReplication
argument_list|()
expr_stmt|;
block|}
comment|// Tests the naming convention of adopted queues for ReplicationQueuesZkImpl
annotation|@
name|Test
specifier|public
name|void
name|testNodeFailoverDeadServerParsing
parameter_list|()
throws|throws
name|Exception
block|{
name|conf
operator|.
name|setBoolean
argument_list|(
name|HConstants
operator|.
name|ZOOKEEPER_USEMULTI
argument_list|,
literal|true
argument_list|)
expr_stmt|;
specifier|final
name|Server
name|server
init|=
operator|new
name|DummyServer
argument_list|(
literal|"ec2-54-234-230-108.compute-1.amazonaws.com"
argument_list|)
decl_stmt|;
name|ReplicationQueues
name|repQueues
init|=
name|ReplicationFactory
operator|.
name|getReplicationQueues
argument_list|(
operator|new
name|ReplicationQueuesArguments
argument_list|(
name|conf
argument_list|,
name|server
argument_list|,
name|server
operator|.
name|getZooKeeper
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|repQueues
operator|.
name|init
argument_list|(
name|server
operator|.
name|getServerName
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
comment|// populate some znodes in the peer znode
name|files
operator|.
name|add
argument_list|(
literal|"log1"
argument_list|)
expr_stmt|;
name|files
operator|.
name|add
argument_list|(
literal|"log2"
argument_list|)
expr_stmt|;
for|for
control|(
name|String
name|file
range|:
name|files
control|)
block|{
name|repQueues
operator|.
name|addLog
argument_list|(
literal|"1"
argument_list|,
name|file
argument_list|)
expr_stmt|;
block|}
comment|// create 3 DummyServers
name|Server
name|s1
init|=
operator|new
name|DummyServer
argument_list|(
literal|"ip-10-8-101-114.ec2.internal"
argument_list|)
decl_stmt|;
name|Server
name|s2
init|=
operator|new
name|DummyServer
argument_list|(
literal|"ec2-107-20-52-47.compute-1.amazonaws.com"
argument_list|)
decl_stmt|;
name|Server
name|s3
init|=
operator|new
name|DummyServer
argument_list|(
literal|"ec2-23-20-187-167.compute-1.amazonaws.com"
argument_list|)
decl_stmt|;
comment|// simulate three servers fail sequentially
name|ReplicationQueues
name|rq1
init|=
name|ReplicationFactory
operator|.
name|getReplicationQueues
argument_list|(
operator|new
name|ReplicationQueuesArguments
argument_list|(
name|s1
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|s1
argument_list|,
name|s1
operator|.
name|getZooKeeper
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|rq1
operator|.
name|init
argument_list|(
name|s1
operator|.
name|getServerName
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Set
argument_list|<
name|String
argument_list|>
argument_list|>
name|testMap
init|=
name|rq1
operator|.
name|claimQueues
argument_list|(
name|server
operator|.
name|getServerName
argument_list|()
operator|.
name|getServerName
argument_list|()
argument_list|)
decl_stmt|;
name|ReplicationQueues
name|rq2
init|=
name|ReplicationFactory
operator|.
name|getReplicationQueues
argument_list|(
operator|new
name|ReplicationQueuesArguments
argument_list|(
name|s2
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|s2
argument_list|,
name|s2
operator|.
name|getZooKeeper
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|rq2
operator|.
name|init
argument_list|(
name|s2
operator|.
name|getServerName
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|testMap
operator|=
name|rq2
operator|.
name|claimQueues
argument_list|(
name|s1
operator|.
name|getServerName
argument_list|()
operator|.
name|getServerName
argument_list|()
argument_list|)
expr_stmt|;
name|ReplicationQueues
name|rq3
init|=
name|ReplicationFactory
operator|.
name|getReplicationQueues
argument_list|(
operator|new
name|ReplicationQueuesArguments
argument_list|(
name|s3
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|s3
argument_list|,
name|s3
operator|.
name|getZooKeeper
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|rq3
operator|.
name|init
argument_list|(
name|s3
operator|.
name|getServerName
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|testMap
operator|=
name|rq3
operator|.
name|claimQueues
argument_list|(
name|s2
operator|.
name|getServerName
argument_list|()
operator|.
name|getServerName
argument_list|()
argument_list|)
expr_stmt|;
name|ReplicationQueueInfo
name|replicationQueueInfo
init|=
operator|new
name|ReplicationQueueInfo
argument_list|(
name|testMap
operator|.
name|keySet
argument_list|()
operator|.
name|iterator
argument_list|()
operator|.
name|next
argument_list|()
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|result
init|=
name|replicationQueueInfo
operator|.
name|getDeadRegionServers
argument_list|()
decl_stmt|;
comment|// verify
name|assertTrue
argument_list|(
name|result
operator|.
name|contains
argument_list|(
name|server
operator|.
name|getServerName
argument_list|()
operator|.
name|getServerName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|result
operator|.
name|contains
argument_list|(
name|s1
operator|.
name|getServerName
argument_list|()
operator|.
name|getServerName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|result
operator|.
name|contains
argument_list|(
name|s2
operator|.
name|getServerName
argument_list|()
operator|.
name|getServerName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|server
operator|.
name|stop
argument_list|(
literal|""
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testFailoverDeadServerCversionChange
parameter_list|()
throws|throws
name|Exception
block|{
name|conf
operator|.
name|setBoolean
argument_list|(
name|HConstants
operator|.
name|ZOOKEEPER_USEMULTI
argument_list|,
literal|true
argument_list|)
expr_stmt|;
specifier|final
name|Server
name|s0
init|=
operator|new
name|DummyServer
argument_list|(
literal|"cversion-change0.example.org"
argument_list|)
decl_stmt|;
name|ReplicationQueues
name|repQueues
init|=
name|ReplicationFactory
operator|.
name|getReplicationQueues
argument_list|(
operator|new
name|ReplicationQueuesArguments
argument_list|(
name|conf
argument_list|,
name|s0
argument_list|,
name|s0
operator|.
name|getZooKeeper
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|repQueues
operator|.
name|init
argument_list|(
name|s0
operator|.
name|getServerName
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
comment|// populate some znodes in the peer znode
name|files
operator|.
name|add
argument_list|(
literal|"log1"
argument_list|)
expr_stmt|;
name|files
operator|.
name|add
argument_list|(
literal|"log2"
argument_list|)
expr_stmt|;
for|for
control|(
name|String
name|file
range|:
name|files
control|)
block|{
name|repQueues
operator|.
name|addLog
argument_list|(
literal|"1"
argument_list|,
name|file
argument_list|)
expr_stmt|;
block|}
comment|// simulate queue transfer
name|Server
name|s1
init|=
operator|new
name|DummyServer
argument_list|(
literal|"cversion-change1.example.org"
argument_list|)
decl_stmt|;
name|ReplicationQueues
name|rq1
init|=
name|ReplicationFactory
operator|.
name|getReplicationQueues
argument_list|(
operator|new
name|ReplicationQueuesArguments
argument_list|(
name|s1
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|s1
argument_list|,
name|s1
operator|.
name|getZooKeeper
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|rq1
operator|.
name|init
argument_list|(
name|s1
operator|.
name|getServerName
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|ReplicationQueuesClientZKImpl
name|client
init|=
operator|(
name|ReplicationQueuesClientZKImpl
operator|)
name|ReplicationFactory
operator|.
name|getReplicationQueuesClient
argument_list|(
operator|new
name|ReplicationQueuesClientArguments
argument_list|(
name|s1
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|s1
argument_list|,
name|s1
operator|.
name|getZooKeeper
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|int
name|v0
init|=
name|client
operator|.
name|getQueuesZNodeCversion
argument_list|()
decl_stmt|;
name|rq1
operator|.
name|claimQueues
argument_list|(
name|s0
operator|.
name|getServerName
argument_list|()
operator|.
name|getServerName
argument_list|()
argument_list|)
expr_stmt|;
name|int
name|v1
init|=
name|client
operator|.
name|getQueuesZNodeCversion
argument_list|()
decl_stmt|;
comment|// cversion should increase by 1 since a child node is deleted
name|assertEquals
argument_list|(
name|v0
operator|+
literal|1
argument_list|,
name|v1
argument_list|)
expr_stmt|;
name|s0
operator|.
name|stop
argument_list|(
literal|""
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

