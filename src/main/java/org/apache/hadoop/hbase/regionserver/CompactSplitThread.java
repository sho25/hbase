begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2010 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|concurrent
operator|.
name|TimeUnit
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
name|locks
operator|.
name|ReentrantLock
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
name|RemoteExceptionHandler
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
name|StringUtils
import|;
end_import

begin_comment
comment|/**  * Compact region on request and then run split if appropriate  */
end_comment

begin_class
specifier|public
class|class
name|CompactSplitThread
extends|extends
name|Thread
implements|implements
name|CompactionRequestor
block|{
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|CompactSplitThread
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|long
name|frequency
decl_stmt|;
specifier|private
specifier|final
name|ReentrantLock
name|lock
init|=
operator|new
name|ReentrantLock
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|HRegionServer
name|server
decl_stmt|;
specifier|private
specifier|final
name|Configuration
name|conf
decl_stmt|;
specifier|private
specifier|final
name|PriorityCompactionQueue
name|compactionQueue
init|=
operator|new
name|PriorityCompactionQueue
argument_list|()
decl_stmt|;
comment|/* The default priority for user-specified compaction requests.    * The user gets top priority unless we have blocking compactions. (Pri<= 0)    */
specifier|public
specifier|static
specifier|final
name|int
name|PRIORITY_USER
init|=
literal|1
decl_stmt|;
comment|/**    * Splitting should not take place if the total number of regions exceed this.    * This is not a hard limit to the number of regions but it is a guideline to    * stop splitting after number of online regions is greater than this.    */
specifier|private
name|int
name|regionSplitLimit
decl_stmt|;
comment|/** @param server */
specifier|public
name|CompactSplitThread
parameter_list|(
name|HRegionServer
name|server
parameter_list|)
block|{
name|super
argument_list|()
expr_stmt|;
name|this
operator|.
name|server
operator|=
name|server
expr_stmt|;
name|this
operator|.
name|conf
operator|=
name|server
operator|.
name|getConfiguration
argument_list|()
expr_stmt|;
name|this
operator|.
name|regionSplitLimit
operator|=
name|conf
operator|.
name|getInt
argument_list|(
literal|"hbase.regionserver.regionSplitLimit"
argument_list|,
name|Integer
operator|.
name|MAX_VALUE
argument_list|)
expr_stmt|;
name|this
operator|.
name|frequency
operator|=
name|conf
operator|.
name|getLong
argument_list|(
literal|"hbase.regionserver.thread.splitcompactcheckfrequency"
argument_list|,
literal|20
operator|*
literal|1000
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
while|while
condition|(
operator|!
name|this
operator|.
name|server
operator|.
name|isStopped
argument_list|()
condition|)
block|{
name|HRegion
name|r
init|=
literal|null
decl_stmt|;
try|try
block|{
name|r
operator|=
name|compactionQueue
operator|.
name|poll
argument_list|(
name|this
operator|.
name|frequency
argument_list|,
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
expr_stmt|;
if|if
condition|(
name|r
operator|!=
literal|null
condition|)
block|{
name|lock
operator|.
name|lock
argument_list|()
expr_stmt|;
try|try
block|{
if|if
condition|(
operator|!
name|this
operator|.
name|server
operator|.
name|isStopped
argument_list|()
condition|)
block|{
comment|// Don't interrupt us while we are working
name|byte
index|[]
name|midKey
init|=
name|r
operator|.
name|compactStores
argument_list|()
decl_stmt|;
if|if
condition|(
name|r
operator|.
name|getLastCompactInfo
argument_list|()
operator|!=
literal|null
condition|)
block|{
comment|// compaction aborted?
name|this
operator|.
name|server
operator|.
name|getMetrics
argument_list|()
operator|.
name|addCompaction
argument_list|(
name|r
operator|.
name|getLastCompactInfo
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|shouldSplitRegion
argument_list|()
operator|&&
name|midKey
operator|!=
literal|null
operator|&&
operator|!
name|this
operator|.
name|server
operator|.
name|isStopped
argument_list|()
condition|)
block|{
name|split
argument_list|(
name|r
argument_list|,
name|midKey
argument_list|)
expr_stmt|;
block|}
block|}
block|}
finally|finally
block|{
name|lock
operator|.
name|unlock
argument_list|()
expr_stmt|;
block|}
block|}
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|ex
parameter_list|)
block|{
continue|continue;
block|}
catch|catch
parameter_list|(
name|IOException
name|ex
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Compaction/Split failed for region "
operator|+
name|r
operator|.
name|getRegionNameAsString
argument_list|()
argument_list|,
name|RemoteExceptionHandler
operator|.
name|checkIOException
argument_list|(
name|ex
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|server
operator|.
name|checkFileSystem
argument_list|()
condition|)
block|{
break|break;
block|}
block|}
catch|catch
parameter_list|(
name|Exception
name|ex
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Compaction failed"
operator|+
operator|(
name|r
operator|!=
literal|null
condition|?
operator|(
literal|" for region "
operator|+
name|r
operator|.
name|getRegionNameAsString
argument_list|()
operator|)
else|:
literal|""
operator|)
argument_list|,
name|ex
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|server
operator|.
name|checkFileSystem
argument_list|()
condition|)
block|{
break|break;
block|}
block|}
block|}
name|compactionQueue
operator|.
name|clear
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
name|getName
argument_list|()
operator|+
literal|" exiting"
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|synchronized
name|void
name|requestCompaction
parameter_list|(
specifier|final
name|HRegion
name|r
parameter_list|,
specifier|final
name|String
name|why
parameter_list|)
block|{
name|requestCompaction
argument_list|(
name|r
argument_list|,
literal|false
argument_list|,
name|why
argument_list|,
name|r
operator|.
name|getCompactPriority
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|synchronized
name|void
name|requestCompaction
parameter_list|(
specifier|final
name|HRegion
name|r
parameter_list|,
specifier|final
name|String
name|why
parameter_list|,
name|int
name|p
parameter_list|)
block|{
name|requestCompaction
argument_list|(
name|r
argument_list|,
literal|false
argument_list|,
name|why
argument_list|,
name|p
argument_list|)
expr_stmt|;
block|}
comment|/**    * @param r HRegion store belongs to    * @param force Whether next compaction should be major    * @param why Why compaction requested -- used in debug messages    */
specifier|public
specifier|synchronized
name|void
name|requestCompaction
parameter_list|(
specifier|final
name|HRegion
name|r
parameter_list|,
specifier|final
name|boolean
name|force
parameter_list|,
specifier|final
name|String
name|why
parameter_list|,
name|int
name|priority
parameter_list|)
block|{
if|if
condition|(
name|this
operator|.
name|server
operator|.
name|isStopped
argument_list|()
condition|)
block|{
return|return;
block|}
comment|// tell the region to major-compact (and don't downgrade it)
if|if
condition|(
name|force
condition|)
block|{
name|r
operator|.
name|setForceMajorCompaction
argument_list|(
name|force
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|compactionQueue
operator|.
name|add
argument_list|(
name|r
argument_list|,
name|priority
argument_list|)
operator|&&
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Compaction "
operator|+
operator|(
name|force
condition|?
literal|"(major) "
else|:
literal|""
operator|)
operator|+
literal|"requested for region "
operator|+
name|r
operator|.
name|getRegionNameAsString
argument_list|()
operator|+
operator|(
name|why
operator|!=
literal|null
operator|&&
operator|!
name|why
operator|.
name|isEmpty
argument_list|()
condition|?
literal|" because: "
operator|+
name|why
else|:
literal|""
operator|)
operator|+
literal|"; Priority: "
operator|+
name|priority
operator|+
literal|"; Compaction queue size: "
operator|+
name|compactionQueue
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|split
parameter_list|(
specifier|final
name|HRegion
name|parent
parameter_list|,
specifier|final
name|byte
index|[]
name|midKey
parameter_list|)
throws|throws
name|IOException
block|{
specifier|final
name|long
name|startTime
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|SplitTransaction
name|st
init|=
operator|new
name|SplitTransaction
argument_list|(
name|parent
argument_list|,
name|midKey
argument_list|)
decl_stmt|;
comment|// If prepare does not return true, for some reason -- logged inside in
comment|// the prepare call -- we are not ready to split just now.  Just return.
if|if
condition|(
operator|!
name|st
operator|.
name|prepare
argument_list|()
condition|)
return|return;
try|try
block|{
name|st
operator|.
name|execute
argument_list|(
name|this
operator|.
name|server
argument_list|,
name|this
operator|.
name|server
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|ioe
parameter_list|)
block|{
try|try
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Running rollback of failed split of "
operator|+
name|parent
operator|.
name|getRegionNameAsString
argument_list|()
operator|+
literal|"; "
operator|+
name|ioe
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
name|st
operator|.
name|rollback
argument_list|(
name|this
operator|.
name|server
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Successful rollback of failed split of "
operator|+
name|parent
operator|.
name|getRegionNameAsString
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|RuntimeException
name|e
parameter_list|)
block|{
comment|// If failed rollback, kill this server to avoid having a hole in table.
name|LOG
operator|.
name|info
argument_list|(
literal|"Failed rollback of failed split of "
operator|+
name|parent
operator|.
name|getRegionNameAsString
argument_list|()
operator|+
literal|" -- aborting server"
argument_list|,
name|e
argument_list|)
expr_stmt|;
name|this
operator|.
name|server
operator|.
name|abort
argument_list|(
literal|"Failed split"
argument_list|)
expr_stmt|;
block|}
return|return;
block|}
comment|// Now tell the master about the new regions.  If we fail here, its OK.
comment|// Basescanner will do fix up.  And reporting split to master is going away.
comment|// TODO: Verify this still holds in new master rewrite.
name|this
operator|.
name|server
operator|.
name|reportSplit
argument_list|(
name|parent
operator|.
name|getRegionInfo
argument_list|()
argument_list|,
name|st
operator|.
name|getFirstDaughter
argument_list|()
argument_list|,
name|st
operator|.
name|getSecondDaughter
argument_list|()
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Region split, META updated, and report to master. Parent="
operator|+
name|parent
operator|.
name|getRegionInfo
argument_list|()
operator|.
name|getRegionNameAsString
argument_list|()
operator|+
literal|", new regions: "
operator|+
name|st
operator|.
name|getFirstDaughter
argument_list|()
operator|.
name|getRegionNameAsString
argument_list|()
operator|+
literal|", "
operator|+
name|st
operator|.
name|getSecondDaughter
argument_list|()
operator|.
name|getRegionNameAsString
argument_list|()
operator|+
literal|". Split took "
operator|+
name|StringUtils
operator|.
name|formatTimeDiff
argument_list|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|,
name|startTime
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * Only interrupt once it's done with a run through the work loop.    */
name|void
name|interruptIfNecessary
parameter_list|()
block|{
if|if
condition|(
name|lock
operator|.
name|tryLock
argument_list|()
condition|)
block|{
try|try
block|{
name|this
operator|.
name|interrupt
argument_list|()
expr_stmt|;
block|}
finally|finally
block|{
name|lock
operator|.
name|unlock
argument_list|()
expr_stmt|;
block|}
block|}
block|}
comment|/**    * Returns the current size of the queue containing regions that are    * processed.    *    * @return The current size of the regions queue.    */
specifier|public
name|int
name|getCompactionQueueSize
parameter_list|()
block|{
return|return
name|compactionQueue
operator|.
name|size
argument_list|()
return|;
block|}
specifier|private
name|boolean
name|shouldSplitRegion
parameter_list|()
block|{
return|return
operator|(
name|regionSplitLimit
operator|>
name|server
operator|.
name|getNumberOfOnlineRegions
argument_list|()
operator|)
return|;
block|}
comment|/**    * @return the regionSplitLimit    */
specifier|public
name|int
name|getRegionSplitLimit
parameter_list|()
block|{
return|return
name|this
operator|.
name|regionSplitLimit
return|;
block|}
block|}
end_class

end_unit

