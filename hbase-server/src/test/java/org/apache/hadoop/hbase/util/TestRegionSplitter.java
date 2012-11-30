begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|util
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
name|assertArrayEquals
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
name|assertNotSame
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
name|Map
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|lang
operator|.
name|ArrayUtils
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|Log
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|LogFactory
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
name|HBaseTestingUtility
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
name|MediumTests
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
name|ServerName
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
name|HTable
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
name|RegionSplitter
operator|.
name|HexStringSplit
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
name|RegionSplitter
operator|.
name|SplitAlgorithm
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
name|RegionSplitter
operator|.
name|UniformSplit
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|AfterClass
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
comment|/**  * Tests for {@link RegionSplitter}, which can create a pre-split table or do a  * rolling split of an existing table.  */
end_comment

begin_class
annotation|@
name|Category
argument_list|(
name|MediumTests
operator|.
name|class
argument_list|)
specifier|public
class|class
name|TestRegionSplitter
block|{
specifier|private
specifier|final
specifier|static
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|TestRegionSplitter
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
specifier|static
name|HBaseTestingUtility
name|UTIL
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
specifier|private
specifier|final
specifier|static
name|String
name|CF_NAME
init|=
literal|"SPLIT_TEST_CF"
decl_stmt|;
specifier|private
specifier|final
specifier|static
name|byte
name|xFF
init|=
operator|(
name|byte
operator|)
literal|0xff
decl_stmt|;
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|setup
parameter_list|()
throws|throws
name|Exception
block|{
name|UTIL
operator|.
name|startMiniCluster
argument_list|()
expr_stmt|;
block|}
annotation|@
name|AfterClass
specifier|public
specifier|static
name|void
name|teardown
parameter_list|()
throws|throws
name|Exception
block|{
name|UTIL
operator|.
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
block|}
comment|/**      * Test creating a pre-split table using the HexStringSplit algorithm.      */
annotation|@
name|Test
specifier|public
name|void
name|testCreatePresplitTableHex
parameter_list|()
throws|throws
name|Exception
block|{
specifier|final
name|List
argument_list|<
name|byte
index|[]
argument_list|>
name|expectedBounds
init|=
operator|new
name|ArrayList
argument_list|<
name|byte
index|[]
argument_list|>
argument_list|()
decl_stmt|;
name|expectedBounds
operator|.
name|add
argument_list|(
name|ArrayUtils
operator|.
name|EMPTY_BYTE_ARRAY
argument_list|)
expr_stmt|;
name|expectedBounds
operator|.
name|add
argument_list|(
literal|"10000000"
operator|.
name|getBytes
argument_list|()
argument_list|)
expr_stmt|;
name|expectedBounds
operator|.
name|add
argument_list|(
literal|"20000000"
operator|.
name|getBytes
argument_list|()
argument_list|)
expr_stmt|;
name|expectedBounds
operator|.
name|add
argument_list|(
literal|"30000000"
operator|.
name|getBytes
argument_list|()
argument_list|)
expr_stmt|;
name|expectedBounds
operator|.
name|add
argument_list|(
literal|"40000000"
operator|.
name|getBytes
argument_list|()
argument_list|)
expr_stmt|;
name|expectedBounds
operator|.
name|add
argument_list|(
literal|"50000000"
operator|.
name|getBytes
argument_list|()
argument_list|)
expr_stmt|;
name|expectedBounds
operator|.
name|add
argument_list|(
literal|"60000000"
operator|.
name|getBytes
argument_list|()
argument_list|)
expr_stmt|;
name|expectedBounds
operator|.
name|add
argument_list|(
literal|"70000000"
operator|.
name|getBytes
argument_list|()
argument_list|)
expr_stmt|;
name|expectedBounds
operator|.
name|add
argument_list|(
literal|"80000000"
operator|.
name|getBytes
argument_list|()
argument_list|)
expr_stmt|;
name|expectedBounds
operator|.
name|add
argument_list|(
literal|"90000000"
operator|.
name|getBytes
argument_list|()
argument_list|)
expr_stmt|;
name|expectedBounds
operator|.
name|add
argument_list|(
literal|"a0000000"
operator|.
name|getBytes
argument_list|()
argument_list|)
expr_stmt|;
name|expectedBounds
operator|.
name|add
argument_list|(
literal|"b0000000"
operator|.
name|getBytes
argument_list|()
argument_list|)
expr_stmt|;
name|expectedBounds
operator|.
name|add
argument_list|(
literal|"c0000000"
operator|.
name|getBytes
argument_list|()
argument_list|)
expr_stmt|;
name|expectedBounds
operator|.
name|add
argument_list|(
literal|"d0000000"
operator|.
name|getBytes
argument_list|()
argument_list|)
expr_stmt|;
name|expectedBounds
operator|.
name|add
argument_list|(
literal|"e0000000"
operator|.
name|getBytes
argument_list|()
argument_list|)
expr_stmt|;
name|expectedBounds
operator|.
name|add
argument_list|(
literal|"f0000000"
operator|.
name|getBytes
argument_list|()
argument_list|)
expr_stmt|;
name|expectedBounds
operator|.
name|add
argument_list|(
name|ArrayUtils
operator|.
name|EMPTY_BYTE_ARRAY
argument_list|)
expr_stmt|;
comment|// Do table creation/pre-splitting and verification of region boundaries
name|preSplitTableAndVerify
argument_list|(
name|expectedBounds
argument_list|,
name|HexStringSplit
operator|.
name|class
operator|.
name|getSimpleName
argument_list|()
argument_list|,
literal|"NewHexPresplitTable"
argument_list|)
expr_stmt|;
block|}
comment|/**      * Test creating a pre-split table using the UniformSplit algorithm.      */
annotation|@
name|Test
specifier|public
name|void
name|testCreatePresplitTableUniform
parameter_list|()
throws|throws
name|Exception
block|{
name|List
argument_list|<
name|byte
index|[]
argument_list|>
name|expectedBounds
init|=
operator|new
name|ArrayList
argument_list|<
name|byte
index|[]
argument_list|>
argument_list|()
decl_stmt|;
name|expectedBounds
operator|.
name|add
argument_list|(
name|ArrayUtils
operator|.
name|EMPTY_BYTE_ARRAY
argument_list|)
expr_stmt|;
name|expectedBounds
operator|.
name|add
argument_list|(
operator|new
name|byte
index|[]
block|{
literal|0x10
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|}
argument_list|)
expr_stmt|;
name|expectedBounds
operator|.
name|add
argument_list|(
operator|new
name|byte
index|[]
block|{
literal|0x20
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|}
argument_list|)
expr_stmt|;
name|expectedBounds
operator|.
name|add
argument_list|(
operator|new
name|byte
index|[]
block|{
literal|0x30
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|}
argument_list|)
expr_stmt|;
name|expectedBounds
operator|.
name|add
argument_list|(
operator|new
name|byte
index|[]
block|{
literal|0x40
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|}
argument_list|)
expr_stmt|;
name|expectedBounds
operator|.
name|add
argument_list|(
operator|new
name|byte
index|[]
block|{
literal|0x50
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|}
argument_list|)
expr_stmt|;
name|expectedBounds
operator|.
name|add
argument_list|(
operator|new
name|byte
index|[]
block|{
literal|0x60
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|}
argument_list|)
expr_stmt|;
name|expectedBounds
operator|.
name|add
argument_list|(
operator|new
name|byte
index|[]
block|{
literal|0x70
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|}
argument_list|)
expr_stmt|;
name|expectedBounds
operator|.
name|add
argument_list|(
operator|new
name|byte
index|[]
block|{
operator|(
name|byte
operator|)
literal|0x80
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|}
argument_list|)
expr_stmt|;
name|expectedBounds
operator|.
name|add
argument_list|(
operator|new
name|byte
index|[]
block|{
operator|(
name|byte
operator|)
literal|0x90
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|}
argument_list|)
expr_stmt|;
name|expectedBounds
operator|.
name|add
argument_list|(
operator|new
name|byte
index|[]
block|{
operator|(
name|byte
operator|)
literal|0xa0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|}
argument_list|)
expr_stmt|;
name|expectedBounds
operator|.
name|add
argument_list|(
operator|new
name|byte
index|[]
block|{
operator|(
name|byte
operator|)
literal|0xb0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|}
argument_list|)
expr_stmt|;
name|expectedBounds
operator|.
name|add
argument_list|(
operator|new
name|byte
index|[]
block|{
operator|(
name|byte
operator|)
literal|0xc0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|}
argument_list|)
expr_stmt|;
name|expectedBounds
operator|.
name|add
argument_list|(
operator|new
name|byte
index|[]
block|{
operator|(
name|byte
operator|)
literal|0xd0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|}
argument_list|)
expr_stmt|;
name|expectedBounds
operator|.
name|add
argument_list|(
operator|new
name|byte
index|[]
block|{
operator|(
name|byte
operator|)
literal|0xe0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|}
argument_list|)
expr_stmt|;
name|expectedBounds
operator|.
name|add
argument_list|(
operator|new
name|byte
index|[]
block|{
operator|(
name|byte
operator|)
literal|0xf0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|}
argument_list|)
expr_stmt|;
name|expectedBounds
operator|.
name|add
argument_list|(
name|ArrayUtils
operator|.
name|EMPTY_BYTE_ARRAY
argument_list|)
expr_stmt|;
comment|// Do table creation/pre-splitting and verification of region boundaries
name|preSplitTableAndVerify
argument_list|(
name|expectedBounds
argument_list|,
name|UniformSplit
operator|.
name|class
operator|.
name|getSimpleName
argument_list|()
argument_list|,
literal|"NewUniformPresplitTable"
argument_list|)
expr_stmt|;
block|}
comment|/**      * Unit tests for the HexStringSplit algorithm. Makes sure it divides up the      * space of keys in the way that we expect.      */
annotation|@
name|Test
specifier|public
name|void
name|unitTestHexStringSplit
parameter_list|()
block|{
name|HexStringSplit
name|splitter
init|=
operator|new
name|HexStringSplit
argument_list|()
decl_stmt|;
comment|// Check splitting while starting from scratch
name|byte
index|[]
index|[]
name|twoRegionsSplits
init|=
name|splitter
operator|.
name|split
argument_list|(
literal|2
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|twoRegionsSplits
operator|.
name|length
argument_list|)
expr_stmt|;
name|assertArrayEquals
argument_list|(
name|twoRegionsSplits
index|[
literal|0
index|]
argument_list|,
literal|"80000000"
operator|.
name|getBytes
argument_list|()
argument_list|)
expr_stmt|;
name|byte
index|[]
index|[]
name|threeRegionsSplits
init|=
name|splitter
operator|.
name|split
argument_list|(
literal|3
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|threeRegionsSplits
operator|.
name|length
argument_list|)
expr_stmt|;
name|byte
index|[]
name|expectedSplit0
init|=
literal|"55555555"
operator|.
name|getBytes
argument_list|()
decl_stmt|;
name|assertArrayEquals
argument_list|(
name|expectedSplit0
argument_list|,
name|threeRegionsSplits
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
name|byte
index|[]
name|expectedSplit1
init|=
literal|"aaaaaaaa"
operator|.
name|getBytes
argument_list|()
decl_stmt|;
name|assertArrayEquals
argument_list|(
name|expectedSplit1
argument_list|,
name|threeRegionsSplits
index|[
literal|1
index|]
argument_list|)
expr_stmt|;
comment|// Check splitting existing regions that have start and end points
name|byte
index|[]
name|splitPoint
init|=
name|splitter
operator|.
name|split
argument_list|(
literal|"10000000"
operator|.
name|getBytes
argument_list|()
argument_list|,
literal|"30000000"
operator|.
name|getBytes
argument_list|()
argument_list|)
decl_stmt|;
name|assertArrayEquals
argument_list|(
literal|"20000000"
operator|.
name|getBytes
argument_list|()
argument_list|,
name|splitPoint
argument_list|)
expr_stmt|;
name|byte
index|[]
name|lastRow
init|=
literal|"ffffffff"
operator|.
name|getBytes
argument_list|()
decl_stmt|;
name|assertArrayEquals
argument_list|(
name|lastRow
argument_list|,
name|splitter
operator|.
name|lastRow
argument_list|()
argument_list|)
expr_stmt|;
name|byte
index|[]
name|firstRow
init|=
literal|"00000000"
operator|.
name|getBytes
argument_list|()
decl_stmt|;
name|assertArrayEquals
argument_list|(
name|firstRow
argument_list|,
name|splitter
operator|.
name|firstRow
argument_list|()
argument_list|)
expr_stmt|;
comment|// Halfway between 00... and 20... should be 10...
name|splitPoint
operator|=
name|splitter
operator|.
name|split
argument_list|(
name|firstRow
argument_list|,
literal|"20000000"
operator|.
name|getBytes
argument_list|()
argument_list|)
expr_stmt|;
name|assertArrayEquals
argument_list|(
name|splitPoint
argument_list|,
literal|"10000000"
operator|.
name|getBytes
argument_list|()
argument_list|)
expr_stmt|;
comment|// Halfway between df... and ff... should be ef....
name|splitPoint
operator|=
name|splitter
operator|.
name|split
argument_list|(
literal|"dfffffff"
operator|.
name|getBytes
argument_list|()
argument_list|,
name|lastRow
argument_list|)
expr_stmt|;
name|assertArrayEquals
argument_list|(
name|splitPoint
argument_list|,
literal|"efffffff"
operator|.
name|getBytes
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**      * Unit tests for the UniformSplit algorithm. Makes sure it divides up the space of      * keys in the way that we expect.      */
annotation|@
name|Test
specifier|public
name|void
name|unitTestUniformSplit
parameter_list|()
block|{
name|UniformSplit
name|splitter
init|=
operator|new
name|UniformSplit
argument_list|()
decl_stmt|;
comment|// Check splitting while starting from scratch
try|try
block|{
name|splitter
operator|.
name|split
argument_list|(
literal|1
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|AssertionError
argument_list|(
literal|"Splitting into<2 regions should have thrown exception"
argument_list|)
throw|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|e
parameter_list|)
block|{ }
name|byte
index|[]
index|[]
name|twoRegionsSplits
init|=
name|splitter
operator|.
name|split
argument_list|(
literal|2
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|twoRegionsSplits
operator|.
name|length
argument_list|)
expr_stmt|;
name|assertArrayEquals
argument_list|(
name|twoRegionsSplits
index|[
literal|0
index|]
argument_list|,
operator|new
name|byte
index|[]
block|{
operator|(
name|byte
operator|)
literal|0x80
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|}
argument_list|)
expr_stmt|;
name|byte
index|[]
index|[]
name|threeRegionsSplits
init|=
name|splitter
operator|.
name|split
argument_list|(
literal|3
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|threeRegionsSplits
operator|.
name|length
argument_list|)
expr_stmt|;
name|byte
index|[]
name|expectedSplit0
init|=
operator|new
name|byte
index|[]
block|{
literal|0x55
block|,
literal|0x55
block|,
literal|0x55
block|,
literal|0x55
block|,
literal|0x55
block|,
literal|0x55
block|,
literal|0x55
block|,
literal|0x55
block|}
decl_stmt|;
name|assertArrayEquals
argument_list|(
name|expectedSplit0
argument_list|,
name|threeRegionsSplits
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
name|byte
index|[]
name|expectedSplit1
init|=
operator|new
name|byte
index|[]
block|{
operator|(
name|byte
operator|)
literal|0xAA
block|,
operator|(
name|byte
operator|)
literal|0xAA
block|,
operator|(
name|byte
operator|)
literal|0xAA
block|,
operator|(
name|byte
operator|)
literal|0xAA
block|,
operator|(
name|byte
operator|)
literal|0xAA
block|,
operator|(
name|byte
operator|)
literal|0xAA
block|,
operator|(
name|byte
operator|)
literal|0xAA
block|,
operator|(
name|byte
operator|)
literal|0xAA
block|}
decl_stmt|;
name|assertArrayEquals
argument_list|(
name|expectedSplit1
argument_list|,
name|threeRegionsSplits
index|[
literal|1
index|]
argument_list|)
expr_stmt|;
comment|// Check splitting existing regions that have start and end points
name|byte
index|[]
name|splitPoint
init|=
name|splitter
operator|.
name|split
argument_list|(
operator|new
name|byte
index|[]
block|{
literal|0x10
block|}
argument_list|,
operator|new
name|byte
index|[]
block|{
literal|0x30
block|}
argument_list|)
decl_stmt|;
name|assertArrayEquals
argument_list|(
operator|new
name|byte
index|[]
block|{
literal|0x20
block|}
argument_list|,
name|splitPoint
argument_list|)
expr_stmt|;
name|byte
index|[]
name|lastRow
init|=
operator|new
name|byte
index|[]
block|{
name|xFF
block|,
name|xFF
block|,
name|xFF
block|,
name|xFF
block|,
name|xFF
block|,
name|xFF
block|,
name|xFF
block|,
name|xFF
block|}
decl_stmt|;
name|assertArrayEquals
argument_list|(
name|lastRow
argument_list|,
name|splitter
operator|.
name|lastRow
argument_list|()
argument_list|)
expr_stmt|;
name|byte
index|[]
name|firstRow
init|=
name|ArrayUtils
operator|.
name|EMPTY_BYTE_ARRAY
decl_stmt|;
name|assertArrayEquals
argument_list|(
name|firstRow
argument_list|,
name|splitter
operator|.
name|firstRow
argument_list|()
argument_list|)
expr_stmt|;
name|splitPoint
operator|=
name|splitter
operator|.
name|split
argument_list|(
name|firstRow
argument_list|,
operator|new
name|byte
index|[]
block|{
literal|0x20
block|}
argument_list|)
expr_stmt|;
name|assertArrayEquals
argument_list|(
name|splitPoint
argument_list|,
operator|new
name|byte
index|[]
block|{
literal|0x10
block|}
argument_list|)
expr_stmt|;
name|splitPoint
operator|=
name|splitter
operator|.
name|split
argument_list|(
operator|new
name|byte
index|[]
block|{
operator|(
name|byte
operator|)
literal|0xdf
block|,
name|xFF
block|,
name|xFF
block|,
name|xFF
block|,
name|xFF
block|,
name|xFF
block|,
name|xFF
block|,
name|xFF
block|}
argument_list|,
name|lastRow
argument_list|)
expr_stmt|;
name|assertArrayEquals
argument_list|(
name|splitPoint
argument_list|,
operator|new
name|byte
index|[]
block|{
operator|(
name|byte
operator|)
literal|0xef
block|,
name|xFF
block|,
name|xFF
block|,
name|xFF
block|,
name|xFF
block|,
name|xFF
block|,
name|xFF
block|,
name|xFF
block|}
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testUserInput
parameter_list|()
block|{
name|SplitAlgorithm
name|algo
init|=
operator|new
name|HexStringSplit
argument_list|()
decl_stmt|;
name|assertFalse
argument_list|(
name|splitFailsPrecondition
argument_list|(
name|algo
argument_list|)
argument_list|)
expr_stmt|;
comment|// default settings are fine
name|assertFalse
argument_list|(
name|splitFailsPrecondition
argument_list|(
name|algo
argument_list|,
literal|"00"
argument_list|,
literal|"AA"
argument_list|)
argument_list|)
expr_stmt|;
comment|// custom is fine
name|assertTrue
argument_list|(
name|splitFailsPrecondition
argument_list|(
name|algo
argument_list|,
literal|"AA"
argument_list|,
literal|"00"
argument_list|)
argument_list|)
expr_stmt|;
comment|// range error
name|assertTrue
argument_list|(
name|splitFailsPrecondition
argument_list|(
name|algo
argument_list|,
literal|"AA"
argument_list|,
literal|"AA"
argument_list|)
argument_list|)
expr_stmt|;
comment|// range error
name|assertFalse
argument_list|(
name|splitFailsPrecondition
argument_list|(
name|algo
argument_list|,
literal|"0"
argument_list|,
literal|"2"
argument_list|,
literal|3
argument_list|)
argument_list|)
expr_stmt|;
comment|// should be fine
name|assertFalse
argument_list|(
name|splitFailsPrecondition
argument_list|(
name|algo
argument_list|,
literal|"0"
argument_list|,
literal|"A"
argument_list|,
literal|11
argument_list|)
argument_list|)
expr_stmt|;
comment|// should be fine
name|assertTrue
argument_list|(
name|splitFailsPrecondition
argument_list|(
name|algo
argument_list|,
literal|"0"
argument_list|,
literal|"A"
argument_list|,
literal|12
argument_list|)
argument_list|)
expr_stmt|;
comment|// too granular
name|algo
operator|=
operator|new
name|UniformSplit
argument_list|()
expr_stmt|;
name|assertFalse
argument_list|(
name|splitFailsPrecondition
argument_list|(
name|algo
argument_list|)
argument_list|)
expr_stmt|;
comment|// default settings are fine
name|assertFalse
argument_list|(
name|splitFailsPrecondition
argument_list|(
name|algo
argument_list|,
literal|"\\x00"
argument_list|,
literal|"\\xAA"
argument_list|)
argument_list|)
expr_stmt|;
comment|// custom is fine
name|assertTrue
argument_list|(
name|splitFailsPrecondition
argument_list|(
name|algo
argument_list|,
literal|"\\xAA"
argument_list|,
literal|"\\x00"
argument_list|)
argument_list|)
expr_stmt|;
comment|// range error
name|assertTrue
argument_list|(
name|splitFailsPrecondition
argument_list|(
name|algo
argument_list|,
literal|"\\xAA"
argument_list|,
literal|"\\xAA"
argument_list|)
argument_list|)
expr_stmt|;
comment|// range error
name|assertFalse
argument_list|(
name|splitFailsPrecondition
argument_list|(
name|algo
argument_list|,
literal|"\\x00"
argument_list|,
literal|"\\x02"
argument_list|,
literal|3
argument_list|)
argument_list|)
expr_stmt|;
comment|// should be fine
name|assertFalse
argument_list|(
name|splitFailsPrecondition
argument_list|(
name|algo
argument_list|,
literal|"\\x00"
argument_list|,
literal|"\\x0A"
argument_list|,
literal|11
argument_list|)
argument_list|)
expr_stmt|;
comment|// should be fine
name|assertTrue
argument_list|(
name|splitFailsPrecondition
argument_list|(
name|algo
argument_list|,
literal|"\\x00"
argument_list|,
literal|"\\x0A"
argument_list|,
literal|12
argument_list|)
argument_list|)
expr_stmt|;
comment|// too granular
block|}
specifier|private
name|boolean
name|splitFailsPrecondition
parameter_list|(
name|SplitAlgorithm
name|algo
parameter_list|)
block|{
return|return
name|splitFailsPrecondition
argument_list|(
name|algo
argument_list|,
literal|100
argument_list|)
return|;
block|}
specifier|private
name|boolean
name|splitFailsPrecondition
parameter_list|(
name|SplitAlgorithm
name|algo
parameter_list|,
name|String
name|firstRow
parameter_list|,
name|String
name|lastRow
parameter_list|)
block|{
return|return
name|splitFailsPrecondition
argument_list|(
name|algo
argument_list|,
name|firstRow
argument_list|,
name|lastRow
argument_list|,
literal|100
argument_list|)
return|;
block|}
specifier|private
name|boolean
name|splitFailsPrecondition
parameter_list|(
name|SplitAlgorithm
name|algo
parameter_list|,
name|String
name|firstRow
parameter_list|,
name|String
name|lastRow
parameter_list|,
name|int
name|numRegions
parameter_list|)
block|{
name|algo
operator|.
name|setFirstRow
argument_list|(
name|firstRow
argument_list|)
expr_stmt|;
name|algo
operator|.
name|setLastRow
argument_list|(
name|lastRow
argument_list|)
expr_stmt|;
return|return
name|splitFailsPrecondition
argument_list|(
name|algo
argument_list|,
name|numRegions
argument_list|)
return|;
block|}
specifier|private
name|boolean
name|splitFailsPrecondition
parameter_list|(
name|SplitAlgorithm
name|algo
parameter_list|,
name|int
name|numRegions
parameter_list|)
block|{
try|try
block|{
name|byte
index|[]
index|[]
name|s
init|=
name|algo
operator|.
name|split
argument_list|(
name|numRegions
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"split algo = "
operator|+
name|algo
argument_list|)
expr_stmt|;
if|if
condition|(
name|s
operator|!=
literal|null
condition|)
block|{
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
for|for
control|(
name|byte
index|[]
name|b
range|:
name|s
control|)
block|{
name|sb
operator|.
name|append
argument_list|(
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|b
argument_list|)
operator|+
literal|"  "
argument_list|)
expr_stmt|;
block|}
name|LOG
operator|.
name|debug
argument_list|(
name|sb
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
literal|false
return|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|e
parameter_list|)
block|{
return|return
literal|true
return|;
block|}
catch|catch
parameter_list|(
name|IllegalStateException
name|e
parameter_list|)
block|{
return|return
literal|true
return|;
block|}
catch|catch
parameter_list|(
name|IndexOutOfBoundsException
name|e
parameter_list|)
block|{
return|return
literal|true
return|;
block|}
block|}
comment|/**      * Creates a pre-split table with expectedBounds.size()+1 regions, then      * verifies that the region boundaries are the same as the expected      * region boundaries in expectedBounds.      * @throws Various junit assertions      */
specifier|private
name|void
name|preSplitTableAndVerify
parameter_list|(
name|List
argument_list|<
name|byte
index|[]
argument_list|>
name|expectedBounds
parameter_list|,
name|String
name|splitClass
parameter_list|,
name|String
name|tableName
parameter_list|)
throws|throws
name|Exception
block|{
specifier|final
name|int
name|numRegions
init|=
name|expectedBounds
operator|.
name|size
argument_list|()
operator|-
literal|1
decl_stmt|;
specifier|final
name|Configuration
name|conf
init|=
name|UTIL
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
name|conf
operator|.
name|setInt
argument_list|(
literal|"split.count"
argument_list|,
name|numRegions
argument_list|)
expr_stmt|;
name|SplitAlgorithm
name|splitAlgo
init|=
name|RegionSplitter
operator|.
name|newSplitAlgoInstance
argument_list|(
name|conf
argument_list|,
name|splitClass
argument_list|)
decl_stmt|;
name|RegionSplitter
operator|.
name|createPresplitTable
argument_list|(
name|tableName
argument_list|,
name|splitAlgo
argument_list|,
operator|new
name|String
index|[]
block|{
name|CF_NAME
block|}
argument_list|,
name|conf
argument_list|)
expr_stmt|;
name|verifyBounds
argument_list|(
name|expectedBounds
argument_list|,
name|tableName
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|rollingSplitAndVerify
parameter_list|(
name|String
name|tableName
parameter_list|,
name|String
name|splitClass
parameter_list|,
name|List
argument_list|<
name|byte
index|[]
argument_list|>
name|expectedBounds
parameter_list|)
throws|throws
name|Exception
block|{
specifier|final
name|Configuration
name|conf
init|=
name|UTIL
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
comment|// Set this larger than the number of splits so RegionSplitter won't block
name|conf
operator|.
name|setInt
argument_list|(
literal|"split.outstanding"
argument_list|,
literal|5
argument_list|)
expr_stmt|;
name|SplitAlgorithm
name|splitAlgo
init|=
name|RegionSplitter
operator|.
name|newSplitAlgoInstance
argument_list|(
name|conf
argument_list|,
name|splitClass
argument_list|)
decl_stmt|;
name|RegionSplitter
operator|.
name|rollingSplit
argument_list|(
name|tableName
argument_list|,
name|splitAlgo
argument_list|,
name|conf
argument_list|)
expr_stmt|;
name|verifyBounds
argument_list|(
name|expectedBounds
argument_list|,
name|tableName
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|verifyBounds
parameter_list|(
name|List
argument_list|<
name|byte
index|[]
argument_list|>
name|expectedBounds
parameter_list|,
name|String
name|tableName
parameter_list|)
throws|throws
name|Exception
block|{
comment|// Get region boundaries from the cluster and verify their endpoints
specifier|final
name|Configuration
name|conf
init|=
name|UTIL
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
specifier|final
name|int
name|numRegions
init|=
name|expectedBounds
operator|.
name|size
argument_list|()
operator|-
literal|1
decl_stmt|;
specifier|final
name|HTable
name|hTable
init|=
operator|new
name|HTable
argument_list|(
name|conf
argument_list|,
name|tableName
operator|.
name|getBytes
argument_list|()
argument_list|)
decl_stmt|;
specifier|final
name|Map
argument_list|<
name|HRegionInfo
argument_list|,
name|ServerName
argument_list|>
name|regionInfoMap
init|=
name|hTable
operator|.
name|getRegionLocations
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
name|numRegions
argument_list|,
name|regionInfoMap
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|HRegionInfo
argument_list|,
name|ServerName
argument_list|>
name|entry
range|:
name|regionInfoMap
operator|.
name|entrySet
argument_list|()
control|)
block|{
specifier|final
name|HRegionInfo
name|regionInfo
init|=
name|entry
operator|.
name|getKey
argument_list|()
decl_stmt|;
name|byte
index|[]
name|regionStart
init|=
name|regionInfo
operator|.
name|getStartKey
argument_list|()
decl_stmt|;
name|byte
index|[]
name|regionEnd
init|=
name|regionInfo
operator|.
name|getEndKey
argument_list|()
decl_stmt|;
comment|// This region's start key should be one of the region boundaries
name|int
name|startBoundaryIndex
init|=
name|indexOfBytes
argument_list|(
name|expectedBounds
argument_list|,
name|regionStart
argument_list|)
decl_stmt|;
name|assertNotSame
argument_list|(
operator|-
literal|1
argument_list|,
name|startBoundaryIndex
argument_list|)
expr_stmt|;
comment|// This region's end key should be the region boundary that comes
comment|// after the starting boundary.
name|byte
index|[]
name|expectedRegionEnd
init|=
name|expectedBounds
operator|.
name|get
argument_list|(
name|startBoundaryIndex
operator|+
literal|1
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|Bytes
operator|.
name|compareTo
argument_list|(
name|regionEnd
argument_list|,
name|expectedRegionEnd
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**      * List.indexOf() doesn't really work for a List<byte[]>, because byte[]      * doesn't override equals(). This method checks whether a list contains      * a given element by checking each element using the byte array      * comparator.      * @return the index of the first element that equals compareTo, or -1      * if no elements are equal.      */
specifier|static
specifier|private
name|int
name|indexOfBytes
parameter_list|(
name|List
argument_list|<
name|byte
index|[]
argument_list|>
name|list
parameter_list|,
name|byte
index|[]
name|compareTo
parameter_list|)
block|{
name|int
name|listIndex
init|=
literal|0
decl_stmt|;
for|for
control|(
name|byte
index|[]
name|elem
range|:
name|list
control|)
block|{
if|if
condition|(
name|Bytes
operator|.
name|BYTES_COMPARATOR
operator|.
name|compare
argument_list|(
name|elem
argument_list|,
name|compareTo
argument_list|)
operator|==
literal|0
condition|)
block|{
return|return
name|listIndex
return|;
block|}
name|listIndex
operator|++
expr_stmt|;
block|}
return|return
operator|-
literal|1
return|;
block|}
block|}
end_class

end_unit

