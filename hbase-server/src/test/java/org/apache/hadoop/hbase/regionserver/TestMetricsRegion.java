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
name|regionserver
package|;
end_package

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
name|CompatibilityFactory
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
name|test
operator|.
name|MetricsAssertHelper
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
name|SmallTests
operator|.
name|class
argument_list|)
specifier|public
class|class
name|TestMetricsRegion
block|{
specifier|public
name|MetricsAssertHelper
name|HELPER
init|=
name|CompatibilityFactory
operator|.
name|getInstance
argument_list|(
name|MetricsAssertHelper
operator|.
name|class
argument_list|)
decl_stmt|;
annotation|@
name|Test
specifier|public
name|void
name|testRegionWrapperMetrics
parameter_list|()
block|{
name|MetricsRegion
name|mr
init|=
operator|new
name|MetricsRegion
argument_list|(
operator|new
name|MetricsRegionWrapperStub
argument_list|()
argument_list|)
decl_stmt|;
name|MetricsRegionAggregateSource
name|agg
init|=
name|mr
operator|.
name|getSource
argument_list|()
operator|.
name|getAggregateSource
argument_list|()
decl_stmt|;
name|HELPER
operator|.
name|assertGauge
argument_list|(
literal|"namespace_TestNS_table_MetricsRegionWrapperStub_region_DEADBEEF001_metric_storeCount"
argument_list|,
literal|101
argument_list|,
name|agg
argument_list|)
expr_stmt|;
name|HELPER
operator|.
name|assertGauge
argument_list|(
literal|"namespace_TestNS_table_MetricsRegionWrapperStub_region_DEADBEEF001_metric_storeFileCount"
argument_list|,
literal|102
argument_list|,
name|agg
argument_list|)
expr_stmt|;
name|HELPER
operator|.
name|assertGauge
argument_list|(
literal|"namespace_TestNS_table_MetricsRegionWrapperStub_region_DEADBEEF001_metric_memstoreSize"
argument_list|,
literal|103
argument_list|,
name|agg
argument_list|)
expr_stmt|;
name|mr
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

