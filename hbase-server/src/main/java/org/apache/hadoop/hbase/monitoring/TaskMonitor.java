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
name|monitoring
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|PrintWriter
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
name|WeakReference
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|reflect
operator|.
name|InvocationHandler
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|reflect
operator|.
name|Method
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|reflect
operator|.
name|Proxy
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
name|Iterator
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
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|collections
operator|.
name|buffer
operator|.
name|CircularFifoBuffer
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
name|classification
operator|.
name|InterfaceAudience
import|;
end_import

begin_import
import|import
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

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Lists
import|;
end_import

begin_comment
comment|/**  * Singleton which keeps track of tasks going on in this VM.  * A Task here is anything which takes more than a few seconds  * and the user might want to inquire about the status  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|TaskMonitor
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
name|TaskMonitor
operator|.
name|class
argument_list|)
decl_stmt|;
comment|// Don't keep around any tasks that have completed more than
comment|// 60 seconds ago
specifier|private
specifier|static
specifier|final
name|long
name|EXPIRATION_TIME
init|=
literal|60
operator|*
literal|1000
decl_stmt|;
annotation|@
name|VisibleForTesting
specifier|static
specifier|final
name|int
name|MAX_TASKS
init|=
literal|1000
decl_stmt|;
specifier|private
specifier|static
name|TaskMonitor
name|instance
decl_stmt|;
specifier|private
name|CircularFifoBuffer
name|tasks
init|=
operator|new
name|CircularFifoBuffer
argument_list|(
name|MAX_TASKS
argument_list|)
decl_stmt|;
comment|/**    * Get singleton instance.    * TODO this would be better off scoped to a single daemon    */
specifier|public
specifier|static
specifier|synchronized
name|TaskMonitor
name|get
parameter_list|()
block|{
if|if
condition|(
name|instance
operator|==
literal|null
condition|)
block|{
name|instance
operator|=
operator|new
name|TaskMonitor
argument_list|()
expr_stmt|;
block|}
return|return
name|instance
return|;
block|}
specifier|public
specifier|synchronized
name|MonitoredTask
name|createStatus
parameter_list|(
name|String
name|description
parameter_list|)
block|{
name|MonitoredTask
name|stat
init|=
operator|new
name|MonitoredTaskImpl
argument_list|()
decl_stmt|;
name|stat
operator|.
name|setDescription
argument_list|(
name|description
argument_list|)
expr_stmt|;
name|MonitoredTask
name|proxy
init|=
operator|(
name|MonitoredTask
operator|)
name|Proxy
operator|.
name|newProxyInstance
argument_list|(
name|stat
operator|.
name|getClass
argument_list|()
operator|.
name|getClassLoader
argument_list|()
argument_list|,
operator|new
name|Class
argument_list|<
name|?
argument_list|>
index|[]
block|{
name|MonitoredTask
operator|.
name|class
block|}
operator|,
operator|new
name|PassthroughInvocationHandler
argument_list|<
name|MonitoredTask
argument_list|>
argument_list|(
name|stat
argument_list|)
block|)
function|;
name|TaskAndWeakRefPair
name|pair
init|=
operator|new
name|TaskAndWeakRefPair
argument_list|(
name|stat
argument_list|,
name|proxy
argument_list|)
decl_stmt|;
name|tasks
operator|.
name|add
parameter_list|(
name|pair
parameter_list|)
constructor_decl|;
return|return
name|proxy
return|;
block|}
end_class

