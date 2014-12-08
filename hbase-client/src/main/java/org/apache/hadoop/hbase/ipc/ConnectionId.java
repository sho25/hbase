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
name|security
operator|.
name|User
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

begin_comment
comment|/**  * This class holds the address and the user ticket, etc. The client connections  * to servers are uniquely identified by<remoteAddress, ticket, serviceName>  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|ConnectionId
block|{
specifier|final
name|InetSocketAddress
name|address
decl_stmt|;
specifier|final
name|User
name|ticket
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|PRIME
init|=
literal|16777619
decl_stmt|;
specifier|final
name|String
name|serviceName
decl_stmt|;
specifier|public
name|ConnectionId
parameter_list|(
name|User
name|ticket
parameter_list|,
name|String
name|serviceName
parameter_list|,
name|InetSocketAddress
name|address
parameter_list|)
block|{
name|this
operator|.
name|address
operator|=
name|address
expr_stmt|;
name|this
operator|.
name|ticket
operator|=
name|ticket
expr_stmt|;
name|this
operator|.
name|serviceName
operator|=
name|serviceName
expr_stmt|;
block|}
specifier|public
name|String
name|getServiceName
parameter_list|()
block|{
return|return
name|this
operator|.
name|serviceName
return|;
block|}
specifier|public
name|InetSocketAddress
name|getAddress
parameter_list|()
block|{
return|return
name|address
return|;
block|}
specifier|public
name|User
name|getTicket
parameter_list|()
block|{
return|return
name|ticket
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
name|this
operator|.
name|address
operator|.
name|toString
argument_list|()
operator|+
literal|"/"
operator|+
name|this
operator|.
name|serviceName
operator|+
literal|"/"
operator|+
name|this
operator|.
name|ticket
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|equals
parameter_list|(
name|Object
name|obj
parameter_list|)
block|{
if|if
condition|(
name|obj
operator|instanceof
name|ConnectionId
condition|)
block|{
name|ConnectionId
name|id
init|=
operator|(
name|ConnectionId
operator|)
name|obj
decl_stmt|;
return|return
name|address
operator|.
name|equals
argument_list|(
name|id
operator|.
name|address
argument_list|)
operator|&&
operator|(
operator|(
name|ticket
operator|!=
literal|null
operator|&&
name|ticket
operator|.
name|equals
argument_list|(
name|id
operator|.
name|ticket
argument_list|)
operator|)
operator|||
operator|(
name|ticket
operator|==
name|id
operator|.
name|ticket
operator|)
operator|)
operator|&&
name|this
operator|.
name|serviceName
operator|==
name|id
operator|.
name|serviceName
return|;
block|}
return|return
literal|false
return|;
block|}
annotation|@
name|Override
comment|// simply use the default Object#hashcode() ?
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
name|int
name|hashcode
init|=
operator|(
name|address
operator|.
name|hashCode
argument_list|()
operator|+
name|PRIME
operator|*
operator|(
name|PRIME
operator|*
name|this
operator|.
name|serviceName
operator|.
name|hashCode
argument_list|()
operator|^
operator|(
name|ticket
operator|==
literal|null
condition|?
literal|0
else|:
name|ticket
operator|.
name|hashCode
argument_list|()
operator|)
operator|)
operator|)
decl_stmt|;
return|return
name|hashcode
return|;
block|}
block|}
end_class

end_unit

