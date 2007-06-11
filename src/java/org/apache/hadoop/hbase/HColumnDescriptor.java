begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2006 The Apache Software Foundation  *  * Licensed under the Apache License, Version 2.0 (the "License");  * you may not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
comment|/**  * A HColumnDescriptor contains information about a column family such as the  * number of versions, compression settings, etc.  *  */
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
specifier|private
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
comment|// Internal values for compression type used for serialization
specifier|private
specifier|static
specifier|final
name|byte
name|COMPRESSION_NONE
init|=
operator|(
name|byte
operator|)
literal|0
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
name|COMPRESSION_RECORD
init|=
operator|(
name|byte
operator|)
literal|1
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
name|COMPRESSION_BLOCK
init|=
operator|(
name|byte
operator|)
literal|2
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|DEFAULT_N_VERSIONS
init|=
literal|3
decl_stmt|;
name|Text
name|name
decl_stmt|;
comment|// Column family name
name|int
name|maxVersions
decl_stmt|;
comment|// Number of versions to keep
name|byte
name|compressionType
decl_stmt|;
comment|// Compression setting if any
name|boolean
name|inMemory
decl_stmt|;
comment|// Serve reads from in-memory cache
name|int
name|maxValueLength
decl_stmt|;
comment|// Maximum value size
name|boolean
name|bloomFilterEnabled
decl_stmt|;
comment|// True if column has a bloom filter
name|byte
name|versionNumber
decl_stmt|;
comment|// Version number of this class
comment|/**    * Default constructor. Must be present for Writable.    */
specifier|public
name|HColumnDescriptor
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
name|maxVersions
operator|=
name|DEFAULT_N_VERSIONS
expr_stmt|;
name|this
operator|.
name|compressionType
operator|=
name|COMPRESSION_NONE
expr_stmt|;
name|this
operator|.
name|inMemory
operator|=
literal|false
expr_stmt|;
name|this
operator|.
name|maxValueLength
operator|=
name|Integer
operator|.
name|MAX_VALUE
expr_stmt|;
name|this
operator|.
name|bloomFilterEnabled
operator|=
literal|false
expr_stmt|;
name|this
operator|.
name|versionNumber
operator|=
name|COLUMN_DESCRIPTOR_VERSION
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
argument_list|()
expr_stmt|;
name|this
operator|.
name|name
operator|.
name|set
argument_list|(
name|columnName
argument_list|)
expr_stmt|;
block|}
comment|/**    * Constructor - specify all parameters.    * @param name                - Column family name    * @param maxVersions         - Maximum number of versions to keep    * @param compression         - Compression type    * @param inMemory            - If true, column data should be kept in a    *                              HRegionServer's cache    * @param maxValueLength      - Restrict values to&lt;= this value    * @param bloomFilter         - Enable a bloom filter for this column    *     * @throws IllegalArgumentException if passed a family name that is made of     * other than 'word' characters: i.e.<code>[a-zA-Z_0-9]</code> and does not    * end in a<code>:</code>    * @throws IllegalArgumentException if the number of versions is&lt;= 0    */
specifier|public
name|HColumnDescriptor
parameter_list|(
name|Text
name|name
parameter_list|,
name|int
name|maxVersions
parameter_list|,
name|CompressionType
name|compression
parameter_list|,
name|boolean
name|inMemory
parameter_list|,
name|int
name|maxValueLength
parameter_list|,
name|boolean
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
literal|"Family names can only contain 'word characters' and must end with a ':'"
argument_list|)
throw|;
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
if|if
condition|(
name|compression
operator|==
name|CompressionType
operator|.
name|NONE
condition|)
block|{
name|this
operator|.
name|compressionType
operator|=
name|COMPRESSION_NONE
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|compression
operator|==
name|CompressionType
operator|.
name|BLOCK
condition|)
block|{
name|this
operator|.
name|compressionType
operator|=
name|COMPRESSION_BLOCK
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|compression
operator|==
name|CompressionType
operator|.
name|RECORD
condition|)
block|{
name|this
operator|.
name|compressionType
operator|=
name|COMPRESSION_RECORD
expr_stmt|;
block|}
else|else
block|{
assert|assert
operator|(
literal|false
operator|)
assert|;
block|}
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
name|bloomFilterEnabled
operator|=
name|bloomFilter
expr_stmt|;
name|this
operator|.
name|versionNumber
operator|=
name|COLUMN_DESCRIPTOR_VERSION
expr_stmt|;
block|}
comment|/**    * @return    - name of column family    */
specifier|public
name|Text
name|getName
parameter_list|()
block|{
return|return
name|name
return|;
block|}
comment|/**    * @return    - compression type being used for the column family    */
specifier|public
name|CompressionType
name|getCompression
parameter_list|()
block|{
name|CompressionType
name|value
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|this
operator|.
name|compressionType
operator|==
name|COMPRESSION_NONE
condition|)
block|{
name|value
operator|=
name|CompressionType
operator|.
name|NONE
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|this
operator|.
name|compressionType
operator|==
name|COMPRESSION_BLOCK
condition|)
block|{
name|value
operator|=
name|CompressionType
operator|.
name|BLOCK
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|this
operator|.
name|compressionType
operator|==
name|COMPRESSION_RECORD
condition|)
block|{
name|value
operator|=
name|CompressionType
operator|.
name|RECORD
expr_stmt|;
block|}
else|else
block|{
assert|assert
operator|(
literal|false
operator|)
assert|;
block|}
return|return
name|value
return|;
block|}
comment|/**    * @return    - maximum number of versions    */
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
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
name|String
name|compression
init|=
literal|"none"
decl_stmt|;
switch|switch
condition|(
name|compressionType
condition|)
block|{
case|case
name|COMPRESSION_NONE
case|:
break|break;
case|case
name|COMPRESSION_RECORD
case|:
name|compression
operator|=
literal|"record"
expr_stmt|;
break|break;
case|case
name|COMPRESSION_BLOCK
case|:
name|compression
operator|=
literal|"block"
expr_stmt|;
break|break;
default|default:
assert|assert
operator|(
literal|false
operator|)
assert|;
block|}
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
name|compression
operator|+
literal|", in memory: "
operator|+
name|inMemory
operator|+
literal|", max value length: "
operator|+
name|maxValueLength
operator|+
literal|", bloom filter:"
operator|+
name|bloomFilterEnabled
operator|+
literal|")"
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
name|Byte
operator|.
name|valueOf
argument_list|(
name|this
operator|.
name|compressionType
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
name|bloomFilterEnabled
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
return|return
name|result
return|;
block|}
comment|//////////////////////////////////////////////////////////////////////////////
comment|// Writable
comment|//////////////////////////////////////////////////////////////////////////////
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
name|this
operator|.
name|compressionType
operator|=
name|in
operator|.
name|readByte
argument_list|()
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
name|bloomFilterEnabled
operator|=
name|in
operator|.
name|readBoolean
argument_list|()
expr_stmt|;
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
name|writeByte
argument_list|(
name|this
operator|.
name|compressionType
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
name|bloomFilterEnabled
argument_list|)
expr_stmt|;
block|}
comment|//////////////////////////////////////////////////////////////////////////////
comment|// Comparable
comment|//////////////////////////////////////////////////////////////////////////////
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
name|Integer
operator|.
name|valueOf
argument_list|(
name|this
operator|.
name|compressionType
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
name|compressionType
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
name|bloomFilterEnabled
operator|==
name|other
operator|.
name|bloomFilterEnabled
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
name|bloomFilterEnabled
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
return|return
name|result
return|;
block|}
block|}
end_class

end_unit

