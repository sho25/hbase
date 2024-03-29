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
name|java
operator|.
name|util
operator|.
name|UUID
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
name|CellBuilder
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
name|CellBuilderType
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
name|CellUtil
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
name|CellVisibility
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
name|ClassSize
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

begin_comment
comment|/**  * Used to perform Increment operations on a single row.  *<p>  * This operation ensures atomicity to readers. Increments are done  * under a single row lock, so write operations to a row are synchronized, and  * readers are guaranteed to see this operation fully completed.  *<p>  * To increment columns of a row, instantiate an Increment object with the row  * to increment.  At least one column to increment must be specified using the  * {@link #addColumn(byte[], byte[], long)} method.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Public
specifier|public
class|class
name|Increment
extends|extends
name|Mutation
block|{
specifier|private
specifier|static
specifier|final
name|int
name|HEAP_OVERHEAD
init|=
name|ClassSize
operator|.
name|REFERENCE
operator|+
name|ClassSize
operator|.
name|TIMERANGE
decl_stmt|;
specifier|private
name|TimeRange
name|tr
init|=
name|TimeRange
operator|.
name|allTime
argument_list|()
decl_stmt|;
comment|/**    * Create a Increment operation for the specified row.    *<p>    * At least one column must be incremented.    * @param row row key (we will make a copy of this).    */
specifier|public
name|Increment
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
literal|0
argument_list|,
name|row
operator|.
name|length
argument_list|)
expr_stmt|;
block|}
comment|/**    * Create a Increment operation for the specified row.    *<p>    * At least one column must be incremented.    * @param row row key (we will make a copy of this).    */
specifier|public
name|Increment
parameter_list|(
specifier|final
name|byte
index|[]
name|row
parameter_list|,
specifier|final
name|int
name|offset
parameter_list|,
specifier|final
name|int
name|length
parameter_list|)
block|{
name|checkRow
argument_list|(
name|row
argument_list|,
name|offset
argument_list|,
name|length
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
name|offset
argument_list|,
name|length
argument_list|)
expr_stmt|;
block|}
comment|/**    * Copy constructor    * @param incrementToCopy increment to copy    */
specifier|public
name|Increment
parameter_list|(
name|Increment
name|incrementToCopy
parameter_list|)
block|{
name|super
argument_list|(
name|incrementToCopy
argument_list|)
expr_stmt|;
name|this
operator|.
name|tr
operator|=
name|incrementToCopy
operator|.
name|getTimeRange
argument_list|()
expr_stmt|;
block|}
comment|/**    * Construct the Increment with user defined data. NOTED:    * 1) all cells in the familyMap must have the Type.Put    * 2) the row of each cell must be same with passed row.    * @param row row. CAN'T be null    * @param ts timestamp    * @param familyMap the map to collect all cells internally. CAN'T be null    */
specifier|public
name|Increment
parameter_list|(
name|byte
index|[]
name|row
parameter_list|,
name|long
name|ts
parameter_list|,
name|NavigableMap
argument_list|<
name|byte
index|[]
argument_list|,
name|List
argument_list|<
name|Cell
argument_list|>
argument_list|>
name|familyMap
parameter_list|)
block|{
name|super
argument_list|(
name|row
argument_list|,
name|ts
argument_list|,
name|familyMap
argument_list|)
expr_stmt|;
block|}
comment|/**    * Add the specified KeyValue to this operation.    * @param cell individual Cell    * @return this    * @throws java.io.IOException e    */
specifier|public
name|Increment
name|add
parameter_list|(
name|Cell
name|cell
parameter_list|)
throws|throws
name|IOException
block|{
name|super
operator|.
name|add
argument_list|(
name|cell
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**    * Increment the column from the specific family with the specified qualifier    * by the specified amount.    *<p>    * Overrides previous calls to addColumn for this family and qualifier.    * @param family family name    * @param qualifier column qualifier    * @param amount amount to increment by    * @return the Increment object    */
specifier|public
name|Increment
name|addColumn
parameter_list|(
name|byte
index|[]
name|family
parameter_list|,
name|byte
index|[]
name|qualifier
parameter_list|,
name|long
name|amount
parameter_list|)
block|{
if|if
condition|(
name|family
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"family cannot be null"
argument_list|)
throw|;
block|}
name|List
argument_list|<
name|Cell
argument_list|>
name|list
init|=
name|getCellList
argument_list|(
name|family
argument_list|)
decl_stmt|;
name|KeyValue
name|kv
init|=
name|createPutKeyValue
argument_list|(
name|family
argument_list|,
name|qualifier
argument_list|,
name|ts
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|amount
argument_list|)
argument_list|)
decl_stmt|;
name|list
operator|.
name|add
argument_list|(
name|kv
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**    * Gets the TimeRange used for this increment.    * @return TimeRange    */
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
comment|/**    * Sets the TimeRange to be used on the Get for this increment.    *<p>    * This is useful for when you have counters that only last for specific    * periods of time (ie. counters that are partitioned by time).  By setting    * the range of valid times for this increment, you can potentially gain    * some performance with a more optimal Get operation.    * Be careful adding the time range to this class as you will update the old cell if the    * time range doesn't include the latest cells.    *<p>    * This range is used as [minStamp, maxStamp).    * @param minStamp minimum timestamp value, inclusive    * @param maxStamp maximum timestamp value, exclusive    * @throws IOException if invalid time range    * @return this    */
specifier|public
name|Increment
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
annotation|@
name|Override
specifier|public
name|Increment
name|setTimestamp
parameter_list|(
name|long
name|timestamp
parameter_list|)
block|{
name|super
operator|.
name|setTimestamp
argument_list|(
name|timestamp
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**    * @param returnResults True (default) if the increment operation should return the results. A    *          client that is not interested in the result can save network bandwidth setting this    *          to false.    */
annotation|@
name|Override
specifier|public
name|Increment
name|setReturnResults
parameter_list|(
name|boolean
name|returnResults
parameter_list|)
block|{
name|super
operator|.
name|setReturnResults
argument_list|(
name|returnResults
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**    * @return current setting for returnResults    */
comment|// This method makes public the superclasses's protected method.
annotation|@
name|Override
specifier|public
name|boolean
name|isReturnResults
parameter_list|()
block|{
return|return
name|super
operator|.
name|isReturnResults
argument_list|()
return|;
block|}
comment|/**    * Method for retrieving the number of families to increment from    * @return number of families    */
annotation|@
name|Override
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
comment|/**    * Method for checking if any families have been inserted into this Increment    * @return true if familyMap is non empty false otherwise    */
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
comment|/**    * Before 0.95, when you called Increment#getFamilyMap(), you got back    * a map of families to a list of Longs.  Now, {@link #getFamilyCellMap()} returns    * families by list of Cells.  This method has been added so you can have the    * old behavior.    * @return Map of families to a Map of qualifiers and their Long increments.    * @since 0.95.0    */
specifier|public
name|Map
argument_list|<
name|byte
index|[]
argument_list|,
name|NavigableMap
argument_list|<
name|byte
index|[]
argument_list|,
name|Long
argument_list|>
argument_list|>
name|getFamilyMapOfLongs
parameter_list|()
block|{
name|NavigableMap
argument_list|<
name|byte
index|[]
argument_list|,
name|List
argument_list|<
name|Cell
argument_list|>
argument_list|>
name|map
init|=
name|super
operator|.
name|getFamilyCellMap
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|byte
index|[]
argument_list|,
name|NavigableMap
argument_list|<
name|byte
index|[]
argument_list|,
name|Long
argument_list|>
argument_list|>
name|results
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
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|byte
index|[]
argument_list|,
name|List
argument_list|<
name|Cell
argument_list|>
argument_list|>
name|entry
range|:
name|map
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
name|Long
argument_list|>
name|longs
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
for|for
control|(
name|Cell
name|cell
range|:
name|entry
operator|.
name|getValue
argument_list|()
control|)
block|{
name|longs
operator|.
name|put
argument_list|(
name|CellUtil
operator|.
name|cloneQualifier
argument_list|(
name|cell
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toLong
argument_list|(
name|cell
operator|.
name|getValueArray
argument_list|()
argument_list|,
name|cell
operator|.
name|getValueOffset
argument_list|()
argument_list|,
name|cell
operator|.
name|getValueLength
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|results
operator|.
name|put
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|,
name|longs
argument_list|)
expr_stmt|;
block|}
return|return
name|results
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
if|if
condition|(
name|this
operator|.
name|familyMap
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|", no columns set to be incremented"
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
literal|", families="
argument_list|)
expr_stmt|;
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
name|List
argument_list|<
name|Cell
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
literal|"NONE"
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
name|Cell
name|cell
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
name|CellUtil
operator|.
name|getCellKeyAsString
argument_list|(
name|cell
argument_list|)
operator|+
literal|"+="
operator|+
name|Bytes
operator|.
name|toLong
argument_list|(
name|cell
operator|.
name|getValueArray
argument_list|()
argument_list|,
name|cell
operator|.
name|getValueOffset
argument_list|()
argument_list|,
name|cell
operator|.
name|getValueLength
argument_list|()
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
annotation|@
name|Override
specifier|protected
name|long
name|extraHeapSize
parameter_list|()
block|{
return|return
name|HEAP_OVERHEAD
return|;
block|}
annotation|@
name|Override
specifier|public
name|Increment
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
name|Increment
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
name|Increment
name|setId
parameter_list|(
name|String
name|id
parameter_list|)
block|{
return|return
operator|(
name|Increment
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
name|Increment
name|setDurability
parameter_list|(
name|Durability
name|d
parameter_list|)
block|{
return|return
operator|(
name|Increment
operator|)
name|super
operator|.
name|setDurability
argument_list|(
name|d
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|Increment
name|setClusterIds
parameter_list|(
name|List
argument_list|<
name|UUID
argument_list|>
name|clusterIds
parameter_list|)
block|{
return|return
operator|(
name|Increment
operator|)
name|super
operator|.
name|setClusterIds
argument_list|(
name|clusterIds
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|Increment
name|setCellVisibility
parameter_list|(
name|CellVisibility
name|expression
parameter_list|)
block|{
return|return
operator|(
name|Increment
operator|)
name|super
operator|.
name|setCellVisibility
argument_list|(
name|expression
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|Increment
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
name|Increment
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
name|Increment
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
name|Increment
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
name|Increment
name|setTTL
parameter_list|(
name|long
name|ttl
parameter_list|)
block|{
return|return
operator|(
name|Increment
operator|)
name|super
operator|.
name|setTTL
argument_list|(
name|ttl
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|Increment
name|setPriority
parameter_list|(
name|int
name|priority
parameter_list|)
block|{
return|return
operator|(
name|Increment
operator|)
name|super
operator|.
name|setPriority
argument_list|(
name|priority
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|CellBuilder
name|getCellBuilder
parameter_list|(
name|CellBuilderType
name|type
parameter_list|)
block|{
return|return
name|getCellBuilder
argument_list|(
name|type
argument_list|,
name|Cell
operator|.
name|Type
operator|.
name|Put
argument_list|)
return|;
block|}
block|}
end_class

end_unit

