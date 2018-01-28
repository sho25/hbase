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
name|client
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
import|import
name|com
operator|.
name|codahale
operator|.
name|metrics
operator|.
name|RatioGauge
import|;
end_import

begin_import
import|import
name|com
operator|.
name|codahale
operator|.
name|metrics
operator|.
name|RatioGauge
operator|.
name|Ratio
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
name|concurrent
operator|.
name|ExecutorService
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
name|Executors
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
name|AtomicBoolean
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
name|ClientTests
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
name|MetricsTests
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
name|AfterClass
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|BeforeClass
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

begin_import
import|import
name|org
operator|.
name|mockito
operator|.
name|Mockito
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hbase
operator|.
name|thirdparty
operator|.
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|ByteString
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
name|ClientProtos
operator|.
name|ClientService
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
name|ClientProtos
operator|.
name|GetRequest
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
name|ClientProtos
operator|.
name|MultiRequest
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
name|ClientProtos
operator|.
name|MutateRequest
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
name|ClientProtos
operator|.
name|MutationProto
operator|.
name|MutationType
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
name|ClientProtos
operator|.
name|ScanRequest
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
name|HBaseProtos
operator|.
name|RegionSpecifier
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
name|HBaseProtos
operator|.
name|RegionSpecifier
operator|.
name|RegionSpecifierType
import|;
end_import

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|ClientTests
operator|.
name|class
block|,
name|MetricsTests
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
name|TestMetricsConnection
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
name|TestMetricsConnection
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|MetricsConnection
name|METRICS
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|ExecutorService
name|BATCH_POOL
init|=
name|Executors
operator|.
name|newFixedThreadPool
argument_list|(
literal|2
argument_list|)
decl_stmt|;
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|beforeClass
parameter_list|()
block|{
name|ConnectionImplementation
name|mocked
init|=
name|Mockito
operator|.
name|mock
argument_list|(
name|ConnectionImplementation
operator|.
name|class
argument_list|)
decl_stmt|;
name|Mockito
operator|.
name|when
argument_list|(
name|mocked
operator|.
name|toString
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
literal|"mocked-connection"
argument_list|)
expr_stmt|;
name|Mockito
operator|.
name|when
argument_list|(
name|mocked
operator|.
name|getCurrentBatchPool
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|BATCH_POOL
argument_list|)
expr_stmt|;
name|METRICS
operator|=
operator|new
name|MetricsConnection
argument_list|(
name|mocked
argument_list|)
expr_stmt|;
block|}
annotation|@
name|AfterClass
specifier|public
specifier|static
name|void
name|afterClass
parameter_list|()
block|{
name|METRICS
operator|.
name|shutdown
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testStaticMetrics
parameter_list|()
throws|throws
name|IOException
block|{
specifier|final
name|byte
index|[]
name|foo
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"foo"
argument_list|)
decl_stmt|;
specifier|final
name|RegionSpecifier
name|region
init|=
name|RegionSpecifier
operator|.
name|newBuilder
argument_list|()
operator|.
name|setValue
argument_list|(
name|ByteString
operator|.
name|EMPTY
argument_list|)
operator|.
name|setType
argument_list|(
name|RegionSpecifierType
operator|.
name|REGION_NAME
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
specifier|final
name|int
name|loop
init|=
literal|5
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|loop
condition|;
name|i
operator|++
control|)
block|{
name|METRICS
operator|.
name|updateRpc
argument_list|(
name|ClientService
operator|.
name|getDescriptor
argument_list|()
operator|.
name|findMethodByName
argument_list|(
literal|"Get"
argument_list|)
argument_list|,
name|GetRequest
operator|.
name|getDefaultInstance
argument_list|()
argument_list|,
name|MetricsConnection
operator|.
name|newCallStats
argument_list|()
argument_list|)
expr_stmt|;
name|METRICS
operator|.
name|updateRpc
argument_list|(
name|ClientService
operator|.
name|getDescriptor
argument_list|()
operator|.
name|findMethodByName
argument_list|(
literal|"Scan"
argument_list|)
argument_list|,
name|ScanRequest
operator|.
name|getDefaultInstance
argument_list|()
argument_list|,
name|MetricsConnection
operator|.
name|newCallStats
argument_list|()
argument_list|)
expr_stmt|;
name|METRICS
operator|.
name|updateRpc
argument_list|(
name|ClientService
operator|.
name|getDescriptor
argument_list|()
operator|.
name|findMethodByName
argument_list|(
literal|"Multi"
argument_list|)
argument_list|,
name|MultiRequest
operator|.
name|getDefaultInstance
argument_list|()
argument_list|,
name|MetricsConnection
operator|.
name|newCallStats
argument_list|()
argument_list|)
expr_stmt|;
name|METRICS
operator|.
name|updateRpc
argument_list|(
name|ClientService
operator|.
name|getDescriptor
argument_list|()
operator|.
name|findMethodByName
argument_list|(
literal|"Mutate"
argument_list|)
argument_list|,
name|MutateRequest
operator|.
name|newBuilder
argument_list|()
operator|.
name|setMutation
argument_list|(
name|ProtobufUtil
operator|.
name|toMutation
argument_list|(
name|MutationType
operator|.
name|APPEND
argument_list|,
operator|new
name|Append
argument_list|(
name|foo
argument_list|)
argument_list|)
argument_list|)
operator|.
name|setRegion
argument_list|(
name|region
argument_list|)
operator|.
name|build
argument_list|()
argument_list|,
name|MetricsConnection
operator|.
name|newCallStats
argument_list|()
argument_list|)
expr_stmt|;
name|METRICS
operator|.
name|updateRpc
argument_list|(
name|ClientService
operator|.
name|getDescriptor
argument_list|()
operator|.
name|findMethodByName
argument_list|(
literal|"Mutate"
argument_list|)
argument_list|,
name|MutateRequest
operator|.
name|newBuilder
argument_list|()
operator|.
name|setMutation
argument_list|(
name|ProtobufUtil
operator|.
name|toMutation
argument_list|(
name|MutationType
operator|.
name|DELETE
argument_list|,
operator|new
name|Delete
argument_list|(
name|foo
argument_list|)
argument_list|)
argument_list|)
operator|.
name|setRegion
argument_list|(
name|region
argument_list|)
operator|.
name|build
argument_list|()
argument_list|,
name|MetricsConnection
operator|.
name|newCallStats
argument_list|()
argument_list|)
expr_stmt|;
name|METRICS
operator|.
name|updateRpc
argument_list|(
name|ClientService
operator|.
name|getDescriptor
argument_list|()
operator|.
name|findMethodByName
argument_list|(
literal|"Mutate"
argument_list|)
argument_list|,
name|MutateRequest
operator|.
name|newBuilder
argument_list|()
operator|.
name|setMutation
argument_list|(
name|ProtobufUtil
operator|.
name|toMutation
argument_list|(
name|MutationType
operator|.
name|INCREMENT
argument_list|,
operator|new
name|Increment
argument_list|(
name|foo
argument_list|)
argument_list|)
argument_list|)
operator|.
name|setRegion
argument_list|(
name|region
argument_list|)
operator|.
name|build
argument_list|()
argument_list|,
name|MetricsConnection
operator|.
name|newCallStats
argument_list|()
argument_list|)
expr_stmt|;
name|METRICS
operator|.
name|updateRpc
argument_list|(
name|ClientService
operator|.
name|getDescriptor
argument_list|()
operator|.
name|findMethodByName
argument_list|(
literal|"Mutate"
argument_list|)
argument_list|,
name|MutateRequest
operator|.
name|newBuilder
argument_list|()
operator|.
name|setMutation
argument_list|(
name|ProtobufUtil
operator|.
name|toMutation
argument_list|(
name|MutationType
operator|.
name|PUT
argument_list|,
operator|new
name|Put
argument_list|(
name|foo
argument_list|)
argument_list|)
argument_list|)
operator|.
name|setRegion
argument_list|(
name|region
argument_list|)
operator|.
name|build
argument_list|()
argument_list|,
name|MetricsConnection
operator|.
name|newCallStats
argument_list|()
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|MetricsConnection
operator|.
name|CallTracker
name|t
range|:
operator|new
name|MetricsConnection
operator|.
name|CallTracker
index|[]
block|{
name|METRICS
operator|.
name|getTracker
block|,
name|METRICS
operator|.
name|scanTracker
block|,
name|METRICS
operator|.
name|multiTracker
block|,
name|METRICS
operator|.
name|appendTracker
block|,
name|METRICS
operator|.
name|deleteTracker
block|,
name|METRICS
operator|.
name|incrementTracker
block|,
name|METRICS
operator|.
name|putTracker
block|}
control|)
block|{
name|assertEquals
argument_list|(
literal|"Failed to invoke callTimer on "
operator|+
name|t
argument_list|,
name|loop
argument_list|,
name|t
operator|.
name|callTimer
operator|.
name|getCount
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"Failed to invoke reqHist on "
operator|+
name|t
argument_list|,
name|loop
argument_list|,
name|t
operator|.
name|reqHist
operator|.
name|getCount
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"Failed to invoke respHist on "
operator|+
name|t
argument_list|,
name|loop
argument_list|,
name|t
operator|.
name|respHist
operator|.
name|getCount
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|RatioGauge
name|executorMetrics
init|=
operator|(
name|RatioGauge
operator|)
name|METRICS
operator|.
name|getMetricRegistry
argument_list|()
operator|.
name|getMetrics
argument_list|()
operator|.
name|get
argument_list|(
name|METRICS
operator|.
name|getExecutorPoolName
argument_list|()
argument_list|)
decl_stmt|;
name|RatioGauge
name|metaMetrics
init|=
operator|(
name|RatioGauge
operator|)
name|METRICS
operator|.
name|getMetricRegistry
argument_list|()
operator|.
name|getMetrics
argument_list|()
operator|.
name|get
argument_list|(
name|METRICS
operator|.
name|getMetaPoolName
argument_list|()
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|Ratio
operator|.
name|of
argument_list|(
literal|0
argument_list|,
literal|3
argument_list|)
operator|.
name|getValue
argument_list|()
argument_list|,
name|executorMetrics
operator|.
name|getValue
argument_list|()
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|Double
operator|.
name|NaN
argument_list|,
name|metaMetrics
operator|.
name|getValue
argument_list|()
argument_list|,
literal|0
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

