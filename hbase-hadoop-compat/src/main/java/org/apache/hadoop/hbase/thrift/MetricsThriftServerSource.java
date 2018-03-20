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
name|thrift
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
name|hadoop
operator|.
name|hbase
operator|.
name|metrics
operator|.
name|JvmPauseMonitorSource
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
comment|/**  * Interface of a class that will export metrics about Thrift to hadoop's metrics2.  */
end_comment

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
interface|interface
name|MetricsThriftServerSource
extends|extends
name|ExceptionTrackingSource
extends|,
name|JvmPauseMonitorSource
block|{
name|String
name|BATCH_GET_KEY
init|=
literal|"batchGet"
decl_stmt|;
name|String
name|BATCH_MUTATE_KEY
init|=
literal|"batchMutate"
decl_stmt|;
name|String
name|TIME_IN_QUEUE_KEY
init|=
literal|"timeInQueue"
decl_stmt|;
name|String
name|THRIFT_CALL_KEY
init|=
literal|"thriftCall"
decl_stmt|;
name|String
name|SLOW_THRIFT_CALL_KEY
init|=
literal|"slowThriftCall"
decl_stmt|;
name|String
name|CALL_QUEUE_LEN_KEY
init|=
literal|"callQueueLen"
decl_stmt|;
name|String
name|ACTIVE_WORKER_COUNT_KEY
init|=
literal|"numActiveWorkers"
decl_stmt|;
comment|/**    * Add how long an operation was in the queue.    * @param time the time to add    */
name|void
name|incTimeInQueue
parameter_list|(
name|long
name|time
parameter_list|)
function_decl|;
comment|/**    * Set the call queue length.    * @param len Time    */
name|void
name|setCallQueueLen
parameter_list|(
name|int
name|len
parameter_list|)
function_decl|;
comment|/**    * Add how many keys were in a batch get.    * @param diff Num Keys    */
name|void
name|incNumRowKeysInBatchGet
parameter_list|(
name|int
name|diff
parameter_list|)
function_decl|;
comment|/**    * Add how many keys were in a batch mutate.    * @param diff Num Keys    */
name|void
name|incNumRowKeysInBatchMutate
parameter_list|(
name|int
name|diff
parameter_list|)
function_decl|;
comment|/**    * Add how long a method took    * @param name Method name    * @param time Time    */
name|void
name|incMethodTime
parameter_list|(
name|String
name|name
parameter_list|,
name|long
name|time
parameter_list|)
function_decl|;
comment|/**    * Add how long a call took    * @param time Time    */
name|void
name|incCall
parameter_list|(
name|long
name|time
parameter_list|)
function_decl|;
comment|/**    * Increment how long a slow call took.    * @param time Time    */
name|void
name|incSlowCall
parameter_list|(
name|long
name|time
parameter_list|)
function_decl|;
comment|/**    * Increment number of active thrift workers.    */
name|void
name|incActiveWorkerCount
parameter_list|()
function_decl|;
comment|/**    * Decrement number of active thrift workers.    */
name|void
name|decActiveWorkerCount
parameter_list|()
function_decl|;
block|}
end_interface

end_unit

