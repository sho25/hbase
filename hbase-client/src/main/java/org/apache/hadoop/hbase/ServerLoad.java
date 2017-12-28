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
name|Arrays
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
name|Set
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|TreeMap
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
name|replication
operator|.
name|ReplicationLoadSink
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
name|replication
operator|.
name|ReplicationLoadSource
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
name|Objects
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

begin_comment
comment|/**  * This class is used for exporting current state of load on a RegionServer.  *  * @deprecated As of release 2.0.0, this will be removed in HBase 3.0.0  *             Use {@link ServerMetrics} instead.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Public
annotation|@
name|Deprecated
specifier|public
class|class
name|ServerLoad
implements|implements
name|ServerMetrics
block|{
specifier|private
specifier|final
name|ServerMetrics
name|metrics
decl_stmt|;
specifier|private
name|int
name|stores
init|=
literal|0
decl_stmt|;
specifier|private
name|int
name|storefiles
init|=
literal|0
decl_stmt|;
specifier|private
name|int
name|storeUncompressedSizeMB
init|=
literal|0
decl_stmt|;
specifier|private
name|int
name|storefileSizeMB
init|=
literal|0
decl_stmt|;
specifier|private
name|int
name|memstoreSizeMB
init|=
literal|0
decl_stmt|;
specifier|private
name|long
name|storefileIndexSizeKB
init|=
literal|0
decl_stmt|;
specifier|private
name|long
name|readRequestsCount
init|=
literal|0
decl_stmt|;
specifier|private
name|long
name|filteredReadRequestsCount
init|=
literal|0
decl_stmt|;
specifier|private
name|long
name|writeRequestsCount
init|=
literal|0
decl_stmt|;
specifier|private
name|int
name|rootIndexSizeKB
init|=
literal|0
decl_stmt|;
specifier|private
name|int
name|totalStaticIndexSizeKB
init|=
literal|0
decl_stmt|;
specifier|private
name|int
name|totalStaticBloomSizeKB
init|=
literal|0
decl_stmt|;
specifier|private
name|long
name|totalCompactingKVs
init|=
literal|0
decl_stmt|;
specifier|private
name|long
name|currentCompactedKVs
init|=
literal|0
decl_stmt|;
comment|/**    * DONT USE this construction. It make a fake server name;    */
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
name|ServerLoad
parameter_list|(
name|ClusterStatusProtos
operator|.
name|ServerLoad
name|serverLoad
parameter_list|)
block|{
name|this
argument_list|(
name|ServerName
operator|.
name|valueOf
argument_list|(
literal|"localhost,1,1"
argument_list|)
argument_list|,
name|serverLoad
argument_list|)
expr_stmt|;
block|}
annotation|@
name|edu
operator|.
name|umd
operator|.
name|cs
operator|.
name|findbugs
operator|.
name|annotations
operator|.
name|SuppressWarnings
argument_list|(
name|value
operator|=
literal|"URF_UNREAD_PUBLIC_OR_PROTECTED_FIELD"
argument_list|)
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
name|ServerLoad
parameter_list|(
name|ServerName
name|name
parameter_list|,
name|ClusterStatusProtos
operator|.
name|ServerLoad
name|serverLoad
parameter_list|)
block|{
name|this
argument_list|(
name|ServerMetricsBuilder
operator|.
name|toServerMetrics
argument_list|(
name|name
argument_list|,
name|serverLoad
argument_list|)
argument_list|)
expr_stmt|;
name|this
operator|.
name|serverLoad
operator|=
name|serverLoad
expr_stmt|;
block|}
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
name|ServerLoad
parameter_list|(
name|ServerMetrics
name|metrics
parameter_list|)
block|{
name|this
operator|.
name|metrics
operator|=
name|metrics
expr_stmt|;
name|this
operator|.
name|serverLoad
operator|=
name|ServerMetricsBuilder
operator|.
name|toServerLoad
argument_list|(
name|metrics
argument_list|)
expr_stmt|;
for|for
control|(
name|RegionMetrics
name|rl
range|:
name|metrics
operator|.
name|getRegionMetrics
argument_list|()
operator|.
name|values
argument_list|()
control|)
block|{
name|stores
operator|+=
name|rl
operator|.
name|getStoreCount
argument_list|()
expr_stmt|;
name|storefiles
operator|+=
name|rl
operator|.
name|getStoreFileCount
argument_list|()
expr_stmt|;
name|storeUncompressedSizeMB
operator|+=
name|rl
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
expr_stmt|;
name|storefileSizeMB
operator|+=
name|rl
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
expr_stmt|;
name|memstoreSizeMB
operator|+=
name|rl
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
expr_stmt|;
name|readRequestsCount
operator|+=
name|rl
operator|.
name|getReadRequestCount
argument_list|()
expr_stmt|;
name|filteredReadRequestsCount
operator|+=
name|rl
operator|.
name|getFilteredReadRequestCount
argument_list|()
expr_stmt|;
name|writeRequestsCount
operator|+=
name|rl
operator|.
name|getWriteRequestCount
argument_list|()
expr_stmt|;
name|storefileIndexSizeKB
operator|+=
name|rl
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
expr_stmt|;
name|rootIndexSizeKB
operator|+=
name|rl
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
expr_stmt|;
name|totalStaticIndexSizeKB
operator|+=
name|rl
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
expr_stmt|;
name|totalStaticBloomSizeKB
operator|+=
name|rl
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
expr_stmt|;
name|totalCompactingKVs
operator|+=
name|rl
operator|.
name|getCompactingCellCount
argument_list|()
expr_stmt|;
name|currentCompactedKVs
operator|+=
name|rl
operator|.
name|getCompactedCellCount
argument_list|()
expr_stmt|;
block|}
block|}
comment|/**    * NOTE: Function name cannot start with "get" because then an OpenDataException is thrown because    * HBaseProtos.ServerLoad cannot be converted to an open data type(see HBASE-5967).    * @return the underlying ServerLoad protobuf object    * @deprecated DONT use this pb object since the byte array backed may be modified in rpc layer    */
annotation|@
name|InterfaceAudience
operator|.
name|Private
annotation|@
name|Deprecated
specifier|public
name|ClusterStatusProtos
operator|.
name|ServerLoad
name|obtainServerLoadPB
parameter_list|()
block|{
return|return
name|serverLoad
return|;
block|}
specifier|protected
name|ClusterStatusProtos
operator|.
name|ServerLoad
name|serverLoad
decl_stmt|;
comment|/**    * @return number of requests  since last report.    * @deprecated As of release 2.0.0, this will be removed in HBase 3.0.0.    *             Use {@link #getRequestCountPerSecond} instead.    */
annotation|@
name|Deprecated
specifier|public
name|long
name|getNumberOfRequests
parameter_list|()
block|{
return|return
name|getRequestCountPerSecond
argument_list|()
return|;
block|}
comment|/**    * @deprecated As of release 2.0.0, this will be removed in HBase 3.0.0    *             No flag in 2.0    */
annotation|@
name|Deprecated
specifier|public
name|boolean
name|hasNumberOfRequests
parameter_list|()
block|{
return|return
literal|true
return|;
block|}
comment|/**    * @return total Number of requests from the start of the region server.    * @deprecated As of release 2.0.0, this will be removed in HBase 3.0.0.    *             Use {@link #getRequestCount} instead.    */
annotation|@
name|Deprecated
specifier|public
name|long
name|getTotalNumberOfRequests
parameter_list|()
block|{
return|return
name|getRequestCount
argument_list|()
return|;
block|}
comment|/**    * @deprecated As of release 2.0.0, this will be removed in HBase 3.0.0    *             No flag in 2.0    */
annotation|@
name|Deprecated
specifier|public
name|boolean
name|hasTotalNumberOfRequests
parameter_list|()
block|{
return|return
literal|true
return|;
block|}
comment|/**    * @return the amount of used heap, in MB.    * @deprecated As of release 2.0.0, this will be removed in HBase 3.0.0.    *             Use {@link #getUsedHeapSize} instead.    */
annotation|@
name|Deprecated
specifier|public
name|int
name|getUsedHeapMB
parameter_list|()
block|{
return|return
operator|(
name|int
operator|)
name|getUsedHeapSize
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
return|;
block|}
comment|/**    * @deprecated As of release 2.0.0, this will be removed in HBase 3.0.0    *             No flag in 2.0    */
annotation|@
name|Deprecated
specifier|public
name|boolean
name|hasUsedHeapMB
parameter_list|()
block|{
return|return
literal|true
return|;
block|}
comment|/**    * @return the maximum allowable size of the heap, in MB.    * @deprecated As of release 2.0.0, this will be removed in HBase 3.0.0    *             Use {@link #getMaxHeapSize} instead.    */
annotation|@
name|Deprecated
specifier|public
name|int
name|getMaxHeapMB
parameter_list|()
block|{
return|return
operator|(
name|int
operator|)
name|getMaxHeapSize
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
return|;
block|}
comment|/**    * @deprecated As of release 2.0.0, this will be removed in HBase 3.0.0    *             No flag in 2.0    */
annotation|@
name|Deprecated
specifier|public
name|boolean
name|hasMaxHeapMB
parameter_list|()
block|{
return|return
literal|true
return|;
block|}
comment|/**    * @deprecated As of release 2.0.0, this will be removed in HBase 3.0.0    *             Use {@link #getRegionMetrics} instead.    */
annotation|@
name|Deprecated
specifier|public
name|int
name|getStores
parameter_list|()
block|{
return|return
name|stores
return|;
block|}
comment|/**    * @deprecated As of release 2.0.0, this will be removed in HBase 3.0.0.    *             Use {@link #getRegionMetrics} instead.    */
annotation|@
name|Deprecated
specifier|public
name|int
name|getStorefiles
parameter_list|()
block|{
return|return
name|storefiles
return|;
block|}
comment|/**    * @deprecated As of release 2.0.0, this will be removed in HBase 3.0.0    *             Use {@link #getRegionMetrics} instead.    */
annotation|@
name|Deprecated
specifier|public
name|int
name|getStoreUncompressedSizeMB
parameter_list|()
block|{
return|return
name|storeUncompressedSizeMB
return|;
block|}
comment|/**    * @deprecated As of release 2.0.0, this will be removed in HBase 3.0.0    *             Use {@link #getRegionMetrics} instead.    */
annotation|@
name|Deprecated
specifier|public
name|int
name|getStorefileSizeInMB
parameter_list|()
block|{
return|return
name|storefileSizeMB
return|;
block|}
comment|/**    * @deprecated As of release 2.0.0, this will be removed in HBase 3.0.0    *             Use {@link #getRegionMetrics} instead.    */
annotation|@
name|Deprecated
specifier|public
name|int
name|getStorefileSizeMB
parameter_list|()
block|{
return|return
name|storefileSizeMB
return|;
block|}
comment|/**    * @deprecated As of release 2.0.0, this will be removed in HBase 3.0.0    *             Use {@link #getRegionMetrics} instead.    */
annotation|@
name|Deprecated
specifier|public
name|int
name|getMemstoreSizeInMB
parameter_list|()
block|{
return|return
name|memstoreSizeMB
return|;
block|}
comment|/**    * @deprecated As of release 2.0.0, this will be removed in HBase 3.0.0    *     Use {@link #getRegionMetrics} instead.    */
annotation|@
name|Deprecated
specifier|public
name|int
name|getMemStoreSizeMB
parameter_list|()
block|{
return|return
name|memstoreSizeMB
return|;
block|}
comment|/**    * @deprecated As of release 2.0.0, this will be removed in HBase 3.0.0    *     Use {@link #getRegionMetrics} instead.    */
annotation|@
name|Deprecated
specifier|public
name|int
name|getStorefileIndexSizeInMB
parameter_list|()
block|{
comment|// Return value divided by 1024
return|return
call|(
name|int
call|)
argument_list|(
name|getStorefileIndexSizeKB
argument_list|()
operator|>>
literal|10
argument_list|)
return|;
block|}
comment|/**    * @deprecated As of release 2.0.0, this will be removed in HBase 3.0.0    *     Use {@link #getRegionMetrics} instead.    */
annotation|@
name|Deprecated
specifier|public
name|long
name|getStorefileIndexSizeKB
parameter_list|()
block|{
return|return
name|storefileIndexSizeKB
return|;
block|}
comment|/**    * @deprecated As of release 2.0.0, this will be removed in HBase 3.0.0    *     Use {@link #getRegionMetrics} instead.    */
annotation|@
name|Deprecated
specifier|public
name|long
name|getReadRequestsCount
parameter_list|()
block|{
return|return
name|readRequestsCount
return|;
block|}
comment|/**    * @deprecated As of release 2.0.0, this will be removed in HBase 3.0.0    *     Use {@link #getRegionMetrics} instead.    */
annotation|@
name|Deprecated
specifier|public
name|long
name|getFilteredReadRequestsCount
parameter_list|()
block|{
return|return
name|filteredReadRequestsCount
return|;
block|}
comment|/**    * @deprecated As of release 2.0.0, this will be removed in HBase 3.0.0    *     Use {@link #getRegionMetrics} instead.    */
annotation|@
name|Deprecated
specifier|public
name|long
name|getWriteRequestsCount
parameter_list|()
block|{
return|return
name|writeRequestsCount
return|;
block|}
comment|/**    * @deprecated As of release 2.0.0, this will be removed in HBase 3.0.0    *     Use {@link #getRegionMetrics} instead.    */
annotation|@
name|Deprecated
specifier|public
name|int
name|getRootIndexSizeKB
parameter_list|()
block|{
return|return
name|rootIndexSizeKB
return|;
block|}
comment|/**    * @deprecated As of release 2.0.0, this will be removed in HBase 3.0.0    *     Use {@link #getRegionMetrics} instead.    */
annotation|@
name|Deprecated
specifier|public
name|int
name|getTotalStaticIndexSizeKB
parameter_list|()
block|{
return|return
name|totalStaticIndexSizeKB
return|;
block|}
comment|/**    * @deprecated As of release 2.0.0, this will be removed in HBase 3.0.0    *     Use {@link #getRegionMetrics} instead.    */
annotation|@
name|Deprecated
specifier|public
name|int
name|getTotalStaticBloomSizeKB
parameter_list|()
block|{
return|return
name|totalStaticBloomSizeKB
return|;
block|}
comment|/**    * @deprecated As of release 2.0.0, this will be removed in HBase 3.0.0    *     Use {@link #getRegionMetrics} instead.    */
annotation|@
name|Deprecated
specifier|public
name|long
name|getTotalCompactingKVs
parameter_list|()
block|{
return|return
name|totalCompactingKVs
return|;
block|}
comment|/**    * @deprecated As of release 2.0.0, this will be removed in HBase 3.0.0    *     Use {@link #getRegionMetrics} instead.    */
annotation|@
name|Deprecated
specifier|public
name|long
name|getCurrentCompactedKVs
parameter_list|()
block|{
return|return
name|currentCompactedKVs
return|;
block|}
comment|/**    * @deprecated As of release 2.0.0, this will be removed in HBase 3.0.0    *             Use {@link #getRegionMetrics} instead.    */
annotation|@
name|Deprecated
specifier|public
name|int
name|getNumberOfRegions
parameter_list|()
block|{
return|return
name|metrics
operator|.
name|getRegionMetrics
argument_list|()
operator|.
name|size
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|ServerName
name|getServerName
parameter_list|()
block|{
return|return
name|metrics
operator|.
name|getServerName
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getRequestCountPerSecond
parameter_list|()
block|{
return|return
name|metrics
operator|.
name|getRequestCountPerSecond
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getRequestCount
parameter_list|()
block|{
return|return
name|metrics
operator|.
name|getRequestCount
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|Size
name|getUsedHeapSize
parameter_list|()
block|{
return|return
name|metrics
operator|.
name|getUsedHeapSize
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|Size
name|getMaxHeapSize
parameter_list|()
block|{
return|return
name|metrics
operator|.
name|getMaxHeapSize
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getInfoServerPort
parameter_list|()
block|{
return|return
name|metrics
operator|.
name|getInfoServerPort
argument_list|()
return|;
block|}
comment|/**    * Call directly from client such as hbase shell    * @return the list of ReplicationLoadSource    */
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|ReplicationLoadSource
argument_list|>
name|getReplicationLoadSourceList
parameter_list|()
block|{
return|return
name|metrics
operator|.
name|getReplicationLoadSourceList
argument_list|()
return|;
block|}
comment|/**    * Call directly from client such as hbase shell    * @return ReplicationLoadSink    */
annotation|@
name|Override
specifier|public
name|ReplicationLoadSink
name|getReplicationLoadSink
parameter_list|()
block|{
return|return
name|metrics
operator|.
name|getReplicationLoadSink
argument_list|()
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
name|RegionMetrics
argument_list|>
name|getRegionMetrics
parameter_list|()
block|{
return|return
name|metrics
operator|.
name|getRegionMetrics
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|Set
argument_list|<
name|String
argument_list|>
name|getCoprocessorNames
parameter_list|()
block|{
return|return
name|metrics
operator|.
name|getCoprocessorNames
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getReportTimestamp
parameter_list|()
block|{
return|return
name|metrics
operator|.
name|getReportTimestamp
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getLastReportTimestamp
parameter_list|()
block|{
return|return
name|metrics
operator|.
name|getLastReportTimestamp
argument_list|()
return|;
block|}
comment|/**    * Originally, this method factored in the effect of requests going to the    * server as well. However, this does not interact very well with the current    * region rebalancing code, which only factors number of regions. For the    * interim, until we can figure out how to make rebalancing use all the info    * available, we're just going to make load purely the number of regions.    *    * @return load factor for this server.    * @deprecated As of release 2.0.0, this will be removed in HBase 3.0.0    *             Use {@link #getNumberOfRegions} instead.    */
annotation|@
name|Deprecated
specifier|public
name|int
name|getLoad
parameter_list|()
block|{
comment|// See above comment
comment|// int load = numberOfRequests == 0 ? 1 : numberOfRequests;
comment|// load *= numberOfRegions == 0 ? 1 : numberOfRegions;
comment|// return load;
return|return
name|getNumberOfRegions
argument_list|()
return|;
block|}
comment|/**    * @deprecated As of release 2.0.0, this will be removed in HBase 3.0.0    *             Use {@link #getRegionMetrics} instead.    */
annotation|@
name|Deprecated
specifier|public
name|Map
argument_list|<
name|byte
index|[]
argument_list|,
name|RegionLoad
argument_list|>
name|getRegionsLoad
parameter_list|()
block|{
return|return
name|getRegionMetrics
argument_list|()
operator|.
name|entrySet
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
name|Map
operator|.
name|Entry
operator|::
name|getKey
argument_list|,
name|e
lambda|->
operator|new
name|RegionLoad
argument_list|(
name|e
operator|.
name|getValue
argument_list|()
argument_list|)
argument_list|,
parameter_list|(
name|v1
parameter_list|,
name|v2
parameter_list|)
lambda|->
block|{
throw|throw
argument_list|new
name|RuntimeException
argument_list|(
literal|"key collisions?"
argument_list|)
argument_list|;
block|}
operator|,
parameter_list|()
lambda|->
operator|new
name|TreeMap
argument_list|(
name|Bytes
operator|.
name|BYTES_COMPARATOR
argument_list|)
block|)
end_class

begin_empty_stmt
unit|)
empty_stmt|;
end_empty_stmt

begin_comment
unit|}
comment|/**    * @deprecated As of release 2.0.0, this will be removed in HBase 3.0.0    *             Use {@link #getCoprocessorNames} instead.    */
end_comment

begin_function
unit|@
name|Deprecated
specifier|public
name|String
index|[]
name|getRegionServerCoprocessors
parameter_list|()
block|{
return|return
name|getCoprocessorNames
argument_list|()
operator|.
name|toArray
argument_list|(
operator|new
name|String
index|[
name|getCoprocessorNames
argument_list|()
operator|.
name|size
argument_list|()
index|]
argument_list|)
return|;
block|}
end_function

begin_comment
comment|/**    * @deprecated As of release 2.0.0, this will be removed in HBase 3.0.0    *             Use {@link #getCoprocessorNames} instead.    */
end_comment

begin_function
annotation|@
name|Deprecated
specifier|public
name|String
index|[]
name|getRsCoprocessors
parameter_list|()
block|{
return|return
name|getRegionServerCoprocessors
argument_list|()
return|;
block|}
end_function

begin_comment
comment|/**    * @deprecated As of release 2.0.0, this will be removed in HBase 3.0.0    *             Use {@link #getRequestCountPerSecond} instead.    */
end_comment

begin_function
annotation|@
name|Deprecated
specifier|public
name|double
name|getRequestsPerSecond
parameter_list|()
block|{
return|return
name|getRequestCountPerSecond
argument_list|()
return|;
block|}
end_function

begin_comment
comment|/**    * @see java.lang.Object#toString()    */
end_comment

begin_function
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
literal|"requestsPerSecond"
argument_list|,
name|Double
operator|.
name|valueOf
argument_list|(
name|getRequestsPerSecond
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|Strings
operator|.
name|appendKeyValue
argument_list|(
name|sb
argument_list|,
literal|"numberOfOnlineRegions"
argument_list|,
name|Integer
operator|.
name|valueOf
argument_list|(
name|getNumberOfRegions
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|Strings
operator|.
name|appendKeyValue
argument_list|(
name|sb
argument_list|,
literal|"usedHeapMB"
argument_list|,
name|Integer
operator|.
name|valueOf
argument_list|(
name|this
operator|.
name|getUsedHeapMB
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|Strings
operator|.
name|appendKeyValue
argument_list|(
name|sb
argument_list|,
literal|"maxHeapMB"
argument_list|,
name|Integer
operator|.
name|valueOf
argument_list|(
name|getMaxHeapMB
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|Strings
operator|.
name|appendKeyValue
argument_list|(
name|sb
argument_list|,
literal|"numberOfStores"
argument_list|,
name|Integer
operator|.
name|valueOf
argument_list|(
name|this
operator|.
name|stores
argument_list|)
argument_list|)
expr_stmt|;
name|Strings
operator|.
name|appendKeyValue
argument_list|(
name|sb
argument_list|,
literal|"numberOfStorefiles"
argument_list|,
name|Integer
operator|.
name|valueOf
argument_list|(
name|this
operator|.
name|storefiles
argument_list|)
argument_list|)
expr_stmt|;
name|Strings
operator|.
name|appendKeyValue
argument_list|(
name|sb
argument_list|,
literal|"storefileUncompressedSizeMB"
argument_list|,
name|Integer
operator|.
name|valueOf
argument_list|(
name|this
operator|.
name|storeUncompressedSizeMB
argument_list|)
argument_list|)
expr_stmt|;
name|Strings
operator|.
name|appendKeyValue
argument_list|(
name|sb
argument_list|,
literal|"storefileSizeMB"
argument_list|,
name|Integer
operator|.
name|valueOf
argument_list|(
name|this
operator|.
name|storefileSizeMB
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|this
operator|.
name|storeUncompressedSizeMB
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
name|storefileSizeMB
operator|/
operator|(
name|float
operator|)
name|this
operator|.
name|storeUncompressedSizeMB
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
literal|"memstoreSizeMB"
argument_list|,
name|Integer
operator|.
name|valueOf
argument_list|(
name|this
operator|.
name|memstoreSizeMB
argument_list|)
argument_list|)
expr_stmt|;
name|Strings
operator|.
name|appendKeyValue
argument_list|(
name|sb
argument_list|,
literal|"storefileIndexSizeKB"
argument_list|,
name|Long
operator|.
name|valueOf
argument_list|(
name|this
operator|.
name|storefileIndexSizeKB
argument_list|)
argument_list|)
expr_stmt|;
name|Strings
operator|.
name|appendKeyValue
argument_list|(
name|sb
argument_list|,
literal|"readRequestsCount"
argument_list|,
name|Long
operator|.
name|valueOf
argument_list|(
name|this
operator|.
name|readRequestsCount
argument_list|)
argument_list|)
expr_stmt|;
name|Strings
operator|.
name|appendKeyValue
argument_list|(
name|sb
argument_list|,
literal|"filteredReadRequestsCount"
argument_list|,
name|Long
operator|.
name|valueOf
argument_list|(
name|this
operator|.
name|filteredReadRequestsCount
argument_list|)
argument_list|)
expr_stmt|;
name|Strings
operator|.
name|appendKeyValue
argument_list|(
name|sb
argument_list|,
literal|"writeRequestsCount"
argument_list|,
name|Long
operator|.
name|valueOf
argument_list|(
name|this
operator|.
name|writeRequestsCount
argument_list|)
argument_list|)
expr_stmt|;
name|Strings
operator|.
name|appendKeyValue
argument_list|(
name|sb
argument_list|,
literal|"rootIndexSizeKB"
argument_list|,
name|Integer
operator|.
name|valueOf
argument_list|(
name|this
operator|.
name|rootIndexSizeKB
argument_list|)
argument_list|)
expr_stmt|;
name|Strings
operator|.
name|appendKeyValue
argument_list|(
name|sb
argument_list|,
literal|"totalStaticIndexSizeKB"
argument_list|,
name|Integer
operator|.
name|valueOf
argument_list|(
name|this
operator|.
name|totalStaticIndexSizeKB
argument_list|)
argument_list|)
expr_stmt|;
name|Strings
operator|.
name|appendKeyValue
argument_list|(
name|sb
argument_list|,
literal|"totalStaticBloomSizeKB"
argument_list|,
name|Integer
operator|.
name|valueOf
argument_list|(
name|this
operator|.
name|totalStaticBloomSizeKB
argument_list|)
argument_list|)
expr_stmt|;
name|Strings
operator|.
name|appendKeyValue
argument_list|(
name|sb
argument_list|,
literal|"totalCompactingKVs"
argument_list|,
name|Long
operator|.
name|valueOf
argument_list|(
name|this
operator|.
name|totalCompactingKVs
argument_list|)
argument_list|)
expr_stmt|;
name|Strings
operator|.
name|appendKeyValue
argument_list|(
name|sb
argument_list|,
literal|"currentCompactedKVs"
argument_list|,
name|Long
operator|.
name|valueOf
argument_list|(
name|this
operator|.
name|currentCompactedKVs
argument_list|)
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
name|totalCompactingKVs
operator|>
literal|0
condition|)
block|{
name|compactionProgressPct
operator|=
name|Float
operator|.
name|valueOf
argument_list|(
operator|(
name|float
operator|)
name|this
operator|.
name|currentCompactedKVs
operator|/
name|this
operator|.
name|totalCompactingKVs
argument_list|)
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
name|String
index|[]
name|coprocessorStrings
init|=
name|getRsCoprocessors
argument_list|()
decl_stmt|;
if|if
condition|(
name|coprocessorStrings
operator|!=
literal|null
condition|)
block|{
name|Strings
operator|.
name|appendKeyValue
argument_list|(
name|sb
argument_list|,
literal|"coprocessors"
argument_list|,
name|Arrays
operator|.
name|toString
argument_list|(
name|coprocessorStrings
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|sb
operator|.
name|toString
argument_list|()
return|;
block|}
end_function

begin_comment
comment|/**    * @deprecated As of release 2.0.0, this will be removed in HBase 3.0.0    *             Use {@link ServerMetricsBuilder#of(ServerName)} instead.    */
end_comment

begin_decl_stmt
annotation|@
name|Deprecated
specifier|public
specifier|static
specifier|final
name|ServerLoad
name|EMPTY_SERVERLOAD
init|=
operator|new
name|ServerLoad
argument_list|(
name|ServerName
operator|.
name|valueOf
argument_list|(
literal|"localhost,1,1"
argument_list|)
argument_list|,
name|ClusterStatusProtos
operator|.
name|ServerLoad
operator|.
name|newBuilder
argument_list|()
operator|.
name|build
argument_list|()
argument_list|)
decl_stmt|;
end_decl_stmt

begin_comment
comment|/**    * @deprecated As of release 2.0.0, this will be removed in HBase 3.0.0    *             Use {@link #getReportTimestamp} instead.    */
end_comment

begin_function
annotation|@
name|Deprecated
specifier|public
name|long
name|getReportTime
parameter_list|()
block|{
return|return
name|getReportTimestamp
argument_list|()
return|;
block|}
end_function

begin_function
annotation|@
name|Override
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
return|return
name|Objects
operator|.
name|hashCode
argument_list|(
name|stores
argument_list|,
name|storefiles
argument_list|,
name|storeUncompressedSizeMB
argument_list|,
name|storefileSizeMB
argument_list|,
name|memstoreSizeMB
argument_list|,
name|storefileIndexSizeKB
argument_list|,
name|readRequestsCount
argument_list|,
name|filteredReadRequestsCount
argument_list|,
name|writeRequestsCount
argument_list|,
name|rootIndexSizeKB
argument_list|,
name|totalStaticIndexSizeKB
argument_list|,
name|totalStaticBloomSizeKB
argument_list|,
name|totalCompactingKVs
argument_list|,
name|currentCompactedKVs
argument_list|)
return|;
block|}
end_function

begin_function
annotation|@
name|Override
specifier|public
name|boolean
name|equals
parameter_list|(
name|Object
name|other
parameter_list|)
block|{
if|if
condition|(
name|other
operator|==
name|this
condition|)
return|return
literal|true
return|;
if|if
condition|(
name|other
operator|instanceof
name|ServerLoad
condition|)
block|{
name|ServerLoad
name|sl
init|=
operator|(
operator|(
name|ServerLoad
operator|)
name|other
operator|)
decl_stmt|;
return|return
name|stores
operator|==
name|sl
operator|.
name|stores
operator|&&
name|storefiles
operator|==
name|sl
operator|.
name|storefiles
operator|&&
name|storeUncompressedSizeMB
operator|==
name|sl
operator|.
name|storeUncompressedSizeMB
operator|&&
name|storefileSizeMB
operator|==
name|sl
operator|.
name|storefileSizeMB
operator|&&
name|memstoreSizeMB
operator|==
name|sl
operator|.
name|memstoreSizeMB
operator|&&
name|storefileIndexSizeKB
operator|==
name|sl
operator|.
name|storefileIndexSizeKB
operator|&&
name|readRequestsCount
operator|==
name|sl
operator|.
name|readRequestsCount
operator|&&
name|filteredReadRequestsCount
operator|==
name|sl
operator|.
name|filteredReadRequestsCount
operator|&&
name|writeRequestsCount
operator|==
name|sl
operator|.
name|writeRequestsCount
operator|&&
name|rootIndexSizeKB
operator|==
name|sl
operator|.
name|rootIndexSizeKB
operator|&&
name|totalStaticIndexSizeKB
operator|==
name|sl
operator|.
name|totalStaticIndexSizeKB
operator|&&
name|totalStaticBloomSizeKB
operator|==
name|sl
operator|.
name|totalStaticBloomSizeKB
operator|&&
name|totalCompactingKVs
operator|==
name|sl
operator|.
name|totalCompactingKVs
operator|&&
name|currentCompactedKVs
operator|==
name|sl
operator|.
name|currentCompactedKVs
return|;
block|}
return|return
literal|false
return|;
block|}
end_function

unit|}
end_unit

