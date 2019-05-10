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
name|META_REPLICAS_NUM
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
name|assertNotEquals
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
name|assertNotSame
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
name|ExecutionException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|stream
operator|.
name|IntStream
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
name|RegionLocations
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
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|zookeeper
operator|.
name|ReadOnlyZKClient
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
name|TestZKAsyncRegistry
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
name|TestZKAsyncRegistry
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|TestZKAsyncRegistry
operator|.
name|class
argument_list|)
decl_stmt|;
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
name|ZKAsyncRegistry
name|REGISTRY
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
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setInt
argument_list|(
name|META_REPLICAS_NUM
argument_list|,
literal|3
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|startMiniCluster
argument_list|(
literal|3
argument_list|)
expr_stmt|;
name|Configuration
name|conf
init|=
operator|new
name|Configuration
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
comment|// make sure that we do not depend on this config when getting locations for meta replicas, see
comment|// HBASE-21658.
name|conf
operator|.
name|setInt
argument_list|(
name|META_REPLICAS_NUM
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|REGISTRY
operator|=
operator|new
name|ZKAsyncRegistry
argument_list|(
name|conf
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
name|IOUtils
operator|.
name|closeQuietly
argument_list|(
name|REGISTRY
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|test
parameter_list|()
throws|throws
name|InterruptedException
throws|,
name|ExecutionException
throws|,
name|IOException
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"STARTED TEST"
argument_list|)
expr_stmt|;
name|String
name|clusterId
init|=
name|REGISTRY
operator|.
name|getClusterId
argument_list|()
operator|.
name|get
argument_list|()
decl_stmt|;
name|String
name|expectedClusterId
init|=
name|TEST_UTIL
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|getMaster
argument_list|()
operator|.
name|getClusterId
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|"Expected "
operator|+
name|expectedClusterId
operator|+
literal|", found="
operator|+
name|clusterId
argument_list|,
name|expectedClusterId
argument_list|,
name|clusterId
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|TEST_UTIL
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|getClusterMetrics
argument_list|()
operator|.
name|getLiveServerMetrics
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
name|REGISTRY
operator|.
name|getCurrentNrHRS
argument_list|()
operator|.
name|get
argument_list|()
operator|.
name|intValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|TEST_UTIL
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|getMaster
argument_list|()
operator|.
name|getServerName
argument_list|()
argument_list|,
name|REGISTRY
operator|.
name|getMasterAddress
argument_list|()
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
operator|-
literal|1
argument_list|,
name|REGISTRY
operator|.
name|getMasterInfoPort
argument_list|()
operator|.
name|get
argument_list|()
operator|.
name|intValue
argument_list|()
argument_list|)
expr_stmt|;
name|RegionReplicaTestHelper
operator|.
name|waitUntilAllMetaReplicasHavingRegionLocation
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|REGISTRY
argument_list|,
literal|3
argument_list|)
expr_stmt|;
name|RegionLocations
name|locs
init|=
name|REGISTRY
operator|.
name|getMetaRegionLocation
argument_list|()
operator|.
name|get
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|3
argument_list|,
name|locs
operator|.
name|getRegionLocations
argument_list|()
operator|.
name|length
argument_list|)
expr_stmt|;
name|IntStream
operator|.
name|range
argument_list|(
literal|0
argument_list|,
literal|3
argument_list|)
operator|.
name|forEach
argument_list|(
name|i
lambda|->
block|{
name|HRegionLocation
name|loc
init|=
name|locs
operator|.
name|getRegionLocation
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|assertNotNull
argument_list|(
literal|"Replica "
operator|+
name|i
operator|+
literal|" doesn't have location"
argument_list|,
name|loc
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|TableName
operator|.
name|META_TABLE_NAME
argument_list|,
name|loc
operator|.
name|getRegion
argument_list|()
operator|.
name|getTable
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|i
argument_list|,
name|loc
operator|.
name|getRegion
argument_list|()
operator|.
name|getReplicaId
argument_list|()
argument_list|)
expr_stmt|;
block|}
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testIndependentZKConnections
parameter_list|()
throws|throws
name|IOException
block|{
try|try
init|(
name|ReadOnlyZKClient
name|zk1
init|=
name|REGISTRY
operator|.
name|getZKClient
argument_list|()
init|)
block|{
name|Configuration
name|otherConf
init|=
operator|new
name|Configuration
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
name|otherConf
operator|.
name|set
argument_list|(
name|HConstants
operator|.
name|ZOOKEEPER_QUORUM
argument_list|,
literal|"127.0.0.1"
argument_list|)
expr_stmt|;
try|try
init|(
name|ZKAsyncRegistry
name|otherRegistry
init|=
operator|new
name|ZKAsyncRegistry
argument_list|(
name|otherConf
argument_list|)
init|)
block|{
name|ReadOnlyZKClient
name|zk2
init|=
name|otherRegistry
operator|.
name|getZKClient
argument_list|()
decl_stmt|;
name|assertNotSame
argument_list|(
literal|"Using a different configuration / quorum should result in different "
operator|+
literal|"backing zk connection."
argument_list|,
name|zk1
argument_list|,
name|zk2
argument_list|)
expr_stmt|;
name|assertNotEquals
argument_list|(
literal|"Using a different configrution / quorum should be reflected in the zk connection."
argument_list|,
name|zk1
operator|.
name|getConnectString
argument_list|()
argument_list|,
name|zk2
operator|.
name|getConnectString
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
finally|finally
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"DONE!"
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

