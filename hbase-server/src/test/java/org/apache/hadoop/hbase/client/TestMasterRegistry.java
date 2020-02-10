begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
import|import
name|java
operator|.
name|net
operator|.
name|UnknownHostException
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
name|Arrays
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
name|Comparator
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
name|master
operator|.
name|HMaster
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
name|TestMasterRegistry
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
name|TestMasterRegistry
operator|.
name|class
argument_list|)
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
literal|3
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
name|TEST_UTIL
operator|.
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
block|}
comment|/**    * Generates a string of dummy master addresses in host:port format. Every other hostname won't    * have a port number.    */
specifier|private
specifier|static
name|String
name|generateDummyMastersList
parameter_list|(
name|int
name|size
parameter_list|)
block|{
name|List
argument_list|<
name|String
argument_list|>
name|masters
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
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
name|size
condition|;
name|i
operator|++
control|)
block|{
name|masters
operator|.
name|add
argument_list|(
literal|" localhost"
operator|+
operator|(
name|i
operator|%
literal|2
operator|==
literal|0
condition|?
literal|":"
operator|+
operator|(
literal|1000
operator|+
name|i
operator|)
else|:
literal|""
operator|)
argument_list|)
expr_stmt|;
block|}
return|return
name|String
operator|.
name|join
argument_list|(
literal|","
argument_list|,
name|masters
argument_list|)
return|;
block|}
comment|/**    * Makes sure the master registry parses the master end points in the configuration correctly.    */
annotation|@
name|Test
specifier|public
name|void
name|testMasterAddressParsing
parameter_list|()
throws|throws
name|UnknownHostException
block|{
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
name|int
name|numMasters
init|=
literal|10
decl_stmt|;
name|conf
operator|.
name|set
argument_list|(
name|HConstants
operator|.
name|MASTER_ADDRS_KEY
argument_list|,
name|generateDummyMastersList
argument_list|(
name|numMasters
argument_list|)
argument_list|)
expr_stmt|;
try|try
init|(
name|MasterRegistry
name|registry
init|=
operator|new
name|MasterRegistry
argument_list|(
name|conf
argument_list|)
init|)
block|{
name|List
argument_list|<
name|ServerName
argument_list|>
name|parsedMasters
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|registry
operator|.
name|getParsedMasterServers
argument_list|()
argument_list|)
decl_stmt|;
comment|// Half of them would be without a port, duplicates are removed.
name|assertEquals
argument_list|(
name|numMasters
operator|/
literal|2
operator|+
literal|1
argument_list|,
name|parsedMasters
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
comment|// Sort in the increasing order of port numbers.
name|Collections
operator|.
name|sort
argument_list|(
name|parsedMasters
argument_list|,
name|Comparator
operator|.
name|comparingInt
argument_list|(
name|ServerName
operator|::
name|getPort
argument_list|)
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|parsedMasters
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|ServerName
name|sn
init|=
name|parsedMasters
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"localhost"
argument_list|,
name|sn
operator|.
name|getHostname
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|i
operator|==
name|parsedMasters
operator|.
name|size
argument_list|()
operator|-
literal|1
condition|)
block|{
comment|// Last entry should be the one with default port.
name|assertEquals
argument_list|(
name|HConstants
operator|.
name|DEFAULT_MASTER_PORT
argument_list|,
name|sn
operator|.
name|getPort
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|assertEquals
argument_list|(
literal|1000
operator|+
operator|(
literal|2
operator|*
name|i
operator|)
argument_list|,
name|sn
operator|.
name|getPort
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testRegistryRPCs
parameter_list|()
throws|throws
name|Exception
block|{
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
name|HMaster
name|activeMaster
init|=
name|TEST_UTIL
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|getMaster
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|numHedgedReqs
init|=
literal|1
init|;
name|numHedgedReqs
operator|<=
literal|3
condition|;
name|numHedgedReqs
operator|++
control|)
block|{
if|if
condition|(
name|numHedgedReqs
operator|==
literal|1
condition|)
block|{
name|conf
operator|.
name|setBoolean
argument_list|(
name|HConstants
operator|.
name|MASTER_REGISTRY_ENABLE_HEDGED_READS_KEY
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|conf
operator|.
name|setBoolean
argument_list|(
name|HConstants
operator|.
name|MASTER_REGISTRY_ENABLE_HEDGED_READS_KEY
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
name|conf
operator|.
name|setInt
argument_list|(
name|HConstants
operator|.
name|HBASE_RPCS_HEDGED_REQS_FANOUT_KEY
argument_list|,
name|numHedgedReqs
argument_list|)
expr_stmt|;
try|try
init|(
name|MasterRegistry
name|registry
init|=
operator|new
name|MasterRegistry
argument_list|(
name|conf
argument_list|)
init|)
block|{
name|assertEquals
argument_list|(
name|registry
operator|.
name|getClusterId
argument_list|()
operator|.
name|get
argument_list|()
argument_list|,
name|activeMaster
operator|.
name|getClusterId
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|registry
operator|.
name|getActiveMaster
argument_list|()
operator|.
name|get
argument_list|()
argument_list|,
name|activeMaster
operator|.
name|getServerName
argument_list|()
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|HRegionLocation
argument_list|>
name|metaLocations
init|=
name|Arrays
operator|.
name|asList
argument_list|(
name|registry
operator|.
name|getMetaRegionLocations
argument_list|()
operator|.
name|get
argument_list|()
operator|.
name|getRegionLocations
argument_list|()
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|HRegionLocation
argument_list|>
name|actualMetaLocations
init|=
name|activeMaster
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
name|Collections
operator|.
name|sort
argument_list|(
name|actualMetaLocations
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|actualMetaLocations
argument_list|,
name|metaLocations
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit

