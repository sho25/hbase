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
name|java
operator|.
name|io
operator|.
name|Closeable
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
name|Map
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|ScheduledExecutorService
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|ScheduledFuture
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|TimeUnit
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
name|logging
operator|.
name|Log
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
name|logging
operator|.
name|LogFactory
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
name|CompatibilitySingletonFactory
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
name|HRegionInfo
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
name|HTableDescriptor
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
name|EnvironmentEdgeManager
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
name|metrics2
operator|.
name|MetricsExecutor
import|;
end_import

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|MetricsRegionWrapperImpl
implements|implements
name|MetricsRegionWrapper
implements|,
name|Closeable
block|{
specifier|private
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|MetricsRegionWrapperImpl
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|int
name|PERIOD
init|=
literal|45
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|UNKNOWN
init|=
literal|"unknown"
decl_stmt|;
specifier|private
specifier|final
name|HRegion
name|region
decl_stmt|;
specifier|private
name|ScheduledExecutorService
name|executor
decl_stmt|;
specifier|private
name|Runnable
name|runnable
decl_stmt|;
specifier|private
name|long
name|numStoreFiles
decl_stmt|;
specifier|private
name|long
name|memstoreSize
decl_stmt|;
specifier|private
name|long
name|storeFileSize
decl_stmt|;
specifier|private
name|long
name|maxStoreFileAge
decl_stmt|;
specifier|private
name|long
name|minStoreFileAge
decl_stmt|;
specifier|private
name|long
name|avgStoreFileAge
decl_stmt|;
specifier|private
name|long
name|numReferenceFiles
decl_stmt|;
specifier|private
name|ScheduledFuture
argument_list|<
name|?
argument_list|>
name|regionMetricsUpdateTask
decl_stmt|;
specifier|public
name|MetricsRegionWrapperImpl
parameter_list|(
name|HRegion
name|region
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
name|executor
operator|=
name|CompatibilitySingletonFactory
operator|.
name|getInstance
argument_list|(
name|MetricsExecutor
operator|.
name|class
argument_list|)
operator|.
name|getExecutor
argument_list|()
expr_stmt|;
name|this
operator|.
name|runnable
operator|=
operator|new
name|HRegionMetricsWrapperRunnable
argument_list|()
expr_stmt|;
name|this
operator|.
name|regionMetricsUpdateTask
operator|=
name|this
operator|.
name|executor
operator|.
name|scheduleWithFixedDelay
argument_list|(
name|this
operator|.
name|runnable
argument_list|,
name|PERIOD
argument_list|,
name|PERIOD
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|getTableName
parameter_list|()
block|{
name|HTableDescriptor
name|tableDesc
init|=
name|this
operator|.
name|region
operator|.
name|getTableDesc
argument_list|()
decl_stmt|;
if|if
condition|(
name|tableDesc
operator|==
literal|null
condition|)
block|{
return|return
name|UNKNOWN
return|;
block|}
return|return
name|tableDesc
operator|.
name|getTableName
argument_list|()
operator|.
name|getQualifierAsString
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|getNamespace
parameter_list|()
block|{
name|HTableDescriptor
name|tableDesc
init|=
name|this
operator|.
name|region
operator|.
name|getTableDesc
argument_list|()
decl_stmt|;
if|if
condition|(
name|tableDesc
operator|==
literal|null
condition|)
block|{
return|return
name|UNKNOWN
return|;
block|}
return|return
name|tableDesc
operator|.
name|getTableName
argument_list|()
operator|.
name|getNamespaceAsString
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|getRegionName
parameter_list|()
block|{
name|HRegionInfo
name|regionInfo
init|=
name|this
operator|.
name|region
operator|.
name|getRegionInfo
argument_list|()
decl_stmt|;
if|if
condition|(
name|regionInfo
operator|==
literal|null
condition|)
block|{
return|return
name|UNKNOWN
return|;
block|}
return|return
name|regionInfo
operator|.
name|getEncodedName
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getNumStores
parameter_list|()
block|{
name|Map
argument_list|<
name|byte
index|[]
argument_list|,
name|Store
argument_list|>
name|stores
init|=
name|this
operator|.
name|region
operator|.
name|stores
decl_stmt|;
if|if
condition|(
name|stores
operator|==
literal|null
condition|)
block|{
return|return
literal|0
return|;
block|}
return|return
name|stores
operator|.
name|size
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getNumStoreFiles
parameter_list|()
block|{
return|return
name|numStoreFiles
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getMemstoreSize
parameter_list|()
block|{
return|return
name|memstoreSize
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
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
name|long
name|getReadRequestCount
parameter_list|()
block|{
return|return
name|this
operator|.
name|region
operator|.
name|getReadRequestsCount
argument_list|()
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
name|this
operator|.
name|region
operator|.
name|getFilteredReadRequestsCount
argument_list|()
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
name|this
operator|.
name|region
operator|.
name|getWriteRequestsCount
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getNumFilesCompacted
parameter_list|()
block|{
return|return
name|this
operator|.
name|region
operator|.
name|compactionNumFilesCompacted
operator|.
name|get
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getNumBytesCompacted
parameter_list|()
block|{
return|return
name|this
operator|.
name|region
operator|.
name|compactionNumBytesCompacted
operator|.
name|get
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getNumCompactionsCompleted
parameter_list|()
block|{
return|return
name|this
operator|.
name|region
operator|.
name|compactionsFinished
operator|.
name|get
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getLastMajorCompactionAge
parameter_list|()
block|{
name|long
name|lastMajorCompactionTs
init|=
literal|0L
decl_stmt|;
try|try
block|{
name|lastMajorCompactionTs
operator|=
name|this
operator|.
name|region
operator|.
name|getOldestHfileTs
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|ioe
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Could not load HFile info "
argument_list|,
name|ioe
argument_list|)
expr_stmt|;
block|}
name|long
name|now
init|=
name|EnvironmentEdgeManager
operator|.
name|currentTime
argument_list|()
decl_stmt|;
return|return
name|now
operator|-
name|lastMajorCompactionTs
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getNumCompactionsFailed
parameter_list|()
block|{
return|return
name|this
operator|.
name|region
operator|.
name|compactionsFailed
operator|.
name|get
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getMaxStoreFileAge
parameter_list|()
block|{
return|return
name|maxStoreFileAge
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getMinStoreFileAge
parameter_list|()
block|{
return|return
name|minStoreFileAge
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getAvgStoreFileAge
parameter_list|()
block|{
return|return
name|avgStoreFileAge
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getNumReferenceFiles
parameter_list|()
block|{
return|return
name|numReferenceFiles
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getRegionHashCode
parameter_list|()
block|{
return|return
name|this
operator|.
name|region
operator|.
name|hashCode
argument_list|()
return|;
block|}
specifier|public
class|class
name|HRegionMetricsWrapperRunnable
implements|implements
name|Runnable
block|{
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
name|long
name|tempNumStoreFiles
init|=
literal|0
decl_stmt|;
name|long
name|tempMemstoreSize
init|=
literal|0
decl_stmt|;
name|long
name|tempStoreFileSize
init|=
literal|0
decl_stmt|;
name|long
name|tempMaxStoreFileAge
init|=
literal|0
decl_stmt|;
name|long
name|tempMinStoreFileAge
init|=
name|Long
operator|.
name|MAX_VALUE
decl_stmt|;
name|long
name|tempNumReferenceFiles
init|=
literal|0
decl_stmt|;
name|long
name|avgAgeNumerator
init|=
literal|0
decl_stmt|;
name|long
name|numHFiles
init|=
literal|0
decl_stmt|;
if|if
condition|(
name|region
operator|.
name|stores
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|Store
name|store
range|:
name|region
operator|.
name|stores
operator|.
name|values
argument_list|()
control|)
block|{
name|tempNumStoreFiles
operator|+=
name|store
operator|.
name|getStorefilesCount
argument_list|()
expr_stmt|;
name|tempMemstoreSize
operator|+=
name|store
operator|.
name|getSizeOfMemStore
argument_list|()
operator|.
name|getDataSize
argument_list|()
expr_stmt|;
name|tempStoreFileSize
operator|+=
name|store
operator|.
name|getStorefilesSize
argument_list|()
expr_stmt|;
name|long
name|storeMaxStoreFileAge
init|=
name|store
operator|.
name|getMaxStoreFileAge
argument_list|()
decl_stmt|;
name|tempMaxStoreFileAge
operator|=
operator|(
name|storeMaxStoreFileAge
operator|>
name|tempMaxStoreFileAge
operator|)
condition|?
name|storeMaxStoreFileAge
else|:
name|tempMaxStoreFileAge
expr_stmt|;
name|long
name|storeMinStoreFileAge
init|=
name|store
operator|.
name|getMinStoreFileAge
argument_list|()
decl_stmt|;
name|tempMinStoreFileAge
operator|=
operator|(
name|storeMinStoreFileAge
operator|<
name|tempMinStoreFileAge
operator|)
condition|?
name|storeMinStoreFileAge
else|:
name|tempMinStoreFileAge
expr_stmt|;
name|long
name|storeHFiles
init|=
name|store
operator|.
name|getNumHFiles
argument_list|()
decl_stmt|;
name|avgAgeNumerator
operator|+=
name|store
operator|.
name|getAvgStoreFileAge
argument_list|()
operator|*
name|storeHFiles
expr_stmt|;
name|numHFiles
operator|+=
name|storeHFiles
expr_stmt|;
name|tempNumReferenceFiles
operator|+=
name|store
operator|.
name|getNumReferenceFiles
argument_list|()
expr_stmt|;
block|}
block|}
name|numStoreFiles
operator|=
name|tempNumStoreFiles
expr_stmt|;
name|memstoreSize
operator|=
name|tempMemstoreSize
expr_stmt|;
name|storeFileSize
operator|=
name|tempStoreFileSize
expr_stmt|;
name|maxStoreFileAge
operator|=
name|tempMaxStoreFileAge
expr_stmt|;
if|if
condition|(
name|tempMinStoreFileAge
operator|!=
name|Long
operator|.
name|MAX_VALUE
condition|)
block|{
name|minStoreFileAge
operator|=
name|tempMinStoreFileAge
expr_stmt|;
block|}
if|if
condition|(
name|numHFiles
operator|!=
literal|0
condition|)
block|{
name|avgStoreFileAge
operator|=
name|avgAgeNumerator
operator|/
name|numHFiles
expr_stmt|;
block|}
name|numReferenceFiles
operator|=
name|tempNumReferenceFiles
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|()
throws|throws
name|IOException
block|{
name|regionMetricsUpdateTask
operator|.
name|cancel
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
comment|/**    * Get the replica id of this region.    */
annotation|@
name|Override
specifier|public
name|int
name|getReplicaId
parameter_list|()
block|{
return|return
name|region
operator|.
name|getRegionInfo
argument_list|()
operator|.
name|getReplicaId
argument_list|()
return|;
block|}
block|}
end_class

end_unit

