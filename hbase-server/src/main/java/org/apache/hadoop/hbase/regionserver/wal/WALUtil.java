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
name|Map
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|NavigableMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|function
operator|.
name|Function
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
name|client
operator|.
name|RegionInfo
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
name|regionserver
operator|.
name|MultiVersionConcurrencyControl
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
name|CommonFSUtils
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
name|WAL
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
name|WALEdit
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
name|WALKeyImpl
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
name|TextFormat
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
name|WALProtos
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
name|WALProtos
operator|.
name|CompactionDescriptor
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
name|WALProtos
operator|.
name|FlushDescriptor
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
name|WALProtos
operator|.
name|RegionEventDescriptor
import|;
end_import

begin_comment
comment|/**  * Helper methods to ease Region Server integration with the Write Ahead Log (WAL).  * Note that methods in this class specifically should not require access to anything  * other than the API found in {@link WAL}. For internal use only.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|WALUtil
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
name|WALUtil
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|WALUtil
parameter_list|()
block|{
comment|// Shut down construction of this class.
block|}
comment|/**    * Write the marker that a compaction has succeeded and is about to be committed. This provides    * info to the HMaster to allow it to recover the compaction if this regionserver dies in the    * middle. It also prevents the compaction from finishing if this regionserver has already lost    * its lease on the log.    *<p/>    * This write is for internal use only. Not for external client consumption.    * @param mvcc Used by WAL to get sequence Id for the waledit.    */
specifier|public
specifier|static
name|WALKeyImpl
name|writeCompactionMarker
parameter_list|(
name|WAL
name|wal
parameter_list|,
name|NavigableMap
argument_list|<
name|byte
index|[]
argument_list|,
name|Integer
argument_list|>
name|replicationScope
parameter_list|,
name|RegionInfo
name|hri
parameter_list|,
specifier|final
name|CompactionDescriptor
name|c
parameter_list|,
name|MultiVersionConcurrencyControl
name|mvcc
parameter_list|)
throws|throws
name|IOException
block|{
name|WALKeyImpl
name|walKey
init|=
name|writeMarker
argument_list|(
name|wal
argument_list|,
name|replicationScope
argument_list|,
name|hri
argument_list|,
name|WALEdit
operator|.
name|createCompaction
argument_list|(
name|hri
argument_list|,
name|c
argument_list|)
argument_list|,
name|mvcc
argument_list|,
literal|null
argument_list|)
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
literal|"Appended compaction marker "
operator|+
name|TextFormat
operator|.
name|shortDebugString
argument_list|(
name|c
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|walKey
return|;
block|}
comment|/**    * Write a flush marker indicating a start / abort or a complete of a region flush    *<p/>    * This write is for internal use only. Not for external client consumption.    */
specifier|public
specifier|static
name|WALKeyImpl
name|writeFlushMarker
parameter_list|(
name|WAL
name|wal
parameter_list|,
name|NavigableMap
argument_list|<
name|byte
index|[]
argument_list|,
name|Integer
argument_list|>
name|replicationScope
parameter_list|,
name|RegionInfo
name|hri
parameter_list|,
specifier|final
name|FlushDescriptor
name|f
parameter_list|,
name|boolean
name|sync
parameter_list|,
name|MultiVersionConcurrencyControl
name|mvcc
parameter_list|)
throws|throws
name|IOException
block|{
name|WALKeyImpl
name|walKey
init|=
name|doFullMarkerAppendTransaction
argument_list|(
name|wal
argument_list|,
name|replicationScope
argument_list|,
name|hri
argument_list|,
name|WALEdit
operator|.
name|createFlushWALEdit
argument_list|(
name|hri
argument_list|,
name|f
argument_list|)
argument_list|,
name|mvcc
argument_list|,
literal|null
argument_list|,
name|sync
argument_list|)
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
literal|"Appended flush marker "
operator|+
name|TextFormat
operator|.
name|shortDebugString
argument_list|(
name|f
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|walKey
return|;
block|}
comment|/**    * Write a region open marker indicating that the region is opened. This write is for internal use    * only. Not for external client consumption.    */
specifier|public
specifier|static
name|WALKeyImpl
name|writeRegionEventMarker
parameter_list|(
name|WAL
name|wal
parameter_list|,
name|NavigableMap
argument_list|<
name|byte
index|[]
argument_list|,
name|Integer
argument_list|>
name|replicationScope
parameter_list|,
name|RegionInfo
name|hri
parameter_list|,
name|RegionEventDescriptor
name|r
parameter_list|,
name|MultiVersionConcurrencyControl
name|mvcc
parameter_list|)
throws|throws
name|IOException
block|{
name|WALKeyImpl
name|walKey
init|=
name|writeMarker
argument_list|(
name|wal
argument_list|,
name|replicationScope
argument_list|,
name|hri
argument_list|,
name|WALEdit
operator|.
name|createRegionEventWALEdit
argument_list|(
name|hri
argument_list|,
name|r
argument_list|)
argument_list|,
name|mvcc
argument_list|,
literal|null
argument_list|)
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
literal|"Appended region event marker "
operator|+
name|TextFormat
operator|.
name|shortDebugString
argument_list|(
name|r
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|walKey
return|;
block|}
comment|/**    * Write a log marker that a bulk load has succeeded and is about to be committed.    * This write is for internal use only. Not for external client consumption.    * @param wal The log to write into.    * @param replicationScope The replication scope of the families in the HRegion    * @param hri A description of the region in the table that we are bulk loading into.    * @param desc A protocol buffers based description of the client's bulk loading request    * @return walKey with sequenceid filled out for this bulk load marker    * @throws IOException We will throw an IOException if we can not append to the HLog.    */
specifier|public
specifier|static
name|WALKeyImpl
name|writeBulkLoadMarkerAndSync
parameter_list|(
specifier|final
name|WAL
name|wal
parameter_list|,
specifier|final
name|NavigableMap
argument_list|<
name|byte
index|[]
argument_list|,
name|Integer
argument_list|>
name|replicationScope
parameter_list|,
specifier|final
name|RegionInfo
name|hri
parameter_list|,
specifier|final
name|WALProtos
operator|.
name|BulkLoadDescriptor
name|desc
parameter_list|,
specifier|final
name|MultiVersionConcurrencyControl
name|mvcc
parameter_list|)
throws|throws
name|IOException
block|{
name|WALKeyImpl
name|walKey
init|=
name|writeMarker
argument_list|(
name|wal
argument_list|,
name|replicationScope
argument_list|,
name|hri
argument_list|,
name|WALEdit
operator|.
name|createBulkLoadEvent
argument_list|(
name|hri
argument_list|,
name|desc
argument_list|)
argument_list|,
name|mvcc
argument_list|,
literal|null
argument_list|)
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
literal|"Appended Bulk Load marker "
operator|+
name|TextFormat
operator|.
name|shortDebugString
argument_list|(
name|desc
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|walKey
return|;
block|}
specifier|private
specifier|static
name|WALKeyImpl
name|writeMarker
parameter_list|(
specifier|final
name|WAL
name|wal
parameter_list|,
specifier|final
name|NavigableMap
argument_list|<
name|byte
index|[]
argument_list|,
name|Integer
argument_list|>
name|replicationScope
parameter_list|,
specifier|final
name|RegionInfo
name|hri
parameter_list|,
specifier|final
name|WALEdit
name|edit
parameter_list|,
specifier|final
name|MultiVersionConcurrencyControl
name|mvcc
parameter_list|,
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|byte
index|[]
argument_list|>
name|extendedAttributes
parameter_list|)
throws|throws
name|IOException
block|{
comment|// If sync == true in below, then timeout is not used; safe to pass UNSPECIFIED_TIMEOUT
return|return
name|doFullMarkerAppendTransaction
argument_list|(
name|wal
argument_list|,
name|replicationScope
argument_list|,
name|hri
argument_list|,
name|edit
argument_list|,
name|mvcc
argument_list|,
name|extendedAttributes
argument_list|,
literal|true
argument_list|)
return|;
block|}
comment|/**    * A 'full' WAL transaction involves starting an mvcc transaction followed by an append, an    * optional sync, and then a call to complete the mvcc transaction. This method does it all. Good    * for case of adding a single edit or marker to the WAL.    *<p/>    * This write is for internal use only. Not for external client consumption.    * @return WALKeyImpl that was added to the WAL.    */
specifier|private
specifier|static
name|WALKeyImpl
name|doFullMarkerAppendTransaction
parameter_list|(
specifier|final
name|WAL
name|wal
parameter_list|,
specifier|final
name|NavigableMap
argument_list|<
name|byte
index|[]
argument_list|,
name|Integer
argument_list|>
name|replicationScope
parameter_list|,
specifier|final
name|RegionInfo
name|hri
parameter_list|,
specifier|final
name|WALEdit
name|edit
parameter_list|,
specifier|final
name|MultiVersionConcurrencyControl
name|mvcc
parameter_list|,
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|byte
index|[]
argument_list|>
name|extendedAttributes
parameter_list|,
specifier|final
name|boolean
name|sync
parameter_list|)
throws|throws
name|IOException
block|{
comment|// TODO: Pass in current time to use?
name|WALKeyImpl
name|walKey
init|=
operator|new
name|WALKeyImpl
argument_list|(
name|hri
operator|.
name|getEncodedNameAsBytes
argument_list|()
argument_list|,
name|hri
operator|.
name|getTable
argument_list|()
argument_list|,
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|,
name|mvcc
argument_list|,
name|replicationScope
argument_list|,
name|extendedAttributes
argument_list|)
decl_stmt|;
name|long
name|trx
init|=
name|MultiVersionConcurrencyControl
operator|.
name|NONE
decl_stmt|;
try|try
block|{
name|trx
operator|=
name|wal
operator|.
name|appendMarker
argument_list|(
name|hri
argument_list|,
name|walKey
argument_list|,
name|edit
argument_list|)
expr_stmt|;
if|if
condition|(
name|sync
condition|)
block|{
name|wal
operator|.
name|sync
argument_list|(
name|trx
argument_list|)
expr_stmt|;
block|}
comment|// Call complete only here because these are markers only. They are not for clients to read.
name|mvcc
operator|.
name|complete
argument_list|(
name|walKey
operator|.
name|getWriteEntry
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|ioe
parameter_list|)
block|{
if|if
condition|(
name|walKey
operator|.
name|getWriteEntry
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|mvcc
operator|.
name|complete
argument_list|(
name|walKey
operator|.
name|getWriteEntry
argument_list|()
argument_list|)
expr_stmt|;
block|}
throw|throw
name|ioe
throw|;
block|}
return|return
name|walKey
return|;
block|}
comment|/**    * Blocksize returned here is 2x the default HDFS blocksize unless explicitly set in    * Configuration. Works in tandem with hbase.regionserver.logroll.multiplier. See comment in    * AbstractFSWAL in Constructor where we set blocksize and logrollsize for why.    * @return Blocksize to use writing WALs.    */
specifier|public
specifier|static
name|long
name|getWALBlockSize
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|FileSystem
name|fs
parameter_list|,
name|Path
name|dir
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|getWALBlockSize
argument_list|(
name|conf
argument_list|,
name|fs
argument_list|,
name|dir
argument_list|,
literal|false
argument_list|)
return|;
block|}
comment|/**    * Public because of FSHLog. Should be package-private    * @param isRecoverEdits the created writer is for recovered edits or WAL.    *                       For recovered edits, it is true and for WAL it is false.    */
specifier|public
specifier|static
name|long
name|getWALBlockSize
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|FileSystem
name|fs
parameter_list|,
name|Path
name|dir
parameter_list|,
name|boolean
name|isRecoverEdits
parameter_list|)
throws|throws
name|IOException
block|{
name|long
name|defaultBlockSize
init|=
name|CommonFSUtils
operator|.
name|getDefaultBlockSize
argument_list|(
name|fs
argument_list|,
name|dir
argument_list|)
operator|*
literal|2
decl_stmt|;
if|if
condition|(
name|isRecoverEdits
condition|)
block|{
return|return
name|conf
operator|.
name|getLong
argument_list|(
literal|"hbase.regionserver.recoverededits.blocksize"
argument_list|,
name|defaultBlockSize
argument_list|)
return|;
block|}
return|return
name|conf
operator|.
name|getLong
argument_list|(
literal|"hbase.regionserver.hlog.blocksize"
argument_list|,
name|defaultBlockSize
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|void
name|filterCells
parameter_list|(
name|WALEdit
name|edit
parameter_list|,
name|Function
argument_list|<
name|Cell
argument_list|,
name|Cell
argument_list|>
name|mapper
parameter_list|)
block|{
name|ArrayList
argument_list|<
name|Cell
argument_list|>
name|cells
init|=
name|edit
operator|.
name|getCells
argument_list|()
decl_stmt|;
name|int
name|size
init|=
name|cells
operator|.
name|size
argument_list|()
decl_stmt|;
name|int
name|newSize
init|=
literal|0
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|size
condition|;
name|i
operator|++
control|)
block|{
name|Cell
name|cell
init|=
name|mapper
operator|.
name|apply
argument_list|(
name|cells
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|cell
operator|!=
literal|null
condition|)
block|{
name|cells
operator|.
name|set
argument_list|(
name|newSize
argument_list|,
name|cell
argument_list|)
expr_stmt|;
name|newSize
operator|++
expr_stmt|;
block|}
block|}
for|for
control|(
name|int
name|i
init|=
name|size
operator|-
literal|1
init|;
name|i
operator|>=
name|newSize
condition|;
name|i
operator|--
control|)
block|{
name|cells
operator|.
name|remove
argument_list|(
name|i
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|newSize
operator|<
name|size
operator|/
literal|2
condition|)
block|{
name|cells
operator|.
name|trimToSize
argument_list|()
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

