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
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|TimeUnit
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|Log
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|LogFactory
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
name|ScheduledChore
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
name|TableDescriptors
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
name|hbase
operator|.
name|client
operator|.
name|ColumnFamilyDescriptor
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
name|client
operator|.
name|TableDescriptor
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
name|master
operator|.
name|locking
operator|.
name|LockManager
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
name|mob
operator|.
name|ExpiredMobFileCleaner
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
name|mob
operator|.
name|MobConstants
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
name|mob
operator|.
name|MobUtils
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
name|procedure2
operator|.
name|LockType
import|;
end_import

begin_comment
comment|/**  * The Class ExpiredMobFileCleanerChore for running cleaner regularly to remove the expired  * mob files.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|ExpiredMobFileCleanerChore
extends|extends
name|ScheduledChore
block|{
specifier|private
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|ExpiredMobFileCleanerChore
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|HMaster
name|master
decl_stmt|;
specifier|private
name|ExpiredMobFileCleaner
name|cleaner
decl_stmt|;
specifier|public
name|ExpiredMobFileCleanerChore
parameter_list|(
name|HMaster
name|master
parameter_list|)
block|{
name|super
argument_list|(
name|master
operator|.
name|getServerName
argument_list|()
operator|+
literal|"-ExpiredMobFileCleanerChore"
argument_list|,
name|master
argument_list|,
name|master
operator|.
name|getConfiguration
argument_list|()
operator|.
name|getInt
argument_list|(
name|MobConstants
operator|.
name|MOB_CLEANER_PERIOD
argument_list|,
name|MobConstants
operator|.
name|DEFAULT_MOB_CLEANER_PERIOD
argument_list|)
argument_list|,
name|master
operator|.
name|getConfiguration
argument_list|()
operator|.
name|getInt
argument_list|(
name|MobConstants
operator|.
name|MOB_CLEANER_PERIOD
argument_list|,
name|MobConstants
operator|.
name|DEFAULT_MOB_CLEANER_PERIOD
argument_list|)
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
expr_stmt|;
name|this
operator|.
name|master
operator|=
name|master
expr_stmt|;
name|cleaner
operator|=
operator|new
name|ExpiredMobFileCleaner
argument_list|()
expr_stmt|;
name|cleaner
operator|.
name|setConf
argument_list|(
name|master
operator|.
name|getConfiguration
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
annotation|@
name|edu
operator|.
name|umd
operator|.
name|cs
operator|.
name|findbugs
operator|.
name|annotations
operator|.
name|SuppressWarnings
argument_list|(
name|value
operator|=
literal|"REC_CATCH_EXCEPTION"
argument_list|,
name|justification
operator|=
literal|"Intentional"
argument_list|)
specifier|protected
name|void
name|chore
parameter_list|()
block|{
try|try
block|{
name|TableDescriptors
name|htds
init|=
name|master
operator|.
name|getTableDescriptors
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|TableDescriptor
argument_list|>
name|map
init|=
name|htds
operator|.
name|getAll
argument_list|()
decl_stmt|;
for|for
control|(
name|TableDescriptor
name|htd
range|:
name|map
operator|.
name|values
argument_list|()
control|)
block|{
for|for
control|(
name|ColumnFamilyDescriptor
name|hcd
range|:
name|htd
operator|.
name|getColumnFamilies
argument_list|()
control|)
block|{
if|if
condition|(
name|hcd
operator|.
name|isMobEnabled
argument_list|()
operator|&&
name|hcd
operator|.
name|getMinVersions
argument_list|()
operator|==
literal|0
condition|)
block|{
comment|// clean only for mob-enabled column.
comment|// obtain a read table lock before cleaning, synchronize with MobFileCompactionChore.
specifier|final
name|LockManager
operator|.
name|MasterLock
name|lock
init|=
name|master
operator|.
name|getLockManager
argument_list|()
operator|.
name|createMasterLock
argument_list|(
name|MobUtils
operator|.
name|getTableLockName
argument_list|(
name|htd
operator|.
name|getTableName
argument_list|()
argument_list|)
argument_list|,
name|LockType
operator|.
name|SHARED
argument_list|,
name|this
operator|.
name|getClass
argument_list|()
operator|.
name|getSimpleName
argument_list|()
operator|+
literal|": Cleaning expired mob files"
argument_list|)
decl_stmt|;
try|try
block|{
name|lock
operator|.
name|acquire
argument_list|()
expr_stmt|;
name|cleaner
operator|.
name|cleanExpiredMobFiles
argument_list|(
name|htd
operator|.
name|getTableName
argument_list|()
operator|.
name|getNameAsString
argument_list|()
argument_list|,
name|hcd
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|lock
operator|.
name|release
argument_list|()
expr_stmt|;
block|}
block|}
block|}
block|}
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Fail to clean the expired mob files"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

