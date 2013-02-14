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
name|KeyValueTool
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
name|cell
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
name|cell
operator|.
name|CellTool
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
name|decode
operator|.
name|PrefixTreeArraySearcher
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
comment|/**    * Currently unused.    *<p/>    * TODO performance leak. should reuse the searchers. hbase does not currently have a hook where    * this can be called    */
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
name|KeyValueTool
operator|.
name|copyKeyToNewByteBuffer
argument_list|(
name|ptSearcher
operator|.
name|getCurrent
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
name|CellTool
operator|.
name|getValueBufferShallowCopy
argument_list|(
name|ptSearcher
operator|.
name|getCurrent
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
name|KeyValueTool
operator|.
name|copyToNewByteBuffer
argument_list|(
name|ptSearcher
operator|.
name|getCurrent
argument_list|()
argument_list|)
return|;
block|}
comment|/**    * currently must do deep copy into new array    */
annotation|@
name|Override
specifier|public
name|KeyValue
name|getKeyValue
parameter_list|()
block|{
return|return
name|KeyValueTool
operator|.
name|copyToNewKeyValue
argument_list|(
name|ptSearcher
operator|.
name|getCurrent
argument_list|()
argument_list|)
return|;
block|}
comment|/**    * Currently unused.    *<p/>    * A nice, lightweight reference, though the underlying cell is transient.  This method may return    * the same reference to the backing PrefixTreeCell repeatedly, while other implementations may    * return a different reference for each Cell.    *<p/>    * The goal will be to transition the upper layers of HBase, like Filters and KeyValueHeap, to use    * this method instead of the getKeyValue() methods above.    */
comment|//  @Override
specifier|public
name|Cell
name|getCurrent
parameter_list|()
block|{
return|return
name|ptSearcher
operator|.
name|getCurrent
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
name|next
argument_list|()
return|;
block|}
comment|//  @Override
specifier|public
name|boolean
name|advance
parameter_list|()
block|{
return|return
name|ptSearcher
operator|.
name|next
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
comment|/**    * Seek forward only (should be called reseekToKeyInBlock?).    *<p/>    * If the exact key is found look at the seekBefore variable and:<br/>    * - if true: go to the previous key if it's true<br/>    * - if false: stay on the exact key    *<p/>    * If the exact key is not found, then go to the previous key *if possible*, but remember to leave    * the scanner in a valid state if possible.    *<p/>    * @param keyOnlyBytes KeyValue format of a Cell's key at which to position the seeker    * @param offset offset into the keyOnlyBytes array    * @param length number of bytes of the keyOnlyBytes array to use    * @param forceBeforeOnExactMatch if an exact match is found and seekBefore=true, back up one Cell    * @return 0 if the seeker is on the exact key<br/>    *         1 if the seeker is not on the key for any reason, including seekBefore being true    */
annotation|@
name|Override
specifier|public
name|int
name|seekToKeyInBlock
parameter_list|(
name|byte
index|[]
name|keyOnlyBytes
parameter_list|,
name|int
name|offset
parameter_list|,
name|int
name|length
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
name|keyOnlyBytes
argument_list|,
name|offset
argument_list|,
name|length
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
name|keyOnlyBytes
argument_list|,
name|offset
argument_list|,
name|length
argument_list|,
name|forceBeforeOnExactMatch
argument_list|)
return|;
block|}
block|}
comment|/*    * Support both of these options since the underlying PrefixTree supports both.  Possibly    * expand the EncodedSeeker to utilize them both.    */
specifier|protected
name|int
name|seekToOrBeforeUsingPositionAtOrBefore
parameter_list|(
name|byte
index|[]
name|keyOnlyBytes
parameter_list|,
name|int
name|offset
parameter_list|,
name|int
name|length
parameter_list|,
name|boolean
name|forceBeforeOnExactMatch
parameter_list|)
block|{
comment|// this does a deep copy of the key byte[] because the CellSearcher interface wants a Cell
name|KeyValue
name|kv
init|=
name|KeyValue
operator|.
name|createKeyValueFromKey
argument_list|(
name|keyOnlyBytes
argument_list|,
name|offset
argument_list|,
name|length
argument_list|)
decl_stmt|;
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
name|forceBeforeOnExactMatch
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
name|byte
index|[]
name|keyOnlyBytes
parameter_list|,
name|int
name|offset
parameter_list|,
name|int
name|length
parameter_list|,
name|boolean
name|forceBeforeOnExactMatch
parameter_list|)
block|{
comment|// this does a deep copy of the key byte[] because the CellSearcher interface wants a Cell
name|KeyValue
name|kv
init|=
name|KeyValue
operator|.
name|createKeyValueFromKey
argument_list|(
name|keyOnlyBytes
argument_list|,
name|offset
argument_list|,
name|length
argument_list|)
decl_stmt|;
comment|//should probably switch this to use the seekForwardToOrBefore method
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
name|forceBeforeOnExactMatch
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
block|}
end_class

end_unit

