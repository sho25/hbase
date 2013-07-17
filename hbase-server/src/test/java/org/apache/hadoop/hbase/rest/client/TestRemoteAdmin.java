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
name|rest
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
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertFalse
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
name|ClusterStatus
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
name|rest
operator|.
name|HBaseRESTTestingUtility
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
name|rest
operator|.
name|model
operator|.
name|StorageClusterStatusModel
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
name|rest
operator|.
name|model
operator|.
name|TableModel
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
name|rest
operator|.
name|model
operator|.
name|VersionModel
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
name|Ignore
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
name|TestRemoteAdmin
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
specifier|private
specifier|static
specifier|final
name|HBaseRESTTestingUtility
name|REST_TEST_UTIL
init|=
operator|new
name|HBaseRESTTestingUtility
argument_list|()
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|TABLE_1
init|=
literal|"TestRemoteAdmin_Table_1"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|TABLE_2
init|=
name|TABLE_1
operator|+
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|COLUMN_1
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"a"
argument_list|)
decl_stmt|;
specifier|static
specifier|final
name|HTableDescriptor
name|DESC_1
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|TABLE_1
argument_list|)
decl_stmt|;
specifier|static
specifier|final
name|HTableDescriptor
name|DESC_2
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|TABLE_2
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|RemoteAdmin
name|remoteAdmin
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
name|DESC_1
operator|.
name|addFamily
argument_list|(
operator|new
name|HColumnDescriptor
argument_list|(
name|COLUMN_1
argument_list|)
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|startMiniCluster
argument_list|()
expr_stmt|;
name|REST_TEST_UTIL
operator|.
name|startServletContainer
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
expr_stmt|;
name|remoteAdmin
operator|=
operator|new
name|RemoteAdmin
argument_list|(
operator|new
name|Client
argument_list|(
operator|new
name|Cluster
argument_list|()
operator|.
name|add
argument_list|(
literal|"localhost"
argument_list|,
name|REST_TEST_UTIL
operator|.
name|getServletPort
argument_list|()
argument_list|)
argument_list|)
argument_list|,
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
name|tearDownAfterClass
parameter_list|()
throws|throws
name|Exception
block|{
name|REST_TEST_UTIL
operator|.
name|shutdownServletContainer
argument_list|()
expr_stmt|;
name|TEST_UTIL
operator|.
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Ignore
argument_list|(
literal|"See hbase-8965; REENABLE"
argument_list|)
annotation|@
name|Test
specifier|public
name|void
name|testCreateAnDeleteTable
parameter_list|()
throws|throws
name|Exception
block|{
specifier|final
name|String
name|tableName
init|=
literal|"testCreateAnDeleteTable"
decl_stmt|;
name|HTableDescriptor
name|htd
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
name|assertFalse
argument_list|(
name|remoteAdmin
operator|.
name|isTableAvailable
argument_list|(
name|tableName
argument_list|)
argument_list|)
expr_stmt|;
name|remoteAdmin
operator|.
name|createTable
argument_list|(
name|htd
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|remoteAdmin
operator|.
name|isTableAvailable
argument_list|(
name|tableName
argument_list|)
argument_list|)
expr_stmt|;
name|remoteAdmin
operator|.
name|deleteTable
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|remoteAdmin
operator|.
name|isTableAvailable
argument_list|(
name|tableName
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testGetRestVersion
parameter_list|()
throws|throws
name|Exception
block|{
name|VersionModel
name|RETURNED_REST_VERSION
init|=
name|remoteAdmin
operator|.
name|getRestVersion
argument_list|()
decl_stmt|;
name|System
operator|.
name|out
operator|.
name|print
argument_list|(
literal|"Returned version is: "
operator|+
name|RETURNED_REST_VERSION
argument_list|)
expr_stmt|;
comment|// Assert that it contains info about rest version, OS, JVM
name|assertTrue
argument_list|(
literal|"Returned REST version did not contain info about rest."
argument_list|,
name|RETURNED_REST_VERSION
operator|.
name|toString
argument_list|()
operator|.
name|contains
argument_list|(
literal|"rest"
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"Returned REST version did not contain info about the JVM."
argument_list|,
name|RETURNED_REST_VERSION
operator|.
name|toString
argument_list|()
operator|.
name|contains
argument_list|(
literal|"JVM"
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"Returned REST version did not contain info about OS."
argument_list|,
name|RETURNED_REST_VERSION
operator|.
name|toString
argument_list|()
operator|.
name|contains
argument_list|(
literal|"OS"
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testClusterVersion
parameter_list|()
throws|throws
name|Exception
block|{
comment|// testing the /version/cluster endpoint
specifier|final
name|String
name|HBASE_VERSION
init|=
name|TEST_UTIL
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|getClusterStatus
argument_list|()
operator|.
name|getHBaseVersion
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|"Cluster status from REST API did not match. "
argument_list|,
name|HBASE_VERSION
argument_list|,
name|remoteAdmin
operator|.
name|getClusterVersion
argument_list|()
operator|.
name|getVersion
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testClusterStatus
parameter_list|()
throws|throws
name|Exception
block|{
name|ClusterStatus
name|status
init|=
name|TEST_UTIL
operator|.
name|getHBaseClusterInterface
argument_list|()
operator|.
name|getClusterStatus
argument_list|()
decl_stmt|;
name|StorageClusterStatusModel
name|returnedStatus
init|=
name|remoteAdmin
operator|.
name|getClusterStatus
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|"Region count from cluster status and returned status did not match up"
argument_list|,
name|status
operator|.
name|getRegionsCount
argument_list|()
argument_list|,
name|returnedStatus
operator|.
name|getRegions
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"Dead server count from cluster status and returned status did not match up"
argument_list|,
name|status
operator|.
name|getDeadServers
argument_list|()
argument_list|,
name|returnedStatus
operator|.
name|getDeadNodes
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testListTables
parameter_list|()
throws|throws
name|Exception
block|{
name|remoteAdmin
operator|.
name|createTable
argument_list|(
name|DESC_2
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|TableModel
argument_list|>
name|tableList
init|=
name|remoteAdmin
operator|.
name|getTableList
argument_list|()
operator|.
name|getTables
argument_list|()
decl_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"List of tables is: "
argument_list|)
expr_stmt|;
name|boolean
name|found
init|=
literal|false
decl_stmt|;
for|for
control|(
name|TableModel
name|tm
range|:
name|tableList
control|)
block|{
if|if
condition|(
name|tm
operator|.
name|getName
argument_list|()
operator|.
name|equals
argument_list|(
name|TABLE_2
argument_list|)
condition|)
block|{
name|found
operator|=
literal|true
expr_stmt|;
break|break;
block|}
block|}
name|assertTrue
argument_list|(
literal|"Table "
operator|+
name|TABLE_2
operator|+
literal|" was not found by get request to '/'"
argument_list|,
name|found
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

