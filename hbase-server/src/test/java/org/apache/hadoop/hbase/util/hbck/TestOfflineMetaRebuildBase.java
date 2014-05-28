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
name|util
operator|.
name|hbck
package|;
end_package

begin_import
import|import static
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
name|hbck
operator|.
name|HbckTestingUtil
operator|.
name|assertErrors
import|;
end_import

begin_import
import|import static
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
name|hbck
operator|.
name|HbckTestingUtil
operator|.
name|doFsck
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
name|assertTrue
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Arrays
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
name|client
operator|.
name|HConnectionManager
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
name|HBaseFsck
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
name|HBaseFsck
operator|.
name|ErrorReporter
operator|.
name|ERROR_CODE
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
name|zookeeper
operator|.
name|ZooKeeperWatcher
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
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|zookeeper
operator|.
name|ZKAssign
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

begin_comment
comment|/**  * This builds a table, removes info from meta, and then rebuilds meta.  */
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
name|TestOfflineMetaRebuildBase
extends|extends
name|OfflineMetaRebuildTestCore
block|{
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|120000
argument_list|)
specifier|public
name|void
name|testMetaRebuild
parameter_list|()
throws|throws
name|Exception
block|{
name|wipeOutMeta
argument_list|()
expr_stmt|;
comment|// is meta really messed up?
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|scanMeta
argument_list|()
argument_list|)
expr_stmt|;
name|assertErrors
argument_list|(
name|doFsck
argument_list|(
name|conf
argument_list|,
literal|false
argument_list|)
argument_list|,
operator|new
name|ERROR_CODE
index|[]
block|{
name|ERROR_CODE
operator|.
name|NOT_IN_META_OR_DEPLOYED
block|,
name|ERROR_CODE
operator|.
name|NOT_IN_META_OR_DEPLOYED
block|,
name|ERROR_CODE
operator|.
name|NOT_IN_META_OR_DEPLOYED
block|,
name|ERROR_CODE
operator|.
name|NOT_IN_META_OR_DEPLOYED
block|}
argument_list|)
expr_stmt|;
comment|// Note, would like to check # of tables, but this takes a while to time
comment|// out.
comment|// shutdown the minicluster
name|TEST_UTIL
operator|.
name|shutdownMiniHBaseCluster
argument_list|()
expr_stmt|;
name|TEST_UTIL
operator|.
name|shutdownMiniZKCluster
argument_list|()
expr_stmt|;
name|HConnectionManager
operator|.
name|deleteConnection
argument_list|(
name|conf
argument_list|)
expr_stmt|;
comment|// rebuild meta table from scratch
name|HBaseFsck
name|fsck
init|=
operator|new
name|HBaseFsck
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|fsck
operator|.
name|rebuildMeta
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
comment|// bring up the minicluster
name|TEST_UTIL
operator|.
name|startMiniZKCluster
argument_list|()
expr_stmt|;
name|TEST_UTIL
operator|.
name|restartHBaseCluster
argument_list|(
literal|3
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|getHBaseAdmin
argument_list|()
operator|.
name|enableTable
argument_list|(
name|table
argument_list|)
expr_stmt|;
name|ZooKeeperWatcher
name|zkw
init|=
name|HBaseTestingUtility
operator|.
name|getZooKeeperWatcher
argument_list|(
name|TEST_UTIL
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Waiting for no more RIT"
argument_list|)
expr_stmt|;
name|ZKAssign
operator|.
name|blockUntilNoRIT
argument_list|(
name|zkw
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"No more RIT in ZK, now doing final test verification"
argument_list|)
expr_stmt|;
comment|// everything is good again.
name|assertEquals
argument_list|(
literal|5
argument_list|,
name|scanMeta
argument_list|()
argument_list|)
expr_stmt|;
name|HTableDescriptor
index|[]
name|htbls
init|=
name|TEST_UTIL
operator|.
name|getHBaseAdmin
argument_list|()
operator|.
name|listTables
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Tables present after restart: "
operator|+
name|Arrays
operator|.
name|toString
argument_list|(
name|htbls
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|htbls
operator|.
name|length
argument_list|)
expr_stmt|;
name|assertErrors
argument_list|(
name|doFsck
argument_list|(
name|conf
argument_list|,
literal|false
argument_list|)
argument_list|,
operator|new
name|ERROR_CODE
index|[]
block|{}
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Table "
operator|+
name|table
operator|+
literal|" has "
operator|+
name|tableRowCount
argument_list|(
name|conf
argument_list|,
name|table
argument_list|)
operator|+
literal|" entries."
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|16
argument_list|,
name|tableRowCount
argument_list|(
name|conf
argument_list|,
name|table
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

