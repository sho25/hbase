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
name|client
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
name|client
operator|.
name|AsyncRegionLocatorHelper
operator|.
name|canUpdateOnError
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
name|client
operator|.
name|AsyncRegionLocatorHelper
operator|.
name|createRegionLocations
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
name|client
operator|.
name|AsyncRegionLocatorHelper
operator|.
name|isGood
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
name|client
operator|.
name|AsyncRegionLocatorHelper
operator|.
name|removeRegionLocation
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
name|client
operator|.
name|AsyncRegionLocatorHelper
operator|.
name|replaceRegionLocation
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
name|CompletableFuture
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
name|AtomicReference
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
name|RegionLocations
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
comment|/**  * The asynchronous locator for meta region.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
class|class
name|AsyncMetaRegionLocator
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
name|AsyncMetaRegionLocator
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|AsyncRegistry
name|registry
decl_stmt|;
specifier|private
specifier|final
name|AtomicReference
argument_list|<
name|RegionLocations
argument_list|>
name|metaRegionLocations
init|=
operator|new
name|AtomicReference
argument_list|<>
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|AtomicReference
argument_list|<
name|CompletableFuture
argument_list|<
name|RegionLocations
argument_list|>
argument_list|>
name|metaRelocateFuture
init|=
operator|new
name|AtomicReference
argument_list|<>
argument_list|()
decl_stmt|;
name|AsyncMetaRegionLocator
parameter_list|(
name|AsyncRegistry
name|registry
parameter_list|)
block|{
name|this
operator|.
name|registry
operator|=
name|registry
expr_stmt|;
block|}
comment|/**    * Get the region locations for meta region. If the location for the given replica is not    * available in the cached locations, then fetch from the HBase cluster.    *<p/>    * The<code>replicaId</code> parameter is important. If the region replication config for meta    * region is changed, then the cached region locations may not have the locations for new    * replicas. If we do not check the location for the given replica, we will always return the    * cached region locations and cause an infinite loop.    */
name|CompletableFuture
argument_list|<
name|RegionLocations
argument_list|>
name|getRegionLocations
parameter_list|(
name|int
name|replicaId
parameter_list|,
name|boolean
name|reload
parameter_list|)
block|{
for|for
control|(
init|;
condition|;
control|)
block|{
if|if
condition|(
operator|!
name|reload
condition|)
block|{
name|RegionLocations
name|locs
init|=
name|this
operator|.
name|metaRegionLocations
operator|.
name|get
argument_list|()
decl_stmt|;
if|if
condition|(
name|isGood
argument_list|(
name|locs
argument_list|,
name|replicaId
argument_list|)
condition|)
block|{
return|return
name|CompletableFuture
operator|.
name|completedFuture
argument_list|(
name|locs
argument_list|)
return|;
block|}
block|}
name|LOG
operator|.
name|trace
argument_list|(
literal|"Meta region location cache is null, try fetching from registry."
argument_list|)
expr_stmt|;
if|if
condition|(
name|metaRelocateFuture
operator|.
name|compareAndSet
argument_list|(
literal|null
argument_list|,
operator|new
name|CompletableFuture
argument_list|<>
argument_list|()
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Start fetching meta region location from registry."
argument_list|)
expr_stmt|;
name|CompletableFuture
argument_list|<
name|RegionLocations
argument_list|>
name|future
init|=
name|metaRelocateFuture
operator|.
name|get
argument_list|()
decl_stmt|;
name|registry
operator|.
name|getMetaRegionLocation
argument_list|()
operator|.
name|whenComplete
argument_list|(
parameter_list|(
name|locs
parameter_list|,
name|error
parameter_list|)
lambda|->
block|{
if|if
condition|(
name|error
operator|!=
literal|null
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Failed to fetch meta region location from registry"
argument_list|,
name|error
argument_list|)
expr_stmt|;
name|metaRelocateFuture
operator|.
name|getAndSet
argument_list|(
literal|null
argument_list|)
operator|.
name|completeExceptionally
argument_list|(
name|error
argument_list|)
expr_stmt|;
return|return;
block|}
name|LOG
operator|.
name|debug
argument_list|(
literal|"The fetched meta region location is {}"
operator|+
name|locs
argument_list|)
expr_stmt|;
comment|// Here we update cache before reset future, so it is possible that someone can get a
comment|// stale value. Consider this:
comment|// 1. update cache
comment|// 2. someone clear the cache and relocate again
comment|// 3. the metaRelocateFuture is not null so the old future is used.
comment|// 4. we clear metaRelocateFuture and complete the future in it with the value being
comment|// cleared in step 2.
comment|// But we do not think it is a big deal as it rarely happens, and even if it happens, the
comment|// caller will retry again later, no correctness problems.
name|this
operator|.
name|metaRegionLocations
operator|.
name|set
argument_list|(
name|locs
argument_list|)
expr_stmt|;
name|metaRelocateFuture
operator|.
name|set
argument_list|(
literal|null
argument_list|)
expr_stmt|;
name|future
operator|.
name|complete
argument_list|(
name|locs
argument_list|)
expr_stmt|;
block|}
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|CompletableFuture
argument_list|<
name|RegionLocations
argument_list|>
name|future
init|=
name|metaRelocateFuture
operator|.
name|get
argument_list|()
decl_stmt|;
if|if
condition|(
name|future
operator|!=
literal|null
condition|)
block|{
return|return
name|future
return|;
block|}
block|}
block|}
block|}
specifier|private
name|HRegionLocation
name|getCacheLocation
parameter_list|(
name|HRegionLocation
name|loc
parameter_list|)
block|{
name|RegionLocations
name|locs
init|=
name|metaRegionLocations
operator|.
name|get
argument_list|()
decl_stmt|;
return|return
name|locs
operator|!=
literal|null
condition|?
name|locs
operator|.
name|getRegionLocation
argument_list|(
name|loc
operator|.
name|getRegion
argument_list|()
operator|.
name|getReplicaId
argument_list|()
argument_list|)
else|:
literal|null
return|;
block|}
specifier|private
name|void
name|addLocationToCache
parameter_list|(
name|HRegionLocation
name|loc
parameter_list|)
block|{
for|for
control|(
init|;
condition|;
control|)
block|{
name|int
name|replicaId
init|=
name|loc
operator|.
name|getRegion
argument_list|()
operator|.
name|getReplicaId
argument_list|()
decl_stmt|;
name|RegionLocations
name|oldLocs
init|=
name|metaRegionLocations
operator|.
name|get
argument_list|()
decl_stmt|;
if|if
condition|(
name|oldLocs
operator|==
literal|null
condition|)
block|{
name|RegionLocations
name|newLocs
init|=
name|createRegionLocations
argument_list|(
name|loc
argument_list|)
decl_stmt|;
if|if
condition|(
name|metaRegionLocations
operator|.
name|compareAndSet
argument_list|(
literal|null
argument_list|,
name|newLocs
argument_list|)
condition|)
block|{
return|return;
block|}
block|}
name|HRegionLocation
name|oldLoc
init|=
name|oldLocs
operator|.
name|getRegionLocation
argument_list|(
name|replicaId
argument_list|)
decl_stmt|;
if|if
condition|(
name|oldLoc
operator|!=
literal|null
operator|&&
operator|(
name|oldLoc
operator|.
name|getSeqNum
argument_list|()
operator|>
name|loc
operator|.
name|getSeqNum
argument_list|()
operator|||
name|oldLoc
operator|.
name|getServerName
argument_list|()
operator|.
name|equals
argument_list|(
name|loc
operator|.
name|getServerName
argument_list|()
argument_list|)
operator|)
condition|)
block|{
return|return;
block|}
name|RegionLocations
name|newLocs
init|=
name|replaceRegionLocation
argument_list|(
name|oldLocs
argument_list|,
name|loc
argument_list|)
decl_stmt|;
if|if
condition|(
name|metaRegionLocations
operator|.
name|compareAndSet
argument_list|(
name|oldLocs
argument_list|,
name|newLocs
argument_list|)
condition|)
block|{
return|return;
block|}
block|}
block|}
specifier|private
name|void
name|removeLocationFromCache
parameter_list|(
name|HRegionLocation
name|loc
parameter_list|)
block|{
for|for
control|(
init|;
condition|;
control|)
block|{
name|RegionLocations
name|oldLocs
init|=
name|metaRegionLocations
operator|.
name|get
argument_list|()
decl_stmt|;
if|if
condition|(
name|oldLocs
operator|==
literal|null
condition|)
block|{
return|return;
block|}
name|HRegionLocation
name|oldLoc
init|=
name|oldLocs
operator|.
name|getRegionLocation
argument_list|(
name|loc
operator|.
name|getRegion
argument_list|()
operator|.
name|getReplicaId
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|canUpdateOnError
argument_list|(
name|loc
argument_list|,
name|oldLoc
argument_list|)
condition|)
block|{
return|return;
block|}
name|RegionLocations
name|newLocs
init|=
name|removeRegionLocation
argument_list|(
name|oldLocs
argument_list|,
name|loc
operator|.
name|getRegion
argument_list|()
operator|.
name|getReplicaId
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|metaRegionLocations
operator|.
name|compareAndSet
argument_list|(
name|oldLocs
argument_list|,
name|newLocs
argument_list|)
condition|)
block|{
return|return;
block|}
block|}
block|}
name|void
name|updateCachedLocationOnError
parameter_list|(
name|HRegionLocation
name|loc
parameter_list|,
name|Throwable
name|exception
parameter_list|)
block|{
name|AsyncRegionLocatorHelper
operator|.
name|updateCachedLocationOnError
argument_list|(
name|loc
argument_list|,
name|exception
argument_list|,
name|this
operator|::
name|getCacheLocation
argument_list|,
name|this
operator|::
name|addLocationToCache
argument_list|,
name|this
operator|::
name|removeLocationFromCache
argument_list|)
expr_stmt|;
block|}
name|void
name|clearCache
parameter_list|()
block|{
name|metaRegionLocations
operator|.
name|set
argument_list|(
literal|null
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

