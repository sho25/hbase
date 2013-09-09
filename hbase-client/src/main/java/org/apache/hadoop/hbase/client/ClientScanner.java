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
name|LinkedList
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
name|classification
operator|.
name|InterfaceStability
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
name|KeyValueUtil
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
name|OutOfOrderScannerNextException
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
name|generated
operator|.
name|MapReduceProtos
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
name|util
operator|.
name|Bytes
import|;
end_import

begin_comment
comment|/**  * Implements the scanner interface for the HBase client.  * If there are multiple regions in a table, this scanner will iterate  * through them all.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Public
annotation|@
name|InterfaceStability
operator|.
name|Stable
specifier|public
class|class
name|ClientScanner
extends|extends
name|AbstractClientScanner
block|{
specifier|private
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|this
operator|.
name|getClass
argument_list|()
argument_list|)
decl_stmt|;
specifier|private
name|Scan
name|scan
decl_stmt|;
specifier|private
name|boolean
name|closed
init|=
literal|false
decl_stmt|;
comment|// Current region scanner is against.  Gets cleared if current region goes
comment|// wonky: e.g. if it splits on us.
specifier|private
name|HRegionInfo
name|currentRegion
init|=
literal|null
decl_stmt|;
specifier|private
name|ScannerCallable
name|callable
init|=
literal|null
decl_stmt|;
specifier|private
specifier|final
name|LinkedList
argument_list|<
name|Result
argument_list|>
name|cache
init|=
operator|new
name|LinkedList
argument_list|<
name|Result
argument_list|>
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|int
name|caching
decl_stmt|;
specifier|private
name|long
name|lastNext
decl_stmt|;
comment|// Keep lastResult returned successfully in case we have to reset scanner.
specifier|private
name|Result
name|lastResult
init|=
literal|null
decl_stmt|;
specifier|private
name|ScanMetrics
name|scanMetrics
init|=
literal|null
decl_stmt|;
specifier|private
specifier|final
name|long
name|maxScannerResultSize
decl_stmt|;
specifier|private
specifier|final
name|HConnection
name|connection
decl_stmt|;
specifier|private
specifier|final
name|TableName
name|tableName
decl_stmt|;
specifier|private
specifier|final
name|int
name|scannerTimeout
decl_stmt|;
specifier|private
name|boolean
name|scanMetricsPublished
init|=
literal|false
decl_stmt|;
specifier|private
name|RpcRetryingCaller
argument_list|<
name|Result
index|[]
argument_list|>
name|caller
decl_stmt|;
comment|/**      * Create a new ClientScanner for the specified table. An HConnection will be      * retrieved using the passed Configuration.      * Note that the passed {@link Scan}'s start row maybe changed changed.      *      * @param conf The {@link Configuration} to use.      * @param scan {@link Scan} to use in this scanner      * @param tableName The table that we wish to scan      * @throws IOException      */
specifier|public
name|ClientScanner
parameter_list|(
specifier|final
name|Configuration
name|conf
parameter_list|,
specifier|final
name|Scan
name|scan
parameter_list|,
specifier|final
name|TableName
name|tableName
parameter_list|)
throws|throws
name|IOException
block|{
name|this
argument_list|(
name|conf
argument_list|,
name|scan
argument_list|,
name|tableName
argument_list|,
name|HConnectionManager
operator|.
name|getConnection
argument_list|(
name|conf
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**      * Create a new ClientScanner for the specified table      * Note that the passed {@link Scan}'s start row maybe changed changed.      *      * @param conf The {@link Configuration} to use.      * @param scan {@link Scan} to use in this scanner      * @param tableName The table that we wish to scan      * @param connection Connection identifying the cluster      * @throws IOException      */
specifier|public
name|ClientScanner
parameter_list|(
specifier|final
name|Configuration
name|conf
parameter_list|,
specifier|final
name|Scan
name|scan
parameter_list|,
specifier|final
name|TableName
name|tableName
parameter_list|,
name|HConnection
name|connection
parameter_list|)
throws|throws
name|IOException
block|{
name|this
argument_list|(
name|conf
argument_list|,
name|scan
argument_list|,
name|tableName
argument_list|,
name|connection
argument_list|,
operator|new
name|RpcRetryingCallerFactory
argument_list|(
name|conf
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * Create a new ClientScanner for the specified table Note that the passed {@link Scan}'s start    * row maybe changed changed.    * @param conf The {@link Configuration} to use.    * @param scan {@link Scan} to use in this scanner    * @param tableName The table that we wish to scan    * @param connection Connection identifying the cluster    * @throws IOException    */
specifier|public
name|ClientScanner
parameter_list|(
specifier|final
name|Configuration
name|conf
parameter_list|,
specifier|final
name|Scan
name|scan
parameter_list|,
specifier|final
name|TableName
name|tableName
parameter_list|,
name|HConnection
name|connection
parameter_list|,
name|RpcRetryingCallerFactory
name|rpcFactory
parameter_list|)
throws|throws
name|IOException
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
literal|"Scan table="
operator|+
name|tableName
operator|+
literal|", startRow="
operator|+
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|scan
operator|.
name|getStartRow
argument_list|()
argument_list|)
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
name|tableName
operator|=
name|tableName
expr_stmt|;
name|this
operator|.
name|lastNext
operator|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
expr_stmt|;
name|this
operator|.
name|connection
operator|=
name|connection
expr_stmt|;
if|if
condition|(
name|scan
operator|.
name|getMaxResultSize
argument_list|()
operator|>
literal|0
condition|)
block|{
name|this
operator|.
name|maxScannerResultSize
operator|=
name|scan
operator|.
name|getMaxResultSize
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|this
operator|.
name|maxScannerResultSize
operator|=
name|conf
operator|.
name|getLong
argument_list|(
name|HConstants
operator|.
name|HBASE_CLIENT_SCANNER_MAX_RESULT_SIZE_KEY
argument_list|,
name|HConstants
operator|.
name|DEFAULT_HBASE_CLIENT_SCANNER_MAX_RESULT_SIZE
argument_list|)
expr_stmt|;
block|}
name|this
operator|.
name|scannerTimeout
operator|=
name|HBaseConfiguration
operator|.
name|getInt
argument_list|(
name|conf
argument_list|,
name|HConstants
operator|.
name|HBASE_CLIENT_SCANNER_TIMEOUT_PERIOD
argument_list|,
name|HConstants
operator|.
name|HBASE_REGIONSERVER_LEASE_PERIOD_KEY
argument_list|,
name|HConstants
operator|.
name|DEFAULT_HBASE_CLIENT_SCANNER_TIMEOUT_PERIOD
argument_list|)
expr_stmt|;
comment|// check if application wants to collect scan metrics
name|byte
index|[]
name|enableMetrics
init|=
name|scan
operator|.
name|getAttribute
argument_list|(
name|Scan
operator|.
name|SCAN_ATTRIBUTES_METRICS_ENABLE
argument_list|)
decl_stmt|;
if|if
condition|(
name|enableMetrics
operator|!=
literal|null
operator|&&
name|Bytes
operator|.
name|toBoolean
argument_list|(
name|enableMetrics
argument_list|)
condition|)
block|{
name|scanMetrics
operator|=
operator|new
name|ScanMetrics
argument_list|()
expr_stmt|;
block|}
comment|// Use the caching from the Scan.  If not set, use the default cache setting for this table.
if|if
condition|(
name|this
operator|.
name|scan
operator|.
name|getCaching
argument_list|()
operator|>
literal|0
condition|)
block|{
name|this
operator|.
name|caching
operator|=
name|this
operator|.
name|scan
operator|.
name|getCaching
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|this
operator|.
name|caching
operator|=
name|conf
operator|.
name|getInt
argument_list|(
name|HConstants
operator|.
name|HBASE_CLIENT_SCANNER_CACHING
argument_list|,
name|HConstants
operator|.
name|DEFAULT_HBASE_CLIENT_SCANNER_CACHING
argument_list|)
expr_stmt|;
block|}
name|this
operator|.
name|caller
operator|=
name|rpcFactory
operator|.
expr|<
name|Result
index|[]
operator|>
name|newCaller
argument_list|()
expr_stmt|;
comment|// initialize the scanner
name|nextScanner
argument_list|(
name|this
operator|.
name|caching
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
specifier|protected
name|HConnection
name|getConnection
parameter_list|()
block|{
return|return
name|this
operator|.
name|connection
return|;
block|}
specifier|protected
name|TableName
name|getTableName
parameter_list|()
block|{
return|return
name|this
operator|.
name|tableName
return|;
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
specifier|protected
name|long
name|getTimestamp
parameter_list|()
block|{
return|return
name|lastNext
return|;
block|}
comment|// returns true if the passed region endKey
specifier|private
name|boolean
name|checkScanStopRow
parameter_list|(
specifier|final
name|byte
index|[]
name|endKey
parameter_list|)
block|{
if|if
condition|(
name|this
operator|.
name|scan
operator|.
name|getStopRow
argument_list|()
operator|.
name|length
operator|>
literal|0
condition|)
block|{
comment|// there is a stop row, check to see if we are past it.
name|byte
index|[]
name|stopRow
init|=
name|scan
operator|.
name|getStopRow
argument_list|()
decl_stmt|;
name|int
name|cmp
init|=
name|Bytes
operator|.
name|compareTo
argument_list|(
name|stopRow
argument_list|,
literal|0
argument_list|,
name|stopRow
operator|.
name|length
argument_list|,
name|endKey
argument_list|,
literal|0
argument_list|,
name|endKey
operator|.
name|length
argument_list|)
decl_stmt|;
if|if
condition|(
name|cmp
operator|<=
literal|0
condition|)
block|{
comment|// stopRow<= endKey (endKey is equals to or larger than stopRow)
comment|// This is a stop.
return|return
literal|true
return|;
block|}
block|}
return|return
literal|false
return|;
comment|//unlikely.
block|}
comment|/*      * Gets a scanner for the next region.  If this.currentRegion != null, then      * we will move to the endrow of this.currentRegion.  Else we will get      * scanner at the scan.getStartRow().  We will go no further, just tidy      * up outstanding scanners, if<code>currentRegion != null</code> and      *<code>done</code> is true.      * @param nbRows      * @param done Server-side says we're done scanning.      */
specifier|private
name|boolean
name|nextScanner
parameter_list|(
name|int
name|nbRows
parameter_list|,
specifier|final
name|boolean
name|done
parameter_list|)
throws|throws
name|IOException
block|{
comment|// Close the previous scanner if it's open
if|if
condition|(
name|this
operator|.
name|callable
operator|!=
literal|null
condition|)
block|{
name|this
operator|.
name|callable
operator|.
name|setClose
argument_list|()
expr_stmt|;
name|this
operator|.
name|caller
operator|.
name|callWithRetries
argument_list|(
name|callable
argument_list|)
expr_stmt|;
name|this
operator|.
name|callable
operator|=
literal|null
expr_stmt|;
block|}
comment|// Where to start the next scanner
name|byte
index|[]
name|localStartKey
decl_stmt|;
comment|// if we're at end of table, close and return false to stop iterating
if|if
condition|(
name|this
operator|.
name|currentRegion
operator|!=
literal|null
condition|)
block|{
name|byte
index|[]
name|endKey
init|=
name|this
operator|.
name|currentRegion
operator|.
name|getEndKey
argument_list|()
decl_stmt|;
if|if
condition|(
name|endKey
operator|==
literal|null
operator|||
name|Bytes
operator|.
name|equals
argument_list|(
name|endKey
argument_list|,
name|HConstants
operator|.
name|EMPTY_BYTE_ARRAY
argument_list|)
operator|||
name|checkScanStopRow
argument_list|(
name|endKey
argument_list|)
operator|||
name|done
condition|)
block|{
name|close
argument_list|()
expr_stmt|;
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
literal|"Finished "
operator|+
name|this
operator|.
name|currentRegion
argument_list|)
expr_stmt|;
block|}
return|return
literal|false
return|;
block|}
name|localStartKey
operator|=
name|endKey
expr_stmt|;
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
literal|"Finished "
operator|+
name|this
operator|.
name|currentRegion
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|localStartKey
operator|=
name|this
operator|.
name|scan
operator|.
name|getStartRow
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
operator|&&
name|this
operator|.
name|currentRegion
operator|!=
literal|null
condition|)
block|{
comment|// Only worth logging if NOT first region in scan.
name|LOG
operator|.
name|debug
argument_list|(
literal|"Advancing internal scanner to startKey at '"
operator|+
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|localStartKey
argument_list|)
operator|+
literal|"'"
argument_list|)
expr_stmt|;
block|}
try|try
block|{
name|callable
operator|=
name|getScannerCallable
argument_list|(
name|localStartKey
argument_list|,
name|nbRows
argument_list|)
expr_stmt|;
comment|// Open a scanner on the region server starting at the
comment|// beginning of the region
name|this
operator|.
name|caller
operator|.
name|callWithRetries
argument_list|(
name|callable
argument_list|)
expr_stmt|;
name|this
operator|.
name|currentRegion
operator|=
name|callable
operator|.
name|getHRegionInfo
argument_list|()
expr_stmt|;
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
name|countOfRegions
operator|.
name|incrementAndGet
argument_list|()
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|close
argument_list|()
expr_stmt|;
throw|throw
name|e
throw|;
block|}
return|return
literal|true
return|;
block|}
specifier|protected
name|ScannerCallable
name|getScannerCallable
parameter_list|(
name|byte
index|[]
name|localStartKey
parameter_list|,
name|int
name|nbRows
parameter_list|)
block|{
name|scan
operator|.
name|setStartRow
argument_list|(
name|localStartKey
argument_list|)
expr_stmt|;
name|ScannerCallable
name|s
init|=
operator|new
name|ScannerCallable
argument_list|(
name|getConnection
argument_list|()
argument_list|,
name|getTableName
argument_list|()
argument_list|,
name|scan
argument_list|,
name|this
operator|.
name|scanMetrics
argument_list|)
decl_stmt|;
name|s
operator|.
name|setCaching
argument_list|(
name|nbRows
argument_list|)
expr_stmt|;
return|return
name|s
return|;
block|}
comment|/**      * Publish the scan metrics. For now, we use scan.setAttribute to pass the metrics back to the      * application or TableInputFormat.Later, we could push it to other systems. We don't use metrics      * framework because it doesn't support multi-instances of the same metrics on the same machine;      * for scan/map reduce scenarios, we will have multiple scans running at the same time.      *      * By default, scan metrics are disabled; if the application wants to collect them, this behavior      * can be turned on by calling calling:      *      * scan.setAttribute(SCAN_ATTRIBUTES_METRICS_ENABLE, Bytes.toBytes(Boolean.TRUE))      */
specifier|private
name|void
name|writeScanMetrics
parameter_list|()
block|{
if|if
condition|(
name|this
operator|.
name|scanMetrics
operator|==
literal|null
operator|||
name|scanMetricsPublished
condition|)
block|{
return|return;
block|}
name|MapReduceProtos
operator|.
name|ScanMetrics
name|pScanMetrics
init|=
name|ProtobufUtil
operator|.
name|toScanMetrics
argument_list|(
name|scanMetrics
argument_list|)
decl_stmt|;
name|scan
operator|.
name|setAttribute
argument_list|(
name|Scan
operator|.
name|SCAN_ATTRIBUTES_METRICS_DATA
argument_list|,
name|pScanMetrics
operator|.
name|toByteArray
argument_list|()
argument_list|)
expr_stmt|;
name|scanMetricsPublished
operator|=
literal|true
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Result
name|next
parameter_list|()
throws|throws
name|IOException
block|{
comment|// If the scanner is closed and there's nothing left in the cache, next is a no-op.
if|if
condition|(
name|cache
operator|.
name|size
argument_list|()
operator|==
literal|0
operator|&&
name|this
operator|.
name|closed
condition|)
block|{
return|return
literal|null
return|;
block|}
if|if
condition|(
name|cache
operator|.
name|size
argument_list|()
operator|==
literal|0
condition|)
block|{
name|Result
index|[]
name|values
init|=
literal|null
decl_stmt|;
name|long
name|remainingResultSize
init|=
name|maxScannerResultSize
decl_stmt|;
name|int
name|countdown
init|=
name|this
operator|.
name|caching
decl_stmt|;
comment|// We need to reset it if it's a new callable that was created
comment|// with a countdown in nextScanner
name|callable
operator|.
name|setCaching
argument_list|(
name|this
operator|.
name|caching
argument_list|)
expr_stmt|;
comment|// This flag is set when we want to skip the result returned.  We do
comment|// this when we reset scanner because it split under us.
name|boolean
name|skipFirst
init|=
literal|false
decl_stmt|;
name|boolean
name|retryAfterOutOfOrderException
init|=
literal|true
decl_stmt|;
do|do
block|{
try|try
block|{
if|if
condition|(
name|skipFirst
condition|)
block|{
comment|// Skip only the first row (which was the last row of the last
comment|// already-processed batch).
name|callable
operator|.
name|setCaching
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|values
operator|=
name|this
operator|.
name|caller
operator|.
name|callWithRetries
argument_list|(
name|callable
argument_list|)
expr_stmt|;
name|callable
operator|.
name|setCaching
argument_list|(
name|this
operator|.
name|caching
argument_list|)
expr_stmt|;
name|skipFirst
operator|=
literal|false
expr_stmt|;
block|}
comment|// Server returns a null values if scanning is to stop.  Else,
comment|// returns an empty array if scanning is to go on and we've just
comment|// exhausted current region.
name|values
operator|=
name|this
operator|.
name|caller
operator|.
name|callWithRetries
argument_list|(
name|callable
argument_list|)
expr_stmt|;
if|if
condition|(
name|skipFirst
operator|&&
name|values
operator|!=
literal|null
operator|&&
name|values
operator|.
name|length
operator|==
literal|1
condition|)
block|{
name|skipFirst
operator|=
literal|false
expr_stmt|;
comment|// Already skipped, unset it before scanning again
name|values
operator|=
name|this
operator|.
name|caller
operator|.
name|callWithRetries
argument_list|(
name|callable
argument_list|)
expr_stmt|;
block|}
name|retryAfterOutOfOrderException
operator|=
literal|true
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|DoNotRetryIOException
name|e
parameter_list|)
block|{
comment|// DNRIOEs are thrown to make us break out of retries.  Some types of DNRIOEs want us
comment|// to reset the scanner and come back in again.
if|if
condition|(
name|e
operator|instanceof
name|UnknownScannerException
condition|)
block|{
name|long
name|timeout
init|=
name|lastNext
operator|+
name|scannerTimeout
decl_stmt|;
comment|// If we are over the timeout, throw this exception to the client wrapped in
comment|// a ScannerTimeoutException. Else, it's because the region moved and we used the old
comment|// id against the new region server; reset the scanner.
if|if
condition|(
name|timeout
operator|<
name|System
operator|.
name|currentTimeMillis
argument_list|()
condition|)
block|{
name|long
name|elapsed
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|-
name|lastNext
decl_stmt|;
name|ScannerTimeoutException
name|ex
init|=
operator|new
name|ScannerTimeoutException
argument_list|(
name|elapsed
operator|+
literal|"ms passed since the last invocation, "
operator|+
literal|"timeout is currently set to "
operator|+
name|scannerTimeout
argument_list|)
decl_stmt|;
name|ex
operator|.
name|initCause
argument_list|(
name|e
argument_list|)
expr_stmt|;
throw|throw
name|ex
throw|;
block|}
block|}
else|else
block|{
comment|// If exception is any but the list below throw it back to the client; else setup
comment|// the scanner and retry.
name|Throwable
name|cause
init|=
name|e
operator|.
name|getCause
argument_list|()
decl_stmt|;
if|if
condition|(
operator|(
name|cause
operator|!=
literal|null
operator|&&
name|cause
operator|instanceof
name|NotServingRegionException
operator|)
operator|||
operator|(
name|cause
operator|!=
literal|null
operator|&&
name|cause
operator|instanceof
name|RegionServerStoppedException
operator|)
operator|||
name|e
operator|instanceof
name|OutOfOrderScannerNextException
condition|)
block|{
comment|// Pass
comment|// It is easier writing the if loop test as list of what is allowed rather than
comment|// as a list of what is not allowed... so if in here, it means we do not throw.
block|}
else|else
block|{
throw|throw
name|e
throw|;
block|}
block|}
comment|// Else, its signal from depths of ScannerCallable that we need to reset the scanner.
if|if
condition|(
name|this
operator|.
name|lastResult
operator|!=
literal|null
condition|)
block|{
name|this
operator|.
name|scan
operator|.
name|setStartRow
argument_list|(
name|this
operator|.
name|lastResult
operator|.
name|getRow
argument_list|()
argument_list|)
expr_stmt|;
comment|// Skip first row returned.  We already let it out on previous
comment|// invocation.
name|skipFirst
operator|=
literal|true
expr_stmt|;
block|}
if|if
condition|(
name|e
operator|instanceof
name|OutOfOrderScannerNextException
condition|)
block|{
if|if
condition|(
name|retryAfterOutOfOrderException
condition|)
block|{
name|retryAfterOutOfOrderException
operator|=
literal|false
expr_stmt|;
block|}
else|else
block|{
comment|// TODO: Why wrap this in a DNRIOE when it already is a DNRIOE?
throw|throw
operator|new
name|DoNotRetryIOException
argument_list|(
literal|"Failed after retry of "
operator|+
literal|"OutOfOrderScannerNextException: was there a rpc timeout?"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
comment|// Clear region.
name|this
operator|.
name|currentRegion
operator|=
literal|null
expr_stmt|;
comment|// Set this to zero so we don't try and do an rpc and close on remote server when
comment|// the exception we got was UnknownScanner or the Server is going down.
name|callable
operator|=
literal|null
expr_stmt|;
comment|// This continue will take us to while at end of loop where we will set up new scanner.
continue|continue;
block|}
name|long
name|currentTime
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
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
name|sumOfMillisSecBetweenNexts
operator|.
name|addAndGet
argument_list|(
name|currentTime
operator|-
name|lastNext
argument_list|)
expr_stmt|;
block|}
name|lastNext
operator|=
name|currentTime
expr_stmt|;
if|if
condition|(
name|values
operator|!=
literal|null
operator|&&
name|values
operator|.
name|length
operator|>
literal|0
condition|)
block|{
for|for
control|(
name|Result
name|rs
range|:
name|values
control|)
block|{
name|cache
operator|.
name|add
argument_list|(
name|rs
argument_list|)
expr_stmt|;
for|for
control|(
name|Cell
name|kv
range|:
name|rs
operator|.
name|raw
argument_list|()
control|)
block|{
comment|// TODO make method in Cell or CellUtil
name|remainingResultSize
operator|-=
name|KeyValueUtil
operator|.
name|ensureKeyValue
argument_list|(
name|kv
argument_list|)
operator|.
name|heapSize
argument_list|()
expr_stmt|;
block|}
name|countdown
operator|--
expr_stmt|;
name|this
operator|.
name|lastResult
operator|=
name|rs
expr_stmt|;
block|}
block|}
comment|// Values == null means server-side filter has determined we must STOP
block|}
do|while
condition|(
name|remainingResultSize
operator|>
literal|0
operator|&&
name|countdown
operator|>
literal|0
operator|&&
name|nextScanner
argument_list|(
name|countdown
argument_list|,
name|values
operator|==
literal|null
argument_list|)
condition|)
do|;
block|}
if|if
condition|(
name|cache
operator|.
name|size
argument_list|()
operator|>
literal|0
condition|)
block|{
return|return
name|cache
operator|.
name|poll
argument_list|()
return|;
block|}
comment|// if we exhausted this scanner before calling close, write out the scan metrics
name|writeScanMetrics
argument_list|()
expr_stmt|;
return|return
literal|null
return|;
block|}
comment|/**      * Get<param>nbRows</param> rows.      * How many RPCs are made is determined by the {@link Scan#setCaching(int)}      * setting (or hbase.client.scanner.caching in hbase-site.xml).      * @param nbRows number of rows to return      * @return Between zero and<param>nbRows</param> RowResults.  Scan is done      * if returned array is of zero-length (We never return null).      * @throws IOException      */
annotation|@
name|Override
specifier|public
name|Result
index|[]
name|next
parameter_list|(
name|int
name|nbRows
parameter_list|)
throws|throws
name|IOException
block|{
comment|// Collect values to be returned here
name|ArrayList
argument_list|<
name|Result
argument_list|>
name|resultSets
init|=
operator|new
name|ArrayList
argument_list|<
name|Result
argument_list|>
argument_list|(
name|nbRows
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
name|nbRows
condition|;
name|i
operator|++
control|)
block|{
name|Result
name|next
init|=
name|next
argument_list|()
decl_stmt|;
if|if
condition|(
name|next
operator|!=
literal|null
condition|)
block|{
name|resultSets
operator|.
name|add
argument_list|(
name|next
argument_list|)
expr_stmt|;
block|}
else|else
block|{
break|break;
block|}
block|}
return|return
name|resultSets
operator|.
name|toArray
argument_list|(
operator|new
name|Result
index|[
name|resultSets
operator|.
name|size
argument_list|()
index|]
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|()
block|{
if|if
condition|(
operator|!
name|scanMetricsPublished
condition|)
name|writeScanMetrics
argument_list|()
expr_stmt|;
if|if
condition|(
name|callable
operator|!=
literal|null
condition|)
block|{
name|callable
operator|.
name|setClose
argument_list|()
expr_stmt|;
try|try
block|{
name|this
operator|.
name|caller
operator|.
name|callWithRetries
argument_list|(
name|callable
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
comment|// We used to catch this error, interpret, and rethrow. However, we
comment|// have since decided that it's not nice for a scanner's close to
comment|// throw exceptions. Chances are it was just an UnknownScanner
comment|// exception due to lease time out.
block|}
name|callable
operator|=
literal|null
expr_stmt|;
block|}
name|closed
operator|=
literal|true
expr_stmt|;
block|}
block|}
end_class

end_unit

