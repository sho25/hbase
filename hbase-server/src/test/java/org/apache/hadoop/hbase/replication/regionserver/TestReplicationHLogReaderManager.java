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
name|replication
operator|.
name|regionserver
package|;
end_package

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
name|KeyValue
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
name|regionserver
operator|.
name|wal
operator|.
name|HLog
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
name|HLogFactory
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
name|HLogKey
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
name|WALActionsListener
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
name|hdfs
operator|.
name|MiniDFSCluster
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
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|*
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|EOFException
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
name|List
import|;
end_import

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
name|TestReplicationHLogReaderManager
block|{
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
name|Configuration
name|conf
decl_stmt|;
specifier|private
specifier|static
name|Path
name|hbaseDir
decl_stmt|;
specifier|private
specifier|static
name|FileSystem
name|fs
decl_stmt|;
specifier|private
specifier|static
name|MiniDFSCluster
name|cluster
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|TableName
name|tableName
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"tablename"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|family
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"column"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|qualifier
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"qualifier"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|HRegionInfo
name|info
init|=
operator|new
name|HRegionInfo
argument_list|(
name|tableName
argument_list|,
name|HConstants
operator|.
name|EMPTY_START_ROW
argument_list|,
name|HConstants
operator|.
name|LAST_ROW
argument_list|,
literal|false
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|HTableDescriptor
name|htd
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
specifier|private
name|HLog
name|log
decl_stmt|;
specifier|private
name|ReplicationHLogReaderManager
name|logManager
decl_stmt|;
specifier|private
name|PathWatcher
name|pathWatcher
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
name|startMiniDFSCluster
argument_list|(
literal|3
argument_list|)
expr_stmt|;
name|conf
operator|=
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
expr_stmt|;
name|hbaseDir
operator|=
name|TEST_UTIL
operator|.
name|createRootDir
argument_list|()
expr_stmt|;
name|cluster
operator|=
name|TEST_UTIL
operator|.
name|getDFSCluster
argument_list|()
expr_stmt|;
name|fs
operator|=
name|cluster
operator|.
name|getFileSystem
argument_list|()
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
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Before
specifier|public
name|void
name|setUp
parameter_list|()
throws|throws
name|Exception
block|{
name|logManager
operator|=
operator|new
name|ReplicationHLogReaderManager
argument_list|(
name|fs
argument_list|,
name|conf
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|WALActionsListener
argument_list|>
name|listeners
init|=
operator|new
name|ArrayList
argument_list|<
name|WALActionsListener
argument_list|>
argument_list|()
decl_stmt|;
name|pathWatcher
operator|=
operator|new
name|PathWatcher
argument_list|()
expr_stmt|;
name|listeners
operator|.
name|add
argument_list|(
name|pathWatcher
argument_list|)
expr_stmt|;
name|log
operator|=
name|HLogFactory
operator|.
name|createHLog
argument_list|(
name|fs
argument_list|,
name|hbaseDir
argument_list|,
literal|"test"
argument_list|,
name|conf
argument_list|,
name|listeners
argument_list|,
literal|"some server"
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
name|log
operator|.
name|closeAndDelete
argument_list|()
expr_stmt|;
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
comment|// Grab the path that was generated when the log rolled as part of its creation
name|Path
name|path
init|=
name|pathWatcher
operator|.
name|currentPath
decl_stmt|;
comment|// open it, it's empty so it fails
try|try
block|{
name|logManager
operator|.
name|openReader
argument_list|(
name|path
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"Shouldn't be able to open an empty file"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|EOFException
name|ex
parameter_list|)
block|{}
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|logManager
operator|.
name|getPosition
argument_list|()
argument_list|)
expr_stmt|;
name|appendToLog
argument_list|()
expr_stmt|;
comment|// There's one edit in the log, read it. Reading past it needs to return nulls
name|assertNotNull
argument_list|(
name|logManager
operator|.
name|openReader
argument_list|(
name|path
argument_list|)
argument_list|)
expr_stmt|;
name|logManager
operator|.
name|seek
argument_list|()
expr_stmt|;
name|HLog
operator|.
name|Entry
index|[]
name|entriesArray
init|=
operator|new
name|HLog
operator|.
name|Entry
index|[
literal|1
index|]
decl_stmt|;
name|HLog
operator|.
name|Entry
name|entry
init|=
name|logManager
operator|.
name|readNextAndSetPosition
argument_list|(
name|entriesArray
argument_list|,
literal|0
argument_list|)
decl_stmt|;
name|assertNotNull
argument_list|(
name|entry
argument_list|)
expr_stmt|;
name|entry
operator|=
name|logManager
operator|.
name|readNextAndSetPosition
argument_list|(
name|entriesArray
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|entry
argument_list|)
expr_stmt|;
name|logManager
operator|.
name|closeReader
argument_list|()
expr_stmt|;
name|long
name|oldPos
init|=
name|logManager
operator|.
name|getPosition
argument_list|()
decl_stmt|;
name|appendToLog
argument_list|()
expr_stmt|;
comment|// Read the newly added entry, make sure we made progress
name|assertNotNull
argument_list|(
name|logManager
operator|.
name|openReader
argument_list|(
name|path
argument_list|)
argument_list|)
expr_stmt|;
name|logManager
operator|.
name|seek
argument_list|()
expr_stmt|;
name|entry
operator|=
name|logManager
operator|.
name|readNextAndSetPosition
argument_list|(
name|entriesArray
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|assertNotEquals
argument_list|(
name|oldPos
argument_list|,
name|logManager
operator|.
name|getPosition
argument_list|()
argument_list|)
expr_stmt|;
name|assertNotNull
argument_list|(
name|entry
argument_list|)
expr_stmt|;
name|logManager
operator|.
name|closeReader
argument_list|()
expr_stmt|;
name|oldPos
operator|=
name|logManager
operator|.
name|getPosition
argument_list|()
expr_stmt|;
name|log
operator|.
name|rollWriter
argument_list|()
expr_stmt|;
comment|// We rolled but we still should see the end of the first log and not get data
name|assertNotNull
argument_list|(
name|logManager
operator|.
name|openReader
argument_list|(
name|path
argument_list|)
argument_list|)
expr_stmt|;
name|logManager
operator|.
name|seek
argument_list|()
expr_stmt|;
name|entry
operator|=
name|logManager
operator|.
name|readNextAndSetPosition
argument_list|(
name|entriesArray
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|oldPos
argument_list|,
name|logManager
operator|.
name|getPosition
argument_list|()
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|entry
argument_list|)
expr_stmt|;
name|logManager
operator|.
name|finishCurrentFile
argument_list|()
expr_stmt|;
name|path
operator|=
name|pathWatcher
operator|.
name|currentPath
expr_stmt|;
comment|// Finally we have a new empty log, which should still give us EOFs
try|try
block|{
name|logManager
operator|.
name|openReader
argument_list|(
name|path
argument_list|)
expr_stmt|;
name|fail
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|EOFException
name|ex
parameter_list|)
block|{}
block|}
specifier|private
name|void
name|appendToLog
parameter_list|()
throws|throws
name|IOException
block|{
name|log
operator|.
name|append
argument_list|(
name|info
argument_list|,
name|tableName
argument_list|,
name|getWALEdit
argument_list|()
argument_list|,
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|,
name|htd
argument_list|)
expr_stmt|;
block|}
specifier|private
name|WALEdit
name|getWALEdit
parameter_list|()
block|{
name|WALEdit
name|edit
init|=
operator|new
name|WALEdit
argument_list|()
decl_stmt|;
name|edit
operator|.
name|add
argument_list|(
operator|new
name|KeyValue
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|)
argument_list|,
name|family
argument_list|,
name|qualifier
argument_list|,
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|,
name|qualifier
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|edit
return|;
block|}
class|class
name|PathWatcher
implements|implements
name|WALActionsListener
block|{
name|Path
name|currentPath
decl_stmt|;
annotation|@
name|Override
specifier|public
name|void
name|preLogRoll
parameter_list|(
name|Path
name|oldPath
parameter_list|,
name|Path
name|newPath
parameter_list|)
throws|throws
name|IOException
block|{
name|currentPath
operator|=
name|newPath
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|postLogRoll
parameter_list|(
name|Path
name|oldPath
parameter_list|,
name|Path
name|newPath
parameter_list|)
throws|throws
name|IOException
block|{}
annotation|@
name|Override
specifier|public
name|void
name|preLogArchive
parameter_list|(
name|Path
name|oldPath
parameter_list|,
name|Path
name|newPath
parameter_list|)
throws|throws
name|IOException
block|{}
annotation|@
name|Override
specifier|public
name|void
name|postLogArchive
parameter_list|(
name|Path
name|oldPath
parameter_list|,
name|Path
name|newPath
parameter_list|)
throws|throws
name|IOException
block|{}
annotation|@
name|Override
specifier|public
name|void
name|logRollRequested
parameter_list|()
block|{}
annotation|@
name|Override
specifier|public
name|void
name|logCloseRequested
parameter_list|()
block|{}
annotation|@
name|Override
specifier|public
name|void
name|visitLogEntryBeforeWrite
parameter_list|(
name|HRegionInfo
name|info
parameter_list|,
name|HLogKey
name|logKey
parameter_list|,
name|WALEdit
name|logEdit
parameter_list|)
block|{}
annotation|@
name|Override
specifier|public
name|void
name|visitLogEntryBeforeWrite
parameter_list|(
name|HTableDescriptor
name|htd
parameter_list|,
name|HLogKey
name|logKey
parameter_list|,
name|WALEdit
name|logEdit
parameter_list|)
block|{}
block|}
block|}
end_class

end_unit

