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
name|mapreduce
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
name|security
operator|.
name|access
operator|.
name|AccessControlLists
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
name|security
operator|.
name|access
operator|.
name|SecureTestUtil
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
comment|/**  * Reruns TestSecureLoadIncrementalHFilesSplitRecovery  * using LoadIncrementalHFiles in secure mode.  * This suite is unable to verify the security handoff/turnover  * as miniCluster is running as system user thus has root privileges  * and delegation tokens don't seem to work on miniDFS.  *  * Thus SecureBulkload can only be completely verified by running  * integration tests against a secure cluster. This suite is still  * invaluable as it verifies the other mechanisms that need to be  * supported as part of a LoadIncrementalFiles call.  */
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
name|TestSecureLoadIncrementalHFilesSplitRecovery
extends|extends
name|TestLoadIncrementalHFilesSplitRecovery
block|{
comment|//This "overrides" the parent static method
comment|//make sure they are in sync
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
name|useSecure
operator|=
literal|true
expr_stmt|;
name|util
operator|=
operator|new
name|HBaseTestingUtility
argument_list|()
expr_stmt|;
comment|// setup configuration
name|SecureTestUtil
operator|.
name|enableSecurity
argument_list|(
name|util
operator|.
name|getConfiguration
argument_list|()
argument_list|)
expr_stmt|;
name|util
operator|.
name|startMiniCluster
argument_list|()
expr_stmt|;
comment|// Wait for the ACL table to become available
name|util
operator|.
name|waitTableEnabled
argument_list|(
name|AccessControlLists
operator|.
name|ACL_TABLE_NAME
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|//Disabling this test as it does not work in secure mode
annotation|@
name|Test
annotation|@
name|Override
specifier|public
name|void
name|testBulkLoadPhaseFailure
parameter_list|()
block|{   }
block|}
end_class

end_unit

