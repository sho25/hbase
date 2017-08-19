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
name|concurrent
operator|.
name|CompletableFuture
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
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|atomic
operator|.
name|AtomicReference
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
name|MasterNotRunningException
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
name|ipc
operator|.
name|HBaseRpcController
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
name|RequestConverter
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
operator|.
name|AdminService
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
name|shaded
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|MasterProtos
operator|.
name|IsMasterRunningResponse
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
specifier|private
specifier|final
name|ConcurrentMap
argument_list|<
name|String
argument_list|,
name|AdminService
operator|.
name|Interface
argument_list|>
name|adminSubs
init|=
operator|new
name|ConcurrentHashMap
argument_list|<>
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|AtomicReference
argument_list|<
name|MasterService
operator|.
name|Interface
argument_list|>
name|masterStub
init|=
operator|new
name|AtomicReference
argument_list|<>
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|AtomicReference
argument_list|<
name|CompletableFuture
argument_list|<
name|MasterService
operator|.
name|Interface
argument_list|>
argument_list|>
name|masterStubMakeFuture
init|=
operator|new
name|AtomicReference
argument_list|<>
argument_list|()
decl_stmt|;
specifier|public
name|AsyncConnectionImpl
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|AsyncRegistry
name|registry
parameter_list|,
name|String
name|clusterId
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
name|registry
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
annotation|@
name|VisibleForTesting
name|AsyncRegionLocator
name|getLocator
parameter_list|()
block|{
return|return
name|locator
return|;
block|}
comment|// ditto
annotation|@
name|VisibleForTesting
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
specifier|private
name|MasterService
operator|.
name|Interface
name|createMasterStub
parameter_list|(
name|ServerName
name|serverName
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|MasterService
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
specifier|private
name|AdminService
operator|.
name|Interface
name|createAdminServerStub
parameter_list|(
name|ServerName
name|serverName
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|AdminService
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
name|AdminService
operator|.
name|Interface
name|getAdminStub
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
name|adminSubs
argument_list|,
name|getStubKey
argument_list|(
name|AdminService
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
name|createAdminServerStub
argument_list|(
name|serverName
argument_list|)
argument_list|)
return|;
block|}
specifier|private
name|void
name|makeMasterStub
parameter_list|(
name|CompletableFuture
argument_list|<
name|MasterService
operator|.
name|Interface
argument_list|>
name|future
parameter_list|)
block|{
name|registry
operator|.
name|getMasterAddress
argument_list|()
operator|.
name|whenComplete
argument_list|(
parameter_list|(
name|sn
parameter_list|,
name|error
parameter_list|)
lambda|->
block|{
if|if
condition|(
name|sn
operator|==
literal|null
condition|)
block|{
name|String
name|msg
init|=
literal|"ZooKeeper available but no active master location found"
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
name|msg
argument_list|)
expr_stmt|;
name|this
operator|.
name|masterStubMakeFuture
operator|.
name|getAndSet
argument_list|(
literal|null
argument_list|)
operator|.
name|completeExceptionally
argument_list|(
operator|new
name|MasterNotRunningException
argument_list|(
name|msg
argument_list|)
argument_list|)
expr_stmt|;
return|return;
block|}
try|try
block|{
name|MasterService
operator|.
name|Interface
name|stub
init|=
name|createMasterStub
argument_list|(
name|sn
argument_list|)
decl_stmt|;
name|HBaseRpcController
name|controller
init|=
name|getRpcController
argument_list|()
decl_stmt|;
name|stub
operator|.
name|isMasterRunning
argument_list|(
name|controller
argument_list|,
name|RequestConverter
operator|.
name|buildIsMasterRunningRequest
argument_list|()
argument_list|,
operator|new
name|RpcCallback
argument_list|<
name|IsMasterRunningResponse
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|(
name|IsMasterRunningResponse
name|resp
parameter_list|)
block|{
if|if
condition|(
name|controller
operator|.
name|failed
argument_list|()
operator|||
name|resp
operator|==
literal|null
operator|||
operator|(
name|resp
operator|!=
literal|null
operator|&&
operator|!
name|resp
operator|.
name|getIsMasterRunning
argument_list|()
operator|)
condition|)
block|{
name|masterStubMakeFuture
operator|.
name|getAndSet
argument_list|(
literal|null
argument_list|)
operator|.
name|completeExceptionally
argument_list|(
operator|new
name|MasterNotRunningException
argument_list|(
literal|"Master connection is not running anymore"
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|masterStub
operator|.
name|set
argument_list|(
name|stub
argument_list|)
expr_stmt|;
name|masterStubMakeFuture
operator|.
name|set
argument_list|(
literal|null
argument_list|)
expr_stmt|;
name|future
operator|.
name|complete
argument_list|(
name|stub
argument_list|)
expr_stmt|;
block|}
block|}
block|}
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|this
operator|.
name|masterStubMakeFuture
operator|.
name|getAndSet
argument_list|(
literal|null
argument_list|)
operator|.
name|completeExceptionally
argument_list|(
operator|new
name|IOException
argument_list|(
literal|"Failed to create async master stub"
argument_list|,
name|e
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
name|CompletableFuture
argument_list|<
name|MasterService
operator|.
name|Interface
argument_list|>
name|getMasterStub
parameter_list|()
block|{
name|MasterService
operator|.
name|Interface
name|masterStub
init|=
name|this
operator|.
name|masterStub
operator|.
name|get
argument_list|()
decl_stmt|;
if|if
condition|(
name|masterStub
operator|==
literal|null
condition|)
block|{
for|for
control|(
init|;
condition|;
control|)
block|{
if|if
condition|(
name|this
operator|.
name|masterStubMakeFuture
operator|.
name|compareAndSet
argument_list|(
literal|null
argument_list|,
operator|new
name|CompletableFuture
argument_list|<>
argument_list|()
argument_list|)
condition|)
block|{
name|CompletableFuture
argument_list|<
name|MasterService
operator|.
name|Interface
argument_list|>
name|future
init|=
name|this
operator|.
name|masterStubMakeFuture
operator|.
name|get
argument_list|()
decl_stmt|;
name|makeMasterStub
argument_list|(
name|future
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|CompletableFuture
argument_list|<
name|MasterService
operator|.
name|Interface
argument_list|>
name|future
init|=
name|this
operator|.
name|masterStubMakeFuture
operator|.
name|get
argument_list|()
decl_stmt|;
if|if
condition|(
name|future
operator|!=
literal|null
condition|)
block|{
return|return
name|future
return|;
block|}
block|}
block|}
block|}
for|for
control|(
init|;
condition|;
control|)
block|{
if|if
condition|(
name|masterStubMakeFuture
operator|.
name|compareAndSet
argument_list|(
literal|null
argument_list|,
operator|new
name|CompletableFuture
argument_list|<>
argument_list|()
argument_list|)
condition|)
block|{
name|CompletableFuture
argument_list|<
name|MasterService
operator|.
name|Interface
argument_list|>
name|future
init|=
name|masterStubMakeFuture
operator|.
name|get
argument_list|()
decl_stmt|;
name|HBaseRpcController
name|controller
init|=
name|getRpcController
argument_list|()
decl_stmt|;
name|masterStub
operator|.
name|isMasterRunning
argument_list|(
name|controller
argument_list|,
name|RequestConverter
operator|.
name|buildIsMasterRunningRequest
argument_list|()
argument_list|,
operator|new
name|RpcCallback
argument_list|<
name|IsMasterRunningResponse
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|(
name|IsMasterRunningResponse
name|resp
parameter_list|)
block|{
if|if
condition|(
name|controller
operator|.
name|failed
argument_list|()
operator|||
name|resp
operator|==
literal|null
operator|||
operator|(
name|resp
operator|!=
literal|null
operator|&&
operator|!
name|resp
operator|.
name|getIsMasterRunning
argument_list|()
operator|)
condition|)
block|{
name|makeMasterStub
argument_list|(
name|future
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|future
operator|.
name|complete
argument_list|(
name|masterStub
argument_list|)
expr_stmt|;
block|}
block|}
block|}
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|CompletableFuture
argument_list|<
name|MasterService
operator|.
name|Interface
argument_list|>
name|future
init|=
name|masterStubMakeFuture
operator|.
name|get
argument_list|()
decl_stmt|;
if|if
condition|(
name|future
operator|!=
literal|null
condition|)
block|{
return|return
name|future
return|;
block|}
block|}
block|}
block|}
specifier|private
name|HBaseRpcController
name|getRpcController
parameter_list|()
block|{
name|HBaseRpcController
name|controller
init|=
name|this
operator|.
name|rpcControllerFactory
operator|.
name|newController
argument_list|()
decl_stmt|;
name|controller
operator|.
name|setCallTimeout
argument_list|(
operator|(
name|int
operator|)
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
return|return
name|controller
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
annotation|@
name|Override
specifier|public
name|AsyncAdminBuilder
name|getAdminBuilder
parameter_list|()
block|{
return|return
operator|new
name|AsyncAdminBuilderBase
argument_list|(
name|connConf
argument_list|)
block|{
annotation|@
name|Override
specifier|public
name|AsyncAdmin
name|build
parameter_list|()
block|{
return|return
operator|new
name|RawAsyncHBaseAdmin
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
name|AsyncAdminBuilder
name|getAdminBuilder
parameter_list|(
name|ExecutorService
name|pool
parameter_list|)
block|{
return|return
operator|new
name|AsyncAdminBuilderBase
argument_list|(
name|connConf
argument_list|)
block|{
annotation|@
name|Override
specifier|public
name|AsyncAdmin
name|build
parameter_list|()
block|{
name|RawAsyncHBaseAdmin
name|rawAdmin
init|=
operator|new
name|RawAsyncHBaseAdmin
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
name|AsyncHBaseAdmin
argument_list|(
name|rawAdmin
argument_list|,
name|pool
argument_list|)
return|;
block|}
block|}
return|;
block|}
annotation|@
name|Override
specifier|public
name|AsyncBufferedMutatorBuilder
name|getBufferedMutatorBuilder
parameter_list|(
name|TableName
name|tableName
parameter_list|)
block|{
return|return
operator|new
name|AsyncBufferedMutatorBuilderImpl
argument_list|(
name|connConf
argument_list|,
name|getRawTableBuilder
argument_list|(
name|tableName
argument_list|)
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|AsyncBufferedMutatorBuilder
name|getBufferedMutatorBuilder
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
name|AsyncBufferedMutatorBuilderImpl
argument_list|(
name|connConf
argument_list|,
name|getTableBuilder
argument_list|(
name|tableName
argument_list|,
name|pool
argument_list|)
argument_list|)
return|;
block|}
block|}
end_class

end_unit

