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
name|mapreduce
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
name|assertTrue
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
name|mapreduce
operator|.
name|RowCounter
operator|.
name|RowCounterMapper
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
name|mapreduce
operator|.
name|Counter
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
name|mapreduce
operator|.
name|Job
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
name|GenericOptionsParser
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
comment|/**  * Test the rowcounter map reduce job.  */
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
name|TestRowCounter
block|{
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|getClass
argument_list|()
argument_list|)
decl_stmt|;
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
name|String
name|TABLE_NAME
init|=
literal|"testRowCounter"
decl_stmt|;
specifier|private
specifier|final
specifier|static
name|String
name|COL_FAM
init|=
literal|"col_fam"
decl_stmt|;
specifier|private
specifier|final
specifier|static
name|String
name|COL1
init|=
literal|"c1"
decl_stmt|;
specifier|private
specifier|final
specifier|static
name|String
name|COL2
init|=
literal|"c2"
decl_stmt|;
specifier|private
specifier|final
specifier|static
name|int
name|TOTAL_ROWS
init|=
literal|10
decl_stmt|;
specifier|private
specifier|final
specifier|static
name|int
name|ROWS_WITH_ONE_COL
init|=
literal|2
decl_stmt|;
comment|/**    * @throws java.lang.Exception    */
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
name|TEST_UTIL
operator|.
name|startMiniCluster
argument_list|()
expr_stmt|;
name|TEST_UTIL
operator|.
name|startMiniMapReduceCluster
argument_list|()
expr_stmt|;
name|HTable
name|table
init|=
name|TEST_UTIL
operator|.
name|createTable
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|TABLE_NAME
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|COL_FAM
argument_list|)
argument_list|)
decl_stmt|;
name|writeRows
argument_list|(
name|table
argument_list|)
expr_stmt|;
name|table
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
comment|/**    * @throws java.lang.Exception    */
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
name|TEST_UTIL
operator|.
name|shutdownMiniMapReduceCluster
argument_list|()
expr_stmt|;
block|}
comment|/**    * Test a case when no column was specified in command line arguments.    *     * @throws Exception    */
annotation|@
name|Test
specifier|public
name|void
name|testRowCounterNoColumn
parameter_list|()
throws|throws
name|Exception
block|{
name|String
index|[]
name|args
init|=
operator|new
name|String
index|[]
block|{
name|TABLE_NAME
block|}
decl_stmt|;
name|runRowCount
argument_list|(
name|args
argument_list|,
literal|10
argument_list|)
expr_stmt|;
block|}
comment|/**    * Test a case when the column specified in command line arguments is    * exclusive for few rows.    *     * @throws Exception    */
annotation|@
name|Test
specifier|public
name|void
name|testRowCounterExclusiveColumn
parameter_list|()
throws|throws
name|Exception
block|{
name|String
index|[]
name|args
init|=
operator|new
name|String
index|[]
block|{
name|TABLE_NAME
block|,
name|COL_FAM
operator|+
literal|":"
operator|+
name|COL1
block|}
decl_stmt|;
name|runRowCount
argument_list|(
name|args
argument_list|,
literal|8
argument_list|)
expr_stmt|;
block|}
comment|/**    * Test a case when the column specified in command line arguments is not part    * of first KV for a row.    *     * @throws Exception    */
annotation|@
name|Test
specifier|public
name|void
name|testRowCounterHiddenColumn
parameter_list|()
throws|throws
name|Exception
block|{
name|String
index|[]
name|args
init|=
operator|new
name|String
index|[]
block|{
name|TABLE_NAME
block|,
name|COL_FAM
operator|+
literal|":"
operator|+
name|COL2
block|}
decl_stmt|;
name|runRowCount
argument_list|(
name|args
argument_list|,
literal|10
argument_list|)
expr_stmt|;
block|}
comment|/**    * Run the RowCounter map reduce job and verify the row count.    *     * @param args the command line arguments to be used for rowcounter job.    * @param expectedCount the expected row count (result of map reduce job).    * @throws Exception    */
specifier|private
name|void
name|runRowCount
parameter_list|(
name|String
index|[]
name|args
parameter_list|,
name|int
name|expectedCount
parameter_list|)
throws|throws
name|Exception
block|{
name|GenericOptionsParser
name|opts
init|=
operator|new
name|GenericOptionsParser
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|args
argument_list|)
decl_stmt|;
name|Configuration
name|conf
init|=
name|opts
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
name|args
operator|=
name|opts
operator|.
name|getRemainingArgs
argument_list|()
expr_stmt|;
name|Job
name|job
init|=
name|RowCounter
operator|.
name|createSubmittableJob
argument_list|(
name|conf
argument_list|,
name|args
argument_list|)
decl_stmt|;
name|job
operator|.
name|waitForCompletion
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|job
operator|.
name|isSuccessful
argument_list|()
argument_list|)
expr_stmt|;
name|Counter
name|counter
init|=
name|job
operator|.
name|getCounters
argument_list|()
operator|.
name|findCounter
argument_list|(
name|RowCounterMapper
operator|.
name|Counters
operator|.
name|ROWS
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|expectedCount
argument_list|,
name|counter
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**    * Writes TOTAL_ROWS number of distinct rows in to the table. Few rows have    * two columns, Few have one.    *     * @param table    * @throws IOException    */
specifier|private
specifier|static
name|void
name|writeRows
parameter_list|(
name|HTable
name|table
parameter_list|)
throws|throws
name|IOException
block|{
specifier|final
name|byte
index|[]
name|family
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|COL_FAM
argument_list|)
decl_stmt|;
specifier|final
name|byte
index|[]
name|value
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"abcd"
argument_list|)
decl_stmt|;
specifier|final
name|byte
index|[]
name|col1
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|COL1
argument_list|)
decl_stmt|;
specifier|final
name|byte
index|[]
name|col2
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|COL2
argument_list|)
decl_stmt|;
name|ArrayList
argument_list|<
name|Put
argument_list|>
name|rowsUpdate
init|=
operator|new
name|ArrayList
argument_list|<
name|Put
argument_list|>
argument_list|()
decl_stmt|;
comment|// write few rows with two columns
name|int
name|i
init|=
literal|0
decl_stmt|;
for|for
control|(
init|;
name|i
operator|<
name|TOTAL_ROWS
operator|-
name|ROWS_WITH_ONE_COL
condition|;
name|i
operator|++
control|)
block|{
name|byte
index|[]
name|row
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"row"
operator|+
name|i
argument_list|)
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
name|family
argument_list|,
name|col1
argument_list|,
name|value
argument_list|)
expr_stmt|;
name|put
operator|.
name|add
argument_list|(
name|family
argument_list|,
name|col2
argument_list|,
name|value
argument_list|)
expr_stmt|;
name|rowsUpdate
operator|.
name|add
argument_list|(
name|put
argument_list|)
expr_stmt|;
block|}
comment|// write few rows with only one column
for|for
control|(
init|;
name|i
operator|<
name|TOTAL_ROWS
condition|;
name|i
operator|++
control|)
block|{
name|byte
index|[]
name|row
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"row"
operator|+
name|i
argument_list|)
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
name|family
argument_list|,
name|col2
argument_list|,
name|value
argument_list|)
expr_stmt|;
name|rowsUpdate
operator|.
name|add
argument_list|(
name|put
argument_list|)
expr_stmt|;
block|}
name|table
operator|.
name|put
argument_list|(
name|rowsUpdate
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

