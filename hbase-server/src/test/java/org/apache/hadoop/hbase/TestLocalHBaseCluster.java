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
name|*
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
name|master
operator|.
name|HMaster
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
name|MiscTests
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
name|TestLocalHBaseCluster
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
name|TestLocalHBaseCluster
operator|.
name|class
argument_list|)
decl_stmt|;
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
comment|/**    * Check that we can start a local HBase cluster specifying a custom master    * and regionserver class and then cast back to those classes; also that    * the cluster will launch and terminate cleanly. See HBASE-6011. Uses the    * HBaseTestingUtility facilities for creating a LocalHBaseCluster with    * custom master and regionserver classes.    */
annotation|@
name|Test
specifier|public
name|void
name|testLocalHBaseCluster
parameter_list|()
throws|throws
name|Exception
block|{
comment|// Set Master class and RegionServer class, and use default values for other options.
name|StartMiniClusterOption
name|option
init|=
name|StartMiniClusterOption
operator|.
name|builder
argument_list|()
operator|.
name|masterClass
argument_list|(
name|MyHMaster
operator|.
name|class
argument_list|)
operator|.
name|rsClass
argument_list|(
name|MyHRegionServer
operator|.
name|class
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|TEST_UTIL
operator|.
name|startMiniCluster
argument_list|(
name|option
argument_list|)
expr_stmt|;
comment|// Can we cast back to our master class?
try|try
block|{
name|int
name|val
init|=
operator|(
operator|(
name|MyHMaster
operator|)
name|TEST_UTIL
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|getMaster
argument_list|(
literal|0
argument_list|)
operator|)
operator|.
name|echo
argument_list|(
literal|42
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|42
argument_list|,
name|val
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ClassCastException
name|e
parameter_list|)
block|{
name|fail
argument_list|(
literal|"Could not cast master to our class"
argument_list|)
expr_stmt|;
block|}
comment|// Can we cast back to our regionserver class?
try|try
block|{
name|int
name|val
init|=
operator|(
operator|(
name|MyHRegionServer
operator|)
name|TEST_UTIL
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|getRegionServer
argument_list|(
literal|0
argument_list|)
operator|)
operator|.
name|echo
argument_list|(
literal|42
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|42
argument_list|,
name|val
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ClassCastException
name|e
parameter_list|)
block|{
name|fail
argument_list|(
literal|"Could not cast regionserver to our class"
argument_list|)
expr_stmt|;
block|}
name|TEST_UTIL
operator|.
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
block|}
comment|/**    * A private master class similar to that used by HMasterCommandLine when    * running in local mode.    */
specifier|public
specifier|static
class|class
name|MyHMaster
extends|extends
name|HMaster
block|{
specifier|public
name|MyHMaster
parameter_list|(
name|Configuration
name|conf
parameter_list|)
throws|throws
name|IOException
throws|,
name|KeeperException
throws|,
name|InterruptedException
block|{
name|super
argument_list|(
name|conf
argument_list|)
expr_stmt|;
block|}
specifier|public
name|int
name|echo
parameter_list|(
name|int
name|val
parameter_list|)
block|{
return|return
name|val
return|;
block|}
block|}
comment|/**    * A private regionserver class with a dummy method for testing casts    */
specifier|public
specifier|static
class|class
name|MyHRegionServer
extends|extends
name|MiniHBaseCluster
operator|.
name|MiniHBaseClusterRegionServer
block|{
specifier|public
name|MyHRegionServer
parameter_list|(
name|Configuration
name|conf
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|super
argument_list|(
name|conf
argument_list|)
expr_stmt|;
block|}
specifier|public
name|int
name|echo
parameter_list|(
name|int
name|val
parameter_list|)
block|{
return|return
name|val
return|;
block|}
block|}
block|}
end_class

end_unit

