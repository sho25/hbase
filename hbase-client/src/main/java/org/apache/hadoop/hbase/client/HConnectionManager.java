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
name|client
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
name|commons
operator|.
name|logging
operator|.
name|Log
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
name|ZooKeeperConnectionException
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
name|security
operator|.
name|User
import|;
end_import

begin_comment
comment|/**  * A non-instantiable class that manages creation of {@link HConnection}s.  *<p>The simplest way to use this class is by using {@link #createConnection(Configuration)}.  * This creates a new {@link HConnection} to the cluster that is managed by the caller.  * From this {@link HConnection} {@link HTableInterface} implementations are retrieved  * with {@link HConnection#getTable(byte[])}. Example:  *<pre>  * {@code  * HConnection connection = HConnectionManager.createConnection(config);  * HTableInterface table = connection.getTable("table1");  * try {  *   // Use the table as needed, for a single operation and a single thread  * } finally {  *   table.close();  *   connection.close();  * }  * }</pre>  *<p>The following logic and API will be removed in the future:  *<p>This class has a static Map of {@link HConnection} instances keyed by  * {@link Configuration}; all invocations of {@link #getConnection(Configuration)}  * that pass the same {@link Configuration} instance will be returned the same  * {@link  HConnection} instance (Adding properties to a Configuration  * instance does not change its object identity; for more on how this is done see  * {@link HConnectionKey}).  Sharing {@link HConnection}  * instances is usually what you want; all clients of the {@link HConnection}  * instances share the HConnections' cache of Region locations rather than each  * having to discover for itself the location of meta, etc.  It makes  * sense for the likes of the pool of HTables class {@link HTablePool}, for  * instance (If concerned that a single {@link HConnection} is insufficient  * for sharing amongst clients in say an heavily-multithreaded environment,  * in practise its not proven to be an issue.  Besides, {@link HConnection} is  * implemented atop Hadoop RPC and as of this writing, Hadoop RPC does a  * connection per cluster-member, exclusively).  *  *<p>But sharing connections makes clean up of {@link HConnection} instances a little awkward.  * Currently, clients cleanup by calling {@link #deleteConnection(Configuration)}. This will  * shutdown the zookeeper connection the HConnection was using and clean up all  * HConnection resources as well as stopping proxies to servers out on the  * cluster. Not running the cleanup will not end the world; it'll  * just stall the closeup some and spew some zookeeper connection failed  * messages into the log.  Running the cleanup on a {@link HConnection} that is  * subsequently used by another will cause breakage so be careful running  * cleanup.  *<p>To create a {@link HConnection} that is not shared by others, you can  * create a new {@link Configuration} instance, pass this new instance to  * {@link #getConnection(Configuration)}, and then when done, close it up by  * doing something like the following:  *<pre>  * {@code  * Configuration newConfig = new Configuration(originalConf);  * HConnection connection = HConnectionManager.getConnection(newConfig);  * // Use the connection to your hearts' delight and then when done...  * HConnectionManager.deleteConnection(newConfig, true);  * }  *</pre>  *<p>Cleanup used to be done inside in a shutdown hook.  On startup we'd  * register a shutdown hook that called {@link #deleteAllConnections()}  * on its way out but the order in which shutdown hooks run is not defined so  * were problematic for clients of HConnection that wanted to register their  * own shutdown hooks so we removed ours though this shifts the onus for  * cleanup to the client.  */
end_comment

