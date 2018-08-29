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
name|java
operator|.
name|io
operator|.
name|Closeable
import|;
end_import

begin_comment
comment|/**  * This interface will be implemented to allow region server to push table metrics into  * MetricsRegionAggregateSource that will in turn push data to the Hadoop metrics system.  */
end_comment

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
interface|interface
name|MetricsTableSource
extends|extends
name|Comparable
argument_list|<
name|MetricsTableSource
argument_list|>
extends|,
name|Closeable
block|{
name|String
name|TABLE_SIZE
init|=
literal|"tableSize"
decl_stmt|;
name|String
name|TABLE_SIZE_DESC
init|=
literal|"Total size of the table in the region server"
decl_stmt|;
name|String
name|getTableName
parameter_list|()
function_decl|;
comment|/**    * Close the table's metrics as all the region are closing.    */
annotation|@
name|Override
name|void
name|close
parameter_list|()
function_decl|;
name|void
name|registerMetrics
parameter_list|()
function_decl|;
comment|/**    * Get the aggregate source to which this reports.    */
name|MetricsTableAggregateSource
name|getAggregateSource
parameter_list|()
function_decl|;
comment|/**    * Update the split transaction time histogram    * @param t time it took, in milliseconds    */
name|void
name|updateSplitTime
parameter_list|(
name|long
name|t
parameter_list|)
function_decl|;
comment|/**    * Increment number of a requested splits    */
name|void
name|incrSplitRequest
parameter_list|()
function_decl|;
comment|/**    * Increment number of successful splits    */
name|void
name|incrSplitSuccess
parameter_list|()
function_decl|;
comment|/**    * Update the flush time histogram    * @param t time it took, in milliseconds    */
name|void
name|updateFlushTime
parameter_list|(
name|long
name|t
parameter_list|)
function_decl|;
comment|/**    * Update the flush memstore size histogram    * @param bytes the number of bytes in the memstore    */
name|void
name|updateFlushMemstoreSize
parameter_list|(
name|long
name|bytes
parameter_list|)
function_decl|;
comment|/**    * Update the flush output file size histogram    * @param bytes the number of bytes in the output file    */
name|void
name|updateFlushOutputSize
parameter_list|(
name|long
name|bytes
parameter_list|)
function_decl|;
comment|/**    * Update the compaction time histogram, both major and minor    * @param isMajor whether compaction is a major compaction    * @param t time it took, in milliseconds    */
name|void
name|updateCompactionTime
parameter_list|(
name|boolean
name|isMajor
parameter_list|,
name|long
name|t
parameter_list|)
function_decl|;
comment|/**    * Update the compaction input number of files histogram    * @param isMajor whether compaction is a major compaction    * @param c number of files    */
name|void
name|updateCompactionInputFileCount
parameter_list|(
name|boolean
name|isMajor
parameter_list|,
name|long
name|c
parameter_list|)
function_decl|;
comment|/**    * Update the compaction total input file size histogram    * @param isMajor whether compaction is a major compaction    * @param bytes the number of bytes of the compaction input file    */
name|void
name|updateCompactionInputSize
parameter_list|(
name|boolean
name|isMajor
parameter_list|,
name|long
name|bytes
parameter_list|)
function_decl|;
comment|/**    * Update the compaction output number of files histogram    * @param isMajor whether compaction is a major compaction    * @param c number of files    */
name|void
name|updateCompactionOutputFileCount
parameter_list|(
name|boolean
name|isMajor
parameter_list|,
name|long
name|c
parameter_list|)
function_decl|;
comment|/**    * Update the compaction total output file size    * @param isMajor whether compaction is a major compaction    * @param bytes the number of bytes of the compaction input file    */
name|void
name|updateCompactionOutputSize
parameter_list|(
name|boolean
name|isMajor
parameter_list|,
name|long
name|bytes
parameter_list|)
function_decl|;
block|}
end_interface

end_unit

