begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2008 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|FSDataInputStream
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
name|FSDataOutputStream
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
name|HRegionInfo
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
name|HStoreKey
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
name|Hash
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
name|SequenceFile
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
name|io
operator|.
name|Writable
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
name|io
operator|.
name|WritableComparable
import|;
end_import

begin_import
import|import
name|org
operator|.
name|onelab
operator|.
name|filter
operator|.
name|BloomFilter
import|;
end_import

begin_import
import|import
name|org
operator|.
name|onelab
operator|.
name|filter
operator|.
name|Key
import|;
end_import

begin_comment
comment|/**  * On write, all keys are added to a bloom filter.  On read, all keys are  * tested first against bloom filter. Keys are HStoreKey.  If passed bloom  * filter is null, just passes invocation to parent.  */
end_comment

begin_comment
comment|// TODO should be fixed generic warnings from MapFile methods
end_comment

begin_class
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
specifier|public
class|class
name|BloomFilterMapFile
extends|extends
name|HBaseMapFile
block|{
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|BloomFilterMapFile
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|protected
specifier|static
specifier|final
name|String
name|BLOOMFILTER_FILE_NAME
init|=
literal|"filter"
decl_stmt|;
specifier|public
specifier|static
class|class
name|Reader
extends|extends
name|HBaseReader
block|{
specifier|private
specifier|final
name|BloomFilter
name|bloomFilter
decl_stmt|;
comment|/**      * @param fs      * @param dirName      * @param conf      * @param filter      * @param blockCacheEnabled      * @param hri      * @throws IOException      */
specifier|public
name|Reader
parameter_list|(
name|FileSystem
name|fs
parameter_list|,
name|String
name|dirName
parameter_list|,
name|Configuration
name|conf
parameter_list|,
specifier|final
name|boolean
name|filter
parameter_list|,
specifier|final
name|boolean
name|blockCacheEnabled
parameter_list|,
name|HRegionInfo
name|hri
parameter_list|)
throws|throws
name|IOException
block|{
name|super
argument_list|(
name|fs
argument_list|,
name|dirName
argument_list|,
name|conf
argument_list|,
name|blockCacheEnabled
argument_list|,
name|hri
argument_list|)
expr_stmt|;
if|if
condition|(
name|filter
condition|)
block|{
name|this
operator|.
name|bloomFilter
operator|=
name|loadBloomFilter
argument_list|(
name|fs
argument_list|,
name|dirName
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|this
operator|.
name|bloomFilter
operator|=
literal|null
expr_stmt|;
block|}
block|}
specifier|private
name|BloomFilter
name|loadBloomFilter
parameter_list|(
name|FileSystem
name|fs
parameter_list|,
name|String
name|dirName
parameter_list|)
throws|throws
name|IOException
block|{
name|Path
name|filterFile
init|=
operator|new
name|Path
argument_list|(
name|dirName
argument_list|,
name|BLOOMFILTER_FILE_NAME
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|fs
operator|.
name|exists
argument_list|(
name|filterFile
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"FileNotFound: "
operator|+
name|filterFile
operator|+
literal|"; proceeding without"
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
name|BloomFilter
name|filter
init|=
operator|new
name|BloomFilter
argument_list|()
decl_stmt|;
name|FSDataInputStream
name|in
init|=
name|fs
operator|.
name|open
argument_list|(
name|filterFile
argument_list|)
decl_stmt|;
try|try
block|{
name|filter
operator|.
name|readFields
argument_list|(
name|in
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|in
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
return|return
name|filter
return|;
block|}
comment|/**      * @see org.apache.hadoop.hbase.io.MapFile.Reader#get(org.apache.hadoop.io.WritableComparable, org.apache.hadoop.io.Writable)      */
annotation|@
name|Override
specifier|public
name|Writable
name|get
parameter_list|(
name|WritableComparable
name|key
parameter_list|,
name|Writable
name|val
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|bloomFilter
operator|==
literal|null
condition|)
block|{
return|return
name|super
operator|.
name|get
argument_list|(
name|key
argument_list|,
name|val
argument_list|)
return|;
block|}
if|if
condition|(
name|bloomFilter
operator|.
name|membershipTest
argument_list|(
name|getBloomFilterKey
argument_list|(
name|key
argument_list|)
argument_list|)
condition|)
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
literal|"bloom filter reported that key exists"
argument_list|)
expr_stmt|;
block|}
return|return
name|super
operator|.
name|get
argument_list|(
name|key
argument_list|,
name|val
argument_list|)
return|;
block|}
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
literal|"bloom filter reported that key does not exist"
argument_list|)
expr_stmt|;
block|}
return|return
literal|null
return|;
block|}
comment|/**      * @see org.apache.hadoop.hbase.io.MapFile.Reader#getClosest(org.apache.hadoop.io.WritableComparable, org.apache.hadoop.io.Writable)      */
annotation|@
name|Override
specifier|public
name|WritableComparable
name|getClosest
parameter_list|(
name|WritableComparable
name|key
parameter_list|,
name|Writable
name|val
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|bloomFilter
operator|==
literal|null
condition|)
block|{
return|return
name|super
operator|.
name|getClosest
argument_list|(
name|key
argument_list|,
name|val
argument_list|)
return|;
block|}
comment|// Note - the key being passed to us is always a HStoreKey
if|if
condition|(
name|bloomFilter
operator|.
name|membershipTest
argument_list|(
name|getBloomFilterKey
argument_list|(
name|key
argument_list|)
argument_list|)
condition|)
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
literal|"bloom filter reported that key exists"
argument_list|)
expr_stmt|;
block|}
return|return
name|super
operator|.
name|getClosest
argument_list|(
name|key
argument_list|,
name|val
argument_list|)
return|;
block|}
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
literal|"bloom filter reported that key does not exist"
argument_list|)
expr_stmt|;
block|}
return|return
literal|null
return|;
block|}
comment|/**      * @return size of the bloom filter      */
specifier|public
name|int
name|getBloomFilterSize
parameter_list|()
block|{
return|return
name|bloomFilter
operator|==
literal|null
condition|?
literal|0
else|:
name|bloomFilter
operator|.
name|getVectorSize
argument_list|()
return|;
block|}
block|}
specifier|public
specifier|static
class|class
name|Writer
extends|extends
name|HBaseWriter
block|{
specifier|private
specifier|static
specifier|final
name|double
name|DEFAULT_NUMBER_OF_HASH_FUNCTIONS
init|=
literal|4.0
decl_stmt|;
specifier|private
specifier|final
name|BloomFilter
name|bloomFilter
decl_stmt|;
specifier|private
specifier|final
name|String
name|dirName
decl_stmt|;
specifier|private
specifier|final
name|FileSystem
name|fs
decl_stmt|;
comment|/**      * @param conf      * @param fs      * @param dirName      * @param compression      * @param filter      * @param nrows      * @param hri      * @throws IOException      */
specifier|public
name|Writer
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|FileSystem
name|fs
parameter_list|,
name|String
name|dirName
parameter_list|,
name|SequenceFile
operator|.
name|CompressionType
name|compression
parameter_list|,
specifier|final
name|boolean
name|filter
parameter_list|,
name|int
name|nrows
parameter_list|,
specifier|final
name|HRegionInfo
name|hri
parameter_list|)
throws|throws
name|IOException
block|{
name|super
argument_list|(
name|conf
argument_list|,
name|fs
argument_list|,
name|dirName
argument_list|,
name|compression
argument_list|,
name|hri
argument_list|)
expr_stmt|;
name|this
operator|.
name|dirName
operator|=
name|dirName
expr_stmt|;
name|this
operator|.
name|fs
operator|=
name|fs
expr_stmt|;
if|if
condition|(
name|filter
condition|)
block|{
comment|/*           * There is no way to automatically determine the vector size and the          * number of hash functions to use. In particular, bloom filters are          * very sensitive to the number of elements inserted into them. For          * HBase, the number of entries depends on the size of the data stored          * in the column. Currently the default region size is 256MB, so the          * number of entries is approximately           * 256MB / (average value size for column).          *           * If m denotes the number of bits in the Bloom filter (vectorSize),          * n denotes the number of elements inserted into the Bloom filter and          * k represents the number of hash functions used (nbHash), then          * according to Broder and Mitzenmacher,          *           * ( http://www.eecs.harvard.edu/~michaelm/NEWWORK/postscripts/BloomFilterSurvey.pdf )          *           * the probability of false positives is minimized when k is          * approximately m/n ln(2).          *           * If we fix the number of hash functions and know the number of          * entries, then the optimal vector size m = (k * n) / ln(2)          */
name|BloomFilter
name|f
init|=
literal|null
decl_stmt|;
try|try
block|{
name|f
operator|=
operator|new
name|BloomFilter
argument_list|(
operator|(
name|int
operator|)
name|Math
operator|.
name|ceil
argument_list|(
operator|(
name|DEFAULT_NUMBER_OF_HASH_FUNCTIONS
operator|*
operator|(
literal|1.0
operator|*
name|nrows
operator|)
operator|)
operator|/
name|Math
operator|.
name|log
argument_list|(
literal|2.0
argument_list|)
argument_list|)
argument_list|,
operator|(
name|int
operator|)
name|DEFAULT_NUMBER_OF_HASH_FUNCTIONS
argument_list|,
name|Hash
operator|.
name|getHashType
argument_list|(
name|conf
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Failed creating bloomfilter; proceeding without"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
name|this
operator|.
name|bloomFilter
operator|=
name|f
expr_stmt|;
block|}
else|else
block|{
name|this
operator|.
name|bloomFilter
operator|=
literal|null
expr_stmt|;
block|}
block|}
comment|/**      * @see org.apache.hadoop.hbase.io.MapFile.Writer#append(org.apache.hadoop.io.WritableComparable, org.apache.hadoop.io.Writable)      */
annotation|@
name|Override
specifier|public
name|void
name|append
parameter_list|(
name|WritableComparable
name|key
parameter_list|,
name|Writable
name|val
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|bloomFilter
operator|!=
literal|null
condition|)
block|{
name|bloomFilter
operator|.
name|add
argument_list|(
name|getBloomFilterKey
argument_list|(
name|key
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|super
operator|.
name|append
argument_list|(
name|key
argument_list|,
name|val
argument_list|)
expr_stmt|;
block|}
comment|/**      * @see org.apache.hadoop.hbase.io.MapFile.Writer#close()      */
annotation|@
name|Override
specifier|public
specifier|synchronized
name|void
name|close
parameter_list|()
throws|throws
name|IOException
block|{
name|super
operator|.
name|close
argument_list|()
expr_stmt|;
if|if
condition|(
name|this
operator|.
name|bloomFilter
operator|!=
literal|null
condition|)
block|{
name|flushBloomFilter
argument_list|()
expr_stmt|;
block|}
block|}
comment|/**      * Flushes bloom filter to disk      *       * @throws IOException      */
specifier|private
name|void
name|flushBloomFilter
parameter_list|()
throws|throws
name|IOException
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
literal|"flushing bloom filter for "
operator|+
name|this
operator|.
name|dirName
argument_list|)
expr_stmt|;
block|}
name|FSDataOutputStream
name|out
init|=
name|fs
operator|.
name|create
argument_list|(
operator|new
name|Path
argument_list|(
name|dirName
argument_list|,
name|BLOOMFILTER_FILE_NAME
argument_list|)
argument_list|)
decl_stmt|;
try|try
block|{
name|bloomFilter
operator|.
name|write
argument_list|(
name|out
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|out
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
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
literal|"flushed bloom filter for "
operator|+
name|this
operator|.
name|dirName
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|/**    * Custom bloom filter key maker.    * @param key    * @return Key made of bytes of row only.    */
specifier|protected
specifier|static
name|Key
name|getBloomFilterKey
parameter_list|(
name|WritableComparable
name|key
parameter_list|)
block|{
return|return
operator|new
name|Key
argument_list|(
operator|(
operator|(
name|HStoreKey
operator|)
name|key
operator|)
operator|.
name|getRow
argument_list|()
argument_list|)
return|;
block|}
block|}
end_class

end_unit

