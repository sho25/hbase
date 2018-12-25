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
name|rsgroup
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
name|ConnectException
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
name|Abortable
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
name|HConnectionTestingUtility
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
name|RegionInfoBuilder
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
name|ipc
operator|.
name|ServerNotRunningYetException
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
name|master
operator|.
name|RegionState
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
name|MiscTests
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
name|AfterClass
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
name|mockito
operator|.
name|Mockito
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|LoggerFactory
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hbase
operator|.
name|thirdparty
operator|.
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
name|org
operator|.
name|apache
operator|.
name|hbase
operator|.
name|thirdparty
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
name|AdminProtos
operator|.
name|GetRegionInfoRequest
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
name|ClientProtos
operator|.
name|GetRequest
import|;
end_import

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|MiscTests
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
name|TestUtility
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
name|TestUtility
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|TestUtility
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|HBaseTestingUtility
name|UTIL
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|ServerName
name|SN
init|=
name|ServerName
operator|.
name|valueOf
argument_list|(
literal|"example.org"
argument_list|,
literal|1234
argument_list|,
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|)
decl_stmt|;
specifier|private
name|ZKWatcher
name|watcher
decl_stmt|;
specifier|private
name|Abortable
name|abortable
decl_stmt|;
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|beforeClass
parameter_list|()
throws|throws
name|Exception
block|{
comment|// Set this down so tests run quicker
name|UTIL
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setInt
argument_list|(
name|HConstants
operator|.
name|HBASE_CLIENT_RETRIES_NUMBER
argument_list|,
literal|3
argument_list|)
expr_stmt|;
name|UTIL
operator|.
name|startMiniZKCluster
argument_list|()
expr_stmt|;
block|}
annotation|@
name|AfterClass
specifier|public
specifier|static
name|void
name|afterClass
parameter_list|()
throws|throws
name|IOException
block|{
name|UTIL
operator|.
name|getZkCluster
argument_list|()
operator|.
name|shutdown
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Before
specifier|public
name|void
name|before
parameter_list|()
throws|throws
name|IOException
block|{
name|this
operator|.
name|abortable
operator|=
operator|new
name|Abortable
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|abort
parameter_list|(
name|String
name|why
parameter_list|,
name|Throwable
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|info
argument_list|(
name|why
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isAborted
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
block|}
expr_stmt|;
name|this
operator|.
name|watcher
operator|=
operator|new
name|ZKWatcher
argument_list|(
name|UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|this
operator|.
name|getClass
argument_list|()
operator|.
name|getSimpleName
argument_list|()
argument_list|,
name|this
operator|.
name|abortable
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
annotation|@
name|After
specifier|public
name|void
name|after
parameter_list|()
block|{
try|try
block|{
comment|// Clean out meta location or later tests will be confused... they presume
comment|// start fresh in zk.
name|MetaTableLocator
operator|.
name|deleteMetaLocation
argument_list|(
name|this
operator|.
name|watcher
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|KeeperException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Unable to delete hbase:meta location"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
name|this
operator|.
name|watcher
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
comment|/**    * @param admin An {@link AdminProtos.AdminService.BlockingInterface} instance; you'll likely want    *          to pass a mocked HRS; can be null.    * @param client A mocked ClientProtocol instance, can be null    * @return Mock up a connection that returns a {@link Configuration} when    *         {@link org.apache.hadoop.hbase.client.ClusterConnection#getConfiguration()} is called,    *         a 'location' when    *         {@link org.apache.hadoop.hbase.client.RegionLocator#getRegionLocation(byte[], boolean)}    *         is called, and that returns the passed    *         {@link AdminProtos.AdminService.BlockingInterface} instance when    *         {@link org.apache.hadoop.hbase.client.ClusterConnection#getAdmin(ServerName)} is    *         called, returns the passed {@link ClientProtos.ClientService.BlockingInterface}    *         instance when    *         {@link org.apache.hadoop.hbase.client.ClusterConnection#getClient(ServerName)} is    *         called.    */
specifier|private
name|ClusterConnection
name|mockConnection
parameter_list|(
specifier|final
name|AdminProtos
operator|.
name|AdminService
operator|.
name|BlockingInterface
name|admin
parameter_list|,
specifier|final
name|ClientProtos
operator|.
name|ClientService
operator|.
name|BlockingInterface
name|client
parameter_list|)
throws|throws
name|IOException
block|{
name|ClusterConnection
name|connection
init|=
name|HConnectionTestingUtility
operator|.
name|getMockedConnection
argument_list|(
name|UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
name|Mockito
operator|.
name|doNothing
argument_list|()
operator|.
name|when
argument_list|(
name|connection
argument_list|)
operator|.
name|close
argument_list|()
expr_stmt|;
comment|// Make it so we return any old location when asked.
specifier|final
name|HRegionLocation
name|anyLocation
init|=
operator|new
name|HRegionLocation
argument_list|(
name|RegionInfoBuilder
operator|.
name|FIRST_META_REGIONINFO
argument_list|,
name|SN
argument_list|)
decl_stmt|;
name|Mockito
operator|.
name|when
argument_list|(
name|connection
operator|.
name|getRegionLocation
argument_list|(
operator|(
name|TableName
operator|)
name|Mockito
operator|.
name|any
argument_list|()
argument_list|,
operator|(
name|byte
index|[]
operator|)
name|Mockito
operator|.
name|any
argument_list|()
argument_list|,
name|Mockito
operator|.
name|anyBoolean
argument_list|()
argument_list|)
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|anyLocation
argument_list|)
expr_stmt|;
name|Mockito
operator|.
name|when
argument_list|(
name|connection
operator|.
name|locateRegion
argument_list|(
operator|(
name|TableName
operator|)
name|Mockito
operator|.
name|any
argument_list|()
argument_list|,
operator|(
name|byte
index|[]
operator|)
name|Mockito
operator|.
name|any
argument_list|()
argument_list|)
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|anyLocation
argument_list|)
expr_stmt|;
if|if
condition|(
name|admin
operator|!=
literal|null
condition|)
block|{
comment|// If a call to getHRegionConnection, return this implementation.
name|Mockito
operator|.
name|when
argument_list|(
name|connection
operator|.
name|getAdmin
argument_list|(
name|Mockito
operator|.
name|any
argument_list|()
argument_list|)
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|admin
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|client
operator|!=
literal|null
condition|)
block|{
comment|// If a call to getClient, return this implementation.
name|Mockito
operator|.
name|when
argument_list|(
name|connection
operator|.
name|getClient
argument_list|(
name|Mockito
operator|.
name|any
argument_list|()
argument_list|)
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|client
argument_list|)
expr_stmt|;
block|}
return|return
name|connection
return|;
block|}
specifier|private
name|void
name|testVerifyMetaRegionLocationWithException
parameter_list|(
name|Exception
name|ex
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
throws|,
name|KeeperException
throws|,
name|ServiceException
block|{
comment|// Mock an ClientProtocol.
specifier|final
name|ClientProtos
operator|.
name|ClientService
operator|.
name|BlockingInterface
name|implementation
init|=
name|Mockito
operator|.
name|mock
argument_list|(
name|ClientProtos
operator|.
name|ClientService
operator|.
name|BlockingInterface
operator|.
name|class
argument_list|)
decl_stmt|;
name|ClusterConnection
name|connection
init|=
name|mockConnection
argument_list|(
literal|null
argument_list|,
name|implementation
argument_list|)
decl_stmt|;
comment|// If a 'get' is called on mocked interface, throw connection refused.
name|Mockito
operator|.
name|when
argument_list|(
name|implementation
operator|.
name|get
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
name|GetRequest
operator|)
name|Mockito
operator|.
name|any
argument_list|()
argument_list|)
argument_list|)
operator|.
name|thenThrow
argument_list|(
operator|new
name|ServiceException
argument_list|(
name|ex
argument_list|)
argument_list|)
expr_stmt|;
name|long
name|timeout
init|=
name|UTIL
operator|.
name|getConfiguration
argument_list|()
operator|.
name|getLong
argument_list|(
literal|"hbase.catalog.verification.timeout"
argument_list|,
literal|1000
argument_list|)
decl_stmt|;
name|MetaTableLocator
operator|.
name|setMetaLocation
argument_list|(
name|this
operator|.
name|watcher
argument_list|,
name|SN
argument_list|,
name|RegionState
operator|.
name|State
operator|.
name|OPENING
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|Utility
operator|.
name|verifyMetaRegionLocation
argument_list|(
name|connection
argument_list|,
name|watcher
argument_list|,
name|timeout
argument_list|)
argument_list|)
expr_stmt|;
name|MetaTableLocator
operator|.
name|setMetaLocation
argument_list|(
name|this
operator|.
name|watcher
argument_list|,
name|SN
argument_list|,
name|RegionState
operator|.
name|State
operator|.
name|OPEN
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|Utility
operator|.
name|verifyMetaRegionLocation
argument_list|(
name|connection
argument_list|,
name|watcher
argument_list|,
name|timeout
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * Test get of meta region fails properly if nothing to connect to.    */
annotation|@
name|Test
specifier|public
name|void
name|testVerifyMetaRegionLocationFails
parameter_list|()
throws|throws
name|IOException
throws|,
name|InterruptedException
throws|,
name|KeeperException
throws|,
name|ServiceException
block|{
name|ClusterConnection
name|connection
init|=
name|Mockito
operator|.
name|mock
argument_list|(
name|ClusterConnection
operator|.
name|class
argument_list|)
decl_stmt|;
name|ServiceException
name|connectException
init|=
operator|new
name|ServiceException
argument_list|(
operator|new
name|ConnectException
argument_list|(
literal|"Connection refused"
argument_list|)
argument_list|)
decl_stmt|;
specifier|final
name|AdminProtos
operator|.
name|AdminService
operator|.
name|BlockingInterface
name|implementation
init|=
name|Mockito
operator|.
name|mock
argument_list|(
name|AdminProtos
operator|.
name|AdminService
operator|.
name|BlockingInterface
operator|.
name|class
argument_list|)
decl_stmt|;
name|Mockito
operator|.
name|when
argument_list|(
name|implementation
operator|.
name|getRegionInfo
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
name|GetRegionInfoRequest
operator|)
name|Mockito
operator|.
name|any
argument_list|()
argument_list|)
argument_list|)
operator|.
name|thenThrow
argument_list|(
name|connectException
argument_list|)
expr_stmt|;
name|Mockito
operator|.
name|when
argument_list|(
name|connection
operator|.
name|getAdmin
argument_list|(
name|Mockito
operator|.
name|any
argument_list|()
argument_list|)
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|implementation
argument_list|)
expr_stmt|;
name|RpcControllerFactory
name|controllerFactory
init|=
name|Mockito
operator|.
name|mock
argument_list|(
name|RpcControllerFactory
operator|.
name|class
argument_list|)
decl_stmt|;
name|Mockito
operator|.
name|when
argument_list|(
name|controllerFactory
operator|.
name|newController
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|Mockito
operator|.
name|mock
argument_list|(
name|HBaseRpcController
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|Mockito
operator|.
name|when
argument_list|(
name|connection
operator|.
name|getRpcControllerFactory
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|controllerFactory
argument_list|)
expr_stmt|;
name|ServerName
name|sn
init|=
name|ServerName
operator|.
name|valueOf
argument_list|(
literal|"example.com"
argument_list|,
literal|1234
argument_list|,
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|)
decl_stmt|;
name|MetaTableLocator
operator|.
name|setMetaLocation
argument_list|(
name|this
operator|.
name|watcher
argument_list|,
name|sn
argument_list|,
name|RegionState
operator|.
name|State
operator|.
name|OPENING
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|Utility
operator|.
name|verifyMetaRegionLocation
argument_list|(
name|connection
argument_list|,
name|watcher
argument_list|,
literal|100
argument_list|)
argument_list|)
expr_stmt|;
name|MetaTableLocator
operator|.
name|setMetaLocation
argument_list|(
name|this
operator|.
name|watcher
argument_list|,
name|sn
argument_list|,
name|RegionState
operator|.
name|State
operator|.
name|OPEN
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|Utility
operator|.
name|verifyMetaRegionLocation
argument_list|(
name|connection
argument_list|,
name|watcher
argument_list|,
literal|100
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * Test we survive a connection refused {@link ConnectException}    */
annotation|@
name|Test
specifier|public
name|void
name|testGetMetaServerConnectionFails
parameter_list|()
throws|throws
name|IOException
throws|,
name|InterruptedException
throws|,
name|KeeperException
throws|,
name|ServiceException
block|{
name|testVerifyMetaRegionLocationWithException
argument_list|(
operator|new
name|ConnectException
argument_list|(
literal|"Connection refused"
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * Test that verifyMetaRegionLocation properly handles getting a ServerNotRunningException. See    * HBASE-4470. Note this doesn't check the exact exception thrown in the HBASE-4470 as there it is    * thrown from getHConnection() and here it is thrown from get() -- but those are both called from    * the same function anyway, and this way is less invasive than throwing from getHConnection would    * be.    */
annotation|@
name|Test
specifier|public
name|void
name|testVerifyMetaRegionServerNotRunning
parameter_list|()
throws|throws
name|IOException
throws|,
name|InterruptedException
throws|,
name|KeeperException
throws|,
name|ServiceException
block|{
name|testVerifyMetaRegionLocationWithException
argument_list|(
operator|new
name|ServerNotRunningYetException
argument_list|(
literal|"mock"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit
