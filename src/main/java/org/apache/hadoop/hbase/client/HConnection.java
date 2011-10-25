begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2010 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|Closeable
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
name|HServerAddress
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
name|catalog
operator|.
name|CatalogTracker
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
name|ipc
operator|.
name|CoprocessorProtocol
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
name|ipc
operator|.
name|HMasterInterface
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
name|ipc
operator|.
name|HRegionInterface
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
name|ZooKeeperWatcher
import|;
end_import

begin_comment
comment|/**  * Cluster connection.  Hosts a connection to the ZooKeeper ensemble and  * thereafter into the HBase cluster.  Knows how to locate regions out on the cluster,  * keeps a cache of locations and then knows how to recalibrate after they move.  * {@link HConnectionManager} manages instances of this class.  *  *<p>HConnections are used by {@link HTable} mostly but also by  * {@link HBaseAdmin}, {@link CatalogTracker},  * and {@link ZooKeeperWatcher}.  HConnection instances can be shared.  Sharing  * is usually what you want because rather than each HConnection instance  * having to do its own discovery of regions out on the cluster, instead, all  * clients get to share the one cache of locations.  Sharing makes cleanup of  * HConnections awkward.  See {@link HConnectionManager} for cleanup  * discussion.  *  * @see HConnectionManager  */
end_comment

