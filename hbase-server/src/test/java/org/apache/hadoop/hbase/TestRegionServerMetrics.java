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
package|;
end_package

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
name|Admin
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
name|Increment
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
name|RowMutations
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
name|filter
operator|.
name|BinaryComparator
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
name|filter
operator|.
name|CompareFilter
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
name|filter
operator|.
name|RowFilter
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
name|filter
operator|.
name|SingleColumnValueFilter
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
name|Collection
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
name|HBaseTestingUtility
name|TEST_UTIL
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|TableName
name|TABLE_NAME
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"test"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|CF1
init|=
literal|"c1"
operator|.
name|getBytes
argument_list|()
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|CF2
init|=
literal|"c2"
operator|.
name|getBytes
argument_list|()
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|ROW1
init|=
literal|"a"
operator|.
name|getBytes
argument_list|()
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|ROW2
init|=
literal|"b"
operator|.
name|getBytes
argument_list|()
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|ROW3
init|=
literal|"c"
operator|.
name|getBytes
argument_list|()
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|COL1
init|=
literal|"q1"
operator|.
name|getBytes
argument_list|()
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|COL2
init|=
literal|"q2"
operator|.
name|getBytes
argument_list|()
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|COL3
init|=
literal|"q3"
operator|.
name|getBytes
argument_list|()
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|VAL1
init|=
literal|"v1"
operator|.
name|getBytes
argument_list|()
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|VAL2
init|=
literal|"v2"
operator|.
name|getBytes
argument_list|()
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|VAL3
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|0L
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|MAX_TRY
init|=
literal|20
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|SLEEP_MS
init|=
literal|100
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|TTL
init|=
literal|1
decl_stmt|;
specifier|private
specifier|static
name|Admin
name|admin
decl_stmt|;
specifier|private
specifier|static
name|Collection
argument_list|<
name|ServerName
argument_list|>
name|serverNames
decl_stmt|;
specifier|private
specifier|static
name|Table
name|table
decl_stmt|;
specifier|private
specifier|static
name|List
argument_list|<
name|HRegionInfo
argument_list|>
name|tableRegions
decl_stmt|;
specifier|private
specifier|static
name|Map
argument_list|<
name|Metric
argument_list|,
name|Long
argument_list|>
name|requestsMap
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
specifier|private
specifier|static
name|Map
argument_list|<
name|Metric
argument_list|,
name|Long
argument_list|>
name|requestsMapPrev
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|setUpOnce
parameter_list|()
throws|throws
name|Exception
block|{
name|TEST_UTIL
operator|.
name|startMiniCluster
argument_list|()
expr_stmt|;
name|admin
operator|=
name|TEST_UTIL
operator|.
name|getAdmin
argument_list|()
expr_stmt|;
name|serverNames
operator|=
name|admin
operator|.
name|getClusterStatus
argument_list|()
operator|.
name|getServers
argument_list|()
expr_stmt|;
name|table
operator|=
name|createTable
argument_list|()
expr_stmt|;
name|putData
argument_list|()
expr_stmt|;
name|tableRegions
operator|=
name|admin
operator|.
name|getTableRegions
argument_list|(
name|TABLE_NAME
argument_list|)
expr_stmt|;
for|for
control|(
name|Metric
name|metric
range|:
name|Metric
operator|.
name|values
argument_list|()
control|)
block|{
name|requestsMap
operator|.
name|put
argument_list|(
name|metric
argument_list|,
literal|0L
argument_list|)
expr_stmt|;
name|requestsMapPrev
operator|.
name|put
argument_list|(
name|metric
argument_list|,
literal|0L
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
specifier|static
name|Table
name|createTable
parameter_list|()
throws|throws
name|IOException
block|{
name|HTableDescriptor
name|td
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|TABLE_NAME
argument_list|)
decl_stmt|;
name|HColumnDescriptor
name|cd1
init|=
operator|new
name|HColumnDescriptor
argument_list|(
name|CF1
argument_list|)
decl_stmt|;
name|td
operator|.
name|addFamily
argument_list|(
name|cd1
argument_list|)
expr_stmt|;
name|HColumnDescriptor
name|cd2
init|=
operator|new
name|HColumnDescriptor
argument_list|(
name|CF2
argument_list|)
decl_stmt|;
name|cd2
operator|.
name|setTimeToLive
argument_list|(
name|TTL
argument_list|)
expr_stmt|;
name|td
operator|.
name|addFamily
argument_list|(
name|cd2
argument_list|)
expr_stmt|;
name|admin
operator|.
name|createTable
argument_list|(
name|td
argument_list|)
expr_stmt|;
return|return
name|TEST_UTIL
operator|.
name|getConnection
argument_list|()
operator|.
name|getTable
argument_list|(
name|TABLE_NAME
argument_list|)
return|;
block|}
specifier|private
specifier|static
name|void
name|testReadRequests
parameter_list|(
name|long
name|resultCount
parameter_list|,
name|long
name|expectedReadRequests
parameter_list|,
name|long
name|expectedFilteredReadRequests
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|updateMetricsMap
argument_list|()
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"requestsMapPrev = "
operator|+
name|requestsMapPrev
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"requestsMap = "
operator|+
name|requestsMap
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|expectedReadRequests
argument_list|,
name|requestsMap
operator|.
name|get
argument_list|(
name|Metric
operator|.
name|REGION_READ
argument_list|)
operator|-
name|requestsMapPrev
operator|.
name|get
argument_list|(
name|Metric
operator|.
name|REGION_READ
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|expectedReadRequests
argument_list|,
name|requestsMap
operator|.
name|get
argument_list|(
name|Metric
operator|.
name|SERVER_READ
argument_list|)
operator|-
name|requestsMapPrev
operator|.
name|get
argument_list|(
name|Metric
operator|.
name|SERVER_READ
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|expectedFilteredReadRequests
argument_list|,
name|requestsMap
operator|.
name|get
argument_list|(
name|Metric
operator|.
name|FILTERED_REGION_READ
argument_list|)
operator|-
name|requestsMapPrev
operator|.
name|get
argument_list|(
name|Metric
operator|.
name|FILTERED_REGION_READ
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|expectedFilteredReadRequests
argument_list|,
name|requestsMap
operator|.
name|get
argument_list|(
name|Metric
operator|.
name|FILTERED_SERVER_READ
argument_list|)
operator|-
name|requestsMapPrev
operator|.
name|get
argument_list|(
name|Metric
operator|.
name|FILTERED_SERVER_READ
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|expectedReadRequests
argument_list|,
name|resultCount
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|static
name|void
name|updateMetricsMap
parameter_list|()
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
for|for
control|(
name|Metric
name|metric
range|:
name|Metric
operator|.
name|values
argument_list|()
control|)
block|{
name|requestsMapPrev
operator|.
name|put
argument_list|(
name|metric
argument_list|,
name|requestsMap
operator|.
name|get
argument_list|(
name|metric
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|ServerLoad
name|serverLoad
init|=
literal|null
decl_stmt|;
name|RegionLoad
name|regionLoadOuter
init|=
literal|null
decl_stmt|;
name|boolean
name|metricsUpdated
init|=
literal|false
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
name|MAX_TRY
condition|;
name|i
operator|++
control|)
block|{
for|for
control|(
name|ServerName
name|serverName
range|:
name|serverNames
control|)
block|{
name|serverLoad
operator|=
name|admin
operator|.
name|getClusterStatus
argument_list|()
operator|.
name|getLoad
argument_list|(
name|serverName
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|byte
index|[]
argument_list|,
name|RegionLoad
argument_list|>
name|regionsLoad
init|=
name|serverLoad
operator|.
name|getRegionsLoad
argument_list|()
decl_stmt|;
for|for
control|(
name|HRegionInfo
name|tableRegion
range|:
name|tableRegions
control|)
block|{
name|RegionLoad
name|regionLoad
init|=
name|regionsLoad
operator|.
name|get
argument_list|(
name|tableRegion
operator|.
name|getRegionName
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|regionLoad
operator|!=
literal|null
condition|)
block|{
name|regionLoadOuter
operator|=
name|regionLoad
expr_stmt|;
for|for
control|(
name|Metric
name|metric
range|:
name|Metric
operator|.
name|values
argument_list|()
control|)
block|{
if|if
condition|(
name|getReadRequest
argument_list|(
name|serverLoad
argument_list|,
name|regionLoad
argument_list|,
name|metric
argument_list|)
operator|>
name|requestsMapPrev
operator|.
name|get
argument_list|(
name|metric
argument_list|)
condition|)
block|{
for|for
control|(
name|Metric
name|metricInner
range|:
name|Metric
operator|.
name|values
argument_list|()
control|)
block|{
name|requestsMap
operator|.
name|put
argument_list|(
name|metricInner
argument_list|,
name|getReadRequest
argument_list|(
name|serverLoad
argument_list|,
name|regionLoad
argument_list|,
name|metricInner
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|metricsUpdated
operator|=
literal|true
expr_stmt|;
break|break;
block|}
block|}
block|}
block|}
block|}
if|if
condition|(
name|metricsUpdated
condition|)
block|{
break|break;
block|}
name|Thread
operator|.
name|sleep
argument_list|(
name|SLEEP_MS
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|metricsUpdated
condition|)
block|{
for|for
control|(
name|Metric
name|metric
range|:
name|Metric
operator|.
name|values
argument_list|()
control|)
block|{
name|requestsMap
operator|.
name|put
argument_list|(
name|metric
argument_list|,
name|getReadRequest
argument_list|(
name|serverLoad
argument_list|,
name|regionLoadOuter
argument_list|,
name|metric
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|private
specifier|static
name|long
name|getReadRequest
parameter_list|(
name|ServerLoad
name|serverLoad
parameter_list|,
name|RegionLoad
name|regionLoad
parameter_list|,
name|Metric
name|metric
parameter_list|)
block|{
switch|switch
condition|(
name|metric
condition|)
block|{
case|case
name|REGION_READ
case|:
return|return
name|regionLoad
operator|.
name|getReadRequestsCount
argument_list|()
return|;
case|case
name|SERVER_READ
case|:
return|return
name|serverLoad
operator|.
name|getReadRequestsCount
argument_list|()
return|;
case|case
name|FILTERED_REGION_READ
case|:
return|return
name|regionLoad
operator|.
name|getFilteredReadRequestsCount
argument_list|()
return|;
case|case
name|FILTERED_SERVER_READ
case|:
return|return
name|serverLoad
operator|.
name|getFilteredReadRequestsCount
argument_list|()
return|;
default|default:
throw|throw
operator|new
name|IllegalStateException
argument_list|()
throw|;
block|}
block|}
specifier|private
specifier|static
name|void
name|putData
parameter_list|()
throws|throws
name|IOException
block|{
name|Put
name|put
decl_stmt|;
name|put
operator|=
operator|new
name|Put
argument_list|(
name|ROW1
argument_list|)
expr_stmt|;
name|put
operator|.
name|addColumn
argument_list|(
name|CF1
argument_list|,
name|COL1
argument_list|,
name|VAL1
argument_list|)
expr_stmt|;
name|put
operator|.
name|addColumn
argument_list|(
name|CF1
argument_list|,
name|COL2
argument_list|,
name|VAL2
argument_list|)
expr_stmt|;
name|put
operator|.
name|addColumn
argument_list|(
name|CF1
argument_list|,
name|COL3
argument_list|,
name|VAL3
argument_list|)
expr_stmt|;
name|table
operator|.
name|put
argument_list|(
name|put
argument_list|)
expr_stmt|;
name|put
operator|=
operator|new
name|Put
argument_list|(
name|ROW2
argument_list|)
expr_stmt|;
name|put
operator|.
name|addColumn
argument_list|(
name|CF1
argument_list|,
name|COL1
argument_list|,
name|VAL2
argument_list|)
expr_stmt|;
comment|// put val2 instead of val1
name|put
operator|.
name|addColumn
argument_list|(
name|CF1
argument_list|,
name|COL2
argument_list|,
name|VAL2
argument_list|)
expr_stmt|;
name|table
operator|.
name|put
argument_list|(
name|put
argument_list|)
expr_stmt|;
name|put
operator|=
operator|new
name|Put
argument_list|(
name|ROW3
argument_list|)
expr_stmt|;
name|put
operator|.
name|addColumn
argument_list|(
name|CF1
argument_list|,
name|COL1
argument_list|,
name|VAL1
argument_list|)
expr_stmt|;
name|put
operator|.
name|addColumn
argument_list|(
name|CF1
argument_list|,
name|COL2
argument_list|,
name|VAL2
argument_list|)
expr_stmt|;
name|table
operator|.
name|put
argument_list|(
name|put
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|static
name|void
name|putTTLExpiredData
parameter_list|()
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|Put
name|put
decl_stmt|;
name|put
operator|=
operator|new
name|Put
argument_list|(
name|ROW1
argument_list|)
expr_stmt|;
name|put
operator|.
name|addColumn
argument_list|(
name|CF2
argument_list|,
name|COL1
argument_list|,
name|VAL1
argument_list|)
expr_stmt|;
name|put
operator|.
name|addColumn
argument_list|(
name|CF2
argument_list|,
name|COL2
argument_list|,
name|VAL2
argument_list|)
expr_stmt|;
name|table
operator|.
name|put
argument_list|(
name|put
argument_list|)
expr_stmt|;
name|Thread
operator|.
name|sleep
argument_list|(
name|TTL
operator|*
literal|1000
argument_list|)
expr_stmt|;
name|put
operator|=
operator|new
name|Put
argument_list|(
name|ROW2
argument_list|)
expr_stmt|;
name|put
operator|.
name|addColumn
argument_list|(
name|CF2
argument_list|,
name|COL1
argument_list|,
name|VAL1
argument_list|)
expr_stmt|;
name|put
operator|.
name|addColumn
argument_list|(
name|CF2
argument_list|,
name|COL2
argument_list|,
name|VAL2
argument_list|)
expr_stmt|;
name|table
operator|.
name|put
argument_list|(
name|put
argument_list|)
expr_stmt|;
name|put
operator|=
operator|new
name|Put
argument_list|(
name|ROW3
argument_list|)
expr_stmt|;
name|put
operator|.
name|addColumn
argument_list|(
name|CF2
argument_list|,
name|COL1
argument_list|,
name|VAL1
argument_list|)
expr_stmt|;
name|put
operator|.
name|addColumn
argument_list|(
name|CF2
argument_list|,
name|COL2
argument_list|,
name|VAL2
argument_list|)
expr_stmt|;
name|table
operator|.
name|put
argument_list|(
name|put
argument_list|)
expr_stmt|;
block|}
annotation|@
name|AfterClass
specifier|public
specifier|static
name|void
name|tearDownOnce
parameter_list|()
throws|throws
name|Exception
block|{
name|TEST_UTIL
operator|.
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testReadRequestsCountNotFiltered
parameter_list|()
throws|throws
name|Exception
block|{
name|int
name|resultCount
decl_stmt|;
name|Scan
name|scan
decl_stmt|;
name|Append
name|append
decl_stmt|;
name|Put
name|put
decl_stmt|;
name|Increment
name|increment
decl_stmt|;
name|Get
name|get
decl_stmt|;
comment|// test for scan
name|scan
operator|=
operator|new
name|Scan
argument_list|()
expr_stmt|;
try|try
init|(
name|ResultScanner
name|scanner
init|=
name|table
operator|.
name|getScanner
argument_list|(
name|scan
argument_list|)
init|)
block|{
name|resultCount
operator|=
literal|0
expr_stmt|;
for|for
control|(
name|Result
name|ignore
range|:
name|scanner
control|)
block|{
name|resultCount
operator|++
expr_stmt|;
block|}
name|testReadRequests
argument_list|(
name|resultCount
argument_list|,
literal|3
argument_list|,
literal|0
argument_list|)
expr_stmt|;
block|}
comment|// test for scan
name|scan
operator|=
operator|new
name|Scan
argument_list|(
name|ROW2
argument_list|,
name|ROW3
argument_list|)
expr_stmt|;
try|try
init|(
name|ResultScanner
name|scanner
init|=
name|table
operator|.
name|getScanner
argument_list|(
name|scan
argument_list|)
init|)
block|{
name|resultCount
operator|=
literal|0
expr_stmt|;
for|for
control|(
name|Result
name|ignore
range|:
name|scanner
control|)
block|{
name|resultCount
operator|++
expr_stmt|;
block|}
name|testReadRequests
argument_list|(
name|resultCount
argument_list|,
literal|1
argument_list|,
literal|0
argument_list|)
expr_stmt|;
block|}
comment|// test for get
name|get
operator|=
operator|new
name|Get
argument_list|(
name|ROW2
argument_list|)
expr_stmt|;
name|Result
name|result
init|=
name|table
operator|.
name|get
argument_list|(
name|get
argument_list|)
decl_stmt|;
name|resultCount
operator|=
name|result
operator|.
name|isEmpty
argument_list|()
condition|?
literal|0
else|:
literal|1
expr_stmt|;
name|testReadRequests
argument_list|(
name|resultCount
argument_list|,
literal|1
argument_list|,
literal|0
argument_list|)
expr_stmt|;
comment|// test for increment
name|increment
operator|=
operator|new
name|Increment
argument_list|(
name|ROW1
argument_list|)
expr_stmt|;
name|increment
operator|.
name|addColumn
argument_list|(
name|CF1
argument_list|,
name|COL3
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|result
operator|=
name|table
operator|.
name|increment
argument_list|(
name|increment
argument_list|)
expr_stmt|;
name|resultCount
operator|=
name|result
operator|.
name|isEmpty
argument_list|()
condition|?
literal|0
else|:
literal|1
expr_stmt|;
name|testReadRequests
argument_list|(
name|resultCount
argument_list|,
literal|1
argument_list|,
literal|0
argument_list|)
expr_stmt|;
comment|// test for checkAndPut
name|put
operator|=
operator|new
name|Put
argument_list|(
name|ROW1
argument_list|)
expr_stmt|;
name|put
operator|.
name|addColumn
argument_list|(
name|CF1
argument_list|,
name|COL2
argument_list|,
name|VAL2
argument_list|)
expr_stmt|;
name|boolean
name|checkAndPut
init|=
name|table
operator|.
name|checkAndPut
argument_list|(
name|ROW1
argument_list|,
name|CF1
argument_list|,
name|COL2
argument_list|,
name|CompareFilter
operator|.
name|CompareOp
operator|.
name|EQUAL
argument_list|,
name|VAL2
argument_list|,
name|put
argument_list|)
decl_stmt|;
name|resultCount
operator|=
name|checkAndPut
condition|?
literal|1
else|:
literal|0
expr_stmt|;
name|testReadRequests
argument_list|(
name|resultCount
argument_list|,
literal|1
argument_list|,
literal|0
argument_list|)
expr_stmt|;
comment|// test for append
name|append
operator|=
operator|new
name|Append
argument_list|(
name|ROW1
argument_list|)
expr_stmt|;
name|append
operator|.
name|add
argument_list|(
name|CF1
argument_list|,
name|COL2
argument_list|,
name|VAL2
argument_list|)
expr_stmt|;
name|result
operator|=
name|table
operator|.
name|append
argument_list|(
name|append
argument_list|)
expr_stmt|;
name|resultCount
operator|=
name|result
operator|.
name|isEmpty
argument_list|()
condition|?
literal|0
else|:
literal|1
expr_stmt|;
name|testReadRequests
argument_list|(
name|resultCount
argument_list|,
literal|1
argument_list|,
literal|0
argument_list|)
expr_stmt|;
comment|// test for checkAndMutate
name|put
operator|=
operator|new
name|Put
argument_list|(
name|ROW1
argument_list|)
expr_stmt|;
name|put
operator|.
name|addColumn
argument_list|(
name|CF1
argument_list|,
name|COL1
argument_list|,
name|VAL1
argument_list|)
expr_stmt|;
name|RowMutations
name|rm
init|=
operator|new
name|RowMutations
argument_list|(
name|ROW1
argument_list|)
decl_stmt|;
name|rm
operator|.
name|add
argument_list|(
name|put
argument_list|)
expr_stmt|;
name|boolean
name|checkAndMutate
init|=
name|table
operator|.
name|checkAndMutate
argument_list|(
name|ROW1
argument_list|,
name|CF1
argument_list|,
name|COL1
argument_list|,
name|CompareFilter
operator|.
name|CompareOp
operator|.
name|EQUAL
argument_list|,
name|VAL1
argument_list|,
name|rm
argument_list|)
decl_stmt|;
name|resultCount
operator|=
name|checkAndMutate
condition|?
literal|1
else|:
literal|0
expr_stmt|;
name|testReadRequests
argument_list|(
name|resultCount
argument_list|,
literal|1
argument_list|,
literal|0
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testReadRequestsCountWithFilter
parameter_list|()
throws|throws
name|Exception
block|{
name|int
name|resultCount
decl_stmt|;
name|Scan
name|scan
decl_stmt|;
comment|// test for scan
name|scan
operator|=
operator|new
name|Scan
argument_list|()
expr_stmt|;
name|scan
operator|.
name|setFilter
argument_list|(
operator|new
name|SingleColumnValueFilter
argument_list|(
name|CF1
argument_list|,
name|COL1
argument_list|,
name|CompareFilter
operator|.
name|CompareOp
operator|.
name|EQUAL
argument_list|,
name|VAL1
argument_list|)
argument_list|)
expr_stmt|;
try|try
init|(
name|ResultScanner
name|scanner
init|=
name|table
operator|.
name|getScanner
argument_list|(
name|scan
argument_list|)
init|)
block|{
name|resultCount
operator|=
literal|0
expr_stmt|;
for|for
control|(
name|Result
name|ignore
range|:
name|scanner
control|)
block|{
name|resultCount
operator|++
expr_stmt|;
block|}
name|testReadRequests
argument_list|(
name|resultCount
argument_list|,
literal|2
argument_list|,
literal|1
argument_list|)
expr_stmt|;
block|}
comment|// test for scan
name|scan
operator|=
operator|new
name|Scan
argument_list|()
expr_stmt|;
name|scan
operator|.
name|setFilter
argument_list|(
operator|new
name|RowFilter
argument_list|(
name|CompareFilter
operator|.
name|CompareOp
operator|.
name|EQUAL
argument_list|,
operator|new
name|BinaryComparator
argument_list|(
name|ROW1
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
try|try
init|(
name|ResultScanner
name|scanner
init|=
name|table
operator|.
name|getScanner
argument_list|(
name|scan
argument_list|)
init|)
block|{
name|resultCount
operator|=
literal|0
expr_stmt|;
for|for
control|(
name|Result
name|ignore
range|:
name|scanner
control|)
block|{
name|resultCount
operator|++
expr_stmt|;
block|}
name|testReadRequests
argument_list|(
name|resultCount
argument_list|,
literal|1
argument_list|,
literal|2
argument_list|)
expr_stmt|;
block|}
comment|// test for scan
name|scan
operator|=
operator|new
name|Scan
argument_list|(
name|ROW2
argument_list|,
name|ROW3
argument_list|)
expr_stmt|;
name|scan
operator|.
name|setFilter
argument_list|(
operator|new
name|RowFilter
argument_list|(
name|CompareFilter
operator|.
name|CompareOp
operator|.
name|EQUAL
argument_list|,
operator|new
name|BinaryComparator
argument_list|(
name|ROW1
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
try|try
init|(
name|ResultScanner
name|scanner
init|=
name|table
operator|.
name|getScanner
argument_list|(
name|scan
argument_list|)
init|)
block|{
name|resultCount
operator|=
literal|0
expr_stmt|;
for|for
control|(
name|Result
name|ignore
range|:
name|scanner
control|)
block|{
name|resultCount
operator|++
expr_stmt|;
block|}
name|testReadRequests
argument_list|(
name|resultCount
argument_list|,
literal|0
argument_list|,
literal|1
argument_list|)
expr_stmt|;
block|}
comment|// fixme filtered get should not increase readRequestsCount
comment|//    Get get = new Get(ROW2);
comment|//    get.setFilter(new SingleColumnValueFilter(CF1, COL1, CompareFilter.CompareOp.EQUAL, VAL1));
comment|//    Result result = table.get(get);
comment|//    resultCount = result.isEmpty() ? 0 : 1;
comment|//    testReadRequests(resultCount, 0, 1);
block|}
annotation|@
name|Test
specifier|public
name|void
name|testReadRequestsCountWithDeletedRow
parameter_list|()
throws|throws
name|Exception
block|{
try|try
block|{
name|Delete
name|delete
init|=
operator|new
name|Delete
argument_list|(
name|ROW3
argument_list|)
decl_stmt|;
name|table
operator|.
name|delete
argument_list|(
name|delete
argument_list|)
expr_stmt|;
name|Scan
name|scan
init|=
operator|new
name|Scan
argument_list|()
decl_stmt|;
try|try
init|(
name|ResultScanner
name|scanner
init|=
name|table
operator|.
name|getScanner
argument_list|(
name|scan
argument_list|)
init|)
block|{
name|int
name|resultCount
init|=
literal|0
decl_stmt|;
for|for
control|(
name|Result
name|ignore
range|:
name|scanner
control|)
block|{
name|resultCount
operator|++
expr_stmt|;
block|}
name|testReadRequests
argument_list|(
name|resultCount
argument_list|,
literal|2
argument_list|,
literal|1
argument_list|)
expr_stmt|;
block|}
block|}
finally|finally
block|{
name|Put
name|put
init|=
operator|new
name|Put
argument_list|(
name|ROW3
argument_list|)
decl_stmt|;
name|put
operator|.
name|addColumn
argument_list|(
name|CF1
argument_list|,
name|COL1
argument_list|,
name|VAL1
argument_list|)
expr_stmt|;
name|put
operator|.
name|addColumn
argument_list|(
name|CF1
argument_list|,
name|COL2
argument_list|,
name|VAL2
argument_list|)
expr_stmt|;
name|table
operator|.
name|put
argument_list|(
name|put
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testReadRequestsCountWithTTLExpiration
parameter_list|()
throws|throws
name|Exception
block|{
name|putTTLExpiredData
argument_list|()
expr_stmt|;
name|Scan
name|scan
init|=
operator|new
name|Scan
argument_list|()
decl_stmt|;
name|scan
operator|.
name|addFamily
argument_list|(
name|CF2
argument_list|)
expr_stmt|;
try|try
init|(
name|ResultScanner
name|scanner
init|=
name|table
operator|.
name|getScanner
argument_list|(
name|scan
argument_list|)
init|)
block|{
name|int
name|resultCount
init|=
literal|0
decl_stmt|;
for|for
control|(
name|Result
name|ignore
range|:
name|scanner
control|)
block|{
name|resultCount
operator|++
expr_stmt|;
block|}
name|testReadRequests
argument_list|(
name|resultCount
argument_list|,
literal|2
argument_list|,
literal|1
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
enum|enum
name|Metric
block|{
name|REGION_READ
block|,
name|SERVER_READ
block|,
name|FILTERED_REGION_READ
block|,
name|FILTERED_SERVER_READ
block|}
block|}
end_class

end_unit

