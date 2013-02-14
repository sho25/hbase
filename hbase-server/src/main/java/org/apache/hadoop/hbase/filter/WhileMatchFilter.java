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
name|protobuf
operator|.
name|generated
operator|.
name|FilterProtos
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|InvalidProtocolBufferException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataInput
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataOutput
import|;
end_import

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

begin_comment
comment|/**  * A wrapper filter that returns true from {@link #filterAllRemaining()} as soon  * as the wrapped filters {@link Filter#filterRowKey(byte[], int, int)},  * {@link Filter#filterKeyValue(org.apache.hadoop.hbase.KeyValue)},  * {@link org.apache.hadoop.hbase.filter.Filter#filterRow()} or  * {@link org.apache.hadoop.hbase.filter.Filter#filterAllRemaining()} methods  * returns true.  */
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
class|class
name|WhileMatchFilter
extends|extends
name|FilterBase
block|{
specifier|private
name|boolean
name|filterAllRemaining
init|=
literal|false
decl_stmt|;
specifier|private
name|Filter
name|filter
decl_stmt|;
specifier|public
name|WhileMatchFilter
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
specifier|public
name|void
name|reset
parameter_list|()
block|{
name|this
operator|.
name|filter
operator|.
name|reset
argument_list|()
expr_stmt|;
block|}
specifier|private
name|void
name|changeFAR
parameter_list|(
name|boolean
name|value
parameter_list|)
block|{
name|filterAllRemaining
operator|=
name|filterAllRemaining
operator|||
name|value
expr_stmt|;
block|}
specifier|public
name|boolean
name|filterAllRemaining
parameter_list|()
block|{
return|return
name|this
operator|.
name|filterAllRemaining
operator|||
name|this
operator|.
name|filter
operator|.
name|filterAllRemaining
argument_list|()
return|;
block|}
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
block|{
name|boolean
name|value
init|=
name|filter
operator|.
name|filterRowKey
argument_list|(
name|buffer
argument_list|,
name|offset
argument_list|,
name|length
argument_list|)
decl_stmt|;
name|changeFAR
argument_list|(
name|value
argument_list|)
expr_stmt|;
return|return
name|value
return|;
block|}
specifier|public
name|ReturnCode
name|filterKeyValue
parameter_list|(
name|KeyValue
name|v
parameter_list|)
block|{
name|ReturnCode
name|c
init|=
name|filter
operator|.
name|filterKeyValue
argument_list|(
name|v
argument_list|)
decl_stmt|;
name|changeFAR
argument_list|(
name|c
operator|!=
name|ReturnCode
operator|.
name|INCLUDE
argument_list|)
expr_stmt|;
return|return
name|c
return|;
block|}
annotation|@
name|Override
specifier|public
name|KeyValue
name|transform
parameter_list|(
name|KeyValue
name|v
parameter_list|)
block|{
return|return
name|filter
operator|.
name|transform
argument_list|(
name|v
argument_list|)
return|;
block|}
specifier|public
name|boolean
name|filterRow
parameter_list|()
block|{
name|boolean
name|filterRow
init|=
name|this
operator|.
name|filter
operator|.
name|filterRow
argument_list|()
decl_stmt|;
name|changeFAR
argument_list|(
name|filterRow
argument_list|)
expr_stmt|;
return|return
name|filterRow
return|;
block|}
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
specifier|public
name|byte
index|[]
name|toByteArray
parameter_list|()
block|{
name|FilterProtos
operator|.
name|WhileMatchFilter
operator|.
name|Builder
name|builder
init|=
name|FilterProtos
operator|.
name|WhileMatchFilter
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
comment|/**    * @param pbBytes A pb serialized {@link WhileMatchFilter} instance    * @return An instance of {@link WhileMatchFilter} made from<code>bytes</code>    * @throws DeserializationException    * @see #toByteArray    */
specifier|public
specifier|static
name|WhileMatchFilter
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
name|WhileMatchFilter
name|proto
decl_stmt|;
try|try
block|{
name|proto
operator|=
name|FilterProtos
operator|.
name|WhileMatchFilter
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
name|WhileMatchFilter
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
comment|/**    * @param other    * @return true if and only if the fields of the filter that are serialized    * are equal to the corresponding fields in other.  Used for testing.    */
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
name|WhileMatchFilter
operator|)
condition|)
return|return
literal|false
return|;
name|WhileMatchFilter
name|other
init|=
operator|(
name|WhileMatchFilter
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
specifier|public
name|boolean
name|isFamilyEssential
parameter_list|(
name|byte
index|[]
name|name
parameter_list|)
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
block|}
end_class

end_unit

