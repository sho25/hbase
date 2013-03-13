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
operator|.
name|token
package|;
end_package

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertEquals
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertTrue
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
name|net
operator|.
name|InetSocketAddress
import|;
end_import

begin_import
import|import
name|java
operator|.
name|security
operator|.
name|PrivilegedExceptionAction
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
name|ServiceException
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
name|ClusterId
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
name|IpcProtocol
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
name|Coprocessor
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
name|HBaseTestingUtility
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
name|MediumTests
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
name|Server
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
name|ServerName
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
name|catalog
operator|.
name|CatalogTracker
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
name|HTableInterface
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
name|RegionCoprocessorEnvironment
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
name|BlockingRpcCallback
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
name|HBaseClientRPC
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
name|HBaseServer
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
name|HBaseServerRPC
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
name|ProtobufRpcClientEngine
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
name|RequestContext
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
name|ipc
operator|.
name|ServerRpcController
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
name|regionserver
operator|.
name|HRegion
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
name|regionserver
operator|.
name|RegionServerServices
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
name|KerberosInfo
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
name|TokenInfo
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
name|util
operator|.
name|Bytes
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
name|EnvironmentEdgeManager
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
name|MockRegionServerServices
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
name|Sleeper
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
name|Strings
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
name|hadoop
operator|.
name|hbase
operator|.
name|util
operator|.
name|Writables
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
name|zookeeper
operator|.
name|ZKClusterId
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
name|zookeeper
operator|.
name|ZooKeeperWatcher
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
name|net
operator|.
name|DNS
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
name|security
operator|.
name|UserGroupInformation
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
name|security
operator|.
name|token
operator|.
name|SecretManager
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
name|security
operator|.
name|token
operator|.
name|Token
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
name|security
operator|.
name|token
operator|.
name|TokenIdentifier
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|AfterClass
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|BeforeClass
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Test
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|experimental
operator|.
name|categories
operator|.
name|Category
import|;
end_import

begin_comment
comment|/**  * Tests for authentication token creation and usage  */
end_comment

