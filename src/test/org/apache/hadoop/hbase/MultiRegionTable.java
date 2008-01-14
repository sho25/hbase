begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2007 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|ConcurrentModificationException
import|;
end_import

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
name|SortedMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|TreeMap
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
name|fs
operator|.
name|FileSystem
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
name|hbase
operator|.
name|util
operator|.
name|Writables
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

begin_comment
comment|/**  * Utility class to build a table of multiple regions.  */
end_comment

begin_class
specifier|public
class|class
name|MultiRegionTable
extends|extends
name|HBaseTestCase
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
name|MultiRegionTable
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
comment|/**    * Make a multi-region table.  Presumption is that table already exists and    * that there is only one regionserver. Makes it multi-region by filling with    * data and provoking splits. Asserts parent region is cleaned up after its    * daughter splits release all references.    * @param conf    * @param cluster    * @param fs    * @param tableName    * @param columnName    * @throws IOException    */
annotation|@
name|SuppressWarnings
argument_list|(
literal|"null"
argument_list|)
specifier|public
specifier|static
name|void
name|makeMultiRegionTable
parameter_list|(
name|HBaseConfiguration
name|conf
parameter_list|,
name|MiniHBaseCluster
name|cluster
parameter_list|,
name|FileSystem
name|fs
parameter_list|,
name|String
name|tableName
parameter_list|,
name|String
name|columnName
parameter_list|)
throws|throws
name|IOException
block|{
specifier|final
name|int
name|retries
init|=
literal|10
decl_stmt|;
specifier|final
name|long
name|waitTime
init|=
literal|20L
operator|*
literal|1000L
decl_stmt|;
comment|// This size should make it so we always split using the addContent
comment|// below.  After adding all data, the first region is 1.3M. Should
comment|// set max filesize to be<= 1M.
name|assertTrue
argument_list|(
name|conf
operator|.
name|getLong
argument_list|(
literal|"hbase.hregion.max.filesize"
argument_list|,
name|HConstants
operator|.
name|DEFAULT_MAX_FILE_SIZE
argument_list|)
operator|<=
literal|1024
operator|*
literal|1024
argument_list|)
expr_stmt|;
name|assertNotNull
argument_list|(
name|fs
argument_list|)
expr_stmt|;
name|Path
name|d
init|=
name|fs
operator|.
name|makeQualified
argument_list|(
operator|new
name|Path
argument_list|(
name|conf
operator|.
name|get
argument_list|(
name|HConstants
operator|.
name|HBASE_DIR
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
comment|// Get connection on the meta table and get count of rows.
name|HTable
name|meta
init|=
operator|new
name|HTable
argument_list|(
name|conf
argument_list|,
name|HConstants
operator|.
name|META_TABLE_NAME
argument_list|)
decl_stmt|;
name|int
name|count
init|=
name|count
argument_list|(
name|meta
argument_list|,
name|tableName
argument_list|)
decl_stmt|;
name|HTable
name|t
init|=
operator|new
name|HTable
argument_list|(
name|conf
argument_list|,
operator|new
name|Text
argument_list|(
name|tableName
argument_list|)
argument_list|)
decl_stmt|;
comment|// Get the parent region here now.
name|HRegionInfo
name|parent
init|=
name|t
operator|.
name|getRegionLocation
argument_list|(
name|HConstants
operator|.
name|EMPTY_START_ROW
argument_list|)
operator|.
name|getRegionInfo
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Parent region "
operator|+
name|parent
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
comment|// Now add content.
name|addContent
argument_list|(
operator|new
name|HTableIncommon
argument_list|(
name|t
argument_list|)
argument_list|,
name|columnName
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Finished content loading"
argument_list|)
expr_stmt|;
comment|// All is running in the one JVM so I should be able to get the single
comment|// region instance and bring on a split. Presumption is that there is only
comment|// one regionserver.   Of not, the split may already have happened by the
comment|// time we got here.  If so, then the region found when we go searching
comment|// with EMPTY_START_ROW will be one of the unsplittable daughters.
name|HRegionInfo
name|hri
init|=
literal|null
decl_stmt|;
name|HRegion
name|r
init|=
literal|null
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
literal|30
condition|;
name|i
operator|++
control|)
block|{
name|hri
operator|=
name|t
operator|.
name|getRegionLocation
argument_list|(
name|HConstants
operator|.
name|EMPTY_START_ROW
argument_list|)
operator|.
name|getRegionInfo
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Region location: "
operator|+
name|hri
argument_list|)
expr_stmt|;
name|r
operator|=
name|cluster
operator|.
name|getRegionThreads
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getRegionServer
argument_list|()
operator|.
name|onlineRegions
operator|.
name|get
argument_list|(
name|hri
operator|.
name|getRegionName
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|r
operator|!=
literal|null
condition|)
block|{
break|break;
block|}
try|try
block|{
name|Thread
operator|.
name|sleep
argument_list|(
literal|1000
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Waiting on region to come online"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
comment|// Flush the cache
name|cluster
operator|.
name|getRegionThreads
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getRegionServer
argument_list|()
operator|.
name|getCacheFlushListener
argument_list|()
operator|.
name|flushRequested
argument_list|(
name|r
argument_list|)
expr_stmt|;
comment|// Now, wait until split makes it into the meta table.
name|int
name|oldCount
init|=
name|count
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|retries
condition|;
name|i
operator|++
control|)
block|{
name|count
operator|=
name|count
argument_list|(
name|meta
argument_list|,
name|tableName
argument_list|)
expr_stmt|;
if|if
condition|(
name|count
operator|>
name|oldCount
condition|)
block|{
break|break;
block|}
try|try
block|{
name|Thread
operator|.
name|sleep
argument_list|(
name|waitTime
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
comment|// continue
block|}
block|}
if|if
condition|(
name|count
operator|<=
name|oldCount
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Failed waiting on splits to show up"
argument_list|)
throw|;
block|}
comment|// Get info on the parent from the meta table.  Pass in 'hri'. Its the
comment|// region we have been dealing with up to this. Its the parent of the
comment|// region split.
name|Map
argument_list|<
name|Text
argument_list|,
name|byte
index|[]
argument_list|>
name|data
init|=
name|getSplitParentInfo
argument_list|(
name|meta
argument_list|,
name|parent
argument_list|)
decl_stmt|;
name|parent
operator|=
name|Writables
operator|.
name|getHRegionInfoOrNull
argument_list|(
name|data
operator|.
name|get
argument_list|(
name|HConstants
operator|.
name|COL_REGIONINFO
argument_list|)
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Found parent region: "
operator|+
name|parent
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|parent
operator|.
name|isOffline
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|parent
operator|.
name|isSplit
argument_list|()
argument_list|)
expr_stmt|;
name|HRegionInfo
name|splitA
init|=
name|Writables
operator|.
name|getHRegionInfoOrNull
argument_list|(
name|data
operator|.
name|get
argument_list|(
name|HConstants
operator|.
name|COL_SPLITA
argument_list|)
argument_list|)
decl_stmt|;
name|HRegionInfo
name|splitB
init|=
name|Writables
operator|.
name|getHRegionInfoOrNull
argument_list|(
name|data
operator|.
name|get
argument_list|(
name|HConstants
operator|.
name|COL_SPLITB
argument_list|)
argument_list|)
decl_stmt|;
name|Path
name|parentDir
init|=
name|HRegion
operator|.
name|getRegionDir
argument_list|(
operator|new
name|Path
argument_list|(
name|d
argument_list|,
name|tableName
argument_list|)
argument_list|,
name|parent
operator|.
name|getEncodedName
argument_list|()
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|fs
operator|.
name|exists
argument_list|(
name|parentDir
argument_list|)
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Split happened. Parent is "
operator|+
name|parent
operator|.
name|getRegionName
argument_list|()
argument_list|)
expr_stmt|;
comment|// Recalibrate will cause us to wait on new regions' deployment
name|recalibrate
argument_list|(
name|t
argument_list|,
operator|new
name|Text
argument_list|(
name|columnName
argument_list|)
argument_list|,
name|retries
argument_list|,
name|waitTime
argument_list|)
expr_stmt|;
if|if
condition|(
name|splitA
operator|==
literal|null
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"splitA was already null. Assuming it was previously compacted."
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Daughter splitA: "
operator|+
name|splitA
operator|.
name|getRegionName
argument_list|()
argument_list|)
expr_stmt|;
comment|// Compact a region at a time so we can test case where one region has
comment|// no references but the other still has some
name|compact
argument_list|(
name|cluster
argument_list|,
name|splitA
argument_list|)
expr_stmt|;
comment|// Wait till the parent only has reference to remaining split, one that
comment|// still has references.
while|while
condition|(
literal|true
condition|)
block|{
name|data
operator|=
name|getSplitParentInfo
argument_list|(
name|meta
argument_list|,
name|parent
argument_list|)
expr_stmt|;
if|if
condition|(
name|data
operator|==
literal|null
operator|||
name|data
operator|.
name|size
argument_list|()
operator|==
literal|3
condition|)
block|{
try|try
block|{
name|Thread
operator|.
name|sleep
argument_list|(
name|waitTime
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
comment|// continue
block|}
continue|continue;
block|}
break|break;
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"Parent split info returned "
operator|+
name|data
operator|.
name|keySet
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|splitB
operator|==
literal|null
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"splitB was already null. Assuming it was previously compacted."
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Daughter splitB: "
operator|+
name|splitA
operator|.
name|getRegionName
argument_list|()
argument_list|)
expr_stmt|;
comment|// Call second split.
name|compact
argument_list|(
name|cluster
argument_list|,
name|splitB
argument_list|)
expr_stmt|;
block|}
comment|// Now wait until parent disappears.
name|LOG
operator|.
name|info
argument_list|(
literal|"Waiting on parent "
operator|+
name|parent
operator|.
name|getRegionName
argument_list|()
operator|+
literal|" to disappear"
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|retries
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|getSplitParentInfo
argument_list|(
name|meta
argument_list|,
name|parent
argument_list|)
operator|==
literal|null
condition|)
block|{
break|break;
block|}
try|try
block|{
name|Thread
operator|.
name|sleep
argument_list|(
name|waitTime
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
comment|// continue
block|}
block|}
name|assertNull
argument_list|(
name|getSplitParentInfo
argument_list|(
name|meta
argument_list|,
name|parent
argument_list|)
argument_list|)
expr_stmt|;
comment|// Assert cleaned up.
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|retries
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
operator|!
name|fs
operator|.
name|exists
argument_list|(
name|parentDir
argument_list|)
condition|)
block|{
break|break;
block|}
try|try
block|{
name|Thread
operator|.
name|sleep
argument_list|(
name|waitTime
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
comment|// continue
block|}
block|}
name|assertFalse
argument_list|(
name|fs
operator|.
name|exists
argument_list|(
name|parentDir
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/*    * Count of regions in passed meta table.    * @param t    * @param column    * @return    * @throws IOException    */
specifier|private
specifier|static
name|int
name|count
parameter_list|(
specifier|final
name|HTable
name|t
parameter_list|,
specifier|final
name|String
name|tableName
parameter_list|)
throws|throws
name|IOException
block|{
name|int
name|size
init|=
literal|0
decl_stmt|;
name|Text
index|[]
name|cols
init|=
operator|new
name|Text
index|[]
block|{
name|HConstants
operator|.
name|COLUMN_FAMILY
block|}
decl_stmt|;
name|HScannerInterface
name|s
init|=
name|t
operator|.
name|obtainScanner
argument_list|(
name|cols
argument_list|,
name|HConstants
operator|.
name|EMPTY_START_ROW
argument_list|,
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|,
literal|null
argument_list|)
decl_stmt|;
try|try
block|{
name|HStoreKey
name|curKey
init|=
operator|new
name|HStoreKey
argument_list|()
decl_stmt|;
name|TreeMap
argument_list|<
name|Text
argument_list|,
name|byte
index|[]
argument_list|>
name|curVals
init|=
operator|new
name|TreeMap
argument_list|<
name|Text
argument_list|,
name|byte
index|[]
argument_list|>
argument_list|()
decl_stmt|;
while|while
condition|(
name|s
operator|.
name|next
argument_list|(
name|curKey
argument_list|,
name|curVals
argument_list|)
condition|)
block|{
name|HRegionInfo
name|hri
init|=
name|Writables
operator|.
name|getHRegionInfoOrNull
argument_list|(
name|curVals
operator|.
name|get
argument_list|(
name|HConstants
operator|.
name|COL_REGIONINFO
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|hri
operator|.
name|getTableDesc
argument_list|()
operator|.
name|getName
argument_list|()
operator|.
name|toString
argument_list|()
operator|.
name|equals
argument_list|(
name|tableName
argument_list|)
condition|)
block|{
name|size
operator|++
expr_stmt|;
block|}
block|}
return|return
name|size
return|;
block|}
finally|finally
block|{
if|if
condition|(
name|s
operator|!=
literal|null
condition|)
block|{
name|s
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
block|}
comment|/*    * @return Return row info for passed in region or null if not found in scan.    */
specifier|private
specifier|static
name|Map
argument_list|<
name|Text
argument_list|,
name|byte
index|[]
argument_list|>
name|getSplitParentInfo
parameter_list|(
specifier|final
name|HTable
name|t
parameter_list|,
specifier|final
name|HRegionInfo
name|parent
parameter_list|)
throws|throws
name|IOException
block|{
name|HScannerInterface
name|s
init|=
name|t
operator|.
name|obtainScanner
argument_list|(
name|HConstants
operator|.
name|COLUMN_FAMILY_ARRAY
argument_list|,
name|HConstants
operator|.
name|EMPTY_START_ROW
argument_list|,
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|,
literal|null
argument_list|)
decl_stmt|;
try|try
block|{
name|HStoreKey
name|curKey
init|=
operator|new
name|HStoreKey
argument_list|()
decl_stmt|;
name|TreeMap
argument_list|<
name|Text
argument_list|,
name|byte
index|[]
argument_list|>
name|curVals
init|=
operator|new
name|TreeMap
argument_list|<
name|Text
argument_list|,
name|byte
index|[]
argument_list|>
argument_list|()
decl_stmt|;
while|while
condition|(
name|s
operator|.
name|next
argument_list|(
name|curKey
argument_list|,
name|curVals
argument_list|)
condition|)
block|{
name|HRegionInfo
name|hri
init|=
name|Writables
operator|.
name|getHRegionInfoOrNull
argument_list|(
name|curVals
operator|.
name|get
argument_list|(
name|HConstants
operator|.
name|COL_REGIONINFO
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|hri
operator|==
literal|null
condition|)
block|{
continue|continue;
block|}
comment|// Make sure I get the parent.
if|if
condition|(
name|hri
operator|.
name|getRegionName
argument_list|()
operator|.
name|toString
argument_list|()
operator|.
name|equals
argument_list|(
name|parent
operator|.
name|getRegionName
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
operator|&&
name|hri
operator|.
name|getRegionId
argument_list|()
operator|==
name|parent
operator|.
name|getRegionId
argument_list|()
condition|)
block|{
return|return
name|curVals
return|;
block|}
block|}
return|return
literal|null
return|;
block|}
finally|finally
block|{
name|s
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
comment|/*    * Recalibrate passed in HTable.  Run after change in region geography.    * Open a scanner on the table. This will force HTable to recalibrate    * and in doing so, will force us to wait until the new child regions    * come on-line (since they are no longer automatically served by the     * HRegionServer that was serving the parent. In this test they will    * end up on the same server (since there is only one), but we have to    * wait until the master assigns them.     * @param t    * @param retries    */
specifier|private
specifier|static
name|void
name|recalibrate
parameter_list|(
specifier|final
name|HTable
name|t
parameter_list|,
specifier|final
name|Text
name|column
parameter_list|,
specifier|final
name|int
name|retries
parameter_list|,
specifier|final
name|long
name|waitTime
parameter_list|)
throws|throws
name|IOException
block|{
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|retries
condition|;
name|i
operator|++
control|)
block|{
try|try
block|{
name|HScannerInterface
name|s
init|=
name|t
operator|.
name|obtainScanner
argument_list|(
operator|new
name|Text
index|[]
block|{
name|column
block|}
argument_list|,
name|HConstants
operator|.
name|EMPTY_START_ROW
argument_list|)
decl_stmt|;
try|try
block|{
name|HStoreKey
name|key
init|=
operator|new
name|HStoreKey
argument_list|()
decl_stmt|;
name|TreeMap
argument_list|<
name|Text
argument_list|,
name|byte
index|[]
argument_list|>
name|results
init|=
operator|new
name|TreeMap
argument_list|<
name|Text
argument_list|,
name|byte
index|[]
argument_list|>
argument_list|()
decl_stmt|;
name|s
operator|.
name|next
argument_list|(
name|key
argument_list|,
name|results
argument_list|)
expr_stmt|;
break|break;
block|}
finally|finally
block|{
name|s
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|NotServingRegionException
name|x
parameter_list|)
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"it's alright"
argument_list|)
expr_stmt|;
try|try
block|{
name|Thread
operator|.
name|sleep
argument_list|(
name|waitTime
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
comment|// continue
block|}
block|}
block|}
block|}
comment|/*    * Compact the passed in region<code>r</code>.     * @param cluster    * @param r    * @throws IOException    */
specifier|private
specifier|static
name|void
name|compact
parameter_list|(
specifier|final
name|MiniHBaseCluster
name|cluster
parameter_list|,
specifier|final
name|HRegionInfo
name|r
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|r
operator|==
literal|null
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Passed region is null"
argument_list|)
expr_stmt|;
return|return;
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"Starting compaction"
argument_list|)
expr_stmt|;
for|for
control|(
name|LocalHBaseCluster
operator|.
name|RegionServerThread
name|thread
range|:
name|cluster
operator|.
name|getRegionThreads
argument_list|()
control|)
block|{
name|SortedMap
argument_list|<
name|Text
argument_list|,
name|HRegion
argument_list|>
name|regions
init|=
name|thread
operator|.
name|getRegionServer
argument_list|()
operator|.
name|onlineRegions
decl_stmt|;
comment|// Retry if ConcurrentModification... alternative of sync'ing is not
comment|// worth it for sake of unit test.
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
literal|10
condition|;
name|i
operator|++
control|)
block|{
try|try
block|{
for|for
control|(
name|HRegion
name|online
range|:
name|regions
operator|.
name|values
argument_list|()
control|)
block|{
if|if
condition|(
name|online
operator|.
name|getRegionName
argument_list|()
operator|.
name|toString
argument_list|()
operator|.
name|equals
argument_list|(
name|r
operator|.
name|getRegionName
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
condition|)
block|{
name|online
operator|.
name|compactStores
argument_list|()
expr_stmt|;
block|}
block|}
break|break;
block|}
catch|catch
parameter_list|(
name|ConcurrentModificationException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Retrying because ..."
operator|+
name|e
operator|.
name|toString
argument_list|()
operator|+
literal|" -- one or "
operator|+
literal|"two should be fine"
argument_list|)
expr_stmt|;
continue|continue;
block|}
block|}
block|}
block|}
block|}
end_class

end_unit

