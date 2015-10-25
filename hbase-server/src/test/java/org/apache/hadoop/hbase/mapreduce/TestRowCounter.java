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
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|fail
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|ByteArrayOutputStream
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
name|io
operator|.
name|PrintStream
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
name|hbase
operator|.
name|CategoryBasedTimeout
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
name|LauncherSecurityManager
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
name|TestRule
import|;
end_import

begin_comment
comment|/**  * Test the rowcounter map reduce job.  */
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
name|MediumTests
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|TestRowCounter
block|{
annotation|@
name|Rule
specifier|public
specifier|final
name|TestRule
name|timeout
init|=
name|CategoryBasedTimeout
operator|.
name|builder
argument_list|()
operator|.
name|withTimeout
argument_list|(
name|this
operator|.
name|getClass
argument_list|()
argument_list|)
operator|.
name|withLookingForStuckThread
argument_list|(
literal|true
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
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
name|TestRowCounter
operator|.
name|class
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
name|String
name|COMPOSITE_COLUMN
init|=
literal|"C:A:A"
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
name|Table
name|table
init|=
name|TEST_UTIL
operator|.
name|createTable
argument_list|(
name|TableName
operator|.
name|valueOf
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
block|}
comment|/**    * Test a case when no column was specified in command line arguments.    *    * @throws Exception    */
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
comment|/**    * Test a case when the column specified in command line arguments is    * exclusive for few rows.    *    * @throws Exception    */
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
comment|/**    * Test a case when the column specified in command line arguments is    * one for which the qualifier contains colons.    *    * @throws Exception    */
annotation|@
name|Test
specifier|public
name|void
name|testRowCounterColumnWithColonInQualifier
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
name|COMPOSITE_COLUMN
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
comment|/**    * Test a case when the column specified in command line arguments is not part    * of first KV for a row.    *    * @throws Exception    */
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
comment|/**    * Test a case when the timerange is specified with --starttime and --endtime options    *    * @throws Exception    */
annotation|@
name|Test
specifier|public
name|void
name|testRowCounterTimeRange
parameter_list|()
throws|throws
name|Exception
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
name|col1
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|COL1
argument_list|)
decl_stmt|;
name|Put
name|put1
init|=
operator|new
name|Put
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"row_timerange_"
operator|+
literal|1
argument_list|)
argument_list|)
decl_stmt|;
name|Put
name|put2
init|=
operator|new
name|Put
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"row_timerange_"
operator|+
literal|2
argument_list|)
argument_list|)
decl_stmt|;
name|Put
name|put3
init|=
operator|new
name|Put
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"row_timerange_"
operator|+
literal|3
argument_list|)
argument_list|)
decl_stmt|;
name|long
name|ts
decl_stmt|;
comment|// clean up content of TABLE_NAME
name|Table
name|table
init|=
name|TEST_UTIL
operator|.
name|deleteTableData
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
name|TABLE_NAME
argument_list|)
argument_list|)
decl_stmt|;
name|ts
operator|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
expr_stmt|;
name|put1
operator|.
name|addColumn
argument_list|(
name|family
argument_list|,
name|col1
argument_list|,
name|ts
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"val1"
argument_list|)
argument_list|)
expr_stmt|;
name|table
operator|.
name|put
argument_list|(
name|put1
argument_list|)
expr_stmt|;
name|Thread
operator|.
name|sleep
argument_list|(
literal|100
argument_list|)
expr_stmt|;
name|ts
operator|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
expr_stmt|;
name|put2
operator|.
name|addColumn
argument_list|(
name|family
argument_list|,
name|col1
argument_list|,
name|ts
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"val2"
argument_list|)
argument_list|)
expr_stmt|;
name|put3
operator|.
name|addColumn
argument_list|(
name|family
argument_list|,
name|col1
argument_list|,
name|ts
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"val3"
argument_list|)
argument_list|)
expr_stmt|;
name|table
operator|.
name|put
argument_list|(
name|put2
argument_list|)
expr_stmt|;
name|table
operator|.
name|put
argument_list|(
name|put3
argument_list|)
expr_stmt|;
name|table
operator|.
name|close
argument_list|()
expr_stmt|;
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
block|,
literal|"--starttime="
operator|+
literal|0
block|,
literal|"--endtime="
operator|+
name|ts
block|}
decl_stmt|;
name|runRowCount
argument_list|(
name|args
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|args
operator|=
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
block|,
literal|"--starttime="
operator|+
literal|0
block|,
literal|"--endtime="
operator|+
operator|(
name|ts
operator|-
literal|10
operator|)
block|}
expr_stmt|;
name|runRowCount
argument_list|(
name|args
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|args
operator|=
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
block|,
literal|"--starttime="
operator|+
name|ts
block|,
literal|"--endtime="
operator|+
operator|(
name|ts
operator|+
literal|1000
operator|)
block|}
expr_stmt|;
name|runRowCount
argument_list|(
name|args
argument_list|,
literal|2
argument_list|)
expr_stmt|;
name|args
operator|=
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
block|,
literal|"--starttime="
operator|+
operator|(
name|ts
operator|-
literal|30
operator|*
literal|1000
operator|)
block|,
literal|"--endtime="
operator|+
operator|(
name|ts
operator|+
literal|30
operator|*
literal|1000
operator|)
block|,     }
expr_stmt|;
name|runRowCount
argument_list|(
name|args
argument_list|,
literal|3
argument_list|)
expr_stmt|;
block|}
comment|/**    * Run the RowCounter map reduce job and verify the row count.    *    * @param args the command line arguments to be used for rowcounter job.    * @param expectedCount the expected row count (result of map reduce job).    * @throws Exception    */
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
specifier|final
name|RowCounter
name|counter
init|=
operator|new
name|RowCounter
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|"job failed either due to failure or miscount (see log output)."
argument_list|,
literal|0
argument_list|,
name|ToolRunner
operator|.
name|run
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|counter
argument_list|,
name|args
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * Writes TOTAL_ROWS number of distinct rows in to the table. Few rows have    * two columns, Few have one.    *    * @param table    * @throws IOException    */
specifier|private
specifier|static
name|void
name|writeRows
parameter_list|(
name|Table
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
specifier|final
name|byte
index|[]
name|col3
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|COMPOSITE_COLUMN
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
name|addColumn
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
name|addColumn
argument_list|(
name|family
argument_list|,
name|col2
argument_list|,
name|value
argument_list|)
expr_stmt|;
name|put
operator|.
name|addColumn
argument_list|(
name|family
argument_list|,
name|col3
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
name|addColumn
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
comment|/**    * test main method. Import should print help and call System.exit    */
annotation|@
name|Test
specifier|public
name|void
name|testImportMain
parameter_list|()
throws|throws
name|Exception
block|{
name|PrintStream
name|oldPrintStream
init|=
name|System
operator|.
name|err
decl_stmt|;
name|SecurityManager
name|SECURITY_MANAGER
init|=
name|System
operator|.
name|getSecurityManager
argument_list|()
decl_stmt|;
name|LauncherSecurityManager
name|newSecurityManager
init|=
operator|new
name|LauncherSecurityManager
argument_list|()
decl_stmt|;
name|System
operator|.
name|setSecurityManager
argument_list|(
name|newSecurityManager
argument_list|)
expr_stmt|;
name|ByteArrayOutputStream
name|data
init|=
operator|new
name|ByteArrayOutputStream
argument_list|()
decl_stmt|;
name|String
index|[]
name|args
init|=
block|{}
decl_stmt|;
name|System
operator|.
name|setErr
argument_list|(
operator|new
name|PrintStream
argument_list|(
name|data
argument_list|)
argument_list|)
expr_stmt|;
try|try
block|{
name|System
operator|.
name|setErr
argument_list|(
operator|new
name|PrintStream
argument_list|(
name|data
argument_list|)
argument_list|)
expr_stmt|;
try|try
block|{
name|RowCounter
operator|.
name|main
argument_list|(
name|args
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"should be SecurityException"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|SecurityException
name|e
parameter_list|)
block|{
name|assertEquals
argument_list|(
operator|-
literal|1
argument_list|,
name|newSecurityManager
operator|.
name|getExitCode
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|data
operator|.
name|toString
argument_list|()
operator|.
name|contains
argument_list|(
literal|"Wrong number of parameters:"
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|data
operator|.
name|toString
argument_list|()
operator|.
name|contains
argument_list|(
literal|"Usage: RowCounter [options]<tablename> "
operator|+
literal|"[--starttime=[start] --endtime=[end] "
operator|+
literal|"[--range=[startKey],[endKey]] "
operator|+
literal|"[<column1><column2>...]"
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|data
operator|.
name|toString
argument_list|()
operator|.
name|contains
argument_list|(
literal|"-Dhbase.client.scanner.caching=100"
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|data
operator|.
name|toString
argument_list|()
operator|.
name|contains
argument_list|(
literal|"-Dmapreduce.map.speculative=false"
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|data
operator|.
name|reset
argument_list|()
expr_stmt|;
try|try
block|{
name|args
operator|=
operator|new
name|String
index|[
literal|2
index|]
expr_stmt|;
name|args
index|[
literal|0
index|]
operator|=
literal|"table"
expr_stmt|;
name|args
index|[
literal|1
index|]
operator|=
literal|"--range=1"
expr_stmt|;
name|RowCounter
operator|.
name|main
argument_list|(
name|args
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"should be SecurityException"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|SecurityException
name|e
parameter_list|)
block|{
name|assertEquals
argument_list|(
operator|-
literal|1
argument_list|,
name|newSecurityManager
operator|.
name|getExitCode
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|data
operator|.
name|toString
argument_list|()
operator|.
name|contains
argument_list|(
literal|"Please specify range in such format as \"--range=a,b\" or, with only one boundary,"
operator|+
literal|" \"--range=,b\" or \"--range=a,\""
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|data
operator|.
name|toString
argument_list|()
operator|.
name|contains
argument_list|(
literal|"Usage: RowCounter [options]<tablename> "
operator|+
literal|"[--starttime=[start] --endtime=[end] "
operator|+
literal|"[--range=[startKey],[endKey]] "
operator|+
literal|"[<column1><column2>...]"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
finally|finally
block|{
name|System
operator|.
name|setErr
argument_list|(
name|oldPrintStream
argument_list|)
expr_stmt|;
name|System
operator|.
name|setSecurityManager
argument_list|(
name|SECURITY_MANAGER
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

