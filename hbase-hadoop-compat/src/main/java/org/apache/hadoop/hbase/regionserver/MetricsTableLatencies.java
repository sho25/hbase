begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one or more  * contributor license agreements.  See the NOTICE file distributed with  * this work for additional information regarding copyright ownership.  * The ASF licenses this file to you under the Apache License, Version 2.0  * (the "License"); you may not use this file except in compliance with  * the License.  You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
package|;
end_package

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
comment|/**  * Latency metrics for a specific table in a RegionServer.  */
end_comment

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
interface|interface
name|MetricsTableLatencies
block|{
comment|/**    * The name of the metrics    */
name|String
name|METRICS_NAME
init|=
literal|"TableLatencies"
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
literal|"Metrics about Tables on a single HBase RegionServer"
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
name|GET_TIME
init|=
literal|"getTime"
decl_stmt|;
name|String
name|SCAN_TIME
init|=
literal|"scanTime"
decl_stmt|;
name|String
name|SCAN_SIZE
init|=
literal|"scanSize"
decl_stmt|;
name|String
name|PUT_TIME
init|=
literal|"putTime"
decl_stmt|;
name|String
name|PUT_BATCH_TIME
init|=
literal|"putBatchTime"
decl_stmt|;
name|String
name|DELETE_TIME
init|=
literal|"deleteTime"
decl_stmt|;
name|String
name|DELETE_BATCH_TIME
init|=
literal|"deleteBatchTime"
decl_stmt|;
name|String
name|INCREMENT_TIME
init|=
literal|"incrementTime"
decl_stmt|;
name|String
name|APPEND_TIME
init|=
literal|"appendTime"
decl_stmt|;
comment|/**    * Update the Put time histogram    *    * @param tableName The table the metric is for    * @param t time it took    */
name|void
name|updatePut
parameter_list|(
name|String
name|tableName
parameter_list|,
name|long
name|t
parameter_list|)
function_decl|;
comment|/**    * Update the batch Put time histogram    *    * @param tableName The table the metric is for    * @param t time it took    */
name|void
name|updatePutBatch
parameter_list|(
name|String
name|tableName
parameter_list|,
name|long
name|t
parameter_list|)
function_decl|;
comment|/**    * Update the Delete time histogram    *    * @param tableName The table the metric is for    * @param t time it took    */
name|void
name|updateDelete
parameter_list|(
name|String
name|tableName
parameter_list|,
name|long
name|t
parameter_list|)
function_decl|;
comment|/**    * Update the batch Delete time histogram    *    * @param tableName The table the metric is for    * @param t time it took    */
name|void
name|updateDeleteBatch
parameter_list|(
name|String
name|tableName
parameter_list|,
name|long
name|t
parameter_list|)
function_decl|;
comment|/**    * Update the Get time histogram .    *    * @param tableName The table the metric is for    * @param t time it took    */
name|void
name|updateGet
parameter_list|(
name|String
name|tableName
parameter_list|,
name|long
name|t
parameter_list|)
function_decl|;
comment|/**    * Update the Increment time histogram.    *    * @param tableName The table the metric is for    * @param t time it took    */
name|void
name|updateIncrement
parameter_list|(
name|String
name|tableName
parameter_list|,
name|long
name|t
parameter_list|)
function_decl|;
comment|/**    * Update the Append time histogram.    *    * @param tableName The table the metric is for    * @param t time it took    */
name|void
name|updateAppend
parameter_list|(
name|String
name|tableName
parameter_list|,
name|long
name|t
parameter_list|)
function_decl|;
comment|/**    * Update the scan size.    *    * @param tableName The table the metric is for    * @param scanSize size of the scan    */
name|void
name|updateScanSize
parameter_list|(
name|String
name|tableName
parameter_list|,
name|long
name|scanSize
parameter_list|)
function_decl|;
comment|/**    * Update the scan time.    *    * @param tableName The table the metric is for    * @param t time it took    */
name|void
name|updateScanTime
parameter_list|(
name|String
name|tableName
parameter_list|,
name|long
name|t
parameter_list|)
function_decl|;
block|}
end_interface

end_unit

