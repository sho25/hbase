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
operator|.
name|mapreduce
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
name|HColumnDescriptor
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
name|Tag
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
name|TagType
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
name|client
operator|.
name|BufferedMutator
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
name|Put
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
name|mob
operator|.
name|MobConstants
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
name|mob
operator|.
name|MobUtils
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
name|mob
operator|.
name|compactions
operator|.
name|PartitionedMobCompactionRequest
operator|.
name|CompactionPartitionId
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
name|mob
operator|.
name|mapreduce
operator|.
name|SweepJob
operator|.
name|SweepCounter
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
name|KeyValueScanner
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
name|MemStore
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
name|StoreFile
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
name|mapreduce
operator|.
name|Reducer
operator|.
name|Context
import|;
end_import

begin_comment
comment|/**  * The wrapper of a DefaultMemStore.  * This wrapper is used in the sweep reducer to buffer and sort the cells written from  * the invalid and small mob files.  * It's flushed when it's full, the mob data are written to the mob files, and their file names  * are written back to store files of HBase.  * This memStore is used to sort the cells in mob files.  * In a reducer of sweep tool, the mob files are grouped by the same prefix (start key and date),  * in each group, the reducer iterates the files and read the cells to a new and bigger mob file.  * The cells in the same mob file are ordered, but cells across mob files are not.  * So we need this MemStoreWrapper to sort those cells come from different mob files before  * flushing them to the disk, when the memStore is big enough it's flushed as a new mob file.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|MemStoreWrapper
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
name|MemStoreWrapper
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|MemStore
name|memstore
decl_stmt|;
specifier|private
name|long
name|flushSize
decl_stmt|;
specifier|private
name|CompactionPartitionId
name|partitionId
decl_stmt|;
specifier|private
name|Context
name|context
decl_stmt|;
specifier|private
name|Configuration
name|conf
decl_stmt|;
specifier|private
name|BufferedMutator
name|table
decl_stmt|;
specifier|private
name|HColumnDescriptor
name|hcd
decl_stmt|;
specifier|private
name|Path
name|mobFamilyDir
decl_stmt|;
specifier|private
name|FileSystem
name|fs
decl_stmt|;
specifier|private
name|CacheConfig
name|cacheConfig
decl_stmt|;
specifier|private
name|Encryption
operator|.
name|Context
name|cryptoContext
init|=
name|Encryption
operator|.
name|Context
operator|.
name|NONE
decl_stmt|;
specifier|public
name|MemStoreWrapper
parameter_list|(
name|Context
name|context
parameter_list|,
name|FileSystem
name|fs
parameter_list|,
name|BufferedMutator
name|table
parameter_list|,
name|HColumnDescriptor
name|hcd
parameter_list|,
name|MemStore
name|memstore
parameter_list|,
name|CacheConfig
name|cacheConfig
parameter_list|)
throws|throws
name|IOException
block|{
name|this
operator|.
name|memstore
operator|=
name|memstore
expr_stmt|;
name|this
operator|.
name|context
operator|=
name|context
expr_stmt|;
name|this
operator|.
name|fs
operator|=
name|fs
expr_stmt|;
name|this
operator|.
name|table
operator|=
name|table
expr_stmt|;
name|this
operator|.
name|hcd
operator|=
name|hcd
expr_stmt|;
name|this
operator|.
name|conf
operator|=
name|context
operator|.
name|getConfiguration
argument_list|()
expr_stmt|;
name|this
operator|.
name|cacheConfig
operator|=
name|cacheConfig
expr_stmt|;
name|flushSize
operator|=
name|this
operator|.
name|conf
operator|.
name|getLong
argument_list|(
name|MobConstants
operator|.
name|MOB_SWEEP_TOOL_COMPACTION_MEMSTORE_FLUSH_SIZE
argument_list|,
name|MobConstants
operator|.
name|DEFAULT_MOB_SWEEP_TOOL_COMPACTION_MEMSTORE_FLUSH_SIZE
argument_list|)
expr_stmt|;
name|mobFamilyDir
operator|=
name|MobUtils
operator|.
name|getMobFamilyPath
argument_list|(
name|conf
argument_list|,
name|table
operator|.
name|getName
argument_list|()
argument_list|,
name|hcd
operator|.
name|getNameAsString
argument_list|()
argument_list|)
expr_stmt|;
name|cryptoContext
operator|=
name|MobUtils
operator|.
name|createEncryptionContext
argument_list|(
name|conf
argument_list|,
name|hcd
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|setPartitionId
parameter_list|(
name|CompactionPartitionId
name|partitionId
parameter_list|)
block|{
name|this
operator|.
name|partitionId
operator|=
name|partitionId
expr_stmt|;
block|}
comment|/**    * Flushes the memstore if the size is large enough.    * @throws IOException    */
specifier|private
name|void
name|flushMemStoreIfNecessary
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|memstore
operator|.
name|heapSize
argument_list|()
operator|>=
name|flushSize
condition|)
block|{
name|flushMemStore
argument_list|()
expr_stmt|;
block|}
block|}
comment|/**    * Flushes the memstore anyway.    * @throws IOException    */
specifier|public
name|void
name|flushMemStore
parameter_list|()
throws|throws
name|IOException
block|{
name|MemStoreSnapshot
name|snapshot
init|=
name|memstore
operator|.
name|snapshot
argument_list|()
decl_stmt|;
name|internalFlushCache
argument_list|(
name|snapshot
argument_list|)
expr_stmt|;
name|memstore
operator|.
name|clearSnapshot
argument_list|(
name|snapshot
operator|.
name|getId
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**    * Flushes the snapshot of the memstore.    * Flushes the mob data to the mob files, and flushes the name of these mob files to HBase.    * @param snapshot The snapshot of the memstore.    * @throws IOException    */
specifier|private
name|void
name|internalFlushCache
parameter_list|(
specifier|final
name|MemStoreSnapshot
name|snapshot
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|snapshot
operator|.
name|getCellsCount
argument_list|()
operator|==
literal|0
condition|)
block|{
return|return;
block|}
comment|// generate the files into a temp directory.
name|String
name|tempPathString
init|=
name|context
operator|.
name|getConfiguration
argument_list|()
operator|.
name|get
argument_list|(
name|SweepJob
operator|.
name|WORKING_FILES_DIR_KEY
argument_list|)
decl_stmt|;
name|StoreFile
operator|.
name|Writer
name|mobFileWriter
init|=
name|MobUtils
operator|.
name|createWriter
argument_list|(
name|conf
argument_list|,
name|fs
argument_list|,
name|hcd
argument_list|,
name|partitionId
operator|.
name|getDate
argument_list|()
argument_list|,
operator|new
name|Path
argument_list|(
name|tempPathString
argument_list|)
argument_list|,
name|snapshot
operator|.
name|getCellsCount
argument_list|()
argument_list|,
name|hcd
operator|.
name|getCompactionCompression
argument_list|()
argument_list|,
name|partitionId
operator|.
name|getStartKey
argument_list|()
argument_list|,
name|cacheConfig
argument_list|,
name|cryptoContext
argument_list|)
decl_stmt|;
name|String
name|relativePath
init|=
name|mobFileWriter
operator|.
name|getPath
argument_list|()
operator|.
name|getName
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Create files under a temp directory "
operator|+
name|mobFileWriter
operator|.
name|getPath
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|byte
index|[]
name|referenceValue
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|relativePath
argument_list|)
decl_stmt|;
name|KeyValueScanner
name|scanner
init|=
name|snapshot
operator|.
name|getScanner
argument_list|()
decl_stmt|;
name|Cell
name|cell
init|=
literal|null
decl_stmt|;
while|while
condition|(
literal|null
operator|!=
operator|(
name|cell
operator|=
name|scanner
operator|.
name|next
argument_list|()
operator|)
condition|)
block|{
name|mobFileWriter
operator|.
name|append
argument_list|(
name|cell
argument_list|)
expr_stmt|;
block|}
name|scanner
operator|.
name|close
argument_list|()
expr_stmt|;
comment|// Write out the log sequence number that corresponds to this output
comment|// hfile. The hfile is current up to and including logCacheFlushId.
name|mobFileWriter
operator|.
name|appendMetadata
argument_list|(
name|Long
operator|.
name|MAX_VALUE
argument_list|,
literal|false
argument_list|,
name|snapshot
operator|.
name|getCellsCount
argument_list|()
argument_list|)
expr_stmt|;
name|mobFileWriter
operator|.
name|close
argument_list|()
expr_stmt|;
name|MobUtils
operator|.
name|commitFile
argument_list|(
name|conf
argument_list|,
name|fs
argument_list|,
name|mobFileWriter
operator|.
name|getPath
argument_list|()
argument_list|,
name|mobFamilyDir
argument_list|,
name|cacheConfig
argument_list|)
expr_stmt|;
name|context
operator|.
name|getCounter
argument_list|(
name|SweepCounter
operator|.
name|FILE_AFTER_MERGE_OR_CLEAN
argument_list|)
operator|.
name|increment
argument_list|(
literal|1
argument_list|)
expr_stmt|;
comment|// write reference/fileName back to the store files of HBase.
name|scanner
operator|=
name|snapshot
operator|.
name|getScanner
argument_list|()
expr_stmt|;
name|scanner
operator|.
name|seek
argument_list|(
name|KeyValueUtil
operator|.
name|createFirstOnRow
argument_list|(
name|HConstants
operator|.
name|EMPTY_START_ROW
argument_list|)
argument_list|)
expr_stmt|;
name|cell
operator|=
literal|null
expr_stmt|;
name|Tag
name|tableNameTag
init|=
operator|new
name|Tag
argument_list|(
name|TagType
operator|.
name|MOB_TABLE_NAME_TAG_TYPE
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|this
operator|.
name|table
operator|.
name|getName
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|long
name|updatedCount
init|=
literal|0
decl_stmt|;
while|while
condition|(
literal|null
operator|!=
operator|(
name|cell
operator|=
name|scanner
operator|.
name|next
argument_list|()
operator|)
condition|)
block|{
name|KeyValue
name|reference
init|=
name|MobUtils
operator|.
name|createMobRefKeyValue
argument_list|(
name|cell
argument_list|,
name|referenceValue
argument_list|,
name|tableNameTag
argument_list|)
decl_stmt|;
name|Put
name|put
init|=
operator|new
name|Put
argument_list|(
name|reference
operator|.
name|getRowArray
argument_list|()
argument_list|,
name|reference
operator|.
name|getRowOffset
argument_list|()
argument_list|,
name|reference
operator|.
name|getRowLength
argument_list|()
argument_list|)
decl_stmt|;
name|put
operator|.
name|add
argument_list|(
name|reference
argument_list|)
expr_stmt|;
name|table
operator|.
name|mutate
argument_list|(
name|put
argument_list|)
expr_stmt|;
name|updatedCount
operator|++
expr_stmt|;
block|}
name|table
operator|.
name|flush
argument_list|()
expr_stmt|;
name|context
operator|.
name|getCounter
argument_list|(
name|SweepCounter
operator|.
name|RECORDS_UPDATED
argument_list|)
operator|.
name|increment
argument_list|(
name|updatedCount
argument_list|)
expr_stmt|;
name|scanner
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
comment|/**    * Adds a KeyValue into the memstore.    * @param kv The KeyValue to be added.    * @throws IOException    */
specifier|public
name|void
name|addToMemstore
parameter_list|(
name|KeyValue
name|kv
parameter_list|)
throws|throws
name|IOException
block|{
name|memstore
operator|.
name|add
argument_list|(
name|kv
argument_list|)
expr_stmt|;
comment|// flush the memstore if it's full.
name|flushMemStoreIfNecessary
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

