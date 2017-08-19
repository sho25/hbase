begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
package|;
end_package

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|ByteBuffer
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
name|Bytes
import|;
end_import

begin_comment
comment|/**  * Tags are part of cells and helps to add metadata about them.  * Metadata could be ACLs, visibility labels, etc.  *<p>  * Each Tag is having a type (one byte) and value part. The max value length for a Tag is 65533.  *<p>  * See {@link TagType} for reserved tag types.  */
end_comment

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|Private
annotation|@
name|InterfaceStability
operator|.
name|Evolving
specifier|public
interface|interface
name|Tag
block|{
specifier|public
specifier|final
specifier|static
name|int
name|TYPE_LENGTH_SIZE
init|=
name|Bytes
operator|.
name|SIZEOF_BYTE
decl_stmt|;
specifier|public
specifier|final
specifier|static
name|int
name|TAG_LENGTH_SIZE
init|=
name|Bytes
operator|.
name|SIZEOF_SHORT
decl_stmt|;
specifier|public
specifier|final
specifier|static
name|int
name|INFRASTRUCTURE_SIZE
init|=
name|TYPE_LENGTH_SIZE
operator|+
name|TAG_LENGTH_SIZE
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|int
name|MAX_TAG_LENGTH
init|=
operator|(
literal|2
operator|*
name|Short
operator|.
name|MAX_VALUE
operator|)
operator|+
literal|1
operator|-
name|TAG_LENGTH_SIZE
decl_stmt|;
comment|/**    * @return the tag type    */
name|byte
name|getType
parameter_list|()
function_decl|;
comment|/**    * @return Offset of tag value within the backed buffer    */
name|int
name|getValueOffset
parameter_list|()
function_decl|;
comment|/**    * @return Length of tag value within the backed buffer    */
name|int
name|getValueLength
parameter_list|()
function_decl|;
comment|/**    * Tells whether or not this Tag is backed by a byte array.    * @return true when this Tag is backed by byte array    */
name|boolean
name|hasArray
parameter_list|()
function_decl|;
comment|/**    * @return The array containing the value bytes.    * @throws UnsupportedOperationException    *           when {@link #hasArray()} return false. Use {@link #getValueByteBuffer()} in such    *           situation    */
name|byte
index|[]
name|getValueArray
parameter_list|()
function_decl|;
comment|/**    * @return The {@link java.nio.ByteBuffer} containing the value bytes.    */
name|ByteBuffer
name|getValueByteBuffer
parameter_list|()
function_decl|;
block|}
end_interface

end_unit

