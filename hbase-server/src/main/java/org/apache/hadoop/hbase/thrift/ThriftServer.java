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
name|thrift
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Arrays
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
name|CommandLineParser
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
name|security
operator|.
name|User
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
name|thrift
operator|.
name|ThriftServerRunner
operator|.
name|ImplType
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
name|util
operator|.
name|Shell
operator|.
name|ExitCodeException
import|;
end_import

begin_comment
comment|/**  * ThriftServer- this class starts up a Thrift server which implements the  * Hbase API specified in the Hbase.thrift IDL file. The server runs in an  * independent process.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|ThriftServer
block|{
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
name|ThriftServer
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|MIN_WORKERS_OPTION
init|=
literal|"minWorkers"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|MAX_WORKERS_OPTION
init|=
literal|"workers"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|MAX_QUEUE_SIZE_OPTION
init|=
literal|"queue"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|KEEP_ALIVE_SEC_OPTION
init|=
literal|"keepAliveSec"
decl_stmt|;
specifier|static
specifier|final
name|String
name|BIND_OPTION
init|=
literal|"bind"
decl_stmt|;
specifier|static
specifier|final
name|String
name|COMPACT_OPTION
init|=
literal|"compact"
decl_stmt|;
specifier|static
specifier|final
name|String
name|FRAMED_OPTION
init|=
literal|"framed"
decl_stmt|;
specifier|static
specifier|final
name|String
name|PORT_OPTION
init|=
literal|"port"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|DEFAULT_BIND_ADDR
init|=
literal|"0.0.0.0"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|DEFAULT_LISTEN_PORT
init|=
literal|9090
decl_stmt|;
specifier|private
name|Configuration
name|conf
decl_stmt|;
name|ThriftServerRunner
name|serverRunner
decl_stmt|;
specifier|private
name|InfoServer
name|infoServer
decl_stmt|;
comment|//
comment|// Main program and support routines
comment|//
specifier|public
name|ThriftServer
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
name|this
operator|.
name|conf
operator|=
name|HBaseConfiguration
operator|.
name|create
argument_list|(
name|conf
argument_list|)
expr_stmt|;
block|}
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
throws|throws
name|ExitCodeException
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
literal|"Thrift"
argument_list|,
literal|null
argument_list|,
name|options
argument_list|,
literal|"To start the Thrift server run 'bin/hbase-daemon.sh start thrift'\n"
operator|+
literal|"To shutdown the thrift server run 'bin/hbase-daemon.sh stop "
operator|+
literal|"thrift' or send a kill signal to the thrift server pid"
argument_list|,
literal|true
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|ExitCodeException
argument_list|(
name|exitCode
argument_list|,
literal|""
argument_list|)
throw|;
block|}
comment|/**    * Start up or shuts down the Thrift server, depending on the arguments.    * @param args    */
name|void
name|doMain
parameter_list|(
specifier|final
name|String
index|[]
name|args
parameter_list|)
throws|throws
name|Exception
block|{
name|processOptions
argument_list|(
name|args
argument_list|)
expr_stmt|;
comment|// login the server principal (if using secure Hadoop)
if|if
condition|(
name|User
operator|.
name|isSecurityEnabled
argument_list|()
operator|&&
name|User
operator|.
name|isHBaseSecurityEnabled
argument_list|(
name|conf
argument_list|)
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
literal|"hbase.thrift.dns.interface"
argument_list|,
literal|"default"
argument_list|)
argument_list|,
name|conf
operator|.
name|get
argument_list|(
literal|"hbase.thrift.dns.nameserver"
argument_list|,
literal|"default"
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
name|User
operator|.
name|login
argument_list|(
name|conf
argument_list|,
literal|"hbase.thrift.keytab.file"
argument_list|,
literal|"hbase.thrift.kerberos.principal"
argument_list|,
name|machineName
argument_list|)
expr_stmt|;
block|}
name|serverRunner
operator|=
operator|new
name|ThriftServerRunner
argument_list|(
name|conf
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
literal|"hbase.thrift.info.port"
argument_list|,
literal|9095
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
literal|"hbase.thrift.info.bindAddress"
argument_list|,
literal|"0.0.0.0"
argument_list|)
decl_stmt|;
name|infoServer
operator|=
operator|new
name|InfoServer
argument_list|(
literal|"thrift"
argument_list|,
name|a
argument_list|,
name|port
argument_list|,
literal|false
argument_list|,
name|conf
argument_list|)
expr_stmt|;
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
name|serverRunner
operator|.
name|run
argument_list|()
expr_stmt|;
block|}
comment|/**    * Parse the command line options to set parameters the conf.    */
specifier|private
name|void
name|processOptions
parameter_list|(
specifier|final
name|String
index|[]
name|args
parameter_list|)
throws|throws
name|Exception
block|{
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
literal|"b"
argument_list|,
name|BIND_OPTION
argument_list|,
literal|true
argument_list|,
literal|"Address to bind "
operator|+
literal|"the Thrift server to. [default: "
operator|+
name|DEFAULT_BIND_ADDR
operator|+
literal|"]"
argument_list|)
expr_stmt|;
name|options
operator|.
name|addOption
argument_list|(
literal|"p"
argument_list|,
name|PORT_OPTION
argument_list|,
literal|true
argument_list|,
literal|"Port to bind to [default: "
operator|+
name|DEFAULT_LISTEN_PORT
operator|+
literal|"]"
argument_list|)
expr_stmt|;
name|options
operator|.
name|addOption
argument_list|(
literal|"f"
argument_list|,
name|FRAMED_OPTION
argument_list|,
literal|false
argument_list|,
literal|"Use framed transport"
argument_list|)
expr_stmt|;
name|options
operator|.
name|addOption
argument_list|(
literal|"c"
argument_list|,
name|COMPACT_OPTION
argument_list|,
literal|false
argument_list|,
literal|"Use the compact protocol"
argument_list|)
expr_stmt|;
name|options
operator|.
name|addOption
argument_list|(
literal|"h"
argument_list|,
literal|"help"
argument_list|,
literal|false
argument_list|,
literal|"Print help information"
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
name|options
operator|.
name|addOption
argument_list|(
literal|"m"
argument_list|,
name|MIN_WORKERS_OPTION
argument_list|,
literal|true
argument_list|,
literal|"The minimum number of worker threads for "
operator|+
name|ImplType
operator|.
name|THREAD_POOL
operator|.
name|simpleClassName
argument_list|()
argument_list|)
expr_stmt|;
name|options
operator|.
name|addOption
argument_list|(
literal|"w"
argument_list|,
name|MAX_WORKERS_OPTION
argument_list|,
literal|true
argument_list|,
literal|"The maximum number of worker threads for "
operator|+
name|ImplType
operator|.
name|THREAD_POOL
operator|.
name|simpleClassName
argument_list|()
argument_list|)
expr_stmt|;
name|options
operator|.
name|addOption
argument_list|(
literal|"q"
argument_list|,
name|MAX_QUEUE_SIZE_OPTION
argument_list|,
literal|true
argument_list|,
literal|"The maximum number of queued requests in "
operator|+
name|ImplType
operator|.
name|THREAD_POOL
operator|.
name|simpleClassName
argument_list|()
argument_list|)
expr_stmt|;
name|options
operator|.
name|addOption
argument_list|(
literal|"k"
argument_list|,
name|KEEP_ALIVE_SEC_OPTION
argument_list|,
literal|true
argument_list|,
literal|"The amount of time in secods to keep a thread alive when idle in "
operator|+
name|ImplType
operator|.
name|THREAD_POOL
operator|.
name|simpleClassName
argument_list|()
argument_list|)
expr_stmt|;
name|options
operator|.
name|addOptionGroup
argument_list|(
name|ImplType
operator|.
name|createOptionGroup
argument_list|()
argument_list|)
expr_stmt|;
name|CommandLineParser
name|parser
init|=
operator|new
name|PosixParser
argument_list|()
decl_stmt|;
name|CommandLine
name|cmd
init|=
name|parser
operator|.
name|parse
argument_list|(
name|options
argument_list|,
name|args
argument_list|)
decl_stmt|;
comment|// This is so complicated to please both bin/hbase and bin/hbase-daemon.
comment|// hbase-daemon provides "start" and "stop" arguments
comment|// hbase should print the help if no argument is provided
name|List
argument_list|<
name|String
argument_list|>
name|commandLine
init|=
name|Arrays
operator|.
name|asList
argument_list|(
name|args
argument_list|)
decl_stmt|;
name|boolean
name|stop
init|=
name|commandLine
operator|.
name|contains
argument_list|(
literal|"stop"
argument_list|)
decl_stmt|;
name|boolean
name|start
init|=
name|commandLine
operator|.
name|contains
argument_list|(
literal|"start"
argument_list|)
decl_stmt|;
name|boolean
name|invalidStartStop
init|=
operator|(
name|start
operator|&&
name|stop
operator|)
operator|||
operator|(
operator|!
name|start
operator|&&
operator|!
name|stop
operator|)
decl_stmt|;
if|if
condition|(
name|cmd
operator|.
name|hasOption
argument_list|(
literal|"help"
argument_list|)
operator|||
name|invalidStartStop
condition|)
block|{
if|if
condition|(
name|invalidStartStop
condition|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Exactly one of 'start' and 'stop' has to be specified"
argument_list|)
expr_stmt|;
block|}
name|printUsageAndExit
argument_list|(
name|options
argument_list|,
literal|1
argument_list|)
expr_stmt|;
block|}
comment|// Get port to bind to
try|try
block|{
name|int
name|listenPort
init|=
name|Integer
operator|.
name|parseInt
argument_list|(
name|cmd
operator|.
name|getOptionValue
argument_list|(
name|PORT_OPTION
argument_list|,
name|String
operator|.
name|valueOf
argument_list|(
name|DEFAULT_LISTEN_PORT
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
name|conf
operator|.
name|setInt
argument_list|(
name|ThriftServerRunner
operator|.
name|PORT_CONF_KEY
argument_list|,
name|listenPort
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|NumberFormatException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Could not parse the value provided for the port option"
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
comment|// check for user-defined info server port setting, if so override the conf
try|try
block|{
if|if
condition|(
name|cmd
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
name|cmd
operator|.
name|getOptionValue
argument_list|(
literal|"infoport"
argument_list|)
decl_stmt|;
name|conf
operator|.
name|setInt
argument_list|(
literal|"hbase.thrift.info.port"
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
block|}
catch|catch
parameter_list|(
name|NumberFormatException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Could not parse the value provided for the infoport option"
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
comment|// Make optional changes to the configuration based on command-line options
name|optionToConf
argument_list|(
name|cmd
argument_list|,
name|MIN_WORKERS_OPTION
argument_list|,
name|conf
argument_list|,
name|TBoundedThreadPoolServer
operator|.
name|MIN_WORKER_THREADS_CONF_KEY
argument_list|)
expr_stmt|;
name|optionToConf
argument_list|(
name|cmd
argument_list|,
name|MAX_WORKERS_OPTION
argument_list|,
name|conf
argument_list|,
name|TBoundedThreadPoolServer
operator|.
name|MAX_WORKER_THREADS_CONF_KEY
argument_list|)
expr_stmt|;
name|optionToConf
argument_list|(
name|cmd
argument_list|,
name|MAX_QUEUE_SIZE_OPTION
argument_list|,
name|conf
argument_list|,
name|TBoundedThreadPoolServer
operator|.
name|MAX_QUEUED_REQUESTS_CONF_KEY
argument_list|)
expr_stmt|;
name|optionToConf
argument_list|(
name|cmd
argument_list|,
name|KEEP_ALIVE_SEC_OPTION
argument_list|,
name|conf
argument_list|,
name|TBoundedThreadPoolServer
operator|.
name|THREAD_KEEP_ALIVE_TIME_SEC_CONF_KEY
argument_list|)
expr_stmt|;
comment|// Set general thrift server options
name|boolean
name|compact
init|=
name|cmd
operator|.
name|hasOption
argument_list|(
name|COMPACT_OPTION
argument_list|)
operator|||
name|conf
operator|.
name|getBoolean
argument_list|(
name|ThriftServerRunner
operator|.
name|COMPACT_CONF_KEY
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|conf
operator|.
name|setBoolean
argument_list|(
name|ThriftServerRunner
operator|.
name|COMPACT_CONF_KEY
argument_list|,
name|compact
argument_list|)
expr_stmt|;
name|boolean
name|framed
init|=
name|cmd
operator|.
name|hasOption
argument_list|(
name|FRAMED_OPTION
argument_list|)
operator|||
name|conf
operator|.
name|getBoolean
argument_list|(
name|ThriftServerRunner
operator|.
name|FRAMED_CONF_KEY
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|conf
operator|.
name|setBoolean
argument_list|(
name|ThriftServerRunner
operator|.
name|FRAMED_CONF_KEY
argument_list|,
name|framed
argument_list|)
expr_stmt|;
if|if
condition|(
name|cmd
operator|.
name|hasOption
argument_list|(
name|BIND_OPTION
argument_list|)
condition|)
block|{
name|conf
operator|.
name|set
argument_list|(
name|ThriftServerRunner
operator|.
name|BIND_CONF_KEY
argument_list|,
name|cmd
operator|.
name|getOptionValue
argument_list|(
name|BIND_OPTION
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|ImplType
operator|.
name|setServerImpl
argument_list|(
name|cmd
argument_list|,
name|conf
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|stop
parameter_list|()
block|{
if|if
condition|(
name|this
operator|.
name|infoServer
operator|!=
literal|null
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Stopping infoServer"
argument_list|)
expr_stmt|;
try|try
block|{
name|this
operator|.
name|infoServer
operator|.
name|stop
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|ex
parameter_list|)
block|{
name|ex
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
block|}
block|}
name|serverRunner
operator|.
name|shutdown
argument_list|()
expr_stmt|;
block|}
specifier|private
specifier|static
name|void
name|optionToConf
parameter_list|(
name|CommandLine
name|cmd
parameter_list|,
name|String
name|option
parameter_list|,
name|Configuration
name|conf
parameter_list|,
name|String
name|destConfKey
parameter_list|)
block|{
if|if
condition|(
name|cmd
operator|.
name|hasOption
argument_list|(
name|option
argument_list|)
condition|)
block|{
name|String
name|value
init|=
name|cmd
operator|.
name|getOptionValue
argument_list|(
name|option
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Set configuration key:"
operator|+
name|destConfKey
operator|+
literal|" value:"
operator|+
name|value
argument_list|)
expr_stmt|;
name|conf
operator|.
name|set
argument_list|(
name|destConfKey
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * @param args    * @throws Exception    */
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
name|VersionInfo
operator|.
name|logVersion
argument_list|()
expr_stmt|;
try|try
block|{
operator|new
name|ThriftServer
argument_list|(
name|HBaseConfiguration
operator|.
name|create
argument_list|()
argument_list|)
operator|.
name|doMain
argument_list|(
name|args
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ExitCodeException
name|ex
parameter_list|)
block|{
name|System
operator|.
name|exit
argument_list|(
name|ex
operator|.
name|getExitCode
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

