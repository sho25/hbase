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
name|HRegionInfo
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
name|mapreduce
operator|.
name|HLogInputFormat
operator|.
name|HLogRecordReader
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
name|regionserver
operator|.
name|wal
operator|.
name|HLog
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
name|regionserver
operator|.
name|wal
operator|.
name|HLogFactory
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
name|regionserver
operator|.
name|wal
operator|.
name|WALEdit
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
name|MapReduceTestUtil
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

begin_comment
comment|/**  * JUnit tests for the HLogRecordReader  */
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
name|TestHLogRecordReader
block|{
specifier|private
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
specifier|static
name|Configuration
name|conf
decl_stmt|;
specifier|private
specifier|static
name|FileSystem
name|fs
decl_stmt|;
specifier|private
specifier|static
name|Path
name|hbaseDir
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|TableName
name|tableName
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
name|getName
argument_list|()
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|rowName
init|=
name|tableName
operator|.
name|getName
argument_list|()
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|HRegionInfo
name|info
init|=
operator|new
name|HRegionInfo
argument_list|(
name|tableName
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|""
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|""
argument_list|)
argument_list|,
literal|false
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|family
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"column"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|value
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
name|HTableDescriptor
name|htd
decl_stmt|;
specifier|private
specifier|static
name|Path
name|logDir
decl_stmt|;
specifier|private
specifier|static
name|String
name|logName
decl_stmt|;
specifier|private
specifier|static
name|String
name|getName
parameter_list|()
block|{
return|return
literal|"TestHLogRecordReader"
return|;
block|}
annotation|@
name|Before
specifier|public
name|void
name|setUp
parameter_list|()
throws|throws
name|Exception
block|{
name|FileStatus
index|[]
name|entries
init|=
name|fs
operator|.
name|listStatus
argument_list|(
name|hbaseDir
argument_list|)
decl_stmt|;
for|for
control|(
name|FileStatus
name|dir
range|:
name|entries
control|)
block|{
name|fs
operator|.
name|delete
argument_list|(
name|dir
operator|.
name|getPath
argument_list|()
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
block|}
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
comment|// Make block sizes small.
name|conf
operator|=
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
expr_stmt|;
name|conf
operator|.
name|setInt
argument_list|(
literal|"dfs.blocksize"
argument_list|,
literal|1024
operator|*
literal|1024
argument_list|)
expr_stmt|;
name|conf
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
name|startMiniDFSCluster
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|conf
operator|=
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
expr_stmt|;
name|fs
operator|=
name|TEST_UTIL
operator|.
name|getDFSCluster
argument_list|()
operator|.
name|getFileSystem
argument_list|()
expr_stmt|;
name|hbaseDir
operator|=
name|TEST_UTIL
operator|.
name|createRootDir
argument_list|()
expr_stmt|;
name|logName
operator|=
name|HConstants
operator|.
name|HREGION_LOGDIR_NAME
expr_stmt|;
name|logDir
operator|=
operator|new
name|Path
argument_list|(
name|hbaseDir
argument_list|,
name|logName
argument_list|)
expr_stmt|;
name|htd
operator|=
operator|new
name|HTableDescriptor
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
name|htd
operator|.
name|addFamily
argument_list|(
operator|new
name|HColumnDescriptor
argument_list|(
name|family
argument_list|)
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
comment|/**    * Test partial reads from the log based on passed time range    * @throws Exception    */
annotation|@
name|Test
specifier|public
name|void
name|testPartialRead
parameter_list|()
throws|throws
name|Exception
block|{
name|HLog
name|log
init|=
name|HLogFactory
operator|.
name|createHLog
argument_list|(
name|fs
argument_list|,
name|hbaseDir
argument_list|,
name|logName
argument_list|,
name|conf
argument_list|)
decl_stmt|;
comment|// This test depends on timestamp being millisecond based and the filename of the WAL also
comment|// being millisecond based.
name|long
name|ts
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|WALEdit
name|edit
init|=
operator|new
name|WALEdit
argument_list|()
decl_stmt|;
specifier|final
name|AtomicLong
name|sequenceId
init|=
operator|new
name|AtomicLong
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|edit
operator|.
name|add
argument_list|(
operator|new
name|KeyValue
argument_list|(
name|rowName
argument_list|,
name|family
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"1"
argument_list|)
argument_list|,
name|ts
argument_list|,
name|value
argument_list|)
argument_list|)
expr_stmt|;
name|log
operator|.
name|append
argument_list|(
name|info
argument_list|,
name|tableName
argument_list|,
name|edit
argument_list|,
name|ts
argument_list|,
name|htd
argument_list|,
name|sequenceId
argument_list|)
expr_stmt|;
name|edit
operator|=
operator|new
name|WALEdit
argument_list|()
expr_stmt|;
name|edit
operator|.
name|add
argument_list|(
operator|new
name|KeyValue
argument_list|(
name|rowName
argument_list|,
name|family
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"2"
argument_list|)
argument_list|,
name|ts
operator|+
literal|1
argument_list|,
name|value
argument_list|)
argument_list|)
expr_stmt|;
name|log
operator|.
name|append
argument_list|(
name|info
argument_list|,
name|tableName
argument_list|,
name|edit
argument_list|,
name|ts
operator|+
literal|1
argument_list|,
name|htd
argument_list|,
name|sequenceId
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Before 1st WAL roll "
operator|+
name|log
operator|.
name|getFilenum
argument_list|()
argument_list|)
expr_stmt|;
name|log
operator|.
name|rollWriter
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Past 1st WAL roll "
operator|+
name|log
operator|.
name|getFilenum
argument_list|()
argument_list|)
expr_stmt|;
name|Thread
operator|.
name|sleep
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|long
name|ts1
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|edit
operator|=
operator|new
name|WALEdit
argument_list|()
expr_stmt|;
name|edit
operator|.
name|add
argument_list|(
operator|new
name|KeyValue
argument_list|(
name|rowName
argument_list|,
name|family
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"3"
argument_list|)
argument_list|,
name|ts1
operator|+
literal|1
argument_list|,
name|value
argument_list|)
argument_list|)
expr_stmt|;
name|log
operator|.
name|append
argument_list|(
name|info
argument_list|,
name|tableName
argument_list|,
name|edit
argument_list|,
name|ts1
operator|+
literal|1
argument_list|,
name|htd
argument_list|,
name|sequenceId
argument_list|)
expr_stmt|;
name|edit
operator|=
operator|new
name|WALEdit
argument_list|()
expr_stmt|;
name|edit
operator|.
name|add
argument_list|(
operator|new
name|KeyValue
argument_list|(
name|rowName
argument_list|,
name|family
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"4"
argument_list|)
argument_list|,
name|ts1
operator|+
literal|2
argument_list|,
name|value
argument_list|)
argument_list|)
expr_stmt|;
name|log
operator|.
name|append
argument_list|(
name|info
argument_list|,
name|tableName
argument_list|,
name|edit
argument_list|,
name|ts1
operator|+
literal|2
argument_list|,
name|htd
argument_list|,
name|sequenceId
argument_list|)
expr_stmt|;
name|log
operator|.
name|close
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Closed WAL "
operator|+
name|log
operator|.
name|getFilenum
argument_list|()
argument_list|)
expr_stmt|;
name|HLogInputFormat
name|input
init|=
operator|new
name|HLogInputFormat
argument_list|()
decl_stmt|;
name|Configuration
name|jobConf
init|=
operator|new
name|Configuration
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|jobConf
operator|.
name|set
argument_list|(
literal|"mapreduce.input.fileinputformat.inputdir"
argument_list|,
name|logDir
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|jobConf
operator|.
name|setLong
argument_list|(
name|HLogInputFormat
operator|.
name|END_TIME_KEY
argument_list|,
name|ts
argument_list|)
expr_stmt|;
comment|// only 1st file is considered, and only its 1st entry is used
name|List
argument_list|<
name|InputSplit
argument_list|>
name|splits
init|=
name|input
operator|.
name|getSplits
argument_list|(
name|MapreduceTestingShim
operator|.
name|createJobContext
argument_list|(
name|jobConf
argument_list|)
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|splits
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|testSplit
argument_list|(
name|splits
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"1"
argument_list|)
argument_list|)
expr_stmt|;
name|jobConf
operator|.
name|setLong
argument_list|(
name|HLogInputFormat
operator|.
name|START_TIME_KEY
argument_list|,
name|ts
operator|+
literal|1
argument_list|)
expr_stmt|;
name|jobConf
operator|.
name|setLong
argument_list|(
name|HLogInputFormat
operator|.
name|END_TIME_KEY
argument_list|,
name|ts1
operator|+
literal|1
argument_list|)
expr_stmt|;
name|splits
operator|=
name|input
operator|.
name|getSplits
argument_list|(
name|MapreduceTestingShim
operator|.
name|createJobContext
argument_list|(
name|jobConf
argument_list|)
argument_list|)
expr_stmt|;
comment|// both files need to be considered
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|splits
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
comment|// only the 2nd entry from the 1st file is used
name|testSplit
argument_list|(
name|splits
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"2"
argument_list|)
argument_list|)
expr_stmt|;
comment|// only the 1nd entry from the 2nd file is used
name|testSplit
argument_list|(
name|splits
operator|.
name|get
argument_list|(
literal|1
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"3"
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * Test basic functionality    * @throws Exception    */
annotation|@
name|Test
specifier|public
name|void
name|testHLogRecordReader
parameter_list|()
throws|throws
name|Exception
block|{
name|HLog
name|log
init|=
name|HLogFactory
operator|.
name|createHLog
argument_list|(
name|fs
argument_list|,
name|hbaseDir
argument_list|,
name|logName
argument_list|,
name|conf
argument_list|)
decl_stmt|;
name|byte
index|[]
name|value
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"value"
argument_list|)
decl_stmt|;
specifier|final
name|AtomicLong
name|sequenceId
init|=
operator|new
name|AtomicLong
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|WALEdit
name|edit
init|=
operator|new
name|WALEdit
argument_list|()
decl_stmt|;
name|edit
operator|.
name|add
argument_list|(
operator|new
name|KeyValue
argument_list|(
name|rowName
argument_list|,
name|family
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"1"
argument_list|)
argument_list|,
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|,
name|value
argument_list|)
argument_list|)
expr_stmt|;
name|log
operator|.
name|append
argument_list|(
name|info
argument_list|,
name|tableName
argument_list|,
name|edit
argument_list|,
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|,
name|htd
argument_list|,
name|sequenceId
argument_list|)
expr_stmt|;
name|Thread
operator|.
name|sleep
argument_list|(
literal|1
argument_list|)
expr_stmt|;
comment|// make sure 2nd log gets a later timestamp
name|long
name|secondTs
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|log
operator|.
name|rollWriter
argument_list|()
expr_stmt|;
name|edit
operator|=
operator|new
name|WALEdit
argument_list|()
expr_stmt|;
name|edit
operator|.
name|add
argument_list|(
operator|new
name|KeyValue
argument_list|(
name|rowName
argument_list|,
name|family
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"2"
argument_list|)
argument_list|,
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|,
name|value
argument_list|)
argument_list|)
expr_stmt|;
name|log
operator|.
name|append
argument_list|(
name|info
argument_list|,
name|tableName
argument_list|,
name|edit
argument_list|,
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|,
name|htd
argument_list|,
name|sequenceId
argument_list|)
expr_stmt|;
name|log
operator|.
name|close
argument_list|()
expr_stmt|;
name|long
name|thirdTs
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
comment|// should have 2 log files now
name|HLogInputFormat
name|input
init|=
operator|new
name|HLogInputFormat
argument_list|()
decl_stmt|;
name|Configuration
name|jobConf
init|=
operator|new
name|Configuration
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|jobConf
operator|.
name|set
argument_list|(
literal|"mapreduce.input.fileinputformat.inputdir"
argument_list|,
name|logDir
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
comment|// make sure both logs are found
name|List
argument_list|<
name|InputSplit
argument_list|>
name|splits
init|=
name|input
operator|.
name|getSplits
argument_list|(
name|MapreduceTestingShim
operator|.
name|createJobContext
argument_list|(
name|jobConf
argument_list|)
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|splits
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
comment|// should return exactly one KV
name|testSplit
argument_list|(
name|splits
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"1"
argument_list|)
argument_list|)
expr_stmt|;
comment|// same for the 2nd split
name|testSplit
argument_list|(
name|splits
operator|.
name|get
argument_list|(
literal|1
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"2"
argument_list|)
argument_list|)
expr_stmt|;
comment|// now test basic time ranges:
comment|// set an endtime, the 2nd log file can be ignored completely.
name|jobConf
operator|.
name|setLong
argument_list|(
name|HLogInputFormat
operator|.
name|END_TIME_KEY
argument_list|,
name|secondTs
operator|-
literal|1
argument_list|)
expr_stmt|;
name|splits
operator|=
name|input
operator|.
name|getSplits
argument_list|(
name|MapreduceTestingShim
operator|.
name|createJobContext
argument_list|(
name|jobConf
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|splits
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|testSplit
argument_list|(
name|splits
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"1"
argument_list|)
argument_list|)
expr_stmt|;
comment|// now set a start time
name|jobConf
operator|.
name|setLong
argument_list|(
name|HLogInputFormat
operator|.
name|END_TIME_KEY
argument_list|,
name|Long
operator|.
name|MAX_VALUE
argument_list|)
expr_stmt|;
name|jobConf
operator|.
name|setLong
argument_list|(
name|HLogInputFormat
operator|.
name|START_TIME_KEY
argument_list|,
name|thirdTs
argument_list|)
expr_stmt|;
name|splits
operator|=
name|input
operator|.
name|getSplits
argument_list|(
name|MapreduceTestingShim
operator|.
name|createJobContext
argument_list|(
name|jobConf
argument_list|)
argument_list|)
expr_stmt|;
comment|// both logs need to be considered
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|splits
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
comment|// but both readers skip all edits
name|testSplit
argument_list|(
name|splits
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|testSplit
argument_list|(
name|splits
operator|.
name|get
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * Create a new reader from the split, and match the edits against the passed columns.    */
specifier|private
name|void
name|testSplit
parameter_list|(
name|InputSplit
name|split
parameter_list|,
name|byte
index|[]
modifier|...
name|columns
parameter_list|)
throws|throws
name|Exception
block|{
name|HLogRecordReader
name|reader
init|=
operator|new
name|HLogRecordReader
argument_list|()
decl_stmt|;
name|reader
operator|.
name|initialize
argument_list|(
name|split
argument_list|,
name|MapReduceTestUtil
operator|.
name|createDummyMapTaskAttemptContext
argument_list|(
name|conf
argument_list|)
argument_list|)
expr_stmt|;
for|for
control|(
name|byte
index|[]
name|column
range|:
name|columns
control|)
block|{
name|assertTrue
argument_list|(
name|reader
operator|.
name|nextKeyValue
argument_list|()
argument_list|)
expr_stmt|;
name|Cell
name|cell
init|=
name|reader
operator|.
name|getCurrentValue
argument_list|()
operator|.
name|getCells
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|Bytes
operator|.
name|equals
argument_list|(
name|column
argument_list|,
name|cell
operator|.
name|getQualifier
argument_list|()
argument_list|)
condition|)
block|{
name|assertTrue
argument_list|(
literal|"expected ["
operator|+
name|Bytes
operator|.
name|toString
argument_list|(
name|column
argument_list|)
operator|+
literal|"], actual ["
operator|+
name|Bytes
operator|.
name|toString
argument_list|(
name|cell
operator|.
name|getQualifier
argument_list|()
argument_list|)
operator|+
literal|"]"
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
block|}
name|assertFalse
argument_list|(
name|reader
operator|.
name|nextKeyValue
argument_list|()
argument_list|)
expr_stmt|;
name|reader
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

