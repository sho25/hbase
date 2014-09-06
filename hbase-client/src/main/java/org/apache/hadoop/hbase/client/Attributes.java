begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|client
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
name|java
operator|.
name|util
operator|.
name|Map
import|;
end_import

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|Public
annotation|@
name|InterfaceStability
operator|.
name|Stable
specifier|public
interface|interface
name|Attributes
block|{
comment|/**    * Sets an attribute.    * In case value = null attribute is removed from the attributes map.    * Attribute names starting with _ indicate system attributes.    * @param name attribute name    * @param value attribute value    */
name|Attributes
name|setAttribute
parameter_list|(
name|String
name|name
parameter_list|,
name|byte
index|[]
name|value
parameter_list|)
function_decl|;
comment|/**    * Gets an attribute    * @param name attribute name    * @return attribute value if attribute is set,<tt>null</tt> otherwise    */
name|byte
index|[]
name|getAttribute
parameter_list|(
name|String
name|name
parameter_list|)
function_decl|;
comment|/**    * Gets all attributes    * @return unmodifiable map of all attributes    */
name|Map
argument_list|<
name|String
argument_list|,
name|byte
index|[]
argument_list|>
name|getAttributesMap
parameter_list|()
function_decl|;
block|}
end_interface

end_unit

