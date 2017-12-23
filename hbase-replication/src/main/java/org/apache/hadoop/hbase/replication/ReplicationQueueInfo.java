begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|replication
package|;
end_package

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
name|List
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
name|yetus
operator|.
name|audience
operator|.
name|InterfaceAudience
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|LoggerFactory
import|;
end_import

begin_comment
comment|/**  * This class is responsible for the parsing logic for a znode representing a queue.  * It will extract the peerId if it's recovered as well as the dead region servers  * that were part of the queue's history.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|ReplicationQueueInfo
block|{
specifier|private
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|ReplicationQueueInfo
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|String
name|peerId
decl_stmt|;
specifier|private
specifier|final
name|String
name|peerClusterZnode
decl_stmt|;
specifier|private
name|boolean
name|queueRecovered
decl_stmt|;
comment|// List of all the dead region servers that had this queue (if recovered)
specifier|private
name|List
argument_list|<
name|ServerName
argument_list|>
name|deadRegionServers
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
comment|/**    * The passed znode will be either the id of the peer cluster or    * the handling story of that queue in the form of id-servername-*    */
specifier|public
name|ReplicationQueueInfo
parameter_list|(
name|String
name|znode
parameter_list|)
block|{
name|this
operator|.
name|peerClusterZnode
operator|=
name|znode
expr_stmt|;
name|String
index|[]
name|parts
init|=
name|znode
operator|.
name|split
argument_list|(
literal|"-"
argument_list|,
literal|2
argument_list|)
decl_stmt|;
name|this
operator|.
name|queueRecovered
operator|=
name|parts
operator|.
name|length
operator|!=
literal|1
expr_stmt|;
name|this
operator|.
name|peerId
operator|=
name|this
operator|.
name|queueRecovered
condition|?
name|parts
index|[
literal|0
index|]
else|:
name|peerClusterZnode
expr_stmt|;
if|if
condition|(
name|parts
operator|.
name|length
operator|>=
literal|2
condition|)
block|{
comment|// extract dead servers
name|extractDeadServersFromZNodeString
argument_list|(
name|parts
index|[
literal|1
index|]
argument_list|,
name|this
operator|.
name|deadRegionServers
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Parse dead server names from znode string servername can contain "-" such as    * "ip-10-46-221-101.ec2.internal", so we need skip some "-" during parsing for the following    * cases: 2-ip-10-46-221-101.ec2.internal,52170,1364333181125-&lt;server name>-...    */
specifier|private
specifier|static
name|void
name|extractDeadServersFromZNodeString
parameter_list|(
name|String
name|deadServerListStr
parameter_list|,
name|List
argument_list|<
name|ServerName
argument_list|>
name|result
parameter_list|)
block|{
if|if
condition|(
name|deadServerListStr
operator|==
literal|null
operator|||
name|result
operator|==
literal|null
operator|||
name|deadServerListStr
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
return|return;
block|}
comment|// valid server name delimiter "-" has to be after "," in a server name
name|int
name|seenCommaCnt
init|=
literal|0
decl_stmt|;
name|int
name|startIndex
init|=
literal|0
decl_stmt|;
name|int
name|len
init|=
name|deadServerListStr
operator|.
name|length
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|len
condition|;
name|i
operator|++
control|)
block|{
switch|switch
condition|(
name|deadServerListStr
operator|.
name|charAt
argument_list|(
name|i
argument_list|)
condition|)
block|{
case|case
literal|','
case|:
name|seenCommaCnt
operator|+=
literal|1
expr_stmt|;
break|break;
case|case
literal|'-'
case|:
if|if
condition|(
name|seenCommaCnt
operator|>=
literal|2
condition|)
block|{
if|if
condition|(
name|i
operator|>
name|startIndex
condition|)
block|{
name|String
name|serverName
init|=
name|deadServerListStr
operator|.
name|substring
argument_list|(
name|startIndex
argument_list|,
name|i
argument_list|)
decl_stmt|;
if|if
condition|(
name|ServerName
operator|.
name|isFullServerName
argument_list|(
name|serverName
argument_list|)
condition|)
block|{
name|result
operator|.
name|add
argument_list|(
name|ServerName
operator|.
name|valueOf
argument_list|(
name|serverName
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Found invalid server name:"
operator|+
name|serverName
argument_list|)
expr_stmt|;
block|}
name|startIndex
operator|=
name|i
operator|+
literal|1
expr_stmt|;
block|}
name|seenCommaCnt
operator|=
literal|0
expr_stmt|;
block|}
break|break;
default|default:
break|break;
block|}
block|}
comment|// add tail
if|if
condition|(
name|startIndex
operator|<
name|len
operator|-
literal|1
condition|)
block|{
name|String
name|serverName
init|=
name|deadServerListStr
operator|.
name|substring
argument_list|(
name|startIndex
argument_list|,
name|len
argument_list|)
decl_stmt|;
if|if
condition|(
name|ServerName
operator|.
name|isFullServerName
argument_list|(
name|serverName
argument_list|)
condition|)
block|{
name|result
operator|.
name|add
argument_list|(
name|ServerName
operator|.
name|valueOf
argument_list|(
name|serverName
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Found invalid server name at the end:"
operator|+
name|serverName
argument_list|)
expr_stmt|;
block|}
block|}
name|LOG
operator|.
name|debug
argument_list|(
literal|"Found dead servers:"
operator|+
name|result
argument_list|)
expr_stmt|;
block|}
specifier|public
name|List
argument_list|<
name|ServerName
argument_list|>
name|getDeadRegionServers
parameter_list|()
block|{
return|return
name|Collections
operator|.
name|unmodifiableList
argument_list|(
name|this
operator|.
name|deadRegionServers
argument_list|)
return|;
block|}
specifier|public
name|String
name|getPeerId
parameter_list|()
block|{
return|return
name|this
operator|.
name|peerId
return|;
block|}
specifier|public
name|String
name|getPeerClusterZnode
parameter_list|()
block|{
return|return
name|this
operator|.
name|peerClusterZnode
return|;
block|}
specifier|public
name|boolean
name|isQueueRecovered
parameter_list|()
block|{
return|return
name|queueRecovered
return|;
block|}
block|}
end_class

end_unit

