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
name|concurrent
operator|.
name|atomic
operator|.
name|AtomicInteger
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
name|yetus
operator|.
name|audience
operator|.
name|InterfaceAudience
import|;
end_import

begin_comment
comment|/**  * A MemStoreLAB implementation which wraps N MemStoreLABs. Its main duty is in proper managing the  * close of the individual MemStoreLAB. This is treated as an immutable one and so do not allow to  * add any more Cells into it. {@link #copyCellInto(Cell)} throws Exception  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|ImmutableMemStoreLAB
implements|implements
name|MemStoreLAB
block|{
specifier|private
specifier|final
name|AtomicInteger
name|openScannerCount
init|=
operator|new
name|AtomicInteger
argument_list|()
decl_stmt|;
specifier|private
specifier|volatile
name|boolean
name|closed
init|=
literal|false
decl_stmt|;
specifier|private
specifier|final
name|List
argument_list|<
name|MemStoreLAB
argument_list|>
name|mslabs
decl_stmt|;
specifier|public
name|ImmutableMemStoreLAB
parameter_list|(
name|List
argument_list|<
name|MemStoreLAB
argument_list|>
name|mslabs
parameter_list|)
block|{
name|this
operator|.
name|mslabs
operator|=
name|mslabs
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Cell
name|copyCellInto
parameter_list|(
name|Cell
name|cell
parameter_list|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"This is an Immutable MemStoreLAB."
argument_list|)
throw|;
block|}
comment|/* Creating chunk to be used as index chunk in CellChunkMap, part of the chunks array.   ** Returning a new chunk, without replacing current chunk,   ** meaning MSLABImpl does not make the returned chunk as CurChunk.   ** The space on this chunk will be allocated externally.   ** The interface is only for external callers   */
annotation|@
name|Override
specifier|public
name|Chunk
name|getNewExternalChunk
parameter_list|()
block|{
name|MemStoreLAB
name|mslab
init|=
name|this
operator|.
name|mslabs
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
return|return
name|mslab
operator|.
name|getNewExternalChunk
argument_list|()
return|;
block|}
comment|/* Creating chunk to be used as data chunk in CellChunkMap.   ** This chunk is bigger the normal constant chunk size, and thus called JumboChunk it is used for   ** jumbo cells (which size is bigger than normal chunks).   ** Jumbo Chunks are needed only for CCM and thus are created only in   ** CompactingMemStore.IndexType.CHUNK_MAP type.   ** Returning a new chunk, without replacing current chunk,   ** meaning MSLABImpl does not make the returned chunk as CurChunk.   ** The space on this chunk will be allocated externally.   ** The interface is only for external callers   */
annotation|@
name|Override
specifier|public
name|Chunk
name|getNewExternalJumboChunk
parameter_list|(
name|int
name|size
parameter_list|)
block|{
name|MemStoreLAB
name|mslab
init|=
name|this
operator|.
name|mslabs
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
return|return
name|mslab
operator|.
name|getNewExternalJumboChunk
argument_list|(
name|size
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|()
block|{
comment|// 'openScannerCount' here tracks the scanners opened on segments which directly refer to this
comment|// MSLAB. The individual MSLABs this refers also having its own 'openScannerCount'. The usage of
comment|// the variable in close() and decScannerCount() is as as that in HeapMemstoreLAB. Here the
comment|// close just delegates the call to the individual MSLABs. The actual return of the chunks to
comment|// MSLABPool will happen within individual MSLABs only (which is at the leaf level).
comment|// Say an ImmutableMemStoreLAB is created over 2 HeapMemStoreLABs at some point and at that time
comment|// both of them were referred by ongoing scanners. So they have> 0 'openScannerCount'. Now over
comment|// the new Segment some scanners come in and this MSLABs 'openScannerCount' also goes up and
comment|// then come down on finish of scanners. Now a close() call comes to this Immutable MSLAB. As
comment|// it's 'openScannerCount' is zero it will call close() on both of the Heap MSLABs. Say by that
comment|// time the old scanners on one of the MSLAB got over where as on the other, still an old
comment|// scanner is going on. The call close() on that MSLAB will not close it immediately but will
comment|// just mark it for closure as it's 'openScannerCount' still> 0. Later once the old scan is
comment|// over, the decScannerCount() call will do the actual close and return of the chunks.
name|this
operator|.
name|closed
operator|=
literal|true
expr_stmt|;
comment|// When there are still on going scanners over this MSLAB, we will defer the close until all
comment|// scanners finish. We will just mark it for closure. See #decScannerCount(). This will be
comment|// called at end of every scan. When it is marked for closure and scanner count reached 0, we
comment|// will do the actual close then.
name|checkAndCloseMSLABs
argument_list|(
name|openScannerCount
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|checkAndCloseMSLABs
parameter_list|(
name|int
name|openScanners
parameter_list|)
block|{
if|if
condition|(
name|openScanners
operator|==
literal|0
condition|)
block|{
for|for
control|(
name|MemStoreLAB
name|mslab
range|:
name|this
operator|.
name|mslabs
control|)
block|{
name|mslab
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|incScannerCount
parameter_list|()
block|{
name|this
operator|.
name|openScannerCount
operator|.
name|incrementAndGet
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|decScannerCount
parameter_list|()
block|{
name|int
name|count
init|=
name|this
operator|.
name|openScannerCount
operator|.
name|decrementAndGet
argument_list|()
decl_stmt|;
if|if
condition|(
name|this
operator|.
name|closed
condition|)
block|{
name|checkAndCloseMSLABs
argument_list|(
name|count
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

