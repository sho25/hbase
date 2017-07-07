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
comment|/**  * Grab-bag of utility functions useful when dealing with RPCs.  *  * @author kenton@google.com Kenton Varda  */
end_comment

begin_class
specifier|public
specifier|final
class|class
name|RpcUtil
block|{
specifier|private
name|RpcUtil
parameter_list|()
block|{}
comment|/**    * Take an {@code RpcCallback<Message>} and convert it to an    * {@code RpcCallback} accepting a specific message type.  This is always    * type-safe (parameter type contravariance).    */
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
specifier|public
specifier|static
parameter_list|<
name|Type
extends|extends
name|Message
parameter_list|>
name|RpcCallback
argument_list|<
name|Type
argument_list|>
name|specializeCallback
parameter_list|(
specifier|final
name|RpcCallback
argument_list|<
name|Message
argument_list|>
name|originalCallback
parameter_list|)
block|{
return|return
operator|(
name|RpcCallback
argument_list|<
name|Type
argument_list|>
operator|)
name|originalCallback
return|;
comment|// The above cast works, but only due to technical details of the Java
comment|// implementation.  A more theoretically correct -- but less efficient --
comment|// implementation would be as follows:
comment|//   return new RpcCallback<Type>() {
comment|//     public void run(Type parameter) {
comment|//       originalCallback.run(parameter);
comment|//     }
comment|//   };
block|}
comment|/**    * Take an {@code RpcCallback} accepting a specific message type and convert    * it to an {@code RpcCallback<Message>}.  The generalized callback will    * accept any message object which has the same descriptor, and will convert    * it to the correct class before calling the original callback.  However,    * if the generalized callback is given a message with a different descriptor,    * an exception will be thrown.    */
specifier|public
specifier|static
parameter_list|<
name|Type
extends|extends
name|Message
parameter_list|>
name|RpcCallback
argument_list|<
name|Message
argument_list|>
name|generalizeCallback
parameter_list|(
specifier|final
name|RpcCallback
argument_list|<
name|Type
argument_list|>
name|originalCallback
parameter_list|,
specifier|final
name|Class
argument_list|<
name|Type
argument_list|>
name|originalClass
parameter_list|,
specifier|final
name|Type
name|defaultInstance
parameter_list|)
block|{
return|return
operator|new
name|RpcCallback
argument_list|<
name|Message
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|(
specifier|final
name|Message
name|parameter
parameter_list|)
block|{
name|Type
name|typedParameter
decl_stmt|;
try|try
block|{
name|typedParameter
operator|=
name|originalClass
operator|.
name|cast
argument_list|(
name|parameter
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ClassCastException
name|ignored
parameter_list|)
block|{
name|typedParameter
operator|=
name|copyAsType
argument_list|(
name|defaultInstance
argument_list|,
name|parameter
argument_list|)
expr_stmt|;
block|}
name|originalCallback
operator|.
name|run
argument_list|(
name|typedParameter
argument_list|)
expr_stmt|;
block|}
block|}
return|;
block|}
comment|/**    * Creates a new message of type "Type" which is a copy of "source".  "source"    * must have the same descriptor but may be a different class (e.g.    * DynamicMessage).    */
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
specifier|private
specifier|static
parameter_list|<
name|Type
extends|extends
name|Message
parameter_list|>
name|Type
name|copyAsType
parameter_list|(
specifier|final
name|Type
name|typeDefaultInstance
parameter_list|,
specifier|final
name|Message
name|source
parameter_list|)
block|{
return|return
operator|(
name|Type
operator|)
name|typeDefaultInstance
operator|.
name|newBuilderForType
argument_list|()
operator|.
name|mergeFrom
argument_list|(
name|source
argument_list|)
operator|.
name|build
argument_list|()
return|;
block|}
comment|/**    * Creates a callback which can only be called once.  This may be useful for    * security, when passing a callback to untrusted code:  most callbacks do    * not expect to be called more than once, so doing so may expose bugs if it    * is not prevented.    */
specifier|public
specifier|static
parameter_list|<
name|ParameterType
parameter_list|>
name|RpcCallback
argument_list|<
name|ParameterType
argument_list|>
name|newOneTimeCallback
parameter_list|(
specifier|final
name|RpcCallback
argument_list|<
name|ParameterType
argument_list|>
name|originalCallback
parameter_list|)
block|{
return|return
operator|new
name|RpcCallback
argument_list|<
name|ParameterType
argument_list|>
argument_list|()
block|{
specifier|private
name|boolean
name|alreadyCalled
init|=
literal|false
decl_stmt|;
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|(
specifier|final
name|ParameterType
name|parameter
parameter_list|)
block|{
synchronized|synchronized
init|(
name|this
init|)
block|{
if|if
condition|(
name|alreadyCalled
condition|)
block|{
throw|throw
operator|new
name|AlreadyCalledException
argument_list|()
throw|;
block|}
name|alreadyCalled
operator|=
literal|true
expr_stmt|;
block|}
name|originalCallback
operator|.
name|run
argument_list|(
name|parameter
argument_list|)
expr_stmt|;
block|}
block|}
return|;
block|}
comment|/**    * Exception thrown when a one-time callback is called more than once.    */
specifier|public
specifier|static
specifier|final
class|class
name|AlreadyCalledException
extends|extends
name|RuntimeException
block|{
specifier|private
specifier|static
specifier|final
name|long
name|serialVersionUID
init|=
literal|5469741279507848266L
decl_stmt|;
specifier|public
name|AlreadyCalledException
parameter_list|()
block|{
name|super
argument_list|(
literal|"This RpcCallback was already called and cannot be called "
operator|+
literal|"multiple times."
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

