begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  *  * Licensed under the Apache License, Version 2.0 (the "License");  * you may not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|conf
operator|.
name|Configuration
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
name|java
operator|.
name|io
operator|.
name|IOException
import|;
end_import

begin_comment
comment|/**  * Coprocessor environment state.  */
end_comment

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|Public
annotation|@
name|InterfaceStability
operator|.
name|Evolving
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
comment|/** @return the loaded coprocessor instance */
specifier|public
name|Coprocessor
name|getInstance
parameter_list|()
function_decl|;
comment|/** @return the priority assigned to the loaded coprocessor */
specifier|public
name|int
name|getPriority
parameter_list|()
function_decl|;
comment|/** @return the load sequence number */
specifier|public
name|int
name|getLoadSequence
parameter_list|()
function_decl|;
comment|/** @return the configuration */
specifier|public
name|Configuration
name|getConfiguration
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
block|}
end_interface

end_unit

