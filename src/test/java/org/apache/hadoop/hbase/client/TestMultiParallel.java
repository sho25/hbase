begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Copyright 2009 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|client
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
name|List
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
name|JVMClusterUtil
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|AfterClass
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Assert
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
name|BeforeClass
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
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|*
import|;
end_import

begin_class
specifier|public
class|class
name|TestMultiParallel
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
name|TestMultiParallel
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|HBaseTestingUtility
name|UTIL
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|VALUE
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"value"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|QUALIFIER
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"qual"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|FAMILY
init|=
literal|"family"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|TEST_TABLE
init|=
literal|"multi_test_table"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|BYTES_FAMILY
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|FAMILY
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|ONE_ROW
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"xxx"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
index|[]
name|KEYS
init|=
name|makeKeys
argument_list|()
decl_stmt|;
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|beforeClass
parameter_list|()
throws|throws
name|Exception
block|{
name|UTIL
operator|.
name|startMiniCluster
argument_list|(
literal|2
argument_list|)
expr_stmt|;
name|HTable
name|t
init|=
name|UTIL
operator|.
name|createTable
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|TEST_TABLE
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|FAMILY
argument_list|)
argument_list|)
decl_stmt|;
name|UTIL
operator|.
name|createMultiRegions
argument_list|(
name|t
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|FAMILY
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|AfterClass
specifier|public
specifier|static
name|void
name|afterClass
parameter_list|()
throws|throws
name|IOException
block|{
name|UTIL
operator|.
name|getMiniHBaseCluster
argument_list|()
operator|.
name|shutdown
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Before
specifier|public
name|void
name|before
parameter_list|()
throws|throws
name|IOException
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"before"
argument_list|)
expr_stmt|;
if|if
condition|(
name|UTIL
operator|.
name|ensureSomeRegionServersAvailable
argument_list|(
literal|2
argument_list|)
condition|)
block|{
comment|// Distribute regions
name|UTIL
operator|.
name|getMiniHBaseCluster
argument_list|()
operator|.
name|getMaster
argument_list|()
operator|.
name|balance
argument_list|()
expr_stmt|;
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"before done"
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|static
name|byte
index|[]
index|[]
name|makeKeys
parameter_list|()
block|{
name|byte
index|[]
index|[]
name|starterKeys
init|=
name|HBaseTestingUtility
operator|.
name|KEYS
decl_stmt|;
comment|// Create a "non-uniform" test set with the following characteristics:
comment|// a) Unequal number of keys per region
comment|// Don't use integer as a multiple, so that we have a number of keys that is
comment|// not a multiple of the number of regions
name|int
name|numKeys
init|=
call|(
name|int
call|)
argument_list|(
operator|(
name|float
operator|)
name|starterKeys
operator|.
name|length
operator|*
literal|10.33F
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|byte
index|[]
argument_list|>
name|keys
init|=
operator|new
name|ArrayList
argument_list|<
name|byte
index|[]
argument_list|>
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
name|numKeys
condition|;
name|i
operator|++
control|)
block|{
name|int
name|kIdx
init|=
name|i
operator|%
name|starterKeys
operator|.
name|length
decl_stmt|;
name|byte
index|[]
name|k
init|=
name|starterKeys
index|[
name|kIdx
index|]
decl_stmt|;
name|byte
index|[]
name|cp
init|=
operator|new
name|byte
index|[
name|k
operator|.
name|length
operator|+
literal|1
index|]
decl_stmt|;
name|System
operator|.
name|arraycopy
argument_list|(
name|k
argument_list|,
literal|0
argument_list|,
name|cp
argument_list|,
literal|0
argument_list|,
name|k
operator|.
name|length
argument_list|)
expr_stmt|;
name|cp
index|[
name|k
operator|.
name|length
index|]
operator|=
operator|new
name|Integer
argument_list|(
name|i
operator|%
literal|256
argument_list|)
operator|.
name|byteValue
argument_list|()
expr_stmt|;
name|keys
operator|.
name|add
argument_list|(
name|cp
argument_list|)
expr_stmt|;
block|}
comment|// b) Same duplicate keys (showing multiple Gets/Puts to the same row, which
comment|// should work)
comment|// c) keys are not in sorted order (within a region), to ensure that the
comment|// sorting code and index mapping doesn't break the functionality
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
literal|100
condition|;
name|i
operator|++
control|)
block|{
name|int
name|kIdx
init|=
name|i
operator|%
name|starterKeys
operator|.
name|length
decl_stmt|;
name|byte
index|[]
name|k
init|=
name|starterKeys
index|[
name|kIdx
index|]
decl_stmt|;
name|byte
index|[]
name|cp
init|=
operator|new
name|byte
index|[
name|k
operator|.
name|length
operator|+
literal|1
index|]
decl_stmt|;
name|System
operator|.
name|arraycopy
argument_list|(
name|k
argument_list|,
literal|0
argument_list|,
name|cp
argument_list|,
literal|0
argument_list|,
name|k
operator|.
name|length
argument_list|)
expr_stmt|;
name|cp
index|[
name|k
operator|.
name|length
index|]
operator|=
operator|new
name|Integer
argument_list|(
name|i
operator|%
literal|256
argument_list|)
operator|.
name|byteValue
argument_list|()
expr_stmt|;
name|keys
operator|.
name|add
argument_list|(
name|cp
argument_list|)
expr_stmt|;
block|}
return|return
name|keys
operator|.
name|toArray
argument_list|(
operator|new
name|byte
index|[]
index|[]
block|{
operator|new
name|byte
index|[]
block|{}
block|}
argument_list|)
return|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testBatchWithGet
parameter_list|()
throws|throws
name|Exception
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"test=testBatchWithGet"
argument_list|)
expr_stmt|;
name|HTable
name|table
init|=
operator|new
name|HTable
argument_list|(
name|UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|TEST_TABLE
argument_list|)
decl_stmt|;
comment|// load test data
name|List
argument_list|<
name|Row
argument_list|>
name|puts
init|=
name|constructPutRequests
argument_list|()
decl_stmt|;
name|table
operator|.
name|batch
argument_list|(
name|puts
argument_list|)
expr_stmt|;
comment|// create a list of gets and run it
name|List
argument_list|<
name|Row
argument_list|>
name|gets
init|=
operator|new
name|ArrayList
argument_list|<
name|Row
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|byte
index|[]
name|k
range|:
name|KEYS
control|)
block|{
name|Get
name|get
init|=
operator|new
name|Get
argument_list|(
name|k
argument_list|)
decl_stmt|;
name|get
operator|.
name|addColumn
argument_list|(
name|BYTES_FAMILY
argument_list|,
name|QUALIFIER
argument_list|)
expr_stmt|;
name|gets
operator|.
name|add
argument_list|(
name|get
argument_list|)
expr_stmt|;
block|}
name|Result
index|[]
name|multiRes
init|=
operator|new
name|Result
index|[
name|gets
operator|.
name|size
argument_list|()
index|]
decl_stmt|;
name|table
operator|.
name|batch
argument_list|(
name|gets
argument_list|,
name|multiRes
argument_list|)
expr_stmt|;
comment|// Same gets using individual call API
name|List
argument_list|<
name|Result
argument_list|>
name|singleRes
init|=
operator|new
name|ArrayList
argument_list|<
name|Result
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|Row
name|get
range|:
name|gets
control|)
block|{
name|singleRes
operator|.
name|add
argument_list|(
name|table
operator|.
name|get
argument_list|(
operator|(
name|Get
operator|)
name|get
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// Compare results
name|Assert
operator|.
name|assertEquals
argument_list|(
name|singleRes
operator|.
name|size
argument_list|()
argument_list|,
name|multiRes
operator|.
name|length
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
name|singleRes
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|Assert
operator|.
name|assertTrue
argument_list|(
name|singleRes
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|containsColumn
argument_list|(
name|BYTES_FAMILY
argument_list|,
name|QUALIFIER
argument_list|)
argument_list|)
expr_stmt|;
name|KeyValue
index|[]
name|singleKvs
init|=
name|singleRes
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|raw
argument_list|()
decl_stmt|;
name|KeyValue
index|[]
name|multiKvs
init|=
name|multiRes
index|[
name|i
index|]
operator|.
name|raw
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
name|singleKvs
operator|.
name|length
condition|;
name|j
operator|++
control|)
block|{
name|Assert
operator|.
name|assertEquals
argument_list|(
name|singleKvs
index|[
name|j
index|]
argument_list|,
name|multiKvs
index|[
name|j
index|]
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|Bytes
operator|.
name|compareTo
argument_list|(
name|singleKvs
index|[
name|j
index|]
operator|.
name|getValue
argument_list|()
argument_list|,
name|multiKvs
index|[
name|j
index|]
operator|.
name|getValue
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testBadFam
parameter_list|()
throws|throws
name|Exception
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"test=testBadFam"
argument_list|)
expr_stmt|;
name|HTable
name|table
init|=
operator|new
name|HTable
argument_list|(
name|UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|TEST_TABLE
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Row
argument_list|>
name|actions
init|=
operator|new
name|ArrayList
argument_list|<
name|Row
argument_list|>
argument_list|()
decl_stmt|;
name|Put
name|p
init|=
operator|new
name|Put
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"row1"
argument_list|)
argument_list|)
decl_stmt|;
name|p
operator|.
name|add
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"bad_family"
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"qual"
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"value"
argument_list|)
argument_list|)
expr_stmt|;
name|actions
operator|.
name|add
argument_list|(
name|p
argument_list|)
expr_stmt|;
name|p
operator|=
operator|new
name|Put
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"row2"
argument_list|)
argument_list|)
expr_stmt|;
name|p
operator|.
name|add
argument_list|(
name|BYTES_FAMILY
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"qual"
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"value"
argument_list|)
argument_list|)
expr_stmt|;
name|actions
operator|.
name|add
argument_list|(
name|p
argument_list|)
expr_stmt|;
comment|// row1 and row2 should be in the same region.
name|Object
index|[]
name|r
init|=
operator|new
name|Object
index|[
name|actions
operator|.
name|size
argument_list|()
index|]
decl_stmt|;
try|try
block|{
name|table
operator|.
name|batch
argument_list|(
name|actions
argument_list|,
name|r
argument_list|)
expr_stmt|;
name|fail
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|RetriesExhaustedWithDetailsException
name|ex
parameter_list|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
name|ex
argument_list|)
expr_stmt|;
comment|// good!
name|assertFalse
argument_list|(
name|ex
operator|.
name|mayHaveClusterIssues
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|r
operator|.
name|length
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|r
index|[
literal|0
index|]
operator|instanceof
name|Throwable
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|r
index|[
literal|1
index|]
operator|instanceof
name|Result
argument_list|)
expr_stmt|;
block|}
comment|/**    * Only run one Multi test with a forced RegionServer abort. Otherwise, the    * unit tests will take an unnecessarily long time to run.    *    * @throws Exception    */
annotation|@
name|Test
specifier|public
name|void
name|testFlushCommitsWithAbort
parameter_list|()
throws|throws
name|Exception
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"test=testFlushCommitsWithAbort"
argument_list|)
expr_stmt|;
name|doTestFlushCommits
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testFlushCommitsNoAbort
parameter_list|()
throws|throws
name|Exception
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"test=testFlushCommitsNoAbort"
argument_list|)
expr_stmt|;
name|doTestFlushCommits
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|doTestFlushCommits
parameter_list|(
name|boolean
name|doAbort
parameter_list|)
throws|throws
name|Exception
block|{
comment|// Load the data
name|LOG
operator|.
name|info
argument_list|(
literal|"get new table"
argument_list|)
expr_stmt|;
name|HTable
name|table
init|=
operator|new
name|HTable
argument_list|(
name|UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|TEST_TABLE
argument_list|)
decl_stmt|;
name|table
operator|.
name|setAutoFlush
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|table
operator|.
name|setWriteBufferSize
argument_list|(
literal|10
operator|*
literal|1024
operator|*
literal|1024
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"constructPutRequests"
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|Row
argument_list|>
name|puts
init|=
name|constructPutRequests
argument_list|()
decl_stmt|;
for|for
control|(
name|Row
name|put
range|:
name|puts
control|)
block|{
name|table
operator|.
name|put
argument_list|(
operator|(
name|Put
operator|)
name|put
argument_list|)
expr_stmt|;
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"puts"
argument_list|)
expr_stmt|;
name|table
operator|.
name|flushCommits
argument_list|()
expr_stmt|;
if|if
condition|(
name|doAbort
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Aborted="
operator|+
name|UTIL
operator|.
name|getMiniHBaseCluster
argument_list|()
operator|.
name|abortRegionServer
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
comment|// try putting more keys after the abort. same key/qual... just validating
comment|// no exceptions thrown
name|puts
operator|=
name|constructPutRequests
argument_list|()
expr_stmt|;
for|for
control|(
name|Row
name|put
range|:
name|puts
control|)
block|{
name|table
operator|.
name|put
argument_list|(
operator|(
name|Put
operator|)
name|put
argument_list|)
expr_stmt|;
block|}
name|table
operator|.
name|flushCommits
argument_list|()
expr_stmt|;
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"validating loaded data"
argument_list|)
expr_stmt|;
name|validateLoadedData
argument_list|(
name|table
argument_list|)
expr_stmt|;
comment|// Validate server and region count
name|List
argument_list|<
name|JVMClusterUtil
operator|.
name|RegionServerThread
argument_list|>
name|liveRSs
init|=
name|UTIL
operator|.
name|getMiniHBaseCluster
argument_list|()
operator|.
name|getLiveRegionServerThreads
argument_list|()
decl_stmt|;
name|int
name|count
init|=
literal|0
decl_stmt|;
for|for
control|(
name|JVMClusterUtil
operator|.
name|RegionServerThread
name|t
range|:
name|liveRSs
control|)
block|{
name|count
operator|++
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Count="
operator|+
name|count
operator|+
literal|", Alive="
operator|+
name|t
operator|.
name|getRegionServer
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"Count="
operator|+
name|count
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"Server count="
operator|+
name|count
operator|+
literal|", abort="
operator|+
name|doAbort
argument_list|,
operator|(
name|doAbort
condition|?
literal|1
else|:
literal|2
operator|)
argument_list|,
name|count
argument_list|)
expr_stmt|;
for|for
control|(
name|JVMClusterUtil
operator|.
name|RegionServerThread
name|t
range|:
name|liveRSs
control|)
block|{
name|int
name|regions
init|=
name|t
operator|.
name|getRegionServer
argument_list|()
operator|.
name|getOnlineRegions
argument_list|()
operator|.
name|size
argument_list|()
decl_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
literal|"Count of regions="
operator|+
name|regions
argument_list|,
name|regions
operator|>
literal|10
argument_list|)
expr_stmt|;
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"done"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testBatchWithPut
parameter_list|()
throws|throws
name|Exception
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"test=testBatchWithPut"
argument_list|)
expr_stmt|;
name|HTable
name|table
init|=
operator|new
name|HTable
argument_list|(
name|UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|TEST_TABLE
argument_list|)
decl_stmt|;
comment|// put multiple rows using a batch
name|List
argument_list|<
name|Row
argument_list|>
name|puts
init|=
name|constructPutRequests
argument_list|()
decl_stmt|;
name|Object
index|[]
name|results
init|=
name|table
operator|.
name|batch
argument_list|(
name|puts
argument_list|)
decl_stmt|;
name|validateSizeAndEmpty
argument_list|(
name|results
argument_list|,
name|KEYS
operator|.
name|length
argument_list|)
expr_stmt|;
if|if
condition|(
literal|true
condition|)
block|{
name|UTIL
operator|.
name|getMiniHBaseCluster
argument_list|()
operator|.
name|abortRegionServer
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|puts
operator|=
name|constructPutRequests
argument_list|()
expr_stmt|;
name|results
operator|=
name|table
operator|.
name|batch
argument_list|(
name|puts
argument_list|)
expr_stmt|;
name|validateSizeAndEmpty
argument_list|(
name|results
argument_list|,
name|KEYS
operator|.
name|length
argument_list|)
expr_stmt|;
block|}
name|validateLoadedData
argument_list|(
name|table
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testBatchWithDelete
parameter_list|()
throws|throws
name|Exception
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"test=testBatchWithDelete"
argument_list|)
expr_stmt|;
name|HTable
name|table
init|=
operator|new
name|HTable
argument_list|(
name|UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|TEST_TABLE
argument_list|)
decl_stmt|;
comment|// Load some data
name|List
argument_list|<
name|Row
argument_list|>
name|puts
init|=
name|constructPutRequests
argument_list|()
decl_stmt|;
name|Object
index|[]
name|results
init|=
name|table
operator|.
name|batch
argument_list|(
name|puts
argument_list|)
decl_stmt|;
name|validateSizeAndEmpty
argument_list|(
name|results
argument_list|,
name|KEYS
operator|.
name|length
argument_list|)
expr_stmt|;
comment|// Deletes
name|List
argument_list|<
name|Row
argument_list|>
name|deletes
init|=
operator|new
name|ArrayList
argument_list|<
name|Row
argument_list|>
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
name|KEYS
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|Delete
name|delete
init|=
operator|new
name|Delete
argument_list|(
name|KEYS
index|[
name|i
index|]
argument_list|)
decl_stmt|;
name|delete
operator|.
name|deleteFamily
argument_list|(
name|BYTES_FAMILY
argument_list|)
expr_stmt|;
name|deletes
operator|.
name|add
argument_list|(
name|delete
argument_list|)
expr_stmt|;
block|}
name|results
operator|=
name|table
operator|.
name|batch
argument_list|(
name|deletes
argument_list|)
expr_stmt|;
name|validateSizeAndEmpty
argument_list|(
name|results
argument_list|,
name|KEYS
operator|.
name|length
argument_list|)
expr_stmt|;
comment|// Get to make sure ...
for|for
control|(
name|byte
index|[]
name|k
range|:
name|KEYS
control|)
block|{
name|Get
name|get
init|=
operator|new
name|Get
argument_list|(
name|k
argument_list|)
decl_stmt|;
name|get
operator|.
name|addColumn
argument_list|(
name|BYTES_FAMILY
argument_list|,
name|QUALIFIER
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertFalse
argument_list|(
name|table
operator|.
name|exists
argument_list|(
name|get
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testHTableDeleteWithList
parameter_list|()
throws|throws
name|Exception
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"test=testHTableDeleteWithList"
argument_list|)
expr_stmt|;
name|HTable
name|table
init|=
operator|new
name|HTable
argument_list|(
name|UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|TEST_TABLE
argument_list|)
decl_stmt|;
comment|// Load some data
name|List
argument_list|<
name|Row
argument_list|>
name|puts
init|=
name|constructPutRequests
argument_list|()
decl_stmt|;
name|Object
index|[]
name|results
init|=
name|table
operator|.
name|batch
argument_list|(
name|puts
argument_list|)
decl_stmt|;
name|validateSizeAndEmpty
argument_list|(
name|results
argument_list|,
name|KEYS
operator|.
name|length
argument_list|)
expr_stmt|;
comment|// Deletes
name|ArrayList
argument_list|<
name|Delete
argument_list|>
name|deletes
init|=
operator|new
name|ArrayList
argument_list|<
name|Delete
argument_list|>
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
name|KEYS
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|Delete
name|delete
init|=
operator|new
name|Delete
argument_list|(
name|KEYS
index|[
name|i
index|]
argument_list|)
decl_stmt|;
name|delete
operator|.
name|deleteFamily
argument_list|(
name|BYTES_FAMILY
argument_list|)
expr_stmt|;
name|deletes
operator|.
name|add
argument_list|(
name|delete
argument_list|)
expr_stmt|;
block|}
name|table
operator|.
name|delete
argument_list|(
name|deletes
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|deletes
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
comment|// Get to make sure ...
for|for
control|(
name|byte
index|[]
name|k
range|:
name|KEYS
control|)
block|{
name|Get
name|get
init|=
operator|new
name|Get
argument_list|(
name|k
argument_list|)
decl_stmt|;
name|get
operator|.
name|addColumn
argument_list|(
name|BYTES_FAMILY
argument_list|,
name|QUALIFIER
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertFalse
argument_list|(
name|table
operator|.
name|exists
argument_list|(
name|get
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testBatchWithManyColsInOneRowGetAndPut
parameter_list|()
throws|throws
name|Exception
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"test=testBatchWithManyColsInOneRowGetAndPut"
argument_list|)
expr_stmt|;
name|HTable
name|table
init|=
operator|new
name|HTable
argument_list|(
name|UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|TEST_TABLE
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Row
argument_list|>
name|puts
init|=
operator|new
name|ArrayList
argument_list|<
name|Row
argument_list|>
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
literal|100
condition|;
name|i
operator|++
control|)
block|{
name|Put
name|put
init|=
operator|new
name|Put
argument_list|(
name|ONE_ROW
argument_list|)
decl_stmt|;
name|byte
index|[]
name|qual
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"column"
operator|+
name|i
argument_list|)
decl_stmt|;
name|put
operator|.
name|add
argument_list|(
name|BYTES_FAMILY
argument_list|,
name|qual
argument_list|,
name|VALUE
argument_list|)
expr_stmt|;
name|puts
operator|.
name|add
argument_list|(
name|put
argument_list|)
expr_stmt|;
block|}
name|Object
index|[]
name|results
init|=
name|table
operator|.
name|batch
argument_list|(
name|puts
argument_list|)
decl_stmt|;
comment|// validate
name|validateSizeAndEmpty
argument_list|(
name|results
argument_list|,
literal|100
argument_list|)
expr_stmt|;
comment|// get the data back and validate that it is correct
name|List
argument_list|<
name|Row
argument_list|>
name|gets
init|=
operator|new
name|ArrayList
argument_list|<
name|Row
argument_list|>
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
literal|100
condition|;
name|i
operator|++
control|)
block|{
name|Get
name|get
init|=
operator|new
name|Get
argument_list|(
name|ONE_ROW
argument_list|)
decl_stmt|;
name|byte
index|[]
name|qual
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"column"
operator|+
name|i
argument_list|)
decl_stmt|;
name|get
operator|.
name|addColumn
argument_list|(
name|BYTES_FAMILY
argument_list|,
name|qual
argument_list|)
expr_stmt|;
name|gets
operator|.
name|add
argument_list|(
name|get
argument_list|)
expr_stmt|;
block|}
name|Object
index|[]
name|multiRes
init|=
name|table
operator|.
name|batch
argument_list|(
name|gets
argument_list|)
decl_stmt|;
name|int
name|idx
init|=
literal|0
decl_stmt|;
for|for
control|(
name|Object
name|r
range|:
name|multiRes
control|)
block|{
name|byte
index|[]
name|qual
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"column"
operator|+
name|idx
argument_list|)
decl_stmt|;
name|validateResult
argument_list|(
name|r
argument_list|,
name|qual
argument_list|,
name|VALUE
argument_list|)
expr_stmt|;
name|idx
operator|++
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testBatchWithMixedActions
parameter_list|()
throws|throws
name|Exception
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"test=testBatchWithMixedActions"
argument_list|)
expr_stmt|;
name|HTable
name|table
init|=
operator|new
name|HTable
argument_list|(
name|UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|TEST_TABLE
argument_list|)
decl_stmt|;
comment|// Load some data to start
name|Object
index|[]
name|results
init|=
name|table
operator|.
name|batch
argument_list|(
name|constructPutRequests
argument_list|()
argument_list|)
decl_stmt|;
name|validateSizeAndEmpty
argument_list|(
name|results
argument_list|,
name|KEYS
operator|.
name|length
argument_list|)
expr_stmt|;
comment|// Batch: get, get, put(new col), delete, get, get of put, get of deleted,
comment|// put
name|List
argument_list|<
name|Row
argument_list|>
name|actions
init|=
operator|new
name|ArrayList
argument_list|<
name|Row
argument_list|>
argument_list|()
decl_stmt|;
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
name|byte
index|[]
name|val2
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"putvalue2"
argument_list|)
decl_stmt|;
comment|// 0 get
name|Get
name|get
init|=
operator|new
name|Get
argument_list|(
name|KEYS
index|[
literal|10
index|]
argument_list|)
decl_stmt|;
name|get
operator|.
name|addColumn
argument_list|(
name|BYTES_FAMILY
argument_list|,
name|QUALIFIER
argument_list|)
expr_stmt|;
name|actions
operator|.
name|add
argument_list|(
name|get
argument_list|)
expr_stmt|;
comment|// 1 get
name|get
operator|=
operator|new
name|Get
argument_list|(
name|KEYS
index|[
literal|11
index|]
argument_list|)
expr_stmt|;
name|get
operator|.
name|addColumn
argument_list|(
name|BYTES_FAMILY
argument_list|,
name|QUALIFIER
argument_list|)
expr_stmt|;
name|actions
operator|.
name|add
argument_list|(
name|get
argument_list|)
expr_stmt|;
comment|// 2 put of new column
name|Put
name|put
init|=
operator|new
name|Put
argument_list|(
name|KEYS
index|[
literal|10
index|]
argument_list|)
decl_stmt|;
name|put
operator|.
name|add
argument_list|(
name|BYTES_FAMILY
argument_list|,
name|qual2
argument_list|,
name|val2
argument_list|)
expr_stmt|;
name|actions
operator|.
name|add
argument_list|(
name|put
argument_list|)
expr_stmt|;
comment|// 3 delete
name|Delete
name|delete
init|=
operator|new
name|Delete
argument_list|(
name|KEYS
index|[
literal|20
index|]
argument_list|)
decl_stmt|;
name|delete
operator|.
name|deleteFamily
argument_list|(
name|BYTES_FAMILY
argument_list|)
expr_stmt|;
name|actions
operator|.
name|add
argument_list|(
name|delete
argument_list|)
expr_stmt|;
comment|// 4 get
name|get
operator|=
operator|new
name|Get
argument_list|(
name|KEYS
index|[
literal|30
index|]
argument_list|)
expr_stmt|;
name|get
operator|.
name|addColumn
argument_list|(
name|BYTES_FAMILY
argument_list|,
name|QUALIFIER
argument_list|)
expr_stmt|;
name|actions
operator|.
name|add
argument_list|(
name|get
argument_list|)
expr_stmt|;
comment|// There used to be a 'get' of a previous put here, but removed
comment|// since this API really cannot guarantee order in terms of mixed
comment|// get/puts.
comment|// 5 put of new column
name|put
operator|=
operator|new
name|Put
argument_list|(
name|KEYS
index|[
literal|40
index|]
argument_list|)
expr_stmt|;
name|put
operator|.
name|add
argument_list|(
name|BYTES_FAMILY
argument_list|,
name|qual2
argument_list|,
name|val2
argument_list|)
expr_stmt|;
name|actions
operator|.
name|add
argument_list|(
name|put
argument_list|)
expr_stmt|;
name|results
operator|=
name|table
operator|.
name|batch
argument_list|(
name|actions
argument_list|)
expr_stmt|;
comment|// Validation
name|validateResult
argument_list|(
name|results
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
name|validateResult
argument_list|(
name|results
index|[
literal|1
index|]
argument_list|)
expr_stmt|;
name|validateEmpty
argument_list|(
name|results
index|[
literal|2
index|]
argument_list|)
expr_stmt|;
name|validateEmpty
argument_list|(
name|results
index|[
literal|3
index|]
argument_list|)
expr_stmt|;
name|validateResult
argument_list|(
name|results
index|[
literal|4
index|]
argument_list|)
expr_stmt|;
name|validateEmpty
argument_list|(
name|results
index|[
literal|5
index|]
argument_list|)
expr_stmt|;
comment|// validate last put, externally from the batch
name|get
operator|=
operator|new
name|Get
argument_list|(
name|KEYS
index|[
literal|40
index|]
argument_list|)
expr_stmt|;
name|get
operator|.
name|addColumn
argument_list|(
name|BYTES_FAMILY
argument_list|,
name|qual2
argument_list|)
expr_stmt|;
name|Result
name|r
init|=
name|table
operator|.
name|get
argument_list|(
name|get
argument_list|)
decl_stmt|;
name|validateResult
argument_list|(
name|r
argument_list|,
name|qual2
argument_list|,
name|val2
argument_list|)
expr_stmt|;
block|}
comment|// // Helper methods ////
specifier|private
name|void
name|validateResult
parameter_list|(
name|Object
name|r
parameter_list|)
block|{
name|validateResult
argument_list|(
name|r
argument_list|,
name|QUALIFIER
argument_list|,
name|VALUE
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|validateResult
parameter_list|(
name|Object
name|r1
parameter_list|,
name|byte
index|[]
name|qual
parameter_list|,
name|byte
index|[]
name|val
parameter_list|)
block|{
comment|// TODO provide nice assert here or something.
name|Result
name|r
init|=
operator|(
name|Result
operator|)
name|r1
decl_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|r
operator|.
name|containsColumn
argument_list|(
name|BYTES_FAMILY
argument_list|,
name|qual
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|Bytes
operator|.
name|compareTo
argument_list|(
name|val
argument_list|,
name|r
operator|.
name|getValue
argument_list|(
name|BYTES_FAMILY
argument_list|,
name|qual
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|private
name|List
argument_list|<
name|Row
argument_list|>
name|constructPutRequests
parameter_list|()
block|{
name|List
argument_list|<
name|Row
argument_list|>
name|puts
init|=
operator|new
name|ArrayList
argument_list|<
name|Row
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|byte
index|[]
name|k
range|:
name|KEYS
control|)
block|{
name|Put
name|put
init|=
operator|new
name|Put
argument_list|(
name|k
argument_list|)
decl_stmt|;
name|put
operator|.
name|add
argument_list|(
name|BYTES_FAMILY
argument_list|,
name|QUALIFIER
argument_list|,
name|VALUE
argument_list|)
expr_stmt|;
name|puts
operator|.
name|add
argument_list|(
name|put
argument_list|)
expr_stmt|;
block|}
return|return
name|puts
return|;
block|}
specifier|private
name|void
name|validateLoadedData
parameter_list|(
name|HTable
name|table
parameter_list|)
throws|throws
name|IOException
block|{
comment|// get the data back and validate that it is correct
for|for
control|(
name|byte
index|[]
name|k
range|:
name|KEYS
control|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Assert="
operator|+
name|Bytes
operator|.
name|toString
argument_list|(
name|k
argument_list|)
argument_list|)
expr_stmt|;
name|Get
name|get
init|=
operator|new
name|Get
argument_list|(
name|k
argument_list|)
decl_stmt|;
name|get
operator|.
name|addColumn
argument_list|(
name|BYTES_FAMILY
argument_list|,
name|QUALIFIER
argument_list|)
expr_stmt|;
name|Result
name|r
init|=
name|table
operator|.
name|get
argument_list|(
name|get
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|r
operator|.
name|containsColumn
argument_list|(
name|BYTES_FAMILY
argument_list|,
name|QUALIFIER
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|Bytes
operator|.
name|compareTo
argument_list|(
name|VALUE
argument_list|,
name|r
operator|.
name|getValue
argument_list|(
name|BYTES_FAMILY
argument_list|,
name|QUALIFIER
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|validateEmpty
parameter_list|(
name|Object
name|r1
parameter_list|)
block|{
name|Result
name|result
init|=
operator|(
name|Result
operator|)
name|r1
decl_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|result
operator|!=
literal|null
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|result
operator|.
name|getRow
argument_list|()
operator|==
literal|null
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|result
operator|.
name|raw
argument_list|()
operator|.
name|length
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|validateSizeAndEmpty
parameter_list|(
name|Object
index|[]
name|results
parameter_list|,
name|int
name|expectedSize
parameter_list|)
block|{
comment|// Validate got back the same number of Result objects, all empty
name|Assert
operator|.
name|assertEquals
argument_list|(
name|expectedSize
argument_list|,
name|results
operator|.
name|length
argument_list|)
expr_stmt|;
for|for
control|(
name|Object
name|result
range|:
name|results
control|)
block|{
name|validateEmpty
argument_list|(
name|result
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

