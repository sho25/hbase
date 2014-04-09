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
name|Comparator
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
name|PriorityQueue
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
operator|.
name|KVComparator
import|;
end_import

begin_comment
comment|/**  * Implements a heap merge across any number of KeyValueScanners.  *<p>  * Implements KeyValueScanner itself.  *<p>  * This class is used at the Region level to merge across Stores  * and at the Store level to merge across the memstore and StoreFiles.  *<p>  * In the Region case, we also need InternalScanner.next(List), so this class  * also implements InternalScanner.  WARNING: As is, if you try to use this  * as an InternalScanner at the Store level, you will get runtime exceptions.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|KeyValueHeap
extends|extends
name|NonReversedNonLazyKeyValueScanner
implements|implements
name|KeyValueScanner
implements|,
name|InternalScanner
block|{
specifier|protected
name|PriorityQueue
argument_list|<
name|KeyValueScanner
argument_list|>
name|heap
init|=
literal|null
decl_stmt|;
comment|/**    * The current sub-scanner, i.e. the one that contains the next key/value    * to return to the client. This scanner is NOT included in {@link #heap}    * (but we frequently add it back to the heap and pull the new winner out).    * We maintain an invariant that the current sub-scanner has already done    * a real seek, and that current.peek() is always a real key/value (or null)    * except for the fake last-key-on-row-column supplied by the multi-column    * Bloom filter optimization, which is OK to propagate to StoreScanner. In    * order to ensure that, always use {@link #pollRealKV()} to update current.    */
specifier|protected
name|KeyValueScanner
name|current
init|=
literal|null
decl_stmt|;
specifier|protected
name|KVScannerComparator
name|comparator
decl_stmt|;
comment|/**    * Constructor.  This KeyValueHeap will handle closing of passed in    * KeyValueScanners.    * @param scanners    * @param comparator    */
specifier|public
name|KeyValueHeap
parameter_list|(
name|List
argument_list|<
name|?
extends|extends
name|KeyValueScanner
argument_list|>
name|scanners
parameter_list|,
name|KVComparator
name|comparator
parameter_list|)
throws|throws
name|IOException
block|{
name|this
argument_list|(
name|scanners
argument_list|,
operator|new
name|KVScannerComparator
argument_list|(
name|comparator
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * Constructor.    * @param scanners    * @param comparator    * @throws IOException    */
name|KeyValueHeap
parameter_list|(
name|List
argument_list|<
name|?
extends|extends
name|KeyValueScanner
argument_list|>
name|scanners
parameter_list|,
name|KVScannerComparator
name|comparator
parameter_list|)
throws|throws
name|IOException
block|{
name|this
operator|.
name|comparator
operator|=
name|comparator
expr_stmt|;
if|if
condition|(
operator|!
name|scanners
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|this
operator|.
name|heap
operator|=
operator|new
name|PriorityQueue
argument_list|<
name|KeyValueScanner
argument_list|>
argument_list|(
name|scanners
operator|.
name|size
argument_list|()
argument_list|,
name|this
operator|.
name|comparator
argument_list|)
expr_stmt|;
for|for
control|(
name|KeyValueScanner
name|scanner
range|:
name|scanners
control|)
block|{
if|if
condition|(
name|scanner
operator|.
name|peek
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|this
operator|.
name|heap
operator|.
name|add
argument_list|(
name|scanner
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|scanner
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
name|this
operator|.
name|current
operator|=
name|pollRealKV
argument_list|()
expr_stmt|;
block|}
block|}
specifier|public
name|Cell
name|peek
parameter_list|()
block|{
if|if
condition|(
name|this
operator|.
name|current
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
return|return
name|this
operator|.
name|current
operator|.
name|peek
argument_list|()
return|;
block|}
specifier|public
name|Cell
name|next
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|this
operator|.
name|current
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
name|Cell
name|kvReturn
init|=
name|this
operator|.
name|current
operator|.
name|next
argument_list|()
decl_stmt|;
name|Cell
name|kvNext
init|=
name|this
operator|.
name|current
operator|.
name|peek
argument_list|()
decl_stmt|;
if|if
condition|(
name|kvNext
operator|==
literal|null
condition|)
block|{
name|this
operator|.
name|current
operator|.
name|close
argument_list|()
expr_stmt|;
name|this
operator|.
name|current
operator|=
name|pollRealKV
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|KeyValueScanner
name|topScanner
init|=
name|this
operator|.
name|heap
operator|.
name|peek
argument_list|()
decl_stmt|;
comment|// no need to add current back to the heap if it is the only scanner left
if|if
condition|(
name|topScanner
operator|!=
literal|null
operator|&&
name|this
operator|.
name|comparator
operator|.
name|compare
argument_list|(
name|kvNext
argument_list|,
name|topScanner
operator|.
name|peek
argument_list|()
argument_list|)
operator|>=
literal|0
condition|)
block|{
name|this
operator|.
name|heap
operator|.
name|add
argument_list|(
name|this
operator|.
name|current
argument_list|)
expr_stmt|;
name|this
operator|.
name|current
operator|=
name|pollRealKV
argument_list|()
expr_stmt|;
block|}
block|}
return|return
name|kvReturn
return|;
block|}
comment|/**    * Gets the next row of keys from the top-most scanner.    *<p>    * This method takes care of updating the heap.    *<p>    * This can ONLY be called when you are using Scanners that implement    * InternalScanner as well as KeyValueScanner (a {@link StoreScanner}).    * @param result    * @param limit    * @return true if there are more keys, false if all scanners are done    */
specifier|public
name|boolean
name|next
parameter_list|(
name|List
argument_list|<
name|Cell
argument_list|>
name|result
parameter_list|,
name|int
name|limit
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|this
operator|.
name|current
operator|==
literal|null
condition|)
block|{
return|return
literal|false
return|;
block|}
name|InternalScanner
name|currentAsInternal
init|=
operator|(
name|InternalScanner
operator|)
name|this
operator|.
name|current
decl_stmt|;
name|boolean
name|mayContainMoreRows
init|=
name|currentAsInternal
operator|.
name|next
argument_list|(
name|result
argument_list|,
name|limit
argument_list|)
decl_stmt|;
name|Cell
name|pee
init|=
name|this
operator|.
name|current
operator|.
name|peek
argument_list|()
decl_stmt|;
comment|/*      * By definition, any InternalScanner must return false only when it has no      * further rows to be fetched. So, we can close a scanner if it returns      * false. All existing implementations seem to be fine with this. It is much      * more efficient to close scanners which are not needed than keep them in      * the heap. This is also required for certain optimizations.      */
if|if
condition|(
name|pee
operator|==
literal|null
operator|||
operator|!
name|mayContainMoreRows
condition|)
block|{
name|this
operator|.
name|current
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|this
operator|.
name|heap
operator|.
name|add
argument_list|(
name|this
operator|.
name|current
argument_list|)
expr_stmt|;
block|}
name|this
operator|.
name|current
operator|=
name|pollRealKV
argument_list|()
expr_stmt|;
return|return
operator|(
name|this
operator|.
name|current
operator|!=
literal|null
operator|)
return|;
block|}
comment|/**    * Gets the next row of keys from the top-most scanner.    *<p>    * This method takes care of updating the heap.    *<p>    * This can ONLY be called when you are using Scanners that implement    * InternalScanner as well as KeyValueScanner (a {@link StoreScanner}).    * @param result    * @return true if there are more keys, false if all scanners are done    */
specifier|public
name|boolean
name|next
parameter_list|(
name|List
argument_list|<
name|Cell
argument_list|>
name|result
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|next
argument_list|(
name|result
argument_list|,
operator|-
literal|1
argument_list|)
return|;
block|}
specifier|protected
specifier|static
class|class
name|KVScannerComparator
implements|implements
name|Comparator
argument_list|<
name|KeyValueScanner
argument_list|>
block|{
specifier|protected
name|KVComparator
name|kvComparator
decl_stmt|;
comment|/**      * Constructor      * @param kvComparator      */
specifier|public
name|KVScannerComparator
parameter_list|(
name|KVComparator
name|kvComparator
parameter_list|)
block|{
name|this
operator|.
name|kvComparator
operator|=
name|kvComparator
expr_stmt|;
block|}
specifier|public
name|int
name|compare
parameter_list|(
name|KeyValueScanner
name|left
parameter_list|,
name|KeyValueScanner
name|right
parameter_list|)
block|{
name|int
name|comparison
init|=
name|compare
argument_list|(
name|left
operator|.
name|peek
argument_list|()
argument_list|,
name|right
operator|.
name|peek
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|comparison
operator|!=
literal|0
condition|)
block|{
return|return
name|comparison
return|;
block|}
else|else
block|{
comment|// Since both the keys are exactly the same, we break the tie in favor
comment|// of the key which came latest.
name|long
name|leftSequenceID
init|=
name|left
operator|.
name|getSequenceID
argument_list|()
decl_stmt|;
name|long
name|rightSequenceID
init|=
name|right
operator|.
name|getSequenceID
argument_list|()
decl_stmt|;
if|if
condition|(
name|leftSequenceID
operator|>
name|rightSequenceID
condition|)
block|{
return|return
operator|-
literal|1
return|;
block|}
elseif|else
if|if
condition|(
name|leftSequenceID
operator|<
name|rightSequenceID
condition|)
block|{
return|return
literal|1
return|;
block|}
else|else
block|{
return|return
literal|0
return|;
block|}
block|}
block|}
comment|/**      * Compares two KeyValue      * @param left      * @param right      * @return less than 0 if left is smaller, 0 if equal etc..      */
specifier|public
name|int
name|compare
parameter_list|(
name|Cell
name|left
parameter_list|,
name|Cell
name|right
parameter_list|)
block|{
return|return
name|this
operator|.
name|kvComparator
operator|.
name|compare
argument_list|(
name|left
argument_list|,
name|right
argument_list|)
return|;
block|}
comment|/**      * @return KVComparator      */
specifier|public
name|KVComparator
name|getComparator
parameter_list|()
block|{
return|return
name|this
operator|.
name|kvComparator
return|;
block|}
block|}
specifier|public
name|void
name|close
parameter_list|()
block|{
if|if
condition|(
name|this
operator|.
name|current
operator|!=
literal|null
condition|)
block|{
name|this
operator|.
name|current
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|this
operator|.
name|heap
operator|!=
literal|null
condition|)
block|{
name|KeyValueScanner
name|scanner
decl_stmt|;
while|while
condition|(
operator|(
name|scanner
operator|=
name|this
operator|.
name|heap
operator|.
name|poll
argument_list|()
operator|)
operator|!=
literal|null
condition|)
block|{
name|scanner
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
block|}
comment|/**    * Seeks all scanners at or below the specified seek key.  If we earlied-out    * of a row, we may end up skipping values that were never reached yet.    * Rather than iterating down, we want to give the opportunity to re-seek.    *<p>    * As individual scanners may run past their ends, those scanners are    * automatically closed and removed from the heap.    *<p>    * This function (and {@link #reseek(Cell)}) does not do multi-column    * Bloom filter and lazy-seek optimizations. To enable those, call    * {@link #requestSeek(Cell, boolean, boolean)}.    * @param seekKey KeyValue to seek at or after    * @return true if KeyValues exist at or after specified key, false if not    * @throws IOException    */
annotation|@
name|Override
specifier|public
name|boolean
name|seek
parameter_list|(
name|Cell
name|seekKey
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|generalizedSeek
argument_list|(
literal|false
argument_list|,
comment|// This is not a lazy seek
name|seekKey
argument_list|,
literal|false
argument_list|,
comment|// forward (false: this is not a reseek)
literal|false
argument_list|)
return|;
comment|// Not using Bloom filters
block|}
comment|/**    * This function is identical to the {@link #seek(Cell)} function except    * that scanner.seek(seekKey) is changed to scanner.reseek(seekKey).    */
annotation|@
name|Override
specifier|public
name|boolean
name|reseek
parameter_list|(
name|Cell
name|seekKey
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|generalizedSeek
argument_list|(
literal|false
argument_list|,
comment|// This is not a lazy seek
name|seekKey
argument_list|,
literal|true
argument_list|,
comment|// forward (true because this is reseek)
literal|false
argument_list|)
return|;
comment|// Not using Bloom filters
block|}
comment|/**    * {@inheritDoc}    */
annotation|@
name|Override
specifier|public
name|boolean
name|requestSeek
parameter_list|(
name|Cell
name|key
parameter_list|,
name|boolean
name|forward
parameter_list|,
name|boolean
name|useBloom
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|generalizedSeek
argument_list|(
literal|true
argument_list|,
name|key
argument_list|,
name|forward
argument_list|,
name|useBloom
argument_list|)
return|;
block|}
comment|/**    * @param isLazy whether we are trying to seek to exactly the given row/col.    *          Enables Bloom filter and most-recent-file-first optimizations for    *          multi-column get/scan queries.    * @param seekKey key to seek to    * @param forward whether to seek forward (also known as reseek)    * @param useBloom whether to optimize seeks using Bloom filters    */
specifier|private
name|boolean
name|generalizedSeek
parameter_list|(
name|boolean
name|isLazy
parameter_list|,
name|Cell
name|seekKey
parameter_list|,
name|boolean
name|forward
parameter_list|,
name|boolean
name|useBloom
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
operator|!
name|isLazy
operator|&&
name|useBloom
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Multi-column Bloom filter "
operator|+
literal|"optimization requires a lazy seek"
argument_list|)
throw|;
block|}
if|if
condition|(
name|current
operator|==
literal|null
condition|)
block|{
return|return
literal|false
return|;
block|}
name|heap
operator|.
name|add
argument_list|(
name|current
argument_list|)
expr_stmt|;
name|current
operator|=
literal|null
expr_stmt|;
name|KeyValueScanner
name|scanner
decl_stmt|;
while|while
condition|(
operator|(
name|scanner
operator|=
name|heap
operator|.
name|poll
argument_list|()
operator|)
operator|!=
literal|null
condition|)
block|{
name|Cell
name|topKey
init|=
name|scanner
operator|.
name|peek
argument_list|()
decl_stmt|;
if|if
condition|(
name|comparator
operator|.
name|getComparator
argument_list|()
operator|.
name|compare
argument_list|(
name|seekKey
argument_list|,
name|topKey
argument_list|)
operator|<=
literal|0
condition|)
block|{
comment|// Top KeyValue is at-or-after Seek KeyValue. We only know that all
comment|// scanners are at or after seekKey (because fake keys of
comment|// scanners where a lazy-seek operation has been done are not greater
comment|// than their real next keys) but we still need to enforce our
comment|// invariant that the top scanner has done a real seek. This way
comment|// StoreScanner and RegionScanner do not have to worry about fake keys.
name|heap
operator|.
name|add
argument_list|(
name|scanner
argument_list|)
expr_stmt|;
name|current
operator|=
name|pollRealKV
argument_list|()
expr_stmt|;
return|return
name|current
operator|!=
literal|null
return|;
block|}
name|boolean
name|seekResult
decl_stmt|;
if|if
condition|(
name|isLazy
operator|&&
name|heap
operator|.
name|size
argument_list|()
operator|>
literal|0
condition|)
block|{
comment|// If there is only one scanner left, we don't do lazy seek.
name|seekResult
operator|=
name|scanner
operator|.
name|requestSeek
argument_list|(
name|seekKey
argument_list|,
name|forward
argument_list|,
name|useBloom
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|seekResult
operator|=
name|NonLazyKeyValueScanner
operator|.
name|doRealSeek
argument_list|(
name|scanner
argument_list|,
name|seekKey
argument_list|,
name|forward
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|seekResult
condition|)
block|{
name|scanner
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|heap
operator|.
name|add
argument_list|(
name|scanner
argument_list|)
expr_stmt|;
block|}
block|}
comment|// Heap is returning empty, scanner is done
return|return
literal|false
return|;
block|}
comment|/**    * Fetches the top sub-scanner from the priority queue, ensuring that a real    * seek has been done on it. Works by fetching the top sub-scanner, and if it    * has not done a real seek, making it do so (which will modify its top KV),    * putting it back, and repeating this until success. Relies on the fact that    * on a lazy seek we set the current key of a StoreFileScanner to a KV that    * is not greater than the real next KV to be read from that file, so the    * scanner that bubbles up to the top of the heap will have global next KV in    * this scanner heap if (1) it has done a real seek and (2) its KV is the top    * among all top KVs (some of which are fake) in the scanner heap.    */
specifier|protected
name|KeyValueScanner
name|pollRealKV
parameter_list|()
throws|throws
name|IOException
block|{
name|KeyValueScanner
name|kvScanner
init|=
name|heap
operator|.
name|poll
argument_list|()
decl_stmt|;
if|if
condition|(
name|kvScanner
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
while|while
condition|(
name|kvScanner
operator|!=
literal|null
operator|&&
operator|!
name|kvScanner
operator|.
name|realSeekDone
argument_list|()
condition|)
block|{
if|if
condition|(
name|kvScanner
operator|.
name|peek
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|kvScanner
operator|.
name|enforceSeek
argument_list|()
expr_stmt|;
name|Cell
name|curKV
init|=
name|kvScanner
operator|.
name|peek
argument_list|()
decl_stmt|;
if|if
condition|(
name|curKV
operator|!=
literal|null
condition|)
block|{
name|KeyValueScanner
name|nextEarliestScanner
init|=
name|heap
operator|.
name|peek
argument_list|()
decl_stmt|;
if|if
condition|(
name|nextEarliestScanner
operator|==
literal|null
condition|)
block|{
comment|// The heap is empty. Return the only possible scanner.
return|return
name|kvScanner
return|;
block|}
comment|// Compare the current scanner to the next scanner. We try to avoid
comment|// putting the current one back into the heap if possible.
name|Cell
name|nextKV
init|=
name|nextEarliestScanner
operator|.
name|peek
argument_list|()
decl_stmt|;
if|if
condition|(
name|nextKV
operator|==
literal|null
operator|||
name|comparator
operator|.
name|compare
argument_list|(
name|curKV
argument_list|,
name|nextKV
argument_list|)
operator|<
literal|0
condition|)
block|{
comment|// We already have the scanner with the earliest KV, so return it.
return|return
name|kvScanner
return|;
block|}
comment|// Otherwise, put the scanner back into the heap and let it compete
comment|// against all other scanners (both those that have done a "real
comment|// seek" and a "lazy seek").
name|heap
operator|.
name|add
argument_list|(
name|kvScanner
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// Close the scanner because we did a real seek and found out there
comment|// are no more KVs.
name|kvScanner
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
else|else
block|{
comment|// Close the scanner because it has already run out of KVs even before
comment|// we had to do a real seek on it.
name|kvScanner
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
name|kvScanner
operator|=
name|heap
operator|.
name|poll
argument_list|()
expr_stmt|;
block|}
return|return
name|kvScanner
return|;
block|}
comment|/**    * @return the current Heap    */
specifier|public
name|PriorityQueue
argument_list|<
name|KeyValueScanner
argument_list|>
name|getHeap
parameter_list|()
block|{
return|return
name|this
operator|.
name|heap
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getSequenceID
parameter_list|()
block|{
return|return
literal|0
return|;
block|}
name|KeyValueScanner
name|getCurrentForTesting
parameter_list|()
block|{
return|return
name|current
return|;
block|}
block|}
end_class

end_unit

