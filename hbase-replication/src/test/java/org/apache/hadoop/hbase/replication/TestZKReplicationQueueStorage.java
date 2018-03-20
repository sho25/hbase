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
name|replication
package|;
end_package

begin_import
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|CoreMatchers
operator|.
name|hasItems
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
name|assertThat
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
name|Collections
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
name|SortedSet
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
name|HBaseZKTestingUtility
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
name|testclassification
operator|.
name|ReplicationTests
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
name|Pair
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|zookeeper
operator|.
name|KeeperException
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
name|ReplicationTests
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
name|TestZKReplicationQueueStorage
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
name|TestZKReplicationQueueStorage
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|HBaseZKTestingUtility
name|UTIL
init|=
operator|new
name|HBaseZKTestingUtility
argument_list|()
decl_stmt|;
specifier|private
specifier|static
name|ZKReplicationQueueStorage
name|STORAGE
decl_stmt|;
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|setUp
parameter_list|()
throws|throws
name|Exception
block|{
name|UTIL
operator|.
name|startMiniZKCluster
argument_list|()
expr_stmt|;
name|STORAGE
operator|=
operator|new
name|ZKReplicationQueueStorage
argument_list|(
name|UTIL
operator|.
name|getZooKeeperWatcher
argument_list|()
argument_list|,
name|UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|AfterClass
specifier|public
specifier|static
name|void
name|tearDown
parameter_list|()
throws|throws
name|IOException
block|{
name|UTIL
operator|.
name|shutdownMiniZKCluster
argument_list|()
expr_stmt|;
block|}
annotation|@
name|After
specifier|public
name|void
name|tearDownAfterTest
parameter_list|()
throws|throws
name|ReplicationException
block|{
for|for
control|(
name|ServerName
name|serverName
range|:
name|STORAGE
operator|.
name|getListOfReplicators
argument_list|()
control|)
block|{
for|for
control|(
name|String
name|queue
range|:
name|STORAGE
operator|.
name|getAllQueues
argument_list|(
name|serverName
argument_list|)
control|)
block|{
name|STORAGE
operator|.
name|removeQueue
argument_list|(
name|serverName
argument_list|,
name|queue
argument_list|)
expr_stmt|;
block|}
name|STORAGE
operator|.
name|removeReplicatorIfQueueIsEmpty
argument_list|(
name|serverName
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|String
name|peerId
range|:
name|STORAGE
operator|.
name|getAllPeersFromHFileRefsQueue
argument_list|()
control|)
block|{
name|STORAGE
operator|.
name|removePeerFromHFileRefs
argument_list|(
name|peerId
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|ServerName
name|getServerName
parameter_list|(
name|int
name|i
parameter_list|)
block|{
return|return
name|ServerName
operator|.
name|valueOf
argument_list|(
literal|"127.0.0.1"
argument_list|,
literal|8000
operator|+
name|i
argument_list|,
literal|10000
operator|+
name|i
argument_list|)
return|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testReplicator
parameter_list|()
throws|throws
name|ReplicationException
block|{
name|assertTrue
argument_list|(
name|STORAGE
operator|.
name|getListOfReplicators
argument_list|()
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
name|String
name|queueId
init|=
literal|"1"
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
name|i
operator|++
control|)
block|{
name|STORAGE
operator|.
name|addWAL
argument_list|(
name|getServerName
argument_list|(
name|i
argument_list|)
argument_list|,
name|queueId
argument_list|,
literal|"file"
operator|+
name|i
argument_list|)
expr_stmt|;
block|}
name|List
argument_list|<
name|ServerName
argument_list|>
name|replicators
init|=
name|STORAGE
operator|.
name|getListOfReplicators
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|10
argument_list|,
name|replicators
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
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
name|i
operator|++
control|)
block|{
name|assertThat
argument_list|(
name|replicators
argument_list|,
name|hasItems
argument_list|(
name|getServerName
argument_list|(
name|i
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
literal|5
condition|;
name|i
operator|++
control|)
block|{
name|STORAGE
operator|.
name|removeQueue
argument_list|(
name|getServerName
argument_list|(
name|i
argument_list|)
argument_list|,
name|queueId
argument_list|)
expr_stmt|;
block|}
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
name|i
operator|++
control|)
block|{
name|STORAGE
operator|.
name|removeReplicatorIfQueueIsEmpty
argument_list|(
name|getServerName
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|replicators
operator|=
name|STORAGE
operator|.
name|getListOfReplicators
argument_list|()
expr_stmt|;
name|assertEquals
argument_list|(
literal|5
argument_list|,
name|replicators
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|5
init|;
name|i
operator|<
literal|10
condition|;
name|i
operator|++
control|)
block|{
name|assertThat
argument_list|(
name|replicators
argument_list|,
name|hasItems
argument_list|(
name|getServerName
argument_list|(
name|i
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|String
name|getFileName
parameter_list|(
name|String
name|base
parameter_list|,
name|int
name|i
parameter_list|)
block|{
return|return
name|String
operator|.
name|format
argument_list|(
name|base
operator|+
literal|"-%04d"
argument_list|,
name|i
argument_list|)
return|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testAddRemoveLog
parameter_list|()
throws|throws
name|ReplicationException
block|{
name|ServerName
name|serverName1
init|=
name|ServerName
operator|.
name|valueOf
argument_list|(
literal|"127.0.0.1"
argument_list|,
literal|8000
argument_list|,
literal|10000
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|STORAGE
operator|.
name|getAllQueues
argument_list|(
name|serverName1
argument_list|)
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
name|String
name|queue1
init|=
literal|"1"
decl_stmt|;
name|String
name|queue2
init|=
literal|"2"
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
name|i
operator|++
control|)
block|{
name|STORAGE
operator|.
name|addWAL
argument_list|(
name|serverName1
argument_list|,
name|queue1
argument_list|,
name|getFileName
argument_list|(
literal|"file1"
argument_list|,
name|i
argument_list|)
argument_list|)
expr_stmt|;
name|STORAGE
operator|.
name|addWAL
argument_list|(
name|serverName1
argument_list|,
name|queue2
argument_list|,
name|getFileName
argument_list|(
literal|"file2"
argument_list|,
name|i
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|List
argument_list|<
name|String
argument_list|>
name|queueIds
init|=
name|STORAGE
operator|.
name|getAllQueues
argument_list|(
name|serverName1
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|queueIds
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|queueIds
argument_list|,
name|hasItems
argument_list|(
literal|"1"
argument_list|,
literal|"2"
argument_list|)
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|wals1
init|=
name|STORAGE
operator|.
name|getWALsInQueue
argument_list|(
name|serverName1
argument_list|,
name|queue1
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|wals2
init|=
name|STORAGE
operator|.
name|getWALsInQueue
argument_list|(
name|serverName1
argument_list|,
name|queue2
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|10
argument_list|,
name|wals1
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|10
argument_list|,
name|wals2
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
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
name|i
operator|++
control|)
block|{
name|assertThat
argument_list|(
name|wals1
argument_list|,
name|hasItems
argument_list|(
name|getFileName
argument_list|(
literal|"file1"
argument_list|,
name|i
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|wals2
argument_list|,
name|hasItems
argument_list|(
name|getFileName
argument_list|(
literal|"file2"
argument_list|,
name|i
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
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
name|i
operator|++
control|)
block|{
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|STORAGE
operator|.
name|getWALPosition
argument_list|(
name|serverName1
argument_list|,
name|queue1
argument_list|,
name|getFileName
argument_list|(
literal|"file1"
argument_list|,
name|i
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|STORAGE
operator|.
name|getWALPosition
argument_list|(
name|serverName1
argument_list|,
name|queue2
argument_list|,
name|getFileName
argument_list|(
literal|"file2"
argument_list|,
name|i
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|STORAGE
operator|.
name|setWALPosition
argument_list|(
name|serverName1
argument_list|,
name|queue1
argument_list|,
name|getFileName
argument_list|(
literal|"file1"
argument_list|,
name|i
argument_list|)
argument_list|,
operator|(
name|i
operator|+
literal|1
operator|)
operator|*
literal|100
argument_list|,
name|Collections
operator|.
name|emptyMap
argument_list|()
argument_list|)
expr_stmt|;
name|STORAGE
operator|.
name|setWALPosition
argument_list|(
name|serverName1
argument_list|,
name|queue2
argument_list|,
name|getFileName
argument_list|(
literal|"file2"
argument_list|,
name|i
argument_list|)
argument_list|,
operator|(
name|i
operator|+
literal|1
operator|)
operator|*
literal|100
operator|+
literal|10
argument_list|,
name|Collections
operator|.
name|emptyMap
argument_list|()
argument_list|)
expr_stmt|;
block|}
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
name|i
operator|++
control|)
block|{
name|assertEquals
argument_list|(
operator|(
name|i
operator|+
literal|1
operator|)
operator|*
literal|100
argument_list|,
name|STORAGE
operator|.
name|getWALPosition
argument_list|(
name|serverName1
argument_list|,
name|queue1
argument_list|,
name|getFileName
argument_list|(
literal|"file1"
argument_list|,
name|i
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
operator|(
name|i
operator|+
literal|1
operator|)
operator|*
literal|100
operator|+
literal|10
argument_list|,
name|STORAGE
operator|.
name|getWALPosition
argument_list|(
name|serverName1
argument_list|,
name|queue2
argument_list|,
name|getFileName
argument_list|(
literal|"file2"
argument_list|,
name|i
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
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
name|i
operator|++
control|)
block|{
if|if
condition|(
name|i
operator|%
literal|2
operator|==
literal|0
condition|)
block|{
name|STORAGE
operator|.
name|removeWAL
argument_list|(
name|serverName1
argument_list|,
name|queue1
argument_list|,
name|getFileName
argument_list|(
literal|"file1"
argument_list|,
name|i
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|STORAGE
operator|.
name|removeWAL
argument_list|(
name|serverName1
argument_list|,
name|queue2
argument_list|,
name|getFileName
argument_list|(
literal|"file2"
argument_list|,
name|i
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
name|queueIds
operator|=
name|STORAGE
operator|.
name|getAllQueues
argument_list|(
name|serverName1
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|queueIds
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|queueIds
argument_list|,
name|hasItems
argument_list|(
literal|"1"
argument_list|,
literal|"2"
argument_list|)
argument_list|)
expr_stmt|;
name|ServerName
name|serverName2
init|=
name|ServerName
operator|.
name|valueOf
argument_list|(
literal|"127.0.0.1"
argument_list|,
literal|8001
argument_list|,
literal|10001
argument_list|)
decl_stmt|;
name|Pair
argument_list|<
name|String
argument_list|,
name|SortedSet
argument_list|<
name|String
argument_list|>
argument_list|>
name|peer1
init|=
name|STORAGE
operator|.
name|claimQueue
argument_list|(
name|serverName1
argument_list|,
literal|"1"
argument_list|,
name|serverName2
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"1-"
operator|+
name|serverName1
operator|.
name|getServerName
argument_list|()
argument_list|,
name|peer1
operator|.
name|getFirst
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|5
argument_list|,
name|peer1
operator|.
name|getSecond
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|int
name|i
init|=
literal|1
decl_stmt|;
for|for
control|(
name|String
name|wal
range|:
name|peer1
operator|.
name|getSecond
argument_list|()
control|)
block|{
name|assertEquals
argument_list|(
name|getFileName
argument_list|(
literal|"file1"
argument_list|,
name|i
argument_list|)
argument_list|,
name|wal
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
operator|(
name|i
operator|+
literal|1
operator|)
operator|*
literal|100
argument_list|,
name|STORAGE
operator|.
name|getWALPosition
argument_list|(
name|serverName2
argument_list|,
name|peer1
operator|.
name|getFirst
argument_list|()
argument_list|,
name|getFileName
argument_list|(
literal|"file1"
argument_list|,
name|i
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|i
operator|+=
literal|2
expr_stmt|;
block|}
name|queueIds
operator|=
name|STORAGE
operator|.
name|getAllQueues
argument_list|(
name|serverName1
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|queueIds
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|queueIds
argument_list|,
name|hasItems
argument_list|(
literal|"2"
argument_list|)
argument_list|)
expr_stmt|;
name|wals2
operator|=
name|STORAGE
operator|.
name|getWALsInQueue
argument_list|(
name|serverName1
argument_list|,
name|queue2
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|5
argument_list|,
name|wals2
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|i
operator|=
literal|0
init|;
name|i
operator|<
literal|10
condition|;
name|i
operator|+=
literal|2
control|)
block|{
name|assertThat
argument_list|(
name|wals2
argument_list|,
name|hasItems
argument_list|(
name|getFileName
argument_list|(
literal|"file2"
argument_list|,
name|i
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|queueIds
operator|=
name|STORAGE
operator|.
name|getAllQueues
argument_list|(
name|serverName2
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|queueIds
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|queueIds
argument_list|,
name|hasItems
argument_list|(
name|peer1
operator|.
name|getFirst
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|wals1
operator|=
name|STORAGE
operator|.
name|getWALsInQueue
argument_list|(
name|serverName2
argument_list|,
name|peer1
operator|.
name|getFirst
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|5
argument_list|,
name|wals1
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|i
operator|=
literal|1
init|;
name|i
operator|<
literal|10
condition|;
name|i
operator|+=
literal|2
control|)
block|{
name|assertThat
argument_list|(
name|wals1
argument_list|,
name|hasItems
argument_list|(
name|getFileName
argument_list|(
literal|"file1"
argument_list|,
name|i
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|Set
argument_list|<
name|String
argument_list|>
name|allWals
init|=
name|STORAGE
operator|.
name|getAllWALs
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|10
argument_list|,
name|allWals
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|i
operator|=
literal|0
init|;
name|i
operator|<
literal|10
condition|;
name|i
operator|++
control|)
block|{
name|assertThat
argument_list|(
name|allWals
argument_list|,
name|hasItems
argument_list|(
name|i
operator|%
literal|2
operator|==
literal|0
condition|?
name|getFileName
argument_list|(
literal|"file2"
argument_list|,
name|i
argument_list|)
else|:
name|getFileName
argument_list|(
literal|"file1"
argument_list|,
name|i
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
comment|// For HBASE-12865
annotation|@
name|Test
specifier|public
name|void
name|testClaimQueueChangeCversion
parameter_list|()
throws|throws
name|ReplicationException
throws|,
name|KeeperException
block|{
name|ServerName
name|serverName1
init|=
name|ServerName
operator|.
name|valueOf
argument_list|(
literal|"127.0.0.1"
argument_list|,
literal|8000
argument_list|,
literal|10000
argument_list|)
decl_stmt|;
name|STORAGE
operator|.
name|addWAL
argument_list|(
name|serverName1
argument_list|,
literal|"1"
argument_list|,
literal|"file"
argument_list|)
expr_stmt|;
name|int
name|v0
init|=
name|STORAGE
operator|.
name|getQueuesZNodeCversion
argument_list|()
decl_stmt|;
name|ServerName
name|serverName2
init|=
name|ServerName
operator|.
name|valueOf
argument_list|(
literal|"127.0.0.1"
argument_list|,
literal|8001
argument_list|,
literal|10001
argument_list|)
decl_stmt|;
name|STORAGE
operator|.
name|claimQueue
argument_list|(
name|serverName1
argument_list|,
literal|"1"
argument_list|,
name|serverName2
argument_list|)
expr_stmt|;
name|int
name|v1
init|=
name|STORAGE
operator|.
name|getQueuesZNodeCversion
argument_list|()
decl_stmt|;
comment|// cversion should increase by 1 since a child node is deleted
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|v1
operator|-
name|v0
argument_list|)
expr_stmt|;
block|}
specifier|private
name|ZKReplicationQueueStorage
name|createWithUnstableCversion
parameter_list|()
throws|throws
name|IOException
block|{
return|return
operator|new
name|ZKReplicationQueueStorage
argument_list|(
name|UTIL
operator|.
name|getZooKeeperWatcher
argument_list|()
argument_list|,
name|UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
block|{
specifier|private
name|int
name|called
init|=
literal|0
decl_stmt|;
annotation|@
name|Override
specifier|protected
name|int
name|getQueuesZNodeCversion
parameter_list|()
throws|throws
name|KeeperException
block|{
if|if
condition|(
name|called
operator|<
literal|4
condition|)
block|{
name|called
operator|++
expr_stmt|;
block|}
return|return
name|called
return|;
block|}
block|}
return|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testGetAllWALsCversionChange
parameter_list|()
throws|throws
name|IOException
throws|,
name|ReplicationException
block|{
name|ZKReplicationQueueStorage
name|storage
init|=
name|createWithUnstableCversion
argument_list|()
decl_stmt|;
name|storage
operator|.
name|addWAL
argument_list|(
name|getServerName
argument_list|(
literal|0
argument_list|)
argument_list|,
literal|"1"
argument_list|,
literal|"file"
argument_list|)
expr_stmt|;
comment|// This should return eventually when cversion stabilizes
name|Set
argument_list|<
name|String
argument_list|>
name|allWals
init|=
name|storage
operator|.
name|getAllWALs
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|allWals
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|allWals
argument_list|,
name|hasItems
argument_list|(
literal|"file"
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// For HBASE-14621
annotation|@
name|Test
specifier|public
name|void
name|testGetAllHFileRefsCversionChange
parameter_list|()
throws|throws
name|IOException
throws|,
name|ReplicationException
block|{
name|ZKReplicationQueueStorage
name|storage
init|=
name|createWithUnstableCversion
argument_list|()
decl_stmt|;
name|storage
operator|.
name|addPeerToHFileRefs
argument_list|(
literal|"1"
argument_list|)
expr_stmt|;
name|Path
name|p
init|=
operator|new
name|Path
argument_list|(
literal|"/test"
argument_list|)
decl_stmt|;
name|storage
operator|.
name|addHFileRefs
argument_list|(
literal|"1"
argument_list|,
name|Arrays
operator|.
name|asList
argument_list|(
name|Pair
operator|.
name|newPair
argument_list|(
name|p
argument_list|,
name|p
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
comment|// This should return eventually when cversion stabilizes
name|Set
argument_list|<
name|String
argument_list|>
name|allHFileRefs
init|=
name|storage
operator|.
name|getAllHFileRefs
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|allHFileRefs
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|allHFileRefs
argument_list|,
name|hasItems
argument_list|(
literal|"test"
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testRegionsZNodeLayout
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|peerId
init|=
literal|"1"
decl_stmt|;
name|String
name|encodedRegionName
init|=
literal|"31d9792f4435b99d9fb1016f6fbc8dc7"
decl_stmt|;
name|String
name|expectedPath
init|=
literal|"/hbase/replication/regions/31/d9/"
operator|+
name|encodedRegionName
operator|+
literal|"-"
operator|+
name|peerId
decl_stmt|;
name|String
name|path
init|=
name|STORAGE
operator|.
name|getSerialReplicationRegionPeerNode
argument_list|(
name|encodedRegionName
argument_list|,
name|peerId
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|expectedPath
argument_list|,
name|path
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

