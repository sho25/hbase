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
name|Cell
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
name|ConnectionFactory
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
name|Put
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
name|regionserver
operator|.
name|HRegion
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
name|HBaseInterClusterReplicationEndpoint
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
name|JVMClusterUtil
operator|.
name|RegionServerThread
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
name|Threads
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
name|wal
operator|.
name|WAL
operator|.
name|Entry
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
name|Before
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
comment|/**  * Tests ReplicationSource and ReplicationEndpoint interactions  */
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
name|TestReplicationEndpoint
extends|extends
name|TestReplicationBase
block|{
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
name|TestReplicationEndpoint
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|static
name|int
name|numRegionServers
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
name|TestReplicationBase
operator|.
name|setUpBeforeClass
argument_list|()
expr_stmt|;
name|admin
operator|.
name|removePeer
argument_list|(
literal|"2"
argument_list|)
expr_stmt|;
name|numRegionServers
operator|=
name|utility1
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|getRegionServerThreads
argument_list|()
operator|.
name|size
argument_list|()
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
name|TestReplicationBase
operator|.
name|tearDownAfterClass
argument_list|()
expr_stmt|;
comment|// check stop is called
name|Assert
operator|.
name|assertTrue
argument_list|(
name|ReplicationEndpointForTest
operator|.
name|stoppedCount
operator|.
name|get
argument_list|()
operator|>
literal|0
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Before
specifier|public
name|void
name|setup
parameter_list|()
throws|throws
name|Exception
block|{
name|ReplicationEndpointForTest
operator|.
name|contructedCount
operator|.
name|set
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|ReplicationEndpointForTest
operator|.
name|startedCount
operator|.
name|set
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|ReplicationEndpointForTest
operator|.
name|replicateCount
operator|.
name|set
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|ReplicationEndpointReturningFalse
operator|.
name|replicated
operator|.
name|set
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|ReplicationEndpointForTest
operator|.
name|lastEntries
operator|=
literal|null
expr_stmt|;
specifier|final
name|List
argument_list|<
name|RegionServerThread
argument_list|>
name|rsThreads
init|=
name|utility1
operator|.
name|getMiniHBaseCluster
argument_list|()
operator|.
name|getRegionServerThreads
argument_list|()
decl_stmt|;
for|for
control|(
name|RegionServerThread
name|rs
range|:
name|rsThreads
control|)
block|{
name|utility1
operator|.
name|getHBaseAdmin
argument_list|()
operator|.
name|rollWALWriter
argument_list|(
name|rs
operator|.
name|getRegionServer
argument_list|()
operator|.
name|getServerName
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|// Wait for  all log roll to finish
name|utility1
operator|.
name|waitFor
argument_list|(
literal|3000
argument_list|,
operator|new
name|Waiter
operator|.
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
throws|throws
name|Exception
block|{
for|for
control|(
name|RegionServerThread
name|rs
range|:
name|rsThreads
control|)
block|{
if|if
condition|(
operator|!
name|rs
operator|.
name|getRegionServer
argument_list|()
operator|.
name|walRollRequestFinished
argument_list|()
condition|)
block|{
return|return
literal|false
return|;
block|}
block|}
return|return
literal|true
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|explainFailure
parameter_list|()
throws|throws
name|Exception
block|{
name|List
argument_list|<
name|String
argument_list|>
name|logRollInProgressRsList
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|RegionServerThread
name|rs
range|:
name|rsThreads
control|)
block|{
if|if
condition|(
operator|!
name|rs
operator|.
name|getRegionServer
argument_list|()
operator|.
name|walRollRequestFinished
argument_list|()
condition|)
block|{
name|logRollInProgressRsList
operator|.
name|add
argument_list|(
name|rs
operator|.
name|getRegionServer
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
return|return
literal|"Still waiting for log roll on regionservers: "
operator|+
name|logRollInProgressRsList
return|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|120000
argument_list|)
specifier|public
name|void
name|testCustomReplicationEndpoint
parameter_list|()
throws|throws
name|Exception
block|{
comment|// test installing a custom replication endpoint other than the default one.
name|admin
operator|.
name|addPeer
argument_list|(
literal|"testCustomReplicationEndpoint"
argument_list|,
operator|new
name|ReplicationPeerConfig
argument_list|()
operator|.
name|setClusterKey
argument_list|(
name|ZKConfig
operator|.
name|getZooKeeperClusterKey
argument_list|(
name|conf1
argument_list|)
argument_list|)
operator|.
name|setReplicationEndpointImpl
argument_list|(
name|ReplicationEndpointForTest
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
argument_list|,
literal|null
argument_list|)
expr_stmt|;
comment|// check whether the class has been constructed and started
name|Waiter
operator|.
name|waitFor
argument_list|(
name|conf1
argument_list|,
literal|60000
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
name|ReplicationEndpointForTest
operator|.
name|contructedCount
operator|.
name|get
argument_list|()
operator|>=
name|numRegionServers
return|;
block|}
block|}
argument_list|)
expr_stmt|;
name|Waiter
operator|.
name|waitFor
argument_list|(
name|conf1
argument_list|,
literal|60000
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
name|ReplicationEndpointForTest
operator|.
name|startedCount
operator|.
name|get
argument_list|()
operator|>=
name|numRegionServers
return|;
block|}
block|}
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|ReplicationEndpointForTest
operator|.
name|replicateCount
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
comment|// now replicate some data.
name|doPut
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"row42"
argument_list|)
argument_list|)
expr_stmt|;
name|Waiter
operator|.
name|waitFor
argument_list|(
name|conf1
argument_list|,
literal|60000
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
name|ReplicationEndpointForTest
operator|.
name|replicateCount
operator|.
name|get
argument_list|()
operator|>=
literal|1
return|;
block|}
block|}
argument_list|)
expr_stmt|;
name|doAssert
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"row42"
argument_list|)
argument_list|)
expr_stmt|;
name|admin
operator|.
name|removePeer
argument_list|(
literal|"testCustomReplicationEndpoint"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|120000
argument_list|)
specifier|public
name|void
name|testReplicationEndpointReturnsFalseOnReplicate
parameter_list|()
throws|throws
name|Exception
block|{
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|ReplicationEndpointForTest
operator|.
name|replicateCount
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
operator|!
name|ReplicationEndpointReturningFalse
operator|.
name|replicated
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
name|int
name|peerCount
init|=
name|admin
operator|.
name|getPeersCount
argument_list|()
decl_stmt|;
specifier|final
name|String
name|id
init|=
literal|"testReplicationEndpointReturnsFalseOnReplicate"
decl_stmt|;
name|admin
operator|.
name|addPeer
argument_list|(
name|id
argument_list|,
operator|new
name|ReplicationPeerConfig
argument_list|()
operator|.
name|setClusterKey
argument_list|(
name|ZKConfig
operator|.
name|getZooKeeperClusterKey
argument_list|(
name|conf1
argument_list|)
argument_list|)
operator|.
name|setReplicationEndpointImpl
argument_list|(
name|ReplicationEndpointReturningFalse
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
argument_list|,
literal|null
argument_list|)
expr_stmt|;
comment|// This test is flakey and then there is so much stuff flying around in here its, hard to
comment|// debug.  Peer needs to be up for the edit to make it across. This wait on
comment|// peer count seems to be a hack that has us not progress till peer is up.
if|if
condition|(
name|admin
operator|.
name|getPeersCount
argument_list|()
operator|<=
name|peerCount
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Waiting on peercount to go up from "
operator|+
name|peerCount
argument_list|)
expr_stmt|;
name|Threads
operator|.
name|sleep
argument_list|(
literal|100
argument_list|)
expr_stmt|;
block|}
comment|// now replicate some data
name|doPut
argument_list|(
name|row
argument_list|)
expr_stmt|;
name|Waiter
operator|.
name|waitFor
argument_list|(
name|conf1
argument_list|,
literal|60000
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
comment|// Looks like replication endpoint returns false unless we put more than 10 edits. We
comment|// only send over one edit.
name|int
name|count
init|=
name|ReplicationEndpointForTest
operator|.
name|replicateCount
operator|.
name|get
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"count="
operator|+
name|count
argument_list|)
expr_stmt|;
return|return
name|ReplicationEndpointReturningFalse
operator|.
name|replicated
operator|.
name|get
argument_list|()
return|;
block|}
block|}
argument_list|)
expr_stmt|;
if|if
condition|(
name|ReplicationEndpointReturningFalse
operator|.
name|ex
operator|.
name|get
argument_list|()
operator|!=
literal|null
condition|)
block|{
throw|throw
name|ReplicationEndpointReturningFalse
operator|.
name|ex
operator|.
name|get
argument_list|()
throw|;
block|}
name|admin
operator|.
name|removePeer
argument_list|(
literal|"testReplicationEndpointReturnsFalseOnReplicate"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|120000
argument_list|)
specifier|public
name|void
name|testInterClusterReplication
parameter_list|()
throws|throws
name|Exception
block|{
specifier|final
name|String
name|id
init|=
literal|"testInterClusterReplication"
decl_stmt|;
name|List
argument_list|<
name|HRegion
argument_list|>
name|regions
init|=
name|utility1
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|getRegions
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
name|int
name|totEdits
init|=
literal|0
decl_stmt|;
comment|// Make sure edits are spread across regions because we do region based batching
comment|// before shipping edits.
for|for
control|(
name|HRegion
name|region
range|:
name|regions
control|)
block|{
name|HRegionInfo
name|hri
init|=
name|region
operator|.
name|getRegionInfo
argument_list|()
decl_stmt|;
name|byte
index|[]
name|row
init|=
name|hri
operator|.
name|getStartKey
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
literal|100
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|row
operator|.
name|length
operator|>
literal|0
condition|)
block|{
name|Put
name|put
init|=
operator|new
name|Put
argument_list|(
name|row
argument_list|)
decl_stmt|;
name|put
operator|.
name|addColumn
argument_list|(
name|famName
argument_list|,
name|row
argument_list|,
name|row
argument_list|)
expr_stmt|;
name|region
operator|.
name|put
argument_list|(
name|put
argument_list|)
expr_stmt|;
name|totEdits
operator|++
expr_stmt|;
block|}
block|}
block|}
name|admin
operator|.
name|addPeer
argument_list|(
name|id
argument_list|,
operator|new
name|ReplicationPeerConfig
argument_list|()
operator|.
name|setClusterKey
argument_list|(
name|ZKConfig
operator|.
name|getZooKeeperClusterKey
argument_list|(
name|conf2
argument_list|)
argument_list|)
operator|.
name|setReplicationEndpointImpl
argument_list|(
name|InterClusterReplicationEndpointForTest
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
argument_list|,
literal|null
argument_list|)
expr_stmt|;
specifier|final
name|int
name|numEdits
init|=
name|totEdits
decl_stmt|;
name|Waiter
operator|.
name|waitFor
argument_list|(
name|conf1
argument_list|,
literal|30000
argument_list|,
operator|new
name|Waiter
operator|.
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
throws|throws
name|Exception
block|{
return|return
name|InterClusterReplicationEndpointForTest
operator|.
name|replicateCount
operator|.
name|get
argument_list|()
operator|==
name|numEdits
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|explainFailure
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|failure
init|=
literal|"Failed to replicate all edits, expected = "
operator|+
name|numEdits
operator|+
literal|" replicated = "
operator|+
name|InterClusterReplicationEndpointForTest
operator|.
name|replicateCount
operator|.
name|get
argument_list|()
decl_stmt|;
return|return
name|failure
return|;
block|}
block|}
argument_list|)
expr_stmt|;
name|admin
operator|.
name|removePeer
argument_list|(
literal|"testInterClusterReplication"
argument_list|)
expr_stmt|;
name|utility1
operator|.
name|deleteTableData
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|120000
argument_list|)
specifier|public
name|void
name|testWALEntryFilterFromReplicationEndpoint
parameter_list|()
throws|throws
name|Exception
block|{
name|admin
operator|.
name|addPeer
argument_list|(
literal|"testWALEntryFilterFromReplicationEndpoint"
argument_list|,
operator|new
name|ReplicationPeerConfig
argument_list|()
operator|.
name|setClusterKey
argument_list|(
name|ZKConfig
operator|.
name|getZooKeeperClusterKey
argument_list|(
name|conf1
argument_list|)
argument_list|)
operator|.
name|setReplicationEndpointImpl
argument_list|(
name|ReplicationEndpointWithWALEntryFilter
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
argument_list|,
literal|null
argument_list|)
expr_stmt|;
comment|// now replicate some data.
try|try
init|(
name|Connection
name|connection
init|=
name|ConnectionFactory
operator|.
name|createConnection
argument_list|(
name|conf1
argument_list|)
init|)
block|{
name|doPut
argument_list|(
name|connection
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"row1"
argument_list|)
argument_list|)
expr_stmt|;
name|doPut
argument_list|(
name|connection
argument_list|,
name|row
argument_list|)
expr_stmt|;
name|doPut
argument_list|(
name|connection
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"row2"
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|Waiter
operator|.
name|waitFor
argument_list|(
name|conf1
argument_list|,
literal|60000
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
name|ReplicationEndpointForTest
operator|.
name|replicateCount
operator|.
name|get
argument_list|()
operator|>=
literal|1
return|;
block|}
block|}
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertNull
argument_list|(
name|ReplicationEndpointWithWALEntryFilter
operator|.
name|ex
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
name|admin
operator|.
name|removePeer
argument_list|(
literal|"testWALEntryFilterFromReplicationEndpoint"
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|doPut
parameter_list|(
name|byte
index|[]
name|row
parameter_list|)
throws|throws
name|IOException
block|{
try|try
init|(
name|Connection
name|connection
init|=
name|ConnectionFactory
operator|.
name|createConnection
argument_list|(
name|conf1
argument_list|)
init|)
block|{
name|doPut
argument_list|(
name|connection
argument_list|,
name|row
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|doPut
parameter_list|(
specifier|final
name|Connection
name|connection
parameter_list|,
specifier|final
name|byte
index|[]
name|row
parameter_list|)
throws|throws
name|IOException
block|{
try|try
init|(
name|Table
name|t
init|=
name|connection
operator|.
name|getTable
argument_list|(
name|tableName
argument_list|)
init|)
block|{
name|Put
name|put
init|=
operator|new
name|Put
argument_list|(
name|row
argument_list|)
decl_stmt|;
name|put
operator|.
name|addColumn
argument_list|(
name|famName
argument_list|,
name|row
argument_list|,
name|row
argument_list|)
expr_stmt|;
name|t
operator|.
name|put
argument_list|(
name|put
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
specifier|static
name|void
name|doAssert
parameter_list|(
name|byte
index|[]
name|row
parameter_list|)
throws|throws
name|Exception
block|{
if|if
condition|(
name|ReplicationEndpointForTest
operator|.
name|lastEntries
operator|==
literal|null
condition|)
block|{
return|return;
comment|// first call
block|}
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|ReplicationEndpointForTest
operator|.
name|lastEntries
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|Cell
argument_list|>
name|cells
init|=
name|ReplicationEndpointForTest
operator|.
name|lastEntries
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getEdit
argument_list|()
operator|.
name|getCells
argument_list|()
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|cells
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|Bytes
operator|.
name|equals
argument_list|(
name|cells
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getRowArray
argument_list|()
argument_list|,
name|cells
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getRowOffset
argument_list|()
argument_list|,
name|cells
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getRowLength
argument_list|()
argument_list|,
name|row
argument_list|,
literal|0
argument_list|,
name|row
operator|.
name|length
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
class|class
name|ReplicationEndpointForTest
extends|extends
name|BaseReplicationEndpoint
block|{
specifier|static
name|UUID
name|uuid
init|=
name|UUID
operator|.
name|randomUUID
argument_list|()
decl_stmt|;
specifier|static
name|AtomicInteger
name|contructedCount
init|=
operator|new
name|AtomicInteger
argument_list|()
decl_stmt|;
specifier|static
name|AtomicInteger
name|startedCount
init|=
operator|new
name|AtomicInteger
argument_list|()
decl_stmt|;
specifier|static
name|AtomicInteger
name|stoppedCount
init|=
operator|new
name|AtomicInteger
argument_list|()
decl_stmt|;
specifier|static
name|AtomicInteger
name|replicateCount
init|=
operator|new
name|AtomicInteger
argument_list|()
decl_stmt|;
specifier|static
specifier|volatile
name|List
argument_list|<
name|Entry
argument_list|>
name|lastEntries
init|=
literal|null
decl_stmt|;
specifier|public
name|ReplicationEndpointForTest
parameter_list|()
block|{
name|contructedCount
operator|.
name|incrementAndGet
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|UUID
name|getPeerUUID
parameter_list|()
block|{
return|return
name|uuid
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|replicate
parameter_list|(
name|ReplicateContext
name|replicateContext
parameter_list|)
block|{
name|replicateCount
operator|.
name|incrementAndGet
argument_list|()
expr_stmt|;
name|lastEntries
operator|=
name|replicateContext
operator|.
name|entries
expr_stmt|;
return|return
literal|true
return|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|doStart
parameter_list|()
block|{
name|startedCount
operator|.
name|incrementAndGet
argument_list|()
expr_stmt|;
name|notifyStarted
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|doStop
parameter_list|()
block|{
name|stoppedCount
operator|.
name|incrementAndGet
argument_list|()
expr_stmt|;
name|notifyStopped
argument_list|()
expr_stmt|;
block|}
block|}
specifier|public
specifier|static
class|class
name|InterClusterReplicationEndpointForTest
extends|extends
name|HBaseInterClusterReplicationEndpoint
block|{
specifier|static
name|AtomicInteger
name|replicateCount
init|=
operator|new
name|AtomicInteger
argument_list|()
decl_stmt|;
specifier|static
name|boolean
name|failedOnce
decl_stmt|;
annotation|@
name|Override
specifier|public
name|boolean
name|replicate
parameter_list|(
name|ReplicateContext
name|replicateContext
parameter_list|)
block|{
name|boolean
name|success
init|=
name|super
operator|.
name|replicate
argument_list|(
name|replicateContext
argument_list|)
decl_stmt|;
if|if
condition|(
name|success
condition|)
block|{
name|replicateCount
operator|.
name|addAndGet
argument_list|(
name|replicateContext
operator|.
name|entries
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|success
return|;
block|}
annotation|@
name|Override
specifier|protected
name|Replicator
name|createReplicator
parameter_list|(
name|List
argument_list|<
name|Entry
argument_list|>
name|entries
parameter_list|,
name|int
name|ordinal
parameter_list|)
block|{
comment|// Fail only once, we don't want to slow down the test.
if|if
condition|(
name|failedOnce
condition|)
block|{
return|return
operator|new
name|DummyReplicator
argument_list|(
name|entries
argument_list|,
name|ordinal
argument_list|)
return|;
block|}
else|else
block|{
name|failedOnce
operator|=
literal|true
expr_stmt|;
return|return
operator|new
name|FailingDummyReplicator
argument_list|(
name|entries
argument_list|,
name|ordinal
argument_list|)
return|;
block|}
block|}
specifier|protected
class|class
name|DummyReplicator
extends|extends
name|Replicator
block|{
specifier|private
name|int
name|ordinal
decl_stmt|;
specifier|public
name|DummyReplicator
parameter_list|(
name|List
argument_list|<
name|Entry
argument_list|>
name|entries
parameter_list|,
name|int
name|ordinal
parameter_list|)
block|{
name|super
argument_list|(
name|entries
argument_list|,
name|ordinal
argument_list|)
expr_stmt|;
name|this
operator|.
name|ordinal
operator|=
name|ordinal
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Integer
name|call
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|ordinal
return|;
block|}
block|}
specifier|protected
class|class
name|FailingDummyReplicator
extends|extends
name|DummyReplicator
block|{
specifier|public
name|FailingDummyReplicator
parameter_list|(
name|List
argument_list|<
name|Entry
argument_list|>
name|entries
parameter_list|,
name|int
name|ordinal
parameter_list|)
block|{
name|super
argument_list|(
name|entries
argument_list|,
name|ordinal
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Integer
name|call
parameter_list|()
throws|throws
name|IOException
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Sample Exception: Failed to replicate."
argument_list|)
throw|;
block|}
block|}
block|}
specifier|public
specifier|static
class|class
name|ReplicationEndpointReturningFalse
extends|extends
name|ReplicationEndpointForTest
block|{
specifier|static
name|int
name|COUNT
init|=
literal|10
decl_stmt|;
specifier|static
name|AtomicReference
argument_list|<
name|Exception
argument_list|>
name|ex
init|=
operator|new
name|AtomicReference
argument_list|<
name|Exception
argument_list|>
argument_list|(
literal|null
argument_list|)
decl_stmt|;
specifier|static
name|AtomicBoolean
name|replicated
init|=
operator|new
name|AtomicBoolean
argument_list|(
literal|false
argument_list|)
decl_stmt|;
annotation|@
name|Override
specifier|public
name|boolean
name|replicate
parameter_list|(
name|ReplicateContext
name|replicateContext
parameter_list|)
block|{
try|try
block|{
comment|// check row
name|doAssert
argument_list|(
name|row
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|ex
operator|.
name|set
argument_list|(
name|e
argument_list|)
expr_stmt|;
block|}
name|super
operator|.
name|replicate
argument_list|(
name|replicateContext
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Replicated "
operator|+
name|row
operator|+
literal|", count="
operator|+
name|replicateCount
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
name|replicated
operator|.
name|set
argument_list|(
name|replicateCount
operator|.
name|get
argument_list|()
operator|>
name|COUNT
argument_list|)
expr_stmt|;
comment|// first 10 times, we return false
return|return
name|replicated
operator|.
name|get
argument_list|()
return|;
block|}
block|}
comment|// return a WALEntry filter which only accepts "row", but not other rows
specifier|public
specifier|static
class|class
name|ReplicationEndpointWithWALEntryFilter
extends|extends
name|ReplicationEndpointForTest
block|{
specifier|static
name|AtomicReference
argument_list|<
name|Exception
argument_list|>
name|ex
init|=
operator|new
name|AtomicReference
argument_list|<
name|Exception
argument_list|>
argument_list|(
literal|null
argument_list|)
decl_stmt|;
annotation|@
name|Override
specifier|public
name|boolean
name|replicate
parameter_list|(
name|ReplicateContext
name|replicateContext
parameter_list|)
block|{
try|try
block|{
name|super
operator|.
name|replicate
argument_list|(
name|replicateContext
argument_list|)
expr_stmt|;
name|doAssert
argument_list|(
name|row
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|ex
operator|.
name|set
argument_list|(
name|e
argument_list|)
expr_stmt|;
block|}
return|return
literal|true
return|;
block|}
annotation|@
name|Override
specifier|public
name|WALEntryFilter
name|getWALEntryfilter
parameter_list|()
block|{
return|return
operator|new
name|ChainWALEntryFilter
argument_list|(
name|super
operator|.
name|getWALEntryfilter
argument_list|()
argument_list|,
operator|new
name|WALEntryFilter
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Entry
name|filter
parameter_list|(
name|Entry
name|entry
parameter_list|)
block|{
name|ArrayList
argument_list|<
name|Cell
argument_list|>
name|cells
init|=
name|entry
operator|.
name|getEdit
argument_list|()
operator|.
name|getCells
argument_list|()
decl_stmt|;
name|int
name|size
init|=
name|cells
operator|.
name|size
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
name|size
operator|-
literal|1
init|;
name|i
operator|>=
literal|0
condition|;
name|i
operator|--
control|)
block|{
name|Cell
name|cell
init|=
name|cells
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|Bytes
operator|.
name|equals
argument_list|(
name|cell
operator|.
name|getRowArray
argument_list|()
argument_list|,
name|cell
operator|.
name|getRowOffset
argument_list|()
argument_list|,
name|cell
operator|.
name|getRowLength
argument_list|()
argument_list|,
name|row
argument_list|,
literal|0
argument_list|,
name|row
operator|.
name|length
argument_list|)
condition|)
block|{
name|cells
operator|.
name|remove
argument_list|(
name|i
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|entry
return|;
block|}
block|}
argument_list|)
return|;
block|}
block|}
block|}
end_class

end_unit

