begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2007 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|client
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
name|HashSet
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
name|HBaseClusterTestCase
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
name|HColumnDescriptor
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
name|HTableDescriptor
import|;
end_import

begin_comment
comment|/**  * Tests the listTables client API  */
end_comment

begin_class
specifier|public
class|class
name|TestListTables
extends|extends
name|HBaseClusterTestCase
block|{
name|HBaseAdmin
name|admin
init|=
literal|null
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|HTableDescriptor
index|[]
name|TABLES
init|=
block|{
operator|new
name|HTableDescriptor
argument_list|(
literal|"table1"
argument_list|)
block|,
operator|new
name|HTableDescriptor
argument_list|(
literal|"table2"
argument_list|)
block|,
operator|new
name|HTableDescriptor
argument_list|(
literal|"table3"
argument_list|)
block|}
decl_stmt|;
annotation|@
name|Override
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
name|admin
operator|=
operator|new
name|HBaseAdmin
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|HColumnDescriptor
name|family
init|=
operator|new
name|HColumnDescriptor
argument_list|(
name|HConstants
operator|.
name|COLUMN_FAMILY_STR
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
name|TABLES
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|TABLES
index|[
name|i
index|]
operator|.
name|addFamily
argument_list|(
name|family
argument_list|)
expr_stmt|;
name|admin
operator|.
name|createTable
argument_list|(
name|TABLES
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * the test    * @throws IOException    */
specifier|public
name|void
name|testListTables
parameter_list|()
throws|throws
name|IOException
block|{
name|HTableDescriptor
index|[]
name|ts
init|=
name|admin
operator|.
name|listTables
argument_list|()
decl_stmt|;
name|HashSet
argument_list|<
name|HTableDescriptor
argument_list|>
name|result
init|=
operator|new
name|HashSet
argument_list|<
name|HTableDescriptor
argument_list|>
argument_list|(
name|ts
operator|.
name|length
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
name|ts
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|result
operator|.
name|add
argument_list|(
name|ts
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
name|int
name|size
init|=
name|result
operator|.
name|size
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
name|TABLES
operator|.
name|length
argument_list|,
name|size
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
name|TABLES
operator|.
name|length
operator|&&
name|i
operator|<
name|size
condition|;
name|i
operator|++
control|)
block|{
name|assertTrue
argument_list|(
name|result
operator|.
name|contains
argument_list|(
name|TABLES
index|[
name|i
index|]
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

