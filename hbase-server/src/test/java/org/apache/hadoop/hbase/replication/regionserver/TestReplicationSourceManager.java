begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|net
operator|.
name|URLEncoder
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
name|Collection
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
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|CountDownLatch
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
name|HColumnDescriptor
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
name|catalog
operator|.
name|CatalogTracker
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
name|HLogFactory
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
name|HLogKey
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
name|WALActionsListener
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
name|ReplicationZookeeper
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
name|zookeeper
operator|.
name|ZKUtil
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
name|ZooKeeperWatcher
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
name|TestReplicationSourceManager
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
name|TestReplicationSourceManager
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|Configuration
name|conf
decl_stmt|;
specifier|private
specifier|static
name|HBaseTestingUtility
name|utility
decl_stmt|;
specifier|private
specifier|static
name|Replication
name|replication
decl_stmt|;
specifier|private
specifier|static
name|ReplicationSourceManager
name|manager
decl_stmt|;
specifier|private
specifier|static
name|ZooKeeperWatcher
name|zkw
decl_stmt|;
specifier|private
specifier|static
name|HTableDescriptor
name|htd
decl_stmt|;
specifier|private
specifier|static
name|HRegionInfo
name|hri
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|r1
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"r1"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|r2
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"r2"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|f1
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"f1"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|test
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"test"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|slaveId
init|=
literal|"1"
decl_stmt|;
specifier|private
specifier|static
name|FileSystem
name|fs
decl_stmt|;
specifier|private
specifier|static
name|String
name|logName
decl_stmt|;
specifier|private
specifier|static
name|Path
name|oldLogDir
decl_stmt|;
specifier|private
specifier|static
name|Path
name|logDir
decl_stmt|;
specifier|private
specifier|static
name|CountDownLatch
name|latch
decl_stmt|;
specifier|private
specifier|static
name|List
argument_list|<
name|String
argument_list|>
name|files
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
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
name|setBoolean
argument_list|(
name|HConstants
operator|.
name|REPLICATION_ENABLE_KEY
argument_list|,
literal|true
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
name|zkw
operator|=
operator|new
name|ZooKeeperWatcher
argument_list|(
name|conf
argument_list|,
literal|"test"
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|ZKUtil
operator|.
name|createWithParents
argument_list|(
name|zkw
argument_list|,
literal|"/hbase/replication"
argument_list|)
expr_stmt|;
name|ZKUtil
operator|.
name|createWithParents
argument_list|(
name|zkw
argument_list|,
literal|"/hbase/replication/peers/1"
argument_list|)
expr_stmt|;
name|ZKUtil
operator|.
name|setData
argument_list|(
name|zkw
argument_list|,
literal|"/hbase/replication/peers/1"
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|conf
operator|.
name|get
argument_list|(
name|HConstants
operator|.
name|ZOOKEEPER_QUORUM
argument_list|)
operator|+
literal|":"
operator|+
name|conf
operator|.
name|get
argument_list|(
name|HConstants
operator|.
name|ZOOKEEPER_CLIENT_PORT
argument_list|)
operator|+
literal|":/1"
argument_list|)
argument_list|)
expr_stmt|;
name|ZKUtil
operator|.
name|createWithParents
argument_list|(
name|zkw
argument_list|,
literal|"/hbase/replication/peers/1/peer-state"
argument_list|)
expr_stmt|;
name|ZKUtil
operator|.
name|setData
argument_list|(
name|zkw
argument_list|,
literal|"/hbase/replication/peers/1/peer-state"
argument_list|,
name|ReplicationZookeeper
operator|.
name|ENABLED_ZNODE_BYTES
argument_list|)
expr_stmt|;
name|ZKUtil
operator|.
name|createWithParents
argument_list|(
name|zkw
argument_list|,
literal|"/hbase/replication/state"
argument_list|)
expr_stmt|;
name|ZKUtil
operator|.
name|setData
argument_list|(
name|zkw
argument_list|,
literal|"/hbase/replication/state"
argument_list|,
name|ReplicationZookeeper
operator|.
name|ENABLED_ZNODE_BYTES
argument_list|)
expr_stmt|;
name|replication
operator|=
operator|new
name|Replication
argument_list|(
operator|new
name|DummyServer
argument_list|()
argument_list|,
name|fs
argument_list|,
name|logDir
argument_list|,
name|oldLogDir
argument_list|)
expr_stmt|;
name|manager
operator|=
name|replication
operator|.
name|getReplicationManager
argument_list|()
expr_stmt|;
name|fs
operator|=
name|FileSystem
operator|.
name|get
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|oldLogDir
operator|=
operator|new
name|Path
argument_list|(
name|utility
operator|.
name|getDataTestDir
argument_list|()
argument_list|,
name|HConstants
operator|.
name|HREGION_OLDLOGDIR_NAME
argument_list|)
expr_stmt|;
name|logDir
operator|=
operator|new
name|Path
argument_list|(
name|utility
operator|.
name|getDataTestDir
argument_list|()
argument_list|,
name|HConstants
operator|.
name|HREGION_LOGDIR_NAME
argument_list|)
expr_stmt|;
name|logName
operator|=
name|HConstants
operator|.
name|HREGION_LOGDIR_NAME
expr_stmt|;
name|manager
operator|.
name|addSource
argument_list|(
name|slaveId
argument_list|)
expr_stmt|;
name|htd
operator|=
operator|new
name|HTableDescriptor
argument_list|(
name|test
argument_list|)
expr_stmt|;
name|HColumnDescriptor
name|col
init|=
operator|new
name|HColumnDescriptor
argument_list|(
literal|"f1"
argument_list|)
decl_stmt|;
name|col
operator|.
name|setScope
argument_list|(
name|HConstants
operator|.
name|REPLICATION_SCOPE_GLOBAL
argument_list|)
expr_stmt|;
name|htd
operator|.
name|addFamily
argument_list|(
name|col
argument_list|)
expr_stmt|;
name|col
operator|=
operator|new
name|HColumnDescriptor
argument_list|(
literal|"f2"
argument_list|)
expr_stmt|;
name|col
operator|.
name|setScope
argument_list|(
name|HConstants
operator|.
name|REPLICATION_SCOPE_LOCAL
argument_list|)
expr_stmt|;
name|htd
operator|.
name|addFamily
argument_list|(
name|col
argument_list|)
expr_stmt|;
name|hri
operator|=
operator|new
name|HRegionInfo
argument_list|(
name|htd
operator|.
name|getName
argument_list|()
argument_list|,
name|r1
argument_list|,
name|r2
argument_list|)
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
name|manager
operator|.
name|join
argument_list|()
expr_stmt|;
name|utility
operator|.
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Before
specifier|public
name|void
name|setUp
parameter_list|()
throws|throws
name|Exception
block|{
name|fs
operator|.
name|delete
argument_list|(
name|logDir
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|fs
operator|.
name|delete
argument_list|(
name|oldLogDir
argument_list|,
literal|true
argument_list|)
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
name|setUp
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testLogRoll
parameter_list|()
throws|throws
name|Exception
block|{
name|long
name|seq
init|=
literal|0
decl_stmt|;
name|long
name|baseline
init|=
literal|1000
decl_stmt|;
name|long
name|time
init|=
name|baseline
decl_stmt|;
name|KeyValue
name|kv
init|=
operator|new
name|KeyValue
argument_list|(
name|r1
argument_list|,
name|f1
argument_list|,
name|r1
argument_list|)
decl_stmt|;
name|WALEdit
name|edit
init|=
operator|new
name|WALEdit
argument_list|()
decl_stmt|;
name|edit
operator|.
name|add
argument_list|(
name|kv
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|WALActionsListener
argument_list|>
name|listeners
init|=
operator|new
name|ArrayList
argument_list|<
name|WALActionsListener
argument_list|>
argument_list|()
decl_stmt|;
name|listeners
operator|.
name|add
argument_list|(
name|replication
argument_list|)
expr_stmt|;
name|HLog
name|hlog
init|=
name|HLogFactory
operator|.
name|createHLog
argument_list|(
name|fs
argument_list|,
name|utility
operator|.
name|getDataTestDir
argument_list|()
argument_list|,
name|logName
argument_list|,
name|conf
argument_list|,
name|listeners
argument_list|,
name|URLEncoder
operator|.
name|encode
argument_list|(
literal|"regionserver:60020"
argument_list|,
literal|"UTF8"
argument_list|)
argument_list|)
decl_stmt|;
name|manager
operator|.
name|init
argument_list|()
expr_stmt|;
name|HTableDescriptor
name|htd
init|=
operator|new
name|HTableDescriptor
argument_list|()
decl_stmt|;
name|htd
operator|.
name|addFamily
argument_list|(
operator|new
name|HColumnDescriptor
argument_list|(
name|f1
argument_list|)
argument_list|)
expr_stmt|;
comment|// Testing normal log rolling every 20
for|for
control|(
name|long
name|i
init|=
literal|1
init|;
name|i
operator|<
literal|101
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|i
operator|>
literal|1
operator|&&
name|i
operator|%
literal|20
operator|==
literal|0
condition|)
block|{
name|hlog
operator|.
name|rollWriter
argument_list|()
expr_stmt|;
block|}
name|LOG
operator|.
name|info
argument_list|(
name|i
argument_list|)
expr_stmt|;
name|HLogKey
name|key
init|=
operator|new
name|HLogKey
argument_list|(
name|hri
operator|.
name|getRegionName
argument_list|()
argument_list|,
name|test
argument_list|,
name|seq
operator|++
argument_list|,
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|,
name|HConstants
operator|.
name|DEFAULT_CLUSTER_ID
argument_list|)
decl_stmt|;
name|hlog
operator|.
name|append
argument_list|(
name|hri
argument_list|,
name|key
argument_list|,
name|edit
argument_list|,
name|htd
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
comment|// Simulate a rapid insert that's followed
comment|// by a report that's still not totally complete (missing last one)
name|LOG
operator|.
name|info
argument_list|(
name|baseline
operator|+
literal|" and "
operator|+
name|time
argument_list|)
expr_stmt|;
name|baseline
operator|+=
literal|101
expr_stmt|;
name|time
operator|=
name|baseline
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
name|baseline
operator|+
literal|" and "
operator|+
name|time
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
literal|3
condition|;
name|i
operator|++
control|)
block|{
name|HLogKey
name|key
init|=
operator|new
name|HLogKey
argument_list|(
name|hri
operator|.
name|getRegionName
argument_list|()
argument_list|,
name|test
argument_list|,
name|seq
operator|++
argument_list|,
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|,
name|HConstants
operator|.
name|DEFAULT_CLUSTER_ID
argument_list|)
decl_stmt|;
name|hlog
operator|.
name|append
argument_list|(
name|hri
argument_list|,
name|key
argument_list|,
name|edit
argument_list|,
name|htd
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
name|assertEquals
argument_list|(
literal|6
argument_list|,
name|manager
operator|.
name|getHLogs
argument_list|()
operator|.
name|get
argument_list|(
name|slaveId
argument_list|)
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|hlog
operator|.
name|rollWriter
argument_list|()
expr_stmt|;
name|manager
operator|.
name|logPositionAndCleanOldLogs
argument_list|(
name|manager
operator|.
name|getSources
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getCurrentPath
argument_list|()
argument_list|,
literal|"1"
argument_list|,
literal|0
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|HLogKey
name|key
init|=
operator|new
name|HLogKey
argument_list|(
name|hri
operator|.
name|getRegionName
argument_list|()
argument_list|,
name|test
argument_list|,
name|seq
operator|++
argument_list|,
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|,
name|HConstants
operator|.
name|DEFAULT_CLUSTER_ID
argument_list|)
decl_stmt|;
name|hlog
operator|.
name|append
argument_list|(
name|hri
argument_list|,
name|key
argument_list|,
name|edit
argument_list|,
name|htd
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|manager
operator|.
name|getHLogs
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
comment|// TODO Need a case with only 2 HLogs and we only want to delete the first one
block|}
annotation|@
name|Test
specifier|public
name|void
name|testClaimQueues
parameter_list|()
throws|throws
name|Exception
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"testNodeFailoverWorkerCopyQueuesFromRSUsingMulti"
argument_list|)
expr_stmt|;
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
literal|"hostname0.example.org"
argument_list|)
decl_stmt|;
name|AtomicBoolean
name|replicating
init|=
operator|new
name|AtomicBoolean
argument_list|(
literal|true
argument_list|)
decl_stmt|;
name|ReplicationZookeeper
name|rz
init|=
operator|new
name|ReplicationZookeeper
argument_list|(
name|server
argument_list|,
name|replicating
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
name|rz
operator|.
name|addLogToList
argument_list|(
name|file
argument_list|,
literal|"1"
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
literal|"dummyserver1.example.org"
argument_list|)
decl_stmt|;
name|Server
name|s2
init|=
operator|new
name|DummyServer
argument_list|(
literal|"dummyserver2.example.org"
argument_list|)
decl_stmt|;
name|Server
name|s3
init|=
operator|new
name|DummyServer
argument_list|(
literal|"dummyserver3.example.org"
argument_list|)
decl_stmt|;
comment|// create 3 DummyNodeFailoverWorkers
name|DummyNodeFailoverWorker
name|w1
init|=
operator|new
name|DummyNodeFailoverWorker
argument_list|(
name|server
operator|.
name|getServerName
argument_list|()
operator|.
name|getServerName
argument_list|()
argument_list|,
name|s1
argument_list|)
decl_stmt|;
name|DummyNodeFailoverWorker
name|w2
init|=
operator|new
name|DummyNodeFailoverWorker
argument_list|(
name|server
operator|.
name|getServerName
argument_list|()
operator|.
name|getServerName
argument_list|()
argument_list|,
name|s2
argument_list|)
decl_stmt|;
name|DummyNodeFailoverWorker
name|w3
init|=
operator|new
name|DummyNodeFailoverWorker
argument_list|(
name|server
operator|.
name|getServerName
argument_list|()
operator|.
name|getServerName
argument_list|()
argument_list|,
name|s3
argument_list|)
decl_stmt|;
name|latch
operator|=
operator|new
name|CountDownLatch
argument_list|(
literal|3
argument_list|)
expr_stmt|;
comment|// start the threads
name|w1
operator|.
name|start
argument_list|()
expr_stmt|;
name|w2
operator|.
name|start
argument_list|()
expr_stmt|;
name|w3
operator|.
name|start
argument_list|()
expr_stmt|;
comment|// make sure only one is successful
name|int
name|populatedMap
init|=
literal|0
decl_stmt|;
comment|// wait for result now... till all the workers are done.
name|latch
operator|.
name|await
argument_list|()
expr_stmt|;
name|populatedMap
operator|+=
name|w1
operator|.
name|isLogZnodesMapPopulated
argument_list|()
operator|+
name|w2
operator|.
name|isLogZnodesMapPopulated
argument_list|()
operator|+
name|w3
operator|.
name|isLogZnodesMapPopulated
argument_list|()
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|populatedMap
argument_list|)
expr_stmt|;
comment|// close out the resources.
name|rz
operator|.
name|close
argument_list|()
expr_stmt|;
name|server
operator|.
name|abort
argument_list|(
literal|""
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testNodeFailoverDeadServerParsing
parameter_list|()
throws|throws
name|Exception
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"testNodeFailoverDeadServerParsing"
argument_list|)
expr_stmt|;
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
name|AtomicBoolean
name|replicating
init|=
operator|new
name|AtomicBoolean
argument_list|(
literal|true
argument_list|)
decl_stmt|;
name|ReplicationZookeeper
name|rz
init|=
operator|new
name|ReplicationZookeeper
argument_list|(
name|server
argument_list|,
name|replicating
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
name|rz
operator|.
name|addLogToList
argument_list|(
name|file
argument_list|,
literal|"1"
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
name|ReplicationZookeeper
name|rz1
init|=
operator|new
name|ReplicationZookeeper
argument_list|(
name|s1
argument_list|,
operator|new
name|AtomicBoolean
argument_list|(
literal|true
argument_list|)
argument_list|)
decl_stmt|;
name|SortedMap
argument_list|<
name|String
argument_list|,
name|SortedSet
argument_list|<
name|String
argument_list|>
argument_list|>
name|testMap
init|=
name|rz1
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
name|rz1
operator|.
name|close
argument_list|()
expr_stmt|;
name|ReplicationZookeeper
name|rz2
init|=
operator|new
name|ReplicationZookeeper
argument_list|(
name|s2
argument_list|,
operator|new
name|AtomicBoolean
argument_list|(
literal|true
argument_list|)
argument_list|)
decl_stmt|;
name|testMap
operator|=
name|rz2
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
name|rz2
operator|.
name|close
argument_list|()
expr_stmt|;
name|ReplicationZookeeper
name|rz3
init|=
operator|new
name|ReplicationZookeeper
argument_list|(
name|s3
argument_list|,
operator|new
name|AtomicBoolean
argument_list|(
literal|true
argument_list|)
argument_list|)
decl_stmt|;
name|testMap
operator|=
name|rz3
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
name|rz3
operator|.
name|close
argument_list|()
expr_stmt|;
name|ReplicationSource
name|s
init|=
operator|new
name|ReplicationSource
argument_list|()
decl_stmt|;
name|s
operator|.
name|checkIfQueueRecovered
argument_list|(
name|testMap
operator|.
name|firstKey
argument_list|()
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|result
init|=
name|s
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
comment|// close out the resources.
name|rz
operator|.
name|close
argument_list|()
expr_stmt|;
name|server
operator|.
name|abort
argument_list|(
literal|""
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
specifier|static
class|class
name|DummyNodeFailoverWorker
extends|extends
name|Thread
block|{
specifier|private
name|SortedMap
argument_list|<
name|String
argument_list|,
name|SortedSet
argument_list|<
name|String
argument_list|>
argument_list|>
name|logZnodesMap
decl_stmt|;
name|Server
name|server
decl_stmt|;
specifier|private
name|String
name|deadRsZnode
decl_stmt|;
name|ReplicationZookeeper
name|rz
decl_stmt|;
specifier|public
name|DummyNodeFailoverWorker
parameter_list|(
name|String
name|znode
parameter_list|,
name|Server
name|s
parameter_list|)
throws|throws
name|Exception
block|{
name|this
operator|.
name|deadRsZnode
operator|=
name|znode
expr_stmt|;
name|this
operator|.
name|server
operator|=
name|s
expr_stmt|;
name|rz
operator|=
operator|new
name|ReplicationZookeeper
argument_list|(
name|server
argument_list|,
operator|new
name|AtomicBoolean
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
try|try
block|{
name|logZnodesMap
operator|=
name|rz
operator|.
name|claimQueues
argument_list|(
name|deadRsZnode
argument_list|)
expr_stmt|;
name|rz
operator|.
name|close
argument_list|()
expr_stmt|;
name|server
operator|.
name|abort
argument_list|(
literal|"Done with testing"
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Got exception while running NodeFailoverWorker"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|latch
operator|.
name|countDown
argument_list|()
expr_stmt|;
block|}
block|}
comment|/**      * @return 1 when the map is not empty.      */
specifier|private
name|int
name|isLogZnodesMapPopulated
parameter_list|()
block|{
name|Collection
argument_list|<
name|SortedSet
argument_list|<
name|String
argument_list|>
argument_list|>
name|sets
init|=
name|logZnodesMap
operator|.
name|values
argument_list|()
decl_stmt|;
if|if
condition|(
name|sets
operator|.
name|size
argument_list|()
operator|>
literal|1
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"unexpected size of logZnodesMap: "
operator|+
name|sets
operator|.
name|size
argument_list|()
argument_list|)
throw|;
block|}
if|if
condition|(
name|sets
operator|.
name|size
argument_list|()
operator|==
literal|1
condition|)
block|{
name|SortedSet
argument_list|<
name|String
argument_list|>
name|s
init|=
name|sets
operator|.
name|iterator
argument_list|()
operator|.
name|next
argument_list|()
decl_stmt|;
for|for
control|(
name|String
name|file
range|:
name|files
control|)
block|{
comment|// at least one file was missing
if|if
condition|(
operator|!
name|s
operator|.
name|contains
argument_list|(
name|file
argument_list|)
condition|)
block|{
return|return
literal|0
return|;
block|}
block|}
return|return
literal|1
return|;
comment|// we found all the files
block|}
return|return
literal|0
return|;
block|}
block|}
specifier|static
class|class
name|DummyServer
implements|implements
name|Server
block|{
name|String
name|hostname
decl_stmt|;
name|DummyServer
parameter_list|()
block|{
name|hostname
operator|=
literal|"hostname.example.org"
expr_stmt|;
block|}
name|DummyServer
parameter_list|(
name|String
name|hostname
parameter_list|)
block|{
name|this
operator|.
name|hostname
operator|=
name|hostname
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Configuration
name|getConfiguration
parameter_list|()
block|{
return|return
name|conf
return|;
block|}
annotation|@
name|Override
specifier|public
name|ZooKeeperWatcher
name|getZooKeeper
parameter_list|()
block|{
return|return
name|zkw
return|;
block|}
annotation|@
name|Override
specifier|public
name|CatalogTracker
name|getCatalogTracker
parameter_list|()
block|{
return|return
literal|null
return|;
comment|// To change body of implemented methods use File | Settings | File Templates.
block|}
annotation|@
name|Override
specifier|public
name|ServerName
name|getServerName
parameter_list|()
block|{
return|return
operator|new
name|ServerName
argument_list|(
name|hostname
argument_list|,
literal|1234
argument_list|,
literal|1L
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|abort
parameter_list|(
name|String
name|why
parameter_list|,
name|Throwable
name|e
parameter_list|)
block|{
comment|// To change body of implemented methods use File | Settings | File Templates.
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isAborted
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|stop
parameter_list|(
name|String
name|why
parameter_list|)
block|{
comment|// To change body of implemented methods use File | Settings | File Templates.
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isStopped
parameter_list|()
block|{
return|return
literal|false
return|;
comment|// To change body of implemented methods use File | Settings | File Templates.
block|}
block|}
block|}
end_class

end_unit

