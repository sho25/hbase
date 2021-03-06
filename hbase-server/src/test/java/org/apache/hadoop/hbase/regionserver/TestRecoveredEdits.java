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
name|assertTrue
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
name|Collections
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
name|CellComparatorImpl
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
name|HBaseClassTestRule
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
name|MemoryCompactionPolicy
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
name|PrivateCellUtil
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
name|ColumnFamilyDescriptorBuilder
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
name|RegionInfo
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
name|client
operator|.
name|TableDescriptorBuilder
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
name|hfile
operator|.
name|BlockCache
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
name|hfile
operator|.
name|BlockCacheFactory
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
name|FSUtils
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
name|wal
operator|.
name|WAL
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
name|wal
operator|.
name|WALEdit
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
name|wal
operator|.
name|WALFactory
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
name|wal
operator|.
name|WALKey
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
name|wal
operator|.
name|WALSplitUtil
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|BeforeClass
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|ClassRule
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Rule
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
name|rules
operator|.
name|TestName
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|LoggerFactory
import|;
end_import

begin_comment
comment|/**  * Tests around replay of recovered.edits content.  */
end_comment

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|MediumTests
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|TestRecoveredEdits
block|{
annotation|@
name|ClassRule
specifier|public
specifier|static
specifier|final
name|HBaseClassTestRule
name|CLASS_RULE
init|=
name|HBaseClassTestRule
operator|.
name|forClass
argument_list|(
name|TestRecoveredEdits
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
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|TestRecoveredEdits
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|BlockCache
name|blockCache
decl_stmt|;
annotation|@
name|Rule
specifier|public
name|TestName
name|testName
init|=
operator|new
name|TestName
argument_list|()
decl_stmt|;
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|setUpBeforeClass
parameter_list|()
throws|throws
name|Exception
block|{
name|blockCache
operator|=
name|BlockCacheFactory
operator|.
name|createBlockCache
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**    * HBASE-12782 ITBLL fails for me if generator does anything but 5M per maptask.    * Create a region. Close it. Then copy into place a file to replay, one that is bigger than    * configured flush size so we bring on lots of flushes.  Then reopen and confirm all edits    * made it in.    * @throws IOException    */
annotation|@
name|Test
specifier|public
name|void
name|testReplayWorksThoughLotsOfFlushing
parameter_list|()
throws|throws
name|IOException
block|{
for|for
control|(
name|MemoryCompactionPolicy
name|policy
range|:
name|MemoryCompactionPolicy
operator|.
name|values
argument_list|()
control|)
block|{
name|testReplayWorksWithMemoryCompactionPolicy
argument_list|(
name|policy
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|testReplayWorksWithMemoryCompactionPolicy
parameter_list|(
name|MemoryCompactionPolicy
name|policy
parameter_list|)
throws|throws
name|IOException
block|{
name|Configuration
name|conf
init|=
operator|new
name|Configuration
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
comment|// Set it so we flush every 1M or so.  Thats a lot.
name|conf
operator|.
name|setInt
argument_list|(
name|HConstants
operator|.
name|HREGION_MEMSTORE_FLUSH_SIZE
argument_list|,
literal|1024
operator|*
literal|1024
argument_list|)
expr_stmt|;
name|conf
operator|.
name|set
argument_list|(
name|CompactingMemStore
operator|.
name|COMPACTING_MEMSTORE_TYPE_KEY
argument_list|,
name|String
operator|.
name|valueOf
argument_list|(
name|policy
argument_list|)
operator|.
name|toLowerCase
argument_list|()
argument_list|)
expr_stmt|;
comment|// The file of recovered edits has a column family of 'meta'. Also has an encoded regionname
comment|// of 4823016d8fca70b25503ee07f4c6d79f which needs to match on replay.
specifier|final
name|String
name|encodedRegionName
init|=
literal|"4823016d8fca70b25503ee07f4c6d79f"
decl_stmt|;
specifier|final
name|String
name|columnFamily
init|=
literal|"meta"
decl_stmt|;
name|byte
index|[]
index|[]
name|columnFamilyAsByteArray
init|=
operator|new
name|byte
index|[]
index|[]
block|{
name|Bytes
operator|.
name|toBytes
argument_list|(
name|columnFamily
argument_list|)
block|}
decl_stmt|;
name|TableDescriptor
name|tableDescriptor
init|=
name|TableDescriptorBuilder
operator|.
name|newBuilder
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
name|testName
operator|.
name|getMethodName
argument_list|()
argument_list|)
argument_list|)
operator|.
name|setColumnFamily
argument_list|(
name|ColumnFamilyDescriptorBuilder
operator|.
name|newBuilder
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|columnFamily
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|RegionInfo
name|hri
init|=
operator|new
name|HRegionInfo
argument_list|(
name|tableDescriptor
operator|.
name|getTableName
argument_list|()
argument_list|)
block|{
annotation|@
name|Override
specifier|public
specifier|synchronized
name|String
name|getEncodedName
parameter_list|()
block|{
return|return
name|encodedRegionName
return|;
block|}
comment|// Cache the name because lots of lookups.
specifier|private
name|byte
index|[]
name|encodedRegionNameAsBytes
init|=
literal|null
decl_stmt|;
annotation|@
name|Override
specifier|public
specifier|synchronized
name|byte
index|[]
name|getEncodedNameAsBytes
parameter_list|()
block|{
if|if
condition|(
name|encodedRegionNameAsBytes
operator|==
literal|null
condition|)
block|{
name|this
operator|.
name|encodedRegionNameAsBytes
operator|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|getEncodedName
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|this
operator|.
name|encodedRegionNameAsBytes
return|;
block|}
block|}
decl_stmt|;
name|Path
name|hbaseRootDir
init|=
name|TEST_UTIL
operator|.
name|getDataTestDir
argument_list|()
decl_stmt|;
name|FileSystem
name|fs
init|=
name|FileSystem
operator|.
name|get
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
name|Path
name|tableDir
init|=
name|FSUtils
operator|.
name|getTableDir
argument_list|(
name|hbaseRootDir
argument_list|,
name|tableDescriptor
operator|.
name|getTableName
argument_list|()
argument_list|)
decl_stmt|;
name|HRegionFileSystem
name|hrfs
init|=
operator|new
name|HRegionFileSystem
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|fs
argument_list|,
name|tableDir
argument_list|,
name|hri
argument_list|)
decl_stmt|;
if|if
condition|(
name|fs
operator|.
name|exists
argument_list|(
name|hrfs
operator|.
name|getRegionDir
argument_list|()
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Region directory already exists. Deleting."
argument_list|)
expr_stmt|;
name|fs
operator|.
name|delete
argument_list|(
name|hrfs
operator|.
name|getRegionDir
argument_list|()
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
name|HRegion
name|region
init|=
name|HBaseTestingUtility
operator|.
name|createRegionAndWAL
argument_list|(
name|hri
argument_list|,
name|hbaseRootDir
argument_list|,
name|conf
argument_list|,
name|tableDescriptor
argument_list|,
name|blockCache
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|encodedRegionName
argument_list|,
name|region
operator|.
name|getRegionInfo
argument_list|()
operator|.
name|getEncodedName
argument_list|()
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|storeFiles
init|=
name|region
operator|.
name|getStoreFileList
argument_list|(
name|columnFamilyAsByteArray
argument_list|)
decl_stmt|;
comment|// There should be no store files.
name|assertTrue
argument_list|(
name|storeFiles
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
name|region
operator|.
name|close
argument_list|()
expr_stmt|;
name|Path
name|regionDir
init|=
name|FSUtils
operator|.
name|getRegionDirFromRootDir
argument_list|(
name|hbaseRootDir
argument_list|,
name|hri
argument_list|)
decl_stmt|;
name|Path
name|recoveredEditsDir
init|=
name|WALSplitUtil
operator|.
name|getRegionDirRecoveredEditsDir
argument_list|(
name|regionDir
argument_list|)
decl_stmt|;
comment|// This is a little fragile getting this path to a file of 10M of edits.
name|Path
name|recoveredEditsFile
init|=
operator|new
name|Path
argument_list|(
name|System
operator|.
name|getProperty
argument_list|(
literal|"test.build.classes"
argument_list|,
literal|"target/test-classes"
argument_list|)
argument_list|,
literal|"0000000000000016310"
argument_list|)
decl_stmt|;
comment|// Copy this file under the region's recovered.edits dir so it is replayed on reopen.
name|Path
name|destination
init|=
operator|new
name|Path
argument_list|(
name|recoveredEditsDir
argument_list|,
name|recoveredEditsFile
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
name|fs
operator|.
name|copyToLocalFile
argument_list|(
name|recoveredEditsFile
argument_list|,
name|destination
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|fs
operator|.
name|exists
argument_list|(
name|destination
argument_list|)
argument_list|)
expr_stmt|;
comment|// Now the file 0000000000000016310 is under recovered.edits, reopen the region to replay.
name|region
operator|=
name|HRegion
operator|.
name|openHRegion
argument_list|(
name|region
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|encodedRegionName
argument_list|,
name|region
operator|.
name|getRegionInfo
argument_list|()
operator|.
name|getEncodedName
argument_list|()
argument_list|)
expr_stmt|;
name|storeFiles
operator|=
name|region
operator|.
name|getStoreFileList
argument_list|(
name|columnFamilyAsByteArray
argument_list|)
expr_stmt|;
comment|// Our 0000000000000016310 is 10MB. Most of the edits are for one region. Lets assume that if
comment|// we flush at 1MB, that there are at least 3 flushed files that are there because of the
comment|// replay of edits.
if|if
condition|(
name|policy
operator|==
name|MemoryCompactionPolicy
operator|.
name|EAGER
operator|||
name|policy
operator|==
name|MemoryCompactionPolicy
operator|.
name|ADAPTIVE
condition|)
block|{
name|assertTrue
argument_list|(
literal|"Files count="
operator|+
name|storeFiles
operator|.
name|size
argument_list|()
argument_list|,
name|storeFiles
operator|.
name|size
argument_list|()
operator|>=
literal|1
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|assertTrue
argument_list|(
literal|"Files count="
operator|+
name|storeFiles
operator|.
name|size
argument_list|()
argument_list|,
name|storeFiles
operator|.
name|size
argument_list|()
operator|>
literal|10
argument_list|)
expr_stmt|;
block|}
comment|// Now verify all edits made it into the region.
name|int
name|count
init|=
name|verifyAllEditsMadeItIn
argument_list|(
name|fs
argument_list|,
name|conf
argument_list|,
name|recoveredEditsFile
argument_list|,
name|region
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Checked "
operator|+
name|count
operator|+
literal|" edits made it in"
argument_list|)
expr_stmt|;
block|}
comment|/**    * @param fs    * @param conf    * @param edits    * @param region    * @return Return how many edits seen.    * @throws IOException    */
specifier|private
name|int
name|verifyAllEditsMadeItIn
parameter_list|(
specifier|final
name|FileSystem
name|fs
parameter_list|,
specifier|final
name|Configuration
name|conf
parameter_list|,
specifier|final
name|Path
name|edits
parameter_list|,
specifier|final
name|HRegion
name|region
parameter_list|)
throws|throws
name|IOException
block|{
name|int
name|count
init|=
literal|0
decl_stmt|;
comment|// Read all cells from recover edits
name|List
argument_list|<
name|Cell
argument_list|>
name|walCells
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
try|try
init|(
name|WAL
operator|.
name|Reader
name|reader
init|=
name|WALFactory
operator|.
name|createReader
argument_list|(
name|fs
argument_list|,
name|edits
argument_list|,
name|conf
argument_list|)
init|)
block|{
name|WAL
operator|.
name|Entry
name|entry
decl_stmt|;
while|while
condition|(
operator|(
name|entry
operator|=
name|reader
operator|.
name|next
argument_list|()
operator|)
operator|!=
literal|null
condition|)
block|{
name|WALKey
name|key
init|=
name|entry
operator|.
name|getKey
argument_list|()
decl_stmt|;
name|WALEdit
name|val
init|=
name|entry
operator|.
name|getEdit
argument_list|()
decl_stmt|;
name|count
operator|++
expr_stmt|;
comment|// Check this edit is for this region.
if|if
condition|(
operator|!
name|Bytes
operator|.
name|equals
argument_list|(
name|key
operator|.
name|getEncodedRegionName
argument_list|()
argument_list|,
name|region
operator|.
name|getRegionInfo
argument_list|()
operator|.
name|getEncodedNameAsBytes
argument_list|()
argument_list|)
condition|)
block|{
continue|continue;
block|}
name|Cell
name|previous
init|=
literal|null
decl_stmt|;
for|for
control|(
name|Cell
name|cell
range|:
name|val
operator|.
name|getCells
argument_list|()
control|)
block|{
if|if
condition|(
name|WALEdit
operator|.
name|isMetaEditFamily
argument_list|(
name|cell
argument_list|)
condition|)
block|{
continue|continue;
block|}
if|if
condition|(
name|previous
operator|!=
literal|null
operator|&&
name|CellComparatorImpl
operator|.
name|COMPARATOR
operator|.
name|compareRows
argument_list|(
name|previous
argument_list|,
name|cell
argument_list|)
operator|==
literal|0
condition|)
block|{
continue|continue;
block|}
name|previous
operator|=
name|cell
expr_stmt|;
name|walCells
operator|.
name|add
argument_list|(
name|cell
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|// Read all cells from region
name|List
argument_list|<
name|Cell
argument_list|>
name|regionCells
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
try|try
init|(
name|RegionScanner
name|scanner
init|=
name|region
operator|.
name|getScanner
argument_list|(
operator|new
name|Scan
argument_list|()
argument_list|)
init|)
block|{
name|List
argument_list|<
name|Cell
argument_list|>
name|tmpCells
decl_stmt|;
do|do
block|{
name|tmpCells
operator|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
expr_stmt|;
name|scanner
operator|.
name|nextRaw
argument_list|(
name|tmpCells
argument_list|)
expr_stmt|;
name|regionCells
operator|.
name|addAll
argument_list|(
name|tmpCells
argument_list|)
expr_stmt|;
block|}
do|while
condition|(
operator|!
name|tmpCells
operator|.
name|isEmpty
argument_list|()
condition|)
do|;
block|}
name|Collections
operator|.
name|sort
argument_list|(
name|walCells
argument_list|,
name|CellComparatorImpl
operator|.
name|COMPARATOR
argument_list|)
expr_stmt|;
name|int
name|found
init|=
literal|0
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|,
name|j
init|=
literal|0
init|;
name|i
operator|<
name|walCells
operator|.
name|size
argument_list|()
operator|&&
name|j
operator|<
name|regionCells
operator|.
name|size
argument_list|()
condition|;
control|)
block|{
name|int
name|compareResult
init|=
name|PrivateCellUtil
operator|.
name|compareKeyIgnoresMvcc
argument_list|(
name|CellComparatorImpl
operator|.
name|COMPARATOR
argument_list|,
name|walCells
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|,
name|regionCells
operator|.
name|get
argument_list|(
name|j
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|compareResult
operator|==
literal|0
condition|)
block|{
name|i
operator|++
expr_stmt|;
name|j
operator|++
expr_stmt|;
name|found
operator|++
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|compareResult
operator|>
literal|0
condition|)
block|{
name|j
operator|++
expr_stmt|;
block|}
else|else
block|{
name|i
operator|++
expr_stmt|;
block|}
block|}
name|assertEquals
argument_list|(
literal|"Only found "
operator|+
name|found
operator|+
literal|" cells in region, but there are "
operator|+
name|walCells
operator|.
name|size
argument_list|()
operator|+
literal|" cells in recover edits"
argument_list|,
name|found
argument_list|,
name|walCells
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|count
return|;
block|}
block|}
end_class

end_unit

