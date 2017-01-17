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
name|client
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
name|hbase
operator|.
name|HConstants
operator|.
name|CLUSTER_ID_DEFAULT
import|;
end_import

begin_import
import|import static
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
name|ConnectionUtils
operator|.
name|NO_NONCE_GENERATOR
import|;
end_import

begin_import
import|import static
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
name|ConnectionUtils
operator|.
name|getStubKey
import|;
end_import

begin_import
import|import static
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
name|NonceGenerator
operator|.
name|CLIENT_NONCES_ENABLED_KEY
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
name|annotations
operator|.
name|VisibleForTesting
import|;
end_import

begin_import
import|import
name|io
operator|.
name|netty
operator|.
name|util
operator|.
name|HashedWheelTimer
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
name|Optional
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
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|TimeUnit
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
name|io
operator|.
name|IOUtils
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
name|RpcControllerFactory
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
name|shaded
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|ClientProtos
operator|.
name|ClientService
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
name|CollectionUtils
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

begin_comment
comment|/**  * The implementation of AsyncConnection.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
class|class
name|AsyncConnectionImpl
implements|implements
name|AsyncConnection
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
name|AsyncConnectionImpl
operator|.
name|class
argument_list|)
decl_stmt|;
annotation|@
name|VisibleForTesting
specifier|static
specifier|final
name|HashedWheelTimer
name|RETRY_TIMER
init|=
operator|new
name|HashedWheelTimer
argument_list|(
name|Threads
operator|.
name|newDaemonThreadFactory
argument_list|(
literal|"Async-Client-Retry-Timer"
argument_list|)
argument_list|,
literal|10
argument_list|,
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|RESOLVE_HOSTNAME_ON_FAIL_KEY
init|=
literal|"hbase.resolve.hostnames.on.failure"
decl_stmt|;
specifier|private
specifier|final
name|Configuration
name|conf
decl_stmt|;
specifier|final
name|AsyncConnectionConfiguration
name|connConf
decl_stmt|;
specifier|private
specifier|final
name|User
name|user
decl_stmt|;
specifier|final
name|AsyncRegistry
name|registry
decl_stmt|;
specifier|private
specifier|final
name|String
name|clusterId
decl_stmt|;
specifier|private
specifier|final
name|int
name|rpcTimeout
decl_stmt|;
specifier|private
specifier|final
name|RpcClient
name|rpcClient
decl_stmt|;
specifier|final
name|RpcControllerFactory
name|rpcControllerFactory
decl_stmt|;
specifier|private
specifier|final
name|boolean
name|hostnameCanChange
decl_stmt|;
specifier|private
specifier|final
name|AsyncRegionLocator
name|locator
decl_stmt|;
specifier|final
name|AsyncRpcRetryingCallerFactory
name|callerFactory
decl_stmt|;
specifier|private
specifier|final
name|NonceGenerator
name|nonceGenerator
decl_stmt|;
specifier|private
specifier|final
name|ConcurrentMap
argument_list|<
name|String
argument_list|,
name|ClientService
operator|.
name|Interface
argument_list|>
name|rsStubs
init|=
operator|new
name|ConcurrentHashMap
argument_list|<>
argument_list|()
decl_stmt|;
specifier|public
name|AsyncConnectionImpl
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|User
name|user
parameter_list|)
block|{
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
name|this
operator|.
name|user
operator|=
name|user
expr_stmt|;
name|this
operator|.
name|connConf
operator|=
operator|new
name|AsyncConnectionConfiguration
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|this
operator|.
name|registry
operator|=
name|AsyncRegistryFactory
operator|.
name|getRegistry
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|this
operator|.
name|clusterId
operator|=
name|Optional
operator|.
name|ofNullable
argument_list|(
name|registry
operator|.
name|getClusterId
argument_list|()
argument_list|)
operator|.
name|orElseGet
argument_list|(
parameter_list|()
lambda|->
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
literal|"cluster id came back null, using default "
operator|+
name|CLUSTER_ID_DEFAULT
argument_list|)
expr_stmt|;
block|}
return|return
name|CLUSTER_ID_DEFAULT
return|;
block|}
argument_list|)
expr_stmt|;
name|this
operator|.
name|rpcClient
operator|=
name|RpcClientFactory
operator|.
name|createClient
argument_list|(
name|conf
argument_list|,
name|clusterId
argument_list|)
expr_stmt|;
name|this
operator|.
name|rpcControllerFactory
operator|=
name|RpcControllerFactory
operator|.
name|instantiate
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|this
operator|.
name|hostnameCanChange
operator|=
name|conf
operator|.
name|getBoolean
argument_list|(
name|RESOLVE_HOSTNAME_ON_FAIL_KEY
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|this
operator|.
name|rpcTimeout
operator|=
operator|(
name|int
operator|)
name|Math
operator|.
name|min
argument_list|(
name|Integer
operator|.
name|MAX_VALUE
argument_list|,
name|TimeUnit
operator|.
name|NANOSECONDS
operator|.
name|toMillis
argument_list|(
name|connConf
operator|.
name|getRpcTimeoutNs
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|this
operator|.
name|locator
operator|=
operator|new
name|AsyncRegionLocator
argument_list|(
name|this
argument_list|,
name|RETRY_TIMER
argument_list|)
expr_stmt|;
name|this
operator|.
name|callerFactory
operator|=
operator|new
name|AsyncRpcRetryingCallerFactory
argument_list|(
name|this
argument_list|,
name|RETRY_TIMER
argument_list|)
expr_stmt|;
if|if
condition|(
name|conf
operator|.
name|getBoolean
argument_list|(
name|CLIENT_NONCES_ENABLED_KEY
argument_list|,
literal|true
argument_list|)
condition|)
block|{
name|nonceGenerator
operator|=
name|PerClientRandomNonceGenerator
operator|.
name|get
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|nonceGenerator
operator|=
name|NO_NONCE_GENERATOR
expr_stmt|;
block|}
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
name|void
name|close
parameter_list|()
block|{
name|IOUtils
operator|.
name|closeQuietly
argument_list|(
name|rpcClient
argument_list|)
expr_stmt|;
name|IOUtils
operator|.
name|closeQuietly
argument_list|(
name|registry
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|AsyncTableRegionLocator
name|getRegionLocator
parameter_list|(
name|TableName
name|tableName
parameter_list|)
block|{
return|return
operator|new
name|AsyncTableRegionLocatorImpl
argument_list|(
name|tableName
argument_list|,
name|locator
argument_list|)
return|;
block|}
comment|// we will override this method for testing retry caller, so do not remove this method.
name|AsyncRegionLocator
name|getLocator
parameter_list|()
block|{
return|return
name|locator
return|;
block|}
comment|// ditto
specifier|public
name|NonceGenerator
name|getNonceGenerator
parameter_list|()
block|{
return|return
name|nonceGenerator
return|;
block|}
specifier|private
name|ClientService
operator|.
name|Interface
name|createRegionServerStub
parameter_list|(
name|ServerName
name|serverName
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|ClientService
operator|.
name|newStub
argument_list|(
name|rpcClient
operator|.
name|createRpcChannel
argument_list|(
name|serverName
argument_list|,
name|user
argument_list|,
name|rpcTimeout
argument_list|)
argument_list|)
return|;
block|}
name|ClientService
operator|.
name|Interface
name|getRegionServerStub
parameter_list|(
name|ServerName
name|serverName
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|CollectionUtils
operator|.
name|computeIfAbsentEx
argument_list|(
name|rsStubs
argument_list|,
name|getStubKey
argument_list|(
name|ClientService
operator|.
name|Interface
operator|.
name|class
operator|.
name|getSimpleName
argument_list|()
argument_list|,
name|serverName
argument_list|,
name|hostnameCanChange
argument_list|)
argument_list|,
parameter_list|()
lambda|->
name|createRegionServerStub
argument_list|(
name|serverName
argument_list|)
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|AsyncTableBuilder
argument_list|<
name|RawAsyncTable
argument_list|>
name|getRawTableBuilder
parameter_list|(
name|TableName
name|tableName
parameter_list|)
block|{
return|return
operator|new
name|AsyncTableBuilderBase
argument_list|<
name|RawAsyncTable
argument_list|>
argument_list|(
name|tableName
argument_list|,
name|connConf
argument_list|)
block|{
annotation|@
name|Override
specifier|public
name|RawAsyncTable
name|build
parameter_list|()
block|{
return|return
operator|new
name|RawAsyncTableImpl
argument_list|(
name|AsyncConnectionImpl
operator|.
name|this
argument_list|,
name|this
argument_list|)
return|;
block|}
block|}
return|;
block|}
annotation|@
name|Override
specifier|public
name|AsyncTableBuilder
argument_list|<
name|AsyncTable
argument_list|>
name|getTableBuilder
parameter_list|(
name|TableName
name|tableName
parameter_list|,
name|ExecutorService
name|pool
parameter_list|)
block|{
return|return
operator|new
name|AsyncTableBuilderBase
argument_list|<
name|AsyncTable
argument_list|>
argument_list|(
name|tableName
argument_list|,
name|connConf
argument_list|)
block|{
annotation|@
name|Override
specifier|public
name|AsyncTable
name|build
parameter_list|()
block|{
name|RawAsyncTableImpl
name|rawTable
init|=
operator|new
name|RawAsyncTableImpl
argument_list|(
name|AsyncConnectionImpl
operator|.
name|this
argument_list|,
name|this
argument_list|)
decl_stmt|;
return|return
operator|new
name|AsyncTableImpl
argument_list|(
name|AsyncConnectionImpl
operator|.
name|this
argument_list|,
name|rawTable
argument_list|,
name|pool
argument_list|)
return|;
block|}
block|}
return|;
block|}
block|}
end_class

end_unit

