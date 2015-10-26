begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|RegexStringComparator
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
name|RowFilter
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
name|mapreduce
operator|.
name|MapreduceTestingShim
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
name|testclassification
operator|.
name|LargeTests
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
name|MapReduceTests
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
name|io
operator|.
name|NullWritable
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
name|mapred
operator|.
name|InputFormat
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
name|mapred
operator|.
name|JobClient
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
name|mapred
operator|.
name|JobConf
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
name|mapred
operator|.
name|JobConfigurable
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
name|mapred
operator|.
name|MiniMRCluster
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
name|mapred
operator|.
name|OutputCollector
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
name|mapred
operator|.
name|Reporter
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
name|mapred
operator|.
name|RunningJob
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
name|mapred
operator|.
name|lib
operator|.
name|NullOutputFormat
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
block|{
name|MapReduceTests
operator|.
name|class
block|,
name|LargeTests
operator|.
name|class
block|}
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
name|Table
name|createTable
parameter_list|(
name|byte
index|[]
name|tableName
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|createTable
argument_list|(
name|tableName
argument_list|,
operator|new
name|byte
index|[]
index|[]
block|{
name|FAMILY
block|}
argument_list|)
return|;
block|}
comment|/**    * Setup a table with two rows and values per column family.    *     * @param tableName    * @return    * @throws IOException    */
specifier|public
specifier|static
name|Table
name|createTable
parameter_list|(
name|byte
index|[]
name|tableName
parameter_list|,
name|byte
index|[]
index|[]
name|families
parameter_list|)
throws|throws
name|IOException
block|{
name|Table
name|table
init|=
name|UTIL
operator|.
name|createTable
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
name|tableName
argument_list|)
argument_list|,
name|families
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
for|for
control|(
name|byte
index|[]
name|family
range|:
name|families
control|)
block|{
name|p
operator|.
name|add
argument_list|(
name|family
argument_list|,
literal|null
argument_list|,
literal|"value aaa"
operator|.
name|getBytes
argument_list|()
argument_list|)
expr_stmt|;
block|}
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
for|for
control|(
name|byte
index|[]
name|family
range|:
name|families
control|)
block|{
name|p
operator|.
name|add
argument_list|(
name|family
argument_list|,
literal|null
argument_list|,
literal|"value bbb"
operator|.
name|getBytes
argument_list|()
argument_list|)
expr_stmt|;
block|}
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
name|Table
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
comment|/**    * Create a table that IOE's on first scanner next call    *     * @throws IOException    */
specifier|static
name|Table
name|createIOEScannerTable
parameter_list|(
name|byte
index|[]
name|name
parameter_list|,
specifier|final
name|int
name|failCnt
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
name|int
name|cnt
init|=
literal|0
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
name|cnt
operator|++
operator|<
name|failCnt
condition|)
block|{
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
name|Table
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
name|Table
name|createDNRIOEScannerTable
parameter_list|(
name|byte
index|[]
name|name
parameter_list|,
specifier|final
name|int
name|failCnt
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
name|int
name|cnt
init|=
literal|0
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
name|cnt
operator|++
operator|<
name|failCnt
condition|)
block|{
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
comment|// simulate NotServingRegionException
name|doThrow
argument_list|(
operator|new
name|NotServingRegionException
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
name|Table
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
name|Table
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
name|Table
name|htable
init|=
name|createIOEScannerTable
argument_list|(
literal|"table2"
operator|.
name|getBytes
argument_list|()
argument_list|,
literal|1
argument_list|)
decl_stmt|;
name|runTestMapred
argument_list|(
name|htable
argument_list|)
expr_stmt|;
block|}
comment|/**    * Run test assuming Scanner IOException failure using mapred api,    *     * @throws IOException    */
annotation|@
name|Test
argument_list|(
name|expected
operator|=
name|IOException
operator|.
name|class
argument_list|)
specifier|public
name|void
name|testTableRecordReaderScannerFailTwice
parameter_list|()
throws|throws
name|IOException
block|{
name|Table
name|htable
init|=
name|createIOEScannerTable
argument_list|(
literal|"table3"
operator|.
name|getBytes
argument_list|()
argument_list|,
literal|2
argument_list|)
decl_stmt|;
name|runTestMapred
argument_list|(
name|htable
argument_list|)
expr_stmt|;
block|}
comment|/**    * Run test assuming NotServingRegionException using mapred api.    *     * @throws org.apache.hadoop.hbase.DoNotRetryIOException    */
annotation|@
name|Test
specifier|public
name|void
name|testTableRecordReaderScannerTimeout
parameter_list|()
throws|throws
name|IOException
block|{
name|Table
name|htable
init|=
name|createDNRIOEScannerTable
argument_list|(
literal|"table4"
operator|.
name|getBytes
argument_list|()
argument_list|,
literal|1
argument_list|)
decl_stmt|;
name|runTestMapred
argument_list|(
name|htable
argument_list|)
expr_stmt|;
block|}
comment|/**    * Run test assuming NotServingRegionException using mapred api.    *     * @throws org.apache.hadoop.hbase.DoNotRetryIOException    */
annotation|@
name|Test
argument_list|(
name|expected
operator|=
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|NotServingRegionException
operator|.
name|class
argument_list|)
specifier|public
name|void
name|testTableRecordReaderScannerTimeoutTwice
parameter_list|()
throws|throws
name|IOException
block|{
name|Table
name|htable
init|=
name|createDNRIOEScannerTable
argument_list|(
literal|"table5"
operator|.
name|getBytes
argument_list|()
argument_list|,
literal|2
argument_list|)
decl_stmt|;
name|runTestMapred
argument_list|(
name|htable
argument_list|)
expr_stmt|;
block|}
comment|/**    * Verify the example we present in javadocs on TableInputFormatBase    */
annotation|@
name|Test
specifier|public
name|void
name|testExtensionOfTableInputFormatBase
parameter_list|()
throws|throws
name|IOException
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"testing use of an InputFormat taht extends InputFormatBase"
argument_list|)
expr_stmt|;
specifier|final
name|Table
name|table
init|=
name|createTable
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"exampleTable"
argument_list|)
argument_list|,
operator|new
name|byte
index|[]
index|[]
block|{
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"columnA"
argument_list|)
block|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"columnB"
argument_list|)
block|}
argument_list|)
decl_stmt|;
name|testInputFormat
argument_list|(
name|ExampleTIF
operator|.
name|class
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testDeprecatedExtensionOfTableInputFormatBase
parameter_list|()
throws|throws
name|IOException
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"testing use of an InputFormat taht extends InputFormatBase, "
operator|+
literal|"as it was given in 0.98."
argument_list|)
expr_stmt|;
specifier|final
name|Table
name|table
init|=
name|createTable
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"exampleDeprecatedTable"
argument_list|)
argument_list|,
operator|new
name|byte
index|[]
index|[]
block|{
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"columnA"
argument_list|)
block|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"columnB"
argument_list|)
block|}
argument_list|)
decl_stmt|;
name|testInputFormat
argument_list|(
name|ExampleDeprecatedTIF
operator|.
name|class
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testJobConfigurableExtensionOfTableInputFormatBase
parameter_list|()
throws|throws
name|IOException
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"testing use of an InputFormat taht extends InputFormatBase, "
operator|+
literal|"using JobConfigurable."
argument_list|)
expr_stmt|;
specifier|final
name|Table
name|table
init|=
name|createTable
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"exampleJobConfigurableTable"
argument_list|)
argument_list|,
operator|new
name|byte
index|[]
index|[]
block|{
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"columnA"
argument_list|)
block|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"columnB"
argument_list|)
block|}
argument_list|)
decl_stmt|;
name|testInputFormat
argument_list|(
name|ExampleJobConfigurableTIF
operator|.
name|class
argument_list|)
expr_stmt|;
block|}
name|void
name|testInputFormat
parameter_list|(
name|Class
argument_list|<
name|?
extends|extends
name|InputFormat
argument_list|>
name|clazz
parameter_list|)
throws|throws
name|IOException
block|{
name|Configuration
name|conf
init|=
name|UTIL
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
specifier|final
name|JobConf
name|job
init|=
operator|new
name|JobConf
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|job
operator|.
name|setInputFormat
argument_list|(
name|clazz
argument_list|)
expr_stmt|;
name|job
operator|.
name|setOutputFormat
argument_list|(
name|NullOutputFormat
operator|.
name|class
argument_list|)
expr_stmt|;
name|job
operator|.
name|setMapperClass
argument_list|(
name|ExampleVerifier
operator|.
name|class
argument_list|)
expr_stmt|;
name|job
operator|.
name|setNumReduceTasks
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"submitting job."
argument_list|)
expr_stmt|;
specifier|final
name|RunningJob
name|run
init|=
name|JobClient
operator|.
name|runJob
argument_list|(
name|job
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
literal|"job failed!"
argument_list|,
name|run
operator|.
name|isSuccessful
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"Saw the wrong number of instances of the filtered-for row."
argument_list|,
literal|2
argument_list|,
name|run
operator|.
name|getCounters
argument_list|()
operator|.
name|findCounter
argument_list|(
name|TestTableInputFormat
operator|.
name|class
operator|.
name|getName
argument_list|()
operator|+
literal|":row"
argument_list|,
literal|"aaa"
argument_list|)
operator|.
name|getCounter
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"Saw any instances of the filtered out row."
argument_list|,
literal|0
argument_list|,
name|run
operator|.
name|getCounters
argument_list|()
operator|.
name|findCounter
argument_list|(
name|TestTableInputFormat
operator|.
name|class
operator|.
name|getName
argument_list|()
operator|+
literal|":row"
argument_list|,
literal|"bbb"
argument_list|)
operator|.
name|getCounter
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"Saw the wrong number of instances of columnA."
argument_list|,
literal|1
argument_list|,
name|run
operator|.
name|getCounters
argument_list|()
operator|.
name|findCounter
argument_list|(
name|TestTableInputFormat
operator|.
name|class
operator|.
name|getName
argument_list|()
operator|+
literal|":family"
argument_list|,
literal|"columnA"
argument_list|)
operator|.
name|getCounter
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"Saw the wrong number of instances of columnB."
argument_list|,
literal|1
argument_list|,
name|run
operator|.
name|getCounters
argument_list|()
operator|.
name|findCounter
argument_list|(
name|TestTableInputFormat
operator|.
name|class
operator|.
name|getName
argument_list|()
operator|+
literal|":family"
argument_list|,
literal|"columnB"
argument_list|)
operator|.
name|getCounter
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"Saw the wrong count of values for the filtered-for row."
argument_list|,
literal|2
argument_list|,
name|run
operator|.
name|getCounters
argument_list|()
operator|.
name|findCounter
argument_list|(
name|TestTableInputFormat
operator|.
name|class
operator|.
name|getName
argument_list|()
operator|+
literal|":value"
argument_list|,
literal|"value aaa"
argument_list|)
operator|.
name|getCounter
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"Saw the wrong count of values for the filtered-out row."
argument_list|,
literal|0
argument_list|,
name|run
operator|.
name|getCounters
argument_list|()
operator|.
name|findCounter
argument_list|(
name|TestTableInputFormat
operator|.
name|class
operator|.
name|getName
argument_list|()
operator|+
literal|":value"
argument_list|,
literal|"value bbb"
argument_list|)
operator|.
name|getCounter
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
class|class
name|ExampleVerifier
implements|implements
name|TableMap
argument_list|<
name|NullWritable
argument_list|,
name|NullWritable
argument_list|>
block|{
annotation|@
name|Override
specifier|public
name|void
name|configure
parameter_list|(
name|JobConf
name|conf
parameter_list|)
block|{     }
annotation|@
name|Override
specifier|public
name|void
name|map
parameter_list|(
name|ImmutableBytesWritable
name|key
parameter_list|,
name|Result
name|value
parameter_list|,
name|OutputCollector
argument_list|<
name|NullWritable
argument_list|,
name|NullWritable
argument_list|>
name|output
parameter_list|,
name|Reporter
name|reporter
parameter_list|)
throws|throws
name|IOException
block|{
for|for
control|(
name|Cell
name|cell
range|:
name|value
operator|.
name|listCells
argument_list|()
control|)
block|{
name|reporter
operator|.
name|getCounter
argument_list|(
name|TestTableInputFormat
operator|.
name|class
operator|.
name|getName
argument_list|()
operator|+
literal|":row"
argument_list|,
name|Bytes
operator|.
name|toString
argument_list|(
name|cell
operator|.
name|getRowArray
argument_list|()
argument_list|,
name|cell
operator|.
name|getRowOffset
argument_list|()
argument_list|,
name|cell
operator|.
name|getRowLength
argument_list|()
argument_list|)
argument_list|)
operator|.
name|increment
argument_list|(
literal|1l
argument_list|)
expr_stmt|;
name|reporter
operator|.
name|getCounter
argument_list|(
name|TestTableInputFormat
operator|.
name|class
operator|.
name|getName
argument_list|()
operator|+
literal|":family"
argument_list|,
name|Bytes
operator|.
name|toString
argument_list|(
name|cell
operator|.
name|getFamilyArray
argument_list|()
argument_list|,
name|cell
operator|.
name|getFamilyOffset
argument_list|()
argument_list|,
name|cell
operator|.
name|getFamilyLength
argument_list|()
argument_list|)
argument_list|)
operator|.
name|increment
argument_list|(
literal|1l
argument_list|)
expr_stmt|;
name|reporter
operator|.
name|getCounter
argument_list|(
name|TestTableInputFormat
operator|.
name|class
operator|.
name|getName
argument_list|()
operator|+
literal|":value"
argument_list|,
name|Bytes
operator|.
name|toString
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
argument_list|)
operator|.
name|increment
argument_list|(
literal|1l
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|()
block|{     }
block|}
specifier|public
specifier|static
class|class
name|ExampleDeprecatedTIF
extends|extends
name|TableInputFormatBase
implements|implements
name|JobConfigurable
block|{
annotation|@
name|Override
specifier|public
name|void
name|configure
parameter_list|(
name|JobConf
name|job
parameter_list|)
block|{
try|try
block|{
name|Connection
name|connection
init|=
name|ConnectionFactory
operator|.
name|createConnection
argument_list|(
name|job
argument_list|)
decl_stmt|;
name|Table
name|exampleTable
init|=
name|connection
operator|.
name|getTable
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"exampleDeprecatedTable"
argument_list|)
argument_list|)
decl_stmt|;
comment|// mandatory
name|initializeTable
argument_list|(
name|connection
argument_list|,
name|exampleTable
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|byte
index|[]
index|[]
name|inputColumns
init|=
operator|new
name|byte
index|[]
index|[]
block|{
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"columnA"
argument_list|)
block|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"columnB"
argument_list|)
block|}
decl_stmt|;
comment|// mandatory
name|setInputColumns
argument_list|(
name|inputColumns
argument_list|)
expr_stmt|;
name|Filter
name|exampleFilter
init|=
operator|new
name|RowFilter
argument_list|(
name|CompareOp
operator|.
name|EQUAL
argument_list|,
operator|new
name|RegexStringComparator
argument_list|(
literal|"aa.*"
argument_list|)
argument_list|)
decl_stmt|;
comment|// optional
name|setRowFilter
argument_list|(
name|exampleFilter
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|exception
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Failed to configure for job."
argument_list|,
name|exception
argument_list|)
throw|;
block|}
block|}
block|}
specifier|public
specifier|static
class|class
name|ExampleJobConfigurableTIF
extends|extends
name|ExampleTIF
implements|implements
name|JobConfigurable
block|{
annotation|@
name|Override
specifier|public
name|void
name|configure
parameter_list|(
name|JobConf
name|job
parameter_list|)
block|{
try|try
block|{
name|initialize
argument_list|(
name|job
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|exception
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Failed to initialize."
argument_list|,
name|exception
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|protected
name|void
name|initialize
parameter_list|(
name|JobConf
name|job
parameter_list|)
throws|throws
name|IOException
block|{
name|initialize
argument_list|(
name|job
argument_list|,
literal|"exampleJobConfigurableTable"
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
specifier|static
class|class
name|ExampleTIF
extends|extends
name|TableInputFormatBase
block|{
annotation|@
name|Override
specifier|protected
name|void
name|initialize
parameter_list|(
name|JobConf
name|job
parameter_list|)
throws|throws
name|IOException
block|{
name|initialize
argument_list|(
name|job
argument_list|,
literal|"exampleTable"
argument_list|)
expr_stmt|;
block|}
specifier|protected
name|void
name|initialize
parameter_list|(
name|JobConf
name|job
parameter_list|,
name|String
name|table
parameter_list|)
throws|throws
name|IOException
block|{
name|Connection
name|connection
init|=
name|ConnectionFactory
operator|.
name|createConnection
argument_list|(
name|HBaseConfiguration
operator|.
name|create
argument_list|(
name|job
argument_list|)
argument_list|)
decl_stmt|;
name|TableName
name|tableName
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
name|table
argument_list|)
decl_stmt|;
comment|// mandatory
name|initializeTable
argument_list|(
name|connection
argument_list|,
name|tableName
argument_list|)
expr_stmt|;
name|byte
index|[]
index|[]
name|inputColumns
init|=
operator|new
name|byte
index|[]
index|[]
block|{
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"columnA"
argument_list|)
block|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"columnB"
argument_list|)
block|}
decl_stmt|;
comment|// mandatory
name|setInputColumns
argument_list|(
name|inputColumns
argument_list|)
expr_stmt|;
name|Filter
name|exampleFilter
init|=
operator|new
name|RowFilter
argument_list|(
name|CompareOp
operator|.
name|EQUAL
argument_list|,
operator|new
name|RegexStringComparator
argument_list|(
literal|"aa.*"
argument_list|)
argument_list|)
decl_stmt|;
comment|// optional
name|setRowFilter
argument_list|(
name|exampleFilter
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

