begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Copyright The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one or more  * contributor license agreements. See the NOTICE file distributed with this  * work for additional information regarding copyright ownership. The ASF  * licenses this file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the  * License for the specific language governing permissions and limitations  * under the License.  */
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
name|Arrays
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
name|Set
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
name|HBaseTestingUtility
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
name|KeyValue
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
name|MediumTests
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
name|Append
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
name|Delete
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
name|Get
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
name|HBaseAdmin
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
name|HTable
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
name|Put
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
name|regionserver
operator|.
name|metrics
operator|.
name|RegionMetricsStorage
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
name|regionserver
operator|.
name|metrics
operator|.
name|RegionServerMetrics
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
name|regionserver
operator|.
name|metrics
operator|.
name|SchemaMetrics
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
name|regionserver
operator|.
name|metrics
operator|.
name|SchemaMetrics
operator|.
name|StoreMetricType
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
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|util
operator|.
name|Pair
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|After
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

begin_comment
comment|/**  * Test metrics incremented on region server operations.  */
end_comment

begin_class
annotation|@
name|Category
argument_list|(
name|MediumTests
operator|.
name|class
argument_list|)
specifier|public
class|class
name|TestRegionServerMetrics
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
name|TestRegionServerMetrics
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
specifier|private
specifier|final
specifier|static
name|String
name|TABLE_NAME
init|=
name|TestRegionServerMetrics
operator|.
name|class
operator|.
name|getSimpleName
argument_list|()
operator|+
literal|"Table"
decl_stmt|;
specifier|private
name|String
index|[]
name|FAMILIES
init|=
operator|new
name|String
index|[]
block|{
literal|"cf1"
block|,
literal|"cf2"
block|,
literal|"anotherCF"
block|}
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|MAX_VERSIONS
init|=
literal|1
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|NUM_COLS_PER_ROW
init|=
literal|15
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|NUM_FLUSHES
init|=
literal|3
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|NUM_REGIONS
init|=
literal|4
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|SchemaMetrics
name|ALL_METRICS
init|=
name|SchemaMetrics
operator|.
name|ALL_SCHEMA_METRICS
decl_stmt|;
specifier|private
specifier|final
name|HBaseTestingUtility
name|TEST_UTIL
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
name|startingMetrics
decl_stmt|;
specifier|private
specifier|final
name|int
name|META_AND_ROOT
init|=
literal|2
decl_stmt|;
annotation|@
name|Before
specifier|public
name|void
name|setUp
parameter_list|()
throws|throws
name|Exception
block|{
name|SchemaMetrics
operator|.
name|setUseTableNameInTest
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|startingMetrics
operator|=
name|SchemaMetrics
operator|.
name|getMetricsSnapshot
argument_list|()
expr_stmt|;
name|TEST_UTIL
operator|.
name|startMiniCluster
argument_list|()
expr_stmt|;
block|}
annotation|@
name|After
specifier|public
name|void
name|tearDown
parameter_list|()
throws|throws
name|Exception
block|{
name|TEST_UTIL
operator|.
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
name|SchemaMetrics
operator|.
name|validateMetricChanges
argument_list|(
name|startingMetrics
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|assertTimeVaryingMetricCount
parameter_list|(
name|int
name|expectedCount
parameter_list|,
name|String
name|table
parameter_list|,
name|String
name|cf
parameter_list|,
name|String
name|regionName
parameter_list|,
name|String
name|metricPrefix
parameter_list|)
block|{
name|Integer
name|expectedCountInteger
init|=
operator|new
name|Integer
argument_list|(
name|expectedCount
argument_list|)
decl_stmt|;
if|if
condition|(
name|cf
operator|!=
literal|null
condition|)
block|{
name|String
name|cfKey
init|=
name|SchemaMetrics
operator|.
name|TABLE_PREFIX
operator|+
name|table
operator|+
literal|"."
operator|+
name|SchemaMetrics
operator|.
name|CF_PREFIX
operator|+
name|cf
operator|+
literal|"."
operator|+
name|metricPrefix
decl_stmt|;
name|Pair
argument_list|<
name|Long
argument_list|,
name|Integer
argument_list|>
name|cfPair
init|=
name|RegionMetricsStorage
operator|.
name|getTimeVaryingMetric
argument_list|(
name|cfKey
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|expectedCountInteger
argument_list|,
name|cfPair
operator|.
name|getSecond
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|regionName
operator|!=
literal|null
condition|)
block|{
name|String
name|rKey
init|=
name|SchemaMetrics
operator|.
name|TABLE_PREFIX
operator|+
name|table
operator|+
literal|"."
operator|+
name|SchemaMetrics
operator|.
name|REGION_PREFIX
operator|+
name|regionName
operator|+
literal|"."
operator|+
name|metricPrefix
decl_stmt|;
name|Pair
argument_list|<
name|Long
argument_list|,
name|Integer
argument_list|>
name|regionPair
init|=
name|RegionMetricsStorage
operator|.
name|getTimeVaryingMetric
argument_list|(
name|rKey
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|expectedCountInteger
argument_list|,
name|regionPair
operator|.
name|getSecond
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|assertStoreMetricEquals
parameter_list|(
name|long
name|expected
parameter_list|,
name|SchemaMetrics
name|schemaMetrics
parameter_list|,
name|StoreMetricType
name|storeMetricType
parameter_list|)
block|{
specifier|final
name|String
name|storeMetricName
init|=
name|schemaMetrics
operator|.
name|getStoreMetricName
argument_list|(
name|storeMetricType
argument_list|)
decl_stmt|;
name|Long
name|startValue
init|=
name|startingMetrics
operator|.
name|get
argument_list|(
name|storeMetricName
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"Invalid value for store metric "
operator|+
name|storeMetricName
operator|+
literal|" (type "
operator|+
name|storeMetricType
operator|+
literal|")"
argument_list|,
name|expected
argument_list|,
name|RegionMetricsStorage
operator|.
name|getNumericMetric
argument_list|(
name|storeMetricName
argument_list|)
operator|-
operator|(
name|startValue
operator|!=
literal|null
condition|?
name|startValue
else|:
literal|0
operator|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testOperationMetrics
parameter_list|()
throws|throws
name|IOException
block|{
name|String
name|cf
init|=
literal|"OPCF"
decl_stmt|;
name|String
name|otherCf
init|=
literal|"otherCF"
decl_stmt|;
name|String
name|rk
init|=
literal|"testRK"
decl_stmt|;
name|String
name|icvCol
init|=
literal|"icvCol"
decl_stmt|;
name|String
name|appendCol
init|=
literal|"appendCol"
decl_stmt|;
name|String
name|regionName
init|=
literal|null
decl_stmt|;
name|HTable
name|hTable
init|=
name|TEST_UTIL
operator|.
name|createTable
argument_list|(
name|TABLE_NAME
operator|.
name|getBytes
argument_list|()
argument_list|,
operator|new
name|byte
index|[]
index|[]
block|{
name|cf
operator|.
name|getBytes
argument_list|()
block|,
name|otherCf
operator|.
name|getBytes
argument_list|()
block|}
argument_list|)
decl_stmt|;
name|Set
argument_list|<
name|HRegionInfo
argument_list|>
name|regionInfos
init|=
name|hTable
operator|.
name|getRegionLocations
argument_list|()
operator|.
name|keySet
argument_list|()
decl_stmt|;
name|regionName
operator|=
name|regionInfos
operator|.
name|toArray
argument_list|(
operator|new
name|HRegionInfo
index|[
name|regionInfos
operator|.
name|size
argument_list|()
index|]
argument_list|)
index|[
literal|0
index|]
operator|.
name|getEncodedName
argument_list|()
expr_stmt|;
comment|//Do a multi put that has one cf.  Since they are in different rk's
comment|//The lock will still be obtained and everything will be applied in one multiput.
name|Put
name|pOne
init|=
operator|new
name|Put
argument_list|(
name|rk
operator|.
name|getBytes
argument_list|()
argument_list|)
decl_stmt|;
name|pOne
operator|.
name|add
argument_list|(
name|cf
operator|.
name|getBytes
argument_list|()
argument_list|,
name|icvCol
operator|.
name|getBytes
argument_list|()
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|0L
argument_list|)
argument_list|)
expr_stmt|;
name|Put
name|pTwo
init|=
operator|new
name|Put
argument_list|(
literal|"ignored1RK"
operator|.
name|getBytes
argument_list|()
argument_list|)
decl_stmt|;
name|pTwo
operator|.
name|add
argument_list|(
name|cf
operator|.
name|getBytes
argument_list|()
argument_list|,
literal|"ignored"
operator|.
name|getBytes
argument_list|()
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|0L
argument_list|)
argument_list|)
expr_stmt|;
name|hTable
operator|.
name|put
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
operator|new
name|Put
index|[]
block|{
name|pOne
block|,
name|pTwo
block|}
argument_list|)
argument_list|)
expr_stmt|;
comment|// Do a multiput where the cf doesn't stay consistent.
name|Put
name|pThree
init|=
operator|new
name|Put
argument_list|(
literal|"ignored2RK"
operator|.
name|getBytes
argument_list|()
argument_list|)
decl_stmt|;
name|pThree
operator|.
name|add
argument_list|(
name|cf
operator|.
name|getBytes
argument_list|()
argument_list|,
literal|"ignored"
operator|.
name|getBytes
argument_list|()
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"TEST1"
argument_list|)
argument_list|)
expr_stmt|;
name|Put
name|pFour
init|=
operator|new
name|Put
argument_list|(
literal|"ignored3RK"
operator|.
name|getBytes
argument_list|()
argument_list|)
decl_stmt|;
name|pFour
operator|.
name|add
argument_list|(
name|otherCf
operator|.
name|getBytes
argument_list|()
argument_list|,
literal|"ignored"
operator|.
name|getBytes
argument_list|()
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|0L
argument_list|)
argument_list|)
expr_stmt|;
name|hTable
operator|.
name|put
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
operator|new
name|Put
index|[]
block|{
name|pThree
block|,
name|pFour
block|}
argument_list|)
argument_list|)
expr_stmt|;
name|hTable
operator|.
name|incrementColumnValue
argument_list|(
name|rk
operator|.
name|getBytes
argument_list|()
argument_list|,
name|cf
operator|.
name|getBytes
argument_list|()
argument_list|,
name|icvCol
operator|.
name|getBytes
argument_list|()
argument_list|,
literal|1L
argument_list|)
expr_stmt|;
name|Get
name|g
init|=
operator|new
name|Get
argument_list|(
name|rk
operator|.
name|getBytes
argument_list|()
argument_list|)
decl_stmt|;
name|g
operator|.
name|addColumn
argument_list|(
name|cf
operator|.
name|getBytes
argument_list|()
argument_list|,
name|appendCol
operator|.
name|getBytes
argument_list|()
argument_list|)
expr_stmt|;
name|hTable
operator|.
name|get
argument_list|(
name|g
argument_list|)
expr_stmt|;
name|Append
name|a
init|=
operator|new
name|Append
argument_list|(
name|rk
operator|.
name|getBytes
argument_list|()
argument_list|)
decl_stmt|;
name|a
operator|.
name|add
argument_list|(
name|cf
operator|.
name|getBytes
argument_list|()
argument_list|,
name|appendCol
operator|.
name|getBytes
argument_list|()
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"-APPEND"
argument_list|)
argument_list|)
expr_stmt|;
name|hTable
operator|.
name|append
argument_list|(
name|a
argument_list|)
expr_stmt|;
name|Delete
name|dOne
init|=
operator|new
name|Delete
argument_list|(
name|rk
operator|.
name|getBytes
argument_list|()
argument_list|)
decl_stmt|;
name|dOne
operator|.
name|deleteFamily
argument_list|(
name|cf
operator|.
name|getBytes
argument_list|()
argument_list|)
expr_stmt|;
name|hTable
operator|.
name|delete
argument_list|(
name|dOne
argument_list|)
expr_stmt|;
name|Delete
name|dTwo
init|=
operator|new
name|Delete
argument_list|(
name|rk
operator|.
name|getBytes
argument_list|()
argument_list|)
decl_stmt|;
name|hTable
operator|.
name|delete
argument_list|(
name|dTwo
argument_list|)
expr_stmt|;
comment|// There should be one multi put where the cf is consistent
name|assertTimeVaryingMetricCount
argument_list|(
literal|1
argument_list|,
name|TABLE_NAME
argument_list|,
name|cf
argument_list|,
literal|null
argument_list|,
literal|"multiput_"
argument_list|)
expr_stmt|;
comment|// There were two multiputs to the cf.
name|assertTimeVaryingMetricCount
argument_list|(
literal|2
argument_list|,
name|TABLE_NAME
argument_list|,
literal|null
argument_list|,
name|regionName
argument_list|,
literal|"multiput_"
argument_list|)
expr_stmt|;
comment|// There was one multiput where the cf was not consistent.
name|assertTimeVaryingMetricCount
argument_list|(
literal|1
argument_list|,
name|TABLE_NAME
argument_list|,
literal|"__unknown"
argument_list|,
literal|null
argument_list|,
literal|"multiput_"
argument_list|)
expr_stmt|;
comment|// One increment and one append
name|assertTimeVaryingMetricCount
argument_list|(
literal|1
argument_list|,
name|TABLE_NAME
argument_list|,
name|cf
argument_list|,
name|regionName
argument_list|,
literal|"increment_"
argument_list|)
expr_stmt|;
name|assertTimeVaryingMetricCount
argument_list|(
literal|1
argument_list|,
name|TABLE_NAME
argument_list|,
name|cf
argument_list|,
name|regionName
argument_list|,
literal|"append_"
argument_list|)
expr_stmt|;
comment|// One delete where the cf is known
name|assertTimeVaryingMetricCount
argument_list|(
literal|1
argument_list|,
name|TABLE_NAME
argument_list|,
name|cf
argument_list|,
literal|null
argument_list|,
literal|"multidelete_"
argument_list|)
expr_stmt|;
comment|// two deletes in the region.
name|assertTimeVaryingMetricCount
argument_list|(
literal|2
argument_list|,
name|TABLE_NAME
argument_list|,
literal|null
argument_list|,
name|regionName
argument_list|,
literal|"multidelete_"
argument_list|)
expr_stmt|;
comment|// Three gets. one for gets. One for append. One for increment.
name|assertTimeVaryingMetricCount
argument_list|(
literal|3
argument_list|,
name|TABLE_NAME
argument_list|,
name|cf
argument_list|,
name|regionName
argument_list|,
literal|"get_"
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|assertCheckAndMutateMetrics
parameter_list|(
specifier|final
name|HRegionServer
name|rs
parameter_list|,
name|long
name|expectedPassed
parameter_list|,
name|long
name|expectedFailed
parameter_list|)
block|{
name|rs
operator|.
name|doMetrics
argument_list|()
expr_stmt|;
name|RegionServerMetrics
name|metrics
init|=
name|rs
operator|.
name|getMetrics
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|"checkAndMutatePassed metrics incorrect"
argument_list|,
name|expectedPassed
argument_list|,
name|metrics
operator|.
name|checkAndMutateChecksPassed
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"checkAndMutateFailed metrics incorrect"
argument_list|,
name|expectedFailed
argument_list|,
name|metrics
operator|.
name|checkAndMutateChecksFailed
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testCheckAndMutateMetrics
parameter_list|()
throws|throws
name|Exception
block|{
specifier|final
name|HRegionServer
name|rs
init|=
name|TEST_UTIL
operator|.
name|getMiniHBaseCluster
argument_list|()
operator|.
name|getRegionServer
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|byte
index|[]
name|tableName
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"testCheckAndMutateMetrics"
argument_list|)
decl_stmt|;
name|byte
index|[]
name|family
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"family"
argument_list|)
decl_stmt|;
name|byte
index|[]
name|qualifier
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"qualifier"
argument_list|)
decl_stmt|;
name|byte
index|[]
name|row
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"row1"
argument_list|)
decl_stmt|;
name|HTable
name|table
init|=
name|TEST_UTIL
operator|.
name|createTable
argument_list|(
name|tableName
argument_list|,
name|family
argument_list|)
decl_stmt|;
name|long
name|expectedPassed
init|=
literal|0
decl_stmt|;
name|long
name|expectedFailed
init|=
literal|0
decl_stmt|;
comment|// checkAndPut success
name|Put
name|put
init|=
operator|new
name|Put
argument_list|(
name|row
argument_list|)
decl_stmt|;
name|byte
index|[]
name|val1
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"val1"
argument_list|)
decl_stmt|;
name|put
operator|.
name|add
argument_list|(
name|family
argument_list|,
name|qualifier
argument_list|,
name|val1
argument_list|)
expr_stmt|;
name|table
operator|.
name|checkAndPut
argument_list|(
name|row
argument_list|,
name|family
argument_list|,
name|qualifier
argument_list|,
literal|null
argument_list|,
name|put
argument_list|)
expr_stmt|;
name|expectedPassed
operator|++
expr_stmt|;
name|assertCheckAndMutateMetrics
argument_list|(
name|rs
argument_list|,
name|expectedPassed
argument_list|,
name|expectedFailed
argument_list|)
expr_stmt|;
comment|// checkAndPut failure
name|byte
index|[]
name|val2
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"val2"
argument_list|)
decl_stmt|;
name|table
operator|.
name|checkAndPut
argument_list|(
name|row
argument_list|,
name|family
argument_list|,
name|qualifier
argument_list|,
name|val2
argument_list|,
name|put
argument_list|)
expr_stmt|;
name|expectedFailed
operator|++
expr_stmt|;
name|assertCheckAndMutateMetrics
argument_list|(
name|rs
argument_list|,
name|expectedPassed
argument_list|,
name|expectedFailed
argument_list|)
expr_stmt|;
comment|// checkAndDelete success
name|Delete
name|delete
init|=
operator|new
name|Delete
argument_list|(
name|row
argument_list|)
decl_stmt|;
name|delete
operator|.
name|deleteColumn
argument_list|(
name|family
argument_list|,
name|qualifier
argument_list|)
expr_stmt|;
name|table
operator|.
name|checkAndDelete
argument_list|(
name|row
argument_list|,
name|family
argument_list|,
name|qualifier
argument_list|,
name|val1
argument_list|,
name|delete
argument_list|)
expr_stmt|;
name|expectedPassed
operator|++
expr_stmt|;
name|assertCheckAndMutateMetrics
argument_list|(
name|rs
argument_list|,
name|expectedPassed
argument_list|,
name|expectedFailed
argument_list|)
expr_stmt|;
comment|// checkAndDelete failure
name|table
operator|.
name|checkAndDelete
argument_list|(
name|row
argument_list|,
name|family
argument_list|,
name|qualifier
argument_list|,
name|val1
argument_list|,
name|delete
argument_list|)
expr_stmt|;
name|expectedFailed
operator|++
expr_stmt|;
name|assertCheckAndMutateMetrics
argument_list|(
name|rs
argument_list|,
name|expectedPassed
argument_list|,
name|expectedFailed
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testRemoveRegionMetrics
parameter_list|()
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|String
name|cf
init|=
literal|"REMOVECF"
decl_stmt|;
name|HTable
name|hTable
init|=
name|TEST_UTIL
operator|.
name|createTable
argument_list|(
name|TABLE_NAME
operator|.
name|getBytes
argument_list|()
argument_list|,
name|cf
operator|.
name|getBytes
argument_list|()
argument_list|)
decl_stmt|;
name|HRegionInfo
index|[]
name|regionInfos
init|=
name|hTable
operator|.
name|getRegionLocations
argument_list|()
operator|.
name|keySet
argument_list|()
operator|.
name|toArray
argument_list|(
operator|new
name|HRegionInfo
index|[
name|hTable
operator|.
name|getRegionLocations
argument_list|()
operator|.
name|keySet
argument_list|()
operator|.
name|size
argument_list|()
index|]
argument_list|)
decl_stmt|;
name|String
name|regionName
init|=
name|regionInfos
index|[
literal|0
index|]
operator|.
name|getEncodedName
argument_list|()
decl_stmt|;
comment|// Do some operations so there are metrics.
name|Put
name|pOne
init|=
operator|new
name|Put
argument_list|(
literal|"TEST"
operator|.
name|getBytes
argument_list|()
argument_list|)
decl_stmt|;
name|pOne
operator|.
name|add
argument_list|(
name|cf
operator|.
name|getBytes
argument_list|()
argument_list|,
literal|"test"
operator|.
name|getBytes
argument_list|()
argument_list|,
literal|"test"
operator|.
name|getBytes
argument_list|()
argument_list|)
expr_stmt|;
name|hTable
operator|.
name|put
argument_list|(
name|pOne
argument_list|)
expr_stmt|;
name|Get
name|g
init|=
operator|new
name|Get
argument_list|(
literal|"TEST"
operator|.
name|getBytes
argument_list|()
argument_list|)
decl_stmt|;
name|g
operator|.
name|addFamily
argument_list|(
name|cf
operator|.
name|getBytes
argument_list|()
argument_list|)
expr_stmt|;
name|hTable
operator|.
name|get
argument_list|(
name|g
argument_list|)
expr_stmt|;
name|assertTimeVaryingMetricCount
argument_list|(
literal|1
argument_list|,
name|TABLE_NAME
argument_list|,
name|cf
argument_list|,
name|regionName
argument_list|,
literal|"get_"
argument_list|)
expr_stmt|;
name|HBaseAdmin
name|admin
init|=
name|TEST_UTIL
operator|.
name|getHBaseAdmin
argument_list|()
decl_stmt|;
name|admin
operator|.
name|disableTable
argument_list|(
name|TABLE_NAME
operator|.
name|getBytes
argument_list|()
argument_list|)
expr_stmt|;
name|admin
operator|.
name|deleteTable
argument_list|(
name|TABLE_NAME
operator|.
name|getBytes
argument_list|()
argument_list|)
expr_stmt|;
name|assertTimeVaryingMetricCount
argument_list|(
literal|0
argument_list|,
name|TABLE_NAME
argument_list|,
name|cf
argument_list|,
name|regionName
argument_list|,
literal|"get_"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testMultipleRegions
parameter_list|()
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|TEST_UTIL
operator|.
name|createRandomTable
argument_list|(
name|TABLE_NAME
argument_list|,
name|Arrays
operator|.
name|asList
argument_list|(
name|FAMILIES
argument_list|)
argument_list|,
name|MAX_VERSIONS
argument_list|,
name|NUM_COLS_PER_ROW
argument_list|,
name|NUM_FLUSHES
argument_list|,
name|NUM_REGIONS
argument_list|,
literal|1000
argument_list|)
expr_stmt|;
specifier|final
name|HRegionServer
name|rs
init|=
name|TEST_UTIL
operator|.
name|getMiniHBaseCluster
argument_list|()
operator|.
name|getRegionServer
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|NUM_REGIONS
operator|+
name|META_AND_ROOT
argument_list|,
name|ProtobufUtil
operator|.
name|getOnlineRegions
argument_list|(
name|rs
argument_list|)
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|rs
operator|.
name|doMetrics
argument_list|()
expr_stmt|;
for|for
control|(
name|HRegion
name|r
range|:
name|TEST_UTIL
operator|.
name|getMiniHBaseCluster
argument_list|()
operator|.
name|getRegions
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|TABLE_NAME
argument_list|)
argument_list|)
control|)
block|{
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|byte
index|[]
argument_list|,
name|Store
argument_list|>
name|storeEntry
range|:
name|r
operator|.
name|getStores
argument_list|()
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"For region "
operator|+
name|r
operator|.
name|getRegionNameAsString
argument_list|()
operator|+
literal|", CF "
operator|+
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|storeEntry
operator|.
name|getKey
argument_list|()
argument_list|)
operator|+
literal|" found store files "
operator|+
literal|": "
operator|+
name|storeEntry
operator|.
name|getValue
argument_list|()
operator|.
name|getStorefiles
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
name|assertStoreMetricEquals
argument_list|(
name|NUM_FLUSHES
operator|*
name|NUM_REGIONS
operator|*
name|FAMILIES
operator|.
name|length
operator|+
name|META_AND_ROOT
argument_list|,
name|ALL_METRICS
argument_list|,
name|StoreMetricType
operator|.
name|STORE_FILE_COUNT
argument_list|)
expr_stmt|;
for|for
control|(
name|String
name|cf
range|:
name|FAMILIES
control|)
block|{
name|SchemaMetrics
name|schemaMetrics
init|=
name|SchemaMetrics
operator|.
name|getInstance
argument_list|(
name|TABLE_NAME
argument_list|,
name|cf
argument_list|)
decl_stmt|;
name|assertStoreMetricEquals
argument_list|(
name|NUM_FLUSHES
operator|*
name|NUM_REGIONS
argument_list|,
name|schemaMetrics
argument_list|,
name|StoreMetricType
operator|.
name|STORE_FILE_COUNT
argument_list|)
expr_stmt|;
block|}
comment|// ensure that the max value is also maintained
specifier|final
name|String
name|storeMetricName
init|=
name|ALL_METRICS
operator|.
name|getStoreMetricNameMax
argument_list|(
name|StoreMetricType
operator|.
name|STORE_FILE_COUNT
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"Invalid value for store metric "
operator|+
name|storeMetricName
argument_list|,
name|NUM_FLUSHES
argument_list|,
name|RegionMetricsStorage
operator|.
name|getNumericMetric
argument_list|(
name|storeMetricName
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|org
operator|.
name|junit
operator|.
name|Rule
specifier|public
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|ResourceCheckerJUnitRule
name|cu
init|=
operator|new
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|ResourceCheckerJUnitRule
argument_list|()
decl_stmt|;
specifier|private
name|void
name|assertSizeMetric
parameter_list|(
name|String
name|table
parameter_list|,
name|String
index|[]
name|cfs
parameter_list|,
name|int
index|[]
name|metrics
parameter_list|)
block|{
comment|// we have getsize& nextsize for each column family
name|assertEquals
argument_list|(
name|cfs
operator|.
name|length
operator|*
literal|2
argument_list|,
name|metrics
operator|.
name|length
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
name|cfs
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
name|String
name|prefix
init|=
name|SchemaMetrics
operator|.
name|generateSchemaMetricsPrefix
argument_list|(
name|table
argument_list|,
name|cfs
index|[
name|i
index|]
argument_list|)
decl_stmt|;
name|String
name|getMetric
init|=
name|prefix
operator|+
name|SchemaMetrics
operator|.
name|METRIC_GETSIZE
decl_stmt|;
name|String
name|nextMetric
init|=
name|prefix
operator|+
name|SchemaMetrics
operator|.
name|METRIC_NEXTSIZE
decl_stmt|;
comment|// verify getsize and nextsize matches
name|int
name|getSize
init|=
name|RegionMetricsStorage
operator|.
name|getNumericMetrics
argument_list|()
operator|.
name|containsKey
argument_list|(
name|getMetric
argument_list|)
condition|?
name|RegionMetricsStorage
operator|.
name|getNumericMetrics
argument_list|()
operator|.
name|get
argument_list|(
name|getMetric
argument_list|)
operator|.
name|intValue
argument_list|()
else|:
literal|0
decl_stmt|;
name|int
name|nextSize
init|=
name|RegionMetricsStorage
operator|.
name|getNumericMetrics
argument_list|()
operator|.
name|containsKey
argument_list|(
name|nextMetric
argument_list|)
condition|?
name|RegionMetricsStorage
operator|.
name|getNumericMetrics
argument_list|()
operator|.
name|get
argument_list|(
name|nextMetric
argument_list|)
operator|.
name|intValue
argument_list|()
else|:
literal|0
decl_stmt|;
name|assertEquals
argument_list|(
name|metrics
index|[
name|i
index|]
argument_list|,
name|getSize
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|metrics
index|[
name|cfs
operator|.
name|length
operator|+
name|i
index|]
argument_list|,
name|nextSize
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testGetNextSize
parameter_list|()
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|String
name|rowName
init|=
literal|"row1"
decl_stmt|;
name|byte
index|[]
name|ROW
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|rowName
argument_list|)
decl_stmt|;
name|String
name|tableName
init|=
literal|"SizeMetricTest"
decl_stmt|;
name|byte
index|[]
name|TABLE
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
name|String
name|cf1Name
init|=
literal|"cf1"
decl_stmt|;
name|String
name|cf2Name
init|=
literal|"cf2"
decl_stmt|;
name|String
index|[]
name|cfs
init|=
operator|new
name|String
index|[]
block|{
name|cf1Name
block|,
name|cf2Name
block|}
decl_stmt|;
name|byte
index|[]
name|CF1
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|cf1Name
argument_list|)
decl_stmt|;
name|byte
index|[]
name|CF2
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|cf2Name
argument_list|)
decl_stmt|;
name|long
name|ts
init|=
literal|1234
decl_stmt|;
name|HTable
name|hTable
init|=
name|TEST_UTIL
operator|.
name|createTable
argument_list|(
name|TABLE
argument_list|,
operator|new
name|byte
index|[]
index|[]
block|{
name|CF1
block|,
name|CF2
block|}
argument_list|)
decl_stmt|;
name|HBaseAdmin
name|admin
init|=
operator|new
name|HBaseAdmin
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
name|Put
name|p
init|=
operator|new
name|Put
argument_list|(
name|ROW
argument_list|)
decl_stmt|;
name|p
operator|.
name|add
argument_list|(
name|CF1
argument_list|,
name|CF1
argument_list|,
name|ts
argument_list|,
name|CF1
argument_list|)
expr_stmt|;
name|p
operator|.
name|add
argument_list|(
name|CF2
argument_list|,
name|CF2
argument_list|,
name|ts
argument_list|,
name|CF2
argument_list|)
expr_stmt|;
name|hTable
operator|.
name|put
argument_list|(
name|p
argument_list|)
expr_stmt|;
name|KeyValue
name|kv1
init|=
operator|new
name|KeyValue
argument_list|(
name|ROW
argument_list|,
name|CF1
argument_list|,
name|CF1
argument_list|,
name|ts
argument_list|,
name|CF1
argument_list|)
decl_stmt|;
name|KeyValue
name|kv2
init|=
operator|new
name|KeyValue
argument_list|(
name|ROW
argument_list|,
name|CF2
argument_list|,
name|CF2
argument_list|,
name|ts
argument_list|,
name|CF2
argument_list|)
decl_stmt|;
name|int
name|kvLength
init|=
name|kv1
operator|.
name|getLength
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
name|kvLength
argument_list|,
name|kv2
operator|.
name|getLength
argument_list|()
argument_list|)
expr_stmt|;
comment|// only cf1.getsize is set on Get
name|hTable
operator|.
name|get
argument_list|(
operator|new
name|Get
argument_list|(
name|ROW
argument_list|)
operator|.
name|addFamily
argument_list|(
name|CF1
argument_list|)
argument_list|)
expr_stmt|;
name|assertSizeMetric
argument_list|(
name|tableName
argument_list|,
name|cfs
argument_list|,
operator|new
name|int
index|[]
block|{
name|kvLength
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|}
argument_list|)
expr_stmt|;
comment|// only cf2.getsize is set on Get
name|hTable
operator|.
name|get
argument_list|(
operator|new
name|Get
argument_list|(
name|ROW
argument_list|)
operator|.
name|addFamily
argument_list|(
name|CF2
argument_list|)
argument_list|)
expr_stmt|;
name|assertSizeMetric
argument_list|(
name|tableName
argument_list|,
name|cfs
argument_list|,
operator|new
name|int
index|[]
block|{
name|kvLength
block|,
name|kvLength
block|,
literal|0
block|,
literal|0
block|}
argument_list|)
expr_stmt|;
comment|// only cf2.nextsize is set
for|for
control|(
name|Result
name|res
range|:
name|hTable
operator|.
name|getScanner
argument_list|(
name|CF2
argument_list|)
control|)
block|{     }
name|assertSizeMetric
argument_list|(
name|tableName
argument_list|,
name|cfs
argument_list|,
operator|new
name|int
index|[]
block|{
name|kvLength
block|,
name|kvLength
block|,
literal|0
block|,
name|kvLength
block|}
argument_list|)
expr_stmt|;
comment|// only cf2.nextsize is set
for|for
control|(
name|Result
name|res
range|:
name|hTable
operator|.
name|getScanner
argument_list|(
name|CF1
argument_list|)
control|)
block|{     }
name|assertSizeMetric
argument_list|(
name|tableName
argument_list|,
name|cfs
argument_list|,
operator|new
name|int
index|[]
block|{
name|kvLength
block|,
name|kvLength
block|,
name|kvLength
block|,
name|kvLength
block|}
argument_list|)
expr_stmt|;
comment|// getsize/nextsize should not be set on flush or compaction
for|for
control|(
name|HRegion
name|hr
range|:
name|TEST_UTIL
operator|.
name|getMiniHBaseCluster
argument_list|()
operator|.
name|getRegions
argument_list|(
name|TABLE
argument_list|)
control|)
block|{
name|hr
operator|.
name|flushcache
argument_list|()
expr_stmt|;
name|hr
operator|.
name|compactStores
argument_list|()
expr_stmt|;
block|}
name|assertSizeMetric
argument_list|(
name|tableName
argument_list|,
name|cfs
argument_list|,
operator|new
name|int
index|[]
block|{
name|kvLength
block|,
name|kvLength
block|,
name|kvLength
block|,
name|kvLength
block|}
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

