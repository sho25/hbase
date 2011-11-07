begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Copyright 2011 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|client
operator|.
name|Delete
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
name|zookeeper
operator|.
name|MiniZooKeeperCluster
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
name|Before
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

begin_class
specifier|public
class|class
name|TestMasterReplication
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
name|TestReplication
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|Configuration
name|conf1
decl_stmt|;
specifier|private
name|Configuration
name|conf2
decl_stmt|;
specifier|private
name|Configuration
name|conf3
decl_stmt|;
specifier|private
name|HBaseTestingUtility
name|utility1
decl_stmt|;
specifier|private
name|HBaseTestingUtility
name|utility2
decl_stmt|;
specifier|private
name|HBaseTestingUtility
name|utility3
decl_stmt|;
specifier|private
name|MiniZooKeeperCluster
name|miniZK
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
name|byte
index|[]
name|tableName
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"test"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|famName
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
name|row
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"row"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|row1
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"row1"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|row2
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"row2"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|noRepfamName
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"norep"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|count
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"count"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|put
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"put"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|delete
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"delete"
argument_list|)
decl_stmt|;
specifier|private
name|HTableDescriptor
name|table
decl_stmt|;
annotation|@
name|Before
specifier|public
name|void
name|setUp
parameter_list|()
throws|throws
name|Exception
block|{
name|conf1
operator|=
name|HBaseConfiguration
operator|.
name|create
argument_list|()
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
comment|// smaller block size and capacity to trigger more operations
comment|// and test them
name|conf1
operator|.
name|setInt
argument_list|(
literal|"hbase.regionserver.hlog.blocksize"
argument_list|,
literal|1024
operator|*
literal|20
argument_list|)
expr_stmt|;
name|conf1
operator|.
name|setInt
argument_list|(
literal|"replication.source.size.capacity"
argument_list|,
literal|1024
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
name|setBoolean
argument_list|(
name|HConstants
operator|.
name|REPLICATION_ENABLE_KEY
argument_list|,
literal|true
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
name|setStrings
argument_list|(
name|CoprocessorHost
operator|.
name|USER_REGION_COPROCESSOR_CONF_KEY
argument_list|,
name|CoprocessorCounter
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
name|miniZK
operator|=
name|utility1
operator|.
name|getZkCluster
argument_list|()
expr_stmt|;
comment|// By setting the mini ZK cluster through this method, even though this is
comment|// already utility1's mini ZK cluster, we are telling utility1 not to shut
comment|// the mini ZK cluster when we shut down the HBase cluster.
name|utility1
operator|.
name|setZkCluster
argument_list|(
name|miniZK
argument_list|)
expr_stmt|;
operator|new
name|ZooKeeperWatcher
argument_list|(
name|conf1
argument_list|,
literal|"cluster1"
argument_list|,
literal|null
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|conf2
operator|=
operator|new
name|Configuration
argument_list|(
name|conf1
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
operator|new
name|ZooKeeperWatcher
argument_list|(
name|conf2
argument_list|,
literal|"cluster2"
argument_list|,
literal|null
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|conf3
operator|=
operator|new
name|Configuration
argument_list|(
name|conf1
argument_list|)
expr_stmt|;
name|conf3
operator|.
name|set
argument_list|(
name|HConstants
operator|.
name|ZOOKEEPER_ZNODE_PARENT
argument_list|,
literal|"/3"
argument_list|)
expr_stmt|;
name|utility3
operator|=
operator|new
name|HBaseTestingUtility
argument_list|(
name|conf3
argument_list|)
expr_stmt|;
name|utility3
operator|.
name|setZkCluster
argument_list|(
name|miniZK
argument_list|)
expr_stmt|;
operator|new
name|ZooKeeperWatcher
argument_list|(
name|conf3
argument_list|,
literal|"cluster3"
argument_list|,
literal|null
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|table
operator|=
operator|new
name|HTableDescriptor
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
name|HColumnDescriptor
name|fam
init|=
operator|new
name|HColumnDescriptor
argument_list|(
name|famName
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
name|table
operator|.
name|addFamily
argument_list|(
name|fam
argument_list|)
expr_stmt|;
name|fam
operator|=
operator|new
name|HColumnDescriptor
argument_list|(
name|noRepfamName
argument_list|)
expr_stmt|;
name|table
operator|.
name|addFamily
argument_list|(
name|fam
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
name|miniZK
operator|.
name|shutdown
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
name|testCyclicReplication
parameter_list|()
throws|throws
name|Exception
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"testCyclicReplication"
argument_list|)
expr_stmt|;
name|utility1
operator|.
name|startMiniCluster
argument_list|()
expr_stmt|;
name|utility2
operator|.
name|startMiniCluster
argument_list|()
expr_stmt|;
name|utility3
operator|.
name|startMiniCluster
argument_list|()
expr_stmt|;
name|ReplicationAdmin
name|admin1
init|=
operator|new
name|ReplicationAdmin
argument_list|(
name|conf1
argument_list|)
decl_stmt|;
name|ReplicationAdmin
name|admin2
init|=
operator|new
name|ReplicationAdmin
argument_list|(
name|conf2
argument_list|)
decl_stmt|;
name|ReplicationAdmin
name|admin3
init|=
operator|new
name|ReplicationAdmin
argument_list|(
name|conf3
argument_list|)
decl_stmt|;
operator|new
name|HBaseAdmin
argument_list|(
name|conf1
argument_list|)
operator|.
name|createTable
argument_list|(
name|table
argument_list|)
expr_stmt|;
operator|new
name|HBaseAdmin
argument_list|(
name|conf2
argument_list|)
operator|.
name|createTable
argument_list|(
name|table
argument_list|)
expr_stmt|;
operator|new
name|HBaseAdmin
argument_list|(
name|conf3
argument_list|)
operator|.
name|createTable
argument_list|(
name|table
argument_list|)
expr_stmt|;
name|HTable
name|htable1
init|=
operator|new
name|HTable
argument_list|(
name|conf1
argument_list|,
name|tableName
argument_list|)
decl_stmt|;
name|htable1
operator|.
name|setWriteBufferSize
argument_list|(
literal|1024
argument_list|)
expr_stmt|;
name|HTable
name|htable2
init|=
operator|new
name|HTable
argument_list|(
name|conf2
argument_list|,
name|tableName
argument_list|)
decl_stmt|;
name|htable2
operator|.
name|setWriteBufferSize
argument_list|(
literal|1024
argument_list|)
expr_stmt|;
name|HTable
name|htable3
init|=
operator|new
name|HTable
argument_list|(
name|conf3
argument_list|,
name|tableName
argument_list|)
decl_stmt|;
name|htable3
operator|.
name|setWriteBufferSize
argument_list|(
literal|1024
argument_list|)
expr_stmt|;
name|admin1
operator|.
name|addPeer
argument_list|(
literal|"1"
argument_list|,
name|utility2
operator|.
name|getClusterKey
argument_list|()
argument_list|)
expr_stmt|;
name|admin2
operator|.
name|addPeer
argument_list|(
literal|"1"
argument_list|,
name|utility3
operator|.
name|getClusterKey
argument_list|()
argument_list|)
expr_stmt|;
name|admin3
operator|.
name|addPeer
argument_list|(
literal|"1"
argument_list|,
name|utility1
operator|.
name|getClusterKey
argument_list|()
argument_list|)
expr_stmt|;
comment|// put "row" and wait 'til it got around
name|putAndWait
argument_list|(
name|row
argument_list|,
name|famName
argument_list|,
name|htable1
argument_list|,
name|htable3
argument_list|)
expr_stmt|;
comment|// it should have passed through table2
name|check
argument_list|(
name|row
argument_list|,
name|famName
argument_list|,
name|htable2
argument_list|)
expr_stmt|;
name|putAndWait
argument_list|(
name|row1
argument_list|,
name|famName
argument_list|,
name|htable2
argument_list|,
name|htable1
argument_list|)
expr_stmt|;
name|check
argument_list|(
name|row
argument_list|,
name|famName
argument_list|,
name|htable3
argument_list|)
expr_stmt|;
name|putAndWait
argument_list|(
name|row2
argument_list|,
name|famName
argument_list|,
name|htable3
argument_list|,
name|htable2
argument_list|)
expr_stmt|;
name|check
argument_list|(
name|row
argument_list|,
name|famName
argument_list|,
name|htable1
argument_list|)
expr_stmt|;
name|deleteAndWait
argument_list|(
name|row
argument_list|,
name|htable1
argument_list|,
name|htable3
argument_list|)
expr_stmt|;
name|deleteAndWait
argument_list|(
name|row1
argument_list|,
name|htable2
argument_list|,
name|htable1
argument_list|)
expr_stmt|;
name|deleteAndWait
argument_list|(
name|row2
argument_list|,
name|htable3
argument_list|,
name|htable2
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"Puts were replicated back "
argument_list|,
literal|3
argument_list|,
name|getCount
argument_list|(
name|htable1
argument_list|,
name|put
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"Puts were replicated back "
argument_list|,
literal|3
argument_list|,
name|getCount
argument_list|(
name|htable2
argument_list|,
name|put
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"Puts were replicated back "
argument_list|,
literal|3
argument_list|,
name|getCount
argument_list|(
name|htable3
argument_list|,
name|put
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"Deletes were replicated back "
argument_list|,
literal|3
argument_list|,
name|getCount
argument_list|(
name|htable1
argument_list|,
name|delete
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"Deletes were replicated back "
argument_list|,
literal|3
argument_list|,
name|getCount
argument_list|(
name|htable2
argument_list|,
name|delete
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"Deletes were replicated back "
argument_list|,
literal|3
argument_list|,
name|getCount
argument_list|(
name|htable3
argument_list|,
name|delete
argument_list|)
argument_list|)
expr_stmt|;
name|utility3
operator|.
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
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
comment|/**    * Add a row to a table in each cluster, check it's replicated,    * delete it, check's gone    * Also check the puts and deletes are not replicated back to    * the originating cluster.    */
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|300000
argument_list|)
specifier|public
name|void
name|testSimplePutDelete
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
name|utility1
operator|.
name|startMiniCluster
argument_list|()
expr_stmt|;
name|utility2
operator|.
name|startMiniCluster
argument_list|()
expr_stmt|;
name|ReplicationAdmin
name|admin1
init|=
operator|new
name|ReplicationAdmin
argument_list|(
name|conf1
argument_list|)
decl_stmt|;
name|ReplicationAdmin
name|admin2
init|=
operator|new
name|ReplicationAdmin
argument_list|(
name|conf2
argument_list|)
decl_stmt|;
operator|new
name|HBaseAdmin
argument_list|(
name|conf1
argument_list|)
operator|.
name|createTable
argument_list|(
name|table
argument_list|)
expr_stmt|;
operator|new
name|HBaseAdmin
argument_list|(
name|conf2
argument_list|)
operator|.
name|createTable
argument_list|(
name|table
argument_list|)
expr_stmt|;
name|HTable
name|htable1
init|=
operator|new
name|HTable
argument_list|(
name|conf1
argument_list|,
name|tableName
argument_list|)
decl_stmt|;
name|htable1
operator|.
name|setWriteBufferSize
argument_list|(
literal|1024
argument_list|)
expr_stmt|;
name|HTable
name|htable2
init|=
operator|new
name|HTable
argument_list|(
name|conf2
argument_list|,
name|tableName
argument_list|)
decl_stmt|;
name|htable2
operator|.
name|setWriteBufferSize
argument_list|(
literal|1024
argument_list|)
expr_stmt|;
comment|// set M-M
name|admin1
operator|.
name|addPeer
argument_list|(
literal|"1"
argument_list|,
name|utility2
operator|.
name|getClusterKey
argument_list|()
argument_list|)
expr_stmt|;
name|admin2
operator|.
name|addPeer
argument_list|(
literal|"1"
argument_list|,
name|utility1
operator|.
name|getClusterKey
argument_list|()
argument_list|)
expr_stmt|;
comment|// add rows to both clusters,
comment|// make sure they are both replication
name|putAndWait
argument_list|(
name|row
argument_list|,
name|famName
argument_list|,
name|htable1
argument_list|,
name|htable2
argument_list|)
expr_stmt|;
name|putAndWait
argument_list|(
name|row1
argument_list|,
name|famName
argument_list|,
name|htable2
argument_list|,
name|htable1
argument_list|)
expr_stmt|;
comment|// make sure "row" did not get replicated back.
name|assertEquals
argument_list|(
literal|"Puts were replicated back "
argument_list|,
literal|2
argument_list|,
name|getCount
argument_list|(
name|htable1
argument_list|,
name|put
argument_list|)
argument_list|)
expr_stmt|;
comment|// delete "row" and wait
name|deleteAndWait
argument_list|(
name|row
argument_list|,
name|htable1
argument_list|,
name|htable2
argument_list|)
expr_stmt|;
comment|// make the 2nd cluster replicated back
name|assertEquals
argument_list|(
literal|"Puts were replicated back "
argument_list|,
literal|2
argument_list|,
name|getCount
argument_list|(
name|htable2
argument_list|,
name|put
argument_list|)
argument_list|)
expr_stmt|;
name|deleteAndWait
argument_list|(
name|row1
argument_list|,
name|htable2
argument_list|,
name|htable1
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"Deletes were replicated back "
argument_list|,
literal|2
argument_list|,
name|getCount
argument_list|(
name|htable1
argument_list|,
name|delete
argument_list|)
argument_list|)
expr_stmt|;
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
specifier|private
name|int
name|getCount
parameter_list|(
name|HTable
name|t
parameter_list|,
name|byte
index|[]
name|type
parameter_list|)
throws|throws
name|IOException
block|{
name|Get
name|test
init|=
operator|new
name|Get
argument_list|(
name|row
argument_list|)
decl_stmt|;
name|test
operator|.
name|setAttribute
argument_list|(
literal|"count"
argument_list|,
operator|new
name|byte
index|[]
block|{}
argument_list|)
expr_stmt|;
name|Result
name|res
init|=
name|t
operator|.
name|get
argument_list|(
name|test
argument_list|)
decl_stmt|;
return|return
name|Bytes
operator|.
name|toInt
argument_list|(
name|res
operator|.
name|getValue
argument_list|(
name|count
argument_list|,
name|type
argument_list|)
argument_list|)
return|;
block|}
specifier|private
name|void
name|deleteAndWait
parameter_list|(
name|byte
index|[]
name|row
parameter_list|,
name|HTable
name|source
parameter_list|,
name|HTable
name|target
parameter_list|)
throws|throws
name|Exception
block|{
name|Delete
name|del
init|=
operator|new
name|Delete
argument_list|(
name|row
argument_list|)
decl_stmt|;
name|source
operator|.
name|delete
argument_list|(
name|del
argument_list|)
expr_stmt|;
name|Get
name|get
init|=
operator|new
name|Get
argument_list|(
name|row
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
literal|"Waited too much time for del replication"
argument_list|)
expr_stmt|;
block|}
name|Result
name|res
init|=
name|target
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
operator|>=
literal|1
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Row not deleted"
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
break|break;
block|}
block|}
block|}
specifier|private
name|void
name|check
parameter_list|(
name|byte
index|[]
name|row
parameter_list|,
name|byte
index|[]
name|fam
parameter_list|,
name|HTable
name|t
parameter_list|)
throws|throws
name|IOException
block|{
name|Get
name|get
init|=
operator|new
name|Get
argument_list|(
name|row
argument_list|)
decl_stmt|;
name|Result
name|res
init|=
name|t
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
name|fail
argument_list|(
literal|"Row is missing"
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|putAndWait
parameter_list|(
name|byte
index|[]
name|row
parameter_list|,
name|byte
index|[]
name|fam
parameter_list|,
name|HTable
name|source
parameter_list|,
name|HTable
name|target
parameter_list|)
throws|throws
name|Exception
block|{
name|Put
name|put
init|=
operator|new
name|Put
argument_list|(
name|row
argument_list|)
decl_stmt|;
name|put
operator|.
name|add
argument_list|(
name|fam
argument_list|,
name|row
argument_list|,
name|row
argument_list|)
expr_stmt|;
name|source
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
name|row
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
name|target
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
name|row
argument_list|)
expr_stmt|;
break|break;
block|}
block|}
block|}
comment|/**    * Use a coprocessor to count puts and deletes.    * as KVs would be replicated back with the same timestamp    * there is otherwise no way to count them.    */
specifier|public
specifier|static
class|class
name|CoprocessorCounter
extends|extends
name|BaseRegionObserver
block|{
specifier|private
name|int
name|nCount
init|=
literal|0
decl_stmt|;
specifier|private
name|int
name|nDelete
init|=
literal|0
decl_stmt|;
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
name|boolean
name|writeToWAL
parameter_list|)
throws|throws
name|IOException
block|{
name|nCount
operator|++
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|postDelete
parameter_list|(
specifier|final
name|ObserverContext
argument_list|<
name|RegionCoprocessorEnvironment
argument_list|>
name|c
parameter_list|,
specifier|final
name|Delete
name|delete
parameter_list|,
specifier|final
name|WALEdit
name|edit
parameter_list|,
specifier|final
name|boolean
name|writeToWAL
parameter_list|)
throws|throws
name|IOException
block|{
name|nDelete
operator|++
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|preGet
parameter_list|(
specifier|final
name|ObserverContext
argument_list|<
name|RegionCoprocessorEnvironment
argument_list|>
name|c
parameter_list|,
specifier|final
name|Get
name|get
parameter_list|,
specifier|final
name|List
argument_list|<
name|KeyValue
argument_list|>
name|result
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|get
operator|.
name|getAttribute
argument_list|(
literal|"count"
argument_list|)
operator|!=
literal|null
condition|)
block|{
name|result
operator|.
name|clear
argument_list|()
expr_stmt|;
comment|// order is important!
name|result
operator|.
name|add
argument_list|(
operator|new
name|KeyValue
argument_list|(
name|count
argument_list|,
name|count
argument_list|,
name|delete
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|nDelete
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|result
operator|.
name|add
argument_list|(
operator|new
name|KeyValue
argument_list|(
name|count
argument_list|,
name|count
argument_list|,
name|put
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|nCount
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|c
operator|.
name|bypass
argument_list|()
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit

