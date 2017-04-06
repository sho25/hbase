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
name|mapreduce
operator|.
name|MapReduceBackupCopyJob
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
name|mapreduce
operator|.
name|MapReduceRestoreJob
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
name|util
operator|.
name|ReflectionUtils
import|;
end_import

begin_comment
comment|/**  * Factory implementation for backup/restore related jobs  *  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
specifier|final
class|class
name|BackupRestoreFactory
block|{
specifier|public
specifier|final
specifier|static
name|String
name|HBASE_INCR_RESTORE_IMPL_CLASS
init|=
literal|"hbase.incremental.restore.class"
decl_stmt|;
specifier|public
specifier|final
specifier|static
name|String
name|HBASE_BACKUP_COPY_IMPL_CLASS
init|=
literal|"hbase.backup.copy.class"
decl_stmt|;
specifier|private
name|BackupRestoreFactory
parameter_list|()
block|{
throw|throw
operator|new
name|AssertionError
argument_list|(
literal|"Instantiating utility class..."
argument_list|)
throw|;
block|}
comment|/**    * Gets backup restore job    * @param conf configuration    * @return backup restore task instance    */
specifier|public
specifier|static
name|RestoreJob
name|getRestoreJob
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
name|Class
argument_list|<
name|?
extends|extends
name|RestoreJob
argument_list|>
name|cls
init|=
name|conf
operator|.
name|getClass
argument_list|(
name|HBASE_INCR_RESTORE_IMPL_CLASS
argument_list|,
name|MapReduceRestoreJob
operator|.
name|class
argument_list|,
name|RestoreJob
operator|.
name|class
argument_list|)
decl_stmt|;
name|RestoreJob
name|service
init|=
name|ReflectionUtils
operator|.
name|newInstance
argument_list|(
name|cls
argument_list|,
name|conf
argument_list|)
decl_stmt|;
name|service
operator|.
name|setConf
argument_list|(
name|conf
argument_list|)
expr_stmt|;
return|return
name|service
return|;
block|}
comment|/**    * Gets backup copy job    * @param conf configuration    * @return backup copy task    */
specifier|public
specifier|static
name|BackupCopyJob
name|getBackupCopyJob
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
name|Class
argument_list|<
name|?
extends|extends
name|BackupCopyJob
argument_list|>
name|cls
init|=
name|conf
operator|.
name|getClass
argument_list|(
name|HBASE_BACKUP_COPY_IMPL_CLASS
argument_list|,
name|MapReduceBackupCopyJob
operator|.
name|class
argument_list|,
name|BackupCopyJob
operator|.
name|class
argument_list|)
decl_stmt|;
name|BackupCopyJob
name|service
init|=
name|ReflectionUtils
operator|.
name|newInstance
argument_list|(
name|cls
argument_list|,
name|conf
argument_list|)
decl_stmt|;
name|service
operator|.
name|setConf
argument_list|(
name|conf
argument_list|)
expr_stmt|;
return|return
name|service
return|;
block|}
block|}
end_class

end_unit
