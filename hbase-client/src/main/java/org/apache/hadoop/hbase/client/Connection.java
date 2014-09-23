begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  *   * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|client
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|Closeable
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
name|concurrent
operator|.
name|ExecutorService
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
name|Abortable
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
name|TableName
import|;
end_import

begin_comment
comment|/**  * A cluster connection encapsulating lower level individual connections to actual servers and  * a connection to zookeeper. Connections are instantiated through the {@link ConnectionFactory}  * class. The lifecycle of the connection is managed by the caller, who has to {@link #close()}  * the connection to release the resources.  *  *<p> The connection object contains logic to find the master, locate regions out on the cluster,  * keeps a cache of locations and then knows how to re-calibrate after they move. The individual  * connections to servers, meta cache, zookeeper connection, etc are all shared by the  * {@link Table} and {@link Admin} instances obtained from this connection.  *  *<p> Connection creation is a heavy-weight operation. Connection implementations are thread-safe,  * so that the client can create a connection once, and share it with different threads.  * {@link Table} and {@link Admin} instances, on the other hand, are light-weight and are not  * thread-safe.  Typically, a single connection per client application is instantiated and every  * thread will obtain its own Table instance. Caching or pooling of {@link Table} and {@link Admin}  * is not recommended.  *  *<p>This class replaces {@link HConnection}, which is now deprecated.  * @see ConnectionFactory  */
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
name|Connection
extends|extends
name|Abortable
extends|,
name|Closeable
block|{
comment|/*    * Implementation notes:    *  - Only allow new style of interfaces:    *   -- All table names are passed as TableName. No more byte[] and string arguments    *   -- Most of the classes with names H is deprecated in favor of non-H versions    *   (Table, Connection vs HConnection, etc)    *   -- Only real client-facing public methods are allowed    *  - Connection should contain only getTable(), gAdmin() kind of general methods.    */
comment|/**    * @return Configuration instance being used by this HConnection instance.    */
name|Configuration
name|getConfiguration
parameter_list|()
function_decl|;
comment|/**    * Retrieve a Table implementation for accessing a table.    * The returned Table is not thread safe, a new instance should be created for each using thread.    * This is a lightweight operation, pooling or caching of the returned Table    * is neither required nor desired.    *     * @param tableName the name of the table    * @return a Table to use for interactions with this table    */
name|Table
name|getTable
parameter_list|(
name|TableName
name|tableName
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Retrieve a Table implementation for accessing a table.    * The returned Table is not thread safe, a new instance should be created for each using thread.    * This is a lightweight operation, pooling or caching of the returned Table    * is neither required nor desired.    *    * @param tableName the name of the table    * @param pool The thread pool to use for batch operations, null to use a default pool.    * @return a Table to use for interactions with this table    */
name|Table
name|getTable
parameter_list|(
name|TableName
name|tableName
parameter_list|,
name|ExecutorService
name|pool
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Retrieve a RegionLocator implementation to inspect region information on a table. The returned    * RegionLocator is not thread-safe, so a new instance should be created for each using thread.    *    * This is a lightweight operation.  Pooling or caching of the returned RegionLocator is neither    * required nor desired.    *    * RegionLocator needs to be unmanaged    *    * @param tableName Name of the table who's region is to be examined    * @return A RegionLocator instance    */
specifier|public
name|RegionLocator
name|getRegionLocator
parameter_list|(
name|TableName
name|tableName
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Retrieve an Admin implementation to administer an HBase cluster.    * The returned Admin is not guaranteed to be thread-safe.  A new instance should be created for    * each using thread.  This is a lightweight operation.  Pooling or caching of the returned    * Admin is not recommended.    *    * @return an Admin instance for cluster administration    */
name|Admin
name|getAdmin
parameter_list|()
throws|throws
name|IOException
function_decl|;
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|()
throws|throws
name|IOException
function_decl|;
comment|/**    * Returns whether the connection is closed or not.    * @return true if this connection is closed    */
name|boolean
name|isClosed
parameter_list|()
function_decl|;
block|}
end_interface

end_unit

