begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one or more  * contributor license agreements. See the NOTICE file distributed with this  * work for additional information regarding copyright ownership. The ASF  * licenses this file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the  * License for the specific language governing permissions and limitations  * under the License.  */
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
name|lang
operator|.
name|management
operator|.
name|ManagementFactory
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
name|BlockingQueue
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
name|Executors
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
name|ScheduledExecutorService
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
name|TimeUnit
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
name|commons
operator|.
name|logging
operator|.
name|Log
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|LogFactory
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
name|regionserver
operator|.
name|HeapMemStoreLAB
operator|.
name|Chunk
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
name|util
operator|.
name|StringUtils
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
name|util
operator|.
name|concurrent
operator|.
name|ThreadFactoryBuilder
import|;
end_import

begin_comment
comment|/**  * A pool of {@link HeapMemStoreLAB$Chunk} instances.  *   * MemStoreChunkPool caches a number of retired chunks for reusing, it could  * decrease allocating bytes when writing, thereby optimizing the garbage  * collection on JVM.  *   * The pool instance is globally unique and could be obtained through  * {@link MemStoreChunkPool#getPool(Configuration)}  *   * {@link MemStoreChunkPool#getChunk()} is called when MemStoreLAB allocating  * bytes, and {@link MemStoreChunkPool#putbackChunks(BlockingQueue)} is called  * when MemStore clearing snapshot for flush  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|MemStoreChunkPool
block|{
specifier|private
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|MemStoreChunkPool
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|final
specifier|static
name|String
name|CHUNK_POOL_MAXSIZE_KEY
init|=
literal|"hbase.hregion.memstore.chunkpool.maxsize"
decl_stmt|;
specifier|final
specifier|static
name|String
name|CHUNK_POOL_INITIALSIZE_KEY
init|=
literal|"hbase.hregion.memstore.chunkpool.initialsize"
decl_stmt|;
specifier|final
specifier|static
name|float
name|POOL_MAX_SIZE_DEFAULT
init|=
literal|0.0f
decl_stmt|;
specifier|final
specifier|static
name|float
name|POOL_INITIAL_SIZE_DEFAULT
init|=
literal|0.0f
decl_stmt|;
comment|// Static reference to the MemStoreChunkPool
specifier|private
specifier|static
name|MemStoreChunkPool
name|globalInstance
decl_stmt|;
comment|/** Boolean whether we have disabled the memstore chunk pool entirely. */
specifier|static
name|boolean
name|chunkPoolDisabled
init|=
literal|false
decl_stmt|;
specifier|private
specifier|final
name|int
name|maxCount
decl_stmt|;
comment|// A queue of reclaimed chunks
specifier|private
specifier|final
name|BlockingQueue
argument_list|<
name|Chunk
argument_list|>
name|reclaimedChunks
decl_stmt|;
specifier|private
specifier|final
name|int
name|chunkSize
decl_stmt|;
comment|/** Statistics thread schedule pool */
specifier|private
specifier|final
name|ScheduledExecutorService
name|scheduleThreadPool
decl_stmt|;
comment|/** Statistics thread */
specifier|private
specifier|static
specifier|final
name|int
name|statThreadPeriod
init|=
literal|60
operator|*
literal|5
decl_stmt|;
specifier|private
name|AtomicLong
name|createdChunkCount
init|=
operator|new
name|AtomicLong
argument_list|()
decl_stmt|;
specifier|private
name|AtomicLong
name|reusedChunkCount
init|=
operator|new
name|AtomicLong
argument_list|()
decl_stmt|;
name|MemStoreChunkPool
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|int
name|chunkSize
parameter_list|,
name|int
name|maxCount
parameter_list|,
name|int
name|initialCount
parameter_list|)
block|{
name|this
operator|.
name|maxCount
operator|=
name|maxCount
expr_stmt|;
name|this
operator|.
name|chunkSize
operator|=
name|chunkSize
expr_stmt|;
name|this
operator|.
name|reclaimedChunks
operator|=
operator|new
name|LinkedBlockingQueue
argument_list|<
name|Chunk
argument_list|>
argument_list|()
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
name|initialCount
condition|;
name|i
operator|++
control|)
block|{
name|Chunk
name|chunk
init|=
operator|new
name|Chunk
argument_list|(
name|chunkSize
argument_list|)
decl_stmt|;
name|chunk
operator|.
name|init
argument_list|()
expr_stmt|;
name|reclaimedChunks
operator|.
name|add
argument_list|(
name|chunk
argument_list|)
expr_stmt|;
block|}
specifier|final
name|String
name|n
init|=
name|Thread
operator|.
name|currentThread
argument_list|()
operator|.
name|getName
argument_list|()
decl_stmt|;
name|scheduleThreadPool
operator|=
name|Executors
operator|.
name|newScheduledThreadPool
argument_list|(
literal|1
argument_list|,
operator|new
name|ThreadFactoryBuilder
argument_list|()
operator|.
name|setNameFormat
argument_list|(
name|n
operator|+
literal|"-MemStoreChunkPool Statistics"
argument_list|)
operator|.
name|setDaemon
argument_list|(
literal|true
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
name|this
operator|.
name|scheduleThreadPool
operator|.
name|scheduleAtFixedRate
argument_list|(
operator|new
name|StatisticsThread
argument_list|(
name|this
argument_list|)
argument_list|,
name|statThreadPeriod
argument_list|,
name|statThreadPeriod
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
expr_stmt|;
block|}
comment|/**    * Poll a chunk from the pool, reset it if not null, else create a new chunk    * to return    * @return a chunk    */
name|Chunk
name|getChunk
parameter_list|()
block|{
name|Chunk
name|chunk
init|=
name|reclaimedChunks
operator|.
name|poll
argument_list|()
decl_stmt|;
if|if
condition|(
name|chunk
operator|==
literal|null
condition|)
block|{
name|chunk
operator|=
operator|new
name|Chunk
argument_list|(
name|chunkSize
argument_list|)
expr_stmt|;
name|createdChunkCount
operator|.
name|incrementAndGet
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|chunk
operator|.
name|reset
argument_list|()
expr_stmt|;
name|reusedChunkCount
operator|.
name|incrementAndGet
argument_list|()
expr_stmt|;
block|}
return|return
name|chunk
return|;
block|}
comment|/**    * Add the chunks to the pool, when the pool achieves the max size, it will    * skip the remaining chunks    * @param chunks    */
name|void
name|putbackChunks
parameter_list|(
name|BlockingQueue
argument_list|<
name|Chunk
argument_list|>
name|chunks
parameter_list|)
block|{
name|int
name|maxNumToPutback
init|=
name|this
operator|.
name|maxCount
operator|-
name|reclaimedChunks
operator|.
name|size
argument_list|()
decl_stmt|;
if|if
condition|(
name|maxNumToPutback
operator|<=
literal|0
condition|)
block|{
return|return;
block|}
name|chunks
operator|.
name|drainTo
argument_list|(
name|reclaimedChunks
argument_list|,
name|maxNumToPutback
argument_list|)
expr_stmt|;
block|}
comment|/**    * Add the chunk to the pool, if the pool has achieved the max size, it will    * skip it    * @param chunk    */
name|void
name|putbackChunk
parameter_list|(
name|Chunk
name|chunk
parameter_list|)
block|{
if|if
condition|(
name|reclaimedChunks
operator|.
name|size
argument_list|()
operator|>=
name|this
operator|.
name|maxCount
condition|)
block|{
return|return;
block|}
name|reclaimedChunks
operator|.
name|add
argument_list|(
name|chunk
argument_list|)
expr_stmt|;
block|}
name|int
name|getPoolSize
parameter_list|()
block|{
return|return
name|this
operator|.
name|reclaimedChunks
operator|.
name|size
argument_list|()
return|;
block|}
comment|/*    * Only used in testing    */
name|void
name|clearChunks
parameter_list|()
block|{
name|this
operator|.
name|reclaimedChunks
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
specifier|private
specifier|static
class|class
name|StatisticsThread
extends|extends
name|Thread
block|{
name|MemStoreChunkPool
name|mcp
decl_stmt|;
specifier|public
name|StatisticsThread
parameter_list|(
name|MemStoreChunkPool
name|mcp
parameter_list|)
block|{
name|super
argument_list|(
literal|"MemStoreChunkPool.StatisticsThread"
argument_list|)
expr_stmt|;
name|setDaemon
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|this
operator|.
name|mcp
operator|=
name|mcp
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
name|mcp
operator|.
name|logStats
argument_list|()
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|logStats
parameter_list|()
block|{
if|if
condition|(
operator|!
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
return|return;
name|long
name|created
init|=
name|createdChunkCount
operator|.
name|get
argument_list|()
decl_stmt|;
name|long
name|reused
init|=
name|reusedChunkCount
operator|.
name|get
argument_list|()
decl_stmt|;
name|long
name|total
init|=
name|created
operator|+
name|reused
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Stats: current pool size="
operator|+
name|reclaimedChunks
operator|.
name|size
argument_list|()
operator|+
literal|",created chunk count="
operator|+
name|created
operator|+
literal|",reused chunk count="
operator|+
name|reused
operator|+
literal|",reuseRatio="
operator|+
operator|(
name|total
operator|==
literal|0
condition|?
literal|"0"
else|:
name|StringUtils
operator|.
name|formatPercent
argument_list|(
operator|(
name|float
operator|)
name|reused
operator|/
operator|(
name|float
operator|)
name|total
argument_list|,
literal|2
argument_list|)
operator|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * @param conf    * @return the global MemStoreChunkPool instance    */
specifier|static
name|MemStoreChunkPool
name|getPool
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
if|if
condition|(
name|globalInstance
operator|!=
literal|null
condition|)
return|return
name|globalInstance
return|;
if|if
condition|(
name|chunkPoolDisabled
condition|)
return|return
literal|null
return|;
synchronized|synchronized
init|(
name|MemStoreChunkPool
operator|.
name|class
init|)
block|{
if|if
condition|(
name|globalInstance
operator|!=
literal|null
condition|)
return|return
name|globalInstance
return|;
name|float
name|poolSizePercentage
init|=
name|conf
operator|.
name|getFloat
argument_list|(
name|CHUNK_POOL_MAXSIZE_KEY
argument_list|,
name|POOL_MAX_SIZE_DEFAULT
argument_list|)
decl_stmt|;
if|if
condition|(
name|poolSizePercentage
operator|<=
literal|0
condition|)
block|{
name|chunkPoolDisabled
operator|=
literal|true
expr_stmt|;
return|return
literal|null
return|;
block|}
if|if
condition|(
name|poolSizePercentage
operator|>
literal|1.0
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
name|CHUNK_POOL_MAXSIZE_KEY
operator|+
literal|" must be between 0.0 and 1.0"
argument_list|)
throw|;
block|}
name|long
name|heapMax
init|=
name|ManagementFactory
operator|.
name|getMemoryMXBean
argument_list|()
operator|.
name|getHeapMemoryUsage
argument_list|()
operator|.
name|getMax
argument_list|()
decl_stmt|;
name|long
name|globalMemStoreLimit
init|=
call|(
name|long
call|)
argument_list|(
name|heapMax
operator|*
name|MemStoreFlusher
operator|.
name|getGlobalMemStorePercent
argument_list|(
name|conf
argument_list|)
argument_list|)
decl_stmt|;
name|int
name|chunkSize
init|=
name|conf
operator|.
name|getInt
argument_list|(
name|HeapMemStoreLAB
operator|.
name|CHUNK_SIZE_KEY
argument_list|,
name|HeapMemStoreLAB
operator|.
name|CHUNK_SIZE_DEFAULT
argument_list|)
decl_stmt|;
name|int
name|maxCount
init|=
call|(
name|int
call|)
argument_list|(
name|globalMemStoreLimit
operator|*
name|poolSizePercentage
operator|/
name|chunkSize
argument_list|)
decl_stmt|;
name|float
name|initialCountPercentage
init|=
name|conf
operator|.
name|getFloat
argument_list|(
name|CHUNK_POOL_INITIALSIZE_KEY
argument_list|,
name|POOL_INITIAL_SIZE_DEFAULT
argument_list|)
decl_stmt|;
if|if
condition|(
name|initialCountPercentage
operator|>
literal|1.0
operator|||
name|initialCountPercentage
operator|<
literal|0
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
name|CHUNK_POOL_INITIALSIZE_KEY
operator|+
literal|" must be between 0.0 and 1.0"
argument_list|)
throw|;
block|}
name|int
name|initialCount
init|=
call|(
name|int
call|)
argument_list|(
name|initialCountPercentage
operator|*
name|maxCount
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Allocating MemStoreChunkPool with chunk size "
operator|+
name|StringUtils
operator|.
name|byteDesc
argument_list|(
name|chunkSize
argument_list|)
operator|+
literal|", max count "
operator|+
name|maxCount
operator|+
literal|", initial count "
operator|+
name|initialCount
argument_list|)
expr_stmt|;
name|globalInstance
operator|=
operator|new
name|MemStoreChunkPool
argument_list|(
name|conf
argument_list|,
name|chunkSize
argument_list|,
name|maxCount
argument_list|,
name|initialCount
argument_list|)
expr_stmt|;
return|return
name|globalInstance
return|;
block|}
block|}
block|}
end_class

end_unit

