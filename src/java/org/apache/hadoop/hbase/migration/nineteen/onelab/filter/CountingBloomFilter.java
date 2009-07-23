begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  *  * Copyright (c) 2005, European Commission project OneLab under contract 034819 (http://www.one-lab.org)  * All rights reserved.  * Redistribution and use in source and binary forms, with or   * without modification, are permitted provided that the following   * conditions are met:  *  - Redistributions of source code must retain the above copyright   *    notice, this list of conditions and the following disclaimer.  *  - Redistributions in binary form must reproduce the above copyright   *    notice, this list of conditions and the following disclaimer in   *    the documentation and/or other materials provided with the distribution.  *  - Neither the name of the University Catholique de Louvain - UCL  *    nor the names of its contributors may be used to endorse or   *    promote products derived from this software without specific prior   *    written permission.  *      * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS   * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT   * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS   * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE   * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,   * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,   * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;   * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER   * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT   * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN   * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE   * POSSIBILITY OF SUCH DAMAGE.  */
end_comment

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
name|migration
operator|.
name|nineteen
operator|.
name|onelab
operator|.
name|filter
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
name|util
operator|.
name|Arrays
import|;
end_import

begin_comment
comment|//TODO: remove
end_comment

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

begin_comment
comment|/**  * Implements a<i>counting Bloom filter</i>, as defined by Fan et al. in a ToN  * 2000 paper.  *<p>  * A counting Bloom filter is an improvement to standard a Bloom filter as it  * allows dynamic additions and deletions of set membership information.  This   * is achieved through the use of a counting vector instead of a bit vector.  *   * contract<a href="http://www.one-lab.org">European Commission One-Lab Project 034819</a>.  *  * @version 1.1 - 19 Jan. 08  *   */
end_comment

begin_class
specifier|public
specifier|final
class|class
name|CountingBloomFilter
extends|extends
name|Filter
block|{
comment|/** Storage for the counting buckets */
specifier|private
name|long
index|[]
name|buckets
decl_stmt|;
comment|/** We are using 4bit buckets, so each bucket can count to 15 */
specifier|private
specifier|final
specifier|static
name|long
name|BUCKET_MAX_VALUE
init|=
literal|15
decl_stmt|;
comment|/** Default constructor - use with readFields */
specifier|public
name|CountingBloomFilter
parameter_list|()
block|{}
comment|/**    * Constructor    * @param vectorSize The vector size of<i>this</i> filter.    * @param nbHash The number of hash function to consider.    * @param hashType type of the hashing function (see {@link Hash}).    */
specifier|public
name|CountingBloomFilter
parameter_list|(
name|int
name|vectorSize
parameter_list|,
name|int
name|nbHash
parameter_list|,
name|int
name|hashType
parameter_list|)
block|{
name|super
argument_list|(
name|vectorSize
argument_list|,
name|nbHash
argument_list|,
name|hashType
argument_list|)
expr_stmt|;
name|buckets
operator|=
operator|new
name|long
index|[
name|buckets2words
argument_list|(
name|vectorSize
argument_list|)
index|]
expr_stmt|;
block|}
comment|//end constructor
comment|/** returns the number of 64 bit words it would take to hold vectorSize buckets */
specifier|private
specifier|static
name|int
name|buckets2words
parameter_list|(
name|int
name|vectorSize
parameter_list|)
block|{
return|return
operator|(
operator|(
name|vectorSize
operator|-
literal|1
operator|)
operator|>>>
literal|4
operator|)
operator|+
literal|1
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|add
parameter_list|(
name|Key
name|key
parameter_list|)
block|{
if|if
condition|(
name|key
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|NullPointerException
argument_list|(
literal|"key can not be null"
argument_list|)
throw|;
block|}
name|int
index|[]
name|h
init|=
name|hash
operator|.
name|hash
argument_list|(
name|key
argument_list|)
decl_stmt|;
name|hash
operator|.
name|clear
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
name|nbHash
condition|;
name|i
operator|++
control|)
block|{
comment|// find the bucket
name|int
name|wordNum
init|=
name|h
index|[
name|i
index|]
operator|>>
literal|4
decl_stmt|;
comment|// div 16
name|int
name|bucketShift
init|=
operator|(
name|h
index|[
name|i
index|]
operator|&
literal|0x0f
operator|)
operator|<<
literal|2
decl_stmt|;
comment|// (mod 16) * 4
name|long
name|bucketMask
init|=
literal|15L
operator|<<
name|bucketShift
decl_stmt|;
name|long
name|bucketValue
init|=
operator|(
name|buckets
index|[
name|wordNum
index|]
operator|&
name|bucketMask
operator|)
operator|>>>
name|bucketShift
decl_stmt|;
comment|// only increment if the count in the bucket is less than BUCKET_MAX_VALUE
if|if
condition|(
name|bucketValue
operator|<
name|BUCKET_MAX_VALUE
condition|)
block|{
comment|// increment by 1
name|buckets
index|[
name|wordNum
index|]
operator|=
operator|(
name|buckets
index|[
name|wordNum
index|]
operator|&
operator|~
name|bucketMask
operator|)
operator||
operator|(
operator|(
name|bucketValue
operator|+
literal|1
operator|)
operator|<<
name|bucketShift
operator|)
expr_stmt|;
block|}
block|}
block|}
comment|//end add()
comment|/**    * Removes a specified key from<i>this</i> counting Bloom filter.    *<p>    *<b>Invariant</b>: nothing happens if the specified key does not belong to<i>this</i> counter Bloom filter.    * @param key The key to remove.    */
specifier|public
name|void
name|delete
parameter_list|(
name|Key
name|key
parameter_list|)
block|{
if|if
condition|(
name|key
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|NullPointerException
argument_list|(
literal|"Key may not be null"
argument_list|)
throw|;
block|}
if|if
condition|(
operator|!
name|membershipTest
argument_list|(
name|key
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Key is not a member"
argument_list|)
throw|;
block|}
name|int
index|[]
name|h
init|=
name|hash
operator|.
name|hash
argument_list|(
name|key
argument_list|)
decl_stmt|;
name|hash
operator|.
name|clear
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
name|nbHash
condition|;
name|i
operator|++
control|)
block|{
comment|// find the bucket
name|int
name|wordNum
init|=
name|h
index|[
name|i
index|]
operator|>>
literal|4
decl_stmt|;
comment|// div 16
name|int
name|bucketShift
init|=
operator|(
name|h
index|[
name|i
index|]
operator|&
literal|0x0f
operator|)
operator|<<
literal|2
decl_stmt|;
comment|// (mod 16) * 4
name|long
name|bucketMask
init|=
literal|15L
operator|<<
name|bucketShift
decl_stmt|;
name|long
name|bucketValue
init|=
operator|(
name|buckets
index|[
name|wordNum
index|]
operator|&
name|bucketMask
operator|)
operator|>>>
name|bucketShift
decl_stmt|;
comment|// only decrement if the count in the bucket is between 0 and BUCKET_MAX_VALUE
if|if
condition|(
name|bucketValue
operator|>=
literal|1
operator|&&
name|bucketValue
operator|<
name|BUCKET_MAX_VALUE
condition|)
block|{
comment|// decrement by 1
name|buckets
index|[
name|wordNum
index|]
operator|=
operator|(
name|buckets
index|[
name|wordNum
index|]
operator|&
operator|~
name|bucketMask
operator|)
operator||
operator|(
operator|(
name|bucketValue
operator|-
literal|1
operator|)
operator|<<
name|bucketShift
operator|)
expr_stmt|;
block|}
block|}
block|}
comment|//end delete
annotation|@
name|Override
specifier|public
name|void
name|and
parameter_list|(
name|Filter
name|filter
parameter_list|)
block|{
if|if
condition|(
name|filter
operator|==
literal|null
operator|||
operator|!
operator|(
name|filter
operator|instanceof
name|CountingBloomFilter
operator|)
operator|||
name|filter
operator|.
name|vectorSize
operator|!=
name|this
operator|.
name|vectorSize
operator|||
name|filter
operator|.
name|nbHash
operator|!=
name|this
operator|.
name|nbHash
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"filters cannot be and-ed"
argument_list|)
throw|;
block|}
name|CountingBloomFilter
name|cbf
init|=
operator|(
name|CountingBloomFilter
operator|)
name|filter
decl_stmt|;
name|int
name|sizeInWords
init|=
name|buckets2words
argument_list|(
name|vectorSize
argument_list|)
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
name|sizeInWords
condition|;
name|i
operator|++
control|)
block|{
name|this
operator|.
name|buckets
index|[
name|i
index|]
operator|&=
name|cbf
operator|.
name|buckets
index|[
name|i
index|]
expr_stmt|;
block|}
block|}
comment|//end and()
annotation|@
name|Override
specifier|public
name|boolean
name|membershipTest
parameter_list|(
name|Key
name|key
parameter_list|)
block|{
if|if
condition|(
name|key
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|NullPointerException
argument_list|(
literal|"Key may not be null"
argument_list|)
throw|;
block|}
name|int
index|[]
name|h
init|=
name|hash
operator|.
name|hash
argument_list|(
name|key
argument_list|)
decl_stmt|;
name|hash
operator|.
name|clear
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
name|nbHash
condition|;
name|i
operator|++
control|)
block|{
comment|// find the bucket
name|int
name|wordNum
init|=
name|h
index|[
name|i
index|]
operator|>>
literal|4
decl_stmt|;
comment|// div 16
name|int
name|bucketShift
init|=
operator|(
name|h
index|[
name|i
index|]
operator|&
literal|0x0f
operator|)
operator|<<
literal|2
decl_stmt|;
comment|// (mod 16) * 4
name|long
name|bucketMask
init|=
literal|15L
operator|<<
name|bucketShift
decl_stmt|;
if|if
condition|(
operator|(
name|buckets
index|[
name|wordNum
index|]
operator|&
name|bucketMask
operator|)
operator|==
literal|0
condition|)
block|{
return|return
literal|false
return|;
block|}
block|}
return|return
literal|true
return|;
block|}
comment|//end membershipTest()
comment|/**    * This method calculates an approximate count of the key, i.e. how many    * times the key was added to the filter. This allows the filter to be    * used as an approximate<code>key -&gt; count</code> map.    *<p>NOTE: due to the bucket size of this filter, inserting the same    * key more than 15 times will cause an overflow at all filter positions    * associated with this key, and it will significantly increase the error    * rate for this and other keys. For this reason the filter can only be    * used to store small count values<code>0&lt;= N&lt;&lt; 15</code>.    * @param key key to be tested    * @return 0 if the key is not present. Otherwise, a positive value v will    * be returned such that<code>v == count</code> with probability equal to the    * error rate of this filter, and<code>v&gt; count</code> otherwise.    * Additionally, if the filter experienced an underflow as a result of    * {@link #delete(Key)} operation, the return value may be lower than the    *<code>count</code> with the probability of the false negative rate of such    * filter.    */
specifier|public
name|int
name|approximateCount
parameter_list|(
name|Key
name|key
parameter_list|)
block|{
name|int
name|res
init|=
name|Integer
operator|.
name|MAX_VALUE
decl_stmt|;
name|int
index|[]
name|h
init|=
name|hash
operator|.
name|hash
argument_list|(
name|key
argument_list|)
decl_stmt|;
name|hash
operator|.
name|clear
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
name|nbHash
condition|;
name|i
operator|++
control|)
block|{
comment|// find the bucket
name|int
name|wordNum
init|=
name|h
index|[
name|i
index|]
operator|>>
literal|4
decl_stmt|;
comment|// div 16
name|int
name|bucketShift
init|=
operator|(
name|h
index|[
name|i
index|]
operator|&
literal|0x0f
operator|)
operator|<<
literal|2
decl_stmt|;
comment|// (mod 16) * 4
name|long
name|bucketMask
init|=
literal|15L
operator|<<
name|bucketShift
decl_stmt|;
name|long
name|bucketValue
init|=
operator|(
name|buckets
index|[
name|wordNum
index|]
operator|&
name|bucketMask
operator|)
operator|>>>
name|bucketShift
decl_stmt|;
if|if
condition|(
name|bucketValue
operator|<
name|res
condition|)
name|res
operator|=
operator|(
name|int
operator|)
name|bucketValue
expr_stmt|;
block|}
if|if
condition|(
name|res
operator|!=
name|Integer
operator|.
name|MAX_VALUE
condition|)
block|{
return|return
name|res
return|;
block|}
else|else
block|{
return|return
literal|0
return|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|not
parameter_list|()
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"not() is undefined for "
operator|+
name|this
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
throw|;
block|}
comment|//end not()
annotation|@
name|Override
specifier|public
name|void
name|or
parameter_list|(
name|Filter
name|filter
parameter_list|)
block|{
if|if
condition|(
name|filter
operator|==
literal|null
operator|||
operator|!
operator|(
name|filter
operator|instanceof
name|CountingBloomFilter
operator|)
operator|||
name|filter
operator|.
name|vectorSize
operator|!=
name|this
operator|.
name|vectorSize
operator|||
name|filter
operator|.
name|nbHash
operator|!=
name|this
operator|.
name|nbHash
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"filters cannot be or-ed"
argument_list|)
throw|;
block|}
name|CountingBloomFilter
name|cbf
init|=
operator|(
name|CountingBloomFilter
operator|)
name|filter
decl_stmt|;
name|int
name|sizeInWords
init|=
name|buckets2words
argument_list|(
name|vectorSize
argument_list|)
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
name|sizeInWords
condition|;
name|i
operator|++
control|)
block|{
name|this
operator|.
name|buckets
index|[
name|i
index|]
operator||=
name|cbf
operator|.
name|buckets
index|[
name|i
index|]
expr_stmt|;
block|}
block|}
comment|//end or()
annotation|@
name|Override
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unused"
argument_list|)
specifier|public
name|void
name|xor
parameter_list|(
name|Filter
name|filter
parameter_list|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"xor() is undefined for "
operator|+
name|this
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
throw|;
block|}
comment|//end xor()
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
name|StringBuilder
name|res
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
name|vectorSize
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|i
operator|>
literal|0
condition|)
block|{
name|res
operator|.
name|append
argument_list|(
literal|" "
argument_list|)
expr_stmt|;
block|}
name|int
name|wordNum
init|=
name|i
operator|>>
literal|4
decl_stmt|;
comment|// div 16
name|int
name|bucketShift
init|=
operator|(
name|i
operator|&
literal|0x0f
operator|)
operator|<<
literal|2
decl_stmt|;
comment|// (mod 16) * 4
name|long
name|bucketMask
init|=
literal|15L
operator|<<
name|bucketShift
decl_stmt|;
name|long
name|bucketValue
init|=
operator|(
name|buckets
index|[
name|wordNum
index|]
operator|&
name|bucketMask
operator|)
operator|>>>
name|bucketShift
decl_stmt|;
name|res
operator|.
name|append
argument_list|(
name|bucketValue
argument_list|)
expr_stmt|;
block|}
return|return
name|res
operator|.
name|toString
argument_list|()
return|;
block|}
comment|//end toString()
annotation|@
name|Override
specifier|public
name|Object
name|clone
parameter_list|()
block|{
name|CountingBloomFilter
name|cbf
init|=
operator|new
name|CountingBloomFilter
argument_list|(
name|vectorSize
argument_list|,
name|nbHash
argument_list|,
name|hashType
argument_list|)
decl_stmt|;
name|cbf
operator|.
name|buckets
operator|=
name|this
operator|.
name|buckets
operator|.
name|clone
argument_list|()
expr_stmt|;
return|return
name|cbf
return|;
block|}
comment|//end clone()
comment|// Writable
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
name|super
operator|.
name|write
argument_list|(
name|out
argument_list|)
expr_stmt|;
name|int
name|sizeInWords
init|=
name|buckets2words
argument_list|(
name|vectorSize
argument_list|)
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
name|sizeInWords
condition|;
name|i
operator|++
control|)
block|{
name|out
operator|.
name|writeLong
argument_list|(
name|buckets
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|readFields
parameter_list|(
name|DataInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|super
operator|.
name|readFields
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|int
name|sizeInWords
init|=
name|buckets2words
argument_list|(
name|vectorSize
argument_list|)
decl_stmt|;
name|buckets
operator|=
operator|new
name|long
index|[
name|sizeInWords
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
name|sizeInWords
condition|;
name|i
operator|++
control|)
block|{
name|buckets
index|[
name|i
index|]
operator|=
name|in
operator|.
name|readLong
argument_list|()
expr_stmt|;
block|}
block|}
block|}
end_class

begin_comment
comment|//end class
end_comment

end_unit

