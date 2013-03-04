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
name|ArrayList
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
name|KeyValue
operator|.
name|KVComparator
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
name|regionserver
operator|.
name|compactions
operator|.
name|DefaultCompactionPolicy
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
name|DefaultCompactor
import|;
end_import

begin_comment
comment|/**  * Default StoreEngine creates the default compactor, policy, and store file manager, or  * their derivatives.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|DefaultStoreEngine
extends|extends
name|StoreEngine
argument_list|<
name|DefaultCompactionPolicy
argument_list|,
name|DefaultCompactor
argument_list|,
name|DefaultStoreFileManager
argument_list|>
block|{
specifier|public
name|DefaultStoreEngine
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|Store
name|store
parameter_list|,
name|KVComparator
name|comparator
parameter_list|)
block|{
name|super
argument_list|(
name|conf
argument_list|,
name|store
argument_list|,
name|comparator
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|createComponents
parameter_list|()
block|{
name|storeFileManager
operator|=
operator|new
name|DefaultStoreFileManager
argument_list|(
name|this
operator|.
name|comparator
argument_list|,
name|this
operator|.
name|conf
argument_list|)
expr_stmt|;
comment|// TODO: compactor and policy may be separately pluggable, but must derive from default ones.
name|compactor
operator|=
operator|new
name|DefaultCompactor
argument_list|(
name|this
operator|.
name|conf
argument_list|,
name|this
operator|.
name|store
argument_list|)
expr_stmt|;
name|compactionPolicy
operator|=
operator|new
name|DefaultCompactionPolicy
argument_list|(
name|this
operator|.
name|conf
argument_list|,
name|this
operator|.
name|store
comment|/*as StoreConfigInfo*/
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|CompactionContext
name|createCompactionContext
parameter_list|()
block|{
return|return
operator|new
name|DefaultCompactionContext
argument_list|()
return|;
block|}
specifier|private
class|class
name|DefaultCompactionContext
extends|extends
name|CompactionContext
block|{
annotation|@
name|Override
specifier|public
name|boolean
name|select
parameter_list|(
name|List
argument_list|<
name|StoreFile
argument_list|>
name|filesCompacting
parameter_list|,
name|boolean
name|isUserCompaction
parameter_list|,
name|boolean
name|mayUseOffPeak
parameter_list|,
name|boolean
name|forceMajor
parameter_list|)
throws|throws
name|IOException
block|{
name|request
operator|=
name|compactionPolicy
operator|.
name|selectCompaction
argument_list|(
name|storeFileManager
operator|.
name|getStorefiles
argument_list|()
argument_list|,
name|filesCompacting
argument_list|,
name|isUserCompaction
argument_list|,
name|mayUseOffPeak
argument_list|,
name|forceMajor
argument_list|)
expr_stmt|;
return|return
name|request
operator|!=
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|Path
argument_list|>
name|compact
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|compactor
operator|.
name|compact
argument_list|(
name|request
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|StoreFile
argument_list|>
name|preSelect
parameter_list|(
name|List
argument_list|<
name|StoreFile
argument_list|>
name|filesCompacting
parameter_list|)
block|{
return|return
name|compactionPolicy
operator|.
name|preSelectCompactionForCoprocessor
argument_list|(
name|storeFileManager
operator|.
name|getStorefiles
argument_list|()
argument_list|,
name|filesCompacting
argument_list|)
return|;
block|}
block|}
block|}
end_class

end_unit

