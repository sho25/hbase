begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
operator|.
name|regionserver
package|;
end_package

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
name|HTestConst
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
name|client
operator|.
name|replication
operator|.
name|ReplicationAdmin
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
name|ReplicationPeerConfig
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
name|EnvironmentEdgeManager
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
name|Threads
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
name|MiniZooKeeperCluster
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
name|TestName
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
name|LargeTests
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|TestGlobalThrottler
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
name|TestGlobalThrottler
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|Configuration
name|conf1
decl_stmt|;
specifier|private
specifier|static
name|Configuration
name|conf2
decl_stmt|;
specifier|private
specifier|static
name|HBaseTestingUtility
name|utility1
decl_stmt|;
specifier|private
specifier|static
name|HBaseTestingUtility
name|utility2
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|famName
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"f"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|VALUE
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"v"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|ROW
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"r"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
index|[]
name|ROWS
init|=
name|HTestConst
operator|.
name|makeNAscii
argument_list|(
name|ROW
argument_list|,
literal|100
argument_list|)
decl_stmt|;
annotation|@
name|Rule
specifier|public
name|TestName
name|name
init|=
operator|new
name|TestName
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
name|conf1
operator|=
name|HBaseConfiguration
operator|.
name|create
argument_list|()
expr_stmt|;
name|conf1
operator|.
name|set
argument_list|(
name|HConstants
operator|.
name|ZOOKEEPER_ZNODE_PARENT
argument_list|,
literal|"/1"
argument_list|)
expr_stmt|;
name|conf1
operator|.
name|setLong
argument_list|(
literal|"replication.source.sleepforretries"
argument_list|,
literal|100
argument_list|)
expr_stmt|;
comment|// Each WAL is about 120 bytes
name|conf1
operator|.
name|setInt
argument_list|(
name|HConstants
operator|.
name|REPLICATION_SOURCE_TOTAL_BUFFER_KEY
argument_list|,
literal|200
argument_list|)
expr_stmt|;
name|conf1
operator|.
name|setLong
argument_list|(
literal|"replication.source.per.peer.node.bandwidth"
argument_list|,
literal|100L
argument_list|)
expr_stmt|;
name|utility1
operator|=
operator|new
name|HBaseTestingUtility
argument_list|(
name|conf1
argument_list|)
expr_stmt|;
name|utility1
operator|.
name|startMiniZKCluster
argument_list|()
expr_stmt|;
name|MiniZooKeeperCluster
name|miniZK
init|=
name|utility1
operator|.
name|getZkCluster
argument_list|()
decl_stmt|;
operator|new
name|ZKWatcher
argument_list|(
name|conf1
argument_list|,
literal|"cluster1"
argument_list|,
literal|null
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|conf2
operator|=
operator|new
name|Configuration
argument_list|(
name|conf1
argument_list|)
expr_stmt|;
name|conf2
operator|.
name|set
argument_list|(
name|HConstants
operator|.
name|ZOOKEEPER_ZNODE_PARENT
argument_list|,
literal|"/2"
argument_list|)
expr_stmt|;
name|utility2
operator|=
operator|new
name|HBaseTestingUtility
argument_list|(
name|conf2
argument_list|)
expr_stmt|;
name|utility2
operator|.
name|setZkCluster
argument_list|(
name|miniZK
argument_list|)
expr_stmt|;
operator|new
name|ZKWatcher
argument_list|(
name|conf2
argument_list|,
literal|"cluster2"
argument_list|,
literal|null
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|ReplicationAdmin
name|admin1
init|=
operator|new
name|ReplicationAdmin
argument_list|(
name|conf1
argument_list|)
decl_stmt|;
name|ReplicationPeerConfig
name|rpc
init|=
operator|new
name|ReplicationPeerConfig
argument_list|()
decl_stmt|;
name|rpc
operator|.
name|setClusterKey
argument_list|(
name|utility2
operator|.
name|getClusterKey
argument_list|()
argument_list|)
expr_stmt|;
name|utility1
operator|.
name|startMiniCluster
argument_list|(
literal|1
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|utility2
operator|.
name|startMiniCluster
argument_list|(
literal|1
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|admin1
operator|.
name|addPeer
argument_list|(
literal|"peer1"
argument_list|,
name|rpc
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|admin1
operator|.
name|addPeer
argument_list|(
literal|"peer2"
argument_list|,
name|rpc
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|admin1
operator|.
name|addPeer
argument_list|(
literal|"peer3"
argument_list|,
name|rpc
argument_list|,
literal|null
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
name|utility2
operator|.
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
name|utility1
operator|.
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
block|}
specifier|volatile
specifier|private
name|boolean
name|testQuotaPass
init|=
literal|false
decl_stmt|;
specifier|volatile
specifier|private
name|boolean
name|testQuotaNonZero
init|=
literal|false
decl_stmt|;
annotation|@
name|Test
specifier|public
name|void
name|testQuota
parameter_list|()
throws|throws
name|IOException
block|{
specifier|final
name|TableName
name|tableName
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
name|name
operator|.
name|getMethodName
argument_list|()
argument_list|)
decl_stmt|;
name|HTableDescriptor
name|table
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
name|HColumnDescriptor
name|fam
init|=
operator|new
name|HColumnDescriptor
argument_list|(
name|famName
argument_list|)
decl_stmt|;
name|fam
operator|.
name|setScope
argument_list|(
name|HConstants
operator|.
name|REPLICATION_SCOPE_SERIAL
argument_list|)
expr_stmt|;
name|table
operator|.
name|addFamily
argument_list|(
name|fam
argument_list|)
expr_stmt|;
name|utility1
operator|.
name|getAdmin
argument_list|()
operator|.
name|createTable
argument_list|(
name|table
argument_list|)
expr_stmt|;
name|utility2
operator|.
name|getAdmin
argument_list|()
operator|.
name|createTable
argument_list|(
name|table
argument_list|)
expr_stmt|;
name|Thread
name|watcher
init|=
operator|new
name|Thread
argument_list|(
parameter_list|()
lambda|->
block|{
name|Replication
name|replication
init|=
operator|(
name|Replication
operator|)
name|utility1
operator|.
name|getMiniHBaseCluster
argument_list|()
operator|.
name|getRegionServer
argument_list|(
literal|0
argument_list|)
operator|.
name|getReplicationSourceService
argument_list|()
decl_stmt|;
name|AtomicLong
name|bufferUsed
init|=
name|replication
operator|.
name|getReplicationManager
argument_list|()
operator|.
name|getTotalBufferUsed
argument_list|()
decl_stmt|;
name|testQuotaPass
operator|=
literal|true
expr_stmt|;
while|while
condition|(
operator|!
name|Thread
operator|.
name|interrupted
argument_list|()
condition|)
block|{
name|long
name|size
init|=
name|bufferUsed
operator|.
name|get
argument_list|()
decl_stmt|;
if|if
condition|(
name|size
operator|>
literal|0
condition|)
block|{
name|testQuotaNonZero
operator|=
literal|true
expr_stmt|;
block|}
if|if
condition|(
name|size
operator|>
literal|600
condition|)
block|{
comment|// We read logs first then check throttler, so if the buffer quota limiter doesn't
comment|// take effect, it will push many logs and exceed the quota.
name|testQuotaPass
operator|=
literal|false
expr_stmt|;
block|}
name|Threads
operator|.
name|sleep
argument_list|(
literal|50
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
decl_stmt|;
name|watcher
operator|.
name|start
argument_list|()
expr_stmt|;
try|try
init|(
name|Table
name|t1
init|=
name|utility1
operator|.
name|getConnection
argument_list|()
operator|.
name|getTable
argument_list|(
name|tableName
argument_list|)
init|;
name|Table
name|t2
operator|=
name|utility2
operator|.
name|getConnection
argument_list|()
operator|.
name|getTable
argument_list|(
name|tableName
argument_list|)
init|)
block|{
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
literal|50
condition|;
name|i
operator|++
control|)
block|{
name|Put
name|put
init|=
operator|new
name|Put
argument_list|(
name|ROWS
index|[
name|i
index|]
argument_list|)
decl_stmt|;
name|put
operator|.
name|addColumn
argument_list|(
name|famName
argument_list|,
name|VALUE
argument_list|,
name|VALUE
argument_list|)
expr_stmt|;
name|t1
operator|.
name|put
argument_list|(
name|put
argument_list|)
expr_stmt|;
block|}
name|long
name|start
init|=
name|EnvironmentEdgeManager
operator|.
name|currentTime
argument_list|()
decl_stmt|;
while|while
condition|(
name|EnvironmentEdgeManager
operator|.
name|currentTime
argument_list|()
operator|-
name|start
operator|<
literal|180000
condition|)
block|{
name|Scan
name|scan
init|=
operator|new
name|Scan
argument_list|()
decl_stmt|;
name|scan
operator|.
name|setCaching
argument_list|(
literal|50
argument_list|)
expr_stmt|;
name|int
name|count
init|=
literal|0
decl_stmt|;
try|try
init|(
name|ResultScanner
name|results
init|=
name|t2
operator|.
name|getScanner
argument_list|(
name|scan
argument_list|)
init|)
block|{
for|for
control|(
name|Result
name|result
range|:
name|results
control|)
block|{
name|count
operator|++
expr_stmt|;
block|}
block|}
if|if
condition|(
name|count
operator|<
literal|50
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Waiting all logs pushed to slave. Expected 50 , actual "
operator|+
name|count
argument_list|)
expr_stmt|;
name|Threads
operator|.
name|sleep
argument_list|(
literal|200
argument_list|)
expr_stmt|;
continue|continue;
block|}
break|break;
block|}
block|}
name|watcher
operator|.
name|interrupt
argument_list|()
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|testQuotaPass
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|testQuotaNonZero
argument_list|)
expr_stmt|;
block|}
specifier|private
name|List
argument_list|<
name|Integer
argument_list|>
name|getRowNumbers
parameter_list|(
name|List
argument_list|<
name|Cell
argument_list|>
name|cells
parameter_list|)
block|{
name|List
argument_list|<
name|Integer
argument_list|>
name|listOfRowNumbers
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|cells
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|Cell
name|c
range|:
name|cells
control|)
block|{
name|listOfRowNumbers
operator|.
name|add
argument_list|(
name|Integer
operator|.
name|parseInt
argument_list|(
name|Bytes
operator|.
name|toString
argument_list|(
name|c
operator|.
name|getRowArray
argument_list|()
argument_list|,
name|c
operator|.
name|getRowOffset
argument_list|()
operator|+
name|ROW
operator|.
name|length
argument_list|,
name|c
operator|.
name|getRowLength
argument_list|()
operator|-
name|ROW
operator|.
name|length
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|listOfRowNumbers
return|;
block|}
block|}
end_class

end_unit

