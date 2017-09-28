begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one or more  * contributor license agreements.  See the NOTICE file distributed with  * this work for additional information regarding copyright ownership.  * The ASF licenses this file to you under the Apache License, Version 2.0  * (the "License"); you may not use this file except in compliance with  * the License.  You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|shaded
operator|.
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Iterables
operator|.
name|size
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
name|assertNull
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|mockito
operator|.
name|Matchers
operator|.
name|any
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|mockito
operator|.
name|Mockito
operator|.
name|mock
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|mockito
operator|.
name|Mockito
operator|.
name|when
import|;
end_import

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
name|Collections
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
name|atomic
operator|.
name|AtomicReference
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
name|RegionInfoBuilder
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
name|Result
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
name|ResultScanner
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
name|Scan
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
name|Table
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
name|quotas
operator|.
name|SpaceQuotaSnapshot
operator|.
name|SpaceQuotaStatus
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
name|Bytes
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Before
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
name|mockito
operator|.
name|invocation
operator|.
name|InvocationOnMock
import|;
end_import

begin_import
import|import
name|org
operator|.
name|mockito
operator|.
name|stubbing
operator|.
name|Answer
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
name|SpaceQuota
import|;
end_import

begin_comment
comment|/**  * Test class for {@link TableQuotaSnapshotStore}.  */
end_comment

