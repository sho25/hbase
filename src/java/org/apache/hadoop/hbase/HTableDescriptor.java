begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2007 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|Collections
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Iterator
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
name|SortedMap
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
name|regex
operator|.
name|Matcher
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|regex
operator|.
name|Pattern
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
name|fs
operator|.
name|Path
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
name|Text
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
name|WritableComparable
import|;
end_import

begin_comment
comment|/**  * HTableDescriptor contains the name of an HTable, and its  * column families.  */
end_comment

begin_class
specifier|public
class|class
name|HTableDescriptor
implements|implements
name|WritableComparable
block|{
comment|/** table descriptor for root table */
specifier|public
specifier|static
specifier|final
name|HTableDescriptor
name|rootTableDesc
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|HConstants
operator|.
name|ROOT_TABLE_NAME
argument_list|,
operator|new
name|HColumnDescriptor
argument_list|(
name|HConstants
operator|.
name|COLUMN_FAMILY
argument_list|,
literal|1
argument_list|,
name|HColumnDescriptor
operator|.
name|CompressionType
operator|.
name|NONE
argument_list|,
literal|false
argument_list|,
name|Integer
operator|.
name|MAX_VALUE
argument_list|,
literal|null
argument_list|)
argument_list|)
decl_stmt|;
comment|/** table descriptor for meta table */
specifier|public
specifier|static
specifier|final
name|HTableDescriptor
name|metaTableDesc
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|HConstants
operator|.
name|META_TABLE_NAME
argument_list|,
operator|new
name|HColumnDescriptor
argument_list|(
name|HConstants
operator|.
name|COLUMN_FAMILY
argument_list|,
literal|1
argument_list|,
name|HColumnDescriptor
operator|.
name|CompressionType
operator|.
name|NONE
argument_list|,
literal|false
argument_list|,
name|Integer
operator|.
name|MAX_VALUE
argument_list|,
literal|null
argument_list|)
argument_list|)
decl_stmt|;
specifier|private
name|boolean
name|rootregion
decl_stmt|;
specifier|private
name|boolean
name|metaregion
decl_stmt|;
specifier|private
name|Text
name|name
decl_stmt|;
comment|// TODO: Does this need to be a treemap?  Can it be a HashMap?
specifier|private
specifier|final
name|TreeMap
argument_list|<
name|Text
argument_list|,
name|HColumnDescriptor
argument_list|>
name|families
decl_stmt|;
comment|/*    * Legal table names can only contain 'word characters':    * i.e.<code>[a-zA-Z_0-9-.]</code>.    * Lets be restrictive until a reason to be otherwise. One reason to limit    * characters in table name is to ensure table regions as entries in META    * regions can be found (See HADOOP-1581 'HBASE: Un-openable tablename bug').    */
specifier|private
specifier|static
specifier|final
name|Pattern
name|LEGAL_TABLE_NAME
init|=
name|Pattern
operator|.
name|compile
argument_list|(
literal|"^[\\w-.]+$"
argument_list|)
decl_stmt|;
comment|/** Used to construct the table descriptors for root and meta tables */
specifier|private
name|HTableDescriptor
parameter_list|(
name|Text
name|name
parameter_list|,
name|HColumnDescriptor
name|family
parameter_list|)
block|{
name|rootregion
operator|=
name|name
operator|.
name|equals
argument_list|(
name|HConstants
operator|.
name|ROOT_TABLE_NAME
argument_list|)
expr_stmt|;
name|this
operator|.
name|metaregion
operator|=
literal|true
expr_stmt|;
name|this
operator|.
name|name
operator|=
operator|new
name|Text
argument_list|(
name|name
argument_list|)
expr_stmt|;
name|this
operator|.
name|families
operator|=
operator|new
name|TreeMap
argument_list|<
name|Text
argument_list|,
name|HColumnDescriptor
argument_list|>
argument_list|()
expr_stmt|;
name|families
operator|.
name|put
argument_list|(
name|family
operator|.
name|getName
argument_list|()
argument_list|,
name|family
argument_list|)
expr_stmt|;
block|}
comment|/**    * Constructs an empty object.    * For deserializing an HTableDescriptor instance only.    * @see #HTableDescriptor(String)    */
specifier|public
name|HTableDescriptor
parameter_list|()
block|{
name|this
operator|.
name|name
operator|=
operator|new
name|Text
argument_list|()
expr_stmt|;
name|this
operator|.
name|families
operator|=
operator|new
name|TreeMap
argument_list|<
name|Text
argument_list|,
name|HColumnDescriptor
argument_list|>
argument_list|()
expr_stmt|;
block|}
comment|/**    * Constructor.    * @param name Table name.    * @throws IllegalArgumentException if passed a table name    * that is made of other than 'word' characters: i.e.    *<code>[a-zA-Z_0-9]    */
specifier|public
name|HTableDescriptor
parameter_list|(
name|String
name|name
parameter_list|)
block|{
name|this
argument_list|()
expr_stmt|;
name|Matcher
name|m
init|=
name|LEGAL_TABLE_NAME
operator|.
name|matcher
argument_list|(
name|name
argument_list|)
decl_stmt|;
if|if
condition|(
name|m
operator|==
literal|null
operator|||
operator|!
name|m
operator|.
name|matches
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Table names can only contain 'word characters': i.e. [a-zA-Z_0-9"
argument_list|)
throw|;
block|}
name|this
operator|.
name|name
operator|.
name|set
argument_list|(
name|name
argument_list|)
expr_stmt|;
name|this
operator|.
name|rootregion
operator|=
literal|false
expr_stmt|;
name|this
operator|.
name|metaregion
operator|=
literal|false
expr_stmt|;
block|}
comment|/** @return true if this is the root region */
specifier|public
name|boolean
name|isRootRegion
parameter_list|()
block|{
return|return
name|rootregion
return|;
block|}
comment|/** @return true if table is the meta table */
specifier|public
name|boolean
name|isMetaTable
parameter_list|()
block|{
return|return
name|metaregion
operator|&&
operator|!
name|rootregion
return|;
block|}
comment|/** @return true if this is a meta region (part of the root or meta tables) */
specifier|public
name|boolean
name|isMetaRegion
parameter_list|()
block|{
return|return
name|metaregion
return|;
block|}
comment|/** @return name of table */
specifier|public
name|Text
name|getName
parameter_list|()
block|{
return|return
name|name
return|;
block|}
comment|/**    * Adds a column family.    * @param family HColumnDescriptor of familyto add.    */
specifier|public
name|void
name|addFamily
parameter_list|(
name|HColumnDescriptor
name|family
parameter_list|)
block|{
name|families
operator|.
name|put
argument_list|(
name|family
operator|.
name|getName
argument_list|()
argument_list|,
name|family
argument_list|)
expr_stmt|;
block|}
comment|/**    * Checks to see if this table contains the given column family    *     * @param family - family name    * @return true if the table contains the specified family name    */
specifier|public
name|boolean
name|hasFamily
parameter_list|(
name|Text
name|family
parameter_list|)
block|{
return|return
name|families
operator|.
name|containsKey
argument_list|(
name|family
argument_list|)
return|;
block|}
comment|/**     * All the column families in this table.    *     *  TODO: What is this used for? Seems Dangerous to let people play with our    *  private members.    *      *  @return map of family members    */
specifier|public
name|TreeMap
argument_list|<
name|Text
argument_list|,
name|HColumnDescriptor
argument_list|>
name|families
parameter_list|()
block|{
return|return
name|families
return|;
block|}
comment|/** {@inheritDoc} */
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
literal|"name: "
operator|+
name|this
operator|.
name|name
operator|.
name|toString
argument_list|()
operator|+
literal|", families: "
operator|+
name|this
operator|.
name|families
return|;
block|}
comment|/** {@inheritDoc} */
annotation|@
name|Override
specifier|public
name|boolean
name|equals
parameter_list|(
name|Object
name|obj
parameter_list|)
block|{
return|return
name|compareTo
argument_list|(
name|obj
argument_list|)
operator|==
literal|0
return|;
block|}
comment|/** {@inheritDoc} */
annotation|@
name|Override
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
comment|// TODO: Cache.
name|int
name|result
init|=
name|this
operator|.
name|name
operator|.
name|hashCode
argument_list|()
decl_stmt|;
if|if
condition|(
name|this
operator|.
name|families
operator|!=
literal|null
operator|&&
name|this
operator|.
name|families
operator|.
name|size
argument_list|()
operator|>
literal|0
condition|)
block|{
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|Text
argument_list|,
name|HColumnDescriptor
argument_list|>
name|e
range|:
name|this
operator|.
name|families
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|result
operator|^=
name|e
operator|.
name|hashCode
argument_list|()
expr_stmt|;
block|}
block|}
return|return
name|result
return|;
block|}
comment|// Writable
comment|/** {@inheritDoc} */
specifier|public
name|void
name|write
parameter_list|(
name|DataOutput
name|out
parameter_list|)
throws|throws
name|IOException
block|{
name|out
operator|.
name|writeBoolean
argument_list|(
name|rootregion
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeBoolean
argument_list|(
name|metaregion
argument_list|)
expr_stmt|;
name|name
operator|.
name|write
argument_list|(
name|out
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeInt
argument_list|(
name|families
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|Iterator
argument_list|<
name|HColumnDescriptor
argument_list|>
name|it
init|=
name|families
operator|.
name|values
argument_list|()
operator|.
name|iterator
argument_list|()
init|;
name|it
operator|.
name|hasNext
argument_list|()
condition|;
control|)
block|{
name|it
operator|.
name|next
argument_list|()
operator|.
name|write
argument_list|(
name|out
argument_list|)
expr_stmt|;
block|}
block|}
comment|/** {@inheritDoc} */
specifier|public
name|void
name|readFields
parameter_list|(
name|DataInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|this
operator|.
name|rootregion
operator|=
name|in
operator|.
name|readBoolean
argument_list|()
expr_stmt|;
name|this
operator|.
name|metaregion
operator|=
name|in
operator|.
name|readBoolean
argument_list|()
expr_stmt|;
name|this
operator|.
name|name
operator|.
name|readFields
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|int
name|numCols
init|=
name|in
operator|.
name|readInt
argument_list|()
decl_stmt|;
name|families
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
name|numCols
condition|;
name|i
operator|++
control|)
block|{
name|HColumnDescriptor
name|c
init|=
operator|new
name|HColumnDescriptor
argument_list|()
decl_stmt|;
name|c
operator|.
name|readFields
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|families
operator|.
name|put
argument_list|(
name|c
operator|.
name|getName
argument_list|()
argument_list|,
name|c
argument_list|)
expr_stmt|;
block|}
block|}
comment|// Comparable
comment|/** {@inheritDoc} */
specifier|public
name|int
name|compareTo
parameter_list|(
name|Object
name|o
parameter_list|)
block|{
name|HTableDescriptor
name|other
init|=
operator|(
name|HTableDescriptor
operator|)
name|o
decl_stmt|;
name|int
name|result
init|=
name|name
operator|.
name|compareTo
argument_list|(
name|other
operator|.
name|name
argument_list|)
decl_stmt|;
if|if
condition|(
name|result
operator|==
literal|0
condition|)
block|{
name|result
operator|=
name|families
operator|.
name|size
argument_list|()
operator|-
name|other
operator|.
name|families
operator|.
name|size
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|result
operator|==
literal|0
operator|&&
name|families
operator|.
name|size
argument_list|()
operator|!=
name|other
operator|.
name|families
operator|.
name|size
argument_list|()
condition|)
block|{
name|result
operator|=
name|Integer
operator|.
name|valueOf
argument_list|(
name|families
operator|.
name|size
argument_list|()
argument_list|)
operator|.
name|compareTo
argument_list|(
name|Integer
operator|.
name|valueOf
argument_list|(
name|other
operator|.
name|families
operator|.
name|size
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|result
operator|==
literal|0
condition|)
block|{
for|for
control|(
name|Iterator
argument_list|<
name|HColumnDescriptor
argument_list|>
name|it
init|=
name|families
operator|.
name|values
argument_list|()
operator|.
name|iterator
argument_list|()
init|,
name|it2
init|=
name|other
operator|.
name|families
operator|.
name|values
argument_list|()
operator|.
name|iterator
argument_list|()
init|;
name|it
operator|.
name|hasNext
argument_list|()
condition|;
control|)
block|{
name|result
operator|=
name|it
operator|.
name|next
argument_list|()
operator|.
name|compareTo
argument_list|(
name|it2
operator|.
name|next
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|result
operator|!=
literal|0
condition|)
block|{
break|break;
block|}
block|}
block|}
return|return
name|result
return|;
block|}
comment|/**    * @return Immutable sorted map of families.    */
specifier|public
name|SortedMap
argument_list|<
name|Text
argument_list|,
name|HColumnDescriptor
argument_list|>
name|getFamilies
parameter_list|()
block|{
return|return
name|Collections
operator|.
name|unmodifiableSortedMap
argument_list|(
name|this
operator|.
name|families
argument_list|)
return|;
block|}
comment|/**    * @param rootdir qualified path of HBase root directory    * @param tableName name of table    * @return path for table    */
specifier|public
specifier|static
name|Path
name|getTableDir
parameter_list|(
name|Path
name|rootdir
parameter_list|,
name|Text
name|tableName
parameter_list|)
block|{
return|return
operator|new
name|Path
argument_list|(
name|rootdir
argument_list|,
name|tableName
operator|.
name|toString
argument_list|()
argument_list|)
return|;
block|}
block|}
end_class

end_unit

