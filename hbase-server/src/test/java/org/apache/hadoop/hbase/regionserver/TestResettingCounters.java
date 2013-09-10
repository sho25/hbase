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
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertTrue
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
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|conf
operator|.
name|Configuration
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
name|fs
operator|.
name|FileSystem
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
name|fs
operator|.
name|Path
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
name|client
operator|.
name|Increment
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
name|Durability
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
name|SmallTests
operator|.
name|class
argument_list|)
specifier|public
class|class
name|TestResettingCounters
block|{
annotation|@
name|Test
specifier|public
name|void
name|testResettingCounters
parameter_list|()
throws|throws
name|Exception
block|{
name|HBaseTestingUtility
name|htu
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
name|Configuration
name|conf
init|=
name|htu
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
name|FileSystem
name|fs
init|=
name|FileSystem
operator|.
name|get
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|byte
index|[]
name|table
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"table"
argument_list|)
decl_stmt|;
name|byte
index|[]
index|[]
name|families
init|=
operator|new
name|byte
index|[]
index|[]
block|{
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"family1"
argument_list|)
block|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"family2"
argument_list|)
block|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"family3"
argument_list|)
block|}
decl_stmt|;
name|int
name|numQualifiers
init|=
literal|10
decl_stmt|;
name|byte
index|[]
index|[]
name|qualifiers
init|=
operator|new
name|byte
index|[
name|numQualifiers
index|]
index|[]
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
name|numQualifiers
condition|;
name|i
operator|++
control|)
name|qualifiers
index|[
name|i
index|]
operator|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"qf"
operator|+
name|i
argument_list|)
expr_stmt|;
name|int
name|numRows
init|=
literal|10
decl_stmt|;
name|byte
index|[]
index|[]
name|rows
init|=
operator|new
name|byte
index|[
name|numRows
index|]
index|[]
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
name|numRows
condition|;
name|i
operator|++
control|)
name|rows
index|[
name|i
index|]
operator|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"r"
operator|+
name|i
argument_list|)
expr_stmt|;
name|HTableDescriptor
name|htd
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
name|table
argument_list|)
argument_list|)
decl_stmt|;
for|for
control|(
name|byte
index|[]
name|family
range|:
name|families
control|)
name|htd
operator|.
name|addFamily
argument_list|(
operator|new
name|HColumnDescriptor
argument_list|(
name|family
argument_list|)
argument_list|)
expr_stmt|;
name|HRegionInfo
name|hri
init|=
operator|new
name|HRegionInfo
argument_list|(
name|htd
operator|.
name|getTableName
argument_list|()
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|String
name|testDir
init|=
name|htu
operator|.
name|getDataTestDir
argument_list|()
operator|+
literal|"/TestResettingCounters/"
decl_stmt|;
name|Path
name|path
init|=
operator|new
name|Path
argument_list|(
name|testDir
argument_list|)
decl_stmt|;
if|if
condition|(
name|fs
operator|.
name|exists
argument_list|(
name|path
argument_list|)
condition|)
block|{
if|if
condition|(
operator|!
name|fs
operator|.
name|delete
argument_list|(
name|path
argument_list|,
literal|true
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Failed delete of "
operator|+
name|path
argument_list|)
throw|;
block|}
block|}
name|HRegion
name|region
init|=
name|HRegion
operator|.
name|createHRegion
argument_list|(
name|hri
argument_list|,
name|path
argument_list|,
name|conf
argument_list|,
name|htd
argument_list|)
decl_stmt|;
try|try
block|{
name|Increment
name|odd
init|=
operator|new
name|Increment
argument_list|(
name|rows
index|[
literal|0
index|]
argument_list|)
decl_stmt|;
name|odd
operator|.
name|setDurability
argument_list|(
name|Durability
operator|.
name|SKIP_WAL
argument_list|)
expr_stmt|;
name|Increment
name|even
init|=
operator|new
name|Increment
argument_list|(
name|rows
index|[
literal|0
index|]
argument_list|)
decl_stmt|;
name|even
operator|.
name|setDurability
argument_list|(
name|Durability
operator|.
name|SKIP_WAL
argument_list|)
expr_stmt|;
name|Increment
name|all
init|=
operator|new
name|Increment
argument_list|(
name|rows
index|[
literal|0
index|]
argument_list|)
decl_stmt|;
name|all
operator|.
name|setDurability
argument_list|(
name|Durability
operator|.
name|SKIP_WAL
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
name|numQualifiers
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|i
operator|%
literal|2
operator|==
literal|0
condition|)
name|even
operator|.
name|addColumn
argument_list|(
name|families
index|[
literal|0
index|]
argument_list|,
name|qualifiers
index|[
name|i
index|]
argument_list|,
literal|1
argument_list|)
expr_stmt|;
else|else
name|odd
operator|.
name|addColumn
argument_list|(
name|families
index|[
literal|0
index|]
argument_list|,
name|qualifiers
index|[
name|i
index|]
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|all
operator|.
name|addColumn
argument_list|(
name|families
index|[
literal|0
index|]
argument_list|,
name|qualifiers
index|[
name|i
index|]
argument_list|,
literal|1
argument_list|)
expr_stmt|;
block|}
comment|// increment odd qualifiers 5 times and flush
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
literal|5
condition|;
name|i
operator|++
control|)
name|region
operator|.
name|increment
argument_list|(
name|odd
argument_list|)
expr_stmt|;
name|region
operator|.
name|flushcache
argument_list|()
expr_stmt|;
comment|// increment even qualifiers 5 times
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
literal|5
condition|;
name|i
operator|++
control|)
name|region
operator|.
name|increment
argument_list|(
name|even
argument_list|)
expr_stmt|;
comment|// increment all qualifiers, should have value=6 for all
name|Result
name|result
init|=
name|region
operator|.
name|increment
argument_list|(
name|all
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|numQualifiers
argument_list|,
name|result
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|Cell
index|[]
name|kvs
init|=
name|result
operator|.
name|rawCells
argument_list|()
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
name|kvs
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
name|kvs
index|[
name|i
index|]
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|CellUtil
operator|.
name|matchingQualifier
argument_list|(
name|kvs
index|[
name|i
index|]
argument_list|,
name|qualifiers
index|[
name|i
index|]
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|6
argument_list|,
name|Bytes
operator|.
name|toLong
argument_list|(
name|CellUtil
operator|.
name|getValueArray
argument_list|(
name|kvs
index|[
name|i
index|]
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
finally|finally
block|{
name|HRegion
operator|.
name|closeHRegion
argument_list|(
name|region
argument_list|)
expr_stmt|;
block|}
name|HRegion
operator|.
name|closeHRegion
argument_list|(
name|region
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

