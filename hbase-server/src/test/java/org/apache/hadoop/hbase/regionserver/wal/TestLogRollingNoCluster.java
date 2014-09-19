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
name|assertFalse
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
comment|/**  * Test many concurrent appenders to an {@link #HLog} while rolling the log.  */
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
name|TestLogRollingNoCluster
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
specifier|final
specifier|static
name|byte
index|[]
name|EMPTY_1K_ARRAY
init|=
operator|new
name|byte
index|[
literal|1024
index|]
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|THREAD_COUNT
init|=
literal|100
decl_stmt|;
comment|// Spin up this many threads
comment|/**    * Spin up a bunch of threads and have them all append to a WAL.  Roll the    * WAL frequently to try and trigger NPE.    * @throws IOException    * @throws InterruptedException    */
annotation|@
name|Test
specifier|public
name|void
name|testContendedLogRolling
parameter_list|()
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|FileSystem
name|fs
init|=
name|FileSystem
operator|.
name|get
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
name|Path
name|dir
init|=
name|TEST_UTIL
operator|.
name|getDataTestDir
argument_list|()
decl_stmt|;
comment|// The implementation needs to know the 'handler' count.
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setInt
argument_list|(
name|HConstants
operator|.
name|REGION_SERVER_HANDLER_COUNT
argument_list|,
name|THREAD_COUNT
argument_list|)
expr_stmt|;
name|HLog
name|wal
init|=
name|HLogFactory
operator|.
name|createHLog
argument_list|(
name|fs
argument_list|,
name|dir
argument_list|,
literal|"logs"
argument_list|,
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
name|Appender
index|[]
name|appenders
init|=
literal|null
decl_stmt|;
specifier|final
name|int
name|count
init|=
name|THREAD_COUNT
decl_stmt|;
name|appenders
operator|=
operator|new
name|Appender
index|[
name|count
index|]
expr_stmt|;
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
name|count
condition|;
name|i
operator|++
control|)
block|{
comment|// Have each appending thread write 'count' entries
name|appenders
index|[
name|i
index|]
operator|=
operator|new
name|Appender
argument_list|(
name|wal
argument_list|,
name|i
argument_list|,
name|count
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|count
condition|;
name|i
operator|++
control|)
block|{
name|appenders
index|[
name|i
index|]
operator|.
name|start
argument_list|()
expr_stmt|;
block|}
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|count
condition|;
name|i
operator|++
control|)
block|{
comment|//ensure that all threads are joined before closing the wal
name|appenders
index|[
name|i
index|]
operator|.
name|join
argument_list|()
expr_stmt|;
block|}
block|}
finally|finally
block|{
name|wal
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|count
condition|;
name|i
operator|++
control|)
block|{
name|assertFalse
argument_list|(
name|appenders
index|[
name|i
index|]
operator|.
name|isException
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Appender thread.  Appends to passed wal file.    */
specifier|static
class|class
name|Appender
extends|extends
name|Thread
block|{
specifier|private
specifier|final
name|Log
name|log
decl_stmt|;
specifier|private
specifier|final
name|HLog
name|wal
decl_stmt|;
specifier|private
specifier|final
name|int
name|count
decl_stmt|;
specifier|private
name|Exception
name|e
init|=
literal|null
decl_stmt|;
name|Appender
parameter_list|(
specifier|final
name|HLog
name|wal
parameter_list|,
specifier|final
name|int
name|index
parameter_list|,
specifier|final
name|int
name|count
parameter_list|)
block|{
name|super
argument_list|(
literal|""
operator|+
name|index
argument_list|)
expr_stmt|;
name|this
operator|.
name|wal
operator|=
name|wal
expr_stmt|;
name|this
operator|.
name|count
operator|=
name|count
expr_stmt|;
name|this
operator|.
name|log
operator|=
name|LogFactory
operator|.
name|getLog
argument_list|(
literal|"Appender:"
operator|+
name|getName
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**      * @return Call when the thread is done.      */
name|boolean
name|isException
parameter_list|()
block|{
return|return
operator|!
name|isAlive
argument_list|()
operator|&&
name|this
operator|.
name|e
operator|!=
literal|null
return|;
block|}
name|Exception
name|getException
parameter_list|()
block|{
return|return
name|this
operator|.
name|e
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
name|this
operator|.
name|log
operator|.
name|info
argument_list|(
name|getName
argument_list|()
operator|+
literal|" started"
argument_list|)
expr_stmt|;
specifier|final
name|AtomicLong
name|sequenceId
init|=
operator|new
name|AtomicLong
argument_list|(
literal|1
argument_list|)
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
name|this
operator|.
name|count
condition|;
name|i
operator|++
control|)
block|{
name|long
name|now
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
comment|// Roll every ten edits if the log has anything in it.
if|if
condition|(
name|i
operator|%
literal|10
operator|==
literal|0
operator|&&
operator|(
operator|(
name|FSHLog
operator|)
name|this
operator|.
name|wal
operator|)
operator|.
name|getNumEntries
argument_list|()
operator|>
literal|0
condition|)
block|{
name|this
operator|.
name|wal
operator|.
name|rollWriter
argument_list|()
expr_stmt|;
block|}
name|WALEdit
name|edit
init|=
operator|new
name|WALEdit
argument_list|()
decl_stmt|;
name|byte
index|[]
name|bytes
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|edit
operator|.
name|add
argument_list|(
operator|new
name|KeyValue
argument_list|(
name|bytes
argument_list|,
name|bytes
argument_list|,
name|bytes
argument_list|,
name|now
argument_list|,
name|EMPTY_1K_ARRAY
argument_list|)
argument_list|)
expr_stmt|;
name|this
operator|.
name|wal
operator|.
name|append
argument_list|(
name|HRegionInfo
operator|.
name|FIRST_META_REGIONINFO
argument_list|,
name|TableName
operator|.
name|META_TABLE_NAME
argument_list|,
name|edit
argument_list|,
name|now
argument_list|,
name|HTableDescriptor
operator|.
name|META_TABLEDESC
argument_list|,
name|sequenceId
argument_list|)
expr_stmt|;
block|}
name|String
name|msg
init|=
name|getName
argument_list|()
operator|+
literal|" finished"
decl_stmt|;
if|if
condition|(
name|isException
argument_list|()
condition|)
name|this
operator|.
name|log
operator|.
name|info
argument_list|(
name|msg
argument_list|,
name|getException
argument_list|()
argument_list|)
expr_stmt|;
else|else
name|this
operator|.
name|log
operator|.
name|info
argument_list|(
name|msg
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|this
operator|.
name|e
operator|=
name|e
expr_stmt|;
name|log
operator|.
name|info
argument_list|(
literal|"Caught exception from Appender:"
operator|+
name|getName
argument_list|()
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
comment|// Call sync on our log.else threads just hang out.
try|try
block|{
name|this
operator|.
name|wal
operator|.
name|sync
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
block|}
block|}
comment|//@org.junit.Rule
comment|//public org.apache.hadoop.hbase.ResourceCheckerJUnitRule cu =
comment|//  new org.apache.hadoop.hbase.ResourceCheckerJUnitRule();
block|}
end_class

end_unit

