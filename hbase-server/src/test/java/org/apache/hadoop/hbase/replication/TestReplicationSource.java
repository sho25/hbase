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
name|assertNotNull
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
name|assertNull
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
name|OptionalLong
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
name|ExecutorService
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
name|Executors
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
name|Future
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
name|MiniHBaseCluster
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
name|Predicate
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
name|replication
operator|.
name|regionserver
operator|.
name|Replication
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
name|ReplicationSource
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
name|ReplicationSourceManager
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
name|wal
operator|.
name|WAL
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
name|WALFactory
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
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|wal
operator|.
name|WALProvider
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
name|org
operator|.
name|mockito
operator|.
name|Mockito
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|LoggerFactory
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
name|MediumTests
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|TestReplicationSource
block|{
specifier|private
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|TestReplicationSource
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
specifier|static
name|HBaseTestingUtility
name|TEST_UTIL
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
specifier|private
specifier|final
specifier|static
name|HBaseTestingUtility
name|TEST_UTIL_PEER
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
specifier|private
specifier|static
name|FileSystem
name|FS
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
name|Configuration
name|conf
init|=
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
comment|/**    * @throws java.lang.Exception    */
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
name|TEST_UTIL
operator|.
name|startMiniDFSCluster
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|FS
operator|=
name|TEST_UTIL
operator|.
name|getDFSCluster
argument_list|()
operator|.
name|getFileSystem
argument_list|()
expr_stmt|;
name|Path
name|rootDir
init|=
name|TEST_UTIL
operator|.
name|createRootDir
argument_list|()
decl_stmt|;
name|oldLogDir
operator|=
operator|new
name|Path
argument_list|(
name|rootDir
argument_list|,
name|HConstants
operator|.
name|HREGION_OLDLOGDIR_NAME
argument_list|)
expr_stmt|;
if|if
condition|(
name|FS
operator|.
name|exists
argument_list|(
name|oldLogDir
argument_list|)
condition|)
name|FS
operator|.
name|delete
argument_list|(
name|oldLogDir
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|logDir
operator|=
operator|new
name|Path
argument_list|(
name|rootDir
argument_list|,
name|HConstants
operator|.
name|HREGION_LOGDIR_NAME
argument_list|)
expr_stmt|;
if|if
condition|(
name|FS
operator|.
name|exists
argument_list|(
name|logDir
argument_list|)
condition|)
name|FS
operator|.
name|delete
argument_list|(
name|logDir
argument_list|,
literal|true
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
name|TEST_UTIL_PEER
operator|.
name|shutdownMiniHBaseCluster
argument_list|()
expr_stmt|;
name|TEST_UTIL
operator|.
name|shutdownMiniHBaseCluster
argument_list|()
expr_stmt|;
name|TEST_UTIL
operator|.
name|shutdownMiniDFSCluster
argument_list|()
expr_stmt|;
block|}
comment|/**    * Sanity check that we can move logs around while we are reading    * from them. Should this test fail, ReplicationSource would have a hard    * time reading logs that are being archived.    * @throws Exception    */
annotation|@
name|Test
specifier|public
name|void
name|testLogMoving
parameter_list|()
throws|throws
name|Exception
block|{
name|Path
name|logPath
init|=
operator|new
name|Path
argument_list|(
name|logDir
argument_list|,
literal|"log"
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|FS
operator|.
name|exists
argument_list|(
name|logDir
argument_list|)
condition|)
name|FS
operator|.
name|mkdirs
argument_list|(
name|logDir
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|FS
operator|.
name|exists
argument_list|(
name|oldLogDir
argument_list|)
condition|)
name|FS
operator|.
name|mkdirs
argument_list|(
name|oldLogDir
argument_list|)
expr_stmt|;
name|WALProvider
operator|.
name|Writer
name|writer
init|=
name|WALFactory
operator|.
name|createWALWriter
argument_list|(
name|FS
argument_list|,
name|logPath
argument_list|,
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
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
operator|<
literal|3
condition|;
name|i
operator|++
control|)
block|{
name|byte
index|[]
name|b
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|Integer
operator|.
name|toString
argument_list|(
name|i
argument_list|)
argument_list|)
decl_stmt|;
name|KeyValue
name|kv
init|=
operator|new
name|KeyValue
argument_list|(
name|b
argument_list|,
name|b
argument_list|,
name|b
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
name|WALKeyImpl
name|key
init|=
operator|new
name|WALKeyImpl
argument_list|(
name|b
argument_list|,
name|TableName
operator|.
name|valueOf
argument_list|(
name|b
argument_list|)
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
name|HConstants
operator|.
name|DEFAULT_CLUSTER_ID
argument_list|)
decl_stmt|;
name|writer
operator|.
name|append
argument_list|(
operator|new
name|WAL
operator|.
name|Entry
argument_list|(
name|key
argument_list|,
name|edit
argument_list|)
argument_list|)
expr_stmt|;
name|writer
operator|.
name|sync
argument_list|()
expr_stmt|;
block|}
name|writer
operator|.
name|close
argument_list|()
expr_stmt|;
name|WAL
operator|.
name|Reader
name|reader
init|=
name|WALFactory
operator|.
name|createReader
argument_list|(
name|FS
argument_list|,
name|logPath
argument_list|,
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
name|WAL
operator|.
name|Entry
name|entry
init|=
name|reader
operator|.
name|next
argument_list|()
decl_stmt|;
name|assertNotNull
argument_list|(
name|entry
argument_list|)
expr_stmt|;
name|Path
name|oldLogPath
init|=
operator|new
name|Path
argument_list|(
name|oldLogDir
argument_list|,
literal|"log"
argument_list|)
decl_stmt|;
name|FS
operator|.
name|rename
argument_list|(
name|logPath
argument_list|,
name|oldLogPath
argument_list|)
expr_stmt|;
name|entry
operator|=
name|reader
operator|.
name|next
argument_list|()
expr_stmt|;
name|assertNotNull
argument_list|(
name|entry
argument_list|)
expr_stmt|;
name|entry
operator|=
name|reader
operator|.
name|next
argument_list|()
expr_stmt|;
name|entry
operator|=
name|reader
operator|.
name|next
argument_list|()
expr_stmt|;
name|assertNull
argument_list|(
name|entry
argument_list|)
expr_stmt|;
name|reader
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
comment|/**    * Tests that {@link ReplicationSource#terminate(String)} will timeout properly    */
annotation|@
name|Test
specifier|public
name|void
name|testTerminateTimeout
parameter_list|()
throws|throws
name|Exception
block|{
name|ReplicationSource
name|source
init|=
operator|new
name|ReplicationSource
argument_list|()
decl_stmt|;
name|ReplicationEndpoint
name|replicationEndpoint
init|=
operator|new
name|HBaseInterClusterReplicationEndpoint
argument_list|()
block|{
annotation|@
name|Override
specifier|protected
name|void
name|doStart
parameter_list|()
block|{
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
comment|// not calling notifyStopped() here causes the caller of stop() to get a Future that never
comment|// completes
block|}
block|}
decl_stmt|;
name|replicationEndpoint
operator|.
name|start
argument_list|()
expr_stmt|;
name|ReplicationPeer
name|mockPeer
init|=
name|Mockito
operator|.
name|mock
argument_list|(
name|ReplicationPeer
operator|.
name|class
argument_list|)
decl_stmt|;
name|Mockito
operator|.
name|when
argument_list|(
name|mockPeer
operator|.
name|getPeerBandwidth
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
literal|0L
argument_list|)
expr_stmt|;
name|Configuration
name|testConf
init|=
name|HBaseConfiguration
operator|.
name|create
argument_list|()
decl_stmt|;
name|testConf
operator|.
name|setInt
argument_list|(
literal|"replication.source.maxretriesmultiplier"
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|ReplicationSourceManager
name|manager
init|=
name|Mockito
operator|.
name|mock
argument_list|(
name|ReplicationSourceManager
operator|.
name|class
argument_list|)
decl_stmt|;
name|Mockito
operator|.
name|when
argument_list|(
name|manager
operator|.
name|getTotalBufferUsed
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
operator|new
name|AtomicLong
argument_list|()
argument_list|)
expr_stmt|;
name|source
operator|.
name|init
argument_list|(
name|testConf
argument_list|,
literal|null
argument_list|,
name|manager
argument_list|,
literal|null
argument_list|,
name|mockPeer
argument_list|,
literal|null
argument_list|,
literal|"testPeer"
argument_list|,
literal|null
argument_list|,
name|p
lambda|->
name|OptionalLong
operator|.
name|empty
argument_list|()
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|ExecutorService
name|executor
init|=
name|Executors
operator|.
name|newSingleThreadExecutor
argument_list|()
decl_stmt|;
name|Future
argument_list|<
name|?
argument_list|>
name|future
init|=
name|executor
operator|.
name|submit
argument_list|(
operator|new
name|Runnable
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
name|source
operator|.
name|terminate
argument_list|(
literal|"testing source termination"
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
decl_stmt|;
name|long
name|sleepForRetries
init|=
name|testConf
operator|.
name|getLong
argument_list|(
literal|"replication.source.sleepforretries"
argument_list|,
literal|1000
argument_list|)
decl_stmt|;
name|Waiter
operator|.
name|waitFor
argument_list|(
name|testConf
argument_list|,
name|sleepForRetries
operator|*
literal|2
argument_list|,
operator|new
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
name|future
operator|.
name|isDone
argument_list|()
return|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
comment|/**    * Tests that recovered queues are preserved on a regionserver shutdown.    * See HBASE-18192    * @throws Exception    */
annotation|@
name|Test
specifier|public
name|void
name|testServerShutdownRecoveredQueue
parameter_list|()
throws|throws
name|Exception
block|{
try|try
block|{
comment|// Ensure single-threaded WAL
name|conf
operator|.
name|set
argument_list|(
literal|"hbase.wal.provider"
argument_list|,
literal|"defaultProvider"
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setInt
argument_list|(
literal|"replication.sleep.before.failover"
argument_list|,
literal|2000
argument_list|)
expr_stmt|;
comment|// Introduces a delay in regionserver shutdown to give the race condition a chance to kick in.
name|conf
operator|.
name|set
argument_list|(
name|HConstants
operator|.
name|REGION_SERVER_IMPL
argument_list|,
name|ShutdownDelayRegionServer
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|MiniHBaseCluster
name|cluster
init|=
name|TEST_UTIL
operator|.
name|startMiniCluster
argument_list|(
literal|2
argument_list|)
decl_stmt|;
name|TEST_UTIL_PEER
operator|.
name|startMiniCluster
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|HRegionServer
name|serverA
init|=
name|cluster
operator|.
name|getRegionServer
argument_list|(
literal|0
argument_list|)
decl_stmt|;
specifier|final
name|ReplicationSourceManager
name|managerA
init|=
operator|(
operator|(
name|Replication
operator|)
name|serverA
operator|.
name|getReplicationSourceService
argument_list|()
operator|)
operator|.
name|getReplicationManager
argument_list|()
decl_stmt|;
name|HRegionServer
name|serverB
init|=
name|cluster
operator|.
name|getRegionServer
argument_list|(
literal|1
argument_list|)
decl_stmt|;
specifier|final
name|ReplicationSourceManager
name|managerB
init|=
operator|(
operator|(
name|Replication
operator|)
name|serverB
operator|.
name|getReplicationSourceService
argument_list|()
operator|)
operator|.
name|getReplicationManager
argument_list|()
decl_stmt|;
specifier|final
name|Admin
name|admin
init|=
name|TEST_UTIL
operator|.
name|getAdmin
argument_list|()
decl_stmt|;
specifier|final
name|String
name|peerId
init|=
literal|"TestPeer"
decl_stmt|;
name|admin
operator|.
name|addReplicationPeer
argument_list|(
name|peerId
argument_list|,
operator|new
name|ReplicationPeerConfig
argument_list|()
operator|.
name|setClusterKey
argument_list|(
name|TEST_UTIL_PEER
operator|.
name|getClusterKey
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
comment|// Wait for replication sources to come up
name|Waiter
operator|.
name|waitFor
argument_list|(
name|conf
argument_list|,
literal|20000
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
operator|!
operator|(
name|managerA
operator|.
name|getSources
argument_list|()
operator|.
name|isEmpty
argument_list|()
operator|||
name|managerB
operator|.
name|getSources
argument_list|()
operator|.
name|isEmpty
argument_list|()
operator|)
return|;
block|}
block|}
argument_list|)
expr_stmt|;
comment|// Disabling peer makes sure there is at least one log to claim when the server dies
comment|// The recovered queue will also stay there until the peer is disabled even if the
comment|// WALs it contains have no data.
name|admin
operator|.
name|disableReplicationPeer
argument_list|(
name|peerId
argument_list|)
expr_stmt|;
comment|// Stopping serverA
comment|// It's queues should be claimed by the only other alive server i.e. serverB
name|cluster
operator|.
name|stopRegionServer
argument_list|(
name|serverA
operator|.
name|getServerName
argument_list|()
argument_list|)
expr_stmt|;
name|Waiter
operator|.
name|waitFor
argument_list|(
name|conf
argument_list|,
literal|20000
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
name|managerB
operator|.
name|getOldSources
argument_list|()
operator|.
name|size
argument_list|()
operator|==
literal|1
return|;
block|}
block|}
argument_list|)
expr_stmt|;
specifier|final
name|HRegionServer
name|serverC
init|=
name|cluster
operator|.
name|startRegionServer
argument_list|()
operator|.
name|getRegionServer
argument_list|()
decl_stmt|;
name|serverC
operator|.
name|waitForServerOnline
argument_list|()
expr_stmt|;
name|Waiter
operator|.
name|waitFor
argument_list|(
name|conf
argument_list|,
literal|20000
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
name|serverC
operator|.
name|getReplicationSourceService
argument_list|()
operator|!=
literal|null
return|;
block|}
block|}
argument_list|)
expr_stmt|;
specifier|final
name|ReplicationSourceManager
name|managerC
init|=
operator|(
operator|(
name|Replication
operator|)
name|serverC
operator|.
name|getReplicationSourceService
argument_list|()
operator|)
operator|.
name|getReplicationManager
argument_list|()
decl_stmt|;
comment|// Sanity check
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|managerC
operator|.
name|getOldSources
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
comment|// Stopping serverB
comment|// Now serverC should have two recovered queues:
comment|// 1. The serverB's normal queue
comment|// 2. serverA's recovered queue on serverB
name|cluster
operator|.
name|stopRegionServer
argument_list|(
name|serverB
operator|.
name|getServerName
argument_list|()
argument_list|)
expr_stmt|;
name|Waiter
operator|.
name|waitFor
argument_list|(
name|conf
argument_list|,
literal|20000
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
name|managerC
operator|.
name|getOldSources
argument_list|()
operator|.
name|size
argument_list|()
operator|==
literal|2
return|;
block|}
block|}
argument_list|)
expr_stmt|;
name|admin
operator|.
name|enableReplicationPeer
argument_list|(
name|peerId
argument_list|)
expr_stmt|;
name|Waiter
operator|.
name|waitFor
argument_list|(
name|conf
argument_list|,
literal|20000
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
name|managerC
operator|.
name|getOldSources
argument_list|()
operator|.
name|size
argument_list|()
operator|==
literal|0
return|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|conf
operator|.
name|set
argument_list|(
name|HConstants
operator|.
name|REGION_SERVER_IMPL
argument_list|,
name|HRegionServer
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Regionserver implementation that adds a delay on the graceful shutdown.    */
specifier|public
specifier|static
class|class
name|ShutdownDelayRegionServer
extends|extends
name|HRegionServer
block|{
specifier|public
name|ShutdownDelayRegionServer
parameter_list|(
name|Configuration
name|conf
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|super
argument_list|(
name|conf
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|stopServiceThreads
parameter_list|()
block|{
comment|// Add a delay before service threads are shutdown.
comment|// This will keep the zookeeper connection alive for the duration of the delay.
name|LOG
operator|.
name|info
argument_list|(
literal|"Adding a delay to the regionserver shutdown"
argument_list|)
expr_stmt|;
try|try
block|{
name|Thread
operator|.
name|sleep
argument_list|(
literal|2000
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|ex
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Interrupted while sleeping"
argument_list|)
expr_stmt|;
block|}
name|super
operator|.
name|stopServiceThreads
argument_list|()
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

