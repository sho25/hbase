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
name|Collections
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Iterator
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
name|CollectionBackedScanner
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
name|TestKeyValueHeap
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
name|List
argument_list|<
name|KeyValueScanner
argument_list|>
name|scanners
init|=
operator|new
name|ArrayList
argument_list|<
name|KeyValueScanner
argument_list|>
argument_list|()
decl_stmt|;
specifier|private
name|byte
index|[]
name|row1
decl_stmt|;
specifier|private
name|byte
index|[]
name|fam1
decl_stmt|;
specifier|private
name|byte
index|[]
name|col1
decl_stmt|;
specifier|private
name|byte
index|[]
name|data
decl_stmt|;
specifier|private
name|byte
index|[]
name|row2
decl_stmt|;
specifier|private
name|byte
index|[]
name|fam2
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
name|data
operator|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"data"
argument_list|)
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
name|fam1
operator|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"fam1"
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
name|row2
operator|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"row2"
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
block|}
specifier|public
name|void
name|testSorted
parameter_list|()
throws|throws
name|IOException
block|{
comment|//Cases that need to be checked are:
comment|//1. The "smallest" KeyValue is in the same scanners as current
comment|//2. Current scanner gets empty
name|List
argument_list|<
name|KeyValue
argument_list|>
name|l1
init|=
operator|new
name|ArrayList
argument_list|<
name|KeyValue
argument_list|>
argument_list|()
decl_stmt|;
name|l1
operator|.
name|add
argument_list|(
operator|new
name|KeyValue
argument_list|(
name|row1
argument_list|,
name|fam1
argument_list|,
name|col5
argument_list|,
name|data
argument_list|)
argument_list|)
expr_stmt|;
name|l1
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
name|l1
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
name|col2
argument_list|,
name|data
argument_list|)
argument_list|)
expr_stmt|;
name|scanners
operator|.
name|add
argument_list|(
operator|new
name|Scanner
argument_list|(
name|l1
argument_list|)
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|KeyValue
argument_list|>
name|l2
init|=
operator|new
name|ArrayList
argument_list|<
name|KeyValue
argument_list|>
argument_list|()
decl_stmt|;
name|l2
operator|.
name|add
argument_list|(
operator|new
name|KeyValue
argument_list|(
name|row1
argument_list|,
name|fam1
argument_list|,
name|col1
argument_list|,
name|data
argument_list|)
argument_list|)
expr_stmt|;
name|l2
operator|.
name|add
argument_list|(
operator|new
name|KeyValue
argument_list|(
name|row1
argument_list|,
name|fam1
argument_list|,
name|col2
argument_list|,
name|data
argument_list|)
argument_list|)
expr_stmt|;
name|scanners
operator|.
name|add
argument_list|(
operator|new
name|Scanner
argument_list|(
name|l2
argument_list|)
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|KeyValue
argument_list|>
name|l3
init|=
operator|new
name|ArrayList
argument_list|<
name|KeyValue
argument_list|>
argument_list|()
decl_stmt|;
name|l3
operator|.
name|add
argument_list|(
operator|new
name|KeyValue
argument_list|(
name|row1
argument_list|,
name|fam1
argument_list|,
name|col3
argument_list|,
name|data
argument_list|)
argument_list|)
expr_stmt|;
name|l3
operator|.
name|add
argument_list|(
operator|new
name|KeyValue
argument_list|(
name|row1
argument_list|,
name|fam1
argument_list|,
name|col4
argument_list|,
name|data
argument_list|)
argument_list|)
expr_stmt|;
name|l3
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
name|data
argument_list|)
argument_list|)
expr_stmt|;
name|l3
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
name|data
argument_list|)
argument_list|)
expr_stmt|;
name|l3
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
name|col3
argument_list|,
name|data
argument_list|)
argument_list|)
expr_stmt|;
name|scanners
operator|.
name|add
argument_list|(
operator|new
name|Scanner
argument_list|(
name|l3
argument_list|)
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|KeyValue
argument_list|>
name|expected
init|=
operator|new
name|ArrayList
argument_list|<
name|KeyValue
argument_list|>
argument_list|()
decl_stmt|;
name|expected
operator|.
name|add
argument_list|(
operator|new
name|KeyValue
argument_list|(
name|row1
argument_list|,
name|fam1
argument_list|,
name|col1
argument_list|,
name|data
argument_list|)
argument_list|)
expr_stmt|;
name|expected
operator|.
name|add
argument_list|(
operator|new
name|KeyValue
argument_list|(
name|row1
argument_list|,
name|fam1
argument_list|,
name|col2
argument_list|,
name|data
argument_list|)
argument_list|)
expr_stmt|;
name|expected
operator|.
name|add
argument_list|(
operator|new
name|KeyValue
argument_list|(
name|row1
argument_list|,
name|fam1
argument_list|,
name|col3
argument_list|,
name|data
argument_list|)
argument_list|)
expr_stmt|;
name|expected
operator|.
name|add
argument_list|(
operator|new
name|KeyValue
argument_list|(
name|row1
argument_list|,
name|fam1
argument_list|,
name|col4
argument_list|,
name|data
argument_list|)
argument_list|)
expr_stmt|;
name|expected
operator|.
name|add
argument_list|(
operator|new
name|KeyValue
argument_list|(
name|row1
argument_list|,
name|fam1
argument_list|,
name|col5
argument_list|,
name|data
argument_list|)
argument_list|)
expr_stmt|;
name|expected
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
name|data
argument_list|)
argument_list|)
expr_stmt|;
name|expected
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
name|data
argument_list|)
argument_list|)
expr_stmt|;
name|expected
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
name|expected
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
name|col2
argument_list|,
name|data
argument_list|)
argument_list|)
expr_stmt|;
name|expected
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
name|col3
argument_list|,
name|data
argument_list|)
argument_list|)
expr_stmt|;
comment|//Creating KeyValueHeap
name|KeyValueHeap
name|kvh
init|=
operator|new
name|KeyValueHeap
argument_list|(
name|scanners
argument_list|,
name|KeyValue
operator|.
name|COMPARATOR
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|KeyValue
argument_list|>
name|actual
init|=
operator|new
name|ArrayList
argument_list|<
name|KeyValue
argument_list|>
argument_list|()
decl_stmt|;
while|while
condition|(
name|kvh
operator|.
name|peek
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|actual
operator|.
name|add
argument_list|(
name|kvh
operator|.
name|next
argument_list|()
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
literal|"\nactual   "
operator|+
name|actual
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|+
literal|"\n"
argument_list|)
expr_stmt|;
block|}
block|}
comment|//Check if result is sorted according to Comparator
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|actual
operator|.
name|size
argument_list|()
operator|-
literal|1
condition|;
name|i
operator|++
control|)
block|{
name|int
name|ret
init|=
name|KeyValue
operator|.
name|COMPARATOR
operator|.
name|compare
argument_list|(
name|actual
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
operator|+
literal|1
argument_list|)
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|ret
operator|<
literal|0
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|void
name|testSeek
parameter_list|()
throws|throws
name|IOException
block|{
comment|//Cases:
comment|//1. Seek KeyValue that is not in scanner
comment|//2. Check that smallest that is returned from a seek is correct
name|List
argument_list|<
name|KeyValue
argument_list|>
name|l1
init|=
operator|new
name|ArrayList
argument_list|<
name|KeyValue
argument_list|>
argument_list|()
decl_stmt|;
name|l1
operator|.
name|add
argument_list|(
operator|new
name|KeyValue
argument_list|(
name|row1
argument_list|,
name|fam1
argument_list|,
name|col5
argument_list|,
name|data
argument_list|)
argument_list|)
expr_stmt|;
name|l1
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
name|l1
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
name|col2
argument_list|,
name|data
argument_list|)
argument_list|)
expr_stmt|;
name|scanners
operator|.
name|add
argument_list|(
operator|new
name|Scanner
argument_list|(
name|l1
argument_list|)
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|KeyValue
argument_list|>
name|l2
init|=
operator|new
name|ArrayList
argument_list|<
name|KeyValue
argument_list|>
argument_list|()
decl_stmt|;
name|l2
operator|.
name|add
argument_list|(
operator|new
name|KeyValue
argument_list|(
name|row1
argument_list|,
name|fam1
argument_list|,
name|col1
argument_list|,
name|data
argument_list|)
argument_list|)
expr_stmt|;
name|l2
operator|.
name|add
argument_list|(
operator|new
name|KeyValue
argument_list|(
name|row1
argument_list|,
name|fam1
argument_list|,
name|col2
argument_list|,
name|data
argument_list|)
argument_list|)
expr_stmt|;
name|scanners
operator|.
name|add
argument_list|(
operator|new
name|Scanner
argument_list|(
name|l2
argument_list|)
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|KeyValue
argument_list|>
name|l3
init|=
operator|new
name|ArrayList
argument_list|<
name|KeyValue
argument_list|>
argument_list|()
decl_stmt|;
name|l3
operator|.
name|add
argument_list|(
operator|new
name|KeyValue
argument_list|(
name|row1
argument_list|,
name|fam1
argument_list|,
name|col3
argument_list|,
name|data
argument_list|)
argument_list|)
expr_stmt|;
name|l3
operator|.
name|add
argument_list|(
operator|new
name|KeyValue
argument_list|(
name|row1
argument_list|,
name|fam1
argument_list|,
name|col4
argument_list|,
name|data
argument_list|)
argument_list|)
expr_stmt|;
name|l3
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
name|data
argument_list|)
argument_list|)
expr_stmt|;
name|l3
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
name|data
argument_list|)
argument_list|)
expr_stmt|;
name|l3
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
name|col3
argument_list|,
name|data
argument_list|)
argument_list|)
expr_stmt|;
name|scanners
operator|.
name|add
argument_list|(
operator|new
name|Scanner
argument_list|(
name|l3
argument_list|)
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|KeyValue
argument_list|>
name|expected
init|=
operator|new
name|ArrayList
argument_list|<
name|KeyValue
argument_list|>
argument_list|()
decl_stmt|;
name|expected
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
comment|//Creating KeyValueHeap
name|KeyValueHeap
name|kvh
init|=
operator|new
name|KeyValueHeap
argument_list|(
name|scanners
argument_list|,
name|KeyValue
operator|.
name|COMPARATOR
argument_list|)
decl_stmt|;
name|KeyValue
name|seekKv
init|=
operator|new
name|KeyValue
argument_list|(
name|row2
argument_list|,
name|fam1
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|kvh
operator|.
name|seek
argument_list|(
name|seekKv
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|KeyValue
argument_list|>
name|actual
init|=
operator|new
name|ArrayList
argument_list|<
name|KeyValue
argument_list|>
argument_list|()
decl_stmt|;
name|actual
operator|.
name|add
argument_list|(
name|kvh
operator|.
name|peek
argument_list|()
argument_list|)
expr_stmt|;
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
literal|"\nactual   "
operator|+
name|actual
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|+
literal|"\n"
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|public
name|void
name|testScannerLeak
parameter_list|()
throws|throws
name|IOException
block|{
comment|// Test for unclosed scanners (HBASE-1927)
name|List
argument_list|<
name|KeyValue
argument_list|>
name|l1
init|=
operator|new
name|ArrayList
argument_list|<
name|KeyValue
argument_list|>
argument_list|()
decl_stmt|;
name|l1
operator|.
name|add
argument_list|(
operator|new
name|KeyValue
argument_list|(
name|row1
argument_list|,
name|fam1
argument_list|,
name|col5
argument_list|,
name|data
argument_list|)
argument_list|)
expr_stmt|;
name|l1
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
name|l1
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
name|col2
argument_list|,
name|data
argument_list|)
argument_list|)
expr_stmt|;
name|scanners
operator|.
name|add
argument_list|(
operator|new
name|Scanner
argument_list|(
name|l1
argument_list|)
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|KeyValue
argument_list|>
name|l2
init|=
operator|new
name|ArrayList
argument_list|<
name|KeyValue
argument_list|>
argument_list|()
decl_stmt|;
name|l2
operator|.
name|add
argument_list|(
operator|new
name|KeyValue
argument_list|(
name|row1
argument_list|,
name|fam1
argument_list|,
name|col1
argument_list|,
name|data
argument_list|)
argument_list|)
expr_stmt|;
name|l2
operator|.
name|add
argument_list|(
operator|new
name|KeyValue
argument_list|(
name|row1
argument_list|,
name|fam1
argument_list|,
name|col2
argument_list|,
name|data
argument_list|)
argument_list|)
expr_stmt|;
name|scanners
operator|.
name|add
argument_list|(
operator|new
name|Scanner
argument_list|(
name|l2
argument_list|)
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|KeyValue
argument_list|>
name|l3
init|=
operator|new
name|ArrayList
argument_list|<
name|KeyValue
argument_list|>
argument_list|()
decl_stmt|;
name|l3
operator|.
name|add
argument_list|(
operator|new
name|KeyValue
argument_list|(
name|row1
argument_list|,
name|fam1
argument_list|,
name|col3
argument_list|,
name|data
argument_list|)
argument_list|)
expr_stmt|;
name|l3
operator|.
name|add
argument_list|(
operator|new
name|KeyValue
argument_list|(
name|row1
argument_list|,
name|fam1
argument_list|,
name|col4
argument_list|,
name|data
argument_list|)
argument_list|)
expr_stmt|;
name|l3
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
name|data
argument_list|)
argument_list|)
expr_stmt|;
name|l3
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
name|data
argument_list|)
argument_list|)
expr_stmt|;
name|l3
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
name|col3
argument_list|,
name|data
argument_list|)
argument_list|)
expr_stmt|;
name|scanners
operator|.
name|add
argument_list|(
operator|new
name|Scanner
argument_list|(
name|l3
argument_list|)
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|KeyValue
argument_list|>
name|l4
init|=
operator|new
name|ArrayList
argument_list|<
name|KeyValue
argument_list|>
argument_list|()
decl_stmt|;
name|scanners
operator|.
name|add
argument_list|(
operator|new
name|Scanner
argument_list|(
name|l4
argument_list|)
argument_list|)
expr_stmt|;
comment|//Creating KeyValueHeap
name|KeyValueHeap
name|kvh
init|=
operator|new
name|KeyValueHeap
argument_list|(
name|scanners
argument_list|,
name|KeyValue
operator|.
name|COMPARATOR
argument_list|)
decl_stmt|;
while|while
condition|(
name|kvh
operator|.
name|next
argument_list|()
operator|!=
literal|null
condition|)
empty_stmt|;
for|for
control|(
name|KeyValueScanner
name|scanner
range|:
name|scanners
control|)
block|{
name|assertTrue
argument_list|(
operator|(
operator|(
name|Scanner
operator|)
name|scanner
operator|)
operator|.
name|isClosed
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
specifier|static
class|class
name|Scanner
extends|extends
name|CollectionBackedScanner
block|{
specifier|private
name|Iterator
argument_list|<
name|KeyValue
argument_list|>
name|iter
decl_stmt|;
specifier|private
name|KeyValue
name|current
decl_stmt|;
specifier|private
name|boolean
name|closed
init|=
literal|false
decl_stmt|;
specifier|public
name|Scanner
parameter_list|(
name|List
argument_list|<
name|KeyValue
argument_list|>
name|list
parameter_list|)
block|{
name|super
argument_list|(
name|list
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|close
parameter_list|()
block|{
name|closed
operator|=
literal|true
expr_stmt|;
block|}
specifier|public
name|boolean
name|isClosed
parameter_list|()
block|{
return|return
name|closed
return|;
block|}
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

