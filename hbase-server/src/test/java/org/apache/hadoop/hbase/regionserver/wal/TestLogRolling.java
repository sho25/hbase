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
name|regionserver
operator|.
name|wal
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
name|assertNotNull
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
name|EOFException
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
name|HashSet
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
name|concurrent
operator|.
name|atomic
operator|.
name|AtomicBoolean
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
name|ColumnFamilyDescriptorBuilder
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
name|RegionInfo
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
name|TableDescriptor
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
name|TableDescriptorBuilder
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
name|fs
operator|.
name|HFileSystem
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
name|HRegion
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
name|VerySlowRegionServerTests
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
name|FSUtils
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
name|JVMClusterUtil
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
name|AbstractFSWALProvider
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
name|WALFactory
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
name|protocol
operator|.
name|DatanodeInfo
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

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|VerySlowRegionServerTests
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
name|TestLogRolling
extends|extends
name|AbstractTestLogRolling
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
name|TestLogRolling
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|TestLogRolling
operator|.
name|class
argument_list|)
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
comment|// TODO: testLogRollOnDatanodeDeath fails if short circuit reads are on under the hadoop2
comment|// profile. See HBASE-9337 for related issues.
name|System
operator|.
name|setProperty
argument_list|(
literal|"hbase.tests.use.shortcircuit.reads"
argument_list|,
literal|"false"
argument_list|)
expr_stmt|;
comment|/**** configuration for testLogRollOnDatanodeDeath ****/
comment|// lower the namenode& datanode heartbeat so the namenode
comment|// quickly detects datanode failures
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
literal|"dfs.namenode.heartbeat.recheck-interval"
argument_list|,
literal|5000
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setInt
argument_list|(
literal|"dfs.heartbeat.interval"
argument_list|,
literal|1
argument_list|)
expr_stmt|;
comment|// the namenode might still try to choose the recently-dead datanode
comment|// for a pipeline, so try to a new pipeline multiple times
name|conf
operator|.
name|setInt
argument_list|(
literal|"dfs.client.block.write.retries"
argument_list|,
literal|30
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setInt
argument_list|(
literal|"hbase.regionserver.hlog.tolerable.lowreplication"
argument_list|,
literal|2
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setInt
argument_list|(
literal|"hbase.regionserver.hlog.lowreplication.rolllimit"
argument_list|,
literal|3
argument_list|)
expr_stmt|;
name|conf
operator|.
name|set
argument_list|(
name|WALFactory
operator|.
name|WAL_PROVIDER
argument_list|,
literal|"filesystem"
argument_list|)
expr_stmt|;
name|conf
operator|.
name|set
argument_list|(
name|WALFactory
operator|.
name|META_WAL_PROVIDER
argument_list|,
literal|"filesystem"
argument_list|)
expr_stmt|;
name|AbstractTestLogRolling
operator|.
name|setUpBeforeClass
argument_list|()
expr_stmt|;
block|}
name|void
name|batchWriteAndWait
parameter_list|(
name|Table
name|table
parameter_list|,
specifier|final
name|FSHLog
name|log
parameter_list|,
name|int
name|start
parameter_list|,
name|boolean
name|expect
parameter_list|,
name|int
name|timeout
parameter_list|)
throws|throws
name|IOException
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
literal|10
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
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"row"
operator|+
name|String
operator|.
name|format
argument_list|(
literal|"%1$04d"
argument_list|,
operator|(
name|start
operator|+
name|i
operator|)
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
name|put
operator|.
name|addColumn
argument_list|(
name|HConstants
operator|.
name|CATALOG_FAMILY
argument_list|,
literal|null
argument_list|,
name|value
argument_list|)
expr_stmt|;
name|table
operator|.
name|put
argument_list|(
name|put
argument_list|)
expr_stmt|;
block|}
name|Put
name|tmpPut
init|=
operator|new
name|Put
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"tmprow"
argument_list|)
argument_list|)
decl_stmt|;
name|tmpPut
operator|.
name|addColumn
argument_list|(
name|HConstants
operator|.
name|CATALOG_FAMILY
argument_list|,
literal|null
argument_list|,
name|value
argument_list|)
expr_stmt|;
name|long
name|startTime
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|long
name|remaining
init|=
name|timeout
decl_stmt|;
while|while
condition|(
name|remaining
operator|>
literal|0
condition|)
block|{
if|if
condition|(
name|log
operator|.
name|isLowReplicationRollEnabled
argument_list|()
operator|==
name|expect
condition|)
block|{
break|break;
block|}
else|else
block|{
comment|// Trigger calling FSHlog#checkLowReplication()
name|table
operator|.
name|put
argument_list|(
name|tmpPut
argument_list|)
expr_stmt|;
try|try
block|{
name|Thread
operator|.
name|sleep
argument_list|(
literal|200
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
comment|// continue
block|}
name|remaining
operator|=
name|timeout
operator|-
operator|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|-
name|startTime
operator|)
expr_stmt|;
block|}
block|}
block|}
comment|/**    * Tests that logs are rolled upon detecting datanode death Requires an HDFS jar with HDFS-826&    * syncFs() support (HDFS-200)    */
annotation|@
name|Test
specifier|public
name|void
name|testLogRollOnDatanodeDeath
parameter_list|()
throws|throws
name|Exception
block|{
name|TEST_UTIL
operator|.
name|ensureSomeRegionServersAvailable
argument_list|(
literal|2
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"This test requires WAL file replication set to 2."
argument_list|,
name|fs
operator|.
name|getDefaultReplication
argument_list|(
name|TEST_UTIL
operator|.
name|getDataTestDirOnTestFS
argument_list|()
argument_list|)
operator|==
literal|2
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Replication="
operator|+
name|fs
operator|.
name|getDefaultReplication
argument_list|(
name|TEST_UTIL
operator|.
name|getDataTestDirOnTestFS
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|this
operator|.
name|server
operator|=
name|cluster
operator|.
name|getRegionServer
argument_list|(
literal|0
argument_list|)
expr_stmt|;
comment|// Create the test table and open it
name|TableDescriptor
name|desc
init|=
name|TableDescriptorBuilder
operator|.
name|newBuilder
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
name|getName
argument_list|()
argument_list|)
argument_list|)
operator|.
name|setColumnFamily
argument_list|(
name|ColumnFamilyDescriptorBuilder
operator|.
name|of
argument_list|(
name|HConstants
operator|.
name|CATALOG_FAMILY
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|admin
operator|.
name|createTable
argument_list|(
name|desc
argument_list|)
expr_stmt|;
name|Table
name|table
init|=
name|TEST_UTIL
operator|.
name|getConnection
argument_list|()
operator|.
name|getTable
argument_list|(
name|desc
operator|.
name|getTableName
argument_list|()
argument_list|)
decl_stmt|;
name|server
operator|=
name|TEST_UTIL
operator|.
name|getRSForFirstRegionInTable
argument_list|(
name|desc
operator|.
name|getTableName
argument_list|()
argument_list|)
expr_stmt|;
name|RegionInfo
name|region
init|=
name|server
operator|.
name|getRegions
argument_list|(
name|desc
operator|.
name|getTableName
argument_list|()
argument_list|)
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getRegionInfo
argument_list|()
decl_stmt|;
specifier|final
name|FSHLog
name|log
init|=
operator|(
name|FSHLog
operator|)
name|server
operator|.
name|getWAL
argument_list|(
name|region
argument_list|)
decl_stmt|;
specifier|final
name|AtomicBoolean
name|lowReplicationHookCalled
init|=
operator|new
name|AtomicBoolean
argument_list|(
literal|false
argument_list|)
decl_stmt|;
name|log
operator|.
name|registerWALActionsListener
argument_list|(
operator|new
name|WALActionsListener
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|logRollRequested
parameter_list|(
name|boolean
name|lowReplication
parameter_list|)
block|{
if|if
condition|(
name|lowReplication
condition|)
block|{
name|lowReplicationHookCalled
operator|.
name|lazySet
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
block|}
block|}
argument_list|)
expr_stmt|;
comment|// add up the datanode count, to ensure proper replication when we kill 1
comment|// This function is synchronous; when it returns, the dfs cluster is active
comment|// We start 3 servers and then stop 2 to avoid a directory naming conflict
comment|// when we stop/start a namenode later, as mentioned in HBASE-5163
name|List
argument_list|<
name|DataNode
argument_list|>
name|existingNodes
init|=
name|dfsCluster
operator|.
name|getDataNodes
argument_list|()
decl_stmt|;
name|int
name|numDataNodes
init|=
literal|3
decl_stmt|;
name|dfsCluster
operator|.
name|startDataNodes
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|numDataNodes
argument_list|,
literal|true
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|DataNode
argument_list|>
name|allNodes
init|=
name|dfsCluster
operator|.
name|getDataNodes
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
name|allNodes
operator|.
name|size
argument_list|()
operator|-
literal|1
init|;
name|i
operator|>=
literal|0
condition|;
name|i
operator|--
control|)
block|{
if|if
condition|(
name|existingNodes
operator|.
name|contains
argument_list|(
name|allNodes
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
condition|)
block|{
name|dfsCluster
operator|.
name|stopDataNode
argument_list|(
name|i
argument_list|)
expr_stmt|;
block|}
block|}
name|assertTrue
argument_list|(
literal|"DataNodes "
operator|+
name|dfsCluster
operator|.
name|getDataNodes
argument_list|()
operator|.
name|size
argument_list|()
operator|+
literal|" default replication "
operator|+
name|fs
operator|.
name|getDefaultReplication
argument_list|(
name|TEST_UTIL
operator|.
name|getDataTestDirOnTestFS
argument_list|()
argument_list|)
argument_list|,
name|dfsCluster
operator|.
name|getDataNodes
argument_list|()
operator|.
name|size
argument_list|()
operator|>=
name|fs
operator|.
name|getDefaultReplication
argument_list|(
name|TEST_UTIL
operator|.
name|getDataTestDirOnTestFS
argument_list|()
argument_list|)
operator|+
literal|1
argument_list|)
expr_stmt|;
name|writeData
argument_list|(
name|table
argument_list|,
literal|2
argument_list|)
expr_stmt|;
name|long
name|curTime
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"log.getCurrentFileName(): "
operator|+
name|log
operator|.
name|getCurrentFileName
argument_list|()
argument_list|)
expr_stmt|;
name|long
name|oldFilenum
init|=
name|AbstractFSWALProvider
operator|.
name|extractFileNumFromWAL
argument_list|(
name|log
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
literal|"Log should have a timestamp older than now"
argument_list|,
name|curTime
operator|>
name|oldFilenum
operator|&&
name|oldFilenum
operator|!=
operator|-
literal|1
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"The log shouldn't have rolled yet"
argument_list|,
name|oldFilenum
operator|==
name|AbstractFSWALProvider
operator|.
name|extractFileNumFromWAL
argument_list|(
name|log
argument_list|)
argument_list|)
expr_stmt|;
specifier|final
name|DatanodeInfo
index|[]
name|pipeline
init|=
name|log
operator|.
name|getPipeline
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
name|pipeline
operator|.
name|length
operator|==
name|fs
operator|.
name|getDefaultReplication
argument_list|(
name|TEST_UTIL
operator|.
name|getDataTestDirOnTestFS
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
comment|// kill a datanode in the pipeline to force a log roll on the next sync()
comment|// This function is synchronous, when it returns the node is killed.
name|assertTrue
argument_list|(
name|dfsCluster
operator|.
name|stopDataNode
argument_list|(
name|pipeline
index|[
literal|0
index|]
operator|.
name|getName
argument_list|()
argument_list|)
operator|!=
literal|null
argument_list|)
expr_stmt|;
comment|// this write should succeed, but trigger a log roll
name|writeData
argument_list|(
name|table
argument_list|,
literal|2
argument_list|)
expr_stmt|;
name|long
name|newFilenum
init|=
name|AbstractFSWALProvider
operator|.
name|extractFileNumFromWAL
argument_list|(
name|log
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
literal|"Missing datanode should've triggered a log roll"
argument_list|,
name|newFilenum
operator|>
name|oldFilenum
operator|&&
name|newFilenum
operator|>
name|curTime
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"The log rolling hook should have been called with the low replication flag"
argument_list|,
name|lowReplicationHookCalled
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
comment|// write some more log data (this should use a new hdfs_out)
name|writeData
argument_list|(
name|table
argument_list|,
literal|3
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"The log should not roll again."
argument_list|,
name|AbstractFSWALProvider
operator|.
name|extractFileNumFromWAL
argument_list|(
name|log
argument_list|)
operator|==
name|newFilenum
argument_list|)
expr_stmt|;
comment|// kill another datanode in the pipeline, so the replicas will be lower than
comment|// the configured value 2.
name|assertTrue
argument_list|(
name|dfsCluster
operator|.
name|stopDataNode
argument_list|(
name|pipeline
index|[
literal|1
index|]
operator|.
name|getName
argument_list|()
argument_list|)
operator|!=
literal|null
argument_list|)
expr_stmt|;
name|batchWriteAndWait
argument_list|(
name|table
argument_list|,
name|log
argument_list|,
literal|3
argument_list|,
literal|false
argument_list|,
literal|14000
argument_list|)
expr_stmt|;
name|int
name|replication
init|=
name|log
operator|.
name|getLogReplication
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
literal|"LowReplication Roller should've been disabled, current replication="
operator|+
name|replication
argument_list|,
operator|!
name|log
operator|.
name|isLowReplicationRollEnabled
argument_list|()
argument_list|)
expr_stmt|;
name|dfsCluster
operator|.
name|startDataNodes
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
literal|1
argument_list|,
literal|true
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
expr_stmt|;
comment|// Force roll writer. The new log file will have the default replications,
comment|// and the LowReplication Roller will be enabled.
name|log
operator|.
name|rollWriter
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|batchWriteAndWait
argument_list|(
name|table
argument_list|,
name|log
argument_list|,
literal|13
argument_list|,
literal|true
argument_list|,
literal|10000
argument_list|)
expr_stmt|;
name|replication
operator|=
name|log
operator|.
name|getLogReplication
argument_list|()
expr_stmt|;
name|assertTrue
argument_list|(
literal|"New log file should have the default replication instead of "
operator|+
name|replication
argument_list|,
name|replication
operator|==
name|fs
operator|.
name|getDefaultReplication
argument_list|(
name|TEST_UTIL
operator|.
name|getDataTestDirOnTestFS
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"LowReplication Roller should've been enabled"
argument_list|,
name|log
operator|.
name|isLowReplicationRollEnabled
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**    * Test that WAL is rolled when all data nodes in the pipeline have been restarted.    * @throws Exception    */
annotation|@
name|Test
specifier|public
name|void
name|testLogRollOnPipelineRestart
parameter_list|()
throws|throws
name|Exception
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Starting testLogRollOnPipelineRestart"
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"This test requires WAL file replication."
argument_list|,
name|fs
operator|.
name|getDefaultReplication
argument_list|(
name|TEST_UTIL
operator|.
name|getDataTestDirOnTestFS
argument_list|()
argument_list|)
operator|>
literal|1
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Replication="
operator|+
name|fs
operator|.
name|getDefaultReplication
argument_list|(
name|TEST_UTIL
operator|.
name|getDataTestDirOnTestFS
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
comment|// When the hbase:meta table can be opened, the region servers are running
name|Table
name|t
init|=
name|TEST_UTIL
operator|.
name|getConnection
argument_list|()
operator|.
name|getTable
argument_list|(
name|TableName
operator|.
name|META_TABLE_NAME
argument_list|)
decl_stmt|;
try|try
block|{
name|this
operator|.
name|server
operator|=
name|cluster
operator|.
name|getRegionServer
argument_list|(
literal|0
argument_list|)
expr_stmt|;
comment|// Create the test table and open it
name|TableDescriptor
name|desc
init|=
name|TableDescriptorBuilder
operator|.
name|newBuilder
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
name|getName
argument_list|()
argument_list|)
argument_list|)
operator|.
name|setColumnFamily
argument_list|(
name|ColumnFamilyDescriptorBuilder
operator|.
name|of
argument_list|(
name|HConstants
operator|.
name|CATALOG_FAMILY
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|admin
operator|.
name|createTable
argument_list|(
name|desc
argument_list|)
expr_stmt|;
name|Table
name|table
init|=
name|TEST_UTIL
operator|.
name|getConnection
argument_list|()
operator|.
name|getTable
argument_list|(
name|desc
operator|.
name|getTableName
argument_list|()
argument_list|)
decl_stmt|;
name|server
operator|=
name|TEST_UTIL
operator|.
name|getRSForFirstRegionInTable
argument_list|(
name|desc
operator|.
name|getTableName
argument_list|()
argument_list|)
expr_stmt|;
name|RegionInfo
name|region
init|=
name|server
operator|.
name|getRegions
argument_list|(
name|desc
operator|.
name|getTableName
argument_list|()
argument_list|)
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getRegionInfo
argument_list|()
decl_stmt|;
specifier|final
name|WAL
name|log
init|=
name|server
operator|.
name|getWAL
argument_list|(
name|region
argument_list|)
decl_stmt|;
specifier|final
name|List
argument_list|<
name|Path
argument_list|>
name|paths
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
literal|1
argument_list|)
decl_stmt|;
specifier|final
name|List
argument_list|<
name|Integer
argument_list|>
name|preLogRolledCalled
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|paths
operator|.
name|add
argument_list|(
name|AbstractFSWALProvider
operator|.
name|getCurrentFileName
argument_list|(
name|log
argument_list|)
argument_list|)
expr_stmt|;
name|log
operator|.
name|registerWALActionsListener
argument_list|(
operator|new
name|WALActionsListener
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|preLogRoll
parameter_list|(
name|Path
name|oldFile
parameter_list|,
name|Path
name|newFile
parameter_list|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"preLogRoll: oldFile="
operator|+
name|oldFile
operator|+
literal|" newFile="
operator|+
name|newFile
argument_list|)
expr_stmt|;
name|preLogRolledCalled
operator|.
name|add
argument_list|(
operator|new
name|Integer
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|postLogRoll
parameter_list|(
name|Path
name|oldFile
parameter_list|,
name|Path
name|newFile
parameter_list|)
block|{
name|paths
operator|.
name|add
argument_list|(
name|newFile
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
name|writeData
argument_list|(
name|table
argument_list|,
literal|1002
argument_list|)
expr_stmt|;
name|long
name|curTime
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"log.getCurrentFileName()): "
operator|+
name|AbstractFSWALProvider
operator|.
name|getCurrentFileName
argument_list|(
name|log
argument_list|)
argument_list|)
expr_stmt|;
name|long
name|oldFilenum
init|=
name|AbstractFSWALProvider
operator|.
name|extractFileNumFromWAL
argument_list|(
name|log
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
literal|"Log should have a timestamp older than now"
argument_list|,
name|curTime
operator|>
name|oldFilenum
operator|&&
name|oldFilenum
operator|!=
operator|-
literal|1
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"The log shouldn't have rolled yet"
argument_list|,
name|oldFilenum
operator|==
name|AbstractFSWALProvider
operator|.
name|extractFileNumFromWAL
argument_list|(
name|log
argument_list|)
argument_list|)
expr_stmt|;
comment|// roll all datanodes in the pipeline
name|dfsCluster
operator|.
name|restartDataNodes
argument_list|()
expr_stmt|;
name|Thread
operator|.
name|sleep
argument_list|(
literal|1000
argument_list|)
expr_stmt|;
name|dfsCluster
operator|.
name|waitActive
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Data Nodes restarted"
argument_list|)
expr_stmt|;
name|validateData
argument_list|(
name|table
argument_list|,
literal|1002
argument_list|)
expr_stmt|;
comment|// this write should succeed, but trigger a log roll
name|writeData
argument_list|(
name|table
argument_list|,
literal|1003
argument_list|)
expr_stmt|;
name|long
name|newFilenum
init|=
name|AbstractFSWALProvider
operator|.
name|extractFileNumFromWAL
argument_list|(
name|log
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
literal|"Missing datanode should've triggered a log roll"
argument_list|,
name|newFilenum
operator|>
name|oldFilenum
operator|&&
name|newFilenum
operator|>
name|curTime
argument_list|)
expr_stmt|;
name|validateData
argument_list|(
name|table
argument_list|,
literal|1003
argument_list|)
expr_stmt|;
name|writeData
argument_list|(
name|table
argument_list|,
literal|1004
argument_list|)
expr_stmt|;
comment|// roll all datanode again
name|dfsCluster
operator|.
name|restartDataNodes
argument_list|()
expr_stmt|;
name|Thread
operator|.
name|sleep
argument_list|(
literal|1000
argument_list|)
expr_stmt|;
name|dfsCluster
operator|.
name|waitActive
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Data Nodes restarted"
argument_list|)
expr_stmt|;
name|validateData
argument_list|(
name|table
argument_list|,
literal|1004
argument_list|)
expr_stmt|;
comment|// this write should succeed, but trigger a log roll
name|writeData
argument_list|(
name|table
argument_list|,
literal|1005
argument_list|)
expr_stmt|;
comment|// force a log roll to read back and verify previously written logs
name|log
operator|.
name|rollWriter
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"preLogRolledCalled has size of "
operator|+
name|preLogRolledCalled
operator|.
name|size
argument_list|()
argument_list|,
name|preLogRolledCalled
operator|.
name|size
argument_list|()
operator|>=
literal|1
argument_list|)
expr_stmt|;
comment|// read back the data written
name|Set
argument_list|<
name|String
argument_list|>
name|loggedRows
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
decl_stmt|;
name|FSUtils
name|fsUtils
init|=
name|FSUtils
operator|.
name|getInstance
argument_list|(
name|fs
argument_list|,
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|Path
name|p
range|:
name|paths
control|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"recovering lease for "
operator|+
name|p
argument_list|)
expr_stmt|;
name|fsUtils
operator|.
name|recoverFileLease
argument_list|(
operator|(
operator|(
name|HFileSystem
operator|)
name|fs
operator|)
operator|.
name|getBackingFs
argument_list|()
argument_list|,
name|p
argument_list|,
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Reading WAL "
operator|+
name|FSUtils
operator|.
name|getPath
argument_list|(
name|p
argument_list|)
argument_list|)
expr_stmt|;
name|WAL
operator|.
name|Reader
name|reader
init|=
literal|null
decl_stmt|;
try|try
block|{
name|reader
operator|=
name|WALFactory
operator|.
name|createReader
argument_list|(
name|fs
argument_list|,
name|p
argument_list|,
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
expr_stmt|;
name|WAL
operator|.
name|Entry
name|entry
decl_stmt|;
while|while
condition|(
operator|(
name|entry
operator|=
name|reader
operator|.
name|next
argument_list|()
operator|)
operator|!=
literal|null
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"#"
operator|+
name|entry
operator|.
name|getKey
argument_list|()
operator|.
name|getSequenceId
argument_list|()
operator|+
literal|": "
operator|+
name|entry
operator|.
name|getEdit
argument_list|()
operator|.
name|getCells
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|Cell
name|cell
range|:
name|entry
operator|.
name|getEdit
argument_list|()
operator|.
name|getCells
argument_list|()
control|)
block|{
name|loggedRows
operator|.
name|add
argument_list|(
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|cell
operator|.
name|getRowArray
argument_list|()
argument_list|,
name|cell
operator|.
name|getRowOffset
argument_list|()
argument_list|,
name|cell
operator|.
name|getRowLength
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
catch|catch
parameter_list|(
name|EOFException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"EOF reading file "
operator|+
name|FSUtils
operator|.
name|getPath
argument_list|(
name|p
argument_list|)
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
if|if
condition|(
name|reader
operator|!=
literal|null
condition|)
name|reader
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
comment|// verify the written rows are there
name|assertTrue
argument_list|(
name|loggedRows
operator|.
name|contains
argument_list|(
literal|"row1002"
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|loggedRows
operator|.
name|contains
argument_list|(
literal|"row1003"
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|loggedRows
operator|.
name|contains
argument_list|(
literal|"row1004"
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|loggedRows
operator|.
name|contains
argument_list|(
literal|"row1005"
argument_list|)
argument_list|)
expr_stmt|;
comment|// flush all regions
for|for
control|(
name|HRegion
name|r
range|:
name|server
operator|.
name|getOnlineRegionsLocalContext
argument_list|()
control|)
block|{
try|try
block|{
name|r
operator|.
name|flush
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
comment|// This try/catch was added by HBASE-14317. It is needed
comment|// because this issue tightened up the semantic such that
comment|// a failed append could not be followed by a successful
comment|// sync. What is coming out here is a failed sync, a sync
comment|// that used to 'pass'.
name|LOG
operator|.
name|info
argument_list|(
name|e
operator|.
name|toString
argument_list|()
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
name|ResultScanner
name|scanner
init|=
name|table
operator|.
name|getScanner
argument_list|(
operator|new
name|Scan
argument_list|()
argument_list|)
decl_stmt|;
try|try
block|{
for|for
control|(
name|int
name|i
init|=
literal|2
init|;
name|i
operator|<=
literal|5
condition|;
name|i
operator|++
control|)
block|{
name|Result
name|r
init|=
name|scanner
operator|.
name|next
argument_list|()
decl_stmt|;
name|assertNotNull
argument_list|(
name|r
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|r
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"row100"
operator|+
name|i
argument_list|,
name|Bytes
operator|.
name|toString
argument_list|(
name|r
operator|.
name|getRow
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
finally|finally
block|{
name|scanner
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
comment|// verify that no region servers aborted
for|for
control|(
name|JVMClusterUtil
operator|.
name|RegionServerThread
name|rsThread
range|:
name|TEST_UTIL
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|getRegionServerThreads
argument_list|()
control|)
block|{
name|assertFalse
argument_list|(
name|rsThread
operator|.
name|getRegionServer
argument_list|()
operator|.
name|isAborted
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
finally|finally
block|{
if|if
condition|(
name|t
operator|!=
literal|null
condition|)
name|t
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

