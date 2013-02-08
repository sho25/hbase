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
name|conf
operator|.
name|Configured
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
name|HColumnDescriptor
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
name|HStore
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

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|util
operator|.
name|ReflectionUtils
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
extends|extends
name|Configured
block|{
comment|/**    * The name of the configuration parameter that specifies    * the class of a compaction policy that is used to compact    * HBase store files.    */
specifier|public
specifier|static
specifier|final
name|String
name|COMPACTION_POLICY_KEY
init|=
literal|"hbase.hstore.compaction.policy"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|Class
argument_list|<
name|?
extends|extends
name|CompactionPolicy
argument_list|>
name|DEFAULT_COMPACTION_POLICY_CLASS
init|=
name|DefaultCompactionPolicy
operator|.
name|class
decl_stmt|;
name|CompactionConfiguration
name|comConf
decl_stmt|;
name|Compactor
name|compactor
decl_stmt|;
name|HStore
name|store
decl_stmt|;
comment|/**    * @param candidateFiles candidate files, ordered from oldest to newest    * @return subset copy of candidate list that meets compaction criteria    * @throws java.io.IOException    */
specifier|public
specifier|abstract
name|CompactSelection
name|selectCompaction
parameter_list|(
specifier|final
name|List
argument_list|<
name|StoreFile
argument_list|>
name|candidateFiles
parameter_list|,
specifier|final
name|boolean
name|isUserCompaction
parameter_list|,
specifier|final
name|boolean
name|forceMajor
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * @param filesToCompact Files to compact. Can be null.    * @return True if we should run a major compaction.    */
specifier|public
specifier|abstract
name|boolean
name|isMajorCompaction
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
comment|/**    * @param numCandidates Number of candidate store files    * @return whether a compactionSelection is possible    */
specifier|public
specifier|abstract
name|boolean
name|needsCompaction
parameter_list|(
name|int
name|numCandidates
parameter_list|)
function_decl|;
comment|/**    * Inform the policy that some configuration has been change,    * so cached value should be updated it any.    */
specifier|public
name|void
name|updateConfiguration
parameter_list|()
block|{
if|if
condition|(
name|getConf
argument_list|()
operator|!=
literal|null
operator|&&
name|store
operator|!=
literal|null
condition|)
block|{
name|comConf
operator|=
operator|new
name|CompactionConfiguration
argument_list|(
name|getConf
argument_list|()
argument_list|,
name|store
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Get the compactor for this policy    * @return the compactor for this policy    */
specifier|public
name|Compactor
name|getCompactor
parameter_list|()
block|{
return|return
name|compactor
return|;
block|}
comment|/**    * Set the new configuration    */
annotation|@
name|Override
specifier|public
name|void
name|setConf
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
name|super
operator|.
name|setConf
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|updateConfiguration
argument_list|()
expr_stmt|;
block|}
comment|/**    * Upon construction, this method will be called with the HStore    * to be governed. It will be called once and only once.    */
specifier|protected
name|void
name|configureForStore
parameter_list|(
name|HStore
name|store
parameter_list|)
block|{
name|this
operator|.
name|store
operator|=
name|store
expr_stmt|;
name|updateConfiguration
argument_list|()
expr_stmt|;
block|}
comment|/**    * Create the CompactionPolicy configured for the given HStore.    * @param store    * @param conf    * @return a CompactionPolicy    * @throws IOException    */
specifier|public
specifier|static
name|CompactionPolicy
name|create
parameter_list|(
name|HStore
name|store
parameter_list|,
name|Configuration
name|conf
parameter_list|)
throws|throws
name|IOException
block|{
name|Class
argument_list|<
name|?
extends|extends
name|CompactionPolicy
argument_list|>
name|clazz
init|=
name|getCompactionPolicyClass
argument_list|(
name|store
operator|.
name|getFamily
argument_list|()
argument_list|,
name|conf
argument_list|)
decl_stmt|;
name|CompactionPolicy
name|policy
init|=
name|ReflectionUtils
operator|.
name|newInstance
argument_list|(
name|clazz
argument_list|,
name|conf
argument_list|)
decl_stmt|;
name|policy
operator|.
name|configureForStore
argument_list|(
name|store
argument_list|)
expr_stmt|;
return|return
name|policy
return|;
block|}
specifier|static
name|Class
argument_list|<
name|?
extends|extends
name|CompactionPolicy
argument_list|>
name|getCompactionPolicyClass
parameter_list|(
name|HColumnDescriptor
name|family
parameter_list|,
name|Configuration
name|conf
parameter_list|)
throws|throws
name|IOException
block|{
name|String
name|className
init|=
name|conf
operator|.
name|get
argument_list|(
name|COMPACTION_POLICY_KEY
argument_list|,
name|DEFAULT_COMPACTION_POLICY_CLASS
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
try|try
block|{
name|Class
argument_list|<
name|?
extends|extends
name|CompactionPolicy
argument_list|>
name|clazz
init|=
name|Class
operator|.
name|forName
argument_list|(
name|className
argument_list|)
operator|.
name|asSubclass
argument_list|(
name|CompactionPolicy
operator|.
name|class
argument_list|)
decl_stmt|;
return|return
name|clazz
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Unable to load configured region compaction policy '"
operator|+
name|className
operator|+
literal|"' for column '"
operator|+
name|family
operator|.
name|getNameAsString
argument_list|()
operator|+
literal|"'"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
block|}
end_class

end_unit

