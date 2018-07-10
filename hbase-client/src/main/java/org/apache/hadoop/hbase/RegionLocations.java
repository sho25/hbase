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
name|RegionInfo
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
name|RegionReplicaUtil
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
comment|/**  * Container for holding a list of {@link HRegionLocation}'s that correspond to the  * same range. The list is indexed by the replicaId. This is an immutable list,  * however mutation operations are provided which returns a new List via copy-on-write  * (assuming small number of locations)  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|RegionLocations
block|{
specifier|private
specifier|final
name|int
name|numNonNullElements
decl_stmt|;
comment|// locations array contains the HRL objects for known region replicas indexes by the replicaId.
comment|// elements can be null if the region replica is not known at all. A null value indicates
comment|// that there is a region replica with the index as replicaId, but the location is not known
comment|// in the cache.
specifier|private
specifier|final
name|HRegionLocation
index|[]
name|locations
decl_stmt|;
comment|// replicaId -> HRegionLocation.
comment|/**    * Constructs the region location list. The locations array should    * contain all the locations for known replicas for the region, and should be    * sorted in replicaId ascending order, although it can contain nulls indicating replicaIds    * that the locations of which are not known.    * @param locations an array of HRegionLocations for the same region range    */
specifier|public
name|RegionLocations
parameter_list|(
name|HRegionLocation
modifier|...
name|locations
parameter_list|)
block|{
name|int
name|numNonNullElements
init|=
literal|0
decl_stmt|;
name|int
name|maxReplicaId
init|=
operator|-
literal|1
decl_stmt|;
name|int
name|maxReplicaIdIndex
init|=
operator|-
literal|1
decl_stmt|;
name|int
name|index
init|=
literal|0
decl_stmt|;
for|for
control|(
name|HRegionLocation
name|loc
range|:
name|locations
control|)
block|{
if|if
condition|(
name|loc
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|loc
operator|.
name|getRegionInfo
argument_list|()
operator|.
name|getReplicaId
argument_list|()
operator|>=
name|maxReplicaId
condition|)
block|{
name|maxReplicaId
operator|=
name|loc
operator|.
name|getRegionInfo
argument_list|()
operator|.
name|getReplicaId
argument_list|()
expr_stmt|;
name|maxReplicaIdIndex
operator|=
name|index
expr_stmt|;
block|}
block|}
name|index
operator|++
expr_stmt|;
block|}
comment|// account for the null elements in the array after maxReplicaIdIndex
name|maxReplicaId
operator|=
name|maxReplicaId
operator|+
operator|(
name|locations
operator|.
name|length
operator|-
operator|(
name|maxReplicaIdIndex
operator|+
literal|1
operator|)
operator|)
expr_stmt|;
if|if
condition|(
name|maxReplicaId
operator|+
literal|1
operator|==
name|locations
operator|.
name|length
condition|)
block|{
name|this
operator|.
name|locations
operator|=
name|locations
expr_stmt|;
block|}
else|else
block|{
name|this
operator|.
name|locations
operator|=
operator|new
name|HRegionLocation
index|[
name|maxReplicaId
operator|+
literal|1
index|]
expr_stmt|;
for|for
control|(
name|HRegionLocation
name|loc
range|:
name|locations
control|)
block|{
if|if
condition|(
name|loc
operator|!=
literal|null
condition|)
block|{
name|this
operator|.
name|locations
index|[
name|loc
operator|.
name|getRegionInfo
argument_list|()
operator|.
name|getReplicaId
argument_list|()
index|]
operator|=
name|loc
expr_stmt|;
block|}
block|}
block|}
for|for
control|(
name|HRegionLocation
name|loc
range|:
name|this
operator|.
name|locations
control|)
block|{
if|if
condition|(
name|loc
operator|!=
literal|null
operator|&&
name|loc
operator|.
name|getServerName
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|numNonNullElements
operator|++
expr_stmt|;
block|}
block|}
name|this
operator|.
name|numNonNullElements
operator|=
name|numNonNullElements
expr_stmt|;
block|}
specifier|public
name|RegionLocations
parameter_list|(
name|Collection
argument_list|<
name|HRegionLocation
argument_list|>
name|locations
parameter_list|)
block|{
name|this
argument_list|(
name|locations
operator|.
name|toArray
argument_list|(
operator|new
name|HRegionLocation
index|[
name|locations
operator|.
name|size
argument_list|()
index|]
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * Returns the size of the list even if some of the elements    * might be null.    * @return the size of the list (corresponding to the max replicaId)    */
specifier|public
name|int
name|size
parameter_list|()
block|{
return|return
name|locations
operator|.
name|length
return|;
block|}
comment|/**    * Returns the size of not-null locations    * @return the size of not-null locations    */
specifier|public
name|int
name|numNonNullElements
parameter_list|()
block|{
return|return
name|numNonNullElements
return|;
block|}
comment|/**    * Returns whether there are non-null elements in the list    * @return whether there are non-null elements in the list    */
specifier|public
name|boolean
name|isEmpty
parameter_list|()
block|{
return|return
name|numNonNullElements
operator|==
literal|0
return|;
block|}
comment|/**    * Returns a new RegionLocations with the locations removed (set to null)    * which have the destination server as given.    * @param serverName the serverName to remove locations of    * @return an RegionLocations object with removed locations or the same object    * if nothing is removed    */
specifier|public
name|RegionLocations
name|removeByServer
parameter_list|(
name|ServerName
name|serverName
parameter_list|)
block|{
name|HRegionLocation
index|[]
name|newLocations
init|=
literal|null
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
name|locations
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
comment|// check whether something to remove
if|if
condition|(
name|locations
index|[
name|i
index|]
operator|!=
literal|null
operator|&&
name|serverName
operator|.
name|equals
argument_list|(
name|locations
index|[
name|i
index|]
operator|.
name|getServerName
argument_list|()
argument_list|)
condition|)
block|{
if|if
condition|(
name|newLocations
operator|==
literal|null
condition|)
block|{
comment|//first time
name|newLocations
operator|=
operator|new
name|HRegionLocation
index|[
name|locations
operator|.
name|length
index|]
expr_stmt|;
name|System
operator|.
name|arraycopy
argument_list|(
name|locations
argument_list|,
literal|0
argument_list|,
name|newLocations
argument_list|,
literal|0
argument_list|,
name|i
argument_list|)
expr_stmt|;
block|}
name|newLocations
index|[
name|i
index|]
operator|=
literal|null
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|newLocations
operator|!=
literal|null
condition|)
block|{
name|newLocations
index|[
name|i
index|]
operator|=
name|locations
index|[
name|i
index|]
expr_stmt|;
block|}
block|}
return|return
name|newLocations
operator|==
literal|null
condition|?
name|this
else|:
operator|new
name|RegionLocations
argument_list|(
name|newLocations
argument_list|)
return|;
block|}
comment|/**    * Removes the given location from the list    * @param location the location to remove    * @return an RegionLocations object with removed locations or the same object    * if nothing is removed    */
specifier|public
name|RegionLocations
name|remove
parameter_list|(
name|HRegionLocation
name|location
parameter_list|)
block|{
if|if
condition|(
name|location
operator|==
literal|null
condition|)
return|return
name|this
return|;
if|if
condition|(
name|location
operator|.
name|getRegion
argument_list|()
operator|==
literal|null
condition|)
return|return
name|this
return|;
name|int
name|replicaId
init|=
name|location
operator|.
name|getRegionInfo
argument_list|()
operator|.
name|getReplicaId
argument_list|()
decl_stmt|;
if|if
condition|(
name|replicaId
operator|>=
name|locations
operator|.
name|length
condition|)
return|return
name|this
return|;
comment|// check whether something to remove. HRL.compareTo() compares ONLY the
comment|// serverName. We want to compare the HRI's as well.
if|if
condition|(
name|locations
index|[
name|replicaId
index|]
operator|==
literal|null
operator|||
name|RegionInfo
operator|.
name|COMPARATOR
operator|.
name|compare
argument_list|(
name|location
operator|.
name|getRegion
argument_list|()
argument_list|,
name|locations
index|[
name|replicaId
index|]
operator|.
name|getRegion
argument_list|()
argument_list|)
operator|!=
literal|0
operator|||
operator|!
name|location
operator|.
name|equals
argument_list|(
name|locations
index|[
name|replicaId
index|]
argument_list|)
condition|)
block|{
return|return
name|this
return|;
block|}
name|HRegionLocation
index|[]
name|newLocations
init|=
operator|new
name|HRegionLocation
index|[
name|locations
operator|.
name|length
index|]
decl_stmt|;
name|System
operator|.
name|arraycopy
argument_list|(
name|locations
argument_list|,
literal|0
argument_list|,
name|newLocations
argument_list|,
literal|0
argument_list|,
name|locations
operator|.
name|length
argument_list|)
expr_stmt|;
name|newLocations
index|[
name|replicaId
index|]
operator|=
literal|null
expr_stmt|;
return|return
operator|new
name|RegionLocations
argument_list|(
name|newLocations
argument_list|)
return|;
block|}
comment|/**    * Removes location of the given replicaId from the list    * @param replicaId the replicaId of the location to remove    * @return an RegionLocations object with removed locations or the same object    * if nothing is removed    */
specifier|public
name|RegionLocations
name|remove
parameter_list|(
name|int
name|replicaId
parameter_list|)
block|{
if|if
condition|(
name|getRegionLocation
argument_list|(
name|replicaId
argument_list|)
operator|==
literal|null
condition|)
block|{
return|return
name|this
return|;
block|}
name|HRegionLocation
index|[]
name|newLocations
init|=
operator|new
name|HRegionLocation
index|[
name|locations
operator|.
name|length
index|]
decl_stmt|;
name|System
operator|.
name|arraycopy
argument_list|(
name|locations
argument_list|,
literal|0
argument_list|,
name|newLocations
argument_list|,
literal|0
argument_list|,
name|locations
operator|.
name|length
argument_list|)
expr_stmt|;
if|if
condition|(
name|replicaId
operator|<
name|newLocations
operator|.
name|length
condition|)
block|{
name|newLocations
index|[
name|replicaId
index|]
operator|=
literal|null
expr_stmt|;
block|}
return|return
operator|new
name|RegionLocations
argument_list|(
name|newLocations
argument_list|)
return|;
block|}
comment|/**    * Merges this RegionLocations list with the given list assuming    * same range, and keeping the most up to date version of the    * HRegionLocation entries from either list according to seqNum. If seqNums    * are equal, the location from the argument (other) is taken.    * @param other the locations to merge with    * @return an RegionLocations object with merged locations or the same object    * if nothing is merged    */
specifier|public
name|RegionLocations
name|mergeLocations
parameter_list|(
name|RegionLocations
name|other
parameter_list|)
block|{
assert|assert
name|other
operator|!=
literal|null
assert|;
name|HRegionLocation
index|[]
name|newLocations
init|=
literal|null
decl_stmt|;
comment|// Use the length from other, since it is coming from meta. Otherwise,
comment|// in case of region replication going down, we might have a leak here.
name|int
name|max
init|=
name|other
operator|.
name|locations
operator|.
name|length
decl_stmt|;
name|HRegionInfo
name|regionInfo
init|=
literal|null
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
name|max
condition|;
name|i
operator|++
control|)
block|{
name|HRegionLocation
name|thisLoc
init|=
name|this
operator|.
name|getRegionLocation
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|HRegionLocation
name|otherLoc
init|=
name|other
operator|.
name|getRegionLocation
argument_list|(
name|i
argument_list|)
decl_stmt|;
if|if
condition|(
name|regionInfo
operator|==
literal|null
operator|&&
name|otherLoc
operator|!=
literal|null
operator|&&
name|otherLoc
operator|.
name|getRegionInfo
argument_list|()
operator|!=
literal|null
condition|)
block|{
comment|// regionInfo is the first non-null HRI from other RegionLocations. We use it to ensure that
comment|// all replica region infos belong to the same region with same region id.
name|regionInfo
operator|=
name|otherLoc
operator|.
name|getRegionInfo
argument_list|()
expr_stmt|;
block|}
name|HRegionLocation
name|selectedLoc
init|=
name|selectRegionLocation
argument_list|(
name|thisLoc
argument_list|,
name|otherLoc
argument_list|,
literal|true
argument_list|,
literal|false
argument_list|)
decl_stmt|;
if|if
condition|(
name|selectedLoc
operator|!=
name|thisLoc
condition|)
block|{
if|if
condition|(
name|newLocations
operator|==
literal|null
condition|)
block|{
name|newLocations
operator|=
operator|new
name|HRegionLocation
index|[
name|max
index|]
expr_stmt|;
name|System
operator|.
name|arraycopy
argument_list|(
name|locations
argument_list|,
literal|0
argument_list|,
name|newLocations
argument_list|,
literal|0
argument_list|,
name|i
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|newLocations
operator|!=
literal|null
condition|)
block|{
name|newLocations
index|[
name|i
index|]
operator|=
name|selectedLoc
expr_stmt|;
block|}
block|}
comment|// ensure that all replicas share the same start code. Otherwise delete them
if|if
condition|(
name|newLocations
operator|!=
literal|null
operator|&&
name|regionInfo
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|newLocations
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|newLocations
index|[
name|i
index|]
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
operator|!
name|RegionReplicaUtil
operator|.
name|isReplicasForSameRegion
argument_list|(
name|regionInfo
argument_list|,
name|newLocations
index|[
name|i
index|]
operator|.
name|getRegionInfo
argument_list|()
argument_list|)
condition|)
block|{
name|newLocations
index|[
name|i
index|]
operator|=
literal|null
expr_stmt|;
block|}
block|}
block|}
block|}
return|return
name|newLocations
operator|==
literal|null
condition|?
name|this
else|:
operator|new
name|RegionLocations
argument_list|(
name|newLocations
argument_list|)
return|;
block|}
specifier|private
name|HRegionLocation
name|selectRegionLocation
parameter_list|(
name|HRegionLocation
name|oldLocation
parameter_list|,
name|HRegionLocation
name|location
parameter_list|,
name|boolean
name|checkForEquals
parameter_list|,
name|boolean
name|force
parameter_list|)
block|{
if|if
condition|(
name|location
operator|==
literal|null
condition|)
block|{
return|return
name|oldLocation
operator|==
literal|null
condition|?
literal|null
else|:
name|oldLocation
return|;
block|}
if|if
condition|(
name|oldLocation
operator|==
literal|null
condition|)
block|{
return|return
name|location
return|;
block|}
if|if
condition|(
name|force
operator|||
name|isGreaterThan
argument_list|(
name|location
operator|.
name|getSeqNum
argument_list|()
argument_list|,
name|oldLocation
operator|.
name|getSeqNum
argument_list|()
argument_list|,
name|checkForEquals
argument_list|)
condition|)
block|{
return|return
name|location
return|;
block|}
return|return
name|oldLocation
return|;
block|}
comment|/**    * Updates the location with new only if the new location has a higher    * seqNum than the old one or force is true.    * @param location the location to add or update    * @param checkForEquals whether to update the location if seqNums for the    * HRegionLocations for the old and new location are the same    * @param force whether to force update    * @return an RegionLocations object with updated locations or the same object    * if nothing is updated    */
specifier|public
name|RegionLocations
name|updateLocation
parameter_list|(
name|HRegionLocation
name|location
parameter_list|,
name|boolean
name|checkForEquals
parameter_list|,
name|boolean
name|force
parameter_list|)
block|{
assert|assert
name|location
operator|!=
literal|null
assert|;
name|int
name|replicaId
init|=
name|location
operator|.
name|getRegionInfo
argument_list|()
operator|.
name|getReplicaId
argument_list|()
decl_stmt|;
name|HRegionLocation
name|oldLoc
init|=
name|getRegionLocation
argument_list|(
name|location
operator|.
name|getRegionInfo
argument_list|()
operator|.
name|getReplicaId
argument_list|()
argument_list|)
decl_stmt|;
name|HRegionLocation
name|selectedLoc
init|=
name|selectRegionLocation
argument_list|(
name|oldLoc
argument_list|,
name|location
argument_list|,
name|checkForEquals
argument_list|,
name|force
argument_list|)
decl_stmt|;
if|if
condition|(
name|selectedLoc
operator|==
name|oldLoc
condition|)
block|{
return|return
name|this
return|;
block|}
name|HRegionLocation
index|[]
name|newLocations
init|=
operator|new
name|HRegionLocation
index|[
name|Math
operator|.
name|max
argument_list|(
name|locations
operator|.
name|length
argument_list|,
name|replicaId
operator|+
literal|1
argument_list|)
index|]
decl_stmt|;
name|System
operator|.
name|arraycopy
argument_list|(
name|locations
argument_list|,
literal|0
argument_list|,
name|newLocations
argument_list|,
literal|0
argument_list|,
name|locations
operator|.
name|length
argument_list|)
expr_stmt|;
name|newLocations
index|[
name|replicaId
index|]
operator|=
name|location
expr_stmt|;
comment|// ensure that all replicas share the same start code. Otherwise delete them
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|newLocations
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|newLocations
index|[
name|i
index|]
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
operator|!
name|RegionReplicaUtil
operator|.
name|isReplicasForSameRegion
argument_list|(
name|location
operator|.
name|getRegionInfo
argument_list|()
argument_list|,
name|newLocations
index|[
name|i
index|]
operator|.
name|getRegionInfo
argument_list|()
argument_list|)
condition|)
block|{
name|newLocations
index|[
name|i
index|]
operator|=
literal|null
expr_stmt|;
block|}
block|}
block|}
return|return
operator|new
name|RegionLocations
argument_list|(
name|newLocations
argument_list|)
return|;
block|}
specifier|private
name|boolean
name|isGreaterThan
parameter_list|(
name|long
name|a
parameter_list|,
name|long
name|b
parameter_list|,
name|boolean
name|checkForEquals
parameter_list|)
block|{
return|return
name|a
operator|>
name|b
operator|||
operator|(
name|checkForEquals
operator|&&
operator|(
name|a
operator|==
name|b
operator|)
operator|)
return|;
block|}
specifier|public
name|HRegionLocation
name|getRegionLocation
parameter_list|(
name|int
name|replicaId
parameter_list|)
block|{
if|if
condition|(
name|replicaId
operator|>=
name|locations
operator|.
name|length
condition|)
block|{
return|return
literal|null
return|;
block|}
return|return
name|locations
index|[
name|replicaId
index|]
return|;
block|}
comment|/**    * Returns the region location from the list for matching regionName, which can    * be regionName or encodedRegionName    * @param regionName regionName or encodedRegionName    * @return HRegionLocation found or null    */
specifier|public
name|HRegionLocation
name|getRegionLocationByRegionName
parameter_list|(
name|byte
index|[]
name|regionName
parameter_list|)
block|{
for|for
control|(
name|HRegionLocation
name|loc
range|:
name|locations
control|)
block|{
if|if
condition|(
name|loc
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|Bytes
operator|.
name|equals
argument_list|(
name|loc
operator|.
name|getRegionInfo
argument_list|()
operator|.
name|getRegionName
argument_list|()
argument_list|,
name|regionName
argument_list|)
operator|||
name|Bytes
operator|.
name|equals
argument_list|(
name|loc
operator|.
name|getRegionInfo
argument_list|()
operator|.
name|getEncodedNameAsBytes
argument_list|()
argument_list|,
name|regionName
argument_list|)
condition|)
block|{
return|return
name|loc
return|;
block|}
block|}
block|}
return|return
literal|null
return|;
block|}
specifier|public
name|HRegionLocation
index|[]
name|getRegionLocations
parameter_list|()
block|{
return|return
name|locations
return|;
block|}
specifier|public
name|HRegionLocation
name|getDefaultRegionLocation
parameter_list|()
block|{
return|return
name|locations
index|[
name|HRegionInfo
operator|.
name|DEFAULT_REPLICA_ID
index|]
return|;
block|}
comment|/**    * Returns the first not-null region location in the list    */
specifier|public
name|HRegionLocation
name|getRegionLocation
parameter_list|()
block|{
for|for
control|(
name|HRegionLocation
name|loc
range|:
name|locations
control|)
block|{
if|if
condition|(
name|loc
operator|!=
literal|null
condition|)
block|{
return|return
name|loc
return|;
block|}
block|}
return|return
literal|null
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
name|builder
init|=
operator|new
name|StringBuilder
argument_list|(
literal|"["
argument_list|)
decl_stmt|;
for|for
control|(
name|HRegionLocation
name|loc
range|:
name|locations
control|)
block|{
if|if
condition|(
name|builder
operator|.
name|length
argument_list|()
operator|>
literal|1
condition|)
block|{
name|builder
operator|.
name|append
argument_list|(
literal|", "
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|append
argument_list|(
name|loc
operator|==
literal|null
condition|?
literal|"null"
else|:
name|loc
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|append
argument_list|(
literal|"]"
argument_list|)
expr_stmt|;
return|return
name|builder
operator|.
name|toString
argument_list|()
return|;
block|}
block|}
end_class

end_unit

