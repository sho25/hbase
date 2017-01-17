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
name|translateException
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
name|Consumer
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
comment|/**  * Retry caller for a single request, such as get, put, delete, etc.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
class|class
name|AsyncSingleRequestRpcRetryingCaller
parameter_list|<
name|T
parameter_list|>
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
name|AsyncSingleRequestRpcRetryingCaller
operator|.
name|class
argument_list|)
decl_stmt|;
annotation|@
name|FunctionalInterface
specifier|public
interface|interface
name|Callable
parameter_list|<
name|T
parameter_list|>
block|{
name|CompletableFuture
argument_list|<
name|T
argument_list|>
name|call
parameter_list|(
name|HBaseRpcController
name|controller
parameter_list|,
name|HRegionLocation
name|loc
parameter_list|,
name|ClientService
operator|.
name|Interface
name|stub
parameter_list|)
function_decl|;
block|}
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
name|byte
index|[]
name|row
decl_stmt|;
specifier|private
specifier|final
name|RegionLocateType
name|locateType
decl_stmt|;
specifier|private
specifier|final
name|Callable
argument_list|<
name|T
argument_list|>
name|callable
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
name|CompletableFuture
argument_list|<
name|T
argument_list|>
name|future
decl_stmt|;
specifier|private
specifier|final
name|HBaseRpcController
name|controller
decl_stmt|;
specifier|private
specifier|final
name|List
argument_list|<
name|RetriesExhaustedException
operator|.
name|ThrowableWithExtraContext
argument_list|>
name|exceptions
decl_stmt|;
specifier|private
specifier|final
name|long
name|startNs
decl_stmt|;
specifier|public
name|AsyncSingleRequestRpcRetryingCaller
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
name|byte
index|[]
name|row
parameter_list|,
name|RegionLocateType
name|locateType
parameter_list|,
name|Callable
argument_list|<
name|T
argument_list|>
name|callable
parameter_list|,
name|long
name|pauseNs
parameter_list|,
name|int
name|maxAttempts
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
name|row
operator|=
name|row
expr_stmt|;
name|this
operator|.
name|locateType
operator|=
name|locateType
expr_stmt|;
name|this
operator|.
name|callable
operator|=
name|callable
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
name|maxAttempts
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
name|future
operator|=
operator|new
name|CompletableFuture
argument_list|<>
argument_list|()
expr_stmt|;
name|this
operator|.
name|controller
operator|=
name|conn
operator|.
name|rpcControllerFactory
operator|.
name|newController
argument_list|()
expr_stmt|;
name|this
operator|.
name|exceptions
operator|=
operator|new
name|ArrayList
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
name|int
name|tries
init|=
literal|1
decl_stmt|;
specifier|private
name|long
name|elapsedMs
parameter_list|()
block|{
return|return
name|TimeUnit
operator|.
name|NANOSECONDS
operator|.
name|toMillis
argument_list|(
name|System
operator|.
name|nanoTime
argument_list|()
operator|-
name|startNs
argument_list|)
return|;
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
name|void
name|completeExceptionally
parameter_list|()
block|{
name|future
operator|.
name|completeExceptionally
argument_list|(
operator|new
name|RetriesExhaustedException
argument_list|(
name|tries
operator|-
literal|1
argument_list|,
name|exceptions
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|onError
parameter_list|(
name|Throwable
name|error
parameter_list|,
name|Supplier
argument_list|<
name|String
argument_list|>
name|errMsg
parameter_list|,
name|Consumer
argument_list|<
name|Throwable
argument_list|>
name|updateCachedLocation
parameter_list|)
block|{
name|error
operator|=
name|translateException
argument_list|(
name|error
argument_list|)
expr_stmt|;
if|if
condition|(
name|tries
operator|>
name|startLogErrorsCnt
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
name|errMsg
operator|.
name|get
argument_list|()
argument_list|,
name|error
argument_list|)
expr_stmt|;
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
name|error
argument_list|,
name|EnvironmentEdgeManager
operator|.
name|currentTime
argument_list|()
argument_list|,
literal|""
argument_list|)
decl_stmt|;
name|exceptions
operator|.
name|add
argument_list|(
name|qt
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
name|completeExceptionally
argument_list|()
expr_stmt|;
return|return;
block|}
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
name|completeExceptionally
argument_list|()
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
name|updateCachedLocation
operator|.
name|accept
argument_list|(
name|error
argument_list|)
expr_stmt|;
name|tries
operator|++
expr_stmt|;
name|retryTimer
operator|.
name|newTimeout
argument_list|(
name|t
lambda|->
name|locateThenCall
argument_list|()
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
name|call
parameter_list|(
name|HRegionLocation
name|loc
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
name|callTimeoutNs
operator|=
name|remainingTimeNs
argument_list|()
expr_stmt|;
if|if
condition|(
name|callTimeoutNs
operator|<=
literal|0
condition|)
block|{
name|completeExceptionally
argument_list|()
expr_stmt|;
return|return;
block|}
name|callTimeoutNs
operator|=
name|Math
operator|.
name|min
argument_list|(
name|callTimeoutNs
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
name|loc
operator|.
name|getServerName
argument_list|()
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
name|e
argument_list|,
parameter_list|()
lambda|->
literal|"Get async stub to "
operator|+
name|loc
operator|.
name|getServerName
argument_list|()
operator|+
literal|" for '"
operator|+
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|row
argument_list|)
operator|+
literal|"' in "
operator|+
name|loc
operator|.
name|getRegionInfo
argument_list|()
operator|.
name|getEncodedName
argument_list|()
operator|+
literal|" of "
operator|+
name|tableName
operator|+
literal|" failed, tries = "
operator|+
name|tries
operator|+
literal|", maxAttempts = "
operator|+
name|maxAttempts
operator|+
literal|", timeout = "
operator|+
name|TimeUnit
operator|.
name|NANOSECONDS
operator|.
name|toMillis
argument_list|(
name|operationTimeoutNs
argument_list|)
operator|+
literal|" ms, time elapsed = "
operator|+
name|elapsedMs
argument_list|()
operator|+
literal|" ms"
argument_list|,
name|err
lambda|->
name|conn
operator|.
name|getLocator
argument_list|()
operator|.
name|updateCachedLocation
argument_list|(
name|loc
argument_list|,
name|err
argument_list|)
argument_list|)
expr_stmt|;
return|return;
block|}
name|resetController
argument_list|(
name|controller
argument_list|,
name|callTimeoutNs
argument_list|)
expr_stmt|;
name|callable
operator|.
name|call
argument_list|(
name|controller
argument_list|,
name|loc
argument_list|,
name|stub
argument_list|)
operator|.
name|whenComplete
argument_list|(
parameter_list|(
name|result
parameter_list|,
name|error
parameter_list|)
lambda|->
block|{
if|if
condition|(
name|error
operator|!=
literal|null
condition|)
block|{
name|onError
argument_list|(
name|error
argument_list|,
parameter_list|()
lambda|->
literal|"Call to "
operator|+
name|loc
operator|.
name|getServerName
argument_list|()
operator|+
literal|" for '"
operator|+
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|row
argument_list|)
operator|+
literal|"' in "
operator|+
name|loc
operator|.
name|getRegionInfo
argument_list|()
operator|.
name|getEncodedName
argument_list|()
operator|+
literal|" of "
operator|+
name|tableName
operator|+
literal|" failed, tries = "
operator|+
name|tries
operator|+
literal|", maxAttempts = "
operator|+
name|maxAttempts
operator|+
literal|", timeout = "
operator|+
name|TimeUnit
operator|.
name|NANOSECONDS
operator|.
name|toMillis
argument_list|(
name|operationTimeoutNs
argument_list|)
operator|+
literal|" ms, time elapsed = "
operator|+
name|elapsedMs
argument_list|()
operator|+
literal|" ms"
argument_list|,
name|err
lambda|->
name|conn
operator|.
name|getLocator
argument_list|()
operator|.
name|updateCachedLocation
argument_list|(
name|loc
argument_list|,
name|err
argument_list|)
argument_list|)
expr_stmt|;
return|return;
block|}
name|future
operator|.
name|complete
argument_list|(
name|result
argument_list|)
expr_stmt|;
block|}
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|locateThenCall
parameter_list|()
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
name|completeExceptionally
argument_list|()
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
name|conn
operator|.
name|getLocator
argument_list|()
operator|.
name|getRegionLocation
argument_list|(
name|tableName
argument_list|,
name|row
argument_list|,
name|locateType
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
if|if
condition|(
name|error
operator|!=
literal|null
condition|)
block|{
name|onError
argument_list|(
name|error
argument_list|,
parameter_list|()
lambda|->
literal|"Locate '"
operator|+
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|row
argument_list|)
operator|+
literal|"' in "
operator|+
name|tableName
operator|+
literal|" failed, tries = "
operator|+
name|tries
operator|+
literal|", maxAttempts = "
operator|+
name|maxAttempts
operator|+
literal|", timeout = "
operator|+
name|TimeUnit
operator|.
name|NANOSECONDS
operator|.
name|toMillis
argument_list|(
name|operationTimeoutNs
argument_list|)
operator|+
literal|" ms, time elapsed = "
operator|+
name|elapsedMs
argument_list|()
operator|+
literal|" ms"
argument_list|,
name|err
lambda|->
block|{               }
argument_list|)
expr_stmt|;
return|return;
block|}
name|call
argument_list|(
name|loc
argument_list|)
expr_stmt|;
block|}
argument_list|)
expr_stmt|;
block|}
specifier|public
name|CompletableFuture
argument_list|<
name|T
argument_list|>
name|call
parameter_list|()
block|{
name|locateThenCall
argument_list|()
expr_stmt|;
return|return
name|future
return|;
block|}
block|}
end_class

end_unit