begin_class
annotation|@
name|SuppressWarnings
argument_list|(
literal|"serial"
argument_list|)
annotation|@
name|InterfaceAudience
operator|.
name|Public
annotation|@
name|InterfaceStability
operator|.
name|Evolving
specifier|public
class|class
name|HConnectionManager
block|{
annotation|@
name|Deprecated
specifier|public
specifier|static
specifier|final
name|String
name|RETRIES_BY_SERVER_KEY
init|=
name|ConnectionManager
operator|.
name|RETRIES_BY_SERVER_KEY
decl_stmt|;
annotation|@
name|Deprecated
specifier|public
specifier|static
specifier|final
name|int
name|MAX_CACHED_CONNECTION_INSTANCES
init|=
name|ConnectionManager
operator|.
name|MAX_CACHED_CONNECTION_INSTANCES
decl_stmt|;
comment|/*    * Non-instantiable.    */
specifier|private
name|HConnectionManager
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
block|}
comment|/**    * Get the connection that goes with the passed<code>conf</code> configuration instance.    * If no current connection exists, method creates a new connection and keys it using    * connection-specific properties from the passed {@link Configuration}; see    * {@link HConnectionKey}.    * @param conf configuration    * @return HConnection object for<code>conf</code>    * @throws ZooKeeperConnectionException    */
specifier|public
specifier|static
name|HConnection
name|getConnection
parameter_list|(
specifier|final
name|Configuration
name|conf
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|ConnectionManager
operator|.
name|getConnectionInternal
argument_list|(
name|conf
argument_list|)
return|;
block|}
comment|/**    * Create a new HConnection instance using the passed<code>conf</code> instance.    *<p>Note: This bypasses the usual HConnection life cycle management done by    * {@link #getConnection(Configuration)}. The caller is responsible for    * calling {@link HConnection#close()} on the returned connection instance.    *    * This is the recommended way to create HConnections.    * {@code    * HConnection connection = HConnectionManager.createConnection(conf);    * HTableInterface table = connection.getTable("mytable");    * table.get(...);    * ...    * table.close();    * connection.close();    * }    *    * @param conf configuration    * @return HConnection object for<code>conf</code>    * @throws ZooKeeperConnectionException    */
specifier|public
specifier|static
name|HConnection
name|createConnection
parameter_list|(
name|Configuration
name|conf
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|ConnectionManager
operator|.
name|createConnectionInternal
argument_list|(
name|conf
argument_list|)
return|;
block|}
comment|/**    * Create a new HConnection instance using the passed<code>conf</code> instance.    *<p>Note: This bypasses the usual HConnection life cycle management done by    * {@link #getConnection(Configuration)}. The caller is responsible for    * calling {@link HConnection#close()} on the returned connection instance.    * This is the recommended way to create HConnections.    * {@code    * ExecutorService pool = ...;    * HConnection connection = HConnectionManager.createConnection(conf, pool);    * HTableInterface table = connection.getTable("mytable");    * table.get(...);    * ...    * table.close();    * connection.close();    * }    * @param conf configuration    * @param pool the thread pool to use for batch operation in HTables used via this HConnection    * @return HConnection object for<code>conf</code>    * @throws ZooKeeperConnectionException    */
specifier|public
specifier|static
name|HConnection
name|createConnection
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|ExecutorService
name|pool
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|ConnectionManager
operator|.
name|createConnection
argument_list|(
name|conf
argument_list|,
name|pool
argument_list|)
return|;
block|}
comment|/**    * Create a new HConnection instance using the passed<code>conf</code> instance.    *<p>Note: This bypasses the usual HConnection life cycle management done by    * {@link #getConnection(Configuration)}. The caller is responsible for    * calling {@link HConnection#close()} on the returned connection instance.    * This is the recommended way to create HConnections.    * {@code    * ExecutorService pool = ...;    * HConnection connection = HConnectionManager.createConnection(conf, pool);    * HTableInterface table = connection.getTable("mytable");    * table.get(...);    * ...    * table.close();    * connection.close();    * }    * @param conf configuration    * @param user the user the connection is for    * @return HConnection object for<code>conf</code>    * @throws ZooKeeperConnectionException    */
specifier|public
specifier|static
name|HConnection
name|createConnection
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|User
name|user
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|ConnectionManager
operator|.
name|createConnection
argument_list|(
name|conf
argument_list|,
name|user
argument_list|)
return|;
block|}
comment|/**    * Create a new HConnection instance using the passed<code>conf</code> instance.    *<p>Note: This bypasses the usual HConnection life cycle management done by    * {@link #getConnection(Configuration)}. The caller is responsible for    * calling {@link HConnection#close()} on the returned connection instance.    * This is the recommended way to create HConnections.    * {@code    * ExecutorService pool = ...;    * HConnection connection = HConnectionManager.createConnection(conf, pool);    * HTableInterface table = connection.getTable("mytable");    * table.get(...);    * ...    * table.close();    * connection.close();    * }    * @param conf configuration    * @param pool the thread pool to use for batch operation in HTables used via this HConnection    * @param user the user the connection is for    * @return HConnection object for<code>conf</code>    * @throws ZooKeeperConnectionException    */
specifier|public
specifier|static
name|HConnection
name|createConnection
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|ExecutorService
name|pool
parameter_list|,
name|User
name|user
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|ConnectionManager
operator|.
name|createConnection
argument_list|(
name|conf
argument_list|,
name|pool
argument_list|,
name|user
argument_list|)
return|;
block|}
annotation|@
name|Deprecated
specifier|static
name|HConnection
name|createConnection
parameter_list|(
specifier|final
name|Configuration
name|conf
parameter_list|,
specifier|final
name|boolean
name|managed
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|ConnectionManager
operator|.
name|createConnection
argument_list|(
name|conf
argument_list|,
name|managed
argument_list|)
return|;
block|}
annotation|@
name|Deprecated
specifier|static
name|ClusterConnection
name|createConnection
parameter_list|(
specifier|final
name|Configuration
name|conf
parameter_list|,
specifier|final
name|boolean
name|managed
parameter_list|,
specifier|final
name|ExecutorService
name|pool
parameter_list|,
specifier|final
name|User
name|user
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|ConnectionManager
operator|.
name|createConnection
argument_list|(
name|conf
argument_list|,
name|managed
argument_list|,
name|pool
argument_list|,
name|user
argument_list|)
return|;
block|}
comment|/**    * Delete connection information for the instance specified by passed configuration.    * If there are no more references to the designated connection connection, this method will    * then close connection to the zookeeper ensemble and let go of all associated resources.    *    * @param conf configuration whose identity is used to find {@link HConnection} instance.    * @deprecated    */
annotation|@
name|Deprecated
specifier|public
specifier|static
name|void
name|deleteConnection
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
name|ConnectionManager
operator|.
name|deleteConnection
argument_list|(
name|conf
argument_list|)
expr_stmt|;
block|}
comment|/**    * Cleanup a known stale connection.    * This will then close connection to the zookeeper ensemble and let go of all resources.    *    * @param connection    * @deprecated    */
annotation|@
name|Deprecated
specifier|public
specifier|static
name|void
name|deleteStaleConnection
parameter_list|(
name|HConnection
name|connection
parameter_list|)
block|{
name|ConnectionManager
operator|.
name|deleteStaleConnection
argument_list|(
name|connection
argument_list|)
expr_stmt|;
block|}
comment|/**    * Delete information for all connections. Close or not the connection, depending on the    *  staleConnection boolean and the ref count. By default, you should use it with    *  staleConnection to true.    * @deprecated    */
annotation|@
name|Deprecated
specifier|public
specifier|static
name|void
name|deleteAllConnections
parameter_list|(
name|boolean
name|staleConnection
parameter_list|)
block|{
name|ConnectionManager
operator|.
name|deleteAllConnections
argument_list|(
name|staleConnection
argument_list|)
expr_stmt|;
block|}
comment|/**    * Delete information for all connections..    * @deprecated kept for backward compatibility, but the behavior is broken. HBASE-8983    */
annotation|@
name|Deprecated
specifier|public
specifier|static
name|void
name|deleteAllConnections
parameter_list|()
block|{
name|ConnectionManager
operator|.
name|deleteAllConnections
argument_list|()
expr_stmt|;
block|}
comment|/**    * This convenience method invokes the given {@link HConnectable#connect}    * implementation using a {@link HConnection} instance that lasts just for the    * duration of the invocation.    *    * @param<T> the return type of the connect method    * @param connectable the {@link HConnectable} instance    * @return the value returned by the connect method    * @throws IOException    * @deprecated Internal method, do not use thru HConnectionManager.    */
annotation|@
name|InterfaceAudience
operator|.
name|Private
annotation|@
name|Deprecated
specifier|public
specifier|static
parameter_list|<
name|T
parameter_list|>
name|T
name|execute
parameter_list|(
name|HConnectable
argument_list|<
name|T
argument_list|>
name|connectable
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|ConnectionManager
operator|.
name|execute
argument_list|(
name|connectable
argument_list|)
return|;
block|}
comment|/**    * Set the number of retries to use serverside when trying to communicate    * with another server over {@link HConnection}.  Used updating catalog    * tables, etc.  Call this method before we create any Connections.    * @param c The Configuration instance to set the retries into.    * @param log Used to log what we set in here.    * @deprecated Internal method, do not use.    */
annotation|@
name|InterfaceAudience
operator|.
name|Private
annotation|@
name|Deprecated
specifier|public
specifier|static
name|void
name|setServerSideHConnectionRetries
parameter_list|(
specifier|final
name|Configuration
name|c
parameter_list|,
specifier|final
name|String
name|sn
parameter_list|,
specifier|final
name|Log
name|log
parameter_list|)
block|{
name|ConnectionUtils
operator|.
name|setServerSideHConnectionRetriesConfig
argument_list|(
name|c
argument_list|,
name|sn
argument_list|,
name|log
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

