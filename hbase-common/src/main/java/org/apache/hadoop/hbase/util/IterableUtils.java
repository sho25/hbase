begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|Collections
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
name|yetus
operator|.
name|audience
operator|.
name|InterfaceAudience
import|;
end_import

begin_comment
comment|/**  * Utility methods for Iterable including null-safe handlers.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|IterableUtils
block|{
specifier|private
specifier|static
specifier|final
name|List
argument_list|<
name|Object
argument_list|>
name|EMPTY_LIST
init|=
name|Collections
operator|.
name|unmodifiableList
argument_list|(
operator|new
name|ArrayList
argument_list|<>
argument_list|(
literal|0
argument_list|)
argument_list|)
decl_stmt|;
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
specifier|public
specifier|static
parameter_list|<
name|T
parameter_list|>
name|Iterable
argument_list|<
name|T
argument_list|>
name|nullSafe
parameter_list|(
name|Iterable
argument_list|<
name|T
argument_list|>
name|in
parameter_list|)
block|{
if|if
condition|(
name|in
operator|==
literal|null
condition|)
block|{
return|return
operator|(
name|List
argument_list|<
name|T
argument_list|>
operator|)
name|EMPTY_LIST
return|;
block|}
return|return
name|in
return|;
block|}
block|}
end_class

end_unit