begin_interface
specifier|public
interface|interface
name|HConnection
extends|extends
name|Abortable
extends|,
name|Closeable
block|{
comment|/**    * @return Configuration instance being used by this HConnection instance.    */
specifier|public
name|Configuration
name|getConfiguration
parameter_list|()
function_decl|;
comment|/**    * Retrieve ZooKeeperWatcher used by this connection.    * @return ZooKeeperWatcher handle being used by the connection.    * @throws IOException if a remote or network exception occurs    * @deprecated Removed because it was a mistake exposing zookeeper in this    * interface (ZooKeeper is an implementation detail).    */
specifier|public
name|ZooKeeperWatcher
name|getZooKeeperWatcher
parameter_list|()
throws|throws
name|IOException
function_decl|;
comment|/**    * @return proxy connection to master server for this instance    * @throws MasterNotRunningException if the master is not running    * @throws ZooKeeperConnectionException if unable to connect to zookeeper    */
specifier|public
name|HMasterInterface
name|getMaster
parameter_list|()
throws|throws
name|MasterNotRunningException
throws|,
name|ZooKeeperConnectionException
function_decl|;
comment|/** @return - true if the master server is running */
specifier|public
name|boolean
name|isMasterRunning
parameter_list|()
throws|throws
name|MasterNotRunningException
throws|,
name|ZooKeeperConnectionException
function_decl|;
comment|/**    * A table that isTableEnabled == false and isTableDisabled == false    * is possible. This happens when a table has a lot of regions    * that must be processed.    * @param tableName table name    * @return true if the table is enabled, false otherwise    * @throws IOException if a remote or network exception occurs    */
specifier|public
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
specifier|public
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
comment|/**    * @param tableName table name    * @return true if all regions of the table are available, false otherwise    * @throws IOException if a remote or network exception occurs    */
specifier|public
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
comment|/**    * List all the userspace tables.  In other words, scan the META table.    *    * If we wanted this to be really fast, we could implement a special    * catalog table that just contains table names and their descriptors.    * Right now, it only exists as part of the META table's region info.    *    * @return - returns an array of HTableDescriptors    * @throws IOException if a remote or network exception occurs    */
specifier|public
name|HTableDescriptor
index|[]
name|listTables
parameter_list|()
throws|throws
name|IOException
function_decl|;
comment|/**    * @param tableName table name    * @return table metadata    * @throws IOException if a remote or network exception occurs    */
specifier|public
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
comment|/**    * Find the location of the region of<i>tableName</i> that<i>row</i>    * lives in.    * @param tableName name of the table<i>row</i> is in    * @param row row key you're trying to find the region of    * @return HRegionLocation that describes where to find the region in    * question    * @throws IOException if a remote or network exception occurs    */
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
comment|/**    * Allows flushing the region cache.    */
specifier|public
name|void
name|clearRegionCache
parameter_list|()
function_decl|;
comment|/**    * Allows flushing the region cache of all locations that pertain to    *<code>tableName</code>    * @param tableName Name of the table whose regions we are to remove from    * cache.    */
specifier|public
name|void
name|clearRegionCache
parameter_list|(
specifier|final
name|byte
index|[]
name|tableName
parameter_list|)
function_decl|;
comment|/**    * Find the location of the region of<i>tableName</i> that<i>row</i>    * lives in, ignoring any value that might be in the cache.    * @param tableName name of the table<i>row</i> is in    * @param row row key you're trying to find the region of    * @return HRegionLocation that describes where to find the region in    * question    * @throws IOException if a remote or network exception occurs    */
specifier|public
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
comment|/**    * Gets the location of the region of<i>regionName</i>.    * @param regionName name of the region to locate    * @return HRegionLocation that describes where to find the region in    * question    * @throws IOException if a remote or network exception occurs    */
specifier|public
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
comment|/**    * Gets the locations of all regions in the specified table,<i>tableName</i>.    * @param tableName table to get regions of    * @return list of region locations for all regions of table    * @throws IOException    */
specifier|public
name|List
argument_list|<
name|HRegionLocation
argument_list|>
name|locateRegions
parameter_list|(
name|byte
index|[]
name|tableName
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Establishes a connection to the region server at the specified address.    * @param regionServer - the server to connect to    * @return proxy for HRegionServer    * @throws IOException if a remote or network exception occurs    * @deprecated Use {@link #getHRegionConnection(String, int)}    */
specifier|public
name|HRegionInterface
name|getHRegionConnection
parameter_list|(
name|HServerAddress
name|regionServer
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Establishes a connection to the region server at the specified address.    * @param hostname RegionServer hostname    * @param port RegionServer port    * @return proxy for HRegionServer    * @throws IOException if a remote or network exception occurs    *    */
specifier|public
name|HRegionInterface
name|getHRegionConnection
parameter_list|(
specifier|final
name|String
name|hostname
parameter_list|,
specifier|final
name|int
name|port
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Establishes a connection to the region server at the specified address.    * @param regionServer - the server to connect to    * @param getMaster - do we check if master is alive    * @return proxy for HRegionServer    * @throws IOException if a remote or network exception occurs    * @deprecated Use {@link #getHRegionConnection(HServerAddress, boolean)}    */
specifier|public
name|HRegionInterface
name|getHRegionConnection
parameter_list|(
name|HServerAddress
name|regionServer
parameter_list|,
name|boolean
name|getMaster
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Establishes a connection to the region server at the specified address.    * @param hostname RegionServer hostname    * @param port RegionServer port    * @param getMaster - do we check if master is alive    * @return proxy for HRegionServer    * @throws IOException if a remote or network exception occurs    */
specifier|public
name|HRegionInterface
name|getHRegionConnection
parameter_list|(
specifier|final
name|String
name|hostname
parameter_list|,
specifier|final
name|int
name|port
parameter_list|,
name|boolean
name|getMaster
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Find region location hosting passed row    * @param tableName table name    * @param row Row to find.    * @param reload If true do not use cache, otherwise bypass.    * @return Location of row.    * @throws IOException if a remote or network exception occurs    */
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
comment|/**    * Pass in a ServerCallable with your particular bit of logic defined and    * this method will manage the process of doing retries with timed waits    * and refinds of missing regions.    *    * @param<T> the type of the return value    * @param callable callable to run    * @return an object of type T    * @throws IOException if a remote or network exception occurs    * @throws RuntimeException other unspecified error    */
specifier|public
parameter_list|<
name|T
parameter_list|>
name|T
name|getRegionServerWithRetries
parameter_list|(
name|ServerCallable
argument_list|<
name|T
argument_list|>
name|callable
parameter_list|)
throws|throws
name|IOException
throws|,
name|RuntimeException
function_decl|;
comment|/**    * Pass in a ServerCallable with your particular bit of logic defined and    * this method will pass it to the defined region server.    * @param<T> the type of the return value    * @param callable callable to run    * @return an object of type T    * @throws IOException if a remote or network exception occurs    * @throws RuntimeException other unspecified error    */
specifier|public
parameter_list|<
name|T
parameter_list|>
name|T
name|getRegionServerWithoutRetries
parameter_list|(
name|ServerCallable
argument_list|<
name|T
argument_list|>
name|callable
parameter_list|)
throws|throws
name|IOException
throws|,
name|RuntimeException
function_decl|;
comment|/**    * Process a mixed batch of Get, Put and Delete actions. All actions for a    * RegionServer are forwarded in one RPC call.    *    *    * @param actions The collection of actions.    * @param tableName Name of the hbase table    * @param pool thread pool for parallel execution    * @param results An empty array, same size as list. If an exception is thrown,    * you can test here for partial results, and to determine which actions    * processed successfully.    * @throws IOException if there are problems talking to META. Per-item    * exceptions are stored in the results array.    */
specifier|public
name|void
name|processBatch
parameter_list|(
name|List
argument_list|<
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
comment|/**    * Parameterized batch processing, allowing varying return types for different    * {@link Row} implementations.    */
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
comment|/**    * Executes the given    * {@link org.apache.hadoop.hbase.client.coprocessor.Batch.Call}    * callable for each row in the given list and invokes    * {@link org.apache.hadoop.hbase.client.coprocessor.Batch.Callback#update(byte[], byte[], Object)}    * for each result returned.    *    * @param protocol the protocol interface being called    * @param rows a list of row keys for which the callable should be invoked    * @param tableName table name for the coprocessor invoked    * @param pool ExecutorService used to submit the calls per row    * @param call instance on which to invoke    * {@link org.apache.hadoop.hbase.client.coprocessor.Batch.Call#call(Object)}    * for each row    * @param callback instance on which to invoke    * {@link org.apache.hadoop.hbase.client.coprocessor.Batch.Callback#update(byte[], byte[], Object)}    * for each result    * @param<T> the protocol interface type    * @param<R> the callable's return type    * @throws IOException    */
specifier|public
parameter_list|<
name|T
extends|extends
name|CoprocessorProtocol
parameter_list|,
name|R
parameter_list|>
name|void
name|processExecs
parameter_list|(
specifier|final
name|Class
argument_list|<
name|T
argument_list|>
name|protocol
parameter_list|,
name|List
argument_list|<
name|byte
index|[]
argument_list|>
name|rows
parameter_list|,
specifier|final
name|byte
index|[]
name|tableName
parameter_list|,
name|ExecutorService
name|pool
parameter_list|,
specifier|final
name|Batch
operator|.
name|Call
argument_list|<
name|T
argument_list|,
name|R
argument_list|>
name|call
parameter_list|,
specifier|final
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
name|Throwable
function_decl|;
comment|/**    * Process a batch of Puts.    *    * @param list The collection of actions. The list is mutated: all successful Puts    * are removed from the list.    * @param tableName Name of the hbase table    * @param pool Thread pool for parallel execution    * @throws IOException    * @deprecated Use HConnectionManager::processBatch instead.    */
specifier|public
name|void
name|processBatchOfPuts
parameter_list|(
name|List
argument_list|<
name|Put
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
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Enable or disable region cache prefetch for the table. It will be    * applied for the given table's all HTable instances within this    * connection. By default, the cache prefetch is enabled.    * @param tableName name of table to configure.    * @param enable Set to true to enable region cache prefetch.    */
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
comment|/**    * Check whether region cache prefetch is enabled or not.    * @param tableName name of table to check    * @return true if table's region cache prefetch is enabled. Otherwise    * it is disabled.    */
specifier|public
name|boolean
name|getRegionCachePrefetch
parameter_list|(
specifier|final
name|byte
index|[]
name|tableName
parameter_list|)
function_decl|;
comment|/**    * Load the region map and warm up the global region cache for the table.    * @param tableName name of the table to perform region cache prewarm.    * @param regions a region map.    */
specifier|public
name|void
name|prewarmRegionCache
parameter_list|(
specifier|final
name|byte
index|[]
name|tableName
parameter_list|,
specifier|final
name|Map
argument_list|<
name|HRegionInfo
argument_list|,
name|HServerAddress
argument_list|>
name|regions
parameter_list|)
function_decl|;
comment|/**    * Scan zookeeper to get the number of region servers    * @return the number of region servers that are currently running    * @throws IOException if a remote or network exception occurs    * @deprecated This method will be changed from public to package protected.    */
specifier|public
name|int
name|getCurrentNrHRS
parameter_list|()
throws|throws
name|IOException
function_decl|;
comment|/**    * @param tableNames List of table names    * @return HTD[] table metadata    * @throws IOException if a remote or network exception occurs    */
specifier|public
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
block|}
end_interface

end_unit

