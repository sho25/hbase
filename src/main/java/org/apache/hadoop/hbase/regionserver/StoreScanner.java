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
name|LinkedList
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
name|NavigableSet
import|;
end_import

begin_comment
comment|/**  * Scanner scans both the memstore and the HStore. Coalesce KeyValue stream  * into List<KeyValue> for a single row.  */
end_comment

begin_class
class|class
name|StoreScanner
implements|implements
name|KeyValueScanner
implements|,
name|InternalScanner
implements|,
name|ChangedReadersObserver
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
name|StoreScanner
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|Store
name|store
decl_stmt|;
specifier|private
name|ScanQueryMatcher
name|matcher
decl_stmt|;
specifier|private
name|KeyValueHeap
name|heap
decl_stmt|;
specifier|private
name|boolean
name|cacheBlocks
decl_stmt|;
comment|// Used to indicate that the scanner has closed (see HBASE-1107)
comment|// Doesnt need to be volatile because it's always accessed via synchronized methods
specifier|private
name|boolean
name|closing
init|=
literal|false
decl_stmt|;
specifier|private
specifier|final
name|boolean
name|isGet
decl_stmt|;
comment|// if heap == null and lastTop != null, you need to reseek given the key below
specifier|private
name|KeyValue
name|lastTop
init|=
literal|null
decl_stmt|;
comment|/**    * Opens a scanner across memstore, snapshot, and all StoreFiles.    *    * @param store who we scan    * @param scan the spec    * @param columns which columns we are scanning    * @throws IOException    */
name|StoreScanner
parameter_list|(
name|Store
name|store
parameter_list|,
name|Scan
name|scan
parameter_list|,
specifier|final
name|NavigableSet
argument_list|<
name|byte
index|[]
argument_list|>
name|columns
parameter_list|)
throws|throws
name|IOException
block|{
name|this
operator|.
name|store
operator|=
name|store
expr_stmt|;
name|this
operator|.
name|cacheBlocks
operator|=
name|scan
operator|.
name|getCacheBlocks
argument_list|()
expr_stmt|;
name|matcher
operator|=
operator|new
name|ScanQueryMatcher
argument_list|(
name|scan
argument_list|,
name|store
operator|.
name|getFamily
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|,
name|columns
argument_list|,
name|store
operator|.
name|ttl
argument_list|,
name|store
operator|.
name|comparator
operator|.
name|getRawComparator
argument_list|()
argument_list|,
name|store
operator|.
name|versionsToReturn
argument_list|(
name|scan
operator|.
name|getMaxVersions
argument_list|()
argument_list|)
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|this
operator|.
name|isGet
operator|=
name|scan
operator|.
name|isGetScan
argument_list|()
expr_stmt|;
comment|// pass columns = try to filter out unnecessary ScanFiles
name|List
argument_list|<
name|KeyValueScanner
argument_list|>
name|scanners
init|=
name|getScanners
argument_list|(
name|scan
argument_list|,
name|columns
argument_list|)
decl_stmt|;
comment|// Seek all scanners to the start of the Row (or if the exact maching row key does not
comment|// exist, then to the start of the next matching Row).
for|for
control|(
name|KeyValueScanner
name|scanner
range|:
name|scanners
control|)
block|{
name|scanner
operator|.
name|seek
argument_list|(
name|matcher
operator|.
name|getStartKey
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|// Combine all seeked scanners with a heap
name|heap
operator|=
operator|new
name|KeyValueHeap
argument_list|(
name|scanners
argument_list|,
name|store
operator|.
name|comparator
argument_list|)
expr_stmt|;
name|this
operator|.
name|store
operator|.
name|addChangedReaderObserver
argument_list|(
name|this
argument_list|)
expr_stmt|;
block|}
comment|/**    * Used for major compactions.<p>    *    * Opens a scanner across specified StoreFiles.    * @param store who we scan    * @param scan the spec    * @param scanners ancilliary scanners    */
name|StoreScanner
parameter_list|(
name|Store
name|store
parameter_list|,
name|Scan
name|scan
parameter_list|,
name|List
argument_list|<
name|?
extends|extends
name|KeyValueScanner
argument_list|>
name|scanners
parameter_list|,
name|boolean
name|retainDeletesInOutput
parameter_list|)
throws|throws
name|IOException
block|{
name|this
operator|.
name|store
operator|=
name|store
expr_stmt|;
name|this
operator|.
name|cacheBlocks
operator|=
literal|false
expr_stmt|;
name|this
operator|.
name|isGet
operator|=
literal|false
expr_stmt|;
name|matcher
operator|=
operator|new
name|ScanQueryMatcher
argument_list|(
name|scan
argument_list|,
name|store
operator|.
name|getFamily
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|,
literal|null
argument_list|,
name|store
operator|.
name|ttl
argument_list|,
name|store
operator|.
name|comparator
operator|.
name|getRawComparator
argument_list|()
argument_list|,
name|store
operator|.
name|versionsToReturn
argument_list|(
name|scan
operator|.
name|getMaxVersions
argument_list|()
argument_list|)
argument_list|,
name|retainDeletesInOutput
argument_list|)
expr_stmt|;
comment|// Seek all scanners to the initial key
for|for
control|(
name|KeyValueScanner
name|scanner
range|:
name|scanners
control|)
block|{
name|scanner
operator|.
name|seek
argument_list|(
name|matcher
operator|.
name|getStartKey
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|// Combine all seeked scanners with a heap
name|heap
operator|=
operator|new
name|KeyValueHeap
argument_list|(
name|scanners
argument_list|,
name|store
operator|.
name|comparator
argument_list|)
expr_stmt|;
block|}
comment|// Constructor for testing.
name|StoreScanner
parameter_list|(
specifier|final
name|Scan
name|scan
parameter_list|,
specifier|final
name|byte
index|[]
name|colFamily
parameter_list|,
specifier|final
name|long
name|ttl
parameter_list|,
specifier|final
name|KeyValue
operator|.
name|KVComparator
name|comparator
parameter_list|,
specifier|final
name|NavigableSet
argument_list|<
name|byte
index|[]
argument_list|>
name|columns
parameter_list|,
specifier|final
name|List
argument_list|<
name|KeyValueScanner
argument_list|>
name|scanners
parameter_list|)
throws|throws
name|IOException
block|{
name|this
operator|.
name|store
operator|=
literal|null
expr_stmt|;
name|this
operator|.
name|isGet
operator|=
literal|false
expr_stmt|;
name|this
operator|.
name|cacheBlocks
operator|=
name|scan
operator|.
name|getCacheBlocks
argument_list|()
expr_stmt|;
name|this
operator|.
name|matcher
operator|=
operator|new
name|ScanQueryMatcher
argument_list|(
name|scan
argument_list|,
name|colFamily
argument_list|,
name|columns
argument_list|,
name|ttl
argument_list|,
name|comparator
operator|.
name|getRawComparator
argument_list|()
argument_list|,
name|scan
operator|.
name|getMaxVersions
argument_list|()
argument_list|,
literal|false
argument_list|)
expr_stmt|;
comment|// Seek all scanners to the initial key
for|for
control|(
name|KeyValueScanner
name|scanner
range|:
name|scanners
control|)
block|{
name|scanner
operator|.
name|seek
argument_list|(
name|matcher
operator|.
name|getStartKey
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|heap
operator|=
operator|new
name|KeyValueHeap
argument_list|(
name|scanners
argument_list|,
name|comparator
argument_list|)
expr_stmt|;
block|}
comment|/*    * @return List of scanners ordered properly.    */
specifier|private
name|List
argument_list|<
name|KeyValueScanner
argument_list|>
name|getScanners
parameter_list|()
throws|throws
name|IOException
block|{
comment|// First the store file scanners
comment|// TODO this used to get the store files in descending order,
comment|// but now we get them in ascending order, which I think is
comment|// actually more correct, since memstore get put at the end.
name|List
argument_list|<
name|StoreFileScanner
argument_list|>
name|sfScanners
init|=
name|StoreFileScanner
operator|.
name|getScannersForStoreFiles
argument_list|(
name|store
operator|.
name|getStorefiles
argument_list|()
argument_list|,
name|cacheBlocks
argument_list|,
name|isGet
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|KeyValueScanner
argument_list|>
name|scanners
init|=
operator|new
name|ArrayList
argument_list|<
name|KeyValueScanner
argument_list|>
argument_list|(
name|sfScanners
operator|.
name|size
argument_list|()
operator|+
literal|1
argument_list|)
decl_stmt|;
name|scanners
operator|.
name|addAll
argument_list|(
name|sfScanners
argument_list|)
expr_stmt|;
comment|// Then the memstore scanners
name|scanners
operator|.
name|addAll
argument_list|(
name|this
operator|.
name|store
operator|.
name|memstore
operator|.
name|getScanners
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|scanners
return|;
block|}
comment|/*    * @return List of scanners to seek, possibly filtered by StoreFile.    */
specifier|private
name|List
argument_list|<
name|KeyValueScanner
argument_list|>
name|getScanners
parameter_list|(
name|Scan
name|scan
parameter_list|,
specifier|final
name|NavigableSet
argument_list|<
name|byte
index|[]
argument_list|>
name|columns
parameter_list|)
throws|throws
name|IOException
block|{
name|boolean
name|memOnly
decl_stmt|;
name|boolean
name|filesOnly
decl_stmt|;
if|if
condition|(
name|scan
operator|instanceof
name|InternalScan
condition|)
block|{
name|InternalScan
name|iscan
init|=
operator|(
name|InternalScan
operator|)
name|scan
decl_stmt|;
name|memOnly
operator|=
name|iscan
operator|.
name|isCheckOnlyMemStore
argument_list|()
expr_stmt|;
name|filesOnly
operator|=
name|iscan
operator|.
name|isCheckOnlyStoreFiles
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|memOnly
operator|=
literal|false
expr_stmt|;
name|filesOnly
operator|=
literal|false
expr_stmt|;
block|}
name|List
argument_list|<
name|KeyValueScanner
argument_list|>
name|scanners
init|=
operator|new
name|LinkedList
argument_list|<
name|KeyValueScanner
argument_list|>
argument_list|()
decl_stmt|;
comment|// First the store file scanners
if|if
condition|(
name|memOnly
operator|==
literal|false
condition|)
block|{
name|List
argument_list|<
name|StoreFileScanner
argument_list|>
name|sfScanners
init|=
name|StoreFileScanner
operator|.
name|getScannersForStoreFiles
argument_list|(
name|store
operator|.
name|getStorefiles
argument_list|()
argument_list|,
name|cacheBlocks
argument_list|,
name|isGet
argument_list|)
decl_stmt|;
comment|// include only those scan files which pass all filters
for|for
control|(
name|StoreFileScanner
name|sfs
range|:
name|sfScanners
control|)
block|{
if|if
condition|(
name|sfs
operator|.
name|shouldSeek
argument_list|(
name|scan
argument_list|,
name|columns
argument_list|)
condition|)
block|{
name|scanners
operator|.
name|add
argument_list|(
name|sfs
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|// Then the memstore scanners
if|if
condition|(
operator|(
name|filesOnly
operator|==
literal|false
operator|)
operator|&&
operator|(
name|this
operator|.
name|store
operator|.
name|memstore
operator|.
name|shouldSeek
argument_list|(
name|scan
argument_list|)
operator|)
condition|)
block|{
name|scanners
operator|.
name|addAll
argument_list|(
name|this
operator|.
name|store
operator|.
name|memstore
operator|.
name|getScanners
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|scanners
return|;
block|}
specifier|public
specifier|synchronized
name|KeyValue
name|peek
parameter_list|()
block|{
if|if
condition|(
name|this
operator|.
name|heap
operator|==
literal|null
condition|)
block|{
return|return
name|this
operator|.
name|lastTop
return|;
block|}
return|return
name|this
operator|.
name|heap
operator|.
name|peek
argument_list|()
return|;
block|}
specifier|public
name|KeyValue
name|next
parameter_list|()
block|{
comment|// throw runtime exception perhaps?
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Never call StoreScanner.next()"
argument_list|)
throw|;
block|}
specifier|public
specifier|synchronized
name|void
name|close
parameter_list|()
block|{
if|if
condition|(
name|this
operator|.
name|closing
condition|)
return|return;
name|this
operator|.
name|closing
operator|=
literal|true
expr_stmt|;
comment|// under test, we dont have a this.store
if|if
condition|(
name|this
operator|.
name|store
operator|!=
literal|null
condition|)
name|this
operator|.
name|store
operator|.
name|deleteChangedReaderObserver
argument_list|(
name|this
argument_list|)
expr_stmt|;
if|if
condition|(
name|this
operator|.
name|heap
operator|!=
literal|null
condition|)
name|this
operator|.
name|heap
operator|.
name|close
argument_list|()
expr_stmt|;
name|this
operator|.
name|heap
operator|=
literal|null
expr_stmt|;
comment|// CLOSED!
name|this
operator|.
name|lastTop
operator|=
literal|null
expr_stmt|;
comment|// If both are null, we are closed.
block|}
specifier|public
specifier|synchronized
name|boolean
name|seek
parameter_list|(
name|KeyValue
name|key
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|this
operator|.
name|heap
operator|==
literal|null
condition|)
block|{
name|List
argument_list|<
name|KeyValueScanner
argument_list|>
name|scanners
init|=
name|getScanners
argument_list|()
decl_stmt|;
name|heap
operator|=
operator|new
name|KeyValueHeap
argument_list|(
name|scanners
argument_list|,
name|store
operator|.
name|comparator
argument_list|)
expr_stmt|;
block|}
return|return
name|this
operator|.
name|heap
operator|.
name|seek
argument_list|(
name|key
argument_list|)
return|;
block|}
comment|/**    * Get the next row of values from this Store.    * @param outResult    * @param limit    * @return true if there are more rows, false if scanner is done    */
specifier|public
specifier|synchronized
name|boolean
name|next
parameter_list|(
name|List
argument_list|<
name|KeyValue
argument_list|>
name|outResult
parameter_list|,
name|int
name|limit
parameter_list|)
throws|throws
name|IOException
block|{
comment|//DebugPrint.println("SS.next");
name|checkReseek
argument_list|()
expr_stmt|;
comment|// if the heap was left null, then the scanners had previously run out anyways, close and
comment|// return.
if|if
condition|(
name|this
operator|.
name|heap
operator|==
literal|null
condition|)
block|{
name|close
argument_list|()
expr_stmt|;
return|return
literal|false
return|;
block|}
name|KeyValue
name|peeked
init|=
name|this
operator|.
name|heap
operator|.
name|peek
argument_list|()
decl_stmt|;
if|if
condition|(
name|peeked
operator|==
literal|null
condition|)
block|{
name|close
argument_list|()
expr_stmt|;
return|return
literal|false
return|;
block|}
comment|// only call setRow if the row changes; avoids confusing the query matcher
comment|// if scanning intra-row
if|if
condition|(
operator|(
name|matcher
operator|.
name|row
operator|==
literal|null
operator|)
operator|||
operator|!
name|peeked
operator|.
name|matchingRow
argument_list|(
name|matcher
operator|.
name|row
argument_list|)
condition|)
block|{
name|matcher
operator|.
name|setRow
argument_list|(
name|peeked
operator|.
name|getRow
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|KeyValue
name|kv
decl_stmt|;
name|List
argument_list|<
name|KeyValue
argument_list|>
name|results
init|=
operator|new
name|ArrayList
argument_list|<
name|KeyValue
argument_list|>
argument_list|()
decl_stmt|;
name|LOOP
label|:
while|while
condition|(
operator|(
name|kv
operator|=
name|this
operator|.
name|heap
operator|.
name|peek
argument_list|()
operator|)
operator|!=
literal|null
condition|)
block|{
comment|// kv is no longer immutable due to KeyOnlyFilter! use copy for safety
name|KeyValue
name|copyKv
init|=
name|kv
operator|.
name|shallowCopy
argument_list|()
decl_stmt|;
name|ScanQueryMatcher
operator|.
name|MatchCode
name|qcode
init|=
name|matcher
operator|.
name|match
argument_list|(
name|copyKv
argument_list|)
decl_stmt|;
comment|//DebugPrint.println("SS peek kv = " + kv + " with qcode = " + qcode);
switch|switch
condition|(
name|qcode
condition|)
block|{
case|case
name|INCLUDE
case|:
name|results
operator|.
name|add
argument_list|(
name|copyKv
argument_list|)
expr_stmt|;
name|this
operator|.
name|heap
operator|.
name|next
argument_list|()
expr_stmt|;
if|if
condition|(
name|limit
operator|>
literal|0
operator|&&
operator|(
name|results
operator|.
name|size
argument_list|()
operator|==
name|limit
operator|)
condition|)
block|{
break|break
name|LOOP
break|;
block|}
continue|continue;
case|case
name|DONE
case|:
comment|// copy jazz
name|outResult
operator|.
name|addAll
argument_list|(
name|results
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
case|case
name|DONE_SCAN
case|:
name|close
argument_list|()
expr_stmt|;
comment|// copy jazz
name|outResult
operator|.
name|addAll
argument_list|(
name|results
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
case|case
name|SEEK_NEXT_ROW
case|:
comment|// This is just a relatively simple end of scan fix, to short-cut end us if there is a
comment|// endKey in the scan.
if|if
condition|(
operator|!
name|matcher
operator|.
name|moreRowsMayExistAfter
argument_list|(
name|kv
argument_list|)
condition|)
block|{
name|outResult
operator|.
name|addAll
argument_list|(
name|results
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
name|reseek
argument_list|(
name|matcher
operator|.
name|getKeyForNextRow
argument_list|(
name|kv
argument_list|)
argument_list|)
expr_stmt|;
break|break;
case|case
name|SEEK_NEXT_COL
case|:
name|reseek
argument_list|(
name|matcher
operator|.
name|getKeyForNextColumn
argument_list|(
name|kv
argument_list|)
argument_list|)
expr_stmt|;
break|break;
case|case
name|SKIP
case|:
name|this
operator|.
name|heap
operator|.
name|next
argument_list|()
expr_stmt|;
break|break;
case|case
name|SEEK_NEXT_USING_HINT
case|:
name|KeyValue
name|nextKV
init|=
name|matcher
operator|.
name|getNextKeyHint
argument_list|(
name|kv
argument_list|)
decl_stmt|;
if|if
condition|(
name|nextKV
operator|!=
literal|null
condition|)
block|{
name|reseek
argument_list|(
name|nextKV
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|heap
operator|.
name|next
argument_list|()
expr_stmt|;
block|}
break|break;
default|default:
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"UNEXPECTED"
argument_list|)
throw|;
block|}
block|}
if|if
condition|(
operator|!
name|results
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
comment|// copy jazz
name|outResult
operator|.
name|addAll
argument_list|(
name|results
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
comment|// No more keys
name|close
argument_list|()
expr_stmt|;
return|return
literal|false
return|;
block|}
specifier|public
specifier|synchronized
name|boolean
name|next
parameter_list|(
name|List
argument_list|<
name|KeyValue
argument_list|>
name|outResult
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|next
argument_list|(
name|outResult
argument_list|,
operator|-
literal|1
argument_list|)
return|;
block|}
comment|// Implementation of ChangedReadersObserver
specifier|public
specifier|synchronized
name|void
name|updateReaders
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|this
operator|.
name|closing
condition|)
return|return;
comment|// All public synchronized API calls will call 'checkReseek' which will cause
comment|// the scanner stack to reseek if this.heap==null&& this.lastTop != null.
comment|// But if two calls to updateReaders() happen without a 'next' or 'peek' then we
comment|// will end up calling this.peek() which would cause a reseek in the middle of a updateReaders
comment|// which is NOT what we want, not to mention could cause an NPE. So we early out here.
if|if
condition|(
name|this
operator|.
name|heap
operator|==
literal|null
condition|)
return|return;
comment|// this could be null.
name|this
operator|.
name|lastTop
operator|=
name|this
operator|.
name|peek
argument_list|()
expr_stmt|;
comment|//DebugPrint.println("SS updateReaders, topKey = " + lastTop);
comment|// close scanners to old obsolete Store files
name|this
operator|.
name|heap
operator|.
name|close
argument_list|()
expr_stmt|;
comment|// bubble thru and close all scanners.
name|this
operator|.
name|heap
operator|=
literal|null
expr_stmt|;
comment|// the re-seeks could be slow (access HDFS) free up memory ASAP
comment|// Let the next() call handle re-creating and seeking
block|}
specifier|private
name|void
name|checkReseek
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|this
operator|.
name|heap
operator|==
literal|null
operator|&&
name|this
operator|.
name|lastTop
operator|!=
literal|null
condition|)
block|{
name|resetScannerStack
argument_list|(
name|this
operator|.
name|lastTop
argument_list|)
expr_stmt|;
name|this
operator|.
name|lastTop
operator|=
literal|null
expr_stmt|;
comment|// gone!
block|}
comment|// else dont need to reseek
block|}
specifier|private
name|void
name|resetScannerStack
parameter_list|(
name|KeyValue
name|lastTopKey
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|heap
operator|!=
literal|null
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"StoreScanner.reseek run on an existing heap!"
argument_list|)
throw|;
block|}
comment|/* When we have the scan object, should we not pass it to getScanners()      * to get a limited set of scanners? We did so in the constructor and we      * could have done it now by storing the scan object from the constructor */
name|List
argument_list|<
name|KeyValueScanner
argument_list|>
name|scanners
init|=
name|getScanners
argument_list|()
decl_stmt|;
for|for
control|(
name|KeyValueScanner
name|scanner
range|:
name|scanners
control|)
block|{
name|scanner
operator|.
name|seek
argument_list|(
name|lastTopKey
argument_list|)
expr_stmt|;
block|}
comment|// Combine all seeked scanners with a heap
name|heap
operator|=
operator|new
name|KeyValueHeap
argument_list|(
name|scanners
argument_list|,
name|store
operator|.
name|comparator
argument_list|)
expr_stmt|;
comment|// Reset the state of the Query Matcher and set to top row
name|matcher
operator|.
name|reset
argument_list|()
expr_stmt|;
name|KeyValue
name|kv
init|=
name|heap
operator|.
name|peek
argument_list|()
decl_stmt|;
name|matcher
operator|.
name|setRow
argument_list|(
operator|(
name|kv
operator|==
literal|null
condition|?
name|lastTopKey
else|:
name|kv
operator|)
operator|.
name|getRow
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
specifier|synchronized
name|boolean
name|reseek
parameter_list|(
name|KeyValue
name|kv
parameter_list|)
throws|throws
name|IOException
block|{
comment|//Heap cannot be null, because this is only called from next() which
comment|//guarantees that heap will never be null before this call.
return|return
name|this
operator|.
name|heap
operator|.
name|reseek
argument_list|(
name|kv
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
literal|0
return|;
block|}
block|}
end_class

end_unit

