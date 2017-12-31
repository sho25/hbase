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
name|util
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
import|import
name|java
operator|.
name|io
operator|.
name|File
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|FileWriter
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
name|HColumnDescriptor
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
name|MiniHBaseCluster
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
name|Waiter
operator|.
name|Predicate
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
name|Admin
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
name|HRegionServer
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
name|RegionMover
operator|.
name|RegionMoverBuilder
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

begin_comment
comment|/**  * Tests for Region Mover Load/Unload functionality with and without ack mode and also to test  * exclude functionality useful for rack decommissioning  */
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
name|TestRegionMover
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
name|TestRegionMover
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|getClass
argument_list|()
argument_list|)
decl_stmt|;
specifier|protected
specifier|final
specifier|static
name|HBaseTestingUtility
name|TEST_UTIL
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
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
literal|3
argument_list|)
expr_stmt|;
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
name|TEST_UTIL
operator|.
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Before
specifier|public
name|void
name|setUp
parameter_list|()
throws|throws
name|Exception
block|{
comment|// Create a pre-split table just to populate some regions
name|TableName
name|tableName
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"testRegionMover"
argument_list|)
decl_stmt|;
name|Admin
name|admin
init|=
name|TEST_UTIL
operator|.
name|getAdmin
argument_list|()
decl_stmt|;
if|if
condition|(
name|admin
operator|.
name|tableExists
argument_list|(
name|tableName
argument_list|)
condition|)
block|{
name|TEST_UTIL
operator|.
name|deleteTable
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
block|}
name|HTableDescriptor
name|tableDesc
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
name|HColumnDescriptor
name|fam1
init|=
operator|new
name|HColumnDescriptor
argument_list|(
literal|"fam1"
argument_list|)
decl_stmt|;
name|tableDesc
operator|.
name|addFamily
argument_list|(
name|fam1
argument_list|)
expr_stmt|;
try|try
block|{
name|admin
operator|.
name|setBalancerRunning
argument_list|(
literal|false
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|String
name|startKey
init|=
literal|"a"
decl_stmt|;
name|String
name|endKey
init|=
literal|"z"
decl_stmt|;
name|admin
operator|.
name|createTable
argument_list|(
name|tableDesc
argument_list|,
name|startKey
operator|.
name|getBytes
argument_list|()
argument_list|,
name|endKey
operator|.
name|getBytes
argument_list|()
argument_list|,
literal|9
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
if|if
condition|(
name|admin
operator|!=
literal|null
condition|)
block|{
name|admin
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testLoadWithAck
parameter_list|()
throws|throws
name|Exception
block|{
name|MiniHBaseCluster
name|cluster
init|=
name|TEST_UTIL
operator|.
name|getHBaseCluster
argument_list|()
decl_stmt|;
name|HRegionServer
name|regionServer
init|=
name|cluster
operator|.
name|getRegionServer
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|String
name|rsName
init|=
name|regionServer
operator|.
name|getServerName
argument_list|()
operator|.
name|getHostname
argument_list|()
decl_stmt|;
name|int
name|port
init|=
name|regionServer
operator|.
name|getServerName
argument_list|()
operator|.
name|getPort
argument_list|()
decl_stmt|;
name|int
name|noRegions
init|=
name|regionServer
operator|.
name|getNumberOfOnlineRegions
argument_list|()
decl_stmt|;
name|String
name|rs
init|=
name|rsName
operator|+
literal|":"
operator|+
name|Integer
operator|.
name|toString
argument_list|(
name|port
argument_list|)
decl_stmt|;
name|RegionMoverBuilder
name|rmBuilder
init|=
operator|new
name|RegionMoverBuilder
argument_list|(
name|rs
argument_list|,
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
operator|.
name|ack
argument_list|(
literal|true
argument_list|)
operator|.
name|maxthreads
argument_list|(
literal|8
argument_list|)
decl_stmt|;
name|RegionMover
name|rm
init|=
name|rmBuilder
operator|.
name|build
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Unloading "
operator|+
name|rs
argument_list|)
expr_stmt|;
name|rm
operator|.
name|unload
argument_list|()
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|regionServer
operator|.
name|getNumberOfOnlineRegions
argument_list|()
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Successfully Unloaded\nNow Loading"
argument_list|)
expr_stmt|;
name|rm
operator|.
name|load
argument_list|()
expr_stmt|;
name|assertEquals
argument_list|(
name|noRegions
argument_list|,
name|regionServer
operator|.
name|getNumberOfOnlineRegions
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/** Test to unload a regionserver first and then load it using no Ack mode    * we check if some regions are loaded on the region server(since no ack is best effort)    */
annotation|@
name|Test
specifier|public
name|void
name|testLoadWithoutAck
parameter_list|()
throws|throws
name|Exception
block|{
name|MiniHBaseCluster
name|cluster
init|=
name|TEST_UTIL
operator|.
name|getHBaseCluster
argument_list|()
decl_stmt|;
specifier|final
name|HRegionServer
name|regionServer
init|=
name|cluster
operator|.
name|getRegionServer
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|String
name|rsName
init|=
name|regionServer
operator|.
name|getServerName
argument_list|()
operator|.
name|getHostname
argument_list|()
decl_stmt|;
name|int
name|port
init|=
name|regionServer
operator|.
name|getServerName
argument_list|()
operator|.
name|getPort
argument_list|()
decl_stmt|;
name|int
name|noRegions
init|=
name|regionServer
operator|.
name|getNumberOfOnlineRegions
argument_list|()
decl_stmt|;
name|String
name|rs
init|=
name|rsName
operator|+
literal|":"
operator|+
name|Integer
operator|.
name|toString
argument_list|(
name|port
argument_list|)
decl_stmt|;
name|RegionMoverBuilder
name|rmBuilder
init|=
operator|new
name|RegionMoverBuilder
argument_list|(
name|rs
argument_list|,
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
operator|.
name|ack
argument_list|(
literal|true
argument_list|)
decl_stmt|;
name|RegionMover
name|rm
init|=
name|rmBuilder
operator|.
name|build
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Unloading "
operator|+
name|rs
argument_list|)
expr_stmt|;
name|rm
operator|.
name|unload
argument_list|()
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|regionServer
operator|.
name|getNumberOfOnlineRegions
argument_list|()
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Successfully Unloaded\nNow Loading"
argument_list|)
expr_stmt|;
name|rm
operator|=
name|rmBuilder
operator|.
name|ack
argument_list|(
literal|false
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
name|rm
operator|.
name|load
argument_list|()
expr_stmt|;
name|TEST_UTIL
operator|.
name|waitFor
argument_list|(
literal|5000
argument_list|,
literal|500
argument_list|,
operator|new
name|Predicate
argument_list|<
name|Exception
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|boolean
name|evaluate
parameter_list|()
throws|throws
name|Exception
block|{
return|return
name|regionServer
operator|.
name|getNumberOfOnlineRegions
argument_list|()
operator|>
literal|0
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
name|testUnloadWithoutAck
parameter_list|()
throws|throws
name|Exception
block|{
name|MiniHBaseCluster
name|cluster
init|=
name|TEST_UTIL
operator|.
name|getHBaseCluster
argument_list|()
decl_stmt|;
specifier|final
name|HRegionServer
name|regionServer
init|=
name|cluster
operator|.
name|getRegionServer
argument_list|(
literal|0
argument_list|)
decl_stmt|;
specifier|final
name|int
name|noRegions
init|=
name|regionServer
operator|.
name|getNumberOfOnlineRegions
argument_list|()
decl_stmt|;
name|String
name|rsName
init|=
name|regionServer
operator|.
name|getServerName
argument_list|()
operator|.
name|getHostname
argument_list|()
decl_stmt|;
name|int
name|port
init|=
name|regionServer
operator|.
name|getServerName
argument_list|()
operator|.
name|getPort
argument_list|()
decl_stmt|;
name|String
name|rs
init|=
name|rsName
operator|+
literal|":"
operator|+
name|Integer
operator|.
name|toString
argument_list|(
name|port
argument_list|)
decl_stmt|;
name|RegionMoverBuilder
name|rmBuilder
init|=
operator|new
name|RegionMoverBuilder
argument_list|(
name|rs
argument_list|,
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
operator|.
name|ack
argument_list|(
literal|false
argument_list|)
decl_stmt|;
name|RegionMover
name|rm
init|=
name|rmBuilder
operator|.
name|build
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Unloading "
operator|+
name|rs
argument_list|)
expr_stmt|;
name|rm
operator|.
name|unload
argument_list|()
expr_stmt|;
name|TEST_UTIL
operator|.
name|waitFor
argument_list|(
literal|5000
argument_list|,
literal|500
argument_list|,
operator|new
name|Predicate
argument_list|<
name|Exception
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|boolean
name|evaluate
parameter_list|()
throws|throws
name|Exception
block|{
return|return
name|regionServer
operator|.
name|getNumberOfOnlineRegions
argument_list|()
operator|<
name|noRegions
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
name|testUnloadWithAck
parameter_list|()
throws|throws
name|Exception
block|{
name|MiniHBaseCluster
name|cluster
init|=
name|TEST_UTIL
operator|.
name|getHBaseCluster
argument_list|()
decl_stmt|;
name|HRegionServer
name|regionServer
init|=
name|cluster
operator|.
name|getRegionServer
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|String
name|rsName
init|=
name|regionServer
operator|.
name|getServerName
argument_list|()
operator|.
name|getHostname
argument_list|()
decl_stmt|;
name|int
name|port
init|=
name|regionServer
operator|.
name|getServerName
argument_list|()
operator|.
name|getPort
argument_list|()
decl_stmt|;
name|String
name|rs
init|=
name|rsName
operator|+
literal|":"
operator|+
name|Integer
operator|.
name|toString
argument_list|(
name|port
argument_list|)
decl_stmt|;
name|RegionMoverBuilder
name|rmBuilder
init|=
operator|new
name|RegionMoverBuilder
argument_list|(
name|rs
argument_list|,
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
operator|.
name|ack
argument_list|(
literal|true
argument_list|)
decl_stmt|;
name|RegionMover
name|rm
init|=
name|rmBuilder
operator|.
name|build
argument_list|()
decl_stmt|;
name|rm
operator|.
name|unload
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Unloading "
operator|+
name|rs
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|regionServer
operator|.
name|getNumberOfOnlineRegions
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**    * Test that loading the same region set doesn't cause timeout loop during meta load.    */
annotation|@
name|Test
specifier|public
name|void
name|testRepeatedLoad
parameter_list|()
throws|throws
name|Exception
block|{
name|MiniHBaseCluster
name|cluster
init|=
name|TEST_UTIL
operator|.
name|getHBaseCluster
argument_list|()
decl_stmt|;
name|HRegionServer
name|regionServer
init|=
name|cluster
operator|.
name|getRegionServer
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|String
name|rsName
init|=
name|regionServer
operator|.
name|getServerName
argument_list|()
operator|.
name|getHostname
argument_list|()
decl_stmt|;
name|int
name|port
init|=
name|regionServer
operator|.
name|getServerName
argument_list|()
operator|.
name|getPort
argument_list|()
decl_stmt|;
name|String
name|rs
init|=
name|rsName
operator|+
literal|":"
operator|+
name|Integer
operator|.
name|toString
argument_list|(
name|port
argument_list|)
decl_stmt|;
name|RegionMoverBuilder
name|rmBuilder
init|=
operator|new
name|RegionMoverBuilder
argument_list|(
name|rs
argument_list|,
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
operator|.
name|ack
argument_list|(
literal|true
argument_list|)
decl_stmt|;
name|RegionMover
name|rm
init|=
name|rmBuilder
operator|.
name|build
argument_list|()
decl_stmt|;
name|rm
operator|.
name|unload
argument_list|()
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|regionServer
operator|.
name|getNumberOfOnlineRegions
argument_list|()
argument_list|)
expr_stmt|;
name|rmBuilder
operator|=
operator|new
name|RegionMoverBuilder
argument_list|(
name|rs
argument_list|,
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
operator|.
name|ack
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|rm
operator|=
name|rmBuilder
operator|.
name|build
argument_list|()
expr_stmt|;
name|rm
operator|.
name|load
argument_list|()
expr_stmt|;
name|rm
operator|.
name|load
argument_list|()
expr_stmt|;
comment|//Repeat the same load. It should be very fast because all regions are already moved.
block|}
comment|/**    * To test that we successfully exclude a server from the unloading process We test for the number    * of regions on Excluded server and also test that regions are unloaded successfully    */
annotation|@
name|Test
specifier|public
name|void
name|testExclude
parameter_list|()
throws|throws
name|Exception
block|{
name|MiniHBaseCluster
name|cluster
init|=
name|TEST_UTIL
operator|.
name|getHBaseCluster
argument_list|()
decl_stmt|;
name|File
name|excludeFile
init|=
operator|new
name|File
argument_list|(
name|TEST_UTIL
operator|.
name|getDataTestDir
argument_list|()
operator|.
name|toUri
argument_list|()
operator|.
name|getPath
argument_list|()
argument_list|,
literal|"exclude_file"
argument_list|)
decl_stmt|;
name|FileWriter
name|fos
init|=
operator|new
name|FileWriter
argument_list|(
name|excludeFile
argument_list|)
decl_stmt|;
name|HRegionServer
name|excludeServer
init|=
name|cluster
operator|.
name|getRegionServer
argument_list|(
literal|1
argument_list|)
decl_stmt|;
name|String
name|excludeHostname
init|=
name|excludeServer
operator|.
name|getServerName
argument_list|()
operator|.
name|getHostname
argument_list|()
decl_stmt|;
name|int
name|excludeServerPort
init|=
name|excludeServer
operator|.
name|getServerName
argument_list|()
operator|.
name|getPort
argument_list|()
decl_stmt|;
name|int
name|regionsExcludeServer
init|=
name|excludeServer
operator|.
name|getNumberOfOnlineRegions
argument_list|()
decl_stmt|;
name|String
name|excludeServerName
init|=
name|excludeHostname
operator|+
literal|":"
operator|+
name|Integer
operator|.
name|toString
argument_list|(
name|excludeServerPort
argument_list|)
decl_stmt|;
name|fos
operator|.
name|write
argument_list|(
name|excludeServerName
argument_list|)
expr_stmt|;
name|fos
operator|.
name|close
argument_list|()
expr_stmt|;
name|HRegionServer
name|regionServer
init|=
name|cluster
operator|.
name|getRegionServer
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|String
name|rsName
init|=
name|regionServer
operator|.
name|getServerName
argument_list|()
operator|.
name|getHostname
argument_list|()
decl_stmt|;
name|int
name|port
init|=
name|regionServer
operator|.
name|getServerName
argument_list|()
operator|.
name|getPort
argument_list|()
decl_stmt|;
name|String
name|rs
init|=
name|rsName
operator|+
literal|":"
operator|+
name|Integer
operator|.
name|toString
argument_list|(
name|port
argument_list|)
decl_stmt|;
name|RegionMoverBuilder
name|rmBuilder
init|=
operator|new
name|RegionMoverBuilder
argument_list|(
name|rs
argument_list|,
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
operator|.
name|ack
argument_list|(
literal|true
argument_list|)
operator|.
name|excludeFile
argument_list|(
name|excludeFile
operator|.
name|getCanonicalPath
argument_list|()
argument_list|)
decl_stmt|;
name|RegionMover
name|rm
init|=
name|rmBuilder
operator|.
name|build
argument_list|()
decl_stmt|;
name|rm
operator|.
name|unload
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Unloading "
operator|+
name|rs
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|regionServer
operator|.
name|getNumberOfOnlineRegions
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|regionsExcludeServer
argument_list|,
name|cluster
operator|.
name|getRegionServer
argument_list|(
literal|1
argument_list|)
operator|.
name|getNumberOfOnlineRegions
argument_list|()
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Before:"
operator|+
name|regionsExcludeServer
operator|+
literal|" After:"
operator|+
name|cluster
operator|.
name|getRegionServer
argument_list|(
literal|1
argument_list|)
operator|.
name|getNumberOfOnlineRegions
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testRegionServerPort
parameter_list|()
block|{
name|MiniHBaseCluster
name|cluster
init|=
name|TEST_UTIL
operator|.
name|getHBaseCluster
argument_list|()
decl_stmt|;
name|HRegionServer
name|regionServer
init|=
name|cluster
operator|.
name|getRegionServer
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|String
name|rsName
init|=
name|regionServer
operator|.
name|getServerName
argument_list|()
operator|.
name|getHostname
argument_list|()
decl_stmt|;
specifier|final
name|int
name|PORT
init|=
literal|16021
decl_stmt|;
name|Configuration
name|conf
init|=
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
name|String
name|originalPort
init|=
name|conf
operator|.
name|get
argument_list|(
name|HConstants
operator|.
name|REGIONSERVER_PORT
argument_list|)
decl_stmt|;
name|conf
operator|.
name|set
argument_list|(
name|HConstants
operator|.
name|REGIONSERVER_PORT
argument_list|,
name|Integer
operator|.
name|toString
argument_list|(
name|PORT
argument_list|)
argument_list|)
expr_stmt|;
name|RegionMoverBuilder
name|rmBuilder
init|=
operator|new
name|RegionMoverBuilder
argument_list|(
name|rsName
argument_list|,
name|conf
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|PORT
argument_list|,
name|rmBuilder
operator|.
name|port
argument_list|)
expr_stmt|;
if|if
condition|(
name|originalPort
operator|!=
literal|null
condition|)
block|{
name|conf
operator|.
name|set
argument_list|(
name|HConstants
operator|.
name|REGIONSERVER_PORT
argument_list|,
name|originalPort
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

