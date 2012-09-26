begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|regionserver
package|;
end_package

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
name|io
operator|.
name|StringWriter
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
name|HTableDescriptor
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
name|SmallTests
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
name|ResponseConverter
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
name|AdminProtos
operator|.
name|GetOnlineRegionRequest
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
name|AdminProtos
operator|.
name|GetServerInfoRequest
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
name|AdminProtos
operator|.
name|GetServerInfoResponse
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
name|metrics
operator|.
name|RegionServerMetrics
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
name|tmpl
operator|.
name|regionserver
operator|.
name|RSStatusTmpl
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
name|MasterAddressTracker
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
name|mockito
operator|.
name|Mockito
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
name|collect
operator|.
name|Lists
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
comment|/**  * Tests for the region server status page and its template.  */
end_comment

begin_class
annotation|@
name|Category
argument_list|(
name|SmallTests
operator|.
name|class
argument_list|)
specifier|public
class|class
name|TestRSStatusServlet
block|{
specifier|private
name|HRegionServer
name|rs
decl_stmt|;
specifier|static
specifier|final
name|int
name|FAKE_IPC_PORT
init|=
literal|1585
decl_stmt|;
specifier|static
specifier|final
name|int
name|FAKE_WEB_PORT
init|=
literal|1586
decl_stmt|;
specifier|private
specifier|final
name|ServerName
name|fakeServerName
init|=
operator|new
name|ServerName
argument_list|(
literal|"localhost"
argument_list|,
name|FAKE_IPC_PORT
argument_list|,
literal|11111
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|GetServerInfoResponse
name|fakeResponse
init|=
name|ResponseConverter
operator|.
name|buildGetServerInfoResponse
argument_list|(
name|fakeServerName
argument_list|,
name|FAKE_WEB_PORT
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|RegionServerMetrics
name|metrics
init|=
operator|new
name|RegionServerMetrics
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|ServerName
name|fakeMasterAddress
init|=
operator|new
name|ServerName
argument_list|(
literal|"localhost"
argument_list|,
literal|60010
argument_list|,
literal|1212121212
argument_list|)
decl_stmt|;
annotation|@
name|Before
specifier|public
name|void
name|setupBasicMocks
parameter_list|()
throws|throws
name|IOException
throws|,
name|ServiceException
block|{
name|rs
operator|=
name|Mockito
operator|.
name|mock
argument_list|(
name|HRegionServer
operator|.
name|class
argument_list|)
expr_stmt|;
name|Mockito
operator|.
name|doReturn
argument_list|(
name|HBaseConfiguration
operator|.
name|create
argument_list|()
argument_list|)
operator|.
name|when
argument_list|(
name|rs
argument_list|)
operator|.
name|getConfiguration
argument_list|()
expr_stmt|;
name|Mockito
operator|.
name|doReturn
argument_list|(
name|fakeResponse
argument_list|)
operator|.
name|when
argument_list|(
name|rs
argument_list|)
operator|.
name|getServerInfo
argument_list|(
operator|(
name|RpcController
operator|)
name|Mockito
operator|.
name|any
argument_list|()
argument_list|,
operator|(
name|GetServerInfoRequest
operator|)
name|Mockito
operator|.
name|any
argument_list|()
argument_list|)
expr_stmt|;
name|Mockito
operator|.
name|doReturn
argument_list|(
name|metrics
argument_list|)
operator|.
name|when
argument_list|(
name|rs
argument_list|)
operator|.
name|getMetrics
argument_list|()
expr_stmt|;
comment|// Fake ZKW
name|ZooKeeperWatcher
name|zkw
init|=
name|Mockito
operator|.
name|mock
argument_list|(
name|ZooKeeperWatcher
operator|.
name|class
argument_list|)
decl_stmt|;
name|Mockito
operator|.
name|doReturn
argument_list|(
literal|"fakequorum"
argument_list|)
operator|.
name|when
argument_list|(
name|zkw
argument_list|)
operator|.
name|getQuorum
argument_list|()
expr_stmt|;
name|Mockito
operator|.
name|doReturn
argument_list|(
name|zkw
argument_list|)
operator|.
name|when
argument_list|(
name|rs
argument_list|)
operator|.
name|getZooKeeper
argument_list|()
expr_stmt|;
comment|// Fake MasterAddressTracker
name|MasterAddressTracker
name|mat
init|=
name|Mockito
operator|.
name|mock
argument_list|(
name|MasterAddressTracker
operator|.
name|class
argument_list|)
decl_stmt|;
name|Mockito
operator|.
name|doReturn
argument_list|(
name|fakeMasterAddress
argument_list|)
operator|.
name|when
argument_list|(
name|mat
argument_list|)
operator|.
name|getMasterAddress
argument_list|()
expr_stmt|;
name|Mockito
operator|.
name|doReturn
argument_list|(
name|mat
argument_list|)
operator|.
name|when
argument_list|(
name|rs
argument_list|)
operator|.
name|getMasterAddressManager
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testBasic
parameter_list|()
throws|throws
name|IOException
throws|,
name|ServiceException
block|{
operator|new
name|RSStatusTmpl
argument_list|()
operator|.
name|render
argument_list|(
operator|new
name|StringWriter
argument_list|()
argument_list|,
name|rs
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testWithRegions
parameter_list|()
throws|throws
name|IOException
throws|,
name|ServiceException
block|{
name|HTableDescriptor
name|htd
init|=
operator|new
name|HTableDescriptor
argument_list|(
literal|"mytable"
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|HRegionInfo
argument_list|>
name|regions
init|=
name|Lists
operator|.
name|newArrayList
argument_list|(
operator|new
name|HRegionInfo
argument_list|(
name|htd
operator|.
name|getName
argument_list|()
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"a"
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"d"
argument_list|)
argument_list|)
argument_list|,
operator|new
name|HRegionInfo
argument_list|(
name|htd
operator|.
name|getName
argument_list|()
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"d"
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"z"
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
name|Mockito
operator|.
name|doReturn
argument_list|(
name|ResponseConverter
operator|.
name|buildGetOnlineRegionResponse
argument_list|(
name|regions
argument_list|)
argument_list|)
operator|.
name|when
argument_list|(
name|rs
argument_list|)
operator|.
name|getOnlineRegion
argument_list|(
operator|(
name|RpcController
operator|)
name|Mockito
operator|.
name|any
argument_list|()
argument_list|,
operator|(
name|GetOnlineRegionRequest
operator|)
name|Mockito
operator|.
name|any
argument_list|()
argument_list|)
expr_stmt|;
operator|new
name|RSStatusTmpl
argument_list|()
operator|.
name|render
argument_list|(
operator|new
name|StringWriter
argument_list|()
argument_list|,
name|rs
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

