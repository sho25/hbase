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
name|test
package|;
end_package

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
name|IntegrationTestingUtility
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
name|IntegrationTests
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
name|LoadTestTool
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
name|util
operator|.
name|ToolRunner
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
name|common
operator|.
name|collect
operator|.
name|Lists
import|;
end_import

begin_comment
comment|/**  * Extends {@link IntegrationTestTimeBoundedRequestsWithRegionReplicas} for multi-gets  * Besides the options already talked about in IntegrationTestTimeBoundedRequestsWithRegionReplicas  * the addition options here are:  *<pre>  * -DIntegrationTestTimeBoundedMultiGetRequestsWithRegionReplicas.multiget_batchsize=100  * -DIntegrationTestTimeBoundedMultiGetRequestsWithRegionReplicas.num_regions_per_server=5  *</pre>  * The multiget_batchsize when set to 1 will issue normal GETs.  * The num_regions_per_server argument indirectly impacts the region size (for a given number of  * num_keys_per_server). That in conjunction with multiget_batchsize would have different behaviors  * - the batch of gets goes to the same region or to multiple regions.  */
end_comment

begin_class
annotation|@
name|Category
argument_list|(
name|IntegrationTests
operator|.
name|class
argument_list|)
specifier|public
class|class
name|IntegrationTestTimeBoundedMultiGetRequestsWithRegionReplicas
extends|extends
name|IntegrationTestTimeBoundedRequestsWithRegionReplicas
block|{
annotation|@
name|Override
specifier|protected
name|String
index|[]
name|getArgsForLoadTestTool
parameter_list|(
name|String
name|mode
parameter_list|,
name|String
name|modeSpecificArg
parameter_list|,
name|long
name|startKey
parameter_list|,
name|long
name|numKeys
parameter_list|)
block|{
name|List
argument_list|<
name|String
argument_list|>
name|args
init|=
name|Lists
operator|.
name|newArrayList
argument_list|(
name|super
operator|.
name|getArgsForLoadTestTool
argument_list|(
name|mode
argument_list|,
name|modeSpecificArg
argument_list|,
name|startKey
argument_list|,
name|numKeys
argument_list|)
argument_list|)
decl_stmt|;
name|String
name|clazz
init|=
name|this
operator|.
name|getClass
argument_list|()
operator|.
name|getSimpleName
argument_list|()
decl_stmt|;
name|args
operator|.
name|add
argument_list|(
literal|"-"
operator|+
name|LoadTestTool
operator|.
name|OPT_MULTIGET
argument_list|)
expr_stmt|;
name|args
operator|.
name|add
argument_list|(
name|conf
operator|.
name|get
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"%s.%s"
argument_list|,
name|clazz
argument_list|,
name|LoadTestTool
operator|.
name|OPT_MULTIGET
argument_list|)
argument_list|,
literal|"100"
argument_list|)
argument_list|)
expr_stmt|;
name|args
operator|.
name|add
argument_list|(
literal|"-"
operator|+
name|LoadTestTool
operator|.
name|OPT_NUM_REGIONS_PER_SERVER
argument_list|)
expr_stmt|;
name|args
operator|.
name|add
argument_list|(
name|conf
operator|.
name|get
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"%s.%s"
argument_list|,
name|clazz
argument_list|,
name|LoadTestTool
operator|.
name|OPT_NUM_REGIONS_PER_SERVER
argument_list|)
argument_list|,
name|Integer
operator|.
name|toString
argument_list|(
name|LoadTestTool
operator|.
name|DEFAULT_NUM_REGIONS_PER_SERVER
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|args
operator|.
name|toArray
argument_list|(
operator|new
name|String
index|[
name|args
operator|.
name|size
argument_list|()
index|]
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|void
name|main
parameter_list|(
name|String
name|args
index|[]
parameter_list|)
throws|throws
name|Exception
block|{
name|Configuration
name|conf
init|=
name|HBaseConfiguration
operator|.
name|create
argument_list|()
decl_stmt|;
name|IntegrationTestingUtility
operator|.
name|setUseDistributedCluster
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|int
name|ret
init|=
name|ToolRunner
operator|.
name|run
argument_list|(
name|conf
argument_list|,
operator|new
name|IntegrationTestTimeBoundedMultiGetRequestsWithRegionReplicas
argument_list|()
argument_list|,
name|args
argument_list|)
decl_stmt|;
name|System
operator|.
name|exit
argument_list|(
name|ret
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

