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
name|TreeMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|TreeSet
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
name|classification
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
name|hadoop
operator|.
name|hbase
operator|.
name|classification
operator|.
name|InterfaceStability
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
name|protobuf
operator|.
name|ProtobufUtil
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
name|protobuf
operator|.
name|generated
operator|.
name|HBaseProtos
operator|.
name|Coprocessor
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

begin_comment
comment|/**  * This class is used for exporting current state of load on a RegionServer.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Public
annotation|@
name|InterfaceStability
operator|.
name|Evolving
specifier|public
class|class
name|ServerLoad
block|{
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
name|int
name|storefileIndexSizeMB
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
operator|.
name|serverLoad
operator|=
name|serverLoad
expr_stmt|;
for|for
control|(
name|ClusterStatusProtos
operator|.
name|RegionLoad
name|rl
range|:
name|serverLoad
operator|.
name|getRegionLoadsList
argument_list|()
control|)
block|{
name|stores
operator|+=
name|rl
operator|.
name|getStores
argument_list|()
expr_stmt|;
name|storefiles
operator|+=
name|rl
operator|.
name|getStorefiles
argument_list|()
expr_stmt|;
name|storeUncompressedSizeMB
operator|+=
name|rl
operator|.
name|getStoreUncompressedSizeMB
argument_list|()
expr_stmt|;
name|storefileSizeMB
operator|+=
name|rl
operator|.
name|getStorefileSizeMB
argument_list|()
expr_stmt|;
name|memstoreSizeMB
operator|+=
name|rl
operator|.
name|getMemstoreSizeMB
argument_list|()
expr_stmt|;
name|storefileIndexSizeMB
operator|+=
name|rl
operator|.
name|getStorefileIndexSizeMB
argument_list|()
expr_stmt|;
name|readRequestsCount
operator|+=
name|rl
operator|.
name|getReadRequestsCount
argument_list|()
expr_stmt|;
name|filteredReadRequestsCount
operator|+=
name|rl
operator|.
name|getFilteredReadRequestsCount
argument_list|()
expr_stmt|;
name|writeRequestsCount
operator|+=
name|rl
operator|.
name|getWriteRequestsCount
argument_list|()
expr_stmt|;
name|rootIndexSizeKB
operator|+=
name|rl
operator|.
name|getRootIndexSizeKB
argument_list|()
expr_stmt|;
name|totalStaticIndexSizeKB
operator|+=
name|rl
operator|.
name|getTotalStaticIndexSizeKB
argument_list|()
expr_stmt|;
name|totalStaticBloomSizeKB
operator|+=
name|rl
operator|.
name|getTotalStaticBloomSizeKB
argument_list|()
expr_stmt|;
name|totalCompactingKVs
operator|+=
name|rl
operator|.
name|getTotalCompactingKVs
argument_list|()
expr_stmt|;
name|currentCompactedKVs
operator|+=
name|rl
operator|.
name|getCurrentCompactedKVs
argument_list|()
expr_stmt|;
block|}
block|}
comment|// NOTE: Function name cannot start with "get" because then an OpenDataException is thrown because
comment|// HBaseProtos.ServerLoad cannot be converted to an open data type(see HBASE-5967).
comment|/* @return the underlying ServerLoad protobuf object */
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
comment|/* @return number of requests  since last report. */
specifier|public
name|long
name|getNumberOfRequests
parameter_list|()
block|{
return|return
name|serverLoad
operator|.
name|getNumberOfRequests
argument_list|()
return|;
block|}
specifier|public
name|boolean
name|hasNumberOfRequests
parameter_list|()
block|{
return|return
name|serverLoad
operator|.
name|hasNumberOfRequests
argument_list|()
return|;
block|}
comment|/* @return total Number of requests from the start of the region server. */
specifier|public
name|long
name|getTotalNumberOfRequests
parameter_list|()
block|{
return|return
name|serverLoad
operator|.
name|getTotalNumberOfRequests
argument_list|()
return|;
block|}
specifier|public
name|boolean
name|hasTotalNumberOfRequests
parameter_list|()
block|{
return|return
name|serverLoad
operator|.
name|hasTotalNumberOfRequests
argument_list|()
return|;
block|}
comment|/* @return the amount of used heap, in MB. */
specifier|public
name|int
name|getUsedHeapMB
parameter_list|()
block|{
return|return
name|serverLoad
operator|.
name|getUsedHeapMB
argument_list|()
return|;
block|}
specifier|public
name|boolean
name|hasUsedHeapMB
parameter_list|()
block|{
return|return
name|serverLoad
operator|.
name|hasUsedHeapMB
argument_list|()
return|;
block|}
comment|/* @return the maximum allowable size of the heap, in MB. */
specifier|public
name|int
name|getMaxHeapMB
parameter_list|()
block|{
return|return
name|serverLoad
operator|.
name|getMaxHeapMB
argument_list|()
return|;
block|}
specifier|public
name|boolean
name|hasMaxHeapMB
parameter_list|()
block|{
return|return
name|serverLoad
operator|.
name|hasMaxHeapMB
argument_list|()
return|;
block|}
specifier|public
name|int
name|getStores
parameter_list|()
block|{
return|return
name|stores
return|;
block|}
specifier|public
name|int
name|getStorefiles
parameter_list|()
block|{
return|return
name|storefiles
return|;
block|}
specifier|public
name|int
name|getStoreUncompressedSizeMB
parameter_list|()
block|{
return|return
name|storeUncompressedSizeMB
return|;
block|}
specifier|public
name|int
name|getStorefileSizeInMB
parameter_list|()
block|{
return|return
name|storefileSizeMB
return|;
block|}
specifier|public
name|int
name|getMemstoreSizeInMB
parameter_list|()
block|{
return|return
name|memstoreSizeMB
return|;
block|}
specifier|public
name|int
name|getStorefileIndexSizeInMB
parameter_list|()
block|{
return|return
name|storefileIndexSizeMB
return|;
block|}
specifier|public
name|long
name|getReadRequestsCount
parameter_list|()
block|{
return|return
name|readRequestsCount
return|;
block|}
specifier|public
name|long
name|getFilteredReadRequestsCount
parameter_list|()
block|{
return|return
name|filteredReadRequestsCount
return|;
block|}
specifier|public
name|long
name|getWriteRequestsCount
parameter_list|()
block|{
return|return
name|writeRequestsCount
return|;
block|}
specifier|public
name|int
name|getRootIndexSizeKB
parameter_list|()
block|{
return|return
name|rootIndexSizeKB
return|;
block|}
specifier|public
name|int
name|getTotalStaticIndexSizeKB
parameter_list|()
block|{
return|return
name|totalStaticIndexSizeKB
return|;
block|}
specifier|public
name|int
name|getTotalStaticBloomSizeKB
parameter_list|()
block|{
return|return
name|totalStaticBloomSizeKB
return|;
block|}
specifier|public
name|long
name|getTotalCompactingKVs
parameter_list|()
block|{
return|return
name|totalCompactingKVs
return|;
block|}
specifier|public
name|long
name|getCurrentCompactedKVs
parameter_list|()
block|{
return|return
name|currentCompactedKVs
return|;
block|}
comment|/**    * @return the number of regions    */
specifier|public
name|int
name|getNumberOfRegions
parameter_list|()
block|{
return|return
name|serverLoad
operator|.
name|getRegionLoadsCount
argument_list|()
return|;
block|}
specifier|public
name|int
name|getInfoServerPort
parameter_list|()
block|{
return|return
name|serverLoad
operator|.
name|getInfoServerPort
argument_list|()
return|;
block|}
comment|/**    * Call directly from client such as hbase shell    * @return the list of ReplicationLoadSource    */
specifier|public
name|List
argument_list|<
name|ReplicationLoadSource
argument_list|>
name|getReplicationLoadSourceList
parameter_list|()
block|{
return|return
name|ProtobufUtil
operator|.
name|toReplicationLoadSourceList
argument_list|(
name|serverLoad
operator|.
name|getReplLoadSourceList
argument_list|()
argument_list|)
return|;
block|}
comment|/**    * Call directly from client such as hbase shell    * @return ReplicationLoadSink    */
specifier|public
name|ReplicationLoadSink
name|getReplicationLoadSink
parameter_list|()
block|{
if|if
condition|(
name|serverLoad
operator|.
name|hasReplLoadSink
argument_list|()
condition|)
block|{
return|return
name|ProtobufUtil
operator|.
name|toReplicationLoadSink
argument_list|(
name|serverLoad
operator|.
name|getReplLoadSink
argument_list|()
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
comment|/**    * Originally, this method factored in the effect of requests going to the    * server as well. However, this does not interact very well with the current    * region rebalancing code, which only factors number of regions. For the    * interim, until we can figure out how to make rebalancing use all the info    * available, we're just going to make load purely the number of regions.    *    * @return load factor for this server    */
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
comment|/**    * @return region load metrics    */
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
name|Map
argument_list|<
name|byte
index|[]
argument_list|,
name|RegionLoad
argument_list|>
name|regionLoads
init|=
operator|new
name|TreeMap
argument_list|<
name|byte
index|[]
argument_list|,
name|RegionLoad
argument_list|>
argument_list|(
name|Bytes
operator|.
name|BYTES_COMPARATOR
argument_list|)
decl_stmt|;
for|for
control|(
name|ClusterStatusProtos
operator|.
name|RegionLoad
name|rl
range|:
name|serverLoad
operator|.
name|getRegionLoadsList
argument_list|()
control|)
block|{
name|RegionLoad
name|regionLoad
init|=
operator|new
name|RegionLoad
argument_list|(
name|rl
argument_list|)
decl_stmt|;
name|regionLoads
operator|.
name|put
argument_list|(
name|regionLoad
operator|.
name|getName
argument_list|()
argument_list|,
name|regionLoad
argument_list|)
expr_stmt|;
block|}
return|return
name|regionLoads
return|;
block|}
comment|/**    * Return the RegionServer-level coprocessors    * @return string array of loaded RegionServer-level coprocessors    */
specifier|public
name|String
index|[]
name|getRegionServerCoprocessors
parameter_list|()
block|{
name|List
argument_list|<
name|Coprocessor
argument_list|>
name|list
init|=
name|obtainServerLoadPB
argument_list|()
operator|.
name|getCoprocessorsList
argument_list|()
decl_stmt|;
name|String
index|[]
name|ret
init|=
operator|new
name|String
index|[
name|list
operator|.
name|size
argument_list|()
index|]
decl_stmt|;
name|int
name|i
init|=
literal|0
decl_stmt|;
for|for
control|(
name|Coprocessor
name|elem
range|:
name|list
control|)
block|{
name|ret
index|[
name|i
operator|++
index|]
operator|=
name|elem
operator|.
name|getName
argument_list|()
expr_stmt|;
block|}
return|return
name|ret
return|;
block|}
comment|/**    * Return the RegionServer-level and Region-level coprocessors    * @return string array of loaded RegionServer-level and    *         Region-level coprocessors    */
specifier|public
name|String
index|[]
name|getRsCoprocessors
parameter_list|()
block|{
comment|// Need a set to remove duplicates, but since generated Coprocessor class
comment|// is not Comparable, make it a Set<String> instead of Set<Coprocessor>
name|TreeSet
argument_list|<
name|String
argument_list|>
name|coprocessSet
init|=
operator|new
name|TreeSet
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|Coprocessor
name|coprocessor
range|:
name|obtainServerLoadPB
argument_list|()
operator|.
name|getCoprocessorsList
argument_list|()
control|)
block|{
name|coprocessSet
operator|.
name|add
argument_list|(
name|coprocessor
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|coprocessSet
operator|.
name|toArray
argument_list|(
operator|new
name|String
index|[
name|coprocessSet
operator|.
name|size
argument_list|()
index|]
argument_list|)
return|;
block|}
comment|/**    * @return number of requests per second received since the last report    */
specifier|public
name|double
name|getRequestsPerSecond
parameter_list|()
block|{
return|return
name|getNumberOfRequests
argument_list|()
return|;
block|}
comment|/**    * @see java.lang.Object#toString()    */
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
name|sb
operator|=
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
name|sb
operator|=
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
name|sb
operator|=
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
name|sb
operator|=
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
name|sb
operator|=
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
name|sb
operator|=
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
name|sb
operator|=
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
name|sb
operator|=
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
name|sb
operator|=
name|Strings
operator|.
name|appendKeyValue
argument_list|(
name|sb
argument_list|,
literal|"storefileIndexSizeMB"
argument_list|,
name|Integer
operator|.
name|valueOf
argument_list|(
name|this
operator|.
name|storefileIndexSizeMB
argument_list|)
argument_list|)
expr_stmt|;
name|sb
operator|=
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
name|sb
operator|=
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
name|sb
operator|=
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
name|sb
operator|=
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
name|sb
operator|=
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
name|sb
operator|=
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
name|sb
operator|=
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
name|sb
operator|=
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
name|sb
operator|=
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
name|sb
operator|=
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
specifier|public
specifier|static
specifier|final
name|ServerLoad
name|EMPTY_SERVERLOAD
init|=
operator|new
name|ServerLoad
argument_list|(
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
block|}
end_class

end_unit

