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
name|CellComparator
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
name|compactions
operator|.
name|CompactionContext
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
name|compactions
operator|.
name|CompactionPolicy
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
name|compactions
operator|.
name|Compactor
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
name|ReflectionUtils
import|;
end_import

begin_comment
comment|/**  * StoreEngine is a factory that can create the objects necessary for HStore to operate.  * Since not all compaction policies, compactors and store file managers are compatible,  * they are tied together and replaced together via StoreEngine-s.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
specifier|abstract
class|class
name|StoreEngine
parameter_list|<
name|SF
extends|extends
name|StoreFlusher
parameter_list|,
name|CP
extends|extends
name|CompactionPolicy
parameter_list|,
name|C
extends|extends
name|Compactor
parameter_list|,
name|SFM
extends|extends
name|StoreFileManager
parameter_list|>
block|{
specifier|protected
name|SF
name|storeFlusher
decl_stmt|;
specifier|protected
name|CP
name|compactionPolicy
decl_stmt|;
specifier|protected
name|C
name|compactor
decl_stmt|;
specifier|protected
name|SFM
name|storeFileManager
decl_stmt|;
comment|/**    * The name of the configuration parameter that specifies the class of    * a store engine that is used to manage and compact HBase store files.    */
specifier|public
specifier|static
specifier|final
name|String
name|STORE_ENGINE_CLASS_KEY
init|=
literal|"hbase.hstore.engine.class"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|Class
argument_list|<
name|?
extends|extends
name|StoreEngine
argument_list|<
name|?
argument_list|,
name|?
argument_list|,
name|?
argument_list|,
name|?
argument_list|>
argument_list|>
name|DEFAULT_STORE_ENGINE_CLASS
init|=
name|DefaultStoreEngine
operator|.
name|class
decl_stmt|;
comment|/**    * @return Compaction policy to use.    */
specifier|public
name|CompactionPolicy
name|getCompactionPolicy
parameter_list|()
block|{
return|return
name|this
operator|.
name|compactionPolicy
return|;
block|}
comment|/**    * @return Compactor to use.    */
specifier|public
name|Compactor
name|getCompactor
parameter_list|()
block|{
return|return
name|this
operator|.
name|compactor
return|;
block|}
comment|/**    * @return Store file manager to use.    */
specifier|public
name|StoreFileManager
name|getStoreFileManager
parameter_list|()
block|{
return|return
name|this
operator|.
name|storeFileManager
return|;
block|}
comment|/**    * @return Store flusher to use.    */
specifier|public
name|StoreFlusher
name|getStoreFlusher
parameter_list|()
block|{
return|return
name|this
operator|.
name|storeFlusher
return|;
block|}
comment|/**    * @param filesCompacting Files currently compacting    * @return whether a compaction selection is possible    */
specifier|public
specifier|abstract
name|boolean
name|needsCompaction
parameter_list|(
name|List
argument_list|<
name|StoreFile
argument_list|>
name|filesCompacting
parameter_list|)
function_decl|;
comment|/**    * Creates an instance of a compaction context specific to this engine.    * Doesn't actually select or start a compaction. See CompactionContext class comment.    * @return New CompactionContext object.    */
specifier|public
specifier|abstract
name|CompactionContext
name|createCompaction
parameter_list|()
throws|throws
name|IOException
function_decl|;
comment|/**    * Create the StoreEngine's components.    */
specifier|protected
specifier|abstract
name|void
name|createComponents
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|Store
name|store
parameter_list|,
name|CellComparator
name|kvComparator
parameter_list|)
throws|throws
name|IOException
function_decl|;
specifier|private
name|void
name|createComponentsOnce
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|Store
name|store
parameter_list|,
name|CellComparator
name|kvComparator
parameter_list|)
throws|throws
name|IOException
block|{
assert|assert
name|compactor
operator|==
literal|null
operator|&&
name|compactionPolicy
operator|==
literal|null
operator|&&
name|storeFileManager
operator|==
literal|null
operator|&&
name|storeFlusher
operator|==
literal|null
assert|;
name|createComponents
argument_list|(
name|conf
argument_list|,
name|store
argument_list|,
name|kvComparator
argument_list|)
expr_stmt|;
assert|assert
name|compactor
operator|!=
literal|null
operator|&&
name|compactionPolicy
operator|!=
literal|null
operator|&&
name|storeFileManager
operator|!=
literal|null
operator|&&
name|storeFlusher
operator|!=
literal|null
assert|;
block|}
comment|/**    * Create the StoreEngine configured for the given Store.    * @param store The store. An unfortunate dependency needed due to it    *              being passed to coprocessors via the compactor.    * @param conf Store configuration.    * @param kvComparator KVComparator for storeFileManager.    * @return StoreEngine to use.    */
specifier|public
specifier|static
name|StoreEngine
argument_list|<
name|?
argument_list|,
name|?
argument_list|,
name|?
argument_list|,
name|?
argument_list|>
name|create
parameter_list|(
name|Store
name|store
parameter_list|,
name|Configuration
name|conf
parameter_list|,
name|CellComparator
name|kvComparator
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
name|STORE_ENGINE_CLASS_KEY
argument_list|,
name|DEFAULT_STORE_ENGINE_CLASS
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
try|try
block|{
name|StoreEngine
argument_list|<
name|?
argument_list|,
name|?
argument_list|,
name|?
argument_list|,
name|?
argument_list|>
name|se
init|=
name|ReflectionUtils
operator|.
name|instantiateWithCustomCtor
argument_list|(
name|className
argument_list|,
operator|new
name|Class
index|[]
block|{ }
argument_list|,
operator|new
name|Object
index|[]
block|{ }
argument_list|)
decl_stmt|;
name|se
operator|.
name|createComponentsOnce
argument_list|(
name|conf
argument_list|,
name|store
argument_list|,
name|kvComparator
argument_list|)
expr_stmt|;
return|return
name|se
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
literal|"Unable to load configured store engine '"
operator|+
name|className
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

