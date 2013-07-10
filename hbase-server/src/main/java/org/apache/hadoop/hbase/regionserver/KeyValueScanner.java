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
name|IOException
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
specifier|public
interface|interface
name|KeyValueScanner
block|{
comment|/**    * Look at the next KeyValue in this scanner, but do not iterate scanner.    * @return the next KeyValue    */
name|KeyValue
name|peek
parameter_list|()
function_decl|;
comment|/**    * Return the next KeyValue in this scanner, iterating the scanner    * @return the next KeyValue    */
name|KeyValue
name|next
parameter_list|()
throws|throws
name|IOException
function_decl|;
comment|/**    * Seek the scanner at or after the specified KeyValue.    * @param key seek value    * @return true if scanner has values left, false if end of scanner    */
name|boolean
name|seek
parameter_list|(
name|KeyValue
name|key
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Reseek the scanner at or after the specified KeyValue.    * This method is guaranteed to seek at or after the required key only if the    * key comes after the current position of the scanner. Should not be used    * to seek to a key which may come before the current position.    * @param key seek value (should be non-null)    * @return true if scanner has values left, false if end of scanner    */
name|boolean
name|reseek
parameter_list|(
name|KeyValue
name|key
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Get the sequence id associated with this KeyValueScanner. This is required    * for comparing multiple files to find out which one has the latest data.    * The default implementation for this would be to return 0. A file having    * lower sequence id will be considered to be the older one.    */
name|long
name|getSequenceID
parameter_list|()
function_decl|;
comment|/**    * Close the KeyValue scanner.    */
name|void
name|close
parameter_list|()
function_decl|;
comment|/**    * Allows to filter out scanners (both StoreFile and memstore) that we don't    * want to use based on criteria such as Bloom filters and timestamp ranges.    * @param scan the scan that we are selecting scanners for    * @param columns the set of columns in the current column family, or null if    *          not specified by the scan    * @param oldestUnexpiredTS the oldest timestamp we are interested in for    *          this query, based on TTL    * @return true if the scanner should be included in the query    */
name|boolean
name|shouldUseScanner
parameter_list|(
name|Scan
name|scan
parameter_list|,
name|SortedSet
argument_list|<
name|byte
index|[]
argument_list|>
name|columns
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
block|}
end_interface

end_unit

