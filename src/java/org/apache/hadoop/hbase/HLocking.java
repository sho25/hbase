begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2007 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
package|;
end_package

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

begin_comment
comment|/**  * HLocking is a set of lock primitives that does not rely on a  * particular thread holding the monitor for an object. This is  * especially important when a lock must persist over multiple RPC's  * since there is no guarantee that the same Server thread will handle  * all the RPC's until the lock is released.  Not requiring that the locker  * thread is same as unlocking thread is the key distinction between this  * class and {@link java.util.concurrent.locks.ReentrantReadWriteLock}.   *  *<p>For each independent entity that needs locking, create a new HLocking  * instance.  */
end_comment

begin_class
specifier|public
class|class
name|HLocking
block|{
specifier|private
name|Integer
name|mutex
decl_stmt|;
comment|// If lockers == 0, the lock is unlocked
comment|// If lockers> 0, locked for read
comment|// If lockers == -1 locked for write
specifier|private
name|AtomicInteger
name|lockers
decl_stmt|;
comment|/** Constructor */
specifier|public
name|HLocking
parameter_list|()
block|{
name|this
operator|.
name|mutex
operator|=
operator|new
name|Integer
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|this
operator|.
name|lockers
operator|=
operator|new
name|AtomicInteger
argument_list|(
literal|0
argument_list|)
expr_stmt|;
block|}
comment|/**    * Caller needs the nonexclusive read-lock    */
specifier|public
name|void
name|obtainReadLock
parameter_list|()
block|{
synchronized|synchronized
init|(
name|mutex
init|)
block|{
while|while
condition|(
name|lockers
operator|.
name|get
argument_list|()
operator|<
literal|0
condition|)
block|{
try|try
block|{
name|mutex
operator|.
name|wait
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|ie
parameter_list|)
block|{         }
block|}
name|lockers
operator|.
name|incrementAndGet
argument_list|()
expr_stmt|;
name|mutex
operator|.
name|notifyAll
argument_list|()
expr_stmt|;
block|}
block|}
comment|/**    * Caller is finished with the nonexclusive read-lock    */
specifier|public
name|void
name|releaseReadLock
parameter_list|()
block|{
synchronized|synchronized
init|(
name|mutex
init|)
block|{
if|if
condition|(
name|lockers
operator|.
name|decrementAndGet
argument_list|()
operator|<
literal|0
condition|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"lockers: "
operator|+
name|lockers
argument_list|)
throw|;
block|}
name|mutex
operator|.
name|notifyAll
argument_list|()
expr_stmt|;
block|}
block|}
comment|/**    * Caller needs the exclusive write-lock    */
specifier|public
name|void
name|obtainWriteLock
parameter_list|()
block|{
synchronized|synchronized
init|(
name|mutex
init|)
block|{
while|while
condition|(
operator|!
name|lockers
operator|.
name|compareAndSet
argument_list|(
literal|0
argument_list|,
operator|-
literal|1
argument_list|)
condition|)
block|{
try|try
block|{
name|mutex
operator|.
name|wait
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|ie
parameter_list|)
block|{         }
block|}
name|mutex
operator|.
name|notifyAll
argument_list|()
expr_stmt|;
block|}
block|}
comment|/**    * Caller is finished with the write lock    */
specifier|public
name|void
name|releaseWriteLock
parameter_list|()
block|{
synchronized|synchronized
init|(
name|mutex
init|)
block|{
if|if
condition|(
operator|!
name|lockers
operator|.
name|compareAndSet
argument_list|(
operator|-
literal|1
argument_list|,
literal|0
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"lockers: "
operator|+
name|lockers
argument_list|)
throw|;
block|}
name|mutex
operator|.
name|notifyAll
argument_list|()
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

