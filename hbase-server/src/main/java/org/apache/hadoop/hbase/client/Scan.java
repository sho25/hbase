begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|client
package|;
end_package

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
name|classification
operator|.
name|InterfaceStability
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
name|filter
operator|.
name|IncompatibleFilterException
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
name|util
operator|.
name|Bytes
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
name|HashMap
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

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|TreeMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|TreeSet
import|;
end_import

begin_comment
comment|/**  * Used to perform Scan operations.  *<p>  * All operations are identical to {@link Get} with the exception of  * instantiation.  Rather than specifying a single row, an optional startRow  * and stopRow may be defined.  If rows are not specified, the Scanner will  * iterate over all rows.  *<p>  * To scan everything for each row, instantiate a Scan object.  *<p>  * To modify scanner caching for just this scan, use {@link #setCaching(int) setCaching}.  * If caching is NOT set, we will use the caching value of the hosting {@link HTable}.  See  * {@link HTable#setScannerCaching(int)}. In addition to row caching, it is possible to specify a  * maximum result size, using {@link #setMaxResultSize(long)}. When both are used,  * single server requests are limited by either number of rows or maximum result size, whichever  * limit comes first.  *<p>  * To further define the scope of what to get when scanning, perform additional  * methods as outlined below.  *<p>  * To get all columns from specific families, execute {@link #addFamily(byte[]) addFamily}  * for each family to retrieve.  *<p>  * To get specific columns, execute {@link #addColumn(byte[], byte[]) addColumn}  * for each column to retrieve.  *<p>  * To only retrieve columns within a specific range of version timestamps,  * execute {@link #setTimeRange(long, long) setTimeRange}.  *<p>  * To only retrieve columns with a specific timestamp, execute  * {@link #setTimeStamp(long) setTimestamp}.  *<p>  * To limit the number of versions of each column to be returned, execute  * {@link #setMaxVersions(int) setMaxVersions}.  *<p>  * To limit the maximum number of values returned for each call to next(),  * execute {@link #setBatch(int) setBatch}.  *<p>  * To add a filter, execute {@link #setFilter(org.apache.hadoop.hbase.filter.Filter) setFilter}.  *<p>  * Expert: To explicitly disable server-side block caching for this scan,  * execute {@link #setCacheBlocks(boolean)}.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Public
annotation|@
name|InterfaceStability
operator|.
name|Stable
specifier|public
class|class
name|Scan
extends|extends
name|OperationWithAttributes
block|{
specifier|private
specifier|static
specifier|final
name|String
name|RAW_ATTR
init|=
literal|"_raw_"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|ISOLATION_LEVEL
init|=
literal|"_isolationlevel_"
decl_stmt|;
specifier|private
name|byte
index|[]
name|startRow
init|=
name|HConstants
operator|.
name|EMPTY_START_ROW
decl_stmt|;
specifier|private
name|byte
index|[]
name|stopRow
init|=
name|HConstants
operator|.
name|EMPTY_END_ROW
decl_stmt|;
specifier|private
name|int
name|maxVersions
init|=
literal|1
decl_stmt|;
specifier|private
name|int
name|batch
init|=
operator|-
literal|1
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
comment|// If application wants to collect scan metrics, it needs to
comment|// call scan.setAttribute(SCAN_ATTRIBUTES_ENABLE, Bytes.toBytes(Boolean.TRUE))
specifier|static
specifier|public
specifier|final
name|String
name|SCAN_ATTRIBUTES_METRICS_ENABLE
init|=
literal|"scan.attributes.metrics.enable"
decl_stmt|;
specifier|static
specifier|public
specifier|final
name|String
name|SCAN_ATTRIBUTES_METRICS_DATA
init|=
literal|"scan.attributes.metrics.data"
decl_stmt|;
comment|/*    * -1 means no caching    */
specifier|private
name|int
name|caching
init|=
operator|-
literal|1
decl_stmt|;
specifier|private
name|long
name|maxResultSize
init|=
operator|-
literal|1
decl_stmt|;
specifier|private
name|boolean
name|cacheBlocks
init|=
literal|true
decl_stmt|;
specifier|private
name|Filter
name|filter
init|=
literal|null
decl_stmt|;
specifier|private
name|TimeRange
name|tr
init|=
operator|new
name|TimeRange
argument_list|()
decl_stmt|;
specifier|private
name|Map
argument_list|<
name|byte
index|[]
argument_list|,
name|NavigableSet
argument_list|<
name|byte
index|[]
argument_list|>
argument_list|>
name|familyMap
init|=
operator|new
name|TreeMap
argument_list|<
name|byte
index|[]
argument_list|,
name|NavigableSet
argument_list|<
name|byte
index|[]
argument_list|>
argument_list|>
argument_list|(
name|Bytes
operator|.
name|BYTES_COMPARATOR
argument_list|)
decl_stmt|;
comment|/**    * Create a Scan operation across all rows.    */
specifier|public
name|Scan
parameter_list|()
block|{}
specifier|public
name|Scan
parameter_list|(
name|byte
index|[]
name|startRow
parameter_list|,
name|Filter
name|filter
parameter_list|)
block|{
name|this
argument_list|(
name|startRow
argument_list|)
expr_stmt|;
name|this
operator|.
name|filter
operator|=
name|filter
expr_stmt|;
block|}
comment|/**    * Create a Scan operation starting at the specified row.    *<p>    * If the specified row does not exist, the Scanner will start from the    * next closest row after the specified row.    * @param startRow row to start scanner at or after    */
specifier|public
name|Scan
parameter_list|(
name|byte
index|[]
name|startRow
parameter_list|)
block|{
name|this
operator|.
name|startRow
operator|=
name|startRow
expr_stmt|;
block|}
comment|/**    * Create a Scan operation for the range of rows specified.    * @param startRow row to start scanner at or after (inclusive)    * @param stopRow row to stop scanner before (exclusive)    */
specifier|public
name|Scan
parameter_list|(
name|byte
index|[]
name|startRow
parameter_list|,
name|byte
index|[]
name|stopRow
parameter_list|)
block|{
name|this
operator|.
name|startRow
operator|=
name|startRow
expr_stmt|;
name|this
operator|.
name|stopRow
operator|=
name|stopRow
expr_stmt|;
block|}
comment|/**    * Creates a new instance of this class while copying all values.    *    * @param scan  The scan instance to copy from.    * @throws IOException When copying the values fails.    */
specifier|public
name|Scan
parameter_list|(
name|Scan
name|scan
parameter_list|)
throws|throws
name|IOException
block|{
name|startRow
operator|=
name|scan
operator|.
name|getStartRow
argument_list|()
expr_stmt|;
name|stopRow
operator|=
name|scan
operator|.
name|getStopRow
argument_list|()
expr_stmt|;
name|maxVersions
operator|=
name|scan
operator|.
name|getMaxVersions
argument_list|()
expr_stmt|;
name|batch
operator|=
name|scan
operator|.
name|getBatch
argument_list|()
expr_stmt|;
name|storeLimit
operator|=
name|scan
operator|.
name|getMaxResultsPerColumnFamily
argument_list|()
expr_stmt|;
name|storeOffset
operator|=
name|scan
operator|.
name|getRowOffsetPerColumnFamily
argument_list|()
expr_stmt|;
name|caching
operator|=
name|scan
operator|.
name|getCaching
argument_list|()
expr_stmt|;
name|maxResultSize
operator|=
name|scan
operator|.
name|getMaxResultSize
argument_list|()
expr_stmt|;
name|cacheBlocks
operator|=
name|scan
operator|.
name|getCacheBlocks
argument_list|()
expr_stmt|;
name|filter
operator|=
name|scan
operator|.
name|getFilter
argument_list|()
expr_stmt|;
comment|// clone?
name|TimeRange
name|ctr
init|=
name|scan
operator|.
name|getTimeRange
argument_list|()
decl_stmt|;
name|tr
operator|=
operator|new
name|TimeRange
argument_list|(
name|ctr
operator|.
name|getMin
argument_list|()
argument_list|,
name|ctr
operator|.
name|getMax
argument_list|()
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|byte
index|[]
argument_list|,
name|NavigableSet
argument_list|<
name|byte
index|[]
argument_list|>
argument_list|>
name|fams
init|=
name|scan
operator|.
name|getFamilyMap
argument_list|()
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|byte
index|[]
argument_list|,
name|NavigableSet
argument_list|<
name|byte
index|[]
argument_list|>
argument_list|>
name|entry
range|:
name|fams
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|byte
index|[]
name|fam
init|=
name|entry
operator|.
name|getKey
argument_list|()
decl_stmt|;
name|NavigableSet
argument_list|<
name|byte
index|[]
argument_list|>
name|cols
init|=
name|entry
operator|.
name|getValue
argument_list|()
decl_stmt|;
if|if
condition|(
name|cols
operator|!=
literal|null
operator|&&
name|cols
operator|.
name|size
argument_list|()
operator|>
literal|0
condition|)
block|{
for|for
control|(
name|byte
index|[]
name|col
range|:
name|cols
control|)
block|{
name|addColumn
argument_list|(
name|fam
argument_list|,
name|col
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|addFamily
argument_list|(
name|fam
argument_list|)
expr_stmt|;
block|}
block|}
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|byte
index|[]
argument_list|>
name|attr
range|:
name|scan
operator|.
name|getAttributesMap
argument_list|()
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|setAttribute
argument_list|(
name|attr
operator|.
name|getKey
argument_list|()
argument_list|,
name|attr
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Builds a scan object with the same specs as get.    * @param get get to model scan after    */
specifier|public
name|Scan
parameter_list|(
name|Get
name|get
parameter_list|)
block|{
name|this
operator|.
name|startRow
operator|=
name|get
operator|.
name|getRow
argument_list|()
expr_stmt|;
name|this
operator|.
name|stopRow
operator|=
name|get
operator|.
name|getRow
argument_list|()
expr_stmt|;
name|this
operator|.
name|filter
operator|=
name|get
operator|.
name|getFilter
argument_list|()
expr_stmt|;
name|this
operator|.
name|cacheBlocks
operator|=
name|get
operator|.
name|getCacheBlocks
argument_list|()
expr_stmt|;
name|this
operator|.
name|maxVersions
operator|=
name|get
operator|.
name|getMaxVersions
argument_list|()
expr_stmt|;
name|this
operator|.
name|storeLimit
operator|=
name|get
operator|.
name|getMaxResultsPerColumnFamily
argument_list|()
expr_stmt|;
name|this
operator|.
name|storeOffset
operator|=
name|get
operator|.
name|getRowOffsetPerColumnFamily
argument_list|()
expr_stmt|;
name|this
operator|.
name|tr
operator|=
name|get
operator|.
name|getTimeRange
argument_list|()
expr_stmt|;
name|this
operator|.
name|familyMap
operator|=
name|get
operator|.
name|getFamilyMap
argument_list|()
expr_stmt|;
block|}
specifier|public
name|boolean
name|isGetScan
parameter_list|()
block|{
return|return
name|this
operator|.
name|startRow
operator|!=
literal|null
operator|&&
name|this
operator|.
name|startRow
operator|.
name|length
operator|>
literal|0
operator|&&
name|Bytes
operator|.
name|equals
argument_list|(
name|this
operator|.
name|startRow
argument_list|,
name|this
operator|.
name|stopRow
argument_list|)
return|;
block|}
comment|/**    * Get all columns from the specified family.    *<p>    * Overrides previous calls to addColumn for this family.    * @param family family name    * @return this    */
specifier|public
name|Scan
name|addFamily
parameter_list|(
name|byte
index|[]
name|family
parameter_list|)
block|{
name|familyMap
operator|.
name|remove
argument_list|(
name|family
argument_list|)
expr_stmt|;
name|familyMap
operator|.
name|put
argument_list|(
name|family
argument_list|,
literal|null
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**    * Get the column from the specified family with the specified qualifier.    *<p>    * Overrides previous calls to addFamily for this family.    * @param family family name    * @param qualifier column qualifier    * @return this    */
specifier|public
name|Scan
name|addColumn
parameter_list|(
name|byte
index|[]
name|family
parameter_list|,
name|byte
index|[]
name|qualifier
parameter_list|)
block|{
name|NavigableSet
argument_list|<
name|byte
index|[]
argument_list|>
name|set
init|=
name|familyMap
operator|.
name|get
argument_list|(
name|family
argument_list|)
decl_stmt|;
if|if
condition|(
name|set
operator|==
literal|null
condition|)
block|{
name|set
operator|=
operator|new
name|TreeSet
argument_list|<
name|byte
index|[]
argument_list|>
argument_list|(
name|Bytes
operator|.
name|BYTES_COMPARATOR
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|qualifier
operator|==
literal|null
condition|)
block|{
name|qualifier
operator|=
name|HConstants
operator|.
name|EMPTY_BYTE_ARRAY
expr_stmt|;
block|}
name|set
operator|.
name|add
argument_list|(
name|qualifier
argument_list|)
expr_stmt|;
name|familyMap
operator|.
name|put
argument_list|(
name|family
argument_list|,
name|set
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**    * Get versions of columns only within the specified timestamp range,    * [minStamp, maxStamp).  Note, default maximum versions to return is 1.  If    * your time range spans more than one version and you want all versions    * returned, up the number of versions beyond the defaut.    * @param minStamp minimum timestamp value, inclusive    * @param maxStamp maximum timestamp value, exclusive    * @throws IOException if invalid time range    * @see #setMaxVersions()    * @see #setMaxVersions(int)    * @return this    */
specifier|public
name|Scan
name|setTimeRange
parameter_list|(
name|long
name|minStamp
parameter_list|,
name|long
name|maxStamp
parameter_list|)
throws|throws
name|IOException
block|{
name|tr
operator|=
operator|new
name|TimeRange
argument_list|(
name|minStamp
argument_list|,
name|maxStamp
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**    * Get versions of columns with the specified timestamp. Note, default maximum    * versions to return is 1.  If your time range spans more than one version    * and you want all versions returned, up the number of versions beyond the    * defaut.    * @param timestamp version timestamp    * @see #setMaxVersions()    * @see #setMaxVersions(int)    * @return this    */
specifier|public
name|Scan
name|setTimeStamp
parameter_list|(
name|long
name|timestamp
parameter_list|)
block|{
try|try
block|{
name|tr
operator|=
operator|new
name|TimeRange
argument_list|(
name|timestamp
argument_list|,
name|timestamp
operator|+
literal|1
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
comment|// Will never happen
block|}
return|return
name|this
return|;
block|}
comment|/**    * Set the start row of the scan.    * @param startRow row to start scan on (inclusive)    * Note: In order to make startRow exclusive add a trailing 0 byte    * @return this    */
specifier|public
name|Scan
name|setStartRow
parameter_list|(
name|byte
index|[]
name|startRow
parameter_list|)
block|{
name|this
operator|.
name|startRow
operator|=
name|startRow
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**    * Set the stop row.    * @param stopRow row to end at (exclusive)    * Note: In order to make stopRow inclusive add a trailing 0 byte    * @return this    */
specifier|public
name|Scan
name|setStopRow
parameter_list|(
name|byte
index|[]
name|stopRow
parameter_list|)
block|{
name|this
operator|.
name|stopRow
operator|=
name|stopRow
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**    * Get all available versions.    * @return this    */
specifier|public
name|Scan
name|setMaxVersions
parameter_list|()
block|{
name|this
operator|.
name|maxVersions
operator|=
name|Integer
operator|.
name|MAX_VALUE
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**    * Get up to the specified number of versions of each column.    * @param maxVersions maximum versions for each column    * @return this    */
specifier|public
name|Scan
name|setMaxVersions
parameter_list|(
name|int
name|maxVersions
parameter_list|)
block|{
name|this
operator|.
name|maxVersions
operator|=
name|maxVersions
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**    * Set the maximum number of values to return for each call to next()    * @param batch the maximum number of values    */
specifier|public
name|void
name|setBatch
parameter_list|(
name|int
name|batch
parameter_list|)
block|{
if|if
condition|(
name|this
operator|.
name|hasFilter
argument_list|()
operator|&&
name|this
operator|.
name|filter
operator|.
name|hasFilterRow
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|IncompatibleFilterException
argument_list|(
literal|"Cannot set batch on a scan using a filter"
operator|+
literal|" that returns true for filter.hasFilterRow"
argument_list|)
throw|;
block|}
name|this
operator|.
name|batch
operator|=
name|batch
expr_stmt|;
block|}
comment|/**    * Set the maximum number of values to return per row per Column Family    * @param limit the maximum number of values returned / row / CF    */
specifier|public
name|void
name|setMaxResultsPerColumnFamily
parameter_list|(
name|int
name|limit
parameter_list|)
block|{
name|this
operator|.
name|storeLimit
operator|=
name|limit
expr_stmt|;
block|}
comment|/**    * Set offset for the row per Column Family.    * @param offset is the number of kvs that will be skipped.    */
specifier|public
name|void
name|setRowOffsetPerColumnFamily
parameter_list|(
name|int
name|offset
parameter_list|)
block|{
name|this
operator|.
name|storeOffset
operator|=
name|offset
expr_stmt|;
block|}
comment|/**    * Set the number of rows for caching that will be passed to scanners.    * If not set, the default setting from {@link HTable#getScannerCaching()} will apply.    * Higher caching values will enable faster scanners but will use more memory.    * @param caching the number of rows for caching    */
specifier|public
name|void
name|setCaching
parameter_list|(
name|int
name|caching
parameter_list|)
block|{
name|this
operator|.
name|caching
operator|=
name|caching
expr_stmt|;
block|}
comment|/**    * @return the maximum result size in bytes. See {@link #setMaxResultSize(long)}    */
specifier|public
name|long
name|getMaxResultSize
parameter_list|()
block|{
return|return
name|maxResultSize
return|;
block|}
comment|/**    * Set the maximum result size. The default is -1; this means that no specific    * maximum result size will be set for this scan, and the global configured    * value will be used instead. (Defaults to unlimited).    *    * @param maxResultSize The maximum result size in bytes.    */
specifier|public
name|void
name|setMaxResultSize
parameter_list|(
name|long
name|maxResultSize
parameter_list|)
block|{
name|this
operator|.
name|maxResultSize
operator|=
name|maxResultSize
expr_stmt|;
block|}
comment|/**    * Apply the specified server-side filter when performing the Scan.    * @param filter filter to run on the server    * @return this    */
specifier|public
name|Scan
name|setFilter
parameter_list|(
name|Filter
name|filter
parameter_list|)
block|{
name|this
operator|.
name|filter
operator|=
name|filter
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**    * Setting the familyMap    * @param familyMap map of family to qualifier    * @return this    */
specifier|public
name|Scan
name|setFamilyMap
parameter_list|(
name|Map
argument_list|<
name|byte
index|[]
argument_list|,
name|NavigableSet
argument_list|<
name|byte
index|[]
argument_list|>
argument_list|>
name|familyMap
parameter_list|)
block|{
name|this
operator|.
name|familyMap
operator|=
name|familyMap
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**    * Getting the familyMap    * @return familyMap    */
specifier|public
name|Map
argument_list|<
name|byte
index|[]
argument_list|,
name|NavigableSet
argument_list|<
name|byte
index|[]
argument_list|>
argument_list|>
name|getFamilyMap
parameter_list|()
block|{
return|return
name|this
operator|.
name|familyMap
return|;
block|}
comment|/**    * @return the number of families in familyMap    */
specifier|public
name|int
name|numFamilies
parameter_list|()
block|{
if|if
condition|(
name|hasFamilies
argument_list|()
condition|)
block|{
return|return
name|this
operator|.
name|familyMap
operator|.
name|size
argument_list|()
return|;
block|}
return|return
literal|0
return|;
block|}
comment|/**    * @return true if familyMap is non empty, false otherwise    */
specifier|public
name|boolean
name|hasFamilies
parameter_list|()
block|{
return|return
operator|!
name|this
operator|.
name|familyMap
operator|.
name|isEmpty
argument_list|()
return|;
block|}
comment|/**    * @return the keys of the familyMap    */
specifier|public
name|byte
index|[]
index|[]
name|getFamilies
parameter_list|()
block|{
if|if
condition|(
name|hasFamilies
argument_list|()
condition|)
block|{
return|return
name|this
operator|.
name|familyMap
operator|.
name|keySet
argument_list|()
operator|.
name|toArray
argument_list|(
operator|new
name|byte
index|[
literal|0
index|]
index|[
literal|0
index|]
argument_list|)
return|;
block|}
return|return
literal|null
return|;
block|}
comment|/**    * @return the startrow    */
specifier|public
name|byte
index|[]
name|getStartRow
parameter_list|()
block|{
return|return
name|this
operator|.
name|startRow
return|;
block|}
comment|/**    * @return the stoprow    */
specifier|public
name|byte
index|[]
name|getStopRow
parameter_list|()
block|{
return|return
name|this
operator|.
name|stopRow
return|;
block|}
comment|/**    * @return the max number of versions to fetch    */
specifier|public
name|int
name|getMaxVersions
parameter_list|()
block|{
return|return
name|this
operator|.
name|maxVersions
return|;
block|}
comment|/**    * @return maximum number of values to return for a single call to next()    */
specifier|public
name|int
name|getBatch
parameter_list|()
block|{
return|return
name|this
operator|.
name|batch
return|;
block|}
comment|/**    * @return maximum number of values to return per row per CF    */
specifier|public
name|int
name|getMaxResultsPerColumnFamily
parameter_list|()
block|{
return|return
name|this
operator|.
name|storeLimit
return|;
block|}
comment|/**    * Method for retrieving the scan's offset per row per column    * family (#kvs to be skipped)    * @return row offset    */
specifier|public
name|int
name|getRowOffsetPerColumnFamily
parameter_list|()
block|{
return|return
name|this
operator|.
name|storeOffset
return|;
block|}
comment|/**    * @return caching the number of rows fetched when calling next on a scanner    */
specifier|public
name|int
name|getCaching
parameter_list|()
block|{
return|return
name|this
operator|.
name|caching
return|;
block|}
comment|/**    * @return TimeRange    */
specifier|public
name|TimeRange
name|getTimeRange
parameter_list|()
block|{
return|return
name|this
operator|.
name|tr
return|;
block|}
comment|/**    * @return RowFilter    */
specifier|public
name|Filter
name|getFilter
parameter_list|()
block|{
return|return
name|filter
return|;
block|}
comment|/**    * @return true is a filter has been specified, false if not    */
specifier|public
name|boolean
name|hasFilter
parameter_list|()
block|{
return|return
name|filter
operator|!=
literal|null
return|;
block|}
comment|/**    * Set whether blocks should be cached for this Scan.    *<p>    * This is true by default.  When true, default settings of the table and    * family are used (this will never override caching blocks if the block    * cache is disabled for that family or entirely).    *    * @param cacheBlocks if false, default settings are overridden and blocks    * will not be cached    */
specifier|public
name|void
name|setCacheBlocks
parameter_list|(
name|boolean
name|cacheBlocks
parameter_list|)
block|{
name|this
operator|.
name|cacheBlocks
operator|=
name|cacheBlocks
expr_stmt|;
block|}
comment|/**    * Get whether blocks should be cached for this Scan.    * @return true if default caching should be used, false if blocks should not    * be cached    */
specifier|public
name|boolean
name|getCacheBlocks
parameter_list|()
block|{
return|return
name|cacheBlocks
return|;
block|}
comment|/**    * Compile the table and column family (i.e. schema) information    * into a String. Useful for parsing and aggregation by debugging,    * logging, and administration tools.    * @return Map    */
annotation|@
name|Override
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|getFingerprint
parameter_list|()
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|map
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|families
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
if|if
condition|(
name|this
operator|.
name|familyMap
operator|.
name|size
argument_list|()
operator|==
literal|0
condition|)
block|{
name|map
operator|.
name|put
argument_list|(
literal|"families"
argument_list|,
literal|"ALL"
argument_list|)
expr_stmt|;
return|return
name|map
return|;
block|}
else|else
block|{
name|map
operator|.
name|put
argument_list|(
literal|"families"
argument_list|,
name|families
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|byte
index|[]
argument_list|,
name|NavigableSet
argument_list|<
name|byte
index|[]
argument_list|>
argument_list|>
name|entry
range|:
name|this
operator|.
name|familyMap
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|families
operator|.
name|add
argument_list|(
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|map
return|;
block|}
comment|/**    * Compile the details beyond the scope of getFingerprint (row, columns,    * timestamps, etc.) into a Map along with the fingerprinted information.    * Useful for debugging, logging, and administration tools.    * @param maxCols a limit on the number of columns output prior to truncation    * @return Map    */
annotation|@
name|Override
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|toMap
parameter_list|(
name|int
name|maxCols
parameter_list|)
block|{
comment|// start with the fingerpring map and build on top of it
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|map
init|=
name|getFingerprint
argument_list|()
decl_stmt|;
comment|// map from families to column list replaces fingerprint's list of families
name|Map
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
name|familyColumns
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
name|map
operator|.
name|put
argument_list|(
literal|"families"
argument_list|,
name|familyColumns
argument_list|)
expr_stmt|;
comment|// add scalar information first
name|map
operator|.
name|put
argument_list|(
literal|"startRow"
argument_list|,
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|this
operator|.
name|startRow
argument_list|)
argument_list|)
expr_stmt|;
name|map
operator|.
name|put
argument_list|(
literal|"stopRow"
argument_list|,
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|this
operator|.
name|stopRow
argument_list|)
argument_list|)
expr_stmt|;
name|map
operator|.
name|put
argument_list|(
literal|"maxVersions"
argument_list|,
name|this
operator|.
name|maxVersions
argument_list|)
expr_stmt|;
name|map
operator|.
name|put
argument_list|(
literal|"batch"
argument_list|,
name|this
operator|.
name|batch
argument_list|)
expr_stmt|;
name|map
operator|.
name|put
argument_list|(
literal|"caching"
argument_list|,
name|this
operator|.
name|caching
argument_list|)
expr_stmt|;
name|map
operator|.
name|put
argument_list|(
literal|"maxResultSize"
argument_list|,
name|this
operator|.
name|maxResultSize
argument_list|)
expr_stmt|;
name|map
operator|.
name|put
argument_list|(
literal|"cacheBlocks"
argument_list|,
name|this
operator|.
name|cacheBlocks
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|Long
argument_list|>
name|timeRange
init|=
operator|new
name|ArrayList
argument_list|<
name|Long
argument_list|>
argument_list|()
decl_stmt|;
name|timeRange
operator|.
name|add
argument_list|(
name|this
operator|.
name|tr
operator|.
name|getMin
argument_list|()
argument_list|)
expr_stmt|;
name|timeRange
operator|.
name|add
argument_list|(
name|this
operator|.
name|tr
operator|.
name|getMax
argument_list|()
argument_list|)
expr_stmt|;
name|map
operator|.
name|put
argument_list|(
literal|"timeRange"
argument_list|,
name|timeRange
argument_list|)
expr_stmt|;
name|int
name|colCount
init|=
literal|0
decl_stmt|;
comment|// iterate through affected families and list out up to maxCols columns
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|byte
index|[]
argument_list|,
name|NavigableSet
argument_list|<
name|byte
index|[]
argument_list|>
argument_list|>
name|entry
range|:
name|this
operator|.
name|familyMap
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|List
argument_list|<
name|String
argument_list|>
name|columns
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|familyColumns
operator|.
name|put
argument_list|(
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
argument_list|,
name|columns
argument_list|)
expr_stmt|;
if|if
condition|(
name|entry
operator|.
name|getValue
argument_list|()
operator|==
literal|null
condition|)
block|{
name|colCount
operator|++
expr_stmt|;
operator|--
name|maxCols
expr_stmt|;
name|columns
operator|.
name|add
argument_list|(
literal|"ALL"
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|colCount
operator|+=
name|entry
operator|.
name|getValue
argument_list|()
operator|.
name|size
argument_list|()
expr_stmt|;
if|if
condition|(
name|maxCols
operator|<=
literal|0
condition|)
block|{
continue|continue;
block|}
for|for
control|(
name|byte
index|[]
name|column
range|:
name|entry
operator|.
name|getValue
argument_list|()
control|)
block|{
if|if
condition|(
operator|--
name|maxCols
operator|<=
literal|0
condition|)
block|{
continue|continue;
block|}
name|columns
operator|.
name|add
argument_list|(
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|column
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
name|map
operator|.
name|put
argument_list|(
literal|"totalColumns"
argument_list|,
name|colCount
argument_list|)
expr_stmt|;
if|if
condition|(
name|this
operator|.
name|filter
operator|!=
literal|null
condition|)
block|{
name|map
operator|.
name|put
argument_list|(
literal|"filter"
argument_list|,
name|this
operator|.
name|filter
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|// add the id if set
if|if
condition|(
name|getId
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|map
operator|.
name|put
argument_list|(
literal|"id"
argument_list|,
name|getId
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|map
return|;
block|}
comment|/**    * Enable/disable "raw" mode for this scan.    * If "raw" is enabled the scan will return all    * delete marker and deleted rows that have not    * been collected, yet.    * This is mostly useful for Scan on column families    * that have KEEP_DELETED_ROWS enabled.    * It is an error to specify any column when "raw" is set.    * @param raw True/False to enable/disable "raw" mode.    */
specifier|public
name|void
name|setRaw
parameter_list|(
name|boolean
name|raw
parameter_list|)
block|{
name|setAttribute
argument_list|(
name|RAW_ATTR
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|raw
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * @return True if this Scan is in "raw" mode.    */
specifier|public
name|boolean
name|isRaw
parameter_list|()
block|{
name|byte
index|[]
name|attr
init|=
name|getAttribute
argument_list|(
name|RAW_ATTR
argument_list|)
decl_stmt|;
return|return
name|attr
operator|==
literal|null
condition|?
literal|false
else|:
name|Bytes
operator|.
name|toBoolean
argument_list|(
name|attr
argument_list|)
return|;
block|}
comment|/*    * Set the isolation level for this scan. If the    * isolation level is set to READ_UNCOMMITTED, then    * this scan will return data from committed and    * uncommitted transactions. If the isolation level     * is set to READ_COMMITTED, then this scan will return     * data from committed transactions only. If a isolation    * level is not explicitly set on a Scan, then it     * is assumed to be READ_COMMITTED.    * @param level IsolationLevel for this scan    */
specifier|public
name|void
name|setIsolationLevel
parameter_list|(
name|IsolationLevel
name|level
parameter_list|)
block|{
name|setAttribute
argument_list|(
name|ISOLATION_LEVEL
argument_list|,
name|level
operator|.
name|toBytes
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/*    * @return The isolation level of this scan.    * If no isolation level was set for this scan object,     * then it returns READ_COMMITTED.    * @return The IsolationLevel for this scan    */
specifier|public
name|IsolationLevel
name|getIsolationLevel
parameter_list|()
block|{
name|byte
index|[]
name|attr
init|=
name|getAttribute
argument_list|(
name|ISOLATION_LEVEL
argument_list|)
decl_stmt|;
return|return
name|attr
operator|==
literal|null
condition|?
name|IsolationLevel
operator|.
name|READ_COMMITTED
else|:
name|IsolationLevel
operator|.
name|fromBytes
argument_list|(
name|attr
argument_list|)
return|;
block|}
block|}
end_class

end_unit

