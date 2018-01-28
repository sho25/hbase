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
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|fail
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
name|ClassRule
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Rule
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

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|rules
operator|.
name|TestName
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
name|shaded
operator|.
name|protobuf
operator|.
name|ProtobufUtil
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
name|shaded
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|QuotaProtos
operator|.
name|Quotas
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
name|shaded
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|QuotaProtos
operator|.
name|Throttle
import|;
end_import

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
name|TestQuotaState
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
name|TestQuotaState
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|TableName
name|UNKNOWN_TABLE_NAME
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"unknownTable"
argument_list|)
decl_stmt|;
annotation|@
name|Rule
specifier|public
name|TestName
name|name
init|=
operator|new
name|TestName
argument_list|()
decl_stmt|;
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|60000
argument_list|)
specifier|public
name|void
name|testQuotaStateBypass
parameter_list|()
block|{
name|QuotaState
name|quotaInfo
init|=
operator|new
name|QuotaState
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
name|quotaInfo
operator|.
name|isBypass
argument_list|()
argument_list|)
expr_stmt|;
name|assertNoopLimiter
argument_list|(
name|quotaInfo
operator|.
name|getGlobalLimiter
argument_list|()
argument_list|)
expr_stmt|;
name|UserQuotaState
name|userQuotaState
init|=
operator|new
name|UserQuotaState
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
name|userQuotaState
operator|.
name|isBypass
argument_list|()
argument_list|)
expr_stmt|;
name|assertNoopLimiter
argument_list|(
name|userQuotaState
operator|.
name|getTableLimiter
argument_list|(
name|UNKNOWN_TABLE_NAME
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|60000
argument_list|)
specifier|public
name|void
name|testSimpleQuotaStateOperation
parameter_list|()
block|{
specifier|final
name|TableName
name|tableName
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
name|name
operator|.
name|getMethodName
argument_list|()
argument_list|)
decl_stmt|;
specifier|final
name|int
name|NUM_GLOBAL_THROTTLE
init|=
literal|3
decl_stmt|;
specifier|final
name|int
name|NUM_TABLE_THROTTLE
init|=
literal|2
decl_stmt|;
name|UserQuotaState
name|quotaInfo
init|=
operator|new
name|UserQuotaState
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
name|quotaInfo
operator|.
name|isBypass
argument_list|()
argument_list|)
expr_stmt|;
comment|// Set global quota
name|quotaInfo
operator|.
name|setQuotas
argument_list|(
name|buildReqNumThrottle
argument_list|(
name|NUM_GLOBAL_THROTTLE
argument_list|)
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|quotaInfo
operator|.
name|isBypass
argument_list|()
argument_list|)
expr_stmt|;
comment|// Set table quota
name|quotaInfo
operator|.
name|setQuotas
argument_list|(
name|tableName
argument_list|,
name|buildReqNumThrottle
argument_list|(
name|NUM_TABLE_THROTTLE
argument_list|)
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|quotaInfo
operator|.
name|isBypass
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|quotaInfo
operator|.
name|getGlobalLimiter
argument_list|()
operator|==
name|quotaInfo
operator|.
name|getTableLimiter
argument_list|(
name|UNKNOWN_TABLE_NAME
argument_list|)
argument_list|)
expr_stmt|;
name|assertThrottleException
argument_list|(
name|quotaInfo
operator|.
name|getTableLimiter
argument_list|(
name|UNKNOWN_TABLE_NAME
argument_list|)
argument_list|,
name|NUM_GLOBAL_THROTTLE
argument_list|)
expr_stmt|;
name|assertThrottleException
argument_list|(
name|quotaInfo
operator|.
name|getTableLimiter
argument_list|(
name|tableName
argument_list|)
argument_list|,
name|NUM_TABLE_THROTTLE
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|60000
argument_list|)
specifier|public
name|void
name|testQuotaStateUpdateBypassThrottle
parameter_list|()
block|{
specifier|final
name|long
name|LAST_UPDATE
init|=
literal|10
decl_stmt|;
name|UserQuotaState
name|quotaInfo
init|=
operator|new
name|UserQuotaState
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|quotaInfo
operator|.
name|getLastUpdate
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|quotaInfo
operator|.
name|isBypass
argument_list|()
argument_list|)
expr_stmt|;
name|UserQuotaState
name|otherQuotaState
init|=
operator|new
name|UserQuotaState
argument_list|(
name|LAST_UPDATE
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|LAST_UPDATE
argument_list|,
name|otherQuotaState
operator|.
name|getLastUpdate
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|otherQuotaState
operator|.
name|isBypass
argument_list|()
argument_list|)
expr_stmt|;
name|quotaInfo
operator|.
name|update
argument_list|(
name|otherQuotaState
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|LAST_UPDATE
argument_list|,
name|quotaInfo
operator|.
name|getLastUpdate
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|quotaInfo
operator|.
name|isBypass
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|quotaInfo
operator|.
name|getGlobalLimiter
argument_list|()
operator|==
name|quotaInfo
operator|.
name|getTableLimiter
argument_list|(
name|UNKNOWN_TABLE_NAME
argument_list|)
argument_list|)
expr_stmt|;
name|assertNoopLimiter
argument_list|(
name|quotaInfo
operator|.
name|getTableLimiter
argument_list|(
name|UNKNOWN_TABLE_NAME
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|60000
argument_list|)
specifier|public
name|void
name|testQuotaStateUpdateGlobalThrottle
parameter_list|()
block|{
specifier|final
name|int
name|NUM_GLOBAL_THROTTLE_1
init|=
literal|3
decl_stmt|;
specifier|final
name|int
name|NUM_GLOBAL_THROTTLE_2
init|=
literal|11
decl_stmt|;
specifier|final
name|long
name|LAST_UPDATE_1
init|=
literal|10
decl_stmt|;
specifier|final
name|long
name|LAST_UPDATE_2
init|=
literal|20
decl_stmt|;
specifier|final
name|long
name|LAST_UPDATE_3
init|=
literal|30
decl_stmt|;
name|QuotaState
name|quotaInfo
init|=
operator|new
name|QuotaState
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|quotaInfo
operator|.
name|getLastUpdate
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|quotaInfo
operator|.
name|isBypass
argument_list|()
argument_list|)
expr_stmt|;
comment|// Add global throttle
name|QuotaState
name|otherQuotaState
init|=
operator|new
name|QuotaState
argument_list|(
name|LAST_UPDATE_1
argument_list|)
decl_stmt|;
name|otherQuotaState
operator|.
name|setQuotas
argument_list|(
name|buildReqNumThrottle
argument_list|(
name|NUM_GLOBAL_THROTTLE_1
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|LAST_UPDATE_1
argument_list|,
name|otherQuotaState
operator|.
name|getLastUpdate
argument_list|()
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|otherQuotaState
operator|.
name|isBypass
argument_list|()
argument_list|)
expr_stmt|;
name|quotaInfo
operator|.
name|update
argument_list|(
name|otherQuotaState
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|LAST_UPDATE_1
argument_list|,
name|quotaInfo
operator|.
name|getLastUpdate
argument_list|()
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|quotaInfo
operator|.
name|isBypass
argument_list|()
argument_list|)
expr_stmt|;
name|assertThrottleException
argument_list|(
name|quotaInfo
operator|.
name|getGlobalLimiter
argument_list|()
argument_list|,
name|NUM_GLOBAL_THROTTLE_1
argument_list|)
expr_stmt|;
comment|// Update global Throttle
name|otherQuotaState
operator|=
operator|new
name|QuotaState
argument_list|(
name|LAST_UPDATE_2
argument_list|)
expr_stmt|;
name|otherQuotaState
operator|.
name|setQuotas
argument_list|(
name|buildReqNumThrottle
argument_list|(
name|NUM_GLOBAL_THROTTLE_2
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|LAST_UPDATE_2
argument_list|,
name|otherQuotaState
operator|.
name|getLastUpdate
argument_list|()
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|otherQuotaState
operator|.
name|isBypass
argument_list|()
argument_list|)
expr_stmt|;
name|quotaInfo
operator|.
name|update
argument_list|(
name|otherQuotaState
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|LAST_UPDATE_2
argument_list|,
name|quotaInfo
operator|.
name|getLastUpdate
argument_list|()
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|quotaInfo
operator|.
name|isBypass
argument_list|()
argument_list|)
expr_stmt|;
name|assertThrottleException
argument_list|(
name|quotaInfo
operator|.
name|getGlobalLimiter
argument_list|()
argument_list|,
name|NUM_GLOBAL_THROTTLE_2
operator|-
name|NUM_GLOBAL_THROTTLE_1
argument_list|)
expr_stmt|;
comment|// Remove global throttle
name|otherQuotaState
operator|=
operator|new
name|QuotaState
argument_list|(
name|LAST_UPDATE_3
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|LAST_UPDATE_3
argument_list|,
name|otherQuotaState
operator|.
name|getLastUpdate
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|otherQuotaState
operator|.
name|isBypass
argument_list|()
argument_list|)
expr_stmt|;
name|quotaInfo
operator|.
name|update
argument_list|(
name|otherQuotaState
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|LAST_UPDATE_3
argument_list|,
name|quotaInfo
operator|.
name|getLastUpdate
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|quotaInfo
operator|.
name|isBypass
argument_list|()
argument_list|)
expr_stmt|;
name|assertNoopLimiter
argument_list|(
name|quotaInfo
operator|.
name|getGlobalLimiter
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|60000
argument_list|)
specifier|public
name|void
name|testQuotaStateUpdateTableThrottle
parameter_list|()
block|{
specifier|final
name|TableName
name|tableNameA
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
name|name
operator|.
name|getMethodName
argument_list|()
operator|+
literal|"A"
argument_list|)
decl_stmt|;
specifier|final
name|TableName
name|tableNameB
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
name|name
operator|.
name|getMethodName
argument_list|()
operator|+
literal|"B"
argument_list|)
decl_stmt|;
specifier|final
name|TableName
name|tableNameC
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
name|name
operator|.
name|getMethodName
argument_list|()
operator|+
literal|"C"
argument_list|)
decl_stmt|;
specifier|final
name|int
name|TABLE_A_THROTTLE_1
init|=
literal|3
decl_stmt|;
specifier|final
name|int
name|TABLE_A_THROTTLE_2
init|=
literal|11
decl_stmt|;
specifier|final
name|int
name|TABLE_B_THROTTLE
init|=
literal|4
decl_stmt|;
specifier|final
name|int
name|TABLE_C_THROTTLE
init|=
literal|5
decl_stmt|;
specifier|final
name|long
name|LAST_UPDATE_1
init|=
literal|10
decl_stmt|;
specifier|final
name|long
name|LAST_UPDATE_2
init|=
literal|20
decl_stmt|;
specifier|final
name|long
name|LAST_UPDATE_3
init|=
literal|30
decl_stmt|;
name|UserQuotaState
name|quotaInfo
init|=
operator|new
name|UserQuotaState
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|quotaInfo
operator|.
name|getLastUpdate
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|quotaInfo
operator|.
name|isBypass
argument_list|()
argument_list|)
expr_stmt|;
comment|// Add A B table limiters
name|UserQuotaState
name|otherQuotaState
init|=
operator|new
name|UserQuotaState
argument_list|(
name|LAST_UPDATE_1
argument_list|)
decl_stmt|;
name|otherQuotaState
operator|.
name|setQuotas
argument_list|(
name|tableNameA
argument_list|,
name|buildReqNumThrottle
argument_list|(
name|TABLE_A_THROTTLE_1
argument_list|)
argument_list|)
expr_stmt|;
name|otherQuotaState
operator|.
name|setQuotas
argument_list|(
name|tableNameB
argument_list|,
name|buildReqNumThrottle
argument_list|(
name|TABLE_B_THROTTLE
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|LAST_UPDATE_1
argument_list|,
name|otherQuotaState
operator|.
name|getLastUpdate
argument_list|()
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|otherQuotaState
operator|.
name|isBypass
argument_list|()
argument_list|)
expr_stmt|;
name|quotaInfo
operator|.
name|update
argument_list|(
name|otherQuotaState
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|LAST_UPDATE_1
argument_list|,
name|quotaInfo
operator|.
name|getLastUpdate
argument_list|()
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|quotaInfo
operator|.
name|isBypass
argument_list|()
argument_list|)
expr_stmt|;
name|assertThrottleException
argument_list|(
name|quotaInfo
operator|.
name|getTableLimiter
argument_list|(
name|tableNameA
argument_list|)
argument_list|,
name|TABLE_A_THROTTLE_1
argument_list|)
expr_stmt|;
name|assertThrottleException
argument_list|(
name|quotaInfo
operator|.
name|getTableLimiter
argument_list|(
name|tableNameB
argument_list|)
argument_list|,
name|TABLE_B_THROTTLE
argument_list|)
expr_stmt|;
name|assertNoopLimiter
argument_list|(
name|quotaInfo
operator|.
name|getTableLimiter
argument_list|(
name|tableNameC
argument_list|)
argument_list|)
expr_stmt|;
comment|// Add C, Remove B, Update A table limiters
name|otherQuotaState
operator|=
operator|new
name|UserQuotaState
argument_list|(
name|LAST_UPDATE_2
argument_list|)
expr_stmt|;
name|otherQuotaState
operator|.
name|setQuotas
argument_list|(
name|tableNameA
argument_list|,
name|buildReqNumThrottle
argument_list|(
name|TABLE_A_THROTTLE_2
argument_list|)
argument_list|)
expr_stmt|;
name|otherQuotaState
operator|.
name|setQuotas
argument_list|(
name|tableNameC
argument_list|,
name|buildReqNumThrottle
argument_list|(
name|TABLE_C_THROTTLE
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|LAST_UPDATE_2
argument_list|,
name|otherQuotaState
operator|.
name|getLastUpdate
argument_list|()
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|otherQuotaState
operator|.
name|isBypass
argument_list|()
argument_list|)
expr_stmt|;
name|quotaInfo
operator|.
name|update
argument_list|(
name|otherQuotaState
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|LAST_UPDATE_2
argument_list|,
name|quotaInfo
operator|.
name|getLastUpdate
argument_list|()
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|quotaInfo
operator|.
name|isBypass
argument_list|()
argument_list|)
expr_stmt|;
name|assertThrottleException
argument_list|(
name|quotaInfo
operator|.
name|getTableLimiter
argument_list|(
name|tableNameA
argument_list|)
argument_list|,
name|TABLE_A_THROTTLE_2
operator|-
name|TABLE_A_THROTTLE_1
argument_list|)
expr_stmt|;
name|assertThrottleException
argument_list|(
name|quotaInfo
operator|.
name|getTableLimiter
argument_list|(
name|tableNameC
argument_list|)
argument_list|,
name|TABLE_C_THROTTLE
argument_list|)
expr_stmt|;
name|assertNoopLimiter
argument_list|(
name|quotaInfo
operator|.
name|getTableLimiter
argument_list|(
name|tableNameB
argument_list|)
argument_list|)
expr_stmt|;
comment|// Remove table limiters
name|otherQuotaState
operator|=
operator|new
name|UserQuotaState
argument_list|(
name|LAST_UPDATE_3
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|LAST_UPDATE_3
argument_list|,
name|otherQuotaState
operator|.
name|getLastUpdate
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|otherQuotaState
operator|.
name|isBypass
argument_list|()
argument_list|)
expr_stmt|;
name|quotaInfo
operator|.
name|update
argument_list|(
name|otherQuotaState
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|LAST_UPDATE_3
argument_list|,
name|quotaInfo
operator|.
name|getLastUpdate
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|quotaInfo
operator|.
name|isBypass
argument_list|()
argument_list|)
expr_stmt|;
name|assertNoopLimiter
argument_list|(
name|quotaInfo
operator|.
name|getTableLimiter
argument_list|(
name|UNKNOWN_TABLE_NAME
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|private
name|Quotas
name|buildReqNumThrottle
parameter_list|(
specifier|final
name|long
name|limit
parameter_list|)
block|{
return|return
name|Quotas
operator|.
name|newBuilder
argument_list|()
operator|.
name|setThrottle
argument_list|(
name|Throttle
operator|.
name|newBuilder
argument_list|()
operator|.
name|setReqNum
argument_list|(
name|ProtobufUtil
operator|.
name|toTimedQuota
argument_list|(
name|limit
argument_list|,
name|TimeUnit
operator|.
name|MINUTES
argument_list|,
name|QuotaScope
operator|.
name|MACHINE
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
operator|.
name|build
argument_list|()
return|;
block|}
specifier|private
name|void
name|assertThrottleException
parameter_list|(
specifier|final
name|QuotaLimiter
name|limiter
parameter_list|,
specifier|final
name|int
name|availReqs
parameter_list|)
block|{
name|assertNoThrottleException
argument_list|(
name|limiter
argument_list|,
name|availReqs
argument_list|)
expr_stmt|;
try|try
block|{
name|limiter
operator|.
name|checkQuota
argument_list|(
literal|1
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"Should have thrown ThrottlingException"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ThrottlingException
name|e
parameter_list|)
block|{
comment|// expected
block|}
block|}
specifier|private
name|void
name|assertNoThrottleException
parameter_list|(
specifier|final
name|QuotaLimiter
name|limiter
parameter_list|,
specifier|final
name|int
name|availReqs
parameter_list|)
block|{
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|availReqs
condition|;
operator|++
name|i
control|)
block|{
try|try
block|{
name|limiter
operator|.
name|checkQuota
argument_list|(
literal|1
argument_list|,
literal|1
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ThrottlingException
name|e
parameter_list|)
block|{
name|fail
argument_list|(
literal|"Unexpected ThrottlingException after "
operator|+
name|i
operator|+
literal|" requests. limit="
operator|+
name|availReqs
argument_list|)
expr_stmt|;
block|}
name|limiter
operator|.
name|grabQuota
argument_list|(
literal|1
argument_list|,
literal|1
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|assertNoopLimiter
parameter_list|(
specifier|final
name|QuotaLimiter
name|limiter
parameter_list|)
block|{
name|assertTrue
argument_list|(
name|limiter
operator|==
name|NoopQuotaLimiter
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
name|assertNoThrottleException
argument_list|(
name|limiter
argument_list|,
literal|100
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

