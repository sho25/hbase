begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Copyright 2009 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|Arrays
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Comparator
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
name|NavigableMap
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
name|KeyValue
operator|.
name|SplitKeyValue
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
name|io
operator|.
name|RowResult
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

begin_comment
comment|/**  * Single row result of a {@link Get} or {@link Scan} query.<p>  *   * Convenience methods are available that return various {@link Map}  * structures and values directly.<p>  *   * To get a complete mapping of all cells in the Result, which can include   * multiple families and multiple versions, use {@link #getMap()}.<p>  *   * To get a mapping of each family to its columns (qualifiers and values),   * including only the latest version of each, use {@link #getNoVersionMap()}.  *   * To get a mapping of qualifiers to latest values for an individual family use   * {@link #getFamilyMap(byte[])}.<p>  *   * To get the latest value for a specific family and qualifier use {@link #getValue(byte[], byte[])}.  *  * A Result is backed by an array of {@link KeyValue} objects, each representing  * an HBase cell defined by the row, family, qualifier, timestamp, and value.<p>  *   * The underlying {@link KeyValue} objects can be accessed through the methods  * {@link #sorted()} and {@link #list()}.  Each KeyValue can then be accessed  * through {@link KeyValue#getRow()}, {@link KeyValue#getFamily()}, {@link KeyValue#getQualifier()},   * {@link KeyValue#getTimestamp()}, and {@link KeyValue#getValue()}.  */
end_comment

