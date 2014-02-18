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
name|client
package|;
end_package

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
name|HRegionInfo
import|;
end_import

begin_comment
comment|/**  * A Get, Put, Increment, Append, or Delete associated with it's region.  Used internally by    * {@link HTable#batch} to associate the action with it's region and maintain  * the index from the original request.   */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
comment|//TODO: R is never used
specifier|public
class|class
name|Action
parameter_list|<
name|R
parameter_list|>
implements|implements
name|Comparable
argument_list|<
name|R
argument_list|>
block|{
comment|// TODO: This class should not be visible outside of the client package.
specifier|private
name|Row
name|action
decl_stmt|;
specifier|private
name|int
name|originalIndex
decl_stmt|;
specifier|private
name|long
name|nonce
init|=
name|HConstants
operator|.
name|NO_NONCE
decl_stmt|;
specifier|private
name|int
name|replicaId
init|=
name|RegionReplicaUtil
operator|.
name|DEFAULT_REPLICA_ID
decl_stmt|;
specifier|public
name|Action
parameter_list|(
name|Row
name|action
parameter_list|,
name|int
name|originalIndex
parameter_list|)
block|{
name|super
argument_list|()
expr_stmt|;
name|this
operator|.
name|action
operator|=
name|action
expr_stmt|;
name|this
operator|.
name|originalIndex
operator|=
name|originalIndex
expr_stmt|;
block|}
comment|/**    * Creates an action for a particular replica from original action.    * @param action Original action.    * @param replicaId Replica id for the new action.    */
specifier|public
name|Action
parameter_list|(
name|Action
argument_list|<
name|R
argument_list|>
name|action
parameter_list|,
name|int
name|replicaId
parameter_list|)
block|{
name|super
argument_list|()
expr_stmt|;
name|this
operator|.
name|action
operator|=
name|action
operator|.
name|action
expr_stmt|;
name|this
operator|.
name|nonce
operator|=
name|action
operator|.
name|nonce
expr_stmt|;
name|this
operator|.
name|originalIndex
operator|=
name|action
operator|.
name|originalIndex
expr_stmt|;
name|this
operator|.
name|replicaId
operator|=
name|replicaId
expr_stmt|;
block|}
specifier|public
name|void
name|setNonce
parameter_list|(
name|long
name|nonce
parameter_list|)
block|{
name|this
operator|.
name|nonce
operator|=
name|nonce
expr_stmt|;
block|}
specifier|public
name|boolean
name|hasNonce
parameter_list|()
block|{
return|return
name|nonce
operator|!=
name|HConstants
operator|.
name|NO_NONCE
return|;
block|}
specifier|public
name|Row
name|getAction
parameter_list|()
block|{
return|return
name|action
return|;
block|}
specifier|public
name|int
name|getOriginalIndex
parameter_list|()
block|{
return|return
name|originalIndex
return|;
block|}
specifier|public
name|int
name|getReplicaId
parameter_list|()
block|{
return|return
name|replicaId
return|;
block|}
annotation|@
name|SuppressWarnings
argument_list|(
literal|"rawtypes"
argument_list|)
annotation|@
name|Override
specifier|public
name|int
name|compareTo
parameter_list|(
name|Object
name|o
parameter_list|)
block|{
return|return
name|action
operator|.
name|compareTo
argument_list|(
operator|(
operator|(
name|Action
operator|)
name|o
operator|)
operator|.
name|getAction
argument_list|()
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
return|return
name|this
operator|.
name|action
operator|.
name|hashCode
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
name|obj
parameter_list|)
block|{
if|if
condition|(
name|this
operator|==
name|obj
condition|)
return|return
literal|true
return|;
if|if
condition|(
name|obj
operator|==
literal|null
operator|||
name|getClass
argument_list|()
operator|!=
name|obj
operator|.
name|getClass
argument_list|()
condition|)
return|return
literal|false
return|;
name|Action
argument_list|<
name|?
argument_list|>
name|other
init|=
operator|(
name|Action
argument_list|<
name|?
argument_list|>
operator|)
name|obj
decl_stmt|;
return|return
name|compareTo
argument_list|(
name|other
argument_list|)
operator|==
literal|0
return|;
block|}
specifier|public
name|long
name|getNonce
parameter_list|()
block|{
return|return
name|nonce
return|;
block|}
block|}
end_class

end_unit

