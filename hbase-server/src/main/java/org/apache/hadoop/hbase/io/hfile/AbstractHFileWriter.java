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
name|Arrays
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
name|fs
operator|.
name|permission
operator|.
name|FsPermission
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
operator|.
name|KeyComparator
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
name|compress
operator|.
name|Compression
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
name|FSUtils
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
comment|/**  * Common functionality needed by all versions of {@link HFile} writers.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
specifier|abstract
class|class
name|AbstractHFileWriter
implements|implements
name|HFile
operator|.
name|Writer
block|{
comment|/** Key previously appended. Becomes the last key in the file. */
specifier|protected
name|byte
index|[]
name|lastKeyBuffer
init|=
literal|null
decl_stmt|;
specifier|protected
name|int
name|lastKeyOffset
init|=
operator|-
literal|1
decl_stmt|;
specifier|protected
name|int
name|lastKeyLength
init|=
operator|-
literal|1
decl_stmt|;
comment|/** FileSystem stream to write into. */
specifier|protected
name|FSDataOutputStream
name|outputStream
decl_stmt|;
comment|/** True if we opened the<code>outputStream</code> (and so will close it). */
specifier|protected
specifier|final
name|boolean
name|closeOutputStream
decl_stmt|;
comment|/** A "file info" block: a key-value map of file-wide metadata. */
specifier|protected
name|FileInfo
name|fileInfo
init|=
operator|new
name|HFile
operator|.
name|FileInfo
argument_list|()
decl_stmt|;
comment|/** Number of uncompressed bytes we allow per block. */
specifier|protected
specifier|final
name|int
name|blockSize
decl_stmt|;
comment|/** Total # of key/value entries, i.e. how many times add() was called. */
specifier|protected
name|long
name|entryCount
init|=
literal|0
decl_stmt|;
comment|/** Used for calculating the average key length. */
specifier|protected
name|long
name|totalKeyLength
init|=
literal|0
decl_stmt|;
comment|/** Used for calculating the average value length. */
specifier|protected
name|long
name|totalValueLength
init|=
literal|0
decl_stmt|;
comment|/** Total uncompressed bytes, maybe calculate a compression ratio later. */
specifier|protected
name|long
name|totalUncompressedBytes
init|=
literal|0
decl_stmt|;
comment|/** Key comparator. Used to ensure we write in order. */
specifier|protected
specifier|final
name|RawComparator
argument_list|<
name|byte
index|[]
argument_list|>
name|comparator
decl_stmt|;
comment|/** Meta block names. */
specifier|protected
name|List
argument_list|<
name|byte
index|[]
argument_list|>
name|metaNames
init|=
operator|new
name|ArrayList
argument_list|<
name|byte
index|[]
argument_list|>
argument_list|()
decl_stmt|;
comment|/** {@link Writable}s representing meta block data. */
specifier|protected
name|List
argument_list|<
name|Writable
argument_list|>
name|metaData
init|=
operator|new
name|ArrayList
argument_list|<
name|Writable
argument_list|>
argument_list|()
decl_stmt|;
comment|/** The compression algorithm used. NONE if no compression. */
specifier|protected
specifier|final
name|Compression
operator|.
name|Algorithm
name|compressAlgo
decl_stmt|;
comment|/**    * The data block encoding which will be used.    * {@link NoOpDataBlockEncoder#INSTANCE} if there is no encoding.    */
specifier|protected
specifier|final
name|HFileDataBlockEncoder
name|blockEncoder
decl_stmt|;
comment|/** First key in a block. */
specifier|protected
name|byte
index|[]
name|firstKeyInBlock
init|=
literal|null
decl_stmt|;
comment|/** May be null if we were passed a stream. */
specifier|protected
specifier|final
name|Path
name|path
decl_stmt|;
comment|/** Cache configuration for caching data on write. */
specifier|protected
specifier|final
name|CacheConfig
name|cacheConf
decl_stmt|;
comment|/**    * Name for this object used when logging or in toString. Is either    * the result of a toString on stream or else name of passed file Path.    */
specifier|protected
specifier|final
name|String
name|name
decl_stmt|;
specifier|public
name|AbstractHFileWriter
parameter_list|(
name|CacheConfig
name|cacheConf
parameter_list|,
name|FSDataOutputStream
name|outputStream
parameter_list|,
name|Path
name|path
parameter_list|,
name|int
name|blockSize
parameter_list|,
name|Compression
operator|.
name|Algorithm
name|compressAlgo
parameter_list|,
name|HFileDataBlockEncoder
name|dataBlockEncoder
parameter_list|,
name|KeyComparator
name|comparator
parameter_list|)
block|{
name|this
operator|.
name|outputStream
operator|=
name|outputStream
expr_stmt|;
name|this
operator|.
name|path
operator|=
name|path
expr_stmt|;
name|this
operator|.
name|name
operator|=
name|path
operator|!=
literal|null
condition|?
name|path
operator|.
name|getName
argument_list|()
else|:
name|outputStream
operator|.
name|toString
argument_list|()
expr_stmt|;
name|this
operator|.
name|blockSize
operator|=
name|blockSize
expr_stmt|;
name|this
operator|.
name|compressAlgo
operator|=
name|compressAlgo
operator|==
literal|null
condition|?
name|HFile
operator|.
name|DEFAULT_COMPRESSION_ALGORITHM
else|:
name|compressAlgo
expr_stmt|;
name|this
operator|.
name|blockEncoder
operator|=
name|dataBlockEncoder
operator|!=
literal|null
condition|?
name|dataBlockEncoder
else|:
name|NoOpDataBlockEncoder
operator|.
name|INSTANCE
expr_stmt|;
name|this
operator|.
name|comparator
operator|=
name|comparator
operator|!=
literal|null
condition|?
name|comparator
else|:
name|Bytes
operator|.
name|BYTES_RAWCOMPARATOR
expr_stmt|;
name|closeOutputStream
operator|=
name|path
operator|!=
literal|null
expr_stmt|;
name|this
operator|.
name|cacheConf
operator|=
name|cacheConf
expr_stmt|;
block|}
comment|/**    * Add last bits of metadata to file info before it is written out.    */
specifier|protected
name|void
name|finishFileInfo
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|lastKeyBuffer
operator|!=
literal|null
condition|)
block|{
comment|// Make a copy. The copy is stuffed into our fileinfo map. Needs a clean
comment|// byte buffer. Won't take a tuple.
name|fileInfo
operator|.
name|append
argument_list|(
name|FileInfo
operator|.
name|LASTKEY
argument_list|,
name|Arrays
operator|.
name|copyOfRange
argument_list|(
name|lastKeyBuffer
argument_list|,
name|lastKeyOffset
argument_list|,
name|lastKeyOffset
operator|+
name|lastKeyLength
argument_list|)
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
comment|// Average key length.
name|int
name|avgKeyLen
init|=
name|entryCount
operator|==
literal|0
condition|?
literal|0
else|:
call|(
name|int
call|)
argument_list|(
name|totalKeyLength
operator|/
name|entryCount
argument_list|)
decl_stmt|;
name|fileInfo
operator|.
name|append
argument_list|(
name|FileInfo
operator|.
name|AVG_KEY_LEN
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|avgKeyLen
argument_list|)
argument_list|,
literal|false
argument_list|)
expr_stmt|;
comment|// Average value length.
name|int
name|avgValueLen
init|=
name|entryCount
operator|==
literal|0
condition|?
literal|0
else|:
call|(
name|int
call|)
argument_list|(
name|totalValueLength
operator|/
name|entryCount
argument_list|)
decl_stmt|;
name|fileInfo
operator|.
name|append
argument_list|(
name|FileInfo
operator|.
name|AVG_VALUE_LEN
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|avgValueLen
argument_list|)
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
comment|/**    * Add to the file info. All added key/value pairs can be obtained using    * {@link HFile.Reader#loadFileInfo()}.    *    * @param k Key    * @param v Value    * @throws IOException in case the key or the value are invalid    */
annotation|@
name|Override
specifier|public
name|void
name|appendFileInfo
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
parameter_list|)
throws|throws
name|IOException
block|{
name|fileInfo
operator|.
name|append
argument_list|(
name|k
argument_list|,
name|v
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
comment|/**    * Sets the file info offset in the trailer, finishes up populating fields in    * the file info, and writes the file info into the given data output. The    * reason the data output is not always {@link #outputStream} is that we store    * file info as a block in version 2.    *    * @param trailer fixed file trailer    * @param out the data output to write the file info to    * @throws IOException    */
specifier|protected
specifier|final
name|void
name|writeFileInfo
parameter_list|(
name|FixedFileTrailer
name|trailer
parameter_list|,
name|DataOutputStream
name|out
parameter_list|)
throws|throws
name|IOException
block|{
name|trailer
operator|.
name|setFileInfoOffset
argument_list|(
name|outputStream
operator|.
name|getPos
argument_list|()
argument_list|)
expr_stmt|;
name|finishFileInfo
argument_list|()
expr_stmt|;
name|fileInfo
operator|.
name|write
argument_list|(
name|out
argument_list|)
expr_stmt|;
block|}
comment|/**    * Checks that the given key does not violate the key order.    *    * @param key Key to check.    * @return true if the key is duplicate    * @throws IOException if the key or the key order is wrong    */
specifier|protected
name|boolean
name|checkKey
parameter_list|(
specifier|final
name|byte
index|[]
name|key
parameter_list|,
specifier|final
name|int
name|offset
parameter_list|,
specifier|final
name|int
name|length
parameter_list|)
throws|throws
name|IOException
block|{
name|boolean
name|isDuplicateKey
init|=
literal|false
decl_stmt|;
if|if
condition|(
name|key
operator|==
literal|null
operator|||
name|length
operator|<=
literal|0
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Key cannot be null or empty"
argument_list|)
throw|;
block|}
if|if
condition|(
name|lastKeyBuffer
operator|!=
literal|null
condition|)
block|{
name|int
name|keyComp
init|=
name|comparator
operator|.
name|compare
argument_list|(
name|lastKeyBuffer
argument_list|,
name|lastKeyOffset
argument_list|,
name|lastKeyLength
argument_list|,
name|key
argument_list|,
name|offset
argument_list|,
name|length
argument_list|)
decl_stmt|;
if|if
condition|(
name|keyComp
operator|>
literal|0
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Added a key not lexically larger than"
operator|+
literal|" previous key="
operator|+
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|key
argument_list|,
name|offset
argument_list|,
name|length
argument_list|)
operator|+
literal|", lastkey="
operator|+
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|lastKeyBuffer
argument_list|,
name|lastKeyOffset
argument_list|,
name|lastKeyLength
argument_list|)
argument_list|)
throw|;
block|}
elseif|else
if|if
condition|(
name|keyComp
operator|==
literal|0
condition|)
block|{
name|isDuplicateKey
operator|=
literal|true
expr_stmt|;
block|}
block|}
return|return
name|isDuplicateKey
return|;
block|}
comment|/** Checks the given value for validity. */
specifier|protected
name|void
name|checkValue
parameter_list|(
specifier|final
name|byte
index|[]
name|value
parameter_list|,
specifier|final
name|int
name|offset
parameter_list|,
specifier|final
name|int
name|length
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|value
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Value cannot be null"
argument_list|)
throw|;
block|}
block|}
comment|/**    * @return Path or null if we were passed a stream rather than a Path.    */
annotation|@
name|Override
specifier|public
name|Path
name|getPath
parameter_list|()
block|{
return|return
name|path
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
literal|"writer="
operator|+
operator|(
name|path
operator|!=
literal|null
condition|?
name|path
operator|.
name|toString
argument_list|()
else|:
literal|null
operator|)
operator|+
literal|", name="
operator|+
name|name
operator|+
literal|", compression="
operator|+
name|compressAlgo
operator|.
name|getName
argument_list|()
return|;
block|}
comment|/**    * Sets remaining trailer fields, writes the trailer to disk, and optionally    * closes the output stream.    */
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
name|trailer
operator|.
name|setMetaIndexCount
argument_list|(
name|metaNames
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|trailer
operator|.
name|setTotalUncompressedBytes
argument_list|(
name|totalUncompressedBytes
operator|+
name|trailer
operator|.
name|getTrailerSize
argument_list|()
argument_list|)
expr_stmt|;
name|trailer
operator|.
name|setEntryCount
argument_list|(
name|entryCount
argument_list|)
expr_stmt|;
name|trailer
operator|.
name|setCompressionCodec
argument_list|(
name|compressAlgo
argument_list|)
expr_stmt|;
name|trailer
operator|.
name|serialize
argument_list|(
name|outputStream
argument_list|)
expr_stmt|;
if|if
condition|(
name|closeOutputStream
condition|)
block|{
name|outputStream
operator|.
name|close
argument_list|()
expr_stmt|;
name|outputStream
operator|=
literal|null
expr_stmt|;
block|}
block|}
specifier|public
specifier|static
name|Compression
operator|.
name|Algorithm
name|compressionByName
parameter_list|(
name|String
name|algoName
parameter_list|)
block|{
if|if
condition|(
name|algoName
operator|==
literal|null
condition|)
return|return
name|HFile
operator|.
name|DEFAULT_COMPRESSION_ALGORITHM
return|;
return|return
name|Compression
operator|.
name|getCompressionAlgorithmByName
argument_list|(
name|algoName
argument_list|)
return|;
block|}
comment|/** A helper method to create HFile output streams in constructors */
specifier|protected
specifier|static
name|FSDataOutputStream
name|createOutputStream
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|FileSystem
name|fs
parameter_list|,
name|Path
name|path
parameter_list|)
throws|throws
name|IOException
block|{
name|FsPermission
name|perms
init|=
name|FSUtils
operator|.
name|getFilePermissions
argument_list|(
name|fs
argument_list|,
name|conf
argument_list|,
name|HConstants
operator|.
name|DATA_FILE_UMASK_KEY
argument_list|)
decl_stmt|;
return|return
name|FSUtils
operator|.
name|create
argument_list|(
name|fs
argument_list|,
name|path
argument_list|,
name|perms
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getCurrentSize
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|this
operator|.
name|outputStream
operator|==
literal|null
condition|)
return|return
operator|-
literal|1
return|;
return|return
name|this
operator|.
name|outputStream
operator|.
name|getPos
argument_list|()
return|;
block|}
block|}
end_class

end_unit

