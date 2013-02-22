begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|zookeeper
operator|.
name|lock
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
name|io
operator|.
name|IOException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|InterruptedIOException
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
name|DaemonThreadFactory
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
name|InterProcessLock
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
name|InterProcessLock
operator|.
name|MetadataHandler
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
name|MultithreadedTestUtil
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
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Lists
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
name|TestZKInterProcessReadWriteLock
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
name|TestZKInterProcessReadWriteLock
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
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
name|int
name|NUM_THREADS
init|=
literal|10
decl_stmt|;
specifier|private
specifier|static
name|Configuration
name|conf
decl_stmt|;
specifier|private
specifier|final
name|AtomicBoolean
name|isLockHeld
init|=
operator|new
name|AtomicBoolean
argument_list|(
literal|false
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|ExecutorService
name|executor
init|=
name|Executors
operator|.
name|newFixedThreadPool
argument_list|(
name|NUM_THREADS
argument_list|,
operator|new
name|DaemonThreadFactory
argument_list|(
literal|"TestZKInterProcessReadWriteLock-"
argument_list|)
argument_list|)
decl_stmt|;
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|beforeAllTests
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
name|TEST_UTIL
operator|.
name|startMiniZKCluster
argument_list|()
expr_stmt|;
name|conf
operator|.
name|setInt
argument_list|(
name|HConstants
operator|.
name|ZK_SESSION_TIMEOUT
argument_list|,
literal|1000
argument_list|)
expr_stmt|;
name|ZooKeeperWatcher
name|zkw
init|=
name|getZooKeeperWatcher
argument_list|(
literal|"setup"
argument_list|)
decl_stmt|;
name|ZKUtil
operator|.
name|createWithParents
argument_list|(
name|zkw
argument_list|,
name|zkw
operator|.
name|tableLockZNode
argument_list|)
expr_stmt|;
block|}
annotation|@
name|AfterClass
specifier|public
specifier|static
name|void
name|afterAllTests
parameter_list|()
throws|throws
name|Exception
block|{
name|TEST_UTIL
operator|.
name|shutdownMiniZKCluster
argument_list|()
expr_stmt|;
block|}
annotation|@
name|After
specifier|public
name|void
name|tearDown
parameter_list|()
block|{
name|executor
operator|.
name|shutdown
argument_list|()
expr_stmt|;
block|}
specifier|private
specifier|static
name|ZooKeeperWatcher
name|getZooKeeperWatcher
parameter_list|(
name|String
name|desc
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|TEST_UTIL
operator|.
name|getZooKeeperWatcher
argument_list|()
return|;
block|}
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|30000
argument_list|)
specifier|public
name|void
name|testWriteLockExcludesWriters
parameter_list|()
throws|throws
name|Exception
block|{
specifier|final
name|String
name|testName
init|=
literal|"testWriteLockExcludesWriters"
decl_stmt|;
specifier|final
name|ZKInterProcessReadWriteLock
name|readWriteLock
init|=
name|getReadWriteLock
argument_list|(
name|testName
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Future
argument_list|<
name|Void
argument_list|>
argument_list|>
name|results
init|=
name|Lists
operator|.
name|newArrayList
argument_list|()
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
name|NUM_THREADS
condition|;
operator|++
name|i
control|)
block|{
specifier|final
name|String
name|threadDesc
init|=
name|testName
operator|+
name|i
decl_stmt|;
name|results
operator|.
name|add
argument_list|(
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
name|IOException
block|{
name|ZKInterProcessWriteLock
name|writeLock
init|=
name|readWriteLock
operator|.
name|writeLock
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|threadDesc
argument_list|)
argument_list|)
decl_stmt|;
try|try
block|{
name|writeLock
operator|.
name|acquire
argument_list|()
expr_stmt|;
try|try
block|{
comment|// No one else should hold the lock
name|assertTrue
argument_list|(
name|isLockHeld
operator|.
name|compareAndSet
argument_list|(
literal|false
argument_list|,
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|Thread
operator|.
name|sleep
argument_list|(
literal|1000
argument_list|)
expr_stmt|;
comment|// No one else should have released the lock
name|assertTrue
argument_list|(
name|isLockHeld
operator|.
name|compareAndSet
argument_list|(
literal|true
argument_list|,
literal|false
argument_list|)
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|isLockHeld
operator|.
name|set
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|writeLock
operator|.
name|release
argument_list|()
expr_stmt|;
block|}
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
name|threadDesc
operator|+
literal|" interrupted"
argument_list|,
name|e
argument_list|)
expr_stmt|;
name|Thread
operator|.
name|currentThread
argument_list|()
operator|.
name|interrupt
argument_list|()
expr_stmt|;
throw|throw
operator|new
name|InterruptedIOException
argument_list|()
throw|;
block|}
return|return
literal|null
return|;
block|}
block|}
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|MultithreadedTestUtil
operator|.
name|assertOnFutures
argument_list|(
name|results
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|30000
argument_list|)
specifier|public
name|void
name|testReadLockDoesNotExcludeReaders
parameter_list|()
throws|throws
name|Exception
block|{
specifier|final
name|String
name|testName
init|=
literal|"testReadLockDoesNotExcludeReaders"
decl_stmt|;
specifier|final
name|ZKInterProcessReadWriteLock
name|readWriteLock
init|=
name|getReadWriteLock
argument_list|(
name|testName
argument_list|)
decl_stmt|;
specifier|final
name|CountDownLatch
name|locksAcquiredLatch
init|=
operator|new
name|CountDownLatch
argument_list|(
name|NUM_THREADS
argument_list|)
decl_stmt|;
specifier|final
name|AtomicInteger
name|locksHeld
init|=
operator|new
name|AtomicInteger
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Future
argument_list|<
name|Void
argument_list|>
argument_list|>
name|results
init|=
name|Lists
operator|.
name|newArrayList
argument_list|()
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
name|NUM_THREADS
condition|;
operator|++
name|i
control|)
block|{
specifier|final
name|String
name|threadDesc
init|=
name|testName
operator|+
name|i
decl_stmt|;
name|results
operator|.
name|add
argument_list|(
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
name|ZKInterProcessReadLock
name|readLock
init|=
name|readWriteLock
operator|.
name|readLock
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|threadDesc
argument_list|)
argument_list|)
decl_stmt|;
name|readLock
operator|.
name|acquire
argument_list|()
expr_stmt|;
try|try
block|{
name|locksHeld
operator|.
name|incrementAndGet
argument_list|()
expr_stmt|;
name|locksAcquiredLatch
operator|.
name|countDown
argument_list|()
expr_stmt|;
name|Thread
operator|.
name|sleep
argument_list|(
literal|1000
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|readLock
operator|.
name|release
argument_list|()
expr_stmt|;
name|locksHeld
operator|.
name|decrementAndGet
argument_list|()
expr_stmt|;
block|}
return|return
literal|null
return|;
block|}
block|}
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|locksAcquiredLatch
operator|.
name|await
argument_list|()
expr_stmt|;
name|assertEquals
argument_list|(
name|locksHeld
operator|.
name|get
argument_list|()
argument_list|,
name|NUM_THREADS
argument_list|)
expr_stmt|;
name|MultithreadedTestUtil
operator|.
name|assertOnFutures
argument_list|(
name|results
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|3000
argument_list|)
specifier|public
name|void
name|testReadLockExcludesWriters
parameter_list|()
throws|throws
name|Exception
block|{
comment|// Submit a read lock request first
comment|// Submit a write lock request second
specifier|final
name|String
name|testName
init|=
literal|"testReadLockExcludesWriters"
decl_stmt|;
name|List
argument_list|<
name|Future
argument_list|<
name|Void
argument_list|>
argument_list|>
name|results
init|=
name|Lists
operator|.
name|newArrayList
argument_list|()
decl_stmt|;
specifier|final
name|CountDownLatch
name|readLockAcquiredLatch
init|=
operator|new
name|CountDownLatch
argument_list|(
literal|1
argument_list|)
decl_stmt|;
name|Callable
argument_list|<
name|Void
argument_list|>
name|acquireReadLock
init|=
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
specifier|final
name|String
name|threadDesc
init|=
name|testName
operator|+
literal|"-acquireReadLock"
decl_stmt|;
name|ZKInterProcessReadLock
name|readLock
init|=
name|getReadWriteLock
argument_list|(
name|testName
argument_list|)
operator|.
name|readLock
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|threadDesc
argument_list|)
argument_list|)
decl_stmt|;
name|readLock
operator|.
name|acquire
argument_list|()
expr_stmt|;
try|try
block|{
name|assertTrue
argument_list|(
name|isLockHeld
operator|.
name|compareAndSet
argument_list|(
literal|false
argument_list|,
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|readLockAcquiredLatch
operator|.
name|countDown
argument_list|()
expr_stmt|;
name|Thread
operator|.
name|sleep
argument_list|(
literal|1000
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|isLockHeld
operator|.
name|set
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|readLock
operator|.
name|release
argument_list|()
expr_stmt|;
block|}
return|return
literal|null
return|;
block|}
block|}
decl_stmt|;
name|Callable
argument_list|<
name|Void
argument_list|>
name|acquireWriteLock
init|=
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
specifier|final
name|String
name|threadDesc
init|=
name|testName
operator|+
literal|"-acquireWriteLock"
decl_stmt|;
name|ZKInterProcessWriteLock
name|writeLock
init|=
name|getReadWriteLock
argument_list|(
name|testName
argument_list|)
operator|.
name|writeLock
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|threadDesc
argument_list|)
argument_list|)
decl_stmt|;
name|readLockAcquiredLatch
operator|.
name|await
argument_list|()
expr_stmt|;
name|assertTrue
argument_list|(
name|isLockHeld
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
name|writeLock
operator|.
name|acquire
argument_list|()
expr_stmt|;
try|try
block|{
name|assertFalse
argument_list|(
name|isLockHeld
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|writeLock
operator|.
name|release
argument_list|()
expr_stmt|;
block|}
return|return
literal|null
return|;
block|}
block|}
decl_stmt|;
name|results
operator|.
name|add
argument_list|(
name|executor
operator|.
name|submit
argument_list|(
name|acquireReadLock
argument_list|)
argument_list|)
expr_stmt|;
name|results
operator|.
name|add
argument_list|(
name|executor
operator|.
name|submit
argument_list|(
name|acquireWriteLock
argument_list|)
argument_list|)
expr_stmt|;
name|MultithreadedTestUtil
operator|.
name|assertOnFutures
argument_list|(
name|results
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|static
name|ZKInterProcessReadWriteLock
name|getReadWriteLock
parameter_list|(
name|String
name|testName
parameter_list|)
throws|throws
name|IOException
block|{
name|MetadataHandler
name|handler
init|=
operator|new
name|MetadataHandler
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|handleMetadata
parameter_list|(
name|byte
index|[]
name|ownerMetadata
parameter_list|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Lock info: "
operator|+
name|Bytes
operator|.
name|toString
argument_list|(
name|ownerMetadata
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
decl_stmt|;
name|ZooKeeperWatcher
name|zkWatcher
init|=
name|getZooKeeperWatcher
argument_list|(
name|testName
argument_list|)
decl_stmt|;
name|String
name|znode
init|=
name|ZKUtil
operator|.
name|joinZNode
argument_list|(
name|zkWatcher
operator|.
name|tableLockZNode
argument_list|,
name|testName
argument_list|)
decl_stmt|;
return|return
operator|new
name|ZKInterProcessReadWriteLock
argument_list|(
name|zkWatcher
argument_list|,
name|znode
argument_list|,
name|handler
argument_list|)
return|;
block|}
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|30000
argument_list|)
specifier|public
name|void
name|testWriteLockExcludesReaders
parameter_list|()
throws|throws
name|Exception
block|{
comment|// Submit a read lock request first
comment|// Submit a write lock request second
specifier|final
name|String
name|testName
init|=
literal|"testReadLockExcludesWriters"
decl_stmt|;
name|List
argument_list|<
name|Future
argument_list|<
name|Void
argument_list|>
argument_list|>
name|results
init|=
name|Lists
operator|.
name|newArrayList
argument_list|()
decl_stmt|;
specifier|final
name|CountDownLatch
name|writeLockAcquiredLatch
init|=
operator|new
name|CountDownLatch
argument_list|(
literal|1
argument_list|)
decl_stmt|;
name|Callable
argument_list|<
name|Void
argument_list|>
name|acquireWriteLock
init|=
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
specifier|final
name|String
name|threadDesc
init|=
name|testName
operator|+
literal|"-acquireWriteLock"
decl_stmt|;
name|ZKInterProcessWriteLock
name|writeLock
init|=
name|getReadWriteLock
argument_list|(
name|testName
argument_list|)
operator|.
name|writeLock
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|threadDesc
argument_list|)
argument_list|)
decl_stmt|;
name|writeLock
operator|.
name|acquire
argument_list|()
expr_stmt|;
try|try
block|{
name|writeLockAcquiredLatch
operator|.
name|countDown
argument_list|()
expr_stmt|;
name|assertTrue
argument_list|(
name|isLockHeld
operator|.
name|compareAndSet
argument_list|(
literal|false
argument_list|,
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|Thread
operator|.
name|sleep
argument_list|(
literal|1000
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|isLockHeld
operator|.
name|set
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|writeLock
operator|.
name|release
argument_list|()
expr_stmt|;
block|}
return|return
literal|null
return|;
block|}
block|}
decl_stmt|;
name|Callable
argument_list|<
name|Void
argument_list|>
name|acquireReadLock
init|=
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
specifier|final
name|String
name|threadDesc
init|=
name|testName
operator|+
literal|"-acquireReadLock"
decl_stmt|;
name|ZKInterProcessReadLock
name|readLock
init|=
name|getReadWriteLock
argument_list|(
name|testName
argument_list|)
operator|.
name|readLock
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|threadDesc
argument_list|)
argument_list|)
decl_stmt|;
name|writeLockAcquiredLatch
operator|.
name|await
argument_list|()
expr_stmt|;
name|readLock
operator|.
name|acquire
argument_list|()
expr_stmt|;
try|try
block|{
name|assertFalse
argument_list|(
name|isLockHeld
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|readLock
operator|.
name|release
argument_list|()
expr_stmt|;
block|}
return|return
literal|null
return|;
block|}
block|}
decl_stmt|;
name|results
operator|.
name|add
argument_list|(
name|executor
operator|.
name|submit
argument_list|(
name|acquireWriteLock
argument_list|)
argument_list|)
expr_stmt|;
name|results
operator|.
name|add
argument_list|(
name|executor
operator|.
name|submit
argument_list|(
name|acquireReadLock
argument_list|)
argument_list|)
expr_stmt|;
name|MultithreadedTestUtil
operator|.
name|assertOnFutures
argument_list|(
name|results
argument_list|)
expr_stmt|;
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
name|testTimeout
parameter_list|()
throws|throws
name|Exception
block|{
specifier|final
name|String
name|testName
init|=
literal|"testTimeout"
decl_stmt|;
specifier|final
name|CountDownLatch
name|lockAcquiredLatch
init|=
operator|new
name|CountDownLatch
argument_list|(
literal|1
argument_list|)
decl_stmt|;
name|Callable
argument_list|<
name|Void
argument_list|>
name|shouldHog
init|=
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
specifier|final
name|String
name|threadDesc
init|=
name|testName
operator|+
literal|"-shouldHog"
decl_stmt|;
name|ZKInterProcessWriteLock
name|lock
init|=
name|getReadWriteLock
argument_list|(
name|testName
argument_list|)
operator|.
name|writeLock
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|threadDesc
argument_list|)
argument_list|)
decl_stmt|;
name|lock
operator|.
name|acquire
argument_list|()
expr_stmt|;
name|lockAcquiredLatch
operator|.
name|countDown
argument_list|()
expr_stmt|;
name|Thread
operator|.
name|sleep
argument_list|(
literal|10000
argument_list|)
expr_stmt|;
name|lock
operator|.
name|release
argument_list|()
expr_stmt|;
return|return
literal|null
return|;
block|}
block|}
decl_stmt|;
name|Callable
argument_list|<
name|Void
argument_list|>
name|shouldTimeout
init|=
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
specifier|final
name|String
name|threadDesc
init|=
name|testName
operator|+
literal|"-shouldTimeout"
decl_stmt|;
name|ZKInterProcessWriteLock
name|lock
init|=
name|getReadWriteLock
argument_list|(
name|testName
argument_list|)
operator|.
name|writeLock
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|threadDesc
argument_list|)
argument_list|)
decl_stmt|;
name|lockAcquiredLatch
operator|.
name|await
argument_list|()
expr_stmt|;
name|assertFalse
argument_list|(
name|lock
operator|.
name|tryAcquire
argument_list|(
literal|5000
argument_list|)
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
block|}
decl_stmt|;
name|Callable
argument_list|<
name|Void
argument_list|>
name|shouldAcquireLock
init|=
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
specifier|final
name|String
name|threadDesc
init|=
name|testName
operator|+
literal|"-shouldAcquireLock"
decl_stmt|;
name|ZKInterProcessWriteLock
name|lock
init|=
name|getReadWriteLock
argument_list|(
name|testName
argument_list|)
operator|.
name|writeLock
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|threadDesc
argument_list|)
argument_list|)
decl_stmt|;
name|lockAcquiredLatch
operator|.
name|await
argument_list|()
expr_stmt|;
name|assertTrue
argument_list|(
name|lock
operator|.
name|tryAcquire
argument_list|(
literal|30000
argument_list|)
argument_list|)
expr_stmt|;
name|lock
operator|.
name|release
argument_list|()
expr_stmt|;
return|return
literal|null
return|;
block|}
block|}
decl_stmt|;
name|List
argument_list|<
name|Future
argument_list|<
name|Void
argument_list|>
argument_list|>
name|results
init|=
name|Lists
operator|.
name|newArrayList
argument_list|()
decl_stmt|;
name|results
operator|.
name|add
argument_list|(
name|executor
operator|.
name|submit
argument_list|(
name|shouldHog
argument_list|)
argument_list|)
expr_stmt|;
name|results
operator|.
name|add
argument_list|(
name|executor
operator|.
name|submit
argument_list|(
name|shouldTimeout
argument_list|)
argument_list|)
expr_stmt|;
name|results
operator|.
name|add
argument_list|(
name|executor
operator|.
name|submit
argument_list|(
name|shouldAcquireLock
argument_list|)
argument_list|)
expr_stmt|;
name|MultithreadedTestUtil
operator|.
name|assertOnFutures
argument_list|(
name|results
argument_list|)
expr_stmt|;
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
name|testMultipleClients
parameter_list|()
throws|throws
name|Exception
block|{
comment|//tests lock usage from multiple zookeeper clients with different sessions.
comment|//acquire one read lock, then one write lock
specifier|final
name|String
name|testName
init|=
literal|"testMultipleClients"
decl_stmt|;
comment|//different zookeeper sessions with separate identifiers
name|ZooKeeperWatcher
name|zkWatcher1
init|=
operator|new
name|ZooKeeperWatcher
argument_list|(
name|conf
argument_list|,
literal|"testMultipleClients-1"
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|ZooKeeperWatcher
name|zkWatcher2
init|=
operator|new
name|ZooKeeperWatcher
argument_list|(
name|conf
argument_list|,
literal|"testMultipleClients-2"
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|String
name|znode
init|=
name|ZKUtil
operator|.
name|joinZNode
argument_list|(
name|zkWatcher1
operator|.
name|tableLockZNode
argument_list|,
name|testName
argument_list|)
decl_stmt|;
name|ZKInterProcessReadWriteLock
name|clientLock1
init|=
operator|new
name|ZKInterProcessReadWriteLock
argument_list|(
name|zkWatcher1
argument_list|,
name|znode
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|ZKInterProcessReadWriteLock
name|clientLock2
init|=
operator|new
name|ZKInterProcessReadWriteLock
argument_list|(
name|zkWatcher2
argument_list|,
name|znode
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|InterProcessLock
name|lock1
init|=
name|clientLock1
operator|.
name|readLock
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"client1"
argument_list|)
argument_list|)
decl_stmt|;
name|lock1
operator|.
name|acquire
argument_list|()
expr_stmt|;
comment|//try to acquire, but it will timeout. We are testing whether this will cause any problems
comment|//due to the read lock being from another client
name|InterProcessLock
name|lock2
init|=
name|clientLock2
operator|.
name|writeLock
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"client2"
argument_list|)
argument_list|)
decl_stmt|;
name|assertFalse
argument_list|(
name|lock2
operator|.
name|tryAcquire
argument_list|(
literal|1000
argument_list|)
argument_list|)
expr_stmt|;
name|lock1
operator|.
name|release
argument_list|()
expr_stmt|;
comment|//this time it will acquire
name|assertTrue
argument_list|(
name|lock2
operator|.
name|tryAcquire
argument_list|(
literal|5000
argument_list|)
argument_list|)
expr_stmt|;
name|lock2
operator|.
name|release
argument_list|()
expr_stmt|;
name|zkWatcher1
operator|.
name|close
argument_list|()
expr_stmt|;
name|zkWatcher2
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

