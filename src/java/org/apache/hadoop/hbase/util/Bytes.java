begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
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
name|util
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
name|io
operator|.
name|UnsupportedEncodingException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|ByteBuffer
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Comparator
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
name|io
operator|.
name|RawComparator
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
name|WritableUtils
import|;
end_import

begin_comment
comment|/**  * Utility class that handles byte arrays, conversions to/from other types,  * comparisons, hash code generation, manufacturing keys for HashMaps or  * HashSets, etc.  */
end_comment

begin_class
specifier|public
class|class
name|Bytes
block|{
comment|/**    * Size of long in bytes    */
specifier|public
specifier|static
specifier|final
name|int
name|SIZEOF_LONG
init|=
name|Long
operator|.
name|SIZE
operator|/
name|Byte
operator|.
name|SIZE
decl_stmt|;
comment|/**    * Size of int in bytes    */
specifier|public
specifier|static
specifier|final
name|int
name|SIZEOF_INT
init|=
name|Integer
operator|.
name|SIZE
operator|/
name|Byte
operator|.
name|SIZE
decl_stmt|;
comment|/**    * Size of float in bytes    */
specifier|public
specifier|static
specifier|final
name|int
name|SIZEOF_FLOAT
init|=
name|Float
operator|.
name|SIZE
operator|/
name|Byte
operator|.
name|SIZE
decl_stmt|;
comment|/**    * Size of double in bytes    */
specifier|public
specifier|static
specifier|final
name|int
name|SIZEOF_DOUBLE
init|=
name|Double
operator|.
name|SIZE
operator|/
name|Byte
operator|.
name|SIZE
decl_stmt|;
comment|/**    * Estimate of size cost to pay beyond payload in jvm for instance of byte [].    * Estimate based on study of jhat and jprofiler numbers.    */
comment|// JHat says BU is 56 bytes.
specifier|public
specifier|static
specifier|final
name|int
name|ESTIMATED_HEAP_TAX
init|=
literal|16
decl_stmt|;
comment|/**    * Byte array comparator class.    * Does byte ordering.    */
specifier|public
specifier|static
class|class
name|ByteArrayComparator
implements|implements
name|RawComparator
argument_list|<
name|byte
index|[]
argument_list|>
block|{
specifier|public
name|ByteArrayComparator
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|compare
parameter_list|(
name|byte
index|[]
name|left
parameter_list|,
name|byte
index|[]
name|right
parameter_list|)
block|{
return|return
name|compareTo
argument_list|(
name|left
argument_list|,
name|right
argument_list|)
return|;
block|}
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
name|compareTo
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
comment|/**    * Pass this to TreeMaps where byte [] are keys.    */
specifier|public
specifier|static
name|Comparator
argument_list|<
name|byte
index|[]
argument_list|>
name|BYTES_COMPARATOR
init|=
operator|new
name|ByteArrayComparator
argument_list|()
decl_stmt|;
comment|/**    * Pass this to TreeMaps where byte [] are keys.    */
specifier|public
specifier|static
name|RawComparator
argument_list|<
name|byte
index|[]
argument_list|>
name|BYTES_RAWCOMPARATOR
init|=
operator|new
name|ByteArrayComparator
argument_list|()
decl_stmt|;
comment|/**    * @param in Input to read from.    * @return byte array read off<code>in</code>    * @throws IOException     */
specifier|public
specifier|static
name|byte
index|[]
name|readByteArray
parameter_list|(
specifier|final
name|DataInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|int
name|len
init|=
name|WritableUtils
operator|.
name|readVInt
argument_list|(
name|in
argument_list|)
decl_stmt|;
if|if
condition|(
name|len
operator|<
literal|0
condition|)
block|{
throw|throw
operator|new
name|NegativeArraySizeException
argument_list|(
name|Integer
operator|.
name|toString
argument_list|(
name|len
argument_list|)
argument_list|)
throw|;
block|}
name|byte
index|[]
name|result
init|=
operator|new
name|byte
index|[
name|len
index|]
decl_stmt|;
name|in
operator|.
name|readFully
argument_list|(
name|result
argument_list|,
literal|0
argument_list|,
name|len
argument_list|)
expr_stmt|;
return|return
name|result
return|;
block|}
comment|/**    * @param in Input to read from.    * @return byte array read off<code>in</code>    */
specifier|public
specifier|static
name|byte
index|[]
name|readByteArrayThrowsRuntime
parameter_list|(
specifier|final
name|DataInput
name|in
parameter_list|)
block|{
try|try
block|{
return|return
name|readByteArray
argument_list|(
name|in
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
comment|/**    * @param out    * @param b    * @throws IOException    */
specifier|public
specifier|static
name|void
name|writeByteArray
parameter_list|(
specifier|final
name|DataOutput
name|out
parameter_list|,
specifier|final
name|byte
index|[]
name|b
parameter_list|)
throws|throws
name|IOException
block|{
name|WritableUtils
operator|.
name|writeVInt
argument_list|(
name|out
argument_list|,
name|b
operator|.
name|length
argument_list|)
expr_stmt|;
name|out
operator|.
name|write
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
comment|/**    * Reads a zero-compressed encoded long from input stream and returns it.    * @param buffer Binary array    * @param offset Offset into array at which vint begins.    * @throws java.io.IOException     * @return deserialized long from stream.    */
specifier|public
specifier|static
name|long
name|readVLong
parameter_list|(
specifier|final
name|byte
index|[]
name|buffer
parameter_list|,
specifier|final
name|int
name|offset
parameter_list|)
throws|throws
name|IOException
block|{
name|byte
name|firstByte
init|=
name|buffer
index|[
name|offset
index|]
decl_stmt|;
name|int
name|len
init|=
name|WritableUtils
operator|.
name|decodeVIntSize
argument_list|(
name|firstByte
argument_list|)
decl_stmt|;
if|if
condition|(
name|len
operator|==
literal|1
condition|)
block|{
return|return
name|firstByte
return|;
block|}
name|long
name|i
init|=
literal|0
decl_stmt|;
for|for
control|(
name|int
name|idx
init|=
literal|0
init|;
name|idx
operator|<
name|len
operator|-
literal|1
condition|;
name|idx
operator|++
control|)
block|{
name|byte
name|b
init|=
name|buffer
index|[
name|offset
operator|+
literal|1
operator|+
name|idx
index|]
decl_stmt|;
name|i
operator|=
name|i
operator|<<
literal|8
expr_stmt|;
name|i
operator|=
name|i
operator||
operator|(
name|b
operator|&
literal|0xFF
operator|)
expr_stmt|;
block|}
return|return
operator|(
name|WritableUtils
operator|.
name|isNegativeVInt
argument_list|(
name|firstByte
argument_list|)
condition|?
operator|(
name|i
operator|^
operator|-
literal|1L
operator|)
else|:
name|i
operator|)
return|;
block|}
comment|/**    * @param b Presumed UTF-8 encoded byte array.    * @return String made from<code>b</code>    */
specifier|public
specifier|static
name|String
name|toString
parameter_list|(
specifier|final
name|byte
index|[]
name|b
parameter_list|)
block|{
return|return
name|toString
argument_list|(
name|b
argument_list|,
literal|0
argument_list|,
name|b
operator|.
name|length
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|String
name|toString
parameter_list|(
specifier|final
name|byte
index|[]
name|b
parameter_list|,
name|int
name|off
parameter_list|,
name|int
name|len
parameter_list|)
block|{
name|String
name|result
init|=
literal|null
decl_stmt|;
try|try
block|{
name|result
operator|=
operator|new
name|String
argument_list|(
name|b
argument_list|,
name|off
argument_list|,
name|len
argument_list|,
name|HConstants
operator|.
name|UTF8_ENCODING
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|UnsupportedEncodingException
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
block|}
return|return
name|result
return|;
block|}
comment|/**    * @param b    * @return<code>b</code> encoded in a byte array.    */
specifier|public
specifier|static
name|byte
index|[]
name|toBytes
parameter_list|(
specifier|final
name|boolean
name|b
parameter_list|)
block|{
name|byte
index|[]
name|bb
init|=
operator|new
name|byte
index|[
literal|1
index|]
decl_stmt|;
name|bb
index|[
literal|0
index|]
operator|=
name|b
condition|?
operator|(
name|byte
operator|)
operator|-
literal|1
else|:
operator|(
name|byte
operator|)
literal|0
expr_stmt|;
return|return
name|bb
return|;
block|}
comment|/**    * @param b    * @return True or false.    */
specifier|public
specifier|static
name|boolean
name|toBoolean
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
operator|>
literal|1
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Array is wrong size"
argument_list|)
throw|;
block|}
return|return
name|b
index|[
literal|0
index|]
operator|!=
operator|(
name|byte
operator|)
literal|0
return|;
block|}
comment|/**    * Converts a string to a UTF-8 byte array.    * @param s    * @return the byte array    */
specifier|public
specifier|static
name|byte
index|[]
name|toBytes
parameter_list|(
name|String
name|s
parameter_list|)
block|{
if|if
condition|(
name|s
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"string cannot be null"
argument_list|)
throw|;
block|}
name|byte
index|[]
name|result
init|=
literal|null
decl_stmt|;
try|try
block|{
name|result
operator|=
name|s
operator|.
name|getBytes
argument_list|(
name|HConstants
operator|.
name|UTF8_ENCODING
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|UnsupportedEncodingException
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
block|}
return|return
name|result
return|;
block|}
comment|/**    * @param bb    * @return Byte array represented by passed<code>bb</code>    */
specifier|public
specifier|static
name|byte
index|[]
name|toBytes
parameter_list|(
specifier|final
name|ByteBuffer
name|bb
parameter_list|)
block|{
name|int
name|length
init|=
name|bb
operator|.
name|limit
argument_list|()
decl_stmt|;
name|byte
index|[]
name|result
init|=
operator|new
name|byte
index|[
name|length
index|]
decl_stmt|;
name|System
operator|.
name|arraycopy
argument_list|(
name|bb
operator|.
name|array
argument_list|()
argument_list|,
name|bb
operator|.
name|arrayOffset
argument_list|()
argument_list|,
name|result
argument_list|,
literal|0
argument_list|,
name|length
argument_list|)
expr_stmt|;
return|return
name|result
return|;
block|}
comment|/**    * Convert a long value to a byte array    * @param val    * @return the byte array    */
specifier|public
specifier|static
name|byte
index|[]
name|toBytes
parameter_list|(
specifier|final
name|long
name|val
parameter_list|)
block|{
name|ByteBuffer
name|bb
init|=
name|ByteBuffer
operator|.
name|allocate
argument_list|(
name|SIZEOF_LONG
argument_list|)
decl_stmt|;
name|bb
operator|.
name|putLong
argument_list|(
name|val
argument_list|)
expr_stmt|;
return|return
name|bb
operator|.
name|array
argument_list|()
return|;
block|}
comment|/**    * Converts a byte array to a long value    * @param bytes    * @return the long value    */
specifier|public
specifier|static
name|long
name|toLong
parameter_list|(
name|byte
index|[]
name|bytes
parameter_list|)
block|{
if|if
condition|(
name|bytes
operator|==
literal|null
operator|||
name|bytes
operator|.
name|length
operator|==
literal|0
condition|)
block|{
return|return
operator|-
literal|1L
return|;
block|}
return|return
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|bytes
argument_list|)
operator|.
name|getLong
argument_list|()
return|;
block|}
comment|/**    * Convert an int value to a byte array    * @param val    * @return the byte array    */
specifier|public
specifier|static
name|byte
index|[]
name|toBytes
parameter_list|(
specifier|final
name|int
name|val
parameter_list|)
block|{
name|ByteBuffer
name|bb
init|=
name|ByteBuffer
operator|.
name|allocate
argument_list|(
name|SIZEOF_INT
argument_list|)
decl_stmt|;
name|bb
operator|.
name|putInt
argument_list|(
name|val
argument_list|)
expr_stmt|;
return|return
name|bb
operator|.
name|array
argument_list|()
return|;
block|}
comment|/**    * Converts a byte array to a long value    * @param bytes    * @return the long value    */
specifier|public
specifier|static
name|int
name|toInt
parameter_list|(
name|byte
index|[]
name|bytes
parameter_list|)
block|{
if|if
condition|(
name|bytes
operator|==
literal|null
operator|||
name|bytes
operator|.
name|length
operator|==
literal|0
condition|)
block|{
return|return
operator|-
literal|1
return|;
block|}
return|return
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|bytes
argument_list|)
operator|.
name|getInt
argument_list|()
return|;
block|}
comment|/**    * Convert an float value to a byte array    * @param val    * @return the byte array    */
specifier|public
specifier|static
name|byte
index|[]
name|toBytes
parameter_list|(
specifier|final
name|float
name|val
parameter_list|)
block|{
name|ByteBuffer
name|bb
init|=
name|ByteBuffer
operator|.
name|allocate
argument_list|(
name|SIZEOF_FLOAT
argument_list|)
decl_stmt|;
name|bb
operator|.
name|putFloat
argument_list|(
name|val
argument_list|)
expr_stmt|;
return|return
name|bb
operator|.
name|array
argument_list|()
return|;
block|}
comment|/**    * Converts a byte array to a float value    * @param bytes    * @return the float value    */
specifier|public
specifier|static
name|float
name|toFloat
parameter_list|(
name|byte
index|[]
name|bytes
parameter_list|)
block|{
if|if
condition|(
name|bytes
operator|==
literal|null
operator|||
name|bytes
operator|.
name|length
operator|==
literal|0
condition|)
block|{
return|return
operator|-
literal|1
return|;
block|}
return|return
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|bytes
argument_list|)
operator|.
name|getFloat
argument_list|()
return|;
block|}
comment|/**    * Convert an double value to a byte array    * @param val    * @return the byte array    */
specifier|public
specifier|static
name|byte
index|[]
name|toBytes
parameter_list|(
specifier|final
name|double
name|val
parameter_list|)
block|{
name|ByteBuffer
name|bb
init|=
name|ByteBuffer
operator|.
name|allocate
argument_list|(
name|SIZEOF_DOUBLE
argument_list|)
decl_stmt|;
name|bb
operator|.
name|putDouble
argument_list|(
name|val
argument_list|)
expr_stmt|;
return|return
name|bb
operator|.
name|array
argument_list|()
return|;
block|}
comment|/**    * Converts a byte array to a double value    * @param bytes    * @return the double value    */
specifier|public
specifier|static
name|double
name|toDouble
parameter_list|(
name|byte
index|[]
name|bytes
parameter_list|)
block|{
if|if
condition|(
name|bytes
operator|==
literal|null
operator|||
name|bytes
operator|.
name|length
operator|==
literal|0
condition|)
block|{
return|return
operator|-
literal|1
return|;
block|}
return|return
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|bytes
argument_list|)
operator|.
name|getDouble
argument_list|()
return|;
block|}
comment|/**    * @param left    * @param right    * @return 0 if equal,< 0 if left is less than right, etc.    */
specifier|public
specifier|static
name|int
name|compareTo
parameter_list|(
specifier|final
name|byte
index|[]
name|left
parameter_list|,
specifier|final
name|byte
index|[]
name|right
parameter_list|)
block|{
return|return
name|compareTo
argument_list|(
name|left
argument_list|,
literal|0
argument_list|,
name|left
operator|.
name|length
argument_list|,
name|right
argument_list|,
literal|0
argument_list|,
name|right
operator|.
name|length
argument_list|)
return|;
block|}
comment|/**    * @param left    * @param right    * @param leftOffset Where to start comparing in the left buffer    * @param rightOffset Where to start comparing in the right buffer    * @param leftLength How much to compare from the left buffer    * @param rightLength How much to compare from the right buffer    * @return 0 if equal,< 0 if left is less than right, etc.    */
specifier|public
specifier|static
name|int
name|compareTo
parameter_list|(
specifier|final
name|byte
index|[]
name|left
parameter_list|,
specifier|final
name|int
name|leftOffset
parameter_list|,
specifier|final
name|int
name|leftLength
parameter_list|,
specifier|final
name|byte
index|[]
name|right
parameter_list|,
specifier|final
name|int
name|rightOffset
parameter_list|,
specifier|final
name|int
name|rightLength
parameter_list|)
block|{
return|return
name|WritableComparator
operator|.
name|compareBytes
argument_list|(
name|left
argument_list|,
name|leftOffset
argument_list|,
name|leftLength
argument_list|,
name|right
argument_list|,
name|rightOffset
argument_list|,
name|rightLength
argument_list|)
return|;
block|}
comment|/**    * @param left    * @param right    * @return True if equal    */
specifier|public
specifier|static
name|boolean
name|equals
parameter_list|(
specifier|final
name|byte
index|[]
name|left
parameter_list|,
specifier|final
name|byte
index|[]
name|right
parameter_list|)
block|{
comment|// Could use Arrays.equals?
return|return
name|left
operator|==
literal|null
operator|&&
name|right
operator|==
literal|null
condition|?
literal|true
else|:
operator|(
name|left
operator|==
literal|null
operator|||
name|right
operator|==
literal|null
operator|||
operator|(
name|left
operator|.
name|length
operator|!=
name|right
operator|.
name|length
operator|)
operator|)
condition|?
literal|false
else|:
name|compareTo
argument_list|(
name|left
argument_list|,
name|right
argument_list|)
operator|==
literal|0
return|;
block|}
comment|/**    * @param b    * @return Runs {@link WritableComparator#hashBytes(byte[], int)} on the    * passed in array.  This method is what {@link org.apache.hadoop.io.Text} and    * {@link ImmutableBytesWritable} use calculating hash code.    */
specifier|public
specifier|static
name|int
name|hashCode
parameter_list|(
specifier|final
name|byte
index|[]
name|b
parameter_list|)
block|{
return|return
name|hashCode
argument_list|(
name|b
argument_list|,
name|b
operator|.
name|length
argument_list|)
return|;
block|}
comment|/**    * @param b    * @param length    * @return Runs {@link WritableComparator#hashBytes(byte[], int)} on the    * passed in array.  This method is what {@link org.apache.hadoop.io.Text} and    * {@link ImmutableBytesWritable} use calculating hash code.    */
specifier|public
specifier|static
name|int
name|hashCode
parameter_list|(
specifier|final
name|byte
index|[]
name|b
parameter_list|,
specifier|final
name|int
name|length
parameter_list|)
block|{
return|return
name|WritableComparator
operator|.
name|hashBytes
argument_list|(
name|b
argument_list|,
name|length
argument_list|)
return|;
block|}
comment|/**    * @param b    * @return A hash of<code>b</code> as an Integer that can be used as key in    * Maps.    */
specifier|public
specifier|static
name|Integer
name|mapKey
parameter_list|(
specifier|final
name|byte
index|[]
name|b
parameter_list|)
block|{
return|return
name|Integer
operator|.
name|valueOf
argument_list|(
name|hashCode
argument_list|(
name|b
argument_list|)
argument_list|)
return|;
block|}
comment|/**    * @param b    * @param length    * @return A hash of<code>b</code> as an Integer that can be used as key in    * Maps.    */
specifier|public
specifier|static
name|Integer
name|mapKey
parameter_list|(
specifier|final
name|byte
index|[]
name|b
parameter_list|,
specifier|final
name|int
name|length
parameter_list|)
block|{
return|return
name|Integer
operator|.
name|valueOf
argument_list|(
name|hashCode
argument_list|(
name|b
argument_list|,
name|length
argument_list|)
argument_list|)
return|;
block|}
comment|/**    * @param a    * @param b    * @return New array that has a in lower half and b in upper half.    */
specifier|public
specifier|static
name|byte
index|[]
name|add
parameter_list|(
specifier|final
name|byte
index|[]
name|a
parameter_list|,
specifier|final
name|byte
index|[]
name|b
parameter_list|)
block|{
return|return
name|add
argument_list|(
name|a
argument_list|,
name|b
argument_list|,
name|HConstants
operator|.
name|EMPTY_BYTE_ARRAY
argument_list|)
return|;
block|}
comment|/**    * @param a    * @param b    * @param c    * @return New array made from a, b and c    */
specifier|public
specifier|static
name|byte
index|[]
name|add
parameter_list|(
specifier|final
name|byte
index|[]
name|a
parameter_list|,
specifier|final
name|byte
index|[]
name|b
parameter_list|,
specifier|final
name|byte
index|[]
name|c
parameter_list|)
block|{
name|byte
index|[]
name|result
init|=
operator|new
name|byte
index|[
name|a
operator|.
name|length
operator|+
name|b
operator|.
name|length
operator|+
name|c
operator|.
name|length
index|]
decl_stmt|;
name|System
operator|.
name|arraycopy
argument_list|(
name|a
argument_list|,
literal|0
argument_list|,
name|result
argument_list|,
literal|0
argument_list|,
name|a
operator|.
name|length
argument_list|)
expr_stmt|;
name|System
operator|.
name|arraycopy
argument_list|(
name|b
argument_list|,
literal|0
argument_list|,
name|result
argument_list|,
name|a
operator|.
name|length
argument_list|,
name|b
operator|.
name|length
argument_list|)
expr_stmt|;
name|System
operator|.
name|arraycopy
argument_list|(
name|c
argument_list|,
literal|0
argument_list|,
name|result
argument_list|,
name|a
operator|.
name|length
operator|+
name|b
operator|.
name|length
argument_list|,
name|c
operator|.
name|length
argument_list|)
expr_stmt|;
return|return
name|result
return|;
block|}
comment|/**    * @param t    * @return Array of byte arrays made from passed array of Text    */
specifier|public
specifier|static
name|byte
index|[]
index|[]
name|toByteArrays
parameter_list|(
specifier|final
name|String
index|[]
name|t
parameter_list|)
block|{
name|byte
index|[]
index|[]
name|result
init|=
operator|new
name|byte
index|[
name|t
operator|.
name|length
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
name|t
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|result
index|[
name|i
index|]
operator|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|t
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
return|return
name|result
return|;
block|}
comment|/**    * @param column    * @return A byte array of a byte array where first and only entry is    *<code>column</code>    */
specifier|public
specifier|static
name|byte
index|[]
index|[]
name|toByteArrays
parameter_list|(
specifier|final
name|String
name|column
parameter_list|)
block|{
return|return
name|toByteArrays
argument_list|(
name|toBytes
argument_list|(
name|column
argument_list|)
argument_list|)
return|;
block|}
comment|/**    * @param column    * @return A byte array of a byte array where first and only entry is    *<code>column</code>    */
specifier|public
specifier|static
name|byte
index|[]
index|[]
name|toByteArrays
parameter_list|(
specifier|final
name|byte
index|[]
name|column
parameter_list|)
block|{
name|byte
index|[]
index|[]
name|result
init|=
operator|new
name|byte
index|[
literal|1
index|]
index|[]
decl_stmt|;
name|result
index|[
literal|0
index|]
operator|=
name|column
expr_stmt|;
return|return
name|result
return|;
block|}
block|}
end_class

end_unit

