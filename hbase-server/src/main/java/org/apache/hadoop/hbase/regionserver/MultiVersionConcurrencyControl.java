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
name|org
operator|.
name|apache
operator|.
name|hbase
operator|.
name|thirdparty
operator|.
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|annotations
operator|.
name|VisibleForTesting
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
name|thirdparty
operator|.
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|base
operator|.
name|MoreObjects
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
name|slf4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|LoggerFactory
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
comment|/**  * Manages the read/write consistency. This provides an interface for readers to determine what  * entries to ignore, and a mechanism for writers to obtain new write numbers, then "commit"  * the new writes for readers to read (thus forming atomic transactions).  */
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
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|MultiVersionConcurrencyControl
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|final
name|AtomicLong
name|readPoint
init|=
operator|new
name|AtomicLong
argument_list|(
literal|0
argument_list|)
decl_stmt|;
specifier|final
name|AtomicLong
name|writePoint
init|=
operator|new
name|AtomicLong
argument_list|(
literal|0
argument_list|)
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
comment|/**    * Represents no value, or not set.    */
specifier|public
specifier|static
specifier|final
name|long
name|NONE
init|=
operator|-
literal|1
decl_stmt|;
comment|// This is the pending queue of writes.
comment|//
comment|// TODO(eclark): Should this be an array of fixed size to
comment|// reduce the number of allocations on the write path?
comment|// This could be equal to the number of handlers + a small number.
comment|// TODO: St.Ack 20150903 Sounds good to me.
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
argument_list|<>
argument_list|()
decl_stmt|;
specifier|public
name|MultiVersionConcurrencyControl
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
block|}
comment|/**    * Construct and set read point. Write point is uninitialized.    */
specifier|public
name|MultiVersionConcurrencyControl
parameter_list|(
name|long
name|startPoint
parameter_list|)
block|{
name|tryAdvanceTo
argument_list|(
name|startPoint
argument_list|,
name|NONE
argument_list|)
expr_stmt|;
block|}
comment|/**    * Step the MVCC forward on to a new read/write basis.    * @param newStartPoint    */
specifier|public
name|void
name|advanceTo
parameter_list|(
name|long
name|newStartPoint
parameter_list|)
block|{
while|while
condition|(
literal|true
condition|)
block|{
name|long
name|seqId
init|=
name|this
operator|.
name|getWritePoint
argument_list|()
decl_stmt|;
if|if
condition|(
name|seqId
operator|>=
name|newStartPoint
condition|)
block|{
break|break;
block|}
if|if
condition|(
name|this
operator|.
name|tryAdvanceTo
argument_list|(
name|newStartPoint
argument_list|,
name|seqId
argument_list|)
condition|)
block|{
break|break;
block|}
block|}
block|}
comment|/**    * Step the MVCC forward on to a new read/write basis.    * @param newStartPoint Point to move read and write points to.    * @param expected If not -1 (#NONE)    * @return Returns false if<code>expected</code> is not equal to the    * current<code>readPoint</code> or if<code>startPoint</code> is less than current    *<code>readPoint</code>    */
name|boolean
name|tryAdvanceTo
parameter_list|(
name|long
name|newStartPoint
parameter_list|,
name|long
name|expected
parameter_list|)
block|{
synchronized|synchronized
init|(
name|writeQueue
init|)
block|{
name|long
name|currentRead
init|=
name|this
operator|.
name|readPoint
operator|.
name|get
argument_list|()
decl_stmt|;
name|long
name|currentWrite
init|=
name|this
operator|.
name|writePoint
operator|.
name|get
argument_list|()
decl_stmt|;
if|if
condition|(
name|currentRead
operator|!=
name|currentWrite
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Already used this mvcc; currentRead="
operator|+
name|currentRead
operator|+
literal|", currentWrite="
operator|+
name|currentWrite
operator|+
literal|"; too late to tryAdvanceTo"
argument_list|)
throw|;
block|}
if|if
condition|(
name|expected
operator|!=
name|NONE
operator|&&
name|expected
operator|!=
name|currentRead
condition|)
block|{
return|return
literal|false
return|;
block|}
if|if
condition|(
name|newStartPoint
operator|<
name|currentRead
condition|)
block|{
return|return
literal|false
return|;
block|}
name|readPoint
operator|.
name|set
argument_list|(
name|newStartPoint
argument_list|)
expr_stmt|;
name|writePoint
operator|.
name|set
argument_list|(
name|newStartPoint
argument_list|)
expr_stmt|;
block|}
return|return
literal|true
return|;
block|}
comment|/**    * Call {@link #begin(Runnable)} with an empty {@link Runnable}.    */
specifier|public
name|WriteEntry
name|begin
parameter_list|()
block|{
return|return
name|begin
argument_list|(
parameter_list|()
lambda|->
block|{}
argument_list|)
return|;
block|}
comment|/**    * Start a write transaction. Create a new {@link WriteEntry} with a new write number and add it    * to our queue of ongoing writes. Return this WriteEntry instance. To complete the write    * transaction and wait for it to be visible, call {@link #completeAndWait(WriteEntry)}. If the    * write failed, call {@link #complete(WriteEntry)} so we can clean up AFTER removing ALL trace of    * the failed write transaction.    *<p>    * The {@code action} will be executed under the lock which means it can keep the same order with    * mvcc.    * @see #complete(WriteEntry)    * @see #completeAndWait(WriteEntry)    */
specifier|public
name|WriteEntry
name|begin
parameter_list|(
name|Runnable
name|action
parameter_list|)
block|{
synchronized|synchronized
init|(
name|writeQueue
init|)
block|{
name|long
name|nextWriteNumber
init|=
name|writePoint
operator|.
name|incrementAndGet
argument_list|()
decl_stmt|;
name|WriteEntry
name|e
init|=
operator|new
name|WriteEntry
argument_list|(
name|nextWriteNumber
argument_list|)
decl_stmt|;
name|writeQueue
operator|.
name|add
argument_list|(
name|e
argument_list|)
expr_stmt|;
name|action
operator|.
name|run
argument_list|()
expr_stmt|;
return|return
name|e
return|;
block|}
block|}
comment|/**    * Wait until the read point catches up to the write point; i.e. wait on all outstanding mvccs    * to complete.    */
specifier|public
name|void
name|await
parameter_list|()
block|{
comment|// Add a write and then wait on reads to catch up to it.
name|completeAndWait
argument_list|(
name|begin
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**    * Complete a {@link WriteEntry} that was created by {@link #begin()} then wait until the    * read point catches up to our write.    *    * At the end of this call, the global read point is at least as large as the write point    * of the passed in WriteEntry.  Thus, the write is visible to MVCC readers.    */
specifier|public
name|void
name|completeAndWait
parameter_list|(
name|WriteEntry
name|e
parameter_list|)
block|{
if|if
condition|(
operator|!
name|complete
argument_list|(
name|e
argument_list|)
condition|)
block|{
name|waitForRead
argument_list|(
name|e
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Mark the {@link WriteEntry} as complete and advance the read point as much as possible.    * Call this even if the write has FAILED (AFTER backing out the write transaction    * changes completely) so we can clean up the outstanding transaction.    *    * How much is the read point advanced?    *    * Let S be the set of all write numbers that are completed. Set the read point to the highest    * numbered write of S.    *    * @param writeEntry    *    * @return true if e is visible to MVCC readers (that is, readpoint>= e.writeNumber)    */
specifier|public
name|boolean
name|complete
parameter_list|(
name|WriteEntry
name|writeEntry
parameter_list|)
block|{
synchronized|synchronized
init|(
name|writeQueue
init|)
block|{
name|writeEntry
operator|.
name|markCompleted
argument_list|()
expr_stmt|;
name|long
name|nextReadValue
init|=
name|NONE
decl_stmt|;
name|boolean
name|ranOnce
init|=
literal|false
decl_stmt|;
while|while
condition|(
operator|!
name|writeQueue
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|ranOnce
operator|=
literal|true
expr_stmt|;
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
name|nextReadValue
operator|>
literal|0
condition|)
block|{
if|if
condition|(
name|nextReadValue
operator|+
literal|1
operator|!=
name|queueFirst
operator|.
name|getWriteNumber
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Invariant in complete violated, nextReadValue="
operator|+
name|nextReadValue
operator|+
literal|", writeNumber="
operator|+
name|queueFirst
operator|.
name|getWriteNumber
argument_list|()
argument_list|)
throw|;
block|}
block|}
if|if
condition|(
name|queueFirst
operator|.
name|isCompleted
argument_list|()
condition|)
block|{
name|nextReadValue
operator|=
name|queueFirst
operator|.
name|getWriteNumber
argument_list|()
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
operator|!
name|ranOnce
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"There is no first!"
argument_list|)
throw|;
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
name|readPoint
operator|.
name|set
argument_list|(
name|nextReadValue
argument_list|)
expr_stmt|;
name|readWaiters
operator|.
name|notifyAll
argument_list|()
expr_stmt|;
block|}
block|}
return|return
name|readPoint
operator|.
name|get
argument_list|()
operator|>=
name|writeEntry
operator|.
name|getWriteNumber
argument_list|()
return|;
block|}
block|}
comment|/**    * Wait for the global readPoint to advance up to the passed in write entry number.    */
name|void
name|waitForRead
parameter_list|(
name|WriteEntry
name|e
parameter_list|)
block|{
name|boolean
name|interrupted
init|=
literal|false
decl_stmt|;
name|int
name|count
init|=
literal|0
decl_stmt|;
synchronized|synchronized
init|(
name|readWaiters
init|)
block|{
while|while
condition|(
name|readPoint
operator|.
name|get
argument_list|()
operator|<
name|e
operator|.
name|getWriteNumber
argument_list|()
condition|)
block|{
if|if
condition|(
name|count
operator|%
literal|100
operator|==
literal|0
operator|&&
name|count
operator|>
literal|0
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"STUCK: "
operator|+
name|this
argument_list|)
expr_stmt|;
block|}
name|count
operator|++
expr_stmt|;
try|try
block|{
name|readWaiters
operator|.
name|wait
argument_list|(
literal|10
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
block|}
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
annotation|@
name|VisibleForTesting
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
name|MoreObjects
operator|.
name|toStringHelper
argument_list|(
name|this
argument_list|)
operator|.
name|add
argument_list|(
literal|"readPoint"
argument_list|,
name|readPoint
argument_list|)
operator|.
name|add
argument_list|(
literal|"writePoint"
argument_list|,
name|writePoint
argument_list|)
operator|.
name|toString
argument_list|()
return|;
block|}
specifier|public
name|long
name|getReadPoint
parameter_list|()
block|{
return|return
name|readPoint
operator|.
name|get
argument_list|()
return|;
block|}
annotation|@
name|VisibleForTesting
specifier|public
name|long
name|getWritePoint
parameter_list|()
block|{
return|return
name|writePoint
operator|.
name|get
argument_list|()
return|;
block|}
comment|/**    * Write number and whether write has completed given out at start of a write transaction.    * Every created WriteEntry must be completed by calling mvcc#complete or #completeAndWait.    */
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
specifier|static
class|class
name|WriteEntry
block|{
specifier|private
specifier|final
name|long
name|writeNumber
decl_stmt|;
specifier|private
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
specifier|public
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
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
name|this
operator|.
name|writeNumber
operator|+
literal|", "
operator|+
name|this
operator|.
name|completed
return|;
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

