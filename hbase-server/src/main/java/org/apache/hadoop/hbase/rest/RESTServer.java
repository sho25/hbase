begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *   http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|rest
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|ArrayList
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|List
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
operator|.
name|Entry
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
name|cli
operator|.
name|CommandLine
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
name|cli
operator|.
name|HelpFormatter
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
name|cli
operator|.
name|Options
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
name|cli
operator|.
name|ParseException
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
name|cli
operator|.
name|PosixParser
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
name|lang
operator|.
name|ArrayUtils
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
name|classification
operator|.
name|InterfaceAudience
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
name|http
operator|.
name|InfoServer
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
name|rest
operator|.
name|filter
operator|.
name|AuthFilter
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
name|UserProvider
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
name|HttpServerUtil
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
name|Strings
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
name|VersionInfo
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
name|net
operator|.
name|DNS
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
name|security
operator|.
name|UserGroupInformation
import|;
end_import

begin_import
import|import
name|org
operator|.
name|mortbay
operator|.
name|jetty
operator|.
name|Connector
import|;
end_import

begin_import
import|import
name|org
operator|.
name|mortbay
operator|.
name|jetty
operator|.
name|Server
import|;
end_import

begin_import
import|import
name|org
operator|.
name|mortbay
operator|.
name|jetty
operator|.
name|nio
operator|.
name|SelectChannelConnector
import|;
end_import

begin_import
import|import
name|org
operator|.
name|mortbay
operator|.
name|jetty
operator|.
name|security
operator|.
name|SslSelectChannelConnector
import|;
end_import

begin_import
import|import
name|org
operator|.
name|mortbay
operator|.
name|jetty
operator|.
name|servlet
operator|.
name|Context
import|;
end_import

begin_import
import|import
name|org
operator|.
name|mortbay
operator|.
name|jetty
operator|.
name|servlet
operator|.
name|FilterHolder
import|;
end_import

begin_import
import|import
name|org
operator|.
name|mortbay
operator|.
name|jetty
operator|.
name|servlet
operator|.
name|ServletHolder
import|;
end_import

begin_import
import|import
name|org
operator|.
name|mortbay
operator|.
name|thread
operator|.
name|QueuedThreadPool
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|base
operator|.
name|Preconditions
import|;
end_import

begin_import
import|import
name|com
operator|.
name|sun
operator|.
name|jersey
operator|.
name|api
operator|.
name|json
operator|.
name|JSONConfiguration
import|;
end_import

begin_import
import|import
name|com
operator|.
name|sun
operator|.
name|jersey
operator|.
name|spi
operator|.
name|container
operator|.
name|servlet
operator|.
name|ServletContainer
import|;
end_import

