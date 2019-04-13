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
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|CompareOperator
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
name|StartMiniClusterOption
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
name|TableDescriptor
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
name|io
operator|.
name|encoding
operator|.
name|DataBlockEncoding
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
name|BeforeClass
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

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hbase
operator|.
name|thirdparty
operator|.
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
name|hbase
operator|.
name|thirdparty
operator|.
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|cli
operator|.
name|CommandLineParser
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hbase
operator|.
name|thirdparty
operator|.
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|cli
operator|.
name|GnuParser
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hbase
operator|.
name|thirdparty
operator|.
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|cli
operator|.
name|HelpFormatter
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hbase
operator|.
name|thirdparty
operator|.
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|cli
operator|.
name|Option
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hbase
operator|.
name|thirdparty
operator|.
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|cli
operator|.
name|Options
import|;
end_import

begin_comment
comment|/**  * Test performance improvement of joined scanners optimization:  * https://issues.apache.org/jira/browse/HBASE-5416  */
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
name|LargeTests
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|TestJoinedScanners
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
name|TestJoinedScanners
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
name|TestJoinedScanners
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|HBaseTestingUtility
name|TEST_UTIL
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
name|cf_essential
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"essential"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|cf_joined
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"joined"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|col_name
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"a"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|flag_yes
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"Y"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|flag_no
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"N"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|DataBlockEncoding
name|blockEncoding
init|=
name|DataBlockEncoding
operator|.
name|FAST_DIFF
decl_stmt|;
specifier|private
specifier|static
name|int
name|selectionRatio
init|=
literal|30
decl_stmt|;
specifier|private
specifier|static
name|int
name|valueWidth
init|=
literal|128
operator|*
literal|1024
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
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|setUpBeforeClass
parameter_list|()
throws|throws
name|Exception
block|{
specifier|final
name|int
name|DEFAULT_BLOCK_SIZE
init|=
literal|1024
operator|*
literal|1024
decl_stmt|;
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setLong
argument_list|(
literal|"dfs.blocksize"
argument_list|,
name|DEFAULT_BLOCK_SIZE
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setInt
argument_list|(
literal|"dfs.replication"
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setLong
argument_list|(
literal|"hbase.hregion.max.filesize"
argument_list|,
literal|322122547200L
argument_list|)
expr_stmt|;
name|String
index|[]
name|dataNodeHosts
init|=
operator|new
name|String
index|[]
block|{
literal|"host1"
block|,
literal|"host2"
block|,
literal|"host3"
block|}
decl_stmt|;
name|int
name|regionServersCount
init|=
literal|3
decl_stmt|;
name|StartMiniClusterOption
name|option
init|=
name|StartMiniClusterOption
operator|.
name|builder
argument_list|()
operator|.
name|numRegionServers
argument_list|(
name|regionServersCount
argument_list|)
operator|.
name|dataNodeHosts
argument_list|(
name|dataNodeHosts
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|TEST_UTIL
operator|.
name|startMiniCluster
argument_list|(
name|option
argument_list|)
expr_stmt|;
block|}
annotation|@
name|AfterClass
specifier|public
specifier|static
name|void
name|tearDownAfterClass
parameter_list|()
throws|throws
name|Exception
block|{
name|TEST_UTIL
operator|.
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testJoinedScanners
parameter_list|()
throws|throws
name|Exception
block|{
name|byte
index|[]
index|[]
name|families
init|=
block|{
name|cf_essential
block|,
name|cf_joined
block|}
decl_stmt|;
specifier|final
name|TableName
name|tableName
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
name|name
operator|.
name|getMethodName
argument_list|()
argument_list|)
decl_stmt|;
name|HTableDescriptor
name|desc
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
name|setDataBlockEncoding
argument_list|(
name|blockEncoding
argument_list|)
expr_stmt|;
name|desc
operator|.
name|addFamily
argument_list|(
name|hcd
argument_list|)
expr_stmt|;
block|}
name|TEST_UTIL
operator|.
name|getAdmin
argument_list|()
operator|.
name|createTable
argument_list|(
name|desc
argument_list|)
expr_stmt|;
name|Table
name|ht
init|=
name|TEST_UTIL
operator|.
name|getConnection
argument_list|()
operator|.
name|getTable
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
name|long
name|rows_to_insert
init|=
literal|1000
decl_stmt|;
name|int
name|insert_batch
init|=
literal|20
decl_stmt|;
name|long
name|time
init|=
name|System
operator|.
name|nanoTime
argument_list|()
decl_stmt|;
name|Random
name|rand
init|=
operator|new
name|Random
argument_list|(
name|time
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Make "
operator|+
name|Long
operator|.
name|toString
argument_list|(
name|rows_to_insert
argument_list|)
operator|+
literal|" rows, total size = "
operator|+
name|Float
operator|.
name|toString
argument_list|(
name|rows_to_insert
operator|*
name|valueWidth
operator|/
literal|1024
operator|/
literal|1024
argument_list|)
operator|+
literal|" MB"
argument_list|)
expr_stmt|;
name|byte
index|[]
name|val_large
init|=
operator|new
name|byte
index|[
name|valueWidth
index|]
decl_stmt|;
name|List
argument_list|<
name|Put
argument_list|>
name|puts
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|long
name|i
init|=
literal|0
init|;
name|i
operator|<
name|rows_to_insert
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
name|Bytes
operator|.
name|toBytes
argument_list|(
name|Long
operator|.
name|toString
argument_list|(
name|i
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|rand
operator|.
name|nextInt
argument_list|(
literal|100
argument_list|)
operator|<=
name|selectionRatio
condition|)
block|{
name|put
operator|.
name|addColumn
argument_list|(
name|cf_essential
argument_list|,
name|col_name
argument_list|,
name|flag_yes
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|put
operator|.
name|addColumn
argument_list|(
name|cf_essential
argument_list|,
name|col_name
argument_list|,
name|flag_no
argument_list|)
expr_stmt|;
block|}
name|put
operator|.
name|addColumn
argument_list|(
name|cf_joined
argument_list|,
name|col_name
argument_list|,
name|val_large
argument_list|)
expr_stmt|;
name|puts
operator|.
name|add
argument_list|(
name|put
argument_list|)
expr_stmt|;
if|if
condition|(
name|puts
operator|.
name|size
argument_list|()
operator|>=
name|insert_batch
condition|)
block|{
name|ht
operator|.
name|put
argument_list|(
name|puts
argument_list|)
expr_stmt|;
name|puts
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
block|}
if|if
condition|(
operator|!
name|puts
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|ht
operator|.
name|put
argument_list|(
name|puts
argument_list|)
expr_stmt|;
name|puts
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"Data generated in "
operator|+
name|Double
operator|.
name|toString
argument_list|(
operator|(
name|System
operator|.
name|nanoTime
argument_list|()
operator|-
name|time
operator|)
operator|/
literal|1000000000.0
argument_list|)
operator|+
literal|" seconds"
argument_list|)
expr_stmt|;
name|boolean
name|slow
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
literal|10
condition|;
operator|++
name|i
control|)
block|{
name|runScanner
argument_list|(
name|ht
argument_list|,
name|slow
argument_list|)
expr_stmt|;
name|slow
operator|=
operator|!
name|slow
expr_stmt|;
block|}
name|ht
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
specifier|private
name|void
name|runScanner
parameter_list|(
name|Table
name|table
parameter_list|,
name|boolean
name|slow
parameter_list|)
throws|throws
name|Exception
block|{
name|long
name|time
init|=
name|System
operator|.
name|nanoTime
argument_list|()
decl_stmt|;
name|Scan
name|scan
init|=
operator|new
name|Scan
argument_list|()
decl_stmt|;
name|scan
operator|.
name|addColumn
argument_list|(
name|cf_essential
argument_list|,
name|col_name
argument_list|)
expr_stmt|;
name|scan
operator|.
name|addColumn
argument_list|(
name|cf_joined
argument_list|,
name|col_name
argument_list|)
expr_stmt|;
name|SingleColumnValueFilter
name|filter
init|=
operator|new
name|SingleColumnValueFilter
argument_list|(
name|cf_essential
argument_list|,
name|col_name
argument_list|,
name|CompareOperator
operator|.
name|EQUAL
argument_list|,
name|flag_yes
argument_list|)
decl_stmt|;
name|filter
operator|.
name|setFilterIfMissing
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|scan
operator|.
name|setFilter
argument_list|(
name|filter
argument_list|)
expr_stmt|;
name|scan
operator|.
name|setLoadColumnFamiliesOnDemand
argument_list|(
operator|!
name|slow
argument_list|)
expr_stmt|;
name|ResultScanner
name|result_scanner
init|=
name|table
operator|.
name|getScanner
argument_list|(
name|scan
argument_list|)
decl_stmt|;
name|Result
name|res
decl_stmt|;
name|long
name|rows_count
init|=
literal|0
decl_stmt|;
while|while
condition|(
operator|(
name|res
operator|=
name|result_scanner
operator|.
name|next
argument_list|()
operator|)
operator|!=
literal|null
condition|)
block|{
name|rows_count
operator|++
expr_stmt|;
block|}
name|double
name|timeSec
init|=
operator|(
name|System
operator|.
name|nanoTime
argument_list|()
operator|-
name|time
operator|)
operator|/
literal|1000000000.0
decl_stmt|;
name|result_scanner
operator|.
name|close
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
operator|(
name|slow
condition|?
literal|"Slow"
else|:
literal|"Joined"
operator|)
operator|+
literal|" scanner finished in "
operator|+
name|Double
operator|.
name|toString
argument_list|(
name|timeSec
argument_list|)
operator|+
literal|" seconds, got "
operator|+
name|Long
operator|.
name|toString
argument_list|(
name|rows_count
operator|/
literal|2
argument_list|)
operator|+
literal|" rows"
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|static
name|Options
name|options
init|=
operator|new
name|Options
argument_list|()
decl_stmt|;
comment|/**    * Command line interface:    * @param args    * @throws IOException if there is a bug while reading from disk    */
specifier|public
specifier|static
name|void
name|main
parameter_list|(
specifier|final
name|String
index|[]
name|args
parameter_list|)
throws|throws
name|Exception
block|{
name|Option
name|encodingOption
init|=
operator|new
name|Option
argument_list|(
literal|"e"
argument_list|,
literal|"blockEncoding"
argument_list|,
literal|true
argument_list|,
literal|"Data block encoding; Default: FAST_DIFF"
argument_list|)
decl_stmt|;
name|encodingOption
operator|.
name|setRequired
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|options
operator|.
name|addOption
argument_list|(
name|encodingOption
argument_list|)
expr_stmt|;
name|Option
name|ratioOption
init|=
operator|new
name|Option
argument_list|(
literal|"r"
argument_list|,
literal|"selectionRatio"
argument_list|,
literal|true
argument_list|,
literal|"Ratio of selected rows using essential column family"
argument_list|)
decl_stmt|;
name|ratioOption
operator|.
name|setRequired
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|options
operator|.
name|addOption
argument_list|(
name|ratioOption
argument_list|)
expr_stmt|;
name|Option
name|widthOption
init|=
operator|new
name|Option
argument_list|(
literal|"w"
argument_list|,
literal|"valueWidth"
argument_list|,
literal|true
argument_list|,
literal|"Width of value for non-essential column family"
argument_list|)
decl_stmt|;
name|widthOption
operator|.
name|setRequired
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|options
operator|.
name|addOption
argument_list|(
name|widthOption
argument_list|)
expr_stmt|;
name|CommandLineParser
name|parser
init|=
operator|new
name|GnuParser
argument_list|()
decl_stmt|;
name|CommandLine
name|cmd
init|=
name|parser
operator|.
name|parse
argument_list|(
name|options
argument_list|,
name|args
argument_list|)
decl_stmt|;
if|if
condition|(
name|args
operator|.
name|length
operator|<
literal|1
condition|)
block|{
name|HelpFormatter
name|formatter
init|=
operator|new
name|HelpFormatter
argument_list|()
decl_stmt|;
name|formatter
operator|.
name|printHelp
argument_list|(
literal|"TestJoinedScanners"
argument_list|,
name|options
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|cmd
operator|.
name|hasOption
argument_list|(
literal|"e"
argument_list|)
condition|)
block|{
name|blockEncoding
operator|=
name|DataBlockEncoding
operator|.
name|valueOf
argument_list|(
name|cmd
operator|.
name|getOptionValue
argument_list|(
literal|"e"
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|cmd
operator|.
name|hasOption
argument_list|(
literal|"r"
argument_list|)
condition|)
block|{
name|selectionRatio
operator|=
name|Integer
operator|.
name|parseInt
argument_list|(
name|cmd
operator|.
name|getOptionValue
argument_list|(
literal|"r"
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|cmd
operator|.
name|hasOption
argument_list|(
literal|"w"
argument_list|)
condition|)
block|{
name|valueWidth
operator|=
name|Integer
operator|.
name|parseInt
argument_list|(
name|cmd
operator|.
name|getOptionValue
argument_list|(
literal|"w"
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// run the test
name|TestJoinedScanners
name|test
init|=
operator|new
name|TestJoinedScanners
argument_list|()
decl_stmt|;
name|test
operator|.
name|testJoinedScanners
argument_list|()
expr_stmt|;
block|}
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
name|testWithReverseScan
parameter_list|()
throws|throws
name|Exception
block|{
try|try
init|(
name|Connection
name|con
init|=
name|TEST_UTIL
operator|.
name|getConnection
argument_list|()
init|;
name|Admin
name|admin
operator|=
name|con
operator|.
name|getAdmin
argument_list|()
init|)
block|{
name|TableName
name|tableName
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
name|name
operator|.
name|getMethodName
argument_list|()
argument_list|)
decl_stmt|;
name|TableDescriptor
name|tableDescriptor
init|=
name|TableDescriptorBuilder
operator|.
name|newBuilder
argument_list|(
name|tableName
argument_list|)
operator|.
name|setColumnFamily
argument_list|(
name|ColumnFamilyDescriptorBuilder
operator|.
name|of
argument_list|(
literal|"cf1"
argument_list|)
argument_list|)
operator|.
name|setColumnFamily
argument_list|(
name|ColumnFamilyDescriptorBuilder
operator|.
name|of
argument_list|(
literal|"cf2"
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|admin
operator|.
name|createTable
argument_list|(
name|tableDescriptor
argument_list|)
expr_stmt|;
try|try
init|(
name|Table
name|table
init|=
name|con
operator|.
name|getTable
argument_list|(
name|tableName
argument_list|)
init|)
block|{
name|SingleColumnValueFilter
name|filter
init|=
operator|new
name|SingleColumnValueFilter
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"cf1"
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"col"
argument_list|)
argument_list|,
name|CompareOperator
operator|.
name|EQUAL
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"val"
argument_list|)
argument_list|)
decl_stmt|;
name|filter
operator|.
name|setFilterIfMissing
argument_list|(
literal|true
argument_list|)
expr_stmt|;
comment|// Reverse scan with loading CFs on demand
name|Scan
name|scan
init|=
operator|new
name|Scan
argument_list|()
decl_stmt|;
name|scan
operator|.
name|setFilter
argument_list|(
name|filter
argument_list|)
expr_stmt|;
name|scan
operator|.
name|setReversed
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|scan
operator|.
name|setLoadColumnFamiliesOnDemand
argument_list|(
literal|true
argument_list|)
expr_stmt|;
try|try
init|(
name|ResultScanner
name|scanner
init|=
name|table
operator|.
name|getScanner
argument_list|(
name|scan
argument_list|)
init|)
block|{
comment|// DoNotRetryIOException should occur
name|scanner
operator|.
name|next
argument_list|()
expr_stmt|;
block|}
block|}
block|}
block|}
block|}
end_class

end_unit

