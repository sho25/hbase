begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|master
operator|.
name|normalizer
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
name|RETURNS_DEEP_STUBS
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
name|ArrayList
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
name|List
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
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|HBaseIOException
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
name|RegionLoad
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
name|ServerName
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
name|master
operator|.
name|MasterRpcServices
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
name|master
operator|.
name|MasterServices
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
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|ServiceException
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
name|MasterProtos
operator|.
name|IsSplitOrMergeEnabledResponse
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
name|MasterTests
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
name|BeforeClass
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
name|mockito
operator|.
name|Mockito
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|LoggerFactory
import|;
end_import

begin_comment
comment|/**  * Tests logic of {@link SimpleRegionNormalizer}.  */
end_comment

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|MasterTests
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
name|TestSimpleRegionNormalizer
block|{
specifier|private
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|TestSimpleRegionNormalizer
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|RegionNormalizer
name|normalizer
decl_stmt|;
comment|// mocks
specifier|private
specifier|static
name|MasterServices
name|masterServices
decl_stmt|;
specifier|private
specifier|static
name|MasterRpcServices
name|masterRpcServices
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
name|BeforeClass
specifier|public
specifier|static
name|void
name|beforeAllTests
parameter_list|()
throws|throws
name|Exception
block|{
name|normalizer
operator|=
operator|new
name|SimpleRegionNormalizer
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testNoNormalizationForMetaTable
parameter_list|()
throws|throws
name|HBaseIOException
block|{
name|TableName
name|testTable
init|=
name|TableName
operator|.
name|META_TABLE_NAME
decl_stmt|;
name|List
argument_list|<
name|RegionInfo
argument_list|>
name|RegionInfo
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|byte
index|[]
argument_list|,
name|Integer
argument_list|>
name|regionSizes
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|setupMocksForNormalizer
argument_list|(
name|regionSizes
argument_list|,
name|RegionInfo
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|NormalizationPlan
argument_list|>
name|plans
init|=
name|normalizer
operator|.
name|computePlanForTable
argument_list|(
name|testTable
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|plans
operator|==
literal|null
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testNoNormalizationIfTooFewRegions
parameter_list|()
throws|throws
name|HBaseIOException
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
name|List
argument_list|<
name|RegionInfo
argument_list|>
name|RegionInfo
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|byte
index|[]
argument_list|,
name|Integer
argument_list|>
name|regionSizes
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|RegionInfo
name|hri1
init|=
name|RegionInfoBuilder
operator|.
name|newBuilder
argument_list|(
name|tableName
argument_list|)
operator|.
name|setStartKey
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"aaa"
argument_list|)
argument_list|)
operator|.
name|setEndKey
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"bbb"
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|RegionInfo
operator|.
name|add
argument_list|(
name|hri1
argument_list|)
expr_stmt|;
name|regionSizes
operator|.
name|put
argument_list|(
name|hri1
operator|.
name|getRegionName
argument_list|()
argument_list|,
literal|10
argument_list|)
expr_stmt|;
name|RegionInfo
name|hri2
init|=
name|RegionInfoBuilder
operator|.
name|newBuilder
argument_list|(
name|tableName
argument_list|)
operator|.
name|setStartKey
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"bbb"
argument_list|)
argument_list|)
operator|.
name|setEndKey
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"ccc"
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|RegionInfo
operator|.
name|add
argument_list|(
name|hri2
argument_list|)
expr_stmt|;
name|regionSizes
operator|.
name|put
argument_list|(
name|hri2
operator|.
name|getRegionName
argument_list|()
argument_list|,
literal|15
argument_list|)
expr_stmt|;
name|setupMocksForNormalizer
argument_list|(
name|regionSizes
argument_list|,
name|RegionInfo
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|NormalizationPlan
argument_list|>
name|plans
init|=
name|normalizer
operator|.
name|computePlanForTable
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|plans
operator|==
literal|null
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testNoNormalizationOnNormalizedCluster
parameter_list|()
throws|throws
name|HBaseIOException
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
name|List
argument_list|<
name|RegionInfo
argument_list|>
name|RegionInfo
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|byte
index|[]
argument_list|,
name|Integer
argument_list|>
name|regionSizes
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|RegionInfo
name|hri1
init|=
name|RegionInfoBuilder
operator|.
name|newBuilder
argument_list|(
name|tableName
argument_list|)
operator|.
name|setStartKey
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"aaa"
argument_list|)
argument_list|)
operator|.
name|setEndKey
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"bbb"
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|RegionInfo
operator|.
name|add
argument_list|(
name|hri1
argument_list|)
expr_stmt|;
name|regionSizes
operator|.
name|put
argument_list|(
name|hri1
operator|.
name|getRegionName
argument_list|()
argument_list|,
literal|10
argument_list|)
expr_stmt|;
name|RegionInfo
name|hri2
init|=
name|RegionInfoBuilder
operator|.
name|newBuilder
argument_list|(
name|tableName
argument_list|)
operator|.
name|setStartKey
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"bbb"
argument_list|)
argument_list|)
operator|.
name|setEndKey
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"ccc"
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|RegionInfo
operator|.
name|add
argument_list|(
name|hri2
argument_list|)
expr_stmt|;
name|regionSizes
operator|.
name|put
argument_list|(
name|hri2
operator|.
name|getRegionName
argument_list|()
argument_list|,
literal|15
argument_list|)
expr_stmt|;
name|RegionInfo
name|hri3
init|=
name|RegionInfoBuilder
operator|.
name|newBuilder
argument_list|(
name|tableName
argument_list|)
operator|.
name|setStartKey
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"ccc"
argument_list|)
argument_list|)
operator|.
name|setEndKey
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"ddd"
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|RegionInfo
operator|.
name|add
argument_list|(
name|hri3
argument_list|)
expr_stmt|;
name|regionSizes
operator|.
name|put
argument_list|(
name|hri3
operator|.
name|getRegionName
argument_list|()
argument_list|,
literal|8
argument_list|)
expr_stmt|;
name|RegionInfo
name|hri4
init|=
name|RegionInfoBuilder
operator|.
name|newBuilder
argument_list|(
name|tableName
argument_list|)
operator|.
name|setStartKey
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"ddd"
argument_list|)
argument_list|)
operator|.
name|setEndKey
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"eee"
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|regionSizes
operator|.
name|put
argument_list|(
name|hri4
operator|.
name|getRegionName
argument_list|()
argument_list|,
literal|10
argument_list|)
expr_stmt|;
name|setupMocksForNormalizer
argument_list|(
name|regionSizes
argument_list|,
name|RegionInfo
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|NormalizationPlan
argument_list|>
name|plans
init|=
name|normalizer
operator|.
name|computePlanForTable
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|plans
operator|==
literal|null
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testMergeOfSmallRegions
parameter_list|()
throws|throws
name|HBaseIOException
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
name|List
argument_list|<
name|RegionInfo
argument_list|>
name|RegionInfo
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|byte
index|[]
argument_list|,
name|Integer
argument_list|>
name|regionSizes
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|RegionInfo
name|hri1
init|=
name|RegionInfoBuilder
operator|.
name|newBuilder
argument_list|(
name|tableName
argument_list|)
operator|.
name|setStartKey
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"aaa"
argument_list|)
argument_list|)
operator|.
name|setEndKey
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"bbb"
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|RegionInfo
operator|.
name|add
argument_list|(
name|hri1
argument_list|)
expr_stmt|;
name|regionSizes
operator|.
name|put
argument_list|(
name|hri1
operator|.
name|getRegionName
argument_list|()
argument_list|,
literal|15
argument_list|)
expr_stmt|;
name|RegionInfo
name|hri2
init|=
name|RegionInfoBuilder
operator|.
name|newBuilder
argument_list|(
name|tableName
argument_list|)
operator|.
name|setStartKey
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"bbb"
argument_list|)
argument_list|)
operator|.
name|setEndKey
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"ccc"
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|RegionInfo
operator|.
name|add
argument_list|(
name|hri2
argument_list|)
expr_stmt|;
name|regionSizes
operator|.
name|put
argument_list|(
name|hri2
operator|.
name|getRegionName
argument_list|()
argument_list|,
literal|5
argument_list|)
expr_stmt|;
name|RegionInfo
name|hri3
init|=
name|RegionInfoBuilder
operator|.
name|newBuilder
argument_list|(
name|tableName
argument_list|)
operator|.
name|setStartKey
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"ccc"
argument_list|)
argument_list|)
operator|.
name|setEndKey
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"ddd"
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|RegionInfo
operator|.
name|add
argument_list|(
name|hri3
argument_list|)
expr_stmt|;
name|regionSizes
operator|.
name|put
argument_list|(
name|hri3
operator|.
name|getRegionName
argument_list|()
argument_list|,
literal|5
argument_list|)
expr_stmt|;
name|RegionInfo
name|hri4
init|=
name|RegionInfoBuilder
operator|.
name|newBuilder
argument_list|(
name|tableName
argument_list|)
operator|.
name|setStartKey
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"ddd"
argument_list|)
argument_list|)
operator|.
name|setEndKey
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"eee"
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|RegionInfo
operator|.
name|add
argument_list|(
name|hri4
argument_list|)
expr_stmt|;
name|regionSizes
operator|.
name|put
argument_list|(
name|hri4
operator|.
name|getRegionName
argument_list|()
argument_list|,
literal|15
argument_list|)
expr_stmt|;
name|RegionInfo
name|hri5
init|=
name|RegionInfoBuilder
operator|.
name|newBuilder
argument_list|(
name|tableName
argument_list|)
operator|.
name|setStartKey
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"eee"
argument_list|)
argument_list|)
operator|.
name|setEndKey
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"fff"
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|RegionInfo
operator|.
name|add
argument_list|(
name|hri5
argument_list|)
expr_stmt|;
name|regionSizes
operator|.
name|put
argument_list|(
name|hri5
operator|.
name|getRegionName
argument_list|()
argument_list|,
literal|16
argument_list|)
expr_stmt|;
name|setupMocksForNormalizer
argument_list|(
name|regionSizes
argument_list|,
name|RegionInfo
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|NormalizationPlan
argument_list|>
name|plans
init|=
name|normalizer
operator|.
name|computePlanForTable
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
name|NormalizationPlan
name|plan
init|=
name|plans
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|plan
operator|instanceof
name|MergeNormalizationPlan
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|hri2
argument_list|,
operator|(
operator|(
name|MergeNormalizationPlan
operator|)
name|plan
operator|)
operator|.
name|getFirstRegion
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|hri3
argument_list|,
operator|(
operator|(
name|MergeNormalizationPlan
operator|)
name|plan
operator|)
operator|.
name|getSecondRegion
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|// Test for situation illustrated in HBASE-14867
annotation|@
name|Test
specifier|public
name|void
name|testMergeOfSecondSmallestRegions
parameter_list|()
throws|throws
name|HBaseIOException
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
name|List
argument_list|<
name|RegionInfo
argument_list|>
name|RegionInfo
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|byte
index|[]
argument_list|,
name|Integer
argument_list|>
name|regionSizes
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|RegionInfo
name|hri1
init|=
name|RegionInfoBuilder
operator|.
name|newBuilder
argument_list|(
name|tableName
argument_list|)
operator|.
name|setStartKey
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"aaa"
argument_list|)
argument_list|)
operator|.
name|setEndKey
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"bbb"
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|RegionInfo
operator|.
name|add
argument_list|(
name|hri1
argument_list|)
expr_stmt|;
name|regionSizes
operator|.
name|put
argument_list|(
name|hri1
operator|.
name|getRegionName
argument_list|()
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|RegionInfo
name|hri2
init|=
name|RegionInfoBuilder
operator|.
name|newBuilder
argument_list|(
name|tableName
argument_list|)
operator|.
name|setStartKey
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"bbb"
argument_list|)
argument_list|)
operator|.
name|setEndKey
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"ccc"
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|RegionInfo
operator|.
name|add
argument_list|(
name|hri2
argument_list|)
expr_stmt|;
name|regionSizes
operator|.
name|put
argument_list|(
name|hri2
operator|.
name|getRegionName
argument_list|()
argument_list|,
literal|10000
argument_list|)
expr_stmt|;
name|RegionInfo
name|hri3
init|=
name|RegionInfoBuilder
operator|.
name|newBuilder
argument_list|(
name|tableName
argument_list|)
operator|.
name|setStartKey
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"ccc"
argument_list|)
argument_list|)
operator|.
name|setEndKey
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"ddd"
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|RegionInfo
operator|.
name|add
argument_list|(
name|hri3
argument_list|)
expr_stmt|;
name|regionSizes
operator|.
name|put
argument_list|(
name|hri3
operator|.
name|getRegionName
argument_list|()
argument_list|,
literal|10000
argument_list|)
expr_stmt|;
name|RegionInfo
name|hri4
init|=
name|RegionInfoBuilder
operator|.
name|newBuilder
argument_list|(
name|tableName
argument_list|)
operator|.
name|setStartKey
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"ddd"
argument_list|)
argument_list|)
operator|.
name|setEndKey
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"eee"
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|RegionInfo
operator|.
name|add
argument_list|(
name|hri4
argument_list|)
expr_stmt|;
name|regionSizes
operator|.
name|put
argument_list|(
name|hri4
operator|.
name|getRegionName
argument_list|()
argument_list|,
literal|10000
argument_list|)
expr_stmt|;
name|RegionInfo
name|hri5
init|=
name|RegionInfoBuilder
operator|.
name|newBuilder
argument_list|(
name|tableName
argument_list|)
operator|.
name|setStartKey
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"eee"
argument_list|)
argument_list|)
operator|.
name|setEndKey
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"fff"
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|RegionInfo
operator|.
name|add
argument_list|(
name|hri5
argument_list|)
expr_stmt|;
name|regionSizes
operator|.
name|put
argument_list|(
name|hri5
operator|.
name|getRegionName
argument_list|()
argument_list|,
literal|2700
argument_list|)
expr_stmt|;
name|RegionInfo
name|hri6
init|=
name|RegionInfoBuilder
operator|.
name|newBuilder
argument_list|(
name|tableName
argument_list|)
operator|.
name|setStartKey
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"fff"
argument_list|)
argument_list|)
operator|.
name|setEndKey
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"ggg"
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|RegionInfo
operator|.
name|add
argument_list|(
name|hri6
argument_list|)
expr_stmt|;
name|regionSizes
operator|.
name|put
argument_list|(
name|hri6
operator|.
name|getRegionName
argument_list|()
argument_list|,
literal|2700
argument_list|)
expr_stmt|;
name|setupMocksForNormalizer
argument_list|(
name|regionSizes
argument_list|,
name|RegionInfo
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|NormalizationPlan
argument_list|>
name|plans
init|=
name|normalizer
operator|.
name|computePlanForTable
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
name|NormalizationPlan
name|plan
init|=
name|plans
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|plan
operator|instanceof
name|MergeNormalizationPlan
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|hri5
argument_list|,
operator|(
operator|(
name|MergeNormalizationPlan
operator|)
name|plan
operator|)
operator|.
name|getFirstRegion
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|hri6
argument_list|,
operator|(
operator|(
name|MergeNormalizationPlan
operator|)
name|plan
operator|)
operator|.
name|getSecondRegion
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testMergeOfSmallNonAdjacentRegions
parameter_list|()
throws|throws
name|HBaseIOException
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
name|List
argument_list|<
name|RegionInfo
argument_list|>
name|RegionInfo
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|byte
index|[]
argument_list|,
name|Integer
argument_list|>
name|regionSizes
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|RegionInfo
name|hri1
init|=
name|RegionInfoBuilder
operator|.
name|newBuilder
argument_list|(
name|tableName
argument_list|)
operator|.
name|setStartKey
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"aaa"
argument_list|)
argument_list|)
operator|.
name|setEndKey
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"bbb"
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|RegionInfo
operator|.
name|add
argument_list|(
name|hri1
argument_list|)
expr_stmt|;
name|regionSizes
operator|.
name|put
argument_list|(
name|hri1
operator|.
name|getRegionName
argument_list|()
argument_list|,
literal|15
argument_list|)
expr_stmt|;
name|RegionInfo
name|hri2
init|=
name|RegionInfoBuilder
operator|.
name|newBuilder
argument_list|(
name|tableName
argument_list|)
operator|.
name|setStartKey
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"bbb"
argument_list|)
argument_list|)
operator|.
name|setEndKey
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"ccc"
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|RegionInfo
operator|.
name|add
argument_list|(
name|hri2
argument_list|)
expr_stmt|;
name|regionSizes
operator|.
name|put
argument_list|(
name|hri2
operator|.
name|getRegionName
argument_list|()
argument_list|,
literal|5
argument_list|)
expr_stmt|;
name|RegionInfo
name|hri3
init|=
name|RegionInfoBuilder
operator|.
name|newBuilder
argument_list|(
name|tableName
argument_list|)
operator|.
name|setStartKey
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"ccc"
argument_list|)
argument_list|)
operator|.
name|setEndKey
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"ddd"
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|RegionInfo
operator|.
name|add
argument_list|(
name|hri3
argument_list|)
expr_stmt|;
name|regionSizes
operator|.
name|put
argument_list|(
name|hri3
operator|.
name|getRegionName
argument_list|()
argument_list|,
literal|16
argument_list|)
expr_stmt|;
name|RegionInfo
name|hri4
init|=
name|RegionInfoBuilder
operator|.
name|newBuilder
argument_list|(
name|tableName
argument_list|)
operator|.
name|setStartKey
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"ddd"
argument_list|)
argument_list|)
operator|.
name|setEndKey
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"eee"
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|RegionInfo
operator|.
name|add
argument_list|(
name|hri4
argument_list|)
expr_stmt|;
name|regionSizes
operator|.
name|put
argument_list|(
name|hri4
operator|.
name|getRegionName
argument_list|()
argument_list|,
literal|15
argument_list|)
expr_stmt|;
name|RegionInfo
name|hri5
init|=
name|RegionInfoBuilder
operator|.
name|newBuilder
argument_list|(
name|tableName
argument_list|)
operator|.
name|setStartKey
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"ddd"
argument_list|)
argument_list|)
operator|.
name|setEndKey
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"eee"
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|RegionInfo
operator|.
name|add
argument_list|(
name|hri4
argument_list|)
expr_stmt|;
name|regionSizes
operator|.
name|put
argument_list|(
name|hri5
operator|.
name|getRegionName
argument_list|()
argument_list|,
literal|5
argument_list|)
expr_stmt|;
name|setupMocksForNormalizer
argument_list|(
name|regionSizes
argument_list|,
name|RegionInfo
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|NormalizationPlan
argument_list|>
name|plans
init|=
name|normalizer
operator|.
name|computePlanForTable
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|plans
operator|==
literal|null
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testSplitOfLargeRegion
parameter_list|()
throws|throws
name|HBaseIOException
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
name|List
argument_list|<
name|RegionInfo
argument_list|>
name|RegionInfo
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|byte
index|[]
argument_list|,
name|Integer
argument_list|>
name|regionSizes
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|RegionInfo
name|hri1
init|=
name|RegionInfoBuilder
operator|.
name|newBuilder
argument_list|(
name|tableName
argument_list|)
operator|.
name|setStartKey
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"aaa"
argument_list|)
argument_list|)
operator|.
name|setEndKey
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"bbb"
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|RegionInfo
operator|.
name|add
argument_list|(
name|hri1
argument_list|)
expr_stmt|;
name|regionSizes
operator|.
name|put
argument_list|(
name|hri1
operator|.
name|getRegionName
argument_list|()
argument_list|,
literal|8
argument_list|)
expr_stmt|;
name|RegionInfo
name|hri2
init|=
name|RegionInfoBuilder
operator|.
name|newBuilder
argument_list|(
name|tableName
argument_list|)
operator|.
name|setStartKey
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"bbb"
argument_list|)
argument_list|)
operator|.
name|setEndKey
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"ccc"
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|RegionInfo
operator|.
name|add
argument_list|(
name|hri2
argument_list|)
expr_stmt|;
name|regionSizes
operator|.
name|put
argument_list|(
name|hri2
operator|.
name|getRegionName
argument_list|()
argument_list|,
literal|6
argument_list|)
expr_stmt|;
name|RegionInfo
name|hri3
init|=
name|RegionInfoBuilder
operator|.
name|newBuilder
argument_list|(
name|tableName
argument_list|)
operator|.
name|setStartKey
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"ccc"
argument_list|)
argument_list|)
operator|.
name|setEndKey
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"ddd"
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|RegionInfo
operator|.
name|add
argument_list|(
name|hri3
argument_list|)
expr_stmt|;
name|regionSizes
operator|.
name|put
argument_list|(
name|hri3
operator|.
name|getRegionName
argument_list|()
argument_list|,
literal|10
argument_list|)
expr_stmt|;
name|RegionInfo
name|hri4
init|=
name|RegionInfoBuilder
operator|.
name|newBuilder
argument_list|(
name|tableName
argument_list|)
operator|.
name|setStartKey
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"ddd"
argument_list|)
argument_list|)
operator|.
name|setEndKey
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"eee"
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|RegionInfo
operator|.
name|add
argument_list|(
name|hri4
argument_list|)
expr_stmt|;
name|regionSizes
operator|.
name|put
argument_list|(
name|hri4
operator|.
name|getRegionName
argument_list|()
argument_list|,
literal|30
argument_list|)
expr_stmt|;
name|setupMocksForNormalizer
argument_list|(
name|regionSizes
argument_list|,
name|RegionInfo
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|NormalizationPlan
argument_list|>
name|plans
init|=
name|normalizer
operator|.
name|computePlanForTable
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
name|NormalizationPlan
name|plan
init|=
name|plans
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|plan
operator|instanceof
name|SplitNormalizationPlan
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|hri4
argument_list|,
operator|(
operator|(
name|SplitNormalizationPlan
operator|)
name|plan
operator|)
operator|.
name|getRegionInfo
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|SuppressWarnings
argument_list|(
literal|"MockitoCast"
argument_list|)
specifier|protected
name|void
name|setupMocksForNormalizer
parameter_list|(
name|Map
argument_list|<
name|byte
index|[]
argument_list|,
name|Integer
argument_list|>
name|regionSizes
parameter_list|,
name|List
argument_list|<
name|RegionInfo
argument_list|>
name|RegionInfo
parameter_list|)
block|{
name|masterServices
operator|=
name|Mockito
operator|.
name|mock
argument_list|(
name|MasterServices
operator|.
name|class
argument_list|,
name|RETURNS_DEEP_STUBS
argument_list|)
expr_stmt|;
name|masterRpcServices
operator|=
name|Mockito
operator|.
name|mock
argument_list|(
name|MasterRpcServices
operator|.
name|class
argument_list|,
name|RETURNS_DEEP_STUBS
argument_list|)
expr_stmt|;
comment|// for simplicity all regions are assumed to be on one server; doesn't matter to us
name|ServerName
name|sn
init|=
name|ServerName
operator|.
name|valueOf
argument_list|(
literal|"localhost"
argument_list|,
literal|0
argument_list|,
literal|1L
argument_list|)
decl_stmt|;
name|when
argument_list|(
name|masterServices
operator|.
name|getAssignmentManager
argument_list|()
operator|.
name|getRegionStates
argument_list|()
operator|.
name|getRegionsOfTable
argument_list|(
name|any
argument_list|()
argument_list|)
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|RegionInfo
argument_list|)
expr_stmt|;
name|when
argument_list|(
name|masterServices
operator|.
name|getAssignmentManager
argument_list|()
operator|.
name|getRegionStates
argument_list|()
operator|.
name|getRegionServerOfRegion
argument_list|(
name|any
argument_list|()
argument_list|)
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|sn
argument_list|)
expr_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|byte
index|[]
argument_list|,
name|Integer
argument_list|>
name|region
range|:
name|regionSizes
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|RegionLoad
name|regionLoad
init|=
name|Mockito
operator|.
name|mock
argument_list|(
name|RegionLoad
operator|.
name|class
argument_list|)
decl_stmt|;
name|when
argument_list|(
name|regionLoad
operator|.
name|getName
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|region
operator|.
name|getKey
argument_list|()
argument_list|)
expr_stmt|;
name|when
argument_list|(
name|regionLoad
operator|.
name|getStorefileSizeMB
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|region
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
comment|// this is possibly broken with jdk9, unclear if false positive or not
comment|// suppress it for now, fix it when we get to running tests on 9
comment|// see: http://errorprone.info/bugpattern/MockitoCast
name|when
argument_list|(
operator|(
name|Object
operator|)
name|masterServices
operator|.
name|getServerManager
argument_list|()
operator|.
name|getLoad
argument_list|(
name|sn
argument_list|)
operator|.
name|getRegionsLoad
argument_list|()
operator|.
name|get
argument_list|(
name|region
operator|.
name|getKey
argument_list|()
argument_list|)
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|regionLoad
argument_list|)
expr_stmt|;
block|}
try|try
block|{
name|when
argument_list|(
name|masterRpcServices
operator|.
name|isSplitOrMergeEnabled
argument_list|(
name|any
argument_list|()
argument_list|,
name|any
argument_list|()
argument_list|)
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|IsSplitOrMergeEnabledResponse
operator|.
name|newBuilder
argument_list|()
operator|.
name|setEnabled
argument_list|(
literal|true
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ServiceException
name|se
parameter_list|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"error setting isSplitOrMergeEnabled switch"
argument_list|,
name|se
argument_list|)
expr_stmt|;
block|}
name|normalizer
operator|.
name|setMasterServices
argument_list|(
name|masterServices
argument_list|)
expr_stmt|;
name|normalizer
operator|.
name|setMasterRpcServices
argument_list|(
name|masterRpcServices
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

