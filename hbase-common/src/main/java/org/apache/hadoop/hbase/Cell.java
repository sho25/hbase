begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|HeapSize
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

begin_comment
comment|/**  * The unit of storage in HBase consisting of the following fields:  *<br>  *<pre>  * 1) row  * 2) column family  * 3) column qualifier  * 4) timestamp  * 5) type  * 6) MVCC version  * 7) value  *</pre>  *<p>  * Uniqueness is determined by the combination of row, column family, column qualifier,  * timestamp, and type.  *</p>  *<p>  * The natural comparator will perform a bitwise comparison on row, column family, and column  * qualifier. Less intuitively, it will then treat the greater timestamp as the lesser value with  * the goal of sorting newer cells first.  *</p>  *<p>  * Cell implements Comparable&lt;Cell&gt; which is only meaningful when  * comparing to other keys in the  * same table. It uses CellComparator which does not work on the -ROOT- and hbase:meta tables.  *</p>  *<p>  * In the future, we may consider adding a boolean isOnHeap() method and a getValueBuffer() method  * that can be used to pass a value directly from an off-heap ByteBuffer to the network without  * copying into an on-heap byte[].  *</p>  *<p>  * Historic note: the original Cell implementation (KeyValue) requires that all fields be encoded as  * consecutive bytes in the same byte[], whereas this interface allows fields to reside in separate  * byte[]'s.  *</p>  */
end_comment

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|Public
specifier|public
interface|interface
name|Cell
extends|extends
name|HeapSize
block|{
comment|//1) Row
comment|/**    * Contiguous raw bytes that may start at any index in the containing array. Max length is    * Short.MAX_VALUE which is 32,767 bytes.    * @return The array containing the row bytes.    */
name|byte
index|[]
name|getRowArray
parameter_list|()
function_decl|;
comment|/**    * @return Array index of first row byte    */
name|int
name|getRowOffset
parameter_list|()
function_decl|;
comment|/**    * @return Number of row bytes. Must be&lt; rowArray.length - offset.    */
name|short
name|getRowLength
parameter_list|()
function_decl|;
comment|//2) Family
comment|/**    * Contiguous bytes composed of legal HDFS filename characters which may start at any index in the    * containing array. Max length is Byte.MAX_VALUE, which is 127 bytes.    * @return the array containing the family bytes.    */
name|byte
index|[]
name|getFamilyArray
parameter_list|()
function_decl|;
comment|/**    * @return Array index of first family byte    */
name|int
name|getFamilyOffset
parameter_list|()
function_decl|;
comment|/**    * @return Number of family bytes.  Must be&lt; familyArray.length - offset.    */
name|byte
name|getFamilyLength
parameter_list|()
function_decl|;
comment|//3) Qualifier
comment|/**    * Contiguous raw bytes that may start at any index in the containing array.    * @return The array containing the qualifier bytes.    */
name|byte
index|[]
name|getQualifierArray
parameter_list|()
function_decl|;
comment|/**    * @return Array index of first qualifier byte    */
name|int
name|getQualifierOffset
parameter_list|()
function_decl|;
comment|/**    * @return Number of qualifier bytes.  Must be&lt; qualifierArray.length - offset.    */
name|int
name|getQualifierLength
parameter_list|()
function_decl|;
comment|//4) Timestamp
comment|/**    * @return Long value representing time at which this cell was "Put" into the row.  Typically    * represents the time of insertion, but can be any value from 0 to Long.MAX_VALUE.    */
name|long
name|getTimestamp
parameter_list|()
function_decl|;
comment|//5) Type
comment|/**    * @return The byte representation of the KeyValue.TYPE of this cell: one of Put, Delete, etc    * @deprecated As of HBase-2.0. Will be removed in HBase-3.0. Use {@link #getType()}.    */
annotation|@
name|Deprecated
name|byte
name|getTypeByte
parameter_list|()
function_decl|;
comment|//6) SequenceId
comment|/**    * A region-specific unique monotonically increasing sequence ID given to each Cell. It always    * exists for cells in the memstore but is not retained forever. It will be kept for    * {@link HConstants#KEEP_SEQID_PERIOD} days, but generally becomes irrelevant after the cell's    * row is no longer involved in any operations that require strict consistency.    * @return seqId (always&gt; 0 if exists), or 0 if it no longer exists    * @deprecated As of HBase-2.0. Will be removed in HBase-3.0.    */
annotation|@
name|Deprecated
name|long
name|getSequenceId
parameter_list|()
function_decl|;
comment|//7) Value
comment|/**    * Contiguous raw bytes that may start at any index in the containing array. Max length is    * Integer.MAX_VALUE which is 2,147,483,647 bytes.    * @return The array containing the value bytes.    */
name|byte
index|[]
name|getValueArray
parameter_list|()
function_decl|;
comment|/**    * @return Array index of first value byte    */
name|int
name|getValueOffset
parameter_list|()
function_decl|;
comment|/**    * @return Number of value bytes.  Must be&lt; valueArray.length - offset.    */
name|int
name|getValueLength
parameter_list|()
function_decl|;
comment|/**    * @return Serialized size (defaults to include tag length if has some tags).    */
name|int
name|getSerializedSize
parameter_list|()
function_decl|;
comment|/**    * Contiguous raw bytes representing tags that may start at any index in the containing array.    * @return the tags byte array    * @deprecated As of HBase-2.0. Will be removed in HBase-3.0. Tags are are now internal.    */
annotation|@
name|Deprecated
name|byte
index|[]
name|getTagsArray
parameter_list|()
function_decl|;
comment|/**    * @return the first offset where the tags start in the Cell    * @deprecated As of HBase-2.0. Will be removed in HBase-3.0. Tags are are now internal.    */
annotation|@
name|Deprecated
name|int
name|getTagsOffset
parameter_list|()
function_decl|;
comment|/**    * HBase internally uses 2 bytes to store tags length in Cell.    * As the tags length is always a non-negative number, to make good use of the sign bit,    * the max of tags length is defined 2 * Short.MAX_VALUE + 1 = 65535.    * As a result, the return type is int, because a short is not capable of handling that.    * Please note that even if the return type is int, the max tags length is far    * less than Integer.MAX_VALUE.    *    * @return the total length of the tags in the Cell.    * @deprecated As of HBase-2.0. Will be removed in HBase-3.0. Tags are are now internal.    */
annotation|@
name|Deprecated
name|int
name|getTagsLength
parameter_list|()
function_decl|;
comment|/**    * Returns the type of cell in a human readable format using {@link Type}.    * Note : This does not expose the internal types of Cells like {@link KeyValue.Type#Maximum} and    * {@link KeyValue.Type#Minimum}    * @return The data type this cell: one of Put, Delete, etc    */
specifier|default
name|Type
name|getType
parameter_list|()
block|{
name|byte
name|byteType
init|=
name|getTypeByte
argument_list|()
decl_stmt|;
name|Type
name|t
init|=
name|Type
operator|.
name|CODE_ARRAY
index|[
name|byteType
operator|&
literal|0xff
index|]
decl_stmt|;
if|if
condition|(
name|t
operator|!=
literal|null
condition|)
block|{
return|return
name|t
return|;
block|}
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"Invalid type of cell "
operator|+
name|byteType
argument_list|)
throw|;
block|}
comment|/**    * The valid types for user to build the cell. Currently, This is subset of {@link KeyValue.Type}.    */
enum|enum
name|Type
block|{
name|Put
argument_list|(
operator|(
name|byte
operator|)
literal|4
argument_list|)
block|,
name|Delete
argument_list|(
operator|(
name|byte
operator|)
literal|8
argument_list|)
block|,
name|DeleteFamilyVersion
argument_list|(
operator|(
name|byte
operator|)
literal|10
argument_list|)
block|,
name|DeleteColumn
argument_list|(
operator|(
name|byte
operator|)
literal|12
argument_list|)
block|,
name|DeleteFamily
argument_list|(
operator|(
name|byte
operator|)
literal|14
argument_list|)
block|;
specifier|private
specifier|final
name|byte
name|code
decl_stmt|;
name|Type
parameter_list|(
specifier|final
name|byte
name|c
parameter_list|)
block|{
name|this
operator|.
name|code
operator|=
name|c
expr_stmt|;
block|}
specifier|public
name|byte
name|getCode
parameter_list|()
block|{
return|return
name|this
operator|.
name|code
return|;
block|}
specifier|private
specifier|static
specifier|final
name|Type
index|[]
name|CODE_ARRAY
init|=
operator|new
name|Type
index|[
literal|256
index|]
decl_stmt|;
static|static
block|{
for|for
control|(
name|Type
name|t
range|:
name|Type
operator|.
name|values
argument_list|()
control|)
block|{
name|CODE_ARRAY
index|[
name|t
operator|.
name|code
operator|&
literal|0xff
index|]
operator|=
name|t
expr_stmt|;
block|}
block|}
block|}
block|}
end_interface

end_unit

