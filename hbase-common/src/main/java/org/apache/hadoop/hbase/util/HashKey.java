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

begin_comment
comment|/**  * Used to calculate the hash {@link Hash} algorithms for Bloomfilters.  *  * @param<T> the type of HashKey  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
specifier|abstract
class|class
name|HashKey
parameter_list|<
name|T
parameter_list|>
block|{
specifier|protected
specifier|final
name|T
name|t
decl_stmt|;
specifier|public
name|HashKey
parameter_list|(
name|T
name|t
parameter_list|)
block|{
name|this
operator|.
name|t
operator|=
name|t
expr_stmt|;
block|}
comment|/**    * @param pos    * @return The byte at the given position in this HashKey    */
specifier|public
specifier|abstract
name|byte
name|get
parameter_list|(
name|int
name|pos
parameter_list|)
function_decl|;
comment|/**    * @return The number of bytes in this HashKey    */
specifier|public
specifier|abstract
name|int
name|length
parameter_list|()
function_decl|;
block|}
end_class

end_unit

