begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|master
package|;
end_package

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
name|Optional
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
name|ConcurrentNavigableMap
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
name|ThreadFactory
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
name|HRegionLocation
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
name|exceptions
operator|.
name|DeserializationException
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
name|types
operator|.
name|CopyOnWriteArrayMap
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
name|RetryCounter
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
name|RetryCounterFactory
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
name|ZKListener
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
name|ZKWatcher
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
name|ZNodePaths
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
name|ProtobufUtil
import|;
end_import

begin_comment
comment|/**  * A cache of meta region location metadata. Registers a listener on ZK to track changes to the  * meta table znodes. Clients are expected to retry if the meta information is stale. This class  * is thread-safe (a single instance of this class can be shared by multiple threads without race  * conditions).  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|MetaRegionLocationCache
extends|extends
name|ZKListener
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
name|MetaRegionLocationCache
operator|.
name|class
argument_list|)
decl_stmt|;
comment|/**    * Maximum number of times we retry when ZK operation times out.    */
specifier|private
specifier|static
specifier|final
name|int
name|MAX_ZK_META_FETCH_RETRIES
init|=
literal|10
decl_stmt|;
comment|/**    * Sleep interval ms between ZK operation retries.    */
specifier|private
specifier|static
specifier|final
name|int
name|SLEEP_INTERVAL_MS_BETWEEN_RETRIES
init|=
literal|1000
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|SLEEP_INTERVAL_MS_MAX
init|=
literal|10000
decl_stmt|;
specifier|private
specifier|final
name|RetryCounterFactory
name|retryCounterFactory
init|=
operator|new
name|RetryCounterFactory
argument_list|(
name|MAX_ZK_META_FETCH_RETRIES
argument_list|,
name|SLEEP_INTERVAL_MS_BETWEEN_RETRIES
argument_list|)
decl_stmt|;
comment|/**    * Cached meta region locations indexed by replica ID.    * CopyOnWriteArrayMap ensures synchronization during updates and a consistent snapshot during    * client requests. Even though CopyOnWriteArrayMap copies the data structure for every write,    * that should be OK since the size of the list is often small and mutations are not too often    * and we do not need to block client requests while mutations are in progress.    */
specifier|private
specifier|final
name|CopyOnWriteArrayMap
argument_list|<
name|Integer
argument_list|,
name|HRegionLocation
argument_list|>
name|cachedMetaLocations
decl_stmt|;
specifier|private
enum|enum
name|ZNodeOpType
block|{
name|INIT
block|,
name|CREATED
block|,
name|CHANGED
block|,
name|DELETED
block|}
specifier|public
name|MetaRegionLocationCache
parameter_list|(
name|ZKWatcher
name|zkWatcher
parameter_list|)
block|{
name|super
argument_list|(
name|zkWatcher
argument_list|)
expr_stmt|;
name|cachedMetaLocations
operator|=
operator|new
name|CopyOnWriteArrayMap
argument_list|<>
argument_list|()
expr_stmt|;
name|watcher
operator|.
name|registerListener
argument_list|(
name|this
argument_list|)
expr_stmt|;
comment|// Populate the initial snapshot of data from meta znodes.
comment|// This is needed because stand-by masters can potentially start after the initial znode
comment|// creation. It blocks forever until the initial meta locations are loaded from ZK and watchers
comment|// are established. Subsequent updates are handled by the registered listener. Also, this runs
comment|// in a separate thread in the background to not block master init.
name|ThreadFactory
name|threadFactory
init|=
operator|new
name|ThreadFactoryBuilder
argument_list|()
operator|.
name|setDaemon
argument_list|(
literal|true
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|RetryCounterFactory
name|retryFactory
init|=
operator|new
name|RetryCounterFactory
argument_list|(
name|Integer
operator|.
name|MAX_VALUE
argument_list|,
name|SLEEP_INTERVAL_MS_BETWEEN_RETRIES
argument_list|,
name|SLEEP_INTERVAL_MS_MAX
argument_list|)
decl_stmt|;
name|threadFactory
operator|.
name|newThread
argument_list|(
parameter_list|()
lambda|->
name|loadMetaLocationsFromZk
argument_list|(
name|retryFactory
operator|.
name|create
argument_list|()
argument_list|,
name|ZNodeOpType
operator|.
name|INIT
argument_list|)
argument_list|)
operator|.
name|start
argument_list|()
expr_stmt|;
block|}
comment|/**    * Populates the current snapshot of meta locations from ZK. If no meta znodes exist, it registers    * a watcher on base znode to check for any CREATE/DELETE events on the children.    * @param retryCounter controls the number of retries and sleep between retries.    */
specifier|private
name|void
name|loadMetaLocationsFromZk
parameter_list|(
name|RetryCounter
name|retryCounter
parameter_list|,
name|ZNodeOpType
name|opType
parameter_list|)
block|{
name|List
argument_list|<
name|String
argument_list|>
name|znodes
init|=
literal|null
decl_stmt|;
while|while
condition|(
name|retryCounter
operator|.
name|shouldRetry
argument_list|()
condition|)
block|{
try|try
block|{
name|znodes
operator|=
name|watcher
operator|.
name|getMetaReplicaNodesAndWatchChildren
argument_list|()
expr_stmt|;
break|break;
block|}
catch|catch
parameter_list|(
name|KeeperException
name|ke
parameter_list|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Error populating initial meta locations"
argument_list|,
name|ke
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|retryCounter
operator|.
name|shouldRetry
argument_list|()
condition|)
block|{
comment|// Retries exhausted and watchers not set. This is not a desirable state since the cache
comment|// could remain stale forever. Propagate the exception.
name|watcher
operator|.
name|abort
argument_list|(
literal|"Error populating meta locations"
argument_list|,
name|ke
argument_list|)
expr_stmt|;
return|return;
block|}
try|try
block|{
name|retryCounter
operator|.
name|sleepUntilNextRetry
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|ie
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Interrupted while loading meta locations from ZK"
argument_list|,
name|ie
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
return|return;
block|}
block|}
block|}
if|if
condition|(
name|znodes
operator|==
literal|null
operator|||
name|znodes
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
comment|// No meta znodes exist at this point but we registered a watcher on the base znode to listen
comment|// for updates. They will be handled via nodeChildrenChanged().
return|return;
block|}
if|if
condition|(
name|znodes
operator|.
name|size
argument_list|()
operator|==
name|cachedMetaLocations
operator|.
name|size
argument_list|()
condition|)
block|{
comment|// No new meta znodes got added.
return|return;
block|}
for|for
control|(
name|String
name|znode
range|:
name|znodes
control|)
block|{
name|String
name|path
init|=
name|ZNodePaths
operator|.
name|joinZNode
argument_list|(
name|watcher
operator|.
name|getZNodePaths
argument_list|()
operator|.
name|baseZNode
argument_list|,
name|znode
argument_list|)
decl_stmt|;
name|updateMetaLocation
argument_list|(
name|path
argument_list|,
name|opType
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Gets the HRegionLocation for a given meta replica ID. Renews the watch on the znode for    * future updates.    * @param replicaId ReplicaID of the region.    * @return HRegionLocation for the meta replica.    * @throws KeeperException if there is any issue fetching/parsing the serialized data.    */
specifier|private
name|HRegionLocation
name|getMetaRegionLocation
parameter_list|(
name|int
name|replicaId
parameter_list|)
throws|throws
name|KeeperException
block|{
name|RegionState
name|metaRegionState
decl_stmt|;
try|try
block|{
name|byte
index|[]
name|data
init|=
name|ZKUtil
operator|.
name|getDataAndWatch
argument_list|(
name|watcher
argument_list|,
name|watcher
operator|.
name|getZNodePaths
argument_list|()
operator|.
name|getZNodeForReplica
argument_list|(
name|replicaId
argument_list|)
argument_list|)
decl_stmt|;
name|metaRegionState
operator|=
name|ProtobufUtil
operator|.
name|parseMetaRegionStateFrom
argument_list|(
name|data
argument_list|,
name|replicaId
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|DeserializationException
name|e
parameter_list|)
block|{
throw|throw
name|ZKUtil
operator|.
name|convert
argument_list|(
name|e
argument_list|)
throw|;
block|}
return|return
operator|new
name|HRegionLocation
argument_list|(
name|metaRegionState
operator|.
name|getRegion
argument_list|()
argument_list|,
name|metaRegionState
operator|.
name|getServerName
argument_list|()
argument_list|)
return|;
block|}
specifier|private
name|void
name|updateMetaLocation
parameter_list|(
name|String
name|path
parameter_list|,
name|ZNodeOpType
name|opType
parameter_list|)
block|{
if|if
condition|(
operator|!
name|isValidMetaZNode
argument_list|(
name|path
argument_list|)
condition|)
block|{
return|return;
block|}
name|LOG
operator|.
name|debug
argument_list|(
literal|"Updating meta znode for path {}: {}"
argument_list|,
name|path
argument_list|,
name|opType
operator|.
name|name
argument_list|()
argument_list|)
expr_stmt|;
name|int
name|replicaId
init|=
name|watcher
operator|.
name|getZNodePaths
argument_list|()
operator|.
name|getMetaReplicaIdFromPath
argument_list|(
name|path
argument_list|)
decl_stmt|;
name|RetryCounter
name|retryCounter
init|=
name|retryCounterFactory
operator|.
name|create
argument_list|()
decl_stmt|;
name|HRegionLocation
name|location
init|=
literal|null
decl_stmt|;
while|while
condition|(
name|retryCounter
operator|.
name|shouldRetry
argument_list|()
condition|)
block|{
try|try
block|{
if|if
condition|(
name|opType
operator|==
name|ZNodeOpType
operator|.
name|DELETED
condition|)
block|{
if|if
condition|(
operator|!
name|ZKUtil
operator|.
name|watchAndCheckExists
argument_list|(
name|watcher
argument_list|,
name|path
argument_list|)
condition|)
block|{
comment|// The path does not exist, we've set the watcher and we can break for now.
break|break;
block|}
comment|// If it is a transient error and the node appears right away, we fetch the
comment|// latest meta state.
block|}
name|location
operator|=
name|getMetaRegionLocation
argument_list|(
name|replicaId
argument_list|)
expr_stmt|;
break|break;
block|}
catch|catch
parameter_list|(
name|KeeperException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Error getting meta location for path {}"
argument_list|,
name|path
argument_list|,
name|e
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|retryCounter
operator|.
name|shouldRetry
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Error getting meta location for path {}. Retries exhausted."
argument_list|,
name|path
argument_list|,
name|e
argument_list|)
expr_stmt|;
break|break;
block|}
try|try
block|{
name|retryCounter
operator|.
name|sleepUntilNextRetry
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|ie
parameter_list|)
block|{
name|Thread
operator|.
name|currentThread
argument_list|()
operator|.
name|interrupt
argument_list|()
expr_stmt|;
return|return;
block|}
block|}
block|}
if|if
condition|(
name|location
operator|==
literal|null
condition|)
block|{
name|cachedMetaLocations
operator|.
name|remove
argument_list|(
name|replicaId
argument_list|)
expr_stmt|;
return|return;
block|}
name|cachedMetaLocations
operator|.
name|put
argument_list|(
name|replicaId
argument_list|,
name|location
argument_list|)
expr_stmt|;
block|}
comment|/**    * @return Optional list of HRegionLocations for meta replica(s), null if the cache is empty.    *    */
specifier|public
name|Optional
argument_list|<
name|List
argument_list|<
name|HRegionLocation
argument_list|>
argument_list|>
name|getMetaRegionLocations
parameter_list|()
block|{
name|ConcurrentNavigableMap
argument_list|<
name|Integer
argument_list|,
name|HRegionLocation
argument_list|>
name|snapshot
init|=
name|cachedMetaLocations
operator|.
name|tailMap
argument_list|(
name|cachedMetaLocations
operator|.
name|firstKey
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|snapshot
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
comment|// This could be possible if the master has not successfully initialized yet or meta region
comment|// is stuck in some weird state.
return|return
name|Optional
operator|.
name|empty
argument_list|()
return|;
block|}
name|List
argument_list|<
name|HRegionLocation
argument_list|>
name|result
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
comment|// Explicitly iterate instead of new ArrayList<>(snapshot.values()) because the underlying
comment|// ArrayValueCollection does not implement toArray().
name|snapshot
operator|.
name|values
argument_list|()
operator|.
name|forEach
argument_list|(
name|location
lambda|->
name|result
operator|.
name|add
argument_list|(
name|location
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|Optional
operator|.
name|of
argument_list|(
name|result
argument_list|)
return|;
block|}
comment|/**    * Helper to check if the given 'path' corresponds to a meta znode. This listener is only    * interested in changes to meta znodes.    */
specifier|private
name|boolean
name|isValidMetaZNode
parameter_list|(
name|String
name|path
parameter_list|)
block|{
return|return
name|watcher
operator|.
name|getZNodePaths
argument_list|()
operator|.
name|isAnyMetaReplicaZNode
argument_list|(
name|path
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|nodeCreated
parameter_list|(
name|String
name|path
parameter_list|)
block|{
name|updateMetaLocation
argument_list|(
name|path
argument_list|,
name|ZNodeOpType
operator|.
name|CREATED
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|nodeDeleted
parameter_list|(
name|String
name|path
parameter_list|)
block|{
name|updateMetaLocation
argument_list|(
name|path
argument_list|,
name|ZNodeOpType
operator|.
name|DELETED
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|nodeDataChanged
parameter_list|(
name|String
name|path
parameter_list|)
block|{
name|updateMetaLocation
argument_list|(
name|path
argument_list|,
name|ZNodeOpType
operator|.
name|CHANGED
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|nodeChildrenChanged
parameter_list|(
name|String
name|path
parameter_list|)
block|{
if|if
condition|(
operator|!
name|path
operator|.
name|equals
argument_list|(
name|watcher
operator|.
name|getZNodePaths
argument_list|()
operator|.
name|baseZNode
argument_list|)
condition|)
block|{
return|return;
block|}
name|loadMetaLocationsFromZk
argument_list|(
name|retryCounterFactory
operator|.
name|create
argument_list|()
argument_list|,
name|ZNodeOpType
operator|.
name|CHANGED
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

