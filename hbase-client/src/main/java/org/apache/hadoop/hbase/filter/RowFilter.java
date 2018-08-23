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
name|ArrayList
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Objects
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
name|CompareOperator
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
name|exceptions
operator|.
name|DeserializationException
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
name|shaded
operator|.
name|protobuf
operator|.
name|ProtobufUtil
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
name|shaded
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|FilterProtos
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
name|thirdparty
operator|.
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|InvalidProtocolBufferException
import|;
end_import

begin_comment
comment|/**  * This filter is used to filter based on the key. It takes an operator  * (equal, greater, not equal, etc) and a byte [] comparator for the row,  * and column qualifier portions of a key.  *<p>  * This filter can be wrapped with {@link WhileMatchFilter} to add more control.  *<p>  * Multiple filters can be combined using {@link FilterList}.  *<p>  * If an already known row range needs to be scanned,   * use {@link org.apache.hadoop.hbase.CellScanner} start  * and stop rows directly rather than a filter.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Public
specifier|public
class|class
name|RowFilter
extends|extends
name|CompareFilter
block|{
specifier|private
name|boolean
name|filterOutRow
init|=
literal|false
decl_stmt|;
comment|/**    * Constructor.    * @param rowCompareOp the compare op for row matching    * @param rowComparator the comparator for row matching    * @deprecated Since 2.0.0. Will remove in 3.0.0. Use    * {@link #RowFilter(CompareOperator, ByteArrayComparable)}} instead.    */
annotation|@
name|Deprecated
specifier|public
name|RowFilter
parameter_list|(
specifier|final
name|CompareOp
name|rowCompareOp
parameter_list|,
specifier|final
name|ByteArrayComparable
name|rowComparator
parameter_list|)
block|{
name|super
argument_list|(
name|rowCompareOp
argument_list|,
name|rowComparator
argument_list|)
expr_stmt|;
block|}
comment|/**    * Constructor.    * @param op the compare op for row matching    * @param rowComparator the comparator for row matching    */
specifier|public
name|RowFilter
parameter_list|(
specifier|final
name|CompareOperator
name|op
parameter_list|,
specifier|final
name|ByteArrayComparable
name|rowComparator
parameter_list|)
block|{
name|super
argument_list|(
name|op
argument_list|,
name|rowComparator
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|reset
parameter_list|()
block|{
name|this
operator|.
name|filterOutRow
operator|=
literal|false
expr_stmt|;
block|}
annotation|@
name|Deprecated
annotation|@
name|Override
specifier|public
name|ReturnCode
name|filterKeyValue
parameter_list|(
specifier|final
name|Cell
name|c
parameter_list|)
block|{
return|return
name|filterCell
argument_list|(
name|c
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|ReturnCode
name|filterCell
parameter_list|(
specifier|final
name|Cell
name|v
parameter_list|)
block|{
if|if
condition|(
name|this
operator|.
name|filterOutRow
condition|)
block|{
return|return
name|ReturnCode
operator|.
name|NEXT_ROW
return|;
block|}
return|return
name|ReturnCode
operator|.
name|INCLUDE
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|filterRowKey
parameter_list|(
name|Cell
name|firstRowCell
parameter_list|)
block|{
if|if
condition|(
name|compareRow
argument_list|(
name|getCompareOperator
argument_list|()
argument_list|,
name|this
operator|.
name|comparator
argument_list|,
name|firstRowCell
argument_list|)
condition|)
block|{
name|this
operator|.
name|filterOutRow
operator|=
literal|true
expr_stmt|;
block|}
return|return
name|this
operator|.
name|filterOutRow
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|filterRow
parameter_list|()
block|{
return|return
name|this
operator|.
name|filterOutRow
return|;
block|}
specifier|public
specifier|static
name|Filter
name|createFilterFromArguments
parameter_list|(
name|ArrayList
argument_list|<
name|byte
index|[]
argument_list|>
name|filterArguments
parameter_list|)
block|{
annotation|@
name|SuppressWarnings
argument_list|(
literal|"rawtypes"
argument_list|)
comment|// for arguments
name|ArrayList
name|arguments
init|=
name|CompareFilter
operator|.
name|extractArguments
argument_list|(
name|filterArguments
argument_list|)
decl_stmt|;
name|CompareOperator
name|compareOp
init|=
operator|(
name|CompareOperator
operator|)
name|arguments
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|ByteArrayComparable
name|comparator
init|=
operator|(
name|ByteArrayComparable
operator|)
name|arguments
operator|.
name|get
argument_list|(
literal|1
argument_list|)
decl_stmt|;
return|return
operator|new
name|RowFilter
argument_list|(
name|compareOp
argument_list|,
name|comparator
argument_list|)
return|;
block|}
comment|/**   * @return The filter serialized using pb   */
annotation|@
name|Override
specifier|public
name|byte
index|[]
name|toByteArray
parameter_list|()
block|{
name|FilterProtos
operator|.
name|RowFilter
operator|.
name|Builder
name|builder
init|=
name|FilterProtos
operator|.
name|RowFilter
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
name|builder
operator|.
name|setCompareFilter
argument_list|(
name|super
operator|.
name|convert
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|builder
operator|.
name|build
argument_list|()
operator|.
name|toByteArray
argument_list|()
return|;
block|}
comment|/**    * @param pbBytes A pb serialized {@link RowFilter} instance    * @return An instance of {@link RowFilter} made from<code>bytes</code>    * @throws DeserializationException    * @see #toByteArray    */
specifier|public
specifier|static
name|RowFilter
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
name|FilterProtos
operator|.
name|RowFilter
name|proto
decl_stmt|;
try|try
block|{
name|proto
operator|=
name|FilterProtos
operator|.
name|RowFilter
operator|.
name|parseFrom
argument_list|(
name|pbBytes
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InvalidProtocolBufferException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|DeserializationException
argument_list|(
name|e
argument_list|)
throw|;
block|}
specifier|final
name|CompareOperator
name|valueCompareOp
init|=
name|CompareOperator
operator|.
name|valueOf
argument_list|(
name|proto
operator|.
name|getCompareFilter
argument_list|()
operator|.
name|getCompareOp
argument_list|()
operator|.
name|name
argument_list|()
argument_list|)
decl_stmt|;
name|ByteArrayComparable
name|valueComparator
init|=
literal|null
decl_stmt|;
try|try
block|{
if|if
condition|(
name|proto
operator|.
name|getCompareFilter
argument_list|()
operator|.
name|hasComparator
argument_list|()
condition|)
block|{
name|valueComparator
operator|=
name|ProtobufUtil
operator|.
name|toComparator
argument_list|(
name|proto
operator|.
name|getCompareFilter
argument_list|()
operator|.
name|getComparator
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|ioe
parameter_list|)
block|{
throw|throw
operator|new
name|DeserializationException
argument_list|(
name|ioe
argument_list|)
throw|;
block|}
return|return
operator|new
name|RowFilter
argument_list|(
name|valueCompareOp
argument_list|,
name|valueComparator
argument_list|)
return|;
block|}
comment|/**    * @return true if and only if the fields of the filter that are serialized    * are equal to the corresponding fields in other.  Used for testing.    */
annotation|@
name|Override
name|boolean
name|areSerializedFieldsEqual
parameter_list|(
name|Filter
name|o
parameter_list|)
block|{
if|if
condition|(
name|o
operator|==
name|this
condition|)
return|return
literal|true
return|;
if|if
condition|(
operator|!
operator|(
name|o
operator|instanceof
name|RowFilter
operator|)
condition|)
return|return
literal|false
return|;
return|return
name|super
operator|.
name|areSerializedFieldsEqual
argument_list|(
name|o
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|equals
parameter_list|(
name|Object
name|obj
parameter_list|)
block|{
if|if
condition|(
name|obj
operator|==
literal|null
operator|||
operator|(
operator|!
operator|(
name|obj
operator|instanceof
name|RowFilter
operator|)
operator|)
condition|)
block|{
return|return
literal|false
return|;
block|}
name|RowFilter
name|f
init|=
operator|(
name|RowFilter
operator|)
name|obj
decl_stmt|;
return|return
name|this
operator|.
name|areSerializedFieldsEqual
argument_list|(
name|f
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
return|return
name|Objects
operator|.
name|hash
argument_list|(
name|this
operator|.
name|getComparator
argument_list|()
argument_list|,
name|this
operator|.
name|getCompareOperator
argument_list|()
argument_list|)
return|;
block|}
block|}
end_class

end_unit

