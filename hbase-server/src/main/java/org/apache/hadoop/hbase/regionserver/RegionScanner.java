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
name|regionserver
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
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
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
name|HBaseInterfaceAudience
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
name|yetus
operator|.
name|audience
operator|.
name|InterfaceStability
import|;
end_import

begin_comment
comment|/**  * RegionScanner describes iterators over rows in an HRegion.  */
end_comment

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|LimitedPrivate
argument_list|(
name|HBaseInterfaceAudience
operator|.
name|COPROC
argument_list|)
annotation|@
name|InterfaceStability
operator|.
name|Evolving
specifier|public
interface|interface
name|RegionScanner
extends|extends
name|InternalScanner
block|{
comment|/**    * @return The RegionInfo for this scanner.    */
name|RegionInfo
name|getRegionInfo
parameter_list|()
function_decl|;
comment|/**    * @return True if a filter indicates that this scanner will return no further rows.    * @throws IOException in case of I/O failure on a filter.    */
name|boolean
name|isFilterDone
parameter_list|()
throws|throws
name|IOException
function_decl|;
comment|/**    * Do a reseek to the required row. Should not be used to seek to a key which    * may come before the current position. Always seeks to the beginning of a    * row boundary.    *    * @throws IOException    * @throws IllegalArgumentException    *           if row is null    *    */
name|boolean
name|reseek
parameter_list|(
name|byte
index|[]
name|row
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * @return The preferred max buffersize. See    * {@link org.apache.hadoop.hbase.client.Scan#setMaxResultSize(long)}    */
name|long
name|getMaxResultSize
parameter_list|()
function_decl|;
comment|/**    * @return The Scanner's MVCC readPt see {@link MultiVersionConcurrencyControl}    */
name|long
name|getMvccReadPoint
parameter_list|()
function_decl|;
comment|/**    * @return The limit on the number of cells to retrieve on each call to next(). See    *         {@link org.apache.hadoop.hbase.client.Scan#setBatch(int)}    */
name|int
name|getBatch
parameter_list|()
function_decl|;
comment|/**    * Grab the next row's worth of values. This is a special internal method to be called from    * coprocessor hooks to avoid expensive setup. Caller must set the thread's readpoint, start and    * close a region operation, an synchronize on the scanner object. Caller should maintain and    * update metrics. See {@link #nextRaw(List, ScannerContext)}    * @param result return output array    * @return true if more rows exist after this one, false if scanner is done    * @throws IOException e    */
name|boolean
name|nextRaw
parameter_list|(
name|List
argument_list|<
name|Cell
argument_list|>
name|result
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Grab the next row's worth of values. The {@link ScannerContext} is used to enforce and track    * any limits associated with this call. Any progress that exists in the {@link ScannerContext}    * prior to calling this method will be LOST if {@link ScannerContext#getKeepProgress()} is false.    * Upon returning from this method, the {@link ScannerContext} will contain information about the    * progress made towards the limits. This is a special internal method to be called from    * coprocessor hooks to avoid expensive setup. Caller must set the thread's readpoint, start and    * close a region operation, an synchronize on the scanner object. Example:<code>    * HRegion region = ...;    * RegionScanner scanner = ...    * MultiVersionConcurrencyControl.setThreadReadPoint(scanner.getMvccReadPoint());    * region.startRegionOperation();    * try {    *   synchronized(scanner) {    *     ...    *     boolean moreRows = scanner.nextRaw(values);    *     ...    *   }    * } finally {    *   region.closeRegionOperation();    * }    *</code>    * @param result return output array    * @param scannerContext The {@link ScannerContext} instance encapsulating all limits that should    *          be tracked during calls to this method. The progress towards these limits can be    *          tracked within this instance.    * @return true if more rows exist after this one, false if scanner is done    * @throws IOException e    */
name|boolean
name|nextRaw
parameter_list|(
name|List
argument_list|<
name|Cell
argument_list|>
name|result
parameter_list|,
name|ScannerContext
name|scannerContext
parameter_list|)
throws|throws
name|IOException
function_decl|;
block|}
end_interface

end_unit

