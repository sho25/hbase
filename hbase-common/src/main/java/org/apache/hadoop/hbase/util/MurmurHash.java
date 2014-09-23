begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|classification
operator|.
name|InterfaceStability
import|;
end_import

begin_comment
comment|/**  * This is a very fast, non-cryptographic hash suitable for general hash-based  * lookup.  See http://murmurhash.googlepages.com/ for more details.  *  *<p>The C version of MurmurHash 2.0 found at that site was ported  * to Java by Andrzej Bialecki (ab at getopt org).</p>  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Public
annotation|@
name|InterfaceStability
operator|.
name|Stable
specifier|public
class|class
name|MurmurHash
extends|extends
name|Hash
block|{
specifier|private
specifier|static
name|MurmurHash
name|_instance
init|=
operator|new
name|MurmurHash
argument_list|()
decl_stmt|;
specifier|public
specifier|static
name|Hash
name|getInstance
parameter_list|()
block|{
return|return
name|_instance
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|hash
parameter_list|(
name|byte
index|[]
name|data
parameter_list|,
name|int
name|offset
parameter_list|,
name|int
name|length
parameter_list|,
name|int
name|seed
parameter_list|)
block|{
name|int
name|m
init|=
literal|0x5bd1e995
decl_stmt|;
name|int
name|r
init|=
literal|24
decl_stmt|;
name|int
name|h
init|=
name|seed
operator|^
name|length
decl_stmt|;
name|int
name|len_4
init|=
name|length
operator|>>
literal|2
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
name|len_4
condition|;
name|i
operator|++
control|)
block|{
name|int
name|i_4
init|=
operator|(
name|i
operator|<<
literal|2
operator|)
operator|+
name|offset
decl_stmt|;
name|int
name|k
init|=
name|data
index|[
name|i_4
operator|+
literal|3
index|]
decl_stmt|;
name|k
operator|=
name|k
operator|<<
literal|8
expr_stmt|;
name|k
operator|=
name|k
operator||
operator|(
name|data
index|[
name|i_4
operator|+
literal|2
index|]
operator|&
literal|0xff
operator|)
expr_stmt|;
name|k
operator|=
name|k
operator|<<
literal|8
expr_stmt|;
name|k
operator|=
name|k
operator||
operator|(
name|data
index|[
name|i_4
operator|+
literal|1
index|]
operator|&
literal|0xff
operator|)
expr_stmt|;
name|k
operator|=
name|k
operator|<<
literal|8
expr_stmt|;
comment|//noinspection PointlessArithmeticExpression
name|k
operator|=
name|k
operator||
operator|(
name|data
index|[
name|i_4
operator|+
literal|0
index|]
operator|&
literal|0xff
operator|)
expr_stmt|;
name|k
operator|*=
name|m
expr_stmt|;
name|k
operator|^=
name|k
operator|>>>
name|r
expr_stmt|;
name|k
operator|*=
name|m
expr_stmt|;
name|h
operator|*=
name|m
expr_stmt|;
name|h
operator|^=
name|k
expr_stmt|;
block|}
comment|// avoid calculating modulo
name|int
name|len_m
init|=
name|len_4
operator|<<
literal|2
decl_stmt|;
name|int
name|left
init|=
name|length
operator|-
name|len_m
decl_stmt|;
name|int
name|i_m
init|=
name|len_m
operator|+
name|offset
decl_stmt|;
if|if
condition|(
name|left
operator|!=
literal|0
condition|)
block|{
if|if
condition|(
name|left
operator|>=
literal|3
condition|)
block|{
name|h
operator|^=
name|data
index|[
name|i_m
operator|+
literal|2
index|]
operator|<<
literal|16
expr_stmt|;
block|}
if|if
condition|(
name|left
operator|>=
literal|2
condition|)
block|{
name|h
operator|^=
name|data
index|[
name|i_m
operator|+
literal|1
index|]
operator|<<
literal|8
expr_stmt|;
block|}
if|if
condition|(
name|left
operator|>=
literal|1
condition|)
block|{
name|h
operator|^=
name|data
index|[
name|i_m
index|]
expr_stmt|;
block|}
name|h
operator|*=
name|m
expr_stmt|;
block|}
name|h
operator|^=
name|h
operator|>>>
literal|13
expr_stmt|;
name|h
operator|*=
name|m
expr_stmt|;
name|h
operator|^=
name|h
operator|>>>
literal|15
expr_stmt|;
return|return
name|h
return|;
block|}
block|}
end_class

end_unit

