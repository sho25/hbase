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
name|zookeeper
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
name|HBaseConfiguration
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
name|ZooKeeperConnectionException
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
name|SmallTests
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
name|SmallTests
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|TestZooKeeperWatcher
block|{
annotation|@
name|Test
specifier|public
name|void
name|testIsClientReadable
parameter_list|()
throws|throws
name|ZooKeeperConnectionException
throws|,
name|IOException
block|{
name|ZooKeeperWatcher
name|watcher
init|=
operator|new
name|ZooKeeperWatcher
argument_list|(
name|HBaseConfiguration
operator|.
name|create
argument_list|()
argument_list|,
literal|"testIsClientReadable"
argument_list|,
literal|null
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|watcher
operator|.
name|isClientReadable
argument_list|(
name|watcher
operator|.
name|znodePaths
operator|.
name|baseZNode
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|watcher
operator|.
name|isClientReadable
argument_list|(
name|watcher
operator|.
name|znodePaths
operator|.
name|getZNodeForReplica
argument_list|(
literal|0
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|watcher
operator|.
name|isClientReadable
argument_list|(
name|watcher
operator|.
name|znodePaths
operator|.
name|masterAddressZNode
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|watcher
operator|.
name|isClientReadable
argument_list|(
name|watcher
operator|.
name|znodePaths
operator|.
name|clusterIdZNode
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|watcher
operator|.
name|isClientReadable
argument_list|(
name|watcher
operator|.
name|znodePaths
operator|.
name|tableZNode
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|watcher
operator|.
name|isClientReadable
argument_list|(
name|ZKUtil
operator|.
name|joinZNode
argument_list|(
name|watcher
operator|.
name|znodePaths
operator|.
name|tableZNode
argument_list|,
literal|"foo"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|watcher
operator|.
name|isClientReadable
argument_list|(
name|watcher
operator|.
name|znodePaths
operator|.
name|rsZNode
argument_list|)
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|watcher
operator|.
name|isClientReadable
argument_list|(
name|watcher
operator|.
name|znodePaths
operator|.
name|tableLockZNode
argument_list|)
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|watcher
operator|.
name|isClientReadable
argument_list|(
name|watcher
operator|.
name|znodePaths
operator|.
name|balancerZNode
argument_list|)
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|watcher
operator|.
name|isClientReadable
argument_list|(
name|watcher
operator|.
name|znodePaths
operator|.
name|regionNormalizerZNode
argument_list|)
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|watcher
operator|.
name|isClientReadable
argument_list|(
name|watcher
operator|.
name|znodePaths
operator|.
name|clusterStateZNode
argument_list|)
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|watcher
operator|.
name|isClientReadable
argument_list|(
name|watcher
operator|.
name|znodePaths
operator|.
name|drainingZNode
argument_list|)
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|watcher
operator|.
name|isClientReadable
argument_list|(
name|watcher
operator|.
name|znodePaths
operator|.
name|recoveringRegionsZNode
argument_list|)
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|watcher
operator|.
name|isClientReadable
argument_list|(
name|watcher
operator|.
name|znodePaths
operator|.
name|splitLogZNode
argument_list|)
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|watcher
operator|.
name|isClientReadable
argument_list|(
name|watcher
operator|.
name|znodePaths
operator|.
name|backupMasterAddressesZNode
argument_list|)
argument_list|)
expr_stmt|;
name|watcher
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

