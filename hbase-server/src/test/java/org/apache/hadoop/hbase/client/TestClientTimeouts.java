begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|MasterAdminProtocol
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
name|MasterMonitorProtocol
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
name|MasterNotRunningException
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
name|ipc
operator|.
name|RandomTimeoutRpcEngine
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
name|MediumTests
operator|.
name|class
argument_list|)
specifier|public
class|class
name|TestClientTimeouts
block|{
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|getClass
argument_list|()
argument_list|)
decl_stmt|;
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
specifier|protected
specifier|static
name|int
name|SLAVES
init|=
literal|1
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
name|conf
init|=
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
name|RandomTimeoutRpcEngine
operator|.
name|setProtocolEngine
argument_list|(
name|conf
argument_list|,
name|MasterAdminProtocol
operator|.
name|class
argument_list|)
expr_stmt|;
name|RandomTimeoutRpcEngine
operator|.
name|setProtocolEngine
argument_list|(
name|conf
argument_list|,
name|MasterMonitorProtocol
operator|.
name|class
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|startMiniCluster
argument_list|(
name|SLAVES
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
comment|/**    * Test that a client that fails an RPC to the master retries properly and    * doesn't throw any unexpected exceptions.    * @throws Exception    */
annotation|@
name|Test
specifier|public
name|void
name|testAdminTimeout
parameter_list|()
throws|throws
name|Exception
block|{
name|long
name|lastLimit
init|=
name|HConstants
operator|.
name|DEFAULT_HBASE_CLIENT_PREFETCH_LIMIT
decl_stmt|;
name|HConnection
name|lastConnection
init|=
literal|null
decl_stmt|;
name|boolean
name|lastFailed
init|=
literal|false
decl_stmt|;
name|int
name|initialInvocations
init|=
name|RandomTimeoutRpcEngine
operator|.
name|getNumberOfInvocations
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
literal|5
operator|||
operator|(
name|lastFailed
operator|&&
name|i
operator|<
literal|100
operator|)
condition|;
operator|++
name|i
control|)
block|{
name|lastFailed
operator|=
literal|false
expr_stmt|;
comment|// Ensure the HBaseAdmin uses a new connection by changing Configuration.
name|Configuration
name|conf
init|=
name|HBaseConfiguration
operator|.
name|create
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
name|conf
operator|.
name|setLong
argument_list|(
name|HConstants
operator|.
name|HBASE_CLIENT_PREFETCH_LIMIT
argument_list|,
operator|++
name|lastLimit
argument_list|)
expr_stmt|;
try|try
block|{
name|HBaseAdmin
name|admin
init|=
operator|new
name|HBaseAdmin
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|HConnection
name|connection
init|=
name|admin
operator|.
name|getConnection
argument_list|()
decl_stmt|;
name|assertFalse
argument_list|(
name|connection
operator|==
name|lastConnection
argument_list|)
expr_stmt|;
comment|// run some admin commands
name|HBaseAdmin
operator|.
name|checkHBaseAvailable
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|admin
operator|.
name|setBalancerRunning
argument_list|(
literal|false
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|MasterNotRunningException
name|ex
parameter_list|)
block|{
comment|// Since we are randomly throwing SocketTimeoutExceptions, it is possible to get
comment|// a MasterNotRunningException.  It's a bug if we get other exceptions.
name|lastFailed
operator|=
literal|true
expr_stmt|;
block|}
block|}
comment|// Ensure the RandomTimeoutRpcEngine is actually being used.
name|assertFalse
argument_list|(
name|lastFailed
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|RandomTimeoutRpcEngine
operator|.
name|getNumberOfInvocations
argument_list|()
operator|>
name|initialInvocations
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

