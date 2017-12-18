begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
package|;
end_package

begin_import
import|import static
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
name|HStoreFile
operator|.
name|BLOOM_FILTER_TYPE_KEY
import|;
end_import

begin_import
import|import static
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
name|HStoreFile
operator|.
name|DELETE_FAMILY_COUNT
import|;
end_import

begin_import
import|import static
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
name|HStoreFile
operator|.
name|EARLIEST_PUT_TS
import|;
end_import

begin_import
import|import static
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
name|HStoreFile
operator|.
name|MAJOR_COMPACTION_KEY
import|;
end_import

begin_import
import|import static
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
name|HStoreFile
operator|.
name|MAX_SEQ_ID_KEY
import|;
end_import

begin_import
import|import static
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
name|HStoreFile
operator|.
name|MOB_CELLS_COUNT
import|;
end_import

begin_import
import|import static
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
name|HStoreFile
operator|.
name|TIMERANGE_KEY
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
name|net
operator|.
name|InetSocketAddress
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
name|java
operator|.
name|util
operator|.
name|regex
operator|.
name|Pattern
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
name|CellComparator
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
name|PrivateCellUtil
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
name|client
operator|.
name|ColumnFamilyDescriptorBuilder
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
name|CacheConfig
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
name|HFileContext
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
name|BloomContext
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
name|BloomFilterFactory
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
name|BloomFilterWriter
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
name|hbase
operator|.
name|util
operator|.
name|RowBloomContext
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
name|RowColBloomContext
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
name|slf4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|LoggerFactory
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
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|base
operator|.
name|Preconditions
import|;
end_import

