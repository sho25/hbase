begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Copyright 2010 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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

begin_comment
comment|/**  * Used to perform Put operations for a single row.  *<p>  * To perform a Put, instantiate a Put object with the row to insert to and  * for each column to be inserted, execute {@link #add(byte[], byte[], byte[]) add} or  * {@link #add(byte[], byte[], long, byte[]) add} if setting the timestamp.  */
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
name|Writable
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
name|PUT_VERSION
init|=
operator|(
name|byte
operator|)
literal|2
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
literal|2
operator|*
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
name|this
argument_list|(
name|row
argument_list|,
name|HConstants
operator|.
name|LATEST_TIMESTAMP
argument_list|,
name|rowLock
argument_list|)
expr_stmt|;
block|}
comment|/**    * Create a Put operation for the specified row, using a given timestamp.    *    * @param row row key    * @param ts timestamp    */
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
name|ts
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
comment|/**    * Create a Put operation for the specified row, using a given timestamp, and an existing row lock.    * @param row row key    * @param ts timestamp    * @param rowLock previously acquired row lock, or null    */
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
name|this
operator|.
name|ts
operator|=
name|ts
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
name|ts
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
name|this
operator|.
name|writeToWAL
operator|=
name|putToCopy
operator|.
name|writeToWAL
expr_stmt|;
block|}
comment|/**    * Add the specified column and value to this Put operation.    * @param family family name    * @param qualifier column qualifier    * @param value column value    * @return this    */
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
name|ts
argument_list|,
name|value
argument_list|)
return|;
block|}
comment|/**    * Add the specified column and value, with the specified timestamp as    * its version to this Put operation.    * @param family family name    * @param qualifier column qualifier    * @param ts version timestamp    * @param value column value    * @return this    */
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
name|getKeyValueList
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
comment|/**    * Add the specified KeyValue to this Put operation.  Operation assumes that    * the passed KeyValue is immutable and its backing array will not be modified    * for the duration of this Put.    * @param kv individual KeyValue    * @return this    * @throws java.io.IOException e    */
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
name|getKeyValueList
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
comment|/*    * Create a KeyValue with this objects row key and the Put identifier.    *    * @return a KeyValue with this objects row key and the Put identifier.    */
specifier|private
name|KeyValue
name|createPutKeyValue
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
return|;
block|}
comment|/**    * A convenience method to determine if this object's familyMap contains    * a value assigned to the given family& qualifier.    * Both given arguments must match the KeyValue object to return true.    *    * @param family column family    * @param qualifier column qualifier    * @return returns true if the given family and qualifier already has an    * existing KeyValue object in the family map.    */
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
name|KeyValue
argument_list|>
name|list
init|=
name|getKeyValueList
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
name|KeyValue
name|kv
range|:
name|list
control|)
block|{
if|if
condition|(
name|Arrays
operator|.
name|equals
argument_list|(
name|kv
operator|.
name|getFamily
argument_list|()
argument_list|,
name|family
argument_list|)
operator|&&
name|Arrays
operator|.
name|equals
argument_list|(
name|kv
operator|.
name|getQualifier
argument_list|()
argument_list|,
name|qualifier
argument_list|)
operator|&&
name|Arrays
operator|.
name|equals
argument_list|(
name|kv
operator|.
name|getValue
argument_list|()
argument_list|,
name|value
argument_list|)
operator|&&
name|kv
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
name|KeyValue
name|kv
range|:
name|list
control|)
block|{
if|if
condition|(
name|Arrays
operator|.
name|equals
argument_list|(
name|kv
operator|.
name|getFamily
argument_list|()
argument_list|,
name|family
argument_list|)
operator|&&
name|Arrays
operator|.
name|equals
argument_list|(
name|kv
operator|.
name|getQualifier
argument_list|()
argument_list|,
name|qualifier
argument_list|)
operator|&&
name|kv
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
name|KeyValue
name|kv
range|:
name|list
control|)
block|{
if|if
condition|(
name|Arrays
operator|.
name|equals
argument_list|(
name|kv
operator|.
name|getFamily
argument_list|()
argument_list|,
name|family
argument_list|)
operator|&&
name|Arrays
operator|.
name|equals
argument_list|(
name|kv
operator|.
name|getQualifier
argument_list|()
argument_list|,
name|qualifier
argument_list|)
operator|&&
name|Arrays
operator|.
name|equals
argument_list|(
name|kv
operator|.
name|getValue
argument_list|()
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
name|KeyValue
name|kv
range|:
name|list
control|)
block|{
if|if
condition|(
name|Arrays
operator|.
name|equals
argument_list|(
name|kv
operator|.
name|getFamily
argument_list|()
argument_list|,
name|family
argument_list|)
operator|&&
name|Arrays
operator|.
name|equals
argument_list|(
name|kv
operator|.
name|getQualifier
argument_list|()
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
comment|/**    * Returns a list of all KeyValue objects with matching column family and qualifier.    *    * @param family column family    * @param qualifier column qualifier    * @return a list of KeyValue objects with the matching family and qualifier,    * returns an empty list if one doesnt exist for the given family.    */
specifier|public
name|List
argument_list|<
name|KeyValue
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
name|KeyValue
argument_list|>
name|filteredList
init|=
operator|new
name|ArrayList
argument_list|<
name|KeyValue
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|KeyValue
name|kv
range|:
name|getKeyValueList
argument_list|(
name|family
argument_list|)
control|)
block|{
if|if
condition|(
name|Arrays
operator|.
name|equals
argument_list|(
name|kv
operator|.
name|getQualifier
argument_list|()
argument_list|,
name|qualifier
argument_list|)
condition|)
block|{
name|filteredList
operator|.
name|add
argument_list|(
name|kv
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|filteredList
return|;
block|}
comment|/**    * Creates an empty list if one doesnt exist for the given column family    * or else it returns the associated list of KeyValue objects.    *    * @param family column family    * @return a list of KeyValue objects, returns an empty list if one doesnt exist.    */
specifier|private
name|List
argument_list|<
name|KeyValue
argument_list|>
name|getKeyValueList
parameter_list|(
name|byte
index|[]
name|family
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
return|return
name|list
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
name|heapsize
operator|+=
name|getAttributeSize
argument_list|()
expr_stmt|;
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
name|PUT_VERSION
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"version not supported"
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
name|ts
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
if|if
condition|(
name|version
operator|>
literal|1
condition|)
block|{
name|readAttributes
argument_list|(
name|in
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
name|out
operator|.
name|writeByte
argument_list|(
name|PUT_VERSION
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
name|ts
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
name|writeAttributes
argument_list|(
name|out
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

