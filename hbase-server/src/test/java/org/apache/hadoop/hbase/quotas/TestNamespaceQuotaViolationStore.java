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
name|HRegionInfo
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
name|NamespaceDescriptor
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
name|quotas
operator|.
name|QuotaViolationStore
operator|.
name|ViolationState
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

begin_comment
comment|/**  * Test class for {@link NamespaceQuotaViolationStore}.  */
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
name|TestNamespaceQuotaViolationStore
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
name|HRegionInfo
argument_list|,
name|Long
argument_list|>
name|regionReports
decl_stmt|;
specifier|private
name|NamespaceQuotaViolationStore
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
name|NamespaceQuotaViolationStore
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
name|testGetSpaceQuota
parameter_list|()
throws|throws
name|Exception
block|{
name|NamespaceQuotaViolationStore
name|mockStore
init|=
name|mock
argument_list|(
name|NamespaceQuotaViolationStore
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
name|String
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
name|getQuotaForNamespace
argument_list|(
name|any
argument_list|(
name|String
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
literal|"ns"
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
literal|"ns"
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
block|{
specifier|final
name|String
name|NS
init|=
literal|"ns"
decl_stmt|;
name|TableName
name|tn1
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
name|NS
argument_list|,
literal|"tn1"
argument_list|)
decl_stmt|;
name|TableName
name|tn2
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
name|NS
argument_list|,
literal|"tn2"
argument_list|)
decl_stmt|;
name|TableName
name|tn3
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"tn3"
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
name|ONE_MEGABYTE
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
operator|new
name|HRegionInfo
argument_list|(
name|tn3
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|i
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|i
operator|+
literal|1
argument_list|)
argument_list|)
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
operator|new
name|HRegionInfo
argument_list|(
name|tn1
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|0
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|1
argument_list|)
argument_list|)
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
operator|new
name|HRegionInfo
argument_list|(
name|tn1
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|1
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|2
argument_list|)
argument_list|)
argument_list|,
literal|1024L
operator|*
literal|256L
argument_list|)
expr_stmt|;
comment|// Below the quota
name|assertEquals
argument_list|(
name|ViolationState
operator|.
name|IN_OBSERVANCE
argument_list|,
name|store
operator|.
name|getTargetState
argument_list|(
name|NS
argument_list|,
name|quota
argument_list|)
argument_list|)
expr_stmt|;
name|regionReports
operator|.
name|put
argument_list|(
operator|new
name|HRegionInfo
argument_list|(
name|tn2
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|2
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|3
argument_list|)
argument_list|)
argument_list|,
literal|1024L
operator|*
literal|256L
argument_list|)
expr_stmt|;
comment|// Equal to the quota is still in observance
name|assertEquals
argument_list|(
name|ViolationState
operator|.
name|IN_OBSERVANCE
argument_list|,
name|store
operator|.
name|getTargetState
argument_list|(
name|NS
argument_list|,
name|quota
argument_list|)
argument_list|)
expr_stmt|;
name|regionReports
operator|.
name|put
argument_list|(
operator|new
name|HRegionInfo
argument_list|(
name|tn2
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|3
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|4
argument_list|)
argument_list|)
argument_list|,
literal|1024L
argument_list|)
expr_stmt|;
comment|// Exceeds the quota, should be in violation
name|assertEquals
argument_list|(
name|ViolationState
operator|.
name|IN_VIOLATION
argument_list|,
name|store
operator|.
name|getTargetState
argument_list|(
name|NS
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
name|testFilterRegionsByNamespace
parameter_list|()
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
literal|"sn"
argument_list|,
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
name|TableName
name|tn4
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"ns"
argument_list|,
literal|"bar"
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
literal|"asdf"
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
operator|new
name|HRegionInfo
argument_list|(
name|tn1
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|i
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|i
operator|+
literal|1
argument_list|)
argument_list|)
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
operator|new
name|HRegionInfo
argument_list|(
name|tn2
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|i
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|i
operator|+
literal|1
argument_list|)
argument_list|)
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
operator|new
name|HRegionInfo
argument_list|(
name|tn3
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|i
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|i
operator|+
literal|1
argument_list|)
argument_list|)
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
literal|8
condition|;
name|i
operator|++
control|)
block|{
name|regionReports
operator|.
name|put
argument_list|(
operator|new
name|HRegionInfo
argument_list|(
name|tn4
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|i
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|i
operator|+
literal|1
argument_list|)
argument_list|)
argument_list|,
literal|0L
argument_list|)
expr_stmt|;
block|}
name|assertEquals
argument_list|(
literal|26
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
name|NamespaceDescriptor
operator|.
name|DEFAULT_NAMESPACE_NAME_STR
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
literal|"sn"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|18
argument_list|,
name|size
argument_list|(
name|store
operator|.
name|filterBySubject
argument_list|(
literal|"ns"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

