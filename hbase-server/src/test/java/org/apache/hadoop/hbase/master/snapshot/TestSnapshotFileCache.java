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
name|master
operator|.
name|snapshot
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
name|*
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
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Iterables
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
name|Lists
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
name|FileStatus
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
name|protobuf
operator|.
name|generated
operator|.
name|SnapshotProtos
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
name|MasterTests
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
name|protobuf
operator|.
name|generated
operator|.
name|HBaseProtos
operator|.
name|SnapshotDescription
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
name|snapshot
operator|.
name|SnapshotDescriptionUtils
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
name|snapshot
operator|.
name|SnapshotReferenceUtil
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
name|snapshot
operator|.
name|SnapshotTestingUtils
operator|.
name|SnapshotMock
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
name|AfterClass
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

begin_comment
comment|/**  * Test that we correctly reload the cache, filter directories, etc.  */
end_comment

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|MasterTests
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
name|TestSnapshotFileCache
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
name|TestSnapshotFileCache
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|HBaseTestingUtility
name|UTIL
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
specifier|private
specifier|static
name|long
name|sequenceId
init|=
literal|0
decl_stmt|;
specifier|private
specifier|static
name|FileSystem
name|fs
decl_stmt|;
specifier|private
specifier|static
name|Path
name|rootDir
decl_stmt|;
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|startCluster
parameter_list|()
throws|throws
name|Exception
block|{
name|UTIL
operator|.
name|startMiniDFSCluster
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|fs
operator|=
name|UTIL
operator|.
name|getDFSCluster
argument_list|()
operator|.
name|getFileSystem
argument_list|()
expr_stmt|;
name|rootDir
operator|=
name|UTIL
operator|.
name|getDefaultRootDirPath
argument_list|()
expr_stmt|;
block|}
annotation|@
name|AfterClass
specifier|public
specifier|static
name|void
name|stopCluster
parameter_list|()
throws|throws
name|Exception
block|{
name|UTIL
operator|.
name|shutdownMiniDFSCluster
argument_list|()
expr_stmt|;
block|}
annotation|@
name|After
specifier|public
name|void
name|cleanupFiles
parameter_list|()
throws|throws
name|Exception
block|{
comment|// cleanup the snapshot directory
name|Path
name|snapshotDir
init|=
name|SnapshotDescriptionUtils
operator|.
name|getSnapshotsDir
argument_list|(
name|rootDir
argument_list|)
decl_stmt|;
name|fs
operator|.
name|delete
argument_list|(
name|snapshotDir
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|10000000
argument_list|)
specifier|public
name|void
name|testLoadAndDelete
parameter_list|()
throws|throws
name|IOException
block|{
comment|// don't refresh the cache unless we tell it to
name|long
name|period
init|=
name|Long
operator|.
name|MAX_VALUE
decl_stmt|;
name|SnapshotFileCache
name|cache
init|=
operator|new
name|SnapshotFileCache
argument_list|(
name|fs
argument_list|,
name|rootDir
argument_list|,
name|period
argument_list|,
literal|10000000
argument_list|,
literal|"test-snapshot-file-cache-refresh"
argument_list|,
operator|new
name|SnapshotFiles
argument_list|()
argument_list|)
decl_stmt|;
name|createAndTestSnapshotV1
argument_list|(
name|cache
argument_list|,
literal|"snapshot1a"
argument_list|,
literal|false
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|createAndTestSnapshotV1
argument_list|(
name|cache
argument_list|,
literal|"snapshot1b"
argument_list|,
literal|true
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|createAndTestSnapshotV2
argument_list|(
name|cache
argument_list|,
literal|"snapshot2a"
argument_list|,
literal|false
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|createAndTestSnapshotV2
argument_list|(
name|cache
argument_list|,
literal|"snapshot2b"
argument_list|,
literal|true
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testReloadModifiedDirectory
parameter_list|()
throws|throws
name|IOException
block|{
comment|// don't refresh the cache unless we tell it to
name|long
name|period
init|=
name|Long
operator|.
name|MAX_VALUE
decl_stmt|;
name|SnapshotFileCache
name|cache
init|=
operator|new
name|SnapshotFileCache
argument_list|(
name|fs
argument_list|,
name|rootDir
argument_list|,
name|period
argument_list|,
literal|10000000
argument_list|,
literal|"test-snapshot-file-cache-refresh"
argument_list|,
operator|new
name|SnapshotFiles
argument_list|()
argument_list|)
decl_stmt|;
name|createAndTestSnapshotV1
argument_list|(
name|cache
argument_list|,
literal|"snapshot1"
argument_list|,
literal|false
argument_list|,
literal|true
argument_list|)
expr_stmt|;
comment|// now delete the snapshot and add a file with a different name
name|createAndTestSnapshotV1
argument_list|(
name|cache
argument_list|,
literal|"snapshot1"
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|createAndTestSnapshotV2
argument_list|(
name|cache
argument_list|,
literal|"snapshot2"
argument_list|,
literal|false
argument_list|,
literal|true
argument_list|)
expr_stmt|;
comment|// now delete the snapshot and add a file with a different name
name|createAndTestSnapshotV2
argument_list|(
name|cache
argument_list|,
literal|"snapshot2"
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testSnapshotTempDirReload
parameter_list|()
throws|throws
name|IOException
block|{
name|long
name|period
init|=
name|Long
operator|.
name|MAX_VALUE
decl_stmt|;
comment|// This doesn't refresh cache until we invoke it explicitly
name|SnapshotFileCache
name|cache
init|=
operator|new
name|SnapshotFileCache
argument_list|(
name|fs
argument_list|,
name|rootDir
argument_list|,
name|period
argument_list|,
literal|10000000
argument_list|,
literal|"test-snapshot-file-cache-refresh"
argument_list|,
operator|new
name|SnapshotFiles
argument_list|()
argument_list|)
decl_stmt|;
comment|// Add a new non-tmp snapshot
name|createAndTestSnapshotV1
argument_list|(
name|cache
argument_list|,
literal|"snapshot0v1"
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|createAndTestSnapshotV1
argument_list|(
name|cache
argument_list|,
literal|"snapshot0v2"
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|)
expr_stmt|;
comment|// Add a new tmp snapshot
name|createAndTestSnapshotV2
argument_list|(
name|cache
argument_list|,
literal|"snapshot1"
argument_list|,
literal|true
argument_list|,
literal|false
argument_list|)
expr_stmt|;
comment|// Add another tmp snapshot
name|createAndTestSnapshotV2
argument_list|(
name|cache
argument_list|,
literal|"snapshot2"
argument_list|,
literal|true
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testWeNeverCacheTmpDirAndLoadIt
parameter_list|()
throws|throws
name|Exception
block|{
specifier|final
name|AtomicInteger
name|count
init|=
operator|new
name|AtomicInteger
argument_list|(
literal|0
argument_list|)
decl_stmt|;
comment|// don't refresh the cache unless we tell it to
name|long
name|period
init|=
name|Long
operator|.
name|MAX_VALUE
decl_stmt|;
name|SnapshotFileCache
name|cache
init|=
operator|new
name|SnapshotFileCache
argument_list|(
name|fs
argument_list|,
name|rootDir
argument_list|,
name|period
argument_list|,
literal|10000000
argument_list|,
literal|"test-snapshot-file-cache-refresh"
argument_list|,
operator|new
name|SnapshotFiles
argument_list|()
argument_list|)
block|{
annotation|@
name|Override
name|List
argument_list|<
name|String
argument_list|>
name|getSnapshotsInProgress
parameter_list|()
throws|throws
name|IOException
block|{
name|List
argument_list|<
name|String
argument_list|>
name|result
init|=
name|super
operator|.
name|getSnapshotsInProgress
argument_list|()
decl_stmt|;
name|count
operator|.
name|incrementAndGet
argument_list|()
expr_stmt|;
return|return
name|result
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|triggerCacheRefreshForTesting
parameter_list|()
block|{
name|super
operator|.
name|triggerCacheRefreshForTesting
argument_list|()
expr_stmt|;
block|}
block|}
decl_stmt|;
name|SnapshotMock
operator|.
name|SnapshotBuilder
name|complete
init|=
name|createAndTestSnapshotV1
argument_list|(
name|cache
argument_list|,
literal|"snapshot"
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|SnapshotMock
operator|.
name|SnapshotBuilder
name|inProgress
init|=
name|createAndTestSnapshotV1
argument_list|(
name|cache
argument_list|,
literal|"snapshotInProgress"
argument_list|,
literal|true
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|int
name|countBeforeCheck
init|=
name|count
operator|.
name|get
argument_list|()
decl_stmt|;
name|FSUtils
operator|.
name|logFileSystemState
argument_list|(
name|fs
argument_list|,
name|rootDir
argument_list|,
name|LOG
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|FileStatus
argument_list|>
name|allStoreFiles
init|=
name|getStoreFilesForSnapshot
argument_list|(
name|complete
argument_list|)
decl_stmt|;
name|Iterable
argument_list|<
name|FileStatus
argument_list|>
name|deletableFiles
init|=
name|cache
operator|.
name|getUnreferencedFiles
argument_list|(
name|allStoreFiles
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|Iterables
operator|.
name|isEmpty
argument_list|(
name|deletableFiles
argument_list|)
argument_list|)
expr_stmt|;
comment|// no need for tmp dir check as all files are accounted for.
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|count
operator|.
name|get
argument_list|()
operator|-
name|countBeforeCheck
argument_list|)
expr_stmt|;
comment|// add a random file to make sure we refresh
name|FileStatus
name|randomFile
init|=
name|mockStoreFile
argument_list|(
name|UUID
operator|.
name|randomUUID
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
decl_stmt|;
name|allStoreFiles
operator|.
name|add
argument_list|(
name|randomFile
argument_list|)
expr_stmt|;
name|deletableFiles
operator|=
name|cache
operator|.
name|getUnreferencedFiles
argument_list|(
name|allStoreFiles
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|randomFile
argument_list|,
name|Iterables
operator|.
name|getOnlyElement
argument_list|(
name|deletableFiles
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|count
operator|.
name|get
argument_list|()
operator|-
name|countBeforeCheck
argument_list|)
expr_stmt|;
comment|// we check the tmp directory
block|}
specifier|private
name|List
argument_list|<
name|FileStatus
argument_list|>
name|getStoreFilesForSnapshot
parameter_list|(
name|SnapshotMock
operator|.
name|SnapshotBuilder
name|builder
parameter_list|)
throws|throws
name|IOException
block|{
specifier|final
name|List
argument_list|<
name|FileStatus
argument_list|>
name|allStoreFiles
init|=
name|Lists
operator|.
name|newArrayList
argument_list|()
decl_stmt|;
name|SnapshotReferenceUtil
operator|.
name|visitReferencedFiles
argument_list|(
name|UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|fs
argument_list|,
name|builder
operator|.
name|getSnapshotsDir
argument_list|()
argument_list|,
operator|new
name|SnapshotReferenceUtil
operator|.
name|SnapshotVisitor
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|storeFile
parameter_list|(
name|HRegionInfo
name|regionInfo
parameter_list|,
name|String
name|familyName
parameter_list|,
name|SnapshotProtos
operator|.
name|SnapshotRegionManifest
operator|.
name|StoreFile
name|storeFile
parameter_list|)
throws|throws
name|IOException
block|{
name|FileStatus
name|status
init|=
name|mockStoreFile
argument_list|(
name|storeFile
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
name|allStoreFiles
operator|.
name|add
argument_list|(
name|status
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
return|return
name|allStoreFiles
return|;
block|}
specifier|private
name|FileStatus
name|mockStoreFile
parameter_list|(
name|String
name|storeFileName
parameter_list|)
block|{
name|FileStatus
name|status
init|=
name|mock
argument_list|(
name|FileStatus
operator|.
name|class
argument_list|)
decl_stmt|;
name|Path
name|path
init|=
name|mock
argument_list|(
name|Path
operator|.
name|class
argument_list|)
decl_stmt|;
name|when
argument_list|(
name|path
operator|.
name|getName
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|storeFileName
argument_list|)
expr_stmt|;
name|when
argument_list|(
name|status
operator|.
name|getPath
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|path
argument_list|)
expr_stmt|;
return|return
name|status
return|;
block|}
class|class
name|SnapshotFiles
implements|implements
name|SnapshotFileCache
operator|.
name|SnapshotFileInspector
block|{
specifier|public
name|Collection
argument_list|<
name|String
argument_list|>
name|filesUnderSnapshot
parameter_list|(
specifier|final
name|Path
name|snapshotDir
parameter_list|)
throws|throws
name|IOException
block|{
name|Collection
argument_list|<
name|String
argument_list|>
name|files
init|=
operator|new
name|HashSet
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|files
operator|.
name|addAll
argument_list|(
name|SnapshotReferenceUtil
operator|.
name|getHFileNames
argument_list|(
name|UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|fs
argument_list|,
name|snapshotDir
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|files
return|;
block|}
block|}
empty_stmt|;
specifier|private
name|SnapshotMock
operator|.
name|SnapshotBuilder
name|createAndTestSnapshotV1
parameter_list|(
specifier|final
name|SnapshotFileCache
name|cache
parameter_list|,
specifier|final
name|String
name|name
parameter_list|,
specifier|final
name|boolean
name|tmp
parameter_list|,
specifier|final
name|boolean
name|removeOnExit
parameter_list|)
throws|throws
name|IOException
block|{
name|SnapshotMock
name|snapshotMock
init|=
operator|new
name|SnapshotMock
argument_list|(
name|UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|fs
argument_list|,
name|rootDir
argument_list|)
decl_stmt|;
name|SnapshotMock
operator|.
name|SnapshotBuilder
name|builder
init|=
name|snapshotMock
operator|.
name|createSnapshotV1
argument_list|(
name|name
argument_list|)
decl_stmt|;
name|createAndTestSnapshot
argument_list|(
name|cache
argument_list|,
name|builder
argument_list|,
name|tmp
argument_list|,
name|removeOnExit
argument_list|)
expr_stmt|;
return|return
name|builder
return|;
block|}
specifier|private
name|void
name|createAndTestSnapshotV2
parameter_list|(
specifier|final
name|SnapshotFileCache
name|cache
parameter_list|,
specifier|final
name|String
name|name
parameter_list|,
specifier|final
name|boolean
name|tmp
parameter_list|,
specifier|final
name|boolean
name|removeOnExit
parameter_list|)
throws|throws
name|IOException
block|{
name|SnapshotMock
name|snapshotMock
init|=
operator|new
name|SnapshotMock
argument_list|(
name|UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|fs
argument_list|,
name|rootDir
argument_list|)
decl_stmt|;
name|SnapshotMock
operator|.
name|SnapshotBuilder
name|builder
init|=
name|snapshotMock
operator|.
name|createSnapshotV2
argument_list|(
name|name
argument_list|)
decl_stmt|;
name|createAndTestSnapshot
argument_list|(
name|cache
argument_list|,
name|builder
argument_list|,
name|tmp
argument_list|,
name|removeOnExit
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|createAndTestSnapshot
parameter_list|(
specifier|final
name|SnapshotFileCache
name|cache
parameter_list|,
specifier|final
name|SnapshotMock
operator|.
name|SnapshotBuilder
name|builder
parameter_list|,
specifier|final
name|boolean
name|tmp
parameter_list|,
specifier|final
name|boolean
name|removeOnExit
parameter_list|)
throws|throws
name|IOException
block|{
name|List
argument_list|<
name|Path
argument_list|>
name|files
init|=
operator|new
name|ArrayList
argument_list|<
name|Path
argument_list|>
argument_list|()
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
operator|++
name|i
control|)
block|{
for|for
control|(
name|Path
name|filePath
range|:
name|builder
operator|.
name|addRegion
argument_list|()
control|)
block|{
name|String
name|fileName
init|=
name|filePath
operator|.
name|getName
argument_list|()
decl_stmt|;
if|if
condition|(
name|tmp
condition|)
block|{
comment|// We should be able to find all the files while the snapshot creation is in-progress
name|FSUtils
operator|.
name|logFileSystemState
argument_list|(
name|fs
argument_list|,
name|rootDir
argument_list|,
name|LOG
argument_list|)
expr_stmt|;
name|Iterable
argument_list|<
name|FileStatus
argument_list|>
name|nonSnapshot
init|=
name|getNonSnapshotFiles
argument_list|(
name|cache
argument_list|,
name|filePath
argument_list|)
decl_stmt|;
name|assertFalse
argument_list|(
literal|"Cache didn't find "
operator|+
name|fileName
argument_list|,
name|Iterables
operator|.
name|contains
argument_list|(
name|nonSnapshot
argument_list|,
name|fileName
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|files
operator|.
name|add
argument_list|(
name|filePath
argument_list|)
expr_stmt|;
block|}
block|}
comment|// Finalize the snapshot
if|if
condition|(
operator|!
name|tmp
condition|)
block|{
name|builder
operator|.
name|commit
argument_list|()
expr_stmt|;
block|}
comment|// Make sure that all files are still present
for|for
control|(
name|Path
name|path
range|:
name|files
control|)
block|{
name|Iterable
argument_list|<
name|FileStatus
argument_list|>
name|nonSnapshotFiles
init|=
name|getNonSnapshotFiles
argument_list|(
name|cache
argument_list|,
name|path
argument_list|)
decl_stmt|;
name|assertFalse
argument_list|(
literal|"Cache didn't find "
operator|+
name|path
operator|.
name|getName
argument_list|()
argument_list|,
name|Iterables
operator|.
name|contains
argument_list|(
name|nonSnapshotFiles
argument_list|,
name|path
operator|.
name|getName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|FSUtils
operator|.
name|logFileSystemState
argument_list|(
name|fs
argument_list|,
name|rootDir
argument_list|,
name|LOG
argument_list|)
expr_stmt|;
if|if
condition|(
name|removeOnExit
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Deleting snapshot."
argument_list|)
expr_stmt|;
name|fs
operator|.
name|delete
argument_list|(
name|builder
operator|.
name|getSnapshotsDir
argument_list|()
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|FSUtils
operator|.
name|logFileSystemState
argument_list|(
name|fs
argument_list|,
name|rootDir
argument_list|,
name|LOG
argument_list|)
expr_stmt|;
comment|// The files should be in cache until next refresh
for|for
control|(
name|Path
name|filePath
range|:
name|files
control|)
block|{
name|Iterable
argument_list|<
name|FileStatus
argument_list|>
name|nonSnapshotFiles
init|=
name|getNonSnapshotFiles
argument_list|(
name|cache
argument_list|,
name|filePath
argument_list|)
decl_stmt|;
name|assertFalse
argument_list|(
literal|"Cache didn't find "
operator|+
name|filePath
operator|.
name|getName
argument_list|()
argument_list|,
name|Iterables
operator|.
name|contains
argument_list|(
name|nonSnapshotFiles
argument_list|,
name|filePath
operator|.
name|getName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// then trigger a refresh
name|cache
operator|.
name|triggerCacheRefreshForTesting
argument_list|()
expr_stmt|;
comment|// and not it shouldn't find those files
for|for
control|(
name|Path
name|filePath
range|:
name|files
control|)
block|{
name|Iterable
argument_list|<
name|FileStatus
argument_list|>
name|nonSnapshotFiles
init|=
name|getNonSnapshotFiles
argument_list|(
name|cache
argument_list|,
name|filePath
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
literal|"Cache found '"
operator|+
name|filePath
operator|.
name|getName
argument_list|()
operator|+
literal|"', but it shouldn't have."
argument_list|,
operator|!
name|Iterables
operator|.
name|contains
argument_list|(
name|nonSnapshotFiles
argument_list|,
name|filePath
operator|.
name|getName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|private
name|Iterable
argument_list|<
name|FileStatus
argument_list|>
name|getNonSnapshotFiles
parameter_list|(
name|SnapshotFileCache
name|cache
parameter_list|,
name|Path
name|storeFile
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|cache
operator|.
name|getUnreferencedFiles
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|FSUtils
operator|.
name|listStatus
argument_list|(
name|fs
argument_list|,
name|storeFile
operator|.
name|getParent
argument_list|()
argument_list|)
argument_list|)
argument_list|)
return|;
block|}
block|}
end_class

end_unit

