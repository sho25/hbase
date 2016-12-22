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
specifier|public
class|class
name|SingleRequestCallerBuilder
parameter_list|<
name|T
parameter_list|>
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
name|conn
operator|.
name|connConf
operator|.
name|getPauseNs
argument_list|()
argument_list|,
name|conn
operator|.
name|connConf
operator|.
name|getMaxRetries
argument_list|()
argument_list|,
name|operationTimeoutNs
argument_list|,
name|rpcTimeoutNs
argument_list|,
name|conn
operator|.
name|connConf
operator|.
name|getStartLogErrorsCnt
argument_list|()
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
name|SmallScanCallerBuilder
block|{
specifier|private
name|TableName
name|tableName
decl_stmt|;
specifier|private
name|Scan
name|scan
decl_stmt|;
specifier|private
name|int
name|limit
decl_stmt|;
specifier|private
name|long
name|scanTimeoutNs
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
name|SmallScanCallerBuilder
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
name|SmallScanCallerBuilder
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
name|SmallScanCallerBuilder
name|limit
parameter_list|(
name|int
name|limit
parameter_list|)
block|{
name|this
operator|.
name|limit
operator|=
name|limit
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|SmallScanCallerBuilder
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
name|SmallScanCallerBuilder
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
name|AsyncSmallScanRpcRetryingCaller
name|build
parameter_list|()
block|{
name|TableName
name|tableName
init|=
name|checkNotNull
argument_list|(
name|this
operator|.
name|tableName
argument_list|,
literal|"tableName is null"
argument_list|)
decl_stmt|;
name|Scan
name|scan
init|=
name|checkNotNull
argument_list|(
name|this
operator|.
name|scan
argument_list|,
literal|"scan is null"
argument_list|)
decl_stmt|;
name|checkArgument
argument_list|(
name|limit
operator|>
literal|0
argument_list|,
literal|"invalid limit %d"
argument_list|,
name|limit
argument_list|)
expr_stmt|;
return|return
operator|new
name|AsyncSmallScanRpcRetryingCaller
argument_list|(
name|conn
argument_list|,
name|tableName
argument_list|,
name|scan
argument_list|,
name|limit
argument_list|,
name|scanTimeoutNs
argument_list|,
name|rpcTimeoutNs
argument_list|)
return|;
block|}
comment|/**      * Shortcut for {@code build().call()}      */
specifier|public
name|CompletableFuture
argument_list|<
name|List
argument_list|<
name|Result
argument_list|>
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
comment|/**    * Create retry caller for small scan.    */
specifier|public
name|SmallScanCallerBuilder
name|smallScan
parameter_list|()
block|{
return|return
operator|new
name|SmallScanCallerBuilder
argument_list|()
return|;
block|}
specifier|public
class|class
name|ScanSingleRegionCallerBuilder
block|{
specifier|private
name|long
name|scannerId
init|=
operator|-
literal|1L
decl_stmt|;
specifier|private
name|Scan
name|scan
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
name|AsyncScanSingleRegionRpcRetryingCaller
name|build
parameter_list|()
block|{
name|checkArgument
argument_list|(
name|scannerId
operator|>=
literal|0
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
name|conn
operator|.
name|connConf
operator|.
name|getPauseNs
argument_list|()
argument_list|,
name|conn
operator|.
name|connConf
operator|.
name|getMaxRetries
argument_list|()
argument_list|,
name|scanTimeoutNs
argument_list|,
name|rpcTimeoutNs
argument_list|,
name|conn
operator|.
name|connConf
operator|.
name|getStartLogErrorsCnt
argument_list|()
argument_list|)
return|;
block|}
comment|/**      * Short cut for {@code build().start()}.      */
specifier|public
name|CompletableFuture
argument_list|<
name|RegionLocateType
argument_list|>
name|start
parameter_list|()
block|{
return|return
name|build
argument_list|()
operator|.
name|start
argument_list|()
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
name|readRpcTimeoutNs
init|=
operator|-
literal|1L
decl_stmt|;
specifier|private
name|long
name|writeRpcTimeoutNs
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
name|readRpcTimeout
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
name|readRpcTimeoutNs
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
name|writeRpcTimeout
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
name|writeRpcTimeoutNs
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
argument_list|<
name|T
argument_list|>
argument_list|(
name|retryTimer
argument_list|,
name|conn
argument_list|,
name|tableName
argument_list|,
name|actions
argument_list|,
name|conn
operator|.
name|connConf
operator|.
name|getPauseNs
argument_list|()
argument_list|,
name|conn
operator|.
name|connConf
operator|.
name|getMaxRetries
argument_list|()
argument_list|,
name|operationTimeoutNs
argument_list|,
name|readRpcTimeoutNs
argument_list|,
name|writeRpcTimeoutNs
argument_list|,
name|conn
operator|.
name|connConf
operator|.
name|getStartLogErrorsCnt
argument_list|()
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
block|}
end_class

end_unit

