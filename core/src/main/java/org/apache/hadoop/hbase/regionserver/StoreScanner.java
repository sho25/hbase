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
name|HFileScanner
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
name|List
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
name|NavigableSet
import|;
end_import

begin_comment
comment|/**  * Scanner scans both the memstore and the HStore. Coaleace KeyValue stream  * into List<KeyValue> for a single row.  */
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
comment|/**    * Opens a scanner across memstore, snapshot, and all StoreFiles.    *    * @param store who we scan    * @param scan the spec    * @param columns which columns we are scanning    */
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
name|List
argument_list|<
name|KeyValueScanner
argument_list|>
name|scanners
init|=
name|getScanners
argument_list|()
decl_stmt|;
comment|// Seek all scanners to the initial key
comment|// TODO if scan.isGetScan, use bloomfilters to skip seeking
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
operator|.
name|toArray
argument_list|(
operator|new
name|KeyValueScanner
index|[
name|scanners
operator|.
name|size
argument_list|()
index|]
argument_list|)
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
name|KeyValueScanner
index|[]
name|scanners
parameter_list|)
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
name|KeyValueScanner
index|[]
name|scanners
parameter_list|)
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
block|{
name|List
argument_list|<
name|KeyValueScanner
argument_list|>
name|scanners
init|=
name|getStoreFileScanners
argument_list|()
decl_stmt|;
name|KeyValueScanner
index|[]
name|memstorescanners
init|=
name|this
operator|.
name|store
operator|.
name|memstore
operator|.
name|getScanners
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
name|memstorescanners
operator|.
name|length
operator|-
literal|1
init|;
name|i
operator|>=
literal|0
condition|;
name|i
operator|--
control|)
block|{
name|scanners
operator|.
name|add
argument_list|(
name|memstorescanners
index|[
name|i
index|]
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
name|this
operator|.
name|heap
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
specifier|public
specifier|synchronized
name|boolean
name|seek
parameter_list|(
name|KeyValue
name|key
parameter_list|)
block|{
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
name|QueryMatcher
operator|.
name|MatchCode
name|qcode
init|=
name|matcher
operator|.
name|match
argument_list|(
name|kv
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
name|KeyValue
name|next
init|=
name|this
operator|.
name|heap
operator|.
name|next
argument_list|()
decl_stmt|;
name|results
operator|.
name|add
argument_list|(
name|next
argument_list|)
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
name|heap
operator|.
name|next
argument_list|()
expr_stmt|;
break|break;
case|case
name|SEEK_NEXT_COL
case|:
comment|// TODO hfile needs 'hinted' seeking to prevent it from
comment|// reseeking from the start of the block on every dang seek.
comment|// We need that API and expose it the scanner chain.
name|heap
operator|.
name|next
argument_list|()
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
specifier|private
name|List
argument_list|<
name|KeyValueScanner
argument_list|>
name|getStoreFileScanners
parameter_list|()
block|{
name|List
argument_list|<
name|HFileScanner
argument_list|>
name|s
init|=
operator|new
name|ArrayList
argument_list|<
name|HFileScanner
argument_list|>
argument_list|(
name|this
operator|.
name|store
operator|.
name|getStorefilesCount
argument_list|()
argument_list|)
decl_stmt|;
name|Map
argument_list|<
name|Long
argument_list|,
name|StoreFile
argument_list|>
name|map
init|=
name|this
operator|.
name|store
operator|.
name|getStorefiles
argument_list|()
operator|.
name|descendingMap
argument_list|()
decl_stmt|;
for|for
control|(
name|StoreFile
name|sf
range|:
name|map
operator|.
name|values
argument_list|()
control|)
block|{
name|HFile
operator|.
name|Reader
name|r
init|=
name|sf
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
literal|"StoreFile "
operator|+
name|sf
operator|+
literal|" has null Reader"
argument_list|)
expr_stmt|;
continue|continue;
block|}
comment|// If isGet, use pread, else false, dont use pread
name|s
operator|.
name|add
argument_list|(
name|r
operator|.
name|getScanner
argument_list|(
name|this
operator|.
name|cacheBlocks
argument_list|,
name|isGet
argument_list|)
argument_list|)
expr_stmt|;
block|}
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
name|s
operator|.
name|size
argument_list|()
operator|+
literal|1
argument_list|)
decl_stmt|;
for|for
control|(
name|HFileScanner
name|hfs
range|:
name|s
control|)
block|{
name|scanners
operator|.
name|add
argument_list|(
operator|new
name|StoreFileScanner
argument_list|(
name|hfs
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|scanners
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
name|KeyValue
name|topKey
init|=
name|this
operator|.
name|peek
argument_list|()
decl_stmt|;
if|if
condition|(
name|topKey
operator|==
literal|null
condition|)
return|return;
name|List
argument_list|<
name|KeyValueScanner
argument_list|>
name|scanners
init|=
name|getScanners
argument_list|()
decl_stmt|;
comment|// close the previous scanners:
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
name|topKey
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
operator|.
name|toArray
argument_list|(
operator|new
name|KeyValueScanner
index|[
name|scanners
operator|.
name|size
argument_list|()
index|]
argument_list|)
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
name|topKey
else|:
name|kv
operator|)
operator|.
name|getRow
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

