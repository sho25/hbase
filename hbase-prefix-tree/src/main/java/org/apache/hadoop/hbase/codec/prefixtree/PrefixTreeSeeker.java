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
package|;
end_package

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
name|CellComparator
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
name|CellUtil
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
operator|.
name|Type
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
name|SettableSequenceId
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
name|classification
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
name|hadoop
operator|.
name|hbase
operator|.
name|io
operator|.
name|HeapSize
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
name|io
operator|.
name|encoding
operator|.
name|DataBlockEncoder
operator|.
name|EncodedSeeker
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
name|ClassSize
import|;
end_import

begin_comment
comment|/**  * These methods have the same definition as any implementation of the EncodedSeeker.  *  * In the future, the EncodedSeeker could be modified to work with the Cell interface directly.  It  * currently returns a new KeyValue object each time getKeyValue is called.  This is not horrible,  * but in order to create a new KeyValue object, we must first allocate a new byte[] and copy in  * the data from the PrefixTreeCell.  It is somewhat heavyweight right now.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|PrefixTreeSeeker
implements|implements
name|EncodedSeeker
block|{
specifier|protected
name|ByteBuffer
name|block
decl_stmt|;
specifier|protected
name|boolean
name|includeMvccVersion
decl_stmt|;
specifier|protected
name|PrefixTreeArraySearcher
name|ptSearcher
decl_stmt|;
specifier|public
name|PrefixTreeSeeker
parameter_list|(
name|boolean
name|includeMvccVersion
parameter_list|)
block|{
name|this
operator|.
name|includeMvccVersion
operator|=
name|includeMvccVersion
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setCurrentBuffer
parameter_list|(
name|ByteBuffer
name|fullBlockBuffer
parameter_list|)
block|{
name|block
operator|=
name|fullBlockBuffer
expr_stmt|;
name|ptSearcher
operator|=
name|DecoderFactory
operator|.
name|checkOut
argument_list|(
name|block
argument_list|,
name|includeMvccVersion
argument_list|)
expr_stmt|;
name|rewind
argument_list|()
expr_stmt|;
block|}
comment|/**    *<p>    * Currently unused.    *</p>    * TODO performance leak. should reuse the searchers. hbase does not currently have a hook where    * this can be called    */
specifier|public
name|void
name|releaseCurrentSearcher
parameter_list|()
block|{
name|DecoderFactory
operator|.
name|checkIn
argument_list|(
name|ptSearcher
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|ByteBuffer
name|getKeyDeepCopy
parameter_list|()
block|{
return|return
name|KeyValueUtil
operator|.
name|copyKeyToNewByteBuffer
argument_list|(
name|ptSearcher
operator|.
name|current
argument_list|()
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|ByteBuffer
name|getValueShallowCopy
parameter_list|()
block|{
return|return
name|CellUtil
operator|.
name|getValueBufferShallowCopy
argument_list|(
name|ptSearcher
operator|.
name|current
argument_list|()
argument_list|)
return|;
block|}
comment|/**    * currently must do deep copy into new array    */
annotation|@
name|Override
specifier|public
name|ByteBuffer
name|getKeyValueBuffer
parameter_list|()
block|{
return|return
name|KeyValueUtil
operator|.
name|copyToNewByteBuffer
argument_list|(
name|ptSearcher
operator|.
name|current
argument_list|()
argument_list|)
return|;
block|}
comment|/**    * currently must do deep copy into new array    */
annotation|@
name|Override
specifier|public
name|Cell
name|getKeyValue
parameter_list|()
block|{
name|Cell
name|cell
init|=
name|ptSearcher
operator|.
name|current
argument_list|()
decl_stmt|;
if|if
condition|(
name|cell
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
return|return
operator|new
name|ClonedPrefixTreeCell
argument_list|(
name|cell
operator|.
name|getRowArray
argument_list|()
argument_list|,
name|cell
operator|.
name|getRowOffset
argument_list|()
argument_list|,
name|cell
operator|.
name|getRowLength
argument_list|()
argument_list|,
name|cell
operator|.
name|getFamilyArray
argument_list|()
argument_list|,
name|cell
operator|.
name|getFamilyOffset
argument_list|()
argument_list|,
name|cell
operator|.
name|getFamilyLength
argument_list|()
argument_list|,
name|cell
operator|.
name|getQualifierArray
argument_list|()
argument_list|,
name|cell
operator|.
name|getQualifierOffset
argument_list|()
argument_list|,
name|cell
operator|.
name|getQualifierLength
argument_list|()
argument_list|,
name|cell
operator|.
name|getValueArray
argument_list|()
argument_list|,
name|cell
operator|.
name|getValueOffset
argument_list|()
argument_list|,
name|cell
operator|.
name|getValueLength
argument_list|()
argument_list|,
name|cell
operator|.
name|getTagsArray
argument_list|()
argument_list|,
name|cell
operator|.
name|getTagsOffset
argument_list|()
argument_list|,
name|cell
operator|.
name|getTagsLength
argument_list|()
argument_list|,
name|cell
operator|.
name|getTimestamp
argument_list|()
argument_list|,
name|cell
operator|.
name|getTypeByte
argument_list|()
argument_list|,
name|cell
operator|.
name|getSequenceId
argument_list|()
argument_list|)
return|;
block|}
comment|/**    *<p>    * Currently unused.    *</p><p>    * A nice, lightweight reference, though the underlying cell is transient. This method may return    * the same reference to the backing PrefixTreeCell repeatedly, while other implementations may    * return a different reference for each Cell.    *</p>    * The goal will be to transition the upper layers of HBase, like Filters and KeyValueHeap, to    * use this method instead of the getKeyValue() methods above.    */
specifier|public
name|Cell
name|get
parameter_list|()
block|{
return|return
name|ptSearcher
operator|.
name|current
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|rewind
parameter_list|()
block|{
name|ptSearcher
operator|.
name|positionAtFirstCell
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|next
parameter_list|()
block|{
return|return
name|ptSearcher
operator|.
name|advance
argument_list|()
return|;
block|}
specifier|public
name|boolean
name|advance
parameter_list|()
block|{
return|return
name|ptSearcher
operator|.
name|advance
argument_list|()
return|;
block|}
specifier|private
specifier|static
specifier|final
name|boolean
name|USE_POSITION_BEFORE
init|=
literal|false
decl_stmt|;
comment|/*    * Support both of these options since the underlying PrefixTree supports    * both. Possibly expand the EncodedSeeker to utilize them both.    */
specifier|protected
name|int
name|seekToOrBeforeUsingPositionAtOrBefore
parameter_list|(
name|Cell
name|kv
parameter_list|,
name|boolean
name|seekBefore
parameter_list|)
block|{
comment|// this does a deep copy of the key byte[] because the CellSearcher
comment|// interface wants a Cell
name|CellScannerPosition
name|position
init|=
name|ptSearcher
operator|.
name|seekForwardToOrBefore
argument_list|(
name|kv
argument_list|)
decl_stmt|;
if|if
condition|(
name|CellScannerPosition
operator|.
name|AT
operator|==
name|position
condition|)
block|{
if|if
condition|(
name|seekBefore
condition|)
block|{
name|ptSearcher
operator|.
name|previous
argument_list|()
expr_stmt|;
return|return
literal|1
return|;
block|}
return|return
literal|0
return|;
block|}
return|return
literal|1
return|;
block|}
specifier|protected
name|int
name|seekToOrBeforeUsingPositionAtOrAfter
parameter_list|(
name|Cell
name|kv
parameter_list|,
name|boolean
name|seekBefore
parameter_list|)
block|{
comment|// should probably switch this to use the seekForwardToOrBefore method
name|CellScannerPosition
name|position
init|=
name|ptSearcher
operator|.
name|seekForwardToOrAfter
argument_list|(
name|kv
argument_list|)
decl_stmt|;
if|if
condition|(
name|CellScannerPosition
operator|.
name|AT
operator|==
name|position
condition|)
block|{
if|if
condition|(
name|seekBefore
condition|)
block|{
name|ptSearcher
operator|.
name|previous
argument_list|()
expr_stmt|;
return|return
literal|1
return|;
block|}
return|return
literal|0
return|;
block|}
if|if
condition|(
name|CellScannerPosition
operator|.
name|AFTER
operator|==
name|position
condition|)
block|{
if|if
condition|(
operator|!
name|ptSearcher
operator|.
name|isBeforeFirst
argument_list|()
condition|)
block|{
name|ptSearcher
operator|.
name|previous
argument_list|()
expr_stmt|;
block|}
return|return
literal|1
return|;
block|}
if|if
condition|(
name|position
operator|==
name|CellScannerPosition
operator|.
name|AFTER_LAST
condition|)
block|{
if|if
condition|(
name|seekBefore
condition|)
block|{
name|ptSearcher
operator|.
name|previous
argument_list|()
expr_stmt|;
block|}
return|return
literal|1
return|;
block|}
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"unexpected CellScannerPosition:"
operator|+
name|position
argument_list|)
throw|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|seekToKeyInBlock
parameter_list|(
name|Cell
name|key
parameter_list|,
name|boolean
name|forceBeforeOnExactMatch
parameter_list|)
block|{
if|if
condition|(
name|USE_POSITION_BEFORE
condition|)
block|{
return|return
name|seekToOrBeforeUsingPositionAtOrBefore
argument_list|(
name|key
argument_list|,
name|forceBeforeOnExactMatch
argument_list|)
return|;
block|}
else|else
block|{
return|return
name|seekToOrBeforeUsingPositionAtOrAfter
argument_list|(
name|key
argument_list|,
name|forceBeforeOnExactMatch
argument_list|)
return|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|int
name|compareKey
parameter_list|(
name|CellComparator
name|comparator
parameter_list|,
name|Cell
name|key
parameter_list|)
block|{
return|return
name|comparator
operator|.
name|compare
argument_list|(
name|key
argument_list|,
name|ptSearcher
operator|.
name|current
argument_list|()
argument_list|)
return|;
block|}
comment|/**    * Cloned version of the PrefixTreeCell where except the value part, the rest    * of the key part is deep copied    *    */
specifier|private
specifier|static
class|class
name|ClonedPrefixTreeCell
implements|implements
name|Cell
implements|,
name|SettableSequenceId
implements|,
name|HeapSize
block|{
specifier|private
specifier|static
specifier|final
name|long
name|FIXED_OVERHEAD
init|=
name|ClassSize
operator|.
name|align
argument_list|(
name|ClassSize
operator|.
name|OBJECT
operator|+
operator|(
literal|5
operator|*
name|ClassSize
operator|.
name|REFERENCE
operator|)
operator|+
operator|(
literal|2
operator|*
name|Bytes
operator|.
name|SIZEOF_LONG
operator|)
operator|+
operator|(
literal|4
operator|*
name|Bytes
operator|.
name|SIZEOF_INT
operator|)
operator|+
operator|(
name|Bytes
operator|.
name|SIZEOF_SHORT
operator|)
operator|+
operator|(
literal|2
operator|*
name|Bytes
operator|.
name|SIZEOF_BYTE
operator|)
operator|+
operator|(
literal|5
operator|*
name|ClassSize
operator|.
name|ARRAY
operator|)
argument_list|)
decl_stmt|;
specifier|private
name|byte
index|[]
name|row
decl_stmt|;
specifier|private
name|short
name|rowLength
decl_stmt|;
specifier|private
name|byte
index|[]
name|fam
decl_stmt|;
specifier|private
name|byte
name|famLength
decl_stmt|;
specifier|private
name|byte
index|[]
name|qual
decl_stmt|;
specifier|private
name|int
name|qualLength
decl_stmt|;
specifier|private
name|byte
index|[]
name|val
decl_stmt|;
specifier|private
name|int
name|valOffset
decl_stmt|;
specifier|private
name|int
name|valLength
decl_stmt|;
specifier|private
name|byte
index|[]
name|tag
decl_stmt|;
specifier|private
name|int
name|tagsLength
decl_stmt|;
specifier|private
name|long
name|ts
decl_stmt|;
specifier|private
name|long
name|seqId
decl_stmt|;
specifier|private
name|byte
name|type
decl_stmt|;
specifier|public
name|ClonedPrefixTreeCell
parameter_list|(
name|byte
index|[]
name|row
parameter_list|,
name|int
name|rowOffset
parameter_list|,
name|short
name|rowLength
parameter_list|,
name|byte
index|[]
name|fam
parameter_list|,
name|int
name|famOffset
parameter_list|,
name|byte
name|famLength
parameter_list|,
name|byte
index|[]
name|qual
parameter_list|,
name|int
name|qualOffset
parameter_list|,
name|int
name|qualLength
parameter_list|,
name|byte
index|[]
name|val
parameter_list|,
name|int
name|valOffset
parameter_list|,
name|int
name|valLength
parameter_list|,
name|byte
index|[]
name|tag
parameter_list|,
name|int
name|tagOffset
parameter_list|,
name|int
name|tagLength
parameter_list|,
name|long
name|ts
parameter_list|,
name|byte
name|type
parameter_list|,
name|long
name|seqId
parameter_list|)
block|{
name|this
operator|.
name|row
operator|=
operator|new
name|byte
index|[
name|rowLength
index|]
expr_stmt|;
name|System
operator|.
name|arraycopy
argument_list|(
name|row
argument_list|,
name|rowOffset
argument_list|,
name|this
operator|.
name|row
argument_list|,
literal|0
argument_list|,
name|rowLength
argument_list|)
expr_stmt|;
name|this
operator|.
name|rowLength
operator|=
name|rowLength
expr_stmt|;
name|this
operator|.
name|fam
operator|=
operator|new
name|byte
index|[
name|famLength
index|]
expr_stmt|;
name|System
operator|.
name|arraycopy
argument_list|(
name|fam
argument_list|,
name|famOffset
argument_list|,
name|this
operator|.
name|fam
argument_list|,
literal|0
argument_list|,
name|famLength
argument_list|)
expr_stmt|;
name|this
operator|.
name|famLength
operator|=
name|famLength
expr_stmt|;
name|this
operator|.
name|qual
operator|=
operator|new
name|byte
index|[
name|qualLength
index|]
expr_stmt|;
name|System
operator|.
name|arraycopy
argument_list|(
name|qual
argument_list|,
name|qualOffset
argument_list|,
name|this
operator|.
name|qual
argument_list|,
literal|0
argument_list|,
name|qualLength
argument_list|)
expr_stmt|;
name|this
operator|.
name|qualLength
operator|=
name|qualLength
expr_stmt|;
name|this
operator|.
name|tag
operator|=
operator|new
name|byte
index|[
name|tagLength
index|]
expr_stmt|;
name|System
operator|.
name|arraycopy
argument_list|(
name|tag
argument_list|,
name|tagOffset
argument_list|,
name|this
operator|.
name|tag
argument_list|,
literal|0
argument_list|,
name|tagLength
argument_list|)
expr_stmt|;
name|this
operator|.
name|tagsLength
operator|=
name|tagLength
expr_stmt|;
name|this
operator|.
name|val
operator|=
name|val
expr_stmt|;
name|this
operator|.
name|valLength
operator|=
name|valLength
expr_stmt|;
name|this
operator|.
name|valOffset
operator|=
name|valOffset
expr_stmt|;
name|this
operator|.
name|ts
operator|=
name|ts
expr_stmt|;
name|this
operator|.
name|seqId
operator|=
name|seqId
expr_stmt|;
name|this
operator|.
name|type
operator|=
name|type
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setSequenceId
parameter_list|(
name|long
name|seqId
parameter_list|)
block|{
name|this
operator|.
name|seqId
operator|=
name|seqId
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|byte
index|[]
name|getRowArray
parameter_list|()
block|{
return|return
name|this
operator|.
name|row
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getRowOffset
parameter_list|()
block|{
return|return
literal|0
return|;
block|}
annotation|@
name|Override
specifier|public
name|short
name|getRowLength
parameter_list|()
block|{
return|return
name|this
operator|.
name|rowLength
return|;
block|}
annotation|@
name|Override
specifier|public
name|byte
index|[]
name|getFamilyArray
parameter_list|()
block|{
return|return
name|this
operator|.
name|fam
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getFamilyOffset
parameter_list|()
block|{
return|return
literal|0
return|;
block|}
annotation|@
name|Override
specifier|public
name|byte
name|getFamilyLength
parameter_list|()
block|{
return|return
name|this
operator|.
name|famLength
return|;
block|}
annotation|@
name|Override
specifier|public
name|byte
index|[]
name|getQualifierArray
parameter_list|()
block|{
return|return
name|this
operator|.
name|qual
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getQualifierOffset
parameter_list|()
block|{
return|return
literal|0
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getQualifierLength
parameter_list|()
block|{
return|return
name|this
operator|.
name|qualLength
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getTimestamp
parameter_list|()
block|{
return|return
name|ts
return|;
block|}
annotation|@
name|Override
specifier|public
name|byte
name|getTypeByte
parameter_list|()
block|{
return|return
name|type
return|;
block|}
annotation|@
name|Override
annotation|@
name|Deprecated
specifier|public
name|long
name|getMvccVersion
parameter_list|()
block|{
return|return
name|getSequenceId
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getSequenceId
parameter_list|()
block|{
return|return
name|seqId
return|;
block|}
annotation|@
name|Override
specifier|public
name|byte
index|[]
name|getValueArray
parameter_list|()
block|{
return|return
name|val
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getValueOffset
parameter_list|()
block|{
return|return
name|this
operator|.
name|valOffset
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getValueLength
parameter_list|()
block|{
return|return
name|this
operator|.
name|valLength
return|;
block|}
annotation|@
name|Override
specifier|public
name|byte
index|[]
name|getTagsArray
parameter_list|()
block|{
return|return
name|this
operator|.
name|tag
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getTagsOffset
parameter_list|()
block|{
return|return
literal|0
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getTagsLength
parameter_list|()
block|{
return|return
name|this
operator|.
name|tagsLength
return|;
block|}
annotation|@
name|Override
annotation|@
name|Deprecated
specifier|public
name|byte
index|[]
name|getValue
parameter_list|()
block|{
return|return
name|this
operator|.
name|val
return|;
block|}
annotation|@
name|Override
annotation|@
name|Deprecated
specifier|public
name|byte
index|[]
name|getFamily
parameter_list|()
block|{
return|return
name|this
operator|.
name|fam
return|;
block|}
annotation|@
name|Override
annotation|@
name|Deprecated
specifier|public
name|byte
index|[]
name|getQualifier
parameter_list|()
block|{
return|return
name|this
operator|.
name|qual
return|;
block|}
annotation|@
name|Override
annotation|@
name|Deprecated
specifier|public
name|byte
index|[]
name|getRow
parameter_list|()
block|{
return|return
name|this
operator|.
name|row
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
name|String
name|row
init|=
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|getRowArray
argument_list|()
argument_list|,
name|getRowOffset
argument_list|()
argument_list|,
name|getRowLength
argument_list|()
argument_list|)
decl_stmt|;
name|String
name|family
init|=
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|getFamilyArray
argument_list|()
argument_list|,
name|getFamilyOffset
argument_list|()
argument_list|,
name|getFamilyLength
argument_list|()
argument_list|)
decl_stmt|;
name|String
name|qualifier
init|=
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|getQualifierArray
argument_list|()
argument_list|,
name|getQualifierOffset
argument_list|()
argument_list|,
name|getQualifierLength
argument_list|()
argument_list|)
decl_stmt|;
name|String
name|timestamp
init|=
name|String
operator|.
name|valueOf
argument_list|(
operator|(
name|getTimestamp
argument_list|()
operator|)
argument_list|)
decl_stmt|;
return|return
name|row
operator|+
literal|"/"
operator|+
name|family
operator|+
operator|(
name|family
operator|!=
literal|null
operator|&&
name|family
operator|.
name|length
argument_list|()
operator|>
literal|0
condition|?
literal|":"
else|:
literal|""
operator|)
operator|+
name|qualifier
operator|+
literal|"/"
operator|+
name|timestamp
operator|+
literal|"/"
operator|+
name|Type
operator|.
name|codeToType
argument_list|(
name|type
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|heapSize
parameter_list|()
block|{
return|return
name|FIXED_OVERHEAD
operator|+
name|rowLength
operator|+
name|famLength
operator|+
name|qualLength
operator|+
name|valLength
operator|+
name|tagsLength
return|;
block|}
block|}
block|}
end_class

end_unit

