begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertFalse
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertTrue
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|mockito
operator|.
name|Mockito
operator|.
name|mock
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
name|concurrent
operator|.
name|CountDownLatch
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
name|atomic
operator|.
name|AtomicInteger
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
name|CompactedHFilesDischarger
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
name|HRegion
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
name|HStoreFile
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
name|RegionScanner
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
name|RegionServerServices
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
name|testclassification
operator|.
name|RegionServerTests
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
name|junit
operator|.
name|After
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Before
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
name|mockito
operator|.
name|Mockito
import|;
end_import

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|MediumTests
operator|.
name|class
block|,
name|RegionServerTests
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|TestCompactedHFilesDischarger
block|{
specifier|private
specifier|final
name|HBaseTestingUtility
name|testUtil
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
specifier|private
name|HRegion
name|region
decl_stmt|;
specifier|private
specifier|final
specifier|static
name|byte
index|[]
name|fam
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"cf_1"
argument_list|)
decl_stmt|;
specifier|private
specifier|final
specifier|static
name|byte
index|[]
name|qual1
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"qf_1"
argument_list|)
decl_stmt|;
specifier|private
specifier|final
specifier|static
name|byte
index|[]
name|val
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"val"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|CountDownLatch
name|latch
init|=
operator|new
name|CountDownLatch
argument_list|(
literal|3
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|AtomicInteger
name|counter
init|=
operator|new
name|AtomicInteger
argument_list|(
literal|0
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|AtomicInteger
name|scanCompletedCounter
init|=
operator|new
name|AtomicInteger
argument_list|(
literal|0
argument_list|)
decl_stmt|;
specifier|private
name|RegionServerServices
name|rss
decl_stmt|;
annotation|@
name|Before
specifier|public
name|void
name|setUp
parameter_list|()
throws|throws
name|Exception
block|{
name|TableName
name|tableName
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
name|getClass
argument_list|()
operator|.
name|getSimpleName
argument_list|()
argument_list|)
decl_stmt|;
name|HTableDescriptor
name|htd
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
name|htd
operator|.
name|addFamily
argument_list|(
operator|new
name|HColumnDescriptor
argument_list|(
name|fam
argument_list|)
argument_list|)
expr_stmt|;
name|HRegionInfo
name|info
init|=
operator|new
name|HRegionInfo
argument_list|(
name|tableName
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|Path
name|path
init|=
name|testUtil
operator|.
name|getDataTestDir
argument_list|(
name|getClass
argument_list|()
operator|.
name|getSimpleName
argument_list|()
argument_list|)
decl_stmt|;
name|region
operator|=
name|HBaseTestingUtility
operator|.
name|createRegionAndWAL
argument_list|(
name|info
argument_list|,
name|path
argument_list|,
name|testUtil
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|htd
argument_list|)
expr_stmt|;
name|rss
operator|=
name|mock
argument_list|(
name|RegionServerServices
operator|.
name|class
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|HRegion
argument_list|>
name|regions
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
literal|1
argument_list|)
decl_stmt|;
name|regions
operator|.
name|add
argument_list|(
name|region
argument_list|)
expr_stmt|;
name|Mockito
operator|.
name|doReturn
argument_list|(
name|regions
argument_list|)
operator|.
name|when
argument_list|(
name|rss
argument_list|)
operator|.
name|getRegions
argument_list|()
expr_stmt|;
block|}
annotation|@
name|After
specifier|public
name|void
name|tearDown
parameter_list|()
throws|throws
name|IOException
block|{
name|counter
operator|.
name|set
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|scanCompletedCounter
operator|.
name|set
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|latch
operator|=
operator|new
name|CountDownLatch
argument_list|(
literal|3
argument_list|)
expr_stmt|;
name|HBaseTestingUtility
operator|.
name|closeRegionAndWAL
argument_list|(
name|region
argument_list|)
expr_stmt|;
name|testUtil
operator|.
name|cleanupTestDir
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testCompactedHFilesCleaner
parameter_list|()
throws|throws
name|Exception
block|{
comment|// Create the cleaner object
name|CompactedHFilesDischarger
name|cleaner
init|=
operator|new
name|CompactedHFilesDischarger
argument_list|(
literal|1000
argument_list|,
operator|(
name|Stoppable
operator|)
literal|null
argument_list|,
name|rss
argument_list|,
literal|false
argument_list|)
decl_stmt|;
comment|// Add some data to the region and do some flushes
for|for
control|(
name|int
name|i
init|=
literal|1
init|;
name|i
operator|<
literal|10
condition|;
name|i
operator|++
control|)
block|{
name|Put
name|p
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
name|i
argument_list|)
argument_list|)
decl_stmt|;
name|p
operator|.
name|addColumn
argument_list|(
name|fam
argument_list|,
name|qual1
argument_list|,
name|val
argument_list|)
expr_stmt|;
name|region
operator|.
name|put
argument_list|(
name|p
argument_list|)
expr_stmt|;
block|}
comment|// flush them
name|region
operator|.
name|flush
argument_list|(
literal|true
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|11
init|;
name|i
operator|<
literal|20
condition|;
name|i
operator|++
control|)
block|{
name|Put
name|p
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
name|i
argument_list|)
argument_list|)
decl_stmt|;
name|p
operator|.
name|addColumn
argument_list|(
name|fam
argument_list|,
name|qual1
argument_list|,
name|val
argument_list|)
expr_stmt|;
name|region
operator|.
name|put
argument_list|(
name|p
argument_list|)
expr_stmt|;
block|}
comment|// flush them
name|region
operator|.
name|flush
argument_list|(
literal|true
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|21
init|;
name|i
operator|<
literal|30
condition|;
name|i
operator|++
control|)
block|{
name|Put
name|p
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
name|i
argument_list|)
argument_list|)
decl_stmt|;
name|p
operator|.
name|addColumn
argument_list|(
name|fam
argument_list|,
name|qual1
argument_list|,
name|val
argument_list|)
expr_stmt|;
name|region
operator|.
name|put
argument_list|(
name|p
argument_list|)
expr_stmt|;
block|}
comment|// flush them
name|region
operator|.
name|flush
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|HStore
name|store
init|=
name|region
operator|.
name|getStore
argument_list|(
name|fam
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|3
argument_list|,
name|store
operator|.
name|getStorefilesCount
argument_list|()
argument_list|)
expr_stmt|;
name|Collection
argument_list|<
name|HStoreFile
argument_list|>
name|storefiles
init|=
name|store
operator|.
name|getStorefiles
argument_list|()
decl_stmt|;
name|Collection
argument_list|<
name|HStoreFile
argument_list|>
name|compactedfiles
init|=
name|store
operator|.
name|getStoreEngine
argument_list|()
operator|.
name|getStoreFileManager
argument_list|()
operator|.
name|getCompactedfiles
argument_list|()
decl_stmt|;
comment|// None of the files should be in compacted state.
for|for
control|(
name|HStoreFile
name|file
range|:
name|storefiles
control|)
block|{
name|assertFalse
argument_list|(
name|file
operator|.
name|isCompactedAway
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|// Try to run the cleaner without compaction. there should not be any change
name|cleaner
operator|.
name|chore
argument_list|()
expr_stmt|;
name|storefiles
operator|=
name|store
operator|.
name|getStorefiles
argument_list|()
expr_stmt|;
comment|// None of the files should be in compacted state.
for|for
control|(
name|HStoreFile
name|file
range|:
name|storefiles
control|)
block|{
name|assertFalse
argument_list|(
name|file
operator|.
name|isCompactedAway
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|// now do some compaction
name|region
operator|.
name|compact
argument_list|(
literal|true
argument_list|)
expr_stmt|;
comment|// Still the flushed files should be present until the cleaner runs. But the state of it should
comment|// be in COMPACTED state
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|store
operator|.
name|getStorefilesCount
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|3
argument_list|,
operator|(
operator|(
name|HStore
operator|)
name|store
operator|)
operator|.
name|getStoreEngine
argument_list|()
operator|.
name|getStoreFileManager
argument_list|()
operator|.
name|getCompactedfiles
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
comment|// Run the cleaner
name|cleaner
operator|.
name|chore
argument_list|()
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|store
operator|.
name|getStorefilesCount
argument_list|()
argument_list|)
expr_stmt|;
name|storefiles
operator|=
name|store
operator|.
name|getStorefiles
argument_list|()
expr_stmt|;
for|for
control|(
name|HStoreFile
name|file
range|:
name|storefiles
control|)
block|{
comment|// Should not be in compacted state
name|assertFalse
argument_list|(
name|file
operator|.
name|isCompactedAway
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|compactedfiles
operator|=
operator|(
operator|(
name|HStore
operator|)
name|store
operator|)
operator|.
name|getStoreEngine
argument_list|()
operator|.
name|getStoreFileManager
argument_list|()
operator|.
name|getCompactedfiles
argument_list|()
expr_stmt|;
name|assertTrue
argument_list|(
name|compactedfiles
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testCleanerWithParallelScannersAfterCompaction
parameter_list|()
throws|throws
name|Exception
block|{
comment|// Create the cleaner object
name|CompactedHFilesDischarger
name|cleaner
init|=
operator|new
name|CompactedHFilesDischarger
argument_list|(
literal|1000
argument_list|,
operator|(
name|Stoppable
operator|)
literal|null
argument_list|,
name|rss
argument_list|,
literal|false
argument_list|)
decl_stmt|;
comment|// Add some data to the region and do some flushes
for|for
control|(
name|int
name|i
init|=
literal|1
init|;
name|i
operator|<
literal|10
condition|;
name|i
operator|++
control|)
block|{
name|Put
name|p
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
name|i
argument_list|)
argument_list|)
decl_stmt|;
name|p
operator|.
name|addColumn
argument_list|(
name|fam
argument_list|,
name|qual1
argument_list|,
name|val
argument_list|)
expr_stmt|;
name|region
operator|.
name|put
argument_list|(
name|p
argument_list|)
expr_stmt|;
block|}
comment|// flush them
name|region
operator|.
name|flush
argument_list|(
literal|true
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|11
init|;
name|i
operator|<
literal|20
condition|;
name|i
operator|++
control|)
block|{
name|Put
name|p
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
name|i
argument_list|)
argument_list|)
decl_stmt|;
name|p
operator|.
name|addColumn
argument_list|(
name|fam
argument_list|,
name|qual1
argument_list|,
name|val
argument_list|)
expr_stmt|;
name|region
operator|.
name|put
argument_list|(
name|p
argument_list|)
expr_stmt|;
block|}
comment|// flush them
name|region
operator|.
name|flush
argument_list|(
literal|true
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|21
init|;
name|i
operator|<
literal|30
condition|;
name|i
operator|++
control|)
block|{
name|Put
name|p
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
name|i
argument_list|)
argument_list|)
decl_stmt|;
name|p
operator|.
name|addColumn
argument_list|(
name|fam
argument_list|,
name|qual1
argument_list|,
name|val
argument_list|)
expr_stmt|;
name|region
operator|.
name|put
argument_list|(
name|p
argument_list|)
expr_stmt|;
block|}
comment|// flush them
name|region
operator|.
name|flush
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|HStore
name|store
init|=
name|region
operator|.
name|getStore
argument_list|(
name|fam
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|3
argument_list|,
name|store
operator|.
name|getStorefilesCount
argument_list|()
argument_list|)
expr_stmt|;
name|Collection
argument_list|<
name|HStoreFile
argument_list|>
name|storefiles
init|=
name|store
operator|.
name|getStorefiles
argument_list|()
decl_stmt|;
name|Collection
argument_list|<
name|HStoreFile
argument_list|>
name|compactedfiles
init|=
name|store
operator|.
name|getStoreEngine
argument_list|()
operator|.
name|getStoreFileManager
argument_list|()
operator|.
name|getCompactedfiles
argument_list|()
decl_stmt|;
comment|// None of the files should be in compacted state.
for|for
control|(
name|HStoreFile
name|file
range|:
name|storefiles
control|)
block|{
name|assertFalse
argument_list|(
name|file
operator|.
name|isCompactedAway
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|// Do compaction
name|region
operator|.
name|compact
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|startScannerThreads
argument_list|()
expr_stmt|;
name|storefiles
operator|=
name|store
operator|.
name|getStorefiles
argument_list|()
expr_stmt|;
name|int
name|usedReaderCount
init|=
literal|0
decl_stmt|;
name|int
name|unusedReaderCount
init|=
literal|0
decl_stmt|;
for|for
control|(
name|HStoreFile
name|file
range|:
name|storefiles
control|)
block|{
if|if
condition|(
operator|(
operator|(
name|HStoreFile
operator|)
name|file
operator|)
operator|.
name|getRefCount
argument_list|()
operator|==
literal|3
condition|)
block|{
name|usedReaderCount
operator|++
expr_stmt|;
block|}
block|}
name|compactedfiles
operator|=
operator|(
operator|(
name|HStore
operator|)
name|store
operator|)
operator|.
name|getStoreEngine
argument_list|()
operator|.
name|getStoreFileManager
argument_list|()
operator|.
name|getCompactedfiles
argument_list|()
expr_stmt|;
for|for
control|(
name|HStoreFile
name|file
range|:
name|compactedfiles
control|)
block|{
name|assertEquals
argument_list|(
literal|"Refcount should be 3"
argument_list|,
literal|0
argument_list|,
operator|(
operator|(
name|HStoreFile
operator|)
name|file
operator|)
operator|.
name|getRefCount
argument_list|()
argument_list|)
expr_stmt|;
name|unusedReaderCount
operator|++
expr_stmt|;
block|}
comment|// Though there are files we are not using them for reads
name|assertEquals
argument_list|(
literal|"unused reader count should be 3"
argument_list|,
literal|3
argument_list|,
name|unusedReaderCount
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"used reader count should be 1"
argument_list|,
literal|1
argument_list|,
name|usedReaderCount
argument_list|)
expr_stmt|;
comment|// now run the cleaner
name|cleaner
operator|.
name|chore
argument_list|()
expr_stmt|;
name|countDown
argument_list|()
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|store
operator|.
name|getStorefilesCount
argument_list|()
argument_list|)
expr_stmt|;
name|storefiles
operator|=
name|store
operator|.
name|getStorefiles
argument_list|()
expr_stmt|;
for|for
control|(
name|HStoreFile
name|file
range|:
name|storefiles
control|)
block|{
comment|// Should not be in compacted state
name|assertFalse
argument_list|(
name|file
operator|.
name|isCompactedAway
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|compactedfiles
operator|=
operator|(
operator|(
name|HStore
operator|)
name|store
operator|)
operator|.
name|getStoreEngine
argument_list|()
operator|.
name|getStoreFileManager
argument_list|()
operator|.
name|getCompactedfiles
argument_list|()
expr_stmt|;
name|assertTrue
argument_list|(
name|compactedfiles
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testCleanerWithParallelScanners
parameter_list|()
throws|throws
name|Exception
block|{
comment|// Create the cleaner object
name|CompactedHFilesDischarger
name|cleaner
init|=
operator|new
name|CompactedHFilesDischarger
argument_list|(
literal|1000
argument_list|,
operator|(
name|Stoppable
operator|)
literal|null
argument_list|,
name|rss
argument_list|,
literal|false
argument_list|)
decl_stmt|;
comment|// Add some data to the region and do some flushes
for|for
control|(
name|int
name|i
init|=
literal|1
init|;
name|i
operator|<
literal|10
condition|;
name|i
operator|++
control|)
block|{
name|Put
name|p
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
name|i
argument_list|)
argument_list|)
decl_stmt|;
name|p
operator|.
name|addColumn
argument_list|(
name|fam
argument_list|,
name|qual1
argument_list|,
name|val
argument_list|)
expr_stmt|;
name|region
operator|.
name|put
argument_list|(
name|p
argument_list|)
expr_stmt|;
block|}
comment|// flush them
name|region
operator|.
name|flush
argument_list|(
literal|true
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|11
init|;
name|i
operator|<
literal|20
condition|;
name|i
operator|++
control|)
block|{
name|Put
name|p
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
name|i
argument_list|)
argument_list|)
decl_stmt|;
name|p
operator|.
name|addColumn
argument_list|(
name|fam
argument_list|,
name|qual1
argument_list|,
name|val
argument_list|)
expr_stmt|;
name|region
operator|.
name|put
argument_list|(
name|p
argument_list|)
expr_stmt|;
block|}
comment|// flush them
name|region
operator|.
name|flush
argument_list|(
literal|true
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|21
init|;
name|i
operator|<
literal|30
condition|;
name|i
operator|++
control|)
block|{
name|Put
name|p
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
name|i
argument_list|)
argument_list|)
decl_stmt|;
name|p
operator|.
name|addColumn
argument_list|(
name|fam
argument_list|,
name|qual1
argument_list|,
name|val
argument_list|)
expr_stmt|;
name|region
operator|.
name|put
argument_list|(
name|p
argument_list|)
expr_stmt|;
block|}
comment|// flush them
name|region
operator|.
name|flush
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|HStore
name|store
init|=
name|region
operator|.
name|getStore
argument_list|(
name|fam
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|3
argument_list|,
name|store
operator|.
name|getStorefilesCount
argument_list|()
argument_list|)
expr_stmt|;
name|Collection
argument_list|<
name|HStoreFile
argument_list|>
name|storefiles
init|=
name|store
operator|.
name|getStorefiles
argument_list|()
decl_stmt|;
name|Collection
argument_list|<
name|HStoreFile
argument_list|>
name|compactedfiles
init|=
name|store
operator|.
name|getStoreEngine
argument_list|()
operator|.
name|getStoreFileManager
argument_list|()
operator|.
name|getCompactedfiles
argument_list|()
decl_stmt|;
comment|// None of the files should be in compacted state.
for|for
control|(
name|HStoreFile
name|file
range|:
name|storefiles
control|)
block|{
name|assertFalse
argument_list|(
name|file
operator|.
name|isCompactedAway
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|startScannerThreads
argument_list|()
expr_stmt|;
comment|// Do compaction
name|region
operator|.
name|compact
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|storefiles
operator|=
name|store
operator|.
name|getStorefiles
argument_list|()
expr_stmt|;
name|int
name|usedReaderCount
init|=
literal|0
decl_stmt|;
name|int
name|unusedReaderCount
init|=
literal|0
decl_stmt|;
for|for
control|(
name|HStoreFile
name|file
range|:
name|storefiles
control|)
block|{
if|if
condition|(
name|file
operator|.
name|getRefCount
argument_list|()
operator|==
literal|0
condition|)
block|{
name|unusedReaderCount
operator|++
expr_stmt|;
block|}
block|}
name|compactedfiles
operator|=
name|store
operator|.
name|getStoreEngine
argument_list|()
operator|.
name|getStoreFileManager
argument_list|()
operator|.
name|getCompactedfiles
argument_list|()
expr_stmt|;
for|for
control|(
name|HStoreFile
name|file
range|:
name|compactedfiles
control|)
block|{
name|assertEquals
argument_list|(
literal|"Refcount should be 3"
argument_list|,
literal|3
argument_list|,
operator|(
operator|(
name|HStoreFile
operator|)
name|file
operator|)
operator|.
name|getRefCount
argument_list|()
argument_list|)
expr_stmt|;
name|usedReaderCount
operator|++
expr_stmt|;
block|}
comment|// The newly compacted file will not be used by any scanner
name|assertEquals
argument_list|(
literal|"unused reader count should be 1"
argument_list|,
literal|1
argument_list|,
name|unusedReaderCount
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"used reader count should be 3"
argument_list|,
literal|3
argument_list|,
name|usedReaderCount
argument_list|)
expr_stmt|;
comment|// now run the cleaner
name|cleaner
operator|.
name|chore
argument_list|()
expr_stmt|;
name|countDown
argument_list|()
expr_stmt|;
comment|// No change in the number of store files as none of the compacted files could be cleaned up
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|store
operator|.
name|getStorefilesCount
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|3
argument_list|,
operator|(
operator|(
name|HStore
operator|)
name|store
operator|)
operator|.
name|getStoreEngine
argument_list|()
operator|.
name|getStoreFileManager
argument_list|()
operator|.
name|getCompactedfiles
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
while|while
condition|(
name|scanCompletedCounter
operator|.
name|get
argument_list|()
operator|!=
literal|3
condition|)
block|{
name|Thread
operator|.
name|sleep
argument_list|(
literal|100
argument_list|)
expr_stmt|;
block|}
comment|// reset
name|latch
operator|=
operator|new
name|CountDownLatch
argument_list|(
literal|3
argument_list|)
expr_stmt|;
name|scanCompletedCounter
operator|.
name|set
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|counter
operator|.
name|set
argument_list|(
literal|0
argument_list|)
expr_stmt|;
comment|// Try creating a new scanner and it should use only the new file created after compaction
name|startScannerThreads
argument_list|()
expr_stmt|;
name|storefiles
operator|=
name|store
operator|.
name|getStorefiles
argument_list|()
expr_stmt|;
name|usedReaderCount
operator|=
literal|0
expr_stmt|;
name|unusedReaderCount
operator|=
literal|0
expr_stmt|;
for|for
control|(
name|HStoreFile
name|file
range|:
name|storefiles
control|)
block|{
if|if
condition|(
name|file
operator|.
name|getRefCount
argument_list|()
operator|==
literal|3
condition|)
block|{
name|usedReaderCount
operator|++
expr_stmt|;
block|}
block|}
name|compactedfiles
operator|=
operator|(
operator|(
name|HStore
operator|)
name|store
operator|)
operator|.
name|getStoreEngine
argument_list|()
operator|.
name|getStoreFileManager
argument_list|()
operator|.
name|getCompactedfiles
argument_list|()
expr_stmt|;
for|for
control|(
name|HStoreFile
name|file
range|:
name|compactedfiles
control|)
block|{
name|assertEquals
argument_list|(
literal|"Refcount should be 0"
argument_list|,
literal|0
argument_list|,
name|file
operator|.
name|getRefCount
argument_list|()
argument_list|)
expr_stmt|;
name|unusedReaderCount
operator|++
expr_stmt|;
block|}
comment|// Though there are files we are not using them for reads
name|assertEquals
argument_list|(
literal|"unused reader count should be 3"
argument_list|,
literal|3
argument_list|,
name|unusedReaderCount
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"used reader count should be 1"
argument_list|,
literal|1
argument_list|,
name|usedReaderCount
argument_list|)
expr_stmt|;
name|countDown
argument_list|()
expr_stmt|;
while|while
condition|(
name|scanCompletedCounter
operator|.
name|get
argument_list|()
operator|!=
literal|3
condition|)
block|{
name|Thread
operator|.
name|sleep
argument_list|(
literal|100
argument_list|)
expr_stmt|;
block|}
comment|// Run the cleaner again
name|cleaner
operator|.
name|chore
argument_list|()
expr_stmt|;
comment|// Now the cleaner should be able to clear it up because there are no active readers
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|store
operator|.
name|getStorefilesCount
argument_list|()
argument_list|)
expr_stmt|;
name|storefiles
operator|=
name|store
operator|.
name|getStorefiles
argument_list|()
expr_stmt|;
for|for
control|(
name|HStoreFile
name|file
range|:
name|storefiles
control|)
block|{
comment|// Should not be in compacted state
name|assertFalse
argument_list|(
name|file
operator|.
name|isCompactedAway
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|compactedfiles
operator|=
operator|(
operator|(
name|HStore
operator|)
name|store
operator|)
operator|.
name|getStoreEngine
argument_list|()
operator|.
name|getStoreFileManager
argument_list|()
operator|.
name|getCompactedfiles
argument_list|()
expr_stmt|;
name|assertTrue
argument_list|(
name|compactedfiles
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|protected
name|void
name|countDown
parameter_list|()
block|{
comment|// count down 3 times
name|latch
operator|.
name|countDown
argument_list|()
expr_stmt|;
name|latch
operator|.
name|countDown
argument_list|()
expr_stmt|;
name|latch
operator|.
name|countDown
argument_list|()
expr_stmt|;
block|}
specifier|protected
name|void
name|startScannerThreads
parameter_list|()
throws|throws
name|InterruptedException
block|{
comment|// Start parallel scan threads
name|ScanThread
index|[]
name|scanThreads
init|=
operator|new
name|ScanThread
index|[
literal|3
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
literal|3
condition|;
name|i
operator|++
control|)
block|{
name|scanThreads
index|[
name|i
index|]
operator|=
operator|new
name|ScanThread
argument_list|(
operator|(
name|HRegion
operator|)
name|region
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|ScanThread
name|thread
range|:
name|scanThreads
control|)
block|{
name|thread
operator|.
name|start
argument_list|()
expr_stmt|;
block|}
while|while
condition|(
name|counter
operator|.
name|get
argument_list|()
operator|!=
literal|3
condition|)
block|{
name|Thread
operator|.
name|sleep
argument_list|(
literal|100
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
specifier|static
class|class
name|ScanThread
extends|extends
name|Thread
block|{
specifier|private
specifier|final
name|HRegion
name|region
decl_stmt|;
specifier|public
name|ScanThread
parameter_list|(
name|HRegion
name|region
parameter_list|)
block|{
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
name|run
parameter_list|()
block|{
try|try
block|{
name|initiateScan
argument_list|(
name|region
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
comment|// do nothing
block|}
block|}
specifier|private
name|void
name|initiateScan
parameter_list|(
name|HRegion
name|region
parameter_list|)
throws|throws
name|IOException
block|{
name|Scan
name|scan
init|=
operator|new
name|Scan
argument_list|()
decl_stmt|;
name|scan
operator|.
name|setCaching
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|RegionScanner
name|resScanner
init|=
literal|null
decl_stmt|;
try|try
block|{
name|resScanner
operator|=
name|region
operator|.
name|getScanner
argument_list|(
name|scan
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|Cell
argument_list|>
name|results
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|boolean
name|next
init|=
name|resScanner
operator|.
name|next
argument_list|(
name|results
argument_list|)
decl_stmt|;
try|try
block|{
name|counter
operator|.
name|incrementAndGet
argument_list|()
expr_stmt|;
name|latch
operator|.
name|await
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{         }
while|while
condition|(
operator|!
name|next
condition|)
block|{
name|resScanner
operator|.
name|next
argument_list|(
name|results
argument_list|)
expr_stmt|;
block|}
block|}
finally|finally
block|{
name|scanCompletedCounter
operator|.
name|incrementAndGet
argument_list|()
expr_stmt|;
name|resScanner
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit

