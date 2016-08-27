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
name|master
operator|.
name|cleaner
package|;
end_package

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
name|Map
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
name|Stoppable
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
name|StoreFileInfo
import|;
end_import

begin_comment
comment|/**  * This Chore, every time it runs, will clear the HFiles in the hfile archive  * folder that are deletable for each HFile cleaner in the chain.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|HFileCleaner
extends|extends
name|CleanerChore
argument_list|<
name|BaseHFileCleanerDelegate
argument_list|>
block|{
specifier|public
specifier|static
specifier|final
name|String
name|MASTER_HFILE_CLEANER_PLUGINS
init|=
literal|"hbase.master.hfilecleaner.plugins"
decl_stmt|;
specifier|public
name|HFileCleaner
parameter_list|(
specifier|final
name|int
name|period
parameter_list|,
specifier|final
name|Stoppable
name|stopper
parameter_list|,
name|Configuration
name|conf
parameter_list|,
name|FileSystem
name|fs
parameter_list|,
name|Path
name|directory
parameter_list|)
block|{
name|this
argument_list|(
name|period
argument_list|,
name|stopper
argument_list|,
name|conf
argument_list|,
name|fs
argument_list|,
name|directory
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
comment|/**    * @param period the period of time to sleep between each run    * @param stopper the stopper    * @param conf configuration to use    * @param fs handle to the FS    * @param directory directory to be cleaned    * @param params params could be used in subclass of BaseHFileCleanerDelegate    */
specifier|public
name|HFileCleaner
parameter_list|(
specifier|final
name|int
name|period
parameter_list|,
specifier|final
name|Stoppable
name|stopper
parameter_list|,
name|Configuration
name|conf
parameter_list|,
name|FileSystem
name|fs
parameter_list|,
name|Path
name|directory
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|params
parameter_list|)
block|{
name|super
argument_list|(
literal|"HFileCleaner"
argument_list|,
name|period
argument_list|,
name|stopper
argument_list|,
name|conf
argument_list|,
name|fs
argument_list|,
name|directory
argument_list|,
name|MASTER_HFILE_CLEANER_PLUGINS
argument_list|,
name|params
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|boolean
name|validate
parameter_list|(
name|Path
name|file
parameter_list|)
block|{
if|if
condition|(
name|HFileLink
operator|.
name|isBackReferencesDir
argument_list|(
name|file
argument_list|)
operator|||
name|HFileLink
operator|.
name|isBackReferencesDir
argument_list|(
name|file
operator|.
name|getParent
argument_list|()
argument_list|)
condition|)
block|{
return|return
literal|true
return|;
block|}
return|return
name|StoreFileInfo
operator|.
name|validateStoreFileName
argument_list|(
name|file
operator|.
name|getName
argument_list|()
argument_list|)
return|;
block|}
comment|/**    * Exposed for TESTING!    */
specifier|public
name|List
argument_list|<
name|BaseHFileCleanerDelegate
argument_list|>
name|getDelegatesForTesting
parameter_list|()
block|{
return|return
name|this
operator|.
name|cleanersChain
return|;
block|}
block|}
end_class

end_unit

