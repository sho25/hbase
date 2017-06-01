begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|ServerName
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
name|EnvironmentEdgeManager
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
name|Pair
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|ArrayList
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collections
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Comparator
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Date
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashMap
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
name|List
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
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

begin_comment
comment|/**  * Class to hold dead servers list and utility querying dead server list.  * On znode expiration, servers are added here.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|DeadServer
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
name|DeadServer
operator|.
name|class
argument_list|)
decl_stmt|;
comment|/**    * Set of known dead servers.  On znode expiration, servers are added here.    * This is needed in case of a network partitioning where the server's lease    * expires, but the server is still running. After the network is healed,    * and it's server logs are recovered, it will be told to call server startup    * because by then, its regions have probably been reassigned.    */
specifier|private
specifier|final
name|Map
argument_list|<
name|ServerName
argument_list|,
name|Long
argument_list|>
name|deadServers
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
comment|/**    * Number of dead servers currently being processed    */
specifier|private
name|int
name|numProcessing
init|=
literal|0
decl_stmt|;
comment|/**    * Whether a dead server is being processed currently.    */
specifier|private
specifier|volatile
name|boolean
name|processing
init|=
literal|false
decl_stmt|;
comment|/**    * A dead server that comes back alive has a different start code. The new start code should be    *  greater than the old one, but we don't take this into account in this method.    *    * @param newServerName Servername as either<code>host:port</code> or    *<code>host,port,startcode</code>.    * @return true if this server was dead before and coming back alive again    */
specifier|public
specifier|synchronized
name|boolean
name|cleanPreviousInstance
parameter_list|(
specifier|final
name|ServerName
name|newServerName
parameter_list|)
block|{
name|Iterator
argument_list|<
name|ServerName
argument_list|>
name|it
init|=
name|deadServers
operator|.
name|keySet
argument_list|()
operator|.
name|iterator
argument_list|()
decl_stmt|;
while|while
condition|(
name|it
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|ServerName
name|sn
init|=
name|it
operator|.
name|next
argument_list|()
decl_stmt|;
if|if
condition|(
name|ServerName
operator|.
name|isSameHostnameAndPort
argument_list|(
name|sn
argument_list|,
name|newServerName
argument_list|)
condition|)
block|{
name|it
operator|.
name|remove
argument_list|()
expr_stmt|;
return|return
literal|true
return|;
block|}
block|}
return|return
literal|false
return|;
block|}
comment|/**    * @param serverName server name.    * @return true if this server is on the dead servers list false otherwise    */
specifier|public
specifier|synchronized
name|boolean
name|isDeadServer
parameter_list|(
specifier|final
name|ServerName
name|serverName
parameter_list|)
block|{
return|return
name|deadServers
operator|.
name|containsKey
argument_list|(
name|serverName
argument_list|)
return|;
block|}
comment|/**    * Checks if there are currently any dead servers being processed by the    * master.  Returns true if at least one region server is currently being    * processed as dead.    *    * @return true if any RS are being processed as dead    */
specifier|public
specifier|synchronized
name|boolean
name|areDeadServersInProgress
parameter_list|()
block|{
return|return
name|processing
return|;
block|}
specifier|public
specifier|synchronized
name|Set
argument_list|<
name|ServerName
argument_list|>
name|copyServerNames
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
argument_list|<>
argument_list|(
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
name|deadServers
operator|.
name|keySet
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|clone
return|;
block|}
comment|/**    * Adds the server to the dead server list if it's not there already.    * @param sn the server name    */
specifier|public
specifier|synchronized
name|void
name|add
parameter_list|(
name|ServerName
name|sn
parameter_list|)
block|{
name|processing
operator|=
literal|true
expr_stmt|;
if|if
condition|(
operator|!
name|deadServers
operator|.
name|containsKey
argument_list|(
name|sn
argument_list|)
condition|)
block|{
name|deadServers
operator|.
name|put
argument_list|(
name|sn
argument_list|,
name|EnvironmentEdgeManager
operator|.
name|currentTime
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Notify that we started processing this dead server.    * @param sn ServerName for the dead server.    */
specifier|public
specifier|synchronized
name|void
name|notifyServer
parameter_list|(
name|ServerName
name|sn
parameter_list|)
block|{
if|if
condition|(
name|LOG
operator|.
name|isTraceEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|trace
argument_list|(
literal|"Started processing "
operator|+
name|sn
argument_list|)
expr_stmt|;
block|}
name|processing
operator|=
literal|true
expr_stmt|;
name|numProcessing
operator|++
expr_stmt|;
block|}
specifier|public
specifier|synchronized
name|void
name|finish
parameter_list|(
name|ServerName
name|sn
parameter_list|)
block|{
name|numProcessing
operator|--
expr_stmt|;
if|if
condition|(
name|LOG
operator|.
name|isTraceEnabled
argument_list|()
condition|)
name|LOG
operator|.
name|trace
argument_list|(
literal|"Finished "
operator|+
name|sn
operator|+
literal|"; numProcessing="
operator|+
name|numProcessing
argument_list|)
expr_stmt|;
assert|assert
name|numProcessing
operator|>=
literal|0
operator|:
literal|"Number of dead servers in processing should always be non-negative"
assert|;
if|if
condition|(
name|numProcessing
operator|<
literal|0
condition|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Number of dead servers in processing = "
operator|+
name|numProcessing
operator|+
literal|". Something went wrong, this should always be non-negative."
argument_list|)
expr_stmt|;
name|numProcessing
operator|=
literal|0
expr_stmt|;
block|}
if|if
condition|(
name|numProcessing
operator|==
literal|0
condition|)
block|{
name|processing
operator|=
literal|false
expr_stmt|;
block|}
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
name|void
name|cleanAllPreviousInstances
parameter_list|(
specifier|final
name|ServerName
name|newServerName
parameter_list|)
block|{
name|Iterator
argument_list|<
name|ServerName
argument_list|>
name|it
init|=
name|deadServers
operator|.
name|keySet
argument_list|()
operator|.
name|iterator
argument_list|()
decl_stmt|;
while|while
condition|(
name|it
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|ServerName
name|sn
init|=
name|it
operator|.
name|next
argument_list|()
decl_stmt|;
if|if
condition|(
name|ServerName
operator|.
name|isSameHostnameAndPort
argument_list|(
name|sn
argument_list|,
name|newServerName
argument_list|)
condition|)
block|{
name|it
operator|.
name|remove
argument_list|()
expr_stmt|;
block|}
block|}
block|}
specifier|public
specifier|synchronized
name|String
name|toString
parameter_list|()
block|{
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
for|for
control|(
name|ServerName
name|sn
range|:
name|deadServers
operator|.
name|keySet
argument_list|()
control|)
block|{
if|if
condition|(
name|sb
operator|.
name|length
argument_list|()
operator|>
literal|0
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|", "
argument_list|)
expr_stmt|;
block|}
name|sb
operator|.
name|append
argument_list|(
name|sn
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|sb
operator|.
name|toString
argument_list|()
return|;
block|}
comment|/**    * Extract all the servers dead since a given time, and sort them.    * @param ts the time, 0 for all    * @return a sorted array list, by death time, lowest values first.    */
specifier|public
specifier|synchronized
name|List
argument_list|<
name|Pair
argument_list|<
name|ServerName
argument_list|,
name|Long
argument_list|>
argument_list|>
name|copyDeadServersSince
parameter_list|(
name|long
name|ts
parameter_list|)
block|{
name|List
argument_list|<
name|Pair
argument_list|<
name|ServerName
argument_list|,
name|Long
argument_list|>
argument_list|>
name|res
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|size
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|ServerName
argument_list|,
name|Long
argument_list|>
name|entry
range|:
name|deadServers
operator|.
name|entrySet
argument_list|()
control|)
block|{
if|if
condition|(
name|entry
operator|.
name|getValue
argument_list|()
operator|>=
name|ts
condition|)
block|{
name|res
operator|.
name|add
argument_list|(
operator|new
name|Pair
argument_list|<>
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|,
name|entry
operator|.
name|getValue
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
name|Collections
operator|.
name|sort
argument_list|(
name|res
argument_list|,
name|ServerNameDeathDateComparator
argument_list|)
expr_stmt|;
return|return
name|res
return|;
block|}
comment|/**    * Get the time when a server died    * @param deadServerName the dead server name    * @return the date when the server died     */
specifier|public
specifier|synchronized
name|Date
name|getTimeOfDeath
parameter_list|(
specifier|final
name|ServerName
name|deadServerName
parameter_list|)
block|{
name|Long
name|time
init|=
name|deadServers
operator|.
name|get
argument_list|(
name|deadServerName
argument_list|)
decl_stmt|;
return|return
name|time
operator|==
literal|null
condition|?
literal|null
else|:
operator|new
name|Date
argument_list|(
name|time
argument_list|)
return|;
block|}
specifier|private
specifier|static
name|Comparator
argument_list|<
name|Pair
argument_list|<
name|ServerName
argument_list|,
name|Long
argument_list|>
argument_list|>
name|ServerNameDeathDateComparator
init|=
operator|new
name|Comparator
argument_list|<
name|Pair
argument_list|<
name|ServerName
argument_list|,
name|Long
argument_list|>
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|int
name|compare
parameter_list|(
name|Pair
argument_list|<
name|ServerName
argument_list|,
name|Long
argument_list|>
name|o1
parameter_list|,
name|Pair
argument_list|<
name|ServerName
argument_list|,
name|Long
argument_list|>
name|o2
parameter_list|)
block|{
return|return
name|o1
operator|.
name|getSecond
argument_list|()
operator|.
name|compareTo
argument_list|(
name|o2
operator|.
name|getSecond
argument_list|()
argument_list|)
return|;
block|}
block|}
decl_stmt|;
block|}
end_class

end_unit

