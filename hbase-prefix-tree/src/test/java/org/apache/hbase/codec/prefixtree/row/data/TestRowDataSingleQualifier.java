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
name|hbase
operator|.
name|codec
operator|.
name|prefixtree
operator|.
name|PrefixTreeTestConstants
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
name|TestRowDataSingleQualifier
extends|extends
name|BaseTestRowData
block|{
specifier|static
name|byte
index|[]
name|rowA
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"rowA"
argument_list|)
decl_stmt|,
name|rowB
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"rowB"
argument_list|)
decl_stmt|,
name|cf
init|=
name|PrefixTreeTestConstants
operator|.
name|TEST_CF
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
block|}
end_class

end_unit

