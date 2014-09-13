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
name|java
operator|.
name|util
operator|.
name|TreeSet
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
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
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
name|TestExplicitColumnTracker
block|{
specifier|private
specifier|final
name|byte
index|[]
name|col1
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"col1"
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|byte
index|[]
name|col2
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"col2"
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|byte
index|[]
name|col3
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"col3"
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|byte
index|[]
name|col4
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"col4"
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|byte
index|[]
name|col5
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"col5"
argument_list|)
decl_stmt|;
specifier|private
name|void
name|runTest
parameter_list|(
name|int
name|maxVersions
parameter_list|,
name|TreeSet
argument_list|<
name|byte
index|[]
argument_list|>
name|trackColumns
parameter_list|,
name|List
argument_list|<
name|byte
index|[]
argument_list|>
name|scannerColumns
parameter_list|,
name|List
argument_list|<
name|MatchCode
argument_list|>
name|expected
parameter_list|,
name|int
name|lookAhead
parameter_list|)
throws|throws
name|IOException
block|{
name|ColumnTracker
name|exp
init|=
operator|new
name|ExplicitColumnTracker
argument_list|(
name|trackColumns
argument_list|,
literal|0
argument_list|,
name|maxVersions
argument_list|,
name|Long
operator|.
name|MIN_VALUE
argument_list|,
name|lookAhead
argument_list|)
decl_stmt|;
comment|//Initialize result
name|List
argument_list|<
name|ScanQueryMatcher
operator|.
name|MatchCode
argument_list|>
name|result
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
name|long
name|timestamp
init|=
literal|0
decl_stmt|;
comment|//"Match"
for|for
control|(
name|byte
index|[]
name|col
range|:
name|scannerColumns
control|)
block|{
name|result
operator|.
name|add
argument_list|(
name|ScanQueryMatcher
operator|.
name|checkColumn
argument_list|(
name|exp
argument_list|,
name|col
argument_list|,
literal|0
argument_list|,
name|col
operator|.
name|length
argument_list|,
operator|++
name|timestamp
argument_list|,
name|KeyValue
operator|.
name|Type
operator|.
name|Put
operator|.
name|getCode
argument_list|()
argument_list|,
literal|false
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
name|result
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
name|result
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testGet_SingleVersion
parameter_list|()
throws|throws
name|IOException
block|{
comment|//Create tracker
name|TreeSet
argument_list|<
name|byte
index|[]
argument_list|>
name|columns
init|=
operator|new
name|TreeSet
argument_list|<
name|byte
index|[]
argument_list|>
argument_list|(
name|Bytes
operator|.
name|BYTES_COMPARATOR
argument_list|)
decl_stmt|;
comment|//Looking for every other
name|columns
operator|.
name|add
argument_list|(
name|col2
argument_list|)
expr_stmt|;
name|columns
operator|.
name|add
argument_list|(
name|col4
argument_list|)
expr_stmt|;
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
comment|// col1
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
comment|// col2
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
comment|// col3
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
comment|// col4
name|expected
operator|.
name|add
argument_list|(
name|ScanQueryMatcher
operator|.
name|MatchCode
operator|.
name|SEEK_NEXT_ROW
argument_list|)
expr_stmt|;
comment|// col5
name|int
name|maxVersions
init|=
literal|1
decl_stmt|;
comment|//Create "Scanner"
name|List
argument_list|<
name|byte
index|[]
argument_list|>
name|scanner
init|=
operator|new
name|ArrayList
argument_list|<
name|byte
index|[]
argument_list|>
argument_list|()
decl_stmt|;
name|scanner
operator|.
name|add
argument_list|(
name|col1
argument_list|)
expr_stmt|;
name|scanner
operator|.
name|add
argument_list|(
name|col2
argument_list|)
expr_stmt|;
name|scanner
operator|.
name|add
argument_list|(
name|col3
argument_list|)
expr_stmt|;
name|scanner
operator|.
name|add
argument_list|(
name|col4
argument_list|)
expr_stmt|;
name|scanner
operator|.
name|add
argument_list|(
name|col5
argument_list|)
expr_stmt|;
name|runTest
argument_list|(
name|maxVersions
argument_list|,
name|columns
argument_list|,
name|scanner
argument_list|,
name|expected
argument_list|,
literal|0
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testGet_MultiVersion
parameter_list|()
throws|throws
name|IOException
block|{
comment|//Create tracker
name|TreeSet
argument_list|<
name|byte
index|[]
argument_list|>
name|columns
init|=
operator|new
name|TreeSet
argument_list|<
name|byte
index|[]
argument_list|>
argument_list|(
name|Bytes
operator|.
name|BYTES_COMPARATOR
argument_list|)
decl_stmt|;
comment|//Looking for every other
name|columns
operator|.
name|add
argument_list|(
name|col2
argument_list|)
expr_stmt|;
name|columns
operator|.
name|add
argument_list|(
name|col4
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|ScanQueryMatcher
operator|.
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
name|INCLUDE
argument_list|)
expr_stmt|;
comment|// col2; 1st version
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
comment|// col2; 2nd version
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
name|INCLUDE
argument_list|)
expr_stmt|;
comment|// col4; 1st version
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
comment|// col4; 2nd version
name|expected
operator|.
name|add
argument_list|(
name|ScanQueryMatcher
operator|.
name|MatchCode
operator|.
name|SEEK_NEXT_ROW
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
name|SEEK_NEXT_ROW
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
name|SEEK_NEXT_ROW
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
name|SEEK_NEXT_ROW
argument_list|)
expr_stmt|;
name|int
name|maxVersions
init|=
literal|2
decl_stmt|;
comment|//Create "Scanner"
name|List
argument_list|<
name|byte
index|[]
argument_list|>
name|scanner
init|=
operator|new
name|ArrayList
argument_list|<
name|byte
index|[]
argument_list|>
argument_list|()
decl_stmt|;
name|scanner
operator|.
name|add
argument_list|(
name|col1
argument_list|)
expr_stmt|;
name|scanner
operator|.
name|add
argument_list|(
name|col1
argument_list|)
expr_stmt|;
name|scanner
operator|.
name|add
argument_list|(
name|col1
argument_list|)
expr_stmt|;
name|scanner
operator|.
name|add
argument_list|(
name|col2
argument_list|)
expr_stmt|;
name|scanner
operator|.
name|add
argument_list|(
name|col2
argument_list|)
expr_stmt|;
name|scanner
operator|.
name|add
argument_list|(
name|col2
argument_list|)
expr_stmt|;
name|scanner
operator|.
name|add
argument_list|(
name|col3
argument_list|)
expr_stmt|;
name|scanner
operator|.
name|add
argument_list|(
name|col3
argument_list|)
expr_stmt|;
name|scanner
operator|.
name|add
argument_list|(
name|col3
argument_list|)
expr_stmt|;
name|scanner
operator|.
name|add
argument_list|(
name|col4
argument_list|)
expr_stmt|;
name|scanner
operator|.
name|add
argument_list|(
name|col4
argument_list|)
expr_stmt|;
name|scanner
operator|.
name|add
argument_list|(
name|col4
argument_list|)
expr_stmt|;
name|scanner
operator|.
name|add
argument_list|(
name|col5
argument_list|)
expr_stmt|;
name|scanner
operator|.
name|add
argument_list|(
name|col5
argument_list|)
expr_stmt|;
name|scanner
operator|.
name|add
argument_list|(
name|col5
argument_list|)
expr_stmt|;
comment|//Initialize result
name|runTest
argument_list|(
name|maxVersions
argument_list|,
name|columns
argument_list|,
name|scanner
argument_list|,
name|expected
argument_list|,
literal|0
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testGet_MultiVersionWithLookAhead
parameter_list|()
throws|throws
name|IOException
block|{
comment|//Create tracker
name|TreeSet
argument_list|<
name|byte
index|[]
argument_list|>
name|columns
init|=
operator|new
name|TreeSet
argument_list|<
name|byte
index|[]
argument_list|>
argument_list|(
name|Bytes
operator|.
name|BYTES_COMPARATOR
argument_list|)
decl_stmt|;
comment|//Looking for every other
name|columns
operator|.
name|add
argument_list|(
name|col2
argument_list|)
expr_stmt|;
name|columns
operator|.
name|add
argument_list|(
name|col4
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|ScanQueryMatcher
operator|.
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
name|INCLUDE
argument_list|)
expr_stmt|;
comment|// col2; 1st version
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
comment|// col2; 2nd version
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
name|INCLUDE
argument_list|)
expr_stmt|;
comment|// col4; 1st version
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
comment|// col4; 2nd version
name|expected
operator|.
name|add
argument_list|(
name|ScanQueryMatcher
operator|.
name|MatchCode
operator|.
name|SEEK_NEXT_ROW
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
name|SEEK_NEXT_ROW
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
name|SEEK_NEXT_ROW
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
name|SEEK_NEXT_ROW
argument_list|)
expr_stmt|;
name|int
name|maxVersions
init|=
literal|2
decl_stmt|;
comment|//Create "Scanner"
name|List
argument_list|<
name|byte
index|[]
argument_list|>
name|scanner
init|=
operator|new
name|ArrayList
argument_list|<
name|byte
index|[]
argument_list|>
argument_list|()
decl_stmt|;
name|scanner
operator|.
name|add
argument_list|(
name|col1
argument_list|)
expr_stmt|;
name|scanner
operator|.
name|add
argument_list|(
name|col1
argument_list|)
expr_stmt|;
name|scanner
operator|.
name|add
argument_list|(
name|col1
argument_list|)
expr_stmt|;
name|scanner
operator|.
name|add
argument_list|(
name|col2
argument_list|)
expr_stmt|;
name|scanner
operator|.
name|add
argument_list|(
name|col2
argument_list|)
expr_stmt|;
name|scanner
operator|.
name|add
argument_list|(
name|col2
argument_list|)
expr_stmt|;
name|scanner
operator|.
name|add
argument_list|(
name|col3
argument_list|)
expr_stmt|;
name|scanner
operator|.
name|add
argument_list|(
name|col3
argument_list|)
expr_stmt|;
name|scanner
operator|.
name|add
argument_list|(
name|col3
argument_list|)
expr_stmt|;
name|scanner
operator|.
name|add
argument_list|(
name|col4
argument_list|)
expr_stmt|;
name|scanner
operator|.
name|add
argument_list|(
name|col4
argument_list|)
expr_stmt|;
name|scanner
operator|.
name|add
argument_list|(
name|col4
argument_list|)
expr_stmt|;
name|scanner
operator|.
name|add
argument_list|(
name|col5
argument_list|)
expr_stmt|;
name|scanner
operator|.
name|add
argument_list|(
name|col5
argument_list|)
expr_stmt|;
name|scanner
operator|.
name|add
argument_list|(
name|col5
argument_list|)
expr_stmt|;
comment|//Initialize result
name|runTest
argument_list|(
name|maxVersions
argument_list|,
name|columns
argument_list|,
name|scanner
argument_list|,
name|expected
argument_list|,
literal|2
argument_list|)
expr_stmt|;
block|}
comment|/**    * hbase-2259    */
annotation|@
name|Test
specifier|public
name|void
name|testStackOverflow
parameter_list|()
throws|throws
name|IOException
block|{
name|int
name|maxVersions
init|=
literal|1
decl_stmt|;
name|TreeSet
argument_list|<
name|byte
index|[]
argument_list|>
name|columns
init|=
operator|new
name|TreeSet
argument_list|<
name|byte
index|[]
argument_list|>
argument_list|(
name|Bytes
operator|.
name|BYTES_COMPARATOR
argument_list|)
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
literal|100000
condition|;
name|i
operator|++
control|)
block|{
name|columns
operator|.
name|add
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"col"
operator|+
name|i
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|ColumnTracker
name|explicit
init|=
operator|new
name|ExplicitColumnTracker
argument_list|(
name|columns
argument_list|,
literal|0
argument_list|,
name|maxVersions
argument_list|,
name|Long
operator|.
name|MIN_VALUE
argument_list|,
literal|0
argument_list|)
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
literal|100000
condition|;
name|i
operator|+=
literal|2
control|)
block|{
name|byte
index|[]
name|col
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"col"
operator|+
name|i
argument_list|)
decl_stmt|;
name|ScanQueryMatcher
operator|.
name|checkColumn
argument_list|(
name|explicit
argument_list|,
name|col
argument_list|,
literal|0
argument_list|,
name|col
operator|.
name|length
argument_list|,
literal|1
argument_list|,
name|KeyValue
operator|.
name|Type
operator|.
name|Put
operator|.
name|getCode
argument_list|()
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
name|explicit
operator|.
name|reset
argument_list|()
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|1
init|;
name|i
operator|<
literal|100000
condition|;
name|i
operator|+=
literal|2
control|)
block|{
name|byte
index|[]
name|col
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"col"
operator|+
name|i
argument_list|)
decl_stmt|;
name|ScanQueryMatcher
operator|.
name|checkColumn
argument_list|(
name|explicit
argument_list|,
name|col
argument_list|,
literal|0
argument_list|,
name|col
operator|.
name|length
argument_list|,
literal|1
argument_list|,
name|KeyValue
operator|.
name|Type
operator|.
name|Put
operator|.
name|getCode
argument_list|()
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Regression test for HBASE-2545    */
annotation|@
name|Test
specifier|public
name|void
name|testInfiniteLoop
parameter_list|()
throws|throws
name|IOException
block|{
name|TreeSet
argument_list|<
name|byte
index|[]
argument_list|>
name|columns
init|=
operator|new
name|TreeSet
argument_list|<
name|byte
index|[]
argument_list|>
argument_list|(
name|Bytes
operator|.
name|BYTES_COMPARATOR
argument_list|)
decl_stmt|;
name|columns
operator|.
name|addAll
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
operator|new
name|byte
index|[]
index|[]
block|{
name|col2
block|,
name|col3
block|,
name|col5
block|}
argument_list|)
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|byte
index|[]
argument_list|>
name|scanner
init|=
name|Arrays
operator|.
expr|<
name|byte
index|[]
operator|>
name|asList
argument_list|(
operator|new
name|byte
index|[]
index|[]
block|{
name|col1
block|,
name|col4
block|}
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|ScanQueryMatcher
operator|.
name|MatchCode
argument_list|>
name|expected
init|=
name|Arrays
operator|.
expr|<
name|ScanQueryMatcher
operator|.
name|MatchCode
operator|>
name|asList
argument_list|(
operator|new
name|ScanQueryMatcher
operator|.
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
name|SEEK_NEXT_COL
block|}
argument_list|)
decl_stmt|;
name|runTest
argument_list|(
literal|1
argument_list|,
name|columns
argument_list|,
name|scanner
argument_list|,
name|expected
argument_list|,
literal|0
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

