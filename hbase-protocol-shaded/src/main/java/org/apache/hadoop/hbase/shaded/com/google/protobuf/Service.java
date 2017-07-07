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
comment|/**  * Abstract base interface for protocol-buffer-based RPC services.  Services  * themselves are abstract classes (implemented either by servers or as  * stubs), but they subclass this base interface.  The methods of this  * interface can be used to call the methods of the service without knowing  * its exact type at compile time (analogous to the Message interface).  *  *<p>Starting with version 2.3.0, RPC implementations should not try to build  * on this, but should instead provide code generator plugins which generate  * code specific to the particular RPC implementation.  This way the generated  * code can be more appropriate for the implementation in use and can avoid  * unnecessary layers of indirection.  *  * @author kenton@google.com Kenton Varda  */
end_comment

begin_interface
specifier|public
interface|interface
name|Service
block|{
comment|/**    * Get the {@code ServiceDescriptor} describing this service and its methods.    */
name|Descriptors
operator|.
name|ServiceDescriptor
name|getDescriptorForType
parameter_list|()
function_decl|;
comment|/**    *<p>Call a method of the service specified by MethodDescriptor.  This is    * normally implemented as a simple {@code switch()} that calls the standard    * definitions of the service's methods.    *    *<p>Preconditions:    *<ul>    *<li>{@code method.getService() == getDescriptorForType()}    *<li>{@code request} is of the exact same class as the object returned by    *       {@code getRequestPrototype(method)}.    *<li>{@code controller} is of the correct type for the RPC implementation    *       being used by this Service.  For stubs, the "correct type" depends    *       on the RpcChannel which the stub is using.  Server-side Service    *       implementations are expected to accept whatever type of    *       {@code RpcController} the server-side RPC implementation uses.    *</ul>    *    *<p>Postconditions:    *<ul>    *<li>{@code done} will be called when the method is complete.  This may be    *       before {@code callMethod()} returns or it may be at some point in    *       the future.    *<li>The parameter to {@code done} is the response.  It must be of the    *       exact same type as would be returned by    *       {@code getResponsePrototype(method)}.    *<li>If the RPC failed, the parameter to {@code done} will be    *       {@code null}.  Further details about the failure can be found by    *       querying {@code controller}.    *</ul>    */
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
name|RpcCallback
argument_list|<
name|Message
argument_list|>
name|done
parameter_list|)
function_decl|;
comment|/**    *<p>{@code callMethod()} requires that the request passed in is of a    * particular subclass of {@code Message}.  {@code getRequestPrototype()}    * gets the default instances of this type for a given method.  You can then    * call {@code Message.newBuilderForType()} on this instance to    * construct a builder to build an object which you can then pass to    * {@code callMethod()}.    *    *<p>Example:    *<pre>    *   MethodDescriptor method =    *     service.getDescriptorForType().findMethodByName("Foo");    *   Message request =    *     stub.getRequestPrototype(method).newBuilderForType()    *         .mergeFrom(input).build();    *   service.callMethod(method, request, callback);    *</pre>    */
name|Message
name|getRequestPrototype
parameter_list|(
name|Descriptors
operator|.
name|MethodDescriptor
name|method
parameter_list|)
function_decl|;
comment|/**    * Like {@code getRequestPrototype()}, but gets a prototype of the response    * message.  {@code getResponsePrototype()} is generally not needed because    * the {@code Service} implementation constructs the response message itself,    * but it may be useful in some cases to know ahead of time what type of    * object will be returned.    */
name|Message
name|getResponsePrototype
parameter_list|(
name|Descriptors
operator|.
name|MethodDescriptor
name|method
parameter_list|)
function_decl|;
block|}
end_interface

end_unit

