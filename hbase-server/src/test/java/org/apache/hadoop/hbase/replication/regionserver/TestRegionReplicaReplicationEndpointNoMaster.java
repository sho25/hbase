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
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|regionserver
operator|.
name|TestRegionServerNoMaster
operator|.
name|closeRegion
import|;
end_import

begin_import
import|import static
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
name|TestRegionServerNoMaster
operator|.
name|openRegion
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
name|Queue
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
name|ConcurrentLinkedQueue
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
name|AtomicLong
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
name|CellUtil
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
name|HTableDescriptor
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
name|RegionLocations
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
name|RegionLocator
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
name|RpcRetryingCallerFactory
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
name|coprocessor
operator|.
name|BaseWALObserver
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
name|coprocessor
operator|.
name|CoprocessorHost
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
name|coprocessor
operator|.
name|ObserverContext
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
name|coprocessor
operator|.
name|WALCoprocessorEnvironment
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
name|ipc
operator|.
name|RpcControllerFactory
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
name|ReplicateWALEntryResponse
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
name|HRegionServer
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
name|Region
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
name|TestRegionServerNoMaster
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
name|WALEdit
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
name|ReplicationEndpoint
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
name|ReplicationEndpoint
operator|.
name|ReplicateContext
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
name|ReplicationPeer
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
name|WALEntryFilter
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
name|RegionReplicaReplicationEndpoint
operator|.
name|RegionReplicaReplayCallable
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
name|ServerRegionReplicaUtil
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
name|wal
operator|.
name|WALKey
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

