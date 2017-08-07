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
comment|/**  * Used to perform Delete operations on a single row.  *<p>  * To delete an entire row, instantiate a Delete object with the row  * to delete.  To further define the scope of what to delete, perform  * additional methods as outlined below.  *<p>  * To delete specific families, execute {@link #addFamily(byte[]) deleteFamily}  * for each family to delete.  *<p>  * To delete multiple versions of specific columns, execute  * {@link #addColumns(byte[], byte[]) deleteColumns}  * for each column to delete.  *<p>  * To delete specific versions of specific columns, execute  * {@link #addColumn(byte[], byte[], long) deleteColumn}  * for each column version to delete.  *<p>  * Specifying timestamps, deleteFamily and deleteColumns will delete all  * versions with a timestamp less than or equal to that passed.  If no  * timestamp is specified, an entry is added with a timestamp of 'now'  * where 'now' is the servers's System.currentTimeMillis().  * Specifying a timestamp to the deleteColumn method will  * delete versions only with a timestamp equal to that specified.  * If no timestamp is passed to deleteColumn, internally, it figures the  * most recent cell's timestamp and adds a delete at that timestamp; i.e.  * it deletes the most recently added cell.  *<p>The timestamp passed to the constructor is used ONLY for delete of  * rows.  For anything less -- a deleteColumn, deleteColumns or  * deleteFamily -- then you need to use the method overrides that take a  * timestamp.  The constructor timestamp is not referenced.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Public
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
comment|/**    * Create a Delete operation for the specified row.    *<p>    * If no further operations are done, this will delete everything    * associated with the specified row (all versions of all columns in all    * families), with timestamp from current point in time to the past.    * Cells defining timestamp for a future point in time    * (timestamp> current time) will not be deleted.    * @param row row key    */
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
comment|/**    * Create a Delete operation for the specified row and timestamp.<p>    *    * If no further operations are done, this will delete all columns in all    * families of the specified row with a timestamp less than or equal to the    * specified timestamp.<p>    *    * This timestamp is ONLY used for a delete row operation.  If specifying    * families or columns, you must specify each timestamp individually.    * @param row We make a local copy of this passed in row.    * @param rowOffset    * @param rowLength    */
specifier|public
name|Delete
parameter_list|(
specifier|final
name|byte
index|[]
name|row
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
name|row
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
comment|/**    * Create a Delete operation for the specified row and timestamp.<p>    *    * If no further operations are done, this will delete all columns in all    * families of the specified row with a timestamp less than or equal to the    * specified timestamp.<p>    *    * This timestamp is ONLY used for a delete row operation.  If specifying    * families or columns, you must specify each timestamp individually.    * @param row We make a local copy of this passed in row.    * @param rowOffset    * @param rowLength    * @param timestamp maximum version timestamp (only for delete row)    */
specifier|public
name|Delete
parameter_list|(
specifier|final
name|byte
index|[]
name|row
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
name|timestamp
parameter_list|)
block|{
name|checkRow
argument_list|(
name|row
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
name|row
argument_list|,
name|rowOffset
argument_list|,
name|rowLength
argument_list|)
expr_stmt|;
name|setTimestamp
argument_list|(
name|timestamp
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
name|d
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
name|super
operator|.
name|setPriority
argument_list|(
name|d
operator|.
name|getPriority
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**    * Advanced use only. Add an existing delete marker to this Delete object.    * @param kv An existing KeyValue of type "delete".    * @return this for invocation chaining    * @throws IOException    * @deprecated As of release 2.0.0, this will be removed in HBase 3.0.0. Use {@link #add(Cell)}    *             instead    */
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
annotation|@
name|Deprecated
specifier|public
name|Delete
name|addDeleteMarker
parameter_list|(
name|Cell
name|kv
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|this
operator|.
name|add
argument_list|(
name|kv
argument_list|)
return|;
block|}
comment|/**    * Add an existing delete marker to this Delete object.    * @param kv An existing KeyValue of type "delete".    * @return this for invocation chaining    * @throws IOException    */
specifier|public
name|Delete
name|add
parameter_list|(
name|Cell
name|kv
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
operator|!
name|CellUtil
operator|.
name|isDelete
argument_list|(
name|kv
argument_list|)
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
operator|!
name|CellUtil
operator|.
name|matchingRow
argument_list|(
name|kv
argument_list|,
name|this
operator|.
name|row
argument_list|)
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
argument_list|<>
argument_list|(
literal|1
argument_list|)
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
name|addFamily
parameter_list|(
specifier|final
name|byte
index|[]
name|family
parameter_list|)
block|{
name|this
operator|.
name|addFamily
argument_list|(
name|family
argument_list|,
name|this
operator|.
name|ts
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**    * Delete all columns of the specified family with a timestamp less than    * or equal to the specified timestamp.    *<p>    * Overrides previous calls to deleteColumn and deleteColumns for the    * specified family.    * @param family family name    * @param timestamp maximum version timestamp    * @return this for invocation chaining    */
specifier|public
name|Delete
name|addFamily
parameter_list|(
specifier|final
name|byte
index|[]
name|family
parameter_list|,
specifier|final
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
argument_list|<>
argument_list|(
literal|1
argument_list|)
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
name|addFamilyVersion
parameter_list|(
specifier|final
name|byte
index|[]
name|family
parameter_list|,
specifier|final
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
argument_list|<>
argument_list|(
literal|1
argument_list|)
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
name|addColumns
parameter_list|(
specifier|final
name|byte
index|[]
name|family
parameter_list|,
specifier|final
name|byte
index|[]
name|qualifier
parameter_list|)
block|{
name|addColumns
argument_list|(
name|family
argument_list|,
name|qualifier
argument_list|,
name|this
operator|.
name|ts
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**    * Delete all versions of the specified column with a timestamp less than    * or equal to the specified timestamp.    * @param family family name    * @param qualifier column qualifier    * @param timestamp maximum version timestamp    * @return this for invocation chaining    */
specifier|public
name|Delete
name|addColumns
parameter_list|(
specifier|final
name|byte
index|[]
name|family
parameter_list|,
specifier|final
name|byte
index|[]
name|qualifier
parameter_list|,
specifier|final
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
argument_list|<>
argument_list|(
literal|1
argument_list|)
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
name|addColumn
parameter_list|(
specifier|final
name|byte
index|[]
name|family
parameter_list|,
specifier|final
name|byte
index|[]
name|qualifier
parameter_list|)
block|{
name|this
operator|.
name|addColumn
argument_list|(
name|family
argument_list|,
name|qualifier
argument_list|,
name|this
operator|.
name|ts
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**    * Delete the specified version of the specified column.    * @param family family name    * @param qualifier column qualifier    * @param timestamp version timestamp    * @return this for invocation chaining    */
specifier|public
name|Delete
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
argument_list|<>
argument_list|(
literal|1
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
name|Delete
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
return|return
name|this
return|;
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
annotation|@
name|Override
specifier|public
name|Delete
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
name|Delete
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
name|Delete
name|setId
parameter_list|(
name|String
name|id
parameter_list|)
block|{
return|return
operator|(
name|Delete
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
name|Delete
name|setDurability
parameter_list|(
name|Durability
name|d
parameter_list|)
block|{
return|return
operator|(
name|Delete
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
name|Delete
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
name|Delete
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
name|Delete
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
name|Delete
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
name|Delete
name|setCellVisibility
parameter_list|(
name|CellVisibility
name|expression
parameter_list|)
block|{
return|return
operator|(
name|Delete
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
name|Delete
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
name|Delete
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
name|Delete
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
name|Delete
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
name|Delete
name|setTTL
parameter_list|(
name|long
name|ttl
parameter_list|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"Setting TTLs on Deletes is not supported"
argument_list|)
throw|;
block|}
annotation|@
name|Override
specifier|public
name|Delete
name|setPriority
parameter_list|(
name|int
name|priority
parameter_list|)
block|{
return|return
operator|(
name|Delete
operator|)
name|super
operator|.
name|setPriority
argument_list|(
name|priority
argument_list|)
return|;
block|}
block|}
end_class

end_unit

