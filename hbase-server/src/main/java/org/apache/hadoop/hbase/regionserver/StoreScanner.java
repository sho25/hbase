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
name|hbase
operator|.
name|DoNotRetryIOException
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
name|filter
operator|.
name|Filter
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
name|HStore
operator|.
name|ScanInfo
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
name|metrics
operator|.
name|RegionMetricsStorage
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
name|metrics
operator|.
name|SchemaMetrics
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
name|hbase
operator|.
name|util
operator|.
name|EnvironmentEdgeManager
import|;
end_import

begin_comment
comment|/**  * Scanner scans both the memstore and the HStore. Coalesce KeyValue stream  * into List<KeyValue> for a single row.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|StoreScanner
extends|extends
name|NonLazyKeyValueScanner
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
name|HStore
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
specifier|private
name|int
name|countPerRow
init|=
literal|0
decl_stmt|;
specifier|private
name|int
name|storeLimit
init|=
operator|-
literal|1
decl_stmt|;
specifier|private
name|int
name|storeOffset
init|=
literal|0
decl_stmt|;
specifier|private
name|String
name|metricNamePrefix
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
specifier|private
specifier|final
name|boolean
name|explicitColumnQuery
decl_stmt|;
specifier|private
specifier|final
name|boolean
name|useRowColBloom
decl_stmt|;
specifier|private
specifier|final
name|Scan
name|scan
decl_stmt|;
specifier|private
specifier|final
name|NavigableSet
argument_list|<
name|byte
index|[]
argument_list|>
name|columns
decl_stmt|;
specifier|private
specifier|final
name|long
name|oldestUnexpiredTS
decl_stmt|;
specifier|private
specifier|final
name|int
name|minVersions
decl_stmt|;
comment|/** We don't ever expect to change this, the constant is just for clarity. */
specifier|static
specifier|final
name|boolean
name|LAZY_SEEK_ENABLED_BY_DEFAULT
init|=
literal|true
decl_stmt|;
comment|/** Used during unit testing to ensure that lazy seek does save seek ops */
specifier|private
specifier|static
name|boolean
name|lazySeekEnabledGlobally
init|=
name|LAZY_SEEK_ENABLED_BY_DEFAULT
decl_stmt|;
comment|// if heap == null and lastTop != null, you need to reseek given the key below
specifier|private
name|KeyValue
name|lastTop
init|=
literal|null
decl_stmt|;
comment|/** An internal constructor. */
specifier|private
name|StoreScanner
parameter_list|(
name|HStore
name|store
parameter_list|,
name|boolean
name|cacheBlocks
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
parameter_list|,
name|long
name|ttl
parameter_list|,
name|int
name|minVersions
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
name|cacheBlocks
expr_stmt|;
name|isGet
operator|=
name|scan
operator|.
name|isGetScan
argument_list|()
expr_stmt|;
name|int
name|numCol
init|=
name|columns
operator|==
literal|null
condition|?
literal|0
else|:
name|columns
operator|.
name|size
argument_list|()
decl_stmt|;
name|explicitColumnQuery
operator|=
name|numCol
operator|>
literal|0
expr_stmt|;
name|this
operator|.
name|scan
operator|=
name|scan
expr_stmt|;
name|this
operator|.
name|columns
operator|=
name|columns
expr_stmt|;
name|oldestUnexpiredTS
operator|=
name|EnvironmentEdgeManager
operator|.
name|currentTimeMillis
argument_list|()
operator|-
name|ttl
expr_stmt|;
name|this
operator|.
name|minVersions
operator|=
name|minVersions
expr_stmt|;
comment|// We look up row-column Bloom filters for multi-column queries as part of
comment|// the seek operation. However, we also look the row-column Bloom filter
comment|// for multi-row (non-"get") scans because this is not done in
comment|// StoreFile.passesBloomFilter(Scan, SortedSet<byte[]>).
name|useRowColBloom
operator|=
name|numCol
operator|>
literal|1
operator|||
operator|(
operator|!
name|isGet
operator|&&
name|numCol
operator|==
literal|1
operator|)
expr_stmt|;
block|}
comment|/**    * Opens a scanner across memstore, snapshot, and all StoreFiles. Assumes we    * are not in a compaction.    *    * @param store who we scan    * @param scan the spec    * @param columns which columns we are scanning    * @throws IOException    */
specifier|public
name|StoreScanner
parameter_list|(
name|HStore
name|store
parameter_list|,
name|ScanInfo
name|scanInfo
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
argument_list|(
name|store
argument_list|,
name|scan
operator|.
name|getCacheBlocks
argument_list|()
argument_list|,
name|scan
argument_list|,
name|columns
argument_list|,
name|scanInfo
operator|.
name|getTtl
argument_list|()
argument_list|,
name|scanInfo
operator|.
name|getMinVersions
argument_list|()
argument_list|)
expr_stmt|;
name|initializeMetricNames
argument_list|()
expr_stmt|;
if|if
condition|(
name|columns
operator|!=
literal|null
operator|&&
name|scan
operator|.
name|isRaw
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|DoNotRetryIOException
argument_list|(
literal|"Cannot specify any column for a raw scan"
argument_list|)
throw|;
block|}
name|matcher
operator|=
operator|new
name|ScanQueryMatcher
argument_list|(
name|scan
argument_list|,
name|scanInfo
argument_list|,
name|columns
argument_list|,
name|ScanType
operator|.
name|USER_SCAN
argument_list|,
name|Long
operator|.
name|MAX_VALUE
argument_list|,
name|HConstants
operator|.
name|LATEST_TIMESTAMP
argument_list|,
name|oldestUnexpiredTS
argument_list|)
expr_stmt|;
comment|// Pass columns to try to filter out unnecessary StoreFiles.
name|List
argument_list|<
name|KeyValueScanner
argument_list|>
name|scanners
init|=
name|getScannersNoCompaction
argument_list|()
decl_stmt|;
comment|// Seek all scanners to the start of the Row (or if the exact matching row
comment|// key does not exist, then to the start of the next matching Row).
comment|// Always check bloom filter to optimize the top row seek for delete
comment|// family marker.
if|if
condition|(
name|explicitColumnQuery
operator|&&
name|lazySeekEnabledGlobally
condition|)
block|{
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
name|requestSeek
argument_list|(
name|matcher
operator|.
name|getStartKey
argument_list|()
argument_list|,
literal|false
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
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
block|}
comment|// set storeLimit
name|this
operator|.
name|storeLimit
operator|=
name|scan
operator|.
name|getMaxResultsPerColumnFamily
argument_list|()
expr_stmt|;
comment|// set rowOffset
name|this
operator|.
name|storeOffset
operator|=
name|scan
operator|.
name|getRowOffsetPerColumnFamily
argument_list|()
expr_stmt|;
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
comment|/**    * Used for major compactions.<p>    *    * Opens a scanner across specified StoreFiles.    * @param store who we scan    * @param scan the spec    * @param scanners ancillary scanners    * @param smallestReadPoint the readPoint that we should use for tracking    *          versions    */
specifier|public
name|StoreScanner
parameter_list|(
name|HStore
name|store
parameter_list|,
name|ScanInfo
name|scanInfo
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
name|ScanType
name|scanType
parameter_list|,
name|long
name|smallestReadPoint
parameter_list|,
name|long
name|earliestPutTs
parameter_list|)
throws|throws
name|IOException
block|{
name|this
argument_list|(
name|store
argument_list|,
literal|false
argument_list|,
name|scan
argument_list|,
literal|null
argument_list|,
name|scanInfo
operator|.
name|getTtl
argument_list|()
argument_list|,
name|scanInfo
operator|.
name|getMinVersions
argument_list|()
argument_list|)
expr_stmt|;
name|initializeMetricNames
argument_list|()
expr_stmt|;
name|matcher
operator|=
operator|new
name|ScanQueryMatcher
argument_list|(
name|scan
argument_list|,
name|scanInfo
argument_list|,
literal|null
argument_list|,
name|scanType
argument_list|,
name|smallestReadPoint
argument_list|,
name|earliestPutTs
argument_list|,
name|oldestUnexpiredTS
argument_list|)
expr_stmt|;
comment|// Filter the list of scanners using Bloom filters, time range, TTL, etc.
name|scanners
operator|=
name|selectScannersFrom
argument_list|(
name|scanners
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
comment|/** Constructor for testing. */
name|StoreScanner
parameter_list|(
specifier|final
name|Scan
name|scan
parameter_list|,
name|HStore
operator|.
name|ScanInfo
name|scanInfo
parameter_list|,
name|ScanType
name|scanType
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
argument_list|(
name|scan
argument_list|,
name|scanInfo
argument_list|,
name|scanType
argument_list|,
name|columns
argument_list|,
name|scanners
argument_list|,
name|HConstants
operator|.
name|LATEST_TIMESTAMP
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
name|HStore
operator|.
name|ScanInfo
name|scanInfo
parameter_list|,
name|ScanType
name|scanType
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
parameter_list|,
name|long
name|earliestPutTs
parameter_list|)
throws|throws
name|IOException
block|{
name|this
argument_list|(
literal|null
argument_list|,
name|scan
operator|.
name|getCacheBlocks
argument_list|()
argument_list|,
name|scan
argument_list|,
name|columns
argument_list|,
name|scanInfo
operator|.
name|getTtl
argument_list|()
argument_list|,
name|scanInfo
operator|.
name|getMinVersions
argument_list|()
argument_list|)
expr_stmt|;
name|this
operator|.
name|initializeMetricNames
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
name|scanInfo
argument_list|,
name|columns
argument_list|,
name|scanType
argument_list|,
name|Long
operator|.
name|MAX_VALUE
argument_list|,
name|earliestPutTs
argument_list|,
name|oldestUnexpiredTS
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
name|scanInfo
operator|.
name|getComparator
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**    * Method used internally to initialize metric names throughout the    * constructors.    *    * To be called after the store variable has been initialized!    */
specifier|private
name|void
name|initializeMetricNames
parameter_list|()
block|{
name|String
name|tableName
init|=
name|SchemaMetrics
operator|.
name|UNKNOWN
decl_stmt|;
name|String
name|family
init|=
name|SchemaMetrics
operator|.
name|UNKNOWN
decl_stmt|;
if|if
condition|(
name|store
operator|!=
literal|null
condition|)
block|{
name|tableName
operator|=
name|store
operator|.
name|getTableName
argument_list|()
expr_stmt|;
name|family
operator|=
name|Bytes
operator|.
name|toString
argument_list|(
name|store
operator|.
name|getFamily
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|this
operator|.
name|metricNamePrefix
operator|=
name|SchemaMetrics
operator|.
name|generateSchemaMetricsPrefix
argument_list|(
name|tableName
argument_list|,
name|family
argument_list|)
expr_stmt|;
block|}
comment|/**    * Get a filtered list of scanners. Assumes we are not in a compaction.    * @return list of scanners to seek    */
specifier|private
name|List
argument_list|<
name|KeyValueScanner
argument_list|>
name|getScannersNoCompaction
parameter_list|()
throws|throws
name|IOException
block|{
specifier|final
name|boolean
name|isCompaction
init|=
literal|false
decl_stmt|;
return|return
name|selectScannersFrom
argument_list|(
name|store
operator|.
name|getScanners
argument_list|(
name|cacheBlocks
argument_list|,
name|isGet
argument_list|,
name|isCompaction
argument_list|,
name|matcher
argument_list|)
argument_list|)
return|;
block|}
comment|/**    * Filters the given list of scanners using Bloom filter, time range, and    * TTL.    */
specifier|private
name|List
argument_list|<
name|KeyValueScanner
argument_list|>
name|selectScannersFrom
parameter_list|(
specifier|final
name|List
argument_list|<
name|?
extends|extends
name|KeyValueScanner
argument_list|>
name|allScanners
parameter_list|)
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
name|ArrayList
argument_list|<
name|KeyValueScanner
argument_list|>
argument_list|(
name|allScanners
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
comment|// We can only exclude store files based on TTL if minVersions is set to 0.
comment|// Otherwise, we might have to return KVs that have technically expired.
name|long
name|expiredTimestampCutoff
init|=
name|minVersions
operator|==
literal|0
condition|?
name|oldestUnexpiredTS
else|:
name|Long
operator|.
name|MIN_VALUE
decl_stmt|;
comment|// include only those scan files which pass all filters
for|for
control|(
name|KeyValueScanner
name|kvs
range|:
name|allScanners
control|)
block|{
name|boolean
name|isFile
init|=
name|kvs
operator|.
name|isFileScanner
argument_list|()
decl_stmt|;
if|if
condition|(
operator|(
operator|!
name|isFile
operator|&&
name|filesOnly
operator|)
operator|||
operator|(
name|isFile
operator|&&
name|memOnly
operator|)
condition|)
block|{
continue|continue;
block|}
if|if
condition|(
name|kvs
operator|.
name|shouldUseScanner
argument_list|(
name|scan
argument_list|,
name|columns
argument_list|,
name|expiredTimestampCutoff
argument_list|)
condition|)
block|{
name|scanners
operator|.
name|add
argument_list|(
name|kvs
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|scanners
return|;
block|}
annotation|@
name|Override
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
annotation|@
name|Override
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
annotation|@
name|Override
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
annotation|@
name|Override
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
name|getScannersNoCompaction
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
annotation|@
name|Override
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
return|return
name|next
argument_list|(
name|outResult
argument_list|,
name|limit
argument_list|,
literal|null
argument_list|)
return|;
block|}
comment|/**    * Get the next row of values from this Store.    * @param outResult    * @param limit    * @return true if there are more rows, false if scanner is done    */
annotation|@
name|Override
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
parameter_list|,
name|String
name|metric
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|checkReseek
argument_list|()
condition|)
block|{
return|return
literal|true
return|;
block|}
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
name|this
operator|.
name|countPerRow
operator|=
literal|0
expr_stmt|;
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
name|KeyValue
name|prevKV
init|=
literal|null
decl_stmt|;
comment|// Only do a sanity-check if store and comparator are available.
name|KeyValue
operator|.
name|KVComparator
name|comparator
init|=
name|store
operator|!=
literal|null
condition|?
name|store
operator|.
name|getComparator
argument_list|()
else|:
literal|null
decl_stmt|;
name|long
name|cumulativeMetric
init|=
literal|0
decl_stmt|;
name|int
name|count
init|=
literal|0
decl_stmt|;
try|try
block|{
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
comment|// Check that the heap gives us KVs in an increasing order.
assert|assert
name|prevKV
operator|==
literal|null
operator|||
name|comparator
operator|==
literal|null
operator|||
name|comparator
operator|.
name|compare
argument_list|(
name|prevKV
argument_list|,
name|kv
argument_list|)
operator|<=
literal|0
operator|:
literal|"Key "
operator|+
name|prevKV
operator|+
literal|" followed by a "
operator|+
literal|"smaller key "
operator|+
name|kv
operator|+
literal|" in cf "
operator|+
name|store
assert|;
name|prevKV
operator|=
name|kv
expr_stmt|;
name|ScanQueryMatcher
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
switch|switch
condition|(
name|qcode
condition|)
block|{
case|case
name|INCLUDE
case|:
case|case
name|INCLUDE_AND_SEEK_NEXT_ROW
case|:
case|case
name|INCLUDE_AND_SEEK_NEXT_COL
case|:
name|Filter
name|f
init|=
name|matcher
operator|.
name|getFilter
argument_list|()
decl_stmt|;
if|if
condition|(
name|f
operator|!=
literal|null
condition|)
block|{
name|kv
operator|=
name|f
operator|.
name|transform
argument_list|(
name|kv
argument_list|)
expr_stmt|;
block|}
name|this
operator|.
name|countPerRow
operator|++
expr_stmt|;
if|if
condition|(
name|storeLimit
operator|>
operator|-
literal|1
operator|&&
name|this
operator|.
name|countPerRow
operator|>
operator|(
name|storeLimit
operator|+
name|storeOffset
operator|)
condition|)
block|{
comment|// do what SEEK_NEXT_ROW does.
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
break|break
name|LOOP
break|;
block|}
comment|// add to results only if we have skipped #storeOffset kvs
comment|// also update metric accordingly
if|if
condition|(
name|this
operator|.
name|countPerRow
operator|>
name|storeOffset
condition|)
block|{
if|if
condition|(
name|metric
operator|!=
literal|null
condition|)
block|{
name|cumulativeMetric
operator|+=
name|kv
operator|.
name|getLength
argument_list|()
expr_stmt|;
block|}
name|outResult
operator|.
name|add
argument_list|(
name|kv
argument_list|)
expr_stmt|;
name|count
operator|++
expr_stmt|;
block|}
if|if
condition|(
name|qcode
operator|==
name|ScanQueryMatcher
operator|.
name|MatchCode
operator|.
name|INCLUDE_AND_SEEK_NEXT_ROW
condition|)
block|{
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
block|}
elseif|else
if|if
condition|(
name|qcode
operator|==
name|ScanQueryMatcher
operator|.
name|MatchCode
operator|.
name|INCLUDE_AND_SEEK_NEXT_COL
condition|)
block|{
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
block|}
else|else
block|{
name|this
operator|.
name|heap
operator|.
name|next
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|limit
operator|>
literal|0
operator|&&
operator|(
name|count
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
return|return
literal|true
return|;
case|case
name|DONE_SCAN
case|:
name|close
argument_list|()
expr_stmt|;
return|return
literal|false
return|;
case|case
name|SEEK_NEXT_ROW
case|:
comment|// This is just a relatively simple end of scan fix, to short-cut end
comment|// us if there is an endKey in the scan.
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
block|}
finally|finally
block|{
if|if
condition|(
name|cumulativeMetric
operator|>
literal|0
operator|&&
name|metric
operator|!=
literal|null
condition|)
block|{
name|RegionMetricsStorage
operator|.
name|incrNumericMetric
argument_list|(
name|this
operator|.
name|metricNamePrefix
operator|+
name|metric
argument_list|,
name|cumulativeMetric
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|count
operator|>
literal|0
condition|)
block|{
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
annotation|@
name|Override
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
argument_list|,
literal|null
argument_list|)
return|;
block|}
annotation|@
name|Override
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
name|String
name|metric
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
argument_list|,
name|metric
argument_list|)
return|;
block|}
comment|// Implementation of ChangedReadersObserver
annotation|@
name|Override
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
comment|/**    * @return true if top of heap has changed (and KeyValueHeap has to try the    *         next KV)    * @throws IOException    */
specifier|private
name|boolean
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
if|if
condition|(
name|this
operator|.
name|heap
operator|.
name|peek
argument_list|()
operator|==
literal|null
operator|||
name|store
operator|.
name|comparator
operator|.
name|compareRows
argument_list|(
name|this
operator|.
name|lastTop
argument_list|,
name|this
operator|.
name|heap
operator|.
name|peek
argument_list|()
argument_list|)
operator|!=
literal|0
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Storescanner.peek() is changed where before = "
operator|+
name|this
operator|.
name|lastTop
operator|.
name|toString
argument_list|()
operator|+
literal|",and after = "
operator|+
name|this
operator|.
name|heap
operator|.
name|peek
argument_list|()
argument_list|)
expr_stmt|;
name|this
operator|.
name|lastTop
operator|=
literal|null
expr_stmt|;
return|return
literal|true
return|;
block|}
name|this
operator|.
name|lastTop
operator|=
literal|null
expr_stmt|;
comment|// gone!
block|}
comment|// else dont need to reseek
return|return
literal|false
return|;
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
name|getScannersNoCompaction
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
comment|// Reset the state of the Query Matcher and set to top row.
comment|// Only reset and call setRow if the row changes; avoids confusing the
comment|// query matcher if scanning intra-row.
name|KeyValue
name|kv
init|=
name|heap
operator|.
name|peek
argument_list|()
decl_stmt|;
if|if
condition|(
name|kv
operator|==
literal|null
condition|)
block|{
name|kv
operator|=
name|lastTopKey
expr_stmt|;
block|}
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
name|kv
operator|.
name|matchingRow
argument_list|(
name|matcher
operator|.
name|row
argument_list|)
condition|)
block|{
name|this
operator|.
name|countPerRow
operator|=
literal|0
expr_stmt|;
name|matcher
operator|.
name|reset
argument_list|()
expr_stmt|;
name|matcher
operator|.
name|setRow
argument_list|(
name|kv
operator|.
name|getRow
argument_list|()
argument_list|)
expr_stmt|;
block|}
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
comment|//Heap will not be null, if this is called from next() which.
comment|//If called from RegionScanner.reseek(...) make sure the scanner
comment|//stack is reset if needed.
name|checkReseek
argument_list|()
expr_stmt|;
if|if
condition|(
name|explicitColumnQuery
operator|&&
name|lazySeekEnabledGlobally
condition|)
block|{
return|return
name|heap
operator|.
name|requestSeek
argument_list|(
name|kv
argument_list|,
literal|true
argument_list|,
name|useRowColBloom
argument_list|)
return|;
block|}
else|else
block|{
return|return
name|heap
operator|.
name|reseek
argument_list|(
name|kv
argument_list|)
return|;
block|}
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
comment|/**    * Used in testing.    * @return all scanners in no particular order    */
name|List
argument_list|<
name|KeyValueScanner
argument_list|>
name|getAllScannersForTesting
parameter_list|()
block|{
name|List
argument_list|<
name|KeyValueScanner
argument_list|>
name|allScanners
init|=
operator|new
name|ArrayList
argument_list|<
name|KeyValueScanner
argument_list|>
argument_list|()
decl_stmt|;
name|KeyValueScanner
name|current
init|=
name|heap
operator|.
name|getCurrentForTesting
argument_list|()
decl_stmt|;
if|if
condition|(
name|current
operator|!=
literal|null
condition|)
name|allScanners
operator|.
name|add
argument_list|(
name|current
argument_list|)
expr_stmt|;
for|for
control|(
name|KeyValueScanner
name|scanner
range|:
name|heap
operator|.
name|getHeap
argument_list|()
control|)
name|allScanners
operator|.
name|add
argument_list|(
name|scanner
argument_list|)
expr_stmt|;
return|return
name|allScanners
return|;
block|}
specifier|static
name|void
name|enableLazySeekGlobally
parameter_list|(
name|boolean
name|enable
parameter_list|)
block|{
name|lazySeekEnabledGlobally
operator|=
name|enable
expr_stmt|;
block|}
block|}
end_class

end_unit

