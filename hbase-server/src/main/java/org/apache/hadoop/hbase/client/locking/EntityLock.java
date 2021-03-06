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
name|client
operator|.
name|locking
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
name|concurrent
operator|.
name|CountDownLatch
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
name|AtomicBoolean
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
name|conf
operator|.
name|Configuration
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
name|Abortable
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
name|slf4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|LoggerFactory
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
name|shaded
operator|.
name|protobuf
operator|.
name|ProtobufUtil
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
name|shaded
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|LockServiceProtos
operator|.
name|LockHeartbeatRequest
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
name|shaded
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|LockServiceProtos
operator|.
name|LockHeartbeatResponse
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
name|shaded
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|LockServiceProtos
operator|.
name|LockRequest
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
name|shaded
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|LockServiceProtos
operator|.
name|LockService
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
comment|/**  * Lock for HBase Entity either a Table, a Namespace, or Regions.  *  * These are remote locks which live on master, and need periodic heartbeats to keep them alive.  * (Once we request the lock, internally an heartbeat thread will be started on the client).  * If master does not receive the heartbeat in time, it'll release the lock and make it available  * to other users.  *  *<p>Use {@link LockServiceClient} to build instances. Then call {@link #requestLock()}.  * {@link #requestLock} will contact master to queue the lock and start the heartbeat thread  * which will check lock's status periodically and once the lock is acquired, it will send the  * heartbeats to the master.  *  *<p>Use {@link #await} or {@link #await(long, TimeUnit)} to wait for the lock to be acquired.  * Always call {@link #unlock()} irrespective of whether lock was acquired or not. If the lock  * was acquired, it'll be released. If it was not acquired, it is possible that master grants the  * lock in future and the heartbeat thread keeps it alive forever by sending heartbeats.  * Calling {@link #unlock()} will stop the heartbeat thread and cancel the lock queued on master.  *  *<p>There are 4 ways in which these remote locks may be released/can be lost:  *<ul><li>Call {@link #unlock}.</li>  *<li>Lock times out on master: Can happen because of network issues, GC pauses, etc.  *     Worker thread will call the given abortable as soon as it detects such a situation.</li>  *<li>Fail to contact master: If worker thread can not contact mater and thus fails to send  *     heartbeat before the timeout expires, it assumes that lock is lost and calls the  *     abortable.</li>  *<li>Worker thread is interrupted.</li>  *</ul>  *  * Use example:  *<code>  * EntityLock lock = lockServiceClient.*Lock(...., "exampled lock", abortable);  * lock.requestLock();  * ....  * ....can do other initializations here since lock is 'asynchronous'...  * ....  * if (lock.await(timeout)) {  *   ....logic requiring mutual exclusion  * }  * lock.unlock();  *</code>  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Public
specifier|public
class|class
name|EntityLock
block|{
specifier|private
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|EntityLock
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|HEARTBEAT_TIME_BUFFER
init|=
literal|"hbase.client.locks.heartbeat.time.buffer.ms"
decl_stmt|;
specifier|private
specifier|final
name|AtomicBoolean
name|locked
init|=
operator|new
name|AtomicBoolean
argument_list|(
literal|false
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|CountDownLatch
name|latch
init|=
operator|new
name|CountDownLatch
argument_list|(
literal|1
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|LockService
operator|.
name|BlockingInterface
name|stub
decl_stmt|;
specifier|private
specifier|final
name|LockHeartbeatWorker
name|worker
decl_stmt|;
specifier|private
specifier|final
name|LockRequest
name|lockRequest
decl_stmt|;
specifier|private
specifier|final
name|Abortable
name|abort
decl_stmt|;
comment|// Buffer for unexpected delays (GC, network delay, etc) in heartbeat rpc.
specifier|private
specifier|final
name|int
name|heartbeatTimeBuffer
decl_stmt|;
comment|// set to a non-zero value for tweaking sleep time during testing so that worker doesn't wait
comment|// for long time periods between heartbeats.
specifier|private
name|long
name|testingSleepTime
init|=
literal|0
decl_stmt|;
specifier|private
name|Long
name|procId
init|=
literal|null
decl_stmt|;
comment|/**    * Abortable.abort() is called when the lease of the lock will expire.    * It's up to the user decide if simply abort the process or handle the loss of the lock    * by aborting the operation that was supposed to be under lock.    */
name|EntityLock
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|LockService
operator|.
name|BlockingInterface
name|stub
parameter_list|,
name|LockRequest
name|request
parameter_list|,
name|Abortable
name|abort
parameter_list|)
block|{
name|this
operator|.
name|stub
operator|=
name|stub
expr_stmt|;
name|this
operator|.
name|lockRequest
operator|=
name|request
expr_stmt|;
name|this
operator|.
name|abort
operator|=
name|abort
expr_stmt|;
name|this
operator|.
name|heartbeatTimeBuffer
operator|=
name|conf
operator|.
name|getInt
argument_list|(
name|HEARTBEAT_TIME_BUFFER
argument_list|,
literal|10000
argument_list|)
expr_stmt|;
name|this
operator|.
name|worker
operator|=
operator|new
name|LockHeartbeatWorker
argument_list|(
name|lockRequest
operator|.
name|getDescription
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
specifier|final
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"EntityLock locked="
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|locked
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|", procId="
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|procId
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|", type="
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|lockRequest
operator|.
name|getLockType
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|lockRequest
operator|.
name|getRegionInfoCount
argument_list|()
operator|>
literal|0
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|", regions="
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
name|lockRequest
operator|.
name|getRegionInfoCount
argument_list|()
condition|;
operator|++
name|i
control|)
block|{
if|if
condition|(
name|i
operator|>
literal|0
condition|)
name|sb
operator|.
name|append
argument_list|(
literal|", "
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|lockRequest
operator|.
name|getRegionInfo
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|lockRequest
operator|.
name|hasTableName
argument_list|()
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|", table="
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|lockRequest
operator|.
name|getTableName
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|lockRequest
operator|.
name|hasNamespace
argument_list|()
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|", namespace="
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|lockRequest
operator|.
name|getNamespace
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|sb
operator|.
name|append
argument_list|(
literal|", description="
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|lockRequest
operator|.
name|getDescription
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|sb
operator|.
name|toString
argument_list|()
return|;
block|}
annotation|@
name|VisibleForTesting
name|void
name|setTestingSleepTime
parameter_list|(
name|long
name|timeInMillis
parameter_list|)
block|{
name|testingSleepTime
operator|=
name|timeInMillis
expr_stmt|;
block|}
annotation|@
name|VisibleForTesting
name|LockHeartbeatWorker
name|getWorker
parameter_list|()
block|{
return|return
name|worker
return|;
block|}
specifier|public
name|boolean
name|isLocked
parameter_list|()
block|{
return|return
name|locked
operator|.
name|get
argument_list|()
return|;
block|}
comment|/**    * Sends rpc to the master to request lock.    * The lock request is queued with other lock requests.    * Call {@link #await()} to wait on lock.    * Always call {@link #unlock()} after calling the below, even after error.    */
specifier|public
name|void
name|requestLock
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|procId
operator|==
literal|null
condition|)
block|{
try|try
block|{
name|procId
operator|=
name|stub
operator|.
name|requestLock
argument_list|(
literal|null
argument_list|,
name|lockRequest
argument_list|)
operator|.
name|getProcId
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
name|ProtobufUtil
operator|.
name|handleRemoteException
argument_list|(
name|e
argument_list|)
throw|;
block|}
name|worker
operator|.
name|start
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Lock already queued : "
operator|+
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * @param timeout in milliseconds. If set to 0, waits indefinitely.    * @return true if lock was acquired; and false if waiting time elapsed before lock could be    * acquired.    */
specifier|public
name|boolean
name|await
parameter_list|(
name|long
name|timeout
parameter_list|,
name|TimeUnit
name|timeUnit
parameter_list|)
throws|throws
name|InterruptedException
block|{
specifier|final
name|boolean
name|result
init|=
name|latch
operator|.
name|await
argument_list|(
name|timeout
argument_list|,
name|timeUnit
argument_list|)
decl_stmt|;
name|String
name|lockRequestStr
init|=
name|lockRequest
operator|.
name|toString
argument_list|()
operator|.
name|replace
argument_list|(
literal|"\n"
argument_list|,
literal|", "
argument_list|)
decl_stmt|;
if|if
condition|(
name|result
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Acquired "
operator|+
name|lockRequestStr
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|LOG
operator|.
name|info
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"Failed acquire in %s %s of %s"
argument_list|,
name|timeout
argument_list|,
name|timeUnit
operator|.
name|toString
argument_list|()
argument_list|,
name|lockRequestStr
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|result
return|;
block|}
specifier|public
name|void
name|await
parameter_list|()
throws|throws
name|InterruptedException
block|{
name|latch
operator|.
name|await
argument_list|()
expr_stmt|;
block|}
specifier|public
name|void
name|unlock
parameter_list|()
throws|throws
name|IOException
block|{
name|Threads
operator|.
name|shutdown
argument_list|(
name|worker
operator|.
name|shutdown
argument_list|()
argument_list|)
expr_stmt|;
try|try
block|{
name|stub
operator|.
name|lockHeartbeat
argument_list|(
literal|null
argument_list|,
name|LockHeartbeatRequest
operator|.
name|newBuilder
argument_list|()
operator|.
name|setProcId
argument_list|(
name|procId
argument_list|)
operator|.
name|setKeepAlive
argument_list|(
literal|false
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
name|ProtobufUtil
operator|.
name|handleRemoteException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
specifier|protected
class|class
name|LockHeartbeatWorker
extends|extends
name|Thread
block|{
specifier|private
specifier|volatile
name|boolean
name|shutdown
init|=
literal|false
decl_stmt|;
specifier|public
name|LockHeartbeatWorker
parameter_list|(
specifier|final
name|String
name|desc
parameter_list|)
block|{
name|super
argument_list|(
literal|"LockHeartbeatWorker("
operator|+
name|desc
operator|+
literal|")"
argument_list|)
expr_stmt|;
name|setDaemon
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
comment|/**      * @return Shuts down the thread clean and quietly.      */
name|Thread
name|shutdown
parameter_list|()
block|{
name|shutdown
operator|=
literal|true
expr_stmt|;
name|interrupt
argument_list|()
expr_stmt|;
return|return
name|this
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
specifier|final
name|LockHeartbeatRequest
name|lockHeartbeatRequest
init|=
name|LockHeartbeatRequest
operator|.
name|newBuilder
argument_list|()
operator|.
name|setProcId
argument_list|(
name|procId
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|LockHeartbeatResponse
name|response
decl_stmt|;
while|while
condition|(
literal|true
condition|)
block|{
try|try
block|{
name|response
operator|=
name|stub
operator|.
name|lockHeartbeat
argument_list|(
literal|null
argument_list|,
name|lockHeartbeatRequest
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|e
operator|=
name|ProtobufUtil
operator|.
name|handleRemoteException
argument_list|(
name|e
argument_list|)
expr_stmt|;
name|locked
operator|.
name|set
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|error
argument_list|(
literal|"Heartbeat failed, releasing "
operator|+
name|EntityLock
operator|.
name|this
argument_list|,
name|e
argument_list|)
expr_stmt|;
name|abort
operator|.
name|abort
argument_list|(
literal|"Heartbeat failed"
argument_list|,
name|e
argument_list|)
expr_stmt|;
return|return;
block|}
if|if
condition|(
operator|!
name|isLocked
argument_list|()
operator|&&
name|response
operator|.
name|getLockStatus
argument_list|()
operator|==
name|LockHeartbeatResponse
operator|.
name|LockStatus
operator|.
name|LOCKED
condition|)
block|{
name|locked
operator|.
name|set
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|latch
operator|.
name|countDown
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|isLocked
argument_list|()
operator|&&
name|response
operator|.
name|getLockStatus
argument_list|()
operator|==
name|LockHeartbeatResponse
operator|.
name|LockStatus
operator|.
name|UNLOCKED
condition|)
block|{
comment|// Lock timed out.
name|locked
operator|.
name|set
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|abort
operator|.
name|abort
argument_list|(
literal|"Lock timed out."
argument_list|,
literal|null
argument_list|)
expr_stmt|;
return|return;
block|}
try|try
block|{
comment|// If lock not acquired yet, poll faster so we can notify faster.
name|long
name|sleepTime
init|=
literal|1000
decl_stmt|;
if|if
condition|(
name|isLocked
argument_list|()
condition|)
block|{
comment|// If lock acquired, then use lock timeout to determine heartbeat rate.
comment|// If timeout is<heartbeatTimeBuffer, send back to back heartbeats.
name|sleepTime
operator|=
name|Math
operator|.
name|max
argument_list|(
name|response
operator|.
name|getTimeoutMs
argument_list|()
operator|-
name|heartbeatTimeBuffer
argument_list|,
literal|1
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|testingSleepTime
operator|!=
literal|0
condition|)
block|{
name|sleepTime
operator|=
name|testingSleepTime
expr_stmt|;
block|}
name|Thread
operator|.
name|sleep
argument_list|(
name|sleepTime
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
comment|// Since there won't be any more heartbeats, assume lock will be lost.
name|locked
operator|.
name|set
argument_list|(
literal|false
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|this
operator|.
name|shutdown
condition|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Interrupted, releasing "
operator|+
name|this
argument_list|,
name|e
argument_list|)
expr_stmt|;
name|abort
operator|.
name|abort
argument_list|(
literal|"Worker thread interrupted"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
return|return;
block|}
block|}
block|}
block|}
block|}
end_class

end_unit

