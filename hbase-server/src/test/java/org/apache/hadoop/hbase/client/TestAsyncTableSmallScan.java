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
name|concurrent
operator|.
name|ExecutionException
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
name|MediumTests
operator|.
name|class
block|,
name|ClientTests
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|TestAsyncTableSmallScan
extends|extends
name|AbstractTestAsyncTableScan
block|{
annotation|@
name|Test
specifier|public
name|void
name|testScanWithLimit
parameter_list|()
throws|throws
name|InterruptedException
throws|,
name|ExecutionException
block|{
name|AsyncTable
name|table
init|=
name|ASYNC_CONN
operator|.
name|getTable
argument_list|(
name|TABLE_NAME
argument_list|)
decl_stmt|;
name|int
name|start
init|=
literal|111
decl_stmt|;
name|int
name|stop
init|=
literal|888
decl_stmt|;
name|int
name|limit
init|=
literal|300
decl_stmt|;
name|List
argument_list|<
name|Result
argument_list|>
name|results
init|=
name|table
operator|.
name|smallScan
argument_list|(
operator|new
name|Scan
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
name|setStopRow
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
argument_list|)
operator|.
name|setSmall
argument_list|(
literal|true
argument_list|)
argument_list|,
name|limit
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
name|limit
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
name|limit
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
name|int
name|actualIndex
init|=
name|start
operator|+
name|i
decl_stmt|;
name|assertEquals
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"%03d"
argument_list|,
name|actualIndex
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
name|actualIndex
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
annotation|@
name|Test
specifier|public
name|void
name|testReversedScanWithLimit
parameter_list|()
throws|throws
name|InterruptedException
throws|,
name|ExecutionException
block|{
name|AsyncTable
name|table
init|=
name|ASYNC_CONN
operator|.
name|getTable
argument_list|(
name|TABLE_NAME
argument_list|)
decl_stmt|;
name|int
name|start
init|=
literal|888
decl_stmt|;
name|int
name|stop
init|=
literal|111
decl_stmt|;
name|int
name|limit
init|=
literal|300
decl_stmt|;
name|List
argument_list|<
name|Result
argument_list|>
name|results
init|=
name|table
operator|.
name|smallScan
argument_list|(
operator|new
name|Scan
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
name|setStopRow
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
argument_list|)
operator|.
name|setSmall
argument_list|(
literal|true
argument_list|)
operator|.
name|setReversed
argument_list|(
literal|true
argument_list|)
argument_list|,
name|limit
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
name|limit
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
name|limit
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
name|int
name|actualIndex
init|=
name|start
operator|-
name|i
decl_stmt|;
name|assertEquals
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"%03d"
argument_list|,
name|actualIndex
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
name|actualIndex
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
annotation|@
name|Override
specifier|protected
name|Scan
name|createScan
parameter_list|()
block|{
return|return
operator|new
name|Scan
argument_list|()
operator|.
name|setSmall
argument_list|(
literal|true
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|protected
name|List
argument_list|<
name|Result
argument_list|>
name|doScan
parameter_list|(
name|AsyncTable
name|table
parameter_list|,
name|Scan
name|scan
parameter_list|)
throws|throws
name|Exception
block|{
return|return
name|table
operator|.
name|smallScan
argument_list|(
name|scan
argument_list|)
operator|.
name|get
argument_list|()
return|;
block|}
block|}
end_class

end_unit

