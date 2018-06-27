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
name|ArrayList
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
name|HashSet
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
name|coprocessor
operator|.
name|CoprocessorHost
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
name|TestSourceFSConfigurationProvider
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
name|tool
operator|.
name|LoadIncrementalHFiles
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
name|HFileTestUtil
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
name|TestReplicationSyncUpToolWithBulkLoadedData
extends|extends
name|TestReplicationSyncUpTool
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
name|TestReplicationSyncUpToolWithBulkLoadedData
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
name|TestReplicationSyncUpToolWithBulkLoadedData
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
name|conf1
operator|.
name|setBoolean
argument_list|(
name|HConstants
operator|.
name|REPLICATION_BULKLOAD_ENABLE_KEY
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|conf1
operator|.
name|set
argument_list|(
name|HConstants
operator|.
name|REPLICATION_CLUSTER_ID
argument_list|,
literal|"12345"
argument_list|)
expr_stmt|;
name|conf1
operator|.
name|set
argument_list|(
literal|"hbase.replication.source.fs.conf.provider"
argument_list|,
name|TestSourceFSConfigurationProvider
operator|.
name|class
operator|.
name|getCanonicalName
argument_list|()
argument_list|)
expr_stmt|;
name|String
name|classes
init|=
name|conf1
operator|.
name|get
argument_list|(
name|CoprocessorHost
operator|.
name|REGION_COPROCESSOR_CONF_KEY
argument_list|,
literal|""
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|classes
operator|.
name|contains
argument_list|(
literal|"org.apache.hadoop.hbase.security.access.SecureBulkLoadEndpoint"
argument_list|)
condition|)
block|{
name|classes
operator|=
name|classes
operator|+
literal|",org.apache.hadoop.hbase.security.access.SecureBulkLoadEndpoint"
expr_stmt|;
name|conf1
operator|.
name|set
argument_list|(
name|CoprocessorHost
operator|.
name|REGION_COPROCESSOR_CONF_KEY
argument_list|,
name|classes
argument_list|)
expr_stmt|;
block|}
name|TestReplicationBase
operator|.
name|setUpBeforeClass
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|testSyncUpTool
parameter_list|()
throws|throws
name|Exception
block|{
comment|/**      * Set up Replication: on Master and one Slave Table: t1_syncup and t2_syncup columnfamily:      * 'cf1' : replicated 'norep': not replicated      */
name|setupReplication
argument_list|()
expr_stmt|;
comment|/**      * Prepare 16 random hfile ranges required for creating hfiles      */
name|Iterator
argument_list|<
name|String
argument_list|>
name|randomHFileRangeListIterator
init|=
literal|null
decl_stmt|;
name|Set
argument_list|<
name|String
argument_list|>
name|randomHFileRanges
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|(
literal|16
argument_list|)
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
literal|16
condition|;
name|i
operator|++
control|)
block|{
name|randomHFileRanges
operator|.
name|add
argument_list|(
name|utility1
operator|.
name|getRandomUUID
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|List
argument_list|<
name|String
argument_list|>
name|randomHFileRangeList
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|randomHFileRanges
argument_list|)
decl_stmt|;
name|Collections
operator|.
name|sort
argument_list|(
name|randomHFileRangeList
argument_list|)
expr_stmt|;
name|randomHFileRangeListIterator
operator|=
name|randomHFileRangeList
operator|.
name|iterator
argument_list|()
expr_stmt|;
comment|/**      * at Master: t1_syncup: Load 100 rows into cf1, and 3 rows into norep t2_syncup: Load 200 rows      * into cf1, and 3 rows into norep verify correctly replicated to slave      */
name|loadAndReplicateHFiles
argument_list|(
literal|true
argument_list|,
name|randomHFileRangeListIterator
argument_list|)
expr_stmt|;
comment|/**      * Verify hfile load works step 1: stop hbase on Slave step 2: at Master: t1_syncup: Load      * another 100 rows into cf1 and 3 rows into norep t2_syncup: Load another 200 rows into cf1 and      * 3 rows into norep step 3: stop hbase on master, restart hbase on Slave step 4: verify Slave      * still has the rows before load t1_syncup: 100 rows from cf1 t2_syncup: 200 rows from cf1 step      * 5: run syncup tool on Master step 6: verify that hfiles show up on Slave and 'norep' does not      * t1_syncup: 200 rows from cf1 t2_syncup: 400 rows from cf1 verify correctly replicated to      * Slave      */
name|mimicSyncUpAfterBulkLoad
argument_list|(
name|randomHFileRangeListIterator
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|mimicSyncUpAfterBulkLoad
parameter_list|(
name|Iterator
argument_list|<
name|String
argument_list|>
name|randomHFileRangeListIterator
parameter_list|)
throws|throws
name|Exception
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"mimicSyncUpAfterBulkLoad"
argument_list|)
expr_stmt|;
name|utility2
operator|.
name|shutdownMiniHBaseCluster
argument_list|()
expr_stmt|;
name|loadAndReplicateHFiles
argument_list|(
literal|false
argument_list|,
name|randomHFileRangeListIterator
argument_list|)
expr_stmt|;
name|int
name|rowCount_ht1Source
init|=
name|utility1
operator|.
name|countRows
argument_list|(
name|ht1Source
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"t1_syncup has 206 rows on source, after bulk load of another 103 hfiles"
argument_list|,
literal|206
argument_list|,
name|rowCount_ht1Source
argument_list|)
expr_stmt|;
name|int
name|rowCount_ht2Source
init|=
name|utility1
operator|.
name|countRows
argument_list|(
name|ht2Source
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"t2_syncup has 406 rows on source, after bulk load of another 203 hfiles"
argument_list|,
literal|406
argument_list|,
name|rowCount_ht2Source
argument_list|)
expr_stmt|;
name|utility1
operator|.
name|shutdownMiniHBaseCluster
argument_list|()
expr_stmt|;
name|utility2
operator|.
name|restartHBaseCluster
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|Thread
operator|.
name|sleep
argument_list|(
name|SLEEP_TIME
argument_list|)
expr_stmt|;
comment|// Before sync up
name|int
name|rowCount_ht1TargetAtPeer1
init|=
name|utility2
operator|.
name|countRows
argument_list|(
name|ht1TargetAtPeer1
argument_list|)
decl_stmt|;
name|int
name|rowCount_ht2TargetAtPeer1
init|=
name|utility2
operator|.
name|countRows
argument_list|(
name|ht2TargetAtPeer1
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"@Peer1 t1_syncup should still have 100 rows"
argument_list|,
literal|100
argument_list|,
name|rowCount_ht1TargetAtPeer1
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"@Peer1 t2_syncup should still have 200 rows"
argument_list|,
literal|200
argument_list|,
name|rowCount_ht2TargetAtPeer1
argument_list|)
expr_stmt|;
comment|// Run sync up tool
name|syncUp
argument_list|(
name|utility1
argument_list|)
expr_stmt|;
comment|// After syun up
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|NB_RETRIES
condition|;
name|i
operator|++
control|)
block|{
name|syncUp
argument_list|(
name|utility1
argument_list|)
expr_stmt|;
name|rowCount_ht1TargetAtPeer1
operator|=
name|utility2
operator|.
name|countRows
argument_list|(
name|ht1TargetAtPeer1
argument_list|)
expr_stmt|;
name|rowCount_ht2TargetAtPeer1
operator|=
name|utility2
operator|.
name|countRows
argument_list|(
name|ht2TargetAtPeer1
argument_list|)
expr_stmt|;
if|if
condition|(
name|i
operator|==
name|NB_RETRIES
operator|-
literal|1
condition|)
block|{
if|if
condition|(
name|rowCount_ht1TargetAtPeer1
operator|!=
literal|200
operator|||
name|rowCount_ht2TargetAtPeer1
operator|!=
literal|400
condition|)
block|{
comment|// syncUP still failed. Let's look at the source in case anything wrong there
name|utility1
operator|.
name|restartHBaseCluster
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|rowCount_ht1Source
operator|=
name|utility1
operator|.
name|countRows
argument_list|(
name|ht1Source
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"t1_syncup should have 206 rows at source, and it is "
operator|+
name|rowCount_ht1Source
argument_list|)
expr_stmt|;
name|rowCount_ht2Source
operator|=
name|utility1
operator|.
name|countRows
argument_list|(
name|ht2Source
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"t2_syncup should have 406 rows at source, and it is "
operator|+
name|rowCount_ht2Source
argument_list|)
expr_stmt|;
block|}
name|assertEquals
argument_list|(
literal|"@Peer1 t1_syncup should be sync up and have 200 rows"
argument_list|,
literal|200
argument_list|,
name|rowCount_ht1TargetAtPeer1
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"@Peer1 t2_syncup should be sync up and have 400 rows"
argument_list|,
literal|400
argument_list|,
name|rowCount_ht2TargetAtPeer1
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|rowCount_ht1TargetAtPeer1
operator|==
literal|200
operator|&&
name|rowCount_ht2TargetAtPeer1
operator|==
literal|400
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"SyncUpAfterBulkLoad succeeded at retry = "
operator|+
name|i
argument_list|)
expr_stmt|;
break|break;
block|}
else|else
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"SyncUpAfterBulkLoad failed at retry = "
operator|+
name|i
operator|+
literal|", with rowCount_ht1TargetPeer1 ="
operator|+
name|rowCount_ht1TargetAtPeer1
operator|+
literal|" and rowCount_ht2TargetAtPeer1 ="
operator|+
name|rowCount_ht2TargetAtPeer1
argument_list|)
expr_stmt|;
block|}
name|Thread
operator|.
name|sleep
argument_list|(
name|SLEEP_TIME
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|loadAndReplicateHFiles
parameter_list|(
name|boolean
name|verifyReplicationOnSlave
parameter_list|,
name|Iterator
argument_list|<
name|String
argument_list|>
name|randomHFileRangeListIterator
parameter_list|)
throws|throws
name|Exception
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"loadAndReplicateHFiles"
argument_list|)
expr_stmt|;
comment|// Load 100 + 3 hfiles to t1_syncup.
name|byte
index|[]
index|[]
index|[]
name|hfileRanges
init|=
operator|new
name|byte
index|[]
index|[]
index|[]
block|{
operator|new
name|byte
index|[]
index|[]
block|{
name|Bytes
operator|.
name|toBytes
argument_list|(
name|randomHFileRangeListIterator
operator|.
name|next
argument_list|()
argument_list|)
block|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|randomHFileRangeListIterator
operator|.
name|next
argument_list|()
argument_list|)
block|}
block|}
decl_stmt|;
name|loadAndValidateHFileReplication
argument_list|(
literal|"HFileReplication_1"
argument_list|,
name|row
argument_list|,
name|famName
argument_list|,
name|ht1Source
argument_list|,
name|hfileRanges
argument_list|,
literal|100
argument_list|)
expr_stmt|;
name|hfileRanges
operator|=
operator|new
name|byte
index|[]
index|[]
index|[]
block|{
operator|new
name|byte
index|[]
index|[]
block|{
name|Bytes
operator|.
name|toBytes
argument_list|(
name|randomHFileRangeListIterator
operator|.
name|next
argument_list|()
argument_list|)
block|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|randomHFileRangeListIterator
operator|.
name|next
argument_list|()
argument_list|)
block|}
block|}
expr_stmt|;
name|loadAndValidateHFileReplication
argument_list|(
literal|"HFileReplication_1"
argument_list|,
name|row
argument_list|,
name|noRepfamName
argument_list|,
name|ht1Source
argument_list|,
name|hfileRanges
argument_list|,
literal|3
argument_list|)
expr_stmt|;
comment|// Load 200 + 3 hfiles to t2_syncup.
name|hfileRanges
operator|=
operator|new
name|byte
index|[]
index|[]
index|[]
block|{
operator|new
name|byte
index|[]
index|[]
block|{
name|Bytes
operator|.
name|toBytes
argument_list|(
name|randomHFileRangeListIterator
operator|.
name|next
argument_list|()
argument_list|)
block|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|randomHFileRangeListIterator
operator|.
name|next
argument_list|()
argument_list|)
block|}
block|}
expr_stmt|;
name|loadAndValidateHFileReplication
argument_list|(
literal|"HFileReplication_1"
argument_list|,
name|row
argument_list|,
name|famName
argument_list|,
name|ht2Source
argument_list|,
name|hfileRanges
argument_list|,
literal|200
argument_list|)
expr_stmt|;
name|hfileRanges
operator|=
operator|new
name|byte
index|[]
index|[]
index|[]
block|{
operator|new
name|byte
index|[]
index|[]
block|{
name|Bytes
operator|.
name|toBytes
argument_list|(
name|randomHFileRangeListIterator
operator|.
name|next
argument_list|()
argument_list|)
block|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|randomHFileRangeListIterator
operator|.
name|next
argument_list|()
argument_list|)
block|}
block|}
expr_stmt|;
name|loadAndValidateHFileReplication
argument_list|(
literal|"HFileReplication_1"
argument_list|,
name|row
argument_list|,
name|noRepfamName
argument_list|,
name|ht2Source
argument_list|,
name|hfileRanges
argument_list|,
literal|3
argument_list|)
expr_stmt|;
if|if
condition|(
name|verifyReplicationOnSlave
condition|)
block|{
comment|// ensure replication completed
name|wait
argument_list|(
name|ht1TargetAtPeer1
argument_list|,
name|utility1
operator|.
name|countRows
argument_list|(
name|ht1Source
argument_list|)
operator|-
literal|3
argument_list|,
literal|"t1_syncup has 103 rows on source, and 100 on slave1"
argument_list|)
expr_stmt|;
name|wait
argument_list|(
name|ht2TargetAtPeer1
argument_list|,
name|utility1
operator|.
name|countRows
argument_list|(
name|ht2Source
argument_list|)
operator|-
literal|3
argument_list|,
literal|"t2_syncup has 203 rows on source, and 200 on slave1"
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|loadAndValidateHFileReplication
parameter_list|(
name|String
name|testName
parameter_list|,
name|byte
index|[]
name|row
parameter_list|,
name|byte
index|[]
name|fam
parameter_list|,
name|Table
name|source
parameter_list|,
name|byte
index|[]
index|[]
index|[]
name|hfileRanges
parameter_list|,
name|int
name|numOfRows
parameter_list|)
throws|throws
name|Exception
block|{
name|Path
name|dir
init|=
name|utility1
operator|.
name|getDataTestDirOnTestFS
argument_list|(
name|testName
argument_list|)
decl_stmt|;
name|FileSystem
name|fs
init|=
name|utility1
operator|.
name|getTestFileSystem
argument_list|()
decl_stmt|;
name|dir
operator|=
name|dir
operator|.
name|makeQualified
argument_list|(
name|fs
argument_list|)
expr_stmt|;
name|Path
name|familyDir
init|=
operator|new
name|Path
argument_list|(
name|dir
argument_list|,
name|Bytes
operator|.
name|toString
argument_list|(
name|fam
argument_list|)
argument_list|)
decl_stmt|;
name|int
name|hfileIdx
init|=
literal|0
decl_stmt|;
for|for
control|(
name|byte
index|[]
index|[]
name|range
range|:
name|hfileRanges
control|)
block|{
name|byte
index|[]
name|from
init|=
name|range
index|[
literal|0
index|]
decl_stmt|;
name|byte
index|[]
name|to
init|=
name|range
index|[
literal|1
index|]
decl_stmt|;
name|HFileTestUtil
operator|.
name|createHFile
argument_list|(
name|utility1
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|fs
argument_list|,
operator|new
name|Path
argument_list|(
name|familyDir
argument_list|,
literal|"hfile_"
operator|+
name|hfileIdx
operator|++
argument_list|)
argument_list|,
name|fam
argument_list|,
name|row
argument_list|,
name|from
argument_list|,
name|to
argument_list|,
name|numOfRows
argument_list|)
expr_stmt|;
block|}
specifier|final
name|TableName
name|tableName
init|=
name|source
operator|.
name|getName
argument_list|()
decl_stmt|;
name|LoadIncrementalHFiles
name|loader
init|=
operator|new
name|LoadIncrementalHFiles
argument_list|(
name|utility1
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
name|String
index|[]
name|args
init|=
block|{
name|dir
operator|.
name|toString
argument_list|()
block|,
name|tableName
operator|.
name|toString
argument_list|()
block|}
decl_stmt|;
name|loader
operator|.
name|run
argument_list|(
name|args
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|wait
parameter_list|(
name|Table
name|target
parameter_list|,
name|int
name|expectedCount
parameter_list|,
name|String
name|msg
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
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
name|NB_RETRIES
condition|;
name|i
operator|++
control|)
block|{
name|int
name|rowCount_ht2TargetAtPeer1
init|=
name|utility2
operator|.
name|countRows
argument_list|(
name|target
argument_list|)
decl_stmt|;
if|if
condition|(
name|i
operator|==
name|NB_RETRIES
operator|-
literal|1
condition|)
block|{
name|assertEquals
argument_list|(
name|msg
argument_list|,
name|expectedCount
argument_list|,
name|rowCount_ht2TargetAtPeer1
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|expectedCount
operator|==
name|rowCount_ht2TargetAtPeer1
condition|)
block|{
break|break;
block|}
name|Thread
operator|.
name|sleep
argument_list|(
name|SLEEP_TIME
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