begin_function
specifier|public
specifier|synchronized
name|MonitoredRPCHandler
name|createRPCStatus
parameter_list|(
name|String
name|description
parameter_list|)
block|{
name|MonitoredRPCHandler
name|stat
init|=
operator|new
name|MonitoredRPCHandlerImpl
argument_list|()
decl_stmt|;
name|stat
operator|.
name|setDescription
argument_list|(
name|description
argument_list|)
expr_stmt|;
name|MonitoredRPCHandler
name|proxy
init|=
operator|(
name|MonitoredRPCHandler
operator|)
name|Proxy
operator|.
name|newProxyInstance
argument_list|(
name|stat
operator|.
name|getClass
argument_list|()
operator|.
name|getClassLoader
argument_list|()
argument_list|,
operator|new
name|Class
argument_list|<
name|?
argument_list|>
index|[]
block|{
name|MonitoredRPCHandler
operator|.
name|class
block|}
operator|,
operator|new
name|PassthroughInvocationHandler
argument_list|<
name|MonitoredRPCHandler
argument_list|>
argument_list|(
name|stat
argument_list|)
block|)
function|;
end_function

begin_decl_stmt
name|TaskAndWeakRefPair
name|pair
init|=
operator|new
name|TaskAndWeakRefPair
argument_list|(
name|stat
argument_list|,
name|proxy
argument_list|)
decl_stmt|;
end_decl_stmt

begin_expr_stmt
name|tasks
operator|.
name|add
argument_list|(
name|pair
argument_list|)
expr_stmt|;
end_expr_stmt

begin_return
return|return
name|proxy
return|;
end_return

begin_function
unit|}    private
specifier|synchronized
name|void
name|purgeExpiredTasks
parameter_list|()
block|{
for|for
control|(
name|Iterator
argument_list|<
name|TaskAndWeakRefPair
argument_list|>
name|it
init|=
name|tasks
operator|.
name|iterator
argument_list|()
init|;
name|it
operator|.
name|hasNext
argument_list|()
condition|;
control|)
block|{
name|TaskAndWeakRefPair
name|pair
init|=
name|it
operator|.
name|next
argument_list|()
decl_stmt|;
name|MonitoredTask
name|stat
init|=
name|pair
operator|.
name|get
argument_list|()
decl_stmt|;
if|if
condition|(
name|pair
operator|.
name|isDead
argument_list|()
condition|)
block|{
comment|// The class who constructed this leaked it. So we can
comment|// assume it's done.
if|if
condition|(
name|stat
operator|.
name|getState
argument_list|()
operator|==
name|MonitoredTaskImpl
operator|.
name|State
operator|.
name|RUNNING
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Status "
operator|+
name|stat
operator|+
literal|" appears to have been leaked"
argument_list|)
expr_stmt|;
name|stat
operator|.
name|cleanup
argument_list|()
expr_stmt|;
block|}
block|}
if|if
condition|(
name|canPurge
argument_list|(
name|stat
argument_list|)
condition|)
block|{
name|it
operator|.
name|remove
argument_list|()
expr_stmt|;
block|}
block|}
block|}
end_function

begin_comment
comment|/**    * Produces a list containing copies of the current state of all non-expired     * MonitoredTasks handled by this TaskMonitor.    * @return A complete list of MonitoredTasks.    */
end_comment

