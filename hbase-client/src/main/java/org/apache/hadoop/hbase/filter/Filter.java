begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|exceptions
operator|.
name|DeserializationException
import|;
end_import

begin_comment
comment|/**  * Interface for row and column filters directly applied within the regionserver.  * A filter can expect the following call sequence:  *<ul>  *<li>{@link #reset()}</li>  *<li>{@link #filterAllRemaining()} -> true indicates scan is over, false, keep going on.</li>  *<li>{@link #filterRowKey(byte[],int,int)} -> true to drop this row,  * if false, we will also call</li>  *<li>{@link #filterKeyValue(KeyValue)} -> true to drop this key/value</li>  *<li>{@link #filterRow(List)} -> allows directmodification of the final list to be submitted  *<li>{@link #filterRow()} -> last chance to drop entire row based on the sequence of  * filterValue() calls. Eg: filter a row if it doesn't contain a specified column.  *</li>  *</ul>  *  * Filter instances are created one per region/scan.  This abstract class replaces  * the old RowFilterInterface.  *  * When implementing your own filters, consider inheriting {@link FilterBase} to help  * you reduce boilerplate.  *   * @see FilterBase  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Public
annotation|@
name|InterfaceStability
operator|.
name|Stable
specifier|public
specifier|abstract
class|class
name|Filter
block|{
comment|/**    * Reset the state of the filter between rows.    *     * Concrete implementers can signal a failure condition in their code by throwing an    * {@link IOException}.    *     * @throws IOException in case an I/O or an filter specific failure needs to be signaled.    */
specifier|abstract
specifier|public
name|void
name|reset
parameter_list|()
throws|throws
name|IOException
function_decl|;
comment|/**    * Filters a row based on the row key. If this returns true, the entire row will be excluded. If    * false, each KeyValue in the row will be passed to {@link #filterKeyValue(KeyValue)} below.    *     * Concrete implementers can signal a failure condition in their code by throwing an    * {@link IOException}.    *     * @param buffer buffer containing row key    * @param offset offset into buffer where row key starts    * @param length length of the row key    * @return true, remove entire row, false, include the row (maybe).    * @throws IOException in case an I/O or an filter specific failure needs to be signaled.    */
specifier|abstract
specifier|public
name|boolean
name|filterRowKey
parameter_list|(
name|byte
index|[]
name|buffer
parameter_list|,
name|int
name|offset
parameter_list|,
name|int
name|length
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * If this returns true, the scan will terminate.    *     * Concrete implementers can signal a failure condition in their code by throwing an    * {@link IOException}.    *     * @return true to end scan, false to continue.    * @throws IOException in case an I/O or an filter specific failure needs to be signaled.    */
specifier|abstract
specifier|public
name|boolean
name|filterAllRemaining
parameter_list|()
throws|throws
name|IOException
function_decl|;
comment|/**    * A way to filter based on the column family, column qualifier and/or the column value. Return    * code is described below. This allows filters to filter only certain number of columns, then    * terminate without matching ever column.    *     * If your filter returns<code>ReturnCode.NEXT_ROW</code>, it should return    *<code>ReturnCode.NEXT_ROW</code> until {@link #reset()} is called just in case the caller calls    * for the next row.    *     * Concrete implementers can signal a failure condition in their code by throwing an    * {@link IOException}.    *     * @param v the KeyValue in question    * @return code as described below    * @throws IOException in case an I/O or an filter specific failure needs to be signaled.    * @see Filter.ReturnCode    */
specifier|abstract
specifier|public
name|ReturnCode
name|filterKeyValue
parameter_list|(
specifier|final
name|KeyValue
name|v
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Give the filter a chance to transform the passed KeyValue. If the KeyValue is changed a new    * KeyValue object must be returned.    *     * @see org.apache.hadoop.hbase.KeyValue#shallowCopy()    *      The transformed KeyValue is what is eventually returned to the client. Most filters will    *      return the passed KeyValue unchanged.    * @see org.apache.hadoop.hbase.filter.KeyOnlyFilter#transform(KeyValue) for an example of a    *      transformation.    *     *      Concrete implementers can signal a failure condition in their code by throwing an    *      {@link IOException}.    *     * @param v the KeyValue in question    * @return the changed KeyValue    * @throws IOException in case an I/O or an filter specific failure needs to be signaled.    */
specifier|abstract
specifier|public
name|KeyValue
name|transform
parameter_list|(
specifier|final
name|KeyValue
name|v
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Return codes for filterValue().    */
specifier|public
enum|enum
name|ReturnCode
block|{
comment|/**      * Include the KeyValue      */
name|INCLUDE
block|,
comment|/**      * Include the KeyValue and seek to the next column skipping older versions.      */
name|INCLUDE_AND_NEXT_COL
block|,
comment|/**      * Skip this KeyValue      */
name|SKIP
block|,
comment|/**      * Skip this column. Go to the next column in this row.      */
name|NEXT_COL
block|,
comment|/**      * Done with columns, skip to next row. Note that filterRow() will      * still be called.      */
name|NEXT_ROW
block|,
comment|/**      * Seek to next key which is given as hint by the filter.      */
name|SEEK_NEXT_USING_HINT
block|, }
comment|/**    * Chance to alter the list of keyvalues to be submitted. Modifications to the list will carry on    *     * Concrete implementers can signal a failure condition in their code by throwing an    * {@link IOException}.    *     * @param kvs the list of keyvalues to be filtered    * @throws IOException in case an I/O or an filter specific failure needs to be signaled.    */
specifier|abstract
specifier|public
name|void
name|filterRow
parameter_list|(
name|List
argument_list|<
name|KeyValue
argument_list|>
name|kvs
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Primarily used to check for conflicts with scans(such as scans that do not read a full row at a    * time).    *     * @return True if this filter actively uses filterRow(List) or filterRow().    */
specifier|abstract
specifier|public
name|boolean
name|hasFilterRow
parameter_list|()
function_decl|;
comment|/**    * Last chance to veto row based on previous {@link #filterKeyValue(KeyValue)} calls. The filter    * needs to retain state then return a particular value for this call if they wish to exclude a    * row if a certain column is missing (for example).    *     * Concrete implementers can signal a failure condition in their code by throwing an    * {@link IOException}.    *     * @return true to exclude row, false to include row.    * @throws IOException in case an I/O or an filter specific failure needs to be signaled.    */
specifier|abstract
specifier|public
name|boolean
name|filterRow
parameter_list|()
throws|throws
name|IOException
function_decl|;
comment|/**    * If the filter returns the match code SEEK_NEXT_USING_HINT, then it should also tell which is    * the next key it must seek to. After receiving the match code SEEK_NEXT_USING_HINT, the    * QueryMatcher would call this function to find out which key it must next seek to.    *     * Concrete implementers can signal a failure condition in their code by throwing an    * {@link IOException}.    *     * @return KeyValue which must be next seeked. return null if the filter is not sure which key to    *         seek to next.    * @throws IOException in case an I/O or an filter specific failure needs to be signaled.    */
specifier|abstract
specifier|public
name|KeyValue
name|getNextKeyHint
parameter_list|(
specifier|final
name|KeyValue
name|currentKV
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Check that given column family is essential for filter to check row. Most filters always return    * true here. But some could have more sophisticated logic which could significantly reduce    * scanning process by not even touching columns until we are 100% sure that it's data is needed    * in result.    *     * Concrete implementers can signal a failure condition in their code by throwing an    * {@link IOException}.    *     * @throws IOException in case an I/O or an filter specific failure needs to be signaled.    */
specifier|abstract
specifier|public
name|boolean
name|isFamilyEssential
parameter_list|(
name|byte
index|[]
name|name
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * TODO: JAVADOC    *     * Concrete implementers can signal a failure condition in their code by throwing an    * {@link IOException}.    *     * @return The filter serialized using pb    * @throws IOException in case an I/O or an filter specific failure needs to be signaled.    */
specifier|abstract
specifier|public
name|byte
index|[]
name|toByteArray
parameter_list|()
throws|throws
name|IOException
function_decl|;
comment|/**    *     * Concrete implementers can signal a failure condition in their code by throwing an    * {@link IOException}.    *     * @param pbBytes A pb serialized {@link Filter} instance    * @return An instance of {@link Filter} made from<code>bytes</code>    * @throws DeserializationException    * @throws IOException in case an I/O or an filter specific failure needs to be signaled.    * @see #toByteArray    */
specifier|public
specifier|static
name|Filter
name|parseFrom
parameter_list|(
specifier|final
name|byte
index|[]
name|pbBytes
parameter_list|)
throws|throws
name|DeserializationException
block|{
throw|throw
operator|new
name|DeserializationException
argument_list|(
literal|"parseFrom called on base Filter, but should be called on derived type"
argument_list|)
throw|;
block|}
comment|/**    * Concrete implementers can signal a failure condition in their code by throwing an    * {@link IOException}.    *     * @param other    * @return true if and only if the fields of the filter that are serialized are equal to the    *         corresponding fields in other. Used for testing.    * @throws IOException in case an I/O or an filter specific failure needs to be signaled.    */
specifier|abstract
name|boolean
name|areSerializedFieldsEqual
parameter_list|(
name|Filter
name|other
parameter_list|)
function_decl|;
block|}
end_class

end_unit

