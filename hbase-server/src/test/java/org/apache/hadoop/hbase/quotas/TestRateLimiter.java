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
name|MILLISECONDS
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
literal|3
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
block|}
end_class

end_unit

