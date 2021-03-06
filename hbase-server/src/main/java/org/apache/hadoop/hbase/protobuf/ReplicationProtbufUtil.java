begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|protobuf
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
name|PrivateCellUtil
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
name|AsyncRegionServerAdmin
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
name|io
operator|.
name|SizedCellScanner
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
name|WALCellCodec
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
name|FutureUtils
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
name|Pair
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
name|yetus
operator|.
name|audience
operator|.
name|InterfaceAudience
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
name|UnsafeByteOperations
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
operator|.
name|ReplicateWALEntryRequest
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
name|shaded
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|WALProtos
import|;
end_import

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|ReplicationProtbufUtil
block|{
comment|/**    * A helper to replicate a list of WAL entries using region server admin    * @param admin the region server admin    * @param entries Array of WAL entries to be replicated    * @param replicationClusterId Id which will uniquely identify source cluster FS client    *          configurations in the replication configuration directory    * @param sourceBaseNamespaceDir Path to source cluster base namespace directory    * @param sourceHFileArchiveDir Path to the source cluster hfile archive directory    */
specifier|public
specifier|static
name|void
name|replicateWALEntry
parameter_list|(
name|AsyncRegionServerAdmin
name|admin
parameter_list|,
name|Entry
index|[]
name|entries
parameter_list|,
name|String
name|replicationClusterId
parameter_list|,
name|Path
name|sourceBaseNamespaceDir
parameter_list|,
name|Path
name|sourceHFileArchiveDir
parameter_list|,
name|int
name|timeout
parameter_list|)
throws|throws
name|IOException
block|{
name|Pair
argument_list|<
name|ReplicateWALEntryRequest
argument_list|,
name|CellScanner
argument_list|>
name|p
init|=
name|buildReplicateWALEntryRequest
argument_list|(
name|entries
argument_list|,
literal|null
argument_list|,
name|replicationClusterId
argument_list|,
name|sourceBaseNamespaceDir
argument_list|,
name|sourceHFileArchiveDir
argument_list|)
decl_stmt|;
name|FutureUtils
operator|.
name|get
argument_list|(
name|admin
operator|.
name|replicateWALEntry
argument_list|(
name|p
operator|.
name|getFirst
argument_list|()
argument_list|,
name|p
operator|.
name|getSecond
argument_list|()
argument_list|,
name|timeout
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * Create a new ReplicateWALEntryRequest from a list of WAL entries    * @param entries the WAL entries to be replicated    * @return a pair of ReplicateWALEntryRequest and a CellScanner over all the WALEdit values found.    */
specifier|public
specifier|static
name|Pair
argument_list|<
name|ReplicateWALEntryRequest
argument_list|,
name|CellScanner
argument_list|>
name|buildReplicateWALEntryRequest
parameter_list|(
specifier|final
name|Entry
index|[]
name|entries
parameter_list|)
block|{
return|return
name|buildReplicateWALEntryRequest
argument_list|(
name|entries
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
return|;
block|}
comment|/**    * Create a new ReplicateWALEntryRequest from a list of WAL entries    * @param entries the WAL entries to be replicated    * @param encodedRegionName alternative region name to use if not null    * @param replicationClusterId Id which will uniquely identify source cluster FS client    *          configurations in the replication configuration directory    * @param sourceBaseNamespaceDir Path to source cluster base namespace directory    * @param sourceHFileArchiveDir Path to the source cluster hfile archive directory    * @return a pair of ReplicateWALEntryRequest and a CellScanner over all the WALEdit values found.    */
specifier|public
specifier|static
name|Pair
argument_list|<
name|ReplicateWALEntryRequest
argument_list|,
name|CellScanner
argument_list|>
name|buildReplicateWALEntryRequest
parameter_list|(
specifier|final
name|Entry
index|[]
name|entries
parameter_list|,
name|byte
index|[]
name|encodedRegionName
parameter_list|,
name|String
name|replicationClusterId
parameter_list|,
name|Path
name|sourceBaseNamespaceDir
parameter_list|,
name|Path
name|sourceHFileArchiveDir
parameter_list|)
block|{
comment|// Accumulate all the Cells seen in here.
name|List
argument_list|<
name|List
argument_list|<
name|?
extends|extends
name|Cell
argument_list|>
argument_list|>
name|allCells
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|entries
operator|.
name|length
argument_list|)
decl_stmt|;
name|int
name|size
init|=
literal|0
decl_stmt|;
name|WALEntry
operator|.
name|Builder
name|entryBuilder
init|=
name|WALEntry
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
name|ReplicateWALEntryRequest
operator|.
name|Builder
name|builder
init|=
name|ReplicateWALEntryRequest
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
for|for
control|(
name|Entry
name|entry
range|:
name|entries
control|)
block|{
name|entryBuilder
operator|.
name|clear
argument_list|()
expr_stmt|;
name|WALProtos
operator|.
name|WALKey
operator|.
name|Builder
name|keyBuilder
decl_stmt|;
try|try
block|{
name|keyBuilder
operator|=
name|entry
operator|.
name|getKey
argument_list|()
operator|.
name|getBuilder
argument_list|(
name|WALCellCodec
operator|.
name|getNoneCompressor
argument_list|()
argument_list|)
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
name|AssertionError
argument_list|(
literal|"There should not throw exception since NoneCompressor do not throw any exceptions"
argument_list|,
name|e
argument_list|)
throw|;
block|}
if|if
condition|(
name|encodedRegionName
operator|!=
literal|null
condition|)
block|{
name|keyBuilder
operator|.
name|setEncodedRegionName
argument_list|(
name|UnsafeByteOperations
operator|.
name|unsafeWrap
argument_list|(
name|encodedRegionName
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|entryBuilder
operator|.
name|setKey
argument_list|(
name|keyBuilder
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
name|WALEdit
name|edit
init|=
name|entry
operator|.
name|getEdit
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|Cell
argument_list|>
name|cells
init|=
name|edit
operator|.
name|getCells
argument_list|()
decl_stmt|;
comment|// Add up the size.  It is used later serializing out the kvs.
for|for
control|(
name|Cell
name|cell
range|:
name|cells
control|)
block|{
name|size
operator|+=
name|PrivateCellUtil
operator|.
name|estimatedSerializedSizeOf
argument_list|(
name|cell
argument_list|)
expr_stmt|;
block|}
comment|// Collect up the cells
name|allCells
operator|.
name|add
argument_list|(
name|cells
argument_list|)
expr_stmt|;
comment|// Write out how many cells associated with this entry.
name|entryBuilder
operator|.
name|setAssociatedCellCount
argument_list|(
name|cells
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|addEntry
argument_list|(
name|entryBuilder
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|replicationClusterId
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|setReplicationClusterId
argument_list|(
name|replicationClusterId
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|sourceBaseNamespaceDir
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|setSourceBaseNamespaceDirPath
argument_list|(
name|sourceBaseNamespaceDir
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|sourceHFileArchiveDir
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|setSourceHFileArchiveDirPath
argument_list|(
name|sourceHFileArchiveDir
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
operator|new
name|Pair
argument_list|<>
argument_list|(
name|builder
operator|.
name|build
argument_list|()
argument_list|,
name|getCellScanner
argument_list|(
name|allCells
argument_list|,
name|size
argument_list|)
argument_list|)
return|;
block|}
comment|/**    * @param cells    * @return<code>cells</code> packaged as a CellScanner    */
specifier|static
name|CellScanner
name|getCellScanner
parameter_list|(
specifier|final
name|List
argument_list|<
name|List
argument_list|<
name|?
extends|extends
name|Cell
argument_list|>
argument_list|>
name|cells
parameter_list|,
specifier|final
name|int
name|size
parameter_list|)
block|{
return|return
operator|new
name|SizedCellScanner
argument_list|()
block|{
specifier|private
specifier|final
name|Iterator
argument_list|<
name|List
argument_list|<
name|?
extends|extends
name|Cell
argument_list|>
argument_list|>
name|entries
init|=
name|cells
operator|.
name|iterator
argument_list|()
decl_stmt|;
specifier|private
name|Iterator
argument_list|<
name|?
extends|extends
name|Cell
argument_list|>
name|currentIterator
init|=
literal|null
decl_stmt|;
specifier|private
name|Cell
name|currentCell
decl_stmt|;
annotation|@
name|Override
specifier|public
name|Cell
name|current
parameter_list|()
block|{
return|return
name|this
operator|.
name|currentCell
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|advance
parameter_list|()
block|{
if|if
condition|(
name|this
operator|.
name|currentIterator
operator|==
literal|null
condition|)
block|{
if|if
condition|(
operator|!
name|this
operator|.
name|entries
operator|.
name|hasNext
argument_list|()
condition|)
return|return
literal|false
return|;
name|this
operator|.
name|currentIterator
operator|=
name|this
operator|.
name|entries
operator|.
name|next
argument_list|()
operator|.
name|iterator
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|this
operator|.
name|currentIterator
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|this
operator|.
name|currentCell
operator|=
name|this
operator|.
name|currentIterator
operator|.
name|next
argument_list|()
expr_stmt|;
return|return
literal|true
return|;
block|}
name|this
operator|.
name|currentCell
operator|=
literal|null
expr_stmt|;
name|this
operator|.
name|currentIterator
operator|=
literal|null
expr_stmt|;
return|return
name|advance
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|heapSize
parameter_list|()
block|{
return|return
name|size
return|;
block|}
block|}
return|;
block|}
block|}
end_class

end_unit

