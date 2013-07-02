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
name|NavigableMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|TreeMap
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
name|ipc
operator|.
name|PayloadCarryingRpcController
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
name|AdminService
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
name|HLog
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
name|HLogKey
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
name|Pair
import|;
end_import

begin_import
import|import
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
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|ServiceException
import|;
end_import

begin_class
specifier|public
class|class
name|ReplicationProtbufUtil
block|{
comment|/**    * Get the HLog entries from a list of protocol buffer WALEntry    *    * @param protoList the list of protocol buffer WALEntry    * @return an array of HLog entries    */
specifier|public
specifier|static
name|HLog
operator|.
name|Entry
index|[]
name|toHLogEntries
parameter_list|(
specifier|final
name|List
argument_list|<
name|AdminProtos
operator|.
name|WALEntry
argument_list|>
name|protoList
parameter_list|)
throws|throws
name|IOException
block|{
name|List
argument_list|<
name|HLog
operator|.
name|Entry
argument_list|>
name|entries
init|=
operator|new
name|ArrayList
argument_list|<
name|HLog
operator|.
name|Entry
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|AdminProtos
operator|.
name|WALEntry
name|entry
range|:
name|protoList
control|)
block|{
name|WALProtos
operator|.
name|WALKey
name|walKey
init|=
name|entry
operator|.
name|getKey
argument_list|()
decl_stmt|;
name|HLogKey
name|key
init|=
operator|new
name|HLogKey
argument_list|(
name|walKey
argument_list|)
decl_stmt|;
name|WALEdit
name|edit
init|=
operator|new
name|WALEdit
argument_list|()
decl_stmt|;
for|for
control|(
name|ByteString
name|keyValue
range|:
name|entry
operator|.
name|getKeyValueBytesList
argument_list|()
control|)
block|{
name|edit
operator|.
name|add
argument_list|(
operator|new
name|KeyValue
argument_list|(
name|keyValue
operator|.
name|toByteArray
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|walKey
operator|.
name|getScopesCount
argument_list|()
operator|>
literal|0
condition|)
block|{
name|TreeMap
argument_list|<
name|byte
index|[]
argument_list|,
name|Integer
argument_list|>
name|scopes
init|=
operator|new
name|TreeMap
argument_list|<
name|byte
index|[]
argument_list|,
name|Integer
argument_list|>
argument_list|(
name|Bytes
operator|.
name|BYTES_COMPARATOR
argument_list|)
decl_stmt|;
for|for
control|(
name|WALProtos
operator|.
name|FamilyScope
name|scope
range|:
name|walKey
operator|.
name|getScopesList
argument_list|()
control|)
block|{
name|scopes
operator|.
name|put
argument_list|(
name|scope
operator|.
name|getFamily
argument_list|()
operator|.
name|toByteArray
argument_list|()
argument_list|,
name|Integer
operator|.
name|valueOf
argument_list|(
name|scope
operator|.
name|getScopeType
argument_list|()
operator|.
name|ordinal
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|key
operator|.
name|setScopes
argument_list|(
name|scopes
argument_list|)
expr_stmt|;
block|}
name|entries
operator|.
name|add
argument_list|(
operator|new
name|HLog
operator|.
name|Entry
argument_list|(
name|key
argument_list|,
name|edit
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|entries
operator|.
name|toArray
argument_list|(
operator|new
name|HLog
operator|.
name|Entry
index|[
name|entries
operator|.
name|size
argument_list|()
index|]
argument_list|)
return|;
block|}
comment|/**    * A helper to replicate a list of HLog entries using admin protocol.    *    * @param admin    * @param entries    * @throws java.io.IOException    */
specifier|public
specifier|static
name|void
name|replicateWALEntry
parameter_list|(
specifier|final
name|AdminService
operator|.
name|BlockingInterface
name|admin
parameter_list|,
specifier|final
name|HLog
operator|.
name|Entry
index|[]
name|entries
parameter_list|)
throws|throws
name|IOException
block|{
name|Pair
argument_list|<
name|AdminProtos
operator|.
name|ReplicateWALEntryRequest
argument_list|,
name|CellScanner
argument_list|>
name|p
init|=
name|buildReplicateWALEntryRequest
argument_list|(
name|entries
argument_list|)
decl_stmt|;
try|try
block|{
name|PayloadCarryingRpcController
name|controller
init|=
operator|new
name|PayloadCarryingRpcController
argument_list|(
name|p
operator|.
name|getSecond
argument_list|()
argument_list|)
decl_stmt|;
name|admin
operator|.
name|replicateWALEntry
argument_list|(
name|controller
argument_list|,
name|p
operator|.
name|getFirst
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ServiceException
name|se
parameter_list|)
block|{
throw|throw
name|ProtobufUtil
operator|.
name|getRemoteException
argument_list|(
name|se
argument_list|)
throw|;
block|}
block|}
comment|/**    * Create a new ReplicateWALEntryRequest from a list of HLog entries    *    * @param entries the HLog entries to be replicated    * @return a pair of ReplicateWALEntryRequest and a CellScanner over all the WALEdit values    * found.    */
specifier|public
specifier|static
name|Pair
argument_list|<
name|AdminProtos
operator|.
name|ReplicateWALEntryRequest
argument_list|,
name|CellScanner
argument_list|>
name|buildReplicateWALEntryRequest
parameter_list|(
specifier|final
name|HLog
operator|.
name|Entry
index|[]
name|entries
parameter_list|)
block|{
comment|// Accumulate all the KVs seen in here.
name|List
argument_list|<
name|List
argument_list|<
name|?
extends|extends
name|Cell
argument_list|>
argument_list|>
name|allkvs
init|=
operator|new
name|ArrayList
argument_list|<
name|List
argument_list|<
name|?
extends|extends
name|Cell
argument_list|>
argument_list|>
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
name|WALProtos
operator|.
name|FamilyScope
operator|.
name|Builder
name|scopeBuilder
init|=
name|WALProtos
operator|.
name|FamilyScope
operator|.
name|newBuilder
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
name|AdminProtos
operator|.
name|ReplicateWALEntryRequest
operator|.
name|Builder
name|builder
init|=
name|AdminProtos
operator|.
name|ReplicateWALEntryRequest
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
for|for
control|(
name|HLog
operator|.
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
init|=
name|entryBuilder
operator|.
name|getKeyBuilder
argument_list|()
decl_stmt|;
name|HLogKey
name|key
init|=
name|entry
operator|.
name|getKey
argument_list|()
decl_stmt|;
name|keyBuilder
operator|.
name|setEncodedRegionName
argument_list|(
name|ByteString
operator|.
name|copyFrom
argument_list|(
name|key
operator|.
name|getEncodedRegionName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|keyBuilder
operator|.
name|setTableName
argument_list|(
name|ByteString
operator|.
name|copyFrom
argument_list|(
name|key
operator|.
name|getTablename
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|keyBuilder
operator|.
name|setLogSequenceNumber
argument_list|(
name|key
operator|.
name|getLogSeqNum
argument_list|()
argument_list|)
expr_stmt|;
name|keyBuilder
operator|.
name|setWriteTime
argument_list|(
name|key
operator|.
name|getWriteTime
argument_list|()
argument_list|)
expr_stmt|;
name|UUID
name|clusterId
init|=
name|key
operator|.
name|getClusterId
argument_list|()
decl_stmt|;
if|if
condition|(
name|clusterId
operator|!=
literal|null
condition|)
block|{
name|HBaseProtos
operator|.
name|UUID
operator|.
name|Builder
name|uuidBuilder
init|=
name|keyBuilder
operator|.
name|getClusterIdBuilder
argument_list|()
decl_stmt|;
name|uuidBuilder
operator|.
name|setLeastSigBits
argument_list|(
name|clusterId
operator|.
name|getLeastSignificantBits
argument_list|()
argument_list|)
expr_stmt|;
name|uuidBuilder
operator|.
name|setMostSigBits
argument_list|(
name|clusterId
operator|.
name|getMostSignificantBits
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|WALEdit
name|edit
init|=
name|entry
operator|.
name|getEdit
argument_list|()
decl_stmt|;
name|NavigableMap
argument_list|<
name|byte
index|[]
argument_list|,
name|Integer
argument_list|>
name|scopes
init|=
name|key
operator|.
name|getScopes
argument_list|()
decl_stmt|;
if|if
condition|(
name|scopes
operator|!=
literal|null
operator|&&
operator|!
name|scopes
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|byte
index|[]
argument_list|,
name|Integer
argument_list|>
name|scope
range|:
name|scopes
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|scopeBuilder
operator|.
name|setFamily
argument_list|(
name|ByteString
operator|.
name|copyFrom
argument_list|(
name|scope
operator|.
name|getKey
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|WALProtos
operator|.
name|ScopeType
name|scopeType
init|=
name|WALProtos
operator|.
name|ScopeType
operator|.
name|valueOf
argument_list|(
name|scope
operator|.
name|getValue
argument_list|()
operator|.
name|intValue
argument_list|()
argument_list|)
decl_stmt|;
name|scopeBuilder
operator|.
name|setScopeType
argument_list|(
name|scopeType
argument_list|)
expr_stmt|;
name|keyBuilder
operator|.
name|addScopes
argument_list|(
name|scopeBuilder
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
name|List
argument_list|<
name|KeyValue
argument_list|>
name|kvs
init|=
name|edit
operator|.
name|getKeyValues
argument_list|()
decl_stmt|;
comment|// Add up the size.  It is used later serializing out the kvs.
for|for
control|(
name|KeyValue
name|kv
range|:
name|kvs
control|)
block|{
name|size
operator|+=
name|kv
operator|.
name|getLength
argument_list|()
expr_stmt|;
block|}
comment|// Collect up the kvs
name|allkvs
operator|.
name|add
argument_list|(
name|kvs
argument_list|)
expr_stmt|;
comment|// Write out how many kvs associated with this entry.
name|entryBuilder
operator|.
name|setAssociatedCellCount
argument_list|(
name|kvs
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
return|return
operator|new
name|Pair
argument_list|<
name|AdminProtos
operator|.
name|ReplicateWALEntryRequest
argument_list|,
name|CellScanner
argument_list|>
argument_list|(
name|builder
operator|.
name|build
argument_list|()
argument_list|,
name|getCellScanner
argument_list|(
name|allkvs
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

