begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|ipc
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
name|hbase
operator|.
name|metrics
operator|.
name|ExceptionTrackingSource
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

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
interface|interface
name|MetricsHBaseServerSource
extends|extends
name|ExceptionTrackingSource
block|{
name|String
name|AUTHORIZATION_SUCCESSES_NAME
init|=
literal|"authorizationSuccesses"
decl_stmt|;
name|String
name|AUTHORIZATION_SUCCESSES_DESC
init|=
literal|"Number of authorization successes."
decl_stmt|;
name|String
name|AUTHORIZATION_FAILURES_NAME
init|=
literal|"authorizationFailures"
decl_stmt|;
name|String
name|AUTHORIZATION_FAILURES_DESC
init|=
literal|"Number of authorization failures."
decl_stmt|;
name|String
name|AUTHENTICATION_SUCCESSES_NAME
init|=
literal|"authenticationSuccesses"
decl_stmt|;
name|String
name|AUTHENTICATION_SUCCESSES_DESC
init|=
literal|"Number of authentication successes."
decl_stmt|;
name|String
name|AUTHENTICATION_FAILURES_NAME
init|=
literal|"authenticationFailures"
decl_stmt|;
name|String
name|AUTHENTICATION_FAILURES_DESC
init|=
literal|"Number of authentication failures."
decl_stmt|;
name|String
name|AUTHENTICATION_FALLBACKS_NAME
init|=
literal|"authenticationFallbacks"
decl_stmt|;
name|String
name|AUTHENTICATION_FALLBACKS_DESC
init|=
literal|"Number of fallbacks to insecure authentication."
decl_stmt|;
name|String
name|SENT_BYTES_NAME
init|=
literal|"sentBytes"
decl_stmt|;
name|String
name|SENT_BYTES_DESC
init|=
literal|"Number of bytes sent."
decl_stmt|;
name|String
name|RECEIVED_BYTES_NAME
init|=
literal|"receivedBytes"
decl_stmt|;
name|String
name|RECEIVED_BYTES_DESC
init|=
literal|"Number of bytes received."
decl_stmt|;
name|String
name|REQUEST_SIZE_NAME
init|=
literal|"requestSize"
decl_stmt|;
name|String
name|REQUEST_SIZE_DESC
init|=
literal|"Request size in bytes."
decl_stmt|;
name|String
name|RESPONSE_SIZE_NAME
init|=
literal|"responseSize"
decl_stmt|;
name|String
name|RESPONSE_SIZE_DESC
init|=
literal|"Response size in bytes."
decl_stmt|;
name|String
name|QUEUE_CALL_TIME_NAME
init|=
literal|"queueCallTime"
decl_stmt|;
name|String
name|QUEUE_CALL_TIME_DESC
init|=
literal|"Queue Call Time."
decl_stmt|;
name|String
name|PROCESS_CALL_TIME_NAME
init|=
literal|"processCallTime"
decl_stmt|;
name|String
name|PROCESS_CALL_TIME_DESC
init|=
literal|"Processing call time."
decl_stmt|;
name|String
name|TOTAL_CALL_TIME_NAME
init|=
literal|"totalCallTime"
decl_stmt|;
name|String
name|TOTAL_CALL_TIME_DESC
init|=
literal|"Total call time, including both queued and processing time."
decl_stmt|;
name|String
name|QUEUE_SIZE_NAME
init|=
literal|"queueSize"
decl_stmt|;
name|String
name|QUEUE_SIZE_DESC
init|=
literal|"Number of bytes in the call queues; request has been read and "
operator|+
literal|"parsed and is waiting to run or is currently being executed."
decl_stmt|;
name|String
name|GENERAL_QUEUE_NAME
init|=
literal|"numCallsInGeneralQueue"
decl_stmt|;
name|String
name|GENERAL_QUEUE_DESC
init|=
literal|"Number of calls in the general call queue; "
operator|+
literal|"parsed requests waiting in scheduler to be executed"
decl_stmt|;
name|String
name|PRIORITY_QUEUE_NAME
init|=
literal|"numCallsInPriorityQueue"
decl_stmt|;
name|String
name|METAPRIORITY_QUEUE_NAME
init|=
literal|"numCallsInMetaPriorityQueue"
decl_stmt|;
name|String
name|REPLICATION_QUEUE_NAME
init|=
literal|"numCallsInReplicationQueue"
decl_stmt|;
name|String
name|REPLICATION_QUEUE_DESC
init|=
literal|"Number of calls in the replication call queue waiting to be run"
decl_stmt|;
name|String
name|PRIORITY_QUEUE_DESC
init|=
literal|"Number of calls in the priority call queue waiting to be run"
decl_stmt|;
name|String
name|METAPRIORITY_QUEUE_DESC
init|=
literal|"Number of calls in the priority call queue waiting to be run"
decl_stmt|;
name|String
name|WRITE_QUEUE_NAME
init|=
literal|"numCallsInWriteQueue"
decl_stmt|;
name|String
name|WRITE_QUEUE_DESC
init|=
literal|"Number of calls in the write call queue; "
operator|+
literal|"parsed requests waiting in scheduler to be executed"
decl_stmt|;
name|String
name|READ_QUEUE_NAME
init|=
literal|"numCallsInReadQueue"
decl_stmt|;
name|String
name|READ_QUEUE_DESC
init|=
literal|"Number of calls in the read call queue; "
operator|+
literal|"parsed requests waiting in scheduler to be executed"
decl_stmt|;
name|String
name|SCAN_QUEUE_NAME
init|=
literal|"numCallsInScanQueue"
decl_stmt|;
name|String
name|SCAN_QUEUE_DESC
init|=
literal|"Number of calls in the scan call queue; "
operator|+
literal|"parsed requests waiting in scheduler to be executed"
decl_stmt|;
name|String
name|NUM_OPEN_CONNECTIONS_NAME
init|=
literal|"numOpenConnections"
decl_stmt|;
name|String
name|NUM_OPEN_CONNECTIONS_DESC
init|=
literal|"Number of open connections."
decl_stmt|;
name|String
name|NUM_ACTIVE_HANDLER_NAME
init|=
literal|"numActiveHandler"
decl_stmt|;
name|String
name|NUM_ACTIVE_HANDLER_DESC
init|=
literal|"Total number of active rpc handlers."
decl_stmt|;
name|String
name|NUM_ACTIVE_GENERAL_HANDLER_NAME
init|=
literal|"numActiveGeneralHandler"
decl_stmt|;
name|String
name|NUM_ACTIVE_GENERAL_HANDLER_DESC
init|=
literal|"Number of active general rpc handlers."
decl_stmt|;
name|String
name|NUM_ACTIVE_PRIORITY_HANDLER_NAME
init|=
literal|"numActivePriorityHandler"
decl_stmt|;
name|String
name|NUM_ACTIVE_PRIORITY_HANDLER_DESC
init|=
literal|"Number of active priority rpc handlers."
decl_stmt|;
name|String
name|NUM_ACTIVE_REPLICATION_HANDLER_NAME
init|=
literal|"numActiveReplicationHandler"
decl_stmt|;
name|String
name|NUM_ACTIVE_REPLICATION_HANDLER_DESC
init|=
literal|"Number of active replication rpc handlers."
decl_stmt|;
name|String
name|NUM_ACTIVE_WRITE_HANDLER_NAME
init|=
literal|"numActiveWriteHandler"
decl_stmt|;
name|String
name|NUM_ACTIVE_WRITE_HANDLER_DESC
init|=
literal|"Number of active write rpc handlers."
decl_stmt|;
name|String
name|NUM_ACTIVE_READ_HANDLER_NAME
init|=
literal|"numActiveReadHandler"
decl_stmt|;
name|String
name|NUM_ACTIVE_READ_HANDLER_DESC
init|=
literal|"Number of active read rpc handlers."
decl_stmt|;
name|String
name|NUM_ACTIVE_SCAN_HANDLER_NAME
init|=
literal|"numActiveScanHandler"
decl_stmt|;
name|String
name|NUM_ACTIVE_SCAN_HANDLER_DESC
init|=
literal|"Number of active scan rpc handlers."
decl_stmt|;
name|String
name|NUM_GENERAL_CALLS_DROPPED_NAME
init|=
literal|"numGeneralCallsDropped"
decl_stmt|;
name|String
name|NUM_GENERAL_CALLS_DROPPED_DESC
init|=
literal|"Total number of calls in general queue which "
operator|+
literal|"were dropped by CoDel RPC executor"
decl_stmt|;
name|String
name|NUM_LIFO_MODE_SWITCHES_NAME
init|=
literal|"numLifoModeSwitches"
decl_stmt|;
name|String
name|NUM_LIFO_MODE_SWITCHES_DESC
init|=
literal|"Total number of calls in general queue which "
operator|+
literal|"were served from the tail of the queue"
decl_stmt|;
comment|// Direct Memory Usage metrics
name|String
name|NETTY_DM_USAGE_NAME
init|=
literal|"nettyDirectMemoryUsage"
decl_stmt|;
name|String
name|NETTY_DM_USAGE_DESC
init|=
literal|"Current Netty direct memory usage."
decl_stmt|;
name|void
name|authorizationSuccess
parameter_list|()
function_decl|;
name|void
name|authorizationFailure
parameter_list|()
function_decl|;
name|void
name|authenticationSuccess
parameter_list|()
function_decl|;
name|void
name|authenticationFailure
parameter_list|()
function_decl|;
name|void
name|authenticationFallback
parameter_list|()
function_decl|;
name|void
name|sentBytes
parameter_list|(
name|long
name|count
parameter_list|)
function_decl|;
name|void
name|receivedBytes
parameter_list|(
name|int
name|count
parameter_list|)
function_decl|;
name|void
name|sentResponse
parameter_list|(
name|long
name|count
parameter_list|)
function_decl|;
name|void
name|receivedRequest
parameter_list|(
name|long
name|count
parameter_list|)
function_decl|;
name|void
name|dequeuedCall
parameter_list|(
name|int
name|qTime
parameter_list|)
function_decl|;
name|void
name|processedCall
parameter_list|(
name|int
name|processingTime
parameter_list|)
function_decl|;
name|void
name|queuedAndProcessedCall
parameter_list|(
name|int
name|totalTime
parameter_list|)
function_decl|;
block|}
end_interface

end_unit

