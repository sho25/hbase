begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  *  * Copyright (c) 2005, European Commission project OneLab under contract 034819 (http://www.one-lab.org)  * All rights reserved.  * Redistribution and use in source and binary forms, with or   * without modification, are permitted provided that the following   * conditions are met:  *  - Redistributions of source code must retain the above copyright   *    notice, this list of conditions and the following disclaimer.  *  - Redistributions in binary form must reproduce the above copyright   *    notice, this list of conditions and the following disclaimer in   *    the documentation and/or other materials provided with the distribution.  *  - Neither the name of the University Catholique de Louvain - UCL  *    nor the names of its contributors may be used to endorse or   *    promote products derived from this software without specific prior   *    written permission.  *      * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS   * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT   * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS   * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE   * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,   * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,   * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;   * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER   * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT   * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN   * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE   * POSSIBILITY OF SUCH DAMAGE.  */
end_comment

begin_package
package|package
name|org
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
name|ArrayList
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
name|Random
import|;
end_import

begin_comment
comment|/**  * Implements a<i>retouched Bloom filter</i>, as defined in the CoNEXT 2006 paper.  *<p>  * It allows the removal of selected false positives at the cost of introducing  * random false negatives, and with the benefit of eliminating some random false  * positives at the same time.  *   * @author<a href="mailto:donnet@ucl.ac.be">Benoit Donnet</a> - Universite Catholique de Louvain - Faculte des Sciences Appliquees - Departement d'Ingenierie Informatique.  * contract<a href="http://www.one-lab.org">European Commission One-Lab Project 034819</a>.  *  * @version 1.0 - 7 Feb. 07  *   * @see org.onelab.filter.Filter The general behavior of a filter  * @see org.onelab.filter.BloomFilter A Bloom filter  * @see org.onelab.filter.RemoveScheme The different selective clearing algorithms  *   * @see<a href="http://www-rp.lip6.fr/site_npa/site_rp/_publications/740-rbf_cameraready.pdf">Retouched Bloom Filters: Allowing Networked Applications to Trade Off Selected False Positives Against False Negatives</a>  */
end_comment

begin_class
specifier|public
specifier|final
class|class
name|RetouchedBloomFilter
extends|extends
name|BloomFilter
implements|implements
name|RemoveScheme
block|{
comment|/**    * KeyList vector (or ElementList Vector, as defined in the paper) of false positives.    */
name|ArrayList
argument_list|<
name|Key
argument_list|>
index|[]
name|fpVector
decl_stmt|;
comment|/**    * KeyList vector of keys recorded in the filter.    */
name|ArrayList
argument_list|<
name|Key
argument_list|>
index|[]
name|keyVector
decl_stmt|;
comment|/**    * Ratio vector.    */
name|double
index|[]
name|ratio
decl_stmt|;
specifier|private
name|Random
name|rand
decl_stmt|;
comment|/** Default constructor - use with readFields */
specifier|public
name|RetouchedBloomFilter
parameter_list|()
block|{}
comment|/**    * Constructor    * @param vectorSize The vector size of<i>this</i> filter.    * @param nbHash The number of hash function to consider.    */
specifier|public
name|RetouchedBloomFilter
parameter_list|(
name|int
name|vectorSize
parameter_list|,
name|int
name|nbHash
parameter_list|)
block|{
name|super
argument_list|(
name|vectorSize
argument_list|,
name|nbHash
argument_list|)
expr_stmt|;
name|this
operator|.
name|rand
operator|=
literal|null
expr_stmt|;
name|createVector
argument_list|()
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
name|vector
index|[
name|h
index|[
name|i
index|]
index|]
operator|=
literal|true
expr_stmt|;
name|keyVector
index|[
name|h
index|[
name|i
index|]
index|]
operator|.
name|add
argument_list|(
name|key
argument_list|)
expr_stmt|;
block|}
comment|//end for - i
block|}
comment|//end add()
comment|/**    * Adds a false positive information to<i>this</i> retouched Bloom filter.    *<p>    *<b>Invariant</b>: if the false positive is<code>null</code>, nothing happens.    * @param key The false positive key to add.    */
specifier|public
name|void
name|addFalsePositive
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
name|fpVector
index|[
name|h
index|[
name|i
index|]
index|]
operator|.
name|add
argument_list|(
name|key
argument_list|)
expr_stmt|;
block|}
block|}
comment|//end addFalsePositive()
comment|/**    * Adds a collection of false positive information to<i>this</i> retouched Bloom filter.    * @param coll The collection of false positive.    */
specifier|public
name|void
name|addFalsePositive
parameter_list|(
name|Collection
argument_list|<
name|Key
argument_list|>
name|coll
parameter_list|)
block|{
if|if
condition|(
name|coll
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|NullPointerException
argument_list|(
literal|"Collection<Key> can not be null"
argument_list|)
throw|;
block|}
for|for
control|(
name|Key
name|k
range|:
name|coll
control|)
block|{
name|addFalsePositive
argument_list|(
name|k
argument_list|)
expr_stmt|;
block|}
block|}
comment|//end addFalsePositive()
comment|/**    * Adds a list of false positive information to<i>this</i> retouched Bloom filter.    * @param keys The list of false positive.    */
specifier|public
name|void
name|addFalsePositive
parameter_list|(
name|ArrayList
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
name|NullPointerException
argument_list|(
literal|"ArrayList<Key> can not be null"
argument_list|)
throw|;
block|}
for|for
control|(
name|Key
name|k
range|:
name|keys
control|)
block|{
name|addFalsePositive
argument_list|(
name|k
argument_list|)
expr_stmt|;
block|}
block|}
comment|//end addFalsePositive()
comment|/**    * Adds an array of false positive information to<i>this</i> retouched Bloom filter.    * @param keys The array of false positive.    */
specifier|public
name|void
name|addFalsePositive
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
name|NullPointerException
argument_list|(
literal|"Key[] can not be null"
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
name|addFalsePositive
argument_list|(
name|keys
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
block|}
comment|//end addFalsePositive()
comment|/**    * Performs the selective clearing for a given key.    * @param k The false positive key to remove from<i>this</i> retouched Bloom filter.    * @param scheme The selective clearing scheme to apply.    */
specifier|public
name|void
name|selectiveClearing
parameter_list|(
name|Key
name|k
parameter_list|,
name|short
name|scheme
parameter_list|)
block|{
if|if
condition|(
name|k
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
if|if
condition|(
operator|!
name|membershipTest
argument_list|(
name|k
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
name|index
init|=
literal|0
decl_stmt|;
name|int
index|[]
name|h
init|=
name|hash
operator|.
name|hash
argument_list|(
name|k
argument_list|)
decl_stmt|;
switch|switch
condition|(
name|scheme
condition|)
block|{
case|case
name|RANDOM
case|:
name|index
operator|=
name|randomRemove
argument_list|()
expr_stmt|;
break|break;
case|case
name|MINIMUM_FN
case|:
name|index
operator|=
name|minimumFnRemove
argument_list|(
name|h
argument_list|)
expr_stmt|;
break|break;
case|case
name|MAXIMUM_FP
case|:
name|index
operator|=
name|maximumFpRemove
argument_list|(
name|h
argument_list|)
expr_stmt|;
break|break;
case|case
name|RATIO
case|:
name|index
operator|=
name|ratioRemove
argument_list|(
name|h
argument_list|)
expr_stmt|;
break|break;
default|default:
throw|throw
operator|new
name|AssertionError
argument_list|(
literal|"Undefined selective clearing scheme"
argument_list|)
throw|;
block|}
comment|//end switch
name|clearBit
argument_list|(
name|index
argument_list|)
expr_stmt|;
block|}
comment|//end selectiveClearing()
specifier|private
name|int
name|randomRemove
parameter_list|()
block|{
if|if
condition|(
name|rand
operator|==
literal|null
condition|)
block|{
name|rand
operator|=
operator|new
name|Random
argument_list|()
expr_stmt|;
block|}
return|return
name|rand
operator|.
name|nextInt
argument_list|(
name|nbHash
argument_list|)
return|;
block|}
comment|//end randomRemove()
comment|/**    * Chooses the bit position that minimizes the number of false negative generated.    * @param h The different bit positions.    * @return int The position that minimizes the number of false negative generated.    */
specifier|private
name|int
name|minimumFnRemove
parameter_list|(
name|int
index|[]
name|h
parameter_list|)
block|{
name|int
name|minIndex
init|=
name|Integer
operator|.
name|MAX_VALUE
decl_stmt|;
name|double
name|minValue
init|=
name|Double
operator|.
name|MAX_VALUE
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
name|nbHash
condition|;
name|i
operator|++
control|)
block|{
name|double
name|keyWeight
init|=
name|getWeight
argument_list|(
name|keyVector
index|[
name|h
index|[
name|i
index|]
index|]
argument_list|)
decl_stmt|;
if|if
condition|(
name|keyWeight
operator|<
name|minValue
condition|)
block|{
name|minIndex
operator|=
name|h
index|[
name|i
index|]
expr_stmt|;
name|minValue
operator|=
name|keyWeight
expr_stmt|;
block|}
block|}
comment|//end for - i
return|return
name|minIndex
return|;
block|}
comment|//end minimumFnRemove()
comment|/**    * Chooses the bit position that maximizes the number of false positive removed.    * @param h The different bit positions.    * @return int The position that maximizes the number of false positive removed.    */
specifier|private
name|int
name|maximumFpRemove
parameter_list|(
name|int
index|[]
name|h
parameter_list|)
block|{
name|int
name|maxIndex
init|=
name|Integer
operator|.
name|MIN_VALUE
decl_stmt|;
name|double
name|maxValue
init|=
name|Double
operator|.
name|MIN_VALUE
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
name|nbHash
condition|;
name|i
operator|++
control|)
block|{
name|double
name|fpWeight
init|=
name|getWeight
argument_list|(
name|fpVector
index|[
name|h
index|[
name|i
index|]
index|]
argument_list|)
decl_stmt|;
if|if
condition|(
name|fpWeight
operator|>
name|maxValue
condition|)
block|{
name|maxValue
operator|=
name|fpWeight
expr_stmt|;
name|maxIndex
operator|=
name|h
index|[
name|i
index|]
expr_stmt|;
block|}
block|}
return|return
name|maxIndex
return|;
block|}
comment|//end maximumFpRemove()
comment|/**    * Chooses the bit position that minimizes the number of false negative generated while maximizing.    * the number of false positive removed.    * @param h The different bit positions.    * @return int The position that minimizes the number of false negative generated while maximizing.    */
specifier|private
name|int
name|ratioRemove
parameter_list|(
name|int
index|[]
name|h
parameter_list|)
block|{
name|computeRatio
argument_list|()
expr_stmt|;
name|int
name|minIndex
init|=
name|Integer
operator|.
name|MAX_VALUE
decl_stmt|;
name|double
name|minValue
init|=
name|Double
operator|.
name|MAX_VALUE
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
name|nbHash
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|ratio
index|[
name|h
index|[
name|i
index|]
index|]
operator|<
name|minValue
condition|)
block|{
name|minValue
operator|=
name|ratio
index|[
name|h
index|[
name|i
index|]
index|]
expr_stmt|;
name|minIndex
operator|=
name|h
index|[
name|i
index|]
expr_stmt|;
block|}
block|}
comment|//end for - i
return|return
name|minIndex
return|;
block|}
comment|//end ratioRemove()
comment|/**    * Clears a specified bit in the bit vector and keeps up-to-date the KeyList vectors.    * @param index The position of the bit to clear.    */
specifier|private
name|void
name|clearBit
parameter_list|(
name|int
name|index
parameter_list|)
block|{
if|if
condition|(
name|index
operator|<
literal|0
operator|||
name|index
operator|>=
name|vectorSize
condition|)
block|{
throw|throw
operator|new
name|ArrayIndexOutOfBoundsException
argument_list|(
name|index
argument_list|)
throw|;
block|}
name|ArrayList
argument_list|<
name|Key
argument_list|>
name|kl
init|=
name|keyVector
index|[
name|index
index|]
decl_stmt|;
name|ArrayList
argument_list|<
name|Key
argument_list|>
name|fpl
init|=
name|fpVector
index|[
name|index
index|]
decl_stmt|;
comment|// update key list
name|int
name|listSize
init|=
name|kl
operator|.
name|size
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
name|listSize
operator|&&
operator|!
name|kl
operator|.
name|isEmpty
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|removeKey
argument_list|(
name|kl
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|,
name|keyVector
argument_list|)
expr_stmt|;
block|}
name|kl
operator|.
name|clear
argument_list|()
expr_stmt|;
name|keyVector
index|[
name|index
index|]
operator|.
name|clear
argument_list|()
expr_stmt|;
comment|//update false positive list
name|listSize
operator|=
name|fpl
operator|.
name|size
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
name|listSize
operator|&&
operator|!
name|fpl
operator|.
name|isEmpty
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|removeKey
argument_list|(
name|fpl
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|,
name|fpVector
argument_list|)
expr_stmt|;
block|}
name|fpl
operator|.
name|clear
argument_list|()
expr_stmt|;
name|fpVector
index|[
name|index
index|]
operator|.
name|clear
argument_list|()
expr_stmt|;
comment|//update ratio
name|ratio
index|[
name|index
index|]
operator|=
literal|0.0
expr_stmt|;
comment|//update bit vector
name|vector
index|[
name|index
index|]
operator|=
literal|false
expr_stmt|;
block|}
comment|//end clearBit()
comment|/**    * Removes a given key from<i>this</i> filer.    * @param k The key to remove.    * @param vector The counting vector associated to the key.    */
specifier|private
name|void
name|removeKey
parameter_list|(
name|Key
name|k
parameter_list|,
name|ArrayList
argument_list|<
name|Key
argument_list|>
index|[]
name|vector
parameter_list|)
block|{
if|if
condition|(
name|k
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
if|if
condition|(
name|vector
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|NullPointerException
argument_list|(
literal|"ArrayList<Key>[] can not be null"
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
name|k
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
name|nbHash
condition|;
name|i
operator|++
control|)
block|{
name|vector
index|[
name|h
index|[
name|i
index|]
index|]
operator|.
name|remove
argument_list|(
name|k
argument_list|)
expr_stmt|;
block|}
block|}
comment|//end removeKey()
comment|/**    * Computes the ratio A/FP.    */
specifier|private
name|void
name|computeRatio
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
name|vectorSize
condition|;
name|i
operator|++
control|)
block|{
name|double
name|keyWeight
init|=
name|getWeight
argument_list|(
name|keyVector
index|[
name|i
index|]
argument_list|)
decl_stmt|;
name|double
name|fpWeight
init|=
name|getWeight
argument_list|(
name|fpVector
index|[
name|i
index|]
argument_list|)
decl_stmt|;
if|if
condition|(
name|keyWeight
operator|>
literal|0
operator|&&
name|fpWeight
operator|>
literal|0
condition|)
block|{
name|ratio
index|[
name|i
index|]
operator|=
name|keyWeight
operator|/
name|fpWeight
expr_stmt|;
block|}
block|}
comment|//end for - i
block|}
comment|//end computeRatio()
specifier|private
name|double
name|getWeight
parameter_list|(
name|ArrayList
argument_list|<
name|Key
argument_list|>
name|keyList
parameter_list|)
block|{
name|double
name|weight
init|=
literal|0.0
decl_stmt|;
for|for
control|(
name|Key
name|k
range|:
name|keyList
control|)
block|{
name|weight
operator|+=
name|k
operator|.
name|getWeight
argument_list|()
expr_stmt|;
block|}
return|return
name|weight
return|;
block|}
comment|/**    * Creates and initialises the various vectors.    */
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
specifier|private
name|void
name|createVector
parameter_list|()
block|{
name|fpVector
operator|=
operator|new
name|ArrayList
index|[
name|vectorSize
index|]
expr_stmt|;
name|keyVector
operator|=
operator|new
name|ArrayList
index|[
name|vectorSize
index|]
expr_stmt|;
name|ratio
operator|=
operator|new
name|double
index|[
name|vectorSize
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
name|vectorSize
condition|;
name|i
operator|++
control|)
block|{
name|fpVector
index|[
name|i
index|]
operator|=
operator|new
name|ArrayList
argument_list|<
name|Key
argument_list|>
argument_list|()
expr_stmt|;
name|keyVector
index|[
name|i
index|]
operator|=
operator|new
name|ArrayList
argument_list|<
name|Key
argument_list|>
argument_list|()
expr_stmt|;
name|ratio
index|[
name|i
index|]
operator|=
literal|0.0
expr_stmt|;
block|}
comment|//end for -i
block|}
comment|//end createVector()
annotation|@
name|Override
specifier|public
name|boolean
name|equals
parameter_list|(
name|Object
name|o
parameter_list|)
block|{
return|return
name|this
operator|.
name|compareTo
argument_list|(
name|o
argument_list|)
operator|==
literal|0
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
name|int
name|result
init|=
name|super
operator|.
name|hashCode
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
name|fpVector
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|result
operator|^=
name|fpVector
index|[
name|i
index|]
operator|.
name|hashCode
argument_list|()
expr_stmt|;
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
name|keyVector
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|result
operator|^=
name|keyVector
index|[
name|i
index|]
operator|.
name|hashCode
argument_list|()
expr_stmt|;
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
name|ratio
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|result
operator|^=
name|Double
operator|.
name|valueOf
argument_list|(
name|ratio
index|[
name|i
index|]
argument_list|)
operator|.
name|hashCode
argument_list|()
expr_stmt|;
block|}
return|return
name|result
return|;
block|}
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
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|fpVector
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|ArrayList
argument_list|<
name|Key
argument_list|>
name|list
init|=
name|fpVector
index|[
name|i
index|]
decl_stmt|;
name|out
operator|.
name|writeInt
argument_list|(
name|list
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|Key
name|k
range|:
name|list
control|)
block|{
name|k
operator|.
name|write
argument_list|(
name|out
argument_list|)
expr_stmt|;
block|}
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
name|keyVector
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|ArrayList
argument_list|<
name|Key
argument_list|>
name|list
init|=
name|keyVector
index|[
name|i
index|]
decl_stmt|;
name|out
operator|.
name|writeInt
argument_list|(
name|list
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|Key
name|k
range|:
name|list
control|)
block|{
name|k
operator|.
name|write
argument_list|(
name|out
argument_list|)
expr_stmt|;
block|}
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
name|ratio
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|out
operator|.
name|writeDouble
argument_list|(
name|ratio
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
name|createVector
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
name|fpVector
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|ArrayList
argument_list|<
name|Key
argument_list|>
name|list
init|=
name|fpVector
index|[
name|i
index|]
decl_stmt|;
name|int
name|size
init|=
name|in
operator|.
name|readInt
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
name|size
condition|;
name|j
operator|++
control|)
block|{
name|Key
name|k
init|=
operator|new
name|Key
argument_list|()
decl_stmt|;
name|k
operator|.
name|readFields
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|list
operator|.
name|add
argument_list|(
name|k
argument_list|)
expr_stmt|;
block|}
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
name|keyVector
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|ArrayList
argument_list|<
name|Key
argument_list|>
name|list
init|=
name|keyVector
index|[
name|i
index|]
decl_stmt|;
name|int
name|size
init|=
name|in
operator|.
name|readInt
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
name|size
condition|;
name|j
operator|++
control|)
block|{
name|Key
name|k
init|=
operator|new
name|Key
argument_list|()
decl_stmt|;
name|k
operator|.
name|readFields
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|list
operator|.
name|add
argument_list|(
name|k
argument_list|)
expr_stmt|;
block|}
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
name|ratio
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|ratio
index|[
name|i
index|]
operator|=
name|in
operator|.
name|readDouble
argument_list|()
expr_stmt|;
block|}
block|}
comment|// Comparable
annotation|@
name|Override
specifier|public
name|int
name|compareTo
parameter_list|(
name|Object
name|o
parameter_list|)
block|{
name|int
name|result
init|=
name|super
operator|.
name|compareTo
argument_list|(
name|o
argument_list|)
decl_stmt|;
name|RetouchedBloomFilter
name|other
init|=
operator|(
name|RetouchedBloomFilter
operator|)
name|o
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|result
operator|==
literal|0
operator|&&
name|i
operator|<
name|fpVector
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|ArrayList
argument_list|<
name|Key
argument_list|>
name|mylist
init|=
name|fpVector
index|[
name|i
index|]
decl_stmt|;
name|ArrayList
argument_list|<
name|Key
argument_list|>
name|otherlist
init|=
name|other
operator|.
name|fpVector
index|[
name|i
index|]
decl_stmt|;
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|result
operator|==
literal|0
operator|&&
name|j
operator|<
name|mylist
operator|.
name|size
argument_list|()
condition|;
name|j
operator|++
control|)
block|{
name|result
operator|=
name|mylist
operator|.
name|get
argument_list|(
name|j
argument_list|)
operator|.
name|compareTo
argument_list|(
name|otherlist
operator|.
name|get
argument_list|(
name|j
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|result
operator|==
literal|0
operator|&&
name|i
operator|<
name|keyVector
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|ArrayList
argument_list|<
name|Key
argument_list|>
name|mylist
init|=
name|keyVector
index|[
name|i
index|]
decl_stmt|;
name|ArrayList
argument_list|<
name|Key
argument_list|>
name|otherlist
init|=
name|other
operator|.
name|keyVector
index|[
name|i
index|]
decl_stmt|;
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|result
operator|==
literal|0
operator|&&
name|j
operator|<
name|mylist
operator|.
name|size
argument_list|()
condition|;
name|j
operator|++
control|)
block|{
name|result
operator|=
name|mylist
operator|.
name|get
argument_list|(
name|j
argument_list|)
operator|.
name|compareTo
argument_list|(
name|otherlist
operator|.
name|get
argument_list|(
name|j
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|result
operator|==
literal|0
operator|&&
name|i
operator|<
name|ratio
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|result
operator|=
name|Double
operator|.
name|valueOf
argument_list|(
name|this
operator|.
name|ratio
index|[
name|i
index|]
operator|-
name|other
operator|.
name|ratio
index|[
name|i
index|]
argument_list|)
operator|.
name|intValue
argument_list|()
expr_stmt|;
block|}
return|return
name|result
return|;
block|}
comment|// end compareTo
block|}
end_class

begin_comment
comment|//end class
end_comment

end_unit