begin_comment
comment|/**  * Main class for launching REST gateway as a servlet hosted by Jetty.  *<p>  * The following options are supported:  *<ul>  *<li>-p --port : service port</li>  *<li>-ro --readonly : server mode</li>  *</ul>  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|RESTServer
implements|implements
name|Constants
block|{
specifier|private
specifier|static
name|void
name|printUsageAndExit
parameter_list|(
name|Options
name|options
parameter_list|,
name|int
name|exitCode
parameter_list|)
block|{
name|HelpFormatter
name|formatter
init|=
operator|new
name|HelpFormatter
argument_list|()
decl_stmt|;
name|formatter
operator|.
name|printHelp
argument_list|(
literal|"bin/hbase rest start"
argument_list|,
literal|""
argument_list|,
name|options
argument_list|,
literal|"\nTo run the REST server as a daemon, execute "
operator|+
literal|"bin/hbase-daemon.sh start|stop rest [--infoport<port>] [-p<port>] [-ro]\n"
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|System
operator|.
name|exit
argument_list|(
name|exitCode
argument_list|)
expr_stmt|;
block|}
comment|/**    * The main method for the HBase rest server.    * @param args command-line arguments    * @throws Exception exception    */
specifier|public
specifier|static
name|void
name|main
parameter_list|(
name|String
index|[]
name|args
parameter_list|)
throws|throws
name|Exception
block|{
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
literal|"RESTServer"
argument_list|)
decl_stmt|;
name|VersionInfo
operator|.
name|logVersion
argument_list|()
expr_stmt|;
name|FilterHolder
name|authFilter
init|=
literal|null
decl_stmt|;
name|Configuration
name|conf
init|=
name|HBaseConfiguration
operator|.
name|create
argument_list|()
decl_stmt|;
name|Class
argument_list|<
name|?
extends|extends
name|ServletContainer
argument_list|>
name|containerClass
init|=
name|ServletContainer
operator|.
name|class
decl_stmt|;
name|UserProvider
name|userProvider
init|=
name|UserProvider
operator|.
name|instantiate
argument_list|(
name|conf
argument_list|)
decl_stmt|;
comment|// login the server principal (if using secure Hadoop)
if|if
condition|(
name|userProvider
operator|.
name|isHadoopSecurityEnabled
argument_list|()
operator|&&
name|userProvider
operator|.
name|isHBaseSecurityEnabled
argument_list|()
condition|)
block|{
name|String
name|machineName
init|=
name|Strings
operator|.
name|domainNamePointerToHostName
argument_list|(
name|DNS
operator|.
name|getDefaultHost
argument_list|(
name|conf
operator|.
name|get
argument_list|(
name|REST_DNS_INTERFACE
argument_list|,
literal|"default"
argument_list|)
argument_list|,
name|conf
operator|.
name|get
argument_list|(
name|REST_DNS_NAMESERVER
argument_list|,
literal|"default"
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
name|String
name|keytabFilename
init|=
name|conf
operator|.
name|get
argument_list|(
name|REST_KEYTAB_FILE
argument_list|)
decl_stmt|;
name|Preconditions
operator|.
name|checkArgument
argument_list|(
name|keytabFilename
operator|!=
literal|null
operator|&&
operator|!
name|keytabFilename
operator|.
name|isEmpty
argument_list|()
argument_list|,
name|REST_KEYTAB_FILE
operator|+
literal|" should be set if security is enabled"
argument_list|)
expr_stmt|;
name|String
name|principalConfig
init|=
name|conf
operator|.
name|get
argument_list|(
name|REST_KERBEROS_PRINCIPAL
argument_list|)
decl_stmt|;
name|Preconditions
operator|.
name|checkArgument
argument_list|(
name|principalConfig
operator|!=
literal|null
operator|&&
operator|!
name|principalConfig
operator|.
name|isEmpty
argument_list|()
argument_list|,
name|REST_KERBEROS_PRINCIPAL
operator|+
literal|" should be set if security is enabled"
argument_list|)
expr_stmt|;
name|userProvider
operator|.
name|login
argument_list|(
name|REST_KEYTAB_FILE
argument_list|,
name|REST_KERBEROS_PRINCIPAL
argument_list|,
name|machineName
argument_list|)
expr_stmt|;
if|if
condition|(
name|conf
operator|.
name|get
argument_list|(
name|REST_AUTHENTICATION_TYPE
argument_list|)
operator|!=
literal|null
condition|)
block|{
name|containerClass
operator|=
name|RESTServletContainer
operator|.
name|class
expr_stmt|;
name|authFilter
operator|=
operator|new
name|FilterHolder
argument_list|()
expr_stmt|;
name|authFilter
operator|.
name|setClassName
argument_list|(
name|AuthFilter
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|authFilter
operator|.
name|setName
argument_list|(
literal|"AuthenticationFilter"
argument_list|)
expr_stmt|;
block|}
block|}
name|UserGroupInformation
name|realUser
init|=
name|userProvider
operator|.
name|getCurrent
argument_list|()
operator|.
name|getUGI
argument_list|()
decl_stmt|;
name|RESTServlet
name|servlet
init|=
name|RESTServlet
operator|.
name|getInstance
argument_list|(
name|conf
argument_list|,
name|realUser
argument_list|)
decl_stmt|;
name|Options
name|options
init|=
operator|new
name|Options
argument_list|()
decl_stmt|;
name|options
operator|.
name|addOption
argument_list|(
literal|"p"
argument_list|,
literal|"port"
argument_list|,
literal|true
argument_list|,
literal|"Port to bind to [default: 8080]"
argument_list|)
expr_stmt|;
name|options
operator|.
name|addOption
argument_list|(
literal|"ro"
argument_list|,
literal|"readonly"
argument_list|,
literal|false
argument_list|,
literal|"Respond only to GET HTTP "
operator|+
literal|"method requests [default: false]"
argument_list|)
expr_stmt|;
name|options
operator|.
name|addOption
argument_list|(
literal|null
argument_list|,
literal|"infoport"
argument_list|,
literal|true
argument_list|,
literal|"Port for web UI"
argument_list|)
expr_stmt|;
name|CommandLine
name|commandLine
init|=
literal|null
decl_stmt|;
try|try
block|{
name|commandLine
operator|=
operator|new
name|PosixParser
argument_list|()
operator|.
name|parse
argument_list|(
name|options
argument_list|,
name|args
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ParseException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Could not parse: "
argument_list|,
name|e
argument_list|)
expr_stmt|;
name|printUsageAndExit
argument_list|(
name|options
argument_list|,
operator|-
literal|1
argument_list|)
expr_stmt|;
block|}
comment|// check for user-defined port setting, if so override the conf
if|if
condition|(
name|commandLine
operator|!=
literal|null
operator|&&
name|commandLine
operator|.
name|hasOption
argument_list|(
literal|"port"
argument_list|)
condition|)
block|{
name|String
name|val
init|=
name|commandLine
operator|.
name|getOptionValue
argument_list|(
literal|"port"
argument_list|)
decl_stmt|;
name|servlet
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setInt
argument_list|(
literal|"hbase.rest.port"
argument_list|,
name|Integer
operator|.
name|valueOf
argument_list|(
name|val
argument_list|)
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"port set to "
operator|+
name|val
argument_list|)
expr_stmt|;
block|}
comment|// check if server should only process GET requests, if so override the conf
if|if
condition|(
name|commandLine
operator|!=
literal|null
operator|&&
name|commandLine
operator|.
name|hasOption
argument_list|(
literal|"readonly"
argument_list|)
condition|)
block|{
name|servlet
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setBoolean
argument_list|(
literal|"hbase.rest.readonly"
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"readonly set to true"
argument_list|)
expr_stmt|;
block|}
comment|// check for user-defined info server port setting, if so override the conf
if|if
condition|(
name|commandLine
operator|!=
literal|null
operator|&&
name|commandLine
operator|.
name|hasOption
argument_list|(
literal|"infoport"
argument_list|)
condition|)
block|{
name|String
name|val
init|=
name|commandLine
operator|.
name|getOptionValue
argument_list|(
literal|"infoport"
argument_list|)
decl_stmt|;
name|servlet
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setInt
argument_list|(
literal|"hbase.rest.info.port"
argument_list|,
name|Integer
operator|.
name|valueOf
argument_list|(
name|val
argument_list|)
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Web UI port set to "
operator|+
name|val
argument_list|)
expr_stmt|;
block|}
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
name|List
argument_list|<
name|String
argument_list|>
name|remainingArgs
init|=
name|commandLine
operator|!=
literal|null
condition|?
name|commandLine
operator|.
name|getArgList
argument_list|()
else|:
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
if|if
condition|(
name|remainingArgs
operator|.
name|size
argument_list|()
operator|!=
literal|1
condition|)
block|{
name|printUsageAndExit
argument_list|(
name|options
argument_list|,
literal|1
argument_list|)
expr_stmt|;
block|}
name|String
name|command
init|=
name|remainingArgs
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
if|if
condition|(
literal|"start"
operator|.
name|equals
argument_list|(
name|command
argument_list|)
condition|)
block|{
comment|// continue and start container
block|}
elseif|else
if|if
condition|(
literal|"stop"
operator|.
name|equals
argument_list|(
name|command
argument_list|)
condition|)
block|{
name|System
operator|.
name|exit
argument_list|(
literal|1
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|printUsageAndExit
argument_list|(
name|options
argument_list|,
literal|1
argument_list|)
expr_stmt|;
block|}
comment|// set up the Jersey servlet container for Jetty
name|ServletHolder
name|sh
init|=
operator|new
name|ServletHolder
argument_list|(
name|containerClass
argument_list|)
decl_stmt|;
name|sh
operator|.
name|setInitParameter
argument_list|(
literal|"com.sun.jersey.config.property.resourceConfigClass"
argument_list|,
name|ResourceConfig
operator|.
name|class
operator|.
name|getCanonicalName
argument_list|()
argument_list|)
expr_stmt|;
name|sh
operator|.
name|setInitParameter
argument_list|(
literal|"com.sun.jersey.config.property.packages"
argument_list|,
literal|"jetty"
argument_list|)
expr_stmt|;
comment|// The servlet holder below is instantiated to only handle the case
comment|// of the /status/cluster returning arrays of nodes (live/dead). Without
comment|// this servlet holder, the problem is that the node arrays in the response
comment|// are collapsed to single nodes. We want to be able to treat the
comment|// node lists as POJO in the response to /status/cluster servlet call,
comment|// but not change the behavior for any of the other servlets
comment|// Hence we don't use the servlet holder for all servlets / paths
name|ServletHolder
name|shPojoMap
init|=
operator|new
name|ServletHolder
argument_list|(
name|containerClass
argument_list|)
decl_stmt|;
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|shInitMap
init|=
name|sh
operator|.
name|getInitParameters
argument_list|()
decl_stmt|;
for|for
control|(
name|Entry
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|e
range|:
name|shInitMap
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|shPojoMap
operator|.
name|setInitParameter
argument_list|(
name|e
operator|.
name|getKey
argument_list|()
argument_list|,
name|e
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|shPojoMap
operator|.
name|setInitParameter
argument_list|(
name|JSONConfiguration
operator|.
name|FEATURE_POJO_MAPPING
argument_list|,
literal|"true"
argument_list|)
expr_stmt|;
comment|// set up Jetty and run the embedded server
name|Server
name|server
init|=
operator|new
name|Server
argument_list|()
decl_stmt|;
name|Connector
name|connector
init|=
operator|new
name|SelectChannelConnector
argument_list|()
decl_stmt|;
if|if
condition|(
name|conf
operator|.
name|getBoolean
argument_list|(
name|REST_SSL_ENABLED
argument_list|,
literal|false
argument_list|)
condition|)
block|{
name|SslSelectChannelConnector
name|sslConnector
init|=
operator|new
name|SslSelectChannelConnector
argument_list|()
decl_stmt|;
name|String
name|keystore
init|=
name|conf
operator|.
name|get
argument_list|(
name|REST_SSL_KEYSTORE_STORE
argument_list|)
decl_stmt|;
name|String
name|password
init|=
name|conf
operator|.
name|get
argument_list|(
name|REST_SSL_KEYSTORE_PASSWORD
argument_list|)
decl_stmt|;
name|String
name|keyPassword
init|=
name|conf
operator|.
name|get
argument_list|(
name|REST_SSL_KEYSTORE_KEYPASSWORD
argument_list|,
name|password
argument_list|)
decl_stmt|;
name|sslConnector
operator|.
name|setKeystore
argument_list|(
name|keystore
argument_list|)
expr_stmt|;
name|sslConnector
operator|.
name|setPassword
argument_list|(
name|password
argument_list|)
expr_stmt|;
name|sslConnector
operator|.
name|setKeyPassword
argument_list|(
name|keyPassword
argument_list|)
expr_stmt|;
name|connector
operator|=
name|sslConnector
expr_stmt|;
block|}
name|connector
operator|.
name|setPort
argument_list|(
name|servlet
operator|.
name|getConfiguration
argument_list|()
operator|.
name|getInt
argument_list|(
literal|"hbase.rest.port"
argument_list|,
literal|8080
argument_list|)
argument_list|)
expr_stmt|;
name|connector
operator|.
name|setHost
argument_list|(
name|servlet
operator|.
name|getConfiguration
argument_list|()
operator|.
name|get
argument_list|(
literal|"hbase.rest.host"
argument_list|,
literal|"0.0.0.0"
argument_list|)
argument_list|)
expr_stmt|;
name|server
operator|.
name|addConnector
argument_list|(
name|connector
argument_list|)
expr_stmt|;
comment|// Set the default max thread number to 100 to limit
comment|// the number of concurrent requests so that REST server doesn't OOM easily.
comment|// Jetty set the default max thread number to 250, if we don't set it.
comment|//
comment|// Our default min thread number 2 is the same as that used by Jetty.
name|int
name|maxThreads
init|=
name|servlet
operator|.
name|getConfiguration
argument_list|()
operator|.
name|getInt
argument_list|(
literal|"hbase.rest.threads.max"
argument_list|,
literal|100
argument_list|)
decl_stmt|;
name|int
name|minThreads
init|=
name|servlet
operator|.
name|getConfiguration
argument_list|()
operator|.
name|getInt
argument_list|(
literal|"hbase.rest.threads.min"
argument_list|,
literal|2
argument_list|)
decl_stmt|;
name|QueuedThreadPool
name|threadPool
init|=
operator|new
name|QueuedThreadPool
argument_list|(
name|maxThreads
argument_list|)
decl_stmt|;
name|threadPool
operator|.
name|setMinThreads
argument_list|(
name|minThreads
argument_list|)
expr_stmt|;
name|server
operator|.
name|setThreadPool
argument_list|(
name|threadPool
argument_list|)
expr_stmt|;
name|server
operator|.
name|setSendServerVersion
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|server
operator|.
name|setSendDateHeader
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|server
operator|.
name|setStopAtShutdown
argument_list|(
literal|true
argument_list|)
expr_stmt|;
comment|// set up context
name|Context
name|context
init|=
operator|new
name|Context
argument_list|(
name|server
argument_list|,
literal|"/"
argument_list|,
name|Context
operator|.
name|SESSIONS
argument_list|)
decl_stmt|;
name|context
operator|.
name|addServlet
argument_list|(
name|shPojoMap
argument_list|,
literal|"/status/cluster"
argument_list|)
expr_stmt|;
name|context
operator|.
name|addServlet
argument_list|(
name|sh
argument_list|,
literal|"/*"
argument_list|)
expr_stmt|;
if|if
condition|(
name|authFilter
operator|!=
literal|null
condition|)
block|{
name|context
operator|.
name|addFilter
argument_list|(
name|authFilter
argument_list|,
literal|"/*"
argument_list|,
literal|1
argument_list|)
expr_stmt|;
block|}
comment|// Load filters from configuration.
name|String
index|[]
name|filterClasses
init|=
name|servlet
operator|.
name|getConfiguration
argument_list|()
operator|.
name|getStrings
argument_list|(
name|FILTER_CLASSES
argument_list|,
name|ArrayUtils
operator|.
name|EMPTY_STRING_ARRAY
argument_list|)
decl_stmt|;
for|for
control|(
name|String
name|filter
range|:
name|filterClasses
control|)
block|{
name|filter
operator|=
name|filter
operator|.
name|trim
argument_list|()
expr_stmt|;
name|context
operator|.
name|addFilter
argument_list|(
name|Class
operator|.
name|forName
argument_list|(
name|filter
argument_list|)
argument_list|,
literal|"/*"
argument_list|,
literal|0
argument_list|)
expr_stmt|;
block|}
name|HttpServerUtil
operator|.
name|constrainHttpMethods
argument_list|(
name|context
argument_list|)
expr_stmt|;
comment|// Put up info server.
name|int
name|port
init|=
name|conf
operator|.
name|getInt
argument_list|(
literal|"hbase.rest.info.port"
argument_list|,
literal|8085
argument_list|)
decl_stmt|;
if|if
condition|(
name|port
operator|>=
literal|0
condition|)
block|{
name|conf
operator|.
name|setLong
argument_list|(
literal|"startcode"
argument_list|,
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|)
expr_stmt|;
name|String
name|a
init|=
name|conf
operator|.
name|get
argument_list|(
literal|"hbase.rest.info.bindAddress"
argument_list|,
literal|"0.0.0.0"
argument_list|)
decl_stmt|;
name|InfoServer
name|infoServer
init|=
operator|new
name|InfoServer
argument_list|(
literal|"rest"
argument_list|,
name|a
argument_list|,
name|port
argument_list|,
literal|false
argument_list|,
name|conf
argument_list|)
decl_stmt|;
name|infoServer
operator|.
name|setAttribute
argument_list|(
literal|"hbase.conf"
argument_list|,
name|conf
argument_list|)
expr_stmt|;
name|infoServer
operator|.
name|start
argument_list|()
expr_stmt|;
block|}
comment|// start server
name|server
operator|.
name|start
argument_list|()
expr_stmt|;
name|server
operator|.
name|join
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

