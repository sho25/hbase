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
name|java
operator|.
name|io
operator|.
name|InputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|OutputStream
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
name|NamespaceDescriptor
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
name|NamespaceExistException
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
name|MasterFileSystem
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
name|TableNamespaceManager
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
name|MasterProcedureProtos
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
name|MasterProcedureProtos
operator|.
name|CreateNamespaceState
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

begin_comment
comment|/**  * The procedure to create a new namespace.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|CreateNamespaceProcedure
extends|extends
name|AbstractStateMachineNamespaceProcedure
argument_list|<
name|CreateNamespaceState
argument_list|>
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
name|CreateNamespaceProcedure
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|NamespaceDescriptor
name|nsDescriptor
decl_stmt|;
specifier|private
name|Boolean
name|traceEnabled
decl_stmt|;
specifier|public
name|CreateNamespaceProcedure
parameter_list|()
block|{
name|this
operator|.
name|traceEnabled
operator|=
literal|null
expr_stmt|;
block|}
specifier|public
name|CreateNamespaceProcedure
parameter_list|(
specifier|final
name|MasterProcedureEnv
name|env
parameter_list|,
specifier|final
name|NamespaceDescriptor
name|nsDescriptor
parameter_list|)
block|{
name|super
argument_list|(
name|env
argument_list|)
expr_stmt|;
name|this
operator|.
name|nsDescriptor
operator|=
name|nsDescriptor
expr_stmt|;
name|this
operator|.
name|traceEnabled
operator|=
literal|null
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|Flow
name|executeFromState
parameter_list|(
specifier|final
name|MasterProcedureEnv
name|env
parameter_list|,
specifier|final
name|CreateNamespaceState
name|state
parameter_list|)
throws|throws
name|InterruptedException
block|{
if|if
condition|(
name|isTraceEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|trace
argument_list|(
name|this
operator|+
literal|" execute state="
operator|+
name|state
argument_list|)
expr_stmt|;
block|}
try|try
block|{
switch|switch
condition|(
name|state
condition|)
block|{
case|case
name|CREATE_NAMESPACE_PREPARE
case|:
name|prepareCreate
argument_list|(
name|env
argument_list|)
expr_stmt|;
name|setNextState
argument_list|(
name|CreateNamespaceState
operator|.
name|CREATE_NAMESPACE_CREATE_DIRECTORY
argument_list|)
expr_stmt|;
break|break;
case|case
name|CREATE_NAMESPACE_CREATE_DIRECTORY
case|:
name|createDirectory
argument_list|(
name|env
argument_list|,
name|nsDescriptor
argument_list|)
expr_stmt|;
name|setNextState
argument_list|(
name|CreateNamespaceState
operator|.
name|CREATE_NAMESPACE_INSERT_INTO_NS_TABLE
argument_list|)
expr_stmt|;
break|break;
case|case
name|CREATE_NAMESPACE_INSERT_INTO_NS_TABLE
case|:
name|insertIntoNSTable
argument_list|(
name|env
argument_list|,
name|nsDescriptor
argument_list|)
expr_stmt|;
name|setNextState
argument_list|(
name|CreateNamespaceState
operator|.
name|CREATE_NAMESPACE_UPDATE_ZK
argument_list|)
expr_stmt|;
break|break;
case|case
name|CREATE_NAMESPACE_UPDATE_ZK
case|:
name|updateZKNamespaceManager
argument_list|(
name|env
argument_list|,
name|nsDescriptor
argument_list|)
expr_stmt|;
name|setNextState
argument_list|(
name|CreateNamespaceState
operator|.
name|CREATE_NAMESPACE_SET_NAMESPACE_QUOTA
argument_list|)
expr_stmt|;
break|break;
case|case
name|CREATE_NAMESPACE_SET_NAMESPACE_QUOTA
case|:
name|setNamespaceQuota
argument_list|(
name|env
argument_list|,
name|nsDescriptor
argument_list|)
expr_stmt|;
return|return
name|Flow
operator|.
name|NO_MORE_STATE
return|;
default|default:
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
name|this
operator|+
literal|" unhandled state="
operator|+
name|state
argument_list|)
throw|;
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
if|if
condition|(
name|isRollbackSupported
argument_list|(
name|state
argument_list|)
condition|)
block|{
name|setFailure
argument_list|(
literal|"master-create-namespace"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Retriable error trying to create namespace="
operator|+
name|nsDescriptor
operator|.
name|getName
argument_list|()
operator|+
literal|" (in state="
operator|+
name|state
operator|+
literal|")"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|Flow
operator|.
name|HAS_MORE_STATE
return|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|rollbackState
parameter_list|(
specifier|final
name|MasterProcedureEnv
name|env
parameter_list|,
specifier|final
name|CreateNamespaceState
name|state
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|state
operator|==
name|CreateNamespaceState
operator|.
name|CREATE_NAMESPACE_PREPARE
condition|)
block|{
comment|// nothing to rollback, pre-create is just state checks.
comment|// TODO: coprocessor rollback semantic is still undefined.
return|return;
block|}
comment|// The procedure doesn't have a rollback. The execution will succeed, at some point.
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"unhandled state="
operator|+
name|state
argument_list|)
throw|;
block|}
annotation|@
name|Override
specifier|protected
name|boolean
name|isRollbackSupported
parameter_list|(
specifier|final
name|CreateNamespaceState
name|state
parameter_list|)
block|{
switch|switch
condition|(
name|state
condition|)
block|{
case|case
name|CREATE_NAMESPACE_PREPARE
case|:
return|return
literal|true
return|;
default|default:
return|return
literal|false
return|;
block|}
block|}
annotation|@
name|Override
specifier|protected
name|CreateNamespaceState
name|getState
parameter_list|(
specifier|final
name|int
name|stateId
parameter_list|)
block|{
return|return
name|CreateNamespaceState
operator|.
name|valueOf
argument_list|(
name|stateId
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|protected
name|int
name|getStateId
parameter_list|(
specifier|final
name|CreateNamespaceState
name|state
parameter_list|)
block|{
return|return
name|state
operator|.
name|getNumber
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|protected
name|CreateNamespaceState
name|getInitialState
parameter_list|()
block|{
return|return
name|CreateNamespaceState
operator|.
name|CREATE_NAMESPACE_PREPARE
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|serializeStateData
parameter_list|(
specifier|final
name|OutputStream
name|stream
parameter_list|)
throws|throws
name|IOException
block|{
name|super
operator|.
name|serializeStateData
argument_list|(
name|stream
argument_list|)
expr_stmt|;
name|MasterProcedureProtos
operator|.
name|CreateNamespaceStateData
operator|.
name|Builder
name|createNamespaceMsg
init|=
name|MasterProcedureProtos
operator|.
name|CreateNamespaceStateData
operator|.
name|newBuilder
argument_list|()
operator|.
name|setNamespaceDescriptor
argument_list|(
name|ProtobufUtil
operator|.
name|toProtoNamespaceDescriptor
argument_list|(
name|this
operator|.
name|nsDescriptor
argument_list|)
argument_list|)
decl_stmt|;
name|createNamespaceMsg
operator|.
name|build
argument_list|()
operator|.
name|writeDelimitedTo
argument_list|(
name|stream
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|deserializeStateData
parameter_list|(
specifier|final
name|InputStream
name|stream
parameter_list|)
throws|throws
name|IOException
block|{
name|super
operator|.
name|deserializeStateData
argument_list|(
name|stream
argument_list|)
expr_stmt|;
name|MasterProcedureProtos
operator|.
name|CreateNamespaceStateData
name|createNamespaceMsg
init|=
name|MasterProcedureProtos
operator|.
name|CreateNamespaceStateData
operator|.
name|parseDelimitedFrom
argument_list|(
name|stream
argument_list|)
decl_stmt|;
name|nsDescriptor
operator|=
name|ProtobufUtil
operator|.
name|toNamespaceDescriptor
argument_list|(
name|createNamespaceMsg
operator|.
name|getNamespaceDescriptor
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|private
name|boolean
name|isBootstrapNamespace
parameter_list|()
block|{
return|return
name|nsDescriptor
operator|.
name|equals
argument_list|(
name|NamespaceDescriptor
operator|.
name|DEFAULT_NAMESPACE
argument_list|)
operator|||
name|nsDescriptor
operator|.
name|equals
argument_list|(
name|NamespaceDescriptor
operator|.
name|SYSTEM_NAMESPACE
argument_list|)
return|;
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
if|if
condition|(
operator|!
name|env
operator|.
name|getMasterServices
argument_list|()
operator|.
name|isInitialized
argument_list|()
condition|)
block|{
comment|// Namespace manager might not be ready if master is not fully initialized,
comment|// return false to reject user namespace creation; return true for default
comment|// and system namespace creation (this is part of master initialization).
if|if
condition|(
operator|!
name|isBootstrapNamespace
argument_list|()
operator|&&
name|env
operator|.
name|waitInitialized
argument_list|(
name|this
argument_list|)
condition|)
block|{
return|return
name|LockState
operator|.
name|LOCK_EVENT_WAIT
return|;
block|}
block|}
if|if
condition|(
name|env
operator|.
name|getProcedureScheduler
argument_list|()
operator|.
name|waitNamespaceExclusiveLock
argument_list|(
name|this
argument_list|,
name|getNamespaceName
argument_list|()
argument_list|)
condition|)
block|{
return|return
name|LockState
operator|.
name|LOCK_EVENT_WAIT
return|;
block|}
return|return
name|LockState
operator|.
name|LOCK_ACQUIRED
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
name|TableOperationType
operator|.
name|EDIT
return|;
block|}
annotation|@
name|Override
specifier|protected
name|String
name|getNamespaceName
parameter_list|()
block|{
return|return
name|nsDescriptor
operator|.
name|getName
argument_list|()
return|;
block|}
comment|/**    * Action before any real action of creating namespace.    * @param env MasterProcedureEnv    * @throws IOException    */
specifier|private
name|void
name|prepareCreate
parameter_list|(
specifier|final
name|MasterProcedureEnv
name|env
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|getTableNamespaceManager
argument_list|(
name|env
argument_list|)
operator|.
name|doesNamespaceExist
argument_list|(
name|nsDescriptor
operator|.
name|getName
argument_list|()
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|NamespaceExistException
argument_list|(
name|nsDescriptor
operator|.
name|getName
argument_list|()
argument_list|)
throw|;
block|}
name|getTableNamespaceManager
argument_list|(
name|env
argument_list|)
operator|.
name|validateTableAndRegionCount
argument_list|(
name|nsDescriptor
argument_list|)
expr_stmt|;
block|}
comment|/**    * Create the namespace directory    * @param env MasterProcedureEnv    * @param nsDescriptor NamespaceDescriptor    * @throws IOException    */
specifier|protected
specifier|static
name|void
name|createDirectory
parameter_list|(
specifier|final
name|MasterProcedureEnv
name|env
parameter_list|,
specifier|final
name|NamespaceDescriptor
name|nsDescriptor
parameter_list|)
throws|throws
name|IOException
block|{
name|MasterFileSystem
name|mfs
init|=
name|env
operator|.
name|getMasterServices
argument_list|()
operator|.
name|getMasterFileSystem
argument_list|()
decl_stmt|;
name|mfs
operator|.
name|getFileSystem
argument_list|()
operator|.
name|mkdirs
argument_list|(
name|FSUtils
operator|.
name|getNamespaceDir
argument_list|(
name|mfs
operator|.
name|getRootDir
argument_list|()
argument_list|,
name|nsDescriptor
operator|.
name|getName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * Insert the row into ns table    * @param env MasterProcedureEnv    * @param nsDescriptor NamespaceDescriptor    * @throws IOException    */
specifier|protected
specifier|static
name|void
name|insertIntoNSTable
parameter_list|(
specifier|final
name|MasterProcedureEnv
name|env
parameter_list|,
specifier|final
name|NamespaceDescriptor
name|nsDescriptor
parameter_list|)
throws|throws
name|IOException
block|{
name|getTableNamespaceManager
argument_list|(
name|env
argument_list|)
operator|.
name|insertIntoNSTable
argument_list|(
name|nsDescriptor
argument_list|)
expr_stmt|;
block|}
comment|/**    * Update ZooKeeper.    * @param env MasterProcedureEnv    * @param nsDescriptor NamespaceDescriptor    * @throws IOException    */
specifier|protected
specifier|static
name|void
name|updateZKNamespaceManager
parameter_list|(
specifier|final
name|MasterProcedureEnv
name|env
parameter_list|,
specifier|final
name|NamespaceDescriptor
name|nsDescriptor
parameter_list|)
throws|throws
name|IOException
block|{
name|getTableNamespaceManager
argument_list|(
name|env
argument_list|)
operator|.
name|updateZKNamespaceManager
argument_list|(
name|nsDescriptor
argument_list|)
expr_stmt|;
block|}
comment|/**    * Set quota for the namespace    * @param env MasterProcedureEnv    * @param nsDescriptor NamespaceDescriptor    * @throws IOException    **/
specifier|protected
specifier|static
name|void
name|setNamespaceQuota
parameter_list|(
specifier|final
name|MasterProcedureEnv
name|env
parameter_list|,
specifier|final
name|NamespaceDescriptor
name|nsDescriptor
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|env
operator|.
name|getMasterServices
argument_list|()
operator|.
name|isInitialized
argument_list|()
condition|)
block|{
name|env
operator|.
name|getMasterServices
argument_list|()
operator|.
name|getMasterQuotaManager
argument_list|()
operator|.
name|setNamespaceQuota
argument_list|(
name|nsDescriptor
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * remove quota for the namespace if exists    * @param env MasterProcedureEnv    * @throws IOException    **/
specifier|private
name|void
name|rollbackSetNamespaceQuota
parameter_list|(
specifier|final
name|MasterProcedureEnv
name|env
parameter_list|)
throws|throws
name|IOException
block|{
try|try
block|{
name|DeleteNamespaceProcedure
operator|.
name|removeNamespaceQuota
argument_list|(
name|env
argument_list|,
name|nsDescriptor
operator|.
name|getName
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
comment|// Ignore exception
name|LOG
operator|.
name|debug
argument_list|(
literal|"Rollback of setNamespaceQuota throws exception: "
operator|+
name|e
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
specifier|static
name|TableNamespaceManager
name|getTableNamespaceManager
parameter_list|(
specifier|final
name|MasterProcedureEnv
name|env
parameter_list|)
block|{
return|return
name|env
operator|.
name|getMasterServices
argument_list|()
operator|.
name|getClusterSchema
argument_list|()
operator|.
name|getTableNamespaceManager
argument_list|()
return|;
block|}
comment|/**    * The procedure could be restarted from a different machine. If the variable is null, we need to    * retrieve it.    * @return traceEnabled    */
specifier|private
name|Boolean
name|isTraceEnabled
parameter_list|()
block|{
if|if
condition|(
name|traceEnabled
operator|==
literal|null
condition|)
block|{
name|traceEnabled
operator|=
name|LOG
operator|.
name|isTraceEnabled
argument_list|()
expr_stmt|;
block|}
return|return
name|traceEnabled
return|;
block|}
annotation|@
name|Override
specifier|protected
name|boolean
name|shouldWaitClientAck
parameter_list|(
name|MasterProcedureEnv
name|env
parameter_list|)
block|{
comment|// hbase and default namespaces are created on bootstrap internally by the system
comment|// the client does not know about this procedures.
return|return
operator|!
name|isBootstrapNamespace
argument_list|()
return|;
block|}
block|}
end_class

end_unit

