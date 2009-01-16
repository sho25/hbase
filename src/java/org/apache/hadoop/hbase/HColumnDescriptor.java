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
name|HashMap
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
comment|/**  * An HColumnDescriptor contains information about a column family such as the  * number of versions, compression settings, etc.  *   * It is used as input when creating a table or adding a column. Once set, the  * parameters that specify a column cannot be changed without deleting the  * column and recreating it. If there is data stored in the column, it will be  * deleted when the column is deleted.  */
end_comment

begin_class
specifier|public
class|class
name|HColumnDescriptor
implements|implements
name|WritableComparable
block|{
comment|// For future backward compatibility
comment|// Version 3 was when column names become byte arrays and when we picked up
comment|// Time-to-live feature.  Version 4 was when we moved to byte arrays, HBASE-82.
comment|// Version 5 was when bloom filter descriptors were removed.
comment|// Version 6 adds metadata as a map where keys and values are byte[].
specifier|private
specifier|static
specifier|final
name|byte
name|COLUMN_DESCRIPTOR_VERSION
init|=
operator|(
name|byte
operator|)
literal|6
decl_stmt|;
comment|/**     * The type of compression.    * @see org.apache.hadoop.io.SequenceFile.Writer    */
specifier|public
specifier|static
enum|enum
name|CompressionType
block|{
comment|/** Do not compress records. */
name|NONE
block|,
comment|/** Compress values only, each separately. */
name|RECORD
block|,
comment|/** Compress sequences of records together in blocks. */
name|BLOCK
block|}
specifier|public
specifier|static
specifier|final
name|String
name|COMPRESSION
init|=
literal|"COMPRESSION"
decl_stmt|;
comment|//TODO: change to protected
specifier|public
specifier|static
specifier|final
name|String
name|BLOCKCACHE
init|=
literal|"BLOCKCACHE"
decl_stmt|;
comment|//TODO: change to protected
specifier|public
specifier|static
specifier|final
name|String
name|LENGTH
init|=
literal|"LENGTH"
decl_stmt|;
comment|//TODO: change to protected
specifier|public
specifier|static
specifier|final
name|String
name|TTL
init|=
literal|"TTL"
decl_stmt|;
comment|//TODO: change to protected
specifier|public
specifier|static
specifier|final
name|String
name|BLOOMFILTER
init|=
literal|"BLOOMFILTER"
decl_stmt|;
comment|//TODO: change to protected
specifier|public
specifier|static
specifier|final
name|String
name|FOREVER
init|=
literal|"FOREVER"
decl_stmt|;
comment|//TODO: change to protected
specifier|public
specifier|static
specifier|final
name|String
name|MAPFILE_INDEX_INTERVAL
init|=
comment|//TODO: change to protected
literal|"MAPFILE_INDEX_INTERVAL"
decl_stmt|;
comment|/**    * Default compression type.    */
specifier|public
specifier|static
specifier|final
name|CompressionType
name|DEFAULT_COMPRESSION
init|=
name|CompressionType
operator|.
name|NONE
decl_stmt|;
comment|/**    * Default number of versions of a record to keep.    */
specifier|public
specifier|static
specifier|final
name|int
name|DEFAULT_VERSIONS
init|=
literal|3
decl_stmt|;
comment|/**    * Default maximum cell length.    */
specifier|public
specifier|static
specifier|final
name|int
name|DEFAULT_LENGTH
init|=
name|Integer
operator|.
name|MAX_VALUE
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|Integer
name|DEFAULT_LENGTH_INTEGER
init|=
name|Integer
operator|.
name|valueOf
argument_list|(
name|DEFAULT_LENGTH
argument_list|)
decl_stmt|;
comment|/*    * Cache here the HCD value.    * Question: its OK to cache since when we're reenable, we create a new HCD?    */
specifier|private
specifier|volatile
name|Integer
name|maxValueLength
init|=
literal|null
decl_stmt|;
comment|/**    * Default setting for whether to serve from memory or not.    */
specifier|public
specifier|static
specifier|final
name|boolean
name|DEFAULT_IN_MEMORY
init|=
literal|false
decl_stmt|;
comment|/**    * Default setting for whether to use a block cache or not.    */
specifier|public
specifier|static
specifier|final
name|boolean
name|DEFAULT_BLOCKCACHE
init|=
literal|false
decl_stmt|;
comment|/**    * Default setting for whether or not to use bloomfilters.    */
specifier|public
specifier|static
specifier|final
name|boolean
name|DEFAULT_BLOOMFILTER
init|=
literal|false
decl_stmt|;
comment|/**    * Default time to live of cell contents.    */
specifier|public
specifier|static
specifier|final
name|int
name|DEFAULT_TTL
init|=
name|HConstants
operator|.
name|FOREVER
decl_stmt|;
comment|// Column family name
specifier|private
name|byte
index|[]
name|name
decl_stmt|;
comment|/**    * Default mapfile index interval.    */
specifier|public
specifier|static
specifier|final
name|int
name|DEFAULT_MAPFILE_INDEX_INTERVAL
init|=
literal|128
decl_stmt|;
comment|// Column metadata
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
comment|/**    * Default constructor. Must be present for Writable.    */
specifier|public
name|HColumnDescriptor
parameter_list|()
block|{
name|this
operator|.
name|name
operator|=
literal|null
expr_stmt|;
block|}
comment|/**    * Construct a column descriptor specifying only the family name     * The other attributes are defaulted.    *     * @param familyName Column family name. Must be 'printable' -- digit or    * letter -- and end in a<code>:<code>    */
specifier|public
name|HColumnDescriptor
parameter_list|(
specifier|final
name|String
name|familyName
parameter_list|)
block|{
name|this
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|familyName
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * Construct a column descriptor specifying only the family name     * The other attributes are defaulted.    *     * @param familyName Column family name. Must be 'printable' -- digit or    * letter -- and end in a<code>:<code>    */
specifier|public
name|HColumnDescriptor
parameter_list|(
specifier|final
name|byte
index|[]
name|familyName
parameter_list|)
block|{
name|this
argument_list|(
name|familyName
operator|==
literal|null
operator|||
name|familyName
operator|.
name|length
operator|<=
literal|0
condition|?
name|HConstants
operator|.
name|EMPTY_BYTE_ARRAY
else|:
name|familyName
argument_list|,
name|DEFAULT_VERSIONS
argument_list|,
name|DEFAULT_COMPRESSION
argument_list|,
name|DEFAULT_IN_MEMORY
argument_list|,
name|DEFAULT_BLOCKCACHE
argument_list|,
name|Integer
operator|.
name|MAX_VALUE
argument_list|,
name|DEFAULT_TTL
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
comment|/**    * Constructor.    * Makes a deep copy of the supplied descriptor.     * Can make a modifiable descriptor from an UnmodifyableHColumnDescriptor.    * @param desc The descriptor.    */
specifier|public
name|HColumnDescriptor
parameter_list|(
name|HColumnDescriptor
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
comment|/**    * Constructor    * @param familyName Column family name. Must be 'printable' -- digit or    * letter -- and end in a<code>:<code>    * @param maxVersions Maximum number of versions to keep    * @param compression Compression type    * @param inMemory If true, column data should be kept in an HRegionServer's    * cache    * @param blockCacheEnabled If true, MapFile blocks should be cached    * @param maxValueLength Restrict values to&lt;= this value    * @param timeToLive Time-to-live of cell contents, in seconds    * (use HConstants.FOREVER for unlimited TTL)    * @param bloomFilter Enable the specified bloom filter for this column    *     * @throws IllegalArgumentException if passed a family name that is made of     * other than 'word' characters: i.e.<code>[a-zA-Z_0-9]</code> and does not    * end in a<code>:</code>    * @throws IllegalArgumentException if the number of versions is&lt;= 0    */
specifier|public
name|HColumnDescriptor
parameter_list|(
specifier|final
name|byte
index|[]
name|familyName
parameter_list|,
specifier|final
name|int
name|maxVersions
parameter_list|,
specifier|final
name|CompressionType
name|compression
parameter_list|,
specifier|final
name|boolean
name|inMemory
parameter_list|,
specifier|final
name|boolean
name|blockCacheEnabled
parameter_list|,
specifier|final
name|int
name|maxValueLength
parameter_list|,
specifier|final
name|int
name|timeToLive
parameter_list|,
specifier|final
name|boolean
name|bloomFilter
parameter_list|)
block|{
name|isLegalFamilyName
argument_list|(
name|familyName
argument_list|)
expr_stmt|;
name|this
operator|.
name|name
operator|=
name|stripColon
argument_list|(
name|familyName
argument_list|)
expr_stmt|;
if|if
condition|(
name|maxVersions
operator|<=
literal|0
condition|)
block|{
comment|// TODO: Allow maxVersion of 0 to be the way you say "Keep all versions".
comment|// Until there is support, consider 0 or< 0 -- a configuration error.
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Maximum versions must be positive"
argument_list|)
throw|;
block|}
name|setMaxVersions
argument_list|(
name|maxVersions
argument_list|)
expr_stmt|;
name|setInMemory
argument_list|(
name|inMemory
argument_list|)
expr_stmt|;
name|setBlockCacheEnabled
argument_list|(
name|blockCacheEnabled
argument_list|)
expr_stmt|;
name|setMaxValueLength
argument_list|(
name|maxValueLength
argument_list|)
expr_stmt|;
name|setTimeToLive
argument_list|(
name|timeToLive
argument_list|)
expr_stmt|;
name|setCompressionType
argument_list|(
name|compression
argument_list|)
expr_stmt|;
name|setBloomfilter
argument_list|(
name|bloomFilter
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|static
name|byte
index|[]
name|stripColon
parameter_list|(
specifier|final
name|byte
index|[]
name|n
parameter_list|)
block|{
name|byte
index|[]
name|result
init|=
operator|new
name|byte
index|[
name|n
operator|.
name|length
operator|-
literal|1
index|]
decl_stmt|;
comment|// Have the stored family name be absent the colon delimiter
name|System
operator|.
name|arraycopy
argument_list|(
name|n
argument_list|,
literal|0
argument_list|,
name|result
argument_list|,
literal|0
argument_list|,
name|n
operator|.
name|length
operator|-
literal|1
argument_list|)
expr_stmt|;
return|return
name|result
return|;
block|}
comment|/**    * @param b Family name.    * @return<code>b</code>    * @throws IllegalArgumentException If not null and not a legitimate family    * name: i.e. 'printable' and ends in a ':' (Null passes are allowed because    *<code>b</code> can be null when deserializing).    */
specifier|public
specifier|static
name|byte
index|[]
name|isLegalFamilyName
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
condition|)
block|{
return|return
name|b
return|;
block|}
if|if
condition|(
name|b
index|[
name|b
operator|.
name|length
operator|-
literal|1
index|]
operator|!=
literal|':'
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Family names must end in a colon: "
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
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
operator|(
name|b
operator|.
name|length
operator|-
literal|1
operator|)
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
operator|||
name|b
index|[
name|i
index|]
operator|==
literal|'.'
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
literal|">. Family names  can only contain  'word characters' and must end"
operator|+
literal|"with a colon: "
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
comment|/**    * @return Name of this column family    */
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
comment|/**    * @return Name of this column family with colon as required by client API    */
specifier|public
name|byte
index|[]
name|getNameWithColon
parameter_list|()
block|{
return|return
name|HStoreKey
operator|.
name|addDelimiter
argument_list|(
name|this
operator|.
name|name
argument_list|)
return|;
block|}
comment|/**    * @return Name of this column family    */
specifier|public
name|String
name|getNameAsString
parameter_list|()
block|{
return|return
name|Bytes
operator|.
name|toString
argument_list|(
name|this
operator|.
name|name
argument_list|)
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
comment|/**    * @return All values.    */
specifier|public
name|Map
argument_list|<
name|ImmutableBytesWritable
argument_list|,
name|ImmutableBytesWritable
argument_list|>
name|getValues
parameter_list|()
block|{
return|return
name|Collections
operator|.
name|unmodifiableMap
argument_list|(
name|values
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
comment|/** @return compression type being used for the column family */
specifier|public
name|CompressionType
name|getCompression
parameter_list|()
block|{
name|String
name|value
init|=
name|getValue
argument_list|(
name|COMPRESSION
argument_list|)
decl_stmt|;
if|if
condition|(
name|value
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|value
operator|.
name|equalsIgnoreCase
argument_list|(
literal|"BLOCK"
argument_list|)
condition|)
return|return
name|CompressionType
operator|.
name|BLOCK
return|;
elseif|else
if|if
condition|(
name|value
operator|.
name|equalsIgnoreCase
argument_list|(
literal|"RECORD"
argument_list|)
condition|)
return|return
name|CompressionType
operator|.
name|RECORD
return|;
block|}
return|return
name|CompressionType
operator|.
name|NONE
return|;
block|}
comment|/** @return maximum number of versions */
specifier|public
name|int
name|getMaxVersions
parameter_list|()
block|{
name|String
name|value
init|=
name|getValue
argument_list|(
name|HConstants
operator|.
name|VERSIONS
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
operator|.
name|intValue
argument_list|()
return|;
return|return
name|DEFAULT_VERSIONS
return|;
block|}
comment|/**    * @param maxVersions maximum number of versions    */
specifier|public
name|void
name|setMaxVersions
parameter_list|(
name|int
name|maxVersions
parameter_list|)
block|{
name|setValue
argument_list|(
name|HConstants
operator|.
name|VERSIONS
argument_list|,
name|Integer
operator|.
name|toString
argument_list|(
name|maxVersions
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * @return Compression type setting.    */
specifier|public
name|CompressionType
name|getCompressionType
parameter_list|()
block|{
return|return
name|getCompression
argument_list|()
return|;
block|}
comment|/**    * @param type Compression type setting.    */
specifier|public
name|void
name|setCompressionType
parameter_list|(
name|CompressionType
name|type
parameter_list|)
block|{
name|String
name|compressionType
decl_stmt|;
switch|switch
condition|(
name|type
condition|)
block|{
case|case
name|BLOCK
case|:
name|compressionType
operator|=
literal|"BLOCK"
expr_stmt|;
break|break;
case|case
name|RECORD
case|:
name|compressionType
operator|=
literal|"RECORD"
expr_stmt|;
break|break;
default|default:
name|compressionType
operator|=
literal|"NONE"
expr_stmt|;
break|break;
block|}
name|setValue
argument_list|(
name|COMPRESSION
argument_list|,
name|compressionType
argument_list|)
expr_stmt|;
block|}
comment|/**    * @return True if we are to keep all in use HRegionServer cache.    */
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
name|HConstants
operator|.
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
operator|.
name|booleanValue
argument_list|()
return|;
return|return
name|DEFAULT_IN_MEMORY
return|;
block|}
comment|/**    * @param inMemory True if we are to keep all values in the HRegionServer    * cache    */
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
name|HConstants
operator|.
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
comment|/**    * @return Maximum value length.    */
specifier|public
specifier|synchronized
name|int
name|getMaxValueLength
parameter_list|()
block|{
if|if
condition|(
name|this
operator|.
name|maxValueLength
operator|==
literal|null
condition|)
block|{
name|String
name|value
init|=
name|getValue
argument_list|(
name|LENGTH
argument_list|)
decl_stmt|;
name|this
operator|.
name|maxValueLength
operator|=
operator|(
name|value
operator|!=
literal|null
operator|)
condition|?
name|Integer
operator|.
name|decode
argument_list|(
name|value
argument_list|)
else|:
name|DEFAULT_LENGTH_INTEGER
expr_stmt|;
block|}
return|return
name|this
operator|.
name|maxValueLength
operator|.
name|intValue
argument_list|()
return|;
block|}
comment|/**    * @param maxLength Maximum value length.    */
specifier|public
name|void
name|setMaxValueLength
parameter_list|(
name|int
name|maxLength
parameter_list|)
block|{
name|setValue
argument_list|(
name|LENGTH
argument_list|,
name|Integer
operator|.
name|toString
argument_list|(
name|maxLength
argument_list|)
argument_list|)
expr_stmt|;
name|this
operator|.
name|maxValueLength
operator|=
literal|null
expr_stmt|;
block|}
comment|/**    * @return Time-to-live of cell contents, in seconds.    */
specifier|public
name|int
name|getTimeToLive
parameter_list|()
block|{
name|String
name|value
init|=
name|getValue
argument_list|(
name|TTL
argument_list|)
decl_stmt|;
return|return
operator|(
name|value
operator|!=
literal|null
operator|)
condition|?
name|Integer
operator|.
name|valueOf
argument_list|(
name|value
argument_list|)
operator|.
name|intValue
argument_list|()
else|:
name|DEFAULT_TTL
return|;
block|}
comment|/**    * @param timeToLive Time-to-live of cell contents, in seconds.    */
specifier|public
name|void
name|setTimeToLive
parameter_list|(
name|int
name|timeToLive
parameter_list|)
block|{
name|setValue
argument_list|(
name|TTL
argument_list|,
name|Integer
operator|.
name|toString
argument_list|(
name|timeToLive
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * @return True if MapFile blocks should be cached.    */
specifier|public
name|boolean
name|isBlockCacheEnabled
parameter_list|()
block|{
name|String
name|value
init|=
name|getValue
argument_list|(
name|BLOCKCACHE
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
operator|.
name|booleanValue
argument_list|()
return|;
return|return
name|DEFAULT_BLOCKCACHE
return|;
block|}
comment|/**    * @param blockCacheEnabled True if MapFile blocks should be cached.    */
specifier|public
name|void
name|setBlockCacheEnabled
parameter_list|(
name|boolean
name|blockCacheEnabled
parameter_list|)
block|{
name|setValue
argument_list|(
name|BLOCKCACHE
argument_list|,
name|Boolean
operator|.
name|toString
argument_list|(
name|blockCacheEnabled
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * @return true if a bloom filter is enabled    */
specifier|public
name|boolean
name|isBloomfilter
parameter_list|()
block|{
name|String
name|value
init|=
name|getValue
argument_list|(
name|BLOOMFILTER
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
operator|.
name|booleanValue
argument_list|()
return|;
return|return
name|DEFAULT_BLOOMFILTER
return|;
block|}
comment|/**    * @param onOff Enable/Disable bloom filter    */
specifier|public
name|void
name|setBloomfilter
parameter_list|(
specifier|final
name|boolean
name|onOff
parameter_list|)
block|{
name|setValue
argument_list|(
name|BLOOMFILTER
argument_list|,
name|Boolean
operator|.
name|toString
argument_list|(
name|onOff
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * @return The number of entries that are added to the store MapFile before    * an index entry is added.    */
specifier|public
name|int
name|getMapFileIndexInterval
parameter_list|()
block|{
name|String
name|value
init|=
name|getValue
argument_list|(
name|MAPFILE_INDEX_INTERVAL
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
operator|.
name|intValue
argument_list|()
return|;
return|return
name|DEFAULT_MAPFILE_INDEX_INTERVAL
return|;
block|}
comment|/**    * @param interval The number of entries that are added to the store MapFile before    * an index entry is added.    */
specifier|public
name|void
name|setMapFileIndexInterval
parameter_list|(
name|int
name|interval
parameter_list|)
block|{
name|setValue
argument_list|(
name|MAPFILE_INDEX_INTERVAL
argument_list|,
name|Integer
operator|.
name|toString
argument_list|(
name|interval
argument_list|)
argument_list|)
expr_stmt|;
block|}
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
name|COLUMN_DESCRIPTOR_VERSION
argument_list|)
operator|.
name|hashCode
argument_list|()
expr_stmt|;
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
annotation|@
name|SuppressWarnings
argument_list|(
literal|"deprecation"
argument_list|)
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
name|readByte
argument_list|()
decl_stmt|;
if|if
condition|(
name|version
operator|<
literal|6
condition|)
block|{
if|if
condition|(
name|version
operator|<=
literal|2
condition|)
block|{
name|Text
name|t
init|=
operator|new
name|Text
argument_list|()
decl_stmt|;
name|t
operator|.
name|readFields
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|this
operator|.
name|name
operator|=
name|t
operator|.
name|getBytes
argument_list|()
expr_stmt|;
if|if
condition|(
name|HStoreKey
operator|.
name|getFamilyDelimiterIndex
argument_list|(
name|this
operator|.
name|name
argument_list|)
operator|>
literal|0
condition|)
block|{
name|this
operator|.
name|name
operator|=
name|stripColon
argument_list|(
name|this
operator|.
name|name
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|this
operator|.
name|name
operator|=
name|Bytes
operator|.
name|readByteArray
argument_list|(
name|in
argument_list|)
expr_stmt|;
block|}
name|this
operator|.
name|values
operator|.
name|clear
argument_list|()
expr_stmt|;
name|setMaxVersions
argument_list|(
name|in
operator|.
name|readInt
argument_list|()
argument_list|)
expr_stmt|;
name|int
name|ordinal
init|=
name|in
operator|.
name|readInt
argument_list|()
decl_stmt|;
name|setCompressionType
argument_list|(
name|CompressionType
operator|.
name|values
argument_list|()
index|[
name|ordinal
index|]
argument_list|)
expr_stmt|;
name|setInMemory
argument_list|(
name|in
operator|.
name|readBoolean
argument_list|()
argument_list|)
expr_stmt|;
name|setMaxValueLength
argument_list|(
name|in
operator|.
name|readInt
argument_list|()
argument_list|)
expr_stmt|;
name|setBloomfilter
argument_list|(
name|in
operator|.
name|readBoolean
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|isBloomfilter
argument_list|()
operator|&&
name|version
operator|<
literal|5
condition|)
block|{
comment|// If a bloomFilter is enabled and the column descriptor is less than
comment|// version 5, we need to skip over it to read the rest of the column
comment|// descriptor. There are no BloomFilterDescriptors written to disk for
comment|// column descriptors with a version number>= 5
throw|throw
operator|new
name|UnsupportedClassVersionError
argument_list|(
name|this
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
operator|+
literal|" does not support backward compatibility with versions older "
operator|+
literal|"than version 5"
argument_list|)
throw|;
block|}
if|if
condition|(
name|version
operator|>
literal|1
condition|)
block|{
name|setBlockCacheEnabled
argument_list|(
name|in
operator|.
name|readBoolean
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|version
operator|>
literal|2
condition|)
block|{
name|setTimeToLive
argument_list|(
name|in
operator|.
name|readInt
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
comment|// version 6+
name|this
operator|.
name|name
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
name|values
operator|.
name|clear
argument_list|()
expr_stmt|;
name|int
name|numValues
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
name|numValues
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
block|}
block|}
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
name|writeByte
argument_list|(
name|COLUMN_DESCRIPTOR_VERSION
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
name|name
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
block|}
comment|// Comparable
specifier|public
name|int
name|compareTo
parameter_list|(
name|Object
name|o
parameter_list|)
block|{
name|HColumnDescriptor
name|other
init|=
operator|(
name|HColumnDescriptor
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
name|getName
argument_list|()
argument_list|)
decl_stmt|;
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
block|}
end_class

end_unit

