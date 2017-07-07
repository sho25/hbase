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
name|hadoop
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
name|CellUtil
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
name|hadoop
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
name|hadoop
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
name|Assert
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
name|shaded
operator|.
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
name|TestRowDataSearcherRowMiss
extends|extends
name|BaseTestRowData
block|{
specifier|static
name|byte
index|[]
comment|//don't let the rows share any common prefix bytes
name|A
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"A"
argument_list|)
decl_stmt|,
name|AA
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"AA"
argument_list|)
decl_stmt|,
name|AAA
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"AAA"
argument_list|)
decl_stmt|,
name|B
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"B"
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
name|cq
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"cq0"
argument_list|)
decl_stmt|,
name|v
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
name|A
argument_list|,
name|cf
argument_list|,
name|cq
argument_list|,
name|ts
argument_list|,
name|v
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
name|AA
argument_list|,
name|cf
argument_list|,
name|cq
argument_list|,
name|ts
argument_list|,
name|v
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
name|AAA
argument_list|,
name|cf
argument_list|,
name|cq
argument_list|,
name|ts
argument_list|,
name|v
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
name|B
argument_list|,
name|cf
argument_list|,
name|cq
argument_list|,
name|ts
argument_list|,
name|v
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
name|assertRowOffsetsCorrect
argument_list|()
expr_stmt|;
name|searcher
operator|.
name|resetToBeforeFirstEntry
argument_list|()
expr_stmt|;
comment|//test first cell
try|try
block|{
name|searcher
operator|.
name|advance
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
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
name|CellUtil
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
comment|//test first cell in second row
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
literal|1
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|CellUtil
operator|.
name|equals
argument_list|(
name|d
operator|.
name|get
argument_list|(
literal|1
argument_list|)
argument_list|,
name|searcher
operator|.
name|current
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|testBetween1and2
argument_list|(
name|searcher
argument_list|)
expr_stmt|;
name|testBetween2and3
argument_list|(
name|searcher
argument_list|)
expr_stmt|;
block|}
comment|/************ private methods, call from above *******************/
specifier|private
name|void
name|assertRowOffsetsCorrect
parameter_list|()
block|{
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|4
argument_list|,
name|getRowStartIndexes
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|testBetween1and2
parameter_list|(
name|CellSearcher
name|searcher
parameter_list|)
block|{
name|CellScannerPosition
name|p
decl_stmt|;
comment|//reuse
name|Cell
name|betweenAAndAAA
init|=
operator|new
name|KeyValue
argument_list|(
name|AA
argument_list|,
name|cf
argument_list|,
name|cq
argument_list|,
name|ts
operator|-
literal|2
argument_list|,
name|v
argument_list|)
decl_stmt|;
comment|//test exact
name|Assert
operator|.
name|assertFalse
argument_list|(
name|searcher
operator|.
name|positionAt
argument_list|(
name|betweenAAndAAA
argument_list|)
argument_list|)
expr_stmt|;
comment|//test atOrBefore
name|p
operator|=
name|searcher
operator|.
name|positionAtOrBefore
argument_list|(
name|betweenAAndAAA
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
name|CellUtil
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
literal|1
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
comment|//test atOrAfter
name|p
operator|=
name|searcher
operator|.
name|positionAtOrAfter
argument_list|(
name|betweenAAndAAA
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
name|CellUtil
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
literal|2
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|testBetween2and3
parameter_list|(
name|CellSearcher
name|searcher
parameter_list|)
block|{
name|CellScannerPosition
name|p
decl_stmt|;
comment|//reuse
name|Cell
name|betweenAAAndB
init|=
operator|new
name|KeyValue
argument_list|(
name|AAA
argument_list|,
name|cf
argument_list|,
name|cq
argument_list|,
name|ts
operator|-
literal|2
argument_list|,
name|v
argument_list|)
decl_stmt|;
comment|//test exact
name|Assert
operator|.
name|assertFalse
argument_list|(
name|searcher
operator|.
name|positionAt
argument_list|(
name|betweenAAAndB
argument_list|)
argument_list|)
expr_stmt|;
comment|//test atOrBefore
name|p
operator|=
name|searcher
operator|.
name|positionAtOrBefore
argument_list|(
name|betweenAAAndB
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
name|CellUtil
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
literal|2
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
comment|//test atOrAfter
name|p
operator|=
name|searcher
operator|.
name|positionAtOrAfter
argument_list|(
name|betweenAAAndB
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
name|CellUtil
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
literal|3
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

