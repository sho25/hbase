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
name|regionserver
package|;
end_package

begin_import
import|import
name|junit
operator|.
name|framework
operator|.
name|TestCase
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
name|experimental
operator|.
name|categories
operator|.
name|Category
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
name|NavigableMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|SortedSet
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
name|TestCellFlatSet
extends|extends
name|TestCase
block|{
specifier|private
specifier|static
specifier|final
name|int
name|NUM_OF_CELLS
init|=
literal|4
decl_stmt|;
specifier|private
name|Cell
name|cells
index|[]
decl_stmt|;
specifier|private
name|CellArrayMap
name|cbOnHeap
decl_stmt|;
specifier|private
specifier|final
specifier|static
name|Configuration
name|conf
init|=
operator|new
name|Configuration
argument_list|()
decl_stmt|;
specifier|private
name|HeapMemStoreLAB
name|mslab
decl_stmt|;
specifier|protected
name|void
name|setUp
parameter_list|()
throws|throws
name|Exception
block|{
name|super
operator|.
name|setUp
argument_list|()
expr_stmt|;
comment|// create array of Cells to bass to the CellFlatMap under CellSet
specifier|final
name|byte
index|[]
name|one
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|15
argument_list|)
decl_stmt|;
specifier|final
name|byte
index|[]
name|two
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|25
argument_list|)
decl_stmt|;
specifier|final
name|byte
index|[]
name|three
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|35
argument_list|)
decl_stmt|;
specifier|final
name|byte
index|[]
name|four
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|45
argument_list|)
decl_stmt|;
specifier|final
name|byte
index|[]
name|f
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"f"
argument_list|)
decl_stmt|;
specifier|final
name|byte
index|[]
name|q
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"q"
argument_list|)
decl_stmt|;
specifier|final
name|byte
index|[]
name|v
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|4
argument_list|)
decl_stmt|;
specifier|final
name|KeyValue
name|kv1
init|=
operator|new
name|KeyValue
argument_list|(
name|one
argument_list|,
name|f
argument_list|,
name|q
argument_list|,
literal|10
argument_list|,
name|v
argument_list|)
decl_stmt|;
specifier|final
name|KeyValue
name|kv2
init|=
operator|new
name|KeyValue
argument_list|(
name|two
argument_list|,
name|f
argument_list|,
name|q
argument_list|,
literal|20
argument_list|,
name|v
argument_list|)
decl_stmt|;
specifier|final
name|KeyValue
name|kv3
init|=
operator|new
name|KeyValue
argument_list|(
name|three
argument_list|,
name|f
argument_list|,
name|q
argument_list|,
literal|30
argument_list|,
name|v
argument_list|)
decl_stmt|;
specifier|final
name|KeyValue
name|kv4
init|=
operator|new
name|KeyValue
argument_list|(
name|four
argument_list|,
name|f
argument_list|,
name|q
argument_list|,
literal|40
argument_list|,
name|v
argument_list|)
decl_stmt|;
name|cells
operator|=
operator|new
name|Cell
index|[]
block|{
name|kv1
block|,
name|kv2
block|,
name|kv3
block|,
name|kv4
block|}
expr_stmt|;
name|cbOnHeap
operator|=
operator|new
name|CellArrayMap
argument_list|(
name|CellComparator
operator|.
name|COMPARATOR
argument_list|,
name|cells
argument_list|,
literal|0
argument_list|,
name|NUM_OF_CELLS
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setBoolean
argument_list|(
name|SegmentFactory
operator|.
name|USEMSLAB_KEY
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setFloat
argument_list|(
name|MemStoreChunkPool
operator|.
name|CHUNK_POOL_MAXSIZE_KEY
argument_list|,
literal|0.2f
argument_list|)
expr_stmt|;
name|MemStoreChunkPool
operator|.
name|chunkPoolDisabled
operator|=
literal|false
expr_stmt|;
name|mslab
operator|=
operator|new
name|HeapMemStoreLAB
argument_list|(
name|conf
argument_list|)
expr_stmt|;
block|}
comment|/* Create and test CellSet based on CellArrayMap */
specifier|public
name|void
name|testCellBlocksOnHeap
parameter_list|()
throws|throws
name|Exception
block|{
name|CellSet
name|cs
init|=
operator|new
name|CellSet
argument_list|(
name|cbOnHeap
argument_list|)
decl_stmt|;
name|testCellBlocks
argument_list|(
name|cs
argument_list|)
expr_stmt|;
name|testIterators
argument_list|(
name|cs
argument_list|)
expr_stmt|;
block|}
comment|/* Generic basic test for immutable CellSet */
specifier|private
name|void
name|testCellBlocks
parameter_list|(
name|CellSet
name|cs
parameter_list|)
throws|throws
name|Exception
block|{
specifier|final
name|byte
index|[]
name|oneAndHalf
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|20
argument_list|)
decl_stmt|;
specifier|final
name|byte
index|[]
name|f
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"f"
argument_list|)
decl_stmt|;
specifier|final
name|byte
index|[]
name|q
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"q"
argument_list|)
decl_stmt|;
specifier|final
name|byte
index|[]
name|v
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|4
argument_list|)
decl_stmt|;
specifier|final
name|KeyValue
name|outerCell
init|=
operator|new
name|KeyValue
argument_list|(
name|oneAndHalf
argument_list|,
name|f
argument_list|,
name|q
argument_list|,
literal|10
argument_list|,
name|v
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|NUM_OF_CELLS
argument_list|,
name|cs
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
comment|// check size
name|assertFalse
argument_list|(
name|cs
operator|.
name|contains
argument_list|(
name|outerCell
argument_list|)
argument_list|)
expr_stmt|;
comment|// check outer cell
name|assertTrue
argument_list|(
name|cs
operator|.
name|contains
argument_list|(
name|cells
index|[
literal|0
index|]
argument_list|)
argument_list|)
expr_stmt|;
comment|// check existence of the first
name|Cell
name|first
init|=
name|cs
operator|.
name|first
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
name|cells
index|[
literal|0
index|]
operator|.
name|equals
argument_list|(
name|first
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|cs
operator|.
name|contains
argument_list|(
name|cells
index|[
name|NUM_OF_CELLS
operator|-
literal|1
index|]
argument_list|)
argument_list|)
expr_stmt|;
comment|// check last
name|Cell
name|last
init|=
name|cs
operator|.
name|last
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
name|cells
index|[
name|NUM_OF_CELLS
operator|-
literal|1
index|]
operator|.
name|equals
argument_list|(
name|last
argument_list|)
argument_list|)
expr_stmt|;
name|SortedSet
argument_list|<
name|Cell
argument_list|>
name|tail
init|=
name|cs
operator|.
name|tailSet
argument_list|(
name|cells
index|[
literal|1
index|]
argument_list|)
decl_stmt|;
comment|// check tail abd head sizes
name|assertEquals
argument_list|(
name|NUM_OF_CELLS
operator|-
literal|1
argument_list|,
name|tail
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|SortedSet
argument_list|<
name|Cell
argument_list|>
name|head
init|=
name|cs
operator|.
name|headSet
argument_list|(
name|cells
index|[
literal|1
index|]
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|head
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|SortedSet
argument_list|<
name|Cell
argument_list|>
name|tailOuter
init|=
name|cs
operator|.
name|tailSet
argument_list|(
name|outerCell
argument_list|)
decl_stmt|;
comment|// check tail starting from outer cell
name|assertEquals
argument_list|(
name|NUM_OF_CELLS
operator|-
literal|1
argument_list|,
name|tailOuter
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|Cell
name|tailFirst
init|=
name|tail
operator|.
name|first
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
name|cells
index|[
literal|1
index|]
operator|.
name|equals
argument_list|(
name|tailFirst
argument_list|)
argument_list|)
expr_stmt|;
name|Cell
name|tailLast
init|=
name|tail
operator|.
name|last
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
name|cells
index|[
name|NUM_OF_CELLS
operator|-
literal|1
index|]
operator|.
name|equals
argument_list|(
name|tailLast
argument_list|)
argument_list|)
expr_stmt|;
name|Cell
name|headFirst
init|=
name|head
operator|.
name|first
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
name|cells
index|[
literal|0
index|]
operator|.
name|equals
argument_list|(
name|headFirst
argument_list|)
argument_list|)
expr_stmt|;
name|Cell
name|headLast
init|=
name|head
operator|.
name|last
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
name|cells
index|[
literal|0
index|]
operator|.
name|equals
argument_list|(
name|headLast
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/* Generic iterators test for immutable CellSet */
specifier|private
name|void
name|testIterators
parameter_list|(
name|CellSet
name|cs
parameter_list|)
throws|throws
name|Exception
block|{
comment|// Assert that we have NUM_OF_CELLS values and that they are in order
name|int
name|count
init|=
literal|0
decl_stmt|;
for|for
control|(
name|Cell
name|kv
range|:
name|cs
control|)
block|{
name|assertEquals
argument_list|(
literal|"\n\n-------------------------------------------------------------------\n"
operator|+
literal|"Comparing iteration number "
operator|+
operator|(
name|count
operator|+
literal|1
operator|)
operator|+
literal|" the returned cell: "
operator|+
name|kv
operator|+
literal|", the first Cell in the CellBlocksMap: "
operator|+
name|cells
index|[
name|count
index|]
operator|+
literal|", and the same transformed to String: "
operator|+
name|cells
index|[
name|count
index|]
operator|.
name|toString
argument_list|()
operator|+
literal|"\n-------------------------------------------------------------------\n"
argument_list|,
name|cells
index|[
name|count
index|]
argument_list|,
name|kv
argument_list|)
expr_stmt|;
name|count
operator|++
expr_stmt|;
block|}
name|assertEquals
argument_list|(
name|NUM_OF_CELLS
argument_list|,
name|count
argument_list|)
expr_stmt|;
comment|// Test descending iterator
name|count
operator|=
literal|0
expr_stmt|;
for|for
control|(
name|Iterator
argument_list|<
name|Cell
argument_list|>
name|i
init|=
name|cs
operator|.
name|descendingIterator
argument_list|()
init|;
name|i
operator|.
name|hasNext
argument_list|()
condition|;
control|)
block|{
name|Cell
name|kv
init|=
name|i
operator|.
name|next
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
name|cells
index|[
name|NUM_OF_CELLS
operator|-
operator|(
name|count
operator|+
literal|1
operator|)
index|]
argument_list|,
name|kv
argument_list|)
expr_stmt|;
name|count
operator|++
expr_stmt|;
block|}
name|assertEquals
argument_list|(
name|NUM_OF_CELLS
argument_list|,
name|count
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

