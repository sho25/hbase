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
comment|/**  * Extends the basic {@link SimpleByteRange} implementation with position  * support. {@code position} is considered transient, not fundamental to the  * definition of the range, and does not participate in  * {@link #compareTo(ByteRange)}, {@link #hashCode()}, or  * {@link #equals(Object)}. {@code Position} is retained by copy operations.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
annotation|@
name|InterfaceStability
operator|.
name|Evolving
specifier|public
specifier|abstract
class|class
name|AbstractPositionedByteRange
extends|extends
name|AbstractByteRange
implements|implements
name|PositionedByteRange
block|{
comment|/**    * The current index into the range. Like {@link java.nio.ByteBuffer} position, it    * points to the next value that will be read/written in the array. It    * provides the appearance of being 0-indexed, even though its value is    * calculated according to offset.    *<p>    * Position is considered transient and does not participate in    * {@link #equals(Object)} or {@link #hashCode()} comparisons.    *</p>    */
specifier|protected
name|int
name|position
init|=
literal|0
decl_stmt|;
specifier|protected
name|int
name|limit
init|=
literal|0
decl_stmt|;
annotation|@
name|Override
specifier|public
name|PositionedByteRange
name|set
parameter_list|(
name|int
name|capacity
parameter_list|)
block|{
name|this
operator|.
name|position
operator|=
literal|0
expr_stmt|;
name|super
operator|.
name|set
argument_list|(
name|capacity
argument_list|)
expr_stmt|;
name|this
operator|.
name|limit
operator|=
name|capacity
expr_stmt|;
return|return
name|this
return|;
block|}
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
block|{
name|this
operator|.
name|position
operator|=
literal|0
expr_stmt|;
name|super
operator|.
name|set
argument_list|(
name|bytes
argument_list|)
expr_stmt|;
name|this
operator|.
name|limit
operator|=
name|bytes
operator|.
name|length
expr_stmt|;
return|return
name|this
return|;
block|}
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
block|{
name|this
operator|.
name|position
operator|=
literal|0
expr_stmt|;
name|super
operator|.
name|set
argument_list|(
name|bytes
argument_list|,
name|offset
argument_list|,
name|length
argument_list|)
expr_stmt|;
name|limit
operator|=
name|length
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**    * Update the beginning of this range. {@code offset + length} may not be    * greater than {@code bytes.length}. Resets {@code position} to 0.    *    * @param offset    *          the new start of this range.    * @return this.    */
annotation|@
name|Override
specifier|public
name|PositionedByteRange
name|setOffset
parameter_list|(
name|int
name|offset
parameter_list|)
block|{
name|this
operator|.
name|position
operator|=
literal|0
expr_stmt|;
name|super
operator|.
name|setOffset
argument_list|(
name|offset
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**    * Update the length of this range. {@code offset + length} should not be    * greater than {@code bytes.length}. If {@code position} is greater than the    * new {@code length}, sets {@code position} to {@code length}.    *    * @param length    *          The new length of this range.    * @return this.    */
annotation|@
name|Override
specifier|public
name|PositionedByteRange
name|setLength
parameter_list|(
name|int
name|length
parameter_list|)
block|{
name|this
operator|.
name|position
operator|=
name|Math
operator|.
name|min
argument_list|(
name|position
argument_list|,
name|length
argument_list|)
expr_stmt|;
name|super
operator|.
name|setLength
argument_list|(
name|length
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getPosition
parameter_list|()
block|{
return|return
name|position
return|;
block|}
annotation|@
name|Override
specifier|public
name|PositionedByteRange
name|setPosition
parameter_list|(
name|int
name|position
parameter_list|)
block|{
name|this
operator|.
name|position
operator|=
name|position
expr_stmt|;
return|return
name|this
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getRemaining
parameter_list|()
block|{
return|return
name|length
operator|-
name|position
return|;
block|}
annotation|@
name|Override
specifier|public
name|byte
name|peek
parameter_list|()
block|{
return|return
name|bytes
index|[
name|offset
operator|+
name|position
index|]
return|;
block|}
annotation|@
name|Override
specifier|public
name|byte
name|get
parameter_list|()
block|{
return|return
name|get
argument_list|(
name|position
operator|++
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|PositionedByteRange
name|get
parameter_list|(
name|byte
index|[]
name|dst
parameter_list|)
block|{
if|if
condition|(
literal|0
operator|==
name|dst
operator|.
name|length
condition|)
block|{
return|return
name|this
return|;
block|}
return|return
name|this
operator|.
name|get
argument_list|(
name|dst
argument_list|,
literal|0
argument_list|,
name|dst
operator|.
name|length
argument_list|)
return|;
comment|// be clear we're calling self, not super
block|}
annotation|@
name|Override
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
block|{
if|if
condition|(
literal|0
operator|==
name|length
condition|)
block|{
return|return
name|this
return|;
block|}
name|super
operator|.
name|get
argument_list|(
name|this
operator|.
name|position
argument_list|,
name|dst
argument_list|,
name|offset
argument_list|,
name|length
argument_list|)
expr_stmt|;
name|this
operator|.
name|position
operator|+=
name|length
expr_stmt|;
return|return
name|this
return|;
block|}
comment|// java boilerplate
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
block|{
name|super
operator|.
name|get
argument_list|(
name|index
argument_list|,
name|dst
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
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
block|{
name|super
operator|.
name|get
argument_list|(
name|index
argument_list|,
name|dst
argument_list|,
name|offset
argument_list|,
name|length
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
annotation|@
name|Override
specifier|public
name|short
name|getShort
parameter_list|()
block|{
name|short
name|s
init|=
name|getShort
argument_list|(
name|position
argument_list|)
decl_stmt|;
name|position
operator|+=
name|Bytes
operator|.
name|SIZEOF_SHORT
expr_stmt|;
return|return
name|s
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getInt
parameter_list|()
block|{
name|int
name|i
init|=
name|getInt
argument_list|(
name|position
argument_list|)
decl_stmt|;
name|position
operator|+=
name|Bytes
operator|.
name|SIZEOF_INT
expr_stmt|;
return|return
name|i
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getLong
parameter_list|()
block|{
name|long
name|l
init|=
name|getLong
argument_list|(
name|position
argument_list|)
decl_stmt|;
name|position
operator|+=
name|Bytes
operator|.
name|SIZEOF_LONG
expr_stmt|;
return|return
name|l
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getVLong
parameter_list|()
block|{
name|long
name|p
init|=
name|getVLong
argument_list|(
name|position
argument_list|)
decl_stmt|;
name|position
operator|+=
name|getVLongSize
argument_list|(
name|p
argument_list|)
expr_stmt|;
return|return
name|p
return|;
block|}
annotation|@
name|Override
specifier|public
name|PositionedByteRange
name|setLimit
parameter_list|(
name|int
name|limit
parameter_list|)
block|{
name|this
operator|.
name|limit
operator|=
name|limit
expr_stmt|;
return|return
name|this
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getLimit
parameter_list|()
block|{
return|return
name|this
operator|.
name|limit
return|;
block|}
block|}
end_class

end_unit

