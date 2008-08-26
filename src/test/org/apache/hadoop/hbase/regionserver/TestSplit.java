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
name|HStoreKey
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
name|HBaseClusterTestCase
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
name|NotServingRegionException
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
name|UnknownScannerException
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
name|io
operator|.
name|Cell
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
name|Bytes
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
name|HBaseClusterTestCase
block|{
annotation|@
name|SuppressWarnings
argument_list|(
literal|"hiding"
argument_list|)
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
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
comment|/** constructor */
specifier|public
name|TestSplit
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
comment|// Always compact if there is more than one store file.
name|conf
operator|.
name|setInt
argument_list|(
literal|"hbase.hstore.compactionThreshold"
argument_list|,
literal|2
argument_list|)
expr_stmt|;
comment|// Make lease timeout longer, lease checks less frequent
name|conf
operator|.
name|setInt
argument_list|(
literal|"hbase.master.lease.period"
argument_list|,
literal|10
operator|*
literal|1000
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setInt
argument_list|(
literal|"hbase.master.lease.thread.wakefrequency"
argument_list|,
literal|5
operator|*
literal|1000
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setInt
argument_list|(
literal|"hbase.regionserver.lease.period"
argument_list|,
literal|10
operator|*
literal|1000
argument_list|)
expr_stmt|;
comment|// Increase the amount of time between client retries
name|conf
operator|.
name|setLong
argument_list|(
literal|"hbase.client.pause"
argument_list|,
literal|15
operator|*
literal|1000
argument_list|)
expr_stmt|;
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
name|region
operator|=
name|createNewHRegion
argument_list|(
name|htd
argument_list|,
literal|null
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
name|region
operator|.
name|getLog
argument_list|()
operator|.
name|closeAndDelete
argument_list|()
expr_stmt|;
block|}
block|}
block|}
comment|/**    * Test for HBASE-810    * @throws Exception    */
specifier|public
name|void
name|testScanSplitOnRegion
parameter_list|()
throws|throws
name|Exception
block|{
name|HRegion
name|region
init|=
literal|null
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
name|region
operator|=
name|createNewHRegion
argument_list|(
name|htd
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|addContent
argument_list|(
name|region
argument_list|,
name|COLFAMILY_NAME3
argument_list|)
expr_stmt|;
name|region
operator|.
name|flushcache
argument_list|()
expr_stmt|;
specifier|final
name|byte
index|[]
name|midkey
init|=
name|region
operator|.
name|compactStores
argument_list|()
decl_stmt|;
name|assertNotNull
argument_list|(
name|midkey
argument_list|)
expr_stmt|;
name|byte
index|[]
index|[]
name|cols
init|=
block|{
name|COLFAMILY_NAME3
block|}
decl_stmt|;
specifier|final
name|InternalScanner
name|s
init|=
name|region
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
specifier|final
name|HRegion
name|regionForThread
init|=
name|region
decl_stmt|;
name|Thread
name|splitThread
init|=
operator|new
name|Thread
argument_list|()
block|{
specifier|public
name|void
name|run
parameter_list|()
block|{
try|try
block|{
name|HRegion
index|[]
name|regions
init|=
name|split
argument_list|(
name|regionForThread
argument_list|,
name|midkey
argument_list|)
decl_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|fail
argument_list|(
literal|"Unexpected exception "
operator|+
name|e
argument_list|)
expr_stmt|;
block|}
block|}
block|}
decl_stmt|;
name|splitThread
operator|.
name|start
argument_list|()
expr_stmt|;
name|HRegionServer
name|server
init|=
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
decl_stmt|;
name|long
name|id
init|=
name|server
operator|.
name|addScanner
argument_list|(
name|s
argument_list|)
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
literal|6
condition|;
name|i
operator|++
control|)
block|{
try|try
block|{
name|BatchUpdate
name|update
init|=
operator|new
name|BatchUpdate
argument_list|(
name|region
operator|.
name|getRegionInfo
argument_list|()
operator|.
name|getStartKey
argument_list|()
argument_list|)
decl_stmt|;
name|update
operator|.
name|put
argument_list|(
name|COLFAMILY_NAME3
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"val"
argument_list|)
argument_list|)
expr_stmt|;
name|region
operator|.
name|batchUpdate
argument_list|(
name|update
argument_list|)
expr_stmt|;
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
name|fail
argument_list|(
literal|"Unexpected exception "
operator|+
name|e
argument_list|)
expr_stmt|;
block|}
block|}
name|server
operator|.
name|next
argument_list|(
name|id
argument_list|)
expr_stmt|;
name|server
operator|.
name|close
argument_list|(
name|id
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|UnknownScannerException
name|ex
parameter_list|)
block|{
name|ex
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
name|fail
argument_list|(
literal|"Got the "
operator|+
name|ex
argument_list|)
expr_stmt|;
block|}
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
name|flushcache
argument_list|()
expr_stmt|;
name|byte
index|[]
name|midkey
init|=
name|region
operator|.
name|compactStores
argument_list|()
decl_stmt|;
name|assertNotNull
argument_list|(
name|midkey
argument_list|)
expr_stmt|;
name|HRegion
index|[]
name|regions
init|=
name|split
argument_list|(
name|region
argument_list|,
name|midkey
argument_list|)
decl_stmt|;
try|try
block|{
comment|// Need to open the regions.
comment|// TODO: Add an 'open' to HRegion... don't do open by constructing
comment|// instance.
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
name|regions
index|[
name|i
index|]
operator|=
name|openClosedRegion
argument_list|(
name|regions
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
comment|// Assert can get rows out of new regions. Should be able to get first
comment|// row from first region and the midkey from second region.
name|assertGet
argument_list|(
name|regions
index|[
literal|0
index|]
argument_list|,
name|COLFAMILY_NAME3
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|START_KEY
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
name|Bytes
operator|.
name|toBytes
argument_list|(
name|START_KEY
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
comment|// Add so much data to this region, we create a store file that is>
comment|// than one of our unsplitable references. it will.
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
name|flushcache
argument_list|()
expr_stmt|;
block|}
name|byte
index|[]
index|[]
name|midkeys
init|=
operator|new
name|byte
index|[
name|regions
operator|.
name|length
index|]
index|[]
decl_stmt|;
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
name|midkeys
index|[
name|i
index|]
operator|=
name|regions
index|[
name|i
index|]
operator|.
name|compactStores
argument_list|()
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
comment|// Split these two daughter regions so then I'll have 4 regions. Will
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
literal|null
decl_stmt|;
if|if
condition|(
name|midkeys
index|[
name|i
index|]
operator|!=
literal|null
condition|)
block|{
name|rs
operator|=
name|split
argument_list|(
name|regions
index|[
name|i
index|]
argument_list|,
name|midkeys
index|[
name|i
index|]
argument_list|)
expr_stmt|;
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
name|Bytes
operator|.
name|toString
argument_list|(
name|rs
index|[
name|j
index|]
operator|.
name|getRegionName
argument_list|()
argument_list|)
argument_list|,
name|openClosedRegion
argument_list|(
name|rs
index|[
name|j
index|]
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"Made 4 regions"
argument_list|)
expr_stmt|;
comment|// The splits should have been even. Test I can get some arbitrary row out
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
name|byte
index|[]
name|b
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|START_KEY
argument_list|)
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
name|b
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
finally|finally
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
name|regions
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
try|try
block|{
name|regions
index|[
name|i
index|]
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
comment|// Ignore.
block|}
block|}
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
name|byte
index|[]
name|family
parameter_list|,
specifier|final
name|byte
index|[]
name|k
parameter_list|)
throws|throws
name|IOException
block|{
comment|// Now I have k, get values out and assert they are as expected.
name|Cell
index|[]
name|results
init|=
name|r
operator|.
name|get
argument_list|(
name|k
argument_list|,
name|family
argument_list|,
operator|-
literal|1
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
name|byte
index|[]
name|tmp
init|=
name|results
index|[
name|j
index|]
operator|.
name|getValue
argument_list|()
decl_stmt|;
comment|// Row should be equal to value every time.
name|assertTrue
argument_list|(
name|Bytes
operator|.
name|equals
argument_list|(
name|k
argument_list|,
name|tmp
argument_list|)
argument_list|)
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
name|byte
index|[]
name|column
parameter_list|,
specifier|final
name|byte
index|[]
name|firstValue
parameter_list|)
throws|throws
name|IOException
block|{
name|byte
index|[]
index|[]
name|cols
init|=
block|{
name|column
block|}
decl_stmt|;
name|InternalScanner
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
name|byte
index|[]
argument_list|,
name|Cell
argument_list|>
name|curVals
init|=
operator|new
name|TreeMap
argument_list|<
name|byte
index|[]
argument_list|,
name|Cell
argument_list|>
argument_list|(
name|Bytes
operator|.
name|BYTES_COMPARATOR
argument_list|)
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
name|byte
index|[]
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
operator|.
name|getValue
argument_list|()
decl_stmt|;
name|byte
index|[]
name|curval
init|=
name|val
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
name|Bytes
operator|.
name|compareTo
argument_list|(
name|curval
argument_list|,
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
parameter_list|,
specifier|final
name|byte
index|[]
name|midKey
parameter_list|)
throws|throws
name|IOException
block|{
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
name|splitRegion
argument_list|(
name|midKey
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
block|}
end_class

end_unit

