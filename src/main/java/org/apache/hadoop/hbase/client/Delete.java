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
comment|/**  * Used to perform Delete operations on a single row.  *<p>  * To delete an entire row, instantiate a Delete object with the row  * to delete.  To further define the scope of what to delete, perform  * additional methods as outlined below.  *<p>  * To delete specific families, execute {@link #deleteFamily(byte[]) deleteFamily}  * for each family to delete.  *<p>  * To delete multiple versions of specific columns, execute  * {@link #deleteColumns(byte[], byte[]) deleteColumns}  * for each column to delete.  *<p>  * To delete specific versions of specific columns, execute  * {@link #deleteColumn(byte[], byte[], long) deleteColumn}  * for each column version to delete.  *<p>  * Specifying timestamps, deleteFamily and deleteColumns will delete all  * versions with a timestamp less than or equal to that passed.  If no  * timestamp is specified, an entry is added with a timestamp of 'now'  * where 'now' is the servers's System.currentTimeMillis().  * Specifying a timestamp to the deleteColumn method will  * delete versions only with a timestamp equal to that specified.  * If no timestamp is passed to deleteColumn, internally, it figures the  * most recent cell's timestamp and adds a delete at that timestamp; i.e.  * it deletes the most recently added cell.  *<p>The timestamp passed to the constructor is used ONLY for delete of  * rows.  For anything less -- a deleteColumn, deleteColumns or  * deleteFamily -- then you need to use the method overrides that take a  * timestamp.  The constructor timestamp is not referenced.  */
end_comment

