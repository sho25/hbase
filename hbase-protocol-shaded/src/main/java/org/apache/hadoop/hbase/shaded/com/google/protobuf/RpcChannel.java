begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|// Protocol Buffers - Google's data interchange format
end_comment

begin_comment
comment|// Copyright 2008 Google Inc.  All rights reserved.
end_comment

begin_comment
comment|// https://developers.google.com/protocol-buffers/
end_comment

begin_comment
comment|//
end_comment

begin_comment
comment|// Redistribution and use in source and binary forms, with or without
end_comment

begin_comment
comment|// modification, are permitted provided that the following conditions are
end_comment

begin_comment
comment|// met:
end_comment

begin_comment
comment|//
end_comment

begin_comment
comment|//     * Redistributions of source code must retain the above copyright
end_comment

begin_comment
comment|// notice, this list of conditions and the following disclaimer.
end_comment

begin_comment
comment|//     * Redistributions in binary form must reproduce the above
end_comment

begin_comment
comment|// copyright notice, this list of conditions and the following disclaimer
end_comment

begin_comment
comment|// in the documentation and/or other materials provided with the
end_comment

begin_comment
comment|// distribution.
end_comment

begin_comment
comment|//     * Neither the name of Google Inc. nor the names of its
end_comment

begin_comment
comment|// contributors may be used to endorse or promote products derived from
end_comment

begin_comment
comment|// this software without specific prior written permission.
end_comment

begin_comment
comment|//
end_comment

begin_comment
comment|// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
end_comment

begin_comment
comment|// "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
end_comment

begin_comment
comment|// LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
end_comment

begin_comment
comment|// A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
end_comment

begin_comment
comment|// OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
end_comment

begin_comment
comment|// SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
end_comment

begin_comment
comment|// LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
end_comment

begin_comment
comment|// DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
end_comment

begin_comment
comment|// THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
end_comment

begin_comment
comment|// (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
end_comment

begin_comment
comment|// OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
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
name|shaded
operator|.
name|com
operator|.
name|google
operator|.
name|protobuf
package|;
end_package

begin_comment
comment|/**  *<p>Abstract interface for an RPC channel.  An {@code RpcChannel} represents a  * communication line to a {@link Service} which can be used to call that  * {@link Service}'s methods.  The {@link Service} may be running on another  * machine.  Normally, you should not call an {@code RpcChannel} directly, but  * instead construct a stub {@link Service} wrapping it.  Example:  *  *<pre>  *   RpcChannel channel = rpcImpl.newChannel("remotehost.example.com:1234");  *   RpcController controller = rpcImpl.newController();  *   MyService service = MyService.newStub(channel);  *   service.myMethod(controller, request, callback);  *</pre>  *  *<p>Starting with version 2.3.0, RPC implementations should not try to build  * on this, but should instead provide code generator plugins which generate  * code specific to the particular RPC implementation.  This way the generated  * code can be more appropriate for the implementation in use and can avoid  * unnecessary layers of indirection.  *  * @author kenton@google.com Kenton Varda  */
end_comment

begin_interface
specifier|public
interface|interface
name|RpcChannel
block|{
comment|/**    * Call the given method of the remote service.  This method is similar to    * {@code Service.callMethod()} with one important difference:  the caller    * decides the types of the {@code Message} objects, not the callee.  The    * request may be of any type as long as    * {@code request.getDescriptor() == method.getInputType()}.    * The response passed to the callback will be of the same type as    * {@code responsePrototype} (which must have    * {@code getDescriptor() == method.getOutputType()}).    */
name|void
name|callMethod
parameter_list|(
name|Descriptors
operator|.
name|MethodDescriptor
name|method
parameter_list|,
name|RpcController
name|controller
parameter_list|,
name|Message
name|request
parameter_list|,
name|Message
name|responsePrototype
parameter_list|,
name|RpcCallback
argument_list|<
name|Message
argument_list|>
name|done
parameter_list|)
function_decl|;
block|}
end_interface

end_unit

