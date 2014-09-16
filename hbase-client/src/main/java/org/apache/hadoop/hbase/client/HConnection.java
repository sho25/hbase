begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|ExecutorService
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
name|classification
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
name|MasterNotRunningException
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
name|ZooKeeperConnectionException
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
name|coprocessor
operator|.
name|Batch
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
name|AdminService
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
name|ClientProtos
operator|.
name|ClientService
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
name|MasterProtos
operator|.
name|MasterService
import|;
end_import

begin_comment
comment|/**  * A cluster connection.  Knows how to find the master, locate regions out on the cluster,  * keeps a cache of locations and then knows how to re-calibrate after they move.  You need one  * of these to talk to your HBase cluster. {@link HConnectionManager} manages instances of this  * class.  See it for how to get one of these.  *  *<p>This is NOT a connection to a particular server but to ALL servers in the cluster.  Individual  * connections are managed at a lower level.  *  *<p>HConnections are used by {@link HTable} mostly but also by  * {@link HBaseAdmin}, and {@link org.apache.hadoop.hbase.zookeeper.MetaTableLocator}.  * HConnection instances can be shared.  Sharing  * is usually what you want because rather than each HConnection instance  * having to do its own discovery of regions out on the cluster, instead, all  * clients get to share the one cache of locations.  {@link HConnectionManager} does the  * sharing for you if you go by it getting connections.  Sharing makes cleanup of  * HConnections awkward.  See {@link HConnectionManager} for cleanup discussion.  *  * @see HConnectionManager  * @deprecated in favor of {@link Connection} and {@link ConnectionFactory}  */
end_comment

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|Public
annotation|@
name|InterfaceStability
operator|.
name|Stable
annotation|@
name|Deprecated
specifier|public
interface|interface
name|HConnection
extends|extends
name|Connection
block|{
comment|/**    * Key for configuration in Configuration whose value is the class we implement making a    * new HConnection instance.    */
specifier|public
specifier|static
specifier|final
name|String
name|HBASE_CLIENT_CONNECTION_IMPL
init|=
literal|"hbase.client.connection.impl"
decl_stmt|;
comment|/**    * @return Configuration instance being used by this HConnection instance.    */
annotation|@
name|Override
name|Configuration
name|getConfiguration
parameter_list|()
function_decl|;
comment|/**    * Retrieve an HTableInterface implementation for access to a table.    * The returned HTableInterface is not thread safe, a new instance should    * be created for each using thread.    * This is a lightweight operation, pooling or caching of the returned HTableInterface    * is neither required nor desired.    * Note that the HConnection needs to be unmanaged    * (created with {@link HConnectionManager#createConnection(Configuration)}).    * @param tableName    * @return an HTable to use for interactions with this table    */
specifier|public
name|HTableInterface
name|getTable
parameter_list|(
name|String
name|tableName
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Retrieve an HTableInterface implementation for access to a table.    * The returned HTableInterface is not thread safe, a new instance should    * be created for each using thread.    * This is a lightweight operation, pooling or caching of the returned HTableInterface    * is neither required nor desired.    * Note that the HConnection needs to be unmanaged    * (created with {@link HConnectionManager#createConnection(Configuration)}).    * @param tableName    * @return an HTable to use for interactions with this table    */
specifier|public
name|HTableInterface
name|getTable
parameter_list|(
name|byte
index|[]
name|tableName
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Retrieve an HTableInterface implementation for access to a table.    * The returned HTableInterface is not thread safe, a new instance should    * be created for each using thread.    * This is a lightweight operation, pooling or caching of the returned HTableInterface    * is neither required nor desired.    * Note that the HConnection needs to be unmanaged    * (created with {@link HConnectionManager#createConnection(Configuration)}).    * @param tableName    * @return an HTable to use for interactions with this table    */
annotation|@
name|Override
specifier|public
name|HTableInterface
name|getTable
parameter_list|(
name|TableName
name|tableName
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Retrieve an HTableInterface implementation for access to a table.    * The returned HTableInterface is not thread safe, a new instance should    * be created for each using thread.    * This is a lightweight operation, pooling or caching of the returned HTableInterface    * is neither required nor desired.    * Note that the HConnection needs to be unmanaged    * (created with {@link HConnectionManager#createConnection(Configuration)}).    * @param tableName    * @param pool The thread pool to use for batch operations, null to use a default pool.    * @return an HTable to use for interactions with this table    */
specifier|public
name|HTableInterface
name|getTable
parameter_list|(
name|String
name|tableName
parameter_list|,
name|ExecutorService
name|pool
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Retrieve an HTableInterface implementation for access to a table.    * The returned HTableInterface is not thread safe, a new instance should    * be created for each using thread.    * This is a lightweight operation, pooling or caching of the returned HTableInterface    * is neither required nor desired.    * Note that the HConnection needs to be unmanaged    * (created with {@link HConnectionManager#createConnection(Configuration)}).    * @param tableName    * @param pool The thread pool to use for batch operations, null to use a default pool.    * @return an HTable to use for interactions with this table    */
specifier|public
name|HTableInterface
name|getTable
parameter_list|(
name|byte
index|[]
name|tableName
parameter_list|,
name|ExecutorService
name|pool
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Retrieve an HTableInterface implementation for access to a table.    * The returned HTableInterface is not thread safe, a new instance should    * be created for each using thread.    * This is a lightweight operation, pooling or caching of the returned HTableInterface    * is neither required nor desired.    * Note that the HConnection needs to be unmanaged    * (created with {@link HConnectionManager#createConnection(Configuration)}).    * @param tableName    * @param pool The thread pool to use for batch operations, null to use a default pool.    * @return an HTable to use for interactions with this table    */
annotation|@
name|Override
specifier|public
name|HTableInterface
name|getTable
parameter_list|(
name|TableName
name|tableName
parameter_list|,
name|ExecutorService
name|pool
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Retrieve a RegionLocator implementation to inspect region information on a table. The returned    * RegionLocator is not thread-safe, so a new instance should be created for each using thread.    *    * This is a lightweight operation.  Pooling or caching of the returned RegionLocator is neither    * required nor desired.    *    * RegionLocator needs to be unmanaged    * (created with {@link HConnectionManager#createConnection(Configuration)}).    *    * @param tableName Name of the table who's region is to be examined    * @return A RegionLocator instance    */
annotation|@
name|Override
specifier|public
name|RegionLocator
name|getRegionLocator
parameter_list|(
name|TableName
name|tableName
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Retrieve an Admin implementation to administer an HBase cluster.    * The returned Admin is not guaranteed to be thread-safe.  A new instance should be created for    * each using thread.  This is a lightweight operation.  Pooling or caching of the returned    * Admin is not recommended.  Note that HConnection needs to be unmanaged    *    * @return an Admin instance for cluster administration    */
annotation|@
name|Override
name|Admin
name|getAdmin
parameter_list|()
throws|throws
name|IOException
function_decl|;
comment|/** @return - true if the master server is running    * @deprecated internal method, do not use thru HConnection */
annotation|@
name|Deprecated
name|boolean
name|isMasterRunning
parameter_list|()
throws|throws
name|MasterNotRunningException
throws|,
name|ZooKeeperConnectionException
function_decl|;
comment|/**    * A table that isTableEnabled == false and isTableDisabled == false    * is possible. This happens when a table has a lot of regions    * that must be processed.    * @param tableName table name    * @return true if the table is enabled, false otherwise    * @throws IOException if a remote or network exception occurs    */
name|boolean
name|isTableEnabled
parameter_list|(
name|TableName
name|tableName
parameter_list|)
throws|throws
name|IOException
function_decl|;
annotation|@
name|Deprecated
name|boolean
name|isTableEnabled
parameter_list|(
name|byte
index|[]
name|tableName
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * @param tableName table name    * @return true if the table is disabled, false otherwise    * @throws IOException if a remote or network exception occurs    */
name|boolean
name|isTableDisabled
parameter_list|(
name|TableName
name|tableName
parameter_list|)
throws|throws
name|IOException
function_decl|;
annotation|@
name|Deprecated
name|boolean
name|isTableDisabled
parameter_list|(
name|byte
index|[]
name|tableName
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Retrieve TableState, represent current table state.    * @param tableName table state for    * @return state of the table    */
specifier|public
name|TableState
name|getTableState
parameter_list|(
name|TableName
name|tableName
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * @param tableName table name    * @return true if all regions of the table are available, false otherwise    * @throws IOException if a remote or network exception occurs    */
name|boolean
name|isTableAvailable
parameter_list|(
name|TableName
name|tableName
parameter_list|)
throws|throws
name|IOException
function_decl|;
annotation|@
name|Deprecated
name|boolean
name|isTableAvailable
parameter_list|(
name|byte
index|[]
name|tableName
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Use this api to check if the table has been created with the specified number of    * splitkeys which was used while creating the given table.    * Note : If this api is used after a table's region gets splitted, the api may return    * false.    * @param tableName    *          tableName    * @param splitKeys    *          splitKeys used while creating table    * @throws IOException    *           if a remote or network exception occurs    * @deprecated internal method, do not use thru HConnection */
annotation|@
name|Deprecated
name|boolean
name|isTableAvailable
parameter_list|(
name|TableName
name|tableName
parameter_list|,
name|byte
index|[]
index|[]
name|splitKeys
parameter_list|)
throws|throws
name|IOException
function_decl|;
annotation|@
name|Deprecated
name|boolean
name|isTableAvailable
parameter_list|(
name|byte
index|[]
name|tableName
parameter_list|,
name|byte
index|[]
index|[]
name|splitKeys
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * List all the userspace tables.  In other words, scan the hbase:meta table.    *    * If we wanted this to be really fast, we could implement a special    * catalog table that just contains table names and their descriptors.    * Right now, it only exists as part of the hbase:meta table's region info.    *    * @return - returns an array of HTableDescriptors    * @throws IOException if a remote or network exception occurs    */
name|HTableDescriptor
index|[]
name|listTables
parameter_list|()
throws|throws
name|IOException
function_decl|;
comment|// This is a bit ugly - We call this getTableNames in 0.94 and the
comment|// successor function, returning TableName, listTableNames in later versions
comment|// because Java polymorphism doesn't consider return value types
annotation|@
name|Deprecated
name|String
index|[]
name|getTableNames
parameter_list|()
throws|throws
name|IOException
function_decl|;
name|TableName
index|[]
name|listTableNames
parameter_list|()
throws|throws
name|IOException
function_decl|;
comment|/**    * @param tableName table name    * @return table metadata    * @throws IOException if a remote or network exception occurs    */
name|HTableDescriptor
name|getHTableDescriptor
parameter_list|(
name|TableName
name|tableName
parameter_list|)
throws|throws
name|IOException
function_decl|;
annotation|@
name|Deprecated
name|HTableDescriptor
name|getHTableDescriptor
parameter_list|(
name|byte
index|[]
name|tableName
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Find the location of the region of<i>tableName</i> that<i>row</i>    * lives in.    * @param tableName name of the table<i>row</i> is in    * @param row row key you're trying to find the region of    * @return HRegionLocation that describes where to find the region in    * question    * @throws IOException if a remote or network exception occurs    * @deprecated internal method, do not use thru HConnection    */
annotation|@
name|Deprecated
specifier|public
name|HRegionLocation
name|locateRegion
parameter_list|(
specifier|final
name|TableName
name|tableName
parameter_list|,
specifier|final
name|byte
index|[]
name|row
parameter_list|)
throws|throws
name|IOException
function_decl|;
annotation|@
name|Deprecated
specifier|public
name|HRegionLocation
name|locateRegion
parameter_list|(
specifier|final
name|byte
index|[]
name|tableName
parameter_list|,
specifier|final
name|byte
index|[]
name|row
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Allows flushing the region cache.    * @deprecated internal method, do not use thru HConnection */
annotation|@
name|Deprecated
name|void
name|clearRegionCache
parameter_list|()
function_decl|;
comment|/**    * Allows flushing the region cache of all locations that pertain to    *<code>tableName</code>    * @param tableName Name of the table whose regions we are to remove from    * cache.    * @deprecated internal method, do not use thru HConnection */
annotation|@
name|Deprecated
name|void
name|clearRegionCache
parameter_list|(
specifier|final
name|TableName
name|tableName
parameter_list|)
function_decl|;
annotation|@
name|Deprecated
name|void
name|clearRegionCache
parameter_list|(
specifier|final
name|byte
index|[]
name|tableName
parameter_list|)
function_decl|;
comment|/**    * Deletes cached locations for the specific region.    * @param location The location object for the region, to be purged from cache.    * @deprecated internal method, do not use thru HConnection */
annotation|@
name|Deprecated
name|void
name|deleteCachedRegionLocation
parameter_list|(
specifier|final
name|HRegionLocation
name|location
parameter_list|)
function_decl|;
comment|/**    * Find the location of the region of<i>tableName</i> that<i>row</i>    * lives in, ignoring any value that might be in the cache.    * @param tableName name of the table<i>row</i> is in    * @param row row key you're trying to find the region of    * @return HRegionLocation that describes where to find the region in    * question    * @throws IOException if a remote or network exception occurs    * @deprecated internal method, do not use thru HConnection */
annotation|@
name|Deprecated
name|HRegionLocation
name|relocateRegion
parameter_list|(
specifier|final
name|TableName
name|tableName
parameter_list|,
specifier|final
name|byte
index|[]
name|row
parameter_list|)
throws|throws
name|IOException
function_decl|;
annotation|@
name|Deprecated
name|HRegionLocation
name|relocateRegion
parameter_list|(
specifier|final
name|byte
index|[]
name|tableName
parameter_list|,
specifier|final
name|byte
index|[]
name|row
parameter_list|)
throws|throws
name|IOException
function_decl|;
annotation|@
name|Deprecated
name|void
name|updateCachedLocations
parameter_list|(
name|TableName
name|tableName
parameter_list|,
name|byte
index|[]
name|rowkey
parameter_list|,
name|Object
name|exception
parameter_list|,
name|HRegionLocation
name|source
parameter_list|)
function_decl|;
comment|/**    * Update the location cache. This is used internally by HBase, in most cases it should not be    *  used by the client application.    * @param tableName the table name    * @param regionName the regionName    * @param rowkey the row    * @param exception the exception if any. Can be null.    * @param source the previous location    * @deprecated internal method, do not use thru HConnection    */
annotation|@
name|Deprecated
name|void
name|updateCachedLocations
parameter_list|(
name|TableName
name|tableName
parameter_list|,
name|byte
index|[]
name|regionName
parameter_list|,
name|byte
index|[]
name|rowkey
parameter_list|,
name|Object
name|exception
parameter_list|,
name|ServerName
name|source
parameter_list|)
function_decl|;
annotation|@
name|Deprecated
name|void
name|updateCachedLocations
parameter_list|(
name|byte
index|[]
name|tableName
parameter_list|,
name|byte
index|[]
name|rowkey
parameter_list|,
name|Object
name|exception
parameter_list|,
name|HRegionLocation
name|source
parameter_list|)
function_decl|;
comment|/**    * Gets the location of the region of<i>regionName</i>.    * @param regionName name of the region to locate    * @return HRegionLocation that describes where to find the region in    * question    * @throws IOException if a remote or network exception occurs    * @deprecated internal method, do not use thru HConnection */
annotation|@
name|Deprecated
name|HRegionLocation
name|locateRegion
parameter_list|(
specifier|final
name|byte
index|[]
name|regionName
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Gets the locations of all regions in the specified table,<i>tableName</i>.    * @param tableName table to get regions of    * @return list of region locations for all regions of table    * @throws IOException    * @deprecated internal method, do not use thru HConnection */
annotation|@
name|Deprecated
name|List
argument_list|<
name|HRegionLocation
argument_list|>
name|locateRegions
parameter_list|(
specifier|final
name|TableName
name|tableName
parameter_list|)
throws|throws
name|IOException
function_decl|;
annotation|@
name|Deprecated
name|List
argument_list|<
name|HRegionLocation
argument_list|>
name|locateRegions
parameter_list|(
specifier|final
name|byte
index|[]
name|tableName
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Gets the locations of all regions in the specified table,<i>tableName</i>.    * @param tableName table to get regions of    * @param useCache Should we use the cache to retrieve the region information.    * @param offlined True if we are to include offlined regions, false and we'll leave out offlined    *          regions from returned list.    * @return list of region locations for all regions of table    * @throws IOException    * @deprecated internal method, do not use thru HConnection    */
annotation|@
name|Deprecated
specifier|public
name|List
argument_list|<
name|HRegionLocation
argument_list|>
name|locateRegions
parameter_list|(
specifier|final
name|TableName
name|tableName
parameter_list|,
specifier|final
name|boolean
name|useCache
parameter_list|,
specifier|final
name|boolean
name|offlined
parameter_list|)
throws|throws
name|IOException
function_decl|;
annotation|@
name|Deprecated
specifier|public
name|List
argument_list|<
name|HRegionLocation
argument_list|>
name|locateRegions
parameter_list|(
specifier|final
name|byte
index|[]
name|tableName
parameter_list|,
specifier|final
name|boolean
name|useCache
parameter_list|,
specifier|final
name|boolean
name|offlined
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Returns a {@link MasterKeepAliveConnection} to the active master    * @deprecated internal method, do not use thru HConnection */
annotation|@
name|Deprecated
name|MasterService
operator|.
name|BlockingInterface
name|getMaster
parameter_list|()
throws|throws
name|IOException
function_decl|;
comment|/**    * Establishes a connection to the region server at the specified address.    * @param serverName    * @return proxy for HRegionServer    * @throws IOException if a remote or network exception occurs    * @deprecated internal method, do not use thru HConnection */
annotation|@
name|Deprecated
name|AdminService
operator|.
name|BlockingInterface
name|getAdmin
parameter_list|(
specifier|final
name|ServerName
name|serverName
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Establishes a connection to the region server at the specified address, and returns    * a region client protocol.    *    * @param serverName    * @return ClientProtocol proxy for RegionServer    * @throws IOException if a remote or network exception occurs    * @deprecated internal method, do not use thru HConnection */
annotation|@
name|Deprecated
name|ClientService
operator|.
name|BlockingInterface
name|getClient
parameter_list|(
specifier|final
name|ServerName
name|serverName
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Establishes a connection to the region server at the specified address.    * @param serverName    * @param getMaster do we check if master is alive    * @return proxy for HRegionServer    * @throws IOException if a remote or network exception occurs    * @deprecated You can pass master flag but nothing special is done.    */
annotation|@
name|Deprecated
name|AdminService
operator|.
name|BlockingInterface
name|getAdmin
parameter_list|(
specifier|final
name|ServerName
name|serverName
parameter_list|,
name|boolean
name|getMaster
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Find region location hosting passed row    * @param tableName table name    * @param row Row to find.    * @param reload If true do not use cache, otherwise bypass.    * @return Location of row.    * @throws IOException if a remote or network exception occurs    * @deprecated internal method, do not use thru HConnection */
annotation|@
name|Deprecated
name|HRegionLocation
name|getRegionLocation
parameter_list|(
name|TableName
name|tableName
parameter_list|,
name|byte
index|[]
name|row
parameter_list|,
name|boolean
name|reload
parameter_list|)
throws|throws
name|IOException
function_decl|;
annotation|@
name|Deprecated
name|HRegionLocation
name|getRegionLocation
parameter_list|(
name|byte
index|[]
name|tableName
parameter_list|,
name|byte
index|[]
name|row
parameter_list|,
name|boolean
name|reload
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Process a mixed batch of Get, Put and Delete actions. All actions for a    * RegionServer are forwarded in one RPC call.    *    *    * @param actions The collection of actions.    * @param tableName Name of the hbase table    * @param pool thread pool for parallel execution    * @param results An empty array, same size as list. If an exception is thrown,    * you can test here for partial results, and to determine which actions    * processed successfully.    * @throws IOException if there are problems talking to META. Per-item    * exceptions are stored in the results array.    * @deprecated since 0.96 - Use {@link HTableInterface#batch} instead    */
annotation|@
name|Deprecated
name|void
name|processBatch
parameter_list|(
name|List
argument_list|<
name|?
extends|extends
name|Row
argument_list|>
name|actions
parameter_list|,
specifier|final
name|TableName
name|tableName
parameter_list|,
name|ExecutorService
name|pool
parameter_list|,
name|Object
index|[]
name|results
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
function_decl|;
annotation|@
name|Deprecated
name|void
name|processBatch
parameter_list|(
name|List
argument_list|<
name|?
extends|extends
name|Row
argument_list|>
name|actions
parameter_list|,
specifier|final
name|byte
index|[]
name|tableName
parameter_list|,
name|ExecutorService
name|pool
parameter_list|,
name|Object
index|[]
name|results
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
function_decl|;
comment|/**    * Parameterized batch processing, allowing varying return types for different    * {@link Row} implementations.    * @deprecated since 0.96 - Use {@link HTableInterface#batchCallback} instead    */
annotation|@
name|Deprecated
specifier|public
parameter_list|<
name|R
parameter_list|>
name|void
name|processBatchCallback
parameter_list|(
name|List
argument_list|<
name|?
extends|extends
name|Row
argument_list|>
name|list
parameter_list|,
specifier|final
name|TableName
name|tableName
parameter_list|,
name|ExecutorService
name|pool
parameter_list|,
name|Object
index|[]
name|results
parameter_list|,
name|Batch
operator|.
name|Callback
argument_list|<
name|R
argument_list|>
name|callback
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
function_decl|;
annotation|@
name|Deprecated
specifier|public
parameter_list|<
name|R
parameter_list|>
name|void
name|processBatchCallback
parameter_list|(
name|List
argument_list|<
name|?
extends|extends
name|Row
argument_list|>
name|list
parameter_list|,
specifier|final
name|byte
index|[]
name|tableName
parameter_list|,
name|ExecutorService
name|pool
parameter_list|,
name|Object
index|[]
name|results
parameter_list|,
name|Batch
operator|.
name|Callback
argument_list|<
name|R
argument_list|>
name|callback
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
function_decl|;
comment|/**    * @deprecated does nothing since since 0.99    **/
annotation|@
name|Deprecated
specifier|public
name|void
name|setRegionCachePrefetch
parameter_list|(
specifier|final
name|TableName
name|tableName
parameter_list|,
specifier|final
name|boolean
name|enable
parameter_list|)
function_decl|;
comment|/**    * @deprecated does nothing since 0.99    **/
annotation|@
name|Deprecated
specifier|public
name|void
name|setRegionCachePrefetch
parameter_list|(
specifier|final
name|byte
index|[]
name|tableName
parameter_list|,
specifier|final
name|boolean
name|enable
parameter_list|)
function_decl|;
comment|/**    * @deprecated always return false since 0.99    **/
annotation|@
name|Deprecated
name|boolean
name|getRegionCachePrefetch
parameter_list|(
specifier|final
name|TableName
name|tableName
parameter_list|)
function_decl|;
comment|/**    * @deprecated always return false since 0.99    **/
annotation|@
name|Deprecated
name|boolean
name|getRegionCachePrefetch
parameter_list|(
specifier|final
name|byte
index|[]
name|tableName
parameter_list|)
function_decl|;
comment|/**    * @return the number of region servers that are currently running    * @throws IOException if a remote or network exception occurs    * @deprecated This method will be changed from public to package protected.    */
annotation|@
name|Deprecated
name|int
name|getCurrentNrHRS
parameter_list|()
throws|throws
name|IOException
function_decl|;
comment|/**    * @param tableNames List of table names    * @return HTD[] table metadata    * @throws IOException if a remote or network exception occurs    */
name|HTableDescriptor
index|[]
name|getHTableDescriptorsByTableName
parameter_list|(
name|List
argument_list|<
name|TableName
argument_list|>
name|tableNames
parameter_list|)
throws|throws
name|IOException
function_decl|;
annotation|@
name|Deprecated
name|HTableDescriptor
index|[]
name|getHTableDescriptors
parameter_list|(
name|List
argument_list|<
name|String
argument_list|>
name|tableNames
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * @return true if this connection is closed    */
annotation|@
name|Override
name|boolean
name|isClosed
parameter_list|()
function_decl|;
comment|/**    * Clear any caches that pertain to server name<code>sn</code>.    * @param sn A server name    * @deprecated internal method, do not use thru HConnection */
annotation|@
name|Deprecated
name|void
name|clearCaches
parameter_list|(
specifier|final
name|ServerName
name|sn
parameter_list|)
function_decl|;
comment|/**    * This function allows HBaseAdmin and potentially others to get a shared MasterService    * connection.    * @return The shared instance. Never returns null.    * @throws MasterNotRunningException    * @deprecated Since 0.96.0    */
comment|// TODO: Why is this in the public interface when the returned type is shutdown package access?
annotation|@
name|Deprecated
name|MasterKeepAliveConnection
name|getKeepAliveMasterService
parameter_list|()
throws|throws
name|MasterNotRunningException
function_decl|;
comment|/**    * @param serverName    * @return true if the server is known as dead, false otherwise.    * @deprecated internal method, do not use thru HConnection */
annotation|@
name|Deprecated
name|boolean
name|isDeadServer
parameter_list|(
name|ServerName
name|serverName
parameter_list|)
function_decl|;
comment|/**    * @return Nonce generator for this HConnection; may be null if disabled in configuration.    * @deprecated internal method, do not use thru HConnection */
annotation|@
name|Deprecated
specifier|public
name|NonceGenerator
name|getNonceGenerator
parameter_list|()
function_decl|;
block|}
end_interface

end_unit

