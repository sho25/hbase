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
name|hbase
operator|.
name|codec
operator|.
name|prefixtree
operator|.
name|row
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|ByteArrayOutputStream
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
name|nio
operator|.
name|ByteBuffer
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
name|List
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
name|KeyValue
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
name|KeyValueUtil
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
name|CollectionUtils
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hbase
operator|.
name|Cell
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hbase
operator|.
name|CellComparator
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hbase
operator|.
name|codec
operator|.
name|prefixtree
operator|.
name|decode
operator|.
name|DecoderFactory
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hbase
operator|.
name|codec
operator|.
name|prefixtree
operator|.
name|encode
operator|.
name|PrefixTreeEncoder
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hbase
operator|.
name|codec
operator|.
name|prefixtree
operator|.
name|scanner
operator|.
name|CellScannerPosition
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hbase
operator|.
name|codec
operator|.
name|prefixtree
operator|.
name|scanner
operator|.
name|CellSearcher
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Assert
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
name|runner
operator|.
name|RunWith
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|runners
operator|.
name|Parameterized
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|runners
operator|.
name|Parameterized
operator|.
name|Parameters
import|;
end_import

begin_class
annotation|@
name|RunWith
argument_list|(
name|Parameterized
operator|.
name|class
argument_list|)
specifier|public
class|class
name|TestPrefixTreeSearcher
block|{
specifier|protected
specifier|static
name|int
name|BLOCK_START
init|=
literal|7
decl_stmt|;
annotation|@
name|Parameters
specifier|public
specifier|static
name|Collection
argument_list|<
name|Object
index|[]
argument_list|>
name|parameters
parameter_list|()
block|{
return|return
operator|new
name|TestRowData
operator|.
name|InMemory
argument_list|()
operator|.
name|getAllAsObjectArray
argument_list|()
return|;
block|}
specifier|protected
name|TestRowData
name|rows
decl_stmt|;
specifier|protected
name|ByteBuffer
name|block
decl_stmt|;
specifier|public
name|TestPrefixTreeSearcher
parameter_list|(
name|TestRowData
name|testRows
parameter_list|)
throws|throws
name|IOException
block|{
name|this
operator|.
name|rows
operator|=
name|testRows
expr_stmt|;
name|ByteArrayOutputStream
name|os
init|=
operator|new
name|ByteArrayOutputStream
argument_list|(
literal|1
operator|<<
literal|20
argument_list|)
decl_stmt|;
name|PrefixTreeEncoder
name|kvBuilder
init|=
operator|new
name|PrefixTreeEncoder
argument_list|(
name|os
argument_list|,
literal|true
argument_list|)
decl_stmt|;
for|for
control|(
name|KeyValue
name|kv
range|:
name|rows
operator|.
name|getInputs
argument_list|()
control|)
block|{
name|kvBuilder
operator|.
name|write
argument_list|(
name|kv
argument_list|)
expr_stmt|;
block|}
name|kvBuilder
operator|.
name|flush
argument_list|()
expr_stmt|;
name|byte
index|[]
name|outputBytes
init|=
name|os
operator|.
name|toByteArray
argument_list|()
decl_stmt|;
name|this
operator|.
name|block
operator|=
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|outputBytes
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testScanForwards
parameter_list|()
throws|throws
name|IOException
block|{
name|CellSearcher
name|searcher
init|=
literal|null
decl_stmt|;
try|try
block|{
name|searcher
operator|=
name|DecoderFactory
operator|.
name|checkOut
argument_list|(
name|block
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|int
name|i
init|=
operator|-
literal|1
decl_stmt|;
while|while
condition|(
name|searcher
operator|.
name|advance
argument_list|()
condition|)
block|{
operator|++
name|i
expr_stmt|;
name|KeyValue
name|inputCell
init|=
name|rows
operator|.
name|getInputs
argument_list|()
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|Cell
name|outputCell
init|=
name|searcher
operator|.
name|current
argument_list|()
decl_stmt|;
comment|// check all 3 permutations of equals()
name|Assert
operator|.
name|assertEquals
argument_list|(
name|inputCell
argument_list|,
name|outputCell
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|outputCell
argument_list|,
name|inputCell
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|CellComparator
operator|.
name|equals
argument_list|(
name|inputCell
argument_list|,
name|outputCell
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|Assert
operator|.
name|assertEquals
argument_list|(
name|rows
operator|.
name|getInputs
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
name|i
operator|+
literal|1
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|DecoderFactory
operator|.
name|checkIn
argument_list|(
name|searcher
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testScanBackwards
parameter_list|()
throws|throws
name|IOException
block|{
name|CellSearcher
name|searcher
init|=
literal|null
decl_stmt|;
try|try
block|{
name|searcher
operator|=
name|DecoderFactory
operator|.
name|checkOut
argument_list|(
name|block
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|searcher
operator|.
name|positionAfterLastCell
argument_list|()
expr_stmt|;
name|int
name|i
init|=
operator|-
literal|1
decl_stmt|;
while|while
condition|(
name|searcher
operator|.
name|previous
argument_list|()
condition|)
block|{
operator|++
name|i
expr_stmt|;
name|int
name|oppositeIndex
init|=
name|rows
operator|.
name|getInputs
argument_list|()
operator|.
name|size
argument_list|()
operator|-
name|i
operator|-
literal|1
decl_stmt|;
name|KeyValue
name|inputKv
init|=
name|rows
operator|.
name|getInputs
argument_list|()
operator|.
name|get
argument_list|(
name|oppositeIndex
argument_list|)
decl_stmt|;
name|KeyValue
name|outputKv
init|=
name|KeyValueUtil
operator|.
name|copyToNewKeyValue
argument_list|(
name|searcher
operator|.
name|current
argument_list|()
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|inputKv
argument_list|,
name|outputKv
argument_list|)
expr_stmt|;
block|}
name|Assert
operator|.
name|assertEquals
argument_list|(
name|rows
operator|.
name|getInputs
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
name|i
operator|+
literal|1
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|DecoderFactory
operator|.
name|checkIn
argument_list|(
name|searcher
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testRandomSeekHits
parameter_list|()
throws|throws
name|IOException
block|{
name|CellSearcher
name|searcher
init|=
literal|null
decl_stmt|;
try|try
block|{
name|searcher
operator|=
name|DecoderFactory
operator|.
name|checkOut
argument_list|(
name|block
argument_list|,
literal|true
argument_list|)
expr_stmt|;
for|for
control|(
name|KeyValue
name|kv
range|:
name|rows
operator|.
name|getInputs
argument_list|()
control|)
block|{
name|boolean
name|hit
init|=
name|searcher
operator|.
name|positionAt
argument_list|(
name|kv
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|hit
argument_list|)
expr_stmt|;
name|Cell
name|foundKv
init|=
name|searcher
operator|.
name|current
argument_list|()
decl_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|CellComparator
operator|.
name|equals
argument_list|(
name|kv
argument_list|,
name|foundKv
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
finally|finally
block|{
name|DecoderFactory
operator|.
name|checkIn
argument_list|(
name|searcher
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * very hard to test nubs with this thing since the a nextRowKey function will usually skip them    */
annotation|@
name|Test
specifier|public
name|void
name|testRandomSeekMisses
parameter_list|()
throws|throws
name|IOException
block|{
name|CellSearcher
name|searcher
init|=
literal|null
decl_stmt|;
name|List
argument_list|<
name|Integer
argument_list|>
name|rowStartIndexes
init|=
name|rows
operator|.
name|getRowStartIndexes
argument_list|()
decl_stmt|;
try|try
block|{
name|searcher
operator|=
name|DecoderFactory
operator|.
name|checkOut
argument_list|(
name|block
argument_list|,
literal|true
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|rows
operator|.
name|getInputs
argument_list|()
operator|.
name|size
argument_list|()
condition|;
operator|++
name|i
control|)
block|{
name|KeyValue
name|kv
init|=
name|rows
operator|.
name|getInputs
argument_list|()
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
comment|//nextRow
name|KeyValue
name|inputNextRow
init|=
name|KeyValueUtil
operator|.
name|createFirstKeyInNextRow
argument_list|(
name|kv
argument_list|)
decl_stmt|;
name|CellScannerPosition
name|position
init|=
name|searcher
operator|.
name|positionAtOrBefore
argument_list|(
name|inputNextRow
argument_list|)
decl_stmt|;
name|boolean
name|isFirstInRow
init|=
name|rowStartIndexes
operator|.
name|contains
argument_list|(
name|i
argument_list|)
decl_stmt|;
if|if
condition|(
name|isFirstInRow
condition|)
block|{
name|int
name|rowIndex
init|=
name|rowStartIndexes
operator|.
name|indexOf
argument_list|(
name|i
argument_list|)
decl_stmt|;
if|if
condition|(
name|rowIndex
operator|<
name|rowStartIndexes
operator|.
name|size
argument_list|()
operator|-
literal|1
condition|)
block|{
comment|//            int lastKvInRowI = rowStartIndexes.get(rowIndex + 1) - 1;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|CellScannerPosition
operator|.
name|BEFORE
argument_list|,
name|position
argument_list|)
expr_stmt|;
comment|/*              * Can't get this to work between nubs like rowB\x00<-> rowBB              *              * No reason to doubt that it works, but will have to come up with a smarter test.              */
comment|//            Assert.assertEquals(rows.getInputs().get(lastKvInRowI), searcher.getCurrentCell());
block|}
block|}
comment|//previous KV
name|KeyValue
name|inputPreviousKv
init|=
name|KeyValueUtil
operator|.
name|previousKey
argument_list|(
name|kv
argument_list|)
decl_stmt|;
name|boolean
name|hit
init|=
name|searcher
operator|.
name|positionAt
argument_list|(
name|inputPreviousKv
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertFalse
argument_list|(
name|hit
argument_list|)
expr_stmt|;
name|position
operator|=
name|searcher
operator|.
name|positionAtOrAfter
argument_list|(
name|inputPreviousKv
argument_list|)
expr_stmt|;
if|if
condition|(
name|CollectionUtils
operator|.
name|isLastIndex
argument_list|(
name|rows
operator|.
name|getInputs
argument_list|()
argument_list|,
name|i
argument_list|)
condition|)
block|{
name|Assert
operator|.
name|assertTrue
argument_list|(
name|CellScannerPosition
operator|.
name|AFTER_LAST
operator|==
name|position
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|Assert
operator|.
name|assertTrue
argument_list|(
name|CellScannerPosition
operator|.
name|AFTER
operator|==
name|position
argument_list|)
expr_stmt|;
comment|/*            * TODO: why i+1 instead of i?            */
name|Assert
operator|.
name|assertEquals
argument_list|(
name|rows
operator|.
name|getInputs
argument_list|()
operator|.
name|get
argument_list|(
name|i
operator|+
literal|1
argument_list|)
argument_list|,
name|searcher
operator|.
name|current
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
finally|finally
block|{
name|DecoderFactory
operator|.
name|checkIn
argument_list|(
name|searcher
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testRandomSeekIndividualAssertions
parameter_list|()
throws|throws
name|IOException
block|{
name|CellSearcher
name|searcher
init|=
literal|null
decl_stmt|;
try|try
block|{
name|searcher
operator|=
name|DecoderFactory
operator|.
name|checkOut
argument_list|(
name|block
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|rows
operator|.
name|individualSearcherAssertions
argument_list|(
name|searcher
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|DecoderFactory
operator|.
name|checkIn
argument_list|(
name|searcher
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

