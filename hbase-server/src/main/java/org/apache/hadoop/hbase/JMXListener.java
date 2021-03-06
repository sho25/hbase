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
name|java
operator|.
name|lang
operator|.
name|management
operator|.
name|ManagementFactory
import|;
end_import

begin_import
import|import
name|java
operator|.
name|rmi
operator|.
name|registry
operator|.
name|LocateRegistry
import|;
end_import

begin_import
import|import
name|java
operator|.
name|rmi
operator|.
name|registry
operator|.
name|Registry
import|;
end_import

begin_import
import|import
name|java
operator|.
name|rmi
operator|.
name|server
operator|.
name|RMIClientSocketFactory
import|;
end_import

begin_import
import|import
name|java
operator|.
name|rmi
operator|.
name|server
operator|.
name|RMIServerSocketFactory
import|;
end_import

begin_import
import|import
name|java
operator|.
name|rmi
operator|.
name|server
operator|.
name|UnicastRemoteObject
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashMap
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|management
operator|.
name|MBeanServer
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
name|JMXConnectorServer
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
name|JMXConnectorServerFactory
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
name|JMXServiceURL
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
name|rmi
operator|.
name|RMIConnectorServer
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
name|coprocessor
operator|.
name|MasterCoprocessor
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
name|RegionCoprocessorEnvironment
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
name|RegionServerCoprocessor
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
name|yetus
operator|.
name|audience
operator|.
name|InterfaceAudience
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
comment|/**  * Pluggable JMX Agent for HBase(to fix the 2 random TCP ports issue  * of the out-of-the-box JMX Agent):  * 1)connector port can share with the registry port if SSL is OFF  * 2)support password authentication  * 3)support subset of SSL (with default configuration)  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|JMXListener
implements|implements
name|MasterCoprocessor
implements|,
name|RegionServerCoprocessor
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
name|JMXListener
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|RMI_REGISTRY_PORT_CONF_KEY
init|=
literal|".rmi.registry.port"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|RMI_CONNECTOR_PORT_CONF_KEY
init|=
literal|".rmi.connector.port"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|int
name|defMasterRMIRegistryPort
init|=
literal|10101
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|int
name|defRegionserverRMIRegistryPort
init|=
literal|10102
decl_stmt|;
comment|/**    * workaround for HBASE-11146    * master and regionserver are in 1 JVM in standalone mode    * only 1 JMX instance is allowed, otherwise there is port conflict even if    * we only load regionserver coprocessor on master    */
specifier|private
specifier|static
name|JMXConnectorServer
name|JMX_CS
init|=
literal|null
decl_stmt|;
specifier|private
name|Registry
name|rmiRegistry
init|=
literal|null
decl_stmt|;
specifier|public
specifier|static
name|JMXServiceURL
name|buildJMXServiceURL
parameter_list|(
name|int
name|rmiRegistryPort
parameter_list|,
name|int
name|rmiConnectorPort
parameter_list|)
throws|throws
name|IOException
block|{
comment|// Build jmxURL
name|StringBuilder
name|url
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|url
operator|.
name|append
argument_list|(
literal|"service:jmx:rmi://localhost:"
argument_list|)
expr_stmt|;
name|url
operator|.
name|append
argument_list|(
name|rmiConnectorPort
argument_list|)
expr_stmt|;
name|url
operator|.
name|append
argument_list|(
literal|"/jndi/rmi://localhost:"
argument_list|)
expr_stmt|;
name|url
operator|.
name|append
argument_list|(
name|rmiRegistryPort
argument_list|)
expr_stmt|;
name|url
operator|.
name|append
argument_list|(
literal|"/jmxrmi"
argument_list|)
expr_stmt|;
return|return
operator|new
name|JMXServiceURL
argument_list|(
name|url
operator|.
name|toString
argument_list|()
argument_list|)
return|;
block|}
specifier|public
name|void
name|startConnectorServer
parameter_list|(
name|int
name|rmiRegistryPort
parameter_list|,
name|int
name|rmiConnectorPort
parameter_list|)
throws|throws
name|IOException
block|{
name|boolean
name|rmiSSL
init|=
literal|false
decl_stmt|;
name|boolean
name|authenticate
init|=
literal|true
decl_stmt|;
name|String
name|passwordFile
init|=
literal|null
decl_stmt|;
name|String
name|accessFile
init|=
literal|null
decl_stmt|;
name|System
operator|.
name|setProperty
argument_list|(
literal|"java.rmi.server.randomIDs"
argument_list|,
literal|"true"
argument_list|)
expr_stmt|;
name|String
name|rmiSSLValue
init|=
name|System
operator|.
name|getProperty
argument_list|(
literal|"com.sun.management.jmxremote.ssl"
argument_list|,
literal|"false"
argument_list|)
decl_stmt|;
name|rmiSSL
operator|=
name|Boolean
operator|.
name|parseBoolean
argument_list|(
name|rmiSSLValue
argument_list|)
expr_stmt|;
name|String
name|authenticateValue
init|=
name|System
operator|.
name|getProperty
argument_list|(
literal|"com.sun.management.jmxremote.authenticate"
argument_list|,
literal|"false"
argument_list|)
decl_stmt|;
name|authenticate
operator|=
name|Boolean
operator|.
name|parseBoolean
argument_list|(
name|authenticateValue
argument_list|)
expr_stmt|;
name|passwordFile
operator|=
name|System
operator|.
name|getProperty
argument_list|(
literal|"com.sun.management.jmxremote.password.file"
argument_list|)
expr_stmt|;
name|accessFile
operator|=
name|System
operator|.
name|getProperty
argument_list|(
literal|"com.sun.management.jmxremote.access.file"
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"rmiSSL:"
operator|+
name|rmiSSLValue
operator|+
literal|",authenticate:"
operator|+
name|authenticateValue
operator|+
literal|",passwordFile:"
operator|+
name|passwordFile
operator|+
literal|",accessFile:"
operator|+
name|accessFile
argument_list|)
expr_stmt|;
comment|// Environment map
name|HashMap
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|jmxEnv
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|RMIClientSocketFactory
name|csf
init|=
literal|null
decl_stmt|;
name|RMIServerSocketFactory
name|ssf
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|rmiSSL
condition|)
block|{
if|if
condition|(
name|rmiRegistryPort
operator|==
name|rmiConnectorPort
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"SSL is enabled. "
operator|+
literal|"rmiConnectorPort cannot share with the rmiRegistryPort!"
argument_list|)
throw|;
block|}
name|csf
operator|=
operator|new
name|SslRMIClientSocketFactorySecure
argument_list|()
expr_stmt|;
name|ssf
operator|=
operator|new
name|SslRMIServerSocketFactorySecure
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|csf
operator|!=
literal|null
condition|)
block|{
name|jmxEnv
operator|.
name|put
argument_list|(
name|RMIConnectorServer
operator|.
name|RMI_CLIENT_SOCKET_FACTORY_ATTRIBUTE
argument_list|,
name|csf
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|ssf
operator|!=
literal|null
condition|)
block|{
name|jmxEnv
operator|.
name|put
argument_list|(
name|RMIConnectorServer
operator|.
name|RMI_SERVER_SOCKET_FACTORY_ATTRIBUTE
argument_list|,
name|ssf
argument_list|)
expr_stmt|;
block|}
comment|// Configure authentication
if|if
condition|(
name|authenticate
condition|)
block|{
name|jmxEnv
operator|.
name|put
argument_list|(
literal|"jmx.remote.x.password.file"
argument_list|,
name|passwordFile
argument_list|)
expr_stmt|;
name|jmxEnv
operator|.
name|put
argument_list|(
literal|"jmx.remote.x.access.file"
argument_list|,
name|accessFile
argument_list|)
expr_stmt|;
block|}
comment|// Create the RMI registry
name|rmiRegistry
operator|=
name|LocateRegistry
operator|.
name|createRegistry
argument_list|(
name|rmiRegistryPort
argument_list|)
expr_stmt|;
comment|// Retrieve the PlatformMBeanServer.
name|MBeanServer
name|mbs
init|=
name|ManagementFactory
operator|.
name|getPlatformMBeanServer
argument_list|()
decl_stmt|;
comment|// Build jmxURL
name|JMXServiceURL
name|serviceUrl
init|=
name|buildJMXServiceURL
argument_list|(
name|rmiRegistryPort
argument_list|,
name|rmiConnectorPort
argument_list|)
decl_stmt|;
try|try
block|{
comment|// Start the JMXListener with the connection string
synchronized|synchronized
init|(
name|JMXListener
operator|.
name|class
init|)
block|{
if|if
condition|(
name|JMX_CS
operator|!=
literal|null
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Started by another thread?"
argument_list|)
throw|;
block|}
name|JMX_CS
operator|=
name|JMXConnectorServerFactory
operator|.
name|newJMXConnectorServer
argument_list|(
name|serviceUrl
argument_list|,
name|jmxEnv
argument_list|,
name|mbs
argument_list|)
expr_stmt|;
name|JMX_CS
operator|.
name|start
argument_list|()
expr_stmt|;
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"ConnectorServer started!"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"fail to start connector server!"
argument_list|,
name|e
argument_list|)
expr_stmt|;
comment|// deregister the RMI registry
if|if
condition|(
name|rmiRegistry
operator|!=
literal|null
condition|)
block|{
name|UnicastRemoteObject
operator|.
name|unexportObject
argument_list|(
name|rmiRegistry
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|public
name|void
name|stopConnectorServer
parameter_list|()
throws|throws
name|IOException
block|{
synchronized|synchronized
init|(
name|JMXListener
operator|.
name|class
init|)
block|{
if|if
condition|(
name|JMX_CS
operator|!=
literal|null
condition|)
block|{
name|JMX_CS
operator|.
name|stop
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"ConnectorServer stopped!"
argument_list|)
expr_stmt|;
name|JMX_CS
operator|=
literal|null
expr_stmt|;
block|}
comment|// deregister the RMI registry
if|if
condition|(
name|rmiRegistry
operator|!=
literal|null
condition|)
block|{
name|UnicastRemoteObject
operator|.
name|unexportObject
argument_list|(
name|rmiRegistry
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|start
parameter_list|(
name|CoprocessorEnvironment
name|env
parameter_list|)
throws|throws
name|IOException
block|{
name|int
name|rmiRegistryPort
init|=
operator|-
literal|1
decl_stmt|;
name|int
name|rmiConnectorPort
init|=
operator|-
literal|1
decl_stmt|;
name|Configuration
name|conf
init|=
name|env
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
if|if
condition|(
name|env
operator|instanceof
name|MasterCoprocessorEnvironment
condition|)
block|{
comment|// running on Master
name|rmiRegistryPort
operator|=
name|conf
operator|.
name|getInt
argument_list|(
literal|"master"
operator|+
name|RMI_REGISTRY_PORT_CONF_KEY
argument_list|,
name|defMasterRMIRegistryPort
argument_list|)
expr_stmt|;
name|rmiConnectorPort
operator|=
name|conf
operator|.
name|getInt
argument_list|(
literal|"master"
operator|+
name|RMI_CONNECTOR_PORT_CONF_KEY
argument_list|,
name|rmiRegistryPort
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Master rmiRegistryPort:"
operator|+
name|rmiRegistryPort
operator|+
literal|",Master rmiConnectorPort:"
operator|+
name|rmiConnectorPort
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|env
operator|instanceof
name|RegionServerCoprocessorEnvironment
condition|)
block|{
comment|// running on RegionServer
name|rmiRegistryPort
operator|=
name|conf
operator|.
name|getInt
argument_list|(
literal|"regionserver"
operator|+
name|RMI_REGISTRY_PORT_CONF_KEY
argument_list|,
name|defRegionserverRMIRegistryPort
argument_list|)
expr_stmt|;
name|rmiConnectorPort
operator|=
name|conf
operator|.
name|getInt
argument_list|(
literal|"regionserver"
operator|+
name|RMI_CONNECTOR_PORT_CONF_KEY
argument_list|,
name|rmiRegistryPort
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"RegionServer rmiRegistryPort:"
operator|+
name|rmiRegistryPort
operator|+
literal|",RegionServer rmiConnectorPort:"
operator|+
name|rmiConnectorPort
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|env
operator|instanceof
name|RegionCoprocessorEnvironment
condition|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"JMXListener should not be loaded in Region Environment!"
argument_list|)
expr_stmt|;
return|return;
block|}
synchronized|synchronized
init|(
name|JMXListener
operator|.
name|class
init|)
block|{
if|if
condition|(
name|JMX_CS
operator|!=
literal|null
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"JMXListener has been started at Registry port "
operator|+
name|rmiRegistryPort
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|startConnectorServer
argument_list|(
name|rmiRegistryPort
argument_list|,
name|rmiConnectorPort
argument_list|)
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|stop
parameter_list|(
name|CoprocessorEnvironment
name|env
parameter_list|)
throws|throws
name|IOException
block|{
name|stopConnectorServer
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

