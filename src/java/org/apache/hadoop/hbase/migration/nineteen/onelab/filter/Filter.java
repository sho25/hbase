begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  *  * Copyright (c) 2005, European Commission project OneLab under contract 034819  * (http://www.one-lab.org)  *   * All rights reserved.  * Redistribution and use in source and binary forms, with or   * without modification, are permitted provided that the following   * conditions are met:  *  - Redistributions of source code must retain the above copyright   *    notice, this list of conditions and the following disclaimer.  *  - Redistributions in binary form must reproduce the above copyright   *    notice, this list of conditions and the following disclaimer in   *    the documentation and/or other materials provided with the distribution.  *  - Neither the name of the University Catholique de Louvain - UCL  *    nor the names of its contributors may be used to endorse or   *    promote products derived from this software without specific prior   *    written permission.  *      * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS   * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT   * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS   * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE   * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,   * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,   * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;   * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER   * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT   * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN   * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE   * POSSIBILITY OF SUCH DAMAGE.  */
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
name|Collection
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
name|io
operator|.
name|Writable
import|;
end_import

begin_comment
comment|/**  * Defines the general behavior of a filter.  *<p>  * A filter is a data structure which aims at offering a lossy summary of a set<code>A</code>.  The  * key idea is to map entries of<code>A</code> (also called<i>keys</i>) into several positions   * in a vector through the use of several hash functions.  *<p>  * Typically, a filter will be implemented as a Bloom filter (or a Bloom filter extension).  *<p>  * It must be extended in order to define the real behavior.  *   * @see org.onelab.filter.Filter The general behavior of a filter  *  * @version 1.0 - 2 Feb. 07  *   * @see org.onelab.filter.Key The general behavior of a key  * @see org.onelab.filter.HashFunction A hash function  */
end_comment

begin_class
specifier|public
specifier|abstract
class|class
name|Filter
implements|implements
name|Writable
block|{
specifier|private
specifier|static
specifier|final
name|int
name|VERSION
init|=
operator|-
literal|1
decl_stmt|;
comment|// negative to accommodate for old format
comment|/** The vector size of<i>this</i> filter. */
specifier|protected
name|int
name|vectorSize
decl_stmt|;
comment|/** The hash function used to map a key to several positions in the vector. */
specifier|protected
name|HashFunction
name|hash
decl_stmt|;
comment|/** The number of hash function to consider. */
specifier|protected
name|int
name|nbHash
decl_stmt|;
comment|/** Type of hashing function to use. */
specifier|protected
name|int
name|hashType
decl_stmt|;
specifier|protected
name|Filter
parameter_list|()
block|{}
comment|/**     * Constructor.    * @param vectorSize The vector size of<i>this</i> filter.    * @param nbHash The number of hash functions to consider.    * @param hashType type of the hashing function (see {@link Hash}).    */
specifier|protected
name|Filter
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
name|this
operator|.
name|vectorSize
operator|=
name|vectorSize
expr_stmt|;
name|this
operator|.
name|nbHash
operator|=
name|nbHash
expr_stmt|;
name|this
operator|.
name|hashType
operator|=
name|hashType
expr_stmt|;
name|this
operator|.
name|hash
operator|=
operator|new
name|HashFunction
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
comment|/**    * Adds a key to<i>this</i> filter.    * @param key The key to add.    */
specifier|public
specifier|abstract
name|void
name|add
parameter_list|(
name|Key
name|key
parameter_list|)
function_decl|;
comment|/**    * Determines wether a specified key belongs to<i>this</i> filter.    * @param key The key to test.    * @return boolean True if the specified key belongs to<i>this</i> filter.    * 		     False otherwise.    */
specifier|public
specifier|abstract
name|boolean
name|membershipTest
parameter_list|(
name|Key
name|key
parameter_list|)
function_decl|;
comment|/**    * Peforms a logical AND between<i>this</i> filter and a specified filter.    *<p>    *<b>Invariant</b>: The result is assigned to<i>this</i> filter.    * @param filter The filter to AND with.    */
specifier|public
specifier|abstract
name|void
name|and
parameter_list|(
name|Filter
name|filter
parameter_list|)
function_decl|;
comment|/**    * Peforms a logical OR between<i>this</i> filter and a specified filter.    *<p>    *<b>Invariant</b>: The result is assigned to<i>this</i> filter.    * @param filter The filter to OR with.    */
specifier|public
specifier|abstract
name|void
name|or
parameter_list|(
name|Filter
name|filter
parameter_list|)
function_decl|;
comment|/**    * Peforms a logical XOR between<i>this</i> filter and a specified filter.    *<p>    *<b>Invariant</b>: The result is assigned to<i>this</i> filter.    * @param filter The filter to XOR with.    */
specifier|public
specifier|abstract
name|void
name|xor
parameter_list|(
name|Filter
name|filter
parameter_list|)
function_decl|;
comment|/**    * Performs a logical NOT on<i>this</i> filter.    *<p>    * The result is assigned to<i>this</i> filter.    */
specifier|public
specifier|abstract
name|void
name|not
parameter_list|()
function_decl|;
comment|/**    * Adds a list of keys to<i>this</i> filter.    * @param keys The list of keys.    */
specifier|public
name|void
name|add
parameter_list|(
name|List
argument_list|<
name|Key
argument_list|>
name|keys
parameter_list|)
block|{
if|if
condition|(
name|keys
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"ArrayList<Key> may not be null"
argument_list|)
throw|;
block|}
for|for
control|(
name|Key
name|key
range|:
name|keys
control|)
block|{
name|add
argument_list|(
name|key
argument_list|)
expr_stmt|;
block|}
block|}
comment|//end add()
comment|/**    * Adds a collection of keys to<i>this</i> filter.    * @param keys The collection of keys.    */
specifier|public
name|void
name|add
parameter_list|(
name|Collection
argument_list|<
name|Key
argument_list|>
name|keys
parameter_list|)
block|{
if|if
condition|(
name|keys
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Collection<Key> may not be null"
argument_list|)
throw|;
block|}
for|for
control|(
name|Key
name|key
range|:
name|keys
control|)
block|{
name|add
argument_list|(
name|key
argument_list|)
expr_stmt|;
block|}
block|}
comment|//end add()
comment|/**    * Adds an array of keys to<i>this</i> filter.    * @param keys The array of keys.    */
specifier|public
name|void
name|add
parameter_list|(
name|Key
index|[]
name|keys
parameter_list|)
block|{
if|if
condition|(
name|keys
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Key[] may not be null"
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
name|keys
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|add
argument_list|(
name|keys
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
block|}
comment|//end add()
comment|// Writable interface
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
name|this
operator|.
name|nbHash
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeByte
argument_list|(
name|this
operator|.
name|hashType
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeInt
argument_list|(
name|this
operator|.
name|vectorSize
argument_list|)
expr_stmt|;
block|}
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
name|int
name|ver
init|=
name|in
operator|.
name|readInt
argument_list|()
decl_stmt|;
if|if
condition|(
name|ver
operator|>
literal|0
condition|)
block|{
comment|// old unversioned format
name|this
operator|.
name|nbHash
operator|=
name|ver
expr_stmt|;
name|this
operator|.
name|hashType
operator|=
name|Hash
operator|.
name|JENKINS_HASH
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|ver
operator|==
name|VERSION
condition|)
block|{
name|this
operator|.
name|nbHash
operator|=
name|in
operator|.
name|readInt
argument_list|()
expr_stmt|;
name|this
operator|.
name|hashType
operator|=
name|in
operator|.
name|readByte
argument_list|()
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Unsupported version: "
operator|+
name|ver
argument_list|)
throw|;
block|}
name|this
operator|.
name|vectorSize
operator|=
name|in
operator|.
name|readInt
argument_list|()
expr_stmt|;
name|this
operator|.
name|hash
operator|=
operator|new
name|HashFunction
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
block|}
end_class

begin_comment
comment|//end class
end_comment

end_unit

