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
name|NamespaceNotFoundException
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
name|ModifyNamespaceState
import|;
end_import

begin_comment
comment|/**  * The procedure to add a namespace to an existing table.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|ModifyNamespaceProcedure
extends|extends
name|AbstractStateMachineNamespaceProcedure
argument_list|<
name|ModifyNamespaceState
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
name|ModifyNamespaceProcedure
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|NamespaceDescriptor
name|oldNsDescriptor
decl_stmt|;
specifier|private
name|NamespaceDescriptor
name|newNsDescriptor
decl_stmt|;
specifier|private
name|Boolean
name|traceEnabled
decl_stmt|;
specifier|public
name|ModifyNamespaceProcedure
parameter_list|()
block|{
name|this
operator|.
name|oldNsDescriptor
operator|=
literal|null
expr_stmt|;
name|this
operator|.
name|traceEnabled
operator|=
literal|null
expr_stmt|;
block|}
specifier|public
name|ModifyNamespaceProcedure
parameter_list|(
specifier|final
name|MasterProcedureEnv
name|env
parameter_list|,
specifier|final
name|NamespaceDescriptor
name|newNsDescriptor
parameter_list|)
block|{
name|super
argument_list|(
name|env
argument_list|)
expr_stmt|;
name|this
operator|.
name|oldNsDescriptor
operator|=
literal|null
expr_stmt|;
name|this
operator|.
name|newNsDescriptor
operator|=
name|newNsDescriptor
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
name|ModifyNamespaceState
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
name|MODIFY_NAMESPACE_PREPARE
case|:
name|prepareModify
argument_list|(
name|env
argument_list|)
expr_stmt|;
name|setNextState
argument_list|(
name|ModifyNamespaceState
operator|.
name|MODIFY_NAMESPACE_UPDATE_NS_TABLE
argument_list|)
expr_stmt|;
break|break;
case|case
name|MODIFY_NAMESPACE_UPDATE_NS_TABLE
case|:
name|insertIntoNSTable
argument_list|(
name|env
argument_list|)
expr_stmt|;
name|setNextState
argument_list|(
name|ModifyNamespaceState
operator|.
name|MODIFY_NAMESPACE_UPDATE_ZK
argument_list|)
expr_stmt|;
break|break;
case|case
name|MODIFY_NAMESPACE_UPDATE_ZK
case|:
name|updateZKNamespaceManager
argument_list|(
name|env
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
literal|"master-modify-namespace"
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
literal|"Retriable error trying to modify namespace="
operator|+
name|newNsDescriptor
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
name|ModifyNamespaceState
name|state
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|state
operator|==
name|ModifyNamespaceState
operator|.
name|MODIFY_NAMESPACE_PREPARE
condition|)
block|{
comment|// nothing to rollback, pre-modify is just checks.
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
name|ModifyNamespaceState
name|state
parameter_list|)
block|{
switch|switch
condition|(
name|state
condition|)
block|{
case|case
name|MODIFY_NAMESPACE_PREPARE
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
name|ModifyNamespaceState
name|getState
parameter_list|(
specifier|final
name|int
name|stateId
parameter_list|)
block|{
return|return
name|ModifyNamespaceState
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
name|ModifyNamespaceState
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
name|ModifyNamespaceState
name|getInitialState
parameter_list|()
block|{
return|return
name|ModifyNamespaceState
operator|.
name|MODIFY_NAMESPACE_PREPARE
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
name|super
operator|.
name|serializeStateData
argument_list|(
name|serializer
argument_list|)
expr_stmt|;
name|MasterProcedureProtos
operator|.
name|ModifyNamespaceStateData
operator|.
name|Builder
name|modifyNamespaceMsg
init|=
name|MasterProcedureProtos
operator|.
name|ModifyNamespaceStateData
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
name|newNsDescriptor
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|this
operator|.
name|oldNsDescriptor
operator|!=
literal|null
condition|)
block|{
name|modifyNamespaceMsg
operator|.
name|setUnmodifiedNamespaceDescriptor
argument_list|(
name|ProtobufUtil
operator|.
name|toProtoNamespaceDescriptor
argument_list|(
name|this
operator|.
name|oldNsDescriptor
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|serializer
operator|.
name|serialize
argument_list|(
name|modifyNamespaceMsg
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
name|super
operator|.
name|deserializeStateData
argument_list|(
name|serializer
argument_list|)
expr_stmt|;
name|MasterProcedureProtos
operator|.
name|ModifyNamespaceStateData
name|modifyNamespaceMsg
init|=
name|serializer
operator|.
name|deserialize
argument_list|(
name|MasterProcedureProtos
operator|.
name|ModifyNamespaceStateData
operator|.
name|class
argument_list|)
decl_stmt|;
name|newNsDescriptor
operator|=
name|ProtobufUtil
operator|.
name|toNamespaceDescriptor
argument_list|(
name|modifyNamespaceMsg
operator|.
name|getNamespaceDescriptor
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|modifyNamespaceMsg
operator|.
name|hasUnmodifiedNamespaceDescriptor
argument_list|()
condition|)
block|{
name|oldNsDescriptor
operator|=
name|ProtobufUtil
operator|.
name|toNamespaceDescriptor
argument_list|(
name|modifyNamespaceMsg
operator|.
name|getUnmodifiedNamespaceDescriptor
argument_list|()
argument_list|)
expr_stmt|;
block|}
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
name|newNsDescriptor
operator|.
name|getName
argument_list|()
return|;
block|}
comment|/**    * Action before any real action of adding namespace.    * @param env MasterProcedureEnv    * @throws IOException    */
specifier|private
name|void
name|prepareModify
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
name|newNsDescriptor
operator|.
name|getName
argument_list|()
argument_list|)
operator|==
literal|false
condition|)
block|{
throw|throw
operator|new
name|NamespaceNotFoundException
argument_list|(
name|newNsDescriptor
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
name|newNsDescriptor
argument_list|)
expr_stmt|;
comment|// This is used for rollback
name|oldNsDescriptor
operator|=
name|getTableNamespaceManager
argument_list|(
name|env
argument_list|)
operator|.
name|get
argument_list|(
name|newNsDescriptor
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**    * Insert/update the row into namespace table    * @param env MasterProcedureEnv    * @throws IOException    */
specifier|private
name|void
name|insertIntoNSTable
parameter_list|(
specifier|final
name|MasterProcedureEnv
name|env
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
name|newNsDescriptor
argument_list|)
expr_stmt|;
block|}
comment|/**    * Update ZooKeeper.    * @param env MasterProcedureEnv    * @throws IOException    */
specifier|private
name|void
name|updateZKNamespaceManager
parameter_list|(
specifier|final
name|MasterProcedureEnv
name|env
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
name|newNsDescriptor
argument_list|)
expr_stmt|;
block|}
specifier|private
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
block|}
end_class

end_unit

