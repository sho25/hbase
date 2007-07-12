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
name|KeyedData
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
name|io
operator|.
name|Text
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
name|VersionedProtocol
import|;
end_import

begin_comment
comment|/*******************************************************************************  * Clients interact with HRegionServers using  * a handle to the HRegionInterface.  ******************************************************************************/
end_comment

begin_interface
specifier|public
interface|interface
name|HRegionInterface
extends|extends
name|VersionedProtocol
block|{
comment|/** initial version */
specifier|public
specifier|static
specifier|final
name|long
name|versionID
init|=
literal|1L
decl_stmt|;
comment|/**     * Get metainfo about an HRegion    *     * @param regionName                  - name of the region    * @return                            - HRegionInfo object for region    * @throws NotServingRegionException    */
specifier|public
name|HRegionInfo
name|getRegionInfo
parameter_list|(
specifier|final
name|Text
name|regionName
parameter_list|)
throws|throws
name|NotServingRegionException
function_decl|;
comment|/**    * Retrieve a single value from the specified region for the specified row    * and column keys    *     * @param regionName name of region    * @param row row key    * @param column column key    * @return alue for that region/row/column    * @throws IOException    */
specifier|public
name|byte
index|[]
name|get
parameter_list|(
specifier|final
name|Text
name|regionName
parameter_list|,
specifier|final
name|Text
name|row
parameter_list|,
specifier|final
name|Text
name|column
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Get the specified number of versions of the specified row and column    *     * @param regionName region name    * @param row row key    * @param column column key    * @param numVersions number of versions to return    * @return array of values    * @throws IOException    */
specifier|public
name|byte
index|[]
index|[]
name|get
parameter_list|(
specifier|final
name|Text
name|regionName
parameter_list|,
specifier|final
name|Text
name|row
parameter_list|,
specifier|final
name|Text
name|column
parameter_list|,
specifier|final
name|int
name|numVersions
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Get the specified number of versions of the specified row and column with    * the specified timestamp.    *    * @param regionName region name    * @param row row key    * @param column column key    * @param timestamp timestamp    * @param numVersions number of versions to return    * @return array of values    * @throws IOException    */
specifier|public
name|byte
index|[]
index|[]
name|get
parameter_list|(
specifier|final
name|Text
name|regionName
parameter_list|,
specifier|final
name|Text
name|row
parameter_list|,
specifier|final
name|Text
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
comment|/**    * Get all the data for the specified row    *     * @param regionName region name    * @param row row key    * @return array of values    * @throws IOException    */
specifier|public
name|KeyedData
index|[]
name|getRow
parameter_list|(
specifier|final
name|Text
name|regionName
parameter_list|,
specifier|final
name|Text
name|row
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|//////////////////////////////////////////////////////////////////////////////
comment|// Start an atomic row insertion/update.  No changes are committed until the
comment|// call to commit() returns. A call to abort() will abandon any updates in progress.
comment|//
comment|// Callers to this method are given a lease for each unique lockid; before the
comment|// lease expires, either abort() or commit() must be called. If it is not
comment|// called, the system will automatically call abort() on the client's behalf.
comment|//
comment|// The client can gain extra time with a call to renewLease().
comment|//////////////////////////////////////////////////////////////////////////////
comment|/**     * Start an atomic row insertion/update.  No changes are committed until the     * call to commit() returns. A call to abort() will abandon any updates in progress.    *    * Callers to this method are given a lease for each unique lockid; before the    * lease expires, either abort() or commit() must be called. If it is not     * called, the system will automatically call abort() on the client's behalf.    *    * The client can gain extra time with a call to renewLease().    * Start an atomic row insertion or update    *     * @param regionName region name    * @param clientid a unique value to identify the client    * @param row Name of row to start update against.    * @return Row lockid.    * @throws IOException    */
specifier|public
name|long
name|startUpdate
parameter_list|(
specifier|final
name|Text
name|regionName
parameter_list|,
specifier|final
name|long
name|clientid
parameter_list|,
specifier|final
name|Text
name|row
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**     * Change a value for the specified column    *    * @param regionName region name    * @param clientid a unique value to identify the client    * @param lockid lock id returned from startUpdate    * @param column column whose value is being set    * @param val new value for column    * @throws IOException    */
specifier|public
name|void
name|put
parameter_list|(
specifier|final
name|Text
name|regionName
parameter_list|,
specifier|final
name|long
name|clientid
parameter_list|,
specifier|final
name|long
name|lockid
parameter_list|,
specifier|final
name|Text
name|column
parameter_list|,
specifier|final
name|byte
index|[]
name|val
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**     * Delete the value for a column    *    * @param regionName region name    * @param clientid a unique value to identify the client    * @param lockid lock id returned from startUpdate    * @param column name of column whose value is to be deleted    * @throws IOException    */
specifier|public
name|void
name|delete
parameter_list|(
specifier|final
name|Text
name|regionName
parameter_list|,
specifier|final
name|long
name|clientid
parameter_list|,
specifier|final
name|long
name|lockid
parameter_list|,
specifier|final
name|Text
name|column
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**     * Abort a row mutation    *    * @param regionName region name    * @param clientid a unique value to identify the client    * @param lockid lock id returned from startUpdate    * @throws IOException    */
specifier|public
name|void
name|abort
parameter_list|(
specifier|final
name|Text
name|regionName
parameter_list|,
specifier|final
name|long
name|clientid
parameter_list|,
specifier|final
name|long
name|lockid
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**     * Finalize a row mutation    *    * @param regionName region name    * @param clientid a unique value to identify the client    * @param lockid lock id returned from startUpdate    * @param timestamp the time (in milliseconds to associate with this change)    * @throws IOException    */
specifier|public
name|void
name|commit
parameter_list|(
specifier|final
name|Text
name|regionName
parameter_list|,
specifier|final
name|long
name|clientid
parameter_list|,
specifier|final
name|long
name|lockid
parameter_list|,
specifier|final
name|long
name|timestamp
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Renew lease on update    *     * @param lockid lock id returned from startUpdate    * @param clientid a unique value to identify the client    * @throws IOException    */
specifier|public
name|void
name|renewLease
parameter_list|(
name|long
name|lockid
parameter_list|,
name|long
name|clientid
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|//////////////////////////////////////////////////////////////////////////////
comment|// remote scanner interface
comment|//////////////////////////////////////////////////////////////////////////////
comment|/**    * Opens a remote scanner with a RowFilter.    *     * @param regionName name of region to scan    * @param columns columns to scan    * @param startRow starting row to scan    * @param timestamp only return values whose timestamp is<= this value    * @param filter RowFilter for filtering results at the row-level.    *    * @return scannerId scanner identifier used in other calls    * @throws IOException    */
specifier|public
name|long
name|openScanner
parameter_list|(
name|Text
name|regionName
parameter_list|,
name|Text
index|[]
name|columns
parameter_list|,
name|Text
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
comment|/**    * Get the next set of values    *     * @param scannerId clientId passed to openScanner    * @return array of values    * @throws IOException    */
specifier|public
name|KeyedData
index|[]
name|next
parameter_list|(
name|long
name|scannerId
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
block|}
end_interface

end_unit

