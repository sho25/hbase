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
comment|/**  * Implements a<i>dynamic Bloom filter</i>, as defined in the INFOCOM 2006 paper.  *<p>  * A dynamic Bloom filter (DBF) makes use of a<code>s * m</code> bit matrix but  * each of the<code>s</code> rows is a standard Bloom filter. The creation   * process of a DBF is iterative. At the start, the DBF is a<code>1 * m</code>  * bit matrix, i.e., it is composed of a single standard Bloom filter.  * It assumes that<code>n<sub>r</sub></code> elements are recorded in the   * initial bit vector, where<code>n<sub>r</sub><= n</code> (<code>n</code> is  * the cardinality of the set<code>A</code> to record in the filter).    *<p>  * As the size of<code>A</code> grows during the execution of the application,  * several keys must be inserted in the DBF.  When inserting a key into the DBF,  * one must first get an active Bloom filter in the matrix.  A Bloom filter is  * active when the number of recorded keys,<code>n<sub>r</sub></code>, is   * strictly less than the current cardinality of<code>A</code>,<code>n</code>.  * If an active Bloom filter is found, the key is inserted and   *<code>n<sub>r</sub></code> is incremented by one. On the other hand, if there  * is no active Bloom filter, a new one is created (i.e., a new row is added to  * the matrix) according to the current size of<code>A</code> and the element  * is added in this new Bloom filter and the<code>n<sub>r</sub></code> value of  * this new Bloom filter is set to one.  A given key is said to belong to the  * DBF if the<code>k</code> positions are set to one in one of the matrix rows.  *   * contract<a href="http://www.one-lab.org">European Commission One-Lab Project 034819</a>.  *  * @version 1.0 - 6 Feb. 07  *   */
end_comment

