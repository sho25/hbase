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
name|regionserver
operator|.
name|snapshot
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
name|Collection
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Iterator
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
name|concurrent
operator|.
name|Callable
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
name|ExecutionException
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
name|ExecutorCompletionService
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
name|Abortable
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
name|DroppedSnapshotException
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
name|HBaseInterfaceAudience
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
name|util
operator|.
name|Threads
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
name|ZKWatcher
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
name|yetus
operator|.
name|audience
operator|.
name|InterfaceStability
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
name|RegionReplicaUtil
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
name|errorhandling
operator|.
name|ForeignException
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
name|errorhandling
operator|.
name|ForeignExceptionDispatcher
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
name|snapshot
operator|.
name|MasterSnapshotVerifier
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
name|snapshot
operator|.
name|SnapshotManager
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
name|procedure
operator|.
name|ProcedureMember
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
name|procedure
operator|.
name|ProcedureMemberRpcs
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
name|procedure
operator|.
name|RegionServerProcedureManager
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
name|procedure
operator|.
name|Subprocedure
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
name|procedure
operator|.
name|SubprocedureFactory
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
name|procedure
operator|.
name|ZKProcedureMemberRpcs
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
name|regionserver
operator|.
name|RegionServerServices
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
name|SnapshotProtos
operator|.
name|SnapshotDescription
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
name|snapshot
operator|.
name|SnapshotCreationException
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

