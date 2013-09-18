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
name|classification
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

begin_comment
comment|/**  * An {@code DataType} for interacting with values encoded using  * {@link Bytes#putByte(byte[], int, byte)}. Intended to make it easier to  * transition away from direct use of {@link Bytes}.  * @see Bytes#putByte(byte[], int, byte)  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Public
annotation|@
name|InterfaceStability
operator|.
name|Evolving
specifier|public
class|class
name|RawByte
implements|implements
name|DataType
argument_list|<
name|Byte
argument_list|>
block|{
annotation|@
name|Override
specifier|public
name|boolean
name|isOrderPreserving
parameter_list|()
block|{
return|return
literal|false
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
literal|null
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
literal|false
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
name|Byte
name|val
parameter_list|)
block|{
return|return
name|Bytes
operator|.
name|SIZEOF_BYTE
return|;
block|}
annotation|@
name|Override
specifier|public
name|Class
argument_list|<
name|Byte
argument_list|>
name|encodedClass
parameter_list|()
block|{
return|return
name|Byte
operator|.
name|class
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
name|Bytes
operator|.
name|SIZEOF_BYTE
argument_list|)
expr_stmt|;
return|return
name|Bytes
operator|.
name|SIZEOF_BYTE
return|;
block|}
annotation|@
name|Override
specifier|public
name|Byte
name|decode
parameter_list|(
name|PositionedByteRange
name|src
parameter_list|)
block|{
name|byte
name|val
init|=
name|src
operator|.
name|getBytes
argument_list|()
index|[
name|src
operator|.
name|getOffset
argument_list|()
operator|+
name|src
operator|.
name|getPosition
argument_list|()
index|]
decl_stmt|;
name|skip
argument_list|(
name|src
argument_list|)
expr_stmt|;
return|return
name|val
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
name|Byte
name|val
parameter_list|)
block|{
name|Bytes
operator|.
name|putByte
argument_list|(
name|dst
operator|.
name|getBytes
argument_list|()
argument_list|,
name|dst
operator|.
name|getOffset
argument_list|()
operator|+
name|dst
operator|.
name|getPosition
argument_list|()
argument_list|,
name|val
argument_list|)
expr_stmt|;
return|return
name|skip
argument_list|(
name|dst
argument_list|)
return|;
block|}
comment|/**    * Read a {@code byte} value from the buffer {@code buff}.    */
specifier|public
name|byte
name|decodeByte
parameter_list|(
name|byte
index|[]
name|buff
parameter_list|,
name|int
name|offset
parameter_list|)
block|{
return|return
name|buff
index|[
name|offset
index|]
return|;
block|}
comment|/**    * Write instance {@code val} into buffer {@code buff}.    */
specifier|public
name|int
name|encodeByte
parameter_list|(
name|byte
index|[]
name|buff
parameter_list|,
name|int
name|offset
parameter_list|,
name|byte
name|val
parameter_list|)
block|{
return|return
name|Bytes
operator|.
name|putByte
argument_list|(
name|buff
argument_list|,
name|offset
argument_list|,
name|val
argument_list|)
return|;
block|}
block|}
end_class

end_unit

