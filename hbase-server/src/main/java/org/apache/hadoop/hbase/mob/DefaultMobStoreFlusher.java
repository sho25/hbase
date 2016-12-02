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
name|mob
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
name|Date
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
name|monitoring
operator|.
name|MonitoredTask
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
name|DefaultStoreFlusher
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
name|HMobStore
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
name|InternalScanner
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
name|MemStoreSnapshot
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
name|ScannerContext
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
name|Store
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
name|StoreFileWriter
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
name|throttle
operator|.
name|ThroughputController
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
name|util
operator|.
name|StringUtils
import|;
end_import

begin_comment
comment|/**  * An implementation of the StoreFlusher. It extends the DefaultStoreFlusher.  * If the store is not a mob store, the flusher flushes the MemStore the same with  * DefaultStoreFlusher,  * If the store is a mob store, the flusher flushes the MemStore into two places.  * One is the store files of HBase, the other is the mob files.  *<ol>  *<li>Cells that are not PUT type or have the delete mark will be directly flushed to HBase.</li>  *<li>If the size of a cell value is larger than a threshold, it'll be flushed  * to a mob file, another cell with the path of this file will be flushed to HBase.</li>  *<li>If the size of a cell value is smaller than or equal with a threshold, it'll be flushed to  * HBase directly.</li>  *</ol>  *  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|DefaultMobStoreFlusher
extends|extends
name|DefaultStoreFlusher
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
name|DefaultMobStoreFlusher
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|Object
name|flushLock
init|=
operator|new
name|Object
argument_list|()
decl_stmt|;
specifier|private
name|long
name|mobCellValueSizeThreshold
init|=
literal|0
decl_stmt|;
specifier|private
name|Path
name|targetPath
decl_stmt|;
specifier|private
name|HMobStore
name|mobStore
decl_stmt|;
specifier|public
name|DefaultMobStoreFlusher
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|Store
name|store
parameter_list|)
throws|throws
name|IOException
block|{
name|super
argument_list|(
name|conf
argument_list|,
name|store
argument_list|)
expr_stmt|;
name|mobCellValueSizeThreshold
operator|=
name|store
operator|.
name|getFamily
argument_list|()
operator|.
name|getMobThreshold
argument_list|()
expr_stmt|;
name|this
operator|.
name|targetPath
operator|=
name|MobUtils
operator|.
name|getMobFamilyPath
argument_list|(
name|conf
argument_list|,
name|store
operator|.
name|getTableName
argument_list|()
argument_list|,
name|store
operator|.
name|getColumnFamilyName
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|this
operator|.
name|store
operator|.
name|getFileSystem
argument_list|()
operator|.
name|exists
argument_list|(
name|targetPath
argument_list|)
condition|)
block|{
name|this
operator|.
name|store
operator|.
name|getFileSystem
argument_list|()
operator|.
name|mkdirs
argument_list|(
name|targetPath
argument_list|)
expr_stmt|;
block|}
name|this
operator|.
name|mobStore
operator|=
operator|(
name|HMobStore
operator|)
name|store
expr_stmt|;
block|}
comment|/**    * Flushes the snapshot of the MemStore.    * If this store is not a mob store, flush the cells in the snapshot to store files of HBase.    * If the store is a mob one, the flusher flushes the MemStore into two places.    * One is the store files of HBase, the other is the mob files.    *<ol>    *<li>Cells that are not PUT type or have the delete mark will be directly flushed to    * HBase.</li>    *<li>If the size of a cell value is larger than a threshold, it'll be    * flushed to a mob file, another cell with the path of this file will be flushed to HBase.</li>    *<li>If the size of a cell value is smaller than or equal with a threshold, it'll be flushed to    * HBase directly.</li>    *</ol>    */
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|Path
argument_list|>
name|flushSnapshot
parameter_list|(
name|MemStoreSnapshot
name|snapshot
parameter_list|,
name|long
name|cacheFlushId
parameter_list|,
name|MonitoredTask
name|status
parameter_list|,
name|ThroughputController
name|throughputController
parameter_list|)
throws|throws
name|IOException
block|{
name|ArrayList
argument_list|<
name|Path
argument_list|>
name|result
init|=
operator|new
name|ArrayList
argument_list|<
name|Path
argument_list|>
argument_list|()
decl_stmt|;
name|long
name|cellsCount
init|=
name|snapshot
operator|.
name|getCellsCount
argument_list|()
decl_stmt|;
if|if
condition|(
name|cellsCount
operator|==
literal|0
condition|)
return|return
name|result
return|;
comment|// don't flush if there are no entries
comment|// Use a store scanner to find which rows to flush.
name|long
name|smallestReadPoint
init|=
name|store
operator|.
name|getSmallestReadPoint
argument_list|()
decl_stmt|;
name|InternalScanner
name|scanner
init|=
name|createScanner
argument_list|(
name|snapshot
operator|.
name|getScanner
argument_list|()
argument_list|,
name|smallestReadPoint
argument_list|)
decl_stmt|;
if|if
condition|(
name|scanner
operator|==
literal|null
condition|)
block|{
return|return
name|result
return|;
comment|// NULL scanner returned from coprocessor hooks means skip normal processing
block|}
name|StoreFileWriter
name|writer
decl_stmt|;
try|try
block|{
comment|// TODO: We can fail in the below block before we complete adding this flush to
comment|// list of store files. Add cleanup of anything put on filesystem if we fail.
synchronized|synchronized
init|(
name|flushLock
init|)
block|{
name|status
operator|.
name|setStatus
argument_list|(
literal|"Flushing "
operator|+
name|store
operator|+
literal|": creating writer"
argument_list|)
expr_stmt|;
comment|// Write the map out to the disk
name|writer
operator|=
name|store
operator|.
name|createWriterInTmp
argument_list|(
name|cellsCount
argument_list|,
name|store
operator|.
name|getFamily
argument_list|()
operator|.
name|getCompression
argument_list|()
argument_list|,
literal|false
argument_list|,
literal|true
argument_list|,
literal|true
argument_list|,
literal|false
comment|/*default for dropbehind*/
argument_list|,
name|snapshot
operator|.
name|getTimeRangeTracker
argument_list|()
argument_list|)
expr_stmt|;
try|try
block|{
comment|// It's a mob store, flush the cells in a mob way. This is the difference of flushing
comment|// between a normal and a mob store.
name|performMobFlush
argument_list|(
name|snapshot
argument_list|,
name|cacheFlushId
argument_list|,
name|scanner
argument_list|,
name|writer
argument_list|,
name|status
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|finalizeWriter
argument_list|(
name|writer
argument_list|,
name|cacheFlushId
argument_list|,
name|status
argument_list|)
expr_stmt|;
block|}
block|}
block|}
finally|finally
block|{
name|scanner
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"Mob store is flushed, sequenceid="
operator|+
name|cacheFlushId
operator|+
literal|", memsize="
operator|+
name|StringUtils
operator|.
name|TraditionalBinaryPrefix
operator|.
name|long2String
argument_list|(
name|snapshot
operator|.
name|getDataSize
argument_list|()
argument_list|,
literal|""
argument_list|,
literal|1
argument_list|)
operator|+
literal|", hasBloomFilter="
operator|+
name|writer
operator|.
name|hasGeneralBloom
argument_list|()
operator|+
literal|", into tmp file "
operator|+
name|writer
operator|.
name|getPath
argument_list|()
argument_list|)
expr_stmt|;
name|result
operator|.
name|add
argument_list|(
name|writer
operator|.
name|getPath
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|result
return|;
block|}
comment|/**    * Flushes the cells in the mob store.    *<ol>In the mob store, the cells with PUT type might have or have no mob tags.    *<li>If a cell does not have a mob tag, flushing the cell to different files depends    * on the value length. If the length is larger than a threshold, it's flushed to a    * mob file and the mob file is flushed to a store file in HBase. Otherwise, directly    * flush the cell to a store file in HBase.</li>    *<li>If a cell have a mob tag, its value is a mob file name, directly flush it    * to a store file in HBase.</li>    *</ol>    * @param snapshot Memstore snapshot.    * @param cacheFlushId Log cache flush sequence number.    * @param scanner The scanner of memstore snapshot.    * @param writer The store file writer.    * @param status Task that represents the flush operation and may be updated with status.    * @throws IOException    */
specifier|protected
name|void
name|performMobFlush
parameter_list|(
name|MemStoreSnapshot
name|snapshot
parameter_list|,
name|long
name|cacheFlushId
parameter_list|,
name|InternalScanner
name|scanner
parameter_list|,
name|StoreFileWriter
name|writer
parameter_list|,
name|MonitoredTask
name|status
parameter_list|)
throws|throws
name|IOException
block|{
name|StoreFileWriter
name|mobFileWriter
init|=
literal|null
decl_stmt|;
name|int
name|compactionKVMax
init|=
name|conf
operator|.
name|getInt
argument_list|(
name|HConstants
operator|.
name|COMPACTION_KV_MAX
argument_list|,
name|HConstants
operator|.
name|COMPACTION_KV_MAX_DEFAULT
argument_list|)
decl_stmt|;
name|long
name|mobCount
init|=
literal|0
decl_stmt|;
name|long
name|mobSize
init|=
literal|0
decl_stmt|;
name|long
name|time
init|=
name|snapshot
operator|.
name|getTimeRangeTracker
argument_list|()
operator|.
name|getMax
argument_list|()
decl_stmt|;
name|mobFileWriter
operator|=
name|mobStore
operator|.
name|createWriterInTmp
argument_list|(
operator|new
name|Date
argument_list|(
name|time
argument_list|)
argument_list|,
name|snapshot
operator|.
name|getCellsCount
argument_list|()
argument_list|,
name|store
operator|.
name|getFamily
argument_list|()
operator|.
name|getCompression
argument_list|()
argument_list|,
name|store
operator|.
name|getRegionInfo
argument_list|()
operator|.
name|getStartKey
argument_list|()
argument_list|)
expr_stmt|;
comment|// the target path is {tableName}/.mob/{cfName}/mobFiles
comment|// the relative path is mobFiles
name|byte
index|[]
name|fileName
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|mobFileWriter
operator|.
name|getPath
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
try|try
block|{
name|List
argument_list|<
name|Cell
argument_list|>
name|cells
init|=
operator|new
name|ArrayList
argument_list|<
name|Cell
argument_list|>
argument_list|()
decl_stmt|;
name|boolean
name|hasMore
decl_stmt|;
name|ScannerContext
name|scannerContext
init|=
name|ScannerContext
operator|.
name|newBuilder
argument_list|()
operator|.
name|setBatchLimit
argument_list|(
name|compactionKVMax
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
do|do
block|{
name|hasMore
operator|=
name|scanner
operator|.
name|next
argument_list|(
name|cells
argument_list|,
name|scannerContext
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|cells
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
for|for
control|(
name|Cell
name|c
range|:
name|cells
control|)
block|{
comment|// If we know that this KV is going to be included always, then let us
comment|// set its memstoreTS to 0. This will help us save space when writing to
comment|// disk.
if|if
condition|(
name|c
operator|.
name|getValueLength
argument_list|()
operator|<=
name|mobCellValueSizeThreshold
operator|||
name|MobUtils
operator|.
name|isMobReferenceCell
argument_list|(
name|c
argument_list|)
operator|||
name|c
operator|.
name|getTypeByte
argument_list|()
operator|!=
name|KeyValue
operator|.
name|Type
operator|.
name|Put
operator|.
name|getCode
argument_list|()
condition|)
block|{
name|writer
operator|.
name|append
argument_list|(
name|c
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// append the original keyValue in the mob file.
name|mobFileWriter
operator|.
name|append
argument_list|(
name|c
argument_list|)
expr_stmt|;
name|mobSize
operator|+=
name|c
operator|.
name|getValueLength
argument_list|()
expr_stmt|;
name|mobCount
operator|++
expr_stmt|;
comment|// append the tags to the KeyValue.
comment|// The key is same, the value is the filename of the mob file
name|Cell
name|reference
init|=
name|MobUtils
operator|.
name|createMobRefCell
argument_list|(
name|c
argument_list|,
name|fileName
argument_list|,
name|this
operator|.
name|mobStore
operator|.
name|getRefCellTags
argument_list|()
argument_list|)
decl_stmt|;
name|writer
operator|.
name|append
argument_list|(
name|reference
argument_list|)
expr_stmt|;
block|}
block|}
name|cells
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
block|}
do|while
condition|(
name|hasMore
condition|)
do|;
block|}
finally|finally
block|{
name|status
operator|.
name|setStatus
argument_list|(
literal|"Flushing mob file "
operator|+
name|store
operator|+
literal|": appending metadata"
argument_list|)
expr_stmt|;
name|mobFileWriter
operator|.
name|appendMetadata
argument_list|(
name|cacheFlushId
argument_list|,
literal|false
argument_list|,
name|mobCount
argument_list|)
expr_stmt|;
name|status
operator|.
name|setStatus
argument_list|(
literal|"Flushing mob file "
operator|+
name|store
operator|+
literal|": closing flushed file"
argument_list|)
expr_stmt|;
name|mobFileWriter
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|mobCount
operator|>
literal|0
condition|)
block|{
comment|// commit the mob file from temp folder to target folder.
comment|// If the mob file is committed successfully but the store file is not,
comment|// the committed mob file will be handled by the sweep tool as an unused
comment|// file.
name|mobStore
operator|.
name|commitFile
argument_list|(
name|mobFileWriter
operator|.
name|getPath
argument_list|()
argument_list|,
name|targetPath
argument_list|)
expr_stmt|;
name|mobStore
operator|.
name|updateMobFlushCount
argument_list|()
expr_stmt|;
name|mobStore
operator|.
name|updateMobFlushedCellsCount
argument_list|(
name|mobCount
argument_list|)
expr_stmt|;
name|mobStore
operator|.
name|updateMobFlushedCellsSize
argument_list|(
name|mobSize
argument_list|)
expr_stmt|;
block|}
else|else
block|{
try|try
block|{
comment|// If the mob file is empty, delete it instead of committing.
name|store
operator|.
name|getFileSystem
argument_list|()
operator|.
name|delete
argument_list|(
name|mobFileWriter
operator|.
name|getPath
argument_list|()
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Failed to delete the temp mob file"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit

