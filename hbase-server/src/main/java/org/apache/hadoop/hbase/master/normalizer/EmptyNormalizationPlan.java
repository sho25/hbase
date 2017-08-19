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
operator|.
name|normalizer
package|;
end_package

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
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|client
operator|.
name|Admin
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
name|normalizer
operator|.
name|NormalizationPlan
operator|.
name|PlanType
import|;
end_import

begin_comment
comment|/**  * Plan which signifies that no normalization is required,  * or normalization of this table isn't allowed, this is singleton.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
specifier|final
class|class
name|EmptyNormalizationPlan
implements|implements
name|NormalizationPlan
block|{
specifier|private
specifier|static
specifier|final
name|EmptyNormalizationPlan
name|instance
init|=
operator|new
name|EmptyNormalizationPlan
argument_list|()
decl_stmt|;
specifier|private
name|EmptyNormalizationPlan
parameter_list|()
block|{   }
comment|/**    * @return singleton instance    */
specifier|public
specifier|static
name|EmptyNormalizationPlan
name|getInstance
parameter_list|()
block|{
return|return
name|instance
return|;
block|}
comment|/**    * No-op for empty plan.    */
annotation|@
name|Override
specifier|public
name|void
name|execute
parameter_list|(
name|Admin
name|admin
parameter_list|)
block|{   }
annotation|@
name|Override
specifier|public
name|PlanType
name|getType
parameter_list|()
block|{
return|return
name|PlanType
operator|.
name|NONE
return|;
block|}
block|}
end_class

end_unit

