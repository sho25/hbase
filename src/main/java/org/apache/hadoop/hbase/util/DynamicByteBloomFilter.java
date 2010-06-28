begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Copyright 2010 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|DataOutput
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

begin_comment
comment|/**  * Implements a<i>dynamic Bloom filter</i>, as defined in the INFOCOM 2006 paper.  *<p>  * A dynamic Bloom filter (DBF) makes use of a<code>s * m</code> bit matrix but  * each of the<code>s</code> rows is a standard Bloom filter. The creation  * process of a DBF is iterative. At the start, the DBF is a<code>1 * m</code>  * bit matrix, i.e., it is composed of a single standard Bloom filter.  * It assumes that<code>n<sub>r</sub></code> elements are recorded in the  * initial bit vector, where<code>n<sub>r</sub><= n</code> (<code>n</code> is  * the cardinality of the set<code>A</code> to record in the filter).  *<p>  * As the size of<code>A</code> grows during the execution of the application,  * several keys must be inserted in the DBF.  When inserting a key into the DBF,  * one must first get an active Bloom filter in the matrix.  A Bloom filter is  * active when the number of recorded keys,<code>n<sub>r</sub></code>, is  * strictly less than the current cardinality of<code>A</code>,<code>n</code>.  * If an active Bloom filter is found, the key is inserted and  *<code>n<sub>r</sub></code> is incremented by one. On the other hand, if there  * is no active Bloom filter, a new one is created (i.e., a new row is added to  * the matrix) according to the current size of<code>A</code> and the element  * is added in this new Bloom filter and the<code>n<sub>r</sub></code> value of  * this new Bloom filter is set to one.  A given key is said to belong to the  * DBF if the<code>k</code> positions are set to one in one of the matrix rows.  *<p>  * Originally created by  *<a href="http://www.one-lab.org">European Commission One-Lab Project 034819</a>.  *  * @see BloomFilter A Bloom filter  *  * @see<a href="http://www.cse.fau.edu/~jie/research/publications/Publication_files/infocom2006.pdf">Theory and Network Applications of Dynamic Bloom Filters</a>  */
end_comment

