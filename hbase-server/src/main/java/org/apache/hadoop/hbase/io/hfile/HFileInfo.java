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
name|io
operator|.
name|hfile
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|ByteArrayInputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataInputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataOutputStream
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
name|io
operator|.
name|SequenceInputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|security
operator|.
name|Key
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
name|Collection
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Comparator
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
name|Set
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|SortedMap
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
name|crypto
operator|.
name|Cipher
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
name|crypto
operator|.
name|Encryption
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
name|ProtobufMagic
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
name|EncryptionUtil
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
name|ProtobufUtil
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
name|shaded
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|HBaseProtos
operator|.
name|BytesBytesPair
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
name|HFileProtos
import|;
end_import

begin_comment
comment|/**  * Metadata for HFile. Conjured by the writer. Read in by the reader.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|HFileInfo
implements|implements
name|SortedMap
argument_list|<
name|byte
index|[]
argument_list|,
name|byte
index|[]
argument_list|>
block|{
specifier|static
specifier|final
name|String
name|RESERVED_PREFIX
init|=
literal|"hfile."
decl_stmt|;
specifier|static
specifier|final
name|byte
index|[]
name|RESERVED_PREFIX_BYTES
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|RESERVED_PREFIX
argument_list|)
decl_stmt|;
specifier|static
specifier|final
name|byte
index|[]
name|LASTKEY
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|RESERVED_PREFIX
operator|+
literal|"LASTKEY"
argument_list|)
decl_stmt|;
specifier|static
specifier|final
name|byte
index|[]
name|AVG_KEY_LEN
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|RESERVED_PREFIX
operator|+
literal|"AVG_KEY_LEN"
argument_list|)
decl_stmt|;
specifier|static
specifier|final
name|byte
index|[]
name|AVG_VALUE_LEN
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|RESERVED_PREFIX
operator|+
literal|"AVG_VALUE_LEN"
argument_list|)
decl_stmt|;
specifier|static
specifier|final
name|byte
index|[]
name|CREATE_TIME_TS
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|RESERVED_PREFIX
operator|+
literal|"CREATE_TIME_TS"
argument_list|)
decl_stmt|;
specifier|static
specifier|final
name|byte
index|[]
name|COMPARATOR
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|RESERVED_PREFIX
operator|+
literal|"COMPARATOR"
argument_list|)
decl_stmt|;
specifier|static
specifier|final
name|byte
index|[]
name|TAGS_COMPRESSED
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|RESERVED_PREFIX
operator|+
literal|"TAGS_COMPRESSED"
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|byte
index|[]
name|MAX_TAGS_LEN
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|RESERVED_PREFIX
operator|+
literal|"MAX_TAGS_LEN"
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|SortedMap
argument_list|<
name|byte
index|[]
argument_list|,
name|byte
index|[]
argument_list|>
name|map
init|=
operator|new
name|TreeMap
argument_list|<>
argument_list|(
name|Bytes
operator|.
name|BYTES_COMPARATOR
argument_list|)
decl_stmt|;
comment|/**    * We can read files whose major version is v2 IFF their minor version is at least 3.    */
specifier|private
specifier|static
specifier|final
name|int
name|MIN_V2_MINOR_VERSION_WITH_PB
init|=
literal|3
decl_stmt|;
comment|/** Maximum minor version supported by this HFile format */
comment|// We went to version 2 when we moved to pb'ing fileinfo and the trailer on
comment|// the file. This version can read Writables version 1.
specifier|static
specifier|final
name|int
name|MAX_MINOR_VERSION
init|=
literal|3
decl_stmt|;
comment|/** Last key in the file. Filled in when we read in the file info */
specifier|private
name|Cell
name|lastKeyCell
init|=
literal|null
decl_stmt|;
comment|/** Average key length read from file info */
specifier|private
name|int
name|avgKeyLen
init|=
operator|-
literal|1
decl_stmt|;
comment|/** Average value length read from file info */
specifier|private
name|int
name|avgValueLen
init|=
operator|-
literal|1
decl_stmt|;
specifier|private
name|boolean
name|includesMemstoreTS
init|=
literal|false
decl_stmt|;
specifier|private
name|boolean
name|decodeMemstoreTS
init|=
literal|false
decl_stmt|;
comment|/**    * Blocks read from the load-on-open section, excluding data root index, meta    * index, and file info.    */
specifier|private
name|List
argument_list|<
name|HFileBlock
argument_list|>
name|loadOnOpenBlocks
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
comment|/**    * The iterator will track all blocks in load-on-open section, since we use the    * {@link org.apache.hadoop.hbase.io.ByteBuffAllocator} to manage the ByteBuffers in block now,    * so we must ensure that deallocate all ByteBuffers in the end.    */
specifier|private
name|HFileBlock
operator|.
name|BlockIterator
name|blockIter
decl_stmt|;
specifier|private
name|HFileBlockIndex
operator|.
name|CellBasedKeyBlockIndexReader
name|dataIndexReader
decl_stmt|;
specifier|private
name|HFileBlockIndex
operator|.
name|ByteArrayKeyBlockIndexReader
name|metaIndexReader
decl_stmt|;
specifier|private
name|FixedFileTrailer
name|trailer
decl_stmt|;
specifier|private
name|HFileContext
name|hfileContext
decl_stmt|;
specifier|public
name|HFileInfo
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
block|}
specifier|public
name|HFileInfo
parameter_list|(
name|ReaderContext
name|context
parameter_list|,
name|Configuration
name|conf
parameter_list|)
throws|throws
name|IOException
block|{
name|this
operator|.
name|initTrailerAndContext
argument_list|(
name|context
argument_list|,
name|conf
argument_list|)
expr_stmt|;
block|}
comment|/**    * Append the given key/value pair to the file info, optionally checking the    * key prefix.    *    * @param k key to add    * @param v value to add    * @param checkPrefix whether to check that the provided key does not start    *          with the reserved prefix    * @return this file info object    * @throws IOException if the key or value is invalid    */
specifier|public
name|HFileInfo
name|append
parameter_list|(
specifier|final
name|byte
index|[]
name|k
parameter_list|,
specifier|final
name|byte
index|[]
name|v
parameter_list|,
specifier|final
name|boolean
name|checkPrefix
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|k
operator|==
literal|null
operator|||
name|v
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|NullPointerException
argument_list|(
literal|"Key nor value may be null"
argument_list|)
throw|;
block|}
if|if
condition|(
name|checkPrefix
operator|&&
name|isReservedFileInfoKey
argument_list|(
name|k
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Keys with a "
operator|+
name|HFileInfo
operator|.
name|RESERVED_PREFIX
operator|+
literal|" are reserved"
argument_list|)
throw|;
block|}
name|put
argument_list|(
name|k
argument_list|,
name|v
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/** Return true if the given file info key is reserved for internal use. */
specifier|public
specifier|static
name|boolean
name|isReservedFileInfoKey
parameter_list|(
name|byte
index|[]
name|key
parameter_list|)
block|{
return|return
name|Bytes
operator|.
name|startsWith
argument_list|(
name|key
argument_list|,
name|HFileInfo
operator|.
name|RESERVED_PREFIX_BYTES
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|clear
parameter_list|()
block|{
name|this
operator|.
name|map
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Comparator
operator|<
condition|?
name|super
name|byte
index|[]
operator|>
name|comparator
argument_list|()
block|{
return|return
name|map
operator|.
name|comparator
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|containsKey
parameter_list|(
name|Object
name|key
parameter_list|)
block|{
return|return
name|map
operator|.
name|containsKey
argument_list|(
name|key
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|containsValue
parameter_list|(
name|Object
name|value
parameter_list|)
block|{
return|return
name|map
operator|.
name|containsValue
argument_list|(
name|value
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|Set
argument_list|<
name|java
operator|.
name|util
operator|.
name|Map
operator|.
name|Entry
argument_list|<
name|byte
index|[]
argument_list|,
name|byte
index|[]
argument_list|>
argument_list|>
name|entrySet
parameter_list|()
block|{
return|return
name|map
operator|.
name|entrySet
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|equals
parameter_list|(
name|Object
name|o
parameter_list|)
block|{
return|return
name|map
operator|.
name|equals
argument_list|(
name|o
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|byte
index|[]
name|firstKey
parameter_list|()
block|{
return|return
name|map
operator|.
name|firstKey
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|byte
index|[]
name|get
parameter_list|(
name|Object
name|key
parameter_list|)
block|{
return|return
name|map
operator|.
name|get
argument_list|(
name|key
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
return|return
name|map
operator|.
name|hashCode
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|SortedMap
argument_list|<
name|byte
index|[]
argument_list|,
name|byte
index|[]
argument_list|>
name|headMap
parameter_list|(
name|byte
index|[]
name|toKey
parameter_list|)
block|{
return|return
name|this
operator|.
name|map
operator|.
name|headMap
argument_list|(
name|toKey
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isEmpty
parameter_list|()
block|{
return|return
name|map
operator|.
name|isEmpty
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|Set
argument_list|<
name|byte
index|[]
argument_list|>
name|keySet
parameter_list|()
block|{
return|return
name|map
operator|.
name|keySet
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|byte
index|[]
name|lastKey
parameter_list|()
block|{
return|return
name|map
operator|.
name|lastKey
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|byte
index|[]
name|put
parameter_list|(
name|byte
index|[]
name|key
parameter_list|,
name|byte
index|[]
name|value
parameter_list|)
block|{
return|return
name|this
operator|.
name|map
operator|.
name|put
argument_list|(
name|key
argument_list|,
name|value
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|putAll
argument_list|(
name|Map
operator|<
condition|?
then|extends
name|byte
index|[]
argument_list|,
operator|?
expr|extends
name|byte
index|[]
operator|>
name|m
argument_list|)
block|{
name|this
operator|.
name|map
operator|.
name|putAll
argument_list|(
name|m
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|byte
index|[]
name|remove
parameter_list|(
name|Object
name|key
parameter_list|)
block|{
return|return
name|this
operator|.
name|map
operator|.
name|remove
argument_list|(
name|key
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|size
parameter_list|()
block|{
return|return
name|map
operator|.
name|size
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|SortedMap
argument_list|<
name|byte
index|[]
argument_list|,
name|byte
index|[]
argument_list|>
name|subMap
parameter_list|(
name|byte
index|[]
name|fromKey
parameter_list|,
name|byte
index|[]
name|toKey
parameter_list|)
block|{
return|return
name|this
operator|.
name|map
operator|.
name|subMap
argument_list|(
name|fromKey
argument_list|,
name|toKey
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|SortedMap
argument_list|<
name|byte
index|[]
argument_list|,
name|byte
index|[]
argument_list|>
name|tailMap
parameter_list|(
name|byte
index|[]
name|fromKey
parameter_list|)
block|{
return|return
name|this
operator|.
name|map
operator|.
name|tailMap
argument_list|(
name|fromKey
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|Collection
argument_list|<
name|byte
index|[]
argument_list|>
name|values
parameter_list|()
block|{
return|return
name|map
operator|.
name|values
argument_list|()
return|;
block|}
comment|/**    * Write out this instance on the passed in<code>out</code> stream.    * We write it as a protobuf.    * @see #read(DataInputStream)    */
name|void
name|write
parameter_list|(
specifier|final
name|DataOutputStream
name|out
parameter_list|)
throws|throws
name|IOException
block|{
name|HFileProtos
operator|.
name|FileInfoProto
operator|.
name|Builder
name|builder
init|=
name|HFileProtos
operator|.
name|FileInfoProto
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|byte
index|[]
argument_list|,
name|byte
index|[]
argument_list|>
name|e
range|:
name|this
operator|.
name|map
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|HBaseProtos
operator|.
name|BytesBytesPair
operator|.
name|Builder
name|bbpBuilder
init|=
name|HBaseProtos
operator|.
name|BytesBytesPair
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
name|bbpBuilder
operator|.
name|setFirst
argument_list|(
name|UnsafeByteOperations
operator|.
name|unsafeWrap
argument_list|(
name|e
operator|.
name|getKey
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|bbpBuilder
operator|.
name|setSecond
argument_list|(
name|UnsafeByteOperations
operator|.
name|unsafeWrap
argument_list|(
name|e
operator|.
name|getValue
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|builder
operator|.
name|addMapEntry
argument_list|(
name|bbpBuilder
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|out
operator|.
name|write
argument_list|(
name|ProtobufMagic
operator|.
name|PB_MAGIC
argument_list|)
expr_stmt|;
name|builder
operator|.
name|build
argument_list|()
operator|.
name|writeDelimitedTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
block|}
comment|/**    * Populate this instance with what we find on the passed in<code>in</code> stream.    * Can deserialize protobuf of old Writables format.    * @see #write(DataOutputStream)    */
name|void
name|read
parameter_list|(
specifier|final
name|DataInputStream
name|in
parameter_list|)
throws|throws
name|IOException
block|{
comment|// This code is tested over in TestHFileReaderV1 where we read an old hfile w/ this new code.
name|int
name|pblen
init|=
name|ProtobufUtil
operator|.
name|lengthOfPBMagic
argument_list|()
decl_stmt|;
name|byte
index|[]
name|pbuf
init|=
operator|new
name|byte
index|[
name|pblen
index|]
decl_stmt|;
if|if
condition|(
name|in
operator|.
name|markSupported
argument_list|()
condition|)
block|{
name|in
operator|.
name|mark
argument_list|(
name|pblen
argument_list|)
expr_stmt|;
block|}
name|int
name|read
init|=
name|in
operator|.
name|read
argument_list|(
name|pbuf
argument_list|)
decl_stmt|;
if|if
condition|(
name|read
operator|!=
name|pblen
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"read="
operator|+
name|read
operator|+
literal|", wanted="
operator|+
name|pblen
argument_list|)
throw|;
block|}
if|if
condition|(
name|ProtobufUtil
operator|.
name|isPBMagicPrefix
argument_list|(
name|pbuf
argument_list|)
condition|)
block|{
name|parsePB
argument_list|(
name|HFileProtos
operator|.
name|FileInfoProto
operator|.
name|parseDelimitedFrom
argument_list|(
name|in
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
if|if
condition|(
name|in
operator|.
name|markSupported
argument_list|()
condition|)
block|{
name|in
operator|.
name|reset
argument_list|()
expr_stmt|;
name|parseWritable
argument_list|(
name|in
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// We cannot use BufferedInputStream, it consumes more than we read from the underlying IS
name|ByteArrayInputStream
name|bais
init|=
operator|new
name|ByteArrayInputStream
argument_list|(
name|pbuf
argument_list|)
decl_stmt|;
name|SequenceInputStream
name|sis
init|=
operator|new
name|SequenceInputStream
argument_list|(
name|bais
argument_list|,
name|in
argument_list|)
decl_stmt|;
comment|// Concatenate input streams
comment|// TODO: Am I leaking anything here wrapping the passed in stream?  We are not calling
comment|// close on the wrapped streams but they should be let go after we leave this context?
comment|// I see that we keep a reference to the passed in inputstream but since we no longer
comment|// have a reference to this after we leave, we should be ok.
name|parseWritable
argument_list|(
operator|new
name|DataInputStream
argument_list|(
name|sis
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|/**    * Now parse the old Writable format.  It was a list of Map entries.  Each map entry was a    * key and a value of a byte [].  The old map format had a byte before each entry that held    * a code which was short for the key or value type.  We know it was a byte [] so in below    * we just read and dump it.    */
name|void
name|parseWritable
parameter_list|(
specifier|final
name|DataInputStream
name|in
parameter_list|)
throws|throws
name|IOException
block|{
comment|// First clear the map.
comment|// Otherwise we will just accumulate entries every time this method is called.
name|this
operator|.
name|map
operator|.
name|clear
argument_list|()
expr_stmt|;
comment|// Read the number of entries in the map
name|int
name|entries
init|=
name|in
operator|.
name|readInt
argument_list|()
decl_stmt|;
comment|// Then read each key/value pair
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|entries
condition|;
name|i
operator|++
control|)
block|{
name|byte
index|[]
name|key
init|=
name|Bytes
operator|.
name|readByteArray
argument_list|(
name|in
argument_list|)
decl_stmt|;
comment|// We used to read a byte that encoded the class type.
comment|// Read and ignore it because it is always byte [] in hfile
name|in
operator|.
name|readByte
argument_list|()
expr_stmt|;
name|byte
index|[]
name|value
init|=
name|Bytes
operator|.
name|readByteArray
argument_list|(
name|in
argument_list|)
decl_stmt|;
name|this
operator|.
name|map
operator|.
name|put
argument_list|(
name|key
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Fill our map with content of the pb we read off disk    * @param fip protobuf message to read    */
name|void
name|parsePB
parameter_list|(
specifier|final
name|HFileProtos
operator|.
name|FileInfoProto
name|fip
parameter_list|)
block|{
name|this
operator|.
name|map
operator|.
name|clear
argument_list|()
expr_stmt|;
for|for
control|(
name|BytesBytesPair
name|pair
range|:
name|fip
operator|.
name|getMapEntryList
argument_list|()
control|)
block|{
name|this
operator|.
name|map
operator|.
name|put
argument_list|(
name|pair
operator|.
name|getFirst
argument_list|()
operator|.
name|toByteArray
argument_list|()
argument_list|,
name|pair
operator|.
name|getSecond
argument_list|()
operator|.
name|toByteArray
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|void
name|initTrailerAndContext
parameter_list|(
name|ReaderContext
name|context
parameter_list|,
name|Configuration
name|conf
parameter_list|)
throws|throws
name|IOException
block|{
try|try
block|{
name|boolean
name|isHBaseChecksum
init|=
name|context
operator|.
name|getInputStreamWrapper
argument_list|()
operator|.
name|shouldUseHBaseChecksum
argument_list|()
decl_stmt|;
name|trailer
operator|=
name|FixedFileTrailer
operator|.
name|readFromStream
argument_list|(
name|context
operator|.
name|getInputStreamWrapper
argument_list|()
operator|.
name|getStream
argument_list|(
name|isHBaseChecksum
argument_list|)
argument_list|,
name|context
operator|.
name|getFileSize
argument_list|()
argument_list|)
expr_stmt|;
name|Path
name|path
init|=
name|context
operator|.
name|getFilePath
argument_list|()
decl_stmt|;
name|checkFileVersion
argument_list|(
name|path
argument_list|)
expr_stmt|;
name|this
operator|.
name|hfileContext
operator|=
name|createHFileContext
argument_list|(
name|path
argument_list|,
name|trailer
argument_list|,
name|conf
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
name|context
operator|.
name|getInputStreamWrapper
argument_list|()
operator|.
name|unbuffer
argument_list|()
expr_stmt|;
name|IOUtils
operator|.
name|closeQuietly
argument_list|(
name|context
operator|.
name|getInputStreamWrapper
argument_list|()
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|CorruptHFileException
argument_list|(
literal|"Problem reading HFile Trailer from file "
operator|+
name|context
operator|.
name|getFilePath
argument_list|()
argument_list|,
name|t
argument_list|)
throw|;
block|}
block|}
comment|/**    * should be called after initTrailerAndContext    */
specifier|public
name|void
name|initMetaAndIndex
parameter_list|(
name|HFile
operator|.
name|Reader
name|reader
parameter_list|)
throws|throws
name|IOException
block|{
name|ReaderContext
name|context
init|=
name|reader
operator|.
name|getContext
argument_list|()
decl_stmt|;
name|HFileBlock
operator|.
name|FSReader
name|blockReader
init|=
name|reader
operator|.
name|getUncachedBlockReader
argument_list|()
decl_stmt|;
comment|// Initialize an block iterator, and parse load-on-open blocks in the following.
name|blockIter
operator|=
name|blockReader
operator|.
name|blockRange
argument_list|(
name|trailer
operator|.
name|getLoadOnOpenDataOffset
argument_list|()
argument_list|,
name|context
operator|.
name|getFileSize
argument_list|()
operator|-
name|trailer
operator|.
name|getTrailerSize
argument_list|()
argument_list|)
expr_stmt|;
comment|// Data index. We also read statistics about the block index written after
comment|// the root level.
name|this
operator|.
name|dataIndexReader
operator|=
operator|new
name|HFileBlockIndex
operator|.
name|CellBasedKeyBlockIndexReader
argument_list|(
name|trailer
operator|.
name|createComparator
argument_list|()
argument_list|,
name|trailer
operator|.
name|getNumDataIndexLevels
argument_list|()
argument_list|)
expr_stmt|;
name|dataIndexReader
operator|.
name|readMultiLevelIndexRoot
argument_list|(
name|blockIter
operator|.
name|nextBlockWithBlockType
argument_list|(
name|BlockType
operator|.
name|ROOT_INDEX
argument_list|)
argument_list|,
name|trailer
operator|.
name|getDataIndexCount
argument_list|()
argument_list|)
expr_stmt|;
name|reader
operator|.
name|setDataBlockIndexReader
argument_list|(
name|dataIndexReader
argument_list|)
expr_stmt|;
comment|// Meta index.
name|this
operator|.
name|metaIndexReader
operator|=
operator|new
name|HFileBlockIndex
operator|.
name|ByteArrayKeyBlockIndexReader
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|metaIndexReader
operator|.
name|readRootIndex
argument_list|(
name|blockIter
operator|.
name|nextBlockWithBlockType
argument_list|(
name|BlockType
operator|.
name|ROOT_INDEX
argument_list|)
argument_list|,
name|trailer
operator|.
name|getMetaIndexCount
argument_list|()
argument_list|)
expr_stmt|;
name|reader
operator|.
name|setMetaBlockIndexReader
argument_list|(
name|metaIndexReader
argument_list|)
expr_stmt|;
name|loadMetaInfo
argument_list|(
name|blockIter
argument_list|,
name|hfileContext
argument_list|)
expr_stmt|;
name|reader
operator|.
name|setDataBlockEncoder
argument_list|(
name|HFileDataBlockEncoderImpl
operator|.
name|createFromFileInfo
argument_list|(
name|this
argument_list|)
argument_list|)
expr_stmt|;
comment|// Load-On-Open info
name|HFileBlock
name|b
decl_stmt|;
while|while
condition|(
operator|(
name|b
operator|=
name|blockIter
operator|.
name|nextBlock
argument_list|()
operator|)
operator|!=
literal|null
condition|)
block|{
name|loadOnOpenBlocks
operator|.
name|add
argument_list|(
name|b
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|HFileContext
name|createHFileContext
parameter_list|(
name|Path
name|path
parameter_list|,
name|FixedFileTrailer
name|trailer
parameter_list|,
name|Configuration
name|conf
parameter_list|)
throws|throws
name|IOException
block|{
name|HFileContextBuilder
name|builder
init|=
operator|new
name|HFileContextBuilder
argument_list|()
operator|.
name|withHBaseCheckSum
argument_list|(
literal|true
argument_list|)
operator|.
name|withHFileName
argument_list|(
name|path
operator|.
name|getName
argument_list|()
argument_list|)
operator|.
name|withCompression
argument_list|(
name|trailer
operator|.
name|getCompressionCodec
argument_list|()
argument_list|)
decl_stmt|;
comment|// Check for any key material available
name|byte
index|[]
name|keyBytes
init|=
name|trailer
operator|.
name|getEncryptionKey
argument_list|()
decl_stmt|;
if|if
condition|(
name|keyBytes
operator|!=
literal|null
condition|)
block|{
name|Encryption
operator|.
name|Context
name|cryptoContext
init|=
name|Encryption
operator|.
name|newContext
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|Key
name|key
init|=
name|EncryptionUtil
operator|.
name|unwrapKey
argument_list|(
name|conf
argument_list|,
name|keyBytes
argument_list|)
decl_stmt|;
comment|// Use the algorithm the key wants
name|Cipher
name|cipher
init|=
name|Encryption
operator|.
name|getCipher
argument_list|(
name|conf
argument_list|,
name|key
operator|.
name|getAlgorithm
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|cipher
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Cipher '"
operator|+
name|key
operator|.
name|getAlgorithm
argument_list|()
operator|+
literal|"' is not available"
operator|+
literal|", path="
operator|+
name|path
argument_list|)
throw|;
block|}
name|cryptoContext
operator|.
name|setCipher
argument_list|(
name|cipher
argument_list|)
expr_stmt|;
name|cryptoContext
operator|.
name|setKey
argument_list|(
name|key
argument_list|)
expr_stmt|;
name|builder
operator|.
name|withEncryptionContext
argument_list|(
name|cryptoContext
argument_list|)
expr_stmt|;
block|}
name|HFileContext
name|context
init|=
name|builder
operator|.
name|build
argument_list|()
decl_stmt|;
return|return
name|context
return|;
block|}
specifier|private
name|void
name|loadMetaInfo
parameter_list|(
name|HFileBlock
operator|.
name|BlockIterator
name|blockIter
parameter_list|,
name|HFileContext
name|hfileContext
parameter_list|)
throws|throws
name|IOException
block|{
name|read
argument_list|(
name|blockIter
operator|.
name|nextBlockWithBlockType
argument_list|(
name|BlockType
operator|.
name|FILE_INFO
argument_list|)
operator|.
name|getByteStream
argument_list|()
argument_list|)
expr_stmt|;
name|byte
index|[]
name|creationTimeBytes
init|=
name|get
argument_list|(
name|HFileInfo
operator|.
name|CREATE_TIME_TS
argument_list|)
decl_stmt|;
name|hfileContext
operator|.
name|setFileCreateTime
argument_list|(
name|creationTimeBytes
operator|==
literal|null
condition|?
literal|0
else|:
name|Bytes
operator|.
name|toLong
argument_list|(
name|creationTimeBytes
argument_list|)
argument_list|)
expr_stmt|;
name|byte
index|[]
name|tmp
init|=
name|get
argument_list|(
name|HFileInfo
operator|.
name|MAX_TAGS_LEN
argument_list|)
decl_stmt|;
comment|// max tag length is not present in the HFile means tags were not at all written to file.
if|if
condition|(
name|tmp
operator|!=
literal|null
condition|)
block|{
name|hfileContext
operator|.
name|setIncludesTags
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|tmp
operator|=
name|get
argument_list|(
name|HFileInfo
operator|.
name|TAGS_COMPRESSED
argument_list|)
expr_stmt|;
if|if
condition|(
name|tmp
operator|!=
literal|null
operator|&&
name|Bytes
operator|.
name|toBoolean
argument_list|(
name|tmp
argument_list|)
condition|)
block|{
name|hfileContext
operator|.
name|setCompressTags
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
block|}
comment|// parse meta info
if|if
condition|(
name|get
argument_list|(
name|HFileInfo
operator|.
name|LASTKEY
argument_list|)
operator|!=
literal|null
condition|)
block|{
name|lastKeyCell
operator|=
operator|new
name|KeyValue
operator|.
name|KeyOnlyKeyValue
argument_list|(
name|get
argument_list|(
name|HFileInfo
operator|.
name|LASTKEY
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|avgKeyLen
operator|=
name|Bytes
operator|.
name|toInt
argument_list|(
name|get
argument_list|(
name|HFileInfo
operator|.
name|AVG_KEY_LEN
argument_list|)
argument_list|)
expr_stmt|;
name|avgValueLen
operator|=
name|Bytes
operator|.
name|toInt
argument_list|(
name|get
argument_list|(
name|HFileInfo
operator|.
name|AVG_VALUE_LEN
argument_list|)
argument_list|)
expr_stmt|;
name|byte
index|[]
name|keyValueFormatVersion
init|=
name|get
argument_list|(
name|HFileWriterImpl
operator|.
name|KEY_VALUE_VERSION
argument_list|)
decl_stmt|;
name|includesMemstoreTS
operator|=
name|keyValueFormatVersion
operator|!=
literal|null
operator|&&
name|Bytes
operator|.
name|toInt
argument_list|(
name|keyValueFormatVersion
argument_list|)
operator|==
name|HFileWriterImpl
operator|.
name|KEY_VALUE_VER_WITH_MEMSTORE
expr_stmt|;
name|hfileContext
operator|.
name|setIncludesMvcc
argument_list|(
name|includesMemstoreTS
argument_list|)
expr_stmt|;
if|if
condition|(
name|includesMemstoreTS
condition|)
block|{
name|decodeMemstoreTS
operator|=
name|Bytes
operator|.
name|toLong
argument_list|(
name|get
argument_list|(
name|HFileWriterImpl
operator|.
name|MAX_MEMSTORE_TS_KEY
argument_list|)
argument_list|)
operator|>
literal|0
expr_stmt|;
block|}
block|}
comment|/**    * File version check is a little sloppy. We read v3 files but can also read v2 files if their    * content has been pb'd; files written with 0.98.    */
specifier|private
name|void
name|checkFileVersion
parameter_list|(
name|Path
name|path
parameter_list|)
block|{
name|int
name|majorVersion
init|=
name|trailer
operator|.
name|getMajorVersion
argument_list|()
decl_stmt|;
if|if
condition|(
name|majorVersion
operator|==
name|getMajorVersion
argument_list|()
condition|)
block|{
return|return;
block|}
name|int
name|minorVersion
init|=
name|trailer
operator|.
name|getMinorVersion
argument_list|()
decl_stmt|;
if|if
condition|(
name|majorVersion
operator|==
literal|2
operator|&&
name|minorVersion
operator|>=
name|MIN_V2_MINOR_VERSION_WITH_PB
condition|)
block|{
return|return;
block|}
comment|// We can read v3 or v2 versions of hfile.
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Invalid HFile version: major="
operator|+
name|trailer
operator|.
name|getMajorVersion
argument_list|()
operator|+
literal|", minor="
operator|+
name|trailer
operator|.
name|getMinorVersion
argument_list|()
operator|+
literal|": expected at least "
operator|+
literal|"major=2 and minor="
operator|+
name|MAX_MINOR_VERSION
operator|+
literal|", path="
operator|+
name|path
argument_list|)
throw|;
block|}
specifier|public
name|void
name|close
parameter_list|()
block|{
if|if
condition|(
name|blockIter
operator|!=
literal|null
condition|)
block|{
name|blockIter
operator|.
name|freeBlocks
argument_list|()
expr_stmt|;
block|}
block|}
specifier|public
name|int
name|getMajorVersion
parameter_list|()
block|{
return|return
literal|3
return|;
block|}
specifier|public
name|void
name|setTrailer
parameter_list|(
name|FixedFileTrailer
name|trailer
parameter_list|)
block|{
name|this
operator|.
name|trailer
operator|=
name|trailer
expr_stmt|;
block|}
specifier|public
name|FixedFileTrailer
name|getTrailer
parameter_list|()
block|{
return|return
name|this
operator|.
name|trailer
return|;
block|}
specifier|public
name|HFileBlockIndex
operator|.
name|CellBasedKeyBlockIndexReader
name|getDataBlockIndexReader
parameter_list|()
block|{
return|return
name|this
operator|.
name|dataIndexReader
return|;
block|}
specifier|public
name|HFileBlockIndex
operator|.
name|ByteArrayKeyBlockIndexReader
name|getMetaBlockIndexReader
parameter_list|()
block|{
return|return
name|this
operator|.
name|metaIndexReader
return|;
block|}
specifier|public
name|HFileContext
name|getHFileContext
parameter_list|()
block|{
return|return
name|this
operator|.
name|hfileContext
return|;
block|}
specifier|public
name|List
argument_list|<
name|HFileBlock
argument_list|>
name|getLoadOnOpenBlocks
parameter_list|()
block|{
return|return
name|loadOnOpenBlocks
return|;
block|}
specifier|public
name|Cell
name|getLastKeyCell
parameter_list|()
block|{
return|return
name|lastKeyCell
return|;
block|}
specifier|public
name|int
name|getAvgKeyLen
parameter_list|()
block|{
return|return
name|avgKeyLen
return|;
block|}
specifier|public
name|int
name|getAvgValueLen
parameter_list|()
block|{
return|return
name|avgValueLen
return|;
block|}
specifier|public
name|boolean
name|shouldIncludeMemStoreTS
parameter_list|()
block|{
return|return
name|includesMemstoreTS
return|;
block|}
specifier|public
name|boolean
name|isDecodeMemstoreTS
parameter_list|()
block|{
return|return
name|decodeMemstoreTS
return|;
block|}
block|}
end_class

end_unit

