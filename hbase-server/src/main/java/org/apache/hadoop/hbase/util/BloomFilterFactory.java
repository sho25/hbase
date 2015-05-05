begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|util
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataInput
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
name|CellComparator
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
name|CacheConfig
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
name|HFile
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

begin_comment
comment|/**  * Handles Bloom filter initialization based on configuration and serialized  * metadata in the reader and writer of {@link org.apache.hadoop.hbase.regionserver.StoreFile}.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
specifier|final
class|class
name|BloomFilterFactory
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
name|BloomFilterFactory
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
comment|/** This class should not be instantiated. */
specifier|private
name|BloomFilterFactory
parameter_list|()
block|{}
comment|/**    * Specifies the target error rate to use when selecting the number of keys    * per Bloom filter.    */
specifier|public
specifier|static
specifier|final
name|String
name|IO_STOREFILE_BLOOM_ERROR_RATE
init|=
literal|"io.storefile.bloom.error.rate"
decl_stmt|;
comment|/**    * Maximum folding factor allowed. The Bloom filter will be shrunk by    * the factor of up to 2 ** this times if we oversize it initially.    */
specifier|public
specifier|static
specifier|final
name|String
name|IO_STOREFILE_BLOOM_MAX_FOLD
init|=
literal|"io.storefile.bloom.max.fold"
decl_stmt|;
comment|/**    * For default (single-block) Bloom filters this specifies the maximum number    * of keys.    */
specifier|public
specifier|static
specifier|final
name|String
name|IO_STOREFILE_BLOOM_MAX_KEYS
init|=
literal|"io.storefile.bloom.max.keys"
decl_stmt|;
comment|/** Master switch to enable Bloom filters */
specifier|public
specifier|static
specifier|final
name|String
name|IO_STOREFILE_BLOOM_ENABLED
init|=
literal|"io.storefile.bloom.enabled"
decl_stmt|;
comment|/** Master switch to enable Delete Family Bloom filters */
specifier|public
specifier|static
specifier|final
name|String
name|IO_STOREFILE_DELETEFAMILY_BLOOM_ENABLED
init|=
literal|"io.storefile.delete.family.bloom.enabled"
decl_stmt|;
comment|/**    * Target Bloom block size. Bloom filter blocks of approximately this size    * are interleaved with data blocks.    */
specifier|public
specifier|static
specifier|final
name|String
name|IO_STOREFILE_BLOOM_BLOCK_SIZE
init|=
literal|"io.storefile.bloom.block.size"
decl_stmt|;
comment|/** Maximum number of times a Bloom filter can be "folded" if oversized */
specifier|private
specifier|static
specifier|final
name|int
name|MAX_ALLOWED_FOLD_FACTOR
init|=
literal|7
decl_stmt|;
comment|/**    * Instantiates the correct Bloom filter class based on the version provided    * in the meta block data.    *    * @param meta the byte array holding the Bloom filter's metadata, including    *          version information    * @param reader the {@link HFile} reader to use to lazily load Bloom filter    *          blocks    * @return an instance of the correct type of Bloom filter    * @throws IllegalArgumentException    */
specifier|public
specifier|static
name|BloomFilter
name|createFromMeta
parameter_list|(
name|DataInput
name|meta
parameter_list|,
name|HFile
operator|.
name|Reader
name|reader
parameter_list|)
throws|throws
name|IllegalArgumentException
throws|,
name|IOException
block|{
name|int
name|version
init|=
name|meta
operator|.
name|readInt
argument_list|()
decl_stmt|;
switch|switch
condition|(
name|version
condition|)
block|{
case|case
name|ByteBloomFilter
operator|.
name|VERSION
case|:
comment|// This is only possible in a version 1 HFile. We are ignoring the
comment|// passed comparator because raw byte comparators are always used
comment|// in version 1 Bloom filters.
comment|// TODO:Remove this code - use only CompoundBloomFilter
return|return
operator|new
name|ByteBloomFilter
argument_list|(
name|meta
argument_list|)
return|;
case|case
name|CompoundBloomFilterBase
operator|.
name|VERSION
case|:
return|return
operator|new
name|CompoundBloomFilter
argument_list|(
name|meta
argument_list|,
name|reader
argument_list|)
return|;
default|default:
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Bad bloom filter format version "
operator|+
name|version
argument_list|)
throw|;
block|}
block|}
comment|/**    * @return true if general Bloom (Row or RowCol) filters are enabled in the    * given configuration    */
specifier|public
specifier|static
name|boolean
name|isGeneralBloomEnabled
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
return|return
name|conf
operator|.
name|getBoolean
argument_list|(
name|IO_STOREFILE_BLOOM_ENABLED
argument_list|,
literal|true
argument_list|)
return|;
block|}
comment|/**    * @return true if Delete Family Bloom filters are enabled in the given configuration    */
specifier|public
specifier|static
name|boolean
name|isDeleteFamilyBloomEnabled
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
return|return
name|conf
operator|.
name|getBoolean
argument_list|(
name|IO_STOREFILE_DELETEFAMILY_BLOOM_ENABLED
argument_list|,
literal|true
argument_list|)
return|;
block|}
comment|/**    * @return the Bloom filter error rate in the given configuration    */
specifier|public
specifier|static
name|float
name|getErrorRate
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
return|return
name|conf
operator|.
name|getFloat
argument_list|(
name|IO_STOREFILE_BLOOM_ERROR_RATE
argument_list|,
operator|(
name|float
operator|)
literal|0.01
argument_list|)
return|;
block|}
comment|/**    * @return the value for Bloom filter max fold in the given configuration    */
specifier|public
specifier|static
name|int
name|getMaxFold
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
return|return
name|conf
operator|.
name|getInt
argument_list|(
name|IO_STOREFILE_BLOOM_MAX_FOLD
argument_list|,
name|MAX_ALLOWED_FOLD_FACTOR
argument_list|)
return|;
block|}
comment|/** @return the compound Bloom filter block size from the configuration */
specifier|public
specifier|static
name|int
name|getBloomBlockSize
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
return|return
name|conf
operator|.
name|getInt
argument_list|(
name|IO_STOREFILE_BLOOM_BLOCK_SIZE
argument_list|,
literal|128
operator|*
literal|1024
argument_list|)
return|;
block|}
comment|/**   * @return max key for the Bloom filter from the configuration   */
specifier|public
specifier|static
name|int
name|getMaxKeys
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
return|return
name|conf
operator|.
name|getInt
argument_list|(
name|IO_STOREFILE_BLOOM_MAX_KEYS
argument_list|,
literal|128
operator|*
literal|1000
operator|*
literal|1000
argument_list|)
return|;
block|}
comment|/**    * Creates a new general (Row or RowCol) Bloom filter at the time of    * {@link org.apache.hadoop.hbase.regionserver.StoreFile} writing.    *    * @param conf    * @param cacheConf    * @param bloomType    * @param maxKeys an estimate of the number of keys we expect to insert.    *        Irrelevant if compound Bloom filters are enabled.    * @param writer the HFile writer    * @return the new Bloom filter, or null in case Bloom filters are disabled    *         or when failed to create one.    */
specifier|public
specifier|static
name|BloomFilterWriter
name|createGeneralBloomAtWrite
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|CacheConfig
name|cacheConf
parameter_list|,
name|BloomType
name|bloomType
parameter_list|,
name|int
name|maxKeys
parameter_list|,
name|HFile
operator|.
name|Writer
name|writer
parameter_list|)
block|{
if|if
condition|(
operator|!
name|isGeneralBloomEnabled
argument_list|(
name|conf
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|trace
argument_list|(
literal|"Bloom filters are disabled by configuration for "
operator|+
name|writer
operator|.
name|getPath
argument_list|()
operator|+
operator|(
name|conf
operator|==
literal|null
condition|?
literal|" (configuration is null)"
else|:
literal|""
operator|)
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
elseif|else
if|if
condition|(
name|bloomType
operator|==
name|BloomType
operator|.
name|NONE
condition|)
block|{
name|LOG
operator|.
name|trace
argument_list|(
literal|"Bloom filter is turned off for the column family"
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
name|float
name|err
init|=
name|getErrorRate
argument_list|(
name|conf
argument_list|)
decl_stmt|;
comment|// In case of row/column Bloom filter lookups, each lookup is an OR if two
comment|// separate lookups. Therefore, if each lookup's false positive rate is p,
comment|// the resulting false positive rate is err = 1 - (1 - p)^2, and
comment|// p = 1 - sqrt(1 - err).
if|if
condition|(
name|bloomType
operator|==
name|BloomType
operator|.
name|ROWCOL
condition|)
block|{
name|err
operator|=
call|(
name|float
call|)
argument_list|(
literal|1
operator|-
name|Math
operator|.
name|sqrt
argument_list|(
literal|1
operator|-
name|err
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|int
name|maxFold
init|=
name|conf
operator|.
name|getInt
argument_list|(
name|IO_STOREFILE_BLOOM_MAX_FOLD
argument_list|,
name|MAX_ALLOWED_FOLD_FACTOR
argument_list|)
decl_stmt|;
comment|// Do we support compound bloom filters?
comment|// In case of compound Bloom filters we ignore the maxKeys hint.
name|CompoundBloomFilterWriter
name|bloomWriter
init|=
operator|new
name|CompoundBloomFilterWriter
argument_list|(
name|getBloomBlockSize
argument_list|(
name|conf
argument_list|)
argument_list|,
name|err
argument_list|,
name|Hash
operator|.
name|getHashType
argument_list|(
name|conf
argument_list|)
argument_list|,
name|maxFold
argument_list|,
name|cacheConf
operator|.
name|shouldCacheBloomsOnWrite
argument_list|()
argument_list|,
name|bloomType
operator|==
name|BloomType
operator|.
name|ROWCOL
condition|?
name|CellComparator
operator|.
name|COMPARATOR
else|:
literal|null
argument_list|)
decl_stmt|;
name|writer
operator|.
name|addInlineBlockWriter
argument_list|(
name|bloomWriter
argument_list|)
expr_stmt|;
return|return
name|bloomWriter
return|;
block|}
comment|/**    * Creates a new Delete Family Bloom filter at the time of    * {@link org.apache.hadoop.hbase.regionserver.StoreFile} writing.    * @param conf    * @param cacheConf    * @param maxKeys an estimate of the number of keys we expect to insert.    *        Irrelevant if compound Bloom filters are enabled.    * @param writer the HFile writer    * @return the new Bloom filter, or null in case Bloom filters are disabled    *         or when failed to create one.    */
specifier|public
specifier|static
name|BloomFilterWriter
name|createDeleteBloomAtWrite
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|CacheConfig
name|cacheConf
parameter_list|,
name|int
name|maxKeys
parameter_list|,
name|HFile
operator|.
name|Writer
name|writer
parameter_list|)
block|{
if|if
condition|(
operator|!
name|isDeleteFamilyBloomEnabled
argument_list|(
name|conf
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Delete Bloom filters are disabled by configuration for "
operator|+
name|writer
operator|.
name|getPath
argument_list|()
operator|+
operator|(
name|conf
operator|==
literal|null
condition|?
literal|" (configuration is null)"
else|:
literal|""
operator|)
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
name|float
name|err
init|=
name|getErrorRate
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|int
name|maxFold
init|=
name|getMaxFold
argument_list|(
name|conf
argument_list|)
decl_stmt|;
comment|// In case of compound Bloom filters we ignore the maxKeys hint.
name|CompoundBloomFilterWriter
name|bloomWriter
init|=
operator|new
name|CompoundBloomFilterWriter
argument_list|(
name|getBloomBlockSize
argument_list|(
name|conf
argument_list|)
argument_list|,
name|err
argument_list|,
name|Hash
operator|.
name|getHashType
argument_list|(
name|conf
argument_list|)
argument_list|,
name|maxFold
argument_list|,
name|cacheConf
operator|.
name|shouldCacheBloomsOnWrite
argument_list|()
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|writer
operator|.
name|addInlineBlockWriter
argument_list|(
name|bloomWriter
argument_list|)
expr_stmt|;
return|return
name|bloomWriter
return|;
block|}
block|}
end_class

begin_empty_stmt
empty_stmt|;
end_empty_stmt

end_unit

