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
name|chaos
operator|.
name|factories
package|;
end_package

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
name|chaos
operator|.
name|actions
operator|.
name|Action
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
name|chaos
operator|.
name|actions
operator|.
name|AddColumnAction
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
name|chaos
operator|.
name|actions
operator|.
name|BatchRestartRsAction
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
name|chaos
operator|.
name|actions
operator|.
name|ChangeBloomFilterAction
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
name|chaos
operator|.
name|actions
operator|.
name|ChangeCompressionAction
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
name|chaos
operator|.
name|actions
operator|.
name|ChangeEncodingAction
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
name|chaos
operator|.
name|actions
operator|.
name|ChangeVersionsAction
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
name|chaos
operator|.
name|actions
operator|.
name|CompactMobAction
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
name|chaos
operator|.
name|actions
operator|.
name|CompactRandomRegionOfTableAction
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
name|chaos
operator|.
name|actions
operator|.
name|CompactTableAction
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
name|chaos
operator|.
name|actions
operator|.
name|DumpClusterStatusAction
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
name|chaos
operator|.
name|actions
operator|.
name|FlushRandomRegionOfTableAction
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
name|chaos
operator|.
name|actions
operator|.
name|FlushTableAction
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
name|chaos
operator|.
name|actions
operator|.
name|MergeRandomAdjacentRegionsOfTableAction
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
name|chaos
operator|.
name|actions
operator|.
name|MoveRandomRegionOfTableAction
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
name|chaos
operator|.
name|actions
operator|.
name|MoveRegionsOfTableAction
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
name|chaos
operator|.
name|actions
operator|.
name|RemoveColumnAction
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
name|chaos
operator|.
name|actions
operator|.
name|RestartActiveMasterAction
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
name|chaos
operator|.
name|actions
operator|.
name|RestartRandomRsAction
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
name|chaos
operator|.
name|actions
operator|.
name|RestartRsHoldingMetaAction
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
name|chaos
operator|.
name|actions
operator|.
name|RollingBatchRestartRsAction
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
name|chaos
operator|.
name|actions
operator|.
name|SnapshotTableAction
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
name|chaos
operator|.
name|actions
operator|.
name|SplitRandomRegionOfTableAction
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
name|chaos
operator|.
name|monkies
operator|.
name|ChaosMonkey
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
name|chaos
operator|.
name|monkies
operator|.
name|PolicyBasedChaosMonkey
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
name|chaos
operator|.
name|policies
operator|.
name|CompositeSequentialPolicy
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
name|chaos
operator|.
name|policies
operator|.
name|DoActionsOncePolicy
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
name|chaos
operator|.
name|policies
operator|.
name|PeriodicRandomActionPolicy
import|;
end_import

begin_comment
comment|/**  * This is a copy of SlowDeterministicMonkeyFactory that also does mob compactions.  */
end_comment

