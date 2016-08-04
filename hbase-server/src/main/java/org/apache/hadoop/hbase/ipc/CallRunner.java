begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
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

begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
end_comment

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
name|nio
operator|.
name|channels
operator|.
name|ClosedChannelException
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
name|CallDroppedException
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
name|CellScanner
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
name|HBaseInterfaceAudience
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
name|hbase
operator|.
name|classification
operator|.
name|InterfaceStability
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
name|RpcServer
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
name|monitoring
operator|.
name|MonitoredRPCHandler
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
name|Pair
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
name|apache
operator|.
name|hadoop
operator|.
name|util
operator|.
name|StringUtils
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|htrace
operator|.
name|Trace
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|htrace
operator|.
name|TraceScope
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|Message
import|;
end_import

begin_comment
comment|/**  * The request processing logic, which is usually executed in thread pools provided by an  * {@link RpcScheduler}.  Call {@link #run()} to actually execute the contained  * RpcServer.Call  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|LimitedPrivate
argument_list|(
block|{
name|HBaseInterfaceAudience
operator|.
name|COPROC
block|,
name|HBaseInterfaceAudience
operator|.
name|PHOENIX
block|}
argument_list|)
annotation|@
name|InterfaceStability
operator|.
name|Evolving
specifier|public
class|class
name|CallRunner
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
name|CallRunner
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|CallDroppedException
name|CALL_DROPPED_EXCEPTION
init|=
operator|new
name|CallDroppedException
argument_list|()
decl_stmt|;
specifier|private
name|Call
name|call
decl_stmt|;
specifier|private
name|RpcServerInterface
name|rpcServer
decl_stmt|;
specifier|private
name|MonitoredRPCHandler
name|status
decl_stmt|;
specifier|private
specifier|volatile
name|boolean
name|sucessful
decl_stmt|;
comment|/**    * On construction, adds the size of this call to the running count of outstanding call sizes.    * Presumption is that we are put on a queue while we wait on an executor to run us.  During this    * time we occupy heap.    */
comment|// The constructor is shutdown so only RpcServer in this class can make one of these.
name|CallRunner
parameter_list|(
specifier|final
name|RpcServerInterface
name|rpcServer
parameter_list|,
specifier|final
name|Call
name|call
parameter_list|)
block|{
name|this
operator|.
name|call
operator|=
name|call
expr_stmt|;
name|this
operator|.
name|rpcServer
operator|=
name|rpcServer
expr_stmt|;
comment|// Add size of the call to queue size.
if|if
condition|(
name|call
operator|!=
literal|null
operator|&&
name|rpcServer
operator|!=
literal|null
condition|)
block|{
name|this
operator|.
name|rpcServer
operator|.
name|addCallSize
argument_list|(
name|call
operator|.
name|getSize
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|Call
name|getCall
parameter_list|()
block|{
return|return
name|call
return|;
block|}
specifier|public
name|void
name|setStatus
parameter_list|(
name|MonitoredRPCHandler
name|status
parameter_list|)
block|{
name|this
operator|.
name|status
operator|=
name|status
expr_stmt|;
block|}
comment|/**    * Cleanup after ourselves... let go of references.    */
specifier|private
name|void
name|cleanup
parameter_list|()
block|{
name|this
operator|.
name|call
operator|=
literal|null
expr_stmt|;
name|this
operator|.
name|rpcServer
operator|=
literal|null
expr_stmt|;
block|}
specifier|public
name|void
name|run
parameter_list|()
block|{
try|try
block|{
if|if
condition|(
operator|!
name|call
operator|.
name|connection
operator|.
name|channel
operator|.
name|isOpen
argument_list|()
condition|)
block|{
if|if
condition|(
name|RpcServer
operator|.
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|RpcServer
operator|.
name|LOG
operator|.
name|debug
argument_list|(
name|Thread
operator|.
name|currentThread
argument_list|()
operator|.
name|getName
argument_list|()
operator|+
literal|": skipped "
operator|+
name|call
argument_list|)
expr_stmt|;
block|}
return|return;
block|}
name|call
operator|.
name|startTime
operator|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
expr_stmt|;
if|if
condition|(
name|call
operator|.
name|startTime
operator|>
name|call
operator|.
name|deadline
condition|)
block|{
name|RpcServer
operator|.
name|LOG
operator|.
name|info
argument_list|(
literal|"Dropping timed out call: "
operator|+
name|call
argument_list|)
expr_stmt|;
return|return;
block|}
name|this
operator|.
name|status
operator|.
name|setStatus
argument_list|(
literal|"Setting up call"
argument_list|)
expr_stmt|;
name|this
operator|.
name|status
operator|.
name|setConnection
argument_list|(
name|call
operator|.
name|connection
operator|.
name|getHostAddress
argument_list|()
argument_list|,
name|call
operator|.
name|connection
operator|.
name|getRemotePort
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|RpcServer
operator|.
name|LOG
operator|.
name|isTraceEnabled
argument_list|()
condition|)
block|{
name|UserGroupInformation
name|remoteUser
init|=
name|call
operator|.
name|connection
operator|.
name|ugi
decl_stmt|;
name|RpcServer
operator|.
name|LOG
operator|.
name|trace
argument_list|(
name|call
operator|.
name|toShortString
argument_list|()
operator|+
literal|" executing as "
operator|+
operator|(
operator|(
name|remoteUser
operator|==
literal|null
operator|)
condition|?
literal|"NULL principal"
else|:
name|remoteUser
operator|.
name|getUserName
argument_list|()
operator|)
argument_list|)
expr_stmt|;
block|}
name|Throwable
name|errorThrowable
init|=
literal|null
decl_stmt|;
name|String
name|error
init|=
literal|null
decl_stmt|;
name|Pair
argument_list|<
name|Message
argument_list|,
name|CellScanner
argument_list|>
name|resultPair
init|=
literal|null
decl_stmt|;
name|RpcServer
operator|.
name|CurCall
operator|.
name|set
argument_list|(
name|call
argument_list|)
expr_stmt|;
name|TraceScope
name|traceScope
init|=
literal|null
decl_stmt|;
try|try
block|{
if|if
condition|(
operator|!
name|this
operator|.
name|rpcServer
operator|.
name|isStarted
argument_list|()
condition|)
block|{
name|InetSocketAddress
name|address
init|=
name|rpcServer
operator|.
name|getListenerAddress
argument_list|()
decl_stmt|;
throw|throw
operator|new
name|ServerNotRunningYetException
argument_list|(
literal|"Server "
operator|+
operator|(
name|address
operator|!=
literal|null
condition|?
name|address
else|:
literal|"(channel closed)"
operator|)
operator|+
literal|" is not running yet"
argument_list|)
throw|;
block|}
if|if
condition|(
name|call
operator|.
name|tinfo
operator|!=
literal|null
condition|)
block|{
name|traceScope
operator|=
name|Trace
operator|.
name|startSpan
argument_list|(
name|call
operator|.
name|toTraceString
argument_list|()
argument_list|,
name|call
operator|.
name|tinfo
argument_list|)
expr_stmt|;
block|}
comment|// make the call
name|resultPair
operator|=
name|this
operator|.
name|rpcServer
operator|.
name|call
argument_list|(
name|call
operator|.
name|service
argument_list|,
name|call
operator|.
name|md
argument_list|,
name|call
operator|.
name|param
argument_list|,
name|call
operator|.
name|cellScanner
argument_list|,
name|call
operator|.
name|timestamp
argument_list|,
name|this
operator|.
name|status
argument_list|,
name|call
operator|.
name|startTime
argument_list|,
name|call
operator|.
name|timeout
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|e
parameter_list|)
block|{
name|RpcServer
operator|.
name|LOG
operator|.
name|debug
argument_list|(
name|Thread
operator|.
name|currentThread
argument_list|()
operator|.
name|getName
argument_list|()
operator|+
literal|": "
operator|+
name|call
operator|.
name|toShortString
argument_list|()
argument_list|,
name|e
argument_list|)
expr_stmt|;
name|errorThrowable
operator|=
name|e
expr_stmt|;
name|error
operator|=
name|StringUtils
operator|.
name|stringifyException
argument_list|(
name|e
argument_list|)
expr_stmt|;
if|if
condition|(
name|e
operator|instanceof
name|Error
condition|)
block|{
throw|throw
operator|(
name|Error
operator|)
name|e
throw|;
block|}
block|}
finally|finally
block|{
if|if
condition|(
name|traceScope
operator|!=
literal|null
condition|)
block|{
name|traceScope
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
name|RpcServer
operator|.
name|CurCall
operator|.
name|set
argument_list|(
literal|null
argument_list|)
expr_stmt|;
if|if
condition|(
name|resultPair
operator|!=
literal|null
condition|)
block|{
name|this
operator|.
name|rpcServer
operator|.
name|addCallSize
argument_list|(
name|call
operator|.
name|getSize
argument_list|()
operator|*
operator|-
literal|1
argument_list|)
expr_stmt|;
name|sucessful
operator|=
literal|true
expr_stmt|;
block|}
block|}
comment|// Set the response
name|Message
name|param
init|=
name|resultPair
operator|!=
literal|null
condition|?
name|resultPair
operator|.
name|getFirst
argument_list|()
else|:
literal|null
decl_stmt|;
name|CellScanner
name|cells
init|=
name|resultPair
operator|!=
literal|null
condition|?
name|resultPair
operator|.
name|getSecond
argument_list|()
else|:
literal|null
decl_stmt|;
name|call
operator|.
name|setResponse
argument_list|(
name|param
argument_list|,
name|cells
argument_list|,
name|errorThrowable
argument_list|,
name|error
argument_list|)
expr_stmt|;
name|call
operator|.
name|sendResponseIfReady
argument_list|()
expr_stmt|;
name|this
operator|.
name|status
operator|.
name|markComplete
argument_list|(
literal|"Sent response"
argument_list|)
expr_stmt|;
name|this
operator|.
name|status
operator|.
name|pause
argument_list|(
literal|"Waiting for a call"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|OutOfMemoryError
name|e
parameter_list|)
block|{
if|if
condition|(
name|this
operator|.
name|rpcServer
operator|.
name|getErrorHandler
argument_list|()
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|this
operator|.
name|rpcServer
operator|.
name|getErrorHandler
argument_list|()
operator|.
name|checkOOME
argument_list|(
name|e
argument_list|)
condition|)
block|{
name|RpcServer
operator|.
name|LOG
operator|.
name|info
argument_list|(
name|Thread
operator|.
name|currentThread
argument_list|()
operator|.
name|getName
argument_list|()
operator|+
literal|": exiting on OutOfMemoryError"
argument_list|)
expr_stmt|;
return|return;
block|}
block|}
else|else
block|{
comment|// rethrow if no handler
throw|throw
name|e
throw|;
block|}
block|}
catch|catch
parameter_list|(
name|ClosedChannelException
name|cce
parameter_list|)
block|{
name|InetSocketAddress
name|address
init|=
name|rpcServer
operator|.
name|getListenerAddress
argument_list|()
decl_stmt|;
name|RpcServer
operator|.
name|LOG
operator|.
name|warn
argument_list|(
name|Thread
operator|.
name|currentThread
argument_list|()
operator|.
name|getName
argument_list|()
operator|+
literal|": caught a ClosedChannelException, "
operator|+
literal|"this means that the server "
operator|+
operator|(
name|address
operator|!=
literal|null
condition|?
name|address
else|:
literal|"(channel closed)"
operator|)
operator|+
literal|" was processing a request but the client went away. The error message was: "
operator|+
name|cce
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|RpcServer
operator|.
name|LOG
operator|.
name|warn
argument_list|(
name|Thread
operator|.
name|currentThread
argument_list|()
operator|.
name|getName
argument_list|()
operator|+
literal|": caught: "
operator|+
name|StringUtils
operator|.
name|stringifyException
argument_list|(
name|e
argument_list|)
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
if|if
condition|(
operator|!
name|sucessful
condition|)
block|{
name|this
operator|.
name|rpcServer
operator|.
name|addCallSize
argument_list|(
name|call
operator|.
name|getSize
argument_list|()
operator|*
operator|-
literal|1
argument_list|)
expr_stmt|;
block|}
name|cleanup
argument_list|()
expr_stmt|;
block|}
block|}
comment|/**    * When we want to drop this call because of server is overloaded.    */
specifier|public
name|void
name|drop
parameter_list|()
block|{
try|try
block|{
if|if
condition|(
operator|!
name|call
operator|.
name|connection
operator|.
name|channel
operator|.
name|isOpen
argument_list|()
condition|)
block|{
if|if
condition|(
name|RpcServer
operator|.
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|RpcServer
operator|.
name|LOG
operator|.
name|debug
argument_list|(
name|Thread
operator|.
name|currentThread
argument_list|()
operator|.
name|getName
argument_list|()
operator|+
literal|": skipped "
operator|+
name|call
argument_list|)
expr_stmt|;
block|}
return|return;
block|}
comment|// Set the response
name|InetSocketAddress
name|address
init|=
name|rpcServer
operator|.
name|getListenerAddress
argument_list|()
decl_stmt|;
name|call
operator|.
name|setResponse
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|,
name|CALL_DROPPED_EXCEPTION
argument_list|,
literal|"Call dropped, server "
operator|+
operator|(
name|address
operator|!=
literal|null
condition|?
name|address
else|:
literal|"(channel closed)"
operator|)
operator|+
literal|" is overloaded, please retry."
argument_list|)
expr_stmt|;
name|call
operator|.
name|sendResponseIfReady
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ClosedChannelException
name|cce
parameter_list|)
block|{
name|InetSocketAddress
name|address
init|=
name|rpcServer
operator|.
name|getListenerAddress
argument_list|()
decl_stmt|;
name|RpcServer
operator|.
name|LOG
operator|.
name|warn
argument_list|(
name|Thread
operator|.
name|currentThread
argument_list|()
operator|.
name|getName
argument_list|()
operator|+
literal|": caught a ClosedChannelException, "
operator|+
literal|"this means that the server "
operator|+
operator|(
name|address
operator|!=
literal|null
condition|?
name|address
else|:
literal|"(channel closed)"
operator|)
operator|+
literal|" was processing a request but the client went away. The error message was: "
operator|+
name|cce
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|RpcServer
operator|.
name|LOG
operator|.
name|warn
argument_list|(
name|Thread
operator|.
name|currentThread
argument_list|()
operator|.
name|getName
argument_list|()
operator|+
literal|": caught: "
operator|+
name|StringUtils
operator|.
name|stringifyException
argument_list|(
name|e
argument_list|)
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
if|if
condition|(
operator|!
name|sucessful
condition|)
block|{
name|this
operator|.
name|rpcServer
operator|.
name|addCallSize
argument_list|(
name|call
operator|.
name|getSize
argument_list|()
operator|*
operator|-
literal|1
argument_list|)
expr_stmt|;
block|}
name|cleanup
argument_list|()
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

