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
name|Tag
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
name|ArrayBackedTag
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
name|PrefixTreeBlockMeta
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
name|TestRowDataTrivialWithTags
extends|extends
name|BaseTestRowData
block|{
specifier|static
name|byte
index|[]
name|rA
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"rA"
argument_list|)
decl_stmt|,
name|rB
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"rB"
argument_list|)
decl_stmt|,
comment|// turn "r"
comment|// into a
comment|// branch for
comment|// the
comment|// Searcher
comment|// tests
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
literal|"q0"
argument_list|)
decl_stmt|,
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
name|List
argument_list|<
name|Tag
argument_list|>
name|tagList
init|=
operator|new
name|ArrayList
argument_list|<
name|Tag
argument_list|>
argument_list|(
literal|2
argument_list|)
decl_stmt|;
name|Tag
name|t
init|=
operator|new
name|ArrayBackedTag
argument_list|(
operator|(
name|byte
operator|)
literal|1
argument_list|,
literal|"visisbility"
argument_list|)
decl_stmt|;
name|tagList
operator|.
name|add
argument_list|(
name|t
argument_list|)
expr_stmt|;
name|t
operator|=
operator|new
name|ArrayBackedTag
argument_list|(
operator|(
name|byte
operator|)
literal|2
argument_list|,
literal|"ACL"
argument_list|)
expr_stmt|;
name|tagList
operator|.
name|add
argument_list|(
name|t
argument_list|)
expr_stmt|;
name|d
operator|.
name|add
argument_list|(
operator|new
name|KeyValue
argument_list|(
name|rA
argument_list|,
name|cf
argument_list|,
name|cq0
argument_list|,
name|ts
argument_list|,
name|v0
argument_list|,
name|tagList
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
name|rB
argument_list|,
name|cf
argument_list|,
name|cq0
argument_list|,
name|ts
argument_list|,
name|v0
argument_list|,
name|tagList
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
name|individualBlockMetaAssertions
parameter_list|(
name|PrefixTreeBlockMeta
name|blockMeta
parameter_list|)
block|{
comment|// node[0] -> root[r]
comment|// node[1] -> leaf[A], etc
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|blockMeta
operator|.
name|getRowTreeDepth
argument_list|()
argument_list|)
expr_stmt|;
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
comment|/**      * The searcher should get a token mismatch on the "r" branch. Assert that      * it skips not only rA, but rB as well.      */
name|KeyValue
name|afterLast
init|=
name|KeyValueUtil
operator|.
name|createFirstOnRow
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"zzz"
argument_list|)
argument_list|)
decl_stmt|;
name|CellScannerPosition
name|position
init|=
name|searcher
operator|.
name|positionAtOrAfter
argument_list|(
name|afterLast
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|CellScannerPosition
operator|.
name|AFTER_LAST
argument_list|,
name|position
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertNull
argument_list|(
name|searcher
operator|.
name|current
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

