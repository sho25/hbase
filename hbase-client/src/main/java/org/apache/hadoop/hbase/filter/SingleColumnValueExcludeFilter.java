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
name|Iterator
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
name|CellUtil
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
comment|/**  * A {@link Filter} that checks a single column value, but does not emit the  * tested column. This will enable a performance boost over  * {@link SingleColumnValueFilter}, if the tested column value is not actually  * needed as input (besides for the filtering itself).  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Public
specifier|public
class|class
name|SingleColumnValueExcludeFilter
extends|extends
name|SingleColumnValueFilter
block|{
comment|/**    * Constructor for binary compare of the value of a single column. If the    * column is found and the condition passes, all columns of the row will be    * emitted; except for the tested column value. If the column is not found or    * the condition fails, the row will not be emitted.    *    * @param family name of column family    * @param qualifier name of column qualifier    * @param op operator    * @param value value to compare column values against    */
specifier|public
name|SingleColumnValueExcludeFilter
parameter_list|(
name|byte
index|[]
name|family
parameter_list|,
name|byte
index|[]
name|qualifier
parameter_list|,
name|CompareOperator
name|op
parameter_list|,
name|byte
index|[]
name|value
parameter_list|)
block|{
name|super
argument_list|(
name|family
argument_list|,
name|qualifier
argument_list|,
name|op
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
comment|/**    * Constructor for binary compare of the value of a single column. If the    * column is found and the condition passes, all columns of the row will be    * emitted; except for the tested column value. If the condition fails, the    * row will not be emitted.    *<p>    * Use the filterIfColumnMissing flag to set whether the rest of the columns    * in a row will be emitted if the specified column to check is not found in    * the row.    *    * @param family name of column family    * @param qualifier name of column qualifier    * @param op operator    * @param comparator Comparator to use.    */
specifier|public
name|SingleColumnValueExcludeFilter
parameter_list|(
name|byte
index|[]
name|family
parameter_list|,
name|byte
index|[]
name|qualifier
parameter_list|,
name|CompareOperator
name|op
parameter_list|,
name|ByteArrayComparable
name|comparator
parameter_list|)
block|{
name|super
argument_list|(
name|family
argument_list|,
name|qualifier
argument_list|,
name|op
argument_list|,
name|comparator
argument_list|)
expr_stmt|;
block|}
comment|/**    * Constructor for protobuf deserialization only.    * @param family    * @param qualifier    * @param op    * @param comparator    * @param filterIfMissing    * @param latestVersionOnly    */
specifier|protected
name|SingleColumnValueExcludeFilter
parameter_list|(
specifier|final
name|byte
index|[]
name|family
parameter_list|,
specifier|final
name|byte
index|[]
name|qualifier
parameter_list|,
specifier|final
name|CompareOperator
name|op
parameter_list|,
name|ByteArrayComparable
name|comparator
parameter_list|,
specifier|final
name|boolean
name|filterIfMissing
parameter_list|,
specifier|final
name|boolean
name|latestVersionOnly
parameter_list|)
block|{
name|super
argument_list|(
name|family
argument_list|,
name|qualifier
argument_list|,
name|op
argument_list|,
name|comparator
argument_list|,
name|filterIfMissing
argument_list|,
name|latestVersionOnly
argument_list|)
expr_stmt|;
block|}
comment|// We cleaned result row in FilterRow to be consistent with scanning process.
annotation|@
name|Override
specifier|public
name|boolean
name|hasFilterRow
parameter_list|()
block|{
return|return
literal|true
return|;
block|}
comment|// Here we remove from row all key values from testing column
annotation|@
name|Override
specifier|public
name|void
name|filterRowCells
parameter_list|(
name|List
argument_list|<
name|Cell
argument_list|>
name|kvs
parameter_list|)
block|{
name|Iterator
argument_list|<
name|?
extends|extends
name|Cell
argument_list|>
name|it
init|=
name|kvs
operator|.
name|iterator
argument_list|()
decl_stmt|;
while|while
condition|(
name|it
operator|.
name|hasNext
argument_list|()
condition|)
block|{
comment|// If the current column is actually the tested column,
comment|// we will skip it instead.
if|if
condition|(
name|CellUtil
operator|.
name|matchingColumn
argument_list|(
name|it
operator|.
name|next
argument_list|()
argument_list|,
name|this
operator|.
name|columnFamily
argument_list|,
name|this
operator|.
name|columnQualifier
argument_list|)
condition|)
block|{
name|it
operator|.
name|remove
argument_list|()
expr_stmt|;
block|}
block|}
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
name|SingleColumnValueFilter
name|tempFilter
init|=
operator|(
name|SingleColumnValueFilter
operator|)
name|SingleColumnValueFilter
operator|.
name|createFilterFromArguments
argument_list|(
name|filterArguments
argument_list|)
decl_stmt|;
name|SingleColumnValueExcludeFilter
name|filter
init|=
operator|new
name|SingleColumnValueExcludeFilter
argument_list|(
name|tempFilter
operator|.
name|getFamily
argument_list|()
argument_list|,
name|tempFilter
operator|.
name|getQualifier
argument_list|()
argument_list|,
name|tempFilter
operator|.
name|getCompareOperator
argument_list|()
argument_list|,
name|tempFilter
operator|.
name|getComparator
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|filterArguments
operator|.
name|size
argument_list|()
operator|==
literal|6
condition|)
block|{
name|filter
operator|.
name|setFilterIfMissing
argument_list|(
name|tempFilter
operator|.
name|getFilterIfMissing
argument_list|()
argument_list|)
expr_stmt|;
name|filter
operator|.
name|setLatestVersionOnly
argument_list|(
name|tempFilter
operator|.
name|getLatestVersionOnly
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|filter
return|;
block|}
comment|/**    * @return The filter serialized using pb    */
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
name|SingleColumnValueExcludeFilter
operator|.
name|Builder
name|builder
init|=
name|FilterProtos
operator|.
name|SingleColumnValueExcludeFilter
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
name|builder
operator|.
name|setSingleColumnValueFilter
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
comment|/**    * @param pbBytes A pb serialized {@link SingleColumnValueExcludeFilter} instance    * @return An instance of {@link SingleColumnValueExcludeFilter} made from<code>bytes</code>    * @throws DeserializationException    * @see #toByteArray    */
specifier|public
specifier|static
name|SingleColumnValueExcludeFilter
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
name|SingleColumnValueExcludeFilter
name|proto
decl_stmt|;
try|try
block|{
name|proto
operator|=
name|FilterProtos
operator|.
name|SingleColumnValueExcludeFilter
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
name|FilterProtos
operator|.
name|SingleColumnValueFilter
name|parentProto
init|=
name|proto
operator|.
name|getSingleColumnValueFilter
argument_list|()
decl_stmt|;
specifier|final
name|CompareOperator
name|compareOp
init|=
name|CompareOperator
operator|.
name|valueOf
argument_list|(
name|parentProto
operator|.
name|getCompareOp
argument_list|()
operator|.
name|name
argument_list|()
argument_list|)
decl_stmt|;
specifier|final
name|ByteArrayComparable
name|comparator
decl_stmt|;
try|try
block|{
name|comparator
operator|=
name|ProtobufUtil
operator|.
name|toComparator
argument_list|(
name|parentProto
operator|.
name|getComparator
argument_list|()
argument_list|)
expr_stmt|;
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
name|SingleColumnValueExcludeFilter
argument_list|(
name|parentProto
operator|.
name|hasColumnFamily
argument_list|()
condition|?
name|parentProto
operator|.
name|getColumnFamily
argument_list|()
operator|.
name|toByteArray
argument_list|()
else|:
literal|null
argument_list|,
name|parentProto
operator|.
name|hasColumnQualifier
argument_list|()
condition|?
name|parentProto
operator|.
name|getColumnQualifier
argument_list|()
operator|.
name|toByteArray
argument_list|()
else|:
literal|null
argument_list|,
name|compareOp
argument_list|,
name|comparator
argument_list|,
name|parentProto
operator|.
name|getFilterIfMissing
argument_list|()
argument_list|,
name|parentProto
operator|.
name|getLatestVersionOnly
argument_list|()
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
name|SingleColumnValueExcludeFilter
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
return|return
name|obj
operator|instanceof
name|Filter
operator|&&
name|areSerializedFieldsEqual
argument_list|(
operator|(
name|Filter
operator|)
name|obj
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
name|super
operator|.
name|hashCode
argument_list|()
return|;
block|}
block|}
end_class

end_unit

