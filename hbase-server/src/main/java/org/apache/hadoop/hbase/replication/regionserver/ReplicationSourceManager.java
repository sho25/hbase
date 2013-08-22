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
name|ArrayList
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collections
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashMap
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
name|Map
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Random
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
name|TreeSet
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
name|LinkedBlockingQueue
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
name|RejectedExecutionException
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
name|ThreadPoolExecutor
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
name|Stoppable
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
name|ReplicationListener
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
comment|/**  * This class is responsible to manage all the replication  * sources. There are two classes of sources:  *<li> Normal sources are persistent and one per peer cluster</li>  *<li> Old sources are recovered from a failed region server and our  * only goal is to finish replicating the HLog queue it had up in ZK</li>  *  * When a region server dies, this class uses a watcher to get notified and it  * tries to grab a lock in order to transfer all the queues in a local  * old source.  *  * This class implements the ReplicationListener interface so that it can track changes in  * replication state.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|ReplicationSourceManager
implements|implements
name|ReplicationListener
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
name|ReplicationSourceManager
operator|.
name|class
argument_list|)
decl_stmt|;
comment|// List of all the sources that read this RS's logs
specifier|private
specifier|final
name|List
argument_list|<
name|ReplicationSourceInterface
argument_list|>
name|sources
decl_stmt|;
comment|// List of all the sources we got from died RSs
specifier|private
specifier|final
name|List
argument_list|<
name|ReplicationSourceInterface
argument_list|>
name|oldsources
decl_stmt|;
specifier|private
specifier|final
name|ReplicationQueues
name|replicationQueues
decl_stmt|;
specifier|private
specifier|final
name|ReplicationTracker
name|replicationTracker
decl_stmt|;
specifier|private
specifier|final
name|ReplicationPeers
name|replicationPeers
decl_stmt|;
comment|// UUID for this cluster
specifier|private
specifier|final
name|UUID
name|clusterId
decl_stmt|;
comment|// All about stopping
specifier|private
specifier|final
name|Stoppable
name|stopper
decl_stmt|;
comment|// All logs we are currently tracking
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|SortedSet
argument_list|<
name|String
argument_list|>
argument_list|>
name|hlogsById
decl_stmt|;
specifier|private
specifier|final
name|Configuration
name|conf
decl_stmt|;
specifier|private
specifier|final
name|FileSystem
name|fs
decl_stmt|;
comment|// The path to the latest log we saw, for new coming sources
specifier|private
name|Path
name|latestPath
decl_stmt|;
comment|// Path to the hlogs directories
specifier|private
specifier|final
name|Path
name|logDir
decl_stmt|;
comment|// Path to the hlog archive
specifier|private
specifier|final
name|Path
name|oldLogDir
decl_stmt|;
comment|// The number of ms that we wait before moving znodes, HBASE-3596
specifier|private
specifier|final
name|long
name|sleepBeforeFailover
decl_stmt|;
comment|// Homemade executer service for replication
specifier|private
specifier|final
name|ThreadPoolExecutor
name|executor
decl_stmt|;
specifier|private
specifier|final
name|Random
name|rand
decl_stmt|;
comment|/**    * Creates a replication manager and sets the watch on all the other registered region servers    * @param replicationQueues the interface for manipulating replication queues    * @param replicationPeers    * @param replicationTracker    * @param conf the configuration to use    * @param stopper the stopper object for this region server    * @param fs the file system to use    * @param logDir the directory that contains all hlog directories of live RSs    * @param oldLogDir the directory where old logs are archived    * @param clusterId    */
specifier|public
name|ReplicationSourceManager
parameter_list|(
specifier|final
name|ReplicationQueues
name|replicationQueues
parameter_list|,
specifier|final
name|ReplicationPeers
name|replicationPeers
parameter_list|,
specifier|final
name|ReplicationTracker
name|replicationTracker
parameter_list|,
specifier|final
name|Configuration
name|conf
parameter_list|,
specifier|final
name|Stoppable
name|stopper
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
parameter_list|,
specifier|final
name|UUID
name|clusterId
parameter_list|)
block|{
name|this
operator|.
name|sources
operator|=
operator|new
name|ArrayList
argument_list|<
name|ReplicationSourceInterface
argument_list|>
argument_list|()
expr_stmt|;
name|this
operator|.
name|replicationQueues
operator|=
name|replicationQueues
expr_stmt|;
name|this
operator|.
name|replicationPeers
operator|=
name|replicationPeers
expr_stmt|;
name|this
operator|.
name|replicationTracker
operator|=
name|replicationTracker
expr_stmt|;
name|this
operator|.
name|stopper
operator|=
name|stopper
expr_stmt|;
name|this
operator|.
name|hlogsById
operator|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|SortedSet
argument_list|<
name|String
argument_list|>
argument_list|>
argument_list|()
expr_stmt|;
name|this
operator|.
name|oldsources
operator|=
operator|new
name|ArrayList
argument_list|<
name|ReplicationSourceInterface
argument_list|>
argument_list|()
expr_stmt|;
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
name|this
operator|.
name|fs
operator|=
name|fs
expr_stmt|;
name|this
operator|.
name|logDir
operator|=
name|logDir
expr_stmt|;
name|this
operator|.
name|oldLogDir
operator|=
name|oldLogDir
expr_stmt|;
name|this
operator|.
name|sleepBeforeFailover
operator|=
name|conf
operator|.
name|getLong
argument_list|(
literal|"replication.sleep.before.failover"
argument_list|,
literal|2000
argument_list|)
expr_stmt|;
name|this
operator|.
name|clusterId
operator|=
name|clusterId
expr_stmt|;
name|this
operator|.
name|replicationTracker
operator|.
name|registerListener
argument_list|(
name|this
argument_list|)
expr_stmt|;
name|this
operator|.
name|replicationPeers
operator|.
name|getAllPeerIds
argument_list|()
expr_stmt|;
comment|// It's preferable to failover 1 RS at a time, but with good zk servers
comment|// more could be processed at the same time.
name|int
name|nbWorkers
init|=
name|conf
operator|.
name|getInt
argument_list|(
literal|"replication.executor.workers"
argument_list|,
literal|1
argument_list|)
decl_stmt|;
comment|// use a short 100ms sleep since this could be done inline with a RS startup
comment|// even if we fail, other region servers can take care of it
name|this
operator|.
name|executor
operator|=
operator|new
name|ThreadPoolExecutor
argument_list|(
name|nbWorkers
argument_list|,
name|nbWorkers
argument_list|,
literal|100
argument_list|,
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|,
operator|new
name|LinkedBlockingQueue
argument_list|<
name|Runnable
argument_list|>
argument_list|()
argument_list|)
expr_stmt|;
name|ThreadFactoryBuilder
name|tfb
init|=
operator|new
name|ThreadFactoryBuilder
argument_list|()
decl_stmt|;
name|tfb
operator|.
name|setNameFormat
argument_list|(
literal|"ReplicationExecutor-%d"
argument_list|)
expr_stmt|;
name|this
operator|.
name|executor
operator|.
name|setThreadFactory
argument_list|(
name|tfb
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
name|this
operator|.
name|rand
operator|=
operator|new
name|Random
argument_list|()
expr_stmt|;
block|}
comment|/**    * Provide the id of the peer and a log key and this method will figure which    * hlog it belongs to and will log, for this region server, the current    * position. It will also clean old logs from the queue.    * @param log Path to the log currently being replicated from    * replication status in zookeeper. It will also delete older entries.    * @param id id of the peer cluster    * @param position current location in the log    * @param queueRecovered indicates if this queue comes from another region server    * @param holdLogInZK if true then the log is retained in ZK    */
specifier|public
name|void
name|logPositionAndCleanOldLogs
parameter_list|(
name|Path
name|log
parameter_list|,
name|String
name|id
parameter_list|,
name|long
name|position
parameter_list|,
name|boolean
name|queueRecovered
parameter_list|,
name|boolean
name|holdLogInZK
parameter_list|)
block|{
name|String
name|fileName
init|=
name|log
operator|.
name|getName
argument_list|()
decl_stmt|;
name|this
operator|.
name|replicationQueues
operator|.
name|setLogPosition
argument_list|(
name|id
argument_list|,
name|fileName
argument_list|,
name|position
argument_list|)
expr_stmt|;
if|if
condition|(
name|holdLogInZK
condition|)
block|{
return|return;
block|}
name|cleanOldLogs
argument_list|(
name|fileName
argument_list|,
name|id
argument_list|,
name|queueRecovered
argument_list|)
expr_stmt|;
block|}
comment|/**    * Cleans a log file and all older files from ZK. Called when we are sure that a    * log file is closed and has no more entries.    * @param key Path to the log    * @param id id of the peer cluster    * @param queueRecovered Whether this is a recovered queue    */
specifier|public
name|void
name|cleanOldLogs
parameter_list|(
name|String
name|key
parameter_list|,
name|String
name|id
parameter_list|,
name|boolean
name|queueRecovered
parameter_list|)
block|{
synchronized|synchronized
init|(
name|this
operator|.
name|hlogsById
init|)
block|{
name|SortedSet
argument_list|<
name|String
argument_list|>
name|hlogs
init|=
name|this
operator|.
name|hlogsById
operator|.
name|get
argument_list|(
name|id
argument_list|)
decl_stmt|;
if|if
condition|(
name|queueRecovered
operator|||
name|hlogs
operator|.
name|first
argument_list|()
operator|.
name|equals
argument_list|(
name|key
argument_list|)
condition|)
block|{
return|return;
block|}
name|SortedSet
argument_list|<
name|String
argument_list|>
name|hlogSet
init|=
name|hlogs
operator|.
name|headSet
argument_list|(
name|key
argument_list|)
decl_stmt|;
for|for
control|(
name|String
name|hlog
range|:
name|hlogSet
control|)
block|{
name|this
operator|.
name|replicationQueues
operator|.
name|removeLog
argument_list|(
name|id
argument_list|,
name|hlog
argument_list|)
expr_stmt|;
block|}
name|hlogSet
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
block|}
comment|/**    * Adds a normal source per registered peer cluster and tries to process all    * old region server hlog queues    */
specifier|protected
name|void
name|init
parameter_list|()
throws|throws
name|IOException
throws|,
name|ReplicationException
block|{
for|for
control|(
name|String
name|id
range|:
name|this
operator|.
name|replicationPeers
operator|.
name|getConnectedPeers
argument_list|()
control|)
block|{
name|addSource
argument_list|(
name|id
argument_list|)
expr_stmt|;
block|}
name|List
argument_list|<
name|String
argument_list|>
name|currentReplicators
init|=
name|this
operator|.
name|replicationQueues
operator|.
name|getListOfReplicators
argument_list|()
decl_stmt|;
if|if
condition|(
name|currentReplicators
operator|==
literal|null
operator|||
name|currentReplicators
operator|.
name|size
argument_list|()
operator|==
literal|0
condition|)
block|{
return|return;
block|}
name|List
argument_list|<
name|String
argument_list|>
name|otherRegionServers
init|=
name|replicationTracker
operator|.
name|getListOfRegionServers
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Current list of replicators: "
operator|+
name|currentReplicators
operator|+
literal|" other RSs: "
operator|+
name|otherRegionServers
argument_list|)
expr_stmt|;
comment|// Look if there's anything to process after a restart
for|for
control|(
name|String
name|rs
range|:
name|currentReplicators
control|)
block|{
if|if
condition|(
operator|!
name|otherRegionServers
operator|.
name|contains
argument_list|(
name|rs
argument_list|)
condition|)
block|{
name|transferQueues
argument_list|(
name|rs
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|/**    * Add a new normal source to this region server    * @param id the id of the peer cluster    * @return the source that was created    * @throws IOException    */
specifier|protected
name|ReplicationSourceInterface
name|addSource
parameter_list|(
name|String
name|id
parameter_list|)
throws|throws
name|IOException
throws|,
name|ReplicationException
block|{
name|ReplicationSourceInterface
name|src
init|=
name|getReplicationSource
argument_list|(
name|this
operator|.
name|conf
argument_list|,
name|this
operator|.
name|fs
argument_list|,
name|this
argument_list|,
name|this
operator|.
name|replicationQueues
argument_list|,
name|this
operator|.
name|replicationPeers
argument_list|,
name|stopper
argument_list|,
name|id
argument_list|,
name|this
operator|.
name|clusterId
argument_list|)
decl_stmt|;
synchronized|synchronized
init|(
name|this
operator|.
name|hlogsById
init|)
block|{
name|this
operator|.
name|sources
operator|.
name|add
argument_list|(
name|src
argument_list|)
expr_stmt|;
name|this
operator|.
name|hlogsById
operator|.
name|put
argument_list|(
name|id
argument_list|,
operator|new
name|TreeSet
argument_list|<
name|String
argument_list|>
argument_list|()
argument_list|)
expr_stmt|;
comment|// Add the latest hlog to that source's queue
if|if
condition|(
name|this
operator|.
name|latestPath
operator|!=
literal|null
condition|)
block|{
name|String
name|name
init|=
name|this
operator|.
name|latestPath
operator|.
name|getName
argument_list|()
decl_stmt|;
name|this
operator|.
name|hlogsById
operator|.
name|get
argument_list|(
name|id
argument_list|)
operator|.
name|add
argument_list|(
name|name
argument_list|)
expr_stmt|;
try|try
block|{
name|this
operator|.
name|replicationQueues
operator|.
name|addLog
argument_list|(
name|src
operator|.
name|getPeerClusterZnode
argument_list|()
argument_list|,
name|name
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ReplicationException
name|e
parameter_list|)
block|{
name|String
name|message
init|=
literal|"Cannot add log to queue when creating a new source, queueId="
operator|+
name|src
operator|.
name|getPeerClusterZnode
argument_list|()
operator|+
literal|", filename="
operator|+
name|name
decl_stmt|;
name|stopper
operator|.
name|stop
argument_list|(
name|message
argument_list|)
expr_stmt|;
throw|throw
name|e
throw|;
block|}
name|src
operator|.
name|enqueueLog
argument_list|(
name|this
operator|.
name|latestPath
argument_list|)
expr_stmt|;
block|}
block|}
name|src
operator|.
name|startup
argument_list|()
expr_stmt|;
return|return
name|src
return|;
block|}
comment|/**    * Delete a complete queue of hlogs associated with a peer cluster    * @param peerId Id of the peer cluster queue of hlogs to delete    */
specifier|public
name|void
name|deleteSource
parameter_list|(
name|String
name|peerId
parameter_list|,
name|boolean
name|closeConnection
parameter_list|)
block|{
name|this
operator|.
name|replicationQueues
operator|.
name|removeQueue
argument_list|(
name|peerId
argument_list|)
expr_stmt|;
if|if
condition|(
name|closeConnection
condition|)
block|{
name|this
operator|.
name|replicationPeers
operator|.
name|disconnectFromPeer
argument_list|(
name|peerId
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Terminate the replication on this region server    */
specifier|public
name|void
name|join
parameter_list|()
block|{
name|this
operator|.
name|executor
operator|.
name|shutdown
argument_list|()
expr_stmt|;
if|if
condition|(
name|this
operator|.
name|sources
operator|.
name|size
argument_list|()
operator|==
literal|0
condition|)
block|{
name|this
operator|.
name|replicationQueues
operator|.
name|removeAllQueues
argument_list|()
expr_stmt|;
block|}
for|for
control|(
name|ReplicationSourceInterface
name|source
range|:
name|this
operator|.
name|sources
control|)
block|{
name|source
operator|.
name|terminate
argument_list|(
literal|"Region server is closing"
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Get a copy of the hlogs of the first source on this rs    * @return a sorted set of hlog names    */
specifier|protected
name|Map
argument_list|<
name|String
argument_list|,
name|SortedSet
argument_list|<
name|String
argument_list|>
argument_list|>
name|getHLogs
parameter_list|()
block|{
return|return
name|Collections
operator|.
name|unmodifiableMap
argument_list|(
name|hlogsById
argument_list|)
return|;
block|}
comment|/**    * Get a list of all the normal sources of this rs    * @return lis of all sources    */
specifier|public
name|List
argument_list|<
name|ReplicationSourceInterface
argument_list|>
name|getSources
parameter_list|()
block|{
return|return
name|this
operator|.
name|sources
return|;
block|}
name|void
name|preLogRoll
parameter_list|(
name|Path
name|newLog
parameter_list|)
throws|throws
name|IOException
block|{
synchronized|synchronized
init|(
name|this
operator|.
name|hlogsById
init|)
block|{
name|String
name|name
init|=
name|newLog
operator|.
name|getName
argument_list|()
decl_stmt|;
for|for
control|(
name|ReplicationSourceInterface
name|source
range|:
name|this
operator|.
name|sources
control|)
block|{
try|try
block|{
name|this
operator|.
name|replicationQueues
operator|.
name|addLog
argument_list|(
name|source
operator|.
name|getPeerClusterZnode
argument_list|()
argument_list|,
name|name
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
literal|"Cannot add log to replication queue with id="
operator|+
name|source
operator|.
name|getPeerClusterZnode
argument_list|()
operator|+
literal|", filename="
operator|+
name|name
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
for|for
control|(
name|SortedSet
argument_list|<
name|String
argument_list|>
name|hlogs
range|:
name|this
operator|.
name|hlogsById
operator|.
name|values
argument_list|()
control|)
block|{
if|if
condition|(
name|this
operator|.
name|sources
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
comment|// If there's no slaves, don't need to keep the old hlogs since
comment|// we only consider the last one when a new slave comes in
name|hlogs
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
name|hlogs
operator|.
name|add
argument_list|(
name|name
argument_list|)
expr_stmt|;
block|}
block|}
name|this
operator|.
name|latestPath
operator|=
name|newLog
expr_stmt|;
block|}
name|void
name|postLogRoll
parameter_list|(
name|Path
name|newLog
parameter_list|)
throws|throws
name|IOException
block|{
comment|// This only updates the sources we own, not the recovered ones
for|for
control|(
name|ReplicationSourceInterface
name|source
range|:
name|this
operator|.
name|sources
control|)
block|{
name|source
operator|.
name|enqueueLog
argument_list|(
name|newLog
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Factory method to create a replication source    * @param conf the configuration to use    * @param fs the file system to use    * @param manager the manager to use    * @param stopper the stopper object for this region server    * @param peerId the id of the peer cluster    * @return the created source    * @throws IOException    */
specifier|protected
name|ReplicationSourceInterface
name|getReplicationSource
parameter_list|(
specifier|final
name|Configuration
name|conf
parameter_list|,
specifier|final
name|FileSystem
name|fs
parameter_list|,
specifier|final
name|ReplicationSourceManager
name|manager
parameter_list|,
specifier|final
name|ReplicationQueues
name|replicationQueues
parameter_list|,
specifier|final
name|ReplicationPeers
name|replicationPeers
parameter_list|,
specifier|final
name|Stoppable
name|stopper
parameter_list|,
specifier|final
name|String
name|peerId
parameter_list|,
specifier|final
name|UUID
name|clusterId
parameter_list|)
throws|throws
name|IOException
block|{
name|ReplicationSourceInterface
name|src
decl_stmt|;
try|try
block|{
annotation|@
name|SuppressWarnings
argument_list|(
literal|"rawtypes"
argument_list|)
name|Class
name|c
init|=
name|Class
operator|.
name|forName
argument_list|(
name|conf
operator|.
name|get
argument_list|(
literal|"replication.replicationsource.implementation"
argument_list|,
name|ReplicationSource
operator|.
name|class
operator|.
name|getCanonicalName
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|src
operator|=
operator|(
name|ReplicationSourceInterface
operator|)
name|c
operator|.
name|newInstance
argument_list|()
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
name|warn
argument_list|(
literal|"Passed replication source implementation throws errors, "
operator|+
literal|"defaulting to ReplicationSource"
argument_list|,
name|e
argument_list|)
expr_stmt|;
name|src
operator|=
operator|new
name|ReplicationSource
argument_list|()
expr_stmt|;
block|}
name|src
operator|.
name|init
argument_list|(
name|conf
argument_list|,
name|fs
argument_list|,
name|manager
argument_list|,
name|replicationQueues
argument_list|,
name|replicationPeers
argument_list|,
name|stopper
argument_list|,
name|peerId
argument_list|,
name|clusterId
argument_list|)
expr_stmt|;
return|return
name|src
return|;
block|}
comment|/**    * Transfer all the queues of the specified to this region server.    * First it tries to grab a lock and if it works it will move the    * znodes and finally will delete the old znodes.    *    * It creates one old source for any type of source of the old rs.    * @param rsZnode    */
specifier|private
name|void
name|transferQueues
parameter_list|(
name|String
name|rsZnode
parameter_list|)
block|{
name|NodeFailoverWorker
name|transfer
init|=
operator|new
name|NodeFailoverWorker
argument_list|(
name|rsZnode
argument_list|,
name|this
operator|.
name|replicationQueues
argument_list|,
name|this
operator|.
name|replicationPeers
argument_list|,
name|this
operator|.
name|clusterId
argument_list|)
decl_stmt|;
try|try
block|{
name|this
operator|.
name|executor
operator|.
name|execute
argument_list|(
name|transfer
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|RejectedExecutionException
name|ex
parameter_list|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Cancelling the transfer of "
operator|+
name|rsZnode
operator|+
literal|" because of "
operator|+
name|ex
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Clear the references to the specified old source    * @param src source to clear    */
specifier|public
name|void
name|closeRecoveredQueue
parameter_list|(
name|ReplicationSourceInterface
name|src
parameter_list|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Done with the recovered queue "
operator|+
name|src
operator|.
name|getPeerClusterZnode
argument_list|()
argument_list|)
expr_stmt|;
name|this
operator|.
name|oldsources
operator|.
name|remove
argument_list|(
name|src
argument_list|)
expr_stmt|;
name|deleteSource
argument_list|(
name|src
operator|.
name|getPeerClusterZnode
argument_list|()
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
comment|/**    * Thie method first deletes all the recovered sources for the specified    * id, then deletes the normal source (deleting all related data in ZK).    * @param id The id of the peer cluster    */
specifier|public
name|void
name|removePeer
parameter_list|(
name|String
name|id
parameter_list|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Closing the following queue "
operator|+
name|id
operator|+
literal|", currently have "
operator|+
name|sources
operator|.
name|size
argument_list|()
operator|+
literal|" and another "
operator|+
name|oldsources
operator|.
name|size
argument_list|()
operator|+
literal|" that were recovered"
argument_list|)
expr_stmt|;
name|String
name|terminateMessage
init|=
literal|"Replication stream was removed by a user"
decl_stmt|;
name|ReplicationSourceInterface
name|srcToRemove
init|=
literal|null
decl_stmt|;
name|List
argument_list|<
name|ReplicationSourceInterface
argument_list|>
name|oldSourcesToDelete
init|=
operator|new
name|ArrayList
argument_list|<
name|ReplicationSourceInterface
argument_list|>
argument_list|()
decl_stmt|;
comment|// First close all the recovered sources for this peer
for|for
control|(
name|ReplicationSourceInterface
name|src
range|:
name|oldsources
control|)
block|{
if|if
condition|(
name|id
operator|.
name|equals
argument_list|(
name|src
operator|.
name|getPeerClusterId
argument_list|()
argument_list|)
condition|)
block|{
name|oldSourcesToDelete
operator|.
name|add
argument_list|(
name|src
argument_list|)
expr_stmt|;
block|}
block|}
for|for
control|(
name|ReplicationSourceInterface
name|src
range|:
name|oldSourcesToDelete
control|)
block|{
name|src
operator|.
name|terminate
argument_list|(
name|terminateMessage
argument_list|)
expr_stmt|;
name|closeRecoveredQueue
argument_list|(
operator|(
name|src
operator|)
argument_list|)
expr_stmt|;
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"Number of deleted recovered sources for "
operator|+
name|id
operator|+
literal|": "
operator|+
name|oldSourcesToDelete
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
comment|// Now look for the one on this cluster
for|for
control|(
name|ReplicationSourceInterface
name|src
range|:
name|this
operator|.
name|sources
control|)
block|{
if|if
condition|(
name|id
operator|.
name|equals
argument_list|(
name|src
operator|.
name|getPeerClusterId
argument_list|()
argument_list|)
condition|)
block|{
name|srcToRemove
operator|=
name|src
expr_stmt|;
break|break;
block|}
block|}
if|if
condition|(
name|srcToRemove
operator|==
literal|null
condition|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"The queue we wanted to close is missing "
operator|+
name|id
argument_list|)
expr_stmt|;
return|return;
block|}
name|srcToRemove
operator|.
name|terminate
argument_list|(
name|terminateMessage
argument_list|)
expr_stmt|;
name|this
operator|.
name|sources
operator|.
name|remove
argument_list|(
name|srcToRemove
argument_list|)
expr_stmt|;
name|deleteSource
argument_list|(
name|id
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|regionServerRemoved
parameter_list|(
name|String
name|regionserver
parameter_list|)
block|{
name|transferQueues
argument_list|(
name|regionserver
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|peerRemoved
parameter_list|(
name|String
name|peerId
parameter_list|)
block|{
name|removePeer
argument_list|(
name|peerId
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|peerListChanged
parameter_list|(
name|List
argument_list|<
name|String
argument_list|>
name|peerIds
parameter_list|)
block|{
for|for
control|(
name|String
name|id
range|:
name|peerIds
control|)
block|{
try|try
block|{
name|boolean
name|added
init|=
name|this
operator|.
name|replicationPeers
operator|.
name|connectToPeer
argument_list|(
name|id
argument_list|)
decl_stmt|;
if|if
condition|(
name|added
condition|)
block|{
name|addSource
argument_list|(
name|id
argument_list|)
expr_stmt|;
block|}
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
literal|"Error while adding a new peer"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|/**    * Class responsible to setup new ReplicationSources to take care of the    * queues from dead region servers.    */
class|class
name|NodeFailoverWorker
extends|extends
name|Thread
block|{
specifier|private
name|String
name|rsZnode
decl_stmt|;
specifier|private
specifier|final
name|ReplicationQueues
name|rq
decl_stmt|;
specifier|private
specifier|final
name|ReplicationPeers
name|rp
decl_stmt|;
specifier|private
specifier|final
name|UUID
name|clusterId
decl_stmt|;
comment|/**      *      * @param rsZnode      */
specifier|public
name|NodeFailoverWorker
parameter_list|(
name|String
name|rsZnode
parameter_list|,
specifier|final
name|ReplicationQueues
name|replicationQueues
parameter_list|,
specifier|final
name|ReplicationPeers
name|replicationPeers
parameter_list|,
specifier|final
name|UUID
name|clusterId
parameter_list|)
block|{
name|super
argument_list|(
literal|"Failover-for-"
operator|+
name|rsZnode
argument_list|)
expr_stmt|;
name|this
operator|.
name|rsZnode
operator|=
name|rsZnode
expr_stmt|;
name|this
operator|.
name|rq
operator|=
name|replicationQueues
expr_stmt|;
name|this
operator|.
name|rp
operator|=
name|replicationPeers
expr_stmt|;
name|this
operator|.
name|clusterId
operator|=
name|clusterId
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
comment|// Wait a bit before transferring the queues, we may be shutting down.
comment|// This sleep may not be enough in some cases.
try|try
block|{
name|Thread
operator|.
name|sleep
argument_list|(
name|sleepBeforeFailover
operator|+
call|(
name|long
call|)
argument_list|(
name|rand
operator|.
name|nextFloat
argument_list|()
operator|*
name|sleepBeforeFailover
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Interrupted while waiting before transferring a queue."
argument_list|)
expr_stmt|;
name|Thread
operator|.
name|currentThread
argument_list|()
operator|.
name|interrupt
argument_list|()
expr_stmt|;
block|}
comment|// We try to lock that rs' queue directory
if|if
condition|(
name|stopper
operator|.
name|isStopped
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Not transferring queue since we are shutting down"
argument_list|)
expr_stmt|;
return|return;
block|}
name|SortedMap
argument_list|<
name|String
argument_list|,
name|SortedSet
argument_list|<
name|String
argument_list|>
argument_list|>
name|newQueues
init|=
literal|null
decl_stmt|;
name|newQueues
operator|=
name|this
operator|.
name|rq
operator|.
name|claimQueues
argument_list|(
name|rsZnode
argument_list|)
expr_stmt|;
comment|// Copying over the failed queue is completed.
if|if
condition|(
name|newQueues
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
comment|// We either didn't get the lock or the failed region server didn't have any outstanding
comment|// HLogs to replicate, so we are done.
return|return;
block|}
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|SortedSet
argument_list|<
name|String
argument_list|>
argument_list|>
name|entry
range|:
name|newQueues
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|String
name|peerId
init|=
name|entry
operator|.
name|getKey
argument_list|()
decl_stmt|;
try|try
block|{
name|ReplicationSourceInterface
name|src
init|=
name|getReplicationSource
argument_list|(
name|conf
argument_list|,
name|fs
argument_list|,
name|ReplicationSourceManager
operator|.
name|this
argument_list|,
name|this
operator|.
name|rq
argument_list|,
name|this
operator|.
name|rp
argument_list|,
name|stopper
argument_list|,
name|peerId
argument_list|,
name|this
operator|.
name|clusterId
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|this
operator|.
name|rp
operator|.
name|getConnectedPeers
argument_list|()
operator|.
name|contains
argument_list|(
operator|(
name|src
operator|.
name|getPeerClusterId
argument_list|()
operator|)
argument_list|)
condition|)
block|{
name|src
operator|.
name|terminate
argument_list|(
literal|"Recovered queue doesn't belong to any current peer"
argument_list|)
expr_stmt|;
break|break;
block|}
name|oldsources
operator|.
name|add
argument_list|(
name|src
argument_list|)
expr_stmt|;
for|for
control|(
name|String
name|hlog
range|:
name|entry
operator|.
name|getValue
argument_list|()
control|)
block|{
name|src
operator|.
name|enqueueLog
argument_list|(
operator|new
name|Path
argument_list|(
name|oldLogDir
argument_list|,
name|hlog
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|src
operator|.
name|startup
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
comment|// TODO manage it
name|LOG
operator|.
name|error
argument_list|(
literal|"Failed creating a source"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
comment|/**    * Get the directory where hlogs are archived    * @return the directory where hlogs are archived    */
specifier|public
name|Path
name|getOldLogDir
parameter_list|()
block|{
return|return
name|this
operator|.
name|oldLogDir
return|;
block|}
comment|/**    * Get the directory where hlogs are stored by their RSs    * @return the directory where hlogs are stored by their RSs    */
specifier|public
name|Path
name|getLogDir
parameter_list|()
block|{
return|return
name|this
operator|.
name|logDir
return|;
block|}
comment|/**    * Get the handle on the local file system    * @return Handle on the local file system    */
specifier|public
name|FileSystem
name|getFs
parameter_list|()
block|{
return|return
name|this
operator|.
name|fs
return|;
block|}
comment|/**    * Get a string representation of all the sources' metrics    */
specifier|public
name|String
name|getStats
parameter_list|()
block|{
name|StringBuffer
name|stats
init|=
operator|new
name|StringBuffer
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
name|stats
operator|.
name|append
argument_list|(
literal|"Normal source for cluster "
operator|+
name|source
operator|.
name|getPeerClusterId
argument_list|()
operator|+
literal|": "
argument_list|)
expr_stmt|;
name|stats
operator|.
name|append
argument_list|(
name|source
operator|.
name|getStats
argument_list|()
operator|+
literal|"\n"
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|ReplicationSourceInterface
name|oldSource
range|:
name|oldsources
control|)
block|{
name|stats
operator|.
name|append
argument_list|(
literal|"Recovered source for cluster/machine(s) "
operator|+
name|oldSource
operator|.
name|getPeerClusterId
argument_list|()
operator|+
literal|": "
argument_list|)
expr_stmt|;
name|stats
operator|.
name|append
argument_list|(
name|oldSource
operator|.
name|getStats
argument_list|()
operator|+
literal|"\n"
argument_list|)
expr_stmt|;
block|}
return|return
name|stats
operator|.
name|toString
argument_list|()
return|;
block|}
block|}
end_class

end_unit

