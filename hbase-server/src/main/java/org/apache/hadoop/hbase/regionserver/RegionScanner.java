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
name|KeyValue
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

begin_comment
comment|/**  * RegionScanner describes iterators over rows in an HRegion.  */
end_comment

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
interface|interface
name|RegionScanner
extends|extends
name|InternalScanner
block|{
comment|/**    * @return The RegionInfo for this scanner.    */
specifier|public
name|HRegionInfo
name|getRegionInfo
parameter_list|()
function_decl|;
comment|/**    * @return True if a filter indicates that this scanner will return no    *         further rows.    */
specifier|public
name|boolean
name|isFilterDone
parameter_list|()
function_decl|;
comment|/**    * Do a reseek to the required row. Should not be used to seek to a key which    * may come before the current position. Always seeks to the beginning of a    * row boundary.    *    * @throws IOException    * @throws IllegalArgumentException    *           if row is null    *    */
specifier|public
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
comment|/**    * @return The preferred max buffersize. See {@link Scan#setMaxResultSize(long)}    */
specifier|public
name|long
name|getMaxResultSize
parameter_list|()
function_decl|;
comment|/**    * @return The Scanner's MVCC readPt see {@link MultiVersionConsistencyControl}    */
specifier|public
name|long
name|getMvccReadPoint
parameter_list|()
function_decl|;
comment|/**    * Grab the next row's worth of values with the default limit on the number of values    * to return.    * This is a special internal method to be called from coprocessor hooks to avoid expensive setup.    * Caller must set the thread's readpoint, start and close a region operation, an synchronize on the scanner object.    * See {@link #nextRaw(List, int, String)}    * @param result return output array    * @return true if more rows exist after this one, false if scanner is done    * @throws IOException e    */
specifier|public
name|boolean
name|nextRaw
parameter_list|(
name|List
argument_list|<
name|KeyValue
argument_list|>
name|result
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Grab the next row's worth of values with a limit on the number of values    * to return.    * This is a special internal method to be called from coprocessor hooks to avoid expensive setup.    * Caller must set the thread's readpoint, start and close a region operation, an synchronize on the scanner object.    * Example:    *<code><pre>    * HRegion region = ...;    * RegionScanner scanner = ...    * MultiVersionConsistencyControl.setThreadReadPoint(scanner.getMvccReadPoint());    * region.startRegionOperation();    * try {    *   synchronized(scanner) {    *     ...    *     boolean moreRows = scanner.nextRaw(values);    *     ...    *   }    * } finally {    *   region.closeRegionOperation();    * }    *</pre></code>    * @param result return output array    * @param limit limit on row count to get    * @param metric the metric name    * @return true if more rows exist after this one, false if scanner is done    * @throws IOException e    */
specifier|public
name|boolean
name|nextRaw
parameter_list|(
name|List
argument_list|<
name|KeyValue
argument_list|>
name|result
parameter_list|,
name|int
name|limit
parameter_list|,
name|String
name|metric
parameter_list|)
throws|throws
name|IOException
function_decl|;
block|}
end_interface

end_unit

