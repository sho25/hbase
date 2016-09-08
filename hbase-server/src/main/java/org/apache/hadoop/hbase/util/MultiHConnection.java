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
name|util
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
name|List
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
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|LinkedBlockingQueue
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
name|ThreadLocalRandom
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
name|ThreadPoolExecutor
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
name|TimeUnit
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
name|commons
operator|.
name|logging
operator|.
name|LogFactory
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
name|HConstants
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
name|client
operator|.
name|ConnectionFactory
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
name|HTable
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
name|Row
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
name|coprocessor
operator|.
name|Batch
import|;
end_import

begin_comment
comment|/**  * Provides ability to create multiple Connection instances and allows to process a batch of  * actions using CHTable.doBatchWithCallback()  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|MultiHConnection
block|{
specifier|private
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|MultiHConnection
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|Connection
index|[]
name|connections
decl_stmt|;
specifier|private
specifier|final
name|Object
name|connectionsLock
init|=
operator|new
name|Object
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|int
name|noOfConnections
decl_stmt|;
specifier|private
name|ExecutorService
name|batchPool
decl_stmt|;
comment|/**    * Create multiple Connection instances and initialize a thread pool executor    * @param conf configuration    * @param noOfConnections total no of Connections to create    * @throws IOException if IO failure occurs    */
specifier|public
name|MultiHConnection
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|int
name|noOfConnections
parameter_list|)
throws|throws
name|IOException
block|{
name|this
operator|.
name|noOfConnections
operator|=
name|noOfConnections
expr_stmt|;
synchronized|synchronized
init|(
name|this
operator|.
name|connectionsLock
init|)
block|{
name|connections
operator|=
operator|new
name|Connection
index|[
name|noOfConnections
index|]
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|noOfConnections
condition|;
name|i
operator|++
control|)
block|{
name|Connection
name|conn
init|=
name|ConnectionFactory
operator|.
name|createConnection
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|connections
index|[
name|i
index|]
operator|=
name|conn
expr_stmt|;
block|}
block|}
name|createBatchPool
argument_list|(
name|conf
argument_list|)
expr_stmt|;
block|}
comment|/**    * Close the open connections and shutdown the batchpool    */
specifier|public
name|void
name|close
parameter_list|()
block|{
synchronized|synchronized
init|(
name|connectionsLock
init|)
block|{
if|if
condition|(
name|connections
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|Connection
name|conn
range|:
name|connections
control|)
block|{
if|if
condition|(
name|conn
operator|!=
literal|null
condition|)
block|{
try|try
block|{
name|conn
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Got exception in closing connection"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|conn
operator|=
literal|null
expr_stmt|;
block|}
block|}
block|}
name|connections
operator|=
literal|null
expr_stmt|;
block|}
block|}
if|if
condition|(
name|this
operator|.
name|batchPool
operator|!=
literal|null
operator|&&
operator|!
name|this
operator|.
name|batchPool
operator|.
name|isShutdown
argument_list|()
condition|)
block|{
name|this
operator|.
name|batchPool
operator|.
name|shutdown
argument_list|()
expr_stmt|;
try|try
block|{
if|if
condition|(
operator|!
name|this
operator|.
name|batchPool
operator|.
name|awaitTermination
argument_list|(
literal|10
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
condition|)
block|{
name|this
operator|.
name|batchPool
operator|.
name|shutdownNow
argument_list|()
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
name|this
operator|.
name|batchPool
operator|.
name|shutdownNow
argument_list|()
expr_stmt|;
block|}
block|}
block|}
comment|/**    * Randomly pick a connection and process the batch of actions for a given table    * @param actions the actions    * @param tableName table name    * @param results the results array    * @param callback to run when results are in    * @throws IOException If IO failure occurs    */
annotation|@
name|SuppressWarnings
argument_list|(
literal|"deprecation"
argument_list|)
specifier|public
parameter_list|<
name|R
parameter_list|>
name|void
name|processBatchCallback
parameter_list|(
name|List
argument_list|<
name|?
extends|extends
name|Row
argument_list|>
name|actions
parameter_list|,
name|TableName
name|tableName
parameter_list|,
name|Object
index|[]
name|results
parameter_list|,
name|Batch
operator|.
name|Callback
argument_list|<
name|R
argument_list|>
name|callback
parameter_list|)
throws|throws
name|IOException
block|{
comment|// Currently used by RegionStateStore
name|ClusterConnection
name|conn
init|=
operator|(
name|ClusterConnection
operator|)
name|connections
index|[
name|ThreadLocalRandom
operator|.
name|current
argument_list|()
operator|.
name|nextInt
argument_list|(
name|noOfConnections
argument_list|)
index|]
decl_stmt|;
name|HTable
operator|.
name|doBatchWithCallback
argument_list|(
name|actions
argument_list|,
name|results
argument_list|,
name|callback
argument_list|,
name|conn
argument_list|,
name|batchPool
argument_list|,
name|tableName
argument_list|)
expr_stmt|;
block|}
comment|// Copied from ConnectionImplementation.getBatchPool()
comment|// We should get rid of this when Connection.processBatchCallback is un-deprecated and provides
comment|// an API to manage a batch pool
specifier|private
name|void
name|createBatchPool
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
comment|// Use the same config for keep alive as in ConnectionImplementation.getBatchPool();
name|int
name|maxThreads
init|=
name|conf
operator|.
name|getInt
argument_list|(
literal|"hbase.multihconnection.threads.max"
argument_list|,
literal|256
argument_list|)
decl_stmt|;
if|if
condition|(
name|maxThreads
operator|==
literal|0
condition|)
block|{
name|maxThreads
operator|=
name|Runtime
operator|.
name|getRuntime
argument_list|()
operator|.
name|availableProcessors
argument_list|()
operator|*
literal|8
expr_stmt|;
block|}
name|long
name|keepAliveTime
init|=
name|conf
operator|.
name|getLong
argument_list|(
literal|"hbase.multihconnection.threads.keepalivetime"
argument_list|,
literal|60
argument_list|)
decl_stmt|;
name|LinkedBlockingQueue
argument_list|<
name|Runnable
argument_list|>
name|workQueue
init|=
operator|new
name|LinkedBlockingQueue
argument_list|<>
argument_list|(
name|maxThreads
operator|*
name|conf
operator|.
name|getInt
argument_list|(
name|HConstants
operator|.
name|HBASE_CLIENT_MAX_TOTAL_TASKS
argument_list|,
name|HConstants
operator|.
name|DEFAULT_HBASE_CLIENT_MAX_TOTAL_TASKS
argument_list|)
argument_list|)
decl_stmt|;
name|ThreadPoolExecutor
name|tpe
init|=
operator|new
name|ThreadPoolExecutor
argument_list|(
name|maxThreads
argument_list|,
name|maxThreads
argument_list|,
name|keepAliveTime
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|,
name|workQueue
argument_list|,
name|Threads
operator|.
name|newDaemonThreadFactory
argument_list|(
literal|"MultiHConnection"
operator|+
literal|"-shared-"
argument_list|)
argument_list|)
decl_stmt|;
name|tpe
operator|.
name|allowCoreThreadTimeOut
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|this
operator|.
name|batchPool
operator|=
name|tpe
expr_stmt|;
block|}
block|}
end_class

end_unit

