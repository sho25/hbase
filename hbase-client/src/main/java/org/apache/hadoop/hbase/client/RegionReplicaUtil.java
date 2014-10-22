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
name|client
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
name|Iterator
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

begin_comment
comment|/**  * Utility methods which contain the logic for regions and replicas.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|RegionReplicaUtil
block|{
comment|/**    * The default replicaId for the region    */
specifier|static
specifier|final
name|int
name|DEFAULT_REPLICA_ID
init|=
literal|0
decl_stmt|;
comment|/**    * Returns the HRegionInfo for the given replicaId. HRegionInfo's correspond to    * a range of a table, but more than one "instance" of the same range can be    * deployed which are differentiated by the replicaId.    * @param replicaId the replicaId to use    * @return an HRegionInfo object corresponding to the same range (table, start and    * end key), but for the given replicaId.    */
specifier|public
specifier|static
name|HRegionInfo
name|getRegionInfoForReplica
parameter_list|(
name|HRegionInfo
name|regionInfo
parameter_list|,
name|int
name|replicaId
parameter_list|)
block|{
if|if
condition|(
name|regionInfo
operator|.
name|getReplicaId
argument_list|()
operator|==
name|replicaId
condition|)
block|{
return|return
name|regionInfo
return|;
block|}
name|HRegionInfo
name|replicaInfo
init|=
operator|new
name|HRegionInfo
argument_list|(
name|regionInfo
operator|.
name|getTable
argument_list|()
argument_list|,
name|regionInfo
operator|.
name|getStartKey
argument_list|()
argument_list|,
name|regionInfo
operator|.
name|getEndKey
argument_list|()
argument_list|,
name|regionInfo
operator|.
name|isSplit
argument_list|()
argument_list|,
name|regionInfo
operator|.
name|getRegionId
argument_list|()
argument_list|,
name|replicaId
argument_list|)
decl_stmt|;
name|replicaInfo
operator|.
name|setOffline
argument_list|(
name|regionInfo
operator|.
name|isOffline
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|replicaInfo
return|;
block|}
comment|/**    * Returns the HRegionInfo for the default replicaId (0). HRegionInfo's correspond to    * a range of a table, but more than one "instance" of the same range can be    * deployed which are differentiated by the replicaId.    * @return an HRegionInfo object corresponding to the same range (table, start and    * end key), but for the default replicaId.    */
specifier|public
specifier|static
name|HRegionInfo
name|getRegionInfoForDefaultReplica
parameter_list|(
name|HRegionInfo
name|regionInfo
parameter_list|)
block|{
return|return
name|getRegionInfoForReplica
argument_list|(
name|regionInfo
argument_list|,
name|DEFAULT_REPLICA_ID
argument_list|)
return|;
block|}
comment|/** @return true if this replicaId corresponds to default replica for the region */
specifier|public
specifier|static
name|boolean
name|isDefaultReplica
parameter_list|(
name|int
name|replicaId
parameter_list|)
block|{
return|return
name|DEFAULT_REPLICA_ID
operator|==
name|replicaId
return|;
block|}
comment|/** @return true if this region is a default replica for the region */
specifier|public
specifier|static
name|boolean
name|isDefaultReplica
parameter_list|(
name|HRegionInfo
name|hri
parameter_list|)
block|{
return|return
name|hri
operator|.
name|getReplicaId
argument_list|()
operator|==
name|DEFAULT_REPLICA_ID
return|;
block|}
comment|/**    * Removes the non-default replicas from the passed regions collection    * @param regions    */
specifier|public
specifier|static
name|void
name|removeNonDefaultRegions
parameter_list|(
name|Collection
argument_list|<
name|HRegionInfo
argument_list|>
name|regions
parameter_list|)
block|{
name|Iterator
argument_list|<
name|HRegionInfo
argument_list|>
name|iterator
init|=
name|regions
operator|.
name|iterator
argument_list|()
decl_stmt|;
while|while
condition|(
name|iterator
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|HRegionInfo
name|hri
init|=
name|iterator
operator|.
name|next
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|RegionReplicaUtil
operator|.
name|isDefaultReplica
argument_list|(
name|hri
argument_list|)
condition|)
block|{
name|iterator
operator|.
name|remove
argument_list|()
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit

