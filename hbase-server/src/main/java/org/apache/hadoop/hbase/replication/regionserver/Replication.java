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
name|zookeeper
operator|.
name|KeeperException
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
implements|implements
name|WALActionsListener
implements|,
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
name|ReplicationSourceManager
name|replicationManager
decl_stmt|;
specifier|private
specifier|final
name|AtomicBoolean
name|replicating
init|=
operator|new
name|AtomicBoolean
argument_list|(
literal|true
argument_list|)
decl_stmt|;
specifier|private
name|ReplicationZookeeper
name|zkHelper
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
name|replication
condition|)
block|{
try|try
block|{
name|this
operator|.
name|zkHelper
operator|=
operator|new
name|ReplicationZookeeper
argument_list|(
name|server
argument_list|,
name|this
operator|.
name|replicating
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
literal|"Failed replication handler create "
operator|+
literal|"(replicating="
operator|+
name|this
operator|.
name|replicating
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
name|zkHelper
argument_list|,
name|conf
argument_list|,
name|this
operator|.
name|server
argument_list|,
name|fs
argument_list|,
name|this
operator|.
name|replicating
argument_list|,
name|logDir
argument_list|,
name|oldLogDir
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
name|zkHelper
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
literal|false
argument_list|)
return|;
block|}
comment|/*     * Returns an object to listen to new hlog changes     **/
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
name|this
operator|.
name|replicationSink
operator|.
name|stopReplicationSinkServices
argument_list|()
expr_stmt|;
block|}
block|}
comment|/**    * Carry on the list of log entries down to the sink    * @param entries list of entries to replicate    * @throws IOException    */
specifier|public
name|void
name|replicateLogEntries
parameter_list|(
name|HLog
operator|.
name|Entry
index|[]
name|entries
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
name|HRegionInfo
name|info
parameter_list|,
name|HLogKey
name|logKey
parameter_list|,
name|WALEdit
name|logEdit
parameter_list|)
block|{
comment|// Not interested
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
name|HLogKey
name|logKey
parameter_list|,
name|WALEdit
name|logEdit
parameter_list|)
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
for|for
control|(
name|KeyValue
name|kv
range|:
name|logEdit
operator|.
name|getKeyValues
argument_list|()
control|)
block|{
name|family
operator|=
name|kv
operator|.
name|getFamily
argument_list|()
expr_stmt|;
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
operator|&&
operator|!
name|scopes
operator|.
name|containsKey
argument_list|(
name|family
argument_list|)
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
if|if
condition|(
operator|!
name|scopes
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|logEdit
operator|.
name|setScopes
argument_list|(
name|scopes
argument_list|)
expr_stmt|;
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
annotation|@
name|Override
specifier|public
name|void
name|preLogArchive
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
comment|// Not interested
block|}
annotation|@
name|Override
specifier|public
name|void
name|postLogArchive
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
comment|// Not interested
block|}
comment|/**    * This method modifies the master's configuration in order to inject    * replication-related features    * @param conf    */
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
if|if
condition|(
operator|!
name|plugins
operator|.
name|contains
argument_list|(
name|ReplicationLogCleaner
operator|.
name|class
operator|.
name|toString
argument_list|()
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
name|ReplicationLogCleaner
operator|.
name|class
operator|.
name|getCanonicalName
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|logRollRequested
parameter_list|()
block|{
comment|// Not interested
block|}
annotation|@
name|Override
specifier|public
name|void
name|logCloseRequested
parameter_list|()
block|{
comment|// not interested
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
block|}
end_class

end_unit

