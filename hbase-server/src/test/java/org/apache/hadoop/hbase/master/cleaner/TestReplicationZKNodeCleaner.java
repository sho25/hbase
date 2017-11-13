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
name|master
operator|.
name|cleaner
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
name|replication
operator|.
name|ReplicationFactory
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
name|replication
operator|.
name|ReplicationQueues
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
name|replication
operator|.
name|ReplicationQueuesArguments
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
name|replication
operator|.
name|ReplicationQueuesZKImpl
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
name|MasterTests
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
name|zookeeper
operator|.
name|ZKWatcher
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
block|{
name|MasterTests
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
name|TestReplicationZKNodeCleaner
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
name|String
name|ID_ONE
init|=
literal|"1"
decl_stmt|;
specifier|private
specifier|final
name|String
name|SERVER_ONE
init|=
literal|"server1"
decl_stmt|;
specifier|private
specifier|final
name|String
name|ID_TWO
init|=
literal|"2"
decl_stmt|;
specifier|private
specifier|final
name|String
name|SERVER_TWO
init|=
literal|"server2"
decl_stmt|;
specifier|private
specifier|final
name|Configuration
name|conf
decl_stmt|;
specifier|private
specifier|final
name|ZKWatcher
name|zkw
decl_stmt|;
specifier|private
specifier|final
name|ReplicationQueues
name|repQueues
decl_stmt|;
specifier|public
name|TestReplicationZKNodeCleaner
parameter_list|()
throws|throws
name|Exception
block|{
name|conf
operator|=
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
expr_stmt|;
name|zkw
operator|=
operator|new
name|ZKWatcher
argument_list|(
name|conf
argument_list|,
literal|"TestReplicationZKNodeCleaner"
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|repQueues
operator|=
name|ReplicationFactory
operator|.
name|getReplicationQueues
argument_list|(
operator|new
name|ReplicationQueuesArguments
argument_list|(
name|conf
argument_list|,
literal|null
argument_list|,
name|zkw
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|repQueues
operator|instanceof
name|ReplicationQueuesZKImpl
argument_list|)
expr_stmt|;
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
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setInt
argument_list|(
literal|"hbase.master.cleaner.interval"
argument_list|,
literal|10000
argument_list|)
expr_stmt|;
name|TEST_UTIL
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
name|testReplicationZKNodeCleaner
parameter_list|()
throws|throws
name|Exception
block|{
name|repQueues
operator|.
name|init
argument_list|(
name|SERVER_ONE
argument_list|)
expr_stmt|;
comment|// add queue for ID_ONE which isn't exist
name|repQueues
operator|.
name|addLog
argument_list|(
name|ID_ONE
argument_list|,
literal|"file1"
argument_list|)
expr_stmt|;
name|ReplicationZKNodeCleaner
name|cleaner
init|=
operator|new
name|ReplicationZKNodeCleaner
argument_list|(
name|conf
argument_list|,
name|zkw
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
name|undeletedQueues
init|=
name|cleaner
operator|.
name|getUnDeletedQueues
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|undeletedQueues
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|undeletedQueues
operator|.
name|containsKey
argument_list|(
name|SERVER_ONE
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|undeletedQueues
operator|.
name|get
argument_list|(
name|SERVER_ONE
argument_list|)
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|undeletedQueues
operator|.
name|get
argument_list|(
name|SERVER_ONE
argument_list|)
operator|.
name|contains
argument_list|(
name|ID_ONE
argument_list|)
argument_list|)
expr_stmt|;
comment|// add a recovery queue for ID_TWO which isn't exist
name|repQueues
operator|.
name|addLog
argument_list|(
name|ID_TWO
operator|+
literal|"-"
operator|+
name|SERVER_TWO
argument_list|,
literal|"file2"
argument_list|)
expr_stmt|;
name|undeletedQueues
operator|=
name|cleaner
operator|.
name|getUnDeletedQueues
argument_list|()
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|undeletedQueues
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|undeletedQueues
operator|.
name|containsKey
argument_list|(
name|SERVER_ONE
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|undeletedQueues
operator|.
name|get
argument_list|(
name|SERVER_ONE
argument_list|)
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|undeletedQueues
operator|.
name|get
argument_list|(
name|SERVER_ONE
argument_list|)
operator|.
name|contains
argument_list|(
name|ID_ONE
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|undeletedQueues
operator|.
name|get
argument_list|(
name|SERVER_ONE
argument_list|)
operator|.
name|contains
argument_list|(
name|ID_TWO
operator|+
literal|"-"
operator|+
name|SERVER_TWO
argument_list|)
argument_list|)
expr_stmt|;
name|cleaner
operator|.
name|removeQueues
argument_list|(
name|undeletedQueues
argument_list|)
expr_stmt|;
name|undeletedQueues
operator|=
name|cleaner
operator|.
name|getUnDeletedQueues
argument_list|()
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|undeletedQueues
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testReplicationZKNodeCleanerChore
parameter_list|()
throws|throws
name|Exception
block|{
name|repQueues
operator|.
name|init
argument_list|(
name|SERVER_ONE
argument_list|)
expr_stmt|;
comment|// add queue for ID_ONE which isn't exist
name|repQueues
operator|.
name|addLog
argument_list|(
name|ID_ONE
argument_list|,
literal|"file1"
argument_list|)
expr_stmt|;
comment|// add a recovery queue for ID_TWO which isn't exist
name|repQueues
operator|.
name|addLog
argument_list|(
name|ID_TWO
operator|+
literal|"-"
operator|+
name|SERVER_TWO
argument_list|,
literal|"file2"
argument_list|)
expr_stmt|;
comment|// Wait the cleaner chore to run
name|Thread
operator|.
name|sleep
argument_list|(
literal|20000
argument_list|)
expr_stmt|;
name|ReplicationZKNodeCleaner
name|cleaner
init|=
operator|new
name|ReplicationZKNodeCleaner
argument_list|(
name|conf
argument_list|,
name|zkw
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|cleaner
operator|.
name|getUnDeletedQueues
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

