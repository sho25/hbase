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

begin_comment
comment|/**  * Implementations of this interface will keep and return to clients   * implementations of classes providing API to execute  * coordinated operations. This interface is client-sise, so it does NOT  * include method to retrieve the particular consensus providers.  *  * For each coarse-grained area of operations there will be a separate  * interface with implementation, providing API for relevant operations  * requiring coordination.  *  * Property hbase.consensus.provider.class in hbase-site.xml controls  * which provider to use.  */
end_comment

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
interface|interface
name|ConsensusProvider
block|{
comment|/**    * Initialize consensus service.    * @param server server instance to run within.    */
name|void
name|initialize
parameter_list|(
name|Server
name|server
parameter_list|)
function_decl|;
comment|/**    * Starts consensus service.    */
name|void
name|start
parameter_list|()
function_decl|;
comment|/**    * Stop consensus provider.    */
name|void
name|stop
parameter_list|()
function_decl|;
comment|/**    * @return instance of Server consensus runs within    */
name|Server
name|getServer
parameter_list|()
function_decl|;
block|}
end_interface

end_unit

