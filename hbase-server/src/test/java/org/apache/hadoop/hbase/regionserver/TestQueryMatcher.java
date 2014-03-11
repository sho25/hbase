begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|java
operator|.
name|util
operator|.
name|NavigableSet
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
name|ScanQueryMatcher
operator|.
name|MatchCode
operator|.
name|*
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
name|HBaseTestCase
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
name|KVComparator
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
name|regionserver
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
name|SmallTests
operator|.
name|class
argument_list|)
specifier|public
class|class
name|TestQueryMatcher
extends|extends
name|HBaseTestCase
block|{
specifier|private
specifier|static
specifier|final
name|boolean
name|PRINT
init|=
literal|false
decl_stmt|;
specifier|private
name|byte
index|[]
name|row1
decl_stmt|;
specifier|private
name|byte
index|[]
name|row2
decl_stmt|;
specifier|private
name|byte
index|[]
name|row3
decl_stmt|;
specifier|private
name|byte
index|[]
name|fam1
decl_stmt|;
specifier|private
name|byte
index|[]
name|fam2
decl_stmt|;
specifier|private
name|byte
index|[]
name|col1
decl_stmt|;
specifier|private
name|byte
index|[]
name|col2
decl_stmt|;
specifier|private
name|byte
index|[]
name|col3
decl_stmt|;
specifier|private
name|byte
index|[]
name|col4
decl_stmt|;
specifier|private
name|byte
index|[]
name|col5
decl_stmt|;
specifier|private
name|byte
index|[]
name|data
decl_stmt|;
specifier|private
name|Get
name|get
decl_stmt|;
name|long
name|ttl
init|=
name|Long
operator|.
name|MAX_VALUE
decl_stmt|;
name|KVComparator
name|rowComparator
decl_stmt|;
specifier|private
name|Scan
name|scan
decl_stmt|;
specifier|public
name|void
name|setUp
parameter_list|()
throws|throws
name|Exception
block|{
name|super
operator|.
name|setUp
argument_list|()
expr_stmt|;
name|row1
operator|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"row1"
argument_list|)
expr_stmt|;
name|row2
operator|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"row2"
argument_list|)
expr_stmt|;
name|row3
operator|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"row3"
argument_list|)
expr_stmt|;
name|fam1
operator|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"fam1"
argument_list|)
expr_stmt|;
name|fam2
operator|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"fam2"
argument_list|)
expr_stmt|;
name|col1
operator|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"col1"
argument_list|)
expr_stmt|;
name|col2
operator|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"col2"
argument_list|)
expr_stmt|;
name|col3
operator|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"col3"
argument_list|)
expr_stmt|;
name|col4
operator|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"col4"
argument_list|)
expr_stmt|;
name|col5
operator|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"col5"
argument_list|)
expr_stmt|;
name|data
operator|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"data"
argument_list|)
expr_stmt|;
comment|//Create Get
name|get
operator|=
operator|new
name|Get
argument_list|(
name|row1
argument_list|)
expr_stmt|;
name|get
operator|.
name|addFamily
argument_list|(
name|fam1
argument_list|)
expr_stmt|;
name|get
operator|.
name|addColumn
argument_list|(
name|fam2
argument_list|,
name|col2
argument_list|)
expr_stmt|;
name|get
operator|.
name|addColumn
argument_list|(
name|fam2
argument_list|,
name|col4
argument_list|)
expr_stmt|;
name|get
operator|.
name|addColumn
argument_list|(
name|fam2
argument_list|,
name|col5
argument_list|)
expr_stmt|;
name|this
operator|.
name|scan
operator|=
operator|new
name|Scan
argument_list|(
name|get
argument_list|)
expr_stmt|;
name|rowComparator
operator|=
name|KeyValue
operator|.
name|COMPARATOR
expr_stmt|;
block|}
specifier|private
name|void
name|_testMatch_ExplicitColumns
parameter_list|(
name|Scan
name|scan
parameter_list|,
name|List
argument_list|<
name|MatchCode
argument_list|>
name|expected
parameter_list|)
throws|throws
name|IOException
block|{
comment|// 2,4,5
name|ScanQueryMatcher
name|qm
init|=
operator|new
name|ScanQueryMatcher
argument_list|(
name|scan
argument_list|,
operator|new
name|ScanInfo
argument_list|(
name|fam2
argument_list|,
literal|0
argument_list|,
literal|1
argument_list|,
name|ttl
argument_list|,
literal|false
argument_list|,
literal|0
argument_list|,
name|rowComparator
argument_list|)
argument_list|,
name|get
operator|.
name|getFamilyMap
argument_list|()
operator|.
name|get
argument_list|(
name|fam2
argument_list|)
argument_list|,
name|EnvironmentEdgeManager
operator|.
name|currentTimeMillis
argument_list|()
operator|-
name|ttl
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|KeyValue
argument_list|>
name|memstore
init|=
operator|new
name|ArrayList
argument_list|<
name|KeyValue
argument_list|>
argument_list|()
decl_stmt|;
name|memstore
operator|.
name|add
argument_list|(
operator|new
name|KeyValue
argument_list|(
name|row1
argument_list|,
name|fam2
argument_list|,
name|col1
argument_list|,
literal|1
argument_list|,
name|data
argument_list|)
argument_list|)
expr_stmt|;
name|memstore
operator|.
name|add
argument_list|(
operator|new
name|KeyValue
argument_list|(
name|row1
argument_list|,
name|fam2
argument_list|,
name|col2
argument_list|,
literal|1
argument_list|,
name|data
argument_list|)
argument_list|)
expr_stmt|;
name|memstore
operator|.
name|add
argument_list|(
operator|new
name|KeyValue
argument_list|(
name|row1
argument_list|,
name|fam2
argument_list|,
name|col3
argument_list|,
literal|1
argument_list|,
name|data
argument_list|)
argument_list|)
expr_stmt|;
name|memstore
operator|.
name|add
argument_list|(
operator|new
name|KeyValue
argument_list|(
name|row1
argument_list|,
name|fam2
argument_list|,
name|col4
argument_list|,
literal|1
argument_list|,
name|data
argument_list|)
argument_list|)
expr_stmt|;
name|memstore
operator|.
name|add
argument_list|(
operator|new
name|KeyValue
argument_list|(
name|row1
argument_list|,
name|fam2
argument_list|,
name|col5
argument_list|,
literal|1
argument_list|,
name|data
argument_list|)
argument_list|)
expr_stmt|;
name|memstore
operator|.
name|add
argument_list|(
operator|new
name|KeyValue
argument_list|(
name|row2
argument_list|,
name|fam1
argument_list|,
name|col1
argument_list|,
name|data
argument_list|)
argument_list|)
expr_stmt|;
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
argument_list|<
name|ScanQueryMatcher
operator|.
name|MatchCode
argument_list|>
argument_list|()
decl_stmt|;
name|KeyValue
name|k
init|=
name|memstore
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|qm
operator|.
name|setRow
argument_list|(
name|k
operator|.
name|getRowArray
argument_list|()
argument_list|,
name|k
operator|.
name|getRowOffset
argument_list|()
argument_list|,
name|k
operator|.
name|getRowLength
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|KeyValue
name|kv
range|:
name|memstore
control|)
block|{
name|actual
operator|.
name|add
argument_list|(
name|qm
operator|.
name|match
argument_list|(
name|kv
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|assertEquals
argument_list|(
name|expected
operator|.
name|size
argument_list|()
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
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|assertEquals
argument_list|(
name|expected
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|,
name|actual
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|PRINT
condition|)
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"expected "
operator|+
name|expected
operator|.
name|get
argument_list|(
name|i
argument_list|)
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
block|}
block|}
block|}
specifier|public
name|void
name|testMatch_ExplicitColumns
parameter_list|()
throws|throws
name|IOException
block|{
comment|//Moving up from the Tracker by using Gets and List<KeyValue> instead
comment|//of just byte []
comment|//Expected result
name|List
argument_list|<
name|MatchCode
argument_list|>
name|expected
init|=
operator|new
name|ArrayList
argument_list|<
name|ScanQueryMatcher
operator|.
name|MatchCode
argument_list|>
argument_list|()
decl_stmt|;
name|expected
operator|.
name|add
argument_list|(
name|ScanQueryMatcher
operator|.
name|MatchCode
operator|.
name|SEEK_NEXT_COL
argument_list|)
expr_stmt|;
name|expected
operator|.
name|add
argument_list|(
name|ScanQueryMatcher
operator|.
name|MatchCode
operator|.
name|INCLUDE_AND_SEEK_NEXT_COL
argument_list|)
expr_stmt|;
name|expected
operator|.
name|add
argument_list|(
name|ScanQueryMatcher
operator|.
name|MatchCode
operator|.
name|SEEK_NEXT_COL
argument_list|)
expr_stmt|;
name|expected
operator|.
name|add
argument_list|(
name|ScanQueryMatcher
operator|.
name|MatchCode
operator|.
name|INCLUDE_AND_SEEK_NEXT_COL
argument_list|)
expr_stmt|;
name|expected
operator|.
name|add
argument_list|(
name|ScanQueryMatcher
operator|.
name|MatchCode
operator|.
name|INCLUDE_AND_SEEK_NEXT_ROW
argument_list|)
expr_stmt|;
name|expected
operator|.
name|add
argument_list|(
name|ScanQueryMatcher
operator|.
name|MatchCode
operator|.
name|DONE
argument_list|)
expr_stmt|;
name|_testMatch_ExplicitColumns
argument_list|(
name|scan
argument_list|,
name|expected
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|testMatch_ExplicitColumnsWithLookAhead
parameter_list|()
throws|throws
name|IOException
block|{
comment|//Moving up from the Tracker by using Gets and List<KeyValue> instead
comment|//of just byte []
comment|//Expected result
name|List
argument_list|<
name|MatchCode
argument_list|>
name|expected
init|=
operator|new
name|ArrayList
argument_list|<
name|ScanQueryMatcher
operator|.
name|MatchCode
argument_list|>
argument_list|()
decl_stmt|;
name|expected
operator|.
name|add
argument_list|(
name|ScanQueryMatcher
operator|.
name|MatchCode
operator|.
name|SKIP
argument_list|)
expr_stmt|;
name|expected
operator|.
name|add
argument_list|(
name|ScanQueryMatcher
operator|.
name|MatchCode
operator|.
name|INCLUDE_AND_SEEK_NEXT_COL
argument_list|)
expr_stmt|;
name|expected
operator|.
name|add
argument_list|(
name|ScanQueryMatcher
operator|.
name|MatchCode
operator|.
name|SKIP
argument_list|)
expr_stmt|;
name|expected
operator|.
name|add
argument_list|(
name|ScanQueryMatcher
operator|.
name|MatchCode
operator|.
name|INCLUDE_AND_SEEK_NEXT_COL
argument_list|)
expr_stmt|;
name|expected
operator|.
name|add
argument_list|(
name|ScanQueryMatcher
operator|.
name|MatchCode
operator|.
name|INCLUDE_AND_SEEK_NEXT_ROW
argument_list|)
expr_stmt|;
name|expected
operator|.
name|add
argument_list|(
name|ScanQueryMatcher
operator|.
name|MatchCode
operator|.
name|DONE
argument_list|)
expr_stmt|;
name|Scan
name|s
init|=
operator|new
name|Scan
argument_list|(
name|scan
argument_list|)
decl_stmt|;
name|s
operator|.
name|setAttribute
argument_list|(
name|Scan
operator|.
name|HINT_LOOKAHEAD
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|_testMatch_ExplicitColumns
argument_list|(
name|s
argument_list|,
name|expected
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|testMatch_Wildcard
parameter_list|()
throws|throws
name|IOException
block|{
comment|//Moving up from the Tracker by using Gets and List<KeyValue> instead
comment|//of just byte []
comment|//Expected result
name|List
argument_list|<
name|MatchCode
argument_list|>
name|expected
init|=
operator|new
name|ArrayList
argument_list|<
name|ScanQueryMatcher
operator|.
name|MatchCode
argument_list|>
argument_list|()
decl_stmt|;
name|expected
operator|.
name|add
argument_list|(
name|ScanQueryMatcher
operator|.
name|MatchCode
operator|.
name|INCLUDE
argument_list|)
expr_stmt|;
name|expected
operator|.
name|add
argument_list|(
name|ScanQueryMatcher
operator|.
name|MatchCode
operator|.
name|INCLUDE
argument_list|)
expr_stmt|;
name|expected
operator|.
name|add
argument_list|(
name|ScanQueryMatcher
operator|.
name|MatchCode
operator|.
name|INCLUDE
argument_list|)
expr_stmt|;
name|expected
operator|.
name|add
argument_list|(
name|ScanQueryMatcher
operator|.
name|MatchCode
operator|.
name|INCLUDE
argument_list|)
expr_stmt|;
name|expected
operator|.
name|add
argument_list|(
name|ScanQueryMatcher
operator|.
name|MatchCode
operator|.
name|INCLUDE
argument_list|)
expr_stmt|;
name|expected
operator|.
name|add
argument_list|(
name|ScanQueryMatcher
operator|.
name|MatchCode
operator|.
name|DONE
argument_list|)
expr_stmt|;
name|ScanQueryMatcher
name|qm
init|=
operator|new
name|ScanQueryMatcher
argument_list|(
name|scan
argument_list|,
operator|new
name|ScanInfo
argument_list|(
name|fam2
argument_list|,
literal|0
argument_list|,
literal|1
argument_list|,
name|ttl
argument_list|,
literal|false
argument_list|,
literal|0
argument_list|,
name|rowComparator
argument_list|)
argument_list|,
literal|null
argument_list|,
name|EnvironmentEdgeManager
operator|.
name|currentTimeMillis
argument_list|()
operator|-
name|ttl
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|KeyValue
argument_list|>
name|memstore
init|=
operator|new
name|ArrayList
argument_list|<
name|KeyValue
argument_list|>
argument_list|()
decl_stmt|;
name|memstore
operator|.
name|add
argument_list|(
operator|new
name|KeyValue
argument_list|(
name|row1
argument_list|,
name|fam2
argument_list|,
name|col1
argument_list|,
literal|1
argument_list|,
name|data
argument_list|)
argument_list|)
expr_stmt|;
name|memstore
operator|.
name|add
argument_list|(
operator|new
name|KeyValue
argument_list|(
name|row1
argument_list|,
name|fam2
argument_list|,
name|col2
argument_list|,
literal|1
argument_list|,
name|data
argument_list|)
argument_list|)
expr_stmt|;
name|memstore
operator|.
name|add
argument_list|(
operator|new
name|KeyValue
argument_list|(
name|row1
argument_list|,
name|fam2
argument_list|,
name|col3
argument_list|,
literal|1
argument_list|,
name|data
argument_list|)
argument_list|)
expr_stmt|;
name|memstore
operator|.
name|add
argument_list|(
operator|new
name|KeyValue
argument_list|(
name|row1
argument_list|,
name|fam2
argument_list|,
name|col4
argument_list|,
literal|1
argument_list|,
name|data
argument_list|)
argument_list|)
expr_stmt|;
name|memstore
operator|.
name|add
argument_list|(
operator|new
name|KeyValue
argument_list|(
name|row1
argument_list|,
name|fam2
argument_list|,
name|col5
argument_list|,
literal|1
argument_list|,
name|data
argument_list|)
argument_list|)
expr_stmt|;
name|memstore
operator|.
name|add
argument_list|(
operator|new
name|KeyValue
argument_list|(
name|row2
argument_list|,
name|fam1
argument_list|,
name|col1
argument_list|,
literal|1
argument_list|,
name|data
argument_list|)
argument_list|)
expr_stmt|;
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
argument_list|<
name|ScanQueryMatcher
operator|.
name|MatchCode
argument_list|>
argument_list|()
decl_stmt|;
name|KeyValue
name|k
init|=
name|memstore
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|qm
operator|.
name|setRow
argument_list|(
name|k
operator|.
name|getRowArray
argument_list|()
argument_list|,
name|k
operator|.
name|getRowOffset
argument_list|()
argument_list|,
name|k
operator|.
name|getRowLength
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|KeyValue
name|kv
range|:
name|memstore
control|)
block|{
name|actual
operator|.
name|add
argument_list|(
name|qm
operator|.
name|match
argument_list|(
name|kv
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|assertEquals
argument_list|(
name|expected
operator|.
name|size
argument_list|()
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
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|assertEquals
argument_list|(
name|expected
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|,
name|actual
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|PRINT
condition|)
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"expected "
operator|+
name|expected
operator|.
name|get
argument_list|(
name|i
argument_list|)
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
block|}
block|}
block|}
comment|/**    * Verify that {@link ScanQueryMatcher} only skips expired KeyValue    * instances and does not exit early from the row (skipping    * later non-expired KeyValues).  This version mimics a Get with    * explicitly specified column qualifiers.    *    * @throws IOException    */
specifier|public
name|void
name|testMatch_ExpiredExplicit
parameter_list|()
throws|throws
name|IOException
block|{
name|long
name|testTTL
init|=
literal|1000
decl_stmt|;
name|MatchCode
index|[]
name|expected
init|=
operator|new
name|MatchCode
index|[]
block|{
name|ScanQueryMatcher
operator|.
name|MatchCode
operator|.
name|SEEK_NEXT_COL
block|,
name|ScanQueryMatcher
operator|.
name|MatchCode
operator|.
name|INCLUDE_AND_SEEK_NEXT_COL
block|,
name|ScanQueryMatcher
operator|.
name|MatchCode
operator|.
name|SEEK_NEXT_COL
block|,
name|ScanQueryMatcher
operator|.
name|MatchCode
operator|.
name|INCLUDE_AND_SEEK_NEXT_COL
block|,
name|ScanQueryMatcher
operator|.
name|MatchCode
operator|.
name|SEEK_NEXT_ROW
block|,
name|ScanQueryMatcher
operator|.
name|MatchCode
operator|.
name|DONE
block|}
decl_stmt|;
name|long
name|now
init|=
name|EnvironmentEdgeManager
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|ScanQueryMatcher
name|qm
init|=
operator|new
name|ScanQueryMatcher
argument_list|(
name|scan
argument_list|,
operator|new
name|ScanInfo
argument_list|(
name|fam2
argument_list|,
literal|0
argument_list|,
literal|1
argument_list|,
name|testTTL
argument_list|,
literal|false
argument_list|,
literal|0
argument_list|,
name|rowComparator
argument_list|)
argument_list|,
name|get
operator|.
name|getFamilyMap
argument_list|()
operator|.
name|get
argument_list|(
name|fam2
argument_list|)
argument_list|,
name|now
operator|-
name|testTTL
argument_list|)
decl_stmt|;
name|KeyValue
index|[]
name|kvs
init|=
operator|new
name|KeyValue
index|[]
block|{
operator|new
name|KeyValue
argument_list|(
name|row1
argument_list|,
name|fam2
argument_list|,
name|col1
argument_list|,
name|now
operator|-
literal|100
argument_list|,
name|data
argument_list|)
block|,
operator|new
name|KeyValue
argument_list|(
name|row1
argument_list|,
name|fam2
argument_list|,
name|col2
argument_list|,
name|now
operator|-
literal|50
argument_list|,
name|data
argument_list|)
block|,
operator|new
name|KeyValue
argument_list|(
name|row1
argument_list|,
name|fam2
argument_list|,
name|col3
argument_list|,
name|now
operator|-
literal|5000
argument_list|,
name|data
argument_list|)
block|,
operator|new
name|KeyValue
argument_list|(
name|row1
argument_list|,
name|fam2
argument_list|,
name|col4
argument_list|,
name|now
operator|-
literal|500
argument_list|,
name|data
argument_list|)
block|,
operator|new
name|KeyValue
argument_list|(
name|row1
argument_list|,
name|fam2
argument_list|,
name|col5
argument_list|,
name|now
operator|-
literal|10000
argument_list|,
name|data
argument_list|)
block|,
operator|new
name|KeyValue
argument_list|(
name|row2
argument_list|,
name|fam1
argument_list|,
name|col1
argument_list|,
name|now
operator|-
literal|10
argument_list|,
name|data
argument_list|)
block|}
decl_stmt|;
name|KeyValue
name|k
init|=
name|kvs
index|[
literal|0
index|]
decl_stmt|;
name|qm
operator|.
name|setRow
argument_list|(
name|k
operator|.
name|getRowArray
argument_list|()
argument_list|,
name|k
operator|.
name|getRowOffset
argument_list|()
argument_list|,
name|k
operator|.
name|getRowLength
argument_list|()
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|MatchCode
argument_list|>
name|actual
init|=
operator|new
name|ArrayList
argument_list|<
name|MatchCode
argument_list|>
argument_list|(
name|kvs
operator|.
name|length
argument_list|)
decl_stmt|;
for|for
control|(
name|KeyValue
name|kv
range|:
name|kvs
control|)
block|{
name|actual
operator|.
name|add
argument_list|(
name|qm
operator|.
name|match
argument_list|(
name|kv
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
if|if
condition|(
name|PRINT
condition|)
block|{
name|System
operator|.
name|out
operator|.
name|println
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
block|}
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
comment|/**    * Verify that {@link ScanQueryMatcher} only skips expired KeyValue    * instances and does not exit early from the row (skipping    * later non-expired KeyValues).  This version mimics a Get with    * wildcard-inferred column qualifiers.    *    * @throws IOException    */
specifier|public
name|void
name|testMatch_ExpiredWildcard
parameter_list|()
throws|throws
name|IOException
block|{
name|long
name|testTTL
init|=
literal|1000
decl_stmt|;
name|MatchCode
index|[]
name|expected
init|=
operator|new
name|MatchCode
index|[]
block|{
name|ScanQueryMatcher
operator|.
name|MatchCode
operator|.
name|INCLUDE
block|,
name|ScanQueryMatcher
operator|.
name|MatchCode
operator|.
name|INCLUDE
block|,
name|ScanQueryMatcher
operator|.
name|MatchCode
operator|.
name|SEEK_NEXT_COL
block|,
name|ScanQueryMatcher
operator|.
name|MatchCode
operator|.
name|INCLUDE
block|,
name|ScanQueryMatcher
operator|.
name|MatchCode
operator|.
name|SEEK_NEXT_COL
block|,
name|ScanQueryMatcher
operator|.
name|MatchCode
operator|.
name|DONE
block|}
decl_stmt|;
name|long
name|now
init|=
name|EnvironmentEdgeManager
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|ScanQueryMatcher
name|qm
init|=
operator|new
name|ScanQueryMatcher
argument_list|(
name|scan
argument_list|,
operator|new
name|ScanInfo
argument_list|(
name|fam2
argument_list|,
literal|0
argument_list|,
literal|1
argument_list|,
name|testTTL
argument_list|,
literal|false
argument_list|,
literal|0
argument_list|,
name|rowComparator
argument_list|)
argument_list|,
literal|null
argument_list|,
name|now
operator|-
name|testTTL
argument_list|)
decl_stmt|;
name|KeyValue
index|[]
name|kvs
init|=
operator|new
name|KeyValue
index|[]
block|{
operator|new
name|KeyValue
argument_list|(
name|row1
argument_list|,
name|fam2
argument_list|,
name|col1
argument_list|,
name|now
operator|-
literal|100
argument_list|,
name|data
argument_list|)
block|,
operator|new
name|KeyValue
argument_list|(
name|row1
argument_list|,
name|fam2
argument_list|,
name|col2
argument_list|,
name|now
operator|-
literal|50
argument_list|,
name|data
argument_list|)
block|,
operator|new
name|KeyValue
argument_list|(
name|row1
argument_list|,
name|fam2
argument_list|,
name|col3
argument_list|,
name|now
operator|-
literal|5000
argument_list|,
name|data
argument_list|)
block|,
operator|new
name|KeyValue
argument_list|(
name|row1
argument_list|,
name|fam2
argument_list|,
name|col4
argument_list|,
name|now
operator|-
literal|500
argument_list|,
name|data
argument_list|)
block|,
operator|new
name|KeyValue
argument_list|(
name|row1
argument_list|,
name|fam2
argument_list|,
name|col5
argument_list|,
name|now
operator|-
literal|10000
argument_list|,
name|data
argument_list|)
block|,
operator|new
name|KeyValue
argument_list|(
name|row2
argument_list|,
name|fam1
argument_list|,
name|col1
argument_list|,
name|now
operator|-
literal|10
argument_list|,
name|data
argument_list|)
block|}
decl_stmt|;
name|KeyValue
name|k
init|=
name|kvs
index|[
literal|0
index|]
decl_stmt|;
name|qm
operator|.
name|setRow
argument_list|(
name|k
operator|.
name|getRowArray
argument_list|()
argument_list|,
name|k
operator|.
name|getRowOffset
argument_list|()
argument_list|,
name|k
operator|.
name|getRowLength
argument_list|()
argument_list|)
expr_stmt|;
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
argument_list|<
name|ScanQueryMatcher
operator|.
name|MatchCode
argument_list|>
argument_list|(
name|kvs
operator|.
name|length
argument_list|)
decl_stmt|;
for|for
control|(
name|KeyValue
name|kv
range|:
name|kvs
control|)
block|{
name|actual
operator|.
name|add
argument_list|(
name|qm
operator|.
name|match
argument_list|(
name|kv
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
if|if
condition|(
name|PRINT
condition|)
block|{
name|System
operator|.
name|out
operator|.
name|println
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
block|}
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
name|currentTimeMillis
argument_list|()
decl_stmt|;
comment|// Set time to purge deletes to negative value to avoid it ever happening.
name|ScanInfo
name|scanInfo
init|=
operator|new
name|ScanInfo
argument_list|(
name|fam2
argument_list|,
literal|0
argument_list|,
literal|1
argument_list|,
name|ttl
argument_list|,
literal|false
argument_list|,
operator|-
literal|1L
argument_list|,
name|rowComparator
argument_list|)
decl_stmt|;
name|NavigableSet
argument_list|<
name|byte
index|[]
argument_list|>
name|cols
init|=
name|get
operator|.
name|getFamilyMap
argument_list|()
operator|.
name|get
argument_list|(
name|fam2
argument_list|)
decl_stmt|;
name|ScanQueryMatcher
name|qm
init|=
operator|new
name|ScanQueryMatcher
argument_list|(
name|scan
argument_list|,
name|scanInfo
argument_list|,
name|cols
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
name|from
argument_list|,
name|to
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
argument_list|<
name|ScanQueryMatcher
operator|.
name|MatchCode
argument_list|>
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
name|setRow
argument_list|(
name|row
argument_list|,
literal|0
argument_list|,
operator|(
name|short
operator|)
name|row
operator|.
name|length
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
if|if
condition|(
name|PRINT
condition|)
name|System
operator|.
name|out
operator|.
name|println
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

