begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|metrics
operator|.
name|impl
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
name|metrics
operator|.
name|Snapshot
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
comment|/**  * Test case for {@link HistogramImpl}  */
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
name|TestHistogramImpl
block|{
annotation|@
name|Test
specifier|public
name|void
name|testUpdate
parameter_list|()
block|{
name|HistogramImpl
name|histogram
init|=
operator|new
name|HistogramImpl
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|histogram
operator|.
name|getCount
argument_list|()
argument_list|)
expr_stmt|;
name|histogram
operator|.
name|update
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|histogram
operator|.
name|getCount
argument_list|()
argument_list|)
expr_stmt|;
name|histogram
operator|.
name|update
argument_list|(
literal|10
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|histogram
operator|.
name|getCount
argument_list|()
argument_list|)
expr_stmt|;
name|histogram
operator|.
name|update
argument_list|(
literal|20
argument_list|)
expr_stmt|;
name|histogram
operator|.
name|update
argument_list|(
literal|30
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|4
argument_list|,
name|histogram
operator|.
name|getCount
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testSnapshot
parameter_list|()
block|{
name|HistogramImpl
name|histogram
init|=
operator|new
name|HistogramImpl
argument_list|()
decl_stmt|;
name|IntStream
operator|.
name|range
argument_list|(
literal|0
argument_list|,
literal|100
argument_list|)
operator|.
name|forEach
argument_list|(
name|histogram
operator|::
name|update
argument_list|)
expr_stmt|;
name|Snapshot
name|snapshot
init|=
name|histogram
operator|.
name|snapshot
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|100
argument_list|,
name|snapshot
operator|.
name|getCount
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|50
argument_list|,
name|snapshot
operator|.
name|getMedian
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|49
argument_list|,
name|snapshot
operator|.
name|getMean
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|snapshot
operator|.
name|getMin
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|99
argument_list|,
name|snapshot
operator|.
name|getMax
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|25
argument_list|,
name|snapshot
operator|.
name|get25thPercentile
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|75
argument_list|,
name|snapshot
operator|.
name|get75thPercentile
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|90
argument_list|,
name|snapshot
operator|.
name|get90thPercentile
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|95
argument_list|,
name|snapshot
operator|.
name|get95thPercentile
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|98
argument_list|,
name|snapshot
operator|.
name|get98thPercentile
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|99
argument_list|,
name|snapshot
operator|.
name|get99thPercentile
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|99
argument_list|,
name|snapshot
operator|.
name|get999thPercentile
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|51
argument_list|,
name|snapshot
operator|.
name|getCountAtOrBelow
argument_list|(
literal|50
argument_list|)
argument_list|)
expr_stmt|;
comment|// check that histogram is reset.
name|assertEquals
argument_list|(
literal|100
argument_list|,
name|histogram
operator|.
name|getCount
argument_list|()
argument_list|)
expr_stmt|;
comment|// count does not reset
comment|// put more data after reset
name|IntStream
operator|.
name|range
argument_list|(
literal|100
argument_list|,
literal|200
argument_list|)
operator|.
name|forEach
argument_list|(
name|histogram
operator|::
name|update
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|200
argument_list|,
name|histogram
operator|.
name|getCount
argument_list|()
argument_list|)
expr_stmt|;
name|snapshot
operator|=
name|histogram
operator|.
name|snapshot
argument_list|()
expr_stmt|;
name|assertEquals
argument_list|(
literal|100
argument_list|,
name|snapshot
operator|.
name|getCount
argument_list|()
argument_list|)
expr_stmt|;
comment|// only 100 more events
name|assertEquals
argument_list|(
literal|150
argument_list|,
name|snapshot
operator|.
name|getMedian
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|149
argument_list|,
name|snapshot
operator|.
name|getMean
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|100
argument_list|,
name|snapshot
operator|.
name|getMin
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|199
argument_list|,
name|snapshot
operator|.
name|getMax
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|125
argument_list|,
name|snapshot
operator|.
name|get25thPercentile
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|175
argument_list|,
name|snapshot
operator|.
name|get75thPercentile
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|190
argument_list|,
name|snapshot
operator|.
name|get90thPercentile
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|195
argument_list|,
name|snapshot
operator|.
name|get95thPercentile
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|198
argument_list|,
name|snapshot
operator|.
name|get98thPercentile
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|199
argument_list|,
name|snapshot
operator|.
name|get99thPercentile
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|199
argument_list|,
name|snapshot
operator|.
name|get999thPercentile
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

