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
name|Map
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
name|ExecutorService
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
name|TimeUnit
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
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|atomic
operator|.
name|AtomicInteger
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
name|CellBuilder
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
name|CellBuilderFactory
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
name|CellBuilderType
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
name|CellScanner
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
name|CompareOperator
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
name|Append
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
name|BufferedMutator
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
name|BufferedMutatorParams
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
name|Increment
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
name|RegionLocator
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
name|Row
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
name|RowMutations
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
name|TableBuilder
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
name|coprocessor
operator|.
name|Batch
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
name|filter
operator|.
name|CompareFilter
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
name|CoprocessorRpcChannel
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
name|security
operator|.
name|User
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
name|testclassification
operator|.
name|SmallTests
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
name|protobuf
operator|.
name|ByteString
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
name|shaded
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|AdminProtos
import|;
end_import

begin_comment
comment|/**  * Simple test of sink-side wal entry filter facility.  */
end_comment

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|ReplicationTests
operator|.
name|class
block|,
name|SmallTests
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|TestWALEntrySinkFilter
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
name|TestWALEntrySinkFilter
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
name|TestReplicationSink
operator|.
name|class
argument_list|)
decl_stmt|;
annotation|@
name|Rule
specifier|public
name|TestName
name|name
init|=
operator|new
name|TestName
argument_list|()
decl_stmt|;
specifier|static
specifier|final
name|int
name|BOUNDARY
init|=
literal|5
decl_stmt|;
specifier|static
specifier|final
name|AtomicInteger
name|UNFILTERED
init|=
operator|new
name|AtomicInteger
argument_list|()
decl_stmt|;
specifier|static
specifier|final
name|AtomicInteger
name|FILTERED
init|=
operator|new
name|AtomicInteger
argument_list|()
decl_stmt|;
comment|/**    * Implemetentation of Stoppable to pass into ReplicationSink.    */
specifier|private
specifier|static
name|Stoppable
name|STOPPABLE
init|=
operator|new
name|Stoppable
argument_list|()
block|{
specifier|private
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
comment|/**    * Test filter.    * Filter will filter out any write time that is<= 5 (BOUNDARY). We count how many items we    * filter out and we count how many cells make it through for distribution way down below in the    * Table#batch implementation. Puts in place a custom DevNullConnection so we can insert our    * counting Table.    * @throws IOException    */
annotation|@
name|Test
specifier|public
name|void
name|testWALEntryFilter
parameter_list|()
throws|throws
name|IOException
block|{
name|Configuration
name|conf
init|=
name|HBaseConfiguration
operator|.
name|create
argument_list|()
decl_stmt|;
comment|// Make it so our filter is instantiated on construction of ReplicationSink.
name|conf
operator|.
name|setClass
argument_list|(
name|WALEntrySinkFilter
operator|.
name|WAL_ENTRY_FILTER_KEY
argument_list|,
name|IfTimeIsGreaterThanBOUNDARYWALEntrySinkFilterImpl
operator|.
name|class
argument_list|,
name|WALEntrySinkFilter
operator|.
name|class
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setClass
argument_list|(
literal|"hbase.client.connection.impl"
argument_list|,
name|DevNullConnection
operator|.
name|class
argument_list|,
name|Connection
operator|.
name|class
argument_list|)
expr_stmt|;
name|ReplicationSink
name|sink
init|=
operator|new
name|ReplicationSink
argument_list|(
name|conf
argument_list|,
name|STOPPABLE
argument_list|)
decl_stmt|;
comment|// Create some dumb walentries.
name|List
argument_list|<
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|shaded
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|AdminProtos
operator|.
name|WALEntry
argument_list|>
name|entries
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|AdminProtos
operator|.
name|WALEntry
operator|.
name|Builder
name|entryBuilder
init|=
name|AdminProtos
operator|.
name|WALEntry
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
comment|// Need a tablename.
name|ByteString
name|tableName
init|=
name|ByteString
operator|.
name|copyFromUtf8
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
name|this
operator|.
name|name
operator|.
name|getMethodName
argument_list|()
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|)
decl_stmt|;
comment|// Add WALEdit Cells to Cells List. The way edits arrive at the sink is with protos
comment|// describing the edit with all Cells from all edits aggregated in a single CellScanner.
specifier|final
name|List
argument_list|<
name|Cell
argument_list|>
name|cells
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|int
name|count
init|=
name|BOUNDARY
operator|*
literal|2
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
name|count
condition|;
name|i
operator|++
control|)
block|{
name|byte
index|[]
name|bytes
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|i
argument_list|)
decl_stmt|;
comment|// Create a wal entry. Everything is set to the current index as bytes or int/long.
name|entryBuilder
operator|.
name|clear
argument_list|()
expr_stmt|;
name|entryBuilder
operator|.
name|setKey
argument_list|(
name|entryBuilder
operator|.
name|getKeyBuilder
argument_list|()
operator|.
name|setLogSequenceNumber
argument_list|(
name|i
argument_list|)
operator|.
name|setEncodedRegionName
argument_list|(
name|ByteString
operator|.
name|copyFrom
argument_list|(
name|bytes
argument_list|)
argument_list|)
operator|.
name|setWriteTime
argument_list|(
name|i
argument_list|)
operator|.
name|setTableName
argument_list|(
name|tableName
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
comment|// Lets have one Cell associated with each WALEdit.
name|entryBuilder
operator|.
name|setAssociatedCellCount
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|entries
operator|.
name|add
argument_list|(
name|entryBuilder
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
comment|// We need to add a Cell per WALEdit to the cells array.
name|CellBuilder
name|cellBuilder
init|=
name|CellBuilderFactory
operator|.
name|create
argument_list|(
name|CellBuilderType
operator|.
name|DEEP_COPY
argument_list|)
decl_stmt|;
comment|// Make cells whose row, family, cell, value, and ts are == 'i'.
name|Cell
name|cell
init|=
name|cellBuilder
operator|.
name|setRow
argument_list|(
name|bytes
argument_list|)
operator|.
name|setFamily
argument_list|(
name|bytes
argument_list|)
operator|.
name|setQualifier
argument_list|(
name|bytes
argument_list|)
operator|.
name|setType
argument_list|(
name|Cell
operator|.
name|Type
operator|.
name|Put
argument_list|)
operator|.
name|setTimestamp
argument_list|(
name|i
argument_list|)
operator|.
name|setValue
argument_list|(
name|bytes
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|cells
operator|.
name|add
argument_list|(
name|cell
argument_list|)
expr_stmt|;
block|}
comment|// Now wrap our cells array in a CellScanner that we can pass in to replicateEntries. It has
comment|// all Cells from all the WALEntries made above.
name|CellScanner
name|cellScanner
init|=
operator|new
name|CellScanner
argument_list|()
block|{
comment|// Set to -1 because advance gets called before current.
name|int
name|index
init|=
operator|-
literal|1
decl_stmt|;
annotation|@
name|Override
specifier|public
name|Cell
name|current
parameter_list|()
block|{
return|return
name|cells
operator|.
name|get
argument_list|(
name|index
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|advance
parameter_list|()
throws|throws
name|IOException
block|{
name|index
operator|++
expr_stmt|;
return|return
name|index
operator|<
name|cells
operator|.
name|size
argument_list|()
return|;
block|}
block|}
decl_stmt|;
comment|// Call our sink.
name|sink
operator|.
name|replicateEntries
argument_list|(
name|entries
argument_list|,
name|cellScanner
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
expr_stmt|;
comment|// Check what made it through and what was filtered.
name|assertTrue
argument_list|(
name|FILTERED
operator|.
name|get
argument_list|()
operator|>
literal|0
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|UNFILTERED
operator|.
name|get
argument_list|()
operator|>
literal|0
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|count
argument_list|,
name|FILTERED
operator|.
name|get
argument_list|()
operator|+
name|UNFILTERED
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**    * Simple filter that will filter out any entry wholse writeTime is<= 5.    */
specifier|public
specifier|static
class|class
name|IfTimeIsGreaterThanBOUNDARYWALEntrySinkFilterImpl
implements|implements
name|WALEntrySinkFilter
block|{
specifier|public
name|IfTimeIsGreaterThanBOUNDARYWALEntrySinkFilterImpl
parameter_list|()
block|{}
annotation|@
name|Override
specifier|public
name|void
name|init
parameter_list|(
name|Connection
name|connection
parameter_list|)
block|{
comment|// Do nothing.
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|filter
parameter_list|(
name|TableName
name|table
parameter_list|,
name|long
name|writeTime
parameter_list|)
block|{
name|boolean
name|b
init|=
name|writeTime
operator|<=
name|BOUNDARY
decl_stmt|;
if|if
condition|(
name|b
condition|)
block|{
name|FILTERED
operator|.
name|incrementAndGet
argument_list|()
expr_stmt|;
block|}
return|return
name|b
return|;
block|}
block|}
comment|/**    * A DevNull Connection whose only purpose is checking what edits made it through. See down in    * {@link Table#batch(List, Object[])}.    */
specifier|public
specifier|static
class|class
name|DevNullConnection
implements|implements
name|Connection
block|{
specifier|private
specifier|final
name|Configuration
name|configuration
decl_stmt|;
name|DevNullConnection
parameter_list|(
name|Configuration
name|configuration
parameter_list|,
name|ExecutorService
name|es
parameter_list|,
name|User
name|user
parameter_list|)
block|{
name|this
operator|.
name|configuration
operator|=
name|configuration
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|abort
parameter_list|(
name|String
name|why
parameter_list|,
name|Throwable
name|e
parameter_list|)
block|{      }
annotation|@
name|Override
specifier|public
name|boolean
name|isAborted
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
annotation|@
name|Override
specifier|public
name|Configuration
name|getConfiguration
parameter_list|()
block|{
return|return
name|this
operator|.
name|configuration
return|;
block|}
annotation|@
name|Override
specifier|public
name|BufferedMutator
name|getBufferedMutator
parameter_list|(
name|TableName
name|tableName
parameter_list|)
throws|throws
name|IOException
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|BufferedMutator
name|getBufferedMutator
parameter_list|(
name|BufferedMutatorParams
name|params
parameter_list|)
throws|throws
name|IOException
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|RegionLocator
name|getRegionLocator
parameter_list|(
name|TableName
name|tableName
parameter_list|)
throws|throws
name|IOException
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|Admin
name|getAdmin
parameter_list|()
throws|throws
name|IOException
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|()
throws|throws
name|IOException
block|{      }
annotation|@
name|Override
specifier|public
name|boolean
name|isClosed
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
annotation|@
name|Override
specifier|public
name|TableBuilder
name|getTableBuilder
parameter_list|(
specifier|final
name|TableName
name|tableName
parameter_list|,
name|ExecutorService
name|pool
parameter_list|)
block|{
return|return
operator|new
name|TableBuilder
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|TableBuilder
name|setOperationTimeout
parameter_list|(
name|int
name|timeout
parameter_list|)
block|{
return|return
name|this
return|;
block|}
annotation|@
name|Override
specifier|public
name|TableBuilder
name|setRpcTimeout
parameter_list|(
name|int
name|timeout
parameter_list|)
block|{
return|return
name|this
return|;
block|}
annotation|@
name|Override
specifier|public
name|TableBuilder
name|setReadRpcTimeout
parameter_list|(
name|int
name|timeout
parameter_list|)
block|{
return|return
name|this
return|;
block|}
annotation|@
name|Override
specifier|public
name|TableBuilder
name|setWriteRpcTimeout
parameter_list|(
name|int
name|timeout
parameter_list|)
block|{
return|return
name|this
return|;
block|}
annotation|@
name|Override
specifier|public
name|Table
name|build
parameter_list|()
block|{
return|return
operator|new
name|Table
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|TableName
name|getName
parameter_list|()
block|{
return|return
name|tableName
return|;
block|}
annotation|@
name|Override
specifier|public
name|Configuration
name|getConfiguration
parameter_list|()
block|{
return|return
name|configuration
return|;
block|}
annotation|@
name|Override
specifier|public
name|HTableDescriptor
name|getTableDescriptor
parameter_list|()
throws|throws
name|IOException
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|TableDescriptor
name|getDescriptor
parameter_list|()
throws|throws
name|IOException
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|exists
parameter_list|(
name|Get
name|get
parameter_list|)
throws|throws
name|IOException
block|{
return|return
literal|false
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
index|[]
name|exists
parameter_list|(
name|List
argument_list|<
name|Get
argument_list|>
name|gets
parameter_list|)
throws|throws
name|IOException
block|{
return|return
operator|new
name|boolean
index|[
literal|0
index|]
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|batch
parameter_list|(
name|List
argument_list|<
name|?
extends|extends
name|Row
argument_list|>
name|actions
parameter_list|,
name|Object
index|[]
name|results
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
for|for
control|(
name|Row
name|action
range|:
name|actions
control|)
block|{
comment|// Row is the index of the loop above where we make WALEntry and Cells.
name|int
name|row
init|=
name|Bytes
operator|.
name|toInt
argument_list|(
name|action
operator|.
name|getRow
argument_list|()
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
literal|""
operator|+
name|row
argument_list|,
name|row
operator|>
name|BOUNDARY
argument_list|)
expr_stmt|;
name|UNFILTERED
operator|.
name|incrementAndGet
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
parameter_list|<
name|R
parameter_list|>
name|void
name|batchCallback
parameter_list|(
name|List
argument_list|<
name|?
extends|extends
name|Row
argument_list|>
name|actions
parameter_list|,
name|Object
index|[]
name|results
parameter_list|,
name|Batch
operator|.
name|Callback
argument_list|<
name|R
argument_list|>
name|callback
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{              }
annotation|@
name|Override
specifier|public
name|Result
name|get
parameter_list|(
name|Get
name|get
parameter_list|)
throws|throws
name|IOException
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|Result
index|[]
name|get
parameter_list|(
name|List
argument_list|<
name|Get
argument_list|>
name|gets
parameter_list|)
throws|throws
name|IOException
block|{
return|return
operator|new
name|Result
index|[
literal|0
index|]
return|;
block|}
annotation|@
name|Override
specifier|public
name|ResultScanner
name|getScanner
parameter_list|(
name|Scan
name|scan
parameter_list|)
throws|throws
name|IOException
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|ResultScanner
name|getScanner
parameter_list|(
name|byte
index|[]
name|family
parameter_list|)
throws|throws
name|IOException
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|ResultScanner
name|getScanner
parameter_list|(
name|byte
index|[]
name|family
parameter_list|,
name|byte
index|[]
name|qualifier
parameter_list|)
throws|throws
name|IOException
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|put
parameter_list|(
name|Put
name|put
parameter_list|)
throws|throws
name|IOException
block|{              }
annotation|@
name|Override
specifier|public
name|void
name|put
parameter_list|(
name|List
argument_list|<
name|Put
argument_list|>
name|puts
parameter_list|)
throws|throws
name|IOException
block|{              }
annotation|@
name|Override
specifier|public
name|boolean
name|checkAndPut
parameter_list|(
name|byte
index|[]
name|row
parameter_list|,
name|byte
index|[]
name|family
parameter_list|,
name|byte
index|[]
name|qualifier
parameter_list|,
name|byte
index|[]
name|value
parameter_list|,
name|Put
name|put
parameter_list|)
throws|throws
name|IOException
block|{
return|return
literal|false
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|checkAndPut
parameter_list|(
name|byte
index|[]
name|row
parameter_list|,
name|byte
index|[]
name|family
parameter_list|,
name|byte
index|[]
name|qualifier
parameter_list|,
name|CompareFilter
operator|.
name|CompareOp
name|compareOp
parameter_list|,
name|byte
index|[]
name|value
parameter_list|,
name|Put
name|put
parameter_list|)
throws|throws
name|IOException
block|{
return|return
literal|false
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|checkAndPut
parameter_list|(
name|byte
index|[]
name|row
parameter_list|,
name|byte
index|[]
name|family
parameter_list|,
name|byte
index|[]
name|qualifier
parameter_list|,
name|CompareOperator
name|op
parameter_list|,
name|byte
index|[]
name|value
parameter_list|,
name|Put
name|put
parameter_list|)
throws|throws
name|IOException
block|{
return|return
literal|false
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|delete
parameter_list|(
name|Delete
name|delete
parameter_list|)
throws|throws
name|IOException
block|{              }
annotation|@
name|Override
specifier|public
name|void
name|delete
parameter_list|(
name|List
argument_list|<
name|Delete
argument_list|>
name|deletes
parameter_list|)
throws|throws
name|IOException
block|{              }
annotation|@
name|Override
specifier|public
name|boolean
name|checkAndDelete
parameter_list|(
name|byte
index|[]
name|row
parameter_list|,
name|byte
index|[]
name|family
parameter_list|,
name|byte
index|[]
name|qualifier
parameter_list|,
name|byte
index|[]
name|value
parameter_list|,
name|Delete
name|delete
parameter_list|)
throws|throws
name|IOException
block|{
return|return
literal|false
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|checkAndDelete
parameter_list|(
name|byte
index|[]
name|row
parameter_list|,
name|byte
index|[]
name|family
parameter_list|,
name|byte
index|[]
name|qualifier
parameter_list|,
name|CompareFilter
operator|.
name|CompareOp
name|compareOp
parameter_list|,
name|byte
index|[]
name|value
parameter_list|,
name|Delete
name|delete
parameter_list|)
throws|throws
name|IOException
block|{
return|return
literal|false
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|checkAndDelete
parameter_list|(
name|byte
index|[]
name|row
parameter_list|,
name|byte
index|[]
name|family
parameter_list|,
name|byte
index|[]
name|qualifier
parameter_list|,
name|CompareOperator
name|op
parameter_list|,
name|byte
index|[]
name|value
parameter_list|,
name|Delete
name|delete
parameter_list|)
throws|throws
name|IOException
block|{
return|return
literal|false
return|;
block|}
annotation|@
name|Override
specifier|public
name|CheckAndMutateBuilder
name|checkAndMutate
parameter_list|(
name|byte
index|[]
name|row
parameter_list|,
name|byte
index|[]
name|family
parameter_list|)
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|mutateRow
parameter_list|(
name|RowMutations
name|rm
parameter_list|)
throws|throws
name|IOException
block|{              }
annotation|@
name|Override
specifier|public
name|Result
name|append
parameter_list|(
name|Append
name|append
parameter_list|)
throws|throws
name|IOException
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|Result
name|increment
parameter_list|(
name|Increment
name|increment
parameter_list|)
throws|throws
name|IOException
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|incrementColumnValue
parameter_list|(
name|byte
index|[]
name|row
parameter_list|,
name|byte
index|[]
name|family
parameter_list|,
name|byte
index|[]
name|qualifier
parameter_list|,
name|long
name|amount
parameter_list|)
throws|throws
name|IOException
block|{
return|return
literal|0
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|incrementColumnValue
parameter_list|(
name|byte
index|[]
name|row
parameter_list|,
name|byte
index|[]
name|family
parameter_list|,
name|byte
index|[]
name|qualifier
parameter_list|,
name|long
name|amount
parameter_list|,
name|Durability
name|durability
parameter_list|)
throws|throws
name|IOException
block|{
return|return
literal|0
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|()
throws|throws
name|IOException
block|{              }
annotation|@
name|Override
specifier|public
name|CoprocessorRpcChannel
name|coprocessorService
parameter_list|(
name|byte
index|[]
name|row
parameter_list|)
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
parameter_list|<
name|T
extends|extends
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|Service
parameter_list|,
name|R
parameter_list|>
name|Map
argument_list|<
name|byte
index|[]
argument_list|,
name|R
argument_list|>
name|coprocessorService
parameter_list|(
name|Class
argument_list|<
name|T
argument_list|>
name|service
parameter_list|,
name|byte
index|[]
name|startKey
parameter_list|,
name|byte
index|[]
name|endKey
parameter_list|,
name|Batch
operator|.
name|Call
argument_list|<
name|T
argument_list|,
name|R
argument_list|>
name|callable
parameter_list|)
throws|throws
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|ServiceException
throws|,
name|Throwable
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
parameter_list|<
name|T
extends|extends
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|Service
parameter_list|,
name|R
parameter_list|>
name|void
name|coprocessorService
parameter_list|(
name|Class
argument_list|<
name|T
argument_list|>
name|service
parameter_list|,
name|byte
index|[]
name|startKey
parameter_list|,
name|byte
index|[]
name|endKey
parameter_list|,
name|Batch
operator|.
name|Call
argument_list|<
name|T
argument_list|,
name|R
argument_list|>
name|callable
parameter_list|,
name|Batch
operator|.
name|Callback
argument_list|<
name|R
argument_list|>
name|callback
parameter_list|)
throws|throws
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|ServiceException
throws|,
name|Throwable
block|{              }
annotation|@
name|Override
specifier|public
parameter_list|<
name|R
extends|extends
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|Message
parameter_list|>
name|Map
argument_list|<
name|byte
index|[]
argument_list|,
name|R
argument_list|>
name|batchCoprocessorService
parameter_list|(
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|Descriptors
operator|.
name|MethodDescriptor
name|methodDescriptor
parameter_list|,
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|Message
name|request
parameter_list|,
name|byte
index|[]
name|startKey
parameter_list|,
name|byte
index|[]
name|endKey
parameter_list|,
name|R
name|responsePrototype
parameter_list|)
throws|throws
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|ServiceException
throws|,
name|Throwable
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
parameter_list|<
name|R
extends|extends
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|Message
parameter_list|>
name|void
name|batchCoprocessorService
parameter_list|(
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|Descriptors
operator|.
name|MethodDescriptor
name|methodDescriptor
parameter_list|,
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|Message
name|request
parameter_list|,
name|byte
index|[]
name|startKey
parameter_list|,
name|byte
index|[]
name|endKey
parameter_list|,
name|R
name|responsePrototype
parameter_list|,
name|Batch
operator|.
name|Callback
argument_list|<
name|R
argument_list|>
name|callback
parameter_list|)
throws|throws
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|ServiceException
throws|,
name|Throwable
block|{              }
annotation|@
name|Override
specifier|public
name|boolean
name|checkAndMutate
parameter_list|(
name|byte
index|[]
name|row
parameter_list|,
name|byte
index|[]
name|family
parameter_list|,
name|byte
index|[]
name|qualifier
parameter_list|,
name|CompareFilter
operator|.
name|CompareOp
name|compareOp
parameter_list|,
name|byte
index|[]
name|value
parameter_list|,
name|RowMutations
name|mutation
parameter_list|)
throws|throws
name|IOException
block|{
return|return
literal|false
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|checkAndMutate
parameter_list|(
name|byte
index|[]
name|row
parameter_list|,
name|byte
index|[]
name|family
parameter_list|,
name|byte
index|[]
name|qualifier
parameter_list|,
name|CompareOperator
name|op
parameter_list|,
name|byte
index|[]
name|value
parameter_list|,
name|RowMutations
name|mutation
parameter_list|)
throws|throws
name|IOException
block|{
return|return
literal|false
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getRpcTimeout
parameter_list|(
name|TimeUnit
name|unit
parameter_list|)
block|{
return|return
literal|0
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getRpcTimeout
parameter_list|()
block|{
return|return
literal|0
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setRpcTimeout
parameter_list|(
name|int
name|rpcTimeout
parameter_list|)
block|{              }
annotation|@
name|Override
specifier|public
name|long
name|getReadRpcTimeout
parameter_list|(
name|TimeUnit
name|unit
parameter_list|)
block|{
return|return
literal|0
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getReadRpcTimeout
parameter_list|()
block|{
return|return
literal|0
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setReadRpcTimeout
parameter_list|(
name|int
name|readRpcTimeout
parameter_list|)
block|{              }
annotation|@
name|Override
specifier|public
name|long
name|getWriteRpcTimeout
parameter_list|(
name|TimeUnit
name|unit
parameter_list|)
block|{
return|return
literal|0
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getWriteRpcTimeout
parameter_list|()
block|{
return|return
literal|0
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setWriteRpcTimeout
parameter_list|(
name|int
name|writeRpcTimeout
parameter_list|)
block|{              }
annotation|@
name|Override
specifier|public
name|long
name|getOperationTimeout
parameter_list|(
name|TimeUnit
name|unit
parameter_list|)
block|{
return|return
literal|0
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getOperationTimeout
parameter_list|()
block|{
return|return
literal|0
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setOperationTimeout
parameter_list|(
name|int
name|operationTimeout
parameter_list|)
block|{             }
annotation|@
name|Override
specifier|public
name|RegionLocator
name|getRegionLocator
parameter_list|()
throws|throws
name|IOException
block|{
return|return
literal|null
return|;
block|}
block|}
return|;
block|}
block|}
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|clearRegionLocationCache
parameter_list|()
block|{     }
block|}
block|}
end_class

end_unit

