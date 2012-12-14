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
name|hbase
operator|.
name|cell
operator|.
name|CellTool
import|;
end_import

begin_comment
comment|/**  * The unit of storage in HBase consisting of the following fields:<br/>  *<pre>  * 1) row  * 2) column family  * 3) column qualifier  * 4) timestamp  * 5) type  * 6) MVCC version  * 7) value  *</pre>  *<p/>  * Uniqueness is determined by the combination of row, column family, column qualifier,  * timestamp, and type.  *<p/>  * The natural comparator will perform a bitwise comparison on row, column family, and column  * qualifier. Less intuitively, it will then treat the greater timestamp as the lesser value with  * the goal of sorting newer cells first.  *<p/>  * This interface does not include methods that allocate new byte[]'s such as those used in client  * or debugging code. These should be placed in a sub-interface or the {@link CellTool} class.  *<p/>  * Cell implements Comparable<Cell> which is only meaningful when comparing to other keys in the  * same table. It uses CellComparator which does not work on the -ROOT- and .META. tables.  *<p/>  * In the future, we may consider adding a boolean isOnHeap() method and a getValueBuffer() method  * that can be used to pass a value directly from an off-heap ByteBuffer to the network without  * copying into an on-heap byte[].  *<p/>  * Historic note: the original Cell implementation (KeyValue) requires that all fields be encoded as  * consecutive bytes in the same byte[], whereas this interface allows fields to reside in separate  * byte[]'s.  *<p/>  */
end_comment

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|Private
annotation|@
name|InterfaceStability
operator|.
name|Evolving
specifier|public
interface|interface
name|Cell
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
comment|/**    * @return Number of row bytes. Must be< rowArray.length - offset.    */
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
comment|/**    * @return Array index of first row byte    */
name|int
name|getFamilyOffset
parameter_list|()
function_decl|;
comment|/**    * @return Number of family bytes.  Must be< familyArray.length - offset.    */
name|byte
name|getFamilyLength
parameter_list|()
function_decl|;
comment|//3) Qualifier
comment|/**    * Contiguous raw bytes that may start at any index in the containing array. Max length is    * Short.MAX_VALUE which is 32,767 bytes.    * @return The array containing the qualifier bytes.    */
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
comment|/**    * @return Number of qualifier bytes.  Must be< qualifierArray.length - offset.    */
name|int
name|getQualifierLength
parameter_list|()
function_decl|;
comment|//4) Timestamp
comment|/**    * @return Long value representing time at which this cell was "Put" into the row.  Typically    * represents the time of insertion, but can be any value from Long.MIN_VALUE to Long.MAX_VALUE.    */
name|long
name|getTimestamp
parameter_list|()
function_decl|;
comment|//5) Type
comment|/**    * @return The byte representation of the KeyValue.TYPE of this cell: one of Put, Delete, etc    */
name|byte
name|getTypeByte
parameter_list|()
function_decl|;
comment|//6) MvccVersion
comment|/**    * Internal use only. A region-specific sequence ID given to each operation. It always exists for    * cells in the memstore but is not retained forever. It may survive several flushes, but    * generally becomes irrelevant after the cell's row is no longer involved in any operations that    * require strict consistency.    * @return mvccVersion (always>= 0 if exists), or 0 if it no longer exists    */
name|long
name|getMvccVersion
parameter_list|()
function_decl|;
comment|//7) Value
comment|/**    * Contiguous raw bytes that may start at any index in the containing array. Max length is    * Integer.MAX_VALUE which is 2,147,483,648 bytes.    * @return The array containing the value bytes.    */
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
comment|/**    * @return Number of value bytes.  Must be< valueArray.length - offset.    */
name|int
name|getValueLength
parameter_list|()
function_decl|;
block|}
end_interface

end_unit

