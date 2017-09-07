begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/* * * Licensed to the Apache Software Foundation (ASF) under one * or more contributor license agreements.  See the NOTICE file * distributed with this work for additional information * regarding copyright ownership.  The ASF licenses this file * to you under the Apache License, Version 2.0 (the * "License"); you may not use this file except in compliance * with the License.  You may obtain a copy of the License at * *     http://www.apache.org/licenses/LICENSE-2.0 * * Unless required by applicable law or agreed to in writing, software * distributed under the License is distributed on an "AS IS" BASIS, * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. * See the License for the specific language governing permissions and * limitations under the License. */
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
name|replication
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
name|CompareOperator
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
name|shaded
operator|.
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|util
operator|.
name|concurrent
operator|.
name|ThreadFactoryBuilder
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
name|HColumnDescriptor
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
name|HTableDescriptor
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
name|NamespaceDescriptor
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
name|TableExistsException
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
name|Admin
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
name|Get
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
name|Result
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
name|ResultScanner
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
name|Scan
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
name|Table
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
name|filter
operator|.
name|CompareFilter
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
name|filter
operator|.
name|SingleColumnValueFilter
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
name|BloomType
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
name|Bytes
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
name|RetryCounter
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
name|RetryCounterFactory
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
name|io
operator|.
name|InterruptedIOException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|ArrayList
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Arrays
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashSet
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
name|Map
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Set
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
name|CountDownLatch
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
name|Executor
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

