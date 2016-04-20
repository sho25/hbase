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
name|replication
operator|.
name|regionserver
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
name|assertTrue
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
name|testclassification
operator|.
name|ReplicationTests
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

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|ReplicationTests
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
name|TestReplicationThrottler
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
name|TestReplicationThrottler
operator|.
name|class
argument_list|)
decl_stmt|;
comment|/**    * unit test for throttling    */
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|10000
argument_list|)
specifier|public
name|void
name|testThrottling
parameter_list|()
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"testThrottling"
argument_list|)
expr_stmt|;
comment|// throttle bandwidth is 100 and 10 bytes/cycle respectively
name|ReplicationThrottler
name|throttler1
init|=
operator|new
name|ReplicationThrottler
argument_list|(
literal|100
argument_list|)
decl_stmt|;
name|ReplicationThrottler
name|throttler2
init|=
operator|new
name|ReplicationThrottler
argument_list|(
literal|10
argument_list|)
decl_stmt|;
name|long
name|ticks1
init|=
name|throttler1
operator|.
name|getNextSleepInterval
argument_list|(
literal|1000
argument_list|)
decl_stmt|;
name|long
name|ticks2
init|=
name|throttler2
operator|.
name|getNextSleepInterval
argument_list|(
literal|1000
argument_list|)
decl_stmt|;
comment|// 1. the first push size is 1000, though 1000 bytes exceeds 100/10
comment|//    bandwidthes, but no sleep since it's the first push of current
comment|//    cycle, amortizing occurs when next push arrives
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|ticks1
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|ticks2
argument_list|)
expr_stmt|;
name|throttler1
operator|.
name|addPushSize
argument_list|(
literal|1000
argument_list|)
expr_stmt|;
name|throttler2
operator|.
name|addPushSize
argument_list|(
literal|1000
argument_list|)
expr_stmt|;
name|ticks1
operator|=
name|throttler1
operator|.
name|getNextSleepInterval
argument_list|(
literal|5
argument_list|)
expr_stmt|;
name|ticks2
operator|=
name|throttler2
operator|.
name|getNextSleepInterval
argument_list|(
literal|5
argument_list|)
expr_stmt|;
comment|// 2. when the second push(5) arrives and throttling(5) is called, the
comment|//    current cyclePushSize is 1000 bytes, this should make throttler1
comment|//    sleep 1000/100 = 10 cycles = 1s and make throttler2 sleep 1000/10
comment|//    = 100 cycles = 10s before the second push occurs -- amortize case
comment|//    after amortizing, both cycleStartTick and cyclePushSize are reset
comment|//
comment|// Note: in a slow machine, the sleep interval might be less than ideal ticks.
comment|// If it is 75% of expected value, its is still acceptable.
if|if
condition|(
name|ticks1
operator|!=
literal|1000
operator|&&
name|ticks1
operator|!=
literal|999
condition|)
block|{
name|assertTrue
argument_list|(
name|ticks1
operator|>=
literal|750
operator|&&
name|ticks1
operator|<=
literal|1000
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|ticks2
operator|!=
literal|10000
operator|&&
name|ticks2
operator|!=
literal|9999
condition|)
block|{
name|assertTrue
argument_list|(
name|ticks2
operator|>=
literal|7500
operator|&&
name|ticks2
operator|<=
literal|10000
argument_list|)
expr_stmt|;
block|}
name|throttler1
operator|.
name|resetStartTick
argument_list|()
expr_stmt|;
name|throttler2
operator|.
name|resetStartTick
argument_list|()
expr_stmt|;
name|throttler1
operator|.
name|addPushSize
argument_list|(
literal|5
argument_list|)
expr_stmt|;
name|throttler2
operator|.
name|addPushSize
argument_list|(
literal|5
argument_list|)
expr_stmt|;
name|ticks1
operator|=
name|throttler1
operator|.
name|getNextSleepInterval
argument_list|(
literal|45
argument_list|)
expr_stmt|;
name|ticks2
operator|=
name|throttler2
operator|.
name|getNextSleepInterval
argument_list|(
literal|45
argument_list|)
expr_stmt|;
comment|// 3. when the third push(45) arrives and throttling(45) is called, the
comment|//    current cyclePushSize is 5 bytes, 50-byte makes throttler1 no
comment|//    sleep, but can make throttler2 delay to next cycle
comment|// note: in real case, sleep time should cover time elapses during push
comment|//       operation
name|assertTrue
argument_list|(
name|ticks1
operator|==
literal|0
argument_list|)
expr_stmt|;
if|if
condition|(
name|ticks2
operator|!=
literal|100
operator|&&
name|ticks2
operator|!=
literal|99
condition|)
block|{
name|assertTrue
argument_list|(
name|ticks1
operator|>=
literal|75
operator|&&
name|ticks1
operator|<=
literal|100
argument_list|)
expr_stmt|;
block|}
name|throttler2
operator|.
name|resetStartTick
argument_list|()
expr_stmt|;
name|throttler1
operator|.
name|addPushSize
argument_list|(
literal|45
argument_list|)
expr_stmt|;
name|throttler2
operator|.
name|addPushSize
argument_list|(
literal|45
argument_list|)
expr_stmt|;
name|ticks1
operator|=
name|throttler1
operator|.
name|getNextSleepInterval
argument_list|(
literal|60
argument_list|)
expr_stmt|;
name|ticks2
operator|=
name|throttler2
operator|.
name|getNextSleepInterval
argument_list|(
literal|60
argument_list|)
expr_stmt|;
comment|// 4. when the fourth push(60) arrives and throttling(60) is called, throttler1
comment|//    delay to next cycle since 45+60 == 105; and throttler2 should firstly sleep
comment|//    ceiling(45/10)= 5 cycles = 500ms to amortize previous push
comment|//
comment|// Note: in real case, sleep time should cover time elapses during push operation
if|if
condition|(
name|ticks1
operator|!=
literal|100
operator|&&
name|ticks1
operator|!=
literal|99
condition|)
block|{
name|assertTrue
argument_list|(
name|ticks1
operator|>=
literal|75
operator|&&
name|ticks1
operator|<=
literal|100
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|ticks2
operator|!=
literal|500
operator|&&
name|ticks2
operator|!=
literal|499
condition|)
block|{
name|assertTrue
argument_list|(
name|ticks1
operator|>=
literal|375
operator|&&
name|ticks1
operator|<=
literal|500
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

