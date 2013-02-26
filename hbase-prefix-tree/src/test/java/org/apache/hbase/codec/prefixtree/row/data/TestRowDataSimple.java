begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
end_comment

begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hbase
operator|.
name|codec
operator|.
name|prefixtree
operator|.
name|row
operator|.
name|data
package|;
end_package

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
name|Cell
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
name|CellComparator
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
name|CollectionUtils
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hbase
operator|.
name|codec
operator|.
name|prefixtree
operator|.
name|row
operator|.
name|BaseTestRowData
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hbase
operator|.
name|codec
operator|.
name|prefixtree
operator|.
name|scanner
operator|.
name|CellScannerPosition
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hbase
operator|.
name|codec
operator|.
name|prefixtree
operator|.
name|scanner
operator|.
name|CellSearcher
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Assert
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Lists
import|;
end_import

begin_class
specifier|public
class|class
name|TestRowDataSimple
extends|extends
name|BaseTestRowData
block|{
specifier|static
name|byte
index|[]
comment|// don't let the rows share any common prefix bytes
name|rowA
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"Arow"
argument_list|)
decl_stmt|,
name|rowB
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"Brow"
argument_list|)
decl_stmt|,
name|cf
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"fam"
argument_list|)
decl_stmt|,
name|cq0
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"cq0"
argument_list|)
decl_stmt|,
name|cq1
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"cq1tail"
argument_list|)
decl_stmt|,
comment|// make sure tail does not come back as liat
name|cq2
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"dcq2"
argument_list|)
decl_stmt|,
comment|// start with a different character
name|v0
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"v0"
argument_list|)
decl_stmt|;
specifier|static
name|long
name|ts
init|=
literal|55L
decl_stmt|;
specifier|static
name|List
argument_list|<
name|KeyValue
argument_list|>
name|d
init|=
name|Lists
operator|.
name|newArrayList
argument_list|()
decl_stmt|;
static|static
block|{
name|d
operator|.
name|add
argument_list|(
operator|new
name|KeyValue
argument_list|(
name|rowA
argument_list|,
name|cf
argument_list|,
name|cq0
argument_list|,
name|ts
argument_list|,
name|v0
argument_list|)
argument_list|)
expr_stmt|;
name|d
operator|.
name|add
argument_list|(
operator|new
name|KeyValue
argument_list|(
name|rowA
argument_list|,
name|cf
argument_list|,
name|cq1
argument_list|,
name|ts
argument_list|,
name|v0
argument_list|)
argument_list|)
expr_stmt|;
name|d
operator|.
name|add
argument_list|(
operator|new
name|KeyValue
argument_list|(
name|rowA
argument_list|,
name|cf
argument_list|,
name|cq2
argument_list|,
name|ts
argument_list|,
name|v0
argument_list|)
argument_list|)
expr_stmt|;
name|d
operator|.
name|add
argument_list|(
operator|new
name|KeyValue
argument_list|(
name|rowB
argument_list|,
name|cf
argument_list|,
name|cq0
argument_list|,
name|ts
argument_list|,
name|v0
argument_list|)
argument_list|)
expr_stmt|;
name|d
operator|.
name|add
argument_list|(
operator|new
name|KeyValue
argument_list|(
name|rowB
argument_list|,
name|cf
argument_list|,
name|cq1
argument_list|,
name|ts
argument_list|,
name|v0
argument_list|)
argument_list|)
expr_stmt|;
name|d
operator|.
name|add
argument_list|(
operator|new
name|KeyValue
argument_list|(
name|rowB
argument_list|,
name|cf
argument_list|,
name|cq2
argument_list|,
name|ts
argument_list|,
name|v0
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|KeyValue
argument_list|>
name|getInputs
parameter_list|()
block|{
return|return
name|d
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|individualSearcherAssertions
parameter_list|(
name|CellSearcher
name|searcher
parameter_list|)
block|{
name|CellScannerPosition
name|p
decl_stmt|;
comment|// reuse
name|searcher
operator|.
name|resetToBeforeFirstEntry
argument_list|()
expr_stmt|;
comment|// test first cell
name|searcher
operator|.
name|advance
argument_list|()
expr_stmt|;
name|Cell
name|first
init|=
name|searcher
operator|.
name|current
argument_list|()
decl_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|CellComparator
operator|.
name|equals
argument_list|(
name|d
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|,
name|first
argument_list|)
argument_list|)
expr_stmt|;
comment|// test first cell in second row
name|Assert
operator|.
name|assertTrue
argument_list|(
name|searcher
operator|.
name|positionAt
argument_list|(
name|d
operator|.
name|get
argument_list|(
literal|3
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|CellComparator
operator|.
name|equals
argument_list|(
name|d
operator|.
name|get
argument_list|(
literal|3
argument_list|)
argument_list|,
name|searcher
operator|.
name|current
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|Cell
name|between4And5
init|=
operator|new
name|KeyValue
argument_list|(
name|rowB
argument_list|,
name|cf
argument_list|,
name|cq1
argument_list|,
name|ts
operator|-
literal|2
argument_list|,
name|v0
argument_list|)
decl_stmt|;
comment|// test exact
name|Assert
operator|.
name|assertFalse
argument_list|(
name|searcher
operator|.
name|positionAt
argument_list|(
name|between4And5
argument_list|)
argument_list|)
expr_stmt|;
comment|// test atOrBefore
name|p
operator|=
name|searcher
operator|.
name|positionAtOrBefore
argument_list|(
name|between4And5
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|CellScannerPosition
operator|.
name|BEFORE
argument_list|,
name|p
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|CellComparator
operator|.
name|equals
argument_list|(
name|searcher
operator|.
name|current
argument_list|()
argument_list|,
name|d
operator|.
name|get
argument_list|(
literal|4
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
comment|// test atOrAfter
name|p
operator|=
name|searcher
operator|.
name|positionAtOrAfter
argument_list|(
name|between4And5
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|CellScannerPosition
operator|.
name|AFTER
argument_list|,
name|p
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|CellComparator
operator|.
name|equals
argument_list|(
name|searcher
operator|.
name|current
argument_list|()
argument_list|,
name|d
operator|.
name|get
argument_list|(
literal|5
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
comment|// test when key falls before first key in block
name|Cell
name|beforeFirst
init|=
operator|new
name|KeyValue
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"A"
argument_list|)
argument_list|,
name|cf
argument_list|,
name|cq0
argument_list|,
name|ts
argument_list|,
name|v0
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertFalse
argument_list|(
name|searcher
operator|.
name|positionAt
argument_list|(
name|beforeFirst
argument_list|)
argument_list|)
expr_stmt|;
name|p
operator|=
name|searcher
operator|.
name|positionAtOrBefore
argument_list|(
name|beforeFirst
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|CellScannerPosition
operator|.
name|BEFORE_FIRST
argument_list|,
name|p
argument_list|)
expr_stmt|;
name|p
operator|=
name|searcher
operator|.
name|positionAtOrAfter
argument_list|(
name|beforeFirst
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|CellScannerPosition
operator|.
name|AFTER
argument_list|,
name|p
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|CellComparator
operator|.
name|equals
argument_list|(
name|searcher
operator|.
name|current
argument_list|()
argument_list|,
name|d
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|d
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|,
name|searcher
operator|.
name|current
argument_list|()
argument_list|)
expr_stmt|;
comment|// test when key falls after last key in block
name|Cell
name|afterLast
init|=
operator|new
name|KeyValue
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"z"
argument_list|)
argument_list|,
name|cf
argument_list|,
name|cq0
argument_list|,
name|ts
argument_list|,
name|v0
argument_list|)
decl_stmt|;
comment|// must be lower case z
name|Assert
operator|.
name|assertFalse
argument_list|(
name|searcher
operator|.
name|positionAt
argument_list|(
name|afterLast
argument_list|)
argument_list|)
expr_stmt|;
name|p
operator|=
name|searcher
operator|.
name|positionAtOrAfter
argument_list|(
name|afterLast
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|CellScannerPosition
operator|.
name|AFTER_LAST
argument_list|,
name|p
argument_list|)
expr_stmt|;
name|p
operator|=
name|searcher
operator|.
name|positionAtOrBefore
argument_list|(
name|afterLast
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|CellScannerPosition
operator|.
name|BEFORE
argument_list|,
name|p
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|CellComparator
operator|.
name|equals
argument_list|(
name|searcher
operator|.
name|current
argument_list|()
argument_list|,
name|CollectionUtils
operator|.
name|getLast
argument_list|(
name|d
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

