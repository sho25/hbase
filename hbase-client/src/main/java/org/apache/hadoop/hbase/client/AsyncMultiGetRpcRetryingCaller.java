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
import|import static
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
name|ConnectionUtils
operator|.
name|SLEEP_DELTA_NS
import|;
end_import

begin_import
import|import static
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
name|ConnectionUtils
operator|.
name|getPauseTime
import|;
end_import

begin_import
import|import static
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
name|ConnectionUtils
operator|.
name|resetController
import|;
end_import

begin_import
import|import static
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
name|ConnectionUtils
operator|.
name|retries2Attempts
import|;
end_import

begin_import
import|import static
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
name|ConnectionUtils
operator|.
name|translateException
import|;
end_import

begin_import
import|import static
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
name|CollectionUtils
operator|.
name|computeIfAbsent
import|;
end_import

begin_import
import|import
name|io
operator|.
name|netty
operator|.
name|util
operator|.
name|HashedWheelTimer
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
name|Collections
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|IdentityHashMap
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
name|Optional
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
name|CompletableFuture
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
name|ConcurrentHashMap
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
name|ConcurrentLinkedQueue
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
name|ConcurrentMap
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
name|ConcurrentSkipListMap
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
name|function
operator|.
name|Supplier
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|stream
operator|.
name|Collectors
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|stream
operator|.
name|Stream
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
name|MultiResponse
operator|.
name|RegionResult
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
name|RetriesExhaustedException
operator|.
name|ThrowableWithExtraContext
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
name|HBaseRpcController
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
name|shaded
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
name|shaded
operator|.
name|protobuf
operator|.
name|ResponseConverter
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
name|shaded
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|ClientProtos
operator|.
name|ClientService
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
name|protobuf
operator|.
name|generated
operator|.
name|HBaseProtos
operator|.
name|RegionSpecifier
operator|.
name|RegionSpecifierType
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
name|EnvironmentEdgeManager
import|;
end_import

