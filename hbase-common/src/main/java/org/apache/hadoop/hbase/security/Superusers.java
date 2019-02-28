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
name|security
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
name|Set
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
name|conf
operator|.
name|Configuration
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
name|AuthUtil
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
comment|/**  * Keeps lists of superusers and super groups loaded from HBase configuration,  * checks if certain user is regarded as superuser.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
specifier|final
class|class
name|Superusers
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
name|Superusers
operator|.
name|class
argument_list|)
decl_stmt|;
comment|/** Configuration key for superusers */
specifier|public
specifier|static
specifier|final
name|String
name|SUPERUSER_CONF_KEY
init|=
literal|"hbase.superuser"
decl_stmt|;
comment|// Not getting a name
specifier|private
specifier|static
name|Set
argument_list|<
name|String
argument_list|>
name|superUsers
decl_stmt|;
specifier|private
specifier|static
name|Set
argument_list|<
name|String
argument_list|>
name|superGroups
decl_stmt|;
specifier|private
specifier|static
name|User
name|systemUser
decl_stmt|;
specifier|private
name|Superusers
parameter_list|()
block|{}
comment|/**    * Should be called only once to pre-load list of super users and super    * groups from Configuration. This operation is idempotent.    * @param conf configuration to load users from    * @throws IOException if unable to initialize lists of superusers or super groups    * @throws IllegalStateException if current user is null    */
specifier|public
specifier|static
name|void
name|initialize
parameter_list|(
name|Configuration
name|conf
parameter_list|)
throws|throws
name|IOException
block|{
name|superUsers
operator|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
expr_stmt|;
name|superGroups
operator|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
expr_stmt|;
name|systemUser
operator|=
name|User
operator|.
name|getCurrent
argument_list|()
expr_stmt|;
if|if
condition|(
name|systemUser
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"Unable to obtain the current user, "
operator|+
literal|"authorization checks for internal operations will not work correctly!"
argument_list|)
throw|;
block|}
name|String
name|currentUser
init|=
name|systemUser
operator|.
name|getShortName
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|trace
argument_list|(
literal|"Current user name is {}"
argument_list|,
name|currentUser
argument_list|)
expr_stmt|;
name|superUsers
operator|.
name|add
argument_list|(
name|currentUser
argument_list|)
expr_stmt|;
name|String
index|[]
name|superUserList
init|=
name|conf
operator|.
name|getStrings
argument_list|(
name|SUPERUSER_CONF_KEY
argument_list|,
operator|new
name|String
index|[
literal|0
index|]
argument_list|)
decl_stmt|;
for|for
control|(
name|String
name|name
range|:
name|superUserList
control|)
block|{
if|if
condition|(
name|AuthUtil
operator|.
name|isGroupPrincipal
argument_list|(
name|name
argument_list|)
condition|)
block|{
comment|// Let's keep the '@' for distinguishing from user.
name|superGroups
operator|.
name|add
argument_list|(
name|name
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|superUsers
operator|.
name|add
argument_list|(
name|name
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|/**    * @return true if current user is a super user (whether as user running process,    * declared as individual superuser or member of supergroup), false otherwise.    * @param user to check    * @throws IllegalStateException if lists of superusers/super groups    *   haven't been initialized properly    */
specifier|public
specifier|static
name|boolean
name|isSuperUser
parameter_list|(
name|User
name|user
parameter_list|)
block|{
if|if
condition|(
name|superUsers
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"Super users/super groups lists"
operator|+
literal|" have not been initialized properly."
argument_list|)
throw|;
block|}
if|if
condition|(
name|superUsers
operator|.
name|contains
argument_list|(
name|user
operator|.
name|getShortName
argument_list|()
argument_list|)
condition|)
block|{
return|return
literal|true
return|;
block|}
for|for
control|(
name|String
name|group
range|:
name|user
operator|.
name|getGroupNames
argument_list|()
control|)
block|{
if|if
condition|(
name|superGroups
operator|.
name|contains
argument_list|(
name|AuthUtil
operator|.
name|toGroupEntry
argument_list|(
name|group
argument_list|)
argument_list|)
condition|)
block|{
return|return
literal|true
return|;
block|}
block|}
return|return
literal|false
return|;
block|}
comment|/**    * @return true if current user is a super user, false otherwise.    * @param user to check    */
specifier|public
specifier|static
name|boolean
name|isSuperUser
parameter_list|(
name|String
name|user
parameter_list|)
block|{
return|return
name|superUsers
operator|.
name|contains
argument_list|(
name|user
argument_list|)
operator|||
name|superGroups
operator|.
name|contains
argument_list|(
name|user
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|Collection
argument_list|<
name|String
argument_list|>
name|getSuperUsers
parameter_list|()
block|{
return|return
name|superUsers
return|;
block|}
specifier|public
specifier|static
name|Collection
argument_list|<
name|String
argument_list|>
name|getSuperGroups
parameter_list|()
block|{
return|return
name|superGroups
return|;
block|}
specifier|public
specifier|static
name|User
name|getSystemUser
parameter_list|()
block|{
return|return
name|systemUser
return|;
block|}
block|}
end_class

end_unit

