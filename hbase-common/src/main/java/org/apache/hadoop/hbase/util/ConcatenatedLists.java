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
name|java
operator|.
name|lang
operator|.
name|reflect
operator|.
name|Array
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
name|Collection
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
name|java
operator|.
name|util
operator|.
name|NoSuchElementException
import|;
end_import

begin_comment
comment|/**  * A collection class that contains multiple sub-lists, which allows us to not copy lists.  * This class does not support modification. The derived classes that add modifications are  * not thread-safe.  * NOTE: Doesn't implement list as it is not necessary for current usage, feel free to add.  */
end_comment

begin_class
specifier|public
class|class
name|ConcatenatedLists
parameter_list|<
name|T
parameter_list|>
implements|implements
name|Collection
argument_list|<
name|T
argument_list|>
block|{
specifier|protected
specifier|final
name|ArrayList
argument_list|<
name|List
argument_list|<
name|T
argument_list|>
argument_list|>
name|components
init|=
operator|new
name|ArrayList
argument_list|<
name|List
argument_list|<
name|T
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
specifier|protected
name|int
name|size
init|=
literal|0
decl_stmt|;
specifier|public
name|void
name|addAllSublists
parameter_list|(
name|List
argument_list|<
name|?
extends|extends
name|List
argument_list|<
name|T
argument_list|>
argument_list|>
name|items
parameter_list|)
block|{
for|for
control|(
name|List
argument_list|<
name|T
argument_list|>
name|list
range|:
name|items
control|)
block|{
name|addSublist
argument_list|(
name|list
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|void
name|addSublist
parameter_list|(
name|List
argument_list|<
name|T
argument_list|>
name|items
parameter_list|)
block|{
if|if
condition|(
operator|!
name|items
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|this
operator|.
name|components
operator|.
name|add
argument_list|(
name|items
argument_list|)
expr_stmt|;
name|this
operator|.
name|size
operator|+=
name|items
operator|.
name|size
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|int
name|size
parameter_list|()
block|{
return|return
name|this
operator|.
name|size
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isEmpty
parameter_list|()
block|{
return|return
name|this
operator|.
name|size
operator|==
literal|0
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|contains
parameter_list|(
name|Object
name|o
parameter_list|)
block|{
for|for
control|(
name|List
argument_list|<
name|T
argument_list|>
name|component
range|:
name|this
operator|.
name|components
control|)
block|{
if|if
condition|(
name|component
operator|.
name|contains
argument_list|(
name|o
argument_list|)
condition|)
return|return
literal|true
return|;
block|}
return|return
literal|false
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|containsAll
parameter_list|(
name|Collection
argument_list|<
name|?
argument_list|>
name|c
parameter_list|)
block|{
for|for
control|(
name|Object
name|o
range|:
name|c
control|)
block|{
if|if
condition|(
operator|!
name|contains
argument_list|(
name|o
argument_list|)
condition|)
return|return
literal|false
return|;
block|}
return|return
literal|true
return|;
block|}
annotation|@
name|Override
specifier|public
name|Object
index|[]
name|toArray
parameter_list|()
block|{
return|return
name|toArray
argument_list|(
operator|(
name|Object
index|[]
operator|)
name|Array
operator|.
name|newInstance
argument_list|(
name|Object
operator|.
name|class
argument_list|,
name|this
operator|.
name|size
argument_list|)
argument_list|)
return|;
block|}
annotation|@
name|Override
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
specifier|public
parameter_list|<
name|U
parameter_list|>
name|U
index|[]
name|toArray
parameter_list|(
name|U
index|[]
name|a
parameter_list|)
block|{
name|U
index|[]
name|result
init|=
operator|(
name|a
operator|.
name|length
operator|==
name|this
operator|.
name|size
argument_list|()
operator|)
condition|?
name|a
else|:
operator|(
name|U
index|[]
operator|)
name|Array
operator|.
name|newInstance
argument_list|(
name|a
operator|.
name|getClass
argument_list|()
operator|.
name|getComponentType
argument_list|()
argument_list|,
name|this
operator|.
name|size
argument_list|)
decl_stmt|;
name|int
name|i
init|=
literal|0
decl_stmt|;
for|for
control|(
name|List
argument_list|<
name|T
argument_list|>
name|component
range|:
name|this
operator|.
name|components
control|)
block|{
for|for
control|(
name|T
name|t
range|:
name|component
control|)
block|{
name|result
index|[
name|i
index|]
operator|=
operator|(
name|U
operator|)
name|t
expr_stmt|;
operator|++
name|i
expr_stmt|;
block|}
block|}
return|return
name|result
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|add
parameter_list|(
name|T
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|remove
parameter_list|(
name|Object
name|o
parameter_list|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|addAll
parameter_list|(
name|Collection
argument_list|<
name|?
extends|extends
name|T
argument_list|>
name|c
parameter_list|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|removeAll
parameter_list|(
name|Collection
argument_list|<
name|?
argument_list|>
name|c
parameter_list|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|retainAll
parameter_list|(
name|Collection
argument_list|<
name|?
argument_list|>
name|c
parameter_list|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|clear
parameter_list|()
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
annotation|@
name|Override
specifier|public
name|java
operator|.
name|util
operator|.
name|Iterator
argument_list|<
name|T
argument_list|>
name|iterator
parameter_list|()
block|{
return|return
operator|new
name|Iterator
argument_list|()
return|;
block|}
annotation|@
name|edu
operator|.
name|umd
operator|.
name|cs
operator|.
name|findbugs
operator|.
name|annotations
operator|.
name|SuppressWarnings
argument_list|(
name|value
operator|=
literal|"URF_UNREAD_PUBLIC_OR_PROTECTED_FIELD"
argument_list|,
name|justification
operator|=
literal|"nextWasCalled is using by StripeStoreFileManager"
argument_list|)
specifier|public
class|class
name|Iterator
implements|implements
name|java
operator|.
name|util
operator|.
name|Iterator
argument_list|<
name|T
argument_list|>
block|{
specifier|protected
name|int
name|currentComponent
init|=
literal|0
decl_stmt|;
specifier|protected
name|int
name|indexWithinComponent
init|=
operator|-
literal|1
decl_stmt|;
specifier|protected
name|boolean
name|nextWasCalled
init|=
literal|false
decl_stmt|;
annotation|@
name|Override
specifier|public
name|boolean
name|hasNext
parameter_list|()
block|{
return|return
operator|(
name|currentComponent
operator|+
literal|1
operator|)
operator|<
name|components
operator|.
name|size
argument_list|()
operator|||
operator|(
operator|(
name|currentComponent
operator|+
literal|1
operator|)
operator|==
name|components
operator|.
name|size
argument_list|()
operator|&&
operator|(
operator|(
name|indexWithinComponent
operator|+
literal|1
operator|)
operator|<
name|components
operator|.
name|get
argument_list|(
name|currentComponent
argument_list|)
operator|.
name|size
argument_list|()
operator|)
operator|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|T
name|next
parameter_list|()
block|{
if|if
condition|(
operator|!
name|components
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|this
operator|.
name|nextWasCalled
operator|=
literal|true
expr_stmt|;
name|List
argument_list|<
name|T
argument_list|>
name|src
init|=
name|components
operator|.
name|get
argument_list|(
name|currentComponent
argument_list|)
decl_stmt|;
if|if
condition|(
operator|++
name|indexWithinComponent
operator|<
name|src
operator|.
name|size
argument_list|()
condition|)
return|return
name|src
operator|.
name|get
argument_list|(
name|indexWithinComponent
argument_list|)
return|;
if|if
condition|(
operator|++
name|currentComponent
operator|<
name|components
operator|.
name|size
argument_list|()
condition|)
block|{
name|indexWithinComponent
operator|=
literal|0
expr_stmt|;
name|src
operator|=
name|components
operator|.
name|get
argument_list|(
name|currentComponent
argument_list|)
expr_stmt|;
assert|assert
name|src
operator|.
name|size
argument_list|()
operator|>
literal|0
assert|;
return|return
name|src
operator|.
name|get
argument_list|(
name|indexWithinComponent
argument_list|)
return|;
block|}
block|}
name|this
operator|.
name|nextWasCalled
operator|=
literal|false
expr_stmt|;
throw|throw
operator|new
name|NoSuchElementException
argument_list|()
throw|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|remove
parameter_list|()
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
block|}
block|}
end_class

end_unit

