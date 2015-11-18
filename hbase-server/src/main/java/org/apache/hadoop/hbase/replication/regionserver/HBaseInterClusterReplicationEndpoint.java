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
name|net
operator|.
name|ConnectException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|SocketTimeoutException
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
name|concurrent
operator|.
name|Callable
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
name|ExecutionException
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
name|Future
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
name|SynchronousQueue
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
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|annotations
operator|.
name|VisibleForTesting
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
name|TableNotFoundException
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
operator|.
name|AdminService
operator|.
name|BlockingInterface
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
name|wal
operator|.
name|WAL
operator|.
name|Entry
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
name|replication
operator|.
name|HBaseReplicationEndpoint
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
name|replication
operator|.
name|ReplicationPeer
operator|.
name|PeerState
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
name|replication
operator|.
name|regionserver
operator|.
name|ReplicationSinkManager
operator|.
name|SinkPeer
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
name|ipc
operator|.
name|RemoteException
import|;
end_import

begin_comment
comment|/**  * A {@link org.apache.hadoop.hbase.replication.ReplicationEndpoint}   * implementation for replicating to another HBase cluster.  * For the slave cluster it selects a random number of peers  * using a replication ratio. For example, if replication ration = 0.1  * and slave cluster has 100 region servers, 10 will be selected.  *<p>  * A stream is considered down when we cannot contact a region server on the  * peer cluster for more than 55 seconds by default.  *</p>  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|HBaseInterClusterReplicationEndpoint
extends|extends
name|HBaseReplicationEndpoint
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
name|HBaseInterClusterReplicationEndpoint
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|HConnection
name|conn
decl_stmt|;
specifier|private
name|Configuration
name|conf
decl_stmt|;
comment|// How long should we sleep for each retry
specifier|private
name|long
name|sleepForRetries
decl_stmt|;
comment|// Maximum number of retries before taking bold actions
specifier|private
name|int
name|maxRetriesMultiplier
decl_stmt|;
comment|// Socket timeouts require even bolder actions since we don't want to DDOS
specifier|private
name|int
name|socketTimeoutMultiplier
decl_stmt|;
comment|//Metrics for this source
specifier|private
name|MetricsSource
name|metrics
decl_stmt|;
comment|// Handles connecting to peer region servers
specifier|private
name|ReplicationSinkManager
name|replicationSinkMgr
decl_stmt|;
specifier|private
name|boolean
name|peersSelected
init|=
literal|false
decl_stmt|;
specifier|private
name|ThreadPoolExecutor
name|exec
decl_stmt|;
specifier|private
name|int
name|maxThreads
decl_stmt|;
annotation|@
name|Override
specifier|public
name|void
name|init
parameter_list|(
name|Context
name|context
parameter_list|)
throws|throws
name|IOException
block|{
name|super
operator|.
name|init
argument_list|(
name|context
argument_list|)
expr_stmt|;
name|this
operator|.
name|conf
operator|=
name|HBaseConfiguration
operator|.
name|create
argument_list|(
name|ctx
operator|.
name|getConfiguration
argument_list|()
argument_list|)
expr_stmt|;
name|decorateConf
argument_list|()
expr_stmt|;
name|this
operator|.
name|maxRetriesMultiplier
operator|=
name|this
operator|.
name|conf
operator|.
name|getInt
argument_list|(
literal|"replication.source.maxretriesmultiplier"
argument_list|,
literal|300
argument_list|)
expr_stmt|;
name|this
operator|.
name|socketTimeoutMultiplier
operator|=
name|this
operator|.
name|conf
operator|.
name|getInt
argument_list|(
literal|"replication.source.socketTimeoutMultiplier"
argument_list|,
name|maxRetriesMultiplier
argument_list|)
expr_stmt|;
comment|// TODO: This connection is replication specific or we should make it particular to
comment|// replication and make replication specific settings such as compression or codec to use
comment|// passing Cells.
name|this
operator|.
name|conn
operator|=
operator|(
name|HConnection
operator|)
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
name|sleepForRetries
operator|=
name|this
operator|.
name|conf
operator|.
name|getLong
argument_list|(
literal|"replication.source.sleepforretries"
argument_list|,
literal|1000
argument_list|)
expr_stmt|;
name|this
operator|.
name|metrics
operator|=
name|context
operator|.
name|getMetrics
argument_list|()
expr_stmt|;
comment|// ReplicationQueueInfo parses the peerId out of the znode for us
name|this
operator|.
name|replicationSinkMgr
operator|=
operator|new
name|ReplicationSinkManager
argument_list|(
name|conn
argument_list|,
name|ctx
operator|.
name|getPeerId
argument_list|()
argument_list|,
name|this
argument_list|,
name|this
operator|.
name|conf
argument_list|)
expr_stmt|;
comment|// per sink thread pool
name|this
operator|.
name|maxThreads
operator|=
name|this
operator|.
name|conf
operator|.
name|getInt
argument_list|(
name|HConstants
operator|.
name|REPLICATION_SOURCE_MAXTHREADS_KEY
argument_list|,
name|HConstants
operator|.
name|REPLICATION_SOURCE_MAXTHREADS_DEFAULT
argument_list|)
expr_stmt|;
name|this
operator|.
name|exec
operator|=
operator|new
name|ThreadPoolExecutor
argument_list|(
literal|1
argument_list|,
name|maxThreads
argument_list|,
literal|60
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|,
operator|new
name|SynchronousQueue
argument_list|<
name|Runnable
argument_list|>
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|decorateConf
parameter_list|()
block|{
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
specifier|private
name|void
name|connectToPeers
parameter_list|()
block|{
name|getRegionServers
argument_list|()
expr_stmt|;
name|int
name|sleepMultiplier
init|=
literal|1
decl_stmt|;
comment|// Connect to peer cluster first, unless we have to stop
while|while
condition|(
name|this
operator|.
name|isRunning
argument_list|()
operator|&&
name|replicationSinkMgr
operator|.
name|getSinks
argument_list|()
operator|.
name|size
argument_list|()
operator|==
literal|0
condition|)
block|{
name|replicationSinkMgr
operator|.
name|chooseSinks
argument_list|()
expr_stmt|;
if|if
condition|(
name|this
operator|.
name|isRunning
argument_list|()
operator|&&
name|replicationSinkMgr
operator|.
name|getSinks
argument_list|()
operator|.
name|size
argument_list|()
operator|==
literal|0
condition|)
block|{
if|if
condition|(
name|sleepForRetries
argument_list|(
literal|"Waiting for peers"
argument_list|,
name|sleepMultiplier
argument_list|)
condition|)
block|{
name|sleepMultiplier
operator|++
expr_stmt|;
block|}
block|}
block|}
block|}
comment|/**    * Do the sleeping logic    * @param msg Why we sleep    * @param sleepMultiplier by how many times the default sleeping time is augmented    * @return True if<code>sleepMultiplier</code> is&lt;<code>maxRetriesMultiplier</code>    */
specifier|protected
name|boolean
name|sleepForRetries
parameter_list|(
name|String
name|msg
parameter_list|,
name|int
name|sleepMultiplier
parameter_list|)
block|{
try|try
block|{
if|if
condition|(
name|LOG
operator|.
name|isTraceEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|trace
argument_list|(
name|msg
operator|+
literal|", sleeping "
operator|+
name|sleepForRetries
operator|+
literal|" times "
operator|+
name|sleepMultiplier
argument_list|)
expr_stmt|;
block|}
name|Thread
operator|.
name|sleep
argument_list|(
name|this
operator|.
name|sleepForRetries
operator|*
name|sleepMultiplier
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Interrupted while sleeping between retries"
argument_list|)
expr_stmt|;
block|}
return|return
name|sleepMultiplier
operator|<
name|maxRetriesMultiplier
return|;
block|}
comment|/**    * Do the shipping logic    */
annotation|@
name|Override
specifier|public
name|boolean
name|replicate
parameter_list|(
name|ReplicateContext
name|replicateContext
parameter_list|)
block|{
name|List
argument_list|<
name|Entry
argument_list|>
name|entries
init|=
name|replicateContext
operator|.
name|getEntries
argument_list|()
decl_stmt|;
name|String
name|walGroupId
init|=
name|replicateContext
operator|.
name|getWalGroupId
argument_list|()
decl_stmt|;
name|int
name|sleepMultiplier
init|=
literal|1
decl_stmt|;
if|if
condition|(
operator|!
name|peersSelected
operator|&&
name|this
operator|.
name|isRunning
argument_list|()
condition|)
block|{
name|connectToPeers
argument_list|()
expr_stmt|;
name|peersSelected
operator|=
literal|true
expr_stmt|;
block|}
if|if
condition|(
name|replicationSinkMgr
operator|.
name|getSinks
argument_list|()
operator|.
name|size
argument_list|()
operator|==
literal|0
condition|)
block|{
return|return
literal|false
return|;
block|}
comment|// minimum of: configured threads, number of 100-waledit batches,
comment|//  and number of current sinks
name|int
name|n
init|=
name|Math
operator|.
name|min
argument_list|(
name|Math
operator|.
name|min
argument_list|(
name|this
operator|.
name|maxThreads
argument_list|,
name|entries
operator|.
name|size
argument_list|()
operator|/
literal|100
operator|+
literal|1
argument_list|)
argument_list|,
name|replicationSinkMgr
operator|.
name|getSinks
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|List
argument_list|<
name|Entry
argument_list|>
argument_list|>
name|entryLists
init|=
operator|new
name|ArrayList
argument_list|<
name|List
argument_list|<
name|Entry
argument_list|>
argument_list|>
argument_list|(
name|n
argument_list|)
decl_stmt|;
if|if
condition|(
name|n
operator|==
literal|1
condition|)
block|{
name|entryLists
operator|.
name|add
argument_list|(
name|entries
argument_list|)
expr_stmt|;
block|}
else|else
block|{
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|n
condition|;
name|i
operator|++
control|)
block|{
name|entryLists
operator|.
name|add
argument_list|(
operator|new
name|ArrayList
argument_list|<
name|Entry
argument_list|>
argument_list|(
name|entries
operator|.
name|size
argument_list|()
operator|/
name|n
operator|+
literal|1
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// now group by region
for|for
control|(
name|Entry
name|e
range|:
name|entries
control|)
block|{
name|entryLists
operator|.
name|get
argument_list|(
name|Math
operator|.
name|abs
argument_list|(
name|Bytes
operator|.
name|hashCode
argument_list|(
name|e
operator|.
name|getKey
argument_list|()
operator|.
name|getEncodedRegionName
argument_list|()
argument_list|)
operator|%
name|n
argument_list|)
argument_list|)
operator|.
name|add
argument_list|(
name|e
argument_list|)
expr_stmt|;
block|}
block|}
while|while
condition|(
name|this
operator|.
name|isRunning
argument_list|()
condition|)
block|{
if|if
condition|(
operator|!
name|isPeerEnabled
argument_list|()
condition|)
block|{
if|if
condition|(
name|sleepForRetries
argument_list|(
literal|"Replication is disabled"
argument_list|,
name|sleepMultiplier
argument_list|)
condition|)
block|{
name|sleepMultiplier
operator|++
expr_stmt|;
block|}
continue|continue;
block|}
try|try
block|{
if|if
condition|(
name|LOG
operator|.
name|isTraceEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|trace
argument_list|(
literal|"Replicating "
operator|+
name|entries
operator|.
name|size
argument_list|()
operator|+
literal|" entries of total size "
operator|+
name|replicateContext
operator|.
name|getSize
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|List
argument_list|<
name|Future
argument_list|<
name|Integer
argument_list|>
argument_list|>
name|futures
init|=
operator|new
name|ArrayList
argument_list|<
name|Future
argument_list|<
name|Integer
argument_list|>
argument_list|>
argument_list|(
name|entryLists
operator|.
name|size
argument_list|()
argument_list|)
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
name|entryLists
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
operator|!
name|entryLists
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
if|if
condition|(
name|LOG
operator|.
name|isTraceEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|trace
argument_list|(
literal|"Submitting "
operator|+
name|entryLists
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|size
argument_list|()
operator|+
literal|" entries of total size "
operator|+
name|replicateContext
operator|.
name|getSize
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|// RuntimeExceptions encountered here bubble up and are handled in ReplicationSource
name|futures
operator|.
name|add
argument_list|(
name|exec
operator|.
name|submit
argument_list|(
name|createReplicator
argument_list|(
name|entryLists
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|,
name|i
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
name|IOException
name|iox
init|=
literal|null
decl_stmt|;
for|for
control|(
name|int
name|index
init|=
name|futures
operator|.
name|size
argument_list|()
operator|-
literal|1
init|;
name|index
operator|>=
literal|0
condition|;
name|index
operator|--
control|)
block|{
try|try
block|{
comment|// wait for all futures, remove successful parts
comment|// (only the remaining parts will be retried)
name|Future
argument_list|<
name|Integer
argument_list|>
name|f
init|=
name|futures
operator|.
name|get
argument_list|(
name|index
argument_list|)
decl_stmt|;
name|entryLists
operator|.
name|remove
argument_list|(
name|f
operator|.
name|get
argument_list|()
operator|.
name|intValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|ie
parameter_list|)
block|{
name|iox
operator|=
operator|new
name|IOException
argument_list|(
name|ie
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ExecutionException
name|ee
parameter_list|)
block|{
comment|// cause must be an IOException
name|iox
operator|=
operator|(
name|IOException
operator|)
name|ee
operator|.
name|getCause
argument_list|()
expr_stmt|;
block|}
block|}
if|if
condition|(
name|iox
operator|!=
literal|null
condition|)
block|{
comment|// if we had any exceptions, try again
throw|throw
name|iox
throw|;
block|}
comment|// update metrics
name|this
operator|.
name|metrics
operator|.
name|setAgeOfLastShippedOp
argument_list|(
name|entries
operator|.
name|get
argument_list|(
name|entries
operator|.
name|size
argument_list|()
operator|-
literal|1
argument_list|)
operator|.
name|getKey
argument_list|()
operator|.
name|getWriteTime
argument_list|()
argument_list|,
name|walGroupId
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
catch|catch
parameter_list|(
name|IOException
name|ioe
parameter_list|)
block|{
comment|// Didn't ship anything, but must still age the last time we did
name|this
operator|.
name|metrics
operator|.
name|refreshAgeOfLastShippedOp
argument_list|(
name|walGroupId
argument_list|)
expr_stmt|;
if|if
condition|(
name|ioe
operator|instanceof
name|RemoteException
condition|)
block|{
name|ioe
operator|=
operator|(
operator|(
name|RemoteException
operator|)
name|ioe
operator|)
operator|.
name|unwrapRemoteException
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|warn
argument_list|(
literal|"Can't replicate because of an error on the remote cluster: "
argument_list|,
name|ioe
argument_list|)
expr_stmt|;
if|if
condition|(
name|ioe
operator|instanceof
name|TableNotFoundException
condition|)
block|{
if|if
condition|(
name|sleepForRetries
argument_list|(
literal|"A table is missing in the peer cluster. "
operator|+
literal|"Replication cannot proceed without losing data."
argument_list|,
name|sleepMultiplier
argument_list|)
condition|)
block|{
name|sleepMultiplier
operator|++
expr_stmt|;
block|}
block|}
block|}
else|else
block|{
if|if
condition|(
name|ioe
operator|instanceof
name|SocketTimeoutException
condition|)
block|{
comment|// This exception means we waited for more than 60s and nothing
comment|// happened, the cluster is alive and calling it right away
comment|// even for a test just makes things worse.
name|sleepForRetries
argument_list|(
literal|"Encountered a SocketTimeoutException. Since the "
operator|+
literal|"call to the remote cluster timed out, which is usually "
operator|+
literal|"caused by a machine failure or a massive slowdown"
argument_list|,
name|this
operator|.
name|socketTimeoutMultiplier
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|ioe
operator|instanceof
name|ConnectException
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Peer is unavailable, rechecking all sinks: "
argument_list|,
name|ioe
argument_list|)
expr_stmt|;
name|replicationSinkMgr
operator|.
name|chooseSinks
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Can't replicate because of a local or network error: "
argument_list|,
name|ioe
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|sleepForRetries
argument_list|(
literal|"Since we are unable to replicate"
argument_list|,
name|sleepMultiplier
argument_list|)
condition|)
block|{
name|sleepMultiplier
operator|++
expr_stmt|;
block|}
block|}
block|}
return|return
literal|false
return|;
comment|// in case we exited before replicating
block|}
specifier|protected
name|boolean
name|isPeerEnabled
parameter_list|()
block|{
return|return
name|ctx
operator|.
name|getReplicationPeer
argument_list|()
operator|.
name|getPeerState
argument_list|()
operator|==
name|PeerState
operator|.
name|ENABLED
return|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|doStop
parameter_list|()
block|{
name|disconnect
argument_list|()
expr_stmt|;
comment|//don't call super.doStop()
if|if
condition|(
name|this
operator|.
name|conn
operator|!=
literal|null
condition|)
block|{
try|try
block|{
name|this
operator|.
name|conn
operator|.
name|close
argument_list|()
expr_stmt|;
name|this
operator|.
name|conn
operator|=
literal|null
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
name|warn
argument_list|(
literal|"Failed to close the connection"
argument_list|)
expr_stmt|;
block|}
block|}
name|exec
operator|.
name|shutdownNow
argument_list|()
expr_stmt|;
name|notifyStopped
argument_list|()
expr_stmt|;
block|}
comment|// is this needed? Nobody else will call doStop() otherwise
annotation|@
name|Override
specifier|public
name|State
name|stopAndWait
parameter_list|()
block|{
name|doStop
argument_list|()
expr_stmt|;
return|return
name|super
operator|.
name|stopAndWait
argument_list|()
return|;
block|}
annotation|@
name|VisibleForTesting
specifier|protected
name|Replicator
name|createReplicator
parameter_list|(
name|List
argument_list|<
name|Entry
argument_list|>
name|entries
parameter_list|,
name|int
name|ordinal
parameter_list|)
block|{
return|return
operator|new
name|Replicator
argument_list|(
name|entries
argument_list|,
name|ordinal
argument_list|)
return|;
block|}
annotation|@
name|VisibleForTesting
specifier|protected
class|class
name|Replicator
implements|implements
name|Callable
argument_list|<
name|Integer
argument_list|>
block|{
specifier|private
name|List
argument_list|<
name|Entry
argument_list|>
name|entries
decl_stmt|;
specifier|private
name|int
name|ordinal
decl_stmt|;
specifier|public
name|Replicator
parameter_list|(
name|List
argument_list|<
name|Entry
argument_list|>
name|entries
parameter_list|,
name|int
name|ordinal
parameter_list|)
block|{
name|this
operator|.
name|entries
operator|=
name|entries
expr_stmt|;
name|this
operator|.
name|ordinal
operator|=
name|ordinal
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Integer
name|call
parameter_list|()
throws|throws
name|IOException
block|{
name|SinkPeer
name|sinkPeer
init|=
literal|null
decl_stmt|;
try|try
block|{
name|sinkPeer
operator|=
name|replicationSinkMgr
operator|.
name|getReplicationSink
argument_list|()
expr_stmt|;
name|BlockingInterface
name|rrs
init|=
name|sinkPeer
operator|.
name|getRegionServer
argument_list|()
decl_stmt|;
name|ReplicationProtbufUtil
operator|.
name|replicateWALEntry
argument_list|(
name|rrs
argument_list|,
name|entries
operator|.
name|toArray
argument_list|(
operator|new
name|Entry
index|[
name|entries
operator|.
name|size
argument_list|()
index|]
argument_list|)
argument_list|)
expr_stmt|;
name|replicationSinkMgr
operator|.
name|reportSinkSuccess
argument_list|(
name|sinkPeer
argument_list|)
expr_stmt|;
return|return
name|ordinal
return|;
block|}
catch|catch
parameter_list|(
name|IOException
name|ioe
parameter_list|)
block|{
if|if
condition|(
name|sinkPeer
operator|!=
literal|null
condition|)
block|{
name|replicationSinkMgr
operator|.
name|reportBadSink
argument_list|(
name|sinkPeer
argument_list|)
expr_stmt|;
block|}
throw|throw
name|ioe
throw|;
block|}
block|}
block|}
block|}
end_class

end_unit

