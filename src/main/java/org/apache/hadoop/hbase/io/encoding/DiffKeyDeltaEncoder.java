begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one or more  * contributor license agreements. See the NOTICE file distributed with this  * work for additional information regarding copyright ownership. The ASF  * licenses this file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the  * License for the specific language governing permissions and limitations  * under the License.  */
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
name|encoding
package|;
end_package

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
name|nio
operator|.
name|ByteBuffer
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
name|util
operator|.
name|ByteBufferUtils
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
name|io
operator|.
name|RawComparator
import|;
end_import

begin_comment
comment|/**  * Compress using:  * - store size of common prefix  * - save column family once, it is same within HFile  * - use integer compression for key, value and prefix (7-bit encoding)  * - use bits to avoid duplication key length, value length  *   and type if it same as previous  * - store in 3 bits length of timestamp field  * - allow diff in timestamp instead of actual value  *  * Format:  * - 1 byte:    flag  * - 1-5 bytes: key length (only if FLAG_SAME_KEY_LENGTH is not set in flag)  * - 1-5 bytes: value length (only if FLAG_SAME_VALUE_LENGTH is not set in flag)  * - 1-5 bytes: prefix length  * - ... bytes: rest of the row (if prefix length is small enough)  * - ... bytes: qualifier (or suffix depending on prefix length)  * - 1-8 bytes: timestamp or diff  * - 1 byte:    type (only if FLAG_SAME_TYPE is not set in the flag)  * - ... bytes: value  */
end_comment

