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
name|HConstants
operator|.
name|EMPTY_END_ROW
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
name|HConstants
operator|.
name|EMPTY_START_ROW
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
name|getLocateType
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
name|ClientService
operator|.
name|Interface
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
name|ScanRequest
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
comment|/**  * The asynchronous client scanner implementation.  *<p>  * Here we will call OpenScanner first and use the returned scannerId to create a  * {@link AsyncScanSingleRegionRpcRetryingCaller} to do the real scan operation. The return value of  * {@link AsyncScanSingleRegionRpcRetryingCaller} will tell us whether open a new scanner or finish  * scan.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
class|class
name|AsyncClientScanner
block|{
comment|// We will use this scan object during the whole scan operation. The
comment|// AsyncScanSingleRegionRpcRetryingCaller will modify this scan object directly.
specifier|private
specifier|final
name|Scan
name|scan
decl_stmt|;
specifier|private
specifier|final
name|RawScanResultConsumer
name|consumer
decl_stmt|;
specifier|private
specifier|final
name|TableName
name|tableName
decl_stmt|;
specifier|private
specifier|final
name|AsyncConnectionImpl
name|conn
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
name|scanTimeoutNs
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
name|ScanResultCache
name|resultCache
decl_stmt|;
specifier|public
name|AsyncClientScanner
parameter_list|(
name|Scan
name|scan
parameter_list|,
name|RawScanResultConsumer
name|consumer
parameter_list|,
name|TableName
name|tableName
parameter_list|,
name|AsyncConnectionImpl
name|conn
parameter_list|,
name|long
name|pauseNs
parameter_list|,
name|int
name|maxAttempts
parameter_list|,
name|long
name|scanTimeoutNs
parameter_list|,
name|long
name|rpcTimeoutNs
parameter_list|,
name|int
name|startLogErrorsCnt
parameter_list|)
block|{
if|if
condition|(
name|scan
operator|.
name|getStartRow
argument_list|()
operator|==
literal|null
condition|)
block|{
name|scan
operator|.
name|withStartRow
argument_list|(
name|EMPTY_START_ROW
argument_list|,
name|scan
operator|.
name|includeStartRow
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|scan
operator|.
name|getStopRow
argument_list|()
operator|==
literal|null
condition|)
block|{
name|scan
operator|.
name|withStopRow
argument_list|(
name|EMPTY_END_ROW
argument_list|,
name|scan
operator|.
name|includeStopRow
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|this
operator|.
name|scan
operator|=
name|scan
expr_stmt|;
name|this
operator|.
name|consumer
operator|=
name|consumer
expr_stmt|;
name|this
operator|.
name|tableName
operator|=
name|tableName
expr_stmt|;
name|this
operator|.
name|conn
operator|=
name|conn
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
name|scanTimeoutNs
operator|=
name|scanTimeoutNs
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
name|resultCache
operator|=
name|scan
operator|.
name|getAllowPartialResults
argument_list|()
operator|||
name|scan
operator|.
name|getBatch
argument_list|()
operator|>
literal|0
condition|?
operator|new
name|AllowPartialScanResultCache
argument_list|()
else|:
operator|new
name|CompleteScanResultCache
argument_list|()
expr_stmt|;
block|}
specifier|private
specifier|static
specifier|final
class|class
name|OpenScannerResponse
block|{
specifier|public
specifier|final
name|HRegionLocation
name|loc
decl_stmt|;
specifier|public
specifier|final
name|ClientService
operator|.
name|Interface
name|stub
decl_stmt|;
specifier|public
specifier|final
name|HBaseRpcController
name|controller
decl_stmt|;
specifier|public
specifier|final
name|ScanResponse
name|resp
decl_stmt|;
specifier|public
name|OpenScannerResponse
parameter_list|(
name|HRegionLocation
name|loc
parameter_list|,
name|Interface
name|stub
parameter_list|,
name|HBaseRpcController
name|controller
parameter_list|,
name|ScanResponse
name|resp
parameter_list|)
block|{
name|this
operator|.
name|loc
operator|=
name|loc
expr_stmt|;
name|this
operator|.
name|stub
operator|=
name|stub
expr_stmt|;
name|this
operator|.
name|controller
operator|=
name|controller
expr_stmt|;
name|this
operator|.
name|resp
operator|=
name|resp
expr_stmt|;
block|}
block|}
specifier|private
name|CompletableFuture
argument_list|<
name|OpenScannerResponse
argument_list|>
name|callOpenScanner
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
block|{
name|CompletableFuture
argument_list|<
name|OpenScannerResponse
argument_list|>
name|future
init|=
operator|new
name|CompletableFuture
argument_list|<>
argument_list|()
decl_stmt|;
try|try
block|{
name|ScanRequest
name|request
init|=
name|RequestConverter
operator|.
name|buildScanRequest
argument_list|(
name|loc
operator|.
name|getRegionInfo
argument_list|()
operator|.
name|getRegionName
argument_list|()
argument_list|,
name|scan
argument_list|,
name|scan
operator|.
name|getCaching
argument_list|()
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|stub
operator|.
name|scan
argument_list|(
name|controller
argument_list|,
name|request
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
name|future
operator|.
name|completeExceptionally
argument_list|(
name|controller
operator|.
name|getFailed
argument_list|()
argument_list|)
expr_stmt|;
return|return;
block|}
name|future
operator|.
name|complete
argument_list|(
operator|new
name|OpenScannerResponse
argument_list|(
name|loc
argument_list|,
name|stub
argument_list|,
name|controller
argument_list|,
name|resp
argument_list|)
argument_list|)
expr_stmt|;
block|}
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|future
operator|.
name|completeExceptionally
argument_list|(
name|e
argument_list|)
expr_stmt|;
block|}
return|return
name|future
return|;
block|}
specifier|private
name|void
name|startScan
parameter_list|(
name|OpenScannerResponse
name|resp
parameter_list|)
block|{
name|conn
operator|.
name|callerFactory
operator|.
name|scanSingleRegion
argument_list|()
operator|.
name|id
argument_list|(
name|resp
operator|.
name|resp
operator|.
name|getScannerId
argument_list|()
argument_list|)
operator|.
name|location
argument_list|(
name|resp
operator|.
name|loc
argument_list|)
operator|.
name|scannerLeaseTimeoutPeriod
argument_list|(
name|resp
operator|.
name|resp
operator|.
name|getTtl
argument_list|()
argument_list|,
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
operator|.
name|stub
argument_list|(
name|resp
operator|.
name|stub
argument_list|)
operator|.
name|setScan
argument_list|(
name|scan
argument_list|)
operator|.
name|consumer
argument_list|(
name|consumer
argument_list|)
operator|.
name|resultCache
argument_list|(
name|resultCache
argument_list|)
operator|.
name|rpcTimeout
argument_list|(
name|rpcTimeoutNs
argument_list|,
name|TimeUnit
operator|.
name|NANOSECONDS
argument_list|)
operator|.
name|scanTimeout
argument_list|(
name|scanTimeoutNs
argument_list|,
name|TimeUnit
operator|.
name|NANOSECONDS
argument_list|)
operator|.
name|pause
argument_list|(
name|pauseNs
argument_list|,
name|TimeUnit
operator|.
name|NANOSECONDS
argument_list|)
operator|.
name|maxAttempts
argument_list|(
name|maxAttempts
argument_list|)
operator|.
name|startLogErrorsCnt
argument_list|(
name|startLogErrorsCnt
argument_list|)
operator|.
name|start
argument_list|(
name|resp
operator|.
name|controller
argument_list|,
name|resp
operator|.
name|resp
argument_list|)
operator|.
name|whenComplete
argument_list|(
parameter_list|(
name|hasMore
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
name|consumer
operator|.
name|onError
argument_list|(
name|error
argument_list|)
expr_stmt|;
return|return;
block|}
if|if
condition|(
name|hasMore
condition|)
block|{
name|openScanner
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|consumer
operator|.
name|onComplete
argument_list|()
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|openScanner
parameter_list|()
block|{
name|conn
operator|.
name|callerFactory
operator|.
expr|<
name|OpenScannerResponse
operator|>
name|single
argument_list|()
operator|.
name|table
argument_list|(
name|tableName
argument_list|)
operator|.
name|row
argument_list|(
name|scan
operator|.
name|getStartRow
argument_list|()
argument_list|)
operator|.
name|locateType
argument_list|(
name|getLocateType
argument_list|(
name|scan
argument_list|)
argument_list|)
operator|.
name|rpcTimeout
argument_list|(
name|rpcTimeoutNs
argument_list|,
name|TimeUnit
operator|.
name|NANOSECONDS
argument_list|)
operator|.
name|operationTimeout
argument_list|(
name|scanTimeoutNs
argument_list|,
name|TimeUnit
operator|.
name|NANOSECONDS
argument_list|)
operator|.
name|pause
argument_list|(
name|pauseNs
argument_list|,
name|TimeUnit
operator|.
name|NANOSECONDS
argument_list|)
operator|.
name|maxAttempts
argument_list|(
name|maxAttempts
argument_list|)
operator|.
name|startLogErrorsCnt
argument_list|(
name|startLogErrorsCnt
argument_list|)
operator|.
name|action
argument_list|(
name|this
operator|::
name|callOpenScanner
argument_list|)
operator|.
name|call
argument_list|()
operator|.
name|whenComplete
argument_list|(
parameter_list|(
name|resp
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
name|consumer
operator|.
name|onError
argument_list|(
name|error
argument_list|)
expr_stmt|;
return|return;
block|}
name|startScan
argument_list|(
name|resp
argument_list|)
expr_stmt|;
block|}
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|start
parameter_list|()
block|{
name|openScanner
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

