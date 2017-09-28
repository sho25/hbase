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
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertFalse
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
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|io
operator|.
name|TimeRange
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
name|RegionServerTests
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
name|Writables
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
name|RegionServerTests
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
name|TestSimpleTimeRangeTracker
block|{
specifier|protected
name|TimeRangeTracker
name|getTimeRangeTracker
parameter_list|()
block|{
return|return
name|TimeRangeTracker
operator|.
name|create
argument_list|(
name|TimeRangeTracker
operator|.
name|Type
operator|.
name|NON_SYNC
argument_list|)
return|;
block|}
specifier|protected
name|TimeRangeTracker
name|getTimeRangeTracker
parameter_list|(
name|long
name|min
parameter_list|,
name|long
name|max
parameter_list|)
block|{
return|return
name|TimeRangeTracker
operator|.
name|create
argument_list|(
name|TimeRangeTracker
operator|.
name|Type
operator|.
name|NON_SYNC
argument_list|,
name|min
argument_list|,
name|max
argument_list|)
return|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testExtreme
parameter_list|()
block|{
name|TimeRange
name|tr
init|=
operator|new
name|TimeRange
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
name|tr
operator|.
name|includesTimeRange
argument_list|(
operator|new
name|TimeRange
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|TimeRangeTracker
name|trt
init|=
name|getTimeRangeTracker
argument_list|()
decl_stmt|;
name|assertFalse
argument_list|(
name|trt
operator|.
name|includesTimeRange
argument_list|(
operator|new
name|TimeRange
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|trt
operator|.
name|includeTimestamp
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|trt
operator|.
name|includeTimestamp
argument_list|(
literal|10
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|trt
operator|.
name|includesTimeRange
argument_list|(
operator|new
name|TimeRange
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testTimeRangeInitialized
parameter_list|()
block|{
name|TimeRangeTracker
name|src
init|=
name|getTimeRangeTracker
argument_list|()
decl_stmt|;
name|TimeRange
name|tr
init|=
operator|new
name|TimeRange
argument_list|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|)
decl_stmt|;
name|assertFalse
argument_list|(
name|src
operator|.
name|includesTimeRange
argument_list|(
name|tr
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testTimeRangeTrackerNullIsSameAsTimeRangeNull
parameter_list|()
throws|throws
name|IOException
block|{
name|TimeRangeTracker
name|src
init|=
name|getTimeRangeTracker
argument_list|(
literal|1
argument_list|,
literal|2
argument_list|)
decl_stmt|;
name|byte
index|[]
name|bytes
init|=
name|Writables
operator|.
name|getBytes
argument_list|(
name|src
argument_list|)
decl_stmt|;
name|TimeRange
name|tgt
init|=
name|TimeRangeTracker
operator|.
name|getTimeRange
argument_list|(
name|bytes
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|src
operator|.
name|getMin
argument_list|()
argument_list|,
name|tgt
operator|.
name|getMin
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|src
operator|.
name|getMax
argument_list|()
argument_list|,
name|tgt
operator|.
name|getMax
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testSerialization
parameter_list|()
throws|throws
name|IOException
block|{
name|TimeRangeTracker
name|src
init|=
name|getTimeRangeTracker
argument_list|(
literal|1
argument_list|,
literal|2
argument_list|)
decl_stmt|;
name|TimeRangeTracker
name|tgt
init|=
name|getTimeRangeTracker
argument_list|()
decl_stmt|;
name|Writables
operator|.
name|copyWritable
argument_list|(
name|src
argument_list|,
name|tgt
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|src
operator|.
name|getMin
argument_list|()
argument_list|,
name|tgt
operator|.
name|getMin
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|src
operator|.
name|getMax
argument_list|()
argument_list|,
name|tgt
operator|.
name|getMax
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testAlwaysDecrementingSetsMaximum
parameter_list|()
block|{
name|TimeRangeTracker
name|trr
init|=
name|getTimeRangeTracker
argument_list|()
decl_stmt|;
name|trr
operator|.
name|includeTimestamp
argument_list|(
literal|3
argument_list|)
expr_stmt|;
name|trr
operator|.
name|includeTimestamp
argument_list|(
literal|2
argument_list|)
expr_stmt|;
name|trr
operator|.
name|includeTimestamp
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|trr
operator|.
name|getMin
argument_list|()
operator|!=
name|TimeRangeTracker
operator|.
name|INITIAL_MIN_TIMESTAMP
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|trr
operator|.
name|getMax
argument_list|()
operator|!=
operator|-
literal|1
comment|/*The initial max value*/
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testSimpleInRange
parameter_list|()
block|{
name|TimeRangeTracker
name|trr
init|=
name|getTimeRangeTracker
argument_list|()
decl_stmt|;
name|trr
operator|.
name|includeTimestamp
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|trr
operator|.
name|includeTimestamp
argument_list|(
literal|2
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|trr
operator|.
name|includesTimeRange
argument_list|(
operator|new
name|TimeRange
argument_list|(
literal|1
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testRangeConstruction
parameter_list|()
throws|throws
name|IOException
block|{
name|TimeRange
name|defaultRange
init|=
operator|new
name|TimeRange
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|0L
argument_list|,
name|defaultRange
operator|.
name|getMin
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|Long
operator|.
name|MAX_VALUE
argument_list|,
name|defaultRange
operator|.
name|getMax
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|defaultRange
operator|.
name|isAllTime
argument_list|()
argument_list|)
expr_stmt|;
name|TimeRange
name|oneArgRange
init|=
operator|new
name|TimeRange
argument_list|(
literal|0L
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|0L
argument_list|,
name|oneArgRange
operator|.
name|getMin
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|Long
operator|.
name|MAX_VALUE
argument_list|,
name|oneArgRange
operator|.
name|getMax
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|oneArgRange
operator|.
name|isAllTime
argument_list|()
argument_list|)
expr_stmt|;
name|TimeRange
name|oneArgRange2
init|=
operator|new
name|TimeRange
argument_list|(
literal|1
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|oneArgRange2
operator|.
name|getMin
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|Long
operator|.
name|MAX_VALUE
argument_list|,
name|oneArgRange2
operator|.
name|getMax
argument_list|()
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|oneArgRange2
operator|.
name|isAllTime
argument_list|()
argument_list|)
expr_stmt|;
name|TimeRange
name|twoArgRange
init|=
operator|new
name|TimeRange
argument_list|(
literal|0L
argument_list|,
name|Long
operator|.
name|MAX_VALUE
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|0L
argument_list|,
name|twoArgRange
operator|.
name|getMin
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|Long
operator|.
name|MAX_VALUE
argument_list|,
name|twoArgRange
operator|.
name|getMax
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|twoArgRange
operator|.
name|isAllTime
argument_list|()
argument_list|)
expr_stmt|;
name|TimeRange
name|twoArgRange2
init|=
operator|new
name|TimeRange
argument_list|(
literal|0L
argument_list|,
name|Long
operator|.
name|MAX_VALUE
operator|-
literal|1
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|0L
argument_list|,
name|twoArgRange2
operator|.
name|getMin
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|Long
operator|.
name|MAX_VALUE
operator|-
literal|1
argument_list|,
name|twoArgRange2
operator|.
name|getMax
argument_list|()
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|twoArgRange2
operator|.
name|isAllTime
argument_list|()
argument_list|)
expr_stmt|;
name|TimeRange
name|twoArgRange3
init|=
operator|new
name|TimeRange
argument_list|(
literal|1
argument_list|,
name|Long
operator|.
name|MAX_VALUE
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|twoArgRange3
operator|.
name|getMin
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|Long
operator|.
name|MAX_VALUE
argument_list|,
name|twoArgRange3
operator|.
name|getMax
argument_list|()
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|twoArgRange3
operator|.
name|isAllTime
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit
