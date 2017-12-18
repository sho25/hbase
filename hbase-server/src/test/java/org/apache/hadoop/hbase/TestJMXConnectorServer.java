begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|javax
operator|.
name|management
operator|.
name|remote
operator|.
name|JMXConnector
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|management
operator|.
name|remote
operator|.
name|JMXConnectorFactory
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|naming
operator|.
name|ServiceUnavailableException
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
name|client
operator|.
name|Admin
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
name|coprocessor
operator|.
name|CoprocessorHost
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
name|coprocessor
operator|.
name|MasterCoprocessorEnvironment
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
name|coprocessor
operator|.
name|ObserverContext
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
name|coprocessor
operator|.
name|RegionServerCoprocessorEnvironment
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
name|AccessDeniedException
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
name|AccessController
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
name|Before
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
comment|/**  * Test case for JMX Connector Server.  */
end_comment

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
name|TestJMXConnectorServer
block|{
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
name|TestJMXConnectorServer
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|HBaseTestingUtility
name|UTIL
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
specifier|private
specifier|static
name|Configuration
name|conf
init|=
literal|null
decl_stmt|;
specifier|private
specifier|static
name|Admin
name|admin
decl_stmt|;
comment|// RMI registry port
specifier|private
specifier|static
name|int
name|rmiRegistryPort
init|=
literal|61120
decl_stmt|;
comment|// Switch for customized Accesscontroller to throw ACD exception while executing test case
specifier|static
name|boolean
name|hasAccess
decl_stmt|;
annotation|@
name|Before
specifier|public
name|void
name|setUp
parameter_list|()
throws|throws
name|Exception
block|{
name|UTIL
operator|=
operator|new
name|HBaseTestingUtility
argument_list|()
expr_stmt|;
name|conf
operator|=
name|UTIL
operator|.
name|getConfiguration
argument_list|()
expr_stmt|;
block|}
annotation|@
name|After
specifier|public
name|void
name|tearDown
parameter_list|()
throws|throws
name|Exception
block|{
comment|// Set to true while stopping cluster
name|hasAccess
operator|=
literal|true
expr_stmt|;
name|admin
operator|.
name|close
argument_list|()
expr_stmt|;
name|UTIL
operator|.
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
block|}
comment|/**    * This tests to validate the HMaster's ConnectorServer after unauthorised stopMaster call.    */
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|180000
argument_list|)
specifier|public
name|void
name|testHMConnectorServerWhenStopMaster
parameter_list|()
throws|throws
name|Exception
block|{
name|conf
operator|.
name|set
argument_list|(
name|CoprocessorHost
operator|.
name|MASTER_COPROCESSOR_CONF_KEY
argument_list|,
name|JMXListener
operator|.
name|class
operator|.
name|getName
argument_list|()
operator|+
literal|","
operator|+
name|MyAccessController
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setInt
argument_list|(
literal|"master.rmi.registry.port"
argument_list|,
name|rmiRegistryPort
argument_list|)
expr_stmt|;
name|UTIL
operator|.
name|startMiniCluster
argument_list|()
expr_stmt|;
name|admin
operator|=
name|UTIL
operator|.
name|getConnection
argument_list|()
operator|.
name|getAdmin
argument_list|()
expr_stmt|;
comment|// try to stop master
name|boolean
name|accessDenied
init|=
literal|false
decl_stmt|;
try|try
block|{
name|hasAccess
operator|=
literal|false
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Stopping HMaster..."
argument_list|)
expr_stmt|;
name|admin
operator|.
name|stopMaster
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|AccessDeniedException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Exception occurred while stopping HMaster. "
argument_list|,
name|e
argument_list|)
expr_stmt|;
name|accessDenied
operator|=
literal|true
expr_stmt|;
block|}
name|Assert
operator|.
name|assertTrue
argument_list|(
name|accessDenied
argument_list|)
expr_stmt|;
comment|// Check whether HMaster JMX Connector server can be connected
name|JMXConnector
name|connector
init|=
literal|null
decl_stmt|;
try|try
block|{
name|connector
operator|=
name|JMXConnectorFactory
operator|.
name|connect
argument_list|(
name|JMXListener
operator|.
name|buildJMXServiceURL
argument_list|(
name|rmiRegistryPort
argument_list|,
name|rmiRegistryPort
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
if|if
condition|(
name|e
operator|.
name|getCause
argument_list|()
operator|instanceof
name|ServiceUnavailableException
condition|)
block|{
name|Assert
operator|.
name|fail
argument_list|(
literal|"Can't connect to HMaster ConnectorServer."
argument_list|)
expr_stmt|;
block|}
block|}
name|Assert
operator|.
name|assertNotNull
argument_list|(
literal|"JMXConnector should not be null."
argument_list|,
name|connector
argument_list|)
expr_stmt|;
name|connector
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
comment|/**    * This tests to validate the RegionServer's ConnectorServer after unauthorised stopRegionServer    * call.    */
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|180000
argument_list|)
specifier|public
name|void
name|testRSConnectorServerWhenStopRegionServer
parameter_list|()
throws|throws
name|Exception
block|{
name|conf
operator|.
name|set
argument_list|(
name|CoprocessorHost
operator|.
name|REGIONSERVER_COPROCESSOR_CONF_KEY
argument_list|,
name|JMXListener
operator|.
name|class
operator|.
name|getName
argument_list|()
operator|+
literal|","
operator|+
name|MyAccessController
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setInt
argument_list|(
literal|"regionserver.rmi.registry.port"
argument_list|,
name|rmiRegistryPort
argument_list|)
expr_stmt|;
name|UTIL
operator|.
name|startMiniCluster
argument_list|()
expr_stmt|;
name|admin
operator|=
name|UTIL
operator|.
name|getConnection
argument_list|()
operator|.
name|getAdmin
argument_list|()
expr_stmt|;
name|hasAccess
operator|=
literal|false
expr_stmt|;
name|ServerName
name|serverName
init|=
name|UTIL
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|getRegionServer
argument_list|(
literal|0
argument_list|)
operator|.
name|getServerName
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Stopping Region Server..."
argument_list|)
expr_stmt|;
name|admin
operator|.
name|stopRegionServer
argument_list|(
name|serverName
operator|.
name|getHostname
argument_list|()
operator|+
literal|":"
operator|+
name|serverName
operator|.
name|getPort
argument_list|()
argument_list|)
expr_stmt|;
comment|// Check whether Region Sever JMX Connector server can be connected
name|JMXConnector
name|connector
init|=
literal|null
decl_stmt|;
try|try
block|{
name|connector
operator|=
name|JMXConnectorFactory
operator|.
name|connect
argument_list|(
name|JMXListener
operator|.
name|buildJMXServiceURL
argument_list|(
name|rmiRegistryPort
argument_list|,
name|rmiRegistryPort
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
if|if
condition|(
name|e
operator|.
name|getCause
argument_list|()
operator|instanceof
name|ServiceUnavailableException
condition|)
block|{
name|Assert
operator|.
name|fail
argument_list|(
literal|"Can't connect to Region Server ConnectorServer."
argument_list|)
expr_stmt|;
block|}
block|}
name|Assert
operator|.
name|assertNotNull
argument_list|(
literal|"JMXConnector should not be null."
argument_list|,
name|connector
argument_list|)
expr_stmt|;
name|connector
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
comment|/**    * This tests to validate the HMaster's ConnectorServer after unauthorised shutdown call.    */
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|180000
argument_list|)
specifier|public
name|void
name|testHMConnectorServerWhenShutdownCluster
parameter_list|()
throws|throws
name|Exception
block|{
name|conf
operator|.
name|set
argument_list|(
name|CoprocessorHost
operator|.
name|MASTER_COPROCESSOR_CONF_KEY
argument_list|,
name|JMXListener
operator|.
name|class
operator|.
name|getName
argument_list|()
operator|+
literal|","
operator|+
name|MyAccessController
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setInt
argument_list|(
literal|"master.rmi.registry.port"
argument_list|,
name|rmiRegistryPort
argument_list|)
expr_stmt|;
name|UTIL
operator|.
name|startMiniCluster
argument_list|()
expr_stmt|;
name|admin
operator|=
name|UTIL
operator|.
name|getConnection
argument_list|()
operator|.
name|getAdmin
argument_list|()
expr_stmt|;
name|boolean
name|accessDenied
init|=
literal|false
decl_stmt|;
try|try
block|{
name|hasAccess
operator|=
literal|false
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Stopping HMaster..."
argument_list|)
expr_stmt|;
name|admin
operator|.
name|shutdown
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|AccessDeniedException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Exception occurred while stopping HMaster. "
argument_list|,
name|e
argument_list|)
expr_stmt|;
name|accessDenied
operator|=
literal|true
expr_stmt|;
block|}
name|Assert
operator|.
name|assertTrue
argument_list|(
name|accessDenied
argument_list|)
expr_stmt|;
comment|// Check whether HMaster JMX Connector server can be connected
name|JMXConnector
name|connector
init|=
literal|null
decl_stmt|;
try|try
block|{
name|connector
operator|=
name|JMXConnectorFactory
operator|.
name|connect
argument_list|(
name|JMXListener
operator|.
name|buildJMXServiceURL
argument_list|(
name|rmiRegistryPort
argument_list|,
name|rmiRegistryPort
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
if|if
condition|(
name|e
operator|.
name|getCause
argument_list|()
operator|instanceof
name|ServiceUnavailableException
condition|)
block|{
name|Assert
operator|.
name|fail
argument_list|(
literal|"Can't connect to HMaster ConnectorServer."
argument_list|)
expr_stmt|;
block|}
block|}
name|Assert
operator|.
name|assertNotNull
argument_list|(
literal|"JMXConnector should not be null."
argument_list|,
name|connector
argument_list|)
expr_stmt|;
name|connector
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
comment|/*    * Customized class for test case execution which will throw ACD exception while executing    * stopMaster/preStopRegionServer/preShutdown explicitly.    */
specifier|public
specifier|static
class|class
name|MyAccessController
extends|extends
name|AccessController
block|{
annotation|@
name|Override
specifier|public
name|void
name|preStopMaster
parameter_list|(
name|ObserverContext
argument_list|<
name|MasterCoprocessorEnvironment
argument_list|>
name|c
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
operator|!
name|hasAccess
condition|)
block|{
throw|throw
operator|new
name|AccessDeniedException
argument_list|(
literal|"Insufficient permissions to stop master"
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|preStopRegionServer
parameter_list|(
name|ObserverContext
argument_list|<
name|RegionServerCoprocessorEnvironment
argument_list|>
name|ctx
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
operator|!
name|hasAccess
condition|)
block|{
throw|throw
operator|new
name|AccessDeniedException
argument_list|(
literal|"Insufficient permissions to stop region server."
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|preShutdown
parameter_list|(
name|ObserverContext
argument_list|<
name|MasterCoprocessorEnvironment
argument_list|>
name|c
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
operator|!
name|hasAccess
condition|)
block|{
throw|throw
operator|new
name|AccessDeniedException
argument_list|(
literal|"Insufficient permissions to shut down cluster."
argument_list|)
throw|;
block|}
block|}
block|}
block|}
end_class

end_unit