begin_comment
comment|/**  * A StoreFile writer.  Use this to read/write HBase Store Files. It is package  * local because it is an implementation detail of the HBase regionserver.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|StoreFileWriter
implements|implements
name|CellSink
implements|,
name|ShipperListener
block|{
specifier|private
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|StoreFileWriter
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|Pattern
name|dash
init|=
name|Pattern
operator|.
name|compile
argument_list|(
literal|"-"
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|BloomFilterWriter
name|generalBloomFilterWriter
decl_stmt|;
specifier|private
specifier|final
name|BloomFilterWriter
name|deleteFamilyBloomFilterWriter
decl_stmt|;
specifier|private
specifier|final
name|BloomType
name|bloomType
decl_stmt|;
specifier|private
name|long
name|earliestPutTs
init|=
name|HConstants
operator|.
name|LATEST_TIMESTAMP
decl_stmt|;
specifier|private
name|long
name|deleteFamilyCnt
init|=
literal|0
decl_stmt|;
specifier|private
name|BloomContext
name|bloomContext
init|=
literal|null
decl_stmt|;
specifier|private
name|BloomContext
name|deleteFamilyBloomContext
init|=
literal|null
decl_stmt|;
specifier|private
specifier|final
name|TimeRangeTracker
name|timeRangeTracker
decl_stmt|;
specifier|protected
name|HFile
operator|.
name|Writer
name|writer
decl_stmt|;
comment|/**      * Creates an HFile.Writer that also write helpful meta data.      * @param fs file system to write to      * @param path file name to create      * @param conf user configuration      * @param comparator key comparator      * @param bloomType bloom filter setting      * @param maxKeys the expected maximum number of keys to be added. Was used      *        for Bloom filter size in {@link HFile} format version 1.      * @param favoredNodes      * @param fileContext - The HFile context      * @param shouldDropCacheBehind Drop pages written to page cache after writing the store file.      * @throws IOException problem writing to FS      */
specifier|private
name|StoreFileWriter
parameter_list|(
name|FileSystem
name|fs
parameter_list|,
name|Path
name|path
parameter_list|,
specifier|final
name|Configuration
name|conf
parameter_list|,
name|CacheConfig
name|cacheConf
parameter_list|,
specifier|final
name|CellComparator
name|comparator
parameter_list|,
name|BloomType
name|bloomType
parameter_list|,
name|long
name|maxKeys
parameter_list|,
name|InetSocketAddress
index|[]
name|favoredNodes
parameter_list|,
name|HFileContext
name|fileContext
parameter_list|,
name|boolean
name|shouldDropCacheBehind
parameter_list|)
throws|throws
name|IOException
block|{
name|this
operator|.
name|timeRangeTracker
operator|=
name|TimeRangeTracker
operator|.
name|create
argument_list|(
name|TimeRangeTracker
operator|.
name|Type
operator|.
name|NON_SYNC
argument_list|)
expr_stmt|;
comment|// TODO : Change all writers to be specifically created for compaction context
name|writer
operator|=
name|HFile
operator|.
name|getWriterFactory
argument_list|(
name|conf
argument_list|,
name|cacheConf
argument_list|)
operator|.
name|withPath
argument_list|(
name|fs
argument_list|,
name|path
argument_list|)
operator|.
name|withComparator
argument_list|(
name|comparator
argument_list|)
operator|.
name|withFavoredNodes
argument_list|(
name|favoredNodes
argument_list|)
operator|.
name|withFileContext
argument_list|(
name|fileContext
argument_list|)
operator|.
name|withShouldDropCacheBehind
argument_list|(
name|shouldDropCacheBehind
argument_list|)
operator|.
name|create
argument_list|()
expr_stmt|;
name|generalBloomFilterWriter
operator|=
name|BloomFilterFactory
operator|.
name|createGeneralBloomAtWrite
argument_list|(
name|conf
argument_list|,
name|cacheConf
argument_list|,
name|bloomType
argument_list|,
operator|(
name|int
operator|)
name|Math
operator|.
name|min
argument_list|(
name|maxKeys
argument_list|,
name|Integer
operator|.
name|MAX_VALUE
argument_list|)
argument_list|,
name|writer
argument_list|)
expr_stmt|;
if|if
condition|(
name|generalBloomFilterWriter
operator|!=
literal|null
condition|)
block|{
name|this
operator|.
name|bloomType
operator|=
name|bloomType
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
literal|"Bloom filter type for "
operator|+
name|path
operator|+
literal|": "
operator|+
name|this
operator|.
name|bloomType
operator|+
literal|", "
operator|+
name|generalBloomFilterWriter
operator|.
name|getClass
argument_list|()
operator|.
name|getSimpleName
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|// init bloom context
switch|switch
condition|(
name|bloomType
condition|)
block|{
case|case
name|ROW
case|:
name|bloomContext
operator|=
operator|new
name|RowBloomContext
argument_list|(
name|generalBloomFilterWriter
argument_list|,
name|comparator
argument_list|)
expr_stmt|;
break|break;
case|case
name|ROWCOL
case|:
name|bloomContext
operator|=
operator|new
name|RowColBloomContext
argument_list|(
name|generalBloomFilterWriter
argument_list|,
name|comparator
argument_list|)
expr_stmt|;
break|break;
default|default:
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Invalid Bloom filter type: "
operator|+
name|bloomType
operator|+
literal|" (ROW or ROWCOL expected)"
argument_list|)
throw|;
block|}
block|}
else|else
block|{
comment|// Not using Bloom filters.
name|this
operator|.
name|bloomType
operator|=
name|BloomType
operator|.
name|NONE
expr_stmt|;
block|}
comment|// initialize delete family Bloom filter when there is NO RowCol Bloom
comment|// filter
if|if
condition|(
name|this
operator|.
name|bloomType
operator|!=
name|BloomType
operator|.
name|ROWCOL
condition|)
block|{
name|this
operator|.
name|deleteFamilyBloomFilterWriter
operator|=
name|BloomFilterFactory
operator|.
name|createDeleteBloomAtWrite
argument_list|(
name|conf
argument_list|,
name|cacheConf
argument_list|,
operator|(
name|int
operator|)
name|Math
operator|.
name|min
argument_list|(
name|maxKeys
argument_list|,
name|Integer
operator|.
name|MAX_VALUE
argument_list|)
argument_list|,
name|writer
argument_list|)
expr_stmt|;
name|deleteFamilyBloomContext
operator|=
operator|new
name|RowBloomContext
argument_list|(
name|deleteFamilyBloomFilterWriter
argument_list|,
name|comparator
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|deleteFamilyBloomFilterWriter
operator|=
literal|null
expr_stmt|;
block|}
if|if
condition|(
name|deleteFamilyBloomFilterWriter
operator|!=
literal|null
operator|&&
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
literal|"Delete Family Bloom filter type for "
operator|+
name|path
operator|+
literal|": "
operator|+
name|deleteFamilyBloomFilterWriter
operator|.
name|getClass
argument_list|()
operator|.
name|getSimpleName
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Writes meta data.    * Call before {@link #close()} since its written as meta data to this file.    * @param maxSequenceId Maximum sequence id.    * @param majorCompaction True if this file is product of a major compaction    * @throws IOException problem writing to FS    */
specifier|public
name|void
name|appendMetadata
parameter_list|(
specifier|final
name|long
name|maxSequenceId
parameter_list|,
specifier|final
name|boolean
name|majorCompaction
parameter_list|)
throws|throws
name|IOException
block|{
name|writer
operator|.
name|appendFileInfo
argument_list|(
name|MAX_SEQ_ID_KEY
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|maxSequenceId
argument_list|)
argument_list|)
expr_stmt|;
name|writer
operator|.
name|appendFileInfo
argument_list|(
name|MAJOR_COMPACTION_KEY
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|majorCompaction
argument_list|)
argument_list|)
expr_stmt|;
name|appendTrackedTimestampsToMetadata
argument_list|()
expr_stmt|;
block|}
comment|/**    * Writes meta data.    * Call before {@link #close()} since its written as meta data to this file.    * @param maxSequenceId Maximum sequence id.    * @param majorCompaction True if this file is product of a major compaction    * @param mobCellsCount The number of mob cells.    * @throws IOException problem writing to FS    */
specifier|public
name|void
name|appendMetadata
parameter_list|(
specifier|final
name|long
name|maxSequenceId
parameter_list|,
specifier|final
name|boolean
name|majorCompaction
parameter_list|,
specifier|final
name|long
name|mobCellsCount
parameter_list|)
throws|throws
name|IOException
block|{
name|writer
operator|.
name|appendFileInfo
argument_list|(
name|MAX_SEQ_ID_KEY
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|maxSequenceId
argument_list|)
argument_list|)
expr_stmt|;
name|writer
operator|.
name|appendFileInfo
argument_list|(
name|MAJOR_COMPACTION_KEY
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|majorCompaction
argument_list|)
argument_list|)
expr_stmt|;
name|writer
operator|.
name|appendFileInfo
argument_list|(
name|MOB_CELLS_COUNT
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|mobCellsCount
argument_list|)
argument_list|)
expr_stmt|;
name|appendTrackedTimestampsToMetadata
argument_list|()
expr_stmt|;
block|}
comment|/**    * Add TimestampRange and earliest put timestamp to Metadata    */
specifier|public
name|void
name|appendTrackedTimestampsToMetadata
parameter_list|()
throws|throws
name|IOException
block|{
comment|// TODO: The StoreFileReader always converts the byte[] to TimeRange
comment|// via TimeRangeTracker, so we should write the serialization data of TimeRange directly.
name|appendFileInfo
argument_list|(
name|TIMERANGE_KEY
argument_list|,
name|TimeRangeTracker
operator|.
name|toByteArray
argument_list|(
name|timeRangeTracker
argument_list|)
argument_list|)
expr_stmt|;
name|appendFileInfo
argument_list|(
name|EARLIEST_PUT_TS
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|earliestPutTs
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * Record the earlest Put timestamp.    *    * If the timeRangeTracker is not set,    * update TimeRangeTracker to include the timestamp of this key    */
specifier|public
name|void
name|trackTimestamps
parameter_list|(
specifier|final
name|Cell
name|cell
parameter_list|)
block|{
if|if
condition|(
name|KeyValue
operator|.
name|Type
operator|.
name|Put
operator|.
name|getCode
argument_list|()
operator|==
name|cell
operator|.
name|getTypeByte
argument_list|()
condition|)
block|{
name|earliestPutTs
operator|=
name|Math
operator|.
name|min
argument_list|(
name|earliestPutTs
argument_list|,
name|cell
operator|.
name|getTimestamp
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|timeRangeTracker
operator|.
name|includeTimestamp
argument_list|(
name|cell
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|appendGeneralBloomfilter
parameter_list|(
specifier|final
name|Cell
name|cell
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|this
operator|.
name|generalBloomFilterWriter
operator|!=
literal|null
condition|)
block|{
comment|/*        * http://2.bp.blogspot.com/_Cib_A77V54U/StZMrzaKufI/AAAAAAAAADo/ZhK7bGoJdMQ/s400/KeyValue.png        * Key = RowLen + Row + FamilyLen + Column [Family + Qualifier] + TimeStamp        *        * 2 Types of Filtering:        *  1. Row = Row        *  2. RowCol = Row + Qualifier        */
name|bloomContext
operator|.
name|writeBloom
argument_list|(
name|cell
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|appendDeleteFamilyBloomFilter
parameter_list|(
specifier|final
name|Cell
name|cell
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
operator|!
name|PrivateCellUtil
operator|.
name|isDeleteFamily
argument_list|(
name|cell
argument_list|)
operator|&&
operator|!
name|PrivateCellUtil
operator|.
name|isDeleteFamilyVersion
argument_list|(
name|cell
argument_list|)
condition|)
block|{
return|return;
block|}
comment|// increase the number of delete family in the store file
name|deleteFamilyCnt
operator|++
expr_stmt|;
if|if
condition|(
name|this
operator|.
name|deleteFamilyBloomFilterWriter
operator|!=
literal|null
condition|)
block|{
name|deleteFamilyBloomContext
operator|.
name|writeBloom
argument_list|(
name|cell
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|append
parameter_list|(
specifier|final
name|Cell
name|cell
parameter_list|)
throws|throws
name|IOException
block|{
name|appendGeneralBloomfilter
argument_list|(
name|cell
argument_list|)
expr_stmt|;
name|appendDeleteFamilyBloomFilter
argument_list|(
name|cell
argument_list|)
expr_stmt|;
name|writer
operator|.
name|append
argument_list|(
name|cell
argument_list|)
expr_stmt|;
name|trackTimestamps
argument_list|(
name|cell
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|beforeShipped
parameter_list|()
throws|throws
name|IOException
block|{
comment|// For now these writer will always be of type ShipperListener true.
comment|// TODO : Change all writers to be specifically created for compaction context
name|writer
operator|.
name|beforeShipped
argument_list|()
expr_stmt|;
if|if
condition|(
name|generalBloomFilterWriter
operator|!=
literal|null
condition|)
block|{
name|generalBloomFilterWriter
operator|.
name|beforeShipped
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|deleteFamilyBloomFilterWriter
operator|!=
literal|null
condition|)
block|{
name|deleteFamilyBloomFilterWriter
operator|.
name|beforeShipped
argument_list|()
expr_stmt|;
block|}
block|}
specifier|public
name|Path
name|getPath
parameter_list|()
block|{
return|return
name|this
operator|.
name|writer
operator|.
name|getPath
argument_list|()
return|;
block|}
specifier|public
name|boolean
name|hasGeneralBloom
parameter_list|()
block|{
return|return
name|this
operator|.
name|generalBloomFilterWriter
operator|!=
literal|null
return|;
block|}
comment|/**    * For unit testing only.    *    * @return the Bloom filter used by this writer.    */
name|BloomFilterWriter
name|getGeneralBloomWriter
parameter_list|()
block|{
return|return
name|generalBloomFilterWriter
return|;
block|}
specifier|private
name|boolean
name|closeBloomFilter
parameter_list|(
name|BloomFilterWriter
name|bfw
parameter_list|)
throws|throws
name|IOException
block|{
name|boolean
name|haveBloom
init|=
operator|(
name|bfw
operator|!=
literal|null
operator|&&
name|bfw
operator|.
name|getKeyCount
argument_list|()
operator|>
literal|0
operator|)
decl_stmt|;
if|if
condition|(
name|haveBloom
condition|)
block|{
name|bfw
operator|.
name|compactBloom
argument_list|()
expr_stmt|;
block|}
return|return
name|haveBloom
return|;
block|}
specifier|private
name|boolean
name|closeGeneralBloomFilter
parameter_list|()
throws|throws
name|IOException
block|{
name|boolean
name|hasGeneralBloom
init|=
name|closeBloomFilter
argument_list|(
name|generalBloomFilterWriter
argument_list|)
decl_stmt|;
comment|// add the general Bloom filter writer and append file info
if|if
condition|(
name|hasGeneralBloom
condition|)
block|{
name|writer
operator|.
name|addGeneralBloomFilter
argument_list|(
name|generalBloomFilterWriter
argument_list|)
expr_stmt|;
name|writer
operator|.
name|appendFileInfo
argument_list|(
name|BLOOM_FILTER_TYPE_KEY
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|bloomType
operator|.
name|toString
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|bloomContext
operator|.
name|addLastBloomKey
argument_list|(
name|writer
argument_list|)
expr_stmt|;
block|}
return|return
name|hasGeneralBloom
return|;
block|}
specifier|private
name|boolean
name|closeDeleteFamilyBloomFilter
parameter_list|()
throws|throws
name|IOException
block|{
name|boolean
name|hasDeleteFamilyBloom
init|=
name|closeBloomFilter
argument_list|(
name|deleteFamilyBloomFilterWriter
argument_list|)
decl_stmt|;
comment|// add the delete family Bloom filter writer
if|if
condition|(
name|hasDeleteFamilyBloom
condition|)
block|{
name|writer
operator|.
name|addDeleteFamilyBloomFilter
argument_list|(
name|deleteFamilyBloomFilterWriter
argument_list|)
expr_stmt|;
block|}
comment|// append file info about the number of delete family kvs
comment|// even if there is no delete family Bloom.
name|writer
operator|.
name|appendFileInfo
argument_list|(
name|DELETE_FAMILY_COUNT
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|this
operator|.
name|deleteFamilyCnt
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|hasDeleteFamilyBloom
return|;
block|}
specifier|public
name|void
name|close
parameter_list|()
throws|throws
name|IOException
block|{
name|boolean
name|hasGeneralBloom
init|=
name|this
operator|.
name|closeGeneralBloomFilter
argument_list|()
decl_stmt|;
name|boolean
name|hasDeleteFamilyBloom
init|=
name|this
operator|.
name|closeDeleteFamilyBloomFilter
argument_list|()
decl_stmt|;
name|writer
operator|.
name|close
argument_list|()
expr_stmt|;
comment|// Log final Bloom filter statistics. This needs to be done after close()
comment|// because compound Bloom filters might be finalized as part of closing.
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
operator|(
name|hasGeneralBloom
condition|?
literal|""
else|:
literal|"NO "
operator|)
operator|+
literal|"General Bloom and "
operator|+
operator|(
name|hasDeleteFamilyBloom
condition|?
literal|""
else|:
literal|"NO "
operator|)
operator|+
literal|"DeleteFamily"
operator|+
literal|" was added to HFile "
operator|+
name|getPath
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|void
name|appendFileInfo
parameter_list|(
name|byte
index|[]
name|key
parameter_list|,
name|byte
index|[]
name|value
parameter_list|)
throws|throws
name|IOException
block|{
name|writer
operator|.
name|appendFileInfo
argument_list|(
name|key
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
comment|/** For use in testing.    */
name|HFile
operator|.
name|Writer
name|getHFileWriter
parameter_list|()
block|{
return|return
name|writer
return|;
block|}
comment|/**    * @param fs    * @param dir Directory to create file in.    * @return random filename inside passed<code>dir</code>    */
specifier|static
name|Path
name|getUniqueFile
parameter_list|(
specifier|final
name|FileSystem
name|fs
parameter_list|,
specifier|final
name|Path
name|dir
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
operator|!
name|fs
operator|.
name|getFileStatus
argument_list|(
name|dir
argument_list|)
operator|.
name|isDirectory
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Expecting "
operator|+
name|dir
operator|.
name|toString
argument_list|()
operator|+
literal|" to be a directory"
argument_list|)
throw|;
block|}
return|return
operator|new
name|Path
argument_list|(
name|dir
argument_list|,
name|dash
operator|.
name|matcher
argument_list|(
name|UUID
operator|.
name|randomUUID
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
operator|.
name|replaceAll
argument_list|(
literal|""
argument_list|)
argument_list|)
return|;
block|}
annotation|@
name|edu
operator|.
name|umd
operator|.
name|cs
operator|.
name|findbugs
operator|.
name|annotations
operator|.
name|SuppressWarnings
argument_list|(
name|value
operator|=
literal|"ICAST_INTEGER_MULTIPLY_CAST_TO_LONG"
argument_list|,
name|justification
operator|=
literal|"Will not overflow"
argument_list|)
specifier|public
specifier|static
class|class
name|Builder
block|{
specifier|private
specifier|final
name|Configuration
name|conf
decl_stmt|;
specifier|private
specifier|final
name|CacheConfig
name|cacheConf
decl_stmt|;
specifier|private
specifier|final
name|FileSystem
name|fs
decl_stmt|;
specifier|private
name|CellComparator
name|comparator
init|=
name|CellComparator
operator|.
name|getInstance
argument_list|()
decl_stmt|;
specifier|private
name|BloomType
name|bloomType
init|=
name|BloomType
operator|.
name|NONE
decl_stmt|;
specifier|private
name|long
name|maxKeyCount
init|=
literal|0
decl_stmt|;
specifier|private
name|Path
name|dir
decl_stmt|;
specifier|private
name|Path
name|filePath
decl_stmt|;
specifier|private
name|InetSocketAddress
index|[]
name|favoredNodes
decl_stmt|;
specifier|private
name|HFileContext
name|fileContext
decl_stmt|;
specifier|private
name|boolean
name|shouldDropCacheBehind
decl_stmt|;
specifier|public
name|Builder
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|CacheConfig
name|cacheConf
parameter_list|,
name|FileSystem
name|fs
parameter_list|)
block|{
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
name|this
operator|.
name|cacheConf
operator|=
name|cacheConf
expr_stmt|;
name|this
operator|.
name|fs
operator|=
name|fs
expr_stmt|;
block|}
comment|/**      * Creates Builder with cache configuration disabled      */
specifier|public
name|Builder
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|FileSystem
name|fs
parameter_list|)
block|{
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
name|this
operator|.
name|cacheConf
operator|=
name|CacheConfig
operator|.
name|DISABLED
expr_stmt|;
name|this
operator|.
name|fs
operator|=
name|fs
expr_stmt|;
block|}
comment|/**      * Use either this method or {@link #withFilePath}, but not both.      * @param dir Path to column family directory. The directory is created if      *          does not exist. The file is given a unique name within this      *          directory.      * @return this (for chained invocation)      */
specifier|public
name|Builder
name|withOutputDir
parameter_list|(
name|Path
name|dir
parameter_list|)
block|{
name|Preconditions
operator|.
name|checkNotNull
argument_list|(
name|dir
argument_list|)
expr_stmt|;
name|this
operator|.
name|dir
operator|=
name|dir
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Use either this method or {@link #withOutputDir}, but not both.      * @param filePath the StoreFile path to write      * @return this (for chained invocation)      */
specifier|public
name|Builder
name|withFilePath
parameter_list|(
name|Path
name|filePath
parameter_list|)
block|{
name|Preconditions
operator|.
name|checkNotNull
argument_list|(
name|filePath
argument_list|)
expr_stmt|;
name|this
operator|.
name|filePath
operator|=
name|filePath
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * @param favoredNodes an array of favored nodes or possibly null      * @return this (for chained invocation)      */
specifier|public
name|Builder
name|withFavoredNodes
parameter_list|(
name|InetSocketAddress
index|[]
name|favoredNodes
parameter_list|)
block|{
name|this
operator|.
name|favoredNodes
operator|=
name|favoredNodes
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|Builder
name|withComparator
parameter_list|(
name|CellComparator
name|comparator
parameter_list|)
block|{
name|Preconditions
operator|.
name|checkNotNull
argument_list|(
name|comparator
argument_list|)
expr_stmt|;
name|this
operator|.
name|comparator
operator|=
name|comparator
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|Builder
name|withBloomType
parameter_list|(
name|BloomType
name|bloomType
parameter_list|)
block|{
name|Preconditions
operator|.
name|checkNotNull
argument_list|(
name|bloomType
argument_list|)
expr_stmt|;
name|this
operator|.
name|bloomType
operator|=
name|bloomType
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * @param maxKeyCount estimated maximum number of keys we expect to add      * @return this (for chained invocation)      */
specifier|public
name|Builder
name|withMaxKeyCount
parameter_list|(
name|long
name|maxKeyCount
parameter_list|)
block|{
name|this
operator|.
name|maxKeyCount
operator|=
name|maxKeyCount
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|Builder
name|withFileContext
parameter_list|(
name|HFileContext
name|fileContext
parameter_list|)
block|{
name|this
operator|.
name|fileContext
operator|=
name|fileContext
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|Builder
name|withShouldDropCacheBehind
parameter_list|(
name|boolean
name|shouldDropCacheBehind
parameter_list|)
block|{
name|this
operator|.
name|shouldDropCacheBehind
operator|=
name|shouldDropCacheBehind
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Create a store file writer. Client is responsible for closing file when      * done. If metadata, add BEFORE closing using      * {@link StoreFileWriter#appendMetadata}.      */
specifier|public
name|StoreFileWriter
name|build
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
operator|(
name|dir
operator|==
literal|null
condition|?
literal|0
else|:
literal|1
operator|)
operator|+
operator|(
name|filePath
operator|==
literal|null
condition|?
literal|0
else|:
literal|1
operator|)
operator|!=
literal|1
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Either specify parent directory "
operator|+
literal|"or file path"
argument_list|)
throw|;
block|}
if|if
condition|(
name|dir
operator|==
literal|null
condition|)
block|{
name|dir
operator|=
name|filePath
operator|.
name|getParent
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|fs
operator|.
name|exists
argument_list|(
name|dir
argument_list|)
condition|)
block|{
comment|// Handle permission for non-HDFS filesystem properly
comment|// See HBASE-17710
name|HRegionFileSystem
operator|.
name|mkdirs
argument_list|(
name|fs
argument_list|,
name|conf
argument_list|,
name|dir
argument_list|)
expr_stmt|;
block|}
comment|// set block storage policy for temp path
name|String
name|policyName
init|=
name|this
operator|.
name|conf
operator|.
name|get
argument_list|(
name|ColumnFamilyDescriptorBuilder
operator|.
name|STORAGE_POLICY
argument_list|)
decl_stmt|;
if|if
condition|(
literal|null
operator|==
name|policyName
condition|)
block|{
name|policyName
operator|=
name|this
operator|.
name|conf
operator|.
name|get
argument_list|(
name|HStore
operator|.
name|BLOCK_STORAGE_POLICY_KEY
argument_list|)
expr_stmt|;
block|}
name|FSUtils
operator|.
name|setStoragePolicy
argument_list|(
name|this
operator|.
name|fs
argument_list|,
name|dir
argument_list|,
name|policyName
argument_list|)
expr_stmt|;
if|if
condition|(
name|filePath
operator|==
literal|null
condition|)
block|{
name|filePath
operator|=
name|getUniqueFile
argument_list|(
name|fs
argument_list|,
name|dir
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|BloomFilterFactory
operator|.
name|isGeneralBloomEnabled
argument_list|(
name|conf
argument_list|)
condition|)
block|{
name|bloomType
operator|=
name|BloomType
operator|.
name|NONE
expr_stmt|;
block|}
block|}
if|if
condition|(
name|comparator
operator|==
literal|null
condition|)
block|{
name|comparator
operator|=
name|CellComparator
operator|.
name|getInstance
argument_list|()
expr_stmt|;
block|}
return|return
operator|new
name|StoreFileWriter
argument_list|(
name|fs
argument_list|,
name|filePath
argument_list|,
name|conf
argument_list|,
name|cacheConf
argument_list|,
name|comparator
argument_list|,
name|bloomType
argument_list|,
name|maxKeyCount
argument_list|,
name|favoredNodes
argument_list|,
name|fileContext
argument_list|,
name|shouldDropCacheBehind
argument_list|)
return|;
block|}
block|}
block|}
end_class

end_unit

