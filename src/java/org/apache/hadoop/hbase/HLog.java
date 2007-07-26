begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2007 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|io
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
name|io
operator|.
name|SequenceFile
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
name|fs
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
name|conf
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
name|*
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

begin_comment
comment|/**  * HLog stores all the edits to the HStore.  *   * It performs logfile-rolling, so external callers are not aware that the   * underlying file is being rolled.  *  *<p>A single HLog is used by several HRegions simultaneously.  *   *<p>Each HRegion is identified by a unique long<code>int</code>. HRegions do  * not need to declare themselves before using the HLog; they simply include  * their HRegion-id in the {@link #append(Text, Text, Text, TreeMap, long)} or   * {@link #completeCacheFlush(Text, Text, long)} calls.  *  *<p>An HLog consists of multiple on-disk files, which have a chronological  * order. As data is flushed to other (better) on-disk structures, the log  * becomes obsolete.  We can destroy all the log messages for a given  * HRegion-id up to the most-recent CACHEFLUSH message from that HRegion.  *  *<p>It's only practical to delete entire files.  Thus, we delete an entire   * on-disk file F when all of the messages in F have a log-sequence-id that's   * older (smaller) than the most-recent CACHEFLUSH message for every HRegion   * that has a message in F.  *   *<p>TODO: Vuk Ercegovac also pointed out that keeping HBase HRegion edit logs  * in HDFS is currently flawed. HBase writes edits to logs and to a memcache.  * The 'atomic' write to the log is meant to serve as insurance against  * abnormal RegionServer exit: on startup, the log is rerun to reconstruct an  * HRegion's last wholesome state. But files in HDFS do not 'exist' until they  * are cleanly closed -- something that will not happen if RegionServer exits  * without running its 'close'.  */
end_comment

