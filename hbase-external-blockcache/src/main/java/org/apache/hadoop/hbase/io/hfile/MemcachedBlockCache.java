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
name|io
operator|.
name|hfile
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
name|net
operator|.
name|InetSocketAddress
import|;
end_import

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
name|Iterator
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
name|NoSuchElementException
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
name|ExecutionException
import|;
end_import

begin_import
import|import
name|net
operator|.
name|spy
operator|.
name|memcached
operator|.
name|CachedData
import|;
end_import

begin_import
import|import
name|net
operator|.
name|spy
operator|.
name|memcached
operator|.
name|ConnectionFactoryBuilder
import|;
end_import

begin_import
import|import
name|net
operator|.
name|spy
operator|.
name|memcached
operator|.
name|FailureMode
import|;
end_import

begin_import
import|import
name|net
operator|.
name|spy
operator|.
name|memcached
operator|.
name|MemcachedClient
import|;
end_import

begin_import
import|import
name|net
operator|.
name|spy
operator|.
name|memcached
operator|.
name|transcoders
operator|.
name|Transcoder
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
name|Cacheable
operator|.
name|MemoryType
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
name|nio
operator|.
name|ByteBuff
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
name|nio
operator|.
name|SingleByteBuff
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
name|trace
operator|.
name|TraceUtil
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
name|Addressing
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
name|core
operator|.
name|TraceScope
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

