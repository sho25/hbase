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
name|column
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
name|column
operator|.
name|ColumnReader
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
name|column
operator|.
name|ColumnSectionWriter
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
name|other
operator|.
name|ColumnNodeType
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
name|tokenize
operator|.
name|Tokenizer
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
name|tokenize
operator|.
name|TokenizerNode
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
name|ByteRange
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
name|ByteRangeUtils
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
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|util
operator|.
name|byterange
operator|.
name|impl
operator|.
name|ByteRangeTreeSet
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
name|TestColumnBuilder
block|{
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
name|TestColumnData
operator|.
name|InMemory
argument_list|()
operator|.
name|getAllAsObjectArray
argument_list|()
return|;
block|}
comment|/*********** fields **********************************/
specifier|protected
name|TestColumnData
name|columns
decl_stmt|;
specifier|protected
name|ByteRangeTreeSet
name|columnSorter
decl_stmt|;
specifier|protected
name|List
argument_list|<
name|ByteRange
argument_list|>
name|sortedUniqueColumns
decl_stmt|;
specifier|protected
name|PrefixTreeBlockMeta
name|blockMeta
decl_stmt|;
specifier|protected
name|Tokenizer
name|builder
decl_stmt|;
specifier|protected
name|ColumnSectionWriter
name|writer
decl_stmt|;
specifier|protected
name|byte
index|[]
name|bytes
decl_stmt|;
specifier|protected
name|byte
index|[]
name|buffer
decl_stmt|;
specifier|protected
name|ColumnReader
name|reader
decl_stmt|;
comment|/*************** construct ****************************/
specifier|public
name|TestColumnBuilder
parameter_list|(
name|TestColumnData
name|columns
parameter_list|)
block|{
name|this
operator|.
name|columns
operator|=
name|columns
expr_stmt|;
name|List
argument_list|<
name|ByteRange
argument_list|>
name|inputs
init|=
name|columns
operator|.
name|getInputs
argument_list|()
decl_stmt|;
name|this
operator|.
name|columnSorter
operator|=
operator|new
name|ByteRangeTreeSet
argument_list|(
name|inputs
argument_list|)
expr_stmt|;
name|this
operator|.
name|sortedUniqueColumns
operator|=
name|columnSorter
operator|.
name|compile
argument_list|()
operator|.
name|getSortedRanges
argument_list|()
expr_stmt|;
name|List
argument_list|<
name|byte
index|[]
argument_list|>
name|copies
init|=
name|ByteRangeUtils
operator|.
name|copyToNewArrays
argument_list|(
name|sortedUniqueColumns
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|Bytes
operator|.
name|isSorted
argument_list|(
name|copies
argument_list|)
argument_list|)
expr_stmt|;
name|this
operator|.
name|blockMeta
operator|=
operator|new
name|PrefixTreeBlockMeta
argument_list|()
expr_stmt|;
name|this
operator|.
name|blockMeta
operator|.
name|setNumMetaBytes
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|this
operator|.
name|blockMeta
operator|.
name|setNumRowBytes
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|this
operator|.
name|builder
operator|=
operator|new
name|Tokenizer
argument_list|()
expr_stmt|;
block|}
comment|/************* methods ********************************/
annotation|@
name|Test
specifier|public
name|void
name|testReaderRoundTrip
parameter_list|()
throws|throws
name|IOException
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
name|sortedUniqueColumns
operator|.
name|size
argument_list|()
condition|;
operator|++
name|i
control|)
block|{
name|ByteRange
name|column
init|=
name|sortedUniqueColumns
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|builder
operator|.
name|addSorted
argument_list|(
name|column
argument_list|)
expr_stmt|;
block|}
name|List
argument_list|<
name|byte
index|[]
argument_list|>
name|builderOutputArrays
init|=
name|builder
operator|.
name|getArrays
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|builderOutputArrays
operator|.
name|size
argument_list|()
condition|;
operator|++
name|i
control|)
block|{
name|byte
index|[]
name|inputArray
init|=
name|sortedUniqueColumns
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|deepCopyToNewArray
argument_list|()
decl_stmt|;
name|byte
index|[]
name|outputArray
init|=
name|builderOutputArrays
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|boolean
name|same
init|=
name|Bytes
operator|.
name|equals
argument_list|(
name|inputArray
argument_list|,
name|outputArray
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|same
argument_list|)
expr_stmt|;
block|}
name|Assert
operator|.
name|assertEquals
argument_list|(
name|sortedUniqueColumns
operator|.
name|size
argument_list|()
argument_list|,
name|builderOutputArrays
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|writer
operator|=
operator|new
name|ColumnSectionWriter
argument_list|(
name|blockMeta
argument_list|,
name|builder
argument_list|,
name|ColumnNodeType
operator|.
name|QUALIFIER
argument_list|)
expr_stmt|;
name|ByteArrayOutputStream
name|baos
init|=
operator|new
name|ByteArrayOutputStream
argument_list|()
decl_stmt|;
name|writer
operator|.
name|compile
argument_list|()
operator|.
name|writeBytes
argument_list|(
name|baos
argument_list|)
expr_stmt|;
name|bytes
operator|=
name|baos
operator|.
name|toByteArray
argument_list|()
expr_stmt|;
name|buffer
operator|=
operator|new
name|byte
index|[
name|blockMeta
operator|.
name|getMaxQualifierLength
argument_list|()
index|]
expr_stmt|;
name|reader
operator|=
operator|new
name|ColumnReader
argument_list|(
name|buffer
argument_list|,
name|ColumnNodeType
operator|.
name|QUALIFIER
argument_list|)
expr_stmt|;
name|reader
operator|.
name|initOnBlock
argument_list|(
name|blockMeta
argument_list|,
name|bytes
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|TokenizerNode
argument_list|>
name|builderNodes
init|=
name|Lists
operator|.
name|newArrayList
argument_list|()
decl_stmt|;
name|builder
operator|.
name|appendNodes
argument_list|(
name|builderNodes
argument_list|,
literal|true
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|int
name|i
init|=
literal|0
decl_stmt|;
for|for
control|(
name|TokenizerNode
name|builderNode
range|:
name|builderNodes
control|)
block|{
if|if
condition|(
operator|!
name|builderNode
operator|.
name|hasOccurrences
argument_list|()
condition|)
block|{
continue|continue;
block|}
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|builderNode
operator|.
name|getNumOccurrences
argument_list|()
argument_list|)
expr_stmt|;
comment|// we de-duped before adding to
comment|// builder
name|int
name|position
init|=
name|builderNode
operator|.
name|getOutputArrayOffset
argument_list|()
decl_stmt|;
name|byte
index|[]
name|output
init|=
name|reader
operator|.
name|populateBuffer
argument_list|(
name|position
argument_list|)
operator|.
name|copyBufferToNewArray
argument_list|()
decl_stmt|;
name|boolean
name|same
init|=
name|Bytes
operator|.
name|equals
argument_list|(
name|sortedUniqueColumns
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|deepCopyToNewArray
argument_list|()
argument_list|,
name|output
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|same
argument_list|)
expr_stmt|;
operator|++
name|i
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