begin_class
specifier|public
class|class
name|Result
implements|implements
name|Writable
block|{
specifier|private
name|KeyValue
index|[]
name|kvs
init|=
literal|null
decl_stmt|;
specifier|private
name|NavigableMap
argument_list|<
name|byte
index|[]
argument_list|,
name|NavigableMap
argument_list|<
name|byte
index|[]
argument_list|,
name|NavigableMap
argument_list|<
name|Long
argument_list|,
name|byte
index|[]
argument_list|>
argument_list|>
argument_list|>
name|familyMap
init|=
literal|null
decl_stmt|;
comment|// We're not using java serialization.  Transient here is just a marker to say
comment|// that this is where we cache row if we're ever asked for it.
specifier|private
specifier|transient
name|byte
index|[]
name|row
init|=
literal|null
decl_stmt|;
comment|/**    * Constructor used for Writable.    */
specifier|public
name|Result
parameter_list|()
block|{}
comment|/**    * Instantiate a Result with the specified array of KeyValues.    * @param kvs array of KeyValues    */
specifier|public
name|Result
parameter_list|(
name|KeyValue
index|[]
name|kvs
parameter_list|)
block|{
if|if
condition|(
name|kvs
operator|!=
literal|null
operator|&&
name|kvs
operator|.
name|length
operator|>
literal|0
condition|)
block|{
name|this
operator|.
name|kvs
operator|=
name|kvs
expr_stmt|;
block|}
block|}
comment|/**    * Instantiate a Result with the specified List of KeyValues.    * @param kvs List of KeyValues    */
specifier|public
name|Result
parameter_list|(
name|List
argument_list|<
name|KeyValue
argument_list|>
name|kvs
parameter_list|)
block|{
name|this
argument_list|(
name|kvs
operator|.
name|toArray
argument_list|(
operator|new
name|KeyValue
index|[
literal|0
index|]
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * Method for retrieving the row that this result is for    * @return row    */
specifier|public
specifier|synchronized
name|byte
index|[]
name|getRow
parameter_list|()
block|{
if|if
condition|(
name|this
operator|.
name|row
operator|==
literal|null
condition|)
block|{
name|this
operator|.
name|row
operator|=
name|this
operator|.
name|kvs
operator|==
literal|null
operator|||
name|this
operator|.
name|kvs
operator|.
name|length
operator|==
literal|0
condition|?
literal|null
else|:
name|this
operator|.
name|kvs
index|[
literal|0
index|]
operator|.
name|getRow
argument_list|()
expr_stmt|;
block|}
return|return
name|this
operator|.
name|row
return|;
block|}
comment|/**    * Return the unsorted array of KeyValues backing this Result instance.    * @return unsorted array of KeyValues    */
specifier|public
name|KeyValue
index|[]
name|raw
parameter_list|()
block|{
return|return
name|kvs
return|;
block|}
comment|/**    * Create a sorted list of the KeyValue's in this result.    *     * @return The sorted list of KeyValue's.    */
specifier|public
name|List
argument_list|<
name|KeyValue
argument_list|>
name|list
parameter_list|()
block|{
return|return
name|Arrays
operator|.
name|asList
argument_list|(
name|sorted
argument_list|()
argument_list|)
return|;
block|}
comment|/**    * Returns a sorted array of KeyValues in this Result.    *<p>    * Note: Sorting is done in place, so the backing array will be sorted    * after calling this method.    * @return sorted array of KeyValues    */
specifier|public
name|KeyValue
index|[]
name|sorted
parameter_list|()
block|{
if|if
condition|(
name|isEmpty
argument_list|()
condition|)
block|{
return|return
literal|null
return|;
block|}
name|Arrays
operator|.
name|sort
argument_list|(
name|kvs
argument_list|,
operator|(
name|Comparator
argument_list|<
name|KeyValue
argument_list|>
operator|)
name|KeyValue
operator|.
name|COMPARATOR
argument_list|)
expr_stmt|;
return|return
name|kvs
return|;
block|}
comment|/**    * Map of families to all versions of its qualifiers and values.    *<p>    * Returns a three level Map of the form:     *<code>Map<family,Map<qualifier,Map<timestamp,value>>></code>    *<p>    * Note: All other map returning methods make use of this map internally.     * @return map from families to qualifiers to versions    */
specifier|public
name|NavigableMap
argument_list|<
name|byte
index|[]
argument_list|,
name|NavigableMap
argument_list|<
name|byte
index|[]
argument_list|,
name|NavigableMap
argument_list|<
name|Long
argument_list|,
name|byte
index|[]
argument_list|>
argument_list|>
argument_list|>
name|getMap
parameter_list|()
block|{
if|if
condition|(
name|this
operator|.
name|familyMap
operator|!=
literal|null
condition|)
block|{
return|return
name|this
operator|.
name|familyMap
return|;
block|}
if|if
condition|(
name|isEmpty
argument_list|()
condition|)
block|{
return|return
literal|null
return|;
block|}
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
name|NavigableMap
argument_list|<
name|byte
index|[]
argument_list|,
name|NavigableMap
argument_list|<
name|Long
argument_list|,
name|byte
index|[]
argument_list|>
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
name|KeyValue
name|kv
range|:
name|this
operator|.
name|kvs
control|)
block|{
name|SplitKeyValue
name|splitKV
init|=
name|kv
operator|.
name|split
argument_list|()
decl_stmt|;
name|byte
index|[]
name|family
init|=
name|splitKV
operator|.
name|getFamily
argument_list|()
decl_stmt|;
name|NavigableMap
argument_list|<
name|byte
index|[]
argument_list|,
name|NavigableMap
argument_list|<
name|Long
argument_list|,
name|byte
index|[]
argument_list|>
argument_list|>
name|columnMap
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
name|columnMap
operator|==
literal|null
condition|)
block|{
name|columnMap
operator|=
operator|new
name|TreeMap
argument_list|<
name|byte
index|[]
argument_list|,
name|NavigableMap
argument_list|<
name|Long
argument_list|,
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
name|familyMap
operator|.
name|put
argument_list|(
name|family
argument_list|,
name|columnMap
argument_list|)
expr_stmt|;
block|}
name|byte
index|[]
name|qualifier
init|=
name|splitKV
operator|.
name|getQualifier
argument_list|()
decl_stmt|;
name|NavigableMap
argument_list|<
name|Long
argument_list|,
name|byte
index|[]
argument_list|>
name|versionMap
init|=
name|columnMap
operator|.
name|get
argument_list|(
name|qualifier
argument_list|)
decl_stmt|;
if|if
condition|(
name|versionMap
operator|==
literal|null
condition|)
block|{
name|versionMap
operator|=
operator|new
name|TreeMap
argument_list|<
name|Long
argument_list|,
name|byte
index|[]
argument_list|>
argument_list|(
operator|new
name|Comparator
argument_list|<
name|Long
argument_list|>
argument_list|()
block|{
specifier|public
name|int
name|compare
parameter_list|(
name|Long
name|l1
parameter_list|,
name|Long
name|l2
parameter_list|)
block|{
return|return
name|l2
operator|.
name|compareTo
argument_list|(
name|l1
argument_list|)
return|;
block|}
block|}
argument_list|)
expr_stmt|;
name|columnMap
operator|.
name|put
argument_list|(
name|qualifier
argument_list|,
name|versionMap
argument_list|)
expr_stmt|;
block|}
name|Long
name|timestamp
init|=
name|Bytes
operator|.
name|toLong
argument_list|(
name|splitKV
operator|.
name|getTimestamp
argument_list|()
argument_list|)
decl_stmt|;
name|byte
index|[]
name|value
init|=
name|splitKV
operator|.
name|getValue
argument_list|()
decl_stmt|;
name|versionMap
operator|.
name|put
argument_list|(
name|timestamp
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
return|return
name|this
operator|.
name|familyMap
return|;
block|}
comment|/**    * Map of families to their most recent qualifiers and values.    *<p>    * Returns a two level Map of the form:<code>Map<family,Map<qualifier,value>></code>    *<p>    * The most recent version of each qualifier will be used.    * @return map from families to qualifiers and value    */
specifier|public
name|NavigableMap
argument_list|<
name|byte
index|[]
argument_list|,
name|NavigableMap
argument_list|<
name|byte
index|[]
argument_list|,
name|byte
index|[]
argument_list|>
argument_list|>
name|getNoVersionMap
parameter_list|()
block|{
if|if
condition|(
name|this
operator|.
name|familyMap
operator|==
literal|null
condition|)
block|{
name|getMap
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|isEmpty
argument_list|()
condition|)
block|{
return|return
literal|null
return|;
block|}
name|NavigableMap
argument_list|<
name|byte
index|[]
argument_list|,
name|NavigableMap
argument_list|<
name|byte
index|[]
argument_list|,
name|byte
index|[]
argument_list|>
argument_list|>
name|returnMap
init|=
operator|new
name|TreeMap
argument_list|<
name|byte
index|[]
argument_list|,
name|NavigableMap
argument_list|<
name|byte
index|[]
argument_list|,
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
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|byte
index|[]
argument_list|,
name|NavigableMap
argument_list|<
name|byte
index|[]
argument_list|,
name|NavigableMap
argument_list|<
name|Long
argument_list|,
name|byte
index|[]
argument_list|>
argument_list|>
argument_list|>
name|familyEntry
range|:
name|familyMap
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|NavigableMap
argument_list|<
name|byte
index|[]
argument_list|,
name|byte
index|[]
argument_list|>
name|qualifierMap
init|=
operator|new
name|TreeMap
argument_list|<
name|byte
index|[]
argument_list|,
name|byte
index|[]
argument_list|>
argument_list|(
name|Bytes
operator|.
name|BYTES_COMPARATOR
argument_list|)
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
name|NavigableMap
argument_list|<
name|Long
argument_list|,
name|byte
index|[]
argument_list|>
argument_list|>
name|qualifierEntry
range|:
name|familyEntry
operator|.
name|getValue
argument_list|()
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|byte
index|[]
name|value
init|=
name|qualifierEntry
operator|.
name|getValue
argument_list|()
operator|.
name|get
argument_list|(
name|qualifierEntry
operator|.
name|getValue
argument_list|()
operator|.
name|firstKey
argument_list|()
argument_list|)
decl_stmt|;
name|qualifierMap
operator|.
name|put
argument_list|(
name|qualifierEntry
operator|.
name|getKey
argument_list|()
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
name|returnMap
operator|.
name|put
argument_list|(
name|familyEntry
operator|.
name|getKey
argument_list|()
argument_list|,
name|qualifierMap
argument_list|)
expr_stmt|;
block|}
return|return
name|returnMap
return|;
block|}
comment|/**    * Map of qualifiers to values.    *<p>    * Returns a Map of the form:<code>Map<qualifier,value></code>    * @return map of qualifiers to values    */
specifier|public
name|NavigableMap
argument_list|<
name|byte
index|[]
argument_list|,
name|byte
index|[]
argument_list|>
name|getFamilyMap
parameter_list|(
name|byte
index|[]
name|family
parameter_list|)
block|{
if|if
condition|(
name|this
operator|.
name|familyMap
operator|==
literal|null
condition|)
block|{
name|getMap
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|isEmpty
argument_list|()
condition|)
block|{
return|return
literal|null
return|;
block|}
name|NavigableMap
argument_list|<
name|byte
index|[]
argument_list|,
name|byte
index|[]
argument_list|>
name|returnMap
init|=
operator|new
name|TreeMap
argument_list|<
name|byte
index|[]
argument_list|,
name|byte
index|[]
argument_list|>
argument_list|(
name|Bytes
operator|.
name|BYTES_COMPARATOR
argument_list|)
decl_stmt|;
name|NavigableMap
argument_list|<
name|byte
index|[]
argument_list|,
name|NavigableMap
argument_list|<
name|Long
argument_list|,
name|byte
index|[]
argument_list|>
argument_list|>
name|qualifierMap
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
name|qualifierMap
operator|==
literal|null
condition|)
block|{
return|return
name|returnMap
return|;
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
name|NavigableMap
argument_list|<
name|Long
argument_list|,
name|byte
index|[]
argument_list|>
argument_list|>
name|entry
range|:
name|qualifierMap
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|byte
index|[]
name|value
init|=
name|entry
operator|.
name|getValue
argument_list|()
operator|.
name|get
argument_list|(
name|entry
operator|.
name|getValue
argument_list|()
operator|.
name|firstKey
argument_list|()
argument_list|)
decl_stmt|;
name|returnMap
operator|.
name|put
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
return|return
name|returnMap
return|;
block|}
comment|/**    * Get the latest version of the specified column.    * @param family family name    * @param qualifier column qualifier    * @return value of latest version of column, null if none found    */
specifier|public
name|byte
index|[]
name|getValue
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
name|Map
operator|.
name|Entry
argument_list|<
name|Long
argument_list|,
name|byte
index|[]
argument_list|>
name|entry
init|=
name|getKeyValue
argument_list|(
name|family
argument_list|,
name|qualifier
argument_list|)
decl_stmt|;
return|return
name|entry
operator|==
literal|null
condition|?
literal|null
else|:
name|entry
operator|.
name|getValue
argument_list|()
return|;
block|}
specifier|public
name|Cell
name|getCellValue
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
name|Map
operator|.
name|Entry
argument_list|<
name|Long
argument_list|,
name|byte
index|[]
argument_list|>
name|val
init|=
name|getKeyValue
argument_list|(
name|family
argument_list|,
name|qualifier
argument_list|)
decl_stmt|;
if|if
condition|(
name|val
operator|==
literal|null
condition|)
return|return
literal|null
return|;
return|return
operator|new
name|Cell
argument_list|(
name|val
operator|.
name|getValue
argument_list|()
argument_list|,
name|val
operator|.
name|getKey
argument_list|()
argument_list|)
return|;
block|}
comment|/**    * @return First KeyValue in this Result as a Cell or null if empty.    */
specifier|public
name|Cell
name|getCellValue
parameter_list|()
block|{
return|return
name|isEmpty
argument_list|()
condition|?
literal|null
else|:
operator|new
name|Cell
argument_list|(
name|kvs
index|[
literal|0
index|]
operator|.
name|getValue
argument_list|()
argument_list|,
name|kvs
index|[
literal|0
index|]
operator|.
name|getTimestamp
argument_list|()
argument_list|)
return|;
block|}
comment|/**    * @return This Result as array of Cells or null if empty.    */
specifier|public
name|Cell
index|[]
name|getCellValues
parameter_list|()
block|{
if|if
condition|(
name|isEmpty
argument_list|()
condition|)
return|return
literal|null
return|;
name|Cell
index|[]
name|results
init|=
operator|new
name|Cell
index|[
name|kvs
operator|.
name|length
index|]
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|kvs
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|results
index|[
name|i
index|]
operator|=
operator|new
name|Cell
argument_list|(
name|kvs
index|[
name|i
index|]
operator|.
name|getValue
argument_list|()
argument_list|,
name|kvs
index|[
name|i
index|]
operator|.
name|getTimestamp
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|results
return|;
block|}
specifier|private
name|Map
operator|.
name|Entry
argument_list|<
name|Long
argument_list|,
name|byte
index|[]
argument_list|>
name|getKeyValue
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
if|if
condition|(
name|this
operator|.
name|familyMap
operator|==
literal|null
condition|)
block|{
name|getMap
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|isEmpty
argument_list|()
condition|)
block|{
return|return
literal|null
return|;
block|}
name|NavigableMap
argument_list|<
name|byte
index|[]
argument_list|,
name|NavigableMap
argument_list|<
name|Long
argument_list|,
name|byte
index|[]
argument_list|>
argument_list|>
name|qualifierMap
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
name|qualifierMap
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
name|NavigableMap
argument_list|<
name|Long
argument_list|,
name|byte
index|[]
argument_list|>
name|versionMap
init|=
name|getVersionMap
argument_list|(
name|qualifierMap
argument_list|,
name|qualifier
argument_list|)
decl_stmt|;
if|if
condition|(
name|versionMap
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
return|return
name|versionMap
operator|.
name|firstEntry
argument_list|()
return|;
block|}
specifier|private
name|NavigableMap
argument_list|<
name|Long
argument_list|,
name|byte
index|[]
argument_list|>
name|getVersionMap
parameter_list|(
name|NavigableMap
argument_list|<
name|byte
index|[]
argument_list|,
name|NavigableMap
argument_list|<
name|Long
argument_list|,
name|byte
index|[]
argument_list|>
argument_list|>
name|qualifierMap
parameter_list|,
name|byte
index|[]
name|qualifier
parameter_list|)
block|{
return|return
name|qualifier
operator|!=
literal|null
condition|?
name|qualifierMap
operator|.
name|get
argument_list|(
name|qualifier
argument_list|)
else|:
name|qualifierMap
operator|.
name|get
argument_list|(
operator|new
name|byte
index|[
literal|0
index|]
argument_list|)
return|;
block|}
comment|/**    * Get the latest version of the specified column,    * using<pre>family:qualifier</pre> notation.    * @param column column in family:qualifier notation    * @return value of latest version of column, null if none found    */
specifier|public
name|byte
index|[]
name|getValue
parameter_list|(
name|byte
index|[]
name|column
parameter_list|)
block|{
try|try
block|{
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
return|return
name|getValue
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
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
return|return
literal|null
return|;
block|}
block|}
comment|/**    * Checks for existence of the specified column.    * @param family family name    * @param qualifier column qualifier    * @return true if at least one value exists in the result, false if not    */
specifier|public
name|boolean
name|containsColumn
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
if|if
condition|(
name|this
operator|.
name|familyMap
operator|==
literal|null
condition|)
block|{
name|getMap
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|isEmpty
argument_list|()
condition|)
block|{
return|return
literal|false
return|;
block|}
name|NavigableMap
argument_list|<
name|byte
index|[]
argument_list|,
name|NavigableMap
argument_list|<
name|Long
argument_list|,
name|byte
index|[]
argument_list|>
argument_list|>
name|qualifierMap
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
name|qualifierMap
operator|==
literal|null
condition|)
block|{
return|return
literal|false
return|;
block|}
name|NavigableMap
argument_list|<
name|Long
argument_list|,
name|byte
index|[]
argument_list|>
name|versionMap
init|=
name|getVersionMap
argument_list|(
name|qualifierMap
argument_list|,
name|qualifier
argument_list|)
decl_stmt|;
if|if
condition|(
name|versionMap
operator|==
literal|null
condition|)
block|{
return|return
literal|false
return|;
block|}
return|return
literal|true
return|;
block|}
comment|/**    * Returns this Result in the old return format, {@link RowResult}.    * @return a RowResult    */
specifier|public
name|RowResult
name|getRowResult
parameter_list|()
block|{
return|return
name|RowResult
operator|.
name|createRowResult
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|kvs
argument_list|)
argument_list|)
return|;
block|}
comment|/**    * Returns the value of the first column in the Result.    * @return value of the first column    */
specifier|public
name|byte
index|[]
name|value
parameter_list|()
block|{
if|if
condition|(
name|isEmpty
argument_list|()
condition|)
block|{
return|return
literal|null
return|;
block|}
return|return
name|kvs
index|[
literal|0
index|]
operator|.
name|getValue
argument_list|()
return|;
block|}
comment|/**    * Check if the underlying KeyValue [] is empty or not    * @return true if empty    */
specifier|public
name|boolean
name|isEmpty
parameter_list|()
block|{
return|return
name|this
operator|.
name|kvs
operator|==
literal|null
operator|||
name|this
operator|.
name|kvs
operator|.
name|length
operator|==
literal|0
return|;
block|}
comment|/**    * @return the size of the underlying KeyValue []    */
specifier|public
name|int
name|size
parameter_list|()
block|{
return|return
name|this
operator|.
name|kvs
operator|==
literal|null
condition|?
literal|0
else|:
name|this
operator|.
name|kvs
operator|.
name|length
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
name|StringBuffer
name|sb
init|=
operator|new
name|StringBuffer
argument_list|()
decl_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"keyvalues="
argument_list|)
expr_stmt|;
if|if
condition|(
name|isEmpty
argument_list|()
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|"NONE"
argument_list|)
expr_stmt|;
return|return
name|sb
operator|.
name|toString
argument_list|()
return|;
block|}
name|sb
operator|.
name|append
argument_list|(
literal|"{"
argument_list|)
expr_stmt|;
name|boolean
name|moreThanOne
init|=
literal|false
decl_stmt|;
for|for
control|(
name|KeyValue
name|kv
range|:
name|this
operator|.
name|kvs
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
literal|", "
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|moreThanOne
operator|=
literal|true
expr_stmt|;
block|}
name|sb
operator|.
name|append
argument_list|(
name|kv
operator|.
name|toString
argument_list|()
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
return|return
name|sb
operator|.
name|toString
argument_list|()
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
name|familyMap
operator|=
literal|null
expr_stmt|;
name|row
operator|=
literal|null
expr_stmt|;
name|int
name|numKeys
init|=
name|in
operator|.
name|readInt
argument_list|()
decl_stmt|;
name|this
operator|.
name|kvs
operator|=
operator|new
name|KeyValue
index|[
name|numKeys
index|]
expr_stmt|;
if|if
condition|(
name|numKeys
operator|==
literal|0
condition|)
block|{
return|return;
block|}
name|int
name|totalBuffer
init|=
name|in
operator|.
name|readInt
argument_list|()
decl_stmt|;
name|byte
index|[]
name|buf
init|=
operator|new
name|byte
index|[
name|totalBuffer
index|]
decl_stmt|;
name|int
name|offset
init|=
literal|0
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|numKeys
condition|;
name|i
operator|++
control|)
block|{
name|int
name|keyLength
init|=
name|in
operator|.
name|readInt
argument_list|()
decl_stmt|;
name|in
operator|.
name|readFully
argument_list|(
name|buf
argument_list|,
name|offset
argument_list|,
name|keyLength
argument_list|)
expr_stmt|;
name|kvs
index|[
name|i
index|]
operator|=
operator|new
name|KeyValue
argument_list|(
name|buf
argument_list|,
name|offset
argument_list|,
name|keyLength
argument_list|)
expr_stmt|;
name|offset
operator|+=
name|keyLength
expr_stmt|;
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
if|if
condition|(
name|isEmpty
argument_list|()
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
name|int
name|len
init|=
name|this
operator|.
name|kvs
operator|.
name|length
decl_stmt|;
name|out
operator|.
name|writeInt
argument_list|(
name|len
argument_list|)
expr_stmt|;
name|int
name|totalLen
init|=
literal|0
decl_stmt|;
for|for
control|(
name|KeyValue
name|kv
range|:
name|kvs
control|)
block|{
name|totalLen
operator|+=
name|kv
operator|.
name|getLength
argument_list|()
expr_stmt|;
block|}
name|out
operator|.
name|writeInt
argument_list|(
name|totalLen
argument_list|)
expr_stmt|;
for|for
control|(
name|KeyValue
name|kv
range|:
name|kvs
control|)
block|{
name|out
operator|.
name|writeInt
argument_list|(
name|kv
operator|.
name|getLength
argument_list|()
argument_list|)
expr_stmt|;
name|out
operator|.
name|write
argument_list|(
name|kv
operator|.
name|getBuffer
argument_list|()
argument_list|,
name|kv
operator|.
name|getOffset
argument_list|()
argument_list|,
name|kv
operator|.
name|getLength
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|public
specifier|static
name|void
name|writeArray
parameter_list|(
specifier|final
name|DataOutput
name|out
parameter_list|,
name|Result
index|[]
name|results
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|results
operator|==
literal|null
operator|||
name|results
operator|.
name|length
operator|==
literal|0
condition|)
block|{
name|out
operator|.
name|writeInt
argument_list|(
literal|0
argument_list|)
expr_stmt|;
return|return;
block|}
name|out
operator|.
name|writeInt
argument_list|(
name|results
operator|.
name|length
argument_list|)
expr_stmt|;
name|int
name|bufLen
init|=
literal|0
decl_stmt|;
for|for
control|(
name|Result
name|result
range|:
name|results
control|)
block|{
if|if
condition|(
name|result
operator|==
literal|null
operator|||
name|result
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
continue|continue;
block|}
for|for
control|(
name|KeyValue
name|key
range|:
name|result
operator|.
name|raw
argument_list|()
control|)
block|{
name|bufLen
operator|+=
name|key
operator|.
name|getLength
argument_list|()
expr_stmt|;
block|}
block|}
name|out
operator|.
name|writeInt
argument_list|(
name|bufLen
argument_list|)
expr_stmt|;
for|for
control|(
name|Result
name|result
range|:
name|results
control|)
block|{
if|if
condition|(
name|result
operator|==
literal|null
operator|||
name|result
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|out
operator|.
name|writeInt
argument_list|(
literal|0
argument_list|)
expr_stmt|;
continue|continue;
block|}
name|out
operator|.
name|writeInt
argument_list|(
name|result
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|KeyValue
name|kv
range|:
name|result
operator|.
name|raw
argument_list|()
control|)
block|{
name|out
operator|.
name|writeInt
argument_list|(
name|kv
operator|.
name|getLength
argument_list|()
argument_list|)
expr_stmt|;
name|out
operator|.
name|write
argument_list|(
name|kv
operator|.
name|getBuffer
argument_list|()
argument_list|,
name|kv
operator|.
name|getOffset
argument_list|()
argument_list|,
name|kv
operator|.
name|getLength
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|public
specifier|static
name|Result
index|[]
name|readArray
parameter_list|(
specifier|final
name|DataInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|int
name|numResults
init|=
name|in
operator|.
name|readInt
argument_list|()
decl_stmt|;
if|if
condition|(
name|numResults
operator|==
literal|0
condition|)
block|{
return|return
operator|new
name|Result
index|[
literal|0
index|]
return|;
block|}
name|Result
index|[]
name|results
init|=
operator|new
name|Result
index|[
name|numResults
index|]
decl_stmt|;
name|int
name|bufSize
init|=
name|in
operator|.
name|readInt
argument_list|()
decl_stmt|;
name|byte
index|[]
name|buf
init|=
operator|new
name|byte
index|[
name|bufSize
index|]
decl_stmt|;
name|int
name|offset
init|=
literal|0
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|numResults
condition|;
name|i
operator|++
control|)
block|{
name|int
name|numKeys
init|=
name|in
operator|.
name|readInt
argument_list|()
decl_stmt|;
name|KeyValue
index|[]
name|keys
init|=
operator|new
name|KeyValue
index|[
name|numKeys
index|]
decl_stmt|;
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
name|numKeys
condition|;
name|j
operator|++
control|)
block|{
name|int
name|keyLen
init|=
name|in
operator|.
name|readInt
argument_list|()
decl_stmt|;
name|in
operator|.
name|readFully
argument_list|(
name|buf
argument_list|,
name|offset
argument_list|,
name|keyLen
argument_list|)
expr_stmt|;
name|keys
index|[
name|j
index|]
operator|=
operator|new
name|KeyValue
argument_list|(
name|buf
argument_list|,
name|offset
argument_list|,
name|keyLen
argument_list|)
expr_stmt|;
name|offset
operator|+=
name|keyLen
expr_stmt|;
block|}
name|results
index|[
name|i
index|]
operator|=
operator|new
name|Result
argument_list|(
name|keys
argument_list|)
expr_stmt|;
block|}
return|return
name|results
return|;
block|}
block|}
end_class

end_unit

