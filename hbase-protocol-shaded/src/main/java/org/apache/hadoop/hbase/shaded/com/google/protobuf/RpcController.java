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
comment|/**  *<p>An {@code RpcController} mediates a single method call.  The primary  * purpose of the controller is to provide a way to manipulate settings  * specific to the RPC implementation and to find out about RPC-level errors.  *  *<p>Starting with version 2.3.0, RPC implementations should not try to build  * on this, but should instead provide code generator plugins which generate  * code specific to the particular RPC implementation.  This way the generated  * code can be more appropriate for the implementation in use and can avoid  * unnecessary layers of indirection.  *  *<p>The methods provided by the {@code RpcController} interface are intended  * to be a "least common denominator" set of features which we expect all  * implementations to support.  Specific implementations may provide more  * advanced features (e.g. deadline propagation).  *  * @author kenton@google.com Kenton Varda  */
end_comment

begin_interface
specifier|public
interface|interface
name|RpcController
block|{
comment|// -----------------------------------------------------------------
comment|// These calls may be made from the client side only.  Their results
comment|// are undefined on the server side (may throw RuntimeExceptions).
comment|/**    * Resets the RpcController to its initial state so that it may be reused in    * a new call.  This can be called from the client side only.  It must not    * be called while an RPC is in progress.    */
name|void
name|reset
parameter_list|()
function_decl|;
comment|/**    * After a call has finished, returns true if the call failed.  The possible    * reasons for failure depend on the RPC implementation.  {@code failed()}    * most only be called on the client side, and must not be called before a    * call has finished.    */
name|boolean
name|failed
parameter_list|()
function_decl|;
comment|/**    * If {@code failed()} is {@code true}, returns a human-readable description    * of the error.    */
name|String
name|errorText
parameter_list|()
function_decl|;
comment|/**    * Advises the RPC system that the caller desires that the RPC call be    * canceled.  The RPC system may cancel it immediately, may wait awhile and    * then cancel it, or may not even cancel the call at all.  If the call is    * canceled, the "done" callback will still be called and the RpcController    * will indicate that the call failed at that time.    */
name|void
name|startCancel
parameter_list|()
function_decl|;
comment|// -----------------------------------------------------------------
comment|// These calls may be made from the server side only.  Their results
comment|// are undefined on the client side (may throw RuntimeExceptions).
comment|/**    * Causes {@code failed()} to return true on the client side.  {@code reason}    * will be incorporated into the message returned by {@code errorText()}.    * If you find you need to return machine-readable information about    * failures, you should incorporate it into your response protocol buffer    * and should NOT call {@code setFailed()}.    */
name|void
name|setFailed
parameter_list|(
name|String
name|reason
parameter_list|)
function_decl|;
comment|/**    * If {@code true}, indicates that the client canceled the RPC, so the server    * may as well give up on replying to it.  This method must be called on the    * server side only.  The server should still call the final "done" callback.    */
name|boolean
name|isCanceled
parameter_list|()
function_decl|;
comment|/**    * Asks that the given callback be called when the RPC is canceled.  The    * parameter passed to the callback will always be {@code null}.  The    * callback will always be called exactly once.  If the RPC completes without    * being canceled, the callback will be called after completion.  If the RPC    * has already been canceled when NotifyOnCancel() is called, the callback    * will be called immediately.    *    *<p>{@code notifyOnCancel()} must be called no more than once per request.    * It must be called on the server side only.    */
name|void
name|notifyOnCancel
parameter_list|(
name|RpcCallback
argument_list|<
name|Object
argument_list|>
name|callback
parameter_list|)
function_decl|;
block|}
end_interface

end_unit

