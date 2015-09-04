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
name|LinkedList
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
name|AtomicLong
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
comment|/**  * Manages the read/write consistency within memstore. This provides  * an interface for readers to determine what entries to ignore, and  * a mechanism for writers to obtain new write numbers, then "commit"  * the new writes for readers to read (thus forming atomic transactions).  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|MultiVersionConcurrencyControl
block|{
specifier|private
specifier|static
specifier|final
name|long
name|NO_WRITE_NUMBER
init|=
literal|0
decl_stmt|;
specifier|private
specifier|volatile
name|long
name|memstoreRead
init|=
literal|0
decl_stmt|;
specifier|private
specifier|final
name|Object
name|readWaiters
init|=
operator|new
name|Object
argument_list|()
decl_stmt|;
comment|// This is the pending queue of writes.
specifier|private
specifier|final
name|LinkedList
argument_list|<
name|WriteEntry
argument_list|>
name|writeQueue
init|=
operator|new
name|LinkedList
argument_list|<
name|WriteEntry
argument_list|>
argument_list|()
decl_stmt|;
comment|/**    * Default constructor. Initializes the memstoreRead/Write points to 0.    */
specifier|public
name|MultiVersionConcurrencyControl
parameter_list|()
block|{   }
comment|/**    * Initializes the memstoreRead/Write points appropriately.    * @param startPoint    */
specifier|public
name|void
name|initialize
parameter_list|(
name|long
name|startPoint
parameter_list|)
block|{
synchronized|synchronized
init|(
name|writeQueue
init|)
block|{
name|writeQueue
operator|.
name|clear
argument_list|()
expr_stmt|;
name|memstoreRead
operator|=
name|startPoint
expr_stmt|;
block|}
block|}
comment|/**    *    * @param initVal The value we used initially and expected it'll be reset later    * @return WriteEntry instance.    */
name|WriteEntry
name|beginMemstoreInsert
parameter_list|()
block|{
return|return
name|beginMemstoreInsertWithSeqNum
argument_list|(
name|NO_WRITE_NUMBER
argument_list|)
return|;
block|}
comment|/**    * Get a mvcc write number before an actual one(its log sequence Id) being assigned    * @param sequenceId    * @return long a faked write number which is bigger enough not to be seen by others before a real    *         one is assigned    */
specifier|public
specifier|static
name|long
name|getPreAssignedWriteNumber
parameter_list|(
name|AtomicLong
name|sequenceId
parameter_list|)
block|{
comment|// the 1 billion is just an arbitrary big number to guard no scanner will reach it before
comment|// current MVCC completes. Theoretically the bump only needs to be 2 * the number of handlers
comment|// because each handler could increment sequence num twice and max concurrent in-flight
comment|// transactions is the number of RPC handlers.
comment|// We can't use Long.MAX_VALUE because we still want to maintain the ordering when multiple
comment|// changes touch same row key.
comment|// If for any reason, the bumped value isn't reset due to failure situations, we'll reset
comment|// curSeqNum to NO_WRITE_NUMBER in order NOT to advance memstore read point at all.
comment|// St.Ack 20150901 Where is the reset to NO_WRITE_NUMBER done?
return|return
name|sequenceId
operator|.
name|incrementAndGet
argument_list|()
operator|+
literal|1000000000
return|;
block|}
comment|/**    * This function starts a MVCC transaction with current region's log change sequence number. Since    * we set change sequence number when flushing current change to WAL(late binding), the flush    * order may differ from the order to start a MVCC transaction. For example, a change begins a    * MVCC firstly may complete later than a change which starts MVCC at a later time. Therefore, we    * add a safe bumper to the passed in sequence number to start a MVCC so that no other concurrent    * transactions will reuse the number till current MVCC completes(success or fail). The "faked"    * big number is safe because we only need it to prevent current change being seen and the number    * will be reset to real sequence number(set in log sync) right before we complete a MVCC in order    * for MVCC to align with flush sequence.    * @param curSeqNum    * @return WriteEntry a WriteEntry instance with the passed in curSeqNum    */
specifier|public
name|WriteEntry
name|beginMemstoreInsertWithSeqNum
parameter_list|(
name|long
name|curSeqNum
parameter_list|)
block|{
name|WriteEntry
name|e
init|=
operator|new
name|WriteEntry
argument_list|(
name|curSeqNum
argument_list|)
decl_stmt|;
synchronized|synchronized
init|(
name|writeQueue
init|)
block|{
name|writeQueue
operator|.
name|add
argument_list|(
name|e
argument_list|)
expr_stmt|;
return|return
name|e
return|;
block|}
block|}
comment|/**    * Complete a {@link WriteEntry} that was created by    * {@link #beginMemstoreInsertWithSeqNum(long)}. At the end of this call, the global read    * point is at least as large as the write point of the passed in WriteEntry. Thus, the write is    * visible to MVCC readers.    * @throws IOException    */
specifier|public
name|void
name|completeMemstoreInsertWithSeqNum
parameter_list|(
name|WriteEntry
name|e
parameter_list|,
name|SequenceId
name|seqId
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|e
operator|==
literal|null
condition|)
return|return;
if|if
condition|(
name|seqId
operator|!=
literal|null
condition|)
block|{
name|e
operator|.
name|setWriteNumber
argument_list|(
name|seqId
operator|.
name|getSequenceId
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// set the value to NO_WRITE_NUMBER in order NOT to advance memstore readpoint inside
comment|// function beginMemstoreInsertWithSeqNum in case of failures
name|e
operator|.
name|setWriteNumber
argument_list|(
name|NO_WRITE_NUMBER
argument_list|)
expr_stmt|;
block|}
name|waitForPreviousTransactionsComplete
argument_list|(
name|e
argument_list|)
expr_stmt|;
block|}
comment|/**    * Cancel a write insert that failed.    * Removes the write entry without advancing read point or without interfering with write    * entries queued behind us. It is like #advanceMemstore(WriteEntry) only this method    * will move the read point to the sequence id that is in WriteEntry even if it ridiculous (see    * the trick in HRegion where we call {@link #getPreAssignedWriteNumber(AtomicLong)} just to mark    * it as for special handling).    * @param writeEntry Failed attempt at write. Does cleanup.    */
specifier|public
name|void
name|cancelMemstoreInsert
parameter_list|(
name|WriteEntry
name|writeEntry
parameter_list|)
block|{
comment|// I'm not clear on how this voodoo all works but setting write number to -1 does NOT advance
comment|// readpoint and gets my little writeEntry completed and removed from queue of outstanding
comment|// events which seems right.  St.Ack 20150901.
name|writeEntry
operator|.
name|setWriteNumber
argument_list|(
name|NO_WRITE_NUMBER
argument_list|)
expr_stmt|;
name|advanceMemstore
argument_list|(
name|writeEntry
argument_list|)
expr_stmt|;
block|}
comment|/**    * Complete a {@link WriteEntry} that was created by {@link #beginMemstoreInsert()}. At the    * end of this call, the global read point is at least as large as the write point of the passed    * in WriteEntry. Thus, the write is visible to MVCC readers.    */
specifier|public
name|void
name|completeMemstoreInsert
parameter_list|(
name|WriteEntry
name|e
parameter_list|)
block|{
name|waitForPreviousTransactionsComplete
argument_list|(
name|e
argument_list|)
expr_stmt|;
block|}
comment|/**    * Mark the {@link WriteEntry} as complete and advance the read point as    * much as possible.    *    * How much is the read point advanced?    * Let S be the set of all write numbers that are completed and where all previous write numbers    * are also completed.  Then, the read point is advanced to the supremum of S.    *    * @param e    * @return true if e is visible to MVCC readers (that is, readpoint>= e.writeNumber)    */
name|boolean
name|advanceMemstore
parameter_list|(
name|WriteEntry
name|e
parameter_list|)
block|{
name|long
name|nextReadValue
init|=
operator|-
literal|1
decl_stmt|;
synchronized|synchronized
init|(
name|writeQueue
init|)
block|{
name|e
operator|.
name|markCompleted
argument_list|()
expr_stmt|;
while|while
condition|(
operator|!
name|writeQueue
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|WriteEntry
name|queueFirst
init|=
name|writeQueue
operator|.
name|getFirst
argument_list|()
decl_stmt|;
if|if
condition|(
name|queueFirst
operator|.
name|isCompleted
argument_list|()
condition|)
block|{
comment|// Using Max because Edit complete in WAL sync order not arriving order
name|nextReadValue
operator|=
name|Math
operator|.
name|max
argument_list|(
name|nextReadValue
argument_list|,
name|queueFirst
operator|.
name|getWriteNumber
argument_list|()
argument_list|)
expr_stmt|;
name|writeQueue
operator|.
name|removeFirst
argument_list|()
expr_stmt|;
block|}
else|else
block|{
break|break;
block|}
block|}
if|if
condition|(
name|nextReadValue
operator|>
name|memstoreRead
condition|)
block|{
name|memstoreRead
operator|=
name|nextReadValue
expr_stmt|;
block|}
comment|// notify waiters on writeQueue before return
name|writeQueue
operator|.
name|notifyAll
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|nextReadValue
operator|>
literal|0
condition|)
block|{
synchronized|synchronized
init|(
name|readWaiters
init|)
block|{
name|readWaiters
operator|.
name|notifyAll
argument_list|()
expr_stmt|;
block|}
block|}
if|if
condition|(
name|memstoreRead
operator|>=
name|e
operator|.
name|getWriteNumber
argument_list|()
condition|)
block|{
return|return
literal|true
return|;
block|}
return|return
literal|false
return|;
block|}
comment|/**    * Advances the current read point to be given seqNum if it is smaller than    * that.    */
name|void
name|advanceMemstoreReadPointIfNeeded
parameter_list|(
name|long
name|seqNum
parameter_list|)
block|{
synchronized|synchronized
init|(
name|writeQueue
init|)
block|{
if|if
condition|(
name|this
operator|.
name|memstoreRead
operator|<
name|seqNum
condition|)
block|{
name|memstoreRead
operator|=
name|seqNum
expr_stmt|;
block|}
block|}
block|}
comment|/**    * Wait for all previous MVCC transactions complete    */
specifier|public
name|void
name|waitForPreviousTransactionsComplete
parameter_list|()
block|{
name|WriteEntry
name|w
init|=
name|beginMemstoreInsert
argument_list|()
decl_stmt|;
name|waitForPreviousTransactionsComplete
argument_list|(
name|w
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|waitForPreviousTransactionsComplete
parameter_list|(
name|WriteEntry
name|waitedEntry
parameter_list|)
block|{
name|boolean
name|interrupted
init|=
literal|false
decl_stmt|;
name|WriteEntry
name|w
init|=
name|waitedEntry
decl_stmt|;
try|try
block|{
name|WriteEntry
name|firstEntry
init|=
literal|null
decl_stmt|;
do|do
block|{
synchronized|synchronized
init|(
name|writeQueue
init|)
block|{
comment|// writeQueue won't be empty at this point, the following is just a safety check
if|if
condition|(
name|writeQueue
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
break|break;
block|}
name|firstEntry
operator|=
name|writeQueue
operator|.
name|getFirst
argument_list|()
expr_stmt|;
if|if
condition|(
name|firstEntry
operator|==
name|w
condition|)
block|{
comment|// all previous in-flight transactions are done
break|break;
block|}
try|try
block|{
name|writeQueue
operator|.
name|wait
argument_list|(
literal|0
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|ie
parameter_list|)
block|{
comment|// We were interrupted... finish the loop -- i.e. cleanup --and then
comment|// on our way out, reset the interrupt flag.
name|interrupted
operator|=
literal|true
expr_stmt|;
break|break;
block|}
block|}
block|}
do|while
condition|(
name|firstEntry
operator|!=
literal|null
condition|)
do|;
block|}
finally|finally
block|{
if|if
condition|(
name|w
operator|!=
literal|null
condition|)
block|{
name|advanceMemstore
argument_list|(
name|w
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|interrupted
condition|)
block|{
name|Thread
operator|.
name|currentThread
argument_list|()
operator|.
name|interrupt
argument_list|()
expr_stmt|;
block|}
block|}
specifier|public
name|long
name|memstoreReadPoint
parameter_list|()
block|{
return|return
name|memstoreRead
return|;
block|}
specifier|public
specifier|static
class|class
name|WriteEntry
block|{
specifier|private
name|long
name|writeNumber
decl_stmt|;
specifier|private
specifier|volatile
name|boolean
name|completed
init|=
literal|false
decl_stmt|;
name|WriteEntry
parameter_list|(
name|long
name|writeNumber
parameter_list|)
block|{
name|this
operator|.
name|writeNumber
operator|=
name|writeNumber
expr_stmt|;
block|}
name|void
name|markCompleted
parameter_list|()
block|{
name|this
operator|.
name|completed
operator|=
literal|true
expr_stmt|;
block|}
name|boolean
name|isCompleted
parameter_list|()
block|{
return|return
name|this
operator|.
name|completed
return|;
block|}
name|long
name|getWriteNumber
parameter_list|()
block|{
return|return
name|this
operator|.
name|writeNumber
return|;
block|}
name|void
name|setWriteNumber
parameter_list|(
name|long
name|val
parameter_list|)
block|{
name|this
operator|.
name|writeNumber
operator|=
name|val
expr_stmt|;
block|}
block|}
specifier|public
specifier|static
specifier|final
name|long
name|FIXED_SIZE
init|=
name|ClassSize
operator|.
name|align
argument_list|(
name|ClassSize
operator|.
name|OBJECT
operator|+
literal|2
operator|*
name|Bytes
operator|.
name|SIZEOF_LONG
operator|+
literal|2
operator|*
name|ClassSize
operator|.
name|REFERENCE
argument_list|)
decl_stmt|;
block|}
end_class

end_unit

