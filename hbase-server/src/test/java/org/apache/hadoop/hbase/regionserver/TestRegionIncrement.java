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
name|assertEquals
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
name|concurrent
operator|.
name|ThreadLocalRandom
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
name|HBaseClassTestRule
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
name|Durability
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
name|Increment
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
name|TestIncrementsFromClientSide
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
name|FSHLog
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
name|After
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Before
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|ClassRule
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
comment|/**  * Increments with some concurrency against a region to ensure we get the right answer.  * Test is parameterized to run the fast and slow path increments; if fast,  * HRegion.INCREMENT_FAST_BUT_NARROW_CONSISTENCY_KEY is true.  *  *<p>There is similar test up in TestAtomicOperation. It does a test where it has 100 threads  * doing increments across two column families all on one row and the increments are connected to  * prove atomicity on row.  */
end_comment

begin_class
annotation|@
name|Category
argument_list|(
name|MediumTests
operator|.
name|class
argument_list|)
specifier|public
class|class
name|TestRegionIncrement
block|{
annotation|@
name|ClassRule
specifier|public
specifier|static
specifier|final
name|HBaseClassTestRule
name|CLASS_RULE
init|=
name|HBaseClassTestRule
operator|.
name|forClass
argument_list|(
name|TestRegionIncrement
operator|.
name|class
argument_list|)
decl_stmt|;
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
name|TestRegionIncrement
operator|.
name|class
argument_list|)
decl_stmt|;
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
specifier|private
specifier|static
name|HBaseTestingUtility
name|TEST_UTIL
decl_stmt|;
specifier|private
specifier|final
specifier|static
name|byte
index|[]
name|INCREMENT_BYTES
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"increment"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|THREAD_COUNT
init|=
literal|10
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|INCREMENT_COUNT
init|=
literal|10000
decl_stmt|;
annotation|@
name|Before
specifier|public
name|void
name|setUp
parameter_list|()
throws|throws
name|Exception
block|{
name|TEST_UTIL
operator|=
operator|new
name|HBaseTestingUtility
argument_list|()
expr_stmt|;
block|}
annotation|@
name|After
specifier|public
name|void
name|tearDown
parameter_list|()
throws|throws
name|Exception
block|{
name|TEST_UTIL
operator|.
name|cleanupTestDir
argument_list|()
expr_stmt|;
block|}
specifier|private
name|HRegion
name|getRegion
parameter_list|(
specifier|final
name|Configuration
name|conf
parameter_list|,
specifier|final
name|String
name|tableName
parameter_list|)
throws|throws
name|IOException
block|{
name|FSHLog
name|wal
init|=
operator|new
name|FSHLog
argument_list|(
name|FileSystem
operator|.
name|get
argument_list|(
name|conf
argument_list|)
argument_list|,
name|TEST_UTIL
operator|.
name|getDataTestDir
argument_list|()
argument_list|,
name|TEST_UTIL
operator|.
name|getDataTestDir
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|,
name|conf
argument_list|)
decl_stmt|;
name|wal
operator|.
name|init
argument_list|()
expr_stmt|;
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
return|return
name|TEST_UTIL
operator|.
name|createLocalHRegion
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
name|tableName
argument_list|)
argument_list|,
name|HConstants
operator|.
name|EMPTY_BYTE_ARRAY
argument_list|,
name|HConstants
operator|.
name|EMPTY_BYTE_ARRAY
argument_list|,
literal|false
argument_list|,
name|Durability
operator|.
name|SKIP_WAL
argument_list|,
name|wal
argument_list|,
name|INCREMENT_BYTES
argument_list|)
return|;
block|}
specifier|private
name|void
name|closeRegion
parameter_list|(
specifier|final
name|HRegion
name|region
parameter_list|)
throws|throws
name|IOException
block|{
name|region
operator|.
name|close
argument_list|()
expr_stmt|;
name|region
operator|.
name|getWAL
argument_list|()
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testMVCCCausingMisRead
parameter_list|()
throws|throws
name|IOException
block|{
specifier|final
name|HRegion
name|region
init|=
name|getRegion
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|this
operator|.
name|name
operator|.
name|getMethodName
argument_list|()
argument_list|)
decl_stmt|;
try|try
block|{
comment|// ADD TEST HERE!!
block|}
finally|finally
block|{
name|closeRegion
argument_list|(
name|region
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Increments a single cell a bunch of times.    */
specifier|private
specifier|static
class|class
name|SingleCellIncrementer
extends|extends
name|Thread
block|{
specifier|private
specifier|final
name|int
name|count
decl_stmt|;
specifier|private
specifier|final
name|HRegion
name|region
decl_stmt|;
specifier|private
specifier|final
name|Increment
name|increment
decl_stmt|;
name|SingleCellIncrementer
parameter_list|(
specifier|final
name|int
name|i
parameter_list|,
specifier|final
name|int
name|count
parameter_list|,
specifier|final
name|HRegion
name|region
parameter_list|,
specifier|final
name|Increment
name|increment
parameter_list|)
block|{
name|super
argument_list|(
literal|""
operator|+
name|i
argument_list|)
expr_stmt|;
name|setDaemon
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|this
operator|.
name|count
operator|=
name|count
expr_stmt|;
name|this
operator|.
name|region
operator|=
name|region
expr_stmt|;
name|this
operator|.
name|increment
operator|=
name|increment
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|run
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
name|this
operator|.
name|count
condition|;
name|i
operator|++
control|)
block|{
try|try
block|{
name|this
operator|.
name|region
operator|.
name|increment
argument_list|(
name|this
operator|.
name|increment
argument_list|)
expr_stmt|;
comment|// LOG.info(getName() + " " + i);
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
comment|/**    * Increments a random row's Cell<code>count</code> times.    */
specifier|private
specifier|static
class|class
name|CrossRowCellIncrementer
extends|extends
name|Thread
block|{
specifier|private
specifier|final
name|int
name|count
decl_stmt|;
specifier|private
specifier|final
name|HRegion
name|region
decl_stmt|;
specifier|private
specifier|final
name|Increment
index|[]
name|increments
decl_stmt|;
name|CrossRowCellIncrementer
parameter_list|(
specifier|final
name|int
name|i
parameter_list|,
specifier|final
name|int
name|count
parameter_list|,
specifier|final
name|HRegion
name|region
parameter_list|,
specifier|final
name|int
name|range
parameter_list|)
block|{
name|super
argument_list|(
literal|""
operator|+
name|i
argument_list|)
expr_stmt|;
name|setDaemon
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|this
operator|.
name|count
operator|=
name|count
expr_stmt|;
name|this
operator|.
name|region
operator|=
name|region
expr_stmt|;
name|this
operator|.
name|increments
operator|=
operator|new
name|Increment
index|[
name|range
index|]
expr_stmt|;
for|for
control|(
name|int
name|ii
init|=
literal|0
init|;
name|ii
operator|<
name|range
condition|;
name|ii
operator|++
control|)
block|{
name|this
operator|.
name|increments
index|[
name|ii
index|]
operator|=
operator|new
name|Increment
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
name|this
operator|.
name|increments
index|[
name|ii
index|]
operator|.
name|addColumn
argument_list|(
name|INCREMENT_BYTES
argument_list|,
name|INCREMENT_BYTES
argument_list|,
literal|1
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|run
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
name|this
operator|.
name|count
condition|;
name|i
operator|++
control|)
block|{
try|try
block|{
name|int
name|index
init|=
name|ThreadLocalRandom
operator|.
name|current
argument_list|()
operator|.
name|nextInt
argument_list|(
literal|0
argument_list|,
name|this
operator|.
name|increments
operator|.
name|length
argument_list|)
decl_stmt|;
name|this
operator|.
name|region
operator|.
name|increment
argument_list|(
name|this
operator|.
name|increments
index|[
name|index
index|]
argument_list|)
expr_stmt|;
comment|// LOG.info(getName() + " " + index);
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
comment|/**    * Have each thread update its own Cell. Avoid contention with another thread.    */
annotation|@
name|Test
specifier|public
name|void
name|testUnContendedSingleCellIncrement
parameter_list|()
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
specifier|final
name|HRegion
name|region
init|=
name|getRegion
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|TestIncrementsFromClientSide
operator|.
name|filterStringSoTableNameSafe
argument_list|(
name|this
operator|.
name|name
operator|.
name|getMethodName
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|long
name|startTime
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
try|try
block|{
name|SingleCellIncrementer
index|[]
name|threads
init|=
operator|new
name|SingleCellIncrementer
index|[
name|THREAD_COUNT
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
name|threads
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|byte
index|[]
name|rowBytes
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|Increment
name|increment
init|=
operator|new
name|Increment
argument_list|(
name|rowBytes
argument_list|)
decl_stmt|;
name|increment
operator|.
name|addColumn
argument_list|(
name|INCREMENT_BYTES
argument_list|,
name|INCREMENT_BYTES
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|threads
index|[
name|i
index|]
operator|=
operator|new
name|SingleCellIncrementer
argument_list|(
name|i
argument_list|,
name|INCREMENT_COUNT
argument_list|,
name|region
argument_list|,
name|increment
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
name|threads
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|threads
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
name|threads
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|threads
index|[
name|i
index|]
operator|.
name|join
argument_list|()
expr_stmt|;
block|}
name|RegionScanner
name|regionScanner
init|=
name|region
operator|.
name|getScanner
argument_list|(
operator|new
name|Scan
argument_list|()
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Cell
argument_list|>
name|cells
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|THREAD_COUNT
argument_list|)
decl_stmt|;
while|while
condition|(
name|regionScanner
operator|.
name|next
argument_list|(
name|cells
argument_list|)
condition|)
continue|continue;
name|assertEquals
argument_list|(
name|THREAD_COUNT
argument_list|,
name|cells
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|long
name|total
init|=
literal|0
decl_stmt|;
for|for
control|(
name|Cell
name|cell
range|:
name|cells
control|)
name|total
operator|+=
name|Bytes
operator|.
name|toLong
argument_list|(
name|cell
operator|.
name|getValueArray
argument_list|()
argument_list|,
name|cell
operator|.
name|getValueOffset
argument_list|()
argument_list|,
name|cell
operator|.
name|getValueLength
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|INCREMENT_COUNT
operator|*
name|THREAD_COUNT
argument_list|,
name|total
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|closeRegion
argument_list|(
name|region
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
name|this
operator|.
name|name
operator|.
name|getMethodName
argument_list|()
operator|+
literal|" "
operator|+
operator|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|-
name|startTime
operator|)
operator|+
literal|"ms"
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Have each thread update its own Cell. Avoid contention with another thread.    */
annotation|@
name|Test
specifier|public
name|void
name|testContendedAcrossCellsIncrement
parameter_list|()
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
specifier|final
name|HRegion
name|region
init|=
name|getRegion
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|TestIncrementsFromClientSide
operator|.
name|filterStringSoTableNameSafe
argument_list|(
name|this
operator|.
name|name
operator|.
name|getMethodName
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|long
name|startTime
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
try|try
block|{
name|CrossRowCellIncrementer
index|[]
name|threads
init|=
operator|new
name|CrossRowCellIncrementer
index|[
name|THREAD_COUNT
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
name|threads
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|threads
index|[
name|i
index|]
operator|=
operator|new
name|CrossRowCellIncrementer
argument_list|(
name|i
argument_list|,
name|INCREMENT_COUNT
argument_list|,
name|region
argument_list|,
name|THREAD_COUNT
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
name|threads
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|threads
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
name|threads
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|threads
index|[
name|i
index|]
operator|.
name|join
argument_list|()
expr_stmt|;
block|}
name|RegionScanner
name|regionScanner
init|=
name|region
operator|.
name|getScanner
argument_list|(
operator|new
name|Scan
argument_list|()
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Cell
argument_list|>
name|cells
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
literal|100
argument_list|)
decl_stmt|;
while|while
condition|(
name|regionScanner
operator|.
name|next
argument_list|(
name|cells
argument_list|)
condition|)
continue|continue;
name|assertEquals
argument_list|(
name|THREAD_COUNT
argument_list|,
name|cells
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|long
name|total
init|=
literal|0
decl_stmt|;
for|for
control|(
name|Cell
name|cell
range|:
name|cells
control|)
name|total
operator|+=
name|Bytes
operator|.
name|toLong
argument_list|(
name|cell
operator|.
name|getValueArray
argument_list|()
argument_list|,
name|cell
operator|.
name|getValueOffset
argument_list|()
argument_list|,
name|cell
operator|.
name|getValueLength
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|INCREMENT_COUNT
operator|*
name|THREAD_COUNT
argument_list|,
name|total
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|closeRegion
argument_list|(
name|region
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
name|this
operator|.
name|name
operator|.
name|getMethodName
argument_list|()
operator|+
literal|" "
operator|+
operator|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|-
name|startTime
operator|)
operator|+
literal|"ms"
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

