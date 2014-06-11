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
operator|.
name|thrift2
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
name|net
operator|.
name|InetAddress
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|InetSocketAddress
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|UnknownHostException
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
name|concurrent
operator|.
name|ExecutorService
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
name|LinkedBlockingQueue
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
name|ThreadPoolExecutor
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
name|TimeUnit
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
name|Option
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
name|OptionGroup
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
name|filter
operator|.
name|ParseFilter
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
name|thrift
operator|.
name|CallQueue
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
name|CallQueue
operator|.
name|Call
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
name|ThriftMetrics
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
name|thrift2
operator|.
name|generated
operator|.
name|THBaseService
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
name|GenericOptionsParser
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|protocol
operator|.
name|TBinaryProtocol
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|protocol
operator|.
name|TCompactProtocol
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|protocol
operator|.
name|TProtocolFactory
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|server
operator|.
name|THsHaServer
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|server
operator|.
name|TNonblockingServer
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|server
operator|.
name|TServer
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|server
operator|.
name|TThreadPoolServer
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|transport
operator|.
name|TFramedTransport
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|transport
operator|.
name|TNonblockingServerSocket
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|transport
operator|.
name|TNonblockingServerTransport
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|transport
operator|.
name|TServerSocket
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|transport
operator|.
name|TServerTransport
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|transport
operator|.
name|TTransportException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|transport
operator|.
name|TTransportFactory
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
name|util
operator|.
name|concurrent
operator|.
name|ThreadFactoryBuilder
import|;
end_import

