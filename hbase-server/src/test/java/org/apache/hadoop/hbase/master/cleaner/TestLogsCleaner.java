begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|mockito
operator|.
name|Mockito
operator|.
name|doThrow
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
name|spy
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
name|lang
operator|.
name|reflect
operator|.
name|Field
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|URLEncoder
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Iterator
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|LinkedList
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
name|hbase
operator|.
name|shaded
operator|.
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
name|Abortable
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
name|ChoreService
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
name|CoordinatedStateManager
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
name|Server
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
name|ZooKeeperConnectionException
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
name|ClusterConnection
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
name|Connection
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
name|ReplicationQueuesClientZKImpl
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
name|master
operator|.
name|ReplicationLogCleaner
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
name|regionserver
operator|.
name|Replication
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
name|MetaTableLocator
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
name|RecoverableZooKeeper
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
name|apache
operator|.
name|zookeeper
operator|.
name|data
operator|.
name|Stat
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
name|Mockito
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
name|TestLogsCleaner
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
name|TestLogsCleaner
operator|.
name|class
argument_list|)
decl_stmt|;
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
name|startMiniZKCluster
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
name|shutdownMiniZKCluster
argument_list|()
expr_stmt|;
block|}
comment|/**    * This tests verifies LogCleaner works correctly with WALs and Procedure WALs located    * in the same oldWALs directory.    * Created files:    * - 2 invalid files    * - 5 old Procedure WALs    * - 30 old WALs from which 3 are in replication    * - 5 recent Procedure WALs    * - 1 recent WAL    * - 1 very new WAL (timestamp in future)    * - masterProcedureWALs subdirectory    * Files which should stay:    * - 3 replication WALs    * - 2 new WALs    * - 5 latest Procedure WALs    * - masterProcedureWALs subdirectory    */
annotation|@
name|Test
specifier|public
name|void
name|testLogCleaning
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
comment|// set TTLs
name|long
name|ttlWAL
init|=
literal|2000
decl_stmt|;
name|long
name|ttlProcedureWAL
init|=
literal|4000
decl_stmt|;
name|conf
operator|.
name|setLong
argument_list|(
literal|"hbase.master.logcleaner.ttl"
argument_list|,
name|ttlWAL
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setLong
argument_list|(
literal|"hbase.master.procedurewalcleaner.ttl"
argument_list|,
name|ttlProcedureWAL
argument_list|)
expr_stmt|;
name|Replication
operator|.
name|decorateMasterConfiguration
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|Server
name|server
init|=
operator|new
name|DummyServer
argument_list|()
decl_stmt|;
name|ReplicationQueues
name|repQueues
init|=
name|ReplicationFactory
operator|.
name|getReplicationQueues
argument_list|(
operator|new
name|ReplicationQueuesArguments
argument_list|(
name|conf
argument_list|,
name|server
argument_list|,
name|server
operator|.
name|getZooKeeper
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|repQueues
operator|.
name|init
argument_list|(
name|server
operator|.
name|getServerName
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
specifier|final
name|Path
name|oldLogDir
init|=
operator|new
name|Path
argument_list|(
name|TEST_UTIL
operator|.
name|getDataTestDir
argument_list|()
argument_list|,
name|HConstants
operator|.
name|HREGION_OLDLOGDIR_NAME
argument_list|)
decl_stmt|;
specifier|final
name|Path
name|oldProcedureWALDir
init|=
operator|new
name|Path
argument_list|(
name|oldLogDir
argument_list|,
literal|"masterProcedureWALs"
argument_list|)
decl_stmt|;
name|String
name|fakeMachineName
init|=
name|URLEncoder
operator|.
name|encode
argument_list|(
name|server
operator|.
name|getServerName
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|,
literal|"UTF8"
argument_list|)
decl_stmt|;
specifier|final
name|FileSystem
name|fs
init|=
name|FileSystem
operator|.
name|get
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|long
name|now
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|fs
operator|.
name|delete
argument_list|(
name|oldLogDir
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|fs
operator|.
name|mkdirs
argument_list|(
name|oldLogDir
argument_list|)
expr_stmt|;
comment|// Case 1: 2 invalid files, which would be deleted directly
name|fs
operator|.
name|createNewFile
argument_list|(
operator|new
name|Path
argument_list|(
name|oldLogDir
argument_list|,
literal|"a"
argument_list|)
argument_list|)
expr_stmt|;
name|fs
operator|.
name|createNewFile
argument_list|(
operator|new
name|Path
argument_list|(
name|oldLogDir
argument_list|,
name|fakeMachineName
operator|+
literal|"."
operator|+
literal|"a"
argument_list|)
argument_list|)
expr_stmt|;
comment|// Case 2: 5 Procedure WALs that are old which would be deleted
for|for
control|(
name|int
name|i
init|=
literal|1
init|;
name|i
operator|<
literal|6
condition|;
name|i
operator|++
control|)
block|{
name|Path
name|fileName
init|=
operator|new
name|Path
argument_list|(
name|oldProcedureWALDir
argument_list|,
name|String
operator|.
name|format
argument_list|(
literal|"pv-%020d.log"
argument_list|,
name|i
argument_list|)
argument_list|)
decl_stmt|;
name|fs
operator|.
name|createNewFile
argument_list|(
name|fileName
argument_list|)
expr_stmt|;
block|}
comment|// Sleep for sometime to get old procedure WALs
name|Thread
operator|.
name|sleep
argument_list|(
name|ttlProcedureWAL
operator|-
name|ttlWAL
argument_list|)
expr_stmt|;
comment|// Case 3: old WALs which would be deletable
for|for
control|(
name|int
name|i
init|=
literal|1
init|;
name|i
operator|<
literal|31
condition|;
name|i
operator|++
control|)
block|{
name|Path
name|fileName
init|=
operator|new
name|Path
argument_list|(
name|oldLogDir
argument_list|,
name|fakeMachineName
operator|+
literal|"."
operator|+
operator|(
name|now
operator|-
name|i
operator|)
argument_list|)
decl_stmt|;
name|fs
operator|.
name|createNewFile
argument_list|(
name|fileName
argument_list|)
expr_stmt|;
comment|// Case 4: put 3 WALs in ZK indicating that they are scheduled for replication so these
comment|// files would pass TimeToLiveLogCleaner but would be rejected by ReplicationLogCleaner
if|if
condition|(
name|i
operator|%
operator|(
literal|30
operator|/
literal|3
operator|)
operator|==
literal|1
condition|)
block|{
name|repQueues
operator|.
name|addLog
argument_list|(
name|fakeMachineName
argument_list|,
name|fileName
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Replication log file: "
operator|+
name|fileName
argument_list|)
expr_stmt|;
block|}
block|}
comment|// Case 5: 5 Procedure WALs that are new, will stay
for|for
control|(
name|int
name|i
init|=
literal|6
init|;
name|i
operator|<
literal|11
condition|;
name|i
operator|++
control|)
block|{
name|Path
name|fileName
init|=
operator|new
name|Path
argument_list|(
name|oldProcedureWALDir
argument_list|,
name|String
operator|.
name|format
argument_list|(
literal|"pv-%020d.log"
argument_list|,
name|i
argument_list|)
argument_list|)
decl_stmt|;
name|fs
operator|.
name|createNewFile
argument_list|(
name|fileName
argument_list|)
expr_stmt|;
block|}
comment|// Sleep for sometime to get newer modification time
name|Thread
operator|.
name|sleep
argument_list|(
name|ttlWAL
argument_list|)
expr_stmt|;
name|fs
operator|.
name|createNewFile
argument_list|(
operator|new
name|Path
argument_list|(
name|oldLogDir
argument_list|,
name|fakeMachineName
operator|+
literal|"."
operator|+
name|now
argument_list|)
argument_list|)
expr_stmt|;
comment|// Case 6: 1 newer WAL, not even deletable for TimeToLiveLogCleaner,
comment|// so we are not going down the chain
name|fs
operator|.
name|createNewFile
argument_list|(
operator|new
name|Path
argument_list|(
name|oldLogDir
argument_list|,
name|fakeMachineName
operator|+
literal|"."
operator|+
operator|(
name|now
operator|+
name|ttlWAL
operator|)
argument_list|)
argument_list|)
expr_stmt|;
for|for
control|(
name|FileStatus
name|stat
range|:
name|fs
operator|.
name|listStatus
argument_list|(
name|oldLogDir
argument_list|)
control|)
block|{
name|LOG
operator|.
name|info
argument_list|(
name|stat
operator|.
name|getPath
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|// There should be 34 files and masterProcedureWALs directory
name|assertEquals
argument_list|(
literal|35
argument_list|,
name|fs
operator|.
name|listStatus
argument_list|(
name|oldLogDir
argument_list|)
operator|.
name|length
argument_list|)
expr_stmt|;
comment|// 10 procedure WALs
name|assertEquals
argument_list|(
literal|10
argument_list|,
name|fs
operator|.
name|listStatus
argument_list|(
name|oldProcedureWALDir
argument_list|)
operator|.
name|length
argument_list|)
expr_stmt|;
name|LogCleaner
name|cleaner
init|=
operator|new
name|LogCleaner
argument_list|(
literal|1000
argument_list|,
name|server
argument_list|,
name|conf
argument_list|,
name|fs
argument_list|,
name|oldLogDir
argument_list|)
decl_stmt|;
name|cleaner
operator|.
name|chore
argument_list|()
expr_stmt|;
comment|// In oldWALs we end up with the current WAL, a newer WAL, the 3 old WALs which
comment|// are scheduled for replication and masterProcedureWALs directory
name|TEST_UTIL
operator|.
name|waitFor
argument_list|(
literal|1000
argument_list|,
call|(
name|Waiter
operator|.
name|Predicate
argument_list|<
name|Exception
argument_list|>
call|)
argument_list|()
operator|->
literal|6
operator|==
name|fs
operator|.
name|listStatus
argument_list|(
name|oldLogDir
argument_list|)
operator|.
name|length
argument_list|)
expr_stmt|;
comment|// In masterProcedureWALs we end up with 5 newer Procedure WALs
name|TEST_UTIL
operator|.
name|waitFor
argument_list|(
literal|1000
argument_list|,
call|(
name|Waiter
operator|.
name|Predicate
argument_list|<
name|Exception
argument_list|>
call|)
argument_list|()
operator|->
literal|5
operator|==
name|fs
operator|.
name|listStatus
argument_list|(
name|oldProcedureWALDir
argument_list|)
operator|.
name|length
argument_list|)
expr_stmt|;
for|for
control|(
name|FileStatus
name|file
range|:
name|fs
operator|.
name|listStatus
argument_list|(
name|oldLogDir
argument_list|)
control|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Kept log file in oldWALs: "
operator|+
name|file
operator|.
name|getPath
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|FileStatus
name|file
range|:
name|fs
operator|.
name|listStatus
argument_list|(
name|oldProcedureWALDir
argument_list|)
control|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Kept log file in masterProcedureWALs: "
operator|+
name|file
operator|.
name|getPath
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|5000
argument_list|)
specifier|public
name|void
name|testZnodeCversionChange
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
name|ReplicationLogCleaner
name|cleaner
init|=
operator|new
name|ReplicationLogCleaner
argument_list|()
decl_stmt|;
name|cleaner
operator|.
name|setConf
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|ReplicationQueuesClientZKImpl
name|rqcMock
init|=
name|Mockito
operator|.
name|mock
argument_list|(
name|ReplicationQueuesClientZKImpl
operator|.
name|class
argument_list|)
decl_stmt|;
name|Mockito
operator|.
name|when
argument_list|(
name|rqcMock
operator|.
name|getQueuesZNodeCversion
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
literal|1
argument_list|,
literal|2
argument_list|,
literal|3
argument_list|,
literal|4
argument_list|)
expr_stmt|;
name|Field
name|rqc
init|=
name|ReplicationLogCleaner
operator|.
name|class
operator|.
name|getDeclaredField
argument_list|(
literal|"replicationQueues"
argument_list|)
decl_stmt|;
name|rqc
operator|.
name|setAccessible
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|rqc
operator|.
name|set
argument_list|(
name|cleaner
argument_list|,
name|rqcMock
argument_list|)
expr_stmt|;
comment|// This should return eventually when cversion stabilizes
name|cleaner
operator|.
name|getDeletableFiles
argument_list|(
operator|new
name|LinkedList
argument_list|<>
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**    * ReplicationLogCleaner should be able to ride over ZooKeeper errors without aborting.    */
annotation|@
name|Test
specifier|public
name|void
name|testZooKeeperAbort
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
name|ReplicationLogCleaner
name|cleaner
init|=
operator|new
name|ReplicationLogCleaner
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|FileStatus
argument_list|>
name|dummyFiles
init|=
name|Lists
operator|.
name|newArrayList
argument_list|(
operator|new
name|FileStatus
argument_list|(
literal|100
argument_list|,
literal|false
argument_list|,
literal|3
argument_list|,
literal|100
argument_list|,
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|,
operator|new
name|Path
argument_list|(
literal|"log1"
argument_list|)
argument_list|)
argument_list|,
operator|new
name|FileStatus
argument_list|(
literal|100
argument_list|,
literal|false
argument_list|,
literal|3
argument_list|,
literal|100
argument_list|,
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|,
operator|new
name|Path
argument_list|(
literal|"log2"
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
try|try
init|(
name|FaultyZooKeeperWatcher
name|faultyZK
init|=
operator|new
name|FaultyZooKeeperWatcher
argument_list|(
name|conf
argument_list|,
literal|"testZooKeeperAbort-faulty"
argument_list|,
literal|null
argument_list|)
init|)
block|{
name|faultyZK
operator|.
name|init
argument_list|()
expr_stmt|;
name|cleaner
operator|.
name|setConf
argument_list|(
name|conf
argument_list|,
name|faultyZK
argument_list|)
expr_stmt|;
name|cleaner
operator|.
name|preClean
argument_list|()
expr_stmt|;
comment|// should keep all files due to a ConnectionLossException getting the queues znodes
name|Iterable
argument_list|<
name|FileStatus
argument_list|>
name|toDelete
init|=
name|cleaner
operator|.
name|getDeletableFiles
argument_list|(
name|dummyFiles
argument_list|)
decl_stmt|;
name|assertFalse
argument_list|(
name|toDelete
operator|.
name|iterator
argument_list|()
operator|.
name|hasNext
argument_list|()
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|cleaner
operator|.
name|isStopped
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|// when zk is working both files should be returned
name|cleaner
operator|=
operator|new
name|ReplicationLogCleaner
argument_list|()
expr_stmt|;
try|try
init|(
name|ZKWatcher
name|zkw
init|=
operator|new
name|ZKWatcher
argument_list|(
name|conf
argument_list|,
literal|"testZooKeeperAbort-normal"
argument_list|,
literal|null
argument_list|)
init|)
block|{
name|cleaner
operator|.
name|setConf
argument_list|(
name|conf
argument_list|,
name|zkw
argument_list|)
expr_stmt|;
name|cleaner
operator|.
name|preClean
argument_list|()
expr_stmt|;
name|Iterable
argument_list|<
name|FileStatus
argument_list|>
name|filesToDelete
init|=
name|cleaner
operator|.
name|getDeletableFiles
argument_list|(
name|dummyFiles
argument_list|)
decl_stmt|;
name|Iterator
argument_list|<
name|FileStatus
argument_list|>
name|iter
init|=
name|filesToDelete
operator|.
name|iterator
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
name|iter
operator|.
name|hasNext
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
operator|new
name|Path
argument_list|(
literal|"log1"
argument_list|)
argument_list|,
name|iter
operator|.
name|next
argument_list|()
operator|.
name|getPath
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|iter
operator|.
name|hasNext
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
operator|new
name|Path
argument_list|(
literal|"log2"
argument_list|)
argument_list|,
name|iter
operator|.
name|next
argument_list|()
operator|.
name|getPath
argument_list|()
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|iter
operator|.
name|hasNext
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
specifier|static
class|class
name|DummyServer
implements|implements
name|Server
block|{
annotation|@
name|Override
specifier|public
name|Configuration
name|getConfiguration
parameter_list|()
block|{
return|return
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|ZKWatcher
name|getZooKeeper
parameter_list|()
block|{
try|try
block|{
return|return
operator|new
name|ZKWatcher
argument_list|(
name|getConfiguration
argument_list|()
argument_list|,
literal|"dummy server"
argument_list|,
name|this
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
block|}
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|CoordinatedStateManager
name|getCoordinatedStateManager
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|ClusterConnection
name|getConnection
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|MetaTableLocator
name|getMetaTableLocator
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|ServerName
name|getServerName
parameter_list|()
block|{
return|return
name|ServerName
operator|.
name|valueOf
argument_list|(
literal|"regionserver,60020,000000"
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|abort
parameter_list|(
name|String
name|why
parameter_list|,
name|Throwable
name|e
parameter_list|)
block|{}
annotation|@
name|Override
specifier|public
name|boolean
name|isAborted
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|stop
parameter_list|(
name|String
name|why
parameter_list|)
block|{}
annotation|@
name|Override
specifier|public
name|boolean
name|isStopped
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
annotation|@
name|Override
specifier|public
name|ChoreService
name|getChoreService
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|ClusterConnection
name|getClusterConnection
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|FileSystem
name|getFileSystem
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isStopping
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
annotation|@
name|Override
specifier|public
name|Connection
name|createConnection
parameter_list|(
name|Configuration
name|conf
parameter_list|)
throws|throws
name|IOException
block|{
return|return
literal|null
return|;
block|}
block|}
specifier|static
class|class
name|FaultyZooKeeperWatcher
extends|extends
name|ZKWatcher
block|{
specifier|private
name|RecoverableZooKeeper
name|zk
decl_stmt|;
specifier|public
name|FaultyZooKeeperWatcher
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|String
name|identifier
parameter_list|,
name|Abortable
name|abortable
parameter_list|)
throws|throws
name|ZooKeeperConnectionException
throws|,
name|IOException
block|{
name|super
argument_list|(
name|conf
argument_list|,
name|identifier
argument_list|,
name|abortable
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|init
parameter_list|()
throws|throws
name|Exception
block|{
name|this
operator|.
name|zk
operator|=
name|spy
argument_list|(
name|super
operator|.
name|getRecoverableZooKeeper
argument_list|()
argument_list|)
expr_stmt|;
name|doThrow
argument_list|(
operator|new
name|KeeperException
operator|.
name|ConnectionLossException
argument_list|()
argument_list|)
operator|.
name|when
argument_list|(
name|zk
argument_list|)
operator|.
name|getData
argument_list|(
literal|"/hbase/replication/rs"
argument_list|,
literal|null
argument_list|,
operator|new
name|Stat
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|public
name|RecoverableZooKeeper
name|getRecoverableZooKeeper
parameter_list|()
block|{
return|return
name|zk
return|;
block|}
block|}
block|}
end_class

end_unit

