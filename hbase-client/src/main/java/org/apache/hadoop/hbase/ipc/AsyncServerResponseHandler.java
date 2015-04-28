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
name|ipc
package|;
end_package

begin_import
import|import
name|io
operator|.
name|netty
operator|.
name|buffer
operator|.
name|ByteBuf
import|;
end_import

begin_import
import|import
name|io
operator|.
name|netty
operator|.
name|buffer
operator|.
name|ByteBufInputStream
import|;
end_import

begin_import
import|import
name|io
operator|.
name|netty
operator|.
name|channel
operator|.
name|ChannelHandlerContext
import|;
end_import

begin_import
import|import
name|io
operator|.
name|netty
operator|.
name|channel
operator|.
name|ChannelInboundHandlerAdapter
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
name|protobuf
operator|.
name|generated
operator|.
name|RPCProtos
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
comment|/**  * Handles Hbase responses  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|AsyncServerResponseHandler
extends|extends
name|ChannelInboundHandlerAdapter
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
name|AsyncServerResponseHandler
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|AsyncRpcChannel
name|channel
decl_stmt|;
comment|/**    * Constructor    *    * @param channel on which this response handler operates    */
specifier|public
name|AsyncServerResponseHandler
parameter_list|(
name|AsyncRpcChannel
name|channel
parameter_list|)
block|{
name|this
operator|.
name|channel
operator|=
name|channel
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|channelRead
parameter_list|(
name|ChannelHandlerContext
name|ctx
parameter_list|,
name|Object
name|msg
parameter_list|)
throws|throws
name|Exception
block|{
name|ByteBuf
name|inBuffer
init|=
operator|(
name|ByteBuf
operator|)
name|msg
decl_stmt|;
name|ByteBufInputStream
name|in
init|=
operator|new
name|ByteBufInputStream
argument_list|(
name|inBuffer
argument_list|)
decl_stmt|;
name|int
name|totalSize
init|=
name|inBuffer
operator|.
name|readableBytes
argument_list|()
decl_stmt|;
try|try
block|{
comment|// Read the header
name|RPCProtos
operator|.
name|ResponseHeader
name|responseHeader
init|=
name|RPCProtos
operator|.
name|ResponseHeader
operator|.
name|parseDelimitedFrom
argument_list|(
name|in
argument_list|)
decl_stmt|;
name|int
name|id
init|=
name|responseHeader
operator|.
name|getCallId
argument_list|()
decl_stmt|;
name|AsyncCall
name|call
init|=
name|channel
operator|.
name|removePendingCall
argument_list|(
name|id
argument_list|)
decl_stmt|;
if|if
condition|(
name|call
operator|==
literal|null
condition|)
block|{
comment|// So we got a response for which we have no corresponding 'call' here on the client-side.
comment|// We probably timed out waiting, cleaned up all references, and now the server decides
comment|// to return a response.  There is nothing we can do w/ the response at this stage. Clean
comment|// out the wire of the response so its out of the way and we can get other responses on
comment|// this connection.
name|int
name|readSoFar
init|=
name|IPCUtil
operator|.
name|getTotalSizeWhenWrittenDelimited
argument_list|(
name|responseHeader
argument_list|)
decl_stmt|;
name|int
name|whatIsLeftToRead
init|=
name|totalSize
operator|-
name|readSoFar
decl_stmt|;
comment|// This is done through a Netty ByteBuf which has different behavior than InputStream.
comment|// It does not return number of bytes read but will update pointer internally and throws an
comment|// exception when too many bytes are to be skipped.
name|inBuffer
operator|.
name|skipBytes
argument_list|(
name|whatIsLeftToRead
argument_list|)
expr_stmt|;
return|return;
block|}
if|if
condition|(
name|responseHeader
operator|.
name|hasException
argument_list|()
condition|)
block|{
name|RPCProtos
operator|.
name|ExceptionResponse
name|exceptionResponse
init|=
name|responseHeader
operator|.
name|getException
argument_list|()
decl_stmt|;
name|RemoteException
name|re
init|=
name|createRemoteException
argument_list|(
name|exceptionResponse
argument_list|)
decl_stmt|;
if|if
condition|(
name|exceptionResponse
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
condition|)
block|{
name|channel
operator|.
name|close
argument_list|(
name|re
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|call
operator|.
name|setFailed
argument_list|(
name|re
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|Message
name|value
init|=
literal|null
decl_stmt|;
comment|// Call may be null because it may have timedout and been cleaned up on this side already
if|if
condition|(
name|call
operator|.
name|responseDefaultType
operator|!=
literal|null
condition|)
block|{
name|Message
operator|.
name|Builder
name|builder
init|=
name|call
operator|.
name|responseDefaultType
operator|.
name|newBuilderForType
argument_list|()
decl_stmt|;
name|builder
operator|.
name|mergeDelimitedFrom
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|value
operator|=
name|builder
operator|.
name|build
argument_list|()
expr_stmt|;
block|}
name|CellScanner
name|cellBlockScanner
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|responseHeader
operator|.
name|hasCellBlockMeta
argument_list|()
condition|)
block|{
name|int
name|size
init|=
name|responseHeader
operator|.
name|getCellBlockMeta
argument_list|()
operator|.
name|getLength
argument_list|()
decl_stmt|;
name|byte
index|[]
name|cellBlock
init|=
operator|new
name|byte
index|[
name|size
index|]
decl_stmt|;
name|inBuffer
operator|.
name|readBytes
argument_list|(
name|cellBlock
argument_list|,
literal|0
argument_list|,
name|cellBlock
operator|.
name|length
argument_list|)
expr_stmt|;
name|cellBlockScanner
operator|=
name|channel
operator|.
name|client
operator|.
name|createCellScanner
argument_list|(
name|cellBlock
argument_list|)
expr_stmt|;
block|}
name|call
operator|.
name|setSuccess
argument_list|(
name|value
argument_list|,
name|cellBlockScanner
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
comment|// Treat this as a fatal condition and close this connection
name|channel
operator|.
name|close
argument_list|(
name|e
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|inBuffer
operator|.
name|release
argument_list|()
expr_stmt|;
block|}
block|}
comment|/**    * @param e Proto exception    * @return RemoteException made from passed<code>e</code>    */
specifier|private
name|RemoteException
name|createRemoteException
parameter_list|(
specifier|final
name|RPCProtos
operator|.
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
block|}
end_class

end_unit

