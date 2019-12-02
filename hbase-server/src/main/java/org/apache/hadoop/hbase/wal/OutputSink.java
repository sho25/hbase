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
name|CompletionService
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
name|ExecutorCompletionService
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
name|ThreadPoolExecutor
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
name|TimeUnit
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
name|AtomicLong
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
name|Threads
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

begin_comment
comment|/**  * The following class is an abstraction class to provide a common interface to support different  * ways of consuming recovered edits.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|abstract
class|class
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
name|OutputSink
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|WALSplitter
operator|.
name|PipelineController
name|controller
decl_stmt|;
specifier|protected
specifier|final
name|EntryBuffers
name|entryBuffers
decl_stmt|;
specifier|private
specifier|final
name|List
argument_list|<
name|WriterThread
argument_list|>
name|writerThreads
init|=
name|Lists
operator|.
name|newArrayList
argument_list|()
decl_stmt|;
specifier|protected
specifier|final
name|int
name|numThreads
decl_stmt|;
specifier|protected
name|CancelableProgressable
name|reporter
init|=
literal|null
decl_stmt|;
specifier|protected
specifier|final
name|AtomicLong
name|totalSkippedEdits
init|=
operator|new
name|AtomicLong
argument_list|()
decl_stmt|;
specifier|protected
specifier|final
name|List
argument_list|<
name|Path
argument_list|>
name|splits
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
comment|/**    * Used when close this output sink.    */
specifier|protected
specifier|final
name|ThreadPoolExecutor
name|closeThreadPool
decl_stmt|;
specifier|protected
specifier|final
name|CompletionService
argument_list|<
name|Void
argument_list|>
name|closeCompletionService
decl_stmt|;
specifier|public
name|OutputSink
parameter_list|(
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
name|this
operator|.
name|numThreads
operator|=
name|numWriters
expr_stmt|;
name|this
operator|.
name|controller
operator|=
name|controller
expr_stmt|;
name|this
operator|.
name|entryBuffers
operator|=
name|entryBuffers
expr_stmt|;
name|this
operator|.
name|closeThreadPool
operator|=
name|Threads
operator|.
name|getBoundedCachedThreadPool
argument_list|(
name|numThreads
argument_list|,
literal|30L
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|,
name|Threads
operator|.
name|newDaemonThreadFactory
argument_list|(
literal|"split-log-closeStream-"
argument_list|)
argument_list|)
expr_stmt|;
name|this
operator|.
name|closeCompletionService
operator|=
operator|new
name|ExecutorCompletionService
argument_list|<>
argument_list|(
name|closeThreadPool
argument_list|)
expr_stmt|;
block|}
name|void
name|setReporter
parameter_list|(
name|CancelableProgressable
name|reporter
parameter_list|)
block|{
name|this
operator|.
name|reporter
operator|=
name|reporter
expr_stmt|;
block|}
comment|/**    * Start the threads that will pump data from the entryBuffers to the output files.    */
specifier|synchronized
name|void
name|startWriterThreads
parameter_list|()
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
name|numThreads
condition|;
name|i
operator|++
control|)
block|{
name|WriterThread
name|t
init|=
operator|new
name|WriterThread
argument_list|(
name|controller
argument_list|,
name|entryBuffers
argument_list|,
name|this
argument_list|,
name|i
argument_list|)
decl_stmt|;
name|t
operator|.
name|start
argument_list|()
expr_stmt|;
name|writerThreads
operator|.
name|add
argument_list|(
name|t
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Wait for writer threads to dump all info to the sink    *    * @return true when there is no error    */
name|boolean
name|finishWriterThreads
parameter_list|()
throws|throws
name|IOException
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Waiting for split writer threads to finish"
argument_list|)
expr_stmt|;
name|boolean
name|progressFailed
init|=
literal|false
decl_stmt|;
for|for
control|(
name|WriterThread
name|t
range|:
name|writerThreads
control|)
block|{
name|t
operator|.
name|finish
argument_list|()
expr_stmt|;
block|}
for|for
control|(
name|WriterThread
name|t
range|:
name|writerThreads
control|)
block|{
if|if
condition|(
operator|!
name|progressFailed
operator|&&
name|reporter
operator|!=
literal|null
operator|&&
operator|!
name|reporter
operator|.
name|progress
argument_list|()
condition|)
block|{
name|progressFailed
operator|=
literal|true
expr_stmt|;
block|}
try|try
block|{
name|t
operator|.
name|join
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|ie
parameter_list|)
block|{
name|IOException
name|iie
init|=
operator|new
name|InterruptedIOException
argument_list|()
decl_stmt|;
name|iie
operator|.
name|initCause
argument_list|(
name|ie
argument_list|)
expr_stmt|;
throw|throw
name|iie
throw|;
block|}
block|}
name|controller
operator|.
name|checkForErrors
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"{} split writer threads finished"
argument_list|,
name|this
operator|.
name|writerThreads
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
return|return
operator|(
operator|!
name|progressFailed
operator|)
return|;
block|}
name|long
name|getTotalSkippedEdits
parameter_list|()
block|{
return|return
name|this
operator|.
name|totalSkippedEdits
operator|.
name|get
argument_list|()
return|;
block|}
comment|/**    * @return the number of currently opened writers    */
specifier|abstract
name|int
name|getNumOpenWriters
parameter_list|()
function_decl|;
comment|/**    * @param buffer A buffer of some number of edits for a given region.    */
specifier|abstract
name|void
name|append
parameter_list|(
name|EntryBuffers
operator|.
name|RegionEntryBuffer
name|buffer
parameter_list|)
throws|throws
name|IOException
function_decl|;
specifier|abstract
name|List
argument_list|<
name|Path
argument_list|>
name|close
parameter_list|()
throws|throws
name|IOException
function_decl|;
comment|/**    * @return a map from encoded region ID to the number of edits written out for that region.    */
specifier|abstract
name|Map
argument_list|<
name|byte
index|[]
argument_list|,
name|Long
argument_list|>
name|getOutputCounts
parameter_list|()
function_decl|;
comment|/**    * @return number of regions we've recovered    */
specifier|abstract
name|int
name|getNumberOfRecoveredRegions
parameter_list|()
function_decl|;
comment|/**    * Some WALEdit's contain only KV's for account on what happened to a region. Not all sinks will    * want to get all of those edits.    * @return Return true if this sink wants to accept this region-level WALEdit.    */
specifier|abstract
name|boolean
name|keepRegionEvent
parameter_list|(
name|WAL
operator|.
name|Entry
name|entry
parameter_list|)
function_decl|;
specifier|public
specifier|static
class|class
name|WriterThread
extends|extends
name|Thread
block|{
specifier|private
specifier|volatile
name|boolean
name|shouldStop
init|=
literal|false
decl_stmt|;
specifier|private
name|WALSplitter
operator|.
name|PipelineController
name|controller
decl_stmt|;
specifier|private
name|EntryBuffers
name|entryBuffers
decl_stmt|;
specifier|private
name|OutputSink
name|outputSink
init|=
literal|null
decl_stmt|;
name|WriterThread
parameter_list|(
name|WALSplitter
operator|.
name|PipelineController
name|controller
parameter_list|,
name|EntryBuffers
name|entryBuffers
parameter_list|,
name|OutputSink
name|sink
parameter_list|,
name|int
name|i
parameter_list|)
block|{
name|super
argument_list|(
name|Thread
operator|.
name|currentThread
argument_list|()
operator|.
name|getName
argument_list|()
operator|+
literal|"-Writer-"
operator|+
name|i
argument_list|)
expr_stmt|;
name|this
operator|.
name|controller
operator|=
name|controller
expr_stmt|;
name|this
operator|.
name|entryBuffers
operator|=
name|entryBuffers
expr_stmt|;
name|outputSink
operator|=
name|sink
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
try|try
block|{
name|doRun
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Exiting thread"
argument_list|,
name|t
argument_list|)
expr_stmt|;
name|controller
operator|.
name|writerThreadError
argument_list|(
name|t
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|doRun
parameter_list|()
throws|throws
name|IOException
block|{
name|LOG
operator|.
name|trace
argument_list|(
literal|"Writer thread starting"
argument_list|)
expr_stmt|;
while|while
condition|(
literal|true
condition|)
block|{
name|EntryBuffers
operator|.
name|RegionEntryBuffer
name|buffer
init|=
name|entryBuffers
operator|.
name|getChunkToWrite
argument_list|()
decl_stmt|;
if|if
condition|(
name|buffer
operator|==
literal|null
condition|)
block|{
comment|// No data currently available, wait on some more to show up
synchronized|synchronized
init|(
name|controller
operator|.
name|dataAvailable
init|)
block|{
if|if
condition|(
name|shouldStop
condition|)
block|{
return|return;
block|}
try|try
block|{
name|controller
operator|.
name|dataAvailable
operator|.
name|wait
argument_list|(
literal|500
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|ie
parameter_list|)
block|{
if|if
condition|(
operator|!
name|shouldStop
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|ie
argument_list|)
throw|;
block|}
block|}
block|}
continue|continue;
block|}
assert|assert
name|buffer
operator|!=
literal|null
assert|;
try|try
block|{
name|writeBuffer
argument_list|(
name|buffer
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|entryBuffers
operator|.
name|doneWriting
argument_list|(
name|buffer
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|private
name|void
name|writeBuffer
parameter_list|(
name|EntryBuffers
operator|.
name|RegionEntryBuffer
name|buffer
parameter_list|)
throws|throws
name|IOException
block|{
name|outputSink
operator|.
name|append
argument_list|(
name|buffer
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|finish
parameter_list|()
block|{
synchronized|synchronized
init|(
name|controller
operator|.
name|dataAvailable
init|)
block|{
name|shouldStop
operator|=
literal|true
expr_stmt|;
name|controller
operator|.
name|dataAvailable
operator|.
name|notifyAll
argument_list|()
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit

