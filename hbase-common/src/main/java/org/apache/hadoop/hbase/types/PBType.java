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
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|CodedInputStream
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|CodedOutputStream
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|Message
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
name|yetus
operator|.
name|audience
operator|.
name|InterfaceAudience
import|;
end_import

begin_comment
comment|/**  * A base-class for {@link DataType} implementations backed by protobuf. See  * {@code PBKeyValue} in {@code hbase-examples} module.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Public
specifier|public
specifier|abstract
class|class
name|PBType
parameter_list|<
name|T
extends|extends
name|Message
parameter_list|>
implements|implements
name|DataType
argument_list|<
name|T
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
name|T
name|val
parameter_list|)
block|{
return|return
name|val
operator|.
name|getSerializedSize
argument_list|()
return|;
block|}
comment|/**    * Create a {@link CodedInputStream} from a {@link PositionedByteRange}. Be sure to update    * {@code src}'s position after consuming from the stream.    *<p>For example:    *<pre>    * Foo.Builder builder = ...    * CodedInputStream is = inputStreamFromByteRange(src);    * Foo ret = builder.mergeFrom(is).build();    * src.setPosition(src.getPosition() + is.getTotalBytesRead());    *</pre>    */
specifier|public
specifier|static
name|CodedInputStream
name|inputStreamFromByteRange
parameter_list|(
name|PositionedByteRange
name|src
parameter_list|)
block|{
return|return
name|CodedInputStream
operator|.
name|newInstance
argument_list|(
name|src
operator|.
name|getBytes
argument_list|()
argument_list|,
name|src
operator|.
name|getOffset
argument_list|()
operator|+
name|src
operator|.
name|getPosition
argument_list|()
argument_list|,
name|src
operator|.
name|getRemaining
argument_list|()
argument_list|)
return|;
block|}
comment|/**    * Create a {@link CodedOutputStream} from a {@link PositionedByteRange}. Be sure to update    * {@code dst}'s position after writing to the stream.    *<p>For example:    *<pre>    * CodedOutputStream os = outputStreamFromByteRange(dst);    * int before = os.spaceLeft(), after, written;    * val.writeTo(os);    * after = os.spaceLeft();    * written = before - after;    * dst.setPosition(dst.getPosition() + written);    *</pre>    */
specifier|public
specifier|static
name|CodedOutputStream
name|outputStreamFromByteRange
parameter_list|(
name|PositionedByteRange
name|dst
parameter_list|)
block|{
return|return
name|CodedOutputStream
operator|.
name|newInstance
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
name|dst
operator|.
name|getRemaining
argument_list|()
argument_list|)
return|;
block|}
block|}
end_class

end_unit

