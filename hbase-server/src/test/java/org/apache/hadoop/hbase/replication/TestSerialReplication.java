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
name|UncheckedIOException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|UUID
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
name|Waiter
operator|.
name|ExplainingPredicate
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
name|regionserver
operator|.
name|HRegionServer
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
name|hbase
operator|.
name|wal
operator|.
name|WALProvider
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
name|TestSerialReplication
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
name|TestSerialReplication
operator|.
name|class
argument_list|)
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
name|String
name|PEER_ID
init|=
literal|"1"
decl_stmt|;
specifier|private
specifier|static
name|byte
index|[]
name|CF
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
name|byte
index|[]
name|CQ
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"CQ"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|FileSystem
name|FS
decl_stmt|;
specifier|private
specifier|static
name|Path
name|LOG_DIR
decl_stmt|;
specifier|private
specifier|static
name|WALProvider
operator|.
name|Writer
name|WRITER
decl_stmt|;
specifier|public
specifier|static
specifier|final
class|class
name|LocalReplicationEndpoint
extends|extends
name|BaseReplicationEndpoint
block|{
specifier|private
specifier|static
specifier|final
name|UUID
name|PEER_UUID
init|=
name|UUID
operator|.
name|randomUUID
argument_list|()
decl_stmt|;
annotation|@
name|Override
specifier|public
name|UUID
name|getPeerUUID
parameter_list|()
block|{
return|return
name|PEER_UUID
return|;
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
synchronized|synchronized
init|(
name|WRITER
init|)
block|{
try|try
block|{
for|for
control|(
name|Entry
name|entry
range|:
name|replicateContext
operator|.
name|getEntries
argument_list|()
control|)
block|{
name|WRITER
operator|.
name|append
argument_list|(
name|entry
argument_list|)
expr_stmt|;
block|}
name|WRITER
operator|.
name|sync
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|UncheckedIOException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
return|return
literal|true
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|start
parameter_list|()
block|{
name|startAsync
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|stop
parameter_list|()
block|{
name|stopAsync
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|doStart
parameter_list|()
block|{
name|notifyStarted
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|doStop
parameter_list|()
block|{
name|notifyStopped
argument_list|()
expr_stmt|;
block|}
block|}
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
name|UTIL
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setInt
argument_list|(
literal|"replication.source.nb.capacity"
argument_list|,
literal|10
argument_list|)
expr_stmt|;
name|UTIL
operator|.
name|startMiniCluster
argument_list|(
literal|3
argument_list|)
expr_stmt|;
name|LOG_DIR
operator|=
name|UTIL
operator|.
name|getDataTestDirOnTestFS
argument_list|(
literal|"replicated"
argument_list|)
expr_stmt|;
name|FS
operator|=
name|UTIL
operator|.
name|getTestFileSystem
argument_list|()
expr_stmt|;
name|FS
operator|.
name|mkdirs
argument_list|(
name|LOG_DIR
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
name|UTIL
operator|.
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Rule
specifier|public
specifier|final
name|TestName
name|name
init|=
operator|new
name|TestName
argument_list|()
decl_stmt|;
specifier|private
name|Path
name|logPath
decl_stmt|;
annotation|@
name|Before
specifier|public
name|void
name|setUp
parameter_list|()
throws|throws
name|IOException
throws|,
name|StreamLacksCapabilityException
block|{
name|UTIL
operator|.
name|ensureSomeRegionServersAvailable
argument_list|(
literal|3
argument_list|)
expr_stmt|;
name|logPath
operator|=
operator|new
name|Path
argument_list|(
name|LOG_DIR
argument_list|,
name|name
operator|.
name|getMethodName
argument_list|()
argument_list|)
expr_stmt|;
name|WRITER
operator|=
name|WALFactory
operator|.
name|createWALWriter
argument_list|(
name|FS
argument_list|,
name|logPath
argument_list|,
name|UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
expr_stmt|;
comment|// add in disable state, so later when enabling it all sources will start push together.
name|UTIL
operator|.
name|getAdmin
argument_list|()
operator|.
name|addReplicationPeer
argument_list|(
name|PEER_ID
argument_list|,
name|ReplicationPeerConfig
operator|.
name|newBuilder
argument_list|()
operator|.
name|setClusterKey
argument_list|(
literal|"127.0.0.1:2181:/hbase"
argument_list|)
operator|.
name|setReplicationEndpointImpl
argument_list|(
name|LocalReplicationEndpoint
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
operator|.
name|build
argument_list|()
argument_list|,
literal|false
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
name|IOException
block|{
name|UTIL
operator|.
name|getAdmin
argument_list|()
operator|.
name|removeReplicationPeer
argument_list|(
name|PEER_ID
argument_list|)
expr_stmt|;
if|if
condition|(
name|WRITER
operator|!=
literal|null
condition|)
block|{
name|WRITER
operator|.
name|close
argument_list|()
expr_stmt|;
name|WRITER
operator|=
literal|null
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testRegionMove
parameter_list|()
throws|throws
name|Exception
block|{
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
name|UTIL
operator|.
name|getAdmin
argument_list|()
operator|.
name|createTable
argument_list|(
name|TableDescriptorBuilder
operator|.
name|newBuilder
argument_list|(
name|tableName
argument_list|)
operator|.
name|addColumnFamily
argument_list|(
name|ColumnFamilyDescriptorBuilder
operator|.
name|newBuilder
argument_list|(
name|CF
argument_list|)
operator|.
name|setScope
argument_list|(
name|HConstants
operator|.
name|REPLICATION_SCOPE_SERIAL
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
name|UTIL
operator|.
name|waitTableAvailable
argument_list|(
name|tableName
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
literal|100
condition|;
name|i
operator|++
control|)
block|{
name|table
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
name|i
argument_list|)
argument_list|)
operator|.
name|addColumn
argument_list|(
name|CF
argument_list|,
name|CQ
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|i
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
name|RegionInfo
name|region
init|=
name|UTIL
operator|.
name|getAdmin
argument_list|()
operator|.
name|getRegions
argument_list|(
name|tableName
argument_list|)
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|HRegionServer
name|rs
init|=
name|UTIL
operator|.
name|getOtherRegionServer
argument_list|(
name|UTIL
operator|.
name|getRSForFirstRegionInTable
argument_list|(
name|tableName
argument_list|)
argument_list|)
decl_stmt|;
name|UTIL
operator|.
name|getAdmin
argument_list|()
operator|.
name|move
argument_list|(
name|region
operator|.
name|getEncodedNameAsBytes
argument_list|()
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|rs
operator|.
name|getServerName
argument_list|()
operator|.
name|getServerName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|UTIL
operator|.
name|waitFor
argument_list|(
literal|30000
argument_list|,
operator|new
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
operator|!
name|rs
operator|.
name|getRegions
argument_list|(
name|tableName
argument_list|)
operator|.
name|isEmpty
argument_list|()
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
name|region
operator|+
literal|" is still not on "
operator|+
name|rs
return|;
block|}
block|}
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
literal|100
init|;
name|i
operator|<
literal|200
condition|;
name|i
operator|++
control|)
block|{
name|table
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
name|i
argument_list|)
argument_list|)
operator|.
name|addColumn
argument_list|(
name|CF
argument_list|,
name|CQ
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|i
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
name|UTIL
operator|.
name|getAdmin
argument_list|()
operator|.
name|enableReplicationPeer
argument_list|(
name|PEER_ID
argument_list|)
expr_stmt|;
name|UTIL
operator|.
name|waitFor
argument_list|(
literal|30000
argument_list|,
operator|new
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
try|try
init|(
name|WAL
operator|.
name|Reader
name|reader
init|=
name|WALFactory
operator|.
name|createReader
argument_list|(
name|FS
argument_list|,
name|logPath
argument_list|,
name|UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
init|)
block|{
name|int
name|count
init|=
literal|0
decl_stmt|;
while|while
condition|(
name|reader
operator|.
name|next
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|count
operator|++
expr_stmt|;
block|}
return|return
name|count
operator|>=
literal|200
return|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
return|return
literal|false
return|;
block|}
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
literal|"Not enough entries replicated"
return|;
block|}
block|}
argument_list|)
expr_stmt|;
try|try
init|(
name|WAL
operator|.
name|Reader
name|reader
init|=
name|WALFactory
operator|.
name|createReader
argument_list|(
name|UTIL
operator|.
name|getTestFileSystem
argument_list|()
argument_list|,
name|logPath
argument_list|,
name|UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
init|)
block|{
name|long
name|seqId
init|=
operator|-
literal|1L
decl_stmt|;
name|int
name|count
init|=
literal|0
decl_stmt|;
for|for
control|(
name|Entry
name|entry
init|;
condition|;
control|)
block|{
name|entry
operator|=
name|reader
operator|.
name|next
argument_list|()
expr_stmt|;
if|if
condition|(
name|entry
operator|==
literal|null
condition|)
block|{
break|break;
block|}
name|assertTrue
argument_list|(
literal|"Sequence id go backwards from "
operator|+
name|seqId
operator|+
literal|" to "
operator|+
name|entry
operator|.
name|getKey
argument_list|()
operator|.
name|getSequenceId
argument_list|()
argument_list|,
name|entry
operator|.
name|getKey
argument_list|()
operator|.
name|getSequenceId
argument_list|()
operator|>=
name|seqId
argument_list|)
expr_stmt|;
name|count
operator|++
expr_stmt|;
block|}
name|assertEquals
argument_list|(
literal|200
argument_list|,
name|count
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

