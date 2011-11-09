begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2011 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|mapred
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
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertTrue
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|mockito
operator|.
name|Matchers
operator|.
name|anyObject
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|mockito
operator|.
name|Mockito
operator|.
name|doAnswer
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|mockito
operator|.
name|Mockito
operator|.
name|doReturn
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|mockito
operator|.
name|Mockito
operator|.
name|doThrow
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|mockito
operator|.
name|Mockito
operator|.
name|mock
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|mockito
operator|.
name|Mockito
operator|.
name|spy
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
name|Arrays
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
name|io
operator|.
name|ImmutableBytesWritable
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
name|AfterClass
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
name|mockito
operator|.
name|invocation
operator|.
name|InvocationOnMock
import|;
end_import

begin_import
import|import
name|org
operator|.
name|mockito
operator|.
name|stubbing
operator|.
name|Answer
import|;
end_import

begin_comment
comment|/**  * This tests the TableInputFormat and its recovery semantics  *   */
end_comment

begin_class
annotation|@
name|Category
argument_list|(
name|LargeTests
operator|.
name|class
argument_list|)
specifier|public
class|class
name|TestTableInputFormat
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
name|TestTableInputFormat
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
specifier|static
name|HBaseTestingUtility
name|UTIL
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
specifier|static
specifier|final
name|byte
index|[]
name|FAMILY
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"family"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
index|[]
name|columns
init|=
operator|new
name|byte
index|[]
index|[]
block|{
name|FAMILY
block|}
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
argument_list|()
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
name|Exception
block|{
name|UTIL
operator|.
name|shutdownMiniCluster
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
name|UTIL
operator|.
name|ensureSomeRegionServersAvailable
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"before done"
argument_list|)
expr_stmt|;
block|}
comment|/**    * Setup a table with two rows and values.    *     * @param tableName    * @return    * @throws IOException    */
specifier|public
specifier|static
name|HTable
name|createTable
parameter_list|(
name|byte
index|[]
name|tableName
parameter_list|)
throws|throws
name|IOException
block|{
name|HTable
name|table
init|=
name|UTIL
operator|.
name|createTable
argument_list|(
name|tableName
argument_list|,
name|FAMILY
argument_list|)
decl_stmt|;
name|Put
name|p
init|=
operator|new
name|Put
argument_list|(
literal|"aaa"
operator|.
name|getBytes
argument_list|()
argument_list|)
decl_stmt|;
name|p
operator|.
name|add
argument_list|(
name|FAMILY
argument_list|,
literal|null
argument_list|,
literal|"value aaa"
operator|.
name|getBytes
argument_list|()
argument_list|)
expr_stmt|;
name|table
operator|.
name|put
argument_list|(
name|p
argument_list|)
expr_stmt|;
name|p
operator|=
operator|new
name|Put
argument_list|(
literal|"bbb"
operator|.
name|getBytes
argument_list|()
argument_list|)
expr_stmt|;
name|p
operator|.
name|add
argument_list|(
name|FAMILY
argument_list|,
literal|null
argument_list|,
literal|"value bbb"
operator|.
name|getBytes
argument_list|()
argument_list|)
expr_stmt|;
name|table
operator|.
name|put
argument_list|(
name|p
argument_list|)
expr_stmt|;
return|return
name|table
return|;
block|}
comment|/**    * Verify that the result and key have expected values.    *     * @param r    * @param key    * @param expectedKey    * @param expectedValue    * @return    */
specifier|static
name|boolean
name|checkResult
parameter_list|(
name|Result
name|r
parameter_list|,
name|ImmutableBytesWritable
name|key
parameter_list|,
name|byte
index|[]
name|expectedKey
parameter_list|,
name|byte
index|[]
name|expectedValue
parameter_list|)
block|{
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|key
operator|.
name|compareTo
argument_list|(
name|expectedKey
argument_list|)
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|byte
index|[]
argument_list|,
name|byte
index|[]
argument_list|>
name|vals
init|=
name|r
operator|.
name|getFamilyMap
argument_list|(
name|FAMILY
argument_list|)
decl_stmt|;
name|byte
index|[]
name|value
init|=
name|vals
operator|.
name|values
argument_list|()
operator|.
name|iterator
argument_list|()
operator|.
name|next
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
name|Arrays
operator|.
name|equals
argument_list|(
name|value
argument_list|,
name|expectedValue
argument_list|)
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
comment|// if succeed
block|}
comment|/**    * Create table data and run tests on specified htable using the    * o.a.h.hbase.mapred API.    *     * @param table    * @throws IOException    */
specifier|static
name|void
name|runTestMapred
parameter_list|(
name|HTable
name|table
parameter_list|)
throws|throws
name|IOException
block|{
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|mapred
operator|.
name|TableRecordReader
name|trr
init|=
operator|new
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|mapred
operator|.
name|TableRecordReader
argument_list|()
decl_stmt|;
name|trr
operator|.
name|setStartRow
argument_list|(
literal|"aaa"
operator|.
name|getBytes
argument_list|()
argument_list|)
expr_stmt|;
name|trr
operator|.
name|setEndRow
argument_list|(
literal|"zzz"
operator|.
name|getBytes
argument_list|()
argument_list|)
expr_stmt|;
name|trr
operator|.
name|setHTable
argument_list|(
name|table
argument_list|)
expr_stmt|;
name|trr
operator|.
name|setInputColumns
argument_list|(
name|columns
argument_list|)
expr_stmt|;
name|trr
operator|.
name|init
argument_list|()
expr_stmt|;
name|Result
name|r
init|=
operator|new
name|Result
argument_list|()
decl_stmt|;
name|ImmutableBytesWritable
name|key
init|=
operator|new
name|ImmutableBytesWritable
argument_list|()
decl_stmt|;
name|boolean
name|more
init|=
name|trr
operator|.
name|next
argument_list|(
name|key
argument_list|,
name|r
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|more
argument_list|)
expr_stmt|;
name|checkResult
argument_list|(
name|r
argument_list|,
name|key
argument_list|,
literal|"aaa"
operator|.
name|getBytes
argument_list|()
argument_list|,
literal|"value aaa"
operator|.
name|getBytes
argument_list|()
argument_list|)
expr_stmt|;
name|more
operator|=
name|trr
operator|.
name|next
argument_list|(
name|key
argument_list|,
name|r
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|more
argument_list|)
expr_stmt|;
name|checkResult
argument_list|(
name|r
argument_list|,
name|key
argument_list|,
literal|"bbb"
operator|.
name|getBytes
argument_list|()
argument_list|,
literal|"value bbb"
operator|.
name|getBytes
argument_list|()
argument_list|)
expr_stmt|;
comment|// no more data
name|more
operator|=
name|trr
operator|.
name|next
argument_list|(
name|key
argument_list|,
name|r
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|more
argument_list|)
expr_stmt|;
block|}
comment|/**    * Create table data and run tests on specified htable using the    * o.a.h.hbase.mapreduce API.    *     * @param table    * @throws IOException    * @throws InterruptedException    */
specifier|static
name|void
name|runTestMapreduce
parameter_list|(
name|HTable
name|table
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|mapreduce
operator|.
name|TableRecordReaderImpl
name|trr
init|=
operator|new
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|mapreduce
operator|.
name|TableRecordReaderImpl
argument_list|()
decl_stmt|;
name|Scan
name|s
init|=
operator|new
name|Scan
argument_list|()
decl_stmt|;
name|s
operator|.
name|setStartRow
argument_list|(
literal|"aaa"
operator|.
name|getBytes
argument_list|()
argument_list|)
expr_stmt|;
name|s
operator|.
name|setStopRow
argument_list|(
literal|"zzz"
operator|.
name|getBytes
argument_list|()
argument_list|)
expr_stmt|;
name|s
operator|.
name|addFamily
argument_list|(
name|FAMILY
argument_list|)
expr_stmt|;
name|trr
operator|.
name|setScan
argument_list|(
name|s
argument_list|)
expr_stmt|;
name|trr
operator|.
name|setHTable
argument_list|(
name|table
argument_list|)
expr_stmt|;
name|trr
operator|.
name|initialize
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|Result
name|r
init|=
operator|new
name|Result
argument_list|()
decl_stmt|;
name|ImmutableBytesWritable
name|key
init|=
operator|new
name|ImmutableBytesWritable
argument_list|()
decl_stmt|;
name|boolean
name|more
init|=
name|trr
operator|.
name|nextKeyValue
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
name|more
argument_list|)
expr_stmt|;
name|key
operator|=
name|trr
operator|.
name|getCurrentKey
argument_list|()
expr_stmt|;
name|r
operator|=
name|trr
operator|.
name|getCurrentValue
argument_list|()
expr_stmt|;
name|checkResult
argument_list|(
name|r
argument_list|,
name|key
argument_list|,
literal|"aaa"
operator|.
name|getBytes
argument_list|()
argument_list|,
literal|"value aaa"
operator|.
name|getBytes
argument_list|()
argument_list|)
expr_stmt|;
name|more
operator|=
name|trr
operator|.
name|nextKeyValue
argument_list|()
expr_stmt|;
name|assertTrue
argument_list|(
name|more
argument_list|)
expr_stmt|;
name|key
operator|=
name|trr
operator|.
name|getCurrentKey
argument_list|()
expr_stmt|;
name|r
operator|=
name|trr
operator|.
name|getCurrentValue
argument_list|()
expr_stmt|;
name|checkResult
argument_list|(
name|r
argument_list|,
name|key
argument_list|,
literal|"bbb"
operator|.
name|getBytes
argument_list|()
argument_list|,
literal|"value bbb"
operator|.
name|getBytes
argument_list|()
argument_list|)
expr_stmt|;
comment|// no more data
name|more
operator|=
name|trr
operator|.
name|nextKeyValue
argument_list|()
expr_stmt|;
name|assertFalse
argument_list|(
name|more
argument_list|)
expr_stmt|;
block|}
comment|/**    * Create a table that IOE's on first scanner next call    *     * @throws IOException    */
specifier|static
name|HTable
name|createIOEScannerTable
parameter_list|(
name|byte
index|[]
name|name
parameter_list|)
throws|throws
name|IOException
block|{
comment|// build up a mock scanner stuff to fail the first time
name|Answer
argument_list|<
name|ResultScanner
argument_list|>
name|a
init|=
operator|new
name|Answer
argument_list|<
name|ResultScanner
argument_list|>
argument_list|()
block|{
name|boolean
name|first
init|=
literal|true
decl_stmt|;
annotation|@
name|Override
specifier|public
name|ResultScanner
name|answer
parameter_list|(
name|InvocationOnMock
name|invocation
parameter_list|)
throws|throws
name|Throwable
block|{
comment|// first invocation return the busted mock scanner
if|if
condition|(
name|first
condition|)
block|{
name|first
operator|=
literal|false
expr_stmt|;
comment|// create mock ResultScanner that always fails.
name|Scan
name|scan
init|=
name|mock
argument_list|(
name|Scan
operator|.
name|class
argument_list|)
decl_stmt|;
name|doReturn
argument_list|(
literal|"bogus"
operator|.
name|getBytes
argument_list|()
argument_list|)
operator|.
name|when
argument_list|(
name|scan
argument_list|)
operator|.
name|getStartRow
argument_list|()
expr_stmt|;
comment|// avoid npe
name|ResultScanner
name|scanner
init|=
name|mock
argument_list|(
name|ResultScanner
operator|.
name|class
argument_list|)
decl_stmt|;
comment|// simulate TimeoutException / IOException
name|doThrow
argument_list|(
operator|new
name|IOException
argument_list|(
literal|"Injected exception"
argument_list|)
argument_list|)
operator|.
name|when
argument_list|(
name|scanner
argument_list|)
operator|.
name|next
argument_list|()
expr_stmt|;
return|return
name|scanner
return|;
block|}
comment|// otherwise return the real scanner.
return|return
operator|(
name|ResultScanner
operator|)
name|invocation
operator|.
name|callRealMethod
argument_list|()
return|;
block|}
block|}
decl_stmt|;
name|HTable
name|htable
init|=
name|spy
argument_list|(
name|createTable
argument_list|(
name|name
argument_list|)
argument_list|)
decl_stmt|;
name|doAnswer
argument_list|(
name|a
argument_list|)
operator|.
name|when
argument_list|(
name|htable
argument_list|)
operator|.
name|getScanner
argument_list|(
operator|(
name|Scan
operator|)
name|anyObject
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|htable
return|;
block|}
comment|/**    * Create a table that throws a DoNoRetryIOException on first scanner next    * call    *     * @throws IOException    */
specifier|static
name|HTable
name|createDNRIOEScannerTable
parameter_list|(
name|byte
index|[]
name|name
parameter_list|)
throws|throws
name|IOException
block|{
comment|// build up a mock scanner stuff to fail the first time
name|Answer
argument_list|<
name|ResultScanner
argument_list|>
name|a
init|=
operator|new
name|Answer
argument_list|<
name|ResultScanner
argument_list|>
argument_list|()
block|{
name|boolean
name|first
init|=
literal|true
decl_stmt|;
annotation|@
name|Override
specifier|public
name|ResultScanner
name|answer
parameter_list|(
name|InvocationOnMock
name|invocation
parameter_list|)
throws|throws
name|Throwable
block|{
comment|// first invocation return the busted mock scanner
if|if
condition|(
name|first
condition|)
block|{
name|first
operator|=
literal|false
expr_stmt|;
comment|// create mock ResultScanner that always fails.
name|Scan
name|scan
init|=
name|mock
argument_list|(
name|Scan
operator|.
name|class
argument_list|)
decl_stmt|;
name|doReturn
argument_list|(
literal|"bogus"
operator|.
name|getBytes
argument_list|()
argument_list|)
operator|.
name|when
argument_list|(
name|scan
argument_list|)
operator|.
name|getStartRow
argument_list|()
expr_stmt|;
comment|// avoid npe
name|ResultScanner
name|scanner
init|=
name|mock
argument_list|(
name|ResultScanner
operator|.
name|class
argument_list|)
decl_stmt|;
name|invocation
operator|.
name|callRealMethod
argument_list|()
expr_stmt|;
comment|// simulate UnknownScannerException
name|doThrow
argument_list|(
operator|new
name|UnknownScannerException
argument_list|(
literal|"Injected simulated TimeoutException"
argument_list|)
argument_list|)
operator|.
name|when
argument_list|(
name|scanner
argument_list|)
operator|.
name|next
argument_list|()
expr_stmt|;
return|return
name|scanner
return|;
block|}
comment|// otherwise return the real scanner.
return|return
operator|(
name|ResultScanner
operator|)
name|invocation
operator|.
name|callRealMethod
argument_list|()
return|;
block|}
block|}
decl_stmt|;
name|HTable
name|htable
init|=
name|spy
argument_list|(
name|createTable
argument_list|(
name|name
argument_list|)
argument_list|)
decl_stmt|;
name|doAnswer
argument_list|(
name|a
argument_list|)
operator|.
name|when
argument_list|(
name|htable
argument_list|)
operator|.
name|getScanner
argument_list|(
operator|(
name|Scan
operator|)
name|anyObject
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|htable
return|;
block|}
comment|/**    * Run test assuming no errors using mapred api.    *     * @throws IOException    */
annotation|@
name|Test
specifier|public
name|void
name|testTableRecordReader
parameter_list|()
throws|throws
name|IOException
block|{
name|HTable
name|table
init|=
name|createTable
argument_list|(
literal|"table1"
operator|.
name|getBytes
argument_list|()
argument_list|)
decl_stmt|;
name|runTestMapred
argument_list|(
name|table
argument_list|)
expr_stmt|;
block|}
comment|/**    * Run test assuming Scanner IOException failure using mapred api,    *     * @throws IOException    */
annotation|@
name|Test
specifier|public
name|void
name|testTableRecordReaderScannerFail
parameter_list|()
throws|throws
name|IOException
block|{
name|HTable
name|htable
init|=
name|createIOEScannerTable
argument_list|(
literal|"table2"
operator|.
name|getBytes
argument_list|()
argument_list|)
decl_stmt|;
name|runTestMapred
argument_list|(
name|htable
argument_list|)
expr_stmt|;
block|}
comment|/**    * Run test assuming UnknownScannerException (which is a type of    * DoNotRetryIOException) using mapred api.    *     * @throws DoNotRetryIOException    */
annotation|@
name|Test
argument_list|(
name|expected
operator|=
name|DoNotRetryIOException
operator|.
name|class
argument_list|)
specifier|public
name|void
name|testTableRecordReaderScannerTimeout
parameter_list|()
throws|throws
name|IOException
block|{
name|HTable
name|htable
init|=
name|createDNRIOEScannerTable
argument_list|(
literal|"table3"
operator|.
name|getBytes
argument_list|()
argument_list|)
decl_stmt|;
name|runTestMapred
argument_list|(
name|htable
argument_list|)
expr_stmt|;
block|}
comment|/**    * Run test assuming no errors using newer mapreduce api    *     * @throws IOException    * @throws InterruptedException    */
annotation|@
name|Test
specifier|public
name|void
name|testTableRecordReaderMapreduce
parameter_list|()
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|HTable
name|table
init|=
name|createTable
argument_list|(
literal|"table1-mr"
operator|.
name|getBytes
argument_list|()
argument_list|)
decl_stmt|;
name|runTestMapreduce
argument_list|(
name|table
argument_list|)
expr_stmt|;
block|}
comment|/**    * Run test assuming Scanner IOException failure using newer mapreduce api    *     * @throws IOException    * @throws InterruptedException    */
annotation|@
name|Test
specifier|public
name|void
name|testTableRecordReaderScannerFailMapreduce
parameter_list|()
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|HTable
name|htable
init|=
name|createIOEScannerTable
argument_list|(
literal|"table2-mr"
operator|.
name|getBytes
argument_list|()
argument_list|)
decl_stmt|;
name|runTestMapreduce
argument_list|(
name|htable
argument_list|)
expr_stmt|;
block|}
comment|/**    * Run test assuming UnknownScannerException (which is a type of    * DoNotRetryIOException) using newer mapreduce api    *     * @throws InterruptedException    * @throws DoNotRetryIOException    */
annotation|@
name|Test
argument_list|(
name|expected
operator|=
name|DoNotRetryIOException
operator|.
name|class
argument_list|)
specifier|public
name|void
name|testTableRecordReaderScannerTimeoutMapreduce
parameter_list|()
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|HTable
name|htable
init|=
name|createDNRIOEScannerTable
argument_list|(
literal|"table3-mr"
operator|.
name|getBytes
argument_list|()
argument_list|)
decl_stmt|;
name|runTestMapreduce
argument_list|(
name|htable
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

