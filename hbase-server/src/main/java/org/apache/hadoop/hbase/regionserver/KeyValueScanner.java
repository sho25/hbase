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
name|Closeable
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

begin_comment
comment|/**  * Scanner that returns the next KeyValue.  */
end_comment

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|Private
comment|// TODO: Change name from KeyValueScanner to CellScanner only we already have a simple CellScanner
comment|// so this should be something else altogether, a decoration on our base CellScanner. TODO.
comment|// This class shows in CPs so do it all in one swell swoop. HBase-2.0.0.
specifier|public
interface|interface
name|KeyValueScanner
extends|extends
name|Shipper
extends|,
name|Closeable
block|{
comment|/**    * The byte array represents for NO_NEXT_INDEXED_KEY;    * The actual value is irrelevant because this is always compared by reference.    */
specifier|public
specifier|static
specifier|final
name|Cell
name|NO_NEXT_INDEXED_KEY
init|=
operator|new
name|KeyValue
argument_list|()
decl_stmt|;
comment|/**    * Look at the next Cell in this scanner, but do not iterate scanner.    * NOTICE: The returned cell has not been passed into ScanQueryMatcher. So it may not be what the    * user need.    * @return the next Cell    */
name|Cell
name|peek
parameter_list|()
function_decl|;
comment|/**    * Return the next Cell in this scanner, iterating the scanner    * @return the next Cell    */
name|Cell
name|next
parameter_list|()
throws|throws
name|IOException
function_decl|;
comment|/**    * Seek the scanner at or after the specified KeyValue.    * @param key seek value    * @return true if scanner has values left, false if end of scanner    */
name|boolean
name|seek
parameter_list|(
name|Cell
name|key
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Reseek the scanner at or after the specified KeyValue.    * This method is guaranteed to seek at or after the required key only if the    * key comes after the current position of the scanner. Should not be used    * to seek to a key which may come before the current position.    * @param key seek value (should be non-null)    * @return true if scanner has values left, false if end of scanner    */
name|boolean
name|reseek
parameter_list|(
name|Cell
name|key
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Get the order of this KeyValueScanner. This is only relevant for StoreFileScanners.    * This is required for comparing multiple files to find out which one has the latest    * data. StoreFileScanners are ordered from 0 (oldest) to newest in increasing order.    */
specifier|default
name|long
name|getScannerOrder
parameter_list|()
block|{
return|return
literal|0
return|;
block|}
comment|/**    * Close the KeyValue scanner.    */
annotation|@
name|Override
name|void
name|close
parameter_list|()
function_decl|;
comment|/**    * Allows to filter out scanners (both StoreFile and memstore) that we don't    * want to use based on criteria such as Bloom filters and timestamp ranges.    * @param scan the scan that we are selecting scanners for    * @param store the store we are performing the scan on.    * @param oldestUnexpiredTS the oldest timestamp we are interested in for    *          this query, based on TTL    * @return true if the scanner should be included in the query    */
name|boolean
name|shouldUseScanner
parameter_list|(
name|Scan
name|scan
parameter_list|,
name|HStore
name|store
parameter_list|,
name|long
name|oldestUnexpiredTS
parameter_list|)
function_decl|;
comment|// "Lazy scanner" optimizations
comment|/**    * Similar to {@link #seek} (or {@link #reseek} if forward is true) but only    * does a seek operation after checking that it is really necessary for the    * row/column combination specified by the kv parameter. This function was    * added to avoid unnecessary disk seeks by checking row-column Bloom filters    * before a seek on multi-column get/scan queries, and to optimize by looking    * up more recent files first.    * @param forward do a forward-only "reseek" instead of a random-access seek    * @param useBloom whether to enable multi-column Bloom filter optimization    */
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
function_decl|;
comment|/**    * We optimize our store scanners by checking the most recent store file    * first, so we sometimes pretend we have done a seek but delay it until the    * store scanner bubbles up to the top of the key-value heap. This method is    * then used to ensure the top store file scanner has done a seek operation.    */
name|boolean
name|realSeekDone
parameter_list|()
function_decl|;
comment|/**    * Does the real seek operation in case it was skipped by    * seekToRowCol(KeyValue, boolean) (TODO: Whats this?). Note that this function should    * be never called on scanners that always do real seek operations (i.e. most    * of the scanners). The easiest way to achieve this is to call    * {@link #realSeekDone()} first.    */
name|void
name|enforceSeek
parameter_list|()
throws|throws
name|IOException
function_decl|;
comment|/**    * @return true if this is a file scanner. Otherwise a memory scanner is    *         assumed.    */
name|boolean
name|isFileScanner
parameter_list|()
function_decl|;
comment|/**    * @return the file path if this is a file scanner, otherwise null.    * @see #isFileScanner()    */
name|Path
name|getFilePath
parameter_list|()
function_decl|;
comment|// Support for "Reversed Scanner"
comment|/**    * Seek the scanner at or before the row of specified Cell, it firstly    * tries to seek the scanner at or after the specified Cell, return if    * peek KeyValue of scanner has the same row with specified Cell,    * otherwise seek the scanner at the first Cell of the row which is the    * previous row of specified KeyValue    *    * @param key seek KeyValue    * @return true if the scanner is at the valid KeyValue, false if such    *         KeyValue does not exist    *    */
specifier|public
name|boolean
name|backwardSeek
parameter_list|(
name|Cell
name|key
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Seek the scanner at the first Cell of the row which is the previous row    * of specified key    * @param key seek value    * @return true if the scanner at the first valid Cell of previous row,    *         false if not existing such Cell    */
specifier|public
name|boolean
name|seekToPreviousRow
parameter_list|(
name|Cell
name|key
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Seek the scanner at the first KeyValue of last row    *    * @return true if scanner has values left, false if the underlying data is    *         empty    * @throws IOException    */
specifier|public
name|boolean
name|seekToLastRow
parameter_list|()
throws|throws
name|IOException
function_decl|;
comment|/**    * @return the next key in the index, usually the first key of next block OR a key that falls    * between last key of current block and first key of next block..    * see HFileWriterImpl#getMidpoint, or null if not known.    */
specifier|public
name|Cell
name|getNextIndexedKey
parameter_list|()
function_decl|;
block|}
end_interface

end_unit

