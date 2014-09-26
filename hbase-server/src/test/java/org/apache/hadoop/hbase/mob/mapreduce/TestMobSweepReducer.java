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
name|mob
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
name|when
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
name|CommonConfigurationKeys
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
name|TableInputFormat
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
name|mob
operator|.
name|MobConstants
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
name|mob
operator|.
name|MobUtils
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
name|mob
operator|.
name|MobZookeeper
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
name|mob
operator|.
name|mapreduce
operator|.
name|SweepJob
operator|.
name|SweepCounter
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
name|SequenceFile
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
name|io
operator|.
name|Writable
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
name|serializer
operator|.
name|JavaSerialization
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
name|counters
operator|.
name|GenericCounter
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|After
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
name|Matchers
import|;
end_import

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
name|TestMobSweepReducer
block|{
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
name|tableName
init|=
literal|"testSweepReducer"
decl_stmt|;
specifier|private
specifier|final
specifier|static
name|String
name|row
init|=
literal|"row"
decl_stmt|;
specifier|private
specifier|final
specifier|static
name|String
name|family
init|=
literal|"family"
decl_stmt|;
specifier|private
specifier|final
specifier|static
name|String
name|qf
init|=
literal|"qf"
decl_stmt|;
specifier|private
specifier|static
name|HTable
name|table
decl_stmt|;
specifier|private
specifier|static
name|Admin
name|admin
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
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setInt
argument_list|(
literal|"hbase.master.info.port"
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setBoolean
argument_list|(
literal|"hbase.regionserver.info.port.auto"
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setInt
argument_list|(
literal|"hfile.format.version"
argument_list|,
literal|3
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|startMiniCluster
argument_list|(
literal|1
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
name|SuppressWarnings
argument_list|(
literal|"deprecation"
argument_list|)
annotation|@
name|Before
specifier|public
name|void
name|setUp
parameter_list|()
throws|throws
name|Exception
block|{
name|HTableDescriptor
name|desc
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
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
name|setMobEnabled
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|hcd
operator|.
name|setMobThreshold
argument_list|(
literal|3L
argument_list|)
expr_stmt|;
name|hcd
operator|.
name|setMaxVersions
argument_list|(
literal|4
argument_list|)
expr_stmt|;
name|desc
operator|.
name|addFamily
argument_list|(
name|hcd
argument_list|)
expr_stmt|;
name|admin
operator|=
name|TEST_UTIL
operator|.
name|getHBaseAdmin
argument_list|()
expr_stmt|;
name|admin
operator|.
name|createTable
argument_list|(
name|desc
argument_list|)
expr_stmt|;
name|table
operator|=
operator|new
name|HTable
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|tableName
argument_list|)
expr_stmt|;
block|}
annotation|@
name|After
specifier|public
name|void
name|tearDown
parameter_list|()
throws|throws
name|Exception
block|{
name|admin
operator|.
name|disableTable
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
name|tableName
argument_list|)
argument_list|)
expr_stmt|;
name|admin
operator|.
name|deleteTable
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
name|tableName
argument_list|)
argument_list|)
expr_stmt|;
name|admin
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
specifier|private
name|List
argument_list|<
name|String
argument_list|>
name|getKeyFromSequenceFile
parameter_list|(
name|FileSystem
name|fs
parameter_list|,
name|Path
name|path
parameter_list|,
name|Configuration
name|conf
parameter_list|)
throws|throws
name|Exception
block|{
name|List
argument_list|<
name|String
argument_list|>
name|list
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|SequenceFile
operator|.
name|Reader
name|reader
init|=
operator|new
name|SequenceFile
operator|.
name|Reader
argument_list|(
name|conf
argument_list|,
name|SequenceFile
operator|.
name|Reader
operator|.
name|file
argument_list|(
name|path
argument_list|)
argument_list|)
decl_stmt|;
name|String
name|next
init|=
operator|(
name|String
operator|)
name|reader
operator|.
name|next
argument_list|(
operator|(
name|String
operator|)
literal|null
argument_list|)
decl_stmt|;
while|while
condition|(
name|next
operator|!=
literal|null
condition|)
block|{
name|list
operator|.
name|add
argument_list|(
name|next
argument_list|)
expr_stmt|;
name|next
operator|=
operator|(
name|String
operator|)
name|reader
operator|.
name|next
argument_list|(
operator|(
name|String
operator|)
literal|null
argument_list|)
expr_stmt|;
block|}
name|reader
operator|.
name|close
argument_list|()
expr_stmt|;
return|return
name|list
return|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testRun
parameter_list|()
throws|throws
name|Exception
block|{
name|byte
index|[]
name|mobValueBytes
init|=
operator|new
name|byte
index|[
literal|100
index|]
decl_stmt|;
comment|//get the path where mob files lie in
name|Path
name|mobFamilyPath
init|=
name|MobUtils
operator|.
name|getMobFamilyPath
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|TableName
operator|.
name|valueOf
argument_list|(
name|tableName
argument_list|)
argument_list|,
name|family
argument_list|)
decl_stmt|;
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
name|row
argument_list|)
argument_list|)
decl_stmt|;
name|put
operator|.
name|add
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|family
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|qf
argument_list|)
argument_list|,
literal|1
argument_list|,
name|mobValueBytes
argument_list|)
expr_stmt|;
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
name|row
operator|+
literal|"ignore"
argument_list|)
argument_list|)
decl_stmt|;
name|put2
operator|.
name|add
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|family
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|qf
argument_list|)
argument_list|,
literal|1
argument_list|,
name|mobValueBytes
argument_list|)
expr_stmt|;
name|table
operator|.
name|put
argument_list|(
name|put
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
name|flushCommits
argument_list|()
expr_stmt|;
name|admin
operator|.
name|flush
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
name|tableName
argument_list|)
argument_list|)
expr_stmt|;
name|FileStatus
index|[]
name|fileStatuses
init|=
name|TEST_UTIL
operator|.
name|getTestFileSystem
argument_list|()
operator|.
name|listStatus
argument_list|(
name|mobFamilyPath
argument_list|)
decl_stmt|;
comment|//check the generation of a mob file
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|fileStatuses
operator|.
name|length
argument_list|)
expr_stmt|;
name|String
name|mobFile1
init|=
name|fileStatuses
index|[
literal|0
index|]
operator|.
name|getPath
argument_list|()
operator|.
name|getName
argument_list|()
decl_stmt|;
name|Configuration
name|configuration
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
name|configuration
operator|.
name|setFloat
argument_list|(
name|MobConstants
operator|.
name|MOB_SWEEP_TOOL_COMPACTION_RATIO
argument_list|,
literal|0.6f
argument_list|)
expr_stmt|;
name|configuration
operator|.
name|setStrings
argument_list|(
name|TableInputFormat
operator|.
name|INPUT_TABLE
argument_list|,
name|tableName
argument_list|)
expr_stmt|;
name|configuration
operator|.
name|setStrings
argument_list|(
name|TableInputFormat
operator|.
name|SCAN_COLUMN_FAMILY
argument_list|,
name|family
argument_list|)
expr_stmt|;
name|configuration
operator|.
name|setStrings
argument_list|(
name|SweepJob
operator|.
name|WORKING_VISITED_DIR_KEY
argument_list|,
literal|"jobWorkingNamesDir"
argument_list|)
expr_stmt|;
name|configuration
operator|.
name|setStrings
argument_list|(
name|SweepJob
operator|.
name|WORKING_FILES_DIR_KEY
argument_list|,
literal|"compactionFileDir"
argument_list|)
expr_stmt|;
name|configuration
operator|.
name|setStrings
argument_list|(
name|CommonConfigurationKeys
operator|.
name|IO_SERIALIZATIONS_KEY
argument_list|,
name|JavaSerialization
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|configuration
operator|.
name|set
argument_list|(
name|SweepJob
operator|.
name|WORKING_VISITED_DIR_KEY
argument_list|,
literal|"compactionVisitedDir"
argument_list|)
expr_stmt|;
name|configuration
operator|.
name|setLong
argument_list|(
name|MobConstants
operator|.
name|MOB_SWEEP_TOOL_COMPACTION_START_DATE
argument_list|,
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|+
literal|24
operator|*
literal|3600
operator|*
literal|1000
argument_list|)
expr_stmt|;
name|configuration
operator|.
name|set
argument_list|(
name|SweepJob
operator|.
name|SWEEP_JOB_ID
argument_list|,
literal|"1"
argument_list|)
expr_stmt|;
name|configuration
operator|.
name|set
argument_list|(
name|SweepJob
operator|.
name|SWEEPER_NODE
argument_list|,
literal|"/hbase/MOB/testSweepReducer:family-sweeper"
argument_list|)
expr_stmt|;
name|MobZookeeper
name|zk
init|=
name|MobZookeeper
operator|.
name|newInstance
argument_list|(
name|configuration
argument_list|,
literal|"1"
argument_list|)
decl_stmt|;
name|zk
operator|.
name|addSweeperZNode
argument_list|(
name|tableName
argument_list|,
name|family
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"1"
argument_list|)
argument_list|)
expr_stmt|;
comment|//use the same counter when mocking
name|Counter
name|counter
init|=
operator|new
name|GenericCounter
argument_list|()
decl_stmt|;
name|Reducer
argument_list|<
name|Text
argument_list|,
name|KeyValue
argument_list|,
name|Writable
argument_list|,
name|Writable
argument_list|>
operator|.
name|Context
name|ctx
init|=
name|mock
argument_list|(
name|Reducer
operator|.
name|Context
operator|.
name|class
argument_list|)
decl_stmt|;
name|when
argument_list|(
name|ctx
operator|.
name|getConfiguration
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|configuration
argument_list|)
expr_stmt|;
name|when
argument_list|(
name|ctx
operator|.
name|getCounter
argument_list|(
name|Matchers
operator|.
name|any
argument_list|(
name|SweepCounter
operator|.
name|class
argument_list|)
argument_list|)
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|counter
argument_list|)
expr_stmt|;
name|when
argument_list|(
name|ctx
operator|.
name|nextKey
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
literal|true
argument_list|)
operator|.
name|thenReturn
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|when
argument_list|(
name|ctx
operator|.
name|getCurrentKey
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
operator|new
name|Text
argument_list|(
name|mobFile1
argument_list|)
argument_list|)
expr_stmt|;
name|byte
index|[]
name|refBytes
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|mobFile1
argument_list|)
decl_stmt|;
name|long
name|valueLength
init|=
name|refBytes
operator|.
name|length
decl_stmt|;
name|byte
index|[]
name|newValue
init|=
name|Bytes
operator|.
name|add
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|valueLength
argument_list|)
argument_list|,
name|refBytes
argument_list|)
decl_stmt|;
name|KeyValue
name|kv2
init|=
operator|new
name|KeyValue
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|row
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|family
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|qf
argument_list|)
argument_list|,
literal|1
argument_list|,
name|KeyValue
operator|.
name|Type
operator|.
name|Put
argument_list|,
name|newValue
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|KeyValue
argument_list|>
name|list
init|=
operator|new
name|ArrayList
argument_list|<
name|KeyValue
argument_list|>
argument_list|()
decl_stmt|;
name|list
operator|.
name|add
argument_list|(
name|kv2
argument_list|)
expr_stmt|;
name|when
argument_list|(
name|ctx
operator|.
name|getValues
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|list
argument_list|)
expr_stmt|;
name|SweepReducer
name|reducer
init|=
operator|new
name|SweepReducer
argument_list|()
decl_stmt|;
name|reducer
operator|.
name|run
argument_list|(
name|ctx
argument_list|)
expr_stmt|;
name|FileStatus
index|[]
name|filsStatuses2
init|=
name|TEST_UTIL
operator|.
name|getTestFileSystem
argument_list|()
operator|.
name|listStatus
argument_list|(
name|mobFamilyPath
argument_list|)
decl_stmt|;
name|String
name|mobFile2
init|=
name|filsStatuses2
index|[
literal|0
index|]
operator|.
name|getPath
argument_list|()
operator|.
name|getName
argument_list|()
decl_stmt|;
comment|//new mob file is generated, old one has been archived
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|filsStatuses2
operator|.
name|length
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|false
argument_list|,
name|mobFile2
operator|.
name|equalsIgnoreCase
argument_list|(
name|mobFile1
argument_list|)
argument_list|)
expr_stmt|;
comment|//test sequence file
name|String
name|workingPath
init|=
name|configuration
operator|.
name|get
argument_list|(
literal|"mob.compaction.visited.dir"
argument_list|)
decl_stmt|;
name|FileStatus
index|[]
name|statuses
init|=
name|TEST_UTIL
operator|.
name|getTestFileSystem
argument_list|()
operator|.
name|listStatus
argument_list|(
operator|new
name|Path
argument_list|(
name|workingPath
argument_list|)
argument_list|)
decl_stmt|;
name|Set
argument_list|<
name|String
argument_list|>
name|files
init|=
operator|new
name|TreeSet
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|FileStatus
name|st
range|:
name|statuses
control|)
block|{
name|files
operator|.
name|addAll
argument_list|(
name|getKeyFromSequenceFile
argument_list|(
name|TEST_UTIL
operator|.
name|getTestFileSystem
argument_list|()
argument_list|,
name|st
operator|.
name|getPath
argument_list|()
argument_list|,
name|configuration
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|files
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|true
argument_list|,
name|files
operator|.
name|contains
argument_list|(
name|mobFile1
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

