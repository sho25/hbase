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
name|io
operator|.
name|IOException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|InterruptedIOException
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
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|classification
operator|.
name|InterfaceAudience
import|;
end_import

begin_comment
comment|/**  * Allows multiple concurrent clients to lock on a numeric id with a minimal  * memory overhead. The intended usage is as follows:  *  *<pre>  * IdLock.Entry lockEntry = idLock.getLockEntry(id);  * try {  *   // User code.  * } finally {  *   idLock.releaseLockEntry(lockEntry);  * }</pre>  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|IdLock
block|{
comment|/** An entry returned to the client as a lock object */
specifier|public
specifier|static
class|class
name|Entry
block|{
specifier|private
specifier|final
name|long
name|id
decl_stmt|;
specifier|private
name|int
name|numWaiters
decl_stmt|;
specifier|private
name|boolean
name|isLocked
init|=
literal|true
decl_stmt|;
specifier|private
name|Entry
parameter_list|(
name|long
name|id
parameter_list|)
block|{
name|this
operator|.
name|id
operator|=
name|id
expr_stmt|;
block|}
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
literal|"id="
operator|+
name|id
operator|+
literal|", numWaiter="
operator|+
name|numWaiters
operator|+
literal|", isLocked="
operator|+
name|isLocked
return|;
block|}
block|}
specifier|private
name|ConcurrentMap
argument_list|<
name|Long
argument_list|,
name|Entry
argument_list|>
name|map
init|=
operator|new
name|ConcurrentHashMap
argument_list|<
name|Long
argument_list|,
name|Entry
argument_list|>
argument_list|()
decl_stmt|;
comment|/**    * Blocks until the lock corresponding to the given id is acquired.    *    * @param id an arbitrary number to lock on    * @return an "entry" to pass to {@link #releaseLockEntry(Entry)} to release    *         the lock    * @throws IOException if interrupted    */
specifier|public
name|Entry
name|getLockEntry
parameter_list|(
name|long
name|id
parameter_list|)
throws|throws
name|IOException
block|{
name|Entry
name|entry
init|=
operator|new
name|Entry
argument_list|(
name|id
argument_list|)
decl_stmt|;
name|Entry
name|existing
decl_stmt|;
while|while
condition|(
operator|(
name|existing
operator|=
name|map
operator|.
name|putIfAbsent
argument_list|(
name|entry
operator|.
name|id
argument_list|,
name|entry
argument_list|)
operator|)
operator|!=
literal|null
condition|)
block|{
synchronized|synchronized
init|(
name|existing
init|)
block|{
if|if
condition|(
name|existing
operator|.
name|isLocked
condition|)
block|{
operator|++
name|existing
operator|.
name|numWaiters
expr_stmt|;
comment|// Add ourselves to waiters.
while|while
condition|(
name|existing
operator|.
name|isLocked
condition|)
block|{
try|try
block|{
name|existing
operator|.
name|wait
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
operator|--
name|existing
operator|.
name|numWaiters
expr_stmt|;
comment|// Remove ourselves from waiters.
throw|throw
operator|new
name|InterruptedIOException
argument_list|(
literal|"Interrupted waiting to acquire sparse lock"
argument_list|)
throw|;
block|}
block|}
operator|--
name|existing
operator|.
name|numWaiters
expr_stmt|;
comment|// Remove ourselves from waiters.
name|existing
operator|.
name|isLocked
operator|=
literal|true
expr_stmt|;
return|return
name|existing
return|;
block|}
comment|// If the entry is not locked, it might already be deleted from the
comment|// map, so we cannot return it. We need to get our entry into the map
comment|// or get someone else's locked entry.
block|}
block|}
return|return
name|entry
return|;
block|}
comment|/**    * Must be called in a finally block to decrease the internal counter and    * remove the monitor object for the given id if the caller is the last    * client.    *    * @param entry the return value of {@link #getLockEntry(long)}    */
specifier|public
name|void
name|releaseLockEntry
parameter_list|(
name|Entry
name|entry
parameter_list|)
block|{
synchronized|synchronized
init|(
name|entry
init|)
block|{
name|entry
operator|.
name|isLocked
operator|=
literal|false
expr_stmt|;
if|if
condition|(
name|entry
operator|.
name|numWaiters
operator|>
literal|0
condition|)
block|{
name|entry
operator|.
name|notify
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|map
operator|.
name|remove
argument_list|(
name|entry
operator|.
name|id
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|/** For testing */
name|void
name|assertMapEmpty
parameter_list|()
block|{
assert|assert
name|map
operator|.
name|size
argument_list|()
operator|==
literal|0
assert|;
block|}
block|}
end_class

end_unit

