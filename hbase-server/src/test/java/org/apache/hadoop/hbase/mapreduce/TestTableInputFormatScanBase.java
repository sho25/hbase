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
name|NavigableMap
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
name|mapreduce
operator|.
name|InputSplit
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
name|Reducer
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
name|BeforeClass
import|;
end_import

begin_comment
comment|/**  *<p>  * Tests various scan start and stop row scenarios. This is set in a scan and  * tested in a MapReduce job to see if that is handed over and done properly  * too.  *</p>  *<p>  * This test is broken into two parts in order to side-step the test timeout  * period of 900, as documented in HBASE-8326.  *</p>  */
end_comment

begin_class
specifier|public
specifier|abstract
class|class
name|TestTableInputFormatScanBase
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
name|TestTableInputFormatScanBase
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|static
specifier|final
name|HBaseTestingUtility
name|TEST_UTIL
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
specifier|static
specifier|final
name|TableName
name|TABLE_NAME
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"scantest"
argument_list|)
decl_stmt|;
specifier|static
specifier|final
name|byte
index|[]
name|INPUT_FAMILY
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"contents"
argument_list|)
decl_stmt|;
specifier|static
specifier|final
name|String
name|KEY_STARTROW
init|=
literal|"startRow"
decl_stmt|;
specifier|static
specifier|final
name|String
name|KEY_LASTROW
init|=
literal|"stpRow"
decl_stmt|;
specifier|private
specifier|static
name|Table
name|table
init|=
literal|null
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
comment|// test intermittently fails under hadoop2 (2.0.2-alpha) if shortcircuit-read (scr) is on.
comment|// this turns it off for this test.  TODO: Figure out why scr breaks recovery.
name|System
operator|.
name|setProperty
argument_list|(
literal|"hbase.tests.use.shortcircuit.reads"
argument_list|,
literal|"false"
argument_list|)
expr_stmt|;
comment|// switch TIF to log at DEBUG level
name|TEST_UTIL
operator|.
name|enableDebug
argument_list|(
name|TableInputFormat
operator|.
name|class
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|enableDebug
argument_list|(
name|TableInputFormatBase
operator|.
name|class
argument_list|)
expr_stmt|;
comment|// start mini hbase cluster
name|TEST_UTIL
operator|.
name|startMiniCluster
argument_list|(
literal|3
argument_list|)
expr_stmt|;
comment|// create and fill table
name|table
operator|=
name|TEST_UTIL
operator|.
name|createMultiRegionTable
argument_list|(
name|TABLE_NAME
argument_list|,
name|INPUT_FAMILY
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|loadTable
argument_list|(
name|table
argument_list|,
name|INPUT_FAMILY
argument_list|,
literal|false
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
comment|/**    * Pass the key and value to reduce.    */
specifier|public
specifier|static
class|class
name|ScanMapper
extends|extends
name|TableMapper
argument_list|<
name|ImmutableBytesWritable
argument_list|,
name|ImmutableBytesWritable
argument_list|>
block|{
comment|/**      * Pass the key and value to reduce.      *      * @param key  The key, here "aaa", "aab" etc.      * @param value  The value is the same as the key.      * @param context  The task context.      * @throws IOException When reading the rows fails.      */
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
name|Context
name|context
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
if|if
condition|(
name|value
operator|.
name|size
argument_list|()
operator|!=
literal|1
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"There should only be one input column"
argument_list|)
throw|;
block|}
name|Map
argument_list|<
name|byte
index|[]
argument_list|,
name|NavigableMap
argument_list|<
name|byte
index|[]
argument_list|,
name|NavigableMap
argument_list|<
name|Long
argument_list|,
name|byte
index|[]
argument_list|>
argument_list|>
argument_list|>
name|cf
init|=
name|value
operator|.
name|getMap
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|cf
operator|.
name|containsKey
argument_list|(
name|INPUT_FAMILY
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Wrong input columns. Missing: '"
operator|+
name|Bytes
operator|.
name|toString
argument_list|(
name|INPUT_FAMILY
argument_list|)
operator|+
literal|"'."
argument_list|)
throw|;
block|}
name|String
name|val
init|=
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|value
operator|.
name|getValue
argument_list|(
name|INPUT_FAMILY
argument_list|,
literal|null
argument_list|)
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"map: key -> "
operator|+
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|key
operator|.
name|get
argument_list|()
argument_list|)
operator|+
literal|", value -> "
operator|+
name|val
argument_list|)
expr_stmt|;
name|context
operator|.
name|write
argument_list|(
name|key
argument_list|,
name|key
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Checks the last and first key seen against the scanner boundaries.    */
specifier|public
specifier|static
class|class
name|ScanReducer
extends|extends
name|Reducer
argument_list|<
name|ImmutableBytesWritable
argument_list|,
name|ImmutableBytesWritable
argument_list|,
name|NullWritable
argument_list|,
name|NullWritable
argument_list|>
block|{
specifier|private
name|String
name|first
init|=
literal|null
decl_stmt|;
specifier|private
name|String
name|last
init|=
literal|null
decl_stmt|;
specifier|protected
name|void
name|reduce
parameter_list|(
name|ImmutableBytesWritable
name|key
parameter_list|,
name|Iterable
argument_list|<
name|ImmutableBytesWritable
argument_list|>
name|values
parameter_list|,
name|Context
name|context
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|int
name|count
init|=
literal|0
decl_stmt|;
for|for
control|(
name|ImmutableBytesWritable
name|value
range|:
name|values
control|)
block|{
name|String
name|val
init|=
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|value
operator|.
name|get
argument_list|()
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"reduce: key["
operator|+
name|count
operator|+
literal|"] -> "
operator|+
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|key
operator|.
name|get
argument_list|()
argument_list|)
operator|+
literal|", value -> "
operator|+
name|val
argument_list|)
expr_stmt|;
if|if
condition|(
name|first
operator|==
literal|null
condition|)
name|first
operator|=
name|val
expr_stmt|;
name|last
operator|=
name|val
expr_stmt|;
name|count
operator|++
expr_stmt|;
block|}
block|}
specifier|protected
name|void
name|cleanup
parameter_list|(
name|Context
name|context
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|Configuration
name|c
init|=
name|context
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
name|String
name|startRow
init|=
name|c
operator|.
name|get
argument_list|(
name|KEY_STARTROW
argument_list|)
decl_stmt|;
name|String
name|lastRow
init|=
name|c
operator|.
name|get
argument_list|(
name|KEY_LASTROW
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"cleanup: first -> \""
operator|+
name|first
operator|+
literal|"\", start row -> \""
operator|+
name|startRow
operator|+
literal|"\""
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"cleanup: last -> \""
operator|+
name|last
operator|+
literal|"\", last row -> \""
operator|+
name|lastRow
operator|+
literal|"\""
argument_list|)
expr_stmt|;
if|if
condition|(
name|startRow
operator|!=
literal|null
operator|&&
name|startRow
operator|.
name|length
argument_list|()
operator|>
literal|0
condition|)
block|{
name|assertEquals
argument_list|(
name|startRow
argument_list|,
name|first
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|lastRow
operator|!=
literal|null
operator|&&
name|lastRow
operator|.
name|length
argument_list|()
operator|>
literal|0
condition|)
block|{
name|assertEquals
argument_list|(
name|lastRow
argument_list|,
name|last
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|/**    * Tests an MR Scan initialized from properties set in the Configuration.    *     * @throws IOException    * @throws ClassNotFoundException    * @throws InterruptedException    */
specifier|protected
name|void
name|testScanFromConfiguration
parameter_list|(
name|String
name|start
parameter_list|,
name|String
name|stop
parameter_list|,
name|String
name|last
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
throws|,
name|ClassNotFoundException
block|{
name|String
name|jobName
init|=
literal|"ScanFromConfig"
operator|+
operator|(
name|start
operator|!=
literal|null
condition|?
name|start
operator|.
name|toUpperCase
argument_list|()
else|:
literal|"Empty"
operator|)
operator|+
literal|"To"
operator|+
operator|(
name|stop
operator|!=
literal|null
condition|?
name|stop
operator|.
name|toUpperCase
argument_list|()
else|:
literal|"Empty"
operator|)
decl_stmt|;
name|Configuration
name|c
init|=
operator|new
name|Configuration
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
name|c
operator|.
name|set
argument_list|(
name|TableInputFormat
operator|.
name|INPUT_TABLE
argument_list|,
name|TABLE_NAME
operator|.
name|getNameAsString
argument_list|()
argument_list|)
expr_stmt|;
name|c
operator|.
name|set
argument_list|(
name|TableInputFormat
operator|.
name|SCAN_COLUMN_FAMILY
argument_list|,
name|Bytes
operator|.
name|toString
argument_list|(
name|INPUT_FAMILY
argument_list|)
argument_list|)
expr_stmt|;
name|c
operator|.
name|set
argument_list|(
name|KEY_STARTROW
argument_list|,
name|start
operator|!=
literal|null
condition|?
name|start
else|:
literal|""
argument_list|)
expr_stmt|;
name|c
operator|.
name|set
argument_list|(
name|KEY_LASTROW
argument_list|,
name|last
operator|!=
literal|null
condition|?
name|last
else|:
literal|""
argument_list|)
expr_stmt|;
if|if
condition|(
name|start
operator|!=
literal|null
condition|)
block|{
name|c
operator|.
name|set
argument_list|(
name|TableInputFormat
operator|.
name|SCAN_ROW_START
argument_list|,
name|start
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|stop
operator|!=
literal|null
condition|)
block|{
name|c
operator|.
name|set
argument_list|(
name|TableInputFormat
operator|.
name|SCAN_ROW_STOP
argument_list|,
name|stop
argument_list|)
expr_stmt|;
block|}
name|Job
name|job
init|=
operator|new
name|Job
argument_list|(
name|c
argument_list|,
name|jobName
argument_list|)
decl_stmt|;
name|job
operator|.
name|setMapperClass
argument_list|(
name|ScanMapper
operator|.
name|class
argument_list|)
expr_stmt|;
name|job
operator|.
name|setReducerClass
argument_list|(
name|ScanReducer
operator|.
name|class
argument_list|)
expr_stmt|;
name|job
operator|.
name|setMapOutputKeyClass
argument_list|(
name|ImmutableBytesWritable
operator|.
name|class
argument_list|)
expr_stmt|;
name|job
operator|.
name|setMapOutputValueClass
argument_list|(
name|ImmutableBytesWritable
operator|.
name|class
argument_list|)
expr_stmt|;
name|job
operator|.
name|setInputFormatClass
argument_list|(
name|TableInputFormat
operator|.
name|class
argument_list|)
expr_stmt|;
name|job
operator|.
name|setNumReduceTasks
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|FileOutputFormat
operator|.
name|setOutputPath
argument_list|(
name|job
argument_list|,
operator|new
name|Path
argument_list|(
name|job
operator|.
name|getJobName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|TableMapReduceUtil
operator|.
name|addDependencyJars
argument_list|(
name|job
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
block|}
comment|/**    * Tests a MR scan using specific start and stop rows.    *    * @throws IOException    * @throws ClassNotFoundException    * @throws InterruptedException    */
specifier|protected
name|void
name|testScan
parameter_list|(
name|String
name|start
parameter_list|,
name|String
name|stop
parameter_list|,
name|String
name|last
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
throws|,
name|ClassNotFoundException
block|{
name|String
name|jobName
init|=
literal|"Scan"
operator|+
operator|(
name|start
operator|!=
literal|null
condition|?
name|start
operator|.
name|toUpperCase
argument_list|()
else|:
literal|"Empty"
operator|)
operator|+
literal|"To"
operator|+
operator|(
name|stop
operator|!=
literal|null
condition|?
name|stop
operator|.
name|toUpperCase
argument_list|()
else|:
literal|"Empty"
operator|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Before map/reduce startup - job "
operator|+
name|jobName
argument_list|)
expr_stmt|;
name|Configuration
name|c
init|=
operator|new
name|Configuration
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
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
name|addFamily
argument_list|(
name|INPUT_FAMILY
argument_list|)
expr_stmt|;
if|if
condition|(
name|start
operator|!=
literal|null
condition|)
block|{
name|scan
operator|.
name|setStartRow
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|start
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|c
operator|.
name|set
argument_list|(
name|KEY_STARTROW
argument_list|,
name|start
operator|!=
literal|null
condition|?
name|start
else|:
literal|""
argument_list|)
expr_stmt|;
if|if
condition|(
name|stop
operator|!=
literal|null
condition|)
block|{
name|scan
operator|.
name|setStopRow
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|stop
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|c
operator|.
name|set
argument_list|(
name|KEY_LASTROW
argument_list|,
name|last
operator|!=
literal|null
condition|?
name|last
else|:
literal|""
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"scan before: "
operator|+
name|scan
argument_list|)
expr_stmt|;
name|Job
name|job
init|=
operator|new
name|Job
argument_list|(
name|c
argument_list|,
name|jobName
argument_list|)
decl_stmt|;
name|TableMapReduceUtil
operator|.
name|initTableMapperJob
argument_list|(
name|TABLE_NAME
argument_list|,
name|scan
argument_list|,
name|ScanMapper
operator|.
name|class
argument_list|,
name|ImmutableBytesWritable
operator|.
name|class
argument_list|,
name|ImmutableBytesWritable
operator|.
name|class
argument_list|,
name|job
argument_list|)
expr_stmt|;
name|job
operator|.
name|setReducerClass
argument_list|(
name|ScanReducer
operator|.
name|class
argument_list|)
expr_stmt|;
name|job
operator|.
name|setNumReduceTasks
argument_list|(
literal|1
argument_list|)
expr_stmt|;
comment|// one to get final "first" and "last" key
name|FileOutputFormat
operator|.
name|setOutputPath
argument_list|(
name|job
argument_list|,
operator|new
name|Path
argument_list|(
name|job
operator|.
name|getJobName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Started "
operator|+
name|job
operator|.
name|getJobName
argument_list|()
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
literal|"After map/reduce completion - job "
operator|+
name|jobName
argument_list|)
expr_stmt|;
block|}
comment|/**    * Tests a MR scan using data skew auto-balance    *    * @throws IOException    * @throws ClassNotFoundException    * @throws InterruptedException    */
specifier|public
name|void
name|testNumOfSplits
parameter_list|(
name|String
name|ratio
parameter_list|,
name|int
name|expectedNumOfSplits
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
throws|,
name|ClassNotFoundException
block|{
name|String
name|jobName
init|=
literal|"TestJobForNumOfSplits"
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Before map/reduce startup - job "
operator|+
name|jobName
argument_list|)
expr_stmt|;
name|Configuration
name|c
init|=
operator|new
name|Configuration
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
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
name|addFamily
argument_list|(
name|INPUT_FAMILY
argument_list|)
expr_stmt|;
name|c
operator|.
name|set
argument_list|(
literal|"hbase.mapreduce.input.autobalance"
argument_list|,
literal|"true"
argument_list|)
expr_stmt|;
name|c
operator|.
name|set
argument_list|(
literal|"hbase.mapreduce.input.autobalance.maxskewratio"
argument_list|,
name|ratio
argument_list|)
expr_stmt|;
name|c
operator|.
name|set
argument_list|(
name|KEY_STARTROW
argument_list|,
literal|""
argument_list|)
expr_stmt|;
name|c
operator|.
name|set
argument_list|(
name|KEY_LASTROW
argument_list|,
literal|""
argument_list|)
expr_stmt|;
name|Job
name|job
init|=
operator|new
name|Job
argument_list|(
name|c
argument_list|,
name|jobName
argument_list|)
decl_stmt|;
name|TableMapReduceUtil
operator|.
name|initTableMapperJob
argument_list|(
name|TABLE_NAME
operator|.
name|getNameAsString
argument_list|()
argument_list|,
name|scan
argument_list|,
name|ScanMapper
operator|.
name|class
argument_list|,
name|ImmutableBytesWritable
operator|.
name|class
argument_list|,
name|ImmutableBytesWritable
operator|.
name|class
argument_list|,
name|job
argument_list|)
expr_stmt|;
name|TableInputFormat
name|tif
init|=
operator|new
name|TableInputFormat
argument_list|()
decl_stmt|;
name|tif
operator|.
name|setConf
argument_list|(
name|job
operator|.
name|getConfiguration
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|TABLE_NAME
argument_list|,
name|table
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|InputSplit
argument_list|>
name|splits
init|=
name|tif
operator|.
name|getSplits
argument_list|(
name|job
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|expectedNumOfSplits
argument_list|,
name|splits
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**    * Tests for the getSplitKey() method in TableInputFormatBase.java    */
specifier|public
name|void
name|testGetSplitKey
parameter_list|(
name|byte
index|[]
name|startKey
parameter_list|,
name|byte
index|[]
name|endKey
parameter_list|,
name|byte
index|[]
name|splitKey
parameter_list|,
name|boolean
name|isText
parameter_list|)
block|{
name|byte
index|[]
name|result
init|=
name|TableInputFormatBase
operator|.
name|getSplitKey
argument_list|(
name|startKey
argument_list|,
name|endKey
argument_list|,
name|isText
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertArrayEquals
argument_list|(
name|splitKey
argument_list|,
name|result
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

