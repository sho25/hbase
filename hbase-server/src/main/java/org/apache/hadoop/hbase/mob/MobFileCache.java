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
name|mob
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
name|ArrayList
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collections
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
name|Map
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
name|ConcurrentHashMap
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
name|fs
operator|.
name|FileSystem
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
name|fs
operator|.
name|Path
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
name|IdLock
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
comment|/**  * The cache for mob files.  * This cache doesn't cache the mob file blocks. It only caches the references of mob files.  * We are doing this to avoid opening and closing mob files all the time. We just keep  * references open.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|MobFileCache
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
name|MobFileCache
operator|.
name|class
argument_list|)
decl_stmt|;
comment|/*    * Eviction and statistics thread. Periodically run to print the statistics and    * evict the lru cached mob files when the count of the cached files is larger    * than the threshold.    */
specifier|static
class|class
name|EvictionThread
extends|extends
name|Thread
block|{
name|MobFileCache
name|lru
decl_stmt|;
specifier|public
name|EvictionThread
parameter_list|(
name|MobFileCache
name|lru
parameter_list|)
block|{
name|super
argument_list|(
literal|"MobFileCache.EvictionThread"
argument_list|)
expr_stmt|;
name|setDaemon
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|this
operator|.
name|lru
operator|=
name|lru
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
name|lru
operator|.
name|evict
argument_list|()
expr_stmt|;
block|}
block|}
comment|// a ConcurrentHashMap, accesses to this map are synchronized.
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|CachedMobFile
argument_list|>
name|map
init|=
literal|null
decl_stmt|;
comment|// caches access count
specifier|private
specifier|final
name|AtomicLong
name|count
init|=
operator|new
name|AtomicLong
argument_list|(
literal|0
argument_list|)
decl_stmt|;
specifier|private
name|long
name|lastAccess
init|=
literal|0
decl_stmt|;
specifier|private
specifier|final
name|AtomicLong
name|miss
init|=
operator|new
name|AtomicLong
argument_list|(
literal|0
argument_list|)
decl_stmt|;
specifier|private
name|long
name|lastMiss
init|=
literal|0
decl_stmt|;
specifier|private
specifier|final
name|AtomicLong
name|evictedFileCount
init|=
operator|new
name|AtomicLong
argument_list|(
literal|0
argument_list|)
decl_stmt|;
specifier|private
name|long
name|lastEvictedFileCount
init|=
literal|0
decl_stmt|;
comment|// a lock to sync the evict to guarantee the eviction occurs in sequence.
comment|// the method evictFile is not sync by this lock, the ConcurrentHashMap does the sync there.
specifier|private
specifier|final
name|ReentrantLock
name|evictionLock
init|=
operator|new
name|ReentrantLock
argument_list|(
literal|true
argument_list|)
decl_stmt|;
comment|//stripes lock on each mob file based on its hash. Sync the openFile/closeFile operations.
specifier|private
specifier|final
name|IdLock
name|keyLock
init|=
operator|new
name|IdLock
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|ScheduledExecutorService
name|scheduleThreadPool
init|=
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
literal|"MobFileCache #%d"
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
decl_stmt|;
specifier|private
specifier|final
name|Configuration
name|conf
decl_stmt|;
comment|// the count of the cached references to mob files
specifier|private
specifier|final
name|int
name|mobFileMaxCacheSize
decl_stmt|;
specifier|private
specifier|final
name|boolean
name|isCacheEnabled
decl_stmt|;
specifier|private
name|float
name|evictRemainRatio
decl_stmt|;
specifier|public
name|MobFileCache
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
name|this
operator|.
name|mobFileMaxCacheSize
operator|=
name|conf
operator|.
name|getInt
argument_list|(
name|MobConstants
operator|.
name|MOB_FILE_CACHE_SIZE_KEY
argument_list|,
name|MobConstants
operator|.
name|DEFAULT_MOB_FILE_CACHE_SIZE
argument_list|)
expr_stmt|;
name|isCacheEnabled
operator|=
operator|(
name|mobFileMaxCacheSize
operator|>
literal|0
operator|)
expr_stmt|;
name|map
operator|=
operator|new
name|ConcurrentHashMap
argument_list|<
name|String
argument_list|,
name|CachedMobFile
argument_list|>
argument_list|(
name|mobFileMaxCacheSize
argument_list|)
expr_stmt|;
if|if
condition|(
name|isCacheEnabled
condition|)
block|{
name|long
name|period
init|=
name|conf
operator|.
name|getLong
argument_list|(
name|MobConstants
operator|.
name|MOB_CACHE_EVICT_PERIOD
argument_list|,
name|MobConstants
operator|.
name|DEFAULT_MOB_CACHE_EVICT_PERIOD
argument_list|)
decl_stmt|;
comment|// in seconds
name|evictRemainRatio
operator|=
name|conf
operator|.
name|getFloat
argument_list|(
name|MobConstants
operator|.
name|MOB_CACHE_EVICT_REMAIN_RATIO
argument_list|,
name|MobConstants
operator|.
name|DEFAULT_EVICT_REMAIN_RATIO
argument_list|)
expr_stmt|;
if|if
condition|(
name|evictRemainRatio
operator|<
literal|0.0
condition|)
block|{
name|evictRemainRatio
operator|=
literal|0.0f
expr_stmt|;
name|LOG
operator|.
name|warn
argument_list|(
name|MobConstants
operator|.
name|MOB_CACHE_EVICT_REMAIN_RATIO
operator|+
literal|" is less than 0.0, 0.0 is used."
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|evictRemainRatio
operator|>
literal|1.0
condition|)
block|{
name|evictRemainRatio
operator|=
literal|1.0f
expr_stmt|;
name|LOG
operator|.
name|warn
argument_list|(
name|MobConstants
operator|.
name|MOB_CACHE_EVICT_REMAIN_RATIO
operator|+
literal|" is larger than 1.0, 1.0 is used."
argument_list|)
expr_stmt|;
block|}
name|this
operator|.
name|scheduleThreadPool
operator|.
name|scheduleAtFixedRate
argument_list|(
operator|new
name|EvictionThread
argument_list|(
name|this
argument_list|)
argument_list|,
name|period
argument_list|,
name|period
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
expr_stmt|;
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"MobFileCache is initialized, and the cache size is "
operator|+
name|mobFileMaxCacheSize
argument_list|)
expr_stmt|;
block|}
comment|/**    * Evicts the lru cached mob files when the count of the cached files is larger    * than the threshold.    */
specifier|public
name|void
name|evict
parameter_list|()
block|{
if|if
condition|(
name|isCacheEnabled
condition|)
block|{
comment|// Ensure only one eviction at a time
if|if
condition|(
operator|!
name|evictionLock
operator|.
name|tryLock
argument_list|()
condition|)
block|{
return|return;
block|}
name|printStatistics
argument_list|()
expr_stmt|;
name|List
argument_list|<
name|CachedMobFile
argument_list|>
name|evictedFiles
init|=
operator|new
name|ArrayList
argument_list|<
name|CachedMobFile
argument_list|>
argument_list|()
decl_stmt|;
try|try
block|{
if|if
condition|(
name|map
operator|.
name|size
argument_list|()
operator|<=
name|mobFileMaxCacheSize
condition|)
block|{
return|return;
block|}
name|List
argument_list|<
name|CachedMobFile
argument_list|>
name|files
init|=
operator|new
name|ArrayList
argument_list|<
name|CachedMobFile
argument_list|>
argument_list|(
name|map
operator|.
name|values
argument_list|()
argument_list|)
decl_stmt|;
name|Collections
operator|.
name|sort
argument_list|(
name|files
argument_list|)
expr_stmt|;
name|int
name|start
init|=
call|(
name|int
call|)
argument_list|(
name|mobFileMaxCacheSize
operator|*
name|evictRemainRatio
argument_list|)
decl_stmt|;
if|if
condition|(
name|start
operator|>=
literal|0
condition|)
block|{
for|for
control|(
name|int
name|i
init|=
name|start
init|;
name|i
operator|<
name|files
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|String
name|name
init|=
name|files
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|getFileName
argument_list|()
decl_stmt|;
name|CachedMobFile
name|evictedFile
init|=
name|map
operator|.
name|remove
argument_list|(
name|name
argument_list|)
decl_stmt|;
if|if
condition|(
name|evictedFile
operator|!=
literal|null
condition|)
block|{
name|evictedFiles
operator|.
name|add
argument_list|(
name|evictedFile
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
finally|finally
block|{
name|evictionLock
operator|.
name|unlock
argument_list|()
expr_stmt|;
block|}
comment|// EvictionLock is released. Close the evicted files one by one.
comment|// The closes are sync in the closeFile method.
for|for
control|(
name|CachedMobFile
name|evictedFile
range|:
name|evictedFiles
control|)
block|{
name|closeFile
argument_list|(
name|evictedFile
argument_list|)
expr_stmt|;
block|}
name|evictedFileCount
operator|.
name|addAndGet
argument_list|(
name|evictedFiles
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Evicts the cached file by the name.    * @param fileName The name of a cached file.    */
specifier|public
name|void
name|evictFile
parameter_list|(
name|String
name|fileName
parameter_list|)
block|{
if|if
condition|(
name|isCacheEnabled
condition|)
block|{
name|IdLock
operator|.
name|Entry
name|lockEntry
init|=
literal|null
decl_stmt|;
try|try
block|{
comment|// obtains the lock to close the cached file.
name|lockEntry
operator|=
name|keyLock
operator|.
name|getLockEntry
argument_list|(
name|fileName
operator|.
name|hashCode
argument_list|()
argument_list|)
expr_stmt|;
name|CachedMobFile
name|evictedFile
init|=
name|map
operator|.
name|remove
argument_list|(
name|fileName
argument_list|)
decl_stmt|;
if|if
condition|(
name|evictedFile
operator|!=
literal|null
condition|)
block|{
name|evictedFile
operator|.
name|close
argument_list|()
expr_stmt|;
name|evictedFileCount
operator|.
name|incrementAndGet
argument_list|()
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Failed to evict the file "
operator|+
name|fileName
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
if|if
condition|(
name|lockEntry
operator|!=
literal|null
condition|)
block|{
name|keyLock
operator|.
name|releaseLockEntry
argument_list|(
name|lockEntry
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
comment|/**    * Opens a mob file.    * @param fs The current file system.    * @param path The file path.    * @param cacheConf The current MobCacheConfig    * @return A opened mob file.    * @throws IOException    */
specifier|public
name|MobFile
name|openFile
parameter_list|(
name|FileSystem
name|fs
parameter_list|,
name|Path
name|path
parameter_list|,
name|MobCacheConfig
name|cacheConf
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
operator|!
name|isCacheEnabled
condition|)
block|{
name|MobFile
name|mobFile
init|=
name|MobFile
operator|.
name|create
argument_list|(
name|fs
argument_list|,
name|path
argument_list|,
name|conf
argument_list|,
name|cacheConf
argument_list|)
decl_stmt|;
name|mobFile
operator|.
name|open
argument_list|()
expr_stmt|;
return|return
name|mobFile
return|;
block|}
else|else
block|{
name|String
name|fileName
init|=
name|path
operator|.
name|getName
argument_list|()
decl_stmt|;
name|CachedMobFile
name|cached
init|=
name|map
operator|.
name|get
argument_list|(
name|fileName
argument_list|)
decl_stmt|;
name|IdLock
operator|.
name|Entry
name|lockEntry
init|=
name|keyLock
operator|.
name|getLockEntry
argument_list|(
name|fileName
operator|.
name|hashCode
argument_list|()
argument_list|)
decl_stmt|;
try|try
block|{
if|if
condition|(
name|cached
operator|==
literal|null
condition|)
block|{
name|cached
operator|=
name|map
operator|.
name|get
argument_list|(
name|fileName
argument_list|)
expr_stmt|;
if|if
condition|(
name|cached
operator|==
literal|null
condition|)
block|{
if|if
condition|(
name|map
operator|.
name|size
argument_list|()
operator|>
name|mobFileMaxCacheSize
condition|)
block|{
name|evict
argument_list|()
expr_stmt|;
block|}
name|cached
operator|=
name|CachedMobFile
operator|.
name|create
argument_list|(
name|fs
argument_list|,
name|path
argument_list|,
name|conf
argument_list|,
name|cacheConf
argument_list|)
expr_stmt|;
name|cached
operator|.
name|open
argument_list|()
expr_stmt|;
name|map
operator|.
name|put
argument_list|(
name|fileName
argument_list|,
name|cached
argument_list|)
expr_stmt|;
name|miss
operator|.
name|incrementAndGet
argument_list|()
expr_stmt|;
block|}
block|}
name|cached
operator|.
name|open
argument_list|()
expr_stmt|;
name|cached
operator|.
name|access
argument_list|(
name|count
operator|.
name|incrementAndGet
argument_list|()
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|keyLock
operator|.
name|releaseLockEntry
argument_list|(
name|lockEntry
argument_list|)
expr_stmt|;
block|}
return|return
name|cached
return|;
block|}
block|}
comment|/**    * Closes a mob file.    * @param file The mob file that needs to be closed.    */
specifier|public
name|void
name|closeFile
parameter_list|(
name|MobFile
name|file
parameter_list|)
block|{
name|IdLock
operator|.
name|Entry
name|lockEntry
init|=
literal|null
decl_stmt|;
try|try
block|{
if|if
condition|(
operator|!
name|isCacheEnabled
condition|)
block|{
name|file
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|lockEntry
operator|=
name|keyLock
operator|.
name|getLockEntry
argument_list|(
name|file
operator|.
name|getFileName
argument_list|()
operator|.
name|hashCode
argument_list|()
argument_list|)
expr_stmt|;
name|file
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"MobFileCache, Exception happen during close "
operator|+
name|file
operator|.
name|getFileName
argument_list|()
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
if|if
condition|(
name|lockEntry
operator|!=
literal|null
condition|)
block|{
name|keyLock
operator|.
name|releaseLockEntry
argument_list|(
name|lockEntry
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|public
name|void
name|shutdown
parameter_list|()
block|{
name|this
operator|.
name|scheduleThreadPool
operator|.
name|shutdown
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
literal|100
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
operator|!
name|this
operator|.
name|scheduleThreadPool
operator|.
name|isShutdown
argument_list|()
condition|)
block|{
try|try
block|{
name|Thread
operator|.
name|sleep
argument_list|(
literal|10
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Interrupted while sleeping"
argument_list|)
expr_stmt|;
name|Thread
operator|.
name|currentThread
argument_list|()
operator|.
name|interrupt
argument_list|()
expr_stmt|;
break|break;
block|}
block|}
block|}
if|if
condition|(
operator|!
name|this
operator|.
name|scheduleThreadPool
operator|.
name|isShutdown
argument_list|()
condition|)
block|{
name|List
argument_list|<
name|Runnable
argument_list|>
name|runnables
init|=
name|this
operator|.
name|scheduleThreadPool
operator|.
name|shutdownNow
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Still running "
operator|+
name|runnables
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Gets the count of cached mob files.    * @return The count of the cached mob files.    */
specifier|public
name|int
name|getCacheSize
parameter_list|()
block|{
return|return
name|map
operator|==
literal|null
condition|?
literal|0
else|:
name|map
operator|.
name|size
argument_list|()
return|;
block|}
comment|/**    * Gets the count of accesses to the mob file cache.    * @return The count of accesses to the mob file cache.    */
specifier|public
name|long
name|getAccessCount
parameter_list|()
block|{
return|return
name|count
operator|.
name|get
argument_list|()
return|;
block|}
comment|/**    * Gets the count of misses to the mob file cache.    * @return The count of misses to the mob file cache.    */
specifier|public
name|long
name|getMissCount
parameter_list|()
block|{
return|return
name|miss
operator|.
name|get
argument_list|()
return|;
block|}
comment|/**    * Gets the number of items evicted from the mob file cache.    * @return The number of items evicted from the mob file cache.    */
specifier|public
name|long
name|getEvictedFileCount
parameter_list|()
block|{
return|return
name|evictedFileCount
operator|.
name|get
argument_list|()
return|;
block|}
comment|/**    * Gets the hit ratio to the mob file cache.    * @return The hit ratio to the mob file cache.    */
specifier|public
name|double
name|getHitRatio
parameter_list|()
block|{
return|return
name|count
operator|.
name|get
argument_list|()
operator|==
literal|0
condition|?
literal|0
else|:
operator|(
call|(
name|float
call|)
argument_list|(
name|count
operator|.
name|get
argument_list|()
operator|-
name|miss
operator|.
name|get
argument_list|()
argument_list|)
operator|)
operator|/
operator|(
name|float
operator|)
name|count
operator|.
name|get
argument_list|()
return|;
block|}
comment|/**    * Prints the statistics.    */
specifier|public
name|void
name|printStatistics
parameter_list|()
block|{
name|long
name|access
init|=
name|count
operator|.
name|get
argument_list|()
operator|-
name|lastAccess
decl_stmt|;
name|long
name|missed
init|=
name|miss
operator|.
name|get
argument_list|()
operator|-
name|lastMiss
decl_stmt|;
name|long
name|evicted
init|=
name|evictedFileCount
operator|.
name|get
argument_list|()
operator|-
name|lastEvictedFileCount
decl_stmt|;
name|int
name|hitRatio
init|=
name|access
operator|==
literal|0
condition|?
literal|0
else|:
call|(
name|int
call|)
argument_list|(
operator|(
call|(
name|float
call|)
argument_list|(
name|access
operator|-
name|missed
argument_list|)
operator|)
operator|/
operator|(
name|float
operator|)
name|access
operator|*
literal|100
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"MobFileCache Statistics, access: "
operator|+
name|access
operator|+
literal|", miss: "
operator|+
name|missed
operator|+
literal|", hit: "
operator|+
operator|(
name|access
operator|-
name|missed
operator|)
operator|+
literal|", hit ratio: "
operator|+
name|hitRatio
operator|+
literal|"%, evicted files: "
operator|+
name|evicted
argument_list|)
expr_stmt|;
name|lastAccess
operator|+=
name|access
expr_stmt|;
name|lastMiss
operator|+=
name|missed
expr_stmt|;
name|lastEvictedFileCount
operator|+=
name|evicted
expr_stmt|;
block|}
block|}
end_class

end_unit

