begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one or more  * contributor license agreements.  See the NOTICE file distributed with  * this work for additional information regarding copyright ownership.  * The ASF licenses this file to you under the Apache License, Version 2.0  * (the "License"); you may not use this file except in compliance with  * the License.  You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|Map
operator|.
name|Entry
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

begin_comment
comment|/**  * Interface allowing various implementations of tracking files that have recently been archived to  * allow for the Master to notice changes to snapshot sizes for space quotas.  *  * This object needs to ensure that {@link #addArchivedFiles(Set)} and  * {@link #computeAndStoreSnapshotSizes(Collection)} are mutually exclusive. If a "full" computation  * is in progress, new changes being archived should be held.  */
end_comment

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
interface|interface
name|FileArchiverNotifier
block|{
comment|/**    * Records a file and its size in bytes being moved to the archive directory.    *    * @param fileSizes A collection of file name to size in bytes    * @throws IOException If there was an IO-related error persisting the file size(s)    */
name|void
name|addArchivedFiles
parameter_list|(
name|Set
argument_list|<
name|Entry
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
argument_list|>
name|fileSizes
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Computes the size of a table and all of its snapshots, recording new "full" sizes for each.    *    * @param currentSnapshots the current list of snapshots against this table    * @return The total size of all snapshots against this table.    * @throws IOException If there was an IO-related error computing or persisting the sizes.    */
name|long
name|computeAndStoreSnapshotSizes
parameter_list|(
name|Collection
argument_list|<
name|String
argument_list|>
name|currentSnapshots
parameter_list|)
throws|throws
name|IOException
function_decl|;
block|}
end_interface

end_unit

