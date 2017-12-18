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
name|replication
package|;
end_package

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
name|replication
operator|.
name|regionserver
operator|.
name|ReplicationSyncUp
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
name|TestReplicationSyncUpTool
extends|extends
name|TestReplicationBase
block|{
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
name|TestReplicationSyncUpTool
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|TableName
name|t1_su
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"t1_syncup"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|TableName
name|t2_su
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"t2_syncup"
argument_list|)
decl_stmt|;
specifier|protected
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
literal|"cf1"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|qualName
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"q1"
argument_list|)
decl_stmt|;
specifier|protected
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
name|HTableDescriptor
name|t1_syncupSource
decl_stmt|,
name|t1_syncupTarget
decl_stmt|;
specifier|private
name|HTableDescriptor
name|t2_syncupSource
decl_stmt|,
name|t2_syncupTarget
decl_stmt|;
specifier|protected
name|Table
name|ht1Source
decl_stmt|,
name|ht2Source
decl_stmt|,
name|ht1TargetAtPeer1
decl_stmt|,
name|ht2TargetAtPeer1
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
name|HColumnDescriptor
name|fam
decl_stmt|;
name|t1_syncupSource
operator|=
operator|new
name|HTableDescriptor
argument_list|(
name|t1_su
argument_list|)
expr_stmt|;
name|fam
operator|=
operator|new
name|HColumnDescriptor
argument_list|(
name|famName
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
name|t1_syncupSource
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
name|t1_syncupSource
operator|.
name|addFamily
argument_list|(
name|fam
argument_list|)
expr_stmt|;
name|t1_syncupTarget
operator|=
operator|new
name|HTableDescriptor
argument_list|(
name|t1_su
argument_list|)
expr_stmt|;
name|fam
operator|=
operator|new
name|HColumnDescriptor
argument_list|(
name|famName
argument_list|)
expr_stmt|;
name|t1_syncupTarget
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
name|t1_syncupTarget
operator|.
name|addFamily
argument_list|(
name|fam
argument_list|)
expr_stmt|;
name|t2_syncupSource
operator|=
operator|new
name|HTableDescriptor
argument_list|(
name|t2_su
argument_list|)
expr_stmt|;
name|fam
operator|=
operator|new
name|HColumnDescriptor
argument_list|(
name|famName
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
name|t2_syncupSource
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
name|t2_syncupSource
operator|.
name|addFamily
argument_list|(
name|fam
argument_list|)
expr_stmt|;
name|t2_syncupTarget
operator|=
operator|new
name|HTableDescriptor
argument_list|(
name|t2_su
argument_list|)
expr_stmt|;
name|fam
operator|=
operator|new
name|HColumnDescriptor
argument_list|(
name|famName
argument_list|)
expr_stmt|;
name|t2_syncupTarget
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
name|t2_syncupTarget
operator|.
name|addFamily
argument_list|(
name|fam
argument_list|)
expr_stmt|;
block|}
comment|/**    * Add a row to a table in each cluster, check it's replicated, delete it,    * check's gone Also check the puts and deletes are not replicated back to    * the originating cluster.    */
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|300000
argument_list|)
specifier|public
name|void
name|testSyncUpTool
parameter_list|()
throws|throws
name|Exception
block|{
comment|/**      * Set up Replication: on Master and one Slave      * Table: t1_syncup and t2_syncup      * columnfamily:      *    'cf1'  : replicated      *    'norep': not replicated      */
name|setupReplication
argument_list|()
expr_stmt|;
comment|/**      * at Master:      * t1_syncup: put 100 rows into cf1, and 1 rows into norep      * t2_syncup: put 200 rows into cf1, and 1 rows into norep      *      * verify correctly replicated to slave      */
name|putAndReplicateRows
argument_list|()
expr_stmt|;
comment|/**      * Verify delete works      *      * step 1: stop hbase on Slave      *      * step 2: at Master:      *  t1_syncup: delete 50 rows  from cf1      *  t2_syncup: delete 100 rows from cf1      *  no change on 'norep'      *      * step 3: stop hbase on master, restart hbase on Slave      *      * step 4: verify Slave still have the rows before delete      *      t1_syncup: 100 rows from cf1      *      t2_syncup: 200 rows from cf1      *      * step 5: run syncup tool on Master      *      * step 6: verify that delete show up on Slave      *      t1_syncup: 50 rows from cf1      *      t2_syncup: 100 rows from cf1      *      * verify correctly replicated to Slave      */
name|mimicSyncUpAfterDelete
argument_list|()
expr_stmt|;
comment|/**      * Verify put works      *      * step 1: stop hbase on Slave      *      * step 2: at Master:      *  t1_syncup: put 100 rows  from cf1      *  t2_syncup: put 200 rows  from cf1      *  and put another row on 'norep'      *  ATTN: put to 'cf1' will overwrite existing rows, so end count will      *        be 100 and 200 respectively      *      put to 'norep' will add a new row.      *      * step 3: stop hbase on master, restart hbase on Slave      *      * step 4: verify Slave still has the rows before put      *      t1_syncup: 50 rows from cf1      *      t2_syncup: 100 rows from cf1      *      * step 5: run syncup tool on Master      *      * step 6: verify that put show up on Slave      *         and 'norep' does not      *      t1_syncup: 100 rows from cf1      *      t2_syncup: 200 rows from cf1      *      * verify correctly replicated to Slave      */
name|mimicSyncUpAfterPut
argument_list|()
expr_stmt|;
block|}
specifier|protected
name|void
name|setupReplication
parameter_list|()
throws|throws
name|Exception
block|{
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
name|Admin
name|ha
init|=
name|utility1
operator|.
name|getAdmin
argument_list|()
decl_stmt|;
name|ha
operator|.
name|createTable
argument_list|(
name|t1_syncupSource
argument_list|)
expr_stmt|;
name|ha
operator|.
name|createTable
argument_list|(
name|t2_syncupSource
argument_list|)
expr_stmt|;
name|ha
operator|.
name|close
argument_list|()
expr_stmt|;
name|ha
operator|=
name|utility2
operator|.
name|getAdmin
argument_list|()
expr_stmt|;
name|ha
operator|.
name|createTable
argument_list|(
name|t1_syncupTarget
argument_list|)
expr_stmt|;
name|ha
operator|.
name|createTable
argument_list|(
name|t2_syncupTarget
argument_list|)
expr_stmt|;
name|ha
operator|.
name|close
argument_list|()
expr_stmt|;
name|Connection
name|connection1
init|=
name|ConnectionFactory
operator|.
name|createConnection
argument_list|(
name|utility1
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
name|Connection
name|connection2
init|=
name|ConnectionFactory
operator|.
name|createConnection
argument_list|(
name|utility2
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
comment|// Get HTable from Master
name|ht1Source
operator|=
name|connection1
operator|.
name|getTable
argument_list|(
name|t1_su
argument_list|)
expr_stmt|;
name|ht2Source
operator|=
name|connection1
operator|.
name|getTable
argument_list|(
name|t2_su
argument_list|)
expr_stmt|;
comment|// Get HTable from Peer1
name|ht1TargetAtPeer1
operator|=
name|connection2
operator|.
name|getTable
argument_list|(
name|t1_su
argument_list|)
expr_stmt|;
name|ht2TargetAtPeer1
operator|=
name|connection2
operator|.
name|getTable
argument_list|(
name|t2_su
argument_list|)
expr_stmt|;
comment|/**      * set M-S : Master: utility1 Slave1: utility2      */
name|ReplicationPeerConfig
name|rpc
init|=
operator|new
name|ReplicationPeerConfig
argument_list|()
decl_stmt|;
name|rpc
operator|.
name|setClusterKey
argument_list|(
name|utility2
operator|.
name|getClusterKey
argument_list|()
argument_list|)
expr_stmt|;
name|admin1
operator|.
name|addPeer
argument_list|(
literal|"1"
argument_list|,
name|rpc
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|admin1
operator|.
name|close
argument_list|()
expr_stmt|;
name|admin2
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
specifier|private
name|void
name|putAndReplicateRows
parameter_list|()
throws|throws
name|Exception
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"putAndReplicateRows"
argument_list|)
expr_stmt|;
comment|// add rows to Master cluster,
name|Put
name|p
decl_stmt|;
comment|// 100 + 1 row to t1_syncup
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|NB_ROWS_IN_BATCH
condition|;
name|i
operator|++
control|)
block|{
name|p
operator|=
operator|new
name|Put
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"row"
operator|+
name|i
argument_list|)
argument_list|)
expr_stmt|;
name|p
operator|.
name|addColumn
argument_list|(
name|famName
argument_list|,
name|qualName
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"val"
operator|+
name|i
argument_list|)
argument_list|)
expr_stmt|;
name|ht1Source
operator|.
name|put
argument_list|(
name|p
argument_list|)
expr_stmt|;
block|}
name|p
operator|=
operator|new
name|Put
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"row"
operator|+
literal|9999
argument_list|)
argument_list|)
expr_stmt|;
name|p
operator|.
name|addColumn
argument_list|(
name|noRepfamName
argument_list|,
name|qualName
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"val"
operator|+
literal|9999
argument_list|)
argument_list|)
expr_stmt|;
name|ht1Source
operator|.
name|put
argument_list|(
name|p
argument_list|)
expr_stmt|;
comment|// 200 + 1 row to t2_syncup
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|NB_ROWS_IN_BATCH
operator|*
literal|2
condition|;
name|i
operator|++
control|)
block|{
name|p
operator|=
operator|new
name|Put
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"row"
operator|+
name|i
argument_list|)
argument_list|)
expr_stmt|;
name|p
operator|.
name|addColumn
argument_list|(
name|famName
argument_list|,
name|qualName
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"val"
operator|+
name|i
argument_list|)
argument_list|)
expr_stmt|;
name|ht2Source
operator|.
name|put
argument_list|(
name|p
argument_list|)
expr_stmt|;
block|}
name|p
operator|=
operator|new
name|Put
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"row"
operator|+
literal|9999
argument_list|)
argument_list|)
expr_stmt|;
name|p
operator|.
name|addColumn
argument_list|(
name|noRepfamName
argument_list|,
name|qualName
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"val"
operator|+
literal|9999
argument_list|)
argument_list|)
expr_stmt|;
name|ht2Source
operator|.
name|put
argument_list|(
name|p
argument_list|)
expr_stmt|;
comment|// ensure replication completed
name|Thread
operator|.
name|sleep
argument_list|(
name|SLEEP_TIME
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
name|rowCount_ht1TargetAtPeer1
init|=
name|utility2
operator|.
name|countRows
argument_list|(
name|ht1TargetAtPeer1
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
literal|"t1_syncup has 101 rows on source, and 100 on slave1"
argument_list|,
name|rowCount_ht1Source
operator|-
literal|1
argument_list|,
name|rowCount_ht1TargetAtPeer1
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|rowCount_ht1Source
operator|-
literal|1
operator|==
name|rowCount_ht1TargetAtPeer1
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
name|ht2TargetAtPeer1
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
literal|"t2_syncup has 201 rows on source, and 200 on slave1"
argument_list|,
name|rowCount_ht2Source
operator|-
literal|1
argument_list|,
name|rowCount_ht2TargetAtPeer1
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|rowCount_ht2Source
operator|-
literal|1
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
specifier|private
name|void
name|mimicSyncUpAfterDelete
parameter_list|()
throws|throws
name|Exception
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"mimicSyncUpAfterDelete"
argument_list|)
expr_stmt|;
name|utility2
operator|.
name|shutdownMiniHBaseCluster
argument_list|()
expr_stmt|;
name|List
argument_list|<
name|Delete
argument_list|>
name|list
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
comment|// delete half of the rows
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|NB_ROWS_IN_BATCH
operator|/
literal|2
condition|;
name|i
operator|++
control|)
block|{
name|String
name|rowKey
init|=
literal|"row"
operator|+
name|i
decl_stmt|;
name|Delete
name|del
init|=
operator|new
name|Delete
argument_list|(
name|rowKey
operator|.
name|getBytes
argument_list|()
argument_list|)
decl_stmt|;
name|list
operator|.
name|add
argument_list|(
name|del
argument_list|)
expr_stmt|;
block|}
name|ht1Source
operator|.
name|delete
argument_list|(
name|list
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
name|NB_ROWS_IN_BATCH
condition|;
name|i
operator|++
control|)
block|{
name|String
name|rowKey
init|=
literal|"row"
operator|+
name|i
decl_stmt|;
name|Delete
name|del
init|=
operator|new
name|Delete
argument_list|(
name|rowKey
operator|.
name|getBytes
argument_list|()
argument_list|)
decl_stmt|;
name|list
operator|.
name|add
argument_list|(
name|del
argument_list|)
expr_stmt|;
block|}
name|ht2Source
operator|.
name|delete
argument_list|(
name|list
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
literal|"t1_syncup has 51 rows on source, after remove 50 of the replicated colfam"
argument_list|,
literal|51
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
literal|"t2_syncup has 101 rows on source, after remove 100 of the replicated colfam"
argument_list|,
literal|101
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
comment|// before sync up
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
comment|// After sync up
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
literal|50
operator|||
name|rowCount_ht2TargetAtPeer1
operator|!=
literal|100
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
literal|"t1_syncup should have 51 rows at source, and it is "
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
literal|"t2_syncup should have 101 rows at source, and it is "
operator|+
name|rowCount_ht2Source
argument_list|)
expr_stmt|;
block|}
name|assertEquals
argument_list|(
literal|"@Peer1 t1_syncup should be sync up and have 50 rows"
argument_list|,
literal|50
argument_list|,
name|rowCount_ht1TargetAtPeer1
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"@Peer1 t2_syncup should be sync up and have 100 rows"
argument_list|,
literal|100
argument_list|,
name|rowCount_ht2TargetAtPeer1
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|rowCount_ht1TargetAtPeer1
operator|==
literal|50
operator|&&
name|rowCount_ht2TargetAtPeer1
operator|==
literal|100
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"SyncUpAfterDelete succeeded at retry = "
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
literal|"SyncUpAfterDelete failed at retry = "
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
name|mimicSyncUpAfterPut
parameter_list|()
throws|throws
name|Exception
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"mimicSyncUpAfterPut"
argument_list|)
expr_stmt|;
name|utility1
operator|.
name|restartHBaseCluster
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|utility2
operator|.
name|shutdownMiniHBaseCluster
argument_list|()
expr_stmt|;
name|Put
name|p
decl_stmt|;
comment|// another 100 + 1 row to t1_syncup
comment|// we should see 100 + 2 rows now
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|NB_ROWS_IN_BATCH
condition|;
name|i
operator|++
control|)
block|{
name|p
operator|=
operator|new
name|Put
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"row"
operator|+
name|i
argument_list|)
argument_list|)
expr_stmt|;
name|p
operator|.
name|addColumn
argument_list|(
name|famName
argument_list|,
name|qualName
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"val"
operator|+
name|i
argument_list|)
argument_list|)
expr_stmt|;
name|ht1Source
operator|.
name|put
argument_list|(
name|p
argument_list|)
expr_stmt|;
block|}
name|p
operator|=
operator|new
name|Put
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"row"
operator|+
literal|9998
argument_list|)
argument_list|)
expr_stmt|;
name|p
operator|.
name|addColumn
argument_list|(
name|noRepfamName
argument_list|,
name|qualName
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"val"
operator|+
literal|9998
argument_list|)
argument_list|)
expr_stmt|;
name|ht1Source
operator|.
name|put
argument_list|(
name|p
argument_list|)
expr_stmt|;
comment|// another 200 + 1 row to t1_syncup
comment|// we should see 200 + 2 rows now
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|NB_ROWS_IN_BATCH
operator|*
literal|2
condition|;
name|i
operator|++
control|)
block|{
name|p
operator|=
operator|new
name|Put
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"row"
operator|+
name|i
argument_list|)
argument_list|)
expr_stmt|;
name|p
operator|.
name|addColumn
argument_list|(
name|famName
argument_list|,
name|qualName
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"val"
operator|+
name|i
argument_list|)
argument_list|)
expr_stmt|;
name|ht2Source
operator|.
name|put
argument_list|(
name|p
argument_list|)
expr_stmt|;
block|}
name|p
operator|=
operator|new
name|Put
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"row"
operator|+
literal|9998
argument_list|)
argument_list|)
expr_stmt|;
name|p
operator|.
name|addColumn
argument_list|(
name|noRepfamName
argument_list|,
name|qualName
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"val"
operator|+
literal|9998
argument_list|)
argument_list|)
expr_stmt|;
name|ht2Source
operator|.
name|put
argument_list|(
name|p
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
literal|"t1_syncup has 102 rows on source"
argument_list|,
literal|102
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
literal|"t2_syncup has 202 rows on source"
argument_list|,
literal|202
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
comment|// before sync up
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
literal|"@Peer1 t1_syncup should be NOT sync up and have 50 rows"
argument_list|,
literal|50
argument_list|,
name|rowCount_ht1TargetAtPeer1
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"@Peer1 t2_syncup should be NOT sync up and have 100 rows"
argument_list|,
literal|100
argument_list|,
name|rowCount_ht2TargetAtPeer1
argument_list|)
expr_stmt|;
comment|// after syun up
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
literal|100
operator|||
name|rowCount_ht2TargetAtPeer1
operator|!=
literal|200
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
literal|"t1_syncup should have 102 rows at source, and it is "
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
literal|"t2_syncup should have 202 rows at source, and it is "
operator|+
name|rowCount_ht2Source
argument_list|)
expr_stmt|;
block|}
name|assertEquals
argument_list|(
literal|"@Peer1 t1_syncup should be sync up and have 100 rows"
argument_list|,
literal|100
argument_list|,
name|rowCount_ht1TargetAtPeer1
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"@Peer1 t2_syncup should be sync up and have 200 rows"
argument_list|,
literal|200
argument_list|,
name|rowCount_ht2TargetAtPeer1
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|rowCount_ht1TargetAtPeer1
operator|==
literal|100
operator|&&
name|rowCount_ht2TargetAtPeer1
operator|==
literal|200
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"SyncUpAfterPut succeeded at retry = "
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
literal|"SyncUpAfterPut failed at retry = "
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
specifier|protected
name|void
name|syncUp
parameter_list|(
name|HBaseTestingUtility
name|ut
parameter_list|)
throws|throws
name|Exception
block|{
name|ReplicationSyncUp
operator|.
name|setConfigure
argument_list|(
name|ut
operator|.
name|getConfiguration
argument_list|()
argument_list|)
expr_stmt|;
name|String
index|[]
name|arguments
init|=
operator|new
name|String
index|[]
block|{
literal|null
block|}
decl_stmt|;
operator|new
name|ReplicationSyncUp
argument_list|()
operator|.
name|run
argument_list|(
name|arguments
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

