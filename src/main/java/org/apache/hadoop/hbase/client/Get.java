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
name|conf
operator|.
name|Configuration
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
name|io
operator|.
name|Writable
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
name|io
operator|.
name|WritableFactories
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
name|io
operator|.
name|WritableUtils
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataInput
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataOutput
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
name|Collections
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

begin_comment
comment|/**  * Used to perform Get operations on a single row.  *<p>  * To get everything for a row, instantiate a Get object with the row to get.  * To further define the scope of what to get, perform additional methods as  * outlined below.  *<p>  * To get all columns from specific families, execute {@link #addFamily(byte[]) addFamily}  * for each family to retrieve.  *<p>  * To get specific columns, execute {@link #addColumn(byte[], byte[]) addColumn}  * for each column to retrieve.  *<p>  * To only retrieve columns within a specific range of version timestamps,  * execute {@link #setTimeRange(long, long) setTimeRange}.  *<p>  * To only retrieve columns with a specific timestamp, execute  * {@link #setTimeStamp(long) setTimestamp}.  *<p>  * To limit the number of versions of each column to be returned, execute  * {@link #setMaxVersions(int) setMaxVersions}.  *<p>  * To add a filter, execute {@link #setFilter(Filter) setFilter}.  */
end_comment

begin_class
specifier|public
class|class
name|Get
implements|implements
name|Writable
implements|,
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
name|byte
name|GET_VERSION
init|=
operator|(
name|byte
operator|)
literal|2
decl_stmt|;
specifier|private
name|byte
index|[]
name|row
init|=
literal|null
decl_stmt|;
specifier|private
name|long
name|lockId
init|=
operator|-
literal|1L
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
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|byte
index|[]
argument_list|>
name|attributes
decl_stmt|;
comment|/** Constructor for Writable.  DO NOT USE */
specifier|public
name|Get
parameter_list|()
block|{}
comment|/**    * Create a Get operation for the specified row.    *<p>    * If no further operations are done, this will get the latest version of    * all columns in all families of the specified row.    * @param row row key    */
specifier|public
name|Get
parameter_list|(
name|byte
index|[]
name|row
parameter_list|)
block|{
name|this
argument_list|(
name|row
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
comment|/**    * Create a Get operation for the specified row, using an existing row lock.    *<p>    * If no further operations are done, this will get the latest version of    * all columns in all families of the specified row.    * @param row row key    * @param rowLock previously acquired row lock, or null    */
specifier|public
name|Get
parameter_list|(
name|byte
index|[]
name|row
parameter_list|,
name|RowLock
name|rowLock
parameter_list|)
block|{
name|this
operator|.
name|row
operator|=
name|row
expr_stmt|;
if|if
condition|(
name|rowLock
operator|!=
literal|null
condition|)
block|{
name|this
operator|.
name|lockId
operator|=
name|rowLock
operator|.
name|getLockId
argument_list|()
expr_stmt|;
block|}
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
comment|/**    * Get versions of columns only within the specified timestamp range,    * [minStamp, maxStamp).    * @param minStamp minimum timestamp value, inclusive    * @param maxStamp maximum timestamp value, exclusive    * @throws IOException if invalid time range    * @return this for invocation chaining    */
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
comment|/**    * Get versions of columns with the specified timestamp.    * @param timestamp version timestamp    * @return this for invocation chaining    */
specifier|public
name|Get
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
comment|/**    * Get all available versions.    * @return this for invocation chaining    */
specifier|public
name|Get
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
comment|/**    * Get up to the specified number of versions of each column.    * @param maxVersions maximum versions for each column    * @throws IOException if invalid number of versions    * @return this for invocation chaining    */
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
if|if
condition|(
name|maxVersions
operator|<=
literal|0
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"maxVersions must be positive"
argument_list|)
throw|;
block|}
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
comment|/**    * Apply the specified server-side filter when performing the Get.    * Only {@link Filter#filterKeyValue(KeyValue)} is called AFTER all tests    * for ttl, column match, deletes and max versions have been run.    * @param filter filter to run on the server    * @return this for invocation chaining    */
specifier|public
name|Get
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
comment|/* Accessors */
comment|/**    * @return Filter    */
specifier|public
name|Filter
name|getFilter
parameter_list|()
block|{
return|return
name|this
operator|.
name|filter
return|;
block|}
comment|/**    * Set whether blocks should be cached for this Get.    *<p>    * This is true by default.  When true, default settings of the table and    * family are used (this will never override caching blocks if the block    * cache is disabled for that family or entirely).    *    * @param cacheBlocks if false, default settings are overridden and blocks    * will not be cached    */
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
comment|/**    * Method for retrieving the get's RowLock    * @return RowLock    */
specifier|public
name|RowLock
name|getRowLock
parameter_list|()
block|{
return|return
operator|new
name|RowLock
argument_list|(
name|this
operator|.
name|row
argument_list|,
name|this
operator|.
name|lockId
argument_list|)
return|;
block|}
comment|/**    * Method for retrieving the get's lockId    * @return lockId    */
specifier|public
name|long
name|getLockId
parameter_list|()
block|{
return|return
name|this
operator|.
name|lockId
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
comment|/**    * Method for retrieving the get's TimeRange    * @return timeRange    */
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
comment|/**    * Sets arbitrary get's attribute.    * In case value = null attribute is removed from the attributes map.    * @param name attribute name    * @param value attribute value    */
specifier|public
name|void
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
if|if
condition|(
name|attributes
operator|==
literal|null
operator|&&
name|value
operator|==
literal|null
condition|)
block|{
return|return;
block|}
if|if
condition|(
name|attributes
operator|==
literal|null
condition|)
block|{
name|attributes
operator|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|byte
index|[]
argument_list|>
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|value
operator|==
literal|null
condition|)
block|{
name|attributes
operator|.
name|remove
argument_list|(
name|name
argument_list|)
expr_stmt|;
if|if
condition|(
name|attributes
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|this
operator|.
name|attributes
operator|=
literal|null
expr_stmt|;
block|}
block|}
else|else
block|{
name|attributes
operator|.
name|put
argument_list|(
name|name
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Gets get's attribute    * @param name attribute name    * @return attribute value if attribute is set,<tt>null</tt> otherwise    */
specifier|public
name|byte
index|[]
name|getAttribute
parameter_list|(
name|String
name|name
parameter_list|)
block|{
if|if
condition|(
name|attributes
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
return|return
name|attributes
operator|.
name|get
argument_list|(
name|name
argument_list|)
return|;
block|}
comment|/**    * Gets all scan's attributes    * @return unmodifiable map of all attributes    */
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|byte
index|[]
argument_list|>
name|getAttributesMap
parameter_list|()
block|{
if|if
condition|(
name|attributes
operator|==
literal|null
condition|)
block|{
return|return
name|Collections
operator|.
name|emptyMap
argument_list|()
return|;
block|}
return|return
name|Collections
operator|.
name|unmodifiableMap
argument_list|(
name|attributes
argument_list|)
return|;
block|}
comment|/**    * @return String    */
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"row="
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
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
name|sb
operator|.
name|append
argument_list|(
literal|", maxVersions="
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|""
argument_list|)
operator|.
name|append
argument_list|(
name|this
operator|.
name|maxVersions
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|", cacheBlocks="
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|this
operator|.
name|cacheBlocks
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|", timeRange="
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"["
argument_list|)
operator|.
name|append
argument_list|(
name|this
operator|.
name|tr
operator|.
name|getMin
argument_list|()
argument_list|)
operator|.
name|append
argument_list|(
literal|","
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|this
operator|.
name|tr
operator|.
name|getMax
argument_list|()
argument_list|)
operator|.
name|append
argument_list|(
literal|")"
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|", families="
argument_list|)
expr_stmt|;
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
name|sb
operator|.
name|append
argument_list|(
literal|"ALL"
argument_list|)
expr_stmt|;
return|return
name|sb
operator|.
name|toString
argument_list|()
return|;
block|}
name|boolean
name|moreThanOne
init|=
literal|false
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
name|this
operator|.
name|familyMap
operator|.
name|entrySet
argument_list|()
control|)
block|{
if|if
condition|(
name|moreThanOne
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|"), "
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|moreThanOne
operator|=
literal|true
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"{"
argument_list|)
expr_stmt|;
block|}
name|sb
operator|.
name|append
argument_list|(
literal|"(family="
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|Bytes
operator|.
name|toString
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|", columns="
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
name|sb
operator|.
name|append
argument_list|(
literal|"ALL"
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|sb
operator|.
name|append
argument_list|(
literal|"{"
argument_list|)
expr_stmt|;
name|boolean
name|moreThanOneB
init|=
literal|false
decl_stmt|;
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
name|moreThanOneB
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|", "
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|moreThanOneB
operator|=
literal|true
expr_stmt|;
block|}
name|sb
operator|.
name|append
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
name|sb
operator|.
name|append
argument_list|(
literal|"}"
argument_list|)
expr_stmt|;
block|}
block|}
name|sb
operator|.
name|append
argument_list|(
literal|"}"
argument_list|)
expr_stmt|;
return|return
name|sb
operator|.
name|toString
argument_list|()
return|;
block|}
comment|//Row
specifier|public
name|int
name|compareTo
parameter_list|(
name|Row
name|other
parameter_list|)
block|{
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
comment|//Writable
specifier|public
name|void
name|readFields
parameter_list|(
specifier|final
name|DataInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|int
name|version
init|=
name|in
operator|.
name|readByte
argument_list|()
decl_stmt|;
if|if
condition|(
name|version
operator|>
name|GET_VERSION
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"unsupported version"
argument_list|)
throw|;
block|}
name|this
operator|.
name|row
operator|=
name|Bytes
operator|.
name|readByteArray
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|this
operator|.
name|lockId
operator|=
name|in
operator|.
name|readLong
argument_list|()
expr_stmt|;
name|this
operator|.
name|maxVersions
operator|=
name|in
operator|.
name|readInt
argument_list|()
expr_stmt|;
name|boolean
name|hasFilter
init|=
name|in
operator|.
name|readBoolean
argument_list|()
decl_stmt|;
if|if
condition|(
name|hasFilter
condition|)
block|{
name|this
operator|.
name|filter
operator|=
operator|(
name|Filter
operator|)
name|createForName
argument_list|(
name|Bytes
operator|.
name|toString
argument_list|(
name|Bytes
operator|.
name|readByteArray
argument_list|(
name|in
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|this
operator|.
name|filter
operator|.
name|readFields
argument_list|(
name|in
argument_list|)
expr_stmt|;
block|}
name|this
operator|.
name|cacheBlocks
operator|=
name|in
operator|.
name|readBoolean
argument_list|()
expr_stmt|;
name|this
operator|.
name|tr
operator|=
operator|new
name|TimeRange
argument_list|()
expr_stmt|;
name|tr
operator|.
name|readFields
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|int
name|numFamilies
init|=
name|in
operator|.
name|readInt
argument_list|()
decl_stmt|;
name|this
operator|.
name|familyMap
operator|=
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
name|numFamilies
condition|;
name|i
operator|++
control|)
block|{
name|byte
index|[]
name|family
init|=
name|Bytes
operator|.
name|readByteArray
argument_list|(
name|in
argument_list|)
decl_stmt|;
name|boolean
name|hasColumns
init|=
name|in
operator|.
name|readBoolean
argument_list|()
decl_stmt|;
name|NavigableSet
argument_list|<
name|byte
index|[]
argument_list|>
name|set
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|hasColumns
condition|)
block|{
name|int
name|numColumns
init|=
name|in
operator|.
name|readInt
argument_list|()
decl_stmt|;
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
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
name|numColumns
condition|;
name|j
operator|++
control|)
block|{
name|byte
index|[]
name|qualifier
init|=
name|Bytes
operator|.
name|readByteArray
argument_list|(
name|in
argument_list|)
decl_stmt|;
name|set
operator|.
name|add
argument_list|(
name|qualifier
argument_list|)
expr_stmt|;
block|}
block|}
name|this
operator|.
name|familyMap
operator|.
name|put
argument_list|(
name|family
argument_list|,
name|set
argument_list|)
expr_stmt|;
block|}
name|int
name|numAttributes
init|=
name|in
operator|.
name|readInt
argument_list|()
decl_stmt|;
if|if
condition|(
name|numAttributes
operator|>
literal|0
condition|)
block|{
name|this
operator|.
name|attributes
operator|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|byte
index|[]
argument_list|>
argument_list|()
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
name|numAttributes
condition|;
name|i
operator|++
control|)
block|{
name|String
name|name
init|=
name|WritableUtils
operator|.
name|readString
argument_list|(
name|in
argument_list|)
decl_stmt|;
name|byte
index|[]
name|value
init|=
name|Bytes
operator|.
name|readByteArray
argument_list|(
name|in
argument_list|)
decl_stmt|;
name|this
operator|.
name|attributes
operator|.
name|put
argument_list|(
name|name
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|public
name|void
name|write
parameter_list|(
specifier|final
name|DataOutput
name|out
parameter_list|)
throws|throws
name|IOException
block|{
name|out
operator|.
name|writeByte
argument_list|(
name|GET_VERSION
argument_list|)
expr_stmt|;
name|Bytes
operator|.
name|writeByteArray
argument_list|(
name|out
argument_list|,
name|this
operator|.
name|row
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeLong
argument_list|(
name|this
operator|.
name|lockId
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeInt
argument_list|(
name|this
operator|.
name|maxVersions
argument_list|)
expr_stmt|;
if|if
condition|(
name|this
operator|.
name|filter
operator|==
literal|null
condition|)
block|{
name|out
operator|.
name|writeBoolean
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|out
operator|.
name|writeBoolean
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|Bytes
operator|.
name|writeByteArray
argument_list|(
name|out
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|filter
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|filter
operator|.
name|write
argument_list|(
name|out
argument_list|)
expr_stmt|;
block|}
name|out
operator|.
name|writeBoolean
argument_list|(
name|this
operator|.
name|cacheBlocks
argument_list|)
expr_stmt|;
name|tr
operator|.
name|write
argument_list|(
name|out
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeInt
argument_list|(
name|familyMap
operator|.
name|size
argument_list|()
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
name|familyMap
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|Bytes
operator|.
name|writeByteArray
argument_list|(
name|out
argument_list|,
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
expr_stmt|;
name|NavigableSet
argument_list|<
name|byte
index|[]
argument_list|>
name|columnSet
init|=
name|entry
operator|.
name|getValue
argument_list|()
decl_stmt|;
if|if
condition|(
name|columnSet
operator|==
literal|null
condition|)
block|{
name|out
operator|.
name|writeBoolean
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|out
operator|.
name|writeBoolean
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeInt
argument_list|(
name|columnSet
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|byte
index|[]
name|qualifier
range|:
name|columnSet
control|)
block|{
name|Bytes
operator|.
name|writeByteArray
argument_list|(
name|out
argument_list|,
name|qualifier
argument_list|)
expr_stmt|;
block|}
block|}
block|}
if|if
condition|(
name|this
operator|.
name|attributes
operator|==
literal|null
condition|)
block|{
name|out
operator|.
name|writeInt
argument_list|(
literal|0
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|out
operator|.
name|writeInt
argument_list|(
name|this
operator|.
name|attributes
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
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
name|this
operator|.
name|attributes
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|WritableUtils
operator|.
name|writeString
argument_list|(
name|out
argument_list|,
name|attr
operator|.
name|getKey
argument_list|()
argument_list|)
expr_stmt|;
name|Bytes
operator|.
name|writeByteArray
argument_list|(
name|out
argument_list|,
name|attr
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
specifier|private
name|Writable
name|createForName
parameter_list|(
name|String
name|className
parameter_list|)
block|{
try|try
block|{
name|Class
argument_list|<
name|?
extends|extends
name|Writable
argument_list|>
name|clazz
init|=
operator|(
name|Class
argument_list|<
name|?
extends|extends
name|Writable
argument_list|>
operator|)
name|Class
operator|.
name|forName
argument_list|(
name|className
argument_list|)
decl_stmt|;
return|return
name|WritableFactories
operator|.
name|newInstance
argument_list|(
name|clazz
argument_list|,
operator|new
name|Configuration
argument_list|()
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|ClassNotFoundException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Can't find class "
operator|+
name|className
argument_list|)
throw|;
block|}
block|}
comment|/**    * Adds an array of columns specified the old format, family:qualifier.    *<p>    * Overrides previous calls to addFamily for any families in the input.    * @param columns array of columns, formatted as<pre>family:qualifier</pre>    * @deprecated issue multiple {@link #addColumn(byte[], byte[])} instead    * @return this for invocation chaining    */
annotation|@
name|SuppressWarnings
argument_list|(
block|{
literal|"deprecation"
block|}
argument_list|)
specifier|public
name|Get
name|addColumns
parameter_list|(
name|byte
index|[]
index|[]
name|columns
parameter_list|)
block|{
if|if
condition|(
name|columns
operator|==
literal|null
condition|)
return|return
name|this
return|;
for|for
control|(
name|byte
index|[]
name|column
range|:
name|columns
control|)
block|{
try|try
block|{
name|addColumn
argument_list|(
name|column
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|ignored
parameter_list|)
block|{       }
block|}
return|return
name|this
return|;
block|}
comment|/**    *    * @param column Old format column.    * @return This.    * @deprecated use {@link #addColumn(byte[], byte[])} instead    */
specifier|public
name|Get
name|addColumn
parameter_list|(
specifier|final
name|byte
index|[]
name|column
parameter_list|)
block|{
if|if
condition|(
name|column
operator|==
literal|null
condition|)
return|return
name|this
return|;
name|byte
index|[]
index|[]
name|split
init|=
name|KeyValue
operator|.
name|parseColumn
argument_list|(
name|column
argument_list|)
decl_stmt|;
if|if
condition|(
name|split
operator|.
name|length
operator|>
literal|1
operator|&&
name|split
index|[
literal|1
index|]
operator|!=
literal|null
operator|&&
name|split
index|[
literal|1
index|]
operator|.
name|length
operator|>
literal|0
condition|)
block|{
name|addColumn
argument_list|(
name|split
index|[
literal|0
index|]
argument_list|,
name|split
index|[
literal|1
index|]
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|addFamily
argument_list|(
name|split
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
block|}
return|return
name|this
return|;
block|}
block|}
end_class

end_unit

