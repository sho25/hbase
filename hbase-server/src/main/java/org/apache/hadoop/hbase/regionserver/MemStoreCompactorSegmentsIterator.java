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
name|ArrayList
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Iterator
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
name|NoSuchElementException
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
name|coprocessor
operator|.
name|CoprocessorException
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
name|common
operator|.
name|io
operator|.
name|Closeables
import|;
end_import

begin_comment
comment|/**  * The MemStoreCompactorSegmentsIterator extends MemStoreSegmentsIterator  * and performs the scan for compaction operation meaning it is based on SQM  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|MemStoreCompactorSegmentsIterator
extends|extends
name|MemStoreSegmentsIterator
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
name|MemStoreCompactorSegmentsIterator
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|List
argument_list|<
name|Cell
argument_list|>
name|kvs
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
specifier|private
name|boolean
name|hasMore
init|=
literal|true
decl_stmt|;
specifier|private
name|Iterator
argument_list|<
name|Cell
argument_list|>
name|kvsIterator
decl_stmt|;
comment|// scanner on top of pipeline scanner that uses ScanQueryMatcher
specifier|private
name|InternalScanner
name|compactingScanner
decl_stmt|;
comment|// C-tor
specifier|public
name|MemStoreCompactorSegmentsIterator
parameter_list|(
name|List
argument_list|<
name|ImmutableSegment
argument_list|>
name|segments
parameter_list|,
name|CellComparator
name|comparator
parameter_list|,
name|int
name|compactionKVMax
parameter_list|,
name|HStore
name|store
parameter_list|)
throws|throws
name|IOException
block|{
name|super
argument_list|(
name|compactionKVMax
argument_list|)
expr_stmt|;
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
argument_list|()
decl_stmt|;
name|AbstractMemStore
operator|.
name|addToScanners
argument_list|(
name|segments
argument_list|,
name|Long
operator|.
name|MAX_VALUE
argument_list|,
name|scanners
argument_list|)
expr_stmt|;
comment|// build the scanner based on Query Matcher
comment|// reinitialize the compacting scanner for each instance of iterator
name|compactingScanner
operator|=
name|createScanner
argument_list|(
name|store
argument_list|,
name|scanners
argument_list|)
expr_stmt|;
name|refillKVS
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|hasNext
parameter_list|()
block|{
if|if
condition|(
name|kvsIterator
operator|==
literal|null
condition|)
block|{
comment|// for the case when the result is empty
return|return
literal|false
return|;
block|}
comment|// return true either we have cells in buffer or we can get more.
return|return
name|kvsIterator
operator|.
name|hasNext
argument_list|()
operator|||
name|refillKVS
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|Cell
name|next
parameter_list|()
block|{
if|if
condition|(
operator|!
name|hasNext
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|NoSuchElementException
argument_list|()
throw|;
block|}
return|return
name|kvsIterator
operator|.
name|next
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|()
block|{
try|try
block|{
name|compactingScanner
operator|.
name|close
argument_list|()
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
name|warn
argument_list|(
literal|"close store scanner failed"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
name|compactingScanner
operator|=
literal|null
expr_stmt|;
name|kvs
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|remove
parameter_list|()
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
comment|/**    * Creates the scanner for compacting the pipeline.    * @return the scanner    */
specifier|private
name|InternalScanner
name|createScanner
parameter_list|(
name|HStore
name|store
parameter_list|,
name|List
argument_list|<
name|KeyValueScanner
argument_list|>
name|scanners
parameter_list|)
throws|throws
name|IOException
block|{
name|InternalScanner
name|scanner
init|=
literal|null
decl_stmt|;
name|boolean
name|success
init|=
literal|false
decl_stmt|;
try|try
block|{
name|RegionCoprocessorHost
name|cpHost
init|=
name|store
operator|.
name|getCoprocessorHost
argument_list|()
decl_stmt|;
name|ScanInfo
name|scanInfo
decl_stmt|;
if|if
condition|(
name|cpHost
operator|!=
literal|null
condition|)
block|{
name|scanInfo
operator|=
name|cpHost
operator|.
name|preMemStoreCompactionCompactScannerOpen
argument_list|(
name|store
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|scanInfo
operator|=
name|store
operator|.
name|getScanInfo
argument_list|()
expr_stmt|;
block|}
name|scanner
operator|=
operator|new
name|StoreScanner
argument_list|(
name|store
argument_list|,
name|scanInfo
argument_list|,
name|scanners
argument_list|,
name|ScanType
operator|.
name|COMPACT_RETAIN_DELETES
argument_list|,
name|store
operator|.
name|getSmallestReadPoint
argument_list|()
argument_list|,
name|HConstants
operator|.
name|OLDEST_TIMESTAMP
argument_list|)
expr_stmt|;
if|if
condition|(
name|cpHost
operator|!=
literal|null
condition|)
block|{
name|InternalScanner
name|scannerFromCp
init|=
name|cpHost
operator|.
name|preMemStoreCompactionCompact
argument_list|(
name|store
argument_list|,
name|scanner
argument_list|)
decl_stmt|;
if|if
condition|(
name|scannerFromCp
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|CoprocessorException
argument_list|(
literal|"Got a null InternalScanner when calling"
operator|+
literal|" preMemStoreCompactionCompact which is not acceptable"
argument_list|)
throw|;
block|}
name|success
operator|=
literal|true
expr_stmt|;
return|return
name|scannerFromCp
return|;
block|}
else|else
block|{
name|success
operator|=
literal|true
expr_stmt|;
return|return
name|scanner
return|;
block|}
block|}
finally|finally
block|{
if|if
condition|(
operator|!
name|success
condition|)
block|{
name|Closeables
operator|.
name|close
argument_list|(
name|scanner
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|scanners
operator|.
name|forEach
argument_list|(
name|KeyValueScanner
operator|::
name|close
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|/*    * Refill kev-value set (should be invoked only when KVS is empty) Returns true if KVS is    * non-empty    */
specifier|private
name|boolean
name|refillKVS
parameter_list|()
block|{
comment|// if there is nothing expected next in compactingScanner
if|if
condition|(
operator|!
name|hasMore
condition|)
block|{
return|return
literal|false
return|;
block|}
comment|// clear previous KVS, first initiated in the constructor
name|kvs
operator|.
name|clear
argument_list|()
expr_stmt|;
for|for
control|(
init|;
condition|;
control|)
block|{
try|try
block|{
name|hasMore
operator|=
name|compactingScanner
operator|.
name|next
argument_list|(
name|kvs
argument_list|,
name|scannerContext
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
comment|// should not happen as all data are in memory
throw|throw
operator|new
name|IllegalStateException
argument_list|(
name|e
argument_list|)
throw|;
block|}
if|if
condition|(
operator|!
name|kvs
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|kvsIterator
operator|=
name|kvs
operator|.
name|iterator
argument_list|()
expr_stmt|;
return|return
literal|true
return|;
block|}
elseif|else
if|if
condition|(
operator|!
name|hasMore
condition|)
block|{
return|return
literal|false
return|;
block|}
block|}
block|}
block|}
end_class

end_unit

