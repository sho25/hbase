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
name|security
operator|.
name|User
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
name|UserProvider
import|;
end_import

begin_comment
comment|/**  * A non-instantiable class that manages creation of {@link Connection}s.  * Managing the lifecycle of the {@link Connection}s to the cluster is the responsibility of  * the caller.  * From this {@link Connection} {@link Table} implementations are retrieved  * with {@link Connection#getTable(TableName)}. Example:  *<pre>  * {@code  * Connection connection = ConnectionFactory.createConnection(config);  * Table table = connection.getTable(TableName.valueOf("table1"));  * try {  *   // Use the table as needed, for a single operation and a single thread  * } finally {  *   table.close();  *   connection.close();  * }  *</pre>  *   * Similarly, {@link Connection} also returns {@link RegionLocator} implementations.  *  * This class replaces {@link HConnectionManager}, which is now deprecated.  */
end_comment

begin_class
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
name|ConnectionFactory
block|{
comment|/** No public c.tors */
specifier|protected
name|ConnectionFactory
parameter_list|()
block|{   }
comment|/**    * Create a new Connection instance using the passed<code>conf</code> instance. Connection    * encapsulates all housekeeping for a connection to the cluster. All tables and interfaces    * created from returned connection share zookeeper connection, meta cache, and connections    * to region servers and masters.    * The caller is responsible for calling {@link Connection#close()} on the returned    * connection instance.    *    * Typical usage:    *<pre>    * Connection connection = ConnectionFactory.createConnection(conf);    * Table table = connection.getTable(TableName.valueOf("mytable"));    * try {    *   table.get(...);    *   ...    * } finally {    *   table.close();    *   connection.close();    * }    *</pre>    *    * @param conf configuration    * @return Connection object for<code>conf</code>    */
specifier|public
specifier|static
name|Connection
name|createConnection
parameter_list|(
name|Configuration
name|conf
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|createConnection
argument_list|(
name|conf
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
return|;
block|}
comment|/**    * Create a new Connection instance using the passed<code>conf</code> instance. Connection    * encapsulates all housekeeping for a connection to the cluster. All tables and interfaces    * created from returned connection share zookeeper connection, meta cache, and connections    * to region servers and masters.    * The caller is responsible for calling {@link Connection#close()} on the returned    * connection instance.    *    * Typical usage:    *<pre>    * Connection connection = ConnectionFactory.createConnection(conf);    * Table table = connection.getTable(TableName.valueOf("mytable"));    * try {    *   table.get(...);    *   ...    * } finally {    *   table.close();    *   connection.close();    * }    *</pre>    *    * @param conf configuration    * @param pool the thread pool to use for batch operations    * @return Connection object for<code>conf</code>    */
specifier|public
specifier|static
name|Connection
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
name|createConnection
argument_list|(
name|conf
argument_list|,
name|pool
argument_list|,
literal|null
argument_list|)
return|;
block|}
comment|/**    * Create a new Connection instance using the passed<code>conf</code> instance. Connection    * encapsulates all housekeeping for a connection to the cluster. All tables and interfaces    * created from returned connection share zookeeper connection, meta cache, and connections    * to region servers and masters.    * The caller is responsible for calling {@link Connection#close()} on the returned    * connection instance.    *    * Typical usage:    *<pre>    * Connection connection = ConnectionFactory.createConnection(conf);    * Table table = connection.getTable(TableName.valueOf("table1"));    * try {    *   table.get(...);    *   ...    * } finally {    *   table.close();    *   connection.close();    * }    *</pre>    *    * @param conf configuration    * @param user the user the connection is for    * @return Connection object for<code>conf</code>    */
specifier|public
specifier|static
name|Connection
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
name|createConnection
argument_list|(
name|conf
argument_list|,
literal|null
argument_list|,
name|user
argument_list|)
return|;
block|}
comment|/**    * Create a new Connection instance using the passed<code>conf</code> instance. Connection    * encapsulates all housekeeping for a connection to the cluster. All tables and interfaces    * created from returned connection share zookeeper connection, meta cache, and connections    * to region servers and masters.    * The caller is responsible for calling {@link Connection#close()} on the returned    * connection instance.    *    * Typical usage:    *<pre>    * Connection connection = ConnectionFactory.createConnection(conf);    * Table table = connection.getTable(TableName.valueOf("table1"));    * try {    *   table.get(...);    *   ...    * } finally {    *   table.close();    *   connection.close();    * }    *</pre>    *    * @param conf configuration    * @param user the user the connection is for    * @param pool the thread pool to use for batch operations    * @return Connection object for<code>conf</code>    */
specifier|public
specifier|static
name|Connection
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
if|if
condition|(
name|user
operator|==
literal|null
condition|)
block|{
name|UserProvider
name|provider
init|=
name|UserProvider
operator|.
name|instantiate
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|user
operator|=
name|provider
operator|.
name|getCurrent
argument_list|()
expr_stmt|;
block|}
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
block|}
end_class

end_unit

