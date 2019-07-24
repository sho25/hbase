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
name|conf
operator|.
name|Configurable
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
name|yetus
operator|.
name|audience
operator|.
name|InterfaceAudience
import|;
end_import

begin_comment
comment|/**  * General interface for cleaning files from a folder (generally an archive or  * backup folder). These are chained via the {@link CleanerChore} to determine  * if a given file should be deleted.  */
end_comment

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
interface|interface
name|FileCleanerDelegate
extends|extends
name|Configurable
extends|,
name|Stoppable
block|{
comment|/**    * Determines which of the given files are safe to delete    * @param files files to check for deletion    * @return files that are ok to delete according to this cleaner    */
name|Iterable
argument_list|<
name|FileStatus
argument_list|>
name|getDeletableFiles
parameter_list|(
name|Iterable
argument_list|<
name|FileStatus
argument_list|>
name|files
parameter_list|)
function_decl|;
comment|/**    * this method is used to pass some instance into subclass    * */
name|void
name|init
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|params
parameter_list|)
function_decl|;
comment|/**    * Used to do some initialize work before every period clean    */
specifier|default
name|void
name|preClean
parameter_list|()
block|{   }
comment|/**    * Check if a empty directory with no subdirs or subfiles can be deleted    * @param dir Path of the directory    * @return True if the directory can be deleted, otherwise false    */
specifier|default
name|boolean
name|isEmptyDirDeletable
parameter_list|(
name|Path
name|dir
parameter_list|)
block|{
return|return
literal|true
return|;
block|}
block|}
end_interface

end_unit

