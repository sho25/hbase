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
name|security
operator|.
name|visibility
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
name|HashSet
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
name|security
operator|.
name|User
import|;
end_import

begin_comment
comment|/**  * If the passed in authorization is null, then this ScanLabelGenerator  * feeds the set of predefined authorization labels for the given user. That is  * the set defined by the admin using the VisibilityClient admin interface  * or the set_auths shell command.  * Otherwise the passed in authorization labels are returned with no change.  *  * Note: This SLG should not be used alone because it does not check  * the passed in authorization labels against what the user is authorized for.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|FeedUserAuthScanLabelGenerator
implements|implements
name|ScanLabelGenerator
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
name|FeedUserAuthScanLabelGenerator
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|Configuration
name|conf
decl_stmt|;
specifier|private
name|VisibilityLabelsCache
name|labelsCache
decl_stmt|;
specifier|public
name|FeedUserAuthScanLabelGenerator
parameter_list|()
block|{
name|this
operator|.
name|labelsCache
operator|=
name|VisibilityLabelsCache
operator|.
name|get
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setConf
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Configuration
name|getConf
parameter_list|()
block|{
return|return
name|this
operator|.
name|conf
return|;
block|}
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|getLabels
parameter_list|(
name|User
name|user
parameter_list|,
name|Authorizations
name|authorizations
parameter_list|)
block|{
if|if
condition|(
name|authorizations
operator|==
literal|null
operator|||
name|authorizations
operator|.
name|getLabels
argument_list|()
operator|==
literal|null
operator|||
name|authorizations
operator|.
name|getLabels
argument_list|()
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|String
name|userName
init|=
name|user
operator|.
name|getShortName
argument_list|()
decl_stmt|;
name|Set
argument_list|<
name|String
argument_list|>
name|auths
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
decl_stmt|;
name|auths
operator|.
name|addAll
argument_list|(
name|this
operator|.
name|labelsCache
operator|.
name|getUserAuths
argument_list|(
name|userName
argument_list|)
argument_list|)
expr_stmt|;
name|auths
operator|.
name|addAll
argument_list|(
name|this
operator|.
name|labelsCache
operator|.
name|getGroupAuths
argument_list|(
name|user
operator|.
name|getGroupNames
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
return|return
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|auths
argument_list|)
return|;
block|}
return|return
name|authorizations
operator|.
name|getLabels
argument_list|()
return|;
block|}
block|}
end_class

end_unit

