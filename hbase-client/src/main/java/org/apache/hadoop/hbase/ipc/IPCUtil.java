begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|shaded
operator|.
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|CodedOutputStream
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
name|shaded
operator|.
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|Message
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
name|io
operator|.
name|OutputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|ConnectException
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
name|SocketTimeoutException
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
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|DoNotRetryIOException
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
name|exceptions
operator|.
name|ConnectionClosingException
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
name|shaded
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|RPCProtos
operator|.
name|CellBlockMeta
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
name|shaded
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|RPCProtos
operator|.
name|ExceptionResponse
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
name|shaded
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|RPCProtos
operator|.
name|RequestHeader
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
name|shaded
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|TracingProtos
operator|.
name|RPCTInfo
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
name|Bytes
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
name|EnvironmentEdgeManager
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
name|ipc
operator|.
name|RemoteException
import|;
end_import

begin_comment
comment|/**  * Utility to help ipc'ing.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
class|class
name|IPCUtil
block|{
comment|/**    * Write out header, param, and cell block if there is one.    * @param dos Stream to write into    * @param header to write    * @param param to write    * @param cellBlock to write    * @return Total number of bytes written.    * @throws IOException if write action fails    */
specifier|public
specifier|static
name|int
name|write
parameter_list|(
specifier|final
name|OutputStream
name|dos
parameter_list|,
specifier|final
name|Message
name|header
parameter_list|,
specifier|final
name|Message
name|param
parameter_list|,
specifier|final
name|ByteBuffer
name|cellBlock
parameter_list|)
throws|throws
name|IOException
block|{
comment|// Must calculate total size and write that first so other side can read it all in in one
comment|// swoop. This is dictated by how the server is currently written. Server needs to change
comment|// if we are to be able to write without the length prefixing.
name|int
name|totalSize
init|=
name|IPCUtil
operator|.
name|getTotalSizeWhenWrittenDelimited
argument_list|(
name|header
argument_list|,
name|param
argument_list|)
decl_stmt|;
if|if
condition|(
name|cellBlock
operator|!=
literal|null
condition|)
block|{
name|totalSize
operator|+=
name|cellBlock
operator|.
name|remaining
argument_list|()
expr_stmt|;
block|}
return|return
name|write
argument_list|(
name|dos
argument_list|,
name|header
argument_list|,
name|param
argument_list|,
name|cellBlock
argument_list|,
name|totalSize
argument_list|)
return|;
block|}
specifier|private
specifier|static
name|int
name|write
parameter_list|(
specifier|final
name|OutputStream
name|dos
parameter_list|,
specifier|final
name|Message
name|header
parameter_list|,
specifier|final
name|Message
name|param
parameter_list|,
specifier|final
name|ByteBuffer
name|cellBlock
parameter_list|,
specifier|final
name|int
name|totalSize
parameter_list|)
throws|throws
name|IOException
block|{
comment|// I confirmed toBytes does same as DataOutputStream#writeInt.
name|dos
operator|.
name|write
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|totalSize
argument_list|)
argument_list|)
expr_stmt|;
comment|// This allocates a buffer that is the size of the message internally.
name|header
operator|.
name|writeDelimitedTo
argument_list|(
name|dos
argument_list|)
expr_stmt|;
if|if
condition|(
name|param
operator|!=
literal|null
condition|)
block|{
name|param
operator|.
name|writeDelimitedTo
argument_list|(
name|dos
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|cellBlock
operator|!=
literal|null
condition|)
block|{
name|dos
operator|.
name|write
argument_list|(
name|cellBlock
operator|.
name|array
argument_list|()
argument_list|,
literal|0
argument_list|,
name|cellBlock
operator|.
name|remaining
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|dos
operator|.
name|flush
argument_list|()
expr_stmt|;
return|return
name|totalSize
return|;
block|}
comment|/**    * @return Size on the wire when the two messages are written with writeDelimitedTo    */
specifier|public
specifier|static
name|int
name|getTotalSizeWhenWrittenDelimited
parameter_list|(
name|Message
modifier|...
name|messages
parameter_list|)
block|{
name|int
name|totalSize
init|=
literal|0
decl_stmt|;
for|for
control|(
name|Message
name|m
range|:
name|messages
control|)
block|{
if|if
condition|(
name|m
operator|==
literal|null
condition|)
block|{
continue|continue;
block|}
name|totalSize
operator|+=
name|m
operator|.
name|getSerializedSize
argument_list|()
expr_stmt|;
name|totalSize
operator|+=
name|CodedOutputStream
operator|.
name|computeRawVarint32Size
argument_list|(
name|m
operator|.
name|getSerializedSize
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|Preconditions
operator|.
name|checkArgument
argument_list|(
name|totalSize
operator|<
name|Integer
operator|.
name|MAX_VALUE
argument_list|)
expr_stmt|;
return|return
name|totalSize
return|;
block|}
specifier|static
name|RequestHeader
name|buildRequestHeader
parameter_list|(
name|Call
name|call
parameter_list|,
name|CellBlockMeta
name|cellBlockMeta
parameter_list|)
block|{
name|RequestHeader
operator|.
name|Builder
name|builder
init|=
name|RequestHeader
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
name|builder
operator|.
name|setCallId
argument_list|(
name|call
operator|.
name|id
argument_list|)
expr_stmt|;
if|if
condition|(
name|call
operator|.
name|span
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|setTraceInfo
argument_list|(
name|RPCTInfo
operator|.
name|newBuilder
argument_list|()
operator|.
name|setParentId
argument_list|(
name|call
operator|.
name|span
operator|.
name|getSpanId
argument_list|()
argument_list|)
operator|.
name|setTraceId
argument_list|(
name|call
operator|.
name|span
operator|.
name|getTraceId
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|setMethodName
argument_list|(
name|call
operator|.
name|md
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|setRequestParam
argument_list|(
name|call
operator|.
name|param
operator|!=
literal|null
argument_list|)
expr_stmt|;
if|if
condition|(
name|cellBlockMeta
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|setCellBlockMeta
argument_list|(
name|cellBlockMeta
argument_list|)
expr_stmt|;
block|}
comment|// Only pass priority if there is one set.
if|if
condition|(
name|call
operator|.
name|priority
operator|!=
name|HBaseRpcController
operator|.
name|PRIORITY_UNSET
condition|)
block|{
name|builder
operator|.
name|setPriority
argument_list|(
name|call
operator|.
name|priority
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|setTimeout
argument_list|(
name|call
operator|.
name|timeout
argument_list|)
expr_stmt|;
return|return
name|builder
operator|.
name|build
argument_list|()
return|;
block|}
comment|/**    * @param e exception to be wrapped    * @return RemoteException made from passed<code>e</code>    */
specifier|static
name|RemoteException
name|createRemoteException
parameter_list|(
specifier|final
name|ExceptionResponse
name|e
parameter_list|)
block|{
name|String
name|innerExceptionClassName
init|=
name|e
operator|.
name|getExceptionClassName
argument_list|()
decl_stmt|;
name|boolean
name|doNotRetry
init|=
name|e
operator|.
name|getDoNotRetry
argument_list|()
decl_stmt|;
return|return
name|e
operator|.
name|hasHostname
argument_list|()
condition|?
comment|// If a hostname then add it to the RemoteWithExtrasException
operator|new
name|RemoteWithExtrasException
argument_list|(
name|innerExceptionClassName
argument_list|,
name|e
operator|.
name|getStackTrace
argument_list|()
argument_list|,
name|e
operator|.
name|getHostname
argument_list|()
argument_list|,
name|e
operator|.
name|getPort
argument_list|()
argument_list|,
name|doNotRetry
argument_list|)
else|:
operator|new
name|RemoteWithExtrasException
argument_list|(
name|innerExceptionClassName
argument_list|,
name|e
operator|.
name|getStackTrace
argument_list|()
argument_list|,
name|doNotRetry
argument_list|)
return|;
block|}
comment|/**    * @return True if the exception is a fatal connection exception.    */
specifier|static
name|boolean
name|isFatalConnectionException
parameter_list|(
specifier|final
name|ExceptionResponse
name|e
parameter_list|)
block|{
return|return
name|e
operator|.
name|getExceptionClassName
argument_list|()
operator|.
name|equals
argument_list|(
name|FatalConnectionException
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
return|;
block|}
specifier|static
name|IOException
name|toIOE
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
if|if
condition|(
name|t
operator|instanceof
name|IOException
condition|)
block|{
return|return
operator|(
name|IOException
operator|)
name|t
return|;
block|}
else|else
block|{
return|return
operator|new
name|IOException
argument_list|(
name|t
argument_list|)
return|;
block|}
block|}
comment|/**    * Takes an Exception and the address we were trying to connect to and return an IOException with    * the input exception as the cause. The new exception provides the stack trace of the place where    * the exception is thrown and some extra diagnostics information. If the exception is    * ConnectException or SocketTimeoutException, return a new one of the same type; Otherwise return    * an IOException.    * @param addr target address    * @param exception the relevant exception    * @return an exception to throw    */
specifier|static
name|IOException
name|wrapException
parameter_list|(
name|InetSocketAddress
name|addr
parameter_list|,
name|Exception
name|exception
parameter_list|)
block|{
if|if
condition|(
name|exception
operator|instanceof
name|ConnectException
condition|)
block|{
comment|// connection refused; include the host:port in the error
return|return
operator|(
name|ConnectException
operator|)
operator|new
name|ConnectException
argument_list|(
literal|"Call to "
operator|+
name|addr
operator|+
literal|" failed on connection exception: "
operator|+
name|exception
argument_list|)
operator|.
name|initCause
argument_list|(
name|exception
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
name|exception
operator|instanceof
name|SocketTimeoutException
condition|)
block|{
return|return
operator|(
name|SocketTimeoutException
operator|)
operator|new
name|SocketTimeoutException
argument_list|(
literal|"Call to "
operator|+
name|addr
operator|+
literal|" failed because "
operator|+
name|exception
argument_list|)
operator|.
name|initCause
argument_list|(
name|exception
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
name|exception
operator|instanceof
name|ConnectionClosingException
condition|)
block|{
return|return
operator|(
name|ConnectionClosingException
operator|)
operator|new
name|ConnectionClosingException
argument_list|(
literal|"Call to "
operator|+
name|addr
operator|+
literal|" failed on local exception: "
operator|+
name|exception
argument_list|)
operator|.
name|initCause
argument_list|(
name|exception
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
name|exception
operator|instanceof
name|ServerTooBusyException
condition|)
block|{
comment|// we already have address in the exception message
return|return
operator|(
name|IOException
operator|)
name|exception
return|;
block|}
elseif|else
if|if
condition|(
name|exception
operator|instanceof
name|DoNotRetryIOException
condition|)
block|{
return|return
operator|(
name|IOException
operator|)
operator|new
name|DoNotRetryIOException
argument_list|(
literal|"Call to "
operator|+
name|addr
operator|+
literal|" failed on local exception: "
operator|+
name|exception
argument_list|)
operator|.
name|initCause
argument_list|(
name|exception
argument_list|)
return|;
block|}
else|else
block|{
return|return
operator|(
name|IOException
operator|)
operator|new
name|IOException
argument_list|(
literal|"Call to "
operator|+
name|addr
operator|+
literal|" failed on local exception: "
operator|+
name|exception
argument_list|)
operator|.
name|initCause
argument_list|(
name|exception
argument_list|)
return|;
block|}
block|}
specifier|static
name|void
name|setCancelled
parameter_list|(
name|Call
name|call
parameter_list|)
block|{
name|call
operator|.
name|setException
argument_list|(
operator|new
name|CallCancelledException
argument_list|(
literal|"Call id="
operator|+
name|call
operator|.
name|id
operator|+
literal|", waitTime="
operator|+
operator|(
name|EnvironmentEdgeManager
operator|.
name|currentTime
argument_list|()
operator|-
name|call
operator|.
name|getStartTime
argument_list|()
operator|)
operator|+
literal|", rpcTimeout="
operator|+
name|call
operator|.
name|timeout
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

