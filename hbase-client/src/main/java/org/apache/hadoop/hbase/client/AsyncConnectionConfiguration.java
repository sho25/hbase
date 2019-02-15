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
name|DEFAULT_HBASE_CLIENT_OPERATION_TIMEOUT
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
name|DEFAULT_HBASE_CLIENT_PAUSE
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
name|DEFAULT_HBASE_CLIENT_RETRIES_NUMBER
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
name|DEFAULT_HBASE_CLIENT_SCANNER_CACHING
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
name|DEFAULT_HBASE_CLIENT_SCANNER_MAX_RESULT_SIZE
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
name|DEFAULT_HBASE_CLIENT_SCANNER_TIMEOUT_PERIOD
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
name|DEFAULT_HBASE_META_SCANNER_CACHING
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
name|DEFAULT_HBASE_RPC_TIMEOUT
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
name|HBASE_CLIENT_META_OPERATION_TIMEOUT
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
name|HBASE_CLIENT_META_REPLICA_SCAN_TIMEOUT
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
name|HBASE_CLIENT_META_REPLICA_SCAN_TIMEOUT_DEFAULT
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
name|HBASE_CLIENT_OPERATION_TIMEOUT
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
name|HBASE_CLIENT_PAUSE
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
name|HBASE_CLIENT_RETRIES_NUMBER
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
name|HBASE_CLIENT_SCANNER_CACHING
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
name|HBASE_CLIENT_SCANNER_MAX_RESULT_SIZE_KEY
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
name|HBASE_CLIENT_SCANNER_TIMEOUT_PERIOD
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
name|HBASE_META_SCANNER_CACHING
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
name|HBASE_RPC_READ_TIMEOUT_KEY
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
name|HBASE_RPC_TIMEOUT_KEY
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
name|HBASE_RPC_WRITE_TIMEOUT_KEY
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
name|AsyncProcess
operator|.
name|DEFAULT_START_LOG_ERRORS_AFTER_COUNT
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
name|AsyncProcess
operator|.
name|START_LOG_ERRORS_AFTER_COUNT_KEY
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
name|ConnectionConfiguration
operator|.
name|MAX_KEYVALUE_SIZE_DEFAULT
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
name|ConnectionConfiguration
operator|.
name|MAX_KEYVALUE_SIZE_KEY
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
name|ConnectionConfiguration
operator|.
name|PRIMARY_CALL_TIMEOUT_MICROSECOND
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
name|ConnectionConfiguration
operator|.
name|PRIMARY_CALL_TIMEOUT_MICROSECOND_DEFAULT
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
name|ConnectionConfiguration
operator|.
name|PRIMARY_SCAN_TIMEOUT_MICROSECOND
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
name|ConnectionConfiguration
operator|.
name|PRIMARY_SCAN_TIMEOUT_MICROSECOND_DEFAULT
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
name|ConnectionConfiguration
operator|.
name|WRITE_BUFFER_PERIODIC_FLUSH_TIMEOUT_MS
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
name|ConnectionConfiguration
operator|.
name|WRITE_BUFFER_PERIODIC_FLUSH_TIMEOUT_MS_DEFAULT
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
name|ConnectionConfiguration
operator|.
name|WRITE_BUFFER_SIZE_DEFAULT
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
name|ConnectionConfiguration
operator|.
name|WRITE_BUFFER_SIZE_KEY
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
name|yetus
operator|.
name|audience
operator|.
name|InterfaceAudience
import|;
end_import

