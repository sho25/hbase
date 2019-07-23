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
name|io
operator|.
name|hfile
package|;
end_package

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
name|HConstants
operator|.
name|BUCKET_CACHE_IOENGINE_KEY
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
name|HConstants
operator|.
name|BUCKET_CACHE_SIZE_KEY
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
name|util
operator|.
name|concurrent
operator|.
name|ForkJoinPool
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
name|HConstants
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
name|hfile
operator|.
name|bucket
operator|.
name|BucketCache
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
name|util
operator|.
name|MemorySizeUtil
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
name|ReflectionUtils
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

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
specifier|final
class|class
name|BlockCacheFactory
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
name|BlockCacheFactory
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
comment|/**    * Configuration keys for Bucket cache    */
comment|/**    * Configuration key to cache block policy (Lru, TinyLfu).    */
specifier|public
specifier|static
specifier|final
name|String
name|BLOCKCACHE_POLICY_KEY
init|=
literal|"hfile.block.cache.policy"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|BLOCKCACHE_POLICY_DEFAULT
init|=
literal|"LRU"
decl_stmt|;
comment|/**    * If the chosen ioengine can persist its state across restarts, the path to the file to persist    * to. This file is NOT the data file. It is a file into which we will serialize the map of    * what is in the data file. For example, if you pass the following argument as    * BUCKET_CACHE_IOENGINE_KEY ("hbase.bucketcache.ioengine"),    *<code>file:/tmp/bucketcache.data</code>, then we will write the bucketcache data to the file    *<code>/tmp/bucketcache.data</code> but the metadata on where the data is in the supplied file    * is an in-memory map that needs to be persisted across restarts. Where to store this    * in-memory state is what you supply here: e.g.<code>/tmp/bucketcache.map</code>.    */
specifier|public
specifier|static
specifier|final
name|String
name|BUCKET_CACHE_PERSISTENT_PATH_KEY
init|=
literal|"hbase.bucketcache.persistent.path"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|BUCKET_CACHE_WRITER_THREADS_KEY
init|=
literal|"hbase.bucketcache.writer.threads"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|BUCKET_CACHE_WRITER_QUEUE_KEY
init|=
literal|"hbase.bucketcache.writer.queuelength"
decl_stmt|;
comment|/**    * A comma-delimited array of values for use as bucket sizes.    */
specifier|public
specifier|static
specifier|final
name|String
name|BUCKET_CACHE_BUCKETS_KEY
init|=
literal|"hbase.bucketcache.bucket.sizes"
decl_stmt|;
comment|/**    * Defaults for Bucket cache    */
specifier|public
specifier|static
specifier|final
name|int
name|DEFAULT_BUCKET_CACHE_WRITER_THREADS
init|=
literal|3
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|int
name|DEFAULT_BUCKET_CACHE_WRITER_QUEUE
init|=
literal|64
decl_stmt|;
comment|/**    * The target block size used by blockcache instances. Defaults to    * {@link HConstants#DEFAULT_BLOCKSIZE}.    */
specifier|public
specifier|static
specifier|final
name|String
name|BLOCKCACHE_BLOCKSIZE_KEY
init|=
literal|"hbase.blockcache.minblocksize"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|EXTERNAL_BLOCKCACHE_KEY
init|=
literal|"hbase.blockcache.use.external"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|boolean
name|EXTERNAL_BLOCKCACHE_DEFAULT
init|=
literal|false
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|EXTERNAL_BLOCKCACHE_CLASS_KEY
init|=
literal|"hbase.blockcache.external.class"
decl_stmt|;
comment|/**    * @deprecated use {@link BlockCacheFactory#BLOCKCACHE_BLOCKSIZE_KEY} instead.    */
annotation|@
name|Deprecated
specifier|static
specifier|final
name|String
name|DEPRECATED_BLOCKCACHE_BLOCKSIZE_KEY
init|=
literal|"hbase.offheapcache.minblocksize"
decl_stmt|;
comment|/**    * The config point hbase.offheapcache.minblocksize is completely wrong, which is replaced by    * {@link BlockCacheFactory#BLOCKCACHE_BLOCKSIZE_KEY}. Keep the old config key here for backward    * compatibility.    */
static|static
block|{
name|Configuration
operator|.
name|addDeprecation
argument_list|(
name|DEPRECATED_BLOCKCACHE_BLOCKSIZE_KEY
argument_list|,
name|BLOCKCACHE_BLOCKSIZE_KEY
argument_list|)
expr_stmt|;
block|}
specifier|private
name|BlockCacheFactory
parameter_list|()
block|{   }
specifier|public
specifier|static
name|BlockCache
name|createBlockCache
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
if|if
condition|(
name|conf
operator|.
name|get
argument_list|(
name|DEPRECATED_BLOCKCACHE_BLOCKSIZE_KEY
argument_list|)
operator|!=
literal|null
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"The config key {} is deprecated now, instead please use {}. In future release "
operator|+
literal|"we will remove the deprecated config."
argument_list|,
name|DEPRECATED_BLOCKCACHE_BLOCKSIZE_KEY
argument_list|,
name|BLOCKCACHE_BLOCKSIZE_KEY
argument_list|)
expr_stmt|;
block|}
name|FirstLevelBlockCache
name|l1Cache
init|=
name|createFirstLevelCache
argument_list|(
name|conf
argument_list|)
decl_stmt|;
if|if
condition|(
name|l1Cache
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
name|boolean
name|useExternal
init|=
name|conf
operator|.
name|getBoolean
argument_list|(
name|EXTERNAL_BLOCKCACHE_KEY
argument_list|,
name|EXTERNAL_BLOCKCACHE_DEFAULT
argument_list|)
decl_stmt|;
if|if
condition|(
name|useExternal
condition|)
block|{
name|BlockCache
name|l2CacheInstance
init|=
name|createExternalBlockcache
argument_list|(
name|conf
argument_list|)
decl_stmt|;
return|return
name|l2CacheInstance
operator|==
literal|null
condition|?
name|l1Cache
else|:
operator|new
name|InclusiveCombinedBlockCache
argument_list|(
name|l1Cache
argument_list|,
name|l2CacheInstance
argument_list|)
return|;
block|}
else|else
block|{
comment|// otherwise use the bucket cache.
name|BucketCache
name|bucketCache
init|=
name|createBucketCache
argument_list|(
name|conf
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|conf
operator|.
name|getBoolean
argument_list|(
literal|"hbase.bucketcache.combinedcache.enabled"
argument_list|,
literal|true
argument_list|)
condition|)
block|{
comment|// Non combined mode is off from 2.0
name|LOG
operator|.
name|warn
argument_list|(
literal|"From HBase 2.0 onwards only combined mode of LRU cache and bucket cache is available"
argument_list|)
expr_stmt|;
block|}
return|return
name|bucketCache
operator|==
literal|null
condition|?
name|l1Cache
else|:
operator|new
name|CombinedBlockCache
argument_list|(
name|l1Cache
argument_list|,
name|bucketCache
argument_list|)
return|;
block|}
block|}
specifier|private
specifier|static
name|FirstLevelBlockCache
name|createFirstLevelCache
parameter_list|(
specifier|final
name|Configuration
name|c
parameter_list|)
block|{
specifier|final
name|long
name|cacheSize
init|=
name|MemorySizeUtil
operator|.
name|getOnHeapCacheSize
argument_list|(
name|c
argument_list|)
decl_stmt|;
if|if
condition|(
name|cacheSize
operator|<
literal|0
condition|)
block|{
return|return
literal|null
return|;
block|}
name|String
name|policy
init|=
name|c
operator|.
name|get
argument_list|(
name|BLOCKCACHE_POLICY_KEY
argument_list|,
name|BLOCKCACHE_POLICY_DEFAULT
argument_list|)
decl_stmt|;
name|int
name|blockSize
init|=
name|c
operator|.
name|getInt
argument_list|(
name|BLOCKCACHE_BLOCKSIZE_KEY
argument_list|,
name|HConstants
operator|.
name|DEFAULT_BLOCKSIZE
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Allocating BlockCache size="
operator|+
name|StringUtils
operator|.
name|byteDesc
argument_list|(
name|cacheSize
argument_list|)
operator|+
literal|", blockSize="
operator|+
name|StringUtils
operator|.
name|byteDesc
argument_list|(
name|blockSize
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|policy
operator|.
name|equalsIgnoreCase
argument_list|(
literal|"LRU"
argument_list|)
condition|)
block|{
return|return
operator|new
name|LruBlockCache
argument_list|(
name|cacheSize
argument_list|,
name|blockSize
argument_list|,
literal|true
argument_list|,
name|c
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
name|policy
operator|.
name|equalsIgnoreCase
argument_list|(
literal|"TinyLFU"
argument_list|)
condition|)
block|{
return|return
operator|new
name|TinyLfuBlockCache
argument_list|(
name|cacheSize
argument_list|,
name|blockSize
argument_list|,
name|ForkJoinPool
operator|.
name|commonPool
argument_list|()
argument_list|,
name|c
argument_list|)
return|;
block|}
else|else
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Unknown policy: "
operator|+
name|policy
argument_list|)
throw|;
block|}
block|}
comment|/**    * Enum of all built in external block caches.    * This is used for config.    */
specifier|private
specifier|static
enum|enum
name|ExternalBlockCaches
block|{
name|memcached
argument_list|(
literal|"org.apache.hadoop.hbase.io.hfile.MemcachedBlockCache"
argument_list|)
block|;
comment|// TODO(eclark): Consider more. Redis, etc.
name|Class
argument_list|<
name|?
extends|extends
name|BlockCache
argument_list|>
name|clazz
decl_stmt|;
name|ExternalBlockCaches
parameter_list|(
name|String
name|clazzName
parameter_list|)
block|{
try|try
block|{
name|clazz
operator|=
operator|(
name|Class
argument_list|<
name|?
extends|extends
name|BlockCache
argument_list|>
operator|)
name|Class
operator|.
name|forName
argument_list|(
name|clazzName
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ClassNotFoundException
name|cnef
parameter_list|)
block|{
name|clazz
operator|=
literal|null
expr_stmt|;
block|}
block|}
name|ExternalBlockCaches
parameter_list|(
name|Class
argument_list|<
name|?
extends|extends
name|BlockCache
argument_list|>
name|clazz
parameter_list|)
block|{
name|this
operator|.
name|clazz
operator|=
name|clazz
expr_stmt|;
block|}
block|}
specifier|private
specifier|static
name|BlockCache
name|createExternalBlockcache
parameter_list|(
name|Configuration
name|c
parameter_list|)
block|{
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Trying to use External l2 cache"
argument_list|)
expr_stmt|;
block|}
name|Class
name|klass
init|=
literal|null
decl_stmt|;
comment|// Get the class, from the config. s
try|try
block|{
name|klass
operator|=
name|ExternalBlockCaches
operator|.
name|valueOf
argument_list|(
name|c
operator|.
name|get
argument_list|(
name|EXTERNAL_BLOCKCACHE_CLASS_KEY
argument_list|,
literal|"memcache"
argument_list|)
argument_list|)
operator|.
name|clazz
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|exception
parameter_list|)
block|{
try|try
block|{
name|klass
operator|=
name|c
operator|.
name|getClass
argument_list|(
name|EXTERNAL_BLOCKCACHE_CLASS_KEY
argument_list|,
name|Class
operator|.
name|forName
argument_list|(
literal|"org.apache.hadoop.hbase.io.hfile.MemcachedBlockCache"
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ClassNotFoundException
name|e
parameter_list|)
block|{
return|return
literal|null
return|;
block|}
block|}
comment|// Now try and create an instance of the block cache.
try|try
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Creating external block cache of type: "
operator|+
name|klass
argument_list|)
expr_stmt|;
return|return
operator|(
name|BlockCache
operator|)
name|ReflectionUtils
operator|.
name|newInstance
argument_list|(
name|klass
argument_list|,
name|c
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Error creating external block cache"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
return|return
literal|null
return|;
block|}
specifier|private
specifier|static
name|BucketCache
name|createBucketCache
parameter_list|(
name|Configuration
name|c
parameter_list|)
block|{
comment|// Check for L2.  ioengine name must be non-null.
name|String
name|bucketCacheIOEngineName
init|=
name|c
operator|.
name|get
argument_list|(
name|BUCKET_CACHE_IOENGINE_KEY
argument_list|,
literal|null
argument_list|)
decl_stmt|;
if|if
condition|(
name|bucketCacheIOEngineName
operator|==
literal|null
operator|||
name|bucketCacheIOEngineName
operator|.
name|length
argument_list|()
operator|<=
literal|0
condition|)
block|{
return|return
literal|null
return|;
block|}
name|int
name|blockSize
init|=
name|c
operator|.
name|getInt
argument_list|(
name|BLOCKCACHE_BLOCKSIZE_KEY
argument_list|,
name|HConstants
operator|.
name|DEFAULT_BLOCKSIZE
argument_list|)
decl_stmt|;
specifier|final
name|long
name|bucketCacheSize
init|=
name|MemorySizeUtil
operator|.
name|getBucketCacheSize
argument_list|(
name|c
argument_list|)
decl_stmt|;
if|if
condition|(
name|bucketCacheSize
operator|<=
literal|0
condition|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"bucketCacheSize<= 0; Check "
operator|+
name|BUCKET_CACHE_SIZE_KEY
operator|+
literal|" setting and/or server java heap size"
argument_list|)
throw|;
block|}
if|if
condition|(
name|c
operator|.
name|get
argument_list|(
literal|"hbase.bucketcache.percentage.in.combinedcache"
argument_list|)
operator|!=
literal|null
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Configuration 'hbase.bucketcache.percentage.in.combinedcache' is no longer "
operator|+
literal|"respected. See comments in http://hbase.apache.org/book.html#_changes_of_note"
argument_list|)
expr_stmt|;
block|}
name|int
name|writerThreads
init|=
name|c
operator|.
name|getInt
argument_list|(
name|BUCKET_CACHE_WRITER_THREADS_KEY
argument_list|,
name|DEFAULT_BUCKET_CACHE_WRITER_THREADS
argument_list|)
decl_stmt|;
name|int
name|writerQueueLen
init|=
name|c
operator|.
name|getInt
argument_list|(
name|BUCKET_CACHE_WRITER_QUEUE_KEY
argument_list|,
name|DEFAULT_BUCKET_CACHE_WRITER_QUEUE
argument_list|)
decl_stmt|;
name|String
name|persistentPath
init|=
name|c
operator|.
name|get
argument_list|(
name|BUCKET_CACHE_PERSISTENT_PATH_KEY
argument_list|)
decl_stmt|;
name|String
index|[]
name|configuredBucketSizes
init|=
name|c
operator|.
name|getStrings
argument_list|(
name|BUCKET_CACHE_BUCKETS_KEY
argument_list|)
decl_stmt|;
name|int
index|[]
name|bucketSizes
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|configuredBucketSizes
operator|!=
literal|null
condition|)
block|{
name|bucketSizes
operator|=
operator|new
name|int
index|[
name|configuredBucketSizes
operator|.
name|length
index|]
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
name|configuredBucketSizes
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|int
name|bucketSize
init|=
name|Integer
operator|.
name|parseInt
argument_list|(
name|configuredBucketSizes
index|[
name|i
index|]
operator|.
name|trim
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|bucketSize
operator|%
literal|256
operator|!=
literal|0
condition|)
block|{
comment|// We need all the bucket sizes to be multiples of 256. Having all the configured bucket
comment|// sizes to be multiples of 256 will ensure that the block offsets within buckets,
comment|// that are calculated, will also be multiples of 256.
comment|// See BucketEntry where offset to each block is represented using 5 bytes (instead of 8
comment|// bytes long). We would like to save heap overhead as less as possible.
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Illegal value: "
operator|+
name|bucketSize
operator|+
literal|" configured for '"
operator|+
name|BUCKET_CACHE_BUCKETS_KEY
operator|+
literal|"'. All bucket sizes to be multiples of 256"
argument_list|)
throw|;
block|}
name|bucketSizes
index|[
name|i
index|]
operator|=
name|bucketSize
expr_stmt|;
block|}
block|}
name|BucketCache
name|bucketCache
init|=
literal|null
decl_stmt|;
try|try
block|{
name|int
name|ioErrorsTolerationDuration
init|=
name|c
operator|.
name|getInt
argument_list|(
literal|"hbase.bucketcache.ioengine.errors.tolerated.duration"
argument_list|,
name|BucketCache
operator|.
name|DEFAULT_ERROR_TOLERATION_DURATION
argument_list|)
decl_stmt|;
comment|// Bucket cache logs its stats on creation internal to the constructor.
name|bucketCache
operator|=
operator|new
name|BucketCache
argument_list|(
name|bucketCacheIOEngineName
argument_list|,
name|bucketCacheSize
argument_list|,
name|blockSize
argument_list|,
name|bucketSizes
argument_list|,
name|writerThreads
argument_list|,
name|writerQueueLen
argument_list|,
name|persistentPath
argument_list|,
name|ioErrorsTolerationDuration
argument_list|,
name|c
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|ioex
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Can't instantiate bucket cache"
argument_list|,
name|ioex
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|ioex
argument_list|)
throw|;
block|}
return|return
name|bucketCache
return|;
block|}
block|}
end_class

end_unit

