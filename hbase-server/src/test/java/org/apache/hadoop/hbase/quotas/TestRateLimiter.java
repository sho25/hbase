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
name|quotas
package|;
end_package

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertEquals
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertFalse
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertTrue
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
name|hbase
operator|.
name|HBaseClassTestRule
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
name|testclassification
operator|.
name|RegionServerTests
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
name|testclassification
operator|.
name|SmallTests
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
name|EnvironmentEdge
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
name|hbase
operator|.
name|util
operator|.
name|ManualEnvironmentEdge
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|ClassRule
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Test
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|experimental
operator|.
name|categories
operator|.
name|Category
import|;
end_import

begin_comment
comment|/**  * Verify the behaviour of the Rate Limiter.  */
end_comment

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|RegionServerTests
operator|.
name|class
block|,
name|SmallTests
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|TestRateLimiter
block|{
annotation|@
name|ClassRule
specifier|public
specifier|static
specifier|final
name|HBaseClassTestRule
name|CLASS_RULE
init|=
name|HBaseClassTestRule
operator|.
name|forClass
argument_list|(
name|TestRateLimiter
operator|.
name|class
argument_list|)
decl_stmt|;
annotation|@
name|Test
specifier|public
name|void
name|testWaitIntervalTimeUnitSeconds
parameter_list|()
block|{
name|testWaitInterval
argument_list|(
name|TimeUnit
operator|.
name|SECONDS
argument_list|,
literal|10
argument_list|,
literal|100
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testWaitIntervalTimeUnitMinutes
parameter_list|()
block|{
name|testWaitInterval
argument_list|(
name|TimeUnit
operator|.
name|MINUTES
argument_list|,
literal|10
argument_list|,
literal|6000
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testWaitIntervalTimeUnitHours
parameter_list|()
block|{
name|testWaitInterval
argument_list|(
name|TimeUnit
operator|.
name|HOURS
argument_list|,
literal|10
argument_list|,
literal|360000
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testWaitIntervalTimeUnitDays
parameter_list|()
block|{
name|testWaitInterval
argument_list|(
name|TimeUnit
operator|.
name|DAYS
argument_list|,
literal|10
argument_list|,
literal|8640000
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|testWaitInterval
parameter_list|(
specifier|final
name|TimeUnit
name|timeUnit
parameter_list|,
specifier|final
name|long
name|limit
parameter_list|,
specifier|final
name|long
name|expectedWaitInterval
parameter_list|)
block|{
name|RateLimiter
name|limiter
init|=
operator|new
name|AverageIntervalRateLimiter
argument_list|()
decl_stmt|;
name|limiter
operator|.
name|set
argument_list|(
name|limit
argument_list|,
name|timeUnit
argument_list|)
expr_stmt|;
name|long
name|nowTs
init|=
literal|0
decl_stmt|;
comment|// consume all the available resources, one request at the time.
comment|// the wait interval should be 0
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
operator|(
name|limit
operator|-
literal|1
operator|)
condition|;
operator|++
name|i
control|)
block|{
name|assertTrue
argument_list|(
name|limiter
operator|.
name|canExecute
argument_list|()
argument_list|)
expr_stmt|;
name|limiter
operator|.
name|consume
argument_list|()
expr_stmt|;
name|long
name|waitInterval
init|=
name|limiter
operator|.
name|waitInterval
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|waitInterval
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
operator|(
name|limit
operator|*
literal|4
operator|)
condition|;
operator|++
name|i
control|)
block|{
comment|// There is one resource available, so we should be able to
comment|// consume it without waiting.
name|limiter
operator|.
name|setNextRefillTime
argument_list|(
name|limiter
operator|.
name|getNextRefillTime
argument_list|()
operator|-
name|nowTs
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|limiter
operator|.
name|canExecute
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|limiter
operator|.
name|waitInterval
argument_list|()
argument_list|)
expr_stmt|;
name|limiter
operator|.
name|consume
argument_list|()
expr_stmt|;
comment|// No more resources are available, we should wait for at least an interval.
name|long
name|waitInterval
init|=
name|limiter
operator|.
name|waitInterval
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
name|expectedWaitInterval
argument_list|,
name|waitInterval
argument_list|)
expr_stmt|;
comment|// set the nowTs to be the exact time when resources should be available again.
name|nowTs
operator|=
name|waitInterval
expr_stmt|;
comment|// artificially go into the past to prove that when too early we should fail.
name|long
name|temp
init|=
name|nowTs
operator|+
literal|500
decl_stmt|;
name|limiter
operator|.
name|setNextRefillTime
argument_list|(
name|limiter
operator|.
name|getNextRefillTime
argument_list|()
operator|+
name|temp
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|limiter
operator|.
name|canExecute
argument_list|()
argument_list|)
expr_stmt|;
comment|//Roll back the nextRefillTime set to continue further testing
name|limiter
operator|.
name|setNextRefillTime
argument_list|(
name|limiter
operator|.
name|getNextRefillTime
argument_list|()
operator|-
name|temp
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testOverconsumptionAverageIntervalRefillStrategy
parameter_list|()
block|{
name|RateLimiter
name|limiter
init|=
operator|new
name|AverageIntervalRateLimiter
argument_list|()
decl_stmt|;
name|limiter
operator|.
name|set
argument_list|(
literal|10
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
expr_stmt|;
comment|// 10 resources are available, but we need to consume 20 resources
comment|// Verify that we have to wait at least 1.1sec to have 1 resource available
name|assertTrue
argument_list|(
name|limiter
operator|.
name|canExecute
argument_list|()
argument_list|)
expr_stmt|;
name|limiter
operator|.
name|consume
argument_list|(
literal|20
argument_list|)
expr_stmt|;
comment|// To consume 1 resource wait for 100ms
name|assertEquals
argument_list|(
literal|100
argument_list|,
name|limiter
operator|.
name|waitInterval
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
comment|// To consume 10 resource wait for 1000ms
name|assertEquals
argument_list|(
literal|1000
argument_list|,
name|limiter
operator|.
name|waitInterval
argument_list|(
literal|10
argument_list|)
argument_list|)
expr_stmt|;
name|limiter
operator|.
name|setNextRefillTime
argument_list|(
name|limiter
operator|.
name|getNextRefillTime
argument_list|()
operator|-
literal|900
argument_list|)
expr_stmt|;
comment|// Verify that after 1sec the 1 resource is available
name|assertTrue
argument_list|(
name|limiter
operator|.
name|canExecute
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|limiter
operator|.
name|setNextRefillTime
argument_list|(
name|limiter
operator|.
name|getNextRefillTime
argument_list|()
operator|-
literal|100
argument_list|)
expr_stmt|;
comment|// Verify that after 1sec the 10 resource is available
name|assertTrue
argument_list|(
name|limiter
operator|.
name|canExecute
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|limiter
operator|.
name|waitInterval
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testOverconsumptionFixedIntervalRefillStrategy
parameter_list|()
throws|throws
name|InterruptedException
block|{
name|RateLimiter
name|limiter
init|=
operator|new
name|FixedIntervalRateLimiter
argument_list|()
decl_stmt|;
name|limiter
operator|.
name|set
argument_list|(
literal|10
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
expr_stmt|;
comment|// fix the current time in order to get the precise value of interval
name|EnvironmentEdge
name|edge
init|=
operator|new
name|EnvironmentEdge
argument_list|()
block|{
specifier|private
specifier|final
name|long
name|ts
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
annotation|@
name|Override
specifier|public
name|long
name|currentTime
parameter_list|()
block|{
return|return
name|ts
return|;
block|}
block|}
decl_stmt|;
name|EnvironmentEdgeManager
operator|.
name|injectEdge
argument_list|(
name|edge
argument_list|)
expr_stmt|;
comment|// 10 resources are available, but we need to consume 20 resources
comment|// Verify that we have to wait at least 1.1sec to have 1 resource available
name|assertTrue
argument_list|(
name|limiter
operator|.
name|canExecute
argument_list|()
argument_list|)
expr_stmt|;
name|limiter
operator|.
name|consume
argument_list|(
literal|20
argument_list|)
expr_stmt|;
comment|// To consume 1 resource also wait for 1000ms
name|assertEquals
argument_list|(
literal|1000
argument_list|,
name|limiter
operator|.
name|waitInterval
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
comment|// To consume 10 resource wait for 100ms
name|assertEquals
argument_list|(
literal|1000
argument_list|,
name|limiter
operator|.
name|waitInterval
argument_list|(
literal|10
argument_list|)
argument_list|)
expr_stmt|;
name|EnvironmentEdgeManager
operator|.
name|reset
argument_list|()
expr_stmt|;
name|limiter
operator|.
name|setNextRefillTime
argument_list|(
name|limiter
operator|.
name|getNextRefillTime
argument_list|()
operator|-
literal|900
argument_list|)
expr_stmt|;
comment|// Verify that after 1sec also no resource should be available
name|assertFalse
argument_list|(
name|limiter
operator|.
name|canExecute
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|limiter
operator|.
name|setNextRefillTime
argument_list|(
name|limiter
operator|.
name|getNextRefillTime
argument_list|()
operator|-
literal|100
argument_list|)
expr_stmt|;
comment|// Verify that after 1sec the 10 resource is available
name|assertTrue
argument_list|(
name|limiter
operator|.
name|canExecute
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|limiter
operator|.
name|waitInterval
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testFixedIntervalResourceAvailability
parameter_list|()
throws|throws
name|Exception
block|{
name|RateLimiter
name|limiter
init|=
operator|new
name|FixedIntervalRateLimiter
argument_list|()
decl_stmt|;
name|limiter
operator|.
name|set
argument_list|(
literal|10
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|limiter
operator|.
name|canExecute
argument_list|(
literal|10
argument_list|)
argument_list|)
expr_stmt|;
name|limiter
operator|.
name|consume
argument_list|(
literal|3
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|7
argument_list|,
name|limiter
operator|.
name|getAvailable
argument_list|()
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|limiter
operator|.
name|canExecute
argument_list|(
literal|10
argument_list|)
argument_list|)
expr_stmt|;
name|limiter
operator|.
name|setNextRefillTime
argument_list|(
name|limiter
operator|.
name|getNextRefillTime
argument_list|()
operator|-
literal|1000
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|limiter
operator|.
name|canExecute
argument_list|(
literal|10
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|10
argument_list|,
name|limiter
operator|.
name|getAvailable
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testLimiterBySmallerRate
parameter_list|()
throws|throws
name|InterruptedException
block|{
comment|// set limiter is 10 resources per seconds
name|RateLimiter
name|limiter
init|=
operator|new
name|FixedIntervalRateLimiter
argument_list|()
decl_stmt|;
name|limiter
operator|.
name|set
argument_list|(
literal|10
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
expr_stmt|;
name|int
name|count
init|=
literal|0
decl_stmt|;
comment|// control the test count
while|while
condition|(
operator|(
name|count
operator|++
operator|)
operator|<
literal|10
condition|)
block|{
comment|// test will get 3 resources per 0.5 sec. so it will get 6 resources per sec.
name|limiter
operator|.
name|setNextRefillTime
argument_list|(
name|limiter
operator|.
name|getNextRefillTime
argument_list|()
operator|-
literal|500
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
literal|3
condition|;
name|i
operator|++
control|)
block|{
comment|// 6 resources/sec< limit, so limiter.canExecute(nowTs, lastTs) should be true
name|assertEquals
argument_list|(
literal|true
argument_list|,
name|limiter
operator|.
name|canExecute
argument_list|()
argument_list|)
expr_stmt|;
name|limiter
operator|.
name|consume
argument_list|()
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testCanExecuteOfAverageIntervalRateLimiter
parameter_list|()
throws|throws
name|InterruptedException
block|{
name|RateLimiter
name|limiter
init|=
operator|new
name|AverageIntervalRateLimiter
argument_list|()
decl_stmt|;
comment|// when set limit is 100 per sec, this AverageIntervalRateLimiter will support at max 200 per sec
name|limiter
operator|.
name|set
argument_list|(
literal|100
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
expr_stmt|;
name|limiter
operator|.
name|setNextRefillTime
argument_list|(
name|EnvironmentEdgeManager
operator|.
name|currentTime
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|50
argument_list|,
name|testCanExecuteByRate
argument_list|(
name|limiter
argument_list|,
literal|50
argument_list|)
argument_list|)
expr_stmt|;
comment|// refill the avail to limit
name|limiter
operator|.
name|set
argument_list|(
literal|100
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
expr_stmt|;
name|limiter
operator|.
name|setNextRefillTime
argument_list|(
name|EnvironmentEdgeManager
operator|.
name|currentTime
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|100
argument_list|,
name|testCanExecuteByRate
argument_list|(
name|limiter
argument_list|,
literal|100
argument_list|)
argument_list|)
expr_stmt|;
comment|// refill the avail to limit
name|limiter
operator|.
name|set
argument_list|(
literal|100
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
expr_stmt|;
name|limiter
operator|.
name|setNextRefillTime
argument_list|(
name|EnvironmentEdgeManager
operator|.
name|currentTime
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|200
argument_list|,
name|testCanExecuteByRate
argument_list|(
name|limiter
argument_list|,
literal|200
argument_list|)
argument_list|)
expr_stmt|;
comment|// refill the avail to limit
name|limiter
operator|.
name|set
argument_list|(
literal|100
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
expr_stmt|;
name|limiter
operator|.
name|setNextRefillTime
argument_list|(
name|EnvironmentEdgeManager
operator|.
name|currentTime
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|200
argument_list|,
name|testCanExecuteByRate
argument_list|(
name|limiter
argument_list|,
literal|500
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testCanExecuteOfFixedIntervalRateLimiter
parameter_list|()
throws|throws
name|InterruptedException
block|{
name|RateLimiter
name|limiter
init|=
operator|new
name|FixedIntervalRateLimiter
argument_list|()
decl_stmt|;
comment|// when set limit is 100 per sec, this FixedIntervalRateLimiter will support at max 100 per sec
name|limiter
operator|.
name|set
argument_list|(
literal|100
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
expr_stmt|;
name|limiter
operator|.
name|setNextRefillTime
argument_list|(
name|EnvironmentEdgeManager
operator|.
name|currentTime
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|50
argument_list|,
name|testCanExecuteByRate
argument_list|(
name|limiter
argument_list|,
literal|50
argument_list|)
argument_list|)
expr_stmt|;
comment|// refill the avail to limit
name|limiter
operator|.
name|set
argument_list|(
literal|100
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
expr_stmt|;
name|limiter
operator|.
name|setNextRefillTime
argument_list|(
name|EnvironmentEdgeManager
operator|.
name|currentTime
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|100
argument_list|,
name|testCanExecuteByRate
argument_list|(
name|limiter
argument_list|,
literal|100
argument_list|)
argument_list|)
expr_stmt|;
comment|// refill the avail to limit
name|limiter
operator|.
name|set
argument_list|(
literal|100
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
expr_stmt|;
name|limiter
operator|.
name|setNextRefillTime
argument_list|(
name|EnvironmentEdgeManager
operator|.
name|currentTime
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|100
argument_list|,
name|testCanExecuteByRate
argument_list|(
name|limiter
argument_list|,
literal|200
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|public
name|int
name|testCanExecuteByRate
parameter_list|(
name|RateLimiter
name|limiter
parameter_list|,
name|int
name|rate
parameter_list|)
block|{
name|int
name|request
init|=
literal|0
decl_stmt|;
name|int
name|count
init|=
literal|0
decl_stmt|;
while|while
condition|(
operator|(
name|request
operator|++
operator|)
operator|<
name|rate
condition|)
block|{
name|limiter
operator|.
name|setNextRefillTime
argument_list|(
name|limiter
operator|.
name|getNextRefillTime
argument_list|()
operator|-
name|limiter
operator|.
name|getTimeUnitInMillis
argument_list|()
operator|/
name|rate
argument_list|)
expr_stmt|;
if|if
condition|(
name|limiter
operator|.
name|canExecute
argument_list|()
condition|)
block|{
name|count
operator|++
expr_stmt|;
name|limiter
operator|.
name|consume
argument_list|()
expr_stmt|;
block|}
block|}
return|return
name|count
return|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testRefillOfAverageIntervalRateLimiter
parameter_list|()
throws|throws
name|InterruptedException
block|{
name|RateLimiter
name|limiter
init|=
operator|new
name|AverageIntervalRateLimiter
argument_list|()
decl_stmt|;
name|limiter
operator|.
name|set
argument_list|(
literal|60
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|60
argument_list|,
name|limiter
operator|.
name|getAvailable
argument_list|()
argument_list|)
expr_stmt|;
comment|// first refill, will return the number same with limit
name|assertEquals
argument_list|(
literal|60
argument_list|,
name|limiter
operator|.
name|refill
argument_list|(
name|limiter
operator|.
name|getLimit
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|limiter
operator|.
name|consume
argument_list|(
literal|30
argument_list|)
expr_stmt|;
comment|// after 0.2 sec, refill should return 12
name|limiter
operator|.
name|setNextRefillTime
argument_list|(
name|limiter
operator|.
name|getNextRefillTime
argument_list|()
operator|-
literal|200
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|12
argument_list|,
name|limiter
operator|.
name|refill
argument_list|(
name|limiter
operator|.
name|getLimit
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
comment|// after 0.5 sec, refill should return 30
name|limiter
operator|.
name|setNextRefillTime
argument_list|(
name|limiter
operator|.
name|getNextRefillTime
argument_list|()
operator|-
literal|500
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|30
argument_list|,
name|limiter
operator|.
name|refill
argument_list|(
name|limiter
operator|.
name|getLimit
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
comment|// after 1 sec, refill should return 60
name|limiter
operator|.
name|setNextRefillTime
argument_list|(
name|limiter
operator|.
name|getNextRefillTime
argument_list|()
operator|-
literal|1000
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|60
argument_list|,
name|limiter
operator|.
name|refill
argument_list|(
name|limiter
operator|.
name|getLimit
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
comment|// after more than 1 sec, refill should return at max 60
name|limiter
operator|.
name|setNextRefillTime
argument_list|(
name|limiter
operator|.
name|getNextRefillTime
argument_list|()
operator|-
literal|3000
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|60
argument_list|,
name|limiter
operator|.
name|refill
argument_list|(
name|limiter
operator|.
name|getLimit
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|limiter
operator|.
name|setNextRefillTime
argument_list|(
name|limiter
operator|.
name|getNextRefillTime
argument_list|()
operator|-
literal|5000
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|60
argument_list|,
name|limiter
operator|.
name|refill
argument_list|(
name|limiter
operator|.
name|getLimit
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testRefillOfFixedIntervalRateLimiter
parameter_list|()
throws|throws
name|InterruptedException
block|{
name|RateLimiter
name|limiter
init|=
operator|new
name|FixedIntervalRateLimiter
argument_list|()
decl_stmt|;
name|limiter
operator|.
name|set
argument_list|(
literal|60
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|60
argument_list|,
name|limiter
operator|.
name|getAvailable
argument_list|()
argument_list|)
expr_stmt|;
comment|// first refill, will return the number same with limit
name|assertEquals
argument_list|(
literal|60
argument_list|,
name|limiter
operator|.
name|refill
argument_list|(
name|limiter
operator|.
name|getLimit
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|limiter
operator|.
name|consume
argument_list|(
literal|30
argument_list|)
expr_stmt|;
comment|// after 0.2 sec, refill should return 0
name|limiter
operator|.
name|setNextRefillTime
argument_list|(
name|limiter
operator|.
name|getNextRefillTime
argument_list|()
operator|-
literal|200
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|limiter
operator|.
name|refill
argument_list|(
name|limiter
operator|.
name|getLimit
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
comment|// after 0.5 sec, refill should return 0
name|limiter
operator|.
name|setNextRefillTime
argument_list|(
name|limiter
operator|.
name|getNextRefillTime
argument_list|()
operator|-
literal|500
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|limiter
operator|.
name|refill
argument_list|(
name|limiter
operator|.
name|getLimit
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
comment|// after 1 sec, refill should return 60
name|limiter
operator|.
name|setNextRefillTime
argument_list|(
name|limiter
operator|.
name|getNextRefillTime
argument_list|()
operator|-
literal|1000
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|60
argument_list|,
name|limiter
operator|.
name|refill
argument_list|(
name|limiter
operator|.
name|getLimit
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
comment|// after more than 1 sec, refill should return at max 60
name|limiter
operator|.
name|setNextRefillTime
argument_list|(
name|limiter
operator|.
name|getNextRefillTime
argument_list|()
operator|-
literal|3000
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|60
argument_list|,
name|limiter
operator|.
name|refill
argument_list|(
name|limiter
operator|.
name|getLimit
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|limiter
operator|.
name|setNextRefillTime
argument_list|(
name|limiter
operator|.
name|getNextRefillTime
argument_list|()
operator|-
literal|5000
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|60
argument_list|,
name|limiter
operator|.
name|refill
argument_list|(
name|limiter
operator|.
name|getLimit
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testUnconfiguredLimiters
parameter_list|()
throws|throws
name|InterruptedException
block|{
name|ManualEnvironmentEdge
name|testEdge
init|=
operator|new
name|ManualEnvironmentEdge
argument_list|()
decl_stmt|;
name|EnvironmentEdgeManager
operator|.
name|injectEdge
argument_list|(
name|testEdge
argument_list|)
expr_stmt|;
name|long
name|limit
init|=
name|Long
operator|.
name|MAX_VALUE
decl_stmt|;
comment|// For unconfigured limiters, it is supposed to use as much as possible
name|RateLimiter
name|avgLimiter
init|=
operator|new
name|AverageIntervalRateLimiter
argument_list|()
decl_stmt|;
name|RateLimiter
name|fixLimiter
init|=
operator|new
name|FixedIntervalRateLimiter
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
name|limit
argument_list|,
name|avgLimiter
operator|.
name|getAvailable
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|limit
argument_list|,
name|fixLimiter
operator|.
name|getAvailable
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|avgLimiter
operator|.
name|canExecute
argument_list|(
name|limit
argument_list|)
argument_list|)
expr_stmt|;
name|avgLimiter
operator|.
name|consume
argument_list|(
name|limit
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|fixLimiter
operator|.
name|canExecute
argument_list|(
name|limit
argument_list|)
argument_list|)
expr_stmt|;
name|fixLimiter
operator|.
name|consume
argument_list|(
name|limit
argument_list|)
expr_stmt|;
comment|// Make sure that available is Long.MAX_VALUE
name|assertTrue
argument_list|(
name|limit
operator|==
name|avgLimiter
operator|.
name|getAvailable
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|limit
operator|==
name|fixLimiter
operator|.
name|getAvailable
argument_list|()
argument_list|)
expr_stmt|;
comment|// after 100 millseconds, it should be able to execute limit as well
name|testEdge
operator|.
name|incValue
argument_list|(
literal|100
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|avgLimiter
operator|.
name|canExecute
argument_list|(
name|limit
argument_list|)
argument_list|)
expr_stmt|;
name|avgLimiter
operator|.
name|consume
argument_list|(
name|limit
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|fixLimiter
operator|.
name|canExecute
argument_list|(
name|limit
argument_list|)
argument_list|)
expr_stmt|;
name|fixLimiter
operator|.
name|consume
argument_list|(
name|limit
argument_list|)
expr_stmt|;
comment|// Make sure that available is Long.MAX_VALUE
name|assertTrue
argument_list|(
name|limit
operator|==
name|avgLimiter
operator|.
name|getAvailable
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|limit
operator|==
name|fixLimiter
operator|.
name|getAvailable
argument_list|()
argument_list|)
expr_stmt|;
name|EnvironmentEdgeManager
operator|.
name|reset
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testExtremeLimiters
parameter_list|()
throws|throws
name|InterruptedException
block|{
name|ManualEnvironmentEdge
name|testEdge
init|=
operator|new
name|ManualEnvironmentEdge
argument_list|()
decl_stmt|;
name|EnvironmentEdgeManager
operator|.
name|injectEdge
argument_list|(
name|testEdge
argument_list|)
expr_stmt|;
name|long
name|limit
init|=
name|Long
operator|.
name|MAX_VALUE
operator|-
literal|1
decl_stmt|;
name|RateLimiter
name|avgLimiter
init|=
operator|new
name|AverageIntervalRateLimiter
argument_list|()
decl_stmt|;
name|avgLimiter
operator|.
name|set
argument_list|(
name|limit
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
expr_stmt|;
name|RateLimiter
name|fixLimiter
init|=
operator|new
name|FixedIntervalRateLimiter
argument_list|()
decl_stmt|;
name|fixLimiter
operator|.
name|set
argument_list|(
name|limit
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|limit
argument_list|,
name|avgLimiter
operator|.
name|getAvailable
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|limit
argument_list|,
name|fixLimiter
operator|.
name|getAvailable
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|avgLimiter
operator|.
name|canExecute
argument_list|(
name|limit
operator|/
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|avgLimiter
operator|.
name|consume
argument_list|(
name|limit
operator|/
literal|2
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|fixLimiter
operator|.
name|canExecute
argument_list|(
name|limit
operator|/
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|fixLimiter
operator|.
name|consume
argument_list|(
name|limit
operator|/
literal|2
argument_list|)
expr_stmt|;
comment|// Make sure that available is whatever left
name|assertTrue
argument_list|(
operator|(
name|limit
operator|-
operator|(
name|limit
operator|/
literal|2
operator|)
operator|)
operator|==
name|avgLimiter
operator|.
name|getAvailable
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
operator|(
name|limit
operator|-
operator|(
name|limit
operator|/
literal|2
operator|)
operator|)
operator|==
name|fixLimiter
operator|.
name|getAvailable
argument_list|()
argument_list|)
expr_stmt|;
comment|// after 100 millseconds, both should not be able to execute the limit
name|testEdge
operator|.
name|incValue
argument_list|(
literal|100
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|avgLimiter
operator|.
name|canExecute
argument_list|(
name|limit
argument_list|)
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|fixLimiter
operator|.
name|canExecute
argument_list|(
name|limit
argument_list|)
argument_list|)
expr_stmt|;
comment|// after 500 millseconds, average interval limiter should be able to execute the limit
name|testEdge
operator|.
name|incValue
argument_list|(
literal|500
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|avgLimiter
operator|.
name|canExecute
argument_list|(
name|limit
argument_list|)
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|fixLimiter
operator|.
name|canExecute
argument_list|(
name|limit
argument_list|)
argument_list|)
expr_stmt|;
comment|// Make sure that available is correct
name|assertTrue
argument_list|(
name|limit
operator|==
name|avgLimiter
operator|.
name|getAvailable
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
operator|(
name|limit
operator|-
operator|(
name|limit
operator|/
literal|2
operator|)
operator|)
operator|==
name|fixLimiter
operator|.
name|getAvailable
argument_list|()
argument_list|)
expr_stmt|;
comment|// after 500 millseconds, both should be able to execute
name|testEdge
operator|.
name|incValue
argument_list|(
literal|500
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|avgLimiter
operator|.
name|canExecute
argument_list|(
name|limit
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|fixLimiter
operator|.
name|canExecute
argument_list|(
name|limit
argument_list|)
argument_list|)
expr_stmt|;
comment|// Make sure that available is Long.MAX_VALUE
name|assertTrue
argument_list|(
name|limit
operator|==
name|avgLimiter
operator|.
name|getAvailable
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|limit
operator|==
name|fixLimiter
operator|.
name|getAvailable
argument_list|()
argument_list|)
expr_stmt|;
name|EnvironmentEdgeManager
operator|.
name|reset
argument_list|()
expr_stmt|;
block|}
comment|/*    * This test case is tricky. Basically, it simulates the following events:    *           Thread-1                             Thread-2    * t0:  canExecute(100) and consume(100)    * t1:                                         canExecute(100), avail may be increased by 80    * t2:  consume(-80) as actual size is 20    * It will check if consume(-80) can handle overflow correctly.    */
annotation|@
name|Test
specifier|public
name|void
name|testLimiterCompensationOverflow
parameter_list|()
throws|throws
name|InterruptedException
block|{
name|long
name|limit
init|=
name|Long
operator|.
name|MAX_VALUE
operator|-
literal|1
decl_stmt|;
name|long
name|guessNumber
init|=
literal|100
decl_stmt|;
comment|// For unconfigured limiters, it is supposed to use as much as possible
name|RateLimiter
name|avgLimiter
init|=
operator|new
name|AverageIntervalRateLimiter
argument_list|()
decl_stmt|;
name|avgLimiter
operator|.
name|set
argument_list|(
name|limit
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|limit
argument_list|,
name|avgLimiter
operator|.
name|getAvailable
argument_list|()
argument_list|)
expr_stmt|;
comment|// The initial guess is that 100 bytes.
name|assertTrue
argument_list|(
name|avgLimiter
operator|.
name|canExecute
argument_list|(
name|guessNumber
argument_list|)
argument_list|)
expr_stmt|;
name|avgLimiter
operator|.
name|consume
argument_list|(
name|guessNumber
argument_list|)
expr_stmt|;
comment|// Make sure that available is whatever left
name|assertTrue
argument_list|(
operator|(
name|limit
operator|-
name|guessNumber
operator|)
operator|==
name|avgLimiter
operator|.
name|getAvailable
argument_list|()
argument_list|)
expr_stmt|;
comment|// Manually set avil to simulate that another thread call canExecute().
comment|// It is simulated by consume().
name|avgLimiter
operator|.
name|consume
argument_list|(
operator|-
literal|80
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
operator|(
name|limit
operator|-
name|guessNumber
operator|+
literal|80
operator|)
operator|==
name|avgLimiter
operator|.
name|getAvailable
argument_list|()
argument_list|)
expr_stmt|;
comment|// Now thread1 compensates 80
name|avgLimiter
operator|.
name|consume
argument_list|(
operator|-
literal|80
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|limit
operator|==
name|avgLimiter
operator|.
name|getAvailable
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

