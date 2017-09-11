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
name|lang
operator|.
name|reflect
operator|.
name|Field
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
name|NavigableMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|TreeMap
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
name|ExecutorService
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
name|Executors
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
name|regionserver
operator|.
name|ChunkCreator
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
name|MemStoreLABImpl
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
name|util
operator|.
name|Threads
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
name|WALKey
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

begin_comment
comment|/**  * Provides FSHLog test cases.  */
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
name|TestFSHLog
extends|extends
name|AbstractTestFSWAL
block|{
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
name|Override
specifier|protected
name|AbstractFSWAL
argument_list|<
name|?
argument_list|>
name|newWAL
parameter_list|(
name|FileSystem
name|fs
parameter_list|,
name|Path
name|rootDir
parameter_list|,
name|String
name|walDir
parameter_list|,
name|String
name|archiveDir
parameter_list|,
name|Configuration
name|conf
parameter_list|,
name|List
argument_list|<
name|WALActionsListener
argument_list|>
name|listeners
parameter_list|,
name|boolean
name|failIfWALExists
parameter_list|,
name|String
name|prefix
parameter_list|,
name|String
name|suffix
parameter_list|)
throws|throws
name|IOException
block|{
return|return
operator|new
name|FSHLog
argument_list|(
name|fs
argument_list|,
name|rootDir
argument_list|,
name|walDir
argument_list|,
name|archiveDir
argument_list|,
name|conf
argument_list|,
name|listeners
argument_list|,
name|failIfWALExists
argument_list|,
name|prefix
argument_list|,
name|suffix
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|protected
name|AbstractFSWAL
argument_list|<
name|?
argument_list|>
name|newSlowWAL
parameter_list|(
name|FileSystem
name|fs
parameter_list|,
name|Path
name|rootDir
parameter_list|,
name|String
name|walDir
parameter_list|,
name|String
name|archiveDir
parameter_list|,
name|Configuration
name|conf
parameter_list|,
name|List
argument_list|<
name|WALActionsListener
argument_list|>
name|listeners
parameter_list|,
name|boolean
name|failIfWALExists
parameter_list|,
name|String
name|prefix
parameter_list|,
name|String
name|suffix
parameter_list|,
specifier|final
name|Runnable
name|action
parameter_list|)
throws|throws
name|IOException
block|{
return|return
operator|new
name|FSHLog
argument_list|(
name|fs
argument_list|,
name|rootDir
argument_list|,
name|walDir
argument_list|,
name|archiveDir
argument_list|,
name|conf
argument_list|,
name|listeners
argument_list|,
name|failIfWALExists
argument_list|,
name|prefix
argument_list|,
name|suffix
argument_list|)
block|{
annotation|@
name|Override
name|void
name|atHeadOfRingBufferEventHandlerAppend
parameter_list|()
block|{
name|action
operator|.
name|run
argument_list|()
expr_stmt|;
name|super
operator|.
name|atHeadOfRingBufferEventHandlerAppend
argument_list|()
expr_stmt|;
block|}
block|}
return|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testSyncRunnerIndexOverflow
parameter_list|()
throws|throws
name|IOException
throws|,
name|NoSuchFieldException
throws|,
name|SecurityException
throws|,
name|IllegalArgumentException
throws|,
name|IllegalAccessException
block|{
specifier|final
name|String
name|name
init|=
name|this
operator|.
name|name
operator|.
name|getMethodName
argument_list|()
decl_stmt|;
name|FSHLog
name|log
init|=
operator|new
name|FSHLog
argument_list|(
name|FS
argument_list|,
name|FSUtils
operator|.
name|getRootDir
argument_list|(
name|CONF
argument_list|)
argument_list|,
name|name
argument_list|,
name|HConstants
operator|.
name|HREGION_OLDLOGDIR_NAME
argument_list|,
name|CONF
argument_list|,
literal|null
argument_list|,
literal|true
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
decl_stmt|;
try|try
block|{
name|Field
name|ringBufferEventHandlerField
init|=
name|FSHLog
operator|.
name|class
operator|.
name|getDeclaredField
argument_list|(
literal|"ringBufferEventHandler"
argument_list|)
decl_stmt|;
name|ringBufferEventHandlerField
operator|.
name|setAccessible
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|FSHLog
operator|.
name|RingBufferEventHandler
name|ringBufferEventHandler
init|=
operator|(
name|FSHLog
operator|.
name|RingBufferEventHandler
operator|)
name|ringBufferEventHandlerField
operator|.
name|get
argument_list|(
name|log
argument_list|)
decl_stmt|;
name|Field
name|syncRunnerIndexField
init|=
name|FSHLog
operator|.
name|RingBufferEventHandler
operator|.
name|class
operator|.
name|getDeclaredField
argument_list|(
literal|"syncRunnerIndex"
argument_list|)
decl_stmt|;
name|syncRunnerIndexField
operator|.
name|setAccessible
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|syncRunnerIndexField
operator|.
name|set
argument_list|(
name|ringBufferEventHandler
argument_list|,
name|Integer
operator|.
name|MAX_VALUE
operator|-
literal|1
argument_list|)
expr_stmt|;
name|HTableDescriptor
name|htd
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
name|this
operator|.
name|name
operator|.
name|getMethodName
argument_list|()
argument_list|)
argument_list|)
operator|.
name|addFamily
argument_list|(
operator|new
name|HColumnDescriptor
argument_list|(
literal|"row"
argument_list|)
argument_list|)
decl_stmt|;
name|NavigableMap
argument_list|<
name|byte
index|[]
argument_list|,
name|Integer
argument_list|>
name|scopes
init|=
operator|new
name|TreeMap
argument_list|<>
argument_list|(
name|Bytes
operator|.
name|BYTES_COMPARATOR
argument_list|)
decl_stmt|;
for|for
control|(
name|byte
index|[]
name|fam
range|:
name|htd
operator|.
name|getFamiliesKeys
argument_list|()
control|)
block|{
name|scopes
operator|.
name|put
argument_list|(
name|fam
argument_list|,
literal|0
argument_list|)
expr_stmt|;
block|}
name|HRegionInfo
name|hri
init|=
operator|new
name|HRegionInfo
argument_list|(
name|htd
operator|.
name|getTableName
argument_list|()
argument_list|,
name|HConstants
operator|.
name|EMPTY_START_ROW
argument_list|,
name|HConstants
operator|.
name|EMPTY_END_ROW
argument_list|)
decl_stmt|;
name|MultiVersionConcurrencyControl
name|mvcc
init|=
operator|new
name|MultiVersionConcurrencyControl
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
literal|10
condition|;
name|i
operator|++
control|)
block|{
name|addEdits
argument_list|(
name|log
argument_list|,
name|hri
argument_list|,
name|htd
argument_list|,
literal|1
argument_list|,
name|mvcc
argument_list|,
name|scopes
argument_list|)
expr_stmt|;
block|}
block|}
finally|finally
block|{
name|log
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
comment|/**    * Test case for https://issues.apache.org/jira/browse/HBASE-16721    */
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|30000
argument_list|)
specifier|public
name|void
name|testUnflushedSeqIdTracking
parameter_list|()
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
specifier|final
name|String
name|name
init|=
name|this
operator|.
name|name
operator|.
name|getMethodName
argument_list|()
decl_stmt|;
specifier|final
name|byte
index|[]
name|b
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"b"
argument_list|)
decl_stmt|;
specifier|final
name|AtomicBoolean
name|startHoldingForAppend
init|=
operator|new
name|AtomicBoolean
argument_list|(
literal|false
argument_list|)
decl_stmt|;
specifier|final
name|CountDownLatch
name|holdAppend
init|=
operator|new
name|CountDownLatch
argument_list|(
literal|1
argument_list|)
decl_stmt|;
specifier|final
name|CountDownLatch
name|flushFinished
init|=
operator|new
name|CountDownLatch
argument_list|(
literal|1
argument_list|)
decl_stmt|;
specifier|final
name|CountDownLatch
name|putFinished
init|=
operator|new
name|CountDownLatch
argument_list|(
literal|1
argument_list|)
decl_stmt|;
try|try
init|(
name|FSHLog
name|log
init|=
operator|new
name|FSHLog
argument_list|(
name|FS
argument_list|,
name|FSUtils
operator|.
name|getRootDir
argument_list|(
name|CONF
argument_list|)
argument_list|,
name|name
argument_list|,
name|HConstants
operator|.
name|HREGION_OLDLOGDIR_NAME
argument_list|,
name|CONF
argument_list|,
literal|null
argument_list|,
literal|true
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
init|)
block|{
name|log
operator|.
name|registerWALActionsListener
argument_list|(
operator|new
name|WALActionsListener
operator|.
name|Base
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|visitLogEntryBeforeWrite
parameter_list|(
name|WALKey
name|logKey
parameter_list|,
name|WALEdit
name|logEdit
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|startHoldingForAppend
operator|.
name|get
argument_list|()
condition|)
block|{
try|try
block|{
name|holdAppend
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
block|{
name|LOG
operator|.
name|error
argument_list|(
name|e
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
argument_list|)
expr_stmt|;
comment|// open a new region which uses this WAL
name|HTableDescriptor
name|htd
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
name|this
operator|.
name|name
operator|.
name|getMethodName
argument_list|()
argument_list|)
argument_list|)
operator|.
name|addFamily
argument_list|(
operator|new
name|HColumnDescriptor
argument_list|(
name|b
argument_list|)
argument_list|)
decl_stmt|;
name|HRegionInfo
name|hri
init|=
operator|new
name|HRegionInfo
argument_list|(
name|htd
operator|.
name|getTableName
argument_list|()
argument_list|,
name|HConstants
operator|.
name|EMPTY_START_ROW
argument_list|,
name|HConstants
operator|.
name|EMPTY_END_ROW
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
name|HRegion
name|region
init|=
name|TEST_UTIL
operator|.
name|createLocalHRegion
argument_list|(
name|hri
argument_list|,
name|htd
argument_list|,
name|log
argument_list|)
decl_stmt|;
name|ExecutorService
name|exec
init|=
name|Executors
operator|.
name|newFixedThreadPool
argument_list|(
literal|2
argument_list|)
decl_stmt|;
comment|// do a regular write first because of memstore size calculation.
name|region
operator|.
name|put
argument_list|(
operator|new
name|Put
argument_list|(
name|b
argument_list|)
operator|.
name|addColumn
argument_list|(
name|b
argument_list|,
name|b
argument_list|,
name|b
argument_list|)
argument_list|)
expr_stmt|;
name|startHoldingForAppend
operator|.
name|set
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|exec
operator|.
name|submit
argument_list|(
operator|new
name|Runnable
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
try|try
block|{
name|region
operator|.
name|put
argument_list|(
operator|new
name|Put
argument_list|(
name|b
argument_list|)
operator|.
name|addColumn
argument_list|(
name|b
argument_list|,
name|b
argument_list|,
name|b
argument_list|)
argument_list|)
expr_stmt|;
name|putFinished
operator|.
name|countDown
argument_list|()
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
name|error
argument_list|(
name|e
argument_list|)
expr_stmt|;
block|}
block|}
block|}
argument_list|)
expr_stmt|;
comment|// give the put a chance to start
name|Threads
operator|.
name|sleep
argument_list|(
literal|3000
argument_list|)
expr_stmt|;
name|exec
operator|.
name|submit
argument_list|(
operator|new
name|Runnable
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
try|try
block|{
name|Region
operator|.
name|FlushResult
name|flushResult
init|=
name|region
operator|.
name|flush
argument_list|(
literal|true
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Flush result:"
operator|+
name|flushResult
operator|.
name|getResult
argument_list|()
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Flush succeeded:"
operator|+
name|flushResult
operator|.
name|isFlushSucceeded
argument_list|()
argument_list|)
expr_stmt|;
name|flushFinished
operator|.
name|countDown
argument_list|()
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
name|error
argument_list|(
name|e
argument_list|)
expr_stmt|;
block|}
block|}
block|}
argument_list|)
expr_stmt|;
comment|// give the flush a chance to start. Flush should have got the region lock, and
comment|// should have been waiting on the mvcc complete after this.
name|Threads
operator|.
name|sleep
argument_list|(
literal|3000
argument_list|)
expr_stmt|;
comment|// let the append to WAL go through now that the flush already started
name|holdAppend
operator|.
name|countDown
argument_list|()
expr_stmt|;
name|putFinished
operator|.
name|await
argument_list|()
expr_stmt|;
name|flushFinished
operator|.
name|await
argument_list|()
expr_stmt|;
comment|// check whether flush went through
name|assertEquals
argument_list|(
literal|"Region did not flush?"
argument_list|,
literal|1
argument_list|,
name|region
operator|.
name|getStoreFileList
argument_list|(
operator|new
name|byte
index|[]
index|[]
block|{
name|b
block|}
argument_list|)
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
comment|// now check the region's unflushed seqIds.
name|long
name|seqId
init|=
name|log
operator|.
name|getEarliestMemstoreSeqNum
argument_list|(
name|hri
operator|.
name|getEncodedNameAsBytes
argument_list|()
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"Found seqId for the region which is already flushed"
argument_list|,
name|HConstants
operator|.
name|NO_SEQNUM
argument_list|,
name|seqId
argument_list|)
expr_stmt|;
name|region
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