begin_comment
comment|/**  * Class to store blocks into memcached.  * This should only be used on a cluster of Memcached daemons that are tuned well and have a  * good network connection to the HBase regionservers. Any other use will likely slow down HBase  * greatly.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|MemcachedBlockCache
implements|implements
name|BlockCache
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
name|MemcachedBlockCache
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
comment|// Some memcache versions won't take more than 1024 * 1024. So set the limit below
comment|// that just in case this client is used with those versions.
specifier|public
specifier|static
specifier|final
name|int
name|MAX_SIZE
init|=
literal|1020
operator|*
literal|1024
decl_stmt|;
comment|// Config key for what memcached servers to use.
comment|// They should be specified in a comma sperated list with ports.
comment|// like:
comment|//
comment|// host1:11211,host3:8080,host4:11211
specifier|public
specifier|static
specifier|final
name|String
name|MEMCACHED_CONFIG_KEY
init|=
literal|"hbase.cache.memcached.servers"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|MEMCACHED_TIMEOUT_KEY
init|=
literal|"hbase.cache.memcached.timeout"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|MEMCACHED_OPTIMEOUT_KEY
init|=
literal|"hbase.cache.memcached.optimeout"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|MEMCACHED_OPTIMIZE_KEY
init|=
literal|"hbase.cache.memcached.spy.optimze"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|long
name|MEMCACHED_DEFAULT_TIMEOUT
init|=
literal|500
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|boolean
name|MEMCACHED_OPTIMIZE_DEFAULT
init|=
literal|false
decl_stmt|;
specifier|private
specifier|final
name|MemcachedClient
name|client
decl_stmt|;
specifier|private
specifier|final
name|HFileBlockTranscoder
name|tc
init|=
operator|new
name|HFileBlockTranscoder
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|CacheStats
name|cacheStats
init|=
operator|new
name|CacheStats
argument_list|(
literal|"MemcachedBlockCache"
argument_list|)
decl_stmt|;
specifier|public
name|MemcachedBlockCache
parameter_list|(
name|Configuration
name|c
parameter_list|)
throws|throws
name|IOException
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Creating MemcachedBlockCache"
argument_list|)
expr_stmt|;
name|long
name|opTimeout
init|=
name|c
operator|.
name|getLong
argument_list|(
name|MEMCACHED_OPTIMEOUT_KEY
argument_list|,
name|MEMCACHED_DEFAULT_TIMEOUT
argument_list|)
decl_stmt|;
name|long
name|queueTimeout
init|=
name|c
operator|.
name|getLong
argument_list|(
name|MEMCACHED_TIMEOUT_KEY
argument_list|,
name|opTimeout
operator|+
name|MEMCACHED_DEFAULT_TIMEOUT
argument_list|)
decl_stmt|;
name|boolean
name|optimize
init|=
name|c
operator|.
name|getBoolean
argument_list|(
name|MEMCACHED_OPTIMIZE_KEY
argument_list|,
name|MEMCACHED_OPTIMIZE_DEFAULT
argument_list|)
decl_stmt|;
name|ConnectionFactoryBuilder
name|builder
init|=
operator|new
name|ConnectionFactoryBuilder
argument_list|()
operator|.
name|setOpTimeout
argument_list|(
name|opTimeout
argument_list|)
operator|.
name|setOpQueueMaxBlockTime
argument_list|(
name|queueTimeout
argument_list|)
comment|// Cap the max time before anything times out
operator|.
name|setFailureMode
argument_list|(
name|FailureMode
operator|.
name|Redistribute
argument_list|)
operator|.
name|setShouldOptimize
argument_list|(
name|optimize
argument_list|)
operator|.
name|setDaemon
argument_list|(
literal|true
argument_list|)
comment|// Don't keep threads around past the end of days.
operator|.
name|setUseNagleAlgorithm
argument_list|(
literal|false
argument_list|)
comment|// Ain't nobody got time for that
operator|.
name|setReadBufferSize
argument_list|(
name|HConstants
operator|.
name|DEFAULT_BLOCKSIZE
operator|*
literal|4
operator|*
literal|1024
argument_list|)
decl_stmt|;
comment|// Much larger just in case
comment|// Assume only the localhost is serving memecached.
comment|// A la mcrouter or co-locating memcached with split regionservers.
comment|//
comment|// If this config is a pool of memecached servers they will all be used according to the
comment|// default hashing scheme defined by the memcache client. Spy Memecache client in this
comment|// case.
name|String
name|serverListString
init|=
name|c
operator|.
name|get
argument_list|(
name|MEMCACHED_CONFIG_KEY
argument_list|,
literal|"localhost:11211"
argument_list|)
decl_stmt|;
name|String
index|[]
name|servers
init|=
name|serverListString
operator|.
name|split
argument_list|(
literal|","
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|InetSocketAddress
argument_list|>
name|serverAddresses
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|servers
operator|.
name|length
argument_list|)
decl_stmt|;
for|for
control|(
name|String
name|s
range|:
name|servers
control|)
block|{
name|serverAddresses
operator|.
name|add
argument_list|(
name|Addressing
operator|.
name|createInetSocketAddressFromHostAndPortStr
argument_list|(
name|s
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|client
operator|=
operator|new
name|MemcachedClient
argument_list|(
name|builder
operator|.
name|build
argument_list|()
argument_list|,
name|serverAddresses
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|cacheBlock
parameter_list|(
name|BlockCacheKey
name|cacheKey
parameter_list|,
name|Cacheable
name|buf
parameter_list|,
name|boolean
name|inMemory
parameter_list|)
block|{
name|cacheBlock
argument_list|(
name|cacheKey
argument_list|,
name|buf
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|cacheBlock
parameter_list|(
name|BlockCacheKey
name|cacheKey
parameter_list|,
name|Cacheable
name|buf
parameter_list|)
block|{
if|if
condition|(
name|buf
operator|instanceof
name|HFileBlock
condition|)
block|{
name|client
operator|.
name|add
argument_list|(
name|cacheKey
operator|.
name|toString
argument_list|()
argument_list|,
name|MAX_SIZE
argument_list|,
operator|(
name|HFileBlock
operator|)
name|buf
argument_list|,
name|tc
argument_list|)
expr_stmt|;
block|}
else|else
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
literal|"MemcachedBlockCache can not cache Cacheable's of type "
operator|+
name|buf
operator|.
name|getClass
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|Override
specifier|public
name|Cacheable
name|getBlock
parameter_list|(
name|BlockCacheKey
name|cacheKey
parameter_list|,
name|boolean
name|caching
parameter_list|,
name|boolean
name|repeat
parameter_list|,
name|boolean
name|updateCacheMetrics
parameter_list|)
block|{
comment|// Assume that nothing is the block cache
name|HFileBlock
name|result
init|=
literal|null
decl_stmt|;
try|try
init|(
name|TraceScope
name|traceScope
init|=
name|TraceUtil
operator|.
name|createTrace
argument_list|(
literal|"MemcachedBlockCache.getBlock"
argument_list|)
init|)
block|{
name|result
operator|=
name|client
operator|.
name|get
argument_list|(
name|cacheKey
operator|.
name|toString
argument_list|()
argument_list|,
name|tc
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
comment|// Catch a pretty broad set of exceptions to limit any changes in the memecache client
comment|// and how it handles failures from leaking into the read path.
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
literal|"Exception pulling from memcached [ "
operator|+
name|cacheKey
operator|.
name|toString
argument_list|()
operator|+
literal|" ]. Treating as a miss."
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
name|result
operator|=
literal|null
expr_stmt|;
block|}
finally|finally
block|{
comment|// Update stats if this request doesn't have it turned off 100% of the time
if|if
condition|(
name|updateCacheMetrics
condition|)
block|{
if|if
condition|(
name|result
operator|==
literal|null
condition|)
block|{
name|cacheStats
operator|.
name|miss
argument_list|(
name|caching
argument_list|,
name|cacheKey
operator|.
name|isPrimary
argument_list|()
argument_list|,
name|cacheKey
operator|.
name|getBlockType
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|cacheStats
operator|.
name|hit
argument_list|(
name|caching
argument_list|,
name|cacheKey
operator|.
name|isPrimary
argument_list|()
argument_list|,
name|cacheKey
operator|.
name|getBlockType
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
return|return
name|result
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|evictBlock
parameter_list|(
name|BlockCacheKey
name|cacheKey
parameter_list|)
block|{
try|try
block|{
name|cacheStats
operator|.
name|evict
argument_list|()
expr_stmt|;
return|return
name|client
operator|.
name|delete
argument_list|(
name|cacheKey
operator|.
name|toString
argument_list|()
argument_list|)
operator|.
name|get
argument_list|()
return|;
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
literal|"Error deleting "
operator|+
name|cacheKey
operator|.
name|toString
argument_list|()
argument_list|,
name|e
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
block|}
catch|catch
parameter_list|(
name|ExecutionException
name|e
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
literal|"Error deleting "
operator|+
name|cacheKey
operator|.
name|toString
argument_list|()
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
return|return
literal|false
return|;
block|}
comment|/**    * This method does nothing so that memcached can handle all evictions.    */
annotation|@
name|Override
specifier|public
name|int
name|evictBlocksByHfileName
parameter_list|(
name|String
name|hfileName
parameter_list|)
block|{
return|return
literal|0
return|;
block|}
annotation|@
name|Override
specifier|public
name|CacheStats
name|getStats
parameter_list|()
block|{
return|return
name|cacheStats
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|shutdown
parameter_list|()
block|{
name|client
operator|.
name|shutdown
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|size
parameter_list|()
block|{
return|return
literal|0
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getMaxSize
parameter_list|()
block|{
return|return
literal|0
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getFreeSize
parameter_list|()
block|{
return|return
literal|0
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getCurrentSize
parameter_list|()
block|{
return|return
literal|0
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getCurrentDataSize
parameter_list|()
block|{
return|return
literal|0
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getBlockCount
parameter_list|()
block|{
return|return
literal|0
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getDataBlockCount
parameter_list|()
block|{
return|return
literal|0
return|;
block|}
annotation|@
name|Override
specifier|public
name|Iterator
argument_list|<
name|CachedBlock
argument_list|>
name|iterator
parameter_list|()
block|{
return|return
operator|new
name|Iterator
argument_list|<
name|CachedBlock
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|boolean
name|hasNext
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
annotation|@
name|Override
specifier|public
name|CachedBlock
name|next
parameter_list|()
block|{
throw|throw
operator|new
name|NoSuchElementException
argument_list|(
literal|"MemcachedBlockCache can't iterate over blocks."
argument_list|)
throw|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|remove
parameter_list|()
block|{        }
block|}
return|;
block|}
annotation|@
name|Override
specifier|public
name|BlockCache
index|[]
name|getBlockCaches
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
comment|/**    * Class to encode and decode an HFileBlock to and from memecached's resulting byte arrays.    */
specifier|private
specifier|static
class|class
name|HFileBlockTranscoder
implements|implements
name|Transcoder
argument_list|<
name|HFileBlock
argument_list|>
block|{
annotation|@
name|Override
specifier|public
name|boolean
name|asyncDecode
parameter_list|(
name|CachedData
name|d
parameter_list|)
block|{
return|return
literal|false
return|;
block|}
annotation|@
name|Override
specifier|public
name|CachedData
name|encode
parameter_list|(
name|HFileBlock
name|block
parameter_list|)
block|{
name|ByteBuffer
name|bb
init|=
name|ByteBuffer
operator|.
name|allocate
argument_list|(
name|block
operator|.
name|getSerializedLength
argument_list|()
argument_list|)
decl_stmt|;
name|block
operator|.
name|serialize
argument_list|(
name|bb
argument_list|)
expr_stmt|;
return|return
operator|new
name|CachedData
argument_list|(
literal|0
argument_list|,
name|bb
operator|.
name|array
argument_list|()
argument_list|,
name|CachedData
operator|.
name|MAX_SIZE
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|HFileBlock
name|decode
parameter_list|(
name|CachedData
name|d
parameter_list|)
block|{
try|try
block|{
name|ByteBuff
name|buf
init|=
operator|new
name|SingleByteBuff
argument_list|(
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|d
operator|.
name|getData
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
return|return
operator|(
name|HFileBlock
operator|)
name|HFileBlock
operator|.
name|BLOCK_DESERIALIZER
operator|.
name|deserialize
argument_list|(
name|buf
argument_list|,
literal|true
argument_list|,
name|MemoryType
operator|.
name|EXCLUSIVE
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Error deserializing data from memcached"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getMaxSize
parameter_list|()
block|{
return|return
name|MAX_SIZE
return|;
block|}
block|}
block|}
end_class

end_unit

