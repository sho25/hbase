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
name|incRPCCallsMetrics
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
name|incRPCRetriesMetrics
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
name|isRemote
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
name|updateResultsMetrics
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
name|updateServerSideMetrics
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
name|NotServingRegionException
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
name|UnknownScannerException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|yetus
operator|.
name|audience
operator|.
name|InterfaceAudience
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|LoggerFactory
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
name|exceptions
operator|.
name|ScannerResetException
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
name|regionserver
operator|.
name|RegionServerStoppedException
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
comment|/**  * Scanner operations such as create, next, etc.  * Used by {@link ResultScanner}s made by {@link Table}. Passed to a retrying caller such as  * {@link RpcRetryingCaller} so fails are retried.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|ScannerCallable
extends|extends
name|ClientServiceCallable
argument_list|<
name|Result
index|[]
argument_list|>
block|{
specifier|public
specifier|static
specifier|final
name|String
name|LOG_SCANNER_LATENCY_CUTOFF
init|=
literal|"hbase.client.log.scanner.latency.cutoff"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|LOG_SCANNER_ACTIVITY
init|=
literal|"hbase.client.log.scanner.activity"
decl_stmt|;
comment|// Keeping LOG public as it is being used in TestScannerHeartbeatMessages
specifier|public
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|ScannerCallable
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|protected
name|long
name|scannerId
init|=
operator|-
literal|1L
decl_stmt|;
specifier|protected
name|boolean
name|instantiated
init|=
literal|false
decl_stmt|;
specifier|protected
name|boolean
name|closed
init|=
literal|false
decl_stmt|;
specifier|protected
name|boolean
name|renew
init|=
literal|false
decl_stmt|;
specifier|protected
specifier|final
name|Scan
name|scan
decl_stmt|;
specifier|private
name|int
name|caching
init|=
literal|1
decl_stmt|;
specifier|protected
name|ScanMetrics
name|scanMetrics
decl_stmt|;
specifier|private
name|boolean
name|logScannerActivity
init|=
literal|false
decl_stmt|;
specifier|private
name|int
name|logCutOffLatency
init|=
literal|1000
decl_stmt|;
specifier|protected
specifier|final
name|int
name|id
decl_stmt|;
enum|enum
name|MoreResults
block|{
name|YES
block|,
name|NO
block|,
name|UNKNOWN
block|}
specifier|private
name|MoreResults
name|moreResultsInRegion
decl_stmt|;
specifier|private
name|MoreResults
name|moreResultsForScan
decl_stmt|;
comment|/**    * Saves whether or not the most recent response from the server was a heartbeat message.    * Heartbeat messages are identified by the flag {@link ScanResponse#getHeartbeatMessage()}    */
specifier|protected
name|boolean
name|heartbeatMessage
init|=
literal|false
decl_stmt|;
specifier|protected
name|Cursor
name|cursor
decl_stmt|;
comment|// indicate if it is a remote server call
specifier|protected
name|boolean
name|isRegionServerRemote
init|=
literal|true
decl_stmt|;
specifier|private
name|long
name|nextCallSeq
init|=
literal|0
decl_stmt|;
specifier|protected
specifier|final
name|RpcControllerFactory
name|rpcControllerFactory
decl_stmt|;
comment|/**    * @param connection which connection    * @param tableName table callable is on    * @param scan the scan to execute    * @param scanMetrics the ScanMetrics to used, if it is null, ScannerCallable won't collect    *          metrics    * @param rpcControllerFactory factory to use when creating    *        {@link com.google.protobuf.RpcController}    */
specifier|public
name|ScannerCallable
parameter_list|(
name|ClusterConnection
name|connection
parameter_list|,
name|TableName
name|tableName
parameter_list|,
name|Scan
name|scan
parameter_list|,
name|ScanMetrics
name|scanMetrics
parameter_list|,
name|RpcControllerFactory
name|rpcControllerFactory
parameter_list|)
block|{
name|this
argument_list|(
name|connection
argument_list|,
name|tableName
argument_list|,
name|scan
argument_list|,
name|scanMetrics
argument_list|,
name|rpcControllerFactory
argument_list|,
literal|0
argument_list|)
expr_stmt|;
block|}
comment|/**    *    * @param connection    * @param tableName    * @param scan    * @param scanMetrics    * @param id the replicaId    */
specifier|public
name|ScannerCallable
parameter_list|(
name|ClusterConnection
name|connection
parameter_list|,
name|TableName
name|tableName
parameter_list|,
name|Scan
name|scan
parameter_list|,
name|ScanMetrics
name|scanMetrics
parameter_list|,
name|RpcControllerFactory
name|rpcControllerFactory
parameter_list|,
name|int
name|id
parameter_list|)
block|{
name|super
argument_list|(
name|connection
argument_list|,
name|tableName
argument_list|,
name|scan
operator|.
name|getStartRow
argument_list|()
argument_list|,
name|rpcControllerFactory
operator|.
name|newController
argument_list|()
argument_list|,
name|scan
operator|.
name|getPriority
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
name|scan
operator|=
name|scan
expr_stmt|;
name|this
operator|.
name|scanMetrics
operator|=
name|scanMetrics
expr_stmt|;
name|Configuration
name|conf
init|=
name|connection
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
name|logScannerActivity
operator|=
name|conf
operator|.
name|getBoolean
argument_list|(
name|LOG_SCANNER_ACTIVITY
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|logCutOffLatency
operator|=
name|conf
operator|.
name|getInt
argument_list|(
name|LOG_SCANNER_LATENCY_CUTOFF
argument_list|,
literal|1000
argument_list|)
expr_stmt|;
name|this
operator|.
name|rpcControllerFactory
operator|=
name|rpcControllerFactory
expr_stmt|;
block|}
comment|/**    * @param reload force reload of server location    * @throws IOException    */
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
name|RegionLocations
name|rl
init|=
name|RpcRetryingCallerWithReadReplicas
operator|.
name|getRegionLocations
argument_list|(
operator|!
name|reload
argument_list|,
name|id
argument_list|,
name|getConnection
argument_list|()
argument_list|,
name|getTableName
argument_list|()
argument_list|,
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
name|super
operator|.
name|getConnection
argument_list|()
operator|.
name|getClient
argument_list|(
name|dest
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|instantiated
operator|||
name|reload
condition|)
block|{
name|checkIfRegionServerIsRemote
argument_list|()
expr_stmt|;
name|instantiated
operator|=
literal|true
expr_stmt|;
block|}
name|cursor
operator|=
literal|null
expr_stmt|;
comment|// check how often we retry.
if|if
condition|(
name|reload
condition|)
block|{
name|incRPCRetriesMetrics
argument_list|(
name|scanMetrics
argument_list|,
name|isRegionServerRemote
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * compare the local machine hostname with region server's hostname to decide if hbase client    * connects to a remote region server    */
specifier|protected
name|void
name|checkIfRegionServerIsRemote
parameter_list|()
block|{
name|isRegionServerRemote
operator|=
name|isRemote
argument_list|(
name|getLocation
argument_list|()
operator|.
name|getHostname
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|private
name|ScanResponse
name|next
parameter_list|()
throws|throws
name|IOException
block|{
comment|// Reset the heartbeat flag prior to each RPC in case an exception is thrown by the server
name|setHeartbeatMessage
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|incRPCCallsMetrics
argument_list|(
name|scanMetrics
argument_list|,
name|isRegionServerRemote
argument_list|)
expr_stmt|;
name|ScanRequest
name|request
init|=
name|RequestConverter
operator|.
name|buildScanRequest
argument_list|(
name|scannerId
argument_list|,
name|caching
argument_list|,
literal|false
argument_list|,
name|nextCallSeq
argument_list|,
name|this
operator|.
name|scanMetrics
operator|!=
literal|null
argument_list|,
name|renew
argument_list|,
name|scan
operator|.
name|getLimit
argument_list|()
argument_list|)
decl_stmt|;
try|try
block|{
name|ScanResponse
name|response
init|=
name|getStub
argument_list|()
operator|.
name|scan
argument_list|(
name|getRpcController
argument_list|()
argument_list|,
name|request
argument_list|)
decl_stmt|;
name|nextCallSeq
operator|++
expr_stmt|;
return|return
name|response
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|IOException
name|ioe
init|=
name|ProtobufUtil
operator|.
name|handleRemoteException
argument_list|(
name|e
argument_list|)
decl_stmt|;
if|if
condition|(
name|logScannerActivity
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Got exception making request "
operator|+
name|ProtobufUtil
operator|.
name|toText
argument_list|(
name|request
argument_list|)
operator|+
literal|" to "
operator|+
name|getLocation
argument_list|()
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|logScannerActivity
condition|)
block|{
if|if
condition|(
name|ioe
operator|instanceof
name|UnknownScannerException
condition|)
block|{
try|try
block|{
name|HRegionLocation
name|location
init|=
name|getConnection
argument_list|()
operator|.
name|relocateRegion
argument_list|(
name|getTableName
argument_list|()
argument_list|,
name|scan
operator|.
name|getStartRow
argument_list|()
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Scanner="
operator|+
name|scannerId
operator|+
literal|" expired, current region location is "
operator|+
name|location
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Failed to relocate region"
argument_list|,
name|t
argument_list|)
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|ioe
operator|instanceof
name|ScannerResetException
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Scanner="
operator|+
name|scannerId
operator|+
literal|" has received an exception, and the server "
operator|+
literal|"asked us to reset the scanner state."
argument_list|,
name|ioe
argument_list|)
expr_stmt|;
block|}
block|}
comment|// The below convertion of exceptions into DoNotRetryExceptions is a little strange.
comment|// Why not just have these exceptions implment DNRIOE you ask? Well, usually we want
comment|// ServerCallable#withRetries to just retry when it gets these exceptions. In here in
comment|// a scan when doing a next in particular, we want to break out and get the scanner to
comment|// reset itself up again. Throwing a DNRIOE is how we signal this to happen (its ugly,
comment|// yeah and hard to follow and in need of a refactor).
if|if
condition|(
name|ioe
operator|instanceof
name|NotServingRegionException
condition|)
block|{
comment|// Throw a DNRE so that we break out of cycle of calling NSRE
comment|// when what we need is to open scanner against new location.
comment|// Attach NSRE to signal client that it needs to re-setup scanner.
if|if
condition|(
name|this
operator|.
name|scanMetrics
operator|!=
literal|null
condition|)
block|{
name|this
operator|.
name|scanMetrics
operator|.
name|countOfNSRE
operator|.
name|incrementAndGet
argument_list|()
expr_stmt|;
block|}
throw|throw
operator|new
name|DoNotRetryIOException
argument_list|(
literal|"Resetting the scanner -- see exception cause"
argument_list|,
name|ioe
argument_list|)
throw|;
block|}
elseif|else
if|if
condition|(
name|ioe
operator|instanceof
name|RegionServerStoppedException
condition|)
block|{
comment|// Throw a DNRE so that we break out of cycle of the retries and instead go and
comment|// open scanner against new location.
throw|throw
operator|new
name|DoNotRetryIOException
argument_list|(
literal|"Resetting the scanner -- see exception cause"
argument_list|,
name|ioe
argument_list|)
throw|;
block|}
else|else
block|{
comment|// The outer layers will retry
throw|throw
name|ioe
throw|;
block|}
block|}
block|}
specifier|private
name|void
name|setAlreadyClosed
parameter_list|()
block|{
name|this
operator|.
name|scannerId
operator|=
operator|-
literal|1L
expr_stmt|;
name|this
operator|.
name|closed
operator|=
literal|true
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|Result
index|[]
name|rpcCall
parameter_list|()
throws|throws
name|Exception
block|{
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
name|closed
condition|)
block|{
name|close
argument_list|()
expr_stmt|;
return|return
literal|null
return|;
block|}
name|ScanResponse
name|response
decl_stmt|;
if|if
condition|(
name|this
operator|.
name|scannerId
operator|==
operator|-
literal|1L
condition|)
block|{
name|response
operator|=
name|openScanner
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|response
operator|=
name|next
argument_list|()
expr_stmt|;
block|}
name|long
name|timestamp
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|boolean
name|isHeartBeat
init|=
name|response
operator|.
name|hasHeartbeatMessage
argument_list|()
operator|&&
name|response
operator|.
name|getHeartbeatMessage
argument_list|()
decl_stmt|;
name|setHeartbeatMessage
argument_list|(
name|isHeartBeat
argument_list|)
expr_stmt|;
if|if
condition|(
name|isHeartBeat
operator|&&
name|scan
operator|.
name|isNeedCursorResult
argument_list|()
operator|&&
name|response
operator|.
name|hasCursor
argument_list|()
condition|)
block|{
name|cursor
operator|=
name|ProtobufUtil
operator|.
name|toCursor
argument_list|(
name|response
operator|.
name|getCursor
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|Result
index|[]
name|rrs
init|=
name|ResponseConverter
operator|.
name|getResults
argument_list|(
name|getRpcControllerCellScanner
argument_list|()
argument_list|,
name|response
argument_list|)
decl_stmt|;
if|if
condition|(
name|logScannerActivity
condition|)
block|{
name|long
name|now
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
if|if
condition|(
name|now
operator|-
name|timestamp
operator|>
name|logCutOffLatency
condition|)
block|{
name|int
name|rows
init|=
name|rrs
operator|==
literal|null
condition|?
literal|0
else|:
name|rrs
operator|.
name|length
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Took "
operator|+
operator|(
name|now
operator|-
name|timestamp
operator|)
operator|+
literal|"ms to fetch "
operator|+
name|rows
operator|+
literal|" rows from scanner="
operator|+
name|scannerId
argument_list|)
expr_stmt|;
block|}
block|}
name|updateServerSideMetrics
argument_list|(
name|scanMetrics
argument_list|,
name|response
argument_list|)
expr_stmt|;
comment|// moreResults is only used for the case where a filter exhausts all elements
if|if
condition|(
name|response
operator|.
name|hasMoreResults
argument_list|()
condition|)
block|{
if|if
condition|(
name|response
operator|.
name|getMoreResults
argument_list|()
condition|)
block|{
name|setMoreResultsForScan
argument_list|(
name|MoreResults
operator|.
name|YES
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|setMoreResultsForScan
argument_list|(
name|MoreResults
operator|.
name|NO
argument_list|)
expr_stmt|;
name|setAlreadyClosed
argument_list|()
expr_stmt|;
block|}
block|}
else|else
block|{
name|setMoreResultsForScan
argument_list|(
name|MoreResults
operator|.
name|UNKNOWN
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|response
operator|.
name|hasMoreResultsInRegion
argument_list|()
condition|)
block|{
if|if
condition|(
name|response
operator|.
name|getMoreResultsInRegion
argument_list|()
condition|)
block|{
name|setMoreResultsInRegion
argument_list|(
name|MoreResults
operator|.
name|YES
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|setMoreResultsInRegion
argument_list|(
name|MoreResults
operator|.
name|NO
argument_list|)
expr_stmt|;
name|setAlreadyClosed
argument_list|()
expr_stmt|;
block|}
block|}
else|else
block|{
name|setMoreResultsInRegion
argument_list|(
name|MoreResults
operator|.
name|UNKNOWN
argument_list|)
expr_stmt|;
block|}
name|updateResultsMetrics
argument_list|(
name|scanMetrics
argument_list|,
name|rrs
argument_list|,
name|isRegionServerRemote
argument_list|)
expr_stmt|;
return|return
name|rrs
return|;
block|}
comment|/**    * @return true when the most recent RPC response indicated that the response was a heartbeat    *         message. Heartbeat messages are sent back from the server when the processing of the    *         scan request exceeds a certain time threshold. Heartbeats allow the server to avoid    *         timeouts during long running scan operations.    */
name|boolean
name|isHeartbeatMessage
parameter_list|()
block|{
return|return
name|heartbeatMessage
return|;
block|}
specifier|public
name|Cursor
name|getCursor
parameter_list|()
block|{
return|return
name|cursor
return|;
block|}
specifier|private
name|void
name|setHeartbeatMessage
parameter_list|(
name|boolean
name|heartbeatMessage
parameter_list|)
block|{
name|this
operator|.
name|heartbeatMessage
operator|=
name|heartbeatMessage
expr_stmt|;
block|}
specifier|private
name|void
name|close
parameter_list|()
block|{
if|if
condition|(
name|this
operator|.
name|scannerId
operator|==
operator|-
literal|1L
condition|)
block|{
return|return;
block|}
try|try
block|{
name|incRPCCallsMetrics
argument_list|(
name|scanMetrics
argument_list|,
name|isRegionServerRemote
argument_list|)
expr_stmt|;
name|ScanRequest
name|request
init|=
name|RequestConverter
operator|.
name|buildScanRequest
argument_list|(
name|this
operator|.
name|scannerId
argument_list|,
literal|0
argument_list|,
literal|true
argument_list|,
name|this
operator|.
name|scanMetrics
operator|!=
literal|null
argument_list|)
decl_stmt|;
try|try
block|{
name|getStub
argument_list|()
operator|.
name|scan
argument_list|(
name|getRpcController
argument_list|()
argument_list|,
name|request
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
name|ProtobufUtil
operator|.
name|handleRemoteException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|TableName
name|table
init|=
name|getTableName
argument_list|()
decl_stmt|;
name|String
name|tableDetails
init|=
operator|(
name|table
operator|==
literal|null
operator|)
condition|?
literal|""
else|:
operator|(
literal|" on table: "
operator|+
name|table
operator|.
name|getNameAsString
argument_list|()
operator|)
decl_stmt|;
name|LOG
operator|.
name|warn
argument_list|(
literal|"Ignore, probably already closed. Current scan: "
operator|+
name|getScan
argument_list|()
operator|.
name|toString
argument_list|()
operator|+
name|tableDetails
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
name|this
operator|.
name|scannerId
operator|=
operator|-
literal|1L
expr_stmt|;
block|}
specifier|private
name|ScanResponse
name|openScanner
parameter_list|()
throws|throws
name|IOException
block|{
name|incRPCCallsMetrics
argument_list|(
name|scanMetrics
argument_list|,
name|isRegionServerRemote
argument_list|)
expr_stmt|;
name|ScanRequest
name|request
init|=
name|RequestConverter
operator|.
name|buildScanRequest
argument_list|(
name|getLocation
argument_list|()
operator|.
name|getRegion
argument_list|()
operator|.
name|getRegionName
argument_list|()
argument_list|,
name|this
operator|.
name|scan
argument_list|,
name|this
operator|.
name|caching
argument_list|,
literal|false
argument_list|)
decl_stmt|;
try|try
block|{
name|ScanResponse
name|response
init|=
name|getStub
argument_list|()
operator|.
name|scan
argument_list|(
name|getRpcController
argument_list|()
argument_list|,
name|request
argument_list|)
decl_stmt|;
name|long
name|id
init|=
name|response
operator|.
name|getScannerId
argument_list|()
decl_stmt|;
if|if
condition|(
name|logScannerActivity
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Open scanner="
operator|+
name|id
operator|+
literal|" for scan="
operator|+
name|scan
operator|.
name|toString
argument_list|()
operator|+
literal|" on region "
operator|+
name|getLocation
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|response
operator|.
name|hasMvccReadPoint
argument_list|()
condition|)
block|{
name|this
operator|.
name|scan
operator|.
name|setMvccReadPoint
argument_list|(
name|response
operator|.
name|getMvccReadPoint
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|this
operator|.
name|scannerId
operator|=
name|id
expr_stmt|;
return|return
name|response
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
name|ProtobufUtil
operator|.
name|handleRemoteException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
specifier|protected
name|Scan
name|getScan
parameter_list|()
block|{
return|return
name|scan
return|;
block|}
comment|/**    * Call this when the next invocation of call should close the scanner    */
specifier|public
name|void
name|setClose
parameter_list|()
block|{
name|this
operator|.
name|closed
operator|=
literal|true
expr_stmt|;
block|}
comment|/**    * Indicate whether we make a call only to renew the lease, but without affected the scanner in    * any other way.    * @param val true if only the lease should be renewed    */
specifier|public
name|void
name|setRenew
parameter_list|(
name|boolean
name|val
parameter_list|)
block|{
name|this
operator|.
name|renew
operator|=
name|val
expr_stmt|;
block|}
comment|/**    * @return the RegionInfo for the current region    */
annotation|@
name|Override
specifier|public
name|RegionInfo
name|getRegionInfo
parameter_list|()
block|{
if|if
condition|(
operator|!
name|instantiated
condition|)
block|{
return|return
literal|null
return|;
block|}
return|return
name|getLocation
argument_list|()
operator|.
name|getRegion
argument_list|()
return|;
block|}
comment|/**    * Get the number of rows that will be fetched on next    * @return the number of rows for caching    */
specifier|public
name|int
name|getCaching
parameter_list|()
block|{
return|return
name|caching
return|;
block|}
comment|/**    * Set the number of rows that will be fetched on next    * @param caching the number of rows for caching    */
specifier|public
name|void
name|setCaching
parameter_list|(
name|int
name|caching
parameter_list|)
block|{
name|this
operator|.
name|caching
operator|=
name|caching
expr_stmt|;
block|}
specifier|public
name|ScannerCallable
name|getScannerCallableForReplica
parameter_list|(
name|int
name|id
parameter_list|)
block|{
name|ScannerCallable
name|s
init|=
operator|new
name|ScannerCallable
argument_list|(
name|this
operator|.
name|getConnection
argument_list|()
argument_list|,
name|getTableName
argument_list|()
argument_list|,
name|this
operator|.
name|getScan
argument_list|()
argument_list|,
name|this
operator|.
name|scanMetrics
argument_list|,
name|this
operator|.
name|rpcControllerFactory
argument_list|,
name|id
argument_list|)
decl_stmt|;
name|s
operator|.
name|setCaching
argument_list|(
name|this
operator|.
name|caching
argument_list|)
expr_stmt|;
return|return
name|s
return|;
block|}
comment|/**    * Should the client attempt to fetch more results from this region    */
name|MoreResults
name|moreResultsInRegion
parameter_list|()
block|{
return|return
name|moreResultsInRegion
return|;
block|}
name|void
name|setMoreResultsInRegion
parameter_list|(
name|MoreResults
name|moreResults
parameter_list|)
block|{
name|this
operator|.
name|moreResultsInRegion
operator|=
name|moreResults
expr_stmt|;
block|}
comment|/**    * Should the client attempt to fetch more results for the whole scan.    */
name|MoreResults
name|moreResultsForScan
parameter_list|()
block|{
return|return
name|moreResultsForScan
return|;
block|}
name|void
name|setMoreResultsForScan
parameter_list|(
name|MoreResults
name|moreResults
parameter_list|)
block|{
name|this
operator|.
name|moreResultsForScan
operator|=
name|moreResults
expr_stmt|;
block|}
block|}
end_class

end_unit

