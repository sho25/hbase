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
name|rsgroup
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
name|HashSet
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
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Sets
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|RpcCallback
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|RpcController
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|Service
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
name|CoprocessorEnvironment
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
name|HRegionInfo
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
name|HTableDescriptor
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
name|coprocessor
operator|.
name|CoprocessorService
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
name|coprocessor
operator|.
name|MasterCoprocessorEnvironment
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
name|coprocessor
operator|.
name|MasterObserver
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
name|coprocessor
operator|.
name|ObserverContext
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
name|CoprocessorRpcUtils
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
name|net
operator|.
name|Address
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
name|HBaseProtos
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
name|RSGroupAdminProtos
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
name|RSGroupAdminProtos
operator|.
name|AddRSGroupRequest
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
name|RSGroupAdminProtos
operator|.
name|AddRSGroupResponse
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
name|RSGroupAdminProtos
operator|.
name|BalanceRSGroupRequest
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
name|RSGroupAdminProtos
operator|.
name|BalanceRSGroupResponse
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
name|RSGroupAdminProtos
operator|.
name|GetRSGroupInfoOfServerRequest
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
name|RSGroupAdminProtos
operator|.
name|GetRSGroupInfoOfServerResponse
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
name|RSGroupAdminProtos
operator|.
name|GetRSGroupInfoOfTableRequest
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
name|RSGroupAdminProtos
operator|.
name|GetRSGroupInfoOfTableResponse
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
name|RSGroupAdminProtos
operator|.
name|GetRSGroupInfoRequest
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
name|RSGroupAdminProtos
operator|.
name|GetRSGroupInfoResponse
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
name|RSGroupAdminProtos
operator|.
name|ListRSGroupInfosRequest
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
name|RSGroupAdminProtos
operator|.
name|ListRSGroupInfosResponse
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
name|RSGroupAdminProtos
operator|.
name|MoveServersRequest
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
name|RSGroupAdminProtos
operator|.
name|MoveServersResponse
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
name|RSGroupAdminProtos
operator|.
name|MoveTablesRequest
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
name|RSGroupAdminProtos
operator|.
name|MoveTablesResponse
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
name|RSGroupAdminProtos
operator|.
name|RSGroupAdminService
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
name|RSGroupAdminProtos
operator|.
name|RemoveRSGroupRequest
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
name|RSGroupAdminProtos
operator|.
name|RemoveRSGroupResponse
import|;
end_import

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|RSGroupAdminEndpoint
implements|implements
name|MasterObserver
implements|,
name|CoprocessorService
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
name|RSGroupAdminEndpoint
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|MasterServices
name|master
init|=
literal|null
decl_stmt|;
comment|// Only instance of RSGroupInfoManager. RSGroup aware load balancers ask for this instance on
comment|// their setup.
specifier|private
name|RSGroupInfoManager
name|groupInfoManager
decl_stmt|;
specifier|private
name|RSGroupAdminServer
name|groupAdminServer
decl_stmt|;
specifier|private
specifier|final
name|RSGroupAdminService
name|groupAdminService
init|=
operator|new
name|RSGroupAdminServiceImpl
argument_list|()
decl_stmt|;
annotation|@
name|Override
specifier|public
name|void
name|start
parameter_list|(
name|CoprocessorEnvironment
name|env
parameter_list|)
throws|throws
name|IOException
block|{
name|master
operator|=
operator|(
operator|(
name|MasterCoprocessorEnvironment
operator|)
name|env
operator|)
operator|.
name|getMasterServices
argument_list|()
expr_stmt|;
name|groupInfoManager
operator|=
name|RSGroupInfoManagerImpl
operator|.
name|getInstance
argument_list|(
name|master
argument_list|)
expr_stmt|;
name|groupAdminServer
operator|=
operator|new
name|RSGroupAdminServer
argument_list|(
name|master
argument_list|,
name|groupInfoManager
argument_list|)
expr_stmt|;
name|Class
argument_list|<
name|?
argument_list|>
name|clazz
init|=
name|master
operator|.
name|getConfiguration
argument_list|()
operator|.
name|getClass
argument_list|(
name|HConstants
operator|.
name|HBASE_MASTER_LOADBALANCER_CLASS
argument_list|,
literal|null
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|RSGroupableBalancer
operator|.
name|class
operator|.
name|isAssignableFrom
argument_list|(
name|clazz
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Configured balancer does not support RegionServer groups."
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|Service
name|getService
parameter_list|()
block|{
return|return
name|groupAdminService
return|;
block|}
name|RSGroupInfoManager
name|getGroupInfoManager
parameter_list|()
block|{
return|return
name|groupInfoManager
return|;
block|}
comment|/**    * Implementation of RSGroupAdminService defined in RSGroupAdmin.proto.    * This class calls {@link RSGroupAdminServer} for actual work, converts result to protocol    * buffer response, handles exceptions if any occurred and then calls the {@code RpcCallback} with    * the response.    * Since our CoprocessorHost asks the Coprocessor for a Service    * ({@link CoprocessorService#getService()}) instead of doing "coproc instanceOf Service"    * and requiring Coprocessor itself to be Service (something we do with our observers),    * we can use composition instead of inheritance here. That makes it easy to manage    * functionalities in concise classes (sometimes inner classes) instead of single class doing    * many different things.    */
specifier|private
class|class
name|RSGroupAdminServiceImpl
extends|extends
name|RSGroupAdminProtos
operator|.
name|RSGroupAdminService
block|{
annotation|@
name|Override
specifier|public
name|void
name|getRSGroupInfo
parameter_list|(
name|RpcController
name|controller
parameter_list|,
name|GetRSGroupInfoRequest
name|request
parameter_list|,
name|RpcCallback
argument_list|<
name|GetRSGroupInfoResponse
argument_list|>
name|done
parameter_list|)
block|{
name|GetRSGroupInfoResponse
operator|.
name|Builder
name|builder
init|=
name|GetRSGroupInfoResponse
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
name|String
name|groupName
init|=
name|request
operator|.
name|getRSGroupName
argument_list|()
decl_stmt|;
try|try
block|{
name|RSGroupInfo
name|rsGroupInfo
init|=
name|groupAdminServer
operator|.
name|getRSGroupInfo
argument_list|(
name|groupName
argument_list|)
decl_stmt|;
if|if
condition|(
name|rsGroupInfo
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|setRSGroupInfo
argument_list|(
name|RSGroupProtobufUtil
operator|.
name|toProtoGroupInfo
argument_list|(
name|rsGroupInfo
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|CoprocessorRpcUtils
operator|.
name|setControllerException
argument_list|(
name|controller
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
name|done
operator|.
name|run
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
specifier|public
name|void
name|getRSGroupInfoOfTable
parameter_list|(
name|RpcController
name|controller
parameter_list|,
name|GetRSGroupInfoOfTableRequest
name|request
parameter_list|,
name|RpcCallback
argument_list|<
name|GetRSGroupInfoOfTableResponse
argument_list|>
name|done
parameter_list|)
block|{
name|GetRSGroupInfoOfTableResponse
operator|.
name|Builder
name|builder
init|=
name|GetRSGroupInfoOfTableResponse
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
try|try
block|{
name|TableName
name|tableName
init|=
name|ProtobufUtil
operator|.
name|toTableName
argument_list|(
name|request
operator|.
name|getTableName
argument_list|()
argument_list|)
decl_stmt|;
name|RSGroupInfo
name|RSGroupInfo
init|=
name|groupAdminServer
operator|.
name|getRSGroupInfoOfTable
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
if|if
condition|(
name|RSGroupInfo
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|setRSGroupInfo
argument_list|(
name|RSGroupProtobufUtil
operator|.
name|toProtoGroupInfo
argument_list|(
name|RSGroupInfo
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|CoprocessorRpcUtils
operator|.
name|setControllerException
argument_list|(
name|controller
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
name|done
operator|.
name|run
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
specifier|public
name|void
name|moveServers
parameter_list|(
name|RpcController
name|controller
parameter_list|,
name|MoveServersRequest
name|request
parameter_list|,
name|RpcCallback
argument_list|<
name|MoveServersResponse
argument_list|>
name|done
parameter_list|)
block|{
name|MoveServersResponse
operator|.
name|Builder
name|builder
init|=
name|MoveServersResponse
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
try|try
block|{
name|Set
argument_list|<
name|Address
argument_list|>
name|hostPorts
init|=
name|Sets
operator|.
name|newHashSet
argument_list|()
decl_stmt|;
for|for
control|(
name|HBaseProtos
operator|.
name|ServerName
name|el
range|:
name|request
operator|.
name|getServersList
argument_list|()
control|)
block|{
name|hostPorts
operator|.
name|add
argument_list|(
name|Address
operator|.
name|fromParts
argument_list|(
name|el
operator|.
name|getHostName
argument_list|()
argument_list|,
name|el
operator|.
name|getPort
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|groupAdminServer
operator|.
name|moveServers
argument_list|(
name|hostPorts
argument_list|,
name|request
operator|.
name|getTargetGroup
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|CoprocessorRpcUtils
operator|.
name|setControllerException
argument_list|(
name|controller
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
name|done
operator|.
name|run
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
specifier|public
name|void
name|moveTables
parameter_list|(
name|RpcController
name|controller
parameter_list|,
name|MoveTablesRequest
name|request
parameter_list|,
name|RpcCallback
argument_list|<
name|MoveTablesResponse
argument_list|>
name|done
parameter_list|)
block|{
name|MoveTablesResponse
operator|.
name|Builder
name|builder
init|=
name|MoveTablesResponse
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
try|try
block|{
name|Set
argument_list|<
name|TableName
argument_list|>
name|tables
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|(
name|request
operator|.
name|getTableNameList
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|HBaseProtos
operator|.
name|TableName
name|tableName
range|:
name|request
operator|.
name|getTableNameList
argument_list|()
control|)
block|{
name|tables
operator|.
name|add
argument_list|(
name|ProtobufUtil
operator|.
name|toTableName
argument_list|(
name|tableName
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|groupAdminServer
operator|.
name|moveTables
argument_list|(
name|tables
argument_list|,
name|request
operator|.
name|getTargetGroup
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|CoprocessorRpcUtils
operator|.
name|setControllerException
argument_list|(
name|controller
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
name|done
operator|.
name|run
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
specifier|public
name|void
name|addRSGroup
parameter_list|(
name|RpcController
name|controller
parameter_list|,
name|AddRSGroupRequest
name|request
parameter_list|,
name|RpcCallback
argument_list|<
name|AddRSGroupResponse
argument_list|>
name|done
parameter_list|)
block|{
name|AddRSGroupResponse
operator|.
name|Builder
name|builder
init|=
name|AddRSGroupResponse
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
try|try
block|{
name|groupAdminServer
operator|.
name|addRSGroup
argument_list|(
name|request
operator|.
name|getRSGroupName
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|CoprocessorRpcUtils
operator|.
name|setControllerException
argument_list|(
name|controller
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
name|done
operator|.
name|run
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
specifier|public
name|void
name|removeRSGroup
parameter_list|(
name|RpcController
name|controller
parameter_list|,
name|RemoveRSGroupRequest
name|request
parameter_list|,
name|RpcCallback
argument_list|<
name|RemoveRSGroupResponse
argument_list|>
name|done
parameter_list|)
block|{
name|RemoveRSGroupResponse
operator|.
name|Builder
name|builder
init|=
name|RemoveRSGroupResponse
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
try|try
block|{
name|groupAdminServer
operator|.
name|removeRSGroup
argument_list|(
name|request
operator|.
name|getRSGroupName
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|CoprocessorRpcUtils
operator|.
name|setControllerException
argument_list|(
name|controller
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
name|done
operator|.
name|run
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
specifier|public
name|void
name|balanceRSGroup
parameter_list|(
name|RpcController
name|controller
parameter_list|,
name|BalanceRSGroupRequest
name|request
parameter_list|,
name|RpcCallback
argument_list|<
name|BalanceRSGroupResponse
argument_list|>
name|done
parameter_list|)
block|{
name|BalanceRSGroupResponse
operator|.
name|Builder
name|builder
init|=
name|BalanceRSGroupResponse
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
try|try
block|{
name|builder
operator|.
name|setBalanceRan
argument_list|(
name|groupAdminServer
operator|.
name|balanceRSGroup
argument_list|(
name|request
operator|.
name|getRSGroupName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|CoprocessorRpcUtils
operator|.
name|setControllerException
argument_list|(
name|controller
argument_list|,
name|e
argument_list|)
expr_stmt|;
name|builder
operator|.
name|setBalanceRan
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
name|done
operator|.
name|run
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
specifier|public
name|void
name|listRSGroupInfos
parameter_list|(
name|RpcController
name|controller
parameter_list|,
name|ListRSGroupInfosRequest
name|request
parameter_list|,
name|RpcCallback
argument_list|<
name|ListRSGroupInfosResponse
argument_list|>
name|done
parameter_list|)
block|{
name|ListRSGroupInfosResponse
operator|.
name|Builder
name|builder
init|=
name|ListRSGroupInfosResponse
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
try|try
block|{
for|for
control|(
name|RSGroupInfo
name|RSGroupInfo
range|:
name|groupAdminServer
operator|.
name|listRSGroups
argument_list|()
control|)
block|{
name|builder
operator|.
name|addRSGroupInfo
argument_list|(
name|RSGroupProtobufUtil
operator|.
name|toProtoGroupInfo
argument_list|(
name|RSGroupInfo
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|CoprocessorRpcUtils
operator|.
name|setControllerException
argument_list|(
name|controller
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
name|done
operator|.
name|run
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
specifier|public
name|void
name|getRSGroupInfoOfServer
parameter_list|(
name|RpcController
name|controller
parameter_list|,
name|GetRSGroupInfoOfServerRequest
name|request
parameter_list|,
name|RpcCallback
argument_list|<
name|GetRSGroupInfoOfServerResponse
argument_list|>
name|done
parameter_list|)
block|{
name|GetRSGroupInfoOfServerResponse
operator|.
name|Builder
name|builder
init|=
name|GetRSGroupInfoOfServerResponse
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
try|try
block|{
name|Address
name|hp
init|=
name|Address
operator|.
name|fromParts
argument_list|(
name|request
operator|.
name|getServer
argument_list|()
operator|.
name|getHostName
argument_list|()
argument_list|,
name|request
operator|.
name|getServer
argument_list|()
operator|.
name|getPort
argument_list|()
argument_list|)
decl_stmt|;
name|RSGroupInfo
name|RSGroupInfo
init|=
name|groupAdminServer
operator|.
name|getRSGroupOfServer
argument_list|(
name|hp
argument_list|)
decl_stmt|;
if|if
condition|(
name|RSGroupInfo
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|setRSGroupInfo
argument_list|(
name|RSGroupProtobufUtil
operator|.
name|toProtoGroupInfo
argument_list|(
name|RSGroupInfo
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|CoprocessorRpcUtils
operator|.
name|setControllerException
argument_list|(
name|controller
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
name|done
operator|.
name|run
argument_list|(
name|builder
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
comment|/////////////////////////////////////////////////////////////////////////////
comment|// MasterObserver overrides
comment|/////////////////////////////////////////////////////////////////////////////
comment|// Assign table to default RSGroup.
annotation|@
name|Override
specifier|public
name|void
name|preCreateTable
parameter_list|(
name|ObserverContext
argument_list|<
name|MasterCoprocessorEnvironment
argument_list|>
name|ctx
parameter_list|,
name|HTableDescriptor
name|desc
parameter_list|,
name|HRegionInfo
index|[]
name|regions
parameter_list|)
throws|throws
name|IOException
block|{
name|String
name|groupName
init|=
name|master
operator|.
name|getClusterSchema
argument_list|()
operator|.
name|getNamespace
argument_list|(
name|desc
operator|.
name|getTableName
argument_list|()
operator|.
name|getNamespaceAsString
argument_list|()
argument_list|)
operator|.
name|getConfigurationValue
argument_list|(
name|RSGroupInfo
operator|.
name|NAMESPACE_DESC_PROP_GROUP
argument_list|)
decl_stmt|;
if|if
condition|(
name|groupName
operator|==
literal|null
condition|)
block|{
name|groupName
operator|=
name|RSGroupInfo
operator|.
name|DEFAULT_GROUP
expr_stmt|;
block|}
name|RSGroupInfo
name|rsGroupInfo
init|=
name|groupAdminServer
operator|.
name|getRSGroupInfo
argument_list|(
name|groupName
argument_list|)
decl_stmt|;
if|if
condition|(
name|rsGroupInfo
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|ConstraintException
argument_list|(
literal|"Default RSGroup ("
operator|+
name|groupName
operator|+
literal|") for this table's "
operator|+
literal|"namespace does not exist."
argument_list|)
throw|;
block|}
if|if
condition|(
operator|!
name|rsGroupInfo
operator|.
name|containsTable
argument_list|(
name|desc
operator|.
name|getTableName
argument_list|()
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Pre-moving table "
operator|+
name|desc
operator|.
name|getTableName
argument_list|()
operator|+
literal|" to RSGroup "
operator|+
name|groupName
argument_list|)
expr_stmt|;
name|groupAdminServer
operator|.
name|moveTables
argument_list|(
name|Sets
operator|.
name|newHashSet
argument_list|(
name|desc
operator|.
name|getTableName
argument_list|()
argument_list|)
argument_list|,
name|groupName
argument_list|)
expr_stmt|;
block|}
block|}
comment|// Remove table from its RSGroup.
annotation|@
name|Override
specifier|public
name|void
name|postDeleteTable
parameter_list|(
name|ObserverContext
argument_list|<
name|MasterCoprocessorEnvironment
argument_list|>
name|ctx
parameter_list|,
name|TableName
name|tableName
parameter_list|)
throws|throws
name|IOException
block|{
try|try
block|{
name|RSGroupInfo
name|group
init|=
name|groupAdminServer
operator|.
name|getRSGroupInfoOfTable
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
if|if
condition|(
name|group
operator|!=
literal|null
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"Removing deleted table '%s' from rsgroup '%s'"
argument_list|,
name|tableName
argument_list|,
name|group
operator|.
name|getName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|groupAdminServer
operator|.
name|moveTables
argument_list|(
name|Sets
operator|.
name|newHashSet
argument_list|(
name|tableName
argument_list|)
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|ex
parameter_list|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Failed to perform RSGroup information cleanup for table: "
operator|+
name|tableName
argument_list|,
name|ex
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|preCreateNamespace
parameter_list|(
name|ObserverContext
argument_list|<
name|MasterCoprocessorEnvironment
argument_list|>
name|ctx
parameter_list|,
name|NamespaceDescriptor
name|ns
parameter_list|)
throws|throws
name|IOException
block|{
name|String
name|group
init|=
name|ns
operator|.
name|getConfigurationValue
argument_list|(
name|RSGroupInfo
operator|.
name|NAMESPACE_DESC_PROP_GROUP
argument_list|)
decl_stmt|;
if|if
condition|(
name|group
operator|!=
literal|null
operator|&&
name|groupAdminServer
operator|.
name|getRSGroupInfo
argument_list|(
name|group
argument_list|)
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|ConstraintException
argument_list|(
literal|"Region server group "
operator|+
name|group
operator|+
literal|" does not exit"
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|preModifyNamespace
parameter_list|(
name|ObserverContext
argument_list|<
name|MasterCoprocessorEnvironment
argument_list|>
name|ctx
parameter_list|,
name|NamespaceDescriptor
name|ns
parameter_list|)
throws|throws
name|IOException
block|{
name|preCreateNamespace
argument_list|(
name|ctx
argument_list|,
name|ns
argument_list|)
expr_stmt|;
block|}
comment|/////////////////////////////////////////////////////////////////////////////
block|}
end_class

end_unit

