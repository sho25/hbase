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
name|EOFException
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
name|UUID
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
name|TableName
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
name|wal
operator|.
name|WALKey
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

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|annotations
operator|.
name|VisibleForTesting
import|;
end_import

begin_comment
comment|/**  * A Key for an entry in the change log.  *  * The log intermingles edits to many tables and rows, so each log entry  * identifies the appropriate table and row.  Within a table and row, they're  * also sorted.  *  *<p>Some Transactional edits (START, COMMIT, ABORT) will not have an  * associated row.  * @deprecated use WALKey  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|LimitedPrivate
argument_list|(
name|HBaseInterfaceAudience
operator|.
name|REPLICATION
argument_list|)
annotation|@
name|Deprecated
specifier|public
class|class
name|HLogKey
extends|extends
name|WALKey
implements|implements
name|Writable
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
name|HLogKey
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|public
name|HLogKey
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
block|}
annotation|@
name|VisibleForTesting
specifier|public
name|HLogKey
parameter_list|(
specifier|final
name|byte
index|[]
name|encodedRegionName
parameter_list|,
specifier|final
name|TableName
name|tablename
parameter_list|,
name|long
name|logSeqNum
parameter_list|,
specifier|final
name|long
name|now
parameter_list|,
name|UUID
name|clusterId
parameter_list|)
block|{
name|super
argument_list|(
name|encodedRegionName
argument_list|,
name|tablename
argument_list|,
name|logSeqNum
argument_list|,
name|now
argument_list|,
name|clusterId
argument_list|)
expr_stmt|;
block|}
specifier|public
name|HLogKey
parameter_list|(
specifier|final
name|byte
index|[]
name|encodedRegionName
parameter_list|,
specifier|final
name|TableName
name|tablename
parameter_list|)
block|{
name|super
argument_list|(
name|encodedRegionName
argument_list|,
name|tablename
argument_list|)
expr_stmt|;
block|}
specifier|public
name|HLogKey
parameter_list|(
specifier|final
name|byte
index|[]
name|encodedRegionName
parameter_list|,
specifier|final
name|TableName
name|tablename
parameter_list|,
specifier|final
name|long
name|now
parameter_list|)
block|{
name|super
argument_list|(
name|encodedRegionName
argument_list|,
name|tablename
argument_list|,
name|now
argument_list|)
expr_stmt|;
block|}
comment|/**    * Create the log key for writing to somewhere.    * We maintain the tablename mainly for debugging purposes.    * A regionName is always a sub-table object.    *<p>Used by log splitting and snapshots.    *    * @param encodedRegionName Encoded name of the region as returned by    *<code>HRegionInfo#getEncodedNameAsBytes()</code>.    * @param tablename   - name of table    * @param logSeqNum   - log sequence number    * @param now Time at which this edit was written.    * @param clusterIds the clusters that have consumed the change(used in Replication)    */
specifier|public
name|HLogKey
parameter_list|(
specifier|final
name|byte
index|[]
name|encodedRegionName
parameter_list|,
specifier|final
name|TableName
name|tablename
parameter_list|,
name|long
name|logSeqNum
parameter_list|,
specifier|final
name|long
name|now
parameter_list|,
name|List
argument_list|<
name|UUID
argument_list|>
name|clusterIds
parameter_list|,
name|long
name|nonceGroup
parameter_list|,
name|long
name|nonce
parameter_list|)
block|{
name|super
argument_list|(
name|encodedRegionName
argument_list|,
name|tablename
argument_list|,
name|logSeqNum
argument_list|,
name|now
argument_list|,
name|clusterIds
argument_list|,
name|nonceGroup
argument_list|,
name|nonce
argument_list|)
expr_stmt|;
block|}
comment|/**    * Create the log key for writing to somewhere.    * We maintain the tablename mainly for debugging purposes.    * A regionName is always a sub-table object.    *    * @param encodedRegionName Encoded name of the region as returned by    *<code>HRegionInfo#getEncodedNameAsBytes()</code>.    * @param tablename    * @param now Time at which this edit was written.    * @param clusterIds the clusters that have consumed the change(used in Replication)    * @param nonceGroup    * @param nonce    */
specifier|public
name|HLogKey
parameter_list|(
specifier|final
name|byte
index|[]
name|encodedRegionName
parameter_list|,
specifier|final
name|TableName
name|tablename
parameter_list|,
specifier|final
name|long
name|now
parameter_list|,
name|List
argument_list|<
name|UUID
argument_list|>
name|clusterIds
parameter_list|,
name|long
name|nonceGroup
parameter_list|,
name|long
name|nonce
parameter_list|)
block|{
name|super
argument_list|(
name|encodedRegionName
argument_list|,
name|tablename
argument_list|,
name|now
argument_list|,
name|clusterIds
argument_list|,
name|nonceGroup
argument_list|,
name|nonce
argument_list|)
expr_stmt|;
block|}
comment|/**    * Create the log key for writing to somewhere.    * We maintain the tablename mainly for debugging purposes.    * A regionName is always a sub-table object.    *    * @param encodedRegionName Encoded name of the region as returned by    *<code>HRegionInfo#getEncodedNameAsBytes()</code>.    * @param tablename    * @param logSeqNum    * @param nonceGroup    * @param nonce    */
specifier|public
name|HLogKey
parameter_list|(
specifier|final
name|byte
index|[]
name|encodedRegionName
parameter_list|,
specifier|final
name|TableName
name|tablename
parameter_list|,
name|long
name|logSeqNum
parameter_list|,
name|long
name|nonceGroup
parameter_list|,
name|long
name|nonce
parameter_list|)
block|{
name|super
argument_list|(
name|encodedRegionName
argument_list|,
name|tablename
argument_list|,
name|logSeqNum
argument_list|,
name|nonceGroup
argument_list|,
name|nonce
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
annotation|@
name|Deprecated
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
literal|"HLogKey is being serialized to writable - only expected in test code"
argument_list|)
expr_stmt|;
name|WritableUtils
operator|.
name|writeVInt
argument_list|(
name|out
argument_list|,
name|VERSION
operator|.
name|code
argument_list|)
expr_stmt|;
if|if
condition|(
name|compressionContext
operator|==
literal|null
condition|)
block|{
name|Bytes
operator|.
name|writeByteArray
argument_list|(
name|out
argument_list|,
name|this
operator|.
name|encodedRegionName
argument_list|)
expr_stmt|;
name|Bytes
operator|.
name|writeByteArray
argument_list|(
name|out
argument_list|,
name|this
operator|.
name|tablename
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|Compressor
operator|.
name|writeCompressed
argument_list|(
name|this
operator|.
name|encodedRegionName
argument_list|,
literal|0
argument_list|,
name|this
operator|.
name|encodedRegionName
operator|.
name|length
argument_list|,
name|out
argument_list|,
name|compressionContext
operator|.
name|regionDict
argument_list|)
expr_stmt|;
name|Compressor
operator|.
name|writeCompressed
argument_list|(
name|this
operator|.
name|tablename
operator|.
name|getName
argument_list|()
argument_list|,
literal|0
argument_list|,
name|this
operator|.
name|tablename
operator|.
name|getName
argument_list|()
operator|.
name|length
argument_list|,
name|out
argument_list|,
name|compressionContext
operator|.
name|tableDict
argument_list|)
expr_stmt|;
block|}
name|out
operator|.
name|writeLong
argument_list|(
name|this
operator|.
name|logSeqNum
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeLong
argument_list|(
name|this
operator|.
name|writeTime
argument_list|)
expr_stmt|;
comment|// Don't need to write the clusters information as we are using protobufs from 0.95
comment|// Writing only the first clusterId for testing the legacy read
name|Iterator
argument_list|<
name|UUID
argument_list|>
name|iterator
init|=
name|clusterIds
operator|.
name|iterator
argument_list|()
decl_stmt|;
if|if
condition|(
name|iterator
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|out
operator|.
name|writeBoolean
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|UUID
name|clusterId
init|=
name|iterator
operator|.
name|next
argument_list|()
decl_stmt|;
name|out
operator|.
name|writeLong
argument_list|(
name|clusterId
operator|.
name|getMostSignificantBits
argument_list|()
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeLong
argument_list|(
name|clusterId
operator|.
name|getLeastSignificantBits
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|out
operator|.
name|writeBoolean
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
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
name|Version
name|version
init|=
name|Version
operator|.
name|UNVERSIONED
decl_stmt|;
comment|// HLogKey was not versioned in the beginning.
comment|// In order to introduce it now, we make use of the fact
comment|// that encodedRegionName was written with Bytes.writeByteArray,
comment|// which encodes the array length as a vint which is>= 0.
comment|// Hence if the vint is>= 0 we have an old version and the vint
comment|// encodes the length of encodedRegionName.
comment|// If< 0 we just read the version and the next vint is the length.
comment|// @see Bytes#readByteArray(DataInput)
name|setScopes
argument_list|(
literal|null
argument_list|)
expr_stmt|;
comment|// writable HLogKey does not contain scopes
name|int
name|len
init|=
name|WritableUtils
operator|.
name|readVInt
argument_list|(
name|in
argument_list|)
decl_stmt|;
name|byte
index|[]
name|tablenameBytes
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|len
operator|<
literal|0
condition|)
block|{
comment|// what we just read was the version
name|version
operator|=
name|Version
operator|.
name|fromCode
argument_list|(
name|len
argument_list|)
expr_stmt|;
comment|// We only compress V2 of WALkey.
comment|// If compression is on, the length is handled by the dictionary
if|if
condition|(
name|compressionContext
operator|==
literal|null
operator|||
operator|!
name|version
operator|.
name|atLeast
argument_list|(
name|Version
operator|.
name|COMPRESSED
argument_list|)
condition|)
block|{
name|len
operator|=
name|WritableUtils
operator|.
name|readVInt
argument_list|(
name|in
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|compressionContext
operator|==
literal|null
operator|||
operator|!
name|version
operator|.
name|atLeast
argument_list|(
name|Version
operator|.
name|COMPRESSED
argument_list|)
condition|)
block|{
name|this
operator|.
name|encodedRegionName
operator|=
operator|new
name|byte
index|[
name|len
index|]
expr_stmt|;
name|in
operator|.
name|readFully
argument_list|(
name|this
operator|.
name|encodedRegionName
argument_list|)
expr_stmt|;
name|tablenameBytes
operator|=
name|Bytes
operator|.
name|readByteArray
argument_list|(
name|in
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|this
operator|.
name|encodedRegionName
operator|=
name|Compressor
operator|.
name|readCompressed
argument_list|(
name|in
argument_list|,
name|compressionContext
operator|.
name|regionDict
argument_list|)
expr_stmt|;
name|tablenameBytes
operator|=
name|Compressor
operator|.
name|readCompressed
argument_list|(
name|in
argument_list|,
name|compressionContext
operator|.
name|tableDict
argument_list|)
expr_stmt|;
block|}
name|this
operator|.
name|logSeqNum
operator|=
name|in
operator|.
name|readLong
argument_list|()
expr_stmt|;
name|this
operator|.
name|writeTime
operator|=
name|in
operator|.
name|readLong
argument_list|()
expr_stmt|;
name|this
operator|.
name|clusterIds
operator|.
name|clear
argument_list|()
expr_stmt|;
if|if
condition|(
name|version
operator|.
name|atLeast
argument_list|(
name|Version
operator|.
name|INITIAL
argument_list|)
condition|)
block|{
if|if
condition|(
name|in
operator|.
name|readBoolean
argument_list|()
condition|)
block|{
comment|// read the older log
comment|// Definitely is the originating cluster
name|clusterIds
operator|.
name|add
argument_list|(
operator|new
name|UUID
argument_list|(
name|in
operator|.
name|readLong
argument_list|()
argument_list|,
name|in
operator|.
name|readLong
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
try|try
block|{
comment|// dummy read (former byte cluster id)
name|in
operator|.
name|readByte
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|EOFException
name|e
parameter_list|)
block|{
comment|// Means it's a very old key, just continue
block|}
block|}
try|try
block|{
name|this
operator|.
name|tablename
operator|=
name|TableName
operator|.
name|valueOf
argument_list|(
name|tablenameBytes
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|iae
parameter_list|)
block|{
if|if
condition|(
name|Bytes
operator|.
name|toString
argument_list|(
name|tablenameBytes
argument_list|)
operator|.
name|equals
argument_list|(
name|TableName
operator|.
name|OLD_META_STR
argument_list|)
condition|)
block|{
comment|// It is a pre-namespace meta table edit, continue with new format.
name|LOG
operator|.
name|info
argument_list|(
literal|"Got an old .META. edit, continuing with new format "
argument_list|)
expr_stmt|;
name|this
operator|.
name|tablename
operator|=
name|TableName
operator|.
name|META_TABLE_NAME
expr_stmt|;
name|this
operator|.
name|encodedRegionName
operator|=
name|HRegionInfo
operator|.
name|FIRST_META_REGIONINFO
operator|.
name|getEncodedNameAsBytes
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|Bytes
operator|.
name|toString
argument_list|(
name|tablenameBytes
argument_list|)
operator|.
name|equals
argument_list|(
name|TableName
operator|.
name|OLD_ROOT_STR
argument_list|)
condition|)
block|{
name|this
operator|.
name|tablename
operator|=
name|TableName
operator|.
name|OLD_ROOT_TABLE_NAME
expr_stmt|;
throw|throw
name|iae
throw|;
block|}
else|else
throw|throw
name|iae
throw|;
block|}
comment|// Do not need to read the clusters information as we are using protobufs from 0.95
block|}
block|}
end_class

end_unit

