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
name|procedure
package|;
end_package

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
name|procedure2
operator|.
name|ProcedureTestingUtility
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
name|procedure2
operator|.
name|ProcedureTestingUtility
operator|.
name|TestProcedure
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
name|procedure2
operator|.
name|store
operator|.
name|ProcedureStore
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
name|procedure2
operator|.
name|store
operator|.
name|wal
operator|.
name|WALProcedureStore
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
name|hdfs
operator|.
name|MiniDFSCluster
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
name|hdfs
operator|.
name|server
operator|.
name|datanode
operator|.
name|DataNode
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
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|fail
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
name|LargeTests
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|TestWALProcedureStoreOnHDFS
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
name|TestWALProcedureStoreOnHDFS
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|protected
specifier|static
specifier|final
name|HBaseTestingUtility
name|UTIL
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
specifier|private
name|WALProcedureStore
name|store
decl_stmt|;
specifier|private
name|ProcedureStore
operator|.
name|ProcedureStoreListener
name|stopProcedureListener
init|=
operator|new
name|ProcedureStore
operator|.
name|ProcedureStoreListener
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|postSync
parameter_list|()
block|{}
annotation|@
name|Override
specifier|public
name|void
name|abortProcess
parameter_list|()
block|{
name|LOG
operator|.
name|fatal
argument_list|(
literal|"Abort the Procedure Store"
argument_list|)
expr_stmt|;
name|store
operator|.
name|stop
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
block|}
decl_stmt|;
specifier|private
specifier|static
name|void
name|initConfig
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
name|conf
operator|.
name|setInt
argument_list|(
literal|"dfs.replication"
argument_list|,
literal|3
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setInt
argument_list|(
literal|"dfs.namenode.replication.min"
argument_list|,
literal|3
argument_list|)
expr_stmt|;
comment|// increase the value for slow test-env
name|conf
operator|.
name|setInt
argument_list|(
literal|"hbase.procedure.store.wal.wait.before.roll"
argument_list|,
literal|1000
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setInt
argument_list|(
literal|"hbase.procedure.store.wal.max.roll.retries"
argument_list|,
literal|10
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setInt
argument_list|(
literal|"hbase.procedure.store.wal.sync.failure.roll.max"
argument_list|,
literal|10
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|setup
parameter_list|()
throws|throws
name|Exception
block|{
name|MiniDFSCluster
name|dfs
init|=
name|UTIL
operator|.
name|startMiniDFSCluster
argument_list|(
literal|3
argument_list|)
decl_stmt|;
name|Path
name|logDir
init|=
operator|new
name|Path
argument_list|(
operator|new
name|Path
argument_list|(
name|dfs
operator|.
name|getFileSystem
argument_list|()
operator|.
name|getUri
argument_list|()
argument_list|)
argument_list|,
literal|"/test-logs"
argument_list|)
decl_stmt|;
name|store
operator|=
name|ProcedureTestingUtility
operator|.
name|createWalStore
argument_list|(
name|UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|dfs
operator|.
name|getFileSystem
argument_list|()
argument_list|,
name|logDir
argument_list|)
expr_stmt|;
name|store
operator|.
name|registerListener
argument_list|(
name|stopProcedureListener
argument_list|)
expr_stmt|;
name|store
operator|.
name|start
argument_list|(
literal|8
argument_list|)
expr_stmt|;
name|store
operator|.
name|recoverLease
argument_list|()
expr_stmt|;
block|}
specifier|public
name|void
name|tearDown
parameter_list|()
throws|throws
name|Exception
block|{
name|store
operator|.
name|stop
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|UTIL
operator|.
name|getDFSCluster
argument_list|()
operator|.
name|getFileSystem
argument_list|()
operator|.
name|delete
argument_list|(
name|store
operator|.
name|getLogDir
argument_list|()
argument_list|,
literal|true
argument_list|)
expr_stmt|;
try|try
block|{
name|UTIL
operator|.
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"failure shutting down cluster"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|60000
argument_list|,
name|expected
operator|=
name|RuntimeException
operator|.
name|class
argument_list|)
specifier|public
name|void
name|testWalAbortOnLowReplication
parameter_list|()
throws|throws
name|Exception
block|{
name|initConfig
argument_list|(
name|UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
expr_stmt|;
name|setup
argument_list|()
expr_stmt|;
try|try
block|{
name|assertEquals
argument_list|(
literal|3
argument_list|,
name|UTIL
operator|.
name|getDFSCluster
argument_list|()
operator|.
name|getDataNodes
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Stop DataNode"
argument_list|)
expr_stmt|;
name|UTIL
operator|.
name|getDFSCluster
argument_list|()
operator|.
name|stopDataNode
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|UTIL
operator|.
name|getDFSCluster
argument_list|()
operator|.
name|getDataNodes
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|store
operator|.
name|insert
argument_list|(
operator|new
name|TestProcedure
argument_list|(
literal|1
argument_list|,
operator|-
literal|1
argument_list|)
argument_list|,
literal|null
argument_list|)
expr_stmt|;
for|for
control|(
name|long
name|i
init|=
literal|2
init|;
name|store
operator|.
name|isRunning
argument_list|()
condition|;
operator|++
name|i
control|)
block|{
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|UTIL
operator|.
name|getDFSCluster
argument_list|()
operator|.
name|getDataNodes
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|store
operator|.
name|insert
argument_list|(
operator|new
name|TestProcedure
argument_list|(
name|i
argument_list|,
operator|-
literal|1
argument_list|)
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|Thread
operator|.
name|sleep
argument_list|(
literal|100
argument_list|)
expr_stmt|;
block|}
name|assertFalse
argument_list|(
name|store
operator|.
name|isRunning
argument_list|()
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"The store.insert() should throw an exeption"
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|tearDown
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|60000
argument_list|)
specifier|public
name|void
name|testWalAbortOnLowReplicationWithQueuedWriters
parameter_list|()
throws|throws
name|Exception
block|{
name|initConfig
argument_list|(
name|UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
expr_stmt|;
name|setup
argument_list|()
expr_stmt|;
try|try
block|{
name|assertEquals
argument_list|(
literal|3
argument_list|,
name|UTIL
operator|.
name|getDFSCluster
argument_list|()
operator|.
name|getDataNodes
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|store
operator|.
name|registerListener
argument_list|(
operator|new
name|ProcedureStore
operator|.
name|ProcedureStoreListener
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|postSync
parameter_list|()
block|{
name|Threads
operator|.
name|sleepWithoutInterrupt
argument_list|(
literal|2000
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|abortProcess
parameter_list|()
block|{}
block|}
argument_list|)
expr_stmt|;
specifier|final
name|AtomicInteger
name|reCount
init|=
operator|new
name|AtomicInteger
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|Thread
index|[]
name|thread
init|=
operator|new
name|Thread
index|[
name|store
operator|.
name|getNumThreads
argument_list|()
operator|*
literal|2
operator|+
literal|1
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
name|thread
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
specifier|final
name|long
name|procId
init|=
name|i
operator|+
literal|1
decl_stmt|;
name|thread
index|[
name|i
index|]
operator|=
operator|new
name|Thread
argument_list|()
block|{
specifier|public
name|void
name|run
parameter_list|()
block|{
try|try
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"[S] INSERT "
operator|+
name|procId
argument_list|)
expr_stmt|;
name|store
operator|.
name|insert
argument_list|(
operator|new
name|TestProcedure
argument_list|(
name|procId
argument_list|,
operator|-
literal|1
argument_list|)
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"[E] INSERT "
operator|+
name|procId
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|RuntimeException
name|e
parameter_list|)
block|{
name|reCount
operator|.
name|incrementAndGet
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"[F] INSERT "
operator|+
name|procId
operator|+
literal|": "
operator|+
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
expr_stmt|;
name|thread
index|[
name|i
index|]
operator|.
name|start
argument_list|()
expr_stmt|;
block|}
name|Thread
operator|.
name|sleep
argument_list|(
literal|1000
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Stop DataNode"
argument_list|)
expr_stmt|;
name|UTIL
operator|.
name|getDFSCluster
argument_list|()
operator|.
name|stopDataNode
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|UTIL
operator|.
name|getDFSCluster
argument_list|()
operator|.
name|getDataNodes
argument_list|()
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
name|thread
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
name|thread
index|[
name|i
index|]
operator|.
name|join
argument_list|()
expr_stmt|;
block|}
name|assertFalse
argument_list|(
name|store
operator|.
name|isRunning
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|reCount
operator|.
name|toString
argument_list|()
argument_list|,
name|reCount
operator|.
name|get
argument_list|()
operator|>=
name|store
operator|.
name|getNumThreads
argument_list|()
operator|&&
name|reCount
operator|.
name|get
argument_list|()
operator|<
name|thread
operator|.
name|length
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|tearDown
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|60000
argument_list|)
specifier|public
name|void
name|testWalRollOnLowReplication
parameter_list|()
throws|throws
name|Exception
block|{
name|initConfig
argument_list|(
name|UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
expr_stmt|;
name|UTIL
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setInt
argument_list|(
literal|"dfs.namenode.replication.min"
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|setup
argument_list|()
expr_stmt|;
try|try
block|{
name|int
name|dnCount
init|=
literal|0
decl_stmt|;
name|store
operator|.
name|insert
argument_list|(
operator|new
name|TestProcedure
argument_list|(
literal|1
argument_list|,
operator|-
literal|1
argument_list|)
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|UTIL
operator|.
name|getDFSCluster
argument_list|()
operator|.
name|restartDataNode
argument_list|(
name|dnCount
argument_list|)
expr_stmt|;
for|for
control|(
name|long
name|i
init|=
literal|2
init|;
name|i
operator|<
literal|100
condition|;
operator|++
name|i
control|)
block|{
name|store
operator|.
name|insert
argument_list|(
operator|new
name|TestProcedure
argument_list|(
name|i
argument_list|,
operator|-
literal|1
argument_list|)
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|waitForNumReplicas
argument_list|(
literal|3
argument_list|)
expr_stmt|;
name|Thread
operator|.
name|sleep
argument_list|(
literal|100
argument_list|)
expr_stmt|;
if|if
condition|(
operator|(
name|i
operator|%
literal|30
operator|)
operator|==
literal|0
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Restart Data Node"
argument_list|)
expr_stmt|;
name|UTIL
operator|.
name|getDFSCluster
argument_list|()
operator|.
name|restartDataNode
argument_list|(
operator|++
name|dnCount
operator|%
literal|3
argument_list|)
expr_stmt|;
block|}
block|}
name|assertTrue
argument_list|(
name|store
operator|.
name|isRunning
argument_list|()
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|tearDown
argument_list|()
expr_stmt|;
block|}
block|}
specifier|public
name|void
name|waitForNumReplicas
parameter_list|(
name|int
name|numReplicas
parameter_list|)
throws|throws
name|Exception
block|{
while|while
condition|(
name|UTIL
operator|.
name|getDFSCluster
argument_list|()
operator|.
name|getDataNodes
argument_list|()
operator|.
name|size
argument_list|()
operator|<
name|numReplicas
condition|)
block|{
name|Thread
operator|.
name|sleep
argument_list|(
literal|100
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
name|numReplicas
condition|;
operator|++
name|i
control|)
block|{
for|for
control|(
name|DataNode
name|dn
range|:
name|UTIL
operator|.
name|getDFSCluster
argument_list|()
operator|.
name|getDataNodes
argument_list|()
control|)
block|{
while|while
condition|(
operator|!
name|dn
operator|.
name|isDatanodeFullyStarted
argument_list|()
condition|)
block|{
name|Thread
operator|.
name|sleep
argument_list|(
literal|100
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
block|}
end_class

end_unit

