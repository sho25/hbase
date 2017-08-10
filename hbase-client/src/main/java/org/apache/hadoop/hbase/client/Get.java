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
name|client
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
name|nio
operator|.
name|ByteBuffer
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
name|Set
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
name|security
operator|.
name|access
operator|.
name|Permission
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
name|security
operator|.
name|visibility
operator|.
name|Authorizations
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

begin_comment
comment|/**  * Used to perform Get operations on a single row.  *<p>  * To get everything for a row, instantiate a Get object with the row to get.  * To further narrow the scope of what to Get, use the methods below.  *<p>  * To get all columns from specific families, execute {@link #addFamily(byte[]) addFamily}  * for each family to retrieve.  *<p>  * To get specific columns, execute {@link #addColumn(byte[], byte[]) addColumn}  * for each column to retrieve.  *<p>  * To only retrieve columns within a specific range of version timestamps,  * execute {@link #setTimeRange(long, long) setTimeRange}.  *<p>  * To only retrieve columns with a specific timestamp, execute  * {@link #setTimeStamp(long) setTimestamp}.  *<p>  * To limit the number of versions of each column to be returned, execute  * {@link #setMaxVersions(int) setMaxVersions}.  *<p>  * To add a filter, call {@link #setFilter(Filter) setFilter}.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Public
specifier|public
class|class
name|Get
extends|extends
name|Query
implements|implements
name|Row
implements|,
name|Comparable
argument_list|<
name|Row
argument_list|>
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
name|Get
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|byte
index|[]
name|row
init|=
literal|null
decl_stmt|;
specifier|private
name|int
name|maxVersions
init|=
literal|1
decl_stmt|;
specifier|private
name|boolean
name|cacheBlocks
init|=
literal|true
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
name|boolean
name|checkExistenceOnly
init|=
literal|false
decl_stmt|;
specifier|private
name|boolean
name|closestRowBefore
init|=
literal|false
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
argument_list|<>
argument_list|(
name|Bytes
operator|.
name|BYTES_COMPARATOR
argument_list|)
decl_stmt|;
comment|/**    * Create a Get operation for the specified row.    *<p>    * If no further operations are done, this will get the latest version of    * all columns in all families of the specified row.    * @param row row key    */
specifier|public
name|Get
parameter_list|(
name|byte
index|[]
name|row
parameter_list|)
block|{
name|Mutation
operator|.
name|checkRow
argument_list|(
name|row
argument_list|)
expr_stmt|;
name|this
operator|.
name|row
operator|=
name|row
expr_stmt|;
block|}
comment|/**    * Copy-constructor    *    * @param get    */
specifier|public
name|Get
parameter_list|(
name|Get
name|get
parameter_list|)
block|{
name|this
argument_list|(
name|get
operator|.
name|getRow
argument_list|()
argument_list|)
expr_stmt|;
comment|// from Query
name|this
operator|.
name|setFilter
argument_list|(
name|get
operator|.
name|getFilter
argument_list|()
argument_list|)
expr_stmt|;
name|this
operator|.
name|setReplicaId
argument_list|(
name|get
operator|.
name|getReplicaId
argument_list|()
argument_list|)
expr_stmt|;
name|this
operator|.
name|setConsistency
argument_list|(
name|get
operator|.
name|getConsistency
argument_list|()
argument_list|)
expr_stmt|;
comment|// from Get
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
name|checkExistenceOnly
operator|=
name|get
operator|.
name|isCheckExistenceOnly
argument_list|()
expr_stmt|;
name|this
operator|.
name|loadColumnFamiliesOnDemand
operator|=
name|get
operator|.
name|getLoadColumnFamiliesOnDemandValue
argument_list|()
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
name|get
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
name|get
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
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|byte
index|[]
argument_list|,
name|TimeRange
argument_list|>
name|entry
range|:
name|get
operator|.
name|getColumnFamilyTimeRange
argument_list|()
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|TimeRange
name|tr
init|=
name|entry
operator|.
name|getValue
argument_list|()
decl_stmt|;
name|setColumnFamilyTimeRange
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|,
name|tr
operator|.
name|getMin
argument_list|()
argument_list|,
name|tr
operator|.
name|getMax
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|super
operator|.
name|setPriority
argument_list|(
name|get
operator|.
name|getPriority
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**    * Create a Get operation for the specified row.    * @param row    * @param rowOffset    * @param rowLength    */
specifier|public
name|Get
parameter_list|(
name|byte
index|[]
name|row
parameter_list|,
name|int
name|rowOffset
parameter_list|,
name|int
name|rowLength
parameter_list|)
block|{
name|Mutation
operator|.
name|checkRow
argument_list|(
name|row
argument_list|,
name|rowOffset
argument_list|,
name|rowLength
argument_list|)
expr_stmt|;
name|this
operator|.
name|row
operator|=
name|Bytes
operator|.
name|copy
argument_list|(
name|row
argument_list|,
name|rowOffset
argument_list|,
name|rowLength
argument_list|)
expr_stmt|;
block|}
comment|/**    * Create a Get operation for the specified row.    * @param row    */
specifier|public
name|Get
parameter_list|(
name|ByteBuffer
name|row
parameter_list|)
block|{
name|Mutation
operator|.
name|checkRow
argument_list|(
name|row
argument_list|)
expr_stmt|;
name|this
operator|.
name|row
operator|=
operator|new
name|byte
index|[
name|row
operator|.
name|remaining
argument_list|()
index|]
expr_stmt|;
name|row
operator|.
name|get
argument_list|(
name|this
operator|.
name|row
argument_list|)
expr_stmt|;
block|}
specifier|public
name|boolean
name|isCheckExistenceOnly
parameter_list|()
block|{
return|return
name|checkExistenceOnly
return|;
block|}
specifier|public
name|Get
name|setCheckExistenceOnly
parameter_list|(
name|boolean
name|checkExistenceOnly
parameter_list|)
block|{
name|this
operator|.
name|checkExistenceOnly
operator|=
name|checkExistenceOnly
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**    * This will always return the default value which is false as client cannot set the value to this    * property any more.    * @deprecated since 2.0.0 and will be removed in 3.0.0    */
annotation|@
name|Deprecated
specifier|public
name|boolean
name|isClosestRowBefore
parameter_list|()
block|{
return|return
name|closestRowBefore
return|;
block|}
comment|/**    * This is not used any more and does nothing. Use reverse scan instead.    * @deprecated since 2.0.0 and will be removed in 3.0.0    */
annotation|@
name|Deprecated
specifier|public
name|Get
name|setClosestRowBefore
parameter_list|(
name|boolean
name|closestRowBefore
parameter_list|)
block|{
comment|// do Nothing
return|return
name|this
return|;
block|}
comment|/**    * Get all columns from the specified family.    *<p>    * Overrides previous calls to addColumn for this family.    * @param family family name    * @return the Get object    */
specifier|public
name|Get
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
comment|/**    * Get the column from the specific family with the specified qualifier.    *<p>    * Overrides previous calls to addFamily for this family.    * @param family family name    * @param qualifier column qualifier    * @return the Get objec    */
specifier|public
name|Get
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
argument_list|<>
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
comment|/**    * Get versions of columns only within the specified timestamp range,    * [minStamp, maxStamp).    * @param minStamp minimum timestamp value, inclusive    * @param maxStamp maximum timestamp value, exclusive    * @throws IOException    * @return this for invocation chaining    */
annotation|@
name|Override
specifier|public
name|Get
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
return|return
operator|(
name|Get
operator|)
name|super
operator|.
name|setTimeRange
argument_list|(
name|minStamp
argument_list|,
name|maxStamp
argument_list|)
return|;
block|}
comment|/**    * Get versions of columns only within the specified timestamp range,    * @param tr Input TimeRange    * @return this for invocation chaining    */
annotation|@
name|Override
specifier|public
name|Get
name|setTimeRange
parameter_list|(
name|TimeRange
name|tr
parameter_list|)
block|{
return|return
operator|(
name|Get
operator|)
name|super
operator|.
name|setTimeRange
argument_list|(
name|tr
argument_list|)
return|;
block|}
comment|/**    * Get versions of columns with the specified timestamp.    * @param timestamp version timestamp    * @return this for invocation chaining    */
specifier|public
name|Get
name|setTimeStamp
parameter_list|(
name|long
name|timestamp
parameter_list|)
throws|throws
name|IOException
block|{
try|try
block|{
name|super
operator|.
name|setTimeRange
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
name|Exception
name|e
parameter_list|)
block|{
comment|// This should never happen, unless integer overflow or something extremely wrong...
name|LOG
operator|.
name|error
argument_list|(
literal|"TimeRange failed, likely caused by integer overflow. "
argument_list|,
name|e
argument_list|)
expr_stmt|;
throw|throw
name|e
throw|;
block|}
return|return
name|this
return|;
block|}
annotation|@
name|Override
specifier|public
name|Get
name|setColumnFamilyTimeRange
parameter_list|(
name|byte
index|[]
name|cf
parameter_list|,
name|long
name|minStamp
parameter_list|,
name|long
name|maxStamp
parameter_list|)
block|{
return|return
operator|(
name|Get
operator|)
name|super
operator|.
name|setColumnFamilyTimeRange
argument_list|(
name|cf
argument_list|,
name|minStamp
argument_list|,
name|maxStamp
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|Get
name|setColumnFamilyTimeRange
parameter_list|(
name|byte
index|[]
name|cf
parameter_list|,
name|TimeRange
name|tr
parameter_list|)
block|{
return|return
operator|(
name|Get
operator|)
name|super
operator|.
name|setColumnFamilyTimeRange
argument_list|(
name|cf
argument_list|,
name|tr
argument_list|)
return|;
block|}
comment|/**    * Get all available versions.    * @return this for invocation chaining    * @deprecated It is easy to misunderstand with column family's max versions, so use    *             {@link #readAllVersions()} instead.    */
annotation|@
name|Deprecated
specifier|public
name|Get
name|setMaxVersions
parameter_list|()
block|{
return|return
name|readAllVersions
argument_list|()
return|;
block|}
comment|/**    * Get up to the specified number of versions of each column.    * @param maxVersions maximum versions for each column    * @throws IOException if invalid number of versions    * @return this for invocation chaining    * @deprecated It is easy to misunderstand with column family's max versions, so use    *             {@link #readVersions(int)} instead.    */
annotation|@
name|Deprecated
specifier|public
name|Get
name|setMaxVersions
parameter_list|(
name|int
name|maxVersions
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|readVersions
argument_list|(
name|maxVersions
argument_list|)
return|;
block|}
comment|/**    * Get all available versions.    * @return this for invocation chaining    */
specifier|public
name|Get
name|readAllVersions
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
comment|/**    * Get up to the specified number of versions of each column.    * @param versions specified number of versions for each column    * @throws IOException if invalid number of versions    * @return this for invocation chaining    */
specifier|public
name|Get
name|readVersions
parameter_list|(
name|int
name|versions
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|versions
operator|<=
literal|0
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"versions must be positive"
argument_list|)
throw|;
block|}
name|this
operator|.
name|maxVersions
operator|=
name|versions
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|Get
name|setLoadColumnFamiliesOnDemand
parameter_list|(
name|boolean
name|value
parameter_list|)
block|{
return|return
operator|(
name|Get
operator|)
name|super
operator|.
name|setLoadColumnFamiliesOnDemand
argument_list|(
name|value
argument_list|)
return|;
block|}
comment|/**    * Set the maximum number of values to return per row per Column Family    * @param limit the maximum number of values returned / row / CF    * @return this for invocation chaining    */
specifier|public
name|Get
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
return|return
name|this
return|;
block|}
comment|/**    * Set offset for the row per Column Family. This offset is only within a particular row/CF    * combination. It gets reset back to zero when we move to the next row or CF.    * @param offset is the number of kvs that will be skipped.    * @return this for invocation chaining    */
specifier|public
name|Get
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
return|return
name|this
return|;
block|}
annotation|@
name|Override
specifier|public
name|Get
name|setFilter
parameter_list|(
name|Filter
name|filter
parameter_list|)
block|{
name|super
operator|.
name|setFilter
argument_list|(
name|filter
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/* Accessors */
comment|/**    * Set whether blocks should be cached for this Get.    *<p>    * This is true by default.  When true, default settings of the table and    * family are used (this will never override caching blocks if the block    * cache is disabled for that family or entirely).    *    * @param cacheBlocks if false, default settings are overridden and blocks    * will not be cached    */
specifier|public
name|Get
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
return|return
name|this
return|;
block|}
comment|/**    * Get whether blocks should be cached for this Get.    * @return true if default caching should be used, false if blocks should not    * be cached    */
specifier|public
name|boolean
name|getCacheBlocks
parameter_list|()
block|{
return|return
name|cacheBlocks
return|;
block|}
comment|/**    * Method for retrieving the get's row    * @return row    */
annotation|@
name|Override
specifier|public
name|byte
index|[]
name|getRow
parameter_list|()
block|{
return|return
name|this
operator|.
name|row
return|;
block|}
comment|/**    * Method for retrieving the get's maximum number of version    * @return the maximum number of version to fetch for this get    */
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
comment|/**    * Method for retrieving the get's maximum number of values    * to return per Column Family    * @return the maximum number of values to fetch per CF    */
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
comment|/**    * Method for retrieving the get's offset per row per column    * family (#kvs to be skipped)    * @return the row offset    */
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
comment|/**    * Method for retrieving the keys in the familyMap    * @return keys in the current familyMap    */
specifier|public
name|Set
argument_list|<
name|byte
index|[]
argument_list|>
name|familySet
parameter_list|()
block|{
return|return
name|this
operator|.
name|familyMap
operator|.
name|keySet
argument_list|()
return|;
block|}
comment|/**    * Method for retrieving the number of families to get from    * @return number of families    */
specifier|public
name|int
name|numFamilies
parameter_list|()
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
comment|/**    * Method for checking if any families have been inserted into this Get    * @return true if familyMap is non empty false otherwise    */
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
comment|/**    * Method for retrieving the get's familyMap    * @return familyMap    */
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
argument_list|<>
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
argument_list|<>
argument_list|(
name|this
operator|.
name|familyMap
operator|.
name|entrySet
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
name|map
operator|.
name|put
argument_list|(
literal|"families"
argument_list|,
name|families
argument_list|)
expr_stmt|;
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
comment|// we start with the fingerprint map and build on top of it.
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
comment|// replace the fingerprint's simple list of families with a
comment|// map from column families to lists of qualifiers and kv details
name|Map
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
name|columns
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|map
operator|.
name|put
argument_list|(
literal|"families"
argument_list|,
name|columns
argument_list|)
expr_stmt|;
comment|// add scalar information first
name|map
operator|.
name|put
argument_list|(
literal|"row"
argument_list|,
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|this
operator|.
name|row
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
argument_list|<>
argument_list|(
literal|2
argument_list|)
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
comment|// iterate through affected families and add details
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
name|familyList
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|columns
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
name|familyList
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
name|familyList
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
name|familyList
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
comment|//Row
annotation|@
name|Override
specifier|public
name|int
name|compareTo
parameter_list|(
name|Row
name|other
parameter_list|)
block|{
comment|// TODO: This is wrong.  Can't have two gets the same just because on same row.
return|return
name|Bytes
operator|.
name|compareTo
argument_list|(
name|this
operator|.
name|getRow
argument_list|()
argument_list|,
name|other
operator|.
name|getRow
argument_list|()
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
comment|// TODO: This is wrong.  Can't have two gets the same just because on same row.  But it
comment|// matches how equals works currently and gets rid of the findbugs warning.
return|return
name|Bytes
operator|.
name|hashCode
argument_list|(
name|this
operator|.
name|getRow
argument_list|()
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|equals
parameter_list|(
name|Object
name|obj
parameter_list|)
block|{
if|if
condition|(
name|this
operator|==
name|obj
condition|)
block|{
return|return
literal|true
return|;
block|}
if|if
condition|(
name|obj
operator|==
literal|null
operator|||
name|getClass
argument_list|()
operator|!=
name|obj
operator|.
name|getClass
argument_list|()
condition|)
block|{
return|return
literal|false
return|;
block|}
name|Row
name|other
init|=
operator|(
name|Row
operator|)
name|obj
decl_stmt|;
comment|// TODO: This is wrong.  Can't have two gets the same just because on same row.
return|return
name|compareTo
argument_list|(
name|other
argument_list|)
operator|==
literal|0
return|;
block|}
annotation|@
name|Override
specifier|public
name|Get
name|setAttribute
parameter_list|(
name|String
name|name
parameter_list|,
name|byte
index|[]
name|value
parameter_list|)
block|{
return|return
operator|(
name|Get
operator|)
name|super
operator|.
name|setAttribute
argument_list|(
name|name
argument_list|,
name|value
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|Get
name|setId
parameter_list|(
name|String
name|id
parameter_list|)
block|{
return|return
operator|(
name|Get
operator|)
name|super
operator|.
name|setId
argument_list|(
name|id
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|Get
name|setAuthorizations
parameter_list|(
name|Authorizations
name|authorizations
parameter_list|)
block|{
return|return
operator|(
name|Get
operator|)
name|super
operator|.
name|setAuthorizations
argument_list|(
name|authorizations
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|Get
name|setACL
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|Permission
argument_list|>
name|perms
parameter_list|)
block|{
return|return
operator|(
name|Get
operator|)
name|super
operator|.
name|setACL
argument_list|(
name|perms
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|Get
name|setACL
parameter_list|(
name|String
name|user
parameter_list|,
name|Permission
name|perms
parameter_list|)
block|{
return|return
operator|(
name|Get
operator|)
name|super
operator|.
name|setACL
argument_list|(
name|user
argument_list|,
name|perms
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|Get
name|setConsistency
parameter_list|(
name|Consistency
name|consistency
parameter_list|)
block|{
return|return
operator|(
name|Get
operator|)
name|super
operator|.
name|setConsistency
argument_list|(
name|consistency
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|Get
name|setReplicaId
parameter_list|(
name|int
name|Id
parameter_list|)
block|{
return|return
operator|(
name|Get
operator|)
name|super
operator|.
name|setReplicaId
argument_list|(
name|Id
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|Get
name|setIsolationLevel
parameter_list|(
name|IsolationLevel
name|level
parameter_list|)
block|{
return|return
operator|(
name|Get
operator|)
name|super
operator|.
name|setIsolationLevel
argument_list|(
name|level
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|Get
name|setPriority
parameter_list|(
name|int
name|priority
parameter_list|)
block|{
return|return
operator|(
name|Get
operator|)
name|super
operator|.
name|setPriority
argument_list|(
name|priority
argument_list|)
return|;
block|}
block|}
end_class

end_unit

