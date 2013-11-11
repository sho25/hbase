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
name|ByteArrayOutputStream
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
name|java
operator|.
name|util
operator|.
name|zip
operator|.
name|Checksum
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

begin_comment
comment|/**  * Utility methods to compute and validate checksums.  */
end_comment

begin_class
specifier|public
class|class
name|ChecksumUtil
block|{
comment|/** This is used to reserve space in a byte buffer */
specifier|private
specifier|static
name|byte
index|[]
name|DUMMY_VALUE
init|=
operator|new
name|byte
index|[
literal|128
operator|*
name|HFileBlock
operator|.
name|CHECKSUM_SIZE
index|]
decl_stmt|;
comment|/**     * This is used by unit tests to make checksum failures throw an     * exception instead of returning null. Returning a null value from     * checksum validation will cause the higher layer to retry that     * read with hdfs-level checksums. Instead, we would like checksum     * failures to cause the entire unit test to fail.    */
specifier|private
specifier|static
name|boolean
name|generateExceptions
init|=
literal|false
decl_stmt|;
comment|/**    * Generates a checksum for all the data in indata. The checksum is    * written to outdata.    * @param indata input data stream    * @param startOffset starting offset in the indata stream from where to    *                    compute checkums from    * @param endOffset ending offset in the indata stream upto    *                   which checksums needs to be computed    * @param outdata the output buffer where checksum values are written    * @param outOffset the starting offset in the outdata where the    *                  checksum values are written    * @param checksumType type of checksum    * @param bytesPerChecksum number of bytes per checksum value    */
specifier|static
name|void
name|generateChecksums
parameter_list|(
name|byte
index|[]
name|indata
parameter_list|,
name|int
name|startOffset
parameter_list|,
name|int
name|endOffset
parameter_list|,
name|byte
index|[]
name|outdata
parameter_list|,
name|int
name|outOffset
parameter_list|,
name|ChecksumType
name|checksumType
parameter_list|,
name|int
name|bytesPerChecksum
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|checksumType
operator|==
name|ChecksumType
operator|.
name|NULL
condition|)
block|{
return|return;
comment|// No checkums for this block.
block|}
name|Checksum
name|checksum
init|=
name|checksumType
operator|.
name|getChecksumObject
argument_list|()
decl_stmt|;
name|int
name|bytesLeft
init|=
name|endOffset
operator|-
name|startOffset
decl_stmt|;
name|int
name|chunkNum
init|=
literal|0
decl_stmt|;
while|while
condition|(
name|bytesLeft
operator|>
literal|0
condition|)
block|{
comment|// generate the checksum for one chunk
name|checksum
operator|.
name|reset
argument_list|()
expr_stmt|;
name|int
name|count
init|=
name|Math
operator|.
name|min
argument_list|(
name|bytesLeft
argument_list|,
name|bytesPerChecksum
argument_list|)
decl_stmt|;
name|checksum
operator|.
name|update
argument_list|(
name|indata
argument_list|,
name|startOffset
argument_list|,
name|count
argument_list|)
expr_stmt|;
comment|// write the checksum value to the output buffer.
name|int
name|cksumValue
init|=
operator|(
name|int
operator|)
name|checksum
operator|.
name|getValue
argument_list|()
decl_stmt|;
name|outOffset
operator|=
name|Bytes
operator|.
name|putInt
argument_list|(
name|outdata
argument_list|,
name|outOffset
argument_list|,
name|cksumValue
argument_list|)
expr_stmt|;
name|chunkNum
operator|++
expr_stmt|;
name|startOffset
operator|+=
name|count
expr_stmt|;
name|bytesLeft
operator|-=
name|count
expr_stmt|;
block|}
block|}
comment|/**    * Validates that the data in the specified HFileBlock matches the    * checksum.  Generates the checksum for the data and    * then validate that it matches the value stored in the header.    * If there is a checksum mismatch, then return false. Otherwise    * return true.    * The header is extracted from the specified HFileBlock while the    * data-to-be-verified is extracted from 'data'.    */
specifier|static
name|boolean
name|validateBlockChecksum
parameter_list|(
name|Path
name|path
parameter_list|,
name|HFileBlock
name|block
parameter_list|,
name|byte
index|[]
name|data
parameter_list|,
name|int
name|hdrSize
parameter_list|)
throws|throws
name|IOException
block|{
comment|// If this is an older version of the block that does not have
comment|// checksums, then return false indicating that checksum verification
comment|// did not succeed. Actually, this methiod should never be called
comment|// when the minorVersion is 0, thus this is a defensive check for a
comment|// cannot-happen case. Since this is a cannot-happen case, it is
comment|// better to return false to indicate a checksum validation failure.
if|if
condition|(
operator|!
name|block
operator|.
name|getHFileContext
argument_list|()
operator|.
name|isUseHBaseChecksum
argument_list|()
condition|)
block|{
return|return
literal|false
return|;
block|}
comment|// Get a checksum object based on the type of checksum that is
comment|// set in the HFileBlock header. A ChecksumType.NULL indicates that
comment|// the caller is not interested in validating checksums, so we
comment|// always return true.
name|ChecksumType
name|cktype
init|=
name|ChecksumType
operator|.
name|codeToType
argument_list|(
name|block
operator|.
name|getChecksumType
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|cktype
operator|==
name|ChecksumType
operator|.
name|NULL
condition|)
block|{
return|return
literal|true
return|;
comment|// No checkums validations needed for this block.
block|}
name|Checksum
name|checksumObject
init|=
name|cktype
operator|.
name|getChecksumObject
argument_list|()
decl_stmt|;
name|checksumObject
operator|.
name|reset
argument_list|()
expr_stmt|;
comment|// read in the stored value of the checksum size from the header.
name|int
name|bytesPerChecksum
init|=
name|block
operator|.
name|getBytesPerChecksum
argument_list|()
decl_stmt|;
comment|// bytesPerChecksum is always larger than the size of the header
if|if
condition|(
name|bytesPerChecksum
operator|<
name|hdrSize
condition|)
block|{
name|String
name|msg
init|=
literal|"Unsupported value of bytesPerChecksum. "
operator|+
literal|" Minimum is "
operator|+
name|hdrSize
operator|+
literal|" but the configured value is "
operator|+
name|bytesPerChecksum
decl_stmt|;
name|HFile
operator|.
name|LOG
operator|.
name|warn
argument_list|(
name|msg
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
comment|// cannot happen case, unable to verify checksum
block|}
comment|// Extract the header and compute checksum for the header.
name|ByteBuffer
name|hdr
init|=
name|block
operator|.
name|getBufferWithHeader
argument_list|()
decl_stmt|;
name|checksumObject
operator|.
name|update
argument_list|(
name|hdr
operator|.
name|array
argument_list|()
argument_list|,
name|hdr
operator|.
name|arrayOffset
argument_list|()
argument_list|,
name|hdrSize
argument_list|)
expr_stmt|;
name|int
name|off
init|=
name|hdrSize
decl_stmt|;
name|int
name|consumed
init|=
name|hdrSize
decl_stmt|;
name|int
name|bytesLeft
init|=
name|block
operator|.
name|getOnDiskDataSizeWithHeader
argument_list|()
operator|-
name|off
decl_stmt|;
name|int
name|cksumOffset
init|=
name|block
operator|.
name|getOnDiskDataSizeWithHeader
argument_list|()
decl_stmt|;
comment|// validate each chunk
while|while
condition|(
name|bytesLeft
operator|>
literal|0
condition|)
block|{
name|int
name|thisChunkSize
init|=
name|bytesPerChecksum
operator|-
name|consumed
decl_stmt|;
name|int
name|count
init|=
name|Math
operator|.
name|min
argument_list|(
name|bytesLeft
argument_list|,
name|thisChunkSize
argument_list|)
decl_stmt|;
name|checksumObject
operator|.
name|update
argument_list|(
name|data
argument_list|,
name|off
argument_list|,
name|count
argument_list|)
expr_stmt|;
name|int
name|storedChecksum
init|=
name|Bytes
operator|.
name|toInt
argument_list|(
name|data
argument_list|,
name|cksumOffset
argument_list|)
decl_stmt|;
if|if
condition|(
name|storedChecksum
operator|!=
operator|(
name|int
operator|)
name|checksumObject
operator|.
name|getValue
argument_list|()
condition|)
block|{
name|String
name|msg
init|=
literal|"File "
operator|+
name|path
operator|+
literal|" Stored checksum value of "
operator|+
name|storedChecksum
operator|+
literal|" at offset "
operator|+
name|cksumOffset
operator|+
literal|" does not match computed checksum "
operator|+
name|checksumObject
operator|.
name|getValue
argument_list|()
operator|+
literal|", total data size "
operator|+
name|data
operator|.
name|length
operator|+
literal|" Checksum data range offset "
operator|+
name|off
operator|+
literal|" len "
operator|+
name|count
operator|+
name|HFileBlock
operator|.
name|toStringHeader
argument_list|(
name|block
operator|.
name|getBufferReadOnly
argument_list|()
argument_list|)
decl_stmt|;
name|HFile
operator|.
name|LOG
operator|.
name|warn
argument_list|(
name|msg
argument_list|)
expr_stmt|;
if|if
condition|(
name|generateExceptions
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
name|msg
argument_list|)
throw|;
comment|// this is only for unit tests
block|}
else|else
block|{
return|return
literal|false
return|;
comment|// checksum validation failure
block|}
block|}
name|cksumOffset
operator|+=
name|HFileBlock
operator|.
name|CHECKSUM_SIZE
expr_stmt|;
name|bytesLeft
operator|-=
name|count
expr_stmt|;
name|off
operator|+=
name|count
expr_stmt|;
name|consumed
operator|=
literal|0
expr_stmt|;
name|checksumObject
operator|.
name|reset
argument_list|()
expr_stmt|;
block|}
return|return
literal|true
return|;
comment|// checksum is valid
block|}
comment|/**    * Returns the number of bytes needed to store the checksums for    * a specified data size    * @param datasize number of bytes of data    * @param bytesPerChecksum number of bytes in a checksum chunk    * @return The number of bytes needed to store the checksum values    */
specifier|static
name|long
name|numBytes
parameter_list|(
name|long
name|datasize
parameter_list|,
name|int
name|bytesPerChecksum
parameter_list|)
block|{
return|return
name|numChunks
argument_list|(
name|datasize
argument_list|,
name|bytesPerChecksum
argument_list|)
operator|*
name|HFileBlock
operator|.
name|CHECKSUM_SIZE
return|;
block|}
comment|/**    * Returns the number of checksum chunks needed to store the checksums for    * a specified data size    * @param datasize number of bytes of data    * @param bytesPerChecksum number of bytes in a checksum chunk    * @return The number of checksum chunks    */
specifier|static
name|long
name|numChunks
parameter_list|(
name|long
name|datasize
parameter_list|,
name|int
name|bytesPerChecksum
parameter_list|)
block|{
name|long
name|numChunks
init|=
name|datasize
operator|/
name|bytesPerChecksum
decl_stmt|;
if|if
condition|(
name|datasize
operator|%
name|bytesPerChecksum
operator|!=
literal|0
condition|)
block|{
name|numChunks
operator|++
expr_stmt|;
block|}
return|return
name|numChunks
return|;
block|}
comment|/**    * Write dummy checksums to the end of the specified bytes array    * to reserve space for writing checksums later    * @param baos OutputStream to write dummy checkum values    * @param numBytes Number of bytes of data for which dummy checksums    *                 need to be generated    * @param bytesPerChecksum Number of bytes per checksum value    */
specifier|static
name|void
name|reserveSpaceForChecksums
parameter_list|(
name|ByteArrayOutputStream
name|baos
parameter_list|,
name|int
name|numBytes
parameter_list|,
name|int
name|bytesPerChecksum
parameter_list|)
throws|throws
name|IOException
block|{
name|long
name|numChunks
init|=
name|numChunks
argument_list|(
name|numBytes
argument_list|,
name|bytesPerChecksum
argument_list|)
decl_stmt|;
name|long
name|bytesLeft
init|=
name|numChunks
operator|*
name|HFileBlock
operator|.
name|CHECKSUM_SIZE
decl_stmt|;
while|while
condition|(
name|bytesLeft
operator|>
literal|0
condition|)
block|{
name|long
name|count
init|=
name|Math
operator|.
name|min
argument_list|(
name|bytesLeft
argument_list|,
name|DUMMY_VALUE
operator|.
name|length
argument_list|)
decl_stmt|;
name|baos
operator|.
name|write
argument_list|(
name|DUMMY_VALUE
argument_list|,
literal|0
argument_list|,
operator|(
name|int
operator|)
name|count
argument_list|)
expr_stmt|;
name|bytesLeft
operator|-=
name|count
expr_stmt|;
block|}
block|}
comment|/**    * Mechanism to throw an exception in case of hbase checksum    * failure. This is used by unit tests only.    * @param value Setting this to true will cause hbase checksum    *              verification failures to generate exceptions.    */
specifier|public
specifier|static
name|void
name|generateExceptionForChecksumFailureForTest
parameter_list|(
name|boolean
name|value
parameter_list|)
block|{
name|generateExceptions
operator|=
name|value
expr_stmt|;
block|}
block|}
end_class

end_unit

