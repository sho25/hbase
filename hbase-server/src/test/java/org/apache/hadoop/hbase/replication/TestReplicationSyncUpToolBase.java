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
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|HConstants
operator|.
name|REPLICATION_SCOPE_GLOBAL
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
name|io
operator|.
name|IOUtils
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
name|replication
operator|.
name|regionserver
operator|.
name|ReplicationSyncUp
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
name|util
operator|.
name|ToolRunner
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
name|Before
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
name|io
operator|.
name|Closeables
import|;
end_import

begin_class
specifier|public
specifier|abstract
class|class
name|TestReplicationSyncUpToolBase
block|{
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
specifier|final
name|TableName
name|TN1
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"t1_syncup"
argument_list|)
decl_stmt|;
specifier|protected
specifier|static
specifier|final
name|TableName
name|TN2
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"t2_syncup"
argument_list|)
decl_stmt|;
specifier|protected
specifier|static
specifier|final
name|byte
index|[]
name|FAMILY
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"cf1"
argument_list|)
decl_stmt|;
specifier|protected
specifier|static
specifier|final
name|byte
index|[]
name|QUALIFIER
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"q1"
argument_list|)
decl_stmt|;
specifier|protected
specifier|static
specifier|final
name|byte
index|[]
name|NO_REP_FAMILY
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"norep"
argument_list|)
decl_stmt|;
specifier|protected
name|TableDescriptor
name|t1SyncupSource
decl_stmt|;
specifier|protected
name|TableDescriptor
name|t1SyncupTarget
decl_stmt|;
specifier|protected
name|TableDescriptor
name|t2SyncupSource
decl_stmt|;
specifier|protected
name|TableDescriptor
name|t2SyncupTarget
decl_stmt|;
specifier|protected
name|Connection
name|conn1
decl_stmt|;
specifier|protected
name|Connection
name|conn2
decl_stmt|;
specifier|protected
name|Table
name|ht1Source
decl_stmt|;
specifier|protected
name|Table
name|ht2Source
decl_stmt|;
specifier|protected
name|Table
name|ht1TargetAtPeer1
decl_stmt|;
specifier|protected
name|Table
name|ht2TargetAtPeer1
decl_stmt|;
specifier|protected
name|void
name|customizeClusterConf
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{   }
annotation|@
name|Before
specifier|public
name|void
name|setUp
parameter_list|()
throws|throws
name|Exception
block|{
name|customizeClusterConf
argument_list|(
name|UTIL1
operator|.
name|getConfiguration
argument_list|()
argument_list|)
expr_stmt|;
name|customizeClusterConf
argument_list|(
name|UTIL2
operator|.
name|getConfiguration
argument_list|()
argument_list|)
expr_stmt|;
name|TestReplicationBase
operator|.
name|configureClusters
argument_list|(
name|UTIL1
argument_list|,
name|UTIL2
argument_list|)
expr_stmt|;
name|UTIL1
operator|.
name|startMiniZKCluster
argument_list|()
expr_stmt|;
name|UTIL2
operator|.
name|setZkCluster
argument_list|(
name|UTIL1
operator|.
name|getZkCluster
argument_list|()
argument_list|)
expr_stmt|;
name|UTIL1
operator|.
name|startMiniCluster
argument_list|(
literal|2
argument_list|)
expr_stmt|;
name|UTIL2
operator|.
name|startMiniCluster
argument_list|(
literal|4
argument_list|)
expr_stmt|;
name|t1SyncupSource
operator|=
name|TableDescriptorBuilder
operator|.
name|newBuilder
argument_list|(
name|TN1
argument_list|)
operator|.
name|setColumnFamily
argument_list|(
name|ColumnFamilyDescriptorBuilder
operator|.
name|newBuilder
argument_list|(
name|FAMILY
argument_list|)
operator|.
name|setScope
argument_list|(
name|REPLICATION_SCOPE_GLOBAL
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
operator|.
name|setColumnFamily
argument_list|(
name|ColumnFamilyDescriptorBuilder
operator|.
name|of
argument_list|(
name|NO_REP_FAMILY
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
name|t1SyncupTarget
operator|=
name|TableDescriptorBuilder
operator|.
name|newBuilder
argument_list|(
name|TN1
argument_list|)
operator|.
name|setColumnFamily
argument_list|(
name|ColumnFamilyDescriptorBuilder
operator|.
name|of
argument_list|(
name|FAMILY
argument_list|)
argument_list|)
operator|.
name|setColumnFamily
argument_list|(
name|ColumnFamilyDescriptorBuilder
operator|.
name|of
argument_list|(
name|NO_REP_FAMILY
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
name|t2SyncupSource
operator|=
name|TableDescriptorBuilder
operator|.
name|newBuilder
argument_list|(
name|TN2
argument_list|)
operator|.
name|setColumnFamily
argument_list|(
name|ColumnFamilyDescriptorBuilder
operator|.
name|newBuilder
argument_list|(
name|FAMILY
argument_list|)
operator|.
name|setScope
argument_list|(
name|REPLICATION_SCOPE_GLOBAL
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
operator|.
name|setColumnFamily
argument_list|(
name|ColumnFamilyDescriptorBuilder
operator|.
name|of
argument_list|(
name|NO_REP_FAMILY
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
name|t2SyncupTarget
operator|=
name|TableDescriptorBuilder
operator|.
name|newBuilder
argument_list|(
name|TN2
argument_list|)
operator|.
name|setColumnFamily
argument_list|(
name|ColumnFamilyDescriptorBuilder
operator|.
name|of
argument_list|(
name|FAMILY
argument_list|)
argument_list|)
operator|.
name|setColumnFamily
argument_list|(
name|ColumnFamilyDescriptorBuilder
operator|.
name|of
argument_list|(
name|NO_REP_FAMILY
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
block|}
annotation|@
name|After
specifier|public
name|void
name|tearDown
parameter_list|()
throws|throws
name|Exception
block|{
name|Closeables
operator|.
name|close
argument_list|(
name|ht1Source
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|Closeables
operator|.
name|close
argument_list|(
name|ht2Source
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|Closeables
operator|.
name|close
argument_list|(
name|ht1TargetAtPeer1
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|Closeables
operator|.
name|close
argument_list|(
name|ht2TargetAtPeer1
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|Closeables
operator|.
name|close
argument_list|(
name|conn1
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|Closeables
operator|.
name|close
argument_list|(
name|conn2
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|UTIL2
operator|.
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
name|UTIL1
operator|.
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
block|}
specifier|final
name|void
name|setupReplication
parameter_list|()
throws|throws
name|Exception
block|{
name|Admin
name|admin1
init|=
name|UTIL1
operator|.
name|getAdmin
argument_list|()
decl_stmt|;
name|admin1
operator|.
name|createTable
argument_list|(
name|t1SyncupSource
argument_list|)
expr_stmt|;
name|admin1
operator|.
name|createTable
argument_list|(
name|t2SyncupSource
argument_list|)
expr_stmt|;
name|Admin
name|admin2
init|=
name|UTIL2
operator|.
name|getAdmin
argument_list|()
decl_stmt|;
name|admin2
operator|.
name|createTable
argument_list|(
name|t1SyncupTarget
argument_list|)
expr_stmt|;
name|admin2
operator|.
name|createTable
argument_list|(
name|t2SyncupTarget
argument_list|)
expr_stmt|;
comment|// Get HTable from Master
name|Connection
name|conn1
init|=
name|ConnectionFactory
operator|.
name|createConnection
argument_list|(
name|UTIL1
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
name|ht1Source
operator|=
name|conn1
operator|.
name|getTable
argument_list|(
name|TN1
argument_list|)
expr_stmt|;
name|ht2Source
operator|=
name|conn1
operator|.
name|getTable
argument_list|(
name|TN2
argument_list|)
expr_stmt|;
comment|// Get HTable from Peer1
name|Connection
name|conn2
init|=
name|ConnectionFactory
operator|.
name|createConnection
argument_list|(
name|UTIL2
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
name|ht1TargetAtPeer1
operator|=
name|conn2
operator|.
name|getTable
argument_list|(
name|TN1
argument_list|)
expr_stmt|;
name|ht2TargetAtPeer1
operator|=
name|conn2
operator|.
name|getTable
argument_list|(
name|TN2
argument_list|)
expr_stmt|;
comment|/**      * set M-S : Master: utility1 Slave1: utility2      */
name|ReplicationPeerConfig
name|rpc
init|=
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
name|build
argument_list|()
decl_stmt|;
name|admin1
operator|.
name|addReplicationPeer
argument_list|(
literal|"1"
argument_list|,
name|rpc
argument_list|)
expr_stmt|;
block|}
specifier|final
name|void
name|syncUp
parameter_list|(
name|HBaseTestingUtility
name|util
parameter_list|)
throws|throws
name|Exception
block|{
name|ToolRunner
operator|.
name|run
argument_list|(
name|util
operator|.
name|getConfiguration
argument_list|()
argument_list|,
operator|new
name|ReplicationSyncUp
argument_list|()
argument_list|,
operator|new
name|String
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
block|}
comment|// Utilities that manager shutdown / restart of source / sink clusters. They take care of
comment|// invalidating stale connections after shutdown / restarts.
specifier|final
name|void
name|shutDownSourceHBaseCluster
parameter_list|()
throws|throws
name|Exception
block|{
name|IOUtils
operator|.
name|closeQuietly
argument_list|(
name|ht1Source
argument_list|,
name|ht2Source
argument_list|)
expr_stmt|;
name|UTIL1
operator|.
name|shutdownMiniHBaseCluster
argument_list|()
expr_stmt|;
block|}
specifier|final
name|void
name|shutDownTargetHBaseCluster
parameter_list|()
throws|throws
name|Exception
block|{
name|IOUtils
operator|.
name|closeQuietly
argument_list|(
name|ht1TargetAtPeer1
argument_list|,
name|ht2TargetAtPeer1
argument_list|)
expr_stmt|;
name|UTIL2
operator|.
name|shutdownMiniHBaseCluster
argument_list|()
expr_stmt|;
block|}
specifier|final
name|void
name|restartSourceHBaseCluster
parameter_list|(
name|int
name|numServers
parameter_list|)
throws|throws
name|Exception
block|{
name|IOUtils
operator|.
name|closeQuietly
argument_list|(
name|ht1Source
argument_list|,
name|ht2Source
argument_list|)
expr_stmt|;
name|UTIL1
operator|.
name|restartHBaseCluster
argument_list|(
name|numServers
argument_list|)
expr_stmt|;
name|ht1Source
operator|=
name|UTIL1
operator|.
name|getConnection
argument_list|()
operator|.
name|getTable
argument_list|(
name|TN1
argument_list|)
expr_stmt|;
name|ht2Source
operator|=
name|UTIL1
operator|.
name|getConnection
argument_list|()
operator|.
name|getTable
argument_list|(
name|TN2
argument_list|)
expr_stmt|;
block|}
specifier|final
name|void
name|restartTargetHBaseCluster
parameter_list|(
name|int
name|numServers
parameter_list|)
throws|throws
name|Exception
block|{
name|IOUtils
operator|.
name|closeQuietly
argument_list|(
name|ht1TargetAtPeer1
argument_list|,
name|ht2TargetAtPeer1
argument_list|)
expr_stmt|;
name|UTIL2
operator|.
name|restartHBaseCluster
argument_list|(
name|numServers
argument_list|)
expr_stmt|;
name|ht1TargetAtPeer1
operator|=
name|UTIL2
operator|.
name|getConnection
argument_list|()
operator|.
name|getTable
argument_list|(
name|TN1
argument_list|)
expr_stmt|;
name|ht2TargetAtPeer1
operator|=
name|UTIL2
operator|.
name|getConnection
argument_list|()
operator|.
name|getTable
argument_list|(
name|TN2
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

