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
name|master
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
name|TableState
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
name|MasterTests
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
name|ZKUtil
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
name|ZKWatcher
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
name|ZNodePaths
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|zookeeper
operator|.
name|KeeperException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|After
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
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|shaded
operator|.
name|protobuf
operator|.
name|ProtobufUtil
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
name|shaded
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|ZooKeeperProtos
import|;
end_import

begin_comment
comment|/**  * Tests the default table lock manager  */
end_comment

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|MasterTests
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
name|TestTableStateManager
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
name|TestTableStateManager
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|HBaseTestingUtility
name|TEST_UTIL
init|=
operator|new
name|HBaseTestingUtility
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
name|After
specifier|public
name|void
name|tearDown
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
name|testUpgradeFromZk
parameter_list|()
throws|throws
name|Exception
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
name|TEST_UTIL
operator|.
name|startMiniCluster
argument_list|(
literal|2
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|shutdownMiniHBaseCluster
argument_list|()
expr_stmt|;
name|ZKWatcher
name|watcher
init|=
name|TEST_UTIL
operator|.
name|getZooKeeperWatcher
argument_list|()
decl_stmt|;
name|setTableStateInZK
argument_list|(
name|watcher
argument_list|,
name|tableName
argument_list|,
name|ZooKeeperProtos
operator|.
name|DeprecatedTableState
operator|.
name|State
operator|.
name|DISABLED
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|restartHBaseCluster
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|HMaster
name|master
init|=
name|TEST_UTIL
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|getMaster
argument_list|()
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|TableState
operator|.
name|State
operator|.
name|DISABLED
argument_list|,
name|master
operator|.
name|getTableStateManager
argument_list|()
operator|.
name|getTableState
argument_list|(
name|tableName
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|setTableStateInZK
parameter_list|(
name|ZKWatcher
name|watcher
parameter_list|,
specifier|final
name|TableName
name|tableName
parameter_list|,
specifier|final
name|ZooKeeperProtos
operator|.
name|DeprecatedTableState
operator|.
name|State
name|state
parameter_list|)
throws|throws
name|KeeperException
throws|,
name|IOException
block|{
name|String
name|znode
init|=
name|ZNodePaths
operator|.
name|joinZNode
argument_list|(
name|watcher
operator|.
name|znodePaths
operator|.
name|tableZNode
argument_list|,
name|tableName
operator|.
name|getNameAsString
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|ZKUtil
operator|.
name|checkExists
argument_list|(
name|watcher
argument_list|,
name|znode
argument_list|)
operator|==
operator|-
literal|1
condition|)
block|{
name|ZKUtil
operator|.
name|createAndFailSilent
argument_list|(
name|watcher
argument_list|,
name|znode
argument_list|)
expr_stmt|;
block|}
name|ZooKeeperProtos
operator|.
name|DeprecatedTableState
operator|.
name|Builder
name|builder
init|=
name|ZooKeeperProtos
operator|.
name|DeprecatedTableState
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
name|builder
operator|.
name|setState
argument_list|(
name|state
argument_list|)
expr_stmt|;
name|byte
index|[]
name|data
init|=
name|ProtobufUtil
operator|.
name|prependPBMagic
argument_list|(
name|builder
operator|.
name|build
argument_list|()
operator|.
name|toByteArray
argument_list|()
argument_list|)
decl_stmt|;
name|ZKUtil
operator|.
name|setData
argument_list|(
name|watcher
argument_list|,
name|znode
argument_list|,
name|data
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

