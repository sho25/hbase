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
name|java
operator|.
name|util
operator|.
name|Random
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|Log
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|LogFactory
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
name|HBaseTestCase
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
name|HBaseTestingUtility
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
name|HTableDescriptor
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
name|TableName
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
name|Scan
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
name|TestWideScanner
extends|extends
name|HBaseTestCase
block|{
specifier|private
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|TestWideScanner
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|static
specifier|final
name|byte
index|[]
name|A
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"A"
argument_list|)
decl_stmt|;
specifier|static
specifier|final
name|byte
index|[]
name|B
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"B"
argument_list|)
decl_stmt|;
specifier|static
specifier|final
name|byte
index|[]
name|C
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"C"
argument_list|)
decl_stmt|;
specifier|static
name|byte
index|[]
index|[]
name|COLUMNS
init|=
block|{
name|A
block|,
name|B
block|,
name|C
block|}
decl_stmt|;
specifier|static
specifier|final
name|Random
name|rng
init|=
operator|new
name|Random
argument_list|()
decl_stmt|;
specifier|static
specifier|final
name|HTableDescriptor
name|TESTTABLEDESC
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"testwidescan"
argument_list|)
argument_list|)
decl_stmt|;
static|static
block|{
for|for
control|(
name|byte
index|[]
name|cfName
range|:
operator|new
name|byte
index|[]
index|[]
block|{
name|A
block|,
name|B
block|,
name|C
block|}
control|)
block|{
name|TESTTABLEDESC
operator|.
name|addFamily
argument_list|(
operator|new
name|HColumnDescriptor
argument_list|(
name|cfName
argument_list|)
comment|// Keep versions to help debugging.
operator|.
name|setMaxVersions
argument_list|(
literal|100
argument_list|)
operator|.
name|setBlocksize
argument_list|(
literal|8
operator|*
literal|1024
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
comment|/** HRegionInfo for root region */
name|HRegion
name|r
decl_stmt|;
specifier|private
name|int
name|addWideContent
parameter_list|(
name|HRegion
name|region
parameter_list|)
throws|throws
name|IOException
block|{
name|int
name|count
init|=
literal|0
decl_stmt|;
for|for
control|(
name|char
name|c
init|=
literal|'a'
init|;
name|c
operator|<=
literal|'c'
condition|;
name|c
operator|++
control|)
block|{
name|byte
index|[]
name|row
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"ab"
operator|+
name|c
argument_list|)
decl_stmt|;
name|int
name|i
decl_stmt|,
name|j
decl_stmt|;
name|long
name|ts
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
for|for
control|(
name|i
operator|=
literal|0
init|;
name|i
operator|<
literal|100
condition|;
name|i
operator|++
control|)
block|{
name|byte
index|[]
name|b
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"%10d"
argument_list|,
name|i
argument_list|)
argument_list|)
decl_stmt|;
for|for
control|(
name|j
operator|=
literal|0
init|;
name|j
operator|<
literal|100
condition|;
name|j
operator|++
control|)
block|{
name|Put
name|put
init|=
operator|new
name|Put
argument_list|(
name|row
argument_list|)
decl_stmt|;
name|put
operator|.
name|setDurability
argument_list|(
name|Durability
operator|.
name|SKIP_WAL
argument_list|)
expr_stmt|;
name|long
name|ts1
init|=
operator|++
name|ts
decl_stmt|;
name|put
operator|.
name|addColumn
argument_list|(
name|COLUMNS
index|[
name|rng
operator|.
name|nextInt
argument_list|(
name|COLUMNS
operator|.
name|length
argument_list|)
index|]
argument_list|,
name|b
argument_list|,
name|ts1
argument_list|,
name|b
argument_list|)
expr_stmt|;
name|region
operator|.
name|put
argument_list|(
name|put
argument_list|)
expr_stmt|;
name|count
operator|++
expr_stmt|;
block|}
block|}
block|}
return|return
name|count
return|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testWideScanBatching
parameter_list|()
throws|throws
name|IOException
block|{
specifier|final
name|int
name|batch
init|=
literal|256
decl_stmt|;
try|try
block|{
name|this
operator|.
name|r
operator|=
name|createNewHRegion
argument_list|(
name|TESTTABLEDESC
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|int
name|inserted
init|=
name|addWideContent
argument_list|(
name|this
operator|.
name|r
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Cell
argument_list|>
name|results
init|=
operator|new
name|ArrayList
argument_list|<
name|Cell
argument_list|>
argument_list|()
decl_stmt|;
name|Scan
name|scan
init|=
operator|new
name|Scan
argument_list|()
decl_stmt|;
name|scan
operator|.
name|addFamily
argument_list|(
name|A
argument_list|)
expr_stmt|;
name|scan
operator|.
name|addFamily
argument_list|(
name|B
argument_list|)
expr_stmt|;
name|scan
operator|.
name|addFamily
argument_list|(
name|C
argument_list|)
expr_stmt|;
name|scan
operator|.
name|setMaxVersions
argument_list|(
literal|100
argument_list|)
expr_stmt|;
name|scan
operator|.
name|setBatch
argument_list|(
name|batch
argument_list|)
expr_stmt|;
name|InternalScanner
name|s
init|=
name|r
operator|.
name|getScanner
argument_list|(
name|scan
argument_list|)
decl_stmt|;
name|int
name|total
init|=
literal|0
decl_stmt|;
name|int
name|i
init|=
literal|0
decl_stmt|;
name|boolean
name|more
decl_stmt|;
do|do
block|{
name|more
operator|=
name|s
operator|.
name|next
argument_list|(
name|results
argument_list|)
expr_stmt|;
name|i
operator|++
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"iteration #"
operator|+
name|i
operator|+
literal|", results.size="
operator|+
name|results
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
comment|// assert that the result set is no larger
name|assertTrue
argument_list|(
name|results
operator|.
name|size
argument_list|()
operator|<=
name|batch
argument_list|)
expr_stmt|;
name|total
operator|+=
name|results
operator|.
name|size
argument_list|()
expr_stmt|;
if|if
condition|(
name|results
operator|.
name|size
argument_list|()
operator|>
literal|0
condition|)
block|{
comment|// assert that all results are from the same row
name|byte
index|[]
name|row
init|=
name|CellUtil
operator|.
name|cloneRow
argument_list|(
name|results
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
decl_stmt|;
for|for
control|(
name|Cell
name|kv
range|:
name|results
control|)
block|{
name|assertTrue
argument_list|(
name|Bytes
operator|.
name|equals
argument_list|(
name|row
argument_list|,
name|CellUtil
operator|.
name|cloneRow
argument_list|(
name|kv
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
name|results
operator|.
name|clear
argument_list|()
expr_stmt|;
comment|// trigger ChangedReadersObservers
name|Iterator
argument_list|<
name|KeyValueScanner
argument_list|>
name|scanners
init|=
operator|(
operator|(
name|HRegion
operator|.
name|RegionScannerImpl
operator|)
name|s
operator|)
operator|.
name|storeHeap
operator|.
name|getHeap
argument_list|()
operator|.
name|iterator
argument_list|()
decl_stmt|;
while|while
condition|(
name|scanners
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|StoreScanner
name|ss
init|=
operator|(
name|StoreScanner
operator|)
name|scanners
operator|.
name|next
argument_list|()
decl_stmt|;
name|ss
operator|.
name|updateReaders
argument_list|()
expr_stmt|;
block|}
block|}
do|while
condition|(
name|more
condition|)
do|;
comment|// assert that the scanner returned all values
name|LOG
operator|.
name|info
argument_list|(
literal|"inserted "
operator|+
name|inserted
operator|+
literal|", scanned "
operator|+
name|total
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|total
argument_list|,
name|inserted
argument_list|)
expr_stmt|;
name|s
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
finally|finally
block|{
name|HBaseTestingUtility
operator|.
name|closeRegionAndWAL
argument_list|(
name|this
operator|.
name|r
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

