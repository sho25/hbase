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
name|backup
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
name|backup
operator|.
name|impl
operator|.
name|BackupManager
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
comment|/**  * Backup copy job is a part of a backup process. The concrete implementation is responsible for  * copying data from a cluster to backup destination. Concrete implementation is provided by backup  * provider, see {@link BackupRestoreFactory}  */
end_comment

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
interface|interface
name|BackupCopyJob
extends|extends
name|Configurable
block|{
comment|/**    * Copy backup data to destination    * @param backupInfo context object    * @param backupManager backup manager    * @param conf configuration    * @param backupType backup type (FULL or INCREMENTAL)    * @param options array of options (implementation-specific)    * @return result (0 - success, -1 failure )    * @throws IOException exception    */
name|int
name|copy
parameter_list|(
name|BackupInfo
name|backupInfo
parameter_list|,
name|BackupManager
name|backupManager
parameter_list|,
name|Configuration
name|conf
parameter_list|,
name|BackupType
name|backupType
parameter_list|,
name|String
index|[]
name|options
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Cancel copy job    * @param jobHandler backup copy job handler    * @throws IOException if cancelling the jobs fails    */
name|void
name|cancel
parameter_list|(
name|String
name|jobHandler
parameter_list|)
throws|throws
name|IOException
function_decl|;
block|}
end_interface

end_unit

