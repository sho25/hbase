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
name|hbase
operator|.
name|regionserver
operator|.
name|StoreConfigInformation
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
comment|/**  * A compaction policy determines how to select files for compaction,  * how to compact them, and how to generate the compacted files.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
specifier|abstract
class|class
name|CompactionPolicy
block|{
specifier|protected
name|CompactionConfiguration
name|comConf
decl_stmt|;
specifier|protected
name|StoreConfigInformation
name|storeConfigInfo
decl_stmt|;
specifier|public
name|CompactionPolicy
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|StoreConfigInformation
name|storeConfigInfo
parameter_list|)
block|{
name|this
operator|.
name|storeConfigInfo
operator|=
name|storeConfigInfo
expr_stmt|;
name|this
operator|.
name|comConf
operator|=
operator|new
name|CompactionConfiguration
argument_list|(
name|conf
argument_list|,
name|this
operator|.
name|storeConfigInfo
argument_list|)
expr_stmt|;
block|}
comment|/**    * @param filesToCompact Files to compact. Can be null.    * @return True if we should run a major compaction.    */
specifier|public
specifier|abstract
name|boolean
name|shouldPerformMajorCompaction
parameter_list|(
specifier|final
name|Collection
argument_list|<
name|StoreFile
argument_list|>
name|filesToCompact
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * @param compactionSize Total size of some compaction    * @return whether this should be a large or small compaction    */
specifier|public
specifier|abstract
name|boolean
name|throttleCompaction
parameter_list|(
name|long
name|compactionSize
parameter_list|)
function_decl|;
comment|/**    * Inform the policy that some configuration has been change,    * so cached value should be updated it any.    */
specifier|public
name|void
name|setConf
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
name|this
operator|.
name|comConf
operator|=
operator|new
name|CompactionConfiguration
argument_list|(
name|conf
argument_list|,
name|this
operator|.
name|storeConfigInfo
argument_list|)
expr_stmt|;
block|}
comment|/**    * @return The current compaction configuration settings.    */
specifier|public
name|CompactionConfiguration
name|getConf
parameter_list|()
block|{
return|return
name|this
operator|.
name|comConf
return|;
block|}
block|}
end_class

end_unit

