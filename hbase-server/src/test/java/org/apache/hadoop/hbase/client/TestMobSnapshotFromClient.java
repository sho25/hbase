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
name|mob
operator|.
name|MobConstants
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
name|snapshot
operator|.
name|MobSnapshotTestingUtils
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
name|LargeTests
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
comment|/**  * Test create/using/deleting snapshots from the client  *<p>  * This is an end-to-end test for the snapshot utility  */
end_comment

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|LargeTests
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
name|TestMobSnapshotFromClient
extends|extends
name|TestSnapshotFromClient
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
name|TestMobSnapshotFromClient
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
name|TestMobSnapshotFromClient
operator|.
name|class
argument_list|)
decl_stmt|;
comment|/**    * Setup the config for the cluster    * @throws Exception on failure    */
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|setupCluster
parameter_list|()
throws|throws
name|Exception
block|{
name|setupConf
argument_list|(
name|UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
expr_stmt|;
name|UTIL
operator|.
name|startMiniCluster
argument_list|(
name|NUM_RS
argument_list|)
expr_stmt|;
block|}
specifier|protected
specifier|static
name|void
name|setupConf
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
name|TestSnapshotFromClient
operator|.
name|setupConf
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setInt
argument_list|(
name|MobConstants
operator|.
name|MOB_FILE_CACHE_SIZE_KEY
argument_list|,
literal|0
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|createTable
parameter_list|()
throws|throws
name|Exception
block|{
name|MobSnapshotTestingUtils
operator|.
name|createMobTable
argument_list|(
name|UTIL
argument_list|,
name|TABLE_NAME
argument_list|,
name|getNumReplicas
argument_list|()
argument_list|,
name|TEST_FAM
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

