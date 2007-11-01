begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  *  * Copyright (c) 2005, European Commission project OneLab under contract 034819   * (http://www.one-lab.org)  *   * All rights reserved.  * Redistribution and use in source and binary forms, with or   * without modification, are permitted provided that the following   * conditions are met:  *  - Redistributions of source code must retain the above copyright   *    notice, this list of conditions and the following disclaimer.  *  - Redistributions in binary form must reproduce the above copyright   *    notice, this list of conditions and the following disclaimer in   *    the documentation and/or other materials provided with the distribution.  *  - Neither the name of the University Catholique de Louvain - UCL  *    nor the names of its contributors may be used to endorse or   *    promote products derived from this software without specific prior   *    written permission.  *      * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS   * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT   * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS   * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE   * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,   * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,   * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;   * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER   * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT   * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN   * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE   * POSSIBILITY OF SUCH DAMAGE.  */
end_comment

begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|JenkinsHash
import|;
end_import

begin_comment
comment|/**  * Implements a hash object that returns a certain number of hashed values.  *<p>  * It is based on the SHA-1 algorithm.   *   * @see org.onelab.filter.Filter The general behavior of a filter  *  * @version 1.0 - 2 Feb. 07  *   * @see org.onelab.filter.Key The general behavior of a key being stored in a filter  * @see org.onelab.filter.Filter The general behavior of a filter  *   * @see<a href="http://www.itl.nist.gov/fipspubs/fip180-1.htm">SHA-1 algorithm</a>  */
end_comment

begin_class
specifier|public
specifier|final
class|class
name|HashFunction
block|{
comment|/** The number of hashed values. */
specifier|private
name|int
name|nbHash
decl_stmt|;
comment|/** The maximum highest returned value. */
specifier|private
name|int
name|maxValue
decl_stmt|;
comment|/**    * Constructor.    *<p>    * Builds a hash function that must obey to a given maximum number of returned values and a highest value.    * @param maxValue The maximum highest returned value.    * @param nbHash The number of resulting hashed values.    */
specifier|public
name|HashFunction
parameter_list|(
name|int
name|maxValue
parameter_list|,
name|int
name|nbHash
parameter_list|)
block|{
if|if
condition|(
name|maxValue
operator|<=
literal|0
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"maxValue must be> 0"
argument_list|)
throw|;
block|}
if|if
condition|(
name|nbHash
operator|<=
literal|0
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"nbHash must be> 0"
argument_list|)
throw|;
block|}
name|this
operator|.
name|maxValue
operator|=
name|maxValue
expr_stmt|;
name|this
operator|.
name|nbHash
operator|=
name|nbHash
expr_stmt|;
block|}
comment|//end constructor
comment|/** Clears<i>this</i> hash function. A NOOP */
specifier|public
name|void
name|clear
parameter_list|()
block|{   }
comment|//end clear()
comment|/**    * Hashes a specified key into several integers.    * @param k The specified key.    * @return The array of hashed values.    */
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
specifier|public
name|int
index|[]
name|hash
parameter_list|(
name|Key
name|k
parameter_list|)
block|{
name|byte
index|[]
name|b
init|=
name|k
operator|.
name|getBytes
argument_list|()
decl_stmt|;
if|if
condition|(
name|b
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|NullPointerException
argument_list|(
literal|"buffer reference is null"
argument_list|)
throw|;
block|}
if|if
condition|(
name|b
operator|.
name|length
operator|==
literal|0
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"key length must be> 0"
argument_list|)
throw|;
block|}
name|int
index|[]
name|result
init|=
operator|new
name|int
index|[
name|nbHash
index|]
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|,
name|initval
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
name|result
index|[
name|i
index|]
operator|=
name|Math
operator|.
name|abs
argument_list|(
name|JenkinsHash
operator|.
name|hash
argument_list|(
name|b
argument_list|,
name|initval
argument_list|)
argument_list|)
operator|%
name|maxValue
expr_stmt|;
block|}
return|return
name|result
return|;
block|}
comment|//end hash()
block|}
end_class

begin_comment
comment|//end class
end_comment

end_unit

