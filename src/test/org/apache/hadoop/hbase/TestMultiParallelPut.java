begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Copyright 2009 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
package|;
end_package

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
name|HTable
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
name|Put
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
name|Result
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
name|HBaseAdmin
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
name|ArrayList
import|;
end_import

begin_class
specifier|public
class|class
name|TestMultiParallelPut
extends|extends
name|MultiRegionTable
block|{
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|VALUE
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"value"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|QUALIFIER
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"qual"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|FAMILY
init|=
literal|"family"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|TEST_TABLE
init|=
literal|"test_table"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|BYTES_FAMILY
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|FAMILY
argument_list|)
decl_stmt|;
specifier|public
name|TestMultiParallelPut
parameter_list|()
block|{
name|super
argument_list|(
literal|2
argument_list|,
name|FAMILY
argument_list|)
expr_stmt|;
name|desc
operator|=
operator|new
name|HTableDescriptor
argument_list|(
name|TEST_TABLE
argument_list|)
expr_stmt|;
name|desc
operator|.
name|addFamily
argument_list|(
operator|new
name|HColumnDescriptor
argument_list|(
name|FAMILY
argument_list|)
argument_list|)
expr_stmt|;
name|makeKeys
argument_list|()
expr_stmt|;
block|}
specifier|private
name|void
name|makeKeys
parameter_list|()
block|{
for|for
control|(
name|byte
index|[]
name|k
range|:
name|KEYS
control|)
block|{
name|byte
index|[]
name|cp
init|=
operator|new
name|byte
index|[
name|k
operator|.
name|length
operator|+
literal|1
index|]
decl_stmt|;
name|System
operator|.
name|arraycopy
argument_list|(
name|k
argument_list|,
literal|0
argument_list|,
name|cp
argument_list|,
literal|0
argument_list|,
name|k
operator|.
name|length
argument_list|)
expr_stmt|;
name|cp
index|[
name|k
operator|.
name|length
index|]
operator|=
literal|1
expr_stmt|;
name|keys
operator|.
name|add
argument_list|(
name|cp
argument_list|)
expr_stmt|;
block|}
block|}
name|List
argument_list|<
name|byte
index|[]
argument_list|>
name|keys
init|=
operator|new
name|ArrayList
argument_list|<
name|byte
index|[]
argument_list|>
argument_list|()
decl_stmt|;
specifier|public
name|void
name|testMultiPut
parameter_list|()
throws|throws
name|Exception
block|{
name|HTable
name|table
init|=
operator|new
name|HTable
argument_list|(
name|TEST_TABLE
argument_list|)
decl_stmt|;
name|table
operator|.
name|setAutoFlush
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|table
operator|.
name|setWriteBufferSize
argument_list|(
literal|10
operator|*
literal|1024
operator|*
literal|1024
argument_list|)
expr_stmt|;
for|for
control|(
name|byte
index|[]
name|k
range|:
name|keys
control|)
block|{
name|Put
name|put
init|=
operator|new
name|Put
argument_list|(
name|k
argument_list|)
decl_stmt|;
name|put
operator|.
name|add
argument_list|(
name|BYTES_FAMILY
argument_list|,
name|QUALIFIER
argument_list|,
name|VALUE
argument_list|)
expr_stmt|;
name|table
operator|.
name|put
argument_list|(
name|put
argument_list|)
expr_stmt|;
block|}
name|table
operator|.
name|flushCommits
argument_list|()
expr_stmt|;
for|for
control|(
name|byte
index|[]
name|k
range|:
name|keys
control|)
block|{
name|Get
name|get
init|=
operator|new
name|Get
argument_list|(
name|k
argument_list|)
decl_stmt|;
name|get
operator|.
name|addColumn
argument_list|(
name|BYTES_FAMILY
argument_list|,
name|QUALIFIER
argument_list|)
expr_stmt|;
name|Result
name|r
init|=
name|table
operator|.
name|get
argument_list|(
name|get
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|r
operator|.
name|containsColumn
argument_list|(
name|BYTES_FAMILY
argument_list|,
name|QUALIFIER
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|Bytes
operator|.
name|compareTo
argument_list|(
name|VALUE
argument_list|,
name|r
operator|.
name|getValue
argument_list|(
name|BYTES_FAMILY
argument_list|,
name|QUALIFIER
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|HBaseAdmin
name|admin
init|=
operator|new
name|HBaseAdmin
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|ClusterStatus
name|cs
init|=
name|admin
operator|.
name|getClusterStatus
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|cs
operator|.
name|getServers
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|HServerInfo
name|info
range|:
name|cs
operator|.
name|getServerInfo
argument_list|()
control|)
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
name|info
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|info
operator|.
name|getLoad
argument_list|()
operator|.
name|getNumberOfRegions
argument_list|()
operator|>
literal|10
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

