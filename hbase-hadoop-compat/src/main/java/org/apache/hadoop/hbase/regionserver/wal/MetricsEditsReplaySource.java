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

begin_comment
comment|/**  * Interface of the source that will export metrics about log replay statistics when recovering a  * region server in distributedLogReplay mode  */
end_comment

begin_interface
specifier|public
interface|interface
name|MetricsEditsReplaySource
extends|extends
name|BaseSource
block|{
comment|/**    * The name of the metrics    */
name|String
name|METRICS_NAME
init|=
literal|"replay"
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
literal|"Metrics about HBase RegionServer WAL Edits Replay"
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
name|REPLAY_TIME_NAME
init|=
literal|"replayTime"
decl_stmt|;
name|String
name|REPLAY_TIME_DESC
init|=
literal|"Time an replay operation took."
decl_stmt|;
name|String
name|REPLAY_BATCH_SIZE_NAME
init|=
literal|"replayBatchSize"
decl_stmt|;
name|String
name|REPLAY_BATCH_SIZE_DESC
init|=
literal|"Number of changes in each replay batch."
decl_stmt|;
name|String
name|REPLAY_DATA_SIZE_NAME
init|=
literal|"replayDataSize"
decl_stmt|;
name|String
name|REPLAY_DATA_SIZE_DESC
init|=
literal|"Size (in bytes) of the data of each replay."
decl_stmt|;
comment|/**    * Add the time a replay command took    */
name|void
name|updateReplayTime
parameter_list|(
name|long
name|time
parameter_list|)
function_decl|;
comment|/**    * Add the batch size of each replay    */
name|void
name|updateReplayBatchSize
parameter_list|(
name|long
name|size
parameter_list|)
function_decl|;
comment|/**    * Add the payload data size of each replay    */
name|void
name|updateReplayDataSize
parameter_list|(
name|long
name|size
parameter_list|)
function_decl|;
block|}
end_interface

end_unit

