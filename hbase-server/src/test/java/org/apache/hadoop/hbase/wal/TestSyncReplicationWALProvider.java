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
name|wal
package|;
end_package

begin_import
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|CoreMatchers
operator|.
name|instanceOf
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|CoreMatchers
operator|.
name|not
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
name|assertThat
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
name|Optional
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|function
operator|.
name|BiPredicate
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
name|Waiter
operator|.
name|ExplainingPredicate
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
name|regionserver
operator|.
name|MultiVersionConcurrencyControl
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
name|wal
operator|.
name|DualAsyncFSWAL
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
name|wal
operator|.
name|ProtobufLogReader
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
name|wal
operator|.
name|ProtobufLogTestHelper
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
name|regionserver
operator|.
name|SyncReplicationPeerInfoProvider
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
name|hdfs
operator|.
name|DistributedFileSystem
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
name|TestSyncReplicationWALProvider
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
name|TestSyncReplicationWALProvider
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
name|String
name|PEER_ID
init|=
literal|"1"
decl_stmt|;
specifier|private
specifier|static
name|String
name|REMOTE_WAL_DIR
init|=
literal|"/RemoteWAL"
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
literal|"table"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|TableName
name|TABLE_NO_REP
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"table-no-rep"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|RegionInfo
name|REGION
init|=
name|RegionInfoBuilder
operator|.
name|newBuilder
argument_list|(
name|TABLE
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
specifier|private
specifier|static
name|RegionInfo
name|REGION_NO_REP
init|=
name|RegionInfoBuilder
operator|.
name|newBuilder
argument_list|(
name|TABLE_NO_REP
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
specifier|private
specifier|static
name|WALFactory
name|FACTORY
decl_stmt|;
specifier|public
specifier|static
specifier|final
class|class
name|InfoProvider
implements|implements
name|SyncReplicationPeerInfoProvider
block|{
annotation|@
name|Override
specifier|public
name|Optional
argument_list|<
name|Pair
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|>
name|getPeerIdAndRemoteWALDir
parameter_list|(
name|TableName
name|table
parameter_list|)
block|{
if|if
condition|(
name|table
operator|!=
literal|null
operator|&&
name|table
operator|.
name|equals
argument_list|(
name|TABLE
argument_list|)
condition|)
block|{
return|return
name|Optional
operator|.
name|of
argument_list|(
name|Pair
operator|.
name|newPair
argument_list|(
name|PEER_ID
argument_list|,
name|REMOTE_WAL_DIR
argument_list|)
argument_list|)
return|;
block|}
else|else
block|{
return|return
name|Optional
operator|.
name|empty
argument_list|()
return|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|checkState
parameter_list|(
name|TableName
name|table
parameter_list|,
name|BiPredicate
argument_list|<
name|SyncReplicationState
argument_list|,
name|SyncReplicationState
argument_list|>
name|checker
parameter_list|)
block|{
return|return
literal|false
return|;
block|}
block|}
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
name|UTIL
operator|.
name|startMiniDFSCluster
argument_list|(
literal|3
argument_list|)
expr_stmt|;
name|FACTORY
operator|=
operator|new
name|WALFactory
argument_list|(
name|UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
literal|"test"
argument_list|)
expr_stmt|;
operator|(
operator|(
name|SyncReplicationWALProvider
operator|)
name|FACTORY
operator|.
name|getWALProvider
argument_list|()
operator|)
operator|.
name|setPeerInfoProvider
argument_list|(
operator|new
name|InfoProvider
argument_list|()
argument_list|)
expr_stmt|;
name|UTIL
operator|.
name|getTestFileSystem
argument_list|()
operator|.
name|mkdirs
argument_list|(
operator|new
name|Path
argument_list|(
name|REMOTE_WAL_DIR
argument_list|,
name|PEER_ID
argument_list|)
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
name|IOException
block|{
name|FACTORY
operator|.
name|close
argument_list|()
expr_stmt|;
name|UTIL
operator|.
name|shutdownMiniDFSCluster
argument_list|()
expr_stmt|;
block|}
specifier|private
name|void
name|testReadWrite
parameter_list|(
name|DualAsyncFSWAL
name|wal
parameter_list|)
throws|throws
name|Exception
block|{
name|int
name|recordCount
init|=
literal|100
decl_stmt|;
name|int
name|columnCount
init|=
literal|10
decl_stmt|;
name|byte
index|[]
name|row
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"testRow"
argument_list|)
decl_stmt|;
name|long
name|timestamp
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|MultiVersionConcurrencyControl
name|mvcc
init|=
operator|new
name|MultiVersionConcurrencyControl
argument_list|()
decl_stmt|;
name|ProtobufLogTestHelper
operator|.
name|doWrite
argument_list|(
name|wal
argument_list|,
name|REGION
argument_list|,
name|TABLE
argument_list|,
name|columnCount
argument_list|,
name|recordCount
argument_list|,
name|row
argument_list|,
name|timestamp
argument_list|,
name|mvcc
argument_list|)
expr_stmt|;
name|Path
name|localFile
init|=
name|wal
operator|.
name|getCurrentFileName
argument_list|()
decl_stmt|;
name|Path
name|remoteFile
init|=
operator|new
name|Path
argument_list|(
name|REMOTE_WAL_DIR
operator|+
literal|"/"
operator|+
name|PEER_ID
argument_list|,
name|localFile
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
try|try
init|(
name|ProtobufLogReader
name|reader
init|=
operator|(
name|ProtobufLogReader
operator|)
name|FACTORY
operator|.
name|createReader
argument_list|(
name|UTIL
operator|.
name|getTestFileSystem
argument_list|()
argument_list|,
name|localFile
argument_list|)
init|)
block|{
name|ProtobufLogTestHelper
operator|.
name|doRead
argument_list|(
name|reader
argument_list|,
literal|false
argument_list|,
name|REGION
argument_list|,
name|TABLE
argument_list|,
name|columnCount
argument_list|,
name|recordCount
argument_list|,
name|row
argument_list|,
name|timestamp
argument_list|)
expr_stmt|;
block|}
try|try
init|(
name|ProtobufLogReader
name|reader
init|=
operator|(
name|ProtobufLogReader
operator|)
name|FACTORY
operator|.
name|createReader
argument_list|(
name|UTIL
operator|.
name|getTestFileSystem
argument_list|()
argument_list|,
name|remoteFile
argument_list|)
init|)
block|{
name|ProtobufLogTestHelper
operator|.
name|doRead
argument_list|(
name|reader
argument_list|,
literal|false
argument_list|,
name|REGION
argument_list|,
name|TABLE
argument_list|,
name|columnCount
argument_list|,
name|recordCount
argument_list|,
name|row
argument_list|,
name|timestamp
argument_list|)
expr_stmt|;
block|}
name|wal
operator|.
name|rollWriter
argument_list|()
expr_stmt|;
name|DistributedFileSystem
name|dfs
init|=
operator|(
name|DistributedFileSystem
operator|)
name|UTIL
operator|.
name|getDFSCluster
argument_list|()
operator|.
name|getFileSystem
argument_list|()
decl_stmt|;
name|UTIL
operator|.
name|waitFor
argument_list|(
literal|5000
argument_list|,
operator|new
name|ExplainingPredicate
argument_list|<
name|Exception
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|boolean
name|evaluate
parameter_list|()
throws|throws
name|Exception
block|{
return|return
name|dfs
operator|.
name|isFileClosed
argument_list|(
name|localFile
argument_list|)
operator|&&
name|dfs
operator|.
name|isFileClosed
argument_list|(
name|remoteFile
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|explainFailure
parameter_list|()
throws|throws
name|Exception
block|{
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|dfs
operator|.
name|isFileClosed
argument_list|(
name|localFile
argument_list|)
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
name|localFile
operator|+
literal|" has not been closed yet."
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|dfs
operator|.
name|isFileClosed
argument_list|(
name|remoteFile
argument_list|)
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
name|remoteFile
operator|+
literal|" has not been closed yet."
argument_list|)
expr_stmt|;
block|}
return|return
name|sb
operator|.
name|toString
argument_list|()
return|;
block|}
block|}
argument_list|)
expr_stmt|;
try|try
init|(
name|ProtobufLogReader
name|reader
init|=
operator|(
name|ProtobufLogReader
operator|)
name|FACTORY
operator|.
name|createReader
argument_list|(
name|UTIL
operator|.
name|getTestFileSystem
argument_list|()
argument_list|,
name|localFile
argument_list|)
init|)
block|{
name|ProtobufLogTestHelper
operator|.
name|doRead
argument_list|(
name|reader
argument_list|,
literal|true
argument_list|,
name|REGION
argument_list|,
name|TABLE
argument_list|,
name|columnCount
argument_list|,
name|recordCount
argument_list|,
name|row
argument_list|,
name|timestamp
argument_list|)
expr_stmt|;
block|}
try|try
init|(
name|ProtobufLogReader
name|reader
init|=
operator|(
name|ProtobufLogReader
operator|)
name|FACTORY
operator|.
name|createReader
argument_list|(
name|UTIL
operator|.
name|getTestFileSystem
argument_list|()
argument_list|,
name|remoteFile
argument_list|)
init|)
block|{
name|ProtobufLogTestHelper
operator|.
name|doRead
argument_list|(
name|reader
argument_list|,
literal|true
argument_list|,
name|REGION
argument_list|,
name|TABLE
argument_list|,
name|columnCount
argument_list|,
name|recordCount
argument_list|,
name|row
argument_list|,
name|timestamp
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|test
parameter_list|()
throws|throws
name|Exception
block|{
name|WAL
name|walNoRep
init|=
name|FACTORY
operator|.
name|getWAL
argument_list|(
name|REGION_NO_REP
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|walNoRep
argument_list|,
name|not
argument_list|(
name|instanceOf
argument_list|(
name|DualAsyncFSWAL
operator|.
name|class
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|DualAsyncFSWAL
name|wal
init|=
operator|(
name|DualAsyncFSWAL
operator|)
name|FACTORY
operator|.
name|getWAL
argument_list|(
name|REGION
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|FACTORY
operator|.
name|getWALs
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|testReadWrite
argument_list|(
name|wal
argument_list|)
expr_stmt|;
name|SyncReplicationWALProvider
name|walProvider
init|=
operator|(
name|SyncReplicationWALProvider
operator|)
name|FACTORY
operator|.
name|getWALProvider
argument_list|()
decl_stmt|;
name|walProvider
operator|.
name|peerSyncReplicationStateChange
argument_list|(
name|PEER_ID
argument_list|,
name|SyncReplicationState
operator|.
name|ACTIVE
argument_list|,
name|SyncReplicationState
operator|.
name|DOWNGRADE_ACTIVE
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|FACTORY
operator|.
name|getWALs
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

