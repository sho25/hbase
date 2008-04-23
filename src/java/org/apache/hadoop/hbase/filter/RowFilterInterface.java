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
name|filter
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|SortedMap
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
name|io
operator|.
name|Writable
import|;
end_import

begin_comment
comment|/**  *   * Interface used for row-level filters applied to HRegion.HScanner scan  * results during calls to next().  */
end_comment

begin_interface
specifier|public
interface|interface
name|RowFilterInterface
extends|extends
name|Writable
block|{
comment|/**    * Resets the state of the filter. Used prior to the start of a Region scan.    *     */
name|void
name|reset
parameter_list|()
function_decl|;
comment|/**    * Called to let filter know the final decision (to pass or filter) on a     * given row.  With out HScanner calling this, the filter does not know if a     * row passed filtering even if it passed the row itself because other     * filters may have failed the row. E.g. when this filter is a member of a     * RowFilterSet with an OR operator.    *     * @see RowFilterSet    * @param filtered    * @param key    */
name|void
name|rowProcessed
parameter_list|(
name|boolean
name|filtered
parameter_list|,
name|Text
name|key
parameter_list|)
function_decl|;
comment|/**    * Returns whether or not the filter should always be processed in any     * filtering call.  This precaution is necessary for filters that maintain     * state and need to be updated according to their response to filtering     * calls (see WhileMatchRowFilter for an example).  At times, filters nested     * in RowFilterSets may or may not be called because the RowFilterSet     * determines a result as fast as possible.  Returning true for     * processAlways() ensures that the filter will always be called.    *     * @return whether or not to always process the filter    */
name|boolean
name|processAlways
parameter_list|()
function_decl|;
comment|/**    * Determines if the filter has decided that all remaining results should be    * filtered (skipped). This is used to prevent the scanner from scanning a    * the rest of the HRegion when for sure the filter will exclude all    * remaining rows.    *     * @return true if the filter intends to filter all remaining rows.    */
name|boolean
name|filterAllRemaining
parameter_list|()
function_decl|;
comment|/**    * Filters on just a row key. This is the first chance to stop a row.    *     * @param rowKey    * @return true if given row key is filtered and row should not be processed.    */
name|boolean
name|filterRowKey
parameter_list|(
specifier|final
name|Text
name|rowKey
parameter_list|)
function_decl|;
comment|/**    * Filters on row key, column name, and column value. This will take individual columns out of a row,     * but the rest of the row will still get through.    *     * @param rowKey row key to filter on.    * @param colunmName column name to filter on    * @param columnValue column value to filter on    * @return true if row filtered and should not be processed.    */
name|boolean
name|filterColumn
parameter_list|(
specifier|final
name|Text
name|rowKey
parameter_list|,
specifier|final
name|Text
name|colunmName
parameter_list|,
specifier|final
name|byte
index|[]
name|columnValue
parameter_list|)
function_decl|;
comment|/**    * Filter on the fully assembled row. This is the last chance to stop a row.     *     * @param columns    * @return true if row filtered and should not be processed.    */
name|boolean
name|filterRow
parameter_list|(
specifier|final
name|SortedMap
argument_list|<
name|Text
argument_list|,
name|byte
index|[]
argument_list|>
name|columns
parameter_list|)
function_decl|;
comment|/**    * Validates that this filter applies only to a subset of the given columns.    * This check is done prior to opening of scanner due to the limitation that    * filtering of columns is dependent on the retrieval of those columns within    * the HRegion. Criteria on columns that are not part of a scanner's column    * list will be ignored. In the case of null value filters, all rows will pass    * the filter. This behavior should be 'undefined' for the user and therefore    * not permitted.    *     * @param columns    */
name|void
name|validate
parameter_list|(
specifier|final
name|Text
index|[]
name|columns
parameter_list|)
function_decl|;
block|}
end_interface

end_unit

