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
name|hbtop
package|;
end_package

begin_import
import|import
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
name|NonNull
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|AbstractMap
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
name|Map
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Set
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|stream
operator|.
name|Stream
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
name|hbtop
operator|.
name|field
operator|.
name|Field
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
name|hbtop
operator|.
name|field
operator|.
name|FieldValue
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
name|hbtop
operator|.
name|field
operator|.
name|FieldValueType
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
name|hbase
operator|.
name|thirdparty
operator|.
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|ImmutableMap
import|;
end_import

begin_comment
comment|/**  * Represents a record of the metrics in the top screen.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
specifier|final
class|class
name|Record
implements|implements
name|Map
argument_list|<
name|Field
argument_list|,
name|FieldValue
argument_list|>
block|{
specifier|private
specifier|final
name|ImmutableMap
argument_list|<
name|Field
argument_list|,
name|FieldValue
argument_list|>
name|values
decl_stmt|;
specifier|public
specifier|final
specifier|static
class|class
name|Entry
extends|extends
name|AbstractMap
operator|.
name|SimpleImmutableEntry
argument_list|<
name|Field
argument_list|,
name|FieldValue
argument_list|>
block|{
specifier|private
name|Entry
parameter_list|(
name|Field
name|key
parameter_list|,
name|FieldValue
name|value
parameter_list|)
block|{
name|super
argument_list|(
name|key
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
specifier|final
specifier|static
class|class
name|Builder
block|{
specifier|private
specifier|final
name|ImmutableMap
operator|.
name|Builder
argument_list|<
name|Field
argument_list|,
name|FieldValue
argument_list|>
name|builder
decl_stmt|;
specifier|private
name|Builder
parameter_list|()
block|{
name|builder
operator|=
name|ImmutableMap
operator|.
name|builder
argument_list|()
expr_stmt|;
block|}
specifier|public
name|Builder
name|put
parameter_list|(
name|Field
name|key
parameter_list|,
name|Object
name|value
parameter_list|)
block|{
name|builder
operator|.
name|put
argument_list|(
name|key
argument_list|,
name|key
operator|.
name|newValue
argument_list|(
name|value
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|Builder
name|put
parameter_list|(
name|Field
name|key
parameter_list|,
name|FieldValue
name|value
parameter_list|)
block|{
name|builder
operator|.
name|put
argument_list|(
name|key
argument_list|,
name|value
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|Builder
name|put
parameter_list|(
name|Entry
name|entry
parameter_list|)
block|{
name|builder
operator|.
name|put
argument_list|(
name|entry
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|Builder
name|putAll
parameter_list|(
name|Map
argument_list|<
name|Field
argument_list|,
name|FieldValue
argument_list|>
name|map
parameter_list|)
block|{
name|builder
operator|.
name|putAll
argument_list|(
name|map
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|Record
name|build
parameter_list|()
block|{
return|return
operator|new
name|Record
argument_list|(
name|builder
operator|.
name|build
argument_list|()
argument_list|)
return|;
block|}
block|}
specifier|public
specifier|static
name|Builder
name|builder
parameter_list|()
block|{
return|return
operator|new
name|Builder
argument_list|()
return|;
block|}
specifier|public
specifier|static
name|Entry
name|entry
parameter_list|(
name|Field
name|field
parameter_list|,
name|Object
name|value
parameter_list|)
block|{
return|return
operator|new
name|Entry
argument_list|(
name|field
argument_list|,
name|field
operator|.
name|newValue
argument_list|(
name|value
argument_list|)
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|Entry
name|entry
parameter_list|(
name|Field
name|field
parameter_list|,
name|FieldValue
name|value
parameter_list|)
block|{
return|return
operator|new
name|Entry
argument_list|(
name|field
argument_list|,
name|value
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|Record
name|ofEntries
parameter_list|(
name|Entry
modifier|...
name|entries
parameter_list|)
block|{
return|return
name|ofEntries
argument_list|(
name|Stream
operator|.
name|of
argument_list|(
name|entries
argument_list|)
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|Record
name|ofEntries
parameter_list|(
name|Stream
argument_list|<
name|Entry
argument_list|>
name|entries
parameter_list|)
block|{
return|return
name|entries
operator|.
name|collect
argument_list|(
name|Record
operator|::
name|builder
argument_list|,
name|Builder
operator|::
name|put
argument_list|,
parameter_list|(
name|r1
parameter_list|,
name|r2
parameter_list|)
lambda|->
block|{}
argument_list|)
operator|.
name|build
argument_list|()
return|;
block|}
specifier|private
name|Record
parameter_list|(
name|ImmutableMap
argument_list|<
name|Field
argument_list|,
name|FieldValue
argument_list|>
name|values
parameter_list|)
block|{
name|this
operator|.
name|values
operator|=
name|values
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|size
parameter_list|()
block|{
return|return
name|values
operator|.
name|size
argument_list|()
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
name|values
operator|.
name|isEmpty
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|containsKey
parameter_list|(
name|Object
name|key
parameter_list|)
block|{
return|return
name|values
operator|.
name|containsKey
argument_list|(
name|key
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|containsValue
parameter_list|(
name|Object
name|value
parameter_list|)
block|{
return|return
name|values
operator|.
name|containsValue
argument_list|(
name|value
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|FieldValue
name|get
parameter_list|(
name|Object
name|key
parameter_list|)
block|{
return|return
name|values
operator|.
name|get
argument_list|(
name|key
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|FieldValue
name|put
parameter_list|(
name|Field
name|key
parameter_list|,
name|FieldValue
name|value
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
name|FieldValue
name|remove
parameter_list|(
name|Object
name|key
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
name|putAll
parameter_list|(
annotation|@
name|NonNull
name|Map
argument_list|<
name|?
extends|extends
name|Field
argument_list|,
name|?
extends|extends
name|FieldValue
argument_list|>
name|m
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
annotation|@
name|NonNull
specifier|public
name|Set
argument_list|<
name|Field
argument_list|>
name|keySet
parameter_list|()
block|{
return|return
name|values
operator|.
name|keySet
argument_list|()
return|;
block|}
annotation|@
name|Override
annotation|@
name|NonNull
specifier|public
name|Collection
argument_list|<
name|FieldValue
argument_list|>
name|values
parameter_list|()
block|{
return|return
name|values
operator|.
name|values
argument_list|()
return|;
block|}
annotation|@
name|Override
annotation|@
name|NonNull
specifier|public
name|Set
argument_list|<
name|Map
operator|.
name|Entry
argument_list|<
name|Field
argument_list|,
name|FieldValue
argument_list|>
argument_list|>
name|entrySet
parameter_list|()
block|{
return|return
name|values
operator|.
name|entrySet
argument_list|()
return|;
block|}
specifier|public
name|Record
name|combine
parameter_list|(
name|Record
name|o
parameter_list|)
block|{
return|return
name|ofEntries
argument_list|(
name|values
operator|.
name|keySet
argument_list|()
operator|.
name|stream
argument_list|()
operator|.
name|map
argument_list|(
name|k
lambda|->
block|{
if|if
condition|(
name|k
operator|.
name|getFieldValueType
argument_list|()
operator|==
name|FieldValueType
operator|.
name|STRING
condition|)
block|{
return|return
name|entry
argument_list|(
name|k
argument_list|,
name|values
operator|.
name|get
argument_list|(
name|k
argument_list|)
argument_list|)
return|;
block|}
return|return
name|entry
argument_list|(
name|k
argument_list|,
name|values
operator|.
name|get
argument_list|(
name|k
argument_list|)
operator|.
name|plus
argument_list|(
name|o
operator|.
name|values
operator|.
name|get
argument_list|(
name|k
argument_list|)
argument_list|)
argument_list|)
return|;
block|}
argument_list|)
argument_list|)
return|;
block|}
block|}
end_class

end_unit
