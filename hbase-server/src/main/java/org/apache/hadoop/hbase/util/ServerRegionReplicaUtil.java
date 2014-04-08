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
name|util
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
name|fs
operator|.
name|FileStatus
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
name|fs
operator|.
name|FileSystem
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
name|io
operator|.
name|HFileLink
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
name|regionserver
operator|.
name|HRegion
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
name|regionserver
operator|.
name|StoreFileInfo
import|;
end_import

begin_comment
comment|/**  * Similar to {@link RegionReplicaUtil} but for the server side  */
end_comment

begin_class
specifier|public
class|class
name|ServerRegionReplicaUtil
extends|extends
name|RegionReplicaUtil
block|{
comment|/**    * Returns the regionInfo object to use for interacting with the file system.    * @return An HRegionInfo object to interact with the filesystem    */
specifier|public
specifier|static
name|HRegionInfo
name|getRegionInfoForFs
parameter_list|(
name|HRegionInfo
name|regionInfo
parameter_list|)
block|{
if|if
condition|(
name|regionInfo
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
return|return
name|RegionReplicaUtil
operator|.
name|getRegionInfoForDefaultReplica
argument_list|(
name|regionInfo
argument_list|)
return|;
block|}
comment|/**    * Returns whether this region replica can accept writes.    * @param region the HRegion object    * @return whether the replica is read only    */
specifier|public
specifier|static
name|boolean
name|isReadOnly
parameter_list|(
name|HRegion
name|region
parameter_list|)
block|{
return|return
name|region
operator|.
name|getTableDesc
argument_list|()
operator|.
name|isReadOnly
argument_list|()
operator|||
operator|!
name|isDefaultReplica
argument_list|(
name|region
operator|.
name|getRegionInfo
argument_list|()
argument_list|)
return|;
block|}
comment|/**    * Returns whether to replay the recovered edits to flush the results.    * Currently secondary region replicas do not replay the edits, since it would    * cause flushes which might affect the primary region. Primary regions even opened    * in read only mode should replay the edits.    * @param region the HRegion object    * @return whether recovered edits should be replayed.    */
specifier|public
specifier|static
name|boolean
name|shouldReplayRecoveredEdits
parameter_list|(
name|HRegion
name|region
parameter_list|)
block|{
return|return
name|isDefaultReplica
argument_list|(
name|region
operator|.
name|getRegionInfo
argument_list|()
argument_list|)
return|;
block|}
comment|/**    * Returns a StoreFileInfo from the given FileStatus. Secondary replicas refer to the    * files of the primary region, so an HFileLink is used to construct the StoreFileInfo. This    * way ensures that the secondary will be able to continue reading the store files even if    * they are moved to archive after compaction    * @throws IOException    */
specifier|public
specifier|static
name|StoreFileInfo
name|getStoreFileInfo
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|FileSystem
name|fs
parameter_list|,
name|HRegionInfo
name|regionInfo
parameter_list|,
name|HRegionInfo
name|regionInfoForFs
parameter_list|,
name|String
name|familyName
parameter_list|,
name|FileStatus
name|status
parameter_list|)
throws|throws
name|IOException
block|{
comment|// if this is a primary region, just return the StoreFileInfo constructed from path
if|if
condition|(
name|regionInfo
operator|.
name|equals
argument_list|(
name|regionInfoForFs
argument_list|)
condition|)
block|{
return|return
operator|new
name|StoreFileInfo
argument_list|(
name|conf
argument_list|,
name|fs
argument_list|,
name|status
argument_list|)
return|;
block|}
comment|// else create a store file link. The link file does not exists on filesystem though.
name|HFileLink
name|link
init|=
operator|new
name|HFileLink
argument_list|(
name|conf
argument_list|,
name|HFileLink
operator|.
name|createPath
argument_list|(
name|regionInfoForFs
operator|.
name|getTable
argument_list|()
argument_list|,
name|regionInfoForFs
operator|.
name|getEncodedName
argument_list|()
argument_list|,
name|familyName
argument_list|,
name|status
operator|.
name|getPath
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
return|return
operator|new
name|StoreFileInfo
argument_list|(
name|conf
argument_list|,
name|fs
argument_list|,
name|status
argument_list|,
name|link
argument_list|)
return|;
block|}
block|}
end_class

end_unit

