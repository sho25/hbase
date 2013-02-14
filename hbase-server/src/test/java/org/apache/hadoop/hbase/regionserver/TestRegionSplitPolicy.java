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
name|assertNull
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
name|io
operator|.
name|IOException
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
name|List
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|TreeMap
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
name|HRegionInfo
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
name|HTableDescriptor
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
name|util
operator|.
name|Bytes
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
name|Mockito
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
name|TestRegionSplitPolicy
block|{
specifier|private
name|Configuration
name|conf
decl_stmt|;
specifier|private
name|HTableDescriptor
name|htd
decl_stmt|;
specifier|private
name|HRegion
name|mockRegion
decl_stmt|;
specifier|private
name|TreeMap
argument_list|<
name|byte
index|[]
argument_list|,
name|HStore
argument_list|>
name|stores
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|TABLENAME
init|=
operator|new
name|byte
index|[]
block|{
literal|'t'
block|}
decl_stmt|;
annotation|@
name|Before
specifier|public
name|void
name|setupMocks
parameter_list|()
block|{
name|conf
operator|=
name|HBaseConfiguration
operator|.
name|create
argument_list|()
expr_stmt|;
name|HRegionInfo
name|hri
init|=
operator|new
name|HRegionInfo
argument_list|(
name|TABLENAME
argument_list|)
decl_stmt|;
name|htd
operator|=
operator|new
name|HTableDescriptor
argument_list|(
name|TABLENAME
argument_list|)
expr_stmt|;
name|mockRegion
operator|=
name|Mockito
operator|.
name|mock
argument_list|(
name|HRegion
operator|.
name|class
argument_list|)
expr_stmt|;
name|Mockito
operator|.
name|doReturn
argument_list|(
name|htd
argument_list|)
operator|.
name|when
argument_list|(
name|mockRegion
argument_list|)
operator|.
name|getTableDesc
argument_list|()
expr_stmt|;
name|Mockito
operator|.
name|doReturn
argument_list|(
name|hri
argument_list|)
operator|.
name|when
argument_list|(
name|mockRegion
argument_list|)
operator|.
name|getRegionInfo
argument_list|()
expr_stmt|;
name|stores
operator|=
operator|new
name|TreeMap
argument_list|<
name|byte
index|[]
argument_list|,
name|HStore
argument_list|>
argument_list|(
name|Bytes
operator|.
name|BYTES_COMPARATOR
argument_list|)
expr_stmt|;
name|Mockito
operator|.
name|doReturn
argument_list|(
name|stores
argument_list|)
operator|.
name|when
argument_list|(
name|mockRegion
argument_list|)
operator|.
name|getStores
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testIncreasingToUpperBoundRegionSplitPolicy
parameter_list|()
throws|throws
name|IOException
block|{
comment|// Configure IncreasingToUpperBoundRegionSplitPolicy as our split policy
name|conf
operator|.
name|set
argument_list|(
name|HConstants
operator|.
name|HBASE_REGION_SPLIT_POLICY_KEY
argument_list|,
name|IncreasingToUpperBoundRegionSplitPolicy
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
comment|// Now make it so the mock region has a RegionServerService that will
comment|// return 'online regions'.
name|RegionServerServices
name|rss
init|=
name|Mockito
operator|.
name|mock
argument_list|(
name|RegionServerServices
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|final
name|List
argument_list|<
name|HRegion
argument_list|>
name|regions
init|=
operator|new
name|ArrayList
argument_list|<
name|HRegion
argument_list|>
argument_list|()
decl_stmt|;
name|Mockito
operator|.
name|when
argument_list|(
name|rss
operator|.
name|getOnlineRegions
argument_list|(
name|TABLENAME
argument_list|)
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|regions
argument_list|)
expr_stmt|;
name|Mockito
operator|.
name|when
argument_list|(
name|mockRegion
operator|.
name|getRegionServerServices
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|rss
argument_list|)
expr_stmt|;
comment|// Set max size for this 'table'.
name|long
name|maxSplitSize
init|=
literal|1024L
decl_stmt|;
name|htd
operator|.
name|setMaxFileSize
argument_list|(
name|maxSplitSize
argument_list|)
expr_stmt|;
comment|// Set flush size to 1/4.  IncreasingToUpperBoundRegionSplitPolicy
comment|// grows by the square of the number of regions times flushsize each time.
name|long
name|flushSize
init|=
name|maxSplitSize
operator|/
literal|4
decl_stmt|;
name|conf
operator|.
name|setLong
argument_list|(
name|HConstants
operator|.
name|HREGION_MEMSTORE_FLUSH_SIZE
argument_list|,
name|flushSize
argument_list|)
expr_stmt|;
name|htd
operator|.
name|setMemStoreFlushSize
argument_list|(
name|flushSize
argument_list|)
expr_stmt|;
comment|// If RegionServerService with no regions in it -- 'online regions' == 0 --
comment|// then IncreasingToUpperBoundRegionSplitPolicy should act like a
comment|// ConstantSizePolicy
name|IncreasingToUpperBoundRegionSplitPolicy
name|policy
init|=
operator|(
name|IncreasingToUpperBoundRegionSplitPolicy
operator|)
name|RegionSplitPolicy
operator|.
name|create
argument_list|(
name|mockRegion
argument_list|,
name|conf
argument_list|)
decl_stmt|;
name|doConstantSizePolicyTests
argument_list|(
name|policy
argument_list|)
expr_stmt|;
comment|// Add a store in excess of split size.  Because there are "no regions"
comment|// on this server -- rss.getOnlineRegions is 0 -- then we should split
comment|// like a constantsizeregionsplitpolicy would
name|HStore
name|mockStore
init|=
name|Mockito
operator|.
name|mock
argument_list|(
name|HStore
operator|.
name|class
argument_list|)
decl_stmt|;
name|Mockito
operator|.
name|doReturn
argument_list|(
literal|2000L
argument_list|)
operator|.
name|when
argument_list|(
name|mockStore
argument_list|)
operator|.
name|getSize
argument_list|()
expr_stmt|;
name|Mockito
operator|.
name|doReturn
argument_list|(
literal|true
argument_list|)
operator|.
name|when
argument_list|(
name|mockStore
argument_list|)
operator|.
name|canSplit
argument_list|()
expr_stmt|;
name|stores
operator|.
name|put
argument_list|(
operator|new
name|byte
index|[]
block|{
literal|1
block|}
argument_list|,
name|mockStore
argument_list|)
expr_stmt|;
comment|// It should split
name|assertTrue
argument_list|(
name|policy
operator|.
name|shouldSplit
argument_list|()
argument_list|)
expr_stmt|;
comment|// Now test that we increase our split size as online regions for a table
comment|// grows. With one region, split size should be flushsize.
name|regions
operator|.
name|add
argument_list|(
name|mockRegion
argument_list|)
expr_stmt|;
name|Mockito
operator|.
name|doReturn
argument_list|(
name|flushSize
operator|/
literal|2
argument_list|)
operator|.
name|when
argument_list|(
name|mockStore
argument_list|)
operator|.
name|getSize
argument_list|()
expr_stmt|;
comment|// Should not split since store is 1/2 flush size.
name|assertFalse
argument_list|(
name|policy
operator|.
name|shouldSplit
argument_list|()
argument_list|)
expr_stmt|;
comment|// Set size of store to be> flush size and we should split
name|Mockito
operator|.
name|doReturn
argument_list|(
name|flushSize
operator|+
literal|1
argument_list|)
operator|.
name|when
argument_list|(
name|mockStore
argument_list|)
operator|.
name|getSize
argument_list|()
expr_stmt|;
name|assertTrue
argument_list|(
name|policy
operator|.
name|shouldSplit
argument_list|()
argument_list|)
expr_stmt|;
comment|// Add another region to the 'online regions' on this server and we should
comment|// now be no longer be splittable since split size has gone up.
name|regions
operator|.
name|add
argument_list|(
name|mockRegion
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|policy
operator|.
name|shouldSplit
argument_list|()
argument_list|)
expr_stmt|;
comment|// Quadruple (2 squared) the store size and make sure its just over; verify it'll split
name|Mockito
operator|.
name|doReturn
argument_list|(
operator|(
name|flushSize
operator|*
literal|2
operator|*
literal|2
operator|)
operator|+
literal|1
argument_list|)
operator|.
name|when
argument_list|(
name|mockStore
argument_list|)
operator|.
name|getSize
argument_list|()
expr_stmt|;
name|assertTrue
argument_list|(
name|policy
operator|.
name|shouldSplit
argument_list|()
argument_list|)
expr_stmt|;
comment|// Finally assert that even if loads of regions, we'll split at max size
name|assertEquals
argument_list|(
name|maxSplitSize
argument_list|,
name|policy
operator|.
name|getSizeToCheck
argument_list|(
literal|1000
argument_list|)
argument_list|)
expr_stmt|;
comment|// Assert same is true if count of regions is zero.
name|assertEquals
argument_list|(
name|maxSplitSize
argument_list|,
name|policy
operator|.
name|getSizeToCheck
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testCreateDefault
parameter_list|()
throws|throws
name|IOException
block|{
name|conf
operator|.
name|setLong
argument_list|(
name|HConstants
operator|.
name|HREGION_MAX_FILESIZE
argument_list|,
literal|1234L
argument_list|)
expr_stmt|;
comment|// Using a default HTD, should pick up the file size from
comment|// configuration.
name|ConstantSizeRegionSplitPolicy
name|policy
init|=
operator|(
name|ConstantSizeRegionSplitPolicy
operator|)
name|RegionSplitPolicy
operator|.
name|create
argument_list|(
name|mockRegion
argument_list|,
name|conf
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|1234L
argument_list|,
name|policy
operator|.
name|getDesiredMaxFileSize
argument_list|()
argument_list|)
expr_stmt|;
comment|// If specified in HTD, should use that
name|htd
operator|.
name|setMaxFileSize
argument_list|(
literal|9999L
argument_list|)
expr_stmt|;
name|policy
operator|=
operator|(
name|ConstantSizeRegionSplitPolicy
operator|)
name|RegionSplitPolicy
operator|.
name|create
argument_list|(
name|mockRegion
argument_list|,
name|conf
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|9999L
argument_list|,
name|policy
operator|.
name|getDesiredMaxFileSize
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**    * Test setting up a customized split policy    */
annotation|@
name|Test
specifier|public
name|void
name|testCustomPolicy
parameter_list|()
throws|throws
name|IOException
block|{
name|HTableDescriptor
name|myHtd
init|=
operator|new
name|HTableDescriptor
argument_list|()
decl_stmt|;
name|myHtd
operator|.
name|setValue
argument_list|(
name|HTableDescriptor
operator|.
name|SPLIT_POLICY
argument_list|,
name|KeyPrefixRegionSplitPolicy
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|myHtd
operator|.
name|setValue
argument_list|(
name|KeyPrefixRegionSplitPolicy
operator|.
name|PREFIX_LENGTH_KEY
argument_list|,
name|String
operator|.
name|valueOf
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|HRegion
name|myMockRegion
init|=
name|Mockito
operator|.
name|mock
argument_list|(
name|HRegion
operator|.
name|class
argument_list|)
decl_stmt|;
name|Mockito
operator|.
name|doReturn
argument_list|(
name|myHtd
argument_list|)
operator|.
name|when
argument_list|(
name|myMockRegion
argument_list|)
operator|.
name|getTableDesc
argument_list|()
expr_stmt|;
name|Mockito
operator|.
name|doReturn
argument_list|(
name|stores
argument_list|)
operator|.
name|when
argument_list|(
name|myMockRegion
argument_list|)
operator|.
name|getStores
argument_list|()
expr_stmt|;
name|HStore
name|mockStore
init|=
name|Mockito
operator|.
name|mock
argument_list|(
name|HStore
operator|.
name|class
argument_list|)
decl_stmt|;
name|Mockito
operator|.
name|doReturn
argument_list|(
literal|2000L
argument_list|)
operator|.
name|when
argument_list|(
name|mockStore
argument_list|)
operator|.
name|getSize
argument_list|()
expr_stmt|;
name|Mockito
operator|.
name|doReturn
argument_list|(
literal|true
argument_list|)
operator|.
name|when
argument_list|(
name|mockStore
argument_list|)
operator|.
name|canSplit
argument_list|()
expr_stmt|;
name|Mockito
operator|.
name|doReturn
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"abcd"
argument_list|)
argument_list|)
operator|.
name|when
argument_list|(
name|mockStore
argument_list|)
operator|.
name|getSplitPoint
argument_list|()
expr_stmt|;
name|stores
operator|.
name|put
argument_list|(
operator|new
name|byte
index|[]
block|{
literal|1
block|}
argument_list|,
name|mockStore
argument_list|)
expr_stmt|;
name|KeyPrefixRegionSplitPolicy
name|policy
init|=
operator|(
name|KeyPrefixRegionSplitPolicy
operator|)
name|RegionSplitPolicy
operator|.
name|create
argument_list|(
name|myMockRegion
argument_list|,
name|conf
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"ab"
argument_list|,
name|Bytes
operator|.
name|toString
argument_list|(
name|policy
operator|.
name|getSplitPoint
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|Mockito
operator|.
name|doReturn
argument_list|(
literal|true
argument_list|)
operator|.
name|when
argument_list|(
name|myMockRegion
argument_list|)
operator|.
name|shouldForceSplit
argument_list|()
expr_stmt|;
name|Mockito
operator|.
name|doReturn
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"efgh"
argument_list|)
argument_list|)
operator|.
name|when
argument_list|(
name|myMockRegion
argument_list|)
operator|.
name|getExplicitSplitPoint
argument_list|()
expr_stmt|;
name|policy
operator|=
operator|(
name|KeyPrefixRegionSplitPolicy
operator|)
name|RegionSplitPolicy
operator|.
name|create
argument_list|(
name|myMockRegion
argument_list|,
name|conf
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"ef"
argument_list|,
name|Bytes
operator|.
name|toString
argument_list|(
name|policy
operator|.
name|getSplitPoint
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testConstantSizePolicy
parameter_list|()
throws|throws
name|IOException
block|{
name|htd
operator|.
name|setMaxFileSize
argument_list|(
literal|1024L
argument_list|)
expr_stmt|;
name|ConstantSizeRegionSplitPolicy
name|policy
init|=
operator|(
name|ConstantSizeRegionSplitPolicy
operator|)
name|RegionSplitPolicy
operator|.
name|create
argument_list|(
name|mockRegion
argument_list|,
name|conf
argument_list|)
decl_stmt|;
name|doConstantSizePolicyTests
argument_list|(
name|policy
argument_list|)
expr_stmt|;
block|}
comment|/**    * Run through tests for a ConstantSizeRegionSplitPolicy    * @param policy    */
specifier|private
name|void
name|doConstantSizePolicyTests
parameter_list|(
specifier|final
name|ConstantSizeRegionSplitPolicy
name|policy
parameter_list|)
block|{
comment|// For no stores, should not split
name|assertFalse
argument_list|(
name|policy
operator|.
name|shouldSplit
argument_list|()
argument_list|)
expr_stmt|;
comment|// Add a store above the requisite size. Should split.
name|HStore
name|mockStore
init|=
name|Mockito
operator|.
name|mock
argument_list|(
name|HStore
operator|.
name|class
argument_list|)
decl_stmt|;
name|Mockito
operator|.
name|doReturn
argument_list|(
literal|2000L
argument_list|)
operator|.
name|when
argument_list|(
name|mockStore
argument_list|)
operator|.
name|getSize
argument_list|()
expr_stmt|;
name|Mockito
operator|.
name|doReturn
argument_list|(
literal|true
argument_list|)
operator|.
name|when
argument_list|(
name|mockStore
argument_list|)
operator|.
name|canSplit
argument_list|()
expr_stmt|;
name|stores
operator|.
name|put
argument_list|(
operator|new
name|byte
index|[]
block|{
literal|1
block|}
argument_list|,
name|mockStore
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|policy
operator|.
name|shouldSplit
argument_list|()
argument_list|)
expr_stmt|;
comment|// Act as if there's a reference file or some other reason it can't split.
comment|// This should prevent splitting even though it's big enough.
name|Mockito
operator|.
name|doReturn
argument_list|(
literal|false
argument_list|)
operator|.
name|when
argument_list|(
name|mockStore
argument_list|)
operator|.
name|canSplit
argument_list|()
expr_stmt|;
name|assertFalse
argument_list|(
name|policy
operator|.
name|shouldSplit
argument_list|()
argument_list|)
expr_stmt|;
comment|// Reset splittability after above
name|Mockito
operator|.
name|doReturn
argument_list|(
literal|true
argument_list|)
operator|.
name|when
argument_list|(
name|mockStore
argument_list|)
operator|.
name|canSplit
argument_list|()
expr_stmt|;
comment|// Set to a small size but turn on forceSplit. Should result in a split.
name|Mockito
operator|.
name|doReturn
argument_list|(
literal|true
argument_list|)
operator|.
name|when
argument_list|(
name|mockRegion
argument_list|)
operator|.
name|shouldForceSplit
argument_list|()
expr_stmt|;
name|Mockito
operator|.
name|doReturn
argument_list|(
literal|100L
argument_list|)
operator|.
name|when
argument_list|(
name|mockStore
argument_list|)
operator|.
name|getSize
argument_list|()
expr_stmt|;
name|assertTrue
argument_list|(
name|policy
operator|.
name|shouldSplit
argument_list|()
argument_list|)
expr_stmt|;
comment|// Turn off forceSplit, should not split
name|Mockito
operator|.
name|doReturn
argument_list|(
literal|false
argument_list|)
operator|.
name|when
argument_list|(
name|mockRegion
argument_list|)
operator|.
name|shouldForceSplit
argument_list|()
expr_stmt|;
name|assertFalse
argument_list|(
name|policy
operator|.
name|shouldSplit
argument_list|()
argument_list|)
expr_stmt|;
comment|// Clear families we added above
name|stores
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testGetSplitPoint
parameter_list|()
throws|throws
name|IOException
block|{
name|ConstantSizeRegionSplitPolicy
name|policy
init|=
operator|(
name|ConstantSizeRegionSplitPolicy
operator|)
name|RegionSplitPolicy
operator|.
name|create
argument_list|(
name|mockRegion
argument_list|,
name|conf
argument_list|)
decl_stmt|;
comment|// For no stores, should not split
name|assertFalse
argument_list|(
name|policy
operator|.
name|shouldSplit
argument_list|()
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|policy
operator|.
name|getSplitPoint
argument_list|()
argument_list|)
expr_stmt|;
comment|// Add a store above the requisite size. Should split.
name|HStore
name|mockStore
init|=
name|Mockito
operator|.
name|mock
argument_list|(
name|HStore
operator|.
name|class
argument_list|)
decl_stmt|;
name|Mockito
operator|.
name|doReturn
argument_list|(
literal|2000L
argument_list|)
operator|.
name|when
argument_list|(
name|mockStore
argument_list|)
operator|.
name|getSize
argument_list|()
expr_stmt|;
name|Mockito
operator|.
name|doReturn
argument_list|(
literal|true
argument_list|)
operator|.
name|when
argument_list|(
name|mockStore
argument_list|)
operator|.
name|canSplit
argument_list|()
expr_stmt|;
name|Mockito
operator|.
name|doReturn
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"store 1 split"
argument_list|)
argument_list|)
operator|.
name|when
argument_list|(
name|mockStore
argument_list|)
operator|.
name|getSplitPoint
argument_list|()
expr_stmt|;
name|stores
operator|.
name|put
argument_list|(
operator|new
name|byte
index|[]
block|{
literal|1
block|}
argument_list|,
name|mockStore
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"store 1 split"
argument_list|,
name|Bytes
operator|.
name|toString
argument_list|(
name|policy
operator|.
name|getSplitPoint
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
comment|// Add a bigger store. The split point should come from that one
name|HStore
name|mockStore2
init|=
name|Mockito
operator|.
name|mock
argument_list|(
name|HStore
operator|.
name|class
argument_list|)
decl_stmt|;
name|Mockito
operator|.
name|doReturn
argument_list|(
literal|4000L
argument_list|)
operator|.
name|when
argument_list|(
name|mockStore2
argument_list|)
operator|.
name|getSize
argument_list|()
expr_stmt|;
name|Mockito
operator|.
name|doReturn
argument_list|(
literal|true
argument_list|)
operator|.
name|when
argument_list|(
name|mockStore2
argument_list|)
operator|.
name|canSplit
argument_list|()
expr_stmt|;
name|Mockito
operator|.
name|doReturn
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"store 2 split"
argument_list|)
argument_list|)
operator|.
name|when
argument_list|(
name|mockStore2
argument_list|)
operator|.
name|getSplitPoint
argument_list|()
expr_stmt|;
name|stores
operator|.
name|put
argument_list|(
operator|new
name|byte
index|[]
block|{
literal|2
block|}
argument_list|,
name|mockStore2
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"store 2 split"
argument_list|,
name|Bytes
operator|.
name|toString
argument_list|(
name|policy
operator|.
name|getSplitPoint
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testDelimitedKeyPrefixRegionSplitPolicy
parameter_list|()
throws|throws
name|IOException
block|{
name|HTableDescriptor
name|myHtd
init|=
operator|new
name|HTableDescriptor
argument_list|()
decl_stmt|;
name|myHtd
operator|.
name|setValue
argument_list|(
name|HTableDescriptor
operator|.
name|SPLIT_POLICY
argument_list|,
name|DelimitedKeyPrefixRegionSplitPolicy
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|myHtd
operator|.
name|setValue
argument_list|(
name|DelimitedKeyPrefixRegionSplitPolicy
operator|.
name|DELIMITER_KEY
argument_list|,
literal|","
argument_list|)
expr_stmt|;
name|HRegion
name|myMockRegion
init|=
name|Mockito
operator|.
name|mock
argument_list|(
name|HRegion
operator|.
name|class
argument_list|)
decl_stmt|;
name|Mockito
operator|.
name|doReturn
argument_list|(
name|myHtd
argument_list|)
operator|.
name|when
argument_list|(
name|myMockRegion
argument_list|)
operator|.
name|getTableDesc
argument_list|()
expr_stmt|;
name|Mockito
operator|.
name|doReturn
argument_list|(
name|stores
argument_list|)
operator|.
name|when
argument_list|(
name|myMockRegion
argument_list|)
operator|.
name|getStores
argument_list|()
expr_stmt|;
name|HStore
name|mockStore
init|=
name|Mockito
operator|.
name|mock
argument_list|(
name|HStore
operator|.
name|class
argument_list|)
decl_stmt|;
name|Mockito
operator|.
name|doReturn
argument_list|(
literal|2000L
argument_list|)
operator|.
name|when
argument_list|(
name|mockStore
argument_list|)
operator|.
name|getSize
argument_list|()
expr_stmt|;
name|Mockito
operator|.
name|doReturn
argument_list|(
literal|true
argument_list|)
operator|.
name|when
argument_list|(
name|mockStore
argument_list|)
operator|.
name|canSplit
argument_list|()
expr_stmt|;
name|Mockito
operator|.
name|doReturn
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"ab,cd"
argument_list|)
argument_list|)
operator|.
name|when
argument_list|(
name|mockStore
argument_list|)
operator|.
name|getSplitPoint
argument_list|()
expr_stmt|;
name|stores
operator|.
name|put
argument_list|(
operator|new
name|byte
index|[]
block|{
literal|1
block|}
argument_list|,
name|mockStore
argument_list|)
expr_stmt|;
name|DelimitedKeyPrefixRegionSplitPolicy
name|policy
init|=
operator|(
name|DelimitedKeyPrefixRegionSplitPolicy
operator|)
name|RegionSplitPolicy
operator|.
name|create
argument_list|(
name|myMockRegion
argument_list|,
name|conf
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"ab"
argument_list|,
name|Bytes
operator|.
name|toString
argument_list|(
name|policy
operator|.
name|getSplitPoint
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|Mockito
operator|.
name|doReturn
argument_list|(
literal|true
argument_list|)
operator|.
name|when
argument_list|(
name|myMockRegion
argument_list|)
operator|.
name|shouldForceSplit
argument_list|()
expr_stmt|;
name|Mockito
operator|.
name|doReturn
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"efg,h"
argument_list|)
argument_list|)
operator|.
name|when
argument_list|(
name|myMockRegion
argument_list|)
operator|.
name|getExplicitSplitPoint
argument_list|()
expr_stmt|;
name|policy
operator|=
operator|(
name|DelimitedKeyPrefixRegionSplitPolicy
operator|)
name|RegionSplitPolicy
operator|.
name|create
argument_list|(
name|myMockRegion
argument_list|,
name|conf
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"efg"
argument_list|,
name|Bytes
operator|.
name|toString
argument_list|(
name|policy
operator|.
name|getSplitPoint
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|Mockito
operator|.
name|doReturn
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"ijk"
argument_list|)
argument_list|)
operator|.
name|when
argument_list|(
name|myMockRegion
argument_list|)
operator|.
name|getExplicitSplitPoint
argument_list|()
expr_stmt|;
name|assertEquals
argument_list|(
literal|"ijk"
argument_list|,
name|Bytes
operator|.
name|toString
argument_list|(
name|policy
operator|.
name|getSplitPoint
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

