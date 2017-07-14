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
name|client
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
name|Comparator
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
name|Optional
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
name|TreeMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|function
operator|.
name|Consumer
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|function
operator|.
name|Predicate
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Iterables
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
name|HConstants
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
name|util
operator|.
name|Bytes
import|;
end_import

begin_comment
comment|/**  * Container for Actions (i.e. Get, Delete, or Put), which are grouped by  * regionName. Intended to be used with {@link AsyncProcess}.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
specifier|final
class|class
name|MultiAction
block|{
comment|// TODO: This class should not be visible outside of the client package.
comment|// map of regions to lists of puts/gets/deletes for that region.
specifier|protected
name|Map
argument_list|<
name|byte
index|[]
argument_list|,
name|List
argument_list|<
name|Action
argument_list|>
argument_list|>
name|actions
init|=
operator|new
name|TreeMap
argument_list|<>
argument_list|(
name|Bytes
operator|.
name|BYTES_COMPARATOR
argument_list|)
decl_stmt|;
specifier|private
name|long
name|nonceGroup
init|=
name|HConstants
operator|.
name|NO_NONCE
decl_stmt|;
specifier|public
name|MultiAction
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
block|}
comment|/**    * Get the total number of Actions    *    * @return total number of Actions for all groups in this container.    */
specifier|public
name|int
name|size
parameter_list|()
block|{
name|int
name|size
init|=
literal|0
decl_stmt|;
for|for
control|(
name|List
argument_list|<
name|?
argument_list|>
name|l
range|:
name|actions
operator|.
name|values
argument_list|()
control|)
block|{
name|size
operator|+=
name|l
operator|.
name|size
argument_list|()
expr_stmt|;
block|}
return|return
name|size
return|;
block|}
comment|/**    * Add an Action to this container based on it's regionName. If the regionName    * is wrong, the initial execution will fail, but will be automatically    * retried after looking up the correct region.    *    * @param regionName    * @param a    */
specifier|public
name|void
name|add
parameter_list|(
name|byte
index|[]
name|regionName
parameter_list|,
name|Action
name|a
parameter_list|)
block|{
name|add
argument_list|(
name|regionName
argument_list|,
name|Collections
operator|.
name|singletonList
argument_list|(
name|a
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * Add an Action to this container based on it's regionName. If the regionName    * is wrong, the initial execution will fail, but will be automatically    * retried after looking up the correct region.    *    * @param regionName    * @param actionList list of actions to add for the region    */
specifier|public
name|void
name|add
parameter_list|(
name|byte
index|[]
name|regionName
parameter_list|,
name|List
argument_list|<
name|Action
argument_list|>
name|actionList
parameter_list|)
block|{
name|List
argument_list|<
name|Action
argument_list|>
name|rsActions
init|=
name|actions
operator|.
name|get
argument_list|(
name|regionName
argument_list|)
decl_stmt|;
if|if
condition|(
name|rsActions
operator|==
literal|null
condition|)
block|{
name|rsActions
operator|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|actionList
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|actions
operator|.
name|put
argument_list|(
name|regionName
argument_list|,
name|rsActions
argument_list|)
expr_stmt|;
block|}
name|rsActions
operator|.
name|addAll
argument_list|(
name|actionList
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|setNonceGroup
parameter_list|(
name|long
name|nonceGroup
parameter_list|)
block|{
name|this
operator|.
name|nonceGroup
operator|=
name|nonceGroup
expr_stmt|;
block|}
specifier|public
name|Set
argument_list|<
name|byte
index|[]
argument_list|>
name|getRegions
parameter_list|()
block|{
return|return
name|actions
operator|.
name|keySet
argument_list|()
return|;
block|}
specifier|public
name|boolean
name|hasNonceGroup
parameter_list|()
block|{
return|return
name|nonceGroup
operator|!=
name|HConstants
operator|.
name|NO_NONCE
return|;
block|}
specifier|public
name|long
name|getNonceGroup
parameter_list|()
block|{
return|return
name|this
operator|.
name|nonceGroup
return|;
block|}
comment|// returns the max priority of all the actions
specifier|public
name|int
name|getPriority
parameter_list|()
block|{
name|Optional
argument_list|<
name|Action
argument_list|>
name|result
init|=
name|actions
operator|.
name|values
argument_list|()
operator|.
name|stream
argument_list|()
operator|.
name|flatMap
argument_list|(
name|List
operator|::
name|stream
argument_list|)
operator|.
name|max
argument_list|(
parameter_list|(
name|action1
parameter_list|,
name|action2
parameter_list|)
lambda|->
name|Math
operator|.
name|max
argument_list|(
name|action1
operator|.
name|getPriority
argument_list|()
argument_list|,
name|action2
operator|.
name|getPriority
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
return|return
name|result
operator|.
name|isPresent
argument_list|()
condition|?
name|result
operator|.
name|get
argument_list|()
operator|.
name|getPriority
argument_list|()
else|:
name|HConstants
operator|.
name|PRIORITY_UNSET
return|;
block|}
block|}
end_class

end_unit

