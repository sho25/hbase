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
name|zookeeper
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
name|CountDownLatch
import|;
end_import

begin_comment
comment|/**  * Placeholder of an instance which will be accessed by other threads  * but is not yet created. Thread safe.  */
end_comment

begin_class
class|class
name|InstancePending
parameter_list|<
name|T
parameter_list|>
block|{
comment|// Based on a subtle part of the Java Language Specification,
comment|// in order to avoid a slight overhead of synchronization for each access.
specifier|private
specifier|final
name|CountDownLatch
name|pendingLatch
init|=
operator|new
name|CountDownLatch
argument_list|(
literal|1
argument_list|)
decl_stmt|;
comment|/** Piggybacking on {@code pendingLatch}. */
specifier|private
name|InstanceHolder
argument_list|<
name|T
argument_list|>
name|instanceHolder
decl_stmt|;
specifier|private
specifier|static
class|class
name|InstanceHolder
parameter_list|<
name|T
parameter_list|>
block|{
comment|// The JLS ensures the visibility of a final field and its contents
comment|// unless they are exposed to another thread while the construction.
specifier|final
name|T
name|instance
decl_stmt|;
name|InstanceHolder
parameter_list|(
name|T
name|instance
parameter_list|)
block|{
name|this
operator|.
name|instance
operator|=
name|instance
expr_stmt|;
block|}
block|}
comment|/**    * Returns the instance given by the method {@link #prepare}.    * This is an interruptible blocking method    * and the interruption flag will be set just before returning if any.    */
name|T
name|get
parameter_list|()
block|{
name|InstanceHolder
argument_list|<
name|T
argument_list|>
name|instanceHolder
decl_stmt|;
name|boolean
name|interrupted
init|=
literal|false
decl_stmt|;
while|while
condition|(
operator|(
name|instanceHolder
operator|=
name|this
operator|.
name|instanceHolder
operator|)
operator|==
literal|null
condition|)
block|{
try|try
block|{
name|pendingLatch
operator|.
name|await
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
name|interrupted
operator|=
literal|true
expr_stmt|;
block|}
block|}
if|if
condition|(
name|interrupted
condition|)
block|{
name|Thread
operator|.
name|currentThread
argument_list|()
operator|.
name|interrupt
argument_list|()
expr_stmt|;
block|}
return|return
name|instanceHolder
operator|.
name|instance
return|;
block|}
comment|/**    * Associates the given instance for the method {@link #get}.    * This method should be called once, and {@code instance} should be non-null.    * This method is expected to call as soon as possible    * because the method {@code get} is uninterruptibly blocked until this method is called.    */
name|void
name|prepare
parameter_list|(
name|T
name|instance
parameter_list|)
block|{
assert|assert
name|instance
operator|!=
literal|null
assert|;
name|instanceHolder
operator|=
operator|new
name|InstanceHolder
argument_list|<
name|T
argument_list|>
argument_list|(
name|instance
argument_list|)
expr_stmt|;
name|pendingLatch
operator|.
name|countDown
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

