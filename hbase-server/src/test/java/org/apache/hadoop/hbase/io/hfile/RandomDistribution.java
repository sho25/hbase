begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one or more  * contributor license agreements. See the NOTICE file distributed with this  * work for additional information regarding copyright ownership. The ASF  * licenses this file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the  * License for the specific language governing permissions and limitations under  * the License.  */
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
name|hfile
package|;
end_package

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
name|Arrays
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collections
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
comment|/**  * A class that generates random numbers that follow some distribution.  *<p>  * Copied from  *<a href="https://issues.apache.org/jira/browse/HADOOP-3315">hadoop-3315 tfile</a>.  * Remove after tfile is committed and use the tfile version of this class  * instead.</p>  */
end_comment

begin_class
specifier|public
class|class
name|RandomDistribution
block|{
comment|/**    * Interface for discrete (integer) random distributions.    */
specifier|public
interface|interface
name|DiscreteRNG
block|{
comment|/**      * Get the next random number      *      * @return the next random number.      */
name|int
name|nextInt
parameter_list|()
function_decl|;
block|}
comment|/**    * P(i)=1/(max-min)    */
specifier|public
specifier|static
specifier|final
class|class
name|Flat
implements|implements
name|DiscreteRNG
block|{
specifier|private
specifier|final
name|Random
name|random
decl_stmt|;
specifier|private
specifier|final
name|int
name|min
decl_stmt|;
specifier|private
specifier|final
name|int
name|max
decl_stmt|;
comment|/**      * Generate random integers from min (inclusive) to max (exclusive)      * following even distribution.      *      * @param random      *          The basic random number generator.      * @param min      *          Minimum integer      * @param max      *          maximum integer (exclusive).      *      */
specifier|public
name|Flat
parameter_list|(
name|Random
name|random
parameter_list|,
name|int
name|min
parameter_list|,
name|int
name|max
parameter_list|)
block|{
if|if
condition|(
name|min
operator|>=
name|max
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Invalid range"
argument_list|)
throw|;
block|}
name|this
operator|.
name|random
operator|=
name|random
expr_stmt|;
name|this
operator|.
name|min
operator|=
name|min
expr_stmt|;
name|this
operator|.
name|max
operator|=
name|max
expr_stmt|;
block|}
comment|/**      * @see DiscreteRNG#nextInt()      */
annotation|@
name|Override
specifier|public
name|int
name|nextInt
parameter_list|()
block|{
return|return
name|random
operator|.
name|nextInt
argument_list|(
name|max
operator|-
name|min
argument_list|)
operator|+
name|min
return|;
block|}
block|}
comment|/**    * Zipf distribution. The ratio of the probabilities of integer i and j is    * defined as follows:    *    * P(i)/P(j)=((j-min+1)/(i-min+1))^sigma.    */
specifier|public
specifier|static
specifier|final
class|class
name|Zipf
implements|implements
name|DiscreteRNG
block|{
specifier|private
specifier|static
specifier|final
name|double
name|DEFAULT_EPSILON
init|=
literal|0.001
decl_stmt|;
specifier|private
specifier|final
name|Random
name|random
decl_stmt|;
specifier|private
specifier|final
name|ArrayList
argument_list|<
name|Integer
argument_list|>
name|k
decl_stmt|;
specifier|private
specifier|final
name|ArrayList
argument_list|<
name|Double
argument_list|>
name|v
decl_stmt|;
comment|/**      * Constructor      *      * @param r      *          The random number generator.      * @param min      *          minimum integer (inclusvie)      * @param max      *          maximum integer (exclusive)      * @param sigma      *          parameter sigma. (sigma> 1.0)      */
specifier|public
name|Zipf
parameter_list|(
name|Random
name|r
parameter_list|,
name|int
name|min
parameter_list|,
name|int
name|max
parameter_list|,
name|double
name|sigma
parameter_list|)
block|{
name|this
argument_list|(
name|r
argument_list|,
name|min
argument_list|,
name|max
argument_list|,
name|sigma
argument_list|,
name|DEFAULT_EPSILON
argument_list|)
expr_stmt|;
block|}
comment|/**      * Constructor.      *      * @param r      *          The random number generator.      * @param min      *          minimum integer (inclusvie)      * @param max      *          maximum integer (exclusive)      * @param sigma      *          parameter sigma. (sigma> 1.0)      * @param epsilon      *          Allowable error percentage (0< epsilon< 1.0).      */
specifier|public
name|Zipf
parameter_list|(
name|Random
name|r
parameter_list|,
name|int
name|min
parameter_list|,
name|int
name|max
parameter_list|,
name|double
name|sigma
parameter_list|,
name|double
name|epsilon
parameter_list|)
block|{
if|if
condition|(
operator|(
name|max
operator|<=
name|min
operator|)
operator|||
operator|(
name|sigma
operator|<=
literal|1
operator|)
operator|||
operator|(
name|epsilon
operator|<=
literal|0
operator|)
operator|||
operator|(
name|epsilon
operator|>=
literal|0.5
operator|)
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Invalid arguments"
argument_list|)
throw|;
block|}
name|random
operator|=
name|r
expr_stmt|;
name|k
operator|=
operator|new
name|ArrayList
argument_list|<
name|Integer
argument_list|>
argument_list|()
expr_stmt|;
name|v
operator|=
operator|new
name|ArrayList
argument_list|<
name|Double
argument_list|>
argument_list|()
expr_stmt|;
name|double
name|sum
init|=
literal|0
decl_stmt|;
name|int
name|last
init|=
operator|-
literal|1
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
name|min
init|;
name|i
operator|<
name|max
condition|;
operator|++
name|i
control|)
block|{
name|sum
operator|+=
name|Math
operator|.
name|exp
argument_list|(
operator|-
name|sigma
operator|*
name|Math
operator|.
name|log
argument_list|(
name|i
operator|-
name|min
operator|+
literal|1
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
operator|(
name|last
operator|==
operator|-
literal|1
operator|)
operator|||
name|i
operator|*
operator|(
literal|1
operator|-
name|epsilon
operator|)
operator|>
name|last
condition|)
block|{
name|k
operator|.
name|add
argument_list|(
name|i
argument_list|)
expr_stmt|;
name|v
operator|.
name|add
argument_list|(
name|sum
argument_list|)
expr_stmt|;
name|last
operator|=
name|i
expr_stmt|;
block|}
block|}
if|if
condition|(
name|last
operator|!=
name|max
operator|-
literal|1
condition|)
block|{
name|k
operator|.
name|add
argument_list|(
name|max
operator|-
literal|1
argument_list|)
expr_stmt|;
name|v
operator|.
name|add
argument_list|(
name|sum
argument_list|)
expr_stmt|;
block|}
name|v
operator|.
name|set
argument_list|(
name|v
operator|.
name|size
argument_list|()
operator|-
literal|1
argument_list|,
literal|1.0
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
name|v
operator|.
name|size
argument_list|()
operator|-
literal|2
init|;
name|i
operator|>=
literal|0
condition|;
operator|--
name|i
control|)
block|{
name|v
operator|.
name|set
argument_list|(
name|i
argument_list|,
name|v
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|/
name|sum
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**      * @see DiscreteRNG#nextInt()      */
annotation|@
name|Override
specifier|public
name|int
name|nextInt
parameter_list|()
block|{
name|double
name|d
init|=
name|random
operator|.
name|nextDouble
argument_list|()
decl_stmt|;
name|int
name|idx
init|=
name|Collections
operator|.
name|binarySearch
argument_list|(
name|v
argument_list|,
name|d
argument_list|)
decl_stmt|;
if|if
condition|(
name|idx
operator|>
literal|0
condition|)
block|{
operator|++
name|idx
expr_stmt|;
block|}
else|else
block|{
name|idx
operator|=
operator|-
operator|(
name|idx
operator|+
literal|1
operator|)
expr_stmt|;
block|}
if|if
condition|(
name|idx
operator|>=
name|v
operator|.
name|size
argument_list|()
condition|)
block|{
name|idx
operator|=
name|v
operator|.
name|size
argument_list|()
operator|-
literal|1
expr_stmt|;
block|}
if|if
condition|(
name|idx
operator|==
literal|0
condition|)
block|{
return|return
name|k
operator|.
name|get
argument_list|(
literal|0
argument_list|)
return|;
block|}
name|int
name|ceiling
init|=
name|k
operator|.
name|get
argument_list|(
name|idx
argument_list|)
decl_stmt|;
name|int
name|lower
init|=
name|k
operator|.
name|get
argument_list|(
name|idx
operator|-
literal|1
argument_list|)
decl_stmt|;
return|return
name|ceiling
operator|-
name|random
operator|.
name|nextInt
argument_list|(
name|ceiling
operator|-
name|lower
argument_list|)
return|;
block|}
block|}
comment|/**    * Binomial distribution.    *    * P(k)=select(n, k)*p^k*(1-p)^(n-k) (k = 0, 1, ..., n)    *    * P(k)=select(max-min-1, k-min)*p^(k-min)*(1-p)^(k-min)*(1-p)^(max-k-1)    */
specifier|public
specifier|static
specifier|final
class|class
name|Binomial
implements|implements
name|DiscreteRNG
block|{
specifier|private
specifier|final
name|Random
name|random
decl_stmt|;
specifier|private
specifier|final
name|int
name|min
decl_stmt|;
specifier|private
specifier|final
name|int
name|n
decl_stmt|;
specifier|private
specifier|final
name|double
index|[]
name|v
decl_stmt|;
specifier|private
specifier|static
name|double
name|select
parameter_list|(
name|int
name|n
parameter_list|,
name|int
name|k
parameter_list|)
block|{
name|double
name|ret
init|=
literal|1.0
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
name|k
operator|+
literal|1
init|;
name|i
operator|<=
name|n
condition|;
operator|++
name|i
control|)
block|{
name|ret
operator|*=
operator|(
name|double
operator|)
name|i
operator|/
operator|(
name|i
operator|-
name|k
operator|)
expr_stmt|;
block|}
return|return
name|ret
return|;
block|}
specifier|private
specifier|static
name|double
name|power
parameter_list|(
name|double
name|p
parameter_list|,
name|int
name|k
parameter_list|)
block|{
return|return
name|Math
operator|.
name|exp
argument_list|(
name|k
operator|*
name|Math
operator|.
name|log
argument_list|(
name|p
argument_list|)
argument_list|)
return|;
block|}
comment|/**      * Generate random integers from min (inclusive) to max (exclusive)      * following Binomial distribution.      *      * @param random      *          The basic random number generator.      * @param min      *          Minimum integer      * @param max      *          maximum integer (exclusive).      * @param p      *          parameter.      *      */
specifier|public
name|Binomial
parameter_list|(
name|Random
name|random
parameter_list|,
name|int
name|min
parameter_list|,
name|int
name|max
parameter_list|,
name|double
name|p
parameter_list|)
block|{
if|if
condition|(
name|min
operator|>=
name|max
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Invalid range"
argument_list|)
throw|;
block|}
name|this
operator|.
name|random
operator|=
name|random
expr_stmt|;
name|this
operator|.
name|min
operator|=
name|min
expr_stmt|;
name|this
operator|.
name|n
operator|=
name|max
operator|-
name|min
operator|-
literal|1
expr_stmt|;
if|if
condition|(
name|n
operator|>
literal|0
condition|)
block|{
name|v
operator|=
operator|new
name|double
index|[
name|n
operator|+
literal|1
index|]
expr_stmt|;
name|double
name|sum
init|=
literal|0.0
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<=
name|n
condition|;
operator|++
name|i
control|)
block|{
name|sum
operator|+=
name|select
argument_list|(
name|n
argument_list|,
name|i
argument_list|)
operator|*
name|power
argument_list|(
name|p
argument_list|,
name|i
argument_list|)
operator|*
name|power
argument_list|(
literal|1
operator|-
name|p
argument_list|,
name|n
operator|-
name|i
argument_list|)
expr_stmt|;
name|v
index|[
name|i
index|]
operator|=
name|sum
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
operator|<=
name|n
condition|;
operator|++
name|i
control|)
block|{
name|v
index|[
name|i
index|]
operator|/=
name|sum
expr_stmt|;
block|}
block|}
else|else
block|{
name|v
operator|=
literal|null
expr_stmt|;
block|}
block|}
comment|/**      * @see DiscreteRNG#nextInt()      */
annotation|@
name|Override
specifier|public
name|int
name|nextInt
parameter_list|()
block|{
if|if
condition|(
name|v
operator|==
literal|null
condition|)
block|{
return|return
name|min
return|;
block|}
name|double
name|d
init|=
name|random
operator|.
name|nextDouble
argument_list|()
decl_stmt|;
name|int
name|idx
init|=
name|Arrays
operator|.
name|binarySearch
argument_list|(
name|v
argument_list|,
name|d
argument_list|)
decl_stmt|;
if|if
condition|(
name|idx
operator|>
literal|0
condition|)
block|{
operator|++
name|idx
expr_stmt|;
block|}
else|else
block|{
name|idx
operator|=
operator|-
operator|(
name|idx
operator|+
literal|1
operator|)
expr_stmt|;
block|}
if|if
condition|(
name|idx
operator|>=
name|v
operator|.
name|length
condition|)
block|{
name|idx
operator|=
name|v
operator|.
name|length
operator|-
literal|1
expr_stmt|;
block|}
return|return
name|idx
operator|+
name|min
return|;
block|}
block|}
block|}
end_class

end_unit

