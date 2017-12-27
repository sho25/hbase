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
name|assertTrue
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
name|Server
name|server
init|=
operator|new
name|DummyServer
argument_list|(
literal|"ec2-54-234-230-108.compute-1.amazonaws.com"
argument_list|)
decl_stmt|;
name|ReplicationQueueStorage
name|queueStorage
init|=
name|ReplicationStorageFactory
operator|.
name|getReplicationQueueStorage
argument_list|(
name|server
operator|.
name|getZooKeeper
argument_list|()
argument_list|,
name|conf
argument_list|)
decl_stmt|;
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
name|queueStorage
operator|.
name|addWAL
argument_list|(
name|server
operator|.
name|getServerName
argument_list|()
argument_list|,
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
name|ServerName
name|serverName
init|=
name|server
operator|.
name|getServerName
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|unclaimed
init|=
name|queueStorage
operator|.
name|getAllQueues
argument_list|(
name|serverName
argument_list|)
decl_stmt|;
name|queueStorage
operator|.
name|claimQueue
argument_list|(
name|serverName
argument_list|,
name|unclaimed
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|,
name|s1
operator|.
name|getServerName
argument_list|()
argument_list|)
expr_stmt|;
name|queueStorage
operator|.
name|removeReplicatorIfQueueIsEmpty
argument_list|(
name|serverName
argument_list|)
expr_stmt|;
name|serverName
operator|=
name|s1
operator|.
name|getServerName
argument_list|()
expr_stmt|;
name|unclaimed
operator|=
name|queueStorage
operator|.
name|getAllQueues
argument_list|(
name|serverName
argument_list|)
expr_stmt|;
name|queueStorage
operator|.
name|claimQueue
argument_list|(
name|serverName
argument_list|,
name|unclaimed
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|,
name|s2
operator|.
name|getServerName
argument_list|()
argument_list|)
expr_stmt|;
name|queueStorage
operator|.
name|removeReplicatorIfQueueIsEmpty
argument_list|(
name|serverName
argument_list|)
expr_stmt|;
name|serverName
operator|=
name|s2
operator|.
name|getServerName
argument_list|()
expr_stmt|;
name|unclaimed
operator|=
name|queueStorage
operator|.
name|getAllQueues
argument_list|(
name|serverName
argument_list|)
expr_stmt|;
name|String
name|queue3
init|=
name|queueStorage
operator|.
name|claimQueue
argument_list|(
name|serverName
argument_list|,
name|unclaimed
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|,
name|s3
operator|.
name|getServerName
argument_list|()
argument_list|)
operator|.
name|getFirst
argument_list|()
decl_stmt|;
name|queueStorage
operator|.
name|removeReplicatorIfQueueIsEmpty
argument_list|(
name|serverName
argument_list|)
expr_stmt|;
name|ReplicationQueueInfo
name|replicationQueueInfo
init|=
operator|new
name|ReplicationQueueInfo
argument_list|(
name|queue3
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|ServerName
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
block|}
end_class

end_unit

