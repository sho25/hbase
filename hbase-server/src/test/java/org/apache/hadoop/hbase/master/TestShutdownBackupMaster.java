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
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertNotNull
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
name|concurrent
operator|.
name|CountDownLatch
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
name|HConstants
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
name|MiniHBaseCluster
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
name|StartMiniClusterOption
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
name|JVMClusterUtil
operator|.
name|MasterThread
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

begin_comment
comment|/**  * Test to confirm that we will not hang when stop a backup master which is trying to become the  * active master. See HBASE-19838  */
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
name|MediumTests
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|TestShutdownBackupMaster
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
name|TestShutdownBackupMaster
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|HBaseTestingUtility
name|UTIL
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
specifier|private
specifier|static
specifier|volatile
name|CountDownLatch
name|ARRIVE
decl_stmt|;
specifier|private
specifier|static
specifier|volatile
name|CountDownLatch
name|CONTINUE
decl_stmt|;
specifier|public
specifier|static
specifier|final
class|class
name|MockHMaster
extends|extends
name|HMaster
block|{
specifier|public
name|MockHMaster
parameter_list|(
name|Configuration
name|conf
parameter_list|)
throws|throws
name|IOException
block|{
name|super
argument_list|(
name|conf
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|initClusterSchemaService
parameter_list|()
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
if|if
condition|(
name|ARRIVE
operator|!=
literal|null
condition|)
block|{
name|ARRIVE
operator|.
name|countDown
argument_list|()
expr_stmt|;
name|CONTINUE
operator|.
name|await
argument_list|()
expr_stmt|;
block|}
name|super
operator|.
name|initClusterSchemaService
argument_list|()
expr_stmt|;
block|}
block|}
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
name|UTIL
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setClass
argument_list|(
name|HConstants
operator|.
name|MASTER_IMPL
argument_list|,
name|MockHMaster
operator|.
name|class
argument_list|,
name|HMaster
operator|.
name|class
argument_list|)
expr_stmt|;
name|StartMiniClusterOption
name|option
init|=
name|StartMiniClusterOption
operator|.
name|builder
argument_list|()
operator|.
name|numMasters
argument_list|(
literal|2
argument_list|)
operator|.
name|numRegionServers
argument_list|(
literal|2
argument_list|)
operator|.
name|numDataNodes
argument_list|(
literal|2
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|UTIL
operator|.
name|startMiniCluster
argument_list|(
name|option
argument_list|)
expr_stmt|;
name|UTIL
operator|.
name|waitUntilAllSystemRegionsAssigned
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
comment|// make sure that we can stop the cluster cleanly
name|UTIL
operator|.
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testShutdownWhileBecomingActive
parameter_list|()
throws|throws
name|InterruptedException
block|{
name|MiniHBaseCluster
name|cluster
init|=
name|UTIL
operator|.
name|getHBaseCluster
argument_list|()
decl_stmt|;
name|HMaster
name|activeMaster
init|=
literal|null
decl_stmt|;
name|HMaster
name|backupMaster
init|=
literal|null
decl_stmt|;
for|for
control|(
name|MasterThread
name|t
range|:
name|cluster
operator|.
name|getMasterThreads
argument_list|()
control|)
block|{
if|if
condition|(
name|t
operator|.
name|getMaster
argument_list|()
operator|.
name|isActiveMaster
argument_list|()
condition|)
block|{
name|activeMaster
operator|=
name|t
operator|.
name|getMaster
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|backupMaster
operator|=
name|t
operator|.
name|getMaster
argument_list|()
expr_stmt|;
block|}
block|}
name|assertNotNull
argument_list|(
name|activeMaster
argument_list|)
expr_stmt|;
name|assertNotNull
argument_list|(
name|backupMaster
argument_list|)
expr_stmt|;
name|ARRIVE
operator|=
operator|new
name|CountDownLatch
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|CONTINUE
operator|=
operator|new
name|CountDownLatch
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|activeMaster
operator|.
name|abort
argument_list|(
literal|"Aborting active master for test"
argument_list|)
expr_stmt|;
comment|// wait until we arrive the initClusterSchemaService
name|ARRIVE
operator|.
name|await
argument_list|()
expr_stmt|;
comment|// killall RSes
name|cluster
operator|.
name|getRegionServerThreads
argument_list|()
operator|.
name|stream
argument_list|()
operator|.
name|map
argument_list|(
name|t
lambda|->
name|t
operator|.
name|getRegionServer
argument_list|()
argument_list|)
operator|.
name|forEachOrdered
argument_list|(
name|rs
lambda|->
name|rs
operator|.
name|abort
argument_list|(
literal|"Aborting RS for test"
argument_list|)
argument_list|)
expr_stmt|;
name|CONTINUE
operator|.
name|countDown
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

