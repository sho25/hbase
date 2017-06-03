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
name|io
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
name|SmallTests
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
name|TestMetricsIO
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
name|testMetrics
parameter_list|()
block|{
name|MetricsIO
name|metrics
init|=
operator|new
name|MetricsIO
argument_list|(
operator|new
name|MetricsIOWrapper
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|long
name|getChecksumFailures
parameter_list|()
block|{
return|return
literal|40
return|;
block|}
block|}
argument_list|)
decl_stmt|;
name|metrics
operator|.
name|updateFsReadTime
argument_list|(
literal|100
argument_list|)
expr_stmt|;
name|metrics
operator|.
name|updateFsReadTime
argument_list|(
literal|200
argument_list|)
expr_stmt|;
name|metrics
operator|.
name|updateFsPreadTime
argument_list|(
literal|300
argument_list|)
expr_stmt|;
name|metrics
operator|.
name|updateFsWriteTime
argument_list|(
literal|400
argument_list|)
expr_stmt|;
name|metrics
operator|.
name|updateFsWriteTime
argument_list|(
literal|500
argument_list|)
expr_stmt|;
name|metrics
operator|.
name|updateFsWriteTime
argument_list|(
literal|600
argument_list|)
expr_stmt|;
name|HELPER
operator|.
name|assertCounter
argument_list|(
literal|"fsChecksumFailureCount"
argument_list|,
literal|40
argument_list|,
name|metrics
operator|.
name|getMetricsSource
argument_list|()
argument_list|)
expr_stmt|;
name|HELPER
operator|.
name|assertCounter
argument_list|(
literal|"fsReadTime_numOps"
argument_list|,
literal|2
argument_list|,
name|metrics
operator|.
name|getMetricsSource
argument_list|()
argument_list|)
expr_stmt|;
name|HELPER
operator|.
name|assertCounter
argument_list|(
literal|"fsPReadTime_numOps"
argument_list|,
literal|1
argument_list|,
name|metrics
operator|.
name|getMetricsSource
argument_list|()
argument_list|)
expr_stmt|;
name|HELPER
operator|.
name|assertCounter
argument_list|(
literal|"fsWriteTime_numOps"
argument_list|,
literal|3
argument_list|,
name|metrics
operator|.
name|getMetricsSource
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

