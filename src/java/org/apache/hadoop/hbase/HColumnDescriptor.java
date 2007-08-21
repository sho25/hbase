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
comment|/**  * An HColumnDescriptor contains information about a column family such as the  * number of versions, compression settings, etc.  */
end_comment

begin_class
specifier|public
class|class
name|HColumnDescriptor
implements|implements
name|WritableComparable
block|{
comment|// For future backward compatibility
specifier|private
specifier|static
specifier|final
name|byte
name|COLUMN_DESCRIPTOR_VERSION
init|=
operator|(
name|byte
operator|)
literal|1
decl_stmt|;
comment|// Legal family names can only contain 'word characters' and end in a colon.
specifier|public
specifier|static
specifier|final
name|Pattern
name|LEGAL_FAMILY_NAME
init|=
name|Pattern
operator|.
name|compile
argument_list|(
literal|"\\w+:"
argument_list|)
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
comment|/**    * Default compression type.    */
specifier|public
specifier|static
specifier|final
name|CompressionType
name|DEFAULT_COMPRESSION_TYPE
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
name|DEFAULT_N_VERSIONS
init|=
literal|3
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
comment|/**    * Default maximum length of cell contents.    */
specifier|public
specifier|static
specifier|final
name|int
name|DEFAULT_MAX_VALUE_LENGTH
init|=
name|Integer
operator|.
name|MAX_VALUE
decl_stmt|;
comment|/**    * Default bloom filter description.    */
specifier|public
specifier|static
specifier|final
name|BloomFilterDescriptor
name|DEFAULT_BLOOM_FILTER_DESCRIPTOR
init|=
literal|null
decl_stmt|;
comment|// Column family name
specifier|private
name|Text
name|name
decl_stmt|;
comment|// Number of versions to keep
specifier|private
name|int
name|maxVersions
decl_stmt|;
comment|// Compression setting if any
specifier|private
name|CompressionType
name|compressionType
decl_stmt|;
comment|// Serve reads from in-memory cache
specifier|private
name|boolean
name|inMemory
decl_stmt|;
comment|// Maximum value size
specifier|private
name|int
name|maxValueLength
decl_stmt|;
comment|// True if bloom filter was specified
specifier|private
name|boolean
name|bloomFilterSpecified
decl_stmt|;
comment|// Descriptor of bloom filter
specifier|private
name|BloomFilterDescriptor
name|bloomFilter
decl_stmt|;
comment|// Version number of this class
specifier|private
name|byte
name|versionNumber
decl_stmt|;
comment|/**    * Default constructor. Must be present for Writable.    */
specifier|public
name|HColumnDescriptor
parameter_list|()
block|{
name|this
argument_list|(
literal|null
argument_list|)
expr_stmt|;
block|}
comment|/**    * Construct a column descriptor specifying only the family name     * The other attributes are defaulted.    *     * @param columnName - column family name    */
specifier|public
name|HColumnDescriptor
parameter_list|(
name|String
name|columnName
parameter_list|)
block|{
name|this
argument_list|(
name|columnName
operator|==
literal|null
operator|||
name|columnName
operator|.
name|length
argument_list|()
operator|<=
literal|0
condition|?
operator|new
name|Text
argument_list|()
else|:
operator|new
name|Text
argument_list|(
name|columnName
argument_list|)
argument_list|,
name|DEFAULT_N_VERSIONS
argument_list|,
name|DEFAULT_COMPRESSION_TYPE
argument_list|,
name|DEFAULT_IN_MEMORY
argument_list|,
name|Integer
operator|.
name|MAX_VALUE
argument_list|,
name|DEFAULT_BLOOM_FILTER_DESCRIPTOR
argument_list|)
expr_stmt|;
block|}
comment|/**    * Constructor    * Specify all parameters.    * @param name Column family name    * @param maxVersions Maximum number of versions to keep    * @param compression Compression type    * @param inMemory If true, column data should be kept in an HRegionServer's    * cache    * @param maxValueLength Restrict values to&lt;= this value    * @param bloomFilter Enable the specified bloom filter for this column    *     * @throws IllegalArgumentException if passed a family name that is made of     * other than 'word' characters: i.e.<code>[a-zA-Z_0-9]</code> and does not    * end in a<code>:</code>    * @throws IllegalArgumentException if the number of versions is&lt;= 0    */
specifier|public
name|HColumnDescriptor
parameter_list|(
specifier|final
name|Text
name|name
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
name|int
name|maxValueLength
parameter_list|,
specifier|final
name|BloomFilterDescriptor
name|bloomFilter
parameter_list|)
block|{
name|String
name|familyStr
init|=
name|name
operator|.
name|toString
argument_list|()
decl_stmt|;
comment|// Test name if not null (It can be null when deserializing after
comment|// construction but before we've read in the fields);
if|if
condition|(
name|familyStr
operator|.
name|length
argument_list|()
operator|>
literal|0
condition|)
block|{
name|Matcher
name|m
init|=
name|LEGAL_FAMILY_NAME
operator|.
name|matcher
argument_list|(
name|familyStr
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
literal|"Illegal family name<"
operator|+
name|name
operator|+
literal|">. Family names can only contain "
operator|+
literal|"'word characters' and must end with a ':'"
argument_list|)
throw|;
block|}
block|}
name|this
operator|.
name|name
operator|=
name|name
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
name|this
operator|.
name|maxVersions
operator|=
name|maxVersions
expr_stmt|;
name|this
operator|.
name|inMemory
operator|=
name|inMemory
expr_stmt|;
name|this
operator|.
name|maxValueLength
operator|=
name|maxValueLength
expr_stmt|;
name|this
operator|.
name|bloomFilter
operator|=
name|bloomFilter
expr_stmt|;
name|this
operator|.
name|bloomFilterSpecified
operator|=
name|this
operator|.
name|bloomFilter
operator|==
literal|null
condition|?
literal|false
else|:
literal|true
expr_stmt|;
name|this
operator|.
name|versionNumber
operator|=
name|COLUMN_DESCRIPTOR_VERSION
expr_stmt|;
name|this
operator|.
name|compressionType
operator|=
name|compression
expr_stmt|;
block|}
comment|/** @return name of column family */
specifier|public
name|Text
name|getName
parameter_list|()
block|{
return|return
name|name
return|;
block|}
comment|/** @return compression type being used for the column family */
specifier|public
name|CompressionType
name|getCompression
parameter_list|()
block|{
return|return
name|this
operator|.
name|compressionType
return|;
block|}
comment|/** @return maximum number of versions */
specifier|public
name|int
name|getMaxVersions
parameter_list|()
block|{
return|return
name|this
operator|.
name|maxVersions
return|;
block|}
comment|/**    * @return Compression type setting.    */
specifier|public
name|CompressionType
name|getCompressionType
parameter_list|()
block|{
return|return
name|this
operator|.
name|compressionType
return|;
block|}
comment|/**    * @return True if we are to keep all in use HRegionServer cache.    */
specifier|public
name|boolean
name|isInMemory
parameter_list|()
block|{
return|return
name|this
operator|.
name|inMemory
return|;
block|}
comment|/**    * @return Maximum value length.    */
specifier|public
name|int
name|getMaxValueLength
parameter_list|()
block|{
return|return
name|this
operator|.
name|maxValueLength
return|;
block|}
comment|/**    * @return Bloom filter descriptor or null if none set.    */
specifier|public
name|BloomFilterDescriptor
name|getBloomFilter
parameter_list|()
block|{
return|return
name|this
operator|.
name|bloomFilter
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
literal|"("
operator|+
name|name
operator|+
literal|", max versions: "
operator|+
name|maxVersions
operator|+
literal|", compression: "
operator|+
name|this
operator|.
name|compressionType
operator|+
literal|", in memory: "
operator|+
name|inMemory
operator|+
literal|", max value length: "
operator|+
name|maxValueLength
operator|+
literal|", bloom filter: "
operator|+
operator|(
name|bloomFilterSpecified
condition|?
name|bloomFilter
operator|.
name|toString
argument_list|()
else|:
literal|"none"
operator|)
operator|+
literal|")"
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
name|this
operator|.
name|name
operator|.
name|hashCode
argument_list|()
decl_stmt|;
name|result
operator|^=
name|Integer
operator|.
name|valueOf
argument_list|(
name|this
operator|.
name|maxVersions
argument_list|)
operator|.
name|hashCode
argument_list|()
expr_stmt|;
name|result
operator|^=
name|this
operator|.
name|compressionType
operator|.
name|hashCode
argument_list|()
expr_stmt|;
name|result
operator|^=
name|Boolean
operator|.
name|valueOf
argument_list|(
name|this
operator|.
name|inMemory
argument_list|)
operator|.
name|hashCode
argument_list|()
expr_stmt|;
name|result
operator|^=
name|Integer
operator|.
name|valueOf
argument_list|(
name|this
operator|.
name|maxValueLength
argument_list|)
operator|.
name|hashCode
argument_list|()
expr_stmt|;
name|result
operator|^=
name|Boolean
operator|.
name|valueOf
argument_list|(
name|this
operator|.
name|bloomFilterSpecified
argument_list|)
operator|.
name|hashCode
argument_list|()
expr_stmt|;
name|result
operator|^=
name|Byte
operator|.
name|valueOf
argument_list|(
name|this
operator|.
name|versionNumber
argument_list|)
operator|.
name|hashCode
argument_list|()
expr_stmt|;
if|if
condition|(
name|this
operator|.
name|bloomFilterSpecified
condition|)
block|{
name|result
operator|^=
name|this
operator|.
name|bloomFilter
operator|.
name|hashCode
argument_list|()
expr_stmt|;
block|}
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
name|this
operator|.
name|versionNumber
operator|=
name|in
operator|.
name|readByte
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
name|this
operator|.
name|maxVersions
operator|=
name|in
operator|.
name|readInt
argument_list|()
expr_stmt|;
name|int
name|ordinal
init|=
name|in
operator|.
name|readInt
argument_list|()
decl_stmt|;
name|this
operator|.
name|compressionType
operator|=
name|CompressionType
operator|.
name|values
argument_list|()
index|[
name|ordinal
index|]
expr_stmt|;
name|this
operator|.
name|inMemory
operator|=
name|in
operator|.
name|readBoolean
argument_list|()
expr_stmt|;
name|this
operator|.
name|maxValueLength
operator|=
name|in
operator|.
name|readInt
argument_list|()
expr_stmt|;
name|this
operator|.
name|bloomFilterSpecified
operator|=
name|in
operator|.
name|readBoolean
argument_list|()
expr_stmt|;
if|if
condition|(
name|bloomFilterSpecified
condition|)
block|{
name|bloomFilter
operator|=
operator|new
name|BloomFilterDescriptor
argument_list|()
expr_stmt|;
name|bloomFilter
operator|.
name|readFields
argument_list|(
name|in
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
name|writeByte
argument_list|(
name|this
operator|.
name|versionNumber
argument_list|)
expr_stmt|;
name|this
operator|.
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
name|this
operator|.
name|maxVersions
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeInt
argument_list|(
name|this
operator|.
name|compressionType
operator|.
name|ordinal
argument_list|()
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeBoolean
argument_list|(
name|this
operator|.
name|inMemory
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeInt
argument_list|(
name|this
operator|.
name|maxValueLength
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeBoolean
argument_list|(
name|this
operator|.
name|bloomFilterSpecified
argument_list|)
expr_stmt|;
if|if
condition|(
name|bloomFilterSpecified
condition|)
block|{
name|bloomFilter
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
comment|// NOTE: we don't do anything with the version number yet.
comment|// Version numbers will come into play when we introduce an incompatible
comment|// change in the future such as the addition of access control lists.
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
name|this
operator|.
name|name
operator|.
name|compareTo
argument_list|(
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
name|result
operator|=
name|Integer
operator|.
name|valueOf
argument_list|(
name|this
operator|.
name|maxVersions
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
name|maxVersions
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
name|result
operator|=
name|this
operator|.
name|compressionType
operator|.
name|compareTo
argument_list|(
name|other
operator|.
name|compressionType
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
if|if
condition|(
name|this
operator|.
name|inMemory
operator|==
name|other
operator|.
name|inMemory
condition|)
block|{
name|result
operator|=
literal|0
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|this
operator|.
name|inMemory
condition|)
block|{
name|result
operator|=
operator|-
literal|1
expr_stmt|;
block|}
else|else
block|{
name|result
operator|=
literal|1
expr_stmt|;
block|}
block|}
if|if
condition|(
name|result
operator|==
literal|0
condition|)
block|{
name|result
operator|=
name|other
operator|.
name|maxValueLength
operator|-
name|this
operator|.
name|maxValueLength
expr_stmt|;
block|}
if|if
condition|(
name|result
operator|==
literal|0
condition|)
block|{
if|if
condition|(
name|this
operator|.
name|bloomFilterSpecified
operator|==
name|other
operator|.
name|bloomFilterSpecified
condition|)
block|{
name|result
operator|=
literal|0
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|this
operator|.
name|bloomFilterSpecified
condition|)
block|{
name|result
operator|=
operator|-
literal|1
expr_stmt|;
block|}
else|else
block|{
name|result
operator|=
literal|1
expr_stmt|;
block|}
block|}
if|if
condition|(
name|result
operator|==
literal|0
operator|&&
name|this
operator|.
name|bloomFilterSpecified
condition|)
block|{
name|result
operator|=
name|this
operator|.
name|bloomFilter
operator|.
name|compareTo
argument_list|(
name|other
operator|.
name|bloomFilter
argument_list|)
expr_stmt|;
block|}
return|return
name|result
return|;
block|}
block|}
end_class

end_unit

