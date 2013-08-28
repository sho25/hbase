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

begin_comment
comment|/**  * Used to perform Delete operations on a single row.  *<p>  * To delete an entire row, instantiate a Delete object with the row  * to delete.  To further define the scope of what to delete, perform  * additional methods as outlined below.  *<p>  * To delete specific families, execute {@link #deleteFamily(byte[]) deleteFamily}  * for each family to delete.  *<p>  * To delete multiple versions of specific columns, execute  * {@link #deleteColumns(byte[], byte[]) deleteColumns}  * for each column to delete.  *<p>  * To delete specific versions of specific columns, execute  * {@link #deleteColumn(byte[], byte[], long) deleteColumn}  * for each column version to delete.  *<p>  * Specifying timestamps, deleteFamily and deleteColumns will delete all  * versions with a timestamp less than or equal to that passed.  If no  * timestamp is specified, an entry is added with a timestamp of 'now'  * where 'now' is the servers's System.currentTimeMillis().  * Specifying a timestamp to the deleteColumn method will  * delete versions only with a timestamp equal to that specified.  * If no timestamp is passed to deleteColumn, internally, it figures the  * most recent cell's timestamp and adds a delete at that timestamp; i.e.  * it deletes the most recently added cell.  *<p>The timestamp passed to the constructor is used ONLY for delete of  * rows.  For anything less -- a deleteColumn, deleteColumns or  * deleteFamily -- then you need to use the method overrides that take a  * timestamp.  The constructor timestamp is not referenced.  */
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
name|Delete
extends|extends
name|Mutation
implements|implements
name|Comparable
argument_list|<
name|Row
argument_list|>
block|{
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
argument_list|)
expr_stmt|;
block|}
comment|/**    * Create a Delete operation for the specified row and timestamp.<p>    *    * If no further operations are done, this will delete all columns in all    * families of the specified row with a timestamp less than or equal to the    * specified timestamp.<p>    *    * This timestamp is ONLY used for a delete row operation.  If specifying    * families or columns, you must specify each timestamp individually.    * @param row row key    * @param timestamp maximum version timestamp (only for delete row)    */
specifier|public
name|Delete
parameter_list|(
name|byte
index|[]
name|row
parameter_list|,
name|long
name|timestamp
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
name|timestamp
argument_list|)
expr_stmt|;
block|}
comment|/**    * Create a Delete operation for the specified row and timestamp.<p>    *    * If no further operations are done, this will delete all columns in all    * families of the specified row with a timestamp less than or equal to the    * specified timestamp.<p>    *    * This timestamp is ONLY used for a delete row operation.  If specifying    * families or columns, you must specify each timestamp individually.    * @param rowArray We make a local copy of this passed in row.    * @param rowOffset    * @param rowLength    */
specifier|public
name|Delete
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
comment|/**    * Create a Delete operation for the specified row and timestamp.<p>    *    * If no further operations are done, this will delete all columns in all    * families of the specified row with a timestamp less than or equal to the    * specified timestamp.<p>    *    * This timestamp is ONLY used for a delete row operation.  If specifying    * families or columns, you must specify each timestamp individually.    * @param rowArray We make a local copy of this passed in row.    * @param rowOffset    * @param rowLength    * @param ts maximum version timestamp (only for delete row)    */
specifier|public
name|Delete
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
name|setTimestamp
argument_list|(
name|ts
argument_list|)
expr_stmt|;
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
name|familyMap
operator|.
name|putAll
argument_list|(
name|d
operator|.
name|getFamilyCellMap
argument_list|()
argument_list|)
expr_stmt|;
name|this
operator|.
name|durability
operator|=
name|d
operator|.
name|durability
expr_stmt|;
block|}
comment|/**    * Advanced use only.    * Add an existing delete marker to this Delete object.    * @param kv An existing KeyValue of type "delete".    * @return this for invocation chaining    * @throws IOException    */
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
specifier|public
name|Delete
name|addDeleteMarker
parameter_list|(
name|KeyValue
name|kv
parameter_list|)
throws|throws
name|IOException
block|{
comment|// TODO: Deprecate and rename 'add' so it matches how we add KVs to Puts.
if|if
condition|(
operator|!
name|kv
operator|.
name|isDelete
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"The recently added KeyValue is not of type "
operator|+
literal|"delete. Rowkey: "
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
if|if
condition|(
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
name|Cell
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
name|Cell
argument_list|>
argument_list|()
expr_stmt|;
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
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
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
if|if
condition|(
name|timestamp
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
name|timestamp
argument_list|)
throw|;
block|}
name|List
argument_list|<
name|Cell
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
name|Cell
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
name|KeyValue
name|kv
init|=
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
comment|/**    * Delete all columns of the specified family with a timestamp equal to    * the specified timestamp.    * @param family family name    * @param timestamp version timestamp    * @return this for invocation chaining    */
specifier|public
name|Delete
name|deleteFamilyVersion
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
name|Cell
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
name|Cell
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
name|DeleteFamilyVersion
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
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
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
if|if
condition|(
name|timestamp
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
name|timestamp
argument_list|)
throw|;
block|}
name|List
argument_list|<
name|Cell
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
name|Cell
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
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
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
if|if
condition|(
name|timestamp
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
name|timestamp
argument_list|)
throw|;
block|}
name|List
argument_list|<
name|Cell
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
name|Cell
argument_list|>
argument_list|()
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
name|timestamp
argument_list|,
name|KeyValue
operator|.
name|Type
operator|.
name|Delete
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
comment|/**    * Set the timestamp of the delete.    *    * @param timestamp    */
specifier|public
name|void
name|setTimestamp
parameter_list|(
name|long
name|timestamp
parameter_list|)
block|{
if|if
condition|(
name|timestamp
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
name|timestamp
argument_list|)
throw|;
block|}
name|this
operator|.
name|ts
operator|=
name|timestamp
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|toMap
parameter_list|(
name|int
name|maxCols
parameter_list|)
block|{
comment|// we start with the fingerprint map and build on top of it.
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|map
init|=
name|super
operator|.
name|toMap
argument_list|(
name|maxCols
argument_list|)
decl_stmt|;
comment|// why is put not doing this?
name|map
operator|.
name|put
argument_list|(
literal|"ts"
argument_list|,
name|this
operator|.
name|ts
argument_list|)
expr_stmt|;
return|return
name|map
return|;
block|}
block|}
end_class

end_unit

