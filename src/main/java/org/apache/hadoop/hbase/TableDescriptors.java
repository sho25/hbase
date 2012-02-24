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
name|FileNotFoundException
import|;
end_import

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

begin_comment
comment|/**  * Get, remove and modify table descriptors.  * Used by servers to host descriptors.  */
end_comment

begin_interface
specifier|public
interface|interface
name|TableDescriptors
block|{
comment|/**    * @param tablename    * @return HTableDescriptor for tablename    * @throws TableExistsException    * @throws FileNotFoundException    * @throws IOException    */
specifier|public
name|HTableDescriptor
name|get
parameter_list|(
specifier|final
name|String
name|tablename
parameter_list|)
throws|throws
name|FileNotFoundException
throws|,
name|IOException
function_decl|;
comment|/**    * @param tablename    * @return HTableDescriptor for tablename    * @throws TableExistsException    * @throws FileNotFoundException    * @throws IOException    */
specifier|public
name|HTableDescriptor
name|get
parameter_list|(
specifier|final
name|byte
index|[]
name|tablename
parameter_list|)
throws|throws
name|FileNotFoundException
throws|,
name|IOException
function_decl|;
comment|/**    * Get Map of all HTableDescriptors. Populates the descriptor cache as a    * side effect.    * @return Map of all descriptors.    * @throws IOException    */
specifier|public
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
comment|/**    * Add or update descriptor    * @param htd Descriptor to set into TableDescriptors    * @throws IOException    */
specifier|public
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
comment|/**    * @param tablename    * @return Instance of table descriptor or null if none found.    * @throws IOException    */
specifier|public
name|HTableDescriptor
name|remove
parameter_list|(
specifier|final
name|String
name|tablename
parameter_list|)
throws|throws
name|IOException
function_decl|;
block|}
end_interface

end_unit

