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
name|hbase
operator|.
name|client
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
name|EnvironmentEdgeManagerTestHelper
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
comment|/**  * Testing of HRegion.incrementColumnValue, HRegion.increment,  * and HRegion.append  */
end_comment

begin_class
annotation|@
name|Category
argument_list|(
name|MediumTests
operator|.
name|class
argument_list|)
comment|// Starts 100 threads
specifier|public
class|class
name|TestAtomicOperation
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
name|TestAtomicOperation
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
name|HBaseTestingUtility
name|TEST_UTIL
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|String
name|DIR
init|=
name|TEST_UTIL
operator|.
name|getDataTestDir
argument_list|(
literal|"TestAtomicOperation"
argument_list|)
operator|.
name|toString
argument_list|()
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
comment|/**    * Test basic append operation.    * More tests in    * @see org.apache.hadoop.hbase.client.TestFromClientSide#testAppend()    */
specifier|public
name|void
name|testAppend
parameter_list|()
throws|throws
name|IOException
block|{
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
name|String
name|v1
init|=
literal|"Ultimate Answer to the Ultimate Question of Life,"
operator|+
literal|" The Universe, and Everything"
decl_stmt|;
name|String
name|v2
init|=
literal|" is... 42."
decl_stmt|;
name|Append
name|a
init|=
operator|new
name|Append
argument_list|(
name|row
argument_list|)
decl_stmt|;
name|a
operator|.
name|setReturnResults
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|a
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
name|v1
argument_list|)
argument_list|)
expr_stmt|;
name|a
operator|.
name|add
argument_list|(
name|fam1
argument_list|,
name|qual2
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|v2
argument_list|)
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|region
operator|.
name|append
argument_list|(
name|a
argument_list|)
argument_list|)
expr_stmt|;
name|a
operator|=
operator|new
name|Append
argument_list|(
name|row
argument_list|)
expr_stmt|;
name|a
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
name|v2
argument_list|)
argument_list|)
expr_stmt|;
name|a
operator|.
name|add
argument_list|(
name|fam1
argument_list|,
name|qual2
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|v1
argument_list|)
argument_list|)
expr_stmt|;
name|Result
name|result
init|=
name|region
operator|.
name|append
argument_list|(
name|a
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|Bytes
operator|.
name|compareTo
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|v1
operator|+
name|v2
argument_list|)
argument_list|,
name|result
operator|.
name|getValue
argument_list|(
name|fam1
argument_list|,
name|qual1
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|Bytes
operator|.
name|compareTo
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|v2
operator|+
name|v1
argument_list|)
argument_list|,
name|result
operator|.
name|getValue
argument_list|(
name|fam1
argument_list|,
name|qual2
argument_list|)
argument_list|)
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
comment|// run a with mixed column families (1 and 3 versions)
name|initHRegion
argument_list|(
name|tableName
argument_list|,
name|getName
argument_list|()
argument_list|,
operator|new
name|int
index|[]
block|{
literal|1
block|,
literal|3
block|}
argument_list|,
name|fam1
argument_list|,
name|fam2
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
name|assertICV
argument_list|(
name|row
argument_list|,
name|fam1
argument_list|,
name|qual2
argument_list|,
name|expectedTotal
operator|*
literal|2
argument_list|)
expr_stmt|;
name|assertICV
argument_list|(
name|row
argument_list|,
name|fam2
argument_list|,
name|qual3
argument_list|,
name|expectedTotal
operator|*
literal|3
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
literal|null
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
name|int
index|[]
name|maxVersions
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
name|int
name|i
init|=
literal|0
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
name|HColumnDescriptor
name|hcd
init|=
operator|new
name|HColumnDescriptor
argument_list|(
name|family
argument_list|)
decl_stmt|;
name|hcd
operator|.
name|setMaxVersions
argument_list|(
name|maxVersions
operator|!=
literal|null
condition|?
name|maxVersions
index|[
name|i
operator|++
index|]
else|:
literal|1
argument_list|)
expr_stmt|;
name|htd
operator|.
name|addFamily
argument_list|(
name|hcd
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
name|HBaseConfiguration
operator|.
name|create
argument_list|()
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
name|numIncrements
decl_stmt|;
specifier|private
specifier|final
name|int
name|amount
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
name|numIncrements
operator|=
name|numIncrements
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
name|Increment
name|inc
init|=
operator|new
name|Increment
argument_list|(
name|row
argument_list|)
decl_stmt|;
name|inc
operator|.
name|addColumn
argument_list|(
name|fam1
argument_list|,
name|qual1
argument_list|,
name|amount
argument_list|)
expr_stmt|;
name|inc
operator|.
name|addColumn
argument_list|(
name|fam1
argument_list|,
name|qual2
argument_list|,
name|amount
operator|*
literal|2
argument_list|)
expr_stmt|;
name|inc
operator|.
name|addColumn
argument_list|(
name|fam2
argument_list|,
name|qual3
argument_list|,
name|amount
operator|*
literal|3
argument_list|)
expr_stmt|;
name|region
operator|.
name|increment
argument_list|(
name|inc
argument_list|)
expr_stmt|;
comment|// verify: Make sure we only see completed increments
name|Get
name|g
init|=
operator|new
name|Get
argument_list|(
name|row
argument_list|)
decl_stmt|;
name|Result
name|result
init|=
name|region
operator|.
name|get
argument_list|(
name|g
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|Bytes
operator|.
name|toLong
argument_list|(
name|result
operator|.
name|getValue
argument_list|(
name|fam1
argument_list|,
name|qual1
argument_list|)
argument_list|)
operator|*
literal|2
argument_list|,
name|Bytes
operator|.
name|toLong
argument_list|(
name|result
operator|.
name|getValue
argument_list|(
name|fam1
argument_list|,
name|qual2
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|Bytes
operator|.
name|toLong
argument_list|(
name|result
operator|.
name|getValue
argument_list|(
name|fam1
argument_list|,
name|qual1
argument_list|)
argument_list|)
operator|*
literal|3
argument_list|,
name|Bytes
operator|.
name|toLong
argument_list|(
name|result
operator|.
name|getValue
argument_list|(
name|fam2
argument_list|,
name|qual3
argument_list|)
argument_list|)
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
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
block|}
block|}
block|}
block|}
specifier|public
name|void
name|testAppendMultiThreads
parameter_list|()
throws|throws
name|IOException
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Starting test testAppendMultiThreads"
argument_list|)
expr_stmt|;
comment|// run a with mixed column families (1 and 3 versions)
name|initHRegion
argument_list|(
name|tableName
argument_list|,
name|getName
argument_list|()
argument_list|,
operator|new
name|int
index|[]
block|{
literal|1
block|,
literal|3
block|}
argument_list|,
name|fam1
argument_list|,
name|fam2
argument_list|)
expr_stmt|;
name|int
name|numThreads
init|=
literal|100
decl_stmt|;
name|int
name|opsPerThread
init|=
literal|100
decl_stmt|;
name|AtomicOperation
index|[]
name|all
init|=
operator|new
name|AtomicOperation
index|[
name|numThreads
index|]
decl_stmt|;
specifier|final
name|byte
index|[]
name|val
init|=
operator|new
name|byte
index|[]
block|{
literal|1
block|}
decl_stmt|;
name|AtomicInteger
name|failures
init|=
operator|new
name|AtomicInteger
argument_list|(
literal|0
argument_list|)
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
name|AtomicOperation
argument_list|(
name|region
argument_list|,
name|opsPerThread
argument_list|,
literal|null
argument_list|,
name|failures
argument_list|)
block|{
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
name|numOps
condition|;
name|i
operator|++
control|)
block|{
try|try
block|{
name|Append
name|a
init|=
operator|new
name|Append
argument_list|(
name|row
argument_list|)
decl_stmt|;
name|a
operator|.
name|add
argument_list|(
name|fam1
argument_list|,
name|qual1
argument_list|,
name|val
argument_list|)
expr_stmt|;
name|a
operator|.
name|add
argument_list|(
name|fam1
argument_list|,
name|qual2
argument_list|,
name|val
argument_list|)
expr_stmt|;
name|a
operator|.
name|add
argument_list|(
name|fam2
argument_list|,
name|qual3
argument_list|,
name|val
argument_list|)
expr_stmt|;
name|region
operator|.
name|append
argument_list|(
name|a
argument_list|)
expr_stmt|;
name|Get
name|g
init|=
operator|new
name|Get
argument_list|(
name|row
argument_list|)
decl_stmt|;
name|Result
name|result
init|=
name|region
operator|.
name|get
argument_list|(
name|g
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|result
operator|.
name|getValue
argument_list|(
name|fam1
argument_list|,
name|qual1
argument_list|)
operator|.
name|length
argument_list|,
name|result
operator|.
name|getValue
argument_list|(
name|fam1
argument_list|,
name|qual2
argument_list|)
operator|.
name|length
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|result
operator|.
name|getValue
argument_list|(
name|fam1
argument_list|,
name|qual1
argument_list|)
operator|.
name|length
argument_list|,
name|result
operator|.
name|getValue
argument_list|(
name|fam2
argument_list|,
name|qual3
argument_list|)
operator|.
name|length
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
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
name|failures
operator|.
name|incrementAndGet
argument_list|()
expr_stmt|;
name|fail
argument_list|()
expr_stmt|;
block|}
block|}
block|}
block|}
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
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|failures
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
name|Get
name|g
init|=
operator|new
name|Get
argument_list|(
name|row
argument_list|)
decl_stmt|;
name|Result
name|result
init|=
name|region
operator|.
name|get
argument_list|(
name|g
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|result
operator|.
name|getValue
argument_list|(
name|fam1
argument_list|,
name|qual1
argument_list|)
operator|.
name|length
argument_list|,
literal|10000
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|result
operator|.
name|getValue
argument_list|(
name|fam1
argument_list|,
name|qual2
argument_list|)
operator|.
name|length
argument_list|,
literal|10000
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|result
operator|.
name|getValue
argument_list|(
name|fam2
argument_list|,
name|qual3
argument_list|)
operator|.
name|length
argument_list|,
literal|10000
argument_list|)
expr_stmt|;
block|}
comment|/**    * Test multi-threaded row mutations.    */
specifier|public
name|void
name|testRowMutationMultiThreads
parameter_list|()
throws|throws
name|IOException
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Starting test testRowMutationMultiThreads"
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
comment|// create 10 threads, each will alternate between adding and
comment|// removing a column
name|int
name|numThreads
init|=
literal|10
decl_stmt|;
name|int
name|opsPerThread
init|=
literal|500
decl_stmt|;
name|AtomicOperation
index|[]
name|all
init|=
operator|new
name|AtomicOperation
index|[
name|numThreads
index|]
decl_stmt|;
name|AtomicLong
name|timeStamps
init|=
operator|new
name|AtomicLong
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|AtomicInteger
name|failures
init|=
operator|new
name|AtomicInteger
argument_list|(
literal|0
argument_list|)
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
name|AtomicOperation
argument_list|(
name|region
argument_list|,
name|opsPerThread
argument_list|,
name|timeStamps
argument_list|,
name|failures
argument_list|)
block|{
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
name|boolean
name|op
init|=
literal|true
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
name|numOps
condition|;
name|i
operator|++
control|)
block|{
try|try
block|{
comment|// throw in some flushes
if|if
condition|(
name|i
operator|%
literal|10
operator|==
literal|0
condition|)
block|{
synchronized|synchronized
init|(
name|region
init|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"flushing"
argument_list|)
expr_stmt|;
name|region
operator|.
name|flushcache
argument_list|()
expr_stmt|;
if|if
condition|(
name|i
operator|%
literal|100
operator|==
literal|0
condition|)
block|{
name|region
operator|.
name|compactStores
argument_list|()
expr_stmt|;
block|}
block|}
block|}
name|long
name|ts
init|=
name|timeStamps
operator|.
name|incrementAndGet
argument_list|()
decl_stmt|;
name|RowMutations
name|rm
init|=
operator|new
name|RowMutations
argument_list|(
name|row
argument_list|)
decl_stmt|;
if|if
condition|(
name|op
condition|)
block|{
name|Put
name|p
init|=
operator|new
name|Put
argument_list|(
name|row
argument_list|,
name|ts
argument_list|)
decl_stmt|;
name|p
operator|.
name|add
argument_list|(
name|fam1
argument_list|,
name|qual1
argument_list|,
name|value1
argument_list|)
expr_stmt|;
name|rm
operator|.
name|add
argument_list|(
name|p
argument_list|)
expr_stmt|;
name|Delete
name|d
init|=
operator|new
name|Delete
argument_list|(
name|row
argument_list|)
decl_stmt|;
name|d
operator|.
name|deleteColumns
argument_list|(
name|fam1
argument_list|,
name|qual2
argument_list|,
name|ts
argument_list|)
expr_stmt|;
name|rm
operator|.
name|add
argument_list|(
name|d
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|Delete
name|d
init|=
operator|new
name|Delete
argument_list|(
name|row
argument_list|)
decl_stmt|;
name|d
operator|.
name|deleteColumns
argument_list|(
name|fam1
argument_list|,
name|qual1
argument_list|,
name|ts
argument_list|)
expr_stmt|;
name|rm
operator|.
name|add
argument_list|(
name|d
argument_list|)
expr_stmt|;
name|Put
name|p
init|=
operator|new
name|Put
argument_list|(
name|row
argument_list|,
name|ts
argument_list|)
decl_stmt|;
name|p
operator|.
name|add
argument_list|(
name|fam1
argument_list|,
name|qual2
argument_list|,
name|value2
argument_list|)
expr_stmt|;
name|rm
operator|.
name|add
argument_list|(
name|p
argument_list|)
expr_stmt|;
block|}
name|region
operator|.
name|mutateRow
argument_list|(
name|rm
argument_list|)
expr_stmt|;
name|op
operator|^=
literal|true
expr_stmt|;
comment|// check: should always see exactly one column
name|Get
name|g
init|=
operator|new
name|Get
argument_list|(
name|row
argument_list|)
decl_stmt|;
name|Result
name|r
init|=
name|region
operator|.
name|get
argument_list|(
name|g
argument_list|)
decl_stmt|;
if|if
condition|(
name|r
operator|.
name|size
argument_list|()
operator|!=
literal|1
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
name|r
argument_list|)
expr_stmt|;
name|failures
operator|.
name|incrementAndGet
argument_list|()
expr_stmt|;
name|fail
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
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
name|failures
operator|.
name|incrementAndGet
argument_list|()
expr_stmt|;
name|fail
argument_list|()
expr_stmt|;
block|}
block|}
block|}
block|}
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
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|failures
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**    * Test multi-threaded region mutations.    */
specifier|public
name|void
name|testMultiRowMutationMultiThreads
parameter_list|()
throws|throws
name|IOException
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Starting test testMultiRowMutationMultiThreads"
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
comment|// create 10 threads, each will alternate between adding and
comment|// removing a column
name|int
name|numThreads
init|=
literal|10
decl_stmt|;
name|int
name|opsPerThread
init|=
literal|500
decl_stmt|;
name|AtomicOperation
index|[]
name|all
init|=
operator|new
name|AtomicOperation
index|[
name|numThreads
index|]
decl_stmt|;
name|AtomicLong
name|timeStamps
init|=
operator|new
name|AtomicLong
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|AtomicInteger
name|failures
init|=
operator|new
name|AtomicInteger
argument_list|(
literal|0
argument_list|)
decl_stmt|;
specifier|final
name|List
argument_list|<
name|byte
index|[]
argument_list|>
name|rowsToLock
init|=
name|Arrays
operator|.
name|asList
argument_list|(
name|row
argument_list|,
name|row2
argument_list|)
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
name|AtomicOperation
argument_list|(
name|region
argument_list|,
name|opsPerThread
argument_list|,
name|timeStamps
argument_list|,
name|failures
argument_list|)
block|{
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
name|boolean
name|op
init|=
literal|true
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
name|numOps
condition|;
name|i
operator|++
control|)
block|{
try|try
block|{
comment|// throw in some flushes
if|if
condition|(
name|i
operator|%
literal|10
operator|==
literal|0
condition|)
block|{
synchronized|synchronized
init|(
name|region
init|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"flushing"
argument_list|)
expr_stmt|;
name|region
operator|.
name|flushcache
argument_list|()
expr_stmt|;
if|if
condition|(
name|i
operator|%
literal|100
operator|==
literal|0
condition|)
block|{
name|region
operator|.
name|compactStores
argument_list|()
expr_stmt|;
block|}
block|}
block|}
name|long
name|ts
init|=
name|timeStamps
operator|.
name|incrementAndGet
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|Mutation
argument_list|>
name|mrm
init|=
operator|new
name|ArrayList
argument_list|<
name|Mutation
argument_list|>
argument_list|()
decl_stmt|;
if|if
condition|(
name|op
condition|)
block|{
name|Put
name|p
init|=
operator|new
name|Put
argument_list|(
name|row2
argument_list|,
name|ts
argument_list|)
decl_stmt|;
name|p
operator|.
name|add
argument_list|(
name|fam1
argument_list|,
name|qual1
argument_list|,
name|value1
argument_list|)
expr_stmt|;
name|mrm
operator|.
name|add
argument_list|(
name|p
argument_list|)
expr_stmt|;
name|Delete
name|d
init|=
operator|new
name|Delete
argument_list|(
name|row
argument_list|)
decl_stmt|;
name|d
operator|.
name|deleteColumns
argument_list|(
name|fam1
argument_list|,
name|qual1
argument_list|,
name|ts
argument_list|)
expr_stmt|;
name|mrm
operator|.
name|add
argument_list|(
name|d
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|Delete
name|d
init|=
operator|new
name|Delete
argument_list|(
name|row2
argument_list|)
decl_stmt|;
name|d
operator|.
name|deleteColumns
argument_list|(
name|fam1
argument_list|,
name|qual1
argument_list|,
name|ts
argument_list|)
expr_stmt|;
name|mrm
operator|.
name|add
argument_list|(
name|d
argument_list|)
expr_stmt|;
name|Put
name|p
init|=
operator|new
name|Put
argument_list|(
name|row
argument_list|,
name|ts
argument_list|)
decl_stmt|;
name|p
operator|.
name|add
argument_list|(
name|fam1
argument_list|,
name|qual1
argument_list|,
name|value2
argument_list|)
expr_stmt|;
name|mrm
operator|.
name|add
argument_list|(
name|p
argument_list|)
expr_stmt|;
block|}
name|region
operator|.
name|mutateRowsWithLocks
argument_list|(
name|mrm
argument_list|,
name|rowsToLock
argument_list|)
expr_stmt|;
name|op
operator|^=
literal|true
expr_stmt|;
comment|// check: should always see exactly one column
name|Scan
name|s
init|=
operator|new
name|Scan
argument_list|(
name|row
argument_list|)
decl_stmt|;
name|RegionScanner
name|rs
init|=
name|region
operator|.
name|getScanner
argument_list|(
name|s
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|KeyValue
argument_list|>
name|r
init|=
operator|new
name|ArrayList
argument_list|<
name|KeyValue
argument_list|>
argument_list|()
decl_stmt|;
while|while
condition|(
name|rs
operator|.
name|next
argument_list|(
name|r
argument_list|)
condition|)
empty_stmt|;
name|rs
operator|.
name|close
argument_list|()
expr_stmt|;
if|if
condition|(
name|r
operator|.
name|size
argument_list|()
operator|!=
literal|1
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
name|r
argument_list|)
expr_stmt|;
name|failures
operator|.
name|incrementAndGet
argument_list|()
expr_stmt|;
name|fail
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
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
name|failures
operator|.
name|incrementAndGet
argument_list|()
expr_stmt|;
name|fail
argument_list|()
expr_stmt|;
block|}
block|}
block|}
block|}
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
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|failures
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
class|class
name|AtomicOperation
extends|extends
name|Thread
block|{
specifier|protected
specifier|final
name|HRegion
name|region
decl_stmt|;
specifier|protected
specifier|final
name|int
name|numOps
decl_stmt|;
specifier|protected
specifier|final
name|AtomicLong
name|timeStamps
decl_stmt|;
specifier|protected
specifier|final
name|AtomicInteger
name|failures
decl_stmt|;
specifier|protected
specifier|final
name|Random
name|r
init|=
operator|new
name|Random
argument_list|()
decl_stmt|;
specifier|public
name|AtomicOperation
parameter_list|(
name|HRegion
name|region
parameter_list|,
name|int
name|numOps
parameter_list|,
name|AtomicLong
name|timeStamps
parameter_list|,
name|AtomicInteger
name|failures
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
name|numOps
operator|=
name|numOps
expr_stmt|;
name|this
operator|.
name|timeStamps
operator|=
name|timeStamps
expr_stmt|;
name|this
operator|.
name|failures
operator|=
name|failures
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

