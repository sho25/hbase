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
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|wal
operator|.
name|WALSplitUtil
operator|.
name|getCompletedRecoveredEditsFilePath
import|;
end_import

begin_import
import|import static
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
operator|.
name|getRegionSplitEditsPath
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

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
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
name|ConcurrentHashMap
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
name|ConcurrentMap
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
name|CellUtil
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
name|log
operator|.
name|HBaseMarkers
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
name|ipc
operator|.
name|RemoteException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|yetus
operator|.
name|audience
operator|.
name|InterfaceAudience
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
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|collections4
operator|.
name|MapUtils
import|;
end_import

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|abstract
class|class
name|AbstractRecoveredEditsOutputSink
extends|extends
name|OutputSink
block|{
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
name|RecoveredEditsOutputSink
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|WALSplitter
name|walSplitter
decl_stmt|;
specifier|private
specifier|final
name|ConcurrentMap
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
name|regionMaximumEditLogSeqNum
init|=
operator|new
name|ConcurrentHashMap
argument_list|<>
argument_list|()
decl_stmt|;
specifier|public
name|AbstractRecoveredEditsOutputSink
parameter_list|(
name|WALSplitter
name|walSplitter
parameter_list|,
name|WALSplitter
operator|.
name|PipelineController
name|controller
parameter_list|,
name|EntryBuffers
name|entryBuffers
parameter_list|,
name|int
name|numWriters
parameter_list|)
block|{
name|super
argument_list|(
name|controller
argument_list|,
name|entryBuffers
argument_list|,
name|numWriters
argument_list|)
expr_stmt|;
name|this
operator|.
name|walSplitter
operator|=
name|walSplitter
expr_stmt|;
block|}
comment|/**    * @return a writer that wraps a {@link WALProvider.Writer} and its Path. Caller should close.    */
specifier|protected
name|RecoveredEditsWriter
name|createRecoveredEditsWriter
parameter_list|(
name|TableName
name|tableName
parameter_list|,
name|byte
index|[]
name|region
parameter_list|,
name|long
name|seqId
parameter_list|)
throws|throws
name|IOException
block|{
name|Path
name|regionEditsPath
init|=
name|getRegionSplitEditsPath
argument_list|(
name|tableName
argument_list|,
name|region
argument_list|,
name|seqId
argument_list|,
name|walSplitter
operator|.
name|getFileBeingSplit
argument_list|()
operator|.
name|getPath
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|,
name|walSplitter
operator|.
name|getTmpDirName
argument_list|()
argument_list|,
name|walSplitter
operator|.
name|conf
argument_list|)
decl_stmt|;
if|if
condition|(
name|walSplitter
operator|.
name|walFS
operator|.
name|exists
argument_list|(
name|regionEditsPath
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Found old edits file. It could be the "
operator|+
literal|"result of a previous failed split attempt. Deleting "
operator|+
name|regionEditsPath
operator|+
literal|", length="
operator|+
name|walSplitter
operator|.
name|walFS
operator|.
name|getFileStatus
argument_list|(
name|regionEditsPath
argument_list|)
operator|.
name|getLen
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|walSplitter
operator|.
name|walFS
operator|.
name|delete
argument_list|(
name|regionEditsPath
argument_list|,
literal|false
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Failed delete of old {}"
argument_list|,
name|regionEditsPath
argument_list|)
expr_stmt|;
block|}
block|}
name|WALProvider
operator|.
name|Writer
name|w
init|=
name|walSplitter
operator|.
name|createWriter
argument_list|(
name|regionEditsPath
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Creating recovered edits writer path={}"
argument_list|,
name|regionEditsPath
argument_list|)
expr_stmt|;
return|return
operator|new
name|RecoveredEditsWriter
argument_list|(
name|region
argument_list|,
name|regionEditsPath
argument_list|,
name|w
argument_list|,
name|seqId
argument_list|)
return|;
block|}
specifier|protected
name|Path
name|closeRecoveredEditsWriter
parameter_list|(
name|RecoveredEditsWriter
name|editsWriter
parameter_list|,
name|List
argument_list|<
name|IOException
argument_list|>
name|thrown
parameter_list|)
throws|throws
name|IOException
block|{
try|try
block|{
name|editsWriter
operator|.
name|writer
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|ioe
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Could not close recovered edits at {}"
argument_list|,
name|editsWriter
operator|.
name|path
argument_list|,
name|ioe
argument_list|)
expr_stmt|;
name|thrown
operator|.
name|add
argument_list|(
name|ioe
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"Closed recovered edits writer path={} (wrote {} edits, skipped {} edits in {} ms"
argument_list|,
name|editsWriter
operator|.
name|path
argument_list|,
name|editsWriter
operator|.
name|editsWritten
argument_list|,
name|editsWriter
operator|.
name|editsSkipped
argument_list|,
name|editsWriter
operator|.
name|nanosSpent
operator|/
literal|1000
operator|/
literal|1000
argument_list|)
expr_stmt|;
if|if
condition|(
name|editsWriter
operator|.
name|editsWritten
operator|==
literal|0
condition|)
block|{
comment|// just remove the empty recovered.edits file
if|if
condition|(
name|walSplitter
operator|.
name|walFS
operator|.
name|exists
argument_list|(
name|editsWriter
operator|.
name|path
argument_list|)
operator|&&
operator|!
name|walSplitter
operator|.
name|walFS
operator|.
name|delete
argument_list|(
name|editsWriter
operator|.
name|path
argument_list|,
literal|false
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Failed deleting empty {}"
argument_list|,
name|editsWriter
operator|.
name|path
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Failed deleting empty  "
operator|+
name|editsWriter
operator|.
name|path
argument_list|)
throw|;
block|}
return|return
literal|null
return|;
block|}
name|Path
name|dst
init|=
name|getCompletedRecoveredEditsFilePath
argument_list|(
name|editsWriter
operator|.
name|path
argument_list|,
name|regionMaximumEditLogSeqNum
operator|.
name|get
argument_list|(
name|Bytes
operator|.
name|toString
argument_list|(
name|editsWriter
operator|.
name|encodedRegionName
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
try|try
block|{
if|if
condition|(
operator|!
name|dst
operator|.
name|equals
argument_list|(
name|editsWriter
operator|.
name|path
argument_list|)
operator|&&
name|walSplitter
operator|.
name|walFS
operator|.
name|exists
argument_list|(
name|dst
argument_list|)
condition|)
block|{
name|deleteOneWithFewerEntries
argument_list|(
name|editsWriter
argument_list|,
name|dst
argument_list|)
expr_stmt|;
block|}
comment|// Skip the unit tests which create a splitter that reads and
comment|// writes the data without touching disk.
comment|// TestHLogSplit#testThreading is an example.
if|if
condition|(
name|walSplitter
operator|.
name|walFS
operator|.
name|exists
argument_list|(
name|editsWriter
operator|.
name|path
argument_list|)
condition|)
block|{
if|if
condition|(
operator|!
name|walSplitter
operator|.
name|walFS
operator|.
name|rename
argument_list|(
name|editsWriter
operator|.
name|path
argument_list|,
name|dst
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Failed renaming recovered edits "
operator|+
name|editsWriter
operator|.
name|path
operator|+
literal|" to "
operator|+
name|dst
argument_list|)
throw|;
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"Rename recovered edits {} to {}"
argument_list|,
name|editsWriter
operator|.
name|path
argument_list|,
name|dst
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|ioe
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Could not rename recovered edits {} to {}"
argument_list|,
name|editsWriter
operator|.
name|path
argument_list|,
name|dst
argument_list|,
name|ioe
argument_list|)
expr_stmt|;
name|thrown
operator|.
name|add
argument_list|(
name|ioe
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
return|return
name|dst
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|keepRegionEvent
parameter_list|(
name|WAL
operator|.
name|Entry
name|entry
parameter_list|)
block|{
name|ArrayList
argument_list|<
name|Cell
argument_list|>
name|cells
init|=
name|entry
operator|.
name|getEdit
argument_list|()
operator|.
name|getCells
argument_list|()
decl_stmt|;
for|for
control|(
name|Cell
name|cell
range|:
name|cells
control|)
block|{
if|if
condition|(
name|WALEdit
operator|.
name|isCompactionMarker
argument_list|(
name|cell
argument_list|)
condition|)
block|{
return|return
literal|true
return|;
block|}
block|}
return|return
literal|false
return|;
block|}
comment|/**    * Update region's maximum edit log SeqNum.    */
name|void
name|updateRegionMaximumEditLogSeqNum
parameter_list|(
name|WAL
operator|.
name|Entry
name|entry
parameter_list|)
block|{
synchronized|synchronized
init|(
name|regionMaximumEditLogSeqNum
init|)
block|{
name|String
name|regionName
init|=
name|Bytes
operator|.
name|toString
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
operator|.
name|getEncodedRegionName
argument_list|()
argument_list|)
decl_stmt|;
name|Long
name|currentMaxSeqNum
init|=
name|regionMaximumEditLogSeqNum
operator|.
name|get
argument_list|(
name|regionName
argument_list|)
decl_stmt|;
if|if
condition|(
name|currentMaxSeqNum
operator|==
literal|null
operator|||
name|entry
operator|.
name|getKey
argument_list|()
operator|.
name|getSequenceId
argument_list|()
operator|>
name|currentMaxSeqNum
condition|)
block|{
name|regionMaximumEditLogSeqNum
operator|.
name|put
argument_list|(
name|regionName
argument_list|,
name|entry
operator|.
name|getKey
argument_list|()
operator|.
name|getSequenceId
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|// delete the one with fewer wal entries
specifier|private
name|void
name|deleteOneWithFewerEntries
parameter_list|(
name|RecoveredEditsWriter
name|editsWriter
parameter_list|,
name|Path
name|dst
parameter_list|)
throws|throws
name|IOException
block|{
name|long
name|dstMinLogSeqNum
init|=
operator|-
literal|1L
decl_stmt|;
try|try
init|(
name|WAL
operator|.
name|Reader
name|reader
init|=
name|walSplitter
operator|.
name|getWalFactory
argument_list|()
operator|.
name|createReader
argument_list|(
name|walSplitter
operator|.
name|walFS
argument_list|,
name|dst
argument_list|)
init|)
block|{
name|WAL
operator|.
name|Entry
name|entry
init|=
name|reader
operator|.
name|next
argument_list|()
decl_stmt|;
if|if
condition|(
name|entry
operator|!=
literal|null
condition|)
block|{
name|dstMinLogSeqNum
operator|=
name|entry
operator|.
name|getKey
argument_list|()
operator|.
name|getSequenceId
argument_list|()
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|EOFException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Got EOF when reading first WAL entry from {}, an empty or broken WAL file?"
argument_list|,
name|dst
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|editsWriter
operator|.
name|minLogSeqNum
operator|<
name|dstMinLogSeqNum
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Found existing old edits file. It could be the result of a previous failed"
operator|+
literal|" split attempt or we have duplicated wal entries. Deleting "
operator|+
name|dst
operator|+
literal|", length="
operator|+
name|walSplitter
operator|.
name|walFS
operator|.
name|getFileStatus
argument_list|(
name|dst
argument_list|)
operator|.
name|getLen
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|walSplitter
operator|.
name|walFS
operator|.
name|delete
argument_list|(
name|dst
argument_list|,
literal|false
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Failed deleting of old {}"
argument_list|,
name|dst
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Failed deleting of old "
operator|+
name|dst
argument_list|)
throw|;
block|}
block|}
else|else
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Found existing old edits file and we have less entries. Deleting "
operator|+
name|editsWriter
operator|.
name|path
operator|+
literal|", length="
operator|+
name|walSplitter
operator|.
name|walFS
operator|.
name|getFileStatus
argument_list|(
name|editsWriter
operator|.
name|path
argument_list|)
operator|.
name|getLen
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|walSplitter
operator|.
name|walFS
operator|.
name|delete
argument_list|(
name|editsWriter
operator|.
name|path
argument_list|,
literal|false
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Failed deleting of {}"
argument_list|,
name|editsWriter
operator|.
name|path
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Failed deleting of "
operator|+
name|editsWriter
operator|.
name|path
argument_list|)
throw|;
block|}
block|}
block|}
comment|/**    * Private data structure that wraps a {@link WALProvider.Writer} and its Path, also collecting    * statistics about the data written to this output.    */
specifier|final
class|class
name|RecoveredEditsWriter
block|{
comment|/* Count of edits written to this path */
name|long
name|editsWritten
init|=
literal|0
decl_stmt|;
comment|/* Count of edits skipped to this path */
name|long
name|editsSkipped
init|=
literal|0
decl_stmt|;
comment|/* Number of nanos spent writing to this log */
name|long
name|nanosSpent
init|=
literal|0
decl_stmt|;
specifier|final
name|byte
index|[]
name|encodedRegionName
decl_stmt|;
specifier|final
name|Path
name|path
decl_stmt|;
specifier|final
name|WALProvider
operator|.
name|Writer
name|writer
decl_stmt|;
specifier|final
name|long
name|minLogSeqNum
decl_stmt|;
name|RecoveredEditsWriter
parameter_list|(
name|byte
index|[]
name|encodedRegionName
parameter_list|,
name|Path
name|path
parameter_list|,
name|WALProvider
operator|.
name|Writer
name|writer
parameter_list|,
name|long
name|minLogSeqNum
parameter_list|)
block|{
name|this
operator|.
name|encodedRegionName
operator|=
name|encodedRegionName
expr_stmt|;
name|this
operator|.
name|path
operator|=
name|path
expr_stmt|;
name|this
operator|.
name|writer
operator|=
name|writer
expr_stmt|;
name|this
operator|.
name|minLogSeqNum
operator|=
name|minLogSeqNum
expr_stmt|;
block|}
specifier|private
name|void
name|incrementEdits
parameter_list|(
name|int
name|edits
parameter_list|)
block|{
name|editsWritten
operator|+=
name|edits
expr_stmt|;
block|}
specifier|private
name|void
name|incrementSkippedEdits
parameter_list|(
name|int
name|skipped
parameter_list|)
block|{
name|editsSkipped
operator|+=
name|skipped
expr_stmt|;
name|totalSkippedEdits
operator|.
name|addAndGet
argument_list|(
name|skipped
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|incrementNanoTime
parameter_list|(
name|long
name|nanos
parameter_list|)
block|{
name|nanosSpent
operator|+=
name|nanos
expr_stmt|;
block|}
name|void
name|writeRegionEntries
parameter_list|(
name|List
argument_list|<
name|WAL
operator|.
name|Entry
argument_list|>
name|entries
parameter_list|)
throws|throws
name|IOException
block|{
name|long
name|startTime
init|=
name|System
operator|.
name|nanoTime
argument_list|()
decl_stmt|;
try|try
block|{
name|int
name|editsCount
init|=
literal|0
decl_stmt|;
for|for
control|(
name|WAL
operator|.
name|Entry
name|logEntry
range|:
name|entries
control|)
block|{
name|filterCellByStore
argument_list|(
name|logEntry
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|logEntry
operator|.
name|getEdit
argument_list|()
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|writer
operator|.
name|append
argument_list|(
name|logEntry
argument_list|)
expr_stmt|;
name|updateRegionMaximumEditLogSeqNum
argument_list|(
name|logEntry
argument_list|)
expr_stmt|;
name|editsCount
operator|++
expr_stmt|;
block|}
else|else
block|{
name|incrementSkippedEdits
argument_list|(
literal|1
argument_list|)
expr_stmt|;
block|}
block|}
comment|// Pass along summary statistics
name|incrementEdits
argument_list|(
name|editsCount
argument_list|)
expr_stmt|;
name|incrementNanoTime
argument_list|(
name|System
operator|.
name|nanoTime
argument_list|()
operator|-
name|startTime
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|e
operator|=
name|e
operator|instanceof
name|RemoteException
condition|?
operator|(
operator|(
name|RemoteException
operator|)
name|e
operator|)
operator|.
name|unwrapRemoteException
argument_list|()
else|:
name|e
expr_stmt|;
name|LOG
operator|.
name|error
argument_list|(
name|HBaseMarkers
operator|.
name|FATAL
argument_list|,
literal|"Got while writing log entry to log"
argument_list|,
name|e
argument_list|)
expr_stmt|;
throw|throw
name|e
throw|;
block|}
block|}
specifier|private
name|void
name|filterCellByStore
parameter_list|(
name|WAL
operator|.
name|Entry
name|logEntry
parameter_list|)
block|{
name|Map
argument_list|<
name|byte
index|[]
argument_list|,
name|Long
argument_list|>
name|maxSeqIdInStores
init|=
name|walSplitter
operator|.
name|getRegionMaxSeqIdInStores
argument_list|()
operator|.
name|get
argument_list|(
name|Bytes
operator|.
name|toString
argument_list|(
name|logEntry
operator|.
name|getKey
argument_list|()
operator|.
name|getEncodedRegionName
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|MapUtils
operator|.
name|isEmpty
argument_list|(
name|maxSeqIdInStores
argument_list|)
condition|)
block|{
return|return;
block|}
comment|// Create the array list for the cells that aren't filtered.
comment|// We make the assumption that most cells will be kept.
name|ArrayList
argument_list|<
name|Cell
argument_list|>
name|keptCells
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|logEntry
operator|.
name|getEdit
argument_list|()
operator|.
name|getCells
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|Cell
name|cell
range|:
name|logEntry
operator|.
name|getEdit
argument_list|()
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
name|keptCells
operator|.
name|add
argument_list|(
name|cell
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|byte
index|[]
name|family
init|=
name|CellUtil
operator|.
name|cloneFamily
argument_list|(
name|cell
argument_list|)
decl_stmt|;
name|Long
name|maxSeqId
init|=
name|maxSeqIdInStores
operator|.
name|get
argument_list|(
name|family
argument_list|)
decl_stmt|;
comment|// Do not skip cell even if maxSeqId is null. Maybe we are in a rolling upgrade,
comment|// or the master was crashed before and we can not get the information.
if|if
condition|(
name|maxSeqId
operator|==
literal|null
operator|||
name|maxSeqId
operator|.
name|longValue
argument_list|()
operator|<
name|logEntry
operator|.
name|getKey
argument_list|()
operator|.
name|getSequenceId
argument_list|()
condition|)
block|{
name|keptCells
operator|.
name|add
argument_list|(
name|cell
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|// Anything in the keptCells array list is still live.
comment|// So rather than removing the cells from the array list
comment|// which would be an O(n^2) operation, we just replace the list
name|logEntry
operator|.
name|getEdit
argument_list|()
operator|.
name|setCells
argument_list|(
name|keptCells
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

