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
name|ExecutorService
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
name|HTableDescriptor
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
name|hadoop
operator|.
name|hbase
operator|.
name|client
operator|.
name|TableState
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

begin_comment
comment|/**  * The Class MobCompactChore for running compaction regularly to merge small mob files.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|MobCompactionChore
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
name|MobCompactionChore
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|HMaster
name|master
decl_stmt|;
specifier|private
name|TableLockManager
name|tableLockManager
decl_stmt|;
specifier|private
name|ExecutorService
name|pool
decl_stmt|;
specifier|public
name|MobCompactionChore
parameter_list|(
name|HMaster
name|master
parameter_list|,
name|int
name|period
parameter_list|)
block|{
comment|// use the period as initial delay.
name|super
argument_list|(
name|master
operator|.
name|getServerName
argument_list|()
operator|+
literal|"-MobCompactionChore"
argument_list|,
name|master
argument_list|,
name|period
argument_list|,
name|period
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
name|this
operator|.
name|tableLockManager
operator|=
name|master
operator|.
name|getTableLockManager
argument_list|()
expr_stmt|;
name|this
operator|.
name|pool
operator|=
name|MobUtils
operator|.
name|createMobCompactorThreadPool
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
name|HTableDescriptor
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
name|HTableDescriptor
name|htd
range|:
name|map
operator|.
name|values
argument_list|()
control|)
block|{
if|if
condition|(
operator|!
name|master
operator|.
name|getTableStateManager
argument_list|()
operator|.
name|isTableState
argument_list|(
name|htd
operator|.
name|getTableName
argument_list|()
argument_list|,
name|TableState
operator|.
name|State
operator|.
name|ENABLED
argument_list|)
condition|)
block|{
continue|continue;
block|}
name|boolean
name|reported
init|=
literal|false
decl_stmt|;
try|try
block|{
for|for
control|(
name|HColumnDescriptor
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
operator|!
name|hcd
operator|.
name|isMobEnabled
argument_list|()
condition|)
block|{
continue|continue;
block|}
if|if
condition|(
operator|!
name|reported
condition|)
block|{
name|master
operator|.
name|reportMobCompactionStart
argument_list|(
name|htd
operator|.
name|getTableName
argument_list|()
argument_list|)
expr_stmt|;
name|reported
operator|=
literal|true
expr_stmt|;
block|}
name|MobUtils
operator|.
name|doMobCompaction
argument_list|(
name|master
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|master
operator|.
name|getFileSystem
argument_list|()
argument_list|,
name|htd
operator|.
name|getTableName
argument_list|()
argument_list|,
name|hcd
argument_list|,
name|pool
argument_list|,
name|tableLockManager
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
block|}
finally|finally
block|{
if|if
condition|(
name|reported
condition|)
block|{
name|master
operator|.
name|reportMobCompactionEnd
argument_list|(
name|htd
operator|.
name|getTableName
argument_list|()
argument_list|)
expr_stmt|;
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
literal|"Failed to compact mob files"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|protected
name|void
name|cleanup
parameter_list|()
block|{
name|super
operator|.
name|cleanup
argument_list|()
expr_stmt|;
name|pool
operator|.
name|shutdown
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

