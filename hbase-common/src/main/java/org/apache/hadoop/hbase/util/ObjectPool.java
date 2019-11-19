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
name|lang
operator|.
name|ref
operator|.
name|Reference
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|ref
operator|.
name|ReferenceQueue
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Objects
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
name|ConcurrentMap
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
name|locks
operator|.
name|Lock
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
name|locks
operator|.
name|ReentrantLock
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
comment|/**  * A thread-safe shared object pool in which object creation is expected to be lightweight, and the  * objects may be excessively created and discarded.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
specifier|abstract
class|class
name|ObjectPool
parameter_list|<
name|K
parameter_list|,
name|V
parameter_list|>
block|{
comment|/**    * An {@code ObjectFactory} object is used to create    * new shared objects on demand.    */
specifier|public
interface|interface
name|ObjectFactory
parameter_list|<
name|K
parameter_list|,
name|V
parameter_list|>
block|{
comment|/**      * Creates a new shared object associated with the given {@code key},      * identified by the {@code equals} method.      * This method may be simultaneously called by multiple threads      * with the same key, and the excessive objects are just discarded.      */
name|V
name|createObject
parameter_list|(
name|K
name|key
parameter_list|)
function_decl|;
block|}
specifier|protected
specifier|final
name|ReferenceQueue
argument_list|<
name|V
argument_list|>
name|staleRefQueue
init|=
operator|new
name|ReferenceQueue
argument_list|<>
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|ObjectFactory
argument_list|<
name|K
argument_list|,
name|V
argument_list|>
name|objectFactory
decl_stmt|;
comment|/** Does not permit null keys. */
specifier|protected
specifier|final
name|ConcurrentMap
argument_list|<
name|K
argument_list|,
name|Reference
argument_list|<
name|V
argument_list|>
argument_list|>
name|referenceCache
decl_stmt|;
comment|/** For preventing parallel purge */
specifier|private
specifier|final
name|Lock
name|purgeLock
init|=
operator|new
name|ReentrantLock
argument_list|()
decl_stmt|;
comment|/**    * The default initial capacity,    * used when not otherwise specified in a constructor.    */
specifier|public
specifier|static
specifier|final
name|int
name|DEFAULT_INITIAL_CAPACITY
init|=
literal|16
decl_stmt|;
comment|/**    * The default concurrency level,    * used when not otherwise specified in a constructor.    */
specifier|public
specifier|static
specifier|final
name|int
name|DEFAULT_CONCURRENCY_LEVEL
init|=
literal|16
decl_stmt|;
comment|/**    * Creates a new pool with the default initial capacity (16)    * and the default concurrency level (16).    *    * @param objectFactory the factory to supply new objects on demand    *    * @throws NullPointerException if {@code objectFactory} is {@code null}    */
specifier|public
name|ObjectPool
parameter_list|(
name|ObjectFactory
argument_list|<
name|K
argument_list|,
name|V
argument_list|>
name|objectFactory
parameter_list|)
block|{
name|this
argument_list|(
name|objectFactory
argument_list|,
name|DEFAULT_INITIAL_CAPACITY
argument_list|,
name|DEFAULT_CONCURRENCY_LEVEL
argument_list|)
expr_stmt|;
block|}
comment|/**    * Creates a new pool with the given initial capacity    * and the default concurrency level (16).    *    * @param objectFactory the factory to supply new objects on demand    * @param initialCapacity the initial capacity to keep objects in the pool    *    * @throws NullPointerException if {@code objectFactory} is {@code null}    * @throws IllegalArgumentException if {@code initialCapacity} is negative    */
specifier|public
name|ObjectPool
parameter_list|(
name|ObjectFactory
argument_list|<
name|K
argument_list|,
name|V
argument_list|>
name|objectFactory
parameter_list|,
name|int
name|initialCapacity
parameter_list|)
block|{
name|this
argument_list|(
name|objectFactory
argument_list|,
name|initialCapacity
argument_list|,
name|DEFAULT_CONCURRENCY_LEVEL
argument_list|)
expr_stmt|;
block|}
comment|/**    * Creates a new pool with the given initial capacity    * and the given concurrency level.    *    * @param objectFactory the factory to supply new objects on demand    * @param initialCapacity the initial capacity to keep objects in the pool    * @param concurrencyLevel the estimated count of concurrently accessing threads    *    * @throws NullPointerException if {@code objectFactory} is {@code null}    * @throws IllegalArgumentException if {@code initialCapacity} is negative or    *    {@code concurrencyLevel} is non-positive    */
specifier|public
name|ObjectPool
parameter_list|(
name|ObjectFactory
argument_list|<
name|K
argument_list|,
name|V
argument_list|>
name|objectFactory
parameter_list|,
name|int
name|initialCapacity
parameter_list|,
name|int
name|concurrencyLevel
parameter_list|)
block|{
name|this
operator|.
name|objectFactory
operator|=
name|Objects
operator|.
name|requireNonNull
argument_list|(
name|objectFactory
argument_list|,
literal|"Object factory cannot be null"
argument_list|)
expr_stmt|;
name|this
operator|.
name|referenceCache
operator|=
operator|new
name|ConcurrentHashMap
argument_list|<
name|K
argument_list|,
name|Reference
argument_list|<
name|V
argument_list|>
argument_list|>
argument_list|(
name|initialCapacity
argument_list|,
literal|0.75f
argument_list|,
name|concurrencyLevel
argument_list|)
expr_stmt|;
block|}
comment|/**    * Removes stale references of shared objects from the pool. References newly becoming stale may    * still remain.    *<p/>    * The implementation of this method is expected to be lightweight when there is no stale    * reference with the Oracle (Sun) implementation of {@code ReferenceQueue}, because    * {@code ReferenceQueue.poll} just checks a volatile instance variable in {@code ReferenceQueue}.    */
specifier|public
name|void
name|purge
parameter_list|()
block|{
if|if
condition|(
name|purgeLock
operator|.
name|tryLock
argument_list|()
condition|)
block|{
comment|// no parallel purge
try|try
block|{
while|while
condition|(
literal|true
condition|)
block|{
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
name|Reference
argument_list|<
name|V
argument_list|>
name|ref
init|=
operator|(
name|Reference
argument_list|<
name|V
argument_list|>
operator|)
name|staleRefQueue
operator|.
name|poll
argument_list|()
decl_stmt|;
if|if
condition|(
name|ref
operator|==
literal|null
condition|)
block|{
break|break;
block|}
name|referenceCache
operator|.
name|remove
argument_list|(
name|getReferenceKey
argument_list|(
name|ref
argument_list|)
argument_list|,
name|ref
argument_list|)
expr_stmt|;
block|}
block|}
finally|finally
block|{
name|purgeLock
operator|.
name|unlock
argument_list|()
expr_stmt|;
block|}
block|}
block|}
comment|/**    * Create a reference associated with the given object    * @param key the key to store in the reference    * @param obj the object to associate with    * @return the reference instance    */
specifier|public
specifier|abstract
name|Reference
argument_list|<
name|V
argument_list|>
name|createReference
parameter_list|(
name|K
name|key
parameter_list|,
name|V
name|obj
parameter_list|)
function_decl|;
comment|/**    * Get key of the given reference    * @param ref The reference    * @return key of the reference    */
specifier|public
specifier|abstract
name|K
name|getReferenceKey
parameter_list|(
name|Reference
argument_list|<
name|V
argument_list|>
name|ref
parameter_list|)
function_decl|;
comment|/**    * Returns a shared object associated with the given {@code key},    * which is identified by the {@code equals} method.    * @throws NullPointerException if {@code key} is {@code null}    */
specifier|public
name|V
name|get
parameter_list|(
name|K
name|key
parameter_list|)
block|{
name|Reference
argument_list|<
name|V
argument_list|>
name|ref
init|=
name|referenceCache
operator|.
name|get
argument_list|(
name|Objects
operator|.
name|requireNonNull
argument_list|(
name|key
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|ref
operator|!=
literal|null
condition|)
block|{
name|V
name|obj
init|=
name|ref
operator|.
name|get
argument_list|()
decl_stmt|;
if|if
condition|(
name|obj
operator|!=
literal|null
condition|)
block|{
return|return
name|obj
return|;
block|}
name|referenceCache
operator|.
name|remove
argument_list|(
name|key
argument_list|,
name|ref
argument_list|)
expr_stmt|;
block|}
name|V
name|newObj
init|=
name|objectFactory
operator|.
name|createObject
argument_list|(
name|key
argument_list|)
decl_stmt|;
name|Reference
argument_list|<
name|V
argument_list|>
name|newRef
init|=
name|createReference
argument_list|(
name|key
argument_list|,
name|newObj
argument_list|)
decl_stmt|;
while|while
condition|(
literal|true
condition|)
block|{
name|Reference
argument_list|<
name|V
argument_list|>
name|existingRef
init|=
name|referenceCache
operator|.
name|putIfAbsent
argument_list|(
name|key
argument_list|,
name|newRef
argument_list|)
decl_stmt|;
if|if
condition|(
name|existingRef
operator|==
literal|null
condition|)
block|{
return|return
name|newObj
return|;
block|}
name|V
name|existingObject
init|=
name|existingRef
operator|.
name|get
argument_list|()
decl_stmt|;
if|if
condition|(
name|existingObject
operator|!=
literal|null
condition|)
block|{
return|return
name|existingObject
return|;
block|}
name|referenceCache
operator|.
name|remove
argument_list|(
name|key
argument_list|,
name|existingRef
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Returns an estimated count of objects kept in the pool.    * This also counts stale references,    * and you might want to call {@link #purge()} beforehand.    */
specifier|public
name|int
name|size
parameter_list|()
block|{
return|return
name|referenceCache
operator|.
name|size
argument_list|()
return|;
block|}
block|}
end_class

end_unit

