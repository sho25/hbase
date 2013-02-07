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
name|KeyValue
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
name|cell
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
specifier|abstract
class|class
name|BaseTestRowData
implements|implements
name|TestRowData
block|{
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|Integer
argument_list|>
name|getRowStartIndexes
parameter_list|()
block|{
name|List
argument_list|<
name|Integer
argument_list|>
name|rowStartIndexes
init|=
name|Lists
operator|.
name|newArrayList
argument_list|()
decl_stmt|;
name|rowStartIndexes
operator|.
name|add
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|KeyValue
argument_list|>
name|inputs
init|=
name|getInputs
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|1
init|;
name|i
operator|<
name|inputs
operator|.
name|size
argument_list|()
condition|;
operator|++
name|i
control|)
block|{
name|KeyValue
name|lastKv
init|=
name|inputs
operator|.
name|get
argument_list|(
name|i
operator|-
literal|1
argument_list|)
decl_stmt|;
name|KeyValue
name|kv
init|=
name|inputs
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|CellComparator
operator|.
name|equalsRow
argument_list|(
name|lastKv
argument_list|,
name|kv
argument_list|)
condition|)
block|{
name|rowStartIndexes
operator|.
name|add
argument_list|(
name|i
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|rowStartIndexes
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
block|{   }
annotation|@
name|Override
specifier|public
name|void
name|individualSearcherAssertions
parameter_list|(
name|CellSearcher
name|searcher
parameter_list|)
block|{   }
block|}
end_class

end_unit

