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
operator|.
name|querymatcher
package|;
end_package

begin_import
import|import static
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
name|querymatcher
operator|.
name|ScanQueryMatcher
operator|.
name|MatchCode
operator|.
name|INCLUDE
import|;
end_import

begin_import
import|import static
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
name|querymatcher
operator|.
name|ScanQueryMatcher
operator|.
name|MatchCode
operator|.
name|SKIP
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
name|ArrayList
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
name|HConstants
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
name|KeepDeletedCells
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
name|KeyValue
operator|.
name|Type
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
name|KeyValueUtil
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
name|ScanInfo
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
name|ScanType
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
name|querymatcher
operator|.
name|ScanQueryMatcher
operator|.
name|MatchCode
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
name|EnvironmentEdgeManager
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
name|TestCompactionScanQueryMatcher
extends|extends
name|AbstractTestScanQueryMatcher
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
name|TestCompactionScanQueryMatcher
operator|.
name|class
argument_list|)
decl_stmt|;
annotation|@
name|Test
specifier|public
name|void
name|testMatch_PartialRangeDropDeletes
parameter_list|()
throws|throws
name|Exception
block|{
comment|// Some ranges.
name|testDropDeletes
argument_list|(
name|row2
argument_list|,
name|row3
argument_list|,
operator|new
name|byte
index|[]
index|[]
block|{
name|row1
block|,
name|row2
block|,
name|row2
block|,
name|row3
block|}
argument_list|,
name|INCLUDE
argument_list|,
name|SKIP
argument_list|,
name|SKIP
argument_list|,
name|INCLUDE
argument_list|)
expr_stmt|;
name|testDropDeletes
argument_list|(
name|row2
argument_list|,
name|row3
argument_list|,
operator|new
name|byte
index|[]
index|[]
block|{
name|row1
block|,
name|row1
block|,
name|row2
block|}
argument_list|,
name|INCLUDE
argument_list|,
name|INCLUDE
argument_list|,
name|SKIP
argument_list|)
expr_stmt|;
name|testDropDeletes
argument_list|(
name|row2
argument_list|,
name|row3
argument_list|,
operator|new
name|byte
index|[]
index|[]
block|{
name|row2
block|,
name|row3
block|,
name|row3
block|}
argument_list|,
name|SKIP
argument_list|,
name|INCLUDE
argument_list|,
name|INCLUDE
argument_list|)
expr_stmt|;
name|testDropDeletes
argument_list|(
name|row1
argument_list|,
name|row3
argument_list|,
operator|new
name|byte
index|[]
index|[]
block|{
name|row1
block|,
name|row2
block|,
name|row3
block|}
argument_list|,
name|SKIP
argument_list|,
name|SKIP
argument_list|,
name|INCLUDE
argument_list|)
expr_stmt|;
comment|// Open ranges.
name|testDropDeletes
argument_list|(
name|HConstants
operator|.
name|EMPTY_START_ROW
argument_list|,
name|row3
argument_list|,
operator|new
name|byte
index|[]
index|[]
block|{
name|row1
block|,
name|row2
block|,
name|row3
block|}
argument_list|,
name|SKIP
argument_list|,
name|SKIP
argument_list|,
name|INCLUDE
argument_list|)
expr_stmt|;
name|testDropDeletes
argument_list|(
name|row2
argument_list|,
name|HConstants
operator|.
name|EMPTY_END_ROW
argument_list|,
operator|new
name|byte
index|[]
index|[]
block|{
name|row1
block|,
name|row2
block|,
name|row3
block|}
argument_list|,
name|INCLUDE
argument_list|,
name|SKIP
argument_list|,
name|SKIP
argument_list|)
expr_stmt|;
name|testDropDeletes
argument_list|(
name|HConstants
operator|.
name|EMPTY_START_ROW
argument_list|,
name|HConstants
operator|.
name|EMPTY_END_ROW
argument_list|,
operator|new
name|byte
index|[]
index|[]
block|{
name|row1
block|,
name|row2
block|,
name|row3
block|,
name|row3
block|}
argument_list|,
name|SKIP
argument_list|,
name|SKIP
argument_list|,
name|SKIP
argument_list|,
name|SKIP
argument_list|)
expr_stmt|;
comment|// No KVs in range.
name|testDropDeletes
argument_list|(
name|row2
argument_list|,
name|row3
argument_list|,
operator|new
name|byte
index|[]
index|[]
block|{
name|row1
block|,
name|row1
block|,
name|row3
block|}
argument_list|,
name|INCLUDE
argument_list|,
name|INCLUDE
argument_list|,
name|INCLUDE
argument_list|)
expr_stmt|;
name|testDropDeletes
argument_list|(
name|row2
argument_list|,
name|row3
argument_list|,
operator|new
name|byte
index|[]
index|[]
block|{
name|row3
block|,
name|row3
block|}
argument_list|,
name|INCLUDE
argument_list|,
name|INCLUDE
argument_list|)
expr_stmt|;
name|testDropDeletes
argument_list|(
name|row2
argument_list|,
name|row3
argument_list|,
operator|new
name|byte
index|[]
index|[]
block|{
name|row1
block|,
name|row1
block|}
argument_list|,
name|INCLUDE
argument_list|,
name|INCLUDE
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|testDropDeletes
parameter_list|(
name|byte
index|[]
name|from
parameter_list|,
name|byte
index|[]
name|to
parameter_list|,
name|byte
index|[]
index|[]
name|rows
parameter_list|,
name|MatchCode
modifier|...
name|expected
parameter_list|)
throws|throws
name|IOException
block|{
name|long
name|now
init|=
name|EnvironmentEdgeManager
operator|.
name|currentTime
argument_list|()
decl_stmt|;
comment|// Set time to purge deletes to negative value to avoid it ever happening.
name|ScanInfo
name|scanInfo
init|=
operator|new
name|ScanInfo
argument_list|(
name|this
operator|.
name|conf
argument_list|,
name|fam2
argument_list|,
literal|0
argument_list|,
literal|1
argument_list|,
name|ttl
argument_list|,
name|KeepDeletedCells
operator|.
name|FALSE
argument_list|,
operator|-
literal|1L
argument_list|,
name|rowComparator
argument_list|)
decl_stmt|;
name|CompactionScanQueryMatcher
name|qm
init|=
name|CompactionScanQueryMatcher
operator|.
name|create
argument_list|(
name|scanInfo
argument_list|,
name|ScanType
operator|.
name|COMPACT_RETAIN_DELETES
argument_list|,
name|Long
operator|.
name|MAX_VALUE
argument_list|,
name|HConstants
operator|.
name|OLDEST_TIMESTAMP
argument_list|,
name|HConstants
operator|.
name|OLDEST_TIMESTAMP
argument_list|,
name|now
argument_list|,
name|from
argument_list|,
name|to
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|ScanQueryMatcher
operator|.
name|MatchCode
argument_list|>
name|actual
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|rows
operator|.
name|length
argument_list|)
decl_stmt|;
name|byte
index|[]
name|prevRow
init|=
literal|null
decl_stmt|;
for|for
control|(
name|byte
index|[]
name|row
range|:
name|rows
control|)
block|{
if|if
condition|(
name|prevRow
operator|==
literal|null
operator|||
operator|!
name|Bytes
operator|.
name|equals
argument_list|(
name|prevRow
argument_list|,
name|row
argument_list|)
condition|)
block|{
name|qm
operator|.
name|setToNewRow
argument_list|(
name|KeyValueUtil
operator|.
name|createFirstOnRow
argument_list|(
name|row
argument_list|)
argument_list|)
expr_stmt|;
name|prevRow
operator|=
name|row
expr_stmt|;
block|}
name|actual
operator|.
name|add
argument_list|(
name|qm
operator|.
name|match
argument_list|(
operator|new
name|KeyValue
argument_list|(
name|row
argument_list|,
name|fam2
argument_list|,
literal|null
argument_list|,
name|now
argument_list|,
name|Type
operator|.
name|Delete
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|assertEquals
argument_list|(
name|expected
operator|.
name|length
argument_list|,
name|actual
operator|.
name|size
argument_list|()
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
name|expected
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"expected "
operator|+
name|expected
index|[
name|i
index|]
operator|+
literal|", actual "
operator|+
name|actual
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|expected
index|[
name|i
index|]
argument_list|,
name|actual
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

