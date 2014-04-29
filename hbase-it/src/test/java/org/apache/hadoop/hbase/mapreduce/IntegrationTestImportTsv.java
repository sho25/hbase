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
name|java
operator|.
name|lang
operator|.
name|String
operator|.
name|format
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
name|Iterator
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
name|TreeSet
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
name|HBaseCommonTestingUtility
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
name|IntegrationTestingUtility
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
name|IntegrationTests
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
name|KeyValue
operator|.
name|Type
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
name|LongWritable
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
name|mapreduce
operator|.
name|JobContext
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
name|OutputCommitter
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
name|OutputFormat
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
name|RecordWriter
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
name|TaskAttemptContext
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
name|lib
operator|.
name|input
operator|.
name|TextInputFormat
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
name|lib
operator|.
name|output
operator|.
name|FileOutputCommitter
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
name|lib
operator|.
name|output
operator|.
name|FileOutputFormat
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
name|lib
operator|.
name|partition
operator|.
name|TotalOrderPartitioner
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

begin_comment
comment|/**  * Validate ImportTsv + LoadIncrementalHFiles on a distributed cluster.  */
end_comment

begin_class
annotation|@
name|Category
argument_list|(
name|IntegrationTests
operator|.
name|class
argument_list|)
specifier|public
class|class
name|IntegrationTestImportTsv
implements|implements
name|Configurable
implements|,
name|Tool
block|{
specifier|private
specifier|static
specifier|final
name|String
name|NAME
init|=
name|IntegrationTestImportTsv
operator|.
name|class
operator|.
name|getSimpleName
argument_list|()
decl_stmt|;
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
name|IntegrationTestImportTsv
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|protected
specifier|static
specifier|final
name|String
name|simple_tsv
init|=
literal|"row1\t1\tc1\tc2\n"
operator|+
literal|"row2\t1\tc1\tc2\n"
operator|+
literal|"row3\t1\tc1\tc2\n"
operator|+
literal|"row4\t1\tc1\tc2\n"
operator|+
literal|"row5\t1\tc1\tc2\n"
operator|+
literal|"row6\t1\tc1\tc2\n"
operator|+
literal|"row7\t1\tc1\tc2\n"
operator|+
literal|"row8\t1\tc1\tc2\n"
operator|+
literal|"row9\t1\tc1\tc2\n"
operator|+
literal|"row10\t1\tc1\tc2\n"
decl_stmt|;
specifier|protected
specifier|static
specifier|final
name|Set
argument_list|<
name|KeyValue
argument_list|>
name|simple_expected
init|=
operator|new
name|TreeSet
argument_list|<
name|KeyValue
argument_list|>
argument_list|(
name|KeyValue
operator|.
name|COMPARATOR
argument_list|)
block|{
specifier|private
specifier|static
specifier|final
name|long
name|serialVersionUID
init|=
literal|1L
decl_stmt|;
block|{
name|byte
index|[]
name|family
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"d"
argument_list|)
decl_stmt|;
for|for
control|(
name|String
name|line
range|:
name|simple_tsv
operator|.
name|split
argument_list|(
literal|"\n"
argument_list|)
control|)
block|{
name|String
index|[]
name|row
init|=
name|line
operator|.
name|split
argument_list|(
literal|"\t"
argument_list|)
decl_stmt|;
name|byte
index|[]
name|key
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|row
index|[
literal|0
index|]
argument_list|)
decl_stmt|;
name|long
name|ts
init|=
name|Long
operator|.
name|parseLong
argument_list|(
name|row
index|[
literal|1
index|]
argument_list|)
decl_stmt|;
name|byte
index|[]
index|[]
name|fields
init|=
block|{
name|Bytes
operator|.
name|toBytes
argument_list|(
name|row
index|[
literal|2
index|]
argument_list|)
block|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|row
index|[
literal|3
index|]
argument_list|)
block|}
decl_stmt|;
name|add
argument_list|(
operator|new
name|KeyValue
argument_list|(
name|key
argument_list|,
name|family
argument_list|,
name|fields
index|[
literal|0
index|]
argument_list|,
name|ts
argument_list|,
name|Type
operator|.
name|Put
argument_list|,
name|fields
index|[
literal|0
index|]
argument_list|)
argument_list|)
expr_stmt|;
name|add
argument_list|(
operator|new
name|KeyValue
argument_list|(
name|key
argument_list|,
name|family
argument_list|,
name|fields
index|[
literal|1
index|]
argument_list|,
name|ts
argument_list|,
name|Type
operator|.
name|Put
argument_list|,
name|fields
index|[
literal|1
index|]
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
decl_stmt|;
comment|// this instance is initialized on first access when the test is run from
comment|// JUnit/Maven or by main when run from the CLI.
specifier|protected
specifier|static
name|IntegrationTestingUtility
name|util
init|=
literal|null
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
if|if
condition|(
literal|null
operator|==
name|util
condition|)
block|{
name|util
operator|=
operator|new
name|IntegrationTestingUtility
argument_list|()
expr_stmt|;
block|}
name|util
operator|.
name|initializeCluster
argument_list|(
literal|1
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|util
operator|.
name|isDistributedCluster
argument_list|()
condition|)
block|{
comment|// also need MR when running without a real cluster
name|util
operator|.
name|startMiniMapReduceCluster
argument_list|()
expr_stmt|;
block|}
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
name|restoreCluster
argument_list|()
expr_stmt|;
if|if
condition|(
operator|!
name|util
operator|.
name|isDistributedCluster
argument_list|()
condition|)
block|{
name|util
operator|.
name|shutdownMiniMapReduceCluster
argument_list|()
expr_stmt|;
block|}
name|util
operator|=
literal|null
expr_stmt|;
block|}
comment|/**    * Verify the data described by<code>simple_tsv</code> matches    *<code>simple_expected</code>.    */
specifier|protected
name|void
name|doLoadIncrementalHFiles
parameter_list|(
name|Path
name|hfiles
parameter_list|,
name|String
name|tableName
parameter_list|)
throws|throws
name|Exception
block|{
name|String
index|[]
name|args
init|=
block|{
name|hfiles
operator|.
name|toString
argument_list|()
block|,
name|tableName
block|}
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
name|format
argument_list|(
literal|"Running LoadIncrememntalHFiles with args: %s"
argument_list|,
name|Arrays
operator|.
name|asList
argument_list|(
name|args
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"Loading HFiles failed."
argument_list|,
literal|0
argument_list|,
name|ToolRunner
operator|.
name|run
argument_list|(
operator|new
name|LoadIncrementalHFiles
argument_list|(
operator|new
name|Configuration
argument_list|(
name|getConf
argument_list|()
argument_list|)
argument_list|)
argument_list|,
name|args
argument_list|)
argument_list|)
expr_stmt|;
name|HTable
name|table
init|=
literal|null
decl_stmt|;
name|Scan
name|scan
init|=
operator|new
name|Scan
argument_list|()
block|{
block|{
name|setCacheBlocks
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|setCaching
argument_list|(
literal|1000
argument_list|)
expr_stmt|;
block|}
block|}
decl_stmt|;
try|try
block|{
name|table
operator|=
operator|new
name|HTable
argument_list|(
name|getConf
argument_list|()
argument_list|,
name|tableName
argument_list|)
expr_stmt|;
name|Iterator
argument_list|<
name|Result
argument_list|>
name|resultsIt
init|=
name|table
operator|.
name|getScanner
argument_list|(
name|scan
argument_list|)
operator|.
name|iterator
argument_list|()
decl_stmt|;
name|Iterator
argument_list|<
name|KeyValue
argument_list|>
name|expectedIt
init|=
name|simple_expected
operator|.
name|iterator
argument_list|()
decl_stmt|;
while|while
condition|(
name|resultsIt
operator|.
name|hasNext
argument_list|()
operator|&&
name|expectedIt
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|Result
name|r
init|=
name|resultsIt
operator|.
name|next
argument_list|()
decl_stmt|;
for|for
control|(
name|Cell
name|actual
range|:
name|r
operator|.
name|rawCells
argument_list|()
control|)
block|{
name|assertTrue
argument_list|(
literal|"Ran out of expected values prematurely!"
argument_list|,
name|expectedIt
operator|.
name|hasNext
argument_list|()
argument_list|)
expr_stmt|;
name|KeyValue
name|expected
init|=
name|expectedIt
operator|.
name|next
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
name|format
argument_list|(
literal|"Scan produced surprising result. expected:<%s>, actual: %s"
argument_list|,
name|expected
argument_list|,
name|actual
argument_list|)
argument_list|,
name|KeyValue
operator|.
name|COMPARATOR
operator|.
name|compare
argument_list|(
name|expected
argument_list|,
name|actual
argument_list|)
operator|==
literal|0
argument_list|)
expr_stmt|;
block|}
block|}
name|assertFalse
argument_list|(
literal|"Did not consume all expected values."
argument_list|,
name|expectedIt
operator|.
name|hasNext
argument_list|()
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
literal|"Did not consume all scan results."
argument_list|,
name|resultsIt
operator|.
name|hasNext
argument_list|()
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
if|if
condition|(
literal|null
operator|!=
name|table
condition|)
name|table
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
comment|/**    * Confirm the absence of the {@link TotalOrderPartitioner} partitions file.    */
specifier|protected
specifier|static
name|void
name|validateDeletedPartitionsFile
parameter_list|(
name|Configuration
name|conf
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
operator|!
name|conf
operator|.
name|getBoolean
argument_list|(
name|IntegrationTestingUtility
operator|.
name|IS_DISTRIBUTED_CLUSTER
argument_list|,
literal|false
argument_list|)
condition|)
return|return;
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
name|partitionsFile
init|=
operator|new
name|Path
argument_list|(
name|TotalOrderPartitioner
operator|.
name|getPartitionFile
argument_list|(
name|conf
argument_list|)
argument_list|)
decl_stmt|;
name|assertFalse
argument_list|(
literal|"Failed to clean up partitions file."
argument_list|,
name|fs
operator|.
name|exists
argument_list|(
name|partitionsFile
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testGenerateAndLoad
parameter_list|()
throws|throws
name|Exception
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Running test testGenerateAndLoad."
argument_list|)
expr_stmt|;
name|String
name|table
init|=
name|NAME
operator|+
literal|"-"
operator|+
name|UUID
operator|.
name|randomUUID
argument_list|()
decl_stmt|;
name|String
name|cf
init|=
literal|"d"
decl_stmt|;
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
block|{
name|format
argument_list|(
literal|"-D%s=%s"
argument_list|,
name|ImportTsv
operator|.
name|BULK_OUTPUT_CONF_KEY
argument_list|,
name|hfiles
argument_list|)
block|,
name|format
argument_list|(
literal|"-D%s=HBASE_ROW_KEY,HBASE_TS_KEY,%s:c1,%s:c2"
argument_list|,
name|ImportTsv
operator|.
name|COLUMNS_CONF_KEY
argument_list|,
name|cf
argument_list|,
name|cf
argument_list|)
block|,
comment|// configure the test harness to NOT delete the HFiles after they're
comment|// generated. We need those for doLoadIncrementalHFiles
name|format
argument_list|(
literal|"-D%s=false"
argument_list|,
name|TestImportTsv
operator|.
name|DELETE_AFTER_LOAD_CONF
argument_list|)
block|,
name|table
block|}
decl_stmt|;
comment|// run the job, complete the load.
name|util
operator|.
name|createTable
argument_list|(
name|table
argument_list|,
name|cf
argument_list|)
expr_stmt|;
name|Tool
name|t
init|=
name|TestImportTsv
operator|.
name|doMROnTableTest
argument_list|(
name|util
argument_list|,
name|cf
argument_list|,
name|simple_tsv
argument_list|,
name|args
argument_list|)
decl_stmt|;
name|doLoadIncrementalHFiles
argument_list|(
name|hfiles
argument_list|,
name|table
argument_list|)
expr_stmt|;
comment|// validate post-conditions
name|validateDeletedPartitionsFile
argument_list|(
name|t
operator|.
name|getConf
argument_list|()
argument_list|)
expr_stmt|;
comment|// clean up after ourselves.
name|util
operator|.
name|deleteTable
argument_list|(
name|table
argument_list|)
expr_stmt|;
name|util
operator|.
name|cleanupDataTestDirOnTestFS
argument_list|(
name|table
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"testGenerateAndLoad completed successfully."
argument_list|)
expr_stmt|;
block|}
comment|//
comment|// helper classes used in the following test.
comment|//
comment|/**    * A {@link FileOutputCommitter} that launches an ImportTsv job through    * its {@link #commitJob(JobContext)} method.    */
specifier|private
specifier|static
class|class
name|JobLaunchingOuputCommitter
extends|extends
name|FileOutputCommitter
block|{
specifier|public
name|JobLaunchingOuputCommitter
parameter_list|(
name|Path
name|outputPath
parameter_list|,
name|TaskAttemptContext
name|context
parameter_list|)
throws|throws
name|IOException
block|{
name|super
argument_list|(
name|outputPath
argument_list|,
name|context
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|commitJob
parameter_list|(
name|JobContext
name|context
parameter_list|)
throws|throws
name|IOException
block|{
name|super
operator|.
name|commitJob
argument_list|(
name|context
argument_list|)
expr_stmt|;
comment|// inherit jar dependencies added to distributed cache loaded by parent job
name|Configuration
name|conf
init|=
name|HBaseConfiguration
operator|.
name|create
argument_list|(
name|context
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
name|conf
operator|.
name|set
argument_list|(
literal|"mapreduce.job.classpath.archives"
argument_list|,
name|context
operator|.
name|getConfiguration
argument_list|()
operator|.
name|get
argument_list|(
literal|"mapreduce.job.classpath.archives"
argument_list|,
literal|""
argument_list|)
argument_list|)
expr_stmt|;
name|conf
operator|.
name|set
argument_list|(
literal|"mapreduce.job.cache.archives.visibilities"
argument_list|,
name|context
operator|.
name|getConfiguration
argument_list|()
operator|.
name|get
argument_list|(
literal|"mapreduce.job.cache.archives.visibilities"
argument_list|,
literal|""
argument_list|)
argument_list|)
expr_stmt|;
comment|// can't use IntegrationTest instance of util because it hasn't been
comment|// instantiated on the JVM running this method. Create our own.
name|IntegrationTestingUtility
name|util
init|=
operator|new
name|IntegrationTestingUtility
argument_list|(
name|conf
argument_list|)
decl_stmt|;
comment|// this is why we're here: launch a child job. The rest of this should
comment|// look a lot like TestImportTsv#testMROnTable.
specifier|final
name|String
name|table
init|=
name|format
argument_list|(
literal|"%s-%s-child"
argument_list|,
name|NAME
argument_list|,
name|context
operator|.
name|getJobID
argument_list|()
argument_list|)
decl_stmt|;
specifier|final
name|String
name|cf
init|=
literal|"FAM"
decl_stmt|;
name|String
index|[]
name|args
init|=
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
try|try
block|{
name|util
operator|.
name|createTable
argument_list|(
name|table
argument_list|,
name|cf
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"testRunFromOutputCommitter: launching child job."
argument_list|)
expr_stmt|;
name|TestImportTsv
operator|.
name|doMROnTableTest
argument_list|(
name|util
argument_list|,
name|cf
argument_list|,
literal|null
argument_list|,
name|args
argument_list|,
literal|1
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Underlying MapReduce job failed. Aborting commit."
argument_list|,
name|e
argument_list|)
throw|;
block|}
finally|finally
block|{
name|util
operator|.
name|deleteTable
argument_list|(
name|table
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|/**    * An {@link OutputFormat} that exposes the<code>JobLaunchingOutputCommitter</code>.    */
specifier|public
specifier|static
class|class
name|JobLaunchingOutputFormat
extends|extends
name|FileOutputFormat
argument_list|<
name|LongWritable
argument_list|,
name|Text
argument_list|>
block|{
specifier|private
name|OutputCommitter
name|committer
init|=
literal|null
decl_stmt|;
annotation|@
name|Override
specifier|public
name|RecordWriter
argument_list|<
name|LongWritable
argument_list|,
name|Text
argument_list|>
name|getRecordWriter
parameter_list|(
name|TaskAttemptContext
name|job
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
return|return
operator|new
name|RecordWriter
argument_list|<
name|LongWritable
argument_list|,
name|Text
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|write
parameter_list|(
name|LongWritable
name|key
parameter_list|,
name|Text
name|value
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
comment|/* do nothing */
block|}
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|(
name|TaskAttemptContext
name|context
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
comment|/* do nothing */
block|}
block|}
return|;
block|}
annotation|@
name|Override
specifier|public
specifier|synchronized
name|OutputCommitter
name|getOutputCommitter
parameter_list|(
name|TaskAttemptContext
name|context
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|committer
operator|==
literal|null
condition|)
block|{
name|Path
name|output
init|=
name|getOutputPath
argument_list|(
name|context
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Using JobLaunchingOuputCommitter."
argument_list|)
expr_stmt|;
name|committer
operator|=
operator|new
name|JobLaunchingOuputCommitter
argument_list|(
name|output
argument_list|,
name|context
argument_list|)
expr_stmt|;
block|}
return|return
name|committer
return|;
block|}
block|}
comment|/**    * Add classes necessary for integration-test jobs.    */
specifier|public
specifier|static
name|void
name|addTestDependencyJars
parameter_list|(
name|Configuration
name|conf
parameter_list|)
throws|throws
name|IOException
block|{
name|TableMapReduceUtil
operator|.
name|addDependencyJars
argument_list|(
name|conf
argument_list|,
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|BaseConfigurable
operator|.
name|class
argument_list|,
comment|// hbase-server
name|HBaseTestingUtility
operator|.
name|class
argument_list|,
comment|// hbase-server-test
name|HBaseCommonTestingUtility
operator|.
name|class
argument_list|,
comment|// hbase-common-test
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|ListMultimap
operator|.
name|class
argument_list|,
comment|// Guava
name|org
operator|.
name|htrace
operator|.
name|Trace
operator|.
name|class
argument_list|)
expr_stmt|;
comment|// HTrace
block|}
comment|/**    * {@link TableMapReduceUtil#addDependencyJars(Job)} is used when    * configuring a mapreduce job to ensure dependencies of the job are shipped    * to the cluster. Sometimes those dependencies are on the classpath, but not    * packaged as a jar, for instance, when run at the end of another mapreduce    * job. In that case, dependency jars have already been shipped to the cluster    * and expanded in the parent job's run folder. This test validates the child    * job's classpath is constructed correctly under that scenario.    */
annotation|@
name|Test
specifier|public
name|void
name|testRunFromOutputCommitter
parameter_list|()
throws|throws
name|Exception
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Running test testRunFromOutputCommitter."
argument_list|)
expr_stmt|;
name|FileSystem
name|fs
init|=
name|FileSystem
operator|.
name|get
argument_list|(
name|getConf
argument_list|()
argument_list|)
decl_stmt|;
name|Path
name|inputPath
init|=
operator|new
name|Path
argument_list|(
name|util
operator|.
name|getDataTestDirOnTestFS
argument_list|(
literal|"parent"
argument_list|)
argument_list|,
literal|"input.txt"
argument_list|)
decl_stmt|;
name|Path
name|outputPath
init|=
operator|new
name|Path
argument_list|(
name|util
operator|.
name|getDataTestDirOnTestFS
argument_list|(
literal|"parent"
argument_list|)
argument_list|,
literal|"output"
argument_list|)
decl_stmt|;
name|FSDataOutputStream
name|fout
init|=
literal|null
decl_stmt|;
try|try
block|{
name|fout
operator|=
name|fs
operator|.
name|create
argument_list|(
name|inputPath
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|fout
operator|.
name|write
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"testRunFromOutputCommitter\n"
argument_list|)
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
name|format
argument_list|(
literal|"Wrote test data to file: %s"
argument_list|,
name|inputPath
argument_list|)
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
if|if
condition|(
name|fout
operator|!=
literal|null
condition|)
block|{
name|fout
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
comment|// create a parent job that ships the HBase dependencies. This is
comment|// accurate as the expected calling context.
name|Job
name|job
init|=
operator|new
name|Job
argument_list|(
name|getConf
argument_list|()
argument_list|,
name|NAME
operator|+
literal|".testRunFromOutputCommitter - parent"
argument_list|)
decl_stmt|;
name|job
operator|.
name|setJarByClass
argument_list|(
name|IntegrationTestImportTsv
operator|.
name|class
argument_list|)
expr_stmt|;
name|job
operator|.
name|setInputFormatClass
argument_list|(
name|TextInputFormat
operator|.
name|class
argument_list|)
expr_stmt|;
name|job
operator|.
name|setOutputFormatClass
argument_list|(
name|JobLaunchingOutputFormat
operator|.
name|class
argument_list|)
expr_stmt|;
name|TextInputFormat
operator|.
name|addInputPath
argument_list|(
name|job
argument_list|,
name|inputPath
argument_list|)
expr_stmt|;
name|JobLaunchingOutputFormat
operator|.
name|setOutputPath
argument_list|(
name|job
argument_list|,
name|outputPath
argument_list|)
expr_stmt|;
name|TableMapReduceUtil
operator|.
name|addDependencyJars
argument_list|(
name|job
argument_list|)
expr_stmt|;
name|addTestDependencyJars
argument_list|(
name|job
operator|.
name|getConfiguration
argument_list|()
argument_list|)
expr_stmt|;
comment|// Job launched by the OutputCommitter will fail if dependency jars are
comment|// not shipped properly.
name|LOG
operator|.
name|info
argument_list|(
literal|"testRunFromOutputCommitter: launching parent job."
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|job
operator|.
name|waitForCompletion
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"testRunFromOutputCommitter completed successfully."
argument_list|)
expr_stmt|;
block|}
specifier|public
name|int
name|run
parameter_list|(
name|String
index|[]
name|args
parameter_list|)
throws|throws
name|Exception
block|{
if|if
condition|(
name|args
operator|.
name|length
operator|!=
literal|0
condition|)
block|{
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
name|format
argument_list|(
literal|"%s [genericOptions]"
argument_list|,
name|NAME
argument_list|)
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"  Runs ImportTsv integration tests against a distributed cluster."
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|()
expr_stmt|;
name|GenericOptionsParser
operator|.
name|printGenericCommandUsage
argument_list|(
name|System
operator|.
name|err
argument_list|)
expr_stmt|;
return|return
literal|1
return|;
block|}
comment|// adding more test methods? Don't forget to add them here... or consider doing what
comment|// IntegrationTestsDriver does.
name|provisionCluster
argument_list|()
expr_stmt|;
name|testGenerateAndLoad
argument_list|()
expr_stmt|;
name|testRunFromOutputCommitter
argument_list|()
expr_stmt|;
name|releaseCluster
argument_list|()
expr_stmt|;
return|return
literal|0
return|;
block|}
specifier|public
specifier|static
name|void
name|main
parameter_list|(
name|String
index|[]
name|args
parameter_list|)
throws|throws
name|Exception
block|{
name|Configuration
name|conf
init|=
name|HBaseConfiguration
operator|.
name|create
argument_list|()
decl_stmt|;
name|IntegrationTestingUtility
operator|.
name|setUseDistributedCluster
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|util
operator|=
operator|new
name|IntegrationTestingUtility
argument_list|(
name|conf
argument_list|)
expr_stmt|;
comment|// not using ToolRunner to avoid unnecessary call to setConf()
name|args
operator|=
operator|new
name|GenericOptionsParser
argument_list|(
name|conf
argument_list|,
name|args
argument_list|)
operator|.
name|getRemainingArgs
argument_list|()
expr_stmt|;
name|int
name|status
init|=
operator|new
name|IntegrationTestImportTsv
argument_list|()
operator|.
name|run
argument_list|(
name|args
argument_list|)
decl_stmt|;
name|System
operator|.
name|exit
argument_list|(
name|status
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

