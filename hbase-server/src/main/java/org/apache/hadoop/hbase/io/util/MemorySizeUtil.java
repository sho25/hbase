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
name|util
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
name|lang
operator|.
name|management
operator|.
name|MemoryType
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|management
operator|.
name|MemoryUsage
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
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|regionserver
operator|.
name|MemStoreLAB
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
name|Pair
import|;
end_import

begin_comment
comment|/**  * Util class to calculate memory size for memstore, block cache(L1, L2) of RS.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|MemorySizeUtil
block|{
specifier|public
specifier|static
specifier|final
name|String
name|MEMSTORE_SIZE_KEY
init|=
literal|"hbase.regionserver.global.memstore.size"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|MEMSTORE_SIZE_OLD_KEY
init|=
literal|"hbase.regionserver.global.memstore.upperLimit"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|MEMSTORE_SIZE_LOWER_LIMIT_KEY
init|=
literal|"hbase.regionserver.global.memstore.size.lower.limit"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|MEMSTORE_SIZE_LOWER_LIMIT_OLD_KEY
init|=
literal|"hbase.regionserver.global.memstore.lowerLimit"
decl_stmt|;
comment|// Max global off heap memory that can be used for all memstores
comment|// This should be an absolute value in MBs and not percent.
specifier|public
specifier|static
specifier|final
name|String
name|OFFHEAP_MEMSTORE_SIZE_KEY
init|=
literal|"hbase.regionserver.offheap.global.memstore.size"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|float
name|DEFAULT_MEMSTORE_SIZE
init|=
literal|0.4f
decl_stmt|;
comment|// Default lower water mark limit is 95% size of memstore size.
specifier|public
specifier|static
specifier|final
name|float
name|DEFAULT_MEMSTORE_SIZE_LOWER_LIMIT
init|=
literal|0.95f
decl_stmt|;
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
name|MemorySizeUtil
operator|.
name|class
argument_list|)
decl_stmt|;
comment|// a constant to convert a fraction to a percentage
specifier|private
specifier|static
specifier|final
name|int
name|CONVERT_TO_PERCENTAGE
init|=
literal|100
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|JVM_HEAP_EXCEPTION
init|=
literal|"Got an exception while attempting to read "
operator|+
literal|"information about the JVM heap. Please submit this log information in a bug report and "
operator|+
literal|"include your JVM settings, specifically the GC in use and any -XX options. Consider "
operator|+
literal|"restarting the service."
decl_stmt|;
comment|/**    * Return JVM memory statistics while properly handling runtime exceptions from the JVM.    * @return a memory usage object, null if there was a runtime exception. (n.b. you    *         could also get -1 values back from the JVM)    * @see MemoryUsage    */
specifier|public
specifier|static
name|MemoryUsage
name|safeGetHeapMemoryUsage
parameter_list|()
block|{
name|MemoryUsage
name|usage
init|=
literal|null
decl_stmt|;
try|try
block|{
name|usage
operator|=
name|ManagementFactory
operator|.
name|getMemoryMXBean
argument_list|()
operator|.
name|getHeapMemoryUsage
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|RuntimeException
name|exception
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
name|JVM_HEAP_EXCEPTION
argument_list|,
name|exception
argument_list|)
expr_stmt|;
block|}
return|return
name|usage
return|;
block|}
comment|/**    * Checks whether we have enough heap memory left out after portion for Memstore and Block cache.    * We need atleast 20% of heap left out for other RS functions.    * @param conf    */
specifier|public
specifier|static
name|void
name|checkForClusterFreeHeapMemoryLimit
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
name|MEMSTORE_SIZE_OLD_KEY
argument_list|)
operator|!=
literal|null
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
name|MEMSTORE_SIZE_OLD_KEY
operator|+
literal|" is deprecated by "
operator|+
name|MEMSTORE_SIZE_KEY
argument_list|)
expr_stmt|;
block|}
name|float
name|globalMemstoreSize
init|=
name|getGlobalMemStoreHeapPercent
argument_list|(
name|conf
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|int
name|gml
init|=
call|(
name|int
call|)
argument_list|(
name|globalMemstoreSize
operator|*
name|CONVERT_TO_PERCENTAGE
argument_list|)
decl_stmt|;
name|float
name|blockCacheUpperLimit
init|=
name|getBlockCacheHeapPercent
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|int
name|bcul
init|=
call|(
name|int
call|)
argument_list|(
name|blockCacheUpperLimit
operator|*
name|CONVERT_TO_PERCENTAGE
argument_list|)
decl_stmt|;
if|if
condition|(
name|CONVERT_TO_PERCENTAGE
operator|-
operator|(
name|gml
operator|+
name|bcul
operator|)
operator|<
call|(
name|int
call|)
argument_list|(
name|CONVERT_TO_PERCENTAGE
operator|*
name|HConstants
operator|.
name|HBASE_CLUSTER_MINIMUM_MEMORY_THRESHOLD
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Current heap configuration for MemStore and BlockCache exceeds "
operator|+
literal|"the threshold required for successful cluster operation. "
operator|+
literal|"The combined value cannot exceed 0.8. Please check "
operator|+
literal|"the settings for hbase.regionserver.global.memstore.size and "
operator|+
literal|"hfile.block.cache.size in your configuration. "
operator|+
literal|"hbase.regionserver.global.memstore.size is "
operator|+
name|globalMemstoreSize
operator|+
literal|" hfile.block.cache.size is "
operator|+
name|blockCacheUpperLimit
argument_list|)
throw|;
block|}
block|}
comment|/**    * Retrieve global memstore configured size as percentage of total heap.    * @param c    * @param logInvalid    */
specifier|public
specifier|static
name|float
name|getGlobalMemStoreHeapPercent
parameter_list|(
specifier|final
name|Configuration
name|c
parameter_list|,
specifier|final
name|boolean
name|logInvalid
parameter_list|)
block|{
name|float
name|limit
init|=
name|c
operator|.
name|getFloat
argument_list|(
name|MEMSTORE_SIZE_KEY
argument_list|,
name|c
operator|.
name|getFloat
argument_list|(
name|MEMSTORE_SIZE_OLD_KEY
argument_list|,
name|DEFAULT_MEMSTORE_SIZE
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|limit
operator|>
literal|0.8f
operator|||
name|limit
operator|<=
literal|0.0f
condition|)
block|{
if|if
condition|(
name|logInvalid
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Setting global memstore limit to default of "
operator|+
name|DEFAULT_MEMSTORE_SIZE
operator|+
literal|" because supplied value outside allowed range of (0 -> 0.8]"
argument_list|)
expr_stmt|;
block|}
name|limit
operator|=
name|DEFAULT_MEMSTORE_SIZE
expr_stmt|;
block|}
return|return
name|limit
return|;
block|}
comment|/**    * Retrieve configured size for global memstore lower water mark as fraction of global memstore    * size.    */
specifier|public
specifier|static
name|float
name|getGlobalMemStoreHeapLowerMark
parameter_list|(
specifier|final
name|Configuration
name|conf
parameter_list|,
name|boolean
name|honorOldConfig
parameter_list|)
block|{
name|String
name|lowMarkPercentStr
init|=
name|conf
operator|.
name|get
argument_list|(
name|MEMSTORE_SIZE_LOWER_LIMIT_KEY
argument_list|)
decl_stmt|;
if|if
condition|(
name|lowMarkPercentStr
operator|!=
literal|null
condition|)
block|{
name|float
name|lowMarkPercent
init|=
name|Float
operator|.
name|parseFloat
argument_list|(
name|lowMarkPercentStr
argument_list|)
decl_stmt|;
if|if
condition|(
name|lowMarkPercent
operator|>
literal|1.0f
condition|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Bad configuration value for "
operator|+
name|MEMSTORE_SIZE_LOWER_LIMIT_KEY
operator|+
literal|": "
operator|+
name|lowMarkPercent
operator|+
literal|". Using 1.0f instead."
argument_list|)
expr_stmt|;
name|lowMarkPercent
operator|=
literal|1.0f
expr_stmt|;
block|}
return|return
name|lowMarkPercent
return|;
block|}
if|if
condition|(
operator|!
name|honorOldConfig
condition|)
return|return
name|DEFAULT_MEMSTORE_SIZE_LOWER_LIMIT
return|;
name|String
name|lowerWaterMarkOldValStr
init|=
name|conf
operator|.
name|get
argument_list|(
name|MEMSTORE_SIZE_LOWER_LIMIT_OLD_KEY
argument_list|)
decl_stmt|;
if|if
condition|(
name|lowerWaterMarkOldValStr
operator|!=
literal|null
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
name|MEMSTORE_SIZE_LOWER_LIMIT_OLD_KEY
operator|+
literal|" is deprecated. Instead use "
operator|+
name|MEMSTORE_SIZE_LOWER_LIMIT_KEY
argument_list|)
expr_stmt|;
name|float
name|lowerWaterMarkOldVal
init|=
name|Float
operator|.
name|parseFloat
argument_list|(
name|lowerWaterMarkOldValStr
argument_list|)
decl_stmt|;
name|float
name|upperMarkPercent
init|=
name|getGlobalMemStoreHeapPercent
argument_list|(
name|conf
argument_list|,
literal|false
argument_list|)
decl_stmt|;
if|if
condition|(
name|lowerWaterMarkOldVal
operator|>
name|upperMarkPercent
condition|)
block|{
name|lowerWaterMarkOldVal
operator|=
name|upperMarkPercent
expr_stmt|;
name|LOG
operator|.
name|error
argument_list|(
literal|"Value of "
operator|+
name|MEMSTORE_SIZE_LOWER_LIMIT_OLD_KEY
operator|+
literal|" ("
operator|+
name|lowerWaterMarkOldVal
operator|+
literal|") is greater than global memstore limit ("
operator|+
name|upperMarkPercent
operator|+
literal|") set by "
operator|+
name|MEMSTORE_SIZE_KEY
operator|+
literal|"/"
operator|+
name|MEMSTORE_SIZE_OLD_KEY
operator|+
literal|". Setting memstore lower limit "
operator|+
literal|"to "
operator|+
name|upperMarkPercent
argument_list|)
expr_stmt|;
block|}
return|return
name|lowerWaterMarkOldVal
operator|/
name|upperMarkPercent
return|;
block|}
return|return
name|DEFAULT_MEMSTORE_SIZE_LOWER_LIMIT
return|;
block|}
comment|/**    * @return Pair of global memstore size and memory type(ie. on heap or off heap).    */
specifier|public
specifier|static
name|Pair
argument_list|<
name|Long
argument_list|,
name|MemoryType
argument_list|>
name|getGlobalMemStoreSize
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
name|long
name|offheapMSGlobal
init|=
name|conf
operator|.
name|getLong
argument_list|(
name|OFFHEAP_MEMSTORE_SIZE_KEY
argument_list|,
literal|0
argument_list|)
decl_stmt|;
comment|// Size in MBs
if|if
condition|(
name|offheapMSGlobal
operator|>
literal|0
condition|)
block|{
comment|// Off heap memstore size has not relevance when MSLAB is turned OFF. We will go with making
comment|// this entire size split into Chunks and pooling them in MemstoreLABPoool. We dont want to
comment|// create so many on demand off heap chunks. In fact when this off heap size is configured, we
comment|// will go with 100% of this size as the pool size
if|if
condition|(
name|MemStoreLAB
operator|.
name|isEnabled
argument_list|(
name|conf
argument_list|)
condition|)
block|{
comment|// We are in offheap Memstore use
name|long
name|globalMemStoreLimit
init|=
call|(
name|long
call|)
argument_list|(
name|offheapMSGlobal
operator|*
literal|1024
operator|*
literal|1024
argument_list|)
decl_stmt|;
comment|// Size in bytes
return|return
operator|new
name|Pair
argument_list|<>
argument_list|(
name|globalMemStoreLimit
argument_list|,
name|MemoryType
operator|.
name|NON_HEAP
argument_list|)
return|;
block|}
else|else
block|{
comment|// Off heap max memstore size is configured with turning off MSLAB. It makes no sense. Do a
comment|// warn log and go with on heap memstore percentage. By default it will be 40% of Xmx
name|LOG
operator|.
name|warn
argument_list|(
literal|"There is no relevance of configuring '"
operator|+
name|OFFHEAP_MEMSTORE_SIZE_KEY
operator|+
literal|"' when '"
operator|+
name|MemStoreLAB
operator|.
name|USEMSLAB_KEY
operator|+
literal|"' is turned off."
operator|+
literal|" Going with on heap global memstore size ('"
operator|+
name|MEMSTORE_SIZE_KEY
operator|+
literal|"')"
argument_list|)
expr_stmt|;
block|}
block|}
return|return
operator|new
name|Pair
argument_list|<>
argument_list|(
name|getOnheapGlobalMemStoreSize
argument_list|(
name|conf
argument_list|)
argument_list|,
name|MemoryType
operator|.
name|HEAP
argument_list|)
return|;
block|}
comment|/**    * Returns the onheap global memstore limit based on the config    * 'hbase.regionserver.global.memstore.size'.    * @param conf    * @return the onheap global memstore limt    */
specifier|public
specifier|static
name|long
name|getOnheapGlobalMemStoreSize
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
name|long
name|max
init|=
operator|-
literal|1L
decl_stmt|;
specifier|final
name|MemoryUsage
name|usage
init|=
name|safeGetHeapMemoryUsage
argument_list|()
decl_stmt|;
if|if
condition|(
name|usage
operator|!=
literal|null
condition|)
block|{
name|max
operator|=
name|usage
operator|.
name|getMax
argument_list|()
expr_stmt|;
block|}
name|float
name|globalMemStorePercent
init|=
name|getGlobalMemStoreHeapPercent
argument_list|(
name|conf
argument_list|,
literal|true
argument_list|)
decl_stmt|;
return|return
operator|(
call|(
name|long
call|)
argument_list|(
name|max
operator|*
name|globalMemStorePercent
argument_list|)
operator|)
return|;
block|}
comment|/**    * Retrieve configured size for on heap block cache as percentage of total heap.    * @param conf    */
specifier|public
specifier|static
name|float
name|getBlockCacheHeapPercent
parameter_list|(
specifier|final
name|Configuration
name|conf
parameter_list|)
block|{
comment|// L1 block cache is always on heap
name|float
name|l1CachePercent
init|=
name|conf
operator|.
name|getFloat
argument_list|(
name|HConstants
operator|.
name|HFILE_BLOCK_CACHE_SIZE_KEY
argument_list|,
name|HConstants
operator|.
name|HFILE_BLOCK_CACHE_SIZE_DEFAULT
argument_list|)
decl_stmt|;
return|return
name|l1CachePercent
return|;
block|}
comment|/**    * @param conf used to read cache configs    * @return the number of bytes to use for LRU, negative if disabled.    * @throws IllegalArgumentException if HFILE_BLOCK_CACHE_SIZE_KEY is> 1.0    */
specifier|public
specifier|static
name|long
name|getOnHeapCacheSize
parameter_list|(
specifier|final
name|Configuration
name|conf
parameter_list|)
block|{
name|float
name|cachePercentage
init|=
name|conf
operator|.
name|getFloat
argument_list|(
name|HConstants
operator|.
name|HFILE_BLOCK_CACHE_SIZE_KEY
argument_list|,
name|HConstants
operator|.
name|HFILE_BLOCK_CACHE_SIZE_DEFAULT
argument_list|)
decl_stmt|;
if|if
condition|(
name|cachePercentage
operator|<=
literal|0.0001f
condition|)
block|{
return|return
operator|-
literal|1
return|;
block|}
if|if
condition|(
name|cachePercentage
operator|>
literal|1.0
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
name|HConstants
operator|.
name|HFILE_BLOCK_CACHE_SIZE_KEY
operator|+
literal|" must be between 0.0 and 1.0, and not> 1.0"
argument_list|)
throw|;
block|}
name|long
name|max
init|=
operator|-
literal|1L
decl_stmt|;
specifier|final
name|MemoryUsage
name|usage
init|=
name|safeGetHeapMemoryUsage
argument_list|()
decl_stmt|;
if|if
condition|(
name|usage
operator|!=
literal|null
condition|)
block|{
name|max
operator|=
name|usage
operator|.
name|getMax
argument_list|()
expr_stmt|;
block|}
comment|// Calculate the amount of heap to give the heap.
return|return
call|(
name|long
call|)
argument_list|(
name|max
operator|*
name|cachePercentage
argument_list|)
return|;
block|}
comment|/**    * @param conf used to read config for bucket cache size. (< 1 is treated as % and> is treated as MiB)    * @return the number of bytes to use for bucket cache, negative if disabled.    */
specifier|public
specifier|static
name|long
name|getBucketCacheSize
parameter_list|(
specifier|final
name|Configuration
name|conf
parameter_list|)
block|{
comment|// Size configured in MBs
name|float
name|bucketCacheSize
init|=
name|conf
operator|.
name|getFloat
argument_list|(
name|HConstants
operator|.
name|BUCKET_CACHE_SIZE_KEY
argument_list|,
literal|0F
argument_list|)
decl_stmt|;
if|if
condition|(
name|bucketCacheSize
operator|<
literal|1
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Bucket Cache should be minimum 1 MB in size."
operator|+
literal|"Configure 'hbase.bucketcache.size' with> 1 value"
argument_list|)
throw|;
block|}
return|return
call|(
name|long
call|)
argument_list|(
name|bucketCacheSize
operator|*
literal|1024
operator|*
literal|1024
argument_list|)
return|;
block|}
block|}
end_class

end_unit

