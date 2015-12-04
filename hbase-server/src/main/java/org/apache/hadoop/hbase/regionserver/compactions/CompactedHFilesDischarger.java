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
name|regionserver
operator|.
name|compactions
package|;
end_package

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
name|regionserver
operator|.
name|Region
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
name|Store
import|;
end_import

begin_comment
comment|/**  * A chore service that periodically cleans up the compacted files when there are no active readers  * using those compacted files and also helps in clearing the block cache with these compacted  * file entries  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|CompactedHFilesDischarger
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
name|CompactedHFilesDischarger
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|Region
name|region
decl_stmt|;
comment|/**    * @param period the period of time to sleep between each run    * @param stopper the stopper    * @param region the store to identify the family name    */
specifier|public
name|CompactedHFilesDischarger
parameter_list|(
specifier|final
name|int
name|period
parameter_list|,
specifier|final
name|Stoppable
name|stopper
parameter_list|,
specifier|final
name|Region
name|region
parameter_list|)
block|{
comment|// Need to add the config classes
name|super
argument_list|(
literal|"CompactedHFilesCleaner"
argument_list|,
name|stopper
argument_list|,
name|period
argument_list|)
expr_stmt|;
name|this
operator|.
name|region
operator|=
name|region
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|chore
parameter_list|()
block|{
if|if
condition|(
name|LOG
operator|.
name|isTraceEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|trace
argument_list|(
literal|"Started the compacted hfiles cleaner for the region "
operator|+
name|this
operator|.
name|region
operator|.
name|getRegionInfo
argument_list|()
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|Store
name|store
range|:
name|region
operator|.
name|getStores
argument_list|()
control|)
block|{
try|try
block|{
name|store
operator|.
name|closeAndArchiveCompactedFiles
argument_list|()
expr_stmt|;
if|if
condition|(
name|LOG
operator|.
name|isTraceEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|trace
argument_list|(
literal|"Completed archiving the compacted files for the region "
operator|+
name|this
operator|.
name|region
operator|.
name|getRegionInfo
argument_list|()
operator|+
literal|" under the store "
operator|+
name|store
operator|.
name|getColumnFamilyName
argument_list|()
argument_list|)
expr_stmt|;
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
literal|"Exception while trying to close and archive the comapcted store files of the store  "
operator|+
name|store
operator|.
name|getColumnFamilyName
argument_list|()
operator|+
literal|" in the region "
operator|+
name|this
operator|.
name|region
operator|.
name|getRegionInfo
argument_list|()
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|LOG
operator|.
name|isTraceEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|trace
argument_list|(
literal|"Completed the compacted hfiles cleaner for the region "
operator|+
name|this
operator|.
name|region
operator|.
name|getRegionInfo
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

