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
name|java
operator|.
name|io
operator|.
name|Closeable
import|;
end_import

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
name|List
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|stream
operator|.
name|Collectors
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
name|HRegionLocation
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
name|util
operator|.
name|Pair
import|;
end_import

begin_comment
comment|/**  * Used to view region location information for a single HBase table. Obtain an instance from an  * {@link Connection}.  * @see ConnectionFactory  * @see Connection  * @see Table  * @since 0.99.0  */
end_comment

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|Public
specifier|public
interface|interface
name|RegionLocator
extends|extends
name|Closeable
block|{
comment|/**    * Finds the region on which the given row is being served. Does not reload the cache.    * @param row Row to find.    * @return Location of the row.    * @throws IOException if a remote or network exception occurs    */
specifier|default
name|HRegionLocation
name|getRegionLocation
parameter_list|(
name|byte
index|[]
name|row
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|getRegionLocation
argument_list|(
name|row
argument_list|,
literal|false
argument_list|)
return|;
block|}
comment|/**    * Finds the region on which the given row is being served.    * @param row Row to find.    * @param reload true to reload information or false to use cached information    * @return Location of the row.    * @throws IOException if a remote or network exception occurs    */
specifier|default
name|HRegionLocation
name|getRegionLocation
parameter_list|(
name|byte
index|[]
name|row
parameter_list|,
name|boolean
name|reload
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|getRegionLocation
argument_list|(
name|row
argument_list|,
name|RegionInfo
operator|.
name|DEFAULT_REPLICA_ID
argument_list|,
name|reload
argument_list|)
return|;
block|}
comment|/**    * Finds the region with the given replica id on which the given row is being served.    * @param row Row to find.    * @param replicaId the replica id    * @return Location of the row.    * @throws IOException if a remote or network exception occurs    */
specifier|default
name|HRegionLocation
name|getRegionLocation
parameter_list|(
name|byte
index|[]
name|row
parameter_list|,
name|int
name|replicaId
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|getRegionLocation
argument_list|(
name|row
argument_list|,
name|replicaId
argument_list|,
literal|false
argument_list|)
return|;
block|}
comment|/**    * Finds the region with the given replica id on which the given row is being served.    * @param row Row to find.    * @param replicaId the replica id    * @param reload true to reload information or false to use cached information    * @return Location of the row.    * @throws IOException if a remote or network exception occurs    */
name|HRegionLocation
name|getRegionLocation
parameter_list|(
name|byte
index|[]
name|row
parameter_list|,
name|int
name|replicaId
parameter_list|,
name|boolean
name|reload
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Find all the replicas for the region on which the given row is being served.    * @param row Row to find.    * @return Locations for all the replicas of the row.    * @throws IOException if a remote or network exception occurs    */
specifier|default
name|List
argument_list|<
name|HRegionLocation
argument_list|>
name|getRegionLocations
parameter_list|(
name|byte
index|[]
name|row
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|getRegionLocations
argument_list|(
name|row
argument_list|,
literal|false
argument_list|)
return|;
block|}
comment|/**    * Find all the replicas for the region on which the given row is being served.    * @param row Row to find.    * @param reload true to reload information or false to use cached information    * @return Locations for all the replicas of the row.    * @throws IOException if a remote or network exception occurs    */
name|List
argument_list|<
name|HRegionLocation
argument_list|>
name|getRegionLocations
parameter_list|(
name|byte
index|[]
name|row
parameter_list|,
name|boolean
name|reload
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Clear all the entries in the region location cache.    *<p/>    * This may cause performance issue so use it with caution.    */
name|void
name|clearRegionLocationCache
parameter_list|()
function_decl|;
comment|/**    * Retrieves all of the regions associated with this table.    *<p/>    * Usually we will go to meta table directly in this method so there is no {@code reload}    * parameter.    *<p/>    * Notice that the location for region replicas other than the default replica are also returned.    * @return a {@link List} of all regions associated with this table.    * @throws IOException if a remote or network exception occurs    */
name|List
argument_list|<
name|HRegionLocation
argument_list|>
name|getAllRegionLocations
parameter_list|()
throws|throws
name|IOException
function_decl|;
comment|/**    * Gets the starting row key for every region in the currently open table.    *<p>    * This is mainly useful for the MapReduce integration.    * @return Array of region starting row keys    * @throws IOException if a remote or network exception occurs    */
specifier|default
name|byte
index|[]
index|[]
name|getStartKeys
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|getStartEndKeys
argument_list|()
operator|.
name|getFirst
argument_list|()
return|;
block|}
comment|/**    * Gets the ending row key for every region in the currently open table.    *<p>    * This is mainly useful for the MapReduce integration.    * @return Array of region ending row keys    * @throws IOException if a remote or network exception occurs    */
specifier|default
name|byte
index|[]
index|[]
name|getEndKeys
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|getStartEndKeys
argument_list|()
operator|.
name|getSecond
argument_list|()
return|;
block|}
comment|/**    * Gets the starting and ending row keys for every region in the currently open table.    *<p>    * This is mainly useful for the MapReduce integration.    * @return Pair of arrays of region starting and ending row keys    * @throws IOException if a remote or network exception occurs    */
specifier|default
name|Pair
argument_list|<
name|byte
index|[]
index|[]
argument_list|,
name|byte
index|[]
index|[]
argument_list|>
name|getStartEndKeys
parameter_list|()
throws|throws
name|IOException
block|{
name|List
argument_list|<
name|HRegionLocation
argument_list|>
name|regions
init|=
name|getAllRegionLocations
argument_list|()
operator|.
name|stream
argument_list|()
operator|.
name|filter
argument_list|(
name|loc
lambda|->
name|RegionReplicaUtil
operator|.
name|isDefaultReplica
argument_list|(
name|loc
operator|.
name|getRegion
argument_list|()
argument_list|)
argument_list|)
operator|.
name|collect
argument_list|(
name|Collectors
operator|.
name|toList
argument_list|()
argument_list|)
decl_stmt|;
name|byte
index|[]
index|[]
name|startKeys
init|=
operator|new
name|byte
index|[
name|regions
operator|.
name|size
argument_list|()
index|]
index|[]
decl_stmt|;
name|byte
index|[]
index|[]
name|endKeys
init|=
operator|new
name|byte
index|[
name|regions
operator|.
name|size
argument_list|()
index|]
index|[]
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|,
name|n
init|=
name|regions
operator|.
name|size
argument_list|()
init|;
name|i
operator|<
name|n
condition|;
name|i
operator|++
control|)
block|{
name|RegionInfo
name|region
init|=
name|regions
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|getRegion
argument_list|()
decl_stmt|;
name|startKeys
index|[
name|i
index|]
operator|=
name|region
operator|.
name|getStartKey
argument_list|()
expr_stmt|;
name|endKeys
index|[
name|i
index|]
operator|=
name|region
operator|.
name|getEndKey
argument_list|()
expr_stmt|;
block|}
return|return
name|Pair
operator|.
name|newPair
argument_list|(
name|startKeys
argument_list|,
name|endKeys
argument_list|)
return|;
block|}
comment|/**    * Gets the fully qualified table name instance of this table.    */
name|TableName
name|getName
parameter_list|()
function_decl|;
block|}
end_interface

end_unit

