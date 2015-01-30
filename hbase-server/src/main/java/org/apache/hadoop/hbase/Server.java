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
name|hadoop
operator|.
name|hbase
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
name|client
operator|.
name|ClusterConnection
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
name|zookeeper
operator|.
name|MetaTableLocator
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
name|zookeeper
operator|.
name|ZooKeeperWatcher
import|;
end_import

begin_comment
comment|/**  * Defines the set of shared functions implemented by HBase servers (Masters  * and RegionServers).  */
end_comment

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
interface|interface
name|Server
extends|extends
name|Abortable
extends|,
name|Stoppable
block|{
comment|/**    * Gets the configuration object for this server.    */
name|Configuration
name|getConfiguration
parameter_list|()
function_decl|;
comment|/**    * Gets the ZooKeeper instance for this server.    */
name|ZooKeeperWatcher
name|getZooKeeper
parameter_list|()
function_decl|;
comment|/**    * Returns a reference to the servers' cluster connection.    *    * Important note: this method returns a reference to Connection which is managed    * by Server itself, so callers must NOT attempt to close connection obtained.    */
name|ClusterConnection
name|getConnection
parameter_list|()
function_decl|;
comment|/**    * Returns instance of {@link org.apache.hadoop.hbase.zookeeper.MetaTableLocator}    * running inside this server. This MetaServerLocator is started and stopped by server, clients    * shouldn't manage it's lifecycle.    * @return instance of {@link MetaTableLocator} associated with this server.    */
name|MetaTableLocator
name|getMetaTableLocator
parameter_list|()
function_decl|;
comment|/**    * @return The unique server name for this server.    */
name|ServerName
name|getServerName
parameter_list|()
function_decl|;
comment|/**    * Get CoordinatedStateManager instance for this server.    */
name|CoordinatedStateManager
name|getCoordinatedStateManager
parameter_list|()
function_decl|;
comment|/**    * @return The {@link ChoreService} instance for this server    */
name|ChoreService
name|getChoreService
parameter_list|()
function_decl|;
block|}
end_interface

end_unit

