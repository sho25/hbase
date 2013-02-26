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
name|regionserver
operator|.
name|compactions
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
name|List
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
name|Path
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
name|StoreFile
import|;
end_import

begin_comment
comment|/**  * A compactor is a compaction algorithm associated a given policy.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
specifier|abstract
class|class
name|Compactor
block|{
specifier|protected
name|CompactionProgress
name|progress
decl_stmt|;
specifier|protected
name|Configuration
name|conf
decl_stmt|;
name|Compactor
parameter_list|(
specifier|final
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
comment|/**    * Do a minor/major compaction on an explicit set of storefiles from a Store.    * @param request the requested compaction    * @return Product of compaction or an empty list if all cells expired or deleted and nothing made    *         it through the compaction.    * @throws IOException    */
specifier|public
specifier|abstract
name|List
argument_list|<
name|Path
argument_list|>
name|compact
parameter_list|(
specifier|final
name|CompactionRequest
name|request
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Compact a list of files for testing. Creates a fake {@link CompactionRequest} to pass to    * {@link #compact(CompactionRequest)};    * @param filesToCompact the files to compact. These are used as the compactionSelection for the    *          generated {@link CompactionRequest}.    * @param isMajor true to major compact (prune all deletes, max versions, etc)    * @return Product of compaction or an empty list if all cells expired or deleted and nothing made    *         it through the compaction.    * @throws IOException    */
specifier|public
name|List
argument_list|<
name|Path
argument_list|>
name|compactForTesting
parameter_list|(
specifier|final
name|Collection
argument_list|<
name|StoreFile
argument_list|>
name|filesToCompact
parameter_list|,
name|boolean
name|isMajor
parameter_list|)
throws|throws
name|IOException
block|{
name|CompactionRequest
name|cr
init|=
operator|new
name|CompactionRequest
argument_list|(
name|filesToCompact
argument_list|)
decl_stmt|;
name|cr
operator|.
name|setIsMajor
argument_list|(
name|isMajor
argument_list|)
expr_stmt|;
return|return
name|this
operator|.
name|compact
argument_list|(
name|cr
argument_list|)
return|;
block|}
specifier|public
name|CompactionProgress
name|getProgress
parameter_list|()
block|{
return|return
name|this
operator|.
name|progress
return|;
block|}
block|}
end_class

end_unit

