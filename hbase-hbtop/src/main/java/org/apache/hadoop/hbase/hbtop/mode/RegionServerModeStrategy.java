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
name|hbtop
operator|.
name|mode
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|ArrayList
import|;
end_import

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
name|ClusterMetrics
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
name|ServerMetrics
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
name|hbtop
operator|.
name|Record
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
name|hbtop
operator|.
name|RecordFilter
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
name|hbtop
operator|.
name|field
operator|.
name|Field
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
name|hbtop
operator|.
name|field
operator|.
name|FieldInfo
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
comment|/**  * Implementation for {@link ModeStrategy} for RegionServer Mode.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
specifier|final
class|class
name|RegionServerModeStrategy
implements|implements
name|ModeStrategy
block|{
specifier|private
specifier|final
name|List
argument_list|<
name|FieldInfo
argument_list|>
name|fieldInfos
init|=
name|Arrays
operator|.
name|asList
argument_list|(
operator|new
name|FieldInfo
argument_list|(
name|Field
operator|.
name|REGION_SERVER
argument_list|,
literal|0
argument_list|,
literal|true
argument_list|)
argument_list|,
operator|new
name|FieldInfo
argument_list|(
name|Field
operator|.
name|LONG_REGION_SERVER
argument_list|,
literal|0
argument_list|,
literal|false
argument_list|)
argument_list|,
operator|new
name|FieldInfo
argument_list|(
name|Field
operator|.
name|REGION_COUNT
argument_list|,
literal|7
argument_list|,
literal|true
argument_list|)
argument_list|,
operator|new
name|FieldInfo
argument_list|(
name|Field
operator|.
name|REQUEST_COUNT_PER_SECOND
argument_list|,
literal|10
argument_list|,
literal|true
argument_list|)
argument_list|,
operator|new
name|FieldInfo
argument_list|(
name|Field
operator|.
name|READ_REQUEST_COUNT_PER_SECOND
argument_list|,
literal|10
argument_list|,
literal|true
argument_list|)
argument_list|,
operator|new
name|FieldInfo
argument_list|(
name|Field
operator|.
name|FILTERED_READ_REQUEST_COUNT_PER_SECOND
argument_list|,
literal|8
argument_list|,
literal|true
argument_list|)
argument_list|,
operator|new
name|FieldInfo
argument_list|(
name|Field
operator|.
name|WRITE_REQUEST_COUNT_PER_SECOND
argument_list|,
literal|10
argument_list|,
literal|true
argument_list|)
argument_list|,
operator|new
name|FieldInfo
argument_list|(
name|Field
operator|.
name|STORE_FILE_SIZE
argument_list|,
literal|13
argument_list|,
literal|true
argument_list|)
argument_list|,
operator|new
name|FieldInfo
argument_list|(
name|Field
operator|.
name|UNCOMPRESSED_STORE_FILE_SIZE
argument_list|,
literal|15
argument_list|,
literal|false
argument_list|)
argument_list|,
operator|new
name|FieldInfo
argument_list|(
name|Field
operator|.
name|NUM_STORE_FILES
argument_list|,
literal|7
argument_list|,
literal|true
argument_list|)
argument_list|,
operator|new
name|FieldInfo
argument_list|(
name|Field
operator|.
name|MEM_STORE_SIZE
argument_list|,
literal|11
argument_list|,
literal|true
argument_list|)
argument_list|,
operator|new
name|FieldInfo
argument_list|(
name|Field
operator|.
name|USED_HEAP_SIZE
argument_list|,
literal|11
argument_list|,
literal|true
argument_list|)
argument_list|,
operator|new
name|FieldInfo
argument_list|(
name|Field
operator|.
name|MAX_HEAP_SIZE
argument_list|,
literal|11
argument_list|,
literal|true
argument_list|)
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|RegionModeStrategy
name|regionModeStrategy
init|=
operator|new
name|RegionModeStrategy
argument_list|()
decl_stmt|;
name|RegionServerModeStrategy
parameter_list|()
block|{   }
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|FieldInfo
argument_list|>
name|getFieldInfos
parameter_list|()
block|{
return|return
name|fieldInfos
return|;
block|}
annotation|@
name|Override
specifier|public
name|Field
name|getDefaultSortField
parameter_list|()
block|{
return|return
name|Field
operator|.
name|REQUEST_COUNT_PER_SECOND
return|;
block|}
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|Record
argument_list|>
name|getRecords
parameter_list|(
name|ClusterMetrics
name|clusterMetrics
parameter_list|,
name|List
argument_list|<
name|RecordFilter
argument_list|>
name|pushDownFilters
parameter_list|)
block|{
comment|// Get records from RegionModeStrategy and add REGION_COUNT field
name|List
argument_list|<
name|Record
argument_list|>
name|records
init|=
name|regionModeStrategy
operator|.
name|selectModeFieldsAndAddCountField
argument_list|(
name|fieldInfos
argument_list|,
name|regionModeStrategy
operator|.
name|getRecords
argument_list|(
name|clusterMetrics
argument_list|,
name|pushDownFilters
argument_list|)
argument_list|,
name|Field
operator|.
name|REGION_COUNT
argument_list|)
decl_stmt|;
comment|// Aggregation by LONG_REGION_SERVER field
name|Map
argument_list|<
name|String
argument_list|,
name|Record
argument_list|>
name|retMap
init|=
name|ModeStrategyUtils
operator|.
name|aggregateRecords
argument_list|(
name|records
argument_list|,
name|Field
operator|.
name|LONG_REGION_SERVER
argument_list|)
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
name|r
lambda|->
name|r
operator|.
name|get
argument_list|(
name|Field
operator|.
name|LONG_REGION_SERVER
argument_list|)
operator|.
name|asString
argument_list|()
argument_list|,
name|r
lambda|->
name|r
argument_list|)
argument_list|)
decl_stmt|;
comment|// Add USED_HEAP_SIZE field and MAX_HEAP_SIZE field
for|for
control|(
name|ServerMetrics
name|sm
range|:
name|clusterMetrics
operator|.
name|getLiveServerMetrics
argument_list|()
operator|.
name|values
argument_list|()
control|)
block|{
name|Record
name|record
init|=
name|retMap
operator|.
name|get
argument_list|(
name|sm
operator|.
name|getServerName
argument_list|()
operator|.
name|getServerName
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|record
operator|==
literal|null
condition|)
block|{
continue|continue;
block|}
name|Record
name|newRecord
init|=
name|Record
operator|.
name|builder
argument_list|()
operator|.
name|putAll
argument_list|(
name|record
argument_list|)
operator|.
name|put
argument_list|(
name|Field
operator|.
name|USED_HEAP_SIZE
argument_list|,
name|sm
operator|.
name|getUsedHeapSize
argument_list|()
argument_list|)
operator|.
name|put
argument_list|(
name|Field
operator|.
name|MAX_HEAP_SIZE
argument_list|,
name|sm
operator|.
name|getMaxHeapSize
argument_list|()
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|retMap
operator|.
name|put
argument_list|(
name|sm
operator|.
name|getServerName
argument_list|()
operator|.
name|getServerName
argument_list|()
argument_list|,
name|newRecord
argument_list|)
expr_stmt|;
block|}
return|return
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|retMap
operator|.
name|values
argument_list|()
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|DrillDownInfo
name|drillDown
parameter_list|(
name|Record
name|selectedRecord
parameter_list|)
block|{
name|List
argument_list|<
name|RecordFilter
argument_list|>
name|initialFilters
init|=
name|Collections
operator|.
name|singletonList
argument_list|(
name|RecordFilter
operator|.
name|newBuilder
argument_list|(
name|Field
operator|.
name|REGION_SERVER
argument_list|)
operator|.
name|doubleEquals
argument_list|(
name|selectedRecord
operator|.
name|get
argument_list|(
name|Field
operator|.
name|REGION_SERVER
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
return|return
operator|new
name|DrillDownInfo
argument_list|(
name|Mode
operator|.
name|REGION
argument_list|,
name|initialFilters
argument_list|)
return|;
block|}
block|}
end_class

end_unit

