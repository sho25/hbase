begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|common
operator|.
name|net
operator|.
name|HostAndPort
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
name|ServiceException
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
name|classification
operator|.
name|InterfaceStability
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
name|Connection
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
name|RSGroupProtos
import|;
end_import

begin_comment
comment|/**  * Client used for managing region server group information.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Public
annotation|@
name|InterfaceStability
operator|.
name|Evolving
class|class
name|RSGroupAdminClient
extends|extends
name|RSGroupAdmin
block|{
specifier|private
name|RSGroupAdminProtos
operator|.
name|RSGroupAdminService
operator|.
name|BlockingInterface
name|proxy
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
name|RSGroupAdminClient
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|public
name|RSGroupAdminClient
parameter_list|(
name|Connection
name|conn
parameter_list|)
throws|throws
name|IOException
block|{
name|proxy
operator|=
name|RSGroupAdminProtos
operator|.
name|RSGroupAdminService
operator|.
name|newBlockingStub
argument_list|(
name|conn
operator|.
name|getAdmin
argument_list|()
operator|.
name|coprocessorService
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|RSGroupInfo
name|getRSGroupInfo
parameter_list|(
name|String
name|groupName
parameter_list|)
throws|throws
name|IOException
block|{
try|try
block|{
name|RSGroupAdminProtos
operator|.
name|GetRSGroupInfoResponse
name|resp
init|=
name|proxy
operator|.
name|getRSGroupInfo
argument_list|(
literal|null
argument_list|,
name|RSGroupAdminProtos
operator|.
name|GetRSGroupInfoRequest
operator|.
name|newBuilder
argument_list|()
operator|.
name|setRSGroupName
argument_list|(
name|groupName
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|resp
operator|.
name|hasRSGroupInfo
argument_list|()
condition|)
block|{
return|return
name|ProtobufUtil
operator|.
name|toGroupInfo
argument_list|(
name|resp
operator|.
name|getRSGroupInfo
argument_list|()
argument_list|)
return|;
block|}
return|return
literal|null
return|;
block|}
catch|catch
parameter_list|(
name|ServiceException
name|e
parameter_list|)
block|{
throw|throw
name|ProtobufUtil
operator|.
name|getRemoteException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|RSGroupInfo
name|getRSGroupInfoOfTable
parameter_list|(
name|TableName
name|tableName
parameter_list|)
throws|throws
name|IOException
block|{
name|RSGroupAdminProtos
operator|.
name|GetRSGroupInfoOfTableRequest
name|request
init|=
name|RSGroupAdminProtos
operator|.
name|GetRSGroupInfoOfTableRequest
operator|.
name|newBuilder
argument_list|()
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
operator|.
name|build
argument_list|()
decl_stmt|;
try|try
block|{
name|GetRSGroupInfoOfTableResponse
name|resp
init|=
name|proxy
operator|.
name|getRSGroupInfoOfTable
argument_list|(
literal|null
argument_list|,
name|request
argument_list|)
decl_stmt|;
if|if
condition|(
name|resp
operator|.
name|hasRSGroupInfo
argument_list|()
condition|)
block|{
return|return
name|ProtobufUtil
operator|.
name|toGroupInfo
argument_list|(
name|resp
operator|.
name|getRSGroupInfo
argument_list|()
argument_list|)
return|;
block|}
return|return
literal|null
return|;
block|}
catch|catch
parameter_list|(
name|ServiceException
name|e
parameter_list|)
block|{
throw|throw
name|ProtobufUtil
operator|.
name|getRemoteException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|moveServers
parameter_list|(
name|Set
argument_list|<
name|HostAndPort
argument_list|>
name|servers
parameter_list|,
name|String
name|targetGroup
parameter_list|)
throws|throws
name|IOException
block|{
name|Set
argument_list|<
name|HBaseProtos
operator|.
name|ServerName
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
name|HostAndPort
name|el
range|:
name|servers
control|)
block|{
name|hostPorts
operator|.
name|add
argument_list|(
name|HBaseProtos
operator|.
name|ServerName
operator|.
name|newBuilder
argument_list|()
operator|.
name|setHostName
argument_list|(
name|el
operator|.
name|getHostText
argument_list|()
argument_list|)
operator|.
name|setPort
argument_list|(
name|el
operator|.
name|getPort
argument_list|()
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|RSGroupAdminProtos
operator|.
name|MoveServersRequest
name|request
init|=
name|RSGroupAdminProtos
operator|.
name|MoveServersRequest
operator|.
name|newBuilder
argument_list|()
operator|.
name|setTargetGroup
argument_list|(
name|targetGroup
argument_list|)
operator|.
name|addAllServers
argument_list|(
name|hostPorts
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
try|try
block|{
name|proxy
operator|.
name|moveServers
argument_list|(
literal|null
argument_list|,
name|request
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ServiceException
name|e
parameter_list|)
block|{
throw|throw
name|ProtobufUtil
operator|.
name|getRemoteException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|moveTables
parameter_list|(
name|Set
argument_list|<
name|TableName
argument_list|>
name|tables
parameter_list|,
name|String
name|targetGroup
parameter_list|)
throws|throws
name|IOException
block|{
name|RSGroupAdminProtos
operator|.
name|MoveTablesRequest
operator|.
name|Builder
name|builder
init|=
name|RSGroupAdminProtos
operator|.
name|MoveTablesRequest
operator|.
name|newBuilder
argument_list|()
operator|.
name|setTargetGroup
argument_list|(
name|targetGroup
argument_list|)
decl_stmt|;
for|for
control|(
name|TableName
name|tableName
range|:
name|tables
control|)
block|{
name|builder
operator|.
name|addTableName
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
try|try
block|{
name|proxy
operator|.
name|moveTables
argument_list|(
literal|null
argument_list|,
name|builder
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ServiceException
name|e
parameter_list|)
block|{
throw|throw
name|ProtobufUtil
operator|.
name|getRemoteException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|addRSGroup
parameter_list|(
name|String
name|groupName
parameter_list|)
throws|throws
name|IOException
block|{
name|RSGroupAdminProtos
operator|.
name|AddRSGroupRequest
name|request
init|=
name|RSGroupAdminProtos
operator|.
name|AddRSGroupRequest
operator|.
name|newBuilder
argument_list|()
operator|.
name|setRSGroupName
argument_list|(
name|groupName
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
try|try
block|{
name|proxy
operator|.
name|addRSGroup
argument_list|(
literal|null
argument_list|,
name|request
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ServiceException
name|e
parameter_list|)
block|{
throw|throw
name|ProtobufUtil
operator|.
name|getRemoteException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|removeRSGroup
parameter_list|(
name|String
name|name
parameter_list|)
throws|throws
name|IOException
block|{
name|RSGroupAdminProtos
operator|.
name|RemoveRSGroupRequest
name|request
init|=
name|RSGroupAdminProtos
operator|.
name|RemoveRSGroupRequest
operator|.
name|newBuilder
argument_list|()
operator|.
name|setRSGroupName
argument_list|(
name|name
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
try|try
block|{
name|proxy
operator|.
name|removeRSGroup
argument_list|(
literal|null
argument_list|,
name|request
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ServiceException
name|e
parameter_list|)
block|{
throw|throw
name|ProtobufUtil
operator|.
name|getRemoteException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|balanceRSGroup
parameter_list|(
name|String
name|name
parameter_list|)
throws|throws
name|IOException
block|{
name|RSGroupAdminProtos
operator|.
name|BalanceRSGroupRequest
name|request
init|=
name|RSGroupAdminProtos
operator|.
name|BalanceRSGroupRequest
operator|.
name|newBuilder
argument_list|()
operator|.
name|setRSGroupName
argument_list|(
name|name
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
try|try
block|{
return|return
name|proxy
operator|.
name|balanceRSGroup
argument_list|(
literal|null
argument_list|,
name|request
argument_list|)
operator|.
name|getBalanceRan
argument_list|()
return|;
block|}
catch|catch
parameter_list|(
name|ServiceException
name|e
parameter_list|)
block|{
throw|throw
name|ProtobufUtil
operator|.
name|getRemoteException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|RSGroupInfo
argument_list|>
name|listRSGroups
parameter_list|()
throws|throws
name|IOException
block|{
try|try
block|{
name|List
argument_list|<
name|RSGroupProtos
operator|.
name|RSGroupInfo
argument_list|>
name|resp
init|=
name|proxy
operator|.
name|listRSGroupInfos
argument_list|(
literal|null
argument_list|,
name|RSGroupAdminProtos
operator|.
name|ListRSGroupInfosRequest
operator|.
name|newBuilder
argument_list|()
operator|.
name|build
argument_list|()
argument_list|)
operator|.
name|getRSGroupInfoList
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|RSGroupInfo
argument_list|>
name|result
init|=
operator|new
name|ArrayList
argument_list|<
name|RSGroupInfo
argument_list|>
argument_list|(
name|resp
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|RSGroupProtos
operator|.
name|RSGroupInfo
name|entry
range|:
name|resp
control|)
block|{
name|result
operator|.
name|add
argument_list|(
name|ProtobufUtil
operator|.
name|toGroupInfo
argument_list|(
name|entry
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|result
return|;
block|}
catch|catch
parameter_list|(
name|ServiceException
name|e
parameter_list|)
block|{
throw|throw
name|ProtobufUtil
operator|.
name|getRemoteException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|RSGroupInfo
name|getRSGroupOfServer
parameter_list|(
name|HostAndPort
name|hostPort
parameter_list|)
throws|throws
name|IOException
block|{
name|RSGroupAdminProtos
operator|.
name|GetRSGroupInfoOfServerRequest
name|request
init|=
name|RSGroupAdminProtos
operator|.
name|GetRSGroupInfoOfServerRequest
operator|.
name|newBuilder
argument_list|()
operator|.
name|setServer
argument_list|(
name|HBaseProtos
operator|.
name|ServerName
operator|.
name|newBuilder
argument_list|()
operator|.
name|setHostName
argument_list|(
name|hostPort
operator|.
name|getHostText
argument_list|()
argument_list|)
operator|.
name|setPort
argument_list|(
name|hostPort
operator|.
name|getPort
argument_list|()
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
try|try
block|{
name|GetRSGroupInfoOfServerResponse
name|resp
init|=
name|proxy
operator|.
name|getRSGroupInfoOfServer
argument_list|(
literal|null
argument_list|,
name|request
argument_list|)
decl_stmt|;
if|if
condition|(
name|resp
operator|.
name|hasRSGroupInfo
argument_list|()
condition|)
block|{
return|return
name|ProtobufUtil
operator|.
name|toGroupInfo
argument_list|(
name|resp
operator|.
name|getRSGroupInfo
argument_list|()
argument_list|)
return|;
block|}
return|return
literal|null
return|;
block|}
catch|catch
parameter_list|(
name|ServiceException
name|e
parameter_list|)
block|{
throw|throw
name|ProtobufUtil
operator|.
name|getRemoteException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|()
throws|throws
name|IOException
block|{   }
block|}
end_class

end_unit

