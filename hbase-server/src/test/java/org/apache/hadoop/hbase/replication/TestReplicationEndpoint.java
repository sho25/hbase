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
name|hadoop
operator|.
name|hbase
operator|.
name|KeyValue
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
name|HTable
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
name|regionserver
operator|.
name|wal
operator|.
name|FailedLogCloseException
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
name|wal
operator|.
name|HLog
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
name|wal
operator|.
name|HLog
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
name|zookeeper
operator|.
name|ZKUtil
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
name|MediumTests
operator|.
name|class
argument_list|)
specifier|public
class|class
name|TestReplicationEndpoint
extends|extends
name|TestReplicationBase
block|{
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
name|utility2
operator|.
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
comment|// we don't need the second cluster
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
name|FailedLogCloseException
throws|,
name|IOException
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
name|ReplicationEndpointForTest
operator|.
name|lastEntries
operator|=
literal|null
expr_stmt|;
for|for
control|(
name|RegionServerThread
name|rs
range|:
name|utility1
operator|.
name|getMiniHBaseCluster
argument_list|()
operator|.
name|getRegionServerThreads
argument_list|()
control|)
block|{
name|utility1
operator|.
name|getHBaseAdmin
argument_list|()
operator|.
name|rollHLogWriter
argument_list|(
name|rs
operator|.
name|getRegionServer
argument_list|()
operator|.
name|getServerName
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
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
name|ZKUtil
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
specifier|public
name|void
name|testReplicationEndpointReturnsFalseOnReplicate
parameter_list|()
throws|throws
name|Exception
block|{
name|admin
operator|.
name|addPeer
argument_list|(
literal|"testReplicationEndpointReturnsFalseOnReplicate"
argument_list|,
operator|new
name|ReplicationPeerConfig
argument_list|()
operator|.
name|setClusterKey
argument_list|(
name|ZKUtil
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
comment|// now replicate some data.
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
name|ZKUtil
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
name|doPut
argument_list|(
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
name|row
argument_list|)
expr_stmt|;
name|doPut
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"row2"
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
name|add
argument_list|(
name|famName
argument_list|,
name|row
argument_list|,
name|row
argument_list|)
expr_stmt|;
name|htable1
operator|=
operator|new
name|HTable
argument_list|(
name|conf1
argument_list|,
name|tableName
argument_list|)
expr_stmt|;
name|htable1
operator|.
name|put
argument_list|(
name|put
argument_list|)
expr_stmt|;
name|htable1
operator|.
name|close
argument_list|()
expr_stmt|;
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
name|KeyValue
argument_list|>
name|kvs
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
name|getKeyValues
argument_list|()
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|kvs
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
name|kvs
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getRowArray
argument_list|()
argument_list|,
name|kvs
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getRowOffset
argument_list|()
argument_list|,
name|kvs
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
name|HLog
operator|.
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
name|ReplicationEndpointReturningFalse
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
name|replicated
operator|.
name|set
argument_list|(
name|replicateCount
operator|.
name|get
argument_list|()
operator|>
literal|10
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
name|KeyValue
argument_list|>
name|kvs
init|=
name|entry
operator|.
name|getEdit
argument_list|()
operator|.
name|getKeyValues
argument_list|()
decl_stmt|;
name|int
name|size
init|=
name|kvs
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
name|KeyValue
name|kv
init|=
name|kvs
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
name|kv
operator|.
name|getRowArray
argument_list|()
argument_list|,
name|kv
operator|.
name|getRowOffset
argument_list|()
argument_list|,
name|kv
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
name|kvs
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

