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
name|ArrayList
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

begin_comment
comment|/**   * Used to perform Put operations for a single row.  *<p>  * To perform a Put, instantiate a Put object with the row to insert to and  * for each column to be inserted, execute {@link #add(byte[], byte[], byte[]) add} or  * {@link #add(byte[], byte[], long, byte[]) add} if setting the timestamp.  */
end_comment

begin_class
specifier|public
class|class
name|Put
implements|implements
name|HeapSize
implements|,
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
name|byte
index|[]
name|row
init|=
literal|null
decl_stmt|;
specifier|private
name|long
name|timestamp
init|=
name|HConstants
operator|.
name|LATEST_TIMESTAMP
decl_stmt|;
specifier|private
name|long
name|lockId
init|=
operator|-
literal|1L
decl_stmt|;
specifier|private
name|boolean
name|writeToWAL
init|=
literal|true
decl_stmt|;
specifier|private
name|Map
argument_list|<
name|byte
index|[]
argument_list|,
name|List
argument_list|<
name|KeyValue
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
name|List
argument_list|<
name|KeyValue
argument_list|>
argument_list|>
argument_list|(
name|Bytes
operator|.
name|BYTES_COMPARATOR
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|long
name|OVERHEAD
init|=
name|ClassSize
operator|.
name|align
argument_list|(
name|ClassSize
operator|.
name|OBJECT
operator|+
name|ClassSize
operator|.
name|REFERENCE
operator|+
literal|2
operator|*
name|Bytes
operator|.
name|SIZEOF_LONG
operator|+
name|Bytes
operator|.
name|SIZEOF_BOOLEAN
operator|+
name|ClassSize
operator|.
name|REFERENCE
operator|+
name|ClassSize
operator|.
name|TREEMAP
argument_list|)
decl_stmt|;
comment|/** Constructor for Writable. DO NOT USE */
specifier|public
name|Put
parameter_list|()
block|{}
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
literal|null
argument_list|)
expr_stmt|;
block|}
comment|/**    * Create a Put operation for the specified row, using an existing row lock.    * @param row row key    * @param rowLock previously acquired row lock, or null    */
specifier|public
name|Put
parameter_list|(
name|byte
index|[]
name|row
parameter_list|,
name|RowLock
name|rowLock
parameter_list|)
block|{
if|if
condition|(
name|row
operator|==
literal|null
operator|||
name|row
operator|.
name|length
operator|>
name|HConstants
operator|.
name|MAX_ROW_LENGTH
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Row key is invalid"
argument_list|)
throw|;
block|}
name|this
operator|.
name|row
operator|=
name|Arrays
operator|.
name|copyOf
argument_list|(
name|row
argument_list|,
name|row
operator|.
name|length
argument_list|)
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
name|getRowLock
argument_list|()
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
name|KeyValue
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
name|KeyValue
argument_list|>
argument_list|>
name|entry
range|:
name|putToCopy
operator|.
name|getFamilyMap
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
name|entry
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Add the specified column and value to this Put operation.    * @param family family name    * @param qualifier column qualifier    * @param value column value    */
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
name|add
argument_list|(
name|family
argument_list|,
name|qualifier
argument_list|,
name|this
operator|.
name|timestamp
argument_list|,
name|value
argument_list|)
return|;
block|}
comment|/**    * Add the specified column and value, with the specified timestamp as     * its version to this Put operation.    * @param column Old style column name with family and qualifier put together    * with a colon.    * @param ts version timestamp    * @param value column value    */
specifier|public
name|Put
name|add
parameter_list|(
name|byte
index|[]
name|column
parameter_list|,
name|long
name|ts
parameter_list|,
name|byte
index|[]
name|value
parameter_list|)
block|{
name|byte
index|[]
index|[]
name|parts
init|=
name|KeyValue
operator|.
name|parseColumn
argument_list|(
name|column
argument_list|)
decl_stmt|;
return|return
name|add
argument_list|(
name|parts
index|[
literal|0
index|]
argument_list|,
name|parts
index|[
literal|1
index|]
argument_list|,
name|ts
argument_list|,
name|value
argument_list|)
return|;
block|}
comment|/**    * Add the specified column and value, with the specified timestamp as     * its version to this Put operation.    * @param family family name    * @param qualifier column qualifier    * @param ts version timestamp    * @param value column value    */
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
name|List
argument_list|<
name|KeyValue
argument_list|>
name|list
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
name|list
operator|==
literal|null
condition|)
block|{
name|list
operator|=
operator|new
name|ArrayList
argument_list|<
name|KeyValue
argument_list|>
argument_list|(
literal|0
argument_list|)
expr_stmt|;
block|}
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
name|kv
operator|.
name|getFamily
argument_list|()
argument_list|,
name|list
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**    * Add the specified KeyValue to this Put operation.  Operation assumes that     * the passed KeyValue is immutable and its backing array will not be modified    * for the duration of this Put.    * @param kv    */
specifier|public
name|Put
name|add
parameter_list|(
name|KeyValue
name|kv
parameter_list|)
throws|throws
name|IOException
block|{
name|byte
index|[]
name|family
init|=
name|kv
operator|.
name|getFamily
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|KeyValue
argument_list|>
name|list
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
name|list
operator|==
literal|null
condition|)
block|{
name|list
operator|=
operator|new
name|ArrayList
argument_list|<
name|KeyValue
argument_list|>
argument_list|()
expr_stmt|;
block|}
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
name|getBuffer
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
name|IOException
argument_list|(
literal|"The row in the recently added KeyValue "
operator|+
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|kv
operator|.
name|getBuffer
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
comment|/**    * Method for retrieving the put's familyMap    * @return familyMap    */
specifier|public
name|Map
argument_list|<
name|byte
index|[]
argument_list|,
name|List
argument_list|<
name|KeyValue
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
comment|/**    * Method for retrieving the put's row    * @return row     */
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
comment|/**    * Method for retrieving the put's RowLock    * @return RowLock    */
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
comment|/**    * Method for retrieving the put's lockId    * @return lockId    */
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
comment|/**    * Method to check if the familyMap is empty    * @return true if empty, false otherwise    */
specifier|public
name|boolean
name|isEmpty
parameter_list|()
block|{
return|return
name|familyMap
operator|.
name|isEmpty
argument_list|()
return|;
block|}
comment|/**    * @return Timestamp    */
specifier|public
name|long
name|getTimeStamp
parameter_list|()
block|{
return|return
name|this
operator|.
name|timestamp
return|;
block|}
comment|/**    * Method for setting the timestamp    * @param timestamp    */
specifier|public
name|Put
name|setTimeStamp
parameter_list|(
name|long
name|timestamp
parameter_list|)
block|{
name|this
operator|.
name|timestamp
operator|=
name|timestamp
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**    * @return the number of different families included in this put     */
specifier|public
name|int
name|numFamilies
parameter_list|()
block|{
return|return
name|familyMap
operator|.
name|size
argument_list|()
return|;
block|}
comment|/**    * @return the total number of KeyValues that will be added with this put    */
specifier|public
name|int
name|size
parameter_list|()
block|{
name|int
name|size
init|=
literal|0
decl_stmt|;
for|for
control|(
name|List
argument_list|<
name|KeyValue
argument_list|>
name|kvList
range|:
name|this
operator|.
name|familyMap
operator|.
name|values
argument_list|()
control|)
block|{
name|size
operator|+=
name|kvList
operator|.
name|size
argument_list|()
expr_stmt|;
block|}
return|return
name|size
return|;
block|}
comment|/**    * @return true if edits should be applied to WAL, false if not    */
specifier|public
name|boolean
name|getWriteToWAL
parameter_list|()
block|{
return|return
name|this
operator|.
name|writeToWAL
return|;
block|}
comment|/**    * Set whether this Put should be written to the WAL or not.    * Not writing the WAL means you may lose edits on server crash.    * @param write true if edits should be written to WAL, false if not    */
specifier|public
name|void
name|setWriteToWAL
parameter_list|(
name|boolean
name|write
parameter_list|)
block|{
name|this
operator|.
name|writeToWAL
operator|=
name|write
expr_stmt|;
block|}
comment|/**    * @return String     */
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
literal|"row="
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
literal|", families={"
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
name|KeyValue
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
literal|", keyvalues=("
argument_list|)
expr_stmt|;
name|boolean
name|moreThanOneB
init|=
literal|false
decl_stmt|;
for|for
control|(
name|KeyValue
name|kv
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
literal|")"
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
specifier|public
name|int
name|compareTo
parameter_list|(
name|Row
name|p
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
name|p
operator|.
name|getRow
argument_list|()
argument_list|)
return|;
block|}
comment|//HeapSize
specifier|public
name|long
name|heapSize
parameter_list|()
block|{
name|long
name|heapsize
init|=
name|OVERHEAD
decl_stmt|;
comment|//Adding row
name|heapsize
operator|+=
name|ClassSize
operator|.
name|align
argument_list|(
name|ClassSize
operator|.
name|ARRAY
operator|+
name|this
operator|.
name|row
operator|.
name|length
argument_list|)
expr_stmt|;
comment|//Adding map overhead
name|heapsize
operator|+=
name|ClassSize
operator|.
name|align
argument_list|(
name|this
operator|.
name|familyMap
operator|.
name|size
argument_list|()
operator|*
name|ClassSize
operator|.
name|MAP_ENTRY
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
name|KeyValue
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
comment|//Adding key overhead
name|heapsize
operator|+=
name|ClassSize
operator|.
name|align
argument_list|(
name|ClassSize
operator|.
name|ARRAY
operator|+
name|entry
operator|.
name|getKey
argument_list|()
operator|.
name|length
argument_list|)
expr_stmt|;
comment|//This part is kinds tricky since the JVM can reuse references if you
comment|//store the same value, but have a good match with SizeOf at the moment
comment|//Adding value overhead
name|heapsize
operator|+=
name|ClassSize
operator|.
name|align
argument_list|(
name|ClassSize
operator|.
name|ARRAYLIST
argument_list|)
expr_stmt|;
name|int
name|size
init|=
name|entry
operator|.
name|getValue
argument_list|()
operator|.
name|size
argument_list|()
decl_stmt|;
name|heapsize
operator|+=
name|ClassSize
operator|.
name|align
argument_list|(
name|ClassSize
operator|.
name|ARRAY
operator|+
name|size
operator|*
name|ClassSize
operator|.
name|REFERENCE
argument_list|)
expr_stmt|;
for|for
control|(
name|KeyValue
name|kv
range|:
name|entry
operator|.
name|getValue
argument_list|()
control|)
block|{
name|heapsize
operator|+=
name|kv
operator|.
name|heapSize
argument_list|()
expr_stmt|;
block|}
block|}
return|return
name|ClassSize
operator|.
name|align
argument_list|(
operator|(
name|int
operator|)
name|heapsize
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
name|timestamp
operator|=
name|in
operator|.
name|readLong
argument_list|()
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
name|writeToWAL
operator|=
name|in
operator|.
name|readBoolean
argument_list|()
expr_stmt|;
name|int
name|numFamilies
init|=
name|in
operator|.
name|readInt
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|this
operator|.
name|familyMap
operator|.
name|isEmpty
argument_list|()
condition|)
name|this
operator|.
name|familyMap
operator|.
name|clear
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
name|int
name|numKeys
init|=
name|in
operator|.
name|readInt
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|KeyValue
argument_list|>
name|keys
init|=
operator|new
name|ArrayList
argument_list|<
name|KeyValue
argument_list|>
argument_list|(
name|numKeys
argument_list|)
decl_stmt|;
name|int
name|totalLen
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
name|totalLen
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
name|keys
operator|.
name|add
argument_list|(
operator|new
name|KeyValue
argument_list|(
name|buf
argument_list|,
name|offset
argument_list|,
name|keyLength
argument_list|)
argument_list|)
expr_stmt|;
name|offset
operator|+=
name|keyLength
expr_stmt|;
block|}
name|this
operator|.
name|familyMap
operator|.
name|put
argument_list|(
name|family
argument_list|,
name|keys
argument_list|)
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
name|timestamp
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
name|writeBoolean
argument_list|(
name|this
operator|.
name|writeToWAL
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
name|List
argument_list|<
name|KeyValue
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
name|List
argument_list|<
name|KeyValue
argument_list|>
name|keys
init|=
name|entry
operator|.
name|getValue
argument_list|()
decl_stmt|;
name|out
operator|.
name|writeInt
argument_list|(
name|keys
operator|.
name|size
argument_list|()
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
name|keys
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
name|keys
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
block|}
end_class

end_unit

