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
name|*
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
name|*
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
name|Store
argument_list|>
name|stores
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
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"testtable"
argument_list|)
argument_list|)
decl_stmt|;
name|htd
operator|=
operator|new
name|HTableDescriptor
argument_list|()
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
name|Store
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
name|testCreateDefault
parameter_list|()
throws|throws
name|IOException
block|{
name|conf
operator|.
name|setLong
argument_list|(
literal|"hbase.hregion.max.filesize"
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
name|Store
name|mockStore
init|=
name|Mockito
operator|.
name|mock
argument_list|(
name|Store
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
name|Store
name|mockStore
init|=
name|Mockito
operator|.
name|mock
argument_list|(
name|Store
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
name|Store
name|mockStore2
init|=
name|Mockito
operator|.
name|mock
argument_list|(
name|Store
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
block|}
end_class

end_unit

