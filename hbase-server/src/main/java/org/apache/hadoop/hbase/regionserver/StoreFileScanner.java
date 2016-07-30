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
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|FileNotFoundException
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
name|Collection
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collections
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
name|CellUtil
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
name|TimeRange
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
name|HFileScanner
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
name|querymatcher
operator|.
name|ScanQueryMatcher
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
name|Counter
import|;
end_import

begin_comment
comment|/**  * KeyValueScanner adaptor over the Reader.  It also provides hooks into  * bloom filter things.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|LimitedPrivate
argument_list|(
literal|"Coprocessor"
argument_list|)
specifier|public
class|class
name|StoreFileScanner
implements|implements
name|KeyValueScanner
block|{
comment|// the reader it comes from:
specifier|private
specifier|final
name|StoreFileReader
name|reader
decl_stmt|;
specifier|private
specifier|final
name|HFileScanner
name|hfs
decl_stmt|;
specifier|private
name|Cell
name|cur
init|=
literal|null
decl_stmt|;
specifier|private
name|boolean
name|closed
init|=
literal|false
decl_stmt|;
specifier|private
name|boolean
name|realSeekDone
decl_stmt|;
specifier|private
name|boolean
name|delayedReseek
decl_stmt|;
specifier|private
name|Cell
name|delayedSeekKV
decl_stmt|;
specifier|private
specifier|final
name|boolean
name|enforceMVCC
decl_stmt|;
specifier|private
specifier|final
name|boolean
name|hasMVCCInfo
decl_stmt|;
comment|// A flag represents whether could stop skipping KeyValues for MVCC
comment|// if have encountered the next row. Only used for reversed scan
specifier|private
name|boolean
name|stopSkippingKVsIfNextRow
init|=
literal|false
decl_stmt|;
specifier|private
specifier|static
name|Counter
name|seekCount
decl_stmt|;
specifier|private
specifier|final
name|boolean
name|canOptimizeForNonNullColumn
decl_stmt|;
specifier|private
specifier|final
name|long
name|readPt
decl_stmt|;
comment|// Order of this scanner relative to other scanners when duplicate key-value is found.
comment|// Higher values means scanner has newer data.
specifier|private
specifier|final
name|long
name|scannerOrder
decl_stmt|;
comment|/**    * Implements a {@link KeyValueScanner} on top of the specified {@link HFileScanner}    * @param useMVCC If true, scanner will filter out updates with MVCC larger than {@code readPt}.    * @param readPt MVCC value to use to filter out the updates newer than this scanner.    * @param hasMVCC Set to true if underlying store file reader has MVCC info.    * @param scannerOrder Order of the scanner relative to other scanners. See    *          {@link KeyValueScanner#getScannerOrder()}.    * @param canOptimizeForNonNullColumn {@code true} if we can make sure there is no null column,    *          otherwise {@code false}. This is a hint for optimization.    */
specifier|public
name|StoreFileScanner
parameter_list|(
name|StoreFileReader
name|reader
parameter_list|,
name|HFileScanner
name|hfs
parameter_list|,
name|boolean
name|useMVCC
parameter_list|,
name|boolean
name|hasMVCC
parameter_list|,
name|long
name|readPt
parameter_list|,
name|long
name|scannerOrder
parameter_list|,
name|boolean
name|canOptimizeForNonNullColumn
parameter_list|)
block|{
name|this
operator|.
name|readPt
operator|=
name|readPt
expr_stmt|;
name|this
operator|.
name|reader
operator|=
name|reader
expr_stmt|;
name|this
operator|.
name|hfs
operator|=
name|hfs
expr_stmt|;
name|this
operator|.
name|enforceMVCC
operator|=
name|useMVCC
expr_stmt|;
name|this
operator|.
name|hasMVCCInfo
operator|=
name|hasMVCC
expr_stmt|;
name|this
operator|.
name|scannerOrder
operator|=
name|scannerOrder
expr_stmt|;
name|this
operator|.
name|canOptimizeForNonNullColumn
operator|=
name|canOptimizeForNonNullColumn
expr_stmt|;
block|}
name|boolean
name|isPrimaryReplica
parameter_list|()
block|{
return|return
name|reader
operator|.
name|isPrimaryReplicaReader
argument_list|()
return|;
block|}
comment|/**    * Return an array of scanners corresponding to the given    * set of store files.    */
specifier|public
specifier|static
name|List
argument_list|<
name|StoreFileScanner
argument_list|>
name|getScannersForStoreFiles
parameter_list|(
name|Collection
argument_list|<
name|StoreFile
argument_list|>
name|files
parameter_list|,
name|boolean
name|cacheBlocks
parameter_list|,
name|boolean
name|usePread
parameter_list|,
name|long
name|readPt
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|getScannersForStoreFiles
argument_list|(
name|files
argument_list|,
name|cacheBlocks
argument_list|,
name|usePread
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|,
name|readPt
argument_list|)
return|;
block|}
comment|/**    * Return an array of scanners corresponding to the given set of store files.    */
specifier|public
specifier|static
name|List
argument_list|<
name|StoreFileScanner
argument_list|>
name|getScannersForStoreFiles
parameter_list|(
name|Collection
argument_list|<
name|StoreFile
argument_list|>
name|files
parameter_list|,
name|boolean
name|cacheBlocks
parameter_list|,
name|boolean
name|usePread
parameter_list|,
name|boolean
name|isCompaction
parameter_list|,
name|boolean
name|useDropBehind
parameter_list|,
name|long
name|readPt
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|getScannersForStoreFiles
argument_list|(
name|files
argument_list|,
name|cacheBlocks
argument_list|,
name|usePread
argument_list|,
name|isCompaction
argument_list|,
name|useDropBehind
argument_list|,
literal|null
argument_list|,
name|readPt
argument_list|)
return|;
block|}
comment|/**    * Return an array of scanners corresponding to the given set of store files, And set the    * ScanQueryMatcher for each store file scanner for further optimization    */
specifier|public
specifier|static
name|List
argument_list|<
name|StoreFileScanner
argument_list|>
name|getScannersForStoreFiles
parameter_list|(
name|Collection
argument_list|<
name|StoreFile
argument_list|>
name|files
parameter_list|,
name|boolean
name|cacheBlocks
parameter_list|,
name|boolean
name|usePread
parameter_list|,
name|boolean
name|isCompaction
parameter_list|,
name|boolean
name|canUseDrop
parameter_list|,
name|ScanQueryMatcher
name|matcher
parameter_list|,
name|long
name|readPt
parameter_list|,
name|boolean
name|isPrimaryReplica
parameter_list|)
throws|throws
name|IOException
block|{
name|List
argument_list|<
name|StoreFileScanner
argument_list|>
name|scanners
init|=
operator|new
name|ArrayList
argument_list|<
name|StoreFileScanner
argument_list|>
argument_list|(
name|files
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|StoreFile
argument_list|>
name|sorted_files
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|files
argument_list|)
decl_stmt|;
name|Collections
operator|.
name|sort
argument_list|(
name|sorted_files
argument_list|,
name|StoreFile
operator|.
name|Comparators
operator|.
name|SEQ_ID
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|sorted_files
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|StoreFileReader
name|r
init|=
name|sorted_files
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|createReader
argument_list|()
decl_stmt|;
name|r
operator|.
name|setReplicaStoreFile
argument_list|(
name|isPrimaryReplica
argument_list|)
expr_stmt|;
name|StoreFileScanner
name|scanner
init|=
name|r
operator|.
name|getStoreFileScanner
argument_list|(
name|cacheBlocks
argument_list|,
name|usePread
argument_list|,
name|isCompaction
argument_list|,
name|readPt
argument_list|,
name|i
argument_list|,
name|matcher
operator|!=
literal|null
condition|?
operator|!
name|matcher
operator|.
name|hasNullColumnInQuery
argument_list|()
else|:
literal|false
argument_list|)
decl_stmt|;
name|scanners
operator|.
name|add
argument_list|(
name|scanner
argument_list|)
expr_stmt|;
block|}
return|return
name|scanners
return|;
block|}
specifier|public
specifier|static
name|List
argument_list|<
name|StoreFileScanner
argument_list|>
name|getScannersForStoreFiles
parameter_list|(
name|Collection
argument_list|<
name|StoreFile
argument_list|>
name|files
parameter_list|,
name|boolean
name|cacheBlocks
parameter_list|,
name|boolean
name|usePread
parameter_list|,
name|boolean
name|isCompaction
parameter_list|,
name|boolean
name|canUseDrop
parameter_list|,
name|ScanQueryMatcher
name|matcher
parameter_list|,
name|long
name|readPt
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|getScannersForStoreFiles
argument_list|(
name|files
argument_list|,
name|cacheBlocks
argument_list|,
name|usePread
argument_list|,
name|isCompaction
argument_list|,
name|canUseDrop
argument_list|,
name|matcher
argument_list|,
name|readPt
argument_list|,
literal|true
argument_list|)
return|;
block|}
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
literal|"StoreFileScanner["
operator|+
name|hfs
operator|.
name|toString
argument_list|()
operator|+
literal|", cur="
operator|+
name|cur
operator|+
literal|"]"
return|;
block|}
specifier|public
name|Cell
name|peek
parameter_list|()
block|{
return|return
name|cur
return|;
block|}
specifier|public
name|Cell
name|next
parameter_list|()
throws|throws
name|IOException
block|{
name|Cell
name|retKey
init|=
name|cur
decl_stmt|;
try|try
block|{
comment|// only seek if we aren't at the end. cur == null implies 'end'.
if|if
condition|(
name|cur
operator|!=
literal|null
condition|)
block|{
name|hfs
operator|.
name|next
argument_list|()
expr_stmt|;
name|setCurrentCell
argument_list|(
name|hfs
operator|.
name|getCell
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|hasMVCCInfo
operator|||
name|this
operator|.
name|reader
operator|.
name|isBulkLoaded
argument_list|()
condition|)
block|{
name|skipKVsNewerThanReadpoint
argument_list|()
expr_stmt|;
block|}
block|}
block|}
catch|catch
parameter_list|(
name|FileNotFoundException
name|e
parameter_list|)
block|{
throw|throw
name|e
throw|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Could not iterate "
operator|+
name|this
argument_list|,
name|e
argument_list|)
throw|;
block|}
return|return
name|retKey
return|;
block|}
specifier|public
name|boolean
name|seek
parameter_list|(
name|Cell
name|key
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|seekCount
operator|!=
literal|null
condition|)
name|seekCount
operator|.
name|increment
argument_list|()
expr_stmt|;
try|try
block|{
try|try
block|{
if|if
condition|(
operator|!
name|seekAtOrAfter
argument_list|(
name|hfs
argument_list|,
name|key
argument_list|)
condition|)
block|{
name|this
operator|.
name|cur
operator|=
literal|null
expr_stmt|;
return|return
literal|false
return|;
block|}
name|setCurrentCell
argument_list|(
name|hfs
operator|.
name|getCell
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|hasMVCCInfo
operator|&&
name|this
operator|.
name|reader
operator|.
name|isBulkLoaded
argument_list|()
condition|)
block|{
return|return
name|skipKVsNewerThanReadpoint
argument_list|()
return|;
block|}
else|else
block|{
return|return
operator|!
name|hasMVCCInfo
condition|?
literal|true
else|:
name|skipKVsNewerThanReadpoint
argument_list|()
return|;
block|}
block|}
finally|finally
block|{
name|realSeekDone
operator|=
literal|true
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|FileNotFoundException
name|e
parameter_list|)
block|{
throw|throw
name|e
throw|;
block|}
catch|catch
parameter_list|(
name|IOException
name|ioe
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Could not seek "
operator|+
name|this
operator|+
literal|" to key "
operator|+
name|key
argument_list|,
name|ioe
argument_list|)
throw|;
block|}
block|}
specifier|public
name|boolean
name|reseek
parameter_list|(
name|Cell
name|key
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|seekCount
operator|!=
literal|null
condition|)
name|seekCount
operator|.
name|increment
argument_list|()
expr_stmt|;
try|try
block|{
try|try
block|{
if|if
condition|(
operator|!
name|reseekAtOrAfter
argument_list|(
name|hfs
argument_list|,
name|key
argument_list|)
condition|)
block|{
name|this
operator|.
name|cur
operator|=
literal|null
expr_stmt|;
return|return
literal|false
return|;
block|}
name|setCurrentCell
argument_list|(
name|hfs
operator|.
name|getCell
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|hasMVCCInfo
operator|&&
name|this
operator|.
name|reader
operator|.
name|isBulkLoaded
argument_list|()
condition|)
block|{
return|return
name|skipKVsNewerThanReadpoint
argument_list|()
return|;
block|}
else|else
block|{
return|return
operator|!
name|hasMVCCInfo
condition|?
literal|true
else|:
name|skipKVsNewerThanReadpoint
argument_list|()
return|;
block|}
block|}
finally|finally
block|{
name|realSeekDone
operator|=
literal|true
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|FileNotFoundException
name|e
parameter_list|)
block|{
throw|throw
name|e
throw|;
block|}
catch|catch
parameter_list|(
name|IOException
name|ioe
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Could not reseek "
operator|+
name|this
operator|+
literal|" to key "
operator|+
name|key
argument_list|,
name|ioe
argument_list|)
throw|;
block|}
block|}
specifier|protected
name|void
name|setCurrentCell
parameter_list|(
name|Cell
name|newVal
parameter_list|)
throws|throws
name|IOException
block|{
name|this
operator|.
name|cur
operator|=
name|newVal
expr_stmt|;
if|if
condition|(
name|this
operator|.
name|cur
operator|!=
literal|null
operator|&&
name|this
operator|.
name|reader
operator|.
name|isBulkLoaded
argument_list|()
operator|&&
operator|!
name|this
operator|.
name|reader
operator|.
name|isSkipResetSeqId
argument_list|()
condition|)
block|{
name|CellUtil
operator|.
name|setSequenceId
argument_list|(
name|cur
argument_list|,
name|this
operator|.
name|reader
operator|.
name|getSequenceID
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
specifier|protected
name|boolean
name|skipKVsNewerThanReadpoint
parameter_list|()
throws|throws
name|IOException
block|{
comment|// We want to ignore all key-values that are newer than our current
comment|// readPoint
name|Cell
name|startKV
init|=
name|cur
decl_stmt|;
while|while
condition|(
name|enforceMVCC
operator|&&
name|cur
operator|!=
literal|null
operator|&&
operator|(
name|cur
operator|.
name|getSequenceId
argument_list|()
operator|>
name|readPt
operator|)
condition|)
block|{
name|boolean
name|hasNext
init|=
name|hfs
operator|.
name|next
argument_list|()
decl_stmt|;
name|setCurrentCell
argument_list|(
name|hfs
operator|.
name|getCell
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|hasNext
operator|&&
name|this
operator|.
name|stopSkippingKVsIfNextRow
operator|&&
name|getComparator
argument_list|()
operator|.
name|compareRows
argument_list|(
name|cur
argument_list|,
name|startKV
argument_list|)
operator|>
literal|0
condition|)
block|{
return|return
literal|false
return|;
block|}
block|}
if|if
condition|(
name|cur
operator|==
literal|null
condition|)
block|{
return|return
literal|false
return|;
block|}
return|return
literal|true
return|;
block|}
specifier|public
name|void
name|close
parameter_list|()
block|{
if|if
condition|(
name|closed
condition|)
return|return;
name|cur
operator|=
literal|null
expr_stmt|;
name|this
operator|.
name|hfs
operator|.
name|close
argument_list|()
expr_stmt|;
if|if
condition|(
name|this
operator|.
name|reader
operator|!=
literal|null
condition|)
block|{
name|this
operator|.
name|reader
operator|.
name|decrementRefCount
argument_list|()
expr_stmt|;
block|}
name|closed
operator|=
literal|true
expr_stmt|;
block|}
comment|/**    *    * @param s    * @param k    * @return false if not found or if k is after the end.    * @throws IOException    */
specifier|public
specifier|static
name|boolean
name|seekAtOrAfter
parameter_list|(
name|HFileScanner
name|s
parameter_list|,
name|Cell
name|k
parameter_list|)
throws|throws
name|IOException
block|{
name|int
name|result
init|=
name|s
operator|.
name|seekTo
argument_list|(
name|k
argument_list|)
decl_stmt|;
if|if
condition|(
name|result
operator|<
literal|0
condition|)
block|{
if|if
condition|(
name|result
operator|==
name|HConstants
operator|.
name|INDEX_KEY_MAGIC
condition|)
block|{
comment|// using faked key
return|return
literal|true
return|;
block|}
comment|// Passed KV is smaller than first KV in file, work from start of file
return|return
name|s
operator|.
name|seekTo
argument_list|()
return|;
block|}
elseif|else
if|if
condition|(
name|result
operator|>
literal|0
condition|)
block|{
comment|// Passed KV is larger than current KV in file, if there is a next
comment|// it is the "after", if not then this scanner is done.
return|return
name|s
operator|.
name|next
argument_list|()
return|;
block|}
comment|// Seeked to the exact key
return|return
literal|true
return|;
block|}
specifier|static
name|boolean
name|reseekAtOrAfter
parameter_list|(
name|HFileScanner
name|s
parameter_list|,
name|Cell
name|k
parameter_list|)
throws|throws
name|IOException
block|{
comment|//This function is similar to seekAtOrAfter function
name|int
name|result
init|=
name|s
operator|.
name|reseekTo
argument_list|(
name|k
argument_list|)
decl_stmt|;
if|if
condition|(
name|result
operator|<=
literal|0
condition|)
block|{
if|if
condition|(
name|result
operator|==
name|HConstants
operator|.
name|INDEX_KEY_MAGIC
condition|)
block|{
comment|// using faked key
return|return
literal|true
return|;
block|}
comment|// If up to now scanner is not seeked yet, this means passed KV is smaller
comment|// than first KV in file, and it is the first time we seek on this file.
comment|// So we also need to work from the start of file.
if|if
condition|(
operator|!
name|s
operator|.
name|isSeeked
argument_list|()
condition|)
block|{
return|return
name|s
operator|.
name|seekTo
argument_list|()
return|;
block|}
return|return
literal|true
return|;
block|}
comment|// passed KV is larger than current KV in file, if there is a next
comment|// it is after, if not then this scanner is done.
return|return
name|s
operator|.
name|next
argument_list|()
return|;
block|}
comment|/**    * @see KeyValueScanner#getScannerOrder()    */
annotation|@
name|Override
specifier|public
name|long
name|getScannerOrder
parameter_list|()
block|{
return|return
name|scannerOrder
return|;
block|}
comment|/**    * Pretend we have done a seek but don't do it yet, if possible. The hope is    * that we find requested columns in more recent files and won't have to seek    * in older files. Creates a fake key/value with the given row/column and the    * highest (most recent) possible timestamp we might get from this file. When    * users of such "lazy scanner" need to know the next KV precisely (e.g. when    * this scanner is at the top of the heap), they run {@link #enforceSeek()}.    *<p>    * Note that this function does guarantee that the current KV of this scanner    * will be advanced to at least the given KV. Because of this, it does have    * to do a real seek in cases when the seek timestamp is older than the    * highest timestamp of the file, e.g. when we are trying to seek to the next    * row/column and use OLDEST_TIMESTAMP in the seek key.    */
annotation|@
name|Override
specifier|public
name|boolean
name|requestSeek
parameter_list|(
name|Cell
name|kv
parameter_list|,
name|boolean
name|forward
parameter_list|,
name|boolean
name|useBloom
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|kv
operator|.
name|getFamilyLength
argument_list|()
operator|==
literal|0
condition|)
block|{
name|useBloom
operator|=
literal|false
expr_stmt|;
block|}
name|boolean
name|haveToSeek
init|=
literal|true
decl_stmt|;
if|if
condition|(
name|useBloom
condition|)
block|{
comment|// check ROWCOL Bloom filter first.
if|if
condition|(
name|reader
operator|.
name|getBloomFilterType
argument_list|()
operator|==
name|BloomType
operator|.
name|ROWCOL
condition|)
block|{
name|haveToSeek
operator|=
name|reader
operator|.
name|passesGeneralRowColBloomFilter
argument_list|(
name|kv
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|canOptimizeForNonNullColumn
operator|&&
operator|(
operator|(
name|CellUtil
operator|.
name|isDeleteFamily
argument_list|(
name|kv
argument_list|)
operator|||
name|CellUtil
operator|.
name|isDeleteFamilyVersion
argument_list|(
name|kv
argument_list|)
operator|)
operator|)
condition|)
block|{
comment|// if there is no such delete family kv in the store file,
comment|// then no need to seek.
name|haveToSeek
operator|=
name|reader
operator|.
name|passesDeleteFamilyBloomFilter
argument_list|(
name|kv
operator|.
name|getRowArray
argument_list|()
argument_list|,
name|kv
operator|.
name|getRowOffset
argument_list|()
argument_list|,
name|kv
operator|.
name|getRowLength
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
name|delayedReseek
operator|=
name|forward
expr_stmt|;
name|delayedSeekKV
operator|=
name|kv
expr_stmt|;
if|if
condition|(
name|haveToSeek
condition|)
block|{
comment|// This row/column might be in this store file (or we did not use the
comment|// Bloom filter), so we still need to seek.
name|realSeekDone
operator|=
literal|false
expr_stmt|;
name|long
name|maxTimestampInFile
init|=
name|reader
operator|.
name|getMaxTimestamp
argument_list|()
decl_stmt|;
name|long
name|seekTimestamp
init|=
name|kv
operator|.
name|getTimestamp
argument_list|()
decl_stmt|;
if|if
condition|(
name|seekTimestamp
operator|>
name|maxTimestampInFile
condition|)
block|{
comment|// Create a fake key that is not greater than the real next key.
comment|// (Lower timestamps correspond to higher KVs.)
comment|// To understand this better, consider that we are asked to seek to
comment|// a higher timestamp than the max timestamp in this file. We know that
comment|// the next point when we have to consider this file again is when we
comment|// pass the max timestamp of this file (with the same row/column).
name|setCurrentCell
argument_list|(
name|CellUtil
operator|.
name|createFirstOnRowColTS
argument_list|(
name|kv
argument_list|,
name|maxTimestampInFile
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// This will be the case e.g. when we need to seek to the next
comment|// row/column, and we don't know exactly what they are, so we set the
comment|// seek key's timestamp to OLDEST_TIMESTAMP to skip the rest of this
comment|// row/column.
name|enforceSeek
argument_list|()
expr_stmt|;
block|}
return|return
name|cur
operator|!=
literal|null
return|;
block|}
comment|// Multi-column Bloom filter optimization.
comment|// Create a fake key/value, so that this scanner only bubbles up to the top
comment|// of the KeyValueHeap in StoreScanner after we scanned this row/column in
comment|// all other store files. The query matcher will then just skip this fake
comment|// key/value and the store scanner will progress to the next column. This
comment|// is obviously not a "real real" seek, but unlike the fake KV earlier in
comment|// this method, we want this to be propagated to ScanQueryMatcher.
name|setCurrentCell
argument_list|(
name|CellUtil
operator|.
name|createLastOnRowCol
argument_list|(
name|kv
argument_list|)
argument_list|)
expr_stmt|;
name|realSeekDone
operator|=
literal|true
expr_stmt|;
return|return
literal|true
return|;
block|}
name|StoreFileReader
name|getReader
parameter_list|()
block|{
return|return
name|reader
return|;
block|}
name|CellComparator
name|getComparator
parameter_list|()
block|{
return|return
name|reader
operator|.
name|getComparator
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|realSeekDone
parameter_list|()
block|{
return|return
name|realSeekDone
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|enforceSeek
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|realSeekDone
condition|)
return|return;
if|if
condition|(
name|delayedReseek
condition|)
block|{
name|reseek
argument_list|(
name|delayedSeekKV
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|seek
argument_list|(
name|delayedSeekKV
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isFileScanner
parameter_list|()
block|{
return|return
literal|true
return|;
block|}
comment|// Test methods
specifier|static
specifier|final
name|long
name|getSeekCount
parameter_list|()
block|{
return|return
name|seekCount
operator|.
name|get
argument_list|()
return|;
block|}
specifier|static
specifier|final
name|void
name|instrument
parameter_list|()
block|{
name|seekCount
operator|=
operator|new
name|Counter
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|shouldUseScanner
parameter_list|(
name|Scan
name|scan
parameter_list|,
name|Store
name|store
parameter_list|,
name|long
name|oldestUnexpiredTS
parameter_list|)
block|{
comment|// if the file has no entries, no need to validate or create a scanner.
name|byte
index|[]
name|cf
init|=
name|store
operator|.
name|getFamily
argument_list|()
operator|.
name|getName
argument_list|()
decl_stmt|;
name|TimeRange
name|timeRange
init|=
name|scan
operator|.
name|getColumnFamilyTimeRange
argument_list|()
operator|.
name|get
argument_list|(
name|cf
argument_list|)
decl_stmt|;
if|if
condition|(
name|timeRange
operator|==
literal|null
condition|)
block|{
name|timeRange
operator|=
name|scan
operator|.
name|getTimeRange
argument_list|()
expr_stmt|;
block|}
return|return
name|reader
operator|.
name|passesTimerangeFilter
argument_list|(
name|timeRange
argument_list|,
name|oldestUnexpiredTS
argument_list|)
operator|&&
name|reader
operator|.
name|passesKeyRangeFilter
argument_list|(
name|scan
argument_list|)
operator|&&
name|reader
operator|.
name|passesBloomFilter
argument_list|(
name|scan
argument_list|,
name|scan
operator|.
name|getFamilyMap
argument_list|()
operator|.
name|get
argument_list|(
name|cf
argument_list|)
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|seekToPreviousRow
parameter_list|(
name|Cell
name|originalKey
parameter_list|)
throws|throws
name|IOException
block|{
try|try
block|{
try|try
block|{
name|boolean
name|keepSeeking
init|=
literal|false
decl_stmt|;
name|Cell
name|key
init|=
name|originalKey
decl_stmt|;
do|do
block|{
name|Cell
name|seekKey
init|=
name|CellUtil
operator|.
name|createFirstOnRow
argument_list|(
name|key
argument_list|)
decl_stmt|;
if|if
condition|(
name|seekCount
operator|!=
literal|null
condition|)
name|seekCount
operator|.
name|increment
argument_list|()
expr_stmt|;
if|if
condition|(
operator|!
name|hfs
operator|.
name|seekBefore
argument_list|(
name|seekKey
argument_list|)
condition|)
block|{
name|this
operator|.
name|cur
operator|=
literal|null
expr_stmt|;
return|return
literal|false
return|;
block|}
name|Cell
name|curCell
init|=
name|hfs
operator|.
name|getCell
argument_list|()
decl_stmt|;
name|Cell
name|firstKeyOfPreviousRow
init|=
name|CellUtil
operator|.
name|createFirstOnRow
argument_list|(
name|curCell
argument_list|)
decl_stmt|;
if|if
condition|(
name|seekCount
operator|!=
literal|null
condition|)
name|seekCount
operator|.
name|increment
argument_list|()
expr_stmt|;
if|if
condition|(
operator|!
name|seekAtOrAfter
argument_list|(
name|hfs
argument_list|,
name|firstKeyOfPreviousRow
argument_list|)
condition|)
block|{
name|this
operator|.
name|cur
operator|=
literal|null
expr_stmt|;
return|return
literal|false
return|;
block|}
name|setCurrentCell
argument_list|(
name|hfs
operator|.
name|getCell
argument_list|()
argument_list|)
expr_stmt|;
name|this
operator|.
name|stopSkippingKVsIfNextRow
operator|=
literal|true
expr_stmt|;
name|boolean
name|resultOfSkipKVs
decl_stmt|;
try|try
block|{
name|resultOfSkipKVs
operator|=
name|skipKVsNewerThanReadpoint
argument_list|()
expr_stmt|;
block|}
finally|finally
block|{
name|this
operator|.
name|stopSkippingKVsIfNextRow
operator|=
literal|false
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|resultOfSkipKVs
operator|||
name|getComparator
argument_list|()
operator|.
name|compareRows
argument_list|(
name|cur
argument_list|,
name|firstKeyOfPreviousRow
argument_list|)
operator|>
literal|0
condition|)
block|{
name|keepSeeking
operator|=
literal|true
expr_stmt|;
name|key
operator|=
name|firstKeyOfPreviousRow
expr_stmt|;
continue|continue;
block|}
else|else
block|{
name|keepSeeking
operator|=
literal|false
expr_stmt|;
block|}
block|}
do|while
condition|(
name|keepSeeking
condition|)
do|;
return|return
literal|true
return|;
block|}
finally|finally
block|{
name|realSeekDone
operator|=
literal|true
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|FileNotFoundException
name|e
parameter_list|)
block|{
throw|throw
name|e
throw|;
block|}
catch|catch
parameter_list|(
name|IOException
name|ioe
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Could not seekToPreviousRow "
operator|+
name|this
operator|+
literal|" to key "
operator|+
name|originalKey
argument_list|,
name|ioe
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|seekToLastRow
parameter_list|()
throws|throws
name|IOException
block|{
name|byte
index|[]
name|lastRow
init|=
name|reader
operator|.
name|getLastRowKey
argument_list|()
decl_stmt|;
if|if
condition|(
name|lastRow
operator|==
literal|null
condition|)
block|{
return|return
literal|false
return|;
block|}
name|KeyValue
name|seekKey
init|=
name|KeyValueUtil
operator|.
name|createFirstOnRow
argument_list|(
name|lastRow
argument_list|)
decl_stmt|;
if|if
condition|(
name|seek
argument_list|(
name|seekKey
argument_list|)
condition|)
block|{
return|return
literal|true
return|;
block|}
else|else
block|{
return|return
name|seekToPreviousRow
argument_list|(
name|seekKey
argument_list|)
return|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|backwardSeek
parameter_list|(
name|Cell
name|key
parameter_list|)
throws|throws
name|IOException
block|{
name|seek
argument_list|(
name|key
argument_list|)
expr_stmt|;
if|if
condition|(
name|cur
operator|==
literal|null
operator|||
name|getComparator
argument_list|()
operator|.
name|compareRows
argument_list|(
name|cur
argument_list|,
name|key
argument_list|)
operator|>
literal|0
condition|)
block|{
return|return
name|seekToPreviousRow
argument_list|(
name|key
argument_list|)
return|;
block|}
return|return
literal|true
return|;
block|}
annotation|@
name|Override
specifier|public
name|Cell
name|getNextIndexedKey
parameter_list|()
block|{
return|return
name|hfs
operator|.
name|getNextIndexedKey
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|shipped
parameter_list|()
throws|throws
name|IOException
block|{
name|this
operator|.
name|hfs
operator|.
name|shipped
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