begin_comment
comment|/**  * Retry caller for multi get.  *<p>  * Notice that, the {@link #operationTimeoutNs} is the total time limit now which is the same with  * other single operations  *<p>  * And the {@link #maxAttempts} is a limit for each single get in the batch logically. In the  * implementation, we will record a {@code tries} parameter for each operation group, and if it is  * split to several groups when retrying, the sub groups will inherit {@code tries}. You can imagine  * that the whole retrying process is a tree, and the {@link #maxAttempts} is the limit of the depth  * of the tree.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
class|class
name|AsyncMultiGetRpcRetryingCaller
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
name|AsyncMultiGetRpcRetryingCaller
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|HashedWheelTimer
name|retryTimer
decl_stmt|;
specifier|private
specifier|final
name|AsyncConnectionImpl
name|conn
decl_stmt|;
specifier|private
specifier|final
name|TableName
name|tableName
decl_stmt|;
specifier|private
specifier|final
name|List
argument_list|<
name|Get
argument_list|>
name|gets
decl_stmt|;
specifier|private
specifier|final
name|List
argument_list|<
name|CompletableFuture
argument_list|<
name|Result
argument_list|>
argument_list|>
name|futures
decl_stmt|;
specifier|private
specifier|final
name|IdentityHashMap
argument_list|<
name|Get
argument_list|,
name|CompletableFuture
argument_list|<
name|Result
argument_list|>
argument_list|>
name|get2Future
decl_stmt|;
specifier|private
specifier|final
name|IdentityHashMap
argument_list|<
name|Get
argument_list|,
name|List
argument_list|<
name|ThrowableWithExtraContext
argument_list|>
argument_list|>
name|get2Errors
decl_stmt|;
specifier|private
specifier|final
name|long
name|pauseNs
decl_stmt|;
specifier|private
specifier|final
name|int
name|maxAttempts
decl_stmt|;
specifier|private
specifier|final
name|long
name|operationTimeoutNs
decl_stmt|;
specifier|private
specifier|final
name|long
name|rpcTimeoutNs
decl_stmt|;
specifier|private
specifier|final
name|int
name|startLogErrorsCnt
decl_stmt|;
specifier|private
specifier|final
name|long
name|startNs
decl_stmt|;
comment|// we can not use HRegionLocation as the map key because the hashCode and equals method of
comment|// HRegionLocation only consider serverName.
specifier|private
specifier|static
specifier|final
class|class
name|RegionRequest
block|{
specifier|public
specifier|final
name|HRegionLocation
name|loc
decl_stmt|;
specifier|public
specifier|final
name|ConcurrentLinkedQueue
argument_list|<
name|Get
argument_list|>
name|gets
init|=
operator|new
name|ConcurrentLinkedQueue
argument_list|<>
argument_list|()
decl_stmt|;
specifier|public
name|RegionRequest
parameter_list|(
name|HRegionLocation
name|loc
parameter_list|)
block|{
name|this
operator|.
name|loc
operator|=
name|loc
expr_stmt|;
block|}
block|}
specifier|public
name|AsyncMultiGetRpcRetryingCaller
parameter_list|(
name|HashedWheelTimer
name|retryTimer
parameter_list|,
name|AsyncConnectionImpl
name|conn
parameter_list|,
name|TableName
name|tableName
parameter_list|,
name|List
argument_list|<
name|Get
argument_list|>
name|gets
parameter_list|,
name|long
name|pauseNs
parameter_list|,
name|int
name|maxRetries
parameter_list|,
name|long
name|operationTimeoutNs
parameter_list|,
name|long
name|rpcTimeoutNs
parameter_list|,
name|int
name|startLogErrorsCnt
parameter_list|)
block|{
name|this
operator|.
name|retryTimer
operator|=
name|retryTimer
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
name|gets
operator|=
name|gets
expr_stmt|;
name|this
operator|.
name|pauseNs
operator|=
name|pauseNs
expr_stmt|;
name|this
operator|.
name|maxAttempts
operator|=
name|retries2Attempts
argument_list|(
name|maxRetries
argument_list|)
expr_stmt|;
name|this
operator|.
name|operationTimeoutNs
operator|=
name|operationTimeoutNs
expr_stmt|;
name|this
operator|.
name|rpcTimeoutNs
operator|=
name|rpcTimeoutNs
expr_stmt|;
name|this
operator|.
name|startLogErrorsCnt
operator|=
name|startLogErrorsCnt
expr_stmt|;
name|this
operator|.
name|futures
operator|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|gets
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|this
operator|.
name|get2Future
operator|=
operator|new
name|IdentityHashMap
argument_list|<>
argument_list|(
name|gets
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|gets
operator|.
name|forEach
argument_list|(
name|get
lambda|->
name|futures
operator|.
name|add
argument_list|(
name|get2Future
operator|.
name|computeIfAbsent
argument_list|(
name|get
argument_list|,
name|k
lambda|->
operator|new
name|CompletableFuture
argument_list|<>
argument_list|()
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|this
operator|.
name|get2Errors
operator|=
operator|new
name|IdentityHashMap
argument_list|<>
argument_list|()
expr_stmt|;
name|this
operator|.
name|startNs
operator|=
name|System
operator|.
name|nanoTime
argument_list|()
expr_stmt|;
block|}
specifier|private
name|long
name|remainingTimeNs
parameter_list|()
block|{
return|return
name|operationTimeoutNs
operator|-
operator|(
name|System
operator|.
name|nanoTime
argument_list|()
operator|-
name|startNs
operator|)
return|;
block|}
specifier|private
name|List
argument_list|<
name|ThrowableWithExtraContext
argument_list|>
name|removeErrors
parameter_list|(
name|Get
name|get
parameter_list|)
block|{
synchronized|synchronized
init|(
name|get2Errors
init|)
block|{
return|return
name|get2Errors
operator|.
name|remove
argument_list|(
name|get
argument_list|)
return|;
block|}
block|}
specifier|private
name|void
name|logException
parameter_list|(
name|int
name|tries
parameter_list|,
name|Supplier
argument_list|<
name|Stream
argument_list|<
name|RegionRequest
argument_list|>
argument_list|>
name|regionsSupplier
parameter_list|,
name|Throwable
name|error
parameter_list|,
name|ServerName
name|serverName
parameter_list|)
block|{
if|if
condition|(
name|tries
operator|>
name|startLogErrorsCnt
condition|)
block|{
name|String
name|regions
init|=
name|regionsSupplier
operator|.
name|get
argument_list|()
operator|.
name|map
argument_list|(
name|r
lambda|->
literal|"'"
operator|+
name|r
operator|.
name|loc
operator|.
name|getRegionInfo
argument_list|()
operator|.
name|getRegionNameAsString
argument_list|()
operator|+
literal|"'"
argument_list|)
operator|.
name|collect
argument_list|(
name|Collectors
operator|.
name|joining
argument_list|(
literal|","
argument_list|,
literal|"["
argument_list|,
literal|"]"
argument_list|)
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|warn
argument_list|(
literal|"Get data for "
operator|+
name|regions
operator|+
literal|" in "
operator|+
name|tableName
operator|+
literal|" from "
operator|+
name|serverName
operator|+
literal|" failed, tries="
operator|+
name|tries
argument_list|,
name|error
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|String
name|getExtras
parameter_list|(
name|ServerName
name|serverName
parameter_list|)
block|{
return|return
name|serverName
operator|!=
literal|null
condition|?
name|serverName
operator|.
name|getServerName
argument_list|()
else|:
literal|""
return|;
block|}
specifier|private
name|void
name|addError
parameter_list|(
name|Get
name|get
parameter_list|,
name|Throwable
name|error
parameter_list|,
name|ServerName
name|serverName
parameter_list|)
block|{
name|List
argument_list|<
name|ThrowableWithExtraContext
argument_list|>
name|errors
decl_stmt|;
synchronized|synchronized
init|(
name|get2Errors
init|)
block|{
name|errors
operator|=
name|get2Errors
operator|.
name|computeIfAbsent
argument_list|(
name|get
argument_list|,
name|k
lambda|->
operator|new
name|ArrayList
argument_list|<>
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|errors
operator|.
name|add
argument_list|(
operator|new
name|ThrowableWithExtraContext
argument_list|(
name|error
argument_list|,
name|EnvironmentEdgeManager
operator|.
name|currentTime
argument_list|()
argument_list|,
name|serverName
operator|!=
literal|null
condition|?
name|serverName
operator|.
name|toString
argument_list|()
else|:
literal|""
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|addError
parameter_list|(
name|Iterable
argument_list|<
name|Get
argument_list|>
name|gets
parameter_list|,
name|Throwable
name|error
parameter_list|,
name|ServerName
name|serverName
parameter_list|)
block|{
name|gets
operator|.
name|forEach
argument_list|(
name|get
lambda|->
name|addError
argument_list|(
name|get
argument_list|,
name|error
argument_list|,
name|serverName
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|failOne
parameter_list|(
name|Get
name|get
parameter_list|,
name|int
name|tries
parameter_list|,
name|Throwable
name|error
parameter_list|,
name|long
name|currentTime
parameter_list|,
name|String
name|extras
parameter_list|)
block|{
name|CompletableFuture
argument_list|<
name|Result
argument_list|>
name|future
init|=
name|get2Future
operator|.
name|get
argument_list|(
name|get
argument_list|)
decl_stmt|;
if|if
condition|(
name|future
operator|.
name|isDone
argument_list|()
condition|)
block|{
return|return;
block|}
name|ThrowableWithExtraContext
name|errorWithCtx
init|=
operator|new
name|ThrowableWithExtraContext
argument_list|(
name|error
argument_list|,
name|currentTime
argument_list|,
name|extras
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|ThrowableWithExtraContext
argument_list|>
name|errors
init|=
name|removeErrors
argument_list|(
name|get
argument_list|)
decl_stmt|;
if|if
condition|(
name|errors
operator|==
literal|null
condition|)
block|{
name|errors
operator|=
name|Collections
operator|.
name|singletonList
argument_list|(
name|errorWithCtx
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|errors
operator|.
name|add
argument_list|(
name|errorWithCtx
argument_list|)
expr_stmt|;
block|}
name|future
operator|.
name|completeExceptionally
argument_list|(
operator|new
name|RetriesExhaustedException
argument_list|(
name|tries
argument_list|,
name|errors
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|failAll
parameter_list|(
name|Stream
argument_list|<
name|Get
argument_list|>
name|gets
parameter_list|,
name|int
name|tries
parameter_list|,
name|Throwable
name|error
parameter_list|,
name|ServerName
name|serverName
parameter_list|)
block|{
name|long
name|currentTime
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|String
name|extras
init|=
name|getExtras
argument_list|(
name|serverName
argument_list|)
decl_stmt|;
name|gets
operator|.
name|forEach
argument_list|(
name|get
lambda|->
name|failOne
argument_list|(
name|get
argument_list|,
name|tries
argument_list|,
name|error
argument_list|,
name|currentTime
argument_list|,
name|extras
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|failAll
parameter_list|(
name|Stream
argument_list|<
name|Get
argument_list|>
name|gets
parameter_list|,
name|int
name|tries
parameter_list|)
block|{
name|gets
operator|.
name|forEach
argument_list|(
name|get
lambda|->
block|{
name|CompletableFuture
argument_list|<
name|Result
argument_list|>
name|future
init|=
name|get2Future
operator|.
name|get
argument_list|(
name|get
argument_list|)
decl_stmt|;
if|if
condition|(
name|future
operator|.
name|isDone
argument_list|()
condition|)
block|{
return|return;
block|}
name|future
operator|.
name|completeExceptionally
argument_list|(
operator|new
name|RetriesExhaustedException
argument_list|(
name|tries
argument_list|,
name|Optional
operator|.
name|ofNullable
argument_list|(
name|removeErrors
argument_list|(
name|get
argument_list|)
argument_list|)
operator|.
name|orElse
argument_list|(
name|Collections
operator|.
name|emptyList
argument_list|()
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
argument_list|)
expr_stmt|;
block|}
specifier|private
name|ClientProtos
operator|.
name|MultiRequest
name|buildReq
parameter_list|(
name|Map
argument_list|<
name|byte
index|[]
argument_list|,
name|RegionRequest
argument_list|>
name|getsByRegion
parameter_list|)
throws|throws
name|IOException
block|{
name|ClientProtos
operator|.
name|MultiRequest
operator|.
name|Builder
name|multiRequestBuilder
init|=
name|ClientProtos
operator|.
name|MultiRequest
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|byte
index|[]
argument_list|,
name|RegionRequest
argument_list|>
name|entry
range|:
name|getsByRegion
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|ClientProtos
operator|.
name|RegionAction
operator|.
name|Builder
name|regionActionBuilder
init|=
name|ClientProtos
operator|.
name|RegionAction
operator|.
name|newBuilder
argument_list|()
operator|.
name|setRegion
argument_list|(
name|RequestConverter
operator|.
name|buildRegionSpecifier
argument_list|(
name|RegionSpecifierType
operator|.
name|REGION_NAME
argument_list|,
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|int
name|index
init|=
literal|0
decl_stmt|;
for|for
control|(
name|Get
name|get
range|:
name|entry
operator|.
name|getValue
argument_list|()
operator|.
name|gets
control|)
block|{
name|regionActionBuilder
operator|.
name|addAction
argument_list|(
name|ClientProtos
operator|.
name|Action
operator|.
name|newBuilder
argument_list|()
operator|.
name|setIndex
argument_list|(
name|index
argument_list|)
operator|.
name|setGet
argument_list|(
name|ProtobufUtil
operator|.
name|toGet
argument_list|(
name|get
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|index
operator|++
expr_stmt|;
block|}
name|multiRequestBuilder
operator|.
name|addRegionAction
argument_list|(
name|regionActionBuilder
argument_list|)
expr_stmt|;
block|}
return|return
name|multiRequestBuilder
operator|.
name|build
argument_list|()
return|;
block|}
specifier|private
name|void
name|onComplete
parameter_list|(
name|Map
argument_list|<
name|byte
index|[]
argument_list|,
name|RegionRequest
argument_list|>
name|getsByRegion
parameter_list|,
name|int
name|tries
parameter_list|,
name|ServerName
name|serverName
parameter_list|,
name|MultiResponse
name|resp
parameter_list|)
block|{
name|List
argument_list|<
name|Get
argument_list|>
name|failedGets
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|getsByRegion
operator|.
name|forEach
argument_list|(
parameter_list|(
name|rn
parameter_list|,
name|regionReq
parameter_list|)
lambda|->
block|{
name|RegionResult
name|regionResult
init|=
name|resp
operator|.
name|getResults
argument_list|()
operator|.
name|get
argument_list|(
name|rn
argument_list|)
decl_stmt|;
if|if
condition|(
name|regionResult
operator|!=
literal|null
condition|)
block|{
name|int
name|index
init|=
literal|0
decl_stmt|;
for|for
control|(
name|Get
name|get
range|:
name|regionReq
operator|.
name|gets
control|)
block|{
name|Object
name|result
init|=
name|regionResult
operator|.
name|result
operator|.
name|get
argument_list|(
name|index
argument_list|)
decl_stmt|;
if|if
condition|(
name|result
operator|==
literal|null
condition|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Server sent us neither result nor exception for row '"
operator|+
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|get
operator|.
name|getRow
argument_list|()
argument_list|)
operator|+
literal|"' of "
operator|+
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|rn
argument_list|)
argument_list|)
expr_stmt|;
name|addError
argument_list|(
name|get
argument_list|,
operator|new
name|RuntimeException
argument_list|(
literal|"Invalid response"
argument_list|)
argument_list|,
name|serverName
argument_list|)
expr_stmt|;
name|failedGets
operator|.
name|add
argument_list|(
name|get
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|result
operator|instanceof
name|Throwable
condition|)
block|{
name|Throwable
name|error
init|=
name|translateException
argument_list|(
operator|(
name|Throwable
operator|)
name|result
argument_list|)
decl_stmt|;
name|logException
argument_list|(
name|tries
argument_list|,
parameter_list|()
lambda|->
name|Stream
operator|.
name|of
argument_list|(
name|regionReq
argument_list|)
argument_list|,
name|error
argument_list|,
name|serverName
argument_list|)
expr_stmt|;
if|if
condition|(
name|error
operator|instanceof
name|DoNotRetryIOException
operator|||
name|tries
operator|>=
name|maxAttempts
condition|)
block|{
name|failOne
argument_list|(
name|get
argument_list|,
name|tries
argument_list|,
name|error
argument_list|,
name|EnvironmentEdgeManager
operator|.
name|currentTime
argument_list|()
argument_list|,
name|getExtras
argument_list|(
name|serverName
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|failedGets
operator|.
name|add
argument_list|(
name|get
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|get2Future
operator|.
name|get
argument_list|(
name|get
argument_list|)
operator|.
name|complete
argument_list|(
operator|(
name|Result
operator|)
name|result
argument_list|)
expr_stmt|;
block|}
name|index
operator|++
expr_stmt|;
block|}
block|}
else|else
block|{
name|Throwable
name|t
init|=
name|resp
operator|.
name|getException
argument_list|(
name|rn
argument_list|)
decl_stmt|;
name|Throwable
name|error
decl_stmt|;
if|if
condition|(
name|t
operator|==
literal|null
condition|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Server sent us neither results nor exceptions for "
operator|+
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|rn
argument_list|)
argument_list|)
expr_stmt|;
name|error
operator|=
operator|new
name|RuntimeException
argument_list|(
literal|"Invalid response"
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|error
operator|=
name|translateException
argument_list|(
name|t
argument_list|)
expr_stmt|;
name|logException
argument_list|(
name|tries
argument_list|,
parameter_list|()
lambda|->
name|Stream
operator|.
name|of
argument_list|(
name|regionReq
argument_list|)
argument_list|,
name|error
argument_list|,
name|serverName
argument_list|)
expr_stmt|;
name|conn
operator|.
name|getLocator
argument_list|()
operator|.
name|updateCachedLocation
argument_list|(
name|regionReq
operator|.
name|loc
argument_list|,
name|error
argument_list|)
expr_stmt|;
if|if
condition|(
name|error
operator|instanceof
name|DoNotRetryIOException
operator|||
name|tries
operator|>=
name|maxAttempts
condition|)
block|{
name|failAll
argument_list|(
name|regionReq
operator|.
name|gets
operator|.
name|stream
argument_list|()
argument_list|,
name|tries
argument_list|,
name|error
argument_list|,
name|serverName
argument_list|)
expr_stmt|;
return|return;
block|}
name|addError
argument_list|(
name|regionReq
operator|.
name|gets
argument_list|,
name|error
argument_list|,
name|serverName
argument_list|)
expr_stmt|;
name|failedGets
operator|.
name|addAll
argument_list|(
name|regionReq
operator|.
name|gets
argument_list|)
expr_stmt|;
block|}
block|}
block|}
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|failedGets
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|tryResubmit
argument_list|(
name|failedGets
operator|.
name|stream
argument_list|()
argument_list|,
name|tries
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|send
parameter_list|(
name|Map
argument_list|<
name|ServerName
argument_list|,
name|?
extends|extends
name|Map
argument_list|<
name|byte
index|[]
argument_list|,
name|RegionRequest
argument_list|>
argument_list|>
name|getsByServer
parameter_list|,
name|int
name|tries
parameter_list|)
block|{
name|long
name|callTimeoutNs
decl_stmt|;
if|if
condition|(
name|operationTimeoutNs
operator|>
literal|0
condition|)
block|{
name|long
name|remainingNs
init|=
name|remainingTimeNs
argument_list|()
decl_stmt|;
if|if
condition|(
name|remainingNs
operator|<=
literal|0
condition|)
block|{
name|failAll
argument_list|(
name|getsByServer
operator|.
name|values
argument_list|()
operator|.
name|stream
argument_list|()
operator|.
name|flatMap
argument_list|(
name|m
lambda|->
name|m
operator|.
name|values
argument_list|()
operator|.
name|stream
argument_list|()
argument_list|)
operator|.
name|flatMap
argument_list|(
name|r
lambda|->
name|r
operator|.
name|gets
operator|.
name|stream
argument_list|()
argument_list|)
argument_list|,
name|tries
argument_list|)
expr_stmt|;
return|return;
block|}
name|callTimeoutNs
operator|=
name|Math
operator|.
name|min
argument_list|(
name|remainingNs
argument_list|,
name|rpcTimeoutNs
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|callTimeoutNs
operator|=
name|rpcTimeoutNs
expr_stmt|;
block|}
name|getsByServer
operator|.
name|forEach
argument_list|(
parameter_list|(
name|sn
parameter_list|,
name|getsByRegion
parameter_list|)
lambda|->
block|{
name|ClientService
operator|.
name|Interface
name|stub
decl_stmt|;
try|try
block|{
name|stub
operator|=
name|conn
operator|.
name|getRegionServerStub
argument_list|(
name|sn
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|onError
argument_list|(
name|getsByRegion
argument_list|,
name|tries
argument_list|,
name|e
argument_list|,
name|sn
argument_list|)
expr_stmt|;
return|return;
block|}
name|ClientProtos
operator|.
name|MultiRequest
name|req
decl_stmt|;
try|try
block|{
name|req
operator|=
name|buildReq
argument_list|(
name|getsByRegion
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|onError
argument_list|(
name|getsByRegion
argument_list|,
name|tries
argument_list|,
name|e
argument_list|,
name|sn
argument_list|)
expr_stmt|;
return|return;
block|}
name|HBaseRpcController
name|controller
init|=
name|conn
operator|.
name|rpcControllerFactory
operator|.
name|newController
argument_list|()
decl_stmt|;
name|resetController
argument_list|(
name|controller
argument_list|,
name|callTimeoutNs
argument_list|)
expr_stmt|;
name|stub
operator|.
name|multi
argument_list|(
name|controller
argument_list|,
name|req
argument_list|,
name|resp
lambda|->
block|{
if|if
condition|(
name|controller
operator|.
name|failed
argument_list|()
condition|)
block|{
name|onError
argument_list|(
name|getsByRegion
argument_list|,
name|tries
argument_list|,
name|controller
operator|.
name|getFailed
argument_list|()
argument_list|,
name|sn
argument_list|)
expr_stmt|;
block|}
else|else
block|{
try|try
block|{
name|onComplete
argument_list|(
name|getsByRegion
argument_list|,
name|tries
argument_list|,
name|sn
argument_list|,
name|ResponseConverter
operator|.
name|getResults
argument_list|(
name|req
argument_list|,
name|resp
argument_list|,
name|controller
operator|.
name|cellScanner
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|onError
argument_list|(
name|getsByRegion
argument_list|,
name|tries
argument_list|,
name|e
argument_list|,
name|sn
argument_list|)
expr_stmt|;
return|return;
block|}
block|}
block|}
argument_list|)
expr_stmt|;
block|}
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|onError
parameter_list|(
name|Map
argument_list|<
name|byte
index|[]
argument_list|,
name|RegionRequest
argument_list|>
name|getsByRegion
parameter_list|,
name|int
name|tries
parameter_list|,
name|Throwable
name|t
parameter_list|,
name|ServerName
name|serverName
parameter_list|)
block|{
name|Throwable
name|error
init|=
name|translateException
argument_list|(
name|t
argument_list|)
decl_stmt|;
name|logException
argument_list|(
name|tries
argument_list|,
parameter_list|()
lambda|->
name|getsByRegion
operator|.
name|values
argument_list|()
operator|.
name|stream
argument_list|()
argument_list|,
name|error
argument_list|,
name|serverName
argument_list|)
expr_stmt|;
if|if
condition|(
name|error
operator|instanceof
name|DoNotRetryIOException
operator|||
name|tries
operator|>=
name|maxAttempts
condition|)
block|{
name|failAll
argument_list|(
name|getsByRegion
operator|.
name|values
argument_list|()
operator|.
name|stream
argument_list|()
operator|.
name|flatMap
argument_list|(
name|r
lambda|->
name|r
operator|.
name|gets
operator|.
name|stream
argument_list|()
argument_list|)
argument_list|,
name|tries
argument_list|,
name|error
argument_list|,
name|serverName
argument_list|)
expr_stmt|;
return|return;
block|}
name|List
argument_list|<
name|Get
argument_list|>
name|copiedGets
init|=
name|getsByRegion
operator|.
name|values
argument_list|()
operator|.
name|stream
argument_list|()
operator|.
name|flatMap
argument_list|(
name|r
lambda|->
name|r
operator|.
name|gets
operator|.
name|stream
argument_list|()
argument_list|)
operator|.
name|collect
argument_list|(
name|Collectors
operator|.
name|toList
argument_list|()
argument_list|)
decl_stmt|;
name|addError
argument_list|(
name|copiedGets
argument_list|,
name|error
argument_list|,
name|serverName
argument_list|)
expr_stmt|;
name|tryResubmit
argument_list|(
name|copiedGets
operator|.
name|stream
argument_list|()
argument_list|,
name|tries
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|tryResubmit
parameter_list|(
name|Stream
argument_list|<
name|Get
argument_list|>
name|gets
parameter_list|,
name|int
name|tries
parameter_list|)
block|{
name|long
name|delayNs
decl_stmt|;
if|if
condition|(
name|operationTimeoutNs
operator|>
literal|0
condition|)
block|{
name|long
name|maxDelayNs
init|=
name|remainingTimeNs
argument_list|()
operator|-
name|SLEEP_DELTA_NS
decl_stmt|;
if|if
condition|(
name|maxDelayNs
operator|<=
literal|0
condition|)
block|{
name|failAll
argument_list|(
name|gets
argument_list|,
name|tries
argument_list|)
expr_stmt|;
return|return;
block|}
name|delayNs
operator|=
name|Math
operator|.
name|min
argument_list|(
name|maxDelayNs
argument_list|,
name|getPauseTime
argument_list|(
name|pauseNs
argument_list|,
name|tries
operator|-
literal|1
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|delayNs
operator|=
name|getPauseTime
argument_list|(
name|pauseNs
argument_list|,
name|tries
operator|-
literal|1
argument_list|)
expr_stmt|;
block|}
name|retryTimer
operator|.
name|newTimeout
argument_list|(
name|t
lambda|->
name|groupAndSend
argument_list|(
name|gets
argument_list|,
name|tries
operator|+
literal|1
argument_list|)
argument_list|,
name|delayNs
argument_list|,
name|TimeUnit
operator|.
name|NANOSECONDS
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|groupAndSend
parameter_list|(
name|Stream
argument_list|<
name|Get
argument_list|>
name|gets
parameter_list|,
name|int
name|tries
parameter_list|)
block|{
name|long
name|locateTimeoutNs
decl_stmt|;
if|if
condition|(
name|operationTimeoutNs
operator|>
literal|0
condition|)
block|{
name|locateTimeoutNs
operator|=
name|remainingTimeNs
argument_list|()
expr_stmt|;
if|if
condition|(
name|locateTimeoutNs
operator|<=
literal|0
condition|)
block|{
name|failAll
argument_list|(
name|gets
argument_list|,
name|tries
argument_list|)
expr_stmt|;
return|return;
block|}
block|}
else|else
block|{
name|locateTimeoutNs
operator|=
operator|-
literal|1L
expr_stmt|;
block|}
name|ConcurrentMap
argument_list|<
name|ServerName
argument_list|,
name|ConcurrentMap
argument_list|<
name|byte
index|[]
argument_list|,
name|RegionRequest
argument_list|>
argument_list|>
name|getsByServer
init|=
operator|new
name|ConcurrentHashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|ConcurrentLinkedQueue
argument_list|<
name|Get
argument_list|>
name|locateFailed
init|=
operator|new
name|ConcurrentLinkedQueue
argument_list|<>
argument_list|()
decl_stmt|;
name|CompletableFuture
operator|.
name|allOf
argument_list|(
name|gets
operator|.
name|map
argument_list|(
name|get
lambda|->
name|conn
operator|.
name|getLocator
argument_list|()
operator|.
name|getRegionLocation
argument_list|(
name|tableName
argument_list|,
name|get
operator|.
name|getRow
argument_list|()
argument_list|,
name|RegionLocateType
operator|.
name|CURRENT
argument_list|,
name|locateTimeoutNs
argument_list|)
operator|.
name|whenComplete
argument_list|(
parameter_list|(
name|loc
parameter_list|,
name|error
parameter_list|)
lambda|->
block|{
block|if (error != null
argument_list|)
block|{
name|error
operator|=
name|translateException
argument_list|(
name|error
argument_list|)
block|;
if|if
condition|(
name|error
operator|instanceof
name|DoNotRetryIOException
condition|)
block|{
name|failOne
argument_list|(
name|get
argument_list|,
name|tries
argument_list|,
name|error
argument_list|,
name|EnvironmentEdgeManager
operator|.
name|currentTime
argument_list|()
argument_list|,
literal|""
argument_list|)
expr_stmt|;
return|return;
block|}
name|addError
argument_list|(
name|get
argument_list|,
name|error
argument_list|,
literal|null
argument_list|)
argument_list|;
name|locateFailed
operator|.
name|add
argument_list|(
name|get
argument_list|)
argument_list|;
block|}
else|else
block|{
name|ConcurrentMap
argument_list|<
name|byte
index|[]
argument_list|,
name|RegionRequest
argument_list|>
name|getsByRegion
init|=
name|computeIfAbsent
argument_list|(
name|getsByServer
argument_list|,
name|loc
operator|.
name|getServerName
argument_list|()
argument_list|,
parameter_list|()
lambda|->
operator|new
name|ConcurrentSkipListMap
argument_list|<>
argument_list|(
name|Bytes
operator|.
name|BYTES_COMPARATOR
argument_list|)
argument_list|)
decl_stmt|;
name|computeIfAbsent
argument_list|(
name|getsByRegion
argument_list|,
name|loc
operator|.
name|getRegionInfo
argument_list|()
operator|.
name|getRegionName
argument_list|()
argument_list|,
parameter_list|()
lambda|->
operator|new
name|RegionRequest
argument_list|(
name|loc
argument_list|)
argument_list|)
operator|.
name|gets
operator|.
name|add
argument_list|(
name|get
argument_list|)
expr_stmt|;
block|}
block|}
end_class

begin_expr_stmt
unit|))
operator|.
name|toArray
argument_list|(
name|CompletableFuture
index|[]
operator|::
operator|new
argument_list|)
end_expr_stmt

begin_expr_stmt
unit|)
operator|.
name|whenComplete
argument_list|(
parameter_list|(
name|v
parameter_list|,
name|r
parameter_list|)
lambda|->
block|{
if|if
condition|(
operator|!
name|getsByServer
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|send
argument_list|(
name|getsByServer
argument_list|,
name|tries
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|locateFailed
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|tryResubmit
argument_list|(
name|locateFailed
operator|.
name|stream
argument_list|()
argument_list|,
name|tries
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
end_expr_stmt

begin_function
unit|}    public
name|List
argument_list|<
name|CompletableFuture
argument_list|<
name|Result
argument_list|>
argument_list|>
name|call
parameter_list|()
block|{
name|groupAndSend
argument_list|(
name|gets
operator|.
name|stream
argument_list|()
argument_list|,
literal|1
argument_list|)
expr_stmt|;
return|return
name|futures
return|;
block|}
end_function

unit|}
end_unit

