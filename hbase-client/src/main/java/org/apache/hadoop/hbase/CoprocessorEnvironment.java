begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *   http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
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
name|yetus
operator|.
name|audience
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
name|yetus
operator|.
name|audience
operator|.
name|InterfaceStability
import|;
end_import

begin_comment
comment|/**  * Coprocessor environment state.  */
end_comment

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|LimitedPrivate
argument_list|(
name|HBaseInterfaceAudience
operator|.
name|COPROC
argument_list|)
annotation|@
name|InterfaceStability
operator|.
name|Evolving
specifier|public
interface|interface
name|CoprocessorEnvironment
parameter_list|<
name|C
extends|extends
name|Coprocessor
parameter_list|>
block|{
comment|/** @return the Coprocessor interface version */
name|int
name|getVersion
parameter_list|()
function_decl|;
comment|/** @return the HBase version as a string (e.g. "0.21.0") */
name|String
name|getHBaseVersion
parameter_list|()
function_decl|;
comment|/** @return the loaded coprocessor instance */
name|C
name|getInstance
parameter_list|()
function_decl|;
comment|/** @return the priority assigned to the loaded coprocessor */
name|int
name|getPriority
parameter_list|()
function_decl|;
comment|/** @return the load sequence number */
name|int
name|getLoadSequence
parameter_list|()
function_decl|;
comment|/** @return the configuration */
name|Configuration
name|getConfiguration
parameter_list|()
function_decl|;
comment|/**    * @return the classloader for the loaded coprocessor instance    */
name|ClassLoader
name|getClassLoader
parameter_list|()
function_decl|;
block|}
end_interface

end_unit

