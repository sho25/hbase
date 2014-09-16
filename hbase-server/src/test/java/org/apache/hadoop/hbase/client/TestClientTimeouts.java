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
name|client
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
name|assertTrue
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|SocketTimeoutException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Random
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
name|AtomicInteger
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
name|HBaseConfiguration
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
name|ClientTests
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
name|Descriptors
operator|.
name|MethodDescriptor
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
name|Message
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

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|MediumTests
operator|.
name|class
block|,
name|ClientTests
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|TestClientTimeouts
block|{
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|getClass
argument_list|()
argument_list|)
decl_stmt|;
specifier|private
specifier|final
specifier|static
name|HBaseTestingUtility
name|TEST_UTIL
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
specifier|protected
specifier|static
name|int
name|SLAVES
init|=
literal|1
decl_stmt|;
comment|/**    * @throws java.lang.Exception    */
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|setUpBeforeClass
parameter_list|()
throws|throws
name|Exception
block|{
name|TEST_UTIL
operator|.
name|startMiniCluster
argument_list|(
name|SLAVES
argument_list|)
expr_stmt|;
block|}
comment|/**    * @throws java.lang.Exception    */
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
name|TEST_UTIL
operator|.
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
block|}
comment|/**    * Test that a client that fails an RPC to the master retries properly and    * doesn't throw any unexpected exceptions.    * @throws Exception    */
annotation|@
name|Test
specifier|public
name|void
name|testAdminTimeout
parameter_list|()
throws|throws
name|Exception
block|{
name|HConnection
name|lastConnection
init|=
literal|null
decl_stmt|;
name|boolean
name|lastFailed
init|=
literal|false
decl_stmt|;
name|int
name|initialInvocations
init|=
name|RandomTimeoutBlockingRpcChannel
operator|.
name|invokations
operator|.
name|get
argument_list|()
decl_stmt|;
name|RpcClient
name|rpcClient
init|=
name|newRandomTimeoutRpcClient
argument_list|()
decl_stmt|;
try|try
block|{
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
literal|5
operator|||
operator|(
name|lastFailed
operator|&&
name|i
operator|<
literal|100
operator|)
condition|;
operator|++
name|i
control|)
block|{
name|lastFailed
operator|=
literal|false
expr_stmt|;
comment|// Ensure the HBaseAdmin uses a new connection by changing Configuration.
name|Configuration
name|conf
init|=
name|HBaseConfiguration
operator|.
name|create
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
name|conf
operator|.
name|set
argument_list|(
name|HConstants
operator|.
name|HBASE_CLIENT_INSTANCE_ID
argument_list|,
name|String
operator|.
name|valueOf
argument_list|(
operator|-
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|HBaseAdmin
name|admin
init|=
literal|null
decl_stmt|;
try|try
block|{
name|admin
operator|=
operator|new
name|HBaseAdmin
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|HConnection
name|connection
init|=
name|admin
operator|.
name|getConnection
argument_list|()
decl_stmt|;
name|assertFalse
argument_list|(
name|connection
operator|==
name|lastConnection
argument_list|)
expr_stmt|;
name|lastConnection
operator|=
name|connection
expr_stmt|;
comment|// Override the connection's rpc client for timeout testing
name|RpcClient
name|oldRpcClient
init|=
operator|(
operator|(
name|ConnectionManager
operator|.
name|HConnectionImplementation
operator|)
name|connection
operator|)
operator|.
name|setRpcClient
argument_list|(
name|rpcClient
argument_list|)
decl_stmt|;
if|if
condition|(
name|oldRpcClient
operator|!=
literal|null
condition|)
block|{
name|oldRpcClient
operator|.
name|stop
argument_list|()
expr_stmt|;
block|}
comment|// run some admin commands
name|HBaseAdmin
operator|.
name|checkHBaseAvailable
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|admin
operator|.
name|setBalancerRunning
argument_list|(
literal|false
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|MasterNotRunningException
name|ex
parameter_list|)
block|{
comment|// Since we are randomly throwing SocketTimeoutExceptions, it is possible to get
comment|// a MasterNotRunningException.  It's a bug if we get other exceptions.
name|lastFailed
operator|=
literal|true
expr_stmt|;
block|}
finally|finally
block|{
name|admin
operator|.
name|close
argument_list|()
expr_stmt|;
if|if
condition|(
name|admin
operator|.
name|getConnection
argument_list|()
operator|.
name|isClosed
argument_list|()
condition|)
block|{
name|rpcClient
operator|=
name|newRandomTimeoutRpcClient
argument_list|()
expr_stmt|;
block|}
block|}
block|}
comment|// Ensure the RandomTimeoutRpcEngine is actually being used.
name|assertFalse
argument_list|(
name|lastFailed
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|RandomTimeoutBlockingRpcChannel
operator|.
name|invokations
operator|.
name|get
argument_list|()
operator|>
name|initialInvocations
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|rpcClient
operator|.
name|stop
argument_list|()
expr_stmt|;
block|}
block|}
specifier|private
specifier|static
name|RpcClient
name|newRandomTimeoutRpcClient
parameter_list|()
block|{
return|return
operator|new
name|RpcClient
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|TEST_UTIL
operator|.
name|getClusterKey
argument_list|()
argument_list|)
block|{
comment|// Return my own instance, one that does random timeouts
annotation|@
name|Override
specifier|public
name|BlockingRpcChannel
name|createBlockingRpcChannel
parameter_list|(
name|ServerName
name|sn
parameter_list|,
name|User
name|ticket
parameter_list|,
name|int
name|rpcTimeout
parameter_list|)
block|{
return|return
operator|new
name|RandomTimeoutBlockingRpcChannel
argument_list|(
name|this
argument_list|,
name|sn
argument_list|,
name|ticket
argument_list|,
name|rpcTimeout
argument_list|)
return|;
block|}
block|}
return|;
block|}
comment|/**    * Blocking rpc channel that goes via hbase rpc.    */
specifier|static
class|class
name|RandomTimeoutBlockingRpcChannel
extends|extends
name|RpcClient
operator|.
name|BlockingRpcChannelImplementation
block|{
specifier|private
specifier|static
specifier|final
name|Random
name|RANDOM
init|=
operator|new
name|Random
argument_list|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|double
name|CHANCE_OF_TIMEOUT
init|=
literal|0.3
decl_stmt|;
specifier|private
specifier|static
name|AtomicInteger
name|invokations
init|=
operator|new
name|AtomicInteger
argument_list|()
decl_stmt|;
name|RandomTimeoutBlockingRpcChannel
parameter_list|(
specifier|final
name|RpcClient
name|rpcClient
parameter_list|,
specifier|final
name|ServerName
name|sn
parameter_list|,
specifier|final
name|User
name|ticket
parameter_list|,
specifier|final
name|int
name|rpcTimeout
parameter_list|)
block|{
name|super
argument_list|(
name|rpcClient
argument_list|,
name|sn
argument_list|,
name|ticket
argument_list|,
name|rpcTimeout
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Message
name|callBlockingMethod
parameter_list|(
name|MethodDescriptor
name|md
parameter_list|,
name|RpcController
name|controller
parameter_list|,
name|Message
name|param
parameter_list|,
name|Message
name|returnType
parameter_list|)
throws|throws
name|ServiceException
block|{
name|invokations
operator|.
name|getAndIncrement
argument_list|()
expr_stmt|;
if|if
condition|(
name|RANDOM
operator|.
name|nextFloat
argument_list|()
operator|<
name|CHANCE_OF_TIMEOUT
condition|)
block|{
comment|// throw a ServiceException, becuase that is the only exception type that
comment|// {@link ProtobufRpcEngine} throws.  If this RpcEngine is used with a different
comment|// "actual" type, this may not properly mimic the underlying RpcEngine.
throw|throw
operator|new
name|ServiceException
argument_list|(
operator|new
name|SocketTimeoutException
argument_list|(
literal|"fake timeout"
argument_list|)
argument_list|)
throw|;
block|}
return|return
name|super
operator|.
name|callBlockingMethod
argument_list|(
name|md
argument_list|,
name|controller
argument_list|,
name|param
argument_list|,
name|returnType
argument_list|)
return|;
block|}
block|}
block|}
end_class

end_unit

