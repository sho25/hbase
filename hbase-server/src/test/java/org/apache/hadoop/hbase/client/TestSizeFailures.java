begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|LinkedList
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
name|Map
operator|.
name|Entry
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
name|hbase
operator|.
name|HBaseClassTestRule
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
name|testclassification
operator|.
name|LargeTests
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
name|AfterClass
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|BeforeClass
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|ClassRule
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

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|LoggerFactory
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
name|thirdparty
operator|.
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Maps
import|;
end_import

begin_class
annotation|@
name|Category
argument_list|(
name|LargeTests
operator|.
name|class
argument_list|)
specifier|public
class|class
name|TestSizeFailures
block|{
annotation|@
name|ClassRule
specifier|public
specifier|static
specifier|final
name|HBaseClassTestRule
name|CLASS_RULE
init|=
name|HBaseClassTestRule
operator|.
name|forClass
argument_list|(
name|TestSizeFailures
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|TestSizeFailures
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|protected
specifier|final
specifier|static
name|HBaseTestingUtility
name|TEST_UTIL
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
specifier|private
specifier|static
name|byte
index|[]
name|FAMILY
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"testFamily"
argument_list|)
decl_stmt|;
specifier|protected
specifier|static
name|int
name|SLAVES
init|=
literal|1
decl_stmt|;
specifier|private
specifier|static
name|TableName
name|TABLENAME
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|NUM_ROWS
init|=
literal|1000
operator|*
literal|1000
decl_stmt|,
name|NUM_COLS
init|=
literal|9
decl_stmt|;
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|setUpBeforeClass
parameter_list|()
throws|throws
name|Exception
block|{
comment|// Uncomment the following lines if more verbosity is needed for
comment|// debugging (see HBASE-12285 for details).
comment|//((Log4JLogger)RpcServer.LOG).getLogger().setLevel(Level.ALL);
comment|//((Log4JLogger)RpcClient.LOG).getLogger().setLevel(Level.ALL);
comment|//((Log4JLogger)ScannerCallable.LOG).getLogger().setLevel(Level.ALL);
name|Configuration
name|conf
init|=
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
name|conf
operator|.
name|setBoolean
argument_list|(
literal|"hbase.table.sanity.checks"
argument_list|,
literal|true
argument_list|)
expr_stmt|;
comment|// ignore sanity checks in the server
name|TEST_UTIL
operator|.
name|startMiniCluster
argument_list|(
name|SLAVES
argument_list|)
expr_stmt|;
comment|// Write a bunch of data
name|TABLENAME
operator|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"testSizeFailures"
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|byte
index|[]
argument_list|>
name|qualifiers
init|=
operator|new
name|ArrayList
argument_list|<>
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
operator|<=
literal|10
condition|;
name|i
operator|++
control|)
block|{
name|qualifiers
operator|.
name|add
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|Integer
operator|.
name|toString
argument_list|(
name|i
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|HColumnDescriptor
name|hcd
init|=
operator|new
name|HColumnDescriptor
argument_list|(
name|FAMILY
argument_list|)
decl_stmt|;
name|HTableDescriptor
name|desc
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|TABLENAME
argument_list|)
decl_stmt|;
name|desc
operator|.
name|addFamily
argument_list|(
name|hcd
argument_list|)
expr_stmt|;
name|byte
index|[]
index|[]
name|splits
init|=
operator|new
name|byte
index|[
literal|9
index|]
index|[
literal|2
index|]
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
literal|10
condition|;
name|i
operator|++
control|)
block|{
name|int
name|split
init|=
literal|48
operator|+
name|i
decl_stmt|;
name|splits
index|[
name|i
operator|-
literal|1
index|]
index|[
literal|0
index|]
operator|=
call|(
name|byte
call|)
argument_list|(
name|split
operator|>>>
literal|8
argument_list|)
expr_stmt|;
name|splits
index|[
name|i
operator|-
literal|1
index|]
index|[
literal|0
index|]
operator|=
call|(
name|byte
call|)
argument_list|(
name|split
argument_list|)
expr_stmt|;
block|}
name|TEST_UTIL
operator|.
name|getAdmin
argument_list|()
operator|.
name|createTable
argument_list|(
name|desc
argument_list|,
name|splits
argument_list|)
expr_stmt|;
name|Connection
name|conn
init|=
name|TEST_UTIL
operator|.
name|getConnection
argument_list|()
decl_stmt|;
try|try
init|(
name|Table
name|table
init|=
name|conn
operator|.
name|getTable
argument_list|(
name|TABLENAME
argument_list|)
init|)
block|{
name|List
argument_list|<
name|Put
argument_list|>
name|puts
init|=
operator|new
name|LinkedList
argument_list|<>
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
name|NUM_ROWS
condition|;
name|i
operator|++
control|)
block|{
name|Put
name|p
init|=
operator|new
name|Put
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|Integer
operator|.
name|toString
argument_list|(
name|i
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
name|NUM_COLS
condition|;
name|j
operator|++
control|)
block|{
name|byte
index|[]
name|value
init|=
operator|new
name|byte
index|[
literal|50
index|]
decl_stmt|;
name|Bytes
operator|.
name|random
argument_list|(
name|value
argument_list|)
expr_stmt|;
name|p
operator|.
name|addColumn
argument_list|(
name|FAMILY
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|Integer
operator|.
name|toString
argument_list|(
name|j
argument_list|)
argument_list|)
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
name|puts
operator|.
name|add
argument_list|(
name|p
argument_list|)
expr_stmt|;
if|if
condition|(
name|puts
operator|.
name|size
argument_list|()
operator|==
literal|1000
condition|)
block|{
name|table
operator|.
name|batch
argument_list|(
name|puts
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|puts
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
block|}
if|if
condition|(
name|puts
operator|.
name|size
argument_list|()
operator|>
literal|0
condition|)
block|{
name|table
operator|.
name|batch
argument_list|(
name|puts
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|AfterClass
specifier|public
specifier|static
name|void
name|tearDownAfterClass
parameter_list|()
throws|throws
name|Exception
block|{
name|TEST_UTIL
operator|.
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
block|}
comment|/**    * Basic client side validation of HBASE-13262    */
annotation|@
name|Test
specifier|public
name|void
name|testScannerSeesAllRecords
parameter_list|()
throws|throws
name|Exception
block|{
name|Connection
name|conn
init|=
name|TEST_UTIL
operator|.
name|getConnection
argument_list|()
decl_stmt|;
try|try
init|(
name|Table
name|table
init|=
name|conn
operator|.
name|getTable
argument_list|(
name|TABLENAME
argument_list|)
init|)
block|{
name|Scan
name|s
init|=
operator|new
name|Scan
argument_list|()
decl_stmt|;
name|s
operator|.
name|addFamily
argument_list|(
name|FAMILY
argument_list|)
expr_stmt|;
name|s
operator|.
name|setMaxResultSize
argument_list|(
operator|-
literal|1
argument_list|)
expr_stmt|;
name|s
operator|.
name|setBatch
argument_list|(
operator|-
literal|1
argument_list|)
expr_stmt|;
name|s
operator|.
name|setCaching
argument_list|(
literal|500
argument_list|)
expr_stmt|;
name|Entry
argument_list|<
name|Long
argument_list|,
name|Long
argument_list|>
name|entry
init|=
name|sumTable
argument_list|(
name|table
operator|.
name|getScanner
argument_list|(
name|s
argument_list|)
argument_list|)
decl_stmt|;
name|long
name|rowsObserved
init|=
name|entry
operator|.
name|getKey
argument_list|()
decl_stmt|;
name|long
name|entriesObserved
init|=
name|entry
operator|.
name|getValue
argument_list|()
decl_stmt|;
comment|// Verify that we see 1M rows and 9M cells
name|assertEquals
argument_list|(
name|NUM_ROWS
argument_list|,
name|rowsObserved
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|NUM_ROWS
operator|*
name|NUM_COLS
argument_list|,
name|entriesObserved
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Basic client side validation of HBASE-13262    */
annotation|@
name|Test
specifier|public
name|void
name|testSmallScannerSeesAllRecords
parameter_list|()
throws|throws
name|Exception
block|{
name|Connection
name|conn
init|=
name|TEST_UTIL
operator|.
name|getConnection
argument_list|()
decl_stmt|;
try|try
init|(
name|Table
name|table
init|=
name|conn
operator|.
name|getTable
argument_list|(
name|TABLENAME
argument_list|)
init|)
block|{
name|Scan
name|s
init|=
operator|new
name|Scan
argument_list|()
decl_stmt|;
name|s
operator|.
name|setSmall
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|s
operator|.
name|addFamily
argument_list|(
name|FAMILY
argument_list|)
expr_stmt|;
name|s
operator|.
name|setMaxResultSize
argument_list|(
operator|-
literal|1
argument_list|)
expr_stmt|;
name|s
operator|.
name|setBatch
argument_list|(
operator|-
literal|1
argument_list|)
expr_stmt|;
name|s
operator|.
name|setCaching
argument_list|(
literal|500
argument_list|)
expr_stmt|;
name|Entry
argument_list|<
name|Long
argument_list|,
name|Long
argument_list|>
name|entry
init|=
name|sumTable
argument_list|(
name|table
operator|.
name|getScanner
argument_list|(
name|s
argument_list|)
argument_list|)
decl_stmt|;
name|long
name|rowsObserved
init|=
name|entry
operator|.
name|getKey
argument_list|()
decl_stmt|;
name|long
name|entriesObserved
init|=
name|entry
operator|.
name|getValue
argument_list|()
decl_stmt|;
comment|// Verify that we see 1M rows and 9M cells
name|assertEquals
argument_list|(
name|NUM_ROWS
argument_list|,
name|rowsObserved
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|NUM_ROWS
operator|*
name|NUM_COLS
argument_list|,
name|entriesObserved
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Count the number of rows and the number of entries from a scanner    *    * @param scanner    *          The Scanner    * @return An entry where the first item is rows observed and the second is entries observed.    */
specifier|private
name|Entry
argument_list|<
name|Long
argument_list|,
name|Long
argument_list|>
name|sumTable
parameter_list|(
name|ResultScanner
name|scanner
parameter_list|)
block|{
name|long
name|rowsObserved
init|=
literal|0L
decl_stmt|;
name|long
name|entriesObserved
init|=
literal|0L
decl_stmt|;
comment|// Read all the records in the table
for|for
control|(
name|Result
name|result
range|:
name|scanner
control|)
block|{
name|rowsObserved
operator|++
expr_stmt|;
while|while
condition|(
name|result
operator|.
name|advance
argument_list|()
condition|)
block|{
name|entriesObserved
operator|++
expr_stmt|;
block|}
block|}
return|return
name|Maps
operator|.
name|immutableEntry
argument_list|(
name|rowsObserved
argument_list|,
name|entriesObserved
argument_list|)
return|;
block|}
block|}
end_class

end_unit

