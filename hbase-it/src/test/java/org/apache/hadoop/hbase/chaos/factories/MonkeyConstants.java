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

begin_interface
specifier|public
interface|interface
name|MonkeyConstants
block|{
name|String
name|PERIODIC_ACTION1_PERIOD
init|=
literal|"sdm.action1.period"
decl_stmt|;
name|String
name|PERIODIC_ACTION2_PERIOD
init|=
literal|"sdm.action2.period"
decl_stmt|;
name|String
name|PERIODIC_ACTION4_PERIOD
init|=
literal|"sdm.action4.period"
decl_stmt|;
name|String
name|COMPOSITE_ACTION3_PERIOD
init|=
literal|"sdm.action3.period"
decl_stmt|;
name|String
name|MOVE_REGIONS_MAX_TIME
init|=
literal|"move.regions.max.time"
decl_stmt|;
name|String
name|MOVE_REGIONS_SLEEP_TIME
init|=
literal|"move.regions.sleep.time"
decl_stmt|;
name|String
name|MOVE_RANDOM_REGION_SLEEP_TIME
init|=
literal|"move.randomregion.sleep.time"
decl_stmt|;
name|String
name|RESTART_RANDOM_RS_SLEEP_TIME
init|=
literal|"restart.random.rs.sleep.time"
decl_stmt|;
name|String
name|BATCH_RESTART_RS_SLEEP_TIME
init|=
literal|"batch.restart.rs.sleep.time"
decl_stmt|;
name|String
name|BATCH_RESTART_RS_RATIO
init|=
literal|"batch.restart.rs.ratio"
decl_stmt|;
name|String
name|RESTART_ACTIVE_MASTER_SLEEP_TIME
init|=
literal|"restart.active.master.sleep.time"
decl_stmt|;
name|String
name|ROLLING_BATCH_RESTART_RS_SLEEP_TIME
init|=
literal|"rolling.batch.restart.rs.sleep.time"
decl_stmt|;
name|String
name|ROLLING_BATCH_RESTART_RS_RATIO
init|=
literal|"rolling.batch.restart.rs.ratio"
decl_stmt|;
name|String
name|RESTART_RS_HOLDING_META_SLEEP_TIME
init|=
literal|"restart.rs.holding.meta.sleep.time"
decl_stmt|;
name|String
name|COMPACT_TABLE_ACTION_RATIO
init|=
literal|"compact.table.ratio"
decl_stmt|;
name|String
name|COMPACT_RANDOM_REGION_RATIO
init|=
literal|"compact.random.region.ratio"
decl_stmt|;
name|String
name|UNBALANCE_CHAOS_EVERY_MS
init|=
literal|"unbalance.chaos.period"
decl_stmt|;
name|String
name|UNBALANCE_WAIT_FOR_UNBALANCE_MS
init|=
literal|"unbalance.action.wait.period"
decl_stmt|;
name|String
name|UNBALANCE_WAIT_FOR_KILLS_MS
init|=
literal|"unbalance.action.kill.period"
decl_stmt|;
name|String
name|UNBALANCE_WAIT_AFTER_BALANCE_MS
init|=
literal|"unbalance.action.wait.after.period"
decl_stmt|;
name|String
name|DECREASE_HFILE_SIZE_SLEEP_TIME
init|=
literal|"decrease.hfile.size.sleep.time"
decl_stmt|;
name|long
name|DEFAULT_PERIODIC_ACTION1_PERIOD
init|=
literal|60
operator|*
literal|1000
decl_stmt|;
name|long
name|DEFAULT_PERIODIC_ACTION2_PERIOD
init|=
literal|90
operator|*
literal|1000
decl_stmt|;
name|long
name|DEFAULT_PERIODIC_ACTION4_PERIOD
init|=
literal|90
operator|*
literal|1000
decl_stmt|;
name|long
name|DEFAULT_COMPOSITE_ACTION3_PERIOD
init|=
literal|150
operator|*
literal|1000
decl_stmt|;
name|long
name|DEFAULT_MOVE_REGIONS_MAX_TIME
init|=
literal|10
operator|*
literal|60
operator|*
literal|1000
decl_stmt|;
name|long
name|DEFAULT_MOVE_REGIONS_SLEEP_TIME
init|=
literal|800
decl_stmt|;
name|long
name|DEFAULT_MOVE_RANDOM_REGION_SLEEP_TIME
init|=
literal|800
decl_stmt|;
name|long
name|DEFAULT_RESTART_RANDOM_RS_SLEEP_TIME
init|=
literal|60000
decl_stmt|;
name|long
name|DEFAULT_BATCH_RESTART_RS_SLEEP_TIME
init|=
literal|5000
decl_stmt|;
name|float
name|DEFAULT_BATCH_RESTART_RS_RATIO
init|=
literal|0.5f
decl_stmt|;
name|long
name|DEFAULT_RESTART_ACTIVE_MASTER_SLEEP_TIME
init|=
literal|5000
decl_stmt|;
name|long
name|DEFAULT_ROLLING_BATCH_RESTART_RS_SLEEP_TIME
init|=
literal|5000
decl_stmt|;
name|float
name|DEFAULT_ROLLING_BATCH_RESTART_RS_RATIO
init|=
literal|1.0f
decl_stmt|;
name|long
name|DEFAULT_RESTART_RS_HOLDING_META_SLEEP_TIME
init|=
literal|35000
decl_stmt|;
name|float
name|DEFAULT_COMPACT_TABLE_ACTION_RATIO
init|=
literal|0.5f
decl_stmt|;
name|float
name|DEFAULT_COMPACT_RANDOM_REGION_RATIO
init|=
literal|0.6f
decl_stmt|;
name|long
name|DEFAULT_UNBALANCE_CHAOS_EVERY_MS
init|=
literal|65
operator|*
literal|1000
decl_stmt|;
name|long
name|DEFAULT_UNBALANCE_WAIT_FOR_UNBALANCE_MS
init|=
literal|2
operator|*
literal|1000
decl_stmt|;
name|long
name|DEFAULT_UNBALANCE_WAIT_FOR_KILLS_MS
init|=
literal|2
operator|*
literal|1000
decl_stmt|;
name|long
name|DEFAULT_UNBALANCE_WAIT_AFTER_BALANCE_MS
init|=
literal|5
operator|*
literal|1000
decl_stmt|;
name|long
name|DEFAULT_DECREASE_HFILE_SIZE_SLEEP_TIME
init|=
literal|30
operator|*
literal|1000
decl_stmt|;
block|}
end_interface

end_unit

