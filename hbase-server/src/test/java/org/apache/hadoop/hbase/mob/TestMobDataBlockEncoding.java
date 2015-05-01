begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/** * * Licensed to the Apache Software Foundation (ASF) under one * or more contributor license agreements.  See the NOTICE file * distributed with this work for additional information * regarding copyright ownership.  The ASF licenses this file * to you under the Apache License, Version 2.0 (the * "License"); you may not use this file except in compliance * with the License.  You may obtain a copy of the License at * *     http://www.apache.org/licenses/LICENSE-2.0 * * Unless required by applicable law or agreed to in writing, software * distributed under the License is distributed on an "AS IS" BASIS, * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. * See the License for the specific language governing permissions and * limitations under the License. */
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
name|mob
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
name|io
operator|.
name|encoding
operator|.
name|DataBlockEncoding
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
name|Assert
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
name|MediumTests
operator|.
name|class
argument_list|)
specifier|public
class|class
name|TestMobDataBlockEncoding
block|{
specifier|private
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
specifier|final
specifier|static
name|byte
index|[]
name|row1
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"row1"
argument_list|)
decl_stmt|;
specifier|private
specifier|final
specifier|static
name|byte
index|[]
name|family
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"family"
argument_list|)
decl_stmt|;
specifier|private
specifier|final
specifier|static
name|byte
index|[]
name|qf1
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"qualifier1"
argument_list|)
decl_stmt|;
specifier|private
specifier|final
specifier|static
name|byte
index|[]
name|qf2
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"qualifier2"
argument_list|)
decl_stmt|;
specifier|protected
specifier|final
name|byte
index|[]
name|qf3
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"qualifier3"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|Table
name|table
decl_stmt|;
specifier|private
specifier|static
name|HBaseAdmin
name|admin
decl_stmt|;
specifier|private
specifier|static
name|HColumnDescriptor
name|hcd
decl_stmt|;
specifier|private
specifier|static
name|HTableDescriptor
name|desc
decl_stmt|;
specifier|private
specifier|static
name|Random
name|random
init|=
operator|new
name|Random
argument_list|()
decl_stmt|;
specifier|private
specifier|static
name|long
name|defaultThreshold
init|=
literal|10
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
literal|"hbase.master.info.port"
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setBoolean
argument_list|(
literal|"hbase.regionserver.info.port.auto"
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
specifier|public
name|void
name|setUp
parameter_list|(
name|long
name|threshold
parameter_list|,
name|String
name|TN
parameter_list|,
name|DataBlockEncoding
name|encoding
parameter_list|)
throws|throws
name|Exception
block|{
name|desc
operator|=
operator|new
name|HTableDescriptor
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
name|TN
argument_list|)
argument_list|)
expr_stmt|;
name|hcd
operator|=
operator|new
name|HColumnDescriptor
argument_list|(
name|family
argument_list|)
expr_stmt|;
name|hcd
operator|.
name|setMobEnabled
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|hcd
operator|.
name|setMobThreshold
argument_list|(
name|threshold
argument_list|)
expr_stmt|;
name|hcd
operator|.
name|setMaxVersions
argument_list|(
literal|4
argument_list|)
expr_stmt|;
name|hcd
operator|.
name|setDataBlockEncoding
argument_list|(
name|encoding
argument_list|)
expr_stmt|;
name|desc
operator|.
name|addFamily
argument_list|(
name|hcd
argument_list|)
expr_stmt|;
name|admin
operator|=
name|TEST_UTIL
operator|.
name|getHBaseAdmin
argument_list|()
expr_stmt|;
name|admin
operator|.
name|createTable
argument_list|(
name|desc
argument_list|)
expr_stmt|;
name|table
operator|=
name|ConnectionFactory
operator|.
name|createConnection
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
operator|.
name|getTable
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
name|TN
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * Generate the mob value.    *    * @param size the size of the value    * @return the mob value generated    */
specifier|private
specifier|static
name|byte
index|[]
name|generateMobValue
parameter_list|(
name|int
name|size
parameter_list|)
block|{
name|byte
index|[]
name|mobVal
init|=
operator|new
name|byte
index|[
name|size
index|]
decl_stmt|;
name|random
operator|.
name|nextBytes
argument_list|(
name|mobVal
argument_list|)
expr_stmt|;
return|return
name|mobVal
return|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testDataBlockEncoding
parameter_list|()
throws|throws
name|Exception
block|{
for|for
control|(
name|DataBlockEncoding
name|encoding
range|:
name|DataBlockEncoding
operator|.
name|values
argument_list|()
control|)
block|{
name|testDataBlockEncoding
argument_list|(
name|encoding
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|void
name|testDataBlockEncoding
parameter_list|(
name|DataBlockEncoding
name|encoding
parameter_list|)
throws|throws
name|Exception
block|{
name|String
name|TN
init|=
literal|"testDataBlockEncoding"
operator|+
name|encoding
decl_stmt|;
name|setUp
argument_list|(
name|defaultThreshold
argument_list|,
name|TN
argument_list|,
name|encoding
argument_list|)
expr_stmt|;
name|long
name|ts1
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|long
name|ts2
init|=
name|ts1
operator|+
literal|1
decl_stmt|;
name|long
name|ts3
init|=
name|ts1
operator|+
literal|2
decl_stmt|;
name|byte
index|[]
name|value
init|=
name|generateMobValue
argument_list|(
operator|(
name|int
operator|)
name|defaultThreshold
operator|+
literal|1
argument_list|)
decl_stmt|;
name|Put
name|put1
init|=
operator|new
name|Put
argument_list|(
name|row1
argument_list|)
decl_stmt|;
name|put1
operator|.
name|addColumn
argument_list|(
name|family
argument_list|,
name|qf1
argument_list|,
name|ts3
argument_list|,
name|value
argument_list|)
expr_stmt|;
name|put1
operator|.
name|addColumn
argument_list|(
name|family
argument_list|,
name|qf2
argument_list|,
name|ts2
argument_list|,
name|value
argument_list|)
expr_stmt|;
name|put1
operator|.
name|addColumn
argument_list|(
name|family
argument_list|,
name|qf3
argument_list|,
name|ts1
argument_list|,
name|value
argument_list|)
expr_stmt|;
name|table
operator|.
name|put
argument_list|(
name|put1
argument_list|)
expr_stmt|;
name|admin
operator|.
name|flush
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
name|TN
argument_list|)
argument_list|)
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
name|setMaxVersions
argument_list|(
literal|4
argument_list|)
expr_stmt|;
name|ResultScanner
name|results
init|=
name|table
operator|.
name|getScanner
argument_list|(
name|scan
argument_list|)
decl_stmt|;
name|int
name|count
init|=
literal|0
decl_stmt|;
for|for
control|(
name|Result
name|res
range|:
name|results
control|)
block|{
name|List
argument_list|<
name|Cell
argument_list|>
name|cells
init|=
name|res
operator|.
name|listCells
argument_list|()
decl_stmt|;
for|for
control|(
name|Cell
name|cell
range|:
name|cells
control|)
block|{
comment|// Verify the value
name|Assert
operator|.
name|assertEquals
argument_list|(
name|Bytes
operator|.
name|toString
argument_list|(
name|value
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toString
argument_list|(
name|CellUtil
operator|.
name|cloneValue
argument_list|(
name|cell
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|count
operator|++
expr_stmt|;
block|}
block|}
name|results
operator|.
name|close
argument_list|()
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|3
argument_list|,
name|count
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

