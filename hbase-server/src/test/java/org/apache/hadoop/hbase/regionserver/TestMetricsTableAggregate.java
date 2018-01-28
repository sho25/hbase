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
name|CompatibilitySingletonFactory
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
name|test
operator|.
name|MetricsAssertHelper
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
name|RegionServerTests
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
name|SmallTests
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
name|RegionServerTests
operator|.
name|class
block|,
name|SmallTests
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|TestMetricsTableAggregate
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
name|TestMetricsTableAggregate
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|public
specifier|static
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
name|testTableWrapperAggregateMetrics
parameter_list|()
throws|throws
name|IOException
block|{
name|String
name|tableName
init|=
literal|"testTableMetrics"
decl_stmt|;
name|MetricsTableWrapperStub
name|tableWrapper
init|=
operator|new
name|MetricsTableWrapperStub
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
name|CompatibilitySingletonFactory
operator|.
name|getInstance
argument_list|(
name|MetricsRegionServerSourceFactory
operator|.
name|class
argument_list|)
operator|.
name|createTable
argument_list|(
name|tableName
argument_list|,
name|tableWrapper
argument_list|)
expr_stmt|;
name|MetricsTableAggregateSource
name|agg
init|=
name|CompatibilitySingletonFactory
operator|.
name|getInstance
argument_list|(
name|MetricsRegionServerSourceFactory
operator|.
name|class
argument_list|)
operator|.
name|getTableAggregate
argument_list|()
decl_stmt|;
name|HELPER
operator|.
name|assertCounter
argument_list|(
literal|"Namespace_default_table_testTableMetrics_metric_readRequestCount"
argument_list|,
literal|10
argument_list|,
name|agg
argument_list|)
expr_stmt|;
name|HELPER
operator|.
name|assertCounter
argument_list|(
literal|"Namespace_default_table_testTableMetrics_metric_writeRequestCount"
argument_list|,
literal|20
argument_list|,
name|agg
argument_list|)
expr_stmt|;
name|HELPER
operator|.
name|assertCounter
argument_list|(
literal|"Namespace_default_table_testTableMetrics_metric_totalRequestCount"
argument_list|,
literal|30
argument_list|,
name|agg
argument_list|)
expr_stmt|;
name|HELPER
operator|.
name|assertGauge
argument_list|(
literal|"Namespace_default_table_testTableMetrics_metric_memstoreSize"
argument_list|,
literal|1000
argument_list|,
name|agg
argument_list|)
expr_stmt|;
name|HELPER
operator|.
name|assertGauge
argument_list|(
literal|"Namespace_default_table_testTableMetrics_metric_storeFileSize"
argument_list|,
literal|2000
argument_list|,
name|agg
argument_list|)
expr_stmt|;
name|HELPER
operator|.
name|assertGauge
argument_list|(
literal|"Namespace_default_table_testTableMetrics_metric_tableSize"
argument_list|,
literal|3000
argument_list|,
name|agg
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

