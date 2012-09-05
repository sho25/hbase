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
name|ipc
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
name|assertFalse
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
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|fail
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
name|java
operator|.
name|lang
operator|.
name|reflect
operator|.
name|Method
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
name|junit
operator|.
name|framework
operator|.
name|Assert
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
name|*
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
name|VersionedProtocol
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
name|protobuf
operator|.
name|generated
operator|.
name|TestDelayedRpcProtos
operator|.
name|TestArg
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
name|protobuf
operator|.
name|generated
operator|.
name|TestDelayedRpcProtos
operator|.
name|TestResponse
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
name|log4j
operator|.
name|AppenderSkeleton
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|log4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|log4j
operator|.
name|spi
operator|.
name|LoggingEvent
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|log4j
operator|.
name|Level
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
name|mortbay
operator|.
name|log
operator|.
name|Log
import|;
end_import

begin_comment
comment|/**  * Test that delayed RPCs work. Fire up three calls, the first of which should  * be delayed. Check that the last two, which are undelayed, return before the  * first one.  */
end_comment

begin_class
annotation|@
name|Category
argument_list|(
name|MediumTests
operator|.
name|class
argument_list|)
comment|// Fails sometimes with small tests
specifier|public
class|class
name|TestDelayedRpc
block|{
specifier|public
specifier|static
name|RpcServer
name|rpcServer
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|int
name|UNDELAYED
init|=
literal|0
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|int
name|DELAYED
init|=
literal|1
decl_stmt|;
annotation|@
name|Test
specifier|public
name|void
name|testDelayedRpcImmediateReturnValue
parameter_list|()
throws|throws
name|Exception
block|{
name|testDelayedRpc
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testDelayedRpcDelayedReturnValue
parameter_list|()
throws|throws
name|Exception
block|{
name|testDelayedRpc
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|testDelayedRpc
parameter_list|(
name|boolean
name|delayReturnValue
parameter_list|)
throws|throws
name|Exception
block|{
name|Configuration
name|conf
init|=
name|HBaseConfiguration
operator|.
name|create
argument_list|()
decl_stmt|;
name|InetSocketAddress
name|isa
init|=
operator|new
name|InetSocketAddress
argument_list|(
literal|"localhost"
argument_list|,
literal|0
argument_list|)
decl_stmt|;
name|rpcServer
operator|=
name|HBaseRPC
operator|.
name|getServer
argument_list|(
operator|new
name|TestRpcImpl
argument_list|(
name|delayReturnValue
argument_list|)
argument_list|,
operator|new
name|Class
argument_list|<
name|?
argument_list|>
index|[]
block|{
name|TestRpcImpl
operator|.
name|class
block|}
operator|,
name|isa
operator|.
name|getHostName
argument_list|()
operator|,
name|isa
operator|.
name|getPort
argument_list|()
operator|,
literal|1
operator|,
literal|0
operator|,
literal|true
operator|,
name|conf
operator|,
literal|0
block|)
function|;
name|rpcServer
operator|.
name|start
parameter_list|()
constructor_decl|;
name|TestRpc
name|client
init|=
operator|(
name|TestRpc
operator|)
name|HBaseRPC
operator|.
name|getProxy
argument_list|(
name|TestRpc
operator|.
name|class
argument_list|,
literal|0
argument_list|,
name|rpcServer
operator|.
name|getListenerAddress
argument_list|()
argument_list|,
name|conf
argument_list|,
literal|1000
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Integer
argument_list|>
name|results
init|=
operator|new
name|ArrayList
argument_list|<
name|Integer
argument_list|>
argument_list|()
decl_stmt|;
name|TestThread
name|th1
init|=
operator|new
name|TestThread
argument_list|(
name|client
argument_list|,
literal|true
argument_list|,
name|results
argument_list|)
decl_stmt|;
name|TestThread
name|th2
init|=
operator|new
name|TestThread
argument_list|(
name|client
argument_list|,
literal|false
argument_list|,
name|results
argument_list|)
decl_stmt|;
name|TestThread
name|th3
init|=
operator|new
name|TestThread
argument_list|(
name|client
argument_list|,
literal|false
argument_list|,
name|results
argument_list|)
decl_stmt|;
name|th1
operator|.
name|start
parameter_list|()
constructor_decl|;
name|Thread
operator|.
name|sleep
argument_list|(
literal|100
argument_list|)
expr_stmt|;
name|th2
operator|.
name|start
parameter_list|()
constructor_decl|;
name|Thread
operator|.
name|sleep
argument_list|(
literal|200
argument_list|)
expr_stmt|;
name|th3
operator|.
name|start
parameter_list|()
constructor_decl|;
name|th1
operator|.
name|join
parameter_list|()
constructor_decl|;
name|th2
operator|.
name|join
parameter_list|()
constructor_decl|;
name|th3
operator|.
name|join
parameter_list|()
constructor_decl|;
name|assertEquals
argument_list|(
name|UNDELAYED
argument_list|,
name|results
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|intValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|UNDELAYED
argument_list|,
name|results
operator|.
name|get
argument_list|(
literal|1
argument_list|)
operator|.
name|intValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|results
operator|.
name|get
argument_list|(
literal|2
argument_list|)
operator|.
name|intValue
argument_list|()
argument_list|,
name|delayReturnValue
condition|?
name|DELAYED
else|:
literal|0xDEADBEEF
argument_list|)
expr_stmt|;
block|}
end_class

begin_class
specifier|private
specifier|static
class|class
name|ListAppender
extends|extends
name|AppenderSkeleton
block|{
specifier|private
name|List
argument_list|<
name|String
argument_list|>
name|messages
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
annotation|@
name|Override
specifier|protected
name|void
name|append
parameter_list|(
name|LoggingEvent
name|event
parameter_list|)
block|{
name|messages
operator|.
name|add
argument_list|(
name|event
operator|.
name|getMessage
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|()
block|{     }
annotation|@
name|Override
specifier|public
name|boolean
name|requiresLayout
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|getMessages
parameter_list|()
block|{
return|return
name|messages
return|;
block|}
block|}
end_class

begin_function
annotation|@
name|Test
specifier|public
name|void
name|testTooManyDelayedRpcs
parameter_list|()
throws|throws
name|Exception
block|{
name|Configuration
name|conf
init|=
name|HBaseConfiguration
operator|.
name|create
argument_list|()
decl_stmt|;
specifier|final
name|int
name|MAX_DELAYED_RPC
init|=
literal|10
decl_stmt|;
name|conf
operator|.
name|setInt
argument_list|(
literal|"hbase.ipc.warn.delayedrpc.number"
argument_list|,
name|MAX_DELAYED_RPC
argument_list|)
expr_stmt|;
name|ListAppender
name|listAppender
init|=
operator|new
name|ListAppender
argument_list|()
decl_stmt|;
name|Logger
name|log
init|=
name|Logger
operator|.
name|getLogger
argument_list|(
literal|"org.apache.hadoop.ipc.HBaseServer"
argument_list|)
decl_stmt|;
name|log
operator|.
name|addAppender
argument_list|(
name|listAppender
argument_list|)
expr_stmt|;
name|log
operator|.
name|setLevel
argument_list|(
name|Level
operator|.
name|WARN
argument_list|)
expr_stmt|;
name|InetSocketAddress
name|isa
init|=
operator|new
name|InetSocketAddress
argument_list|(
literal|"localhost"
argument_list|,
literal|0
argument_list|)
decl_stmt|;
name|rpcServer
operator|=
name|HBaseRPC
operator|.
name|getServer
argument_list|(
operator|new
name|TestRpcImpl
argument_list|(
literal|true
argument_list|)
argument_list|,
operator|new
name|Class
argument_list|<
name|?
argument_list|>
index|[]
block|{
name|TestRpcImpl
operator|.
name|class
block|}
operator|,
name|isa
operator|.
name|getHostName
argument_list|()
operator|,
name|isa
operator|.
name|getPort
argument_list|()
operator|,
literal|1
operator|,
literal|0
operator|,
literal|true
operator|,
name|conf
operator|,
literal|0
block|)
function|;
end_function

begin_expr_stmt
name|rpcServer
operator|.
name|start
argument_list|()
expr_stmt|;
end_expr_stmt

begin_decl_stmt
name|TestRpc
name|client
init|=
operator|(
name|TestRpc
operator|)
name|HBaseRPC
operator|.
name|getProxy
argument_list|(
name|TestRpc
operator|.
name|class
argument_list|,
literal|0
argument_list|,
name|rpcServer
operator|.
name|getListenerAddress
argument_list|()
argument_list|,
name|conf
argument_list|,
literal|1000
argument_list|)
decl_stmt|;
end_decl_stmt

begin_decl_stmt
name|Thread
name|threads
index|[]
init|=
operator|new
name|Thread
index|[
name|MAX_DELAYED_RPC
operator|+
literal|1
index|]
decl_stmt|;
end_decl_stmt

begin_for
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|MAX_DELAYED_RPC
condition|;
name|i
operator|++
control|)
block|{
name|threads
index|[
name|i
index|]
operator|=
operator|new
name|TestThread
argument_list|(
name|client
argument_list|,
literal|true
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|threads
index|[
name|i
index|]
operator|.
name|start
argument_list|()
expr_stmt|;
block|}
end_for

begin_comment
comment|/* No warnings till here. */
end_comment

begin_expr_stmt
name|assertTrue
argument_list|(
name|listAppender
operator|.
name|getMessages
argument_list|()
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
end_expr_stmt

begin_comment
comment|/* This should give a warning. */
end_comment

begin_expr_stmt
name|threads
index|[
name|MAX_DELAYED_RPC
index|]
operator|=
operator|new
name|TestThread
argument_list|(
name|client
argument_list|,
literal|true
argument_list|,
literal|null
argument_list|)
expr_stmt|;
end_expr_stmt

begin_expr_stmt
name|threads
index|[
name|MAX_DELAYED_RPC
index|]
operator|.
name|start
argument_list|()
expr_stmt|;
end_expr_stmt

begin_for
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|MAX_DELAYED_RPC
condition|;
name|i
operator|++
control|)
block|{
name|threads
index|[
name|i
index|]
operator|.
name|join
argument_list|()
expr_stmt|;
block|}
end_for

begin_expr_stmt
name|assertFalse
argument_list|(
name|listAppender
operator|.
name|getMessages
argument_list|()
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
end_expr_stmt

begin_expr_stmt
name|assertTrue
argument_list|(
name|listAppender
operator|.
name|getMessages
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|startsWith
argument_list|(
literal|"Too many delayed calls"
argument_list|)
argument_list|)
expr_stmt|;
end_expr_stmt

begin_expr_stmt
name|log
operator|.
name|removeAppender
argument_list|(
name|listAppender
argument_list|)
expr_stmt|;
end_expr_stmt

begin_interface
unit|}    public
interface|interface
name|TestRpc
extends|extends
name|VersionedProtocol
block|{
specifier|public
specifier|static
specifier|final
name|long
name|VERSION
init|=
literal|1L
decl_stmt|;
name|TestResponse
name|test
parameter_list|(
name|TestArg
name|delay
parameter_list|)
function_decl|;
block|}
end_interface

begin_class
specifier|private
specifier|static
class|class
name|TestRpcImpl
implements|implements
name|TestRpc
block|{
comment|/**      * Should the return value of delayed call be set at the end of the delay      * or at call return.      */
specifier|private
name|boolean
name|delayReturnValue
decl_stmt|;
comment|/**      * @param delayReturnValue Should the response to the delayed call be set      * at the start or the end of the delay.      * @param delay Amount of milliseconds to delay the call by      */
specifier|public
name|TestRpcImpl
parameter_list|(
name|boolean
name|delayReturnValue
parameter_list|)
block|{
name|this
operator|.
name|delayReturnValue
operator|=
name|delayReturnValue
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|TestResponse
name|test
parameter_list|(
specifier|final
name|TestArg
name|testArg
parameter_list|)
block|{
name|boolean
name|delay
init|=
name|testArg
operator|.
name|getDelay
argument_list|()
decl_stmt|;
name|TestResponse
operator|.
name|Builder
name|responseBuilder
init|=
name|TestResponse
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|delay
condition|)
block|{
name|responseBuilder
operator|.
name|setResponse
argument_list|(
name|UNDELAYED
argument_list|)
expr_stmt|;
return|return
name|responseBuilder
operator|.
name|build
argument_list|()
return|;
block|}
specifier|final
name|Delayable
name|call
init|=
name|HBaseServer
operator|.
name|getCurrentCall
argument_list|()
decl_stmt|;
name|call
operator|.
name|startDelay
argument_list|(
name|delayReturnValue
argument_list|)
expr_stmt|;
operator|new
name|Thread
argument_list|()
block|{
specifier|public
name|void
name|run
parameter_list|()
block|{
try|try
block|{
name|Thread
operator|.
name|sleep
argument_list|(
literal|500
argument_list|)
expr_stmt|;
name|TestResponse
operator|.
name|Builder
name|responseBuilder
init|=
name|TestResponse
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
name|call
operator|.
name|endDelay
argument_list|(
name|delayReturnValue
condition|?
name|responseBuilder
operator|.
name|setResponse
argument_list|(
name|DELAYED
argument_list|)
operator|.
name|build
argument_list|()
else|:
literal|null
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
block|}
block|}
block|}
operator|.
name|start
argument_list|()
expr_stmt|;
comment|// This value should go back to client only if the response is set
comment|// immediately at delay time.
name|responseBuilder
operator|.
name|setResponse
argument_list|(
literal|0xDEADBEEF
argument_list|)
expr_stmt|;
return|return
name|responseBuilder
operator|.
name|build
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getProtocolVersion
parameter_list|(
name|String
name|arg0
parameter_list|,
name|long
name|arg1
parameter_list|)
throws|throws
name|IOException
block|{
return|return
literal|0
return|;
block|}
annotation|@
name|Override
specifier|public
name|ProtocolSignature
name|getProtocolSignature
parameter_list|(
name|String
name|protocol
parameter_list|,
name|long
name|clientVersion
parameter_list|,
name|int
name|clientMethodsHash
parameter_list|)
throws|throws
name|IOException
block|{
name|Method
index|[]
name|methods
init|=
name|this
operator|.
name|getClass
argument_list|()
operator|.
name|getMethods
argument_list|()
decl_stmt|;
name|int
index|[]
name|hashes
init|=
operator|new
name|int
index|[
name|methods
operator|.
name|length
index|]
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
name|methods
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|hashes
index|[
name|i
index|]
operator|=
name|methods
index|[
name|i
index|]
operator|.
name|hashCode
argument_list|()
expr_stmt|;
block|}
return|return
operator|new
name|ProtocolSignature
argument_list|(
name|clientVersion
argument_list|,
name|hashes
argument_list|)
return|;
block|}
block|}
end_class

begin_class
specifier|private
specifier|static
class|class
name|TestThread
extends|extends
name|Thread
block|{
specifier|private
name|TestRpc
name|server
decl_stmt|;
specifier|private
name|boolean
name|delay
decl_stmt|;
specifier|private
name|List
argument_list|<
name|Integer
argument_list|>
name|results
decl_stmt|;
specifier|public
name|TestThread
parameter_list|(
name|TestRpc
name|server
parameter_list|,
name|boolean
name|delay
parameter_list|,
name|List
argument_list|<
name|Integer
argument_list|>
name|results
parameter_list|)
block|{
name|this
operator|.
name|server
operator|=
name|server
expr_stmt|;
name|this
operator|.
name|delay
operator|=
name|delay
expr_stmt|;
name|this
operator|.
name|results
operator|=
name|results
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
try|try
block|{
name|Integer
name|result
init|=
operator|new
name|Integer
argument_list|(
name|server
operator|.
name|test
argument_list|(
name|TestArg
operator|.
name|newBuilder
argument_list|()
operator|.
name|setDelay
argument_list|(
name|delay
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
operator|.
name|getResponse
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|results
operator|!=
literal|null
condition|)
block|{
synchronized|synchronized
init|(
name|results
init|)
block|{
name|results
operator|.
name|add
argument_list|(
name|result
argument_list|)
expr_stmt|;
block|}
block|}
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|fail
argument_list|(
literal|"Unexpected exception: "
operator|+
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

begin_function
annotation|@
name|Test
specifier|public
name|void
name|testEndDelayThrowing
parameter_list|()
throws|throws
name|IOException
block|{
name|Configuration
name|conf
init|=
name|HBaseConfiguration
operator|.
name|create
argument_list|()
decl_stmt|;
name|InetSocketAddress
name|isa
init|=
operator|new
name|InetSocketAddress
argument_list|(
literal|"localhost"
argument_list|,
literal|0
argument_list|)
decl_stmt|;
name|rpcServer
operator|=
name|HBaseRPC
operator|.
name|getServer
argument_list|(
operator|new
name|FaultyTestRpc
argument_list|()
argument_list|,
operator|new
name|Class
argument_list|<
name|?
argument_list|>
index|[]
block|{
name|TestRpcImpl
operator|.
name|class
block|}
operator|,
name|isa
operator|.
name|getHostName
argument_list|()
operator|,
name|isa
operator|.
name|getPort
argument_list|()
operator|,
literal|1
operator|,
literal|0
operator|,
literal|true
operator|,
name|conf
operator|,
literal|0
block|)
function|;
end_function

begin_expr_stmt
name|rpcServer
operator|.
name|start
argument_list|()
expr_stmt|;
end_expr_stmt

begin_decl_stmt
name|TestRpc
name|client
init|=
operator|(
name|TestRpc
operator|)
name|HBaseRPC
operator|.
name|getProxy
argument_list|(
name|TestRpc
operator|.
name|class
argument_list|,
literal|0
argument_list|,
name|rpcServer
operator|.
name|getListenerAddress
argument_list|()
argument_list|,
name|conf
argument_list|,
literal|1000
argument_list|)
decl_stmt|;
end_decl_stmt

begin_decl_stmt
name|int
name|result
init|=
literal|0xDEADBEEF
decl_stmt|;
end_decl_stmt

begin_try
try|try
block|{
name|result
operator|=
name|client
operator|.
name|test
argument_list|(
name|TestArg
operator|.
name|newBuilder
argument_list|()
operator|.
name|setDelay
argument_list|(
literal|false
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
operator|.
name|getResponse
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|fail
argument_list|(
literal|"No exception should have been thrown."
argument_list|)
expr_stmt|;
block|}
end_try

begin_expr_stmt
name|assertEquals
argument_list|(
name|result
argument_list|,
name|UNDELAYED
argument_list|)
expr_stmt|;
end_expr_stmt

begin_decl_stmt
name|boolean
name|caughtException
init|=
literal|false
decl_stmt|;
end_decl_stmt

begin_try
try|try
block|{
name|result
operator|=
name|client
operator|.
name|test
argument_list|(
name|TestArg
operator|.
name|newBuilder
argument_list|()
operator|.
name|setDelay
argument_list|(
literal|true
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
operator|.
name|getResponse
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
comment|// Exception thrown by server is enclosed in a RemoteException.
if|if
condition|(
name|e
operator|.
name|getCause
argument_list|()
operator|.
name|getMessage
argument_list|()
operator|.
name|contains
argument_list|(
literal|"java.lang.Exception: Something went wrong"
argument_list|)
condition|)
name|caughtException
operator|=
literal|true
expr_stmt|;
name|Log
operator|.
name|warn
argument_list|(
name|e
argument_list|)
expr_stmt|;
block|}
end_try

begin_expr_stmt
name|assertTrue
argument_list|(
name|caughtException
argument_list|)
expr_stmt|;
end_expr_stmt

begin_comment
unit|}
comment|/**    * Delayed calls to this class throw an exception.    */
end_comment

begin_class
unit|private
specifier|static
class|class
name|FaultyTestRpc
implements|implements
name|TestRpc
block|{
annotation|@
name|Override
specifier|public
name|TestResponse
name|test
parameter_list|(
name|TestArg
name|arg
parameter_list|)
block|{
if|if
condition|(
operator|!
name|arg
operator|.
name|getDelay
argument_list|()
condition|)
return|return
name|TestResponse
operator|.
name|newBuilder
argument_list|()
operator|.
name|setResponse
argument_list|(
name|UNDELAYED
argument_list|)
operator|.
name|build
argument_list|()
return|;
name|Delayable
name|call
init|=
name|HBaseServer
operator|.
name|getCurrentCall
argument_list|()
decl_stmt|;
name|call
operator|.
name|startDelay
argument_list|(
literal|true
argument_list|)
expr_stmt|;
try|try
block|{
name|call
operator|.
name|endDelayThrowing
argument_list|(
operator|new
name|Exception
argument_list|(
literal|"Something went wrong"
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
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
block|}
comment|// Client will receive the Exception, not this value.
return|return
name|TestResponse
operator|.
name|newBuilder
argument_list|()
operator|.
name|setResponse
argument_list|(
name|DELAYED
argument_list|)
operator|.
name|build
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getProtocolVersion
parameter_list|(
name|String
name|arg0
parameter_list|,
name|long
name|arg1
parameter_list|)
throws|throws
name|IOException
block|{
return|return
literal|0
return|;
block|}
annotation|@
name|Override
specifier|public
name|ProtocolSignature
name|getProtocolSignature
parameter_list|(
name|String
name|protocol
parameter_list|,
name|long
name|clientVersion
parameter_list|,
name|int
name|clientMethodsHash
parameter_list|)
throws|throws
name|IOException
block|{
return|return
operator|new
name|ProtocolSignature
argument_list|(
name|clientVersion
argument_list|,
operator|new
name|int
index|[]
block|{}
argument_list|)
return|;
block|}
block|}
end_class

begin_decl_stmt
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
end_decl_stmt

unit|}
end_unit

