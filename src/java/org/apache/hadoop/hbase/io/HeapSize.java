begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2009 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
package|;
end_package

begin_comment
comment|/**  * Implementations can be asked for an estimate of their size in bytes.  *<p>  * Useful for sizing caches.  Its a given that implementation approximations  * do not account for 32 vs 64 bit nor for different VM implementations.  *<p>  * An Object's size is determined by the non-static data members in it,  * as well as the fixed {@link Object} overhead.  *<p>  * For example:  *<pre>  * public class SampleObject implements HeapSize {  *     *   int [] numbers;  *   int x;  * }  *</pre>  */
end_comment

begin_interface
specifier|public
interface|interface
name|HeapSize
block|{
comment|/** Reference size is 8 bytes on 64-bit, 4 bytes on 32-bit */
specifier|static
specifier|final
name|int
name|REFERENCE
init|=
literal|8
decl_stmt|;
comment|/** Object overhead is minimum 2 * reference size (8 bytes on 64-bit) */
specifier|static
specifier|final
name|int
name|OBJECT
init|=
literal|2
operator|*
name|REFERENCE
decl_stmt|;
comment|/** Array overhead */
specifier|static
specifier|final
name|int
name|ARRAY
init|=
literal|3
operator|*
name|REFERENCE
decl_stmt|;
comment|/** OverHead for nested arrays */
specifier|static
specifier|final
name|int
name|MULTI_ARRAY
init|=
operator|(
literal|4
operator|*
name|REFERENCE
operator|)
operator|+
name|ARRAY
decl_stmt|;
comment|/** Byte arrays are fixed size below plus its length, 8 byte aligned */
specifier|static
specifier|final
name|int
name|BYTE_ARRAY
init|=
literal|3
operator|*
name|REFERENCE
decl_stmt|;
comment|/** Overhead for ByteBuffer */
specifier|static
specifier|final
name|int
name|BYTE_BUFFER
init|=
literal|56
decl_stmt|;
comment|/** String overhead */
specifier|static
specifier|final
name|int
name|STRING_SIZE
init|=
literal|64
decl_stmt|;
comment|/** Overhead for ArrayList(0) */
specifier|static
specifier|final
name|int
name|ARRAYLIST_SIZE
init|=
literal|64
decl_stmt|;
comment|/** Overhead for TreeMap */
specifier|static
specifier|final
name|int
name|TREEMAP_SIZE
init|=
literal|80
decl_stmt|;
comment|/** Overhead for entry in map */
specifier|static
specifier|final
name|int
name|MAP_ENTRY_SIZE
init|=
literal|64
decl_stmt|;
comment|/**    * @return Approximate 'exclusive deep size' of implementing object.  Includes    * count of payload and hosting object sizings.   */
specifier|public
name|long
name|heapSize
parameter_list|()
function_decl|;
block|}
end_interface

end_unit

