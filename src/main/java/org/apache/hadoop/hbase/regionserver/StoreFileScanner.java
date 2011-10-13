begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2010 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|StoreFile
operator|.
name|Reader
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
name|concurrent
operator|.
name|atomic
operator|.
name|AtomicLong
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
name|java
operator|.
name|util
operator|.
name|SortedSet
import|;
end_import

begin_comment
comment|/**  * KeyValueScanner adaptor over the Reader.  It also provides hooks into  * bloom filter things.  */
end_comment

begin_class
class|class
name|StoreFileScanner
implements|implements
name|KeyValueScanner
block|{
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|Store
operator|.
name|class
argument_list|)
decl_stmt|;
comment|// the reader it comes from:
specifier|private
specifier|final
name|StoreFile
operator|.
name|Reader
name|reader
decl_stmt|;
specifier|private
specifier|final
name|HFileScanner
name|hfs
decl_stmt|;
specifier|private
name|KeyValue
name|cur
init|=
literal|null
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
name|KeyValue
name|delayedSeekKV
decl_stmt|;
comment|//The variable, realSeekDone, may cheat on store file scanner for the
comment|// multi-column bloom-filter optimization.
comment|// So this flag shows whether this storeFileScanner could do a reseek.
specifier|private
name|boolean
name|isReseekable
init|=
literal|false
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|AtomicLong
name|seekCount
init|=
operator|new
name|AtomicLong
argument_list|()
decl_stmt|;
comment|/**    * Implements a {@link KeyValueScanner} on top of the specified {@link HFileScanner}    * @param hfs HFile scanner    */
specifier|public
name|StoreFileScanner
parameter_list|(
name|StoreFile
operator|.
name|Reader
name|reader
parameter_list|,
name|HFileScanner
name|hfs
parameter_list|)
block|{
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
name|filesToCompact
parameter_list|,
name|boolean
name|cacheBlocks
parameter_list|,
name|boolean
name|usePread
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
name|filesToCompact
operator|.
name|size
argument_list|()
argument_list|)
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
name|createReader
argument_list|()
decl_stmt|;
name|scanners
operator|.
name|add
argument_list|(
name|r
operator|.
name|getStoreFileScanner
argument_list|(
name|cacheBlocks
argument_list|,
name|usePread
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|scanners
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
name|KeyValue
name|peek
parameter_list|()
block|{
return|return
name|cur
return|;
block|}
specifier|public
name|KeyValue
name|next
parameter_list|()
throws|throws
name|IOException
block|{
name|KeyValue
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
name|cur
operator|=
name|hfs
operator|.
name|getKeyValue
argument_list|()
expr_stmt|;
block|}
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
name|KeyValue
name|key
parameter_list|)
throws|throws
name|IOException
block|{
name|seekCount
operator|.
name|incrementAndGet
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
name|close
argument_list|()
expr_stmt|;
return|return
literal|false
return|;
block|}
name|this
operator|.
name|isReseekable
operator|=
literal|true
expr_stmt|;
name|cur
operator|=
name|hfs
operator|.
name|getKeyValue
argument_list|()
expr_stmt|;
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
name|KeyValue
name|key
parameter_list|)
throws|throws
name|IOException
block|{
name|seekCount
operator|.
name|incrementAndGet
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
name|close
argument_list|()
expr_stmt|;
return|return
literal|false
return|;
block|}
name|cur
operator|=
name|hfs
operator|.
name|getKeyValue
argument_list|()
expr_stmt|;
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
argument_list|,
name|ioe
argument_list|)
throw|;
block|}
block|}
specifier|public
name|void
name|close
parameter_list|()
block|{
comment|// Nothing to close on HFileScanner?
name|cur
operator|=
literal|null
expr_stmt|;
block|}
comment|/**    *    * @param s    * @param k    * @return    * @throws IOException    */
specifier|public
specifier|static
name|boolean
name|seekAtOrAfter
parameter_list|(
name|HFileScanner
name|s
parameter_list|,
name|KeyValue
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
operator|.
name|getBuffer
argument_list|()
argument_list|,
name|k
operator|.
name|getKeyOffset
argument_list|()
argument_list|,
name|k
operator|.
name|getKeyLength
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|result
operator|<
literal|0
condition|)
block|{
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
name|KeyValue
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
operator|.
name|getBuffer
argument_list|()
argument_list|,
name|k
operator|.
name|getKeyOffset
argument_list|()
argument_list|,
name|k
operator|.
name|getKeyLength
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|result
operator|<=
literal|0
condition|)
block|{
return|return
literal|true
return|;
block|}
else|else
block|{
comment|// passed KV is larger than current KV in file, if there is a next
comment|// it is after, if not then this scanner is done.
return|return
name|s
operator|.
name|next
argument_list|()
return|;
block|}
block|}
comment|// StoreFile filter hook.
specifier|public
name|boolean
name|shouldSeek
parameter_list|(
name|Scan
name|scan
parameter_list|,
specifier|final
name|SortedSet
argument_list|<
name|byte
index|[]
argument_list|>
name|columns
parameter_list|)
block|{
return|return
name|reader
operator|.
name|shouldSeek
argument_list|(
name|scan
argument_list|,
name|columns
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getSequenceID
parameter_list|()
block|{
return|return
name|reader
operator|.
name|getSequenceID
argument_list|()
return|;
block|}
comment|/**    * Pretend we have done a seek but don't do it yet, if possible. The hope is    * that we find requested columns in more recent files and won't have to seek    * in older files. Creates a fake key/value with the given row/column and the    * highest (most recent) possible timestamp we might get from this file. When    * users of such "lazy scanner" need to know the next KV precisely (e.g. when    * this scanner is at the top of the heap), they run {@link #enforceSeek()}.    *<p>    * Note that this function does guarantee that the current KV of this scanner    * will be advanced to at least the given KV. Because of this, it does have    * to do a real seek in cases when the seek timestamp is older than the    * highest timestamp of the file, e.g. when we are trying to seek to the next    * row/column and use OLDEST_TIMESTAMP in the seek key.    */
annotation|@
name|Override
specifier|public
name|boolean
name|requestSeek
parameter_list|(
name|KeyValue
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
name|reader
operator|.
name|getBloomFilterType
argument_list|()
operator|!=
name|StoreFile
operator|.
name|BloomType
operator|.
name|ROWCOL
operator|||
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
name|haveToSeek
operator|=
name|reader
operator|.
name|passesBloomFilter
argument_list|(
name|kv
operator|.
name|getBuffer
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
argument_list|,
name|kv
operator|.
name|getBuffer
argument_list|()
argument_list|,
name|kv
operator|.
name|getQualifierOffset
argument_list|()
argument_list|,
name|kv
operator|.
name|getQualifierLength
argument_list|()
argument_list|)
expr_stmt|;
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
name|cur
operator|=
name|kv
operator|.
name|createFirstOnRowColTS
argument_list|(
name|maxTimestampInFile
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
name|cur
operator|=
name|kv
operator|.
name|createLastOnRowCol
argument_list|()
expr_stmt|;
name|realSeekDone
operator|=
literal|true
expr_stmt|;
return|return
literal|true
return|;
block|}
name|Reader
name|getReaderForTesting
parameter_list|()
block|{
return|return
name|reader
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
operator|&&
name|this
operator|.
name|isReseekable
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
block|}
end_class

end_unit

