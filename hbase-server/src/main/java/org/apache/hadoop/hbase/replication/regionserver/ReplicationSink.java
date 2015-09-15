begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
operator|.
name|regionserver
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
name|Collection
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashMap
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
name|Map
operator|.
name|Entry
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|TreeMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|UUID
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
name|atomic
operator|.
name|AtomicLong
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
name|lang
operator|.
name|StringUtils
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
name|Cell
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
name|CellScanner
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
name|CellUtil
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
name|HBaseConfiguration
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
name|Stoppable
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
name|Delete
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
name|Mutation
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
name|Put
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
name|protobuf
operator|.
name|generated
operator|.
name|AdminProtos
operator|.
name|WALEntry
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
name|protobuf
operator|.
name|generated
operator|.
name|HBaseProtos
import|;
end_import

begin_comment
comment|/**  *<p>  * This class is responsible for replicating the edits coming  * from another cluster.  *</p><p>  * This replication process is currently waiting for the edits to be applied  * before the method can return. This means that the replication of edits  * is synchronized (after reading from WALs in ReplicationSource) and that a  * single region server cannot receive edits from two sources at the same time  *</p><p>  * This class uses the native HBase client in order to replicate entries.  *</p>  *  * TODO make this class more like ReplicationSource wrt log handling  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|ReplicationSink
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
name|ReplicationSink
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|Configuration
name|conf
decl_stmt|;
comment|// Volatile because of note in here -- look for double-checked locking:
comment|// http://www.oracle.com/technetwork/articles/javase/bloch-effective-08-qa-140880.html
specifier|private
specifier|volatile
name|Connection
name|sharedHtableCon
decl_stmt|;
specifier|private
specifier|final
name|MetricsSink
name|metrics
decl_stmt|;
specifier|private
specifier|final
name|AtomicLong
name|totalReplicatedEdits
init|=
operator|new
name|AtomicLong
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|Object
name|sharedHtableConLock
init|=
operator|new
name|Object
argument_list|()
decl_stmt|;
comment|/**    * Create a sink for replication    *    * @param conf                conf object    * @param stopper             boolean to tell this thread to stop    * @throws IOException thrown when HDFS goes bad or bad file name    */
specifier|public
name|ReplicationSink
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|Stoppable
name|stopper
parameter_list|)
throws|throws
name|IOException
block|{
name|this
operator|.
name|conf
operator|=
name|HBaseConfiguration
operator|.
name|create
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|decorateConf
argument_list|()
expr_stmt|;
name|this
operator|.
name|metrics
operator|=
operator|new
name|MetricsSink
argument_list|()
expr_stmt|;
block|}
comment|/**    * decorate the Configuration object to make replication more receptive to delays:    * lessen the timeout and numTries.    */
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
name|this
operator|.
name|conf
operator|.
name|getInt
argument_list|(
literal|"replication.sink.client.retries.number"
argument_list|,
literal|4
argument_list|)
argument_list|)
expr_stmt|;
name|this
operator|.
name|conf
operator|.
name|setInt
argument_list|(
name|HConstants
operator|.
name|HBASE_CLIENT_OPERATION_TIMEOUT
argument_list|,
name|this
operator|.
name|conf
operator|.
name|getInt
argument_list|(
literal|"replication.sink.client.ops.timeout"
argument_list|,
literal|10000
argument_list|)
argument_list|)
expr_stmt|;
name|String
name|replicationCodec
init|=
name|this
operator|.
name|conf
operator|.
name|get
argument_list|(
name|HConstants
operator|.
name|REPLICATION_CODEC_CONF_KEY
argument_list|)
decl_stmt|;
if|if
condition|(
name|StringUtils
operator|.
name|isNotEmpty
argument_list|(
name|replicationCodec
argument_list|)
condition|)
block|{
name|this
operator|.
name|conf
operator|.
name|set
argument_list|(
name|HConstants
operator|.
name|RPC_CODEC_CONF_KEY
argument_list|,
name|replicationCodec
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Replicate this array of entries directly into the local cluster using the native client. Only    * operates against raw protobuf type saving on a conversion from pb to pojo.    * @param entries    * @param cells    * @throws IOException    */
specifier|public
name|void
name|replicateEntries
parameter_list|(
name|List
argument_list|<
name|WALEntry
argument_list|>
name|entries
parameter_list|,
specifier|final
name|CellScanner
name|cells
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|entries
operator|.
name|isEmpty
argument_list|()
condition|)
return|return;
if|if
condition|(
name|cells
operator|==
literal|null
condition|)
throw|throw
operator|new
name|NullPointerException
argument_list|(
literal|"TODO: Add handling of null CellScanner"
argument_list|)
throw|;
comment|// Very simple optimization where we batch sequences of rows going
comment|// to the same table.
try|try
block|{
name|long
name|totalReplicated
init|=
literal|0
decl_stmt|;
comment|// Map of table => list of Rows, grouped by cluster id, we only want to flushCommits once per
comment|// invocation of this method per table and cluster id.
name|Map
argument_list|<
name|TableName
argument_list|,
name|Map
argument_list|<
name|List
argument_list|<
name|UUID
argument_list|>
argument_list|,
name|List
argument_list|<
name|Row
argument_list|>
argument_list|>
argument_list|>
name|rowMap
init|=
operator|new
name|TreeMap
argument_list|<
name|TableName
argument_list|,
name|Map
argument_list|<
name|List
argument_list|<
name|UUID
argument_list|>
argument_list|,
name|List
argument_list|<
name|Row
argument_list|>
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|WALEntry
name|entry
range|:
name|entries
control|)
block|{
name|TableName
name|table
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
operator|.
name|getTableName
argument_list|()
operator|.
name|toByteArray
argument_list|()
argument_list|)
decl_stmt|;
name|Cell
name|previousCell
init|=
literal|null
decl_stmt|;
name|Mutation
name|m
init|=
literal|null
decl_stmt|;
name|int
name|count
init|=
name|entry
operator|.
name|getAssociatedCellCount
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|count
condition|;
name|i
operator|++
control|)
block|{
comment|// Throw index out of bounds if our cell count is off
if|if
condition|(
operator|!
name|cells
operator|.
name|advance
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|ArrayIndexOutOfBoundsException
argument_list|(
literal|"Expected="
operator|+
name|count
operator|+
literal|", index="
operator|+
name|i
argument_list|)
throw|;
block|}
name|Cell
name|cell
init|=
name|cells
operator|.
name|current
argument_list|()
decl_stmt|;
if|if
condition|(
name|isNewRowOrType
argument_list|(
name|previousCell
argument_list|,
name|cell
argument_list|)
condition|)
block|{
comment|// Create new mutation
name|m
operator|=
name|CellUtil
operator|.
name|isDelete
argument_list|(
name|cell
argument_list|)
condition|?
operator|new
name|Delete
argument_list|(
name|cell
operator|.
name|getRowArray
argument_list|()
argument_list|,
name|cell
operator|.
name|getRowOffset
argument_list|()
argument_list|,
name|cell
operator|.
name|getRowLength
argument_list|()
argument_list|)
else|:
operator|new
name|Put
argument_list|(
name|cell
operator|.
name|getRowArray
argument_list|()
argument_list|,
name|cell
operator|.
name|getRowOffset
argument_list|()
argument_list|,
name|cell
operator|.
name|getRowLength
argument_list|()
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|UUID
argument_list|>
name|clusterIds
init|=
operator|new
name|ArrayList
argument_list|<
name|UUID
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|HBaseProtos
operator|.
name|UUID
name|clusterId
range|:
name|entry
operator|.
name|getKey
argument_list|()
operator|.
name|getClusterIdsList
argument_list|()
control|)
block|{
name|clusterIds
operator|.
name|add
argument_list|(
name|toUUID
argument_list|(
name|clusterId
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|m
operator|.
name|setClusterIds
argument_list|(
name|clusterIds
argument_list|)
expr_stmt|;
name|addToHashMultiMap
argument_list|(
name|rowMap
argument_list|,
name|table
argument_list|,
name|clusterIds
argument_list|,
name|m
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|CellUtil
operator|.
name|isDelete
argument_list|(
name|cell
argument_list|)
condition|)
block|{
operator|(
operator|(
name|Delete
operator|)
name|m
operator|)
operator|.
name|addDeleteMarker
argument_list|(
name|cell
argument_list|)
expr_stmt|;
block|}
else|else
block|{
operator|(
operator|(
name|Put
operator|)
name|m
operator|)
operator|.
name|add
argument_list|(
name|cell
argument_list|)
expr_stmt|;
block|}
name|previousCell
operator|=
name|cell
expr_stmt|;
block|}
name|totalReplicated
operator|++
expr_stmt|;
block|}
for|for
control|(
name|Entry
argument_list|<
name|TableName
argument_list|,
name|Map
argument_list|<
name|List
argument_list|<
name|UUID
argument_list|>
argument_list|,
name|List
argument_list|<
name|Row
argument_list|>
argument_list|>
argument_list|>
name|entry
range|:
name|rowMap
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|batch
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|,
name|entry
operator|.
name|getValue
argument_list|()
operator|.
name|values
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|int
name|size
init|=
name|entries
operator|.
name|size
argument_list|()
decl_stmt|;
name|this
operator|.
name|metrics
operator|.
name|setAgeOfLastAppliedOp
argument_list|(
name|entries
operator|.
name|get
argument_list|(
name|size
operator|-
literal|1
argument_list|)
operator|.
name|getKey
argument_list|()
operator|.
name|getWriteTime
argument_list|()
argument_list|)
expr_stmt|;
name|this
operator|.
name|metrics
operator|.
name|applyBatch
argument_list|(
name|size
argument_list|)
expr_stmt|;
name|this
operator|.
name|totalReplicatedEdits
operator|.
name|addAndGet
argument_list|(
name|totalReplicated
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|ex
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Unable to accept edit because:"
argument_list|,
name|ex
argument_list|)
expr_stmt|;
throw|throw
name|ex
throw|;
block|}
block|}
comment|/**    * @param previousCell    * @param cell    * @return True if we have crossed over onto a new row or type    */
specifier|private
name|boolean
name|isNewRowOrType
parameter_list|(
specifier|final
name|Cell
name|previousCell
parameter_list|,
specifier|final
name|Cell
name|cell
parameter_list|)
block|{
return|return
name|previousCell
operator|==
literal|null
operator|||
name|previousCell
operator|.
name|getTypeByte
argument_list|()
operator|!=
name|cell
operator|.
name|getTypeByte
argument_list|()
operator|||
operator|!
name|CellUtil
operator|.
name|matchingRow
argument_list|(
name|previousCell
argument_list|,
name|cell
argument_list|)
return|;
block|}
specifier|private
name|java
operator|.
name|util
operator|.
name|UUID
name|toUUID
parameter_list|(
specifier|final
name|HBaseProtos
operator|.
name|UUID
name|uuid
parameter_list|)
block|{
return|return
operator|new
name|java
operator|.
name|util
operator|.
name|UUID
argument_list|(
name|uuid
operator|.
name|getMostSigBits
argument_list|()
argument_list|,
name|uuid
operator|.
name|getLeastSigBits
argument_list|()
argument_list|)
return|;
block|}
comment|/**    * Simple helper to a map from key to (a list of) values    * TODO: Make a general utility method    * @param map    * @param key1    * @param key2    * @param value    * @return the list of values corresponding to key1 and key2    */
specifier|private
parameter_list|<
name|K1
parameter_list|,
name|K2
parameter_list|,
name|V
parameter_list|>
name|List
argument_list|<
name|V
argument_list|>
name|addToHashMultiMap
parameter_list|(
name|Map
argument_list|<
name|K1
argument_list|,
name|Map
argument_list|<
name|K2
argument_list|,
name|List
argument_list|<
name|V
argument_list|>
argument_list|>
argument_list|>
name|map
parameter_list|,
name|K1
name|key1
parameter_list|,
name|K2
name|key2
parameter_list|,
name|V
name|value
parameter_list|)
block|{
name|Map
argument_list|<
name|K2
argument_list|,
name|List
argument_list|<
name|V
argument_list|>
argument_list|>
name|innerMap
init|=
name|map
operator|.
name|get
argument_list|(
name|key1
argument_list|)
decl_stmt|;
if|if
condition|(
name|innerMap
operator|==
literal|null
condition|)
block|{
name|innerMap
operator|=
operator|new
name|HashMap
argument_list|<
name|K2
argument_list|,
name|List
argument_list|<
name|V
argument_list|>
argument_list|>
argument_list|()
expr_stmt|;
name|map
operator|.
name|put
argument_list|(
name|key1
argument_list|,
name|innerMap
argument_list|)
expr_stmt|;
block|}
name|List
argument_list|<
name|V
argument_list|>
name|values
init|=
name|innerMap
operator|.
name|get
argument_list|(
name|key2
argument_list|)
decl_stmt|;
if|if
condition|(
name|values
operator|==
literal|null
condition|)
block|{
name|values
operator|=
operator|new
name|ArrayList
argument_list|<
name|V
argument_list|>
argument_list|()
expr_stmt|;
name|innerMap
operator|.
name|put
argument_list|(
name|key2
argument_list|,
name|values
argument_list|)
expr_stmt|;
block|}
name|values
operator|.
name|add
argument_list|(
name|value
argument_list|)
expr_stmt|;
return|return
name|values
return|;
block|}
comment|/**    * stop the thread pool executor. It is called when the regionserver is stopped.    */
specifier|public
name|void
name|stopReplicationSinkServices
parameter_list|()
block|{
try|try
block|{
if|if
condition|(
name|this
operator|.
name|sharedHtableCon
operator|!=
literal|null
condition|)
block|{
synchronized|synchronized
init|(
name|sharedHtableConLock
init|)
block|{
if|if
condition|(
name|this
operator|.
name|sharedHtableCon
operator|!=
literal|null
condition|)
block|{
name|this
operator|.
name|sharedHtableCon
operator|.
name|close
argument_list|()
expr_stmt|;
name|this
operator|.
name|sharedHtableCon
operator|=
literal|null
expr_stmt|;
block|}
block|}
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"IOException while closing the connection"
argument_list|,
name|e
argument_list|)
expr_stmt|;
comment|// ignoring as we are closing.
block|}
block|}
comment|/**    * Do the changes and handle the pool    * @param tableName table to insert into    * @param allRows list of actions    * @throws IOException    */
specifier|protected
name|void
name|batch
parameter_list|(
name|TableName
name|tableName
parameter_list|,
name|Collection
argument_list|<
name|List
argument_list|<
name|Row
argument_list|>
argument_list|>
name|allRows
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|allRows
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
return|return;
block|}
name|Table
name|table
init|=
literal|null
decl_stmt|;
try|try
block|{
comment|// See https://en.wikipedia.org/wiki/Double-checked_locking
name|Connection
name|connection
init|=
name|this
operator|.
name|sharedHtableCon
decl_stmt|;
if|if
condition|(
name|connection
operator|==
literal|null
condition|)
block|{
synchronized|synchronized
init|(
name|sharedHtableConLock
init|)
block|{
name|connection
operator|=
name|this
operator|.
name|sharedHtableCon
expr_stmt|;
if|if
condition|(
name|connection
operator|==
literal|null
condition|)
block|{
name|connection
operator|=
name|this
operator|.
name|sharedHtableCon
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
block|}
block|}
block|}
name|table
operator|=
name|connection
operator|.
name|getTable
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
for|for
control|(
name|List
argument_list|<
name|Row
argument_list|>
name|rows
range|:
name|allRows
control|)
block|{
name|table
operator|.
name|batch
argument_list|(
name|rows
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|ix
parameter_list|)
block|{
throw|throw
operator|(
name|InterruptedIOException
operator|)
operator|new
name|InterruptedIOException
argument_list|()
operator|.
name|initCause
argument_list|(
name|ix
argument_list|)
throw|;
block|}
finally|finally
block|{
if|if
condition|(
name|table
operator|!=
literal|null
condition|)
block|{
name|table
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
block|}
comment|/**    * Get a string representation of this sink's metrics    * @return string with the total replicated edits count and the date    * of the last edit that was applied    */
specifier|public
name|String
name|getStats
parameter_list|()
block|{
return|return
name|this
operator|.
name|totalReplicatedEdits
operator|.
name|get
argument_list|()
operator|==
literal|0
condition|?
literal|""
else|:
literal|"Sink: "
operator|+
literal|"age in ms of last applied edit: "
operator|+
name|this
operator|.
name|metrics
operator|.
name|refreshAgeOfLastAppliedOp
argument_list|()
operator|+
literal|", total replicated edits: "
operator|+
name|this
operator|.
name|totalReplicatedEdits
return|;
block|}
comment|/**    * Get replication Sink Metrics    * @return MetricsSink    */
specifier|public
name|MetricsSink
name|getSinkMetrics
parameter_list|()
block|{
return|return
name|this
operator|.
name|metrics
return|;
block|}
block|}
end_class

end_unit

