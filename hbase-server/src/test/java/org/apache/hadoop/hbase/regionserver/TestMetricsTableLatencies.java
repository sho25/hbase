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
name|TestMetricsTableLatencies
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
name|TestMetricsTableLatencies
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
name|TableName
name|tn1
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"table1"
argument_list|)
decl_stmt|;
name|TableName
name|tn2
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"table2"
argument_list|)
decl_stmt|;
name|MetricsTableLatencies
name|latencies
init|=
name|CompatibilitySingletonFactory
operator|.
name|getInstance
argument_list|(
name|MetricsTableLatencies
operator|.
name|class
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
literal|"'latencies' is actually "
operator|+
name|latencies
operator|.
name|getClass
argument_list|()
argument_list|,
name|latencies
operator|instanceof
name|MetricsTableLatenciesImpl
argument_list|)
expr_stmt|;
name|MetricsTableLatenciesImpl
name|latenciesImpl
init|=
operator|(
name|MetricsTableLatenciesImpl
operator|)
name|latencies
decl_stmt|;
name|RegionServerTableMetrics
name|tableMetrics
init|=
operator|new
name|RegionServerTableMetrics
argument_list|()
decl_stmt|;
comment|// Metrics to each table should be disjoint
comment|// N.B. each call to assertGauge removes all previously acquired metrics so we have to
comment|//   make the metrics call and then immediately verify it. Trying to do multiple metrics
comment|//   updates followed by multiple verifications will fail on the 2nd verification (as the
comment|//   first verification cleaned the data structures in MetricsAssertHelperImpl).
name|tableMetrics
operator|.
name|updateGet
argument_list|(
name|tn1
argument_list|,
literal|500L
argument_list|)
expr_stmt|;
name|HELPER
operator|.
name|assertGauge
argument_list|(
name|MetricsTableLatenciesImpl
operator|.
name|qualifyMetricsName
argument_list|(
name|tn1
argument_list|,
name|MetricsTableLatencies
operator|.
name|GET_TIME
operator|+
literal|"_"
operator|+
literal|"999th_percentile"
argument_list|)
argument_list|,
literal|500L
argument_list|,
name|latenciesImpl
argument_list|)
expr_stmt|;
name|tableMetrics
operator|.
name|updatePut
argument_list|(
name|tn1
argument_list|,
literal|50L
argument_list|)
expr_stmt|;
name|HELPER
operator|.
name|assertGauge
argument_list|(
name|MetricsTableLatenciesImpl
operator|.
name|qualifyMetricsName
argument_list|(
name|tn1
argument_list|,
name|MetricsTableLatencies
operator|.
name|PUT_TIME
operator|+
literal|"_"
operator|+
literal|"99th_percentile"
argument_list|)
argument_list|,
literal|50L
argument_list|,
name|latenciesImpl
argument_list|)
expr_stmt|;
name|tableMetrics
operator|.
name|updateGet
argument_list|(
name|tn2
argument_list|,
literal|300L
argument_list|)
expr_stmt|;
name|HELPER
operator|.
name|assertGauge
argument_list|(
name|MetricsTableLatenciesImpl
operator|.
name|qualifyMetricsName
argument_list|(
name|tn2
argument_list|,
name|MetricsTableLatencies
operator|.
name|GET_TIME
operator|+
literal|"_"
operator|+
literal|"999th_percentile"
argument_list|)
argument_list|,
literal|300L
argument_list|,
name|latenciesImpl
argument_list|)
expr_stmt|;
name|tableMetrics
operator|.
name|updatePut
argument_list|(
name|tn2
argument_list|,
literal|75L
argument_list|)
expr_stmt|;
name|HELPER
operator|.
name|assertGauge
argument_list|(
name|MetricsTableLatenciesImpl
operator|.
name|qualifyMetricsName
argument_list|(
name|tn2
argument_list|,
name|MetricsTableLatencies
operator|.
name|PUT_TIME
operator|+
literal|"_"
operator|+
literal|"99th_percentile"
argument_list|)
argument_list|,
literal|75L
argument_list|,
name|latenciesImpl
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

