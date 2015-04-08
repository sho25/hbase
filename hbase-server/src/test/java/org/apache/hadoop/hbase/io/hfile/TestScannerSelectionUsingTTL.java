begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one or more  * contributor license agreements. See the NOTICE file distributed with this  * work for additional information regarding copyright ownership. The ASF  * licenses this file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the  * License for the specific language governing permissions and limitations  * under the License.  */
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
name|io
operator|.
name|hfile
package|;
end_package

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertEquals
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
name|ArrayList
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collection
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
name|java
operator|.
name|util
operator|.
name|Set
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
name|HBaseTestingUtility
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
name|client
operator|.
name|Put
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
name|Scan
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
name|HStore
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
name|InternalScanner
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
name|InternalScanner
operator|.
name|NextState
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
name|testclassification
operator|.
name|IOTests
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
name|testclassification
operator|.
name|MediumTests
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
name|EnvironmentEdgeManager
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
name|Threads
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Test
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|experimental
operator|.
name|categories
operator|.
name|Category
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|runner
operator|.
name|RunWith
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|runners
operator|.
name|Parameterized
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|runners
operator|.
name|Parameterized
operator|.
name|Parameters
import|;
end_import

begin_comment
comment|/**  * Test the optimization that does not scan files where all timestamps are  * expired.  */
end_comment