begin_class
specifier|public
class|class
name|DynamicBloomFilter
extends|extends
name|Filter
block|{
comment|/**     * Threshold for the maximum number of key to record in a dynamic Bloom filter row.    */
specifier|private
name|int
name|nr
decl_stmt|;
comment|/**    * The number of keys recorded in the current standard active Bloom filter.    */
specifier|private
name|int
name|currentNbRecord
decl_stmt|;
comment|/**    * The matrix of Bloom filter.    */
specifier|private
name|BloomFilter
index|[]
name|matrix
decl_stmt|;
comment|/**    * Zero-args constructor for the serialization.    */
specifier|public
name|DynamicBloomFilter
parameter_list|()
block|{ }
comment|/**    * Constructor.    *<p>    * Builds an empty Dynamic Bloom filter.    * @param vectorSize The number of bits in the vector.    * @param nbHash The number of hash function to consider.    * @param hashType type of the hashing function (see {@link Hash}).    * @param nr The threshold for the maximum number of keys to record in a dynamic Bloom filter row.    */
specifier|public
name|DynamicBloomFilter
parameter_list|(
name|int
name|vectorSize
parameter_list|,
name|int
name|nbHash
parameter_list|,
name|int
name|hashType
parameter_list|,
name|int
name|nr
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
name|this
operator|.
name|nr
operator|=
name|nr
expr_stmt|;
name|this
operator|.
name|currentNbRecord
operator|=
literal|0
expr_stmt|;
name|matrix
operator|=
operator|new
name|BloomFilter
index|[
literal|1
index|]
expr_stmt|;
name|matrix
index|[
literal|0
index|]
operator|=
operator|new
name|BloomFilter
argument_list|(
name|this
operator|.
name|vectorSize
argument_list|,
name|this
operator|.
name|nbHash
argument_list|,
name|this
operator|.
name|hashType
argument_list|)
expr_stmt|;
block|}
comment|//end constructor
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
literal|"Key can not be null"
argument_list|)
throw|;
block|}
name|BloomFilter
name|bf
init|=
name|getActiveStandardBF
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
name|currentNbRecord
operator|=
literal|0
expr_stmt|;
block|}
name|bf
operator|.
name|add
argument_list|(
name|key
argument_list|)
expr_stmt|;
name|currentNbRecord
operator|++
expr_stmt|;
block|}
comment|//end add()
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
name|DynamicBloomFilter
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
name|DynamicBloomFilter
name|dbf
init|=
operator|(
name|DynamicBloomFilter
operator|)
name|filter
decl_stmt|;
if|if
condition|(
name|dbf
operator|.
name|matrix
operator|.
name|length
operator|!=
name|this
operator|.
name|matrix
operator|.
name|length
operator|||
name|dbf
operator|.
name|nr
operator|!=
name|this
operator|.
name|nr
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
name|matrix
index|[
name|i
index|]
operator|.
name|and
argument_list|(
name|dbf
operator|.
name|matrix
index|[
name|i
index|]
argument_list|)
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
return|return
literal|true
return|;
block|}
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
name|membershipTest
argument_list|(
name|key
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
comment|//end membershipTest()
annotation|@
name|Override
specifier|public
name|void
name|not
parameter_list|()
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
name|matrix
index|[
name|i
index|]
operator|.
name|not
argument_list|()
expr_stmt|;
block|}
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
name|DynamicBloomFilter
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
name|DynamicBloomFilter
name|dbf
init|=
operator|(
name|DynamicBloomFilter
operator|)
name|filter
decl_stmt|;
if|if
condition|(
name|dbf
operator|.
name|matrix
operator|.
name|length
operator|!=
name|this
operator|.
name|matrix
operator|.
name|length
operator|||
name|dbf
operator|.
name|nr
operator|!=
name|this
operator|.
name|nr
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
name|matrix
index|[
name|i
index|]
operator|.
name|or
argument_list|(
name|dbf
operator|.
name|matrix
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
block|}
comment|//end or()
annotation|@
name|Override
specifier|public
name|void
name|xor
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
name|DynamicBloomFilter
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
literal|"filters cannot be xor-ed"
argument_list|)
throw|;
block|}
name|DynamicBloomFilter
name|dbf
init|=
operator|(
name|DynamicBloomFilter
operator|)
name|filter
decl_stmt|;
if|if
condition|(
name|dbf
operator|.
name|matrix
operator|.
name|length
operator|!=
name|this
operator|.
name|matrix
operator|.
name|length
operator|||
name|dbf
operator|.
name|nr
operator|!=
name|this
operator|.
name|nr
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"filters cannot be xor-ed"
argument_list|)
throw|;
block|}
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
name|matrix
index|[
name|i
index|]
operator|.
name|xor
argument_list|(
name|dbf
operator|.
name|matrix
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
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
name|matrix
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|res
operator|.
name|append
argument_list|(
name|matrix
index|[
name|i
index|]
argument_list|)
expr_stmt|;
name|res
operator|.
name|append
argument_list|(
name|Character
operator|.
name|LINE_SEPARATOR
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
name|DynamicBloomFilter
name|dbf
init|=
operator|new
name|DynamicBloomFilter
argument_list|(
name|vectorSize
argument_list|,
name|nbHash
argument_list|,
name|hashType
argument_list|,
name|nr
argument_list|)
decl_stmt|;
name|dbf
operator|.
name|currentNbRecord
operator|=
name|this
operator|.
name|currentNbRecord
expr_stmt|;
name|dbf
operator|.
name|matrix
operator|=
operator|new
name|BloomFilter
index|[
name|this
operator|.
name|matrix
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
name|this
operator|.
name|matrix
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|dbf
operator|.
name|matrix
index|[
name|i
index|]
operator|=
operator|(
name|BloomFilter
operator|)
name|this
operator|.
name|matrix
index|[
name|i
index|]
operator|.
name|clone
argument_list|()
expr_stmt|;
block|}
return|return
name|dbf
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
name|out
operator|.
name|writeInt
argument_list|(
name|nr
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeInt
argument_list|(
name|currentNbRecord
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
name|matrix
index|[
name|i
index|]
operator|.
name|write
argument_list|(
name|out
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
name|nr
operator|=
name|in
operator|.
name|readInt
argument_list|()
expr_stmt|;
name|currentNbRecord
operator|=
name|in
operator|.
name|readInt
argument_list|()
expr_stmt|;
name|int
name|len
init|=
name|in
operator|.
name|readInt
argument_list|()
decl_stmt|;
name|matrix
operator|=
operator|new
name|BloomFilter
index|[
name|len
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
name|matrix
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|matrix
index|[
name|i
index|]
operator|=
operator|new
name|BloomFilter
argument_list|()
expr_stmt|;
name|matrix
index|[
name|i
index|]
operator|.
name|readFields
argument_list|(
name|in
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Adds a new row to<i>this</i> dynamic Bloom filter.    */
specifier|private
name|void
name|addRow
parameter_list|()
block|{
name|BloomFilter
index|[]
name|tmp
init|=
operator|new
name|BloomFilter
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
operator|(
name|BloomFilter
operator|)
name|matrix
index|[
name|i
index|]
operator|.
name|clone
argument_list|()
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
name|BloomFilter
argument_list|(
name|vectorSize
argument_list|,
name|nbHash
argument_list|,
name|hashType
argument_list|)
expr_stmt|;
name|matrix
operator|=
name|tmp
expr_stmt|;
block|}
comment|//end addRow()
comment|/**    * Returns the active standard Bloom filter in<i>this</i> dynamic Bloom filter.    * @return BloomFilter The active standard Bloom filter.    *<code>Null</code> otherwise.    */
specifier|private
name|BloomFilter
name|getActiveStandardBF
parameter_list|()
block|{
if|if
condition|(
name|currentNbRecord
operator|>=
name|nr
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
comment|//end getActiveStandardBF()
block|}
end_class

begin_comment
comment|//end class
end_comment

end_unit

