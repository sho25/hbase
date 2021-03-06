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
name|Random
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
name|RandomDistribution
operator|.
name|DiscreteRNG
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
name|BytesWritable
import|;
end_import

begin_comment
comment|/* *<p> * Copied from *<a href="https://issues.apache.org/jira/browse/HADOOP-3315">hadoop-3315 tfile</a>. * Remove after tfile is committed and use the tfile version of this class * instead.</p> */
end_comment

begin_class
class|class
name|KeySampler
block|{
name|Random
name|random
decl_stmt|;
name|int
name|min
decl_stmt|,
name|max
decl_stmt|;
name|DiscreteRNG
name|keyLenRNG
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|MIN_KEY_LEN
init|=
literal|4
decl_stmt|;
specifier|public
name|KeySampler
parameter_list|(
name|Random
name|random
parameter_list|,
name|byte
index|[]
name|first
parameter_list|,
name|byte
index|[]
name|last
parameter_list|,
name|DiscreteRNG
name|keyLenRNG
parameter_list|)
block|{
name|this
operator|.
name|random
operator|=
name|random
expr_stmt|;
name|int
name|firstLen
init|=
name|keyPrefixToInt
argument_list|(
name|first
argument_list|)
decl_stmt|;
name|int
name|lastLen
init|=
name|keyPrefixToInt
argument_list|(
name|last
argument_list|)
decl_stmt|;
name|min
operator|=
name|Math
operator|.
name|min
argument_list|(
name|firstLen
argument_list|,
name|lastLen
argument_list|)
expr_stmt|;
name|max
operator|=
name|Math
operator|.
name|max
argument_list|(
name|firstLen
argument_list|,
name|lastLen
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
name|min
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
name|max
argument_list|)
expr_stmt|;
name|this
operator|.
name|keyLenRNG
operator|=
name|keyLenRNG
expr_stmt|;
block|}
specifier|private
name|int
name|keyPrefixToInt
parameter_list|(
name|byte
index|[]
name|key
parameter_list|)
block|{
name|byte
index|[]
name|b
init|=
name|key
decl_stmt|;
name|int
name|o
init|=
literal|0
decl_stmt|;
return|return
operator|(
name|b
index|[
name|o
index|]
operator|&
literal|0xff
operator|)
operator|<<
literal|24
operator||
operator|(
name|b
index|[
name|o
operator|+
literal|1
index|]
operator|&
literal|0xff
operator|)
operator|<<
literal|16
operator||
operator|(
name|b
index|[
name|o
operator|+
literal|2
index|]
operator|&
literal|0xff
operator|)
operator|<<
literal|8
operator||
operator|(
name|b
index|[
name|o
operator|+
literal|3
index|]
operator|&
literal|0xff
operator|)
return|;
block|}
specifier|public
name|void
name|next
parameter_list|(
name|BytesWritable
name|key
parameter_list|)
block|{
name|key
operator|.
name|setSize
argument_list|(
name|Math
operator|.
name|max
argument_list|(
name|MIN_KEY_LEN
argument_list|,
name|keyLenRNG
operator|.
name|nextInt
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|random
operator|.
name|nextBytes
argument_list|(
name|key
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
name|int
name|rnd
init|=
literal|0
decl_stmt|;
if|if
condition|(
name|max
operator|!=
name|min
condition|)
block|{
name|rnd
operator|=
name|random
operator|.
name|nextInt
argument_list|(
name|max
operator|-
name|min
argument_list|)
expr_stmt|;
block|}
name|int
name|n
init|=
name|rnd
operator|+
name|min
decl_stmt|;
name|byte
index|[]
name|b
init|=
name|key
operator|.
name|get
argument_list|()
decl_stmt|;
name|b
index|[
literal|0
index|]
operator|=
call|(
name|byte
call|)
argument_list|(
name|n
operator|>>
literal|24
argument_list|)
expr_stmt|;
name|b
index|[
literal|1
index|]
operator|=
call|(
name|byte
call|)
argument_list|(
name|n
operator|>>
literal|16
argument_list|)
expr_stmt|;
name|b
index|[
literal|2
index|]
operator|=
call|(
name|byte
call|)
argument_list|(
name|n
operator|>>
literal|8
argument_list|)
expr_stmt|;
name|b
index|[
literal|3
index|]
operator|=
operator|(
name|byte
operator|)
name|n
expr_stmt|;
block|}
block|}
end_class

end_unit

