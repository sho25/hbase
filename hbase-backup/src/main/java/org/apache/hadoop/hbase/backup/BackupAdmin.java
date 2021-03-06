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
name|Closeable
import|;
end_import

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
name|TableName
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
name|util
operator|.
name|BackupSet
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
comment|/**  * The administrative API for HBase Backup. Construct an instance and call {@link #close()}  * afterwards.  *<p>  * BackupAdmin can be used to create backups, restore data from backups and for other  * backup-related operations.  * @since 2.0  */
end_comment

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
interface|interface
name|BackupAdmin
extends|extends
name|Closeable
block|{
comment|/**    * Backup given list of tables fully. This is a synchronous operation. It returns backup id on    * success or throw exception on failure.    * @param userRequest BackupRequest instance    * @return the backup Id    */
name|String
name|backupTables
parameter_list|(
specifier|final
name|BackupRequest
name|userRequest
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Restore backup    * @param request restore request    * @throws IOException exception    */
name|void
name|restore
parameter_list|(
name|RestoreRequest
name|request
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Describe backup image command    * @param backupId backup id    * @return backup info    * @throws IOException exception    */
name|BackupInfo
name|getBackupInfo
parameter_list|(
name|String
name|backupId
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Delete backup image command    * @param backupIds array of backup ids    * @return total number of deleted sessions    * @throws IOException exception    */
name|int
name|deleteBackups
parameter_list|(
name|String
index|[]
name|backupIds
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Merge backup images command    * @param backupIds array of backup ids of images to be merged    *        The resulting backup image will have the same backup id as the most    *        recent image from a list of images to be merged    * @throws IOException exception    */
name|void
name|mergeBackups
parameter_list|(
name|String
index|[]
name|backupIds
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Show backup history command    * @param n last n backup sessions    * @return list of backup info objects    * @throws IOException exception    */
name|List
argument_list|<
name|BackupInfo
argument_list|>
name|getHistory
parameter_list|(
name|int
name|n
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Show backup history command with filters    * @param n last n backup sessions    * @param f list of filters    * @return list of backup info objects    * @throws IOException exception    */
name|List
argument_list|<
name|BackupInfo
argument_list|>
name|getHistory
parameter_list|(
name|int
name|n
parameter_list|,
name|BackupInfo
operator|.
name|Filter
modifier|...
name|f
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Backup sets list command - list all backup sets. Backup set is a named group of tables.    * @return all registered backup sets    * @throws IOException exception    */
name|List
argument_list|<
name|BackupSet
argument_list|>
name|listBackupSets
parameter_list|()
throws|throws
name|IOException
function_decl|;
comment|/**    * Backup set describe command. Shows list of tables in this particular backup set.    * @param name set name    * @return backup set description or null    * @throws IOException exception    */
name|BackupSet
name|getBackupSet
parameter_list|(
name|String
name|name
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Delete backup set command    * @param name backup set name    * @return true, if success, false - otherwise    * @throws IOException exception    */
name|boolean
name|deleteBackupSet
parameter_list|(
name|String
name|name
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Add tables to backup set command    * @param name name of backup set.    * @param tables array of tables to be added to this set.    * @throws IOException exception    */
name|void
name|addToBackupSet
parameter_list|(
name|String
name|name
parameter_list|,
name|TableName
index|[]
name|tables
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Remove tables from backup set    * @param name name of backup set.    * @param tables array of tables to be removed from this set.    * @throws IOException exception    */
name|void
name|removeFromBackupSet
parameter_list|(
name|String
name|name
parameter_list|,
name|TableName
index|[]
name|tables
parameter_list|)
throws|throws
name|IOException
function_decl|;
block|}
end_interface

end_unit

