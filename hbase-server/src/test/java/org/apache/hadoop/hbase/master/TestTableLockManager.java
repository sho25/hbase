begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Copyright The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|CountDownLatch
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
name|ExecutionException
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
name|ExecutorService
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
name|Executors
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
name|Future
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
name|LockTimeoutException
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
name|TableNotDisabledException
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
name|coprocessor
operator|.
name|BaseMasterObserver
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
name|coprocessor
operator|.
name|MasterCoprocessorEnvironment
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
name|coprocessor
operator|.
name|ObserverContext
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
comment|/**  * Tests the default table lock manager  */
end_comment

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
name|TestTableLockManager
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
name|TestTableLockManager
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|TABLE_NAME
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"TestTableLevelLocks"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|FAMILY
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"f1"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|NEW_FAMILY
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"f2"
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|HBaseTestingUtility
name|TEST_UTIL
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|CountDownLatch
name|deleteColumn
init|=
operator|new
name|CountDownLatch
argument_list|(
literal|1
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|CountDownLatch
name|addColumn
init|=
operator|new
name|CountDownLatch
argument_list|(
literal|1
argument_list|)
decl_stmt|;
specifier|public
name|void
name|prepareMiniCluster
parameter_list|()
throws|throws
name|Exception
block|{
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setBoolean
argument_list|(
literal|"hbase.online.schema.update.enable"
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|startMiniCluster
argument_list|(
literal|2
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|createTable
argument_list|(
name|TABLE_NAME
argument_list|,
name|FAMILY
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|prepareMiniZkCluster
parameter_list|()
throws|throws
name|Exception
block|{
name|TEST_UTIL
operator|.
name|startMiniZKCluster
argument_list|(
literal|1
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
name|TEST_UTIL
operator|.
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|600000
argument_list|)
specifier|public
name|void
name|testLockTimeoutException
parameter_list|()
throws|throws
name|Exception
block|{
name|Configuration
name|conf
init|=
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
name|conf
operator|.
name|setInt
argument_list|(
name|TableLockManager
operator|.
name|TABLE_WRITE_LOCK_TIMEOUT_MS
argument_list|,
literal|3000
argument_list|)
expr_stmt|;
name|prepareMiniCluster
argument_list|()
expr_stmt|;
name|HMaster
name|master
init|=
name|TEST_UTIL
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|getMaster
argument_list|()
decl_stmt|;
name|master
operator|.
name|getCoprocessorHost
argument_list|()
operator|.
name|load
argument_list|(
name|TestLockTimeoutExceptionMasterObserver
operator|.
name|class
argument_list|,
literal|0
argument_list|,
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
expr_stmt|;
name|ExecutorService
name|executor
init|=
name|Executors
operator|.
name|newSingleThreadExecutor
argument_list|()
decl_stmt|;
name|Future
argument_list|<
name|Object
argument_list|>
name|shouldFinish
init|=
name|executor
operator|.
name|submit
argument_list|(
operator|new
name|Callable
argument_list|<
name|Object
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Object
name|call
parameter_list|()
throws|throws
name|Exception
block|{
name|HBaseAdmin
name|admin
init|=
name|TEST_UTIL
operator|.
name|getHBaseAdmin
argument_list|()
decl_stmt|;
name|admin
operator|.
name|deleteColumn
argument_list|(
name|TABLE_NAME
argument_list|,
name|FAMILY
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
block|}
argument_list|)
decl_stmt|;
name|deleteColumn
operator|.
name|await
argument_list|()
expr_stmt|;
try|try
block|{
name|HBaseAdmin
name|admin
init|=
name|TEST_UTIL
operator|.
name|getHBaseAdmin
argument_list|()
decl_stmt|;
name|admin
operator|.
name|addColumn
argument_list|(
name|TABLE_NAME
argument_list|,
operator|new
name|HColumnDescriptor
argument_list|(
name|NEW_FAMILY
argument_list|)
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"Was expecting TableLockTimeoutException"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|LockTimeoutException
name|ex
parameter_list|)
block|{
comment|//expected
block|}
name|shouldFinish
operator|.
name|get
argument_list|()
expr_stmt|;
block|}
specifier|public
specifier|static
class|class
name|TestLockTimeoutExceptionMasterObserver
extends|extends
name|BaseMasterObserver
block|{
annotation|@
name|Override
specifier|public
name|void
name|preDeleteColumnHandler
parameter_list|(
name|ObserverContext
argument_list|<
name|MasterCoprocessorEnvironment
argument_list|>
name|ctx
parameter_list|,
name|byte
index|[]
name|tableName
parameter_list|,
name|byte
index|[]
name|c
parameter_list|)
throws|throws
name|IOException
block|{
name|deleteColumn
operator|.
name|countDown
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|postDeleteColumnHandler
parameter_list|(
name|ObserverContext
argument_list|<
name|MasterCoprocessorEnvironment
argument_list|>
name|ctx
parameter_list|,
name|byte
index|[]
name|tableName
parameter_list|,
name|byte
index|[]
name|c
parameter_list|)
throws|throws
name|IOException
block|{
name|Threads
operator|.
name|sleep
argument_list|(
literal|10000
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|preAddColumnHandler
parameter_list|(
name|ObserverContext
argument_list|<
name|MasterCoprocessorEnvironment
argument_list|>
name|ctx
parameter_list|,
name|byte
index|[]
name|tableName
parameter_list|,
name|HColumnDescriptor
name|column
parameter_list|)
throws|throws
name|IOException
block|{
name|fail
argument_list|(
literal|"Add column should have timeouted out for acquiring the table lock"
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|600000
argument_list|)
specifier|public
name|void
name|testAlterAndDisable
parameter_list|()
throws|throws
name|Exception
block|{
name|prepareMiniCluster
argument_list|()
expr_stmt|;
comment|// Send a request to alter a table, then sleep during
comment|// the alteration phase. In the mean time, from another
comment|// thread, send a request to disable, and then delete a table.
name|HMaster
name|master
init|=
name|TEST_UTIL
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|getMaster
argument_list|()
decl_stmt|;
name|master
operator|.
name|getCoprocessorHost
argument_list|()
operator|.
name|load
argument_list|(
name|TestAlterAndDisableMasterObserver
operator|.
name|class
argument_list|,
literal|0
argument_list|,
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
expr_stmt|;
name|ExecutorService
name|executor
init|=
name|Executors
operator|.
name|newFixedThreadPool
argument_list|(
literal|2
argument_list|)
decl_stmt|;
name|Future
argument_list|<
name|Object
argument_list|>
name|alterTableFuture
init|=
name|executor
operator|.
name|submit
argument_list|(
operator|new
name|Callable
argument_list|<
name|Object
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Object
name|call
parameter_list|()
throws|throws
name|Exception
block|{
name|HBaseAdmin
name|admin
init|=
name|TEST_UTIL
operator|.
name|getHBaseAdmin
argument_list|()
decl_stmt|;
name|admin
operator|.
name|addColumn
argument_list|(
name|TABLE_NAME
argument_list|,
operator|new
name|HColumnDescriptor
argument_list|(
name|NEW_FAMILY
argument_list|)
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Added new column family"
argument_list|)
expr_stmt|;
name|HTableDescriptor
name|tableDesc
init|=
name|admin
operator|.
name|getTableDescriptor
argument_list|(
name|TABLE_NAME
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|tableDesc
operator|.
name|getFamiliesKeys
argument_list|()
operator|.
name|contains
argument_list|(
name|NEW_FAMILY
argument_list|)
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
block|}
argument_list|)
decl_stmt|;
name|Future
argument_list|<
name|Object
argument_list|>
name|disableTableFuture
init|=
name|executor
operator|.
name|submit
argument_list|(
operator|new
name|Callable
argument_list|<
name|Object
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Object
name|call
parameter_list|()
throws|throws
name|Exception
block|{
name|HBaseAdmin
name|admin
init|=
name|TEST_UTIL
operator|.
name|getHBaseAdmin
argument_list|()
decl_stmt|;
name|admin
operator|.
name|disableTable
argument_list|(
name|TABLE_NAME
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|admin
operator|.
name|isTableDisabled
argument_list|(
name|TABLE_NAME
argument_list|)
argument_list|)
expr_stmt|;
name|admin
operator|.
name|deleteTable
argument_list|(
name|TABLE_NAME
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|admin
operator|.
name|tableExists
argument_list|(
name|TABLE_NAME
argument_list|)
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
block|}
argument_list|)
decl_stmt|;
try|try
block|{
name|disableTableFuture
operator|.
name|get
argument_list|()
expr_stmt|;
name|alterTableFuture
operator|.
name|get
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ExecutionException
name|e
parameter_list|)
block|{
if|if
condition|(
name|e
operator|.
name|getCause
argument_list|()
operator|instanceof
name|AssertionError
condition|)
block|{
throw|throw
operator|(
name|AssertionError
operator|)
name|e
operator|.
name|getCause
argument_list|()
throw|;
block|}
throw|throw
name|e
throw|;
block|}
block|}
specifier|public
specifier|static
class|class
name|TestAlterAndDisableMasterObserver
extends|extends
name|BaseMasterObserver
block|{
annotation|@
name|Override
specifier|public
name|void
name|preAddColumnHandler
parameter_list|(
name|ObserverContext
argument_list|<
name|MasterCoprocessorEnvironment
argument_list|>
name|ctx
parameter_list|,
name|byte
index|[]
name|tableName
parameter_list|,
name|HColumnDescriptor
name|column
parameter_list|)
throws|throws
name|IOException
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"addColumn called"
argument_list|)
expr_stmt|;
name|addColumn
operator|.
name|countDown
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|postAddColumnHandler
parameter_list|(
name|ObserverContext
argument_list|<
name|MasterCoprocessorEnvironment
argument_list|>
name|ctx
parameter_list|,
name|byte
index|[]
name|tableName
parameter_list|,
name|HColumnDescriptor
name|column
parameter_list|)
throws|throws
name|IOException
block|{
name|Threads
operator|.
name|sleep
argument_list|(
literal|6000
argument_list|)
expr_stmt|;
try|try
block|{
name|ctx
operator|.
name|getEnvironment
argument_list|()
operator|.
name|getMasterServices
argument_list|()
operator|.
name|checkTableModifiable
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|TableNotDisabledException
name|expected
parameter_list|)
block|{
comment|//pass
return|return;
block|}
catch|catch
parameter_list|(
name|IOException
name|ex
parameter_list|)
block|{       }
name|fail
argument_list|(
literal|"was expecting the table to be enabled"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|preDisableTable
parameter_list|(
name|ObserverContext
argument_list|<
name|MasterCoprocessorEnvironment
argument_list|>
name|ctx
parameter_list|,
name|byte
index|[]
name|tableName
parameter_list|)
throws|throws
name|IOException
block|{
try|try
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Waiting for addColumn to be processed first"
argument_list|)
expr_stmt|;
comment|//wait for addColumn to be processed first
name|addColumn
operator|.
name|await
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"addColumn started, we can continue"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|ex
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Sleep interrupted while waiting for addColumn countdown"
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|postDisableTableHandler
parameter_list|(
name|ObserverContext
argument_list|<
name|MasterCoprocessorEnvironment
argument_list|>
name|ctx
parameter_list|,
name|byte
index|[]
name|tableName
parameter_list|)
throws|throws
name|IOException
block|{
name|Threads
operator|.
name|sleep
argument_list|(
literal|3000
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|600000
argument_list|)
specifier|public
name|void
name|testDelete
parameter_list|()
throws|throws
name|Exception
block|{
name|prepareMiniCluster
argument_list|()
expr_stmt|;
name|HBaseAdmin
name|admin
init|=
name|TEST_UTIL
operator|.
name|getHBaseAdmin
argument_list|()
decl_stmt|;
name|admin
operator|.
name|disableTable
argument_list|(
name|TABLE_NAME
argument_list|)
expr_stmt|;
name|admin
operator|.
name|deleteTable
argument_list|(
name|TABLE_NAME
argument_list|)
expr_stmt|;
comment|//ensure that znode for the table node has been deleted
name|ZooKeeperWatcher
name|zkWatcher
init|=
name|TEST_UTIL
operator|.
name|getZooKeeperWatcher
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
name|ZKUtil
operator|.
name|checkExists
argument_list|(
name|zkWatcher
argument_list|,
name|ZKUtil
operator|.
name|joinZNode
argument_list|(
name|zkWatcher
operator|.
name|tableLockZNode
argument_list|,
name|Bytes
operator|.
name|toString
argument_list|(
name|TABLE_NAME
argument_list|)
argument_list|)
argument_list|)
operator|<
literal|0
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|600000
argument_list|)
specifier|public
name|void
name|testReapAllTableLocks
parameter_list|()
throws|throws
name|Exception
block|{
name|prepareMiniZkCluster
argument_list|()
expr_stmt|;
name|ServerName
name|serverName
init|=
operator|new
name|ServerName
argument_list|(
literal|"localhost:10000"
argument_list|,
literal|0
argument_list|)
decl_stmt|;
specifier|final
name|TableLockManager
name|lockManager
init|=
name|TableLockManager
operator|.
name|createTableLockManager
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|TEST_UTIL
operator|.
name|getZooKeeperWatcher
argument_list|()
argument_list|,
name|serverName
argument_list|)
decl_stmt|;
name|String
name|tables
index|[]
init|=
block|{
literal|"table1"
block|,
literal|"table2"
block|,
literal|"table3"
block|,
literal|"table4"
block|}
decl_stmt|;
name|ExecutorService
name|executor
init|=
name|Executors
operator|.
name|newFixedThreadPool
argument_list|(
literal|6
argument_list|)
decl_stmt|;
specifier|final
name|CountDownLatch
name|writeLocksObtained
init|=
operator|new
name|CountDownLatch
argument_list|(
literal|4
argument_list|)
decl_stmt|;
specifier|final
name|CountDownLatch
name|writeLocksAttempted
init|=
operator|new
name|CountDownLatch
argument_list|(
literal|10
argument_list|)
decl_stmt|;
comment|//TODO: read lock tables
comment|//6 threads will be stuck waiting for the table lock
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|tables
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
specifier|final
name|String
name|table
init|=
name|tables
index|[
name|i
index|]
decl_stmt|;
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
name|i
operator|+
literal|1
condition|;
name|j
operator|++
control|)
block|{
comment|//i+1 write locks attempted for table[i]
name|executor
operator|.
name|submit
argument_list|(
operator|new
name|Callable
argument_list|<
name|Void
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Void
name|call
parameter_list|()
throws|throws
name|Exception
block|{
name|writeLocksAttempted
operator|.
name|countDown
argument_list|()
expr_stmt|;
name|lockManager
operator|.
name|writeLock
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|table
argument_list|)
argument_list|,
literal|"testReapAllTableLocks"
argument_list|)
operator|.
name|acquire
argument_list|()
expr_stmt|;
name|writeLocksObtained
operator|.
name|countDown
argument_list|()
expr_stmt|;
return|return
literal|null
return|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
block|}
name|writeLocksObtained
operator|.
name|await
argument_list|()
expr_stmt|;
name|writeLocksAttempted
operator|.
name|await
argument_list|()
expr_stmt|;
comment|//now reap all table locks
name|lockManager
operator|.
name|reapAllTableWriteLocks
argument_list|()
expr_stmt|;
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setInt
argument_list|(
name|TableLockManager
operator|.
name|TABLE_WRITE_LOCK_TIMEOUT_MS
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|TableLockManager
name|zeroTimeoutLockManager
init|=
name|TableLockManager
operator|.
name|createTableLockManager
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|TEST_UTIL
operator|.
name|getZooKeeperWatcher
argument_list|()
argument_list|,
name|serverName
argument_list|)
decl_stmt|;
comment|//should not throw table lock timeout exception
name|zeroTimeoutLockManager
operator|.
name|writeLock
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|tables
index|[
name|tables
operator|.
name|length
operator|-
literal|1
index|]
argument_list|)
argument_list|,
literal|"zero timeout"
argument_list|)
operator|.
name|acquire
argument_list|()
expr_stmt|;
name|executor
operator|.
name|shutdownNow
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

