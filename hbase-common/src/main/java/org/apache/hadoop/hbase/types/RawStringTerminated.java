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
name|yetus
operator|.
name|audience
operator|.
name|InterfaceAudience
import|;
end_import

begin_comment
comment|/**  * An {@code DataType} that encodes variable-length values encoded using  * {@link org.apache.hadoop.hbase.util.Bytes#toBytes(String)}.   * Includes a termination marker following the  * raw {@code byte[]} value. Intended to make it easier to transition  * away from direct use of {@link org.apache.hadoop.hbase.util.Bytes}.  * @see org.apache.hadoop.hbase.util.Bytes#toBytes(String)  * @see org.apache.hadoop.hbase.util.Bytes#toString(byte[], int, int)  * @see RawString  * @see OrderedString  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Public
specifier|public
class|class
name|RawStringTerminated
extends|extends
name|TerminatedWrapper
argument_list|<
name|String
argument_list|>
block|{
comment|/**    * Create a {@code RawStringTerminated} using the specified terminator and    * {@code order}.    * @throws IllegalArgumentException if {@code term} is {@code null} or empty.    */
specifier|public
name|RawStringTerminated
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
name|RawString
argument_list|(
name|order
argument_list|)
argument_list|,
name|term
argument_list|)
expr_stmt|;
block|}
comment|/**    * Create a {@code RawStringTerminated} using the specified terminator and    * {@code order}.    * @throws IllegalArgumentException if {@code term} is {@code null} or empty.    */
specifier|public
name|RawStringTerminated
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
name|RawString
argument_list|(
name|order
argument_list|)
argument_list|,
name|term
argument_list|)
expr_stmt|;
block|}
comment|/**    * Create a {@code RawStringTerminated} using the specified terminator.    * @throws IllegalArgumentException if {@code term} is {@code null} or empty.    */
specifier|public
name|RawStringTerminated
parameter_list|(
name|byte
index|[]
name|term
parameter_list|)
block|{
name|super
argument_list|(
operator|new
name|RawString
argument_list|(
name|Order
operator|.
name|ASCENDING
argument_list|)
argument_list|,
name|term
argument_list|)
expr_stmt|;
block|}
comment|/**    * Create a {@code RawStringTerminated} using the specified terminator.    * @throws IllegalArgumentException if {@code term} is {@code null} or empty.    */
specifier|public
name|RawStringTerminated
parameter_list|(
name|String
name|term
parameter_list|)
block|{
name|super
argument_list|(
operator|new
name|RawString
argument_list|(
name|Order
operator|.
name|ASCENDING
argument_list|)
argument_list|,
name|term
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

