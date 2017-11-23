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
name|List
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Random
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
name|LinkedBlockingQueue
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
name|java
operator|.
name|util
operator|.
name|stream
operator|.
name|Stream
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
name|cli
operator|.
name|CommandLine
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
name|MultithreadedTestUtil
operator|.
name|RepeatingTestThread
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
name|MultithreadedTestUtil
operator|.
name|TestContext
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
name|Admin
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
name|ColumnFamilyDescriptor
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
name|ColumnFamilyDescriptorBuilder
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
name|Connection
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
name|ConnectionFactory
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
name|ResultScanner
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
name|Scan
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
name|client
operator|.
name|TableDescriptorBuilder
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
name|AbstractHBaseTool
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
name|util
operator|.
name|ToolRunner
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
name|hadoop
operator|.
name|hbase
operator|.
name|shaded
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
comment|/**  * A test tool that uses multiple threads to read and write multifamily rows into a table, verifying  * that reads never see partially-complete writes  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|AcidGuaranteesTestTool
extends|extends
name|AbstractHBaseTool
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
name|AcidGuaranteesTestTool
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|TableName
name|TABLE_NAME
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"TestAcidGuarantees"
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|byte
index|[]
name|FAMILY_A
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"A"
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|byte
index|[]
name|FAMILY_B
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"B"
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|byte
index|[]
name|FAMILY_C
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"C"
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|byte
index|[]
name|QUALIFIER_NAME
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"data"
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|byte
index|[]
index|[]
name|FAMILIES
init|=
operator|new
name|byte
index|[]
index|[]
block|{
name|FAMILY_A
block|,
name|FAMILY_B
block|,
name|FAMILY_C
block|}
decl_stmt|;
specifier|public
specifier|static
name|int
name|NUM_COLS_TO_CHECK
init|=
literal|50
decl_stmt|;
specifier|private
name|ExecutorService
name|sharedPool
decl_stmt|;
specifier|private
name|long
name|millisToRun
decl_stmt|;
specifier|private
name|int
name|numWriters
decl_stmt|;
specifier|private
name|int
name|numGetters
decl_stmt|;
specifier|private
name|int
name|numScanners
decl_stmt|;
specifier|private
name|int
name|numUniqueRows
decl_stmt|;
specifier|private
name|boolean
name|crazyFlush
decl_stmt|;
specifier|private
name|boolean
name|useMob
decl_stmt|;
specifier|private
name|ExecutorService
name|createThreadPool
parameter_list|()
block|{
name|int
name|maxThreads
init|=
literal|256
decl_stmt|;
name|int
name|coreThreads
init|=
literal|128
decl_stmt|;
name|long
name|keepAliveTime
init|=
literal|60
decl_stmt|;
name|BlockingQueue
argument_list|<
name|Runnable
argument_list|>
name|workQueue
init|=
operator|new
name|LinkedBlockingQueue
argument_list|<
name|Runnable
argument_list|>
argument_list|(
name|maxThreads
operator|*
name|HConstants
operator|.
name|DEFAULT_HBASE_CLIENT_MAX_TOTAL_TASKS
argument_list|)
decl_stmt|;
name|ThreadPoolExecutor
name|tpe
init|=
operator|new
name|ThreadPoolExecutor
argument_list|(
name|coreThreads
argument_list|,
name|maxThreads
argument_list|,
name|keepAliveTime
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|,
name|workQueue
argument_list|,
name|Threads
operator|.
name|newDaemonThreadFactory
argument_list|(
name|toString
argument_list|()
operator|+
literal|"-shared"
argument_list|)
argument_list|)
decl_stmt|;
name|tpe
operator|.
name|allowCoreThreadTimeOut
argument_list|(
literal|true
argument_list|)
expr_stmt|;
return|return
name|tpe
return|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|addOptions
parameter_list|()
block|{
name|addOptWithArg
argument_list|(
literal|"millis"
argument_list|,
literal|"time limit in milliseconds"
argument_list|)
expr_stmt|;
name|addOptWithArg
argument_list|(
literal|"numWriters"
argument_list|,
literal|"number of write threads"
argument_list|)
expr_stmt|;
name|addOptWithArg
argument_list|(
literal|"numGetters"
argument_list|,
literal|"number of get threads"
argument_list|)
expr_stmt|;
name|addOptWithArg
argument_list|(
literal|"numScanners"
argument_list|,
literal|"number of scan threads"
argument_list|)
expr_stmt|;
name|addOptWithArg
argument_list|(
literal|"numUniqueRows"
argument_list|,
literal|"number of unique rows to test"
argument_list|)
expr_stmt|;
name|addOptNoArg
argument_list|(
literal|"crazyFlush"
argument_list|,
literal|"if specified we will flush continuously otherwise will flush every minute"
argument_list|)
expr_stmt|;
name|addOptNoArg
argument_list|(
literal|"useMob"
argument_list|,
literal|"if specified we will enable mob on the first column family"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|processOptions
parameter_list|(
name|CommandLine
name|cmd
parameter_list|)
block|{
name|millisToRun
operator|=
name|getOptionAsLong
argument_list|(
name|cmd
argument_list|,
literal|"millis"
argument_list|,
literal|5000
argument_list|)
expr_stmt|;
name|numWriters
operator|=
name|getOptionAsInt
argument_list|(
name|cmd
argument_list|,
literal|"numWriters"
argument_list|,
literal|50
argument_list|)
expr_stmt|;
name|numGetters
operator|=
name|getOptionAsInt
argument_list|(
name|cmd
argument_list|,
literal|"numGetters"
argument_list|,
literal|2
argument_list|)
expr_stmt|;
name|numScanners
operator|=
name|getOptionAsInt
argument_list|(
name|cmd
argument_list|,
literal|"numScanners"
argument_list|,
literal|2
argument_list|)
expr_stmt|;
name|numUniqueRows
operator|=
name|getOptionAsInt
argument_list|(
name|cmd
argument_list|,
literal|"numUniqueRows"
argument_list|,
literal|3
argument_list|)
expr_stmt|;
name|crazyFlush
operator|=
name|cmd
operator|.
name|hasOption
argument_list|(
literal|"crazyFlush"
argument_list|)
expr_stmt|;
name|useMob
operator|=
name|cmd
operator|.
name|hasOption
argument_list|(
literal|"useMob"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|int
name|doWork
parameter_list|()
throws|throws
name|Exception
block|{
name|sharedPool
operator|=
name|createThreadPool
argument_list|()
expr_stmt|;
try|try
init|(
name|Connection
name|conn
init|=
name|ConnectionFactory
operator|.
name|createConnection
argument_list|(
name|getConf
argument_list|()
argument_list|)
init|)
block|{
name|runTestAtomicity
argument_list|(
name|conn
operator|.
name|getAdmin
argument_list|()
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|sharedPool
operator|.
name|shutdown
argument_list|()
expr_stmt|;
block|}
return|return
literal|0
return|;
block|}
comment|/**    * Thread that does random full-row writes into a table.    */
specifier|public
specifier|static
class|class
name|AtomicityWriter
extends|extends
name|RepeatingTestThread
block|{
name|Random
name|rand
init|=
operator|new
name|Random
argument_list|()
decl_stmt|;
name|byte
name|data
index|[]
init|=
operator|new
name|byte
index|[
literal|10
index|]
decl_stmt|;
name|byte
index|[]
index|[]
name|targetRows
decl_stmt|;
name|byte
index|[]
index|[]
name|targetFamilies
decl_stmt|;
name|Connection
name|connection
decl_stmt|;
name|Table
name|table
decl_stmt|;
name|AtomicLong
name|numWritten
init|=
operator|new
name|AtomicLong
argument_list|()
decl_stmt|;
specifier|public
name|AtomicityWriter
parameter_list|(
name|TestContext
name|ctx
parameter_list|,
name|byte
index|[]
index|[]
name|targetRows
parameter_list|,
name|byte
index|[]
index|[]
name|targetFamilies
parameter_list|,
name|ExecutorService
name|pool
parameter_list|)
throws|throws
name|IOException
block|{
name|super
argument_list|(
name|ctx
argument_list|)
expr_stmt|;
name|this
operator|.
name|targetRows
operator|=
name|targetRows
expr_stmt|;
name|this
operator|.
name|targetFamilies
operator|=
name|targetFamilies
expr_stmt|;
name|connection
operator|=
name|ConnectionFactory
operator|.
name|createConnection
argument_list|(
name|ctx
operator|.
name|getConf
argument_list|()
argument_list|,
name|pool
argument_list|)
expr_stmt|;
name|table
operator|=
name|connection
operator|.
name|getTable
argument_list|(
name|TABLE_NAME
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|doAnAction
parameter_list|()
throws|throws
name|Exception
block|{
comment|// Pick a random row to write into
name|byte
index|[]
name|targetRow
init|=
name|targetRows
index|[
name|rand
operator|.
name|nextInt
argument_list|(
name|targetRows
operator|.
name|length
argument_list|)
index|]
decl_stmt|;
name|Put
name|p
init|=
operator|new
name|Put
argument_list|(
name|targetRow
argument_list|)
decl_stmt|;
name|rand
operator|.
name|nextBytes
argument_list|(
name|data
argument_list|)
expr_stmt|;
for|for
control|(
name|byte
index|[]
name|family
range|:
name|targetFamilies
control|)
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
name|NUM_COLS_TO_CHECK
condition|;
name|i
operator|++
control|)
block|{
name|byte
name|qualifier
index|[]
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"col"
operator|+
name|i
argument_list|)
decl_stmt|;
name|p
operator|.
name|addColumn
argument_list|(
name|family
argument_list|,
name|qualifier
argument_list|,
name|data
argument_list|)
expr_stmt|;
block|}
block|}
name|table
operator|.
name|put
argument_list|(
name|p
argument_list|)
expr_stmt|;
name|numWritten
operator|.
name|getAndIncrement
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|workDone
parameter_list|()
throws|throws
name|IOException
block|{
try|try
block|{
name|table
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
finally|finally
block|{
name|connection
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
block|}
comment|/**    * Thread that does single-row reads in a table, looking for partially completed rows.    */
specifier|public
specifier|static
class|class
name|AtomicGetReader
extends|extends
name|RepeatingTestThread
block|{
name|byte
index|[]
name|targetRow
decl_stmt|;
name|byte
index|[]
index|[]
name|targetFamilies
decl_stmt|;
name|Connection
name|connection
decl_stmt|;
name|Table
name|table
decl_stmt|;
name|int
name|numVerified
init|=
literal|0
decl_stmt|;
name|AtomicLong
name|numRead
init|=
operator|new
name|AtomicLong
argument_list|()
decl_stmt|;
specifier|public
name|AtomicGetReader
parameter_list|(
name|TestContext
name|ctx
parameter_list|,
name|byte
index|[]
name|targetRow
parameter_list|,
name|byte
index|[]
index|[]
name|targetFamilies
parameter_list|,
name|ExecutorService
name|pool
parameter_list|)
throws|throws
name|IOException
block|{
name|super
argument_list|(
name|ctx
argument_list|)
expr_stmt|;
name|this
operator|.
name|targetRow
operator|=
name|targetRow
expr_stmt|;
name|this
operator|.
name|targetFamilies
operator|=
name|targetFamilies
expr_stmt|;
name|connection
operator|=
name|ConnectionFactory
operator|.
name|createConnection
argument_list|(
name|ctx
operator|.
name|getConf
argument_list|()
argument_list|,
name|pool
argument_list|)
expr_stmt|;
name|table
operator|=
name|connection
operator|.
name|getTable
argument_list|(
name|TABLE_NAME
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|doAnAction
parameter_list|()
throws|throws
name|Exception
block|{
name|Get
name|g
init|=
operator|new
name|Get
argument_list|(
name|targetRow
argument_list|)
decl_stmt|;
name|Result
name|res
init|=
name|table
operator|.
name|get
argument_list|(
name|g
argument_list|)
decl_stmt|;
name|byte
index|[]
name|gotValue
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|res
operator|.
name|getRow
argument_list|()
operator|==
literal|null
condition|)
block|{
comment|// Trying to verify but we didn't find the row - the writing
comment|// thread probably just hasn't started writing yet, so we can
comment|// ignore this action
return|return;
block|}
for|for
control|(
name|byte
index|[]
name|family
range|:
name|targetFamilies
control|)
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
name|NUM_COLS_TO_CHECK
condition|;
name|i
operator|++
control|)
block|{
name|byte
name|qualifier
index|[]
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"col"
operator|+
name|i
argument_list|)
decl_stmt|;
name|byte
name|thisValue
index|[]
init|=
name|res
operator|.
name|getValue
argument_list|(
name|family
argument_list|,
name|qualifier
argument_list|)
decl_stmt|;
if|if
condition|(
name|gotValue
operator|!=
literal|null
operator|&&
operator|!
name|Bytes
operator|.
name|equals
argument_list|(
name|gotValue
argument_list|,
name|thisValue
argument_list|)
condition|)
block|{
name|gotFailure
argument_list|(
name|gotValue
argument_list|,
name|res
argument_list|)
expr_stmt|;
block|}
name|numVerified
operator|++
expr_stmt|;
name|gotValue
operator|=
name|thisValue
expr_stmt|;
block|}
block|}
name|numRead
operator|.
name|getAndIncrement
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|workDone
parameter_list|()
throws|throws
name|IOException
block|{
try|try
block|{
name|table
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
finally|finally
block|{
name|connection
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|gotFailure
parameter_list|(
name|byte
index|[]
name|expected
parameter_list|,
name|Result
name|res
parameter_list|)
block|{
name|StringBuilder
name|msg
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|msg
operator|.
name|append
argument_list|(
literal|"Failed after "
argument_list|)
operator|.
name|append
argument_list|(
name|numVerified
argument_list|)
operator|.
name|append
argument_list|(
literal|"!"
argument_list|)
expr_stmt|;
name|msg
operator|.
name|append
argument_list|(
literal|"Expected="
argument_list|)
operator|.
name|append
argument_list|(
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|expected
argument_list|)
argument_list|)
expr_stmt|;
name|msg
operator|.
name|append
argument_list|(
literal|"Got:\n"
argument_list|)
expr_stmt|;
for|for
control|(
name|Cell
name|kv
range|:
name|res
operator|.
name|listCells
argument_list|()
control|)
block|{
name|msg
operator|.
name|append
argument_list|(
name|kv
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|msg
operator|.
name|append
argument_list|(
literal|" val= "
argument_list|)
expr_stmt|;
name|msg
operator|.
name|append
argument_list|(
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|CellUtil
operator|.
name|cloneValue
argument_list|(
name|kv
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|msg
operator|.
name|append
argument_list|(
literal|"\n"
argument_list|)
expr_stmt|;
block|}
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|msg
operator|.
name|toString
argument_list|()
argument_list|)
throw|;
block|}
block|}
comment|/**    * Thread that does full scans of the table looking for any partially completed rows.    */
specifier|public
specifier|static
class|class
name|AtomicScanReader
extends|extends
name|RepeatingTestThread
block|{
name|byte
index|[]
index|[]
name|targetFamilies
decl_stmt|;
name|Table
name|table
decl_stmt|;
name|Connection
name|connection
decl_stmt|;
name|AtomicLong
name|numScans
init|=
operator|new
name|AtomicLong
argument_list|()
decl_stmt|;
name|AtomicLong
name|numRowsScanned
init|=
operator|new
name|AtomicLong
argument_list|()
decl_stmt|;
specifier|public
name|AtomicScanReader
parameter_list|(
name|TestContext
name|ctx
parameter_list|,
name|byte
index|[]
index|[]
name|targetFamilies
parameter_list|,
name|ExecutorService
name|pool
parameter_list|)
throws|throws
name|IOException
block|{
name|super
argument_list|(
name|ctx
argument_list|)
expr_stmt|;
name|this
operator|.
name|targetFamilies
operator|=
name|targetFamilies
expr_stmt|;
name|connection
operator|=
name|ConnectionFactory
operator|.
name|createConnection
argument_list|(
name|ctx
operator|.
name|getConf
argument_list|()
argument_list|,
name|pool
argument_list|)
expr_stmt|;
name|table
operator|=
name|connection
operator|.
name|getTable
argument_list|(
name|TABLE_NAME
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|doAnAction
parameter_list|()
throws|throws
name|Exception
block|{
name|Scan
name|s
init|=
operator|new
name|Scan
argument_list|()
decl_stmt|;
for|for
control|(
name|byte
index|[]
name|family
range|:
name|targetFamilies
control|)
block|{
name|s
operator|.
name|addFamily
argument_list|(
name|family
argument_list|)
expr_stmt|;
block|}
name|ResultScanner
name|scanner
init|=
name|table
operator|.
name|getScanner
argument_list|(
name|s
argument_list|)
decl_stmt|;
for|for
control|(
name|Result
name|res
range|:
name|scanner
control|)
block|{
name|byte
index|[]
name|gotValue
init|=
literal|null
decl_stmt|;
for|for
control|(
name|byte
index|[]
name|family
range|:
name|targetFamilies
control|)
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
name|NUM_COLS_TO_CHECK
condition|;
name|i
operator|++
control|)
block|{
name|byte
name|qualifier
index|[]
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"col"
operator|+
name|i
argument_list|)
decl_stmt|;
name|byte
name|thisValue
index|[]
init|=
name|res
operator|.
name|getValue
argument_list|(
name|family
argument_list|,
name|qualifier
argument_list|)
decl_stmt|;
if|if
condition|(
name|gotValue
operator|!=
literal|null
operator|&&
operator|!
name|Bytes
operator|.
name|equals
argument_list|(
name|gotValue
argument_list|,
name|thisValue
argument_list|)
condition|)
block|{
name|gotFailure
argument_list|(
name|gotValue
argument_list|,
name|res
argument_list|)
expr_stmt|;
block|}
name|gotValue
operator|=
name|thisValue
expr_stmt|;
block|}
block|}
name|numRowsScanned
operator|.
name|getAndIncrement
argument_list|()
expr_stmt|;
block|}
name|numScans
operator|.
name|getAndIncrement
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|workDone
parameter_list|()
throws|throws
name|IOException
block|{
try|try
block|{
name|table
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
finally|finally
block|{
name|connection
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|gotFailure
parameter_list|(
name|byte
index|[]
name|expected
parameter_list|,
name|Result
name|res
parameter_list|)
block|{
name|StringBuilder
name|msg
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|msg
operator|.
name|append
argument_list|(
literal|"Failed after "
argument_list|)
operator|.
name|append
argument_list|(
name|numRowsScanned
argument_list|)
operator|.
name|append
argument_list|(
literal|"!"
argument_list|)
expr_stmt|;
name|msg
operator|.
name|append
argument_list|(
literal|"Expected="
argument_list|)
operator|.
name|append
argument_list|(
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|expected
argument_list|)
argument_list|)
expr_stmt|;
name|msg
operator|.
name|append
argument_list|(
literal|"Got:\n"
argument_list|)
expr_stmt|;
for|for
control|(
name|Cell
name|kv
range|:
name|res
operator|.
name|listCells
argument_list|()
control|)
block|{
name|msg
operator|.
name|append
argument_list|(
name|kv
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|msg
operator|.
name|append
argument_list|(
literal|" val= "
argument_list|)
expr_stmt|;
name|msg
operator|.
name|append
argument_list|(
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|CellUtil
operator|.
name|cloneValue
argument_list|(
name|kv
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|msg
operator|.
name|append
argument_list|(
literal|"\n"
argument_list|)
expr_stmt|;
block|}
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|msg
operator|.
name|toString
argument_list|()
argument_list|)
throw|;
block|}
block|}
specifier|private
name|void
name|createTableIfMissing
parameter_list|(
name|Admin
name|admin
parameter_list|,
name|boolean
name|useMob
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
operator|!
name|admin
operator|.
name|tableExists
argument_list|(
name|TABLE_NAME
argument_list|)
condition|)
block|{
name|TableDescriptorBuilder
name|builder
init|=
name|TableDescriptorBuilder
operator|.
name|newBuilder
argument_list|(
name|TABLE_NAME
argument_list|)
decl_stmt|;
name|Stream
operator|.
name|of
argument_list|(
name|FAMILIES
argument_list|)
operator|.
name|map
argument_list|(
name|ColumnFamilyDescriptorBuilder
operator|::
name|of
argument_list|)
operator|.
name|forEachOrdered
argument_list|(
name|builder
operator|::
name|addColumnFamily
argument_list|)
expr_stmt|;
name|admin
operator|.
name|createTable
argument_list|(
name|builder
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|ColumnFamilyDescriptor
name|cfd
init|=
name|admin
operator|.
name|getDescriptor
argument_list|(
name|TABLE_NAME
argument_list|)
operator|.
name|getColumnFamilies
argument_list|()
index|[
literal|0
index|]
decl_stmt|;
if|if
condition|(
name|cfd
operator|.
name|isMobEnabled
argument_list|()
operator|!=
name|useMob
condition|)
block|{
name|admin
operator|.
name|modifyColumnFamily
argument_list|(
name|TABLE_NAME
argument_list|,
name|ColumnFamilyDescriptorBuilder
operator|.
name|newBuilder
argument_list|(
name|cfd
argument_list|)
operator|.
name|setMobEnabled
argument_list|(
name|useMob
argument_list|)
operator|.
name|setMobThreshold
argument_list|(
literal|4
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|runTestAtomicity
parameter_list|(
name|Admin
name|admin
parameter_list|)
throws|throws
name|Exception
block|{
name|createTableIfMissing
argument_list|(
name|admin
argument_list|,
name|useMob
argument_list|)
expr_stmt|;
name|TestContext
name|ctx
init|=
operator|new
name|TestContext
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|byte
name|rows
index|[]
index|[]
init|=
operator|new
name|byte
index|[
name|numUniqueRows
index|]
index|[]
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
name|numUniqueRows
condition|;
name|i
operator|++
control|)
block|{
name|rows
index|[
name|i
index|]
operator|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"test_row_"
operator|+
name|i
argument_list|)
expr_stmt|;
block|}
name|List
argument_list|<
name|AtomicityWriter
argument_list|>
name|writers
init|=
name|Lists
operator|.
name|newArrayList
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
name|numWriters
condition|;
name|i
operator|++
control|)
block|{
name|AtomicityWriter
name|writer
init|=
operator|new
name|AtomicityWriter
argument_list|(
name|ctx
argument_list|,
name|rows
argument_list|,
name|FAMILIES
argument_list|,
name|sharedPool
argument_list|)
decl_stmt|;
name|writers
operator|.
name|add
argument_list|(
name|writer
argument_list|)
expr_stmt|;
name|ctx
operator|.
name|addThread
argument_list|(
name|writer
argument_list|)
expr_stmt|;
block|}
comment|// Add a flusher
name|ctx
operator|.
name|addThread
argument_list|(
operator|new
name|RepeatingTestThread
argument_list|(
name|ctx
argument_list|)
block|{
specifier|public
name|void
name|doAnAction
parameter_list|()
throws|throws
name|Exception
block|{
try|try
block|{
name|admin
operator|.
name|flush
argument_list|(
name|TABLE_NAME
argument_list|)
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
name|warn
argument_list|(
literal|"Ignoring exception while flushing: "
operator|+
name|StringUtils
operator|.
name|stringifyException
argument_list|(
name|ioe
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// Flushing has been a source of ACID violations previously (see HBASE-2856), so ideally,
comment|// we would flush as often as possible. On a running cluster, this isn't practical:
comment|// (1) we will cause a lot of load due to all the flushing and compacting
comment|// (2) we cannot change the flushing/compacting related Configuration options to try to
comment|// alleviate this
comment|// (3) it is an unrealistic workload, since no one would actually flush that often.
comment|// Therefore, let's flush every minute to have more flushes than usual, but not overload
comment|// the running cluster.
if|if
condition|(
operator|!
name|crazyFlush
condition|)
block|{
name|Thread
operator|.
name|sleep
argument_list|(
literal|60000
argument_list|)
expr_stmt|;
block|}
block|}
block|}
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|AtomicGetReader
argument_list|>
name|getters
init|=
name|Lists
operator|.
name|newArrayList
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
name|numGetters
condition|;
name|i
operator|++
control|)
block|{
name|AtomicGetReader
name|getter
init|=
operator|new
name|AtomicGetReader
argument_list|(
name|ctx
argument_list|,
name|rows
index|[
name|i
operator|%
name|numUniqueRows
index|]
argument_list|,
name|FAMILIES
argument_list|,
name|sharedPool
argument_list|)
decl_stmt|;
name|getters
operator|.
name|add
argument_list|(
name|getter
argument_list|)
expr_stmt|;
name|ctx
operator|.
name|addThread
argument_list|(
name|getter
argument_list|)
expr_stmt|;
block|}
name|List
argument_list|<
name|AtomicScanReader
argument_list|>
name|scanners
init|=
name|Lists
operator|.
name|newArrayList
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
name|numScanners
condition|;
name|i
operator|++
control|)
block|{
name|AtomicScanReader
name|scanner
init|=
operator|new
name|AtomicScanReader
argument_list|(
name|ctx
argument_list|,
name|FAMILIES
argument_list|,
name|sharedPool
argument_list|)
decl_stmt|;
name|scanners
operator|.
name|add
argument_list|(
name|scanner
argument_list|)
expr_stmt|;
name|ctx
operator|.
name|addThread
argument_list|(
name|scanner
argument_list|)
expr_stmt|;
block|}
name|ctx
operator|.
name|startThreads
argument_list|()
expr_stmt|;
name|ctx
operator|.
name|waitFor
argument_list|(
name|millisToRun
argument_list|)
expr_stmt|;
name|ctx
operator|.
name|stop
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Finished test. Writers:"
argument_list|)
expr_stmt|;
for|for
control|(
name|AtomicityWriter
name|writer
range|:
name|writers
control|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"  wrote "
operator|+
name|writer
operator|.
name|numWritten
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"Readers:"
argument_list|)
expr_stmt|;
for|for
control|(
name|AtomicGetReader
name|reader
range|:
name|getters
control|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"  read "
operator|+
name|reader
operator|.
name|numRead
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"Scanners:"
argument_list|)
expr_stmt|;
for|for
control|(
name|AtomicScanReader
name|scanner
range|:
name|scanners
control|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"  scanned "
operator|+
name|scanner
operator|.
name|numScans
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"  verified "
operator|+
name|scanner
operator|.
name|numRowsScanned
operator|.
name|get
argument_list|()
operator|+
literal|" rows"
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
specifier|static
name|void
name|main
parameter_list|(
name|String
index|[]
name|args
parameter_list|)
block|{
name|Configuration
name|c
init|=
name|HBaseConfiguration
operator|.
name|create
argument_list|()
decl_stmt|;
name|int
name|status
decl_stmt|;
try|try
block|{
name|AcidGuaranteesTestTool
name|test
init|=
operator|new
name|AcidGuaranteesTestTool
argument_list|()
decl_stmt|;
name|status
operator|=
name|ToolRunner
operator|.
name|run
argument_list|(
name|c
argument_list|,
name|test
argument_list|,
name|args
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Exiting due to error"
argument_list|,
name|e
argument_list|)
expr_stmt|;
name|status
operator|=
operator|-
literal|1
expr_stmt|;
block|}
name|System
operator|.
name|exit
argument_list|(
name|status
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