begin_class
specifier|public
class|class
name|DiffKeyDeltaEncoder
extends|extends
name|BufferedDataBlockEncoder
block|{
specifier|static
specifier|final
name|int
name|FLAG_SAME_KEY_LENGTH
init|=
literal|1
decl_stmt|;
specifier|static
specifier|final
name|int
name|FLAG_SAME_VALUE_LENGTH
init|=
literal|1
operator|<<
literal|1
decl_stmt|;
specifier|static
specifier|final
name|int
name|FLAG_SAME_TYPE
init|=
literal|1
operator|<<
literal|2
decl_stmt|;
specifier|static
specifier|final
name|int
name|FLAG_TIMESTAMP_IS_DIFF
init|=
literal|1
operator|<<
literal|3
decl_stmt|;
specifier|static
specifier|final
name|int
name|MASK_TIMESTAMP_LENGTH
init|=
operator|(
literal|1
operator|<<
literal|4
operator|)
operator||
operator|(
literal|1
operator|<<
literal|5
operator|)
operator||
operator|(
literal|1
operator|<<
literal|6
operator|)
decl_stmt|;
specifier|static
specifier|final
name|int
name|SHIFT_TIMESTAMP_LENGTH
init|=
literal|4
decl_stmt|;
specifier|static
specifier|final
name|int
name|FLAG_TIMESTAMP_SIGN
init|=
literal|1
operator|<<
literal|7
decl_stmt|;
specifier|protected
specifier|static
class|class
name|DiffCompressionState
extends|extends
name|CompressionState
block|{
name|long
name|timestamp
decl_stmt|;
name|byte
index|[]
name|familyNameWithSize
decl_stmt|;
annotation|@
name|Override
specifier|protected
name|void
name|readTimestamp
parameter_list|(
name|ByteBuffer
name|in
parameter_list|)
block|{
name|timestamp
operator|=
name|in
operator|.
name|getLong
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
name|void
name|copyFrom
parameter_list|(
name|CompressionState
name|state
parameter_list|)
block|{
name|super
operator|.
name|copyFrom
argument_list|(
name|state
argument_list|)
expr_stmt|;
name|DiffCompressionState
name|state2
init|=
operator|(
name|DiffCompressionState
operator|)
name|state
decl_stmt|;
name|timestamp
operator|=
name|state2
operator|.
name|timestamp
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|compressSingleKeyValue
parameter_list|(
name|DiffCompressionState
name|previousState
parameter_list|,
name|DiffCompressionState
name|currentState
parameter_list|,
name|DataOutputStream
name|out
parameter_list|,
name|ByteBuffer
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|byte
name|flag
init|=
literal|0
decl_stmt|;
name|int
name|kvPos
init|=
name|in
operator|.
name|position
argument_list|()
decl_stmt|;
name|int
name|keyLength
init|=
name|in
operator|.
name|getInt
argument_list|()
decl_stmt|;
name|int
name|valueLength
init|=
name|in
operator|.
name|getInt
argument_list|()
decl_stmt|;
name|long
name|timestamp
decl_stmt|;
name|long
name|diffTimestamp
init|=
literal|0
decl_stmt|;
name|int
name|diffTimestampFitsInBytes
init|=
literal|0
decl_stmt|;
name|int
name|commonPrefix
decl_stmt|;
name|int
name|timestampFitsInBytes
decl_stmt|;
if|if
condition|(
name|previousState
operator|.
name|isFirst
argument_list|()
condition|)
block|{
name|currentState
operator|.
name|readKey
argument_list|(
name|in
argument_list|,
name|keyLength
argument_list|,
name|valueLength
argument_list|)
expr_stmt|;
name|currentState
operator|.
name|prevOffset
operator|=
name|kvPos
expr_stmt|;
name|timestamp
operator|=
name|currentState
operator|.
name|timestamp
expr_stmt|;
if|if
condition|(
name|timestamp
operator|<
literal|0
condition|)
block|{
name|flag
operator||=
name|FLAG_TIMESTAMP_SIGN
expr_stmt|;
name|timestamp
operator|=
operator|-
name|timestamp
expr_stmt|;
block|}
name|timestampFitsInBytes
operator|=
name|ByteBufferUtils
operator|.
name|longFitsIn
argument_list|(
name|timestamp
argument_list|)
expr_stmt|;
name|flag
operator||=
operator|(
name|timestampFitsInBytes
operator|-
literal|1
operator|)
operator|<<
name|SHIFT_TIMESTAMP_LENGTH
expr_stmt|;
name|commonPrefix
operator|=
literal|0
expr_stmt|;
comment|// put column family
name|in
operator|.
name|mark
argument_list|()
expr_stmt|;
name|ByteBufferUtils
operator|.
name|skip
argument_list|(
name|in
argument_list|,
name|currentState
operator|.
name|rowLength
operator|+
name|KeyValue
operator|.
name|ROW_LENGTH_SIZE
argument_list|)
expr_stmt|;
name|ByteBufferUtils
operator|.
name|moveBufferToStream
argument_list|(
name|out
argument_list|,
name|in
argument_list|,
name|currentState
operator|.
name|familyLength
operator|+
name|KeyValue
operator|.
name|FAMILY_LENGTH_SIZE
argument_list|)
expr_stmt|;
name|in
operator|.
name|reset
argument_list|()
expr_stmt|;
block|}
else|else
block|{
comment|// find a common prefix and skip it
name|commonPrefix
operator|=
name|ByteBufferUtils
operator|.
name|findCommonPrefix
argument_list|(
name|in
argument_list|,
name|in
operator|.
name|position
argument_list|()
argument_list|,
name|previousState
operator|.
name|prevOffset
operator|+
name|KeyValue
operator|.
name|ROW_OFFSET
argument_list|,
name|keyLength
operator|-
name|KeyValue
operator|.
name|TIMESTAMP_TYPE_SIZE
argument_list|)
expr_stmt|;
comment|// don't compress timestamp and type using prefix
name|currentState
operator|.
name|readKey
argument_list|(
name|in
argument_list|,
name|keyLength
argument_list|,
name|valueLength
argument_list|,
name|commonPrefix
argument_list|,
name|previousState
argument_list|)
expr_stmt|;
name|currentState
operator|.
name|prevOffset
operator|=
name|kvPos
expr_stmt|;
name|timestamp
operator|=
name|currentState
operator|.
name|timestamp
expr_stmt|;
name|boolean
name|negativeTimestamp
init|=
name|timestamp
operator|<
literal|0
decl_stmt|;
if|if
condition|(
name|negativeTimestamp
condition|)
block|{
name|timestamp
operator|=
operator|-
name|timestamp
expr_stmt|;
block|}
name|timestampFitsInBytes
operator|=
name|ByteBufferUtils
operator|.
name|longFitsIn
argument_list|(
name|timestamp
argument_list|)
expr_stmt|;
if|if
condition|(
name|keyLength
operator|==
name|previousState
operator|.
name|keyLength
condition|)
block|{
name|flag
operator||=
name|FLAG_SAME_KEY_LENGTH
expr_stmt|;
block|}
if|if
condition|(
name|valueLength
operator|==
name|previousState
operator|.
name|valueLength
condition|)
block|{
name|flag
operator||=
name|FLAG_SAME_VALUE_LENGTH
expr_stmt|;
block|}
if|if
condition|(
name|currentState
operator|.
name|type
operator|==
name|previousState
operator|.
name|type
condition|)
block|{
name|flag
operator||=
name|FLAG_SAME_TYPE
expr_stmt|;
block|}
comment|// encode timestamp
name|diffTimestamp
operator|=
name|previousState
operator|.
name|timestamp
operator|-
name|currentState
operator|.
name|timestamp
expr_stmt|;
name|boolean
name|minusDiffTimestamp
init|=
name|diffTimestamp
operator|<
literal|0
decl_stmt|;
if|if
condition|(
name|minusDiffTimestamp
condition|)
block|{
name|diffTimestamp
operator|=
operator|-
name|diffTimestamp
expr_stmt|;
block|}
name|diffTimestampFitsInBytes
operator|=
name|ByteBufferUtils
operator|.
name|longFitsIn
argument_list|(
name|diffTimestamp
argument_list|)
expr_stmt|;
if|if
condition|(
name|diffTimestampFitsInBytes
operator|<
name|timestampFitsInBytes
condition|)
block|{
name|flag
operator||=
operator|(
name|diffTimestampFitsInBytes
operator|-
literal|1
operator|)
operator|<<
name|SHIFT_TIMESTAMP_LENGTH
expr_stmt|;
name|flag
operator||=
name|FLAG_TIMESTAMP_IS_DIFF
expr_stmt|;
if|if
condition|(
name|minusDiffTimestamp
condition|)
block|{
name|flag
operator||=
name|FLAG_TIMESTAMP_SIGN
expr_stmt|;
block|}
block|}
else|else
block|{
name|flag
operator||=
operator|(
name|timestampFitsInBytes
operator|-
literal|1
operator|)
operator|<<
name|SHIFT_TIMESTAMP_LENGTH
expr_stmt|;
if|if
condition|(
name|negativeTimestamp
condition|)
block|{
name|flag
operator||=
name|FLAG_TIMESTAMP_SIGN
expr_stmt|;
block|}
block|}
block|}
name|out
operator|.
name|write
argument_list|(
name|flag
argument_list|)
expr_stmt|;
if|if
condition|(
operator|(
name|flag
operator|&
name|FLAG_SAME_KEY_LENGTH
operator|)
operator|==
literal|0
condition|)
block|{
name|ByteBufferUtils
operator|.
name|putCompressedInt
argument_list|(
name|out
argument_list|,
name|keyLength
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
operator|(
name|flag
operator|&
name|FLAG_SAME_VALUE_LENGTH
operator|)
operator|==
literal|0
condition|)
block|{
name|ByteBufferUtils
operator|.
name|putCompressedInt
argument_list|(
name|out
argument_list|,
name|valueLength
argument_list|)
expr_stmt|;
block|}
name|ByteBufferUtils
operator|.
name|putCompressedInt
argument_list|(
name|out
argument_list|,
name|commonPrefix
argument_list|)
expr_stmt|;
name|ByteBufferUtils
operator|.
name|skip
argument_list|(
name|in
argument_list|,
name|commonPrefix
argument_list|)
expr_stmt|;
if|if
condition|(
name|previousState
operator|.
name|isFirst
argument_list|()
operator|||
name|commonPrefix
operator|<
name|currentState
operator|.
name|rowLength
operator|+
name|KeyValue
operator|.
name|ROW_LENGTH_SIZE
condition|)
block|{
name|int
name|restRowLength
init|=
name|currentState
operator|.
name|rowLength
operator|+
name|KeyValue
operator|.
name|ROW_LENGTH_SIZE
operator|-
name|commonPrefix
decl_stmt|;
name|ByteBufferUtils
operator|.
name|moveBufferToStream
argument_list|(
name|out
argument_list|,
name|in
argument_list|,
name|restRowLength
argument_list|)
expr_stmt|;
name|ByteBufferUtils
operator|.
name|skip
argument_list|(
name|in
argument_list|,
name|currentState
operator|.
name|familyLength
operator|+
name|KeyValue
operator|.
name|FAMILY_LENGTH_SIZE
argument_list|)
expr_stmt|;
name|ByteBufferUtils
operator|.
name|moveBufferToStream
argument_list|(
name|out
argument_list|,
name|in
argument_list|,
name|currentState
operator|.
name|qualifierLength
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|ByteBufferUtils
operator|.
name|moveBufferToStream
argument_list|(
name|out
argument_list|,
name|in
argument_list|,
name|keyLength
operator|-
name|commonPrefix
operator|-
name|KeyValue
operator|.
name|TIMESTAMP_TYPE_SIZE
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
operator|(
name|flag
operator|&
name|FLAG_TIMESTAMP_IS_DIFF
operator|)
operator|==
literal|0
condition|)
block|{
name|ByteBufferUtils
operator|.
name|putLong
argument_list|(
name|out
argument_list|,
name|timestamp
argument_list|,
name|timestampFitsInBytes
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|ByteBufferUtils
operator|.
name|putLong
argument_list|(
name|out
argument_list|,
name|diffTimestamp
argument_list|,
name|diffTimestampFitsInBytes
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
operator|(
name|flag
operator|&
name|FLAG_SAME_TYPE
operator|)
operator|==
literal|0
condition|)
block|{
name|out
operator|.
name|write
argument_list|(
name|currentState
operator|.
name|type
argument_list|)
expr_stmt|;
block|}
name|ByteBufferUtils
operator|.
name|skip
argument_list|(
name|in
argument_list|,
name|KeyValue
operator|.
name|TIMESTAMP_TYPE_SIZE
argument_list|)
expr_stmt|;
name|ByteBufferUtils
operator|.
name|moveBufferToStream
argument_list|(
name|out
argument_list|,
name|in
argument_list|,
name|valueLength
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|uncompressSingleKeyValue
parameter_list|(
name|DataInputStream
name|source
parameter_list|,
name|ByteBuffer
name|buffer
parameter_list|,
name|DiffCompressionState
name|state
parameter_list|)
throws|throws
name|IOException
throws|,
name|EncoderBufferTooSmallException
block|{
comment|// read the column family at the beginning
if|if
condition|(
name|state
operator|.
name|isFirst
argument_list|()
condition|)
block|{
name|state
operator|.
name|familyLength
operator|=
name|source
operator|.
name|readByte
argument_list|()
expr_stmt|;
name|state
operator|.
name|familyNameWithSize
operator|=
operator|new
name|byte
index|[
operator|(
name|state
operator|.
name|familyLength
operator|&
literal|0xff
operator|)
operator|+
name|KeyValue
operator|.
name|FAMILY_LENGTH_SIZE
index|]
expr_stmt|;
name|state
operator|.
name|familyNameWithSize
index|[
literal|0
index|]
operator|=
name|state
operator|.
name|familyLength
expr_stmt|;
name|source
operator|.
name|read
argument_list|(
name|state
operator|.
name|familyNameWithSize
argument_list|,
name|KeyValue
operator|.
name|FAMILY_LENGTH_SIZE
argument_list|,
name|state
operator|.
name|familyLength
argument_list|)
expr_stmt|;
block|}
comment|// read flag
name|byte
name|flag
init|=
name|source
operator|.
name|readByte
argument_list|()
decl_stmt|;
comment|// read key/value/common lengths
name|int
name|keyLength
decl_stmt|;
name|int
name|valueLength
decl_stmt|;
if|if
condition|(
operator|(
name|flag
operator|&
name|FLAG_SAME_KEY_LENGTH
operator|)
operator|!=
literal|0
condition|)
block|{
name|keyLength
operator|=
name|state
operator|.
name|keyLength
expr_stmt|;
block|}
else|else
block|{
name|keyLength
operator|=
name|ByteBufferUtils
operator|.
name|readCompressedInt
argument_list|(
name|source
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
operator|(
name|flag
operator|&
name|FLAG_SAME_VALUE_LENGTH
operator|)
operator|!=
literal|0
condition|)
block|{
name|valueLength
operator|=
name|state
operator|.
name|valueLength
expr_stmt|;
block|}
else|else
block|{
name|valueLength
operator|=
name|ByteBufferUtils
operator|.
name|readCompressedInt
argument_list|(
name|source
argument_list|)
expr_stmt|;
block|}
name|int
name|commonPrefix
init|=
name|ByteBufferUtils
operator|.
name|readCompressedInt
argument_list|(
name|source
argument_list|)
decl_stmt|;
comment|// create KeyValue buffer and fill it prefix
name|int
name|keyOffset
init|=
name|buffer
operator|.
name|position
argument_list|()
decl_stmt|;
name|ByteBufferUtils
operator|.
name|ensureSpace
argument_list|(
name|buffer
argument_list|,
name|keyLength
operator|+
name|valueLength
operator|+
name|KeyValue
operator|.
name|ROW_OFFSET
argument_list|)
expr_stmt|;
name|buffer
operator|.
name|putInt
argument_list|(
name|keyLength
argument_list|)
expr_stmt|;
name|buffer
operator|.
name|putInt
argument_list|(
name|valueLength
argument_list|)
expr_stmt|;
comment|// copy common from previous key
if|if
condition|(
name|commonPrefix
operator|>
literal|0
condition|)
block|{
name|ByteBufferUtils
operator|.
name|copyFromBufferToBuffer
argument_list|(
name|buffer
argument_list|,
name|buffer
argument_list|,
name|state
operator|.
name|prevOffset
operator|+
name|KeyValue
operator|.
name|ROW_OFFSET
argument_list|,
name|commonPrefix
argument_list|)
expr_stmt|;
block|}
comment|// copy the rest of the key from the buffer
name|int
name|keyRestLength
decl_stmt|;
if|if
condition|(
name|state
operator|.
name|isFirst
argument_list|()
operator|||
name|commonPrefix
operator|<
name|state
operator|.
name|rowLength
operator|+
name|KeyValue
operator|.
name|ROW_LENGTH_SIZE
condition|)
block|{
comment|// omit the family part of the key, it is always the same
name|short
name|rowLength
decl_stmt|;
name|int
name|rowRestLength
decl_stmt|;
comment|// check length of row
if|if
condition|(
name|commonPrefix
operator|<
name|KeyValue
operator|.
name|ROW_LENGTH_SIZE
condition|)
block|{
comment|// not yet copied, do it now
name|ByteBufferUtils
operator|.
name|copyFromStreamToBuffer
argument_list|(
name|buffer
argument_list|,
name|source
argument_list|,
name|KeyValue
operator|.
name|ROW_LENGTH_SIZE
operator|-
name|commonPrefix
argument_list|)
expr_stmt|;
name|ByteBufferUtils
operator|.
name|skip
argument_list|(
name|buffer
argument_list|,
operator|-
name|KeyValue
operator|.
name|ROW_LENGTH_SIZE
argument_list|)
expr_stmt|;
name|rowLength
operator|=
name|buffer
operator|.
name|getShort
argument_list|()
expr_stmt|;
name|rowRestLength
operator|=
name|rowLength
expr_stmt|;
block|}
else|else
block|{
comment|// already in buffer, just read it
name|rowLength
operator|=
name|buffer
operator|.
name|getShort
argument_list|(
name|keyOffset
operator|+
name|KeyValue
operator|.
name|ROW_OFFSET
argument_list|)
expr_stmt|;
name|rowRestLength
operator|=
name|rowLength
operator|+
name|KeyValue
operator|.
name|ROW_LENGTH_SIZE
operator|-
name|commonPrefix
expr_stmt|;
block|}
comment|// copy the rest of row
name|ByteBufferUtils
operator|.
name|copyFromStreamToBuffer
argument_list|(
name|buffer
argument_list|,
name|source
argument_list|,
name|rowRestLength
argument_list|)
expr_stmt|;
name|state
operator|.
name|rowLength
operator|=
name|rowLength
expr_stmt|;
comment|// copy the column family
name|buffer
operator|.
name|put
argument_list|(
name|state
operator|.
name|familyNameWithSize
argument_list|)
expr_stmt|;
name|keyRestLength
operator|=
name|keyLength
operator|-
name|rowLength
operator|-
name|state
operator|.
name|familyNameWithSize
operator|.
name|length
operator|-
operator|(
name|KeyValue
operator|.
name|ROW_LENGTH_SIZE
operator|+
name|KeyValue
operator|.
name|TIMESTAMP_TYPE_SIZE
operator|)
expr_stmt|;
block|}
else|else
block|{
comment|// prevRowWithSizeLength is the same as on previous row
name|keyRestLength
operator|=
name|keyLength
operator|-
name|commonPrefix
operator|-
name|KeyValue
operator|.
name|TIMESTAMP_TYPE_SIZE
expr_stmt|;
block|}
comment|// copy the rest of the key, after column family -> column qualifier
name|ByteBufferUtils
operator|.
name|copyFromStreamToBuffer
argument_list|(
name|buffer
argument_list|,
name|source
argument_list|,
name|keyRestLength
argument_list|)
expr_stmt|;
comment|// handle timestamp
name|int
name|timestampFitsInBytes
init|=
operator|(
operator|(
name|flag
operator|&
name|MASK_TIMESTAMP_LENGTH
operator|)
operator|>>>
name|SHIFT_TIMESTAMP_LENGTH
operator|)
operator|+
literal|1
decl_stmt|;
name|long
name|timestamp
init|=
name|ByteBufferUtils
operator|.
name|readLong
argument_list|(
name|source
argument_list|,
name|timestampFitsInBytes
argument_list|)
decl_stmt|;
if|if
condition|(
operator|(
name|flag
operator|&
name|FLAG_TIMESTAMP_SIGN
operator|)
operator|!=
literal|0
condition|)
block|{
name|timestamp
operator|=
operator|-
name|timestamp
expr_stmt|;
block|}
if|if
condition|(
operator|(
name|flag
operator|&
name|FLAG_TIMESTAMP_IS_DIFF
operator|)
operator|!=
literal|0
condition|)
block|{
name|timestamp
operator|=
name|state
operator|.
name|timestamp
operator|-
name|timestamp
expr_stmt|;
block|}
name|buffer
operator|.
name|putLong
argument_list|(
name|timestamp
argument_list|)
expr_stmt|;
comment|// copy the type field
name|byte
name|type
decl_stmt|;
if|if
condition|(
operator|(
name|flag
operator|&
name|FLAG_SAME_TYPE
operator|)
operator|!=
literal|0
condition|)
block|{
name|type
operator|=
name|state
operator|.
name|type
expr_stmt|;
block|}
else|else
block|{
name|type
operator|=
name|source
operator|.
name|readByte
argument_list|()
expr_stmt|;
block|}
name|buffer
operator|.
name|put
argument_list|(
name|type
argument_list|)
expr_stmt|;
comment|// copy value part
name|ByteBufferUtils
operator|.
name|copyFromStreamToBuffer
argument_list|(
name|buffer
argument_list|,
name|source
argument_list|,
name|valueLength
argument_list|)
expr_stmt|;
name|state
operator|.
name|keyLength
operator|=
name|keyLength
expr_stmt|;
name|state
operator|.
name|valueLength
operator|=
name|valueLength
expr_stmt|;
name|state
operator|.
name|prevOffset
operator|=
name|keyOffset
expr_stmt|;
name|state
operator|.
name|timestamp
operator|=
name|timestamp
expr_stmt|;
name|state
operator|.
name|type
operator|=
name|type
expr_stmt|;
comment|// state.qualifier is unused
block|}
annotation|@
name|Override
specifier|public
name|void
name|compressKeyValues
parameter_list|(
name|DataOutputStream
name|out
parameter_list|,
name|ByteBuffer
name|in
parameter_list|,
name|boolean
name|includesMemstoreTS
parameter_list|)
throws|throws
name|IOException
block|{
name|in
operator|.
name|rewind
argument_list|()
expr_stmt|;
name|ByteBufferUtils
operator|.
name|putInt
argument_list|(
name|out
argument_list|,
name|in
operator|.
name|limit
argument_list|()
argument_list|)
expr_stmt|;
name|DiffCompressionState
name|previousState
init|=
operator|new
name|DiffCompressionState
argument_list|()
decl_stmt|;
name|DiffCompressionState
name|currentState
init|=
operator|new
name|DiffCompressionState
argument_list|()
decl_stmt|;
while|while
condition|(
name|in
operator|.
name|hasRemaining
argument_list|()
condition|)
block|{
name|compressSingleKeyValue
argument_list|(
name|previousState
argument_list|,
name|currentState
argument_list|,
name|out
argument_list|,
name|in
argument_list|)
expr_stmt|;
name|afterEncodingKeyValue
argument_list|(
name|in
argument_list|,
name|out
argument_list|,
name|includesMemstoreTS
argument_list|)
expr_stmt|;
comment|// swap previousState<-> currentState
name|DiffCompressionState
name|tmp
init|=
name|previousState
decl_stmt|;
name|previousState
operator|=
name|currentState
expr_stmt|;
name|currentState
operator|=
name|tmp
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|ByteBuffer
name|uncompressKeyValues
parameter_list|(
name|DataInputStream
name|source
parameter_list|,
name|int
name|allocHeaderLength
parameter_list|,
name|int
name|skipLastBytes
parameter_list|,
name|boolean
name|includesMemstoreTS
parameter_list|)
throws|throws
name|IOException
block|{
name|int
name|decompressedSize
init|=
name|source
operator|.
name|readInt
argument_list|()
decl_stmt|;
name|ByteBuffer
name|buffer
init|=
name|ByteBuffer
operator|.
name|allocate
argument_list|(
name|decompressedSize
operator|+
name|allocHeaderLength
argument_list|)
decl_stmt|;
name|buffer
operator|.
name|position
argument_list|(
name|allocHeaderLength
argument_list|)
expr_stmt|;
name|DiffCompressionState
name|state
init|=
operator|new
name|DiffCompressionState
argument_list|()
decl_stmt|;
while|while
condition|(
name|source
operator|.
name|available
argument_list|()
operator|>
name|skipLastBytes
condition|)
block|{
name|uncompressSingleKeyValue
argument_list|(
name|source
argument_list|,
name|buffer
argument_list|,
name|state
argument_list|)
expr_stmt|;
name|afterDecodingKeyValue
argument_list|(
name|source
argument_list|,
name|buffer
argument_list|,
name|includesMemstoreTS
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|source
operator|.
name|available
argument_list|()
operator|!=
name|skipLastBytes
condition|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"Read too much bytes."
argument_list|)
throw|;
block|}
return|return
name|buffer
return|;
block|}
annotation|@
name|Override
specifier|public
name|ByteBuffer
name|getFirstKeyInBlock
parameter_list|(
name|ByteBuffer
name|block
parameter_list|)
block|{
name|block
operator|.
name|mark
argument_list|()
expr_stmt|;
name|block
operator|.
name|position
argument_list|(
name|Bytes
operator|.
name|SIZEOF_INT
argument_list|)
expr_stmt|;
name|byte
name|familyLength
init|=
name|block
operator|.
name|get
argument_list|()
decl_stmt|;
name|ByteBufferUtils
operator|.
name|skip
argument_list|(
name|block
argument_list|,
name|familyLength
argument_list|)
expr_stmt|;
name|byte
name|flag
init|=
name|block
operator|.
name|get
argument_list|()
decl_stmt|;
name|int
name|keyLength
init|=
name|ByteBufferUtils
operator|.
name|readCompressedInt
argument_list|(
name|block
argument_list|)
decl_stmt|;
name|ByteBufferUtils
operator|.
name|readCompressedInt
argument_list|(
name|block
argument_list|)
expr_stmt|;
comment|// valueLength
name|ByteBufferUtils
operator|.
name|readCompressedInt
argument_list|(
name|block
argument_list|)
expr_stmt|;
comment|// commonLength
name|ByteBuffer
name|result
init|=
name|ByteBuffer
operator|.
name|allocate
argument_list|(
name|keyLength
argument_list|)
decl_stmt|;
comment|// copy row
name|int
name|pos
init|=
name|result
operator|.
name|arrayOffset
argument_list|()
decl_stmt|;
name|block
operator|.
name|get
argument_list|(
name|result
operator|.
name|array
argument_list|()
argument_list|,
name|pos
argument_list|,
name|Bytes
operator|.
name|SIZEOF_SHORT
argument_list|)
expr_stmt|;
name|pos
operator|+=
name|Bytes
operator|.
name|SIZEOF_SHORT
expr_stmt|;
name|short
name|rowLength
init|=
name|result
operator|.
name|getShort
argument_list|()
decl_stmt|;
name|block
operator|.
name|get
argument_list|(
name|result
operator|.
name|array
argument_list|()
argument_list|,
name|pos
argument_list|,
name|rowLength
argument_list|)
expr_stmt|;
name|pos
operator|+=
name|rowLength
expr_stmt|;
comment|// copy family
name|int
name|savePosition
init|=
name|block
operator|.
name|position
argument_list|()
decl_stmt|;
name|block
operator|.
name|position
argument_list|(
name|Bytes
operator|.
name|SIZEOF_INT
argument_list|)
expr_stmt|;
name|block
operator|.
name|get
argument_list|(
name|result
operator|.
name|array
argument_list|()
argument_list|,
name|pos
argument_list|,
name|familyLength
operator|+
name|Bytes
operator|.
name|SIZEOF_BYTE
argument_list|)
expr_stmt|;
name|pos
operator|+=
name|familyLength
operator|+
name|Bytes
operator|.
name|SIZEOF_BYTE
expr_stmt|;
comment|// copy qualifier
name|block
operator|.
name|position
argument_list|(
name|savePosition
argument_list|)
expr_stmt|;
name|int
name|qualifierLength
init|=
name|keyLength
operator|-
name|pos
operator|+
name|result
operator|.
name|arrayOffset
argument_list|()
operator|-
name|KeyValue
operator|.
name|TIMESTAMP_TYPE_SIZE
decl_stmt|;
name|block
operator|.
name|get
argument_list|(
name|result
operator|.
name|array
argument_list|()
argument_list|,
name|pos
argument_list|,
name|qualifierLength
argument_list|)
expr_stmt|;
name|pos
operator|+=
name|qualifierLength
expr_stmt|;
comment|// copy the timestamp and type
name|int
name|timestampFitInBytes
init|=
operator|(
operator|(
name|flag
operator|&
name|MASK_TIMESTAMP_LENGTH
operator|)
operator|>>>
name|SHIFT_TIMESTAMP_LENGTH
operator|)
operator|+
literal|1
decl_stmt|;
name|long
name|timestamp
init|=
name|ByteBufferUtils
operator|.
name|readLong
argument_list|(
name|block
argument_list|,
name|timestampFitInBytes
argument_list|)
decl_stmt|;
if|if
condition|(
operator|(
name|flag
operator|&
name|FLAG_TIMESTAMP_SIGN
operator|)
operator|!=
literal|0
condition|)
block|{
name|timestamp
operator|=
operator|-
name|timestamp
expr_stmt|;
block|}
name|result
operator|.
name|putLong
argument_list|(
name|pos
argument_list|,
name|timestamp
argument_list|)
expr_stmt|;
name|pos
operator|+=
name|Bytes
operator|.
name|SIZEOF_LONG
expr_stmt|;
name|block
operator|.
name|get
argument_list|(
name|result
operator|.
name|array
argument_list|()
argument_list|,
name|pos
argument_list|,
name|Bytes
operator|.
name|SIZEOF_BYTE
argument_list|)
expr_stmt|;
name|block
operator|.
name|reset
argument_list|()
expr_stmt|;
return|return
name|result
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
name|DiffKeyDeltaEncoder
operator|.
name|class
operator|.
name|getSimpleName
argument_list|()
return|;
block|}
specifier|protected
specifier|static
class|class
name|DiffSeekerState
extends|extends
name|SeekerState
block|{
specifier|private
name|int
name|rowLengthWithSize
decl_stmt|;
specifier|private
name|long
name|timestamp
decl_stmt|;
annotation|@
name|Override
specifier|protected
name|void
name|copyFromNext
parameter_list|(
name|SeekerState
name|that
parameter_list|)
block|{
name|super
operator|.
name|copyFromNext
argument_list|(
name|that
argument_list|)
expr_stmt|;
name|DiffSeekerState
name|other
init|=
operator|(
name|DiffSeekerState
operator|)
name|that
decl_stmt|;
name|rowLengthWithSize
operator|=
name|other
operator|.
name|rowLengthWithSize
expr_stmt|;
name|timestamp
operator|=
name|other
operator|.
name|timestamp
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|EncodedSeeker
name|createSeeker
parameter_list|(
name|RawComparator
argument_list|<
name|byte
index|[]
argument_list|>
name|comparator
parameter_list|,
specifier|final
name|boolean
name|includesMemstoreTS
parameter_list|)
block|{
return|return
operator|new
name|BufferedEncodedSeeker
argument_list|<
name|DiffSeekerState
argument_list|>
argument_list|(
name|comparator
argument_list|)
block|{
specifier|private
name|byte
index|[]
name|familyNameWithSize
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|TIMESTAMP_WITH_TYPE_LENGTH
init|=
name|Bytes
operator|.
name|SIZEOF_LONG
operator|+
name|Bytes
operator|.
name|SIZEOF_BYTE
decl_stmt|;
specifier|private
name|void
name|decode
parameter_list|(
name|boolean
name|isFirst
parameter_list|)
block|{
name|byte
name|flag
init|=
name|currentBuffer
operator|.
name|get
argument_list|()
decl_stmt|;
name|byte
name|type
init|=
literal|0
decl_stmt|;
if|if
condition|(
operator|(
name|flag
operator|&
name|FLAG_SAME_KEY_LENGTH
operator|)
operator|==
literal|0
condition|)
block|{
if|if
condition|(
operator|!
name|isFirst
condition|)
block|{
name|type
operator|=
name|current
operator|.
name|keyBuffer
index|[
name|current
operator|.
name|keyLength
operator|-
name|Bytes
operator|.
name|SIZEOF_BYTE
index|]
expr_stmt|;
block|}
name|current
operator|.
name|keyLength
operator|=
name|ByteBufferUtils
operator|.
name|readCompressedInt
argument_list|(
name|currentBuffer
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
operator|(
name|flag
operator|&
name|FLAG_SAME_VALUE_LENGTH
operator|)
operator|==
literal|0
condition|)
block|{
name|current
operator|.
name|valueLength
operator|=
name|ByteBufferUtils
operator|.
name|readCompressedInt
argument_list|(
name|currentBuffer
argument_list|)
expr_stmt|;
block|}
name|current
operator|.
name|lastCommonPrefix
operator|=
name|ByteBufferUtils
operator|.
name|readCompressedInt
argument_list|(
name|currentBuffer
argument_list|)
expr_stmt|;
name|current
operator|.
name|ensureSpaceForKey
argument_list|()
expr_stmt|;
if|if
condition|(
name|current
operator|.
name|lastCommonPrefix
operator|<
name|Bytes
operator|.
name|SIZEOF_SHORT
condition|)
block|{
comment|// length of row is different, copy everything except family
comment|// copy the row size
name|currentBuffer
operator|.
name|get
argument_list|(
name|current
operator|.
name|keyBuffer
argument_list|,
name|current
operator|.
name|lastCommonPrefix
argument_list|,
name|Bytes
operator|.
name|SIZEOF_SHORT
operator|-
name|current
operator|.
name|lastCommonPrefix
argument_list|)
expr_stmt|;
name|current
operator|.
name|rowLengthWithSize
operator|=
name|Bytes
operator|.
name|toShort
argument_list|(
name|current
operator|.
name|keyBuffer
argument_list|,
literal|0
argument_list|)
operator|+
name|Bytes
operator|.
name|SIZEOF_SHORT
expr_stmt|;
comment|// copy the rest of row
name|currentBuffer
operator|.
name|get
argument_list|(
name|current
operator|.
name|keyBuffer
argument_list|,
name|Bytes
operator|.
name|SIZEOF_SHORT
argument_list|,
name|current
operator|.
name|rowLengthWithSize
operator|-
name|Bytes
operator|.
name|SIZEOF_SHORT
argument_list|)
expr_stmt|;
comment|// copy the column family
name|System
operator|.
name|arraycopy
argument_list|(
name|familyNameWithSize
argument_list|,
literal|0
argument_list|,
name|current
operator|.
name|keyBuffer
argument_list|,
name|current
operator|.
name|rowLengthWithSize
argument_list|,
name|familyNameWithSize
operator|.
name|length
argument_list|)
expr_stmt|;
comment|// copy the qualifier
name|currentBuffer
operator|.
name|get
argument_list|(
name|current
operator|.
name|keyBuffer
argument_list|,
name|current
operator|.
name|rowLengthWithSize
operator|+
name|familyNameWithSize
operator|.
name|length
argument_list|,
name|current
operator|.
name|keyLength
operator|-
name|current
operator|.
name|rowLengthWithSize
operator|-
name|familyNameWithSize
operator|.
name|length
operator|-
name|TIMESTAMP_WITH_TYPE_LENGTH
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|current
operator|.
name|lastCommonPrefix
operator|<
name|current
operator|.
name|rowLengthWithSize
condition|)
block|{
comment|// we have to copy part of row and qualifier,
comment|// but column family is in right place
comment|// before column family (rest of row)
name|currentBuffer
operator|.
name|get
argument_list|(
name|current
operator|.
name|keyBuffer
argument_list|,
name|current
operator|.
name|lastCommonPrefix
argument_list|,
name|current
operator|.
name|rowLengthWithSize
operator|-
name|current
operator|.
name|lastCommonPrefix
argument_list|)
expr_stmt|;
comment|// after column family (qualifier)
name|currentBuffer
operator|.
name|get
argument_list|(
name|current
operator|.
name|keyBuffer
argument_list|,
name|current
operator|.
name|rowLengthWithSize
operator|+
name|familyNameWithSize
operator|.
name|length
argument_list|,
name|current
operator|.
name|keyLength
operator|-
name|current
operator|.
name|rowLengthWithSize
operator|-
name|familyNameWithSize
operator|.
name|length
operator|-
name|TIMESTAMP_WITH_TYPE_LENGTH
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// copy just the ending
name|currentBuffer
operator|.
name|get
argument_list|(
name|current
operator|.
name|keyBuffer
argument_list|,
name|current
operator|.
name|lastCommonPrefix
argument_list|,
name|current
operator|.
name|keyLength
operator|-
name|TIMESTAMP_WITH_TYPE_LENGTH
operator|-
name|current
operator|.
name|lastCommonPrefix
argument_list|)
expr_stmt|;
block|}
comment|// timestamp
name|int
name|pos
init|=
name|current
operator|.
name|keyLength
operator|-
name|TIMESTAMP_WITH_TYPE_LENGTH
decl_stmt|;
name|int
name|timestampFitInBytes
init|=
literal|1
operator|+
operator|(
operator|(
name|flag
operator|&
name|MASK_TIMESTAMP_LENGTH
operator|)
operator|>>>
name|SHIFT_TIMESTAMP_LENGTH
operator|)
decl_stmt|;
name|long
name|timestampOrDiff
init|=
name|ByteBufferUtils
operator|.
name|readLong
argument_list|(
name|currentBuffer
argument_list|,
name|timestampFitInBytes
argument_list|)
decl_stmt|;
if|if
condition|(
operator|(
name|flag
operator|&
name|FLAG_TIMESTAMP_SIGN
operator|)
operator|!=
literal|0
condition|)
block|{
name|timestampOrDiff
operator|=
operator|-
name|timestampOrDiff
expr_stmt|;
block|}
if|if
condition|(
operator|(
name|flag
operator|&
name|FLAG_TIMESTAMP_IS_DIFF
operator|)
operator|==
literal|0
condition|)
block|{
comment|// it is timestamp
name|current
operator|.
name|timestamp
operator|=
name|timestampOrDiff
expr_stmt|;
block|}
else|else
block|{
comment|// it is diff
name|current
operator|.
name|timestamp
operator|=
name|current
operator|.
name|timestamp
operator|-
name|timestampOrDiff
expr_stmt|;
block|}
name|Bytes
operator|.
name|putLong
argument_list|(
name|current
operator|.
name|keyBuffer
argument_list|,
name|pos
argument_list|,
name|current
operator|.
name|timestamp
argument_list|)
expr_stmt|;
name|pos
operator|+=
name|Bytes
operator|.
name|SIZEOF_LONG
expr_stmt|;
comment|// type
if|if
condition|(
operator|(
name|flag
operator|&
name|FLAG_SAME_TYPE
operator|)
operator|==
literal|0
condition|)
block|{
name|currentBuffer
operator|.
name|get
argument_list|(
name|current
operator|.
name|keyBuffer
argument_list|,
name|pos
argument_list|,
name|Bytes
operator|.
name|SIZEOF_BYTE
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
operator|(
name|flag
operator|&
name|FLAG_SAME_KEY_LENGTH
operator|)
operator|==
literal|0
condition|)
block|{
name|current
operator|.
name|keyBuffer
index|[
name|pos
index|]
operator|=
name|type
expr_stmt|;
block|}
name|current
operator|.
name|valueOffset
operator|=
name|currentBuffer
operator|.
name|position
argument_list|()
expr_stmt|;
name|ByteBufferUtils
operator|.
name|skip
argument_list|(
name|currentBuffer
argument_list|,
name|current
operator|.
name|valueLength
argument_list|)
expr_stmt|;
if|if
condition|(
name|includesMemstoreTS
condition|)
block|{
name|current
operator|.
name|memstoreTS
operator|=
name|ByteBufferUtils
operator|.
name|readVLong
argument_list|(
name|currentBuffer
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|current
operator|.
name|memstoreTS
operator|=
literal|0
expr_stmt|;
block|}
name|current
operator|.
name|nextKvOffset
operator|=
name|currentBuffer
operator|.
name|position
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|decodeFirst
parameter_list|()
block|{
name|ByteBufferUtils
operator|.
name|skip
argument_list|(
name|currentBuffer
argument_list|,
name|Bytes
operator|.
name|SIZEOF_INT
argument_list|)
expr_stmt|;
comment|// read column family
name|byte
name|familyNameLength
init|=
name|currentBuffer
operator|.
name|get
argument_list|()
decl_stmt|;
name|familyNameWithSize
operator|=
operator|new
name|byte
index|[
name|familyNameLength
operator|+
name|Bytes
operator|.
name|SIZEOF_BYTE
index|]
expr_stmt|;
name|familyNameWithSize
index|[
literal|0
index|]
operator|=
name|familyNameLength
expr_stmt|;
name|currentBuffer
operator|.
name|get
argument_list|(
name|familyNameWithSize
argument_list|,
name|Bytes
operator|.
name|SIZEOF_BYTE
argument_list|,
name|familyNameLength
argument_list|)
expr_stmt|;
name|decode
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|decodeNext
parameter_list|()
block|{
name|decode
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|DiffSeekerState
name|createSeekerState
parameter_list|()
block|{
return|return
operator|new
name|DiffSeekerState
argument_list|()
return|;
block|}
block|}
return|;
block|}
block|}
end_class

end_unit

