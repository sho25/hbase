begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one or more  * contributor license agreements.  See the NOTICE file distributed with  * this work for additional information regarding copyright ownership.  * The ASF licenses this file to you under the Apache License, Version 2.0  * (the "License"); you may not use this file except in compliance with  * the License.  You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|quotas
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
name|assertNotNull
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|mockito
operator|.
name|Matchers
operator|.
name|any
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|mockito
operator|.
name|Mockito
operator|.
name|doAnswer
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|mockito
operator|.
name|Mockito
operator|.
name|mock
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|mockito
operator|.
name|Mockito
operator|.
name|when
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
name|Collection
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
name|Iterator
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
name|Map
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
name|TimeUnit
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
name|client
operator|.
name|RegionInfo
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
name|regionserver
operator|.
name|HRegionServer
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
name|regionserver
operator|.
name|Region
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
name|regionserver
operator|.
name|Store
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

begin_import
import|import
name|org
operator|.
name|mockito
operator|.
name|invocation
operator|.
name|InvocationOnMock
import|;
end_import

begin_import
import|import
name|org
operator|.
name|mockito
operator|.
name|stubbing
operator|.
name|Answer
import|;
end_import

begin_comment
comment|/**  * Test class for {@link FileSystemUtilizationChore}.  */
end_comment

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
name|TestFileSystemUtilizationChore
block|{
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
annotation|@
name|Test
specifier|public
name|void
name|testNoOnlineRegions
parameter_list|()
block|{
comment|// One region with a store size of one.
specifier|final
name|List
argument_list|<
name|Long
argument_list|>
name|regionSizes
init|=
name|Collections
operator|.
name|emptyList
argument_list|()
decl_stmt|;
specifier|final
name|Configuration
name|conf
init|=
name|getDefaultHBaseConfiguration
argument_list|()
decl_stmt|;
specifier|final
name|HRegionServer
name|rs
init|=
name|mockRegionServer
argument_list|(
name|conf
argument_list|)
decl_stmt|;
specifier|final
name|FileSystemUtilizationChore
name|chore
init|=
operator|new
name|FileSystemUtilizationChore
argument_list|(
name|rs
argument_list|)
decl_stmt|;
name|doAnswer
argument_list|(
operator|new
name|ExpectedRegionSizeSummationAnswer
argument_list|(
name|sum
argument_list|(
name|regionSizes
argument_list|)
argument_list|)
argument_list|)
operator|.
name|when
argument_list|(
name|rs
argument_list|)
operator|.
name|reportRegionSizesForQuotas
argument_list|(
operator|(
name|Map
argument_list|<
name|RegionInfo
argument_list|,
name|Long
argument_list|>
operator|)
name|any
argument_list|(
name|Map
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
specifier|final
name|Region
name|region
init|=
name|mockRegionWithSize
argument_list|(
name|regionSizes
argument_list|)
decl_stmt|;
name|when
argument_list|(
name|rs
operator|.
name|getRegions
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|region
argument_list|)
argument_list|)
expr_stmt|;
name|chore
operator|.
name|chore
argument_list|()
expr_stmt|;
block|}
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
annotation|@
name|Test
specifier|public
name|void
name|testRegionSizes
parameter_list|()
block|{
comment|// One region with a store size of one.
specifier|final
name|List
argument_list|<
name|Long
argument_list|>
name|regionSizes
init|=
name|Arrays
operator|.
name|asList
argument_list|(
literal|1024L
argument_list|)
decl_stmt|;
specifier|final
name|Configuration
name|conf
init|=
name|getDefaultHBaseConfiguration
argument_list|()
decl_stmt|;
specifier|final
name|HRegionServer
name|rs
init|=
name|mockRegionServer
argument_list|(
name|conf
argument_list|)
decl_stmt|;
specifier|final
name|FileSystemUtilizationChore
name|chore
init|=
operator|new
name|FileSystemUtilizationChore
argument_list|(
name|rs
argument_list|)
decl_stmt|;
name|doAnswer
argument_list|(
operator|new
name|ExpectedRegionSizeSummationAnswer
argument_list|(
name|sum
argument_list|(
name|regionSizes
argument_list|)
argument_list|)
argument_list|)
operator|.
name|when
argument_list|(
name|rs
argument_list|)
operator|.
name|reportRegionSizesForQuotas
argument_list|(
operator|(
name|Map
argument_list|<
name|RegionInfo
argument_list|,
name|Long
argument_list|>
operator|)
name|any
argument_list|(
name|Map
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
specifier|final
name|Region
name|region
init|=
name|mockRegionWithSize
argument_list|(
name|regionSizes
argument_list|)
decl_stmt|;
name|when
argument_list|(
name|rs
operator|.
name|getRegions
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|region
argument_list|)
argument_list|)
expr_stmt|;
name|chore
operator|.
name|chore
argument_list|()
expr_stmt|;
block|}
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
annotation|@
name|Test
specifier|public
name|void
name|testMultipleRegionSizes
parameter_list|()
block|{
specifier|final
name|Configuration
name|conf
init|=
name|getDefaultHBaseConfiguration
argument_list|()
decl_stmt|;
specifier|final
name|HRegionServer
name|rs
init|=
name|mockRegionServer
argument_list|(
name|conf
argument_list|)
decl_stmt|;
comment|// Three regions with multiple store sizes
specifier|final
name|List
argument_list|<
name|Long
argument_list|>
name|r1Sizes
init|=
name|Arrays
operator|.
name|asList
argument_list|(
literal|1024L
argument_list|,
literal|2048L
argument_list|)
decl_stmt|;
specifier|final
name|long
name|r1Sum
init|=
name|sum
argument_list|(
name|r1Sizes
argument_list|)
decl_stmt|;
specifier|final
name|List
argument_list|<
name|Long
argument_list|>
name|r2Sizes
init|=
name|Arrays
operator|.
name|asList
argument_list|(
literal|1024L
operator|*
literal|1024L
argument_list|)
decl_stmt|;
specifier|final
name|long
name|r2Sum
init|=
name|sum
argument_list|(
name|r2Sizes
argument_list|)
decl_stmt|;
specifier|final
name|List
argument_list|<
name|Long
argument_list|>
name|r3Sizes
init|=
name|Arrays
operator|.
name|asList
argument_list|(
literal|10L
operator|*
literal|1024L
operator|*
literal|1024L
argument_list|)
decl_stmt|;
specifier|final
name|long
name|r3Sum
init|=
name|sum
argument_list|(
name|r3Sizes
argument_list|)
decl_stmt|;
specifier|final
name|FileSystemUtilizationChore
name|chore
init|=
operator|new
name|FileSystemUtilizationChore
argument_list|(
name|rs
argument_list|)
decl_stmt|;
name|doAnswer
argument_list|(
operator|new
name|ExpectedRegionSizeSummationAnswer
argument_list|(
name|sum
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|r1Sum
argument_list|,
name|r2Sum
argument_list|,
name|r3Sum
argument_list|)
argument_list|)
argument_list|)
argument_list|)
operator|.
name|when
argument_list|(
name|rs
argument_list|)
operator|.
name|reportRegionSizesForQuotas
argument_list|(
operator|(
name|Map
argument_list|<
name|RegionInfo
argument_list|,
name|Long
argument_list|>
operator|)
name|any
argument_list|(
name|Map
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
specifier|final
name|Region
name|r1
init|=
name|mockRegionWithSize
argument_list|(
name|r1Sizes
argument_list|)
decl_stmt|;
specifier|final
name|Region
name|r2
init|=
name|mockRegionWithSize
argument_list|(
name|r2Sizes
argument_list|)
decl_stmt|;
specifier|final
name|Region
name|r3
init|=
name|mockRegionWithSize
argument_list|(
name|r3Sizes
argument_list|)
decl_stmt|;
name|when
argument_list|(
name|rs
operator|.
name|getRegions
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|r1
argument_list|,
name|r2
argument_list|,
name|r3
argument_list|)
argument_list|)
expr_stmt|;
name|chore
operator|.
name|chore
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testDefaultConfigurationProperties
parameter_list|()
block|{
specifier|final
name|Configuration
name|conf
init|=
name|getDefaultHBaseConfiguration
argument_list|()
decl_stmt|;
specifier|final
name|HRegionServer
name|rs
init|=
name|mockRegionServer
argument_list|(
name|conf
argument_list|)
decl_stmt|;
specifier|final
name|FileSystemUtilizationChore
name|chore
init|=
operator|new
name|FileSystemUtilizationChore
argument_list|(
name|rs
argument_list|)
decl_stmt|;
comment|// Verify that the expected default values are actually represented.
name|assertEquals
argument_list|(
name|FileSystemUtilizationChore
operator|.
name|FS_UTILIZATION_CHORE_PERIOD_DEFAULT
argument_list|,
name|chore
operator|.
name|getPeriod
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|FileSystemUtilizationChore
operator|.
name|FS_UTILIZATION_CHORE_DELAY_DEFAULT
argument_list|,
name|chore
operator|.
name|getInitialDelay
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|TimeUnit
operator|.
name|valueOf
argument_list|(
name|FileSystemUtilizationChore
operator|.
name|FS_UTILIZATION_CHORE_TIMEUNIT_DEFAULT
argument_list|)
argument_list|,
name|chore
operator|.
name|getTimeUnit
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testNonDefaultConfigurationProperties
parameter_list|()
block|{
specifier|final
name|Configuration
name|conf
init|=
name|getDefaultHBaseConfiguration
argument_list|()
decl_stmt|;
comment|// Override the default values
specifier|final
name|int
name|period
init|=
literal|60
operator|*
literal|10
decl_stmt|;
specifier|final
name|long
name|delay
init|=
literal|30L
decl_stmt|;
specifier|final
name|TimeUnit
name|timeUnit
init|=
name|TimeUnit
operator|.
name|SECONDS
decl_stmt|;
name|conf
operator|.
name|setInt
argument_list|(
name|FileSystemUtilizationChore
operator|.
name|FS_UTILIZATION_CHORE_PERIOD_KEY
argument_list|,
name|period
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setLong
argument_list|(
name|FileSystemUtilizationChore
operator|.
name|FS_UTILIZATION_CHORE_DELAY_KEY
argument_list|,
name|delay
argument_list|)
expr_stmt|;
name|conf
operator|.
name|set
argument_list|(
name|FileSystemUtilizationChore
operator|.
name|FS_UTILIZATION_CHORE_TIMEUNIT_KEY
argument_list|,
name|timeUnit
operator|.
name|name
argument_list|()
argument_list|)
expr_stmt|;
comment|// Verify that the chore reports these non-default values
specifier|final
name|HRegionServer
name|rs
init|=
name|mockRegionServer
argument_list|(
name|conf
argument_list|)
decl_stmt|;
specifier|final
name|FileSystemUtilizationChore
name|chore
init|=
operator|new
name|FileSystemUtilizationChore
argument_list|(
name|rs
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|period
argument_list|,
name|chore
operator|.
name|getPeriod
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|delay
argument_list|,
name|chore
operator|.
name|getInitialDelay
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|timeUnit
argument_list|,
name|chore
operator|.
name|getTimeUnit
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
annotation|@
name|Test
specifier|public
name|void
name|testProcessingLeftoverRegions
parameter_list|()
block|{
specifier|final
name|Configuration
name|conf
init|=
name|getDefaultHBaseConfiguration
argument_list|()
decl_stmt|;
specifier|final
name|HRegionServer
name|rs
init|=
name|mockRegionServer
argument_list|(
name|conf
argument_list|)
decl_stmt|;
comment|// Some leftover regions from a previous chore()
specifier|final
name|List
argument_list|<
name|Long
argument_list|>
name|leftover1Sizes
init|=
name|Arrays
operator|.
name|asList
argument_list|(
literal|1024L
argument_list|,
literal|4096L
argument_list|)
decl_stmt|;
specifier|final
name|long
name|leftover1Sum
init|=
name|sum
argument_list|(
name|leftover1Sizes
argument_list|)
decl_stmt|;
specifier|final
name|List
argument_list|<
name|Long
argument_list|>
name|leftover2Sizes
init|=
name|Arrays
operator|.
name|asList
argument_list|(
literal|2048L
argument_list|)
decl_stmt|;
specifier|final
name|long
name|leftover2Sum
init|=
name|sum
argument_list|(
name|leftover2Sizes
argument_list|)
decl_stmt|;
specifier|final
name|Region
name|lr1
init|=
name|mockRegionWithSize
argument_list|(
name|leftover1Sizes
argument_list|)
decl_stmt|;
specifier|final
name|Region
name|lr2
init|=
name|mockRegionWithSize
argument_list|(
name|leftover2Sizes
argument_list|)
decl_stmt|;
specifier|final
name|FileSystemUtilizationChore
name|chore
init|=
operator|new
name|FileSystemUtilizationChore
argument_list|(
name|rs
argument_list|)
block|{
annotation|@
name|Override
name|Iterator
argument_list|<
name|Region
argument_list|>
name|getLeftoverRegions
parameter_list|()
block|{
return|return
name|Arrays
operator|.
name|asList
argument_list|(
name|lr1
argument_list|,
name|lr2
argument_list|)
operator|.
name|iterator
argument_list|()
return|;
block|}
block|}
decl_stmt|;
name|doAnswer
argument_list|(
operator|new
name|ExpectedRegionSizeSummationAnswer
argument_list|(
name|sum
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|leftover1Sum
argument_list|,
name|leftover2Sum
argument_list|)
argument_list|)
argument_list|)
argument_list|)
operator|.
name|when
argument_list|(
name|rs
argument_list|)
operator|.
name|reportRegionSizesForQuotas
argument_list|(
operator|(
name|Map
argument_list|<
name|RegionInfo
argument_list|,
name|Long
argument_list|>
operator|)
name|any
argument_list|(
name|Map
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
comment|// We shouldn't compute all of these region sizes, just the leftovers
specifier|final
name|Region
name|r1
init|=
name|mockRegionWithSize
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
literal|1024L
argument_list|,
literal|2048L
argument_list|)
argument_list|)
decl_stmt|;
specifier|final
name|Region
name|r2
init|=
name|mockRegionWithSize
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
literal|1024L
operator|*
literal|1024L
argument_list|)
argument_list|)
decl_stmt|;
specifier|final
name|Region
name|r3
init|=
name|mockRegionWithSize
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
literal|10L
operator|*
literal|1024L
operator|*
literal|1024L
argument_list|)
argument_list|)
decl_stmt|;
name|when
argument_list|(
name|rs
operator|.
name|getRegions
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|r1
argument_list|,
name|r2
argument_list|,
name|r3
argument_list|,
name|lr1
argument_list|,
name|lr2
argument_list|)
argument_list|)
expr_stmt|;
name|chore
operator|.
name|chore
argument_list|()
expr_stmt|;
block|}
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
annotation|@
name|Test
specifier|public
name|void
name|testProcessingNowOfflineLeftoversAreIgnored
parameter_list|()
block|{
specifier|final
name|Configuration
name|conf
init|=
name|getDefaultHBaseConfiguration
argument_list|()
decl_stmt|;
specifier|final
name|HRegionServer
name|rs
init|=
name|mockRegionServer
argument_list|(
name|conf
argument_list|)
decl_stmt|;
comment|// Some leftover regions from a previous chore()
specifier|final
name|List
argument_list|<
name|Long
argument_list|>
name|leftover1Sizes
init|=
name|Arrays
operator|.
name|asList
argument_list|(
literal|1024L
argument_list|,
literal|4096L
argument_list|)
decl_stmt|;
specifier|final
name|long
name|leftover1Sum
init|=
name|sum
argument_list|(
name|leftover1Sizes
argument_list|)
decl_stmt|;
specifier|final
name|List
argument_list|<
name|Long
argument_list|>
name|leftover2Sizes
init|=
name|Arrays
operator|.
name|asList
argument_list|(
literal|2048L
argument_list|)
decl_stmt|;
specifier|final
name|Region
name|lr1
init|=
name|mockRegionWithSize
argument_list|(
name|leftover1Sizes
argument_list|)
decl_stmt|;
specifier|final
name|Region
name|lr2
init|=
name|mockRegionWithSize
argument_list|(
name|leftover2Sizes
argument_list|)
decl_stmt|;
specifier|final
name|FileSystemUtilizationChore
name|chore
init|=
operator|new
name|FileSystemUtilizationChore
argument_list|(
name|rs
argument_list|)
block|{
annotation|@
name|Override
name|Iterator
argument_list|<
name|Region
argument_list|>
name|getLeftoverRegions
parameter_list|()
block|{
return|return
name|Arrays
operator|.
name|asList
argument_list|(
name|lr1
argument_list|,
name|lr2
argument_list|)
operator|.
name|iterator
argument_list|()
return|;
block|}
block|}
decl_stmt|;
name|doAnswer
argument_list|(
operator|new
name|ExpectedRegionSizeSummationAnswer
argument_list|(
name|sum
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|leftover1Sum
argument_list|)
argument_list|)
argument_list|)
argument_list|)
operator|.
name|when
argument_list|(
name|rs
argument_list|)
operator|.
name|reportRegionSizesForQuotas
argument_list|(
operator|(
name|Map
argument_list|<
name|RegionInfo
argument_list|,
name|Long
argument_list|>
operator|)
name|any
argument_list|(
name|Map
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
comment|// We shouldn't compute all of these region sizes, just the leftovers
specifier|final
name|Region
name|r1
init|=
name|mockRegionWithSize
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
literal|1024L
argument_list|,
literal|2048L
argument_list|)
argument_list|)
decl_stmt|;
specifier|final
name|Region
name|r2
init|=
name|mockRegionWithSize
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
literal|1024L
operator|*
literal|1024L
argument_list|)
argument_list|)
decl_stmt|;
specifier|final
name|Region
name|r3
init|=
name|mockRegionWithSize
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
literal|10L
operator|*
literal|1024L
operator|*
literal|1024L
argument_list|)
argument_list|)
decl_stmt|;
comment|// lr2 is no longer online, so it should be ignored
name|when
argument_list|(
name|rs
operator|.
name|getRegions
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|r1
argument_list|,
name|r2
argument_list|,
name|r3
argument_list|,
name|lr1
argument_list|)
argument_list|)
expr_stmt|;
name|chore
operator|.
name|chore
argument_list|()
expr_stmt|;
block|}
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
annotation|@
name|Test
specifier|public
name|void
name|testIgnoreSplitParents
parameter_list|()
block|{
specifier|final
name|Configuration
name|conf
init|=
name|getDefaultHBaseConfiguration
argument_list|()
decl_stmt|;
specifier|final
name|HRegionServer
name|rs
init|=
name|mockRegionServer
argument_list|(
name|conf
argument_list|)
decl_stmt|;
comment|// Three regions with multiple store sizes
specifier|final
name|List
argument_list|<
name|Long
argument_list|>
name|r1Sizes
init|=
name|Arrays
operator|.
name|asList
argument_list|(
literal|1024L
argument_list|,
literal|2048L
argument_list|)
decl_stmt|;
specifier|final
name|long
name|r1Sum
init|=
name|sum
argument_list|(
name|r1Sizes
argument_list|)
decl_stmt|;
specifier|final
name|List
argument_list|<
name|Long
argument_list|>
name|r2Sizes
init|=
name|Arrays
operator|.
name|asList
argument_list|(
literal|1024L
operator|*
literal|1024L
argument_list|)
decl_stmt|;
specifier|final
name|FileSystemUtilizationChore
name|chore
init|=
operator|new
name|FileSystemUtilizationChore
argument_list|(
name|rs
argument_list|)
decl_stmt|;
name|doAnswer
argument_list|(
operator|new
name|ExpectedRegionSizeSummationAnswer
argument_list|(
name|sum
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|r1Sum
argument_list|)
argument_list|)
argument_list|)
argument_list|)
operator|.
name|when
argument_list|(
name|rs
argument_list|)
operator|.
name|reportRegionSizesForQuotas
argument_list|(
operator|(
name|Map
argument_list|<
name|RegionInfo
argument_list|,
name|Long
argument_list|>
operator|)
name|any
argument_list|(
name|Map
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
specifier|final
name|Region
name|r1
init|=
name|mockRegionWithSize
argument_list|(
name|r1Sizes
argument_list|)
decl_stmt|;
specifier|final
name|Region
name|r2
init|=
name|mockSplitParentRegionWithSize
argument_list|(
name|r2Sizes
argument_list|)
decl_stmt|;
name|when
argument_list|(
name|rs
operator|.
name|getRegions
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|r1
argument_list|,
name|r2
argument_list|)
argument_list|)
expr_stmt|;
name|chore
operator|.
name|chore
argument_list|()
expr_stmt|;
block|}
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
annotation|@
name|Test
specifier|public
name|void
name|testIgnoreRegionReplicas
parameter_list|()
block|{
specifier|final
name|Configuration
name|conf
init|=
name|getDefaultHBaseConfiguration
argument_list|()
decl_stmt|;
specifier|final
name|HRegionServer
name|rs
init|=
name|mockRegionServer
argument_list|(
name|conf
argument_list|)
decl_stmt|;
comment|// Two regions with multiple store sizes
specifier|final
name|List
argument_list|<
name|Long
argument_list|>
name|r1Sizes
init|=
name|Arrays
operator|.
name|asList
argument_list|(
literal|1024L
argument_list|,
literal|2048L
argument_list|)
decl_stmt|;
specifier|final
name|long
name|r1Sum
init|=
name|sum
argument_list|(
name|r1Sizes
argument_list|)
decl_stmt|;
specifier|final
name|List
argument_list|<
name|Long
argument_list|>
name|r2Sizes
init|=
name|Arrays
operator|.
name|asList
argument_list|(
literal|1024L
operator|*
literal|1024L
argument_list|)
decl_stmt|;
specifier|final
name|FileSystemUtilizationChore
name|chore
init|=
operator|new
name|FileSystemUtilizationChore
argument_list|(
name|rs
argument_list|)
decl_stmt|;
name|doAnswer
argument_list|(
operator|new
name|ExpectedRegionSizeSummationAnswer
argument_list|(
name|r1Sum
argument_list|)
argument_list|)
operator|.
name|when
argument_list|(
name|rs
argument_list|)
operator|.
name|reportRegionSizesForQuotas
argument_list|(
operator|(
name|Map
argument_list|<
name|RegionInfo
argument_list|,
name|Long
argument_list|>
operator|)
name|any
argument_list|(
name|Map
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
specifier|final
name|Region
name|r1
init|=
name|mockRegionWithSize
argument_list|(
name|r1Sizes
argument_list|)
decl_stmt|;
specifier|final
name|Region
name|r2
init|=
name|mockRegionReplicaWithSize
argument_list|(
name|r2Sizes
argument_list|)
decl_stmt|;
name|when
argument_list|(
name|rs
operator|.
name|getRegions
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|r1
argument_list|,
name|r2
argument_list|)
argument_list|)
expr_stmt|;
name|chore
operator|.
name|chore
argument_list|()
expr_stmt|;
block|}
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
annotation|@
name|Test
specifier|public
name|void
name|testNonHFilesAreIgnored
parameter_list|()
block|{
specifier|final
name|Configuration
name|conf
init|=
name|getDefaultHBaseConfiguration
argument_list|()
decl_stmt|;
specifier|final
name|HRegionServer
name|rs
init|=
name|mockRegionServer
argument_list|(
name|conf
argument_list|)
decl_stmt|;
comment|// Region r1 has two store files, one hfile link and one hfile
specifier|final
name|List
argument_list|<
name|Long
argument_list|>
name|r1StoreFileSizes
init|=
name|Arrays
operator|.
name|asList
argument_list|(
literal|1024L
argument_list|,
literal|2048L
argument_list|)
decl_stmt|;
specifier|final
name|List
argument_list|<
name|Long
argument_list|>
name|r1HFileSizes
init|=
name|Arrays
operator|.
name|asList
argument_list|(
literal|0L
argument_list|,
literal|2048L
argument_list|)
decl_stmt|;
specifier|final
name|long
name|r1HFileSizeSum
init|=
name|sum
argument_list|(
name|r1HFileSizes
argument_list|)
decl_stmt|;
comment|// Region r2 has one store file which is a hfile link
specifier|final
name|List
argument_list|<
name|Long
argument_list|>
name|r2StoreFileSizes
init|=
name|Arrays
operator|.
name|asList
argument_list|(
literal|1024L
operator|*
literal|1024L
argument_list|)
decl_stmt|;
specifier|final
name|List
argument_list|<
name|Long
argument_list|>
name|r2HFileSizes
init|=
name|Arrays
operator|.
name|asList
argument_list|(
literal|0L
argument_list|)
decl_stmt|;
specifier|final
name|long
name|r2HFileSizeSum
init|=
name|sum
argument_list|(
name|r2HFileSizes
argument_list|)
decl_stmt|;
comment|// We expect that only the hfiles would be counted (hfile links are ignored)
specifier|final
name|FileSystemUtilizationChore
name|chore
init|=
operator|new
name|FileSystemUtilizationChore
argument_list|(
name|rs
argument_list|)
decl_stmt|;
name|doAnswer
argument_list|(
operator|new
name|ExpectedRegionSizeSummationAnswer
argument_list|(
name|sum
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|r1HFileSizeSum
argument_list|,
name|r2HFileSizeSum
argument_list|)
argument_list|)
argument_list|)
argument_list|)
operator|.
name|when
argument_list|(
name|rs
argument_list|)
operator|.
name|reportRegionSizesForQuotas
argument_list|(
operator|(
name|Map
argument_list|<
name|RegionInfo
argument_list|,
name|Long
argument_list|>
operator|)
name|any
argument_list|(
name|Map
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
specifier|final
name|Region
name|r1
init|=
name|mockRegionWithHFileLinks
argument_list|(
name|r1StoreFileSizes
argument_list|,
name|r1HFileSizes
argument_list|)
decl_stmt|;
specifier|final
name|Region
name|r2
init|=
name|mockRegionWithHFileLinks
argument_list|(
name|r2StoreFileSizes
argument_list|,
name|r2HFileSizes
argument_list|)
decl_stmt|;
name|when
argument_list|(
name|rs
operator|.
name|getRegions
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|r1
argument_list|,
name|r2
argument_list|)
argument_list|)
expr_stmt|;
name|chore
operator|.
name|chore
argument_list|()
expr_stmt|;
block|}
comment|/**    * Creates an HBase Configuration object for the default values.    */
specifier|private
name|Configuration
name|getDefaultHBaseConfiguration
parameter_list|()
block|{
specifier|final
name|Configuration
name|conf
init|=
name|HBaseConfiguration
operator|.
name|create
argument_list|()
decl_stmt|;
name|conf
operator|.
name|addResource
argument_list|(
literal|"hbase-default.xml"
argument_list|)
expr_stmt|;
return|return
name|conf
return|;
block|}
comment|/**    * Creates an HRegionServer using the given Configuration.    */
specifier|private
name|HRegionServer
name|mockRegionServer
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
specifier|final
name|HRegionServer
name|rs
init|=
name|mock
argument_list|(
name|HRegionServer
operator|.
name|class
argument_list|)
decl_stmt|;
name|when
argument_list|(
name|rs
operator|.
name|getConfiguration
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|conf
argument_list|)
expr_stmt|;
return|return
name|rs
return|;
block|}
comment|/**    * Sums the collection of non-null numbers.    */
specifier|private
name|long
name|sum
parameter_list|(
name|Collection
argument_list|<
name|Long
argument_list|>
name|values
parameter_list|)
block|{
name|long
name|sum
init|=
literal|0L
decl_stmt|;
for|for
control|(
name|Long
name|value
range|:
name|values
control|)
block|{
name|assertNotNull
argument_list|(
name|value
argument_list|)
expr_stmt|;
name|sum
operator|+=
name|value
expr_stmt|;
block|}
return|return
name|sum
return|;
block|}
comment|/**    * Creates a region with a number of Stores equal to the length of {@code storeSizes}. Each    * {@link Store} will have a reported size corresponding to the element in {@code storeSizes}.    *    * @param storeSizes A list of sizes for each Store.    * @return A mocked Region.    */
specifier|private
name|Region
name|mockRegionWithSize
parameter_list|(
name|Collection
argument_list|<
name|Long
argument_list|>
name|storeSizes
parameter_list|)
block|{
specifier|final
name|Region
name|r
init|=
name|mock
argument_list|(
name|Region
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|final
name|RegionInfo
name|info
init|=
name|mock
argument_list|(
name|RegionInfo
operator|.
name|class
argument_list|)
decl_stmt|;
name|when
argument_list|(
name|r
operator|.
name|getRegionInfo
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|info
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|Store
argument_list|>
name|stores
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|when
argument_list|(
name|r
operator|.
name|getStores
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
operator|(
name|List
operator|)
name|stores
argument_list|)
expr_stmt|;
for|for
control|(
name|Long
name|storeSize
range|:
name|storeSizes
control|)
block|{
specifier|final
name|Store
name|s
init|=
name|mock
argument_list|(
name|Store
operator|.
name|class
argument_list|)
decl_stmt|;
name|stores
operator|.
name|add
argument_list|(
name|s
argument_list|)
expr_stmt|;
name|when
argument_list|(
name|s
operator|.
name|getHFilesSize
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|storeSize
argument_list|)
expr_stmt|;
block|}
return|return
name|r
return|;
block|}
specifier|private
name|Region
name|mockRegionWithHFileLinks
parameter_list|(
name|Collection
argument_list|<
name|Long
argument_list|>
name|storeSizes
parameter_list|,
name|Collection
argument_list|<
name|Long
argument_list|>
name|hfileSizes
parameter_list|)
block|{
specifier|final
name|Region
name|r
init|=
name|mock
argument_list|(
name|Region
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|final
name|RegionInfo
name|info
init|=
name|mock
argument_list|(
name|RegionInfo
operator|.
name|class
argument_list|)
decl_stmt|;
name|when
argument_list|(
name|r
operator|.
name|getRegionInfo
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|info
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|Store
argument_list|>
name|stores
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|when
argument_list|(
name|r
operator|.
name|getStores
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
operator|(
name|List
operator|)
name|stores
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"Logic error, storeSizes and linkSizes must be equal in size"
argument_list|,
name|storeSizes
operator|.
name|size
argument_list|()
argument_list|,
name|hfileSizes
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|Iterator
argument_list|<
name|Long
argument_list|>
name|storeSizeIter
init|=
name|storeSizes
operator|.
name|iterator
argument_list|()
decl_stmt|;
name|Iterator
argument_list|<
name|Long
argument_list|>
name|hfileSizeIter
init|=
name|hfileSizes
operator|.
name|iterator
argument_list|()
decl_stmt|;
while|while
condition|(
name|storeSizeIter
operator|.
name|hasNext
argument_list|()
operator|&&
name|hfileSizeIter
operator|.
name|hasNext
argument_list|()
condition|)
block|{
specifier|final
name|long
name|storeSize
init|=
name|storeSizeIter
operator|.
name|next
argument_list|()
decl_stmt|;
specifier|final
name|long
name|hfileSize
init|=
name|hfileSizeIter
operator|.
name|next
argument_list|()
decl_stmt|;
specifier|final
name|Store
name|s
init|=
name|mock
argument_list|(
name|Store
operator|.
name|class
argument_list|)
decl_stmt|;
name|stores
operator|.
name|add
argument_list|(
name|s
argument_list|)
expr_stmt|;
name|when
argument_list|(
name|s
operator|.
name|getStorefilesSize
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|storeSize
argument_list|)
expr_stmt|;
name|when
argument_list|(
name|s
operator|.
name|getHFilesSize
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|hfileSize
argument_list|)
expr_stmt|;
block|}
return|return
name|r
return|;
block|}
comment|/**    * Creates a region which is the parent of a split.    *    * @param storeSizes A list of sizes for each Store.    * @return A mocked Region.    */
specifier|private
name|Region
name|mockSplitParentRegionWithSize
parameter_list|(
name|Collection
argument_list|<
name|Long
argument_list|>
name|storeSizes
parameter_list|)
block|{
specifier|final
name|Region
name|r
init|=
name|mockRegionWithSize
argument_list|(
name|storeSizes
argument_list|)
decl_stmt|;
specifier|final
name|RegionInfo
name|info
init|=
name|r
operator|.
name|getRegionInfo
argument_list|()
decl_stmt|;
name|when
argument_list|(
name|info
operator|.
name|isSplitParent
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
literal|true
argument_list|)
expr_stmt|;
return|return
name|r
return|;
block|}
comment|/**    * Creates a region who has a replicaId of<code>1</code>.    *    * @param storeSizes A list of sizes for each Store.    * @return A mocked Region.    */
specifier|private
name|Region
name|mockRegionReplicaWithSize
parameter_list|(
name|Collection
argument_list|<
name|Long
argument_list|>
name|storeSizes
parameter_list|)
block|{
specifier|final
name|Region
name|r
init|=
name|mockRegionWithSize
argument_list|(
name|storeSizes
argument_list|)
decl_stmt|;
specifier|final
name|RegionInfo
name|info
init|=
name|r
operator|.
name|getRegionInfo
argument_list|()
decl_stmt|;
name|when
argument_list|(
name|info
operator|.
name|getReplicaId
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
literal|1
argument_list|)
expr_stmt|;
return|return
name|r
return|;
block|}
comment|/**    * An Answer implementation which verifies the sum of the Region sizes to report is as expected.    */
specifier|private
specifier|static
class|class
name|ExpectedRegionSizeSummationAnswer
implements|implements
name|Answer
argument_list|<
name|Void
argument_list|>
block|{
specifier|private
specifier|final
name|long
name|expectedSize
decl_stmt|;
specifier|public
name|ExpectedRegionSizeSummationAnswer
parameter_list|(
name|long
name|expectedSize
parameter_list|)
block|{
name|this
operator|.
name|expectedSize
operator|=
name|expectedSize
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Void
name|answer
parameter_list|(
name|InvocationOnMock
name|invocation
parameter_list|)
throws|throws
name|Throwable
block|{
name|Object
index|[]
name|args
init|=
name|invocation
operator|.
name|getArguments
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|args
operator|.
name|length
argument_list|)
expr_stmt|;
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
name|Map
argument_list|<
name|RegionInfo
argument_list|,
name|Long
argument_list|>
name|regionSizes
init|=
operator|(
name|Map
argument_list|<
name|RegionInfo
argument_list|,
name|Long
argument_list|>
operator|)
name|args
index|[
literal|0
index|]
decl_stmt|;
name|long
name|sum
init|=
literal|0L
decl_stmt|;
for|for
control|(
name|Long
name|regionSize
range|:
name|regionSizes
operator|.
name|values
argument_list|()
control|)
block|{
name|sum
operator|+=
name|regionSize
expr_stmt|;
block|}
name|assertEquals
argument_list|(
name|expectedSize
argument_list|,
name|sum
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
block|}
block|}
end_class

end_unit

