begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|conf
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
name|classification
operator|.
name|InterfaceAudience
import|;
end_import

begin_comment
comment|/**  * This extension to ConfigurationObserver allows the configuration to propagate to the children of  * the current {@link ConfigurationObserver}. This is the preferred way to make a class online  * configurable because it allows the user to configure the children in a recursive manner  * automatically.   */
end_comment

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
interface|interface
name|PropagatingConfigurationObserver
extends|extends
name|ConfigurationObserver
block|{
comment|/**    * Needs to be called to register the children to the manager.     * @param manager : to register to    */
name|void
name|registerChildren
parameter_list|(
name|ConfigurationManager
name|manager
parameter_list|)
function_decl|;
comment|/**    * Needs to be called to deregister the children from the manager.     * @param manager : to deregister from    */
name|void
name|deregisterChildren
parameter_list|(
name|ConfigurationManager
name|manager
parameter_list|)
function_decl|;
block|}
end_interface

end_unit

