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
name|Nullable
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|IOException
import|;
end_import

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
name|HashMap
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
name|commons
operator|.
name|lang3
operator|.
name|time
operator|.
name|FastDateFormat
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
name|RegionMetrics
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
name|TableName
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
comment|/**  * Implementation for {@link ModeStrategy} for Region Mode.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
specifier|final
class|class
name|RegionModeStrategy
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
name|REGION_NAME
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
name|NAMESPACE
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
name|TABLE
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
name|START_CODE
argument_list|,
literal|13
argument_list|,
literal|false
argument_list|)
argument_list|,
operator|new
name|FieldInfo
argument_list|(
name|Field
operator|.
name|REPLICA_ID
argument_list|,
literal|5
argument_list|,
literal|false
argument_list|)
argument_list|,
operator|new
name|FieldInfo
argument_list|(
name|Field
operator|.
name|REGION
argument_list|,
literal|32
argument_list|,
literal|true
argument_list|)
argument_list|,
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
name|REQUEST_COUNT_PER_SECOND
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
name|READ_REQUEST_COUNT_PER_SECOND
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
name|STORE_FILE_SIZE
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
name|UNCOMPRESSED_STORE_FILE_SIZE
argument_list|,
literal|12
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
literal|4
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
name|LOCALITY
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
name|START_KEY
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
name|COMPACTING_CELL_COUNT
argument_list|,
literal|12
argument_list|,
literal|false
argument_list|)
argument_list|,
operator|new
name|FieldInfo
argument_list|(
name|Field
operator|.
name|COMPACTED_CELL_COUNT
argument_list|,
literal|12
argument_list|,
literal|false
argument_list|)
argument_list|,
operator|new
name|FieldInfo
argument_list|(
name|Field
operator|.
name|COMPACTION_PROGRESS
argument_list|,
literal|7
argument_list|,
literal|false
argument_list|)
argument_list|,
operator|new
name|FieldInfo
argument_list|(
name|Field
operator|.
name|LAST_MAJOR_COMPACTION_TIME
argument_list|,
literal|19
argument_list|,
literal|false
argument_list|)
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|RequestCountPerSecond
argument_list|>
name|requestCountPerSecondMap
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|RegionModeStrategy
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
name|List
argument_list|<
name|Record
argument_list|>
name|ret
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
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
name|long
name|lastReportTimestamp
init|=
name|sm
operator|.
name|getLastReportTimestamp
argument_list|()
decl_stmt|;
for|for
control|(
name|RegionMetrics
name|rm
range|:
name|sm
operator|.
name|getRegionMetrics
argument_list|()
operator|.
name|values
argument_list|()
control|)
block|{
name|ret
operator|.
name|add
argument_list|(
name|createRecord
argument_list|(
name|sm
argument_list|,
name|rm
argument_list|,
name|lastReportTimestamp
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|ret
return|;
block|}
specifier|private
name|Record
name|createRecord
parameter_list|(
name|ServerMetrics
name|serverMetrics
parameter_list|,
name|RegionMetrics
name|regionMetrics
parameter_list|,
name|long
name|lastReportTimestamp
parameter_list|)
block|{
name|Record
operator|.
name|Builder
name|builder
init|=
name|Record
operator|.
name|builder
argument_list|()
decl_stmt|;
name|String
name|regionName
init|=
name|regionMetrics
operator|.
name|getNameAsString
argument_list|()
decl_stmt|;
name|builder
operator|.
name|put
argument_list|(
name|Field
operator|.
name|REGION_NAME
argument_list|,
name|regionName
argument_list|)
expr_stmt|;
name|String
name|namespaceName
init|=
literal|""
decl_stmt|;
name|String
name|tableName
init|=
literal|""
decl_stmt|;
name|String
name|region
init|=
literal|""
decl_stmt|;
name|String
name|startKey
init|=
literal|""
decl_stmt|;
name|String
name|startCode
init|=
literal|""
decl_stmt|;
name|String
name|replicaId
init|=
literal|""
decl_stmt|;
try|try
block|{
name|byte
index|[]
index|[]
name|elements
init|=
name|RegionInfo
operator|.
name|parseRegionName
argument_list|(
name|regionMetrics
operator|.
name|getRegionName
argument_list|()
argument_list|)
decl_stmt|;
name|TableName
name|tn
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
name|elements
index|[
literal|0
index|]
argument_list|)
decl_stmt|;
name|namespaceName
operator|=
name|tn
operator|.
name|getNamespaceAsString
argument_list|()
expr_stmt|;
name|tableName
operator|=
name|tn
operator|.
name|getQualifierAsString
argument_list|()
expr_stmt|;
name|startKey
operator|=
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|elements
index|[
literal|1
index|]
argument_list|)
expr_stmt|;
name|startCode
operator|=
name|Bytes
operator|.
name|toString
argument_list|(
name|elements
index|[
literal|2
index|]
argument_list|)
expr_stmt|;
name|replicaId
operator|=
name|elements
operator|.
name|length
operator|==
literal|4
condition|?
name|Integer
operator|.
name|valueOf
argument_list|(
name|Bytes
operator|.
name|toString
argument_list|(
name|elements
index|[
literal|3
index|]
argument_list|)
argument_list|)
operator|.
name|toString
argument_list|()
else|:
literal|""
expr_stmt|;
name|region
operator|=
name|RegionInfo
operator|.
name|encodeRegionName
argument_list|(
name|regionMetrics
operator|.
name|getRegionName
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|ignored
parameter_list|)
block|{     }
name|builder
operator|.
name|put
argument_list|(
name|Field
operator|.
name|NAMESPACE
argument_list|,
name|namespaceName
argument_list|)
expr_stmt|;
name|builder
operator|.
name|put
argument_list|(
name|Field
operator|.
name|TABLE
argument_list|,
name|tableName
argument_list|)
expr_stmt|;
name|builder
operator|.
name|put
argument_list|(
name|Field
operator|.
name|START_CODE
argument_list|,
name|startCode
argument_list|)
expr_stmt|;
name|builder
operator|.
name|put
argument_list|(
name|Field
operator|.
name|REPLICA_ID
argument_list|,
name|replicaId
argument_list|)
expr_stmt|;
name|builder
operator|.
name|put
argument_list|(
name|Field
operator|.
name|REGION
argument_list|,
name|region
argument_list|)
expr_stmt|;
name|builder
operator|.
name|put
argument_list|(
name|Field
operator|.
name|START_KEY
argument_list|,
name|startKey
argument_list|)
expr_stmt|;
name|builder
operator|.
name|put
argument_list|(
name|Field
operator|.
name|REGION_SERVER
argument_list|,
name|serverMetrics
operator|.
name|getServerName
argument_list|()
operator|.
name|toShortString
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|put
argument_list|(
name|Field
operator|.
name|LONG_REGION_SERVER
argument_list|,
name|serverMetrics
operator|.
name|getServerName
argument_list|()
operator|.
name|getServerName
argument_list|()
argument_list|)
expr_stmt|;
name|RequestCountPerSecond
name|requestCountPerSecond
init|=
name|requestCountPerSecondMap
operator|.
name|get
argument_list|(
name|regionName
argument_list|)
decl_stmt|;
if|if
condition|(
name|requestCountPerSecond
operator|==
literal|null
condition|)
block|{
name|requestCountPerSecond
operator|=
operator|new
name|RequestCountPerSecond
argument_list|()
expr_stmt|;
name|requestCountPerSecondMap
operator|.
name|put
argument_list|(
name|regionName
argument_list|,
name|requestCountPerSecond
argument_list|)
expr_stmt|;
block|}
name|requestCountPerSecond
operator|.
name|refresh
argument_list|(
name|lastReportTimestamp
argument_list|,
name|regionMetrics
operator|.
name|getReadRequestCount
argument_list|()
argument_list|,
name|regionMetrics
operator|.
name|getFilteredReadRequestCount
argument_list|()
argument_list|,
name|regionMetrics
operator|.
name|getWriteRequestCount
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|put
argument_list|(
name|Field
operator|.
name|READ_REQUEST_COUNT_PER_SECOND
argument_list|,
name|requestCountPerSecond
operator|.
name|getReadRequestCountPerSecond
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|put
argument_list|(
name|Field
operator|.
name|FILTERED_READ_REQUEST_COUNT_PER_SECOND
argument_list|,
name|requestCountPerSecond
operator|.
name|getFilteredReadRequestCountPerSecond
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|put
argument_list|(
name|Field
operator|.
name|WRITE_REQUEST_COUNT_PER_SECOND
argument_list|,
name|requestCountPerSecond
operator|.
name|getWriteRequestCountPerSecond
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|put
argument_list|(
name|Field
operator|.
name|REQUEST_COUNT_PER_SECOND
argument_list|,
name|requestCountPerSecond
operator|.
name|getRequestCountPerSecond
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|put
argument_list|(
name|Field
operator|.
name|STORE_FILE_SIZE
argument_list|,
name|regionMetrics
operator|.
name|getStoreFileSize
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|put
argument_list|(
name|Field
operator|.
name|UNCOMPRESSED_STORE_FILE_SIZE
argument_list|,
name|regionMetrics
operator|.
name|getUncompressedStoreFileSize
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|put
argument_list|(
name|Field
operator|.
name|NUM_STORE_FILES
argument_list|,
name|regionMetrics
operator|.
name|getStoreFileCount
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|put
argument_list|(
name|Field
operator|.
name|MEM_STORE_SIZE
argument_list|,
name|regionMetrics
operator|.
name|getMemStoreSize
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|put
argument_list|(
name|Field
operator|.
name|LOCALITY
argument_list|,
name|regionMetrics
operator|.
name|getDataLocality
argument_list|()
argument_list|)
expr_stmt|;
name|long
name|compactingCellCount
init|=
name|regionMetrics
operator|.
name|getCompactingCellCount
argument_list|()
decl_stmt|;
name|long
name|compactedCellCount
init|=
name|regionMetrics
operator|.
name|getCompactedCellCount
argument_list|()
decl_stmt|;
name|float
name|compactionProgress
init|=
literal|0
decl_stmt|;
if|if
condition|(
name|compactedCellCount
operator|>
literal|0
condition|)
block|{
name|compactionProgress
operator|=
literal|100
operator|*
operator|(
operator|(
name|float
operator|)
name|compactedCellCount
operator|/
name|compactingCellCount
operator|)
expr_stmt|;
block|}
name|builder
operator|.
name|put
argument_list|(
name|Field
operator|.
name|COMPACTING_CELL_COUNT
argument_list|,
name|compactingCellCount
argument_list|)
expr_stmt|;
name|builder
operator|.
name|put
argument_list|(
name|Field
operator|.
name|COMPACTED_CELL_COUNT
argument_list|,
name|compactedCellCount
argument_list|)
expr_stmt|;
name|builder
operator|.
name|put
argument_list|(
name|Field
operator|.
name|COMPACTION_PROGRESS
argument_list|,
name|compactionProgress
argument_list|)
expr_stmt|;
name|FastDateFormat
name|df
init|=
name|FastDateFormat
operator|.
name|getInstance
argument_list|(
literal|"yyyy-MM-dd HH:mm:ss"
argument_list|)
decl_stmt|;
name|long
name|lastMajorCompactionTimestamp
init|=
name|regionMetrics
operator|.
name|getLastMajorCompactionTimestamp
argument_list|()
decl_stmt|;
name|builder
operator|.
name|put
argument_list|(
name|Field
operator|.
name|LAST_MAJOR_COMPACTION_TIME
argument_list|,
name|lastMajorCompactionTimestamp
operator|==
literal|0
condition|?
literal|""
else|:
name|df
operator|.
name|format
argument_list|(
name|lastMajorCompactionTimestamp
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|builder
operator|.
name|build
argument_list|()
return|;
block|}
comment|/**    * Form new record list with records formed by only fields provided through fieldInfo and    * add a count field for each record with value 1    * We are doing two operation of selecting and adding new field    * because of saving some CPU cycles on rebuilding the record again    *    * @param fieldInfos List of FieldInfos required in the record    * @param records    List of records which needs to be processed    * @param countField Field which needs to be added with value 1 for each record    * @return records after selecting required fields and adding count field    */
name|List
argument_list|<
name|Record
argument_list|>
name|selectModeFieldsAndAddCountField
parameter_list|(
name|List
argument_list|<
name|FieldInfo
argument_list|>
name|fieldInfos
parameter_list|,
name|List
argument_list|<
name|Record
argument_list|>
name|records
parameter_list|,
name|Field
name|countField
parameter_list|)
block|{
return|return
name|records
operator|.
name|stream
argument_list|()
operator|.
name|map
argument_list|(
name|record
lambda|->
name|Record
operator|.
name|ofEntries
argument_list|(
name|fieldInfos
operator|.
name|stream
argument_list|()
operator|.
name|filter
argument_list|(
name|fi
lambda|->
name|record
operator|.
name|containsKey
argument_list|(
name|fi
operator|.
name|getField
argument_list|()
argument_list|)
argument_list|)
operator|.
name|map
argument_list|(
name|fi
lambda|->
name|Record
operator|.
name|entry
argument_list|(
name|fi
operator|.
name|getField
argument_list|()
argument_list|,
name|record
operator|.
name|get
argument_list|(
name|fi
operator|.
name|getField
argument_list|()
argument_list|)
argument_list|)
argument_list|)
argument_list|)
argument_list|)
operator|.
name|map
argument_list|(
name|record
lambda|->
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
name|countField
argument_list|,
literal|1
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
annotation|@
name|Nullable
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
comment|// do nothing
return|return
literal|null
return|;
block|}
block|}
end_class

end_unit

