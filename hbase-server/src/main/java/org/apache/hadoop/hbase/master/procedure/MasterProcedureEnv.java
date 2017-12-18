begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|procedure
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
name|fs
operator|.
name|FileSystem
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
name|fs
operator|.
name|Path
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
name|conf
operator|.
name|ConfigurationObserver
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
name|yetus
operator|.
name|audience
operator|.
name|InterfaceStability
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
name|ipc
operator|.
name|RpcServer
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
name|master
operator|.
name|MasterCoprocessorHost
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
name|MasterServices
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
name|assignment
operator|.
name|AssignmentManager
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
name|replication
operator|.
name|ReplicationManager
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
name|store
operator|.
name|ProcedureStore
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
name|store
operator|.
name|wal
operator|.
name|WALProcedureStore
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
name|security
operator|.
name|User
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
name|security
operator|.
name|Superusers
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
name|CancelableProgressable
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
name|FSUtils
import|;
end_import

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
annotation|@
name|InterfaceStability
operator|.
name|Evolving
specifier|public
class|class
name|MasterProcedureEnv
implements|implements
name|ConfigurationObserver
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
name|MasterProcedureEnv
operator|.
name|class
argument_list|)
decl_stmt|;
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
specifier|static
class|class
name|WALStoreLeaseRecovery
implements|implements
name|WALProcedureStore
operator|.
name|LeaseRecovery
block|{
specifier|private
specifier|final
name|MasterServices
name|master
decl_stmt|;
specifier|public
name|WALStoreLeaseRecovery
parameter_list|(
specifier|final
name|MasterServices
name|master
parameter_list|)
block|{
name|this
operator|.
name|master
operator|=
name|master
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|recoverFileLease
parameter_list|(
specifier|final
name|FileSystem
name|fs
parameter_list|,
specifier|final
name|Path
name|path
parameter_list|)
throws|throws
name|IOException
block|{
specifier|final
name|Configuration
name|conf
init|=
name|master
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
specifier|final
name|FSUtils
name|fsUtils
init|=
name|FSUtils
operator|.
name|getInstance
argument_list|(
name|fs
argument_list|,
name|conf
argument_list|)
decl_stmt|;
name|fsUtils
operator|.
name|recoverFileLease
argument_list|(
name|fs
argument_list|,
name|path
argument_list|,
name|conf
argument_list|,
operator|new
name|CancelableProgressable
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|boolean
name|progress
parameter_list|()
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Recover Procedure Store log lease: "
operator|+
name|path
argument_list|)
expr_stmt|;
return|return
name|isRunning
argument_list|()
return|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
specifier|private
name|boolean
name|isRunning
parameter_list|()
block|{
return|return
name|master
operator|.
name|isActiveMaster
argument_list|()
operator|&&
operator|!
name|master
operator|.
name|isStopped
argument_list|()
operator|&&
operator|!
name|master
operator|.
name|isStopping
argument_list|()
operator|&&
operator|!
name|master
operator|.
name|isAborted
argument_list|()
return|;
block|}
block|}
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
specifier|static
class|class
name|MasterProcedureStoreListener
implements|implements
name|ProcedureStore
operator|.
name|ProcedureStoreListener
block|{
specifier|private
specifier|final
name|MasterServices
name|master
decl_stmt|;
specifier|public
name|MasterProcedureStoreListener
parameter_list|(
specifier|final
name|MasterServices
name|master
parameter_list|)
block|{
name|this
operator|.
name|master
operator|=
name|master
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|postSync
parameter_list|()
block|{
comment|// no-op
block|}
annotation|@
name|Override
specifier|public
name|void
name|abortProcess
parameter_list|()
block|{
name|master
operator|.
name|abort
argument_list|(
literal|"The Procedure Store lost the lease"
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
specifier|final
name|RSProcedureDispatcher
name|remoteDispatcher
decl_stmt|;
specifier|private
specifier|final
name|MasterProcedureScheduler
name|procSched
decl_stmt|;
specifier|private
specifier|final
name|MasterServices
name|master
decl_stmt|;
specifier|public
name|MasterProcedureEnv
parameter_list|(
specifier|final
name|MasterServices
name|master
parameter_list|)
block|{
name|this
argument_list|(
name|master
argument_list|,
operator|new
name|RSProcedureDispatcher
argument_list|(
name|master
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|public
name|MasterProcedureEnv
parameter_list|(
specifier|final
name|MasterServices
name|master
parameter_list|,
specifier|final
name|RSProcedureDispatcher
name|remoteDispatcher
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
name|procSched
operator|=
operator|new
name|MasterProcedureScheduler
argument_list|(
name|master
operator|.
name|getConfiguration
argument_list|()
argument_list|)
expr_stmt|;
name|this
operator|.
name|remoteDispatcher
operator|=
name|remoteDispatcher
expr_stmt|;
block|}
specifier|public
name|User
name|getRequestUser
parameter_list|()
block|{
return|return
name|RpcServer
operator|.
name|getRequestUser
argument_list|()
operator|.
name|orElse
argument_list|(
name|Superusers
operator|.
name|getSystemUser
argument_list|()
argument_list|)
return|;
block|}
specifier|public
name|MasterServices
name|getMasterServices
parameter_list|()
block|{
return|return
name|master
return|;
block|}
specifier|public
name|Configuration
name|getMasterConfiguration
parameter_list|()
block|{
return|return
name|master
operator|.
name|getConfiguration
argument_list|()
return|;
block|}
specifier|public
name|AssignmentManager
name|getAssignmentManager
parameter_list|()
block|{
return|return
name|master
operator|.
name|getAssignmentManager
argument_list|()
return|;
block|}
specifier|public
name|MasterCoprocessorHost
name|getMasterCoprocessorHost
parameter_list|()
block|{
return|return
name|master
operator|.
name|getMasterCoprocessorHost
argument_list|()
return|;
block|}
specifier|public
name|MasterProcedureScheduler
name|getProcedureScheduler
parameter_list|()
block|{
return|return
name|procSched
return|;
block|}
specifier|public
name|RSProcedureDispatcher
name|getRemoteDispatcher
parameter_list|()
block|{
return|return
name|remoteDispatcher
return|;
block|}
specifier|public
name|ReplicationManager
name|getReplicationManager
parameter_list|()
block|{
return|return
name|master
operator|.
name|getReplicationManager
argument_list|()
return|;
block|}
specifier|public
name|boolean
name|isRunning
parameter_list|()
block|{
if|if
condition|(
name|this
operator|.
name|master
operator|==
literal|null
operator|||
name|this
operator|.
name|master
operator|.
name|getMasterProcedureExecutor
argument_list|()
operator|==
literal|null
condition|)
return|return
literal|false
return|;
return|return
name|master
operator|.
name|getMasterProcedureExecutor
argument_list|()
operator|.
name|isRunning
argument_list|()
return|;
block|}
specifier|public
name|boolean
name|isInitialized
parameter_list|()
block|{
return|return
name|master
operator|.
name|isInitialized
argument_list|()
return|;
block|}
specifier|public
name|boolean
name|waitInitialized
parameter_list|(
name|Procedure
name|proc
parameter_list|)
block|{
return|return
name|master
operator|.
name|getInitializedEvent
argument_list|()
operator|.
name|suspendIfNotReady
argument_list|(
name|proc
argument_list|)
return|;
block|}
specifier|public
name|boolean
name|waitServerCrashProcessingEnabled
parameter_list|(
name|Procedure
name|proc
parameter_list|)
block|{
if|if
condition|(
name|master
operator|instanceof
name|HMaster
condition|)
block|{
return|return
operator|(
operator|(
name|HMaster
operator|)
name|master
operator|)
operator|.
name|getServerCrashProcessingEnabledEvent
argument_list|()
operator|.
name|suspendIfNotReady
argument_list|(
name|proc
argument_list|)
return|;
block|}
return|return
literal|false
return|;
block|}
specifier|public
name|boolean
name|waitFailoverCleanup
parameter_list|(
name|Procedure
name|proc
parameter_list|)
block|{
return|return
name|master
operator|.
name|getAssignmentManager
argument_list|()
operator|.
name|getFailoverCleanupEvent
argument_list|()
operator|.
name|suspendIfNotReady
argument_list|(
name|proc
argument_list|)
return|;
block|}
specifier|public
name|void
name|setEventReady
parameter_list|(
name|ProcedureEvent
name|event
parameter_list|,
name|boolean
name|isReady
parameter_list|)
block|{
if|if
condition|(
name|isReady
condition|)
block|{
name|event
operator|.
name|wake
argument_list|(
name|procSched
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|event
operator|.
name|suspend
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|onConfigurationChange
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
name|master
operator|.
name|getMasterProcedureExecutor
argument_list|()
operator|.
name|refreshConfiguration
argument_list|(
name|conf
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

