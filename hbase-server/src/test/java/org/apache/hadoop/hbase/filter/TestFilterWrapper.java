begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Copyright The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|filter
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
name|List
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
name|HBaseConfiguration
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
name|exceptions
operator|.
name|MasterNotRunningException
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
name|exceptions
operator|.
name|ZooKeeperConnectionException
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
name|ResultScanner
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
name|Test
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
name|MediumTests
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

begin_comment
comment|/**  * Test if the FilterWrapper retains the same semantics defined in the  * {@link org.apache.hadoop.hbase.filter.Filter}  */
end_comment

begin_class
annotation|@
name|Category
argument_list|(
name|MediumTests
operator|.
name|class
argument_list|)
specifier|public
class|class
name|TestFilterWrapper
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
name|TestFilterWrapper
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|HBaseTestingUtility
name|TEST_UTIL
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
specifier|private
specifier|static
name|Configuration
name|conf
init|=
literal|null
decl_stmt|;
specifier|private
specifier|static
name|HBaseAdmin
name|admin
init|=
literal|null
decl_stmt|;
specifier|private
specifier|static
name|byte
index|[]
name|name
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"test"
argument_list|)
decl_stmt|;
annotation|@
name|Test
specifier|public
name|void
name|testFilterWrapper
parameter_list|()
block|{
name|int
name|kv_number
init|=
literal|0
decl_stmt|;
name|int
name|row_number
init|=
literal|0
decl_stmt|;
try|try
block|{
name|Scan
name|scan
init|=
operator|new
name|Scan
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|Filter
argument_list|>
name|fs
init|=
operator|new
name|ArrayList
argument_list|<
name|Filter
argument_list|>
argument_list|()
decl_stmt|;
name|DependentColumnFilter
name|f1
init|=
operator|new
name|DependentColumnFilter
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"f1"
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"c5"
argument_list|)
argument_list|,
literal|true
argument_list|,
name|CompareFilter
operator|.
name|CompareOp
operator|.
name|EQUAL
argument_list|,
operator|new
name|SubstringComparator
argument_list|(
literal|"c5"
argument_list|)
argument_list|)
decl_stmt|;
name|PageFilter
name|f2
init|=
operator|new
name|PageFilter
argument_list|(
literal|2
argument_list|)
decl_stmt|;
name|fs
operator|.
name|add
argument_list|(
name|f1
argument_list|)
expr_stmt|;
name|fs
operator|.
name|add
argument_list|(
name|f2
argument_list|)
expr_stmt|;
name|FilterList
name|filter
init|=
operator|new
name|FilterList
argument_list|(
name|fs
argument_list|)
decl_stmt|;
name|scan
operator|.
name|setFilter
argument_list|(
name|filter
argument_list|)
expr_stmt|;
name|HTable
name|table
init|=
operator|new
name|HTable
argument_list|(
name|conf
argument_list|,
name|name
argument_list|)
decl_stmt|;
name|ResultScanner
name|scanner
init|=
name|table
operator|.
name|getScanner
argument_list|(
name|scan
argument_list|)
decl_stmt|;
comment|// row2 (c1-c4) and row3(c1-c4) are returned
for|for
control|(
name|Result
name|result
range|:
name|scanner
control|)
block|{
name|row_number
operator|++
expr_stmt|;
for|for
control|(
name|KeyValue
name|kv
range|:
name|result
operator|.
name|list
argument_list|()
control|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
name|kv_number
operator|+
literal|". kv: "
operator|+
name|kv
argument_list|)
expr_stmt|;
name|kv_number
operator|++
expr_stmt|;
name|assertEquals
argument_list|(
literal|"Returned row is not correct"
argument_list|,
operator|new
name|String
argument_list|(
name|kv
operator|.
name|getRow
argument_list|()
argument_list|)
argument_list|,
literal|"row"
operator|+
operator|(
name|row_number
operator|+
literal|1
operator|)
argument_list|)
expr_stmt|;
block|}
block|}
name|scanner
operator|.
name|close
argument_list|()
expr_stmt|;
name|table
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
comment|// no correct result is expected
name|assertNull
argument_list|(
literal|"Exception happens in scan"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
name|LOG
operator|.
name|debug
argument_list|(
literal|"check the fetched kv number"
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"We should get 8 results returned."
argument_list|,
literal|8
argument_list|,
name|kv_number
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"We should get 2 rows returned"
argument_list|,
literal|2
argument_list|,
name|row_number
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|static
name|void
name|prepareData
parameter_list|()
block|{
try|try
block|{
name|HTable
name|table
init|=
operator|new
name|HTable
argument_list|(
name|TestFilterWrapper
operator|.
name|conf
argument_list|,
name|name
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
literal|"Fail to create the table"
argument_list|,
name|admin
operator|.
name|tableExists
argument_list|(
name|name
argument_list|)
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|Put
argument_list|>
name|puts
init|=
operator|new
name|ArrayList
argument_list|<
name|Put
argument_list|>
argument_list|()
decl_stmt|;
comment|// row1 =><f1:c1, 1_c1, ts=1>,<f1:c2, 1_c2, ts=2>,<f1:c3, 1_c3,ts=3>,
comment|//<f1:c4,1_c4, ts=4>,<f1:c5, 1_c5, ts=5>
comment|// row2 =><f1:c1, 2_c1, ts=2>,<f1,c2, 2_c2, ts=2>,<f1:c3, 2_c3,ts=2>,
comment|//<f1:c4,2_c4, ts=2>,<f1:c5, 2_c5, ts=2>
comment|// row3 =><f1:c1, 3_c1, ts=3>,<f1:c2, 3_c2, ts=3>,<f1:c3, 3_c3,ts=2>,
comment|//<f1:c4,3_c4, ts=3>,<f1:c5, 3_c5, ts=3>
for|for
control|(
name|int
name|i
init|=
literal|1
init|;
name|i
operator|<
literal|4
condition|;
name|i
operator|++
control|)
block|{
name|Put
name|put
init|=
operator|new
name|Put
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"row"
operator|+
name|i
argument_list|)
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|j
init|=
literal|1
init|;
name|j
operator|<
literal|6
condition|;
name|j
operator|++
control|)
block|{
name|long
name|timestamp
init|=
name|j
decl_stmt|;
if|if
condition|(
name|i
operator|!=
literal|1
condition|)
name|timestamp
operator|=
name|i
expr_stmt|;
name|put
operator|.
name|add
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"f1"
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"c"
operator|+
name|j
argument_list|)
argument_list|,
name|timestamp
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|i
operator|+
literal|"_c"
operator|+
name|j
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|puts
operator|.
name|add
argument_list|(
name|put
argument_list|)
expr_stmt|;
block|}
name|table
operator|.
name|put
argument_list|(
name|puts
argument_list|)
expr_stmt|;
name|table
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|assertNull
argument_list|(
literal|"Exception found while putting data into table"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
specifier|static
name|void
name|createTable
parameter_list|()
block|{
name|assertNotNull
argument_list|(
literal|"HBaseAdmin is not initialized successfully."
argument_list|,
name|admin
argument_list|)
expr_stmt|;
if|if
condition|(
name|admin
operator|!=
literal|null
condition|)
block|{
name|HTableDescriptor
name|desc
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|name
argument_list|)
decl_stmt|;
name|HColumnDescriptor
name|coldef
init|=
operator|new
name|HColumnDescriptor
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"f1"
argument_list|)
argument_list|)
decl_stmt|;
name|desc
operator|.
name|addFamily
argument_list|(
name|coldef
argument_list|)
expr_stmt|;
try|try
block|{
name|admin
operator|.
name|createTable
argument_list|(
name|desc
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"Fail to create the table"
argument_list|,
name|admin
operator|.
name|tableExists
argument_list|(
name|name
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|assertNull
argument_list|(
literal|"Exception found while creating table"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|private
specifier|static
name|void
name|deleteTable
parameter_list|()
block|{
if|if
condition|(
name|admin
operator|!=
literal|null
condition|)
block|{
try|try
block|{
name|admin
operator|.
name|disableTable
argument_list|(
name|name
argument_list|)
expr_stmt|;
name|admin
operator|.
name|deleteTable
argument_list|(
name|name
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|assertNull
argument_list|(
literal|"Exception found deleting the table"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|private
specifier|static
name|void
name|initialize
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
name|TestFilterWrapper
operator|.
name|conf
operator|=
name|HBaseConfiguration
operator|.
name|create
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|TestFilterWrapper
operator|.
name|conf
operator|.
name|setInt
argument_list|(
name|HConstants
operator|.
name|HBASE_CLIENT_RETRIES_NUMBER
argument_list|,
literal|1
argument_list|)
expr_stmt|;
try|try
block|{
name|admin
operator|=
operator|new
name|HBaseAdmin
argument_list|(
name|conf
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|MasterNotRunningException
name|e
parameter_list|)
block|{
name|assertNull
argument_list|(
literal|"Master is not running"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ZooKeeperConnectionException
name|e
parameter_list|)
block|{
name|assertNull
argument_list|(
literal|"Cannot connect to Zookeeper"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|assertNull
argument_list|(
literal|"Caught IOException"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
name|createTable
argument_list|()
expr_stmt|;
name|prepareData
argument_list|()
expr_stmt|;
block|}
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|setUp
parameter_list|()
throws|throws
name|Exception
block|{
name|Configuration
name|config
init|=
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
name|TEST_UTIL
operator|.
name|startMiniCluster
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|initialize
argument_list|(
name|TEST_UTIL
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
name|tearDown
parameter_list|()
throws|throws
name|Exception
block|{
name|deleteTable
argument_list|()
expr_stmt|;
name|TEST_UTIL
operator|.
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

