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
name|HTableUtil
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
name|Row
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
name|io
operator|.
name|hfile
operator|.
name|BlockCache
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
name|hfile
operator|.
name|BlockCacheColumnFamilySummary
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
name|hfile
operator|.
name|CacheConfig
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
comment|/**  * Tests the block cache summary functionality in StoreFile,   * which contains the BlockCache  *  */
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
name|TestStoreFileBlockCacheSummary
block|{
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|getClass
argument_list|()
argument_list|)
decl_stmt|;
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
specifier|static
specifier|final
name|String
name|TEST_TABLE
init|=
literal|"testTable"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|TEST_TABLE2
init|=
literal|"testTable2"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|TEST_CF
init|=
literal|"testFamily"
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
name|TEST_CF
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|byte
index|[]
name|QUALIFIER
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"testQualifier"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|byte
index|[]
name|VALUE
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"testValue"
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|int
name|TOTAL_ROWS
init|=
literal|4
decl_stmt|;
comment|/**    * @throws java.lang.Exception exception    */
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
name|startMiniCluster
argument_list|()
expr_stmt|;
block|}
comment|/**    * @throws java.lang.Exception exception    */
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
specifier|private
name|Put
name|createPut
parameter_list|(
name|byte
index|[]
name|family
parameter_list|,
name|String
name|row
parameter_list|)
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
name|row
argument_list|)
argument_list|)
decl_stmt|;
name|put
operator|.
name|add
argument_list|(
name|family
argument_list|,
name|QUALIFIER
argument_list|,
name|VALUE
argument_list|)
expr_stmt|;
return|return
name|put
return|;
block|}
comment|/**   * This test inserts data into multiple tables and then reads both tables to ensure   * they are in the block cache.   *   * @throws Exception exception   */
annotation|@
name|Test
specifier|public
name|void
name|testBlockCacheSummary
parameter_list|()
throws|throws
name|Exception
block|{
name|HTable
name|ht
init|=
name|TEST_UTIL
operator|.
name|createTable
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|TEST_TABLE
argument_list|)
argument_list|,
name|FAMILY
argument_list|)
decl_stmt|;
name|addRows
argument_list|(
name|ht
argument_list|,
name|FAMILY
argument_list|)
expr_stmt|;
name|HTable
name|ht2
init|=
name|TEST_UTIL
operator|.
name|createTable
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|TEST_TABLE2
argument_list|)
argument_list|,
name|FAMILY
argument_list|)
decl_stmt|;
name|addRows
argument_list|(
name|ht2
argument_list|,
name|FAMILY
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|flush
argument_list|()
expr_stmt|;
name|scan
argument_list|(
name|ht
argument_list|,
name|FAMILY
argument_list|)
expr_stmt|;
name|scan
argument_list|(
name|ht2
argument_list|,
name|FAMILY
argument_list|)
expr_stmt|;
name|BlockCache
name|bc
init|=
operator|new
name|CacheConfig
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
operator|.
name|getBlockCache
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|BlockCacheColumnFamilySummary
argument_list|>
name|bcs
init|=
name|bc
operator|.
name|getBlockCacheColumnFamilySummaries
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"blockCacheSummary: "
operator|+
name|bcs
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"blockCache summary has entries"
argument_list|,
literal|2
argument_list|,
name|bcs
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|BlockCacheColumnFamilySummary
name|e
init|=
name|bcs
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"table"
argument_list|,
name|TEST_TABLE
argument_list|,
name|e
operator|.
name|getTable
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"cf"
argument_list|,
name|TEST_CF
argument_list|,
name|e
operator|.
name|getColumnFamily
argument_list|()
argument_list|)
expr_stmt|;
name|e
operator|=
name|bcs
operator|.
name|get
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"table"
argument_list|,
name|TEST_TABLE2
argument_list|,
name|e
operator|.
name|getTable
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"cf"
argument_list|,
name|TEST_CF
argument_list|,
name|e
operator|.
name|getColumnFamily
argument_list|()
argument_list|)
expr_stmt|;
name|ht
operator|.
name|close
argument_list|()
expr_stmt|;
name|ht2
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
specifier|private
name|void
name|addRows
parameter_list|(
name|HTable
name|ht
parameter_list|,
name|byte
index|[]
name|family
parameter_list|)
throws|throws
name|IOException
block|{
name|List
argument_list|<
name|Row
argument_list|>
name|rows
init|=
operator|new
name|ArrayList
argument_list|<
name|Row
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
name|TOTAL_ROWS
condition|;
name|i
operator|++
control|)
block|{
name|rows
operator|.
name|add
argument_list|(
name|createPut
argument_list|(
name|family
argument_list|,
literal|"row"
operator|+
name|i
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|HTableUtil
operator|.
name|bucketRsBatch
argument_list|(
name|ht
argument_list|,
name|rows
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|scan
parameter_list|(
name|HTable
name|ht
parameter_list|,
name|byte
index|[]
name|family
parameter_list|)
throws|throws
name|IOException
block|{
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
name|QUALIFIER
argument_list|)
expr_stmt|;
name|int
name|count
init|=
literal|0
decl_stmt|;
for|for
control|(
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unused"
argument_list|)
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
name|count
operator|++
expr_stmt|;
block|}
if|if
condition|(
name|TOTAL_ROWS
operator|!=
name|count
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Incorrect number of rows!"
argument_list|)
throw|;
block|}
block|}
block|}
end_class

end_unit

