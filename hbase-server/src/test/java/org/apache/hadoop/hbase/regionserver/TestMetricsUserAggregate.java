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
name|assertFalse
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
name|security
operator|.
name|PrivilegedAction
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
name|security
operator|.
name|User
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
name|LargeTests
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

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|RegionServerTests
operator|.
name|class
block|,
name|LargeTests
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|TestMetricsUserAggregate
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
name|TestMetricsUserAggregate
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
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
specifier|private
name|MetricsRegionServerWrapperStub
name|wrapper
decl_stmt|;
specifier|private
name|MetricsRegionServer
name|rsm
decl_stmt|;
specifier|private
name|MetricsUserAggregateImpl
name|userAgg
decl_stmt|;
specifier|private
name|TableName
name|tableName
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"testUserAggregateMetrics"
argument_list|)
decl_stmt|;
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|classSetUp
parameter_list|()
block|{
name|HELPER
operator|.
name|init
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Before
specifier|public
name|void
name|setUp
parameter_list|()
block|{
name|wrapper
operator|=
operator|new
name|MetricsRegionServerWrapperStub
argument_list|()
expr_stmt|;
name|Configuration
name|conf
init|=
name|HBaseConfiguration
operator|.
name|create
argument_list|()
decl_stmt|;
name|rsm
operator|=
operator|new
name|MetricsRegionServer
argument_list|(
name|wrapper
argument_list|,
name|conf
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|userAgg
operator|=
operator|(
name|MetricsUserAggregateImpl
operator|)
name|rsm
operator|.
name|getMetricsUserAggregate
argument_list|()
expr_stmt|;
block|}
specifier|private
name|void
name|doOperations
parameter_list|()
block|{
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
literal|10
condition|;
name|i
operator|++
control|)
block|{
name|rsm
operator|.
name|updateGet
argument_list|(
name|tableName
argument_list|,
literal|10
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
literal|11
condition|;
name|i
operator|++
control|)
block|{
name|rsm
operator|.
name|updateScanTime
argument_list|(
name|tableName
argument_list|,
literal|11
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
literal|12
condition|;
name|i
operator|++
control|)
block|{
name|rsm
operator|.
name|updatePut
argument_list|(
name|tableName
argument_list|,
literal|12
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
literal|13
condition|;
name|i
operator|++
control|)
block|{
name|rsm
operator|.
name|updateDelete
argument_list|(
name|tableName
argument_list|,
literal|13
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
literal|14
condition|;
name|i
operator|++
control|)
block|{
name|rsm
operator|.
name|updateIncrement
argument_list|(
name|tableName
argument_list|,
literal|14
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
literal|15
condition|;
name|i
operator|++
control|)
block|{
name|rsm
operator|.
name|updateAppend
argument_list|(
name|tableName
argument_list|,
literal|15
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
literal|16
condition|;
name|i
operator|++
control|)
block|{
name|rsm
operator|.
name|updateReplay
argument_list|(
literal|16
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testPerUserOperations
parameter_list|()
block|{
name|Configuration
name|conf
init|=
name|HBaseConfiguration
operator|.
name|create
argument_list|()
decl_stmt|;
name|User
name|userFoo
init|=
name|User
operator|.
name|createUserForTesting
argument_list|(
name|conf
argument_list|,
literal|"FOO"
argument_list|,
operator|new
name|String
index|[
literal|0
index|]
argument_list|)
decl_stmt|;
name|User
name|userBar
init|=
name|User
operator|.
name|createUserForTesting
argument_list|(
name|conf
argument_list|,
literal|"BAR"
argument_list|,
operator|new
name|String
index|[
literal|0
index|]
argument_list|)
decl_stmt|;
name|userFoo
operator|.
name|getUGI
argument_list|()
operator|.
name|doAs
argument_list|(
operator|new
name|PrivilegedAction
argument_list|<
name|Void
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Void
name|run
parameter_list|()
block|{
name|doOperations
argument_list|()
expr_stmt|;
return|return
literal|null
return|;
block|}
block|}
argument_list|)
expr_stmt|;
name|userBar
operator|.
name|getUGI
argument_list|()
operator|.
name|doAs
argument_list|(
operator|new
name|PrivilegedAction
argument_list|<
name|Void
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Void
name|run
parameter_list|()
block|{
name|doOperations
argument_list|()
expr_stmt|;
return|return
literal|null
return|;
block|}
block|}
argument_list|)
expr_stmt|;
name|HELPER
operator|.
name|assertCounter
argument_list|(
literal|"userfoometricgetnumops"
argument_list|,
literal|10
argument_list|,
name|userAgg
operator|.
name|getSource
argument_list|()
argument_list|)
expr_stmt|;
name|HELPER
operator|.
name|assertCounter
argument_list|(
literal|"userfoometricscantimenumops"
argument_list|,
literal|11
argument_list|,
name|userAgg
operator|.
name|getSource
argument_list|()
argument_list|)
expr_stmt|;
name|HELPER
operator|.
name|assertCounter
argument_list|(
literal|"userfoometricputnumops"
argument_list|,
literal|12
argument_list|,
name|userAgg
operator|.
name|getSource
argument_list|()
argument_list|)
expr_stmt|;
name|HELPER
operator|.
name|assertCounter
argument_list|(
literal|"userfoometricdeletenumops"
argument_list|,
literal|13
argument_list|,
name|userAgg
operator|.
name|getSource
argument_list|()
argument_list|)
expr_stmt|;
name|HELPER
operator|.
name|assertCounter
argument_list|(
literal|"userfoometricincrementnumops"
argument_list|,
literal|14
argument_list|,
name|userAgg
operator|.
name|getSource
argument_list|()
argument_list|)
expr_stmt|;
name|HELPER
operator|.
name|assertCounter
argument_list|(
literal|"userfoometricappendnumops"
argument_list|,
literal|15
argument_list|,
name|userAgg
operator|.
name|getSource
argument_list|()
argument_list|)
expr_stmt|;
name|HELPER
operator|.
name|assertCounter
argument_list|(
literal|"userfoometricreplaynumops"
argument_list|,
literal|16
argument_list|,
name|userAgg
operator|.
name|getSource
argument_list|()
argument_list|)
expr_stmt|;
name|HELPER
operator|.
name|assertCounter
argument_list|(
literal|"userbarmetricgetnumops"
argument_list|,
literal|10
argument_list|,
name|userAgg
operator|.
name|getSource
argument_list|()
argument_list|)
expr_stmt|;
name|HELPER
operator|.
name|assertCounter
argument_list|(
literal|"userbarmetricscantimenumops"
argument_list|,
literal|11
argument_list|,
name|userAgg
operator|.
name|getSource
argument_list|()
argument_list|)
expr_stmt|;
name|HELPER
operator|.
name|assertCounter
argument_list|(
literal|"userbarmetricputnumops"
argument_list|,
literal|12
argument_list|,
name|userAgg
operator|.
name|getSource
argument_list|()
argument_list|)
expr_stmt|;
name|HELPER
operator|.
name|assertCounter
argument_list|(
literal|"userbarmetricdeletenumops"
argument_list|,
literal|13
argument_list|,
name|userAgg
operator|.
name|getSource
argument_list|()
argument_list|)
expr_stmt|;
name|HELPER
operator|.
name|assertCounter
argument_list|(
literal|"userbarmetricincrementnumops"
argument_list|,
literal|14
argument_list|,
name|userAgg
operator|.
name|getSource
argument_list|()
argument_list|)
expr_stmt|;
name|HELPER
operator|.
name|assertCounter
argument_list|(
literal|"userbarmetricappendnumops"
argument_list|,
literal|15
argument_list|,
name|userAgg
operator|.
name|getSource
argument_list|()
argument_list|)
expr_stmt|;
name|HELPER
operator|.
name|assertCounter
argument_list|(
literal|"userbarmetricreplaynumops"
argument_list|,
literal|16
argument_list|,
name|userAgg
operator|.
name|getSource
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testLossyCountingOfUserMetrics
parameter_list|()
block|{
name|Configuration
name|conf
init|=
name|HBaseConfiguration
operator|.
name|create
argument_list|()
decl_stmt|;
name|int
name|noOfUsers
init|=
literal|10000
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|1
init|;
name|i
operator|<=
name|noOfUsers
condition|;
name|i
operator|++
control|)
block|{
name|User
operator|.
name|createUserForTesting
argument_list|(
name|conf
argument_list|,
literal|"FOO"
operator|+
name|i
argument_list|,
operator|new
name|String
index|[
literal|0
index|]
argument_list|)
operator|.
name|getUGI
argument_list|()
operator|.
name|doAs
argument_list|(
operator|new
name|PrivilegedAction
argument_list|<
name|Void
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Void
name|run
parameter_list|()
block|{
name|rsm
operator|.
name|updateGet
argument_list|(
name|tableName
argument_list|,
literal|10
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
name|assertTrue
argument_list|(
operator|(
operator|(
name|MetricsUserAggregateSourceImpl
operator|)
name|userAgg
operator|.
name|getSource
argument_list|()
operator|)
operator|.
name|getUserSources
argument_list|()
operator|.
name|size
argument_list|()
operator|<=
operator|(
name|noOfUsers
operator|/
literal|10
operator|)
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|1
init|;
name|i
operator|<=
name|noOfUsers
operator|/
literal|10
condition|;
name|i
operator|++
control|)
block|{
name|assertFalse
argument_list|(
name|HELPER
operator|.
name|checkCounterExists
argument_list|(
literal|"userfoo"
operator|+
name|i
operator|+
literal|"metricgetnumops"
argument_list|,
name|userAgg
operator|.
name|getSource
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|HELPER
operator|.
name|assertCounter
argument_list|(
literal|"userfoo"
operator|+
name|noOfUsers
operator|+
literal|"metricgetnumops"
argument_list|,
literal|1
argument_list|,
name|userAgg
operator|.
name|getSource
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

