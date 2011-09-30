begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2010 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|Arrays
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashMap
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
name|atomic
operator|.
name|AtomicBoolean
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
name|AtomicInteger
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
name|AtomicReference
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
name|hdfs
operator|.
name|MiniDFSCluster
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
name|DoNotRetryIOException
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
name|HBaseConfiguration
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
name|HBaseTestCase
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
name|HDFSBlocksDistribution
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
name|HConstants
operator|.
name|OperationStatusCode
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
name|MultithreadedTestUtil
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
name|TestThread
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
name|MiniHBaseCluster
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
name|Delete
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
name|filter
operator|.
name|BinaryComparator
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
name|filter
operator|.
name|ColumnCountGetFilter
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
name|filter
operator|.
name|CompareFilter
operator|.
name|CompareOp
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
name|filter
operator|.
name|Filter
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
name|filter
operator|.
name|FilterList
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
name|filter
operator|.
name|NullComparator
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
name|filter
operator|.
name|PrefixFilter
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
name|filter
operator|.
name|SingleColumnValueFilter
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
operator|.
name|RegionScannerImpl
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
name|HLog
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
name|EnvironmentEdgeManager
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
name|EnvironmentEdgeManagerTestHelper
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
name|IncrementingEnvironmentEdge
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
name|ManualEnvironmentEdge
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
name|Pair
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
name|PairOfSameType
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
name|junit
operator|.
name|Test
import|;
end_import

begin_import
import|import
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
comment|/**  * Testing of HRegion.incrementColumnValue  *  */
end_comment

