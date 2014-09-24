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
name|IOException
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
name|security
operator|.
name|KeyException
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
name|fs
operator|.
name|HFileSystem
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
name|FSDataInputStreamWrapper
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
name|WritableUtils
import|;
end_import

begin_comment
comment|/**  * {@link HFile} reader for version 3.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|HFileReaderV3
extends|extends
name|HFileReaderV2
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
name|HFileReaderV3
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|int
name|MAX_MINOR_VERSION
init|=
literal|0
decl_stmt|;
comment|/**    * Opens a HFile. You must load the index before you can use it by calling    * {@link #loadFileInfo()}.    * @param path    *          Path to HFile.    * @param trailer    *          File trailer.    * @param fsdis    *          input stream.    * @param size    *          Length of the stream.    * @param cacheConf    *          Cache configuration.    * @param hfs    *          The file system.    * @param conf    *          Configuration    */
specifier|public
name|HFileReaderV3
parameter_list|(
specifier|final
name|Path
name|path
parameter_list|,
name|FixedFileTrailer
name|trailer
parameter_list|,
specifier|final
name|FSDataInputStreamWrapper
name|fsdis
parameter_list|,
specifier|final
name|long
name|size
parameter_list|,
specifier|final
name|CacheConfig
name|cacheConf
parameter_list|,
specifier|final
name|HFileSystem
name|hfs
parameter_list|,
specifier|final
name|Configuration
name|conf
parameter_list|)
throws|throws
name|IOException
block|{
name|super
argument_list|(
name|path
argument_list|,
name|trailer
argument_list|,
name|fsdis
argument_list|,
name|size
argument_list|,
name|cacheConf
argument_list|,
name|hfs
argument_list|,
name|conf
argument_list|)
expr_stmt|;
name|byte
index|[]
name|tmp
init|=
name|fileInfo
operator|.
name|get
argument_list|(
name|FileInfo
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
name|fileInfo
operator|.
name|get
argument_list|(
name|FileInfo
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
block|}
annotation|@
name|Override
specifier|protected
name|HFileContext
name|createHFileContext
parameter_list|(
name|FSDataInputStreamWrapper
name|fsdis
parameter_list|,
name|long
name|fileSize
parameter_list|,
name|HFileSystem
name|hfs
parameter_list|,
name|Path
name|path
parameter_list|,
name|FixedFileTrailer
name|trailer
parameter_list|)
throws|throws
name|IOException
block|{
name|trailer
operator|.
name|expectMajorVersion
argument_list|(
literal|3
argument_list|)
expr_stmt|;
name|HFileContextBuilder
name|builder
init|=
operator|new
name|HFileContextBuilder
argument_list|()
operator|.
name|withIncludesMvcc
argument_list|(
name|this
operator|.
name|includesMemstoreTS
argument_list|)
operator|.
name|withHBaseCheckSum
argument_list|(
literal|true
argument_list|)
operator|.
name|withCompression
argument_list|(
name|this
operator|.
name|compressAlgo
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
decl_stmt|;
name|String
name|masterKeyName
init|=
name|conf
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
decl_stmt|;
try|try
block|{
comment|// First try the master key
name|key
operator|=
name|EncryptionUtil
operator|.
name|unwrapKey
argument_list|(
name|conf
argument_list|,
name|masterKeyName
argument_list|,
name|keyBytes
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|KeyException
name|e
parameter_list|)
block|{
comment|// If the current master key fails to unwrap, try the alternate, if
comment|// one is configured
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Unable to unwrap key with current master key '"
operator|+
name|masterKeyName
operator|+
literal|"'"
argument_list|)
expr_stmt|;
block|}
name|String
name|alternateKeyName
init|=
name|conf
operator|.
name|get
argument_list|(
name|HConstants
operator|.
name|CRYPTO_MASTERKEY_ALTERNATE_NAME_CONF_KEY
argument_list|)
decl_stmt|;
if|if
condition|(
name|alternateKeyName
operator|!=
literal|null
condition|)
block|{
try|try
block|{
name|key
operator|=
name|EncryptionUtil
operator|.
name|unwrapKey
argument_list|(
name|conf
argument_list|,
name|alternateKeyName
argument_list|,
name|keyBytes
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|KeyException
name|ex
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
name|ex
argument_list|)
throw|;
block|}
block|}
else|else
block|{
throw|throw
operator|new
name|IOException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
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
literal|"Reader"
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
name|context
argument_list|)
expr_stmt|;
block|}
return|return
name|context
return|;
block|}
comment|/**    * Create a Scanner on this file. No seeks or reads are done on creation. Call    * {@link HFileScanner#seekTo(byte[])} to position an start the read. There is    * nothing to clean up in a Scanner. Letting go of your references to the    * scanner is sufficient.    * @param cacheBlocks    *          True if we should cache blocks read in by this scanner.    * @param pread    *          Use positional read rather than seek+read if true (pread is better    *          for random reads, seek+read is better scanning).    * @param isCompaction    *          is scanner being used for a compaction?    * @return Scanner on this file.    */
annotation|@
name|Override
specifier|public
name|HFileScanner
name|getScanner
parameter_list|(
name|boolean
name|cacheBlocks
parameter_list|,
specifier|final
name|boolean
name|pread
parameter_list|,
specifier|final
name|boolean
name|isCompaction
parameter_list|)
block|{
if|if
condition|(
name|dataBlockEncoder
operator|.
name|useEncodedScanner
argument_list|()
condition|)
block|{
return|return
operator|new
name|EncodedScannerV3
argument_list|(
name|this
argument_list|,
name|cacheBlocks
argument_list|,
name|pread
argument_list|,
name|isCompaction
argument_list|,
name|this
operator|.
name|hfileContext
argument_list|)
return|;
block|}
return|return
operator|new
name|ScannerV3
argument_list|(
name|this
argument_list|,
name|cacheBlocks
argument_list|,
name|pread
argument_list|,
name|isCompaction
argument_list|)
return|;
block|}
comment|/**    * Implementation of {@link HFileScanner} interface.    */
specifier|protected
specifier|static
class|class
name|ScannerV3
extends|extends
name|ScannerV2
block|{
specifier|private
name|HFileReaderV3
name|reader
decl_stmt|;
specifier|private
name|int
name|currTagsLen
decl_stmt|;
specifier|public
name|ScannerV3
parameter_list|(
name|HFileReaderV3
name|r
parameter_list|,
name|boolean
name|cacheBlocks
parameter_list|,
specifier|final
name|boolean
name|pread
parameter_list|,
specifier|final
name|boolean
name|isCompaction
parameter_list|)
block|{
name|super
argument_list|(
name|r
argument_list|,
name|cacheBlocks
argument_list|,
name|pread
argument_list|,
name|isCompaction
argument_list|)
expr_stmt|;
name|this
operator|.
name|reader
operator|=
name|r
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|int
name|getCellBufSize
parameter_list|()
block|{
name|int
name|kvBufSize
init|=
name|super
operator|.
name|getCellBufSize
argument_list|()
decl_stmt|;
if|if
condition|(
name|reader
operator|.
name|hfileContext
operator|.
name|isIncludesTags
argument_list|()
condition|)
block|{
name|kvBufSize
operator|+=
name|Bytes
operator|.
name|SIZEOF_SHORT
operator|+
name|currTagsLen
expr_stmt|;
block|}
return|return
name|kvBufSize
return|;
block|}
specifier|protected
name|void
name|setNonSeekedState
parameter_list|()
block|{
name|super
operator|.
name|setNonSeekedState
argument_list|()
expr_stmt|;
name|currTagsLen
operator|=
literal|0
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|int
name|getNextCellStartPosition
parameter_list|()
block|{
name|int
name|nextKvPos
init|=
name|super
operator|.
name|getNextCellStartPosition
argument_list|()
decl_stmt|;
if|if
condition|(
name|reader
operator|.
name|hfileContext
operator|.
name|isIncludesTags
argument_list|()
condition|)
block|{
name|nextKvPos
operator|+=
name|Bytes
operator|.
name|SIZEOF_SHORT
operator|+
name|currTagsLen
expr_stmt|;
block|}
return|return
name|nextKvPos
return|;
block|}
specifier|protected
name|void
name|readKeyValueLen
parameter_list|()
block|{
name|blockBuffer
operator|.
name|mark
argument_list|()
expr_stmt|;
name|currKeyLen
operator|=
name|blockBuffer
operator|.
name|getInt
argument_list|()
expr_stmt|;
name|currValueLen
operator|=
name|blockBuffer
operator|.
name|getInt
argument_list|()
expr_stmt|;
if|if
condition|(
name|currKeyLen
argument_list|<
literal|0
operator|||
name|currValueLen
argument_list|<
literal|0
operator|||
name|currKeyLen
argument_list|>
name|blockBuffer
operator|.
name|limit
operator|(
operator|)
operator|||
name|currValueLen
argument_list|>
name|blockBuffer
operator|.
name|limit
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"Invalid currKeyLen "
operator|+
name|currKeyLen
operator|+
literal|" or currValueLen "
operator|+
name|currValueLen
operator|+
literal|". Block offset: "
operator|+
name|block
operator|.
name|getOffset
argument_list|()
operator|+
literal|", block length: "
operator|+
name|blockBuffer
operator|.
name|limit
argument_list|()
operator|+
literal|", position: "
operator|+
name|blockBuffer
operator|.
name|position
argument_list|()
operator|+
literal|" (without header)."
argument_list|)
throw|;
block|}
name|ByteBufferUtils
operator|.
name|skip
argument_list|(
name|blockBuffer
argument_list|,
name|currKeyLen
operator|+
name|currValueLen
argument_list|)
expr_stmt|;
if|if
condition|(
name|reader
operator|.
name|hfileContext
operator|.
name|isIncludesTags
argument_list|()
condition|)
block|{
comment|// Read short as unsigned, high byte first
name|currTagsLen
operator|=
operator|(
operator|(
name|blockBuffer
operator|.
name|get
argument_list|()
operator|&
literal|0xff
operator|)
operator|<<
literal|8
operator|)
operator|^
operator|(
name|blockBuffer
operator|.
name|get
argument_list|()
operator|&
literal|0xff
operator|)
expr_stmt|;
if|if
condition|(
name|currTagsLen
argument_list|<
literal|0
operator|||
name|currTagsLen
argument_list|>
name|blockBuffer
operator|.
name|limit
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"Invalid currTagsLen "
operator|+
name|currTagsLen
operator|+
literal|". Block offset: "
operator|+
name|block
operator|.
name|getOffset
argument_list|()
operator|+
literal|", block length: "
operator|+
name|blockBuffer
operator|.
name|limit
argument_list|()
operator|+
literal|", position: "
operator|+
name|blockBuffer
operator|.
name|position
argument_list|()
operator|+
literal|" (without header)."
argument_list|)
throw|;
block|}
name|ByteBufferUtils
operator|.
name|skip
argument_list|(
name|blockBuffer
argument_list|,
name|currTagsLen
argument_list|)
expr_stmt|;
block|}
name|readMvccVersion
argument_list|()
expr_stmt|;
name|blockBuffer
operator|.
name|reset
argument_list|()
expr_stmt|;
block|}
comment|/**      * Within a loaded block, seek looking for the last key that is smaller than      * (or equal to?) the key we are interested in.      * A note on the seekBefore: if you have seekBefore = true, AND the first      * key in the block = key, then you'll get thrown exceptions. The caller has      * to check for that case and load the previous block as appropriate.      * @param key      *          the key to find      * @param seekBefore      *          find the key before the given key in case of exact match.      * @return 0 in case of an exact key match, 1 in case of an inexact match,      *         -2 in case of an inexact match and furthermore, the input key      *         less than the first key of current block(e.g. using a faked index      *         key)      */
annotation|@
name|Override
specifier|protected
name|int
name|blockSeek
parameter_list|(
name|Cell
name|key
parameter_list|,
name|boolean
name|seekBefore
parameter_list|)
block|{
name|int
name|klen
decl_stmt|,
name|vlen
decl_stmt|,
name|tlen
init|=
literal|0
decl_stmt|;
name|long
name|memstoreTS
init|=
literal|0
decl_stmt|;
name|int
name|memstoreTSLen
init|=
literal|0
decl_stmt|;
name|int
name|lastKeyValueSize
init|=
operator|-
literal|1
decl_stmt|;
name|KeyValue
operator|.
name|KeyOnlyKeyValue
name|keyOnlyKv
init|=
operator|new
name|KeyValue
operator|.
name|KeyOnlyKeyValue
argument_list|()
decl_stmt|;
do|do
block|{
name|blockBuffer
operator|.
name|mark
argument_list|()
expr_stmt|;
name|klen
operator|=
name|blockBuffer
operator|.
name|getInt
argument_list|()
expr_stmt|;
name|vlen
operator|=
name|blockBuffer
operator|.
name|getInt
argument_list|()
expr_stmt|;
if|if
condition|(
name|klen
argument_list|<
literal|0
operator|||
name|vlen
argument_list|<
literal|0
operator|||
name|klen
argument_list|>
name|blockBuffer
operator|.
name|limit
operator|(
operator|)
operator|||
name|vlen
argument_list|>
name|blockBuffer
operator|.
name|limit
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"Invalid klen "
operator|+
name|klen
operator|+
literal|" or vlen "
operator|+
name|vlen
operator|+
literal|". Block offset: "
operator|+
name|block
operator|.
name|getOffset
argument_list|()
operator|+
literal|", block length: "
operator|+
name|blockBuffer
operator|.
name|limit
argument_list|()
operator|+
literal|", position: "
operator|+
name|blockBuffer
operator|.
name|position
argument_list|()
operator|+
literal|" (without header)."
argument_list|)
throw|;
block|}
name|ByteBufferUtils
operator|.
name|skip
argument_list|(
name|blockBuffer
argument_list|,
name|klen
operator|+
name|vlen
argument_list|)
expr_stmt|;
if|if
condition|(
name|reader
operator|.
name|hfileContext
operator|.
name|isIncludesTags
argument_list|()
condition|)
block|{
comment|// Read short as unsigned, high byte first
name|tlen
operator|=
operator|(
operator|(
name|blockBuffer
operator|.
name|get
argument_list|()
operator|&
literal|0xff
operator|)
operator|<<
literal|8
operator|)
operator|^
operator|(
name|blockBuffer
operator|.
name|get
argument_list|()
operator|&
literal|0xff
operator|)
expr_stmt|;
if|if
condition|(
name|tlen
argument_list|<
literal|0
operator|||
name|tlen
argument_list|>
name|blockBuffer
operator|.
name|limit
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"Invalid tlen "
operator|+
name|tlen
operator|+
literal|". Block offset: "
operator|+
name|block
operator|.
name|getOffset
argument_list|()
operator|+
literal|", block length: "
operator|+
name|blockBuffer
operator|.
name|limit
argument_list|()
operator|+
literal|", position: "
operator|+
name|blockBuffer
operator|.
name|position
argument_list|()
operator|+
literal|" (without header)."
argument_list|)
throw|;
block|}
name|ByteBufferUtils
operator|.
name|skip
argument_list|(
name|blockBuffer
argument_list|,
name|tlen
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|this
operator|.
name|reader
operator|.
name|shouldIncludeMemstoreTS
argument_list|()
condition|)
block|{
if|if
condition|(
name|this
operator|.
name|reader
operator|.
name|decodeMemstoreTS
condition|)
block|{
try|try
block|{
name|memstoreTS
operator|=
name|Bytes
operator|.
name|readVLong
argument_list|(
name|blockBuffer
operator|.
name|array
argument_list|()
argument_list|,
name|blockBuffer
operator|.
name|arrayOffset
argument_list|()
operator|+
name|blockBuffer
operator|.
name|position
argument_list|()
argument_list|)
expr_stmt|;
name|memstoreTSLen
operator|=
name|WritableUtils
operator|.
name|getVIntSize
argument_list|(
name|memstoreTS
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Error reading memstore timestamp"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
else|else
block|{
name|memstoreTS
operator|=
literal|0
expr_stmt|;
name|memstoreTSLen
operator|=
literal|1
expr_stmt|;
block|}
block|}
name|blockBuffer
operator|.
name|reset
argument_list|()
expr_stmt|;
name|int
name|keyOffset
init|=
name|blockBuffer
operator|.
name|arrayOffset
argument_list|()
operator|+
name|blockBuffer
operator|.
name|position
argument_list|()
operator|+
operator|(
name|Bytes
operator|.
name|SIZEOF_INT
operator|*
literal|2
operator|)
decl_stmt|;
name|keyOnlyKv
operator|.
name|setKey
argument_list|(
name|blockBuffer
operator|.
name|array
argument_list|()
argument_list|,
name|keyOffset
argument_list|,
name|klen
argument_list|)
expr_stmt|;
name|int
name|comp
init|=
name|reader
operator|.
name|getComparator
argument_list|()
operator|.
name|compareOnlyKeyPortion
argument_list|(
name|key
argument_list|,
name|keyOnlyKv
argument_list|)
decl_stmt|;
if|if
condition|(
name|comp
operator|==
literal|0
condition|)
block|{
if|if
condition|(
name|seekBefore
condition|)
block|{
if|if
condition|(
name|lastKeyValueSize
operator|<
literal|0
condition|)
block|{
name|KeyValue
name|kv
init|=
name|KeyValueUtil
operator|.
name|ensureKeyValue
argument_list|(
name|key
argument_list|)
decl_stmt|;
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"blockSeek with seekBefore "
operator|+
literal|"at the first key of the block: key="
operator|+
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|kv
operator|.
name|getKey
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
argument_list|)
operator|+
literal|", blockOffset="
operator|+
name|block
operator|.
name|getOffset
argument_list|()
operator|+
literal|", onDiskSize="
operator|+
name|block
operator|.
name|getOnDiskSizeWithHeader
argument_list|()
argument_list|)
throw|;
block|}
name|blockBuffer
operator|.
name|position
argument_list|(
name|blockBuffer
operator|.
name|position
argument_list|()
operator|-
name|lastKeyValueSize
argument_list|)
expr_stmt|;
name|readKeyValueLen
argument_list|()
expr_stmt|;
return|return
literal|1
return|;
comment|// non exact match.
block|}
name|currKeyLen
operator|=
name|klen
expr_stmt|;
name|currValueLen
operator|=
name|vlen
expr_stmt|;
name|currTagsLen
operator|=
name|tlen
expr_stmt|;
if|if
condition|(
name|this
operator|.
name|reader
operator|.
name|shouldIncludeMemstoreTS
argument_list|()
condition|)
block|{
name|currMemstoreTS
operator|=
name|memstoreTS
expr_stmt|;
name|currMemstoreTSLen
operator|=
name|memstoreTSLen
expr_stmt|;
block|}
return|return
literal|0
return|;
comment|// indicate exact match
block|}
elseif|else
if|if
condition|(
name|comp
operator|<
literal|0
condition|)
block|{
if|if
condition|(
name|lastKeyValueSize
operator|>
literal|0
condition|)
name|blockBuffer
operator|.
name|position
argument_list|(
name|blockBuffer
operator|.
name|position
argument_list|()
operator|-
name|lastKeyValueSize
argument_list|)
expr_stmt|;
name|readKeyValueLen
argument_list|()
expr_stmt|;
if|if
condition|(
name|lastKeyValueSize
operator|==
operator|-
literal|1
operator|&&
name|blockBuffer
operator|.
name|position
argument_list|()
operator|==
literal|0
condition|)
block|{
return|return
name|HConstants
operator|.
name|INDEX_KEY_MAGIC
return|;
block|}
return|return
literal|1
return|;
block|}
comment|// The size of this key/value tuple, including key/value length fields.
name|lastKeyValueSize
operator|=
name|klen
operator|+
name|vlen
operator|+
name|memstoreTSLen
operator|+
name|KEY_VALUE_LEN_SIZE
expr_stmt|;
comment|// include tag length also if tags included with KV
if|if
condition|(
name|reader
operator|.
name|hfileContext
operator|.
name|isIncludesTags
argument_list|()
condition|)
block|{
name|lastKeyValueSize
operator|+=
name|tlen
operator|+
name|Bytes
operator|.
name|SIZEOF_SHORT
expr_stmt|;
block|}
name|blockBuffer
operator|.
name|position
argument_list|(
name|blockBuffer
operator|.
name|position
argument_list|()
operator|+
name|lastKeyValueSize
argument_list|)
expr_stmt|;
block|}
do|while
condition|(
name|blockBuffer
operator|.
name|remaining
argument_list|()
operator|>
literal|0
condition|)
do|;
comment|// Seek to the last key we successfully read. This will happen if this is
comment|// the last key/value pair in the file, in which case the following call
comment|// to next() has to return false.
name|blockBuffer
operator|.
name|position
argument_list|(
name|blockBuffer
operator|.
name|position
argument_list|()
operator|-
name|lastKeyValueSize
argument_list|)
expr_stmt|;
name|readKeyValueLen
argument_list|()
expr_stmt|;
return|return
literal|1
return|;
comment|// didn't exactly find it.
block|}
block|}
comment|/**    * ScannerV3 that operates on encoded data blocks.    */
specifier|protected
specifier|static
class|class
name|EncodedScannerV3
extends|extends
name|EncodedScannerV2
block|{
specifier|public
name|EncodedScannerV3
parameter_list|(
name|HFileReaderV3
name|reader
parameter_list|,
name|boolean
name|cacheBlocks
parameter_list|,
name|boolean
name|pread
parameter_list|,
name|boolean
name|isCompaction
parameter_list|,
name|HFileContext
name|context
parameter_list|)
block|{
name|super
argument_list|(
name|reader
argument_list|,
name|cacheBlocks
argument_list|,
name|pread
argument_list|,
name|isCompaction
argument_list|,
name|context
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|int
name|getMajorVersion
parameter_list|()
block|{
return|return
literal|3
return|;
block|}
block|}
end_class

end_unit

