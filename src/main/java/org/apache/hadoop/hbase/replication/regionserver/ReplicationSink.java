begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Copyright 2010 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|KeyValue
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
name|HTableInterface
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
name|HTablePool
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
name|regionserver
operator|.
name|wal
operator|.
name|HLog
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
name|wal
operator|.
name|WALEdit
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
name|Stoppable
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
name|ArrayList
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
name|TreeMap
import|;
end_import

begin_comment
comment|/**  * This class is responsible for replicating the edits coming  * from another cluster.  *<p/>  * This replication process is currently waiting for the edits to be applied  * before the method can return. This means that the replication of edits  * is synchronized (after reading from HLogs in ReplicationSource) and that a  * single region server cannot receive edits from two sources at the same time  *<p/>  * This class uses the native HBase client in order to replicate entries.  *<p/>  *  * TODO make this class more like ReplicationSource wrt log handling  */
end_comment

begin_class
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
comment|// Name of the HDFS directory that contains the temporary rep logs
specifier|public
specifier|static
specifier|final
name|String
name|REPLICATION_LOG_DIR
init|=
literal|".replogs"
decl_stmt|;
specifier|private
specifier|final
name|Configuration
name|conf
decl_stmt|;
comment|// Pool used to replicated
specifier|private
specifier|final
name|HTablePool
name|pool
decl_stmt|;
specifier|private
specifier|final
name|ReplicationSinkMetrics
name|metrics
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
name|conf
expr_stmt|;
name|this
operator|.
name|pool
operator|=
operator|new
name|HTablePool
argument_list|(
name|this
operator|.
name|conf
argument_list|,
name|conf
operator|.
name|getInt
argument_list|(
literal|"replication.sink.htablepool.capacity"
argument_list|,
literal|10
argument_list|)
argument_list|)
expr_stmt|;
name|this
operator|.
name|metrics
operator|=
operator|new
name|ReplicationSinkMetrics
argument_list|()
expr_stmt|;
block|}
comment|/**    * Replicate this array of entries directly into the local cluster    * using the native client.    *    * @param entries    * @throws IOException    */
specifier|public
name|void
name|replicateEntries
parameter_list|(
name|HLog
operator|.
name|Entry
index|[]
name|entries
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|entries
operator|.
name|length
operator|==
literal|0
condition|)
block|{
return|return;
block|}
comment|// Very simple optimization where we batch sequences of rows going
comment|// to the same table.
try|try
block|{
name|long
name|totalReplicated
init|=
literal|0
decl_stmt|;
comment|// Map of table => list of Rows, we only want to flushCommits once per
comment|// invocation of this method per table.
name|Map
argument_list|<
name|byte
index|[]
argument_list|,
name|List
argument_list|<
name|Row
argument_list|>
argument_list|>
name|rows
init|=
operator|new
name|TreeMap
argument_list|<
name|byte
index|[]
argument_list|,
name|List
argument_list|<
name|Row
argument_list|>
argument_list|>
argument_list|(
name|Bytes
operator|.
name|BYTES_COMPARATOR
argument_list|)
decl_stmt|;
for|for
control|(
name|HLog
operator|.
name|Entry
name|entry
range|:
name|entries
control|)
block|{
name|WALEdit
name|edit
init|=
name|entry
operator|.
name|getEdit
argument_list|()
decl_stmt|;
name|byte
index|[]
name|table
init|=
name|entry
operator|.
name|getKey
argument_list|()
operator|.
name|getTablename
argument_list|()
decl_stmt|;
name|Put
name|put
init|=
literal|null
decl_stmt|;
name|Delete
name|del
init|=
literal|null
decl_stmt|;
name|KeyValue
name|lastKV
init|=
literal|null
decl_stmt|;
name|List
argument_list|<
name|KeyValue
argument_list|>
name|kvs
init|=
name|edit
operator|.
name|getKeyValues
argument_list|()
decl_stmt|;
for|for
control|(
name|KeyValue
name|kv
range|:
name|kvs
control|)
block|{
if|if
condition|(
name|lastKV
operator|==
literal|null
operator|||
name|lastKV
operator|.
name|getType
argument_list|()
operator|!=
name|kv
operator|.
name|getType
argument_list|()
operator|||
operator|!
name|lastKV
operator|.
name|matchingRow
argument_list|(
name|kv
argument_list|)
condition|)
block|{
if|if
condition|(
name|kv
operator|.
name|isDelete
argument_list|()
condition|)
block|{
name|del
operator|=
operator|new
name|Delete
argument_list|(
name|kv
operator|.
name|getRow
argument_list|()
argument_list|)
expr_stmt|;
name|del
operator|.
name|setClusterId
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
operator|.
name|getClusterId
argument_list|()
argument_list|)
expr_stmt|;
name|addToMultiMap
argument_list|(
name|rows
argument_list|,
name|table
argument_list|,
name|del
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|put
operator|=
operator|new
name|Put
argument_list|(
name|kv
operator|.
name|getRow
argument_list|()
argument_list|)
expr_stmt|;
name|put
operator|.
name|setClusterId
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
operator|.
name|getClusterId
argument_list|()
argument_list|)
expr_stmt|;
name|addToMultiMap
argument_list|(
name|rows
argument_list|,
name|table
argument_list|,
name|put
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|kv
operator|.
name|isDelete
argument_list|()
condition|)
block|{
name|del
operator|.
name|addDeleteMarker
argument_list|(
name|kv
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|put
operator|.
name|add
argument_list|(
name|kv
argument_list|)
expr_stmt|;
block|}
name|lastKV
operator|=
name|kv
expr_stmt|;
block|}
name|totalReplicated
operator|++
expr_stmt|;
block|}
for|for
control|(
name|byte
index|[]
name|table
range|:
name|rows
operator|.
name|keySet
argument_list|()
control|)
block|{
name|batch
argument_list|(
name|table
argument_list|,
name|rows
operator|.
name|get
argument_list|(
name|table
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|this
operator|.
name|metrics
operator|.
name|setAgeOfLastAppliedOp
argument_list|(
name|entries
index|[
name|entries
operator|.
name|length
operator|-
literal|1
index|]
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
name|appliedBatchesRate
operator|.
name|inc
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Total replicated: "
operator|+
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
comment|/**    * Simple helper to a map from key to (a list of) values    * TODO: Make a general utility method    * @param map    * @param key    * @param value    * @return    */
specifier|private
parameter_list|<
name|K
parameter_list|,
name|V
parameter_list|>
name|List
argument_list|<
name|V
argument_list|>
name|addToMultiMap
parameter_list|(
name|Map
argument_list|<
name|K
argument_list|,
name|List
argument_list|<
name|V
argument_list|>
argument_list|>
name|map
parameter_list|,
name|K
name|key
parameter_list|,
name|V
name|value
parameter_list|)
block|{
name|List
argument_list|<
name|V
argument_list|>
name|values
init|=
name|map
operator|.
name|get
argument_list|(
name|key
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
name|map
operator|.
name|put
argument_list|(
name|key
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
comment|/**    * Do the changes and handle the pool    * @param tableName table to insert into    * @param rows list of actions    * @throws IOException    */
specifier|private
name|void
name|batch
parameter_list|(
name|byte
index|[]
name|tableName
parameter_list|,
name|List
argument_list|<
name|Row
argument_list|>
name|rows
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|rows
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
return|return;
block|}
name|HTableInterface
name|table
init|=
literal|null
decl_stmt|;
try|try
block|{
name|table
operator|=
name|this
operator|.
name|pool
operator|.
name|getTable
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
name|table
operator|.
name|batch
argument_list|(
name|rows
argument_list|)
expr_stmt|;
name|this
operator|.
name|metrics
operator|.
name|appliedOpsRate
operator|.
name|inc
argument_list|(
name|rows
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|ix
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
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
block|}
end_class

end_unit

