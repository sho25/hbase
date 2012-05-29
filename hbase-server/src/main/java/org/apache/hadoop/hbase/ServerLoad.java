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
name|generated
operator|.
name|HBaseProtos
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
name|protobuf
operator|.
name|generated
operator|.
name|HBaseProtos
operator|.
name|RegionLoad
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
name|int
name|readRequestsCount
init|=
literal|0
decl_stmt|;
specifier|private
name|int
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
name|HBaseProtos
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
name|HBaseProtos
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
comment|/* @return the underlying ServerLoad protobuf object */
specifier|public
name|HBaseProtos
operator|.
name|ServerLoad
name|getServerLoadPB
parameter_list|()
block|{
return|return
name|serverLoad
return|;
block|}
specifier|protected
name|HBaseProtos
operator|.
name|ServerLoad
name|serverLoad
decl_stmt|;
comment|/* @return number of requests per second since last report. */
specifier|public
name|int
name|getRequestsPerSecond
parameter_list|()
block|{
return|return
name|serverLoad
operator|.
name|getRequestsPerSecond
argument_list|()
return|;
block|}
specifier|public
name|boolean
name|hasRequestsPerSecond
parameter_list|()
block|{
return|return
name|serverLoad
operator|.
name|hasRequestsPerSecond
argument_list|()
return|;
block|}
comment|/* @return total Number of requests from the start of the region server. */
specifier|public
name|int
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
comment|/* Returns list of RegionLoads, which contain information on the load of individual regions. */
specifier|public
name|List
argument_list|<
name|RegionLoad
argument_list|>
name|getRegionLoadsList
parameter_list|()
block|{
return|return
name|serverLoad
operator|.
name|getRegionLoadsList
argument_list|()
return|;
block|}
specifier|public
name|RegionLoad
name|getRegionLoads
parameter_list|(
name|int
name|index
parameter_list|)
block|{
return|return
name|serverLoad
operator|.
name|getRegionLoads
argument_list|(
name|index
argument_list|)
return|;
block|}
specifier|public
name|int
name|getRegionLoadsCount
parameter_list|()
block|{
return|return
name|serverLoad
operator|.
name|getRegionLoadsCount
argument_list|()
return|;
block|}
comment|/**    * @return the list Regionserver-level coprocessors, e.g., WALObserver implementations.    * Region-level coprocessors, on the other hand, are stored inside the RegionLoad objects.    */
specifier|public
name|List
argument_list|<
name|Coprocessor
argument_list|>
name|getCoprocessorsList
parameter_list|()
block|{
return|return
name|serverLoad
operator|.
name|getCoprocessorsList
argument_list|()
return|;
block|}
specifier|public
name|Coprocessor
name|getCoprocessors
parameter_list|(
name|int
name|index
parameter_list|)
block|{
return|return
name|serverLoad
operator|.
name|getCoprocessors
argument_list|(
name|index
argument_list|)
return|;
block|}
specifier|public
name|int
name|getCoprocessorsCount
parameter_list|()
block|{
return|return
name|serverLoad
operator|.
name|getCoprocessorsCount
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
name|getStorefileSizeMB
parameter_list|()
block|{
return|return
name|storefileSizeMB
return|;
block|}
specifier|public
name|int
name|getMemstoreSizeMB
parameter_list|()
block|{
return|return
name|memstoreSizeMB
return|;
block|}
specifier|public
name|int
name|getStorefileIndexSizeMB
parameter_list|()
block|{
return|return
name|storefileIndexSizeMB
return|;
block|}
specifier|public
name|int
name|getReadRequestsCount
parameter_list|()
block|{
return|return
name|readRequestsCount
return|;
block|}
specifier|public
name|int
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
comment|/**    * Return the RegionServer-level coprocessors from a ServerLoad pb.    * @param sl - ServerLoad    * @return string array of loaded RegionServer-level coprocessors    */
specifier|public
specifier|static
name|String
index|[]
name|getRegionServerCoprocessors
parameter_list|(
name|ServerLoad
name|sl
parameter_list|)
block|{
if|if
condition|(
name|sl
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
name|List
argument_list|<
name|Coprocessor
argument_list|>
name|list
init|=
name|sl
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
comment|/**    * Return the RegionServer-level and Region-level coprocessors    * from a ServerLoad pb.    * @param sl - ServerLoad    * @return string array of loaded RegionServer-level and    *         Region-level coprocessors    */
specifier|public
specifier|static
name|String
index|[]
name|getAllCoprocessors
parameter_list|(
name|ServerLoad
name|sl
parameter_list|)
block|{
if|if
condition|(
name|sl
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
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
name|sl
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
for|for
control|(
name|RegionLoad
name|rl
range|:
name|sl
operator|.
name|getRegionLoadsList
argument_list|()
control|)
block|{
for|for
control|(
name|Coprocessor
name|coprocessor
range|:
name|rl
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
block|}
return|return
name|coprocessSet
operator|.
name|toArray
argument_list|(
operator|new
name|String
index|[
literal|0
index|]
argument_list|)
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
literal|"requestsPerSecond"
argument_list|,
name|Integer
operator|.
name|valueOf
argument_list|(
name|this
operator|.
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
name|getRegionLoadsCount
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
name|getAllCoprocessors
argument_list|(
name|this
argument_list|)
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
name|HBaseProtos
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

