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
name|NamespaceDescriptor
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
name|JVMClusterUtil
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
name|Before
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
name|TestReplicationDroppedTables
extends|extends
name|TestReplicationBase
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
name|TestReplicationDroppedTables
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
name|TestReplicationDroppedTables
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|ROWS_COUNT
init|=
literal|1000
decl_stmt|;
annotation|@
name|Before
specifier|public
name|void
name|setUpBase
parameter_list|()
throws|throws
name|Exception
block|{
comment|// Starting and stopping replication can make us miss new logs,
comment|// rolling like this makes sure the most recent one gets added to the queue
for|for
control|(
name|JVMClusterUtil
operator|.
name|RegionServerThread
name|r
range|:
name|utility1
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|getRegionServerThreads
argument_list|()
control|)
block|{
name|utility1
operator|.
name|getAdmin
argument_list|()
operator|.
name|rollWALWriter
argument_list|(
name|r
operator|.
name|getRegionServer
argument_list|()
operator|.
name|getServerName
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|// Initialize the peer after wal rolling, so that we will abandon the stuck WALs.
name|super
operator|.
name|setUpBase
argument_list|()
expr_stmt|;
name|int
name|rowCount
init|=
name|utility1
operator|.
name|countRows
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
name|utility1
operator|.
name|deleteTableData
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
comment|// truncating the table will send one Delete per row to the slave cluster
comment|// in an async fashion, which is why we cannot just call deleteTableData on
comment|// utility2 since late writes could make it to the slave in some way.
comment|// Instead, we truncate the first table and wait for all the Deletes to
comment|// make it to the slave.
name|Scan
name|scan
init|=
operator|new
name|Scan
argument_list|()
decl_stmt|;
name|int
name|lastCount
init|=
literal|0
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
literal|"Waited too much time for truncate"
argument_list|)
expr_stmt|;
block|}
name|ResultScanner
name|scanner
init|=
name|htable2
operator|.
name|getScanner
argument_list|(
name|scan
argument_list|)
decl_stmt|;
name|Result
index|[]
name|res
init|=
name|scanner
operator|.
name|next
argument_list|(
name|rowCount
argument_list|)
decl_stmt|;
name|scanner
operator|.
name|close
argument_list|()
expr_stmt|;
if|if
condition|(
name|res
operator|.
name|length
operator|!=
literal|0
condition|)
block|{
if|if
condition|(
name|res
operator|.
name|length
operator|<
name|lastCount
condition|)
block|{
name|i
operator|--
expr_stmt|;
comment|// Don't increment timeout if we make progress
block|}
name|lastCount
operator|=
name|res
operator|.
name|length
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Still got "
operator|+
name|res
operator|.
name|length
operator|+
literal|" rows"
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
comment|// Set the max request size to a tiny 10K for dividing the replication WAL entries into multiple
comment|// batches. the default max request size is 256M, so all replication entries are in a batch, but
comment|// when replicate at sink side, it'll apply to rs group by table name, so the WAL of test table
comment|// may apply first, and then test_dropped table, and we will believe that the replication is not
comment|// got stuck (HBASE-20475).
name|conf1
operator|.
name|setInt
argument_list|(
name|RpcServer
operator|.
name|MAX_REQUEST_SIZE
argument_list|,
literal|10
operator|*
literal|1024
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testEditsStuckBehindDroppedTable
parameter_list|()
throws|throws
name|Exception
block|{
comment|// Sanity check Make sure by default edits for dropped tables stall the replication queue, even
comment|// when the table(s) in question have been deleted on both ends.
name|testEditsBehindDroppedTable
argument_list|(
literal|false
argument_list|,
literal|"test_dropped"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testEditsDroppedWithDroppedTable
parameter_list|()
throws|throws
name|Exception
block|{
comment|// Make sure by default edits for dropped tables are themselves dropped when the
comment|// table(s) in question have been deleted on both ends.
name|testEditsBehindDroppedTable
argument_list|(
literal|true
argument_list|,
literal|"test_dropped"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testEditsDroppedWithDroppedTableNS
parameter_list|()
throws|throws
name|Exception
block|{
comment|// also try with a namespace
name|Connection
name|connection1
init|=
name|ConnectionFactory
operator|.
name|createConnection
argument_list|(
name|conf1
argument_list|)
decl_stmt|;
try|try
init|(
name|Admin
name|admin1
init|=
name|connection1
operator|.
name|getAdmin
argument_list|()
init|)
block|{
name|admin1
operator|.
name|createNamespace
argument_list|(
name|NamespaceDescriptor
operator|.
name|create
argument_list|(
literal|"NS"
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|Connection
name|connection2
init|=
name|ConnectionFactory
operator|.
name|createConnection
argument_list|(
name|conf2
argument_list|)
decl_stmt|;
try|try
init|(
name|Admin
name|admin2
init|=
name|connection2
operator|.
name|getAdmin
argument_list|()
init|)
block|{
name|admin2
operator|.
name|createNamespace
argument_list|(
name|NamespaceDescriptor
operator|.
name|create
argument_list|(
literal|"NS"
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|testEditsBehindDroppedTable
argument_list|(
literal|true
argument_list|,
literal|"NS:test_dropped"
argument_list|)
expr_stmt|;
try|try
init|(
name|Admin
name|admin1
init|=
name|connection1
operator|.
name|getAdmin
argument_list|()
init|)
block|{
name|admin1
operator|.
name|deleteNamespace
argument_list|(
literal|"NS"
argument_list|)
expr_stmt|;
block|}
try|try
init|(
name|Admin
name|admin2
init|=
name|connection2
operator|.
name|getAdmin
argument_list|()
init|)
block|{
name|admin2
operator|.
name|deleteNamespace
argument_list|(
literal|"NS"
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|byte
index|[]
name|generateRowKey
parameter_list|(
name|int
name|id
parameter_list|)
block|{
return|return
name|Bytes
operator|.
name|toBytes
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"NormalPut%03d"
argument_list|,
name|id
argument_list|)
argument_list|)
return|;
block|}
specifier|private
name|void
name|testEditsBehindDroppedTable
parameter_list|(
name|boolean
name|allowProceeding
parameter_list|,
name|String
name|tName
parameter_list|)
throws|throws
name|Exception
block|{
name|conf1
operator|.
name|setBoolean
argument_list|(
name|HConstants
operator|.
name|REPLICATION_DROP_ON_DELETED_TABLE_KEY
argument_list|,
name|allowProceeding
argument_list|)
expr_stmt|;
name|conf1
operator|.
name|setInt
argument_list|(
name|HConstants
operator|.
name|REPLICATION_SOURCE_MAXTHREADS_KEY
argument_list|,
literal|1
argument_list|)
expr_stmt|;
comment|// make sure we have a single region server only, so that all
comment|// edits for all tables go there
name|utility1
operator|.
name|shutdownMiniHBaseCluster
argument_list|()
expr_stmt|;
name|utility1
operator|.
name|startMiniHBaseCluster
argument_list|(
literal|1
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|TableName
name|tablename
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
name|tName
argument_list|)
decl_stmt|;
name|byte
index|[]
name|familyName
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"fam"
argument_list|)
decl_stmt|;
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
name|TableDescriptor
name|table
init|=
name|TableDescriptorBuilder
operator|.
name|newBuilder
argument_list|(
name|tablename
argument_list|)
operator|.
name|setColumnFamily
argument_list|(
name|ColumnFamilyDescriptorBuilder
operator|.
name|newBuilder
argument_list|(
name|familyName
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
name|Connection
name|connection1
init|=
name|ConnectionFactory
operator|.
name|createConnection
argument_list|(
name|conf1
argument_list|)
decl_stmt|;
name|Connection
name|connection2
init|=
name|ConnectionFactory
operator|.
name|createConnection
argument_list|(
name|conf2
argument_list|)
decl_stmt|;
try|try
init|(
name|Admin
name|admin1
init|=
name|connection1
operator|.
name|getAdmin
argument_list|()
init|)
block|{
name|admin1
operator|.
name|createTable
argument_list|(
name|table
argument_list|)
expr_stmt|;
block|}
try|try
init|(
name|Admin
name|admin2
init|=
name|connection2
operator|.
name|getAdmin
argument_list|()
init|)
block|{
name|admin2
operator|.
name|createTable
argument_list|(
name|table
argument_list|)
expr_stmt|;
block|}
name|utility1
operator|.
name|waitUntilAllRegionsAssigned
argument_list|(
name|tablename
argument_list|)
expr_stmt|;
name|utility2
operator|.
name|waitUntilAllRegionsAssigned
argument_list|(
name|tablename
argument_list|)
expr_stmt|;
comment|// now suspend replication
try|try
init|(
name|Admin
name|admin1
init|=
name|connection1
operator|.
name|getAdmin
argument_list|()
init|)
block|{
name|admin1
operator|.
name|disableReplicationPeer
argument_list|(
name|PEER_ID2
argument_list|)
expr_stmt|;
block|}
comment|// put some data (lead with 0 so the edit gets sorted before the other table's edits
comment|// in the replication batch) write a bunch of edits, making sure we fill a batch
try|try
init|(
name|Table
name|droppedTable
init|=
name|connection1
operator|.
name|getTable
argument_list|(
name|tablename
argument_list|)
init|)
block|{
name|byte
index|[]
name|rowKey
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|0
operator|+
literal|" put on table to be dropped"
argument_list|)
decl_stmt|;
name|Put
name|put
init|=
operator|new
name|Put
argument_list|(
name|rowKey
argument_list|)
decl_stmt|;
name|put
operator|.
name|addColumn
argument_list|(
name|familyName
argument_list|,
name|row
argument_list|,
name|row
argument_list|)
expr_stmt|;
name|droppedTable
operator|.
name|put
argument_list|(
name|put
argument_list|)
expr_stmt|;
block|}
try|try
init|(
name|Table
name|table1
init|=
name|connection1
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
name|ROWS_COUNT
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
name|generateRowKey
argument_list|(
name|i
argument_list|)
argument_list|)
operator|.
name|addColumn
argument_list|(
name|famName
argument_list|,
name|row
argument_list|,
name|row
argument_list|)
decl_stmt|;
name|table1
operator|.
name|put
argument_list|(
name|put
argument_list|)
expr_stmt|;
block|}
block|}
try|try
init|(
name|Admin
name|admin1
init|=
name|connection1
operator|.
name|getAdmin
argument_list|()
init|)
block|{
name|admin1
operator|.
name|disableTable
argument_list|(
name|tablename
argument_list|)
expr_stmt|;
name|admin1
operator|.
name|deleteTable
argument_list|(
name|tablename
argument_list|)
expr_stmt|;
block|}
try|try
init|(
name|Admin
name|admin2
init|=
name|connection2
operator|.
name|getAdmin
argument_list|()
init|)
block|{
name|admin2
operator|.
name|disableTable
argument_list|(
name|tablename
argument_list|)
expr_stmt|;
name|admin2
operator|.
name|deleteTable
argument_list|(
name|tablename
argument_list|)
expr_stmt|;
block|}
try|try
init|(
name|Admin
name|admin1
init|=
name|connection1
operator|.
name|getAdmin
argument_list|()
init|)
block|{
name|admin1
operator|.
name|enableReplicationPeer
argument_list|(
name|PEER_ID2
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|allowProceeding
condition|)
block|{
comment|// in this we'd expect the key to make it over
name|verifyReplicationProceeded
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|verifyReplicationStuck
argument_list|()
expr_stmt|;
block|}
comment|// just to be safe
name|conf1
operator|.
name|setBoolean
argument_list|(
name|HConstants
operator|.
name|REPLICATION_DROP_ON_DELETED_TABLE_KEY
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testEditsBehindDroppedTableTiming
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
name|REPLICATION_DROP_ON_DELETED_TABLE_KEY
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|conf1
operator|.
name|setInt
argument_list|(
name|HConstants
operator|.
name|REPLICATION_SOURCE_MAXTHREADS_KEY
argument_list|,
literal|1
argument_list|)
expr_stmt|;
comment|// make sure we have a single region server only, so that all
comment|// edits for all tables go there
name|utility1
operator|.
name|shutdownMiniHBaseCluster
argument_list|()
expr_stmt|;
name|utility1
operator|.
name|startMiniHBaseCluster
argument_list|(
literal|1
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|TableName
name|tablename
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"testdroppedtimed"
argument_list|)
decl_stmt|;
name|byte
index|[]
name|familyName
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"fam"
argument_list|)
decl_stmt|;
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
name|TableDescriptor
name|table
init|=
name|TableDescriptorBuilder
operator|.
name|newBuilder
argument_list|(
name|tablename
argument_list|)
operator|.
name|setColumnFamily
argument_list|(
name|ColumnFamilyDescriptorBuilder
operator|.
name|newBuilder
argument_list|(
name|familyName
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
name|Connection
name|connection1
init|=
name|ConnectionFactory
operator|.
name|createConnection
argument_list|(
name|conf1
argument_list|)
decl_stmt|;
name|Connection
name|connection2
init|=
name|ConnectionFactory
operator|.
name|createConnection
argument_list|(
name|conf2
argument_list|)
decl_stmt|;
try|try
init|(
name|Admin
name|admin1
init|=
name|connection1
operator|.
name|getAdmin
argument_list|()
init|)
block|{
name|admin1
operator|.
name|createTable
argument_list|(
name|table
argument_list|)
expr_stmt|;
block|}
try|try
init|(
name|Admin
name|admin2
init|=
name|connection2
operator|.
name|getAdmin
argument_list|()
init|)
block|{
name|admin2
operator|.
name|createTable
argument_list|(
name|table
argument_list|)
expr_stmt|;
block|}
name|utility1
operator|.
name|waitUntilAllRegionsAssigned
argument_list|(
name|tablename
argument_list|)
expr_stmt|;
name|utility2
operator|.
name|waitUntilAllRegionsAssigned
argument_list|(
name|tablename
argument_list|)
expr_stmt|;
comment|// now suspend replication
try|try
init|(
name|Admin
name|admin1
init|=
name|connection1
operator|.
name|getAdmin
argument_list|()
init|)
block|{
name|admin1
operator|.
name|disableReplicationPeer
argument_list|(
name|PEER_ID2
argument_list|)
expr_stmt|;
block|}
comment|// put some data (lead with 0 so the edit gets sorted before the other table's edits
comment|// in the replication batch) write a bunch of edits, making sure we fill a batch
try|try
init|(
name|Table
name|droppedTable
init|=
name|connection1
operator|.
name|getTable
argument_list|(
name|tablename
argument_list|)
init|)
block|{
name|byte
index|[]
name|rowKey
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|0
operator|+
literal|" put on table to be dropped"
argument_list|)
decl_stmt|;
name|Put
name|put
init|=
operator|new
name|Put
argument_list|(
name|rowKey
argument_list|)
decl_stmt|;
name|put
operator|.
name|addColumn
argument_list|(
name|familyName
argument_list|,
name|row
argument_list|,
name|row
argument_list|)
expr_stmt|;
name|droppedTable
operator|.
name|put
argument_list|(
name|put
argument_list|)
expr_stmt|;
block|}
try|try
init|(
name|Table
name|table1
init|=
name|connection1
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
name|ROWS_COUNT
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
name|generateRowKey
argument_list|(
name|i
argument_list|)
argument_list|)
operator|.
name|addColumn
argument_list|(
name|famName
argument_list|,
name|row
argument_list|,
name|row
argument_list|)
decl_stmt|;
name|table1
operator|.
name|put
argument_list|(
name|put
argument_list|)
expr_stmt|;
block|}
block|}
try|try
init|(
name|Admin
name|admin2
init|=
name|connection2
operator|.
name|getAdmin
argument_list|()
init|)
block|{
name|admin2
operator|.
name|disableTable
argument_list|(
name|tablename
argument_list|)
expr_stmt|;
name|admin2
operator|.
name|deleteTable
argument_list|(
name|tablename
argument_list|)
expr_stmt|;
block|}
comment|// edit should still be stuck
try|try
init|(
name|Admin
name|admin1
init|=
name|connection1
operator|.
name|getAdmin
argument_list|()
init|)
block|{
comment|// enable the replication peer.
name|admin1
operator|.
name|enableReplicationPeer
argument_list|(
name|PEER_ID2
argument_list|)
expr_stmt|;
comment|// the source table still exists, replication should be stalled
name|verifyReplicationStuck
argument_list|()
expr_stmt|;
name|admin1
operator|.
name|disableTable
argument_list|(
name|tablename
argument_list|)
expr_stmt|;
comment|// still stuck, source table still exists
name|verifyReplicationStuck
argument_list|()
expr_stmt|;
name|admin1
operator|.
name|deleteTable
argument_list|(
name|tablename
argument_list|)
expr_stmt|;
comment|// now the source table is gone, replication should proceed, the
comment|// offending edits be dropped
name|verifyReplicationProceeded
argument_list|()
expr_stmt|;
block|}
comment|// just to be safe
name|conf1
operator|.
name|setBoolean
argument_list|(
name|HConstants
operator|.
name|REPLICATION_DROP_ON_DELETED_TABLE_KEY
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
specifier|private
name|boolean
name|peerHasAllNormalRows
parameter_list|()
throws|throws
name|IOException
block|{
try|try
init|(
name|ResultScanner
name|scanner
init|=
name|htable2
operator|.
name|getScanner
argument_list|(
operator|new
name|Scan
argument_list|()
argument_list|)
init|)
block|{
name|Result
index|[]
name|results
init|=
name|scanner
operator|.
name|next
argument_list|(
name|ROWS_COUNT
argument_list|)
decl_stmt|;
if|if
condition|(
name|results
operator|.
name|length
operator|!=
name|ROWS_COUNT
condition|)
block|{
return|return
literal|false
return|;
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
name|results
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|Assert
operator|.
name|assertArrayEquals
argument_list|(
name|generateRowKey
argument_list|(
name|i
argument_list|)
argument_list|,
name|results
index|[
name|i
index|]
operator|.
name|getRow
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
literal|true
return|;
block|}
block|}
specifier|private
name|void
name|verifyReplicationProceeded
parameter_list|()
throws|throws
name|Exception
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
if|if
condition|(
operator|!
name|peerHasAllNormalRows
argument_list|()
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
break|break;
block|}
block|}
block|}
specifier|private
name|void
name|verifyReplicationStuck
parameter_list|()
throws|throws
name|Exception
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
name|peerHasAllNormalRows
argument_list|()
condition|)
block|{
name|fail
argument_list|(
literal|"Edit should have been stuck behind dropped tables"
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Row not replicated, let's wait a bit more..."
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
block|}
block|}
block|}
end_class

end_unit

