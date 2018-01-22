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
name|assertTrue
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
name|EnumSet
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashMap
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
name|stream
operator|.
name|Collectors
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
name|ClusterMetrics
operator|.
name|Option
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
name|junit
operator|.
name|runner
operator|.
name|RunWith
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|runners
operator|.
name|Parameterized
import|;
end_import

begin_class
annotation|@
name|RunWith
argument_list|(
name|Parameterized
operator|.
name|class
argument_list|)
annotation|@
name|Category
argument_list|(
block|{
name|ClientTests
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
name|TestAsyncDecommissionAdminApi
extends|extends
name|TestAsyncAdminBase
block|{
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|30000
argument_list|)
specifier|public
name|void
name|testAsyncDecommissionRegionServers
parameter_list|()
throws|throws
name|Exception
block|{
name|List
argument_list|<
name|ServerName
argument_list|>
name|decommissionedRegionServers
init|=
name|admin
operator|.
name|listDecommissionedRegionServers
argument_list|()
operator|.
name|get
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
name|decommissionedRegionServers
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|createMultiRegionTable
argument_list|(
name|tableName
argument_list|,
name|FAMILY
argument_list|,
literal|4
argument_list|)
expr_stmt|;
name|ArrayList
argument_list|<
name|ServerName
argument_list|>
name|clusterRegionServers
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|admin
operator|.
name|getClusterMetrics
argument_list|(
name|EnumSet
operator|.
name|of
argument_list|(
name|Option
operator|.
name|LIVE_SERVERS
argument_list|)
argument_list|)
operator|.
name|get
argument_list|()
operator|.
name|getLiveServerMetrics
argument_list|()
operator|.
name|keySet
argument_list|()
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|clusterRegionServers
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|HashMap
argument_list|<
name|ServerName
argument_list|,
name|List
argument_list|<
name|RegionInfo
argument_list|>
argument_list|>
name|serversToDecommssion
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
comment|// Get a server that has regions. We will decommission one of the servers,
comment|// leaving one online.
name|int
name|i
decl_stmt|;
for|for
control|(
name|i
operator|=
literal|0
init|;
name|i
operator|<
name|clusterRegionServers
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|List
argument_list|<
name|RegionInfo
argument_list|>
name|regionsOnServer
init|=
name|admin
operator|.
name|getRegions
argument_list|(
name|clusterRegionServers
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
if|if
condition|(
name|regionsOnServer
operator|.
name|size
argument_list|()
operator|>
literal|0
condition|)
block|{
name|serversToDecommssion
operator|.
name|put
argument_list|(
name|clusterRegionServers
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|,
name|regionsOnServer
argument_list|)
expr_stmt|;
break|break;
block|}
block|}
name|clusterRegionServers
operator|.
name|remove
argument_list|(
name|i
argument_list|)
expr_stmt|;
name|ServerName
name|remainingServer
init|=
name|clusterRegionServers
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
comment|// Decommission
name|admin
operator|.
name|decommissionRegionServers
argument_list|(
operator|new
name|ArrayList
argument_list|<
name|ServerName
argument_list|>
argument_list|(
name|serversToDecommssion
operator|.
name|keySet
argument_list|()
argument_list|)
argument_list|,
literal|true
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|admin
operator|.
name|listDecommissionedRegionServers
argument_list|()
operator|.
name|get
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
comment|// Verify the regions have been off the decommissioned servers, all on the remaining server.
for|for
control|(
name|ServerName
name|server
range|:
name|serversToDecommssion
operator|.
name|keySet
argument_list|()
control|)
block|{
for|for
control|(
name|RegionInfo
name|region
range|:
name|serversToDecommssion
operator|.
name|get
argument_list|(
name|server
argument_list|)
control|)
block|{
name|TEST_UTIL
operator|.
name|assertRegionOnServer
argument_list|(
name|region
argument_list|,
name|remainingServer
argument_list|,
literal|10000
argument_list|)
expr_stmt|;
block|}
block|}
comment|// Recommission and load regions
for|for
control|(
name|ServerName
name|server
range|:
name|serversToDecommssion
operator|.
name|keySet
argument_list|()
control|)
block|{
name|List
argument_list|<
name|byte
index|[]
argument_list|>
name|encodedRegionNames
init|=
name|serversToDecommssion
operator|.
name|get
argument_list|(
name|server
argument_list|)
operator|.
name|stream
argument_list|()
operator|.
name|map
argument_list|(
name|region
lambda|->
name|region
operator|.
name|getEncodedNameAsBytes
argument_list|()
argument_list|)
operator|.
name|collect
argument_list|(
name|Collectors
operator|.
name|toList
argument_list|()
argument_list|)
decl_stmt|;
name|admin
operator|.
name|recommissionRegionServer
argument_list|(
name|server
argument_list|,
name|encodedRegionNames
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
block|}
name|assertTrue
argument_list|(
name|admin
operator|.
name|listDecommissionedRegionServers
argument_list|()
operator|.
name|get
argument_list|()
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
comment|// Verify the regions have been moved to the recommissioned servers
for|for
control|(
name|ServerName
name|server
range|:
name|serversToDecommssion
operator|.
name|keySet
argument_list|()
control|)
block|{
for|for
control|(
name|RegionInfo
name|region
range|:
name|serversToDecommssion
operator|.
name|get
argument_list|(
name|server
argument_list|)
control|)
block|{
name|TEST_UTIL
operator|.
name|assertRegionOnServer
argument_list|(
name|region
argument_list|,
name|server
argument_list|,
literal|10000
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit

