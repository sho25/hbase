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

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|log4j
operator|.
name|Level
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|log4j
operator|.
name|Logger
import|;
end_import

begin_comment
comment|/**  * {@Link TestHRegion} does a split but this TestCase adds testing of fast  * split and manufactures odd-ball split scenarios.  */
end_comment

begin_class
specifier|public
class|class
name|TestSplit
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
name|TestSplit
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
specifier|static
name|String
name|COLFAMILY_NAME1
init|=
literal|"colfamily1:"
decl_stmt|;
specifier|private
specifier|final
specifier|static
name|String
name|COLFAMILY_NAME2
init|=
literal|"colfamily2:"
decl_stmt|;
specifier|private
specifier|final
specifier|static
name|String
name|COLFAMILY_NAME3
init|=
literal|"colfamily3:"
decl_stmt|;
specifier|private
name|Path
name|testDir
init|=
literal|null
decl_stmt|;
specifier|private
name|FileSystem
name|fs
init|=
literal|null
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|char
name|FIRST_CHAR
init|=
literal|'a'
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|char
name|LAST_CHAR
init|=
literal|'z'
decl_stmt|;
comment|/** constructor */
specifier|public
name|TestSplit
parameter_list|()
block|{
name|Logger
operator|.
name|getRootLogger
argument_list|()
operator|.
name|setLevel
argument_list|(
name|Level
operator|.
name|WARN
argument_list|)
expr_stmt|;
name|Logger
operator|.
name|getLogger
argument_list|(
name|this
operator|.
name|getClass
argument_list|()
operator|.
name|getPackage
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
operator|.
name|setLevel
argument_list|(
name|Level
operator|.
name|DEBUG
argument_list|)
expr_stmt|;
block|}
comment|/** {@inheritDoc} */
annotation|@
name|Override
specifier|public
name|void
name|setUp
parameter_list|()
throws|throws
name|Exception
block|{
name|super
operator|.
name|setUp
argument_list|()
expr_stmt|;
name|this
operator|.
name|testDir
operator|=
name|getUnitTestdir
argument_list|(
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|this
operator|.
name|fs
operator|=
name|FileSystem
operator|.
name|getLocal
argument_list|(
name|this
operator|.
name|conf
argument_list|)
expr_stmt|;
if|if
condition|(
name|fs
operator|.
name|exists
argument_list|(
name|testDir
argument_list|)
condition|)
block|{
name|fs
operator|.
name|delete
argument_list|(
name|testDir
argument_list|)
expr_stmt|;
block|}
comment|// This size should make it so we always split using the addContent
comment|// below.  After adding all data, the first region is 1.3M
name|conf
operator|.
name|setLong
argument_list|(
literal|"hbase.hregion.max.filesize"
argument_list|,
literal|1024
operator|*
literal|128
argument_list|)
expr_stmt|;
block|}
comment|/** {@inheritDoc} */
annotation|@
name|Override
specifier|public
name|void
name|tearDown
parameter_list|()
throws|throws
name|Exception
block|{
if|if
condition|(
name|fs
operator|!=
literal|null
condition|)
block|{
try|try
block|{
if|if
condition|(
name|this
operator|.
name|fs
operator|.
name|exists
argument_list|(
name|testDir
argument_list|)
condition|)
block|{
name|this
operator|.
name|fs
operator|.
name|delete
argument_list|(
name|testDir
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
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
block|}
block|}
name|super
operator|.
name|tearDown
argument_list|()
expr_stmt|;
block|}
comment|/**    * Splits twice and verifies getting from each of the split regions.    * @throws Exception    */
specifier|public
name|void
name|testBasicSplit
parameter_list|()
throws|throws
name|Exception
block|{
name|HRegion
name|region
init|=
literal|null
decl_stmt|;
name|HLog
name|hlog
init|=
operator|new
name|HLog
argument_list|(
name|this
operator|.
name|fs
argument_list|,
name|this
operator|.
name|testDir
argument_list|,
name|this
operator|.
name|conf
argument_list|)
decl_stmt|;
try|try
block|{
name|HTableDescriptor
name|htd
init|=
name|createTableDescriptor
argument_list|(
name|getName
argument_list|()
argument_list|)
decl_stmt|;
name|HRegionInfo
name|hri
init|=
operator|new
name|HRegionInfo
argument_list|(
literal|1
argument_list|,
name|htd
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|region
operator|=
operator|new
name|HRegion
argument_list|(
name|testDir
argument_list|,
name|hlog
argument_list|,
name|fs
argument_list|,
name|this
operator|.
name|conf
argument_list|,
name|hri
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|basicSplit
argument_list|(
name|region
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
if|if
condition|(
name|region
operator|!=
literal|null
condition|)
block|{
name|region
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
name|hlog
operator|.
name|closeAndDelete
argument_list|()
expr_stmt|;
block|}
block|}
specifier|private
name|HTableDescriptor
name|createTableDescriptor
parameter_list|(
specifier|final
name|String
name|name
parameter_list|)
block|{
name|HTableDescriptor
name|htd
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|name
argument_list|)
decl_stmt|;
name|htd
operator|.
name|addFamily
argument_list|(
operator|new
name|HColumnDescriptor
argument_list|(
name|COLFAMILY_NAME1
argument_list|)
argument_list|)
expr_stmt|;
name|htd
operator|.
name|addFamily
argument_list|(
operator|new
name|HColumnDescriptor
argument_list|(
name|COLFAMILY_NAME2
argument_list|)
argument_list|)
expr_stmt|;
name|htd
operator|.
name|addFamily
argument_list|(
operator|new
name|HColumnDescriptor
argument_list|(
name|COLFAMILY_NAME3
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|htd
return|;
block|}
specifier|private
name|void
name|basicSplit
parameter_list|(
specifier|final
name|HRegion
name|region
parameter_list|)
throws|throws
name|Exception
block|{
name|addContent
argument_list|(
name|region
argument_list|,
name|COLFAMILY_NAME3
argument_list|)
expr_stmt|;
name|region
operator|.
name|internalFlushcache
argument_list|()
expr_stmt|;
name|Text
name|midkey
init|=
operator|new
name|Text
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
name|region
operator|.
name|needsSplit
argument_list|(
name|midkey
argument_list|)
argument_list|)
expr_stmt|;
name|HRegion
index|[]
name|regions
init|=
name|split
argument_list|(
name|region
argument_list|)
decl_stmt|;
comment|// Assert can get rows out of new regions.  Should be able to get first
comment|// row from first region and the midkey from second region.
name|byte
index|[]
name|b
init|=
operator|new
name|byte
index|[]
block|{
name|FIRST_CHAR
block|,
name|FIRST_CHAR
block|,
name|FIRST_CHAR
block|}
decl_stmt|;
name|assertGet
argument_list|(
name|regions
index|[
literal|0
index|]
argument_list|,
name|COLFAMILY_NAME3
argument_list|,
operator|new
name|Text
argument_list|(
name|b
argument_list|)
argument_list|)
expr_stmt|;
name|assertGet
argument_list|(
name|regions
index|[
literal|1
index|]
argument_list|,
name|COLFAMILY_NAME3
argument_list|,
name|midkey
argument_list|)
expr_stmt|;
comment|// Test I can get scanner and that it starts at right place.
name|assertScan
argument_list|(
name|regions
index|[
literal|0
index|]
argument_list|,
name|COLFAMILY_NAME3
argument_list|,
operator|new
name|Text
argument_list|(
name|b
argument_list|)
argument_list|)
expr_stmt|;
name|assertScan
argument_list|(
name|regions
index|[
literal|1
index|]
argument_list|,
name|COLFAMILY_NAME3
argument_list|,
name|midkey
argument_list|)
expr_stmt|;
comment|// Now prove can't split regions that have references.
name|Text
index|[]
name|midkeys
init|=
operator|new
name|Text
index|[
name|regions
operator|.
name|length
index|]
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
name|regions
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|midkeys
index|[
name|i
index|]
operator|=
operator|new
name|Text
argument_list|()
expr_stmt|;
comment|// Even after above splits, still needs split but after splits its
comment|// unsplitable because biggest store file is reference.  References
comment|// make the store unsplittable, until something bigger comes along.
name|assertFalse
argument_list|(
name|regions
index|[
name|i
index|]
operator|.
name|needsSplit
argument_list|(
name|midkeys
index|[
name|i
index|]
argument_list|)
argument_list|)
expr_stmt|;
comment|// Add so much data to this region, we create a store file that is> than
comment|// one of our unsplitable references.
comment|// it will.
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
literal|2
condition|;
name|j
operator|++
control|)
block|{
name|addContent
argument_list|(
name|regions
index|[
name|i
index|]
argument_list|,
name|COLFAMILY_NAME3
argument_list|)
expr_stmt|;
block|}
name|addContent
argument_list|(
name|regions
index|[
name|i
index|]
argument_list|,
name|COLFAMILY_NAME2
argument_list|)
expr_stmt|;
name|addContent
argument_list|(
name|regions
index|[
name|i
index|]
argument_list|,
name|COLFAMILY_NAME1
argument_list|)
expr_stmt|;
name|regions
index|[
name|i
index|]
operator|.
name|internalFlushcache
argument_list|()
expr_stmt|;
block|}
comment|// Assert that even if one store file is larger than a reference, the
comment|// region is still deemed unsplitable (Can't split region if references
comment|// presen).
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|regions
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|midkeys
index|[
name|i
index|]
operator|=
operator|new
name|Text
argument_list|()
expr_stmt|;
comment|// Even after above splits, still needs split but after splits its
comment|// unsplitable because biggest store file is reference.  References
comment|// make the store unsplittable, until something bigger comes along.
name|assertFalse
argument_list|(
name|regions
index|[
name|i
index|]
operator|.
name|needsSplit
argument_list|(
name|midkeys
index|[
name|i
index|]
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// To make regions splitable force compaction.
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|regions
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|assertTrue
argument_list|(
name|regions
index|[
name|i
index|]
operator|.
name|compactStores
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|TreeMap
argument_list|<
name|String
argument_list|,
name|HRegion
argument_list|>
name|sortedMap
init|=
operator|new
name|TreeMap
argument_list|<
name|String
argument_list|,
name|HRegion
argument_list|>
argument_list|()
decl_stmt|;
comment|// Split these two daughter regions so then I'll have 4 regions.  Will
comment|// split because added data above.
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|regions
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|HRegion
index|[]
name|rs
init|=
name|split
argument_list|(
name|regions
index|[
name|i
index|]
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
name|rs
operator|.
name|length
condition|;
name|j
operator|++
control|)
block|{
name|sortedMap
operator|.
name|put
argument_list|(
name|rs
index|[
name|j
index|]
operator|.
name|getRegionName
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|,
name|rs
index|[
name|j
index|]
argument_list|)
expr_stmt|;
block|}
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"Made 4 regions"
argument_list|)
expr_stmt|;
comment|// The splits should have been even.  Test I can get some arbitrary row out
comment|// of each.
name|int
name|interval
init|=
operator|(
name|LAST_CHAR
operator|-
name|FIRST_CHAR
operator|)
operator|/
literal|3
decl_stmt|;
for|for
control|(
name|HRegion
name|r
range|:
name|sortedMap
operator|.
name|values
argument_list|()
control|)
block|{
name|assertGet
argument_list|(
name|r
argument_list|,
name|COLFAMILY_NAME3
argument_list|,
operator|new
name|Text
argument_list|(
operator|new
name|String
argument_list|(
name|b
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|b
index|[
literal|0
index|]
operator|+=
name|interval
expr_stmt|;
block|}
block|}
comment|/**    * Test that a region is cleaned up after its daughter splits release all    * references.    * @throws Exception    */
specifier|public
name|void
name|testSplitRegionIsDeleted
parameter_list|()
throws|throws
name|Exception
block|{
specifier|final
name|int
name|retries
init|=
literal|10
decl_stmt|;
name|this
operator|.
name|testDir
operator|=
literal|null
expr_stmt|;
name|this
operator|.
name|fs
operator|=
literal|null
expr_stmt|;
comment|// Start up a hbase cluster
name|MiniHBaseCluster
name|cluster
init|=
operator|new
name|MiniHBaseCluster
argument_list|(
name|conf
argument_list|,
literal|1
argument_list|)
decl_stmt|;
name|Path
name|testDir
init|=
name|cluster
operator|.
name|regionThreads
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getRegionServer
argument_list|()
operator|.
name|rootDir
decl_stmt|;
name|FileSystem
name|fs
init|=
name|cluster
operator|.
name|getDFSCluster
argument_list|()
operator|.
name|getFileSystem
argument_list|()
decl_stmt|;
name|HTable
name|meta
init|=
literal|null
decl_stmt|;
name|HTable
name|t
init|=
literal|null
decl_stmt|;
try|try
block|{
comment|// Create a table.
name|HBaseAdmin
name|admin
init|=
operator|new
name|HBaseAdmin
argument_list|(
name|this
operator|.
name|conf
argument_list|)
decl_stmt|;
name|admin
operator|.
name|createTable
argument_list|(
name|createTableDescriptor
argument_list|(
name|getName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
comment|// Get connection on the meta table and get count of rows.
name|meta
operator|=
operator|new
name|HTable
argument_list|(
name|this
operator|.
name|conf
argument_list|,
name|HConstants
operator|.
name|META_TABLE_NAME
argument_list|)
expr_stmt|;
name|int
name|count
init|=
name|count
argument_list|(
name|meta
argument_list|,
name|HConstants
operator|.
name|COLUMN_FAMILY_STR
argument_list|)
decl_stmt|;
name|t
operator|=
operator|new
name|HTable
argument_list|(
name|this
operator|.
name|conf
argument_list|,
operator|new
name|Text
argument_list|(
name|getName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|addContent
argument_list|(
name|t
argument_list|,
name|COLFAMILY_NAME3
argument_list|)
expr_stmt|;
comment|// All is running in the one JVM so I should be able to get the
comment|// region instance and bring on a split.
name|HRegionInfo
name|hri
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
name|HRegion
name|r
init|=
name|cluster
operator|.
name|regionThreads
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
decl_stmt|;
comment|// Flush will provoke a split next time the split-checker thread runs.
name|r
operator|.
name|flushcache
argument_list|(
literal|false
argument_list|)
expr_stmt|;
comment|// Now, wait until split makes it into the meta table.
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
operator|&&
operator|(
name|count
argument_list|(
name|meta
argument_list|,
name|HConstants
operator|.
name|COLUMN_FAMILY_STR
argument_list|)
operator|<=
name|count
operator|)
condition|;
name|i
operator|++
control|)
block|{
name|Thread
operator|.
name|sleep
argument_list|(
literal|5000
argument_list|)
expr_stmt|;
block|}
name|int
name|oldCount
init|=
name|count
decl_stmt|;
name|count
operator|=
name|count
argument_list|(
name|meta
argument_list|,
name|HConstants
operator|.
name|COLUMN_FAMILY_STR
argument_list|)
expr_stmt|;
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
name|HRegionInfo
name|parent
init|=
name|getSplitParent
argument_list|(
name|meta
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|parent
operator|.
name|isOffline
argument_list|()
argument_list|)
expr_stmt|;
name|Path
name|parentDir
init|=
name|HRegion
operator|.
name|getRegionDir
argument_list|(
name|testDir
argument_list|,
name|parent
operator|.
name|getRegionName
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
literal|"Split happened and parent "
operator|+
name|parent
operator|.
name|getRegionName
argument_list|()
operator|+
literal|" is "
operator|+
literal|"offline"
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
comment|// Now open a scanner on the table. This will force HTable to recalibrate
comment|// and in doing so, will force us to wait until the new child regions
comment|// come on-line (since they are no longer automatically served by the
comment|// HRegionServer that was serving the parent. In this test they will
comment|// end up on the same server (since there is only one), but we have to
comment|// wait until the master assigns them.
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
operator|new
name|Text
argument_list|(
name|COLFAMILY_NAME3
argument_list|)
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
name|Thread
operator|.
name|sleep
argument_list|(
literal|5000
argument_list|)
expr_stmt|;
block|}
block|}
comment|// Now, force a compaction.  This will rewrite references and make it
comment|// so the parent region becomes deletable.
name|LOG
operator|.
name|info
argument_list|(
literal|"Starting compaction"
argument_list|)
expr_stmt|;
for|for
control|(
name|MiniHBaseCluster
operator|.
name|RegionServerThread
name|thread
range|:
name|cluster
operator|.
name|regionThreads
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
name|startsWith
argument_list|(
name|getName
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
operator|&&
name|getSplitParent
argument_list|(
name|meta
argument_list|)
operator|!=
literal|null
condition|;
name|i
operator|++
control|)
block|{
name|Thread
operator|.
name|sleep
argument_list|(
literal|5000
argument_list|)
expr_stmt|;
block|}
name|assertTrue
argument_list|(
name|getSplitParent
argument_list|(
name|meta
argument_list|)
operator|==
literal|null
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
operator|&&
name|fs
operator|.
name|exists
argument_list|(
name|parentDir
argument_list|)
condition|;
name|i
operator|++
control|)
block|{
name|Thread
operator|.
name|sleep
argument_list|(
literal|5000
argument_list|)
expr_stmt|;
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
finally|finally
block|{
name|cluster
operator|.
name|shutdown
argument_list|()
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|assertGet
parameter_list|(
specifier|final
name|HRegion
name|r
parameter_list|,
specifier|final
name|String
name|family
parameter_list|,
specifier|final
name|Text
name|k
parameter_list|)
throws|throws
name|IOException
block|{
comment|// Now I have k, get values out and assert they are as expected.
name|byte
index|[]
index|[]
name|results
init|=
name|r
operator|.
name|get
argument_list|(
name|k
argument_list|,
operator|new
name|Text
argument_list|(
name|family
argument_list|)
argument_list|,
name|Integer
operator|.
name|MAX_VALUE
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
name|results
operator|.
name|length
condition|;
name|j
operator|++
control|)
block|{
name|Text
name|tmp
init|=
operator|new
name|Text
argument_list|(
name|results
index|[
name|j
index|]
argument_list|)
decl_stmt|;
comment|// Row should be equal to value every time.
name|assertEquals
argument_list|(
name|k
operator|.
name|toString
argument_list|()
argument_list|,
name|tmp
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|HRegionInfo
name|getSplitParent
parameter_list|(
specifier|final
name|HTable
name|t
parameter_list|)
throws|throws
name|IOException
block|{
name|HRegionInfo
name|result
init|=
literal|null
decl_stmt|;
name|HScannerInterface
name|s
init|=
name|t
operator|.
name|obtainScanner
argument_list|(
name|HConstants
operator|.
name|COL_REGIONINFO_ARRAY
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
name|byte
index|[]
name|bytes
init|=
name|curVals
operator|.
name|get
argument_list|(
name|HConstants
operator|.
name|COL_REGIONINFO
argument_list|)
decl_stmt|;
if|if
condition|(
name|bytes
operator|==
literal|null
operator|||
name|bytes
operator|.
name|length
operator|==
literal|0
condition|)
block|{
continue|continue;
block|}
name|HRegionInfo
name|hri
init|=
operator|(
name|HRegionInfo
operator|)
name|Writables
operator|.
name|getWritable
argument_list|(
name|bytes
argument_list|,
operator|new
name|HRegionInfo
argument_list|()
argument_list|)
decl_stmt|;
comment|// Assert that if region is a split region, that it is also offline.
comment|// Otherwise, if not a split region, assert that it is online.
if|if
condition|(
name|hri
operator|.
name|isSplit
argument_list|()
operator|&&
name|hri
operator|.
name|isOffline
argument_list|()
condition|)
block|{
name|result
operator|=
name|hri
expr_stmt|;
break|break;
block|}
block|}
return|return
name|result
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
comment|/*    * Count of rows in table for given column.     * @param t    * @param column    * @return    * @throws IOException    */
specifier|private
name|int
name|count
parameter_list|(
specifier|final
name|HTable
name|t
parameter_list|,
specifier|final
name|String
name|column
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
operator|new
name|Text
argument_list|(
name|column
argument_list|)
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
name|size
operator|++
expr_stmt|;
block|}
return|return
name|size
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
comment|/*    * Assert first value in the passed region is<code>firstValue</code>.    * @param r    * @param column    * @param firstValue    * @throws IOException    */
specifier|private
name|void
name|assertScan
parameter_list|(
specifier|final
name|HRegion
name|r
parameter_list|,
specifier|final
name|String
name|column
parameter_list|,
specifier|final
name|Text
name|firstValue
parameter_list|)
throws|throws
name|IOException
block|{
name|Text
index|[]
name|cols
init|=
operator|new
name|Text
index|[]
block|{
operator|new
name|Text
argument_list|(
name|column
argument_list|)
block|}
decl_stmt|;
name|HInternalScannerInterface
name|s
init|=
name|r
operator|.
name|getScanner
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
name|boolean
name|first
init|=
literal|true
decl_stmt|;
name|OUTER_LOOP
label|:
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
for|for
control|(
name|Text
name|col
range|:
name|curVals
operator|.
name|keySet
argument_list|()
control|)
block|{
name|byte
index|[]
name|val
init|=
name|curVals
operator|.
name|get
argument_list|(
name|col
argument_list|)
decl_stmt|;
name|Text
name|curval
init|=
operator|new
name|Text
argument_list|(
name|val
argument_list|)
decl_stmt|;
if|if
condition|(
name|first
condition|)
block|{
name|first
operator|=
literal|false
expr_stmt|;
name|assertTrue
argument_list|(
name|curval
operator|.
name|compareTo
argument_list|(
name|firstValue
argument_list|)
operator|==
literal|0
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// Not asserting anything.  Might as well break.
break|break
name|OUTER_LOOP
break|;
block|}
block|}
block|}
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
specifier|private
name|HRegion
index|[]
name|split
parameter_list|(
specifier|final
name|HRegion
name|r
parameter_list|)
throws|throws
name|IOException
block|{
name|Text
name|midKey
init|=
operator|new
name|Text
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
name|r
operator|.
name|needsSplit
argument_list|(
name|midKey
argument_list|)
argument_list|)
expr_stmt|;
comment|// Assert can get mid key from passed region.
name|assertGet
argument_list|(
name|r
argument_list|,
name|COLFAMILY_NAME3
argument_list|,
name|midKey
argument_list|)
expr_stmt|;
name|HRegion
index|[]
name|regions
init|=
name|r
operator|.
name|closeAndSplit
argument_list|(
name|midKey
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|regions
operator|.
name|length
argument_list|,
literal|2
argument_list|)
expr_stmt|;
return|return
name|regions
return|;
block|}
specifier|private
name|void
name|addContent
parameter_list|(
specifier|final
name|HRegion
name|r
parameter_list|,
specifier|final
name|String
name|column
parameter_list|)
throws|throws
name|IOException
block|{
name|Text
name|startKey
init|=
name|r
operator|.
name|getRegionInfo
argument_list|()
operator|.
name|getStartKey
argument_list|()
decl_stmt|;
name|Text
name|endKey
init|=
name|r
operator|.
name|getRegionInfo
argument_list|()
operator|.
name|getEndKey
argument_list|()
decl_stmt|;
name|byte
index|[]
name|startKeyBytes
init|=
name|startKey
operator|.
name|getBytes
argument_list|()
decl_stmt|;
if|if
condition|(
name|startKeyBytes
operator|==
literal|null
operator|||
name|startKeyBytes
operator|.
name|length
operator|==
literal|0
condition|)
block|{
name|startKeyBytes
operator|=
operator|new
name|byte
index|[]
block|{
name|FIRST_CHAR
block|,
name|FIRST_CHAR
block|,
name|FIRST_CHAR
block|}
expr_stmt|;
block|}
comment|// Add rows of three characters.  The first character starts with the
comment|// 'a' character and runs up to 'z'.  Per first character, we run the
comment|// second character over same range.  And same for the third so rows
comment|// (and values) look like this: 'aaa', 'aab', 'aac', etc.
name|char
name|secondCharStart
init|=
operator|(
name|char
operator|)
name|startKeyBytes
index|[
literal|1
index|]
decl_stmt|;
name|char
name|thirdCharStart
init|=
operator|(
name|char
operator|)
name|startKeyBytes
index|[
literal|2
index|]
decl_stmt|;
name|EXIT_ALL_LOOPS
label|:
for|for
control|(
name|char
name|c
init|=
operator|(
name|char
operator|)
name|startKeyBytes
index|[
literal|0
index|]
init|;
name|c
operator|<=
name|LAST_CHAR
condition|;
name|c
operator|++
control|)
block|{
for|for
control|(
name|char
name|d
init|=
name|secondCharStart
init|;
name|d
operator|<=
name|LAST_CHAR
condition|;
name|d
operator|++
control|)
block|{
for|for
control|(
name|char
name|e
init|=
name|thirdCharStart
init|;
name|e
operator|<=
name|LAST_CHAR
condition|;
name|e
operator|++
control|)
block|{
name|byte
index|[]
name|bytes
init|=
operator|new
name|byte
index|[]
block|{
operator|(
name|byte
operator|)
name|c
block|,
operator|(
name|byte
operator|)
name|d
block|,
operator|(
name|byte
operator|)
name|e
block|}
decl_stmt|;
name|Text
name|t
init|=
operator|new
name|Text
argument_list|(
operator|new
name|String
argument_list|(
name|bytes
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|endKey
operator|!=
literal|null
operator|&&
name|endKey
operator|.
name|getLength
argument_list|()
operator|>
literal|0
operator|&&
name|endKey
operator|.
name|compareTo
argument_list|(
name|t
argument_list|)
operator|<=
literal|0
condition|)
block|{
break|break
name|EXIT_ALL_LOOPS
break|;
block|}
name|long
name|lockid
init|=
name|r
operator|.
name|startUpdate
argument_list|(
name|t
argument_list|)
decl_stmt|;
try|try
block|{
name|r
operator|.
name|put
argument_list|(
name|lockid
argument_list|,
operator|new
name|Text
argument_list|(
name|column
argument_list|)
argument_list|,
name|bytes
argument_list|)
expr_stmt|;
name|r
operator|.
name|commit
argument_list|(
name|lockid
argument_list|,
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|)
expr_stmt|;
name|lockid
operator|=
operator|-
literal|1
expr_stmt|;
block|}
finally|finally
block|{
if|if
condition|(
name|lockid
operator|!=
operator|-
literal|1
condition|)
block|{
name|r
operator|.
name|abort
argument_list|(
name|lockid
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|// Set start character back to FIRST_CHAR after we've done first loop.
name|thirdCharStart
operator|=
name|FIRST_CHAR
expr_stmt|;
block|}
name|secondCharStart
operator|=
name|FIRST_CHAR
expr_stmt|;
block|}
block|}
comment|// TODO: Have HTable and HRegion implement interface that has in it
comment|// startUpdate, put, delete, commit, abort, etc.
specifier|private
name|void
name|addContent
parameter_list|(
specifier|final
name|HTable
name|table
parameter_list|,
specifier|final
name|String
name|column
parameter_list|)
throws|throws
name|IOException
block|{
name|byte
index|[]
name|startKeyBytes
init|=
operator|new
name|byte
index|[]
block|{
name|FIRST_CHAR
block|,
name|FIRST_CHAR
block|,
name|FIRST_CHAR
block|}
decl_stmt|;
comment|// Add rows of three characters.  The first character starts with the
comment|// 'a' character and runs up to 'z'.  Per first character, we run the
comment|// second character over same range.  And same for the third so rows
comment|// (and values) look like this: 'aaa', 'aab', 'aac', etc.
name|char
name|secondCharStart
init|=
operator|(
name|char
operator|)
name|startKeyBytes
index|[
literal|1
index|]
decl_stmt|;
name|char
name|thirdCharStart
init|=
operator|(
name|char
operator|)
name|startKeyBytes
index|[
literal|2
index|]
decl_stmt|;
for|for
control|(
name|char
name|c
init|=
operator|(
name|char
operator|)
name|startKeyBytes
index|[
literal|0
index|]
init|;
name|c
operator|<=
name|LAST_CHAR
condition|;
name|c
operator|++
control|)
block|{
for|for
control|(
name|char
name|d
init|=
name|secondCharStart
init|;
name|d
operator|<=
name|LAST_CHAR
condition|;
name|d
operator|++
control|)
block|{
for|for
control|(
name|char
name|e
init|=
name|thirdCharStart
init|;
name|e
operator|<=
name|LAST_CHAR
condition|;
name|e
operator|++
control|)
block|{
name|byte
index|[]
name|bytes
init|=
operator|new
name|byte
index|[]
block|{
operator|(
name|byte
operator|)
name|c
block|,
operator|(
name|byte
operator|)
name|d
block|,
operator|(
name|byte
operator|)
name|e
block|}
decl_stmt|;
name|Text
name|t
init|=
operator|new
name|Text
argument_list|(
operator|new
name|String
argument_list|(
name|bytes
argument_list|)
argument_list|)
decl_stmt|;
name|long
name|lockid
init|=
name|table
operator|.
name|startBatchUpdate
argument_list|(
name|t
argument_list|)
decl_stmt|;
try|try
block|{
name|table
operator|.
name|put
argument_list|(
name|lockid
argument_list|,
operator|new
name|Text
argument_list|(
name|column
argument_list|)
argument_list|,
name|bytes
argument_list|)
expr_stmt|;
name|table
operator|.
name|commit
argument_list|(
name|lockid
argument_list|,
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|)
expr_stmt|;
name|lockid
operator|=
operator|-
literal|1
expr_stmt|;
block|}
finally|finally
block|{
if|if
condition|(
name|lockid
operator|!=
operator|-
literal|1
condition|)
block|{
name|table
operator|.
name|abort
argument_list|(
name|lockid
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|// Set start character back to FIRST_CHAR after we've done first loop.
name|thirdCharStart
operator|=
name|FIRST_CHAR
expr_stmt|;
block|}
name|secondCharStart
operator|=
name|FIRST_CHAR
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

