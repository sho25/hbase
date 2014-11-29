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
name|net
operator|.
name|UnknownHostException
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
name|Cell
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
name|protobuf
operator|.
name|generated
operator|.
name|ClientProtos
operator|.
name|ScanResponse
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
name|ipc
operator|.
name|RemoteException
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
name|net
operator|.
name|DNS
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

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|TextFormat
import|;
end_import

begin_comment
comment|/**  * Scanner operations such as create, next, etc.  * Used by {@link ResultScanner}s made by {@link HTable}. Passed to a retrying caller such as  * {@link RpcRetryingCaller} so fails are retried.  */
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
name|RegionServerCallable
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
specifier|public
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
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
specifier|private
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
specifier|final
name|ClusterConnection
name|cConnection
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
specifier|private
specifier|static
name|String
name|myAddress
decl_stmt|;
specifier|protected
specifier|final
name|int
name|id
decl_stmt|;
static|static
block|{
try|try
block|{
name|myAddress
operator|=
name|DNS
operator|.
name|getDefaultHost
argument_list|(
literal|"default"
argument_list|,
literal|"default"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|UnknownHostException
name|uhe
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"cannot determine my address"
argument_list|,
name|uhe
argument_list|)
expr_stmt|;
block|}
block|}
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
name|RpcControllerFactory
name|controllerFactory
decl_stmt|;
comment|/**    * @param connection which connection    * @param tableName table callable is on    * @param scan the scan to execute    * @param scanMetrics the ScanMetrics to used, if it is null, ScannerCallable won't collect    *          metrics    * @param rpcControllerFactory factory to use when creating {@link RpcController}    */
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
name|cConnection
operator|=
name|connection
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
name|controllerFactory
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
comment|// check how often we retry.
comment|// HConnectionManager will call instantiateServer with reload==true
comment|// if and only if for retries.
if|if
condition|(
name|reload
operator|&&
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
name|countOfRPCRetries
operator|.
name|incrementAndGet
argument_list|()
expr_stmt|;
if|if
condition|(
name|isRegionServerRemote
condition|)
block|{
name|this
operator|.
name|scanMetrics
operator|.
name|countOfRemoteRPCRetries
operator|.
name|incrementAndGet
argument_list|()
expr_stmt|;
block|}
block|}
block|}
comment|/**    * compare the local machine hostname with region server's hostname    * to decide if hbase client connects to a remote region server    */
specifier|protected
name|void
name|checkIfRegionServerIsRemote
parameter_list|()
block|{
if|if
condition|(
name|getLocation
argument_list|()
operator|.
name|getHostname
argument_list|()
operator|.
name|equalsIgnoreCase
argument_list|(
name|myAddress
argument_list|)
condition|)
block|{
name|isRegionServerRemote
operator|=
literal|false
expr_stmt|;
block|}
else|else
block|{
name|isRegionServerRemote
operator|=
literal|true
expr_stmt|;
block|}
block|}
annotation|@
name|Override
annotation|@
name|SuppressWarnings
argument_list|(
literal|"deprecation"
argument_list|)
specifier|public
name|Result
index|[]
name|call
parameter_list|(
name|int
name|callTimeout
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
if|if
condition|(
name|closed
condition|)
block|{
if|if
condition|(
name|scannerId
operator|!=
operator|-
literal|1
condition|)
block|{
name|close
argument_list|()
expr_stmt|;
block|}
block|}
else|else
block|{
if|if
condition|(
name|scannerId
operator|==
operator|-
literal|1L
condition|)
block|{
name|this
operator|.
name|scannerId
operator|=
name|openScanner
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|Result
index|[]
name|rrs
init|=
literal|null
decl_stmt|;
name|ScanRequest
name|request
init|=
literal|null
decl_stmt|;
try|try
block|{
name|incRPCcallsMetrics
argument_list|()
expr_stmt|;
name|request
operator|=
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
argument_list|)
expr_stmt|;
name|ScanResponse
name|response
init|=
literal|null
decl_stmt|;
name|PayloadCarryingRpcController
name|controller
init|=
name|controllerFactory
operator|.
name|newController
argument_list|()
decl_stmt|;
name|controller
operator|.
name|setPriority
argument_list|(
name|getTableName
argument_list|()
argument_list|)
expr_stmt|;
name|controller
operator|.
name|setCallTimeout
argument_list|(
name|callTimeout
argument_list|)
expr_stmt|;
try|try
block|{
name|response
operator|=
name|getStub
argument_list|()
operator|.
name|scan
argument_list|(
name|controller
argument_list|,
name|request
argument_list|)
expr_stmt|;
comment|// Client and RS maintain a nextCallSeq number during the scan. Every next() call
comment|// from client to server will increment this number in both sides. Client passes this
comment|// number along with the request and at RS side both the incoming nextCallSeq and its
comment|// nextCallSeq will be matched. In case of a timeout this increment at the client side
comment|// should not happen. If at the server side fetching of next batch of data was over,
comment|// there will be mismatch in the nextCallSeq number. Server will throw
comment|// OutOfOrderScannerNextException and then client will reopen the scanner with startrow
comment|// as the last successfully retrieved row.
comment|// See HBASE-5974
name|nextCallSeq
operator|++
expr_stmt|;
name|long
name|timestamp
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
comment|// Results are returned via controller
name|CellScanner
name|cellScanner
init|=
name|controller
operator|.
name|cellScanner
argument_list|()
decl_stmt|;
name|rrs
operator|=
name|ResponseConverter
operator|.
name|getResults
argument_list|(
name|cellScanner
argument_list|,
name|response
argument_list|)
expr_stmt|;
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
if|if
condition|(
name|response
operator|.
name|hasMoreResults
argument_list|()
operator|&&
operator|!
name|response
operator|.
name|getMoreResults
argument_list|()
condition|)
block|{
name|scannerId
operator|=
operator|-
literal|1L
expr_stmt|;
name|closed
operator|=
literal|true
expr_stmt|;
return|return
literal|null
return|;
block|}
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
name|updateResultsMetrics
argument_list|(
name|rrs
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
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
name|TextFormat
operator|.
name|shortDebugString
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
name|IOException
name|ioe
init|=
name|e
decl_stmt|;
if|if
condition|(
name|e
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
name|e
operator|)
operator|.
name|unwrapRemoteException
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|logScannerActivity
operator|&&
operator|(
name|ioe
operator|instanceof
name|UnknownScannerException
operator|)
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
comment|// The below convertion of exceptions into DoNotRetryExceptions is a little strange.
comment|// Why not just have these exceptions implment DNRIOE you ask?  Well, usually we want
comment|// ServerCallable#withRetries to just retry when it gets these exceptions.  In here in
comment|// a scan when doing a next in particular, we want to break out and get the scanner to
comment|// reset itself up again.  Throwing a DNRIOE is how we signal this to happen (its ugly,
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
return|return
name|rrs
return|;
block|}
block|}
return|return
literal|null
return|;
block|}
specifier|private
name|void
name|incRPCcallsMetrics
parameter_list|()
block|{
if|if
condition|(
name|this
operator|.
name|scanMetrics
operator|==
literal|null
condition|)
block|{
return|return;
block|}
name|this
operator|.
name|scanMetrics
operator|.
name|countOfRPCcalls
operator|.
name|incrementAndGet
argument_list|()
expr_stmt|;
if|if
condition|(
name|isRegionServerRemote
condition|)
block|{
name|this
operator|.
name|scanMetrics
operator|.
name|countOfRemoteRPCcalls
operator|.
name|incrementAndGet
argument_list|()
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|updateResultsMetrics
parameter_list|(
name|Result
index|[]
name|rrs
parameter_list|)
block|{
if|if
condition|(
name|this
operator|.
name|scanMetrics
operator|==
literal|null
operator|||
name|rrs
operator|==
literal|null
operator|||
name|rrs
operator|.
name|length
operator|==
literal|0
condition|)
block|{
return|return;
block|}
name|long
name|resultSize
init|=
literal|0
decl_stmt|;
for|for
control|(
name|Result
name|rr
range|:
name|rrs
control|)
block|{
for|for
control|(
name|Cell
name|cell
range|:
name|rr
operator|.
name|rawCells
argument_list|()
control|)
block|{
name|resultSize
operator|+=
name|CellUtil
operator|.
name|estimatedSerializedSizeOf
argument_list|(
name|cell
argument_list|)
expr_stmt|;
block|}
block|}
name|this
operator|.
name|scanMetrics
operator|.
name|countOfBytesInResults
operator|.
name|addAndGet
argument_list|(
name|resultSize
argument_list|)
expr_stmt|;
if|if
condition|(
name|isRegionServerRemote
condition|)
block|{
name|this
operator|.
name|scanMetrics
operator|.
name|countOfBytesInRemoteResults
operator|.
name|addAndGet
argument_list|(
name|resultSize
argument_list|)
expr_stmt|;
block|}
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
name|incRPCcallsMetrics
argument_list|()
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
argument_list|)
decl_stmt|;
try|try
block|{
name|getStub
argument_list|()
operator|.
name|scan
argument_list|(
literal|null
argument_list|,
name|request
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
literal|"Ignore, probably already closed"
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
specifier|protected
name|long
name|openScanner
parameter_list|()
throws|throws
name|IOException
block|{
name|incRPCcallsMetrics
argument_list|()
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
name|getRegionInfo
argument_list|()
operator|.
name|getRegionName
argument_list|()
argument_list|,
name|this
operator|.
name|scan
argument_list|,
literal|0
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
literal|null
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
return|return
name|id
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
comment|/**    * @return the HRegionInfo for the current region    */
annotation|@
name|Override
specifier|public
name|HRegionInfo
name|getHRegionInfo
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
name|getRegionInfo
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
annotation|@
name|Override
specifier|public
name|ClusterConnection
name|getConnection
parameter_list|()
block|{
return|return
name|cConnection
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
name|this
operator|.
name|tableName
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
name|controllerFactory
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
block|}
end_class

end_unit

