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
name|client
operator|.
name|Admin
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
name|CompactionState
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
name|testclassification
operator|.
name|VerySlowRegionServerTests
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

begin_comment
comment|/** Unit tests to test retrieving table/region compaction state*/
end_comment

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|VerySlowRegionServerTests
operator|.
name|class
block|,
name|LargeTests
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|TestCompactionState
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
name|TestCompactionState
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
name|TestCompactionState
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
specifier|final
specifier|static
name|Random
name|random
init|=
operator|new
name|Random
argument_list|()
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
name|startMiniCluster
argument_list|()
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
name|testMajorCompaction
parameter_list|()
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|compaction
argument_list|(
name|name
operator|.
name|getMethodName
argument_list|()
argument_list|,
literal|8
argument_list|,
name|CompactionState
operator|.
name|MAJOR
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testMinorCompaction
parameter_list|()
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|compaction
argument_list|(
name|name
operator|.
name|getMethodName
argument_list|()
argument_list|,
literal|15
argument_list|,
name|CompactionState
operator|.
name|MINOR
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testMajorCompactionOnFamily
parameter_list|()
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|compaction
argument_list|(
name|name
operator|.
name|getMethodName
argument_list|()
argument_list|,
literal|8
argument_list|,
name|CompactionState
operator|.
name|MAJOR
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testMinorCompactionOnFamily
parameter_list|()
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|compaction
argument_list|(
name|name
operator|.
name|getMethodName
argument_list|()
argument_list|,
literal|15
argument_list|,
name|CompactionState
operator|.
name|MINOR
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testInvalidColumnFamily
parameter_list|()
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
specifier|final
name|TableName
name|tableName
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
name|name
operator|.
name|getMethodName
argument_list|()
argument_list|)
decl_stmt|;
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
name|byte
index|[]
name|fakecf
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"fakecf"
argument_list|)
decl_stmt|;
name|boolean
name|caughtMinorCompact
init|=
literal|false
decl_stmt|;
name|boolean
name|caughtMajorCompact
init|=
literal|false
decl_stmt|;
name|Table
name|ht
init|=
literal|null
decl_stmt|;
try|try
block|{
name|ht
operator|=
name|TEST_UTIL
operator|.
name|createTable
argument_list|(
name|tableName
argument_list|,
name|family
argument_list|)
expr_stmt|;
name|Admin
name|admin
init|=
name|TEST_UTIL
operator|.
name|getAdmin
argument_list|()
decl_stmt|;
try|try
block|{
name|admin
operator|.
name|compact
argument_list|(
name|tableName
argument_list|,
name|fakecf
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|ioe
parameter_list|)
block|{
name|caughtMinorCompact
operator|=
literal|true
expr_stmt|;
block|}
try|try
block|{
name|admin
operator|.
name|majorCompact
argument_list|(
name|tableName
argument_list|,
name|fakecf
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|ioe
parameter_list|)
block|{
name|caughtMajorCompact
operator|=
literal|true
expr_stmt|;
block|}
block|}
finally|finally
block|{
if|if
condition|(
name|ht
operator|!=
literal|null
condition|)
block|{
name|TEST_UTIL
operator|.
name|deleteTable
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
block|}
name|assertTrue
argument_list|(
name|caughtMinorCompact
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|caughtMajorCompact
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Load data to a table, flush it to disk, trigger compaction,    * confirm the compaction state is right and wait till it is done.    *    * @param tableName    * @param flushes    * @param expectedState    * @param singleFamily otherwise, run compaction on all cfs    * @throws IOException    * @throws InterruptedException    */
specifier|private
name|void
name|compaction
parameter_list|(
specifier|final
name|String
name|tableName
parameter_list|,
specifier|final
name|int
name|flushes
parameter_list|,
specifier|final
name|CompactionState
name|expectedState
parameter_list|,
name|boolean
name|singleFamily
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
comment|// Create a table with regions
name|TableName
name|table
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
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
name|byte
index|[]
index|[]
name|families
init|=
block|{
name|family
block|,
name|Bytes
operator|.
name|add
argument_list|(
name|family
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"2"
argument_list|)
argument_list|)
block|,
name|Bytes
operator|.
name|add
argument_list|(
name|family
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"3"
argument_list|)
argument_list|)
block|}
decl_stmt|;
name|Table
name|ht
init|=
literal|null
decl_stmt|;
try|try
block|{
name|ht
operator|=
name|TEST_UTIL
operator|.
name|createTable
argument_list|(
name|table
argument_list|,
name|families
argument_list|)
expr_stmt|;
name|loadData
argument_list|(
name|ht
argument_list|,
name|families
argument_list|,
literal|3000
argument_list|,
name|flushes
argument_list|)
expr_stmt|;
name|HRegionServer
name|rs
init|=
name|TEST_UTIL
operator|.
name|getMiniHBaseCluster
argument_list|()
operator|.
name|getRegionServer
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|HRegion
argument_list|>
name|regions
init|=
name|rs
operator|.
name|getRegions
argument_list|(
name|table
argument_list|)
decl_stmt|;
name|int
name|countBefore
init|=
name|countStoreFilesInFamilies
argument_list|(
name|regions
argument_list|,
name|families
argument_list|)
decl_stmt|;
name|int
name|countBeforeSingleFamily
init|=
name|countStoreFilesInFamily
argument_list|(
name|regions
argument_list|,
name|family
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|countBefore
operator|>
literal|0
argument_list|)
expr_stmt|;
comment|// there should be some data files
name|Admin
name|admin
init|=
name|TEST_UTIL
operator|.
name|getAdmin
argument_list|()
decl_stmt|;
if|if
condition|(
name|expectedState
operator|==
name|CompactionState
operator|.
name|MINOR
condition|)
block|{
if|if
condition|(
name|singleFamily
condition|)
block|{
name|admin
operator|.
name|compact
argument_list|(
name|table
argument_list|,
name|family
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|admin
operator|.
name|compact
argument_list|(
name|table
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
if|if
condition|(
name|singleFamily
condition|)
block|{
name|admin
operator|.
name|majorCompact
argument_list|(
name|table
argument_list|,
name|family
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|admin
operator|.
name|majorCompact
argument_list|(
name|table
argument_list|)
expr_stmt|;
block|}
block|}
name|long
name|curt
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|long
name|waitTime
init|=
literal|5000
decl_stmt|;
name|long
name|endt
init|=
name|curt
operator|+
name|waitTime
decl_stmt|;
name|CompactionState
name|state
init|=
name|admin
operator|.
name|getCompactionState
argument_list|(
name|table
argument_list|)
decl_stmt|;
while|while
condition|(
name|state
operator|==
name|CompactionState
operator|.
name|NONE
operator|&&
name|curt
operator|<
name|endt
condition|)
block|{
name|Thread
operator|.
name|sleep
argument_list|(
literal|10
argument_list|)
expr_stmt|;
name|state
operator|=
name|admin
operator|.
name|getCompactionState
argument_list|(
name|table
argument_list|)
expr_stmt|;
name|curt
operator|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
expr_stmt|;
block|}
comment|// Now, should have the right compaction state,
comment|// otherwise, the compaction should have already been done
if|if
condition|(
name|expectedState
operator|!=
name|state
condition|)
block|{
for|for
control|(
name|Region
name|region
range|:
name|regions
control|)
block|{
name|state
operator|=
name|CompactionState
operator|.
name|valueOf
argument_list|(
name|region
operator|.
name|getCompactionState
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|CompactionState
operator|.
name|NONE
argument_list|,
name|state
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
comment|// Wait until the compaction is done
name|state
operator|=
name|admin
operator|.
name|getCompactionState
argument_list|(
name|table
argument_list|)
expr_stmt|;
while|while
condition|(
name|state
operator|!=
name|CompactionState
operator|.
name|NONE
operator|&&
name|curt
operator|<
name|endt
condition|)
block|{
name|Thread
operator|.
name|sleep
argument_list|(
literal|10
argument_list|)
expr_stmt|;
name|state
operator|=
name|admin
operator|.
name|getCompactionState
argument_list|(
name|table
argument_list|)
expr_stmt|;
block|}
comment|// Now, compaction should be done.
name|assertEquals
argument_list|(
name|CompactionState
operator|.
name|NONE
argument_list|,
name|state
argument_list|)
expr_stmt|;
block|}
name|int
name|countAfter
init|=
name|countStoreFilesInFamilies
argument_list|(
name|regions
argument_list|,
name|families
argument_list|)
decl_stmt|;
name|int
name|countAfterSingleFamily
init|=
name|countStoreFilesInFamily
argument_list|(
name|regions
argument_list|,
name|family
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|countAfter
operator|<
name|countBefore
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|singleFamily
condition|)
block|{
if|if
condition|(
name|expectedState
operator|==
name|CompactionState
operator|.
name|MAJOR
condition|)
name|assertTrue
argument_list|(
name|families
operator|.
name|length
operator|==
name|countAfter
argument_list|)
expr_stmt|;
else|else
name|assertTrue
argument_list|(
name|families
operator|.
name|length
operator|<
name|countAfter
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|int
name|singleFamDiff
init|=
name|countBeforeSingleFamily
operator|-
name|countAfterSingleFamily
decl_stmt|;
comment|// assert only change was to single column family
name|assertTrue
argument_list|(
name|singleFamDiff
operator|==
operator|(
name|countBefore
operator|-
name|countAfter
operator|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|expectedState
operator|==
name|CompactionState
operator|.
name|MAJOR
condition|)
block|{
name|assertTrue
argument_list|(
literal|1
operator|==
name|countAfterSingleFamily
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|assertTrue
argument_list|(
literal|1
operator|<
name|countAfterSingleFamily
argument_list|)
expr_stmt|;
block|}
block|}
block|}
finally|finally
block|{
if|if
condition|(
name|ht
operator|!=
literal|null
condition|)
block|{
name|TEST_UTIL
operator|.
name|deleteTable
argument_list|(
name|table
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|private
specifier|static
name|int
name|countStoreFilesInFamily
parameter_list|(
name|List
argument_list|<
name|HRegion
argument_list|>
name|regions
parameter_list|,
specifier|final
name|byte
index|[]
name|family
parameter_list|)
block|{
return|return
name|countStoreFilesInFamilies
argument_list|(
name|regions
argument_list|,
operator|new
name|byte
index|[]
index|[]
block|{
name|family
block|}
argument_list|)
return|;
block|}
specifier|private
specifier|static
name|int
name|countStoreFilesInFamilies
parameter_list|(
name|List
argument_list|<
name|HRegion
argument_list|>
name|regions
parameter_list|,
specifier|final
name|byte
index|[]
index|[]
name|families
parameter_list|)
block|{
name|int
name|count
init|=
literal|0
decl_stmt|;
for|for
control|(
name|HRegion
name|region
range|:
name|regions
control|)
block|{
name|count
operator|+=
name|region
operator|.
name|getStoreFileList
argument_list|(
name|families
argument_list|)
operator|.
name|size
argument_list|()
expr_stmt|;
block|}
return|return
name|count
return|;
block|}
specifier|private
specifier|static
name|void
name|loadData
parameter_list|(
specifier|final
name|Table
name|ht
parameter_list|,
specifier|final
name|byte
index|[]
index|[]
name|families
parameter_list|,
specifier|final
name|int
name|rows
parameter_list|,
specifier|final
name|int
name|flushes
parameter_list|)
throws|throws
name|IOException
block|{
name|List
argument_list|<
name|Put
argument_list|>
name|puts
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|rows
argument_list|)
decl_stmt|;
name|byte
index|[]
name|qualifier
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"val"
argument_list|)
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
name|flushes
condition|;
name|i
operator|++
control|)
block|{
for|for
control|(
name|int
name|k
init|=
literal|0
init|;
name|k
operator|<
name|rows
condition|;
name|k
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
name|random
operator|.
name|nextLong
argument_list|()
argument_list|)
decl_stmt|;
name|Put
name|p
init|=
operator|new
name|Put
argument_list|(
name|row
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
name|families
operator|.
name|length
condition|;
operator|++
name|j
control|)
block|{
name|p
operator|.
name|addColumn
argument_list|(
name|families
index|[
name|j
index|]
argument_list|,
name|qualifier
argument_list|,
name|row
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
block|}
name|ht
operator|.
name|put
argument_list|(
name|puts
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|flush
argument_list|()
expr_stmt|;
name|puts
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

