begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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

begin_comment
comment|/**  * Performs Append operations on a single row.  *<p>  * This operation ensures atomicty to readers. Appends are done  * under a single row lock, so write operations to a row are synchronized, and  * readers are guaranteed to see this operation fully completed.  *<p>  * To append to a set of columns of a row, instantiate an Append object with the  * row to append to. At least one column to append must be specified using the  * {@link #addColumn(byte[], byte[], byte[])} method.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Public
specifier|public
class|class
name|Append
extends|extends
name|Mutation
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
name|Append
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|long
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
comment|/**    * Sets the TimeRange to be used on the Get for this append.    *<p>    * This is useful for when you have counters that only last for specific    * periods of time (ie. counters that are partitioned by time).  By setting    * the range of valid times for this append, you can potentially gain    * some performance with a more optimal Get operation.    * Be careful adding the time range to this class as you will update the old cell if the    * time range doesn't include the latest cells.    *<p>    * This range is used as [minStamp, maxStamp).    * @param minStamp minimum timestamp value, inclusive    * @param maxStamp maximum timestamp value, exclusive    * @return this    */
specifier|public
name|Append
name|setTimeRange
parameter_list|(
name|long
name|minStamp
parameter_list|,
name|long
name|maxStamp
parameter_list|)
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
comment|/**    * Gets the TimeRange used for this append.    * @return TimeRange    */
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
comment|/**    * @param returnResults    *          True (default) if the append operation should return the results.    *          A client that is not interested in the result can save network    *          bandwidth setting this to false.    */
annotation|@
name|Override
specifier|public
name|Append
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
comment|/**    * Create a Append operation for the specified row.    *<p>    * At least one column must be appended to.    * @param row row key; makes a local copy of passed in array.    */
specifier|public
name|Append
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
comment|/**    * Copy constructor    * @param appendToCopy append to copy    */
specifier|public
name|Append
parameter_list|(
name|Append
name|appendToCopy
parameter_list|)
block|{
name|super
argument_list|(
name|appendToCopy
argument_list|)
expr_stmt|;
name|this
operator|.
name|tr
operator|=
name|appendToCopy
operator|.
name|getTimeRange
argument_list|()
expr_stmt|;
block|}
comment|/** Create a Append operation for the specified row.    *<p>    * At least one column must be appended to.    * @param rowArray Makes a copy out of this buffer.    * @param rowOffset    * @param rowLength    */
specifier|public
name|Append
parameter_list|(
specifier|final
name|byte
index|[]
name|rowArray
parameter_list|,
specifier|final
name|int
name|rowOffset
parameter_list|,
specifier|final
name|int
name|rowLength
parameter_list|)
block|{
name|checkRow
argument_list|(
name|rowArray
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
name|rowArray
argument_list|,
name|rowOffset
argument_list|,
name|rowLength
argument_list|)
expr_stmt|;
block|}
comment|/**    * Construct the Append with user defined data. NOTED:    * 1) all cells in the familyMap must have the Type.Put    * 2) the row of each cell must be same with passed row.    * @param row row. CAN'T be null    * @param ts timestamp    * @param familyMap the map to collect all cells internally. CAN'T be null    */
specifier|public
name|Append
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
comment|/**    * Add the specified column and value to this Append operation.    * @param family family name    * @param qualifier column qualifier    * @param value value to append to specified column    * @return this    */
specifier|public
name|Append
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
name|byte
index|[]
name|value
parameter_list|)
block|{
name|KeyValue
name|kv
init|=
operator|new
name|KeyValue
argument_list|(
name|this
operator|.
name|row
argument_list|,
name|family
argument_list|,
name|qualifier
argument_list|,
name|this
operator|.
name|ts
argument_list|,
name|KeyValue
operator|.
name|Type
operator|.
name|Put
argument_list|,
name|value
argument_list|)
decl_stmt|;
return|return
name|add
argument_list|(
name|kv
argument_list|)
return|;
block|}
comment|/**    * Add column and value to this Append operation.    * @param cell    * @return This instance    */
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
specifier|public
name|Append
name|add
parameter_list|(
specifier|final
name|Cell
name|cell
parameter_list|)
block|{
try|try
block|{
name|super
operator|.
name|add
argument_list|(
name|cell
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
comment|// we eat the exception of wrong row for BC..
name|LOG
operator|.
name|error
argument_list|(
name|e
operator|.
name|toString
argument_list|()
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
return|return
name|this
return|;
block|}
annotation|@
name|Override
specifier|public
name|Append
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
annotation|@
name|Override
specifier|public
name|Append
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
name|Append
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
name|Append
name|setId
parameter_list|(
name|String
name|id
parameter_list|)
block|{
return|return
operator|(
name|Append
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
name|Append
name|setDurability
parameter_list|(
name|Durability
name|d
parameter_list|)
block|{
return|return
operator|(
name|Append
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
name|Append
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
name|Append
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
name|Append
name|setCellVisibility
parameter_list|(
name|CellVisibility
name|expression
parameter_list|)
block|{
return|return
operator|(
name|Append
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
name|Append
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
name|Append
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
name|Append
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
name|Append
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
name|Append
name|setPriority
parameter_list|(
name|int
name|priority
parameter_list|)
block|{
return|return
operator|(
name|Append
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
name|Append
name|setTTL
parameter_list|(
name|long
name|ttl
parameter_list|)
block|{
return|return
operator|(
name|Append
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

