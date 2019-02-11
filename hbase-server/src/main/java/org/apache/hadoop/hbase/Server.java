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
name|fs
operator|.
name|FileSystem
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
name|AsyncClusterConnection
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
name|AsyncConnection
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
name|Connection
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
name|ZKWatcher
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

begin_comment
comment|/**  * Defines a curated set of shared functions implemented by HBase servers (Masters  * and RegionServers). For use internally only. Be judicious adding API. Changes cause ripples  * through the code base.  */
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
name|ZKWatcher
name|getZooKeeper
parameter_list|()
function_decl|;
comment|/**    * Returns a reference to the servers' connection.    *    * Important note: this method returns a reference to Connection which is managed    * by Server itself, so callers must NOT attempt to close connection obtained.    */
name|Connection
name|getConnection
parameter_list|()
function_decl|;
name|Connection
name|createConnection
parameter_list|(
name|Configuration
name|conf
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Returns a reference to the servers' async connection.    *<p/>    * Important note: this method returns a reference to Connection which is managed by Server    * itself, so callers must NOT attempt to close connection obtained.    */
specifier|default
name|AsyncConnection
name|getAsyncConnection
parameter_list|()
block|{
return|return
name|getAsyncClusterConnection
argument_list|()
return|;
block|}
comment|/**    * Returns a reference to the servers' async cluster connection.    *<p/>    * Important note: this method returns a reference to Connection which is managed by Server    * itself, so callers must NOT attempt to close connection obtained.    */
name|AsyncClusterConnection
name|getAsyncClusterConnection
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
comment|/**    * @return Return the FileSystem object used (can return null!).    */
comment|// TODO: On Master, return Master's. On RegionServer, return RegionServers. The FileSystems
comment|// may differ. TODO.
specifier|default
name|FileSystem
name|getFileSystem
parameter_list|()
block|{
comment|// This default is pretty dodgy!
name|Configuration
name|c
init|=
name|getConfiguration
argument_list|()
decl_stmt|;
name|FileSystem
name|fs
init|=
literal|null
decl_stmt|;
try|try
block|{
if|if
condition|(
name|c
operator|!=
literal|null
condition|)
block|{
name|fs
operator|=
name|FileSystem
operator|.
name|get
argument_list|(
name|c
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
comment|// If an exception, just return null
block|}
return|return
name|fs
return|;
block|}
comment|/**    * @return True is the server is Stopping    */
comment|// Note: This method is not part of the Stoppable Interface.
specifier|default
name|boolean
name|isStopping
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
block|}
end_interface

end_unit

