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
name|assertTrue
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
name|testclassification
operator|.
name|MetricsTests
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

begin_comment
comment|/**  *  Test for MetricsTableSourceImpl  */
end_comment

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|MetricsTests
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
name|TestMetricsTableSourceImpl
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
name|TestMetricsTableSourceImpl
operator|.
name|class
argument_list|)
decl_stmt|;
annotation|@
name|SuppressWarnings
argument_list|(
literal|"SelfComparison"
argument_list|)
annotation|@
name|Test
specifier|public
name|void
name|testCompareToHashCode
parameter_list|()
throws|throws
name|Exception
block|{
name|MetricsRegionServerSourceFactory
name|metricsFact
init|=
name|CompatibilitySingletonFactory
operator|.
name|getInstance
argument_list|(
name|MetricsRegionServerSourceFactory
operator|.
name|class
argument_list|)
decl_stmt|;
name|MetricsTableSource
name|one
init|=
name|metricsFact
operator|.
name|createTable
argument_list|(
literal|"ONETABLE"
argument_list|,
operator|new
name|MetricsTableWrapperStub
argument_list|(
literal|"ONETABLE"
argument_list|)
argument_list|)
decl_stmt|;
name|MetricsTableSource
name|oneClone
init|=
name|metricsFact
operator|.
name|createTable
argument_list|(
literal|"ONETABLE"
argument_list|,
operator|new
name|MetricsTableWrapperStub
argument_list|(
literal|"ONETABLE"
argument_list|)
argument_list|)
decl_stmt|;
name|MetricsTableSource
name|two
init|=
name|metricsFact
operator|.
name|createTable
argument_list|(
literal|"TWOTABLE"
argument_list|,
operator|new
name|MetricsTableWrapperStub
argument_list|(
literal|"TWOTABLE"
argument_list|)
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|one
operator|.
name|compareTo
argument_list|(
name|oneClone
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|one
operator|.
name|hashCode
argument_list|()
argument_list|,
name|oneClone
operator|.
name|hashCode
argument_list|()
argument_list|)
expr_stmt|;
name|assertNotEquals
argument_list|(
name|one
argument_list|,
name|two
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|one
operator|.
name|compareTo
argument_list|(
name|two
argument_list|)
operator|!=
literal|0
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|two
operator|.
name|compareTo
argument_list|(
name|one
argument_list|)
operator|!=
literal|0
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|two
operator|.
name|compareTo
argument_list|(
name|one
argument_list|)
operator|!=
name|one
operator|.
name|compareTo
argument_list|(
name|two
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|two
operator|.
name|compareTo
argument_list|(
name|two
argument_list|)
operator|==
literal|0
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
argument_list|(
name|expected
operator|=
name|RuntimeException
operator|.
name|class
argument_list|)
specifier|public
name|void
name|testNoGetTableMetricsSourceImpl
parameter_list|()
block|{
comment|// This should throw an exception because MetricsTableSourceImpl should only
comment|// be created by a factory.
name|CompatibilitySingletonFactory
operator|.
name|getInstance
argument_list|(
name|MetricsTableSourceImpl
operator|.
name|class
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testGetTableMetrics
parameter_list|()
block|{
name|MetricsTableSource
name|oneTbl
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
name|createTable
argument_list|(
literal|"ONETABLE"
argument_list|,
operator|new
name|MetricsTableWrapperStub
argument_list|(
literal|"ONETABLE"
argument_list|)
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"ONETABLE"
argument_list|,
name|oneTbl
operator|.
name|getTableName
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

