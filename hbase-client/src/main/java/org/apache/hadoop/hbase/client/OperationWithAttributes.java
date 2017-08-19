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
name|client
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collections
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
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
name|HConstants
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
name|util
operator|.
name|Bytes
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
name|ClassSize
import|;
end_import

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Public
specifier|public
specifier|abstract
class|class
name|OperationWithAttributes
extends|extends
name|Operation
implements|implements
name|Attributes
block|{
comment|// An opaque blob of attributes
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|byte
index|[]
argument_list|>
name|attributes
decl_stmt|;
comment|// used for uniquely identifying an operation
specifier|public
specifier|static
specifier|final
name|String
name|ID_ATRIBUTE
init|=
literal|"_operation.attributes.id"
decl_stmt|;
specifier|private
name|int
name|priority
init|=
name|HConstants
operator|.
name|PRIORITY_UNSET
decl_stmt|;
annotation|@
name|Override
specifier|public
name|OperationWithAttributes
name|setAttribute
parameter_list|(
name|String
name|name
parameter_list|,
name|byte
index|[]
name|value
parameter_list|)
block|{
if|if
condition|(
name|attributes
operator|==
literal|null
operator|&&
name|value
operator|==
literal|null
condition|)
block|{
return|return
name|this
return|;
block|}
if|if
condition|(
name|attributes
operator|==
literal|null
condition|)
block|{
name|attributes
operator|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|value
operator|==
literal|null
condition|)
block|{
name|attributes
operator|.
name|remove
argument_list|(
name|name
argument_list|)
expr_stmt|;
if|if
condition|(
name|attributes
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|this
operator|.
name|attributes
operator|=
literal|null
expr_stmt|;
block|}
block|}
else|else
block|{
name|attributes
operator|.
name|put
argument_list|(
name|name
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
return|return
name|this
return|;
block|}
annotation|@
name|Override
specifier|public
name|byte
index|[]
name|getAttribute
parameter_list|(
name|String
name|name
parameter_list|)
block|{
if|if
condition|(
name|attributes
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
return|return
name|attributes
operator|.
name|get
argument_list|(
name|name
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|byte
index|[]
argument_list|>
name|getAttributesMap
parameter_list|()
block|{
if|if
condition|(
name|attributes
operator|==
literal|null
condition|)
block|{
return|return
name|Collections
operator|.
name|emptyMap
argument_list|()
return|;
block|}
return|return
name|Collections
operator|.
name|unmodifiableMap
argument_list|(
name|attributes
argument_list|)
return|;
block|}
specifier|protected
name|long
name|getAttributeSize
parameter_list|()
block|{
name|long
name|size
init|=
literal|0
decl_stmt|;
if|if
condition|(
name|attributes
operator|!=
literal|null
condition|)
block|{
name|size
operator|+=
name|ClassSize
operator|.
name|align
argument_list|(
name|this
operator|.
name|attributes
operator|.
name|size
argument_list|()
operator|*
name|ClassSize
operator|.
name|MAP_ENTRY
argument_list|)
expr_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|byte
index|[]
argument_list|>
name|entry
range|:
name|this
operator|.
name|attributes
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|size
operator|+=
name|ClassSize
operator|.
name|align
argument_list|(
name|ClassSize
operator|.
name|STRING
operator|+
name|entry
operator|.
name|getKey
argument_list|()
operator|.
name|length
argument_list|()
argument_list|)
expr_stmt|;
name|size
operator|+=
name|ClassSize
operator|.
name|align
argument_list|(
name|ClassSize
operator|.
name|ARRAY
operator|+
name|entry
operator|.
name|getValue
argument_list|()
operator|.
name|length
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|size
return|;
block|}
comment|/**    * This method allows you to set an identifier on an operation. The original    * motivation for this was to allow the identifier to be used in slow query    * logging, but this could obviously be useful in other places. One use of    * this could be to put a class.method identifier in here to see where the    * slow query is coming from.    * @param id    *          id to set for the scan    */
specifier|public
name|OperationWithAttributes
name|setId
parameter_list|(
name|String
name|id
parameter_list|)
block|{
name|setAttribute
argument_list|(
name|ID_ATRIBUTE
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|id
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**    * This method allows you to retrieve the identifier for the operation if one    * was set.    * @return the id or null if not set    */
specifier|public
name|String
name|getId
parameter_list|()
block|{
name|byte
index|[]
name|attr
init|=
name|getAttribute
argument_list|(
name|ID_ATRIBUTE
argument_list|)
decl_stmt|;
return|return
name|attr
operator|==
literal|null
condition|?
literal|null
else|:
name|Bytes
operator|.
name|toString
argument_list|(
name|attr
argument_list|)
return|;
block|}
specifier|public
name|OperationWithAttributes
name|setPriority
parameter_list|(
name|int
name|priority
parameter_list|)
block|{
name|this
operator|.
name|priority
operator|=
name|priority
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|int
name|getPriority
parameter_list|()
block|{
return|return
name|priority
return|;
block|}
block|}
end_class

end_unit

