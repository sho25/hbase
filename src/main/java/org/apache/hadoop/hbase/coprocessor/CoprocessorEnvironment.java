begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Copyright 2010 The Apache Software Foundation  *  * Licensed under the Apache License, Version 2.0 (the "License");  * you may not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|coprocessor
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
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|client
operator|.
name|HTableInterface
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
name|regionserver
operator|.
name|HRegion
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
name|regionserver
operator|.
name|RegionServerServices
import|;
end_import

begin_comment
comment|/**  * Coprocessor environment state.  */
end_comment

begin_interface
specifier|public
interface|interface
name|CoprocessorEnvironment
block|{
comment|/** @return the Coprocessor interface version */
specifier|public
name|int
name|getVersion
parameter_list|()
function_decl|;
comment|/** @return the HBase version as a string (e.g. "0.21.0") */
specifier|public
name|String
name|getHBaseVersion
parameter_list|()
function_decl|;
comment|/** @return the region associated with this coprocessor */
specifier|public
name|HRegion
name|getRegion
parameter_list|()
function_decl|;
comment|/** @return reference to the region server services */
specifier|public
name|RegionServerServices
name|getRegionServerServices
parameter_list|()
function_decl|;
comment|/**    * @return an interface for accessing the given table    * @throws IOException    */
specifier|public
name|HTableInterface
name|getTable
parameter_list|(
name|byte
index|[]
name|tableName
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|// environment variables
comment|/**    * Get an environment variable    * @param key the key    * @return the object corresponding to the environment variable, if set    */
specifier|public
name|Object
name|get
parameter_list|(
name|Object
name|key
parameter_list|)
function_decl|;
comment|/**    * Set an environment variable    * @param key the key    * @param value the value    */
specifier|public
name|void
name|put
parameter_list|(
name|Object
name|key
parameter_list|,
name|Object
name|value
parameter_list|)
function_decl|;
comment|/**    * Remove an environment variable    * @param key the key    * @return the object corresponding to the environment variable, if set    */
specifier|public
name|Object
name|remove
parameter_list|(
name|Object
name|key
parameter_list|)
function_decl|;
block|}
end_interface

end_unit

