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
name|assertFalse
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
name|io
operator|.
name|InterruptedIOException
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
name|atomic
operator|.
name|AtomicBoolean
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
name|AtomicReference
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
name|RegionInfoBuilder
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

begin_import
import|import
name|org
operator|.
name|mockito
operator|.
name|Mockito
import|;
end_import

begin_comment
comment|/**  * Tests a race condition between archiving of compacted files in CompactedHFilesDischarger chore  * and HRegion.close();  */
end_comment

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|RegionServerTests
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
name|TestCompactionArchiveConcurrentClose
block|{
specifier|public
name|HBaseTestingUtility
name|testUtil
decl_stmt|;
specifier|private
name|Path
name|testDir
decl_stmt|;
specifier|private
name|AtomicBoolean
name|archived
init|=
operator|new
name|AtomicBoolean
argument_list|()
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
name|HBaseTestingUtility
operator|.
name|createLocalHTU
argument_list|()
expr_stmt|;
name|testDir
operator|=
name|testUtil
operator|.
name|getDataTestDir
argument_list|(
literal|"TestStoreFileRefresherChore"
argument_list|)
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
block|}
annotation|@
name|Test
specifier|public
name|void
name|testStoreCloseAndDischargeRunningInParallel
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
name|TableDescriptor
name|htd
init|=
name|TableDescriptorBuilder
operator|.
name|newBuilder
argument_list|(
name|tableName
argument_list|)
operator|.
name|addColumnFamily
argument_list|(
name|ColumnFamilyDescriptorBuilder
operator|.
name|of
argument_list|(
name|fam
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|RegionInfo
name|info
init|=
name|RegionInfoBuilder
operator|.
name|newBuilder
argument_list|(
name|tableName
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
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
name|HRegion
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
name|HStoreFile
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
comment|// now run the cleaner with a concurrent close
name|Thread
name|cleanerThread
init|=
operator|new
name|Thread
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
name|cleaner
operator|.
name|chore
argument_list|()
expr_stmt|;
block|}
block|}
decl_stmt|;
name|cleanerThread
operator|.
name|start
argument_list|()
expr_stmt|;
comment|// wait for cleaner to pause
synchronized|synchronized
init|(
name|archived
init|)
block|{
if|if
condition|(
operator|!
name|archived
operator|.
name|get
argument_list|()
condition|)
block|{
name|archived
operator|.
name|wait
argument_list|()
expr_stmt|;
block|}
block|}
specifier|final
name|AtomicReference
argument_list|<
name|Exception
argument_list|>
name|closeException
init|=
operator|new
name|AtomicReference
argument_list|<>
argument_list|()
decl_stmt|;
name|Thread
name|closeThread
init|=
operator|new
name|Thread
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
comment|// wait for the chore to complete and call close
try|try
block|{
operator|(
operator|(
name|HRegion
operator|)
name|region
operator|)
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
name|closeException
operator|.
name|set
argument_list|(
name|e
argument_list|)
expr_stmt|;
block|}
block|}
block|}
decl_stmt|;
name|closeThread
operator|.
name|start
argument_list|()
expr_stmt|;
comment|// no error should occur after the execution of the test
name|closeThread
operator|.
name|join
argument_list|()
expr_stmt|;
name|cleanerThread
operator|.
name|join
argument_list|()
expr_stmt|;
if|if
condition|(
name|closeException
operator|.
name|get
argument_list|()
operator|!=
literal|null
condition|)
block|{
throw|throw
name|closeException
operator|.
name|get
argument_list|()
throw|;
block|}
block|}
specifier|private
name|HRegion
name|initHRegion
parameter_list|(
name|TableDescriptor
name|htd
parameter_list|,
name|RegionInfo
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
name|HRegionFileSystem
name|fs
init|=
operator|new
name|WaitingHRegionFileSystem
argument_list|(
name|conf
argument_list|,
name|tableDir
operator|.
name|getFileSystem
argument_list|(
name|conf
argument_list|)
argument_list|,
name|tableDir
argument_list|,
name|info
argument_list|)
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
specifier|private
class|class
name|WaitingHRegionFileSystem
extends|extends
name|HRegionFileSystem
block|{
specifier|public
name|WaitingHRegionFileSystem
parameter_list|(
specifier|final
name|Configuration
name|conf
parameter_list|,
specifier|final
name|FileSystem
name|fs
parameter_list|,
specifier|final
name|Path
name|tableDir
parameter_list|,
specifier|final
name|RegionInfo
name|regionInfo
parameter_list|)
block|{
name|super
argument_list|(
name|conf
argument_list|,
name|fs
argument_list|,
name|tableDir
argument_list|,
name|regionInfo
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|removeStoreFiles
parameter_list|(
name|String
name|familyName
parameter_list|,
name|Collection
argument_list|<
name|HStoreFile
argument_list|>
name|storeFiles
parameter_list|)
throws|throws
name|IOException
block|{
name|super
operator|.
name|removeStoreFiles
argument_list|(
name|familyName
argument_list|,
name|storeFiles
argument_list|)
expr_stmt|;
name|archived
operator|.
name|set
argument_list|(
literal|true
argument_list|)
expr_stmt|;
synchronized|synchronized
init|(
name|archived
init|)
block|{
name|archived
operator|.
name|notifyAll
argument_list|()
expr_stmt|;
block|}
try|try
block|{
comment|// unfortunately we can't use a stronger barrier here as the fix synchronizing
comment|// the race condition will then block
name|Thread
operator|.
name|sleep
argument_list|(
literal|100
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|ie
parameter_list|)
block|{
throw|throw
operator|new
name|InterruptedIOException
argument_list|(
literal|"Interrupted waiting for latch"
argument_list|)
throw|;
block|}
block|}
block|}
block|}
end_class

end_unit

