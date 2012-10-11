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

begin_comment
comment|/**  * Used to perform Increment operations on a single row.  *<p>  * This operation does not appear atomic to readers.  Increments are done  * under a single row lock, so write operations to a row are synchronized, but  * readers do not take row locks so get and scan operations can see this  * operation partially completed.  *<p>  * To increment columns of a row, instantiate an Increment object with the row  * to increment.  At least one column to increment must be specified using the  * {@link #addColumn(byte[], byte[], long)} method.  */
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
name|Increment
implements|implements
name|Row
block|{
specifier|private
specifier|static
specifier|final
name|byte
name|INCREMENT_VERSION
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
name|boolean
name|writeToWAL
init|=
literal|true
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
name|NavigableMap
argument_list|<
name|byte
index|[]
argument_list|,
name|Long
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
name|NavigableMap
argument_list|<
name|byte
index|[]
argument_list|,
name|Long
argument_list|>
argument_list|>
argument_list|(
name|Bytes
operator|.
name|BYTES_COMPARATOR
argument_list|)
decl_stmt|;
comment|/** Constructor for Writable.  DO NOT USE */
specifier|public
name|Increment
parameter_list|()
block|{}
comment|/**    * Create a Increment operation for the specified row.    *<p>    * At least one column must be incremented.    * @param row row key    */
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
literal|null
argument_list|)
expr_stmt|;
block|}
comment|/**    * Create a Increment operation for the specified row, using an existing row    * lock.    *<p>    * At least one column must be incremented.    * @param row row key    * @param rowLock previously acquired row lock, or null    */
specifier|public
name|Increment
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
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Cannot increment a null row"
argument_list|)
throw|;
block|}
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
if|if
condition|(
name|qualifier
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"qualifier cannot be null"
argument_list|)
throw|;
block|}
name|NavigableMap
argument_list|<
name|byte
index|[]
argument_list|,
name|Long
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
name|TreeMap
argument_list|<
name|byte
index|[]
argument_list|,
name|Long
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
name|put
argument_list|(
name|qualifier
argument_list|,
name|amount
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
comment|/* Accessors */
comment|/**    * Method for retrieving the increment's row    * @return row    */
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
comment|/**    * Method for retrieving the increment's RowLock    * @return RowLock    */
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
comment|/**    * Method for retrieving the increment's lockId    * @return lockId    */
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
comment|/**    * Method for retrieving whether WAL will be written to or not    * @return true if WAL should be used, false if not    */
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
comment|/**    * Sets whether this operation should write to the WAL or not.    * @param writeToWAL true if WAL should be used, false if not    * @return this increment operation    */
specifier|public
name|Increment
name|setWriteToWAL
parameter_list|(
name|boolean
name|writeToWAL
parameter_list|)
block|{
name|this
operator|.
name|writeToWAL
operator|=
name|writeToWAL
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
comment|/**    * Sets the TimeRange to be used on the Get for this increment.    *<p>    * This is useful for when you have counters that only last for specific    * periods of time (ie. counters that are partitioned by time).  By setting    * the range of valid times for this increment, you can potentially gain    * some performance with a more optimal Get operation.    *<p>    * This range is used as [minStamp, maxStamp).    * @param minStamp minimum timestamp value, inclusive    * @param maxStamp maximum timestamp value, exclusive    * @throws IOException if invalid time range    * @return this    */
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
comment|/**    * Method for retrieving the number of families to increment from    * @return number of families    */
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
comment|/**    * Method for retrieving the number of columns to increment    * @return number of columns across all families    */
specifier|public
name|int
name|numColumns
parameter_list|()
block|{
if|if
condition|(
operator|!
name|hasFamilies
argument_list|()
condition|)
return|return
literal|0
return|;
name|int
name|num
init|=
literal|0
decl_stmt|;
for|for
control|(
name|NavigableMap
argument_list|<
name|byte
index|[]
argument_list|,
name|Long
argument_list|>
name|family
range|:
name|familyMap
operator|.
name|values
argument_list|()
control|)
block|{
name|num
operator|+=
name|family
operator|.
name|size
argument_list|()
expr_stmt|;
block|}
return|return
name|num
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
comment|/**    * Method for retrieving the increment's familyMap    * @return familyMap    */
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
name|getFamilyMap
parameter_list|()
block|{
return|return
name|this
operator|.
name|familyMap
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
name|NavigableMap
argument_list|<
name|byte
index|[]
argument_list|,
name|Long
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
name|Map
operator|.
name|Entry
argument_list|<
name|byte
index|[]
argument_list|,
name|Long
argument_list|>
name|column
range|:
name|entry
operator|.
name|getValue
argument_list|()
operator|.
name|entrySet
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
operator|.
name|getKey
argument_list|()
argument_list|)
operator|+
literal|"+="
operator|+
name|column
operator|.
name|getValue
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
name|INCREMENT_VERSION
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
name|this
operator|.
name|lockId
operator|=
name|in
operator|.
name|readLong
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
name|numFamilies
operator|==
literal|0
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"At least one column required"
argument_list|)
throw|;
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
name|Long
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
name|NavigableMap
argument_list|<
name|byte
index|[]
argument_list|,
name|Long
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
name|TreeMap
argument_list|<
name|byte
index|[]
argument_list|,
name|Long
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
name|put
argument_list|(
name|qualifier
argument_list|,
name|in
operator|.
name|readLong
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"At least one column required per family"
argument_list|)
throw|;
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
if|if
condition|(
name|version
operator|>
literal|1
condition|)
block|{
name|this
operator|.
name|writeToWAL
operator|=
name|in
operator|.
name|readBoolean
argument_list|()
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
name|INCREMENT_VERSION
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
name|tr
operator|.
name|write
argument_list|(
name|out
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
if|if
condition|(
name|familyMap
operator|.
name|size
argument_list|()
operator|==
literal|0
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"At least one column required"
argument_list|)
throw|;
block|}
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
name|NavigableMap
argument_list|<
name|byte
index|[]
argument_list|,
name|Long
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
name|NavigableMap
argument_list|<
name|byte
index|[]
argument_list|,
name|Long
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
throw|throw
operator|new
name|IOException
argument_list|(
literal|"At least one column required per family"
argument_list|)
throw|;
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
name|Map
operator|.
name|Entry
argument_list|<
name|byte
index|[]
argument_list|,
name|Long
argument_list|>
name|qualifier
range|:
name|columnSet
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
name|qualifier
operator|.
name|getKey
argument_list|()
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeLong
argument_list|(
name|qualifier
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
name|out
operator|.
name|writeBoolean
argument_list|(
name|writeToWAL
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|compareTo
parameter_list|(
name|Row
name|i
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
name|i
operator|.
name|getRow
argument_list|()
argument_list|)
return|;
block|}
block|}
end_class

end_unit

