begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Copyright The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|classification
operator|.
name|InterfaceAudience
import|;
end_import

begin_comment
comment|/**  * Utilities for class manipulation.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|Classes
block|{
comment|/**    * Equivalent of {@link Class#forName(String)} which also returns classes for    * primitives like<code>boolean</code>, etc.    *     * @param className    *          The name of the class to retrieve. Can be either a normal class or    *          a primitive class.    * @return The class specified by<code>className</code>    * @throws ClassNotFoundException    *           If the requested class can not be found.    */
specifier|public
specifier|static
name|Class
argument_list|<
name|?
argument_list|>
name|extendedForName
parameter_list|(
name|String
name|className
parameter_list|)
throws|throws
name|ClassNotFoundException
block|{
name|Class
argument_list|<
name|?
argument_list|>
name|valueType
decl_stmt|;
if|if
condition|(
name|className
operator|.
name|equals
argument_list|(
literal|"boolean"
argument_list|)
condition|)
block|{
name|valueType
operator|=
name|boolean
operator|.
name|class
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|className
operator|.
name|equals
argument_list|(
literal|"byte"
argument_list|)
condition|)
block|{
name|valueType
operator|=
name|byte
operator|.
name|class
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|className
operator|.
name|equals
argument_list|(
literal|"short"
argument_list|)
condition|)
block|{
name|valueType
operator|=
name|short
operator|.
name|class
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|className
operator|.
name|equals
argument_list|(
literal|"int"
argument_list|)
condition|)
block|{
name|valueType
operator|=
name|int
operator|.
name|class
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|className
operator|.
name|equals
argument_list|(
literal|"long"
argument_list|)
condition|)
block|{
name|valueType
operator|=
name|long
operator|.
name|class
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|className
operator|.
name|equals
argument_list|(
literal|"float"
argument_list|)
condition|)
block|{
name|valueType
operator|=
name|float
operator|.
name|class
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|className
operator|.
name|equals
argument_list|(
literal|"double"
argument_list|)
condition|)
block|{
name|valueType
operator|=
name|double
operator|.
name|class
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|className
operator|.
name|equals
argument_list|(
literal|"char"
argument_list|)
condition|)
block|{
name|valueType
operator|=
name|char
operator|.
name|class
expr_stmt|;
block|}
else|else
block|{
name|valueType
operator|=
name|Class
operator|.
name|forName
argument_list|(
name|className
argument_list|)
expr_stmt|;
block|}
return|return
name|valueType
return|;
block|}
specifier|public
specifier|static
name|String
name|stringify
parameter_list|(
name|Class
index|[]
name|classes
parameter_list|)
block|{
name|StringBuilder
name|buf
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
if|if
condition|(
name|classes
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|Class
name|c
range|:
name|classes
control|)
block|{
if|if
condition|(
name|buf
operator|.
name|length
argument_list|()
operator|>
literal|0
condition|)
block|{
name|buf
operator|.
name|append
argument_list|(
literal|","
argument_list|)
expr_stmt|;
block|}
name|buf
operator|.
name|append
argument_list|(
name|c
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|buf
operator|.
name|append
argument_list|(
literal|"NULL"
argument_list|)
expr_stmt|;
block|}
return|return
name|buf
operator|.
name|toString
argument_list|()
return|;
block|}
block|}
end_class

end_unit