begin_comment
comment|/**  * Tests RegionReplicaReplicationEndpoint. Unlike TestRegionReplicaReplicationEndpoint this  * class contains lower level tests using callables.  */
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
name|TestRegionReplicaReplicationEndpointNoMaster
block|{
specifier|private
specifier|static
specifier|final
name|int
name|NB_SERVERS
init|=
literal|2
decl_stmt|;
specifier|private
specifier|static
name|TableName
name|tableName
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
name|TestRegionReplicaReplicationEndpointNoMaster
operator|.
name|class
operator|.
name|getSimpleName
argument_list|()
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|Table
name|table
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|row
init|=
literal|"TestRegionReplicaReplicator"
operator|.
name|getBytes
argument_list|()
decl_stmt|;
specifier|private
specifier|static
name|HRegionServer
name|rs0
decl_stmt|;
specifier|private
specifier|static
name|HRegionServer
name|rs1
decl_stmt|;
specifier|private
specifier|static
name|HRegionInfo
name|hriPrimary
decl_stmt|;
specifier|private
specifier|static
name|HRegionInfo
name|hriSecondary
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|HBaseTestingUtility
name|HTU
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|f
init|=
name|HConstants
operator|.
name|CATALOG_FAMILY
decl_stmt|;
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|beforeClass
parameter_list|()
throws|throws
name|Exception
block|{
name|Configuration
name|conf
init|=
name|HTU
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
name|conf
operator|.
name|setBoolean
argument_list|(
name|ServerRegionReplicaUtil
operator|.
name|REGION_REPLICA_REPLICATION_CONF_KEY
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setBoolean
argument_list|(
name|ServerRegionReplicaUtil
operator|.
name|REGION_REPLICA_WAIT_FOR_PRIMARY_FLUSH_CONF_KEY
argument_list|,
literal|false
argument_list|)
expr_stmt|;
comment|// install WALObserver coprocessor for tests
name|String
name|walCoprocs
init|=
name|HTU
operator|.
name|getConfiguration
argument_list|()
operator|.
name|get
argument_list|(
name|CoprocessorHost
operator|.
name|WAL_COPROCESSOR_CONF_KEY
argument_list|)
decl_stmt|;
if|if
condition|(
name|walCoprocs
operator|==
literal|null
condition|)
block|{
name|walCoprocs
operator|=
name|WALEditCopro
operator|.
name|class
operator|.
name|getName
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|walCoprocs
operator|+=
literal|","
operator|+
name|WALEditCopro
operator|.
name|class
operator|.
name|getName
argument_list|()
expr_stmt|;
block|}
name|HTU
operator|.
name|getConfiguration
argument_list|()
operator|.
name|set
argument_list|(
name|CoprocessorHost
operator|.
name|WAL_COPROCESSOR_CONF_KEY
argument_list|,
name|walCoprocs
argument_list|)
expr_stmt|;
name|HTU
operator|.
name|startMiniCluster
argument_list|(
name|NB_SERVERS
argument_list|)
expr_stmt|;
comment|// Create table then get the single region for our new table.
name|HTableDescriptor
name|htd
init|=
name|HTU
operator|.
name|createTableDescriptor
argument_list|(
name|tableName
operator|.
name|getNameAsString
argument_list|()
argument_list|)
decl_stmt|;
name|table
operator|=
name|HTU
operator|.
name|createTable
argument_list|(
name|htd
argument_list|,
operator|new
name|byte
index|[]
index|[]
block|{
name|f
block|}
argument_list|,
literal|null
argument_list|)
expr_stmt|;
try|try
init|(
name|RegionLocator
name|locator
init|=
name|HTU
operator|.
name|getConnection
argument_list|()
operator|.
name|getRegionLocator
argument_list|(
name|tableName
argument_list|)
init|)
block|{
name|hriPrimary
operator|=
name|locator
operator|.
name|getRegionLocation
argument_list|(
name|row
argument_list|,
literal|false
argument_list|)
operator|.
name|getRegionInfo
argument_list|()
expr_stmt|;
block|}
comment|// mock a secondary region info to open
name|hriSecondary
operator|=
operator|new
name|HRegionInfo
argument_list|(
name|hriPrimary
operator|.
name|getTable
argument_list|()
argument_list|,
name|hriPrimary
operator|.
name|getStartKey
argument_list|()
argument_list|,
name|hriPrimary
operator|.
name|getEndKey
argument_list|()
argument_list|,
name|hriPrimary
operator|.
name|isSplit
argument_list|()
argument_list|,
name|hriPrimary
operator|.
name|getRegionId
argument_list|()
argument_list|,
literal|1
argument_list|)
expr_stmt|;
comment|// No master
name|TestRegionServerNoMaster
operator|.
name|stopMasterAndAssignMeta
argument_list|(
name|HTU
argument_list|)
expr_stmt|;
name|rs0
operator|=
name|HTU
operator|.
name|getMiniHBaseCluster
argument_list|()
operator|.
name|getRegionServer
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|rs1
operator|=
name|HTU
operator|.
name|getMiniHBaseCluster
argument_list|()
operator|.
name|getRegionServer
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
name|afterClass
parameter_list|()
throws|throws
name|Exception
block|{
name|table
operator|.
name|close
argument_list|()
expr_stmt|;
name|HTU
operator|.
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Before
specifier|public
name|void
name|before
parameter_list|()
throws|throws
name|Exception
block|{
name|entries
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
annotation|@
name|After
specifier|public
name|void
name|after
parameter_list|()
throws|throws
name|Exception
block|{   }
specifier|static
name|ConcurrentLinkedQueue
argument_list|<
name|Entry
argument_list|>
name|entries
init|=
operator|new
name|ConcurrentLinkedQueue
argument_list|<
name|Entry
argument_list|>
argument_list|()
decl_stmt|;
specifier|public
specifier|static
class|class
name|WALEditCopro
extends|extends
name|BaseWALObserver
block|{
specifier|public
name|WALEditCopro
parameter_list|()
block|{
name|entries
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|postWALWrite
parameter_list|(
name|ObserverContext
argument_list|<
name|?
extends|extends
name|WALCoprocessorEnvironment
argument_list|>
name|ctx
parameter_list|,
name|HRegionInfo
name|info
parameter_list|,
name|WALKey
name|logKey
parameter_list|,
name|WALEdit
name|logEdit
parameter_list|)
throws|throws
name|IOException
block|{
comment|// only keep primary region's edits
if|if
condition|(
name|logKey
operator|.
name|getTablename
argument_list|()
operator|.
name|equals
argument_list|(
name|tableName
argument_list|)
operator|&&
name|info
operator|.
name|getReplicaId
argument_list|()
operator|==
literal|0
condition|)
block|{
name|entries
operator|.
name|add
argument_list|(
operator|new
name|Entry
argument_list|(
name|logKey
argument_list|,
name|logEdit
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|240000
argument_list|)
specifier|public
name|void
name|testReplayCallable
parameter_list|()
throws|throws
name|Exception
block|{
comment|// tests replaying the edits to a secondary region replica using the Callable directly
name|openRegion
argument_list|(
name|HTU
argument_list|,
name|rs0
argument_list|,
name|hriSecondary
argument_list|)
expr_stmt|;
name|ClusterConnection
name|connection
init|=
operator|(
name|ClusterConnection
operator|)
name|ConnectionFactory
operator|.
name|createConnection
argument_list|(
name|HTU
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
comment|//load some data to primary
name|HTU
operator|.
name|loadNumericRows
argument_list|(
name|table
argument_list|,
name|f
argument_list|,
literal|0
argument_list|,
literal|1000
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|1000
argument_list|,
name|entries
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
comment|// replay the edits to the secondary using replay callable
name|replicateUsingCallable
argument_list|(
name|connection
argument_list|,
name|entries
argument_list|)
expr_stmt|;
name|Region
name|region
init|=
name|rs0
operator|.
name|getFromOnlineRegions
argument_list|(
name|hriSecondary
operator|.
name|getEncodedName
argument_list|()
argument_list|)
decl_stmt|;
name|HTU
operator|.
name|verifyNumericRows
argument_list|(
name|region
argument_list|,
name|f
argument_list|,
literal|0
argument_list|,
literal|1000
argument_list|)
expr_stmt|;
name|HTU
operator|.
name|deleteNumericRows
argument_list|(
name|table
argument_list|,
name|f
argument_list|,
literal|0
argument_list|,
literal|1000
argument_list|)
expr_stmt|;
name|closeRegion
argument_list|(
name|HTU
argument_list|,
name|rs0
argument_list|,
name|hriSecondary
argument_list|)
expr_stmt|;
name|connection
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
specifier|private
name|void
name|replicateUsingCallable
parameter_list|(
name|ClusterConnection
name|connection
parameter_list|,
name|Queue
argument_list|<
name|Entry
argument_list|>
name|entries
parameter_list|)
throws|throws
name|IOException
throws|,
name|RuntimeException
block|{
name|Entry
name|entry
decl_stmt|;
while|while
condition|(
operator|(
name|entry
operator|=
name|entries
operator|.
name|poll
argument_list|()
operator|)
operator|!=
literal|null
condition|)
block|{
name|byte
index|[]
name|row
init|=
name|CellUtil
operator|.
name|cloneRow
argument_list|(
name|entry
operator|.
name|getEdit
argument_list|()
operator|.
name|getCells
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
decl_stmt|;
name|RegionLocations
name|locations
init|=
name|connection
operator|.
name|locateRegion
argument_list|(
name|tableName
argument_list|,
name|row
argument_list|,
literal|true
argument_list|,
literal|true
argument_list|)
decl_stmt|;
name|RegionReplicaReplayCallable
name|callable
init|=
operator|new
name|RegionReplicaReplayCallable
argument_list|(
name|connection
argument_list|,
name|RpcControllerFactory
operator|.
name|instantiate
argument_list|(
name|connection
operator|.
name|getConfiguration
argument_list|()
argument_list|)
argument_list|,
name|table
operator|.
name|getName
argument_list|()
argument_list|,
name|locations
operator|.
name|getRegionLocation
argument_list|(
literal|1
argument_list|)
argument_list|,
name|locations
operator|.
name|getRegionLocation
argument_list|(
literal|1
argument_list|)
operator|.
name|getRegionInfo
argument_list|()
argument_list|,
name|row
argument_list|,
name|Lists
operator|.
name|newArrayList
argument_list|(
name|entry
argument_list|)
argument_list|,
operator|new
name|AtomicLong
argument_list|()
argument_list|)
decl_stmt|;
name|RpcRetryingCallerFactory
name|factory
init|=
name|RpcRetryingCallerFactory
operator|.
name|instantiate
argument_list|(
name|connection
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
name|factory
operator|.
expr|<
name|ReplicateWALEntryResponse
operator|>
name|newCaller
argument_list|()
operator|.
name|callWithRetries
argument_list|(
name|callable
argument_list|,
literal|10000
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|240000
argument_list|)
specifier|public
name|void
name|testReplayCallableWithRegionMove
parameter_list|()
throws|throws
name|Exception
block|{
comment|// tests replaying the edits to a secondary region replica using the Callable directly while
comment|// the region is moved to another location.It tests handling of RME.
name|openRegion
argument_list|(
name|HTU
argument_list|,
name|rs0
argument_list|,
name|hriSecondary
argument_list|)
expr_stmt|;
name|ClusterConnection
name|connection
init|=
operator|(
name|ClusterConnection
operator|)
name|ConnectionFactory
operator|.
name|createConnection
argument_list|(
name|HTU
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
comment|//load some data to primary
name|HTU
operator|.
name|loadNumericRows
argument_list|(
name|table
argument_list|,
name|f
argument_list|,
literal|0
argument_list|,
literal|1000
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|1000
argument_list|,
name|entries
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
comment|// replay the edits to the secondary using replay callable
name|replicateUsingCallable
argument_list|(
name|connection
argument_list|,
name|entries
argument_list|)
expr_stmt|;
name|Region
name|region
init|=
name|rs0
operator|.
name|getFromOnlineRegions
argument_list|(
name|hriSecondary
operator|.
name|getEncodedName
argument_list|()
argument_list|)
decl_stmt|;
name|HTU
operator|.
name|verifyNumericRows
argument_list|(
name|region
argument_list|,
name|f
argument_list|,
literal|0
argument_list|,
literal|1000
argument_list|)
expr_stmt|;
name|HTU
operator|.
name|loadNumericRows
argument_list|(
name|table
argument_list|,
name|f
argument_list|,
literal|1000
argument_list|,
literal|2000
argument_list|)
expr_stmt|;
comment|// load some more data to primary
comment|// move the secondary region from RS0 to RS1
name|closeRegion
argument_list|(
name|HTU
argument_list|,
name|rs0
argument_list|,
name|hriSecondary
argument_list|)
expr_stmt|;
name|openRegion
argument_list|(
name|HTU
argument_list|,
name|rs1
argument_list|,
name|hriSecondary
argument_list|)
expr_stmt|;
comment|// replicate the new data
name|replicateUsingCallable
argument_list|(
name|connection
argument_list|,
name|entries
argument_list|)
expr_stmt|;
name|region
operator|=
name|rs1
operator|.
name|getFromOnlineRegions
argument_list|(
name|hriSecondary
operator|.
name|getEncodedName
argument_list|()
argument_list|)
expr_stmt|;
comment|// verify the new data. old data may or may not be there
name|HTU
operator|.
name|verifyNumericRows
argument_list|(
name|region
argument_list|,
name|f
argument_list|,
literal|1000
argument_list|,
literal|2000
argument_list|)
expr_stmt|;
name|HTU
operator|.
name|deleteNumericRows
argument_list|(
name|table
argument_list|,
name|f
argument_list|,
literal|0
argument_list|,
literal|2000
argument_list|)
expr_stmt|;
name|closeRegion
argument_list|(
name|HTU
argument_list|,
name|rs1
argument_list|,
name|hriSecondary
argument_list|)
expr_stmt|;
name|connection
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|240000
argument_list|)
specifier|public
name|void
name|testRegionReplicaReplicationEndpointReplicate
parameter_list|()
throws|throws
name|Exception
block|{
comment|// tests replaying the edits to a secondary region replica using the RRRE.replicate()
name|openRegion
argument_list|(
name|HTU
argument_list|,
name|rs0
argument_list|,
name|hriSecondary
argument_list|)
expr_stmt|;
name|ClusterConnection
name|connection
init|=
operator|(
name|ClusterConnection
operator|)
name|ConnectionFactory
operator|.
name|createConnection
argument_list|(
name|HTU
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
name|RegionReplicaReplicationEndpoint
name|replicator
init|=
operator|new
name|RegionReplicaReplicationEndpoint
argument_list|()
decl_stmt|;
name|ReplicationEndpoint
operator|.
name|Context
name|context
init|=
name|mock
argument_list|(
name|ReplicationEndpoint
operator|.
name|Context
operator|.
name|class
argument_list|)
decl_stmt|;
name|when
argument_list|(
name|context
operator|.
name|getConfiguration
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|HTU
operator|.
name|getConfiguration
argument_list|()
argument_list|)
expr_stmt|;
name|when
argument_list|(
name|context
operator|.
name|getMetrics
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|mock
argument_list|(
name|MetricsSource
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|replicator
operator|.
name|init
argument_list|(
name|context
argument_list|)
expr_stmt|;
name|replicator
operator|.
name|start
argument_list|()
expr_stmt|;
comment|//load some data to primary
name|HTU
operator|.
name|loadNumericRows
argument_list|(
name|table
argument_list|,
name|f
argument_list|,
literal|0
argument_list|,
literal|1000
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|1000
argument_list|,
name|entries
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
comment|// replay the edits to the secondary using replay callable
specifier|final
name|String
name|fakeWalGroupId
init|=
literal|"fakeWALGroup"
decl_stmt|;
name|replicator
operator|.
name|replicate
argument_list|(
operator|new
name|ReplicateContext
argument_list|()
operator|.
name|setEntries
argument_list|(
name|Lists
operator|.
name|newArrayList
argument_list|(
name|entries
argument_list|)
argument_list|)
operator|.
name|setWalGroupId
argument_list|(
name|fakeWalGroupId
argument_list|)
argument_list|)
expr_stmt|;
name|Region
name|region
init|=
name|rs0
operator|.
name|getFromOnlineRegions
argument_list|(
name|hriSecondary
operator|.
name|getEncodedName
argument_list|()
argument_list|)
decl_stmt|;
name|HTU
operator|.
name|verifyNumericRows
argument_list|(
name|region
argument_list|,
name|f
argument_list|,
literal|0
argument_list|,
literal|1000
argument_list|)
expr_stmt|;
name|HTU
operator|.
name|deleteNumericRows
argument_list|(
name|table
argument_list|,
name|f
argument_list|,
literal|0
argument_list|,
literal|1000
argument_list|)
expr_stmt|;
name|closeRegion
argument_list|(
name|HTU
argument_list|,
name|rs0
argument_list|,
name|hriSecondary
argument_list|)
expr_stmt|;
name|connection
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|240000
argument_list|)
specifier|public
name|void
name|testReplayedEditsAreSkipped
parameter_list|()
throws|throws
name|Exception
block|{
name|openRegion
argument_list|(
name|HTU
argument_list|,
name|rs0
argument_list|,
name|hriSecondary
argument_list|)
expr_stmt|;
name|ClusterConnection
name|connection
init|=
operator|(
name|ClusterConnection
operator|)
name|ConnectionFactory
operator|.
name|createConnection
argument_list|(
name|HTU
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
name|RegionReplicaReplicationEndpoint
name|replicator
init|=
operator|new
name|RegionReplicaReplicationEndpoint
argument_list|()
decl_stmt|;
name|ReplicationEndpoint
operator|.
name|Context
name|context
init|=
name|mock
argument_list|(
name|ReplicationEndpoint
operator|.
name|Context
operator|.
name|class
argument_list|)
decl_stmt|;
name|when
argument_list|(
name|context
operator|.
name|getConfiguration
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|HTU
operator|.
name|getConfiguration
argument_list|()
argument_list|)
expr_stmt|;
name|when
argument_list|(
name|context
operator|.
name|getMetrics
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|mock
argument_list|(
name|MetricsSource
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|ReplicationPeer
name|mockPeer
init|=
name|mock
argument_list|(
name|ReplicationPeer
operator|.
name|class
argument_list|)
decl_stmt|;
name|when
argument_list|(
name|mockPeer
operator|.
name|getTableCFs
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
literal|null
argument_list|)
expr_stmt|;
name|when
argument_list|(
name|context
operator|.
name|getReplicationPeer
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|mockPeer
argument_list|)
expr_stmt|;
name|replicator
operator|.
name|init
argument_list|(
name|context
argument_list|)
expr_stmt|;
name|replicator
operator|.
name|start
argument_list|()
expr_stmt|;
comment|// test the filter for the RE, not actual replication
name|WALEntryFilter
name|filter
init|=
name|replicator
operator|.
name|getWALEntryfilter
argument_list|()
decl_stmt|;
comment|//load some data to primary
name|HTU
operator|.
name|loadNumericRows
argument_list|(
name|table
argument_list|,
name|f
argument_list|,
literal|0
argument_list|,
literal|1000
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|1000
argument_list|,
name|entries
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|Entry
name|e
range|:
name|entries
control|)
block|{
name|Cell
name|_c
init|=
name|e
operator|.
name|getEdit
argument_list|()
operator|.
name|getCells
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
if|if
condition|(
name|Integer
operator|.
name|parseInt
argument_list|(
name|Bytes
operator|.
name|toString
argument_list|(
name|_c
operator|.
name|getValueArray
argument_list|()
argument_list|,
name|_c
operator|.
name|getValueOffset
argument_list|()
argument_list|,
name|_c
operator|.
name|getValueLength
argument_list|()
argument_list|)
argument_list|)
operator|%
literal|2
operator|==
literal|0
condition|)
block|{
name|e
operator|.
name|getKey
argument_list|()
operator|.
name|setOrigLogSeqNum
argument_list|(
literal|1
argument_list|)
expr_stmt|;
comment|// simulate dist log replay by setting orig seq id
block|}
block|}
name|long
name|skipped
init|=
literal|0
decl_stmt|,
name|replayed
init|=
literal|0
decl_stmt|;
for|for
control|(
name|Entry
name|e
range|:
name|entries
control|)
block|{
if|if
condition|(
name|filter
operator|.
name|filter
argument_list|(
name|e
argument_list|)
operator|==
literal|null
condition|)
block|{
name|skipped
operator|++
expr_stmt|;
block|}
else|else
block|{
name|replayed
operator|++
expr_stmt|;
block|}
block|}
name|assertEquals
argument_list|(
literal|500
argument_list|,
name|skipped
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|500
argument_list|,
name|replayed
argument_list|)
expr_stmt|;
name|HTU
operator|.
name|deleteNumericRows
argument_list|(
name|table
argument_list|,
name|f
argument_list|,
literal|0
argument_list|,
literal|1000
argument_list|)
expr_stmt|;
name|closeRegion
argument_list|(
name|HTU
argument_list|,
name|rs0
argument_list|,
name|hriSecondary
argument_list|)
expr_stmt|;
name|connection
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

