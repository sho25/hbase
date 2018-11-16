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
name|assertNotEquals
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

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|MediumTests
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
name|TestRegionLocationCaching
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
name|TestRegionLocationCaching
operator|.
name|class
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
name|int
name|SLAVES
init|=
literal|1
decl_stmt|;
specifier|private
specifier|static
name|int
name|PER_REGIONSERVER_QUEUE_SIZE
init|=
literal|100000
decl_stmt|;
specifier|private
specifier|static
name|TableName
name|TABLE_NAME
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"TestRegionLocationCaching"
argument_list|)
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
argument_list|(
name|SLAVES
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|createTable
argument_list|(
name|TABLE_NAME
argument_list|,
operator|new
name|byte
index|[]
index|[]
block|{
name|FAMILY
block|}
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|waitUntilAllRegionsAssigned
argument_list|(
name|TABLE_NAME
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
name|testCachingForHTableMultiplexerSinglePut
parameter_list|()
throws|throws
name|Exception
block|{
name|HTableMultiplexer
name|multiplexer
init|=
operator|new
name|HTableMultiplexer
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|PER_REGIONSERVER_QUEUE_SIZE
argument_list|)
decl_stmt|;
name|byte
index|[]
name|row
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"htable_multiplexer_single_put"
argument_list|)
decl_stmt|;
name|byte
index|[]
name|value
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"value"
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
name|FAMILY
argument_list|,
name|QUALIFIER
argument_list|,
name|value
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"Put request not accepted by multiplexer queue"
argument_list|,
name|multiplexer
operator|.
name|put
argument_list|(
name|TABLE_NAME
argument_list|,
name|put
argument_list|)
argument_list|)
expr_stmt|;
name|checkRegionLocationIsCached
argument_list|(
name|TABLE_NAME
argument_list|,
name|multiplexer
operator|.
name|getConnection
argument_list|()
argument_list|)
expr_stmt|;
name|checkExistence
argument_list|(
name|TABLE_NAME
argument_list|,
name|row
argument_list|,
name|FAMILY
argument_list|,
name|QUALIFIER
argument_list|)
expr_stmt|;
name|multiplexer
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testCachingForHTableMultiplexerMultiPut
parameter_list|()
throws|throws
name|Exception
block|{
name|HTableMultiplexer
name|multiplexer
init|=
operator|new
name|HTableMultiplexer
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|PER_REGIONSERVER_QUEUE_SIZE
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Put
argument_list|>
name|multiput
init|=
operator|new
name|ArrayList
argument_list|<
name|Put
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
literal|10
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
literal|"htable_multiplexer_multi_put"
operator|+
name|i
argument_list|)
argument_list|)
decl_stmt|;
name|byte
index|[]
name|value
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"value_"
operator|+
name|i
argument_list|)
decl_stmt|;
name|put
operator|.
name|addColumn
argument_list|(
name|FAMILY
argument_list|,
name|QUALIFIER
argument_list|,
name|value
argument_list|)
expr_stmt|;
name|multiput
operator|.
name|add
argument_list|(
name|put
argument_list|)
expr_stmt|;
block|}
name|List
argument_list|<
name|Put
argument_list|>
name|failedPuts
init|=
name|multiplexer
operator|.
name|put
argument_list|(
name|TABLE_NAME
argument_list|,
name|multiput
argument_list|)
decl_stmt|;
name|assertNull
argument_list|(
literal|"All put requests were not accepted by multiplexer queue"
argument_list|,
name|failedPuts
argument_list|)
expr_stmt|;
name|checkRegionLocationIsCached
argument_list|(
name|TABLE_NAME
argument_list|,
name|multiplexer
operator|.
name|getConnection
argument_list|()
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
literal|10
condition|;
name|i
operator|++
control|)
block|{
name|checkExistence
argument_list|(
name|TABLE_NAME
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"htable_multiplexer_multi_put"
operator|+
name|i
argument_list|)
argument_list|,
name|FAMILY
argument_list|,
name|QUALIFIER
argument_list|)
expr_stmt|;
block|}
name|multiplexer
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testCachingForHTableSinglePut
parameter_list|()
throws|throws
name|Exception
block|{
name|byte
index|[]
name|row
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"htable_single_put"
argument_list|)
decl_stmt|;
name|byte
index|[]
name|value
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"value"
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
name|FAMILY
argument_list|,
name|QUALIFIER
argument_list|,
name|value
argument_list|)
expr_stmt|;
try|try
init|(
name|Table
name|table
init|=
name|TEST_UTIL
operator|.
name|getConnection
argument_list|()
operator|.
name|getTable
argument_list|(
name|TABLE_NAME
argument_list|)
init|)
block|{
name|table
operator|.
name|put
argument_list|(
name|put
argument_list|)
expr_stmt|;
block|}
name|checkRegionLocationIsCached
argument_list|(
name|TABLE_NAME
argument_list|,
name|TEST_UTIL
operator|.
name|getConnection
argument_list|()
argument_list|)
expr_stmt|;
name|checkExistence
argument_list|(
name|TABLE_NAME
argument_list|,
name|row
argument_list|,
name|FAMILY
argument_list|,
name|QUALIFIER
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testCachingForHTableMultiPut
parameter_list|()
throws|throws
name|Exception
block|{
name|List
argument_list|<
name|Put
argument_list|>
name|multiput
init|=
operator|new
name|ArrayList
argument_list|<
name|Put
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
literal|10
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
literal|"htable_multi_put"
operator|+
name|i
argument_list|)
argument_list|)
decl_stmt|;
name|byte
index|[]
name|value
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"value_"
operator|+
name|i
argument_list|)
decl_stmt|;
name|put
operator|.
name|addColumn
argument_list|(
name|FAMILY
argument_list|,
name|QUALIFIER
argument_list|,
name|value
argument_list|)
expr_stmt|;
name|multiput
operator|.
name|add
argument_list|(
name|put
argument_list|)
expr_stmt|;
block|}
try|try
init|(
name|Table
name|table
init|=
name|TEST_UTIL
operator|.
name|getConnection
argument_list|()
operator|.
name|getTable
argument_list|(
name|TABLE_NAME
argument_list|)
init|)
block|{
name|table
operator|.
name|put
argument_list|(
name|multiput
argument_list|)
expr_stmt|;
block|}
name|checkRegionLocationIsCached
argument_list|(
name|TABLE_NAME
argument_list|,
name|TEST_UTIL
operator|.
name|getConnection
argument_list|()
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
literal|10
condition|;
name|i
operator|++
control|)
block|{
name|checkExistence
argument_list|(
name|TABLE_NAME
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"htable_multi_put"
operator|+
name|i
argument_list|)
argument_list|,
name|FAMILY
argument_list|,
name|QUALIFIER
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Method to check whether the cached region location is non-empty for the given table. It repeats    * the same check several times as clearing of cache by some async operations may not reflect    * immediately.    */
specifier|private
name|void
name|checkRegionLocationIsCached
parameter_list|(
specifier|final
name|TableName
name|tableName
parameter_list|,
specifier|final
name|Connection
name|conn
parameter_list|)
throws|throws
name|InterruptedException
throws|,
name|IOException
block|{
for|for
control|(
name|int
name|count
init|=
literal|0
init|;
name|count
operator|<
literal|50
condition|;
name|count
operator|++
control|)
block|{
name|int
name|number
init|=
operator|(
operator|(
name|ConnectionImplementation
operator|)
name|conn
operator|)
operator|.
name|getNumberOfCachedRegionLocations
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
name|assertNotEquals
argument_list|(
literal|"Expected non-zero number of cached region locations"
argument_list|,
literal|0
argument_list|,
name|number
argument_list|)
expr_stmt|;
name|Thread
operator|.
name|sleep
argument_list|(
literal|100
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Method to check whether the passed row exists in the given table    */
specifier|private
specifier|static
name|void
name|checkExistence
parameter_list|(
specifier|final
name|TableName
name|tableName
parameter_list|,
specifier|final
name|byte
index|[]
name|row
parameter_list|,
specifier|final
name|byte
index|[]
name|family
parameter_list|,
specifier|final
name|byte
index|[]
name|qualifier
parameter_list|)
throws|throws
name|Exception
block|{
comment|// verify that the row exists
name|Result
name|r
decl_stmt|;
name|Get
name|get
init|=
operator|new
name|Get
argument_list|(
name|row
argument_list|)
decl_stmt|;
name|get
operator|.
name|addColumn
argument_list|(
name|family
argument_list|,
name|qualifier
argument_list|)
expr_stmt|;
name|int
name|nbTry
init|=
literal|0
decl_stmt|;
try|try
init|(
name|Table
name|table
init|=
name|TEST_UTIL
operator|.
name|getConnection
argument_list|()
operator|.
name|getTable
argument_list|(
name|tableName
argument_list|)
init|)
block|{
do|do
block|{
name|assertTrue
argument_list|(
literal|"Failed to get row after "
operator|+
name|nbTry
operator|+
literal|" tries"
argument_list|,
name|nbTry
operator|<
literal|50
argument_list|)
expr_stmt|;
name|nbTry
operator|++
expr_stmt|;
name|Thread
operator|.
name|sleep
argument_list|(
literal|100
argument_list|)
expr_stmt|;
name|r
operator|=
name|table
operator|.
name|get
argument_list|(
name|get
argument_list|)
expr_stmt|;
block|}
do|while
condition|(
name|r
operator|==
literal|null
operator|||
name|r
operator|.
name|getValue
argument_list|(
name|family
argument_list|,
name|qualifier
argument_list|)
operator|==
literal|null
condition|)
do|;
block|}
block|}
block|}
end_class

end_unit
