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
name|BlockingQueue
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
name|LinkedBlockingQueue
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
name|io
operator|.
name|IOUtils
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
name|common
operator|.
name|collect
operator|.
name|ImmutableList
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
name|common
operator|.
name|collect
operator|.
name|ImmutableMap
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
name|MediumTests
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|TestSerialReplicationEndpoint
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
name|TestSerialReplicationEndpoint
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|HBaseTestingUtility
name|UTIL
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
specifier|private
specifier|static
name|Configuration
name|CONF
decl_stmt|;
specifier|private
specifier|static
name|Connection
name|CONN
decl_stmt|;
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|setUp
parameter_list|()
throws|throws
name|Exception
block|{
name|UTIL
operator|.
name|startMiniCluster
argument_list|()
expr_stmt|;
name|CONF
operator|=
name|UTIL
operator|.
name|getConfiguration
argument_list|()
expr_stmt|;
name|CONF
operator|.
name|setLong
argument_list|(
name|RpcServer
operator|.
name|MAX_REQUEST_SIZE
argument_list|,
literal|102400
argument_list|)
expr_stmt|;
name|CONN
operator|=
name|UTIL
operator|.
name|getConnection
argument_list|()
expr_stmt|;
block|}
annotation|@
name|AfterClass
specifier|public
specifier|static
name|void
name|tearDown
parameter_list|()
throws|throws
name|Exception
block|{
name|IOUtils
operator|.
name|closeQuietly
argument_list|(
name|CONN
argument_list|)
expr_stmt|;
name|UTIL
operator|.
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
block|}
specifier|private
name|String
name|getZKClusterKey
parameter_list|()
block|{
return|return
name|String
operator|.
name|format
argument_list|(
literal|"127.0.0.1:%d:%s"
argument_list|,
name|UTIL
operator|.
name|getZkCluster
argument_list|()
operator|.
name|getClientPort
argument_list|()
argument_list|,
name|CONF
operator|.
name|get
argument_list|(
name|HConstants
operator|.
name|ZOOKEEPER_ZNODE_PARENT
argument_list|)
argument_list|)
return|;
block|}
specifier|private
name|void
name|testHBaseReplicationEndpoint
parameter_list|(
name|String
name|tableNameStr
parameter_list|,
name|String
name|peerId
parameter_list|,
name|boolean
name|isSerial
parameter_list|)
throws|throws
name|IOException
block|{
name|TestEndpoint
operator|.
name|reset
argument_list|()
expr_stmt|;
name|int
name|cellNum
init|=
literal|10000
decl_stmt|;
name|TableName
name|tableName
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
name|tableNameStr
argument_list|)
decl_stmt|;
name|byte
index|[]
name|family
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"f"
argument_list|)
decl_stmt|;
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
name|TableDescriptor
name|td
init|=
name|TableDescriptorBuilder
operator|.
name|newBuilder
argument_list|(
name|tableName
argument_list|)
operator|.
name|setColumnFamily
argument_list|(
name|ColumnFamilyDescriptorBuilder
operator|.
name|newBuilder
argument_list|(
name|family
argument_list|)
operator|.
name|setScope
argument_list|(
name|HConstants
operator|.
name|REPLICATION_SCOPE_GLOBAL
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|UTIL
operator|.
name|createTable
argument_list|(
name|td
argument_list|,
literal|null
argument_list|)
expr_stmt|;
try|try
init|(
name|Admin
name|admin
init|=
name|CONN
operator|.
name|getAdmin
argument_list|()
init|)
block|{
name|ReplicationPeerConfig
name|peerConfig
init|=
name|ReplicationPeerConfig
operator|.
name|newBuilder
argument_list|()
operator|.
name|setClusterKey
argument_list|(
name|getZKClusterKey
argument_list|()
argument_list|)
operator|.
name|setReplicationEndpointImpl
argument_list|(
name|TestEndpoint
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
operator|.
name|setReplicateAllUserTables
argument_list|(
literal|false
argument_list|)
operator|.
name|setSerial
argument_list|(
name|isSerial
argument_list|)
operator|.
name|setTableCFsMap
argument_list|(
name|ImmutableMap
operator|.
name|of
argument_list|(
name|tableName
argument_list|,
name|ImmutableList
operator|.
name|of
argument_list|()
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|admin
operator|.
name|addReplicationPeer
argument_list|(
name|peerId
argument_list|,
name|peerConfig
argument_list|)
expr_stmt|;
block|}
try|try
init|(
name|Table
name|table
init|=
name|CONN
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
name|cellNum
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
name|i
argument_list|)
argument_list|)
operator|.
name|addColumn
argument_list|(
name|family
argument_list|,
name|qualifier
argument_list|,
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|i
argument_list|)
argument_list|)
decl_stmt|;
name|table
operator|.
name|put
argument_list|(
name|put
argument_list|)
expr_stmt|;
block|}
block|}
name|Waiter
operator|.
name|waitFor
argument_list|(
name|CONF
argument_list|,
literal|60000
argument_list|,
parameter_list|()
lambda|->
name|TestEndpoint
operator|.
name|getEntries
argument_list|()
operator|.
name|size
argument_list|()
operator|>=
name|cellNum
argument_list|)
expr_stmt|;
name|int
name|index
init|=
literal|0
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|TestEndpoint
operator|.
name|getEntries
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
name|cellNum
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|isSerial
condition|)
block|{
name|Collections
operator|.
name|sort
argument_list|(
name|TestEndpoint
operator|.
name|getEntries
argument_list|()
argument_list|,
parameter_list|(
name|a
parameter_list|,
name|b
parameter_list|)
lambda|->
block|{
name|long
name|seqA
init|=
name|a
operator|.
name|getKey
argument_list|()
operator|.
name|getSequenceId
argument_list|()
decl_stmt|;
name|long
name|seqB
init|=
name|b
operator|.
name|getKey
argument_list|()
operator|.
name|getSequenceId
argument_list|()
decl_stmt|;
return|return
name|seqA
operator|==
name|seqB
condition|?
literal|0
else|:
operator|(
name|seqA
operator|<
name|seqB
condition|?
operator|-
literal|1
else|:
literal|1
operator|)
return|;
block|}
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|Entry
name|entry
range|:
name|TestEndpoint
operator|.
name|getEntries
argument_list|()
control|)
block|{
name|Assert
operator|.
name|assertEquals
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
operator|.
name|getTableName
argument_list|()
argument_list|,
name|tableName
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|entry
operator|.
name|getEdit
argument_list|()
operator|.
name|getCells
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|Cell
name|cell
init|=
name|entry
operator|.
name|getEdit
argument_list|()
operator|.
name|getCells
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertArrayEquals
argument_list|(
name|Bytes
operator|.
name|copy
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
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|index
argument_list|)
argument_list|)
expr_stmt|;
name|index
operator|++
expr_stmt|;
block|}
name|Assert
operator|.
name|assertEquals
argument_list|(
name|index
argument_list|,
name|cellNum
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testSerialReplicate
parameter_list|()
throws|throws
name|Exception
block|{
name|testHBaseReplicationEndpoint
argument_list|(
literal|"testSerialReplicate"
argument_list|,
literal|"100"
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testParallelReplicate
parameter_list|()
throws|throws
name|Exception
block|{
name|testHBaseReplicationEndpoint
argument_list|(
literal|"testParallelReplicate"
argument_list|,
literal|"101"
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
class|class
name|TestEndpoint
extends|extends
name|HBaseInterClusterReplicationEndpoint
block|{
specifier|private
specifier|final
specifier|static
name|BlockingQueue
argument_list|<
name|Entry
argument_list|>
name|entryQueue
init|=
operator|new
name|LinkedBlockingQueue
argument_list|<>
argument_list|()
decl_stmt|;
specifier|public
specifier|static
name|void
name|reset
parameter_list|()
block|{
name|entryQueue
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
specifier|public
specifier|static
name|List
argument_list|<
name|Entry
argument_list|>
name|getEntries
parameter_list|()
block|{
return|return
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|entryQueue
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|canReplicateToSameCluster
parameter_list|()
block|{
return|return
literal|true
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
parameter_list|,
name|int
name|timeout
parameter_list|)
block|{
return|return
parameter_list|()
lambda|->
block|{
name|entryQueue
operator|.
name|addAll
argument_list|(
name|entries
argument_list|)
expr_stmt|;
return|return
name|ordinal
return|;
block|}
return|;
block|}
annotation|@
name|Override
specifier|public
specifier|synchronized
name|List
argument_list|<
name|ServerName
argument_list|>
name|getRegionServers
parameter_list|()
block|{
comment|// Return multiple server names for endpoint parallel replication.
return|return
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|ImmutableList
operator|.
name|of
argument_list|(
name|ServerName
operator|.
name|valueOf
argument_list|(
literal|"www.example.com"
argument_list|,
literal|12016
argument_list|,
literal|1525245876026L
argument_list|)
argument_list|,
name|ServerName
operator|.
name|valueOf
argument_list|(
literal|"www.example2.com"
argument_list|,
literal|12016
argument_list|,
literal|1525245876026L
argument_list|)
argument_list|,
name|ServerName
operator|.
name|valueOf
argument_list|(
literal|"www.example3.com"
argument_list|,
literal|12016
argument_list|,
literal|1525245876026L
argument_list|)
argument_list|,
name|ServerName
operator|.
name|valueOf
argument_list|(
literal|"www.example4.com"
argument_list|,
literal|12016
argument_list|,
literal|1525245876026L
argument_list|)
argument_list|,
name|ServerName
operator|.
name|valueOf
argument_list|(
literal|"www.example4.com"
argument_list|,
literal|12016
argument_list|,
literal|1525245876026L
argument_list|)
argument_list|)
argument_list|)
return|;
block|}
block|}
block|}
end_class

end_unit

