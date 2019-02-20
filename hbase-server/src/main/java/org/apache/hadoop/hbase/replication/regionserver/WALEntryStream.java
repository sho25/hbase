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
name|replication
operator|.
name|regionserver
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|Closeable
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|FileNotFoundException
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
name|OptionalLong
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
name|PriorityBlockingQueue
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
name|util
operator|.
name|CancelableProgressable
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
name|CommonFSUtils
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
name|LeaseNotRecoveredException
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
operator|.
name|Entry
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
operator|.
name|Reader
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
name|apache
operator|.
name|yetus
operator|.
name|audience
operator|.
name|InterfaceStability
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
comment|/**  * Streaming access to WAL entries. This class is given a queue of WAL {@link Path}, and continually  * iterates through all the WAL {@link Entry} in the queue. When it's done reading from a Path, it  * dequeues it and starts reading from the next.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
annotation|@
name|InterfaceStability
operator|.
name|Evolving
class|class
name|WALEntryStream
implements|implements
name|Closeable
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
name|WALEntryStream
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|Reader
name|reader
decl_stmt|;
specifier|private
name|Path
name|currentPath
decl_stmt|;
comment|// cache of next entry for hasNext()
specifier|private
name|Entry
name|currentEntry
decl_stmt|;
comment|// position for the current entry. As now we support peek, which means that the upper layer may
comment|// choose to return before reading the current entry, so it is not safe to return the value below
comment|// in getPosition.
specifier|private
name|long
name|currentPositionOfEntry
init|=
literal|0
decl_stmt|;
comment|// position after reading current entry
specifier|private
name|long
name|currentPositionOfReader
init|=
literal|0
decl_stmt|;
specifier|private
specifier|final
name|PriorityBlockingQueue
argument_list|<
name|Path
argument_list|>
name|logQueue
decl_stmt|;
specifier|private
specifier|final
name|FileSystem
name|fs
decl_stmt|;
specifier|private
specifier|final
name|Configuration
name|conf
decl_stmt|;
specifier|private
specifier|final
name|WALFileLengthProvider
name|walFileLengthProvider
decl_stmt|;
comment|// which region server the WALs belong to
specifier|private
specifier|final
name|ServerName
name|serverName
decl_stmt|;
specifier|private
specifier|final
name|MetricsSource
name|metrics
decl_stmt|;
comment|/**    * Create an entry stream over the given queue at the given start position    * @param logQueue the queue of WAL paths    * @param fs {@link FileSystem} to use to create {@link Reader} for this stream    * @param conf {@link Configuration} to use to create {@link Reader} for this stream    * @param startPosition the position in the first WAL to start reading at    * @param serverName the server name which all WALs belong to    * @param metrics replication metrics    * @throws IOException    */
specifier|public
name|WALEntryStream
parameter_list|(
name|PriorityBlockingQueue
argument_list|<
name|Path
argument_list|>
name|logQueue
parameter_list|,
name|Configuration
name|conf
parameter_list|,
name|long
name|startPosition
parameter_list|,
name|WALFileLengthProvider
name|walFileLengthProvider
parameter_list|,
name|ServerName
name|serverName
parameter_list|,
name|MetricsSource
name|metrics
parameter_list|)
throws|throws
name|IOException
block|{
name|this
operator|.
name|logQueue
operator|=
name|logQueue
expr_stmt|;
name|this
operator|.
name|fs
operator|=
name|CommonFSUtils
operator|.
name|getWALFileSystem
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
name|this
operator|.
name|currentPositionOfEntry
operator|=
name|startPosition
expr_stmt|;
name|this
operator|.
name|walFileLengthProvider
operator|=
name|walFileLengthProvider
expr_stmt|;
name|this
operator|.
name|serverName
operator|=
name|serverName
expr_stmt|;
name|this
operator|.
name|metrics
operator|=
name|metrics
expr_stmt|;
block|}
comment|/**    * @return true if there is another WAL {@link Entry}    */
specifier|public
name|boolean
name|hasNext
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|currentEntry
operator|==
literal|null
condition|)
block|{
name|tryAdvanceEntry
argument_list|()
expr_stmt|;
block|}
return|return
name|currentEntry
operator|!=
literal|null
return|;
block|}
comment|/**    * Returns the next WAL entry in this stream but does not advance.    */
specifier|public
name|Entry
name|peek
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|hasNext
argument_list|()
condition|?
name|currentEntry
else|:
literal|null
return|;
block|}
comment|/**    * Returns the next WAL entry in this stream and advance the stream.    */
specifier|public
name|Entry
name|next
parameter_list|()
throws|throws
name|IOException
block|{
name|Entry
name|save
init|=
name|peek
argument_list|()
decl_stmt|;
name|currentPositionOfEntry
operator|=
name|currentPositionOfReader
expr_stmt|;
name|currentEntry
operator|=
literal|null
expr_stmt|;
return|return
name|save
return|;
block|}
comment|/**    * {@inheritDoc}    */
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|()
throws|throws
name|IOException
block|{
name|closeReader
argument_list|()
expr_stmt|;
block|}
comment|/**    * @return the position of the last Entry returned by next()    */
specifier|public
name|long
name|getPosition
parameter_list|()
block|{
return|return
name|currentPositionOfEntry
return|;
block|}
comment|/**    * @return the {@link Path} of the current WAL    */
specifier|public
name|Path
name|getCurrentPath
parameter_list|()
block|{
return|return
name|currentPath
return|;
block|}
specifier|private
name|String
name|getCurrentPathStat
parameter_list|()
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
name|currentPath
operator|!=
literal|null
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|"currently replicating from: "
argument_list|)
operator|.
name|append
argument_list|(
name|currentPath
argument_list|)
operator|.
name|append
argument_list|(
literal|" at position: "
argument_list|)
operator|.
name|append
argument_list|(
name|currentPositionOfEntry
argument_list|)
operator|.
name|append
argument_list|(
literal|"\n"
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|sb
operator|.
name|append
argument_list|(
literal|"no replication ongoing, waiting for new log"
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
comment|/**    * Should be called if the stream is to be reused (i.e. used again after hasNext() has returned    * false)    */
specifier|public
name|void
name|reset
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|reader
operator|!=
literal|null
operator|&&
name|currentPath
operator|!=
literal|null
condition|)
block|{
name|resetReader
argument_list|()
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|setPosition
parameter_list|(
name|long
name|position
parameter_list|)
block|{
name|currentPositionOfEntry
operator|=
name|position
expr_stmt|;
block|}
specifier|private
name|void
name|setCurrentPath
parameter_list|(
name|Path
name|path
parameter_list|)
block|{
name|this
operator|.
name|currentPath
operator|=
name|path
expr_stmt|;
block|}
specifier|private
name|void
name|tryAdvanceEntry
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|checkReader
argument_list|()
condition|)
block|{
name|boolean
name|beingWritten
init|=
name|readNextEntryAndRecordReaderPosition
argument_list|()
decl_stmt|;
if|if
condition|(
name|currentEntry
operator|==
literal|null
operator|&&
operator|!
name|beingWritten
condition|)
block|{
comment|// no more entries in this log file, and the file is already closed, i.e, rolled
comment|// Before dequeueing, we should always get one more attempt at reading.
comment|// This is in case more entries came in after we opened the reader, and the log is rolled
comment|// while we were reading. See HBASE-6758
name|resetReader
argument_list|()
expr_stmt|;
name|readNextEntryAndRecordReaderPosition
argument_list|()
expr_stmt|;
if|if
condition|(
name|currentEntry
operator|==
literal|null
condition|)
block|{
if|if
condition|(
name|checkAllBytesParsed
argument_list|()
condition|)
block|{
comment|// now we're certain we're done with this log file
name|dequeueCurrentLog
argument_list|()
expr_stmt|;
if|if
condition|(
name|openNextLog
argument_list|()
condition|)
block|{
name|readNextEntryAndRecordReaderPosition
argument_list|()
expr_stmt|;
block|}
block|}
block|}
block|}
comment|// if currentEntry != null then just return
comment|// if currentEntry == null but the file is still being written, then we should not switch to
comment|// the next log either, just return here and try next time to see if there are more entries in
comment|// the current file
block|}
comment|// do nothing if we don't have a WAL Reader (e.g. if there's no logs in queue)
block|}
comment|// HBASE-15984 check to see we have in fact parsed all data in a cleanly closed file
specifier|private
name|boolean
name|checkAllBytesParsed
parameter_list|()
throws|throws
name|IOException
block|{
comment|// -1 means the wal wasn't closed cleanly.
specifier|final
name|long
name|trailerSize
init|=
name|currentTrailerSize
argument_list|()
decl_stmt|;
name|FileStatus
name|stat
init|=
literal|null
decl_stmt|;
try|try
block|{
name|stat
operator|=
name|fs
operator|.
name|getFileStatus
argument_list|(
name|this
operator|.
name|currentPath
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|exception
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Couldn't get file length information about log {}, it {} closed cleanly {}"
argument_list|,
name|currentPath
argument_list|,
name|trailerSize
operator|<
literal|0
condition|?
literal|"was not"
else|:
literal|"was"
argument_list|,
name|getCurrentPathStat
argument_list|()
argument_list|)
expr_stmt|;
name|metrics
operator|.
name|incrUnknownFileLengthForClosedWAL
argument_list|()
expr_stmt|;
block|}
comment|// Here we use currentPositionOfReader instead of currentPositionOfEntry.
comment|// We only call this method when currentEntry is null so usually they are the same, but there
comment|// are two exceptions. One is we have nothing in the file but only a header, in this way
comment|// the currentPositionOfEntry will always be 0 since we have no change to update it. The other
comment|// is that we reach the end of file, then currentPositionOfEntry will point to the tail of the
comment|// last valid entry, and the currentPositionOfReader will usually point to the end of the file.
if|if
condition|(
name|stat
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|trailerSize
operator|<
literal|0
condition|)
block|{
if|if
condition|(
name|currentPositionOfReader
operator|<
name|stat
operator|.
name|getLen
argument_list|()
condition|)
block|{
specifier|final
name|long
name|skippedBytes
init|=
name|stat
operator|.
name|getLen
argument_list|()
operator|-
name|currentPositionOfReader
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Reached the end of WAL file '{}'. It was not closed cleanly,"
operator|+
literal|" so we did not parse {} bytes of data. This is normally ok."
argument_list|,
name|currentPath
argument_list|,
name|skippedBytes
argument_list|)
expr_stmt|;
name|metrics
operator|.
name|incrUncleanlyClosedWALs
argument_list|()
expr_stmt|;
name|metrics
operator|.
name|incrBytesSkippedInUncleanlyClosedWALs
argument_list|(
name|skippedBytes
argument_list|)
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|currentPositionOfReader
operator|+
name|trailerSize
operator|<
name|stat
operator|.
name|getLen
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Processing end of WAL file '{}'. At position {}, which is too far away from"
operator|+
literal|" reported file length {}. Restarting WAL reading (see HBASE-15983 for details). {}"
argument_list|,
name|currentPath
argument_list|,
name|currentPositionOfReader
argument_list|,
name|stat
operator|.
name|getLen
argument_list|()
argument_list|,
name|getCurrentPathStat
argument_list|()
argument_list|)
expr_stmt|;
name|setPosition
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|resetReader
argument_list|()
expr_stmt|;
name|metrics
operator|.
name|incrRestartedWALReading
argument_list|()
expr_stmt|;
name|metrics
operator|.
name|incrRepeatedFileBytes
argument_list|(
name|currentPositionOfReader
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
block|}
if|if
condition|(
name|LOG
operator|.
name|isTraceEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|trace
argument_list|(
literal|"Reached the end of log "
operator|+
name|this
operator|.
name|currentPath
operator|+
literal|", and the length of the file is "
operator|+
operator|(
name|stat
operator|==
literal|null
condition|?
literal|"N/A"
else|:
name|stat
operator|.
name|getLen
argument_list|()
operator|)
argument_list|)
expr_stmt|;
block|}
name|metrics
operator|.
name|incrCompletedWAL
argument_list|()
expr_stmt|;
return|return
literal|true
return|;
block|}
specifier|private
name|void
name|dequeueCurrentLog
parameter_list|()
throws|throws
name|IOException
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Reached the end of log {}"
argument_list|,
name|currentPath
argument_list|)
expr_stmt|;
name|closeReader
argument_list|()
expr_stmt|;
name|logQueue
operator|.
name|remove
argument_list|()
expr_stmt|;
name|setPosition
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|metrics
operator|.
name|decrSizeOfLogQueue
argument_list|()
expr_stmt|;
block|}
comment|/**    * Returns whether the file is opened for writing.    */
specifier|private
name|boolean
name|readNextEntryAndRecordReaderPosition
parameter_list|()
throws|throws
name|IOException
block|{
name|Entry
name|readEntry
init|=
name|reader
operator|.
name|next
argument_list|()
decl_stmt|;
name|long
name|readerPos
init|=
name|reader
operator|.
name|getPosition
argument_list|()
decl_stmt|;
name|OptionalLong
name|fileLength
init|=
name|walFileLengthProvider
operator|.
name|getLogFileSizeIfBeingWritten
argument_list|(
name|currentPath
argument_list|)
decl_stmt|;
if|if
condition|(
name|fileLength
operator|.
name|isPresent
argument_list|()
operator|&&
name|readerPos
operator|>
name|fileLength
operator|.
name|getAsLong
argument_list|()
condition|)
block|{
comment|// see HBASE-14004, for AsyncFSWAL which uses fan-out, it is possible that we read uncommitted
comment|// data, so we need to make sure that we do not read beyond the committed file length.
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"The provider tells us the valid length for "
operator|+
name|currentPath
operator|+
literal|" is "
operator|+
name|fileLength
operator|.
name|getAsLong
argument_list|()
operator|+
literal|", but we have advanced to "
operator|+
name|readerPos
argument_list|)
expr_stmt|;
block|}
name|resetReader
argument_list|()
expr_stmt|;
return|return
literal|true
return|;
block|}
if|if
condition|(
name|readEntry
operator|!=
literal|null
condition|)
block|{
name|metrics
operator|.
name|incrLogEditsRead
argument_list|()
expr_stmt|;
name|metrics
operator|.
name|incrLogReadInBytes
argument_list|(
name|readerPos
operator|-
name|currentPositionOfEntry
argument_list|)
expr_stmt|;
block|}
name|currentEntry
operator|=
name|readEntry
expr_stmt|;
comment|// could be null
name|this
operator|.
name|currentPositionOfReader
operator|=
name|readerPos
expr_stmt|;
return|return
name|fileLength
operator|.
name|isPresent
argument_list|()
return|;
block|}
specifier|private
name|void
name|closeReader
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|reader
operator|!=
literal|null
condition|)
block|{
name|reader
operator|.
name|close
argument_list|()
expr_stmt|;
name|reader
operator|=
literal|null
expr_stmt|;
block|}
block|}
comment|// if we don't have a reader, open a reader on the next log
specifier|private
name|boolean
name|checkReader
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|reader
operator|==
literal|null
condition|)
block|{
return|return
name|openNextLog
argument_list|()
return|;
block|}
return|return
literal|true
return|;
block|}
comment|// open a reader on the next log in queue
specifier|private
name|boolean
name|openNextLog
parameter_list|()
throws|throws
name|IOException
block|{
name|Path
name|nextPath
init|=
name|logQueue
operator|.
name|peek
argument_list|()
decl_stmt|;
if|if
condition|(
name|nextPath
operator|!=
literal|null
condition|)
block|{
name|openReader
argument_list|(
name|nextPath
argument_list|)
expr_stmt|;
if|if
condition|(
name|reader
operator|!=
literal|null
condition|)
block|{
return|return
literal|true
return|;
block|}
block|}
else|else
block|{
comment|// no more files in queue, this could happen for recovered queue, or for a wal group of a sync
comment|// replication peer which has already been transited to DA or S.
name|setCurrentPath
argument_list|(
literal|null
argument_list|)
expr_stmt|;
block|}
return|return
literal|false
return|;
block|}
specifier|private
name|Path
name|getArchivedLog
parameter_list|(
name|Path
name|path
parameter_list|)
throws|throws
name|IOException
block|{
name|Path
name|walRootDir
init|=
name|CommonFSUtils
operator|.
name|getWALRootDir
argument_list|(
name|conf
argument_list|)
decl_stmt|;
comment|// Try found the log in old dir
name|Path
name|oldLogDir
init|=
operator|new
name|Path
argument_list|(
name|walRootDir
argument_list|,
name|HConstants
operator|.
name|HREGION_OLDLOGDIR_NAME
argument_list|)
decl_stmt|;
name|Path
name|archivedLogLocation
init|=
operator|new
name|Path
argument_list|(
name|oldLogDir
argument_list|,
name|path
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|fs
operator|.
name|exists
argument_list|(
name|archivedLogLocation
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Log "
operator|+
name|path
operator|+
literal|" was moved to "
operator|+
name|archivedLogLocation
argument_list|)
expr_stmt|;
return|return
name|archivedLogLocation
return|;
block|}
comment|// Try found the log in the seperate old log dir
name|oldLogDir
operator|=
operator|new
name|Path
argument_list|(
name|walRootDir
argument_list|,
operator|new
name|StringBuilder
argument_list|(
name|HConstants
operator|.
name|HREGION_OLDLOGDIR_NAME
argument_list|)
operator|.
name|append
argument_list|(
name|Path
operator|.
name|SEPARATOR
argument_list|)
operator|.
name|append
argument_list|(
name|serverName
operator|.
name|getServerName
argument_list|()
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|archivedLogLocation
operator|=
operator|new
name|Path
argument_list|(
name|oldLogDir
argument_list|,
name|path
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|fs
operator|.
name|exists
argument_list|(
name|archivedLogLocation
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Log "
operator|+
name|path
operator|+
literal|" was moved to "
operator|+
name|archivedLogLocation
argument_list|)
expr_stmt|;
return|return
name|archivedLogLocation
return|;
block|}
name|LOG
operator|.
name|error
argument_list|(
literal|"Couldn't locate log: "
operator|+
name|path
argument_list|)
expr_stmt|;
return|return
name|path
return|;
block|}
specifier|private
name|void
name|handleFileNotFound
parameter_list|(
name|Path
name|path
parameter_list|,
name|FileNotFoundException
name|fnfe
parameter_list|)
throws|throws
name|IOException
block|{
comment|// If the log was archived, continue reading from there
name|Path
name|archivedLog
init|=
name|getArchivedLog
argument_list|(
name|path
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|path
operator|.
name|equals
argument_list|(
name|archivedLog
argument_list|)
condition|)
block|{
name|openReader
argument_list|(
name|archivedLog
argument_list|)
expr_stmt|;
block|}
else|else
block|{
throw|throw
name|fnfe
throw|;
block|}
block|}
specifier|private
name|void
name|openReader
parameter_list|(
name|Path
name|path
parameter_list|)
throws|throws
name|IOException
block|{
try|try
block|{
comment|// Detect if this is a new file, if so get a new reader else
comment|// reset the current reader so that we see the new data
if|if
condition|(
name|reader
operator|==
literal|null
operator|||
operator|!
name|getCurrentPath
argument_list|()
operator|.
name|equals
argument_list|(
name|path
argument_list|)
condition|)
block|{
name|closeReader
argument_list|()
expr_stmt|;
name|reader
operator|=
name|WALFactory
operator|.
name|createReader
argument_list|(
name|fs
argument_list|,
name|path
argument_list|,
name|conf
argument_list|)
expr_stmt|;
name|seek
argument_list|()
expr_stmt|;
name|setCurrentPath
argument_list|(
name|path
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|resetReader
argument_list|()
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|FileNotFoundException
name|fnfe
parameter_list|)
block|{
name|handleFileNotFound
argument_list|(
name|path
argument_list|,
name|fnfe
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|RemoteException
name|re
parameter_list|)
block|{
name|IOException
name|ioe
init|=
name|re
operator|.
name|unwrapRemoteException
argument_list|(
name|FileNotFoundException
operator|.
name|class
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
operator|(
name|ioe
operator|instanceof
name|FileNotFoundException
operator|)
condition|)
throw|throw
name|ioe
throw|;
name|handleFileNotFound
argument_list|(
name|path
argument_list|,
operator|(
name|FileNotFoundException
operator|)
name|ioe
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|LeaseNotRecoveredException
name|lnre
parameter_list|)
block|{
comment|// HBASE-15019 the WAL was not closed due to some hiccup.
name|LOG
operator|.
name|warn
argument_list|(
literal|"Try to recover the WAL lease "
operator|+
name|currentPath
argument_list|,
name|lnre
argument_list|)
expr_stmt|;
name|recoverLease
argument_list|(
name|conf
argument_list|,
name|currentPath
argument_list|)
expr_stmt|;
name|reader
operator|=
literal|null
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|NullPointerException
name|npe
parameter_list|)
block|{
comment|// Workaround for race condition in HDFS-4380
comment|// which throws a NPE if we open a file before any data node has the most recent block
comment|// Just sleep and retry. Will require re-reading compressed WALs for compressionContext.
name|LOG
operator|.
name|warn
argument_list|(
literal|"Got NPE opening reader, will retry."
argument_list|)
expr_stmt|;
name|reader
operator|=
literal|null
expr_stmt|;
block|}
block|}
comment|// For HBASE-15019
specifier|private
name|void
name|recoverLease
parameter_list|(
specifier|final
name|Configuration
name|conf
parameter_list|,
specifier|final
name|Path
name|path
parameter_list|)
block|{
try|try
block|{
specifier|final
name|FileSystem
name|dfs
init|=
name|CommonFSUtils
operator|.
name|getWALFileSystem
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|FSUtils
name|fsUtils
init|=
name|FSUtils
operator|.
name|getInstance
argument_list|(
name|dfs
argument_list|,
name|conf
argument_list|)
decl_stmt|;
name|fsUtils
operator|.
name|recoverFileLease
argument_list|(
name|dfs
argument_list|,
name|path
argument_list|,
name|conf
argument_list|,
operator|new
name|CancelableProgressable
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|boolean
name|progress
parameter_list|()
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"recover WAL lease: "
operator|+
name|path
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
block|}
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
literal|"unable to recover lease for WAL: "
operator|+
name|path
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|resetReader
parameter_list|()
throws|throws
name|IOException
block|{
try|try
block|{
name|currentEntry
operator|=
literal|null
expr_stmt|;
name|reader
operator|.
name|reset
argument_list|()
expr_stmt|;
name|seek
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|FileNotFoundException
name|fnfe
parameter_list|)
block|{
comment|// If the log was archived, continue reading from there
name|Path
name|archivedLog
init|=
name|getArchivedLog
argument_list|(
name|currentPath
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|currentPath
operator|.
name|equals
argument_list|(
name|archivedLog
argument_list|)
condition|)
block|{
name|openReader
argument_list|(
name|archivedLog
argument_list|)
expr_stmt|;
block|}
else|else
block|{
throw|throw
name|fnfe
throw|;
block|}
block|}
catch|catch
parameter_list|(
name|NullPointerException
name|npe
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"NPE resetting reader, likely HDFS-4380"
argument_list|,
name|npe
argument_list|)
throw|;
block|}
block|}
specifier|private
name|void
name|seek
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|currentPositionOfEntry
operator|!=
literal|0
condition|)
block|{
name|reader
operator|.
name|seek
argument_list|(
name|currentPositionOfEntry
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|long
name|currentTrailerSize
parameter_list|()
block|{
name|long
name|size
init|=
operator|-
literal|1L
decl_stmt|;
if|if
condition|(
name|reader
operator|instanceof
name|ProtobufLogReader
condition|)
block|{
specifier|final
name|ProtobufLogReader
name|pblr
init|=
operator|(
name|ProtobufLogReader
operator|)
name|reader
decl_stmt|;
name|size
operator|=
name|pblr
operator|.
name|trailerSize
argument_list|()
expr_stmt|;
block|}
return|return
name|size
return|;
block|}
block|}
end_class

end_unit

