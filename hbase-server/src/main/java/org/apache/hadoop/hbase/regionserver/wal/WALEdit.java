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
name|regionserver
operator|.
name|wal
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataInput
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataOutput
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
name|hbase
operator|.
name|classification
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
name|HRegionInfo
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
name|HBaseInterfaceAudience
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
name|codec
operator|.
name|Codec
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
name|HeapSize
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
name|protobuf
operator|.
name|generated
operator|.
name|WALProtos
operator|.
name|CompactionDescriptor
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
name|FlushDescriptor
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
name|RegionEventDescriptor
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
name|ClassSize
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
name|EnvironmentEdgeManager
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
name|io
operator|.
name|Writable
import|;
end_import

begin_comment
comment|/**  * WALEdit: Used in HBase's transaction log (WAL) to represent  * the collection of edits (KeyValue objects) corresponding to a  * single transaction. The class implements "Writable" interface  * for serializing/deserializing a set of KeyValue items.  *  * Previously, if a transaction contains 3 edits to c1, c2, c3 for a row R,  * the WAL would have three log entries as follows:  *  *<logseq1-for-edit1>:<KeyValue-for-edit-c1>  *<logseq2-for-edit2>:<KeyValue-for-edit-c2>  *<logseq3-for-edit3>:<KeyValue-for-edit-c3>  *  * This presents problems because row level atomicity of transactions  * was not guaranteed. If we crash after few of the above appends make  * it, then recovery will restore a partial transaction.  *  * In the new world, all the edits for a given transaction are written  * out as a single record, for example:  *  *<logseq#-for-entire-txn>:<WALEdit-for-entire-txn>  *  * where, the WALEdit is serialized as:  *<-1, # of edits,<KeyValue>,<KeyValue>, ...>  * For example:  *<-1, 3,<Keyvalue-for-edit-c1>,<KeyValue-for-edit-c2>,<KeyValue-for-edit-c3>>  *  * The -1 marker is just a special way of being backward compatible with  * an old WAL which would have contained a single<KeyValue>.  *  * The deserializer for WALEdit backward compatibly detects if the record  * is an old style KeyValue or the new style WALEdit.  *  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|LimitedPrivate
argument_list|(
block|{
name|HBaseInterfaceAudience
operator|.
name|REPLICATION
block|,
name|HBaseInterfaceAudience
operator|.
name|COPROC
block|}
argument_list|)
specifier|public
class|class
name|WALEdit
implements|implements
name|Writable
implements|,
name|HeapSize
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
name|WALEdit
operator|.
name|class
argument_list|)
decl_stmt|;
comment|// TODO: Get rid of this; see HBASE-8457
specifier|public
specifier|static
specifier|final
name|byte
index|[]
name|METAFAMILY
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"METAFAMILY"
argument_list|)
decl_stmt|;
specifier|static
specifier|final
name|byte
index|[]
name|METAROW
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"METAROW"
argument_list|)
decl_stmt|;
specifier|static
specifier|final
name|byte
index|[]
name|COMPACTION
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"HBASE::COMPACTION"
argument_list|)
decl_stmt|;
specifier|static
specifier|final
name|byte
index|[]
name|FLUSH
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"HBASE::FLUSH"
argument_list|)
decl_stmt|;
specifier|static
specifier|final
name|byte
index|[]
name|REGION_EVENT
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"HBASE::REGION_EVENT"
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|byte
index|[]
name|BULK_LOAD
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"HBASE::BULK_LOAD"
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|int
name|VERSION_2
init|=
operator|-
literal|1
decl_stmt|;
specifier|private
specifier|final
name|boolean
name|isReplay
decl_stmt|;
specifier|private
specifier|final
name|ArrayList
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
argument_list|(
literal|1
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|WALEdit
name|EMPTY_WALEDIT
init|=
operator|new
name|WALEdit
argument_list|()
decl_stmt|;
comment|// Only here for legacy writable deserialization
annotation|@
name|Deprecated
specifier|private
name|NavigableMap
argument_list|<
name|byte
index|[]
argument_list|,
name|Integer
argument_list|>
name|scopes
decl_stmt|;
specifier|private
name|CompressionContext
name|compressionContext
decl_stmt|;
specifier|public
name|WALEdit
parameter_list|()
block|{
name|this
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
specifier|public
name|WALEdit
parameter_list|(
name|boolean
name|isReplay
parameter_list|)
block|{
name|this
operator|.
name|isReplay
operator|=
name|isReplay
expr_stmt|;
block|}
comment|/**    * @param f    * @return True is<code>f</code> is {@link #METAFAMILY}    */
specifier|public
specifier|static
name|boolean
name|isMetaEditFamily
parameter_list|(
specifier|final
name|byte
index|[]
name|f
parameter_list|)
block|{
return|return
name|Bytes
operator|.
name|equals
argument_list|(
name|METAFAMILY
argument_list|,
name|f
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|boolean
name|isMetaEditFamily
parameter_list|(
name|Cell
name|cell
parameter_list|)
block|{
return|return
name|CellUtil
operator|.
name|matchingFamily
argument_list|(
name|cell
argument_list|,
name|METAFAMILY
argument_list|)
return|;
block|}
specifier|public
name|boolean
name|isMetaEdit
parameter_list|()
block|{
for|for
control|(
name|Cell
name|cell
range|:
name|cells
control|)
block|{
if|if
condition|(
operator|!
name|isMetaEditFamily
argument_list|(
name|cell
argument_list|)
condition|)
block|{
return|return
literal|false
return|;
block|}
block|}
return|return
literal|true
return|;
block|}
comment|/**    * @return True when current WALEdit is created by log replay. Replication skips WALEdits from    *         replay.    */
specifier|public
name|boolean
name|isReplay
parameter_list|()
block|{
return|return
name|this
operator|.
name|isReplay
return|;
block|}
specifier|public
name|void
name|setCompressionContext
parameter_list|(
specifier|final
name|CompressionContext
name|compressionContext
parameter_list|)
block|{
name|this
operator|.
name|compressionContext
operator|=
name|compressionContext
expr_stmt|;
block|}
specifier|public
name|WALEdit
name|add
parameter_list|(
name|Cell
name|cell
parameter_list|)
block|{
name|this
operator|.
name|cells
operator|.
name|add
argument_list|(
name|cell
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|boolean
name|isEmpty
parameter_list|()
block|{
return|return
name|cells
operator|.
name|isEmpty
argument_list|()
return|;
block|}
specifier|public
name|int
name|size
parameter_list|()
block|{
return|return
name|cells
operator|.
name|size
argument_list|()
return|;
block|}
specifier|public
name|ArrayList
argument_list|<
name|Cell
argument_list|>
name|getCells
parameter_list|()
block|{
return|return
name|cells
return|;
block|}
specifier|public
name|NavigableMap
argument_list|<
name|byte
index|[]
argument_list|,
name|Integer
argument_list|>
name|getAndRemoveScopes
parameter_list|()
block|{
name|NavigableMap
argument_list|<
name|byte
index|[]
argument_list|,
name|Integer
argument_list|>
name|result
init|=
name|scopes
decl_stmt|;
name|scopes
operator|=
literal|null
expr_stmt|;
return|return
name|result
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|readFields
parameter_list|(
name|DataInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|cells
operator|.
name|clear
argument_list|()
expr_stmt|;
if|if
condition|(
name|scopes
operator|!=
literal|null
condition|)
block|{
name|scopes
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
name|int
name|versionOrLength
init|=
name|in
operator|.
name|readInt
argument_list|()
decl_stmt|;
comment|// TODO: Change version when we protobuf.  Also, change way we serialize KV!  Pb it too.
if|if
condition|(
name|versionOrLength
operator|==
name|VERSION_2
condition|)
block|{
comment|// this is new style WAL entry containing multiple KeyValues.
name|int
name|numEdits
init|=
name|in
operator|.
name|readInt
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|idx
init|=
literal|0
init|;
name|idx
operator|<
name|numEdits
condition|;
name|idx
operator|++
control|)
block|{
if|if
condition|(
name|compressionContext
operator|!=
literal|null
condition|)
block|{
name|this
operator|.
name|add
argument_list|(
name|KeyValueCompression
operator|.
name|readKV
argument_list|(
name|in
argument_list|,
name|compressionContext
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|this
operator|.
name|add
argument_list|(
name|KeyValueUtil
operator|.
name|create
argument_list|(
name|in
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
name|int
name|numFamilies
init|=
name|in
operator|.
name|readInt
argument_list|()
decl_stmt|;
if|if
condition|(
name|numFamilies
operator|>
literal|0
condition|)
block|{
if|if
condition|(
name|scopes
operator|==
literal|null
condition|)
block|{
name|scopes
operator|=
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
expr_stmt|;
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
name|numFamilies
condition|;
name|i
operator|++
control|)
block|{
name|byte
index|[]
name|fam
init|=
name|Bytes
operator|.
name|readByteArray
argument_list|(
name|in
argument_list|)
decl_stmt|;
name|int
name|scope
init|=
name|in
operator|.
name|readInt
argument_list|()
decl_stmt|;
name|scopes
operator|.
name|put
argument_list|(
name|fam
argument_list|,
name|scope
argument_list|)
expr_stmt|;
block|}
block|}
block|}
else|else
block|{
comment|// this is an old style WAL entry. The int that we just
comment|// read is actually the length of a single KeyValue
name|this
operator|.
name|add
argument_list|(
name|KeyValueUtil
operator|.
name|create
argument_list|(
name|versionOrLength
argument_list|,
name|in
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|write
parameter_list|(
name|DataOutput
name|out
parameter_list|)
throws|throws
name|IOException
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"WALEdit is being serialized to writable - only expected in test code"
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeInt
argument_list|(
name|VERSION_2
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeInt
argument_list|(
name|cells
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
comment|// We interleave the two lists for code simplicity
for|for
control|(
name|Cell
name|cell
range|:
name|cells
control|)
block|{
comment|// This is not used in any of the core code flows so it is just fine to convert to KV
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
name|compressionContext
operator|!=
literal|null
condition|)
block|{
name|KeyValueCompression
operator|.
name|writeKV
argument_list|(
name|out
argument_list|,
name|kv
argument_list|,
name|compressionContext
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|KeyValueUtil
operator|.
name|write
argument_list|(
name|kv
argument_list|,
name|out
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|scopes
operator|==
literal|null
condition|)
block|{
name|out
operator|.
name|writeInt
argument_list|(
literal|0
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|out
operator|.
name|writeInt
argument_list|(
name|scopes
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|byte
index|[]
name|key
range|:
name|scopes
operator|.
name|keySet
argument_list|()
control|)
block|{
name|Bytes
operator|.
name|writeByteArray
argument_list|(
name|out
argument_list|,
name|key
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeInt
argument_list|(
name|scopes
operator|.
name|get
argument_list|(
name|key
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|/**    * Reads WALEdit from cells.    * @param cellDecoder Cell decoder.    * @param expectedCount Expected cell count.    * @return Number of KVs read.    */
specifier|public
name|int
name|readFromCells
parameter_list|(
name|Codec
operator|.
name|Decoder
name|cellDecoder
parameter_list|,
name|int
name|expectedCount
parameter_list|)
throws|throws
name|IOException
block|{
name|cells
operator|.
name|clear
argument_list|()
expr_stmt|;
name|cells
operator|.
name|ensureCapacity
argument_list|(
name|expectedCount
argument_list|)
expr_stmt|;
while|while
condition|(
name|cells
operator|.
name|size
argument_list|()
operator|<
name|expectedCount
operator|&&
name|cellDecoder
operator|.
name|advance
argument_list|()
condition|)
block|{
name|cells
operator|.
name|add
argument_list|(
name|cellDecoder
operator|.
name|current
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|cells
operator|.
name|size
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
name|long
name|ret
init|=
name|ClassSize
operator|.
name|ARRAYLIST
decl_stmt|;
for|for
control|(
name|Cell
name|cell
range|:
name|cells
control|)
block|{
name|ret
operator|+=
name|CellUtil
operator|.
name|estimatedHeapSizeOf
argument_list|(
name|cell
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|scopes
operator|!=
literal|null
condition|)
block|{
name|ret
operator|+=
name|ClassSize
operator|.
name|TREEMAP
expr_stmt|;
name|ret
operator|+=
name|ClassSize
operator|.
name|align
argument_list|(
name|scopes
operator|.
name|size
argument_list|()
operator|*
name|ClassSize
operator|.
name|MAP_ENTRY
argument_list|)
expr_stmt|;
comment|// TODO this isn't quite right, need help here
block|}
return|return
name|ret
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"[#edits: "
operator|+
name|cells
operator|.
name|size
argument_list|()
operator|+
literal|" =<"
argument_list|)
expr_stmt|;
for|for
control|(
name|Cell
name|cell
range|:
name|cells
control|)
block|{
name|sb
operator|.
name|append
argument_list|(
name|cell
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"; "
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|scopes
operator|!=
literal|null
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|" scopes: "
operator|+
name|scopes
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|sb
operator|.
name|append
argument_list|(
literal|">]"
argument_list|)
expr_stmt|;
return|return
name|sb
operator|.
name|toString
argument_list|()
return|;
block|}
specifier|public
specifier|static
name|WALEdit
name|createFlushWALEdit
parameter_list|(
name|HRegionInfo
name|hri
parameter_list|,
name|FlushDescriptor
name|f
parameter_list|)
block|{
name|KeyValue
name|kv
init|=
operator|new
name|KeyValue
argument_list|(
name|getRowForRegion
argument_list|(
name|hri
argument_list|)
argument_list|,
name|METAFAMILY
argument_list|,
name|FLUSH
argument_list|,
name|EnvironmentEdgeManager
operator|.
name|currentTime
argument_list|()
argument_list|,
name|f
operator|.
name|toByteArray
argument_list|()
argument_list|)
decl_stmt|;
return|return
operator|new
name|WALEdit
argument_list|()
operator|.
name|add
argument_list|(
name|kv
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|FlushDescriptor
name|getFlushDescriptor
parameter_list|(
name|Cell
name|cell
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|CellUtil
operator|.
name|matchingColumn
argument_list|(
name|cell
argument_list|,
name|METAFAMILY
argument_list|,
name|FLUSH
argument_list|)
condition|)
block|{
return|return
name|FlushDescriptor
operator|.
name|parseFrom
argument_list|(
name|cell
operator|.
name|getValue
argument_list|()
argument_list|)
return|;
block|}
return|return
literal|null
return|;
block|}
specifier|public
specifier|static
name|WALEdit
name|createRegionEventWALEdit
parameter_list|(
name|HRegionInfo
name|hri
parameter_list|,
name|RegionEventDescriptor
name|regionEventDesc
parameter_list|)
block|{
name|KeyValue
name|kv
init|=
operator|new
name|KeyValue
argument_list|(
name|getRowForRegion
argument_list|(
name|hri
argument_list|)
argument_list|,
name|METAFAMILY
argument_list|,
name|REGION_EVENT
argument_list|,
name|EnvironmentEdgeManager
operator|.
name|currentTime
argument_list|()
argument_list|,
name|regionEventDesc
operator|.
name|toByteArray
argument_list|()
argument_list|)
decl_stmt|;
return|return
operator|new
name|WALEdit
argument_list|()
operator|.
name|add
argument_list|(
name|kv
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|RegionEventDescriptor
name|getRegionEventDescriptor
parameter_list|(
name|Cell
name|cell
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|CellUtil
operator|.
name|matchingColumn
argument_list|(
name|cell
argument_list|,
name|METAFAMILY
argument_list|,
name|REGION_EVENT
argument_list|)
condition|)
block|{
return|return
name|RegionEventDescriptor
operator|.
name|parseFrom
argument_list|(
name|cell
operator|.
name|getValue
argument_list|()
argument_list|)
return|;
block|}
return|return
literal|null
return|;
block|}
comment|/**    * Create a compaction WALEdit    * @param c    * @return A WALEdit that has<code>c</code> serialized as its value    */
specifier|public
specifier|static
name|WALEdit
name|createCompaction
parameter_list|(
specifier|final
name|HRegionInfo
name|hri
parameter_list|,
specifier|final
name|CompactionDescriptor
name|c
parameter_list|)
block|{
name|byte
index|[]
name|pbbytes
init|=
name|c
operator|.
name|toByteArray
argument_list|()
decl_stmt|;
name|KeyValue
name|kv
init|=
operator|new
name|KeyValue
argument_list|(
name|getRowForRegion
argument_list|(
name|hri
argument_list|)
argument_list|,
name|METAFAMILY
argument_list|,
name|COMPACTION
argument_list|,
name|EnvironmentEdgeManager
operator|.
name|currentTime
argument_list|()
argument_list|,
name|pbbytes
argument_list|)
decl_stmt|;
return|return
operator|new
name|WALEdit
argument_list|()
operator|.
name|add
argument_list|(
name|kv
argument_list|)
return|;
comment|//replication scope null so that this won't be replicated
block|}
specifier|private
specifier|static
name|byte
index|[]
name|getRowForRegion
parameter_list|(
name|HRegionInfo
name|hri
parameter_list|)
block|{
name|byte
index|[]
name|startKey
init|=
name|hri
operator|.
name|getStartKey
argument_list|()
decl_stmt|;
if|if
condition|(
name|startKey
operator|.
name|length
operator|==
literal|0
condition|)
block|{
comment|// empty row key is not allowed in mutations because it is both the start key and the end key
comment|// we return the smallest byte[] that is bigger (in lex comparison) than byte[0].
return|return
operator|new
name|byte
index|[]
block|{
literal|0
block|}
return|;
block|}
return|return
name|startKey
return|;
block|}
comment|/**    * Deserialized and returns a CompactionDescriptor is the KeyValue contains one.    * @param kv the key value    * @return deserialized CompactionDescriptor or null.    */
specifier|public
specifier|static
name|CompactionDescriptor
name|getCompaction
parameter_list|(
name|Cell
name|kv
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|CellUtil
operator|.
name|matchingColumn
argument_list|(
name|kv
argument_list|,
name|METAFAMILY
argument_list|,
name|COMPACTION
argument_list|)
condition|)
block|{
return|return
name|CompactionDescriptor
operator|.
name|parseFrom
argument_list|(
name|kv
operator|.
name|getValue
argument_list|()
argument_list|)
return|;
block|}
return|return
literal|null
return|;
block|}
comment|/**    * Create a bulk loader WALEdit    *    * @param hri                The HRegionInfo for the region in which we are bulk loading    * @param bulkLoadDescriptor The descriptor for the Bulk Loader    * @return The WALEdit for the BulkLoad    */
specifier|public
specifier|static
name|WALEdit
name|createBulkLoadEvent
parameter_list|(
name|HRegionInfo
name|hri
parameter_list|,
name|WALProtos
operator|.
name|BulkLoadDescriptor
name|bulkLoadDescriptor
parameter_list|)
block|{
name|KeyValue
name|kv
init|=
operator|new
name|KeyValue
argument_list|(
name|getRowForRegion
argument_list|(
name|hri
argument_list|)
argument_list|,
name|METAFAMILY
argument_list|,
name|BULK_LOAD
argument_list|,
name|EnvironmentEdgeManager
operator|.
name|currentTime
argument_list|()
argument_list|,
name|bulkLoadDescriptor
operator|.
name|toByteArray
argument_list|()
argument_list|)
decl_stmt|;
return|return
operator|new
name|WALEdit
argument_list|()
operator|.
name|add
argument_list|(
name|kv
argument_list|)
return|;
block|}
comment|/**    * Deserialized and returns a BulkLoadDescriptor from the passed in Cell    * @param cell the key value    * @return deserialized BulkLoadDescriptor or null.    */
specifier|public
specifier|static
name|WALProtos
operator|.
name|BulkLoadDescriptor
name|getBulkLoadDescriptor
parameter_list|(
name|Cell
name|cell
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|CellUtil
operator|.
name|matchingColumn
argument_list|(
name|cell
argument_list|,
name|METAFAMILY
argument_list|,
name|BULK_LOAD
argument_list|)
condition|)
block|{
return|return
name|WALProtos
operator|.
name|BulkLoadDescriptor
operator|.
name|parseFrom
argument_list|(
name|cell
operator|.
name|getValue
argument_list|()
argument_list|)
return|;
block|}
return|return
literal|null
return|;
block|}
block|}
end_class

end_unit