begin_class
specifier|public
class|class
name|HLog
implements|implements
name|HConstants
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
name|HLog
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|static
specifier|final
name|String
name|HLOG_DATFILE
init|=
literal|"hlog.dat."
decl_stmt|;
specifier|static
specifier|final
name|Text
name|METACOLUMN
init|=
operator|new
name|Text
argument_list|(
literal|"METACOLUMN:"
argument_list|)
decl_stmt|;
specifier|static
specifier|final
name|Text
name|METAROW
init|=
operator|new
name|Text
argument_list|(
literal|"METAROW"
argument_list|)
decl_stmt|;
name|FileSystem
name|fs
decl_stmt|;
name|Path
name|dir
decl_stmt|;
name|Configuration
name|conf
decl_stmt|;
name|SequenceFile
operator|.
name|Writer
name|writer
decl_stmt|;
name|TreeMap
argument_list|<
name|Long
argument_list|,
name|Path
argument_list|>
name|outputfiles
init|=
operator|new
name|TreeMap
argument_list|<
name|Long
argument_list|,
name|Path
argument_list|>
argument_list|()
decl_stmt|;
name|boolean
name|insideCacheFlush
init|=
literal|false
decl_stmt|;
name|TreeMap
argument_list|<
name|Text
argument_list|,
name|Long
argument_list|>
name|regionToLastFlush
init|=
operator|new
name|TreeMap
argument_list|<
name|Text
argument_list|,
name|Long
argument_list|>
argument_list|()
decl_stmt|;
name|boolean
name|closed
init|=
literal|false
decl_stmt|;
specifier|transient
name|long
name|logSeqNum
init|=
literal|0
decl_stmt|;
name|long
name|filenum
init|=
literal|0
decl_stmt|;
specifier|transient
name|int
name|numEntries
init|=
literal|0
decl_stmt|;
name|Integer
name|rollLock
init|=
operator|new
name|Integer
argument_list|(
literal|0
argument_list|)
decl_stmt|;
comment|/**    * Split up a bunch of log files, that are no longer being written to,    * into new files, one per region.  Delete the old log files when ready.    * @param rootDir Root directory of the HBase instance    * @param srcDir Directory of log files to split:    * e.g.<code>${ROOTDIR}/log_HOST_PORT</code>    * @param fs FileSystem    * @param conf HBaseConfiguration    * @throws IOException    */
specifier|static
name|void
name|splitLog
parameter_list|(
name|Path
name|rootDir
parameter_list|,
name|Path
name|srcDir
parameter_list|,
name|FileSystem
name|fs
parameter_list|,
name|Configuration
name|conf
parameter_list|)
throws|throws
name|IOException
block|{
name|Path
name|logfiles
index|[]
init|=
name|fs
operator|.
name|listPaths
argument_list|(
name|srcDir
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"splitting "
operator|+
name|logfiles
operator|.
name|length
operator|+
literal|" log files in "
operator|+
name|srcDir
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|HashMap
argument_list|<
name|Text
argument_list|,
name|SequenceFile
operator|.
name|Writer
argument_list|>
name|logWriters
init|=
operator|new
name|HashMap
argument_list|<
name|Text
argument_list|,
name|SequenceFile
operator|.
name|Writer
argument_list|>
argument_list|()
decl_stmt|;
try|try
block|{
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|logfiles
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|SequenceFile
operator|.
name|Reader
name|in
init|=
operator|new
name|SequenceFile
operator|.
name|Reader
argument_list|(
name|fs
argument_list|,
name|logfiles
index|[
name|i
index|]
argument_list|,
name|conf
argument_list|)
decl_stmt|;
try|try
block|{
name|HLogKey
name|key
init|=
operator|new
name|HLogKey
argument_list|()
decl_stmt|;
name|HLogEdit
name|val
init|=
operator|new
name|HLogEdit
argument_list|()
decl_stmt|;
while|while
condition|(
name|in
operator|.
name|next
argument_list|(
name|key
argument_list|,
name|val
argument_list|)
condition|)
block|{
name|Text
name|regionName
init|=
name|key
operator|.
name|getRegionName
argument_list|()
decl_stmt|;
name|SequenceFile
operator|.
name|Writer
name|w
init|=
name|logWriters
operator|.
name|get
argument_list|(
name|regionName
argument_list|)
decl_stmt|;
if|if
condition|(
name|w
operator|==
literal|null
condition|)
block|{
name|Path
name|logfile
init|=
operator|new
name|Path
argument_list|(
name|HStoreFile
operator|.
name|getHRegionDir
argument_list|(
name|rootDir
argument_list|,
name|regionName
argument_list|)
argument_list|,
name|HREGION_OLDLOGFILE_NAME
argument_list|)
decl_stmt|;
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
literal|"getting new log file writer for path "
operator|+
name|logfile
argument_list|)
expr_stmt|;
block|}
name|w
operator|=
name|SequenceFile
operator|.
name|createWriter
argument_list|(
name|fs
argument_list|,
name|conf
argument_list|,
name|logfile
argument_list|,
name|HLogKey
operator|.
name|class
argument_list|,
name|HLogEdit
operator|.
name|class
argument_list|)
expr_stmt|;
name|logWriters
operator|.
name|put
argument_list|(
name|regionName
argument_list|,
name|w
argument_list|)
expr_stmt|;
block|}
name|w
operator|.
name|append
argument_list|(
name|key
argument_list|,
name|val
argument_list|)
expr_stmt|;
block|}
block|}
finally|finally
block|{
name|in
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
block|}
finally|finally
block|{
for|for
control|(
name|SequenceFile
operator|.
name|Writer
name|w
range|:
name|logWriters
operator|.
name|values
argument_list|()
control|)
block|{
name|w
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
if|if
condition|(
name|fs
operator|.
name|exists
argument_list|(
name|srcDir
argument_list|)
condition|)
block|{
if|if
condition|(
operator|!
name|fs
operator|.
name|delete
argument_list|(
name|srcDir
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Cannot delete: "
operator|+
name|srcDir
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|FileUtil
operator|.
name|fullyDelete
argument_list|(
operator|new
name|File
argument_list|(
name|srcDir
operator|.
name|toString
argument_list|()
argument_list|)
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Cannot delete: "
operator|+
name|srcDir
argument_list|)
throw|;
block|}
block|}
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"log file splitting completed for "
operator|+
name|srcDir
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**    * Create an edit log at the given<code>dir</code> location.    *    * You should never have to load an existing log.  If there is a log    * at startup, it should have already been processed and deleted by     * the time the HLog object is started up.    *     * @param fs    * @param dir    * @param conf    * @throws IOException    */
name|HLog
parameter_list|(
name|FileSystem
name|fs
parameter_list|,
name|Path
name|dir
parameter_list|,
name|Configuration
name|conf
parameter_list|)
throws|throws
name|IOException
block|{
name|this
operator|.
name|fs
operator|=
name|fs
expr_stmt|;
name|this
operator|.
name|dir
operator|=
name|dir
expr_stmt|;
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
if|if
condition|(
name|fs
operator|.
name|exists
argument_list|(
name|dir
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Target HLog directory already exists: "
operator|+
name|dir
argument_list|)
throw|;
block|}
name|fs
operator|.
name|mkdirs
argument_list|(
name|dir
argument_list|)
expr_stmt|;
name|rollWriter
argument_list|()
expr_stmt|;
block|}
comment|/**    * Roll the log writer.  That is, start writing log messages to a new file.    *    * The 'rollLock' prevents us from entering rollWriter() more than    * once at a time.    *    * The 'this' lock limits access to the current writer so    * we don't append multiple items simultaneously.    *     * @throws IOException    */
name|void
name|rollWriter
parameter_list|()
throws|throws
name|IOException
block|{
synchronized|synchronized
init|(
name|rollLock
init|)
block|{
comment|// Try to roll the writer to a new file.  We may have to
comment|// wait for a cache-flush to complete.  In the process,
comment|// compute a list of old log files that can be deleted.
name|Vector
argument_list|<
name|Path
argument_list|>
name|toDeleteList
init|=
operator|new
name|Vector
argument_list|<
name|Path
argument_list|>
argument_list|()
decl_stmt|;
synchronized|synchronized
init|(
name|this
init|)
block|{
if|if
condition|(
name|closed
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Cannot roll log; log is closed"
argument_list|)
throw|;
block|}
comment|// Make sure we do not roll the log while inside a
comment|// cache-flush.  Otherwise, the log sequence number for
comment|// the CACHEFLUSH operation will appear in a "newer" log file
comment|// than it should.
while|while
condition|(
name|insideCacheFlush
condition|)
block|{
try|try
block|{
name|wait
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|ie
parameter_list|)
block|{
comment|// continue;
block|}
block|}
comment|// Close the current writer (if any), and grab a new one.
if|if
condition|(
name|writer
operator|!=
literal|null
condition|)
block|{
name|writer
operator|.
name|close
argument_list|()
expr_stmt|;
name|Path
name|p
init|=
name|computeFilename
argument_list|(
name|filenum
operator|-
literal|1
argument_list|)
decl_stmt|;
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
literal|"Closing current log writer "
operator|+
name|p
operator|.
name|toString
argument_list|()
operator|+
literal|" to get a new one"
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|filenum
operator|>
literal|0
condition|)
block|{
name|outputfiles
operator|.
name|put
argument_list|(
name|logSeqNum
operator|-
literal|1
argument_list|,
name|p
argument_list|)
expr_stmt|;
block|}
block|}
name|Path
name|newPath
init|=
name|computeFilename
argument_list|(
name|filenum
operator|++
argument_list|)
decl_stmt|;
name|this
operator|.
name|writer
operator|=
name|SequenceFile
operator|.
name|createWriter
argument_list|(
name|fs
argument_list|,
name|conf
argument_list|,
name|newPath
argument_list|,
name|HLogKey
operator|.
name|class
argument_list|,
name|HLogEdit
operator|.
name|class
argument_list|)
expr_stmt|;
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
literal|"new log writer created at "
operator|+
name|newPath
argument_list|)
expr_stmt|;
block|}
comment|// Can we delete any of the old log files?
comment|// First, compute the oldest relevant log operation
comment|// over all the regions.
name|long
name|oldestOutstandingSeqNum
init|=
name|Long
operator|.
name|MAX_VALUE
decl_stmt|;
for|for
control|(
name|Long
name|l
range|:
name|regionToLastFlush
operator|.
name|values
argument_list|()
control|)
block|{
name|long
name|curSeqNum
init|=
name|l
operator|.
name|longValue
argument_list|()
decl_stmt|;
if|if
condition|(
name|curSeqNum
operator|<
name|oldestOutstandingSeqNum
condition|)
block|{
name|oldestOutstandingSeqNum
operator|=
name|curSeqNum
expr_stmt|;
block|}
block|}
comment|// Next, remove all files with a final ID that's older
comment|// than the oldest pending region-operation.
for|for
control|(
name|Iterator
argument_list|<
name|Long
argument_list|>
name|it
init|=
name|outputfiles
operator|.
name|keySet
argument_list|()
operator|.
name|iterator
argument_list|()
init|;
name|it
operator|.
name|hasNext
argument_list|()
condition|;
control|)
block|{
name|long
name|maxSeqNum
init|=
name|it
operator|.
name|next
argument_list|()
operator|.
name|longValue
argument_list|()
decl_stmt|;
if|if
condition|(
name|maxSeqNum
operator|<
name|oldestOutstandingSeqNum
condition|)
block|{
name|Path
name|p
init|=
name|outputfiles
operator|.
name|get
argument_list|(
name|maxSeqNum
argument_list|)
decl_stmt|;
name|it
operator|.
name|remove
argument_list|()
expr_stmt|;
name|toDeleteList
operator|.
name|add
argument_list|(
name|p
argument_list|)
expr_stmt|;
block|}
else|else
block|{
break|break;
block|}
block|}
block|}
comment|// Actually delete them, if any!
for|for
control|(
name|Iterator
argument_list|<
name|Path
argument_list|>
name|it
init|=
name|toDeleteList
operator|.
name|iterator
argument_list|()
init|;
name|it
operator|.
name|hasNext
argument_list|()
condition|;
control|)
block|{
name|Path
name|p
init|=
name|it
operator|.
name|next
argument_list|()
decl_stmt|;
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
literal|"removing old log file "
operator|+
name|p
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|fs
operator|.
name|delete
argument_list|(
name|p
argument_list|)
expr_stmt|;
block|}
name|this
operator|.
name|numEntries
operator|=
literal|0
expr_stmt|;
block|}
block|}
comment|/**    * This is a convenience method that computes a new filename with    * a given file-number.    */
name|Path
name|computeFilename
parameter_list|(
specifier|final
name|long
name|fn
parameter_list|)
block|{
return|return
operator|new
name|Path
argument_list|(
name|dir
argument_list|,
name|HLOG_DATFILE
operator|+
name|String
operator|.
name|format
argument_list|(
literal|"%1$03d"
argument_list|,
name|fn
argument_list|)
argument_list|)
return|;
block|}
comment|/**    * Shut down the log and delete the log directory    * @throws IOException    */
specifier|synchronized
name|void
name|closeAndDelete
parameter_list|()
throws|throws
name|IOException
block|{
name|close
argument_list|()
expr_stmt|;
name|fs
operator|.
name|delete
argument_list|(
name|dir
argument_list|)
expr_stmt|;
block|}
comment|/**    * Shut down the log.    * @throws IOException    */
specifier|synchronized
name|void
name|close
parameter_list|()
throws|throws
name|IOException
block|{
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
literal|"closing log writer in "
operator|+
name|this
operator|.
name|dir
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|this
operator|.
name|writer
operator|.
name|close
argument_list|()
expr_stmt|;
name|this
operator|.
name|closed
operator|=
literal|true
expr_stmt|;
block|}
comment|/**    * Append a set of edits to the log. Log edits are keyed by regionName,    * rowname, and log-sequence-id.    *    * Later, if we sort by these keys, we obtain all the relevant edits for    * a given key-range of the HRegion (TODO).  Any edits that do not have a    * matching {@link HConstants#COMPLETE_CACHEFLUSH} message can be discarded.    *    *<p>Logs cannot be restarted once closed, or once the HLog process dies.    * Each time the HLog starts, it must create a new log.  This means that    * other systems should process the log appropriately upon each startup    * (and prior to initializing HLog).    *    * We need to seize a lock on the writer so that writes are atomic.    * @param regionName    * @param tableName    * @param row    * @param columns    * @param timestamp    * @throws IOException    */
specifier|synchronized
name|void
name|append
parameter_list|(
name|Text
name|regionName
parameter_list|,
name|Text
name|tableName
parameter_list|,
name|Text
name|row
parameter_list|,
name|TreeMap
argument_list|<
name|Text
argument_list|,
name|byte
index|[]
argument_list|>
name|columns
parameter_list|,
name|long
name|timestamp
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|closed
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Cannot append; log is closed"
argument_list|)
throw|;
block|}
name|long
name|seqNum
index|[]
init|=
name|obtainSeqNum
argument_list|(
name|columns
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
comment|// The 'regionToLastFlush' map holds the sequence id of the
comment|// most recent flush for every regionName.  However, for regions
comment|// that don't have any flush yet, the relevant operation is the
comment|// first one that's been added.
if|if
condition|(
name|regionToLastFlush
operator|.
name|get
argument_list|(
name|regionName
argument_list|)
operator|==
literal|null
condition|)
block|{
name|regionToLastFlush
operator|.
name|put
argument_list|(
name|regionName
argument_list|,
name|seqNum
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
block|}
name|int
name|counter
init|=
literal|0
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|Text
argument_list|,
name|byte
index|[]
argument_list|>
name|es
range|:
name|columns
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|HLogKey
name|logKey
init|=
operator|new
name|HLogKey
argument_list|(
name|regionName
argument_list|,
name|tableName
argument_list|,
name|row
argument_list|,
name|seqNum
index|[
name|counter
operator|++
index|]
argument_list|)
decl_stmt|;
name|HLogEdit
name|logEdit
init|=
operator|new
name|HLogEdit
argument_list|(
name|es
operator|.
name|getKey
argument_list|()
argument_list|,
name|es
operator|.
name|getValue
argument_list|()
argument_list|,
name|timestamp
argument_list|)
decl_stmt|;
name|writer
operator|.
name|append
argument_list|(
name|logKey
argument_list|,
name|logEdit
argument_list|)
expr_stmt|;
name|numEntries
operator|++
expr_stmt|;
block|}
block|}
comment|/** @return How many items have been added to the log */
name|int
name|getNumEntries
parameter_list|()
block|{
return|return
name|numEntries
return|;
block|}
comment|/**    * Obtain a log sequence number.  This seizes the whole HLog    * lock, but it shouldn't last too long.    */
specifier|synchronized
name|long
name|obtainSeqNum
parameter_list|()
block|{
return|return
name|logSeqNum
operator|++
return|;
block|}
comment|/**    * Obtain a specified number of sequence numbers    *     * @param num - number of sequence numbers to obtain    * @return - array of sequence numbers    */
specifier|synchronized
name|long
index|[]
name|obtainSeqNum
parameter_list|(
name|int
name|num
parameter_list|)
block|{
name|long
index|[]
name|results
init|=
operator|new
name|long
index|[
name|num
index|]
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
name|num
condition|;
name|i
operator|++
control|)
block|{
name|results
index|[
name|i
index|]
operator|=
name|logSeqNum
operator|++
expr_stmt|;
block|}
return|return
name|results
return|;
block|}
comment|/**    * By acquiring a log sequence ID, we can allow log messages    * to continue while we flush the cache.    *    * Set a flag so that we do not roll the log between the start    * and complete of a cache-flush.  Otherwise the log-seq-id for    * the flush will not appear in the correct logfile.    * @return sequence ID to pass {@link #completeCacheFlush(Text, Text, long)}    * @see #completeCacheFlush(Text, Text, long)    */
specifier|synchronized
name|long
name|startCacheFlush
parameter_list|()
block|{
while|while
condition|(
name|insideCacheFlush
condition|)
block|{
try|try
block|{
name|wait
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|ie
parameter_list|)
block|{
comment|// continue
block|}
block|}
name|insideCacheFlush
operator|=
literal|true
expr_stmt|;
name|notifyAll
argument_list|()
expr_stmt|;
return|return
name|obtainSeqNum
argument_list|()
return|;
block|}
comment|/** Complete the cache flush    * @param regionName    * @param tableName    * @param logSeqId    * @throws IOException    */
specifier|synchronized
name|void
name|completeCacheFlush
parameter_list|(
specifier|final
name|Text
name|regionName
parameter_list|,
specifier|final
name|Text
name|tableName
parameter_list|,
specifier|final
name|long
name|logSeqId
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|closed
condition|)
block|{
return|return;
block|}
if|if
condition|(
operator|!
name|insideCacheFlush
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Impossible situation: inside "
operator|+
literal|"completeCacheFlush(), but 'insideCacheFlush' flag is false"
argument_list|)
throw|;
block|}
name|writer
operator|.
name|append
argument_list|(
operator|new
name|HLogKey
argument_list|(
name|regionName
argument_list|,
name|tableName
argument_list|,
name|HLog
operator|.
name|METAROW
argument_list|,
name|logSeqId
argument_list|)
argument_list|,
operator|new
name|HLogEdit
argument_list|(
name|HLog
operator|.
name|METACOLUMN
argument_list|,
name|COMPLETE_CACHEFLUSH
operator|.
name|get
argument_list|()
argument_list|,
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|numEntries
operator|++
expr_stmt|;
comment|// Remember the most-recent flush for each region.
comment|// This is used to delete obsolete log files.
name|regionToLastFlush
operator|.
name|put
argument_list|(
name|regionName
argument_list|,
name|logSeqId
argument_list|)
expr_stmt|;
name|insideCacheFlush
operator|=
literal|false
expr_stmt|;
name|notifyAll
argument_list|()
expr_stmt|;
block|}
comment|/**    * Pass a log file and it will dump out a text version on    *<code>stdout</code>.    * @param args    * @throws IOException    */
specifier|public
specifier|static
name|void
name|main
parameter_list|(
name|String
index|[]
name|args
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|args
operator|.
name|length
operator|<
literal|1
condition|)
block|{
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"Usage: java org.apache.hbase.HLog<logfile>"
argument_list|)
expr_stmt|;
name|System
operator|.
name|exit
argument_list|(
operator|-
literal|1
argument_list|)
expr_stmt|;
block|}
name|Configuration
name|conf
init|=
operator|new
name|HBaseConfiguration
argument_list|()
decl_stmt|;
name|FileSystem
name|fs
init|=
name|FileSystem
operator|.
name|get
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|Path
name|logfile
init|=
operator|new
name|Path
argument_list|(
name|args
index|[
literal|0
index|]
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|fs
operator|.
name|exists
argument_list|(
name|logfile
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|FileNotFoundException
argument_list|(
name|args
index|[
literal|0
index|]
operator|+
literal|" does not exist"
argument_list|)
throw|;
block|}
if|if
condition|(
operator|!
name|fs
operator|.
name|isFile
argument_list|(
name|logfile
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
name|args
index|[
literal|0
index|]
operator|+
literal|" is not a file"
argument_list|)
throw|;
block|}
name|Reader
name|log
init|=
operator|new
name|SequenceFile
operator|.
name|Reader
argument_list|(
name|fs
argument_list|,
name|logfile
argument_list|,
name|conf
argument_list|)
decl_stmt|;
try|try
block|{
name|HLogKey
name|key
init|=
operator|new
name|HLogKey
argument_list|()
decl_stmt|;
name|HLogEdit
name|val
init|=
operator|new
name|HLogEdit
argument_list|()
decl_stmt|;
while|while
condition|(
name|log
operator|.
name|next
argument_list|(
name|key
argument_list|,
name|val
argument_list|)
condition|)
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
name|key
operator|.
name|toString
argument_list|()
operator|+
literal|" "
operator|+
name|val
operator|.
name|toString
argument_list|()
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
block|}
end_class

end_unit