begin_class
specifier|public
class|class
name|DynamicByteBloomFilter
implements|implements
name|BloomFilter
block|{
comment|/** Current file format version */
specifier|public
specifier|static
specifier|final
name|int
name|VERSION
init|=
literal|2
decl_stmt|;
comment|/** Maximum number of keys in a dynamic Bloom filter row. */
specifier|protected
specifier|final
name|int
name|keyInterval
decl_stmt|;
comment|/** The maximum false positive rate per bloom */
specifier|protected
specifier|final
name|float
name|errorRate
decl_stmt|;
comment|/** Hash type */
specifier|protected
specifier|final
name|int
name|hashType
decl_stmt|;
comment|/** The number of keys recorded in the current Bloom filter. */
specifier|protected
name|int
name|curKeys
decl_stmt|;
comment|/** expected size of bloom filter matrix (used during reads) */
specifier|protected
name|int
name|readMatrixSize
decl_stmt|;
comment|/** The matrix of Bloom filters (contains bloom data only during writes). */
specifier|protected
name|ByteBloomFilter
index|[]
name|matrix
decl_stmt|;
comment|/**    * Normal read constructor.  Loads bloom filter meta data.    * @param meta stored bloom meta data    * @throws IllegalArgumentException meta data is invalid    */
specifier|public
name|DynamicByteBloomFilter
parameter_list|(
name|ByteBuffer
name|meta
parameter_list|)
throws|throws
name|IllegalArgumentException
block|{
name|int
name|version
init|=
name|meta
operator|.
name|getInt
argument_list|()
decl_stmt|;
if|if
condition|(
name|version
operator|!=
name|VERSION
condition|)
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Bad version"
argument_list|)
throw|;
name|this
operator|.
name|keyInterval
operator|=
name|meta
operator|.
name|getInt
argument_list|()
expr_stmt|;
name|this
operator|.
name|errorRate
operator|=
name|meta
operator|.
name|getFloat
argument_list|()
expr_stmt|;
name|this
operator|.
name|hashType
operator|=
name|meta
operator|.
name|getInt
argument_list|()
expr_stmt|;
name|this
operator|.
name|readMatrixSize
operator|=
name|meta
operator|.
name|getInt
argument_list|()
expr_stmt|;
name|this
operator|.
name|curKeys
operator|=
name|meta
operator|.
name|getInt
argument_list|()
expr_stmt|;
name|readSanityCheck
argument_list|()
expr_stmt|;
name|this
operator|.
name|matrix
operator|=
operator|new
name|ByteBloomFilter
index|[
literal|1
index|]
expr_stmt|;
name|this
operator|.
name|matrix
index|[
literal|0
index|]
operator|=
operator|new
name|ByteBloomFilter
argument_list|(
name|keyInterval
argument_list|,
name|errorRate
argument_list|,
name|hashType
argument_list|,
literal|0
argument_list|)
expr_stmt|;
block|}
comment|/**    * Normal write constructor.  Note that this doesn't allocate bloom data by    * default.  Instead, call allocBloom() before adding entries.    * @param hashType type of the hashing function (see {@link org.apache.hadoop.util.hash.Hash}).    * @param keyInterval Maximum number of keys to record per Bloom filter row.    * @throws IllegalArgumentException The input parameters were invalid    */
specifier|public
name|DynamicByteBloomFilter
parameter_list|(
name|int
name|keyInterval
parameter_list|,
name|float
name|errorRate
parameter_list|,
name|int
name|hashType
parameter_list|)
throws|throws
name|IllegalArgumentException
block|{
name|this
operator|.
name|keyInterval
operator|=
name|keyInterval
expr_stmt|;
name|this
operator|.
name|errorRate
operator|=
name|errorRate
expr_stmt|;
name|this
operator|.
name|hashType
operator|=
name|hashType
expr_stmt|;
name|this
operator|.
name|curKeys
operator|=
literal|0
expr_stmt|;
if|if
condition|(
name|keyInterval
operator|<=
literal|0
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"keyCount must be> 0"
argument_list|)
throw|;
block|}
name|this
operator|.
name|matrix
operator|=
operator|new
name|ByteBloomFilter
index|[
literal|1
index|]
expr_stmt|;
name|this
operator|.
name|matrix
index|[
literal|0
index|]
operator|=
operator|new
name|ByteBloomFilter
argument_list|(
name|keyInterval
argument_list|,
name|errorRate
argument_list|,
name|hashType
argument_list|,
literal|0
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|allocBloom
parameter_list|()
block|{
name|this
operator|.
name|matrix
index|[
literal|0
index|]
operator|.
name|allocBloom
argument_list|()
expr_stmt|;
block|}
name|void
name|readSanityCheck
parameter_list|()
throws|throws
name|IllegalArgumentException
block|{
if|if
condition|(
name|this
operator|.
name|curKeys
operator|<=
literal|0
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"last bloom's key count invalid"
argument_list|)
throw|;
block|}
if|if
condition|(
name|this
operator|.
name|readMatrixSize
operator|<=
literal|0
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"matrix size must be known"
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|add
parameter_list|(
name|byte
index|[]
name|buf
parameter_list|,
name|int
name|offset
parameter_list|,
name|int
name|len
parameter_list|)
block|{
name|BloomFilter
name|bf
init|=
name|getCurBloom
argument_list|()
decl_stmt|;
if|if
condition|(
name|bf
operator|==
literal|null
condition|)
block|{
name|addRow
argument_list|()
expr_stmt|;
name|bf
operator|=
name|matrix
index|[
name|matrix
operator|.
name|length
operator|-
literal|1
index|]
expr_stmt|;
name|curKeys
operator|=
literal|0
expr_stmt|;
block|}
name|bf
operator|.
name|add
argument_list|(
name|buf
argument_list|,
name|offset
argument_list|,
name|len
argument_list|)
expr_stmt|;
name|curKeys
operator|++
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|add
parameter_list|(
name|byte
index|[]
name|buf
parameter_list|)
block|{
name|add
argument_list|(
name|buf
argument_list|,
literal|0
argument_list|,
name|buf
operator|.
name|length
argument_list|)
expr_stmt|;
block|}
comment|/**    * Should only be used in tests when writing a bloom filter.    */
name|boolean
name|contains
parameter_list|(
name|byte
index|[]
name|buf
parameter_list|)
block|{
return|return
name|contains
argument_list|(
name|buf
argument_list|,
literal|0
argument_list|,
name|buf
operator|.
name|length
argument_list|)
return|;
block|}
comment|/**    * Should only be used in tests when writing a bloom filter.    */
name|boolean
name|contains
parameter_list|(
name|byte
index|[]
name|buf
parameter_list|,
name|int
name|offset
parameter_list|,
name|int
name|length
parameter_list|)
block|{
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|matrix
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|matrix
index|[
name|i
index|]
operator|.
name|contains
argument_list|(
name|buf
argument_list|,
name|offset
argument_list|,
name|length
argument_list|)
condition|)
block|{
return|return
literal|true
return|;
block|}
block|}
return|return
literal|false
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|contains
parameter_list|(
name|byte
index|[]
name|buf
parameter_list|,
name|ByteBuffer
name|theBloom
parameter_list|)
block|{
return|return
name|contains
argument_list|(
name|buf
argument_list|,
literal|0
argument_list|,
name|buf
operator|.
name|length
argument_list|,
name|theBloom
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|contains
parameter_list|(
name|byte
index|[]
name|buf
parameter_list|,
name|int
name|offset
parameter_list|,
name|int
name|length
parameter_list|,
name|ByteBuffer
name|theBloom
parameter_list|)
block|{
if|if
condition|(
name|offset
operator|+
name|length
operator|>
name|buf
operator|.
name|length
condition|)
block|{
return|return
literal|false
return|;
block|}
comment|// current version assumes uniform size
name|int
name|bytesPerBloom
init|=
name|this
operator|.
name|matrix
index|[
literal|0
index|]
operator|.
name|getByteSize
argument_list|()
decl_stmt|;
if|if
condition|(
name|theBloom
operator|.
name|limit
argument_list|()
operator|!=
name|bytesPerBloom
operator|*
name|readMatrixSize
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Bloom does not match expected size"
argument_list|)
throw|;
block|}
name|ByteBuffer
name|tmp
init|=
name|theBloom
operator|.
name|duplicate
argument_list|()
decl_stmt|;
comment|// note: actually searching an array of blooms that have been serialized
for|for
control|(
name|int
name|m
init|=
literal|0
init|;
name|m
operator|<
name|readMatrixSize
condition|;
operator|++
name|m
control|)
block|{
name|tmp
operator|.
name|position
argument_list|(
name|m
operator|*
name|bytesPerBloom
argument_list|)
expr_stmt|;
name|tmp
operator|.
name|limit
argument_list|(
name|tmp
operator|.
name|position
argument_list|()
operator|+
name|bytesPerBloom
argument_list|)
expr_stmt|;
name|boolean
name|match
init|=
name|this
operator|.
name|matrix
index|[
literal|0
index|]
operator|.
name|contains
argument_list|(
name|buf
argument_list|,
name|offset
argument_list|,
name|length
argument_list|,
name|tmp
operator|.
name|slice
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|match
condition|)
block|{
return|return
literal|true
return|;
block|}
block|}
comment|// matched no bloom filters
return|return
literal|false
return|;
block|}
name|int
name|bloomCount
parameter_list|()
block|{
return|return
name|Math
operator|.
name|max
argument_list|(
name|this
operator|.
name|matrix
operator|.
name|length
argument_list|,
name|this
operator|.
name|readMatrixSize
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getKeyCount
parameter_list|()
block|{
return|return
operator|(
name|bloomCount
argument_list|()
operator|-
literal|1
operator|)
operator|*
name|this
operator|.
name|keyInterval
operator|+
name|this
operator|.
name|curKeys
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getMaxKeys
parameter_list|()
block|{
return|return
name|bloomCount
argument_list|()
operator|*
name|this
operator|.
name|keyInterval
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getByteSize
parameter_list|()
block|{
return|return
name|bloomCount
argument_list|()
operator|*
name|this
operator|.
name|matrix
index|[
literal|0
index|]
operator|.
name|getByteSize
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|compactBloom
parameter_list|()
block|{   }
comment|/**    * Adds a new row to<i>this</i> dynamic Bloom filter.    */
specifier|private
name|void
name|addRow
parameter_list|()
block|{
name|ByteBloomFilter
index|[]
name|tmp
init|=
operator|new
name|ByteBloomFilter
index|[
name|matrix
operator|.
name|length
operator|+
literal|1
index|]
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
name|matrix
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|tmp
index|[
name|i
index|]
operator|=
name|matrix
index|[
name|i
index|]
expr_stmt|;
block|}
name|tmp
index|[
name|tmp
operator|.
name|length
operator|-
literal|1
index|]
operator|=
operator|new
name|ByteBloomFilter
argument_list|(
name|keyInterval
argument_list|,
name|errorRate
argument_list|,
name|hashType
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|tmp
index|[
name|tmp
operator|.
name|length
operator|-
literal|1
index|]
operator|.
name|allocBloom
argument_list|()
expr_stmt|;
name|matrix
operator|=
name|tmp
expr_stmt|;
block|}
comment|/**    * Returns the currently-unfilled row in the dynamic Bloom Filter array.    * @return BloomFilter The active standard Bloom filter.    *<code>Null</code> otherwise.    */
specifier|private
name|BloomFilter
name|getCurBloom
parameter_list|()
block|{
if|if
condition|(
name|curKeys
operator|>=
name|keyInterval
condition|)
block|{
return|return
literal|null
return|;
block|}
return|return
name|matrix
index|[
name|matrix
operator|.
name|length
operator|-
literal|1
index|]
return|;
block|}
annotation|@
name|Override
specifier|public
name|Writable
name|getMetaWriter
parameter_list|()
block|{
return|return
operator|new
name|MetaWriter
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|Writable
name|getDataWriter
parameter_list|()
block|{
return|return
operator|new
name|DataWriter
argument_list|()
return|;
block|}
specifier|private
class|class
name|MetaWriter
implements|implements
name|Writable
block|{
specifier|protected
name|MetaWriter
parameter_list|()
block|{}
annotation|@
name|Override
specifier|public
name|void
name|readFields
parameter_list|(
name|DataInput
name|arg0
parameter_list|)
throws|throws
name|IOException
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Cant read with this class."
argument_list|)
throw|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|write
parameter_list|(
name|DataOutput
name|out
parameter_list|)
throws|throws
name|IOException
block|{
name|out
operator|.
name|writeInt
argument_list|(
name|VERSION
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeInt
argument_list|(
name|keyInterval
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeFloat
argument_list|(
name|errorRate
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeInt
argument_list|(
name|hashType
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeInt
argument_list|(
name|matrix
operator|.
name|length
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeInt
argument_list|(
name|curKeys
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
class|class
name|DataWriter
implements|implements
name|Writable
block|{
specifier|protected
name|DataWriter
parameter_list|()
block|{}
annotation|@
name|Override
specifier|public
name|void
name|readFields
parameter_list|(
name|DataInput
name|arg0
parameter_list|)
throws|throws
name|IOException
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Cant read with this class."
argument_list|)
throw|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|write
parameter_list|(
name|DataOutput
name|out
parameter_list|)
throws|throws
name|IOException
block|{
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|matrix
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
name|matrix
index|[
name|i
index|]
operator|.
name|writeBloom
argument_list|(
name|out
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit

