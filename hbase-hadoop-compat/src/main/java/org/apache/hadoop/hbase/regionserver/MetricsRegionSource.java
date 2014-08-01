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
package|;
end_package

begin_comment
comment|/**  * This interface will be implemented to allow single regions to push metrics into  * MetricsRegionAggregateSource that will in turn push data to the Hadoop metrics system.  */
end_comment

begin_interface
specifier|public
interface|interface
name|MetricsRegionSource
extends|extends
name|Comparable
argument_list|<
name|MetricsRegionSource
argument_list|>
block|{
name|String
name|OPS_SAMPLE_NAME
init|=
literal|"ops"
decl_stmt|;
name|String
name|SIZE_VALUE_NAME
init|=
literal|"size"
decl_stmt|;
name|String
name|COMPACTIONS_COMPLETED_COUNT
init|=
literal|"compactionsCompletedCount"
decl_stmt|;
name|String
name|NUM_BYTES_COMPACTED_COUNT
init|=
literal|"numBytesCompactedCount"
decl_stmt|;
name|String
name|NUM_FILES_COMPACTED_COUNT
init|=
literal|"numFilesCompactedCount"
decl_stmt|;
name|String
name|COMPACTIONS_COMPLETED_DESC
init|=
literal|"Number of compactions that have completed."
decl_stmt|;
name|String
name|NUM_BYTES_COMPACTED_DESC
init|=
literal|"Sum of filesize on all files entering a finished, successful or aborted, compaction"
decl_stmt|;
name|String
name|NUM_FILES_COMPACTED_DESC
init|=
literal|"Number of files that were input for finished, successful or aborted, compactions"
decl_stmt|;
name|String
name|COPROCESSOR_EXECUTION_STATISTICS
init|=
literal|"coprocessorExecutionStatistics"
decl_stmt|;
name|String
name|COPROCESSOR_EXECUTION_STATISTICS_DESC
init|=
literal|"Statistics for coprocessor execution times"
decl_stmt|;
comment|/**    * Close the region's metrics as this region is closing.    */
name|void
name|close
parameter_list|()
function_decl|;
comment|/**    * Update related counts of puts.    */
name|void
name|updatePut
parameter_list|()
function_decl|;
comment|/**    * Update related counts of deletes.    */
name|void
name|updateDelete
parameter_list|()
function_decl|;
comment|/**    * Update count and sizes of gets.    * @param getSize size in bytes of the resulting key values for a get    */
name|void
name|updateGet
parameter_list|(
name|long
name|getSize
parameter_list|)
function_decl|;
comment|/**    * Update the count and sizes of resultScanner.next()    * @param scanSize Size in bytes of the resulting key values for a next()    */
name|void
name|updateScan
parameter_list|(
name|long
name|scanSize
parameter_list|)
function_decl|;
comment|/**    * Update related counts of increments.    */
name|void
name|updateIncrement
parameter_list|()
function_decl|;
comment|/**    * Update related counts of appends.    */
name|void
name|updateAppend
parameter_list|()
function_decl|;
comment|/**    * Get the aggregate source to which this reports.    */
name|MetricsRegionAggregateSource
name|getAggregateSource
parameter_list|()
function_decl|;
block|}
end_interface

end_unit

