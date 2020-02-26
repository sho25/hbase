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
name|OptionalLong
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
name|ScheduledExecutorService
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
name|TimeUnit
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
name|CellScanner
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
name|regionserver
operator|.
name|ReplicationSinkService
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
name|ReplicationSourceService
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
name|replication
operator|.
name|ReplicationTracker
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
name|ReplicationUtils
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
name|SyncReplicationState
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
name|Pair
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
name|SyncReplicationWALProvider
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
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|zookeeper
operator|.
name|ZKClusterId
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|yetus
operator|.
name|audience
operator|.
name|InterfaceAudience
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|zookeeper
operator|.
name|KeeperException
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
name|util
operator|.
name|concurrent
operator|.
name|ThreadFactoryBuilder
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
name|shaded
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|AdminProtos
operator|.
name|WALEntry
import|;
end_import

begin_comment
comment|/**  * Gateway to Replication. Used by {@link org.apache.hadoop.hbase.regionserver.HRegionServer}.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|Replication
implements|implements
name|ReplicationSourceService
implements|,
name|ReplicationSinkService
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
name|Replication
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|boolean
name|isReplicationForBulkLoadDataEnabled
decl_stmt|;
specifier|private
name|ReplicationSourceManager
name|replicationManager
decl_stmt|;
specifier|private
name|ReplicationQueueStorage
name|queueStorage
decl_stmt|;
specifier|private
name|ReplicationPeers
name|replicationPeers
decl_stmt|;
specifier|private
name|ReplicationTracker
name|replicationTracker
decl_stmt|;
specifier|private
name|Configuration
name|conf
decl_stmt|;
specifier|private
name|ReplicationSink
name|replicationSink
decl_stmt|;
specifier|private
name|SyncReplicationPeerInfoProvider
name|syncReplicationPeerInfoProvider
decl_stmt|;
comment|// Hosting server
specifier|private
name|Server
name|server
decl_stmt|;
comment|/** Statistics thread schedule pool */
specifier|private
name|ScheduledExecutorService
name|scheduleThreadPool
decl_stmt|;
specifier|private
name|int
name|statsThreadPeriod
decl_stmt|;
comment|// ReplicationLoad to access replication metrics
specifier|private
name|ReplicationLoad
name|replicationLoad
decl_stmt|;
specifier|private
name|PeerProcedureHandler
name|peerProcedureHandler
decl_stmt|;
comment|/**    * Empty constructor    */
specifier|public
name|Replication
parameter_list|()
block|{   }
annotation|@
name|Override
specifier|public
name|void
name|initialize
parameter_list|(
name|Server
name|server
parameter_list|,
name|FileSystem
name|fs
parameter_list|,
name|Path
name|logDir
parameter_list|,
name|Path
name|oldLogDir
parameter_list|,
name|WALProvider
name|walProvider
parameter_list|)
throws|throws
name|IOException
block|{
name|this
operator|.
name|server
operator|=
name|server
expr_stmt|;
name|this
operator|.
name|conf
operator|=
name|this
operator|.
name|server
operator|.
name|getConfiguration
argument_list|()
expr_stmt|;
name|this
operator|.
name|isReplicationForBulkLoadDataEnabled
operator|=
name|ReplicationUtils
operator|.
name|isReplicationForBulkLoadDataEnabled
argument_list|(
name|this
operator|.
name|conf
argument_list|)
expr_stmt|;
name|this
operator|.
name|scheduleThreadPool
operator|=
name|Executors
operator|.
name|newScheduledThreadPool
argument_list|(
literal|1
argument_list|,
operator|new
name|ThreadFactoryBuilder
argument_list|()
operator|.
name|setNameFormat
argument_list|(
name|server
operator|.
name|getServerName
argument_list|()
operator|.
name|toShortString
argument_list|()
operator|+
literal|"Replication Statistics #%d"
argument_list|)
operator|.
name|setDaemon
argument_list|(
literal|true
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|this
operator|.
name|isReplicationForBulkLoadDataEnabled
condition|)
block|{
if|if
condition|(
name|conf
operator|.
name|get
argument_list|(
name|HConstants
operator|.
name|REPLICATION_CLUSTER_ID
argument_list|)
operator|==
literal|null
operator|||
name|conf
operator|.
name|get
argument_list|(
name|HConstants
operator|.
name|REPLICATION_CLUSTER_ID
argument_list|)
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
name|HConstants
operator|.
name|REPLICATION_CLUSTER_ID
operator|+
literal|" cannot be null/empty when "
operator|+
name|HConstants
operator|.
name|REPLICATION_BULKLOAD_ENABLE_KEY
operator|+
literal|" is set to true."
argument_list|)
throw|;
block|}
block|}
try|try
block|{
name|this
operator|.
name|queueStorage
operator|=
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
expr_stmt|;
name|this
operator|.
name|replicationPeers
operator|=
name|ReplicationFactory
operator|.
name|getReplicationPeers
argument_list|(
name|server
operator|.
name|getZooKeeper
argument_list|()
argument_list|,
name|this
operator|.
name|conf
argument_list|)
expr_stmt|;
name|this
operator|.
name|replicationPeers
operator|.
name|init
argument_list|()
expr_stmt|;
name|this
operator|.
name|replicationTracker
operator|=
name|ReplicationFactory
operator|.
name|getReplicationTracker
argument_list|(
name|server
operator|.
name|getZooKeeper
argument_list|()
argument_list|,
name|this
operator|.
name|server
argument_list|,
name|this
operator|.
name|server
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Failed replication handler create"
argument_list|,
name|e
argument_list|)
throw|;
block|}
name|UUID
name|clusterId
init|=
literal|null
decl_stmt|;
try|try
block|{
name|clusterId
operator|=
name|ZKClusterId
operator|.
name|getUUIDForCluster
argument_list|(
name|this
operator|.
name|server
operator|.
name|getZooKeeper
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|KeeperException
name|ke
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Could not read cluster id"
argument_list|,
name|ke
argument_list|)
throw|;
block|}
name|SyncReplicationPeerMappingManager
name|mapping
init|=
operator|new
name|SyncReplicationPeerMappingManager
argument_list|()
decl_stmt|;
name|this
operator|.
name|replicationManager
operator|=
operator|new
name|ReplicationSourceManager
argument_list|(
name|queueStorage
argument_list|,
name|replicationPeers
argument_list|,
name|replicationTracker
argument_list|,
name|conf
argument_list|,
name|this
operator|.
name|server
argument_list|,
name|fs
argument_list|,
name|logDir
argument_list|,
name|oldLogDir
argument_list|,
name|clusterId
argument_list|,
name|walProvider
operator|!=
literal|null
condition|?
name|walProvider
operator|.
name|getWALFileLengthProvider
argument_list|()
else|:
name|p
lambda|->
name|OptionalLong
operator|.
name|empty
argument_list|()
argument_list|,
name|mapping
argument_list|)
expr_stmt|;
name|this
operator|.
name|syncReplicationPeerInfoProvider
operator|=
operator|new
name|SyncReplicationPeerInfoProviderImpl
argument_list|(
name|replicationPeers
argument_list|,
name|mapping
argument_list|)
expr_stmt|;
name|PeerActionListener
name|peerActionListener
init|=
name|PeerActionListener
operator|.
name|DUMMY
decl_stmt|;
if|if
condition|(
name|walProvider
operator|!=
literal|null
condition|)
block|{
name|walProvider
operator|.
name|addWALActionsListener
argument_list|(
operator|new
name|ReplicationSourceWALActionListener
argument_list|(
name|conf
argument_list|,
name|replicationManager
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|walProvider
operator|instanceof
name|SyncReplicationWALProvider
condition|)
block|{
name|SyncReplicationWALProvider
name|syncWALProvider
init|=
operator|(
name|SyncReplicationWALProvider
operator|)
name|walProvider
decl_stmt|;
name|peerActionListener
operator|=
name|syncWALProvider
expr_stmt|;
name|syncWALProvider
operator|.
name|setPeerInfoProvider
argument_list|(
name|syncReplicationPeerInfoProvider
argument_list|)
expr_stmt|;
comment|// for sync replication state change, we need to reload the state twice, you can see the
comment|// code in PeerProcedureHandlerImpl, so here we need to go over the sync replication peers
comment|// to see if any of them are in the middle of the two refreshes, if so, we need to manually
comment|// repeat the action we have done in the first refresh, otherwise when the second refresh
comment|// comes we will be in trouble, such as NPE.
name|replicationPeers
operator|.
name|getAllPeerIds
argument_list|()
operator|.
name|stream
argument_list|()
operator|.
name|map
argument_list|(
name|replicationPeers
operator|::
name|getPeer
argument_list|)
operator|.
name|filter
argument_list|(
name|p
lambda|->
name|p
operator|.
name|getPeerConfig
argument_list|()
operator|.
name|isSyncReplication
argument_list|()
argument_list|)
operator|.
name|filter
argument_list|(
name|p
lambda|->
name|p
operator|.
name|getNewSyncReplicationState
argument_list|()
operator|!=
name|SyncReplicationState
operator|.
name|NONE
argument_list|)
operator|.
name|forEach
argument_list|(
name|p
lambda|->
name|syncWALProvider
operator|.
name|peerSyncReplicationStateChange
argument_list|(
name|p
operator|.
name|getId
argument_list|()
argument_list|,
name|p
operator|.
name|getSyncReplicationState
argument_list|()
argument_list|,
name|p
operator|.
name|getNewSyncReplicationState
argument_list|()
argument_list|,
literal|0
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
name|this
operator|.
name|statsThreadPeriod
operator|=
name|this
operator|.
name|conf
operator|.
name|getInt
argument_list|(
literal|"replication.stats.thread.period.seconds"
argument_list|,
literal|5
operator|*
literal|60
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Replication stats-in-log period={} seconds"
argument_list|,
name|this
operator|.
name|statsThreadPeriod
argument_list|)
expr_stmt|;
name|this
operator|.
name|replicationLoad
operator|=
operator|new
name|ReplicationLoad
argument_list|()
expr_stmt|;
name|this
operator|.
name|peerProcedureHandler
operator|=
operator|new
name|PeerProcedureHandlerImpl
argument_list|(
name|replicationManager
argument_list|,
name|peerActionListener
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|PeerProcedureHandler
name|getPeerProcedureHandler
parameter_list|()
block|{
return|return
name|peerProcedureHandler
return|;
block|}
comment|/**    * Stops replication service.    */
annotation|@
name|Override
specifier|public
name|void
name|stopReplicationService
parameter_list|()
block|{
name|join
argument_list|()
expr_stmt|;
block|}
comment|/**    * Join with the replication threads    */
specifier|public
name|void
name|join
parameter_list|()
block|{
name|this
operator|.
name|replicationManager
operator|.
name|join
argument_list|()
expr_stmt|;
if|if
condition|(
name|this
operator|.
name|replicationSink
operator|!=
literal|null
condition|)
block|{
name|this
operator|.
name|replicationSink
operator|.
name|stopReplicationSinkServices
argument_list|()
expr_stmt|;
block|}
name|scheduleThreadPool
operator|.
name|shutdown
argument_list|()
expr_stmt|;
block|}
comment|/**    * Carry on the list of log entries down to the sink    * @param entries list of entries to replicate    * @param cells The data -- the cells -- that<code>entries</code> describes (the entries do not    *          contain the Cells we are replicating; they are passed here on the side in this    *          CellScanner).    * @param replicationClusterId Id which will uniquely identify source cluster FS client    *          configurations in the replication configuration directory    * @param sourceBaseNamespaceDirPath Path that point to the source cluster base namespace    *          directory required for replicating hfiles    * @param sourceHFileArchiveDirPath Path that point to the source cluster hfile archive directory    * @throws IOException    */
annotation|@
name|Override
specifier|public
name|void
name|replicateLogEntries
parameter_list|(
name|List
argument_list|<
name|WALEntry
argument_list|>
name|entries
parameter_list|,
name|CellScanner
name|cells
parameter_list|,
name|String
name|replicationClusterId
parameter_list|,
name|String
name|sourceBaseNamespaceDirPath
parameter_list|,
name|String
name|sourceHFileArchiveDirPath
parameter_list|)
throws|throws
name|IOException
block|{
name|this
operator|.
name|replicationSink
operator|.
name|replicateEntries
argument_list|(
name|entries
argument_list|,
name|cells
argument_list|,
name|replicationClusterId
argument_list|,
name|sourceBaseNamespaceDirPath
argument_list|,
name|sourceHFileArchiveDirPath
argument_list|)
expr_stmt|;
block|}
comment|/**    * If replication is enabled and this cluster is a master,    * it starts    * @throws IOException    */
annotation|@
name|Override
specifier|public
name|void
name|startReplicationService
parameter_list|()
throws|throws
name|IOException
block|{
name|this
operator|.
name|replicationManager
operator|.
name|init
argument_list|()
expr_stmt|;
name|this
operator|.
name|replicationSink
operator|=
operator|new
name|ReplicationSink
argument_list|(
name|this
operator|.
name|conf
argument_list|,
name|this
operator|.
name|server
argument_list|)
expr_stmt|;
name|this
operator|.
name|scheduleThreadPool
operator|.
name|scheduleAtFixedRate
argument_list|(
operator|new
name|ReplicationStatisticsTask
argument_list|(
name|this
operator|.
name|replicationSink
argument_list|,
name|this
operator|.
name|replicationManager
argument_list|)
argument_list|,
name|statsThreadPeriod
argument_list|,
name|statsThreadPeriod
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"{} started"
argument_list|,
name|this
operator|.
name|server
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**    * Get the replication sources manager    * @return the manager if replication is enabled, else returns false    */
specifier|public
name|ReplicationSourceManager
name|getReplicationManager
parameter_list|()
block|{
return|return
name|this
operator|.
name|replicationManager
return|;
block|}
name|void
name|addHFileRefsToQueue
parameter_list|(
name|TableName
name|tableName
parameter_list|,
name|byte
index|[]
name|family
parameter_list|,
name|List
argument_list|<
name|Pair
argument_list|<
name|Path
argument_list|,
name|Path
argument_list|>
argument_list|>
name|pairs
parameter_list|)
throws|throws
name|IOException
block|{
try|try
block|{
name|this
operator|.
name|replicationManager
operator|.
name|addHFileRefs
argument_list|(
name|tableName
argument_list|,
name|family
argument_list|,
name|pairs
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Failed to add hfile references in the replication queue."
argument_list|,
name|e
argument_list|)
expr_stmt|;
throw|throw
name|e
throw|;
block|}
block|}
comment|/**    * Statistics task. Periodically prints the cache statistics to the log.    */
specifier|private
specifier|final
specifier|static
class|class
name|ReplicationStatisticsTask
implements|implements
name|Runnable
block|{
specifier|private
specifier|final
name|ReplicationSink
name|replicationSink
decl_stmt|;
specifier|private
specifier|final
name|ReplicationSourceManager
name|replicationManager
decl_stmt|;
specifier|public
name|ReplicationStatisticsTask
parameter_list|(
name|ReplicationSink
name|replicationSink
parameter_list|,
name|ReplicationSourceManager
name|replicationManager
parameter_list|)
block|{
name|this
operator|.
name|replicationManager
operator|=
name|replicationManager
expr_stmt|;
name|this
operator|.
name|replicationSink
operator|=
name|replicationSink
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
name|printStats
argument_list|(
name|this
operator|.
name|replicationManager
operator|.
name|getStats
argument_list|()
argument_list|)
expr_stmt|;
name|printStats
argument_list|(
name|this
operator|.
name|replicationSink
operator|.
name|getStats
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|printStats
parameter_list|(
name|String
name|stats
parameter_list|)
block|{
if|if
condition|(
operator|!
name|stats
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
name|stats
argument_list|)
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|Override
specifier|public
name|ReplicationLoad
name|refreshAndGetReplicationLoad
parameter_list|()
block|{
if|if
condition|(
name|this
operator|.
name|replicationLoad
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
comment|// always build for latest data
name|buildReplicationLoad
argument_list|()
expr_stmt|;
return|return
name|this
operator|.
name|replicationLoad
return|;
block|}
specifier|private
name|void
name|buildReplicationLoad
parameter_list|()
block|{
name|List
argument_list|<
name|ReplicationSourceInterface
argument_list|>
name|allSources
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|allSources
operator|.
name|addAll
argument_list|(
name|this
operator|.
name|replicationManager
operator|.
name|getSources
argument_list|()
argument_list|)
expr_stmt|;
name|allSources
operator|.
name|addAll
argument_list|(
name|this
operator|.
name|replicationManager
operator|.
name|getOldSources
argument_list|()
argument_list|)
expr_stmt|;
comment|// get sink
name|MetricsSink
name|sinkMetrics
init|=
name|this
operator|.
name|replicationSink
operator|.
name|getSinkMetrics
argument_list|()
decl_stmt|;
name|this
operator|.
name|replicationLoad
operator|.
name|buildReplicationLoad
argument_list|(
name|allSources
argument_list|,
name|sinkMetrics
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|SyncReplicationPeerInfoProvider
name|getSyncReplicationPeerInfoProvider
parameter_list|()
block|{
return|return
name|syncReplicationPeerInfoProvider
return|;
block|}
annotation|@
name|Override
specifier|public
name|ReplicationPeers
name|getReplicationPeers
parameter_list|()
block|{
return|return
name|replicationPeers
return|;
block|}
block|}
end_class

end_unit

