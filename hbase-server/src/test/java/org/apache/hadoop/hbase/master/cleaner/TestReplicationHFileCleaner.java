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
name|cleaner
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
name|junit
operator|.
name|Assert
operator|.
name|fail
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
name|spy
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
name|Iterator
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
name|Abortable
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
name|ChoreService
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
name|CoordinatedStateManager
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
name|Server
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
name|ServerName
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
name|ZooKeeperConnectionException
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
name|AsyncClusterConnection
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
name|ClusterConnection
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
name|Connection
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
name|master
operator|.
name|HMaster
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
name|replication
operator|.
name|ReplicationException
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
name|replication
operator|.
name|ReplicationFactory
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
name|replication
operator|.
name|ReplicationPeerConfig
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
name|replication
operator|.
name|ReplicationPeers
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
name|replication
operator|.
name|ReplicationQueueStorage
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
name|replication
operator|.
name|ReplicationStorageFactory
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
name|replication
operator|.
name|SyncReplicationState
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
name|replication
operator|.
name|master
operator|.
name|ReplicationHFileCleaner
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
name|testclassification
operator|.
name|SmallTests
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
name|Pair
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
name|zookeeper
operator|.
name|RecoverableZooKeeper
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
name|zookeeper
operator|.
name|ZKWatcher
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|zookeeper
operator|.
name|KeeperException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|zookeeper
operator|.
name|data
operator|.
name|Stat
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
name|Before
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

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hbase
operator|.
name|thirdparty
operator|.
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

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|MasterTests
operator|.
name|class
block|,
name|SmallTests
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|TestReplicationHFileCleaner
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
name|TestReplicationHFileCleaner
operator|.
name|class
argument_list|)
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
name|TestReplicationHFileCleaner
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
specifier|static
name|HBaseTestingUtility
name|TEST_UTIL
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
specifier|private
specifier|static
name|Server
name|server
decl_stmt|;
specifier|private
specifier|static
name|ReplicationQueueStorage
name|rq
decl_stmt|;
specifier|private
specifier|static
name|ReplicationPeers
name|rp
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|peerId
init|=
literal|"TestReplicationHFileCleaner"
decl_stmt|;
specifier|private
specifier|static
name|Configuration
name|conf
init|=
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
specifier|static
name|FileSystem
name|fs
init|=
literal|null
decl_stmt|;
name|Path
name|root
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
name|TEST_UTIL
operator|.
name|startMiniZKCluster
argument_list|()
expr_stmt|;
name|server
operator|=
operator|new
name|DummyServer
argument_list|()
expr_stmt|;
name|conf
operator|.
name|setBoolean
argument_list|(
name|HConstants
operator|.
name|REPLICATION_BULKLOAD_ENABLE_KEY
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|HMaster
operator|.
name|decorateMasterConfiguration
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|rp
operator|=
name|ReplicationFactory
operator|.
name|getReplicationPeers
argument_list|(
name|server
operator|.
name|getZooKeeper
argument_list|()
argument_list|,
name|conf
argument_list|)
expr_stmt|;
name|rp
operator|.
name|init
argument_list|()
expr_stmt|;
name|rq
operator|=
name|ReplicationStorageFactory
operator|.
name|getReplicationQueueStorage
argument_list|(
name|server
operator|.
name|getZooKeeper
argument_list|()
argument_list|,
name|conf
argument_list|)
expr_stmt|;
name|fs
operator|=
name|FileSystem
operator|.
name|get
argument_list|(
name|conf
argument_list|)
expr_stmt|;
block|}
annotation|@
name|AfterClass
specifier|public
specifier|static
name|void
name|tearDownAfterClass
parameter_list|()
throws|throws
name|Exception
block|{
name|TEST_UTIL
operator|.
name|shutdownMiniZKCluster
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Before
specifier|public
name|void
name|setup
parameter_list|()
throws|throws
name|ReplicationException
throws|,
name|IOException
block|{
name|root
operator|=
name|TEST_UTIL
operator|.
name|getDataTestDirOnTestFS
argument_list|()
expr_stmt|;
name|rp
operator|.
name|getPeerStorage
argument_list|()
operator|.
name|addPeer
argument_list|(
name|peerId
argument_list|,
name|ReplicationPeerConfig
operator|.
name|newBuilder
argument_list|()
operator|.
name|setClusterKey
argument_list|(
name|TEST_UTIL
operator|.
name|getClusterKey
argument_list|()
argument_list|)
operator|.
name|build
argument_list|()
argument_list|,
literal|true
argument_list|,
name|SyncReplicationState
operator|.
name|NONE
argument_list|)
expr_stmt|;
name|rq
operator|.
name|addPeerToHFileRefs
argument_list|(
name|peerId
argument_list|)
expr_stmt|;
block|}
annotation|@
name|After
specifier|public
name|void
name|cleanup
parameter_list|()
throws|throws
name|ReplicationException
block|{
try|try
block|{
name|fs
operator|.
name|delete
argument_list|(
name|root
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Failed to delete files recursively from path "
operator|+
name|root
argument_list|)
expr_stmt|;
block|}
name|rp
operator|.
name|getPeerStorage
argument_list|()
operator|.
name|removePeer
argument_list|(
name|peerId
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testIsFileDeletable
parameter_list|()
throws|throws
name|IOException
throws|,
name|ReplicationException
block|{
comment|// 1. Create a file
name|Path
name|file
init|=
operator|new
name|Path
argument_list|(
name|root
argument_list|,
literal|"testIsFileDeletableWithNoHFileRefs"
argument_list|)
decl_stmt|;
name|fs
operator|.
name|createNewFile
argument_list|(
name|file
argument_list|)
expr_stmt|;
comment|// 2. Assert file is successfully created
name|assertTrue
argument_list|(
literal|"Test file not created!"
argument_list|,
name|fs
operator|.
name|exists
argument_list|(
name|file
argument_list|)
argument_list|)
expr_stmt|;
name|ReplicationHFileCleaner
name|cleaner
init|=
operator|new
name|ReplicationHFileCleaner
argument_list|()
decl_stmt|;
name|cleaner
operator|.
name|setConf
argument_list|(
name|conf
argument_list|)
expr_stmt|;
comment|// 3. Assert that file as is should be deletable
name|assertTrue
argument_list|(
literal|"Cleaner should allow to delete this file as there is no hfile reference node "
operator|+
literal|"for it in the queue."
argument_list|,
name|cleaner
operator|.
name|isFileDeletable
argument_list|(
name|fs
operator|.
name|getFileStatus
argument_list|(
name|file
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|Pair
argument_list|<
name|Path
argument_list|,
name|Path
argument_list|>
argument_list|>
name|files
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
literal|1
argument_list|)
decl_stmt|;
name|files
operator|.
name|add
argument_list|(
operator|new
name|Pair
argument_list|<>
argument_list|(
literal|null
argument_list|,
name|file
argument_list|)
argument_list|)
expr_stmt|;
comment|// 4. Add the file to hfile-refs queue
name|rq
operator|.
name|addHFileRefs
argument_list|(
name|peerId
argument_list|,
name|files
argument_list|)
expr_stmt|;
comment|// 5. Assert file should not be deletable
name|assertFalse
argument_list|(
literal|"Cleaner should not allow to delete this file as there is a hfile reference node "
operator|+
literal|"for it in the queue."
argument_list|,
name|cleaner
operator|.
name|isFileDeletable
argument_list|(
name|fs
operator|.
name|getFileStatus
argument_list|(
name|file
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testGetDeletableFiles
parameter_list|()
throws|throws
name|Exception
block|{
comment|// 1. Create two files and assert that they do not exist
name|Path
name|notDeletablefile
init|=
operator|new
name|Path
argument_list|(
name|root
argument_list|,
literal|"testGetDeletableFiles_1"
argument_list|)
decl_stmt|;
name|fs
operator|.
name|createNewFile
argument_list|(
name|notDeletablefile
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"Test file not created!"
argument_list|,
name|fs
operator|.
name|exists
argument_list|(
name|notDeletablefile
argument_list|)
argument_list|)
expr_stmt|;
name|Path
name|deletablefile
init|=
operator|new
name|Path
argument_list|(
name|root
argument_list|,
literal|"testGetDeletableFiles_2"
argument_list|)
decl_stmt|;
name|fs
operator|.
name|createNewFile
argument_list|(
name|deletablefile
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"Test file not created!"
argument_list|,
name|fs
operator|.
name|exists
argument_list|(
name|deletablefile
argument_list|)
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|FileStatus
argument_list|>
name|files
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
literal|2
argument_list|)
decl_stmt|;
name|FileStatus
name|f
init|=
operator|new
name|FileStatus
argument_list|()
decl_stmt|;
name|f
operator|.
name|setPath
argument_list|(
name|deletablefile
argument_list|)
expr_stmt|;
name|files
operator|.
name|add
argument_list|(
name|f
argument_list|)
expr_stmt|;
name|f
operator|=
operator|new
name|FileStatus
argument_list|()
expr_stmt|;
name|f
operator|.
name|setPath
argument_list|(
name|notDeletablefile
argument_list|)
expr_stmt|;
name|files
operator|.
name|add
argument_list|(
name|f
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|Pair
argument_list|<
name|Path
argument_list|,
name|Path
argument_list|>
argument_list|>
name|hfiles
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
literal|1
argument_list|)
decl_stmt|;
name|hfiles
operator|.
name|add
argument_list|(
operator|new
name|Pair
argument_list|<>
argument_list|(
literal|null
argument_list|,
name|notDeletablefile
argument_list|)
argument_list|)
expr_stmt|;
comment|// 2. Add one file to hfile-refs queue
name|rq
operator|.
name|addHFileRefs
argument_list|(
name|peerId
argument_list|,
name|hfiles
argument_list|)
expr_stmt|;
name|ReplicationHFileCleaner
name|cleaner
init|=
operator|new
name|ReplicationHFileCleaner
argument_list|()
decl_stmt|;
name|cleaner
operator|.
name|setConf
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|Iterator
argument_list|<
name|FileStatus
argument_list|>
name|deletableFilesIterator
init|=
name|cleaner
operator|.
name|getDeletableFiles
argument_list|(
name|files
argument_list|)
operator|.
name|iterator
argument_list|()
decl_stmt|;
name|int
name|i
init|=
literal|0
decl_stmt|;
while|while
condition|(
name|deletableFilesIterator
operator|.
name|hasNext
argument_list|()
operator|&&
name|i
operator|<
literal|2
condition|)
block|{
name|i
operator|++
expr_stmt|;
block|}
comment|// 5. Assert one file should not be deletable and it is present in the list returned
if|if
condition|(
name|i
operator|>
literal|2
condition|)
block|{
name|fail
argument_list|(
literal|"File "
operator|+
name|notDeletablefile
operator|+
literal|" should not be deletable as its hfile reference node is not added."
argument_list|)
expr_stmt|;
block|}
name|assertTrue
argument_list|(
name|deletableFilesIterator
operator|.
name|next
argument_list|()
operator|.
name|getPath
argument_list|()
operator|.
name|equals
argument_list|(
name|deletablefile
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * ReplicationHFileCleaner should be able to ride over ZooKeeper errors without aborting.    */
annotation|@
name|Test
specifier|public
name|void
name|testZooKeeperAbort
parameter_list|()
throws|throws
name|Exception
block|{
name|ReplicationHFileCleaner
name|cleaner
init|=
operator|new
name|ReplicationHFileCleaner
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|FileStatus
argument_list|>
name|dummyFiles
init|=
name|Lists
operator|.
name|newArrayList
argument_list|(
operator|new
name|FileStatus
argument_list|(
literal|100
argument_list|,
literal|false
argument_list|,
literal|3
argument_list|,
literal|100
argument_list|,
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|,
operator|new
name|Path
argument_list|(
literal|"hfile1"
argument_list|)
argument_list|)
argument_list|,
operator|new
name|FileStatus
argument_list|(
literal|100
argument_list|,
literal|false
argument_list|,
literal|3
argument_list|,
literal|100
argument_list|,
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|,
operator|new
name|Path
argument_list|(
literal|"hfile2"
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
name|FaultyZooKeeperWatcher
name|faultyZK
init|=
operator|new
name|FaultyZooKeeperWatcher
argument_list|(
name|conf
argument_list|,
literal|"testZooKeeperAbort-faulty"
argument_list|,
literal|null
argument_list|)
decl_stmt|;
try|try
block|{
name|faultyZK
operator|.
name|init
argument_list|()
expr_stmt|;
name|cleaner
operator|.
name|setConf
argument_list|(
name|conf
argument_list|,
name|faultyZK
argument_list|)
expr_stmt|;
comment|// should keep all files due to a ConnectionLossException getting the queues znodes
name|Iterable
argument_list|<
name|FileStatus
argument_list|>
name|toDelete
init|=
name|cleaner
operator|.
name|getDeletableFiles
argument_list|(
name|dummyFiles
argument_list|)
decl_stmt|;
name|assertFalse
argument_list|(
name|toDelete
operator|.
name|iterator
argument_list|()
operator|.
name|hasNext
argument_list|()
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|cleaner
operator|.
name|isStopped
argument_list|()
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|faultyZK
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
comment|// when zk is working both files should be returned
name|cleaner
operator|=
operator|new
name|ReplicationHFileCleaner
argument_list|()
expr_stmt|;
name|ZKWatcher
name|zkw
init|=
operator|new
name|ZKWatcher
argument_list|(
name|conf
argument_list|,
literal|"testZooKeeperAbort-normal"
argument_list|,
literal|null
argument_list|)
decl_stmt|;
try|try
block|{
name|cleaner
operator|.
name|setConf
argument_list|(
name|conf
argument_list|,
name|zkw
argument_list|)
expr_stmt|;
name|Iterable
argument_list|<
name|FileStatus
argument_list|>
name|filesToDelete
init|=
name|cleaner
operator|.
name|getDeletableFiles
argument_list|(
name|dummyFiles
argument_list|)
decl_stmt|;
name|Iterator
argument_list|<
name|FileStatus
argument_list|>
name|iter
init|=
name|filesToDelete
operator|.
name|iterator
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
name|iter
operator|.
name|hasNext
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
operator|new
name|Path
argument_list|(
literal|"hfile1"
argument_list|)
argument_list|,
name|iter
operator|.
name|next
argument_list|()
operator|.
name|getPath
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|iter
operator|.
name|hasNext
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
operator|new
name|Path
argument_list|(
literal|"hfile2"
argument_list|)
argument_list|,
name|iter
operator|.
name|next
argument_list|()
operator|.
name|getPath
argument_list|()
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|iter
operator|.
name|hasNext
argument_list|()
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|zkw
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
specifier|static
class|class
name|DummyServer
implements|implements
name|Server
block|{
annotation|@
name|Override
specifier|public
name|Configuration
name|getConfiguration
parameter_list|()
block|{
return|return
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|ZKWatcher
name|getZooKeeper
parameter_list|()
block|{
try|try
block|{
return|return
operator|new
name|ZKWatcher
argument_list|(
name|getConfiguration
argument_list|()
argument_list|,
literal|"dummy server"
argument_list|,
name|this
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
block|}
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|CoordinatedStateManager
name|getCoordinatedStateManager
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|ClusterConnection
name|getConnection
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|ServerName
name|getServerName
parameter_list|()
block|{
return|return
name|ServerName
operator|.
name|valueOf
argument_list|(
literal|"regionserver,60020,000000"
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|abort
parameter_list|(
name|String
name|why
parameter_list|,
name|Throwable
name|e
parameter_list|)
block|{     }
annotation|@
name|Override
specifier|public
name|boolean
name|isAborted
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|stop
parameter_list|(
name|String
name|why
parameter_list|)
block|{     }
annotation|@
name|Override
specifier|public
name|boolean
name|isStopped
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
annotation|@
name|Override
specifier|public
name|ChoreService
name|getChoreService
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|ClusterConnection
name|getClusterConnection
parameter_list|()
block|{
comment|// TODO Auto-generated method stub
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|FileSystem
name|getFileSystem
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isStopping
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
annotation|@
name|Override
specifier|public
name|Connection
name|createConnection
parameter_list|(
name|Configuration
name|conf
parameter_list|)
throws|throws
name|IOException
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|AsyncClusterConnection
name|getAsyncClusterConnection
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
block|}
specifier|static
class|class
name|FaultyZooKeeperWatcher
extends|extends
name|ZKWatcher
block|{
specifier|private
name|RecoverableZooKeeper
name|zk
decl_stmt|;
specifier|public
name|FaultyZooKeeperWatcher
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|String
name|identifier
parameter_list|,
name|Abortable
name|abortable
parameter_list|)
throws|throws
name|ZooKeeperConnectionException
throws|,
name|IOException
block|{
name|super
argument_list|(
name|conf
argument_list|,
name|identifier
argument_list|,
name|abortable
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|init
parameter_list|()
throws|throws
name|Exception
block|{
name|this
operator|.
name|zk
operator|=
name|spy
argument_list|(
name|super
operator|.
name|getRecoverableZooKeeper
argument_list|()
argument_list|)
expr_stmt|;
name|doThrow
argument_list|(
operator|new
name|KeeperException
operator|.
name|ConnectionLossException
argument_list|()
argument_list|)
operator|.
name|when
argument_list|(
name|zk
argument_list|)
operator|.
name|getData
argument_list|(
literal|"/hbase/replication/hfile-refs"
argument_list|,
literal|null
argument_list|,
operator|new
name|Stat
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|RecoverableZooKeeper
name|getRecoverableZooKeeper
parameter_list|()
block|{
return|return
name|zk
return|;
block|}
block|}
block|}
end_class

end_unit

