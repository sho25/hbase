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
name|assertTrue
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
name|fail
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
name|ArrayList
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|regex
operator|.
name|Pattern
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
name|fs
operator|.
name|FileSystem
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
name|fs
operator|.
name|Path
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
name|DoNotRetryIOException
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
name|HBaseZKTestingUtility
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
name|Waiter
operator|.
name|ExplainingPredicate
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
name|ColumnFamilyDescriptorBuilder
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
name|client
operator|.
name|TableDescriptor
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
name|TableDescriptorBuilder
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
name|MasterFileSystem
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
name|ReplicationProtbufUtil
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
name|wal
operator|.
name|WALKeyImpl
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
name|BeforeClass
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hbase
operator|.
name|thirdparty
operator|.
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|ImmutableMap
import|;
end_import

begin_comment
comment|/**  * Base class for testing sync replication.  */
end_comment

begin_class
specifier|public
class|class
name|SyncReplicationTestBase
block|{
specifier|protected
specifier|static
specifier|final
name|HBaseZKTestingUtility
name|ZK_UTIL
init|=
operator|new
name|HBaseZKTestingUtility
argument_list|()
decl_stmt|;
specifier|protected
specifier|static
specifier|final
name|HBaseTestingUtility
name|UTIL1
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
specifier|protected
specifier|static
specifier|final
name|HBaseTestingUtility
name|UTIL2
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
specifier|protected
specifier|static
name|TableName
name|TABLE_NAME
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"SyncRep"
argument_list|)
decl_stmt|;
specifier|protected
specifier|static
name|byte
index|[]
name|CF
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"cf"
argument_list|)
decl_stmt|;
specifier|protected
specifier|static
name|byte
index|[]
name|CQ
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"cq"
argument_list|)
decl_stmt|;
specifier|protected
specifier|static
name|String
name|PEER_ID
init|=
literal|"1"
decl_stmt|;
specifier|protected
specifier|static
name|Path
name|REMOTE_WAL_DIR1
decl_stmt|;
specifier|protected
specifier|static
name|Path
name|REMOTE_WAL_DIR2
decl_stmt|;
specifier|protected
specifier|static
name|void
name|initTestingUtility
parameter_list|(
name|HBaseTestingUtility
name|util
parameter_list|,
name|String
name|zkParent
parameter_list|)
block|{
name|util
operator|.
name|setZkCluster
argument_list|(
name|ZK_UTIL
operator|.
name|getZkCluster
argument_list|()
argument_list|)
expr_stmt|;
name|Configuration
name|conf
init|=
name|util
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
name|conf
operator|.
name|set
argument_list|(
name|HConstants
operator|.
name|ZOOKEEPER_ZNODE_PARENT
argument_list|,
name|zkParent
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setInt
argument_list|(
literal|"replication.source.size.capacity"
argument_list|,
literal|102400
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setLong
argument_list|(
literal|"replication.source.sleepforretries"
argument_list|,
literal|100
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setInt
argument_list|(
literal|"hbase.regionserver.maxlogs"
argument_list|,
literal|10
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setLong
argument_list|(
literal|"hbase.master.logcleaner.ttl"
argument_list|,
literal|10
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setInt
argument_list|(
literal|"zookeeper.recovery.retry"
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setInt
argument_list|(
literal|"zookeeper.recovery.retry.intervalmill"
argument_list|,
literal|10
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setLong
argument_list|(
name|HConstants
operator|.
name|THREAD_WAKE_FREQUENCY
argument_list|,
literal|100
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setInt
argument_list|(
literal|"replication.stats.thread.period.seconds"
argument_list|,
literal|5
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setBoolean
argument_list|(
literal|"hbase.tests.use.shortcircuit.reads"
argument_list|,
literal|false
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
name|conf
operator|.
name|setFloat
argument_list|(
literal|"replication.source.ratio"
argument_list|,
literal|1.0f
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setBoolean
argument_list|(
literal|"replication.source.eof.autorecovery"
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
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
name|ZK_UTIL
operator|.
name|startMiniZKCluster
argument_list|()
expr_stmt|;
name|initTestingUtility
argument_list|(
name|UTIL1
argument_list|,
literal|"/cluster1"
argument_list|)
expr_stmt|;
name|initTestingUtility
argument_list|(
name|UTIL2
argument_list|,
literal|"/cluster2"
argument_list|)
expr_stmt|;
name|UTIL1
operator|.
name|startMiniCluster
argument_list|(
literal|2
argument_list|,
literal|3
argument_list|)
expr_stmt|;
name|UTIL2
operator|.
name|startMiniCluster
argument_list|(
literal|2
argument_list|,
literal|3
argument_list|)
expr_stmt|;
name|TableDescriptor
name|td
init|=
name|TableDescriptorBuilder
operator|.
name|newBuilder
argument_list|(
name|TABLE_NAME
argument_list|)
operator|.
name|setColumnFamily
argument_list|(
name|ColumnFamilyDescriptorBuilder
operator|.
name|newBuilder
argument_list|(
name|CF
argument_list|)
operator|.
name|setScope
argument_list|(
name|HConstants
operator|.
name|REPLICATION_SCOPE_GLOBAL
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|UTIL1
operator|.
name|getAdmin
argument_list|()
operator|.
name|createTable
argument_list|(
name|td
argument_list|)
expr_stmt|;
name|UTIL2
operator|.
name|getAdmin
argument_list|()
operator|.
name|createTable
argument_list|(
name|td
argument_list|)
expr_stmt|;
name|FileSystem
name|fs1
init|=
name|UTIL1
operator|.
name|getTestFileSystem
argument_list|()
decl_stmt|;
name|FileSystem
name|fs2
init|=
name|UTIL2
operator|.
name|getTestFileSystem
argument_list|()
decl_stmt|;
name|REMOTE_WAL_DIR1
operator|=
operator|new
name|Path
argument_list|(
name|UTIL1
operator|.
name|getMiniHBaseCluster
argument_list|()
operator|.
name|getMaster
argument_list|()
operator|.
name|getMasterFileSystem
argument_list|()
operator|.
name|getWALRootDir
argument_list|()
argument_list|,
literal|"remoteWALs"
argument_list|)
operator|.
name|makeQualified
argument_list|(
name|fs1
operator|.
name|getUri
argument_list|()
argument_list|,
name|fs1
operator|.
name|getWorkingDirectory
argument_list|()
argument_list|)
expr_stmt|;
name|REMOTE_WAL_DIR2
operator|=
operator|new
name|Path
argument_list|(
name|UTIL2
operator|.
name|getMiniHBaseCluster
argument_list|()
operator|.
name|getMaster
argument_list|()
operator|.
name|getMasterFileSystem
argument_list|()
operator|.
name|getWALRootDir
argument_list|()
argument_list|,
literal|"remoteWALs"
argument_list|)
operator|.
name|makeQualified
argument_list|(
name|fs2
operator|.
name|getUri
argument_list|()
argument_list|,
name|fs2
operator|.
name|getWorkingDirectory
argument_list|()
argument_list|)
expr_stmt|;
name|UTIL1
operator|.
name|getAdmin
argument_list|()
operator|.
name|addReplicationPeer
argument_list|(
name|PEER_ID
argument_list|,
name|ReplicationPeerConfig
operator|.
name|newBuilder
argument_list|()
operator|.
name|setClusterKey
argument_list|(
name|UTIL2
operator|.
name|getClusterKey
argument_list|()
argument_list|)
operator|.
name|setReplicateAllUserTables
argument_list|(
literal|false
argument_list|)
operator|.
name|setTableCFsMap
argument_list|(
name|ImmutableMap
operator|.
name|of
argument_list|(
name|TABLE_NAME
argument_list|,
operator|new
name|ArrayList
argument_list|<>
argument_list|()
argument_list|)
argument_list|)
operator|.
name|setRemoteWALDir
argument_list|(
name|REMOTE_WAL_DIR2
operator|.
name|toUri
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
name|UTIL2
operator|.
name|getAdmin
argument_list|()
operator|.
name|addReplicationPeer
argument_list|(
name|PEER_ID
argument_list|,
name|ReplicationPeerConfig
operator|.
name|newBuilder
argument_list|()
operator|.
name|setClusterKey
argument_list|(
name|UTIL1
operator|.
name|getClusterKey
argument_list|()
argument_list|)
operator|.
name|setReplicateAllUserTables
argument_list|(
literal|false
argument_list|)
operator|.
name|setTableCFsMap
argument_list|(
name|ImmutableMap
operator|.
name|of
argument_list|(
name|TABLE_NAME
argument_list|,
operator|new
name|ArrayList
argument_list|<>
argument_list|()
argument_list|)
argument_list|)
operator|.
name|setRemoteWALDir
argument_list|(
name|REMOTE_WAL_DIR1
operator|.
name|toUri
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|static
name|void
name|shutdown
parameter_list|(
name|HBaseTestingUtility
name|util
parameter_list|)
throws|throws
name|Exception
block|{
if|if
condition|(
name|util
operator|.
name|getHBaseCluster
argument_list|()
operator|==
literal|null
condition|)
block|{
return|return;
block|}
name|Admin
name|admin
init|=
name|util
operator|.
name|getAdmin
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|admin
operator|.
name|listReplicationPeers
argument_list|(
name|Pattern
operator|.
name|compile
argument_list|(
name|PEER_ID
argument_list|)
argument_list|)
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
if|if
condition|(
name|admin
operator|.
name|getReplicationPeerSyncReplicationState
argument_list|(
name|PEER_ID
argument_list|)
operator|!=
name|SyncReplicationState
operator|.
name|DOWNGRADE_ACTIVE
condition|)
block|{
name|admin
operator|.
name|transitReplicationPeerSyncReplicationState
argument_list|(
name|PEER_ID
argument_list|,
name|SyncReplicationState
operator|.
name|DOWNGRADE_ACTIVE
argument_list|)
expr_stmt|;
block|}
name|admin
operator|.
name|removeReplicationPeer
argument_list|(
name|PEER_ID
argument_list|)
expr_stmt|;
block|}
name|util
operator|.
name|shutdownMiniCluster
argument_list|()
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
name|shutdown
argument_list|(
name|UTIL1
argument_list|)
expr_stmt|;
name|shutdown
argument_list|(
name|UTIL2
argument_list|)
expr_stmt|;
name|ZK_UTIL
operator|.
name|shutdownMiniZKCluster
argument_list|()
expr_stmt|;
block|}
specifier|protected
specifier|final
name|void
name|write
parameter_list|(
name|HBaseTestingUtility
name|util
parameter_list|,
name|int
name|start
parameter_list|,
name|int
name|end
parameter_list|)
throws|throws
name|IOException
block|{
try|try
init|(
name|Table
name|table
init|=
name|util
operator|.
name|getConnection
argument_list|()
operator|.
name|getTable
argument_list|(
name|TABLE_NAME
argument_list|)
init|)
block|{
for|for
control|(
name|int
name|i
init|=
name|start
init|;
name|i
operator|<
name|end
condition|;
name|i
operator|++
control|)
block|{
name|table
operator|.
name|put
argument_list|(
operator|new
name|Put
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|i
argument_list|)
argument_list|)
operator|.
name|addColumn
argument_list|(
name|CF
argument_list|,
name|CQ
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|i
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|protected
specifier|final
name|void
name|verify
parameter_list|(
name|HBaseTestingUtility
name|util
parameter_list|,
name|int
name|start
parameter_list|,
name|int
name|end
parameter_list|)
throws|throws
name|IOException
block|{
try|try
init|(
name|Table
name|table
init|=
name|util
operator|.
name|getConnection
argument_list|()
operator|.
name|getTable
argument_list|(
name|TABLE_NAME
argument_list|)
init|)
block|{
for|for
control|(
name|int
name|i
init|=
name|start
init|;
name|i
operator|<
name|end
condition|;
name|i
operator|++
control|)
block|{
name|assertEquals
argument_list|(
name|i
argument_list|,
name|Bytes
operator|.
name|toInt
argument_list|(
name|table
operator|.
name|get
argument_list|(
operator|new
name|Get
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|i
argument_list|)
argument_list|)
argument_list|)
operator|.
name|getValue
argument_list|(
name|CF
argument_list|,
name|CQ
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|protected
specifier|final
name|void
name|verifyThroughRegion
parameter_list|(
name|HBaseTestingUtility
name|util
parameter_list|,
name|int
name|start
parameter_list|,
name|int
name|end
parameter_list|)
throws|throws
name|IOException
block|{
name|HRegion
name|region
init|=
name|util
operator|.
name|getMiniHBaseCluster
argument_list|()
operator|.
name|getRegions
argument_list|(
name|TABLE_NAME
argument_list|)
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
name|start
init|;
name|i
operator|<
name|end
condition|;
name|i
operator|++
control|)
block|{
name|assertEquals
argument_list|(
name|i
argument_list|,
name|Bytes
operator|.
name|toInt
argument_list|(
name|region
operator|.
name|get
argument_list|(
operator|new
name|Get
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|i
argument_list|)
argument_list|)
argument_list|)
operator|.
name|getValue
argument_list|(
name|CF
argument_list|,
name|CQ
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
specifier|protected
specifier|final
name|void
name|verifyNotReplicatedThroughRegion
parameter_list|(
name|HBaseTestingUtility
name|util
parameter_list|,
name|int
name|start
parameter_list|,
name|int
name|end
parameter_list|)
throws|throws
name|IOException
block|{
name|HRegion
name|region
init|=
name|util
operator|.
name|getMiniHBaseCluster
argument_list|()
operator|.
name|getRegions
argument_list|(
name|TABLE_NAME
argument_list|)
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
name|start
init|;
name|i
operator|<
name|end
condition|;
name|i
operator|++
control|)
block|{
name|assertTrue
argument_list|(
name|region
operator|.
name|get
argument_list|(
operator|new
name|Get
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|i
argument_list|)
argument_list|)
argument_list|)
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
specifier|protected
specifier|final
name|void
name|waitUntilReplicationDone
parameter_list|(
name|HBaseTestingUtility
name|util
parameter_list|,
name|int
name|end
parameter_list|)
throws|throws
name|Exception
block|{
comment|// The reject check is in RSRpcService so we can still read through HRegion
name|HRegion
name|region
init|=
name|util
operator|.
name|getMiniHBaseCluster
argument_list|()
operator|.
name|getRegions
argument_list|(
name|TABLE_NAME
argument_list|)
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|util
operator|.
name|waitFor
argument_list|(
literal|30000
argument_list|,
operator|new
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
operator|!
name|region
operator|.
name|get
argument_list|(
operator|new
name|Get
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|end
operator|-
literal|1
argument_list|)
argument_list|)
argument_list|)
operator|.
name|isEmpty
argument_list|()
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
return|return
literal|"Replication has not been catched up yet"
return|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
specifier|protected
specifier|final
name|void
name|writeAndVerifyReplication
parameter_list|(
name|HBaseTestingUtility
name|util1
parameter_list|,
name|HBaseTestingUtility
name|util2
parameter_list|,
name|int
name|start
parameter_list|,
name|int
name|end
parameter_list|)
throws|throws
name|Exception
block|{
name|write
argument_list|(
name|util1
argument_list|,
name|start
argument_list|,
name|end
argument_list|)
expr_stmt|;
name|waitUntilReplicationDone
argument_list|(
name|util2
argument_list|,
name|end
argument_list|)
expr_stmt|;
name|verifyThroughRegion
argument_list|(
name|util2
argument_list|,
name|start
argument_list|,
name|end
argument_list|)
expr_stmt|;
block|}
specifier|protected
specifier|final
name|Path
name|getRemoteWALDir
parameter_list|(
name|MasterFileSystem
name|mfs
parameter_list|,
name|String
name|peerId
parameter_list|)
block|{
name|Path
name|remoteWALDir
init|=
operator|new
name|Path
argument_list|(
name|mfs
operator|.
name|getWALRootDir
argument_list|()
argument_list|,
name|ReplicationUtils
operator|.
name|REMOTE_WAL_DIR_NAME
argument_list|)
decl_stmt|;
return|return
name|getRemoteWALDir
argument_list|(
name|remoteWALDir
argument_list|,
name|peerId
argument_list|)
return|;
block|}
specifier|protected
name|Path
name|getRemoteWALDir
parameter_list|(
name|Path
name|remoteWALDir
parameter_list|,
name|String
name|peerId
parameter_list|)
block|{
return|return
operator|new
name|Path
argument_list|(
name|remoteWALDir
argument_list|,
name|peerId
argument_list|)
return|;
block|}
specifier|protected
name|Path
name|getReplayRemoteWALs
parameter_list|(
name|Path
name|remoteWALDir
parameter_list|,
name|String
name|peerId
parameter_list|)
block|{
return|return
operator|new
name|Path
argument_list|(
name|remoteWALDir
argument_list|,
name|peerId
operator|+
literal|"-replay"
argument_list|)
return|;
block|}
specifier|protected
name|void
name|verifyRemovedPeer
parameter_list|(
name|String
name|peerId
parameter_list|,
name|Path
name|remoteWALDir
parameter_list|,
name|HBaseTestingUtility
name|utility
parameter_list|)
throws|throws
name|Exception
block|{
name|ReplicationPeerStorage
name|rps
init|=
name|ReplicationStorageFactory
operator|.
name|getReplicationPeerStorage
argument_list|(
name|utility
operator|.
name|getZooKeeperWatcher
argument_list|()
argument_list|,
name|utility
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
try|try
block|{
name|rps
operator|.
name|getPeerSyncReplicationState
argument_list|(
name|peerId
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"Should throw exception when get the sync replication state of a removed peer."
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|NullPointerException
name|e
parameter_list|)
block|{
comment|// ignore.
block|}
try|try
block|{
name|rps
operator|.
name|getPeerNewSyncReplicationState
argument_list|(
name|peerId
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"Should throw exception when get the new sync replication state of a removed peer"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|NullPointerException
name|e
parameter_list|)
block|{
comment|// ignore.
block|}
try|try
init|(
name|FileSystem
name|fs
init|=
name|utility
operator|.
name|getTestFileSystem
argument_list|()
init|)
block|{
name|Assert
operator|.
name|assertFalse
argument_list|(
name|fs
operator|.
name|exists
argument_list|(
name|getRemoteWALDir
argument_list|(
name|remoteWALDir
argument_list|,
name|peerId
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertFalse
argument_list|(
name|fs
operator|.
name|exists
argument_list|(
name|getReplayRemoteWALs
argument_list|(
name|remoteWALDir
argument_list|,
name|peerId
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
specifier|protected
name|void
name|verifyReplicationRequestRejection
parameter_list|(
name|HBaseTestingUtility
name|utility
parameter_list|,
name|boolean
name|expectedRejection
parameter_list|)
throws|throws
name|Exception
block|{
name|HRegionServer
name|regionServer
init|=
name|utility
operator|.
name|getRSForFirstRegionInTable
argument_list|(
name|TABLE_NAME
argument_list|)
decl_stmt|;
name|ClusterConnection
name|connection
init|=
name|regionServer
operator|.
name|getClusterConnection
argument_list|()
decl_stmt|;
name|Entry
index|[]
name|entries
init|=
operator|new
name|Entry
index|[
literal|10
index|]
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
name|entries
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|entries
index|[
name|i
index|]
operator|=
operator|new
name|Entry
argument_list|(
operator|new
name|WALKeyImpl
argument_list|(
name|HConstants
operator|.
name|EMPTY_BYTE_ARRAY
argument_list|,
name|TABLE_NAME
argument_list|,
literal|0
argument_list|)
argument_list|,
operator|new
name|WALEdit
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|expectedRejection
condition|)
block|{
name|ReplicationProtbufUtil
operator|.
name|replicateWALEntry
argument_list|(
name|connection
operator|.
name|getAdmin
argument_list|(
name|regionServer
operator|.
name|getServerName
argument_list|()
argument_list|)
argument_list|,
name|entries
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
else|else
block|{
try|try
block|{
name|ReplicationProtbufUtil
operator|.
name|replicateWALEntry
argument_list|(
name|connection
operator|.
name|getAdmin
argument_list|(
name|regionServer
operator|.
name|getServerName
argument_list|()
argument_list|)
argument_list|,
name|entries
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|fail
argument_list|(
literal|"Should throw IOException when sync-replication state is in A or DA"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|DoNotRetryIOException
name|e
parameter_list|)
block|{
name|Assert
operator|.
name|assertTrue
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
operator|.
name|contains
argument_list|(
literal|"Reject to apply to sink cluster"
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
operator|.
name|contains
argument_list|(
name|TABLE_NAME
operator|.
name|toString
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit

