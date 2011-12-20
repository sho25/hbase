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
specifier|static
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
name|HRegion
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
name|rs
operator|.
name|getOnlineRegions
argument_list|()
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
name|HRegion
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
block|}
end_class

end_unit

