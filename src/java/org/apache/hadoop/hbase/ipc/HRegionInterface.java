begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2009 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|regionserver
operator|.
name|HRegion
import|;
end_import

begin_comment
comment|/**  * Clients interact with HRegionServers using a handle to the HRegionInterface.  *   *<p>NOTE: if you change the interface, you must change the RPC version  * number in HBaseRPCProtocolVersion  *   */
end_comment

begin_interface
specifier|public
interface|interface
name|HRegionInterface
extends|extends
name|HBaseRPCProtocolVersion
block|{
comment|/**     * Get metainfo about an HRegion    *     * @param regionName name of the region    * @return HRegionInfo object for region    * @throws NotServingRegionException    */
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
function_decl|;
comment|/**    * Return all the data for the row that matches<i>row</i> exactly,     * or the one that immediately preceeds it.    *     * @param regionName region name    * @param row row key    * @param family Column family to look for row in.    * @return map of values    * @throws IOException    */
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
comment|/**    *     * @return the regions served by this regionserver    */
specifier|public
name|HRegion
index|[]
name|getOnlineRegionsAsArray
parameter_list|()
function_decl|;
comment|/**    * Perform Get operation.    * @param regionName name of region to get from    * @param get Get operation    * @return Result    * @throws IOException    */
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
comment|/**    * Perform exists operation.    * @param regionName name of region to get from    * @param get Get operation describing cell to test    * @return true if exists    * @throws IOException    */
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
comment|/**    * Put data into the specified region     * @param regionName    * @param put the data to be put    * @throws IOException    */
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
comment|/**    * Put an array of puts into the specified region    *     * @param regionName    * @param puts    * @return The number of processed put's.  Returns -1 if all Puts    * processed successfully.    * @throws IOException    */
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
name|Put
index|[]
name|puts
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Deletes all the KeyValues that match those found in the Delete object,     * if their ts<= to the Delete. In case of a delete with a specific ts it    * only deletes that specific KeyValue.    * @param regionName    * @param delete    * @throws IOException    */
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
comment|/**    * Put an array of deletes into the specified region    *     * @param regionName    * @param deletes    * @return The number of processed deletes.  Returns -1 if all Deletes    * processed successfully.    * @throws IOException    */
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
name|Delete
index|[]
name|deletes
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Atomically checks if a row/family/qualifier value match the expectedValue.    * If it does, it adds the put.    *     * @param regionName    * @param row    * @param family    * @param qualifier    * @param value the expected value    * @param put    * @throws IOException    * @return true if the new put was execute, false otherwise    */
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
comment|/**    * Atomically increments a column value. If the column value isn't long-like,    * this could throw an exception.    *     * @param regionName    * @param row    * @param family    * @param qualifier    * @param amount    * @param writeToWAL whether to write the increment to the WAL    * @return new incremented column value    * @throws IOException    */
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
comment|//
comment|// remote scanner interface
comment|//
comment|/**    * Opens a remote scanner with a RowFilter.    *     * @param regionName name of region to scan    * @param scan configured scan object    * @return scannerId scanner identifier used in other calls    * @throws IOException    */
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
comment|/**    * Get the next set of values    * @param scannerId clientId passed to openScanner    * @return map of values; returns null if no results.    * @throws IOException    */
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
comment|/**    * Get the next set of values    * @param scannerId clientId passed to openScanner    * @param numberOfRows the number of rows to fetch    * @return Array of Results (map of values); array is empty if done with this    * region and null if we are NOT to go to the next region (happens when a    * filter rules that the scan is done).    * @throws IOException    */
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
comment|/**    * Close a scanner    *     * @param scannerId the scanner id returned by openScanner    * @throws IOException    */
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
comment|/**    * Opens a remote row lock.    *    * @param regionName name of region    * @param row row to lock    * @return lockId lock identifier    * @throws IOException    */
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
comment|/**    * Releases a remote row lock.    *    * @param regionName    * @param lockId the lock id returned by lockRow    * @throws IOException    */
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
comment|/**    * Method used when a master is taking the place of another failed one.    * @return All regions assigned on this region server    * @throws IOException    */
specifier|public
name|HRegionInfo
index|[]
name|getRegionsAssignment
parameter_list|()
throws|throws
name|IOException
function_decl|;
comment|/**    * Method used when a master is taking the place of another failed one.    * @return The HSI    * @throws IOException    */
specifier|public
name|HServerInfo
name|getHServerInfo
parameter_list|()
throws|throws
name|IOException
function_decl|;
block|}
end_interface

end_unit

