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
name|io
operator|.
name|InterruptedIOException
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
name|Collection
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
name|conf
operator|.
name|Configured
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
name|client
operator|.
name|Scan
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
name|regionserver
operator|.
name|compactions
operator|.
name|CompactionProgress
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
comment|/**  * Compact passed set of files.  * Create an instance and then call {@ink #compact(Store, Collection, boolean, long)}.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
class|class
name|Compactor
extends|extends
name|Configured
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
name|Compactor
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|CompactionProgress
name|progress
decl_stmt|;
name|Compactor
parameter_list|(
specifier|final
name|Configuration
name|c
parameter_list|)
block|{
name|super
argument_list|(
name|c
argument_list|)
expr_stmt|;
block|}
comment|/**    * Do a minor/major compaction on an explicit set of storefiles from a Store.    *    * @param store Store the files belong to    * @param filesToCompact which files to compact    * @param majorCompaction true to major compact (prune all deletes, max versions, etc)    * @param maxId Readers maximum sequence id.    * @return Product of compaction or null if all cells expired or deleted and    * nothing made it through the compaction.    * @throws IOException    */
name|StoreFile
operator|.
name|Writer
name|compact
parameter_list|(
specifier|final
name|Store
name|store
parameter_list|,
specifier|final
name|Collection
argument_list|<
name|StoreFile
argument_list|>
name|filesToCompact
parameter_list|,
specifier|final
name|boolean
name|majorCompaction
parameter_list|,
specifier|final
name|long
name|maxId
parameter_list|)
throws|throws
name|IOException
block|{
comment|// Calculate maximum key count after compaction (for blooms)
comment|// Also calculate earliest put timestamp if major compaction
name|int
name|maxKeyCount
init|=
literal|0
decl_stmt|;
name|long
name|earliestPutTs
init|=
name|HConstants
operator|.
name|LATEST_TIMESTAMP
decl_stmt|;
for|for
control|(
name|StoreFile
name|file
range|:
name|filesToCompact
control|)
block|{
name|StoreFile
operator|.
name|Reader
name|r
init|=
name|file
operator|.
name|getReader
argument_list|()
decl_stmt|;
if|if
condition|(
name|r
operator|==
literal|null
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Null reader for "
operator|+
name|file
operator|.
name|getPath
argument_list|()
argument_list|)
expr_stmt|;
continue|continue;
block|}
comment|// NOTE: getFilterEntries could cause under-sized blooms if the user
comment|// switches bloom type (e.g. from ROW to ROWCOL)
name|long
name|keyCount
init|=
operator|(
name|r
operator|.
name|getBloomFilterType
argument_list|()
operator|==
name|store
operator|.
name|getFamily
argument_list|()
operator|.
name|getBloomFilterType
argument_list|()
operator|)
condition|?
name|r
operator|.
name|getFilterEntries
argument_list|()
else|:
name|r
operator|.
name|getEntries
argument_list|()
decl_stmt|;
name|maxKeyCount
operator|+=
name|keyCount
expr_stmt|;
comment|// For major compactions calculate the earliest put timestamp of all
comment|// involved storefiles. This is used to remove family delete marker during
comment|// compaction.
if|if
condition|(
name|majorCompaction
condition|)
block|{
name|byte
index|[]
name|tmp
init|=
name|r
operator|.
name|loadFileInfo
argument_list|()
operator|.
name|get
argument_list|(
name|StoreFile
operator|.
name|EARLIEST_PUT_TS
argument_list|)
decl_stmt|;
if|if
condition|(
name|tmp
operator|==
literal|null
condition|)
block|{
comment|// There's a file with no information, must be an old one
comment|// assume we have very old puts
name|earliestPutTs
operator|=
name|HConstants
operator|.
name|OLDEST_TIMESTAMP
expr_stmt|;
block|}
else|else
block|{
name|earliestPutTs
operator|=
name|Math
operator|.
name|min
argument_list|(
name|earliestPutTs
argument_list|,
name|Bytes
operator|.
name|toLong
argument_list|(
name|tmp
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
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
literal|"Compacting "
operator|+
name|file
operator|+
literal|", keycount="
operator|+
name|keyCount
operator|+
literal|", bloomtype="
operator|+
name|r
operator|.
name|getBloomFilterType
argument_list|()
operator|.
name|toString
argument_list|()
operator|+
literal|", size="
operator|+
name|StringUtils
operator|.
name|humanReadableInt
argument_list|(
name|r
operator|.
name|length
argument_list|()
argument_list|)
operator|+
literal|", encoding="
operator|+
name|r
operator|.
name|getHFileReader
argument_list|()
operator|.
name|getEncodingOnDisk
argument_list|()
operator|+
operator|(
name|majorCompaction
condition|?
literal|", earliestPutTs="
operator|+
name|earliestPutTs
else|:
literal|""
operator|)
argument_list|)
expr_stmt|;
block|}
block|}
comment|// keep track of compaction progress
name|this
operator|.
name|progress
operator|=
operator|new
name|CompactionProgress
argument_list|(
name|maxKeyCount
argument_list|)
expr_stmt|;
comment|// For each file, obtain a scanner:
name|List
argument_list|<
name|StoreFileScanner
argument_list|>
name|scanners
init|=
name|StoreFileScanner
operator|.
name|getScannersForStoreFiles
argument_list|(
name|filesToCompact
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|,
literal|true
argument_list|)
decl_stmt|;
comment|// Get some configs
name|int
name|compactionKVMax
init|=
name|getConf
argument_list|()
operator|.
name|getInt
argument_list|(
name|HConstants
operator|.
name|COMPACTION_KV_MAX
argument_list|,
literal|10
argument_list|)
decl_stmt|;
name|Compression
operator|.
name|Algorithm
name|compression
init|=
name|store
operator|.
name|getFamily
argument_list|()
operator|.
name|getCompression
argument_list|()
decl_stmt|;
comment|// Avoid overriding compression setting for major compactions if the user
comment|// has not specified it separately
name|Compression
operator|.
name|Algorithm
name|compactionCompression
init|=
operator|(
name|store
operator|.
name|getFamily
argument_list|()
operator|.
name|getCompactionCompression
argument_list|()
operator|!=
name|Compression
operator|.
name|Algorithm
operator|.
name|NONE
operator|)
condition|?
name|store
operator|.
name|getFamily
argument_list|()
operator|.
name|getCompactionCompression
argument_list|()
else|:
name|compression
decl_stmt|;
comment|// Make the instantiation lazy in case compaction produces no product; i.e.
comment|// where all source cells are expired or deleted.
name|StoreFile
operator|.
name|Writer
name|writer
init|=
literal|null
decl_stmt|;
comment|// Find the smallest read point across all the Scanners.
name|long
name|smallestReadPoint
init|=
name|store
operator|.
name|getHRegion
argument_list|()
operator|.
name|getSmallestReadPoint
argument_list|()
decl_stmt|;
name|MultiVersionConsistencyControl
operator|.
name|setThreadReadPoint
argument_list|(
name|smallestReadPoint
argument_list|)
expr_stmt|;
try|try
block|{
name|InternalScanner
name|scanner
init|=
literal|null
decl_stmt|;
try|try
block|{
if|if
condition|(
name|store
operator|.
name|getHRegion
argument_list|()
operator|.
name|getCoprocessorHost
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|scanner
operator|=
name|store
operator|.
name|getHRegion
argument_list|()
operator|.
name|getCoprocessorHost
argument_list|()
operator|.
name|preCompactScannerOpen
argument_list|(
name|store
argument_list|,
name|scanners
argument_list|,
name|majorCompaction
condition|?
name|ScanType
operator|.
name|MAJOR_COMPACT
else|:
name|ScanType
operator|.
name|MINOR_COMPACT
argument_list|,
name|earliestPutTs
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|scanner
operator|==
literal|null
condition|)
block|{
name|Scan
name|scan
init|=
operator|new
name|Scan
argument_list|()
decl_stmt|;
name|scan
operator|.
name|setMaxVersions
argument_list|(
name|store
operator|.
name|getFamily
argument_list|()
operator|.
name|getMaxVersions
argument_list|()
argument_list|)
expr_stmt|;
comment|/* Include deletes, unless we are doing a major compaction */
name|scanner
operator|=
operator|new
name|StoreScanner
argument_list|(
name|store
argument_list|,
name|store
operator|.
name|scanInfo
argument_list|,
name|scan
argument_list|,
name|scanners
argument_list|,
name|majorCompaction
condition|?
name|ScanType
operator|.
name|MAJOR_COMPACT
else|:
name|ScanType
operator|.
name|MINOR_COMPACT
argument_list|,
name|smallestReadPoint
argument_list|,
name|earliestPutTs
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|store
operator|.
name|getHRegion
argument_list|()
operator|.
name|getCoprocessorHost
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|InternalScanner
name|cpScanner
init|=
name|store
operator|.
name|getHRegion
argument_list|()
operator|.
name|getCoprocessorHost
argument_list|()
operator|.
name|preCompact
argument_list|(
name|store
argument_list|,
name|scanner
argument_list|)
decl_stmt|;
comment|// NULL scanner returned from coprocessor hooks means skip normal processing
if|if
condition|(
name|cpScanner
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
name|scanner
operator|=
name|cpScanner
expr_stmt|;
block|}
name|int
name|bytesWritten
init|=
literal|0
decl_stmt|;
comment|// Since scanner.next() can return 'false' but still be delivering data,
comment|// we have to use a do/while loop.
name|List
argument_list|<
name|KeyValue
argument_list|>
name|kvs
init|=
operator|new
name|ArrayList
argument_list|<
name|KeyValue
argument_list|>
argument_list|()
decl_stmt|;
comment|// Limit to "hbase.hstore.compaction.kv.max" (default 10) to avoid OOME
name|boolean
name|hasMore
decl_stmt|;
do|do
block|{
name|hasMore
operator|=
name|scanner
operator|.
name|next
argument_list|(
name|kvs
argument_list|,
name|compactionKVMax
argument_list|)
expr_stmt|;
comment|// Create the writer even if no kv(Empty store file is also ok),
comment|// because we need record the max seq id for the store file, see
comment|// HBASE-6059
if|if
condition|(
name|writer
operator|==
literal|null
condition|)
block|{
name|writer
operator|=
name|store
operator|.
name|createWriterInTmp
argument_list|(
name|maxKeyCount
argument_list|,
name|compactionCompression
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|writer
operator|!=
literal|null
condition|)
block|{
comment|// output to writer:
for|for
control|(
name|KeyValue
name|kv
range|:
name|kvs
control|)
block|{
if|if
condition|(
name|kv
operator|.
name|getMemstoreTS
argument_list|()
operator|<=
name|smallestReadPoint
condition|)
block|{
name|kv
operator|.
name|setMemstoreTS
argument_list|(
literal|0
argument_list|)
expr_stmt|;
block|}
name|writer
operator|.
name|append
argument_list|(
name|kv
argument_list|)
expr_stmt|;
comment|// update progress per key
operator|++
name|progress
operator|.
name|currentCompactedKVs
expr_stmt|;
comment|// check periodically to see if a system stop is requested
if|if
condition|(
name|Store
operator|.
name|closeCheckInterval
operator|>
literal|0
condition|)
block|{
name|bytesWritten
operator|+=
name|kv
operator|.
name|getLength
argument_list|()
expr_stmt|;
if|if
condition|(
name|bytesWritten
operator|>
name|Store
operator|.
name|closeCheckInterval
condition|)
block|{
name|bytesWritten
operator|=
literal|0
expr_stmt|;
name|isInterrupted
argument_list|(
name|store
argument_list|,
name|writer
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
name|kvs
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
do|while
condition|(
name|hasMore
condition|)
do|;
block|}
finally|finally
block|{
if|if
condition|(
name|scanner
operator|!=
literal|null
condition|)
block|{
name|scanner
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
block|}
finally|finally
block|{
if|if
condition|(
name|writer
operator|!=
literal|null
condition|)
block|{
name|writer
operator|.
name|appendMetadata
argument_list|(
name|maxId
argument_list|,
name|majorCompaction
argument_list|)
expr_stmt|;
name|writer
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
return|return
name|writer
return|;
block|}
name|void
name|isInterrupted
parameter_list|(
specifier|final
name|Store
name|store
parameter_list|,
specifier|final
name|StoreFile
operator|.
name|Writer
name|writer
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|store
operator|.
name|getHRegion
argument_list|()
operator|.
name|areWritesEnabled
argument_list|()
condition|)
return|return;
comment|// Else cleanup.
name|writer
operator|.
name|close
argument_list|()
expr_stmt|;
name|store
operator|.
name|getFileSystem
argument_list|()
operator|.
name|delete
argument_list|(
name|writer
operator|.
name|getPath
argument_list|()
argument_list|,
literal|false
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|InterruptedIOException
argument_list|(
literal|"Aborting compaction of store "
operator|+
name|store
operator|+
literal|" in region "
operator|+
name|store
operator|.
name|getHRegion
argument_list|()
operator|+
literal|" because it was interrupted."
argument_list|)
throw|;
block|}
name|CompactionProgress
name|getProgress
parameter_list|()
block|{
return|return
name|this
operator|.
name|progress
return|;
block|}
block|}
end_class

end_unit

