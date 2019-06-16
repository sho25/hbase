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
operator|.
name|regionserver
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
name|concurrent
operator|.
name|Callable
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
name|AtomicBoolean
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
name|AtomicInteger
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
name|Waiter
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
name|HBaseAdmin
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
name|ipc
operator|.
name|RpcServer
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
name|replication
operator|.
name|TestReplicationBase
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
name|hbase
operator|.
name|wal
operator|.
name|WAL
operator|.
name|Entry
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

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|LoggerFactory
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hbase
operator|.
name|thirdparty
operator|.
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|ServiceException
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
name|TestReplicator
extends|extends
name|TestReplicationBase
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
name|TestReplicator
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|TestReplicator
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|static
specifier|final
name|int
name|NUM_ROWS
init|=
literal|10
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
comment|// Set RPC size limit to 10kb (will be applied to both source and sink clusters)
name|CONF1
operator|.
name|setInt
argument_list|(
name|RpcServer
operator|.
name|MAX_REQUEST_SIZE
argument_list|,
literal|1024
operator|*
literal|10
argument_list|)
expr_stmt|;
name|TestReplicationBase
operator|.
name|setUpBeforeClass
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testReplicatorBatching
parameter_list|()
throws|throws
name|Exception
block|{
comment|// Clear the tables
name|truncateTable
argument_list|(
name|UTIL1
argument_list|,
name|tableName
argument_list|)
expr_stmt|;
name|truncateTable
argument_list|(
name|UTIL2
argument_list|,
name|tableName
argument_list|)
expr_stmt|;
comment|// Replace the peer set up for us by the base class with a wrapper for this test
name|hbaseAdmin
operator|.
name|addReplicationPeer
argument_list|(
literal|"testReplicatorBatching"
argument_list|,
operator|new
name|ReplicationPeerConfig
argument_list|()
operator|.
name|setClusterKey
argument_list|(
name|UTIL2
operator|.
name|getClusterKey
argument_list|()
argument_list|)
operator|.
name|setReplicationEndpointImpl
argument_list|(
name|ReplicationEndpointForTest
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|ReplicationEndpointForTest
operator|.
name|setBatchCount
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|ReplicationEndpointForTest
operator|.
name|setEntriesCount
argument_list|(
literal|0
argument_list|)
expr_stmt|;
try|try
block|{
name|ReplicationEndpointForTest
operator|.
name|pause
argument_list|()
expr_stmt|;
try|try
block|{
comment|// Queue up a bunch of cells of size 8K. Because of RPC size limits, they will all
comment|// have to be replicated separately.
specifier|final
name|byte
index|[]
name|valueBytes
init|=
operator|new
name|byte
index|[
literal|8
operator|*
literal|1024
index|]
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
name|NUM_ROWS
condition|;
name|i
operator|++
control|)
block|{
name|htable1
operator|.
name|put
argument_list|(
operator|new
name|Put
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"row"
operator|+
name|Integer
operator|.
name|toString
argument_list|(
name|i
argument_list|)
argument_list|)
argument_list|)
operator|.
name|addColumn
argument_list|(
name|famName
argument_list|,
literal|null
argument_list|,
name|valueBytes
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
finally|finally
block|{
name|ReplicationEndpointForTest
operator|.
name|resume
argument_list|()
expr_stmt|;
block|}
comment|// Wait for replication to complete.
name|Waiter
operator|.
name|waitFor
argument_list|(
name|CONF1
argument_list|,
literal|60000
argument_list|,
operator|new
name|Waiter
operator|.
name|ExplainingPredicate
argument_list|<
name|Exception
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|boolean
name|evaluate
parameter_list|()
throws|throws
name|Exception
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Count="
operator|+
name|ReplicationEndpointForTest
operator|.
name|getBatchCount
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|ReplicationEndpointForTest
operator|.
name|getBatchCount
argument_list|()
operator|>=
name|NUM_ROWS
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|explainFailure
parameter_list|()
throws|throws
name|Exception
block|{
return|return
literal|"We waited too long for expected replication of "
operator|+
name|NUM_ROWS
operator|+
literal|" entries"
return|;
block|}
block|}
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"We sent an incorrect number of batches"
argument_list|,
name|NUM_ROWS
argument_list|,
name|ReplicationEndpointForTest
operator|.
name|getBatchCount
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"We did not replicate enough rows"
argument_list|,
name|NUM_ROWS
argument_list|,
name|UTIL2
operator|.
name|countRows
argument_list|(
name|htable2
argument_list|)
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|hbaseAdmin
operator|.
name|removeReplicationPeer
argument_list|(
literal|"testReplicatorBatching"
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testReplicatorWithErrors
parameter_list|()
throws|throws
name|Exception
block|{
comment|// Clear the tables
name|truncateTable
argument_list|(
name|UTIL1
argument_list|,
name|tableName
argument_list|)
expr_stmt|;
name|truncateTable
argument_list|(
name|UTIL2
argument_list|,
name|tableName
argument_list|)
expr_stmt|;
comment|// Replace the peer set up for us by the base class with a wrapper for this test
name|hbaseAdmin
operator|.
name|addReplicationPeer
argument_list|(
literal|"testReplicatorWithErrors"
argument_list|,
operator|new
name|ReplicationPeerConfig
argument_list|()
operator|.
name|setClusterKey
argument_list|(
name|UTIL2
operator|.
name|getClusterKey
argument_list|()
argument_list|)
operator|.
name|setReplicationEndpointImpl
argument_list|(
name|FailureInjectingReplicationEndpointForTest
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|FailureInjectingReplicationEndpointForTest
operator|.
name|setBatchCount
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|FailureInjectingReplicationEndpointForTest
operator|.
name|setEntriesCount
argument_list|(
literal|0
argument_list|)
expr_stmt|;
try|try
block|{
name|FailureInjectingReplicationEndpointForTest
operator|.
name|pause
argument_list|()
expr_stmt|;
try|try
block|{
comment|// Queue up a bunch of cells of size 8K. Because of RPC size limits, they will all
comment|// have to be replicated separately.
specifier|final
name|byte
index|[]
name|valueBytes
init|=
operator|new
name|byte
index|[
literal|8
operator|*
literal|1024
index|]
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
name|NUM_ROWS
condition|;
name|i
operator|++
control|)
block|{
name|htable1
operator|.
name|put
argument_list|(
operator|new
name|Put
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"row"
operator|+
name|Integer
operator|.
name|toString
argument_list|(
name|i
argument_list|)
argument_list|)
argument_list|)
operator|.
name|addColumn
argument_list|(
name|famName
argument_list|,
literal|null
argument_list|,
name|valueBytes
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
finally|finally
block|{
name|FailureInjectingReplicationEndpointForTest
operator|.
name|resume
argument_list|()
expr_stmt|;
block|}
comment|// Wait for replication to complete.
comment|// We can expect 10 batches
name|Waiter
operator|.
name|waitFor
argument_list|(
name|CONF1
argument_list|,
literal|60000
argument_list|,
operator|new
name|Waiter
operator|.
name|ExplainingPredicate
argument_list|<
name|Exception
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|boolean
name|evaluate
parameter_list|()
throws|throws
name|Exception
block|{
return|return
name|FailureInjectingReplicationEndpointForTest
operator|.
name|getEntriesCount
argument_list|()
operator|>=
name|NUM_ROWS
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|explainFailure
parameter_list|()
throws|throws
name|Exception
block|{
return|return
literal|"We waited too long for expected replication of "
operator|+
name|NUM_ROWS
operator|+
literal|" entries"
return|;
block|}
block|}
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"We did not replicate enough rows"
argument_list|,
name|NUM_ROWS
argument_list|,
name|UTIL2
operator|.
name|countRows
argument_list|(
name|htable2
argument_list|)
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|hbaseAdmin
operator|.
name|removeReplicationPeer
argument_list|(
literal|"testReplicatorWithErrors"
argument_list|)
expr_stmt|;
block|}
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
name|TestReplicationBase
operator|.
name|tearDownAfterClass
argument_list|()
expr_stmt|;
block|}
specifier|private
name|void
name|truncateTable
parameter_list|(
name|HBaseTestingUtility
name|util
parameter_list|,
name|TableName
name|tablename
parameter_list|)
throws|throws
name|IOException
block|{
name|HBaseAdmin
name|admin
init|=
name|util
operator|.
name|getHBaseAdmin
argument_list|()
decl_stmt|;
name|admin
operator|.
name|disableTable
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
name|admin
operator|.
name|truncateTable
argument_list|(
name|tablename
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
class|class
name|ReplicationEndpointForTest
extends|extends
name|HBaseInterClusterReplicationEndpoint
block|{
specifier|protected
specifier|static
name|AtomicInteger
name|batchCount
init|=
operator|new
name|AtomicInteger
argument_list|(
literal|0
argument_list|)
decl_stmt|;
specifier|protected
specifier|static
name|int
name|entriesCount
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|Object
name|latch
init|=
operator|new
name|Object
argument_list|()
decl_stmt|;
specifier|private
specifier|static
name|AtomicBoolean
name|useLatch
init|=
operator|new
name|AtomicBoolean
argument_list|(
literal|false
argument_list|)
decl_stmt|;
specifier|public
specifier|static
name|void
name|resume
parameter_list|()
block|{
name|useLatch
operator|.
name|set
argument_list|(
literal|false
argument_list|)
expr_stmt|;
synchronized|synchronized
init|(
name|latch
init|)
block|{
name|latch
operator|.
name|notifyAll
argument_list|()
expr_stmt|;
block|}
block|}
specifier|public
specifier|static
name|void
name|pause
parameter_list|()
block|{
name|useLatch
operator|.
name|set
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
name|void
name|await
parameter_list|()
throws|throws
name|InterruptedException
block|{
if|if
condition|(
name|useLatch
operator|.
name|get
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Waiting on latch"
argument_list|)
expr_stmt|;
synchronized|synchronized
init|(
name|latch
init|)
block|{
name|latch
operator|.
name|wait
argument_list|()
expr_stmt|;
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"Waited on latch, now proceeding"
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
specifier|static
name|int
name|getBatchCount
parameter_list|()
block|{
return|return
name|batchCount
operator|.
name|get
argument_list|()
return|;
block|}
specifier|public
specifier|static
name|void
name|setBatchCount
parameter_list|(
name|int
name|i
parameter_list|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"SetBatchCount="
operator|+
name|i
operator|+
literal|", old="
operator|+
name|getBatchCount
argument_list|()
argument_list|)
expr_stmt|;
name|batchCount
operator|.
name|set
argument_list|(
name|i
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
name|int
name|getEntriesCount
parameter_list|()
block|{
return|return
name|entriesCount
return|;
block|}
specifier|public
specifier|static
name|void
name|setEntriesCount
parameter_list|(
name|int
name|i
parameter_list|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"SetEntriesCount="
operator|+
name|i
argument_list|)
expr_stmt|;
name|entriesCount
operator|=
name|i
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|replicate
parameter_list|(
name|ReplicateContext
name|replicateContext
parameter_list|)
block|{
try|try
block|{
name|await
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Interrupted waiting for latch"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
return|return
name|super
operator|.
name|replicate
argument_list|(
name|replicateContext
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|protected
name|Callable
argument_list|<
name|Integer
argument_list|>
name|createReplicator
parameter_list|(
name|List
argument_list|<
name|Entry
argument_list|>
name|entries
parameter_list|,
name|int
name|ordinal
parameter_list|)
block|{
return|return
parameter_list|()
lambda|->
block|{
name|int
name|batchIndex
init|=
name|replicateEntries
argument_list|(
name|entries
argument_list|,
name|ordinal
argument_list|)
decl_stmt|;
name|entriesCount
operator|+=
name|entries
operator|.
name|size
argument_list|()
expr_stmt|;
name|int
name|count
init|=
name|batchCount
operator|.
name|incrementAndGet
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Completed replicating batch "
operator|+
name|System
operator|.
name|identityHashCode
argument_list|(
name|entries
argument_list|)
operator|+
literal|" count="
operator|+
name|count
argument_list|)
expr_stmt|;
return|return
name|batchIndex
return|;
block|}
return|;
block|}
block|}
specifier|public
specifier|static
class|class
name|FailureInjectingReplicationEndpointForTest
extends|extends
name|ReplicationEndpointForTest
block|{
specifier|private
specifier|final
name|AtomicBoolean
name|failNext
init|=
operator|new
name|AtomicBoolean
argument_list|(
literal|false
argument_list|)
decl_stmt|;
annotation|@
name|Override
specifier|protected
name|Callable
argument_list|<
name|Integer
argument_list|>
name|createReplicator
parameter_list|(
name|List
argument_list|<
name|Entry
argument_list|>
name|entries
parameter_list|,
name|int
name|ordinal
parameter_list|)
block|{
return|return
parameter_list|()
lambda|->
block|{
if|if
condition|(
name|failNext
operator|.
name|compareAndSet
argument_list|(
literal|false
argument_list|,
literal|true
argument_list|)
condition|)
block|{
name|int
name|batchIndex
init|=
name|replicateEntries
argument_list|(
name|entries
argument_list|,
name|ordinal
argument_list|)
decl_stmt|;
name|entriesCount
operator|+=
name|entries
operator|.
name|size
argument_list|()
expr_stmt|;
name|int
name|count
init|=
name|batchCount
operator|.
name|incrementAndGet
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Completed replicating batch "
operator|+
name|System
operator|.
name|identityHashCode
argument_list|(
name|entries
argument_list|)
operator|+
literal|" count="
operator|+
name|count
argument_list|)
expr_stmt|;
return|return
name|batchIndex
return|;
block|}
elseif|else
if|if
condition|(
name|failNext
operator|.
name|compareAndSet
argument_list|(
literal|true
argument_list|,
literal|false
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|ServiceException
argument_list|(
literal|"Injected failure"
argument_list|)
throw|;
block|}
return|return
name|ordinal
return|;
block|}
return|;
block|}
block|}
block|}
end_class

end_unit