begin_function
specifier|public
specifier|synchronized
name|List
argument_list|<
name|MonitoredTask
argument_list|>
name|getTasks
parameter_list|()
block|{
name|purgeExpiredTasks
argument_list|()
expr_stmt|;
name|ArrayList
argument_list|<
name|MonitoredTask
argument_list|>
name|ret
init|=
name|Lists
operator|.
name|newArrayListWithCapacity
argument_list|(
name|tasks
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|Iterator
argument_list|<
name|TaskAndWeakRefPair
argument_list|>
name|it
init|=
name|tasks
operator|.
name|iterator
argument_list|()
init|;
name|it
operator|.
name|hasNext
argument_list|()
condition|;
control|)
block|{
name|TaskAndWeakRefPair
name|pair
init|=
name|it
operator|.
name|next
argument_list|()
decl_stmt|;
name|MonitoredTask
name|t
init|=
name|pair
operator|.
name|get
argument_list|()
decl_stmt|;
name|ret
operator|.
name|add
argument_list|(
name|t
operator|.
name|clone
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|ret
return|;
block|}
end_function

begin_function
specifier|private
name|boolean
name|canPurge
parameter_list|(
name|MonitoredTask
name|stat
parameter_list|)
block|{
name|long
name|cts
init|=
name|stat
operator|.
name|getCompletionTimestamp
argument_list|()
decl_stmt|;
return|return
operator|(
name|cts
operator|>
literal|0
operator|&&
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|-
name|cts
operator|>
name|EXPIRATION_TIME
operator|)
return|;
block|}
end_function

begin_function
specifier|public
name|void
name|dumpAsText
parameter_list|(
name|PrintWriter
name|out
parameter_list|)
block|{
name|long
name|now
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|MonitoredTask
argument_list|>
name|tasks
init|=
name|getTasks
argument_list|()
decl_stmt|;
for|for
control|(
name|MonitoredTask
name|task
range|:
name|tasks
control|)
block|{
name|out
operator|.
name|println
argument_list|(
literal|"Task: "
operator|+
name|task
operator|.
name|getDescription
argument_list|()
argument_list|)
expr_stmt|;
name|out
operator|.
name|println
argument_list|(
literal|"Status: "
operator|+
name|task
operator|.
name|getState
argument_list|()
operator|+
literal|":"
operator|+
name|task
operator|.
name|getStatus
argument_list|()
argument_list|)
expr_stmt|;
name|long
name|running
init|=
operator|(
name|now
operator|-
name|task
operator|.
name|getStartTime
argument_list|()
operator|)
operator|/
literal|1000
decl_stmt|;
if|if
condition|(
name|task
operator|.
name|getCompletionTimestamp
argument_list|()
operator|!=
operator|-
literal|1
condition|)
block|{
name|long
name|completed
init|=
operator|(
name|now
operator|-
name|task
operator|.
name|getCompletionTimestamp
argument_list|()
operator|)
operator|/
literal|1000
decl_stmt|;
name|out
operator|.
name|println
argument_list|(
literal|"Completed "
operator|+
name|completed
operator|+
literal|"s ago"
argument_list|)
expr_stmt|;
name|out
operator|.
name|println
argument_list|(
literal|"Ran for "
operator|+
operator|(
name|task
operator|.
name|getCompletionTimestamp
argument_list|()
operator|-
name|task
operator|.
name|getStartTime
argument_list|()
operator|)
operator|/
literal|1000
operator|+
literal|"s"
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|out
operator|.
name|println
argument_list|(
literal|"Running for "
operator|+
name|running
operator|+
literal|"s"
argument_list|)
expr_stmt|;
block|}
name|out
operator|.
name|println
argument_list|()
expr_stmt|;
block|}
block|}
end_function

begin_comment
comment|/**    * This class encapsulates an object as well as a weak reference to a proxy    * that passes through calls to that object. In art form:    *<code>    *     Proxy<------------------    *       |                       \    *       v                        \    * PassthroughInvocationHandler   |  weak reference    *       |                       /    * MonitoredTaskImpl            /     *       |                     /    * StatAndWeakRefProxy  ------/    *    * Since we only return the Proxy to the creator of the MonitorableStatus,    * this means that they can leak that object, and we'll detect it    * since our weak reference will go null. But, we still have the actual    * object, so we can log it and display it as a leaked (incomplete) action.    */
end_comment

begin_class
specifier|private
specifier|static
class|class
name|TaskAndWeakRefPair
block|{
specifier|private
name|MonitoredTask
name|impl
decl_stmt|;
specifier|private
name|WeakReference
argument_list|<
name|MonitoredTask
argument_list|>
name|weakProxy
decl_stmt|;
specifier|public
name|TaskAndWeakRefPair
parameter_list|(
name|MonitoredTask
name|stat
parameter_list|,
name|MonitoredTask
name|proxy
parameter_list|)
block|{
name|this
operator|.
name|impl
operator|=
name|stat
expr_stmt|;
name|this
operator|.
name|weakProxy
operator|=
operator|new
name|WeakReference
argument_list|<
name|MonitoredTask
argument_list|>
argument_list|(
name|proxy
argument_list|)
expr_stmt|;
block|}
specifier|public
name|MonitoredTask
name|get
parameter_list|()
block|{
return|return
name|impl
return|;
block|}
specifier|public
name|boolean
name|isDead
parameter_list|()
block|{
return|return
name|weakProxy
operator|.
name|get
argument_list|()
operator|==
literal|null
return|;
block|}
block|}
end_class

begin_comment
comment|/**    * An InvocationHandler that simply passes through calls to the original     * object.    */
end_comment

begin_class
specifier|private
specifier|static
class|class
name|PassthroughInvocationHandler
parameter_list|<
name|T
parameter_list|>
implements|implements
name|InvocationHandler
block|{
specifier|private
name|T
name|delegatee
decl_stmt|;
specifier|public
name|PassthroughInvocationHandler
parameter_list|(
name|T
name|delegatee
parameter_list|)
block|{
name|this
operator|.
name|delegatee
operator|=
name|delegatee
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Object
name|invoke
parameter_list|(
name|Object
name|proxy
parameter_list|,
name|Method
name|method
parameter_list|,
name|Object
index|[]
name|args
parameter_list|)
throws|throws
name|Throwable
block|{
return|return
name|method
operator|.
name|invoke
argument_list|(
name|delegatee
argument_list|,
name|args
argument_list|)
return|;
block|}
block|}
end_class

unit|}
end_unit

