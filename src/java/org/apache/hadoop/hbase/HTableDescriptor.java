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
name|Collection
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
name|HashMap
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
name|hbase
operator|.
name|io
operator|.
name|ImmutableBytesWritable
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
comment|/** Table descriptor for<core>-ROOT-</code> catalog table */
specifier|public
specifier|static
specifier|final
name|HTableDescriptor
name|ROOT_TABLEDESC
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
index|[]
block|{
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
literal|false
argument_list|,
name|Integer
operator|.
name|MAX_VALUE
argument_list|,
name|HConstants
operator|.
name|FOREVER
argument_list|,
literal|false
argument_list|)
block|}
argument_list|)
decl_stmt|;
comment|/** Table descriptor for<code>.META.</code> catalog table */
specifier|public
specifier|static
specifier|final
name|HTableDescriptor
name|META_TABLEDESC
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
index|[]
block|{
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
literal|false
argument_list|,
name|Integer
operator|.
name|MAX_VALUE
argument_list|,
name|HConstants
operator|.
name|FOREVER
argument_list|,
literal|false
argument_list|)
block|,
operator|new
name|HColumnDescriptor
argument_list|(
name|HConstants
operator|.
name|COLUMN_FAMILY_HISTORIAN
argument_list|,
name|HConstants
operator|.
name|ALL_VERSIONS
argument_list|,
name|HColumnDescriptor
operator|.
name|CompressionType
operator|.
name|NONE
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|,
name|Integer
operator|.
name|MAX_VALUE
argument_list|,
name|HConstants
operator|.
name|FOREVER
argument_list|,
literal|false
argument_list|)
block|}
argument_list|)
decl_stmt|;
comment|// Changes prior to version 3 were not recorded here.
comment|// Version 3 adds metadata as a map where keys and values are byte[].
specifier|public
specifier|static
specifier|final
name|byte
name|TABLE_DESCRIPTOR_VERSION
init|=
literal|3
decl_stmt|;
specifier|private
name|byte
index|[]
name|name
init|=
name|HConstants
operator|.
name|EMPTY_BYTE_ARRAY
decl_stmt|;
specifier|private
name|String
name|nameAsString
init|=
literal|""
decl_stmt|;
comment|// Table metadata
specifier|protected
name|Map
argument_list|<
name|ImmutableBytesWritable
argument_list|,
name|ImmutableBytesWritable
argument_list|>
name|values
init|=
operator|new
name|HashMap
argument_list|<
name|ImmutableBytesWritable
argument_list|,
name|ImmutableBytesWritable
argument_list|>
argument_list|()
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|FAMILIES
init|=
literal|"FAMILIES"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|MAX_FILESIZE
init|=
literal|"MAX_FILESIZE"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|IN_MEMORY
init|=
literal|"IN_MEMORY"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|READONLY
init|=
literal|"READONLY"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|MEMCACHE_FLUSHSIZE
init|=
literal|"MEMCACHE_FLUSHSIZE"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|IS_ROOT
init|=
literal|"IS_ROOT"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|IS_META
init|=
literal|"IS_META"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|boolean
name|DEFAULT_IN_MEMORY
init|=
literal|false
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|boolean
name|DEFAULT_READONLY
init|=
literal|false
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|int
name|DEFAULT_MEMCACHE_FLUSH_SIZE
init|=
literal|1024
operator|*
literal|1024
operator|*
literal|64
decl_stmt|;
comment|// Key is hash of the family name.
specifier|private
specifier|final
name|Map
argument_list|<
name|Integer
argument_list|,
name|HColumnDescriptor
argument_list|>
name|families
init|=
operator|new
name|HashMap
argument_list|<
name|Integer
argument_list|,
name|HColumnDescriptor
argument_list|>
argument_list|()
decl_stmt|;
comment|/**    * Private constructor used internally creating table descriptors for     * catalog tables: e.g. .META. and -ROOT-.    */
specifier|private
name|HTableDescriptor
parameter_list|(
specifier|final
name|byte
index|[]
name|name
parameter_list|,
name|HColumnDescriptor
index|[]
name|families
parameter_list|)
block|{
name|this
operator|.
name|name
operator|=
name|name
operator|.
name|clone
argument_list|()
expr_stmt|;
name|setMetaFlags
argument_list|(
name|name
argument_list|)
expr_stmt|;
for|for
control|(
name|HColumnDescriptor
name|descriptor
range|:
name|families
control|)
block|{
name|this
operator|.
name|families
operator|.
name|put
argument_list|(
name|Bytes
operator|.
name|mapKey
argument_list|(
name|descriptor
operator|.
name|getName
argument_list|()
argument_list|)
argument_list|,
name|descriptor
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Constructs an empty object.    * For deserializing an HTableDescriptor instance only.    * @see #HTableDescriptor(byte[])    */
specifier|public
name|HTableDescriptor
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
block|}
comment|/**    * Constructor.    * @param name Table name.    * @throws IllegalArgumentException if passed a table name    * that is made of other than 'word' characters, underscore or period: i.e.    *<code>[a-zA-Z_0-9.].    * @see<a href="HADOOP-1581">HADOOP-1581 HBASE: Un-openable tablename bug</a>    */
specifier|public
name|HTableDescriptor
parameter_list|(
specifier|final
name|String
name|name
parameter_list|)
block|{
name|this
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|name
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * Constructor.    * @param name Table name.    * @throws IllegalArgumentException if passed a table name    * that is made of other than 'word' characters, underscore or period: i.e.    *<code>[a-zA-Z_0-9.].    * @see<a href="HADOOP-1581">HADOOP-1581 HBASE: Un-openable tablename bug</a>    */
specifier|public
name|HTableDescriptor
parameter_list|(
specifier|final
name|byte
index|[]
name|name
parameter_list|)
block|{
name|super
argument_list|()
expr_stmt|;
name|this
operator|.
name|name
operator|=
name|this
operator|.
name|isMetaRegion
argument_list|()
condition|?
name|name
else|:
name|isLegalTableName
argument_list|(
name|name
argument_list|)
expr_stmt|;
name|this
operator|.
name|nameAsString
operator|=
name|Bytes
operator|.
name|toString
argument_list|(
name|this
operator|.
name|name
argument_list|)
expr_stmt|;
name|setMetaFlags
argument_list|(
name|this
operator|.
name|name
argument_list|)
expr_stmt|;
block|}
comment|/**    * Constructor.    *<p>    * Makes a deep copy of the supplied descriptor.     * Can make a modifiable descriptor from an UnmodifyableHTableDescriptor.    * @param desc The descriptor.    */
specifier|public
name|HTableDescriptor
parameter_list|(
specifier|final
name|HTableDescriptor
name|desc
parameter_list|)
block|{
name|super
argument_list|()
expr_stmt|;
name|this
operator|.
name|name
operator|=
name|desc
operator|.
name|name
operator|.
name|clone
argument_list|()
expr_stmt|;
name|this
operator|.
name|nameAsString
operator|=
name|Bytes
operator|.
name|toString
argument_list|(
name|this
operator|.
name|name
argument_list|)
expr_stmt|;
name|setMetaFlags
argument_list|(
name|this
operator|.
name|name
argument_list|)
expr_stmt|;
for|for
control|(
name|HColumnDescriptor
name|c
range|:
name|desc
operator|.
name|families
operator|.
name|values
argument_list|()
control|)
block|{
name|this
operator|.
name|families
operator|.
name|put
argument_list|(
name|Bytes
operator|.
name|mapKey
argument_list|(
name|c
operator|.
name|getName
argument_list|()
argument_list|)
argument_list|,
operator|new
name|HColumnDescriptor
argument_list|(
name|c
argument_list|)
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|ImmutableBytesWritable
argument_list|,
name|ImmutableBytesWritable
argument_list|>
name|e
range|:
name|desc
operator|.
name|values
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|this
operator|.
name|values
operator|.
name|put
argument_list|(
name|e
operator|.
name|getKey
argument_list|()
argument_list|,
name|e
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
comment|/*    * Set meta flags on this table.    * Called by constructors.    * @param name    */
specifier|private
name|void
name|setMetaFlags
parameter_list|(
specifier|final
name|byte
index|[]
name|name
parameter_list|)
block|{
name|setRootRegion
argument_list|(
name|Bytes
operator|.
name|equals
argument_list|(
name|name
argument_list|,
name|HConstants
operator|.
name|ROOT_TABLE_NAME
argument_list|)
argument_list|)
expr_stmt|;
name|setMetaRegion
argument_list|(
name|isRootRegion
argument_list|()
operator|||
name|Bytes
operator|.
name|equals
argument_list|(
name|name
argument_list|,
name|HConstants
operator|.
name|META_TABLE_NAME
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/** @return true if this is the root region */
specifier|public
name|boolean
name|isRootRegion
parameter_list|()
block|{
name|String
name|value
init|=
name|getValue
argument_list|(
name|IS_ROOT
argument_list|)
decl_stmt|;
if|if
condition|(
name|value
operator|!=
literal|null
condition|)
return|return
name|Boolean
operator|.
name|valueOf
argument_list|(
name|value
argument_list|)
return|;
return|return
literal|false
return|;
block|}
comment|/** @param isRoot true if this is the root region */
specifier|protected
name|void
name|setRootRegion
parameter_list|(
name|boolean
name|isRoot
parameter_list|)
block|{
name|values
operator|.
name|put
argument_list|(
operator|new
name|ImmutableBytesWritable
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|IS_ROOT
argument_list|)
argument_list|)
argument_list|,
operator|new
name|ImmutableBytesWritable
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|Boolean
operator|.
name|toString
argument_list|(
name|isRoot
argument_list|)
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/** @return true if this is a meta region (part of the root or meta tables) */
specifier|public
name|boolean
name|isMetaRegion
parameter_list|()
block|{
name|String
name|value
init|=
name|getValue
argument_list|(
name|IS_META
argument_list|)
decl_stmt|;
if|if
condition|(
name|value
operator|!=
literal|null
condition|)
return|return
name|Boolean
operator|.
name|valueOf
argument_list|(
name|value
argument_list|)
return|;
return|return
literal|false
return|;
block|}
comment|/**    * @param isMeta true if this is a meta region (part of the root or meta    * tables) */
specifier|protected
name|void
name|setMetaRegion
parameter_list|(
name|boolean
name|isMeta
parameter_list|)
block|{
name|values
operator|.
name|put
argument_list|(
operator|new
name|ImmutableBytesWritable
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|IS_META
argument_list|)
argument_list|)
argument_list|,
operator|new
name|ImmutableBytesWritable
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|Boolean
operator|.
name|toString
argument_list|(
name|isMeta
argument_list|)
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/** @return true if table is the meta table */
specifier|public
name|boolean
name|isMetaTable
parameter_list|()
block|{
return|return
name|isMetaRegion
argument_list|()
operator|&&
operator|!
name|isRootRegion
argument_list|()
return|;
block|}
comment|/**    * Check passed buffer is legal user-space table name.    * @param b Table name.    * @return Returns passed<code>b</code> param    * @throws NullPointerException If passed<code>b</code> is null    * @throws IllegalArgumentException if passed a table name    * that is made of other than 'word' characters or underscores: i.e.    *<code>[a-zA-Z_0-9].    */
specifier|public
specifier|static
name|byte
index|[]
name|isLegalTableName
parameter_list|(
specifier|final
name|byte
index|[]
name|b
parameter_list|)
block|{
if|if
condition|(
name|b
operator|==
literal|null
operator|||
name|b
operator|.
name|length
operator|<=
literal|0
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Name is null or empty"
argument_list|)
throw|;
block|}
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|b
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|Character
operator|.
name|isLetterOrDigit
argument_list|(
name|b
index|[
name|i
index|]
argument_list|)
operator|||
name|b
index|[
name|i
index|]
operator|==
literal|'_'
condition|)
block|{
continue|continue;
block|}
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Illegal character<"
operator|+
name|b
index|[
name|i
index|]
operator|+
literal|">. "
operator|+
literal|"User-space table names can only contain 'word characters':"
operator|+
literal|"i.e. [a-zA-Z_0-9]: "
operator|+
name|Bytes
operator|.
name|toString
argument_list|(
name|b
argument_list|)
argument_list|)
throw|;
block|}
return|return
name|b
return|;
block|}
comment|/**    * @param key The key.    * @return The value.    */
specifier|public
name|byte
index|[]
name|getValue
parameter_list|(
name|byte
index|[]
name|key
parameter_list|)
block|{
name|ImmutableBytesWritable
name|ibw
init|=
name|values
operator|.
name|get
argument_list|(
operator|new
name|ImmutableBytesWritable
argument_list|(
name|key
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|ibw
operator|==
literal|null
condition|)
return|return
literal|null
return|;
return|return
name|ibw
operator|.
name|get
argument_list|()
return|;
block|}
comment|/**    * @param key The key.    * @return The value as a string.    */
specifier|public
name|String
name|getValue
parameter_list|(
name|String
name|key
parameter_list|)
block|{
name|byte
index|[]
name|value
init|=
name|getValue
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|key
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|value
operator|==
literal|null
condition|)
return|return
literal|null
return|;
return|return
name|Bytes
operator|.
name|toString
argument_list|(
name|value
argument_list|)
return|;
block|}
comment|/**    * @param key The key.    * @param value The value.    */
specifier|public
name|void
name|setValue
parameter_list|(
name|byte
index|[]
name|key
parameter_list|,
name|byte
index|[]
name|value
parameter_list|)
block|{
name|values
operator|.
name|put
argument_list|(
operator|new
name|ImmutableBytesWritable
argument_list|(
name|key
argument_list|)
argument_list|,
operator|new
name|ImmutableBytesWritable
argument_list|(
name|value
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * @param key The key.    * @param value The value.    */
specifier|public
name|void
name|setValue
parameter_list|(
name|String
name|key
parameter_list|,
name|String
name|value
parameter_list|)
block|{
name|setValue
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|key
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|value
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * @return true if all columns in the table should be kept in the     * HRegionServer cache only    */
specifier|public
name|boolean
name|isInMemory
parameter_list|()
block|{
name|String
name|value
init|=
name|getValue
argument_list|(
name|IN_MEMORY
argument_list|)
decl_stmt|;
if|if
condition|(
name|value
operator|!=
literal|null
condition|)
return|return
name|Boolean
operator|.
name|valueOf
argument_list|(
name|value
argument_list|)
return|;
return|return
name|DEFAULT_IN_MEMORY
return|;
block|}
comment|/**    * @param inMemory True if all of the columns in the table should be kept in    * the HRegionServer cache only.    */
specifier|public
name|void
name|setInMemory
parameter_list|(
name|boolean
name|inMemory
parameter_list|)
block|{
name|setValue
argument_list|(
name|IN_MEMORY
argument_list|,
name|Boolean
operator|.
name|toString
argument_list|(
name|inMemory
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * @return true if all columns in the table should be read only    */
specifier|public
name|boolean
name|isReadOnly
parameter_list|()
block|{
name|String
name|value
init|=
name|getValue
argument_list|(
name|READONLY
argument_list|)
decl_stmt|;
if|if
condition|(
name|value
operator|!=
literal|null
condition|)
return|return
name|Boolean
operator|.
name|valueOf
argument_list|(
name|value
argument_list|)
return|;
return|return
name|DEFAULT_READONLY
return|;
block|}
comment|/**    * @param readOnly True if all of the columns in the table should be read    * only.    */
specifier|public
name|void
name|setReadOnly
parameter_list|(
name|boolean
name|readOnly
parameter_list|)
block|{
name|setValue
argument_list|(
name|READONLY
argument_list|,
name|Boolean
operator|.
name|toString
argument_list|(
name|readOnly
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/** @return name of table */
specifier|public
name|byte
index|[]
name|getName
parameter_list|()
block|{
return|return
name|name
return|;
block|}
comment|/** @return name of table */
specifier|public
name|String
name|getNameAsString
parameter_list|()
block|{
return|return
name|this
operator|.
name|nameAsString
return|;
block|}
comment|/** @return max hregion size for table */
specifier|public
name|long
name|getMaxFileSize
parameter_list|()
block|{
name|String
name|value
init|=
name|getValue
argument_list|(
name|MAX_FILESIZE
argument_list|)
decl_stmt|;
if|if
condition|(
name|value
operator|!=
literal|null
condition|)
return|return
name|Long
operator|.
name|valueOf
argument_list|(
name|value
argument_list|)
return|;
return|return
name|HConstants
operator|.
name|DEFAULT_MAX_FILE_SIZE
return|;
block|}
comment|/**    * @param maxFileSize The maximum file size that a store file can grow to    * before a split is triggered.    */
specifier|public
name|void
name|setMaxFileSize
parameter_list|(
name|long
name|maxFileSize
parameter_list|)
block|{
name|setValue
argument_list|(
name|MAX_FILESIZE
argument_list|,
name|Long
operator|.
name|toString
argument_list|(
name|maxFileSize
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * @return memory cache flush size for each hregion    */
specifier|public
name|int
name|getMemcacheFlushSize
parameter_list|()
block|{
name|String
name|value
init|=
name|getValue
argument_list|(
name|MEMCACHE_FLUSHSIZE
argument_list|)
decl_stmt|;
if|if
condition|(
name|value
operator|!=
literal|null
condition|)
return|return
name|Integer
operator|.
name|valueOf
argument_list|(
name|value
argument_list|)
return|;
return|return
name|DEFAULT_MEMCACHE_FLUSH_SIZE
return|;
block|}
comment|/**    * @param memcacheFlushSize memory cache flush size for each hregion    */
specifier|public
name|void
name|setMemcacheFlushSize
parameter_list|(
name|int
name|memcacheFlushSize
parameter_list|)
block|{
name|setValue
argument_list|(
name|MEMCACHE_FLUSHSIZE
argument_list|,
name|Integer
operator|.
name|toString
argument_list|(
name|memcacheFlushSize
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * Adds a column family.    * @param family HColumnDescriptor of familyto add.    */
specifier|public
name|void
name|addFamily
parameter_list|(
specifier|final
name|HColumnDescriptor
name|family
parameter_list|)
block|{
if|if
condition|(
name|family
operator|.
name|getName
argument_list|()
operator|==
literal|null
operator|||
name|family
operator|.
name|getName
argument_list|()
operator|.
name|length
operator|<=
literal|0
condition|)
block|{
throw|throw
operator|new
name|NullPointerException
argument_list|(
literal|"Family name cannot be null or empty"
argument_list|)
throw|;
block|}
name|this
operator|.
name|families
operator|.
name|put
argument_list|(
name|Bytes
operator|.
name|mapKey
argument_list|(
name|family
operator|.
name|getName
argument_list|()
argument_list|)
argument_list|,
name|family
argument_list|)
expr_stmt|;
block|}
comment|/**    * Checks to see if this table contains the given column family    * @param c Family name or column name.    * @return true if the table contains the specified family name    */
specifier|public
name|boolean
name|hasFamily
parameter_list|(
specifier|final
name|byte
index|[]
name|c
parameter_list|)
block|{
return|return
name|hasFamily
argument_list|(
name|c
argument_list|,
name|HStoreKey
operator|.
name|getFamilyDelimiterIndex
argument_list|(
name|c
argument_list|)
argument_list|)
return|;
block|}
comment|/**    * Checks to see if this table contains the given column family    * @param c Family name or column name.    * @param index Index to column family delimiter    * @return true if the table contains the specified family name    */
specifier|public
name|boolean
name|hasFamily
parameter_list|(
specifier|final
name|byte
index|[]
name|c
parameter_list|,
specifier|final
name|int
name|index
parameter_list|)
block|{
comment|// If index is -1, then presume we were passed a column family name minus
comment|// the colon delimiter.
return|return
name|families
operator|.
name|containsKey
argument_list|(
name|Bytes
operator|.
name|mapKey
argument_list|(
name|c
argument_list|,
name|index
operator|==
operator|-
literal|1
condition|?
name|c
operator|.
name|length
else|:
name|index
argument_list|)
argument_list|)
return|;
block|}
comment|/**    * @return Name of this table and then a map of all of the column family    * descriptors.    * @see #getNameAsString()    */
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
name|StringBuffer
name|s
init|=
operator|new
name|StringBuffer
argument_list|()
decl_stmt|;
name|s
operator|.
name|append
argument_list|(
literal|'{'
argument_list|)
expr_stmt|;
name|s
operator|.
name|append
argument_list|(
name|HConstants
operator|.
name|NAME
argument_list|)
expr_stmt|;
name|s
operator|.
name|append
argument_list|(
literal|" => '"
argument_list|)
expr_stmt|;
name|s
operator|.
name|append
argument_list|(
name|Bytes
operator|.
name|toString
argument_list|(
name|name
argument_list|)
argument_list|)
expr_stmt|;
name|s
operator|.
name|append
argument_list|(
literal|"'"
argument_list|)
expr_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|ImmutableBytesWritable
argument_list|,
name|ImmutableBytesWritable
argument_list|>
name|e
range|:
name|values
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|s
operator|.
name|append
argument_list|(
literal|", "
argument_list|)
expr_stmt|;
name|s
operator|.
name|append
argument_list|(
name|Bytes
operator|.
name|toString
argument_list|(
name|e
operator|.
name|getKey
argument_list|()
operator|.
name|get
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|s
operator|.
name|append
argument_list|(
literal|" => '"
argument_list|)
expr_stmt|;
name|s
operator|.
name|append
argument_list|(
name|Bytes
operator|.
name|toString
argument_list|(
name|e
operator|.
name|getValue
argument_list|()
operator|.
name|get
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|s
operator|.
name|append
argument_list|(
literal|"'"
argument_list|)
expr_stmt|;
block|}
name|s
operator|.
name|append
argument_list|(
literal|", "
argument_list|)
expr_stmt|;
name|s
operator|.
name|append
argument_list|(
name|FAMILIES
argument_list|)
expr_stmt|;
name|s
operator|.
name|append
argument_list|(
literal|" => "
argument_list|)
expr_stmt|;
name|s
operator|.
name|append
argument_list|(
name|families
operator|.
name|values
argument_list|()
argument_list|)
expr_stmt|;
name|s
operator|.
name|append
argument_list|(
literal|'}'
argument_list|)
expr_stmt|;
return|return
name|s
operator|.
name|toString
argument_list|()
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
name|int
name|result
init|=
name|Bytes
operator|.
name|hashCode
argument_list|(
name|this
operator|.
name|name
argument_list|)
decl_stmt|;
name|result
operator|^=
name|Byte
operator|.
name|valueOf
argument_list|(
name|TABLE_DESCRIPTOR_VERSION
argument_list|)
operator|.
name|hashCode
argument_list|()
expr_stmt|;
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
name|HColumnDescriptor
name|e
range|:
name|this
operator|.
name|families
operator|.
name|values
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
name|result
operator|^=
name|values
operator|.
name|hashCode
argument_list|()
expr_stmt|;
return|return
name|result
return|;
block|}
comment|// Writable
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
name|int
name|version
init|=
name|in
operator|.
name|readInt
argument_list|()
decl_stmt|;
if|if
condition|(
name|version
operator|<
literal|3
condition|)
throw|throw
operator|new
name|IOException
argument_list|(
literal|"versions< 3 are not supported (and never existed!?)"
argument_list|)
throw|;
comment|// version 3+
name|name
operator|=
name|Bytes
operator|.
name|readByteArray
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|nameAsString
operator|=
name|Bytes
operator|.
name|toString
argument_list|(
name|this
operator|.
name|name
argument_list|)
expr_stmt|;
name|setRootRegion
argument_list|(
name|in
operator|.
name|readBoolean
argument_list|()
argument_list|)
expr_stmt|;
name|setMetaRegion
argument_list|(
name|in
operator|.
name|readBoolean
argument_list|()
argument_list|)
expr_stmt|;
name|values
operator|.
name|clear
argument_list|()
expr_stmt|;
name|int
name|numVals
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
name|numVals
condition|;
name|i
operator|++
control|)
block|{
name|ImmutableBytesWritable
name|key
init|=
operator|new
name|ImmutableBytesWritable
argument_list|()
decl_stmt|;
name|ImmutableBytesWritable
name|value
init|=
operator|new
name|ImmutableBytesWritable
argument_list|()
decl_stmt|;
name|key
operator|.
name|readFields
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|value
operator|.
name|readFields
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|values
operator|.
name|put
argument_list|(
name|key
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
name|families
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
name|Bytes
operator|.
name|mapKey
argument_list|(
name|c
operator|.
name|getName
argument_list|()
argument_list|)
argument_list|,
name|c
argument_list|)
expr_stmt|;
block|}
block|}
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
name|writeInt
argument_list|(
name|TABLE_DESCRIPTOR_VERSION
argument_list|)
expr_stmt|;
name|Bytes
operator|.
name|writeByteArray
argument_list|(
name|out
argument_list|,
name|name
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeBoolean
argument_list|(
name|isRootRegion
argument_list|()
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeBoolean
argument_list|(
name|isMetaRegion
argument_list|()
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeInt
argument_list|(
name|values
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
name|ImmutableBytesWritable
argument_list|,
name|ImmutableBytesWritable
argument_list|>
name|e
range|:
name|values
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|e
operator|.
name|getKey
argument_list|()
operator|.
name|write
argument_list|(
name|out
argument_list|)
expr_stmt|;
name|e
operator|.
name|getValue
argument_list|()
operator|.
name|write
argument_list|(
name|out
argument_list|)
expr_stmt|;
block|}
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
name|HColumnDescriptor
name|family
init|=
name|it
operator|.
name|next
argument_list|()
decl_stmt|;
name|family
operator|.
name|write
argument_list|(
name|out
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
name|Bytes
operator|.
name|compareTo
argument_list|(
name|this
operator|.
name|name
argument_list|,
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
if|if
condition|(
name|result
operator|==
literal|0
condition|)
block|{
comment|// punt on comparison for ordering, just calculate difference
name|result
operator|=
name|this
operator|.
name|values
operator|.
name|hashCode
argument_list|()
operator|-
name|other
operator|.
name|values
operator|.
name|hashCode
argument_list|()
expr_stmt|;
if|if
condition|(
name|result
operator|<
literal|0
condition|)
name|result
operator|=
operator|-
literal|1
expr_stmt|;
elseif|else
if|if
condition|(
name|result
operator|>
literal|0
condition|)
name|result
operator|=
literal|1
expr_stmt|;
block|}
return|return
name|result
return|;
block|}
comment|/**    * @return Immutable sorted map of families.    */
specifier|public
name|Collection
argument_list|<
name|HColumnDescriptor
argument_list|>
name|getFamilies
parameter_list|()
block|{
return|return
name|Collections
operator|.
name|unmodifiableCollection
argument_list|(
name|this
operator|.
name|families
operator|.
name|values
argument_list|()
argument_list|)
return|;
block|}
comment|/**    * @param column    * @return Column descriptor for the passed family name or the family on    * passed in column.    */
specifier|public
name|HColumnDescriptor
name|getFamily
parameter_list|(
specifier|final
name|byte
index|[]
name|column
parameter_list|)
block|{
return|return
name|this
operator|.
name|families
operator|.
name|get
argument_list|(
name|HStoreKey
operator|.
name|getFamilyMapKey
argument_list|(
name|column
argument_list|)
argument_list|)
return|;
block|}
comment|/**    * @param column    * @return Column descriptor for the passed family name or the family on    * passed in column.    */
specifier|public
name|HColumnDescriptor
name|removeFamily
parameter_list|(
specifier|final
name|byte
index|[]
name|column
parameter_list|)
block|{
return|return
name|this
operator|.
name|families
operator|.
name|remove
argument_list|(
name|HStoreKey
operator|.
name|getFamilyMapKey
argument_list|(
name|column
argument_list|)
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
specifier|final
name|byte
index|[]
name|tableName
parameter_list|)
block|{
return|return
operator|new
name|Path
argument_list|(
name|rootdir
argument_list|,
name|Bytes
operator|.
name|toString
argument_list|(
name|tableName
argument_list|)
argument_list|)
return|;
block|}
block|}
end_class

end_unit

