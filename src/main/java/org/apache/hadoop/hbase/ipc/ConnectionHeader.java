begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Copyright 2010 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|java
operator|.
name|io
operator|.
name|DataInput
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataOutput
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
name|hadoop
operator|.
name|io
operator|.
name|Text
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
name|io
operator|.
name|Writable
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

begin_comment
comment|/**  * The IPC connection header sent by the client to the server  * on connection establishment.  */
end_comment

begin_class
class|class
name|ConnectionHeader
implements|implements
name|Writable
block|{
specifier|protected
name|String
name|protocol
decl_stmt|;
specifier|public
name|ConnectionHeader
parameter_list|()
block|{}
comment|/**    * Create a new {@link ConnectionHeader} with the given<code>protocol</code>    * and {@link User}.    * @param protocol protocol used for communication between the IPC client    *                 and the server    * @param user {@link User} of the client communicating with    *            the server    */
specifier|public
name|ConnectionHeader
parameter_list|(
name|String
name|protocol
parameter_list|,
name|User
name|user
parameter_list|)
block|{
name|this
operator|.
name|protocol
operator|=
name|protocol
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|readFields
parameter_list|(
name|DataInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|protocol
operator|=
name|Text
operator|.
name|readString
argument_list|(
name|in
argument_list|)
expr_stmt|;
if|if
condition|(
name|protocol
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|protocol
operator|=
literal|null
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|write
parameter_list|(
name|DataOutput
name|out
parameter_list|)
throws|throws
name|IOException
block|{
name|Text
operator|.
name|writeString
argument_list|(
name|out
argument_list|,
operator|(
name|protocol
operator|==
literal|null
operator|)
condition|?
literal|""
else|:
name|protocol
argument_list|)
expr_stmt|;
block|}
specifier|public
name|String
name|getProtocol
parameter_list|()
block|{
return|return
name|protocol
return|;
block|}
specifier|public
name|User
name|getUser
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
name|protocol
return|;
block|}
block|}
end_class

end_unit

