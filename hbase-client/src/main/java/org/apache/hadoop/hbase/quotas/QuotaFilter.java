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
name|quotas
package|;
end_package

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
name|classification
operator|.
name|InterfaceStability
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
name|Strings
import|;
end_import

begin_comment
comment|/**  * Filter to use to filter the QuotaRetriever results.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Public
annotation|@
name|InterfaceStability
operator|.
name|Evolving
specifier|public
class|class
name|QuotaFilter
block|{
specifier|private
name|Set
argument_list|<
name|QuotaType
argument_list|>
name|types
init|=
operator|new
name|HashSet
argument_list|<
name|QuotaType
argument_list|>
argument_list|()
decl_stmt|;
specifier|private
name|boolean
name|hasFilters
init|=
literal|false
decl_stmt|;
specifier|private
name|String
name|namespaceRegex
decl_stmt|;
specifier|private
name|String
name|tableRegex
decl_stmt|;
specifier|private
name|String
name|userRegex
decl_stmt|;
specifier|public
name|QuotaFilter
parameter_list|()
block|{   }
comment|/**    * Set the user filter regex    * @param regex the user filter    * @return the quota filter object    */
specifier|public
name|QuotaFilter
name|setUserFilter
parameter_list|(
specifier|final
name|String
name|regex
parameter_list|)
block|{
name|this
operator|.
name|userRegex
operator|=
name|regex
expr_stmt|;
name|hasFilters
operator||=
operator|!
name|Strings
operator|.
name|isEmpty
argument_list|(
name|regex
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**    * Set the table filter regex    * @param regex the table filter    * @return the quota filter object    */
specifier|public
name|QuotaFilter
name|setTableFilter
parameter_list|(
specifier|final
name|String
name|regex
parameter_list|)
block|{
name|this
operator|.
name|tableRegex
operator|=
name|regex
expr_stmt|;
name|hasFilters
operator||=
operator|!
name|Strings
operator|.
name|isEmpty
argument_list|(
name|regex
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**    * Set the namespace filter regex    * @param regex the namespace filter    * @return the quota filter object    */
specifier|public
name|QuotaFilter
name|setNamespaceFilter
parameter_list|(
specifier|final
name|String
name|regex
parameter_list|)
block|{
name|this
operator|.
name|namespaceRegex
operator|=
name|regex
expr_stmt|;
name|hasFilters
operator||=
operator|!
name|Strings
operator|.
name|isEmpty
argument_list|(
name|regex
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**    * Add a type to the filter list    * @param type the type to filter on    * @return the quota filter object    */
specifier|public
name|QuotaFilter
name|addTypeFilter
parameter_list|(
specifier|final
name|QuotaType
name|type
parameter_list|)
block|{
name|this
operator|.
name|types
operator|.
name|add
argument_list|(
name|type
argument_list|)
expr_stmt|;
name|hasFilters
operator||=
literal|true
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/** @return true if the filter is empty */
specifier|public
name|boolean
name|isNull
parameter_list|()
block|{
return|return
operator|!
name|hasFilters
return|;
block|}
comment|/** @return the QuotaType types that we want to filter one */
specifier|public
name|Set
argument_list|<
name|QuotaType
argument_list|>
name|getTypeFilters
parameter_list|()
block|{
return|return
name|types
return|;
block|}
comment|/** @return the Namespace filter regex */
specifier|public
name|String
name|getNamespaceFilter
parameter_list|()
block|{
return|return
name|namespaceRegex
return|;
block|}
comment|/** @return the Table filter regex */
specifier|public
name|String
name|getTableFilter
parameter_list|()
block|{
return|return
name|tableRegex
return|;
block|}
comment|/** @return the User filter regex */
specifier|public
name|String
name|getUserFilter
parameter_list|()
block|{
return|return
name|userRegex
return|;
block|}
block|}
end_class

end_unit

