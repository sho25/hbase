begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**   * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUTKey WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|atomic
operator|.
name|AtomicBoolean
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
name|procedure
operator|.
name|MasterProcedureEnv
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
name|procedure
operator|.
name|TableProcedureInterface
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
name|procedure2
operator|.
name|Procedure
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
name|ProcedureEvent
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
name|ProcedureStateSerializer
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
name|ProcedureSuspendedException
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
name|LockProcedureData
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
name|ProcedureProtos
import|;
end_import

begin_comment
comment|/**  * Procedure to allow blessed clients and external admin tools to take our internal Schema locks  * used by the procedure framework isolating procedures doing creates/deletes etc. on  * table/namespace/regions.  * This procedure when scheduled, acquires specified locks, suspends itself and waits for:  *<ul>  *<li>Call to unlock: if lock request came from the process itself, say master chore.</li>  *<li>Timeout : if lock request came from RPC. On timeout, evaluates if it should continue holding  * the lock or not based on last heartbeat timestamp.</li>  *</ul>  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
specifier|final
class|class
name|LockProcedure
extends|extends
name|Procedure
argument_list|<
name|MasterProcedureEnv
argument_list|>
implements|implements
name|TableProcedureInterface
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
name|LockProcedure
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|int
name|DEFAULT_REMOTE_LOCKS_TIMEOUT_MS
init|=
literal|30000
decl_stmt|;
comment|// timeout in ms
specifier|public
specifier|static
specifier|final
name|String
name|REMOTE_LOCKS_TIMEOUT_MS_CONF
init|=
literal|"hbase.master.procedure.remote.locks.timeout.ms"
decl_stmt|;
comment|// 10 min. Same as old ZK lock timeout.
specifier|public
specifier|static
specifier|final
name|int
name|DEFAULT_LOCAL_MASTER_LOCKS_TIMEOUT_MS
init|=
literal|600000
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|LOCAL_MASTER_LOCKS_TIMEOUT_MS_CONF
init|=
literal|"hbase.master.procedure.local.master.locks.timeout.ms"
decl_stmt|;
specifier|private
name|String
name|namespace
decl_stmt|;
specifier|private
name|TableName
name|tableName
decl_stmt|;
specifier|private
name|RegionInfo
index|[]
name|regionInfos
decl_stmt|;
specifier|private
name|LockType
name|type
decl_stmt|;
comment|// underlying namespace/table/region lock.
specifier|private
name|LockInterface
name|lock
decl_stmt|;
specifier|private
name|TableOperationType
name|opType
decl_stmt|;
specifier|private
name|String
name|description
decl_stmt|;
comment|// True when recovery of master lock from WALs
specifier|private
name|boolean
name|recoveredMasterLock
decl_stmt|;
comment|// this is for internal working
specifier|private
name|boolean
name|hasLock
decl_stmt|;
specifier|private
specifier|final
name|ProcedureEvent
argument_list|<
name|LockProcedure
argument_list|>
name|event
init|=
operator|new
name|ProcedureEvent
argument_list|<>
argument_list|(
name|this
argument_list|)
decl_stmt|;
comment|// True if this proc acquired relevant locks. This value is for client checks.
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
comment|// Last system time (in ms) when client sent the heartbeat.
comment|// Initialize to system time for non-null value in case of recovery.
specifier|private
specifier|final
name|AtomicLong
name|lastHeartBeat
init|=
operator|new
name|AtomicLong
argument_list|()
decl_stmt|;
comment|// Set to true when unlock request is received.
specifier|private
specifier|final
name|AtomicBoolean
name|unlock
init|=
operator|new
name|AtomicBoolean
argument_list|(
literal|false
argument_list|)
decl_stmt|;
comment|// decreased when locks are acquired. Only used for local (with master process) purposes.
comment|// Setting latch to non-null value increases default timeout to
comment|// DEFAULT_LOCAL_MASTER_LOCKS_TIMEOUT_MS (10 min) so that there is no need to heartbeat.
specifier|private
specifier|final
name|CountDownLatch
name|lockAcquireLatch
decl_stmt|;
annotation|@
name|Override
specifier|public
name|TableName
name|getTableName
parameter_list|()
block|{
return|return
name|tableName
return|;
block|}
annotation|@
name|Override
specifier|public
name|TableOperationType
name|getTableOperationType
parameter_list|()
block|{
return|return
name|opType
return|;
block|}
specifier|private
interface|interface
name|LockInterface
block|{
name|boolean
name|acquireLock
parameter_list|(
name|MasterProcedureEnv
name|env
parameter_list|)
function_decl|;
name|void
name|releaseLock
parameter_list|(
name|MasterProcedureEnv
name|env
parameter_list|)
function_decl|;
block|}
specifier|public
name|LockProcedure
parameter_list|()
block|{
name|lockAcquireLatch
operator|=
literal|null
expr_stmt|;
block|}
specifier|private
name|LockProcedure
parameter_list|(
specifier|final
name|Configuration
name|conf
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
name|CountDownLatch
name|lockAcquireLatch
parameter_list|)
block|{
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
name|this
operator|.
name|lockAcquireLatch
operator|=
name|lockAcquireLatch
expr_stmt|;
if|if
condition|(
name|lockAcquireLatch
operator|==
literal|null
condition|)
block|{
name|setTimeout
argument_list|(
name|conf
operator|.
name|getInt
argument_list|(
name|REMOTE_LOCKS_TIMEOUT_MS_CONF
argument_list|,
name|DEFAULT_REMOTE_LOCKS_TIMEOUT_MS
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|setTimeout
argument_list|(
name|conf
operator|.
name|getInt
argument_list|(
name|LOCAL_MASTER_LOCKS_TIMEOUT_MS_CONF
argument_list|,
name|DEFAULT_LOCAL_MASTER_LOCKS_TIMEOUT_MS
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Constructor for namespace lock.    * @param lockAcquireLatch if not null, the latch is decreased when lock is acquired.    */
specifier|public
name|LockProcedure
parameter_list|(
specifier|final
name|Configuration
name|conf
parameter_list|,
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
name|CountDownLatch
name|lockAcquireLatch
parameter_list|)
throws|throws
name|IllegalArgumentException
block|{
name|this
argument_list|(
name|conf
argument_list|,
name|type
argument_list|,
name|description
argument_list|,
name|lockAcquireLatch
argument_list|)
expr_stmt|;
if|if
condition|(
name|namespace
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Empty namespace"
argument_list|)
throw|;
block|}
name|this
operator|.
name|namespace
operator|=
name|namespace
expr_stmt|;
name|this
operator|.
name|lock
operator|=
name|setupNamespaceLock
argument_list|()
expr_stmt|;
block|}
comment|/**    * Constructor for table lock.    * @param lockAcquireLatch if not null, the latch is decreased when lock is acquired.    */
specifier|public
name|LockProcedure
parameter_list|(
specifier|final
name|Configuration
name|conf
parameter_list|,
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
name|CountDownLatch
name|lockAcquireLatch
parameter_list|)
throws|throws
name|IllegalArgumentException
block|{
name|this
argument_list|(
name|conf
argument_list|,
name|type
argument_list|,
name|description
argument_list|,
name|lockAcquireLatch
argument_list|)
expr_stmt|;
name|this
operator|.
name|tableName
operator|=
name|tableName
expr_stmt|;
name|this
operator|.
name|lock
operator|=
name|setupTableLock
argument_list|()
expr_stmt|;
block|}
comment|/**    * Constructor for region lock(s).    * @param lockAcquireLatch if not null, the latch is decreased when lock is acquired.    *                        Useful for locks acquired locally from master process.    * @throws IllegalArgumentException if all regions are not from same table.    */
specifier|public
name|LockProcedure
parameter_list|(
specifier|final
name|Configuration
name|conf
parameter_list|,
specifier|final
name|RegionInfo
index|[]
name|regionInfos
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
name|CountDownLatch
name|lockAcquireLatch
parameter_list|)
throws|throws
name|IllegalArgumentException
block|{
name|this
argument_list|(
name|conf
argument_list|,
name|type
argument_list|,
name|description
argument_list|,
name|lockAcquireLatch
argument_list|)
expr_stmt|;
comment|// Build RegionInfo from region names.
if|if
condition|(
name|regionInfos
operator|.
name|length
operator|==
literal|0
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"No regions specified for region lock"
argument_list|)
throw|;
block|}
comment|// check all regions belong to same table.
specifier|final
name|TableName
name|regionTable
init|=
name|regionInfos
index|[
literal|0
index|]
operator|.
name|getTable
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|1
init|;
name|i
operator|<
name|regionInfos
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
if|if
condition|(
operator|!
name|regionInfos
index|[
name|i
index|]
operator|.
name|getTable
argument_list|()
operator|.
name|equals
argument_list|(
name|regionTable
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"All regions should be from same table"
argument_list|)
throw|;
block|}
block|}
name|this
operator|.
name|regionInfos
operator|=
name|regionInfos
expr_stmt|;
name|this
operator|.
name|lock
operator|=
name|setupRegionLock
argument_list|()
expr_stmt|;
block|}
specifier|private
name|boolean
name|hasHeartbeatExpired
parameter_list|()
block|{
return|return
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|-
name|lastHeartBeat
operator|.
name|get
argument_list|()
operator|>=
name|getTimeout
argument_list|()
return|;
block|}
comment|/**    * Updates timeout deadline for the lock.    */
specifier|public
name|void
name|updateHeartBeat
parameter_list|()
block|{
name|lastHeartBeat
operator|.
name|set
argument_list|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|)
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
literal|"Heartbeat "
operator|+
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Re run the procedure after every timeout to write new WAL entries so we don't hold back old    * WALs.    * @return false, so procedure framework doesn't mark this procedure as failure.    */
annotation|@
name|Override
specifier|protected
name|boolean
name|setTimeoutFailure
parameter_list|(
specifier|final
name|MasterProcedureEnv
name|env
parameter_list|)
block|{
synchronized|synchronized
init|(
name|event
init|)
block|{
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
name|LOG
operator|.
name|debug
argument_list|(
literal|"Timeout failure "
operator|+
name|this
operator|.
name|event
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|event
operator|.
name|isReady
argument_list|()
condition|)
block|{
comment|// Maybe unlock() awakened the event.
name|setState
argument_list|(
name|ProcedureProtos
operator|.
name|ProcedureState
operator|.
name|RUNNABLE
argument_list|)
expr_stmt|;
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
name|LOG
operator|.
name|debug
argument_list|(
literal|"Calling wake on "
operator|+
name|this
operator|.
name|event
argument_list|)
expr_stmt|;
name|event
operator|.
name|wake
argument_list|(
name|env
operator|.
name|getProcedureScheduler
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
return|return
literal|false
return|;
comment|// false: do not mark the procedure as failed.
block|}
comment|// Can be called before procedure gets scheduled, in which case, the execute() will finish
comment|// immediately and release the underlying locks.
specifier|public
name|void
name|unlock
parameter_list|(
specifier|final
name|MasterProcedureEnv
name|env
parameter_list|)
block|{
name|unlock
operator|.
name|set
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|locked
operator|.
name|set
argument_list|(
literal|false
argument_list|)
expr_stmt|;
comment|// Maybe timeout already awakened the event and the procedure has finished.
synchronized|synchronized
init|(
name|event
init|)
block|{
if|if
condition|(
operator|!
name|event
operator|.
name|isReady
argument_list|()
condition|)
block|{
name|setState
argument_list|(
name|ProcedureProtos
operator|.
name|ProcedureState
operator|.
name|RUNNABLE
argument_list|)
expr_stmt|;
name|event
operator|.
name|wake
argument_list|(
name|env
operator|.
name|getProcedureScheduler
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|Override
specifier|protected
name|Procedure
argument_list|<
name|MasterProcedureEnv
argument_list|>
index|[]
name|execute
parameter_list|(
specifier|final
name|MasterProcedureEnv
name|env
parameter_list|)
throws|throws
name|ProcedureSuspendedException
block|{
comment|// Local master locks don't store any state, so on recovery, simply finish this procedure
comment|// immediately.
if|if
condition|(
name|recoveredMasterLock
condition|)
return|return
literal|null
return|;
if|if
condition|(
name|lockAcquireLatch
operator|!=
literal|null
condition|)
block|{
name|lockAcquireLatch
operator|.
name|countDown
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|unlock
operator|.
name|get
argument_list|()
operator|||
name|hasHeartbeatExpired
argument_list|()
condition|)
block|{
name|locked
operator|.
name|set
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
operator|(
name|unlock
operator|.
name|get
argument_list|()
condition|?
literal|"UNLOCKED "
else|:
literal|"TIMED OUT "
operator|)
operator|+
name|toString
argument_list|()
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
synchronized|synchronized
init|(
name|event
init|)
block|{
name|event
operator|.
name|suspend
argument_list|()
expr_stmt|;
name|event
operator|.
name|suspendIfNotReady
argument_list|(
name|this
argument_list|)
expr_stmt|;
name|setState
argument_list|(
name|ProcedureProtos
operator|.
name|ProcedureState
operator|.
name|WAITING_TIMEOUT
argument_list|)
expr_stmt|;
block|}
throw|throw
operator|new
name|ProcedureSuspendedException
argument_list|()
throw|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|rollback
parameter_list|(
specifier|final
name|MasterProcedureEnv
name|env
parameter_list|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
annotation|@
name|Override
specifier|protected
name|boolean
name|abort
parameter_list|(
specifier|final
name|MasterProcedureEnv
name|env
parameter_list|)
block|{
name|unlock
argument_list|(
name|env
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|serializeStateData
parameter_list|(
name|ProcedureStateSerializer
name|serializer
parameter_list|)
throws|throws
name|IOException
block|{
specifier|final
name|LockProcedureData
operator|.
name|Builder
name|builder
init|=
name|LockProcedureData
operator|.
name|newBuilder
argument_list|()
operator|.
name|setLockType
argument_list|(
name|LockServiceProtos
operator|.
name|LockType
operator|.
name|valueOf
argument_list|(
name|type
operator|.
name|name
argument_list|()
argument_list|)
argument_list|)
operator|.
name|setDescription
argument_list|(
name|description
argument_list|)
decl_stmt|;
if|if
condition|(
name|regionInfos
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|regionInfos
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
name|builder
operator|.
name|addRegionInfo
argument_list|(
name|ProtobufUtil
operator|.
name|toRegionInfo
argument_list|(
name|regionInfos
index|[
name|i
index|]
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|namespace
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|setNamespace
argument_list|(
name|namespace
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
name|builder
operator|.
name|setTableName
argument_list|(
name|ProtobufUtil
operator|.
name|toProtoTableName
argument_list|(
name|tableName
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|lockAcquireLatch
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|setIsMasterLock
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
name|serializer
operator|.
name|serialize
argument_list|(
name|builder
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|deserializeStateData
parameter_list|(
name|ProcedureStateSerializer
name|serializer
parameter_list|)
throws|throws
name|IOException
block|{
specifier|final
name|LockProcedureData
name|state
init|=
name|serializer
operator|.
name|deserialize
argument_list|(
name|LockProcedureData
operator|.
name|class
argument_list|)
decl_stmt|;
name|type
operator|=
name|LockType
operator|.
name|valueOf
argument_list|(
name|state
operator|.
name|getLockType
argument_list|()
operator|.
name|name
argument_list|()
argument_list|)
expr_stmt|;
name|description
operator|=
name|state
operator|.
name|getDescription
argument_list|()
expr_stmt|;
if|if
condition|(
name|state
operator|.
name|getRegionInfoCount
argument_list|()
operator|>
literal|0
condition|)
block|{
name|regionInfos
operator|=
operator|new
name|RegionInfo
index|[
name|state
operator|.
name|getRegionInfoCount
argument_list|()
index|]
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
name|state
operator|.
name|getRegionInfoCount
argument_list|()
condition|;
operator|++
name|i
control|)
block|{
name|regionInfos
index|[
name|i
index|]
operator|=
name|ProtobufUtil
operator|.
name|toRegionInfo
argument_list|(
name|state
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
name|state
operator|.
name|hasNamespace
argument_list|()
condition|)
block|{
name|namespace
operator|=
name|state
operator|.
name|getNamespace
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|state
operator|.
name|hasTableName
argument_list|()
condition|)
block|{
name|tableName
operator|=
name|ProtobufUtil
operator|.
name|toTableName
argument_list|(
name|state
operator|.
name|getTableName
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|recoveredMasterLock
operator|=
name|state
operator|.
name|getIsMasterLock
argument_list|()
expr_stmt|;
name|this
operator|.
name|lock
operator|=
name|setupLock
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|LockState
name|acquireLock
parameter_list|(
specifier|final
name|MasterProcedureEnv
name|env
parameter_list|)
block|{
name|boolean
name|ret
init|=
name|lock
operator|.
name|acquireLock
argument_list|(
name|env
argument_list|)
decl_stmt|;
name|locked
operator|.
name|set
argument_list|(
name|ret
argument_list|)
expr_stmt|;
name|hasLock
operator|=
name|ret
expr_stmt|;
if|if
condition|(
name|ret
condition|)
block|{
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
literal|"LOCKED "
operator|+
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|lastHeartBeat
operator|.
name|set
argument_list|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|LockState
operator|.
name|LOCK_ACQUIRED
return|;
block|}
name|LOG
operator|.
name|warn
argument_list|(
literal|"Failed acquire LOCK "
operator|+
name|toString
argument_list|()
operator|+
literal|"; YIELDING"
argument_list|)
expr_stmt|;
return|return
name|LockState
operator|.
name|LOCK_EVENT_WAIT
return|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|releaseLock
parameter_list|(
specifier|final
name|MasterProcedureEnv
name|env
parameter_list|)
block|{
name|lock
operator|.
name|releaseLock
argument_list|(
name|env
argument_list|)
expr_stmt|;
name|hasLock
operator|=
literal|false
expr_stmt|;
block|}
comment|/**    * On recovery, re-execute from start to acquire the locks.    * Need to explicitly set it to RUNNABLE because the procedure might have been in WAITING_TIMEOUT    * state when crash happened. In which case, it'll be sent back to timeout queue on recovery,    * which we don't want since we want to require locks.    */
annotation|@
name|Override
specifier|protected
name|void
name|beforeReplay
parameter_list|(
name|MasterProcedureEnv
name|env
parameter_list|)
block|{
name|setState
argument_list|(
name|ProcedureProtos
operator|.
name|ProcedureState
operator|.
name|RUNNABLE
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|toStringClassDetails
parameter_list|(
specifier|final
name|StringBuilder
name|builder
parameter_list|)
block|{
name|super
operator|.
name|toStringClassDetails
argument_list|(
name|builder
argument_list|)
expr_stmt|;
if|if
condition|(
name|regionInfos
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|append
argument_list|(
literal|" regions="
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
name|regionInfos
operator|.
name|length
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
name|builder
operator|.
name|append
argument_list|(
literal|","
argument_list|)
expr_stmt|;
name|builder
operator|.
name|append
argument_list|(
name|regionInfos
index|[
name|i
index|]
operator|.
name|getShortNameToLog
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|namespace
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|append
argument_list|(
literal|", namespace="
argument_list|)
operator|.
name|append
argument_list|(
name|namespace
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
name|builder
operator|.
name|append
argument_list|(
literal|", tableName="
argument_list|)
operator|.
name|append
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|append
argument_list|(
literal|", type="
argument_list|)
operator|.
name|append
argument_list|(
name|type
argument_list|)
expr_stmt|;
block|}
specifier|public
name|LockType
name|getType
parameter_list|()
block|{
return|return
name|type
return|;
block|}
specifier|private
name|LockInterface
name|setupLock
parameter_list|()
throws|throws
name|IllegalArgumentException
block|{
if|if
condition|(
name|regionInfos
operator|!=
literal|null
condition|)
block|{
return|return
name|setupRegionLock
argument_list|()
return|;
block|}
elseif|else
if|if
condition|(
name|namespace
operator|!=
literal|null
condition|)
block|{
return|return
name|setupNamespaceLock
argument_list|()
return|;
block|}
elseif|else
if|if
condition|(
name|tableName
operator|!=
literal|null
condition|)
block|{
return|return
name|setupTableLock
argument_list|()
return|;
block|}
else|else
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Unknown level specified in "
operator|+
name|toString
argument_list|()
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"no namespace/table/region provided"
argument_list|)
throw|;
block|}
block|}
specifier|private
name|LockInterface
name|setupNamespaceLock
parameter_list|()
throws|throws
name|IllegalArgumentException
block|{
name|this
operator|.
name|tableName
operator|=
name|TableName
operator|.
name|NAMESPACE_TABLE_NAME
expr_stmt|;
switch|switch
condition|(
name|type
condition|)
block|{
case|case
name|EXCLUSIVE
case|:
name|this
operator|.
name|opType
operator|=
name|TableOperationType
operator|.
name|EDIT
expr_stmt|;
return|return
operator|new
name|NamespaceExclusiveLock
argument_list|()
return|;
case|case
name|SHARED
case|:
name|LOG
operator|.
name|error
argument_list|(
literal|"Shared lock on namespace not supported for "
operator|+
name|toString
argument_list|()
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Shared lock on namespace not supported"
argument_list|)
throw|;
default|default:
name|LOG
operator|.
name|error
argument_list|(
literal|"Unexpected lock type "
operator|+
name|toString
argument_list|()
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Wrong lock type: "
operator|+
name|type
operator|.
name|toString
argument_list|()
argument_list|)
throw|;
block|}
block|}
specifier|private
name|LockInterface
name|setupTableLock
parameter_list|()
throws|throws
name|IllegalArgumentException
block|{
switch|switch
condition|(
name|type
condition|)
block|{
case|case
name|EXCLUSIVE
case|:
name|this
operator|.
name|opType
operator|=
name|TableOperationType
operator|.
name|EDIT
expr_stmt|;
return|return
operator|new
name|TableExclusiveLock
argument_list|()
return|;
case|case
name|SHARED
case|:
name|this
operator|.
name|opType
operator|=
name|TableOperationType
operator|.
name|READ
expr_stmt|;
return|return
operator|new
name|TableSharedLock
argument_list|()
return|;
default|default:
name|LOG
operator|.
name|error
argument_list|(
literal|"Unexpected lock type "
operator|+
name|toString
argument_list|()
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Wrong lock type:"
operator|+
name|type
operator|.
name|toString
argument_list|()
argument_list|)
throw|;
block|}
block|}
specifier|private
name|LockInterface
name|setupRegionLock
parameter_list|()
throws|throws
name|IllegalArgumentException
block|{
name|this
operator|.
name|tableName
operator|=
name|regionInfos
index|[
literal|0
index|]
operator|.
name|getTable
argument_list|()
expr_stmt|;
switch|switch
condition|(
name|type
condition|)
block|{
case|case
name|EXCLUSIVE
case|:
name|this
operator|.
name|opType
operator|=
name|TableOperationType
operator|.
name|REGION_EDIT
expr_stmt|;
return|return
operator|new
name|RegionExclusiveLock
argument_list|()
return|;
default|default:
name|LOG
operator|.
name|error
argument_list|(
literal|"Only exclusive lock supported on regions for "
operator|+
name|toString
argument_list|()
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Only exclusive lock supported on regions."
argument_list|)
throw|;
block|}
block|}
specifier|public
name|String
name|getDescription
parameter_list|()
block|{
return|return
name|description
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
annotation|@
name|Override
specifier|public
name|boolean
name|holdLock
parameter_list|(
specifier|final
name|MasterProcedureEnv
name|env
parameter_list|)
block|{
return|return
literal|true
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|hasLock
parameter_list|(
specifier|final
name|MasterProcedureEnv
name|env
parameter_list|)
block|{
return|return
name|hasLock
return|;
block|}
comment|///////////////////////
comment|// LOCK IMPLEMENTATIONS
comment|///////////////////////
specifier|private
class|class
name|TableExclusiveLock
implements|implements
name|LockInterface
block|{
annotation|@
name|Override
specifier|public
name|boolean
name|acquireLock
parameter_list|(
specifier|final
name|MasterProcedureEnv
name|env
parameter_list|)
block|{
comment|// We invert return from waitNamespaceExclusiveLock; it returns true if you HAVE TO WAIT
comment|// to get the lock and false if you don't; i.e. you got the lock.
return|return
operator|!
name|env
operator|.
name|getProcedureScheduler
argument_list|()
operator|.
name|waitTableExclusiveLock
argument_list|(
name|LockProcedure
operator|.
name|this
argument_list|,
name|tableName
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|releaseLock
parameter_list|(
specifier|final
name|MasterProcedureEnv
name|env
parameter_list|)
block|{
name|env
operator|.
name|getProcedureScheduler
argument_list|()
operator|.
name|wakeTableExclusiveLock
argument_list|(
name|LockProcedure
operator|.
name|this
argument_list|,
name|tableName
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
class|class
name|TableSharedLock
implements|implements
name|LockInterface
block|{
annotation|@
name|Override
specifier|public
name|boolean
name|acquireLock
parameter_list|(
specifier|final
name|MasterProcedureEnv
name|env
parameter_list|)
block|{
comment|// We invert return from waitNamespaceExclusiveLock; it returns true if you HAVE TO WAIT
comment|// to get the lock and false if you don't; i.e. you got the lock.
return|return
operator|!
name|env
operator|.
name|getProcedureScheduler
argument_list|()
operator|.
name|waitTableSharedLock
argument_list|(
name|LockProcedure
operator|.
name|this
argument_list|,
name|tableName
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|releaseLock
parameter_list|(
specifier|final
name|MasterProcedureEnv
name|env
parameter_list|)
block|{
name|env
operator|.
name|getProcedureScheduler
argument_list|()
operator|.
name|wakeTableSharedLock
argument_list|(
name|LockProcedure
operator|.
name|this
argument_list|,
name|tableName
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
class|class
name|NamespaceExclusiveLock
implements|implements
name|LockInterface
block|{
annotation|@
name|Override
specifier|public
name|boolean
name|acquireLock
parameter_list|(
specifier|final
name|MasterProcedureEnv
name|env
parameter_list|)
block|{
comment|// We invert return from waitNamespaceExclusiveLock; it returns true if you HAVE TO WAIT
comment|// to get the lock and false if you don't; i.e. you got the lock.
return|return
operator|!
name|env
operator|.
name|getProcedureScheduler
argument_list|()
operator|.
name|waitNamespaceExclusiveLock
argument_list|(
name|LockProcedure
operator|.
name|this
argument_list|,
name|namespace
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|releaseLock
parameter_list|(
specifier|final
name|MasterProcedureEnv
name|env
parameter_list|)
block|{
name|env
operator|.
name|getProcedureScheduler
argument_list|()
operator|.
name|wakeNamespaceExclusiveLock
argument_list|(
name|LockProcedure
operator|.
name|this
argument_list|,
name|namespace
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
class|class
name|RegionExclusiveLock
implements|implements
name|LockInterface
block|{
annotation|@
name|Override
specifier|public
name|boolean
name|acquireLock
parameter_list|(
specifier|final
name|MasterProcedureEnv
name|env
parameter_list|)
block|{
comment|// We invert return from waitNamespaceExclusiveLock; it returns true if you HAVE TO WAIT
comment|// to get the lock and false if you don't; i.e. you got the lock.
return|return
operator|!
name|env
operator|.
name|getProcedureScheduler
argument_list|()
operator|.
name|waitRegions
argument_list|(
name|LockProcedure
operator|.
name|this
argument_list|,
name|tableName
argument_list|,
name|regionInfos
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|releaseLock
parameter_list|(
specifier|final
name|MasterProcedureEnv
name|env
parameter_list|)
block|{
name|env
operator|.
name|getProcedureScheduler
argument_list|()
operator|.
name|wakeRegions
argument_list|(
name|LockProcedure
operator|.
name|this
argument_list|,
name|tableName
argument_list|,
name|regionInfos
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

