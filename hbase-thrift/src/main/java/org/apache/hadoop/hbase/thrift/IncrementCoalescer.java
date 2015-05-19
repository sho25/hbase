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
name|thrift
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
name|util
operator|.
name|Arrays
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
name|LinkedBlockingQueue
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
name|ThreadFactory
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
name|ThreadPoolExecutor
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
name|atomic
operator|.
name|AtomicInteger
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
name|AtomicLong
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
name|KeyValue
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
name|client
operator|.
name|Table
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
name|thrift
operator|.
name|ThriftServerRunner
operator|.
name|HBaseHandler
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
name|thrift
operator|.
name|generated
operator|.
name|TIncrement
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
name|util
operator|.
name|Bytes
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
name|util
operator|.
name|Threads
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
name|metrics
operator|.
name|util
operator|.
name|MBeanUtil
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|TException
import|;
end_import

begin_comment
comment|/**  * This class will coalesce increments from a thift server if  * hbase.regionserver.thrift.coalesceIncrement is set to true. Turning this  * config to true will cause the thrift server to queue increments into an  * instance of this class. The thread pool associated with this class will drain  * the coalesced increments as the thread is able. This can cause data loss if the  * thrift server dies or is shut down before everything in the queue is drained.  *  */
end_comment

begin_class
specifier|public
class|class
name|IncrementCoalescer
implements|implements
name|IncrementCoalescerMBean
block|{
comment|/**    * Used to identify a cell that will be incremented.    *    */
specifier|static
class|class
name|FullyQualifiedRow
block|{
specifier|private
name|byte
index|[]
name|table
decl_stmt|;
specifier|private
name|byte
index|[]
name|rowKey
decl_stmt|;
specifier|private
name|byte
index|[]
name|family
decl_stmt|;
specifier|private
name|byte
index|[]
name|qualifier
decl_stmt|;
specifier|public
name|FullyQualifiedRow
parameter_list|(
name|byte
index|[]
name|table
parameter_list|,
name|byte
index|[]
name|rowKey
parameter_list|,
name|byte
index|[]
name|fam
parameter_list|,
name|byte
index|[]
name|qual
parameter_list|)
block|{
name|super
argument_list|()
expr_stmt|;
name|this
operator|.
name|table
operator|=
name|table
expr_stmt|;
name|this
operator|.
name|rowKey
operator|=
name|rowKey
expr_stmt|;
name|this
operator|.
name|family
operator|=
name|fam
expr_stmt|;
name|this
operator|.
name|qualifier
operator|=
name|qual
expr_stmt|;
block|}
specifier|public
name|byte
index|[]
name|getTable
parameter_list|()
block|{
return|return
name|table
return|;
block|}
specifier|public
name|void
name|setTable
parameter_list|(
name|byte
index|[]
name|table
parameter_list|)
block|{
name|this
operator|.
name|table
operator|=
name|table
expr_stmt|;
block|}
specifier|public
name|byte
index|[]
name|getRowKey
parameter_list|()
block|{
return|return
name|rowKey
return|;
block|}
specifier|public
name|void
name|setRowKey
parameter_list|(
name|byte
index|[]
name|rowKey
parameter_list|)
block|{
name|this
operator|.
name|rowKey
operator|=
name|rowKey
expr_stmt|;
block|}
specifier|public
name|byte
index|[]
name|getFamily
parameter_list|()
block|{
return|return
name|family
return|;
block|}
specifier|public
name|void
name|setFamily
parameter_list|(
name|byte
index|[]
name|fam
parameter_list|)
block|{
name|this
operator|.
name|family
operator|=
name|fam
expr_stmt|;
block|}
specifier|public
name|byte
index|[]
name|getQualifier
parameter_list|()
block|{
return|return
name|qualifier
return|;
block|}
specifier|public
name|void
name|setQualifier
parameter_list|(
name|byte
index|[]
name|qual
parameter_list|)
block|{
name|this
operator|.
name|qualifier
operator|=
name|qual
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
specifier|final
name|int
name|prime
init|=
literal|31
decl_stmt|;
name|int
name|result
init|=
literal|1
decl_stmt|;
name|result
operator|=
name|prime
operator|*
name|result
operator|+
name|Arrays
operator|.
name|hashCode
argument_list|(
name|family
argument_list|)
expr_stmt|;
name|result
operator|=
name|prime
operator|*
name|result
operator|+
name|Arrays
operator|.
name|hashCode
argument_list|(
name|qualifier
argument_list|)
expr_stmt|;
name|result
operator|=
name|prime
operator|*
name|result
operator|+
name|Arrays
operator|.
name|hashCode
argument_list|(
name|rowKey
argument_list|)
expr_stmt|;
name|result
operator|=
name|prime
operator|*
name|result
operator|+
name|Arrays
operator|.
name|hashCode
argument_list|(
name|table
argument_list|)
expr_stmt|;
return|return
name|result
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|equals
parameter_list|(
name|Object
name|obj
parameter_list|)
block|{
if|if
condition|(
name|this
operator|==
name|obj
condition|)
return|return
literal|true
return|;
if|if
condition|(
name|obj
operator|==
literal|null
condition|)
return|return
literal|false
return|;
if|if
condition|(
name|getClass
argument_list|()
operator|!=
name|obj
operator|.
name|getClass
argument_list|()
condition|)
return|return
literal|false
return|;
name|FullyQualifiedRow
name|other
init|=
operator|(
name|FullyQualifiedRow
operator|)
name|obj
decl_stmt|;
if|if
condition|(
operator|!
name|Arrays
operator|.
name|equals
argument_list|(
name|family
argument_list|,
name|other
operator|.
name|family
argument_list|)
condition|)
return|return
literal|false
return|;
if|if
condition|(
operator|!
name|Arrays
operator|.
name|equals
argument_list|(
name|qualifier
argument_list|,
name|other
operator|.
name|qualifier
argument_list|)
condition|)
return|return
literal|false
return|;
if|if
condition|(
operator|!
name|Arrays
operator|.
name|equals
argument_list|(
name|rowKey
argument_list|,
name|other
operator|.
name|rowKey
argument_list|)
condition|)
return|return
literal|false
return|;
if|if
condition|(
operator|!
name|Arrays
operator|.
name|equals
argument_list|(
name|table
argument_list|,
name|other
operator|.
name|table
argument_list|)
condition|)
return|return
literal|false
return|;
return|return
literal|true
return|;
block|}
block|}
specifier|static
class|class
name|DaemonThreadFactory
implements|implements
name|ThreadFactory
block|{
specifier|static
specifier|final
name|AtomicInteger
name|poolNumber
init|=
operator|new
name|AtomicInteger
argument_list|(
literal|1
argument_list|)
decl_stmt|;
specifier|final
name|ThreadGroup
name|group
decl_stmt|;
specifier|final
name|AtomicInteger
name|threadNumber
init|=
operator|new
name|AtomicInteger
argument_list|(
literal|1
argument_list|)
decl_stmt|;
specifier|final
name|String
name|namePrefix
decl_stmt|;
name|DaemonThreadFactory
parameter_list|()
block|{
name|SecurityManager
name|s
init|=
name|System
operator|.
name|getSecurityManager
argument_list|()
decl_stmt|;
name|group
operator|=
operator|(
name|s
operator|!=
literal|null
operator|)
condition|?
name|s
operator|.
name|getThreadGroup
argument_list|()
else|:
name|Thread
operator|.
name|currentThread
argument_list|()
operator|.
name|getThreadGroup
argument_list|()
expr_stmt|;
name|namePrefix
operator|=
literal|"ICV-"
operator|+
name|poolNumber
operator|.
name|getAndIncrement
argument_list|()
operator|+
literal|"-thread-"
expr_stmt|;
block|}
specifier|public
name|Thread
name|newThread
parameter_list|(
name|Runnable
name|r
parameter_list|)
block|{
name|Thread
name|t
init|=
operator|new
name|Thread
argument_list|(
name|group
argument_list|,
name|r
argument_list|,
name|namePrefix
operator|+
name|threadNumber
operator|.
name|getAndIncrement
argument_list|()
argument_list|,
literal|0
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|t
operator|.
name|isDaemon
argument_list|()
condition|)
name|t
operator|.
name|setDaemon
argument_list|(
literal|true
argument_list|)
expr_stmt|;
if|if
condition|(
name|t
operator|.
name|getPriority
argument_list|()
operator|!=
name|Thread
operator|.
name|NORM_PRIORITY
condition|)
name|t
operator|.
name|setPriority
argument_list|(
name|Thread
operator|.
name|NORM_PRIORITY
argument_list|)
expr_stmt|;
return|return
name|t
return|;
block|}
block|}
specifier|private
specifier|final
name|AtomicLong
name|failedIncrements
init|=
operator|new
name|AtomicLong
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|AtomicLong
name|successfulCoalescings
init|=
operator|new
name|AtomicLong
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|AtomicLong
name|totalIncrements
init|=
operator|new
name|AtomicLong
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|ConcurrentMap
argument_list|<
name|FullyQualifiedRow
argument_list|,
name|Long
argument_list|>
name|countersMap
init|=
operator|new
name|ConcurrentHashMap
argument_list|<
name|FullyQualifiedRow
argument_list|,
name|Long
argument_list|>
argument_list|(
literal|100000
argument_list|,
literal|0.75f
argument_list|,
literal|1500
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|ThreadPoolExecutor
name|pool
decl_stmt|;
specifier|private
specifier|final
name|HBaseHandler
name|handler
decl_stmt|;
specifier|private
name|int
name|maxQueueSize
init|=
literal|500000
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|CORE_POOL_SIZE
init|=
literal|1
decl_stmt|;
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
name|FullyQualifiedRow
operator|.
name|class
argument_list|)
decl_stmt|;
annotation|@
name|SuppressWarnings
argument_list|(
literal|"deprecation"
argument_list|)
specifier|public
name|IncrementCoalescer
parameter_list|(
name|HBaseHandler
name|hand
parameter_list|)
block|{
name|this
operator|.
name|handler
operator|=
name|hand
expr_stmt|;
name|LinkedBlockingQueue
argument_list|<
name|Runnable
argument_list|>
name|queue
init|=
operator|new
name|LinkedBlockingQueue
argument_list|<
name|Runnable
argument_list|>
argument_list|()
decl_stmt|;
name|pool
operator|=
operator|new
name|ThreadPoolExecutor
argument_list|(
name|CORE_POOL_SIZE
argument_list|,
name|CORE_POOL_SIZE
argument_list|,
literal|50
argument_list|,
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|,
name|queue
argument_list|,
name|Threads
operator|.
name|newDaemonThreadFactory
argument_list|(
literal|"IncrementCoalescer"
argument_list|)
argument_list|)
expr_stmt|;
name|MBeanUtil
operator|.
name|registerMBean
argument_list|(
literal|"thrift"
argument_list|,
literal|"Thrift"
argument_list|,
name|this
argument_list|)
expr_stmt|;
block|}
specifier|public
name|boolean
name|queueIncrement
parameter_list|(
name|TIncrement
name|inc
parameter_list|)
throws|throws
name|TException
block|{
if|if
condition|(
operator|!
name|canQueue
argument_list|()
condition|)
block|{
name|failedIncrements
operator|.
name|incrementAndGet
argument_list|()
expr_stmt|;
return|return
literal|false
return|;
block|}
return|return
name|internalQueueTincrement
argument_list|(
name|inc
argument_list|)
return|;
block|}
specifier|public
name|boolean
name|queueIncrements
parameter_list|(
name|List
argument_list|<
name|TIncrement
argument_list|>
name|incs
parameter_list|)
throws|throws
name|TException
block|{
if|if
condition|(
operator|!
name|canQueue
argument_list|()
condition|)
block|{
name|failedIncrements
operator|.
name|incrementAndGet
argument_list|()
expr_stmt|;
return|return
literal|false
return|;
block|}
for|for
control|(
name|TIncrement
name|tinc
range|:
name|incs
control|)
block|{
name|internalQueueTincrement
argument_list|(
name|tinc
argument_list|)
expr_stmt|;
block|}
return|return
literal|true
return|;
block|}
specifier|private
name|boolean
name|internalQueueTincrement
parameter_list|(
name|TIncrement
name|inc
parameter_list|)
throws|throws
name|TException
block|{
name|byte
index|[]
index|[]
name|famAndQf
init|=
name|KeyValue
operator|.
name|parseColumn
argument_list|(
name|inc
operator|.
name|getColumn
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|famAndQf
operator|.
name|length
operator|!=
literal|2
condition|)
return|return
literal|false
return|;
return|return
name|internalQueueIncrement
argument_list|(
name|inc
operator|.
name|getTable
argument_list|()
argument_list|,
name|inc
operator|.
name|getRow
argument_list|()
argument_list|,
name|famAndQf
index|[
literal|0
index|]
argument_list|,
name|famAndQf
index|[
literal|1
index|]
argument_list|,
name|inc
operator|.
name|getAmmount
argument_list|()
argument_list|)
return|;
block|}
specifier|private
name|boolean
name|internalQueueIncrement
parameter_list|(
name|byte
index|[]
name|tableName
parameter_list|,
name|byte
index|[]
name|rowKey
parameter_list|,
name|byte
index|[]
name|fam
parameter_list|,
name|byte
index|[]
name|qual
parameter_list|,
name|long
name|ammount
parameter_list|)
throws|throws
name|TException
block|{
name|int
name|countersMapSize
init|=
name|countersMap
operator|.
name|size
argument_list|()
decl_stmt|;
comment|//Make sure that the number of threads is scaled.
name|dynamicallySetCoreSize
argument_list|(
name|countersMapSize
argument_list|)
expr_stmt|;
name|totalIncrements
operator|.
name|incrementAndGet
argument_list|()
expr_stmt|;
name|FullyQualifiedRow
name|key
init|=
operator|new
name|FullyQualifiedRow
argument_list|(
name|tableName
argument_list|,
name|rowKey
argument_list|,
name|fam
argument_list|,
name|qual
argument_list|)
decl_stmt|;
name|long
name|currentAmount
init|=
name|ammount
decl_stmt|;
comment|// Spin until able to insert the value back without collisions
while|while
condition|(
literal|true
condition|)
block|{
name|Long
name|value
init|=
name|countersMap
operator|.
name|remove
argument_list|(
name|key
argument_list|)
decl_stmt|;
if|if
condition|(
name|value
operator|==
literal|null
condition|)
block|{
comment|// There was nothing there, create a new value
name|value
operator|=
name|Long
operator|.
name|valueOf
argument_list|(
name|currentAmount
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|value
operator|+=
name|currentAmount
expr_stmt|;
name|successfulCoalescings
operator|.
name|incrementAndGet
argument_list|()
expr_stmt|;
block|}
comment|// Try to put the value, only if there was none
name|Long
name|oldValue
init|=
name|countersMap
operator|.
name|putIfAbsent
argument_list|(
name|key
argument_list|,
name|value
argument_list|)
decl_stmt|;
if|if
condition|(
name|oldValue
operator|==
literal|null
condition|)
block|{
comment|// We were able to put it in, we're done
break|break;
block|}
comment|// Someone else was able to put a value in, so let's remember our
comment|// current value (plus what we picked up) and retry to add it in
name|currentAmount
operator|=
name|value
expr_stmt|;
block|}
comment|// We limit the size of the queue simply because all we need is a
comment|// notification that something needs to be incremented. No need
comment|// for millions of callables that mean the same thing.
if|if
condition|(
name|pool
operator|.
name|getQueue
argument_list|()
operator|.
name|size
argument_list|()
operator|<=
literal|1000
condition|)
block|{
comment|// queue it up
name|Callable
argument_list|<
name|Integer
argument_list|>
name|callable
init|=
name|createIncCallable
argument_list|()
decl_stmt|;
name|pool
operator|.
name|submit
argument_list|(
name|callable
argument_list|)
expr_stmt|;
block|}
return|return
literal|true
return|;
block|}
specifier|public
name|boolean
name|canQueue
parameter_list|()
block|{
return|return
name|countersMap
operator|.
name|size
argument_list|()
operator|<
name|maxQueueSize
return|;
block|}
specifier|private
name|Callable
argument_list|<
name|Integer
argument_list|>
name|createIncCallable
parameter_list|()
block|{
return|return
operator|new
name|Callable
argument_list|<
name|Integer
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Integer
name|call
parameter_list|()
throws|throws
name|Exception
block|{
name|int
name|failures
init|=
literal|0
decl_stmt|;
name|Set
argument_list|<
name|FullyQualifiedRow
argument_list|>
name|keys
init|=
name|countersMap
operator|.
name|keySet
argument_list|()
decl_stmt|;
for|for
control|(
name|FullyQualifiedRow
name|row
range|:
name|keys
control|)
block|{
name|Long
name|counter
init|=
name|countersMap
operator|.
name|remove
argument_list|(
name|row
argument_list|)
decl_stmt|;
if|if
condition|(
name|counter
operator|==
literal|null
condition|)
block|{
continue|continue;
block|}
try|try
block|{
name|Table
name|table
init|=
name|handler
operator|.
name|getTable
argument_list|(
name|row
operator|.
name|getTable
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|failures
operator|>
literal|2
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Auto-Fail rest of ICVs"
argument_list|)
throw|;
block|}
name|table
operator|.
name|incrementColumnValue
argument_list|(
name|row
operator|.
name|getRowKey
argument_list|()
argument_list|,
name|row
operator|.
name|getFamily
argument_list|()
argument_list|,
name|row
operator|.
name|getQualifier
argument_list|()
argument_list|,
name|counter
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
comment|// log failure of increment
name|failures
operator|++
expr_stmt|;
name|LOG
operator|.
name|error
argument_list|(
literal|"FAILED_ICV: "
operator|+
name|Bytes
operator|.
name|toString
argument_list|(
name|row
operator|.
name|getTable
argument_list|()
argument_list|)
operator|+
literal|", "
operator|+
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|row
operator|.
name|getRowKey
argument_list|()
argument_list|)
operator|+
literal|", "
operator|+
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|row
operator|.
name|getFamily
argument_list|()
argument_list|)
operator|+
literal|", "
operator|+
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|row
operator|.
name|getQualifier
argument_list|()
argument_list|)
operator|+
literal|", "
operator|+
name|counter
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|failures
return|;
block|}
block|}
return|;
block|}
comment|/**    * This method samples the incoming requests and, if selected, will check if    * the corePoolSize should be changed.    * @param countersMapSize    */
specifier|private
name|void
name|dynamicallySetCoreSize
parameter_list|(
name|int
name|countersMapSize
parameter_list|)
block|{
comment|// Here we are using countersMapSize as a random number, meaning this
comment|// could be a Random object
if|if
condition|(
name|countersMapSize
operator|%
literal|10
operator|!=
literal|0
condition|)
block|{
return|return;
block|}
name|double
name|currentRatio
init|=
operator|(
name|double
operator|)
name|countersMapSize
operator|/
operator|(
name|double
operator|)
name|maxQueueSize
decl_stmt|;
name|int
name|newValue
init|=
literal|1
decl_stmt|;
if|if
condition|(
name|currentRatio
operator|<
literal|0.1
condition|)
block|{
comment|// it's 1
block|}
elseif|else
if|if
condition|(
name|currentRatio
operator|<
literal|0.3
condition|)
block|{
name|newValue
operator|=
literal|2
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|currentRatio
operator|<
literal|0.5
condition|)
block|{
name|newValue
operator|=
literal|4
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|currentRatio
operator|<
literal|0.7
condition|)
block|{
name|newValue
operator|=
literal|8
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|currentRatio
operator|<
literal|0.9
condition|)
block|{
name|newValue
operator|=
literal|14
expr_stmt|;
block|}
else|else
block|{
name|newValue
operator|=
literal|22
expr_stmt|;
block|}
if|if
condition|(
name|pool
operator|.
name|getCorePoolSize
argument_list|()
operator|!=
name|newValue
condition|)
block|{
name|pool
operator|.
name|setCorePoolSize
argument_list|(
name|newValue
argument_list|)
expr_stmt|;
block|}
block|}
comment|// MBean get/set methods
specifier|public
name|int
name|getQueueSize
parameter_list|()
block|{
return|return
name|pool
operator|.
name|getQueue
argument_list|()
operator|.
name|size
argument_list|()
return|;
block|}
specifier|public
name|int
name|getMaxQueueSize
parameter_list|()
block|{
return|return
name|this
operator|.
name|maxQueueSize
return|;
block|}
specifier|public
name|void
name|setMaxQueueSize
parameter_list|(
name|int
name|newSize
parameter_list|)
block|{
name|this
operator|.
name|maxQueueSize
operator|=
name|newSize
expr_stmt|;
block|}
specifier|public
name|long
name|getPoolCompletedTaskCount
parameter_list|()
block|{
return|return
name|pool
operator|.
name|getCompletedTaskCount
argument_list|()
return|;
block|}
specifier|public
name|long
name|getPoolTaskCount
parameter_list|()
block|{
return|return
name|pool
operator|.
name|getTaskCount
argument_list|()
return|;
block|}
specifier|public
name|int
name|getPoolLargestPoolSize
parameter_list|()
block|{
return|return
name|pool
operator|.
name|getLargestPoolSize
argument_list|()
return|;
block|}
specifier|public
name|int
name|getCorePoolSize
parameter_list|()
block|{
return|return
name|pool
operator|.
name|getCorePoolSize
argument_list|()
return|;
block|}
specifier|public
name|void
name|setCorePoolSize
parameter_list|(
name|int
name|newCoreSize
parameter_list|)
block|{
name|pool
operator|.
name|setCorePoolSize
argument_list|(
name|newCoreSize
argument_list|)
expr_stmt|;
block|}
specifier|public
name|int
name|getMaxPoolSize
parameter_list|()
block|{
return|return
name|pool
operator|.
name|getMaximumPoolSize
argument_list|()
return|;
block|}
specifier|public
name|void
name|setMaxPoolSize
parameter_list|(
name|int
name|newMaxSize
parameter_list|)
block|{
name|pool
operator|.
name|setMaximumPoolSize
argument_list|(
name|newMaxSize
argument_list|)
expr_stmt|;
block|}
specifier|public
name|long
name|getFailedIncrements
parameter_list|()
block|{
return|return
name|failedIncrements
operator|.
name|get
argument_list|()
return|;
block|}
specifier|public
name|long
name|getSuccessfulCoalescings
parameter_list|()
block|{
return|return
name|successfulCoalescings
operator|.
name|get
argument_list|()
return|;
block|}
specifier|public
name|long
name|getTotalIncrements
parameter_list|()
block|{
return|return
name|totalIncrements
operator|.
name|get
argument_list|()
return|;
block|}
specifier|public
name|long
name|getCountersMapSize
parameter_list|()
block|{
return|return
name|countersMap
operator|.
name|size
argument_list|()
return|;
block|}
block|}
end_class

end_unit