begin_comment
comment|/**  * Timeout configs.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
class|class
name|AsyncConnectionConfiguration
block|{
specifier|private
specifier|final
name|long
name|metaOperationTimeoutNs
decl_stmt|;
comment|// timeout for a whole operation such as get, put or delete. Notice that scan will not be effected
comment|// by this value, see scanTimeoutNs.
specifier|private
specifier|final
name|long
name|operationTimeoutNs
decl_stmt|;
comment|// timeout for each rpc request. Can be overridden by a more specific config, such as
comment|// readRpcTimeout or writeRpcTimeout.
specifier|private
specifier|final
name|long
name|rpcTimeoutNs
decl_stmt|;
comment|// timeout for each read rpc request
specifier|private
specifier|final
name|long
name|readRpcTimeoutNs
decl_stmt|;
comment|// timeout for each write rpc request
specifier|private
specifier|final
name|long
name|writeRpcTimeoutNs
decl_stmt|;
specifier|private
specifier|final
name|long
name|pauseNs
decl_stmt|;
specifier|private
specifier|final
name|int
name|maxRetries
decl_stmt|;
comment|/** How many retries are allowed before we start to log */
specifier|private
specifier|final
name|int
name|startLogErrorsCnt
decl_stmt|;
comment|// As now we have heartbeat support for scan, ideally a scan will never timeout unless the RS is
comment|// crash. The RS will always return something before the rpc timeout or scan timeout to tell the
comment|// client that it is still alive. The scan timeout is used as operation timeout for every
comment|// operations in a scan, such as openScanner or next.
specifier|private
specifier|final
name|long
name|scanTimeoutNs
decl_stmt|;
specifier|private
specifier|final
name|int
name|scannerCaching
decl_stmt|;
specifier|private
specifier|final
name|int
name|metaScannerCaching
decl_stmt|;
specifier|private
specifier|final
name|long
name|scannerMaxResultSize
decl_stmt|;
specifier|private
specifier|final
name|long
name|writeBufferSize
decl_stmt|;
specifier|private
specifier|final
name|long
name|writeBufferPeriodicFlushTimeoutNs
decl_stmt|;
comment|// this is for supporting region replica get, if the primary does not finished within this
comment|// timeout, we will send request to secondaries.
specifier|private
specifier|final
name|long
name|primaryCallTimeoutNs
decl_stmt|;
specifier|private
specifier|final
name|long
name|primaryScanTimeoutNs
decl_stmt|;
specifier|private
specifier|final
name|long
name|primaryMetaScanTimeoutNs
decl_stmt|;
specifier|private
specifier|final
name|int
name|maxKeyValueSize
decl_stmt|;
name|AsyncConnectionConfiguration
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
name|this
operator|.
name|metaOperationTimeoutNs
operator|=
name|TimeUnit
operator|.
name|MILLISECONDS
operator|.
name|toNanos
argument_list|(
name|conf
operator|.
name|getLong
argument_list|(
name|HBASE_CLIENT_META_OPERATION_TIMEOUT
argument_list|,
name|DEFAULT_HBASE_CLIENT_OPERATION_TIMEOUT
argument_list|)
argument_list|)
expr_stmt|;
name|this
operator|.
name|operationTimeoutNs
operator|=
name|TimeUnit
operator|.
name|MILLISECONDS
operator|.
name|toNanos
argument_list|(
name|conf
operator|.
name|getLong
argument_list|(
name|HBASE_CLIENT_OPERATION_TIMEOUT
argument_list|,
name|DEFAULT_HBASE_CLIENT_OPERATION_TIMEOUT
argument_list|)
argument_list|)
expr_stmt|;
name|this
operator|.
name|rpcTimeoutNs
operator|=
name|TimeUnit
operator|.
name|MILLISECONDS
operator|.
name|toNanos
argument_list|(
name|conf
operator|.
name|getLong
argument_list|(
name|HBASE_RPC_TIMEOUT_KEY
argument_list|,
name|DEFAULT_HBASE_RPC_TIMEOUT
argument_list|)
argument_list|)
expr_stmt|;
name|this
operator|.
name|readRpcTimeoutNs
operator|=
name|TimeUnit
operator|.
name|MILLISECONDS
operator|.
name|toNanos
argument_list|(
name|conf
operator|.
name|getLong
argument_list|(
name|HBASE_RPC_READ_TIMEOUT_KEY
argument_list|,
name|rpcTimeoutNs
argument_list|)
argument_list|)
expr_stmt|;
name|this
operator|.
name|writeRpcTimeoutNs
operator|=
name|TimeUnit
operator|.
name|MILLISECONDS
operator|.
name|toNanos
argument_list|(
name|conf
operator|.
name|getLong
argument_list|(
name|HBASE_RPC_WRITE_TIMEOUT_KEY
argument_list|,
name|rpcTimeoutNs
argument_list|)
argument_list|)
expr_stmt|;
name|this
operator|.
name|pauseNs
operator|=
name|TimeUnit
operator|.
name|MILLISECONDS
operator|.
name|toNanos
argument_list|(
name|conf
operator|.
name|getLong
argument_list|(
name|HBASE_CLIENT_PAUSE
argument_list|,
name|DEFAULT_HBASE_CLIENT_PAUSE
argument_list|)
argument_list|)
expr_stmt|;
name|this
operator|.
name|maxRetries
operator|=
name|conf
operator|.
name|getInt
argument_list|(
name|HBASE_CLIENT_RETRIES_NUMBER
argument_list|,
name|DEFAULT_HBASE_CLIENT_RETRIES_NUMBER
argument_list|)
expr_stmt|;
name|this
operator|.
name|startLogErrorsCnt
operator|=
name|conf
operator|.
name|getInt
argument_list|(
name|START_LOG_ERRORS_AFTER_COUNT_KEY
argument_list|,
name|DEFAULT_START_LOG_ERRORS_AFTER_COUNT
argument_list|)
expr_stmt|;
name|this
operator|.
name|scanTimeoutNs
operator|=
name|TimeUnit
operator|.
name|MILLISECONDS
operator|.
name|toNanos
argument_list|(
name|conf
operator|.
name|getInt
argument_list|(
name|HBASE_CLIENT_SCANNER_TIMEOUT_PERIOD
argument_list|,
name|DEFAULT_HBASE_CLIENT_SCANNER_TIMEOUT_PERIOD
argument_list|)
argument_list|)
expr_stmt|;
name|this
operator|.
name|scannerCaching
operator|=
name|conf
operator|.
name|getInt
argument_list|(
name|HBASE_CLIENT_SCANNER_CACHING
argument_list|,
name|DEFAULT_HBASE_CLIENT_SCANNER_CACHING
argument_list|)
expr_stmt|;
name|this
operator|.
name|metaScannerCaching
operator|=
name|conf
operator|.
name|getInt
argument_list|(
name|HBASE_META_SCANNER_CACHING
argument_list|,
name|DEFAULT_HBASE_META_SCANNER_CACHING
argument_list|)
expr_stmt|;
name|this
operator|.
name|scannerMaxResultSize
operator|=
name|conf
operator|.
name|getLong
argument_list|(
name|HBASE_CLIENT_SCANNER_MAX_RESULT_SIZE_KEY
argument_list|,
name|DEFAULT_HBASE_CLIENT_SCANNER_MAX_RESULT_SIZE
argument_list|)
expr_stmt|;
name|this
operator|.
name|writeBufferSize
operator|=
name|conf
operator|.
name|getLong
argument_list|(
name|WRITE_BUFFER_SIZE_KEY
argument_list|,
name|WRITE_BUFFER_SIZE_DEFAULT
argument_list|)
expr_stmt|;
name|this
operator|.
name|writeBufferPeriodicFlushTimeoutNs
operator|=
name|TimeUnit
operator|.
name|MILLISECONDS
operator|.
name|toNanos
argument_list|(
name|conf
operator|.
name|getLong
argument_list|(
name|WRITE_BUFFER_PERIODIC_FLUSH_TIMEOUT_MS
argument_list|,
name|WRITE_BUFFER_PERIODIC_FLUSH_TIMEOUT_MS_DEFAULT
argument_list|)
argument_list|)
expr_stmt|;
name|this
operator|.
name|primaryCallTimeoutNs
operator|=
name|TimeUnit
operator|.
name|MICROSECONDS
operator|.
name|toNanos
argument_list|(
name|conf
operator|.
name|getLong
argument_list|(
name|PRIMARY_CALL_TIMEOUT_MICROSECOND
argument_list|,
name|PRIMARY_CALL_TIMEOUT_MICROSECOND_DEFAULT
argument_list|)
argument_list|)
expr_stmt|;
name|this
operator|.
name|primaryScanTimeoutNs
operator|=
name|TimeUnit
operator|.
name|MICROSECONDS
operator|.
name|toNanos
argument_list|(
name|conf
operator|.
name|getLong
argument_list|(
name|PRIMARY_SCAN_TIMEOUT_MICROSECOND
argument_list|,
name|PRIMARY_SCAN_TIMEOUT_MICROSECOND_DEFAULT
argument_list|)
argument_list|)
expr_stmt|;
name|this
operator|.
name|primaryMetaScanTimeoutNs
operator|=
name|TimeUnit
operator|.
name|MICROSECONDS
operator|.
name|toNanos
argument_list|(
name|conf
operator|.
name|getLong
argument_list|(
name|HBASE_CLIENT_META_REPLICA_SCAN_TIMEOUT
argument_list|,
name|HBASE_CLIENT_META_REPLICA_SCAN_TIMEOUT_DEFAULT
argument_list|)
argument_list|)
expr_stmt|;
name|this
operator|.
name|maxKeyValueSize
operator|=
name|conf
operator|.
name|getInt
argument_list|(
name|MAX_KEYVALUE_SIZE_KEY
argument_list|,
name|MAX_KEYVALUE_SIZE_DEFAULT
argument_list|)
expr_stmt|;
block|}
name|long
name|getMetaOperationTimeoutNs
parameter_list|()
block|{
return|return
name|metaOperationTimeoutNs
return|;
block|}
name|long
name|getOperationTimeoutNs
parameter_list|()
block|{
return|return
name|operationTimeoutNs
return|;
block|}
name|long
name|getRpcTimeoutNs
parameter_list|()
block|{
return|return
name|rpcTimeoutNs
return|;
block|}
name|long
name|getReadRpcTimeoutNs
parameter_list|()
block|{
return|return
name|readRpcTimeoutNs
return|;
block|}
name|long
name|getWriteRpcTimeoutNs
parameter_list|()
block|{
return|return
name|writeRpcTimeoutNs
return|;
block|}
name|long
name|getPauseNs
parameter_list|()
block|{
return|return
name|pauseNs
return|;
block|}
name|int
name|getMaxRetries
parameter_list|()
block|{
return|return
name|maxRetries
return|;
block|}
name|int
name|getStartLogErrorsCnt
parameter_list|()
block|{
return|return
name|startLogErrorsCnt
return|;
block|}
name|long
name|getScanTimeoutNs
parameter_list|()
block|{
return|return
name|scanTimeoutNs
return|;
block|}
name|int
name|getScannerCaching
parameter_list|()
block|{
return|return
name|scannerCaching
return|;
block|}
name|int
name|getMetaScannerCaching
parameter_list|()
block|{
return|return
name|metaScannerCaching
return|;
block|}
name|long
name|getScannerMaxResultSize
parameter_list|()
block|{
return|return
name|scannerMaxResultSize
return|;
block|}
name|long
name|getWriteBufferSize
parameter_list|()
block|{
return|return
name|writeBufferSize
return|;
block|}
name|long
name|getWriteBufferPeriodicFlushTimeoutNs
parameter_list|()
block|{
return|return
name|writeBufferPeriodicFlushTimeoutNs
return|;
block|}
name|long
name|getPrimaryCallTimeoutNs
parameter_list|()
block|{
return|return
name|primaryCallTimeoutNs
return|;
block|}
name|long
name|getPrimaryScanTimeoutNs
parameter_list|()
block|{
return|return
name|primaryScanTimeoutNs
return|;
block|}
name|long
name|getPrimaryMetaScanTimeoutNs
parameter_list|()
block|{
return|return
name|primaryMetaScanTimeoutNs
return|;
block|}
name|int
name|getMaxKeyValueSize
parameter_list|()
block|{
return|return
name|maxKeyValueSize
return|;
block|}
block|}
end_class

end_unit

