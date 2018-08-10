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
name|util
operator|.
name|concurrent
operator|.
name|locks
operator|.
name|ReentrantReadWriteLock
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
name|annotations
operator|.
name|VisibleForTesting
import|;
end_import

begin_comment
comment|/**  * Allows multiple concurrent clients to lock on a numeric id with ReentrantReadWriteLock. The  * intended usage for read lock is as follows:  *  *<pre>  * ReentrantReadWriteLock lock = idReadWriteLock.getLock(id);  * try {  *   lock.readLock().lock();  *   // User code.  * } finally {  *   lock.readLock().unlock();  * }  *</pre>  *  * For write lock, use lock.writeLock()  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|IdReadWriteLock
parameter_list|<
name|T
parameter_list|>
block|{
comment|// The number of lock we want to easily support. It's not a maximum.
specifier|private
specifier|static
specifier|final
name|int
name|NB_CONCURRENT_LOCKS
init|=
literal|1000
decl_stmt|;
comment|/**    * The pool to get entry from, entries are mapped by {@link Reference} and will be automatically    * garbage-collected by JVM    */
specifier|private
specifier|final
name|ObjectPool
argument_list|<
name|T
argument_list|,
name|ReentrantReadWriteLock
argument_list|>
name|lockPool
decl_stmt|;
specifier|private
specifier|final
name|ReferenceType
name|refType
decl_stmt|;
specifier|public
name|IdReadWriteLock
parameter_list|()
block|{
name|this
argument_list|(
name|ReferenceType
operator|.
name|WEAK
argument_list|)
expr_stmt|;
block|}
comment|/**    * Constructor of IdReadWriteLock    * @param referenceType type of the reference used in lock pool, {@link ReferenceType#WEAK} by    *          default. Use {@link ReferenceType#SOFT} if the key set is limited and the locks will    *          be reused with a high frequency    */
specifier|public
name|IdReadWriteLock
parameter_list|(
name|ReferenceType
name|referenceType
parameter_list|)
block|{
name|this
operator|.
name|refType
operator|=
name|referenceType
expr_stmt|;
switch|switch
condition|(
name|referenceType
condition|)
block|{
case|case
name|SOFT
case|:
name|lockPool
operator|=
operator|new
name|SoftObjectPool
argument_list|<>
argument_list|(
operator|new
name|ObjectPool
operator|.
name|ObjectFactory
argument_list|<
name|T
argument_list|,
name|ReentrantReadWriteLock
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|ReentrantReadWriteLock
name|createObject
parameter_list|(
name|T
name|id
parameter_list|)
block|{
return|return
operator|new
name|ReentrantReadWriteLock
argument_list|()
return|;
block|}
block|}
argument_list|,
name|NB_CONCURRENT_LOCKS
argument_list|)
expr_stmt|;
break|break;
case|case
name|WEAK
case|:
default|default:
name|lockPool
operator|=
operator|new
name|WeakObjectPool
argument_list|<>
argument_list|(
operator|new
name|ObjectPool
operator|.
name|ObjectFactory
argument_list|<
name|T
argument_list|,
name|ReentrantReadWriteLock
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|ReentrantReadWriteLock
name|createObject
parameter_list|(
name|T
name|id
parameter_list|)
block|{
return|return
operator|new
name|ReentrantReadWriteLock
argument_list|()
return|;
block|}
block|}
argument_list|,
name|NB_CONCURRENT_LOCKS
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
specifier|static
enum|enum
name|ReferenceType
block|{
name|WEAK
block|,
name|SOFT
block|}
comment|/**    * Get the ReentrantReadWriteLock corresponding to the given id    * @param id an arbitrary number to identify the lock    */
specifier|public
name|ReentrantReadWriteLock
name|getLock
parameter_list|(
name|T
name|id
parameter_list|)
block|{
name|lockPool
operator|.
name|purge
argument_list|()
expr_stmt|;
name|ReentrantReadWriteLock
name|readWriteLock
init|=
name|lockPool
operator|.
name|get
argument_list|(
name|id
argument_list|)
decl_stmt|;
return|return
name|readWriteLock
return|;
block|}
comment|/** For testing */
annotation|@
name|VisibleForTesting
name|int
name|purgeAndGetEntryPoolSize
parameter_list|()
block|{
name|gc
argument_list|()
expr_stmt|;
name|Threads
operator|.
name|sleep
argument_list|(
literal|200
argument_list|)
expr_stmt|;
name|lockPool
operator|.
name|purge
argument_list|()
expr_stmt|;
return|return
name|lockPool
operator|.
name|size
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
literal|"DM_GC"
argument_list|,
name|justification
operator|=
literal|"Intentional"
argument_list|)
specifier|private
name|void
name|gc
parameter_list|()
block|{
name|System
operator|.
name|gc
argument_list|()
expr_stmt|;
block|}
annotation|@
name|VisibleForTesting
specifier|public
name|void
name|waitForWaiters
parameter_list|(
name|T
name|id
parameter_list|,
name|int
name|numWaiters
parameter_list|)
throws|throws
name|InterruptedException
block|{
for|for
control|(
name|ReentrantReadWriteLock
name|readWriteLock
init|;
condition|;
control|)
block|{
name|readWriteLock
operator|=
name|lockPool
operator|.
name|get
argument_list|(
name|id
argument_list|)
expr_stmt|;
if|if
condition|(
name|readWriteLock
operator|!=
literal|null
condition|)
block|{
synchronized|synchronized
init|(
name|readWriteLock
init|)
block|{
if|if
condition|(
name|readWriteLock
operator|.
name|getQueueLength
argument_list|()
operator|>=
name|numWaiters
condition|)
block|{
return|return;
block|}
block|}
block|}
name|Thread
operator|.
name|sleep
argument_list|(
literal|50
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|VisibleForTesting
specifier|public
name|ReferenceType
name|getReferenceType
parameter_list|()
block|{
return|return
name|this
operator|.
name|refType
return|;
block|}
block|}
end_class

end_unit

