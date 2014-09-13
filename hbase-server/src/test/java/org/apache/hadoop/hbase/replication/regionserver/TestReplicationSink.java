begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|ByteStringer
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
name|CellUtil
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
name|Stoppable
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
name|protobuf
operator|.
name|generated
operator|.
name|AdminProtos
operator|.
name|WALEntry
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
name|protobuf
operator|.
name|generated
operator|.
name|HBaseProtos
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
name|hbase
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|WALProtos
operator|.
name|WALKey
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
name|MediumTests
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|TestReplicationSink
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
name|TestReplicationSink
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|BATCH_SIZE
init|=
literal|10
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
specifier|private
specifier|static
name|ReplicationSink
name|SINK
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|TABLE_NAME1
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"table1"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|TABLE_NAME2
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"table2"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|FAM_NAME1
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"info1"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|FAM_NAME2
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"info2"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|Table
name|table1
decl_stmt|;
specifier|private
specifier|static
name|Stoppable
name|STOPPABLE
init|=
operator|new
name|Stoppable
argument_list|()
block|{
specifier|final
name|AtomicBoolean
name|stop
init|=
operator|new
name|AtomicBoolean
argument_list|(
literal|false
argument_list|)
decl_stmt|;
annotation|@
name|Override
specifier|public
name|boolean
name|isStopped
parameter_list|()
block|{
return|return
name|this
operator|.
name|stop
operator|.
name|get
argument_list|()
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
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"STOPPING BECAUSE: "
operator|+
name|why
argument_list|)
expr_stmt|;
name|this
operator|.
name|stop
operator|.
name|set
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
block|}
decl_stmt|;
specifier|private
specifier|static
name|Table
name|table2
decl_stmt|;
comment|/**    * @throws java.lang.Exception    */
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
name|getConfiguration
argument_list|()
operator|.
name|setBoolean
argument_list|(
literal|"dfs.support.append"
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setBoolean
argument_list|(
name|HConstants
operator|.
name|REPLICATION_ENABLE_KEY
argument_list|,
name|HConstants
operator|.
name|REPLICATION_ENABLE_DEFAULT
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|startMiniCluster
argument_list|(
literal|3
argument_list|)
expr_stmt|;
name|SINK
operator|=
operator|new
name|ReplicationSink
argument_list|(
operator|new
name|Configuration
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
argument_list|,
name|STOPPABLE
argument_list|)
expr_stmt|;
name|table1
operator|=
name|TEST_UTIL
operator|.
name|createTable
argument_list|(
name|TABLE_NAME1
argument_list|,
name|FAM_NAME1
argument_list|)
expr_stmt|;
name|table2
operator|=
name|TEST_UTIL
operator|.
name|createTable
argument_list|(
name|TABLE_NAME2
argument_list|,
name|FAM_NAME2
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
name|STOPPABLE
operator|.
name|stop
argument_list|(
literal|"Shutting down"
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
block|}
comment|/**    * @throws java.lang.Exception    */
annotation|@
name|Before
specifier|public
name|void
name|setUp
parameter_list|()
throws|throws
name|Exception
block|{
name|table1
operator|=
name|TEST_UTIL
operator|.
name|deleteTableData
argument_list|(
name|TABLE_NAME1
argument_list|)
expr_stmt|;
name|table2
operator|=
name|TEST_UTIL
operator|.
name|deleteTableData
argument_list|(
name|TABLE_NAME2
argument_list|)
expr_stmt|;
block|}
comment|/**    * Insert a whole batch of entries    * @throws Exception    */
annotation|@
name|Test
specifier|public
name|void
name|testBatchSink
parameter_list|()
throws|throws
name|Exception
block|{
name|List
argument_list|<
name|WALEntry
argument_list|>
name|entries
init|=
operator|new
name|ArrayList
argument_list|<
name|WALEntry
argument_list|>
argument_list|(
name|BATCH_SIZE
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Cell
argument_list|>
name|cells
init|=
operator|new
name|ArrayList
argument_list|<
name|Cell
argument_list|>
argument_list|()
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
name|BATCH_SIZE
condition|;
name|i
operator|++
control|)
block|{
name|entries
operator|.
name|add
argument_list|(
name|createEntry
argument_list|(
name|TABLE_NAME1
argument_list|,
name|i
argument_list|,
name|KeyValue
operator|.
name|Type
operator|.
name|Put
argument_list|,
name|cells
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|SINK
operator|.
name|replicateEntries
argument_list|(
name|entries
argument_list|,
name|CellUtil
operator|.
name|createCellScanner
argument_list|(
name|cells
operator|.
name|iterator
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|Scan
name|scan
init|=
operator|new
name|Scan
argument_list|()
decl_stmt|;
name|ResultScanner
name|scanRes
init|=
name|table1
operator|.
name|getScanner
argument_list|(
name|scan
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|BATCH_SIZE
argument_list|,
name|scanRes
operator|.
name|next
argument_list|(
name|BATCH_SIZE
argument_list|)
operator|.
name|length
argument_list|)
expr_stmt|;
block|}
comment|/**    * Insert a mix of puts and deletes    * @throws Exception    */
annotation|@
name|Test
specifier|public
name|void
name|testMixedPutDelete
parameter_list|()
throws|throws
name|Exception
block|{
name|List
argument_list|<
name|WALEntry
argument_list|>
name|entries
init|=
operator|new
name|ArrayList
argument_list|<
name|WALEntry
argument_list|>
argument_list|(
name|BATCH_SIZE
operator|/
literal|2
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Cell
argument_list|>
name|cells
init|=
operator|new
name|ArrayList
argument_list|<
name|Cell
argument_list|>
argument_list|()
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
name|BATCH_SIZE
operator|/
literal|2
condition|;
name|i
operator|++
control|)
block|{
name|entries
operator|.
name|add
argument_list|(
name|createEntry
argument_list|(
name|TABLE_NAME1
argument_list|,
name|i
argument_list|,
name|KeyValue
operator|.
name|Type
operator|.
name|Put
argument_list|,
name|cells
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|SINK
operator|.
name|replicateEntries
argument_list|(
name|entries
argument_list|,
name|CellUtil
operator|.
name|createCellScanner
argument_list|(
name|cells
argument_list|)
argument_list|)
expr_stmt|;
name|entries
operator|=
operator|new
name|ArrayList
argument_list|<
name|WALEntry
argument_list|>
argument_list|(
name|BATCH_SIZE
argument_list|)
expr_stmt|;
name|cells
operator|=
operator|new
name|ArrayList
argument_list|<
name|Cell
argument_list|>
argument_list|()
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
name|BATCH_SIZE
condition|;
name|i
operator|++
control|)
block|{
name|entries
operator|.
name|add
argument_list|(
name|createEntry
argument_list|(
name|TABLE_NAME1
argument_list|,
name|i
argument_list|,
name|i
operator|%
literal|2
operator|!=
literal|0
condition|?
name|KeyValue
operator|.
name|Type
operator|.
name|Put
else|:
name|KeyValue
operator|.
name|Type
operator|.
name|DeleteColumn
argument_list|,
name|cells
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|SINK
operator|.
name|replicateEntries
argument_list|(
name|entries
argument_list|,
name|CellUtil
operator|.
name|createCellScanner
argument_list|(
name|cells
operator|.
name|iterator
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|Scan
name|scan
init|=
operator|new
name|Scan
argument_list|()
decl_stmt|;
name|ResultScanner
name|scanRes
init|=
name|table1
operator|.
name|getScanner
argument_list|(
name|scan
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|BATCH_SIZE
operator|/
literal|2
argument_list|,
name|scanRes
operator|.
name|next
argument_list|(
name|BATCH_SIZE
argument_list|)
operator|.
name|length
argument_list|)
expr_stmt|;
block|}
comment|/**    * Insert to 2 different tables    * @throws Exception    */
annotation|@
name|Test
specifier|public
name|void
name|testMixedPutTables
parameter_list|()
throws|throws
name|Exception
block|{
name|List
argument_list|<
name|WALEntry
argument_list|>
name|entries
init|=
operator|new
name|ArrayList
argument_list|<
name|WALEntry
argument_list|>
argument_list|(
name|BATCH_SIZE
operator|/
literal|2
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Cell
argument_list|>
name|cells
init|=
operator|new
name|ArrayList
argument_list|<
name|Cell
argument_list|>
argument_list|()
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
name|BATCH_SIZE
condition|;
name|i
operator|++
control|)
block|{
name|entries
operator|.
name|add
argument_list|(
name|createEntry
argument_list|(
name|i
operator|%
literal|2
operator|==
literal|0
condition|?
name|TABLE_NAME2
else|:
name|TABLE_NAME1
argument_list|,
name|i
argument_list|,
name|KeyValue
operator|.
name|Type
operator|.
name|Put
argument_list|,
name|cells
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|SINK
operator|.
name|replicateEntries
argument_list|(
name|entries
argument_list|,
name|CellUtil
operator|.
name|createCellScanner
argument_list|(
name|cells
operator|.
name|iterator
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|Scan
name|scan
init|=
operator|new
name|Scan
argument_list|()
decl_stmt|;
name|ResultScanner
name|scanRes
init|=
name|table2
operator|.
name|getScanner
argument_list|(
name|scan
argument_list|)
decl_stmt|;
for|for
control|(
name|Result
name|res
range|:
name|scanRes
control|)
block|{
name|assertTrue
argument_list|(
name|Bytes
operator|.
name|toInt
argument_list|(
name|res
operator|.
name|getRow
argument_list|()
argument_list|)
operator|%
literal|2
operator|==
literal|0
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Insert then do different types of deletes    * @throws Exception    */
annotation|@
name|Test
specifier|public
name|void
name|testMixedDeletes
parameter_list|()
throws|throws
name|Exception
block|{
name|List
argument_list|<
name|WALEntry
argument_list|>
name|entries
init|=
operator|new
name|ArrayList
argument_list|<
name|WALEntry
argument_list|>
argument_list|(
literal|3
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Cell
argument_list|>
name|cells
init|=
operator|new
name|ArrayList
argument_list|<
name|Cell
argument_list|>
argument_list|()
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
literal|3
condition|;
name|i
operator|++
control|)
block|{
name|entries
operator|.
name|add
argument_list|(
name|createEntry
argument_list|(
name|TABLE_NAME1
argument_list|,
name|i
argument_list|,
name|KeyValue
operator|.
name|Type
operator|.
name|Put
argument_list|,
name|cells
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|SINK
operator|.
name|replicateEntries
argument_list|(
name|entries
argument_list|,
name|CellUtil
operator|.
name|createCellScanner
argument_list|(
name|cells
operator|.
name|iterator
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|entries
operator|=
operator|new
name|ArrayList
argument_list|<
name|WALEntry
argument_list|>
argument_list|(
literal|3
argument_list|)
expr_stmt|;
name|cells
operator|=
operator|new
name|ArrayList
argument_list|<
name|Cell
argument_list|>
argument_list|()
expr_stmt|;
name|entries
operator|.
name|add
argument_list|(
name|createEntry
argument_list|(
name|TABLE_NAME1
argument_list|,
literal|0
argument_list|,
name|KeyValue
operator|.
name|Type
operator|.
name|DeleteColumn
argument_list|,
name|cells
argument_list|)
argument_list|)
expr_stmt|;
name|entries
operator|.
name|add
argument_list|(
name|createEntry
argument_list|(
name|TABLE_NAME1
argument_list|,
literal|1
argument_list|,
name|KeyValue
operator|.
name|Type
operator|.
name|DeleteFamily
argument_list|,
name|cells
argument_list|)
argument_list|)
expr_stmt|;
name|entries
operator|.
name|add
argument_list|(
name|createEntry
argument_list|(
name|TABLE_NAME1
argument_list|,
literal|2
argument_list|,
name|KeyValue
operator|.
name|Type
operator|.
name|DeleteColumn
argument_list|,
name|cells
argument_list|)
argument_list|)
expr_stmt|;
name|SINK
operator|.
name|replicateEntries
argument_list|(
name|entries
argument_list|,
name|CellUtil
operator|.
name|createCellScanner
argument_list|(
name|cells
operator|.
name|iterator
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|Scan
name|scan
init|=
operator|new
name|Scan
argument_list|()
decl_stmt|;
name|ResultScanner
name|scanRes
init|=
name|table1
operator|.
name|getScanner
argument_list|(
name|scan
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|scanRes
operator|.
name|next
argument_list|(
literal|3
argument_list|)
operator|.
name|length
argument_list|)
expr_stmt|;
block|}
comment|/**    * Puts are buffered, but this tests when a delete (not-buffered) is applied    * before the actual Put that creates it.    * @throws Exception    */
annotation|@
name|Test
specifier|public
name|void
name|testApplyDeleteBeforePut
parameter_list|()
throws|throws
name|Exception
block|{
name|List
argument_list|<
name|WALEntry
argument_list|>
name|entries
init|=
operator|new
name|ArrayList
argument_list|<
name|WALEntry
argument_list|>
argument_list|(
literal|5
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Cell
argument_list|>
name|cells
init|=
operator|new
name|ArrayList
argument_list|<
name|Cell
argument_list|>
argument_list|()
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
literal|2
condition|;
name|i
operator|++
control|)
block|{
name|entries
operator|.
name|add
argument_list|(
name|createEntry
argument_list|(
name|TABLE_NAME1
argument_list|,
name|i
argument_list|,
name|KeyValue
operator|.
name|Type
operator|.
name|Put
argument_list|,
name|cells
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|entries
operator|.
name|add
argument_list|(
name|createEntry
argument_list|(
name|TABLE_NAME1
argument_list|,
literal|1
argument_list|,
name|KeyValue
operator|.
name|Type
operator|.
name|DeleteFamily
argument_list|,
name|cells
argument_list|)
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|3
init|;
name|i
operator|<
literal|5
condition|;
name|i
operator|++
control|)
block|{
name|entries
operator|.
name|add
argument_list|(
name|createEntry
argument_list|(
name|TABLE_NAME1
argument_list|,
name|i
argument_list|,
name|KeyValue
operator|.
name|Type
operator|.
name|Put
argument_list|,
name|cells
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|SINK
operator|.
name|replicateEntries
argument_list|(
name|entries
argument_list|,
name|CellUtil
operator|.
name|createCellScanner
argument_list|(
name|cells
operator|.
name|iterator
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|Get
name|get
init|=
operator|new
name|Get
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|1
argument_list|)
argument_list|)
decl_stmt|;
name|Result
name|res
init|=
name|table1
operator|.
name|get
argument_list|(
name|get
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|res
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|private
name|WALEntry
name|createEntry
parameter_list|(
name|byte
index|[]
name|table
parameter_list|,
name|int
name|row
parameter_list|,
name|KeyValue
operator|.
name|Type
name|type
parameter_list|,
name|List
argument_list|<
name|Cell
argument_list|>
name|cells
parameter_list|)
block|{
name|byte
index|[]
name|fam
init|=
name|Bytes
operator|.
name|equals
argument_list|(
name|table
argument_list|,
name|TABLE_NAME1
argument_list|)
condition|?
name|FAM_NAME1
else|:
name|FAM_NAME2
decl_stmt|;
name|byte
index|[]
name|rowBytes
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|row
argument_list|)
decl_stmt|;
comment|// Just make sure we don't get the same ts for two consecutive rows with
comment|// same key
try|try
block|{
name|Thread
operator|.
name|sleep
argument_list|(
literal|1
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Was interrupted while sleep, meh"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
specifier|final
name|long
name|now
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|KeyValue
name|kv
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|type
operator|.
name|getCode
argument_list|()
operator|==
name|KeyValue
operator|.
name|Type
operator|.
name|Put
operator|.
name|getCode
argument_list|()
condition|)
block|{
name|kv
operator|=
operator|new
name|KeyValue
argument_list|(
name|rowBytes
argument_list|,
name|fam
argument_list|,
name|fam
argument_list|,
name|now
argument_list|,
name|KeyValue
operator|.
name|Type
operator|.
name|Put
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|row
argument_list|)
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|type
operator|.
name|getCode
argument_list|()
operator|==
name|KeyValue
operator|.
name|Type
operator|.
name|DeleteColumn
operator|.
name|getCode
argument_list|()
condition|)
block|{
name|kv
operator|=
operator|new
name|KeyValue
argument_list|(
name|rowBytes
argument_list|,
name|fam
argument_list|,
name|fam
argument_list|,
name|now
argument_list|,
name|KeyValue
operator|.
name|Type
operator|.
name|DeleteColumn
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|type
operator|.
name|getCode
argument_list|()
operator|==
name|KeyValue
operator|.
name|Type
operator|.
name|DeleteFamily
operator|.
name|getCode
argument_list|()
condition|)
block|{
name|kv
operator|=
operator|new
name|KeyValue
argument_list|(
name|rowBytes
argument_list|,
name|fam
argument_list|,
literal|null
argument_list|,
name|now
argument_list|,
name|KeyValue
operator|.
name|Type
operator|.
name|DeleteFamily
argument_list|)
expr_stmt|;
block|}
name|WALEntry
operator|.
name|Builder
name|builder
init|=
name|WALEntry
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
name|builder
operator|.
name|setAssociatedCellCount
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|WALKey
operator|.
name|Builder
name|keyBuilder
init|=
name|WALKey
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
name|UUID
operator|.
name|Builder
name|uuidBuilder
init|=
name|UUID
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
name|uuidBuilder
operator|.
name|setLeastSigBits
argument_list|(
name|HConstants
operator|.
name|DEFAULT_CLUSTER_ID
operator|.
name|getLeastSignificantBits
argument_list|()
argument_list|)
expr_stmt|;
name|uuidBuilder
operator|.
name|setMostSigBits
argument_list|(
name|HConstants
operator|.
name|DEFAULT_CLUSTER_ID
operator|.
name|getMostSignificantBits
argument_list|()
argument_list|)
expr_stmt|;
name|keyBuilder
operator|.
name|setClusterId
argument_list|(
name|uuidBuilder
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
name|keyBuilder
operator|.
name|setTableName
argument_list|(
name|ByteStringer
operator|.
name|wrap
argument_list|(
name|table
argument_list|)
argument_list|)
expr_stmt|;
name|keyBuilder
operator|.
name|setWriteTime
argument_list|(
name|now
argument_list|)
expr_stmt|;
name|keyBuilder
operator|.
name|setEncodedRegionName
argument_list|(
name|ByteStringer
operator|.
name|wrap
argument_list|(
name|HConstants
operator|.
name|EMPTY_BYTE_ARRAY
argument_list|)
argument_list|)
expr_stmt|;
name|keyBuilder
operator|.
name|setLogSequenceNumber
argument_list|(
operator|-
literal|1
argument_list|)
expr_stmt|;
name|builder
operator|.
name|setKey
argument_list|(
name|keyBuilder
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
name|cells
operator|.
name|add
argument_list|(
name|kv
argument_list|)
expr_stmt|;
return|return
name|builder
operator|.
name|build
argument_list|()
return|;
block|}
block|}
end_class

end_unit

