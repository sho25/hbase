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
operator|.
name|querymatcher
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
name|Cell
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
name|hadoop
operator|.
name|hbase
operator|.
name|regionserver
operator|.
name|ShipperListener
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
name|querymatcher
operator|.
name|ScanQueryMatcher
operator|.
name|MatchCode
import|;
end_import

begin_comment
comment|/**  * Implementing classes of this interface will be used for the tracking  * and enforcement of columns and numbers of versions and timeToLive during  * the course of a Get or Scan operation.  *<p>  * Currently there are two different types of Store/Family-level queries.  *<ul><li>{@link ExplicitColumnTracker} is used when the query specifies  * one or more column qualifiers to return in the family.</li>  *<li>{@link ScanWildcardColumnTracker} is used when no columns are  * explicitly specified.</li>  *</ul>  *<p>  * This class is utilized by {@link ScanQueryMatcher} mainly through two methods:  *<ul><li>{@link #checkColumn} is called when a Put satisfies all other  * conditions of the query.</li>  *<li>{@link #getNextRowOrNextColumn} is called whenever ScanQueryMatcher  * believes that the current column should be skipped (by timestamp, filter etc.)</li>  *</ul>  *<p>  * These two methods returns a  * {@link org.apache.hadoop.hbase.regionserver.querymatcher.ScanQueryMatcher.MatchCode}  * to define what action should be taken.  *<p>  * This class is NOT thread-safe as queries are never multi-threaded  */
end_comment

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
interface|interface
name|ColumnTracker
extends|extends
name|ShipperListener
block|{
comment|/**    * Checks if the column is present in the list of requested columns by returning the match code    * instance. It does not check against the number of versions for the columns asked for. To do the    * version check, one has to call {@link #checkVersions(Cell, long, byte, boolean)}    * method based on the return type (INCLUDE) of this method. The values that can be returned by    * this method are {@link MatchCode#INCLUDE}, {@link MatchCode#SEEK_NEXT_COL} and    * {@link MatchCode#SEEK_NEXT_ROW}.    * @param cell    * @param type The type of the KeyValue    * @return The match code instance.    * @throws IOException in case there is an internal consistency problem caused by a data    *           corruption.    */
name|ScanQueryMatcher
operator|.
name|MatchCode
name|checkColumn
parameter_list|(
name|Cell
name|cell
parameter_list|,
name|byte
name|type
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Keeps track of the number of versions for the columns asked for. It assumes that the user has    * already checked if the keyvalue needs to be included by calling the    * {@link #checkColumn(Cell, byte)} method. The enum values returned by this method    * are {@link MatchCode#SKIP}, {@link MatchCode#INCLUDE},    * {@link MatchCode#INCLUDE_AND_SEEK_NEXT_COL} and {@link MatchCode#INCLUDE_AND_SEEK_NEXT_ROW}.    * Implementations which include all the columns could just return {@link MatchCode#INCLUDE} in    * the {@link #checkColumn(Cell, byte)} method and perform all the operations in this    * checkVersions method.    * @param cell    * @param timestamp The timestamp of the cell.    * @param type the type of the key value (Put/Delete)    * @param ignoreCount indicates if the KV needs to be excluded while counting (used during    *          compactions. We only count KV's that are older than all the scanners' read points.)    * @return the scan query matcher match code instance    * @throws IOException in case there is an internal consistency problem caused by a data    *           corruption.    */
name|ScanQueryMatcher
operator|.
name|MatchCode
name|checkVersions
parameter_list|(
name|Cell
name|cell
parameter_list|,
name|long
name|timestamp
parameter_list|,
name|byte
name|type
parameter_list|,
name|boolean
name|ignoreCount
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Resets the Matcher    */
name|void
name|reset
parameter_list|()
function_decl|;
comment|/**    *    * @return<code>true</code> when done.    */
name|boolean
name|done
parameter_list|()
function_decl|;
comment|/**    * Used by matcher and scan/get to get a hint of the next column    * to seek to after checkColumn() returns SKIP.  Returns the next interesting    * column we want, or NULL there is none (wildcard scanner).    *    * Implementations aren't required to return anything useful unless the most recent    * call was to checkColumn() and the return code was SKIP.  This is pretty implementation    * detail-y, but optimizations are like that.    *    * @return null, or a ColumnCount that we should seek to    */
name|ColumnCount
name|getColumnHint
parameter_list|()
function_decl|;
comment|/**    * Retrieve the MatchCode for the next row or column    * @param cell    */
name|MatchCode
name|getNextRowOrNextColumn
parameter_list|(
name|Cell
name|cell
parameter_list|)
function_decl|;
comment|/**    * Give the tracker a chance to declare it's done based on only the timestamp    * to allow an early out.    *    * @param timestamp    * @return<code>true</code> to early out based on timestamp.    */
name|boolean
name|isDone
parameter_list|(
name|long
name|timestamp
parameter_list|)
function_decl|;
block|}
end_interface

end_unit

