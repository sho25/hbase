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
comment|/**  * An {@code DataType} that encodes variable-length values encoded using  * {@link org.apache.hadoop.hbase.util.Bytes#putBytes(byte[], int, byte[], int, int)}.   * Includes a termination marker following the raw {@code byte[]} value. Intended to make it easier   * to transition away from direct use of {@link org.apache.hadoop.hbase.util.Bytes}.  * @see org.apache.hadoop.hbase.util.Bytes#putBytes(byte[], int, byte[], int, int)  * @see RawBytes  * @see OrderedBlob  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Public
specifier|public
class|class
name|RawBytesTerminated
extends|extends
name|TerminatedWrapper
argument_list|<
name|byte
index|[]
argument_list|>
block|{
comment|/**    * Create a {@code RawBytesTerminated} using the specified terminator and    * {@code order}.    * @throws IllegalArgumentException if {@code term} is {@code null} or empty.    */
specifier|public
name|RawBytesTerminated
parameter_list|(
name|Order
name|order
parameter_list|,
name|byte
index|[]
name|term
parameter_list|)
block|{
name|super
argument_list|(
operator|new
name|RawBytes
argument_list|(
name|order
argument_list|)
argument_list|,
name|term
argument_list|)
expr_stmt|;
block|}
comment|/**    * Create a {@code RawBytesTerminated} using the specified terminator and    * {@code order}.    * @throws IllegalArgumentException if {@code term} is {@code null} or empty.    */
specifier|public
name|RawBytesTerminated
parameter_list|(
name|Order
name|order
parameter_list|,
name|String
name|term
parameter_list|)
block|{
name|super
argument_list|(
operator|new
name|RawBytes
argument_list|(
name|order
argument_list|)
argument_list|,
name|term
argument_list|)
expr_stmt|;
block|}
comment|/**    * Create a {@code RawBytesTerminated} using the specified terminator.    * @throws IllegalArgumentException if {@code term} is {@code null} or empty.    */
specifier|public
name|RawBytesTerminated
parameter_list|(
name|byte
index|[]
name|term
parameter_list|)
block|{
name|super
argument_list|(
operator|new
name|RawBytes
argument_list|()
argument_list|,
name|term
argument_list|)
expr_stmt|;
block|}
comment|/**    * Create a {@code RawBytesTerminated} using the specified terminator.    * @throws IllegalArgumentException if {@code term} is {@code null} or empty.    */
specifier|public
name|RawBytesTerminated
parameter_list|(
name|String
name|term
parameter_list|)
block|{
name|super
argument_list|(
operator|new
name|RawBytes
argument_list|()
argument_list|,
name|term
argument_list|)
expr_stmt|;
block|}
comment|/**    * Read a {@code byte[]} from the buffer {@code src}.    */
specifier|public
name|byte
index|[]
name|decode
parameter_list|(
name|PositionedByteRange
name|src
parameter_list|,
name|int
name|length
parameter_list|)
block|{
return|return
operator|(
operator|(
name|RawBytes
operator|)
name|wrapped
operator|)
operator|.
name|decode
argument_list|(
name|src
argument_list|,
name|length
argument_list|)
return|;
block|}
comment|/**    * Write {@code val} into {@code dst}, respecting {@code offset} and    * {@code length}.    * @return number of bytes written.    */
specifier|public
name|int
name|encode
parameter_list|(
name|PositionedByteRange
name|dst
parameter_list|,
name|byte
index|[]
name|val
parameter_list|,
name|int
name|voff
parameter_list|,
name|int
name|vlen
parameter_list|)
block|{
return|return
operator|(
operator|(
name|RawBytes
operator|)
name|wrapped
operator|)
operator|.
name|encode
argument_list|(
name|dst
argument_list|,
name|val
argument_list|,
name|voff
argument_list|,
name|vlen
argument_list|)
return|;
block|}
block|}
end_class

end_unit

