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
name|assertArrayEquals
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
name|HashSet
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
name|Set
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|UUID
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
name|Configurable
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
name|FSDataOutputStream
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
name|FileStatus
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
name|Text
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
name|Utils
operator|.
name|OutputFileUtils
operator|.
name|OutputFilesFilter
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
name|apache
operator|.
name|hadoop
operator|.
name|util
operator|.
name|Tool
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
name|TestImportTsv
implements|implements
name|Configurable
block|{
specifier|protected
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|TestImportTsv
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|protected
specifier|static
specifier|final
name|String
name|NAME
init|=
name|TestImportTsv
operator|.
name|class
operator|.
name|getSimpleName
argument_list|()
decl_stmt|;
specifier|protected
specifier|static
name|HBaseTestingUtility
name|util
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
comment|/**    * Delete the tmp directory after running doMROnTableTest. Boolean. Default is    * false.    */
specifier|protected
specifier|static
specifier|final
name|String
name|DELETE_AFTER_LOAD_CONF
init|=
name|NAME
operator|+
literal|".deleteAfterLoad"
decl_stmt|;
comment|/**    * Force use of combiner in doMROnTableTest. Boolean. Default is true.    */
specifier|protected
specifier|static
specifier|final
name|String
name|FORCE_COMBINER_CONF
init|=
name|NAME
operator|+
literal|".forceCombiner"
decl_stmt|;
specifier|private
specifier|final
name|String
name|FAMILY
init|=
literal|"FAM"
decl_stmt|;
specifier|public
name|Configuration
name|getConf
parameter_list|()
block|{
return|return
name|util
operator|.
name|getConfiguration
argument_list|()
return|;
block|}
specifier|public
name|void
name|setConf
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"setConf not supported"
argument_list|)
throw|;
block|}
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|provisionCluster
parameter_list|()
throws|throws
name|Exception
block|{
name|util
operator|.
name|startMiniCluster
argument_list|()
expr_stmt|;
name|util
operator|.
name|startMiniMapReduceCluster
argument_list|()
expr_stmt|;
block|}
annotation|@
name|AfterClass
specifier|public
specifier|static
name|void
name|releaseCluster
parameter_list|()
throws|throws
name|Exception
block|{
name|util
operator|.
name|shutdownMiniMapReduceCluster
argument_list|()
expr_stmt|;
name|util
operator|.
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testMROnTable
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|table
init|=
literal|"test-"
operator|+
name|UUID
operator|.
name|randomUUID
argument_list|()
decl_stmt|;
comment|// Prepare the arguments required for the test.
name|String
index|[]
name|args
init|=
operator|new
name|String
index|[]
block|{
literal|"-D"
operator|+
name|ImportTsv
operator|.
name|COLUMNS_CONF_KEY
operator|+
literal|"=HBASE_ROW_KEY,FAM:A,FAM:B"
block|,
literal|"-D"
operator|+
name|ImportTsv
operator|.
name|SEPARATOR_CONF_KEY
operator|+
literal|"=\u001b"
block|,
name|table
block|}
decl_stmt|;
name|util
operator|.
name|createTable
argument_list|(
name|table
argument_list|,
name|FAMILY
argument_list|)
expr_stmt|;
name|doMROnTableTest
argument_list|(
name|util
argument_list|,
name|FAMILY
argument_list|,
literal|null
argument_list|,
name|args
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|util
operator|.
name|deleteTable
argument_list|(
name|table
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testMROnTableWithTimestamp
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|table
init|=
literal|"test-"
operator|+
name|UUID
operator|.
name|randomUUID
argument_list|()
decl_stmt|;
comment|// Prepare the arguments required for the test.
name|String
index|[]
name|args
init|=
operator|new
name|String
index|[]
block|{
literal|"-D"
operator|+
name|ImportTsv
operator|.
name|COLUMNS_CONF_KEY
operator|+
literal|"=HBASE_ROW_KEY,HBASE_TS_KEY,FAM:A,FAM:B"
block|,
literal|"-D"
operator|+
name|ImportTsv
operator|.
name|SEPARATOR_CONF_KEY
operator|+
literal|"=,"
block|,
name|table
block|}
decl_stmt|;
name|String
name|data
init|=
literal|"KEY,1234,VALUE1,VALUE2\n"
decl_stmt|;
name|util
operator|.
name|createTable
argument_list|(
name|table
argument_list|,
name|FAMILY
argument_list|)
expr_stmt|;
name|doMROnTableTest
argument_list|(
name|util
argument_list|,
name|FAMILY
argument_list|,
name|data
argument_list|,
name|args
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|util
operator|.
name|deleteTable
argument_list|(
name|table
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testMROnTableWithCustomMapper
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|table
init|=
literal|"test-"
operator|+
name|UUID
operator|.
name|randomUUID
argument_list|()
decl_stmt|;
comment|// Prepare the arguments required for the test.
name|String
index|[]
name|args
init|=
operator|new
name|String
index|[]
block|{
literal|"-D"
operator|+
name|ImportTsv
operator|.
name|MAPPER_CONF_KEY
operator|+
literal|"=org.apache.hadoop.hbase.mapreduce.TsvImporterCustomTestMapper"
block|,
name|table
block|}
decl_stmt|;
name|util
operator|.
name|createTable
argument_list|(
name|table
argument_list|,
name|FAMILY
argument_list|)
expr_stmt|;
name|doMROnTableTest
argument_list|(
name|util
argument_list|,
name|FAMILY
argument_list|,
literal|null
argument_list|,
name|args
argument_list|,
literal|3
argument_list|)
expr_stmt|;
name|util
operator|.
name|deleteTable
argument_list|(
name|table
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testBulkOutputWithoutAnExistingTable
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|table
init|=
literal|"test-"
operator|+
name|UUID
operator|.
name|randomUUID
argument_list|()
decl_stmt|;
comment|// Prepare the arguments required for the test.
name|Path
name|hfiles
init|=
operator|new
name|Path
argument_list|(
name|util
operator|.
name|getDataTestDirOnTestFS
argument_list|(
name|table
argument_list|)
argument_list|,
literal|"hfiles"
argument_list|)
decl_stmt|;
name|String
index|[]
name|args
init|=
operator|new
name|String
index|[]
block|{
literal|"-D"
operator|+
name|ImportTsv
operator|.
name|COLUMNS_CONF_KEY
operator|+
literal|"=HBASE_ROW_KEY,FAM:A,FAM:B"
block|,
literal|"-D"
operator|+
name|ImportTsv
operator|.
name|SEPARATOR_CONF_KEY
operator|+
literal|"=\u001b"
block|,
literal|"-D"
operator|+
name|ImportTsv
operator|.
name|BULK_OUTPUT_CONF_KEY
operator|+
literal|"="
operator|+
name|hfiles
operator|.
name|toString
argument_list|()
block|,
name|table
block|}
decl_stmt|;
name|doMROnTableTest
argument_list|(
name|util
argument_list|,
name|FAMILY
argument_list|,
literal|null
argument_list|,
name|args
argument_list|,
literal|3
argument_list|)
expr_stmt|;
name|util
operator|.
name|deleteTable
argument_list|(
name|table
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testBulkOutputWithAnExistingTable
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|table
init|=
literal|"test-"
operator|+
name|UUID
operator|.
name|randomUUID
argument_list|()
decl_stmt|;
comment|// Prepare the arguments required for the test.
name|Path
name|hfiles
init|=
operator|new
name|Path
argument_list|(
name|util
operator|.
name|getDataTestDirOnTestFS
argument_list|(
name|table
argument_list|)
argument_list|,
literal|"hfiles"
argument_list|)
decl_stmt|;
name|String
index|[]
name|args
init|=
operator|new
name|String
index|[]
block|{
literal|"-D"
operator|+
name|ImportTsv
operator|.
name|COLUMNS_CONF_KEY
operator|+
literal|"=HBASE_ROW_KEY,FAM:A,FAM:B"
block|,
literal|"-D"
operator|+
name|ImportTsv
operator|.
name|SEPARATOR_CONF_KEY
operator|+
literal|"=\u001b"
block|,
literal|"-D"
operator|+
name|ImportTsv
operator|.
name|BULK_OUTPUT_CONF_KEY
operator|+
literal|"="
operator|+
name|hfiles
operator|.
name|toString
argument_list|()
block|,
name|table
block|}
decl_stmt|;
name|util
operator|.
name|createTable
argument_list|(
name|table
argument_list|,
name|FAMILY
argument_list|)
expr_stmt|;
name|doMROnTableTest
argument_list|(
name|util
argument_list|,
name|FAMILY
argument_list|,
literal|null
argument_list|,
name|args
argument_list|,
literal|3
argument_list|)
expr_stmt|;
name|util
operator|.
name|deleteTable
argument_list|(
name|table
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testJobConfigurationsWithTsvImporterTextMapper
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|table
init|=
literal|"test-"
operator|+
name|UUID
operator|.
name|randomUUID
argument_list|()
decl_stmt|;
name|Path
name|bulkOutputPath
init|=
operator|new
name|Path
argument_list|(
name|util
operator|.
name|getDataTestDir
argument_list|(
name|table
argument_list|)
argument_list|,
literal|"hfiles"
argument_list|)
decl_stmt|;
name|String
name|INPUT_FILE
init|=
literal|"InputFile1.csv"
decl_stmt|;
comment|// Prepare the arguments required for the test.
name|String
index|[]
name|args
init|=
operator|new
name|String
index|[]
block|{
literal|"-D"
operator|+
name|ImportTsv
operator|.
name|MAPPER_CONF_KEY
operator|+
literal|"=org.apache.hadoop.hbase.mapreduce.TsvImporterTextMapper"
block|,
literal|"-D"
operator|+
name|ImportTsv
operator|.
name|COLUMNS_CONF_KEY
operator|+
literal|"=HBASE_ROW_KEY,FAM:A,FAM:B"
block|,
literal|"-D"
operator|+
name|ImportTsv
operator|.
name|SEPARATOR_CONF_KEY
operator|+
literal|"=,"
block|,
literal|"-D"
operator|+
name|ImportTsv
operator|.
name|BULK_OUTPUT_CONF_KEY
operator|+
literal|"="
operator|+
name|bulkOutputPath
operator|.
name|toString
argument_list|()
block|,
name|table
block|,
name|INPUT_FILE
block|}
decl_stmt|;
name|GenericOptionsParser
name|opts
init|=
operator|new
name|GenericOptionsParser
argument_list|(
name|util
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|args
argument_list|)
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
name|ImportTsv
operator|.
name|createSubmittableJob
argument_list|(
name|util
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|args
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|job
operator|.
name|getMapperClass
argument_list|()
operator|.
name|equals
argument_list|(
name|TsvImporterTextMapper
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|job
operator|.
name|getReducerClass
argument_list|()
operator|.
name|equals
argument_list|(
name|TextSortReducer
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|job
operator|.
name|getMapOutputValueClass
argument_list|()
operator|.
name|equals
argument_list|(
name|Text
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testBulkOutputWithTsvImporterTextMapper
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|table
init|=
literal|"test-"
operator|+
name|UUID
operator|.
name|randomUUID
argument_list|()
decl_stmt|;
name|String
name|FAMILY
init|=
literal|"FAM"
decl_stmt|;
name|Path
name|bulkOutputPath
init|=
operator|new
name|Path
argument_list|(
name|util
operator|.
name|getDataTestDir
argument_list|(
name|table
argument_list|)
argument_list|,
literal|"hfiles"
argument_list|)
decl_stmt|;
comment|// Prepare the arguments required for the test.
name|String
index|[]
name|args
init|=
operator|new
name|String
index|[]
block|{
literal|"-D"
operator|+
name|ImportTsv
operator|.
name|MAPPER_CONF_KEY
operator|+
literal|"=org.apache.hadoop.hbase.mapreduce.TsvImporterTextMapper"
block|,
literal|"-D"
operator|+
name|ImportTsv
operator|.
name|COLUMNS_CONF_KEY
operator|+
literal|"=HBASE_ROW_KEY,FAM:A,FAM:B"
block|,
literal|"-D"
operator|+
name|ImportTsv
operator|.
name|SEPARATOR_CONF_KEY
operator|+
literal|"=,"
block|,
literal|"-D"
operator|+
name|ImportTsv
operator|.
name|BULK_OUTPUT_CONF_KEY
operator|+
literal|"="
operator|+
name|bulkOutputPath
operator|.
name|toString
argument_list|()
block|,
name|table
block|}
decl_stmt|;
name|String
name|data
init|=
literal|"KEY\u001bVALUE4\u001bVALUE8\n"
decl_stmt|;
name|doMROnTableTest
argument_list|(
name|util
argument_list|,
name|FAMILY
argument_list|,
name|data
argument_list|,
name|args
argument_list|,
literal|4
argument_list|)
expr_stmt|;
block|}
specifier|protected
specifier|static
name|Tool
name|doMROnTableTest
parameter_list|(
name|HBaseTestingUtility
name|util
parameter_list|,
name|String
name|family
parameter_list|,
name|String
name|data
parameter_list|,
name|String
index|[]
name|args
parameter_list|)
throws|throws
name|Exception
block|{
return|return
name|doMROnTableTest
argument_list|(
name|util
argument_list|,
name|family
argument_list|,
name|data
argument_list|,
name|args
argument_list|,
literal|1
argument_list|)
return|;
block|}
comment|/**    * Run an ImportTsv job and perform basic validation on the results.    * Returns the ImportTsv<code>Tool</code> instance so that other tests can    * inspect it for further validation as necessary. This method is static to    * insure non-reliance on instance's util/conf facilities.    * @param args Any arguments to pass BEFORE inputFile path is appended.    * @return The Tool instance used to run the test.    */
specifier|protected
specifier|static
name|Tool
name|doMROnTableTest
parameter_list|(
name|HBaseTestingUtility
name|util
parameter_list|,
name|String
name|family
parameter_list|,
name|String
name|data
parameter_list|,
name|String
index|[]
name|args
parameter_list|,
name|int
name|valueMultiplier
parameter_list|)
throws|throws
name|Exception
block|{
name|String
name|table
init|=
name|args
index|[
name|args
operator|.
name|length
operator|-
literal|1
index|]
decl_stmt|;
name|Configuration
name|conf
init|=
operator|new
name|Configuration
argument_list|(
name|util
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
comment|// populate input file
name|FileSystem
name|fs
init|=
name|FileSystem
operator|.
name|get
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|Path
name|inputPath
init|=
name|fs
operator|.
name|makeQualified
argument_list|(
operator|new
name|Path
argument_list|(
name|util
operator|.
name|getDataTestDirOnTestFS
argument_list|(
name|table
argument_list|)
argument_list|,
literal|"input.dat"
argument_list|)
argument_list|)
decl_stmt|;
name|FSDataOutputStream
name|op
init|=
name|fs
operator|.
name|create
argument_list|(
name|inputPath
argument_list|,
literal|true
argument_list|)
decl_stmt|;
if|if
condition|(
name|data
operator|==
literal|null
condition|)
block|{
name|data
operator|=
literal|"KEY\u001bVALUE1\u001bVALUE2\n"
expr_stmt|;
block|}
name|op
operator|.
name|write
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|data
argument_list|)
argument_list|)
expr_stmt|;
name|op
operator|.
name|close
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"Wrote test data to file: %s"
argument_list|,
name|inputPath
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|conf
operator|.
name|getBoolean
argument_list|(
name|FORCE_COMBINER_CONF
argument_list|,
literal|true
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Forcing combiner."
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setInt
argument_list|(
literal|"min.num.spills.for.combine"
argument_list|,
literal|1
argument_list|)
expr_stmt|;
block|}
comment|// run the import
name|List
argument_list|<
name|String
argument_list|>
name|argv
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|args
argument_list|)
argument_list|)
decl_stmt|;
name|argv
operator|.
name|add
argument_list|(
name|inputPath
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|Tool
name|tool
init|=
operator|new
name|ImportTsv
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Running ImportTsv with arguments: "
operator|+
name|argv
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|ToolRunner
operator|.
name|run
argument_list|(
name|conf
argument_list|,
name|tool
argument_list|,
name|argv
operator|.
name|toArray
argument_list|(
name|args
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
comment|// Perform basic validation. If the input args did not include
comment|// ImportTsv.BULK_OUTPUT_CONF_KEY then validate data in the table.
comment|// Otherwise, validate presence of hfiles.
name|boolean
name|createdHFiles
init|=
literal|false
decl_stmt|;
name|String
name|outputPath
init|=
literal|null
decl_stmt|;
for|for
control|(
name|String
name|arg
range|:
name|argv
control|)
block|{
if|if
condition|(
name|arg
operator|.
name|contains
argument_list|(
name|ImportTsv
operator|.
name|BULK_OUTPUT_CONF_KEY
argument_list|)
condition|)
block|{
name|createdHFiles
operator|=
literal|true
expr_stmt|;
comment|// split '-Dfoo=bar' on '=' and keep 'bar'
name|outputPath
operator|=
name|arg
operator|.
name|split
argument_list|(
literal|"="
argument_list|)
index|[
literal|1
index|]
expr_stmt|;
break|break;
block|}
block|}
if|if
condition|(
name|createdHFiles
condition|)
name|validateHFiles
argument_list|(
name|fs
argument_list|,
name|outputPath
argument_list|,
name|family
argument_list|)
expr_stmt|;
else|else
name|validateTable
argument_list|(
name|conf
argument_list|,
name|table
argument_list|,
name|family
argument_list|,
name|valueMultiplier
argument_list|)
expr_stmt|;
if|if
condition|(
name|conf
operator|.
name|getBoolean
argument_list|(
name|DELETE_AFTER_LOAD_CONF
argument_list|,
literal|true
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Deleting test subdirectory"
argument_list|)
expr_stmt|;
name|util
operator|.
name|cleanupDataTestDirOnTestFS
argument_list|(
name|table
argument_list|)
expr_stmt|;
block|}
return|return
name|tool
return|;
block|}
comment|/**    * Confirm ImportTsv via data in online table.    */
specifier|private
specifier|static
name|void
name|validateTable
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|String
name|tableName
parameter_list|,
name|String
name|family
parameter_list|,
name|int
name|valueMultiplier
parameter_list|)
throws|throws
name|IOException
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Validating table."
argument_list|)
expr_stmt|;
name|HTable
name|table
init|=
operator|new
name|HTable
argument_list|(
name|conf
argument_list|,
name|tableName
argument_list|)
decl_stmt|;
name|boolean
name|verified
init|=
literal|false
decl_stmt|;
name|long
name|pause
init|=
name|conf
operator|.
name|getLong
argument_list|(
literal|"hbase.client.pause"
argument_list|,
literal|5
operator|*
literal|1000
argument_list|)
decl_stmt|;
name|int
name|numRetries
init|=
name|conf
operator|.
name|getInt
argument_list|(
name|HConstants
operator|.
name|HBASE_CLIENT_RETRIES_NUMBER
argument_list|,
literal|5
argument_list|)
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
name|numRetries
condition|;
name|i
operator|++
control|)
block|{
try|try
block|{
name|Scan
name|scan
init|=
operator|new
name|Scan
argument_list|()
decl_stmt|;
comment|// Scan entire family.
name|scan
operator|.
name|addFamily
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|family
argument_list|)
argument_list|)
expr_stmt|;
name|ResultScanner
name|resScanner
init|=
name|table
operator|.
name|getScanner
argument_list|(
name|scan
argument_list|)
decl_stmt|;
for|for
control|(
name|Result
name|res
range|:
name|resScanner
control|)
block|{
name|assertTrue
argument_list|(
name|res
operator|.
name|size
argument_list|()
operator|==
literal|2
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|KeyValue
argument_list|>
name|kvs
init|=
name|res
operator|.
name|list
argument_list|()
decl_stmt|;
name|assertArrayEquals
argument_list|(
name|kvs
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getRow
argument_list|()
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"KEY"
argument_list|)
argument_list|)
expr_stmt|;
name|assertArrayEquals
argument_list|(
name|kvs
operator|.
name|get
argument_list|(
literal|1
argument_list|)
operator|.
name|getRow
argument_list|()
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"KEY"
argument_list|)
argument_list|)
expr_stmt|;
name|assertArrayEquals
argument_list|(
name|kvs
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getValue
argument_list|()
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"VALUE"
operator|+
name|valueMultiplier
argument_list|)
argument_list|)
expr_stmt|;
name|assertArrayEquals
argument_list|(
name|kvs
operator|.
name|get
argument_list|(
literal|1
argument_list|)
operator|.
name|getValue
argument_list|()
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"VALUE"
operator|+
literal|2
operator|*
name|valueMultiplier
argument_list|)
argument_list|)
expr_stmt|;
comment|// Only one result set is expected, so let it loop.
block|}
name|verified
operator|=
literal|true
expr_stmt|;
break|break;
block|}
catch|catch
parameter_list|(
name|NullPointerException
name|e
parameter_list|)
block|{
comment|// If here, a cell was empty. Presume its because updates came in
comment|// after the scanner had been opened. Wait a while and retry.
block|}
try|try
block|{
name|Thread
operator|.
name|sleep
argument_list|(
name|pause
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
comment|// continue
block|}
block|}
name|table
operator|.
name|close
argument_list|()
expr_stmt|;
name|assertTrue
argument_list|(
name|verified
argument_list|)
expr_stmt|;
block|}
comment|/**    * Confirm ImportTsv via HFiles on fs.    */
specifier|private
specifier|static
name|void
name|validateHFiles
parameter_list|(
name|FileSystem
name|fs
parameter_list|,
name|String
name|outputPath
parameter_list|,
name|String
name|family
parameter_list|)
throws|throws
name|IOException
block|{
comment|// validate number and content of output columns
name|LOG
operator|.
name|debug
argument_list|(
literal|"Validating HFiles."
argument_list|)
expr_stmt|;
name|Set
argument_list|<
name|String
argument_list|>
name|configFamilies
init|=
operator|new
name|HashSet
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|configFamilies
operator|.
name|add
argument_list|(
name|family
argument_list|)
expr_stmt|;
name|Set
argument_list|<
name|String
argument_list|>
name|foundFamilies
init|=
operator|new
name|HashSet
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|FileStatus
name|cfStatus
range|:
name|fs
operator|.
name|listStatus
argument_list|(
operator|new
name|Path
argument_list|(
name|outputPath
argument_list|)
argument_list|,
operator|new
name|OutputFilesFilter
argument_list|()
argument_list|)
control|)
block|{
name|String
index|[]
name|elements
init|=
name|cfStatus
operator|.
name|getPath
argument_list|()
operator|.
name|toString
argument_list|()
operator|.
name|split
argument_list|(
name|Path
operator|.
name|SEPARATOR
argument_list|)
decl_stmt|;
name|String
name|cf
init|=
name|elements
index|[
name|elements
operator|.
name|length
operator|-
literal|1
index|]
decl_stmt|;
name|foundFamilies
operator|.
name|add
argument_list|(
name|cf
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"HFile ouput contains a column family (%s) not present in input families (%s)"
argument_list|,
name|cf
argument_list|,
name|configFamilies
argument_list|)
argument_list|,
name|configFamilies
operator|.
name|contains
argument_list|(
name|cf
argument_list|)
argument_list|)
expr_stmt|;
for|for
control|(
name|FileStatus
name|hfile
range|:
name|fs
operator|.
name|listStatus
argument_list|(
name|cfStatus
operator|.
name|getPath
argument_list|()
argument_list|)
control|)
block|{
name|assertTrue
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"HFile %s appears to contain no data."
argument_list|,
name|hfile
operator|.
name|getPath
argument_list|()
argument_list|)
argument_list|,
name|hfile
operator|.
name|getLen
argument_list|()
operator|>
literal|0
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit

