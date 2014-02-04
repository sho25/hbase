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
name|FSDataOutputStream
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
name|FileSystem
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
name|KeyValue
operator|.
name|KVComparator
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
name|io
operator|.
name|encoding
operator|.
name|DataBlockEncoding
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
name|hfile
operator|.
name|HFile
operator|.
name|FileInfo
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
name|hfile
operator|.
name|HFile
operator|.
name|Writer
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
name|WritableUtils
import|;
end_import

begin_comment
comment|/**  * {@link HFile} writer for version 3.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|HFileWriterV3
extends|extends
name|HFileWriterV2
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
name|HFileWriterV3
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|int
name|maxTagsLength
init|=
literal|0
decl_stmt|;
specifier|static
class|class
name|WriterFactoryV3
extends|extends
name|HFile
operator|.
name|WriterFactory
block|{
name|WriterFactoryV3
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|CacheConfig
name|cacheConf
parameter_list|)
block|{
name|super
argument_list|(
name|conf
argument_list|,
name|cacheConf
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Writer
name|createWriter
parameter_list|(
name|FileSystem
name|fs
parameter_list|,
name|Path
name|path
parameter_list|,
name|FSDataOutputStream
name|ostream
parameter_list|,
specifier|final
name|KVComparator
name|comparator
parameter_list|,
name|HFileContext
name|fileContext
parameter_list|)
throws|throws
name|IOException
block|{
return|return
operator|new
name|HFileWriterV3
argument_list|(
name|conf
argument_list|,
name|cacheConf
argument_list|,
name|fs
argument_list|,
name|path
argument_list|,
name|ostream
argument_list|,
name|comparator
argument_list|,
name|fileContext
argument_list|)
return|;
block|}
block|}
comment|/** Constructor that takes a path, creates and closes the output stream. */
specifier|public
name|HFileWriterV3
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|CacheConfig
name|cacheConf
parameter_list|,
name|FileSystem
name|fs
parameter_list|,
name|Path
name|path
parameter_list|,
name|FSDataOutputStream
name|ostream
parameter_list|,
specifier|final
name|KVComparator
name|comparator
parameter_list|,
specifier|final
name|HFileContext
name|fileContext
parameter_list|)
throws|throws
name|IOException
block|{
name|super
argument_list|(
name|conf
argument_list|,
name|cacheConf
argument_list|,
name|fs
argument_list|,
name|path
argument_list|,
name|ostream
argument_list|,
name|comparator
argument_list|,
name|fileContext
argument_list|)
expr_stmt|;
if|if
condition|(
name|LOG
operator|.
name|isTraceEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|trace
argument_list|(
literal|"Writer"
operator|+
operator|(
name|path
operator|!=
literal|null
condition|?
literal|" for "
operator|+
name|path
else|:
literal|""
operator|)
operator|+
literal|" initialized with cacheConf: "
operator|+
name|cacheConf
operator|+
literal|" comparator: "
operator|+
name|comparator
operator|.
name|getClass
argument_list|()
operator|.
name|getSimpleName
argument_list|()
operator|+
literal|" fileContext: "
operator|+
name|fileContext
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Add key/value to file. Keys must be added in an order that agrees with the    * Comparator passed on construction.    *     * @param kv    *          KeyValue to add. Cannot be empty nor null.    * @throws IOException    */
annotation|@
name|Override
specifier|public
name|void
name|append
parameter_list|(
specifier|final
name|KeyValue
name|kv
parameter_list|)
throws|throws
name|IOException
block|{
comment|// Currently get the complete arrays
name|append
argument_list|(
name|kv
operator|.
name|getMvccVersion
argument_list|()
argument_list|,
name|kv
operator|.
name|getBuffer
argument_list|()
argument_list|,
name|kv
operator|.
name|getKeyOffset
argument_list|()
argument_list|,
name|kv
operator|.
name|getKeyLength
argument_list|()
argument_list|,
name|kv
operator|.
name|getValueArray
argument_list|()
argument_list|,
name|kv
operator|.
name|getValueOffset
argument_list|()
argument_list|,
name|kv
operator|.
name|getValueLength
argument_list|()
argument_list|,
name|kv
operator|.
name|getTagsArray
argument_list|()
argument_list|,
name|kv
operator|.
name|getTagsOffset
argument_list|()
argument_list|,
name|kv
operator|.
name|getTagsLength
argument_list|()
argument_list|)
expr_stmt|;
name|this
operator|.
name|maxMemstoreTS
operator|=
name|Math
operator|.
name|max
argument_list|(
name|this
operator|.
name|maxMemstoreTS
argument_list|,
name|kv
operator|.
name|getMvccVersion
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**    * Add key/value to file. Keys must be added in an order that agrees with the    * Comparator passed on construction.    * @param key    *          Key to add. Cannot be empty nor null.    * @param value    *          Value to add. Cannot be empty nor null.    * @throws IOException    */
annotation|@
name|Override
specifier|public
name|void
name|append
parameter_list|(
specifier|final
name|byte
index|[]
name|key
parameter_list|,
specifier|final
name|byte
index|[]
name|value
parameter_list|)
throws|throws
name|IOException
block|{
name|append
argument_list|(
name|key
argument_list|,
name|value
argument_list|,
name|HConstants
operator|.
name|EMPTY_BYTE_ARRAY
argument_list|)
expr_stmt|;
block|}
comment|/**    * Add key/value to file. Keys must be added in an order that agrees with the    * Comparator passed on construction.    * @param key    *          Key to add. Cannot be empty nor null.    * @param value    *          Value to add. Cannot be empty nor null.    * @param tag    *          Tag t add. Cannot be empty or null.    * @throws IOException    */
annotation|@
name|Override
specifier|public
name|void
name|append
parameter_list|(
specifier|final
name|byte
index|[]
name|key
parameter_list|,
specifier|final
name|byte
index|[]
name|value
parameter_list|,
name|byte
index|[]
name|tag
parameter_list|)
throws|throws
name|IOException
block|{
name|append
argument_list|(
literal|0
argument_list|,
name|key
argument_list|,
literal|0
argument_list|,
name|key
operator|.
name|length
argument_list|,
name|value
argument_list|,
literal|0
argument_list|,
name|value
operator|.
name|length
argument_list|,
name|tag
argument_list|,
literal|0
argument_list|,
name|tag
operator|.
name|length
argument_list|)
expr_stmt|;
block|}
comment|/**    * Add key/value to file. Keys must be added in an order that agrees with the    * Comparator passed on construction.    * @param key    * @param koffset    * @param klength    * @param value    * @param voffset    * @param vlength    * @param tag    * @param tagsOffset    * @param tagLength    * @throws IOException    */
specifier|private
name|void
name|append
parameter_list|(
specifier|final
name|long
name|memstoreTS
parameter_list|,
specifier|final
name|byte
index|[]
name|key
parameter_list|,
specifier|final
name|int
name|koffset
parameter_list|,
specifier|final
name|int
name|klength
parameter_list|,
specifier|final
name|byte
index|[]
name|value
parameter_list|,
specifier|final
name|int
name|voffset
parameter_list|,
specifier|final
name|int
name|vlength
parameter_list|,
specifier|final
name|byte
index|[]
name|tag
parameter_list|,
specifier|final
name|int
name|tagsOffset
parameter_list|,
specifier|final
name|int
name|tagsLength
parameter_list|)
throws|throws
name|IOException
block|{
name|boolean
name|dupKey
init|=
name|checkKey
argument_list|(
name|key
argument_list|,
name|koffset
argument_list|,
name|klength
argument_list|)
decl_stmt|;
name|checkValue
argument_list|(
name|value
argument_list|,
name|voffset
argument_list|,
name|vlength
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|dupKey
condition|)
block|{
name|checkBlockBoundary
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|fsBlockWriter
operator|.
name|isWriting
argument_list|()
condition|)
name|newBlock
argument_list|()
expr_stmt|;
comment|// Write length of key and value and then actual key and value bytes.
comment|// Additionally, we may also write down the memstoreTS.
block|{
name|DataOutputStream
name|out
init|=
name|fsBlockWriter
operator|.
name|getUserDataStream
argument_list|()
decl_stmt|;
name|out
operator|.
name|writeInt
argument_list|(
name|klength
argument_list|)
expr_stmt|;
name|totalKeyLength
operator|+=
name|klength
expr_stmt|;
name|out
operator|.
name|writeInt
argument_list|(
name|vlength
argument_list|)
expr_stmt|;
name|totalValueLength
operator|+=
name|vlength
expr_stmt|;
name|out
operator|.
name|write
argument_list|(
name|key
argument_list|,
name|koffset
argument_list|,
name|klength
argument_list|)
expr_stmt|;
name|out
operator|.
name|write
argument_list|(
name|value
argument_list|,
name|voffset
argument_list|,
name|vlength
argument_list|)
expr_stmt|;
comment|// Write the additional tag into the stream
if|if
condition|(
name|hFileContext
operator|.
name|isIncludesTags
argument_list|()
condition|)
block|{
name|out
operator|.
name|writeShort
argument_list|(
operator|(
name|short
operator|)
name|tagsLength
argument_list|)
expr_stmt|;
if|if
condition|(
name|tagsLength
operator|>
literal|0
condition|)
block|{
name|out
operator|.
name|write
argument_list|(
name|tag
argument_list|,
name|tagsOffset
argument_list|,
name|tagsLength
argument_list|)
expr_stmt|;
if|if
condition|(
name|tagsLength
operator|>
name|maxTagsLength
condition|)
block|{
name|maxTagsLength
operator|=
name|tagsLength
expr_stmt|;
block|}
block|}
block|}
if|if
condition|(
name|this
operator|.
name|hFileContext
operator|.
name|isIncludesMvcc
argument_list|()
condition|)
block|{
name|WritableUtils
operator|.
name|writeVLong
argument_list|(
name|out
argument_list|,
name|memstoreTS
argument_list|)
expr_stmt|;
block|}
block|}
comment|// Are we the first key in this block?
if|if
condition|(
name|firstKeyInBlock
operator|==
literal|null
condition|)
block|{
comment|// Copy the key.
name|firstKeyInBlock
operator|=
operator|new
name|byte
index|[
name|klength
index|]
expr_stmt|;
name|System
operator|.
name|arraycopy
argument_list|(
name|key
argument_list|,
name|koffset
argument_list|,
name|firstKeyInBlock
argument_list|,
literal|0
argument_list|,
name|klength
argument_list|)
expr_stmt|;
block|}
name|lastKeyBuffer
operator|=
name|key
expr_stmt|;
name|lastKeyOffset
operator|=
name|koffset
expr_stmt|;
name|lastKeyLength
operator|=
name|klength
expr_stmt|;
name|entryCount
operator|++
expr_stmt|;
block|}
specifier|protected
name|void
name|finishFileInfo
parameter_list|()
throws|throws
name|IOException
block|{
name|super
operator|.
name|finishFileInfo
argument_list|()
expr_stmt|;
if|if
condition|(
name|hFileContext
operator|.
name|getDataBlockEncoding
argument_list|()
operator|==
name|DataBlockEncoding
operator|.
name|PREFIX_TREE
condition|)
block|{
comment|// In case of Prefix Tree encoding, we always write tags information into HFiles even if all
comment|// KVs are having no tags.
name|fileInfo
operator|.
name|append
argument_list|(
name|FileInfo
operator|.
name|MAX_TAGS_LEN
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|this
operator|.
name|maxTagsLength
argument_list|)
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|hFileContext
operator|.
name|isIncludesTags
argument_list|()
condition|)
block|{
comment|// When tags are not being written in this file, MAX_TAGS_LEN is excluded
comment|// from the FileInfo
name|fileInfo
operator|.
name|append
argument_list|(
name|FileInfo
operator|.
name|MAX_TAGS_LEN
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|this
operator|.
name|maxTagsLength
argument_list|)
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|boolean
name|tagsCompressed
init|=
operator|(
name|hFileContext
operator|.
name|getDataBlockEncoding
argument_list|()
operator|!=
name|DataBlockEncoding
operator|.
name|NONE
operator|)
operator|&&
name|hFileContext
operator|.
name|isCompressTags
argument_list|()
decl_stmt|;
name|fileInfo
operator|.
name|append
argument_list|(
name|FileInfo
operator|.
name|TAGS_COMPRESSED
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|tagsCompressed
argument_list|)
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|protected
name|int
name|getMajorVersion
parameter_list|()
block|{
return|return
literal|3
return|;
block|}
annotation|@
name|Override
specifier|protected
name|int
name|getMinorVersion
parameter_list|()
block|{
return|return
name|HFileReaderV3
operator|.
name|MAX_MINOR_VERSION
return|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|finishClose
parameter_list|(
name|FixedFileTrailer
name|trailer
parameter_list|)
throws|throws
name|IOException
block|{
comment|// Write out encryption metadata before finalizing if we have a valid crypto context
name|Encryption
operator|.
name|Context
name|cryptoContext
init|=
name|hFileContext
operator|.
name|getEncryptionContext
argument_list|()
decl_stmt|;
if|if
condition|(
name|cryptoContext
operator|!=
name|Encryption
operator|.
name|Context
operator|.
name|NONE
condition|)
block|{
comment|// Wrap the context's key and write it as the encryption metadata, the wrapper includes
comment|// all information needed for decryption
name|trailer
operator|.
name|setEncryptionKey
argument_list|(
name|EncryptionUtil
operator|.
name|wrapKey
argument_list|(
name|cryptoContext
operator|.
name|getConf
argument_list|()
argument_list|,
name|cryptoContext
operator|.
name|getConf
argument_list|()
operator|.
name|get
argument_list|(
name|HConstants
operator|.
name|CRYPTO_MASTERKEY_NAME_CONF_KEY
argument_list|,
name|User
operator|.
name|getCurrent
argument_list|()
operator|.
name|getShortName
argument_list|()
argument_list|)
argument_list|,
name|cryptoContext
operator|.
name|getKey
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// Now we can finish the close
name|super
operator|.
name|finishClose
argument_list|(
name|trailer
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

