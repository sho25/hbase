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
name|assertNotNull
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
name|Matchers
operator|.
name|any
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|mockito
operator|.
name|Matchers
operator|.
name|eq
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
name|doThrow
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
import|import static
name|org
operator|.
name|mockito
operator|.
name|Mockito
operator|.
name|spy
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
name|when
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|ImmutableList
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
name|FSDataOutputStream
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
name|backup
operator|.
name|FailedArchiveException
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
name|WALFactory
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

begin_comment
comment|/**  * Tests that archiving compacted files behaves correctly when encountering exceptions.  */
end_comment

begin_class
annotation|@
name|Category
argument_list|(
name|MediumTests
operator|.
name|class
argument_list|)
specifier|public
class|class
name|TestCompactionArchiveIOException
block|{
specifier|private
specifier|static
specifier|final
name|String
name|ERROR_FILE
init|=
literal|"fffffffffffffffffdeadbeef"
decl_stmt|;
specifier|public
name|HBaseTestingUtility
name|testUtil
decl_stmt|;
specifier|private
name|Path
name|testDir
decl_stmt|;
annotation|@
name|Rule
specifier|public
name|TestName
name|name
init|=
operator|new
name|TestName
argument_list|()
decl_stmt|;
annotation|@
name|Before
specifier|public
name|void
name|setup
parameter_list|()
throws|throws
name|Exception
block|{
name|testUtil
operator|=
operator|new
name|HBaseTestingUtility
argument_list|()
expr_stmt|;
name|testUtil
operator|.
name|startMiniDFSCluster
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|testDir
operator|=
name|testUtil
operator|.
name|getDataTestDirOnTestFS
argument_list|()
expr_stmt|;
name|FSUtils
operator|.
name|setRootDir
argument_list|(
name|testUtil
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|testDir
argument_list|)
expr_stmt|;
block|}
annotation|@
name|After
specifier|public
name|void
name|tearDown
parameter_list|()
throws|throws
name|Exception
block|{
name|testUtil
operator|.
name|cleanupTestDir
argument_list|()
expr_stmt|;
name|testUtil
operator|.
name|shutdownMiniDFSCluster
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testRemoveCompactedFilesWithException
parameter_list|()
throws|throws
name|Exception
block|{
name|byte
index|[]
name|fam
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"f"
argument_list|)
decl_stmt|;
name|byte
index|[]
name|col
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"c"
argument_list|)
decl_stmt|;
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
specifier|final
name|TableName
name|tableName
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
name|name
operator|.
name|getMethodName
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
specifier|final
name|HRegion
name|region
init|=
name|initHRegion
argument_list|(
name|htd
argument_list|,
name|info
argument_list|)
decl_stmt|;
name|RegionServerServices
name|rss
init|=
name|mock
argument_list|(
name|RegionServerServices
operator|.
name|class
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Region
argument_list|>
name|regions
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|regions
operator|.
name|add
argument_list|(
name|region
argument_list|)
expr_stmt|;
name|when
argument_list|(
name|rss
operator|.
name|getOnlineRegions
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|regions
argument_list|)
expr_stmt|;
comment|// Create the cleaner object
specifier|final
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
name|int
name|batchSize
init|=
literal|10
decl_stmt|;
name|int
name|fileCount
init|=
literal|10
decl_stmt|;
for|for
control|(
name|int
name|f
init|=
literal|0
init|;
name|f
operator|<
name|fileCount
condition|;
name|f
operator|++
control|)
block|{
name|int
name|start
init|=
name|f
operator|*
name|batchSize
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
name|start
init|;
name|i
operator|<
name|start
operator|+
name|batchSize
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
name|col
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
block|}
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
name|fam
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|fileCount
argument_list|,
name|store
operator|.
name|getStorefilesCount
argument_list|()
argument_list|)
expr_stmt|;
name|Collection
argument_list|<
name|StoreFile
argument_list|>
name|storefiles
init|=
name|store
operator|.
name|getStorefiles
argument_list|()
decl_stmt|;
comment|// None of the files should be in compacted state.
for|for
control|(
name|StoreFile
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
name|StoreFileManager
name|fileManager
init|=
name|store
operator|.
name|getStoreEngine
argument_list|()
operator|.
name|getStoreFileManager
argument_list|()
decl_stmt|;
name|Collection
argument_list|<
name|StoreFile
argument_list|>
name|initialCompactedFiles
init|=
name|fileManager
operator|.
name|getCompactedfiles
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
name|initialCompactedFiles
operator|==
literal|null
operator|||
name|initialCompactedFiles
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
comment|// Do compaction
name|region
operator|.
name|compact
argument_list|(
literal|true
argument_list|)
expr_stmt|;
comment|// all prior store files should now be compacted
name|Collection
argument_list|<
name|StoreFile
argument_list|>
name|compactedFilesPreClean
init|=
name|fileManager
operator|.
name|getCompactedfiles
argument_list|()
decl_stmt|;
name|assertNotNull
argument_list|(
name|compactedFilesPreClean
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|compactedFilesPreClean
operator|.
name|size
argument_list|()
operator|>
literal|0
argument_list|)
expr_stmt|;
comment|// add the dummy file to the store directory
name|HRegionFileSystem
name|regionFS
init|=
name|region
operator|.
name|getRegionFileSystem
argument_list|()
decl_stmt|;
name|Path
name|errFile
init|=
name|regionFS
operator|.
name|getStoreFilePath
argument_list|(
name|Bytes
operator|.
name|toString
argument_list|(
name|fam
argument_list|)
argument_list|,
name|ERROR_FILE
argument_list|)
decl_stmt|;
name|FSDataOutputStream
name|out
init|=
name|regionFS
operator|.
name|getFileSystem
argument_list|()
operator|.
name|create
argument_list|(
name|errFile
argument_list|)
decl_stmt|;
name|out
operator|.
name|writeInt
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|out
operator|.
name|close
argument_list|()
expr_stmt|;
name|StoreFile
name|errStoreFile
init|=
operator|new
name|MockStoreFile
argument_list|(
name|testUtil
argument_list|,
name|errFile
argument_list|,
literal|1
argument_list|,
literal|0
argument_list|,
literal|false
argument_list|,
literal|1
argument_list|)
decl_stmt|;
name|fileManager
operator|.
name|addCompactionResults
argument_list|(
name|ImmutableList
operator|.
name|of
argument_list|(
name|errStoreFile
argument_list|)
argument_list|,
name|ImmutableList
operator|.
expr|<
name|StoreFile
operator|>
name|of
argument_list|()
argument_list|)
expr_stmt|;
comment|// cleanup compacted files
name|cleaner
operator|.
name|chore
argument_list|()
expr_stmt|;
comment|// make sure the compacted files are cleared
name|Collection
argument_list|<
name|StoreFile
argument_list|>
name|compactedFilesPostClean
init|=
name|fileManager
operator|.
name|getCompactedfiles
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|compactedFilesPostClean
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|StoreFile
name|origFile
range|:
name|compactedFilesPreClean
control|)
block|{
name|assertFalse
argument_list|(
name|compactedFilesPostClean
operator|.
name|contains
argument_list|(
name|origFile
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// close the region
try|try
block|{
name|region
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|FailedArchiveException
name|e
parameter_list|)
block|{
comment|// expected due to errorfile
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|e
operator|.
name|getFailedFiles
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|ERROR_FILE
argument_list|,
name|e
operator|.
name|getFailedFiles
argument_list|()
operator|.
name|iterator
argument_list|()
operator|.
name|next
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|HRegion
name|initHRegion
parameter_list|(
name|HTableDescriptor
name|htd
parameter_list|,
name|HRegionInfo
name|info
parameter_list|)
throws|throws
name|IOException
block|{
name|Configuration
name|conf
init|=
name|testUtil
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
name|ChunkCreator
operator|.
name|initialize
argument_list|(
name|MemStoreLABImpl
operator|.
name|CHUNK_SIZE_DEFAULT
argument_list|,
literal|false
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|Path
name|tableDir
init|=
name|FSUtils
operator|.
name|getTableDir
argument_list|(
name|testDir
argument_list|,
name|htd
operator|.
name|getTableName
argument_list|()
argument_list|)
decl_stmt|;
name|Path
name|regionDir
init|=
operator|new
name|Path
argument_list|(
name|tableDir
argument_list|,
name|info
operator|.
name|getEncodedName
argument_list|()
argument_list|)
decl_stmt|;
name|Path
name|storeDir
init|=
operator|new
name|Path
argument_list|(
name|regionDir
argument_list|,
name|htd
operator|.
name|getColumnFamilies
argument_list|()
index|[
literal|0
index|]
operator|.
name|getNameAsString
argument_list|()
argument_list|)
decl_stmt|;
name|FileSystem
name|errFS
init|=
name|spy
argument_list|(
name|testUtil
operator|.
name|getTestFileSystem
argument_list|()
argument_list|)
decl_stmt|;
comment|// Prior to HBASE-16964, when an exception is thrown archiving any compacted file,
comment|// none of the other files are cleared from the compactedfiles list.
comment|// Simulate this condition with a dummy file
name|doThrow
argument_list|(
operator|new
name|IOException
argument_list|(
literal|"Error for test"
argument_list|)
argument_list|)
operator|.
name|when
argument_list|(
name|errFS
argument_list|)
operator|.
name|rename
argument_list|(
name|eq
argument_list|(
operator|new
name|Path
argument_list|(
name|storeDir
argument_list|,
name|ERROR_FILE
argument_list|)
argument_list|)
argument_list|,
name|any
argument_list|(
name|Path
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|HRegionFileSystem
name|fs
init|=
operator|new
name|HRegionFileSystem
argument_list|(
name|conf
argument_list|,
name|errFS
argument_list|,
name|tableDir
argument_list|,
name|info
argument_list|)
decl_stmt|;
specifier|final
name|Configuration
name|walConf
init|=
operator|new
name|Configuration
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|FSUtils
operator|.
name|setRootDir
argument_list|(
name|walConf
argument_list|,
name|tableDir
argument_list|)
expr_stmt|;
specifier|final
name|WALFactory
name|wals
init|=
operator|new
name|WALFactory
argument_list|(
name|walConf
argument_list|,
literal|null
argument_list|,
literal|"log_"
operator|+
name|info
operator|.
name|getEncodedName
argument_list|()
argument_list|)
decl_stmt|;
name|HRegion
name|region
init|=
operator|new
name|HRegion
argument_list|(
name|fs
argument_list|,
name|wals
operator|.
name|getWAL
argument_list|(
name|info
operator|.
name|getEncodedNameAsBytes
argument_list|()
argument_list|,
name|info
operator|.
name|getTable
argument_list|()
operator|.
name|getNamespace
argument_list|()
argument_list|)
argument_list|,
name|conf
argument_list|,
name|htd
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|region
operator|.
name|initialize
argument_list|()
expr_stmt|;
return|return
name|region
return|;
block|}
block|}
end_class

end_unit

