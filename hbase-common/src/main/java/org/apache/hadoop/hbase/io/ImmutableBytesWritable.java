begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|io
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
name|util
operator|.
name|Arrays
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
name|io
operator|.
name|BytesWritable
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
name|WritableComparator
import|;
end_import

begin_comment
comment|/**  * A byte sequence that is usable as a key or value.  Based on  * {@link org.apache.hadoop.io.BytesWritable} only this class is NOT resizable  * and DOES NOT distinguish between the size of the sequence and the current  * capacity as {@link org.apache.hadoop.io.BytesWritable} does. Hence its  * comparatively 'immutable'. When creating a new instance of this class,  * the underlying byte [] is not copied, just referenced.  The backing  * buffer is accessed when we go to serialize.  */
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
annotation|@
name|edu
operator|.
name|umd
operator|.
name|cs
operator|.
name|findbugs
operator|.
name|annotations
operator|.
name|SuppressWarnings
argument_list|(
name|value
operator|=
literal|"EQ_CHECK_FOR_OPERAND_NOT_COMPATIBLE_WITH_THIS"
argument_list|,
name|justification
operator|=
literal|"It has been like this forever"
argument_list|)
specifier|public
class|class
name|ImmutableBytesWritable
implements|implements
name|WritableComparable
argument_list|<
name|ImmutableBytesWritable
argument_list|>
block|{
specifier|private
name|byte
index|[]
name|bytes
decl_stmt|;
specifier|private
name|int
name|offset
decl_stmt|;
specifier|private
name|int
name|length
decl_stmt|;
comment|/**    * Create a zero-size sequence.    */
specifier|public
name|ImmutableBytesWritable
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
block|}
comment|/**    * Create a ImmutableBytesWritable using the byte array as the initial value.    * @param bytes This array becomes the backing storage for the object.    */
specifier|public
name|ImmutableBytesWritable
parameter_list|(
name|byte
index|[]
name|bytes
parameter_list|)
block|{
name|this
argument_list|(
name|bytes
argument_list|,
literal|0
argument_list|,
name|bytes
operator|.
name|length
argument_list|)
expr_stmt|;
block|}
comment|/**    * Set the new ImmutableBytesWritable to the contents of the passed    *<code>ibw</code>.    * @param ibw the value to set this ImmutableBytesWritable to.    */
specifier|public
name|ImmutableBytesWritable
parameter_list|(
specifier|final
name|ImmutableBytesWritable
name|ibw
parameter_list|)
block|{
name|this
argument_list|(
name|ibw
operator|.
name|get
argument_list|()
argument_list|,
name|ibw
operator|.
name|getOffset
argument_list|()
argument_list|,
name|ibw
operator|.
name|getLength
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**    * Set the value to a given byte range    * @param bytes the new byte range to set to    * @param offset the offset in newData to start at    * @param length the number of bytes in the range    */
specifier|public
name|ImmutableBytesWritable
parameter_list|(
specifier|final
name|byte
index|[]
name|bytes
parameter_list|,
specifier|final
name|int
name|offset
parameter_list|,
specifier|final
name|int
name|length
parameter_list|)
block|{
name|this
operator|.
name|bytes
operator|=
name|bytes
expr_stmt|;
name|this
operator|.
name|offset
operator|=
name|offset
expr_stmt|;
name|this
operator|.
name|length
operator|=
name|length
expr_stmt|;
block|}
comment|/**    * Get the data from the BytesWritable.    * @return The data is only valid between offset and offset+length.    */
specifier|public
name|byte
index|[]
name|get
parameter_list|()
block|{
if|if
condition|(
name|this
operator|.
name|bytes
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"Uninitialiized. Null constructor "
operator|+
literal|"called w/o accompaying readFields invocation"
argument_list|)
throw|;
block|}
return|return
name|this
operator|.
name|bytes
return|;
block|}
comment|/**    * @param b Use passed bytes as backing array for this instance.    */
specifier|public
name|void
name|set
parameter_list|(
specifier|final
name|byte
index|[]
name|b
parameter_list|)
block|{
name|set
argument_list|(
name|b
argument_list|,
literal|0
argument_list|,
name|b
operator|.
name|length
argument_list|)
expr_stmt|;
block|}
comment|/**    * @param b Use passed bytes as backing array for this instance.    * @param offset    * @param length    */
specifier|public
name|void
name|set
parameter_list|(
specifier|final
name|byte
index|[]
name|b
parameter_list|,
specifier|final
name|int
name|offset
parameter_list|,
specifier|final
name|int
name|length
parameter_list|)
block|{
name|this
operator|.
name|bytes
operator|=
name|b
expr_stmt|;
name|this
operator|.
name|offset
operator|=
name|offset
expr_stmt|;
name|this
operator|.
name|length
operator|=
name|length
expr_stmt|;
block|}
comment|/**    * @return the number of valid bytes in the buffer    * @deprecated use {@link #getLength()} instead    */
annotation|@
name|Deprecated
specifier|public
name|int
name|getSize
parameter_list|()
block|{
if|if
condition|(
name|this
operator|.
name|bytes
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"Uninitialiized. Null constructor "
operator|+
literal|"called w/o accompaying readFields invocation"
argument_list|)
throw|;
block|}
return|return
name|this
operator|.
name|length
return|;
block|}
comment|/**    * @return the number of valid bytes in the buffer    */
specifier|public
name|int
name|getLength
parameter_list|()
block|{
if|if
condition|(
name|this
operator|.
name|bytes
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"Uninitialiized. Null constructor "
operator|+
literal|"called w/o accompaying readFields invocation"
argument_list|)
throw|;
block|}
return|return
name|this
operator|.
name|length
return|;
block|}
comment|/**    * @return offset    */
specifier|public
name|int
name|getOffset
parameter_list|()
block|{
return|return
name|this
operator|.
name|offset
return|;
block|}
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
name|this
operator|.
name|length
operator|=
name|in
operator|.
name|readInt
argument_list|()
expr_stmt|;
name|this
operator|.
name|bytes
operator|=
operator|new
name|byte
index|[
name|this
operator|.
name|length
index|]
expr_stmt|;
name|in
operator|.
name|readFully
argument_list|(
name|this
operator|.
name|bytes
argument_list|,
literal|0
argument_list|,
name|this
operator|.
name|length
argument_list|)
expr_stmt|;
name|this
operator|.
name|offset
operator|=
literal|0
expr_stmt|;
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
name|writeInt
argument_list|(
name|this
operator|.
name|length
argument_list|)
expr_stmt|;
name|out
operator|.
name|write
argument_list|(
name|this
operator|.
name|bytes
argument_list|,
name|this
operator|.
name|offset
argument_list|,
name|this
operator|.
name|length
argument_list|)
expr_stmt|;
block|}
comment|// Below methods copied from BytesWritable
annotation|@
name|Override
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
name|int
name|hash
init|=
literal|1
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
name|offset
init|;
name|i
operator|<
name|offset
operator|+
name|length
condition|;
name|i
operator|++
control|)
name|hash
operator|=
operator|(
literal|31
operator|*
name|hash
operator|)
operator|+
operator|(
name|int
operator|)
name|bytes
index|[
name|i
index|]
expr_stmt|;
return|return
name|hash
return|;
block|}
comment|/**    * Define the sort order of the BytesWritable.    * @param that The other bytes writable    * @return Positive if left is bigger than right, 0 if they are equal, and    *         negative if left is smaller than right.    */
specifier|public
name|int
name|compareTo
parameter_list|(
name|ImmutableBytesWritable
name|that
parameter_list|)
block|{
return|return
name|WritableComparator
operator|.
name|compareBytes
argument_list|(
name|this
operator|.
name|bytes
argument_list|,
name|this
operator|.
name|offset
argument_list|,
name|this
operator|.
name|length
argument_list|,
name|that
operator|.
name|bytes
argument_list|,
name|that
operator|.
name|offset
argument_list|,
name|that
operator|.
name|length
argument_list|)
return|;
block|}
comment|/**    * Compares the bytes in this object to the specified byte array    * @param that    * @return Positive if left is bigger than right, 0 if they are equal, and    *         negative if left is smaller than right.    */
specifier|public
name|int
name|compareTo
parameter_list|(
specifier|final
name|byte
index|[]
name|that
parameter_list|)
block|{
return|return
name|WritableComparator
operator|.
name|compareBytes
argument_list|(
name|this
operator|.
name|bytes
argument_list|,
name|this
operator|.
name|offset
argument_list|,
name|this
operator|.
name|length
argument_list|,
name|that
argument_list|,
literal|0
argument_list|,
name|that
operator|.
name|length
argument_list|)
return|;
block|}
comment|/**    * @see java.lang.Object#equals(java.lang.Object)    */
annotation|@
name|Override
specifier|public
name|boolean
name|equals
parameter_list|(
name|Object
name|right_obj
parameter_list|)
block|{
if|if
condition|(
name|right_obj
operator|instanceof
name|byte
index|[]
condition|)
block|{
return|return
name|compareTo
argument_list|(
operator|(
name|byte
index|[]
operator|)
name|right_obj
argument_list|)
operator|==
literal|0
return|;
block|}
if|if
condition|(
name|right_obj
operator|instanceof
name|ImmutableBytesWritable
condition|)
block|{
return|return
name|compareTo
argument_list|(
operator|(
name|ImmutableBytesWritable
operator|)
name|right_obj
argument_list|)
operator|==
literal|0
return|;
block|}
return|return
literal|false
return|;
block|}
comment|/**    * @see java.lang.Object#toString()    */
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
argument_list|(
literal|3
operator|*
name|this
operator|.
name|length
argument_list|)
decl_stmt|;
specifier|final
name|int
name|endIdx
init|=
name|this
operator|.
name|offset
operator|+
name|this
operator|.
name|length
decl_stmt|;
for|for
control|(
name|int
name|idx
init|=
name|this
operator|.
name|offset
init|;
name|idx
operator|<
name|endIdx
condition|;
name|idx
operator|++
control|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|' '
argument_list|)
expr_stmt|;
name|String
name|num
init|=
name|Integer
operator|.
name|toHexString
argument_list|(
literal|0xff
operator|&
name|this
operator|.
name|bytes
index|[
name|idx
index|]
argument_list|)
decl_stmt|;
comment|// if it is only one digit, add a leading 0.
if|if
condition|(
name|num
operator|.
name|length
argument_list|()
operator|<
literal|2
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|'0'
argument_list|)
expr_stmt|;
block|}
name|sb
operator|.
name|append
argument_list|(
name|num
argument_list|)
expr_stmt|;
block|}
return|return
name|sb
operator|.
name|length
argument_list|()
operator|>
literal|0
condition|?
name|sb
operator|.
name|substring
argument_list|(
literal|1
argument_list|)
else|:
literal|""
return|;
block|}
comment|/** A Comparator optimized for ImmutableBytesWritable.    */
specifier|public
specifier|static
class|class
name|Comparator
extends|extends
name|WritableComparator
block|{
specifier|private
name|BytesWritable
operator|.
name|Comparator
name|comparator
init|=
operator|new
name|BytesWritable
operator|.
name|Comparator
argument_list|()
decl_stmt|;
comment|/** constructor */
specifier|public
name|Comparator
parameter_list|()
block|{
name|super
argument_list|(
name|ImmutableBytesWritable
operator|.
name|class
argument_list|)
expr_stmt|;
block|}
comment|/**      * @see org.apache.hadoop.io.WritableComparator#compare(byte[], int, int, byte[], int, int)      */
annotation|@
name|Override
specifier|public
name|int
name|compare
parameter_list|(
name|byte
index|[]
name|b1
parameter_list|,
name|int
name|s1
parameter_list|,
name|int
name|l1
parameter_list|,
name|byte
index|[]
name|b2
parameter_list|,
name|int
name|s2
parameter_list|,
name|int
name|l2
parameter_list|)
block|{
return|return
name|comparator
operator|.
name|compare
argument_list|(
name|b1
argument_list|,
name|s1
argument_list|,
name|l1
argument_list|,
name|b2
argument_list|,
name|s2
argument_list|,
name|l2
argument_list|)
return|;
block|}
block|}
static|static
block|{
comment|// register this comparator
name|WritableComparator
operator|.
name|define
argument_list|(
name|ImmutableBytesWritable
operator|.
name|class
argument_list|,
operator|new
name|Comparator
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**    * @param array List of byte [].    * @return Array of byte [].    */
specifier|public
specifier|static
name|byte
index|[]
index|[]
name|toArray
parameter_list|(
specifier|final
name|List
argument_list|<
name|byte
index|[]
argument_list|>
name|array
parameter_list|)
block|{
comment|// List#toArray doesn't work on lists of byte [].
name|byte
index|[]
index|[]
name|results
init|=
operator|new
name|byte
index|[
name|array
operator|.
name|size
argument_list|()
index|]
index|[]
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
name|array
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|results
index|[
name|i
index|]
operator|=
name|array
operator|.
name|get
argument_list|(
name|i
argument_list|)
expr_stmt|;
block|}
return|return
name|results
return|;
block|}
comment|/**    * Returns a copy of the bytes referred to by this writable    */
specifier|public
name|byte
index|[]
name|copyBytes
parameter_list|()
block|{
return|return
name|Arrays
operator|.
name|copyOfRange
argument_list|(
name|bytes
argument_list|,
name|offset
argument_list|,
name|offset
operator|+
name|length
argument_list|)
return|;
block|}
block|}
end_class

end_unit

