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
name|assertNotNull
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
name|stream
operator|.
name|Collectors
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|stream
operator|.
name|IntStream
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
name|KeyValue
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
name|Admin
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
name|Get
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
name|RegionInfoBuilder
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
name|master
operator|.
name|HMaster
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
name|master
operator|.
name|procedure
operator|.
name|MasterProcedureEnv
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
name|master
operator|.
name|replication
operator|.
name|RecoverStandbyProcedure
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
name|master
operator|.
name|replication
operator|.
name|SyncReplicationReplayWALManager
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
name|ProcedureExecutor
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
name|regionserver
operator|.
name|wal
operator|.
name|ProtobufLogWriter
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
name|wal
operator|.
name|WALUtil
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
name|ReplicationUtils
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
name|CommonFSUtils
operator|.
name|StreamLacksCapabilityException
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
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|wal
operator|.
name|WALEdit
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
name|WALKeyImpl
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
name|Before
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
name|TestRecoverStandbyProcedure
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
name|TestRecoverStandbyProcedure
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
name|TestRecoverStandbyProcedure
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|TableName
name|tableName
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"TestRecoverStandbyProcedure"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|RegionInfo
name|regionInfo
init|=
name|RegionInfoBuilder
operator|.
name|newBuilder
argument_list|(
name|tableName
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|family
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"CF"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|qualifier
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"q"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|long
name|timestamp
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|ROW_COUNT
init|=
literal|1000
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|WAL_NUMBER
init|=
literal|10
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|RS_NUMBER
init|=
literal|3
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|PEER_ID
init|=
literal|"1"
decl_stmt|;
specifier|private
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
specifier|static
name|SyncReplicationReplayWALManager
name|syncReplicationReplayWALManager
decl_stmt|;
specifier|private
specifier|static
name|ProcedureExecutor
argument_list|<
name|MasterProcedureEnv
argument_list|>
name|procExec
decl_stmt|;
specifier|private
specifier|static
name|FileSystem
name|fs
decl_stmt|;
specifier|private
specifier|static
name|Configuration
name|conf
decl_stmt|;
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|setupCluster
parameter_list|()
throws|throws
name|Exception
block|{
name|UTIL
operator|.
name|startMiniCluster
argument_list|(
name|RS_NUMBER
argument_list|)
expr_stmt|;
name|UTIL
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|waitForActiveAndReadyMaster
argument_list|()
expr_stmt|;
name|conf
operator|=
name|UTIL
operator|.
name|getConfiguration
argument_list|()
expr_stmt|;
name|HMaster
name|master
init|=
name|UTIL
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|getMaster
argument_list|()
decl_stmt|;
name|fs
operator|=
name|master
operator|.
name|getMasterFileSystem
argument_list|()
operator|.
name|getWALFileSystem
argument_list|()
expr_stmt|;
name|syncReplicationReplayWALManager
operator|=
name|master
operator|.
name|getSyncReplicationReplayWALManager
argument_list|()
expr_stmt|;
name|procExec
operator|=
name|master
operator|.
name|getMasterProcedureExecutor
argument_list|()
expr_stmt|;
block|}
annotation|@
name|AfterClass
specifier|public
specifier|static
name|void
name|cleanupTest
parameter_list|()
throws|throws
name|Exception
block|{
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
name|Before
specifier|public
name|void
name|setupBeforeTest
parameter_list|()
throws|throws
name|IOException
block|{
name|UTIL
operator|.
name|createTable
argument_list|(
name|tableName
argument_list|,
name|family
argument_list|)
expr_stmt|;
block|}
annotation|@
name|After
specifier|public
name|void
name|tearDownAfterTest
parameter_list|()
throws|throws
name|IOException
block|{
try|try
init|(
name|Admin
name|admin
init|=
name|UTIL
operator|.
name|getAdmin
argument_list|()
init|)
block|{
if|if
condition|(
name|admin
operator|.
name|isTableEnabled
argument_list|(
name|tableName
argument_list|)
condition|)
block|{
name|admin
operator|.
name|disableTable
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
block|}
name|admin
operator|.
name|deleteTable
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testRecoverStandby
parameter_list|()
throws|throws
name|IOException
throws|,
name|StreamLacksCapabilityException
block|{
name|setupSyncReplicationWALs
argument_list|()
expr_stmt|;
name|long
name|procId
init|=
name|procExec
operator|.
name|submitProcedure
argument_list|(
operator|new
name|RecoverStandbyProcedure
argument_list|(
name|PEER_ID
argument_list|,
literal|false
argument_list|)
argument_list|)
decl_stmt|;
name|ProcedureTestingUtility
operator|.
name|waitProcedure
argument_list|(
name|procExec
argument_list|,
name|procId
argument_list|)
expr_stmt|;
name|ProcedureTestingUtility
operator|.
name|assertProcNotFailed
argument_list|(
name|procExec
argument_list|,
name|procId
argument_list|)
expr_stmt|;
try|try
init|(
name|Table
name|table
init|=
name|UTIL
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
name|WAL_NUMBER
operator|*
name|ROW_COUNT
condition|;
name|i
operator|++
control|)
block|{
name|Result
name|result
init|=
name|table
operator|.
name|get
argument_list|(
operator|new
name|Get
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|i
argument_list|)
argument_list|)
operator|.
name|setTimestamp
argument_list|(
name|timestamp
argument_list|)
argument_list|)
decl_stmt|;
name|assertNotNull
argument_list|(
name|result
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|i
argument_list|,
name|Bytes
operator|.
name|toInt
argument_list|(
name|result
operator|.
name|getValue
argument_list|(
name|family
argument_list|,
name|qualifier
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|private
name|void
name|setupSyncReplicationWALs
parameter_list|()
throws|throws
name|IOException
throws|,
name|StreamLacksCapabilityException
block|{
name|Path
name|peerRemoteWALDir
init|=
name|ReplicationUtils
operator|.
name|getPeerRemoteWALDir
argument_list|(
name|syncReplicationReplayWALManager
operator|.
name|getRemoteWALDir
argument_list|()
argument_list|,
name|PEER_ID
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|fs
operator|.
name|exists
argument_list|(
name|peerRemoteWALDir
argument_list|)
condition|)
block|{
name|fs
operator|.
name|mkdirs
argument_list|(
name|peerRemoteWALDir
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
name|WAL_NUMBER
condition|;
name|i
operator|++
control|)
block|{
try|try
init|(
name|ProtobufLogWriter
name|writer
init|=
operator|new
name|ProtobufLogWriter
argument_list|()
init|)
block|{
name|Path
name|wal
init|=
operator|new
name|Path
argument_list|(
name|peerRemoteWALDir
argument_list|,
literal|"srv1,8888."
operator|+
name|i
operator|+
literal|".syncrep"
argument_list|)
decl_stmt|;
name|writer
operator|.
name|init
argument_list|(
name|fs
argument_list|,
name|wal
argument_list|,
name|conf
argument_list|,
literal|true
argument_list|,
name|WALUtil
operator|.
name|getWALBlockSize
argument_list|(
name|conf
argument_list|,
name|fs
argument_list|,
name|peerRemoteWALDir
argument_list|)
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|Entry
argument_list|>
name|entries
init|=
name|setupWALEntries
argument_list|(
name|i
operator|*
name|ROW_COUNT
argument_list|,
operator|(
name|i
operator|+
literal|1
operator|)
operator|*
name|ROW_COUNT
argument_list|)
decl_stmt|;
for|for
control|(
name|Entry
name|entry
range|:
name|entries
control|)
block|{
name|writer
operator|.
name|append
argument_list|(
name|entry
argument_list|)
expr_stmt|;
block|}
name|writer
operator|.
name|sync
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Created wal {} to replay for peer id={}"
argument_list|,
name|wal
argument_list|,
name|PEER_ID
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|private
name|List
argument_list|<
name|Entry
argument_list|>
name|setupWALEntries
parameter_list|(
name|int
name|startRow
parameter_list|,
name|int
name|endRow
parameter_list|)
block|{
return|return
name|IntStream
operator|.
name|range
argument_list|(
name|startRow
argument_list|,
name|endRow
argument_list|)
operator|.
name|mapToObj
argument_list|(
name|i
lambda|->
name|createWALEntry
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|i
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|i
argument_list|)
argument_list|)
argument_list|)
operator|.
name|collect
argument_list|(
name|Collectors
operator|.
name|toList
argument_list|()
argument_list|)
return|;
block|}
specifier|private
name|Entry
name|createWALEntry
parameter_list|(
name|byte
index|[]
name|row
parameter_list|,
name|byte
index|[]
name|value
parameter_list|)
block|{
name|WALKeyImpl
name|key
init|=
operator|new
name|WALKeyImpl
argument_list|(
name|regionInfo
operator|.
name|getEncodedNameAsBytes
argument_list|()
argument_list|,
name|tableName
argument_list|,
literal|1
argument_list|)
decl_stmt|;
name|WALEdit
name|edit
init|=
operator|new
name|WALEdit
argument_list|()
decl_stmt|;
name|edit
operator|.
name|add
argument_list|(
operator|new
name|KeyValue
argument_list|(
name|row
argument_list|,
name|family
argument_list|,
name|qualifier
argument_list|,
name|timestamp
argument_list|,
name|value
argument_list|)
argument_list|)
expr_stmt|;
return|return
operator|new
name|Entry
argument_list|(
name|key
argument_list|,
name|edit
argument_list|)
return|;
block|}
block|}
end_class

end_unit

