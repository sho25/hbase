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
name|util
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
name|classification
operator|.
name|InterfaceAudience
import|;
end_import

begin_comment
comment|/**  * Mainly used as keys for HashMap.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
specifier|final
class|class
name|ImmutableByteArray
block|{
specifier|private
specifier|final
name|byte
index|[]
name|b
decl_stmt|;
specifier|private
name|ImmutableByteArray
parameter_list|(
name|byte
index|[]
name|b
parameter_list|)
block|{
name|this
operator|.
name|b
operator|=
name|b
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
return|return
name|Bytes
operator|.
name|hashCode
argument_list|(
name|b
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
name|obj
operator|.
name|getClass
argument_list|()
operator|!=
name|ImmutableByteArray
operator|.
name|class
condition|)
block|{
return|return
literal|false
return|;
block|}
return|return
name|Bytes
operator|.
name|equals
argument_list|(
name|b
argument_list|,
operator|(
operator|(
name|ImmutableByteArray
operator|)
name|obj
operator|)
operator|.
name|b
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|ImmutableByteArray
name|wrap
parameter_list|(
name|byte
index|[]
name|b
parameter_list|)
block|{
return|return
operator|new
name|ImmutableByteArray
argument_list|(
name|b
argument_list|)
return|;
block|}
specifier|public
name|String
name|toStringUtf8
parameter_list|()
block|{
return|return
name|Bytes
operator|.
name|toString
argument_list|(
name|b
argument_list|)
return|;
block|}
block|}
end_class

end_unit

