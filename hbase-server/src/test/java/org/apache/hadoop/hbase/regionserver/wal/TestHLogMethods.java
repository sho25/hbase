begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|wal
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
name|*
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
name|NavigableSet
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
name|*
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
name|ZooKeeperProtos
operator|.
name|SplitLogTask
operator|.
name|RecoveryMode
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
name|HLogSplitter
operator|.
name|EntryBuffers
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
name|HLogSplitter
operator|.
name|PipelineController
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
name|HLogSplitter
operator|.
name|RegionEntryBuffer
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
name|Bytes
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
comment|/**  * Simple testing of a few HLog methods.  */
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
name|SmallTests
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|TestHLogMethods
block|{
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|TEST_REGION
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"test_region"
argument_list|)
decl_stmt|;
empty_stmt|;
specifier|private
specifier|static
specifier|final
name|TableName
name|TEST_TABLE
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"test_table"
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|HBaseTestingUtility
name|util
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
comment|/**    * Assert that getSplitEditFilesSorted returns files in expected order and    * that it skips moved-aside files.    * @throws IOException    */
annotation|@
name|Test
specifier|public
name|void
name|testGetSplitEditFilesSorted
parameter_list|()
throws|throws
name|IOException
block|{
name|FileSystem
name|fs
init|=
name|FileSystem
operator|.
name|get
argument_list|(
name|util
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
name|Path
name|regiondir
init|=
name|util
operator|.
name|getDataTestDir
argument_list|(
literal|"regiondir"
argument_list|)
decl_stmt|;
name|fs
operator|.
name|delete
argument_list|(
name|regiondir
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|fs
operator|.
name|mkdirs
argument_list|(
name|regiondir
argument_list|)
expr_stmt|;
name|Path
name|recoverededits
init|=
name|HLogUtil
operator|.
name|getRegionDirRecoveredEditsDir
argument_list|(
name|regiondir
argument_list|)
decl_stmt|;
name|String
name|first
init|=
name|HLogSplitter
operator|.
name|formatRecoveredEditsFileName
argument_list|(
operator|-
literal|1
argument_list|)
decl_stmt|;
name|createFile
argument_list|(
name|fs
argument_list|,
name|recoverededits
argument_list|,
name|first
argument_list|)
expr_stmt|;
name|createFile
argument_list|(
name|fs
argument_list|,
name|recoverededits
argument_list|,
name|HLogSplitter
operator|.
name|formatRecoveredEditsFileName
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|createFile
argument_list|(
name|fs
argument_list|,
name|recoverededits
argument_list|,
name|HLogSplitter
operator|.
name|formatRecoveredEditsFileName
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|createFile
argument_list|(
name|fs
argument_list|,
name|recoverededits
argument_list|,
name|HLogSplitter
operator|.
name|formatRecoveredEditsFileName
argument_list|(
literal|11
argument_list|)
argument_list|)
expr_stmt|;
name|createFile
argument_list|(
name|fs
argument_list|,
name|recoverededits
argument_list|,
name|HLogSplitter
operator|.
name|formatRecoveredEditsFileName
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|createFile
argument_list|(
name|fs
argument_list|,
name|recoverededits
argument_list|,
name|HLogSplitter
operator|.
name|formatRecoveredEditsFileName
argument_list|(
literal|50
argument_list|)
argument_list|)
expr_stmt|;
name|String
name|last
init|=
name|HLogSplitter
operator|.
name|formatRecoveredEditsFileName
argument_list|(
name|Long
operator|.
name|MAX_VALUE
argument_list|)
decl_stmt|;
name|createFile
argument_list|(
name|fs
argument_list|,
name|recoverededits
argument_list|,
name|last
argument_list|)
expr_stmt|;
name|createFile
argument_list|(
name|fs
argument_list|,
name|recoverededits
argument_list|,
name|Long
operator|.
name|toString
argument_list|(
name|Long
operator|.
name|MAX_VALUE
argument_list|)
operator|+
literal|"."
operator|+
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|)
expr_stmt|;
name|HLogFactory
operator|.
name|createHLog
argument_list|(
name|fs
argument_list|,
name|regiondir
argument_list|,
literal|"dummyLogName"
argument_list|,
name|util
operator|.
name|getConfiguration
argument_list|()
argument_list|)
expr_stmt|;
name|NavigableSet
argument_list|<
name|Path
argument_list|>
name|files
init|=
name|HLogUtil
operator|.
name|getSplitEditFilesSorted
argument_list|(
name|fs
argument_list|,
name|regiondir
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|7
argument_list|,
name|files
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|files
operator|.
name|pollFirst
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|,
name|first
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|files
operator|.
name|pollLast
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|,
name|last
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|files
operator|.
name|pollFirst
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|,
name|HLogSplitter
operator|.
name|formatRecoveredEditsFileName
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|files
operator|.
name|pollFirst
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|,
name|HLogSplitter
operator|.
name|formatRecoveredEditsFileName
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|files
operator|.
name|pollFirst
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|,
name|HLogSplitter
operator|.
name|formatRecoveredEditsFileName
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|files
operator|.
name|pollFirst
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|,
name|HLogSplitter
operator|.
name|formatRecoveredEditsFileName
argument_list|(
literal|11
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|createFile
parameter_list|(
specifier|final
name|FileSystem
name|fs
parameter_list|,
specifier|final
name|Path
name|testdir
parameter_list|,
specifier|final
name|String
name|name
parameter_list|)
throws|throws
name|IOException
block|{
name|FSDataOutputStream
name|fdos
init|=
name|fs
operator|.
name|create
argument_list|(
operator|new
name|Path
argument_list|(
name|testdir
argument_list|,
name|name
argument_list|)
argument_list|,
literal|true
argument_list|)
decl_stmt|;
name|fdos
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testRegionEntryBuffer
parameter_list|()
throws|throws
name|Exception
block|{
name|HLogSplitter
operator|.
name|RegionEntryBuffer
name|reb
init|=
operator|new
name|HLogSplitter
operator|.
name|RegionEntryBuffer
argument_list|(
name|TEST_TABLE
argument_list|,
name|TEST_REGION
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|reb
operator|.
name|heapSize
argument_list|()
argument_list|)
expr_stmt|;
name|reb
operator|.
name|appendEntry
argument_list|(
name|createTestLogEntry
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|reb
operator|.
name|heapSize
argument_list|()
operator|>
literal|0
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testEntrySink
parameter_list|()
throws|throws
name|Exception
block|{
name|Configuration
name|conf
init|=
operator|new
name|Configuration
argument_list|()
decl_stmt|;
name|RecoveryMode
name|mode
init|=
operator|(
name|conf
operator|.
name|getBoolean
argument_list|(
name|HConstants
operator|.
name|DISTRIBUTED_LOG_REPLAY_KEY
argument_list|,
literal|false
argument_list|)
condition|?
name|RecoveryMode
operator|.
name|LOG_REPLAY
else|:
name|RecoveryMode
operator|.
name|LOG_SPLITTING
operator|)
decl_stmt|;
name|EntryBuffers
name|sink
init|=
operator|new
name|EntryBuffers
argument_list|(
operator|new
name|PipelineController
argument_list|()
argument_list|,
literal|1
operator|*
literal|1024
operator|*
literal|1024
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
literal|1000
condition|;
name|i
operator|++
control|)
block|{
name|HLog
operator|.
name|Entry
name|entry
init|=
name|createTestLogEntry
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|sink
operator|.
name|appendEntry
argument_list|(
name|entry
argument_list|)
expr_stmt|;
block|}
name|assertTrue
argument_list|(
name|sink
operator|.
name|totalBuffered
operator|>
literal|0
argument_list|)
expr_stmt|;
name|long
name|amountInChunk
init|=
name|sink
operator|.
name|totalBuffered
decl_stmt|;
comment|// Get a chunk
name|RegionEntryBuffer
name|chunk
init|=
name|sink
operator|.
name|getChunkToWrite
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
name|chunk
operator|.
name|heapSize
argument_list|()
argument_list|,
name|amountInChunk
argument_list|)
expr_stmt|;
comment|// Make sure it got marked that a thread is "working on this"
name|assertTrue
argument_list|(
name|sink
operator|.
name|isRegionCurrentlyWriting
argument_list|(
name|TEST_REGION
argument_list|)
argument_list|)
expr_stmt|;
comment|// Insert some more entries
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
literal|500
condition|;
name|i
operator|++
control|)
block|{
name|HLog
operator|.
name|Entry
name|entry
init|=
name|createTestLogEntry
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|sink
operator|.
name|appendEntry
argument_list|(
name|entry
argument_list|)
expr_stmt|;
block|}
comment|// Asking for another chunk shouldn't work since the first one
comment|// is still writing
name|assertNull
argument_list|(
name|sink
operator|.
name|getChunkToWrite
argument_list|()
argument_list|)
expr_stmt|;
comment|// If we say we're done writing the first chunk, then we should be able
comment|// to get the second
name|sink
operator|.
name|doneWriting
argument_list|(
name|chunk
argument_list|)
expr_stmt|;
name|RegionEntryBuffer
name|chunk2
init|=
name|sink
operator|.
name|getChunkToWrite
argument_list|()
decl_stmt|;
name|assertNotNull
argument_list|(
name|chunk2
argument_list|)
expr_stmt|;
name|assertNotSame
argument_list|(
name|chunk
argument_list|,
name|chunk2
argument_list|)
expr_stmt|;
name|long
name|amountInChunk2
init|=
name|sink
operator|.
name|totalBuffered
decl_stmt|;
comment|// The second chunk had fewer rows than the first
name|assertTrue
argument_list|(
name|amountInChunk2
operator|<
name|amountInChunk
argument_list|)
expr_stmt|;
name|sink
operator|.
name|doneWriting
argument_list|(
name|chunk2
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|sink
operator|.
name|totalBuffered
argument_list|)
expr_stmt|;
block|}
specifier|private
name|HLog
operator|.
name|Entry
name|createTestLogEntry
parameter_list|(
name|int
name|i
parameter_list|)
block|{
name|long
name|seq
init|=
name|i
decl_stmt|;
name|long
name|now
init|=
name|i
operator|*
literal|1000
decl_stmt|;
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
name|KeyValueTestUtil
operator|.
name|create
argument_list|(
literal|"row"
argument_list|,
literal|"fam"
argument_list|,
literal|"qual"
argument_list|,
literal|1234
argument_list|,
literal|"val"
argument_list|)
argument_list|)
expr_stmt|;
name|HLogKey
name|key
init|=
operator|new
name|HLogKey
argument_list|(
name|TEST_REGION
argument_list|,
name|TEST_TABLE
argument_list|,
name|seq
argument_list|,
name|now
argument_list|,
name|HConstants
operator|.
name|DEFAULT_CLUSTER_ID
argument_list|)
decl_stmt|;
name|HLog
operator|.
name|Entry
name|entry
init|=
operator|new
name|HLog
operator|.
name|Entry
argument_list|(
name|key
argument_list|,
name|edit
argument_list|)
decl_stmt|;
return|return
name|entry
return|;
block|}
block|}
end_class

end_unit

