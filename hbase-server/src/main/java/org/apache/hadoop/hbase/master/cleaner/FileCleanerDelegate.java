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
comment|/**    * Should the master delete the file or keep it?    * @param fStat file status of the file to check    * @return<tt>true</tt> if the file is deletable,<tt>false</tt> if not    */
name|boolean
name|isFileDeletable
parameter_list|(
name|FileStatus
name|fStat
parameter_list|)
function_decl|;
block|}
end_interface

end_unit

