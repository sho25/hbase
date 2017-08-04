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
name|client
package|;
end_package

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
name|annotations
operator|.
name|VisibleForTesting
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|client
operator|.
name|ConnectionUtils
operator|.
name|calcEstimatedSize
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
name|io
operator|.
name|InterruptedIOException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Queue
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
name|ConcurrentLinkedQueue
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
name|ExecutorService
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
name|LinkedBlockingQueue
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
name|AtomicBoolean
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
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|locks
operator|.
name|Condition
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
name|locks
operator|.
name|Lock
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
name|locks
operator|.
name|ReentrantLock
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|function
operator|.
name|Consumer
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
name|conf
operator|.
name|Configuration
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
name|TableName
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
name|ipc
operator|.
name|RpcControllerFactory
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
name|Threads
import|;
end_import

begin_comment
comment|/**  * ClientAsyncPrefetchScanner implements async scanner behaviour.  * Specifically, the cache used by this scanner is a concurrent queue which allows both  * the producer (hbase client) and consumer (application) to access the queue in parallel.  * The number of rows returned in a prefetch is defined by the caching factor and the result size  * factor.  * This class allocates a buffer cache, whose size is a function of both factors.  * The prefetch is invoked when the cache is half­filled, instead of waiting for it to be empty.  * This is defined in the method {@link ClientAsyncPrefetchScanner#prefetchCondition()}.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|ClientAsyncPrefetchScanner
extends|extends
name|ClientSimpleScanner
block|{
specifier|private
name|long
name|maxCacheSize
decl_stmt|;
specifier|private
name|AtomicLong
name|cacheSizeInBytes
decl_stmt|;
comment|// exception queue (from prefetch to main scan execution)
specifier|private
name|Queue
argument_list|<
name|Exception
argument_list|>
name|exceptionsQueue
decl_stmt|;
comment|// prefetch thread to be executed asynchronously
specifier|private
name|Thread
name|prefetcher
decl_stmt|;
comment|// used for testing
specifier|private
name|Consumer
argument_list|<
name|Boolean
argument_list|>
name|prefetchListener
decl_stmt|;
specifier|private
specifier|final
name|Lock
name|lock
init|=
operator|new
name|ReentrantLock
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|Condition
name|notEmpty
init|=
name|lock
operator|.
name|newCondition
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|Condition
name|notFull
init|=
name|lock
operator|.
name|newCondition
argument_list|()
decl_stmt|;
specifier|public
name|ClientAsyncPrefetchScanner
parameter_list|(
name|Configuration
name|configuration
parameter_list|,
name|Scan
name|scan
parameter_list|,
name|TableName
name|name
parameter_list|,
name|ClusterConnection
name|connection
parameter_list|,
name|RpcRetryingCallerFactory
name|rpcCallerFactory
parameter_list|,
name|RpcControllerFactory
name|rpcControllerFactory
parameter_list|,
name|ExecutorService
name|pool
parameter_list|,
name|int
name|replicaCallTimeoutMicroSecondScan
parameter_list|)
throws|throws
name|IOException
block|{
name|super
argument_list|(
name|configuration
argument_list|,
name|scan
argument_list|,
name|name
argument_list|,
name|connection
argument_list|,
name|rpcCallerFactory
argument_list|,
name|rpcControllerFactory
argument_list|,
name|pool
argument_list|,
name|replicaCallTimeoutMicroSecondScan
argument_list|)
expr_stmt|;
block|}
annotation|@
name|VisibleForTesting
name|void
name|setPrefetchListener
parameter_list|(
name|Consumer
argument_list|<
name|Boolean
argument_list|>
name|prefetchListener
parameter_list|)
block|{
name|this
operator|.
name|prefetchListener
operator|=
name|prefetchListener
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|initCache
parameter_list|()
block|{
comment|// concurrent cache
name|maxCacheSize
operator|=
name|resultSize2CacheSize
argument_list|(
name|maxScannerResultSize
argument_list|)
expr_stmt|;
name|cache
operator|=
operator|new
name|LinkedBlockingQueue
argument_list|<>
argument_list|()
expr_stmt|;
name|cacheSizeInBytes
operator|=
operator|new
name|AtomicLong
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|exceptionsQueue
operator|=
operator|new
name|ConcurrentLinkedQueue
argument_list|<>
argument_list|()
expr_stmt|;
name|prefetcher
operator|=
operator|new
name|Thread
argument_list|(
operator|new
name|PrefetchRunnable
argument_list|()
argument_list|)
expr_stmt|;
name|Threads
operator|.
name|setDaemonThreadRunning
argument_list|(
name|prefetcher
argument_list|,
name|tableName
operator|+
literal|".asyncPrefetcher"
argument_list|)
expr_stmt|;
block|}
specifier|private
name|long
name|resultSize2CacheSize
parameter_list|(
name|long
name|maxResultSize
parameter_list|)
block|{
comment|// * 2 if possible
return|return
name|maxResultSize
operator|>
name|Long
operator|.
name|MAX_VALUE
operator|/
literal|2
condition|?
name|maxResultSize
else|:
name|maxResultSize
operator|*
literal|2
return|;
block|}
annotation|@
name|Override
specifier|public
name|Result
name|next
parameter_list|()
throws|throws
name|IOException
block|{
try|try
block|{
name|lock
operator|.
name|lock
argument_list|()
expr_stmt|;
while|while
condition|(
name|cache
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|handleException
argument_list|()
expr_stmt|;
if|if
condition|(
name|this
operator|.
name|closed
condition|)
block|{
return|return
literal|null
return|;
block|}
try|try
block|{
name|notEmpty
operator|.
name|await
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|InterruptedIOException
argument_list|(
literal|"Interrupted when wait to load cache"
argument_list|)
throw|;
block|}
block|}
name|Result
name|result
init|=
name|pollCache
argument_list|()
decl_stmt|;
if|if
condition|(
name|prefetchCondition
argument_list|()
condition|)
block|{
name|notFull
operator|.
name|signalAll
argument_list|()
expr_stmt|;
block|}
return|return
name|result
return|;
block|}
finally|finally
block|{
name|lock
operator|.
name|unlock
argument_list|()
expr_stmt|;
name|handleException
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|()
block|{
try|try
block|{
name|lock
operator|.
name|lock
argument_list|()
expr_stmt|;
name|super
operator|.
name|close
argument_list|()
expr_stmt|;
name|closed
operator|=
literal|true
expr_stmt|;
name|notFull
operator|.
name|signalAll
argument_list|()
expr_stmt|;
name|notEmpty
operator|.
name|signalAll
argument_list|()
expr_stmt|;
block|}
finally|finally
block|{
name|lock
operator|.
name|unlock
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|protected
name|void
name|addEstimatedSize
parameter_list|(
name|long
name|estimatedSize
parameter_list|)
block|{
name|cacheSizeInBytes
operator|.
name|addAndGet
argument_list|(
name|estimatedSize
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|handleException
parameter_list|()
throws|throws
name|IOException
block|{
comment|//The prefetch task running in the background puts any exception it
comment|//catches into this exception queue.
comment|// Rethrow the exception so the application can handle it.
while|while
condition|(
operator|!
name|exceptionsQueue
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|Exception
name|first
init|=
name|exceptionsQueue
operator|.
name|peek
argument_list|()
decl_stmt|;
name|first
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
if|if
condition|(
name|first
operator|instanceof
name|IOException
condition|)
block|{
throw|throw
operator|(
name|IOException
operator|)
name|first
throw|;
block|}
throw|throw
operator|(
name|RuntimeException
operator|)
name|first
throw|;
block|}
block|}
specifier|private
name|boolean
name|prefetchCondition
parameter_list|()
block|{
return|return
name|cacheSizeInBytes
operator|.
name|get
argument_list|()
operator|<
name|maxCacheSize
operator|/
literal|2
return|;
block|}
specifier|private
name|Result
name|pollCache
parameter_list|()
block|{
name|Result
name|res
init|=
name|cache
operator|.
name|poll
argument_list|()
decl_stmt|;
name|long
name|estimatedSize
init|=
name|calcEstimatedSize
argument_list|(
name|res
argument_list|)
decl_stmt|;
name|addEstimatedSize
argument_list|(
operator|-
name|estimatedSize
argument_list|)
expr_stmt|;
return|return
name|res
return|;
block|}
specifier|private
class|class
name|PrefetchRunnable
implements|implements
name|Runnable
block|{
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
while|while
condition|(
operator|!
name|closed
condition|)
block|{
name|boolean
name|succeed
init|=
literal|false
decl_stmt|;
try|try
block|{
name|lock
operator|.
name|lock
argument_list|()
expr_stmt|;
while|while
condition|(
operator|!
name|prefetchCondition
argument_list|()
condition|)
block|{
name|notFull
operator|.
name|await
argument_list|()
expr_stmt|;
block|}
name|loadCache
argument_list|()
expr_stmt|;
name|succeed
operator|=
literal|true
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|exceptionsQueue
operator|.
name|add
argument_list|(
name|e
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|notEmpty
operator|.
name|signalAll
argument_list|()
expr_stmt|;
name|lock
operator|.
name|unlock
argument_list|()
expr_stmt|;
if|if
condition|(
name|prefetchListener
operator|!=
literal|null
condition|)
block|{
name|prefetchListener
operator|.
name|accept
argument_list|(
name|succeed
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
block|}
block|}
end_class

end_unit

