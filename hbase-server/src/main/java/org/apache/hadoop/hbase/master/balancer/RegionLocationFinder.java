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
name|master
operator|.
name|balancer
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|FileNotFoundException
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
name|Collection
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
name|ClusterMetrics
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
name|HDFSBlocksDistribution
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
name|RegionInfo
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
name|master
operator|.
name|MasterServices
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
name|assignment
operator|.
name|AssignmentManager
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
name|util
operator|.
name|EnvironmentEdgeManager
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
name|cache
operator|.
name|CacheBuilder
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
name|cache
operator|.
name|CacheLoader
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
name|cache
operator|.
name|LoadingCache
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
name|collect
operator|.
name|Lists
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
name|Futures
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
name|ListenableFuture
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
name|ListeningExecutorService
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
name|MoreExecutors
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

begin_comment
comment|/**  * This will find where data for a region is located in HDFS. It ranks  * {@link ServerName}'s by the size of the store files they are holding for a  * given region.  *  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
class|class
name|RegionLocationFinder
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
name|RegionLocationFinder
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|long
name|CACHE_TIME
init|=
literal|240
operator|*
literal|60
operator|*
literal|1000
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|HDFSBlocksDistribution
name|EMPTY_BLOCK_DISTRIBUTION
init|=
operator|new
name|HDFSBlocksDistribution
argument_list|()
decl_stmt|;
specifier|private
name|Configuration
name|conf
decl_stmt|;
specifier|private
specifier|volatile
name|ClusterMetrics
name|status
decl_stmt|;
specifier|private
name|MasterServices
name|services
decl_stmt|;
specifier|private
specifier|final
name|ListeningExecutorService
name|executor
decl_stmt|;
comment|// Do not scheduleFullRefresh at master startup
specifier|private
name|long
name|lastFullRefresh
init|=
name|EnvironmentEdgeManager
operator|.
name|currentTime
argument_list|()
decl_stmt|;
specifier|private
name|CacheLoader
argument_list|<
name|RegionInfo
argument_list|,
name|HDFSBlocksDistribution
argument_list|>
name|loader
init|=
operator|new
name|CacheLoader
argument_list|<
name|RegionInfo
argument_list|,
name|HDFSBlocksDistribution
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|ListenableFuture
argument_list|<
name|HDFSBlocksDistribution
argument_list|>
name|reload
parameter_list|(
specifier|final
name|RegionInfo
name|hri
parameter_list|,
name|HDFSBlocksDistribution
name|oldValue
parameter_list|)
throws|throws
name|Exception
block|{
return|return
name|executor
operator|.
name|submit
argument_list|(
operator|new
name|Callable
argument_list|<
name|HDFSBlocksDistribution
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|HDFSBlocksDistribution
name|call
parameter_list|()
throws|throws
name|Exception
block|{
return|return
name|internalGetTopBlockLocation
argument_list|(
name|hri
argument_list|)
return|;
block|}
block|}
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|HDFSBlocksDistribution
name|load
parameter_list|(
name|RegionInfo
name|key
parameter_list|)
throws|throws
name|Exception
block|{
return|return
name|internalGetTopBlockLocation
argument_list|(
name|key
argument_list|)
return|;
block|}
block|}
decl_stmt|;
comment|// The cache for where regions are located.
specifier|private
name|LoadingCache
argument_list|<
name|RegionInfo
argument_list|,
name|HDFSBlocksDistribution
argument_list|>
name|cache
init|=
literal|null
decl_stmt|;
name|RegionLocationFinder
parameter_list|()
block|{
name|this
operator|.
name|cache
operator|=
name|createCache
argument_list|()
expr_stmt|;
name|executor
operator|=
name|MoreExecutors
operator|.
name|listeningDecorator
argument_list|(
name|Executors
operator|.
name|newScheduledThreadPool
argument_list|(
literal|5
argument_list|,
operator|new
name|ThreadFactoryBuilder
argument_list|()
operator|.
name|setDaemon
argument_list|(
literal|true
argument_list|)
operator|.
name|setNameFormat
argument_list|(
literal|"region-location-%d"
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * Create a cache for region to list of servers    * @return A new Cache.    */
specifier|private
name|LoadingCache
argument_list|<
name|RegionInfo
argument_list|,
name|HDFSBlocksDistribution
argument_list|>
name|createCache
parameter_list|()
block|{
return|return
name|CacheBuilder
operator|.
name|newBuilder
argument_list|()
operator|.
name|expireAfterWrite
argument_list|(
name|CACHE_TIME
argument_list|,
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
operator|.
name|build
argument_list|(
name|loader
argument_list|)
return|;
block|}
specifier|public
name|Configuration
name|getConf
parameter_list|()
block|{
return|return
name|conf
return|;
block|}
specifier|public
name|void
name|setConf
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
block|}
specifier|public
name|void
name|setServices
parameter_list|(
name|MasterServices
name|services
parameter_list|)
block|{
name|this
operator|.
name|services
operator|=
name|services
expr_stmt|;
block|}
specifier|public
name|void
name|setClusterMetrics
parameter_list|(
name|ClusterMetrics
name|status
parameter_list|)
block|{
name|long
name|currentTime
init|=
name|EnvironmentEdgeManager
operator|.
name|currentTime
argument_list|()
decl_stmt|;
name|this
operator|.
name|status
operator|=
name|status
expr_stmt|;
if|if
condition|(
name|currentTime
operator|>
name|lastFullRefresh
operator|+
operator|(
name|CACHE_TIME
operator|/
literal|2
operator|)
condition|)
block|{
comment|// Only count the refresh if it includes user tables ( eg more than meta and namespace ).
name|lastFullRefresh
operator|=
name|scheduleFullRefresh
argument_list|()
condition|?
name|currentTime
else|:
name|lastFullRefresh
expr_stmt|;
block|}
block|}
comment|/**    * Refresh all the region locations.    *    * @return true if user created regions got refreshed.    */
specifier|private
name|boolean
name|scheduleFullRefresh
parameter_list|()
block|{
comment|// Protect from anything being null while starting up.
if|if
condition|(
name|services
operator|==
literal|null
condition|)
block|{
return|return
literal|false
return|;
block|}
specifier|final
name|AssignmentManager
name|am
init|=
name|services
operator|.
name|getAssignmentManager
argument_list|()
decl_stmt|;
if|if
condition|(
name|am
operator|==
literal|null
condition|)
block|{
return|return
literal|false
return|;
block|}
comment|// TODO: Should this refresh all the regions or only the ones assigned?
name|boolean
name|includesUserTables
init|=
literal|false
decl_stmt|;
for|for
control|(
specifier|final
name|RegionInfo
name|hri
range|:
name|am
operator|.
name|getAssignedRegions
argument_list|()
control|)
block|{
name|cache
operator|.
name|refresh
argument_list|(
name|hri
argument_list|)
expr_stmt|;
name|includesUserTables
operator|=
name|includesUserTables
operator|||
operator|!
name|hri
operator|.
name|getTable
argument_list|()
operator|.
name|isSystemTable
argument_list|()
expr_stmt|;
block|}
return|return
name|includesUserTables
return|;
block|}
specifier|protected
name|List
argument_list|<
name|ServerName
argument_list|>
name|getTopBlockLocations
parameter_list|(
name|RegionInfo
name|region
parameter_list|)
block|{
name|List
argument_list|<
name|String
argument_list|>
name|topHosts
init|=
name|getBlockDistribution
argument_list|(
name|region
argument_list|)
operator|.
name|getTopHosts
argument_list|()
decl_stmt|;
return|return
name|mapHostNameToServerName
argument_list|(
name|topHosts
argument_list|)
return|;
block|}
comment|/**    * Returns an ordered list of hosts which have better locality for this region    * than the current host.    */
specifier|protected
name|List
argument_list|<
name|ServerName
argument_list|>
name|getTopBlockLocations
parameter_list|(
name|RegionInfo
name|region
parameter_list|,
name|String
name|currentHost
parameter_list|)
block|{
name|HDFSBlocksDistribution
name|blocksDistribution
init|=
name|getBlockDistribution
argument_list|(
name|region
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|topHosts
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|String
name|host
range|:
name|blocksDistribution
operator|.
name|getTopHosts
argument_list|()
control|)
block|{
if|if
condition|(
name|host
operator|.
name|equals
argument_list|(
name|currentHost
argument_list|)
condition|)
block|{
break|break;
block|}
name|topHosts
operator|.
name|add
argument_list|(
name|host
argument_list|)
expr_stmt|;
block|}
return|return
name|mapHostNameToServerName
argument_list|(
name|topHosts
argument_list|)
return|;
block|}
comment|/**    * Returns an ordered list of hosts that are hosting the blocks for this    * region. The weight of each host is the sum of the block lengths of all    * files on that host, so the first host in the list is the server which holds    * the most bytes of the given region's HFiles.    *    * @param region region    * @return ordered list of hosts holding blocks of the specified region    */
specifier|protected
name|HDFSBlocksDistribution
name|internalGetTopBlockLocation
parameter_list|(
name|RegionInfo
name|region
parameter_list|)
block|{
try|try
block|{
name|TableDescriptor
name|tableDescriptor
init|=
name|getDescriptor
argument_list|(
name|region
operator|.
name|getTable
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|tableDescriptor
operator|!=
literal|null
condition|)
block|{
name|HDFSBlocksDistribution
name|blocksDistribution
init|=
name|HRegion
operator|.
name|computeHDFSBlocksDistribution
argument_list|(
name|getConf
argument_list|()
argument_list|,
name|tableDescriptor
argument_list|,
name|region
argument_list|)
decl_stmt|;
return|return
name|blocksDistribution
return|;
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|ioe
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"IOException during HDFSBlocksDistribution computation. for "
operator|+
literal|"region = "
operator|+
name|region
operator|.
name|getEncodedName
argument_list|()
argument_list|,
name|ioe
argument_list|)
expr_stmt|;
block|}
return|return
name|EMPTY_BLOCK_DISTRIBUTION
return|;
block|}
comment|/**    * return TableDescriptor for a given tableName    *    * @param tableName the table name    * @return TableDescriptor    * @throws IOException    */
specifier|protected
name|TableDescriptor
name|getDescriptor
parameter_list|(
name|TableName
name|tableName
parameter_list|)
throws|throws
name|IOException
block|{
name|TableDescriptor
name|tableDescriptor
init|=
literal|null
decl_stmt|;
try|try
block|{
if|if
condition|(
name|this
operator|.
name|services
operator|!=
literal|null
operator|&&
name|this
operator|.
name|services
operator|.
name|getTableDescriptors
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|tableDescriptor
operator|=
name|this
operator|.
name|services
operator|.
name|getTableDescriptors
argument_list|()
operator|.
name|get
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|FileNotFoundException
name|fnfe
parameter_list|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"tableName={}"
argument_list|,
name|tableName
argument_list|,
name|fnfe
argument_list|)
expr_stmt|;
block|}
return|return
name|tableDescriptor
return|;
block|}
comment|/**    * Map hostname to ServerName, The output ServerName list will have the same    * order as input hosts.    *    * @param hosts the list of hosts    * @return ServerName list    */
specifier|protected
name|List
argument_list|<
name|ServerName
argument_list|>
name|mapHostNameToServerName
parameter_list|(
name|List
argument_list|<
name|String
argument_list|>
name|hosts
parameter_list|)
block|{
if|if
condition|(
name|hosts
operator|==
literal|null
operator|||
name|status
operator|==
literal|null
condition|)
block|{
if|if
condition|(
name|hosts
operator|==
literal|null
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"RegionLocationFinder top hosts is null"
argument_list|)
expr_stmt|;
block|}
return|return
name|Lists
operator|.
name|newArrayList
argument_list|()
return|;
block|}
name|List
argument_list|<
name|ServerName
argument_list|>
name|topServerNames
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|Collection
argument_list|<
name|ServerName
argument_list|>
name|regionServers
init|=
name|status
operator|.
name|getLiveServerMetrics
argument_list|()
operator|.
name|keySet
argument_list|()
decl_stmt|;
comment|// create a mapping from hostname to ServerName for fast lookup
name|HashMap
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|ServerName
argument_list|>
argument_list|>
name|hostToServerName
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|ServerName
name|sn
range|:
name|regionServers
control|)
block|{
name|String
name|host
init|=
name|sn
operator|.
name|getHostname
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|hostToServerName
operator|.
name|containsKey
argument_list|(
name|host
argument_list|)
condition|)
block|{
name|hostToServerName
operator|.
name|put
argument_list|(
name|host
argument_list|,
operator|new
name|ArrayList
argument_list|<>
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|hostToServerName
operator|.
name|get
argument_list|(
name|host
argument_list|)
operator|.
name|add
argument_list|(
name|sn
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|String
name|host
range|:
name|hosts
control|)
block|{
if|if
condition|(
operator|!
name|hostToServerName
operator|.
name|containsKey
argument_list|(
name|host
argument_list|)
condition|)
block|{
continue|continue;
block|}
for|for
control|(
name|ServerName
name|sn
range|:
name|hostToServerName
operator|.
name|get
argument_list|(
name|host
argument_list|)
control|)
block|{
comment|// it is possible that HDFS is up ( thus host is valid ),
comment|// but RS is down ( thus sn is null )
if|if
condition|(
name|sn
operator|!=
literal|null
condition|)
block|{
name|topServerNames
operator|.
name|add
argument_list|(
name|sn
argument_list|)
expr_stmt|;
block|}
block|}
block|}
return|return
name|topServerNames
return|;
block|}
specifier|public
name|HDFSBlocksDistribution
name|getBlockDistribution
parameter_list|(
name|RegionInfo
name|hri
parameter_list|)
block|{
name|HDFSBlocksDistribution
name|blockDistbn
init|=
literal|null
decl_stmt|;
try|try
block|{
if|if
condition|(
name|cache
operator|.
name|asMap
argument_list|()
operator|.
name|containsKey
argument_list|(
name|hri
argument_list|)
condition|)
block|{
name|blockDistbn
operator|=
name|cache
operator|.
name|get
argument_list|(
name|hri
argument_list|)
expr_stmt|;
return|return
name|blockDistbn
return|;
block|}
else|else
block|{
name|LOG
operator|.
name|trace
argument_list|(
literal|"HDFSBlocksDistribution not found in cache for {}"
argument_list|,
name|hri
operator|.
name|getRegionNameAsString
argument_list|()
argument_list|)
expr_stmt|;
name|blockDistbn
operator|=
name|internalGetTopBlockLocation
argument_list|(
name|hri
argument_list|)
expr_stmt|;
name|cache
operator|.
name|put
argument_list|(
name|hri
argument_list|,
name|blockDistbn
argument_list|)
expr_stmt|;
return|return
name|blockDistbn
return|;
block|}
block|}
catch|catch
parameter_list|(
name|ExecutionException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Error while fetching cache entry "
argument_list|,
name|e
argument_list|)
expr_stmt|;
name|blockDistbn
operator|=
name|internalGetTopBlockLocation
argument_list|(
name|hri
argument_list|)
expr_stmt|;
name|cache
operator|.
name|put
argument_list|(
name|hri
argument_list|,
name|blockDistbn
argument_list|)
expr_stmt|;
return|return
name|blockDistbn
return|;
block|}
block|}
specifier|private
name|ListenableFuture
argument_list|<
name|HDFSBlocksDistribution
argument_list|>
name|asyncGetBlockDistribution
parameter_list|(
name|RegionInfo
name|hri
parameter_list|)
block|{
try|try
block|{
return|return
name|loader
operator|.
name|reload
argument_list|(
name|hri
argument_list|,
name|EMPTY_BLOCK_DISTRIBUTION
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
return|return
name|Futures
operator|.
name|immediateFuture
argument_list|(
name|EMPTY_BLOCK_DISTRIBUTION
argument_list|)
return|;
block|}
block|}
specifier|public
name|void
name|refreshAndWait
parameter_list|(
name|Collection
argument_list|<
name|RegionInfo
argument_list|>
name|hris
parameter_list|)
block|{
name|ArrayList
argument_list|<
name|ListenableFuture
argument_list|<
name|HDFSBlocksDistribution
argument_list|>
argument_list|>
name|regionLocationFutures
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|hris
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|RegionInfo
name|hregionInfo
range|:
name|hris
control|)
block|{
name|regionLocationFutures
operator|.
name|add
argument_list|(
name|asyncGetBlockDistribution
argument_list|(
name|hregionInfo
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|int
name|index
init|=
literal|0
decl_stmt|;
for|for
control|(
name|RegionInfo
name|hregionInfo
range|:
name|hris
control|)
block|{
name|ListenableFuture
argument_list|<
name|HDFSBlocksDistribution
argument_list|>
name|future
init|=
name|regionLocationFutures
operator|.
name|get
argument_list|(
name|index
argument_list|)
decl_stmt|;
try|try
block|{
name|cache
operator|.
name|put
argument_list|(
name|hregionInfo
argument_list|,
name|future
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|ite
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
block|}
catch|catch
parameter_list|(
name|ExecutionException
name|ee
parameter_list|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"ExecutionException during HDFSBlocksDistribution computation. for region = "
operator|+
name|hregionInfo
operator|.
name|getEncodedName
argument_list|()
argument_list|,
name|ee
argument_list|)
expr_stmt|;
block|}
name|index
operator|++
expr_stmt|;
block|}
block|}
comment|// For test
name|LoadingCache
argument_list|<
name|RegionInfo
argument_list|,
name|HDFSBlocksDistribution
argument_list|>
name|getCache
parameter_list|()
block|{
return|return
name|cache
return|;
block|}
block|}
end_class

end_unit

