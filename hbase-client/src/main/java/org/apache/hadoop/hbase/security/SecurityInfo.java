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
name|security
package|;
end_package

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
name|AuthenticationProtos
operator|.
name|TokenIdentifier
operator|.
name|Kind
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
name|MasterProtos
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
name|AdminProtos
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
name|ClientProtos
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
name|MasterProtos
operator|.
name|MasterService
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
name|RegionServerStatusProtos
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

begin_comment
comment|/**  * Maps RPC protocol interfaces to required configuration  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|SecurityInfo
block|{
comment|/** Maps RPC service names to authentication information */
specifier|private
specifier|static
name|ConcurrentMap
argument_list|<
name|String
argument_list|,
name|SecurityInfo
argument_list|>
name|infos
init|=
operator|new
name|ConcurrentHashMap
argument_list|<>
argument_list|()
decl_stmt|;
comment|// populate info for known services
static|static
block|{
name|infos
operator|.
name|put
argument_list|(
name|AdminProtos
operator|.
name|AdminService
operator|.
name|getDescriptor
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|,
operator|new
name|SecurityInfo
argument_list|(
name|SecurityConstants
operator|.
name|REGIONSERVER_KRB_PRINCIPAL
argument_list|,
name|Kind
operator|.
name|HBASE_AUTH_TOKEN
argument_list|)
argument_list|)
expr_stmt|;
name|infos
operator|.
name|put
argument_list|(
name|ClientProtos
operator|.
name|ClientService
operator|.
name|getDescriptor
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|,
operator|new
name|SecurityInfo
argument_list|(
name|SecurityConstants
operator|.
name|REGIONSERVER_KRB_PRINCIPAL
argument_list|,
name|Kind
operator|.
name|HBASE_AUTH_TOKEN
argument_list|)
argument_list|)
expr_stmt|;
name|infos
operator|.
name|put
argument_list|(
name|MasterService
operator|.
name|getDescriptor
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|,
operator|new
name|SecurityInfo
argument_list|(
name|SecurityConstants
operator|.
name|MASTER_KRB_PRINCIPAL
argument_list|,
name|Kind
operator|.
name|HBASE_AUTH_TOKEN
argument_list|)
argument_list|)
expr_stmt|;
name|infos
operator|.
name|put
argument_list|(
name|RegionServerStatusProtos
operator|.
name|RegionServerStatusService
operator|.
name|getDescriptor
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|,
operator|new
name|SecurityInfo
argument_list|(
name|SecurityConstants
operator|.
name|MASTER_KRB_PRINCIPAL
argument_list|,
name|Kind
operator|.
name|HBASE_AUTH_TOKEN
argument_list|)
argument_list|)
expr_stmt|;
name|infos
operator|.
name|put
argument_list|(
name|MasterProtos
operator|.
name|HbckService
operator|.
name|getDescriptor
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|,
operator|new
name|SecurityInfo
argument_list|(
name|SecurityConstants
operator|.
name|MASTER_KRB_PRINCIPAL
argument_list|,
name|Kind
operator|.
name|HBASE_AUTH_TOKEN
argument_list|)
argument_list|)
expr_stmt|;
comment|// NOTE: IF ADDING A NEW SERVICE, BE SURE TO UPDATE HBasePolicyProvider ALSO ELSE
comment|// new Service will not be found when all is Kerberized!!!!
block|}
comment|/**    * Adds a security configuration for a new service name.  Note that this will have no effect if    * the service name was already registered.    */
specifier|public
specifier|static
name|void
name|addInfo
parameter_list|(
name|String
name|serviceName
parameter_list|,
name|SecurityInfo
name|securityInfo
parameter_list|)
block|{
name|infos
operator|.
name|putIfAbsent
argument_list|(
name|serviceName
argument_list|,
name|securityInfo
argument_list|)
expr_stmt|;
block|}
comment|/**    * Returns the security configuration associated with the given service name.    */
specifier|public
specifier|static
name|SecurityInfo
name|getInfo
parameter_list|(
name|String
name|serviceName
parameter_list|)
block|{
return|return
name|infos
operator|.
name|get
argument_list|(
name|serviceName
argument_list|)
return|;
block|}
specifier|private
specifier|final
name|String
name|serverPrincipal
decl_stmt|;
specifier|private
specifier|final
name|Kind
name|tokenKind
decl_stmt|;
specifier|public
name|SecurityInfo
parameter_list|(
name|String
name|serverPrincipal
parameter_list|,
name|Kind
name|tokenKind
parameter_list|)
block|{
name|this
operator|.
name|serverPrincipal
operator|=
name|serverPrincipal
expr_stmt|;
name|this
operator|.
name|tokenKind
operator|=
name|tokenKind
expr_stmt|;
block|}
specifier|public
name|String
name|getServerPrincipal
parameter_list|()
block|{
return|return
name|serverPrincipal
return|;
block|}
specifier|public
name|Kind
name|getTokenKind
parameter_list|()
block|{
return|return
name|tokenKind
return|;
block|}
block|}
end_class

end_unit

