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
name|Collections
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|List
import|;
end_import

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
name|java
operator|.
name|util
operator|.
name|stream
operator|.
name|Collectors
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
name|Strings
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
name|base
operator|.
name|Preconditions
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
name|protobuf
operator|.
name|UnsafeByteOperations
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
name|shaded
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|AdminProtos
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
name|shaded
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|ClusterStatusProtos
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
name|shaded
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|HBaseProtos
import|;
end_import

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
specifier|final
class|class
name|RegionMetricsBuilder
block|{
specifier|public
specifier|static
name|List
argument_list|<
name|RegionMetrics
argument_list|>
name|toRegionMetrics
parameter_list|(
name|AdminProtos
operator|.
name|GetRegionLoadResponse
name|regionLoadResponse
parameter_list|)
block|{
return|return
name|regionLoadResponse
operator|.
name|getRegionLoadsList
argument_list|()
operator|.
name|stream
argument_list|()
operator|.
name|map
argument_list|(
name|RegionMetricsBuilder
operator|::
name|toRegionMetrics
argument_list|)
operator|.
name|collect
argument_list|(
name|Collectors
operator|.
name|toList
argument_list|()
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|RegionMetrics
name|toRegionMetrics
parameter_list|(
name|ClusterStatusProtos
operator|.
name|RegionLoad
name|regionLoadPB
parameter_list|)
block|{
return|return
name|RegionMetricsBuilder
operator|.
name|newBuilder
argument_list|(
name|regionLoadPB
operator|.
name|getRegionSpecifier
argument_list|()
operator|.
name|getValue
argument_list|()
operator|.
name|toByteArray
argument_list|()
argument_list|)
operator|.
name|setBloomFilterSize
argument_list|(
operator|new
name|Size
argument_list|(
name|regionLoadPB
operator|.
name|getTotalStaticBloomSizeKB
argument_list|()
argument_list|,
name|Size
operator|.
name|Unit
operator|.
name|KILOBYTE
argument_list|)
argument_list|)
operator|.
name|setCompactedCellCount
argument_list|(
name|regionLoadPB
operator|.
name|getCurrentCompactedKVs
argument_list|()
argument_list|)
operator|.
name|setCompactingCellCount
argument_list|(
name|regionLoadPB
operator|.
name|getTotalCompactingKVs
argument_list|()
argument_list|)
operator|.
name|setCompletedSequenceId
argument_list|(
name|regionLoadPB
operator|.
name|getCompleteSequenceId
argument_list|()
argument_list|)
operator|.
name|setDataLocality
argument_list|(
name|regionLoadPB
operator|.
name|hasDataLocality
argument_list|()
condition|?
name|regionLoadPB
operator|.
name|getDataLocality
argument_list|()
else|:
literal|0.0f
argument_list|)
operator|.
name|setFilteredReadRequestCount
argument_list|(
name|regionLoadPB
operator|.
name|getFilteredReadRequestsCount
argument_list|()
argument_list|)
operator|.
name|setStoreFileUncompressedDataIndexSize
argument_list|(
operator|new
name|Size
argument_list|(
name|regionLoadPB
operator|.
name|getTotalStaticIndexSizeKB
argument_list|()
argument_list|,
name|Size
operator|.
name|Unit
operator|.
name|KILOBYTE
argument_list|)
argument_list|)
operator|.
name|setLastMajorCompactionTimestamp
argument_list|(
name|regionLoadPB
operator|.
name|getLastMajorCompactionTs
argument_list|()
argument_list|)
operator|.
name|setMemStoreSize
argument_list|(
operator|new
name|Size
argument_list|(
name|regionLoadPB
operator|.
name|getMemStoreSizeMB
argument_list|()
argument_list|,
name|Size
operator|.
name|Unit
operator|.
name|MEGABYTE
argument_list|)
argument_list|)
operator|.
name|setReadRequestCount
argument_list|(
name|regionLoadPB
operator|.
name|getReadRequestsCount
argument_list|()
argument_list|)
operator|.
name|setCpRequestCount
argument_list|(
name|regionLoadPB
operator|.
name|getCpRequestsCount
argument_list|()
argument_list|)
operator|.
name|setWriteRequestCount
argument_list|(
name|regionLoadPB
operator|.
name|getWriteRequestsCount
argument_list|()
argument_list|)
operator|.
name|setStoreFileIndexSize
argument_list|(
operator|new
name|Size
argument_list|(
name|regionLoadPB
operator|.
name|getStorefileIndexSizeKB
argument_list|()
argument_list|,
name|Size
operator|.
name|Unit
operator|.
name|KILOBYTE
argument_list|)
argument_list|)
operator|.
name|setStoreFileRootLevelIndexSize
argument_list|(
operator|new
name|Size
argument_list|(
name|regionLoadPB
operator|.
name|getRootIndexSizeKB
argument_list|()
argument_list|,
name|Size
operator|.
name|Unit
operator|.
name|KILOBYTE
argument_list|)
argument_list|)
operator|.
name|setStoreCount
argument_list|(
name|regionLoadPB
operator|.
name|getStores
argument_list|()
argument_list|)
operator|.
name|setStoreFileCount
argument_list|(
name|regionLoadPB
operator|.
name|getStorefiles
argument_list|()
argument_list|)
operator|.
name|setStoreRefCount
argument_list|(
name|regionLoadPB
operator|.
name|getStoreRefCount
argument_list|()
argument_list|)
operator|.
name|setMaxCompactedStoreFileRefCount
argument_list|(
name|regionLoadPB
operator|.
name|getMaxCompactedStoreFileRefCount
argument_list|()
argument_list|)
operator|.
name|setStoreFileSize
argument_list|(
operator|new
name|Size
argument_list|(
name|regionLoadPB
operator|.
name|getStorefileSizeMB
argument_list|()
argument_list|,
name|Size
operator|.
name|Unit
operator|.
name|MEGABYTE
argument_list|)
argument_list|)
operator|.
name|setStoreSequenceIds
argument_list|(
name|regionLoadPB
operator|.
name|getStoreCompleteSequenceIdList
argument_list|()
operator|.
name|stream
argument_list|()
operator|.
name|collect
argument_list|(
name|Collectors
operator|.
name|toMap
argument_list|(
parameter_list|(
name|ClusterStatusProtos
operator|.
name|StoreSequenceId
name|s
parameter_list|)
lambda|->
name|s
operator|.
name|getFamilyName
argument_list|()
operator|.
name|toByteArray
argument_list|()
argument_list|,
name|ClusterStatusProtos
operator|.
name|StoreSequenceId
operator|::
name|getSequenceId
argument_list|)
argument_list|)
argument_list|)
operator|.
name|setUncompressedStoreFileSize
argument_list|(
operator|new
name|Size
argument_list|(
name|regionLoadPB
operator|.
name|getStoreUncompressedSizeMB
argument_list|()
argument_list|,
name|Size
operator|.
name|Unit
operator|.
name|MEGABYTE
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
return|;
block|}
specifier|private
specifier|static
name|List
argument_list|<
name|ClusterStatusProtos
operator|.
name|StoreSequenceId
argument_list|>
name|toStoreSequenceId
parameter_list|(
name|Map
argument_list|<
name|byte
index|[]
argument_list|,
name|Long
argument_list|>
name|ids
parameter_list|)
block|{
return|return
name|ids
operator|.
name|entrySet
argument_list|()
operator|.
name|stream
argument_list|()
operator|.
name|map
argument_list|(
name|e
lambda|->
name|ClusterStatusProtos
operator|.
name|StoreSequenceId
operator|.
name|newBuilder
argument_list|()
operator|.
name|setFamilyName
argument_list|(
name|UnsafeByteOperations
operator|.
name|unsafeWrap
argument_list|(
name|e
operator|.
name|getKey
argument_list|()
argument_list|)
argument_list|)
operator|.
name|setSequenceId
argument_list|(
name|e
operator|.
name|getValue
argument_list|()
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
operator|.
name|collect
argument_list|(
name|Collectors
operator|.
name|toList
argument_list|()
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|ClusterStatusProtos
operator|.
name|RegionLoad
name|toRegionLoad
parameter_list|(
name|RegionMetrics
name|regionMetrics
parameter_list|)
block|{
return|return
name|ClusterStatusProtos
operator|.
name|RegionLoad
operator|.
name|newBuilder
argument_list|()
operator|.
name|setRegionSpecifier
argument_list|(
name|HBaseProtos
operator|.
name|RegionSpecifier
operator|.
name|newBuilder
argument_list|()
operator|.
name|setType
argument_list|(
name|HBaseProtos
operator|.
name|RegionSpecifier
operator|.
name|RegionSpecifierType
operator|.
name|REGION_NAME
argument_list|)
operator|.
name|setValue
argument_list|(
name|UnsafeByteOperations
operator|.
name|unsafeWrap
argument_list|(
name|regionMetrics
operator|.
name|getRegionName
argument_list|()
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
operator|.
name|setTotalStaticBloomSizeKB
argument_list|(
operator|(
name|int
operator|)
name|regionMetrics
operator|.
name|getBloomFilterSize
argument_list|()
operator|.
name|get
argument_list|(
name|Size
operator|.
name|Unit
operator|.
name|KILOBYTE
argument_list|)
argument_list|)
operator|.
name|setCurrentCompactedKVs
argument_list|(
name|regionMetrics
operator|.
name|getCompactedCellCount
argument_list|()
argument_list|)
operator|.
name|setTotalCompactingKVs
argument_list|(
name|regionMetrics
operator|.
name|getCompactingCellCount
argument_list|()
argument_list|)
operator|.
name|setCompleteSequenceId
argument_list|(
name|regionMetrics
operator|.
name|getCompletedSequenceId
argument_list|()
argument_list|)
operator|.
name|setDataLocality
argument_list|(
name|regionMetrics
operator|.
name|getDataLocality
argument_list|()
argument_list|)
operator|.
name|setFilteredReadRequestsCount
argument_list|(
name|regionMetrics
operator|.
name|getFilteredReadRequestCount
argument_list|()
argument_list|)
operator|.
name|setTotalStaticIndexSizeKB
argument_list|(
operator|(
name|int
operator|)
name|regionMetrics
operator|.
name|getStoreFileUncompressedDataIndexSize
argument_list|()
operator|.
name|get
argument_list|(
name|Size
operator|.
name|Unit
operator|.
name|KILOBYTE
argument_list|)
argument_list|)
operator|.
name|setLastMajorCompactionTs
argument_list|(
name|regionMetrics
operator|.
name|getLastMajorCompactionTimestamp
argument_list|()
argument_list|)
operator|.
name|setMemStoreSizeMB
argument_list|(
operator|(
name|int
operator|)
name|regionMetrics
operator|.
name|getMemStoreSize
argument_list|()
operator|.
name|get
argument_list|(
name|Size
operator|.
name|Unit
operator|.
name|MEGABYTE
argument_list|)
argument_list|)
operator|.
name|setReadRequestsCount
argument_list|(
name|regionMetrics
operator|.
name|getReadRequestCount
argument_list|()
argument_list|)
operator|.
name|setCpRequestsCount
argument_list|(
name|regionMetrics
operator|.
name|getCpRequestCount
argument_list|()
argument_list|)
operator|.
name|setWriteRequestsCount
argument_list|(
name|regionMetrics
operator|.
name|getWriteRequestCount
argument_list|()
argument_list|)
operator|.
name|setStorefileIndexSizeKB
argument_list|(
operator|(
name|long
operator|)
name|regionMetrics
operator|.
name|getStoreFileIndexSize
argument_list|()
operator|.
name|get
argument_list|(
name|Size
operator|.
name|Unit
operator|.
name|KILOBYTE
argument_list|)
argument_list|)
operator|.
name|setRootIndexSizeKB
argument_list|(
operator|(
name|int
operator|)
name|regionMetrics
operator|.
name|getStoreFileRootLevelIndexSize
argument_list|()
operator|.
name|get
argument_list|(
name|Size
operator|.
name|Unit
operator|.
name|KILOBYTE
argument_list|)
argument_list|)
operator|.
name|setStores
argument_list|(
name|regionMetrics
operator|.
name|getStoreCount
argument_list|()
argument_list|)
operator|.
name|setStorefiles
argument_list|(
name|regionMetrics
operator|.
name|getStoreFileCount
argument_list|()
argument_list|)
operator|.
name|setStoreRefCount
argument_list|(
name|regionMetrics
operator|.
name|getStoreRefCount
argument_list|()
argument_list|)
operator|.
name|setMaxCompactedStoreFileRefCount
argument_list|(
name|regionMetrics
operator|.
name|getMaxCompactedStoreFileRefCount
argument_list|()
argument_list|)
operator|.
name|setStorefileSizeMB
argument_list|(
operator|(
name|int
operator|)
name|regionMetrics
operator|.
name|getStoreFileSize
argument_list|()
operator|.
name|get
argument_list|(
name|Size
operator|.
name|Unit
operator|.
name|MEGABYTE
argument_list|)
argument_list|)
operator|.
name|addAllStoreCompleteSequenceId
argument_list|(
name|toStoreSequenceId
argument_list|(
name|regionMetrics
operator|.
name|getStoreSequenceId
argument_list|()
argument_list|)
argument_list|)
operator|.
name|setStoreUncompressedSizeMB
argument_list|(
operator|(
name|int
operator|)
name|regionMetrics
operator|.
name|getUncompressedStoreFileSize
argument_list|()
operator|.
name|get
argument_list|(
name|Size
operator|.
name|Unit
operator|.
name|MEGABYTE
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
return|;
block|}
specifier|public
specifier|static
name|RegionMetricsBuilder
name|newBuilder
parameter_list|(
name|byte
index|[]
name|name
parameter_list|)
block|{
return|return
operator|new
name|RegionMetricsBuilder
argument_list|(
name|name
argument_list|)
return|;
block|}
specifier|private
specifier|final
name|byte
index|[]
name|name
decl_stmt|;
specifier|private
name|int
name|storeCount
decl_stmt|;
specifier|private
name|int
name|storeFileCount
decl_stmt|;
specifier|private
name|int
name|storeRefCount
decl_stmt|;
specifier|private
name|int
name|maxCompactedStoreFileRefCount
decl_stmt|;
specifier|private
name|long
name|compactingCellCount
decl_stmt|;
specifier|private
name|long
name|compactedCellCount
decl_stmt|;
specifier|private
name|Size
name|storeFileSize
init|=
name|Size
operator|.
name|ZERO
decl_stmt|;
specifier|private
name|Size
name|memStoreSize
init|=
name|Size
operator|.
name|ZERO
decl_stmt|;
specifier|private
name|Size
name|indexSize
init|=
name|Size
operator|.
name|ZERO
decl_stmt|;
specifier|private
name|Size
name|rootLevelIndexSize
init|=
name|Size
operator|.
name|ZERO
decl_stmt|;
specifier|private
name|Size
name|uncompressedDataIndexSize
init|=
name|Size
operator|.
name|ZERO
decl_stmt|;
specifier|private
name|Size
name|bloomFilterSize
init|=
name|Size
operator|.
name|ZERO
decl_stmt|;
specifier|private
name|Size
name|uncompressedStoreFileSize
init|=
name|Size
operator|.
name|ZERO
decl_stmt|;
specifier|private
name|long
name|writeRequestCount
decl_stmt|;
specifier|private
name|long
name|readRequestCount
decl_stmt|;
specifier|private
name|long
name|cpRequestCount
decl_stmt|;
specifier|private
name|long
name|filteredReadRequestCount
decl_stmt|;
specifier|private
name|long
name|completedSequenceId
decl_stmt|;
specifier|private
name|Map
argument_list|<
name|byte
index|[]
argument_list|,
name|Long
argument_list|>
name|storeSequenceIds
init|=
name|Collections
operator|.
name|emptyMap
argument_list|()
decl_stmt|;
specifier|private
name|float
name|dataLocality
decl_stmt|;
specifier|private
name|long
name|lastMajorCompactionTimestamp
decl_stmt|;
specifier|private
name|RegionMetricsBuilder
parameter_list|(
name|byte
index|[]
name|name
parameter_list|)
block|{
name|this
operator|.
name|name
operator|=
name|name
expr_stmt|;
block|}
specifier|public
name|RegionMetricsBuilder
name|setStoreCount
parameter_list|(
name|int
name|value
parameter_list|)
block|{
name|this
operator|.
name|storeCount
operator|=
name|value
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|RegionMetricsBuilder
name|setStoreFileCount
parameter_list|(
name|int
name|value
parameter_list|)
block|{
name|this
operator|.
name|storeFileCount
operator|=
name|value
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|RegionMetricsBuilder
name|setStoreRefCount
parameter_list|(
name|int
name|value
parameter_list|)
block|{
name|this
operator|.
name|storeRefCount
operator|=
name|value
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|RegionMetricsBuilder
name|setMaxCompactedStoreFileRefCount
parameter_list|(
name|int
name|value
parameter_list|)
block|{
name|this
operator|.
name|maxCompactedStoreFileRefCount
operator|=
name|value
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|RegionMetricsBuilder
name|setCompactingCellCount
parameter_list|(
name|long
name|value
parameter_list|)
block|{
name|this
operator|.
name|compactingCellCount
operator|=
name|value
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|RegionMetricsBuilder
name|setCompactedCellCount
parameter_list|(
name|long
name|value
parameter_list|)
block|{
name|this
operator|.
name|compactedCellCount
operator|=
name|value
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|RegionMetricsBuilder
name|setStoreFileSize
parameter_list|(
name|Size
name|value
parameter_list|)
block|{
name|this
operator|.
name|storeFileSize
operator|=
name|value
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|RegionMetricsBuilder
name|setMemStoreSize
parameter_list|(
name|Size
name|value
parameter_list|)
block|{
name|this
operator|.
name|memStoreSize
operator|=
name|value
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|RegionMetricsBuilder
name|setStoreFileIndexSize
parameter_list|(
name|Size
name|value
parameter_list|)
block|{
name|this
operator|.
name|indexSize
operator|=
name|value
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|RegionMetricsBuilder
name|setStoreFileRootLevelIndexSize
parameter_list|(
name|Size
name|value
parameter_list|)
block|{
name|this
operator|.
name|rootLevelIndexSize
operator|=
name|value
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|RegionMetricsBuilder
name|setStoreFileUncompressedDataIndexSize
parameter_list|(
name|Size
name|value
parameter_list|)
block|{
name|this
operator|.
name|uncompressedDataIndexSize
operator|=
name|value
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|RegionMetricsBuilder
name|setBloomFilterSize
parameter_list|(
name|Size
name|value
parameter_list|)
block|{
name|this
operator|.
name|bloomFilterSize
operator|=
name|value
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|RegionMetricsBuilder
name|setUncompressedStoreFileSize
parameter_list|(
name|Size
name|value
parameter_list|)
block|{
name|this
operator|.
name|uncompressedStoreFileSize
operator|=
name|value
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|RegionMetricsBuilder
name|setWriteRequestCount
parameter_list|(
name|long
name|value
parameter_list|)
block|{
name|this
operator|.
name|writeRequestCount
operator|=
name|value
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|RegionMetricsBuilder
name|setReadRequestCount
parameter_list|(
name|long
name|value
parameter_list|)
block|{
name|this
operator|.
name|readRequestCount
operator|=
name|value
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|RegionMetricsBuilder
name|setCpRequestCount
parameter_list|(
name|long
name|value
parameter_list|)
block|{
name|this
operator|.
name|cpRequestCount
operator|=
name|value
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|RegionMetricsBuilder
name|setFilteredReadRequestCount
parameter_list|(
name|long
name|value
parameter_list|)
block|{
name|this
operator|.
name|filteredReadRequestCount
operator|=
name|value
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|RegionMetricsBuilder
name|setCompletedSequenceId
parameter_list|(
name|long
name|value
parameter_list|)
block|{
name|this
operator|.
name|completedSequenceId
operator|=
name|value
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|RegionMetricsBuilder
name|setStoreSequenceIds
parameter_list|(
name|Map
argument_list|<
name|byte
index|[]
argument_list|,
name|Long
argument_list|>
name|value
parameter_list|)
block|{
name|this
operator|.
name|storeSequenceIds
operator|=
name|value
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|RegionMetricsBuilder
name|setDataLocality
parameter_list|(
name|float
name|value
parameter_list|)
block|{
name|this
operator|.
name|dataLocality
operator|=
name|value
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|RegionMetricsBuilder
name|setLastMajorCompactionTimestamp
parameter_list|(
name|long
name|value
parameter_list|)
block|{
name|this
operator|.
name|lastMajorCompactionTimestamp
operator|=
name|value
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|RegionMetrics
name|build
parameter_list|()
block|{
return|return
operator|new
name|RegionMetricsImpl
argument_list|(
name|name
argument_list|,
name|storeCount
argument_list|,
name|storeFileCount
argument_list|,
name|storeRefCount
argument_list|,
name|maxCompactedStoreFileRefCount
argument_list|,
name|compactingCellCount
argument_list|,
name|compactedCellCount
argument_list|,
name|storeFileSize
argument_list|,
name|memStoreSize
argument_list|,
name|indexSize
argument_list|,
name|rootLevelIndexSize
argument_list|,
name|uncompressedDataIndexSize
argument_list|,
name|bloomFilterSize
argument_list|,
name|uncompressedStoreFileSize
argument_list|,
name|writeRequestCount
argument_list|,
name|readRequestCount
argument_list|,
name|cpRequestCount
argument_list|,
name|filteredReadRequestCount
argument_list|,
name|completedSequenceId
argument_list|,
name|storeSequenceIds
argument_list|,
name|dataLocality
argument_list|,
name|lastMajorCompactionTimestamp
argument_list|)
return|;
block|}
specifier|private
specifier|static
class|class
name|RegionMetricsImpl
implements|implements
name|RegionMetrics
block|{
specifier|private
specifier|final
name|byte
index|[]
name|name
decl_stmt|;
specifier|private
specifier|final
name|int
name|storeCount
decl_stmt|;
specifier|private
specifier|final
name|int
name|storeFileCount
decl_stmt|;
specifier|private
specifier|final
name|int
name|storeRefCount
decl_stmt|;
specifier|private
specifier|final
name|int
name|maxCompactedStoreFileRefCount
decl_stmt|;
specifier|private
specifier|final
name|long
name|compactingCellCount
decl_stmt|;
specifier|private
specifier|final
name|long
name|compactedCellCount
decl_stmt|;
specifier|private
specifier|final
name|Size
name|storeFileSize
decl_stmt|;
specifier|private
specifier|final
name|Size
name|memStoreSize
decl_stmt|;
specifier|private
specifier|final
name|Size
name|indexSize
decl_stmt|;
specifier|private
specifier|final
name|Size
name|rootLevelIndexSize
decl_stmt|;
specifier|private
specifier|final
name|Size
name|uncompressedDataIndexSize
decl_stmt|;
specifier|private
specifier|final
name|Size
name|bloomFilterSize
decl_stmt|;
specifier|private
specifier|final
name|Size
name|uncompressedStoreFileSize
decl_stmt|;
specifier|private
specifier|final
name|long
name|writeRequestCount
decl_stmt|;
specifier|private
specifier|final
name|long
name|readRequestCount
decl_stmt|;
specifier|private
specifier|final
name|long
name|cpRequestCount
decl_stmt|;
specifier|private
specifier|final
name|long
name|filteredReadRequestCount
decl_stmt|;
specifier|private
specifier|final
name|long
name|completedSequenceId
decl_stmt|;
specifier|private
specifier|final
name|Map
argument_list|<
name|byte
index|[]
argument_list|,
name|Long
argument_list|>
name|storeSequenceIds
decl_stmt|;
specifier|private
specifier|final
name|float
name|dataLocality
decl_stmt|;
specifier|private
specifier|final
name|long
name|lastMajorCompactionTimestamp
decl_stmt|;
name|RegionMetricsImpl
parameter_list|(
name|byte
index|[]
name|name
parameter_list|,
name|int
name|storeCount
parameter_list|,
name|int
name|storeFileCount
parameter_list|,
name|int
name|storeRefCount
parameter_list|,
name|int
name|maxCompactedStoreFileRefCount
parameter_list|,
specifier|final
name|long
name|compactingCellCount
parameter_list|,
name|long
name|compactedCellCount
parameter_list|,
name|Size
name|storeFileSize
parameter_list|,
name|Size
name|memStoreSize
parameter_list|,
name|Size
name|indexSize
parameter_list|,
name|Size
name|rootLevelIndexSize
parameter_list|,
name|Size
name|uncompressedDataIndexSize
parameter_list|,
name|Size
name|bloomFilterSize
parameter_list|,
name|Size
name|uncompressedStoreFileSize
parameter_list|,
name|long
name|writeRequestCount
parameter_list|,
name|long
name|readRequestCount
parameter_list|,
name|long
name|cpRequestCount
parameter_list|,
name|long
name|filteredReadRequestCount
parameter_list|,
name|long
name|completedSequenceId
parameter_list|,
name|Map
argument_list|<
name|byte
index|[]
argument_list|,
name|Long
argument_list|>
name|storeSequenceIds
parameter_list|,
name|float
name|dataLocality
parameter_list|,
name|long
name|lastMajorCompactionTimestamp
parameter_list|)
block|{
name|this
operator|.
name|name
operator|=
name|Preconditions
operator|.
name|checkNotNull
argument_list|(
name|name
argument_list|)
expr_stmt|;
name|this
operator|.
name|storeCount
operator|=
name|storeCount
expr_stmt|;
name|this
operator|.
name|storeFileCount
operator|=
name|storeFileCount
expr_stmt|;
name|this
operator|.
name|storeRefCount
operator|=
name|storeRefCount
expr_stmt|;
name|this
operator|.
name|maxCompactedStoreFileRefCount
operator|=
name|maxCompactedStoreFileRefCount
expr_stmt|;
name|this
operator|.
name|compactingCellCount
operator|=
name|compactingCellCount
expr_stmt|;
name|this
operator|.
name|compactedCellCount
operator|=
name|compactedCellCount
expr_stmt|;
name|this
operator|.
name|storeFileSize
operator|=
name|Preconditions
operator|.
name|checkNotNull
argument_list|(
name|storeFileSize
argument_list|)
expr_stmt|;
name|this
operator|.
name|memStoreSize
operator|=
name|Preconditions
operator|.
name|checkNotNull
argument_list|(
name|memStoreSize
argument_list|)
expr_stmt|;
name|this
operator|.
name|indexSize
operator|=
name|Preconditions
operator|.
name|checkNotNull
argument_list|(
name|indexSize
argument_list|)
expr_stmt|;
name|this
operator|.
name|rootLevelIndexSize
operator|=
name|Preconditions
operator|.
name|checkNotNull
argument_list|(
name|rootLevelIndexSize
argument_list|)
expr_stmt|;
name|this
operator|.
name|uncompressedDataIndexSize
operator|=
name|Preconditions
operator|.
name|checkNotNull
argument_list|(
name|uncompressedDataIndexSize
argument_list|)
expr_stmt|;
name|this
operator|.
name|bloomFilterSize
operator|=
name|Preconditions
operator|.
name|checkNotNull
argument_list|(
name|bloomFilterSize
argument_list|)
expr_stmt|;
name|this
operator|.
name|uncompressedStoreFileSize
operator|=
name|Preconditions
operator|.
name|checkNotNull
argument_list|(
name|uncompressedStoreFileSize
argument_list|)
expr_stmt|;
name|this
operator|.
name|writeRequestCount
operator|=
name|writeRequestCount
expr_stmt|;
name|this
operator|.
name|readRequestCount
operator|=
name|readRequestCount
expr_stmt|;
name|this
operator|.
name|cpRequestCount
operator|=
name|cpRequestCount
expr_stmt|;
name|this
operator|.
name|filteredReadRequestCount
operator|=
name|filteredReadRequestCount
expr_stmt|;
name|this
operator|.
name|completedSequenceId
operator|=
name|completedSequenceId
expr_stmt|;
name|this
operator|.
name|storeSequenceIds
operator|=
name|Preconditions
operator|.
name|checkNotNull
argument_list|(
name|storeSequenceIds
argument_list|)
expr_stmt|;
name|this
operator|.
name|dataLocality
operator|=
name|dataLocality
expr_stmt|;
name|this
operator|.
name|lastMajorCompactionTimestamp
operator|=
name|lastMajorCompactionTimestamp
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|byte
index|[]
name|getRegionName
parameter_list|()
block|{
return|return
name|name
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getStoreCount
parameter_list|()
block|{
return|return
name|storeCount
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getStoreFileCount
parameter_list|()
block|{
return|return
name|storeFileCount
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getStoreRefCount
parameter_list|()
block|{
return|return
name|storeRefCount
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getMaxCompactedStoreFileRefCount
parameter_list|()
block|{
return|return
name|maxCompactedStoreFileRefCount
return|;
block|}
annotation|@
name|Override
specifier|public
name|Size
name|getStoreFileSize
parameter_list|()
block|{
return|return
name|storeFileSize
return|;
block|}
annotation|@
name|Override
specifier|public
name|Size
name|getMemStoreSize
parameter_list|()
block|{
return|return
name|memStoreSize
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getReadRequestCount
parameter_list|()
block|{
return|return
name|readRequestCount
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getCpRequestCount
parameter_list|()
block|{
return|return
name|cpRequestCount
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getFilteredReadRequestCount
parameter_list|()
block|{
return|return
name|filteredReadRequestCount
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getWriteRequestCount
parameter_list|()
block|{
return|return
name|writeRequestCount
return|;
block|}
annotation|@
name|Override
specifier|public
name|Size
name|getStoreFileIndexSize
parameter_list|()
block|{
return|return
name|indexSize
return|;
block|}
annotation|@
name|Override
specifier|public
name|Size
name|getStoreFileRootLevelIndexSize
parameter_list|()
block|{
return|return
name|rootLevelIndexSize
return|;
block|}
annotation|@
name|Override
specifier|public
name|Size
name|getStoreFileUncompressedDataIndexSize
parameter_list|()
block|{
return|return
name|uncompressedDataIndexSize
return|;
block|}
annotation|@
name|Override
specifier|public
name|Size
name|getBloomFilterSize
parameter_list|()
block|{
return|return
name|bloomFilterSize
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getCompactingCellCount
parameter_list|()
block|{
return|return
name|compactingCellCount
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getCompactedCellCount
parameter_list|()
block|{
return|return
name|compactedCellCount
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getCompletedSequenceId
parameter_list|()
block|{
return|return
name|completedSequenceId
return|;
block|}
annotation|@
name|Override
specifier|public
name|Map
argument_list|<
name|byte
index|[]
argument_list|,
name|Long
argument_list|>
name|getStoreSequenceId
parameter_list|()
block|{
return|return
name|Collections
operator|.
name|unmodifiableMap
argument_list|(
name|storeSequenceIds
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|Size
name|getUncompressedStoreFileSize
parameter_list|()
block|{
return|return
name|uncompressedStoreFileSize
return|;
block|}
annotation|@
name|Override
specifier|public
name|float
name|getDataLocality
parameter_list|()
block|{
return|return
name|dataLocality
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getLastMajorCompactionTimestamp
parameter_list|()
block|{
return|return
name|lastMajorCompactionTimestamp
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
name|StringBuilder
name|sb
init|=
name|Strings
operator|.
name|appendKeyValue
argument_list|(
operator|new
name|StringBuilder
argument_list|()
argument_list|,
literal|"storeCount"
argument_list|,
name|this
operator|.
name|getStoreCount
argument_list|()
argument_list|)
decl_stmt|;
name|Strings
operator|.
name|appendKeyValue
argument_list|(
name|sb
argument_list|,
literal|"storeFileCount"
argument_list|,
name|this
operator|.
name|getStoreFileCount
argument_list|()
argument_list|)
expr_stmt|;
name|Strings
operator|.
name|appendKeyValue
argument_list|(
name|sb
argument_list|,
literal|"storeRefCount"
argument_list|,
name|this
operator|.
name|getStoreRefCount
argument_list|()
argument_list|)
expr_stmt|;
name|Strings
operator|.
name|appendKeyValue
argument_list|(
name|sb
argument_list|,
literal|"maxCompactedStoreFileRefCount"
argument_list|,
name|this
operator|.
name|getMaxCompactedStoreFileRefCount
argument_list|()
argument_list|)
expr_stmt|;
name|Strings
operator|.
name|appendKeyValue
argument_list|(
name|sb
argument_list|,
literal|"uncompressedStoreFileSize"
argument_list|,
name|this
operator|.
name|getUncompressedStoreFileSize
argument_list|()
argument_list|)
expr_stmt|;
name|Strings
operator|.
name|appendKeyValue
argument_list|(
name|sb
argument_list|,
literal|"lastMajorCompactionTimestamp"
argument_list|,
name|this
operator|.
name|getLastMajorCompactionTimestamp
argument_list|()
argument_list|)
expr_stmt|;
name|Strings
operator|.
name|appendKeyValue
argument_list|(
name|sb
argument_list|,
literal|"storeFileSize"
argument_list|,
name|this
operator|.
name|getStoreFileSize
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|this
operator|.
name|getUncompressedStoreFileSize
argument_list|()
operator|.
name|get
argument_list|()
operator|!=
literal|0
condition|)
block|{
name|Strings
operator|.
name|appendKeyValue
argument_list|(
name|sb
argument_list|,
literal|"compressionRatio"
argument_list|,
name|String
operator|.
name|format
argument_list|(
literal|"%.4f"
argument_list|,
operator|(
name|float
operator|)
name|this
operator|.
name|getStoreFileSize
argument_list|()
operator|.
name|get
argument_list|(
name|Size
operator|.
name|Unit
operator|.
name|MEGABYTE
argument_list|)
operator|/
operator|(
name|float
operator|)
name|this
operator|.
name|getUncompressedStoreFileSize
argument_list|()
operator|.
name|get
argument_list|(
name|Size
operator|.
name|Unit
operator|.
name|MEGABYTE
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|Strings
operator|.
name|appendKeyValue
argument_list|(
name|sb
argument_list|,
literal|"memStoreSize"
argument_list|,
name|this
operator|.
name|getMemStoreSize
argument_list|()
argument_list|)
expr_stmt|;
name|Strings
operator|.
name|appendKeyValue
argument_list|(
name|sb
argument_list|,
literal|"readRequestCount"
argument_list|,
name|this
operator|.
name|getReadRequestCount
argument_list|()
argument_list|)
expr_stmt|;
name|Strings
operator|.
name|appendKeyValue
argument_list|(
name|sb
argument_list|,
literal|"cpRequestCount"
argument_list|,
name|this
operator|.
name|getCpRequestCount
argument_list|()
argument_list|)
expr_stmt|;
name|Strings
operator|.
name|appendKeyValue
argument_list|(
name|sb
argument_list|,
literal|"writeRequestCount"
argument_list|,
name|this
operator|.
name|getWriteRequestCount
argument_list|()
argument_list|)
expr_stmt|;
name|Strings
operator|.
name|appendKeyValue
argument_list|(
name|sb
argument_list|,
literal|"rootLevelIndexSize"
argument_list|,
name|this
operator|.
name|getStoreFileRootLevelIndexSize
argument_list|()
argument_list|)
expr_stmt|;
name|Strings
operator|.
name|appendKeyValue
argument_list|(
name|sb
argument_list|,
literal|"uncompressedDataIndexSize"
argument_list|,
name|this
operator|.
name|getStoreFileUncompressedDataIndexSize
argument_list|()
argument_list|)
expr_stmt|;
name|Strings
operator|.
name|appendKeyValue
argument_list|(
name|sb
argument_list|,
literal|"bloomFilterSize"
argument_list|,
name|this
operator|.
name|getBloomFilterSize
argument_list|()
argument_list|)
expr_stmt|;
name|Strings
operator|.
name|appendKeyValue
argument_list|(
name|sb
argument_list|,
literal|"compactingCellCount"
argument_list|,
name|this
operator|.
name|getCompactingCellCount
argument_list|()
argument_list|)
expr_stmt|;
name|Strings
operator|.
name|appendKeyValue
argument_list|(
name|sb
argument_list|,
literal|"compactedCellCount"
argument_list|,
name|this
operator|.
name|getCompactedCellCount
argument_list|()
argument_list|)
expr_stmt|;
name|float
name|compactionProgressPct
init|=
name|Float
operator|.
name|NaN
decl_stmt|;
if|if
condition|(
name|this
operator|.
name|getCompactingCellCount
argument_list|()
operator|>
literal|0
condition|)
block|{
name|compactionProgressPct
operator|=
operator|(
operator|(
name|float
operator|)
name|this
operator|.
name|getCompactedCellCount
argument_list|()
operator|/
operator|(
name|float
operator|)
name|this
operator|.
name|getCompactingCellCount
argument_list|()
operator|)
expr_stmt|;
block|}
name|Strings
operator|.
name|appendKeyValue
argument_list|(
name|sb
argument_list|,
literal|"compactionProgressPct"
argument_list|,
name|compactionProgressPct
argument_list|)
expr_stmt|;
name|Strings
operator|.
name|appendKeyValue
argument_list|(
name|sb
argument_list|,
literal|"completedSequenceId"
argument_list|,
name|this
operator|.
name|getCompletedSequenceId
argument_list|()
argument_list|)
expr_stmt|;
name|Strings
operator|.
name|appendKeyValue
argument_list|(
name|sb
argument_list|,
literal|"dataLocality"
argument_list|,
name|this
operator|.
name|getDataLocality
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|sb
operator|.
name|toString
argument_list|()
return|;
block|}
block|}
block|}
end_class

end_unit

