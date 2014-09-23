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
name|procedure
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
name|hbase
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
name|hbase
operator|.
name|regionserver
operator|.
name|RegionServerServices
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|zookeeper
operator|.
name|KeeperException
import|;
end_import

begin_comment
comment|/**  * A life-cycle management interface for globally barriered procedures on  * region servers.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
annotation|@
name|InterfaceStability
operator|.
name|Evolving
specifier|public
specifier|abstract
class|class
name|RegionServerProcedureManager
extends|extends
name|ProcedureManager
block|{
comment|/**    * Initialize a globally barriered procedure for region servers.    *    * @param rss Region Server service interface    * @throws KeeperException    */
specifier|public
specifier|abstract
name|void
name|initialize
parameter_list|(
name|RegionServerServices
name|rss
parameter_list|)
throws|throws
name|KeeperException
function_decl|;
comment|/**    * Start accepting procedure requests.    */
specifier|public
specifier|abstract
name|void
name|start
parameter_list|()
function_decl|;
comment|/**    * Close<tt>this</tt> and all running procedure tasks    *    * @param force forcefully stop all running tasks    * @throws IOException    */
specifier|public
specifier|abstract
name|void
name|stop
parameter_list|(
name|boolean
name|force
parameter_list|)
throws|throws
name|IOException
function_decl|;
block|}
end_class

end_unit

