begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2007 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|filter
operator|.
name|RowFilterInterface
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
name|BatchUpdate
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
name|Cell
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
name|RowResult
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
name|HbaseMapWritable
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
comment|/**    * Get the specified number of versions of the specified row and column with    * the specified timestamp.    *    * @param regionName region name    * @param row row key    * @param column column key    * @param timestamp timestamp    * @param numVersions number of versions to return    * @return array of values    * @throws IOException    */
specifier|public
name|Cell
index|[]
name|get
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
name|column
parameter_list|,
specifier|final
name|long
name|timestamp
parameter_list|,
specifier|final
name|int
name|numVersions
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Return all the data for the row that matches<i>row</i> exactly,     * or the one that immediately preceeds it.    *     * @param regionName region name    * @param row row key    * @param columnFamily Column family to look for row in.    * @return map of values    * @throws IOException    */
specifier|public
name|RowResult
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
name|columnFamily
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Get selected columns for the specified row at a given timestamp.    *     * @param regionName region name    * @param row row key    * @param columns columns to get    * @param ts time stamp    * @param numVersions number of versions    * @param lockId lock id    * @return map of values    * @throws IOException    */
specifier|public
name|RowResult
name|getRow
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
index|[]
name|columns
parameter_list|,
specifier|final
name|long
name|ts
parameter_list|,
specifier|final
name|int
name|numVersions
parameter_list|,
specifier|final
name|long
name|lockId
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Applies a batch of updates via one RPC    *     * @param regionName name of the region to update    * @param b BatchUpdate    * @param lockId lock id    * @throws IOException    */
specifier|public
name|void
name|batchUpdate
parameter_list|(
specifier|final
name|byte
index|[]
name|regionName
parameter_list|,
specifier|final
name|BatchUpdate
name|b
parameter_list|,
specifier|final
name|long
name|lockId
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Applies a batch of updates via one RPC for many rows    *     * @param regionName name of the region to update    * @param b BatchUpdate[]    * @throws IOException    * @return number of updates applied    */
specifier|public
name|int
name|batchUpdates
parameter_list|(
specifier|final
name|byte
index|[]
name|regionName
parameter_list|,
specifier|final
name|BatchUpdate
index|[]
name|b
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Applies a batch of updates to one row atomically via one RPC    * if the columns specified in expectedValues match    * the given values in expectedValues    *     * @param regionName name of the region to update    * @param b BatchUpdate    * @param expectedValues map of column names to expected data values.    * @return true if update was applied    * @throws IOException    */
specifier|public
name|boolean
name|checkAndSave
parameter_list|(
specifier|final
name|byte
index|[]
name|regionName
parameter_list|,
specifier|final
name|BatchUpdate
name|b
parameter_list|,
specifier|final
name|HbaseMapWritable
argument_list|<
name|byte
index|[]
argument_list|,
name|byte
index|[]
argument_list|>
name|expectedValues
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Delete all cells that match the passed row and column and whose timestamp    * is equal-to or older than the passed timestamp.    *     * @param regionName region name    * @param row row key    * @param column column key    * @param timestamp Delete all entries that have this timestamp or older    * @param lockId lock id    * @throws IOException    */
specifier|public
name|void
name|deleteAll
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
name|column
parameter_list|,
name|long
name|timestamp
parameter_list|,
name|long
name|lockId
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Delete all cells that match the passed row and whose    * timestamp is equal-to or older than the passed timestamp.    *    * @param regionName region name    * @param row row key    * @param timestamp Delete all entries that have this timestamp or older    * @param lockId lock id    * @throws IOException    */
specifier|public
name|void
name|deleteAll
parameter_list|(
name|byte
index|[]
name|regionName
parameter_list|,
name|byte
index|[]
name|row
parameter_list|,
name|long
name|timestamp
parameter_list|,
name|long
name|lockId
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Delete all cells that match the passed row& the column regex and whose    * timestamp is equal-to or older than the passed timestamp.    *     * @param regionName    * @param row    * @param colRegex    * @param timestamp    * @param lockId    * @throws IOException    */
specifier|public
name|void
name|deleteAllByRegex
parameter_list|(
name|byte
index|[]
name|regionName
parameter_list|,
name|byte
index|[]
name|row
parameter_list|,
name|String
name|colRegex
parameter_list|,
name|long
name|timestamp
parameter_list|,
name|long
name|lockId
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Delete all cells for a row with matching column family with timestamps    * less than or equal to<i>timestamp</i>.    *    * @param regionName The name of the region to operate on    * @param row The row to operate on    * @param family The column family to match    * @param timestamp Timestamp to match    * @param lockId lock id    * @throws IOException    */
specifier|public
name|void
name|deleteFamily
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
name|long
name|timestamp
parameter_list|,
name|long
name|lockId
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Delete all cells for a row with matching column family regex with     * timestamps less than or equal to<i>timestamp</i>.    *     * @param regionName The name of the region to operate on    * @param row The row to operate on    * @param familyRegex column family regex    * @param timestamp Timestamp to match    * @param lockId lock id    * @throws IOException    */
specifier|public
name|void
name|deleteFamilyByRegex
parameter_list|(
name|byte
index|[]
name|regionName
parameter_list|,
name|byte
index|[]
name|row
parameter_list|,
name|String
name|familyRegex
parameter_list|,
name|long
name|timestamp
parameter_list|,
name|long
name|lockId
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Returns true if any cells exist for the given coordinate.    *     * @param regionName The name of the region    * @param row The row    * @param column The column, or null for any    * @param timestamp The timestamp, or LATEST_TIMESTAMP for any    * @param lockID lock id    * @return true if the row exists, false otherwise    * @throws IOException    */
specifier|public
name|boolean
name|exists
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
name|column
parameter_list|,
name|long
name|timestamp
parameter_list|,
name|long
name|lockID
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|//
comment|// remote scanner interface
comment|//
comment|/**    * Opens a remote scanner with a RowFilter.    *     * @param regionName name of region to scan    * @param columns columns to scan. If column name is a column family, all    * columns of the specified column family are returned.  Its also possible    * to pass a regex for column family name. A column name is judged to be    * regex if it contains at least one of the following characters:    *<code>\+|^&*$[]]}{)(</code>.    * @param startRow starting row to scan    * @param timestamp only return values whose timestamp is<= this value    * @param filter RowFilter for filtering results at the row-level.    *    * @return scannerId scanner identifier used in other calls    * @throws IOException    */
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
name|byte
index|[]
index|[]
name|columns
parameter_list|,
specifier|final
name|byte
index|[]
name|startRow
parameter_list|,
name|long
name|timestamp
parameter_list|,
name|RowFilterInterface
name|filter
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Get the next set of values    * @param scannerId clientId passed to openScanner    * @return map of values    * @throws IOException    */
specifier|public
name|RowResult
name|next
parameter_list|(
name|long
name|scannerId
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Get the next set of values    * @param scannerId clientId passed to openScanner    * @param numberOfRows the number of rows to fetch    * @return map of values    * @throws IOException    */
specifier|public
name|RowResult
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
comment|/**    * Atomically increments a column value. If the column value isn't long-like, this could    * throw an exception.    *     * @param regionName    * @param row    * @param column    * @param amount    * @return new incremented column value    * @throws IOException    */
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
name|column
parameter_list|,
name|long
name|amount
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

