begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one or more contributor license  * agreements. See the NOTICE file distributed with this work for additional information regarding  * copyright ownership. The ASF licenses this file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance with the License. You may obtain a  * copy of the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable  * law or agreed to in writing, software distributed under the License is distributed on an "AS IS"  * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License  * for the specific language governing permissions and limitations under the License.  */
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
name|HConstants
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
name|apache
operator|.
name|hbase
operator|.
name|thirdparty
operator|.
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|annotations
operator|.
name|VisibleForTesting
import|;
end_import

begin_comment
comment|/**  * Configuration parameters for the connection.  * Configuration is a heavy weight registry that does a lot of string operations and regex matching.  * Method calls into Configuration account for high CPU usage and have huge performance impact.  * This class caches connection-related configuration values in the  ConnectionConfiguration  * object so that expensive conf.getXXX() calls are avoided every time HTable, etc is instantiated.  * see HBASE-12128  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|ConnectionConfiguration
block|{
specifier|public
specifier|static
specifier|final
name|String
name|WRITE_BUFFER_SIZE_KEY
init|=
literal|"hbase.client.write.buffer"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|long
name|WRITE_BUFFER_SIZE_DEFAULT
init|=
literal|2097152
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|WRITE_BUFFER_PERIODIC_FLUSH_TIMEOUT_MS
init|=
literal|"hbase.client.write.buffer.periodicflush.timeout.ms"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|WRITE_BUFFER_PERIODIC_FLUSH_TIMERTICK_MS
init|=
literal|"hbase.client.write.buffer.periodicflush.timertick.ms"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|long
name|WRITE_BUFFER_PERIODIC_FLUSH_TIMEOUT_MS_DEFAULT
init|=
literal|0
decl_stmt|;
comment|// 0 == Disabled
specifier|public
specifier|static
specifier|final
name|long
name|WRITE_BUFFER_PERIODIC_FLUSH_TIMERTICK_MS_DEFAULT
init|=
literal|1000L
decl_stmt|;
comment|// 1 second
specifier|public
specifier|static
specifier|final
name|String
name|MAX_KEYVALUE_SIZE_KEY
init|=
literal|"hbase.client.keyvalue.maxsize"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|int
name|MAX_KEYVALUE_SIZE_DEFAULT
init|=
literal|10485760
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|PRIMARY_CALL_TIMEOUT_MICROSECOND
init|=
literal|"hbase.client.primaryCallTimeout.get"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|int
name|PRIMARY_CALL_TIMEOUT_MICROSECOND_DEFAULT
init|=
literal|10000
decl_stmt|;
comment|// 10ms
specifier|public
specifier|static
specifier|final
name|String
name|PRIMARY_SCAN_TIMEOUT_MICROSECOND
init|=
literal|"hbase.client.replicaCallTimeout.scan"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|int
name|PRIMARY_SCAN_TIMEOUT_MICROSECOND_DEFAULT
init|=
literal|1000000
decl_stmt|;
comment|// 1s
specifier|private
specifier|final
name|long
name|writeBufferSize
decl_stmt|;
specifier|private
specifier|final
name|long
name|writeBufferPeriodicFlushTimeoutMs
decl_stmt|;
specifier|private
specifier|final
name|long
name|writeBufferPeriodicFlushTimerTickMs
decl_stmt|;
specifier|private
specifier|final
name|int
name|metaOperationTimeout
decl_stmt|;
specifier|private
specifier|final
name|int
name|operationTimeout
decl_stmt|;
specifier|private
specifier|final
name|int
name|scannerCaching
decl_stmt|;
specifier|private
specifier|final
name|long
name|scannerMaxResultSize
decl_stmt|;
specifier|private
specifier|final
name|int
name|primaryCallTimeoutMicroSecond
decl_stmt|;
specifier|private
specifier|final
name|int
name|replicaCallTimeoutMicroSecondScan
decl_stmt|;
specifier|private
specifier|final
name|int
name|metaReplicaCallTimeoutMicroSecondScan
decl_stmt|;
specifier|private
specifier|final
name|int
name|retries
decl_stmt|;
specifier|private
specifier|final
name|int
name|maxKeyValueSize
decl_stmt|;
specifier|private
specifier|final
name|int
name|rpcTimeout
decl_stmt|;
specifier|private
specifier|final
name|int
name|readRpcTimeout
decl_stmt|;
specifier|private
specifier|final
name|int
name|writeRpcTimeout
decl_stmt|;
comment|// toggle for async/sync prefetch
specifier|private
specifier|final
name|boolean
name|clientScannerAsyncPrefetch
decl_stmt|;
comment|/**    * Constructor    * @param conf Configuration object    */
name|ConnectionConfiguration
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
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
name|writeBufferPeriodicFlushTimeoutMs
operator|=
name|conf
operator|.
name|getLong
argument_list|(
name|WRITE_BUFFER_PERIODIC_FLUSH_TIMEOUT_MS
argument_list|,
name|WRITE_BUFFER_PERIODIC_FLUSH_TIMEOUT_MS_DEFAULT
argument_list|)
expr_stmt|;
name|this
operator|.
name|writeBufferPeriodicFlushTimerTickMs
operator|=
name|conf
operator|.
name|getLong
argument_list|(
name|WRITE_BUFFER_PERIODIC_FLUSH_TIMERTICK_MS
argument_list|,
name|WRITE_BUFFER_PERIODIC_FLUSH_TIMERTICK_MS_DEFAULT
argument_list|)
expr_stmt|;
name|this
operator|.
name|metaOperationTimeout
operator|=
name|conf
operator|.
name|getInt
argument_list|(
name|HConstants
operator|.
name|HBASE_CLIENT_META_OPERATION_TIMEOUT
argument_list|,
name|HConstants
operator|.
name|DEFAULT_HBASE_CLIENT_OPERATION_TIMEOUT
argument_list|)
expr_stmt|;
name|this
operator|.
name|operationTimeout
operator|=
name|conf
operator|.
name|getInt
argument_list|(
name|HConstants
operator|.
name|HBASE_CLIENT_OPERATION_TIMEOUT
argument_list|,
name|HConstants
operator|.
name|DEFAULT_HBASE_CLIENT_OPERATION_TIMEOUT
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
name|HConstants
operator|.
name|HBASE_CLIENT_SCANNER_CACHING
argument_list|,
name|HConstants
operator|.
name|DEFAULT_HBASE_CLIENT_SCANNER_CACHING
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
name|HConstants
operator|.
name|HBASE_CLIENT_SCANNER_MAX_RESULT_SIZE_KEY
argument_list|,
name|HConstants
operator|.
name|DEFAULT_HBASE_CLIENT_SCANNER_MAX_RESULT_SIZE
argument_list|)
expr_stmt|;
name|this
operator|.
name|primaryCallTimeoutMicroSecond
operator|=
name|conf
operator|.
name|getInt
argument_list|(
name|PRIMARY_CALL_TIMEOUT_MICROSECOND
argument_list|,
name|PRIMARY_CALL_TIMEOUT_MICROSECOND_DEFAULT
argument_list|)
expr_stmt|;
name|this
operator|.
name|replicaCallTimeoutMicroSecondScan
operator|=
name|conf
operator|.
name|getInt
argument_list|(
name|PRIMARY_SCAN_TIMEOUT_MICROSECOND
argument_list|,
name|PRIMARY_SCAN_TIMEOUT_MICROSECOND_DEFAULT
argument_list|)
expr_stmt|;
name|this
operator|.
name|metaReplicaCallTimeoutMicroSecondScan
operator|=
name|conf
operator|.
name|getInt
argument_list|(
name|HConstants
operator|.
name|HBASE_CLIENT_META_REPLICA_SCAN_TIMEOUT
argument_list|,
name|HConstants
operator|.
name|HBASE_CLIENT_META_REPLICA_SCAN_TIMEOUT_DEFAULT
argument_list|)
expr_stmt|;
name|this
operator|.
name|retries
operator|=
name|conf
operator|.
name|getInt
argument_list|(
name|HConstants
operator|.
name|HBASE_CLIENT_RETRIES_NUMBER
argument_list|,
name|HConstants
operator|.
name|DEFAULT_HBASE_CLIENT_RETRIES_NUMBER
argument_list|)
expr_stmt|;
name|this
operator|.
name|clientScannerAsyncPrefetch
operator|=
name|conf
operator|.
name|getBoolean
argument_list|(
name|Scan
operator|.
name|HBASE_CLIENT_SCANNER_ASYNC_PREFETCH
argument_list|,
name|Scan
operator|.
name|DEFAULT_HBASE_CLIENT_SCANNER_ASYNC_PREFETCH
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
name|this
operator|.
name|rpcTimeout
operator|=
name|conf
operator|.
name|getInt
argument_list|(
name|HConstants
operator|.
name|HBASE_RPC_TIMEOUT_KEY
argument_list|,
name|HConstants
operator|.
name|DEFAULT_HBASE_RPC_TIMEOUT
argument_list|)
expr_stmt|;
name|this
operator|.
name|readRpcTimeout
operator|=
name|conf
operator|.
name|getInt
argument_list|(
name|HConstants
operator|.
name|HBASE_RPC_READ_TIMEOUT_KEY
argument_list|,
name|conf
operator|.
name|getInt
argument_list|(
name|HConstants
operator|.
name|HBASE_RPC_TIMEOUT_KEY
argument_list|,
name|HConstants
operator|.
name|DEFAULT_HBASE_RPC_TIMEOUT
argument_list|)
argument_list|)
expr_stmt|;
name|this
operator|.
name|writeRpcTimeout
operator|=
name|conf
operator|.
name|getInt
argument_list|(
name|HConstants
operator|.
name|HBASE_RPC_WRITE_TIMEOUT_KEY
argument_list|,
name|conf
operator|.
name|getInt
argument_list|(
name|HConstants
operator|.
name|HBASE_RPC_TIMEOUT_KEY
argument_list|,
name|HConstants
operator|.
name|DEFAULT_HBASE_RPC_TIMEOUT
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * Constructor    * This is for internal testing purpose (using the default value).    * In real usage, we should read the configuration from the Configuration object.    */
annotation|@
name|VisibleForTesting
specifier|protected
name|ConnectionConfiguration
parameter_list|()
block|{
name|this
operator|.
name|writeBufferSize
operator|=
name|WRITE_BUFFER_SIZE_DEFAULT
expr_stmt|;
name|this
operator|.
name|writeBufferPeriodicFlushTimeoutMs
operator|=
name|WRITE_BUFFER_PERIODIC_FLUSH_TIMEOUT_MS_DEFAULT
expr_stmt|;
name|this
operator|.
name|writeBufferPeriodicFlushTimerTickMs
operator|=
name|WRITE_BUFFER_PERIODIC_FLUSH_TIMERTICK_MS_DEFAULT
expr_stmt|;
name|this
operator|.
name|metaOperationTimeout
operator|=
name|HConstants
operator|.
name|DEFAULT_HBASE_CLIENT_OPERATION_TIMEOUT
expr_stmt|;
name|this
operator|.
name|operationTimeout
operator|=
name|HConstants
operator|.
name|DEFAULT_HBASE_CLIENT_OPERATION_TIMEOUT
expr_stmt|;
name|this
operator|.
name|scannerCaching
operator|=
name|HConstants
operator|.
name|DEFAULT_HBASE_CLIENT_SCANNER_CACHING
expr_stmt|;
name|this
operator|.
name|scannerMaxResultSize
operator|=
name|HConstants
operator|.
name|DEFAULT_HBASE_CLIENT_SCANNER_MAX_RESULT_SIZE
expr_stmt|;
name|this
operator|.
name|primaryCallTimeoutMicroSecond
operator|=
literal|10000
expr_stmt|;
name|this
operator|.
name|replicaCallTimeoutMicroSecondScan
operator|=
literal|1000000
expr_stmt|;
name|this
operator|.
name|metaReplicaCallTimeoutMicroSecondScan
operator|=
name|HConstants
operator|.
name|HBASE_CLIENT_META_REPLICA_SCAN_TIMEOUT_DEFAULT
expr_stmt|;
name|this
operator|.
name|retries
operator|=
name|HConstants
operator|.
name|DEFAULT_HBASE_CLIENT_RETRIES_NUMBER
expr_stmt|;
name|this
operator|.
name|clientScannerAsyncPrefetch
operator|=
name|Scan
operator|.
name|DEFAULT_HBASE_CLIENT_SCANNER_ASYNC_PREFETCH
expr_stmt|;
name|this
operator|.
name|maxKeyValueSize
operator|=
name|MAX_KEYVALUE_SIZE_DEFAULT
expr_stmt|;
name|this
operator|.
name|readRpcTimeout
operator|=
name|HConstants
operator|.
name|DEFAULT_HBASE_RPC_TIMEOUT
expr_stmt|;
name|this
operator|.
name|writeRpcTimeout
operator|=
name|HConstants
operator|.
name|DEFAULT_HBASE_RPC_TIMEOUT
expr_stmt|;
name|this
operator|.
name|rpcTimeout
operator|=
name|HConstants
operator|.
name|DEFAULT_HBASE_RPC_TIMEOUT
expr_stmt|;
block|}
specifier|public
name|int
name|getReadRpcTimeout
parameter_list|()
block|{
return|return
name|readRpcTimeout
return|;
block|}
specifier|public
name|int
name|getWriteRpcTimeout
parameter_list|()
block|{
return|return
name|writeRpcTimeout
return|;
block|}
specifier|public
name|long
name|getWriteBufferSize
parameter_list|()
block|{
return|return
name|writeBufferSize
return|;
block|}
specifier|public
name|long
name|getWriteBufferPeriodicFlushTimeoutMs
parameter_list|()
block|{
return|return
name|writeBufferPeriodicFlushTimeoutMs
return|;
block|}
specifier|public
name|long
name|getWriteBufferPeriodicFlushTimerTickMs
parameter_list|()
block|{
return|return
name|writeBufferPeriodicFlushTimerTickMs
return|;
block|}
specifier|public
name|int
name|getMetaOperationTimeout
parameter_list|()
block|{
return|return
name|metaOperationTimeout
return|;
block|}
specifier|public
name|int
name|getOperationTimeout
parameter_list|()
block|{
return|return
name|operationTimeout
return|;
block|}
specifier|public
name|int
name|getScannerCaching
parameter_list|()
block|{
return|return
name|scannerCaching
return|;
block|}
specifier|public
name|int
name|getPrimaryCallTimeoutMicroSecond
parameter_list|()
block|{
return|return
name|primaryCallTimeoutMicroSecond
return|;
block|}
specifier|public
name|int
name|getReplicaCallTimeoutMicroSecondScan
parameter_list|()
block|{
return|return
name|replicaCallTimeoutMicroSecondScan
return|;
block|}
specifier|public
name|int
name|getMetaReplicaCallTimeoutMicroSecondScan
parameter_list|()
block|{
return|return
name|metaReplicaCallTimeoutMicroSecondScan
return|;
block|}
specifier|public
name|int
name|getRetriesNumber
parameter_list|()
block|{
return|return
name|retries
return|;
block|}
specifier|public
name|int
name|getMaxKeyValueSize
parameter_list|()
block|{
return|return
name|maxKeyValueSize
return|;
block|}
specifier|public
name|long
name|getScannerMaxResultSize
parameter_list|()
block|{
return|return
name|scannerMaxResultSize
return|;
block|}
specifier|public
name|boolean
name|isClientScannerAsyncPrefetch
parameter_list|()
block|{
return|return
name|clientScannerAsyncPrefetch
return|;
block|}
specifier|public
name|int
name|getRpcTimeout
parameter_list|()
block|{
return|return
name|rpcTimeout
return|;
block|}
block|}
end_class

end_unit

