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
name|master
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
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|TableName
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
name|RegionInfo
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
name|master
operator|.
name|HMaster
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
name|procedure2
operator|.
name|LockType
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
name|NonceKey
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
comment|/**  * Functions to acquire lock on table/namespace/regions.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
specifier|final
class|class
name|LockManager
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
name|LockManager
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|HMaster
name|master
decl_stmt|;
specifier|private
specifier|final
name|RemoteLocks
name|remoteLocks
decl_stmt|;
specifier|public
name|LockManager
parameter_list|(
name|HMaster
name|master
parameter_list|)
block|{
name|this
operator|.
name|master
operator|=
name|master
expr_stmt|;
name|this
operator|.
name|remoteLocks
operator|=
operator|new
name|RemoteLocks
argument_list|()
expr_stmt|;
block|}
specifier|public
name|RemoteLocks
name|remoteLocks
parameter_list|()
block|{
return|return
name|remoteLocks
return|;
block|}
specifier|public
name|MasterLock
name|createMasterLock
parameter_list|(
specifier|final
name|String
name|namespace
parameter_list|,
specifier|final
name|LockType
name|type
parameter_list|,
specifier|final
name|String
name|description
parameter_list|)
block|{
return|return
operator|new
name|MasterLock
argument_list|(
name|namespace
argument_list|,
name|type
argument_list|,
name|description
argument_list|)
return|;
block|}
specifier|public
name|MasterLock
name|createMasterLock
parameter_list|(
specifier|final
name|TableName
name|tableName
parameter_list|,
specifier|final
name|LockType
name|type
parameter_list|,
specifier|final
name|String
name|description
parameter_list|)
block|{
return|return
operator|new
name|MasterLock
argument_list|(
name|tableName
argument_list|,
name|type
argument_list|,
name|description
argument_list|)
return|;
block|}
specifier|public
name|MasterLock
name|createMasterLock
parameter_list|(
specifier|final
name|RegionInfo
index|[]
name|regionInfos
parameter_list|,
specifier|final
name|String
name|description
parameter_list|)
block|{
return|return
operator|new
name|MasterLock
argument_list|(
name|regionInfos
argument_list|,
name|description
argument_list|)
return|;
block|}
specifier|private
name|void
name|submitProcedure
parameter_list|(
specifier|final
name|LockProcedure
name|proc
parameter_list|,
specifier|final
name|NonceKey
name|nonceKey
parameter_list|)
block|{
name|proc
operator|.
name|setOwner
argument_list|(
name|master
operator|.
name|getMasterProcedureExecutor
argument_list|()
operator|.
name|getEnvironment
argument_list|()
operator|.
name|getRequestUser
argument_list|()
argument_list|)
expr_stmt|;
name|master
operator|.
name|getMasterProcedureExecutor
argument_list|()
operator|.
name|submitProcedure
argument_list|(
name|proc
argument_list|,
name|nonceKey
argument_list|)
expr_stmt|;
block|}
comment|/**    * Locks on namespace/table/regions.    * Underneath, uses procedure framework and queues a {@link LockProcedure} which waits in a    * queue until scheduled.    * Use this lock instead LockManager.remoteLocks() for MASTER ONLY operations for two advantages:    * - no need of polling on LockProcedure to check if lock was acquired.    * - Generous timeout for lock preemption (default 10 min), no need to spawn thread for heartbeats.    * (timeout configuration {@link LockProcedure#DEFAULT_LOCAL_MASTER_LOCKS_TIMEOUT_MS}).    */
specifier|public
class|class
name|MasterLock
block|{
specifier|private
specifier|final
name|String
name|namespace
decl_stmt|;
specifier|private
specifier|final
name|TableName
name|tableName
decl_stmt|;
specifier|private
specifier|final
name|RegionInfo
index|[]
name|regionInfos
decl_stmt|;
specifier|private
specifier|final
name|LockType
name|type
decl_stmt|;
specifier|private
specifier|final
name|String
name|description
decl_stmt|;
specifier|private
name|LockProcedure
name|proc
init|=
literal|null
decl_stmt|;
specifier|public
name|MasterLock
parameter_list|(
specifier|final
name|String
name|namespace
parameter_list|,
specifier|final
name|LockType
name|type
parameter_list|,
specifier|final
name|String
name|description
parameter_list|)
block|{
name|this
operator|.
name|namespace
operator|=
name|namespace
expr_stmt|;
name|this
operator|.
name|tableName
operator|=
literal|null
expr_stmt|;
name|this
operator|.
name|regionInfos
operator|=
literal|null
expr_stmt|;
name|this
operator|.
name|type
operator|=
name|type
expr_stmt|;
name|this
operator|.
name|description
operator|=
name|description
expr_stmt|;
block|}
specifier|public
name|MasterLock
parameter_list|(
specifier|final
name|TableName
name|tableName
parameter_list|,
specifier|final
name|LockType
name|type
parameter_list|,
specifier|final
name|String
name|description
parameter_list|)
block|{
name|this
operator|.
name|namespace
operator|=
literal|null
expr_stmt|;
name|this
operator|.
name|tableName
operator|=
name|tableName
expr_stmt|;
name|this
operator|.
name|regionInfos
operator|=
literal|null
expr_stmt|;
name|this
operator|.
name|type
operator|=
name|type
expr_stmt|;
name|this
operator|.
name|description
operator|=
name|description
expr_stmt|;
block|}
specifier|public
name|MasterLock
parameter_list|(
specifier|final
name|RegionInfo
index|[]
name|regionInfos
parameter_list|,
specifier|final
name|String
name|description
parameter_list|)
block|{
name|this
operator|.
name|namespace
operator|=
literal|null
expr_stmt|;
name|this
operator|.
name|tableName
operator|=
literal|null
expr_stmt|;
name|this
operator|.
name|regionInfos
operator|=
name|regionInfos
expr_stmt|;
name|this
operator|.
name|type
operator|=
name|LockType
operator|.
name|EXCLUSIVE
expr_stmt|;
name|this
operator|.
name|description
operator|=
name|description
expr_stmt|;
block|}
comment|/**      * Acquire the lock, waiting indefinitely until the lock is released or      * the thread is interrupted.      * @throws InterruptedException If current thread is interrupted while      *                              waiting for the lock      */
specifier|public
name|boolean
name|acquire
parameter_list|()
throws|throws
name|InterruptedException
block|{
return|return
name|tryAcquire
argument_list|(
literal|0
argument_list|)
return|;
block|}
comment|/**      * Acquire the lock within a wait time.      * @param timeoutMs The maximum time (in milliseconds) to wait for the lock,      *                  0 to wait indefinitely      * @return True if the lock was acquired, false if waiting time elapsed      *         before the lock was acquired      * @throws InterruptedException If the thread is interrupted while waiting to      *                              acquire the lock      */
specifier|public
name|boolean
name|tryAcquire
parameter_list|(
specifier|final
name|long
name|timeoutMs
parameter_list|)
throws|throws
name|InterruptedException
block|{
if|if
condition|(
name|proc
operator|!=
literal|null
operator|&&
name|proc
operator|.
name|isLocked
argument_list|()
condition|)
block|{
return|return
literal|true
return|;
block|}
comment|// Use new condition and procedure every time lock is requested.
specifier|final
name|CountDownLatch
name|lockAcquireLatch
init|=
operator|new
name|CountDownLatch
argument_list|(
literal|1
argument_list|)
decl_stmt|;
if|if
condition|(
name|regionInfos
operator|!=
literal|null
condition|)
block|{
name|proc
operator|=
operator|new
name|LockProcedure
argument_list|(
name|master
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|regionInfos
argument_list|,
name|type
argument_list|,
name|description
argument_list|,
name|lockAcquireLatch
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|tableName
operator|!=
literal|null
condition|)
block|{
name|proc
operator|=
operator|new
name|LockProcedure
argument_list|(
name|master
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|tableName
argument_list|,
name|type
argument_list|,
name|description
argument_list|,
name|lockAcquireLatch
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|namespace
operator|!=
literal|null
condition|)
block|{
name|proc
operator|=
operator|new
name|LockProcedure
argument_list|(
name|master
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|namespace
argument_list|,
name|type
argument_list|,
name|description
argument_list|,
name|lockAcquireLatch
argument_list|)
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"no namespace/table/region provided"
argument_list|)
throw|;
block|}
comment|// The user of a MasterLock should be 'hbase', the only case where this is not true
comment|// is if from inside a coprocessor we try to take a master lock (which should be avoided)
name|proc
operator|.
name|setOwner
argument_list|(
name|master
operator|.
name|getMasterProcedureExecutor
argument_list|()
operator|.
name|getEnvironment
argument_list|()
operator|.
name|getRequestUser
argument_list|()
argument_list|)
expr_stmt|;
name|master
operator|.
name|getMasterProcedureExecutor
argument_list|()
operator|.
name|submitProcedure
argument_list|(
name|proc
argument_list|)
expr_stmt|;
name|long
name|deadline
init|=
operator|(
name|timeoutMs
operator|>
literal|0
operator|)
condition|?
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|+
name|timeoutMs
else|:
name|Long
operator|.
name|MAX_VALUE
decl_stmt|;
while|while
condition|(
name|deadline
operator|>=
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|&&
operator|!
name|proc
operator|.
name|isLocked
argument_list|()
condition|)
block|{
try|try
block|{
name|lockAcquireLatch
operator|.
name|await
argument_list|(
name|deadline
operator|-
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|,
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"InterruptedException when waiting for lock: "
operator|+
name|proc
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
comment|// kind of weird, releasing a lock which is not locked. This is to make the procedure
comment|// finish immediately whenever it gets scheduled so that it doesn't hold the lock.
name|release
argument_list|()
expr_stmt|;
throw|throw
name|e
throw|;
block|}
block|}
if|if
condition|(
operator|!
name|proc
operator|.
name|isLocked
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Timed out waiting to acquire procedure lock: "
operator|+
name|proc
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|release
argument_list|()
expr_stmt|;
return|return
literal|false
return|;
block|}
return|return
literal|true
return|;
block|}
comment|/**      * Release the lock.      * No-op if the lock was never acquired.      */
specifier|public
name|void
name|release
parameter_list|()
block|{
if|if
condition|(
name|proc
operator|!=
literal|null
condition|)
block|{
name|proc
operator|.
name|unlock
argument_list|(
name|master
operator|.
name|getMasterProcedureExecutor
argument_list|()
operator|.
name|getEnvironment
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|proc
operator|=
literal|null
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
literal|"MasterLock: proc = "
operator|+
name|proc
operator|.
name|toString
argument_list|()
return|;
block|}
annotation|@
name|VisibleForTesting
name|LockProcedure
name|getProc
parameter_list|()
block|{
return|return
name|proc
return|;
block|}
block|}
comment|/**    * Locks on namespace/table/regions for remote operations.    * Since remote operations are unreliable and the client/RS may die anytime and never release    * locks, regular heartbeats are required to keep the lock held.    */
specifier|public
class|class
name|RemoteLocks
block|{
specifier|public
name|long
name|requestNamespaceLock
parameter_list|(
specifier|final
name|String
name|namespace
parameter_list|,
specifier|final
name|LockType
name|type
parameter_list|,
specifier|final
name|String
name|description
parameter_list|,
specifier|final
name|NonceKey
name|nonceKey
parameter_list|)
throws|throws
name|IllegalArgumentException
throws|,
name|IOException
block|{
name|master
operator|.
name|getMasterCoprocessorHost
argument_list|()
operator|.
name|preRequestLock
argument_list|(
name|namespace
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
name|type
argument_list|,
name|description
argument_list|)
expr_stmt|;
specifier|final
name|LockProcedure
name|proc
init|=
operator|new
name|LockProcedure
argument_list|(
name|master
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|namespace
argument_list|,
name|type
argument_list|,
name|description
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|submitProcedure
argument_list|(
name|proc
argument_list|,
name|nonceKey
argument_list|)
expr_stmt|;
name|master
operator|.
name|getMasterCoprocessorHost
argument_list|()
operator|.
name|postRequestLock
argument_list|(
name|namespace
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
name|type
argument_list|,
name|description
argument_list|)
expr_stmt|;
return|return
name|proc
operator|.
name|getProcId
argument_list|()
return|;
block|}
specifier|public
name|long
name|requestTableLock
parameter_list|(
specifier|final
name|TableName
name|tableName
parameter_list|,
specifier|final
name|LockType
name|type
parameter_list|,
specifier|final
name|String
name|description
parameter_list|,
specifier|final
name|NonceKey
name|nonceKey
parameter_list|)
throws|throws
name|IllegalArgumentException
throws|,
name|IOException
block|{
name|master
operator|.
name|getMasterCoprocessorHost
argument_list|()
operator|.
name|preRequestLock
argument_list|(
literal|null
argument_list|,
name|tableName
argument_list|,
literal|null
argument_list|,
name|type
argument_list|,
name|description
argument_list|)
expr_stmt|;
specifier|final
name|LockProcedure
name|proc
init|=
operator|new
name|LockProcedure
argument_list|(
name|master
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|tableName
argument_list|,
name|type
argument_list|,
name|description
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|submitProcedure
argument_list|(
name|proc
argument_list|,
name|nonceKey
argument_list|)
expr_stmt|;
name|master
operator|.
name|getMasterCoprocessorHost
argument_list|()
operator|.
name|postRequestLock
argument_list|(
literal|null
argument_list|,
name|tableName
argument_list|,
literal|null
argument_list|,
name|type
argument_list|,
name|description
argument_list|)
expr_stmt|;
return|return
name|proc
operator|.
name|getProcId
argument_list|()
return|;
block|}
comment|/**      * @throws IllegalArgumentException if all regions are not from same table.      */
specifier|public
name|long
name|requestRegionsLock
parameter_list|(
specifier|final
name|RegionInfo
index|[]
name|regionInfos
parameter_list|,
specifier|final
name|String
name|description
parameter_list|,
specifier|final
name|NonceKey
name|nonceKey
parameter_list|)
throws|throws
name|IllegalArgumentException
throws|,
name|IOException
block|{
name|master
operator|.
name|getMasterCoprocessorHost
argument_list|()
operator|.
name|preRequestLock
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|,
name|regionInfos
argument_list|,
name|LockType
operator|.
name|EXCLUSIVE
argument_list|,
name|description
argument_list|)
expr_stmt|;
specifier|final
name|LockProcedure
name|proc
init|=
operator|new
name|LockProcedure
argument_list|(
name|master
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|regionInfos
argument_list|,
name|LockType
operator|.
name|EXCLUSIVE
argument_list|,
name|description
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|submitProcedure
argument_list|(
name|proc
argument_list|,
name|nonceKey
argument_list|)
expr_stmt|;
name|master
operator|.
name|getMasterCoprocessorHost
argument_list|()
operator|.
name|postRequestLock
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|,
name|regionInfos
argument_list|,
name|LockType
operator|.
name|EXCLUSIVE
argument_list|,
name|description
argument_list|)
expr_stmt|;
return|return
name|proc
operator|.
name|getProcId
argument_list|()
return|;
block|}
comment|/**      * @param keepAlive if false, release the lock.      * @return true, if procedure is found and it has the lock; else false.      */
specifier|public
name|boolean
name|lockHeartbeat
parameter_list|(
specifier|final
name|long
name|procId
parameter_list|,
specifier|final
name|boolean
name|keepAlive
parameter_list|)
throws|throws
name|IOException
block|{
specifier|final
name|LockProcedure
name|proc
init|=
name|master
operator|.
name|getMasterProcedureExecutor
argument_list|()
operator|.
name|getProcedure
argument_list|(
name|LockProcedure
operator|.
name|class
argument_list|,
name|procId
argument_list|)
decl_stmt|;
if|if
condition|(
name|proc
operator|==
literal|null
condition|)
return|return
literal|false
return|;
name|master
operator|.
name|getMasterCoprocessorHost
argument_list|()
operator|.
name|preLockHeartbeat
argument_list|(
name|proc
argument_list|,
name|keepAlive
argument_list|)
expr_stmt|;
name|proc
operator|.
name|updateHeartBeat
argument_list|()
expr_stmt|;
if|if
condition|(
operator|!
name|keepAlive
condition|)
block|{
name|proc
operator|.
name|unlock
argument_list|(
name|master
operator|.
name|getMasterProcedureExecutor
argument_list|()
operator|.
name|getEnvironment
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|master
operator|.
name|getMasterCoprocessorHost
argument_list|()
operator|.
name|postLockHeartbeat
argument_list|(
name|proc
argument_list|,
name|keepAlive
argument_list|)
expr_stmt|;
return|return
name|proc
operator|.
name|isLocked
argument_list|()
return|;
block|}
block|}
block|}
end_class

end_unit