begin_comment
comment|/*  * Abstract class that provides an interface to the Replication Table. Which is currently  * being used for WAL offset tracking.  * The basic schema of this table will store each individual queue as a  * seperate row. The row key will be a unique identifier of the creating server's name and the  * queueId. Each queue must have the following two columns:  *  COL_QUEUE_OWNER: tracks which server is currently responsible for tracking the queue  *  COL_QUEUE_OWNER_HISTORY: a "|" delimited list of the previous server's that have owned this  *    queue. The most recent previous owner is the leftmost entry.  * They will also have columns mapping [WAL filename : offset]  * The most flexible method of interacting with the Replication Table is by calling  * getOrBlockOnReplicationTable() which will return a new copy of the Replication Table. It is up  * to the caller to close the returned table.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|abstract
class|class
name|ReplicationTableBase
block|{
comment|/** Name of the HBase Table used for tracking replication*/
specifier|public
specifier|static
specifier|final
name|TableName
name|REPLICATION_TABLE_NAME
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
name|NamespaceDescriptor
operator|.
name|SYSTEM_NAMESPACE_NAME_STR
argument_list|,
literal|"replication"
argument_list|)
decl_stmt|;
comment|// Column family and column names for Queues in the Replication Table
specifier|public
specifier|static
specifier|final
name|byte
index|[]
name|CF_QUEUE
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"q"
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|byte
index|[]
name|COL_QUEUE_OWNER
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"o"
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|byte
index|[]
name|COL_QUEUE_OWNER_HISTORY
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"h"
argument_list|)
decl_stmt|;
comment|// Column Descriptor for the Replication Table
specifier|private
specifier|static
specifier|final
name|HColumnDescriptor
name|REPLICATION_COL_DESCRIPTOR
init|=
operator|new
name|HColumnDescriptor
argument_list|(
name|CF_QUEUE
argument_list|)
operator|.
name|setMaxVersions
argument_list|(
literal|1
argument_list|)
operator|.
name|setInMemory
argument_list|(
literal|true
argument_list|)
operator|.
name|setScope
argument_list|(
name|HConstants
operator|.
name|REPLICATION_SCOPE_LOCAL
argument_list|)
comment|// TODO: Figure out which bloom filter to use
operator|.
name|setBloomFilterType
argument_list|(
name|BloomType
operator|.
name|NONE
argument_list|)
decl_stmt|;
comment|// The value used to delimit the queueId and server name inside of a queue's row key. Currently a
comment|// hyphen, because it is guaranteed that queueId (which is a cluster id) cannot contain hyphens.
comment|// See HBASE-11394.
specifier|public
specifier|static
specifier|final
name|String
name|ROW_KEY_DELIMITER
init|=
literal|"-"
decl_stmt|;
comment|// The value used to delimit server names in the queue history list
specifier|public
specifier|static
specifier|final
name|String
name|QUEUE_HISTORY_DELIMITER
init|=
literal|"|"
decl_stmt|;
comment|/*   * Make sure that HBase table operations for replication have a high number of retries. This is   * because the server is aborted if any HBase table operation fails. Each RPC will be attempted   * 3600 times before exiting. This provides each operation with 2 hours of retries   * before the server is aborted.   */
specifier|private
specifier|static
specifier|final
name|int
name|CLIENT_RETRIES
init|=
literal|3600
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|RPC_TIMEOUT
init|=
literal|2000
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|OPERATION_TIMEOUT
init|=
name|CLIENT_RETRIES
operator|*
name|RPC_TIMEOUT
decl_stmt|;
comment|// We only need a single thread to initialize the Replication Table
specifier|private
specifier|static
specifier|final
name|int
name|NUM_INITIALIZE_WORKERS
init|=
literal|1
decl_stmt|;
specifier|protected
specifier|final
name|Configuration
name|conf
decl_stmt|;
specifier|protected
specifier|final
name|Abortable
name|abortable
decl_stmt|;
specifier|private
specifier|final
name|Connection
name|connection
decl_stmt|;
specifier|private
specifier|final
name|Executor
name|executor
decl_stmt|;
specifier|private
specifier|volatile
name|CountDownLatch
name|replicationTableInitialized
decl_stmt|;
specifier|public
name|ReplicationTableBase
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|Abortable
name|abort
parameter_list|)
throws|throws
name|IOException
block|{
name|this
operator|.
name|conf
operator|=
operator|new
name|Configuration
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|this
operator|.
name|abortable
operator|=
name|abort
expr_stmt|;
name|decorateConf
argument_list|()
expr_stmt|;
name|this
operator|.
name|connection
operator|=
name|ConnectionFactory
operator|.
name|createConnection
argument_list|(
name|this
operator|.
name|conf
argument_list|)
expr_stmt|;
name|this
operator|.
name|executor
operator|=
name|setUpExecutor
argument_list|()
expr_stmt|;
name|this
operator|.
name|replicationTableInitialized
operator|=
operator|new
name|CountDownLatch
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|createReplicationTableInBackground
argument_list|()
expr_stmt|;
block|}
comment|/**    * Modify the connection's config so that operations run on the Replication Table have longer and    * a larger number of retries    */
specifier|private
name|void
name|decorateConf
parameter_list|()
block|{
name|this
operator|.
name|conf
operator|.
name|setInt
argument_list|(
name|HConstants
operator|.
name|HBASE_CLIENT_RETRIES_NUMBER
argument_list|,
name|CLIENT_RETRIES
argument_list|)
expr_stmt|;
block|}
comment|/**    * Sets up the thread pool executor used to build the Replication Table in the background    * @return the configured executor    */
specifier|private
name|Executor
name|setUpExecutor
parameter_list|()
block|{
name|ThreadPoolExecutor
name|tempExecutor
init|=
operator|new
name|ThreadPoolExecutor
argument_list|(
name|NUM_INITIALIZE_WORKERS
argument_list|,
name|NUM_INITIALIZE_WORKERS
argument_list|,
literal|100
argument_list|,
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|,
operator|new
name|LinkedBlockingQueue
argument_list|<>
argument_list|()
argument_list|)
decl_stmt|;
name|ThreadFactoryBuilder
name|tfb
init|=
operator|new
name|ThreadFactoryBuilder
argument_list|()
decl_stmt|;
name|tfb
operator|.
name|setNameFormat
argument_list|(
literal|"ReplicationTableExecutor-%d"
argument_list|)
expr_stmt|;
name|tfb
operator|.
name|setDaemon
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|tempExecutor
operator|.
name|setThreadFactory
argument_list|(
name|tfb
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|tempExecutor
return|;
block|}
comment|/**    * Get whether the Replication Table has been successfully initialized yet    * @return whether the Replication Table is initialized    */
specifier|public
name|boolean
name|getInitializationStatus
parameter_list|()
block|{
return|return
name|replicationTableInitialized
operator|.
name|getCount
argument_list|()
operator|==
literal|0
return|;
block|}
comment|/**    * Increases the RPC and operations timeouts for the Replication Table    */
specifier|private
name|Table
name|setReplicationTableTimeOuts
parameter_list|(
name|Table
name|replicationTable
parameter_list|)
block|{
name|replicationTable
operator|.
name|setRpcTimeout
argument_list|(
name|RPC_TIMEOUT
argument_list|)
expr_stmt|;
name|replicationTable
operator|.
name|setOperationTimeout
argument_list|(
name|OPERATION_TIMEOUT
argument_list|)
expr_stmt|;
return|return
name|replicationTable
return|;
block|}
comment|/**    * Build the row key for the given queueId. This will uniquely identify it from all other queues    * in the cluster.    * @param serverName The owner of the queue    * @param queueId String identifier of the queue    * @return String representation of the queue's row key    */
specifier|protected
name|String
name|buildQueueRowKey
parameter_list|(
name|String
name|serverName
parameter_list|,
name|String
name|queueId
parameter_list|)
block|{
return|return
name|queueId
operator|+
name|ROW_KEY_DELIMITER
operator|+
name|serverName
return|;
block|}
comment|/**    * Parse the original queueId from a row key    * @param rowKey String representation of a queue's row key    * @return the original queueId    */
specifier|protected
name|String
name|getRawQueueIdFromRowKey
parameter_list|(
name|String
name|rowKey
parameter_list|)
block|{
return|return
name|rowKey
operator|.
name|split
argument_list|(
name|ROW_KEY_DELIMITER
argument_list|)
index|[
literal|0
index|]
return|;
block|}
comment|/**    * Returns a queue's row key given either its raw or reclaimed queueId    *    * @param queueId queueId of the queue    * @return byte representation of the queue's row key    */
specifier|protected
name|byte
index|[]
name|queueIdToRowKey
parameter_list|(
name|String
name|serverName
parameter_list|,
name|String
name|queueId
parameter_list|)
block|{
comment|// Cluster id's are guaranteed to have no hyphens, so if the passed in queueId has no hyphen
comment|// then this is not a reclaimed queue.
if|if
condition|(
operator|!
name|queueId
operator|.
name|contains
argument_list|(
name|ROW_KEY_DELIMITER
argument_list|)
condition|)
block|{
return|return
name|Bytes
operator|.
name|toBytes
argument_list|(
name|buildQueueRowKey
argument_list|(
name|serverName
argument_list|,
name|queueId
argument_list|)
argument_list|)
return|;
comment|// If the queueId contained some hyphen it was reclaimed. In this case, the queueId is the
comment|// queue's row key
block|}
else|else
block|{
return|return
name|Bytes
operator|.
name|toBytes
argument_list|(
name|queueId
argument_list|)
return|;
block|}
block|}
comment|/**    * Creates a "|" delimited record of the queue's past region server owners.    *    * @param originalHistory the queue's original owner history    * @param oldServer the name of the server that used to own the queue    * @return the queue's new owner history    */
specifier|protected
name|String
name|buildClaimedQueueHistory
parameter_list|(
name|String
name|originalHistory
parameter_list|,
name|String
name|oldServer
parameter_list|)
block|{
return|return
name|oldServer
operator|+
name|QUEUE_HISTORY_DELIMITER
operator|+
name|originalHistory
return|;
block|}
comment|/**    * Get a list of all region servers that have outstanding replication queues. These servers could    * be alive, dead or from a previous run of the cluster.    * @return a list of server names    */
specifier|protected
name|List
argument_list|<
name|String
argument_list|>
name|getListOfReplicators
parameter_list|()
block|{
comment|// scan all of the queues and return a list of all unique OWNER values
name|Set
argument_list|<
name|String
argument_list|>
name|peerServers
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
decl_stmt|;
name|ResultScanner
name|allQueuesInCluster
init|=
literal|null
decl_stmt|;
try|try
init|(
name|Table
name|replicationTable
init|=
name|getOrBlockOnReplicationTable
argument_list|()
init|)
block|{
name|Scan
name|scan
init|=
operator|new
name|Scan
argument_list|()
decl_stmt|;
name|scan
operator|.
name|addColumn
argument_list|(
name|CF_QUEUE
argument_list|,
name|COL_QUEUE_OWNER
argument_list|)
expr_stmt|;
name|allQueuesInCluster
operator|=
name|replicationTable
operator|.
name|getScanner
argument_list|(
name|scan
argument_list|)
expr_stmt|;
for|for
control|(
name|Result
name|queue
range|:
name|allQueuesInCluster
control|)
block|{
name|peerServers
operator|.
name|add
argument_list|(
name|Bytes
operator|.
name|toString
argument_list|(
name|queue
operator|.
name|getValue
argument_list|(
name|CF_QUEUE
argument_list|,
name|COL_QUEUE_OWNER
argument_list|)
argument_list|)
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
name|String
name|errMsg
init|=
literal|"Failed getting list of replicators"
decl_stmt|;
name|abortable
operator|.
name|abort
argument_list|(
name|errMsg
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
if|if
condition|(
name|allQueuesInCluster
operator|!=
literal|null
condition|)
block|{
name|allQueuesInCluster
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
return|return
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|peerServers
argument_list|)
return|;
block|}
specifier|protected
name|List
argument_list|<
name|String
argument_list|>
name|getAllQueues
parameter_list|(
name|String
name|serverName
parameter_list|)
block|{
name|List
argument_list|<
name|String
argument_list|>
name|allQueues
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|ResultScanner
name|queueScanner
init|=
literal|null
decl_stmt|;
try|try
block|{
name|queueScanner
operator|=
name|getQueuesBelongingToServer
argument_list|(
name|serverName
argument_list|)
expr_stmt|;
for|for
control|(
name|Result
name|queue
range|:
name|queueScanner
control|)
block|{
name|String
name|rowKey
init|=
name|Bytes
operator|.
name|toString
argument_list|(
name|queue
operator|.
name|getRow
argument_list|()
argument_list|)
decl_stmt|;
comment|// If the queue does not have a Owner History, then we must be its original owner. So we
comment|// want to return its queueId in raw form
if|if
condition|(
name|Bytes
operator|.
name|toString
argument_list|(
name|queue
operator|.
name|getValue
argument_list|(
name|CF_QUEUE
argument_list|,
name|COL_QUEUE_OWNER_HISTORY
argument_list|)
argument_list|)
operator|.
name|length
argument_list|()
operator|==
literal|0
condition|)
block|{
name|allQueues
operator|.
name|add
argument_list|(
name|getRawQueueIdFromRowKey
argument_list|(
name|rowKey
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|allQueues
operator|.
name|add
argument_list|(
name|rowKey
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|allQueues
return|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|String
name|errMsg
init|=
literal|"Failed getting list of all replication queues for serverName="
operator|+
name|serverName
decl_stmt|;
name|abortable
operator|.
name|abort
argument_list|(
name|errMsg
argument_list|,
name|e
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
finally|finally
block|{
if|if
condition|(
name|queueScanner
operator|!=
literal|null
condition|)
block|{
name|queueScanner
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
block|}
specifier|protected
name|List
argument_list|<
name|String
argument_list|>
name|getLogsInQueue
parameter_list|(
name|String
name|serverName
parameter_list|,
name|String
name|queueId
parameter_list|)
block|{
name|String
name|rowKey
init|=
name|queueId
decl_stmt|;
if|if
condition|(
operator|!
name|queueId
operator|.
name|contains
argument_list|(
name|ROW_KEY_DELIMITER
argument_list|)
condition|)
block|{
name|rowKey
operator|=
name|buildQueueRowKey
argument_list|(
name|serverName
argument_list|,
name|queueId
argument_list|)
expr_stmt|;
block|}
return|return
name|getLogsInQueue
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|rowKey
argument_list|)
argument_list|)
return|;
block|}
specifier|protected
name|List
argument_list|<
name|String
argument_list|>
name|getLogsInQueue
parameter_list|(
name|byte
index|[]
name|rowKey
parameter_list|)
block|{
name|String
name|errMsg
init|=
literal|"Failed getting logs in queue queueId="
operator|+
name|Bytes
operator|.
name|toString
argument_list|(
name|rowKey
argument_list|)
decl_stmt|;
try|try
init|(
name|Table
name|replicationTable
init|=
name|getOrBlockOnReplicationTable
argument_list|()
init|)
block|{
name|Get
name|getQueue
init|=
operator|new
name|Get
argument_list|(
name|rowKey
argument_list|)
decl_stmt|;
name|Result
name|queue
init|=
name|replicationTable
operator|.
name|get
argument_list|(
name|getQueue
argument_list|)
decl_stmt|;
if|if
condition|(
name|queue
operator|==
literal|null
operator|||
name|queue
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|abortable
operator|.
name|abort
argument_list|(
name|errMsg
argument_list|,
operator|new
name|ReplicationException
argument_list|(
name|errMsg
argument_list|)
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
return|return
name|readWALsFromResult
argument_list|(
name|queue
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|abortable
operator|.
name|abort
argument_list|(
name|errMsg
argument_list|,
name|e
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
block|}
comment|/**    * Read all of the WAL's from a queue into a list    *    * @param queue HBase query result containing the queue    * @return a list of all the WAL filenames    */
specifier|protected
name|List
argument_list|<
name|String
argument_list|>
name|readWALsFromResult
parameter_list|(
name|Result
name|queue
parameter_list|)
block|{
name|List
argument_list|<
name|String
argument_list|>
name|wals
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|byte
index|[]
argument_list|,
name|byte
index|[]
argument_list|>
name|familyMap
init|=
name|queue
operator|.
name|getFamilyMap
argument_list|(
name|CF_QUEUE
argument_list|)
decl_stmt|;
for|for
control|(
name|byte
index|[]
name|cQualifier
range|:
name|familyMap
operator|.
name|keySet
argument_list|()
control|)
block|{
comment|// Ignore the meta data fields of the queue
if|if
condition|(
name|Arrays
operator|.
name|equals
argument_list|(
name|cQualifier
argument_list|,
name|COL_QUEUE_OWNER
argument_list|)
operator|||
name|Arrays
operator|.
name|equals
argument_list|(
name|cQualifier
argument_list|,
name|COL_QUEUE_OWNER_HISTORY
argument_list|)
condition|)
block|{
continue|continue;
block|}
name|wals
operator|.
name|add
argument_list|(
name|Bytes
operator|.
name|toString
argument_list|(
name|cQualifier
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|wals
return|;
block|}
comment|/**    * Get the queue id's and meta data (Owner and History) for the queues belonging to the named    * server    *    * @param server name of the server    * @return a ResultScanner over the QueueIds belonging to the server    * @throws IOException    */
specifier|protected
name|ResultScanner
name|getQueuesBelongingToServer
parameter_list|(
name|String
name|server
parameter_list|)
throws|throws
name|IOException
block|{
name|Scan
name|scan
init|=
operator|new
name|Scan
argument_list|()
decl_stmt|;
name|SingleColumnValueFilter
name|filterMyQueues
init|=
operator|new
name|SingleColumnValueFilter
argument_list|(
name|CF_QUEUE
argument_list|,
name|COL_QUEUE_OWNER
argument_list|,
name|CompareOperator
operator|.
name|EQUAL
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|server
argument_list|)
argument_list|)
decl_stmt|;
name|scan
operator|.
name|setFilter
argument_list|(
name|filterMyQueues
argument_list|)
expr_stmt|;
name|scan
operator|.
name|addColumn
argument_list|(
name|CF_QUEUE
argument_list|,
name|COL_QUEUE_OWNER
argument_list|)
expr_stmt|;
name|scan
operator|.
name|addColumn
argument_list|(
name|CF_QUEUE
argument_list|,
name|COL_QUEUE_OWNER_HISTORY
argument_list|)
expr_stmt|;
try|try
init|(
name|Table
name|replicationTable
init|=
name|getOrBlockOnReplicationTable
argument_list|()
init|)
block|{
name|ResultScanner
name|results
init|=
name|replicationTable
operator|.
name|getScanner
argument_list|(
name|scan
argument_list|)
decl_stmt|;
return|return
name|results
return|;
block|}
block|}
comment|/**    * Attempts to acquire the Replication Table. This operation will block until it is assigned by    * the CreateReplicationWorker thread. It is up to the caller of this method to close the    * returned Table    * @return the Replication Table when it is created    * @throws IOException    */
specifier|protected
name|Table
name|getOrBlockOnReplicationTable
parameter_list|()
throws|throws
name|IOException
block|{
comment|// Sleep until the Replication Table becomes available
try|try
block|{
name|replicationTableInitialized
operator|.
name|await
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
name|String
name|errMsg
init|=
literal|"Unable to acquire the Replication Table due to InterruptedException: "
operator|+
name|e
operator|.
name|getMessage
argument_list|()
decl_stmt|;
throw|throw
operator|new
name|InterruptedIOException
argument_list|(
name|errMsg
argument_list|)
throw|;
block|}
return|return
name|getAndSetUpReplicationTable
argument_list|()
return|;
block|}
comment|/**    * Creates a new copy of the Replication Table and sets up the proper Table time outs for it    *    * @return the Replication Table    * @throws IOException    */
specifier|private
name|Table
name|getAndSetUpReplicationTable
parameter_list|()
throws|throws
name|IOException
block|{
name|Table
name|replicationTable
init|=
name|connection
operator|.
name|getTable
argument_list|(
name|REPLICATION_TABLE_NAME
argument_list|)
decl_stmt|;
name|setReplicationTableTimeOuts
argument_list|(
name|replicationTable
argument_list|)
expr_stmt|;
return|return
name|replicationTable
return|;
block|}
comment|/**    * Builds the Replication Table in a background thread. Any method accessing the Replication Table    * should do so through getOrBlockOnReplicationTable()    *    * @return the Replication Table    * @throws IOException if the Replication Table takes too long to build    */
specifier|private
name|void
name|createReplicationTableInBackground
parameter_list|()
throws|throws
name|IOException
block|{
name|executor
operator|.
name|execute
argument_list|(
operator|new
name|CreateReplicationTableWorker
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**    * Attempts to build the Replication Table. Will continue blocking until we have a valid    * Table for the Replication Table.    */
specifier|private
class|class
name|CreateReplicationTableWorker
implements|implements
name|Runnable
block|{
specifier|private
name|Admin
name|admin
decl_stmt|;
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
try|try
block|{
name|admin
operator|=
name|connection
operator|.
name|getAdmin
argument_list|()
expr_stmt|;
if|if
condition|(
operator|!
name|replicationTableExists
argument_list|()
condition|)
block|{
name|createReplicationTable
argument_list|()
expr_stmt|;
block|}
name|int
name|maxRetries
init|=
name|conf
operator|.
name|getInt
argument_list|(
literal|"hbase.replication.queues.createtable.retries.number"
argument_list|,
name|CLIENT_RETRIES
argument_list|)
decl_stmt|;
name|RetryCounterFactory
name|counterFactory
init|=
operator|new
name|RetryCounterFactory
argument_list|(
name|maxRetries
argument_list|,
name|RPC_TIMEOUT
argument_list|)
decl_stmt|;
name|RetryCounter
name|retryCounter
init|=
name|counterFactory
operator|.
name|create
argument_list|()
decl_stmt|;
while|while
condition|(
operator|!
name|replicationTableExists
argument_list|()
condition|)
block|{
name|retryCounter
operator|.
name|sleepUntilNextRetry
argument_list|()
expr_stmt|;
if|if
condition|(
operator|!
name|retryCounter
operator|.
name|shouldRetry
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Unable to acquire the Replication Table"
argument_list|)
throw|;
block|}
block|}
name|replicationTableInitialized
operator|.
name|countDown
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
decl||
name|InterruptedException
name|e
parameter_list|)
block|{
name|abortable
operator|.
name|abort
argument_list|(
literal|"Failed building Replication Table"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**      * Create the replication table with the provided HColumnDescriptor REPLICATION_COL_DESCRIPTOR      * in TableBasedReplicationQueuesImpl      *      * @throws IOException      */
specifier|private
name|void
name|createReplicationTable
parameter_list|()
throws|throws
name|IOException
block|{
name|HTableDescriptor
name|replicationTableDescriptor
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|REPLICATION_TABLE_NAME
argument_list|)
decl_stmt|;
name|replicationTableDescriptor
operator|.
name|addFamily
argument_list|(
name|REPLICATION_COL_DESCRIPTOR
argument_list|)
expr_stmt|;
try|try
block|{
name|admin
operator|.
name|createTable
argument_list|(
name|replicationTableDescriptor
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|TableExistsException
name|e
parameter_list|)
block|{
comment|// In this case we can just continue as normal
block|}
block|}
comment|/**      * Checks whether the Replication Table exists yet      *      * @return whether the Replication Table exists      * @throws IOException      */
specifier|private
name|boolean
name|replicationTableExists
parameter_list|()
block|{
try|try
block|{
return|return
name|admin
operator|.
name|tableExists
argument_list|(
name|REPLICATION_TABLE_NAME
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
return|return
literal|false
return|;
block|}
block|}
block|}
block|}
end_class

end_unit

