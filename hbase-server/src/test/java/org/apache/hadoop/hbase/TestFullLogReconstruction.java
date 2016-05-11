begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|MiscTests
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

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|MiscTests
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
name|TestFullLogReconstruction
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
name|TableName
name|TABLE_NAME
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"tabletest"
argument_list|)
decl_stmt|;
specifier|private
specifier|final
specifier|static
name|byte
index|[]
name|FAMILY
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"family"
argument_list|)
decl_stmt|;
comment|/**    * @throws java.lang.Exception    */
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
name|Configuration
name|c
init|=
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
name|c
operator|.
name|setBoolean
argument_list|(
literal|"dfs.support.append"
argument_list|,
literal|true
argument_list|)
expr_stmt|;
comment|// quicker heartbeat interval for faster DN death notification
name|c
operator|.
name|setInt
argument_list|(
literal|"dfs.namenode.heartbeat.recheck-interval"
argument_list|,
literal|5000
argument_list|)
expr_stmt|;
name|c
operator|.
name|setInt
argument_list|(
literal|"dfs.heartbeat.interval"
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|c
operator|.
name|setInt
argument_list|(
literal|"dfs.client.socket-timeout"
argument_list|,
literal|5000
argument_list|)
expr_stmt|;
comment|// faster failover with cluster.shutdown();fs.close() idiom
name|c
operator|.
name|setInt
argument_list|(
literal|"hbase.ipc.client.connect.max.retries"
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|c
operator|.
name|setInt
argument_list|(
literal|"dfs.client.block.recovery.retries"
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|c
operator|.
name|setInt
argument_list|(
name|HConstants
operator|.
name|ZK_SESSION_TIMEOUT
argument_list|,
literal|1000
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|startMiniCluster
argument_list|(
literal|3
argument_list|)
expr_stmt|;
block|}
comment|/**    * @throws java.lang.Exception    */
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
comment|/**    * Test the whole reconstruction loop. Build a table with regions aaa to zzz    * and load every one of them multiple times with the same date and do a flush    * at some point. Kill one of the region servers and scan the table. We should    * see all the rows.    * @throws Exception    */
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|300000
argument_list|)
specifier|public
name|void
name|testReconstruction
parameter_list|()
throws|throws
name|Exception
block|{
name|Table
name|table
init|=
name|TEST_UTIL
operator|.
name|createMultiRegionTable
argument_list|(
name|TABLE_NAME
argument_list|,
name|FAMILY
argument_list|)
decl_stmt|;
comment|// Load up the table with simple rows and count them
name|int
name|initialCount
init|=
name|TEST_UTIL
operator|.
name|loadTable
argument_list|(
name|table
argument_list|,
name|FAMILY
argument_list|)
decl_stmt|;
name|int
name|count
init|=
name|TEST_UTIL
operator|.
name|countRows
argument_list|(
name|table
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|initialCount
argument_list|,
name|count
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
literal|4
condition|;
name|i
operator|++
control|)
block|{
name|TEST_UTIL
operator|.
name|loadTable
argument_list|(
name|table
argument_list|,
name|FAMILY
argument_list|)
expr_stmt|;
block|}
name|TEST_UTIL
operator|.
name|expireRegionServerSession
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|int
name|newCount
init|=
name|TEST_UTIL
operator|.
name|countRows
argument_list|(
name|table
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|count
argument_list|,
name|newCount
argument_list|)
expr_stmt|;
name|table
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

