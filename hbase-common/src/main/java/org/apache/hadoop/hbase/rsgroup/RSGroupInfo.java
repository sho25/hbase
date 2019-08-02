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
comment|/**  * Stores the group information of region server groups.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Public
specifier|public
class|class
name|RSGroupInfo
block|{
specifier|public
specifier|static
specifier|final
name|String
name|DEFAULT_GROUP
init|=
literal|"default"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|NAMESPACE_DESC_PROP_GROUP
init|=
literal|"hbase.rsgroup.name"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|TABLE_DESC_PROP_GROUP
init|=
literal|"hbase.rsgroup.name"
decl_stmt|;
specifier|private
specifier|final
name|String
name|name
decl_stmt|;
comment|// Keep servers in a sorted set so has an expected ordering when displayed.
specifier|private
specifier|final
name|SortedSet
argument_list|<
name|Address
argument_list|>
name|servers
decl_stmt|;
comment|// Keep tables sorted too.
comment|/**    * @deprecated Since 3.0.0, will be removed in 4.0.0. The rsgroup information will be stored in    *             the configuration of a table so this will be removed.    */
annotation|@
name|Deprecated
specifier|private
specifier|final
name|SortedSet
argument_list|<
name|TableName
argument_list|>
name|tables
decl_stmt|;
specifier|public
name|RSGroupInfo
parameter_list|(
name|String
name|name
parameter_list|)
block|{
name|this
argument_list|(
name|name
argument_list|,
operator|new
name|TreeSet
argument_list|<
name|Address
argument_list|>
argument_list|()
argument_list|,
operator|new
name|TreeSet
argument_list|<
name|TableName
argument_list|>
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|RSGroupInfo
parameter_list|(
name|String
name|name
parameter_list|,
name|SortedSet
argument_list|<
name|Address
argument_list|>
name|servers
parameter_list|)
block|{
name|this
operator|.
name|name
operator|=
name|name
expr_stmt|;
name|this
operator|.
name|servers
operator|=
name|servers
operator|==
literal|null
condition|?
operator|new
name|TreeSet
argument_list|<>
argument_list|()
else|:
operator|new
name|TreeSet
argument_list|<>
argument_list|(
name|servers
argument_list|)
expr_stmt|;
name|this
operator|.
name|tables
operator|=
operator|new
name|TreeSet
argument_list|<>
argument_list|()
expr_stmt|;
block|}
comment|/**    * @deprecated Since 3.0.0, will be removed in 4.0.0. The rsgroup information for a table will be    *             stored in the configuration of a table so this will be removed.    */
annotation|@
name|Deprecated
name|RSGroupInfo
parameter_list|(
name|String
name|name
parameter_list|,
name|SortedSet
argument_list|<
name|Address
argument_list|>
name|servers
parameter_list|,
name|SortedSet
argument_list|<
name|TableName
argument_list|>
name|tables
parameter_list|)
block|{
name|this
operator|.
name|name
operator|=
name|name
expr_stmt|;
name|this
operator|.
name|servers
operator|=
operator|(
name|servers
operator|==
literal|null
operator|)
condition|?
operator|new
name|TreeSet
argument_list|<>
argument_list|()
else|:
operator|new
name|TreeSet
argument_list|<>
argument_list|(
name|servers
argument_list|)
expr_stmt|;
name|this
operator|.
name|tables
operator|=
operator|(
name|tables
operator|==
literal|null
operator|)
condition|?
operator|new
name|TreeSet
argument_list|<>
argument_list|()
else|:
operator|new
name|TreeSet
argument_list|<>
argument_list|(
name|tables
argument_list|)
expr_stmt|;
block|}
specifier|public
name|RSGroupInfo
parameter_list|(
name|RSGroupInfo
name|src
parameter_list|)
block|{
name|this
argument_list|(
name|src
operator|.
name|name
argument_list|,
name|src
operator|.
name|servers
argument_list|,
name|src
operator|.
name|tables
argument_list|)
expr_stmt|;
block|}
comment|/**    * Get group name.    */
specifier|public
name|String
name|getName
parameter_list|()
block|{
return|return
name|name
return|;
block|}
comment|/**    * Adds the given server to the group.    */
specifier|public
name|void
name|addServer
parameter_list|(
name|Address
name|hostPort
parameter_list|)
block|{
name|servers
operator|.
name|add
argument_list|(
name|hostPort
argument_list|)
expr_stmt|;
block|}
comment|/**    * Adds the given servers to the group.    */
specifier|public
name|void
name|addAllServers
parameter_list|(
name|Collection
argument_list|<
name|Address
argument_list|>
name|hostPort
parameter_list|)
block|{
name|servers
operator|.
name|addAll
argument_list|(
name|hostPort
argument_list|)
expr_stmt|;
block|}
comment|/**    * @param hostPort hostPort of the server    * @return true, if a server with hostPort is found    */
specifier|public
name|boolean
name|containsServer
parameter_list|(
name|Address
name|hostPort
parameter_list|)
block|{
return|return
name|servers
operator|.
name|contains
argument_list|(
name|hostPort
argument_list|)
return|;
block|}
comment|/**    * Get list of servers.    */
specifier|public
name|Set
argument_list|<
name|Address
argument_list|>
name|getServers
parameter_list|()
block|{
return|return
name|servers
return|;
block|}
comment|/**    * Remove given server from the group.    */
specifier|public
name|boolean
name|removeServer
parameter_list|(
name|Address
name|hostPort
parameter_list|)
block|{
return|return
name|servers
operator|.
name|remove
argument_list|(
name|hostPort
argument_list|)
return|;
block|}
comment|/**    * Get set of tables that are members of the group.    * @deprecated Since 3.0.0, will be removed in 4.0.0. The rsgroup information will be stored in    *             the configuration of a table so this will be removed.    */
annotation|@
name|Deprecated
specifier|public
name|SortedSet
argument_list|<
name|TableName
argument_list|>
name|getTables
parameter_list|()
block|{
return|return
name|tables
return|;
block|}
comment|/**    * @deprecated Since 3.0.0, will be removed in 4.0.0. The rsgroup information will be stored in    *             the configuration of a table so this will be removed.    */
annotation|@
name|Deprecated
specifier|public
name|void
name|addTable
parameter_list|(
name|TableName
name|table
parameter_list|)
block|{
name|tables
operator|.
name|add
argument_list|(
name|table
argument_list|)
expr_stmt|;
block|}
comment|/**    * @deprecated Since 3.0.0, will be removed in 4.0.0. The rsgroup information will be stored in    *             the configuration of a table so this will be removed.    */
annotation|@
name|Deprecated
specifier|public
name|void
name|addAllTables
parameter_list|(
name|Collection
argument_list|<
name|TableName
argument_list|>
name|arg
parameter_list|)
block|{
name|tables
operator|.
name|addAll
argument_list|(
name|arg
argument_list|)
expr_stmt|;
block|}
comment|/**    * @deprecated Since 3.0.0, will be removed in 4.0.0. The rsgroup information will be stored in    *             the configuration of a table so this will be removed.    */
annotation|@
name|Deprecated
specifier|public
name|boolean
name|containsTable
parameter_list|(
name|TableName
name|table
parameter_list|)
block|{
return|return
name|tables
operator|.
name|contains
argument_list|(
name|table
argument_list|)
return|;
block|}
comment|/**    * @deprecated Since 3.0.0, will be removed in 4.0.0. The rsgroup information will be stored in    *             the configuration of a table so this will be removed.    */
annotation|@
name|Deprecated
specifier|public
name|boolean
name|removeTable
parameter_list|(
name|TableName
name|table
parameter_list|)
block|{
return|return
name|tables
operator|.
name|remove
argument_list|(
name|table
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
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
name|sb
operator|.
name|append
argument_list|(
literal|"Name:"
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|this
operator|.
name|name
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|", "
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|" Servers:"
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|this
operator|.
name|servers
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|", "
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|" Tables:"
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|this
operator|.
name|tables
argument_list|)
expr_stmt|;
return|return
name|sb
operator|.
name|toString
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|equals
parameter_list|(
name|Object
name|o
parameter_list|)
block|{
if|if
condition|(
name|this
operator|==
name|o
condition|)
block|{
return|return
literal|true
return|;
block|}
if|if
condition|(
name|o
operator|==
literal|null
operator|||
name|getClass
argument_list|()
operator|!=
name|o
operator|.
name|getClass
argument_list|()
condition|)
block|{
return|return
literal|false
return|;
block|}
name|RSGroupInfo
name|RSGroupInfo
init|=
operator|(
name|RSGroupInfo
operator|)
name|o
decl_stmt|;
if|if
condition|(
operator|!
name|name
operator|.
name|equals
argument_list|(
name|RSGroupInfo
operator|.
name|name
argument_list|)
condition|)
block|{
return|return
literal|false
return|;
block|}
if|if
condition|(
operator|!
name|servers
operator|.
name|equals
argument_list|(
name|RSGroupInfo
operator|.
name|servers
argument_list|)
condition|)
block|{
return|return
literal|false
return|;
block|}
if|if
condition|(
operator|!
name|tables
operator|.
name|equals
argument_list|(
name|RSGroupInfo
operator|.
name|tables
argument_list|)
condition|)
block|{
return|return
literal|false
return|;
block|}
return|return
literal|true
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
name|int
name|result
init|=
name|servers
operator|.
name|hashCode
argument_list|()
decl_stmt|;
name|result
operator|=
literal|31
operator|*
name|result
operator|+
name|tables
operator|.
name|hashCode
argument_list|()
expr_stmt|;
name|result
operator|=
literal|31
operator|*
name|result
operator|+
name|name
operator|.
name|hashCode
argument_list|()
expr_stmt|;
return|return
name|result
return|;
block|}
block|}
end_class

end_unit