begin_class
annotation|@
name|Category
argument_list|(
name|SmallTests
operator|.
name|class
argument_list|)
specifier|public
class|class
name|TestTableQuotaViolationStore
block|{
specifier|private
specifier|static
specifier|final
name|long
name|ONE_MEGABYTE
init|=
literal|1024L
operator|*
literal|1024L
decl_stmt|;
specifier|private
name|Connection
name|conn
decl_stmt|;
specifier|private
name|QuotaObserverChore
name|chore
decl_stmt|;
specifier|private
name|Map
argument_list|<
name|RegionInfo
argument_list|,
name|Long
argument_list|>
name|regionReports
decl_stmt|;
specifier|private
name|TableQuotaSnapshotStore
name|store
decl_stmt|;
annotation|@
name|Before
specifier|public
name|void
name|setup
parameter_list|()
block|{
name|conn
operator|=
name|mock
argument_list|(
name|Connection
operator|.
name|class
argument_list|)
expr_stmt|;
name|chore
operator|=
name|mock
argument_list|(
name|QuotaObserverChore
operator|.
name|class
argument_list|)
expr_stmt|;
name|regionReports
operator|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
expr_stmt|;
name|store
operator|=
operator|new
name|TableQuotaSnapshotStore
argument_list|(
name|conn
argument_list|,
name|chore
argument_list|,
name|regionReports
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testFilterRegionsByTable
parameter_list|()
throws|throws
name|Exception
block|{
name|TableName
name|tn1
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"foo"
argument_list|)
decl_stmt|;
name|TableName
name|tn2
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"bar"
argument_list|)
decl_stmt|;
name|TableName
name|tn3
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"ns"
argument_list|,
literal|"foo"
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|size
argument_list|(
name|store
operator|.
name|filterBySubject
argument_list|(
name|tn1
argument_list|)
argument_list|)
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
literal|5
condition|;
name|i
operator|++
control|)
block|{
name|regionReports
operator|.
name|put
argument_list|(
name|RegionInfoBuilder
operator|.
name|newBuilder
argument_list|(
name|tn1
argument_list|)
operator|.
name|setStartKey
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|i
argument_list|)
argument_list|)
operator|.
name|setEndKey
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|i
operator|+
literal|1
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
argument_list|,
literal|0L
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
literal|3
condition|;
name|i
operator|++
control|)
block|{
name|regionReports
operator|.
name|put
argument_list|(
name|RegionInfoBuilder
operator|.
name|newBuilder
argument_list|(
name|tn2
argument_list|)
operator|.
name|setStartKey
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|i
argument_list|)
argument_list|)
operator|.
name|setEndKey
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|i
operator|+
literal|1
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
argument_list|,
literal|0L
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
literal|10
condition|;
name|i
operator|++
control|)
block|{
name|regionReports
operator|.
name|put
argument_list|(
name|RegionInfoBuilder
operator|.
name|newBuilder
argument_list|(
name|tn3
argument_list|)
operator|.
name|setStartKey
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|i
argument_list|)
argument_list|)
operator|.
name|setEndKey
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|i
operator|+
literal|1
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
argument_list|,
literal|0L
argument_list|)
expr_stmt|;
block|}
name|assertEquals
argument_list|(
literal|18
argument_list|,
name|regionReports
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|5
argument_list|,
name|size
argument_list|(
name|store
operator|.
name|filterBySubject
argument_list|(
name|tn1
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|3
argument_list|,
name|size
argument_list|(
name|store
operator|.
name|filterBySubject
argument_list|(
name|tn2
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|10
argument_list|,
name|size
argument_list|(
name|store
operator|.
name|filterBySubject
argument_list|(
name|tn3
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testTargetViolationState
parameter_list|()
throws|throws
name|IOException
block|{
name|mockNoSnapshotSizes
argument_list|()
expr_stmt|;
name|TableName
name|tn1
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"violation1"
argument_list|)
decl_stmt|;
name|TableName
name|tn2
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"observance1"
argument_list|)
decl_stmt|;
name|TableName
name|tn3
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"observance2"
argument_list|)
decl_stmt|;
name|SpaceQuota
name|quota
init|=
name|SpaceQuota
operator|.
name|newBuilder
argument_list|()
operator|.
name|setSoftLimit
argument_list|(
literal|1024L
operator|*
literal|1024L
argument_list|)
operator|.
name|setViolationPolicy
argument_list|(
name|ProtobufUtil
operator|.
name|toProtoViolationPolicy
argument_list|(
name|SpaceViolationPolicy
operator|.
name|DISABLE
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
comment|// Create some junk data to filter. Makes sure it's so large that it would
comment|// immediately violate the quota.
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
name|regionReports
operator|.
name|put
argument_list|(
name|RegionInfoBuilder
operator|.
name|newBuilder
argument_list|(
name|tn2
argument_list|)
operator|.
name|setStartKey
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|i
argument_list|)
argument_list|)
operator|.
name|setEndKey
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|i
operator|+
literal|1
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
argument_list|,
literal|5L
operator|*
name|ONE_MEGABYTE
argument_list|)
expr_stmt|;
name|regionReports
operator|.
name|put
argument_list|(
name|RegionInfoBuilder
operator|.
name|newBuilder
argument_list|(
name|tn3
argument_list|)
operator|.
name|setStartKey
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|i
argument_list|)
argument_list|)
operator|.
name|setEndKey
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|i
operator|+
literal|1
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
argument_list|,
literal|5L
operator|*
name|ONE_MEGABYTE
argument_list|)
expr_stmt|;
block|}
name|regionReports
operator|.
name|put
argument_list|(
name|RegionInfoBuilder
operator|.
name|newBuilder
argument_list|(
name|tn1
argument_list|)
operator|.
name|setStartKey
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|0
argument_list|)
argument_list|)
operator|.
name|setEndKey
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|1
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
argument_list|,
literal|1024L
operator|*
literal|512L
argument_list|)
expr_stmt|;
name|regionReports
operator|.
name|put
argument_list|(
name|RegionInfoBuilder
operator|.
name|newBuilder
argument_list|(
name|tn1
argument_list|)
operator|.
name|setStartKey
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|1
argument_list|)
argument_list|)
operator|.
name|setEndKey
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|2
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
argument_list|,
literal|1024L
operator|*
literal|256L
argument_list|)
expr_stmt|;
name|SpaceQuotaSnapshot
name|tn1Snapshot
init|=
operator|new
name|SpaceQuotaSnapshot
argument_list|(
name|SpaceQuotaStatus
operator|.
name|notInViolation
argument_list|()
argument_list|,
literal|1024L
operator|*
literal|768L
argument_list|,
literal|1024L
operator|*
literal|1024L
argument_list|)
decl_stmt|;
comment|// Below the quota
name|assertEquals
argument_list|(
name|tn1Snapshot
argument_list|,
name|store
operator|.
name|getTargetState
argument_list|(
name|tn1
argument_list|,
name|quota
argument_list|)
argument_list|)
expr_stmt|;
name|regionReports
operator|.
name|put
argument_list|(
name|RegionInfoBuilder
operator|.
name|newBuilder
argument_list|(
name|tn1
argument_list|)
operator|.
name|setStartKey
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|2
argument_list|)
argument_list|)
operator|.
name|setEndKey
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|3
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
argument_list|,
literal|1024L
operator|*
literal|256L
argument_list|)
expr_stmt|;
name|tn1Snapshot
operator|=
operator|new
name|SpaceQuotaSnapshot
argument_list|(
name|SpaceQuotaStatus
operator|.
name|notInViolation
argument_list|()
argument_list|,
literal|1024L
operator|*
literal|1024L
argument_list|,
literal|1024L
operator|*
literal|1024L
argument_list|)
expr_stmt|;
comment|// Equal to the quota is still in observance
name|assertEquals
argument_list|(
name|tn1Snapshot
argument_list|,
name|store
operator|.
name|getTargetState
argument_list|(
name|tn1
argument_list|,
name|quota
argument_list|)
argument_list|)
expr_stmt|;
name|regionReports
operator|.
name|put
argument_list|(
name|RegionInfoBuilder
operator|.
name|newBuilder
argument_list|(
name|tn1
argument_list|)
operator|.
name|setStartKey
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|3
argument_list|)
argument_list|)
operator|.
name|setEndKey
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|4
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
argument_list|,
literal|1024L
argument_list|)
expr_stmt|;
name|tn1Snapshot
operator|=
operator|new
name|SpaceQuotaSnapshot
argument_list|(
operator|new
name|SpaceQuotaStatus
argument_list|(
name|SpaceViolationPolicy
operator|.
name|DISABLE
argument_list|)
argument_list|,
literal|1024L
operator|*
literal|1024L
operator|+
literal|1024L
argument_list|,
literal|1024L
operator|*
literal|1024L
argument_list|)
expr_stmt|;
comment|// Exceeds the quota, should be in violation
name|assertEquals
argument_list|(
name|tn1Snapshot
argument_list|,
name|store
operator|.
name|getTargetState
argument_list|(
name|tn1
argument_list|,
name|quota
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testGetSpaceQuota
parameter_list|()
throws|throws
name|Exception
block|{
name|TableQuotaSnapshotStore
name|mockStore
init|=
name|mock
argument_list|(
name|TableQuotaSnapshotStore
operator|.
name|class
argument_list|)
decl_stmt|;
name|when
argument_list|(
name|mockStore
operator|.
name|getSpaceQuota
argument_list|(
name|any
argument_list|(
name|TableName
operator|.
name|class
argument_list|)
argument_list|)
argument_list|)
operator|.
name|thenCallRealMethod
argument_list|()
expr_stmt|;
name|Quotas
name|quotaWithSpace
init|=
name|Quotas
operator|.
name|newBuilder
argument_list|()
operator|.
name|setSpace
argument_list|(
name|SpaceQuota
operator|.
name|newBuilder
argument_list|()
operator|.
name|setSoftLimit
argument_list|(
literal|1024L
argument_list|)
operator|.
name|setViolationPolicy
argument_list|(
name|QuotaProtos
operator|.
name|SpaceViolationPolicy
operator|.
name|DISABLE
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|Quotas
name|quotaWithoutSpace
init|=
name|Quotas
operator|.
name|newBuilder
argument_list|()
operator|.
name|build
argument_list|()
decl_stmt|;
name|AtomicReference
argument_list|<
name|Quotas
argument_list|>
name|quotaRef
init|=
operator|new
name|AtomicReference
argument_list|<>
argument_list|()
decl_stmt|;
name|when
argument_list|(
name|mockStore
operator|.
name|getQuotaForTable
argument_list|(
name|any
argument_list|(
name|TableName
operator|.
name|class
argument_list|)
argument_list|)
argument_list|)
operator|.
name|then
argument_list|(
operator|new
name|Answer
argument_list|<
name|Quotas
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Quotas
name|answer
parameter_list|(
name|InvocationOnMock
name|invocation
parameter_list|)
throws|throws
name|Throwable
block|{
return|return
name|quotaRef
operator|.
name|get
argument_list|()
return|;
block|}
block|}
argument_list|)
expr_stmt|;
name|quotaRef
operator|.
name|set
argument_list|(
name|quotaWithSpace
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|quotaWithSpace
operator|.
name|getSpace
argument_list|()
argument_list|,
name|mockStore
operator|.
name|getSpaceQuota
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"foo"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|quotaRef
operator|.
name|set
argument_list|(
name|quotaWithoutSpace
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|mockStore
operator|.
name|getSpaceQuota
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"foo"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|void
name|mockNoSnapshotSizes
parameter_list|()
throws|throws
name|IOException
block|{
name|Table
name|quotaTable
init|=
name|mock
argument_list|(
name|Table
operator|.
name|class
argument_list|)
decl_stmt|;
name|ResultScanner
name|scanner
init|=
name|mock
argument_list|(
name|ResultScanner
operator|.
name|class
argument_list|)
decl_stmt|;
name|when
argument_list|(
name|conn
operator|.
name|getTable
argument_list|(
name|QuotaTableUtil
operator|.
name|QUOTA_TABLE_NAME
argument_list|)
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|quotaTable
argument_list|)
expr_stmt|;
name|when
argument_list|(
name|quotaTable
operator|.
name|getScanner
argument_list|(
name|any
argument_list|(
name|Scan
operator|.
name|class
argument_list|)
argument_list|)
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|scanner
argument_list|)
expr_stmt|;
name|when
argument_list|(
name|scanner
operator|.
name|iterator
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|Collections
operator|.
expr|<
name|Result
operator|>
name|emptyList
argument_list|()
operator|.
name|iterator
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

