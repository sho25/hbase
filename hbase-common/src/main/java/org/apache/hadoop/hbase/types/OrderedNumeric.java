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
name|java
operator|.
name|math
operator|.
name|BigDecimal
import|;
end_import

begin_import
import|import
name|java
operator|.
name|math
operator|.
name|BigInteger
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
comment|/**  * An {@link Number} of arbitrary precision and variable-length encoding. The  * resulting length of encoded values is determined by the numerical (base  * 100) precision, not absolute value. Use this data type anywhere you would  * expect to use a {@code DECIMAL} type, a {@link BigDecimal}, a  * {@link BigInteger}, or any time you've parsed floating precision values  * from text. Built on {@link OrderedBytes#encodeNumeric(PositionedByteRange, BigDecimal, Order)}.  */
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
name|OrderedNumeric
extends|extends
name|OrderedBytesBase
argument_list|<
name|Number
argument_list|>
block|{
specifier|public
specifier|static
specifier|final
name|OrderedNumeric
name|ASCENDING
init|=
operator|new
name|OrderedNumeric
argument_list|(
name|Order
operator|.
name|ASCENDING
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|OrderedNumeric
name|DESCENDING
init|=
operator|new
name|OrderedNumeric
argument_list|(
name|Order
operator|.
name|DESCENDING
argument_list|)
decl_stmt|;
specifier|protected
name|OrderedNumeric
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
name|int
name|encodedLength
parameter_list|(
name|Number
name|val
parameter_list|)
block|{
comment|// TODO: this could be done better.
name|PositionedByteRange
name|buff
init|=
operator|new
name|SimplePositionedMutableByteRange
argument_list|(
literal|100
argument_list|)
decl_stmt|;
return|return
name|encode
argument_list|(
name|buff
argument_list|,
name|val
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|Class
argument_list|<
name|Number
argument_list|>
name|encodedClass
parameter_list|()
block|{
return|return
name|Number
operator|.
name|class
return|;
block|}
annotation|@
name|Override
specifier|public
name|Number
name|decode
parameter_list|(
name|PositionedByteRange
name|src
parameter_list|)
block|{
if|if
condition|(
name|OrderedBytes
operator|.
name|isNumericInfinite
argument_list|(
name|src
argument_list|)
operator|||
name|OrderedBytes
operator|.
name|isNumericNaN
argument_list|(
name|src
argument_list|)
condition|)
block|{
return|return
name|OrderedBytes
operator|.
name|decodeNumericAsDouble
argument_list|(
name|src
argument_list|)
return|;
block|}
return|return
name|OrderedBytes
operator|.
name|decodeNumericAsBigDecimal
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
name|Number
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
return|return
name|OrderedBytes
operator|.
name|encodeNull
argument_list|(
name|dst
argument_list|,
name|order
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
name|val
operator|instanceof
name|BigDecimal
condition|)
block|{
return|return
name|OrderedBytes
operator|.
name|encodeNumeric
argument_list|(
name|dst
argument_list|,
operator|(
name|BigDecimal
operator|)
name|val
argument_list|,
name|order
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
name|val
operator|instanceof
name|BigInteger
condition|)
block|{
return|return
name|OrderedBytes
operator|.
name|encodeNumeric
argument_list|(
name|dst
argument_list|,
operator|new
name|BigDecimal
argument_list|(
operator|(
name|BigInteger
operator|)
name|val
argument_list|)
argument_list|,
name|order
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
name|val
operator|instanceof
name|Double
operator|||
name|val
operator|instanceof
name|Float
condition|)
block|{
return|return
name|OrderedBytes
operator|.
name|encodeNumeric
argument_list|(
name|dst
argument_list|,
name|val
operator|.
name|doubleValue
argument_list|()
argument_list|,
name|order
argument_list|)
return|;
block|}
else|else
block|{
comment|// TODO: other instances of Numeric to consider?
return|return
name|OrderedBytes
operator|.
name|encodeNumeric
argument_list|(
name|dst
argument_list|,
name|val
operator|.
name|longValue
argument_list|()
argument_list|,
name|order
argument_list|)
return|;
block|}
block|}
comment|/**    * Read a {@code long} value from the buffer {@code src}.    */
specifier|public
name|long
name|decodeLong
parameter_list|(
name|PositionedByteRange
name|src
parameter_list|)
block|{
return|return
name|OrderedBytes
operator|.
name|decodeNumericAsLong
argument_list|(
name|src
argument_list|)
return|;
block|}
comment|/**    * Write instance {@code val} into buffer {@code dst}.    */
specifier|public
name|int
name|encodeLong
parameter_list|(
name|PositionedByteRange
name|dst
parameter_list|,
name|long
name|val
parameter_list|)
block|{
return|return
name|OrderedBytes
operator|.
name|encodeNumeric
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
name|decodeNumericAsLong
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
name|encodeNumeric
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

