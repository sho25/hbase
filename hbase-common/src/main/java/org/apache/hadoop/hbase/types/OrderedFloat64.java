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
name|OrderedBytes
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
comment|/**  * A {@code double} of 64-bits using a fixed-length encoding. Built on  * {@link OrderedBytes#encodeFloat64(PositionedByteRange, double, Order)}.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Public
specifier|public
class|class
name|OrderedFloat64
extends|extends
name|OrderedBytesBase
argument_list|<
name|Double
argument_list|>
block|{
specifier|public
specifier|static
specifier|final
name|OrderedFloat64
name|ASCENDING
init|=
operator|new
name|OrderedFloat64
argument_list|(
name|Order
operator|.
name|ASCENDING
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|OrderedFloat64
name|DESCENDING
init|=
operator|new
name|OrderedFloat64
argument_list|(
name|Order
operator|.
name|DESCENDING
argument_list|)
decl_stmt|;
specifier|protected
name|OrderedFloat64
parameter_list|(
name|Order
name|order
parameter_list|)
block|{
name|super
argument_list|(
name|order
argument_list|)
expr_stmt|;
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
name|int
name|encodedLength
parameter_list|(
name|Double
name|val
parameter_list|)
block|{
return|return
literal|9
return|;
block|}
annotation|@
name|Override
specifier|public
name|Class
argument_list|<
name|Double
argument_list|>
name|encodedClass
parameter_list|()
block|{
return|return
name|Double
operator|.
name|class
return|;
block|}
annotation|@
name|Override
specifier|public
name|Double
name|decode
parameter_list|(
name|PositionedByteRange
name|src
parameter_list|)
block|{
return|return
name|OrderedBytes
operator|.
name|decodeFloat64
argument_list|(
name|src
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
name|Double
name|val
parameter_list|)
block|{
if|if
condition|(
literal|null
operator|==
name|val
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Null values not supported."
argument_list|)
throw|;
block|}
return|return
name|OrderedBytes
operator|.
name|encodeFloat64
argument_list|(
name|dst
argument_list|,
name|val
argument_list|,
name|order
argument_list|)
return|;
block|}
comment|/**    * Read a {@code double} value from the buffer {@code src}.    */
specifier|public
name|double
name|decodeDouble
parameter_list|(
name|PositionedByteRange
name|src
parameter_list|)
block|{
return|return
name|OrderedBytes
operator|.
name|decodeFloat64
argument_list|(
name|src
argument_list|)
return|;
block|}
comment|/**    * Write instance {@code val} into buffer {@code dst}.    */
specifier|public
name|int
name|encodeDouble
parameter_list|(
name|PositionedByteRange
name|dst
parameter_list|,
name|double
name|val
parameter_list|)
block|{
return|return
name|OrderedBytes
operator|.
name|encodeFloat64
argument_list|(
name|dst
argument_list|,
name|val
argument_list|,
name|order
argument_list|)
return|;
block|}
block|}
end_class

end_unit