begin_class
annotation|@
name|RunWith
argument_list|(
name|Parameterized
operator|.
name|class
argument_list|)
annotation|@
name|Category
argument_list|(
block|{
name|IOTests
operator|.
name|class
block|,
name|MediumTests
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|TestScannerSelectionUsingTTL
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
name|TestScannerSelectionUsingTTL
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|HBaseTestingUtility
name|TEST_UTIL
init|=
name|HBaseTestingUtility
operator|.
name|createLocalHTU
argument_list|()
decl_stmt|;
specifier|private
specifier|static
name|TableName
name|TABLE
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"myTable"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|String
name|FAMILY
init|=
literal|"myCF"
decl_stmt|;
specifier|private
specifier|static
name|byte
index|[]
name|FAMILY_BYTES
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|FAMILY
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|TTL_SECONDS
init|=
literal|10
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|TTL_MS
init|=
name|TTL_SECONDS
operator|*
literal|1000
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|NUM_EXPIRED_FILES
init|=
literal|2
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|NUM_ROWS
init|=
literal|8
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|NUM_COLS_PER_ROW
init|=
literal|5
decl_stmt|;
specifier|public
specifier|final
name|int
name|numFreshFiles
decl_stmt|,
name|totalNumFiles
decl_stmt|;
comment|/** Whether we are specifying the exact files to compact */
specifier|private
specifier|final
name|boolean
name|explicitCompaction
decl_stmt|;
annotation|@
name|Parameters
specifier|public
specifier|static
name|Collection
argument_list|<
name|Object
index|[]
argument_list|>
name|parameters
parameter_list|()
block|{
name|List
argument_list|<
name|Object
index|[]
argument_list|>
name|params
init|=
operator|new
name|ArrayList
argument_list|<
name|Object
index|[]
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|numFreshFiles
init|=
literal|1
init|;
name|numFreshFiles
operator|<=
literal|3
condition|;
operator|++
name|numFreshFiles
control|)
block|{
for|for
control|(
name|boolean
name|explicitCompaction
range|:
operator|new
name|boolean
index|[]
block|{
literal|false
block|,
literal|true
block|}
control|)
block|{
name|params
operator|.
name|add
argument_list|(
operator|new
name|Object
index|[]
block|{
name|numFreshFiles
block|,
name|explicitCompaction
block|}
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|params
return|;
block|}
specifier|public
name|TestScannerSelectionUsingTTL
parameter_list|(
name|int
name|numFreshFiles
parameter_list|,
name|boolean
name|explicitCompaction
parameter_list|)
block|{
name|this
operator|.
name|numFreshFiles
operator|=
name|numFreshFiles
expr_stmt|;
name|this
operator|.
name|totalNumFiles
operator|=
name|numFreshFiles
operator|+
name|NUM_EXPIRED_FILES
expr_stmt|;
name|this
operator|.
name|explicitCompaction
operator|=
name|explicitCompaction
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testScannerSelection
parameter_list|()
throws|throws
name|IOException
block|{
name|Configuration
name|conf
init|=
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
name|conf
operator|.
name|setBoolean
argument_list|(
literal|"hbase.store.delete.expired.storefile"
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|HColumnDescriptor
name|hcd
init|=
operator|new
name|HColumnDescriptor
argument_list|(
name|FAMILY_BYTES
argument_list|)
operator|.
name|setMaxVersions
argument_list|(
name|Integer
operator|.
name|MAX_VALUE
argument_list|)
operator|.
name|setTimeToLive
argument_list|(
name|TTL_SECONDS
argument_list|)
decl_stmt|;
name|HTableDescriptor
name|htd
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|TABLE
argument_list|)
decl_stmt|;
name|htd
operator|.
name|addFamily
argument_list|(
name|hcd
argument_list|)
expr_stmt|;
name|HRegionInfo
name|info
init|=
operator|new
name|HRegionInfo
argument_list|(
name|TABLE
argument_list|)
decl_stmt|;
name|Region
name|region
init|=
name|HBaseTestingUtility
operator|.
name|createRegionAndWAL
argument_list|(
name|info
argument_list|,
name|TEST_UTIL
operator|.
name|getDataTestDir
argument_list|(
name|info
operator|.
name|getEncodedName
argument_list|()
argument_list|)
argument_list|,
name|conf
argument_list|,
name|htd
argument_list|)
decl_stmt|;
name|long
name|ts
init|=
name|EnvironmentEdgeManager
operator|.
name|currentTime
argument_list|()
decl_stmt|;
name|long
name|version
init|=
literal|0
decl_stmt|;
comment|//make sure each new set of Put's have a new ts
for|for
control|(
name|int
name|iFile
init|=
literal|0
init|;
name|iFile
operator|<
name|totalNumFiles
condition|;
operator|++
name|iFile
control|)
block|{
if|if
condition|(
name|iFile
operator|==
name|NUM_EXPIRED_FILES
condition|)
block|{
name|Threads
operator|.
name|sleepWithoutInterrupt
argument_list|(
name|TTL_MS
argument_list|)
expr_stmt|;
name|version
operator|+=
name|TTL_MS
expr_stmt|;
block|}
for|for
control|(
name|int
name|iRow
init|=
literal|0
init|;
name|iRow
operator|<
name|NUM_ROWS
condition|;
operator|++
name|iRow
control|)
block|{
name|Put
name|put
init|=
operator|new
name|Put
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"row"
operator|+
name|iRow
argument_list|)
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|iCol
init|=
literal|0
init|;
name|iCol
operator|<
name|NUM_COLS_PER_ROW
condition|;
operator|++
name|iCol
control|)
block|{
name|put
operator|.
name|add
argument_list|(
name|FAMILY_BYTES
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"col"
operator|+
name|iCol
argument_list|)
argument_list|,
name|ts
operator|+
name|version
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"value"
operator|+
name|iFile
operator|+
literal|"_"
operator|+
name|iRow
operator|+
literal|"_"
operator|+
name|iCol
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|region
operator|.
name|put
argument_list|(
name|put
argument_list|)
expr_stmt|;
block|}
name|region
operator|.
name|flush
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|version
operator|++
expr_stmt|;
block|}
name|Scan
name|scan
init|=
operator|new
name|Scan
argument_list|()
decl_stmt|;
name|scan
operator|.
name|setMaxVersions
argument_list|(
name|Integer
operator|.
name|MAX_VALUE
argument_list|)
expr_stmt|;
name|CacheConfig
name|cacheConf
init|=
operator|new
name|CacheConfig
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|LruBlockCache
name|cache
init|=
operator|(
name|LruBlockCache
operator|)
name|cacheConf
operator|.
name|getBlockCache
argument_list|()
decl_stmt|;
name|cache
operator|.
name|clearCache
argument_list|()
expr_stmt|;
name|InternalScanner
name|scanner
init|=
name|region
operator|.
name|getScanner
argument_list|(
name|scan
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Cell
argument_list|>
name|results
init|=
operator|new
name|ArrayList
argument_list|<
name|Cell
argument_list|>
argument_list|()
decl_stmt|;
specifier|final
name|int
name|expectedKVsPerRow
init|=
name|numFreshFiles
operator|*
name|NUM_COLS_PER_ROW
decl_stmt|;
name|int
name|numReturnedRows
init|=
literal|0
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Scanning the entire table"
argument_list|)
expr_stmt|;
while|while
condition|(
name|NextState
operator|.
name|hasMoreValues
argument_list|(
name|scanner
operator|.
name|next
argument_list|(
name|results
argument_list|)
argument_list|)
operator|||
name|results
operator|.
name|size
argument_list|()
operator|>
literal|0
condition|)
block|{
name|assertEquals
argument_list|(
name|expectedKVsPerRow
argument_list|,
name|results
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
operator|++
name|numReturnedRows
expr_stmt|;
name|results
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
name|assertEquals
argument_list|(
name|NUM_ROWS
argument_list|,
name|numReturnedRows
argument_list|)
expr_stmt|;
name|Set
argument_list|<
name|String
argument_list|>
name|accessedFiles
init|=
name|cache
operator|.
name|getCachedFileNamesForTest
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Files accessed during scan: "
operator|+
name|accessedFiles
argument_list|)
expr_stmt|;
comment|// Exercise both compaction codepaths.
if|if
condition|(
name|explicitCompaction
condition|)
block|{
name|HStore
name|store
init|=
operator|(
name|HStore
operator|)
name|region
operator|.
name|getStore
argument_list|(
name|FAMILY_BYTES
argument_list|)
decl_stmt|;
name|store
operator|.
name|compactRecentForTestingAssumingDefaultPolicy
argument_list|(
name|totalNumFiles
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|region
operator|.
name|compact
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
name|HBaseTestingUtility
operator|.
name|closeRegionAndWAL
argument_list|(
name|region
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

