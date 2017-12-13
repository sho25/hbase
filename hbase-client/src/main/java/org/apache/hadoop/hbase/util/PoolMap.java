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
name|HashMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashSet
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
name|Locale
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
name|ConcurrentLinkedQueue
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
name|CopyOnWriteArrayList
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
name|yetus
operator|.
name|audience
operator|.
name|InterfaceAudience
import|;
end_import

begin_comment
comment|/**  *  * The<code>PoolMap</code> maps a key to a collection of values, the elements  * of which are managed by a pool. In effect, that collection acts as a shared  * pool of resources, access to which is closely controlled as per the semantics  * of the pool.  *  *<p>  * In case the size of the pool is set to a non-zero positive number, that is  * used to cap the number of resources that a pool may contain for any given  * key. A size of {@link Integer#MAX_VALUE} is interpreted as an unbounded pool.  *</p>  *  * @param<K>  *          the type of the key to the resource  * @param<V>  *          the type of the resource being pooled  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|PoolMap
parameter_list|<
name|K
parameter_list|,
name|V
parameter_list|>
implements|implements
name|Map
argument_list|<
name|K
argument_list|,
name|V
argument_list|>
block|{
specifier|private
name|PoolType
name|poolType
decl_stmt|;
specifier|private
name|int
name|poolMaxSize
decl_stmt|;
specifier|private
name|Map
argument_list|<
name|K
argument_list|,
name|Pool
argument_list|<
name|V
argument_list|>
argument_list|>
name|pools
init|=
operator|new
name|ConcurrentHashMap
argument_list|<>
argument_list|()
decl_stmt|;
specifier|public
name|PoolMap
parameter_list|(
name|PoolType
name|poolType
parameter_list|)
block|{
name|this
operator|.
name|poolType
operator|=
name|poolType
expr_stmt|;
block|}
specifier|public
name|PoolMap
parameter_list|(
name|PoolType
name|poolType
parameter_list|,
name|int
name|poolMaxSize
parameter_list|)
block|{
name|this
operator|.
name|poolType
operator|=
name|poolType
expr_stmt|;
name|this
operator|.
name|poolMaxSize
operator|=
name|poolMaxSize
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|V
name|get
parameter_list|(
name|Object
name|key
parameter_list|)
block|{
name|Pool
argument_list|<
name|V
argument_list|>
name|pool
init|=
name|pools
operator|.
name|get
argument_list|(
name|key
argument_list|)
decl_stmt|;
return|return
name|pool
operator|!=
literal|null
condition|?
name|pool
operator|.
name|get
argument_list|()
else|:
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|V
name|put
parameter_list|(
name|K
name|key
parameter_list|,
name|V
name|value
parameter_list|)
block|{
name|Pool
argument_list|<
name|V
argument_list|>
name|pool
init|=
name|pools
operator|.
name|get
argument_list|(
name|key
argument_list|)
decl_stmt|;
if|if
condition|(
name|pool
operator|==
literal|null
condition|)
block|{
name|pools
operator|.
name|put
argument_list|(
name|key
argument_list|,
name|pool
operator|=
name|createPool
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|pool
operator|!=
literal|null
condition|?
name|pool
operator|.
name|put
argument_list|(
name|value
argument_list|)
else|:
literal|null
return|;
block|}
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
annotation|@
name|Override
specifier|public
name|V
name|remove
parameter_list|(
name|Object
name|key
parameter_list|)
block|{
name|Pool
argument_list|<
name|V
argument_list|>
name|pool
init|=
name|pools
operator|.
name|remove
argument_list|(
name|key
argument_list|)
decl_stmt|;
if|if
condition|(
name|pool
operator|!=
literal|null
condition|)
block|{
name|removeValue
argument_list|(
operator|(
name|K
operator|)
name|key
argument_list|,
name|pool
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
literal|null
return|;
block|}
specifier|public
name|boolean
name|removeValue
parameter_list|(
name|K
name|key
parameter_list|,
name|V
name|value
parameter_list|)
block|{
name|Pool
argument_list|<
name|V
argument_list|>
name|pool
init|=
name|pools
operator|.
name|get
argument_list|(
name|key
argument_list|)
decl_stmt|;
name|boolean
name|res
init|=
literal|false
decl_stmt|;
if|if
condition|(
name|pool
operator|!=
literal|null
condition|)
block|{
name|res
operator|=
name|pool
operator|.
name|remove
argument_list|(
name|value
argument_list|)
expr_stmt|;
if|if
condition|(
name|res
operator|&&
name|pool
operator|.
name|size
argument_list|()
operator|==
literal|0
condition|)
block|{
name|pools
operator|.
name|remove
argument_list|(
name|key
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|res
return|;
block|}
annotation|@
name|Override
specifier|public
name|Collection
argument_list|<
name|V
argument_list|>
name|values
parameter_list|()
block|{
name|Collection
argument_list|<
name|V
argument_list|>
name|values
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|Pool
argument_list|<
name|V
argument_list|>
name|pool
range|:
name|pools
operator|.
name|values
argument_list|()
control|)
block|{
name|Collection
argument_list|<
name|V
argument_list|>
name|poolValues
init|=
name|pool
operator|.
name|values
argument_list|()
decl_stmt|;
if|if
condition|(
name|poolValues
operator|!=
literal|null
condition|)
block|{
name|values
operator|.
name|addAll
argument_list|(
name|poolValues
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|values
return|;
block|}
specifier|public
name|Collection
argument_list|<
name|V
argument_list|>
name|values
parameter_list|(
name|K
name|key
parameter_list|)
block|{
name|Collection
argument_list|<
name|V
argument_list|>
name|values
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|Pool
argument_list|<
name|V
argument_list|>
name|pool
init|=
name|pools
operator|.
name|get
argument_list|(
name|key
argument_list|)
decl_stmt|;
if|if
condition|(
name|pool
operator|!=
literal|null
condition|)
block|{
name|Collection
argument_list|<
name|V
argument_list|>
name|poolValues
init|=
name|pool
operator|.
name|values
argument_list|()
decl_stmt|;
if|if
condition|(
name|poolValues
operator|!=
literal|null
condition|)
block|{
name|values
operator|.
name|addAll
argument_list|(
name|poolValues
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|values
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
name|pools
operator|.
name|isEmpty
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|size
parameter_list|()
block|{
return|return
name|pools
operator|.
name|size
argument_list|()
return|;
block|}
specifier|public
name|int
name|size
parameter_list|(
name|K
name|key
parameter_list|)
block|{
name|Pool
argument_list|<
name|V
argument_list|>
name|pool
init|=
name|pools
operator|.
name|get
argument_list|(
name|key
argument_list|)
decl_stmt|;
return|return
name|pool
operator|!=
literal|null
condition|?
name|pool
operator|.
name|size
argument_list|()
else|:
literal|0
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
name|pools
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
if|if
condition|(
name|value
operator|==
literal|null
condition|)
block|{
return|return
literal|false
return|;
block|}
for|for
control|(
name|Pool
argument_list|<
name|V
argument_list|>
name|pool
range|:
name|pools
operator|.
name|values
argument_list|()
control|)
block|{
if|if
condition|(
name|value
operator|.
name|equals
argument_list|(
name|pool
operator|.
name|get
argument_list|()
argument_list|)
condition|)
block|{
return|return
literal|true
return|;
block|}
block|}
return|return
literal|false
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|putAll
parameter_list|(
name|Map
argument_list|<
name|?
extends|extends
name|K
argument_list|,
name|?
extends|extends
name|V
argument_list|>
name|map
parameter_list|)
block|{
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|?
extends|extends
name|K
argument_list|,
name|?
extends|extends
name|V
argument_list|>
name|entry
range|:
name|map
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|put
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|,
name|entry
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|clear
parameter_list|()
block|{
for|for
control|(
name|Pool
argument_list|<
name|V
argument_list|>
name|pool
range|:
name|pools
operator|.
name|values
argument_list|()
control|)
block|{
name|pool
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
name|pools
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Set
argument_list|<
name|K
argument_list|>
name|keySet
parameter_list|()
block|{
return|return
name|pools
operator|.
name|keySet
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|Set
argument_list|<
name|Map
operator|.
name|Entry
argument_list|<
name|K
argument_list|,
name|V
argument_list|>
argument_list|>
name|entrySet
parameter_list|()
block|{
name|Set
argument_list|<
name|Map
operator|.
name|Entry
argument_list|<
name|K
argument_list|,
name|V
argument_list|>
argument_list|>
name|entries
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|K
argument_list|,
name|Pool
argument_list|<
name|V
argument_list|>
argument_list|>
name|poolEntry
range|:
name|pools
operator|.
name|entrySet
argument_list|()
control|)
block|{
specifier|final
name|K
name|poolKey
init|=
name|poolEntry
operator|.
name|getKey
argument_list|()
decl_stmt|;
specifier|final
name|Pool
argument_list|<
name|V
argument_list|>
name|pool
init|=
name|poolEntry
operator|.
name|getValue
argument_list|()
decl_stmt|;
if|if
condition|(
name|pool
operator|!=
literal|null
condition|)
block|{
for|for
control|(
specifier|final
name|V
name|poolValue
range|:
name|pool
operator|.
name|values
argument_list|()
control|)
block|{
name|entries
operator|.
name|add
argument_list|(
operator|new
name|Map
operator|.
name|Entry
argument_list|<
name|K
argument_list|,
name|V
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|K
name|getKey
parameter_list|()
block|{
return|return
name|poolKey
return|;
block|}
annotation|@
name|Override
specifier|public
name|V
name|getValue
parameter_list|()
block|{
return|return
name|poolValue
return|;
block|}
annotation|@
name|Override
specifier|public
name|V
name|setValue
parameter_list|(
name|V
name|value
parameter_list|)
block|{
return|return
name|pool
operator|.
name|put
argument_list|(
name|value
argument_list|)
return|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
block|}
block|}
return|return
name|entries
return|;
block|}
specifier|protected
interface|interface
name|Pool
parameter_list|<
name|R
parameter_list|>
block|{
name|R
name|get
parameter_list|()
function_decl|;
name|R
name|put
parameter_list|(
name|R
name|resource
parameter_list|)
function_decl|;
name|boolean
name|remove
parameter_list|(
name|R
name|resource
parameter_list|)
function_decl|;
name|void
name|clear
parameter_list|()
function_decl|;
name|Collection
argument_list|<
name|R
argument_list|>
name|values
parameter_list|()
function_decl|;
name|int
name|size
parameter_list|()
function_decl|;
block|}
specifier|public
enum|enum
name|PoolType
block|{
name|Reusable
block|,
name|ThreadLocal
block|,
name|RoundRobin
block|;
specifier|public
specifier|static
name|PoolType
name|valueOf
parameter_list|(
name|String
name|poolTypeName
parameter_list|,
name|PoolType
name|defaultPoolType
parameter_list|,
name|PoolType
modifier|...
name|allowedPoolTypes
parameter_list|)
block|{
name|PoolType
name|poolType
init|=
name|PoolType
operator|.
name|fuzzyMatch
argument_list|(
name|poolTypeName
argument_list|)
decl_stmt|;
if|if
condition|(
name|poolType
operator|!=
literal|null
condition|)
block|{
name|boolean
name|allowedType
init|=
literal|false
decl_stmt|;
if|if
condition|(
name|poolType
operator|.
name|equals
argument_list|(
name|defaultPoolType
argument_list|)
condition|)
block|{
name|allowedType
operator|=
literal|true
expr_stmt|;
block|}
else|else
block|{
if|if
condition|(
name|allowedPoolTypes
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|PoolType
name|allowedPoolType
range|:
name|allowedPoolTypes
control|)
block|{
if|if
condition|(
name|poolType
operator|.
name|equals
argument_list|(
name|allowedPoolType
argument_list|)
condition|)
block|{
name|allowedType
operator|=
literal|true
expr_stmt|;
break|break;
block|}
block|}
block|}
block|}
if|if
condition|(
operator|!
name|allowedType
condition|)
block|{
name|poolType
operator|=
literal|null
expr_stmt|;
block|}
block|}
return|return
operator|(
name|poolType
operator|!=
literal|null
operator|)
condition|?
name|poolType
else|:
name|defaultPoolType
return|;
block|}
specifier|public
specifier|static
name|String
name|fuzzyNormalize
parameter_list|(
name|String
name|name
parameter_list|)
block|{
return|return
name|name
operator|!=
literal|null
condition|?
name|name
operator|.
name|replaceAll
argument_list|(
literal|"-"
argument_list|,
literal|""
argument_list|)
operator|.
name|trim
argument_list|()
operator|.
name|toLowerCase
argument_list|(
name|Locale
operator|.
name|ROOT
argument_list|)
else|:
literal|""
return|;
block|}
specifier|public
specifier|static
name|PoolType
name|fuzzyMatch
parameter_list|(
name|String
name|name
parameter_list|)
block|{
for|for
control|(
name|PoolType
name|poolType
range|:
name|values
argument_list|()
control|)
block|{
if|if
condition|(
name|fuzzyNormalize
argument_list|(
name|name
argument_list|)
operator|.
name|equals
argument_list|(
name|fuzzyNormalize
argument_list|(
name|poolType
operator|.
name|name
argument_list|()
argument_list|)
argument_list|)
condition|)
block|{
return|return
name|poolType
return|;
block|}
block|}
return|return
literal|null
return|;
block|}
block|}
specifier|protected
name|Pool
argument_list|<
name|V
argument_list|>
name|createPool
parameter_list|()
block|{
switch|switch
condition|(
name|poolType
condition|)
block|{
case|case
name|Reusable
case|:
return|return
operator|new
name|ReusablePool
argument_list|<>
argument_list|(
name|poolMaxSize
argument_list|)
return|;
case|case
name|RoundRobin
case|:
return|return
operator|new
name|RoundRobinPool
argument_list|<>
argument_list|(
name|poolMaxSize
argument_list|)
return|;
case|case
name|ThreadLocal
case|:
return|return
operator|new
name|ThreadLocalPool
argument_list|<>
argument_list|()
return|;
block|}
return|return
literal|null
return|;
block|}
comment|/**    * The<code>ReusablePool</code> represents a {@link PoolMap.Pool} that builds    * on the {@link java.util.LinkedList} class. It essentially allows resources to be    * checked out, at which point it is removed from this pool. When the resource    * is no longer required, it should be returned to the pool in order to be    * reused.    *    *<p>    * If {@link #maxSize} is set to {@link Integer#MAX_VALUE}, then the size of    * the pool is unbounded. Otherwise, it caps the number of consumers that can    * check out a resource from this pool to the (non-zero positive) value    * specified in {@link #maxSize}.    *</p>    *    * @param<R>    *          the type of the resource    */
annotation|@
name|SuppressWarnings
argument_list|(
literal|"serial"
argument_list|)
specifier|public
specifier|static
class|class
name|ReusablePool
parameter_list|<
name|R
parameter_list|>
extends|extends
name|ConcurrentLinkedQueue
argument_list|<
name|R
argument_list|>
implements|implements
name|Pool
argument_list|<
name|R
argument_list|>
block|{
specifier|private
name|int
name|maxSize
decl_stmt|;
specifier|public
name|ReusablePool
parameter_list|(
name|int
name|maxSize
parameter_list|)
block|{
name|this
operator|.
name|maxSize
operator|=
name|maxSize
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|R
name|get
parameter_list|()
block|{
return|return
name|poll
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|R
name|put
parameter_list|(
name|R
name|resource
parameter_list|)
block|{
if|if
condition|(
name|super
operator|.
name|size
argument_list|()
operator|<
name|maxSize
condition|)
block|{
name|add
argument_list|(
name|resource
argument_list|)
expr_stmt|;
block|}
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|Collection
argument_list|<
name|R
argument_list|>
name|values
parameter_list|()
block|{
return|return
name|this
return|;
block|}
block|}
comment|/**    * The<code>RoundRobinPool</code> represents a {@link PoolMap.Pool}, which    * stores its resources in an {@link ArrayList}. It load-balances access to    * its resources by returning a different resource every time a given key is    * looked up.    *    *<p>    * If {@link #maxSize} is set to {@link Integer#MAX_VALUE}, then the size of    * the pool is unbounded. Otherwise, it caps the number of resources in this    * pool to the (non-zero positive) value specified in {@link #maxSize}.    *</p>    *    * @param<R>    *          the type of the resource    *    */
annotation|@
name|SuppressWarnings
argument_list|(
literal|"serial"
argument_list|)
specifier|static
class|class
name|RoundRobinPool
parameter_list|<
name|R
parameter_list|>
extends|extends
name|CopyOnWriteArrayList
argument_list|<
name|R
argument_list|>
implements|implements
name|Pool
argument_list|<
name|R
argument_list|>
block|{
specifier|private
name|int
name|maxSize
decl_stmt|;
specifier|private
name|int
name|nextResource
init|=
literal|0
decl_stmt|;
specifier|public
name|RoundRobinPool
parameter_list|(
name|int
name|maxSize
parameter_list|)
block|{
name|this
operator|.
name|maxSize
operator|=
name|maxSize
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|R
name|put
parameter_list|(
name|R
name|resource
parameter_list|)
block|{
if|if
condition|(
name|super
operator|.
name|size
argument_list|()
operator|<
name|maxSize
condition|)
block|{
name|add
argument_list|(
name|resource
argument_list|)
expr_stmt|;
block|}
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|R
name|get
parameter_list|()
block|{
if|if
condition|(
name|super
operator|.
name|size
argument_list|()
operator|<
name|maxSize
condition|)
block|{
return|return
literal|null
return|;
block|}
name|nextResource
operator|%=
name|super
operator|.
name|size
argument_list|()
expr_stmt|;
name|R
name|resource
init|=
name|get
argument_list|(
name|nextResource
operator|++
argument_list|)
decl_stmt|;
return|return
name|resource
return|;
block|}
annotation|@
name|Override
specifier|public
name|Collection
argument_list|<
name|R
argument_list|>
name|values
parameter_list|()
block|{
return|return
name|this
return|;
block|}
block|}
comment|/**    * The<code>ThreadLocalPool</code> represents a {@link PoolMap.Pool} that    * builds on the {@link ThreadLocal} class. It essentially binds the resource    * to the thread from which it is accessed.    *    *<p>    * Note that the size of the pool is essentially bounded by the number of threads    * that add resources to this pool.    *</p>    *    * @param<R>    *          the type of the resource    */
specifier|static
class|class
name|ThreadLocalPool
parameter_list|<
name|R
parameter_list|>
extends|extends
name|ThreadLocal
argument_list|<
name|R
argument_list|>
implements|implements
name|Pool
argument_list|<
name|R
argument_list|>
block|{
specifier|private
specifier|static
specifier|final
name|Map
argument_list|<
name|ThreadLocalPool
argument_list|<
name|?
argument_list|>
argument_list|,
name|AtomicInteger
argument_list|>
name|poolSizes
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
specifier|public
name|ThreadLocalPool
parameter_list|()
block|{     }
annotation|@
name|Override
specifier|public
name|R
name|put
parameter_list|(
name|R
name|resource
parameter_list|)
block|{
name|R
name|previousResource
init|=
name|get
argument_list|()
decl_stmt|;
if|if
condition|(
name|previousResource
operator|==
literal|null
condition|)
block|{
name|AtomicInteger
name|poolSize
init|=
name|poolSizes
operator|.
name|get
argument_list|(
name|this
argument_list|)
decl_stmt|;
if|if
condition|(
name|poolSize
operator|==
literal|null
condition|)
block|{
name|poolSizes
operator|.
name|put
argument_list|(
name|this
argument_list|,
name|poolSize
operator|=
operator|new
name|AtomicInteger
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|poolSize
operator|.
name|incrementAndGet
argument_list|()
expr_stmt|;
block|}
name|this
operator|.
name|set
argument_list|(
name|resource
argument_list|)
expr_stmt|;
return|return
name|previousResource
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|remove
parameter_list|()
block|{
name|super
operator|.
name|remove
argument_list|()
expr_stmt|;
name|AtomicInteger
name|poolSize
init|=
name|poolSizes
operator|.
name|get
argument_list|(
name|this
argument_list|)
decl_stmt|;
if|if
condition|(
name|poolSize
operator|!=
literal|null
condition|)
block|{
name|poolSize
operator|.
name|decrementAndGet
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
name|AtomicInteger
name|poolSize
init|=
name|poolSizes
operator|.
name|get
argument_list|(
name|this
argument_list|)
decl_stmt|;
return|return
name|poolSize
operator|!=
literal|null
condition|?
name|poolSize
operator|.
name|get
argument_list|()
else|:
literal|0
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|remove
parameter_list|(
name|R
name|resource
parameter_list|)
block|{
name|R
name|previousResource
init|=
name|super
operator|.
name|get
argument_list|()
decl_stmt|;
if|if
condition|(
name|resource
operator|!=
literal|null
operator|&&
name|resource
operator|.
name|equals
argument_list|(
name|previousResource
argument_list|)
condition|)
block|{
name|remove
argument_list|()
expr_stmt|;
return|return
literal|true
return|;
block|}
else|else
block|{
return|return
literal|false
return|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|clear
parameter_list|()
block|{
name|super
operator|.
name|remove
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Collection
argument_list|<
name|R
argument_list|>
name|values
parameter_list|()
block|{
name|List
argument_list|<
name|R
argument_list|>
name|values
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|values
operator|.
name|add
argument_list|(
name|get
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|values
return|;
block|}
block|}
block|}
end_class

end_unit

