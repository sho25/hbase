begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|master
package|;
end_package

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
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|conf
operator|.
name|Configuration
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
name|HConstants
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
name|ScheduledChore
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
name|ServerName
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
name|Stoppable
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
name|PerClientRandomNonceGenerator
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
name|slf4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|LoggerFactory
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
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|collections4
operator|.
name|MapUtils
import|;
end_import

begin_comment
comment|/**  * This chore, every time it runs, will try to recover regions with high store ref count  * by reopening them  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|RegionsRecoveryChore
extends|extends
name|ScheduledChore
block|{
specifier|private
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|RegionsRecoveryChore
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|REGIONS_RECOVERY_CHORE_NAME
init|=
literal|"RegionsRecoveryChore"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|ERROR_REOPEN_REIONS_MSG
init|=
literal|"Error reopening regions with high storeRefCount. "
decl_stmt|;
specifier|private
specifier|final
name|HMaster
name|hMaster
decl_stmt|;
specifier|private
specifier|final
name|int
name|storeFileRefCountThreshold
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|PerClientRandomNonceGenerator
name|NONCE_GENERATOR
init|=
name|PerClientRandomNonceGenerator
operator|.
name|get
argument_list|()
decl_stmt|;
comment|/**    * Construct RegionsRecoveryChore with provided params    *    * @param stopper When {@link Stoppable#isStopped()} is true, this chore will cancel and cleanup    * @param configuration The configuration params to be used    * @param hMaster HMaster instance to initiate RegionTableRegions    */
name|RegionsRecoveryChore
parameter_list|(
specifier|final
name|Stoppable
name|stopper
parameter_list|,
specifier|final
name|Configuration
name|configuration
parameter_list|,
specifier|final
name|HMaster
name|hMaster
parameter_list|)
block|{
name|super
argument_list|(
name|REGIONS_RECOVERY_CHORE_NAME
argument_list|,
name|stopper
argument_list|,
name|configuration
operator|.
name|getInt
argument_list|(
name|HConstants
operator|.
name|REGIONS_RECOVERY_INTERVAL
argument_list|,
name|HConstants
operator|.
name|DEFAULT_REGIONS_RECOVERY_INTERVAL
argument_list|)
argument_list|)
expr_stmt|;
name|this
operator|.
name|hMaster
operator|=
name|hMaster
expr_stmt|;
name|this
operator|.
name|storeFileRefCountThreshold
operator|=
name|configuration
operator|.
name|getInt
argument_list|(
name|HConstants
operator|.
name|STORE_FILE_REF_COUNT_THRESHOLD
argument_list|,
name|HConstants
operator|.
name|DEFAULT_STORE_FILE_REF_COUNT_THRESHOLD
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|chore
parameter_list|()
block|{
if|if
condition|(
name|LOG
operator|.
name|isTraceEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|trace
argument_list|(
literal|"Starting up Regions Recovery chore for reopening regions based on storeFileRefCount..."
argument_list|)
expr_stmt|;
block|}
try|try
block|{
comment|// only if storeFileRefCountThreshold> 0, consider the feature turned on
if|if
condition|(
name|storeFileRefCountThreshold
operator|>
literal|0
condition|)
block|{
specifier|final
name|ClusterMetrics
name|clusterMetrics
init|=
name|hMaster
operator|.
name|getClusterMetrics
argument_list|()
decl_stmt|;
specifier|final
name|Map
argument_list|<
name|ServerName
argument_list|,
name|ServerMetrics
argument_list|>
name|serverMetricsMap
init|=
name|clusterMetrics
operator|.
name|getLiveServerMetrics
argument_list|()
decl_stmt|;
specifier|final
name|Map
argument_list|<
name|TableName
argument_list|,
name|List
argument_list|<
name|byte
index|[]
argument_list|>
argument_list|>
name|tableToReopenRegionsMap
init|=
name|getTableToRegionsByRefCount
argument_list|(
name|serverMetricsMap
argument_list|)
decl_stmt|;
if|if
condition|(
name|MapUtils
operator|.
name|isNotEmpty
argument_list|(
name|tableToReopenRegionsMap
argument_list|)
condition|)
block|{
name|tableToReopenRegionsMap
operator|.
name|forEach
argument_list|(
parameter_list|(
name|tableName
parameter_list|,
name|regionNames
parameter_list|)
lambda|->
block|{
try|try
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Reopening regions due to high storeFileRefCount. "
operator|+
literal|"TableName: {} , noOfRegions: {}"
argument_list|,
name|tableName
argument_list|,
name|regionNames
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|hMaster
operator|.
name|reopenRegions
argument_list|(
name|tableName
argument_list|,
name|regionNames
argument_list|,
name|NONCE_GENERATOR
operator|.
name|getNonceGroup
argument_list|()
argument_list|,
name|NONCE_GENERATOR
operator|.
name|newNonce
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"{} tableName: {}, regionNames: {}"
argument_list|,
name|ERROR_REOPEN_REIONS_MSG
argument_list|,
name|tableName
argument_list|,
name|regionNames
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Reopening regions with very high storeFileRefCount is disabled. "
operator|+
literal|"Provide threshold value> 0 for {} to enable it."
argument_list|,
name|HConstants
operator|.
name|STORE_FILE_REF_COUNT_THRESHOLD
argument_list|)
expr_stmt|;
block|}
block|}
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Error while reopening regions based on storeRefCount threshold"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|LOG
operator|.
name|isTraceEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|trace
argument_list|(
literal|"Exiting Regions Recovery chore for reopening regions based on storeFileRefCount..."
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|Map
argument_list|<
name|TableName
argument_list|,
name|List
argument_list|<
name|byte
index|[]
argument_list|>
argument_list|>
name|getTableToRegionsByRefCount
parameter_list|(
specifier|final
name|Map
argument_list|<
name|ServerName
argument_list|,
name|ServerMetrics
argument_list|>
name|serverMetricsMap
parameter_list|)
block|{
specifier|final
name|Map
argument_list|<
name|TableName
argument_list|,
name|List
argument_list|<
name|byte
index|[]
argument_list|>
argument_list|>
name|tableToReopenRegionsMap
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|ServerMetrics
name|serverMetrics
range|:
name|serverMetricsMap
operator|.
name|values
argument_list|()
control|)
block|{
name|Map
argument_list|<
name|byte
index|[]
argument_list|,
name|RegionMetrics
argument_list|>
name|regionMetricsMap
init|=
name|serverMetrics
operator|.
name|getRegionMetrics
argument_list|()
decl_stmt|;
for|for
control|(
name|RegionMetrics
name|regionMetrics
range|:
name|regionMetricsMap
operator|.
name|values
argument_list|()
control|)
block|{
comment|// For each region, each compacted store file can have different ref counts
comment|// We need to find maximum of all such ref counts and if that max count of compacted
comment|// store files is beyond a threshold value, we should reopen the region.
comment|// Here, we take max ref count of all compacted store files and not the cumulative
comment|// count of all compacted store files
specifier|final
name|int
name|maxCompactedStoreFileRefCount
init|=
name|regionMetrics
operator|.
name|getMaxCompactedStoreFileRefCount
argument_list|()
decl_stmt|;
if|if
condition|(
name|maxCompactedStoreFileRefCount
operator|>
name|storeFileRefCountThreshold
condition|)
block|{
specifier|final
name|byte
index|[]
name|regionName
init|=
name|regionMetrics
operator|.
name|getRegionName
argument_list|()
decl_stmt|;
name|prepareTableToReopenRegionsMap
argument_list|(
name|tableToReopenRegionsMap
argument_list|,
name|regionName
argument_list|,
name|maxCompactedStoreFileRefCount
argument_list|)
expr_stmt|;
block|}
block|}
block|}
return|return
name|tableToReopenRegionsMap
return|;
block|}
specifier|private
name|void
name|prepareTableToReopenRegionsMap
parameter_list|(
specifier|final
name|Map
argument_list|<
name|TableName
argument_list|,
name|List
argument_list|<
name|byte
index|[]
argument_list|>
argument_list|>
name|tableToReopenRegionsMap
parameter_list|,
specifier|final
name|byte
index|[]
name|regionName
parameter_list|,
specifier|final
name|int
name|regionStoreRefCount
parameter_list|)
block|{
specifier|final
name|RegionInfo
name|regionInfo
init|=
name|hMaster
operator|.
name|getAssignmentManager
argument_list|()
operator|.
name|getRegionInfo
argument_list|(
name|regionName
argument_list|)
decl_stmt|;
specifier|final
name|TableName
name|tableName
init|=
name|regionInfo
operator|.
name|getTable
argument_list|()
decl_stmt|;
if|if
condition|(
name|TableName
operator|.
name|isMetaTableName
argument_list|(
name|tableName
argument_list|)
condition|)
block|{
comment|// Do not reopen regions of meta table even if it has
comment|// high store file reference count
return|return;
block|}
name|LOG
operator|.
name|warn
argument_list|(
literal|"Region {} for Table {} has high storeFileRefCount {}, considering it for reopen.."
argument_list|,
name|regionInfo
operator|.
name|getRegionNameAsString
argument_list|()
argument_list|,
name|tableName
argument_list|,
name|regionStoreRefCount
argument_list|)
expr_stmt|;
name|tableToReopenRegionsMap
operator|.
name|computeIfAbsent
argument_list|(
name|tableName
argument_list|,
parameter_list|(
name|key
parameter_list|)
lambda|->
operator|new
name|ArrayList
argument_list|<>
argument_list|()
argument_list|)
operator|.
name|add
argument_list|(
name|regionName
argument_list|)
expr_stmt|;
block|}
comment|// hashcode/equals implementation to ensure at-most one object of RegionsRecoveryChore
comment|// is scheduled at a time - RegionsRecoveryConfigManager
annotation|@
name|Override
specifier|public
name|boolean
name|equals
parameter_list|(
name|Object
name|o
parameter_list|)
block|{
if|if
condition|(
name|this
operator|==
name|o
condition|)
block|{
return|return
literal|true
return|;
block|}
return|return
name|o
operator|!=
literal|null
operator|&&
name|getClass
argument_list|()
operator|==
name|o
operator|.
name|getClass
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
return|return
literal|31
return|;
block|}
block|}
end_class

end_unit