begin_class
annotation|@
name|Category
argument_list|(
name|MediumTests
operator|.
name|class
argument_list|)
specifier|public
class|class
name|TestTokenAuthentication
block|{
specifier|private
specifier|static
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|TestTokenAuthentication
operator|.
name|class
argument_list|)
decl_stmt|;
annotation|@
name|KerberosInfo
argument_list|(
name|serverPrincipal
operator|=
literal|"hbase.test.kerberos.principal"
argument_list|)
annotation|@
name|TokenInfo
argument_list|(
literal|"HBASE_AUTH_TOKEN"
argument_list|)
specifier|private
specifier|static
interface|interface
name|BlockingAuthenticationService
extends|extends
name|AuthenticationProtos
operator|.
name|AuthenticationService
operator|.
name|BlockingInterface
extends|,
name|IpcProtocol
block|{   }
comment|/**    * Basic server process for RPC authentication testing    */
specifier|private
specifier|static
class|class
name|TokenServer
extends|extends
name|TokenProvider
implements|implements
name|BlockingAuthenticationService
implements|,
name|Runnable
implements|,
name|Server
block|{
specifier|private
specifier|static
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|TokenServer
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|Configuration
name|conf
decl_stmt|;
specifier|private
name|RpcServer
name|rpcServer
decl_stmt|;
specifier|private
name|InetSocketAddress
name|isa
decl_stmt|;
specifier|private
name|ZooKeeperWatcher
name|zookeeper
decl_stmt|;
specifier|private
name|Sleeper
name|sleeper
decl_stmt|;
specifier|private
name|boolean
name|started
init|=
literal|false
decl_stmt|;
specifier|private
name|boolean
name|aborted
init|=
literal|false
decl_stmt|;
specifier|private
name|boolean
name|stopped
init|=
literal|false
decl_stmt|;
specifier|private
name|long
name|startcode
decl_stmt|;
specifier|private
name|AuthenticationProtos
operator|.
name|AuthenticationService
operator|.
name|BlockingInterface
name|blockingService
decl_stmt|;
specifier|public
name|TokenServer
parameter_list|(
name|Configuration
name|conf
parameter_list|)
throws|throws
name|IOException
block|{
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
name|this
operator|.
name|startcode
operator|=
name|EnvironmentEdgeManager
operator|.
name|currentTimeMillis
argument_list|()
expr_stmt|;
comment|// Server to handle client requests.
name|String
name|hostname
init|=
name|Strings
operator|.
name|domainNamePointerToHostName
argument_list|(
name|DNS
operator|.
name|getDefaultHost
argument_list|(
literal|"default"
argument_list|,
literal|"default"
argument_list|)
argument_list|)
decl_stmt|;
name|int
name|port
init|=
literal|0
decl_stmt|;
comment|// Creation of an ISA will force a resolve.
name|InetSocketAddress
name|initialIsa
init|=
operator|new
name|InetSocketAddress
argument_list|(
name|hostname
argument_list|,
name|port
argument_list|)
decl_stmt|;
if|if
condition|(
name|initialIsa
operator|.
name|getAddress
argument_list|()
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Failed resolve of "
operator|+
name|initialIsa
argument_list|)
throw|;
block|}
name|this
operator|.
name|rpcServer
operator|=
name|HBaseServerRPC
operator|.
name|getServer
argument_list|(
name|TokenServer
operator|.
name|class
argument_list|,
name|this
argument_list|,
operator|new
name|Class
argument_list|<
name|?
argument_list|>
index|[]
block|{
name|AuthenticationProtos
operator|.
name|AuthenticationService
operator|.
name|Interface
operator|.
name|class
block|}
operator|,
name|initialIsa
operator|.
name|getHostName
argument_list|()
operator|,
comment|// BindAddress is IP we got for this server.
name|initialIsa
operator|.
name|getPort
argument_list|()
operator|,
literal|3
operator|,
comment|// handlers
literal|1
operator|,
comment|// meta handlers (not used)
literal|true
operator|,
name|this
operator|.
name|conf
operator|,
name|HConstants
operator|.
name|QOS_THRESHOLD
block|)
empty_stmt|;
comment|// Set our address.
name|this
operator|.
name|isa
operator|=
name|this
operator|.
name|rpcServer
operator|.
name|getListenerAddress
argument_list|()
expr_stmt|;
name|this
operator|.
name|sleeper
operator|=
operator|new
name|Sleeper
argument_list|(
literal|1000
argument_list|,
name|this
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Configuration
name|getConfiguration
parameter_list|()
block|{
return|return
name|conf
return|;
block|}
annotation|@
name|Override
specifier|public
name|CatalogTracker
name|getCatalogTracker
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|ZooKeeperWatcher
name|getZooKeeper
parameter_list|()
block|{
return|return
name|zookeeper
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isAborted
parameter_list|()
block|{
return|return
name|aborted
return|;
block|}
annotation|@
name|Override
specifier|public
name|ServerName
name|getServerName
parameter_list|()
block|{
return|return
operator|new
name|ServerName
argument_list|(
name|isa
operator|.
name|getHostName
argument_list|()
argument_list|,
name|isa
operator|.
name|getPort
argument_list|()
argument_list|,
name|startcode
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|abort
parameter_list|(
name|String
name|reason
parameter_list|,
name|Throwable
name|error
parameter_list|)
block|{
name|LOG
operator|.
name|fatal
argument_list|(
literal|"Aborting on: "
operator|+
name|reason
argument_list|,
name|error
argument_list|)
expr_stmt|;
name|this
operator|.
name|aborted
operator|=
literal|true
expr_stmt|;
name|this
operator|.
name|stopped
operator|=
literal|true
expr_stmt|;
name|sleeper
operator|.
name|skipSleepCycle
argument_list|()
expr_stmt|;
block|}
specifier|private
name|void
name|initialize
parameter_list|()
throws|throws
name|IOException
block|{
comment|// ZK configuration must _not_ have hbase.security.authentication or it will require SASL auth
name|Configuration
name|zkConf
init|=
operator|new
name|Configuration
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|zkConf
operator|.
name|set
argument_list|(
name|User
operator|.
name|HBASE_SECURITY_CONF_KEY
argument_list|,
literal|"simple"
argument_list|)
expr_stmt|;
name|this
operator|.
name|zookeeper
operator|=
operator|new
name|ZooKeeperWatcher
argument_list|(
name|zkConf
argument_list|,
name|TokenServer
operator|.
name|class
operator|.
name|getSimpleName
argument_list|()
argument_list|,
name|this
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|this
operator|.
name|rpcServer
operator|.
name|start
argument_list|()
expr_stmt|;
comment|// mock RegionServerServices to provide to coprocessor environment
specifier|final
name|RegionServerServices
name|mockServices
init|=
operator|new
name|MockRegionServerServices
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|RpcServer
name|getRpcServer
parameter_list|()
block|{
return|return
name|rpcServer
return|;
block|}
block|}
decl_stmt|;
comment|// mock up coprocessor environment
name|super
operator|.
name|start
argument_list|(
operator|new
name|RegionCoprocessorEnvironment
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|HRegion
name|getRegion
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|RegionServerServices
name|getRegionServerServices
parameter_list|()
block|{
return|return
name|mockServices
return|;
block|}
annotation|@
name|Override
specifier|public
name|ConcurrentMap
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|getSharedData
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getVersion
parameter_list|()
block|{
return|return
literal|0
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|getHBaseVersion
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|Coprocessor
name|getInstance
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getPriority
parameter_list|()
block|{
return|return
literal|0
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getLoadSequence
parameter_list|()
block|{
return|return
literal|0
return|;
block|}
annotation|@
name|Override
specifier|public
name|Configuration
name|getConfiguration
parameter_list|()
block|{
return|return
name|conf
return|;
block|}
annotation|@
name|Override
specifier|public
name|HTableInterface
name|getTable
parameter_list|(
name|byte
index|[]
name|tableName
parameter_list|)
throws|throws
name|IOException
block|{
return|return
literal|null
return|;
block|}
block|}
argument_list|)
expr_stmt|;
name|started
operator|=
literal|true
expr_stmt|;
block|}
specifier|public
name|void
name|run
parameter_list|()
block|{
try|try
block|{
name|initialize
argument_list|()
expr_stmt|;
while|while
condition|(
operator|!
name|stopped
condition|)
block|{
name|this
operator|.
name|sleeper
operator|.
name|sleep
argument_list|()
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|abort
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
name|this
operator|.
name|rpcServer
operator|.
name|stop
argument_list|()
expr_stmt|;
block|}
specifier|public
name|boolean
name|isStarted
parameter_list|()
block|{
return|return
name|started
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|stop
parameter_list|(
name|String
name|reason
parameter_list|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Stopping due to: "
operator|+
name|reason
argument_list|)
expr_stmt|;
name|this
operator|.
name|stopped
operator|=
literal|true
expr_stmt|;
name|sleeper
operator|.
name|skipSleepCycle
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isStopped
parameter_list|()
block|{
return|return
name|stopped
return|;
block|}
specifier|public
name|InetSocketAddress
name|getAddress
parameter_list|()
block|{
return|return
name|isa
return|;
block|}
specifier|public
name|SecretManager
argument_list|<
name|?
extends|extends
name|TokenIdentifier
argument_list|>
name|getSecretManager
parameter_list|()
block|{
return|return
operator|(
operator|(
name|HBaseServer
operator|)
name|rpcServer
operator|)
operator|.
name|getSecretManager
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|AuthenticationProtos
operator|.
name|TokenResponse
name|getAuthenticationToken
parameter_list|(
name|RpcController
name|controller
parameter_list|,
name|AuthenticationProtos
operator|.
name|TokenRequest
name|request
parameter_list|)
throws|throws
name|ServiceException
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Authentication token request from "
operator|+
name|RequestContext
operator|.
name|getRequestUserName
argument_list|()
argument_list|)
expr_stmt|;
comment|// ignore passed in controller -- it's always null
name|ServerRpcController
name|serverController
init|=
operator|new
name|ServerRpcController
argument_list|()
decl_stmt|;
name|BlockingRpcCallback
argument_list|<
name|AuthenticationProtos
operator|.
name|TokenResponse
argument_list|>
name|callback
init|=
operator|new
name|BlockingRpcCallback
argument_list|<
name|AuthenticationProtos
operator|.
name|TokenResponse
argument_list|>
argument_list|()
decl_stmt|;
name|getAuthenticationToken
argument_list|(
name|serverController
argument_list|,
name|request
argument_list|,
name|callback
argument_list|)
expr_stmt|;
try|try
block|{
name|serverController
operator|.
name|checkFailed
argument_list|()
expr_stmt|;
return|return
name|callback
operator|.
name|get
argument_list|()
return|;
block|}
catch|catch
parameter_list|(
name|IOException
name|ioe
parameter_list|)
block|{
throw|throw
operator|new
name|ServiceException
argument_list|(
name|ioe
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|AuthenticationProtos
operator|.
name|WhoAmIResponse
name|whoami
parameter_list|(
name|RpcController
name|controller
parameter_list|,
name|AuthenticationProtos
operator|.
name|WhoAmIRequest
name|request
parameter_list|)
throws|throws
name|ServiceException
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"whoami() request from "
operator|+
name|RequestContext
operator|.
name|getRequestUserName
argument_list|()
argument_list|)
expr_stmt|;
comment|// ignore passed in controller -- it's always null
name|ServerRpcController
name|serverController
init|=
operator|new
name|ServerRpcController
argument_list|()
decl_stmt|;
name|BlockingRpcCallback
argument_list|<
name|AuthenticationProtos
operator|.
name|WhoAmIResponse
argument_list|>
name|callback
init|=
operator|new
name|BlockingRpcCallback
argument_list|<
name|AuthenticationProtos
operator|.
name|WhoAmIResponse
argument_list|>
argument_list|()
decl_stmt|;
name|whoami
argument_list|(
name|serverController
argument_list|,
name|request
argument_list|,
name|callback
argument_list|)
expr_stmt|;
try|try
block|{
name|serverController
operator|.
name|checkFailed
argument_list|()
expr_stmt|;
return|return
name|callback
operator|.
name|get
argument_list|()
return|;
block|}
catch|catch
parameter_list|(
name|IOException
name|ioe
parameter_list|)
block|{
throw|throw
operator|new
name|ServiceException
argument_list|(
name|ioe
argument_list|)
throw|;
block|}
block|}
block|}
end_class

begin_decl_stmt
specifier|private
specifier|static
name|HBaseTestingUtility
name|TEST_UTIL
decl_stmt|;
end_decl_stmt

begin_decl_stmt
specifier|private
specifier|static
name|TokenServer
name|server
decl_stmt|;
end_decl_stmt

begin_decl_stmt
specifier|private
specifier|static
name|Thread
name|serverThread
decl_stmt|;
end_decl_stmt

begin_decl_stmt
specifier|private
specifier|static
name|AuthenticationTokenSecretManager
name|secretManager
decl_stmt|;
end_decl_stmt

begin_decl_stmt
specifier|private
specifier|static
name|ClusterId
name|clusterId
init|=
operator|new
name|ClusterId
argument_list|()
decl_stmt|;
end_decl_stmt

begin_function
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|setupBeforeClass
parameter_list|()
throws|throws
name|Exception
block|{
name|TEST_UTIL
operator|=
operator|new
name|HBaseTestingUtility
argument_list|()
expr_stmt|;
name|TEST_UTIL
operator|.
name|startMiniZKCluster
argument_list|()
expr_stmt|;
comment|// security settings only added after startup so that ZK does not require SASL
name|Configuration
name|conf
init|=
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
name|conf
operator|.
name|set
argument_list|(
literal|"hadoop.security.authentication"
argument_list|,
literal|"kerberos"
argument_list|)
expr_stmt|;
name|conf
operator|.
name|set
argument_list|(
literal|"hbase.security.authentication"
argument_list|,
literal|"kerberos"
argument_list|)
expr_stmt|;
name|server
operator|=
operator|new
name|TokenServer
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|serverThread
operator|=
operator|new
name|Thread
argument_list|(
name|server
argument_list|)
expr_stmt|;
name|Threads
operator|.
name|setDaemonThreadRunning
argument_list|(
name|serverThread
argument_list|,
literal|"TokenServer:"
operator|+
name|server
operator|.
name|getServerName
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
comment|// wait for startup
while|while
condition|(
operator|!
name|server
operator|.
name|isStarted
argument_list|()
operator|&&
operator|!
name|server
operator|.
name|isStopped
argument_list|()
condition|)
block|{
name|Thread
operator|.
name|sleep
argument_list|(
literal|10
argument_list|)
expr_stmt|;
block|}
name|ZKClusterId
operator|.
name|setClusterId
argument_list|(
name|server
operator|.
name|getZooKeeper
argument_list|()
argument_list|,
name|clusterId
argument_list|)
expr_stmt|;
name|secretManager
operator|=
operator|(
name|AuthenticationTokenSecretManager
operator|)
name|server
operator|.
name|getSecretManager
argument_list|()
expr_stmt|;
while|while
condition|(
name|secretManager
operator|.
name|getCurrentKey
argument_list|()
operator|==
literal|null
condition|)
block|{
name|Thread
operator|.
name|sleep
argument_list|(
literal|1
argument_list|)
expr_stmt|;
block|}
block|}
end_function

begin_function
annotation|@
name|AfterClass
specifier|public
specifier|static
name|void
name|tearDownAfterClass
parameter_list|()
throws|throws
name|Exception
block|{
name|server
operator|.
name|stop
argument_list|(
literal|"Test complete"
argument_list|)
expr_stmt|;
name|Threads
operator|.
name|shutdown
argument_list|(
name|serverThread
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|shutdownMiniZKCluster
argument_list|()
expr_stmt|;
block|}
end_function

begin_function
annotation|@
name|Test
specifier|public
name|void
name|testTokenCreation
parameter_list|()
throws|throws
name|Exception
block|{
name|Token
argument_list|<
name|AuthenticationTokenIdentifier
argument_list|>
name|token
init|=
name|secretManager
operator|.
name|generateToken
argument_list|(
literal|"testuser"
argument_list|)
decl_stmt|;
name|AuthenticationTokenIdentifier
name|ident
init|=
operator|new
name|AuthenticationTokenIdentifier
argument_list|()
decl_stmt|;
name|Writables
operator|.
name|getWritable
argument_list|(
name|token
operator|.
name|getIdentifier
argument_list|()
argument_list|,
name|ident
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"Token username should match"
argument_list|,
literal|"testuser"
argument_list|,
name|ident
operator|.
name|getUsername
argument_list|()
argument_list|)
expr_stmt|;
name|byte
index|[]
name|passwd
init|=
name|secretManager
operator|.
name|retrievePassword
argument_list|(
name|ident
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
literal|"Token password and password from secret manager should match"
argument_list|,
name|Bytes
operator|.
name|equals
argument_list|(
name|token
operator|.
name|getPassword
argument_list|()
argument_list|,
name|passwd
argument_list|)
argument_list|)
expr_stmt|;
block|}
end_function

begin_function
annotation|@
name|Test
specifier|public
name|void
name|testTokenAuthentication
parameter_list|()
throws|throws
name|Exception
block|{
name|UserGroupInformation
name|testuser
init|=
name|UserGroupInformation
operator|.
name|createUserForTesting
argument_list|(
literal|"testuser"
argument_list|,
operator|new
name|String
index|[]
block|{
literal|"testgroup"
block|}
argument_list|)
decl_stmt|;
name|testuser
operator|.
name|setAuthenticationMethod
argument_list|(
name|UserGroupInformation
operator|.
name|AuthenticationMethod
operator|.
name|TOKEN
argument_list|)
expr_stmt|;
specifier|final
name|Configuration
name|conf
init|=
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
name|UserGroupInformation
operator|.
name|setConfiguration
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|Token
argument_list|<
name|AuthenticationTokenIdentifier
argument_list|>
name|token
init|=
name|secretManager
operator|.
name|generateToken
argument_list|(
literal|"testuser"
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Got token: "
operator|+
name|token
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|testuser
operator|.
name|addToken
argument_list|(
name|token
argument_list|)
expr_stmt|;
comment|// verify the server authenticates us as this token user
name|testuser
operator|.
name|doAs
argument_list|(
operator|new
name|PrivilegedExceptionAction
argument_list|<
name|Object
argument_list|>
argument_list|()
block|{
specifier|public
name|Object
name|run
parameter_list|()
throws|throws
name|Exception
block|{
name|Configuration
name|c
init|=
name|server
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
name|ProtobufRpcClientEngine
name|rpcClient
init|=
operator|new
name|ProtobufRpcClientEngine
argument_list|(
name|c
argument_list|,
name|clusterId
operator|.
name|toString
argument_list|()
argument_list|)
decl_stmt|;
try|try
block|{
name|AuthenticationProtos
operator|.
name|AuthenticationService
operator|.
name|BlockingInterface
name|proxy
init|=
name|HBaseClientRPC
operator|.
name|waitForProxy
argument_list|(
name|rpcClient
argument_list|,
name|BlockingAuthenticationService
operator|.
name|class
argument_list|,
name|server
operator|.
name|getAddress
argument_list|()
argument_list|,
name|c
argument_list|,
name|HConstants
operator|.
name|DEFAULT_HBASE_CLIENT_RPC_MAXATTEMPTS
argument_list|,
name|HConstants
operator|.
name|DEFAULT_HBASE_RPC_TIMEOUT
argument_list|,
name|HConstants
operator|.
name|DEFAULT_HBASE_RPC_TIMEOUT
argument_list|)
decl_stmt|;
name|AuthenticationProtos
operator|.
name|WhoAmIResponse
name|response
init|=
name|proxy
operator|.
name|whoami
argument_list|(
literal|null
argument_list|,
name|AuthenticationProtos
operator|.
name|WhoAmIRequest
operator|.
name|getDefaultInstance
argument_list|()
argument_list|)
decl_stmt|;
name|String
name|myname
init|=
name|response
operator|.
name|getUsername
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|"testuser"
argument_list|,
name|myname
argument_list|)
expr_stmt|;
name|String
name|authMethod
init|=
name|response
operator|.
name|getAuthMethod
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|"TOKEN"
argument_list|,
name|authMethod
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|rpcClient
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
return|return
literal|null
return|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
end_function

unit|}
end_unit

