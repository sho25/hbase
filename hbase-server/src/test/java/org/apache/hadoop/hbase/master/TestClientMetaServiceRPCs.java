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
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|HConstants
operator|.
name|DEFAULT_HBASE_RPC_TIMEOUT
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
name|HConstants
operator|.
name|HBASE_RPC_TIMEOUT_KEY
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
name|Collections
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
name|TimeUnit
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
name|HBaseClassTestRule
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
name|HRegionLocation
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
name|StartMiniClusterOption
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
name|JVMClusterUtil
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
name|ClassRule
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
operator|.
name|ClientMetaService
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
name|GetActiveMasterRequest
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
name|GetActiveMasterResponse
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
name|GetClusterIdRequest
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
name|GetClusterIdResponse
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
name|GetMetaRegionLocationsRequest
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
name|GetMetaRegionLocationsResponse
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
name|MasterTests
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|TestClientMetaServiceRPCs
block|{
annotation|@
name|ClassRule
specifier|public
specifier|static
specifier|final
name|HBaseClassTestRule
name|CLASS_RULE
init|=
name|HBaseClassTestRule
operator|.
name|forClass
argument_list|(
name|TestClientMetaServiceRPCs
operator|.
name|class
argument_list|)
decl_stmt|;
comment|// Total number of masters (active + stand by) for the purpose of this test.
specifier|private
specifier|static
specifier|final
name|int
name|MASTER_COUNT
init|=
literal|3
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|HBaseTestingUtility
name|TEST_UTIL
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
specifier|private
specifier|static
name|Configuration
name|conf
decl_stmt|;
specifier|private
specifier|static
name|int
name|rpcTimeout
decl_stmt|;
specifier|private
specifier|static
name|RpcClient
name|rpcClient
decl_stmt|;
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|setUp
parameter_list|()
throws|throws
name|Exception
block|{
comment|// Start the mini cluster with stand-by masters.
name|StartMiniClusterOption
operator|.
name|Builder
name|builder
init|=
name|StartMiniClusterOption
operator|.
name|builder
argument_list|()
decl_stmt|;
name|builder
operator|.
name|numMasters
argument_list|(
name|MASTER_COUNT
argument_list|)
operator|.
name|numRegionServers
argument_list|(
literal|3
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|startMiniCluster
argument_list|(
name|builder
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
name|conf
operator|=
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
expr_stmt|;
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
name|MILLISECONDS
operator|.
name|toNanos
argument_list|(
name|conf
operator|.
name|getLong
argument_list|(
name|HBASE_RPC_TIMEOUT_KEY
argument_list|,
name|DEFAULT_HBASE_RPC_TIMEOUT
argument_list|)
argument_list|)
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
name|TEST_UTIL
operator|.
name|getMiniHBaseCluster
argument_list|()
operator|.
name|getMaster
argument_list|()
operator|.
name|getClusterId
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|AfterClass
specifier|public
specifier|static
name|void
name|tearDown
parameter_list|()
throws|throws
name|Exception
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
name|TEST_UTIL
operator|.
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
block|}
specifier|private
specifier|static
name|ClientMetaService
operator|.
name|BlockingInterface
name|getMasterStub
parameter_list|(
name|ServerName
name|server
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|ClientMetaService
operator|.
name|newBlockingStub
argument_list|(
name|rpcClient
operator|.
name|createBlockingRpcChannel
argument_list|(
name|server
argument_list|,
name|User
operator|.
name|getCurrent
argument_list|()
argument_list|,
name|rpcTimeout
argument_list|)
argument_list|)
return|;
block|}
specifier|private
specifier|static
name|HBaseRpcController
name|getRpcController
parameter_list|()
block|{
return|return
name|RpcControllerFactory
operator|.
name|instantiate
argument_list|(
name|conf
argument_list|)
operator|.
name|newController
argument_list|()
return|;
block|}
comment|/**    * Verifies the cluster ID from all running masters.    */
annotation|@
name|Test
specifier|public
name|void
name|TestClusterID
parameter_list|()
throws|throws
name|Exception
block|{
name|HBaseRpcController
name|rpcController
init|=
name|getRpcController
argument_list|()
decl_stmt|;
name|String
name|clusterID
init|=
name|TEST_UTIL
operator|.
name|getMiniHBaseCluster
argument_list|()
operator|.
name|getMaster
argument_list|()
operator|.
name|getClusterId
argument_list|()
decl_stmt|;
name|int
name|rpcCount
init|=
literal|0
decl_stmt|;
for|for
control|(
name|JVMClusterUtil
operator|.
name|MasterThread
name|masterThread
range|:
name|TEST_UTIL
operator|.
name|getMiniHBaseCluster
argument_list|()
operator|.
name|getMasterThreads
argument_list|()
control|)
block|{
name|ClientMetaService
operator|.
name|BlockingInterface
name|stub
init|=
name|getMasterStub
argument_list|(
name|masterThread
operator|.
name|getMaster
argument_list|()
operator|.
name|getServerName
argument_list|()
argument_list|)
decl_stmt|;
name|GetClusterIdResponse
name|resp
init|=
name|stub
operator|.
name|getClusterId
argument_list|(
name|rpcController
argument_list|,
name|GetClusterIdRequest
operator|.
name|getDefaultInstance
argument_list|()
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|clusterID
argument_list|,
name|resp
operator|.
name|getClusterId
argument_list|()
argument_list|)
expr_stmt|;
name|rpcCount
operator|++
expr_stmt|;
block|}
name|assertEquals
argument_list|(
name|MASTER_COUNT
argument_list|,
name|rpcCount
argument_list|)
expr_stmt|;
block|}
comment|/**    * Verifies the active master ServerName as seen by all masters.    */
annotation|@
name|Test
specifier|public
name|void
name|TestActiveMaster
parameter_list|()
throws|throws
name|Exception
block|{
name|HBaseRpcController
name|rpcController
init|=
name|getRpcController
argument_list|()
decl_stmt|;
name|ServerName
name|activeMaster
init|=
name|TEST_UTIL
operator|.
name|getMiniHBaseCluster
argument_list|()
operator|.
name|getMaster
argument_list|()
operator|.
name|getServerName
argument_list|()
decl_stmt|;
name|int
name|rpcCount
init|=
literal|0
decl_stmt|;
for|for
control|(
name|JVMClusterUtil
operator|.
name|MasterThread
name|masterThread
range|:
name|TEST_UTIL
operator|.
name|getMiniHBaseCluster
argument_list|()
operator|.
name|getMasterThreads
argument_list|()
control|)
block|{
name|ClientMetaService
operator|.
name|BlockingInterface
name|stub
init|=
name|getMasterStub
argument_list|(
name|masterThread
operator|.
name|getMaster
argument_list|()
operator|.
name|getServerName
argument_list|()
argument_list|)
decl_stmt|;
name|GetActiveMasterResponse
name|resp
init|=
name|stub
operator|.
name|getActiveMaster
argument_list|(
name|rpcController
argument_list|,
name|GetActiveMasterRequest
operator|.
name|getDefaultInstance
argument_list|()
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|activeMaster
argument_list|,
name|ProtobufUtil
operator|.
name|toServerName
argument_list|(
name|resp
operator|.
name|getServerName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|rpcCount
operator|++
expr_stmt|;
block|}
name|assertEquals
argument_list|(
name|MASTER_COUNT
argument_list|,
name|rpcCount
argument_list|)
expr_stmt|;
block|}
comment|/**    * Verifies that the meta region locations RPC returns consistent results across all masters.    */
annotation|@
name|Test
specifier|public
name|void
name|TestMetaLocations
parameter_list|()
throws|throws
name|Exception
block|{
name|HBaseRpcController
name|rpcController
init|=
name|getRpcController
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|HRegionLocation
argument_list|>
name|metaLocations
init|=
name|TEST_UTIL
operator|.
name|getMiniHBaseCluster
argument_list|()
operator|.
name|getMaster
argument_list|()
operator|.
name|getMetaRegionLocationCache
argument_list|()
operator|.
name|getMetaRegionLocations
argument_list|()
operator|.
name|get
argument_list|()
decl_stmt|;
name|Collections
operator|.
name|sort
argument_list|(
name|metaLocations
argument_list|)
expr_stmt|;
name|int
name|rpcCount
init|=
literal|0
decl_stmt|;
for|for
control|(
name|JVMClusterUtil
operator|.
name|MasterThread
name|masterThread
range|:
name|TEST_UTIL
operator|.
name|getMiniHBaseCluster
argument_list|()
operator|.
name|getMasterThreads
argument_list|()
control|)
block|{
name|ClientMetaService
operator|.
name|BlockingInterface
name|stub
init|=
name|getMasterStub
argument_list|(
name|masterThread
operator|.
name|getMaster
argument_list|()
operator|.
name|getServerName
argument_list|()
argument_list|)
decl_stmt|;
name|GetMetaRegionLocationsResponse
name|resp
init|=
name|stub
operator|.
name|getMetaRegionLocations
argument_list|(
name|rpcController
argument_list|,
name|GetMetaRegionLocationsRequest
operator|.
name|getDefaultInstance
argument_list|()
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|HRegionLocation
argument_list|>
name|result
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|resp
operator|.
name|getMetaLocationsList
argument_list|()
operator|.
name|forEach
argument_list|(
name|location
lambda|->
name|result
operator|.
name|add
argument_list|(
name|ProtobufUtil
operator|.
name|toRegionLocation
argument_list|(
name|location
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|Collections
operator|.
name|sort
argument_list|(
name|result
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|metaLocations
argument_list|,
name|result
argument_list|)
expr_stmt|;
name|rpcCount
operator|++
expr_stmt|;
block|}
name|assertEquals
argument_list|(
name|MASTER_COUNT
argument_list|,
name|rpcCount
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

