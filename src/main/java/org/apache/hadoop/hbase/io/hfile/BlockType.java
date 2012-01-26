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
name|DataInputStream
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
name|io
operator|.
name|OutputStream
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
name|util
operator|.
name|Bytes
import|;
end_import

begin_comment
comment|/**  * Various types of {@link HFile} blocks. Ordinal values of these enum constants  * must not be relied upon. The values in the enum appear in the order they  * appear in a version 2 {@link HFile}.  */
end_comment

begin_enum
specifier|public
enum|enum
name|BlockType
block|{
comment|// Scanned block section
comment|/** Data block, both versions */
name|DATA
argument_list|(
literal|"DATABLK*"
argument_list|,
name|BlockCategory
operator|.
name|DATA
argument_list|)
block|,
comment|/** An encoded data block (e.g. with prefix compression), version 2 */
name|ENCODED_DATA
argument_list|(
literal|"DATABLKE"
argument_list|,
name|BlockCategory
operator|.
name|DATA
argument_list|)
block|{
annotation|@
name|Override
specifier|public
name|int
name|getId
parameter_list|()
block|{
return|return
name|DATA
operator|.
name|ordinal
argument_list|()
return|;
block|}
block|}
block|,
comment|/** Version 2 leaf index block. Appears in the data block section */
name|LEAF_INDEX
argument_list|(
literal|"IDXLEAF2"
argument_list|,
name|BlockCategory
operator|.
name|INDEX
argument_list|)
block|,
comment|/** Bloom filter block, version 2 */
name|BLOOM_CHUNK
argument_list|(
literal|"BLMFBLK2"
argument_list|,
name|BlockCategory
operator|.
name|BLOOM
argument_list|)
block|,
comment|// Non-scanned block section
comment|/** Meta blocks */
name|META
argument_list|(
literal|"METABLKc"
argument_list|,
name|BlockCategory
operator|.
name|META
argument_list|)
block|,
comment|/** Intermediate-level version 2 index in the non-data block section */
name|INTERMEDIATE_INDEX
argument_list|(
literal|"IDXINTE2"
argument_list|,
name|BlockCategory
operator|.
name|INDEX
argument_list|)
block|,
comment|// Load-on-open section.
comment|/** Root index block, also used for the single-level meta index, version 2 */
name|ROOT_INDEX
argument_list|(
literal|"IDXROOT2"
argument_list|,
name|BlockCategory
operator|.
name|INDEX
argument_list|)
block|,
comment|/** File info, version 2 */
name|FILE_INFO
argument_list|(
literal|"FILEINF2"
argument_list|,
name|BlockCategory
operator|.
name|META
argument_list|)
block|,
comment|/** General Bloom filter metadata, version 2 */
name|GENERAL_BLOOM_META
argument_list|(
literal|"BLMFMET2"
argument_list|,
name|BlockCategory
operator|.
name|BLOOM
argument_list|)
block|,
comment|/** Delete Family Bloom filter metadata, version 2 */
name|DELETE_FAMILY_BLOOM_META
argument_list|(
literal|"DFBLMET2"
argument_list|,
name|BlockCategory
operator|.
name|BLOOM
argument_list|)
block|,
comment|// Trailer
comment|/** Fixed file trailer, both versions (always just a magic string) */
name|TRAILER
argument_list|(
literal|"TRABLK\"$"
argument_list|,
name|BlockCategory
operator|.
name|META
argument_list|)
block|,
comment|// Legacy blocks
comment|/** Block index magic string in version 1 */
name|INDEX_V1
argument_list|(
literal|"IDXBLK)+"
argument_list|,
name|BlockCategory
operator|.
name|INDEX
argument_list|)
block|;
specifier|public
enum|enum
name|BlockCategory
block|{
name|DATA
block|,
name|META
block|,
name|INDEX
block|,
name|BLOOM
block|,
name|ALL_CATEGORIES
block|,
name|UNKNOWN
block|;
comment|/**      * Throws an exception if the block category passed is the special category      * meaning "all categories".      */
specifier|public
name|void
name|expectSpecific
parameter_list|()
block|{
if|if
condition|(
name|this
operator|==
name|ALL_CATEGORIES
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Expected a specific block "
operator|+
literal|"category but got "
operator|+
name|this
argument_list|)
throw|;
block|}
block|}
block|}
specifier|public
specifier|static
specifier|final
name|int
name|MAGIC_LENGTH
init|=
literal|8
decl_stmt|;
specifier|private
specifier|final
name|byte
index|[]
name|magic
decl_stmt|;
specifier|private
specifier|final
name|BlockCategory
name|metricCat
decl_stmt|;
specifier|private
name|BlockType
parameter_list|(
name|String
name|magicStr
parameter_list|,
name|BlockCategory
name|metricCat
parameter_list|)
block|{
name|magic
operator|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|magicStr
argument_list|)
expr_stmt|;
name|this
operator|.
name|metricCat
operator|=
name|metricCat
expr_stmt|;
assert|assert
name|magic
operator|.
name|length
operator|==
name|MAGIC_LENGTH
assert|;
block|}
comment|/**    * Use this instead of {@link #ordinal()}. They work exactly the same, except    * DATA and ENCODED_DATA get the same id using this method (overridden for    * {@link #ENCODED_DATA}).    * @return block type id from 0 to the number of block types - 1    */
specifier|public
name|int
name|getId
parameter_list|()
block|{
comment|// Default implementation, can be overridden for individual enum members.
return|return
name|ordinal
argument_list|()
return|;
block|}
specifier|public
name|void
name|writeToStream
parameter_list|(
name|OutputStream
name|out
parameter_list|)
throws|throws
name|IOException
block|{
name|out
operator|.
name|write
argument_list|(
name|magic
argument_list|)
expr_stmt|;
block|}
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
name|out
operator|.
name|write
argument_list|(
name|magic
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|write
parameter_list|(
name|ByteBuffer
name|buf
parameter_list|)
block|{
name|buf
operator|.
name|put
argument_list|(
name|magic
argument_list|)
expr_stmt|;
block|}
specifier|public
name|BlockCategory
name|getCategory
parameter_list|()
block|{
return|return
name|metricCat
return|;
block|}
specifier|public
specifier|static
name|BlockType
name|parse
parameter_list|(
name|byte
index|[]
name|buf
parameter_list|,
name|int
name|offset
parameter_list|,
name|int
name|length
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|length
operator|!=
name|MAGIC_LENGTH
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Magic record of invalid length: "
operator|+
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|buf
argument_list|,
name|offset
argument_list|,
name|length
argument_list|)
argument_list|)
throw|;
block|}
for|for
control|(
name|BlockType
name|blockType
range|:
name|values
argument_list|()
control|)
if|if
condition|(
name|Bytes
operator|.
name|compareTo
argument_list|(
name|blockType
operator|.
name|magic
argument_list|,
literal|0
argument_list|,
name|MAGIC_LENGTH
argument_list|,
name|buf
argument_list|,
name|offset
argument_list|,
name|MAGIC_LENGTH
argument_list|)
operator|==
literal|0
condition|)
return|return
name|blockType
return|;
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Invalid HFile block magic: "
operator|+
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|buf
argument_list|,
name|offset
argument_list|,
name|MAGIC_LENGTH
argument_list|)
argument_list|)
throw|;
block|}
specifier|public
specifier|static
name|BlockType
name|read
parameter_list|(
name|DataInputStream
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|byte
index|[]
name|buf
init|=
operator|new
name|byte
index|[
name|MAGIC_LENGTH
index|]
decl_stmt|;
name|in
operator|.
name|readFully
argument_list|(
name|buf
argument_list|)
expr_stmt|;
return|return
name|parse
argument_list|(
name|buf
argument_list|,
literal|0
argument_list|,
name|buf
operator|.
name|length
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|BlockType
name|read
parameter_list|(
name|ByteBuffer
name|buf
parameter_list|)
throws|throws
name|IOException
block|{
name|BlockType
name|blockType
init|=
name|parse
argument_list|(
name|buf
operator|.
name|array
argument_list|()
argument_list|,
name|buf
operator|.
name|arrayOffset
argument_list|()
operator|+
name|buf
operator|.
name|position
argument_list|()
argument_list|,
name|Math
operator|.
name|min
argument_list|(
name|buf
operator|.
name|limit
argument_list|()
operator|-
name|buf
operator|.
name|position
argument_list|()
argument_list|,
name|MAGIC_LENGTH
argument_list|)
argument_list|)
decl_stmt|;
comment|// If we got here, we have read exactly MAGIC_LENGTH bytes.
name|buf
operator|.
name|position
argument_list|(
name|buf
operator|.
name|position
argument_list|()
operator|+
name|MAGIC_LENGTH
argument_list|)
expr_stmt|;
return|return
name|blockType
return|;
block|}
comment|/**    * Put the magic record out to the specified byte array position.    *    * @param bytes the byte array    * @param offset position in the array    * @return incremented offset    */
specifier|public
name|int
name|put
parameter_list|(
name|byte
index|[]
name|bytes
parameter_list|,
name|int
name|offset
parameter_list|)
block|{
name|System
operator|.
name|arraycopy
argument_list|(
name|magic
argument_list|,
literal|0
argument_list|,
name|bytes
argument_list|,
name|offset
argument_list|,
name|MAGIC_LENGTH
argument_list|)
expr_stmt|;
return|return
name|offset
operator|+
name|MAGIC_LENGTH
return|;
block|}
comment|/**    * Reads a magic record of the length {@link #MAGIC_LENGTH} from the given    * stream and expects it to match this block type.    */
specifier|public
name|void
name|readAndCheck
parameter_list|(
name|DataInputStream
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|byte
index|[]
name|buf
init|=
operator|new
name|byte
index|[
name|MAGIC_LENGTH
index|]
decl_stmt|;
name|in
operator|.
name|readFully
argument_list|(
name|buf
argument_list|)
expr_stmt|;
if|if
condition|(
name|Bytes
operator|.
name|compareTo
argument_list|(
name|buf
argument_list|,
name|magic
argument_list|)
operator|!=
literal|0
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Invalid magic: expected "
operator|+
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|magic
argument_list|)
operator|+
literal|", got "
operator|+
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|buf
argument_list|)
argument_list|)
throw|;
block|}
block|}
comment|/**    * Reads a magic record of the length {@link #MAGIC_LENGTH} from the given    * byte buffer and expects it to match this block type.    */
specifier|public
name|void
name|readAndCheck
parameter_list|(
name|ByteBuffer
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|byte
index|[]
name|buf
init|=
operator|new
name|byte
index|[
name|MAGIC_LENGTH
index|]
decl_stmt|;
name|in
operator|.
name|get
argument_list|(
name|buf
argument_list|)
expr_stmt|;
if|if
condition|(
name|Bytes
operator|.
name|compareTo
argument_list|(
name|buf
argument_list|,
name|magic
argument_list|)
operator|!=
literal|0
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Invalid magic: expected "
operator|+
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|magic
argument_list|)
operator|+
literal|", got "
operator|+
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|buf
argument_list|)
argument_list|)
throw|;
block|}
block|}
comment|/**    * @return whether this block type is encoded or unencoded data block    */
specifier|public
specifier|final
name|boolean
name|isData
parameter_list|()
block|{
return|return
name|this
operator|==
name|DATA
operator|||
name|this
operator|==
name|ENCODED_DATA
return|;
block|}
block|}
end_enum

end_unit

