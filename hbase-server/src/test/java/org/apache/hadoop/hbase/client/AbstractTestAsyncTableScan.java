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
name|io
operator|.
name|IOException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|UncheckedIOException
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
name|List
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|stream
operator|.
name|Collectors
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|stream
operator|.
name|IntStream
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

begin_class
specifier|public
specifier|abstract
class|class
name|AbstractTestAsyncTableScan
block|{
specifier|protected
specifier|static
specifier|final
name|HBaseTestingUtility
name|TEST_UTIL
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
specifier|protected
specifier|static
name|TableName
name|TABLE_NAME
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"async"
argument_list|)
decl_stmt|;
specifier|protected
specifier|static
name|byte
index|[]
name|FAMILY
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"cf"
argument_list|)
decl_stmt|;
specifier|protected
specifier|static
name|byte
index|[]
name|CQ1
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"cq1"
argument_list|)
decl_stmt|;
specifier|protected
specifier|static
name|byte
index|[]
name|CQ2
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"cq2"
argument_list|)
decl_stmt|;
specifier|protected
specifier|static
name|int
name|COUNT
init|=
literal|1000
decl_stmt|;
specifier|protected
specifier|static
name|AsyncConnection
name|ASYNC_CONN
decl_stmt|;
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|setUp
parameter_list|()
throws|throws
name|Exception
block|{
name|TEST_UTIL
operator|.
name|startMiniCluster
argument_list|(
literal|3
argument_list|)
expr_stmt|;
name|byte
index|[]
index|[]
name|splitKeys
init|=
operator|new
name|byte
index|[
literal|8
index|]
index|[]
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|111
init|;
name|i
operator|<
literal|999
condition|;
name|i
operator|+=
literal|111
control|)
block|{
name|splitKeys
index|[
name|i
operator|/
literal|111
operator|-
literal|1
index|]
operator|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"%03d"
argument_list|,
name|i
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|TEST_UTIL
operator|.
name|createTable
argument_list|(
name|TABLE_NAME
argument_list|,
name|FAMILY
argument_list|,
name|splitKeys
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|waitTableAvailable
argument_list|(
name|TABLE_NAME
argument_list|)
expr_stmt|;
name|ASYNC_CONN
operator|=
name|ConnectionFactory
operator|.
name|createAsyncConnection
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
expr_stmt|;
name|ASYNC_CONN
operator|.
name|getRawTable
argument_list|(
name|TABLE_NAME
argument_list|)
operator|.
name|putAll
argument_list|(
name|IntStream
operator|.
name|range
argument_list|(
literal|0
argument_list|,
name|COUNT
argument_list|)
operator|.
name|mapToObj
argument_list|(
name|i
lambda|->
operator|new
name|Put
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"%03d"
argument_list|,
name|i
argument_list|)
argument_list|)
argument_list|)
operator|.
name|addColumn
argument_list|(
name|FAMILY
argument_list|,
name|CQ1
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|i
argument_list|)
argument_list|)
operator|.
name|addColumn
argument_list|(
name|FAMILY
argument_list|,
name|CQ2
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|i
operator|*
name|i
argument_list|)
argument_list|)
argument_list|)
operator|.
name|collect
argument_list|(
name|Collectors
operator|.
name|toList
argument_list|()
argument_list|)
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
block|}
annotation|@
name|AfterClass
specifier|public
specifier|static
name|void
name|tearDown
parameter_list|()
throws|throws
name|Exception
block|{
name|ASYNC_CONN
operator|.
name|close
argument_list|()
expr_stmt|;
name|TEST_UTIL
operator|.
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
block|}
specifier|protected
specifier|abstract
name|Scan
name|createScan
parameter_list|()
function_decl|;
specifier|protected
specifier|abstract
name|List
argument_list|<
name|Result
argument_list|>
name|doScan
parameter_list|(
name|Scan
name|scan
parameter_list|)
throws|throws
name|Exception
function_decl|;
specifier|private
name|Result
name|convertToPartial
parameter_list|(
name|Result
name|result
parameter_list|)
block|{
return|return
name|Result
operator|.
name|create
argument_list|(
name|result
operator|.
name|rawCells
argument_list|()
argument_list|,
name|result
operator|.
name|getExists
argument_list|()
argument_list|,
name|result
operator|.
name|isStale
argument_list|()
argument_list|,
literal|true
argument_list|)
return|;
block|}
specifier|protected
specifier|final
name|List
argument_list|<
name|Result
argument_list|>
name|convertFromBatchResult
parameter_list|(
name|List
argument_list|<
name|Result
argument_list|>
name|results
parameter_list|)
block|{
name|assertTrue
argument_list|(
name|results
operator|.
name|size
argument_list|()
operator|%
literal|2
operator|==
literal|0
argument_list|)
expr_stmt|;
return|return
name|IntStream
operator|.
name|range
argument_list|(
literal|0
argument_list|,
name|results
operator|.
name|size
argument_list|()
operator|/
literal|2
argument_list|)
operator|.
name|mapToObj
argument_list|(
name|i
lambda|->
block|{
try|try
block|{
return|return
name|Result
operator|.
name|createCompleteResult
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|convertToPartial
argument_list|(
name|results
operator|.
name|get
argument_list|(
literal|2
operator|*
name|i
argument_list|)
argument_list|)
argument_list|,
name|convertToPartial
argument_list|(
name|results
operator|.
name|get
argument_list|(
literal|2
operator|*
name|i
operator|+
literal|1
argument_list|)
argument_list|)
argument_list|)
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|UncheckedIOException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
argument_list|)
operator|.
name|collect
argument_list|(
name|Collectors
operator|.
name|toList
argument_list|()
argument_list|)
return|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testScanAll
parameter_list|()
throws|throws
name|Exception
block|{
name|List
argument_list|<
name|Result
argument_list|>
name|results
init|=
name|doScan
argument_list|(
name|createScan
argument_list|()
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|COUNT
argument_list|,
name|results
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|IntStream
operator|.
name|range
argument_list|(
literal|0
argument_list|,
name|COUNT
argument_list|)
operator|.
name|forEach
argument_list|(
name|i
lambda|->
block|{
name|Result
name|result
init|=
name|results
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"%03d"
argument_list|,
name|i
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toString
argument_list|(
name|result
operator|.
name|getRow
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|i
argument_list|,
name|Bytes
operator|.
name|toInt
argument_list|(
name|result
operator|.
name|getValue
argument_list|(
name|FAMILY
argument_list|,
name|CQ1
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|assertResultEquals
parameter_list|(
name|Result
name|result
parameter_list|,
name|int
name|i
parameter_list|)
block|{
name|assertEquals
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"%03d"
argument_list|,
name|i
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toString
argument_list|(
name|result
operator|.
name|getRow
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|i
argument_list|,
name|Bytes
operator|.
name|toInt
argument_list|(
name|result
operator|.
name|getValue
argument_list|(
name|FAMILY
argument_list|,
name|CQ1
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|i
operator|*
name|i
argument_list|,
name|Bytes
operator|.
name|toInt
argument_list|(
name|result
operator|.
name|getValue
argument_list|(
name|FAMILY
argument_list|,
name|CQ2
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testReversedScanAll
parameter_list|()
throws|throws
name|Exception
block|{
name|List
argument_list|<
name|Result
argument_list|>
name|results
init|=
name|doScan
argument_list|(
name|createScan
argument_list|()
operator|.
name|setReversed
argument_list|(
literal|true
argument_list|)
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|COUNT
argument_list|,
name|results
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|IntStream
operator|.
name|range
argument_list|(
literal|0
argument_list|,
name|COUNT
argument_list|)
operator|.
name|forEach
argument_list|(
name|i
lambda|->
name|assertResultEquals
argument_list|(
name|results
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|,
name|COUNT
operator|-
name|i
operator|-
literal|1
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testScanNoStopKey
parameter_list|()
throws|throws
name|Exception
block|{
name|int
name|start
init|=
literal|345
decl_stmt|;
name|List
argument_list|<
name|Result
argument_list|>
name|results
init|=
name|doScan
argument_list|(
name|createScan
argument_list|()
operator|.
name|withStartRow
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"%03d"
argument_list|,
name|start
argument_list|)
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|COUNT
operator|-
name|start
argument_list|,
name|results
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|IntStream
operator|.
name|range
argument_list|(
literal|0
argument_list|,
name|COUNT
operator|-
name|start
argument_list|)
operator|.
name|forEach
argument_list|(
name|i
lambda|->
name|assertResultEquals
argument_list|(
name|results
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|,
name|start
operator|+
name|i
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testReverseScanNoStopKey
parameter_list|()
throws|throws
name|Exception
block|{
name|int
name|start
init|=
literal|765
decl_stmt|;
name|List
argument_list|<
name|Result
argument_list|>
name|results
init|=
name|doScan
argument_list|(
name|createScan
argument_list|()
operator|.
name|withStartRow
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"%03d"
argument_list|,
name|start
argument_list|)
argument_list|)
argument_list|)
operator|.
name|setReversed
argument_list|(
literal|true
argument_list|)
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|start
operator|+
literal|1
argument_list|,
name|results
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|IntStream
operator|.
name|range
argument_list|(
literal|0
argument_list|,
name|start
operator|+
literal|1
argument_list|)
operator|.
name|forEach
argument_list|(
name|i
lambda|->
name|assertResultEquals
argument_list|(
name|results
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|,
name|start
operator|-
name|i
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|testScan
parameter_list|(
name|int
name|start
parameter_list|,
name|boolean
name|startInclusive
parameter_list|,
name|int
name|stop
parameter_list|,
name|boolean
name|stopInclusive
parameter_list|)
throws|throws
name|Exception
block|{
name|List
argument_list|<
name|Result
argument_list|>
name|results
init|=
name|doScan
argument_list|(
name|createScan
argument_list|()
operator|.
name|withStartRow
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"%03d"
argument_list|,
name|start
argument_list|)
argument_list|)
argument_list|,
name|startInclusive
argument_list|)
operator|.
name|withStopRow
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"%03d"
argument_list|,
name|stop
argument_list|)
argument_list|)
argument_list|,
name|stopInclusive
argument_list|)
argument_list|)
decl_stmt|;
name|int
name|actualStart
init|=
name|startInclusive
condition|?
name|start
else|:
name|start
operator|+
literal|1
decl_stmt|;
name|int
name|actualStop
init|=
name|stopInclusive
condition|?
name|stop
operator|+
literal|1
else|:
name|stop
decl_stmt|;
name|assertEquals
argument_list|(
name|actualStop
operator|-
name|actualStart
argument_list|,
name|results
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|IntStream
operator|.
name|range
argument_list|(
literal|0
argument_list|,
name|actualStop
operator|-
name|actualStart
argument_list|)
operator|.
name|forEach
argument_list|(
name|i
lambda|->
name|assertResultEquals
argument_list|(
name|results
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|,
name|actualStart
operator|+
name|i
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|testReversedScan
parameter_list|(
name|int
name|start
parameter_list|,
name|boolean
name|startInclusive
parameter_list|,
name|int
name|stop
parameter_list|,
name|boolean
name|stopInclusive
parameter_list|)
throws|throws
name|Exception
block|{
name|List
argument_list|<
name|Result
argument_list|>
name|results
init|=
name|doScan
argument_list|(
name|createScan
argument_list|()
operator|.
name|withStartRow
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"%03d"
argument_list|,
name|start
argument_list|)
argument_list|)
argument_list|,
name|startInclusive
argument_list|)
operator|.
name|withStopRow
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"%03d"
argument_list|,
name|stop
argument_list|)
argument_list|)
argument_list|,
name|stopInclusive
argument_list|)
operator|.
name|setReversed
argument_list|(
literal|true
argument_list|)
argument_list|)
decl_stmt|;
name|int
name|actualStart
init|=
name|startInclusive
condition|?
name|start
else|:
name|start
operator|-
literal|1
decl_stmt|;
name|int
name|actualStop
init|=
name|stopInclusive
condition|?
name|stop
operator|-
literal|1
else|:
name|stop
decl_stmt|;
name|assertEquals
argument_list|(
name|actualStart
operator|-
name|actualStop
argument_list|,
name|results
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|IntStream
operator|.
name|range
argument_list|(
literal|0
argument_list|,
name|actualStart
operator|-
name|actualStop
argument_list|)
operator|.
name|forEach
argument_list|(
name|i
lambda|->
name|assertResultEquals
argument_list|(
name|results
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|,
name|actualStart
operator|-
name|i
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testScanWithStartKeyAndStopKey
parameter_list|()
throws|throws
name|Exception
block|{
name|testScan
argument_list|(
literal|1
argument_list|,
literal|true
argument_list|,
literal|998
argument_list|,
literal|false
argument_list|)
expr_stmt|;
comment|// from first region to last region
name|testScan
argument_list|(
literal|123
argument_list|,
literal|true
argument_list|,
literal|345
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|testScan
argument_list|(
literal|234
argument_list|,
literal|true
argument_list|,
literal|456
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|testScan
argument_list|(
literal|345
argument_list|,
literal|false
argument_list|,
literal|567
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|testScan
argument_list|(
literal|456
argument_list|,
literal|false
argument_list|,
literal|678
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testReversedScanWithStartKeyAndStopKey
parameter_list|()
throws|throws
name|Exception
block|{
name|testReversedScan
argument_list|(
literal|998
argument_list|,
literal|true
argument_list|,
literal|1
argument_list|,
literal|false
argument_list|)
expr_stmt|;
comment|// from first region to first region
name|testReversedScan
argument_list|(
literal|543
argument_list|,
literal|true
argument_list|,
literal|321
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|testReversedScan
argument_list|(
literal|654
argument_list|,
literal|true
argument_list|,
literal|432
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|testReversedScan
argument_list|(
literal|765
argument_list|,
literal|false
argument_list|,
literal|543
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|testReversedScan
argument_list|(
literal|876
argument_list|,
literal|false
argument_list|,
literal|654
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testScanAtRegionBoundary
parameter_list|()
throws|throws
name|Exception
block|{
name|testScan
argument_list|(
literal|222
argument_list|,
literal|true
argument_list|,
literal|333
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|testScan
argument_list|(
literal|333
argument_list|,
literal|true
argument_list|,
literal|444
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|testScan
argument_list|(
literal|444
argument_list|,
literal|false
argument_list|,
literal|555
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|testScan
argument_list|(
literal|555
argument_list|,
literal|false
argument_list|,
literal|666
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testReversedScanAtRegionBoundary
parameter_list|()
throws|throws
name|Exception
block|{
name|testReversedScan
argument_list|(
literal|333
argument_list|,
literal|true
argument_list|,
literal|222
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|testReversedScan
argument_list|(
literal|444
argument_list|,
literal|true
argument_list|,
literal|333
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|testReversedScan
argument_list|(
literal|555
argument_list|,
literal|false
argument_list|,
literal|444
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|testReversedScan
argument_list|(
literal|666
argument_list|,
literal|false
argument_list|,
literal|555
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

