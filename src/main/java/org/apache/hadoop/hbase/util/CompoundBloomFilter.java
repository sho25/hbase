begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Copyright 2011 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|java
operator|.
name|nio
operator|.
name|ByteBuffer
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
name|hbase
operator|.
name|io
operator|.
name|hfile
operator|.
name|BlockType
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
name|FixedFileTrailer
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
name|io
operator|.
name|hfile
operator|.
name|HFileBlock
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
name|HFileBlockIndex
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
name|RawComparator
import|;
end_import

begin_comment
comment|/**  * A Bloom filter implementation built on top of {@link ByteBloomFilter},  * encapsulating a set of fixed-size Bloom filters written out at the time of  * {@link org.apache.hadoop.hbase.io.hfile.HFile} generation into the data  * block stream, and loaded on demand at query time. This class only provides  * reading capabilities.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|CompoundBloomFilter
extends|extends
name|CompoundBloomFilterBase
implements|implements
name|BloomFilter
block|{
comment|/** Used to load chunks on demand */
specifier|private
name|HFile
operator|.
name|Reader
name|reader
decl_stmt|;
specifier|private
name|HFileBlockIndex
operator|.
name|BlockIndexReader
name|index
decl_stmt|;
specifier|private
name|int
name|hashCount
decl_stmt|;
specifier|private
name|Hash
name|hash
decl_stmt|;
specifier|private
name|long
index|[]
name|numQueriesPerChunk
decl_stmt|;
specifier|private
name|long
index|[]
name|numPositivesPerChunk
decl_stmt|;
comment|/**    * De-serialization for compound Bloom filter metadata. Must be consistent    * with what {@link CompoundBloomFilterWriter} does.    *    * @param meta serialized Bloom filter metadata without any magic blocks    * @throws IOException    */
specifier|public
name|CompoundBloomFilter
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
name|IOException
block|{
name|this
operator|.
name|reader
operator|=
name|reader
expr_stmt|;
name|totalByteSize
operator|=
name|meta
operator|.
name|readLong
argument_list|()
expr_stmt|;
name|hashCount
operator|=
name|meta
operator|.
name|readInt
argument_list|()
expr_stmt|;
name|hashType
operator|=
name|meta
operator|.
name|readInt
argument_list|()
expr_stmt|;
name|totalKeyCount
operator|=
name|meta
operator|.
name|readLong
argument_list|()
expr_stmt|;
name|totalMaxKeys
operator|=
name|meta
operator|.
name|readLong
argument_list|()
expr_stmt|;
name|numChunks
operator|=
name|meta
operator|.
name|readInt
argument_list|()
expr_stmt|;
name|comparator
operator|=
name|FixedFileTrailer
operator|.
name|createComparator
argument_list|(
name|Bytes
operator|.
name|toString
argument_list|(
name|Bytes
operator|.
name|readByteArray
argument_list|(
name|meta
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|hash
operator|=
name|Hash
operator|.
name|getInstance
argument_list|(
name|hashType
argument_list|)
expr_stmt|;
if|if
condition|(
name|hash
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Invalid hash type: "
operator|+
name|hashType
argument_list|)
throw|;
block|}
name|index
operator|=
operator|new
name|HFileBlockIndex
operator|.
name|BlockIndexReader
argument_list|(
name|comparator
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|index
operator|.
name|readRootIndex
argument_list|(
name|meta
argument_list|,
name|numChunks
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|contains
parameter_list|(
name|byte
index|[]
name|key
parameter_list|,
name|int
name|keyOffset
parameter_list|,
name|int
name|keyLength
parameter_list|,
name|ByteBuffer
name|bloom
parameter_list|)
block|{
comment|// We try to store the result in this variable so we can update stats for
comment|// testing, but when an error happens, we log a message and return.
name|boolean
name|result
decl_stmt|;
name|int
name|block
init|=
name|index
operator|.
name|rootBlockContainingKey
argument_list|(
name|key
argument_list|,
name|keyOffset
argument_list|,
name|keyLength
argument_list|)
decl_stmt|;
if|if
condition|(
name|block
operator|<
literal|0
condition|)
block|{
name|result
operator|=
literal|false
expr_stmt|;
comment|// This key is not in the file.
block|}
else|else
block|{
name|HFileBlock
name|bloomBlock
decl_stmt|;
try|try
block|{
comment|// We cache the block and use a positional read.
name|bloomBlock
operator|=
name|reader
operator|.
name|readBlock
argument_list|(
name|index
operator|.
name|getRootBlockOffset
argument_list|(
name|block
argument_list|)
argument_list|,
name|index
operator|.
name|getRootBlockDataSize
argument_list|(
name|block
argument_list|)
argument_list|,
literal|true
argument_list|,
literal|true
argument_list|,
literal|false
argument_list|,
name|BlockType
operator|.
name|BLOOM_CHUNK
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|ex
parameter_list|)
block|{
comment|// The Bloom filter is broken, turn it off.
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Failed to load Bloom block for key "
operator|+
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|key
argument_list|,
name|keyOffset
argument_list|,
name|keyLength
argument_list|)
argument_list|,
name|ex
argument_list|)
throw|;
block|}
name|ByteBuffer
name|bloomBuf
init|=
name|bloomBlock
operator|.
name|getBufferReadOnly
argument_list|()
decl_stmt|;
name|result
operator|=
name|ByteBloomFilter
operator|.
name|contains
argument_list|(
name|key
argument_list|,
name|keyOffset
argument_list|,
name|keyLength
argument_list|,
name|bloomBuf
operator|.
name|array
argument_list|()
argument_list|,
name|bloomBuf
operator|.
name|arrayOffset
argument_list|()
operator|+
name|bloomBlock
operator|.
name|headerSize
argument_list|()
argument_list|,
name|bloomBlock
operator|.
name|getUncompressedSizeWithoutHeader
argument_list|()
argument_list|,
name|hash
argument_list|,
name|hashCount
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|numQueriesPerChunk
operator|!=
literal|null
operator|&&
name|block
operator|>=
literal|0
condition|)
block|{
comment|// Update statistics. Only used in unit tests.
operator|++
name|numQueriesPerChunk
index|[
name|block
index|]
expr_stmt|;
if|if
condition|(
name|result
condition|)
operator|++
name|numPositivesPerChunk
index|[
name|block
index|]
expr_stmt|;
block|}
return|return
name|result
return|;
block|}
specifier|public
name|boolean
name|supportsAutoLoading
parameter_list|()
block|{
return|return
literal|true
return|;
block|}
specifier|public
name|int
name|getNumChunks
parameter_list|()
block|{
return|return
name|numChunks
return|;
block|}
annotation|@
name|Override
specifier|public
name|RawComparator
argument_list|<
name|byte
index|[]
argument_list|>
name|getComparator
parameter_list|()
block|{
return|return
name|comparator
return|;
block|}
specifier|public
name|void
name|enableTestingStats
parameter_list|()
block|{
name|numQueriesPerChunk
operator|=
operator|new
name|long
index|[
name|numChunks
index|]
expr_stmt|;
name|numPositivesPerChunk
operator|=
operator|new
name|long
index|[
name|numChunks
index|]
expr_stmt|;
block|}
specifier|public
name|String
name|formatTestingStats
parameter_list|()
block|{
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|numChunks
condition|;
operator|++
name|i
control|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|"chunk #"
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|i
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|": queries="
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|numQueriesPerChunk
index|[
name|i
index|]
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|", positives="
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|numPositivesPerChunk
index|[
name|i
index|]
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|", positiveRatio="
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|numPositivesPerChunk
index|[
name|i
index|]
operator|*
literal|1.0
operator|/
name|numQueriesPerChunk
index|[
name|i
index|]
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|";\n"
argument_list|)
expr_stmt|;
block|}
return|return
name|sb
operator|.
name|toString
argument_list|()
return|;
block|}
specifier|public
name|long
name|getNumQueriesForTesting
parameter_list|(
name|int
name|chunk
parameter_list|)
block|{
return|return
name|numQueriesPerChunk
index|[
name|chunk
index|]
return|;
block|}
specifier|public
name|long
name|getNumPositivesForTesting
parameter_list|(
name|int
name|chunk
parameter_list|)
block|{
return|return
name|numPositivesPerChunk
index|[
name|chunk
index|]
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|ByteBloomFilter
operator|.
name|formatStats
argument_list|(
name|this
argument_list|)
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|ByteBloomFilter
operator|.
name|STATS_RECORD_SEP
operator|+
literal|"Number of chunks: "
operator|+
name|numChunks
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|ByteBloomFilter
operator|.
name|STATS_RECORD_SEP
operator|+
literal|"Comparator: "
operator|+
name|comparator
operator|.
name|getClass
argument_list|()
operator|.
name|getSimpleName
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|sb
operator|.
name|toString
argument_list|()
return|;
block|}
block|}
end_class

end_unit