begin_class
specifier|public
class|class
name|Delete
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
name|DELETE_VERSION
init|=
operator|(
name|byte
operator|)
literal|1
decl_stmt|;
specifier|private
name|byte
index|[]
name|row
init|=
literal|null
decl_stmt|;
comment|// This ts is only used when doing a deleteRow.  Anything less,
specifier|private
name|long
name|ts
decl_stmt|;
specifier|private
name|long
name|lockId
init|=
operator|-
literal|1L
decl_stmt|;
specifier|private
specifier|final
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
comment|/** Constructor for Writable.  DO NOT USE */
specifier|public
name|Delete
parameter_list|()
block|{
name|this
argument_list|(
operator|(
name|byte
index|[]
operator|)
literal|null
argument_list|)
expr_stmt|;
block|}
comment|/**    * Create a Delete operation for the specified row.    *<p>    * If no further operations are done, this will delete everything    * associated with the specified row (all versions of all columns in all    * families).    * @param row row key    */
specifier|public
name|Delete
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
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
comment|/**    * Create a Delete operation for the specified row and timestamp, using    * an optional row lock.<p>    *    * If no further operations are done, this will delete all columns in all    * families of the specified row with a timestamp less than or equal to the    * specified timestamp.<p>    *    * This timestamp is ONLY used for a delete row operation.  If specifying    * families or columns, you must specify each timestamp individually.    * @param row row key    * @param timestamp maximum version timestamp (only for delete row)    * @param rowLock previously acquired row lock, or null    */
specifier|public
name|Delete
parameter_list|(
name|byte
index|[]
name|row
parameter_list|,
name|long
name|timestamp
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
name|this
operator|.
name|ts
operator|=
name|timestamp
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
comment|/**    * @param d Delete to clone.    */
specifier|public
name|Delete
parameter_list|(
specifier|final
name|Delete
name|d
parameter_list|)
block|{
name|this
operator|.
name|row
operator|=
name|d
operator|.
name|getRow
argument_list|()
expr_stmt|;
name|this
operator|.
name|ts
operator|=
name|d
operator|.
name|getTimeStamp
argument_list|()
expr_stmt|;
name|this
operator|.
name|lockId
operator|=
name|d
operator|.
name|getLockId
argument_list|()
expr_stmt|;
name|this
operator|.
name|familyMap
operator|.
name|putAll
argument_list|(
name|d
operator|.
name|getFamilyMap
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|public
name|int
name|compareTo
parameter_list|(
specifier|final
name|Row
name|d
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
name|d
operator|.
name|getRow
argument_list|()
argument_list|)
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
comment|/**    * Delete all versions of all columns of the specified family.    *<p>    * Overrides previous calls to deleteColumn and deleteColumns for the    * specified family.    * @param family family name    * @return this for invocation chaining    */
specifier|public
name|Delete
name|deleteFamily
parameter_list|(
name|byte
index|[]
name|family
parameter_list|)
block|{
name|this
operator|.
name|deleteFamily
argument_list|(
name|family
argument_list|,
name|HConstants
operator|.
name|LATEST_TIMESTAMP
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**    * Delete all columns of the specified family with a timestamp less than    * or equal to the specified timestamp.    *<p>    * Overrides previous calls to deleteColumn and deleteColumns for the    * specified family.    * @param family family name    * @param timestamp maximum version timestamp    * @return this for invocation chaining    */
specifier|public
name|Delete
name|deleteFamily
parameter_list|(
name|byte
index|[]
name|family
parameter_list|,
name|long
name|timestamp
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
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
operator|!
name|list
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|list
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
name|list
operator|.
name|add
argument_list|(
operator|new
name|KeyValue
argument_list|(
name|row
argument_list|,
name|family
argument_list|,
literal|null
argument_list|,
name|timestamp
argument_list|,
name|KeyValue
operator|.
name|Type
operator|.
name|DeleteFamily
argument_list|)
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
comment|/**    * Delete all versions of the specified column.    * @param family family name    * @param qualifier column qualifier    * @return this for invocation chaining    */
specifier|public
name|Delete
name|deleteColumns
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
name|this
operator|.
name|deleteColumns
argument_list|(
name|family
argument_list|,
name|qualifier
argument_list|,
name|HConstants
operator|.
name|LATEST_TIMESTAMP
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**    * Delete all versions of the specified column with a timestamp less than    * or equal to the specified timestamp.    * @param family family name    * @param qualifier column qualifier    * @param timestamp maximum version timestamp    * @return this for invocation chaining    */
specifier|public
name|Delete
name|deleteColumns
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
name|timestamp
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
argument_list|()
expr_stmt|;
block|}
name|list
operator|.
name|add
argument_list|(
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
name|timestamp
argument_list|,
name|KeyValue
operator|.
name|Type
operator|.
name|DeleteColumn
argument_list|)
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
comment|/**    * Delete the latest version of the specified column.    * This is an expensive call in that on the server-side, it first does a    * get to find the latest versions timestamp.  Then it adds a delete using    * the fetched cells timestamp.    * @param family family name    * @param qualifier column qualifier    * @return this for invocation chaining    */
specifier|public
name|Delete
name|deleteColumn
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
name|this
operator|.
name|deleteColumn
argument_list|(
name|family
argument_list|,
name|qualifier
argument_list|,
name|HConstants
operator|.
name|LATEST_TIMESTAMP
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**    * Delete the specified version of the specified column.    * @param family family name    * @param qualifier column qualifier    * @param timestamp version timestamp    * @return this for invocation chaining    */
specifier|public
name|Delete
name|deleteColumn
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
name|timestamp
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
argument_list|()
expr_stmt|;
block|}
name|list
operator|.
name|add
argument_list|(
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
name|timestamp
argument_list|,
name|KeyValue
operator|.
name|Type
operator|.
name|Delete
argument_list|)
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
comment|/**    * Method for retrieving the delete's familyMap    * @return familyMap    */
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
comment|/**    *  Method for retrieving the delete's row    * @return row    */
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
comment|/**    * Method for retrieving the delete's RowLock    * @return RowLock    */
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
comment|/**    * Method for retrieving the delete's lock ID.    *    * @return The lock ID.    */
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
comment|/**    * Method for retrieving the delete's timestamp    * @return timestamp    */
specifier|public
name|long
name|getTimeStamp
parameter_list|()
block|{
return|return
name|this
operator|.
name|ts
return|;
block|}
comment|/**    * Set the timestamp of the delete.    *     * @param timestamp    */
specifier|public
name|void
name|setTimestamp
parameter_list|(
name|long
name|timestamp
parameter_list|)
block|{
name|this
operator|.
name|ts
operator|=
name|timestamp
expr_stmt|;
block|}
comment|/**    * @return string    */
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
literal|", ts="
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|this
operator|.
name|ts
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
name|DELETE_VERSION
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
name|familyMap
operator|.
name|clear
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
name|numColumns
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
name|list
init|=
operator|new
name|ArrayList
argument_list|<
name|KeyValue
argument_list|>
argument_list|(
name|numColumns
argument_list|)
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
name|numColumns
condition|;
name|j
operator|++
control|)
block|{
name|KeyValue
name|kv
init|=
operator|new
name|KeyValue
argument_list|()
decl_stmt|;
name|kv
operator|.
name|readFields
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|list
operator|.
name|add
argument_list|(
name|kv
argument_list|)
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
name|list
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
name|DELETE_VERSION
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
name|list
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
name|list
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
name|list
control|)
block|{
name|kv
operator|.
name|write
argument_list|(
name|out
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|/**    * Delete all versions of the specified column, given in    *<code>family:qualifier</code> notation, and with a timestamp less than    * or equal to the specified timestamp.    * @param column colon-delimited family and qualifier    * @param timestamp maximum version timestamp    * @deprecated use {@link #deleteColumn(byte[], byte[], long)} instead    * @return this for invocation chaining    */
specifier|public
name|Delete
name|deleteColumns
parameter_list|(
name|byte
index|[]
name|column
parameter_list|,
name|long
name|timestamp
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
name|this
operator|.
name|deleteColumns
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
name|timestamp
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**    * Delete the latest version of the specified column, given in    *<code>family:qualifier</code> notation.    * @param column colon-delimited family and qualifier    * @deprecated use {@link #deleteColumn(byte[], byte[])} instead    * @return this for invocation chaining    */
specifier|public
name|Delete
name|deleteColumn
parameter_list|(
name|byte
index|[]
name|column
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
name|this
operator|.
name|deleteColumn
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
name|HConstants
operator|.
name|LATEST_TIMESTAMP
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
block|}
end_class

end_unit

