begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one or more  * contributor license agreements. See the NOTICE file distributed with this  * work for additional information regarding copyright ownership. The ASF  * licenses this file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the  * License for the specific language governing permissions and limitations  * under the License.  */
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
name|util
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
name|util
operator|.
name|Arrays
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashSet
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Set
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
name|commons
operator|.
name|lang
operator|.
name|math
operator|.
name|RandomUtils
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
name|hbase
operator|.
name|HRegionLocation
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
name|Get
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
name|Consistency
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
name|Result
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
name|Table
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
name|test
operator|.
name|LoadTestDataGenerator
import|;
end_import

begin_comment
comment|/** Creates multiple threads that read and verify previously written data */
end_comment

begin_class
specifier|public
class|class
name|MultiThreadedReader
extends|extends
name|MultiThreadedAction
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
name|MultiThreadedReader
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|protected
name|Set
argument_list|<
name|HBaseReaderThread
argument_list|>
name|readers
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|double
name|verifyPercent
decl_stmt|;
specifier|protected
specifier|volatile
name|boolean
name|aborted
decl_stmt|;
specifier|protected
name|MultiThreadedWriterBase
name|writer
init|=
literal|null
decl_stmt|;
comment|/**    * The number of keys verified in a sequence. This will never be larger than    * the total number of keys in the range. The reader might also verify    * random keys when it catches up with the writer.    */
specifier|private
specifier|final
name|AtomicLong
name|numUniqueKeysVerified
init|=
operator|new
name|AtomicLong
argument_list|()
decl_stmt|;
comment|/**    * Default maximum number of read errors to tolerate before shutting down all    * readers.    */
specifier|public
specifier|static
specifier|final
name|int
name|DEFAULT_MAX_ERRORS
init|=
literal|10
decl_stmt|;
comment|/**    * Default "window" size between the last key written by the writer and the    * key that we attempt to read. The lower this number, the stricter our    * testing is. If this is zero, we always attempt to read the highest key    * in the contiguous sequence of keys written by the writers.    */
specifier|public
specifier|static
specifier|final
name|int
name|DEFAULT_KEY_WINDOW
init|=
literal|0
decl_stmt|;
comment|/**    * Default batch size for multigets    */
specifier|public
specifier|static
specifier|final
name|int
name|DEFAULT_BATCH_SIZE
init|=
literal|1
decl_stmt|;
comment|//translates to simple GET (no multi GET)
specifier|protected
name|AtomicLong
name|numKeysVerified
init|=
operator|new
name|AtomicLong
argument_list|(
literal|0
argument_list|)
decl_stmt|;
specifier|protected
name|AtomicLong
name|numReadErrors
init|=
operator|new
name|AtomicLong
argument_list|(
literal|0
argument_list|)
decl_stmt|;
specifier|protected
name|AtomicLong
name|numReadFailures
init|=
operator|new
name|AtomicLong
argument_list|(
literal|0
argument_list|)
decl_stmt|;
specifier|protected
name|AtomicLong
name|nullResult
init|=
operator|new
name|AtomicLong
argument_list|(
literal|0
argument_list|)
decl_stmt|;
specifier|private
name|int
name|maxErrors
init|=
name|DEFAULT_MAX_ERRORS
decl_stmt|;
specifier|private
name|int
name|keyWindow
init|=
name|DEFAULT_KEY_WINDOW
decl_stmt|;
specifier|private
name|int
name|batchSize
init|=
name|DEFAULT_BATCH_SIZE
decl_stmt|;
specifier|private
name|int
name|regionReplicaId
init|=
operator|-
literal|1
decl_stmt|;
comment|// particular region replica id to do reads against if set
specifier|public
name|MultiThreadedReader
parameter_list|(
name|LoadTestDataGenerator
name|dataGen
parameter_list|,
name|Configuration
name|conf
parameter_list|,
name|TableName
name|tableName
parameter_list|,
name|double
name|verifyPercent
parameter_list|)
throws|throws
name|IOException
block|{
name|super
argument_list|(
name|dataGen
argument_list|,
name|conf
argument_list|,
name|tableName
argument_list|,
literal|"R"
argument_list|)
expr_stmt|;
name|this
operator|.
name|verifyPercent
operator|=
name|verifyPercent
expr_stmt|;
block|}
specifier|public
name|void
name|linkToWriter
parameter_list|(
name|MultiThreadedWriterBase
name|writer
parameter_list|)
block|{
name|this
operator|.
name|writer
operator|=
name|writer
expr_stmt|;
name|writer
operator|.
name|setTrackWroteKeys
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|setMaxErrors
parameter_list|(
name|int
name|maxErrors
parameter_list|)
block|{
name|this
operator|.
name|maxErrors
operator|=
name|maxErrors
expr_stmt|;
block|}
specifier|public
name|void
name|setKeyWindow
parameter_list|(
name|int
name|keyWindow
parameter_list|)
block|{
name|this
operator|.
name|keyWindow
operator|=
name|keyWindow
expr_stmt|;
block|}
specifier|public
name|void
name|setMultiGetBatchSize
parameter_list|(
name|int
name|batchSize
parameter_list|)
block|{
name|this
operator|.
name|batchSize
operator|=
name|batchSize
expr_stmt|;
block|}
specifier|public
name|void
name|setRegionReplicaId
parameter_list|(
name|int
name|regionReplicaId
parameter_list|)
block|{
name|this
operator|.
name|regionReplicaId
operator|=
name|regionReplicaId
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|start
parameter_list|(
name|long
name|startKey
parameter_list|,
name|long
name|endKey
parameter_list|,
name|int
name|numThreads
parameter_list|)
throws|throws
name|IOException
block|{
name|super
operator|.
name|start
argument_list|(
name|startKey
argument_list|,
name|endKey
argument_list|,
name|numThreads
argument_list|)
expr_stmt|;
if|if
condition|(
name|verbose
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Reading keys ["
operator|+
name|startKey
operator|+
literal|", "
operator|+
name|endKey
operator|+
literal|")"
argument_list|)
expr_stmt|;
block|}
name|addReaderThreads
argument_list|(
name|numThreads
argument_list|)
expr_stmt|;
name|startThreads
argument_list|(
name|readers
argument_list|)
expr_stmt|;
block|}
specifier|protected
name|void
name|addReaderThreads
parameter_list|(
name|int
name|numThreads
parameter_list|)
throws|throws
name|IOException
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
operator|++
name|i
control|)
block|{
name|HBaseReaderThread
name|reader
init|=
name|createReaderThread
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|readers
operator|.
name|add
argument_list|(
name|reader
argument_list|)
expr_stmt|;
block|}
block|}
specifier|protected
name|HBaseReaderThread
name|createReaderThread
parameter_list|(
name|int
name|readerId
parameter_list|)
throws|throws
name|IOException
block|{
name|HBaseReaderThread
name|reader
init|=
operator|new
name|HBaseReaderThread
argument_list|(
name|readerId
argument_list|)
decl_stmt|;
name|Threads
operator|.
name|setLoggingUncaughtExceptionHandler
argument_list|(
name|reader
argument_list|)
expr_stmt|;
return|return
name|reader
return|;
block|}
specifier|public
class|class
name|HBaseReaderThread
extends|extends
name|Thread
block|{
specifier|protected
specifier|final
name|int
name|readerId
decl_stmt|;
specifier|protected
specifier|final
name|Table
name|table
decl_stmt|;
comment|/** The "current" key being read. Increases from startKey to endKey. */
specifier|private
name|long
name|curKey
decl_stmt|;
comment|/** Time when the thread started */
specifier|protected
name|long
name|startTimeMs
decl_stmt|;
comment|/** If we are ahead of the writer and reading a random key. */
specifier|private
name|boolean
name|readingRandomKey
decl_stmt|;
specifier|private
name|boolean
name|printExceptionTrace
init|=
literal|true
decl_stmt|;
comment|/**      * @param readerId only the keys with this remainder from division by      *          {@link #numThreads} will be read by this thread      */
specifier|public
name|HBaseReaderThread
parameter_list|(
name|int
name|readerId
parameter_list|)
throws|throws
name|IOException
block|{
name|this
operator|.
name|readerId
operator|=
name|readerId
expr_stmt|;
name|table
operator|=
name|createTable
argument_list|()
expr_stmt|;
name|setName
argument_list|(
name|getClass
argument_list|()
operator|.
name|getSimpleName
argument_list|()
operator|+
literal|"_"
operator|+
name|readerId
argument_list|)
expr_stmt|;
block|}
specifier|protected
name|Table
name|createTable
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|connection
operator|.
name|getTable
argument_list|(
name|tableName
argument_list|)
return|;
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
name|runReader
argument_list|()
expr_stmt|;
block|}
finally|finally
block|{
name|closeTable
argument_list|()
expr_stmt|;
name|numThreadsWorking
operator|.
name|decrementAndGet
argument_list|()
expr_stmt|;
block|}
block|}
specifier|protected
name|void
name|closeTable
parameter_list|()
block|{
try|try
block|{
if|if
condition|(
name|table
operator|!=
literal|null
condition|)
block|{
name|table
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
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
literal|"Error closing table"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|runReader
parameter_list|()
block|{
if|if
condition|(
name|verbose
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Started thread #"
operator|+
name|readerId
operator|+
literal|" for reads..."
argument_list|)
expr_stmt|;
block|}
name|startTimeMs
operator|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
expr_stmt|;
name|curKey
operator|=
name|startKey
expr_stmt|;
name|long
index|[]
name|keysForThisReader
init|=
operator|new
name|long
index|[
name|batchSize
index|]
decl_stmt|;
while|while
condition|(
name|curKey
operator|<
name|endKey
operator|&&
operator|!
name|aborted
condition|)
block|{
name|int
name|readingRandomKeyStartIndex
init|=
operator|-
literal|1
decl_stmt|;
name|int
name|numKeys
init|=
literal|0
decl_stmt|;
comment|// if multiGet, loop until we have the number of keys equal to the batch size
do|do
block|{
name|long
name|k
init|=
name|getNextKeyToRead
argument_list|()
decl_stmt|;
if|if
condition|(
name|k
operator|<
name|startKey
operator|||
name|k
operator|>=
name|endKey
condition|)
block|{
name|numReadErrors
operator|.
name|incrementAndGet
argument_list|()
expr_stmt|;
throw|throw
operator|new
name|AssertionError
argument_list|(
literal|"Load tester logic error: proposed key "
operator|+
literal|"to read "
operator|+
name|k
operator|+
literal|" is out of range (startKey="
operator|+
name|startKey
operator|+
literal|", endKey="
operator|+
name|endKey
operator|+
literal|")"
argument_list|)
throw|;
block|}
if|if
condition|(
name|k
operator|%
name|numThreads
operator|!=
name|readerId
operator|||
name|writer
operator|!=
literal|null
operator|&&
name|writer
operator|.
name|failedToWriteKey
argument_list|(
name|k
argument_list|)
condition|)
block|{
comment|// Skip keys that this thread should not read, as well as the keys
comment|// that we know the writer failed to write.
continue|continue;
block|}
name|keysForThisReader
index|[
name|numKeys
index|]
operator|=
name|k
expr_stmt|;
if|if
condition|(
name|readingRandomKey
operator|&&
name|readingRandomKeyStartIndex
operator|==
operator|-
literal|1
condition|)
block|{
comment|//store the first index of a random read
name|readingRandomKeyStartIndex
operator|=
name|numKeys
expr_stmt|;
block|}
name|numKeys
operator|++
expr_stmt|;
block|}
do|while
condition|(
name|numKeys
operator|<
name|batchSize
operator|&&
name|curKey
operator|<
name|endKey
operator|&&
operator|!
name|aborted
condition|)
do|;
if|if
condition|(
name|numKeys
operator|>
literal|0
condition|)
block|{
comment|//meaning there is some key to read
name|readKey
argument_list|(
name|keysForThisReader
argument_list|)
expr_stmt|;
comment|// We have verified some unique key(s).
name|numUniqueKeysVerified
operator|.
name|getAndAdd
argument_list|(
name|readingRandomKeyStartIndex
operator|==
operator|-
literal|1
condition|?
name|numKeys
else|:
name|readingRandomKeyStartIndex
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|/**      * Should only be used for the concurrent writer/reader workload. The      * maximum key we are allowed to read, subject to the "key window"      * constraint.      */
specifier|private
name|long
name|maxKeyWeCanRead
parameter_list|()
block|{
name|long
name|insertedUpToKey
init|=
name|writer
operator|.
name|wroteUpToKey
argument_list|()
decl_stmt|;
if|if
condition|(
name|insertedUpToKey
operator|>=
name|endKey
operator|-
literal|1
condition|)
block|{
comment|// The writer has finished writing our range, so we can read any
comment|// key in the range.
return|return
name|endKey
operator|-
literal|1
return|;
block|}
return|return
name|Math
operator|.
name|min
argument_list|(
name|endKey
operator|-
literal|1
argument_list|,
name|writer
operator|.
name|wroteUpToKey
argument_list|()
operator|-
name|keyWindow
argument_list|)
return|;
block|}
specifier|protected
name|long
name|getNextKeyToRead
parameter_list|()
block|{
name|readingRandomKey
operator|=
literal|false
expr_stmt|;
if|if
condition|(
name|writer
operator|==
literal|null
operator|||
name|curKey
operator|<=
name|maxKeyWeCanRead
argument_list|()
condition|)
block|{
return|return
name|curKey
operator|++
return|;
block|}
comment|// We caught up with the writer. See if we can read any keys at all.
name|long
name|maxKeyToRead
decl_stmt|;
while|while
condition|(
operator|(
name|maxKeyToRead
operator|=
name|maxKeyWeCanRead
argument_list|()
operator|)
operator|<
name|startKey
condition|)
block|{
comment|// The writer has not written sufficient keys for us to be able to read
comment|// anything at all. Sleep a bit. This should only happen in the
comment|// beginning of a load test run.
name|Threads
operator|.
name|sleepWithoutInterrupt
argument_list|(
literal|50
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|curKey
operator|<=
name|maxKeyToRead
condition|)
block|{
comment|// The writer wrote some keys, and we are now allowed to read our
comment|// current key.
return|return
name|curKey
operator|++
return|;
block|}
comment|// startKey<= maxKeyToRead<= curKey - 1. Read one of the previous keys.
comment|// Don't increment the current key -- we still have to try reading it
comment|// later. Set a flag to make sure that we don't count this key towards
comment|// the set of unique keys we have verified.
name|readingRandomKey
operator|=
literal|true
expr_stmt|;
return|return
name|startKey
operator|+
name|Math
operator|.
name|abs
argument_list|(
name|RandomUtils
operator|.
name|nextLong
argument_list|()
argument_list|)
operator|%
operator|(
name|maxKeyToRead
operator|-
name|startKey
operator|+
literal|1
operator|)
return|;
block|}
specifier|private
name|Get
index|[]
name|readKey
parameter_list|(
name|long
index|[]
name|keysToRead
parameter_list|)
block|{
name|Get
index|[]
name|gets
init|=
operator|new
name|Get
index|[
name|keysToRead
operator|.
name|length
index|]
decl_stmt|;
name|int
name|i
init|=
literal|0
decl_stmt|;
for|for
control|(
name|long
name|keyToRead
range|:
name|keysToRead
control|)
block|{
try|try
block|{
name|gets
index|[
name|i
index|]
operator|=
name|createGet
argument_list|(
name|keyToRead
argument_list|)
expr_stmt|;
if|if
condition|(
name|keysToRead
operator|.
name|length
operator|==
literal|1
condition|)
block|{
name|queryKey
argument_list|(
name|gets
index|[
name|i
index|]
argument_list|,
name|RandomUtils
operator|.
name|nextInt
argument_list|(
literal|100
argument_list|)
operator|<
name|verifyPercent
argument_list|,
name|keyToRead
argument_list|)
expr_stmt|;
block|}
name|i
operator|++
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|numReadFailures
operator|.
name|addAndGet
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"["
operator|+
name|readerId
operator|+
literal|"] FAILED read, key = "
operator|+
operator|(
name|keyToRead
operator|+
literal|""
operator|)
operator|+
literal|", time from start: "
operator|+
operator|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|-
name|startTimeMs
operator|)
operator|+
literal|" ms"
argument_list|)
expr_stmt|;
if|if
condition|(
name|printExceptionTrace
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
name|e
argument_list|)
expr_stmt|;
name|printExceptionTrace
operator|=
literal|false
expr_stmt|;
block|}
block|}
block|}
if|if
condition|(
name|keysToRead
operator|.
name|length
operator|>
literal|1
condition|)
block|{
try|try
block|{
name|queryKey
argument_list|(
name|gets
argument_list|,
name|RandomUtils
operator|.
name|nextInt
argument_list|(
literal|100
argument_list|)
operator|<
name|verifyPercent
argument_list|,
name|keysToRead
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|numReadFailures
operator|.
name|addAndGet
argument_list|(
name|gets
operator|.
name|length
argument_list|)
expr_stmt|;
for|for
control|(
name|long
name|keyToRead
range|:
name|keysToRead
control|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"["
operator|+
name|readerId
operator|+
literal|"] FAILED read, key = "
operator|+
operator|(
name|keyToRead
operator|+
literal|""
operator|)
operator|+
literal|", time from start: "
operator|+
operator|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|-
name|startTimeMs
operator|)
operator|+
literal|" ms"
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|printExceptionTrace
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
name|e
argument_list|)
expr_stmt|;
name|printExceptionTrace
operator|=
literal|false
expr_stmt|;
block|}
block|}
block|}
return|return
name|gets
return|;
block|}
specifier|protected
name|Get
name|createGet
parameter_list|(
name|long
name|keyToRead
parameter_list|)
throws|throws
name|IOException
block|{
name|Get
name|get
init|=
operator|new
name|Get
argument_list|(
name|dataGenerator
operator|.
name|getDeterministicUniqueKey
argument_list|(
name|keyToRead
argument_list|)
argument_list|)
decl_stmt|;
name|String
name|cfsString
init|=
literal|""
decl_stmt|;
name|byte
index|[]
index|[]
name|columnFamilies
init|=
name|dataGenerator
operator|.
name|getColumnFamilies
argument_list|()
decl_stmt|;
for|for
control|(
name|byte
index|[]
name|cf
range|:
name|columnFamilies
control|)
block|{
name|get
operator|.
name|addFamily
argument_list|(
name|cf
argument_list|)
expr_stmt|;
if|if
condition|(
name|verbose
condition|)
block|{
if|if
condition|(
name|cfsString
operator|.
name|length
argument_list|()
operator|>
literal|0
condition|)
block|{
name|cfsString
operator|+=
literal|", "
expr_stmt|;
block|}
name|cfsString
operator|+=
literal|"["
operator|+
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|cf
argument_list|)
operator|+
literal|"]"
expr_stmt|;
block|}
block|}
name|get
operator|=
name|dataGenerator
operator|.
name|beforeGet
argument_list|(
name|keyToRead
argument_list|,
name|get
argument_list|)
expr_stmt|;
if|if
condition|(
name|regionReplicaId
operator|>
literal|0
condition|)
block|{
name|get
operator|.
name|setReplicaId
argument_list|(
name|regionReplicaId
argument_list|)
expr_stmt|;
name|get
operator|.
name|setConsistency
argument_list|(
name|Consistency
operator|.
name|TIMELINE
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|verbose
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"["
operator|+
name|readerId
operator|+
literal|"] "
operator|+
literal|"Querying key "
operator|+
name|keyToRead
operator|+
literal|", cfs "
operator|+
name|cfsString
argument_list|)
expr_stmt|;
block|}
return|return
name|get
return|;
block|}
specifier|public
name|void
name|queryKey
parameter_list|(
name|Get
index|[]
name|gets
parameter_list|,
name|boolean
name|verify
parameter_list|,
name|long
index|[]
name|keysToRead
parameter_list|)
throws|throws
name|IOException
block|{
comment|// read the data
name|long
name|start
init|=
name|System
operator|.
name|nanoTime
argument_list|()
decl_stmt|;
comment|// Uses multi/batch gets
name|Result
index|[]
name|results
init|=
name|table
operator|.
name|get
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|gets
argument_list|)
argument_list|)
decl_stmt|;
name|long
name|end
init|=
name|System
operator|.
name|nanoTime
argument_list|()
decl_stmt|;
name|verifyResultsAndUpdateMetrics
argument_list|(
name|verify
argument_list|,
name|gets
argument_list|,
name|end
operator|-
name|start
argument_list|,
name|results
argument_list|,
name|table
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|queryKey
parameter_list|(
name|Get
name|get
parameter_list|,
name|boolean
name|verify
parameter_list|,
name|long
name|keyToRead
parameter_list|)
throws|throws
name|IOException
block|{
comment|// read the data
name|long
name|start
init|=
name|System
operator|.
name|nanoTime
argument_list|()
decl_stmt|;
comment|// Uses simple get
name|Result
name|result
init|=
name|table
operator|.
name|get
argument_list|(
name|get
argument_list|)
decl_stmt|;
name|long
name|end
init|=
name|System
operator|.
name|nanoTime
argument_list|()
decl_stmt|;
name|verifyResultsAndUpdateMetrics
argument_list|(
name|verify
argument_list|,
name|get
argument_list|,
name|end
operator|-
name|start
argument_list|,
name|result
argument_list|,
name|table
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
specifier|protected
name|void
name|verifyResultsAndUpdateMetrics
parameter_list|(
name|boolean
name|verify
parameter_list|,
name|Get
index|[]
name|gets
parameter_list|,
name|long
name|elapsedNano
parameter_list|,
name|Result
index|[]
name|results
parameter_list|,
name|Table
name|table
parameter_list|,
name|boolean
name|isNullExpected
parameter_list|)
throws|throws
name|IOException
block|{
name|totalOpTimeMs
operator|.
name|addAndGet
argument_list|(
name|elapsedNano
operator|/
literal|1000000
argument_list|)
expr_stmt|;
name|numKeys
operator|.
name|addAndGet
argument_list|(
name|gets
operator|.
name|length
argument_list|)
expr_stmt|;
name|int
name|i
init|=
literal|0
decl_stmt|;
for|for
control|(
name|Result
name|result
range|:
name|results
control|)
block|{
name|verifyResultsAndUpdateMetricsOnAPerGetBasis
argument_list|(
name|verify
argument_list|,
name|gets
index|[
name|i
operator|++
index|]
argument_list|,
name|result
argument_list|,
name|table
argument_list|,
name|isNullExpected
argument_list|)
expr_stmt|;
block|}
block|}
specifier|protected
name|void
name|verifyResultsAndUpdateMetrics
parameter_list|(
name|boolean
name|verify
parameter_list|,
name|Get
name|get
parameter_list|,
name|long
name|elapsedNano
parameter_list|,
name|Result
name|result
parameter_list|,
name|Table
name|table
parameter_list|,
name|boolean
name|isNullExpected
parameter_list|)
throws|throws
name|IOException
block|{
name|verifyResultsAndUpdateMetrics
argument_list|(
name|verify
argument_list|,
operator|new
name|Get
index|[]
block|{
name|get
block|}
argument_list|,
name|elapsedNano
argument_list|,
operator|new
name|Result
index|[]
block|{
name|result
block|}
argument_list|,
name|table
argument_list|,
name|isNullExpected
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|verifyResultsAndUpdateMetricsOnAPerGetBasis
parameter_list|(
name|boolean
name|verify
parameter_list|,
name|Get
name|get
parameter_list|,
name|Result
name|result
parameter_list|,
name|Table
name|table
parameter_list|,
name|boolean
name|isNullExpected
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
operator|!
name|result
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
if|if
condition|(
name|verify
condition|)
block|{
name|numKeysVerified
operator|.
name|incrementAndGet
argument_list|()
expr_stmt|;
block|}
block|}
else|else
block|{
name|HRegionLocation
name|hloc
init|=
operator|(
operator|(
name|ClusterConnection
operator|)
name|connection
operator|)
operator|.
name|getRegionLocation
argument_list|(
name|tableName
argument_list|,
name|get
operator|.
name|getRow
argument_list|()
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|String
name|rowKey
init|=
name|Bytes
operator|.
name|toString
argument_list|(
name|get
operator|.
name|getRow
argument_list|()
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Key = "
operator|+
name|rowKey
operator|+
literal|", Region location: "
operator|+
name|hloc
argument_list|)
expr_stmt|;
if|if
condition|(
name|isNullExpected
condition|)
block|{
name|nullResult
operator|.
name|incrementAndGet
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Null result obtained for the key ="
operator|+
name|rowKey
argument_list|)
expr_stmt|;
return|return;
block|}
block|}
name|boolean
name|isOk
init|=
name|verifyResultAgainstDataGenerator
argument_list|(
name|result
argument_list|,
name|verify
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|long
name|numErrorsAfterThis
init|=
literal|0
decl_stmt|;
if|if
condition|(
name|isOk
condition|)
block|{
name|long
name|cols
init|=
literal|0
decl_stmt|;
comment|// Count the columns for reporting purposes.
for|for
control|(
name|byte
index|[]
name|cf
range|:
name|result
operator|.
name|getMap
argument_list|()
operator|.
name|keySet
argument_list|()
control|)
block|{
name|cols
operator|+=
name|result
operator|.
name|getFamilyMap
argument_list|(
name|cf
argument_list|)
operator|.
name|size
argument_list|()
expr_stmt|;
block|}
name|numCols
operator|.
name|addAndGet
argument_list|(
name|cols
argument_list|)
expr_stmt|;
block|}
else|else
block|{
if|if
condition|(
name|writer
operator|!=
literal|null
condition|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"At the time of failure, writer wrote "
operator|+
name|writer
operator|.
name|numKeys
operator|.
name|get
argument_list|()
operator|+
literal|" keys"
argument_list|)
expr_stmt|;
block|}
name|numErrorsAfterThis
operator|=
name|numReadErrors
operator|.
name|incrementAndGet
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|numErrorsAfterThis
operator|>
name|maxErrors
condition|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Aborting readers -- found more than "
operator|+
name|maxErrors
operator|+
literal|" errors"
argument_list|)
expr_stmt|;
name|aborted
operator|=
literal|true
expr_stmt|;
block|}
block|}
block|}
specifier|public
name|long
name|getNumReadFailures
parameter_list|()
block|{
return|return
name|numReadFailures
operator|.
name|get
argument_list|()
return|;
block|}
specifier|public
name|long
name|getNumReadErrors
parameter_list|()
block|{
return|return
name|numReadErrors
operator|.
name|get
argument_list|()
return|;
block|}
specifier|public
name|long
name|getNumKeysVerified
parameter_list|()
block|{
return|return
name|numKeysVerified
operator|.
name|get
argument_list|()
return|;
block|}
specifier|public
name|long
name|getNumUniqueKeysVerified
parameter_list|()
block|{
return|return
name|numUniqueKeysVerified
operator|.
name|get
argument_list|()
return|;
block|}
specifier|public
name|long
name|getNullResultsCount
parameter_list|()
block|{
return|return
name|nullResult
operator|.
name|get
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|protected
name|String
name|progressInfo
parameter_list|()
block|{
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|appendToStatus
argument_list|(
name|sb
argument_list|,
literal|"verified"
argument_list|,
name|numKeysVerified
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
name|appendToStatus
argument_list|(
name|sb
argument_list|,
literal|"READ FAILURES"
argument_list|,
name|numReadFailures
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
name|appendToStatus
argument_list|(
name|sb
argument_list|,
literal|"READ ERRORS"
argument_list|,
name|numReadErrors
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
name|appendToStatus
argument_list|(
name|sb
argument_list|,
literal|"NULL RESULT"
argument_list|,
name|nullResult
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|sb
operator|.
name|toString
argument_list|()
return|;
block|}
block|}
end_class

end_unit

