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
name|HBaseInterfaceAudience
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
name|ServerName
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
name|util
operator|.
name|FutureUtils
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
comment|/**  * A cluster connection encapsulating lower level individual connections to actual servers and  * a connection to zookeeper. Connections are instantiated through the {@link ConnectionFactory}  * class. The lifecycle of the connection is managed by the caller, who has to {@link #close()}  * the connection to release the resources.  *  *<p> The connection object contains logic to find the master, locate regions out on the cluster,  * keeps a cache of locations and then knows how to re-calibrate after they move. The individual  * connections to servers, meta cache, zookeeper connection, etc are all shared by the  * {@link Table} and {@link Admin} instances obtained from this connection.  *  *<p> Connection creation is a heavy-weight operation. Connection implementations are thread-safe,  * so that the client can create a connection once, and share it with different threads.  * {@link Table} and {@link Admin} instances, on the other hand, are light-weight and are not  * thread-safe.  Typically, a single connection per client application is instantiated and every  * thread will obtain its own Table instance. Caching or pooling of {@link Table} and {@link Admin}  * is not recommended.  *  * @see ConnectionFactory  * @since 0.99.0  */
end_comment

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|Public
specifier|public
interface|interface
name|Connection
extends|extends
name|Abortable
extends|,
name|Closeable
block|{
comment|/*    * Implementation notes:    *  - Only allow new style of interfaces:    *   -- All table names are passed as TableName. No more byte[] and string arguments    *   -- Most of the classes with names H is deprecated in favor of non-H versions    *   (Table, Connection, etc)    *   -- Only real client-facing public methods are allowed    *  - Connection should contain only getTable(), getAdmin() kind of general methods.    */
comment|/**    * @return Configuration instance being used by this Connection instance.    */
name|Configuration
name|getConfiguration
parameter_list|()
function_decl|;
comment|/**    * Retrieve a Table implementation for accessing a table.    * The returned Table is not thread safe, a new instance should be created for each using thread.    * This is a lightweight operation, pooling or caching of the returned Table    * is neither required nor desired.    *<p>    * The caller is responsible for calling {@link Table#close()} on the returned    * table instance.    *<p>    * Since 0.98.1 this method no longer checks table existence. An exception    * will be thrown if the table does not exist only when the first operation is    * attempted.    * @param tableName the name of the table    * @return a Table to use for interactions with this table    */
specifier|default
name|Table
name|getTable
parameter_list|(
name|TableName
name|tableName
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|getTable
argument_list|(
name|tableName
argument_list|,
literal|null
argument_list|)
return|;
block|}
comment|/**    * Retrieve a Table implementation for accessing a table.    * The returned Table is not thread safe, a new instance should be created for each using thread.    * This is a lightweight operation, pooling or caching of the returned Table    * is neither required nor desired.    *<p>    * The caller is responsible for calling {@link Table#close()} on the returned    * table instance.    *<p>    * Since 0.98.1 this method no longer checks table existence. An exception    * will be thrown if the table does not exist only when the first operation is    * attempted.    *    * @param tableName the name of the table    * @param pool The thread pool to use for batch operations, null to use a default pool.    * @return a Table to use for interactions with this table    */
specifier|default
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
block|{
return|return
name|getTableBuilder
argument_list|(
name|tableName
argument_list|,
name|pool
argument_list|)
operator|.
name|build
argument_list|()
return|;
block|}
comment|/**    *<p>    * Retrieve a {@link BufferedMutator} for performing client-side buffering of writes. The    * {@link BufferedMutator} returned by this method is thread-safe. This BufferedMutator will    * use the Connection's ExecutorService. This object can be used for long lived operations.    *</p>    *<p>    * The caller is responsible for calling {@link BufferedMutator#close()} on    * the returned {@link BufferedMutator} instance.    *</p>    *<p>    * This accessor will use the connection's ExecutorService and will throw an    * exception in the main thread when an asynchronous exception occurs.    *    * @param tableName the name of the table    *    * @return a {@link BufferedMutator} for the supplied tableName.    */
specifier|default
name|BufferedMutator
name|getBufferedMutator
parameter_list|(
name|TableName
name|tableName
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|getBufferedMutator
argument_list|(
operator|new
name|BufferedMutatorParams
argument_list|(
name|tableName
argument_list|)
argument_list|)
return|;
block|}
comment|/**    * Retrieve a {@link BufferedMutator} for performing client-side buffering of writes. The    * {@link BufferedMutator} returned by this method is thread-safe. This object can be used for    * long lived table operations. The caller is responsible for calling    * {@link BufferedMutator#close()} on the returned {@link BufferedMutator} instance.    *    * @param params details on how to instantiate the {@code BufferedMutator}.    * @return a {@link BufferedMutator} for the supplied tableName.    */
name|BufferedMutator
name|getBufferedMutator
parameter_list|(
name|BufferedMutatorParams
name|params
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Retrieve a RegionLocator implementation to inspect region information on a table. The returned    * RegionLocator is not thread-safe, so a new instance should be created for each using thread.    *    * This is a lightweight operation.  Pooling or caching of the returned RegionLocator is neither    * required nor desired.    *<br>    * The caller is responsible for calling {@link RegionLocator#close()} on the returned    * RegionLocator instance.    *    * RegionLocator needs to be unmanaged    *    * @param tableName Name of the table who's region is to be examined    * @return A RegionLocator instance    */
name|RegionLocator
name|getRegionLocator
parameter_list|(
name|TableName
name|tableName
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Clear all the entries in the region location cache, for all the tables.    *<p/>    * If you only want to clear the cache for a specific table, use    * {@link RegionLocator#clearRegionLocationCache()}.    *<p/>    * This may cause performance issue so use it with caution.    */
name|void
name|clearRegionLocationCache
parameter_list|()
function_decl|;
comment|/**    * Retrieve an Admin implementation to administer an HBase cluster.    * The returned Admin is not guaranteed to be thread-safe.  A new instance should be created for    * each using thread.  This is a lightweight operation.  Pooling or caching of the returned    * Admin is not recommended.    *<br>    * The caller is responsible for calling {@link Admin#close()} on the returned    * Admin instance.    *    * @return an Admin instance for cluster administration    */
name|Admin
name|getAdmin
parameter_list|()
throws|throws
name|IOException
function_decl|;
annotation|@
name|Override
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
comment|/**    * Returns an {@link TableBuilder} for creating {@link Table}.    * @param tableName the name of the table    * @param pool the thread pool to use for requests like batch and scan    */
name|TableBuilder
name|getTableBuilder
parameter_list|(
name|TableName
name|tableName
parameter_list|,
name|ExecutorService
name|pool
parameter_list|)
function_decl|;
comment|/**    * Convert this connection to an {@link AsyncConnection}.    *<p/>    * Usually we will return the same instance if you call this method multiple times so you can    * consider this as a light-weighted operation.    */
name|AsyncConnection
name|toAsyncConnection
parameter_list|()
function_decl|;
comment|/**    * @return the cluster ID unique to this HBase cluster.    */
name|String
name|getClusterId
parameter_list|()
function_decl|;
comment|/**    * Retrieve an Hbck implementation to fix an HBase cluster.    * The returned Hbck is not guaranteed to be thread-safe. A new instance should be created by    * each thread. This is a lightweight operation. Pooling or caching of the returned Hbck instance    * is not recommended.    *<br>    * The caller is responsible for calling {@link Hbck#close()} on the returned Hbck instance.    *<br>    * This will be used mostly by hbck tool.    *    * @return an Hbck instance for active master. Active master is fetched from the zookeeper.    */
annotation|@
name|InterfaceAudience
operator|.
name|LimitedPrivate
argument_list|(
name|HBaseInterfaceAudience
operator|.
name|HBCK
argument_list|)
specifier|default
name|Hbck
name|getHbck
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|FutureUtils
operator|.
name|get
argument_list|(
name|toAsyncConnection
argument_list|()
operator|.
name|getHbck
argument_list|()
argument_list|)
return|;
block|}
comment|/**    * Retrieve an Hbck implementation to fix an HBase cluster.    * The returned Hbck is not guaranteed to be thread-safe. A new instance should be created by    * each thread. This is a lightweight operation. Pooling or caching of the returned Hbck instance    * is not recommended.    *<br>    * The caller is responsible for calling {@link Hbck#close()} on the returned Hbck instance.    *<br>    * This will be used mostly by hbck tool. This may only be used to by pass getting    * registered master from ZK. In situations where ZK is not available or active master is not    * registered with ZK and user can get master address by other means, master can be explicitly    * specified.    *    * @param masterServer explicit {@link ServerName} for master server    * @return an Hbck instance for a specified master server    */
annotation|@
name|InterfaceAudience
operator|.
name|LimitedPrivate
argument_list|(
name|HBaseInterfaceAudience
operator|.
name|HBCK
argument_list|)
specifier|default
name|Hbck
name|getHbck
parameter_list|(
name|ServerName
name|masterServer
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|toAsyncConnection
argument_list|()
operator|.
name|getHbck
argument_list|(
name|masterServer
argument_list|)
return|;
block|}
block|}
end_interface

end_unit

