begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|fail
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
name|SocketTimeoutException
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
name|*
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
name|protobuf
operator|.
name|generated
operator|.
name|MasterProtos
operator|.
name|IsMasterRunningRequest
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
name|ServiceException
import|;
end_import

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
name|TestHMasterRPCException
block|{
annotation|@
name|Test
specifier|public
name|void
name|testRPCException
parameter_list|()
throws|throws
name|Exception
block|{
name|HBaseTestingUtility
name|TEST_UTIL
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
name|TEST_UTIL
operator|.
name|startMiniZKCluster
argument_list|()
expr_stmt|;
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
name|HConstants
operator|.
name|MASTER_PORT
argument_list|,
literal|"0"
argument_list|)
expr_stmt|;
name|HMaster
name|hm
init|=
operator|new
name|HMaster
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|ServerName
name|sm
init|=
name|hm
operator|.
name|getServerName
argument_list|()
decl_stmt|;
name|RpcClient
name|rpcClient
init|=
operator|new
name|RpcClient
argument_list|(
name|conf
argument_list|,
name|HConstants
operator|.
name|CLUSTER_ID_DEFAULT
argument_list|)
decl_stmt|;
try|try
block|{
name|int
name|i
init|=
literal|0
decl_stmt|;
comment|//retry the RPC a few times; we have seen SocketTimeoutExceptions if we
comment|//try to connect too soon. Retry on SocketTimeoutException.
while|while
condition|(
name|i
operator|<
literal|20
condition|)
block|{
try|try
block|{
name|BlockingRpcChannel
name|channel
init|=
name|rpcClient
operator|.
name|createBlockingRpcChannel
argument_list|(
name|sm
argument_list|,
name|User
operator|.
name|getCurrent
argument_list|()
argument_list|,
literal|0
argument_list|)
decl_stmt|;
name|MasterProtos
operator|.
name|MasterService
operator|.
name|BlockingInterface
name|stub
init|=
name|MasterProtos
operator|.
name|MasterService
operator|.
name|newBlockingStub
argument_list|(
name|channel
argument_list|)
decl_stmt|;
name|stub
operator|.
name|isMasterRunning
argument_list|(
literal|null
argument_list|,
name|IsMasterRunningRequest
operator|.
name|getDefaultInstance
argument_list|()
argument_list|)
expr_stmt|;
name|fail
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ServiceException
name|ex
parameter_list|)
block|{
name|IOException
name|ie
init|=
name|ProtobufUtil
operator|.
name|getRemoteException
argument_list|(
name|ex
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
operator|(
name|ie
operator|instanceof
name|SocketTimeoutException
operator|)
condition|)
block|{
if|if
condition|(
name|ie
operator|.
name|getMessage
argument_list|()
operator|.
name|startsWith
argument_list|(
literal|"org.apache.hadoop.hbase.ipc."
operator|+
literal|"ServerNotRunningYetException: Server is not running yet"
argument_list|)
condition|)
block|{
comment|// Done.  Got the exception we wanted.
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Expected exception: "
operator|+
name|ie
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
return|return;
block|}
else|else
block|{
throw|throw
name|ex
throw|;
block|}
block|}
else|else
block|{
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"Got SocketTimeoutException. Will retry. "
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
name|fail
argument_list|(
literal|"Unexpected throwable: "
operator|+
name|t
argument_list|)
expr_stmt|;
block|}
name|Thread
operator|.
name|sleep
argument_list|(
literal|100
argument_list|)
expr_stmt|;
name|i
operator|++
expr_stmt|;
block|}
name|fail
argument_list|()
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
block|}
end_class

end_unit

