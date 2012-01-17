begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Copyright The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one or more  * contributor license agreements. See the NOTICE file distributed with this  * work for additional information regarding copyright ownership. The ASF  * licenses this file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the  * License for the specific language governing permissions and limitations  * under the License.  */
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
name|InetAddress
import|;
end_import

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|ByteBuffer
import|;
end_import

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
name|Collection
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
name|thrift
operator|.
name|generated
operator|.
name|Hbase
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
name|Threads
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
name|TProtocol
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
name|TSocket
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
name|TTransport
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

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|runner
operator|.
name|RunWith
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|runners
operator|.
name|Parameterized
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|runners
operator|.
name|Parameterized
operator|.
name|Parameters
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
name|Joiner
import|;
end_import

begin_comment
comment|/**  * Start the HBase Thrift server on a random port through the command-line  * interface and talk to it from client side.  */
end_comment

begin_class
annotation|@
name|Category
argument_list|(
name|LargeTests
operator|.
name|class
argument_list|)
annotation|@
name|RunWith
argument_list|(
name|Parameterized
operator|.
name|class
argument_list|)
specifier|public
class|class
name|TestThriftServerCmdLine
block|{
specifier|public
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|TestThriftServerCmdLine
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|ImplType
name|implType
decl_stmt|;
specifier|private
name|boolean
name|specifyFramed
decl_stmt|;
specifier|private
name|boolean
name|specifyBindIP
decl_stmt|;
specifier|private
name|boolean
name|specifyCompact
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
name|Thread
name|cmdLineThread
decl_stmt|;
specifier|private
specifier|volatile
name|Exception
name|cmdLineException
decl_stmt|;
specifier|private
name|Exception
name|clientSideException
decl_stmt|;
specifier|private
name|ThriftServer
name|thriftServer
decl_stmt|;
specifier|private
name|int
name|port
decl_stmt|;
annotation|@
name|Parameters
specifier|public
specifier|static
name|Collection
argument_list|<
name|Object
index|[]
argument_list|>
name|getParameters
parameter_list|()
block|{
name|Collection
argument_list|<
name|Object
index|[]
argument_list|>
name|parameters
init|=
operator|new
name|ArrayList
argument_list|<
name|Object
index|[]
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|ImplType
name|implType
range|:
name|ImplType
operator|.
name|values
argument_list|()
control|)
block|{
for|for
control|(
name|boolean
name|specifyFramed
range|:
operator|new
name|boolean
index|[]
block|{
literal|false
block|,
literal|true
block|}
control|)
block|{
for|for
control|(
name|boolean
name|specifyBindIP
range|:
operator|new
name|boolean
index|[]
block|{
literal|false
block|,
literal|true
block|}
control|)
block|{
if|if
condition|(
name|specifyBindIP
operator|&&
operator|!
name|implType
operator|.
name|canSpecifyBindIP
condition|)
block|{
continue|continue;
block|}
for|for
control|(
name|boolean
name|specifyCompact
range|:
operator|new
name|boolean
index|[]
block|{
literal|false
block|,
literal|true
block|}
control|)
block|{
name|parameters
operator|.
name|add
argument_list|(
operator|new
name|Object
index|[]
block|{
name|implType
block|,
operator|new
name|Boolean
argument_list|(
name|specifyFramed
argument_list|)
block|,
operator|new
name|Boolean
argument_list|(
name|specifyBindIP
argument_list|)
block|,
operator|new
name|Boolean
argument_list|(
name|specifyCompact
argument_list|)
block|}
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
return|return
name|parameters
return|;
block|}
specifier|public
name|TestThriftServerCmdLine
parameter_list|(
name|ImplType
name|implType
parameter_list|,
name|boolean
name|specifyFramed
parameter_list|,
name|boolean
name|specifyBindIP
parameter_list|,
name|boolean
name|specifyCompact
parameter_list|)
block|{
name|this
operator|.
name|implType
operator|=
name|implType
expr_stmt|;
name|this
operator|.
name|specifyFramed
operator|=
name|specifyFramed
expr_stmt|;
name|this
operator|.
name|specifyBindIP
operator|=
name|specifyBindIP
expr_stmt|;
name|this
operator|.
name|specifyCompact
operator|=
name|specifyCompact
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"implType="
operator|+
name|implType
operator|+
literal|", "
operator|+
literal|"specifyFramed="
operator|+
name|specifyFramed
operator|+
literal|", "
operator|+
literal|"specifyBindIP="
operator|+
name|specifyBindIP
operator|+
literal|", "
operator|+
literal|"specifyCompact="
operator|+
name|specifyCompact
argument_list|)
expr_stmt|;
block|}
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
name|TEST_UTIL
operator|.
name|startMiniCluster
argument_list|()
expr_stmt|;
block|}
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
specifier|private
name|void
name|startCmdLineThread
parameter_list|(
specifier|final
name|String
index|[]
name|args
parameter_list|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Starting HBase Thrift server with command line: "
operator|+
name|Joiner
operator|.
name|on
argument_list|(
literal|" "
argument_list|)
operator|.
name|join
argument_list|(
name|args
argument_list|)
argument_list|)
expr_stmt|;
name|cmdLineException
operator|=
literal|null
expr_stmt|;
name|cmdLineThread
operator|=
operator|new
name|Thread
argument_list|(
operator|new
name|Runnable
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
try|try
block|{
name|thriftServer
operator|.
name|doMain
argument_list|(
name|args
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|cmdLineException
operator|=
name|e
expr_stmt|;
block|}
block|}
block|}
argument_list|)
expr_stmt|;
name|cmdLineThread
operator|.
name|setName
argument_list|(
name|ThriftServer
operator|.
name|class
operator|.
name|getSimpleName
argument_list|()
operator|+
literal|"-cmdline"
argument_list|)
expr_stmt|;
name|cmdLineThread
operator|.
name|start
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|30
operator|*
literal|1000
argument_list|)
specifier|public
name|void
name|testRunThriftServer
parameter_list|()
throws|throws
name|Exception
block|{
name|List
argument_list|<
name|String
argument_list|>
name|args
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
if|if
condition|(
name|implType
operator|!=
literal|null
condition|)
block|{
name|String
name|serverTypeOption
init|=
name|implType
operator|.
name|toString
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
name|serverTypeOption
operator|.
name|startsWith
argument_list|(
literal|"-"
argument_list|)
argument_list|)
expr_stmt|;
name|args
operator|.
name|add
argument_list|(
name|serverTypeOption
argument_list|)
expr_stmt|;
block|}
name|port
operator|=
name|HBaseTestingUtility
operator|.
name|randomFreePort
argument_list|()
expr_stmt|;
name|args
operator|.
name|add
argument_list|(
literal|"-"
operator|+
name|ThriftServer
operator|.
name|PORT_OPTION
argument_list|)
expr_stmt|;
name|args
operator|.
name|add
argument_list|(
name|String
operator|.
name|valueOf
argument_list|(
name|port
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|specifyFramed
condition|)
block|{
name|args
operator|.
name|add
argument_list|(
literal|"-"
operator|+
name|ThriftServer
operator|.
name|FRAMED_OPTION
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|specifyBindIP
condition|)
block|{
name|args
operator|.
name|add
argument_list|(
literal|"-"
operator|+
name|ThriftServer
operator|.
name|BIND_OPTION
argument_list|)
expr_stmt|;
name|args
operator|.
name|add
argument_list|(
name|InetAddress
operator|.
name|getLocalHost
argument_list|()
operator|.
name|getHostName
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|specifyCompact
condition|)
block|{
name|args
operator|.
name|add
argument_list|(
literal|"-"
operator|+
name|ThriftServer
operator|.
name|COMPACT_OPTION
argument_list|)
expr_stmt|;
block|}
name|args
operator|.
name|add
argument_list|(
literal|"start"
argument_list|)
expr_stmt|;
name|thriftServer
operator|=
operator|new
name|ThriftServer
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
expr_stmt|;
name|startCmdLineThread
argument_list|(
name|args
operator|.
name|toArray
argument_list|(
operator|new
name|String
index|[
literal|0
index|]
argument_list|)
argument_list|)
expr_stmt|;
name|Threads
operator|.
name|sleepWithoutInterrupt
argument_list|(
literal|2000
argument_list|)
expr_stmt|;
name|Class
argument_list|<
name|?
extends|extends
name|TServer
argument_list|>
name|expectedClass
init|=
name|implType
operator|!=
literal|null
condition|?
name|implType
operator|.
name|serverClass
else|:
name|TBoundedThreadPoolServer
operator|.
name|class
decl_stmt|;
name|assertEquals
argument_list|(
name|expectedClass
argument_list|,
name|thriftServer
operator|.
name|serverRunner
operator|.
name|tserver
operator|.
name|getClass
argument_list|()
argument_list|)
expr_stmt|;
try|try
block|{
name|talkToThriftServer
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|ex
parameter_list|)
block|{
name|clientSideException
operator|=
name|ex
expr_stmt|;
block|}
finally|finally
block|{
name|stopCmdLineThread
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|clientSideException
operator|!=
literal|null
condition|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Thrift client threw an exception"
argument_list|,
name|clientSideException
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|Exception
argument_list|(
name|clientSideException
argument_list|)
throw|;
block|}
block|}
specifier|private
name|void
name|talkToThriftServer
parameter_list|()
throws|throws
name|Exception
block|{
name|TSocket
name|sock
init|=
operator|new
name|TSocket
argument_list|(
name|InetAddress
operator|.
name|getLocalHost
argument_list|()
operator|.
name|getHostName
argument_list|()
argument_list|,
name|port
argument_list|)
decl_stmt|;
name|TTransport
name|transport
init|=
name|sock
decl_stmt|;
if|if
condition|(
name|specifyFramed
operator|||
name|implType
operator|.
name|isAlwaysFramed
condition|)
block|{
name|transport
operator|=
operator|new
name|TFramedTransport
argument_list|(
name|transport
argument_list|)
expr_stmt|;
block|}
name|sock
operator|.
name|open
argument_list|()
expr_stmt|;
name|TProtocol
name|prot
decl_stmt|;
if|if
condition|(
name|specifyCompact
condition|)
block|{
name|prot
operator|=
operator|new
name|TCompactProtocol
argument_list|(
name|transport
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|prot
operator|=
operator|new
name|TBinaryProtocol
argument_list|(
name|transport
argument_list|)
expr_stmt|;
block|}
name|Hbase
operator|.
name|Client
name|client
init|=
operator|new
name|Hbase
operator|.
name|Client
argument_list|(
name|prot
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|ByteBuffer
argument_list|>
name|tableNames
init|=
name|client
operator|.
name|getTableNames
argument_list|()
decl_stmt|;
if|if
condition|(
name|tableNames
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|TestThriftServer
operator|.
name|createTestTables
argument_list|(
name|client
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|client
operator|.
name|getTableNames
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|tableNames
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|client
operator|.
name|getColumnDescriptors
argument_list|(
name|tableNames
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|sock
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
specifier|private
name|void
name|stopCmdLineThread
parameter_list|()
throws|throws
name|Exception
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Stopping "
operator|+
name|implType
operator|.
name|simpleClassName
argument_list|()
operator|+
literal|" Thrift server"
argument_list|)
expr_stmt|;
name|thriftServer
operator|.
name|stop
argument_list|()
expr_stmt|;
name|cmdLineThread
operator|.
name|join
argument_list|()
expr_stmt|;
if|if
condition|(
name|cmdLineException
operator|!=
literal|null
condition|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Command-line invocation of HBase Thrift server threw an "
operator|+
literal|"exception"
argument_list|,
name|cmdLineException
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|Exception
argument_list|(
name|cmdLineException
argument_list|)
throw|;
block|}
block|}
annotation|@
name|org
operator|.
name|junit
operator|.
name|Rule
specifier|public
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|ResourceCheckerJUnitRule
name|cu
init|=
operator|new
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|ResourceCheckerJUnitRule
argument_list|()
decl_stmt|;
block|}
end_class

end_unit

