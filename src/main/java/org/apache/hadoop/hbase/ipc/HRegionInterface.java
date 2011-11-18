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
name|ipc
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
name|net
operator|.
name|ConnectException
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
name|HServerInfo
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
name|NotServingRegionException
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
name|client
operator|.
name|Append
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
name|Delete
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
name|Get
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
name|Increment
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
name|MultiAction
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
name|MultiResponse
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
name|Put
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
name|Result
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
name|Scan
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
name|Exec
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
name|ExecResult
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
name|filter
operator|.
name|CompareFilter
operator|.
name|CompareOp
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
name|filter
operator|.
name|WritableByteArrayComparable
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
name|io
operator|.
name|hfile
operator|.
name|BlockCacheColumnFamilySummary
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
name|RegionOpeningState
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
name|FailedLogCloseException
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
name|security
operator|.
name|TokenInfo
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
name|security
operator|.
name|KerberosInfo
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
name|Pair
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
name|ipc
operator|.
name|RemoteException
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
name|VersionedProtocol
import|;
end_import

begin_comment
comment|/**  * Clients interact with HRegionServers using a handle to the HRegionInterface.  *  *<p>NOTE: if you change the interface, you must change the RPC version  * number in HBaseRPCProtocolVersion  */
end_comment

begin_interface
annotation|@
name|KerberosInfo
argument_list|(
name|serverPrincipal
operator|=
literal|"hbase.regionserver.kerberos.principal"
argument_list|)
annotation|@
name|TokenInfo
argument_list|(
literal|"HBASE_AUTH_TOKEN"
argument_list|)
specifier|public
interface|interface
name|HRegionInterface
extends|extends
name|VersionedProtocol
extends|,
name|Stoppable
extends|,
name|Abortable
block|{
comment|/**    * This Interfaces' version. Version changes when the Interface changes.    */
comment|// All HBase Interfaces used derive from HBaseRPCProtocolVersion.  It
comment|// maintained a single global version number on all HBase Interfaces.  This
comment|// meant all HBase RPC was broke though only one of the three RPC Interfaces
comment|// had changed.  This has since been undone.
specifier|public
specifier|static
specifier|final
name|long
name|VERSION
init|=
literal|29L
decl_stmt|;
comment|/**    * Get metainfo about an HRegion    *    * @param regionName name of the region    * @return HRegionInfo object for region    * @throws NotServingRegionException    * @throws ConnectException    * @throws IOException This can manifest as an Hadoop ipc {@link RemoteException}    */
specifier|public
name|HRegionInfo
name|getRegionInfo
parameter_list|(
specifier|final
name|byte
index|[]
name|regionName
parameter_list|)
throws|throws
name|NotServingRegionException
throws|,
name|ConnectException
throws|,
name|IOException
function_decl|;
comment|/**    * Return all the data for the row that matches<i>row</i> exactly,    * or the one that immediately preceeds it.    *    * @param regionName region name    * @param row row key    * @param family Column family to look for row in.    * @return map of values    * @throws IOException e    */
specifier|public
name|Result
name|getClosestRowBefore
parameter_list|(
specifier|final
name|byte
index|[]
name|regionName
parameter_list|,
specifier|final
name|byte
index|[]
name|row
parameter_list|,
specifier|final
name|byte
index|[]
name|family
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Perform Get operation.    * @param regionName name of region to get from    * @param get Get operation    * @return Result    * @throws IOException e    */
specifier|public
name|Result
name|get
parameter_list|(
name|byte
index|[]
name|regionName
parameter_list|,
name|Get
name|get
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Perform exists operation.    * @param regionName name of region to get from    * @param get Get operation describing cell to test    * @return true if exists    * @throws IOException e    */
specifier|public
name|boolean
name|exists
parameter_list|(
name|byte
index|[]
name|regionName
parameter_list|,
name|Get
name|get
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Put data into the specified region    * @param regionName region name    * @param put the data to be put    * @throws IOException e    */
specifier|public
name|void
name|put
parameter_list|(
specifier|final
name|byte
index|[]
name|regionName
parameter_list|,
specifier|final
name|Put
name|put
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Put an array of puts into the specified region    *    * @param regionName region name    * @param puts List of puts to execute    * @return The number of processed put's.  Returns -1 if all Puts    * processed successfully.    * @throws IOException e    */
specifier|public
name|int
name|put
parameter_list|(
specifier|final
name|byte
index|[]
name|regionName
parameter_list|,
specifier|final
name|List
argument_list|<
name|Put
argument_list|>
name|puts
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Deletes all the KeyValues that match those found in the Delete object,    * if their ts<= to the Delete. In case of a delete with a specific ts it    * only deletes that specific KeyValue.    * @param regionName region name    * @param delete delete object    * @throws IOException e    */
specifier|public
name|void
name|delete
parameter_list|(
specifier|final
name|byte
index|[]
name|regionName
parameter_list|,
specifier|final
name|Delete
name|delete
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Put an array of deletes into the specified region    *    * @param regionName region name    * @param deletes delete List to execute    * @return The number of processed deletes.  Returns -1 if all Deletes    * processed successfully.    * @throws IOException e    */
specifier|public
name|int
name|delete
parameter_list|(
specifier|final
name|byte
index|[]
name|regionName
parameter_list|,
specifier|final
name|List
argument_list|<
name|Delete
argument_list|>
name|deletes
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Atomically checks if a row/family/qualifier value match the expectedValue.    * If it does, it adds the put. If passed expected value is null, then the    * check is for non-existance of the row/column.    *    * @param regionName region name    * @param row row to check    * @param family column family    * @param qualifier column qualifier    * @param value the expected value    * @param put data to put if check succeeds    * @throws IOException e    * @return true if the new put was execute, false otherwise    */
specifier|public
name|boolean
name|checkAndPut
parameter_list|(
specifier|final
name|byte
index|[]
name|regionName
parameter_list|,
specifier|final
name|byte
index|[]
name|row
parameter_list|,
specifier|final
name|byte
index|[]
name|family
parameter_list|,
specifier|final
name|byte
index|[]
name|qualifier
parameter_list|,
specifier|final
name|byte
index|[]
name|value
parameter_list|,
specifier|final
name|Put
name|put
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Atomically checks if a row/family/qualifier value match the expectedValue.    * If it does, it adds the delete. If passed expected value is null, then the    * check is for non-existance of the row/column.    *    * @param regionName region name    * @param row row to check    * @param family column family    * @param qualifier column qualifier    * @param value the expected value    * @param delete data to delete if check succeeds    * @throws IOException e    * @return true if the new delete was execute, false otherwise    */
specifier|public
name|boolean
name|checkAndDelete
parameter_list|(
specifier|final
name|byte
index|[]
name|regionName
parameter_list|,
specifier|final
name|byte
index|[]
name|row
parameter_list|,
specifier|final
name|byte
index|[]
name|family
parameter_list|,
specifier|final
name|byte
index|[]
name|qualifier
parameter_list|,
specifier|final
name|byte
index|[]
name|value
parameter_list|,
specifier|final
name|Delete
name|delete
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Atomically increments a column value. If the column value isn't long-like,    * this could throw an exception. If passed expected value is null, then the    * check is for non-existance of the row/column.    *    * @param regionName region name    * @param row row to check    * @param family column family    * @param qualifier column qualifier    * @param amount long amount to increment    * @param writeToWAL whether to write the increment to the WAL    * @return new incremented column value    * @throws IOException e    */
specifier|public
name|long
name|incrementColumnValue
parameter_list|(
name|byte
index|[]
name|regionName
parameter_list|,
name|byte
index|[]
name|row
parameter_list|,
name|byte
index|[]
name|family
parameter_list|,
name|byte
index|[]
name|qualifier
parameter_list|,
name|long
name|amount
parameter_list|,
name|boolean
name|writeToWAL
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Appends values to one or more columns values in a row. Optionally    * Returns the updated keys after the append.    *<p>    * This operation does not appear atomic to readers. Appends are done    * under a row lock but readers do not take row locks.    * @param regionName region name    * @param append Append operation    * @return changed cells (maybe null)    */
specifier|public
name|Result
name|append
parameter_list|(
name|byte
index|[]
name|regionName
parameter_list|,
name|Append
name|append
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Increments one or more columns values in a row.  Returns the    * updated keys after the increment.    *<p>    * This operation does not appear atomic to readers.  Increments are done    * under a row lock but readers do not take row locks.    * @param regionName region name    * @param increment increment operation    * @return incremented cells    */
specifier|public
name|Result
name|increment
parameter_list|(
name|byte
index|[]
name|regionName
parameter_list|,
name|Increment
name|increment
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|//
comment|// remote scanner interface
comment|//
comment|/**    * Opens a remote scanner with a RowFilter.    *    * @param regionName name of region to scan    * @param scan configured scan object    * @return scannerId scanner identifier used in other calls    * @throws IOException e    */
specifier|public
name|long
name|openScanner
parameter_list|(
specifier|final
name|byte
index|[]
name|regionName
parameter_list|,
specifier|final
name|Scan
name|scan
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Get the next set of values    * @param scannerId clientId passed to openScanner    * @return map of values; returns null if no results.    * @throws IOException e    */
specifier|public
name|Result
name|next
parameter_list|(
name|long
name|scannerId
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Get the next set of values    * @param scannerId clientId passed to openScanner    * @param numberOfRows the number of rows to fetch    * @return Array of Results (map of values); array is empty if done with this    * region and null if we are NOT to go to the next region (happens when a    * filter rules that the scan is done).    * @throws IOException e    */
specifier|public
name|Result
index|[]
name|next
parameter_list|(
name|long
name|scannerId
parameter_list|,
name|int
name|numberOfRows
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Close a scanner    *    * @param scannerId the scanner id returned by openScanner    * @throws IOException e    */
specifier|public
name|void
name|close
parameter_list|(
name|long
name|scannerId
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Opens a remote row lock.    *    * @param regionName name of region    * @param row row to lock    * @return lockId lock identifier    * @throws IOException e    */
specifier|public
name|long
name|lockRow
parameter_list|(
specifier|final
name|byte
index|[]
name|regionName
parameter_list|,
specifier|final
name|byte
index|[]
name|row
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Releases a remote row lock.    *    * @param regionName region name    * @param lockId the lock id returned by lockRow    * @throws IOException e    */
specifier|public
name|void
name|unlockRow
parameter_list|(
specifier|final
name|byte
index|[]
name|regionName
parameter_list|,
specifier|final
name|long
name|lockId
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * @return All regions online on this region server    * @throws IOException e    */
specifier|public
name|List
argument_list|<
name|HRegionInfo
argument_list|>
name|getOnlineRegions
parameter_list|()
throws|throws
name|IOException
function_decl|;
comment|/**    * Method used when a master is taking the place of another failed one.    * @return This servers {@link HServerInfo}; it has RegionServer POV on the    * hostname which may not agree w/ how the Master sees this server.    * @throws IOException e    * @deprecated    */
specifier|public
name|HServerInfo
name|getHServerInfo
parameter_list|()
throws|throws
name|IOException
function_decl|;
comment|/**    * Method used for doing multiple actions(Deletes, Gets and Puts) in one call    * @param multi    * @return MultiResult    * @throws IOException    */
specifier|public
parameter_list|<
name|R
parameter_list|>
name|MultiResponse
name|multi
parameter_list|(
name|MultiAction
argument_list|<
name|R
argument_list|>
name|multi
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Atomically bulk load multiple HFiles (say from different column families)    * into an open region.    *     * @param familyPaths List of (family, hfile path) pairs    * @param regionName name of region to load hfiles into    * @return true if successful, false if failed recoverably    * @throws IOException if fails unrecoverably    */
specifier|public
name|boolean
name|bulkLoadHFiles
parameter_list|(
name|List
argument_list|<
name|Pair
argument_list|<
name|byte
index|[]
argument_list|,
name|String
argument_list|>
argument_list|>
name|familyPaths
parameter_list|,
name|byte
index|[]
name|regionName
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|// Master methods
comment|/**    * Opens the specified region.    *     * @param region    *          region to open    * @return RegionOpeningState     *         OPENED         - if region open request was successful.    *         ALREADY_OPENED - if the region was already opened.     *         FAILED_OPENING - if region opening failed.    *    * @throws IOException    */
specifier|public
name|RegionOpeningState
name|openRegion
parameter_list|(
specifier|final
name|HRegionInfo
name|region
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Opens the specified region.    * @param region    *          region to open    * @param versionOfOfflineNode    *          the version of znode to compare when RS transitions the znode from    *          OFFLINE state.    * @return RegionOpeningState     *         OPENED         - if region open request was successful.    *         ALREADY_OPENED - if the region was already opened.     *         FAILED_OPENING - if region opening failed.    * @throws IOException    */
specifier|public
name|RegionOpeningState
name|openRegion
parameter_list|(
name|HRegionInfo
name|region
parameter_list|,
name|int
name|versionOfOfflineNode
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Opens the specified regions.    * @param regions regions to open    * @throws IOException    */
specifier|public
name|void
name|openRegions
parameter_list|(
specifier|final
name|List
argument_list|<
name|HRegionInfo
argument_list|>
name|regions
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Closes the specified region.    * @param region region to close    * @return true if closing region, false if not    * @throws IOException    */
specifier|public
name|boolean
name|closeRegion
parameter_list|(
specifier|final
name|HRegionInfo
name|region
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Closes the specified region and will use or not use ZK during the close    * according to the specified flag.    * @param region region to close    * @param zk true if transitions should be done in ZK, false if not    * @return true if closing region, false if not    * @throws IOException    */
specifier|public
name|boolean
name|closeRegion
parameter_list|(
specifier|final
name|HRegionInfo
name|region
parameter_list|,
specifier|final
name|boolean
name|zk
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Closes the region in the RS with the specified encoded regionName and will    * use or not use ZK during the close according to the specified flag. Note    * that the encoded region name is in byte format.    *     * @param encodedRegionName    *          in bytes    * @param zk    *          true if to use zookeeper, false if need not.    * @return true if region is closed, false if not.    * @throws IOException    */
specifier|public
name|boolean
name|closeRegion
parameter_list|(
name|byte
index|[]
name|encodedRegionName
parameter_list|,
specifier|final
name|boolean
name|zk
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|// Region administrative methods
comment|/**    * Flushes the MemStore of the specified region.    *<p>    * This method is synchronous.    * @param regionInfo region to flush    * @throws NotServingRegionException    * @throws IOException    */
name|void
name|flushRegion
parameter_list|(
name|HRegionInfo
name|regionInfo
parameter_list|)
throws|throws
name|NotServingRegionException
throws|,
name|IOException
function_decl|;
comment|/**    * Splits the specified region.    *<p>    * This method currently flushes the region and then forces a compaction which    * will then trigger a split.  The flush is done synchronously but the    * compaction is asynchronous.    * @param regionInfo region to split    * @throws NotServingRegionException    * @throws IOException    */
name|void
name|splitRegion
parameter_list|(
name|HRegionInfo
name|regionInfo
parameter_list|)
throws|throws
name|NotServingRegionException
throws|,
name|IOException
function_decl|;
comment|/**    * Splits the specified region.    *<p>    * This method currently flushes the region and then forces a compaction which    * will then trigger a split.  The flush is done synchronously but the    * compaction is asynchronous.    * @param regionInfo region to split    * @param splitPoint the explicit row to split on    * @throws NotServingRegionException    * @throws IOException    */
name|void
name|splitRegion
parameter_list|(
name|HRegionInfo
name|regionInfo
parameter_list|,
name|byte
index|[]
name|splitPoint
parameter_list|)
throws|throws
name|NotServingRegionException
throws|,
name|IOException
function_decl|;
comment|/**    * Compacts the specified region.  Performs a major compaction if specified.    *<p>    * This method is asynchronous.    * @param regionInfo region to compact    * @param major true to force major compaction    * @throws NotServingRegionException    * @throws IOException    */
name|void
name|compactRegion
parameter_list|(
name|HRegionInfo
name|regionInfo
parameter_list|,
name|boolean
name|major
parameter_list|)
throws|throws
name|NotServingRegionException
throws|,
name|IOException
function_decl|;
comment|/**    * Replicates the given entries. The guarantee is that the given entries    * will be durable on the slave cluster if this method returns without    * any exception.    * hbase.replication has to be set to true for this to work.    *    * @param entries entries to replicate    * @throws IOException    */
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
function_decl|;
comment|/**    * Executes a single {@link org.apache.hadoop.hbase.ipc.CoprocessorProtocol}    * method using the registered protocol handlers.    * {@link CoprocessorProtocol} implementations must be registered via the    * {@link org.apache.hadoop.hbase.regionserver.HRegion#registerProtocol(Class, org.apache.hadoop.hbase.ipc.CoprocessorProtocol)}    * method before they are available.    *    * @param regionName name of the region against which the invocation is executed    * @param call an {@code Exec} instance identifying the protocol, method name,    *     and parameters for the method invocation    * @return an {@code ExecResult} instance containing the region name of the    *     invocation and the return value    * @throws IOException if no registered protocol handler is found or an error    *     occurs during the invocation    * @see org.apache.hadoop.hbase.regionserver.HRegion#registerProtocol(Class, org.apache.hadoop.hbase.ipc.CoprocessorProtocol)    */
name|ExecResult
name|execCoprocessor
parameter_list|(
name|byte
index|[]
name|regionName
parameter_list|,
name|Exec
name|call
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Atomically checks if a row/family/qualifier value match the expectedValue.    * If it does, it adds the put. If passed expected value is null, then the    * check is for non-existance of the row/column.    *    * @param regionName    * @param row    * @param family    * @param qualifier    * @param compareOp    * @param comparator    * @param put    * @throws IOException    * @return true if the new put was execute, false otherwise    */
specifier|public
name|boolean
name|checkAndPut
parameter_list|(
specifier|final
name|byte
index|[]
name|regionName
parameter_list|,
specifier|final
name|byte
index|[]
name|row
parameter_list|,
specifier|final
name|byte
index|[]
name|family
parameter_list|,
specifier|final
name|byte
index|[]
name|qualifier
parameter_list|,
specifier|final
name|CompareOp
name|compareOp
parameter_list|,
specifier|final
name|WritableByteArrayComparable
name|comparator
parameter_list|,
specifier|final
name|Put
name|put
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Atomically checks if a row/family/qualifier value match the expectedValue.    * If it does, it adds the delete. If passed expected value is null, then the    * check is for non-existance of the row/column.    *    * @param regionName    * @param row    * @param family    * @param qualifier    * @param compareOp    * @param comparator    * @param delete    * @throws IOException    * @return true if the new put was execute, false otherwise    */
specifier|public
name|boolean
name|checkAndDelete
parameter_list|(
specifier|final
name|byte
index|[]
name|regionName
parameter_list|,
specifier|final
name|byte
index|[]
name|row
parameter_list|,
specifier|final
name|byte
index|[]
name|family
parameter_list|,
specifier|final
name|byte
index|[]
name|qualifier
parameter_list|,
specifier|final
name|CompareOp
name|compareOp
parameter_list|,
specifier|final
name|WritableByteArrayComparable
name|comparator
parameter_list|,
specifier|final
name|Delete
name|delete
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Performs a BlockCache summary and returns a List of BlockCacheColumnFamilySummary objects.    * This method could be fairly heavyweight in that it evaluates the entire HBase file-system    * against what is in the RegionServer BlockCache.     *     * @return BlockCacheColumnFamilySummary    * @throws IOException exception    */
specifier|public
name|List
argument_list|<
name|BlockCacheColumnFamilySummary
argument_list|>
name|getBlockCacheColumnFamilySummaries
parameter_list|()
throws|throws
name|IOException
function_decl|;
comment|/**    * Roll the log writer. That is, start writing log messages to a new file.    *     * @throws IOException    * @throws FailedLogCloseException    * @return If lots of logs, flush the returned regions so next time through    * we can clean logs. Returns null if nothing to flush.  Names are actual    * region names as returned by {@link HRegionInfo#getEncodedName()}     */
specifier|public
name|byte
index|[]
index|[]
name|rollHLogWriter
parameter_list|()
throws|throws
name|IOException
throws|,
name|FailedLogCloseException
function_decl|;
block|}
end_interface

end_unit