begin_comment
comment|/**  * This manager class handles the work dealing with snapshots for a {@link HRegionServer}.  *<p>  * This provides the mechanism necessary to kick off a online snapshot specific  * {@link Subprocedure} that is responsible for the regions being served by this region server.  * If any failures occur with the subprocedure, the RegionSeverSnapshotManager's subprocedure  * handler, {@link ProcedureMember}, notifies the master's ProcedureCoordinator to abort all  * others.  *<p>  * On startup, requires {@link #start()} to be called.  *<p>  * On shutdown, requires {@link #stop(boolean)} to be called  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|LimitedPrivate
argument_list|(
name|HBaseInterfaceAudience
operator|.
name|CONFIG
argument_list|)
annotation|@
name|InterfaceStability
operator|.
name|Unstable
specifier|public
class|class
name|RegionServerSnapshotManager
extends|extends
name|RegionServerProcedureManager
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
name|RegionServerSnapshotManager
operator|.
name|class
argument_list|)
decl_stmt|;
comment|/** Maximum number of snapshot region tasks that can run concurrently */
specifier|private
specifier|static
specifier|final
name|String
name|CONCURENT_SNAPSHOT_TASKS_KEY
init|=
literal|"hbase.snapshot.region.concurrentTasks"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|DEFAULT_CONCURRENT_SNAPSHOT_TASKS
init|=
literal|3
decl_stmt|;
comment|/** Conf key for number of request threads to start snapshots on regionservers */
specifier|public
specifier|static
specifier|final
name|String
name|SNAPSHOT_REQUEST_THREADS_KEY
init|=
literal|"hbase.snapshot.region.pool.threads"
decl_stmt|;
comment|/** # of threads for snapshotting regions on the rs. */
specifier|public
specifier|static
specifier|final
name|int
name|SNAPSHOT_REQUEST_THREADS_DEFAULT
init|=
literal|10
decl_stmt|;
comment|/** Conf key for max time to keep threads in snapshot request pool waiting */
specifier|public
specifier|static
specifier|final
name|String
name|SNAPSHOT_TIMEOUT_MILLIS_KEY
init|=
literal|"hbase.snapshot.region.timeout"
decl_stmt|;
comment|/** Keep threads alive in request pool for max of 300 seconds */
specifier|public
specifier|static
specifier|final
name|long
name|SNAPSHOT_TIMEOUT_MILLIS_DEFAULT
init|=
literal|5
operator|*
literal|60000
decl_stmt|;
comment|/** Conf key for millis between checks to see if snapshot completed or if there are errors*/
specifier|public
specifier|static
specifier|final
name|String
name|SNAPSHOT_REQUEST_WAKE_MILLIS_KEY
init|=
literal|"hbase.snapshot.region.wakefrequency"
decl_stmt|;
comment|/** Default amount of time to check for errors while regions finish snapshotting */
specifier|private
specifier|static
specifier|final
name|long
name|SNAPSHOT_REQUEST_WAKE_MILLIS_DEFAULT
init|=
literal|500
decl_stmt|;
specifier|private
name|RegionServerServices
name|rss
decl_stmt|;
specifier|private
name|ProcedureMemberRpcs
name|memberRpcs
decl_stmt|;
specifier|private
name|ProcedureMember
name|member
decl_stmt|;
comment|/**    * Exposed for testing.    * @param conf HBase configuration.    * @param parent parent running the snapshot handler    * @param memberRpc use specified memberRpc instance    * @param procMember use specified ProcedureMember    */
name|RegionServerSnapshotManager
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|HRegionServer
name|parent
parameter_list|,
name|ProcedureMemberRpcs
name|memberRpc
parameter_list|,
name|ProcedureMember
name|procMember
parameter_list|)
block|{
name|this
operator|.
name|rss
operator|=
name|parent
expr_stmt|;
name|this
operator|.
name|memberRpcs
operator|=
name|memberRpc
expr_stmt|;
name|this
operator|.
name|member
operator|=
name|procMember
expr_stmt|;
block|}
specifier|public
name|RegionServerSnapshotManager
parameter_list|()
block|{}
comment|/**    * Start accepting snapshot requests.    */
annotation|@
name|Override
specifier|public
name|void
name|start
parameter_list|()
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Start Snapshot Manager "
operator|+
name|rss
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
name|memberRpcs
operator|.
name|start
argument_list|(
name|rss
operator|.
name|getServerName
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|,
name|member
argument_list|)
expr_stmt|;
block|}
comment|/**    * Close<tt>this</tt> and all running snapshot tasks    * @param force forcefully stop all running tasks    * @throws IOException    */
annotation|@
name|Override
specifier|public
name|void
name|stop
parameter_list|(
name|boolean
name|force
parameter_list|)
throws|throws
name|IOException
block|{
name|String
name|mode
init|=
name|force
condition|?
literal|"abruptly"
else|:
literal|"gracefully"
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Stopping RegionServerSnapshotManager "
operator|+
name|mode
operator|+
literal|"."
argument_list|)
expr_stmt|;
try|try
block|{
name|this
operator|.
name|member
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
finally|finally
block|{
name|this
operator|.
name|memberRpcs
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
comment|/**    * If in a running state, creates the specified subprocedure for handling an online snapshot.    *    * Because this gets the local list of regions to snapshot and not the set the master had,    * there is a possibility of a race where regions may be missed.  This detected by the master in    * the snapshot verification step.    *    * @param snapshot    * @return Subprocedure to submit to the ProcedureMemeber.    */
specifier|public
name|Subprocedure
name|buildSubprocedure
parameter_list|(
name|SnapshotDescription
name|snapshot
parameter_list|)
block|{
comment|// don't run a snapshot if the parent is stop(ping)
if|if
condition|(
name|rss
operator|.
name|isStopping
argument_list|()
operator|||
name|rss
operator|.
name|isStopped
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"Can't start snapshot on RS: "
operator|+
name|rss
operator|.
name|getServerName
argument_list|()
operator|+
literal|", because stopping/stopped!"
argument_list|)
throw|;
block|}
comment|// check to see if this server is hosting any regions for the snapshots
comment|// check to see if we have regions for the snapshot
name|List
argument_list|<
name|HRegion
argument_list|>
name|involvedRegions
decl_stmt|;
try|try
block|{
name|involvedRegions
operator|=
name|getRegionsToSnapshot
argument_list|(
name|snapshot
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e1
parameter_list|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"Failed to figure out if we should handle a snapshot - "
operator|+
literal|"something has gone awry with the online regions."
argument_list|,
name|e1
argument_list|)
throw|;
block|}
comment|// We need to run the subprocedure even if we have no relevant regions.  The coordinator
comment|// expects participation in the procedure and without sending message the snapshot attempt
comment|// will hang and fail.
name|LOG
operator|.
name|debug
argument_list|(
literal|"Launching subprocedure for snapshot "
operator|+
name|snapshot
operator|.
name|getName
argument_list|()
operator|+
literal|" from table "
operator|+
name|snapshot
operator|.
name|getTable
argument_list|()
operator|+
literal|" type "
operator|+
name|snapshot
operator|.
name|getType
argument_list|()
argument_list|)
expr_stmt|;
name|ForeignExceptionDispatcher
name|exnDispatcher
init|=
operator|new
name|ForeignExceptionDispatcher
argument_list|(
name|snapshot
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
name|Configuration
name|conf
init|=
name|rss
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
name|long
name|timeoutMillis
init|=
name|conf
operator|.
name|getLong
argument_list|(
name|SNAPSHOT_TIMEOUT_MILLIS_KEY
argument_list|,
name|SNAPSHOT_TIMEOUT_MILLIS_DEFAULT
argument_list|)
decl_stmt|;
name|long
name|wakeMillis
init|=
name|conf
operator|.
name|getLong
argument_list|(
name|SNAPSHOT_REQUEST_WAKE_MILLIS_KEY
argument_list|,
name|SNAPSHOT_REQUEST_WAKE_MILLIS_DEFAULT
argument_list|)
decl_stmt|;
switch|switch
condition|(
name|snapshot
operator|.
name|getType
argument_list|()
condition|)
block|{
case|case
name|FLUSH
case|:
name|SnapshotSubprocedurePool
name|taskManager
init|=
operator|new
name|SnapshotSubprocedurePool
argument_list|(
name|rss
operator|.
name|getServerName
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|,
name|conf
argument_list|,
name|rss
argument_list|)
decl_stmt|;
return|return
operator|new
name|FlushSnapshotSubprocedure
argument_list|(
name|member
argument_list|,
name|exnDispatcher
argument_list|,
name|wakeMillis
argument_list|,
name|timeoutMillis
argument_list|,
name|involvedRegions
argument_list|,
name|snapshot
argument_list|,
name|taskManager
argument_list|)
return|;
case|case
name|SKIPFLUSH
case|:
comment|/*          * This is to take an online-snapshot without force a coordinated flush to prevent pause          * The snapshot type is defined inside the snapshot description. FlushSnapshotSubprocedure          * should be renamed to distributedSnapshotSubprocedure, and the flush() behavior can be          * turned on/off based on the flush type.          * To minimized the code change, class name is not changed.          */
name|SnapshotSubprocedurePool
name|taskManager2
init|=
operator|new
name|SnapshotSubprocedurePool
argument_list|(
name|rss
operator|.
name|getServerName
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|,
name|conf
argument_list|,
name|rss
argument_list|)
decl_stmt|;
return|return
operator|new
name|FlushSnapshotSubprocedure
argument_list|(
name|member
argument_list|,
name|exnDispatcher
argument_list|,
name|wakeMillis
argument_list|,
name|timeoutMillis
argument_list|,
name|involvedRegions
argument_list|,
name|snapshot
argument_list|,
name|taskManager2
argument_list|)
return|;
default|default:
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"Unrecognized snapshot type:"
operator|+
name|snapshot
operator|.
name|getType
argument_list|()
argument_list|)
throw|;
block|}
block|}
comment|/**    * Determine if the snapshot should be handled on this server    *    * NOTE: This is racy -- the master expects a list of regionservers.    * This means if a region moves somewhere between the calls we'll miss some regions.    * For example, a region move during a snapshot could result in a region to be skipped or done    * twice.  This is manageable because the {@link MasterSnapshotVerifier} will double check the    * region lists after the online portion of the snapshot completes and will explicitly fail the    * snapshot.    *    * @param snapshot    * @return the list of online regions. Empty list is returned if no regions are responsible for    *         the given snapshot.    * @throws IOException    */
specifier|private
name|List
argument_list|<
name|HRegion
argument_list|>
name|getRegionsToSnapshot
parameter_list|(
name|SnapshotDescription
name|snapshot
parameter_list|)
throws|throws
name|IOException
block|{
name|List
argument_list|<
name|HRegion
argument_list|>
name|onlineRegions
init|=
operator|(
name|List
argument_list|<
name|HRegion
argument_list|>
operator|)
name|rss
operator|.
name|getRegions
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
name|snapshot
operator|.
name|getTable
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|Iterator
argument_list|<
name|HRegion
argument_list|>
name|iterator
init|=
name|onlineRegions
operator|.
name|iterator
argument_list|()
decl_stmt|;
comment|// remove the non-default regions
while|while
condition|(
name|iterator
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|HRegion
name|r
init|=
name|iterator
operator|.
name|next
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|RegionReplicaUtil
operator|.
name|isDefaultReplica
argument_list|(
name|r
operator|.
name|getRegionInfo
argument_list|()
argument_list|)
condition|)
block|{
name|iterator
operator|.
name|remove
argument_list|()
expr_stmt|;
block|}
block|}
return|return
name|onlineRegions
return|;
block|}
comment|/**    * Build the actual snapshot runner that will do all the 'hard' work    */
specifier|public
class|class
name|SnapshotSubprocedureBuilder
implements|implements
name|SubprocedureFactory
block|{
annotation|@
name|Override
specifier|public
name|Subprocedure
name|buildSubprocedure
parameter_list|(
name|String
name|name
parameter_list|,
name|byte
index|[]
name|data
parameter_list|)
block|{
try|try
block|{
comment|// unwrap the snapshot information
name|SnapshotDescription
name|snapshot
init|=
name|SnapshotDescription
operator|.
name|parseFrom
argument_list|(
name|data
argument_list|)
decl_stmt|;
return|return
name|RegionServerSnapshotManager
operator|.
name|this
operator|.
name|buildSubprocedure
argument_list|(
name|snapshot
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Could not read snapshot information from request."
argument_list|)
throw|;
block|}
block|}
block|}
comment|/**    * We use the SnapshotSubprocedurePool, a class specific thread pool instead of    * {@link org.apache.hadoop.hbase.executor.ExecutorService}.    *    * It uses a {@link java.util.concurrent.ExecutorCompletionService} which provides queuing of    * completed tasks which lets us efficiently cancel pending tasks upon the earliest operation    * failures.    *    * HBase's ExecutorService (different from {@link java.util.concurrent.ExecutorService}) isn't    * really built for coordinated tasks where multiple threads as part of one larger task.  In    * RS's the HBase Executor services are only used for open and close and not other threadpooled    * operations such as compactions and replication  sinks.    */
specifier|static
class|class
name|SnapshotSubprocedurePool
block|{
specifier|private
specifier|final
name|Abortable
name|abortable
decl_stmt|;
specifier|private
specifier|final
name|ExecutorCompletionService
argument_list|<
name|Void
argument_list|>
name|taskPool
decl_stmt|;
specifier|private
specifier|final
name|ThreadPoolExecutor
name|executor
decl_stmt|;
specifier|private
specifier|volatile
name|boolean
name|stopped
decl_stmt|;
specifier|private
specifier|final
name|List
argument_list|<
name|Future
argument_list|<
name|Void
argument_list|>
argument_list|>
name|futures
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|String
name|name
decl_stmt|;
name|SnapshotSubprocedurePool
parameter_list|(
name|String
name|name
parameter_list|,
name|Configuration
name|conf
parameter_list|,
name|Abortable
name|abortable
parameter_list|)
block|{
name|this
operator|.
name|abortable
operator|=
name|abortable
expr_stmt|;
comment|// configure the executor service
name|long
name|keepAlive
init|=
name|conf
operator|.
name|getLong
argument_list|(
name|RegionServerSnapshotManager
operator|.
name|SNAPSHOT_TIMEOUT_MILLIS_KEY
argument_list|,
name|RegionServerSnapshotManager
operator|.
name|SNAPSHOT_TIMEOUT_MILLIS_DEFAULT
argument_list|)
decl_stmt|;
name|int
name|threads
init|=
name|conf
operator|.
name|getInt
argument_list|(
name|CONCURENT_SNAPSHOT_TASKS_KEY
argument_list|,
name|DEFAULT_CONCURRENT_SNAPSHOT_TASKS
argument_list|)
decl_stmt|;
name|this
operator|.
name|name
operator|=
name|name
expr_stmt|;
name|executor
operator|=
name|Threads
operator|.
name|getBoundedCachedThreadPool
argument_list|(
name|threads
argument_list|,
name|keepAlive
argument_list|,
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|,
literal|"rs("
operator|+
name|name
operator|+
literal|")-snapshot"
argument_list|)
expr_stmt|;
name|taskPool
operator|=
operator|new
name|ExecutorCompletionService
argument_list|<>
argument_list|(
name|executor
argument_list|)
expr_stmt|;
block|}
name|boolean
name|hasTasks
parameter_list|()
block|{
return|return
name|futures
operator|.
name|size
argument_list|()
operator|!=
literal|0
return|;
block|}
comment|/**      * Submit a task to the pool.      *      * NOTE: all must be submitted before you can safely {@link #waitForOutstandingTasks()}. This      * version does not support issuing tasks from multiple concurrent table snapshots requests.      */
name|void
name|submitTask
parameter_list|(
specifier|final
name|Callable
argument_list|<
name|Void
argument_list|>
name|task
parameter_list|)
block|{
name|Future
argument_list|<
name|Void
argument_list|>
name|f
init|=
name|this
operator|.
name|taskPool
operator|.
name|submit
argument_list|(
name|task
argument_list|)
decl_stmt|;
name|futures
operator|.
name|add
argument_list|(
name|f
argument_list|)
expr_stmt|;
block|}
comment|/**      * Wait for all of the currently outstanding tasks submitted via {@link #submitTask(Callable)}.      * This *must* be called after all tasks are submitted via submitTask.      *      * @return<tt>true</tt> on success,<tt>false</tt> otherwise      * @throws InterruptedException      * @throws SnapshotCreationException if the snapshot failed while we were waiting      */
name|boolean
name|waitForOutstandingTasks
parameter_list|()
throws|throws
name|ForeignException
throws|,
name|InterruptedException
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Waiting for local region snapshots to finish."
argument_list|)
expr_stmt|;
name|int
name|sz
init|=
name|futures
operator|.
name|size
argument_list|()
decl_stmt|;
try|try
block|{
comment|// Using the completion service to process the futures that finish first first.
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|sz
condition|;
name|i
operator|++
control|)
block|{
name|Future
argument_list|<
name|Void
argument_list|>
name|f
init|=
name|taskPool
operator|.
name|take
argument_list|()
decl_stmt|;
name|f
operator|.
name|get
argument_list|()
expr_stmt|;
if|if
condition|(
operator|!
name|futures
operator|.
name|remove
argument_list|(
name|f
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"unexpected future"
operator|+
name|f
argument_list|)
expr_stmt|;
block|}
name|LOG
operator|.
name|debug
argument_list|(
literal|"Completed "
operator|+
operator|(
name|i
operator|+
literal|1
operator|)
operator|+
literal|"/"
operator|+
name|sz
operator|+
literal|" local region snapshots."
argument_list|)
expr_stmt|;
block|}
name|LOG
operator|.
name|debug
argument_list|(
literal|"Completed "
operator|+
name|sz
operator|+
literal|" local region snapshots."
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
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
literal|"Got InterruptedException in SnapshotSubprocedurePool"
argument_list|,
name|e
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|stopped
condition|)
block|{
name|Thread
operator|.
name|currentThread
argument_list|()
operator|.
name|interrupt
argument_list|()
expr_stmt|;
throw|throw
operator|new
name|ForeignException
argument_list|(
literal|"SnapshotSubprocedurePool"
argument_list|,
name|e
argument_list|)
throw|;
block|}
comment|// we are stopped so we can just exit.
block|}
catch|catch
parameter_list|(
name|ExecutionException
name|e
parameter_list|)
block|{
name|Throwable
name|cause
init|=
name|e
operator|.
name|getCause
argument_list|()
decl_stmt|;
if|if
condition|(
name|cause
operator|instanceof
name|ForeignException
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Rethrowing ForeignException from SnapshotSubprocedurePool"
argument_list|,
name|e
argument_list|)
expr_stmt|;
throw|throw
operator|(
name|ForeignException
operator|)
name|e
operator|.
name|getCause
argument_list|()
throw|;
block|}
elseif|else
if|if
condition|(
name|cause
operator|instanceof
name|DroppedSnapshotException
condition|)
block|{
comment|// we have to abort the region server according to contract of flush
name|abortable
operator|.
name|abort
argument_list|(
literal|"Received DroppedSnapshotException, aborting"
argument_list|,
name|cause
argument_list|)
expr_stmt|;
block|}
name|LOG
operator|.
name|warn
argument_list|(
literal|"Got Exception in SnapshotSubprocedurePool"
argument_list|,
name|e
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|ForeignException
argument_list|(
name|name
argument_list|,
name|e
operator|.
name|getCause
argument_list|()
argument_list|)
throw|;
block|}
finally|finally
block|{
name|cancelTasks
argument_list|()
expr_stmt|;
block|}
return|return
literal|false
return|;
block|}
comment|/**      * This attempts to cancel out all pending and in progress tasks (interruptions issues)      * @throws InterruptedException      */
name|void
name|cancelTasks
parameter_list|()
throws|throws
name|InterruptedException
block|{
name|Collection
argument_list|<
name|Future
argument_list|<
name|Void
argument_list|>
argument_list|>
name|tasks
init|=
name|futures
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"cancelling "
operator|+
name|tasks
operator|.
name|size
argument_list|()
operator|+
literal|" tasks for snapshot "
operator|+
name|name
argument_list|)
expr_stmt|;
for|for
control|(
name|Future
argument_list|<
name|Void
argument_list|>
name|f
range|:
name|tasks
control|)
block|{
comment|// TODO Ideally we'd interrupt hbase threads when we cancel.  However it seems that there
comment|// are places in the HBase code where row/region locks are taken and not released in a
comment|// finally block.  Thus we cancel without interrupting.  Cancellations will be slower to
comment|// complete but we won't suffer from unreleased locks due to poor code discipline.
name|f
operator|.
name|cancel
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
comment|// evict remaining tasks and futures from taskPool.
name|futures
operator|.
name|clear
argument_list|()
expr_stmt|;
while|while
condition|(
name|taskPool
operator|.
name|poll
argument_list|()
operator|!=
literal|null
condition|)
block|{}
name|stop
argument_list|()
expr_stmt|;
block|}
comment|/**      * Abruptly shutdown the thread pool.  Call when exiting a region server.      */
name|void
name|stop
parameter_list|()
block|{
if|if
condition|(
name|this
operator|.
name|stopped
condition|)
return|return;
name|this
operator|.
name|stopped
operator|=
literal|true
expr_stmt|;
name|this
operator|.
name|executor
operator|.
name|shutdown
argument_list|()
expr_stmt|;
block|}
block|}
comment|/**    * Create a default snapshot handler - uses a zookeeper based member controller.    * @param rss region server running the handler    * @throws KeeperException if the zookeeper cluster cannot be reached    */
annotation|@
name|Override
specifier|public
name|void
name|initialize
parameter_list|(
name|RegionServerServices
name|rss
parameter_list|)
throws|throws
name|KeeperException
block|{
name|this
operator|.
name|rss
operator|=
name|rss
expr_stmt|;
name|ZKWatcher
name|zkw
init|=
name|rss
operator|.
name|getZooKeeper
argument_list|()
decl_stmt|;
name|this
operator|.
name|memberRpcs
operator|=
operator|new
name|ZKProcedureMemberRpcs
argument_list|(
name|zkw
argument_list|,
name|SnapshotManager
operator|.
name|ONLINE_SNAPSHOT_CONTROLLER_DESCRIPTION
argument_list|)
expr_stmt|;
comment|// read in the snapshot request configuration properties
name|Configuration
name|conf
init|=
name|rss
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
name|long
name|keepAlive
init|=
name|conf
operator|.
name|getLong
argument_list|(
name|SNAPSHOT_TIMEOUT_MILLIS_KEY
argument_list|,
name|SNAPSHOT_TIMEOUT_MILLIS_DEFAULT
argument_list|)
decl_stmt|;
name|int
name|opThreads
init|=
name|conf
operator|.
name|getInt
argument_list|(
name|SNAPSHOT_REQUEST_THREADS_KEY
argument_list|,
name|SNAPSHOT_REQUEST_THREADS_DEFAULT
argument_list|)
decl_stmt|;
comment|// create the actual snapshot procedure member
name|ThreadPoolExecutor
name|pool
init|=
name|ProcedureMember
operator|.
name|defaultPool
argument_list|(
name|rss
operator|.
name|getServerName
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|,
name|opThreads
argument_list|,
name|keepAlive
argument_list|)
decl_stmt|;
name|this
operator|.
name|member
operator|=
operator|new
name|ProcedureMember
argument_list|(
name|memberRpcs
argument_list|,
name|pool
argument_list|,
operator|new
name|SnapshotSubprocedureBuilder
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|getProcedureSignature
parameter_list|()
block|{
return|return
name|SnapshotManager
operator|.
name|ONLINE_SNAPSHOT_CONTROLLER_DESCRIPTION
return|;
block|}
block|}
end_class

end_unit

