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
name|io
operator|.
name|PrintWriter
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|StringWriter
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
name|PriorityQueue
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Queue
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
name|ArrayBlockingQueue
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
name|BlockingQueue
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
name|ConcurrentSkipListSet
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
name|util
operator|.
name|StringUtils
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
name|client
operator|.
name|HTable
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
name|client
operator|.
name|RetriesExhaustedWithDetailsException
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
comment|/** Creates multiple threads that write key/values into the */
end_comment

begin_class
specifier|public
class|class
name|MultiThreadedWriter
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
name|MultiThreadedWriter
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|Set
argument_list|<
name|HBaseWriterThread
argument_list|>
name|writers
init|=
operator|new
name|HashSet
argument_list|<
name|HBaseWriterThread
argument_list|>
argument_list|()
decl_stmt|;
specifier|private
name|boolean
name|isMultiPut
init|=
literal|false
decl_stmt|;
comment|/**    * A temporary place to keep track of inserted keys. This is written to by    * all writers and is drained on a separate thread that populates    * {@link #insertedUpToKey}, the maximum key in the contiguous range of keys    * being inserted. This queue is supposed to stay small.    */
specifier|private
name|BlockingQueue
argument_list|<
name|Long
argument_list|>
name|insertedKeys
init|=
operator|new
name|ArrayBlockingQueue
argument_list|<
name|Long
argument_list|>
argument_list|(
literal|10000
argument_list|)
decl_stmt|;
comment|/**    * This is the current key to be inserted by any thread. Each thread does an    * atomic get and increment operation and inserts the current value.    */
specifier|private
name|AtomicLong
name|nextKeyToInsert
init|=
operator|new
name|AtomicLong
argument_list|()
decl_stmt|;
comment|/**    * The highest key in the contiguous range of keys .    */
specifier|private
name|AtomicLong
name|insertedUpToKey
init|=
operator|new
name|AtomicLong
argument_list|()
decl_stmt|;
comment|/** The sorted set of keys NOT inserted by the writers */
specifier|private
name|Set
argument_list|<
name|Long
argument_list|>
name|failedKeySet
init|=
operator|new
name|ConcurrentSkipListSet
argument_list|<
name|Long
argument_list|>
argument_list|()
decl_stmt|;
comment|/**    * The total size of the temporary inserted key set that have not yet lined    * up in a our contiguous sequence starting from startKey. Supposed to stay    * small.    */
specifier|private
name|AtomicLong
name|insertedKeyQueueSize
init|=
operator|new
name|AtomicLong
argument_list|()
decl_stmt|;
comment|/** Enable this if used in conjunction with a concurrent reader. */
specifier|private
name|boolean
name|trackInsertedKeys
decl_stmt|;
specifier|public
name|MultiThreadedWriter
parameter_list|(
name|LoadTestDataGenerator
name|dataGen
parameter_list|,
name|Configuration
name|conf
parameter_list|,
name|TableName
name|tableName
parameter_list|)
block|{
name|super
argument_list|(
name|dataGen
argument_list|,
name|conf
argument_list|,
name|tableName
argument_list|,
literal|"W"
argument_list|)
expr_stmt|;
block|}
comment|/** Use multi-puts vs. separate puts for every column in a row */
specifier|public
name|void
name|setMultiPut
parameter_list|(
name|boolean
name|isMultiPut
parameter_list|)
block|{
name|this
operator|.
name|isMultiPut
operator|=
name|isMultiPut
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
literal|"Inserting keys ["
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
name|nextKeyToInsert
operator|.
name|set
argument_list|(
name|startKey
argument_list|)
expr_stmt|;
name|insertedUpToKey
operator|.
name|set
argument_list|(
name|startKey
operator|-
literal|1
argument_list|)
expr_stmt|;
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
name|HBaseWriterThread
name|writer
init|=
operator|new
name|HBaseWriterThread
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|writers
operator|.
name|add
argument_list|(
name|writer
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|trackInsertedKeys
condition|)
block|{
operator|new
name|Thread
argument_list|(
operator|new
name|InsertedKeysTracker
argument_list|()
argument_list|)
operator|.
name|start
argument_list|()
expr_stmt|;
name|numThreadsWorking
operator|.
name|incrementAndGet
argument_list|()
expr_stmt|;
block|}
name|startThreads
argument_list|(
name|writers
argument_list|)
expr_stmt|;
block|}
specifier|private
class|class
name|HBaseWriterThread
extends|extends
name|Thread
block|{
specifier|private
specifier|final
name|HTable
name|table
decl_stmt|;
specifier|public
name|HBaseWriterThread
parameter_list|(
name|int
name|writerId
parameter_list|)
throws|throws
name|IOException
block|{
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
name|writerId
argument_list|)
expr_stmt|;
name|table
operator|=
operator|new
name|HTable
argument_list|(
name|conf
argument_list|,
name|tableName
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|run
parameter_list|()
block|{
try|try
block|{
name|long
name|rowKeyBase
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
while|while
condition|(
operator|(
name|rowKeyBase
operator|=
name|nextKeyToInsert
operator|.
name|getAndIncrement
argument_list|()
operator|)
operator|<
name|endKey
condition|)
block|{
name|byte
index|[]
name|rowKey
init|=
name|dataGenerator
operator|.
name|getDeterministicUniqueKey
argument_list|(
name|rowKeyBase
argument_list|)
decl_stmt|;
name|Put
name|put
init|=
operator|new
name|Put
argument_list|(
name|rowKey
argument_list|)
decl_stmt|;
name|numKeys
operator|.
name|addAndGet
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|int
name|columnCount
init|=
literal|0
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
name|String
name|s
decl_stmt|;
name|byte
index|[]
index|[]
name|columns
init|=
name|dataGenerator
operator|.
name|generateColumnsForCf
argument_list|(
name|rowKey
argument_list|,
name|cf
argument_list|)
decl_stmt|;
for|for
control|(
name|byte
index|[]
name|column
range|:
name|columns
control|)
block|{
name|byte
index|[]
name|value
init|=
name|dataGenerator
operator|.
name|generateValue
argument_list|(
name|rowKey
argument_list|,
name|cf
argument_list|,
name|column
argument_list|)
decl_stmt|;
name|put
operator|.
name|add
argument_list|(
name|cf
argument_list|,
name|column
argument_list|,
name|value
argument_list|)
expr_stmt|;
operator|++
name|columnCount
expr_stmt|;
if|if
condition|(
operator|!
name|isMultiPut
condition|)
block|{
name|insert
argument_list|(
name|table
argument_list|,
name|put
argument_list|,
name|rowKeyBase
argument_list|)
expr_stmt|;
name|numCols
operator|.
name|addAndGet
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|put
operator|=
operator|new
name|Put
argument_list|(
name|rowKey
argument_list|)
expr_stmt|;
block|}
block|}
block|}
if|if
condition|(
name|isMultiPut
condition|)
block|{
if|if
condition|(
name|verbose
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Preparing put for key = ["
operator|+
name|rowKey
operator|+
literal|"], "
operator|+
name|columnCount
operator|+
literal|" columns"
argument_list|)
expr_stmt|;
block|}
name|insert
argument_list|(
name|table
argument_list|,
name|put
argument_list|,
name|rowKeyBase
argument_list|)
expr_stmt|;
name|numCols
operator|.
name|addAndGet
argument_list|(
name|columnCount
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|trackInsertedKeys
condition|)
block|{
name|insertedKeys
operator|.
name|add
argument_list|(
name|rowKeyBase
argument_list|)
expr_stmt|;
block|}
block|}
block|}
finally|finally
block|{
try|try
block|{
name|table
operator|.
name|close
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
literal|"Error closing table"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
name|numThreadsWorking
operator|.
name|decrementAndGet
argument_list|()
expr_stmt|;
block|}
block|}
block|}
specifier|public
name|void
name|insert
parameter_list|(
name|HTable
name|table
parameter_list|,
name|Put
name|put
parameter_list|,
name|long
name|keyBase
parameter_list|)
block|{
name|long
name|start
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
try|try
block|{
name|table
operator|.
name|put
argument_list|(
name|put
argument_list|)
expr_stmt|;
name|totalOpTimeMs
operator|.
name|addAndGet
argument_list|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|-
name|start
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|failedKeySet
operator|.
name|add
argument_list|(
name|keyBase
argument_list|)
expr_stmt|;
name|String
name|exceptionInfo
decl_stmt|;
if|if
condition|(
name|e
operator|instanceof
name|RetriesExhaustedWithDetailsException
condition|)
block|{
name|RetriesExhaustedWithDetailsException
name|aggEx
init|=
operator|(
name|RetriesExhaustedWithDetailsException
operator|)
name|e
decl_stmt|;
name|exceptionInfo
operator|=
name|aggEx
operator|.
name|getExhaustiveDescription
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|StringWriter
name|stackWriter
init|=
operator|new
name|StringWriter
argument_list|()
decl_stmt|;
name|PrintWriter
name|pw
init|=
operator|new
name|PrintWriter
argument_list|(
name|stackWriter
argument_list|)
decl_stmt|;
name|e
operator|.
name|printStackTrace
argument_list|(
name|pw
argument_list|)
expr_stmt|;
name|pw
operator|.
name|flush
argument_list|()
expr_stmt|;
name|exceptionInfo
operator|=
name|StringUtils
operator|.
name|stringifyException
argument_list|(
name|e
argument_list|)
expr_stmt|;
block|}
name|LOG
operator|.
name|error
argument_list|(
literal|"Failed to insert: "
operator|+
name|keyBase
operator|+
literal|" after "
operator|+
operator|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|-
name|start
operator|)
operator|+
literal|"ms; region information: "
operator|+
name|getRegionDebugInfoSafe
argument_list|(
name|table
argument_list|,
name|put
operator|.
name|getRow
argument_list|()
argument_list|)
operator|+
literal|"; errors: "
operator|+
name|exceptionInfo
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|String
name|getRegionDebugInfoSafe
parameter_list|(
name|HTable
name|table
parameter_list|,
name|byte
index|[]
name|rowKey
parameter_list|)
block|{
name|HRegionLocation
name|cached
init|=
literal|null
decl_stmt|,
name|real
init|=
literal|null
decl_stmt|;
try|try
block|{
name|cached
operator|=
name|table
operator|.
name|getRegionLocation
argument_list|(
name|rowKey
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|real
operator|=
name|table
operator|.
name|getRegionLocation
argument_list|(
name|rowKey
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
comment|// Cannot obtain region information for another catch block - too bad!
block|}
name|String
name|result
init|=
literal|"no information can be obtained"
decl_stmt|;
if|if
condition|(
name|cached
operator|!=
literal|null
condition|)
block|{
name|result
operator|=
literal|"cached: "
operator|+
name|cached
operator|.
name|toString
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|real
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|real
operator|.
name|equals
argument_list|(
name|cached
argument_list|)
condition|)
block|{
name|result
operator|+=
literal|"; cache is up to date"
expr_stmt|;
block|}
else|else
block|{
name|result
operator|=
operator|(
name|cached
operator|!=
literal|null
operator|)
condition|?
operator|(
name|result
operator|+
literal|"; "
operator|)
else|:
literal|""
expr_stmt|;
name|result
operator|+=
literal|"real: "
operator|+
name|real
operator|.
name|toString
argument_list|()
expr_stmt|;
block|}
block|}
return|return
name|result
return|;
block|}
comment|/**    * A thread that keeps track of the highest key in the contiguous range of    * inserted keys.    */
specifier|private
class|class
name|InsertedKeysTracker
implements|implements
name|Runnable
block|{
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
name|Thread
operator|.
name|currentThread
argument_list|()
operator|.
name|setName
argument_list|(
name|getClass
argument_list|()
operator|.
name|getSimpleName
argument_list|()
argument_list|)
expr_stmt|;
try|try
block|{
name|long
name|expectedKey
init|=
name|startKey
decl_stmt|;
name|Queue
argument_list|<
name|Long
argument_list|>
name|sortedKeys
init|=
operator|new
name|PriorityQueue
argument_list|<
name|Long
argument_list|>
argument_list|()
decl_stmt|;
while|while
condition|(
name|expectedKey
operator|<
name|endKey
condition|)
block|{
comment|// Block until a new element is available.
name|Long
name|k
decl_stmt|;
try|try
block|{
name|k
operator|=
name|insertedKeys
operator|.
name|poll
argument_list|(
literal|1
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
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
name|info
argument_list|(
literal|"Inserted key tracker thread interrupted"
argument_list|,
name|e
argument_list|)
expr_stmt|;
break|break;
block|}
if|if
condition|(
name|k
operator|==
literal|null
condition|)
block|{
continue|continue;
block|}
if|if
condition|(
name|k
operator|==
name|expectedKey
condition|)
block|{
comment|// Skip the "sorted key" queue and consume this key.
name|insertedUpToKey
operator|.
name|set
argument_list|(
name|k
argument_list|)
expr_stmt|;
operator|++
name|expectedKey
expr_stmt|;
block|}
else|else
block|{
name|sortedKeys
operator|.
name|add
argument_list|(
name|k
argument_list|)
expr_stmt|;
block|}
comment|// See if we have a sequence of contiguous keys lined up.
while|while
condition|(
operator|!
name|sortedKeys
operator|.
name|isEmpty
argument_list|()
operator|&&
operator|(
operator|(
name|k
operator|=
name|sortedKeys
operator|.
name|peek
argument_list|()
operator|)
operator|==
name|expectedKey
operator|)
condition|)
block|{
name|sortedKeys
operator|.
name|poll
argument_list|()
expr_stmt|;
name|insertedUpToKey
operator|.
name|set
argument_list|(
name|k
argument_list|)
expr_stmt|;
operator|++
name|expectedKey
expr_stmt|;
block|}
name|insertedKeyQueueSize
operator|.
name|set
argument_list|(
name|insertedKeys
operator|.
name|size
argument_list|()
operator|+
name|sortedKeys
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|Exception
name|ex
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Error in inserted key tracker"
argument_list|,
name|ex
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|numThreadsWorking
operator|.
name|decrementAndGet
argument_list|()
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|waitForFinish
parameter_list|()
block|{
name|super
operator|.
name|waitForFinish
argument_list|()
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Failed to write keys: "
operator|+
name|failedKeySet
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|Long
name|key
range|:
name|failedKeySet
control|)
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Failed to write key: "
operator|+
name|key
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|int
name|getNumWriteFailures
parameter_list|()
block|{
return|return
name|failedKeySet
operator|.
name|size
argument_list|()
return|;
block|}
comment|/**    * The max key until which all keys have been inserted (successfully or not).    * @return the last key that we have inserted all keys up to (inclusive)    */
specifier|public
name|long
name|insertedUpToKey
parameter_list|()
block|{
return|return
name|insertedUpToKey
operator|.
name|get
argument_list|()
return|;
block|}
specifier|public
name|boolean
name|failedToWriteKey
parameter_list|(
name|long
name|k
parameter_list|)
block|{
return|return
name|failedKeySet
operator|.
name|contains
argument_list|(
name|k
argument_list|)
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
literal|"insertedUpTo"
argument_list|,
name|insertedUpToKey
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
name|appendToStatus
argument_list|(
name|sb
argument_list|,
literal|"insertedQSize"
argument_list|,
name|insertedKeyQueueSize
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
comment|/**    * Used for a joint write/read workload. Enables tracking the last inserted    * key, which requires a blocking queue and a consumer thread.    * @param enable whether to enable tracking the last inserted key    */
specifier|public
name|void
name|setTrackInsertedKeys
parameter_list|(
name|boolean
name|enable
parameter_list|)
block|{
name|trackInsertedKeys
operator|=
name|enable
expr_stmt|;
block|}
block|}
end_class

end_unit

