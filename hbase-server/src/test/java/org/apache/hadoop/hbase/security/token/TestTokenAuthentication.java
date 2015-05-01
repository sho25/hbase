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
name|apache
operator|.
name|hadoop
operator|.
name|fs
operator|.
name|CommonConfigurationKeysPublic
operator|.
name|HADOOP_SECURITY_AUTHORIZATION
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
name|assertFalse
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
name|assertNotNull
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
name|concurrent
operator|.
name|ConcurrentMap
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
name|ExecutorService
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
name|ChoreService
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
name|CoordinatedStateManager
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
name|ClusterConnection
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
name|client
operator|.
name|ConnectionFactory
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
name|FifoRpcScheduler
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
name|RpcClient
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
name|RpcClientFactory
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
name|RpcServer
operator|.
name|BlockingServiceAndInterface
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
name|RpcServerInterface
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
name|SecurityInfo
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
name|testclassification
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
name|testclassification
operator|.
name|SecurityTests
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
name|MetaTableLocator
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
name|authorize
operator|.
name|PolicyProvider
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
name|authorize
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

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|BlockingRpcChannel
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
name|BlockingService
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

begin_comment
comment|/**  * Tests for authentication token creation and usage  */
end_comment

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|SecurityTests
operator|.
name|class
block|,
name|MediumTests
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|TestTokenAuthentication
block|{
static|static
block|{
comment|// Setting whatever system properties after recommendation from
comment|// http://docs.oracle.com/javase/6/docs/technotes/guides/security/jgss/tutorials/KerberosReq.html
name|System
operator|.
name|setProperty
argument_list|(
literal|"java.security.krb5.realm"
argument_list|,
literal|"hbase"
argument_list|)
expr_stmt|;
name|System
operator|.
name|setProperty
argument_list|(
literal|"java.security.krb5.kdc"
argument_list|,
literal|"blah"
argument_list|)
expr_stmt|;
block|}
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
specifier|public
interface|interface
name|AuthenticationServiceSecurityInfo
block|{}
comment|/**    * Basic server process for RPC authentication testing    */
specifier|private
specifier|static
class|class
name|TokenServer
extends|extends
name|TokenProvider
implements|implements
name|AuthenticationProtos
operator|.
name|AuthenticationService
operator|.
name|BlockingInterface
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
name|RpcServerInterface
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
name|currentTime
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
specifier|final
name|List
argument_list|<
name|BlockingServiceAndInterface
argument_list|>
name|sai
init|=
operator|new
name|ArrayList
argument_list|<
name|BlockingServiceAndInterface
argument_list|>
argument_list|(
literal|1
argument_list|)
decl_stmt|;
name|BlockingService
name|service
init|=
name|AuthenticationProtos
operator|.
name|AuthenticationService
operator|.
name|newReflectiveBlockingService
argument_list|(
name|this
argument_list|)
decl_stmt|;
name|sai
operator|.
name|add
argument_list|(
operator|new
name|BlockingServiceAndInterface
argument_list|(
name|service
argument_list|,
name|AuthenticationProtos
operator|.
name|AuthenticationService
operator|.
name|BlockingInterface
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|this
operator|.
name|rpcServer
operator|=
operator|new
name|RpcServer
argument_list|(
name|this
argument_list|,
literal|"tokenServer"
argument_list|,
name|sai
argument_list|,
name|initialIsa
argument_list|,
name|conf
argument_list|,
operator|new
name|FifoRpcScheduler
argument_list|(
name|conf
argument_list|,
literal|1
argument_list|)
argument_list|)
expr_stmt|;
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
name|ClusterConnection
name|getConnection
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|MetaTableLocator
name|getMetaTableLocator
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
name|CoordinatedStateManager
name|getCoordinatedStateManager
parameter_list|()
block|{
return|return
literal|null
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
name|ServerName
operator|.
name|valueOf
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
name|TEST_UTIL
operator|.
name|createMockRegionServerService
argument_list|(
name|rpcServer
argument_list|)
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
name|TableName
name|tableName
parameter_list|)
throws|throws
name|IOException
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|HTableInterface
name|getTable
parameter_list|(
name|TableName
name|tableName
parameter_list|,
name|ExecutorService
name|service
parameter_list|)
throws|throws
name|IOException
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|ClassLoader
name|getClassLoader
parameter_list|()
block|{
return|return
name|Thread
operator|.
name|currentThread
argument_list|()
operator|.
name|getContextClassLoader
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|HRegionInfo
name|getRegionInfo
parameter_list|()
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
name|RpcServer
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
name|GetAuthenticationTokenResponse
name|getAuthenticationToken
parameter_list|(
name|RpcController
name|controller
parameter_list|,
name|AuthenticationProtos
operator|.
name|GetAuthenticationTokenRequest
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
name|RpcServer
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
name|GetAuthenticationTokenResponse
argument_list|>
name|callback
init|=
operator|new
name|BlockingRpcCallback
argument_list|<
name|AuthenticationProtos
operator|.
name|GetAuthenticationTokenResponse
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
name|whoAmI
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
literal|"whoAmI() request from "
operator|+
name|RpcServer
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
name|whoAmI
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
name|ChoreService
name|getChoreService
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
block|}
specifier|private
specifier|static
name|HBaseTestingUtility
name|TEST_UTIL
decl_stmt|;
specifier|private
specifier|static
name|TokenServer
name|server
decl_stmt|;
specifier|private
specifier|static
name|Thread
name|serverThread
decl_stmt|;
specifier|private
specifier|static
name|AuthenticationTokenSecretManager
name|secretManager
decl_stmt|;
specifier|private
specifier|static
name|ClusterId
name|clusterId
init|=
operator|new
name|ClusterId
argument_list|()
decl_stmt|;
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
comment|// register token type for protocol
name|SecurityInfo
operator|.
name|addInfo
argument_list|(
name|AuthenticationProtos
operator|.
name|AuthenticationService
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
literal|"hbase.test.kerberos.principal"
argument_list|,
name|AuthenticationProtos
operator|.
name|TokenIdentifier
operator|.
name|Kind
operator|.
name|HBASE_AUTH_TOKEN
argument_list|)
argument_list|)
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
name|conf
operator|.
name|setBoolean
argument_list|(
name|HADOOP_SECURITY_AUTHORIZATION
argument_list|,
literal|true
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
name|server
operator|.
name|rpcServer
operator|.
name|refreshAuthManager
argument_list|(
operator|new
name|PolicyProvider
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Service
index|[]
name|getServices
parameter_list|()
block|{
return|return
operator|new
name|Service
index|[]
block|{
operator|new
name|Service
argument_list|(
literal|"security.client.protocol.acl"
argument_list|,
name|AuthenticationProtos
operator|.
name|AuthenticationService
operator|.
name|BlockingInterface
operator|.
name|class
argument_list|)
block|}
return|;
block|}
block|}
argument_list|)
expr_stmt|;
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
name|RpcClient
name|rpcClient
init|=
name|RpcClientFactory
operator|.
name|createClient
argument_list|(
name|c
argument_list|,
name|clusterId
operator|.
name|toString
argument_list|()
argument_list|)
decl_stmt|;
name|ServerName
name|sn
init|=
name|ServerName
operator|.
name|valueOf
argument_list|(
name|server
operator|.
name|getAddress
argument_list|()
operator|.
name|getHostName
argument_list|()
argument_list|,
name|server
operator|.
name|getAddress
argument_list|()
operator|.
name|getPort
argument_list|()
argument_list|,
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|)
decl_stmt|;
try|try
block|{
name|BlockingRpcChannel
name|channel
init|=
name|rpcClient
operator|.
name|createBlockingRpcChannel
argument_list|(
name|sn
argument_list|,
name|User
operator|.
name|getCurrent
argument_list|()
argument_list|,
name|HConstants
operator|.
name|DEFAULT_HBASE_RPC_TIMEOUT
argument_list|)
decl_stmt|;
name|AuthenticationProtos
operator|.
name|AuthenticationService
operator|.
name|BlockingInterface
name|stub
init|=
name|AuthenticationProtos
operator|.
name|AuthenticationService
operator|.
name|newBlockingStub
argument_list|(
name|channel
argument_list|)
decl_stmt|;
name|AuthenticationProtos
operator|.
name|WhoAmIResponse
name|response
init|=
name|stub
operator|.
name|whoAmI
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
annotation|@
name|Test
specifier|public
name|void
name|testUseExistingToken
parameter_list|()
throws|throws
name|Exception
block|{
name|User
name|user
init|=
name|User
operator|.
name|createUserForTesting
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
literal|"testuser2"
argument_list|,
operator|new
name|String
index|[]
block|{
literal|"testgroup"
block|}
argument_list|)
decl_stmt|;
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
name|user
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
name|assertNotNull
argument_list|(
name|token
argument_list|)
expr_stmt|;
name|user
operator|.
name|addToken
argument_list|(
name|token
argument_list|)
expr_stmt|;
comment|// make sure we got a token
name|Token
argument_list|<
name|AuthenticationTokenIdentifier
argument_list|>
name|firstToken
init|=
operator|new
name|AuthenticationTokenSelector
argument_list|()
operator|.
name|selectToken
argument_list|(
name|token
operator|.
name|getService
argument_list|()
argument_list|,
name|user
operator|.
name|getTokens
argument_list|()
argument_list|)
decl_stmt|;
name|assertNotNull
argument_list|(
name|firstToken
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|token
argument_list|,
name|firstToken
argument_list|)
expr_stmt|;
name|Connection
name|conn
init|=
name|ConnectionFactory
operator|.
name|createConnection
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
try|try
block|{
name|assertFalse
argument_list|(
name|TokenUtil
operator|.
name|addTokenIfMissing
argument_list|(
name|conn
argument_list|,
name|user
argument_list|)
argument_list|)
expr_stmt|;
comment|// make sure we still have the same token
name|Token
argument_list|<
name|AuthenticationTokenIdentifier
argument_list|>
name|secondToken
init|=
operator|new
name|AuthenticationTokenSelector
argument_list|()
operator|.
name|selectToken
argument_list|(
name|token
operator|.
name|getService
argument_list|()
argument_list|,
name|user
operator|.
name|getTokens
argument_list|()
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|firstToken
argument_list|,
name|secondToken
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|conn
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

