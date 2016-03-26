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
operator|.
name|wal
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|ExecutionException
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
name|htrace
operator|.
name|Span
import|;
end_import

begin_comment
comment|/**  * A Future on a filesystem sync call. It given to a client or 'Handler' for it to wait on till the  * sync completes.  *<p>  * Handlers coming in call append, append, append, and then do a flush/sync of the edits they have  * appended the WAL before returning. Since sync takes a while to complete, we give the Handlers  * back this sync future to wait on until the actual HDFS sync completes. Meantime this sync future  * goes across a queue and is handled by a background thread; when it completes, it finishes up the  * future, the handler get or failed check completes and the Handler can then progress.  *<p>  * This is just a partial implementation of Future; we just implement get and failure.  *<p>  * There is not a one-to-one correlation between dfs sync invocations and instances of this class. A  * single dfs sync call may complete and mark many SyncFutures as done; i.e. we batch up sync calls  * rather than do a dfs sync call every time a Handler asks for it.  *<p>  * SyncFutures are immutable but recycled. Call #reset(long, Span) before use even if it the first  * time, start the sync, then park the 'hitched' thread on a call to #get().  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
class|class
name|SyncFuture
block|{
comment|// Implementation notes: I tried using a cyclicbarrier in here for handler and sync threads
comment|// to coordinate on but it did not give any obvious advantage and some issues with order in which
comment|// events happen.
specifier|private
specifier|static
specifier|final
name|long
name|NOT_DONE
init|=
literal|0
decl_stmt|;
comment|/**    * The transaction id of this operation, monotonically increases.    */
specifier|private
name|long
name|txid
decl_stmt|;
comment|/**    * The transaction id that was set in here when we were marked done. Should be equal or> txnId.    * Put this data member into the NOT_DONE state while this class is in use. But for the first    * position on construction, let it be -1 so we can immediately call {@link #reset(long, Span)}    * below and it will work.    */
specifier|private
name|long
name|doneTxid
init|=
operator|-
literal|1
decl_stmt|;
comment|/**    * If error, the associated throwable. Set when the future is 'done'.    */
specifier|private
name|Throwable
name|throwable
init|=
literal|null
decl_stmt|;
specifier|private
name|Thread
name|t
decl_stmt|;
comment|/**    * Optionally carry a disconnected scope to the SyncRunner.    */
specifier|private
name|Span
name|span
decl_stmt|;
comment|/**    * Call this method to clear old usage and get it ready for new deploy. Call this method even if    * it is being used for the first time.    * @param txnId the new transaction id    * @return this    */
specifier|synchronized
name|SyncFuture
name|reset
parameter_list|(
specifier|final
name|long
name|txnId
parameter_list|)
block|{
return|return
name|reset
argument_list|(
name|txnId
argument_list|,
literal|null
argument_list|)
return|;
block|}
comment|/**    * Call this method to clear old usage and get it ready for new deploy. Call this method even if    * it is being used for the first time.    * @param sequence sequenceId from this Future's position in the RingBuffer    * @param span curren span, detached from caller. Don't forget to attach it when resuming after a    *          call to {@link #get()}.    * @return this    */
specifier|synchronized
name|SyncFuture
name|reset
parameter_list|(
specifier|final
name|long
name|txnId
parameter_list|,
name|Span
name|span
parameter_list|)
block|{
if|if
condition|(
name|t
operator|!=
literal|null
operator|&&
name|t
operator|!=
name|Thread
operator|.
name|currentThread
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|()
throw|;
block|}
name|t
operator|=
name|Thread
operator|.
name|currentThread
argument_list|()
expr_stmt|;
if|if
condition|(
operator|!
name|isDone
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|""
operator|+
name|txnId
operator|+
literal|" "
operator|+
name|Thread
operator|.
name|currentThread
argument_list|()
argument_list|)
throw|;
block|}
name|this
operator|.
name|doneTxid
operator|=
name|NOT_DONE
expr_stmt|;
name|this
operator|.
name|txid
operator|=
name|txnId
expr_stmt|;
name|this
operator|.
name|span
operator|=
name|span
expr_stmt|;
return|return
name|this
return|;
block|}
annotation|@
name|Override
specifier|public
specifier|synchronized
name|String
name|toString
parameter_list|()
block|{
return|return
literal|"done="
operator|+
name|isDone
argument_list|()
operator|+
literal|", txid="
operator|+
name|this
operator|.
name|txid
return|;
block|}
specifier|synchronized
name|long
name|getTxid
parameter_list|()
block|{
return|return
name|this
operator|.
name|txid
return|;
block|}
comment|/**    * Retrieve the {@code span} instance from this Future. EventHandler calls this method to continue    * the span. Thread waiting on this Future musn't call this method until AFTER calling    * {@link #get()} and the future has been released back to the originating thread.    */
specifier|synchronized
name|Span
name|getSpan
parameter_list|()
block|{
return|return
name|this
operator|.
name|span
return|;
block|}
comment|/**    * Used to re-attach a {@code span} to the Future. Called by the EventHandler after a it has    * completed processing and detached the span from its scope.    */
specifier|synchronized
name|void
name|setSpan
parameter_list|(
name|Span
name|span
parameter_list|)
block|{
name|this
operator|.
name|span
operator|=
name|span
expr_stmt|;
block|}
comment|/**    * @param txid the transaction id at which this future 'completed'.    * @param t Can be null. Set if we are 'completing' on error (and this 't' is the error).    * @return True if we successfully marked this outstanding future as completed/done. Returns false    *         if this future is already 'done' when this method called.    */
specifier|synchronized
name|boolean
name|done
parameter_list|(
specifier|final
name|long
name|txid
parameter_list|,
specifier|final
name|Throwable
name|t
parameter_list|)
block|{
if|if
condition|(
name|isDone
argument_list|()
condition|)
block|{
return|return
literal|false
return|;
block|}
name|this
operator|.
name|throwable
operator|=
name|t
expr_stmt|;
if|if
condition|(
name|txid
operator|<
name|this
operator|.
name|txid
condition|)
block|{
comment|// Something badly wrong.
if|if
condition|(
name|throwable
operator|==
literal|null
condition|)
block|{
name|this
operator|.
name|throwable
operator|=
operator|new
name|IllegalStateException
argument_list|(
literal|"done txid="
operator|+
name|txid
operator|+
literal|", my txid="
operator|+
name|this
operator|.
name|txid
argument_list|)
expr_stmt|;
block|}
block|}
comment|// Mark done.
name|this
operator|.
name|doneTxid
operator|=
name|txid
expr_stmt|;
comment|// Wake up waiting threads.
name|notify
argument_list|()
expr_stmt|;
return|return
literal|true
return|;
block|}
specifier|public
name|boolean
name|cancel
parameter_list|(
name|boolean
name|mayInterruptIfRunning
parameter_list|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
specifier|public
specifier|synchronized
name|long
name|get
parameter_list|()
throws|throws
name|InterruptedException
throws|,
name|ExecutionException
block|{
while|while
condition|(
operator|!
name|isDone
argument_list|()
condition|)
block|{
name|wait
argument_list|(
literal|1000
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|this
operator|.
name|throwable
operator|!=
literal|null
condition|)
block|{
throw|throw
operator|new
name|ExecutionException
argument_list|(
name|this
operator|.
name|throwable
argument_list|)
throw|;
block|}
return|return
name|this
operator|.
name|doneTxid
return|;
block|}
specifier|synchronized
name|boolean
name|isDone
parameter_list|()
block|{
return|return
name|this
operator|.
name|doneTxid
operator|!=
name|NOT_DONE
return|;
block|}
specifier|synchronized
name|boolean
name|isThrowable
parameter_list|()
block|{
return|return
name|isDone
argument_list|()
operator|&&
name|getThrowable
argument_list|()
operator|!=
literal|null
return|;
block|}
specifier|synchronized
name|Throwable
name|getThrowable
parameter_list|()
block|{
return|return
name|this
operator|.
name|throwable
return|;
block|}
block|}
end_class

end_unit

