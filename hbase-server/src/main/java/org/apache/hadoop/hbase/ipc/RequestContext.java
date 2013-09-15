begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Copyright The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|org
operator|.
name|apache
operator|.
name|hadoop
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
name|security
operator|.
name|User
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
name|BlockingService
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
name|cloudera
operator|.
name|htrace
operator|.
name|Trace
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

begin_comment
comment|/**  * Represents client information (authenticated username, remote address, protocol)  * for the currently executing request.  If called outside the context of a RPC request, all values  * will be<code>null</code>. The {@link CallRunner} class before it a call and then on  * its way out, it will clear the thread local.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|RequestContext
block|{
specifier|private
specifier|static
name|ThreadLocal
argument_list|<
name|RequestContext
argument_list|>
name|instance
init|=
operator|new
name|ThreadLocal
argument_list|<
name|RequestContext
argument_list|>
argument_list|()
block|{
specifier|protected
name|RequestContext
name|initialValue
parameter_list|()
block|{
return|return
operator|new
name|RequestContext
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
return|;
block|}
block|}
decl_stmt|;
specifier|public
specifier|static
name|RequestContext
name|get
parameter_list|()
block|{
return|return
name|instance
operator|.
name|get
argument_list|()
return|;
block|}
comment|/**    * Returns the user credentials associated with the current RPC request or    *<code>null</code> if no credentials were provided.    * @return A User    */
specifier|public
specifier|static
name|User
name|getRequestUser
parameter_list|()
block|{
name|RequestContext
name|ctx
init|=
name|instance
operator|.
name|get
argument_list|()
decl_stmt|;
if|if
condition|(
name|ctx
operator|!=
literal|null
condition|)
block|{
return|return
name|ctx
operator|.
name|getUser
argument_list|()
return|;
block|}
return|return
literal|null
return|;
block|}
comment|/**    * Returns the username for any user associated with the current RPC    * request or<code>null</code> if no user is set.    */
specifier|public
specifier|static
name|String
name|getRequestUserName
parameter_list|()
block|{
name|User
name|user
init|=
name|getRequestUser
argument_list|()
decl_stmt|;
if|if
condition|(
name|user
operator|!=
literal|null
condition|)
block|{
return|return
name|user
operator|.
name|getShortName
argument_list|()
return|;
block|}
return|return
literal|null
return|;
block|}
comment|/**    * Indicates whether or not the current thread is within scope of executing    * an RPC request.    */
specifier|public
specifier|static
name|boolean
name|isInRequestContext
parameter_list|()
block|{
name|RequestContext
name|ctx
init|=
name|instance
operator|.
name|get
argument_list|()
decl_stmt|;
if|if
condition|(
name|ctx
operator|!=
literal|null
condition|)
block|{
return|return
name|ctx
operator|.
name|isInRequest
argument_list|()
return|;
block|}
return|return
literal|false
return|;
block|}
comment|/**    * Initializes the client credentials for the current request.    * @param user    * @param remoteAddress    * @param service    */
specifier|public
specifier|static
name|void
name|set
parameter_list|(
name|User
name|user
parameter_list|,
name|InetAddress
name|remoteAddress
parameter_list|,
name|BlockingService
name|service
parameter_list|)
block|{
name|RequestContext
name|ctx
init|=
name|instance
operator|.
name|get
argument_list|()
decl_stmt|;
name|ctx
operator|.
name|user
operator|=
name|user
expr_stmt|;
name|ctx
operator|.
name|remoteAddress
operator|=
name|remoteAddress
expr_stmt|;
name|ctx
operator|.
name|service
operator|=
name|service
expr_stmt|;
name|ctx
operator|.
name|inRequest
operator|=
literal|true
expr_stmt|;
if|if
condition|(
name|Trace
operator|.
name|isTracing
argument_list|()
condition|)
block|{
if|if
condition|(
name|user
operator|!=
literal|null
condition|)
block|{
name|Trace
operator|.
name|currentSpan
argument_list|()
operator|.
name|addKVAnnotation
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"user"
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|user
operator|.
name|getName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|remoteAddress
operator|!=
literal|null
condition|)
block|{
name|Trace
operator|.
name|currentSpan
argument_list|()
operator|.
name|addKVAnnotation
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"remoteAddress"
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|remoteAddress
operator|.
name|getHostAddress
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|/**    * Clears out the client credentials for a given request.    */
specifier|public
specifier|static
name|void
name|clear
parameter_list|()
block|{
name|RequestContext
name|ctx
init|=
name|instance
operator|.
name|get
argument_list|()
decl_stmt|;
name|ctx
operator|.
name|user
operator|=
literal|null
expr_stmt|;
name|ctx
operator|.
name|remoteAddress
operator|=
literal|null
expr_stmt|;
name|ctx
operator|.
name|service
operator|=
literal|null
expr_stmt|;
name|ctx
operator|.
name|inRequest
operator|=
literal|false
expr_stmt|;
block|}
specifier|private
name|User
name|user
decl_stmt|;
specifier|private
name|InetAddress
name|remoteAddress
decl_stmt|;
specifier|private
name|BlockingService
name|service
decl_stmt|;
comment|// indicates we're within a RPC request invocation
specifier|private
name|boolean
name|inRequest
decl_stmt|;
specifier|private
name|RequestContext
parameter_list|(
name|User
name|user
parameter_list|,
name|InetAddress
name|remoteAddr
parameter_list|,
name|BlockingService
name|service
parameter_list|)
block|{
name|this
operator|.
name|user
operator|=
name|user
expr_stmt|;
name|this
operator|.
name|remoteAddress
operator|=
name|remoteAddr
expr_stmt|;
name|this
operator|.
name|service
operator|=
name|service
expr_stmt|;
block|}
specifier|public
name|User
name|getUser
parameter_list|()
block|{
return|return
name|user
return|;
block|}
specifier|public
name|InetAddress
name|getRemoteAddress
parameter_list|()
block|{
return|return
name|remoteAddress
return|;
block|}
specifier|public
name|BlockingService
name|getService
parameter_list|()
block|{
return|return
name|this
operator|.
name|service
return|;
block|}
name|boolean
name|isInRequest
parameter_list|()
block|{
return|return
name|inRequest
return|;
block|}
block|}
end_class

end_unit

