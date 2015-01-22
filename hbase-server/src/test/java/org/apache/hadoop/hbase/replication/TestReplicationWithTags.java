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
name|assertArrayEquals
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
name|ArrayList
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
name|KeyValueUtil
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
name|Tag
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
name|ConnectionFactory
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
name|Durability
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
name|client
operator|.
name|HTable
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
name|codec
operator|.
name|KeyValueCodecWithTags
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
name|regionserver
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
name|TestReplicationWithTags
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
name|TestReplicationWithTags
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
name|TAG_TYPE
init|=
literal|1
decl_stmt|;
specifier|private
specifier|static
name|Configuration
name|conf1
init|=
name|HBaseConfiguration
operator|.
name|create
argument_list|()
decl_stmt|;
specifier|private
specifier|static
name|Configuration
name|conf2
decl_stmt|;
specifier|private
specifier|static
name|ReplicationAdmin
name|replicationAdmin
decl_stmt|;
specifier|private
specifier|static
name|Connection
name|connection1
decl_stmt|;
specifier|private
specifier|static
name|Connection
name|connection2
decl_stmt|;
specifier|private
specifier|static
name|Table
name|htable1
decl_stmt|;
specifier|private
specifier|static
name|Table
name|htable2
decl_stmt|;
specifier|private
specifier|static
name|HBaseTestingUtility
name|utility1
decl_stmt|;
specifier|private
specifier|static
name|HBaseTestingUtility
name|utility2
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|long
name|SLEEP_TIME
init|=
literal|500
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|NB_RETRIES
init|=
literal|10
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|TableName
name|TABLE_NAME
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"TestReplicationWithTags"
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
literal|"f"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|ROW
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"row"
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
name|setInt
argument_list|(
literal|"hfile.format.version"
argument_list|,
literal|3
argument_list|)
expr_stmt|;
name|conf1
operator|.
name|set
argument_list|(
name|HConstants
operator|.
name|ZOOKEEPER_ZNODE_PARENT
argument_list|,
literal|"/1"
argument_list|)
expr_stmt|;
name|conf1
operator|.
name|setInt
argument_list|(
literal|"replication.source.size.capacity"
argument_list|,
literal|10240
argument_list|)
expr_stmt|;
name|conf1
operator|.
name|setLong
argument_list|(
literal|"replication.source.sleepforretries"
argument_list|,
literal|100
argument_list|)
expr_stmt|;
name|conf1
operator|.
name|setInt
argument_list|(
literal|"hbase.regionserver.maxlogs"
argument_list|,
literal|10
argument_list|)
expr_stmt|;
name|conf1
operator|.
name|setLong
argument_list|(
literal|"hbase.master.logcleaner.ttl"
argument_list|,
literal|10
argument_list|)
expr_stmt|;
name|conf1
operator|.
name|setInt
argument_list|(
literal|"zookeeper.recovery.retry"
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|conf1
operator|.
name|setInt
argument_list|(
literal|"zookeeper.recovery.retry.intervalmill"
argument_list|,
literal|10
argument_list|)
expr_stmt|;
name|conf1
operator|.
name|setBoolean
argument_list|(
literal|"dfs.support.append"
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|conf1
operator|.
name|setLong
argument_list|(
name|HConstants
operator|.
name|THREAD_WAKE_FREQUENCY
argument_list|,
literal|100
argument_list|)
expr_stmt|;
name|conf1
operator|.
name|setInt
argument_list|(
literal|"replication.stats.thread.period.seconds"
argument_list|,
literal|5
argument_list|)
expr_stmt|;
name|conf1
operator|.
name|setBoolean
argument_list|(
literal|"hbase.tests.use.shortcircuit.reads"
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|conf1
operator|.
name|setStrings
argument_list|(
name|HConstants
operator|.
name|REPLICATION_CODEC_CONF_KEY
argument_list|,
name|KeyValueCodecWithTags
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|conf1
operator|.
name|setStrings
argument_list|(
name|CoprocessorHost
operator|.
name|USER_REGION_COPROCESSOR_CONF_KEY
argument_list|,
name|TestCoprocessorForTagsAtSource
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|utility1
operator|=
operator|new
name|HBaseTestingUtility
argument_list|(
name|conf1
argument_list|)
expr_stmt|;
name|utility1
operator|.
name|startMiniZKCluster
argument_list|()
expr_stmt|;
name|MiniZooKeeperCluster
name|miniZK
init|=
name|utility1
operator|.
name|getZkCluster
argument_list|()
decl_stmt|;
comment|// Have to reget conf1 in case zk cluster location different
comment|// than default
name|conf1
operator|=
name|utility1
operator|.
name|getConfiguration
argument_list|()
expr_stmt|;
name|replicationAdmin
operator|=
operator|new
name|ReplicationAdmin
argument_list|(
name|conf1
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Setup first Zk"
argument_list|)
expr_stmt|;
comment|// Base conf2 on conf1 so it gets the right zk cluster.
name|conf2
operator|=
name|HBaseConfiguration
operator|.
name|create
argument_list|(
name|conf1
argument_list|)
expr_stmt|;
name|conf2
operator|.
name|setInt
argument_list|(
literal|"hfile.format.version"
argument_list|,
literal|3
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
name|conf2
operator|.
name|setInt
argument_list|(
name|HConstants
operator|.
name|HBASE_CLIENT_RETRIES_NUMBER
argument_list|,
literal|6
argument_list|)
expr_stmt|;
name|conf2
operator|.
name|setBoolean
argument_list|(
literal|"dfs.support.append"
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|conf2
operator|.
name|setBoolean
argument_list|(
literal|"hbase.tests.use.shortcircuit.reads"
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|conf2
operator|.
name|setStrings
argument_list|(
name|HConstants
operator|.
name|REPLICATION_CODEC_CONF_KEY
argument_list|,
name|KeyValueCodecWithTags
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|conf2
operator|.
name|setStrings
argument_list|(
name|CoprocessorHost
operator|.
name|USER_REGION_COPROCESSOR_CONF_KEY
argument_list|,
name|TestCoprocessorForTagsAtSink
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|utility2
operator|=
operator|new
name|HBaseTestingUtility
argument_list|(
name|conf2
argument_list|)
expr_stmt|;
name|utility2
operator|.
name|setZkCluster
argument_list|(
name|miniZK
argument_list|)
expr_stmt|;
name|replicationAdmin
operator|.
name|addPeer
argument_list|(
literal|"2"
argument_list|,
name|utility2
operator|.
name|getClusterKey
argument_list|()
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Setup second Zk"
argument_list|)
expr_stmt|;
name|utility1
operator|.
name|startMiniCluster
argument_list|(
literal|2
argument_list|)
expr_stmt|;
name|utility2
operator|.
name|startMiniCluster
argument_list|(
literal|2
argument_list|)
expr_stmt|;
name|HTableDescriptor
name|table
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|TABLE_NAME
argument_list|)
decl_stmt|;
name|HColumnDescriptor
name|fam
init|=
operator|new
name|HColumnDescriptor
argument_list|(
name|FAMILY
argument_list|)
decl_stmt|;
name|fam
operator|.
name|setMaxVersions
argument_list|(
literal|3
argument_list|)
expr_stmt|;
name|fam
operator|.
name|setScope
argument_list|(
name|HConstants
operator|.
name|REPLICATION_SCOPE_GLOBAL
argument_list|)
expr_stmt|;
name|table
operator|.
name|addFamily
argument_list|(
name|fam
argument_list|)
expr_stmt|;
try|try
init|(
name|Connection
name|conn
init|=
name|ConnectionFactory
operator|.
name|createConnection
argument_list|(
name|conf1
argument_list|)
init|;
name|Admin
name|admin
operator|=
name|conn
operator|.
name|getAdmin
argument_list|()
init|)
block|{
name|admin
operator|.
name|createTable
argument_list|(
name|table
argument_list|,
name|HBaseTestingUtility
operator|.
name|KEYS_FOR_HBA_CREATE_TABLE
argument_list|)
expr_stmt|;
block|}
try|try
init|(
name|Connection
name|conn
init|=
name|ConnectionFactory
operator|.
name|createConnection
argument_list|(
name|conf2
argument_list|)
init|;
name|Admin
name|admin
operator|=
name|conn
operator|.
name|getAdmin
argument_list|()
init|)
block|{
name|admin
operator|.
name|createTable
argument_list|(
name|table
argument_list|,
name|HBaseTestingUtility
operator|.
name|KEYS_FOR_HBA_CREATE_TABLE
argument_list|)
expr_stmt|;
block|}
name|htable1
operator|=
name|utility1
operator|.
name|getConnection
argument_list|()
operator|.
name|getTable
argument_list|(
name|TABLE_NAME
argument_list|)
expr_stmt|;
name|htable2
operator|=
name|utility2
operator|.
name|getConnection
argument_list|()
operator|.
name|getTable
argument_list|(
name|TABLE_NAME
argument_list|)
expr_stmt|;
block|}
comment|/**    * @throws java.lang.Exception    */
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
name|utility2
operator|.
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
name|utility1
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
literal|300000
argument_list|)
specifier|public
name|void
name|testReplicationWithCellTags
parameter_list|()
throws|throws
name|Exception
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"testSimplePutDelete"
argument_list|)
expr_stmt|;
name|Put
name|put
init|=
operator|new
name|Put
argument_list|(
name|ROW
argument_list|)
decl_stmt|;
name|put
operator|.
name|setAttribute
argument_list|(
literal|"visibility"
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"myTag3"
argument_list|)
argument_list|)
expr_stmt|;
name|put
operator|.
name|add
argument_list|(
name|FAMILY
argument_list|,
name|ROW
argument_list|,
name|ROW
argument_list|)
expr_stmt|;
name|htable1
operator|=
name|utility1
operator|.
name|getConnection
argument_list|()
operator|.
name|getTable
argument_list|(
name|TABLE_NAME
argument_list|)
expr_stmt|;
name|htable1
operator|.
name|put
argument_list|(
name|put
argument_list|)
expr_stmt|;
name|Get
name|get
init|=
operator|new
name|Get
argument_list|(
name|ROW
argument_list|)
decl_stmt|;
try|try
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
if|if
condition|(
name|i
operator|==
name|NB_RETRIES
operator|-
literal|1
condition|)
block|{
name|fail
argument_list|(
literal|"Waited too much time for put replication"
argument_list|)
expr_stmt|;
block|}
name|Result
name|res
init|=
name|htable2
operator|.
name|get
argument_list|(
name|get
argument_list|)
decl_stmt|;
if|if
condition|(
name|res
operator|.
name|size
argument_list|()
operator|==
literal|0
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Row not available"
argument_list|)
expr_stmt|;
name|Thread
operator|.
name|sleep
argument_list|(
name|SLEEP_TIME
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|assertArrayEquals
argument_list|(
name|res
operator|.
name|value
argument_list|()
argument_list|,
name|ROW
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|TestCoprocessorForTagsAtSink
operator|.
name|tags
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|Tag
name|tag
init|=
name|TestCoprocessorForTagsAtSink
operator|.
name|tags
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|TAG_TYPE
argument_list|,
name|tag
operator|.
name|getType
argument_list|()
argument_list|)
expr_stmt|;
break|break;
block|}
block|}
block|}
finally|finally
block|{
name|TestCoprocessorForTagsAtSink
operator|.
name|tags
operator|=
literal|null
expr_stmt|;
block|}
block|}
specifier|public
specifier|static
class|class
name|TestCoprocessorForTagsAtSource
extends|extends
name|BaseRegionObserver
block|{
annotation|@
name|Override
specifier|public
name|void
name|prePut
parameter_list|(
specifier|final
name|ObserverContext
argument_list|<
name|RegionCoprocessorEnvironment
argument_list|>
name|e
parameter_list|,
specifier|final
name|Put
name|put
parameter_list|,
specifier|final
name|WALEdit
name|edit
parameter_list|,
specifier|final
name|Durability
name|durability
parameter_list|)
throws|throws
name|IOException
block|{
name|byte
index|[]
name|attribute
init|=
name|put
operator|.
name|getAttribute
argument_list|(
literal|"visibility"
argument_list|)
decl_stmt|;
name|byte
index|[]
name|cf
init|=
literal|null
decl_stmt|;
name|List
argument_list|<
name|Cell
argument_list|>
name|updatedCells
init|=
operator|new
name|ArrayList
argument_list|<
name|Cell
argument_list|>
argument_list|()
decl_stmt|;
if|if
condition|(
name|attribute
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|List
argument_list|<
name|?
extends|extends
name|Cell
argument_list|>
name|edits
range|:
name|put
operator|.
name|getFamilyCellMap
argument_list|()
operator|.
name|values
argument_list|()
control|)
block|{
for|for
control|(
name|Cell
name|cell
range|:
name|edits
control|)
block|{
name|KeyValue
name|kv
init|=
name|KeyValueUtil
operator|.
name|ensureKeyValue
argument_list|(
name|cell
argument_list|)
decl_stmt|;
if|if
condition|(
name|cf
operator|==
literal|null
condition|)
block|{
name|cf
operator|=
name|kv
operator|.
name|getFamily
argument_list|()
expr_stmt|;
block|}
name|Tag
name|tag
init|=
operator|new
name|Tag
argument_list|(
name|TAG_TYPE
argument_list|,
name|attribute
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Tag
argument_list|>
name|tagList
init|=
operator|new
name|ArrayList
argument_list|<
name|Tag
argument_list|>
argument_list|()
decl_stmt|;
name|tagList
operator|.
name|add
argument_list|(
name|tag
argument_list|)
expr_stmt|;
name|KeyValue
name|newKV
init|=
operator|new
name|KeyValue
argument_list|(
name|kv
operator|.
name|getRow
argument_list|()
argument_list|,
literal|0
argument_list|,
name|kv
operator|.
name|getRowLength
argument_list|()
argument_list|,
name|kv
operator|.
name|getFamily
argument_list|()
argument_list|,
literal|0
argument_list|,
name|kv
operator|.
name|getFamilyLength
argument_list|()
argument_list|,
name|kv
operator|.
name|getQualifier
argument_list|()
argument_list|,
literal|0
argument_list|,
name|kv
operator|.
name|getQualifierLength
argument_list|()
argument_list|,
name|kv
operator|.
name|getTimestamp
argument_list|()
argument_list|,
name|KeyValue
operator|.
name|Type
operator|.
name|codeToType
argument_list|(
name|kv
operator|.
name|getType
argument_list|()
argument_list|)
argument_list|,
name|kv
operator|.
name|getValue
argument_list|()
argument_list|,
literal|0
argument_list|,
name|kv
operator|.
name|getValueLength
argument_list|()
argument_list|,
name|tagList
argument_list|)
decl_stmt|;
operator|(
operator|(
name|List
argument_list|<
name|Cell
argument_list|>
operator|)
name|updatedCells
operator|)
operator|.
name|add
argument_list|(
name|newKV
argument_list|)
expr_stmt|;
block|}
block|}
name|put
operator|.
name|getFamilyCellMap
argument_list|()
operator|.
name|remove
argument_list|(
name|cf
argument_list|)
expr_stmt|;
comment|// Update the family map
name|put
operator|.
name|getFamilyCellMap
argument_list|()
operator|.
name|put
argument_list|(
name|cf
argument_list|,
name|updatedCells
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|public
specifier|static
class|class
name|TestCoprocessorForTagsAtSink
extends|extends
name|BaseRegionObserver
block|{
specifier|public
specifier|static
name|List
argument_list|<
name|Tag
argument_list|>
name|tags
init|=
literal|null
decl_stmt|;
annotation|@
name|Override
specifier|public
name|void
name|postGetOp
parameter_list|(
name|ObserverContext
argument_list|<
name|RegionCoprocessorEnvironment
argument_list|>
name|e
parameter_list|,
name|Get
name|get
parameter_list|,
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
name|results
operator|.
name|size
argument_list|()
operator|>
literal|0
condition|)
block|{
comment|// Check tag presence in the 1st cell in 1st Result
if|if
condition|(
operator|!
name|results
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|Cell
name|cell
init|=
name|results
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|tags
operator|=
name|Tag
operator|.
name|asList
argument_list|(
name|cell
operator|.
name|getTagsArray
argument_list|()
argument_list|,
name|cell
operator|.
name|getTagsOffset
argument_list|()
argument_list|,
name|cell
operator|.
name|getTagsLength
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
block|}
end_class

end_unit

