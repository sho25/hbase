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
name|regionserver
operator|.
name|wal
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
comment|/**  * Interface of the source that will export metrics about the region server's WAL.  */
end_comment

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
interface|interface
name|MetricsWALSource
extends|extends
name|BaseSource
block|{
comment|/**    * The name of the metrics    */
name|String
name|METRICS_NAME
init|=
literal|"WAL"
decl_stmt|;
comment|/**    * The name of the metrics context that metrics will be under.    */
name|String
name|METRICS_CONTEXT
init|=
literal|"regionserver"
decl_stmt|;
comment|/**    * Description    */
name|String
name|METRICS_DESCRIPTION
init|=
literal|"Metrics about HBase RegionServer WAL"
decl_stmt|;
comment|/**    * The name of the metrics context that metrics will be under in jmx    */
name|String
name|METRICS_JMX_CONTEXT
init|=
literal|"RegionServer,sub="
operator|+
name|METRICS_NAME
decl_stmt|;
name|String
name|APPEND_TIME
init|=
literal|"appendTime"
decl_stmt|;
name|String
name|APPEND_TIME_DESC
init|=
literal|"Time an append to the log took."
decl_stmt|;
name|String
name|APPEND_COUNT
init|=
literal|"appendCount"
decl_stmt|;
name|String
name|APPEND_COUNT_DESC
init|=
literal|"Number of appends to the write ahead log."
decl_stmt|;
name|String
name|APPEND_SIZE
init|=
literal|"appendSize"
decl_stmt|;
name|String
name|APPEND_SIZE_DESC
init|=
literal|"Size (in bytes) of the data appended to the WAL."
decl_stmt|;
name|String
name|SLOW_APPEND_COUNT
init|=
literal|"slowAppendCount"
decl_stmt|;
name|String
name|SLOW_APPEND_COUNT_DESC
init|=
literal|"Number of appends that were slow."
decl_stmt|;
name|String
name|SYNC_TIME
init|=
literal|"syncTime"
decl_stmt|;
name|String
name|SYNC_TIME_DESC
init|=
literal|"The time it took to sync the WAL to HDFS."
decl_stmt|;
name|String
name|ROLL_REQUESTED
init|=
literal|"rollRequest"
decl_stmt|;
name|String
name|ROLL_REQUESTED_DESC
init|=
literal|"How many times a log roll has been requested total"
decl_stmt|;
name|String
name|LOW_REPLICA_ROLL_REQUESTED
init|=
literal|"lowReplicaRollRequest"
decl_stmt|;
name|String
name|LOW_REPLICA_ROLL_REQUESTED_DESC
init|=
literal|"How many times a log roll was requested due to too few DN's in the write pipeline."
decl_stmt|;
name|String
name|WRITTEN_BYTES
init|=
literal|"writtenBytes"
decl_stmt|;
name|String
name|WRITTEN_BYTES_DESC
init|=
literal|"Size (in bytes) of the data written to the WAL."
decl_stmt|;
comment|/**    * Add the append size.    */
name|void
name|incrementAppendSize
parameter_list|(
name|long
name|size
parameter_list|)
function_decl|;
comment|/**    * Add the time it took to append.    */
name|void
name|incrementAppendTime
parameter_list|(
name|long
name|time
parameter_list|)
function_decl|;
comment|/**    * Increment the count of wal appends    */
name|void
name|incrementAppendCount
parameter_list|()
function_decl|;
comment|/**    * Increment the number of appends that were slow    */
name|void
name|incrementSlowAppendCount
parameter_list|()
function_decl|;
comment|/**    * Add the time it took to sync the wal.    */
name|void
name|incrementSyncTime
parameter_list|(
name|long
name|time
parameter_list|)
function_decl|;
name|void
name|incrementLogRollRequested
parameter_list|()
function_decl|;
name|void
name|incrementLowReplicationLogRoll
parameter_list|()
function_decl|;
name|long
name|getSlowAppendCount
parameter_list|()
function_decl|;
name|void
name|incrementWrittenBytes
parameter_list|(
name|long
name|val
parameter_list|)
function_decl|;
name|long
name|getWrittenBytes
parameter_list|()
function_decl|;
block|}
end_interface

end_unit