begin_comment
comment|/**  * ThriftServer - this class starts up a Thrift server which implements the HBase API specified in the  * HbaseClient.thrift IDL file.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
annotation|@
name|SuppressWarnings
argument_list|(
block|{
literal|"rawtypes"
block|,
literal|"unchecked"
block|}
argument_list|)
specifier|public
class|class
name|ThriftServer
block|{
specifier|private
specifier|static
specifier|final
name|Log
name|log
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
specifier|public
specifier|static
specifier|final
name|String
name|DEFAULT_LISTEN_PORT
init|=
literal|"9090"
decl_stmt|;
specifier|public
name|ThriftServer
parameter_list|()
block|{   }
specifier|private
specifier|static
name|void
name|printUsage
parameter_list|()
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
name|getOptions
argument_list|()
argument_list|,
literal|"To start the Thrift server run 'bin/hbase-daemon.sh start thrift2'\n"
operator|+
literal|"To shutdown the thrift server run 'bin/hbase-daemon.sh stop thrift2' or"
operator|+
literal|" send a kill signal to the thrift server pid"
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|static
name|Options
name|getOptions
parameter_list|()
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
literal|"bind"
argument_list|,
literal|true
argument_list|,
literal|"Address to bind the Thrift server to. [default: 0.0.0.0]"
argument_list|)
expr_stmt|;
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
literal|"framed"
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
literal|"compact"
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
name|OptionGroup
name|servers
init|=
operator|new
name|OptionGroup
argument_list|()
decl_stmt|;
name|servers
operator|.
name|addOption
argument_list|(
operator|new
name|Option
argument_list|(
literal|"nonblocking"
argument_list|,
literal|false
argument_list|,
literal|"Use the TNonblockingServer. This implies the framed transport."
argument_list|)
argument_list|)
expr_stmt|;
name|servers
operator|.
name|addOption
argument_list|(
operator|new
name|Option
argument_list|(
literal|"hsha"
argument_list|,
literal|false
argument_list|,
literal|"Use the THsHaServer. This implies the framed transport."
argument_list|)
argument_list|)
expr_stmt|;
name|servers
operator|.
name|addOption
argument_list|(
operator|new
name|Option
argument_list|(
literal|"threadpool"
argument_list|,
literal|false
argument_list|,
literal|"Use the TThreadPoolServer. This is the default."
argument_list|)
argument_list|)
expr_stmt|;
name|options
operator|.
name|addOptionGroup
argument_list|(
name|servers
argument_list|)
expr_stmt|;
return|return
name|options
return|;
block|}
specifier|private
specifier|static
name|CommandLine
name|parseArguments
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|Options
name|options
parameter_list|,
name|String
index|[]
name|args
parameter_list|)
throws|throws
name|ParseException
throws|,
name|IOException
block|{
name|GenericOptionsParser
name|genParser
init|=
operator|new
name|GenericOptionsParser
argument_list|(
name|conf
argument_list|,
name|args
argument_list|)
decl_stmt|;
name|String
index|[]
name|remainingArgs
init|=
name|genParser
operator|.
name|getRemainingArgs
argument_list|()
decl_stmt|;
name|CommandLineParser
name|parser
init|=
operator|new
name|PosixParser
argument_list|()
decl_stmt|;
return|return
name|parser
operator|.
name|parse
argument_list|(
name|options
argument_list|,
name|remainingArgs
argument_list|)
return|;
block|}
specifier|private
specifier|static
name|TProtocolFactory
name|getTProtocolFactory
parameter_list|(
name|boolean
name|isCompact
parameter_list|)
block|{
if|if
condition|(
name|isCompact
condition|)
block|{
name|log
operator|.
name|debug
argument_list|(
literal|"Using compact protocol"
argument_list|)
expr_stmt|;
return|return
operator|new
name|TCompactProtocol
operator|.
name|Factory
argument_list|()
return|;
block|}
else|else
block|{
name|log
operator|.
name|debug
argument_list|(
literal|"Using binary protocol"
argument_list|)
expr_stmt|;
return|return
operator|new
name|TBinaryProtocol
operator|.
name|Factory
argument_list|()
return|;
block|}
block|}
specifier|private
specifier|static
name|TTransportFactory
name|getTTransportFactory
parameter_list|(
name|boolean
name|framed
parameter_list|)
block|{
if|if
condition|(
name|framed
condition|)
block|{
name|log
operator|.
name|debug
argument_list|(
literal|"Using framed transport"
argument_list|)
expr_stmt|;
return|return
operator|new
name|TFramedTransport
operator|.
name|Factory
argument_list|()
return|;
block|}
else|else
block|{
return|return
operator|new
name|TTransportFactory
argument_list|()
return|;
block|}
block|}
comment|/*    * If bindValue is null, we don't bind.    */
specifier|private
specifier|static
name|InetSocketAddress
name|bindToPort
parameter_list|(
name|String
name|bindValue
parameter_list|,
name|int
name|listenPort
parameter_list|)
throws|throws
name|UnknownHostException
block|{
try|try
block|{
if|if
condition|(
name|bindValue
operator|==
literal|null
condition|)
block|{
return|return
operator|new
name|InetSocketAddress
argument_list|(
name|listenPort
argument_list|)
return|;
block|}
else|else
block|{
return|return
operator|new
name|InetSocketAddress
argument_list|(
name|InetAddress
operator|.
name|getByName
argument_list|(
name|bindValue
argument_list|)
argument_list|,
name|listenPort
argument_list|)
return|;
block|}
block|}
catch|catch
parameter_list|(
name|UnknownHostException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Could not bind to provided ip address"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
specifier|private
specifier|static
name|TServer
name|getTNonBlockingServer
parameter_list|(
name|TProtocolFactory
name|protocolFactory
parameter_list|,
name|THBaseService
operator|.
name|Processor
name|processor
parameter_list|,
name|TTransportFactory
name|transportFactory
parameter_list|,
name|InetSocketAddress
name|inetSocketAddress
parameter_list|)
throws|throws
name|TTransportException
block|{
name|TNonblockingServerTransport
name|serverTransport
init|=
operator|new
name|TNonblockingServerSocket
argument_list|(
name|inetSocketAddress
argument_list|)
decl_stmt|;
name|log
operator|.
name|info
argument_list|(
literal|"starting HBase Nonblocking Thrift server on "
operator|+
name|inetSocketAddress
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|TNonblockingServer
operator|.
name|Args
name|serverArgs
init|=
operator|new
name|TNonblockingServer
operator|.
name|Args
argument_list|(
name|serverTransport
argument_list|)
decl_stmt|;
name|serverArgs
operator|.
name|processor
argument_list|(
name|processor
argument_list|)
expr_stmt|;
name|serverArgs
operator|.
name|transportFactory
argument_list|(
name|transportFactory
argument_list|)
expr_stmt|;
name|serverArgs
operator|.
name|protocolFactory
argument_list|(
name|protocolFactory
argument_list|)
expr_stmt|;
return|return
operator|new
name|TNonblockingServer
argument_list|(
name|serverArgs
argument_list|)
return|;
block|}
specifier|private
specifier|static
name|TServer
name|getTHsHaServer
parameter_list|(
name|TProtocolFactory
name|protocolFactory
parameter_list|,
name|THBaseService
operator|.
name|Processor
name|processor
parameter_list|,
name|TTransportFactory
name|transportFactory
parameter_list|,
name|InetSocketAddress
name|inetSocketAddress
parameter_list|,
name|ThriftMetrics
name|metrics
parameter_list|)
throws|throws
name|TTransportException
block|{
name|TNonblockingServerTransport
name|serverTransport
init|=
operator|new
name|TNonblockingServerSocket
argument_list|(
name|inetSocketAddress
argument_list|)
decl_stmt|;
name|log
operator|.
name|info
argument_list|(
literal|"starting HBase HsHA Thrift server on "
operator|+
name|inetSocketAddress
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|THsHaServer
operator|.
name|Args
name|serverArgs
init|=
operator|new
name|THsHaServer
operator|.
name|Args
argument_list|(
name|serverTransport
argument_list|)
decl_stmt|;
name|ExecutorService
name|executorService
init|=
name|createExecutor
argument_list|(
name|serverArgs
operator|.
name|getWorkerThreads
argument_list|()
argument_list|,
name|metrics
argument_list|)
decl_stmt|;
name|serverArgs
operator|.
name|executorService
argument_list|(
name|executorService
argument_list|)
expr_stmt|;
name|serverArgs
operator|.
name|processor
argument_list|(
name|processor
argument_list|)
expr_stmt|;
name|serverArgs
operator|.
name|transportFactory
argument_list|(
name|transportFactory
argument_list|)
expr_stmt|;
name|serverArgs
operator|.
name|protocolFactory
argument_list|(
name|protocolFactory
argument_list|)
expr_stmt|;
return|return
operator|new
name|THsHaServer
argument_list|(
name|serverArgs
argument_list|)
return|;
block|}
specifier|private
specifier|static
name|ExecutorService
name|createExecutor
parameter_list|(
name|int
name|workerThreads
parameter_list|,
name|ThriftMetrics
name|metrics
parameter_list|)
block|{
name|CallQueue
name|callQueue
init|=
operator|new
name|CallQueue
argument_list|(
operator|new
name|LinkedBlockingQueue
argument_list|<
name|Call
argument_list|>
argument_list|()
argument_list|,
name|metrics
argument_list|)
decl_stmt|;
name|ThreadFactoryBuilder
name|tfb
init|=
operator|new
name|ThreadFactoryBuilder
argument_list|()
decl_stmt|;
name|tfb
operator|.
name|setDaemon
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|tfb
operator|.
name|setNameFormat
argument_list|(
literal|"thrift2-worker-%d"
argument_list|)
expr_stmt|;
return|return
operator|new
name|ThreadPoolExecutor
argument_list|(
name|workerThreads
argument_list|,
name|workerThreads
argument_list|,
name|Long
operator|.
name|MAX_VALUE
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|,
name|callQueue
argument_list|,
name|tfb
operator|.
name|build
argument_list|()
argument_list|)
return|;
block|}
specifier|private
specifier|static
name|TServer
name|getTThreadPoolServer
parameter_list|(
name|TProtocolFactory
name|protocolFactory
parameter_list|,
name|THBaseService
operator|.
name|Processor
name|processor
parameter_list|,
name|TTransportFactory
name|transportFactory
parameter_list|,
name|InetSocketAddress
name|inetSocketAddress
parameter_list|)
throws|throws
name|TTransportException
block|{
name|TServerTransport
name|serverTransport
init|=
operator|new
name|TServerSocket
argument_list|(
name|inetSocketAddress
argument_list|)
decl_stmt|;
name|log
operator|.
name|info
argument_list|(
literal|"starting HBase ThreadPool Thrift server on "
operator|+
name|inetSocketAddress
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|TThreadPoolServer
operator|.
name|Args
name|serverArgs
init|=
operator|new
name|TThreadPoolServer
operator|.
name|Args
argument_list|(
name|serverTransport
argument_list|)
decl_stmt|;
name|serverArgs
operator|.
name|processor
argument_list|(
name|processor
argument_list|)
expr_stmt|;
name|serverArgs
operator|.
name|transportFactory
argument_list|(
name|transportFactory
argument_list|)
expr_stmt|;
name|serverArgs
operator|.
name|protocolFactory
argument_list|(
name|protocolFactory
argument_list|)
expr_stmt|;
return|return
operator|new
name|TThreadPoolServer
argument_list|(
name|serverArgs
argument_list|)
return|;
block|}
comment|/**    * Adds the option to pre-load filters at startup.    *    * @param conf  The current configuration instance.    */
specifier|protected
specifier|static
name|void
name|registerFilters
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
name|String
index|[]
name|filters
init|=
name|conf
operator|.
name|getStrings
argument_list|(
literal|"hbase.thrift.filters"
argument_list|)
decl_stmt|;
if|if
condition|(
name|filters
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|String
name|filterClass
range|:
name|filters
control|)
block|{
name|String
index|[]
name|filterPart
init|=
name|filterClass
operator|.
name|split
argument_list|(
literal|":"
argument_list|)
decl_stmt|;
if|if
condition|(
name|filterPart
operator|.
name|length
operator|!=
literal|2
condition|)
block|{
name|log
operator|.
name|warn
argument_list|(
literal|"Invalid filter specification "
operator|+
name|filterClass
operator|+
literal|" - skipping"
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|ParseFilter
operator|.
name|registerFilter
argument_list|(
name|filterPart
index|[
literal|0
index|]
argument_list|,
name|filterPart
index|[
literal|1
index|]
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
comment|/**    * Start up the Thrift2 server.    *    * @param args    */
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
name|TServer
name|server
init|=
literal|null
decl_stmt|;
name|Options
name|options
init|=
name|getOptions
argument_list|()
decl_stmt|;
name|Configuration
name|conf
init|=
name|HBaseConfiguration
operator|.
name|create
argument_list|()
decl_stmt|;
name|CommandLine
name|cmd
init|=
name|parseArguments
argument_list|(
name|conf
argument_list|,
name|options
argument_list|,
name|args
argument_list|)
decl_stmt|;
comment|/**      * This is to please both bin/hbase and bin/hbase-daemon. hbase-daemon provides "start" and "stop" arguments hbase      * should print the help if no argument is provided      */
name|List
argument_list|<
name|?
argument_list|>
name|argList
init|=
name|cmd
operator|.
name|getArgList
argument_list|()
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
operator|!
name|argList
operator|.
name|contains
argument_list|(
literal|"start"
argument_list|)
operator|||
name|argList
operator|.
name|contains
argument_list|(
literal|"stop"
argument_list|)
condition|)
block|{
name|printUsage
argument_list|()
expr_stmt|;
name|System
operator|.
name|exit
argument_list|(
literal|1
argument_list|)
expr_stmt|;
block|}
comment|// Get port to bind to
name|int
name|listenPort
init|=
literal|0
decl_stmt|;
try|try
block|{
name|listenPort
operator|=
name|Integer
operator|.
name|parseInt
argument_list|(
name|cmd
operator|.
name|getOptionValue
argument_list|(
literal|"port"
argument_list|,
name|DEFAULT_LISTEN_PORT
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|NumberFormatException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Could not parse the value provided for the port option"
argument_list|,
name|e
argument_list|)
throw|;
block|}
name|boolean
name|nonblocking
init|=
name|cmd
operator|.
name|hasOption
argument_list|(
literal|"nonblocking"
argument_list|)
decl_stmt|;
name|boolean
name|hsha
init|=
name|cmd
operator|.
name|hasOption
argument_list|(
literal|"hsha"
argument_list|)
decl_stmt|;
name|ThriftMetrics
name|metrics
init|=
operator|new
name|ThriftMetrics
argument_list|(
name|conf
argument_list|,
name|ThriftMetrics
operator|.
name|ThriftServerType
operator|.
name|TWO
argument_list|)
decl_stmt|;
name|String
name|implType
init|=
literal|"threadpool"
decl_stmt|;
if|if
condition|(
name|nonblocking
condition|)
block|{
name|implType
operator|=
literal|"nonblocking"
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|hsha
condition|)
block|{
name|implType
operator|=
literal|"hsha"
expr_stmt|;
block|}
name|conf
operator|.
name|set
argument_list|(
literal|"hbase.regionserver.thrift.server.type"
argument_list|,
name|implType
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setInt
argument_list|(
literal|"hbase.regionserver.thrift.port"
argument_list|,
name|listenPort
argument_list|)
expr_stmt|;
name|registerFilters
argument_list|(
name|conf
argument_list|)
expr_stmt|;
comment|// Construct correct ProtocolFactory
name|boolean
name|compact
init|=
name|cmd
operator|.
name|hasOption
argument_list|(
literal|"compact"
argument_list|)
operator|||
name|conf
operator|.
name|getBoolean
argument_list|(
literal|"hbase.regionserver.thrift.compact"
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|TProtocolFactory
name|protocolFactory
init|=
name|getTProtocolFactory
argument_list|(
name|compact
argument_list|)
decl_stmt|;
name|THBaseService
operator|.
name|Iface
name|handler
init|=
name|ThriftHBaseServiceHandler
operator|.
name|newInstance
argument_list|(
name|conf
argument_list|,
name|metrics
argument_list|)
decl_stmt|;
name|THBaseService
operator|.
name|Processor
name|processor
init|=
operator|new
name|THBaseService
operator|.
name|Processor
argument_list|(
name|handler
argument_list|)
decl_stmt|;
name|conf
operator|.
name|setBoolean
argument_list|(
literal|"hbase.regionserver.thrift.compact"
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
literal|"framed"
argument_list|)
operator|||
name|conf
operator|.
name|getBoolean
argument_list|(
literal|"hbase.regionserver.thrift.framed"
argument_list|,
literal|false
argument_list|)
operator|||
name|nonblocking
operator|||
name|hsha
decl_stmt|;
name|TTransportFactory
name|transportFactory
init|=
name|getTTransportFactory
argument_list|(
name|framed
argument_list|)
decl_stmt|;
name|InetSocketAddress
name|inetSocketAddress
init|=
name|bindToPort
argument_list|(
name|cmd
operator|.
name|getOptionValue
argument_list|(
literal|"bind"
argument_list|)
argument_list|,
name|listenPort
argument_list|)
decl_stmt|;
name|conf
operator|.
name|setBoolean
argument_list|(
literal|"hbase.regionserver.thrift.framed"
argument_list|,
name|framed
argument_list|)
expr_stmt|;
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
name|log
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
name|log
operator|.
name|error
argument_list|(
literal|"Could not parse the value provided for the infoport option"
argument_list|,
name|e
argument_list|)
expr_stmt|;
name|printUsage
argument_list|()
expr_stmt|;
name|System
operator|.
name|exit
argument_list|(
literal|1
argument_list|)
expr_stmt|;
block|}
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
name|InfoServer
name|infoServer
init|=
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
if|if
condition|(
name|nonblocking
condition|)
block|{
name|server
operator|=
name|getTNonBlockingServer
argument_list|(
name|protocolFactory
argument_list|,
name|processor
argument_list|,
name|transportFactory
argument_list|,
name|inetSocketAddress
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|hsha
condition|)
block|{
name|server
operator|=
name|getTHsHaServer
argument_list|(
name|protocolFactory
argument_list|,
name|processor
argument_list|,
name|transportFactory
argument_list|,
name|inetSocketAddress
argument_list|,
name|metrics
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|server
operator|=
name|getTThreadPoolServer
argument_list|(
name|protocolFactory
argument_list|,
name|processor
argument_list|,
name|transportFactory
argument_list|,
name|inetSocketAddress
argument_list|)
expr_stmt|;
block|}
name|server
operator|.
name|serve
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

