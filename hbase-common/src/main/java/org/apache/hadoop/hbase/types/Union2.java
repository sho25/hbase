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
comment|/**  * The {@code Union} family of {@link DataType}s encode one of a fixed  * set of {@code Object}s. They provide convenience methods which handle  * type casting on your behalf.  */
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
name|Union2
parameter_list|<
name|A
parameter_list|,
name|B
parameter_list|>
implements|implements
name|DataType
argument_list|<
name|Object
argument_list|>
block|{
specifier|protected
specifier|final
name|DataType
argument_list|<
name|A
argument_list|>
name|typeA
decl_stmt|;
specifier|protected
specifier|final
name|DataType
argument_list|<
name|B
argument_list|>
name|typeB
decl_stmt|;
comment|/**    * Create an instance of {@code Union2} over the set of specified    * types.    */
specifier|public
name|Union2
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
parameter_list|)
block|{
name|this
operator|.
name|typeA
operator|=
name|typeA
expr_stmt|;
name|this
operator|.
name|typeB
operator|=
name|typeB
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
name|typeA
operator|.
name|isOrderPreserving
argument_list|()
operator|&&
name|typeB
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
name|typeA
operator|.
name|isNullable
argument_list|()
operator|&&
name|typeB
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
name|typeA
operator|.
name|isSkippable
argument_list|()
operator|&&
name|typeB
operator|.
name|isSkippable
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|Class
argument_list|<
name|Object
argument_list|>
name|encodedClass
parameter_list|()
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"Union types do not expose a definitive encoded class."
argument_list|)
throw|;
block|}
comment|/**    * Read an instance of the first type parameter from buffer {@code src}.    */
specifier|public
name|A
name|decodeA
parameter_list|(
name|PositionedByteRange
name|src
parameter_list|)
block|{
return|return
operator|(
name|A
operator|)
name|decode
argument_list|(
name|src
argument_list|)
return|;
block|}
comment|/**    * Read an instance of the second type parameter from buffer {@code src}.    */
specifier|public
name|B
name|decodeB
parameter_list|(
name|PositionedByteRange
name|src
parameter_list|)
block|{
return|return
operator|(
name|B
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

