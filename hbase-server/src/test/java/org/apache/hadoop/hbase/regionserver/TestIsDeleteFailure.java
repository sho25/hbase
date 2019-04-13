begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  * http://www.apache.org/licenses/LICENSE-2.0  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|CompareOperator
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
name|Delete
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
name|Mutation
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
name|client
operator|.
name|Table
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
name|filter
operator|.
name|BinaryComparator
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
name|filter
operator|.
name|SingleColumnValueFilter
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
name|FilterTests
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
name|MediumTests
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

begin_comment
comment|/**  * Test failure in ScanDeleteTracker.isDeleted when ROWCOL bloom filter  * is used during a scan with a filter.  */
end_comment

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|RegionServerTests
operator|.
name|class
block|,
name|FilterTests
operator|.
name|class
block|,
name|MediumTests
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|TestIsDeleteFailure
block|{
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
name|TestIsDeleteFailure
operator|.
name|class
argument_list|)
decl_stmt|;
annotation|@
name|Rule
specifier|public
name|TestName
name|name
init|=
operator|new
name|TestName
argument_list|()
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
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setInt
argument_list|(
literal|"hbase.regionserver.msginterval"
argument_list|,
literal|100
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setInt
argument_list|(
literal|"hbase.client.pause"
argument_list|,
literal|250
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setInt
argument_list|(
literal|"hbase.client.retries.number"
argument_list|,
literal|2
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setBoolean
argument_list|(
literal|"hbase.master.enabletable.roundrobin"
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|startMiniCluster
argument_list|(
literal|1
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
name|TEST_UTIL
operator|.
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testIsDeleteFailure
parameter_list|()
throws|throws
name|Exception
block|{
specifier|final
name|HTableDescriptor
name|table
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
name|name
operator|.
name|getMethodName
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
specifier|final
name|byte
index|[]
name|family
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"0"
argument_list|)
decl_stmt|;
specifier|final
name|byte
index|[]
name|c1
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"C01"
argument_list|)
decl_stmt|;
specifier|final
name|byte
index|[]
name|c2
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"C02"
argument_list|)
decl_stmt|;
specifier|final
name|byte
index|[]
name|c3
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"C03"
argument_list|)
decl_stmt|;
specifier|final
name|byte
index|[]
name|c4
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"C04"
argument_list|)
decl_stmt|;
specifier|final
name|byte
index|[]
name|c5
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"C05"
argument_list|)
decl_stmt|;
specifier|final
name|byte
index|[]
name|c6
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"C07"
argument_list|)
decl_stmt|;
specifier|final
name|byte
index|[]
name|c7
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"C07"
argument_list|)
decl_stmt|;
specifier|final
name|byte
index|[]
name|c8
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"C08"
argument_list|)
decl_stmt|;
specifier|final
name|byte
index|[]
name|c9
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"C09"
argument_list|)
decl_stmt|;
specifier|final
name|byte
index|[]
name|c10
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"C10"
argument_list|)
decl_stmt|;
specifier|final
name|byte
index|[]
name|c11
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"C11"
argument_list|)
decl_stmt|;
specifier|final
name|byte
index|[]
name|c12
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"C12"
argument_list|)
decl_stmt|;
specifier|final
name|byte
index|[]
name|c13
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"C13"
argument_list|)
decl_stmt|;
specifier|final
name|byte
index|[]
name|c14
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"C14"
argument_list|)
decl_stmt|;
specifier|final
name|byte
index|[]
name|c15
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"C15"
argument_list|)
decl_stmt|;
specifier|final
name|byte
index|[]
name|val
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"foo"
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|byte
index|[]
argument_list|>
name|fams
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
literal|1
argument_list|)
decl_stmt|;
name|fams
operator|.
name|add
argument_list|(
name|family
argument_list|)
expr_stmt|;
name|Table
name|ht
init|=
name|TEST_UTIL
operator|.
name|createTable
argument_list|(
name|table
argument_list|,
name|fams
operator|.
name|toArray
argument_list|(
operator|new
name|byte
index|[
literal|0
index|]
index|[]
argument_list|)
argument_list|,
literal|null
argument_list|,
name|BloomType
operator|.
name|ROWCOL
argument_list|,
literal|10000
argument_list|,
operator|new
name|Configuration
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Mutation
argument_list|>
name|pending
init|=
operator|new
name|ArrayList
argument_list|<
name|Mutation
argument_list|>
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
literal|1000
condition|;
name|i
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
literal|"key"
operator|+
name|Integer
operator|.
name|toString
argument_list|(
name|i
argument_list|)
argument_list|)
decl_stmt|;
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
name|addColumn
argument_list|(
name|family
argument_list|,
name|c3
argument_list|,
name|val
argument_list|)
expr_stmt|;
name|put
operator|.
name|addColumn
argument_list|(
name|family
argument_list|,
name|c4
argument_list|,
name|val
argument_list|)
expr_stmt|;
name|put
operator|.
name|addColumn
argument_list|(
name|family
argument_list|,
name|c5
argument_list|,
name|val
argument_list|)
expr_stmt|;
name|put
operator|.
name|addColumn
argument_list|(
name|family
argument_list|,
name|c6
argument_list|,
name|val
argument_list|)
expr_stmt|;
name|put
operator|.
name|addColumn
argument_list|(
name|family
argument_list|,
name|c7
argument_list|,
name|val
argument_list|)
expr_stmt|;
name|put
operator|.
name|addColumn
argument_list|(
name|family
argument_list|,
name|c8
argument_list|,
name|val
argument_list|)
expr_stmt|;
name|put
operator|.
name|addColumn
argument_list|(
name|family
argument_list|,
name|c12
argument_list|,
name|val
argument_list|)
expr_stmt|;
name|put
operator|.
name|addColumn
argument_list|(
name|family
argument_list|,
name|c13
argument_list|,
name|val
argument_list|)
expr_stmt|;
name|put
operator|.
name|addColumn
argument_list|(
name|family
argument_list|,
name|c15
argument_list|,
name|val
argument_list|)
expr_stmt|;
name|pending
operator|.
name|add
argument_list|(
name|put
argument_list|)
expr_stmt|;
name|Delete
name|del
init|=
operator|new
name|Delete
argument_list|(
name|row
argument_list|)
decl_stmt|;
name|del
operator|.
name|addColumns
argument_list|(
name|family
argument_list|,
name|c2
argument_list|)
expr_stmt|;
name|del
operator|.
name|addColumns
argument_list|(
name|family
argument_list|,
name|c9
argument_list|)
expr_stmt|;
name|del
operator|.
name|addColumns
argument_list|(
name|family
argument_list|,
name|c10
argument_list|)
expr_stmt|;
name|del
operator|.
name|addColumns
argument_list|(
name|family
argument_list|,
name|c14
argument_list|)
expr_stmt|;
name|pending
operator|.
name|add
argument_list|(
name|del
argument_list|)
expr_stmt|;
block|}
name|ht
operator|.
name|batch
argument_list|(
name|pending
argument_list|,
operator|new
name|Object
index|[
name|pending
operator|.
name|size
argument_list|()
index|]
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|flush
argument_list|()
expr_stmt|;
name|TEST_UTIL
operator|.
name|compact
argument_list|(
literal|true
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|20
init|;
name|i
operator|<
literal|300
condition|;
name|i
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
literal|"key"
operator|+
name|Integer
operator|.
name|toString
argument_list|(
name|i
argument_list|)
argument_list|)
decl_stmt|;
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
name|addColumn
argument_list|(
name|family
argument_list|,
name|c3
argument_list|,
name|val
argument_list|)
expr_stmt|;
name|put
operator|.
name|addColumn
argument_list|(
name|family
argument_list|,
name|c4
argument_list|,
name|val
argument_list|)
expr_stmt|;
name|put
operator|.
name|addColumn
argument_list|(
name|family
argument_list|,
name|c5
argument_list|,
name|val
argument_list|)
expr_stmt|;
name|put
operator|.
name|addColumn
argument_list|(
name|family
argument_list|,
name|c6
argument_list|,
name|val
argument_list|)
expr_stmt|;
name|put
operator|.
name|addColumn
argument_list|(
name|family
argument_list|,
name|c7
argument_list|,
name|val
argument_list|)
expr_stmt|;
name|put
operator|.
name|addColumn
argument_list|(
name|family
argument_list|,
name|c8
argument_list|,
name|val
argument_list|)
expr_stmt|;
name|put
operator|.
name|addColumn
argument_list|(
name|family
argument_list|,
name|c12
argument_list|,
name|val
argument_list|)
expr_stmt|;
name|put
operator|.
name|addColumn
argument_list|(
name|family
argument_list|,
name|c13
argument_list|,
name|val
argument_list|)
expr_stmt|;
name|put
operator|.
name|addColumn
argument_list|(
name|family
argument_list|,
name|c15
argument_list|,
name|val
argument_list|)
expr_stmt|;
name|pending
operator|.
name|add
argument_list|(
name|put
argument_list|)
expr_stmt|;
name|Delete
name|del
init|=
operator|new
name|Delete
argument_list|(
name|row
argument_list|)
decl_stmt|;
name|del
operator|.
name|addColumns
argument_list|(
name|family
argument_list|,
name|c2
argument_list|)
expr_stmt|;
name|del
operator|.
name|addColumns
argument_list|(
name|family
argument_list|,
name|c9
argument_list|)
expr_stmt|;
name|del
operator|.
name|addColumns
argument_list|(
name|family
argument_list|,
name|c10
argument_list|)
expr_stmt|;
name|del
operator|.
name|addColumns
argument_list|(
name|family
argument_list|,
name|c14
argument_list|)
expr_stmt|;
name|pending
operator|.
name|add
argument_list|(
name|del
argument_list|)
expr_stmt|;
block|}
name|ht
operator|.
name|batch
argument_list|(
name|pending
argument_list|,
operator|new
name|Object
index|[
name|pending
operator|.
name|size
argument_list|()
index|]
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|flush
argument_list|()
expr_stmt|;
name|Scan
name|scan
init|=
operator|new
name|Scan
argument_list|()
decl_stmt|;
name|scan
operator|.
name|addColumn
argument_list|(
name|family
argument_list|,
name|c9
argument_list|)
expr_stmt|;
name|scan
operator|.
name|addColumn
argument_list|(
name|family
argument_list|,
name|c15
argument_list|)
expr_stmt|;
name|SingleColumnValueFilter
name|filter
init|=
operator|new
name|SingleColumnValueFilter
argument_list|(
name|family
argument_list|,
name|c15
argument_list|,
name|CompareOperator
operator|.
name|EQUAL
argument_list|,
operator|new
name|BinaryComparator
argument_list|(
name|c15
argument_list|)
argument_list|)
decl_stmt|;
name|scan
operator|.
name|setFilter
argument_list|(
name|filter
argument_list|)
expr_stmt|;
comment|//Trigger the scan for not existing row, so it will scan over all rows
for|for
control|(
name|Result
name|result
range|:
name|ht
operator|.
name|getScanner
argument_list|(
name|scan
argument_list|)
control|)
block|{
name|result
operator|.
name|advance
argument_list|()
expr_stmt|;
block|}
name|ht
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

