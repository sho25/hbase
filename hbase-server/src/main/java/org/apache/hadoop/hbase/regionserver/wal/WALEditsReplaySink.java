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
name|regionserver
operator|.
name|wal
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
name|ArrayList
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
name|HRegionInfo
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
name|HRegionLocation
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
name|client
operator|.
name|HConnection
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
name|RegionServerCallable
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
name|RpcRetryingCallerFactory
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
name|ipc
operator|.
name|PayloadCarryingRpcController
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
name|ProtobufUtil
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
name|ReplicationProtbufUtil
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
name|AdminService
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
name|ReplicateWALEntryResponse
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
name|EnvironmentEdgeManager
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
name|Pair
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|ServiceException
import|;
end_import

begin_comment
comment|/**  * This class is responsible for replaying the edits coming from a failed region server.  *<p/>  * This class uses the native HBase client in order to replay WAL entries.  *<p/>  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|WALEditsReplaySink
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
name|WALEditsReplaySink
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|MAX_BATCH_SIZE
init|=
literal|1024
decl_stmt|;
specifier|private
specifier|final
name|Configuration
name|conf
decl_stmt|;
specifier|private
specifier|final
name|HConnection
name|conn
decl_stmt|;
specifier|private
specifier|final
name|TableName
name|tableName
decl_stmt|;
specifier|private
specifier|final
name|MetricsWALEditsReplay
name|metrics
decl_stmt|;
specifier|private
specifier|final
name|AtomicLong
name|totalReplayedEdits
init|=
operator|new
name|AtomicLong
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|boolean
name|skipErrors
decl_stmt|;
specifier|private
specifier|final
name|int
name|replayTimeout
decl_stmt|;
comment|/**    * Create a sink for WAL log entries replay    * @param conf    * @param tableName    * @param conn    * @throws IOException    */
specifier|public
name|WALEditsReplaySink
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|TableName
name|tableName
parameter_list|,
name|HConnection
name|conn
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
name|metrics
operator|=
operator|new
name|MetricsWALEditsReplay
argument_list|()
expr_stmt|;
name|this
operator|.
name|conn
operator|=
name|conn
expr_stmt|;
name|this
operator|.
name|tableName
operator|=
name|tableName
expr_stmt|;
name|this
operator|.
name|skipErrors
operator|=
name|conf
operator|.
name|getBoolean
argument_list|(
name|HConstants
operator|.
name|HREGION_EDITS_REPLAY_SKIP_ERRORS
argument_list|,
name|HConstants
operator|.
name|DEFAULT_HREGION_EDITS_REPLAY_SKIP_ERRORS
argument_list|)
expr_stmt|;
comment|// a single replay operation time out and default is 60 seconds
name|this
operator|.
name|replayTimeout
operator|=
name|conf
operator|.
name|getInt
argument_list|(
literal|"hbase.regionserver.logreplay.timeout"
argument_list|,
literal|60000
argument_list|)
expr_stmt|;
block|}
comment|/**    * Replay an array of actions of the same region directly into the newly assigned Region Server    * @param entries    * @throws IOException    */
specifier|public
name|void
name|replayEntries
parameter_list|(
name|List
argument_list|<
name|Pair
argument_list|<
name|HRegionLocation
argument_list|,
name|HLog
operator|.
name|Entry
argument_list|>
argument_list|>
name|entries
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|entries
operator|.
name|size
argument_list|()
operator|==
literal|0
condition|)
block|{
return|return;
block|}
name|int
name|batchSize
init|=
name|entries
operator|.
name|size
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|HRegionInfo
argument_list|,
name|List
argument_list|<
name|HLog
operator|.
name|Entry
argument_list|>
argument_list|>
name|entriesByRegion
init|=
operator|new
name|HashMap
argument_list|<
name|HRegionInfo
argument_list|,
name|List
argument_list|<
name|HLog
operator|.
name|Entry
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
name|HRegionLocation
name|loc
init|=
literal|null
decl_stmt|;
name|HLog
operator|.
name|Entry
name|entry
init|=
literal|null
decl_stmt|;
name|List
argument_list|<
name|HLog
operator|.
name|Entry
argument_list|>
name|regionEntries
init|=
literal|null
decl_stmt|;
comment|// Build the action list.
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|batchSize
condition|;
name|i
operator|++
control|)
block|{
name|loc
operator|=
name|entries
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|getFirst
argument_list|()
expr_stmt|;
name|entry
operator|=
name|entries
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|getSecond
argument_list|()
expr_stmt|;
if|if
condition|(
name|entriesByRegion
operator|.
name|containsKey
argument_list|(
name|loc
operator|.
name|getRegionInfo
argument_list|()
argument_list|)
condition|)
block|{
name|regionEntries
operator|=
name|entriesByRegion
operator|.
name|get
argument_list|(
name|loc
operator|.
name|getRegionInfo
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|regionEntries
operator|=
operator|new
name|ArrayList
argument_list|<
name|HLog
operator|.
name|Entry
argument_list|>
argument_list|()
expr_stmt|;
name|entriesByRegion
operator|.
name|put
argument_list|(
name|loc
operator|.
name|getRegionInfo
argument_list|()
argument_list|,
name|regionEntries
argument_list|)
expr_stmt|;
block|}
name|regionEntries
operator|.
name|add
argument_list|(
name|entry
argument_list|)
expr_stmt|;
block|}
name|long
name|startTime
init|=
name|EnvironmentEdgeManager
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
comment|// replaying edits by region
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|HRegionInfo
argument_list|,
name|List
argument_list|<
name|HLog
operator|.
name|Entry
argument_list|>
argument_list|>
name|_entry
range|:
name|entriesByRegion
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|HRegionInfo
name|curRegion
init|=
name|_entry
operator|.
name|getKey
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|HLog
operator|.
name|Entry
argument_list|>
name|allActions
init|=
name|_entry
operator|.
name|getValue
argument_list|()
decl_stmt|;
comment|// send edits in chunks
name|int
name|totalActions
init|=
name|allActions
operator|.
name|size
argument_list|()
decl_stmt|;
name|int
name|replayedActions
init|=
literal|0
decl_stmt|;
name|int
name|curBatchSize
init|=
literal|0
decl_stmt|;
for|for
control|(
init|;
name|replayedActions
operator|<
name|totalActions
condition|;
control|)
block|{
name|curBatchSize
operator|=
operator|(
name|totalActions
operator|>
operator|(
name|MAX_BATCH_SIZE
operator|+
name|replayedActions
operator|)
operator|)
condition|?
name|MAX_BATCH_SIZE
else|:
operator|(
name|totalActions
operator|-
name|replayedActions
operator|)
expr_stmt|;
name|replayEdits
argument_list|(
name|loc
argument_list|,
name|curRegion
argument_list|,
name|allActions
operator|.
name|subList
argument_list|(
name|replayedActions
argument_list|,
name|replayedActions
operator|+
name|curBatchSize
argument_list|)
argument_list|)
expr_stmt|;
name|replayedActions
operator|+=
name|curBatchSize
expr_stmt|;
block|}
block|}
name|long
name|endTime
init|=
name|EnvironmentEdgeManager
operator|.
name|currentTimeMillis
argument_list|()
operator|-
name|startTime
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"number of rows:"
operator|+
name|entries
operator|.
name|size
argument_list|()
operator|+
literal|" are sent by batch! spent "
operator|+
name|endTime
operator|+
literal|"(ms)!"
argument_list|)
expr_stmt|;
name|metrics
operator|.
name|updateReplayTime
argument_list|(
name|endTime
argument_list|)
expr_stmt|;
name|metrics
operator|.
name|updateReplayBatchSize
argument_list|(
name|batchSize
argument_list|)
expr_stmt|;
name|this
operator|.
name|totalReplayedEdits
operator|.
name|addAndGet
argument_list|(
name|batchSize
argument_list|)
expr_stmt|;
block|}
comment|/**    * Get a string representation of this sink's metrics    * @return string with the total replayed edits count    */
specifier|public
name|String
name|getStats
parameter_list|()
block|{
return|return
name|this
operator|.
name|totalReplayedEdits
operator|.
name|get
argument_list|()
operator|==
literal|0
condition|?
literal|""
else|:
literal|"Sink: total replayed edits: "
operator|+
name|this
operator|.
name|totalReplayedEdits
return|;
block|}
specifier|private
name|void
name|replayEdits
parameter_list|(
specifier|final
name|HRegionLocation
name|regionLoc
parameter_list|,
specifier|final
name|HRegionInfo
name|regionInfo
parameter_list|,
specifier|final
name|List
argument_list|<
name|HLog
operator|.
name|Entry
argument_list|>
name|entries
parameter_list|)
throws|throws
name|IOException
block|{
try|try
block|{
name|RpcRetryingCallerFactory
name|factory
init|=
name|RpcRetryingCallerFactory
operator|.
name|instantiate
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|ReplayServerCallable
argument_list|<
name|ReplicateWALEntryResponse
argument_list|>
name|callable
init|=
operator|new
name|ReplayServerCallable
argument_list|<
name|ReplicateWALEntryResponse
argument_list|>
argument_list|(
name|this
operator|.
name|conn
argument_list|,
name|this
operator|.
name|tableName
argument_list|,
name|regionLoc
argument_list|,
name|regionInfo
argument_list|,
name|entries
argument_list|)
decl_stmt|;
name|factory
operator|.
expr|<
name|ReplicateWALEntryResponse
operator|>
name|newCaller
argument_list|()
operator|.
name|callWithRetries
argument_list|(
name|callable
argument_list|,
name|this
operator|.
name|replayTimeout
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|ie
parameter_list|)
block|{
if|if
condition|(
name|skipErrors
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
name|HConstants
operator|.
name|HREGION_EDITS_REPLAY_SKIP_ERRORS
operator|+
literal|"=true so continuing replayEdits with error:"
operator|+
name|ie
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
throw|throw
name|ie
throw|;
block|}
block|}
block|}
comment|/**    * Callable that handles the<code>replay</code> method call going against a single regionserver    * @param<R>    */
class|class
name|ReplayServerCallable
parameter_list|<
name|R
parameter_list|>
extends|extends
name|RegionServerCallable
argument_list|<
name|ReplicateWALEntryResponse
argument_list|>
block|{
specifier|private
name|HRegionInfo
name|regionInfo
decl_stmt|;
specifier|private
name|List
argument_list|<
name|HLog
operator|.
name|Entry
argument_list|>
name|entries
decl_stmt|;
name|ReplayServerCallable
parameter_list|(
specifier|final
name|HConnection
name|connection
parameter_list|,
specifier|final
name|TableName
name|tableName
parameter_list|,
specifier|final
name|HRegionLocation
name|regionLoc
parameter_list|,
specifier|final
name|HRegionInfo
name|regionInfo
parameter_list|,
specifier|final
name|List
argument_list|<
name|HLog
operator|.
name|Entry
argument_list|>
name|entries
parameter_list|)
block|{
name|super
argument_list|(
name|connection
argument_list|,
name|tableName
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|this
operator|.
name|entries
operator|=
name|entries
expr_stmt|;
name|this
operator|.
name|regionInfo
operator|=
name|regionInfo
expr_stmt|;
name|setLocation
argument_list|(
name|regionLoc
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|ReplicateWALEntryResponse
name|call
parameter_list|(
name|int
name|callTimeout
parameter_list|)
throws|throws
name|IOException
block|{
try|try
block|{
name|replayToServer
argument_list|(
name|this
operator|.
name|regionInfo
argument_list|,
name|this
operator|.
name|entries
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ServiceException
name|se
parameter_list|)
block|{
throw|throw
name|ProtobufUtil
operator|.
name|getRemoteException
argument_list|(
name|se
argument_list|)
throw|;
block|}
return|return
literal|null
return|;
block|}
specifier|private
name|void
name|replayToServer
parameter_list|(
name|HRegionInfo
name|regionInfo
parameter_list|,
name|List
argument_list|<
name|HLog
operator|.
name|Entry
argument_list|>
name|entries
parameter_list|)
throws|throws
name|IOException
throws|,
name|ServiceException
block|{
if|if
condition|(
name|entries
operator|.
name|isEmpty
argument_list|()
condition|)
return|return;
name|HLog
operator|.
name|Entry
index|[]
name|entriesArray
init|=
operator|new
name|HLog
operator|.
name|Entry
index|[
name|entries
operator|.
name|size
argument_list|()
index|]
decl_stmt|;
name|entriesArray
operator|=
name|entries
operator|.
name|toArray
argument_list|(
name|entriesArray
argument_list|)
expr_stmt|;
name|AdminService
operator|.
name|BlockingInterface
name|remoteSvr
init|=
name|conn
operator|.
name|getAdmin
argument_list|(
name|getLocation
argument_list|()
operator|.
name|getServerName
argument_list|()
argument_list|)
decl_stmt|;
name|Pair
argument_list|<
name|AdminProtos
operator|.
name|ReplicateWALEntryRequest
argument_list|,
name|CellScanner
argument_list|>
name|p
init|=
name|ReplicationProtbufUtil
operator|.
name|buildReplicateWALEntryRequest
argument_list|(
name|entriesArray
argument_list|)
decl_stmt|;
name|PayloadCarryingRpcController
name|controller
init|=
operator|new
name|PayloadCarryingRpcController
argument_list|(
name|p
operator|.
name|getSecond
argument_list|()
argument_list|)
decl_stmt|;
try|try
block|{
name|remoteSvr
operator|.
name|replay
argument_list|(
name|controller
argument_list|,
name|p
operator|.
name|getFirst
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ServiceException
name|se
parameter_list|)
block|{
throw|throw
name|ProtobufUtil
operator|.
name|getRemoteException
argument_list|(
name|se
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|prepare
parameter_list|(
name|boolean
name|reload
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
operator|!
name|reload
condition|)
return|return;
comment|// relocate regions in case we have a new dead server or network hiccup
comment|// if not due to connection issue, the following code should run fast because it uses
comment|// cached location
name|boolean
name|skip
init|=
literal|false
decl_stmt|;
for|for
control|(
name|HLog
operator|.
name|Entry
name|entry
range|:
name|this
operator|.
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
comment|// filtering HLog meta entries
if|if
condition|(
name|CellUtil
operator|.
name|matchingFamily
argument_list|(
name|kv
argument_list|,
name|WALEdit
operator|.
name|METAFAMILY
argument_list|)
condition|)
continue|continue;
name|setLocation
argument_list|(
name|conn
operator|.
name|locateRegion
argument_list|(
name|tableName
argument_list|,
name|kv
operator|.
name|getRow
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|skip
operator|=
literal|true
expr_stmt|;
break|break;
block|}
comment|// use first log entry to relocate region because all entries are for one region
if|if
condition|(
name|skip
condition|)
break|break;
block|}
block|}
block|}
block|}
end_class

end_unit

