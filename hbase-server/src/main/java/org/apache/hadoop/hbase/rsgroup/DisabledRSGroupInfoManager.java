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
name|rsgroup
package|;
end_package

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
name|java
operator|.
name|util
operator|.
name|Arrays
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
name|Set
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|SortedSet
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|TreeSet
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
name|DoNotRetryIOException
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
name|TableName
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
name|master
operator|.
name|ServerManager
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
name|net
operator|.
name|Address
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

begin_comment
comment|/**  * A dummy RSGroupInfoManager which only contains a default rs group.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
class|class
name|DisabledRSGroupInfoManager
implements|implements
name|RSGroupInfoManager
block|{
specifier|private
specifier|final
name|ServerManager
name|serverManager
decl_stmt|;
specifier|public
name|DisabledRSGroupInfoManager
parameter_list|(
name|ServerManager
name|serverManager
parameter_list|)
block|{
name|this
operator|.
name|serverManager
operator|=
name|serverManager
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|start
parameter_list|()
block|{   }
annotation|@
name|Override
specifier|public
name|void
name|addRSGroup
parameter_list|(
name|RSGroupInfo
name|rsGroupInfo
parameter_list|)
throws|throws
name|IOException
block|{
throw|throw
operator|new
name|DoNotRetryIOException
argument_list|(
literal|"RSGroup is disabled"
argument_list|)
throw|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|removeRSGroup
parameter_list|(
name|String
name|groupName
parameter_list|)
throws|throws
name|IOException
block|{
throw|throw
operator|new
name|DoNotRetryIOException
argument_list|(
literal|"RSGroup is disabled"
argument_list|)
throw|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|moveServers
parameter_list|(
name|Set
argument_list|<
name|Address
argument_list|>
name|servers
parameter_list|,
name|String
name|targetGroupName
parameter_list|)
throws|throws
name|IOException
block|{
throw|throw
operator|new
name|DoNotRetryIOException
argument_list|(
literal|"RSGroup is disabled"
argument_list|)
throw|;
block|}
specifier|private
name|SortedSet
argument_list|<
name|Address
argument_list|>
name|getOnlineServers
parameter_list|()
block|{
name|SortedSet
argument_list|<
name|Address
argument_list|>
name|onlineServers
init|=
operator|new
name|TreeSet
argument_list|<
name|Address
argument_list|>
argument_list|()
decl_stmt|;
name|serverManager
operator|.
name|getOnlineServers
argument_list|()
operator|.
name|keySet
argument_list|()
operator|.
name|stream
argument_list|()
operator|.
name|map
argument_list|(
name|ServerName
operator|::
name|getAddress
argument_list|)
operator|.
name|forEach
argument_list|(
name|onlineServers
operator|::
name|add
argument_list|)
expr_stmt|;
return|return
name|onlineServers
return|;
block|}
annotation|@
name|Override
specifier|public
name|RSGroupInfo
name|getRSGroupOfServer
parameter_list|(
name|Address
name|serverHostPort
parameter_list|)
throws|throws
name|IOException
block|{
name|SortedSet
argument_list|<
name|Address
argument_list|>
name|onlineServers
init|=
name|getOnlineServers
argument_list|()
decl_stmt|;
if|if
condition|(
name|onlineServers
operator|.
name|contains
argument_list|(
name|serverHostPort
argument_list|)
condition|)
block|{
return|return
operator|new
name|RSGroupInfo
argument_list|(
name|RSGroupInfo
operator|.
name|DEFAULT_GROUP
argument_list|,
name|onlineServers
argument_list|)
return|;
block|}
else|else
block|{
return|return
literal|null
return|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|RSGroupInfo
name|getRSGroup
parameter_list|(
name|String
name|groupName
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|RSGroupInfo
operator|.
name|DEFAULT_GROUP
operator|.
name|equals
argument_list|(
name|groupName
argument_list|)
condition|)
block|{
return|return
operator|new
name|RSGroupInfo
argument_list|(
name|RSGroupInfo
operator|.
name|DEFAULT_GROUP
argument_list|,
name|getOnlineServers
argument_list|()
argument_list|)
return|;
block|}
else|else
block|{
return|return
literal|null
return|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|RSGroupInfo
argument_list|>
name|listRSGroups
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|Arrays
operator|.
name|asList
argument_list|(
operator|new
name|RSGroupInfo
argument_list|(
name|RSGroupInfo
operator|.
name|DEFAULT_GROUP
argument_list|,
name|getOnlineServers
argument_list|()
argument_list|)
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isOnline
parameter_list|()
block|{
return|return
literal|true
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|removeServers
parameter_list|(
name|Set
argument_list|<
name|Address
argument_list|>
name|servers
parameter_list|)
throws|throws
name|IOException
block|{   }
annotation|@
name|Override
specifier|public
name|RSGroupInfo
name|getRSGroupForTable
parameter_list|(
name|TableName
name|tableName
parameter_list|)
throws|throws
name|IOException
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|balanceRSGroup
parameter_list|(
name|String
name|groupName
parameter_list|)
throws|throws
name|IOException
block|{
throw|throw
operator|new
name|DoNotRetryIOException
argument_list|(
literal|"RSGroup is disabled"
argument_list|)
throw|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setRSGroup
parameter_list|(
name|Set
argument_list|<
name|TableName
argument_list|>
name|tables
parameter_list|,
name|String
name|groupName
parameter_list|)
throws|throws
name|IOException
block|{
throw|throw
operator|new
name|DoNotRetryIOException
argument_list|(
literal|"RSGroup is disabled"
argument_list|)
throw|;
block|}
block|}
end_class

end_unit
