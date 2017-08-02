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
name|shaded
operator|.
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|base
operator|.
name|Preconditions
operator|.
name|checkArgument
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
name|shaded
operator|.
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|base
operator|.
name|Preconditions
operator|.
name|checkNotNull
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
name|metrics
operator|.
name|ScanMetrics
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
name|shaded
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|ClientProtos
operator|.
name|ScanResponse
import|;
end_import

begin_comment
comment|/**  * Factory to create an AsyncRpcRetryCaller.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
class|class
name|AsyncRpcRetryingCallerFactory
block|{
specifier|private
specifier|final
name|AsyncConnectionImpl
name|conn
decl_stmt|;
specifier|private
specifier|final
name|HashedWheelTimer
name|retryTimer
decl_stmt|;
specifier|public
name|AsyncRpcRetryingCallerFactory
parameter_list|(
name|AsyncConnectionImpl
name|conn
parameter_list|,
name|HashedWheelTimer
name|retryTimer
parameter_list|)
block|{
name|this
operator|.
name|conn
operator|=
name|conn
expr_stmt|;
name|this
operator|.
name|retryTimer
operator|=
name|retryTimer
expr_stmt|;
block|}
specifier|private
specifier|abstract
class|class
name|BuilderBase
block|{
specifier|protected
name|long
name|pauseNs
init|=
name|conn
operator|.
name|connConf
operator|.
name|getPauseNs
argument_list|()
decl_stmt|;
specifier|protected
name|int
name|maxAttempts
init|=
name|retries2Attempts
argument_list|(
name|conn
operator|.
name|connConf
operator|.
name|getMaxRetries
argument_list|()
argument_list|)
decl_stmt|;
specifier|protected
name|int
name|startLogErrorsCnt
init|=
name|conn
operator|.
name|connConf
operator|.
name|getStartLogErrorsCnt
argument_list|()
decl_stmt|;
block|}
specifier|public
class|class
name|SingleRequestCallerBuilder
parameter_list|<
name|T
parameter_list|>
extends|extends
name|BuilderBase
block|{
specifier|private
name|TableName
name|tableName
decl_stmt|;
specifier|private
name|byte
index|[]
name|row
decl_stmt|;
specifier|private
name|AsyncSingleRequestRpcRetryingCaller
operator|.
name|Callable
argument_list|<
name|T
argument_list|>
name|callable
decl_stmt|;
specifier|private
name|long
name|operationTimeoutNs
init|=
operator|-
literal|1L
decl_stmt|;
specifier|private
name|long
name|rpcTimeoutNs
init|=
operator|-
literal|1L
decl_stmt|;
specifier|private
name|RegionLocateType
name|locateType
init|=
name|RegionLocateType
operator|.
name|CURRENT
decl_stmt|;
specifier|public
name|SingleRequestCallerBuilder
argument_list|<
name|T
argument_list|>
name|table
parameter_list|(
name|TableName
name|tableName
parameter_list|)
block|{
name|this
operator|.
name|tableName
operator|=
name|tableName
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|SingleRequestCallerBuilder
argument_list|<
name|T
argument_list|>
name|row
parameter_list|(
name|byte
index|[]
name|row
parameter_list|)
block|{
name|this
operator|.
name|row
operator|=
name|row
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|SingleRequestCallerBuilder
argument_list|<
name|T
argument_list|>
name|action
parameter_list|(
name|AsyncSingleRequestRpcRetryingCaller
operator|.
name|Callable
argument_list|<
name|T
argument_list|>
name|callable
parameter_list|)
block|{
name|this
operator|.
name|callable
operator|=
name|callable
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|SingleRequestCallerBuilder
argument_list|<
name|T
argument_list|>
name|operationTimeout
parameter_list|(
name|long
name|operationTimeout
parameter_list|,
name|TimeUnit
name|unit
parameter_list|)
block|{
name|this
operator|.
name|operationTimeoutNs
operator|=
name|unit
operator|.
name|toNanos
argument_list|(
name|operationTimeout
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|SingleRequestCallerBuilder
argument_list|<
name|T
argument_list|>
name|rpcTimeout
parameter_list|(
name|long
name|rpcTimeout
parameter_list|,
name|TimeUnit
name|unit
parameter_list|)
block|{
name|this
operator|.
name|rpcTimeoutNs
operator|=
name|unit
operator|.
name|toNanos
argument_list|(
name|rpcTimeout
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|SingleRequestCallerBuilder
argument_list|<
name|T
argument_list|>
name|locateType
parameter_list|(
name|RegionLocateType
name|locateType
parameter_list|)
block|{
name|this
operator|.
name|locateType
operator|=
name|locateType
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|SingleRequestCallerBuilder
argument_list|<
name|T
argument_list|>
name|pause
parameter_list|(
name|long
name|pause
parameter_list|,
name|TimeUnit
name|unit
parameter_list|)
block|{
name|this
operator|.
name|pauseNs
operator|=
name|unit
operator|.
name|toNanos
argument_list|(
name|pause
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|SingleRequestCallerBuilder
argument_list|<
name|T
argument_list|>
name|maxAttempts
parameter_list|(
name|int
name|maxAttempts
parameter_list|)
block|{
name|this
operator|.
name|maxAttempts
operator|=
name|maxAttempts
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|SingleRequestCallerBuilder
argument_list|<
name|T
argument_list|>
name|startLogErrorsCnt
parameter_list|(
name|int
name|startLogErrorsCnt
parameter_list|)
block|{
name|this
operator|.
name|startLogErrorsCnt
operator|=
name|startLogErrorsCnt
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|AsyncSingleRequestRpcRetryingCaller
argument_list|<
name|T
argument_list|>
name|build
parameter_list|()
block|{
return|return
operator|new
name|AsyncSingleRequestRpcRetryingCaller
argument_list|<>
argument_list|(
name|retryTimer
argument_list|,
name|conn
argument_list|,
name|checkNotNull
argument_list|(
name|tableName
argument_list|,
literal|"tableName is null"
argument_list|)
argument_list|,
name|checkNotNull
argument_list|(
name|row
argument_list|,
literal|"row is null"
argument_list|)
argument_list|,
name|checkNotNull
argument_list|(
name|locateType
argument_list|,
literal|"locateType is null"
argument_list|)
argument_list|,
name|checkNotNull
argument_list|(
name|callable
argument_list|,
literal|"action is null"
argument_list|)
argument_list|,
name|pauseNs
argument_list|,
name|maxAttempts
argument_list|,
name|operationTimeoutNs
argument_list|,
name|rpcTimeoutNs
argument_list|,
name|startLogErrorsCnt
argument_list|)
return|;
block|}
comment|/**      * Shortcut for {@code build().call()}      */
specifier|public
name|CompletableFuture
argument_list|<
name|T
argument_list|>
name|call
parameter_list|()
block|{
return|return
name|build
argument_list|()
operator|.
name|call
argument_list|()
return|;
block|}
block|}
comment|/**    * Create retry caller for single action, such as get, put, delete, etc.    */
specifier|public
parameter_list|<
name|T
parameter_list|>
name|SingleRequestCallerBuilder
argument_list|<
name|T
argument_list|>
name|single
parameter_list|()
block|{
return|return
operator|new
name|SingleRequestCallerBuilder
argument_list|<>
argument_list|()
return|;
block|}
specifier|public
class|class
name|ScanSingleRegionCallerBuilder
extends|extends
name|BuilderBase
block|{
specifier|private
name|Long
name|scannerId
init|=
literal|null
decl_stmt|;
specifier|private
name|Scan
name|scan
decl_stmt|;
specifier|private
name|ScanMetrics
name|scanMetrics
decl_stmt|;
specifier|private
name|ScanResultCache
name|resultCache
decl_stmt|;
specifier|private
name|RawScanResultConsumer
name|consumer
decl_stmt|;
specifier|private
name|ClientService
operator|.
name|Interface
name|stub
decl_stmt|;
specifier|private
name|HRegionLocation
name|loc
decl_stmt|;
specifier|private
name|boolean
name|isRegionServerRemote
decl_stmt|;
specifier|private
name|long
name|scannerLeaseTimeoutPeriodNs
decl_stmt|;
specifier|private
name|long
name|scanTimeoutNs
decl_stmt|;
specifier|private
name|long
name|rpcTimeoutNs
decl_stmt|;
specifier|public
name|ScanSingleRegionCallerBuilder
name|id
parameter_list|(
name|long
name|scannerId
parameter_list|)
block|{
name|this
operator|.
name|scannerId
operator|=
name|scannerId
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|ScanSingleRegionCallerBuilder
name|setScan
parameter_list|(
name|Scan
name|scan
parameter_list|)
block|{
name|this
operator|.
name|scan
operator|=
name|scan
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|ScanSingleRegionCallerBuilder
name|metrics
parameter_list|(
name|ScanMetrics
name|scanMetrics
parameter_list|)
block|{
name|this
operator|.
name|scanMetrics
operator|=
name|scanMetrics
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|ScanSingleRegionCallerBuilder
name|remote
parameter_list|(
name|boolean
name|isRegionServerRemote
parameter_list|)
block|{
name|this
operator|.
name|isRegionServerRemote
operator|=
name|isRegionServerRemote
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|ScanSingleRegionCallerBuilder
name|resultCache
parameter_list|(
name|ScanResultCache
name|resultCache
parameter_list|)
block|{
name|this
operator|.
name|resultCache
operator|=
name|resultCache
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|ScanSingleRegionCallerBuilder
name|consumer
parameter_list|(
name|RawScanResultConsumer
name|consumer
parameter_list|)
block|{
name|this
operator|.
name|consumer
operator|=
name|consumer
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|ScanSingleRegionCallerBuilder
name|stub
parameter_list|(
name|ClientService
operator|.
name|Interface
name|stub
parameter_list|)
block|{
name|this
operator|.
name|stub
operator|=
name|stub
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|ScanSingleRegionCallerBuilder
name|location
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
return|return
name|this
return|;
block|}
specifier|public
name|ScanSingleRegionCallerBuilder
name|scannerLeaseTimeoutPeriod
parameter_list|(
name|long
name|scannerLeaseTimeoutPeriod
parameter_list|,
name|TimeUnit
name|unit
parameter_list|)
block|{
name|this
operator|.
name|scannerLeaseTimeoutPeriodNs
operator|=
name|unit
operator|.
name|toNanos
argument_list|(
name|scannerLeaseTimeoutPeriod
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|ScanSingleRegionCallerBuilder
name|scanTimeout
parameter_list|(
name|long
name|scanTimeout
parameter_list|,
name|TimeUnit
name|unit
parameter_list|)
block|{
name|this
operator|.
name|scanTimeoutNs
operator|=
name|unit
operator|.
name|toNanos
argument_list|(
name|scanTimeout
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|ScanSingleRegionCallerBuilder
name|rpcTimeout
parameter_list|(
name|long
name|rpcTimeout
parameter_list|,
name|TimeUnit
name|unit
parameter_list|)
block|{
name|this
operator|.
name|rpcTimeoutNs
operator|=
name|unit
operator|.
name|toNanos
argument_list|(
name|rpcTimeout
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|ScanSingleRegionCallerBuilder
name|pause
parameter_list|(
name|long
name|pause
parameter_list|,
name|TimeUnit
name|unit
parameter_list|)
block|{
name|this
operator|.
name|pauseNs
operator|=
name|unit
operator|.
name|toNanos
argument_list|(
name|pause
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|ScanSingleRegionCallerBuilder
name|maxAttempts
parameter_list|(
name|int
name|maxAttempts
parameter_list|)
block|{
name|this
operator|.
name|maxAttempts
operator|=
name|maxAttempts
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|ScanSingleRegionCallerBuilder
name|startLogErrorsCnt
parameter_list|(
name|int
name|startLogErrorsCnt
parameter_list|)
block|{
name|this
operator|.
name|startLogErrorsCnt
operator|=
name|startLogErrorsCnt
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|AsyncScanSingleRegionRpcRetryingCaller
name|build
parameter_list|()
block|{
name|checkArgument
argument_list|(
name|scannerId
operator|!=
literal|null
argument_list|,
literal|"invalid scannerId %d"
argument_list|,
name|scannerId
argument_list|)
expr_stmt|;
return|return
operator|new
name|AsyncScanSingleRegionRpcRetryingCaller
argument_list|(
name|retryTimer
argument_list|,
name|conn
argument_list|,
name|checkNotNull
argument_list|(
name|scan
argument_list|,
literal|"scan is null"
argument_list|)
argument_list|,
name|scanMetrics
argument_list|,
name|scannerId
argument_list|,
name|checkNotNull
argument_list|(
name|resultCache
argument_list|,
literal|"resultCache is null"
argument_list|)
argument_list|,
name|checkNotNull
argument_list|(
name|consumer
argument_list|,
literal|"consumer is null"
argument_list|)
argument_list|,
name|checkNotNull
argument_list|(
name|stub
argument_list|,
literal|"stub is null"
argument_list|)
argument_list|,
name|checkNotNull
argument_list|(
name|loc
argument_list|,
literal|"location is null"
argument_list|)
argument_list|,
name|isRegionServerRemote
argument_list|,
name|scannerLeaseTimeoutPeriodNs
argument_list|,
name|pauseNs
argument_list|,
name|maxAttempts
argument_list|,
name|scanTimeoutNs
argument_list|,
name|rpcTimeoutNs
argument_list|,
name|startLogErrorsCnt
argument_list|)
return|;
block|}
comment|/**      * Short cut for {@code build().start(HBaseRpcController, ScanResponse)}.      */
specifier|public
name|CompletableFuture
argument_list|<
name|Boolean
argument_list|>
name|start
parameter_list|(
name|HBaseRpcController
name|controller
parameter_list|,
name|ScanResponse
name|respWhenOpen
parameter_list|)
block|{
return|return
name|build
argument_list|()
operator|.
name|start
argument_list|(
name|controller
argument_list|,
name|respWhenOpen
argument_list|)
return|;
block|}
block|}
comment|/**    * Create retry caller for scanning a region.    */
specifier|public
name|ScanSingleRegionCallerBuilder
name|scanSingleRegion
parameter_list|()
block|{
return|return
operator|new
name|ScanSingleRegionCallerBuilder
argument_list|()
return|;
block|}
specifier|public
class|class
name|BatchCallerBuilder
extends|extends
name|BuilderBase
block|{
specifier|private
name|TableName
name|tableName
decl_stmt|;
specifier|private
name|List
argument_list|<
name|?
extends|extends
name|Row
argument_list|>
name|actions
decl_stmt|;
specifier|private
name|long
name|operationTimeoutNs
init|=
operator|-
literal|1L
decl_stmt|;
specifier|private
name|long
name|rpcTimeoutNs
init|=
operator|-
literal|1L
decl_stmt|;
specifier|public
name|BatchCallerBuilder
name|table
parameter_list|(
name|TableName
name|tableName
parameter_list|)
block|{
name|this
operator|.
name|tableName
operator|=
name|tableName
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|BatchCallerBuilder
name|actions
parameter_list|(
name|List
argument_list|<
name|?
extends|extends
name|Row
argument_list|>
name|actions
parameter_list|)
block|{
name|this
operator|.
name|actions
operator|=
name|actions
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|BatchCallerBuilder
name|operationTimeout
parameter_list|(
name|long
name|operationTimeout
parameter_list|,
name|TimeUnit
name|unit
parameter_list|)
block|{
name|this
operator|.
name|operationTimeoutNs
operator|=
name|unit
operator|.
name|toNanos
argument_list|(
name|operationTimeout
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|BatchCallerBuilder
name|rpcTimeout
parameter_list|(
name|long
name|rpcTimeout
parameter_list|,
name|TimeUnit
name|unit
parameter_list|)
block|{
name|this
operator|.
name|rpcTimeoutNs
operator|=
name|unit
operator|.
name|toNanos
argument_list|(
name|rpcTimeout
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|BatchCallerBuilder
name|pause
parameter_list|(
name|long
name|pause
parameter_list|,
name|TimeUnit
name|unit
parameter_list|)
block|{
name|this
operator|.
name|pauseNs
operator|=
name|unit
operator|.
name|toNanos
argument_list|(
name|pause
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|BatchCallerBuilder
name|maxAttempts
parameter_list|(
name|int
name|maxAttempts
parameter_list|)
block|{
name|this
operator|.
name|maxAttempts
operator|=
name|maxAttempts
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|BatchCallerBuilder
name|startLogErrorsCnt
parameter_list|(
name|int
name|startLogErrorsCnt
parameter_list|)
block|{
name|this
operator|.
name|startLogErrorsCnt
operator|=
name|startLogErrorsCnt
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
parameter_list|<
name|T
parameter_list|>
name|AsyncBatchRpcRetryingCaller
argument_list|<
name|T
argument_list|>
name|build
parameter_list|()
block|{
return|return
operator|new
name|AsyncBatchRpcRetryingCaller
argument_list|<>
argument_list|(
name|retryTimer
argument_list|,
name|conn
argument_list|,
name|tableName
argument_list|,
name|actions
argument_list|,
name|pauseNs
argument_list|,
name|maxAttempts
argument_list|,
name|operationTimeoutNs
argument_list|,
name|rpcTimeoutNs
argument_list|,
name|startLogErrorsCnt
argument_list|)
return|;
block|}
specifier|public
parameter_list|<
name|T
parameter_list|>
name|List
argument_list|<
name|CompletableFuture
argument_list|<
name|T
argument_list|>
argument_list|>
name|call
parameter_list|()
block|{
return|return
name|this
operator|.
expr|<
name|T
operator|>
name|build
argument_list|()
operator|.
name|call
argument_list|()
return|;
block|}
block|}
specifier|public
name|BatchCallerBuilder
name|batch
parameter_list|()
block|{
return|return
operator|new
name|BatchCallerBuilder
argument_list|()
return|;
block|}
specifier|public
class|class
name|MasterRequestCallerBuilder
parameter_list|<
name|T
parameter_list|>
extends|extends
name|BuilderBase
block|{
specifier|private
name|AsyncMasterRequestRpcRetryingCaller
operator|.
name|Callable
argument_list|<
name|T
argument_list|>
name|callable
decl_stmt|;
specifier|private
name|long
name|operationTimeoutNs
init|=
operator|-
literal|1L
decl_stmt|;
specifier|private
name|long
name|rpcTimeoutNs
init|=
operator|-
literal|1L
decl_stmt|;
specifier|public
name|MasterRequestCallerBuilder
argument_list|<
name|T
argument_list|>
name|action
parameter_list|(
name|AsyncMasterRequestRpcRetryingCaller
operator|.
name|Callable
argument_list|<
name|T
argument_list|>
name|callable
parameter_list|)
block|{
name|this
operator|.
name|callable
operator|=
name|callable
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|MasterRequestCallerBuilder
argument_list|<
name|T
argument_list|>
name|operationTimeout
parameter_list|(
name|long
name|operationTimeout
parameter_list|,
name|TimeUnit
name|unit
parameter_list|)
block|{
name|this
operator|.
name|operationTimeoutNs
operator|=
name|unit
operator|.
name|toNanos
argument_list|(
name|operationTimeout
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|MasterRequestCallerBuilder
argument_list|<
name|T
argument_list|>
name|rpcTimeout
parameter_list|(
name|long
name|rpcTimeout
parameter_list|,
name|TimeUnit
name|unit
parameter_list|)
block|{
name|this
operator|.
name|rpcTimeoutNs
operator|=
name|unit
operator|.
name|toNanos
argument_list|(
name|rpcTimeout
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|MasterRequestCallerBuilder
argument_list|<
name|T
argument_list|>
name|pause
parameter_list|(
name|long
name|pause
parameter_list|,
name|TimeUnit
name|unit
parameter_list|)
block|{
name|this
operator|.
name|pauseNs
operator|=
name|unit
operator|.
name|toNanos
argument_list|(
name|pause
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|MasterRequestCallerBuilder
argument_list|<
name|T
argument_list|>
name|maxAttempts
parameter_list|(
name|int
name|maxAttempts
parameter_list|)
block|{
name|this
operator|.
name|maxAttempts
operator|=
name|maxAttempts
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|MasterRequestCallerBuilder
argument_list|<
name|T
argument_list|>
name|startLogErrorsCnt
parameter_list|(
name|int
name|startLogErrorsCnt
parameter_list|)
block|{
name|this
operator|.
name|startLogErrorsCnt
operator|=
name|startLogErrorsCnt
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|AsyncMasterRequestRpcRetryingCaller
argument_list|<
name|T
argument_list|>
name|build
parameter_list|()
block|{
return|return
operator|new
name|AsyncMasterRequestRpcRetryingCaller
argument_list|<
name|T
argument_list|>
argument_list|(
name|retryTimer
argument_list|,
name|conn
argument_list|,
name|checkNotNull
argument_list|(
name|callable
argument_list|,
literal|"action is null"
argument_list|)
argument_list|,
name|pauseNs
argument_list|,
name|maxAttempts
argument_list|,
name|operationTimeoutNs
argument_list|,
name|rpcTimeoutNs
argument_list|,
name|startLogErrorsCnt
argument_list|)
return|;
block|}
comment|/**      * Shortcut for {@code build().call()}      */
specifier|public
name|CompletableFuture
argument_list|<
name|T
argument_list|>
name|call
parameter_list|()
block|{
return|return
name|build
argument_list|()
operator|.
name|call
argument_list|()
return|;
block|}
block|}
specifier|public
parameter_list|<
name|T
parameter_list|>
name|MasterRequestCallerBuilder
argument_list|<
name|T
argument_list|>
name|masterRequest
parameter_list|()
block|{
return|return
operator|new
name|MasterRequestCallerBuilder
argument_list|<>
argument_list|()
return|;
block|}
specifier|public
class|class
name|AdminRequestCallerBuilder
parameter_list|<
name|T
parameter_list|>
extends|extends
name|BuilderBase
block|{
comment|// TODO: maybe we can reuse AdminRequestCallerBuild, MasterRequestCallerBuild etc.
specifier|private
name|AsyncAdminRequestRetryingCaller
operator|.
name|Callable
argument_list|<
name|T
argument_list|>
name|callable
decl_stmt|;
specifier|private
name|long
name|operationTimeoutNs
init|=
operator|-
literal|1L
decl_stmt|;
specifier|private
name|long
name|rpcTimeoutNs
init|=
operator|-
literal|1L
decl_stmt|;
specifier|private
name|ServerName
name|serverName
decl_stmt|;
specifier|public
name|AdminRequestCallerBuilder
argument_list|<
name|T
argument_list|>
name|action
parameter_list|(
name|AsyncAdminRequestRetryingCaller
operator|.
name|Callable
argument_list|<
name|T
argument_list|>
name|callable
parameter_list|)
block|{
name|this
operator|.
name|callable
operator|=
name|callable
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|AdminRequestCallerBuilder
argument_list|<
name|T
argument_list|>
name|operationTimeout
parameter_list|(
name|long
name|operationTimeout
parameter_list|,
name|TimeUnit
name|unit
parameter_list|)
block|{
name|this
operator|.
name|operationTimeoutNs
operator|=
name|unit
operator|.
name|toNanos
argument_list|(
name|operationTimeout
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|AdminRequestCallerBuilder
argument_list|<
name|T
argument_list|>
name|rpcTimeout
parameter_list|(
name|long
name|rpcTimeout
parameter_list|,
name|TimeUnit
name|unit
parameter_list|)
block|{
name|this
operator|.
name|rpcTimeoutNs
operator|=
name|unit
operator|.
name|toNanos
argument_list|(
name|rpcTimeout
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|AdminRequestCallerBuilder
argument_list|<
name|T
argument_list|>
name|pause
parameter_list|(
name|long
name|pause
parameter_list|,
name|TimeUnit
name|unit
parameter_list|)
block|{
name|this
operator|.
name|pauseNs
operator|=
name|unit
operator|.
name|toNanos
argument_list|(
name|pause
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|AdminRequestCallerBuilder
argument_list|<
name|T
argument_list|>
name|maxAttempts
parameter_list|(
name|int
name|maxAttempts
parameter_list|)
block|{
name|this
operator|.
name|maxAttempts
operator|=
name|maxAttempts
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|AdminRequestCallerBuilder
argument_list|<
name|T
argument_list|>
name|startLogErrorsCnt
parameter_list|(
name|int
name|startLogErrorsCnt
parameter_list|)
block|{
name|this
operator|.
name|startLogErrorsCnt
operator|=
name|startLogErrorsCnt
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|AdminRequestCallerBuilder
argument_list|<
name|T
argument_list|>
name|serverName
parameter_list|(
name|ServerName
name|serverName
parameter_list|)
block|{
name|this
operator|.
name|serverName
operator|=
name|serverName
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|AsyncAdminRequestRetryingCaller
argument_list|<
name|T
argument_list|>
name|build
parameter_list|()
block|{
return|return
operator|new
name|AsyncAdminRequestRetryingCaller
argument_list|<
name|T
argument_list|>
argument_list|(
name|retryTimer
argument_list|,
name|conn
argument_list|,
name|pauseNs
argument_list|,
name|maxAttempts
argument_list|,
name|operationTimeoutNs
argument_list|,
name|rpcTimeoutNs
argument_list|,
name|startLogErrorsCnt
argument_list|,
name|serverName
argument_list|,
name|checkNotNull
argument_list|(
name|callable
argument_list|,
literal|"action is null"
argument_list|)
argument_list|)
return|;
block|}
specifier|public
name|CompletableFuture
argument_list|<
name|T
argument_list|>
name|call
parameter_list|()
block|{
return|return
name|build
argument_list|()
operator|.
name|call
argument_list|()
return|;
block|}
block|}
specifier|public
parameter_list|<
name|T
parameter_list|>
name|AdminRequestCallerBuilder
argument_list|<
name|T
argument_list|>
name|adminRequest
parameter_list|()
block|{
return|return
operator|new
name|AdminRequestCallerBuilder
argument_list|<>
argument_list|()
return|;
block|}
specifier|public
class|class
name|ServerRequestCallerBuilder
parameter_list|<
name|T
parameter_list|>
extends|extends
name|BuilderBase
block|{
specifier|private
name|AsyncServerRequestRpcRetryingCaller
operator|.
name|Callable
argument_list|<
name|T
argument_list|>
name|callable
decl_stmt|;
specifier|private
name|long
name|operationTimeoutNs
init|=
operator|-
literal|1L
decl_stmt|;
specifier|private
name|long
name|rpcTimeoutNs
init|=
operator|-
literal|1L
decl_stmt|;
specifier|private
name|ServerName
name|serverName
decl_stmt|;
specifier|public
name|ServerRequestCallerBuilder
argument_list|<
name|T
argument_list|>
name|action
parameter_list|(
name|AsyncServerRequestRpcRetryingCaller
operator|.
name|Callable
argument_list|<
name|T
argument_list|>
name|callable
parameter_list|)
block|{
name|this
operator|.
name|callable
operator|=
name|callable
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|ServerRequestCallerBuilder
argument_list|<
name|T
argument_list|>
name|operationTimeout
parameter_list|(
name|long
name|operationTimeout
parameter_list|,
name|TimeUnit
name|unit
parameter_list|)
block|{
name|this
operator|.
name|operationTimeoutNs
operator|=
name|unit
operator|.
name|toNanos
argument_list|(
name|operationTimeout
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|ServerRequestCallerBuilder
argument_list|<
name|T
argument_list|>
name|rpcTimeout
parameter_list|(
name|long
name|rpcTimeout
parameter_list|,
name|TimeUnit
name|unit
parameter_list|)
block|{
name|this
operator|.
name|rpcTimeoutNs
operator|=
name|unit
operator|.
name|toNanos
argument_list|(
name|rpcTimeout
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|ServerRequestCallerBuilder
argument_list|<
name|T
argument_list|>
name|pause
parameter_list|(
name|long
name|pause
parameter_list|,
name|TimeUnit
name|unit
parameter_list|)
block|{
name|this
operator|.
name|pauseNs
operator|=
name|unit
operator|.
name|toNanos
argument_list|(
name|pause
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|ServerRequestCallerBuilder
argument_list|<
name|T
argument_list|>
name|maxAttempts
parameter_list|(
name|int
name|maxAttempts
parameter_list|)
block|{
name|this
operator|.
name|maxAttempts
operator|=
name|maxAttempts
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|ServerRequestCallerBuilder
argument_list|<
name|T
argument_list|>
name|startLogErrorsCnt
parameter_list|(
name|int
name|startLogErrorsCnt
parameter_list|)
block|{
name|this
operator|.
name|startLogErrorsCnt
operator|=
name|startLogErrorsCnt
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|ServerRequestCallerBuilder
argument_list|<
name|T
argument_list|>
name|serverName
parameter_list|(
name|ServerName
name|serverName
parameter_list|)
block|{
name|this
operator|.
name|serverName
operator|=
name|serverName
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|AsyncServerRequestRpcRetryingCaller
argument_list|<
name|T
argument_list|>
name|build
parameter_list|()
block|{
return|return
operator|new
name|AsyncServerRequestRpcRetryingCaller
argument_list|<
name|T
argument_list|>
argument_list|(
name|retryTimer
argument_list|,
name|conn
argument_list|,
name|pauseNs
argument_list|,
name|maxAttempts
argument_list|,
name|operationTimeoutNs
argument_list|,
name|rpcTimeoutNs
argument_list|,
name|startLogErrorsCnt
argument_list|,
name|serverName
argument_list|,
name|checkNotNull
argument_list|(
name|callable
argument_list|,
literal|"action is null"
argument_list|)
argument_list|)
return|;
block|}
specifier|public
name|CompletableFuture
argument_list|<
name|T
argument_list|>
name|call
parameter_list|()
block|{
return|return
name|build
argument_list|()
operator|.
name|call
argument_list|()
return|;
block|}
block|}
specifier|public
parameter_list|<
name|T
parameter_list|>
name|ServerRequestCallerBuilder
argument_list|<
name|T
argument_list|>
name|serverRequest
parameter_list|()
block|{
return|return
operator|new
name|ServerRequestCallerBuilder
argument_list|<>
argument_list|()
return|;
block|}
block|}
end_class

end_unit

