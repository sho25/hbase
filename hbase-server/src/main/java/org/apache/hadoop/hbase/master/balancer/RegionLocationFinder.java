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
name|LinkedList
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
name|ClusterStatus
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
name|Bytes
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
name|cache
operator|.
name|CacheBuilder
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
name|cache
operator|.
name|CacheLoader
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
name|cache
operator|.
name|LoadingCache
import|;
end_import

begin_comment
comment|/**  * This will find where data for a region is located in HDFS. It ranks  * {@link ServerName}'s by the size of the store files they are holding for a  * given region.  *  */
end_comment

begin_class
class|class
name|RegionLocationFinder
block|{
specifier|private
specifier|static
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|RegionLocationFinder
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|Configuration
name|conf
decl_stmt|;
specifier|private
name|ClusterStatus
name|status
decl_stmt|;
specifier|private
name|MasterServices
name|services
decl_stmt|;
specifier|private
name|CacheLoader
argument_list|<
name|HRegionInfo
argument_list|,
name|List
argument_list|<
name|ServerName
argument_list|>
argument_list|>
name|loader
init|=
operator|new
name|CacheLoader
argument_list|<
name|HRegionInfo
argument_list|,
name|List
argument_list|<
name|ServerName
argument_list|>
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|ServerName
argument_list|>
name|load
parameter_list|(
name|HRegionInfo
name|key
parameter_list|)
throws|throws
name|Exception
block|{
name|List
argument_list|<
name|ServerName
argument_list|>
name|servers
init|=
name|internalGetTopBlockLocation
argument_list|(
name|key
argument_list|)
decl_stmt|;
if|if
condition|(
name|servers
operator|==
literal|null
condition|)
block|{
return|return
operator|new
name|LinkedList
argument_list|<
name|ServerName
argument_list|>
argument_list|()
return|;
block|}
return|return
name|servers
return|;
block|}
block|}
decl_stmt|;
comment|// The cache for where regions are located.
specifier|private
name|LoadingCache
argument_list|<
name|HRegionInfo
argument_list|,
name|List
argument_list|<
name|ServerName
argument_list|>
argument_list|>
name|cache
init|=
literal|null
decl_stmt|;
comment|/**    * Create a cache for region to list of servers    * @param mins Number of mins to cache    * @return A new Cache.    */
specifier|private
name|LoadingCache
argument_list|<
name|HRegionInfo
argument_list|,
name|List
argument_list|<
name|ServerName
argument_list|>
argument_list|>
name|createCache
parameter_list|(
name|int
name|mins
parameter_list|)
block|{
return|return
name|CacheBuilder
operator|.
name|newBuilder
argument_list|()
operator|.
name|expireAfterAccess
argument_list|(
name|mins
argument_list|,
name|TimeUnit
operator|.
name|MINUTES
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
name|cache
operator|=
name|createCache
argument_list|(
name|conf
operator|.
name|getInt
argument_list|(
literal|"hbase.master.balancer.regionLocationCacheTime"
argument_list|,
literal|30
argument_list|)
argument_list|)
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
name|setClusterStatus
parameter_list|(
name|ClusterStatus
name|status
parameter_list|)
block|{
name|this
operator|.
name|status
operator|=
name|status
expr_stmt|;
block|}
specifier|protected
name|List
argument_list|<
name|ServerName
argument_list|>
name|getTopBlockLocations
parameter_list|(
name|HRegionInfo
name|region
parameter_list|)
block|{
name|List
argument_list|<
name|ServerName
argument_list|>
name|servers
init|=
literal|null
decl_stmt|;
try|try
block|{
name|servers
operator|=
name|cache
operator|.
name|get
argument_list|(
name|region
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ExecutionException
name|ex
parameter_list|)
block|{
name|servers
operator|=
operator|new
name|LinkedList
argument_list|<
name|ServerName
argument_list|>
argument_list|()
expr_stmt|;
block|}
return|return
name|servers
return|;
block|}
comment|/**    * Returns an ordered list of hosts that are hosting the blocks for this    * region. The weight of each host is the sum of the block lengths of all    * files on that host, so the first host in the list is the server which holds    * the most bytes of the given region's HFiles.    *    * @param region region    * @return ordered list of hosts holding blocks of the specified region    */
specifier|protected
name|List
argument_list|<
name|ServerName
argument_list|>
name|internalGetTopBlockLocation
parameter_list|(
name|HRegionInfo
name|region
parameter_list|)
block|{
name|List
argument_list|<
name|ServerName
argument_list|>
name|topServerNames
init|=
literal|null
decl_stmt|;
try|try
block|{
name|HTableDescriptor
name|tableDescriptor
init|=
name|getTableDescriptor
argument_list|(
name|region
operator|.
name|getTableName
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
operator|.
name|getEncodedName
argument_list|()
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|topHosts
init|=
name|blocksDistribution
operator|.
name|getTopHosts
argument_list|()
decl_stmt|;
name|topServerNames
operator|=
name|mapHostNameToServerName
argument_list|(
name|topHosts
argument_list|)
expr_stmt|;
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
name|debug
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
name|topServerNames
return|;
block|}
comment|/**    * return HTableDescriptor for a given tableName    *    * @param tableName the table name    * @return HTableDescriptor    * @throws IOException    */
specifier|protected
name|HTableDescriptor
name|getTableDescriptor
parameter_list|(
name|byte
index|[]
name|tableName
parameter_list|)
throws|throws
name|IOException
block|{
name|HTableDescriptor
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
name|Bytes
operator|.
name|toString
argument_list|(
name|tableName
argument_list|)
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
literal|"FileNotFoundException during getTableDescriptors."
operator|+
literal|" Current table name = "
operator|+
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|tableName
argument_list|)
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
return|return
literal|null
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
argument_list|<
name|ServerName
argument_list|>
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
name|getServers
argument_list|()
decl_stmt|;
comment|// create a mapping from hostname to ServerName for fast lookup
name|HashMap
argument_list|<
name|String
argument_list|,
name|ServerName
argument_list|>
name|hostToServerName
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|ServerName
argument_list|>
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
name|hostToServerName
operator|.
name|put
argument_list|(
name|sn
operator|.
name|getHostname
argument_list|()
argument_list|,
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
name|ServerName
name|sn
init|=
name|hostToServerName
operator|.
name|get
argument_list|(
name|host
argument_list|)
decl_stmt|;
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
return|return
name|topServerNames
return|;
block|}
block|}
end_class

end_unit

