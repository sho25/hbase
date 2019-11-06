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
name|ForceBalancerAction
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
name|GracefulRollingRestartRsAction
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
name|RestartRandomRsExceptMetaAction
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
name|RollingBatchRestartRsExceptMetaAction
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
name|RollingBatchSuspendResumeRsAction
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
comment|/**  * Creates ChaosMonkeys for doing server restart actions, but not  * flush / compact / snapshot kind of actions.  */
end_comment

begin_class
specifier|public
class|class
name|ServerKillingMonkeyFactory
extends|extends
name|MonkeyFactory
block|{
specifier|private
name|long
name|gracefulRollingRestartTSSLeepTime
decl_stmt|;
specifier|private
name|long
name|rollingBatchSuspendRSSleepTime
decl_stmt|;
specifier|private
name|float
name|rollingBatchSuspendtRSRatio
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
comment|// Destructive actions to mess things around. Cannot run batch restart
name|Action
index|[]
name|actions1
init|=
operator|new
name|Action
index|[]
block|{
operator|new
name|RestartRandomRsExceptMetaAction
argument_list|(
literal|60000
argument_list|)
block|,
operator|new
name|RestartActiveMasterAction
argument_list|(
literal|5000
argument_list|)
block|,
operator|new
name|RollingBatchRestartRsExceptMetaAction
argument_list|(
literal|5000
argument_list|,
literal|1.0f
argument_list|,
literal|2
argument_list|)
block|,
comment|//only allow 2 servers to be dead
operator|new
name|ForceBalancerAction
argument_list|()
block|,
operator|new
name|GracefulRollingRestartRsAction
argument_list|(
name|gracefulRollingRestartTSSLeepTime
argument_list|)
block|,
operator|new
name|RollingBatchSuspendResumeRsAction
argument_list|(
name|rollingBatchSuspendRSSleepTime
argument_list|,
name|rollingBatchSuspendtRSRatio
argument_list|)
block|}
decl_stmt|;
comment|// Action to log more info for debugging
name|Action
index|[]
name|actions2
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
name|CompositeSequentialPolicy
argument_list|(
operator|new
name|DoActionsOncePolicy
argument_list|(
literal|60
operator|*
literal|1000
argument_list|,
name|actions1
argument_list|)
argument_list|,
operator|new
name|PeriodicRandomActionPolicy
argument_list|(
literal|60
operator|*
literal|1000
argument_list|,
name|actions1
argument_list|)
argument_list|)
argument_list|,
operator|new
name|PeriodicRandomActionPolicy
argument_list|(
literal|60
operator|*
literal|1000
argument_list|,
name|actions2
argument_list|)
argument_list|)
return|;
block|}
specifier|private
name|void
name|loadProperties
parameter_list|()
block|{
name|gracefulRollingRestartTSSLeepTime
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
name|GRACEFUL_RESTART_RS_SLEEP_TIME
argument_list|,
name|MonkeyConstants
operator|.
name|DEFAULT_GRACEFUL_RESTART_RS_SLEEP_TIME
operator|+
literal|""
argument_list|)
argument_list|)
expr_stmt|;
name|rollingBatchSuspendRSSleepTime
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
name|ROLLING_BATCH_SUSPEND_RS_SLEEP_TIME
argument_list|,
name|MonkeyConstants
operator|.
name|DEFAULT_ROLLING_BATCH_SUSPEND_RS_SLEEP_TIME
operator|+
literal|""
argument_list|)
argument_list|)
expr_stmt|;
name|rollingBatchSuspendtRSRatio
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
name|ROLLING_BATCH_SUSPEND_RS_RATIO
argument_list|,
name|MonkeyConstants
operator|.
name|DEFAULT_ROLLING_BATCH_SUSPEND_RS_RATIO
operator|+
literal|""
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

