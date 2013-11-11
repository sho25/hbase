begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|io
operator|.
name|compress
operator|.
name|Compression
operator|.
name|Algorithm
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
name|ChecksumType
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

begin_comment
comment|/**  * This carries the information on some of the meta data about the HFile. This  * meta data is used across the HFileWriter/Readers and the HFileBlocks.  * This helps to add new information to the HFile.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|HFileContext
implements|implements
name|HeapSize
implements|,
name|Cloneable
block|{
specifier|public
specifier|static
specifier|final
name|int
name|DEFAULT_BYTES_PER_CHECKSUM
init|=
literal|16
operator|*
literal|1024
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|ChecksumType
name|DEFAULT_CHECKSUM_TYPE
init|=
name|ChecksumType
operator|.
name|CRC32
decl_stmt|;
comment|/** Whether checksum is enabled or not**/
specifier|private
name|boolean
name|usesHBaseChecksum
init|=
literal|true
decl_stmt|;
comment|/** Whether mvcc is to be included in the Read/Write**/
specifier|private
name|boolean
name|includesMvcc
init|=
literal|true
decl_stmt|;
comment|/**Whether tags are to be included in the Read/Write**/
specifier|private
name|boolean
name|includesTags
decl_stmt|;
comment|/**Compression algorithm used**/
specifier|private
name|Algorithm
name|compressAlgo
init|=
name|Algorithm
operator|.
name|NONE
decl_stmt|;
comment|/** Whether tags to be compressed or not**/
specifier|private
name|boolean
name|compressTags
decl_stmt|;
comment|/** the checksum type **/
specifier|private
name|ChecksumType
name|checksumType
init|=
name|DEFAULT_CHECKSUM_TYPE
decl_stmt|;
comment|/** the number of bytes per checksum value **/
specifier|private
name|int
name|bytesPerChecksum
init|=
name|DEFAULT_BYTES_PER_CHECKSUM
decl_stmt|;
comment|/** Number of uncompressed bytes we allow per block. */
specifier|private
name|int
name|blocksize
init|=
name|HConstants
operator|.
name|DEFAULT_BLOCKSIZE
decl_stmt|;
specifier|private
name|DataBlockEncoding
name|encodingOnDisk
init|=
name|DataBlockEncoding
operator|.
name|NONE
decl_stmt|;
specifier|private
name|DataBlockEncoding
name|encodingInCache
init|=
name|DataBlockEncoding
operator|.
name|NONE
decl_stmt|;
comment|//Empty constructor.  Go with setters
specifier|public
name|HFileContext
parameter_list|()
block|{   }
comment|/**    * Copy constructor    * @param context    */
specifier|public
name|HFileContext
parameter_list|(
name|HFileContext
name|context
parameter_list|)
block|{
name|this
operator|.
name|usesHBaseChecksum
operator|=
name|context
operator|.
name|usesHBaseChecksum
expr_stmt|;
name|this
operator|.
name|includesMvcc
operator|=
name|context
operator|.
name|includesMvcc
expr_stmt|;
name|this
operator|.
name|includesTags
operator|=
name|context
operator|.
name|includesTags
expr_stmt|;
name|this
operator|.
name|compressAlgo
operator|=
name|context
operator|.
name|compressAlgo
expr_stmt|;
name|this
operator|.
name|compressTags
operator|=
name|context
operator|.
name|compressTags
expr_stmt|;
name|this
operator|.
name|checksumType
operator|=
name|context
operator|.
name|checksumType
expr_stmt|;
name|this
operator|.
name|bytesPerChecksum
operator|=
name|context
operator|.
name|bytesPerChecksum
expr_stmt|;
name|this
operator|.
name|blocksize
operator|=
name|context
operator|.
name|blocksize
expr_stmt|;
name|this
operator|.
name|encodingOnDisk
operator|=
name|context
operator|.
name|encodingOnDisk
expr_stmt|;
name|this
operator|.
name|encodingInCache
operator|=
name|context
operator|.
name|encodingInCache
expr_stmt|;
block|}
specifier|public
name|HFileContext
parameter_list|(
name|boolean
name|useHBaseChecksum
parameter_list|,
name|boolean
name|includesMvcc
parameter_list|,
name|boolean
name|includesTags
parameter_list|,
name|Algorithm
name|compressAlgo
parameter_list|,
name|boolean
name|compressTags
parameter_list|,
name|ChecksumType
name|checksumType
parameter_list|,
name|int
name|bytesPerChecksum
parameter_list|,
name|int
name|blockSize
parameter_list|,
name|DataBlockEncoding
name|encodingOnDisk
parameter_list|,
name|DataBlockEncoding
name|encodingInCache
parameter_list|)
block|{
name|this
operator|.
name|usesHBaseChecksum
operator|=
name|useHBaseChecksum
expr_stmt|;
name|this
operator|.
name|includesMvcc
operator|=
name|includesMvcc
expr_stmt|;
name|this
operator|.
name|includesTags
operator|=
name|includesTags
expr_stmt|;
name|this
operator|.
name|compressAlgo
operator|=
name|compressAlgo
expr_stmt|;
name|this
operator|.
name|compressTags
operator|=
name|compressTags
expr_stmt|;
name|this
operator|.
name|checksumType
operator|=
name|checksumType
expr_stmt|;
name|this
operator|.
name|bytesPerChecksum
operator|=
name|bytesPerChecksum
expr_stmt|;
name|this
operator|.
name|blocksize
operator|=
name|blockSize
expr_stmt|;
name|this
operator|.
name|encodingOnDisk
operator|=
name|encodingOnDisk
expr_stmt|;
name|this
operator|.
name|encodingInCache
operator|=
name|encodingInCache
expr_stmt|;
block|}
specifier|public
name|Algorithm
name|getCompression
parameter_list|()
block|{
return|return
name|compressAlgo
return|;
block|}
specifier|public
name|boolean
name|isUseHBaseChecksum
parameter_list|()
block|{
return|return
name|usesHBaseChecksum
return|;
block|}
specifier|public
name|boolean
name|isIncludesMvcc
parameter_list|()
block|{
return|return
name|includesMvcc
return|;
block|}
specifier|public
name|void
name|setIncludesMvcc
parameter_list|(
name|boolean
name|includesMvcc
parameter_list|)
block|{
name|this
operator|.
name|includesMvcc
operator|=
name|includesMvcc
expr_stmt|;
block|}
specifier|public
name|boolean
name|isIncludesTags
parameter_list|()
block|{
return|return
name|includesTags
return|;
block|}
specifier|public
name|void
name|setIncludesTags
parameter_list|(
name|boolean
name|includesTags
parameter_list|)
block|{
name|this
operator|.
name|includesTags
operator|=
name|includesTags
expr_stmt|;
block|}
specifier|public
name|boolean
name|isCompressTags
parameter_list|()
block|{
return|return
name|compressTags
return|;
block|}
specifier|public
name|void
name|setCompressTags
parameter_list|(
name|boolean
name|compressTags
parameter_list|)
block|{
name|this
operator|.
name|compressTags
operator|=
name|compressTags
expr_stmt|;
block|}
specifier|public
name|ChecksumType
name|getChecksumType
parameter_list|()
block|{
return|return
name|checksumType
return|;
block|}
specifier|public
name|int
name|getBytesPerChecksum
parameter_list|()
block|{
return|return
name|bytesPerChecksum
return|;
block|}
specifier|public
name|int
name|getBlocksize
parameter_list|()
block|{
return|return
name|blocksize
return|;
block|}
specifier|public
name|DataBlockEncoding
name|getEncodingOnDisk
parameter_list|()
block|{
return|return
name|encodingOnDisk
return|;
block|}
specifier|public
name|DataBlockEncoding
name|getEncodingInCache
parameter_list|()
block|{
return|return
name|encodingInCache
return|;
block|}
comment|/**    * HeapSize implementation    * NOTE : The heapsize should be altered as and when new state variable are added    * @return heap size of the HFileContext    */
annotation|@
name|Override
specifier|public
name|long
name|heapSize
parameter_list|()
block|{
name|long
name|size
init|=
name|ClassSize
operator|.
name|align
argument_list|(
name|ClassSize
operator|.
name|OBJECT
operator|+
comment|// Algorithm reference, encodingondisk, encodingincache, checksumtype
literal|4
operator|*
name|ClassSize
operator|.
name|REFERENCE
operator|+
literal|2
operator|*
name|Bytes
operator|.
name|SIZEOF_INT
operator|+
comment|// usesHBaseChecksum, includesMvcc, includesTags and compressTags
literal|4
operator|*
name|Bytes
operator|.
name|SIZEOF_BOOLEAN
argument_list|)
decl_stmt|;
return|return
name|size
return|;
block|}
annotation|@
name|Override
specifier|public
name|HFileContext
name|clone
parameter_list|()
block|{
name|HFileContext
name|clonnedCtx
init|=
operator|new
name|HFileContext
argument_list|()
decl_stmt|;
name|clonnedCtx
operator|.
name|usesHBaseChecksum
operator|=
name|this
operator|.
name|usesHBaseChecksum
expr_stmt|;
name|clonnedCtx
operator|.
name|includesMvcc
operator|=
name|this
operator|.
name|includesMvcc
expr_stmt|;
name|clonnedCtx
operator|.
name|includesTags
operator|=
name|this
operator|.
name|includesTags
expr_stmt|;
name|clonnedCtx
operator|.
name|compressAlgo
operator|=
name|this
operator|.
name|compressAlgo
expr_stmt|;
name|clonnedCtx
operator|.
name|compressTags
operator|=
name|this
operator|.
name|compressTags
expr_stmt|;
name|clonnedCtx
operator|.
name|checksumType
operator|=
name|this
operator|.
name|checksumType
expr_stmt|;
name|clonnedCtx
operator|.
name|bytesPerChecksum
operator|=
name|this
operator|.
name|bytesPerChecksum
expr_stmt|;
name|clonnedCtx
operator|.
name|blocksize
operator|=
name|this
operator|.
name|blocksize
expr_stmt|;
name|clonnedCtx
operator|.
name|encodingOnDisk
operator|=
name|this
operator|.
name|encodingOnDisk
expr_stmt|;
name|clonnedCtx
operator|.
name|encodingInCache
operator|=
name|this
operator|.
name|encodingInCache
expr_stmt|;
return|return
name|clonnedCtx
return|;
block|}
block|}
end_class

end_unit

