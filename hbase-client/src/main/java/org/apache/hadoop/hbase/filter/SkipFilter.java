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
comment|/**  * A wrapper filter that filters an entire row if any of the Cell checks do  * not pass.  *<p>  * For example, if all columns in a row represent weights of different things,  * with the values being the actual weights, and we want to filter out the  * entire row if any of its weights are zero.  In this case, we want to prevent  * rows from being emitted if a single key is filtered.  Combine this filter  * with a {@link ValueFilter}:  *</p>  *<p>  *<code>  * scan.setFilter(new SkipFilter(new ValueFilter(CompareOp.NOT_EQUAL,  *     new BinaryComparator(Bytes.toBytes(0))));  *</code>  * Any row which contained a column whose value was 0 will be filtered out  * (since ValueFilter will not pass that Cell).  * Without this filter, the other non-zero valued columns in the row would still  * be emitted.  *</p>  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Public
specifier|public
class|class
name|SkipFilter
extends|extends
name|FilterBase
block|{
specifier|private
name|boolean
name|filterRow
init|=
literal|false
decl_stmt|;
specifier|private
name|Filter
name|filter
decl_stmt|;
specifier|public
name|SkipFilter
parameter_list|(
name|Filter
name|filter
parameter_list|)
block|{
name|this
operator|.
name|filter
operator|=
name|filter
expr_stmt|;
block|}
specifier|public
name|Filter
name|getFilter
parameter_list|()
block|{
return|return
name|filter
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|reset
parameter_list|()
throws|throws
name|IOException
block|{
name|filter
operator|.
name|reset
argument_list|()
expr_stmt|;
name|filterRow
operator|=
literal|false
expr_stmt|;
block|}
specifier|private
name|void
name|changeFR
parameter_list|(
name|boolean
name|value
parameter_list|)
block|{
name|filterRow
operator|=
name|filterRow
operator|||
name|value
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|filterRowKey
parameter_list|(
name|Cell
name|cell
parameter_list|)
throws|throws
name|IOException
block|{
comment|// Impl in FilterBase might do unnecessary copy for Off heap backed Cells.
return|return
literal|false
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
name|c
parameter_list|)
throws|throws
name|IOException
block|{
name|ReturnCode
name|rc
init|=
name|filter
operator|.
name|filterCell
argument_list|(
name|c
argument_list|)
decl_stmt|;
name|changeFR
argument_list|(
name|rc
operator|!=
name|ReturnCode
operator|.
name|INCLUDE
argument_list|)
expr_stmt|;
return|return
name|rc
return|;
block|}
annotation|@
name|Override
specifier|public
name|Cell
name|transformCell
parameter_list|(
name|Cell
name|v
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|filter
operator|.
name|transformCell
argument_list|(
name|v
argument_list|)
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
name|filterRow
return|;
block|}
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
comment|/**    * @return The filter serialized using pb    */
annotation|@
name|Override
specifier|public
name|byte
index|[]
name|toByteArray
parameter_list|()
throws|throws
name|IOException
block|{
name|FilterProtos
operator|.
name|SkipFilter
operator|.
name|Builder
name|builder
init|=
name|FilterProtos
operator|.
name|SkipFilter
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
name|builder
operator|.
name|setFilter
argument_list|(
name|ProtobufUtil
operator|.
name|toFilter
argument_list|(
name|this
operator|.
name|filter
argument_list|)
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
comment|/**    * @param pbBytes A pb serialized {@link SkipFilter} instance    * @return An instance of {@link SkipFilter} made from<code>bytes</code>    * @throws DeserializationException    * @see #toByteArray    */
specifier|public
specifier|static
name|SkipFilter
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
name|SkipFilter
name|proto
decl_stmt|;
try|try
block|{
name|proto
operator|=
name|FilterProtos
operator|.
name|SkipFilter
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
try|try
block|{
return|return
operator|new
name|SkipFilter
argument_list|(
name|ProtobufUtil
operator|.
name|toFilter
argument_list|(
name|proto
operator|.
name|getFilter
argument_list|()
argument_list|)
argument_list|)
return|;
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
block|}
comment|/**    * @param o the other filter to compare with    * @return true if and only if the fields of the filter that are serialized    * are equal to the corresponding fields in other.  Used for testing.    */
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
name|SkipFilter
operator|)
condition|)
return|return
literal|false
return|;
name|SkipFilter
name|other
init|=
operator|(
name|SkipFilter
operator|)
name|o
decl_stmt|;
return|return
name|getFilter
argument_list|()
operator|.
name|areSerializedFieldsEqual
argument_list|(
name|other
operator|.
name|getFilter
argument_list|()
argument_list|)
return|;
block|}
annotation|@
name|Override
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
block|{
return|return
name|filter
operator|.
name|isFamilyEssential
argument_list|(
name|name
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
name|this
operator|.
name|getClass
argument_list|()
operator|.
name|getSimpleName
argument_list|()
operator|+
literal|" "
operator|+
name|this
operator|.
name|filter
operator|.
name|toString
argument_list|()
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
name|Objects
operator|.
name|hash
argument_list|(
name|this
operator|.
name|filter
argument_list|)
return|;
block|}
block|}
end_class

end_unit

