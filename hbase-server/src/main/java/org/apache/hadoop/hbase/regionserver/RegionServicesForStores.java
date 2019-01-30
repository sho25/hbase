begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|ThreadPoolExecutor
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
name|client
operator|.
name|RegionInfo
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
name|executor
operator|.
name|ExecutorType
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
name|wal
operator|.
name|WAL
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
comment|/**  * Services a Store needs from a Region.  * RegionServicesForStores class is the interface through which memstore access services at the  * region level.  * For example, when using alternative memory formats or due to compaction the memstore needs to  * take occasional lock and update size counters at the region level.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|RegionServicesForStores
block|{
specifier|private
specifier|final
name|HRegion
name|region
decl_stmt|;
specifier|private
specifier|final
name|RegionServerServices
name|rsServices
decl_stmt|;
specifier|private
name|int
name|inMemoryPoolSize
decl_stmt|;
specifier|public
name|RegionServicesForStores
parameter_list|(
name|HRegion
name|region
parameter_list|,
name|RegionServerServices
name|rsServices
parameter_list|)
block|{
name|this
operator|.
name|region
operator|=
name|region
expr_stmt|;
name|this
operator|.
name|rsServices
operator|=
name|rsServices
expr_stmt|;
if|if
condition|(
name|this
operator|.
name|rsServices
operator|!=
literal|null
condition|)
block|{
name|this
operator|.
name|inMemoryPoolSize
operator|=
name|rsServices
operator|.
name|getConfiguration
argument_list|()
operator|.
name|getInt
argument_list|(
name|CompactingMemStore
operator|.
name|IN_MEMORY_CONPACTION_POOL_SIZE_KEY
argument_list|,
name|CompactingMemStore
operator|.
name|IN_MEMORY_CONPACTION_POOL_SIZE_DEFAULT
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|void
name|addMemStoreSize
parameter_list|(
name|long
name|dataSizeDelta
parameter_list|,
name|long
name|heapSizeDelta
parameter_list|,
name|long
name|offHeapSizeDelta
parameter_list|,
name|int
name|cellsCountDelta
parameter_list|)
block|{
name|region
operator|.
name|incMemStoreSize
argument_list|(
name|dataSizeDelta
argument_list|,
name|heapSizeDelta
argument_list|,
name|offHeapSizeDelta
argument_list|,
name|cellsCountDelta
argument_list|)
expr_stmt|;
block|}
specifier|public
name|RegionInfo
name|getRegionInfo
parameter_list|()
block|{
return|return
name|region
operator|.
name|getRegionInfo
argument_list|()
return|;
block|}
specifier|public
name|WAL
name|getWAL
parameter_list|()
block|{
return|return
name|region
operator|.
name|getWAL
argument_list|()
return|;
block|}
name|ThreadPoolExecutor
name|getInMemoryCompactionPool
parameter_list|()
block|{
if|if
condition|(
name|rsServices
operator|!=
literal|null
condition|)
block|{
return|return
name|rsServices
operator|.
name|getExecutorService
argument_list|()
operator|.
name|getExecutorLazily
argument_list|(
name|ExecutorType
operator|.
name|RS_IN_MEMORY_COMPACTION
argument_list|,
name|inMemoryPoolSize
argument_list|)
return|;
block|}
else|else
block|{
return|return
literal|null
return|;
block|}
block|}
specifier|public
name|long
name|getMemStoreFlushSize
parameter_list|()
block|{
return|return
name|region
operator|.
name|getMemStoreFlushSize
argument_list|()
return|;
block|}
specifier|public
name|int
name|getNumStores
parameter_list|()
block|{
return|return
name|region
operator|.
name|getTableDescriptor
argument_list|()
operator|.
name|getColumnFamilyCount
argument_list|()
return|;
block|}
annotation|@
name|VisibleForTesting
name|long
name|getMemStoreSize
parameter_list|()
block|{
return|return
name|region
operator|.
name|getMemStoreDataSize
argument_list|()
return|;
block|}
block|}
end_class

end_unit

