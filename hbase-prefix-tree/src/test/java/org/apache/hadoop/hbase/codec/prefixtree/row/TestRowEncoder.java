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
name|ByteArrayInputStream
import|;
end_import

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
name|Cell
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
name|nio
operator|.
name|ByteBuff
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
name|nio
operator|.
name|SingleByteBuff
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
name|MiscTests
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
name|codec
operator|.
name|prefixtree
operator|.
name|PrefixTreeBlockMeta
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
name|codec
operator|.
name|prefixtree
operator|.
name|decode
operator|.
name|PrefixTreeArraySearcher
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
name|hadoop
operator|.
name|hbase
operator|.
name|util
operator|.
name|ByteBufferUtils
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
name|Assert
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
name|shaded
operator|.
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Lists
import|;
end_import

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|MiscTests
operator|.
name|class
block|,
name|SmallTests
operator|.
name|class
block|}
argument_list|)
annotation|@
name|RunWith
argument_list|(
name|Parameterized
operator|.
name|class
argument_list|)
specifier|public
class|class
name|TestRowEncoder
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
name|List
argument_list|<
name|Object
index|[]
argument_list|>
name|parameters
init|=
name|Lists
operator|.
name|newArrayList
argument_list|()
decl_stmt|;
for|for
control|(
name|TestRowData
name|testRows
range|:
name|TestRowData
operator|.
name|InMemory
operator|.
name|getAll
argument_list|()
control|)
block|{
name|parameters
operator|.
name|add
argument_list|(
operator|new
name|Object
index|[]
block|{
name|testRows
block|}
argument_list|)
expr_stmt|;
block|}
return|return
name|parameters
return|;
block|}
specifier|protected
name|TestRowData
name|rows
decl_stmt|;
specifier|protected
name|List
argument_list|<
name|KeyValue
argument_list|>
name|inputKvs
decl_stmt|;
specifier|protected
name|boolean
name|includeMemstoreTS
init|=
literal|true
decl_stmt|;
specifier|protected
name|ByteArrayOutputStream
name|os
decl_stmt|;
specifier|protected
name|PrefixTreeEncoder
name|encoder
decl_stmt|;
specifier|protected
name|int
name|totalBytes
decl_stmt|;
specifier|protected
name|PrefixTreeBlockMeta
name|blockMetaWriter
decl_stmt|;
specifier|protected
name|byte
index|[]
name|outputBytes
decl_stmt|;
specifier|protected
name|ByteBuff
name|buffer
decl_stmt|;
specifier|protected
name|ByteArrayInputStream
name|is
decl_stmt|;
specifier|protected
name|PrefixTreeBlockMeta
name|blockMetaReader
decl_stmt|;
specifier|protected
name|byte
index|[]
name|inputBytes
decl_stmt|;
specifier|protected
name|PrefixTreeArraySearcher
name|searcher
decl_stmt|;
specifier|public
name|TestRowEncoder
parameter_list|(
name|TestRowData
name|testRows
parameter_list|)
block|{
name|this
operator|.
name|rows
operator|=
name|testRows
expr_stmt|;
block|}
annotation|@
name|Before
specifier|public
name|void
name|compile
parameter_list|()
throws|throws
name|IOException
block|{
comment|// Always run with tags. But should also ensure that KVs without tags work fine
name|os
operator|=
operator|new
name|ByteArrayOutputStream
argument_list|(
literal|1
operator|<<
literal|20
argument_list|)
expr_stmt|;
name|encoder
operator|=
operator|new
name|PrefixTreeEncoder
argument_list|(
name|os
argument_list|,
name|includeMemstoreTS
argument_list|)
expr_stmt|;
name|inputKvs
operator|=
name|rows
operator|.
name|getInputs
argument_list|()
expr_stmt|;
for|for
control|(
name|KeyValue
name|kv
range|:
name|inputKvs
control|)
block|{
name|encoder
operator|.
name|write
argument_list|(
name|kv
argument_list|)
expr_stmt|;
block|}
name|encoder
operator|.
name|flush
argument_list|()
expr_stmt|;
name|totalBytes
operator|=
name|encoder
operator|.
name|getTotalBytes
argument_list|()
expr_stmt|;
name|blockMetaWriter
operator|=
name|encoder
operator|.
name|getBlockMeta
argument_list|()
expr_stmt|;
name|outputBytes
operator|=
name|os
operator|.
name|toByteArray
argument_list|()
expr_stmt|;
comment|// start reading, but save the assertions for @Test methods
name|ByteBuffer
name|out
init|=
name|ByteBuffer
operator|.
name|allocateDirect
argument_list|(
name|outputBytes
operator|.
name|length
argument_list|)
decl_stmt|;
name|ByteBufferUtils
operator|.
name|copyFromArrayToBuffer
argument_list|(
name|out
argument_list|,
name|outputBytes
argument_list|,
literal|0
argument_list|,
name|outputBytes
operator|.
name|length
argument_list|)
expr_stmt|;
name|out
operator|.
name|position
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|buffer
operator|=
operator|new
name|SingleByteBuff
argument_list|(
name|out
argument_list|)
expr_stmt|;
name|blockMetaReader
operator|=
operator|new
name|PrefixTreeBlockMeta
argument_list|(
name|buffer
argument_list|)
expr_stmt|;
name|searcher
operator|=
operator|new
name|PrefixTreeArraySearcher
argument_list|(
name|blockMetaReader
argument_list|,
name|blockMetaReader
operator|.
name|getRowTreeDepth
argument_list|()
argument_list|,
name|blockMetaReader
operator|.
name|getMaxRowLength
argument_list|()
argument_list|,
name|blockMetaReader
operator|.
name|getMaxQualifierLength
argument_list|()
argument_list|,
name|blockMetaReader
operator|.
name|getMaxTagsLength
argument_list|()
argument_list|)
expr_stmt|;
name|searcher
operator|.
name|initOnBlock
argument_list|(
name|blockMetaReader
argument_list|,
name|buffer
argument_list|,
name|includeMemstoreTS
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testEncoderOutput
parameter_list|()
throws|throws
name|IOException
block|{
name|Assert
operator|.
name|assertEquals
argument_list|(
name|totalBytes
argument_list|,
name|outputBytes
operator|.
name|length
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|blockMetaWriter
argument_list|,
name|blockMetaReader
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testForwardScanner
parameter_list|()
block|{
name|int
name|counter
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
name|counter
expr_stmt|;
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
name|counter
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
name|assertKeyAndValueEqual
argument_list|(
name|inputKv
argument_list|,
name|outputKv
argument_list|)
expr_stmt|;
block|}
comment|// assert same number of cells
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
name|counter
operator|+
literal|1
argument_list|)
expr_stmt|;
block|}
comment|/**    * probably not needed since testReverseScannerWithJitter() below is more thorough    */
annotation|@
name|Test
specifier|public
name|void
name|testReverseScanner
parameter_list|()
block|{
name|searcher
operator|.
name|positionAfterLastCell
argument_list|()
expr_stmt|;
name|int
name|counter
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
name|counter
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
name|counter
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
name|assertKeyAndValueEqual
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
name|counter
operator|+
literal|1
argument_list|)
expr_stmt|;
block|}
comment|/**    * Exercise the nubCellsRemain variable by calling next+previous.  NubCellsRemain is basically    * a special fan index.    */
annotation|@
name|Test
specifier|public
name|void
name|testReverseScannerWithJitter
parameter_list|()
block|{
name|searcher
operator|.
name|positionAfterLastCell
argument_list|()
expr_stmt|;
name|int
name|counter
init|=
operator|-
literal|1
decl_stmt|;
while|while
condition|(
literal|true
condition|)
block|{
name|boolean
name|foundCell
init|=
name|searcher
operator|.
name|previous
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|foundCell
condition|)
block|{
break|break;
block|}
operator|++
name|counter
expr_stmt|;
comment|// a next+previous should cancel out
if|if
condition|(
operator|!
name|searcher
operator|.
name|isAfterLast
argument_list|()
condition|)
block|{
name|searcher
operator|.
name|advance
argument_list|()
expr_stmt|;
name|searcher
operator|.
name|previous
argument_list|()
expr_stmt|;
block|}
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
name|counter
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
name|assertKeyAndValueEqual
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
name|counter
operator|+
literal|1
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testIndividualBlockMetaAssertions
parameter_list|()
block|{
name|rows
operator|.
name|individualBlockMetaAssertions
argument_list|(
name|blockMetaReader
argument_list|)
expr_stmt|;
block|}
comment|/**************** helper **************************/
specifier|protected
name|void
name|assertKeyAndValueEqual
parameter_list|(
name|Cell
name|expected
parameter_list|,
name|Cell
name|actual
parameter_list|)
block|{
comment|// assert keys are equal (doesn't compare values)
name|Assert
operator|.
name|assertEquals
argument_list|(
name|expected
argument_list|,
name|actual
argument_list|)
expr_stmt|;
if|if
condition|(
name|includeMemstoreTS
condition|)
block|{
name|Assert
operator|.
name|assertEquals
argument_list|(
name|expected
operator|.
name|getSequenceId
argument_list|()
argument_list|,
name|actual
operator|.
name|getSequenceId
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|// assert values equal
name|Assert
operator|.
name|assertTrue
argument_list|(
name|Bytes
operator|.
name|equals
argument_list|(
name|expected
operator|.
name|getValueArray
argument_list|()
argument_list|,
name|expected
operator|.
name|getValueOffset
argument_list|()
argument_list|,
name|expected
operator|.
name|getValueLength
argument_list|()
argument_list|,
name|actual
operator|.
name|getValueArray
argument_list|()
argument_list|,
name|actual
operator|.
name|getValueOffset
argument_list|()
argument_list|,
name|actual
operator|.
name|getValueLength
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

