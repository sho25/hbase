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
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|ServiceException
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
name|DoNotRetryIOException
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
name|HBaseIOException
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
name|RegionLocations
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
name|ipc
operator|.
name|RpcControllerFactory
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
name|RequestConverter
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
name|ClientProtos
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
name|Collections
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
name|CancellationException
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
name|RunnableFuture
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
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|TimeoutException
import|;
end_import

begin_comment
comment|/**  * Caller that goes to replica if the primary region does no answer within a configurable  * timeout. If the timeout is reached, it calls all the secondary replicas, and returns  * the first answer. If the answer comes from one of the secondary replica, it will  * be marked as stale.  */
end_comment

begin_class
specifier|public
class|class
name|RpcRetryingCallerWithReadReplicas
block|{
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|RpcRetryingCallerWithReadReplicas
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|protected
specifier|final
name|ExecutorService
name|pool
decl_stmt|;
specifier|protected
specifier|final
name|ClusterConnection
name|cConnection
decl_stmt|;
specifier|protected
specifier|final
name|Configuration
name|conf
decl_stmt|;
specifier|protected
specifier|final
name|Get
name|get
decl_stmt|;
specifier|protected
specifier|final
name|TableName
name|tableName
decl_stmt|;
specifier|protected
specifier|final
name|int
name|timeBeforeReplicas
decl_stmt|;
specifier|private
specifier|final
name|int
name|callTimeout
decl_stmt|;
specifier|private
specifier|final
name|int
name|retries
decl_stmt|;
specifier|private
specifier|final
name|RpcControllerFactory
name|rpcControllerFactory
decl_stmt|;
specifier|private
specifier|final
name|RpcRetryingCallerFactory
name|rpcRetryingCallerFactory
decl_stmt|;
specifier|public
name|RpcRetryingCallerWithReadReplicas
parameter_list|(
name|RpcControllerFactory
name|rpcControllerFactory
parameter_list|,
name|TableName
name|tableName
parameter_list|,
name|ClusterConnection
name|cConnection
parameter_list|,
specifier|final
name|Get
name|get
parameter_list|,
name|ExecutorService
name|pool
parameter_list|,
name|int
name|retries
parameter_list|,
name|int
name|callTimeout
parameter_list|,
name|int
name|timeBeforeReplicas
parameter_list|)
block|{
name|this
operator|.
name|rpcControllerFactory
operator|=
name|rpcControllerFactory
expr_stmt|;
name|this
operator|.
name|tableName
operator|=
name|tableName
expr_stmt|;
name|this
operator|.
name|cConnection
operator|=
name|cConnection
expr_stmt|;
name|this
operator|.
name|conf
operator|=
name|cConnection
operator|.
name|getConfiguration
argument_list|()
expr_stmt|;
name|this
operator|.
name|get
operator|=
name|get
expr_stmt|;
name|this
operator|.
name|pool
operator|=
name|pool
expr_stmt|;
name|this
operator|.
name|retries
operator|=
name|retries
expr_stmt|;
name|this
operator|.
name|callTimeout
operator|=
name|callTimeout
expr_stmt|;
name|this
operator|.
name|timeBeforeReplicas
operator|=
name|timeBeforeReplicas
expr_stmt|;
name|this
operator|.
name|rpcRetryingCallerFactory
operator|=
operator|new
name|RpcRetryingCallerFactory
argument_list|(
name|conf
argument_list|)
expr_stmt|;
block|}
comment|/**    * A RegionServerCallable that takes into account the replicas, i.e.    * - the call can be on any replica    * - we need to stop retrying when the call is completed    * - we can be interrupted    */
class|class
name|ReplicaRegionServerCallable
extends|extends
name|RegionServerCallable
argument_list|<
name|Result
argument_list|>
block|{
specifier|final
name|int
name|id
decl_stmt|;
specifier|private
specifier|final
name|PayloadCarryingRpcController
name|controller
decl_stmt|;
specifier|public
name|ReplicaRegionServerCallable
parameter_list|(
name|int
name|id
parameter_list|,
name|HRegionLocation
name|location
parameter_list|)
block|{
name|super
argument_list|(
name|RpcRetryingCallerWithReadReplicas
operator|.
name|this
operator|.
name|cConnection
argument_list|,
name|RpcRetryingCallerWithReadReplicas
operator|.
name|this
operator|.
name|tableName
argument_list|,
name|get
operator|.
name|getRow
argument_list|()
argument_list|)
expr_stmt|;
name|this
operator|.
name|id
operator|=
name|id
expr_stmt|;
name|this
operator|.
name|location
operator|=
name|location
expr_stmt|;
name|this
operator|.
name|controller
operator|=
name|rpcControllerFactory
operator|.
name|newController
argument_list|()
expr_stmt|;
name|controller
operator|.
name|setPriority
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|startCancel
parameter_list|()
block|{
name|controller
operator|.
name|startCancel
argument_list|()
expr_stmt|;
block|}
comment|/**      * Two responsibilities      * - if the call is already completed (by another replica) stops the retries.      * - set the location to the right region, depending on the replica.      */
annotation|@
name|Override
specifier|public
name|void
name|prepare
parameter_list|(
specifier|final
name|boolean
name|reload
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|controller
operator|.
name|isCanceled
argument_list|()
condition|)
return|return;
if|if
condition|(
name|Thread
operator|.
name|interrupted
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|InterruptedIOException
argument_list|()
throw|;
block|}
if|if
condition|(
name|reload
operator|||
name|location
operator|==
literal|null
condition|)
block|{
name|RegionLocations
name|rl
init|=
name|getRegionLocations
argument_list|(
literal|false
argument_list|,
name|id
argument_list|,
name|cConnection
argument_list|,
name|tableName
argument_list|,
name|get
operator|.
name|getRow
argument_list|()
argument_list|)
decl_stmt|;
name|location
operator|=
name|id
operator|<
name|rl
operator|.
name|size
argument_list|()
condition|?
name|rl
operator|.
name|getRegionLocation
argument_list|(
name|id
argument_list|)
else|:
literal|null
expr_stmt|;
block|}
if|if
condition|(
name|location
operator|==
literal|null
operator|||
name|location
operator|.
name|getServerName
argument_list|()
operator|==
literal|null
condition|)
block|{
comment|// With this exception, there will be a retry. The location can be null for a replica
comment|//  when the table is created or after a split.
throw|throw
operator|new
name|HBaseIOException
argument_list|(
literal|"There is no location for replica id #"
operator|+
name|id
argument_list|)
throw|;
block|}
name|ServerName
name|dest
init|=
name|location
operator|.
name|getServerName
argument_list|()
decl_stmt|;
name|setStub
argument_list|(
name|cConnection
operator|.
name|getClient
argument_list|(
name|dest
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Result
name|call
parameter_list|(
name|int
name|callTimeout
parameter_list|)
throws|throws
name|Exception
block|{
if|if
condition|(
name|controller
operator|.
name|isCanceled
argument_list|()
condition|)
return|return
literal|null
return|;
if|if
condition|(
name|Thread
operator|.
name|interrupted
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|InterruptedIOException
argument_list|()
throw|;
block|}
name|byte
index|[]
name|reg
init|=
name|location
operator|.
name|getRegionInfo
argument_list|()
operator|.
name|getRegionName
argument_list|()
decl_stmt|;
name|ClientProtos
operator|.
name|GetRequest
name|request
init|=
name|RequestConverter
operator|.
name|buildGetRequest
argument_list|(
name|reg
argument_list|,
name|get
argument_list|)
decl_stmt|;
name|controller
operator|.
name|setCallTimeout
argument_list|(
name|callTimeout
argument_list|)
expr_stmt|;
try|try
block|{
name|ClientProtos
operator|.
name|GetResponse
name|response
init|=
name|getStub
argument_list|()
operator|.
name|get
argument_list|(
name|controller
argument_list|,
name|request
argument_list|)
decl_stmt|;
if|if
condition|(
name|response
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
return|return
name|ProtobufUtil
operator|.
name|toResult
argument_list|(
name|response
operator|.
name|getResult
argument_list|()
argument_list|)
return|;
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
block|}
comment|/**    * Algo:    * - we put the query into the execution pool.    * - after x ms, if we don't have a result, we add the queries for the secondary replicas    * - we take the first answer    * - when done, we cancel what's left. Cancelling means:    * - removing from the pool if the actual call was not started    * - interrupting the call if it has started    * Client side, we need to take into account    * - a call is not executed immediately after being put into the pool    * - a call is a thread. Let's not multiply the number of thread by the number of replicas.    * Server side, if we can cancel when it's still in the handler pool, it's much better, as a call    * can take some i/o.    *<p/>    * Globally, the number of retries, timeout and so on still applies, but it's per replica,    * not global. We continue until all retries are done, or all timeouts are exceeded.    */
specifier|public
specifier|synchronized
name|Result
name|call
parameter_list|()
throws|throws
name|DoNotRetryIOException
throws|,
name|InterruptedIOException
throws|,
name|RetriesExhaustedException
block|{
name|boolean
name|isTargetReplicaSpecified
init|=
operator|(
name|get
operator|.
name|getReplicaId
argument_list|()
operator|>=
literal|0
operator|)
decl_stmt|;
name|RegionLocations
name|rl
init|=
name|getRegionLocations
argument_list|(
literal|true
argument_list|,
operator|(
name|isTargetReplicaSpecified
condition|?
name|get
operator|.
name|getReplicaId
argument_list|()
else|:
name|RegionReplicaUtil
operator|.
name|DEFAULT_REPLICA_ID
operator|)
argument_list|,
name|cConnection
argument_list|,
name|tableName
argument_list|,
name|get
operator|.
name|getRow
argument_list|()
argument_list|)
decl_stmt|;
name|ResultBoundedCompletionService
name|cs
init|=
operator|new
name|ResultBoundedCompletionService
argument_list|(
name|pool
argument_list|,
name|rl
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|isTargetReplicaSpecified
condition|)
block|{
name|addCallsForReplica
argument_list|(
name|cs
argument_list|,
name|rl
argument_list|,
name|get
operator|.
name|getReplicaId
argument_list|()
argument_list|,
name|get
operator|.
name|getReplicaId
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|addCallsForReplica
argument_list|(
name|cs
argument_list|,
name|rl
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|)
expr_stmt|;
try|try
block|{
comment|// wait for the timeout to see whether the primary responds back
name|Future
argument_list|<
name|Result
argument_list|>
name|f
init|=
name|cs
operator|.
name|poll
argument_list|(
name|timeBeforeReplicas
argument_list|,
name|TimeUnit
operator|.
name|MICROSECONDS
argument_list|)
decl_stmt|;
comment|// Yes, microseconds
if|if
condition|(
name|f
operator|!=
literal|null
condition|)
block|{
return|return
name|f
operator|.
name|get
argument_list|()
return|;
comment|//great we got a response
block|}
block|}
catch|catch
parameter_list|(
name|ExecutionException
name|e
parameter_list|)
block|{
name|throwEnrichedException
argument_list|(
name|e
argument_list|,
name|retries
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|CancellationException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|InterruptedIOException
argument_list|()
throw|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|InterruptedIOException
argument_list|()
throw|;
block|}
comment|// submit call for the all of the secondaries at once
name|addCallsForReplica
argument_list|(
name|cs
argument_list|,
name|rl
argument_list|,
literal|1
argument_list|,
name|rl
operator|.
name|size
argument_list|()
operator|-
literal|1
argument_list|)
expr_stmt|;
block|}
try|try
block|{
try|try
block|{
name|Future
argument_list|<
name|Result
argument_list|>
name|f
init|=
name|cs
operator|.
name|take
argument_list|()
decl_stmt|;
return|return
name|f
operator|.
name|get
argument_list|()
return|;
block|}
catch|catch
parameter_list|(
name|ExecutionException
name|e
parameter_list|)
block|{
name|throwEnrichedException
argument_list|(
name|e
argument_list|,
name|retries
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|CancellationException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|InterruptedIOException
argument_list|()
throw|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|InterruptedIOException
argument_list|()
throw|;
block|}
finally|finally
block|{
comment|// We get there because we were interrupted or because one or more of the
comment|// calls succeeded or failed. In all case, we stop all our tasks.
name|cs
operator|.
name|cancelAll
argument_list|()
expr_stmt|;
block|}
return|return
literal|null
return|;
comment|// unreachable
block|}
comment|/**    * Extract the real exception from the ExecutionException, and throws what makes more    * sense.    */
specifier|static
name|void
name|throwEnrichedException
parameter_list|(
name|ExecutionException
name|e
parameter_list|,
name|int
name|retries
parameter_list|)
throws|throws
name|RetriesExhaustedException
throws|,
name|DoNotRetryIOException
block|{
name|Throwable
name|t
init|=
name|e
operator|.
name|getCause
argument_list|()
decl_stmt|;
assert|assert
name|t
operator|!=
literal|null
assert|;
comment|// That's what ExecutionException is about: holding an exception
if|if
condition|(
name|t
operator|instanceof
name|RetriesExhaustedException
condition|)
block|{
throw|throw
operator|(
name|RetriesExhaustedException
operator|)
name|t
throw|;
block|}
if|if
condition|(
name|t
operator|instanceof
name|DoNotRetryIOException
condition|)
block|{
throw|throw
operator|(
name|DoNotRetryIOException
operator|)
name|t
throw|;
block|}
name|RetriesExhaustedException
operator|.
name|ThrowableWithExtraContext
name|qt
init|=
operator|new
name|RetriesExhaustedException
operator|.
name|ThrowableWithExtraContext
argument_list|(
name|t
argument_list|,
name|EnvironmentEdgeManager
operator|.
name|currentTime
argument_list|()
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|RetriesExhaustedException
operator|.
name|ThrowableWithExtraContext
argument_list|>
name|exceptions
init|=
name|Collections
operator|.
name|singletonList
argument_list|(
name|qt
argument_list|)
decl_stmt|;
throw|throw
operator|new
name|RetriesExhaustedException
argument_list|(
name|retries
argument_list|,
name|exceptions
argument_list|)
throw|;
block|}
comment|/**    * Creates the calls and submit them    *    * @param cs  - the completion service to use for submitting    * @param rl  - the region locations    * @param min - the id of the first replica, inclusive    * @param max - the id of the last replica, inclusive.    */
specifier|private
name|void
name|addCallsForReplica
parameter_list|(
name|ResultBoundedCompletionService
name|cs
parameter_list|,
name|RegionLocations
name|rl
parameter_list|,
name|int
name|min
parameter_list|,
name|int
name|max
parameter_list|)
block|{
for|for
control|(
name|int
name|id
init|=
name|min
init|;
name|id
operator|<=
name|max
condition|;
name|id
operator|++
control|)
block|{
name|HRegionLocation
name|hrl
init|=
name|rl
operator|.
name|getRegionLocation
argument_list|(
name|id
argument_list|)
decl_stmt|;
name|ReplicaRegionServerCallable
name|callOnReplica
init|=
operator|new
name|ReplicaRegionServerCallable
argument_list|(
name|id
argument_list|,
name|hrl
argument_list|)
decl_stmt|;
name|cs
operator|.
name|submit
argument_list|(
name|callOnReplica
argument_list|,
name|callTimeout
argument_list|)
expr_stmt|;
block|}
block|}
specifier|static
name|RegionLocations
name|getRegionLocations
parameter_list|(
name|boolean
name|useCache
parameter_list|,
name|int
name|replicaId
parameter_list|,
name|ClusterConnection
name|cConnection
parameter_list|,
name|TableName
name|tableName
parameter_list|,
name|byte
index|[]
name|row
parameter_list|)
throws|throws
name|RetriesExhaustedException
throws|,
name|DoNotRetryIOException
throws|,
name|InterruptedIOException
block|{
name|RegionLocations
name|rl
decl_stmt|;
try|try
block|{
if|if
condition|(
operator|!
name|useCache
condition|)
block|{
name|rl
operator|=
name|cConnection
operator|.
name|relocateRegion
argument_list|(
name|tableName
argument_list|,
name|row
argument_list|,
name|replicaId
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|rl
operator|=
name|cConnection
operator|.
name|locateRegion
argument_list|(
name|tableName
argument_list|,
name|row
argument_list|,
name|useCache
argument_list|,
literal|true
argument_list|,
name|replicaId
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|DoNotRetryIOException
name|e
parameter_list|)
block|{
throw|throw
name|e
throw|;
block|}
catch|catch
parameter_list|(
name|RetriesExhaustedException
name|e
parameter_list|)
block|{
throw|throw
name|e
throw|;
block|}
catch|catch
parameter_list|(
name|InterruptedIOException
name|e
parameter_list|)
block|{
throw|throw
name|e
throw|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RetriesExhaustedException
argument_list|(
literal|"Can't get the location"
argument_list|,
name|e
argument_list|)
throw|;
block|}
if|if
condition|(
name|rl
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|RetriesExhaustedException
argument_list|(
literal|"Can't get the locations"
argument_list|)
throw|;
block|}
return|return
name|rl
return|;
block|}
comment|/**    * A completion service for the RpcRetryingCallerFactory.    * Keeps the list of the futures, and allows to cancel them all.    * This means as well that it can be used for a small set of tasks only.    *<br>Implementation is not Thread safe.    */
specifier|public
class|class
name|ResultBoundedCompletionService
block|{
specifier|private
specifier|final
name|Executor
name|executor
decl_stmt|;
specifier|private
specifier|final
name|QueueingFuture
index|[]
name|tasks
decl_stmt|;
comment|// all the tasks
specifier|private
specifier|volatile
name|QueueingFuture
name|completed
init|=
literal|null
decl_stmt|;
class|class
name|QueueingFuture
implements|implements
name|RunnableFuture
argument_list|<
name|Result
argument_list|>
block|{
specifier|private
specifier|final
name|ReplicaRegionServerCallable
name|future
decl_stmt|;
specifier|private
name|Result
name|result
init|=
literal|null
decl_stmt|;
specifier|private
name|ExecutionException
name|exeEx
init|=
literal|null
decl_stmt|;
specifier|private
specifier|volatile
name|boolean
name|canceled
decl_stmt|;
specifier|private
specifier|final
name|int
name|callTimeout
decl_stmt|;
specifier|private
specifier|final
name|RpcRetryingCaller
argument_list|<
name|Result
argument_list|>
name|retryingCaller
decl_stmt|;
specifier|public
name|QueueingFuture
parameter_list|(
name|ReplicaRegionServerCallable
name|future
parameter_list|,
name|int
name|callTimeout
parameter_list|)
block|{
name|this
operator|.
name|future
operator|=
name|future
expr_stmt|;
name|this
operator|.
name|callTimeout
operator|=
name|callTimeout
expr_stmt|;
name|this
operator|.
name|retryingCaller
operator|=
name|rpcRetryingCallerFactory
operator|.
expr|<
name|Result
operator|>
name|newCaller
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
try|try
block|{
if|if
condition|(
operator|!
name|canceled
condition|)
block|{
name|result
operator|=
name|rpcRetryingCallerFactory
operator|.
expr|<
name|Result
operator|>
name|newCaller
argument_list|()
operator|.
name|callWithRetries
argument_list|(
name|future
argument_list|,
name|callTimeout
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
name|exeEx
operator|=
operator|new
name|ExecutionException
argument_list|(
name|t
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
if|if
condition|(
operator|!
name|canceled
operator|&&
name|completed
operator|==
literal|null
condition|)
block|{
name|completed
operator|=
name|QueueingFuture
operator|.
name|this
expr_stmt|;
synchronized|synchronized
init|(
name|tasks
init|)
block|{
name|tasks
operator|.
name|notify
argument_list|()
expr_stmt|;
block|}
block|}
block|}
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|cancel
parameter_list|(
name|boolean
name|mayInterruptIfRunning
parameter_list|)
block|{
if|if
condition|(
name|result
operator|!=
literal|null
operator|||
name|exeEx
operator|!=
literal|null
condition|)
return|return
literal|false
return|;
name|retryingCaller
operator|.
name|cancel
argument_list|()
expr_stmt|;
name|future
operator|.
name|startCancel
argument_list|()
expr_stmt|;
name|canceled
operator|=
literal|true
expr_stmt|;
return|return
literal|true
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isCancelled
parameter_list|()
block|{
return|return
name|canceled
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isDone
parameter_list|()
block|{
return|return
name|result
operator|!=
literal|null
operator|||
name|exeEx
operator|!=
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|Result
name|get
parameter_list|()
throws|throws
name|InterruptedException
throws|,
name|ExecutionException
block|{
try|try
block|{
return|return
name|get
argument_list|(
literal|1000
argument_list|,
name|TimeUnit
operator|.
name|DAYS
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|TimeoutException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"You did wait for 1000 days here?"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|Result
name|get
parameter_list|(
name|long
name|timeout
parameter_list|,
name|TimeUnit
name|unit
parameter_list|)
throws|throws
name|InterruptedException
throws|,
name|ExecutionException
throws|,
name|TimeoutException
block|{
synchronized|synchronized
init|(
name|tasks
init|)
block|{
if|if
condition|(
name|result
operator|!=
literal|null
condition|)
block|{
return|return
name|result
return|;
block|}
if|if
condition|(
name|exeEx
operator|!=
literal|null
condition|)
block|{
throw|throw
name|exeEx
throw|;
block|}
name|unit
operator|.
name|timedWait
argument_list|(
name|tasks
argument_list|,
name|timeout
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|result
operator|!=
literal|null
condition|)
block|{
return|return
name|result
return|;
block|}
if|if
condition|(
name|exeEx
operator|!=
literal|null
condition|)
block|{
throw|throw
name|exeEx
throw|;
block|}
throw|throw
operator|new
name|TimeoutException
argument_list|()
throw|;
block|}
block|}
specifier|public
name|ResultBoundedCompletionService
parameter_list|(
name|Executor
name|executor
parameter_list|,
name|int
name|maxTasks
parameter_list|)
block|{
name|this
operator|.
name|executor
operator|=
name|executor
expr_stmt|;
name|this
operator|.
name|tasks
operator|=
operator|new
name|QueueingFuture
index|[
name|maxTasks
index|]
expr_stmt|;
block|}
specifier|public
name|void
name|submit
parameter_list|(
name|ReplicaRegionServerCallable
name|task
parameter_list|,
name|int
name|callTimeout
parameter_list|)
block|{
name|QueueingFuture
name|newFuture
init|=
operator|new
name|QueueingFuture
argument_list|(
name|task
argument_list|,
name|callTimeout
argument_list|)
decl_stmt|;
name|executor
operator|.
name|execute
argument_list|(
name|newFuture
argument_list|)
expr_stmt|;
name|tasks
index|[
name|task
operator|.
name|id
index|]
operator|=
name|newFuture
expr_stmt|;
block|}
specifier|public
name|QueueingFuture
name|take
parameter_list|()
throws|throws
name|InterruptedException
block|{
synchronized|synchronized
init|(
name|tasks
init|)
block|{
if|if
condition|(
name|completed
operator|==
literal|null
condition|)
name|tasks
operator|.
name|wait
argument_list|()
expr_stmt|;
block|}
return|return
name|completed
return|;
block|}
specifier|public
name|QueueingFuture
name|poll
parameter_list|(
name|long
name|timeout
parameter_list|,
name|TimeUnit
name|unit
parameter_list|)
throws|throws
name|InterruptedException
block|{
synchronized|synchronized
init|(
name|tasks
init|)
block|{
if|if
condition|(
name|completed
operator|==
literal|null
condition|)
name|unit
operator|.
name|timedWait
argument_list|(
name|tasks
argument_list|,
name|timeout
argument_list|)
expr_stmt|;
block|}
return|return
name|completed
return|;
block|}
specifier|public
name|void
name|cancelAll
parameter_list|()
block|{
for|for
control|(
name|QueueingFuture
name|future
range|:
name|tasks
control|)
block|{
if|if
condition|(
name|future
operator|!=
literal|null
condition|)
name|future
operator|.
name|cancel
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit

