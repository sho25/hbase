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
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|IOException
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
name|classification
operator|.
name|InterfaceAudience
import|;
end_import

begin_comment
comment|/**  * Get, remove and modify table descriptors.  * Used by servers to host descriptors.  */
end_comment

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
interface|interface
name|TableDescriptors
block|{
comment|/**    * @param tableName    * @return HTableDescriptor for tablename    * @throws IOException    */
name|HTableDescriptor
name|get
parameter_list|(
specifier|final
name|TableName
name|tableName
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * @param tableName    * @return TableDescriptor for tablename    * @throws IOException    */
name|TableDescriptor
name|getDescriptor
parameter_list|(
specifier|final
name|TableName
name|tableName
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Get Map of all NamespaceDescriptors for a given namespace.    * @return Map of all descriptors.    * @throws IOException    */
name|Map
argument_list|<
name|String
argument_list|,
name|HTableDescriptor
argument_list|>
name|getByNamespace
parameter_list|(
name|String
name|name
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Get Map of all HTableDescriptors. Populates the descriptor cache as a    * side effect.    * @return Map of all descriptors.    * @throws IOException    */
name|Map
argument_list|<
name|String
argument_list|,
name|HTableDescriptor
argument_list|>
name|getAll
parameter_list|()
throws|throws
name|IOException
function_decl|;
comment|/**    * Get Map of all TableDescriptors. Populates the descriptor cache as a    * side effect.    * @return Map of all descriptors.    * @throws IOException    */
name|Map
argument_list|<
name|String
argument_list|,
name|TableDescriptor
argument_list|>
name|getAllDescriptors
parameter_list|()
throws|throws
name|IOException
function_decl|;
comment|/**    * Add or update descriptor    * @param htd Descriptor to set into TableDescriptors    * @throws IOException    */
name|void
name|add
parameter_list|(
specifier|final
name|HTableDescriptor
name|htd
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Add or update descriptor    * @param htd Descriptor to set into TableDescriptors    * @throws IOException    */
name|void
name|add
parameter_list|(
specifier|final
name|TableDescriptor
name|htd
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * @param tablename    * @return Instance of table descriptor or null if none found.    * @throws IOException    */
name|HTableDescriptor
name|remove
parameter_list|(
specifier|final
name|TableName
name|tablename
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Enables the tabledescriptor cache    */
name|void
name|setCacheOn
parameter_list|()
throws|throws
name|IOException
function_decl|;
comment|/**    * Disables the tabledescriptor cache    */
name|void
name|setCacheOff
parameter_list|()
throws|throws
name|IOException
function_decl|;
block|}
end_interface

end_unit

