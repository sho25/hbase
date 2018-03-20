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
name|zookeeper
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
name|BaseSource
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
comment|/**  * Interface of the source that will export metrics about the ZooKeeper.  */
end_comment

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
interface|interface
name|MetricsZooKeeperSource
extends|extends
name|BaseSource
block|{
comment|/**    * The name of the metrics    */
name|String
name|METRICS_NAME
init|=
literal|"ZOOKEEPER"
decl_stmt|;
comment|/**    * The name of the metrics context that metrics will be under.    */
name|String
name|METRICS_CONTEXT
init|=
literal|"zookeeper"
decl_stmt|;
comment|/**    * Description    */
name|String
name|METRICS_DESCRIPTION
init|=
literal|"Metrics about ZooKeeper"
decl_stmt|;
comment|/**    * The name of the metrics context that metrics will be under in jmx.    */
name|String
name|METRICS_JMX_CONTEXT
init|=
literal|"ZooKeeper,sub="
operator|+
name|METRICS_NAME
decl_stmt|;
name|String
name|EXCEPTION_AUTHFAILED
init|=
literal|"AUTHFAILED Exception"
decl_stmt|;
name|String
name|EXCEPTION_AUTHFAILED_DESC
init|=
literal|"Number of failed ops due to an AUTHFAILED exception,"
decl_stmt|;
name|String
name|EXCEPTION_CONNECTIONLOSS
init|=
literal|"CONNECTIONLOSS Exception"
decl_stmt|;
name|String
name|EXCEPTION_CONNECTIONLOSS_DESC
init|=
literal|"Number of failed ops due to a CONNECTIONLOSS exception."
decl_stmt|;
name|String
name|EXCEPTION_DATAINCONSISTENCY
init|=
literal|"DATAINCONSISTENCY Exception"
decl_stmt|;
name|String
name|EXCEPTION_DATAINCONSISTENCY_DESC
init|=
literal|"Number of failed ops due to a DATAINCONSISTENCY exception."
decl_stmt|;
name|String
name|EXCEPTION_INVALIDACL
init|=
literal|"INVALIDACL Exception"
decl_stmt|;
name|String
name|EXCEPTION_INVALIDACL_DESC
init|=
literal|"Number of failed ops due to an INVALIDACL exception"
decl_stmt|;
name|String
name|EXCEPTION_NOAUTH
init|=
literal|"NOAUTH Exception"
decl_stmt|;
name|String
name|EXCEPTION_NOAUTH_DESC
init|=
literal|"Number of failed ops due to a NOAUTH exception."
decl_stmt|;
name|String
name|EXCEPTION_OPERATIONTIMEOUT
init|=
literal|"OPERATIONTIMEOUT Exception"
decl_stmt|;
name|String
name|EXCEPTION_OPERATIONTIMEOUT_DESC
init|=
literal|"Number of failed ops due to an OPERATIONTIMEOUT exception."
decl_stmt|;
name|String
name|EXCEPTION_RUNTIMEINCONSISTENCY
init|=
literal|"RUNTIMEINCONSISTENCY Exception"
decl_stmt|;
name|String
name|EXCEPTION_RUNTIMEINCONSISTENCY_DESC
init|=
literal|"Number of failed ops due to a RUNTIMEINCONSISTENCY exception."
decl_stmt|;
name|String
name|EXCEPTION_SESSIONEXPIRED
init|=
literal|"SESSIONEXPIRED Exception"
decl_stmt|;
name|String
name|EXCEPTION_SESSIONEXPIRED_DESC
init|=
literal|"Number of failed ops due to a SESSIONEXPIRED exception."
decl_stmt|;
name|String
name|EXCEPTION_SYSTEMERROR
init|=
literal|"SYSTEMERROR Exception"
decl_stmt|;
name|String
name|EXCEPTION_SYSTEMERROR_DESC
init|=
literal|"Number of failed ops due to a SYSTEMERROR exception."
decl_stmt|;
name|String
name|TOTAL_FAILED_ZK_CALLS
init|=
literal|"TotalFailedZKCalls"
decl_stmt|;
name|String
name|TOTAL_FAILED_ZK_CALLS_DESC
init|=
literal|"Total number of failed ZooKeeper API Calls"
decl_stmt|;
name|String
name|READ_OPERATION_LATENCY_NAME
init|=
literal|"ReadOperationLatency"
decl_stmt|;
name|String
name|READ_OPERATION_LATENCY_DESC
init|=
literal|"Latency histogram for read operations."
decl_stmt|;
name|String
name|WRITE_OPERATION_LATENCY_NAME
init|=
literal|"WriteOperationLatency"
decl_stmt|;
name|String
name|WRITE_OPERATION_LATENCY_DESC
init|=
literal|"Latency histogram for write operations."
decl_stmt|;
name|String
name|SYNC_OPERATION_LATENCY_NAME
init|=
literal|"SyncOperationLatency"
decl_stmt|;
name|String
name|SYNC_OPERATION_LATENCY_DESC
init|=
literal|"Latency histogram for sync operations."
decl_stmt|;
comment|/**    * Increment the count of failed ops due to AUTHFAILED Exception.    */
name|void
name|incrementAuthFailedCount
parameter_list|()
function_decl|;
comment|/**    * Increment the count of failed ops due to a CONNECTIONLOSS Exception.    */
name|void
name|incrementConnectionLossCount
parameter_list|()
function_decl|;
comment|/**    * Increment the count of failed ops due to a DATAINCONSISTENCY Exception.    */
name|void
name|incrementDataInconsistencyCount
parameter_list|()
function_decl|;
comment|/**    * Increment the count of failed ops due to INVALIDACL Exception.    */
name|void
name|incrementInvalidACLCount
parameter_list|()
function_decl|;
comment|/**    * Increment the count of failed ops due to NOAUTH Exception.    */
name|void
name|incrementNoAuthCount
parameter_list|()
function_decl|;
comment|/**    * Increment the count of failed ops due to an OPERATIONTIMEOUT Exception.    */
name|void
name|incrementOperationTimeoutCount
parameter_list|()
function_decl|;
comment|/**    * Increment the count of failed ops due to RUNTIMEINCONSISTENCY Exception.    */
name|void
name|incrementRuntimeInconsistencyCount
parameter_list|()
function_decl|;
comment|/**    * Increment the count of failed ops due to a SESSIONEXPIRED Exception.    */
name|void
name|incrementSessionExpiredCount
parameter_list|()
function_decl|;
comment|/**    * Increment the count of failed ops due to a SYSTEMERROR Exception.    */
name|void
name|incrementSystemErrorCount
parameter_list|()
function_decl|;
comment|/**    * Record the latency incurred for read operations.    */
name|void
name|recordReadOperationLatency
parameter_list|(
name|long
name|latency
parameter_list|)
function_decl|;
comment|/**    * Record the latency incurred for write operations.    */
name|void
name|recordWriteOperationLatency
parameter_list|(
name|long
name|latency
parameter_list|)
function_decl|;
comment|/**    * Record the latency incurred for sync operations.    */
name|void
name|recordSyncOperationLatency
parameter_list|(
name|long
name|latency
parameter_list|)
function_decl|;
comment|/**    * Record the total number of failed ZooKeeper API calls.    */
name|void
name|incrementTotalFailedZKCalls
parameter_list|()
function_decl|;
block|}
end_interface

end_unit

