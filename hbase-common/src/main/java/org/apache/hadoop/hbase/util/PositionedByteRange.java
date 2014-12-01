begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|hbase
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
comment|/**  *<p>  * Extends {@link ByteRange} with additional methods to support tracking a  * consumers position within the viewport. The API is extended with methods  * {@link #get()} and {@link #put(byte)} for interacting with the backing  * array from the current position forward. This frees the caller from managing  * their own index into the array.  *</p>  *<p>  * Designed to be a slimmed-down, mutable alternative to {@link java.nio.ByteBuffer}.  *</p>  */
end_comment

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|Public
annotation|@
name|InterfaceStability
operator|.
name|Evolving
specifier|public
interface|interface
name|PositionedByteRange
extends|extends
name|ByteRange
block|{
comment|// net new API is here.
comment|/**    * The current {@code position} marker. This valuae is 0-indexed, relative to    * the beginning of the range.    */
specifier|public
name|int
name|getPosition
parameter_list|()
function_decl|;
comment|/**    * Update the {@code position} index. May not be greater than {@code length}.    * @param position the new position in this range.    * @return this.    */
specifier|public
name|PositionedByteRange
name|setPosition
parameter_list|(
name|int
name|position
parameter_list|)
function_decl|;
comment|/**    * The number of bytes remaining between position and the end of the range.    */
specifier|public
name|int
name|getRemaining
parameter_list|()
function_decl|;
comment|/**    * Retrieve the next byte from this range without incrementing position.    */
specifier|public
name|byte
name|peek
parameter_list|()
function_decl|;
comment|/**    * Retrieve the next byte from this range.    */
specifier|public
name|byte
name|get
parameter_list|()
function_decl|;
comment|/**    * Retrieve the next short value from this range.    */
specifier|public
name|short
name|getShort
parameter_list|()
function_decl|;
comment|/**    * Retrieve the next int value from this range.    */
specifier|public
name|int
name|getInt
parameter_list|()
function_decl|;
comment|/**    * Retrieve the next long value from this range.    */
specifier|public
name|long
name|getLong
parameter_list|()
function_decl|;
comment|/**    * Retrieve the next long value, which is stored as VLong, from this range    * @return the long value which is stored as VLong    */
specifier|public
name|long
name|getVLong
parameter_list|()
function_decl|;
comment|/**    * Fill {@code dst} with bytes from the range, starting from {@code position}.    * This range's {@code position} is incremented by the length of {@code dst},    * the number of bytes copied.    * @param dst the destination of the copy.    * @return this.    */
specifier|public
name|PositionedByteRange
name|get
parameter_list|(
name|byte
index|[]
name|dst
parameter_list|)
function_decl|;
comment|/**    * Fill {@code dst} with bytes from the range, starting from the current    * {@code position}. {@code length} bytes are copied into {@code dst},    * starting at {@code offset}. This range's {@code position} is incremented    * by the number of bytes copied.    * @param dst the destination of the copy.    * @param offset the offset into {@code dst} to start the copy.    * @param length the number of bytes to copy into {@code dst}.    * @return this.    */
specifier|public
name|PositionedByteRange
name|get
parameter_list|(
name|byte
index|[]
name|dst
parameter_list|,
name|int
name|offset
parameter_list|,
name|int
name|length
parameter_list|)
function_decl|;
comment|/**    * Store {@code val} at the next position in this range.    * @param val the new value.    * @return this.    */
specifier|public
name|PositionedByteRange
name|put
parameter_list|(
name|byte
name|val
parameter_list|)
function_decl|;
comment|/**    * Store short {@code val} at the next position in this range.    * @param val the new value.    * @return this.    */
specifier|public
name|PositionedByteRange
name|putShort
parameter_list|(
name|short
name|val
parameter_list|)
function_decl|;
comment|/**    * Store int {@code val} at the next position in this range.    * @param val the new value.    * @return this.    */
specifier|public
name|PositionedByteRange
name|putInt
parameter_list|(
name|int
name|val
parameter_list|)
function_decl|;
comment|/**    * Store long {@code val} at the next position in this range.    * @param val the new value.    * @return this.    */
specifier|public
name|PositionedByteRange
name|putLong
parameter_list|(
name|long
name|val
parameter_list|)
function_decl|;
comment|/**    * Store the long {@code val} at the next position as a VLong    * @param val the value to store    * @return number of bytes written    */
specifier|public
name|int
name|putVLong
parameter_list|(
name|long
name|val
parameter_list|)
function_decl|;
comment|/**    * Store the content of {@code val} in this range, starting at the next position.    * @param val the new value.    * @return this.    */
specifier|public
name|PositionedByteRange
name|put
parameter_list|(
name|byte
index|[]
name|val
parameter_list|)
function_decl|;
comment|/**    * Store {@code length} bytes from {@code val} into this range. Bytes from    * {@code val} are copied starting at {@code offset} into the range, starting at    * the current position.    * @param val the new value.    * @param offset the offset in {@code val} from which to start copying.    * @param length the number of bytes to copy from {@code val}.    * @return this.    */
specifier|public
name|PositionedByteRange
name|put
parameter_list|(
name|byte
index|[]
name|val
parameter_list|,
name|int
name|offset
parameter_list|,
name|int
name|length
parameter_list|)
function_decl|;
comment|/**    * Limits the byte range upto a specified value. Limit cannot be greater than    * capacity    *    * @param limit    * @return PositionedByteRange    */
specifier|public
name|PositionedByteRange
name|setLimit
parameter_list|(
name|int
name|limit
parameter_list|)
function_decl|;
comment|/**    * Return the current limit    *    * @return limit    */
specifier|public
name|int
name|getLimit
parameter_list|()
function_decl|;
comment|// override parent interface declarations to return this interface.
annotation|@
name|Override
specifier|public
name|PositionedByteRange
name|unset
parameter_list|()
function_decl|;
annotation|@
name|Override
specifier|public
name|PositionedByteRange
name|set
parameter_list|(
name|int
name|capacity
parameter_list|)
function_decl|;
annotation|@
name|Override
specifier|public
name|PositionedByteRange
name|set
parameter_list|(
name|byte
index|[]
name|bytes
parameter_list|)
function_decl|;
annotation|@
name|Override
specifier|public
name|PositionedByteRange
name|set
parameter_list|(
name|byte
index|[]
name|bytes
parameter_list|,
name|int
name|offset
parameter_list|,
name|int
name|length
parameter_list|)
function_decl|;
annotation|@
name|Override
specifier|public
name|PositionedByteRange
name|setOffset
parameter_list|(
name|int
name|offset
parameter_list|)
function_decl|;
annotation|@
name|Override
specifier|public
name|PositionedByteRange
name|setLength
parameter_list|(
name|int
name|length
parameter_list|)
function_decl|;
annotation|@
name|Override
specifier|public
name|PositionedByteRange
name|get
parameter_list|(
name|int
name|index
parameter_list|,
name|byte
index|[]
name|dst
parameter_list|)
function_decl|;
annotation|@
name|Override
specifier|public
name|PositionedByteRange
name|get
parameter_list|(
name|int
name|index
parameter_list|,
name|byte
index|[]
name|dst
parameter_list|,
name|int
name|offset
parameter_list|,
name|int
name|length
parameter_list|)
function_decl|;
annotation|@
name|Override
specifier|public
name|PositionedByteRange
name|put
parameter_list|(
name|int
name|index
parameter_list|,
name|byte
name|val
parameter_list|)
function_decl|;
annotation|@
name|Override
specifier|public
name|PositionedByteRange
name|putShort
parameter_list|(
name|int
name|index
parameter_list|,
name|short
name|val
parameter_list|)
function_decl|;
annotation|@
name|Override
specifier|public
name|PositionedByteRange
name|putInt
parameter_list|(
name|int
name|index
parameter_list|,
name|int
name|val
parameter_list|)
function_decl|;
annotation|@
name|Override
specifier|public
name|PositionedByteRange
name|putLong
parameter_list|(
name|int
name|index
parameter_list|,
name|long
name|val
parameter_list|)
function_decl|;
annotation|@
name|Override
specifier|public
name|PositionedByteRange
name|put
parameter_list|(
name|int
name|index
parameter_list|,
name|byte
index|[]
name|val
parameter_list|)
function_decl|;
annotation|@
name|Override
specifier|public
name|PositionedByteRange
name|put
parameter_list|(
name|int
name|index
parameter_list|,
name|byte
index|[]
name|val
parameter_list|,
name|int
name|offset
parameter_list|,
name|int
name|length
parameter_list|)
function_decl|;
annotation|@
name|Override
specifier|public
name|PositionedByteRange
name|deepCopy
parameter_list|()
function_decl|;
annotation|@
name|Override
specifier|public
name|PositionedByteRange
name|shallowCopy
parameter_list|()
function_decl|;
annotation|@
name|Override
specifier|public
name|PositionedByteRange
name|shallowCopySubRange
parameter_list|(
name|int
name|innerOffset
parameter_list|,
name|int
name|copyLength
parameter_list|)
function_decl|;
block|}
end_interface

end_unit

