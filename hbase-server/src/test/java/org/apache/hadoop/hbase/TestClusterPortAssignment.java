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
name|net
operator|.
name|BindException
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
name|testclassification
operator|.
name|MediumTests
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
name|MediumTests
operator|.
name|class
argument_list|)
specifier|public
class|class
name|TestClusterPortAssignment
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
name|TestClusterPortAssignment
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
specifier|private
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|TestClusterPortAssignment
operator|.
name|class
argument_list|)
decl_stmt|;
comment|/**    * Check that we can start an HBase cluster specifying a custom set of    * RPC and infoserver ports.    */
annotation|@
name|Test
specifier|public
name|void
name|testClusterPortAssignment
parameter_list|()
throws|throws
name|Exception
block|{
name|boolean
name|retry
init|=
literal|false
decl_stmt|;
do|do
block|{
name|int
name|masterPort
init|=
name|HBaseTestingUtility
operator|.
name|randomFreePort
argument_list|()
decl_stmt|;
name|int
name|masterInfoPort
init|=
name|HBaseTestingUtility
operator|.
name|randomFreePort
argument_list|()
decl_stmt|;
name|int
name|rsPort
init|=
name|HBaseTestingUtility
operator|.
name|randomFreePort
argument_list|()
decl_stmt|;
name|int
name|rsInfoPort
init|=
name|HBaseTestingUtility
operator|.
name|randomFreePort
argument_list|()
decl_stmt|;
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setBoolean
argument_list|(
name|LocalHBaseCluster
operator|.
name|ASSIGN_RANDOM_PORTS
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setInt
argument_list|(
name|HConstants
operator|.
name|MASTER_PORT
argument_list|,
name|masterPort
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setInt
argument_list|(
name|HConstants
operator|.
name|MASTER_INFO_PORT
argument_list|,
name|masterInfoPort
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setInt
argument_list|(
name|HConstants
operator|.
name|REGIONSERVER_PORT
argument_list|,
name|rsPort
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setInt
argument_list|(
name|HConstants
operator|.
name|REGIONSERVER_INFO_PORT
argument_list|,
name|rsInfoPort
argument_list|)
expr_stmt|;
try|try
block|{
name|MiniHBaseCluster
name|cluster
init|=
name|TEST_UTIL
operator|.
name|startMiniCluster
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
literal|"Cluster failed to come up"
argument_list|,
name|cluster
operator|.
name|waitForActiveAndReadyMaster
argument_list|(
literal|30000
argument_list|)
argument_list|)
expr_stmt|;
name|retry
operator|=
literal|false
expr_stmt|;
name|assertEquals
argument_list|(
literal|"Master RPC port is incorrect"
argument_list|,
name|masterPort
argument_list|,
name|cluster
operator|.
name|getMaster
argument_list|()
operator|.
name|getRpcServer
argument_list|()
operator|.
name|getListenerAddress
argument_list|()
operator|.
name|getPort
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"Master info port is incorrect"
argument_list|,
name|masterInfoPort
argument_list|,
name|cluster
operator|.
name|getMaster
argument_list|()
operator|.
name|getInfoServer
argument_list|()
operator|.
name|getPort
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"RS RPC port is incorrect"
argument_list|,
name|rsPort
argument_list|,
name|cluster
operator|.
name|getRegionServer
argument_list|(
literal|0
argument_list|)
operator|.
name|getRpcServer
argument_list|()
operator|.
name|getListenerAddress
argument_list|()
operator|.
name|getPort
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"RS info port is incorrect"
argument_list|,
name|rsInfoPort
argument_list|,
name|cluster
operator|.
name|getRegionServer
argument_list|(
literal|0
argument_list|)
operator|.
name|getInfoServer
argument_list|()
operator|.
name|getPort
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|BindException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Failed to bind, need to retry"
argument_list|,
name|e
argument_list|)
expr_stmt|;
name|retry
operator|=
literal|true
expr_stmt|;
block|}
finally|finally
block|{
name|TEST_UTIL
operator|.
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
block|}
block|}
do|while
condition|(
name|retry
condition|)
do|;
block|}
block|}
end_class

end_unit
