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
name|protobuf
operator|.
name|generated
operator|.
name|AdminProtos
operator|.
name|GetRegionInfoResponse
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
name|regionserver
operator|.
name|compactions
operator|.
name|CompactionRequest
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
comment|/** Unit tests to test retrieving table/region compaction state*/
end_comment

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
name|TestCompactionState
block|{
specifier|final
specifier|static
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
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
argument_list|(
name|timeout
operator|=
literal|60000
argument_list|)
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
literal|"testMajorCompaction"
argument_list|,
literal|8
argument_list|,
name|CompactionState
operator|.
name|MAJOR
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|60000
argument_list|)
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
literal|"testMinorCompaction"
argument_list|,
literal|15
argument_list|,
name|CompactionState
operator|.
name|MINOR
argument_list|)
expr_stmt|;
block|}
comment|/**    * Load data to a table, flush it to disk, trigger compaction,    * confirm the compaction state is right and wait till it is done.    *    * @param tableName    * @param flushes    * @param expectedState    * @throws IOException    * @throws InterruptedException    */
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
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
comment|// Create a table with regions
name|byte
index|[]
name|table
init|=
name|Bytes
operator|.
name|toBytes
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
name|HTable
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
name|family
argument_list|)
expr_stmt|;
name|loadData
argument_list|(
name|ht
argument_list|,
name|family
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
name|getOnlineRegions
argument_list|(
name|table
argument_list|)
decl_stmt|;
name|int
name|countBefore
init|=
name|countStoreFiles
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
name|HBaseAdmin
name|admin
init|=
operator|new
name|HBaseAdmin
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
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
name|admin
operator|.
name|compact
argument_list|(
name|tableName
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
name|HRegion
name|region
range|:
name|regions
control|)
block|{
name|state
operator|=
name|CompactionRequest
operator|.
name|getCompactionState
argument_list|(
name|region
operator|.
name|getRegionId
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
name|curt
operator|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
expr_stmt|;
name|waitTime
operator|=
literal|20000
expr_stmt|;
name|endt
operator|=
name|curt
operator|+
name|waitTime
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
name|curt
operator|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
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
name|countStoreFiles
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
name|expectedState
operator|==
name|CompactionState
operator|.
name|MAJOR
condition|)
name|assertTrue
argument_list|(
literal|1
operator|==
name|countAfter
argument_list|)
expr_stmt|;
else|else
name|assertTrue
argument_list|(
literal|1
operator|<
name|countAfter
argument_list|)
expr_stmt|;
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
name|countStoreFiles
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
operator|new
name|byte
index|[]
index|[]
block|{
name|family
block|}
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
name|HTable
name|ht
parameter_list|,
specifier|final
name|byte
index|[]
name|family
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
argument_list|<
name|Put
argument_list|>
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
name|p
operator|.
name|add
argument_list|(
name|family
argument_list|,
name|qualifier
argument_list|,
name|row
argument_list|)
expr_stmt|;
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
name|ht
operator|.
name|flushCommits
argument_list|()
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

