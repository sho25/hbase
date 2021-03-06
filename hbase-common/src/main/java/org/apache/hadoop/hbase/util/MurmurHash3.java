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
name|yetus
operator|.
name|audience
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
name|yetus
operator|.
name|audience
operator|.
name|InterfaceStability
import|;
end_import

begin_comment
comment|/**  * This is a very fast, non-cryptographic hash suitable for general hash-based  * lookup.  See http://code.google.com/p/smhasher/wiki/MurmurHash3 for details.  *  *<p>MurmurHash3 is the successor to MurmurHash2. It comes in 3 variants, and  * the 32-bit version targets low latency for hash table use.</p>  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
annotation|@
name|InterfaceStability
operator|.
name|Stable
specifier|public
class|class
name|MurmurHash3
extends|extends
name|Hash
block|{
specifier|private
specifier|static
name|MurmurHash3
name|_instance
init|=
operator|new
name|MurmurHash3
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
comment|/** Returns the MurmurHash3_x86_32 hash. */
annotation|@
name|edu
operator|.
name|umd
operator|.
name|cs
operator|.
name|findbugs
operator|.
name|annotations
operator|.
name|SuppressWarnings
argument_list|(
literal|"SF"
argument_list|)
annotation|@
name|Override
specifier|public
parameter_list|<
name|T
parameter_list|>
name|int
name|hash
parameter_list|(
name|HashKey
argument_list|<
name|T
argument_list|>
name|hashKey
parameter_list|,
name|int
name|initval
parameter_list|)
block|{
specifier|final
name|int
name|c1
init|=
literal|0xcc9e2d51
decl_stmt|;
specifier|final
name|int
name|c2
init|=
literal|0x1b873593
decl_stmt|;
name|int
name|length
init|=
name|hashKey
operator|.
name|length
argument_list|()
decl_stmt|;
name|int
name|h1
init|=
name|initval
decl_stmt|;
name|int
name|roundedEnd
init|=
operator|(
name|length
operator|&
literal|0xfffffffc
operator|)
decl_stmt|;
comment|// round down to 4 byte block
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|roundedEnd
condition|;
name|i
operator|+=
literal|4
control|)
block|{
comment|// little endian load order
name|int
name|k1
init|=
operator|(
name|hashKey
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|&
literal|0xff
operator|)
operator||
operator|(
operator|(
name|hashKey
operator|.
name|get
argument_list|(
name|i
operator|+
literal|1
argument_list|)
operator|&
literal|0xff
operator|)
operator|<<
literal|8
operator|)
operator||
operator|(
operator|(
name|hashKey
operator|.
name|get
argument_list|(
name|i
operator|+
literal|2
argument_list|)
operator|&
literal|0xff
operator|)
operator|<<
literal|16
operator|)
operator||
operator|(
name|hashKey
operator|.
name|get
argument_list|(
name|i
operator|+
literal|3
argument_list|)
operator|<<
literal|24
operator|)
decl_stmt|;
name|k1
operator|*=
name|c1
expr_stmt|;
name|k1
operator|=
operator|(
name|k1
operator|<<
literal|15
operator|)
operator||
operator|(
name|k1
operator|>>>
literal|17
operator|)
expr_stmt|;
comment|// ROTL32(k1,15);
name|k1
operator|*=
name|c2
expr_stmt|;
name|h1
operator|^=
name|k1
expr_stmt|;
name|h1
operator|=
operator|(
name|h1
operator|<<
literal|13
operator|)
operator||
operator|(
name|h1
operator|>>>
literal|19
operator|)
expr_stmt|;
comment|// ROTL32(h1,13);
name|h1
operator|=
name|h1
operator|*
literal|5
operator|+
literal|0xe6546b64
expr_stmt|;
block|}
comment|// tail
name|int
name|k1
init|=
literal|0
decl_stmt|;
switch|switch
condition|(
name|length
operator|&
literal|0x03
condition|)
block|{
case|case
literal|3
case|:
name|k1
operator|=
operator|(
name|hashKey
operator|.
name|get
argument_list|(
name|roundedEnd
operator|+
literal|2
argument_list|)
operator|&
literal|0xff
operator|)
operator|<<
literal|16
expr_stmt|;
comment|// FindBugs SF_SWITCH_FALLTHROUGH
case|case
literal|2
case|:
name|k1
operator||=
operator|(
name|hashKey
operator|.
name|get
argument_list|(
name|roundedEnd
operator|+
literal|1
argument_list|)
operator|&
literal|0xff
operator|)
operator|<<
literal|8
expr_stmt|;
comment|// FindBugs SF_SWITCH_FALLTHROUGH
case|case
literal|1
case|:
name|k1
operator||=
operator|(
name|hashKey
operator|.
name|get
argument_list|(
name|roundedEnd
argument_list|)
operator|&
literal|0xff
operator|)
expr_stmt|;
name|k1
operator|*=
name|c1
expr_stmt|;
name|k1
operator|=
operator|(
name|k1
operator|<<
literal|15
operator|)
operator||
operator|(
name|k1
operator|>>>
literal|17
operator|)
expr_stmt|;
comment|// ROTL32(k1,15);
name|k1
operator|*=
name|c2
expr_stmt|;
name|h1
operator|^=
name|k1
expr_stmt|;
default|default:
comment|// fall out
block|}
comment|// finalization
name|h1
operator|^=
name|length
expr_stmt|;
comment|// fmix(h1);
name|h1
operator|^=
name|h1
operator|>>>
literal|16
expr_stmt|;
name|h1
operator|*=
literal|0x85ebca6b
expr_stmt|;
name|h1
operator|^=
name|h1
operator|>>>
literal|13
expr_stmt|;
name|h1
operator|*=
literal|0xc2b2ae35
expr_stmt|;
name|h1
operator|^=
name|h1
operator|>>>
literal|16
expr_stmt|;
return|return
name|h1
return|;
block|}
block|}
end_class

end_unit

