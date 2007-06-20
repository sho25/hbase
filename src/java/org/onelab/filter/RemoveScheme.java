begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  *  * Copyright (c) 2005, European Commission project OneLab under contract 034819  * (http://www.one-lab.org)  *   * All rights reserved.  * Redistribution and use in source and binary forms, with or   * without modification, are permitted provided that the following   * conditions are met:  *  - Redistributions of source code must retain the above copyright   *    notice, this list of conditions and the following disclaimer.  *  - Redistributions in binary form must reproduce the above copyright   *    notice, this list of conditions and the following disclaimer in   *    the documentation and/or other materials provided with the distribution.  *  - Neither the name of the University Catholique de Louvain - UCL  *    nor the names of its contributors may be used to endorse or   *    promote products derived from this software without specific prior   *    written permission.  *      * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS   * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT   * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS   * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE   * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,   * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,   * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;   * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER   * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT   * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN   * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE   * POSSIBILITY OF SUCH DAMAGE.  */
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

begin_comment
comment|/**  * Defines the different remove scheme for retouched Bloom filters.  *   * contract<a href="http://www.one-lab.org">European Commission One-Lab Project 034819</a>.  *  * @version 1.0 - 7 Feb. 07  */
end_comment

begin_interface
specifier|public
interface|interface
name|RemoveScheme
block|{
comment|/**    * Random selection.    *<p>    * The idea is to randomly select a bit to reset.    */
specifier|public
specifier|final
specifier|static
name|short
name|RANDOM
init|=
literal|0
decl_stmt|;
comment|/**    * MinimumFN Selection.    *<p>    * The idea is to select the bit to reset that will generate the minimum    * number of false negative.    */
specifier|public
specifier|final
specifier|static
name|short
name|MINIMUM_FN
init|=
literal|1
decl_stmt|;
comment|/**    * MaximumFP Selection.    *<p>    * The idea is to select the bit to reset that will remove the maximum number    * of false positive.    */
specifier|public
specifier|final
specifier|static
name|short
name|MAXIMUM_FP
init|=
literal|2
decl_stmt|;
comment|/**    * Ratio Selection.    *<p>    * The idea is to select the bit to reset that will, at the same time, remove    * the maximum number of false positve while minimizing the amount of false    * negative generated.    */
specifier|public
specifier|final
specifier|static
name|short
name|RATIO
init|=
literal|3
decl_stmt|;
block|}
end_interface

begin_comment
comment|//end interface
end_comment

end_unit

