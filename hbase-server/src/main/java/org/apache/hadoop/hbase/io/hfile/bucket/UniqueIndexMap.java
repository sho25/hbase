begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one or more  * contributor license agreements. See the NOTICE file distributed with this  * work for additional information regarding copyright ownership. The ASF  * licenses this file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the  * License for the specific language governing permissions and limitations  * under the License.  */
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
name|io
operator|.
name|hfile
operator|.
name|bucket
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|Serializable
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|ConcurrentHashMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|atomic
operator|.
name|AtomicInteger
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
name|InterfaceAudience
import|;
end_import

begin_comment
comment|/**  * Map from type T to int and vice-versa. Used for reducing bit field item  * counts.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
specifier|final
class|class
name|UniqueIndexMap
parameter_list|<
name|T
parameter_list|>
implements|implements
name|Serializable
block|{
specifier|private
specifier|static
specifier|final
name|long
name|serialVersionUID
init|=
operator|-
literal|1145635738654002342L
decl_stmt|;
name|ConcurrentHashMap
argument_list|<
name|T
argument_list|,
name|Integer
argument_list|>
name|mForwardMap
init|=
operator|new
name|ConcurrentHashMap
argument_list|<
name|T
argument_list|,
name|Integer
argument_list|>
argument_list|()
decl_stmt|;
name|ConcurrentHashMap
argument_list|<
name|Integer
argument_list|,
name|T
argument_list|>
name|mReverseMap
init|=
operator|new
name|ConcurrentHashMap
argument_list|<
name|Integer
argument_list|,
name|T
argument_list|>
argument_list|()
decl_stmt|;
name|AtomicInteger
name|mIndex
init|=
operator|new
name|AtomicInteger
argument_list|(
literal|0
argument_list|)
decl_stmt|;
comment|// Map a length to an index. If we can't, allocate a new mapping. We might
comment|// race here and get two entries with the same deserialiser. This is fine.
name|int
name|map
parameter_list|(
name|T
name|parameter
parameter_list|)
block|{
name|Integer
name|ret
init|=
name|mForwardMap
operator|.
name|get
argument_list|(
name|parameter
argument_list|)
decl_stmt|;
if|if
condition|(
name|ret
operator|!=
literal|null
condition|)
return|return
name|ret
operator|.
name|intValue
argument_list|()
return|;
name|int
name|nexti
init|=
name|mIndex
operator|.
name|incrementAndGet
argument_list|()
decl_stmt|;
assert|assert
operator|(
name|nexti
operator|<
name|Short
operator|.
name|MAX_VALUE
operator|)
assert|;
name|mForwardMap
operator|.
name|put
argument_list|(
name|parameter
argument_list|,
name|nexti
argument_list|)
expr_stmt|;
name|mReverseMap
operator|.
name|put
argument_list|(
name|nexti
argument_list|,
name|parameter
argument_list|)
expr_stmt|;
return|return
name|nexti
return|;
block|}
name|T
name|unmap
parameter_list|(
name|int
name|leni
parameter_list|)
block|{
name|Integer
name|len
init|=
name|Integer
operator|.
name|valueOf
argument_list|(
name|leni
argument_list|)
decl_stmt|;
assert|assert
name|mReverseMap
operator|.
name|containsKey
argument_list|(
name|len
argument_list|)
assert|;
return|return
name|mReverseMap
operator|.
name|get
argument_list|(
name|len
argument_list|)
return|;
block|}
block|}
end_class

end_unit

