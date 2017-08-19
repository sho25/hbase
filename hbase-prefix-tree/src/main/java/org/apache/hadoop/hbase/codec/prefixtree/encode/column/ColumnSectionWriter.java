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
name|encode
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
name|IOException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|OutputStream
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
name|org
operator|.
name|apache
operator|.
name|yetus
operator|.
name|audience
operator|.
name|InterfaceAudience
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
name|CollectionUtils
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
name|vint
operator|.
name|UFIntTool
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

begin_comment
comment|/**  *<p>  * Takes the tokenized family or qualifier data and flattens it into a stream of bytes. The family  * section is written after the row section, and qualifier section after family section.  *</p>  * The family and qualifier tries, or "column tries", are structured differently than the row trie.  * The trie cannot be reassembled without external data about the offsets of the leaf nodes, and  * these external pointers are stored in the nubs and leaves of the row trie. For each cell in a  * row, the row trie contains a list of offsets into the column sections (along with pointers to  * timestamps and other per-cell fields). These offsets point to the last column node/token that  * comprises the column name. To assemble the column name, the trie is traversed in reverse (right  * to left), with the rightmost tokens pointing to the start of their "parent" node which is the  * node to the left.  *<p>  * This choice was made to reduce the size of the column trie by storing the minimum amount of  * offset data. As a result, to find a specific qualifier within a row, you must do a binary search  * of the column nodes, reassembling each one as you search. Future versions of the PrefixTree might  * encode the columns in both a forward and reverse trie, which would convert binary searches into  * more efficient trie searches which would be beneficial for wide rows.  *</p>  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|ColumnSectionWriter
block|{
specifier|public
specifier|static
specifier|final
name|int
name|EXPECTED_NUBS_PLUS_LEAVES
init|=
literal|100
decl_stmt|;
comment|/****************** fields ****************************/
specifier|private
name|PrefixTreeBlockMeta
name|blockMeta
decl_stmt|;
specifier|private
name|ColumnNodeType
name|nodeType
decl_stmt|;
specifier|private
name|Tokenizer
name|tokenizer
decl_stmt|;
specifier|private
name|int
name|numBytes
init|=
literal|0
decl_stmt|;
specifier|private
name|ArrayList
argument_list|<
name|TokenizerNode
argument_list|>
name|nonLeaves
decl_stmt|;
specifier|private
name|ArrayList
argument_list|<
name|TokenizerNode
argument_list|>
name|leaves
decl_stmt|;
specifier|private
name|ArrayList
argument_list|<
name|TokenizerNode
argument_list|>
name|allNodes
decl_stmt|;
specifier|private
name|ArrayList
argument_list|<
name|ColumnNodeWriter
argument_list|>
name|columnNodeWriters
decl_stmt|;
specifier|private
name|List
argument_list|<
name|Integer
argument_list|>
name|outputArrayOffsets
decl_stmt|;
comment|/*********************** construct *********************/
specifier|public
name|ColumnSectionWriter
parameter_list|()
block|{
name|this
operator|.
name|nonLeaves
operator|=
name|Lists
operator|.
name|newArrayList
argument_list|()
expr_stmt|;
name|this
operator|.
name|leaves
operator|=
name|Lists
operator|.
name|newArrayList
argument_list|()
expr_stmt|;
name|this
operator|.
name|outputArrayOffsets
operator|=
name|Lists
operator|.
name|newArrayList
argument_list|()
expr_stmt|;
block|}
specifier|public
name|ColumnSectionWriter
parameter_list|(
name|PrefixTreeBlockMeta
name|blockMeta
parameter_list|,
name|Tokenizer
name|builder
parameter_list|,
name|ColumnNodeType
name|nodeType
parameter_list|)
block|{
name|this
argument_list|()
expr_stmt|;
comment|// init collections
name|reconstruct
argument_list|(
name|blockMeta
argument_list|,
name|builder
argument_list|,
name|nodeType
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|reconstruct
parameter_list|(
name|PrefixTreeBlockMeta
name|blockMeta
parameter_list|,
name|Tokenizer
name|builder
parameter_list|,
name|ColumnNodeType
name|nodeType
parameter_list|)
block|{
name|this
operator|.
name|blockMeta
operator|=
name|blockMeta
expr_stmt|;
name|this
operator|.
name|tokenizer
operator|=
name|builder
expr_stmt|;
name|this
operator|.
name|nodeType
operator|=
name|nodeType
expr_stmt|;
block|}
specifier|public
name|void
name|reset
parameter_list|()
block|{
name|numBytes
operator|=
literal|0
expr_stmt|;
name|nonLeaves
operator|.
name|clear
argument_list|()
expr_stmt|;
name|leaves
operator|.
name|clear
argument_list|()
expr_stmt|;
name|outputArrayOffsets
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
comment|/****************** methods *******************************/
specifier|public
name|ColumnSectionWriter
name|compile
parameter_list|()
block|{
if|if
condition|(
name|this
operator|.
name|nodeType
operator|==
name|ColumnNodeType
operator|.
name|FAMILY
condition|)
block|{
comment|// do nothing. max family length fixed at Byte.MAX_VALUE
block|}
elseif|else
if|if
condition|(
name|this
operator|.
name|nodeType
operator|==
name|ColumnNodeType
operator|.
name|QUALIFIER
condition|)
block|{
name|blockMeta
operator|.
name|setMaxQualifierLength
argument_list|(
name|tokenizer
operator|.
name|getMaxElementLength
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|blockMeta
operator|.
name|setMaxTagsLength
argument_list|(
name|tokenizer
operator|.
name|getMaxElementLength
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|compilerInternals
argument_list|()
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|protected
name|void
name|compilerInternals
parameter_list|()
block|{
name|tokenizer
operator|.
name|setNodeFirstInsertionIndexes
argument_list|()
expr_stmt|;
name|tokenizer
operator|.
name|appendNodes
argument_list|(
name|nonLeaves
argument_list|,
literal|true
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|tokenizer
operator|.
name|appendNodes
argument_list|(
name|leaves
argument_list|,
literal|false
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|allNodes
operator|=
name|Lists
operator|.
name|newArrayListWithCapacity
argument_list|(
name|nonLeaves
operator|.
name|size
argument_list|()
operator|+
name|leaves
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|allNodes
operator|.
name|addAll
argument_list|(
name|nonLeaves
argument_list|)
expr_stmt|;
name|allNodes
operator|.
name|addAll
argument_list|(
name|leaves
argument_list|)
expr_stmt|;
name|columnNodeWriters
operator|=
name|Lists
operator|.
name|newArrayListWithCapacity
argument_list|(
name|CollectionUtils
operator|.
name|nullSafeSize
argument_list|(
name|allNodes
argument_list|)
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
name|allNodes
operator|.
name|size
argument_list|()
condition|;
operator|++
name|i
control|)
block|{
name|TokenizerNode
name|node
init|=
name|allNodes
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|columnNodeWriters
operator|.
name|add
argument_list|(
operator|new
name|ColumnNodeWriter
argument_list|(
name|blockMeta
argument_list|,
name|node
argument_list|,
name|this
operator|.
name|nodeType
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// leaf widths are known at this point, so add them up
name|int
name|totalBytesWithoutOffsets
init|=
literal|0
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
name|allNodes
operator|.
name|size
argument_list|()
operator|-
literal|1
init|;
name|i
operator|>=
literal|0
condition|;
operator|--
name|i
control|)
block|{
name|ColumnNodeWriter
name|columnNodeWriter
init|=
name|columnNodeWriters
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
comment|// leaves store all but their first token byte
name|totalBytesWithoutOffsets
operator|+=
name|columnNodeWriter
operator|.
name|getWidthUsingPlaceholderForOffsetWidth
argument_list|(
literal|0
argument_list|)
expr_stmt|;
block|}
comment|// figure out how wide our offset FInts are
name|int
name|parentOffsetWidth
init|=
literal|0
decl_stmt|;
while|while
condition|(
literal|true
condition|)
block|{
operator|++
name|parentOffsetWidth
expr_stmt|;
name|int
name|numBytesFinder
init|=
name|totalBytesWithoutOffsets
operator|+
name|parentOffsetWidth
operator|*
name|allNodes
operator|.
name|size
argument_list|()
decl_stmt|;
if|if
condition|(
name|numBytesFinder
operator|<
name|UFIntTool
operator|.
name|maxValueForNumBytes
argument_list|(
name|parentOffsetWidth
argument_list|)
condition|)
block|{
name|numBytes
operator|=
name|numBytesFinder
expr_stmt|;
break|break;
block|}
comment|// it fits
block|}
if|if
condition|(
name|this
operator|.
name|nodeType
operator|==
name|ColumnNodeType
operator|.
name|FAMILY
condition|)
block|{
name|blockMeta
operator|.
name|setFamilyOffsetWidth
argument_list|(
name|parentOffsetWidth
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|this
operator|.
name|nodeType
operator|==
name|ColumnNodeType
operator|.
name|QUALIFIER
condition|)
block|{
name|blockMeta
operator|.
name|setQualifierOffsetWidth
argument_list|(
name|parentOffsetWidth
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|blockMeta
operator|.
name|setTagsOffsetWidth
argument_list|(
name|parentOffsetWidth
argument_list|)
expr_stmt|;
block|}
name|int
name|forwardIndex
init|=
literal|0
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
name|allNodes
operator|.
name|size
argument_list|()
condition|;
operator|++
name|i
control|)
block|{
name|TokenizerNode
name|node
init|=
name|allNodes
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|ColumnNodeWriter
name|columnNodeWriter
init|=
name|columnNodeWriters
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|int
name|fullNodeWidth
init|=
name|columnNodeWriter
operator|.
name|getWidthUsingPlaceholderForOffsetWidth
argument_list|(
name|parentOffsetWidth
argument_list|)
decl_stmt|;
name|node
operator|.
name|setOutputArrayOffset
argument_list|(
name|forwardIndex
argument_list|)
expr_stmt|;
name|columnNodeWriter
operator|.
name|setTokenBytes
argument_list|(
name|node
operator|.
name|getToken
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|node
operator|.
name|isRoot
argument_list|()
condition|)
block|{
name|columnNodeWriter
operator|.
name|setParentStartPosition
argument_list|(
literal|0
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|columnNodeWriter
operator|.
name|setParentStartPosition
argument_list|(
name|node
operator|.
name|getParent
argument_list|()
operator|.
name|getOutputArrayOffset
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|forwardIndex
operator|+=
name|fullNodeWidth
expr_stmt|;
block|}
name|tokenizer
operator|.
name|appendOutputArrayOffsets
argument_list|(
name|outputArrayOffsets
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|writeBytes
parameter_list|(
name|OutputStream
name|os
parameter_list|)
throws|throws
name|IOException
block|{
for|for
control|(
name|ColumnNodeWriter
name|columnNodeWriter
range|:
name|columnNodeWriters
control|)
block|{
name|columnNodeWriter
operator|.
name|writeBytes
argument_list|(
name|os
argument_list|)
expr_stmt|;
block|}
block|}
comment|/************* get/set **************************/
specifier|public
name|ArrayList
argument_list|<
name|ColumnNodeWriter
argument_list|>
name|getColumnNodeWriters
parameter_list|()
block|{
return|return
name|columnNodeWriters
return|;
block|}
specifier|public
name|int
name|getNumBytes
parameter_list|()
block|{
return|return
name|numBytes
return|;
block|}
specifier|public
name|int
name|getOutputArrayOffset
parameter_list|(
name|int
name|sortedIndex
parameter_list|)
block|{
return|return
name|outputArrayOffsets
operator|.
name|get
argument_list|(
name|sortedIndex
argument_list|)
return|;
block|}
specifier|public
name|ArrayList
argument_list|<
name|TokenizerNode
argument_list|>
name|getNonLeaves
parameter_list|()
block|{
return|return
name|nonLeaves
return|;
block|}
specifier|public
name|ArrayList
argument_list|<
name|TokenizerNode
argument_list|>
name|getLeaves
parameter_list|()
block|{
return|return
name|leaves
return|;
block|}
block|}
end_class

end_unit

