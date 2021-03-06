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
name|util
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

begin_comment
comment|/**  *  A generic class for pair of an Object and and a primitive int value.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|ObjectIntPair
parameter_list|<
name|T
parameter_list|>
block|{
specifier|private
name|T
name|first
decl_stmt|;
specifier|private
name|int
name|second
decl_stmt|;
specifier|public
name|ObjectIntPair
parameter_list|()
block|{   }
specifier|public
name|ObjectIntPair
parameter_list|(
name|T
name|first
parameter_list|,
name|int
name|second
parameter_list|)
block|{
name|this
operator|.
name|setFirst
argument_list|(
name|first
argument_list|)
expr_stmt|;
name|this
operator|.
name|setSecond
argument_list|(
name|second
argument_list|)
expr_stmt|;
block|}
specifier|public
name|T
name|getFirst
parameter_list|()
block|{
return|return
name|first
return|;
block|}
specifier|public
name|void
name|setFirst
parameter_list|(
name|T
name|first
parameter_list|)
block|{
name|this
operator|.
name|first
operator|=
name|first
expr_stmt|;
block|}
specifier|public
name|int
name|getSecond
parameter_list|()
block|{
return|return
name|second
return|;
block|}
specifier|public
name|void
name|setSecond
parameter_list|(
name|int
name|second
parameter_list|)
block|{
name|this
operator|.
name|second
operator|=
name|second
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|equals
parameter_list|(
name|Object
name|other
parameter_list|)
block|{
return|return
name|other
operator|instanceof
name|ObjectIntPair
operator|&&
name|equals
argument_list|(
name|first
argument_list|,
operator|(
operator|(
name|ObjectIntPair
argument_list|<
name|?
argument_list|>
operator|)
name|other
operator|)
operator|.
name|first
argument_list|)
operator|&&
operator|(
name|this
operator|.
name|second
operator|==
operator|(
operator|(
name|ObjectIntPair
argument_list|<
name|?
argument_list|>
operator|)
name|other
operator|)
operator|.
name|second
operator|)
return|;
block|}
specifier|private
specifier|static
name|boolean
name|equals
parameter_list|(
name|Object
name|x
parameter_list|,
name|Object
name|y
parameter_list|)
block|{
return|return
operator|(
name|x
operator|==
literal|null
operator|&&
name|y
operator|==
literal|null
operator|)
operator|||
operator|(
name|x
operator|!=
literal|null
operator|&&
name|x
operator|.
name|equals
argument_list|(
name|y
argument_list|)
operator|)
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
name|first
operator|==
literal|null
condition|?
literal|0
else|:
operator|(
name|first
operator|.
name|hashCode
argument_list|()
operator|*
literal|17
operator|)
operator|+
literal|13
operator|*
name|second
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
literal|"{"
operator|+
name|getFirst
argument_list|()
operator|+
literal|","
operator|+
name|getSecond
argument_list|()
operator|+
literal|"}"
return|;
block|}
block|}
end_class

end_unit

