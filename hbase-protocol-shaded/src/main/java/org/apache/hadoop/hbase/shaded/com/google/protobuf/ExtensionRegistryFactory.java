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

begin_import
import|import static
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
name|ExtensionRegistryLite
operator|.
name|EMPTY_REGISTRY_LITE
import|;
end_import

begin_comment
comment|/**  * A factory object to create instances of {@link ExtensionRegistryLite}.  *   *<p>  * This factory detects (via reflection) if the full (non-Lite) protocol buffer libraries  * are available, and if so, the instances returned are actually {@link ExtensionRegistry}.  */
end_comment

begin_class
specifier|final
class|class
name|ExtensionRegistryFactory
block|{
specifier|static
specifier|final
name|String
name|FULL_REGISTRY_CLASS_NAME
init|=
literal|"org.apache.hadoop.hbase.shaded.com.google.protobuf.ExtensionRegistry"
decl_stmt|;
comment|/* Visible for Testing      @Nullable */
specifier|static
specifier|final
name|Class
argument_list|<
name|?
argument_list|>
name|EXTENSION_REGISTRY_CLASS
init|=
name|reflectExtensionRegistry
argument_list|()
decl_stmt|;
comment|/* @Nullable */
specifier|static
name|Class
argument_list|<
name|?
argument_list|>
name|reflectExtensionRegistry
parameter_list|()
block|{
try|try
block|{
return|return
name|Class
operator|.
name|forName
argument_list|(
name|FULL_REGISTRY_CLASS_NAME
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|ClassNotFoundException
name|e
parameter_list|)
block|{
comment|// The exception allocation is potentially expensive on Android (where it can be triggered
comment|// many times at start up). Is there a way to ameliorate this?
return|return
literal|null
return|;
block|}
block|}
comment|/** Construct a new, empty instance. */
specifier|public
specifier|static
name|ExtensionRegistryLite
name|create
parameter_list|()
block|{
if|if
condition|(
name|EXTENSION_REGISTRY_CLASS
operator|!=
literal|null
condition|)
block|{
try|try
block|{
return|return
name|invokeSubclassFactory
argument_list|(
literal|"newInstance"
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
comment|// return a Lite registry.
block|}
block|}
return|return
operator|new
name|ExtensionRegistryLite
argument_list|()
return|;
block|}
comment|/** Get the unmodifiable singleton empty instance. */
specifier|public
specifier|static
name|ExtensionRegistryLite
name|createEmpty
parameter_list|()
block|{
if|if
condition|(
name|EXTENSION_REGISTRY_CLASS
operator|!=
literal|null
condition|)
block|{
try|try
block|{
return|return
name|invokeSubclassFactory
argument_list|(
literal|"getEmptyRegistry"
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
comment|// return a Lite registry.
block|}
block|}
return|return
name|EMPTY_REGISTRY_LITE
return|;
block|}
specifier|static
name|boolean
name|isFullRegistry
parameter_list|(
name|ExtensionRegistryLite
name|registry
parameter_list|)
block|{
return|return
name|EXTENSION_REGISTRY_CLASS
operator|!=
literal|null
operator|&&
name|EXTENSION_REGISTRY_CLASS
operator|.
name|isAssignableFrom
argument_list|(
name|registry
operator|.
name|getClass
argument_list|()
argument_list|)
return|;
block|}
specifier|private
specifier|static
specifier|final
name|ExtensionRegistryLite
name|invokeSubclassFactory
parameter_list|(
name|String
name|methodName
parameter_list|)
throws|throws
name|Exception
block|{
return|return
operator|(
name|ExtensionRegistryLite
operator|)
name|EXTENSION_REGISTRY_CLASS
operator|.
name|getMethod
argument_list|(
name|methodName
argument_list|)
operator|.
name|invoke
argument_list|(
literal|null
argument_list|)
return|;
block|}
block|}
end_class

end_unit

