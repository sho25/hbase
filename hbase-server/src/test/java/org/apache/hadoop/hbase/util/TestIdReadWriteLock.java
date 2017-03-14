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
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertTrue
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertEquals
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
name|Random
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
name|Callable
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
name|ExecutorCompletionService
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
name|ExecutorService
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
name|Executors
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
name|Future
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
name|TimeUnit
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
name|ReentrantReadWriteLock
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|Log
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|LogFactory
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
name|testclassification
operator|.
name|MediumTests
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
name|testclassification
operator|.
name|MiscTests
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Test
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|experimental
operator|.
name|categories
operator|.
name|Category
import|;
end_import

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|MiscTests
operator|.
name|class
block|,
name|MediumTests
operator|.
name|class
block|}
argument_list|)
comment|// Medium as it creates 100 threads; seems better to run it isolated
specifier|public
class|class
name|TestIdReadWriteLock
block|{
specifier|private
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|TestIdReadWriteLock
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|NUM_IDS
init|=
literal|16
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|NUM_THREADS
init|=
literal|128
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|NUM_SECONDS
init|=
literal|15
decl_stmt|;
specifier|private
name|IdReadWriteLock
name|idLock
init|=
operator|new
name|IdReadWriteLock
argument_list|()
decl_stmt|;
specifier|private
name|Map
argument_list|<
name|Long
argument_list|,
name|String
argument_list|>
name|idOwner
init|=
operator|new
name|ConcurrentHashMap
argument_list|<>
argument_list|()
decl_stmt|;
specifier|private
class|class
name|IdLockTestThread
implements|implements
name|Callable
argument_list|<
name|Boolean
argument_list|>
block|{
specifier|private
name|String
name|clientId
decl_stmt|;
specifier|public
name|IdLockTestThread
parameter_list|(
name|String
name|clientId
parameter_list|)
block|{
name|this
operator|.
name|clientId
operator|=
name|clientId
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Boolean
name|call
parameter_list|()
throws|throws
name|Exception
block|{
name|Thread
operator|.
name|currentThread
argument_list|()
operator|.
name|setName
argument_list|(
name|clientId
argument_list|)
expr_stmt|;
name|Random
name|rand
init|=
operator|new
name|Random
argument_list|()
decl_stmt|;
name|long
name|endTime
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|+
name|NUM_SECONDS
operator|*
literal|1000
decl_stmt|;
while|while
condition|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|<
name|endTime
condition|)
block|{
name|long
name|id
init|=
name|rand
operator|.
name|nextInt
argument_list|(
name|NUM_IDS
argument_list|)
decl_stmt|;
name|boolean
name|readLock
init|=
name|rand
operator|.
name|nextBoolean
argument_list|()
decl_stmt|;
name|ReentrantReadWriteLock
name|readWriteLock
init|=
name|idLock
operator|.
name|getLock
argument_list|(
name|id
argument_list|)
decl_stmt|;
name|Lock
name|lock
init|=
name|readLock
condition|?
name|readWriteLock
operator|.
name|readLock
argument_list|()
else|:
name|readWriteLock
operator|.
name|writeLock
argument_list|()
decl_stmt|;
try|try
block|{
name|lock
operator|.
name|lock
argument_list|()
expr_stmt|;
name|int
name|sleepMs
init|=
literal|1
operator|+
name|rand
operator|.
name|nextInt
argument_list|(
literal|4
argument_list|)
decl_stmt|;
name|String
name|owner
init|=
name|idOwner
operator|.
name|get
argument_list|(
name|id
argument_list|)
decl_stmt|;
if|if
condition|(
name|owner
operator|!=
literal|null
operator|&&
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
operator|(
name|readLock
condition|?
literal|"Read"
else|:
literal|"Write"
operator|)
operator|+
literal|"lock of Id "
operator|+
name|id
operator|+
literal|" already taken by "
operator|+
name|owner
operator|+
literal|", we are "
operator|+
name|clientId
argument_list|)
expr_stmt|;
block|}
name|idOwner
operator|.
name|put
argument_list|(
name|id
argument_list|,
name|clientId
argument_list|)
expr_stmt|;
name|Thread
operator|.
name|sleep
argument_list|(
name|sleepMs
argument_list|)
expr_stmt|;
name|idOwner
operator|.
name|remove
argument_list|(
name|id
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|lock
operator|.
name|unlock
argument_list|()
expr_stmt|;
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Release "
operator|+
operator|(
name|readLock
condition|?
literal|"Read"
else|:
literal|"Write"
operator|)
operator|+
literal|" lock of Id"
operator|+
name|id
operator|+
literal|", we are "
operator|+
name|clientId
argument_list|)
expr_stmt|;
block|}
block|}
block|}
return|return
literal|true
return|;
block|}
block|}
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|60000
argument_list|)
specifier|public
name|void
name|testMultipleClients
parameter_list|()
throws|throws
name|Exception
block|{
name|ExecutorService
name|exec
init|=
name|Executors
operator|.
name|newFixedThreadPool
argument_list|(
name|NUM_THREADS
argument_list|)
decl_stmt|;
try|try
block|{
name|ExecutorCompletionService
argument_list|<
name|Boolean
argument_list|>
name|ecs
init|=
operator|new
name|ExecutorCompletionService
argument_list|<>
argument_list|(
name|exec
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|NUM_THREADS
condition|;
operator|++
name|i
control|)
name|ecs
operator|.
name|submit
argument_list|(
operator|new
name|IdLockTestThread
argument_list|(
literal|"client_"
operator|+
name|i
argument_list|)
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|NUM_THREADS
condition|;
operator|++
name|i
control|)
block|{
name|Future
argument_list|<
name|Boolean
argument_list|>
name|result
init|=
name|ecs
operator|.
name|take
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
name|result
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|// make sure the entry pool won't be cleared when JVM memory is enough
comment|// even after GC and purge call
name|int
name|entryPoolSize
init|=
name|idLock
operator|.
name|purgeAndGetEntryPoolSize
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Size of entry pool after gc and purge: "
operator|+
name|entryPoolSize
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|NUM_IDS
argument_list|,
name|entryPoolSize
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|exec
operator|.
name|shutdown
argument_list|()
expr_stmt|;
name|exec
operator|.
name|awaitTermination
argument_list|(
literal|5000
argument_list|,
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

