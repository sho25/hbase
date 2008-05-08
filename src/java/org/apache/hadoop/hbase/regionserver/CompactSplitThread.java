begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2008 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|HashSet
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
name|BlockingQueue
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
name|LinkedBlockingQueue
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
name|hadoop
operator|.
name|io
operator|.
name|Text
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
name|client
operator|.
name|HTable
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
name|HBaseConfiguration
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
name|hbase
operator|.
name|HRegionInfo
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
name|HConstants
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
name|BatchUpdate
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
name|util
operator|.
name|Writables
import|;
end_import

begin_comment
comment|/**   * Compact region on request and then run split if appropriate  */
end_comment

begin_class
class|class
name|CompactSplitThread
extends|extends
name|Thread
implements|implements
name|RegionUnavailableListener
implements|,
name|HConstants
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
name|HTable
name|root
init|=
literal|null
decl_stmt|;
specifier|private
name|HTable
name|meta
init|=
literal|null
decl_stmt|;
specifier|private
specifier|volatile
name|long
name|startTime
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
name|HBaseConfiguration
name|conf
decl_stmt|;
specifier|private
specifier|final
name|BlockingQueue
argument_list|<
name|HRegion
argument_list|>
name|compactionQueue
init|=
operator|new
name|LinkedBlockingQueue
argument_list|<
name|HRegion
argument_list|>
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|HashSet
argument_list|<
name|HRegion
argument_list|>
name|regionsInQueue
init|=
operator|new
name|HashSet
argument_list|<
name|HRegion
argument_list|>
argument_list|()
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
name|conf
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
comment|/** {@inheritDoc} */
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
name|server
operator|.
name|isStopRequested
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
synchronized|synchronized
init|(
name|regionsInQueue
init|)
block|{
name|regionsInQueue
operator|.
name|remove
argument_list|(
name|r
argument_list|)
expr_stmt|;
block|}
name|lock
operator|.
name|lock
argument_list|()
expr_stmt|;
try|try
block|{
comment|// Don't interrupt us while we are working
name|Text
name|midKey
init|=
name|r
operator|.
name|compactStores
argument_list|()
decl_stmt|;
if|if
condition|(
name|midKey
operator|!=
literal|null
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
name|getRegionName
argument_list|()
operator|)
else|:
literal|""
operator|)
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
name|getRegionName
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
name|regionsInQueue
operator|.
name|clear
argument_list|()
expr_stmt|;
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
comment|/**    * @param r HRegion store belongs to    */
specifier|public
specifier|synchronized
name|void
name|compactionRequested
parameter_list|(
name|HRegion
name|r
parameter_list|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Compaction requested for region: "
operator|+
name|r
operator|.
name|getRegionName
argument_list|()
argument_list|)
expr_stmt|;
synchronized|synchronized
init|(
name|regionsInQueue
init|)
block|{
if|if
condition|(
operator|!
name|regionsInQueue
operator|.
name|contains
argument_list|(
name|r
argument_list|)
condition|)
block|{
name|compactionQueue
operator|.
name|add
argument_list|(
name|r
argument_list|)
expr_stmt|;
name|regionsInQueue
operator|.
name|add
argument_list|(
name|r
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|private
name|void
name|split
parameter_list|(
specifier|final
name|HRegion
name|region
parameter_list|,
specifier|final
name|Text
name|midKey
parameter_list|)
throws|throws
name|IOException
block|{
specifier|final
name|HRegionInfo
name|oldRegionInfo
init|=
name|region
operator|.
name|getRegionInfo
argument_list|()
decl_stmt|;
specifier|final
name|HRegion
index|[]
name|newRegions
init|=
name|region
operator|.
name|splitRegion
argument_list|(
name|this
argument_list|,
name|midKey
argument_list|)
decl_stmt|;
if|if
condition|(
name|newRegions
operator|==
literal|null
condition|)
block|{
comment|// Didn't need to be split
return|return;
block|}
comment|// When a region is split, the META table needs to updated if we're
comment|// splitting a 'normal' region, and the ROOT table needs to be
comment|// updated if we are splitting a META region.
name|HTable
name|t
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|region
operator|.
name|getRegionInfo
argument_list|()
operator|.
name|isMetaTable
argument_list|()
condition|)
block|{
comment|// We need to update the root region
if|if
condition|(
name|this
operator|.
name|root
operator|==
literal|null
condition|)
block|{
name|this
operator|.
name|root
operator|=
operator|new
name|HTable
argument_list|(
name|conf
argument_list|,
name|ROOT_TABLE_NAME
argument_list|)
expr_stmt|;
block|}
name|t
operator|=
name|root
expr_stmt|;
block|}
else|else
block|{
comment|// For normal regions we need to update the meta region
if|if
condition|(
name|meta
operator|==
literal|null
condition|)
block|{
name|meta
operator|=
operator|new
name|HTable
argument_list|(
name|conf
argument_list|,
name|META_TABLE_NAME
argument_list|)
expr_stmt|;
block|}
name|t
operator|=
name|meta
expr_stmt|;
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"Updating "
operator|+
name|t
operator|.
name|getTableName
argument_list|()
operator|+
literal|" with region split info"
argument_list|)
expr_stmt|;
comment|// Mark old region as offline and split in META.
comment|// NOTE: there is no need for retry logic here. HTable does it for us.
name|oldRegionInfo
operator|.
name|setOffline
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|oldRegionInfo
operator|.
name|setSplit
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|BatchUpdate
name|update
init|=
operator|new
name|BatchUpdate
argument_list|(
name|oldRegionInfo
operator|.
name|getRegionName
argument_list|()
argument_list|)
decl_stmt|;
name|update
operator|.
name|put
argument_list|(
name|COL_REGIONINFO
argument_list|,
name|Writables
operator|.
name|getBytes
argument_list|(
name|oldRegionInfo
argument_list|)
argument_list|)
expr_stmt|;
name|update
operator|.
name|put
argument_list|(
name|COL_SPLITA
argument_list|,
name|Writables
operator|.
name|getBytes
argument_list|(
name|newRegions
index|[
literal|0
index|]
operator|.
name|getRegionInfo
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|update
operator|.
name|put
argument_list|(
name|COL_SPLITB
argument_list|,
name|Writables
operator|.
name|getBytes
argument_list|(
name|newRegions
index|[
literal|1
index|]
operator|.
name|getRegionInfo
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|t
operator|.
name|commit
argument_list|(
name|update
argument_list|)
expr_stmt|;
comment|// Add new regions to META
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|newRegions
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|update
operator|=
operator|new
name|BatchUpdate
argument_list|(
name|newRegions
index|[
name|i
index|]
operator|.
name|getRegionName
argument_list|()
argument_list|)
expr_stmt|;
name|update
operator|.
name|put
argument_list|(
name|COL_REGIONINFO
argument_list|,
name|Writables
operator|.
name|getBytes
argument_list|(
name|newRegions
index|[
name|i
index|]
operator|.
name|getRegionInfo
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|t
operator|.
name|commit
argument_list|(
name|update
argument_list|)
expr_stmt|;
block|}
comment|// Now tell the master about the new regions
if|if
condition|(
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
literal|"Reporting region split to master"
argument_list|)
expr_stmt|;
block|}
name|server
operator|.
name|reportSplit
argument_list|(
name|oldRegionInfo
argument_list|,
name|newRegions
index|[
literal|0
index|]
operator|.
name|getRegionInfo
argument_list|()
argument_list|,
name|newRegions
index|[
literal|1
index|]
operator|.
name|getRegionInfo
argument_list|()
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"region split, META updated, and report to master all"
operator|+
literal|" successful. Old region="
operator|+
name|oldRegionInfo
operator|.
name|toString
argument_list|()
operator|+
literal|", new regions: "
operator|+
name|newRegions
index|[
literal|0
index|]
operator|.
name|toString
argument_list|()
operator|+
literal|", "
operator|+
name|newRegions
index|[
literal|1
index|]
operator|.
name|toString
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
comment|// Do not serve the new regions. Let the Master assign them.
block|}
comment|/** {@inheritDoc} */
specifier|public
name|void
name|closing
parameter_list|(
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unused"
argument_list|)
specifier|final
name|Text
name|regionName
parameter_list|)
block|{   }
comment|/** {@inheritDoc} */
specifier|public
name|void
name|closed
parameter_list|(
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unused"
argument_list|)
specifier|final
name|Text
name|regionName
parameter_list|)
block|{   }
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
name|this
operator|.
name|interrupt
argument_list|()
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

