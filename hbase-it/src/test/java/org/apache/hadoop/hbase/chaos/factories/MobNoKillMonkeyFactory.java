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
name|PeriodicRandomActionPolicy
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
name|TwoConcurrentActionPolicy
import|;
end_import

begin_comment
comment|/**  * This is a copy of NoKillMonkeyFactory that also does mob compactions.  */
end_comment

begin_class
specifier|public
class|class
name|MobNoKillMonkeyFactory
extends|extends
name|MonkeyFactory
block|{
annotation|@
name|Override
specifier|public
name|ChaosMonkey
name|build
parameter_list|()
block|{
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
name|MonkeyConstants
operator|.
name|DEFAULT_PERIODIC_ACTION1_PERIOD
argument_list|)
block|,
operator|new
name|CompactTableAction
argument_list|(
name|tableName
argument_list|,
name|MonkeyConstants
operator|.
name|DEFAULT_PERIODIC_ACTION1_PERIOD
argument_list|)
block|,
operator|new
name|CompactRandomRegionOfTableAction
argument_list|(
name|tableName
argument_list|,
name|MonkeyConstants
operator|.
name|DEFAULT_COMPACT_RANDOM_REGION_RATIO
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
name|MonkeyConstants
operator|.
name|DEFAULT_MOVE_REGIONS_SLEEP_TIME
argument_list|,
name|MonkeyConstants
operator|.
name|DEFAULT_MOVE_REGIONS_MAX_TIME
argument_list|,
name|tableName
argument_list|)
block|,
operator|new
name|MoveRandomRegionOfTableAction
argument_list|(
name|MonkeyConstants
operator|.
name|DEFAULT_RESTART_ACTIVE_MASTER_SLEEP_TIME
argument_list|,
name|tableName
argument_list|)
block|, }
decl_stmt|;
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
name|util
argument_list|,
operator|new
name|TwoConcurrentActionPolicy
argument_list|(
name|MonkeyConstants
operator|.
name|DEFAULT_PERIODIC_ACTION1_PERIOD
argument_list|,
name|actions1
argument_list|,
name|actions2
argument_list|)
argument_list|,
operator|new
name|PeriodicRandomActionPolicy
argument_list|(
name|MonkeyConstants
operator|.
name|DEFAULT_PERIODIC_ACTION2_PERIOD
argument_list|,
name|actions3
argument_list|)
argument_list|,
operator|new
name|PeriodicRandomActionPolicy
argument_list|(
name|MonkeyConstants
operator|.
name|DEFAULT_PERIODIC_ACTION4_PERIOD
argument_list|,
name|actions4
argument_list|)
argument_list|)
return|;
block|}
block|}
end_class

end_unit

