begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2010 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|master
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collection
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashSet
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Iterator
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Set
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
name|lang
operator|.
name|NotImplementedException
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
name|ServerName
import|;
end_import

begin_comment
comment|/**  * Class to hold dead servers list and utility querying dead server list.  */
end_comment

begin_class
specifier|public
class|class
name|DeadServer
implements|implements
name|Set
argument_list|<
name|ServerName
argument_list|>
block|{
comment|/**    * Set of known dead servers.  On znode expiration, servers are added here.    * This is needed in case of a network partitioning where the server's lease    * expires, but the server is still running. After the network is healed,    * and it's server logs are recovered, it will be told to call server startup    * because by then, its regions have probably been reassigned.    */
specifier|private
specifier|final
name|Set
argument_list|<
name|ServerName
argument_list|>
name|deadServers
init|=
operator|new
name|HashSet
argument_list|<
name|ServerName
argument_list|>
argument_list|()
decl_stmt|;
comment|/** Number of dead servers currently being processed */
specifier|private
name|int
name|numProcessing
decl_stmt|;
specifier|public
name|DeadServer
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
name|this
operator|.
name|numProcessing
operator|=
literal|0
expr_stmt|;
block|}
comment|/**    * @param serverName Server name    * @return true if server is dead    */
specifier|public
name|boolean
name|isDeadServer
parameter_list|(
specifier|final
name|String
name|serverName
parameter_list|)
block|{
return|return
name|isDeadServer
argument_list|(
operator|new
name|ServerName
argument_list|(
name|serverName
argument_list|)
argument_list|)
return|;
block|}
comment|/**    * A dead server that comes back alive has a different start code.    * @param newServerName Servername as either<code>host:port</code> or    *<code>host,port,startcode</code>.    * @return true if this server was dead before and coming back alive again    */
specifier|public
name|boolean
name|cleanPreviousInstance
parameter_list|(
specifier|final
name|ServerName
name|newServerName
parameter_list|)
block|{
name|ServerName
name|sn
init|=
name|ServerName
operator|.
name|findServerWithSameHostnamePort
argument_list|(
name|this
operator|.
name|deadServers
argument_list|,
name|newServerName
argument_list|)
decl_stmt|;
if|if
condition|(
name|sn
operator|==
literal|null
condition|)
return|return
literal|false
return|;
return|return
name|this
operator|.
name|deadServers
operator|.
name|remove
argument_list|(
name|sn
argument_list|)
return|;
block|}
comment|/**    * @param serverName    * @return true if this server is on the dead servers list.    */
name|boolean
name|isDeadServer
parameter_list|(
specifier|final
name|ServerName
name|serverName
parameter_list|)
block|{
return|return
name|this
operator|.
name|deadServers
operator|.
name|contains
argument_list|(
name|serverName
argument_list|)
return|;
block|}
comment|/**    * @return True if we have a server with matching hostname and port.    */
name|boolean
name|isDeadServerWithSameHostnamePort
parameter_list|(
specifier|final
name|ServerName
name|serverName
parameter_list|)
block|{
return|return
name|ServerName
operator|.
name|findServerWithSameHostnamePort
argument_list|(
name|this
operator|.
name|deadServers
argument_list|,
name|serverName
argument_list|)
operator|!=
literal|null
return|;
block|}
comment|/**    * Checks if there are currently any dead servers being processed by the    * master.  Returns true if at least one region server is currently being    * processed as dead.    * @return true if any RS are being processed as dead    */
specifier|public
name|boolean
name|areDeadServersInProgress
parameter_list|()
block|{
return|return
name|numProcessing
operator|!=
literal|0
return|;
block|}
specifier|public
specifier|synchronized
name|Set
argument_list|<
name|ServerName
argument_list|>
name|clone
parameter_list|()
block|{
name|Set
argument_list|<
name|ServerName
argument_list|>
name|clone
init|=
operator|new
name|HashSet
argument_list|<
name|ServerName
argument_list|>
argument_list|(
name|this
operator|.
name|deadServers
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
name|clone
operator|.
name|addAll
argument_list|(
name|this
operator|.
name|deadServers
argument_list|)
expr_stmt|;
return|return
name|clone
return|;
block|}
specifier|public
specifier|synchronized
name|boolean
name|add
parameter_list|(
name|ServerName
name|e
parameter_list|)
block|{
name|this
operator|.
name|numProcessing
operator|++
expr_stmt|;
return|return
name|deadServers
operator|.
name|add
argument_list|(
name|e
argument_list|)
return|;
block|}
specifier|public
specifier|synchronized
name|void
name|finish
parameter_list|(
name|ServerName
name|e
parameter_list|)
block|{
name|this
operator|.
name|numProcessing
operator|--
expr_stmt|;
block|}
specifier|public
specifier|synchronized
name|int
name|size
parameter_list|()
block|{
return|return
name|deadServers
operator|.
name|size
argument_list|()
return|;
block|}
specifier|public
specifier|synchronized
name|boolean
name|isEmpty
parameter_list|()
block|{
return|return
name|deadServers
operator|.
name|isEmpty
argument_list|()
return|;
block|}
specifier|public
specifier|synchronized
name|boolean
name|contains
parameter_list|(
name|Object
name|o
parameter_list|)
block|{
return|return
name|deadServers
operator|.
name|contains
argument_list|(
name|o
argument_list|)
return|;
block|}
specifier|public
name|Iterator
argument_list|<
name|ServerName
argument_list|>
name|iterator
parameter_list|()
block|{
return|return
name|this
operator|.
name|deadServers
operator|.
name|iterator
argument_list|()
return|;
block|}
specifier|public
specifier|synchronized
name|Object
index|[]
name|toArray
parameter_list|()
block|{
return|return
name|deadServers
operator|.
name|toArray
argument_list|()
return|;
block|}
specifier|public
specifier|synchronized
parameter_list|<
name|T
parameter_list|>
name|T
index|[]
name|toArray
parameter_list|(
name|T
index|[]
name|a
parameter_list|)
block|{
return|return
name|deadServers
operator|.
name|toArray
argument_list|(
name|a
argument_list|)
return|;
block|}
specifier|public
specifier|synchronized
name|boolean
name|remove
parameter_list|(
name|Object
name|o
parameter_list|)
block|{
return|return
name|this
operator|.
name|deadServers
operator|.
name|remove
argument_list|(
name|o
argument_list|)
return|;
block|}
specifier|public
specifier|synchronized
name|boolean
name|containsAll
parameter_list|(
name|Collection
argument_list|<
name|?
argument_list|>
name|c
parameter_list|)
block|{
return|return
name|deadServers
operator|.
name|containsAll
argument_list|(
name|c
argument_list|)
return|;
block|}
specifier|public
specifier|synchronized
name|boolean
name|addAll
parameter_list|(
name|Collection
argument_list|<
name|?
extends|extends
name|ServerName
argument_list|>
name|c
parameter_list|)
block|{
return|return
name|deadServers
operator|.
name|addAll
argument_list|(
name|c
argument_list|)
return|;
block|}
specifier|public
specifier|synchronized
name|boolean
name|retainAll
parameter_list|(
name|Collection
argument_list|<
name|?
argument_list|>
name|c
parameter_list|)
block|{
return|return
name|deadServers
operator|.
name|retainAll
argument_list|(
name|c
argument_list|)
return|;
block|}
specifier|public
specifier|synchronized
name|boolean
name|removeAll
parameter_list|(
name|Collection
argument_list|<
name|?
argument_list|>
name|c
parameter_list|)
block|{
return|return
name|deadServers
operator|.
name|removeAll
argument_list|(
name|c
argument_list|)
return|;
block|}
specifier|public
specifier|synchronized
name|void
name|clear
parameter_list|()
block|{
throw|throw
operator|new
name|NotImplementedException
argument_list|()
throw|;
block|}
specifier|public
specifier|synchronized
name|boolean
name|equals
parameter_list|(
name|Object
name|o
parameter_list|)
block|{
return|return
name|deadServers
operator|.
name|equals
argument_list|(
name|o
argument_list|)
return|;
block|}
specifier|public
specifier|synchronized
name|int
name|hashCode
parameter_list|()
block|{
return|return
name|deadServers
operator|.
name|hashCode
argument_list|()
return|;
block|}
specifier|public
specifier|synchronized
name|String
name|toString
parameter_list|()
block|{
return|return
name|this
operator|.
name|deadServers
operator|.
name|toString
argument_list|()
return|;
block|}
block|}
end_class

end_unit