begin_class
specifier|public
class|class
name|TestIncrement
extends|extends
name|HBaseTestCase
block|{
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|TestIncrement
operator|.
name|class
argument_list|)
decl_stmt|;
name|HRegion
name|region
init|=
literal|null
decl_stmt|;
specifier|private
specifier|final
name|String
name|DIR
init|=
name|HBaseTestingUtility
operator|.
name|getTestDir
argument_list|()
operator|+
literal|"/TestIncrement/"
decl_stmt|;
specifier|private
specifier|final
name|int
name|MAX_VERSIONS
init|=
literal|2
decl_stmt|;
comment|// Test names
specifier|static
specifier|final
name|byte
index|[]
name|tableName
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"testtable"
argument_list|)
decl_stmt|;
empty_stmt|;
specifier|static
specifier|final
name|byte
index|[]
name|qual1
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"qual1"
argument_list|)
decl_stmt|;
specifier|static
specifier|final
name|byte
index|[]
name|qual2
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"qual2"
argument_list|)
decl_stmt|;
specifier|static
specifier|final
name|byte
index|[]
name|qual3
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"qual3"
argument_list|)
decl_stmt|;
specifier|static
specifier|final
name|byte
index|[]
name|value1
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"value1"
argument_list|)
decl_stmt|;
specifier|static
specifier|final
name|byte
index|[]
name|value2
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"value2"
argument_list|)
decl_stmt|;
specifier|static
specifier|final
name|byte
index|[]
name|row
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"rowA"
argument_list|)
decl_stmt|;
specifier|static
specifier|final
name|byte
index|[]
name|row2
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"rowB"
argument_list|)
decl_stmt|;
comment|/**    * @see org.apache.hadoop.hbase.HBaseTestCase#setUp()    */
annotation|@
name|Override
specifier|protected
name|void
name|setUp
parameter_list|()
throws|throws
name|Exception
block|{
name|super
operator|.
name|setUp
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|tearDown
parameter_list|()
throws|throws
name|Exception
block|{
name|super
operator|.
name|tearDown
argument_list|()
expr_stmt|;
name|EnvironmentEdgeManagerTestHelper
operator|.
name|reset
argument_list|()
expr_stmt|;
block|}
comment|//////////////////////////////////////////////////////////////////////////////
comment|// New tests that doesn't spin up a mini cluster but rather just test the
comment|// individual code pieces in the HRegion.
comment|//////////////////////////////////////////////////////////////////////////////
comment|/**    * Test one increment command.    */
specifier|public
name|void
name|testIncrementColumnValue
parameter_list|()
throws|throws
name|IOException
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Starting test testIncrementColumnValue"
argument_list|)
expr_stmt|;
name|initHRegion
argument_list|(
name|tableName
argument_list|,
name|getName
argument_list|()
argument_list|,
name|fam1
argument_list|)
expr_stmt|;
name|long
name|value
init|=
literal|1L
decl_stmt|;
name|long
name|amount
init|=
literal|3L
decl_stmt|;
name|Put
name|put
init|=
operator|new
name|Put
argument_list|(
name|row
argument_list|)
decl_stmt|;
name|put
operator|.
name|add
argument_list|(
name|fam1
argument_list|,
name|qual1
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|value
argument_list|)
argument_list|)
expr_stmt|;
name|region
operator|.
name|put
argument_list|(
name|put
argument_list|)
expr_stmt|;
name|long
name|result
init|=
name|region
operator|.
name|incrementColumnValue
argument_list|(
name|row
argument_list|,
name|fam1
argument_list|,
name|qual1
argument_list|,
name|amount
argument_list|,
literal|true
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|value
operator|+
name|amount
argument_list|,
name|result
argument_list|)
expr_stmt|;
name|Store
name|store
init|=
name|region
operator|.
name|getStore
argument_list|(
name|fam1
argument_list|)
decl_stmt|;
comment|// ICV removes any extra values floating around in there.
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|store
operator|.
name|memstore
operator|.
name|kvset
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|store
operator|.
name|memstore
operator|.
name|snapshot
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
name|assertICV
argument_list|(
name|row
argument_list|,
name|fam1
argument_list|,
name|qual1
argument_list|,
name|value
operator|+
name|amount
argument_list|)
expr_stmt|;
block|}
comment|/**    * Test multi-threaded increments.    */
specifier|public
name|void
name|testIncrementMultiThreads
parameter_list|()
throws|throws
name|IOException
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Starting test testIncrementMultiThreads"
argument_list|)
expr_stmt|;
name|initHRegion
argument_list|(
name|tableName
argument_list|,
name|getName
argument_list|()
argument_list|,
name|fam1
argument_list|)
expr_stmt|;
comment|// create 100 threads, each will increment by its own quantity
name|int
name|numThreads
init|=
literal|100
decl_stmt|;
name|int
name|incrementsPerThread
init|=
literal|1000
decl_stmt|;
name|Incrementer
index|[]
name|all
init|=
operator|new
name|Incrementer
index|[
name|numThreads
index|]
decl_stmt|;
name|int
name|expectedTotal
init|=
literal|0
decl_stmt|;
comment|// create all threads
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
name|all
index|[
name|i
index|]
operator|=
operator|new
name|Incrementer
argument_list|(
name|region
argument_list|,
name|i
argument_list|,
name|i
argument_list|,
name|incrementsPerThread
argument_list|)
expr_stmt|;
name|expectedTotal
operator|+=
operator|(
name|i
operator|*
name|incrementsPerThread
operator|)
expr_stmt|;
block|}
comment|// run all threads
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
name|all
index|[
name|i
index|]
operator|.
name|start
argument_list|()
expr_stmt|;
block|}
comment|// wait for all threads to finish
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
try|try
block|{
name|all
index|[
name|i
index|]
operator|.
name|join
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{       }
block|}
name|assertICV
argument_list|(
name|row
argument_list|,
name|fam1
argument_list|,
name|qual1
argument_list|,
name|expectedTotal
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"testIncrementMultiThreads successfully verified that total is "
operator|+
name|expectedTotal
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|assertICV
parameter_list|(
name|byte
index|[]
name|row
parameter_list|,
name|byte
index|[]
name|familiy
parameter_list|,
name|byte
index|[]
name|qualifier
parameter_list|,
name|long
name|amount
parameter_list|)
throws|throws
name|IOException
block|{
comment|// run a get and see?
name|Get
name|get
init|=
operator|new
name|Get
argument_list|(
name|row
argument_list|)
decl_stmt|;
name|get
operator|.
name|addColumn
argument_list|(
name|familiy
argument_list|,
name|qualifier
argument_list|)
expr_stmt|;
name|Result
name|result
init|=
name|region
operator|.
name|get
argument_list|(
name|get
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|result
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|KeyValue
name|kv
init|=
name|result
operator|.
name|raw
argument_list|()
index|[
literal|0
index|]
decl_stmt|;
name|long
name|r
init|=
name|Bytes
operator|.
name|toLong
argument_list|(
name|kv
operator|.
name|getValue
argument_list|()
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|amount
argument_list|,
name|r
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|initHRegion
parameter_list|(
name|byte
index|[]
name|tableName
parameter_list|,
name|String
name|callingMethod
parameter_list|,
name|byte
index|[]
modifier|...
name|families
parameter_list|)
throws|throws
name|IOException
block|{
name|initHRegion
argument_list|(
name|tableName
argument_list|,
name|callingMethod
argument_list|,
name|HBaseConfiguration
operator|.
name|create
argument_list|()
argument_list|,
name|families
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|initHRegion
parameter_list|(
name|byte
index|[]
name|tableName
parameter_list|,
name|String
name|callingMethod
parameter_list|,
name|Configuration
name|conf
parameter_list|,
name|byte
index|[]
modifier|...
name|families
parameter_list|)
throws|throws
name|IOException
block|{
name|HTableDescriptor
name|htd
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
for|for
control|(
name|byte
index|[]
name|family
range|:
name|families
control|)
block|{
name|htd
operator|.
name|addFamily
argument_list|(
operator|new
name|HColumnDescriptor
argument_list|(
name|family
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|HRegionInfo
name|info
init|=
operator|new
name|HRegionInfo
argument_list|(
name|htd
operator|.
name|getName
argument_list|()
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|Path
name|path
init|=
operator|new
name|Path
argument_list|(
name|DIR
operator|+
name|callingMethod
argument_list|)
decl_stmt|;
if|if
condition|(
name|fs
operator|.
name|exists
argument_list|(
name|path
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
name|path
argument_list|,
literal|true
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Failed delete of "
operator|+
name|path
argument_list|)
throw|;
block|}
block|}
name|region
operator|=
name|HRegion
operator|.
name|createHRegion
argument_list|(
name|info
argument_list|,
name|path
argument_list|,
name|conf
argument_list|,
name|htd
argument_list|)
expr_stmt|;
block|}
comment|/**    * A thread that makes a few increment calls    */
specifier|public
specifier|static
class|class
name|Incrementer
extends|extends
name|Thread
block|{
specifier|private
specifier|final
name|HRegion
name|region
decl_stmt|;
specifier|private
specifier|final
name|int
name|threadNumber
decl_stmt|;
specifier|private
specifier|final
name|int
name|numIncrements
decl_stmt|;
specifier|private
specifier|final
name|int
name|amount
decl_stmt|;
specifier|private
name|int
name|count
decl_stmt|;
specifier|public
name|Incrementer
parameter_list|(
name|HRegion
name|region
parameter_list|,
name|int
name|threadNumber
parameter_list|,
name|int
name|amount
parameter_list|,
name|int
name|numIncrements
parameter_list|)
block|{
name|this
operator|.
name|region
operator|=
name|region
expr_stmt|;
name|this
operator|.
name|threadNumber
operator|=
name|threadNumber
expr_stmt|;
name|this
operator|.
name|numIncrements
operator|=
name|numIncrements
expr_stmt|;
name|this
operator|.
name|count
operator|=
literal|0
expr_stmt|;
name|this
operator|.
name|amount
operator|=
name|amount
expr_stmt|;
name|setDaemon
argument_list|(
literal|true
argument_list|)
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
name|numIncrements
condition|;
name|i
operator|++
control|)
block|{
try|try
block|{
name|long
name|result
init|=
name|region
operator|.
name|incrementColumnValue
argument_list|(
name|row
argument_list|,
name|fam1
argument_list|,
name|qual1
argument_list|,
name|amount
argument_list|,
literal|true
argument_list|)
decl_stmt|;
comment|// LOG.info("thread:" + threadNumber + " iter:" + i);
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
block|}
name|count
operator|++
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit

