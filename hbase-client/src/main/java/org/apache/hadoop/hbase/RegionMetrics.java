begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
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
name|util
operator|.
name|Bytes
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
comment|/**   * Encapsulates per-region load metrics.   */
end_comment

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|Public
specifier|public
interface|interface
name|RegionMetrics
block|{
comment|/**    * @return the region name    */
name|byte
index|[]
name|getRegionName
parameter_list|()
function_decl|;
comment|/**    * @return the number of stores    */
name|int
name|getStoreCount
parameter_list|()
function_decl|;
comment|/**    * @return the number of storefiles    */
name|int
name|getStoreFileCount
parameter_list|()
function_decl|;
comment|/**    * @return the total size of the storefiles    */
name|Size
name|getStoreFileSize
parameter_list|()
function_decl|;
comment|/**    * @return the memstore size    */
name|Size
name|getMemStoreSize
parameter_list|()
function_decl|;
comment|/**    * @return the number of read requests made to region    */
name|long
name|getReadRequestCount
parameter_list|()
function_decl|;
comment|/**    * @return the number of write requests made to region    */
name|long
name|getWriteRequestCount
parameter_list|()
function_decl|;
comment|/**    * @return the number of coprocessor service requests made to region    */
specifier|public
name|long
name|getCpRequestCount
parameter_list|()
function_decl|;
comment|/**    * @return the number of write requests and read requests and coprocessor    *         service requests made to region    */
specifier|default
name|long
name|getRequestCount
parameter_list|()
block|{
return|return
name|getReadRequestCount
argument_list|()
operator|+
name|getWriteRequestCount
argument_list|()
operator|+
name|getCpRequestCount
argument_list|()
return|;
block|}
comment|/**    * @return the region name as a string    */
specifier|default
name|String
name|getNameAsString
parameter_list|()
block|{
return|return
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|getRegionName
argument_list|()
argument_list|)
return|;
block|}
comment|/**    * @return the number of filtered read requests made to region    */
name|long
name|getFilteredReadRequestCount
parameter_list|()
function_decl|;
comment|/**    * TODO: why we pass the same value to different counters? Currently, the value from    * getStoreFileIndexSize() is same with getStoreFileRootLevelIndexSize()    * see HRegionServer#createRegionLoad.    * @return The current total size of root-level indexes for the region    */
name|Size
name|getStoreFileIndexSize
parameter_list|()
function_decl|;
comment|/**    * @return The current total size of root-level indexes for the region    */
name|Size
name|getStoreFileRootLevelIndexSize
parameter_list|()
function_decl|;
comment|/**    * @return The total size of all index blocks, not just the root level    */
name|Size
name|getStoreFileUncompressedDataIndexSize
parameter_list|()
function_decl|;
comment|/**    * @return The total size of all Bloom filter blocks, not just loaded into the block cache    */
name|Size
name|getBloomFilterSize
parameter_list|()
function_decl|;
comment|/**    * @return the total number of cells in current compaction    */
name|long
name|getCompactingCellCount
parameter_list|()
function_decl|;
comment|/**    * @return the number of already compacted kvs in current compaction    */
name|long
name|getCompactedCellCount
parameter_list|()
function_decl|;
comment|/**    * This does not really belong inside RegionLoad but its being done in the name of expediency.    * @return the completed sequence Id for the region    */
name|long
name|getCompletedSequenceId
parameter_list|()
function_decl|;
comment|/**    * @return completed sequence id per store.    */
name|Map
argument_list|<
name|byte
index|[]
argument_list|,
name|Long
argument_list|>
name|getStoreSequenceId
parameter_list|()
function_decl|;
comment|/**    * @return the uncompressed size of the storefiles    */
name|Size
name|getUncompressedStoreFileSize
parameter_list|()
function_decl|;
comment|/**    * @return the data locality of region in the regionserver.    */
name|float
name|getDataLocality
parameter_list|()
function_decl|;
comment|/**    * @return the timestamp of the oldest hfile for any store of this region.    */
name|long
name|getLastMajorCompactionTimestamp
parameter_list|()
function_decl|;
comment|/**    * @return the reference count for the stores of this region    */
name|int
name|getStoreRefCount
parameter_list|()
function_decl|;
comment|/**    * @return the max reference count for any store file among all compacted stores files    *   of this region    */
name|int
name|getMaxCompactedStoreFileRefCount
parameter_list|()
function_decl|;
block|}
end_interface

end_unit

