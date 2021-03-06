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
name|io
operator|.
name|HeapSize
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
name|yetus
operator|.
name|audience
operator|.
name|InterfaceAudience
import|;
end_import

begin_comment
comment|/**  * Used to perform Put operations for a single row.  *<p>  * To perform a Put, instantiate a Put object with the row to insert to, and  * for each column to be inserted, execute {@link #addColumn(byte[], byte[],  * byte[]) add} or {@link #addColumn(byte[], byte[], long, byte[]) add} if  * setting the timestamp.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Public
specifier|public
class|class
name|Put
extends|extends
name|Mutation
implements|implements
name|HeapSize
block|{
comment|/**    * Create a Put operation for the specified row.    * @param row row key    */
specifier|public
name|Put
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
name|HConstants
operator|.
name|LATEST_TIMESTAMP
argument_list|)
expr_stmt|;
block|}
comment|/**    * Create a Put operation for the specified row, using a given timestamp.    *    * @param row row key; we make a copy of what we are passed to keep local.    * @param ts timestamp    */
specifier|public
name|Put
parameter_list|(
name|byte
index|[]
name|row
parameter_list|,
name|long
name|ts
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
argument_list|,
name|ts
argument_list|)
expr_stmt|;
block|}
comment|/**    * We make a copy of the passed in row key to keep local.    * @param rowArray    * @param rowOffset    * @param rowLength    */
specifier|public
name|Put
parameter_list|(
name|byte
index|[]
name|rowArray
parameter_list|,
name|int
name|rowOffset
parameter_list|,
name|int
name|rowLength
parameter_list|)
block|{
name|this
argument_list|(
name|rowArray
argument_list|,
name|rowOffset
argument_list|,
name|rowLength
argument_list|,
name|HConstants
operator|.
name|LATEST_TIMESTAMP
argument_list|)
expr_stmt|;
block|}
comment|/**    * @param row row key; we make a copy of what we are passed to keep local.    * @param ts  timestamp    */
specifier|public
name|Put
parameter_list|(
name|ByteBuffer
name|row
parameter_list|,
name|long
name|ts
parameter_list|)
block|{
if|if
condition|(
name|ts
operator|<
literal|0
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Timestamp cannot be negative. ts="
operator|+
name|ts
argument_list|)
throw|;
block|}
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
name|this
operator|.
name|ts
operator|=
name|ts
expr_stmt|;
block|}
comment|/**    * @param row row key; we make a copy of what we are passed to keep local.    */
specifier|public
name|Put
parameter_list|(
name|ByteBuffer
name|row
parameter_list|)
block|{
name|this
argument_list|(
name|row
argument_list|,
name|HConstants
operator|.
name|LATEST_TIMESTAMP
argument_list|)
expr_stmt|;
block|}
comment|/**    * We make a copy of the passed in row key to keep local.    * @param rowArray    * @param rowOffset    * @param rowLength    * @param ts    */
specifier|public
name|Put
parameter_list|(
name|byte
index|[]
name|rowArray
parameter_list|,
name|int
name|rowOffset
parameter_list|,
name|int
name|rowLength
parameter_list|,
name|long
name|ts
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
name|this
operator|.
name|ts
operator|=
name|ts
expr_stmt|;
if|if
condition|(
name|ts
operator|<
literal|0
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Timestamp cannot be negative. ts="
operator|+
name|ts
argument_list|)
throw|;
block|}
block|}
comment|/**    * Create a Put operation for an immutable row key.    *    * @param row row key    * @param rowIsImmutable whether the input row is immutable.    *                       Set to true if the caller can guarantee that    *                       the row will not be changed for the Put duration.    */
specifier|public
name|Put
parameter_list|(
name|byte
index|[]
name|row
parameter_list|,
name|boolean
name|rowIsImmutable
parameter_list|)
block|{
name|this
argument_list|(
name|row
argument_list|,
name|HConstants
operator|.
name|LATEST_TIMESTAMP
argument_list|,
name|rowIsImmutable
argument_list|)
expr_stmt|;
block|}
comment|/**    * Create a Put operation for an immutable row key, using a given timestamp.    *    * @param row row key    * @param ts timestamp    * @param rowIsImmutable whether the input row is immutable.    *                       Set to true if the caller can guarantee that    *                       the row will not be changed for the Put duration.    */
specifier|public
name|Put
parameter_list|(
name|byte
index|[]
name|row
parameter_list|,
name|long
name|ts
parameter_list|,
name|boolean
name|rowIsImmutable
parameter_list|)
block|{
comment|// Check and set timestamp
if|if
condition|(
name|ts
operator|<
literal|0
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Timestamp cannot be negative. ts="
operator|+
name|ts
argument_list|)
throw|;
block|}
name|this
operator|.
name|ts
operator|=
name|ts
expr_stmt|;
comment|// Deal with row according to rowIsImmutable
name|checkRow
argument_list|(
name|row
argument_list|)
expr_stmt|;
if|if
condition|(
name|rowIsImmutable
condition|)
block|{
comment|// Row is immutable
name|this
operator|.
name|row
operator|=
name|row
expr_stmt|;
comment|// Do not make a local copy, but point to the provided byte array directly
block|}
else|else
block|{
comment|// Row is not immutable
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
literal|0
argument_list|,
name|row
operator|.
name|length
argument_list|)
expr_stmt|;
comment|// Make a local copy
block|}
block|}
comment|/**    * Copy constructor.  Creates a Put operation cloned from the specified Put.    * @param putToCopy put to copy    */
specifier|public
name|Put
parameter_list|(
name|Put
name|putToCopy
parameter_list|)
block|{
name|super
argument_list|(
name|putToCopy
argument_list|)
expr_stmt|;
block|}
comment|/**    * Construct the Put with user defined data. NOTED:    * 1) all cells in the familyMap must have the Type.Put    * 2) the row of each cell must be same with passed row.    * @param row row. CAN'T be null    * @param ts timestamp    * @param familyMap the map to collect all cells internally. CAN'T be null    */
specifier|public
name|Put
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
comment|/**    * Add the specified column and value to this Put operation.    * @param family family name    * @param qualifier column qualifier    * @param value column value    * @return this    */
specifier|public
name|Put
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
return|return
name|addColumn
argument_list|(
name|family
argument_list|,
name|qualifier
argument_list|,
name|this
operator|.
name|ts
argument_list|,
name|value
argument_list|)
return|;
block|}
comment|/**    * Add the specified column and value, with the specified timestamp as    * its version to this Put operation.    * @param family family name    * @param qualifier column qualifier    * @param ts version timestamp    * @param value column value    * @return this    */
specifier|public
name|Put
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
name|ts
parameter_list|,
name|byte
index|[]
name|value
parameter_list|)
block|{
if|if
condition|(
name|ts
operator|<
literal|0
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Timestamp cannot be negative. ts="
operator|+
name|ts
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
name|value
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
comment|/**    * Add the specified column and value, with the specified timestamp as    * its version to this Put operation.    * @param family family name    * @param qualifier column qualifier    * @param ts version timestamp    * @param value column value    * @return this    */
specifier|public
name|Put
name|addColumn
parameter_list|(
name|byte
index|[]
name|family
parameter_list|,
name|ByteBuffer
name|qualifier
parameter_list|,
name|long
name|ts
parameter_list|,
name|ByteBuffer
name|value
parameter_list|)
block|{
if|if
condition|(
name|ts
operator|<
literal|0
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Timestamp cannot be negative. ts="
operator|+
name|ts
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
name|value
argument_list|,
literal|null
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
comment|/**    * Add the specified KeyValue to this Put operation.  Operation assumes that    * the passed KeyValue is immutable and its backing array will not be modified    * for the duration of this Put.    * @param cell individual cell    * @return this    * @throws java.io.IOException e    */
specifier|public
name|Put
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
annotation|@
name|Override
specifier|public
name|Put
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
name|Put
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
name|Put
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
name|Put
name|setId
parameter_list|(
name|String
name|id
parameter_list|)
block|{
return|return
operator|(
name|Put
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
name|Put
name|setDurability
parameter_list|(
name|Durability
name|d
parameter_list|)
block|{
return|return
operator|(
name|Put
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
name|Put
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
name|Put
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
name|Put
name|setCellVisibility
parameter_list|(
name|CellVisibility
name|expression
parameter_list|)
block|{
return|return
operator|(
name|Put
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
name|Put
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
name|Put
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
name|Put
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
name|Put
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
name|Put
name|setTTL
parameter_list|(
name|long
name|ttl
parameter_list|)
block|{
return|return
operator|(
name|Put
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
name|Put
name|setPriority
parameter_list|(
name|int
name|priority
parameter_list|)
block|{
return|return
operator|(
name|Put
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

