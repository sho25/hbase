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
name|Matchers
operator|.
name|any
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
name|ServerName
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
name|SmallTests
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
name|master
operator|.
name|TableLockManager
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
name|master
operator|.
name|TableLockManager
operator|.
name|TableLock
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
name|mapreduce
operator|.
name|SweepJob
operator|.
name|DummyMobAbortable
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
name|zookeeper
operator|.
name|ZKUtil
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
name|zookeeper
operator|.
name|ZooKeeperWatcher
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
name|Mapper
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

begin_class
annotation|@
name|Category
argument_list|(
name|SmallTests
operator|.
name|class
argument_list|)
specifier|public
class|class
name|TestMobSweepMapper
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
name|Test
specifier|public
name|void
name|TestMap
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|prefix
init|=
literal|"0000"
decl_stmt|;
specifier|final
name|String
name|fileName
init|=
literal|"19691231f2cd014ea28f42788214560a21a44cef"
decl_stmt|;
specifier|final
name|String
name|mobFilePath
init|=
name|prefix
operator|+
name|fileName
decl_stmt|;
name|ImmutableBytesWritable
name|r
init|=
operator|new
name|ImmutableBytesWritable
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"r"
argument_list|)
argument_list|)
decl_stmt|;
specifier|final
name|KeyValue
index|[]
name|kvList
init|=
operator|new
name|KeyValue
index|[
literal|1
index|]
decl_stmt|;
name|kvList
index|[
literal|0
index|]
operator|=
operator|new
name|KeyValue
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"row"
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"family"
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"column"
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|mobFilePath
argument_list|)
argument_list|)
expr_stmt|;
name|Result
name|columns
init|=
name|mock
argument_list|(
name|Result
operator|.
name|class
argument_list|)
decl_stmt|;
name|when
argument_list|(
name|columns
operator|.
name|rawCells
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|kvList
argument_list|)
expr_stmt|;
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
name|ZooKeeperWatcher
name|zkw
init|=
operator|new
name|ZooKeeperWatcher
argument_list|(
name|configuration
argument_list|,
literal|"1"
argument_list|,
operator|new
name|DummyMobAbortable
argument_list|()
argument_list|)
decl_stmt|;
name|TableName
name|tn
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"testSweepMapper"
argument_list|)
decl_stmt|;
name|TableName
name|lockName
init|=
name|MobUtils
operator|.
name|getTableLockName
argument_list|(
name|tn
argument_list|)
decl_stmt|;
name|String
name|znode
init|=
name|ZKUtil
operator|.
name|joinZNode
argument_list|(
name|zkw
operator|.
name|tableLockZNode
argument_list|,
name|lockName
operator|.
name|getNameAsString
argument_list|()
argument_list|)
decl_stmt|;
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
name|SWEEP_JOB_TABLE_NODE
argument_list|,
name|znode
argument_list|)
expr_stmt|;
name|ServerName
name|serverName
init|=
name|SweepJob
operator|.
name|getCurrentServerName
argument_list|(
name|configuration
argument_list|)
decl_stmt|;
name|configuration
operator|.
name|set
argument_list|(
name|SweepJob
operator|.
name|SWEEP_JOB_SERVERNAME
argument_list|,
name|serverName
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|TableLockManager
name|tableLockManager
init|=
name|TableLockManager
operator|.
name|createTableLockManager
argument_list|(
name|configuration
argument_list|,
name|zkw
argument_list|,
name|serverName
argument_list|)
decl_stmt|;
name|TableLock
name|lock
init|=
name|tableLockManager
operator|.
name|writeLock
argument_list|(
name|lockName
argument_list|,
literal|"Run sweep tool"
argument_list|)
decl_stmt|;
name|lock
operator|.
name|acquire
argument_list|()
expr_stmt|;
try|try
block|{
name|Mapper
argument_list|<
name|ImmutableBytesWritable
argument_list|,
name|Result
argument_list|,
name|Text
argument_list|,
name|KeyValue
argument_list|>
operator|.
name|Context
name|ctx
init|=
name|mock
argument_list|(
name|Mapper
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
name|SweepMapper
name|map
init|=
operator|new
name|SweepMapper
argument_list|()
decl_stmt|;
name|doAnswer
argument_list|(
operator|new
name|Answer
argument_list|<
name|Void
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Void
name|answer
parameter_list|(
name|InvocationOnMock
name|invocation
parameter_list|)
throws|throws
name|Throwable
block|{
name|Text
name|text
init|=
operator|(
name|Text
operator|)
name|invocation
operator|.
name|getArguments
argument_list|()
index|[
literal|0
index|]
decl_stmt|;
name|KeyValue
name|kv
init|=
operator|(
name|KeyValue
operator|)
name|invocation
operator|.
name|getArguments
argument_list|()
index|[
literal|1
index|]
decl_stmt|;
name|assertEquals
argument_list|(
name|Bytes
operator|.
name|toString
argument_list|(
name|text
operator|.
name|getBytes
argument_list|()
argument_list|,
literal|0
argument_list|,
name|text
operator|.
name|getLength
argument_list|()
argument_list|)
argument_list|,
name|fileName
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|Bytes
operator|.
name|compareTo
argument_list|(
name|kv
operator|.
name|getKey
argument_list|()
argument_list|,
name|kvList
index|[
literal|0
index|]
operator|.
name|getKey
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
block|}
argument_list|)
operator|.
name|when
argument_list|(
name|ctx
argument_list|)
operator|.
name|write
argument_list|(
name|any
argument_list|(
name|Text
operator|.
name|class
argument_list|)
argument_list|,
name|any
argument_list|(
name|KeyValue
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|map
operator|.
name|map
argument_list|(
name|r
argument_list|,
name|columns
argument_list|,
name|ctx
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|lock
operator|.
name|release
argument_list|()
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

