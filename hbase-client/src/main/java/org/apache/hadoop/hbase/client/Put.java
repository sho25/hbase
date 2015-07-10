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
name|Tag
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

begin_comment
comment|/**  * Used to perform Put operations for a single row.  *<p>  * To perform a Put, instantiate a Put object with the row to insert to and  * for eachumn to be inserted, execute {@link #add(byte[], byte[], byte[]) add} or  * {@link #add(byte[], byte[], long, byte[]) add} if setting the timestamp.  */
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
name|Put
extends|extends
name|Mutation
implements|implements
name|HeapSize
implements|,
name|Comparable
argument_list|<
name|Row
argument_list|>
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
comment|/**    * Copy constructor.  Creates a Put operation cloned from the specified Put.    * @param putToCopy put to copy    */
specifier|public
name|Put
parameter_list|(
name|Put
name|putToCopy
parameter_list|)
block|{
name|this
argument_list|(
name|putToCopy
operator|.
name|getRow
argument_list|()
argument_list|,
name|putToCopy
operator|.
name|ts
argument_list|)
expr_stmt|;
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
name|List
argument_list|<
name|Cell
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
name|putToCopy
operator|.
name|getFamilyCellMap
argument_list|()
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|this
operator|.
name|familyMap
operator|.
name|put
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|,
operator|new
name|ArrayList
argument_list|<
name|Cell
argument_list|>
argument_list|(
name|entry
operator|.
name|getValue
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|this
operator|.
name|durability
operator|=
name|putToCopy
operator|.
name|durability
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
name|entry
range|:
name|putToCopy
operator|.
name|getAttributesMap
argument_list|()
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|this
operator|.
name|setAttribute
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|,
name|entry
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Add the specified column and value to this Put operation.    * @param family family name    * @param qualifier column qualifier    * @param value column value    * @return this    * @deprecated Since 1.0.0. Use {@link #addColumn(byte[], byte[], byte[])}    */
annotation|@
name|Deprecated
specifier|public
name|Put
name|add
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
name|value
argument_list|)
return|;
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
comment|/**    * See {@link #add(byte[], byte[], byte[])}. This version expects    * that the underlying arrays won't change. It's intended    * for usage internal HBase to and for advanced client applications.    */
specifier|public
name|Put
name|addImmutable
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
name|addImmutable
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
comment|/**    * This expects that the underlying arrays won't change. It's intended    * for usage internal HBase to and for advanced client applications.    *<p>Marked as audience Private as of 1.2.0. {@link Tag} is an internal implementation detail    * that should not be exposed publicly.    */
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
name|Put
name|addImmutable
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
parameter_list|,
name|Tag
index|[]
name|tag
parameter_list|)
block|{
return|return
name|addImmutable
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
argument_list|,
name|tag
argument_list|)
return|;
block|}
comment|/**    * Add the specified column and value, with the specified timestamp as    * its version to this Put operation.    * @param family family name    * @param qualifier column qualifier    * @param ts version timestamp    * @param value column value    * @return this    * @deprecated Since 1.0.0. Use {@link #addColumn(byte[], byte[], long, byte[])}    */
annotation|@
name|Deprecated
specifier|public
name|Put
name|add
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
return|return
name|addColumn
argument_list|(
name|family
argument_list|,
name|qualifier
argument_list|,
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
name|familyMap
operator|.
name|put
argument_list|(
name|CellUtil
operator|.
name|cloneFamily
argument_list|(
name|kv
argument_list|)
argument_list|,
name|list
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**    * See {@link #add(byte[], byte[], long, byte[])}. This version expects    * that the underlying arrays won't change. It's intended    * for usage internal HBase to and for advanced client applications.    */
specifier|public
name|Put
name|addImmutable
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
name|familyMap
operator|.
name|put
argument_list|(
name|family
argument_list|,
name|list
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**    * This expects that the underlying arrays won't change. It's intended    * for usage internal HBase to and for advanced client applications.    *<p>Marked as audience Private as of 1.2.0. {@link Tag} is an internal implementation detail    * that should not be exposed publicly.    */
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
name|Put
name|addImmutable
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
parameter_list|,
name|Tag
index|[]
name|tag
parameter_list|)
block|{
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
name|tag
argument_list|)
decl_stmt|;
name|list
operator|.
name|add
argument_list|(
name|kv
argument_list|)
expr_stmt|;
name|familyMap
operator|.
name|put
argument_list|(
name|family
argument_list|,
name|list
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**    * This expects that the underlying arrays won't change. It's intended    * for usage internal HBase to and for advanced client applications.    *<p>Marked as audience Private as of 1.2.0. {@link Tag} is an internal implementation detail    * that should not be exposed publicly.    */
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
name|Put
name|addImmutable
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
parameter_list|,
name|Tag
index|[]
name|tag
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
name|tag
argument_list|)
decl_stmt|;
name|list
operator|.
name|add
argument_list|(
name|kv
argument_list|)
expr_stmt|;
name|familyMap
operator|.
name|put
argument_list|(
name|family
argument_list|,
name|list
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**    * Add the specified column and value, with the specified timestamp as    * its version to this Put operation.    * @param family family name    * @param qualifier column qualifier    * @param ts version timestamp    * @param value column value    * @return this    * @deprecated Since 1.0.0. Use {@link Put#addColumn(byte[], ByteBuffer, long, ByteBuffer)}    */
annotation|@
name|Deprecated
specifier|public
name|Put
name|add
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
return|return
name|addColumn
argument_list|(
name|family
argument_list|,
name|qualifier
argument_list|,
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
name|familyMap
operator|.
name|put
argument_list|(
name|CellUtil
operator|.
name|cloneFamily
argument_list|(
name|kv
argument_list|)
argument_list|,
name|list
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**    * See {@link #add(byte[], ByteBuffer, long, ByteBuffer)}. This version expects    * that the underlying arrays won't change. It's intended    * for usage internal HBase to and for advanced client applications.    */
specifier|public
name|Put
name|addImmutable
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
name|familyMap
operator|.
name|put
argument_list|(
name|family
argument_list|,
name|list
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**    * Add the specified KeyValue to this Put operation.  Operation assumes that    * the passed KeyValue is immutable and its backing array will not be modified    * for the duration of this Put.    * @param kv individual KeyValue    * @return this    * @throws java.io.IOException e    */
specifier|public
name|Put
name|add
parameter_list|(
name|Cell
name|kv
parameter_list|)
throws|throws
name|IOException
block|{
name|byte
index|[]
name|family
init|=
name|CellUtil
operator|.
name|cloneFamily
argument_list|(
name|kv
argument_list|)
decl_stmt|;
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
comment|//Checking that the row of the kv is the same as the put
name|int
name|res
init|=
name|Bytes
operator|.
name|compareTo
argument_list|(
name|this
operator|.
name|row
argument_list|,
literal|0
argument_list|,
name|row
operator|.
name|length
argument_list|,
name|kv
operator|.
name|getRowArray
argument_list|()
argument_list|,
name|kv
operator|.
name|getRowOffset
argument_list|()
argument_list|,
name|kv
operator|.
name|getRowLength
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|res
operator|!=
literal|0
condition|)
block|{
throw|throw
operator|new
name|WrongRowIOException
argument_list|(
literal|"The row in "
operator|+
name|kv
operator|.
name|toString
argument_list|()
operator|+
literal|" doesn't match the original one "
operator|+
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|this
operator|.
name|row
argument_list|)
argument_list|)
throw|;
block|}
name|list
operator|.
name|add
argument_list|(
name|kv
argument_list|)
expr_stmt|;
name|familyMap
operator|.
name|put
argument_list|(
name|family
argument_list|,
name|list
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**    * A convenience method to determine if this object's familyMap contains    * a value assigned to the given family&amp; qualifier.    * Both given arguments must match the KeyValue object to return true.    *    * @param family column family    * @param qualifier column qualifier    * @return returns true if the given family and qualifier already has an    * existing KeyValue object in the family map.    */
specifier|public
name|boolean
name|has
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
return|return
name|has
argument_list|(
name|family
argument_list|,
name|qualifier
argument_list|,
name|this
operator|.
name|ts
argument_list|,
operator|new
name|byte
index|[
literal|0
index|]
argument_list|,
literal|true
argument_list|,
literal|true
argument_list|)
return|;
block|}
comment|/**    * A convenience method to determine if this object's familyMap contains    * a value assigned to the given family, qualifier and timestamp.    * All 3 given arguments must match the KeyValue object to return true.    *    * @param family column family    * @param qualifier column qualifier    * @param ts timestamp    * @return returns true if the given family, qualifier and timestamp already has an    * existing KeyValue object in the family map.    */
specifier|public
name|boolean
name|has
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
parameter_list|)
block|{
return|return
name|has
argument_list|(
name|family
argument_list|,
name|qualifier
argument_list|,
name|ts
argument_list|,
operator|new
name|byte
index|[
literal|0
index|]
argument_list|,
literal|false
argument_list|,
literal|true
argument_list|)
return|;
block|}
comment|/**    * A convenience method to determine if this object's familyMap contains    * a value assigned to the given family, qualifier and timestamp.    * All 3 given arguments must match the KeyValue object to return true.    *    * @param family column family    * @param qualifier column qualifier    * @param value value to check    * @return returns true if the given family, qualifier and value already has an    * existing KeyValue object in the family map.    */
specifier|public
name|boolean
name|has
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
name|has
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
argument_list|,
literal|true
argument_list|,
literal|false
argument_list|)
return|;
block|}
comment|/**    * A convenience method to determine if this object's familyMap contains    * the given value assigned to the given family, qualifier and timestamp.    * All 4 given arguments must match the KeyValue object to return true.    *    * @param family column family    * @param qualifier column qualifier    * @param ts timestamp    * @param value value to check    * @return returns true if the given family, qualifier timestamp and value    * already has an existing KeyValue object in the family map.    */
specifier|public
name|boolean
name|has
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
return|return
name|has
argument_list|(
name|family
argument_list|,
name|qualifier
argument_list|,
name|ts
argument_list|,
name|value
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|)
return|;
block|}
comment|/*    * Private method to determine if this object's familyMap contains    * the given value assigned to the given family, qualifier and timestamp    * respecting the 2 boolean arguments    *    * @param family    * @param qualifier    * @param ts    * @param value    * @param ignoreTS    * @param ignoreValue    * @return returns true if the given family, qualifier timestamp and value    * already has an existing KeyValue object in the family map.    */
specifier|private
name|boolean
name|has
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
parameter_list|,
name|boolean
name|ignoreTS
parameter_list|,
name|boolean
name|ignoreValue
parameter_list|)
block|{
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
if|if
condition|(
name|list
operator|.
name|size
argument_list|()
operator|==
literal|0
condition|)
block|{
return|return
literal|false
return|;
block|}
comment|// Boolean analysis of ignoreTS/ignoreValue.
comment|// T T => 2
comment|// T F => 3 (first is always true)
comment|// F T => 2
comment|// F F => 1
if|if
condition|(
operator|!
name|ignoreTS
operator|&&
operator|!
name|ignoreValue
condition|)
block|{
for|for
control|(
name|Cell
name|cell
range|:
name|list
control|)
block|{
if|if
condition|(
name|CellUtil
operator|.
name|matchingFamily
argument_list|(
name|cell
argument_list|,
name|family
argument_list|)
operator|&&
name|CellUtil
operator|.
name|matchingQualifier
argument_list|(
name|cell
argument_list|,
name|qualifier
argument_list|)
operator|&&
name|CellUtil
operator|.
name|matchingValue
argument_list|(
name|cell
argument_list|,
name|value
argument_list|)
operator|&&
name|cell
operator|.
name|getTimestamp
argument_list|()
operator|==
name|ts
condition|)
block|{
return|return
literal|true
return|;
block|}
block|}
block|}
elseif|else
if|if
condition|(
name|ignoreValue
operator|&&
operator|!
name|ignoreTS
condition|)
block|{
for|for
control|(
name|Cell
name|cell
range|:
name|list
control|)
block|{
if|if
condition|(
name|CellUtil
operator|.
name|matchingFamily
argument_list|(
name|cell
argument_list|,
name|family
argument_list|)
operator|&&
name|CellUtil
operator|.
name|matchingQualifier
argument_list|(
name|cell
argument_list|,
name|qualifier
argument_list|)
operator|&&
name|cell
operator|.
name|getTimestamp
argument_list|()
operator|==
name|ts
condition|)
block|{
return|return
literal|true
return|;
block|}
block|}
block|}
elseif|else
if|if
condition|(
operator|!
name|ignoreValue
operator|&&
name|ignoreTS
condition|)
block|{
for|for
control|(
name|Cell
name|cell
range|:
name|list
control|)
block|{
if|if
condition|(
name|CellUtil
operator|.
name|matchingFamily
argument_list|(
name|cell
argument_list|,
name|family
argument_list|)
operator|&&
name|CellUtil
operator|.
name|matchingQualifier
argument_list|(
name|cell
argument_list|,
name|qualifier
argument_list|)
operator|&&
name|CellUtil
operator|.
name|matchingValue
argument_list|(
name|cell
argument_list|,
name|value
argument_list|)
condition|)
block|{
return|return
literal|true
return|;
block|}
block|}
block|}
else|else
block|{
for|for
control|(
name|Cell
name|cell
range|:
name|list
control|)
block|{
if|if
condition|(
name|CellUtil
operator|.
name|matchingFamily
argument_list|(
name|cell
argument_list|,
name|family
argument_list|)
operator|&&
name|CellUtil
operator|.
name|matchingQualifier
argument_list|(
name|cell
argument_list|,
name|qualifier
argument_list|)
condition|)
block|{
return|return
literal|true
return|;
block|}
block|}
block|}
return|return
literal|false
return|;
block|}
comment|/**    * Returns a list of all KeyValue objects with matching column family and qualifier.    *    * @param family column family    * @param qualifier column qualifier    * @return a list of KeyValue objects with the matching family and qualifier,    * returns an empty list if one doesn't exist for the given family.    */
specifier|public
name|List
argument_list|<
name|Cell
argument_list|>
name|get
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
name|List
argument_list|<
name|Cell
argument_list|>
name|filteredList
init|=
operator|new
name|ArrayList
argument_list|<
name|Cell
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|Cell
name|cell
range|:
name|getCellList
argument_list|(
name|family
argument_list|)
control|)
block|{
if|if
condition|(
name|CellUtil
operator|.
name|matchingQualifier
argument_list|(
name|cell
argument_list|,
name|qualifier
argument_list|)
condition|)
block|{
name|filteredList
operator|.
name|add
argument_list|(
name|cell
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|filteredList
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
name|setFamilyCellMap
parameter_list|(
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
parameter_list|)
block|{
return|return
operator|(
name|Put
operator|)
name|super
operator|.
name|setFamilyCellMap
argument_list|(
name|map
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
block|}
end_class

end_unit

