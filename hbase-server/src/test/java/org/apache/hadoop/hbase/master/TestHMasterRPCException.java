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
name|shaded
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
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|testclassification
operator|.
name|MasterTests
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
name|zookeeper
operator|.
name|ZKUtil
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
name|ZKWatcher
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|zookeeper
operator|.
name|KeeperException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|After
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Before
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
name|BlockingRpcChannel
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
name|ServiceException
import|;
end_import

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|MasterTests
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
name|TestHMasterRPCException
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
name|TestHMasterRPCException
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|HBaseTestingUtility
name|testUtil
init|=
name|HBaseTestingUtility
operator|.
name|createLocalHTU
argument_list|()
decl_stmt|;
specifier|private
name|HMaster
name|master
decl_stmt|;
specifier|private
name|RpcClient
name|rpcClient
decl_stmt|;
annotation|@
name|Before
specifier|public
name|void
name|setUp
parameter_list|()
throws|throws
name|Exception
block|{
name|Configuration
name|conf
init|=
name|testUtil
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
name|conf
operator|.
name|setInt
argument_list|(
name|HConstants
operator|.
name|ZK_SESSION_TIMEOUT
argument_list|,
literal|2000
argument_list|)
expr_stmt|;
name|testUtil
operator|.
name|startMiniZKCluster
argument_list|()
expr_stmt|;
name|ZKWatcher
name|watcher
init|=
name|testUtil
operator|.
name|getZooKeeperWatcher
argument_list|()
decl_stmt|;
name|ZKUtil
operator|.
name|createWithParents
argument_list|(
name|watcher
argument_list|,
name|watcher
operator|.
name|znodePaths
operator|.
name|masterAddressZNode
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"fake:123"
argument_list|)
argument_list|)
expr_stmt|;
name|master
operator|=
operator|new
name|HMaster
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|rpcClient
operator|=
name|RpcClientFactory
operator|.
name|createClient
argument_list|(
name|conf
argument_list|,
name|HConstants
operator|.
name|CLUSTER_ID_DEFAULT
argument_list|)
expr_stmt|;
block|}
annotation|@
name|After
specifier|public
name|void
name|tearDown
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|rpcClient
operator|!=
literal|null
condition|)
block|{
name|rpcClient
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|master
operator|!=
literal|null
condition|)
block|{
name|master
operator|.
name|stopMaster
argument_list|()
expr_stmt|;
block|}
name|testUtil
operator|.
name|shutdownMiniZKCluster
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testRPCException
parameter_list|()
throws|throws
name|IOException
throws|,
name|InterruptedException
throws|,
name|KeeperException
block|{
name|ServerName
name|sm
init|=
name|master
operator|.
name|getServerName
argument_list|()
decl_stmt|;
name|boolean
name|fakeZNodeDelete
init|=
literal|false
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
literal|20
condition|;
name|i
operator|++
control|)
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
name|assertTrue
argument_list|(
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
operator|.
name|getIsMasterRunning
argument_list|()
argument_list|)
expr_stmt|;
return|return;
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
name|handleRemoteException
argument_list|(
name|ex
argument_list|)
decl_stmt|;
comment|// No SocketTimeoutException here. RpcServer is already started after the construction of
comment|// HMaster.
name|assertTrue
argument_list|(
name|ie
operator|.
name|getMessage
argument_list|()
operator|.
name|startsWith
argument_list|(
literal|"org.apache.hadoop.hbase.ipc.ServerNotRunningYetException: Server is not running yet"
argument_list|)
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Expected exception: "
argument_list|,
name|ie
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|fakeZNodeDelete
condition|)
block|{
name|testUtil
operator|.
name|getZooKeeperWatcher
argument_list|()
operator|.
name|getRecoverableZooKeeper
argument_list|()
operator|.
name|delete
argument_list|(
name|testUtil
operator|.
name|getZooKeeperWatcher
argument_list|()
operator|.
name|znodePaths
operator|.
name|masterAddressZNode
argument_list|,
operator|-
literal|1
argument_list|)
expr_stmt|;
name|fakeZNodeDelete
operator|=
literal|true
expr_stmt|;
block|}
block|}
name|Thread
operator|.
name|sleep
argument_list|(
literal|1000
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

