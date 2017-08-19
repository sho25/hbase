begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|types
package|;
end_package

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
name|util
operator|.
name|Order
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
name|util
operator|.
name|PositionedByteRange
import|;
end_import

begin_comment
comment|/**  * The {@code Union} family of {@link DataType}s encode one of a fixed  * collection of Objects. They provide convenience methods which handle type  * casting on your behalf.  * @see Union2  */
end_comment

begin_class
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
annotation|@
name|InterfaceAudience
operator|.
name|Public
specifier|public
specifier|abstract
class|class
name|Union3
parameter_list|<
name|A
parameter_list|,
name|B
parameter_list|,
name|C
parameter_list|>
extends|extends
name|Union2
argument_list|<
name|A
argument_list|,
name|B
argument_list|>
block|{
specifier|protected
specifier|final
name|DataType
argument_list|<
name|C
argument_list|>
name|typeC
decl_stmt|;
comment|/**    * Create an instance of {@code Union3} over the set of specified    * types.    */
specifier|public
name|Union3
parameter_list|(
name|DataType
argument_list|<
name|A
argument_list|>
name|typeA
parameter_list|,
name|DataType
argument_list|<
name|B
argument_list|>
name|typeB
parameter_list|,
name|DataType
argument_list|<
name|C
argument_list|>
name|typeC
parameter_list|)
block|{
name|super
argument_list|(
name|typeA
argument_list|,
name|typeB
argument_list|)
expr_stmt|;
name|this
operator|.
name|typeC
operator|=
name|typeC
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isOrderPreserving
parameter_list|()
block|{
return|return
name|super
operator|.
name|isOrderPreserving
argument_list|()
operator|&&
name|typeC
operator|.
name|isOrderPreserving
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|Order
name|getOrder
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isNullable
parameter_list|()
block|{
return|return
name|super
operator|.
name|isNullable
argument_list|()
operator|&&
name|typeC
operator|.
name|isNullable
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isSkippable
parameter_list|()
block|{
return|return
name|super
operator|.
name|isSkippable
argument_list|()
operator|&&
name|typeC
operator|.
name|isSkippable
argument_list|()
return|;
block|}
comment|/**    * Read an instance of the third type parameter from buffer {@code src}.    */
specifier|public
name|C
name|decodeC
parameter_list|(
name|PositionedByteRange
name|src
parameter_list|)
block|{
return|return
operator|(
name|C
operator|)
name|decode
argument_list|(
name|src
argument_list|)
return|;
block|}
block|}
end_class

end_unit

