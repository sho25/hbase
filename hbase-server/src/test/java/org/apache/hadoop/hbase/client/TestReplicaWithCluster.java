begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|client
package|;
end_package

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
name|coprocessor
operator|.
name|BaseRegionObserver
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
name|coprocessor
operator|.
name|RegionCoprocessorEnvironment
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
name|protobuf
operator|.
name|generated
operator|.
name|ClientProtos
operator|.
name|BulkLoadHFileRequest
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
name|protobuf
operator|.
name|RequestConverter
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
name|StorefileRefresherChore
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
name|TestHRegionServerBulkLoad
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
name|ClientTests
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
name|util
operator|.
name|FSTableDescriptors
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
name|Arrays
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
name|TimeUnit
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
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|atomic
operator|.
name|AtomicReference
import|;
end_import

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|MediumTests
operator|.
name|class
block|,
name|ClientTests
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|TestReplicaWithCluster
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
name|TestReplicaWithCluster
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|NB_SERVERS
init|=
literal|2
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|row
init|=
name|TestReplicaWithCluster
operator|.
name|class
operator|.
name|getName
argument_list|()
operator|.
name|getBytes
argument_list|()
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|HBaseTestingUtility
name|HTU
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
comment|// second minicluster used in testing of replication
specifier|private
specifier|static
name|HBaseTestingUtility
name|HTU2
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|f
init|=
name|HConstants
operator|.
name|CATALOG_FAMILY
decl_stmt|;
specifier|private
specifier|final
specifier|static
name|int
name|REFRESH_PERIOD
init|=
literal|1000
decl_stmt|;
comment|/**    * This copro is used to synchronize the tests.    */
specifier|public
specifier|static
class|class
name|SlowMeCopro
extends|extends
name|BaseRegionObserver
block|{
specifier|static
specifier|final
name|AtomicLong
name|sleepTime
init|=
operator|new
name|AtomicLong
argument_list|(
literal|0
argument_list|)
decl_stmt|;
specifier|static
specifier|final
name|AtomicReference
argument_list|<
name|CountDownLatch
argument_list|>
name|cdl
init|=
operator|new
name|AtomicReference
argument_list|<
name|CountDownLatch
argument_list|>
argument_list|(
operator|new
name|CountDownLatch
argument_list|(
literal|0
argument_list|)
argument_list|)
decl_stmt|;
specifier|public
name|SlowMeCopro
parameter_list|()
block|{     }
annotation|@
name|Override
specifier|public
name|void
name|preGetOp
parameter_list|(
specifier|final
name|ObserverContext
argument_list|<
name|RegionCoprocessorEnvironment
argument_list|>
name|e
parameter_list|,
specifier|final
name|Get
name|get
parameter_list|,
specifier|final
name|List
argument_list|<
name|Cell
argument_list|>
name|results
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|e
operator|.
name|getEnvironment
argument_list|()
operator|.
name|getRegion
argument_list|()
operator|.
name|getRegionInfo
argument_list|()
operator|.
name|getReplicaId
argument_list|()
operator|==
literal|0
condition|)
block|{
name|CountDownLatch
name|latch
init|=
name|cdl
operator|.
name|get
argument_list|()
decl_stmt|;
try|try
block|{
if|if
condition|(
name|sleepTime
operator|.
name|get
argument_list|()
operator|>
literal|0
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Sleeping for "
operator|+
name|sleepTime
operator|.
name|get
argument_list|()
operator|+
literal|" ms"
argument_list|)
expr_stmt|;
name|Thread
operator|.
name|sleep
argument_list|(
name|sleepTime
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|latch
operator|.
name|getCount
argument_list|()
operator|>
literal|0
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Waiting for the counterCountDownLatch"
argument_list|)
expr_stmt|;
name|latch
operator|.
name|await
argument_list|(
literal|2
argument_list|,
name|TimeUnit
operator|.
name|MINUTES
argument_list|)
expr_stmt|;
comment|// To help the tests to finish.
if|if
condition|(
name|latch
operator|.
name|getCount
argument_list|()
operator|>
literal|0
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Can't wait more"
argument_list|)
throw|;
block|}
block|}
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e1
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
name|e1
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"We're not the primary replicas."
argument_list|)
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|beforeClass
parameter_list|()
throws|throws
name|Exception
block|{
comment|// enable store file refreshing
name|HTU
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setInt
argument_list|(
name|StorefileRefresherChore
operator|.
name|REGIONSERVER_STOREFILE_REFRESH_PERIOD
argument_list|,
name|REFRESH_PERIOD
argument_list|)
expr_stmt|;
name|HTU
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setFloat
argument_list|(
literal|"hbase.regionserver.logroll.multiplier"
argument_list|,
literal|0.0001f
argument_list|)
expr_stmt|;
name|HTU
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setInt
argument_list|(
literal|"replication.source.size.capacity"
argument_list|,
literal|10240
argument_list|)
expr_stmt|;
name|HTU
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setLong
argument_list|(
literal|"replication.source.sleepforretries"
argument_list|,
literal|100
argument_list|)
expr_stmt|;
name|HTU
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setInt
argument_list|(
literal|"hbase.regionserver.maxlogs"
argument_list|,
literal|2
argument_list|)
expr_stmt|;
name|HTU
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setLong
argument_list|(
literal|"hbase.master.logcleaner.ttl"
argument_list|,
literal|10
argument_list|)
expr_stmt|;
name|HTU
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setInt
argument_list|(
literal|"zookeeper.recovery.retry"
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|HTU
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setInt
argument_list|(
literal|"zookeeper.recovery.retry.intervalmill"
argument_list|,
literal|10
argument_list|)
expr_stmt|;
name|HTU
operator|.
name|startMiniCluster
argument_list|(
name|NB_SERVERS
argument_list|)
expr_stmt|;
name|HTU
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|startMaster
argument_list|()
expr_stmt|;
block|}
annotation|@
name|AfterClass
specifier|public
specifier|static
name|void
name|afterClass
parameter_list|()
throws|throws
name|Exception
block|{
if|if
condition|(
name|HTU2
operator|!=
literal|null
condition|)
name|HTU2
operator|.
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
name|HTU
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
literal|30000
argument_list|)
specifier|public
name|void
name|testCreateDeleteTable
parameter_list|()
throws|throws
name|IOException
block|{
comment|// Create table then get the single region for our new table.
name|HTableDescriptor
name|hdt
init|=
name|HTU
operator|.
name|createTableDescriptor
argument_list|(
literal|"testCreateDeleteTable"
argument_list|)
decl_stmt|;
name|hdt
operator|.
name|setRegionReplication
argument_list|(
name|NB_SERVERS
argument_list|)
expr_stmt|;
name|hdt
operator|.
name|addCoprocessor
argument_list|(
name|SlowMeCopro
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|Table
name|table
init|=
name|HTU
operator|.
name|createTable
argument_list|(
name|hdt
argument_list|,
operator|new
name|byte
index|[]
index|[]
block|{
name|f
block|}
argument_list|,
name|HTU
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
name|Put
name|p
init|=
operator|new
name|Put
argument_list|(
name|row
argument_list|)
decl_stmt|;
name|p
operator|.
name|add
argument_list|(
name|f
argument_list|,
name|row
argument_list|,
name|row
argument_list|)
expr_stmt|;
name|table
operator|.
name|put
argument_list|(
name|p
argument_list|)
expr_stmt|;
name|Get
name|g
init|=
operator|new
name|Get
argument_list|(
name|row
argument_list|)
decl_stmt|;
name|Result
name|r
init|=
name|table
operator|.
name|get
argument_list|(
name|g
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertFalse
argument_list|(
name|r
operator|.
name|isStale
argument_list|()
argument_list|)
expr_stmt|;
try|try
block|{
comment|// But if we ask for stale we will get it
name|SlowMeCopro
operator|.
name|cdl
operator|.
name|set
argument_list|(
operator|new
name|CountDownLatch
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|g
operator|=
operator|new
name|Get
argument_list|(
name|row
argument_list|)
expr_stmt|;
name|g
operator|.
name|setConsistency
argument_list|(
name|Consistency
operator|.
name|TIMELINE
argument_list|)
expr_stmt|;
name|r
operator|=
name|table
operator|.
name|get
argument_list|(
name|g
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|r
operator|.
name|isStale
argument_list|()
argument_list|)
expr_stmt|;
name|SlowMeCopro
operator|.
name|cdl
operator|.
name|get
argument_list|()
operator|.
name|countDown
argument_list|()
expr_stmt|;
block|}
finally|finally
block|{
name|SlowMeCopro
operator|.
name|cdl
operator|.
name|get
argument_list|()
operator|.
name|countDown
argument_list|()
expr_stmt|;
name|SlowMeCopro
operator|.
name|sleepTime
operator|.
name|set
argument_list|(
literal|0
argument_list|)
expr_stmt|;
block|}
name|HTU
operator|.
name|getHBaseAdmin
argument_list|()
operator|.
name|disableTable
argument_list|(
name|hdt
operator|.
name|getTableName
argument_list|()
argument_list|)
expr_stmt|;
name|HTU
operator|.
name|deleteTable
argument_list|(
name|hdt
operator|.
name|getTableName
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|120000
argument_list|)
specifier|public
name|void
name|testChangeTable
parameter_list|()
throws|throws
name|Exception
block|{
name|HTableDescriptor
name|hdt
init|=
name|HTU
operator|.
name|createTableDescriptor
argument_list|(
literal|"testChangeTable"
argument_list|)
decl_stmt|;
name|hdt
operator|.
name|setRegionReplication
argument_list|(
name|NB_SERVERS
argument_list|)
expr_stmt|;
name|hdt
operator|.
name|addCoprocessor
argument_list|(
name|SlowMeCopro
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|Table
name|table
init|=
name|HTU
operator|.
name|createTable
argument_list|(
name|hdt
argument_list|,
operator|new
name|byte
index|[]
index|[]
block|{
name|f
block|}
argument_list|,
name|HTU
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
comment|// basic test: it should work.
name|Put
name|p
init|=
operator|new
name|Put
argument_list|(
name|row
argument_list|)
decl_stmt|;
name|p
operator|.
name|add
argument_list|(
name|f
argument_list|,
name|row
argument_list|,
name|row
argument_list|)
expr_stmt|;
name|table
operator|.
name|put
argument_list|(
name|p
argument_list|)
expr_stmt|;
name|Get
name|g
init|=
operator|new
name|Get
argument_list|(
name|row
argument_list|)
decl_stmt|;
name|Result
name|r
init|=
name|table
operator|.
name|get
argument_list|(
name|g
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertFalse
argument_list|(
name|r
operator|.
name|isStale
argument_list|()
argument_list|)
expr_stmt|;
comment|// Add a CF, it should work.
name|HTableDescriptor
name|bHdt
init|=
name|HTU
operator|.
name|getHBaseAdmin
argument_list|()
operator|.
name|getTableDescriptor
argument_list|(
name|hdt
operator|.
name|getTableName
argument_list|()
argument_list|)
decl_stmt|;
name|HColumnDescriptor
name|hcd
init|=
operator|new
name|HColumnDescriptor
argument_list|(
name|row
argument_list|)
decl_stmt|;
name|hdt
operator|.
name|addFamily
argument_list|(
name|hcd
argument_list|)
expr_stmt|;
name|HTU
operator|.
name|getHBaseAdmin
argument_list|()
operator|.
name|disableTable
argument_list|(
name|hdt
operator|.
name|getTableName
argument_list|()
argument_list|)
expr_stmt|;
name|HTU
operator|.
name|getHBaseAdmin
argument_list|()
operator|.
name|modifyTable
argument_list|(
name|hdt
operator|.
name|getTableName
argument_list|()
argument_list|,
name|hdt
argument_list|)
expr_stmt|;
name|HTU
operator|.
name|getHBaseAdmin
argument_list|()
operator|.
name|enableTable
argument_list|(
name|hdt
operator|.
name|getTableName
argument_list|()
argument_list|)
expr_stmt|;
name|HTableDescriptor
name|nHdt
init|=
name|HTU
operator|.
name|getHBaseAdmin
argument_list|()
operator|.
name|getTableDescriptor
argument_list|(
name|hdt
operator|.
name|getTableName
argument_list|()
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"fams="
operator|+
name|Arrays
operator|.
name|toString
argument_list|(
name|nHdt
operator|.
name|getColumnFamilies
argument_list|()
argument_list|)
argument_list|,
name|bHdt
operator|.
name|getColumnFamilies
argument_list|()
operator|.
name|length
operator|+
literal|1
argument_list|,
name|nHdt
operator|.
name|getColumnFamilies
argument_list|()
operator|.
name|length
argument_list|)
expr_stmt|;
name|p
operator|=
operator|new
name|Put
argument_list|(
name|row
argument_list|)
expr_stmt|;
name|p
operator|.
name|add
argument_list|(
name|row
argument_list|,
name|row
argument_list|,
name|row
argument_list|)
expr_stmt|;
name|table
operator|.
name|put
argument_list|(
name|p
argument_list|)
expr_stmt|;
name|g
operator|=
operator|new
name|Get
argument_list|(
name|row
argument_list|)
expr_stmt|;
name|r
operator|=
name|table
operator|.
name|get
argument_list|(
name|g
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertFalse
argument_list|(
name|r
operator|.
name|isStale
argument_list|()
argument_list|)
expr_stmt|;
try|try
block|{
name|SlowMeCopro
operator|.
name|cdl
operator|.
name|set
argument_list|(
operator|new
name|CountDownLatch
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|g
operator|=
operator|new
name|Get
argument_list|(
name|row
argument_list|)
expr_stmt|;
name|g
operator|.
name|setConsistency
argument_list|(
name|Consistency
operator|.
name|TIMELINE
argument_list|)
expr_stmt|;
name|r
operator|=
name|table
operator|.
name|get
argument_list|(
name|g
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|r
operator|.
name|isStale
argument_list|()
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|SlowMeCopro
operator|.
name|cdl
operator|.
name|get
argument_list|()
operator|.
name|countDown
argument_list|()
expr_stmt|;
name|SlowMeCopro
operator|.
name|sleepTime
operator|.
name|set
argument_list|(
literal|0
argument_list|)
expr_stmt|;
block|}
name|Admin
name|admin
init|=
name|HTU
operator|.
name|getHBaseAdmin
argument_list|()
decl_stmt|;
name|nHdt
operator|=
name|admin
operator|.
name|getTableDescriptor
argument_list|(
name|hdt
operator|.
name|getTableName
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"fams="
operator|+
name|Arrays
operator|.
name|toString
argument_list|(
name|nHdt
operator|.
name|getColumnFamilies
argument_list|()
argument_list|)
argument_list|,
name|bHdt
operator|.
name|getColumnFamilies
argument_list|()
operator|.
name|length
operator|+
literal|1
argument_list|,
name|nHdt
operator|.
name|getColumnFamilies
argument_list|()
operator|.
name|length
argument_list|)
expr_stmt|;
name|admin
operator|.
name|disableTable
argument_list|(
name|hdt
operator|.
name|getTableName
argument_list|()
argument_list|)
expr_stmt|;
name|admin
operator|.
name|deleteTable
argument_list|(
name|hdt
operator|.
name|getTableName
argument_list|()
argument_list|)
expr_stmt|;
name|admin
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
annotation|@
name|SuppressWarnings
argument_list|(
literal|"deprecation"
argument_list|)
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|300000
argument_list|)
specifier|public
name|void
name|testReplicaAndReplication
parameter_list|()
throws|throws
name|Exception
block|{
name|HTableDescriptor
name|hdt
init|=
name|HTU
operator|.
name|createTableDescriptor
argument_list|(
literal|"testReplicaAndReplication"
argument_list|)
decl_stmt|;
name|hdt
operator|.
name|setRegionReplication
argument_list|(
name|NB_SERVERS
argument_list|)
expr_stmt|;
name|HColumnDescriptor
name|fam
init|=
operator|new
name|HColumnDescriptor
argument_list|(
name|row
argument_list|)
decl_stmt|;
name|fam
operator|.
name|setScope
argument_list|(
name|HConstants
operator|.
name|REPLICATION_SCOPE_GLOBAL
argument_list|)
expr_stmt|;
name|hdt
operator|.
name|addFamily
argument_list|(
name|fam
argument_list|)
expr_stmt|;
name|hdt
operator|.
name|addCoprocessor
argument_list|(
name|SlowMeCopro
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|HTU
operator|.
name|getHBaseAdmin
argument_list|()
operator|.
name|createTable
argument_list|(
name|hdt
argument_list|,
name|HBaseTestingUtility
operator|.
name|KEYS_FOR_HBA_CREATE_TABLE
argument_list|)
expr_stmt|;
name|Configuration
name|conf2
init|=
name|HBaseConfiguration
operator|.
name|create
argument_list|(
name|HTU
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
name|conf2
operator|.
name|set
argument_list|(
name|HConstants
operator|.
name|HBASE_CLIENT_INSTANCE_ID
argument_list|,
name|String
operator|.
name|valueOf
argument_list|(
operator|-
literal|1
argument_list|)
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
name|MiniZooKeeperCluster
name|miniZK
init|=
name|HTU
operator|.
name|getZkCluster
argument_list|()
decl_stmt|;
name|HTU2
operator|=
operator|new
name|HBaseTestingUtility
argument_list|(
name|conf2
argument_list|)
expr_stmt|;
name|HTU2
operator|.
name|setZkCluster
argument_list|(
name|miniZK
argument_list|)
expr_stmt|;
name|HTU2
operator|.
name|startMiniCluster
argument_list|(
name|NB_SERVERS
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Setup second Zk"
argument_list|)
expr_stmt|;
name|HTU2
operator|.
name|getHBaseAdmin
argument_list|()
operator|.
name|createTable
argument_list|(
name|hdt
argument_list|,
name|HBaseTestingUtility
operator|.
name|KEYS_FOR_HBA_CREATE_TABLE
argument_list|)
expr_stmt|;
name|ReplicationAdmin
name|admin
init|=
operator|new
name|ReplicationAdmin
argument_list|(
name|HTU
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
name|admin
operator|.
name|addPeer
argument_list|(
literal|"2"
argument_list|,
name|HTU2
operator|.
name|getClusterKey
argument_list|()
argument_list|)
expr_stmt|;
name|admin
operator|.
name|close
argument_list|()
expr_stmt|;
name|Put
name|p
init|=
operator|new
name|Put
argument_list|(
name|row
argument_list|)
decl_stmt|;
name|p
operator|.
name|add
argument_list|(
name|row
argument_list|,
name|row
argument_list|,
name|row
argument_list|)
expr_stmt|;
specifier|final
name|Table
name|table
init|=
name|HTU
operator|.
name|getConnection
argument_list|()
operator|.
name|getTable
argument_list|(
name|hdt
operator|.
name|getTableName
argument_list|()
argument_list|)
decl_stmt|;
name|table
operator|.
name|put
argument_list|(
name|p
argument_list|)
expr_stmt|;
name|HTU
operator|.
name|getHBaseAdmin
argument_list|()
operator|.
name|flush
argument_list|(
name|table
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Put& flush done on the first cluster. Now doing a get on the same cluster."
argument_list|)
expr_stmt|;
name|Waiter
operator|.
name|waitFor
argument_list|(
name|HTU
operator|.
name|getConfiguration
argument_list|()
argument_list|,
literal|1000
argument_list|,
operator|new
name|Waiter
operator|.
name|Predicate
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
try|try
block|{
name|SlowMeCopro
operator|.
name|cdl
operator|.
name|set
argument_list|(
operator|new
name|CountDownLatch
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|Get
name|g
init|=
operator|new
name|Get
argument_list|(
name|row
argument_list|)
decl_stmt|;
name|g
operator|.
name|setConsistency
argument_list|(
name|Consistency
operator|.
name|TIMELINE
argument_list|)
expr_stmt|;
name|Result
name|r
init|=
name|table
operator|.
name|get
argument_list|(
name|g
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|r
operator|.
name|isStale
argument_list|()
argument_list|)
expr_stmt|;
return|return
operator|!
name|r
operator|.
name|isEmpty
argument_list|()
return|;
block|}
finally|finally
block|{
name|SlowMeCopro
operator|.
name|cdl
operator|.
name|get
argument_list|()
operator|.
name|countDown
argument_list|()
expr_stmt|;
name|SlowMeCopro
operator|.
name|sleepTime
operator|.
name|set
argument_list|(
literal|0
argument_list|)
expr_stmt|;
block|}
block|}
block|}
argument_list|)
expr_stmt|;
name|table
operator|.
name|close
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"stale get on the first cluster done. Now for the second."
argument_list|)
expr_stmt|;
specifier|final
name|Table
name|table2
init|=
name|HTU
operator|.
name|getConnection
argument_list|()
operator|.
name|getTable
argument_list|(
name|hdt
operator|.
name|getTableName
argument_list|()
argument_list|)
decl_stmt|;
name|Waiter
operator|.
name|waitFor
argument_list|(
name|HTU
operator|.
name|getConfiguration
argument_list|()
argument_list|,
literal|1000
argument_list|,
operator|new
name|Waiter
operator|.
name|Predicate
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
try|try
block|{
name|SlowMeCopro
operator|.
name|cdl
operator|.
name|set
argument_list|(
operator|new
name|CountDownLatch
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|Get
name|g
init|=
operator|new
name|Get
argument_list|(
name|row
argument_list|)
decl_stmt|;
name|g
operator|.
name|setConsistency
argument_list|(
name|Consistency
operator|.
name|TIMELINE
argument_list|)
expr_stmt|;
name|Result
name|r
init|=
name|table2
operator|.
name|get
argument_list|(
name|g
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|r
operator|.
name|isStale
argument_list|()
argument_list|)
expr_stmt|;
return|return
operator|!
name|r
operator|.
name|isEmpty
argument_list|()
return|;
block|}
finally|finally
block|{
name|SlowMeCopro
operator|.
name|cdl
operator|.
name|get
argument_list|()
operator|.
name|countDown
argument_list|()
expr_stmt|;
name|SlowMeCopro
operator|.
name|sleepTime
operator|.
name|set
argument_list|(
literal|0
argument_list|)
expr_stmt|;
block|}
block|}
block|}
argument_list|)
expr_stmt|;
name|table2
operator|.
name|close
argument_list|()
expr_stmt|;
name|HTU
operator|.
name|getHBaseAdmin
argument_list|()
operator|.
name|disableTable
argument_list|(
name|hdt
operator|.
name|getTableName
argument_list|()
argument_list|)
expr_stmt|;
name|HTU
operator|.
name|deleteTable
argument_list|(
name|hdt
operator|.
name|getTableName
argument_list|()
argument_list|)
expr_stmt|;
name|HTU2
operator|.
name|getHBaseAdmin
argument_list|()
operator|.
name|disableTable
argument_list|(
name|hdt
operator|.
name|getTableName
argument_list|()
argument_list|)
expr_stmt|;
name|HTU2
operator|.
name|deleteTable
argument_list|(
name|hdt
operator|.
name|getTableName
argument_list|()
argument_list|)
expr_stmt|;
comment|// We shutdown HTU2 minicluster later, in afterClass(), as shutting down
comment|// the minicluster has negative impact of deleting all HConnections in JVM.
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
name|testBulkLoad
parameter_list|()
throws|throws
name|IOException
block|{
comment|// Create table then get the single region for our new table.
name|LOG
operator|.
name|debug
argument_list|(
literal|"Creating test table"
argument_list|)
expr_stmt|;
name|HTableDescriptor
name|hdt
init|=
name|HTU
operator|.
name|createTableDescriptor
argument_list|(
literal|"testBulkLoad"
argument_list|)
decl_stmt|;
name|hdt
operator|.
name|setRegionReplication
argument_list|(
name|NB_SERVERS
argument_list|)
expr_stmt|;
name|hdt
operator|.
name|addCoprocessor
argument_list|(
name|SlowMeCopro
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|Table
name|table
init|=
name|HTU
operator|.
name|createTable
argument_list|(
name|hdt
argument_list|,
operator|new
name|byte
index|[]
index|[]
block|{
name|f
block|}
argument_list|,
name|HTU
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
comment|// create hfiles to load.
name|LOG
operator|.
name|debug
argument_list|(
literal|"Creating test data"
argument_list|)
expr_stmt|;
name|Path
name|dir
init|=
name|HTU
operator|.
name|getDataTestDirOnTestFS
argument_list|(
literal|"testBulkLoad"
argument_list|)
decl_stmt|;
specifier|final
name|int
name|numRows
init|=
literal|10
decl_stmt|;
specifier|final
name|byte
index|[]
name|qual
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"qual"
argument_list|)
decl_stmt|;
specifier|final
name|byte
index|[]
name|val
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"val"
argument_list|)
decl_stmt|;
specifier|final
name|List
argument_list|<
name|Pair
argument_list|<
name|byte
index|[]
argument_list|,
name|String
argument_list|>
argument_list|>
name|famPaths
init|=
operator|new
name|ArrayList
argument_list|<
name|Pair
argument_list|<
name|byte
index|[]
argument_list|,
name|String
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|HColumnDescriptor
name|col
range|:
name|hdt
operator|.
name|getColumnFamilies
argument_list|()
control|)
block|{
name|Path
name|hfile
init|=
operator|new
name|Path
argument_list|(
name|dir
argument_list|,
name|col
operator|.
name|getNameAsString
argument_list|()
argument_list|)
decl_stmt|;
name|TestHRegionServerBulkLoad
operator|.
name|createHFile
argument_list|(
name|HTU
operator|.
name|getTestFileSystem
argument_list|()
argument_list|,
name|hfile
argument_list|,
name|col
operator|.
name|getName
argument_list|()
argument_list|,
name|qual
argument_list|,
name|val
argument_list|,
name|numRows
argument_list|)
expr_stmt|;
name|famPaths
operator|.
name|add
argument_list|(
operator|new
name|Pair
argument_list|<
name|byte
index|[]
argument_list|,
name|String
argument_list|>
argument_list|(
name|col
operator|.
name|getName
argument_list|()
argument_list|,
name|hfile
operator|.
name|toString
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// bulk load HFiles
name|LOG
operator|.
name|debug
argument_list|(
literal|"Loading test data"
argument_list|)
expr_stmt|;
annotation|@
name|SuppressWarnings
argument_list|(
literal|"deprecation"
argument_list|)
specifier|final
name|HConnection
name|conn
init|=
name|HTU
operator|.
name|getHBaseAdmin
argument_list|()
operator|.
name|getConnection
argument_list|()
decl_stmt|;
name|RegionServerCallable
argument_list|<
name|Void
argument_list|>
name|callable
init|=
operator|new
name|RegionServerCallable
argument_list|<
name|Void
argument_list|>
argument_list|(
name|conn
argument_list|,
name|hdt
operator|.
name|getTableName
argument_list|()
argument_list|,
name|TestHRegionServerBulkLoad
operator|.
name|rowkey
argument_list|(
literal|0
argument_list|)
argument_list|)
block|{
annotation|@
name|Override
specifier|public
name|Void
name|call
parameter_list|(
name|int
name|timeout
parameter_list|)
throws|throws
name|Exception
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Going to connect to server "
operator|+
name|getLocation
argument_list|()
operator|+
literal|" for row "
operator|+
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|getRow
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|byte
index|[]
name|regionName
init|=
name|getLocation
argument_list|()
operator|.
name|getRegionInfo
argument_list|()
operator|.
name|getRegionName
argument_list|()
decl_stmt|;
name|BulkLoadHFileRequest
name|request
init|=
name|RequestConverter
operator|.
name|buildBulkLoadHFileRequest
argument_list|(
name|famPaths
argument_list|,
name|regionName
argument_list|,
literal|true
argument_list|)
decl_stmt|;
name|getStub
argument_list|()
operator|.
name|bulkLoadHFile
argument_list|(
literal|null
argument_list|,
name|request
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
block|}
decl_stmt|;
name|RpcRetryingCallerFactory
name|factory
init|=
operator|new
name|RpcRetryingCallerFactory
argument_list|(
name|HTU
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
name|RpcRetryingCaller
argument_list|<
name|Void
argument_list|>
name|caller
init|=
name|factory
operator|.
expr|<
name|Void
operator|>
name|newCaller
argument_list|()
decl_stmt|;
name|caller
operator|.
name|callWithRetries
argument_list|(
name|callable
argument_list|,
literal|10000
argument_list|)
expr_stmt|;
comment|// verify we can read them from the primary
name|LOG
operator|.
name|debug
argument_list|(
literal|"Verifying data load"
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
name|numRows
condition|;
name|i
operator|++
control|)
block|{
name|byte
index|[]
name|row
init|=
name|TestHRegionServerBulkLoad
operator|.
name|rowkey
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|Get
name|g
init|=
operator|new
name|Get
argument_list|(
name|row
argument_list|)
decl_stmt|;
name|Result
name|r
init|=
name|table
operator|.
name|get
argument_list|(
name|g
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertFalse
argument_list|(
name|r
operator|.
name|isStale
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|// verify we can read them from the replica
name|LOG
operator|.
name|debug
argument_list|(
literal|"Verifying replica queries"
argument_list|)
expr_stmt|;
try|try
block|{
name|SlowMeCopro
operator|.
name|cdl
operator|.
name|set
argument_list|(
operator|new
name|CountDownLatch
argument_list|(
literal|1
argument_list|)
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
name|numRows
condition|;
name|i
operator|++
control|)
block|{
name|byte
index|[]
name|row
init|=
name|TestHRegionServerBulkLoad
operator|.
name|rowkey
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|Get
name|g
init|=
operator|new
name|Get
argument_list|(
name|row
argument_list|)
decl_stmt|;
name|g
operator|.
name|setConsistency
argument_list|(
name|Consistency
operator|.
name|TIMELINE
argument_list|)
expr_stmt|;
name|Result
name|r
init|=
name|table
operator|.
name|get
argument_list|(
name|g
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|r
operator|.
name|isStale
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|SlowMeCopro
operator|.
name|cdl
operator|.
name|get
argument_list|()
operator|.
name|countDown
argument_list|()
expr_stmt|;
block|}
finally|finally
block|{
name|SlowMeCopro
operator|.
name|cdl
operator|.
name|get
argument_list|()
operator|.
name|countDown
argument_list|()
expr_stmt|;
name|SlowMeCopro
operator|.
name|sleepTime
operator|.
name|set
argument_list|(
literal|0
argument_list|)
expr_stmt|;
block|}
name|HTU
operator|.
name|getHBaseAdmin
argument_list|()
operator|.
name|disableTable
argument_list|(
name|hdt
operator|.
name|getTableName
argument_list|()
argument_list|)
expr_stmt|;
name|HTU
operator|.
name|deleteTable
argument_list|(
name|hdt
operator|.
name|getTableName
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

