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
name|Map
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
name|KeepDeletedCells
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
name|MemoryCompactionPolicy
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
name|io
operator|.
name|compress
operator|.
name|Compression
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
name|encoding
operator|.
name|DataBlockEncoding
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
name|BloomType
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

begin_comment
comment|/**  * An ColumnFamilyDescriptor contains information about a column family such as the  * number of versions, compression settings, etc.  *  * It is used as input when creating a table or adding a column.  *  * To construct a new instance, use the {@link ColumnFamilyDescriptorBuilder} methods  */
end_comment

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|Public
specifier|public
interface|interface
name|ColumnFamilyDescriptor
block|{
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|static
specifier|final
name|Comparator
argument_list|<
name|ColumnFamilyDescriptor
argument_list|>
name|COMPARATOR
init|=
parameter_list|(
name|ColumnFamilyDescriptor
name|lhs
parameter_list|,
name|ColumnFamilyDescriptor
name|rhs
parameter_list|)
lambda|->
block|{
name|int
name|result
init|=
name|Bytes
operator|.
name|compareTo
argument_list|(
name|lhs
operator|.
name|getName
argument_list|()
argument_list|,
name|rhs
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|result
operator|!=
literal|0
condition|)
block|{
return|return
name|result
return|;
block|}
comment|// punt on comparison for ordering, just calculate difference.
name|result
operator|=
name|lhs
operator|.
name|getValues
argument_list|()
operator|.
name|hashCode
argument_list|()
operator|-
name|rhs
operator|.
name|getValues
argument_list|()
operator|.
name|hashCode
argument_list|()
expr_stmt|;
if|if
condition|(
name|result
operator|!=
literal|0
condition|)
block|{
return|return
name|result
return|;
block|}
return|return
name|lhs
operator|.
name|getConfiguration
argument_list|()
operator|.
name|hashCode
argument_list|()
operator|-
name|rhs
operator|.
name|getConfiguration
argument_list|()
operator|.
name|hashCode
argument_list|()
return|;
block|}
decl_stmt|;
comment|/**    * @return The storefile/hfile blocksize for this column family.    */
name|int
name|getBlocksize
parameter_list|()
function_decl|;
comment|/**    * @return bloom filter type used for new StoreFiles in ColumnFamily    */
name|BloomType
name|getBloomFilterType
parameter_list|()
function_decl|;
comment|/**    * @return Compression type setting.    */
name|Compression
operator|.
name|Algorithm
name|getCompactionCompressionType
parameter_list|()
function_decl|;
comment|/**    * @return Compression type setting.    */
name|Compression
operator|.
name|Algorithm
name|getCompressionType
parameter_list|()
function_decl|;
comment|/**    * @return an unmodifiable map.    */
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|getConfiguration
parameter_list|()
function_decl|;
comment|/**    * @param key the key whose associated value is to be returned    * @return accessing the configuration value by key.    */
name|String
name|getConfigurationValue
parameter_list|(
name|String
name|key
parameter_list|)
function_decl|;
comment|/**    * @return replication factor set for this CF    */
name|short
name|getDFSReplication
parameter_list|()
function_decl|;
comment|/**    * @return the data block encoding algorithm used in block cache and    *         optionally on disk    */
name|DataBlockEncoding
name|getDataBlockEncoding
parameter_list|()
function_decl|;
comment|/**    * @return Return the raw crypto key attribute for the family, or null if not set    */
name|byte
index|[]
name|getEncryptionKey
parameter_list|()
function_decl|;
comment|/**    * @return Return the encryption algorithm in use by this family    */
name|String
name|getEncryptionType
parameter_list|()
function_decl|;
comment|/**    * @return in-memory compaction policy if set for the cf. Returns null if no policy is set for    *          for this column family    */
name|MemoryCompactionPolicy
name|getInMemoryCompaction
parameter_list|()
function_decl|;
comment|/**    * @return return the KeepDeletedCells    */
name|KeepDeletedCells
name|getKeepDeletedCells
parameter_list|()
function_decl|;
comment|/**    * @return maximum number of versions    */
name|int
name|getMaxVersions
parameter_list|()
function_decl|;
comment|/**    * @return The minimum number of versions to keep.    */
name|int
name|getMinVersions
parameter_list|()
function_decl|;
comment|/**    * Get the mob compact partition policy for this family    * @return MobCompactPartitionPolicy    */
name|MobCompactPartitionPolicy
name|getMobCompactPartitionPolicy
parameter_list|()
function_decl|;
comment|/**    * Gets the mob threshold of the family.    * If the size of a cell value is larger than this threshold, it's regarded as a mob.    * The default threshold is 1024*100(100K)B.    * @return The mob threshold.    */
name|long
name|getMobThreshold
parameter_list|()
function_decl|;
comment|/**    * @return a copy of Name of this column family    */
name|byte
index|[]
name|getName
parameter_list|()
function_decl|;
comment|/**    * @return Name of this column family    */
name|String
name|getNameAsString
parameter_list|()
function_decl|;
comment|/**     * @return the scope tag     */
name|int
name|getScope
parameter_list|()
function_decl|;
comment|/**    * Not using {@code enum} here because HDFS is not using {@code enum} for storage policy, see    * org.apache.hadoop.hdfs.server.blockmanagement.BlockStoragePolicySuite for more details.    * @return Return the storage policy in use by this family    */
name|String
name|getStoragePolicy
parameter_list|()
function_decl|;
comment|/**    * @return Time-to-live of cell contents, in seconds.    */
name|int
name|getTimeToLive
parameter_list|()
function_decl|;
comment|/**    * @param key The key.    * @return A clone value. Null if no mapping for the key    */
name|Bytes
name|getValue
parameter_list|(
name|Bytes
name|key
parameter_list|)
function_decl|;
comment|/**    * @param key The key.    * @return A clone value. Null if no mapping for the key    */
name|byte
index|[]
name|getValue
parameter_list|(
name|byte
index|[]
name|key
parameter_list|)
function_decl|;
comment|/**    * It clone all bytes of all elements.    * @return All values    */
name|Map
argument_list|<
name|Bytes
argument_list|,
name|Bytes
argument_list|>
name|getValues
parameter_list|()
function_decl|;
comment|/**    * @return True if hfile DATA type blocks should be cached (You cannot disable caching of INDEX    * and BLOOM type blocks).    */
name|boolean
name|isBlockCacheEnabled
parameter_list|()
function_decl|;
comment|/**    * @return true if we should cache bloomfilter blocks on write    */
name|boolean
name|isCacheBloomsOnWrite
parameter_list|()
function_decl|;
comment|/**    * @return true if we should cache data blocks in the L1 cache (if block cache deploy has more    *         than one tier; e.g. we are using CombinedBlockCache).    */
name|boolean
name|isCacheDataInL1
parameter_list|()
function_decl|;
comment|/**    * @return true if we should cache data blocks on write    */
name|boolean
name|isCacheDataOnWrite
parameter_list|()
function_decl|;
comment|/**    * @return true if we should cache index blocks on write    */
name|boolean
name|isCacheIndexesOnWrite
parameter_list|()
function_decl|;
comment|/**    * @return Whether KV tags should be compressed along with DataBlockEncoding. When no    *         DataBlockEncoding is been used, this is having no effect.    */
name|boolean
name|isCompressTags
parameter_list|()
function_decl|;
comment|/**    * @return true if we should evict cached blocks from the blockcache on close    */
name|boolean
name|isEvictBlocksOnClose
parameter_list|()
function_decl|;
comment|/**    * @return True if we are to favor keeping all values for this column family in the    * HRegionServer cache.    */
name|boolean
name|isInMemory
parameter_list|()
function_decl|;
comment|/**    * Gets whether the mob is enabled for the family.    * @return True if the mob is enabled for the family.    */
name|boolean
name|isMobEnabled
parameter_list|()
function_decl|;
comment|/**    * @return true if we should prefetch blocks into the blockcache on open    */
name|boolean
name|isPrefetchBlocksOnOpen
parameter_list|()
function_decl|;
comment|/**    * @return Column family descriptor with only the customized attributes.    */
name|String
name|toStringCustomizedValues
parameter_list|()
function_decl|;
comment|/**    * By default, HBase only consider timestamp in versions. So a previous Delete with higher ts    * will mask a later Put with lower ts. Set this to true to enable new semantics of versions.    * We will also consider mvcc in versions. See HBASE-15968 for details.    */
name|boolean
name|isNewVersionBehavior
parameter_list|()
function_decl|;
block|}
end_interface

end_unit

