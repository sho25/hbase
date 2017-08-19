begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|types
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
name|hadoop
operator|.
name|hbase
operator|.
name|util
operator|.
name|Order
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
name|PositionedByteRange
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
name|SimplePositionedMutableByteRange
import|;
end_import

begin_comment
comment|/**  * Wraps an existing {@link DataType} implementation as a fixed-length  * version of itself. This has the useful side-effect of turning an existing  * {@link DataType} which is not {@code skippable} into a {@code skippable}  * variant.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Public
specifier|public
class|class
name|FixedLengthWrapper
parameter_list|<
name|T
parameter_list|>
implements|implements
name|DataType
argument_list|<
name|T
argument_list|>
block|{
specifier|protected
specifier|final
name|DataType
argument_list|<
name|T
argument_list|>
name|base
decl_stmt|;
specifier|protected
specifier|final
name|int
name|length
decl_stmt|;
comment|/**    * Create a fixed-length version of the {@code wrapped}.    * @param base the {@link DataType} to restrict to a fixed length.    * @param length the maximum length (in bytes) for encoded values.    */
specifier|public
name|FixedLengthWrapper
parameter_list|(
name|DataType
argument_list|<
name|T
argument_list|>
name|base
parameter_list|,
name|int
name|length
parameter_list|)
block|{
name|this
operator|.
name|base
operator|=
name|base
expr_stmt|;
name|this
operator|.
name|length
operator|=
name|length
expr_stmt|;
block|}
comment|/**    * Retrieve the maximum length (in bytes) of encoded values.    */
specifier|public
name|int
name|getLength
parameter_list|()
block|{
return|return
name|length
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isOrderPreserving
parameter_list|()
block|{
return|return
name|base
operator|.
name|isOrderPreserving
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|Order
name|getOrder
parameter_list|()
block|{
return|return
name|base
operator|.
name|getOrder
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isNullable
parameter_list|()
block|{
return|return
name|base
operator|.
name|isNullable
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isSkippable
parameter_list|()
block|{
return|return
literal|true
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|encodedLength
parameter_list|(
name|T
name|val
parameter_list|)
block|{
return|return
name|length
return|;
block|}
annotation|@
name|Override
specifier|public
name|Class
argument_list|<
name|T
argument_list|>
name|encodedClass
parameter_list|()
block|{
return|return
name|base
operator|.
name|encodedClass
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|skip
parameter_list|(
name|PositionedByteRange
name|src
parameter_list|)
block|{
name|src
operator|.
name|setPosition
argument_list|(
name|src
operator|.
name|getPosition
argument_list|()
operator|+
name|this
operator|.
name|length
argument_list|)
expr_stmt|;
return|return
name|this
operator|.
name|length
return|;
block|}
annotation|@
name|Override
specifier|public
name|T
name|decode
parameter_list|(
name|PositionedByteRange
name|src
parameter_list|)
block|{
if|if
condition|(
name|src
operator|.
name|getRemaining
argument_list|()
operator|<
name|length
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Not enough buffer remaining. src.offset: "
operator|+
name|src
operator|.
name|getOffset
argument_list|()
operator|+
literal|" src.length: "
operator|+
name|src
operator|.
name|getLength
argument_list|()
operator|+
literal|" src.position: "
operator|+
name|src
operator|.
name|getPosition
argument_list|()
operator|+
literal|" max length: "
operator|+
name|length
argument_list|)
throw|;
block|}
comment|// create a copy range limited to length bytes. boo.
name|PositionedByteRange
name|b
init|=
operator|new
name|SimplePositionedMutableByteRange
argument_list|(
name|length
argument_list|)
decl_stmt|;
name|src
operator|.
name|get
argument_list|(
name|b
operator|.
name|getBytes
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|base
operator|.
name|decode
argument_list|(
name|b
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|encode
parameter_list|(
name|PositionedByteRange
name|dst
parameter_list|,
name|T
name|val
parameter_list|)
block|{
if|if
condition|(
name|dst
operator|.
name|getRemaining
argument_list|()
operator|<
name|length
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Not enough buffer remaining. dst.offset: "
operator|+
name|dst
operator|.
name|getOffset
argument_list|()
operator|+
literal|" dst.length: "
operator|+
name|dst
operator|.
name|getLength
argument_list|()
operator|+
literal|" dst.position: "
operator|+
name|dst
operator|.
name|getPosition
argument_list|()
operator|+
literal|" max length: "
operator|+
name|length
argument_list|)
throw|;
block|}
name|int
name|written
init|=
name|base
operator|.
name|encode
argument_list|(
name|dst
argument_list|,
name|val
argument_list|)
decl_stmt|;
if|if
condition|(
name|written
operator|>
name|length
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Length of encoded value ("
operator|+
name|written
operator|+
literal|") exceeds max length ("
operator|+
name|length
operator|+
literal|")."
argument_list|)
throw|;
block|}
comment|// TODO: is the zero-padding appropriate?
for|for
control|(
init|;
name|written
operator|<
name|length
condition|;
name|written
operator|++
control|)
block|{
name|dst
operator|.
name|put
argument_list|(
operator|(
name|byte
operator|)
literal|0x00
argument_list|)
expr_stmt|;
block|}
return|return
name|written
return|;
block|}
block|}
end_class

end_unit

