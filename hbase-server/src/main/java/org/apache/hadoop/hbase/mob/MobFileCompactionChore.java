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
name|mob
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
name|TableDescriptors
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
name|Admin
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
name|ColumnFamilyDescriptor
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
name|CompactionState
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
name|Connection
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
name|client
operator|.
name|TableDescriptor
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
name|TableState
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
name|master
operator|.
name|HMaster
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
name|EnvironmentEdgeManager
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
comment|/**  * Periodic MOB compaction chore.  * It runs MOB compaction on region servers in parallel, thus  * utilizing distributed cluster resources. To avoid possible major  * compaction storms, one can specify maximum number regions to be compacted  * in parallel by setting configuration parameter:<br>  * 'hbase.mob.major.compaction.region.batch.size', which by default is 0 (unlimited).  *  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|MobFileCompactionChore
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
name|MobFileCompactionChore
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|HMaster
name|master
decl_stmt|;
specifier|private
name|int
name|regionBatchSize
init|=
literal|0
decl_stmt|;
comment|// not set - compact all
specifier|public
name|MobFileCompactionChore
parameter_list|(
name|HMaster
name|master
parameter_list|)
block|{
name|super
argument_list|(
name|master
operator|.
name|getServerName
argument_list|()
operator|+
literal|"-MobFileCompactionChore"
argument_list|,
name|master
argument_list|,
name|master
operator|.
name|getConfiguration
argument_list|()
operator|.
name|getInt
argument_list|(
name|MobConstants
operator|.
name|MOB_COMPACTION_CHORE_PERIOD
argument_list|,
name|MobConstants
operator|.
name|DEFAULT_MOB_COMPACTION_CHORE_PERIOD
argument_list|)
argument_list|,
name|master
operator|.
name|getConfiguration
argument_list|()
operator|.
name|getInt
argument_list|(
name|MobConstants
operator|.
name|MOB_COMPACTION_CHORE_PERIOD
argument_list|,
name|MobConstants
operator|.
name|DEFAULT_MOB_COMPACTION_CHORE_PERIOD
argument_list|)
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
expr_stmt|;
name|this
operator|.
name|master
operator|=
name|master
expr_stmt|;
name|this
operator|.
name|regionBatchSize
operator|=
name|master
operator|.
name|getConfiguration
argument_list|()
operator|.
name|getInt
argument_list|(
name|MobConstants
operator|.
name|MOB_MAJOR_COMPACTION_REGION_BATCH_SIZE
argument_list|,
name|MobConstants
operator|.
name|DEFAULT_MOB_MAJOR_COMPACTION_REGION_BATCH_SIZE
argument_list|)
expr_stmt|;
block|}
annotation|@
name|VisibleForTesting
specifier|public
name|MobFileCompactionChore
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|int
name|batchSize
parameter_list|)
block|{
name|this
operator|.
name|regionBatchSize
operator|=
name|batchSize
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|chore
parameter_list|()
block|{
name|boolean
name|reported
init|=
literal|false
decl_stmt|;
try|try
init|(
name|Connection
name|conn
init|=
name|master
operator|.
name|getConnection
argument_list|()
init|;
name|Admin
name|admin
operator|=
name|conn
operator|.
name|getAdmin
argument_list|()
init|;
init|)
block|{
name|TableDescriptors
name|htds
init|=
name|master
operator|.
name|getTableDescriptors
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|TableDescriptor
argument_list|>
name|map
init|=
name|htds
operator|.
name|getAll
argument_list|()
decl_stmt|;
for|for
control|(
name|TableDescriptor
name|htd
range|:
name|map
operator|.
name|values
argument_list|()
control|)
block|{
if|if
condition|(
operator|!
name|master
operator|.
name|getTableStateManager
argument_list|()
operator|.
name|isTableState
argument_list|(
name|htd
operator|.
name|getTableName
argument_list|()
argument_list|,
name|TableState
operator|.
name|State
operator|.
name|ENABLED
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Skipping MOB compaction on table {} because it is not ENABLED"
argument_list|,
name|htd
operator|.
name|getTableName
argument_list|()
argument_list|)
expr_stmt|;
continue|continue;
block|}
else|else
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Starting MOB compaction on table {}, checking {} column families"
argument_list|,
name|htd
operator|.
name|getTableName
argument_list|()
argument_list|,
name|htd
operator|.
name|getColumnFamilyCount
argument_list|()
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|ColumnFamilyDescriptor
name|hcd
range|:
name|htd
operator|.
name|getColumnFamilies
argument_list|()
control|)
block|{
try|try
block|{
if|if
condition|(
name|hcd
operator|.
name|isMobEnabled
argument_list|()
condition|)
block|{
if|if
condition|(
operator|!
name|reported
condition|)
block|{
name|master
operator|.
name|reportMobCompactionStart
argument_list|(
name|htd
operator|.
name|getTableName
argument_list|()
argument_list|)
expr_stmt|;
name|reported
operator|=
literal|true
expr_stmt|;
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"Major MOB compacting table={} cf={}"
argument_list|,
name|htd
operator|.
name|getTableName
argument_list|()
argument_list|,
name|hcd
operator|.
name|getNameAsString
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|regionBatchSize
operator|==
name|MobConstants
operator|.
name|DEFAULT_MOB_MAJOR_COMPACTION_REGION_BATCH_SIZE
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Table={} cf ={}: batch MOB compaction is disabled, {}=0 -"
operator|+
literal|" all regions will be compacted in parallel"
argument_list|,
name|htd
operator|.
name|getTableName
argument_list|()
argument_list|,
name|hcd
operator|.
name|getNameAsString
argument_list|()
argument_list|,
literal|"hbase.mob.compaction.batch.size"
argument_list|)
expr_stmt|;
name|admin
operator|.
name|majorCompact
argument_list|(
name|htd
operator|.
name|getTableName
argument_list|()
argument_list|,
name|hcd
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Table={} cf={}: performing MOB major compaction in batches "
operator|+
literal|"'hbase.mob.compaction.batch.size'={}"
argument_list|,
name|htd
operator|.
name|getTableName
argument_list|()
argument_list|,
name|hcd
operator|.
name|getNameAsString
argument_list|()
argument_list|,
name|regionBatchSize
argument_list|)
expr_stmt|;
name|performMajorCompactionInBatches
argument_list|(
name|admin
argument_list|,
name|htd
argument_list|,
name|hcd
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Skipping table={} column family={} because it is not MOB-enabled"
argument_list|,
name|htd
operator|.
name|getTableName
argument_list|()
argument_list|,
name|hcd
operator|.
name|getNameAsString
argument_list|()
argument_list|)
expr_stmt|;
block|}
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
literal|"Failed to compact table={} cf={}"
argument_list|,
name|htd
operator|.
name|getTableName
argument_list|()
argument_list|,
name|hcd
operator|.
name|getNameAsString
argument_list|()
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|ee
parameter_list|)
block|{
name|Thread
operator|.
name|currentThread
argument_list|()
operator|.
name|interrupt
argument_list|()
expr_stmt|;
name|master
operator|.
name|reportMobCompactionEnd
argument_list|(
name|htd
operator|.
name|getTableName
argument_list|()
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|warn
argument_list|(
literal|"Failed to compact table={} cf={}"
argument_list|,
name|htd
operator|.
name|getTableName
argument_list|()
argument_list|,
name|hcd
operator|.
name|getNameAsString
argument_list|()
argument_list|,
name|ee
argument_list|)
expr_stmt|;
comment|// Quit the chore
return|return;
block|}
block|}
if|if
condition|(
name|reported
condition|)
block|{
name|master
operator|.
name|reportMobCompactionEnd
argument_list|(
name|htd
operator|.
name|getTableName
argument_list|()
argument_list|)
expr_stmt|;
name|reported
operator|=
literal|false
expr_stmt|;
block|}
block|}
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
literal|"Failed to compact"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|VisibleForTesting
specifier|public
name|void
name|performMajorCompactionInBatches
parameter_list|(
name|Admin
name|admin
parameter_list|,
name|TableDescriptor
name|htd
parameter_list|,
name|ColumnFamilyDescriptor
name|hcd
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|List
argument_list|<
name|RegionInfo
argument_list|>
name|regions
init|=
name|admin
operator|.
name|getRegions
argument_list|(
name|htd
operator|.
name|getTableName
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|regions
operator|.
name|size
argument_list|()
operator|<=
name|this
operator|.
name|regionBatchSize
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Table={} cf={} - performing major MOB compaction in non-batched mode,"
operator|+
literal|"regions={}, batch size={}"
argument_list|,
name|htd
operator|.
name|getTableName
argument_list|()
argument_list|,
name|hcd
operator|.
name|getNameAsString
argument_list|()
argument_list|,
name|regions
operator|.
name|size
argument_list|()
argument_list|,
name|regionBatchSize
argument_list|)
expr_stmt|;
name|admin
operator|.
name|majorCompact
argument_list|(
name|htd
operator|.
name|getTableName
argument_list|()
argument_list|,
name|hcd
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
return|return;
block|}
comment|// Shuffle list of regions in case if they come ordered by region server
name|Collections
operator|.
name|shuffle
argument_list|(
name|regions
argument_list|)
expr_stmt|;
comment|// Create first batch
name|List
argument_list|<
name|RegionInfo
argument_list|>
name|toCompact
init|=
operator|new
name|ArrayList
argument_list|<
name|RegionInfo
argument_list|>
argument_list|(
name|this
operator|.
name|regionBatchSize
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|this
operator|.
name|regionBatchSize
condition|;
name|i
operator|++
control|)
block|{
name|toCompact
operator|.
name|add
argument_list|(
name|regions
operator|.
name|remove
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// Start compaction now
for|for
control|(
name|RegionInfo
name|ri
range|:
name|toCompact
control|)
block|{
name|startCompaction
argument_list|(
name|admin
argument_list|,
name|htd
operator|.
name|getTableName
argument_list|()
argument_list|,
name|ri
argument_list|,
name|hcd
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|List
argument_list|<
name|RegionInfo
argument_list|>
name|compacted
init|=
operator|new
name|ArrayList
argument_list|<
name|RegionInfo
argument_list|>
argument_list|(
name|toCompact
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|RegionInfo
argument_list|>
name|failed
init|=
operator|new
name|ArrayList
argument_list|<
name|RegionInfo
argument_list|>
argument_list|()
decl_stmt|;
name|int
name|totalCompacted
init|=
literal|0
decl_stmt|;
while|while
condition|(
operator|!
name|toCompact
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
comment|// Check status of active compactions
for|for
control|(
name|RegionInfo
name|ri
range|:
name|toCompact
control|)
block|{
try|try
block|{
if|if
condition|(
name|admin
operator|.
name|getCompactionStateForRegion
argument_list|(
name|ri
operator|.
name|getRegionName
argument_list|()
argument_list|)
operator|==
name|CompactionState
operator|.
name|NONE
condition|)
block|{
name|totalCompacted
operator|++
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Finished major MOB compaction: table={} cf={} region={} compacted regions={}"
argument_list|,
name|htd
operator|.
name|getTableName
argument_list|()
argument_list|,
name|hcd
operator|.
name|getNameAsString
argument_list|()
argument_list|,
name|ri
operator|.
name|getRegionNameAsString
argument_list|()
argument_list|,
name|totalCompacted
argument_list|)
expr_stmt|;
name|compacted
operator|.
name|add
argument_list|(
name|ri
argument_list|)
expr_stmt|;
block|}
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
literal|"Could not get compaction state for table={} cf={} region={}, compaction will"
operator|+
literal|" aborted for the region."
argument_list|,
name|htd
operator|.
name|getTableName
argument_list|()
argument_list|,
name|hcd
operator|.
name|getNameAsString
argument_list|()
argument_list|,
name|ri
operator|.
name|getEncodedName
argument_list|()
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|error
argument_list|(
literal|"Because of:"
argument_list|,
name|e
argument_list|)
expr_stmt|;
name|failed
operator|.
name|add
argument_list|(
name|ri
argument_list|)
expr_stmt|;
block|}
block|}
comment|// Remove failed regions to avoid
comment|// endless compaction loop
name|toCompact
operator|.
name|removeAll
argument_list|(
name|failed
argument_list|)
expr_stmt|;
name|failed
operator|.
name|clear
argument_list|()
expr_stmt|;
comment|// Update batch: remove compacted regions and add new ones
for|for
control|(
name|RegionInfo
name|ri
range|:
name|compacted
control|)
block|{
name|toCompact
operator|.
name|remove
argument_list|(
name|ri
argument_list|)
expr_stmt|;
if|if
condition|(
name|regions
operator|.
name|size
argument_list|()
operator|>
literal|0
condition|)
block|{
name|RegionInfo
name|region
init|=
name|regions
operator|.
name|remove
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|toCompact
operator|.
name|add
argument_list|(
name|region
argument_list|)
expr_stmt|;
name|startCompaction
argument_list|(
name|admin
argument_list|,
name|htd
operator|.
name|getTableName
argument_list|()
argument_list|,
name|region
argument_list|,
name|hcd
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
name|compacted
operator|.
name|clear
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Table={}  cf={}. Wait for 10 sec, toCompact size={} regions left={}"
operator|+
literal|" compacted so far={}"
argument_list|,
name|htd
operator|.
name|getTableName
argument_list|()
argument_list|,
name|hcd
operator|.
name|getNameAsString
argument_list|()
argument_list|,
name|toCompact
operator|.
name|size
argument_list|()
argument_list|,
name|regions
operator|.
name|size
argument_list|()
argument_list|,
name|totalCompacted
argument_list|)
expr_stmt|;
name|Thread
operator|.
name|sleep
argument_list|(
literal|10000
argument_list|)
expr_stmt|;
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"Finished major MOB compacting table={}. cf={}"
argument_list|,
name|htd
operator|.
name|getTableName
argument_list|()
argument_list|,
name|hcd
operator|.
name|getNameAsString
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|startCompaction
parameter_list|(
name|Admin
name|admin
parameter_list|,
name|TableName
name|table
parameter_list|,
name|RegionInfo
name|region
parameter_list|,
name|byte
index|[]
name|cf
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Started major compaction: table={} cf={} region={}"
argument_list|,
name|table
argument_list|,
name|Bytes
operator|.
name|toString
argument_list|(
name|cf
argument_list|)
argument_list|,
name|region
operator|.
name|getRegionNameAsString
argument_list|()
argument_list|)
expr_stmt|;
name|admin
operator|.
name|majorCompactRegion
argument_list|(
name|region
operator|.
name|getRegionName
argument_list|()
argument_list|,
name|cf
argument_list|)
expr_stmt|;
comment|// Wait until it really starts
comment|// but with finite timeout
name|long
name|waitTime
init|=
literal|300000
decl_stmt|;
comment|// 5 min
name|long
name|startTime
init|=
name|EnvironmentEdgeManager
operator|.
name|currentTime
argument_list|()
decl_stmt|;
while|while
condition|(
name|admin
operator|.
name|getCompactionStateForRegion
argument_list|(
name|region
operator|.
name|getRegionName
argument_list|()
argument_list|)
operator|==
name|CompactionState
operator|.
name|NONE
condition|)
block|{
comment|// Is 1 second too aggressive?
name|Thread
operator|.
name|sleep
argument_list|(
literal|1000
argument_list|)
expr_stmt|;
if|if
condition|(
name|EnvironmentEdgeManager
operator|.
name|currentTime
argument_list|()
operator|-
name|startTime
operator|>
name|waitTime
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Waited for {} ms to start major MOB compaction on table={} cf={} region={}."
operator|+
literal|" Stopped waiting for request confirmation. This is not an ERROR, continue next region."
argument_list|,
name|waitTime
argument_list|,
name|table
operator|.
name|getNameAsString
argument_list|()
argument_list|,
name|Bytes
operator|.
name|toString
argument_list|(
name|cf
argument_list|)
argument_list|,
name|region
operator|.
name|getRegionNameAsString
argument_list|()
argument_list|)
expr_stmt|;
break|break;
block|}
block|}
block|}
block|}
end_class

end_unit

