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
name|FileNotFoundException
import|;
end_import

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
name|fs
operator|.
name|FileStatus
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
name|HConstants
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
name|constraint
operator|.
name|ConstraintException
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
name|procedure2
operator|.
name|StateMachineProcedure
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
name|protobuf
operator|.
name|generated
operator|.
name|MasterProcedureProtos
operator|.
name|DeleteNamespaceState
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
comment|/**  * The procedure to remove a namespace.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|DeleteNamespaceProcedure
extends|extends
name|StateMachineProcedure
argument_list|<
name|MasterProcedureEnv
argument_list|,
name|DeleteNamespaceState
argument_list|>
implements|implements
name|TableProcedureInterface
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
name|DeleteNamespaceProcedure
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|AtomicBoolean
name|aborted
init|=
operator|new
name|AtomicBoolean
argument_list|(
literal|false
argument_list|)
decl_stmt|;
specifier|private
name|NamespaceDescriptor
name|nsDescriptor
decl_stmt|;
specifier|private
name|String
name|namespaceName
decl_stmt|;
specifier|private
name|Boolean
name|traceEnabled
decl_stmt|;
specifier|public
name|DeleteNamespaceProcedure
parameter_list|()
block|{
name|this
operator|.
name|nsDescriptor
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
name|DeleteNamespaceProcedure
parameter_list|(
specifier|final
name|MasterProcedureEnv
name|env
parameter_list|,
specifier|final
name|String
name|namespaceName
parameter_list|)
throws|throws
name|IOException
block|{
name|this
operator|.
name|namespaceName
operator|=
name|namespaceName
expr_stmt|;
name|this
operator|.
name|nsDescriptor
operator|=
literal|null
expr_stmt|;
name|this
operator|.
name|traceEnabled
operator|=
literal|null
expr_stmt|;
name|this
operator|.
name|setOwner
argument_list|(
name|env
operator|.
name|getRequestUser
argument_list|()
operator|.
name|getUGI
argument_list|()
operator|.
name|getShortUserName
argument_list|()
argument_list|)
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
name|DeleteNamespaceState
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
name|DELETE_NAMESPACE_PREPARE
case|:
name|prepareDelete
argument_list|(
name|env
argument_list|)
expr_stmt|;
name|setNextState
argument_list|(
name|DeleteNamespaceState
operator|.
name|DELETE_NAMESPACE_DELETE_FROM_NS_TABLE
argument_list|)
expr_stmt|;
break|break;
case|case
name|DELETE_NAMESPACE_DELETE_FROM_NS_TABLE
case|:
name|deleteFromNSTable
argument_list|(
name|env
argument_list|,
name|namespaceName
argument_list|)
expr_stmt|;
name|setNextState
argument_list|(
name|DeleteNamespaceState
operator|.
name|DELETE_NAMESPACE_REMOVE_FROM_ZK
argument_list|)
expr_stmt|;
break|break;
case|case
name|DELETE_NAMESPACE_REMOVE_FROM_ZK
case|:
name|removeFromZKNamespaceManager
argument_list|(
name|env
argument_list|,
name|namespaceName
argument_list|)
expr_stmt|;
name|setNextState
argument_list|(
name|DeleteNamespaceState
operator|.
name|DELETE_NAMESPACE_DELETE_DIRECTORIES
argument_list|)
expr_stmt|;
break|break;
case|case
name|DELETE_NAMESPACE_DELETE_DIRECTORIES
case|:
name|deleteDirectory
argument_list|(
name|env
argument_list|,
name|namespaceName
argument_list|)
expr_stmt|;
name|setNextState
argument_list|(
name|DeleteNamespaceState
operator|.
name|DELETE_NAMESPACE_REMOVE_NAMESPACE_QUOTA
argument_list|)
expr_stmt|;
break|break;
case|case
name|DELETE_NAMESPACE_REMOVE_NAMESPACE_QUOTA
case|:
name|removeNamespaceQuota
argument_list|(
name|env
argument_list|,
name|namespaceName
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
name|LOG
operator|.
name|warn
argument_list|(
literal|"Error trying to delete the namespace "
operator|+
name|namespaceName
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
name|setFailure
argument_list|(
literal|"master-delete-namespace"
argument_list|,
name|e
argument_list|)
expr_stmt|;
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
name|DeleteNamespaceState
name|state
parameter_list|)
throws|throws
name|IOException
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
literal|" rollback state="
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
name|DELETE_NAMESPACE_REMOVE_NAMESPACE_QUOTA
case|:
name|rollbacRemoveNamespaceQuota
argument_list|(
name|env
argument_list|)
expr_stmt|;
break|break;
case|case
name|DELETE_NAMESPACE_DELETE_DIRECTORIES
case|:
name|rollbackDeleteDirectory
argument_list|(
name|env
argument_list|)
expr_stmt|;
break|break;
case|case
name|DELETE_NAMESPACE_REMOVE_FROM_ZK
case|:
name|undoRemoveFromZKNamespaceManager
argument_list|(
name|env
argument_list|)
expr_stmt|;
break|break;
case|case
name|DELETE_NAMESPACE_DELETE_FROM_NS_TABLE
case|:
name|undoDeleteFromNSTable
argument_list|(
name|env
argument_list|)
expr_stmt|;
break|break;
case|case
name|DELETE_NAMESPACE_PREPARE
case|:
break|break;
comment|// nothing to do
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
comment|// This will be retried. Unless there is a bug in the code,
comment|// this should be just a "temporary error" (e.g. network down)
name|LOG
operator|.
name|warn
argument_list|(
literal|"Failed rollback attempt step "
operator|+
name|state
operator|+
literal|" for deleting the namespace "
operator|+
name|namespaceName
argument_list|,
name|e
argument_list|)
expr_stmt|;
throw|throw
name|e
throw|;
block|}
block|}
annotation|@
name|Override
specifier|protected
name|DeleteNamespaceState
name|getState
parameter_list|(
specifier|final
name|int
name|stateId
parameter_list|)
block|{
return|return
name|DeleteNamespaceState
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
name|DeleteNamespaceState
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
name|DeleteNamespaceState
name|getInitialState
parameter_list|()
block|{
return|return
name|DeleteNamespaceState
operator|.
name|DELETE_NAMESPACE_PREPARE
return|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|setNextState
parameter_list|(
name|DeleteNamespaceState
name|state
parameter_list|)
block|{
if|if
condition|(
name|aborted
operator|.
name|get
argument_list|()
condition|)
block|{
name|setAbortFailure
argument_list|(
literal|"delete-namespace"
argument_list|,
literal|"abort requested"
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|super
operator|.
name|setNextState
argument_list|(
name|state
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|abort
parameter_list|(
specifier|final
name|MasterProcedureEnv
name|env
parameter_list|)
block|{
name|aborted
operator|.
name|set
argument_list|(
literal|true
argument_list|)
expr_stmt|;
return|return
literal|true
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
name|DeleteNamespaceStateData
operator|.
name|Builder
name|deleteNamespaceMsg
init|=
name|MasterProcedureProtos
operator|.
name|DeleteNamespaceStateData
operator|.
name|newBuilder
argument_list|()
operator|.
name|setNamespaceName
argument_list|(
name|namespaceName
argument_list|)
decl_stmt|;
if|if
condition|(
name|this
operator|.
name|nsDescriptor
operator|!=
literal|null
condition|)
block|{
name|deleteNamespaceMsg
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
expr_stmt|;
block|}
name|deleteNamespaceMsg
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
name|DeleteNamespaceStateData
name|deleteNamespaceMsg
init|=
name|MasterProcedureProtos
operator|.
name|DeleteNamespaceStateData
operator|.
name|parseDelimitedFrom
argument_list|(
name|stream
argument_list|)
decl_stmt|;
name|namespaceName
operator|=
name|deleteNamespaceMsg
operator|.
name|getNamespaceName
argument_list|()
expr_stmt|;
if|if
condition|(
name|deleteNamespaceMsg
operator|.
name|hasNamespaceDescriptor
argument_list|()
condition|)
block|{
name|nsDescriptor
operator|=
name|ProtobufUtil
operator|.
name|toNamespaceDescriptor
argument_list|(
name|deleteNamespaceMsg
operator|.
name|getNamespaceDescriptor
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|toStringClassDetails
parameter_list|(
name|StringBuilder
name|sb
parameter_list|)
block|{
name|sb
operator|.
name|append
argument_list|(
name|getClass
argument_list|()
operator|.
name|getSimpleName
argument_list|()
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|" (Namespace="
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|namespaceName
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|")"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|boolean
name|acquireLock
parameter_list|(
specifier|final
name|MasterProcedureEnv
name|env
parameter_list|)
block|{
if|if
condition|(
name|env
operator|.
name|waitInitialized
argument_list|(
name|this
argument_list|)
condition|)
return|return
literal|false
return|;
return|return
name|env
operator|.
name|getProcedureQueue
argument_list|()
operator|.
name|tryAcquireNamespaceExclusiveLock
argument_list|(
name|this
argument_list|,
name|getNamespaceName
argument_list|()
argument_list|)
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
name|env
operator|.
name|getProcedureQueue
argument_list|()
operator|.
name|releaseNamespaceExclusiveLock
argument_list|(
name|this
argument_list|,
name|getNamespaceName
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|TableName
name|getTableName
parameter_list|()
block|{
return|return
name|TableName
operator|.
name|NAMESPACE_TABLE_NAME
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
specifier|private
name|String
name|getNamespaceName
parameter_list|()
block|{
return|return
name|namespaceName
return|;
block|}
comment|/**    * Action before any real action of deleting namespace.    * @param env MasterProcedureEnv    * @throws IOException    */
specifier|private
name|void
name|prepareDelete
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
name|namespaceName
argument_list|)
operator|==
literal|false
condition|)
block|{
throw|throw
operator|new
name|NamespaceNotFoundException
argument_list|(
name|namespaceName
argument_list|)
throw|;
block|}
if|if
condition|(
name|NamespaceDescriptor
operator|.
name|RESERVED_NAMESPACES
operator|.
name|contains
argument_list|(
name|namespaceName
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|ConstraintException
argument_list|(
literal|"Reserved namespace "
operator|+
name|namespaceName
operator|+
literal|" cannot be removed."
argument_list|)
throw|;
block|}
name|int
name|tableCount
init|=
literal|0
decl_stmt|;
try|try
block|{
name|tableCount
operator|=
name|env
operator|.
name|getMasterServices
argument_list|()
operator|.
name|listTableDescriptorsByNamespace
argument_list|(
name|namespaceName
argument_list|)
operator|.
name|size
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|FileNotFoundException
name|fnfe
parameter_list|)
block|{
throw|throw
operator|new
name|NamespaceNotFoundException
argument_list|(
name|namespaceName
argument_list|)
throw|;
block|}
if|if
condition|(
name|tableCount
operator|>
literal|0
condition|)
block|{
throw|throw
operator|new
name|ConstraintException
argument_list|(
literal|"Only empty namespaces can be removed. "
operator|+
literal|"Namespace "
operator|+
name|namespaceName
operator|+
literal|" has "
operator|+
name|tableCount
operator|+
literal|" tables"
argument_list|)
throw|;
block|}
comment|// This is used for rollback
name|nsDescriptor
operator|=
name|getTableNamespaceManager
argument_list|(
name|env
argument_list|)
operator|.
name|get
argument_list|(
name|namespaceName
argument_list|)
expr_stmt|;
block|}
comment|/**    * delete the row from namespace table    * @param env MasterProcedureEnv    * @param namespaceName name of the namespace in string format    * @throws IOException    */
specifier|protected
specifier|static
name|void
name|deleteFromNSTable
parameter_list|(
specifier|final
name|MasterProcedureEnv
name|env
parameter_list|,
specifier|final
name|String
name|namespaceName
parameter_list|)
throws|throws
name|IOException
block|{
name|getTableNamespaceManager
argument_list|(
name|env
argument_list|)
operator|.
name|removeFromNSTable
argument_list|(
name|namespaceName
argument_list|)
expr_stmt|;
block|}
comment|/**    * undo the delete    * @param env MasterProcedureEnv    * @throws IOException    */
specifier|private
name|void
name|undoDeleteFromNSTable
parameter_list|(
specifier|final
name|MasterProcedureEnv
name|env
parameter_list|)
block|{
try|try
block|{
if|if
condition|(
name|nsDescriptor
operator|!=
literal|null
condition|)
block|{
name|CreateNamespaceProcedure
operator|.
name|insertIntoNSTable
argument_list|(
name|env
argument_list|,
name|nsDescriptor
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
comment|// Ignore
name|LOG
operator|.
name|debug
argument_list|(
literal|"Rollback of deleteFromNSTable throws exception: "
operator|+
name|e
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * remove from Zookeeper.    * @param env MasterProcedureEnv    * @param namespaceName name of the namespace in string format    * @throws IOException    */
specifier|protected
specifier|static
name|void
name|removeFromZKNamespaceManager
parameter_list|(
specifier|final
name|MasterProcedureEnv
name|env
parameter_list|,
specifier|final
name|String
name|namespaceName
parameter_list|)
throws|throws
name|IOException
block|{
name|getTableNamespaceManager
argument_list|(
name|env
argument_list|)
operator|.
name|removeFromZKNamespaceManager
argument_list|(
name|namespaceName
argument_list|)
expr_stmt|;
block|}
comment|/**    * undo the remove from Zookeeper    * @param env MasterProcedureEnv    * @throws IOException    */
specifier|private
name|void
name|undoRemoveFromZKNamespaceManager
parameter_list|(
specifier|final
name|MasterProcedureEnv
name|env
parameter_list|)
block|{
try|try
block|{
if|if
condition|(
name|nsDescriptor
operator|!=
literal|null
condition|)
block|{
name|CreateNamespaceProcedure
operator|.
name|updateZKNamespaceManager
argument_list|(
name|env
argument_list|,
name|nsDescriptor
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
comment|// Ignore
name|LOG
operator|.
name|debug
argument_list|(
literal|"Rollback of removeFromZKNamespaceManager throws exception: "
operator|+
name|e
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Delete the namespace directories from the file system    * @param env MasterProcedureEnv    * @param namespaceName name of the namespace in string format    * @throws IOException    */
specifier|protected
specifier|static
name|void
name|deleteDirectory
parameter_list|(
specifier|final
name|MasterProcedureEnv
name|env
parameter_list|,
specifier|final
name|String
name|namespaceName
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
name|FileSystem
name|fs
init|=
name|mfs
operator|.
name|getFileSystem
argument_list|()
decl_stmt|;
name|Path
name|p
init|=
name|FSUtils
operator|.
name|getNamespaceDir
argument_list|(
name|mfs
operator|.
name|getRootDir
argument_list|()
argument_list|,
name|namespaceName
argument_list|)
decl_stmt|;
try|try
block|{
for|for
control|(
name|FileStatus
name|status
range|:
name|fs
operator|.
name|listStatus
argument_list|(
name|p
argument_list|)
control|)
block|{
if|if
condition|(
operator|!
name|HConstants
operator|.
name|HBASE_NON_TABLE_DIRS
operator|.
name|contains
argument_list|(
name|status
operator|.
name|getPath
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Namespace directory contains table dir: "
operator|+
name|status
operator|.
name|getPath
argument_list|()
argument_list|)
throw|;
block|}
block|}
if|if
condition|(
operator|!
name|fs
operator|.
name|delete
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
name|namespaceName
argument_list|)
argument_list|,
literal|true
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Failed to remove namespace: "
operator|+
name|namespaceName
argument_list|)
throw|;
block|}
block|}
catch|catch
parameter_list|(
name|FileNotFoundException
name|e
parameter_list|)
block|{
comment|// File already deleted, continue
name|LOG
operator|.
name|debug
argument_list|(
literal|"deleteDirectory throws exception: "
operator|+
name|e
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * undo delete directory    * @param env MasterProcedureEnv    * @throws IOException    */
specifier|private
name|void
name|rollbackDeleteDirectory
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
name|CreateNamespaceProcedure
operator|.
name|createDirectory
argument_list|(
name|env
argument_list|,
name|nsDescriptor
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
literal|"Rollback of deleteDirectory throws exception: "
operator|+
name|e
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * remove quota for the namespace    * @param env MasterProcedureEnv    * @param namespaceName name of the namespace in string format    * @throws IOException    **/
specifier|protected
specifier|static
name|void
name|removeNamespaceQuota
parameter_list|(
specifier|final
name|MasterProcedureEnv
name|env
parameter_list|,
specifier|final
name|String
name|namespaceName
parameter_list|)
throws|throws
name|IOException
block|{
name|env
operator|.
name|getMasterServices
argument_list|()
operator|.
name|getMasterQuotaManager
argument_list|()
operator|.
name|removeNamespaceQuota
argument_list|(
name|namespaceName
argument_list|)
expr_stmt|;
block|}
comment|/**    * undo remove quota for the namespace    * @param env MasterProcedureEnv    * @throws IOException    **/
specifier|private
name|void
name|rollbacRemoveNamespaceQuota
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
name|CreateNamespaceProcedure
operator|.
name|setNamespaceQuota
argument_list|(
name|env
argument_list|,
name|nsDescriptor
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
literal|"Rollback of removeNamespaceQuota throws exception: "
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
block|}
end_class

end_unit