begin_class
specifier|public
class|class
name|MobSlowDeterministicMonkeyFactory
extends|extends
name|MonkeyFactory
block|{
specifier|private
name|long
name|action1Period
decl_stmt|;
specifier|private
name|long
name|action2Period
decl_stmt|;
specifier|private
name|long
name|action3Period
decl_stmt|;
specifier|private
name|long
name|action4Period
decl_stmt|;
specifier|private
name|long
name|moveRegionsMaxTime
decl_stmt|;
specifier|private
name|long
name|moveRegionsSleepTime
decl_stmt|;
specifier|private
name|long
name|moveRandomRegionSleepTime
decl_stmt|;
specifier|private
name|long
name|restartRandomRSSleepTime
decl_stmt|;
specifier|private
name|long
name|batchRestartRSSleepTime
decl_stmt|;
specifier|private
name|float
name|batchRestartRSRatio
decl_stmt|;
specifier|private
name|long
name|restartActiveMasterSleepTime
decl_stmt|;
specifier|private
name|long
name|rollingBatchRestartRSSleepTime
decl_stmt|;
specifier|private
name|float
name|rollingBatchRestartRSRatio
decl_stmt|;
specifier|private
name|long
name|restartRsHoldingMetaSleepTime
decl_stmt|;
specifier|private
name|float
name|compactTableRatio
decl_stmt|;
specifier|private
name|float
name|compactRandomRegionRatio
decl_stmt|;
annotation|@
name|Override
specifier|public
name|ChaosMonkey
name|build
parameter_list|()
block|{
name|loadProperties
argument_list|()
expr_stmt|;
comment|// Actions such as compact/flush a table/region,
comment|// move one region around. They are not so destructive,
comment|// can be executed more frequently.
name|Action
index|[]
name|actions1
init|=
operator|new
name|Action
index|[]
block|{
operator|new
name|CompactMobAction
argument_list|(
name|tableName
argument_list|,
name|compactTableRatio
argument_list|)
block|,
operator|new
name|CompactTableAction
argument_list|(
name|tableName
argument_list|,
name|compactTableRatio
argument_list|)
block|,
operator|new
name|CompactRandomRegionOfTableAction
argument_list|(
name|tableName
argument_list|,
name|compactRandomRegionRatio
argument_list|)
block|,
operator|new
name|FlushTableAction
argument_list|(
name|tableName
argument_list|)
block|,
operator|new
name|FlushRandomRegionOfTableAction
argument_list|(
name|tableName
argument_list|)
block|,
operator|new
name|MoveRandomRegionOfTableAction
argument_list|(
name|tableName
argument_list|)
block|}
decl_stmt|;
comment|// Actions such as split/merge/snapshot.
comment|// They should not cause data loss, or unreliability
comment|// such as region stuck in transition.
name|Action
index|[]
name|actions2
init|=
operator|new
name|Action
index|[]
block|{
operator|new
name|SplitRandomRegionOfTableAction
argument_list|(
name|tableName
argument_list|)
block|,
operator|new
name|MergeRandomAdjacentRegionsOfTableAction
argument_list|(
name|tableName
argument_list|)
block|,
operator|new
name|SnapshotTableAction
argument_list|(
name|tableName
argument_list|)
block|,
operator|new
name|AddColumnAction
argument_list|(
name|tableName
argument_list|)
block|,
operator|new
name|RemoveColumnAction
argument_list|(
name|tableName
argument_list|,
name|columnFamilies
argument_list|)
block|,
operator|new
name|ChangeEncodingAction
argument_list|(
name|tableName
argument_list|)
block|,
operator|new
name|ChangeCompressionAction
argument_list|(
name|tableName
argument_list|)
block|,
operator|new
name|ChangeBloomFilterAction
argument_list|(
name|tableName
argument_list|)
block|,
operator|new
name|ChangeVersionsAction
argument_list|(
name|tableName
argument_list|)
block|}
decl_stmt|;
comment|// Destructive actions to mess things around.
name|Action
index|[]
name|actions3
init|=
operator|new
name|Action
index|[]
block|{
operator|new
name|MoveRegionsOfTableAction
argument_list|(
name|moveRegionsSleepTime
argument_list|,
name|moveRegionsMaxTime
argument_list|,
name|tableName
argument_list|)
block|,
operator|new
name|MoveRandomRegionOfTableAction
argument_list|(
name|moveRandomRegionSleepTime
argument_list|,
name|tableName
argument_list|)
block|,
operator|new
name|RestartRandomRsAction
argument_list|(
name|restartRandomRSSleepTime
argument_list|)
block|,
operator|new
name|BatchRestartRsAction
argument_list|(
name|batchRestartRSSleepTime
argument_list|,
name|batchRestartRSRatio
argument_list|)
block|,
operator|new
name|RestartActiveMasterAction
argument_list|(
name|restartActiveMasterSleepTime
argument_list|)
block|,
operator|new
name|RollingBatchRestartRsAction
argument_list|(
name|rollingBatchRestartRSSleepTime
argument_list|,
name|rollingBatchRestartRSRatio
argument_list|)
block|,
operator|new
name|RestartRsHoldingMetaAction
argument_list|(
name|restartRsHoldingMetaSleepTime
argument_list|)
block|}
decl_stmt|;
comment|// Action to log more info for debugging
name|Action
index|[]
name|actions4
init|=
operator|new
name|Action
index|[]
block|{
operator|new
name|DumpClusterStatusAction
argument_list|()
block|}
decl_stmt|;
return|return
operator|new
name|PolicyBasedChaosMonkey
argument_list|(
name|properties
argument_list|,
name|util
argument_list|,
operator|new
name|PeriodicRandomActionPolicy
argument_list|(
name|action1Period
argument_list|,
name|actions1
argument_list|)
argument_list|,
operator|new
name|PeriodicRandomActionPolicy
argument_list|(
name|action2Period
argument_list|,
name|actions2
argument_list|)
argument_list|,
operator|new
name|CompositeSequentialPolicy
argument_list|(
operator|new
name|DoActionsOncePolicy
argument_list|(
name|action3Period
argument_list|,
name|actions3
argument_list|)
argument_list|,
operator|new
name|PeriodicRandomActionPolicy
argument_list|(
name|action3Period
argument_list|,
name|actions3
argument_list|)
argument_list|)
argument_list|,
operator|new
name|PeriodicRandomActionPolicy
argument_list|(
name|action4Period
argument_list|,
name|actions4
argument_list|)
argument_list|)
return|;
block|}
specifier|private
name|void
name|loadProperties
parameter_list|()
block|{
name|action1Period
operator|=
name|Long
operator|.
name|parseLong
argument_list|(
name|this
operator|.
name|properties
operator|.
name|getProperty
argument_list|(
name|MonkeyConstants
operator|.
name|PERIODIC_ACTION1_PERIOD
argument_list|,
name|MonkeyConstants
operator|.
name|DEFAULT_PERIODIC_ACTION1_PERIOD
operator|+
literal|""
argument_list|)
argument_list|)
expr_stmt|;
name|action2Period
operator|=
name|Long
operator|.
name|parseLong
argument_list|(
name|this
operator|.
name|properties
operator|.
name|getProperty
argument_list|(
name|MonkeyConstants
operator|.
name|PERIODIC_ACTION2_PERIOD
argument_list|,
name|MonkeyConstants
operator|.
name|DEFAULT_PERIODIC_ACTION2_PERIOD
operator|+
literal|""
argument_list|)
argument_list|)
expr_stmt|;
name|action3Period
operator|=
name|Long
operator|.
name|parseLong
argument_list|(
name|this
operator|.
name|properties
operator|.
name|getProperty
argument_list|(
name|MonkeyConstants
operator|.
name|COMPOSITE_ACTION3_PERIOD
argument_list|,
name|MonkeyConstants
operator|.
name|DEFAULT_COMPOSITE_ACTION3_PERIOD
operator|+
literal|""
argument_list|)
argument_list|)
expr_stmt|;
name|action4Period
operator|=
name|Long
operator|.
name|parseLong
argument_list|(
name|this
operator|.
name|properties
operator|.
name|getProperty
argument_list|(
name|MonkeyConstants
operator|.
name|PERIODIC_ACTION4_PERIOD
argument_list|,
name|MonkeyConstants
operator|.
name|DEFAULT_PERIODIC_ACTION4_PERIOD
operator|+
literal|""
argument_list|)
argument_list|)
expr_stmt|;
name|moveRegionsMaxTime
operator|=
name|Long
operator|.
name|parseLong
argument_list|(
name|this
operator|.
name|properties
operator|.
name|getProperty
argument_list|(
name|MonkeyConstants
operator|.
name|MOVE_REGIONS_MAX_TIME
argument_list|,
name|MonkeyConstants
operator|.
name|DEFAULT_MOVE_REGIONS_MAX_TIME
operator|+
literal|""
argument_list|)
argument_list|)
expr_stmt|;
name|moveRegionsSleepTime
operator|=
name|Long
operator|.
name|parseLong
argument_list|(
name|this
operator|.
name|properties
operator|.
name|getProperty
argument_list|(
name|MonkeyConstants
operator|.
name|MOVE_REGIONS_SLEEP_TIME
argument_list|,
name|MonkeyConstants
operator|.
name|DEFAULT_MOVE_REGIONS_SLEEP_TIME
operator|+
literal|""
argument_list|)
argument_list|)
expr_stmt|;
name|moveRandomRegionSleepTime
operator|=
name|Long
operator|.
name|parseLong
argument_list|(
name|this
operator|.
name|properties
operator|.
name|getProperty
argument_list|(
name|MonkeyConstants
operator|.
name|MOVE_RANDOM_REGION_SLEEP_TIME
argument_list|,
name|MonkeyConstants
operator|.
name|DEFAULT_MOVE_RANDOM_REGION_SLEEP_TIME
operator|+
literal|""
argument_list|)
argument_list|)
expr_stmt|;
name|restartRandomRSSleepTime
operator|=
name|Long
operator|.
name|parseLong
argument_list|(
name|this
operator|.
name|properties
operator|.
name|getProperty
argument_list|(
name|MonkeyConstants
operator|.
name|RESTART_RANDOM_RS_SLEEP_TIME
argument_list|,
name|MonkeyConstants
operator|.
name|DEFAULT_RESTART_RANDOM_RS_SLEEP_TIME
operator|+
literal|""
argument_list|)
argument_list|)
expr_stmt|;
name|batchRestartRSSleepTime
operator|=
name|Long
operator|.
name|parseLong
argument_list|(
name|this
operator|.
name|properties
operator|.
name|getProperty
argument_list|(
name|MonkeyConstants
operator|.
name|BATCH_RESTART_RS_SLEEP_TIME
argument_list|,
name|MonkeyConstants
operator|.
name|DEFAULT_BATCH_RESTART_RS_SLEEP_TIME
operator|+
literal|""
argument_list|)
argument_list|)
expr_stmt|;
name|restartActiveMasterSleepTime
operator|=
name|Long
operator|.
name|parseLong
argument_list|(
name|this
operator|.
name|properties
operator|.
name|getProperty
argument_list|(
name|MonkeyConstants
operator|.
name|RESTART_ACTIVE_MASTER_SLEEP_TIME
argument_list|,
name|MonkeyConstants
operator|.
name|DEFAULT_RESTART_ACTIVE_MASTER_SLEEP_TIME
operator|+
literal|""
argument_list|)
argument_list|)
expr_stmt|;
name|rollingBatchRestartRSSleepTime
operator|=
name|Long
operator|.
name|parseLong
argument_list|(
name|this
operator|.
name|properties
operator|.
name|getProperty
argument_list|(
name|MonkeyConstants
operator|.
name|ROLLING_BATCH_RESTART_RS_SLEEP_TIME
argument_list|,
name|MonkeyConstants
operator|.
name|DEFAULT_ROLLING_BATCH_RESTART_RS_SLEEP_TIME
operator|+
literal|""
argument_list|)
argument_list|)
expr_stmt|;
name|rollingBatchRestartRSRatio
operator|=
name|Float
operator|.
name|parseFloat
argument_list|(
name|this
operator|.
name|properties
operator|.
name|getProperty
argument_list|(
name|MonkeyConstants
operator|.
name|ROLLING_BATCH_RESTART_RS_RATIO
argument_list|,
name|MonkeyConstants
operator|.
name|DEFAULT_ROLLING_BATCH_RESTART_RS_RATIO
operator|+
literal|""
argument_list|)
argument_list|)
expr_stmt|;
name|restartRsHoldingMetaSleepTime
operator|=
name|Long
operator|.
name|parseLong
argument_list|(
name|this
operator|.
name|properties
operator|.
name|getProperty
argument_list|(
name|MonkeyConstants
operator|.
name|RESTART_RS_HOLDING_META_SLEEP_TIME
argument_list|,
name|MonkeyConstants
operator|.
name|DEFAULT_RESTART_RS_HOLDING_META_SLEEP_TIME
operator|+
literal|""
argument_list|)
argument_list|)
expr_stmt|;
name|compactTableRatio
operator|=
name|Float
operator|.
name|parseFloat
argument_list|(
name|this
operator|.
name|properties
operator|.
name|getProperty
argument_list|(
name|MonkeyConstants
operator|.
name|COMPACT_TABLE_ACTION_RATIO
argument_list|,
name|MonkeyConstants
operator|.
name|DEFAULT_COMPACT_TABLE_ACTION_RATIO
operator|+
literal|""
argument_list|)
argument_list|)
expr_stmt|;
name|compactRandomRegionRatio
operator|=
name|Float
operator|.
name|parseFloat
argument_list|(
name|this
operator|.
name|properties
operator|.
name|getProperty
argument_list|(
name|MonkeyConstants
operator|.
name|COMPACT_RANDOM_REGION_RATIO
argument_list|,
name|MonkeyConstants
operator|.
name|DEFAULT_COMPACT_RANDOM_REGION_RATIO
operator|+
literal|""
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

