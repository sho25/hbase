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
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|HConstants
operator|.
name|HBASE_MASTER_LOGCLEANER_PLUGINS
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
name|HConstants
operator|.
name|REPLICATION_ENABLE_KEY
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
name|HConstants
operator|.
name|REPLICATION_SCOPE_LOCAL
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
name|List
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|NavigableMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|TreeMap
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
name|classification
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
name|hadoop
operator|.
name|hbase
operator|.
name|master
operator|.
name|cleaner
operator|.
name|HFileCleaner
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
name|protobuf
operator|.
name|generated
operator|.
name|AdminProtos
operator|.
name|WALEntry
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
name|WALProtos
operator|.
name|BulkLoadDescriptor
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
name|WALProtos
operator|.
name|StoreDescriptor
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
name|wal
operator|.
name|WALKey
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
name|ReplicationException
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
name|master
operator|.
name|ReplicationHFileCleaner
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
name|master
operator|.
name|ReplicationLogCleaner
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
name|ZKClusterId
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

begin_comment
comment|/**  * Gateway to Replication.  Used by {@link org.apache.hadoop.hbase.regionserver.HRegionServer}.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|Replication
extends|extends
name|WALActionsListener
operator|.
name|Base
implements|implements
name|ReplicationSourceService
implements|,
name|ReplicationSinkService
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
name|Replication
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|boolean
name|replication
decl_stmt|;
specifier|private
name|boolean
name|replicationForBulkLoadData
decl_stmt|;
specifier|private
name|ReplicationSourceManager
name|replicationManager
decl_stmt|;
specifier|private
name|ReplicationQueues
name|replicationQueues
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
comment|/**    * Instantiate the replication management (if rep is enabled).    * @param server Hosting server    * @param fs handle to the filesystem    * @param logDir    * @param oldLogDir directory where logs are archived    * @throws IOException    */
specifier|public
name|Replication
parameter_list|(
specifier|final
name|Server
name|server
parameter_list|,
specifier|final
name|FileSystem
name|fs
parameter_list|,
specifier|final
name|Path
name|logDir
parameter_list|,
specifier|final
name|Path
name|oldLogDir
parameter_list|)
throws|throws
name|IOException
block|{
name|initialize
argument_list|(
name|server
argument_list|,
name|fs
argument_list|,
name|logDir
argument_list|,
name|oldLogDir
argument_list|)
expr_stmt|;
block|}
comment|/**    * Empty constructor    */
specifier|public
name|Replication
parameter_list|()
block|{   }
specifier|public
name|void
name|initialize
parameter_list|(
specifier|final
name|Server
name|server
parameter_list|,
specifier|final
name|FileSystem
name|fs
parameter_list|,
specifier|final
name|Path
name|logDir
parameter_list|,
specifier|final
name|Path
name|oldLogDir
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
name|replication
operator|=
name|isReplication
argument_list|(
name|this
operator|.
name|conf
argument_list|)
expr_stmt|;
name|this
operator|.
name|replicationForBulkLoadData
operator|=
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
name|replicationForBulkLoadData
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
if|if
condition|(
name|replication
condition|)
block|{
try|try
block|{
name|this
operator|.
name|replicationQueues
operator|=
name|ReplicationFactory
operator|.
name|getReplicationQueues
argument_list|(
name|server
operator|.
name|getZooKeeper
argument_list|()
argument_list|,
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
name|replicationQueues
operator|.
name|init
argument_list|(
name|this
operator|.
name|server
operator|.
name|getServerName
argument_list|()
operator|.
name|toString
argument_list|()
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
argument_list|,
name|this
operator|.
name|server
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
name|replicationPeers
argument_list|,
name|this
operator|.
name|conf
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
name|ReplicationException
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
name|this
operator|.
name|replicationManager
operator|=
operator|new
name|ReplicationSourceManager
argument_list|(
name|replicationQueues
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
argument_list|)
expr_stmt|;
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
literal|"ReplicationStatisticsThread "
operator|+
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
block|}
else|else
block|{
name|this
operator|.
name|replicationManager
operator|=
literal|null
expr_stmt|;
name|this
operator|.
name|replicationQueues
operator|=
literal|null
expr_stmt|;
name|this
operator|.
name|replicationPeers
operator|=
literal|null
expr_stmt|;
name|this
operator|.
name|replicationTracker
operator|=
literal|null
expr_stmt|;
name|this
operator|.
name|replicationLoad
operator|=
literal|null
expr_stmt|;
block|}
block|}
comment|/**     * @param c Configuration to look at     * @return True if replication is enabled.     */
specifier|public
specifier|static
name|boolean
name|isReplication
parameter_list|(
specifier|final
name|Configuration
name|c
parameter_list|)
block|{
return|return
name|c
operator|.
name|getBoolean
argument_list|(
name|REPLICATION_ENABLE_KEY
argument_list|,
name|HConstants
operator|.
name|REPLICATION_ENABLE_DEFAULT
argument_list|)
return|;
block|}
comment|/**    * @param c Configuration to look at    * @return True if replication for bulk load data is enabled.    */
specifier|public
specifier|static
name|boolean
name|isReplicationForBulkLoadDataEnabled
parameter_list|(
specifier|final
name|Configuration
name|c
parameter_list|)
block|{
return|return
name|c
operator|.
name|getBoolean
argument_list|(
name|HConstants
operator|.
name|REPLICATION_BULKLOAD_ENABLE_KEY
argument_list|,
name|HConstants
operator|.
name|REPLICATION_BULKLOAD_ENABLE_DEFAULT
argument_list|)
return|;
block|}
comment|/*     * Returns an object to listen to new wal changes     **/
specifier|public
name|WALActionsListener
name|getWALActionsListener
parameter_list|()
block|{
return|return
name|this
return|;
block|}
comment|/**    * Stops replication service.    */
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
if|if
condition|(
name|this
operator|.
name|replication
condition|)
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
block|}
name|scheduleThreadPool
operator|.
name|shutdown
argument_list|()
expr_stmt|;
block|}
comment|/**    * Carry on the list of log entries down to the sink    * @param entries list of entries to replicate    * @param cells The data -- the cells -- that<code>entries</code> describes (the entries do not    *          contain the Cells we are replicating; they are passed here on the side in this    *          CellScanner).    * @param replicationClusterId Id which will uniquely identify source cluster FS client    *          configurations in the replication configuration directory    * @param sourceBaseNamespaceDirPath Path that point to the source cluster base namespace    *          directory required for replicating hfiles    * @param sourceHFileArchiveDirPath Path that point to the source cluster hfile archive directory    * @throws IOException    */
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
if|if
condition|(
name|this
operator|.
name|replication
condition|)
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
block|}
comment|/**    * If replication is enabled and this cluster is a master,    * it starts    * @throws IOException    */
specifier|public
name|void
name|startReplicationService
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|this
operator|.
name|replication
condition|)
block|{
try|try
block|{
name|this
operator|.
name|replicationManager
operator|.
name|init
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ReplicationException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
name|e
argument_list|)
throw|;
block|}
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
name|ReplicationStatisticsThread
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
block|}
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
annotation|@
name|Override
specifier|public
name|void
name|visitLogEntryBeforeWrite
parameter_list|(
name|HTableDescriptor
name|htd
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
name|scopeWALEdits
argument_list|(
name|htd
argument_list|,
name|logKey
argument_list|,
name|logEdit
argument_list|,
name|this
operator|.
name|conf
argument_list|,
name|this
operator|.
name|getReplicationManager
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**    * Utility method used to set the correct scopes on each log key. Doesn't set a scope on keys from    * compaction WAL edits and if the scope is local.    * @param htd Descriptor used to find the scope to use    * @param logKey Key that may get scoped according to its edits    * @param logEdit Edits used to lookup the scopes    * @param replicationManager Manager used to add bulk load events hfile references    * @throws IOException If failed to parse the WALEdit    */
specifier|public
specifier|static
name|void
name|scopeWALEdits
parameter_list|(
name|HTableDescriptor
name|htd
parameter_list|,
name|WALKey
name|logKey
parameter_list|,
name|WALEdit
name|logEdit
parameter_list|,
name|Configuration
name|conf
parameter_list|,
name|ReplicationSourceManager
name|replicationManager
parameter_list|)
throws|throws
name|IOException
block|{
name|NavigableMap
argument_list|<
name|byte
index|[]
argument_list|,
name|Integer
argument_list|>
name|scopes
init|=
operator|new
name|TreeMap
argument_list|<
name|byte
index|[]
argument_list|,
name|Integer
argument_list|>
argument_list|(
name|Bytes
operator|.
name|BYTES_COMPARATOR
argument_list|)
decl_stmt|;
name|byte
index|[]
name|family
decl_stmt|;
name|boolean
name|replicationForBulkLoadEnabled
init|=
name|isReplicationForBulkLoadDataEnabled
argument_list|(
name|conf
argument_list|)
decl_stmt|;
for|for
control|(
name|Cell
name|cell
range|:
name|logEdit
operator|.
name|getCells
argument_list|()
control|)
block|{
if|if
condition|(
name|CellUtil
operator|.
name|matchingFamily
argument_list|(
name|cell
argument_list|,
name|WALEdit
operator|.
name|METAFAMILY
argument_list|)
condition|)
block|{
if|if
condition|(
name|replicationForBulkLoadEnabled
operator|&&
name|CellUtil
operator|.
name|matchingQualifier
argument_list|(
name|cell
argument_list|,
name|WALEdit
operator|.
name|BULK_LOAD
argument_list|)
condition|)
block|{
name|scopeBulkLoadEdits
argument_list|(
name|htd
argument_list|,
name|replicationManager
argument_list|,
name|scopes
argument_list|,
name|logKey
operator|.
name|getTablename
argument_list|()
argument_list|,
name|cell
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// Skip the flush/compaction/region events
continue|continue;
block|}
block|}
else|else
block|{
name|family
operator|=
name|CellUtil
operator|.
name|cloneFamily
argument_list|(
name|cell
argument_list|)
expr_stmt|;
comment|// Unexpected, has a tendency to happen in unit tests
assert|assert
name|htd
operator|.
name|getFamily
argument_list|(
name|family
argument_list|)
operator|!=
literal|null
assert|;
if|if
condition|(
operator|!
name|scopes
operator|.
name|containsKey
argument_list|(
name|family
argument_list|)
condition|)
block|{
name|int
name|scope
init|=
name|htd
operator|.
name|getFamily
argument_list|(
name|family
argument_list|)
operator|.
name|getScope
argument_list|()
decl_stmt|;
if|if
condition|(
name|scope
operator|!=
name|REPLICATION_SCOPE_LOCAL
condition|)
block|{
name|scopes
operator|.
name|put
argument_list|(
name|family
argument_list|,
name|scope
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
if|if
condition|(
operator|!
name|scopes
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|logKey
operator|.
name|setScopes
argument_list|(
name|scopes
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
specifier|static
name|void
name|scopeBulkLoadEdits
parameter_list|(
name|HTableDescriptor
name|htd
parameter_list|,
name|ReplicationSourceManager
name|replicationManager
parameter_list|,
name|NavigableMap
argument_list|<
name|byte
index|[]
argument_list|,
name|Integer
argument_list|>
name|scopes
parameter_list|,
name|TableName
name|tableName
parameter_list|,
name|Cell
name|cell
parameter_list|)
throws|throws
name|IOException
block|{
name|byte
index|[]
name|family
decl_stmt|;
try|try
block|{
name|BulkLoadDescriptor
name|bld
init|=
name|WALEdit
operator|.
name|getBulkLoadDescriptor
argument_list|(
name|cell
argument_list|)
decl_stmt|;
for|for
control|(
name|StoreDescriptor
name|s
range|:
name|bld
operator|.
name|getStoresList
argument_list|()
control|)
block|{
name|family
operator|=
name|s
operator|.
name|getFamilyName
argument_list|()
operator|.
name|toByteArray
argument_list|()
expr_stmt|;
if|if
condition|(
operator|!
name|scopes
operator|.
name|containsKey
argument_list|(
name|family
argument_list|)
condition|)
block|{
name|int
name|scope
init|=
name|htd
operator|.
name|getFamily
argument_list|(
name|family
argument_list|)
operator|.
name|getScope
argument_list|()
decl_stmt|;
if|if
condition|(
name|scope
operator|!=
name|REPLICATION_SCOPE_LOCAL
condition|)
block|{
name|scopes
operator|.
name|put
argument_list|(
name|family
argument_list|,
name|scope
argument_list|)
expr_stmt|;
name|addHFileRefsToQueue
argument_list|(
name|replicationManager
argument_list|,
name|tableName
argument_list|,
name|family
argument_list|,
name|s
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|addHFileRefsToQueue
argument_list|(
name|replicationManager
argument_list|,
name|tableName
argument_list|,
name|family
argument_list|,
name|s
argument_list|)
expr_stmt|;
block|}
block|}
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
literal|"Failed to get bulk load events information from the wal file."
argument_list|,
name|e
argument_list|)
expr_stmt|;
throw|throw
name|e
throw|;
block|}
block|}
specifier|private
specifier|static
name|void
name|addHFileRefsToQueue
parameter_list|(
name|ReplicationSourceManager
name|replicationManager
parameter_list|,
name|TableName
name|tableName
parameter_list|,
name|byte
index|[]
name|family
parameter_list|,
name|StoreDescriptor
name|s
parameter_list|)
throws|throws
name|IOException
block|{
try|try
block|{
name|replicationManager
operator|.
name|addHFileRefs
argument_list|(
name|tableName
argument_list|,
name|family
argument_list|,
name|s
operator|.
name|getStoreFileList
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ReplicationException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Failed to create hfile references in ZK."
argument_list|,
name|e
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|IOException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|preLogRoll
parameter_list|(
name|Path
name|oldPath
parameter_list|,
name|Path
name|newPath
parameter_list|)
throws|throws
name|IOException
block|{
name|getReplicationManager
argument_list|()
operator|.
name|preLogRoll
argument_list|(
name|newPath
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|postLogRoll
parameter_list|(
name|Path
name|oldPath
parameter_list|,
name|Path
name|newPath
parameter_list|)
throws|throws
name|IOException
block|{
name|getReplicationManager
argument_list|()
operator|.
name|postLogRoll
argument_list|(
name|newPath
argument_list|)
expr_stmt|;
block|}
comment|/**    * This method modifies the master's configuration in order to inject replication-related features    * @param conf    */
specifier|public
specifier|static
name|void
name|decorateMasterConfiguration
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
if|if
condition|(
operator|!
name|isReplication
argument_list|(
name|conf
argument_list|)
condition|)
block|{
return|return;
block|}
name|String
name|plugins
init|=
name|conf
operator|.
name|get
argument_list|(
name|HBASE_MASTER_LOGCLEANER_PLUGINS
argument_list|)
decl_stmt|;
name|String
name|cleanerClass
init|=
name|ReplicationLogCleaner
operator|.
name|class
operator|.
name|getCanonicalName
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|plugins
operator|.
name|contains
argument_list|(
name|cleanerClass
argument_list|)
condition|)
block|{
name|conf
operator|.
name|set
argument_list|(
name|HBASE_MASTER_LOGCLEANER_PLUGINS
argument_list|,
name|plugins
operator|+
literal|","
operator|+
name|cleanerClass
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|isReplicationForBulkLoadDataEnabled
argument_list|(
name|conf
argument_list|)
condition|)
block|{
name|plugins
operator|=
name|conf
operator|.
name|get
argument_list|(
name|HFileCleaner
operator|.
name|MASTER_HFILE_CLEANER_PLUGINS
argument_list|)
expr_stmt|;
name|cleanerClass
operator|=
name|ReplicationHFileCleaner
operator|.
name|class
operator|.
name|getCanonicalName
argument_list|()
expr_stmt|;
if|if
condition|(
operator|!
name|plugins
operator|.
name|contains
argument_list|(
name|cleanerClass
argument_list|)
condition|)
block|{
name|conf
operator|.
name|set
argument_list|(
name|HFileCleaner
operator|.
name|MASTER_HFILE_CLEANER_PLUGINS
argument_list|,
name|plugins
operator|+
literal|","
operator|+
name|cleanerClass
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|/*    * Statistics thread. Periodically prints the cache statistics to the log.    */
specifier|static
class|class
name|ReplicationStatisticsThread
extends|extends
name|Thread
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
name|ReplicationStatisticsThread
parameter_list|(
specifier|final
name|ReplicationSink
name|replicationSink
parameter_list|,
specifier|final
name|ReplicationSourceManager
name|replicationManager
parameter_list|)
block|{
name|super
argument_list|(
literal|"ReplicationStatisticsThread"
argument_list|)
expr_stmt|;
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
comment|// get source
name|List
argument_list|<
name|ReplicationSourceInterface
argument_list|>
name|sources
init|=
name|this
operator|.
name|replicationManager
operator|.
name|getSources
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|MetricsSource
argument_list|>
name|sourceMetricsList
init|=
operator|new
name|ArrayList
argument_list|<
name|MetricsSource
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|ReplicationSourceInterface
name|source
range|:
name|sources
control|)
block|{
if|if
condition|(
name|source
operator|instanceof
name|ReplicationSource
condition|)
block|{
name|sourceMetricsList
operator|.
name|add
argument_list|(
operator|(
operator|(
name|ReplicationSource
operator|)
name|source
operator|)
operator|.
name|getSourceMetrics
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
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
name|sourceMetricsList
argument_list|,
name|sinkMetrics
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

