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
name|assertArrayEquals
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
name|assertNull
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
name|regionserver
operator|.
name|HRegionServer
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
name|ClientTests
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
name|Before
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
name|Rule
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
name|junit
operator|.
name|rules
operator|.
name|TestName
import|;
end_import

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|LargeTests
operator|.
name|class
block|,
name|ClientTests
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|TestMvccConsistentScanner
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
name|TestMvccConsistentScanner
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|HBaseTestingUtility
name|UTIL
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
specifier|private
specifier|static
name|Connection
name|CONN
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|CF
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"cf"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|CQ1
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"cq1"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|CQ2
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"cq2"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|CQ3
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"cq3"
argument_list|)
decl_stmt|;
annotation|@
name|Rule
specifier|public
name|TestName
name|testName
init|=
operator|new
name|TestName
argument_list|()
decl_stmt|;
specifier|private
name|TableName
name|tableName
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
name|UTIL
operator|.
name|startMiniCluster
argument_list|(
literal|2
argument_list|)
expr_stmt|;
name|CONN
operator|=
name|ConnectionFactory
operator|.
name|createConnection
argument_list|(
name|UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
expr_stmt|;
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
name|CONN
operator|.
name|close
argument_list|()
expr_stmt|;
name|UTIL
operator|.
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Before
specifier|public
name|void
name|setUp
parameter_list|()
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|tableName
operator|=
name|TableName
operator|.
name|valueOf
argument_list|(
name|testName
operator|.
name|getMethodName
argument_list|()
operator|.
name|replaceAll
argument_list|(
literal|"[^0-9a-zA-Z]"
argument_list|,
literal|"_"
argument_list|)
argument_list|)
expr_stmt|;
name|UTIL
operator|.
name|createTable
argument_list|(
name|tableName
argument_list|,
name|CF
argument_list|)
expr_stmt|;
name|UTIL
operator|.
name|waitTableAvailable
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|put
parameter_list|(
name|byte
index|[]
name|row
parameter_list|,
name|byte
index|[]
name|cq
parameter_list|,
name|byte
index|[]
name|value
parameter_list|)
throws|throws
name|IOException
block|{
try|try
init|(
name|Table
name|table
init|=
name|CONN
operator|.
name|getTable
argument_list|(
name|tableName
argument_list|)
init|)
block|{
name|table
operator|.
name|put
argument_list|(
operator|new
name|Put
argument_list|(
name|row
argument_list|)
operator|.
name|addColumn
argument_list|(
name|CF
argument_list|,
name|cq
argument_list|,
name|value
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|move
parameter_list|()
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|RegionInfo
name|region
init|=
name|UTIL
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|getRegions
argument_list|(
name|tableName
argument_list|)
operator|.
name|stream
argument_list|()
operator|.
name|findAny
argument_list|()
operator|.
name|get
argument_list|()
operator|.
name|getRegionInfo
argument_list|()
decl_stmt|;
name|HRegionServer
name|rs
init|=
name|UTIL
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|getRegionServerThreads
argument_list|()
operator|.
name|stream
argument_list|()
operator|.
name|map
argument_list|(
name|t
lambda|->
name|t
operator|.
name|getRegionServer
argument_list|()
argument_list|)
operator|.
name|filter
argument_list|(
name|r
lambda|->
operator|!
name|r
operator|.
name|getOnlineTables
argument_list|()
operator|.
name|contains
argument_list|(
name|tableName
argument_list|)
argument_list|)
operator|.
name|findAny
argument_list|()
operator|.
name|get
argument_list|()
decl_stmt|;
name|UTIL
operator|.
name|getAdmin
argument_list|()
operator|.
name|move
argument_list|(
name|region
operator|.
name|getEncodedNameAsBytes
argument_list|()
argument_list|,
name|rs
operator|.
name|getServerName
argument_list|()
argument_list|)
expr_stmt|;
while|while
condition|(
name|UTIL
operator|.
name|getRSForFirstRegionInTable
argument_list|(
name|tableName
argument_list|)
operator|!=
name|rs
condition|)
block|{
name|Thread
operator|.
name|sleep
argument_list|(
literal|100
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testRowAtomic
parameter_list|()
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|byte
index|[]
name|row
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"row"
argument_list|)
decl_stmt|;
name|put
argument_list|(
name|row
argument_list|,
name|CQ1
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|put
argument_list|(
name|row
argument_list|,
name|CQ2
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
try|try
init|(
name|Table
name|table
init|=
name|CONN
operator|.
name|getTable
argument_list|(
name|tableName
argument_list|)
init|;
name|ResultScanner
name|scanner
operator|=
name|table
operator|.
name|getScanner
argument_list|(
operator|new
name|Scan
argument_list|()
operator|.
name|setBatch
argument_list|(
literal|1
argument_list|)
operator|.
name|setCaching
argument_list|(
literal|1
argument_list|)
argument_list|)
init|)
block|{
name|Result
name|result
init|=
name|scanner
operator|.
name|next
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|result
operator|.
name|rawCells
argument_list|()
operator|.
name|length
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|Bytes
operator|.
name|toInt
argument_list|(
name|result
operator|.
name|getValue
argument_list|(
name|CF
argument_list|,
name|CQ1
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|move
argument_list|()
expr_stmt|;
name|put
argument_list|(
name|row
argument_list|,
name|CQ3
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|3
argument_list|)
argument_list|)
expr_stmt|;
name|result
operator|=
name|scanner
operator|.
name|next
argument_list|()
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|result
operator|.
name|rawCells
argument_list|()
operator|.
name|length
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|Bytes
operator|.
name|toInt
argument_list|(
name|result
operator|.
name|getValue
argument_list|(
name|CF
argument_list|,
name|CQ2
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|scanner
operator|.
name|next
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testCrossRowAtomicInRegion
parameter_list|()
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|put
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"row1"
argument_list|)
argument_list|,
name|CQ1
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|put
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"row2"
argument_list|)
argument_list|,
name|CQ1
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
try|try
init|(
name|Table
name|table
init|=
name|CONN
operator|.
name|getTable
argument_list|(
name|tableName
argument_list|)
init|;
name|ResultScanner
name|scanner
operator|=
name|table
operator|.
name|getScanner
argument_list|(
operator|new
name|Scan
argument_list|()
operator|.
name|setCaching
argument_list|(
literal|1
argument_list|)
argument_list|)
init|)
block|{
name|Result
name|result
init|=
name|scanner
operator|.
name|next
argument_list|()
decl_stmt|;
name|assertArrayEquals
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"row1"
argument_list|)
argument_list|,
name|result
operator|.
name|getRow
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|Bytes
operator|.
name|toInt
argument_list|(
name|result
operator|.
name|getValue
argument_list|(
name|CF
argument_list|,
name|CQ1
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|move
argument_list|()
expr_stmt|;
name|put
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"row3"
argument_list|)
argument_list|,
name|CQ1
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|3
argument_list|)
argument_list|)
expr_stmt|;
name|result
operator|=
name|scanner
operator|.
name|next
argument_list|()
expr_stmt|;
name|assertArrayEquals
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"row2"
argument_list|)
argument_list|,
name|result
operator|.
name|getRow
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|Bytes
operator|.
name|toInt
argument_list|(
name|result
operator|.
name|getValue
argument_list|(
name|CF
argument_list|,
name|CQ1
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|scanner
operator|.
name|next
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

