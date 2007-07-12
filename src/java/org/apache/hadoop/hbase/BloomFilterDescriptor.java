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
comment|/**   * Supplied as a parameter to HColumnDescriptor to specify what kind of  * bloom filter to use for a column, and its configuration parameters  */
end_comment

begin_class
specifier|public
class|class
name|BloomFilterDescriptor
implements|implements
name|WritableComparable
block|{
comment|/*    * Specify the kind of bloom filter that will be instantiated    */
comment|/**    *<i>Bloom filter</i>, as defined by Bloom in 1970.    */
specifier|public
specifier|static
specifier|final
name|int
name|BLOOMFILTER
init|=
literal|1
decl_stmt|;
comment|/**    *<i>counting Bloom filter</i>, as defined by Fan et al. in a ToN 2000 paper.    */
specifier|public
specifier|static
specifier|final
name|int
name|COUNTING_BLOOMFILTER
init|=
literal|2
decl_stmt|;
comment|/**    *<i>retouched Bloom filter</i>, as defined in the CoNEXT 2006 paper.    */
specifier|public
specifier|static
specifier|final
name|int
name|RETOUCHED_BLOOMFILTER
init|=
literal|3
decl_stmt|;
comment|/** Default constructor - used in conjunction with Writable */
specifier|public
name|BloomFilterDescriptor
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
block|}
comment|/**    * @param type The kind of bloom filter to use.    * @param vectorSize The vector size of<i>this</i> filter.    * @param nbHash The number of hash functions to consider.    */
specifier|public
name|BloomFilterDescriptor
parameter_list|(
name|int
name|type
parameter_list|,
name|int
name|vectorSize
parameter_list|,
name|int
name|nbHash
parameter_list|)
block|{
switch|switch
condition|(
name|type
condition|)
block|{
case|case
name|BLOOMFILTER
case|:
case|case
name|COUNTING_BLOOMFILTER
case|:
case|case
name|RETOUCHED_BLOOMFILTER
case|:
name|this
operator|.
name|filterType
operator|=
name|type
expr_stmt|;
break|break;
default|default:
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Invalid bloom filter type: "
operator|+
name|type
argument_list|)
throw|;
block|}
name|this
operator|.
name|vectorSize
operator|=
name|vectorSize
expr_stmt|;
name|this
operator|.
name|nbHash
operator|=
name|nbHash
expr_stmt|;
block|}
name|int
name|filterType
decl_stmt|;
name|int
name|vectorSize
decl_stmt|;
name|int
name|nbHash
decl_stmt|;
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
name|StringBuilder
name|value
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
switch|switch
condition|(
name|filterType
condition|)
block|{
case|case
name|BLOOMFILTER
case|:
name|value
operator|.
name|append
argument_list|(
literal|"standard"
argument_list|)
expr_stmt|;
break|break;
case|case
name|COUNTING_BLOOMFILTER
case|:
name|value
operator|.
name|append
argument_list|(
literal|"counting"
argument_list|)
expr_stmt|;
break|break;
case|case
name|RETOUCHED_BLOOMFILTER
case|:
name|value
operator|.
name|append
argument_list|(
literal|"retouched"
argument_list|)
expr_stmt|;
block|}
name|value
operator|.
name|append
argument_list|(
literal|"(vector size="
argument_list|)
expr_stmt|;
name|value
operator|.
name|append
argument_list|(
name|vectorSize
argument_list|)
expr_stmt|;
name|value
operator|.
name|append
argument_list|(
literal|", number hashes="
argument_list|)
expr_stmt|;
name|value
operator|.
name|append
argument_list|(
name|nbHash
argument_list|)
expr_stmt|;
name|value
operator|.
name|append
argument_list|(
literal|")"
argument_list|)
expr_stmt|;
return|return
name|value
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
name|Integer
operator|.
name|valueOf
argument_list|(
name|this
operator|.
name|filterType
argument_list|)
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
name|vectorSize
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
name|nbHash
argument_list|)
operator|.
name|hashCode
argument_list|()
expr_stmt|;
return|return
name|result
return|;
block|}
comment|// Writable
comment|/* (non-Javadoc)    * @see org.apache.hadoop.io.Writable#readFields(java.io.DataInput)    */
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
name|filterType
operator|=
name|in
operator|.
name|readInt
argument_list|()
expr_stmt|;
name|vectorSize
operator|=
name|in
operator|.
name|readInt
argument_list|()
expr_stmt|;
name|nbHash
operator|=
name|in
operator|.
name|readInt
argument_list|()
expr_stmt|;
block|}
comment|/* (non-Javadoc)    * @see org.apache.hadoop.io.Writable#write(java.io.DataOutput)    */
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
name|filterType
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeInt
argument_list|(
name|vectorSize
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeInt
argument_list|(
name|nbHash
argument_list|)
expr_stmt|;
block|}
comment|// Comparable
comment|/* (non-Javadoc)    * @see java.lang.Comparable#compareTo(java.lang.Object)    */
specifier|public
name|int
name|compareTo
parameter_list|(
name|Object
name|o
parameter_list|)
block|{
name|BloomFilterDescriptor
name|other
init|=
operator|(
name|BloomFilterDescriptor
operator|)
name|o
decl_stmt|;
name|int
name|result
init|=
name|this
operator|.
name|filterType
operator|-
name|other
operator|.
name|filterType
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
name|this
operator|.
name|vectorSize
operator|-
name|other
operator|.
name|vectorSize
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
name|nbHash
operator|-
name|other
operator|.
name|nbHash
expr_stmt|;
block|}
return|return
name|result
return|;
block|}
block|}
end_class

end_unit

