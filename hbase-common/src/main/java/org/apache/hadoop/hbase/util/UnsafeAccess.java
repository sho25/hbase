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
name|util
package|;
end_package

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|reflect
operator|.
name|Field
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
name|nio
operator|.
name|ByteOrder
import|;
end_import

begin_import
import|import
name|java
operator|.
name|security
operator|.
name|AccessController
import|;
end_import

begin_import
import|import
name|java
operator|.
name|security
operator|.
name|PrivilegedAction
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|yetus
operator|.
name|audience
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
name|yetus
operator|.
name|audience
operator|.
name|InterfaceStability
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|LoggerFactory
import|;
end_import

begin_import
import|import
name|sun
operator|.
name|misc
operator|.
name|Unsafe
import|;
end_import

begin_import
import|import
name|sun
operator|.
name|nio
operator|.
name|ch
operator|.
name|DirectBuffer
import|;
end_import

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
annotation|@
name|InterfaceStability
operator|.
name|Evolving
specifier|public
specifier|final
class|class
name|UnsafeAccess
block|{
specifier|private
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|UnsafeAccess
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|Unsafe
name|theUnsafe
decl_stmt|;
comment|/** The offset to the first element in a byte array. */
specifier|public
specifier|static
specifier|final
name|long
name|BYTE_ARRAY_BASE_OFFSET
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|boolean
name|LITTLE_ENDIAN
init|=
name|ByteOrder
operator|.
name|nativeOrder
argument_list|()
operator|.
name|equals
argument_list|(
name|ByteOrder
operator|.
name|LITTLE_ENDIAN
argument_list|)
decl_stmt|;
comment|// This number limits the number of bytes to copy per call to Unsafe's
comment|// copyMemory method. A limit is imposed to allow for safepoint polling
comment|// during a large copy
specifier|static
specifier|final
name|long
name|UNSAFE_COPY_THRESHOLD
init|=
literal|1024L
operator|*
literal|1024L
decl_stmt|;
static|static
block|{
name|theUnsafe
operator|=
operator|(
name|Unsafe
operator|)
name|AccessController
operator|.
name|doPrivileged
argument_list|(
operator|new
name|PrivilegedAction
argument_list|<
name|Object
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Object
name|run
parameter_list|()
block|{
try|try
block|{
name|Field
name|f
init|=
name|Unsafe
operator|.
name|class
operator|.
name|getDeclaredField
argument_list|(
literal|"theUnsafe"
argument_list|)
decl_stmt|;
name|f
operator|.
name|setAccessible
argument_list|(
literal|true
argument_list|)
expr_stmt|;
return|return
name|f
operator|.
name|get
argument_list|(
literal|null
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"sun.misc.Unsafe is not accessible"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
return|return
literal|null
return|;
block|}
block|}
argument_list|)
expr_stmt|;
if|if
condition|(
name|theUnsafe
operator|!=
literal|null
condition|)
block|{
name|BYTE_ARRAY_BASE_OFFSET
operator|=
name|theUnsafe
operator|.
name|arrayBaseOffset
argument_list|(
name|byte
index|[]
operator|.
expr|class
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|BYTE_ARRAY_BASE_OFFSET
operator|=
operator|-
literal|1
expr_stmt|;
block|}
block|}
specifier|private
name|UnsafeAccess
parameter_list|()
block|{}
comment|// APIs to read primitive data from a byte[] using Unsafe way
comment|/**    * Converts a byte array to a short value considering it was written in big-endian format.    * @param bytes byte array    * @param offset offset into array    * @return the short value    */
specifier|public
specifier|static
name|short
name|toShort
parameter_list|(
name|byte
index|[]
name|bytes
parameter_list|,
name|int
name|offset
parameter_list|)
block|{
if|if
condition|(
name|LITTLE_ENDIAN
condition|)
block|{
return|return
name|Short
operator|.
name|reverseBytes
argument_list|(
name|theUnsafe
operator|.
name|getShort
argument_list|(
name|bytes
argument_list|,
name|offset
operator|+
name|BYTE_ARRAY_BASE_OFFSET
argument_list|)
argument_list|)
return|;
block|}
else|else
block|{
return|return
name|theUnsafe
operator|.
name|getShort
argument_list|(
name|bytes
argument_list|,
name|offset
operator|+
name|BYTE_ARRAY_BASE_OFFSET
argument_list|)
return|;
block|}
block|}
comment|/**    * Converts a byte array to an int value considering it was written in big-endian format.    * @param bytes byte array    * @param offset offset into array    * @return the int value    */
specifier|public
specifier|static
name|int
name|toInt
parameter_list|(
name|byte
index|[]
name|bytes
parameter_list|,
name|int
name|offset
parameter_list|)
block|{
if|if
condition|(
name|LITTLE_ENDIAN
condition|)
block|{
return|return
name|Integer
operator|.
name|reverseBytes
argument_list|(
name|theUnsafe
operator|.
name|getInt
argument_list|(
name|bytes
argument_list|,
name|offset
operator|+
name|BYTE_ARRAY_BASE_OFFSET
argument_list|)
argument_list|)
return|;
block|}
else|else
block|{
return|return
name|theUnsafe
operator|.
name|getInt
argument_list|(
name|bytes
argument_list|,
name|offset
operator|+
name|BYTE_ARRAY_BASE_OFFSET
argument_list|)
return|;
block|}
block|}
comment|/**    * Converts a byte array to a long value considering it was written in big-endian format.    * @param bytes byte array    * @param offset offset into array    * @return the long value    */
specifier|public
specifier|static
name|long
name|toLong
parameter_list|(
name|byte
index|[]
name|bytes
parameter_list|,
name|int
name|offset
parameter_list|)
block|{
if|if
condition|(
name|LITTLE_ENDIAN
condition|)
block|{
return|return
name|Long
operator|.
name|reverseBytes
argument_list|(
name|theUnsafe
operator|.
name|getLong
argument_list|(
name|bytes
argument_list|,
name|offset
operator|+
name|BYTE_ARRAY_BASE_OFFSET
argument_list|)
argument_list|)
return|;
block|}
else|else
block|{
return|return
name|theUnsafe
operator|.
name|getLong
argument_list|(
name|bytes
argument_list|,
name|offset
operator|+
name|BYTE_ARRAY_BASE_OFFSET
argument_list|)
return|;
block|}
block|}
comment|// APIs to write primitive data to a byte[] using Unsafe way
comment|/**    * Put a short value out to the specified byte array position in big-endian format.    * @param bytes the byte array    * @param offset position in the array    * @param val short to write out    * @return incremented offset    */
specifier|public
specifier|static
name|int
name|putShort
parameter_list|(
name|byte
index|[]
name|bytes
parameter_list|,
name|int
name|offset
parameter_list|,
name|short
name|val
parameter_list|)
block|{
if|if
condition|(
name|LITTLE_ENDIAN
condition|)
block|{
name|val
operator|=
name|Short
operator|.
name|reverseBytes
argument_list|(
name|val
argument_list|)
expr_stmt|;
block|}
name|theUnsafe
operator|.
name|putShort
argument_list|(
name|bytes
argument_list|,
name|offset
operator|+
name|BYTE_ARRAY_BASE_OFFSET
argument_list|,
name|val
argument_list|)
expr_stmt|;
return|return
name|offset
operator|+
name|Bytes
operator|.
name|SIZEOF_SHORT
return|;
block|}
comment|/**    * Put an int value out to the specified byte array position in big-endian format.    * @param bytes the byte array    * @param offset position in the array    * @param val int to write out    * @return incremented offset    */
specifier|public
specifier|static
name|int
name|putInt
parameter_list|(
name|byte
index|[]
name|bytes
parameter_list|,
name|int
name|offset
parameter_list|,
name|int
name|val
parameter_list|)
block|{
if|if
condition|(
name|LITTLE_ENDIAN
condition|)
block|{
name|val
operator|=
name|Integer
operator|.
name|reverseBytes
argument_list|(
name|val
argument_list|)
expr_stmt|;
block|}
name|theUnsafe
operator|.
name|putInt
argument_list|(
name|bytes
argument_list|,
name|offset
operator|+
name|BYTE_ARRAY_BASE_OFFSET
argument_list|,
name|val
argument_list|)
expr_stmt|;
return|return
name|offset
operator|+
name|Bytes
operator|.
name|SIZEOF_INT
return|;
block|}
comment|/**    * Put a long value out to the specified byte array position in big-endian format.    * @param bytes the byte array    * @param offset position in the array    * @param val long to write out    * @return incremented offset    */
specifier|public
specifier|static
name|int
name|putLong
parameter_list|(
name|byte
index|[]
name|bytes
parameter_list|,
name|int
name|offset
parameter_list|,
name|long
name|val
parameter_list|)
block|{
if|if
condition|(
name|LITTLE_ENDIAN
condition|)
block|{
name|val
operator|=
name|Long
operator|.
name|reverseBytes
argument_list|(
name|val
argument_list|)
expr_stmt|;
block|}
name|theUnsafe
operator|.
name|putLong
argument_list|(
name|bytes
argument_list|,
name|offset
operator|+
name|BYTE_ARRAY_BASE_OFFSET
argument_list|,
name|val
argument_list|)
expr_stmt|;
return|return
name|offset
operator|+
name|Bytes
operator|.
name|SIZEOF_LONG
return|;
block|}
comment|// APIs to read primitive data from a ByteBuffer using Unsafe way
comment|/**    * Reads a short value at the given buffer's offset considering it was written in big-endian    * format.    *    * @param buf    * @param offset    * @return short value at offset    */
specifier|public
specifier|static
name|short
name|toShort
parameter_list|(
name|ByteBuffer
name|buf
parameter_list|,
name|int
name|offset
parameter_list|)
block|{
if|if
condition|(
name|LITTLE_ENDIAN
condition|)
block|{
return|return
name|Short
operator|.
name|reverseBytes
argument_list|(
name|getAsShort
argument_list|(
name|buf
argument_list|,
name|offset
argument_list|)
argument_list|)
return|;
block|}
return|return
name|getAsShort
argument_list|(
name|buf
argument_list|,
name|offset
argument_list|)
return|;
block|}
comment|/**    * Reads a short value at the given Object's offset considering it was written in big-endian    * format.    * @param ref    * @param offset    * @return short value at offset    */
specifier|public
specifier|static
name|short
name|toShort
parameter_list|(
name|Object
name|ref
parameter_list|,
name|long
name|offset
parameter_list|)
block|{
if|if
condition|(
name|LITTLE_ENDIAN
condition|)
block|{
return|return
name|Short
operator|.
name|reverseBytes
argument_list|(
name|theUnsafe
operator|.
name|getShort
argument_list|(
name|ref
argument_list|,
name|offset
argument_list|)
argument_list|)
return|;
block|}
return|return
name|theUnsafe
operator|.
name|getShort
argument_list|(
name|ref
argument_list|,
name|offset
argument_list|)
return|;
block|}
comment|/**    * Reads bytes at the given offset as a short value.    * @param buf    * @param offset    * @return short value at offset    */
specifier|static
name|short
name|getAsShort
parameter_list|(
name|ByteBuffer
name|buf
parameter_list|,
name|int
name|offset
parameter_list|)
block|{
if|if
condition|(
name|buf
operator|.
name|isDirect
argument_list|()
condition|)
block|{
return|return
name|theUnsafe
operator|.
name|getShort
argument_list|(
operator|(
operator|(
name|DirectBuffer
operator|)
name|buf
operator|)
operator|.
name|address
argument_list|()
operator|+
name|offset
argument_list|)
return|;
block|}
return|return
name|theUnsafe
operator|.
name|getShort
argument_list|(
name|buf
operator|.
name|array
argument_list|()
argument_list|,
name|BYTE_ARRAY_BASE_OFFSET
operator|+
name|buf
operator|.
name|arrayOffset
argument_list|()
operator|+
name|offset
argument_list|)
return|;
block|}
comment|/**    * Reads an int value at the given buffer's offset considering it was written in big-endian    * format.    *    * @param buf    * @param offset    * @return int value at offset    */
specifier|public
specifier|static
name|int
name|toInt
parameter_list|(
name|ByteBuffer
name|buf
parameter_list|,
name|int
name|offset
parameter_list|)
block|{
if|if
condition|(
name|LITTLE_ENDIAN
condition|)
block|{
return|return
name|Integer
operator|.
name|reverseBytes
argument_list|(
name|getAsInt
argument_list|(
name|buf
argument_list|,
name|offset
argument_list|)
argument_list|)
return|;
block|}
return|return
name|getAsInt
argument_list|(
name|buf
argument_list|,
name|offset
argument_list|)
return|;
block|}
comment|/**    * Reads a int value at the given Object's offset considering it was written in big-endian    * format.    * @param ref    * @param offset    * @return int value at offset    */
specifier|public
specifier|static
name|int
name|toInt
parameter_list|(
name|Object
name|ref
parameter_list|,
name|long
name|offset
parameter_list|)
block|{
if|if
condition|(
name|LITTLE_ENDIAN
condition|)
block|{
return|return
name|Integer
operator|.
name|reverseBytes
argument_list|(
name|theUnsafe
operator|.
name|getInt
argument_list|(
name|ref
argument_list|,
name|offset
argument_list|)
argument_list|)
return|;
block|}
return|return
name|theUnsafe
operator|.
name|getInt
argument_list|(
name|ref
argument_list|,
name|offset
argument_list|)
return|;
block|}
comment|/**    * Reads bytes at the given offset as an int value.    * @param buf    * @param offset    * @return int value at offset    */
specifier|static
name|int
name|getAsInt
parameter_list|(
name|ByteBuffer
name|buf
parameter_list|,
name|int
name|offset
parameter_list|)
block|{
if|if
condition|(
name|buf
operator|.
name|isDirect
argument_list|()
condition|)
block|{
return|return
name|theUnsafe
operator|.
name|getInt
argument_list|(
operator|(
operator|(
name|DirectBuffer
operator|)
name|buf
operator|)
operator|.
name|address
argument_list|()
operator|+
name|offset
argument_list|)
return|;
block|}
return|return
name|theUnsafe
operator|.
name|getInt
argument_list|(
name|buf
operator|.
name|array
argument_list|()
argument_list|,
name|BYTE_ARRAY_BASE_OFFSET
operator|+
name|buf
operator|.
name|arrayOffset
argument_list|()
operator|+
name|offset
argument_list|)
return|;
block|}
comment|/**    * Reads a long value at the given buffer's offset considering it was written in big-endian    * format.    *    * @param buf    * @param offset    * @return long value at offset    */
specifier|public
specifier|static
name|long
name|toLong
parameter_list|(
name|ByteBuffer
name|buf
parameter_list|,
name|int
name|offset
parameter_list|)
block|{
if|if
condition|(
name|LITTLE_ENDIAN
condition|)
block|{
return|return
name|Long
operator|.
name|reverseBytes
argument_list|(
name|getAsLong
argument_list|(
name|buf
argument_list|,
name|offset
argument_list|)
argument_list|)
return|;
block|}
return|return
name|getAsLong
argument_list|(
name|buf
argument_list|,
name|offset
argument_list|)
return|;
block|}
comment|/**    * Reads a long value at the given Object's offset considering it was written in big-endian    * format.    * @param ref    * @param offset    * @return long value at offset    */
specifier|public
specifier|static
name|long
name|toLong
parameter_list|(
name|Object
name|ref
parameter_list|,
name|long
name|offset
parameter_list|)
block|{
if|if
condition|(
name|LITTLE_ENDIAN
condition|)
block|{
return|return
name|Long
operator|.
name|reverseBytes
argument_list|(
name|theUnsafe
operator|.
name|getLong
argument_list|(
name|ref
argument_list|,
name|offset
argument_list|)
argument_list|)
return|;
block|}
return|return
name|theUnsafe
operator|.
name|getLong
argument_list|(
name|ref
argument_list|,
name|offset
argument_list|)
return|;
block|}
comment|/**    * Reads bytes at the given offset as a long value.    * @param buf    * @param offset    * @return long value at offset    */
specifier|static
name|long
name|getAsLong
parameter_list|(
name|ByteBuffer
name|buf
parameter_list|,
name|int
name|offset
parameter_list|)
block|{
if|if
condition|(
name|buf
operator|.
name|isDirect
argument_list|()
condition|)
block|{
return|return
name|theUnsafe
operator|.
name|getLong
argument_list|(
operator|(
operator|(
name|DirectBuffer
operator|)
name|buf
operator|)
operator|.
name|address
argument_list|()
operator|+
name|offset
argument_list|)
return|;
block|}
return|return
name|theUnsafe
operator|.
name|getLong
argument_list|(
name|buf
operator|.
name|array
argument_list|()
argument_list|,
name|BYTE_ARRAY_BASE_OFFSET
operator|+
name|buf
operator|.
name|arrayOffset
argument_list|()
operator|+
name|offset
argument_list|)
return|;
block|}
comment|/**    * Put an int value out to the specified ByteBuffer offset in big-endian format.    * @param buf the ByteBuffer to write to    * @param offset offset in the ByteBuffer    * @param val int to write out    * @return incremented offset    */
specifier|public
specifier|static
name|int
name|putInt
parameter_list|(
name|ByteBuffer
name|buf
parameter_list|,
name|int
name|offset
parameter_list|,
name|int
name|val
parameter_list|)
block|{
if|if
condition|(
name|LITTLE_ENDIAN
condition|)
block|{
name|val
operator|=
name|Integer
operator|.
name|reverseBytes
argument_list|(
name|val
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|buf
operator|.
name|isDirect
argument_list|()
condition|)
block|{
name|theUnsafe
operator|.
name|putInt
argument_list|(
operator|(
operator|(
name|DirectBuffer
operator|)
name|buf
operator|)
operator|.
name|address
argument_list|()
operator|+
name|offset
argument_list|,
name|val
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|theUnsafe
operator|.
name|putInt
argument_list|(
name|buf
operator|.
name|array
argument_list|()
argument_list|,
name|offset
operator|+
name|buf
operator|.
name|arrayOffset
argument_list|()
operator|+
name|BYTE_ARRAY_BASE_OFFSET
argument_list|,
name|val
argument_list|)
expr_stmt|;
block|}
return|return
name|offset
operator|+
name|Bytes
operator|.
name|SIZEOF_INT
return|;
block|}
comment|// APIs to copy data. This will be direct memory location copy and will be much faster
comment|/**    * Copies the bytes from given array's offset to length part into the given buffer.    * @param src    * @param srcOffset    * @param dest    * @param destOffset    * @param length    */
specifier|public
specifier|static
name|void
name|copy
parameter_list|(
name|byte
index|[]
name|src
parameter_list|,
name|int
name|srcOffset
parameter_list|,
name|ByteBuffer
name|dest
parameter_list|,
name|int
name|destOffset
parameter_list|,
name|int
name|length
parameter_list|)
block|{
name|long
name|destAddress
init|=
name|destOffset
decl_stmt|;
name|Object
name|destBase
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|dest
operator|.
name|isDirect
argument_list|()
condition|)
block|{
name|destAddress
operator|=
name|destAddress
operator|+
operator|(
operator|(
name|DirectBuffer
operator|)
name|dest
operator|)
operator|.
name|address
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|destAddress
operator|=
name|destAddress
operator|+
name|BYTE_ARRAY_BASE_OFFSET
operator|+
name|dest
operator|.
name|arrayOffset
argument_list|()
expr_stmt|;
name|destBase
operator|=
name|dest
operator|.
name|array
argument_list|()
expr_stmt|;
block|}
name|long
name|srcAddress
init|=
name|srcOffset
operator|+
name|BYTE_ARRAY_BASE_OFFSET
decl_stmt|;
name|unsafeCopy
argument_list|(
name|src
argument_list|,
name|srcAddress
argument_list|,
name|destBase
argument_list|,
name|destAddress
argument_list|,
name|length
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|static
name|void
name|unsafeCopy
parameter_list|(
name|Object
name|src
parameter_list|,
name|long
name|srcAddr
parameter_list|,
name|Object
name|dst
parameter_list|,
name|long
name|destAddr
parameter_list|,
name|long
name|len
parameter_list|)
block|{
while|while
condition|(
name|len
operator|>
literal|0
condition|)
block|{
name|long
name|size
init|=
operator|(
name|len
operator|>
name|UNSAFE_COPY_THRESHOLD
operator|)
condition|?
name|UNSAFE_COPY_THRESHOLD
else|:
name|len
decl_stmt|;
name|theUnsafe
operator|.
name|copyMemory
argument_list|(
name|src
argument_list|,
name|srcAddr
argument_list|,
name|dst
argument_list|,
name|destAddr
argument_list|,
name|size
argument_list|)
expr_stmt|;
name|len
operator|-=
name|size
expr_stmt|;
name|srcAddr
operator|+=
name|size
expr_stmt|;
name|destAddr
operator|+=
name|size
expr_stmt|;
block|}
block|}
comment|/**    * Copies specified number of bytes from given offset of {@code src} ByteBuffer to the    * {@code dest} array.    *    * @param src    * @param srcOffset    * @param dest    * @param destOffset    * @param length    */
specifier|public
specifier|static
name|void
name|copy
parameter_list|(
name|ByteBuffer
name|src
parameter_list|,
name|int
name|srcOffset
parameter_list|,
name|byte
index|[]
name|dest
parameter_list|,
name|int
name|destOffset
parameter_list|,
name|int
name|length
parameter_list|)
block|{
name|long
name|srcAddress
init|=
name|srcOffset
decl_stmt|;
name|Object
name|srcBase
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|src
operator|.
name|isDirect
argument_list|()
condition|)
block|{
name|srcAddress
operator|=
name|srcAddress
operator|+
operator|(
operator|(
name|DirectBuffer
operator|)
name|src
operator|)
operator|.
name|address
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|srcAddress
operator|=
name|srcAddress
operator|+
name|BYTE_ARRAY_BASE_OFFSET
operator|+
name|src
operator|.
name|arrayOffset
argument_list|()
expr_stmt|;
name|srcBase
operator|=
name|src
operator|.
name|array
argument_list|()
expr_stmt|;
block|}
name|long
name|destAddress
init|=
name|destOffset
operator|+
name|BYTE_ARRAY_BASE_OFFSET
decl_stmt|;
name|unsafeCopy
argument_list|(
name|srcBase
argument_list|,
name|srcAddress
argument_list|,
name|dest
argument_list|,
name|destAddress
argument_list|,
name|length
argument_list|)
expr_stmt|;
block|}
comment|/**    * Copies specified number of bytes from given offset of {@code src} buffer into the {@code dest}    * buffer.    *    * @param src    * @param srcOffset    * @param dest    * @param destOffset    * @param length    */
specifier|public
specifier|static
name|void
name|copy
parameter_list|(
name|ByteBuffer
name|src
parameter_list|,
name|int
name|srcOffset
parameter_list|,
name|ByteBuffer
name|dest
parameter_list|,
name|int
name|destOffset
parameter_list|,
name|int
name|length
parameter_list|)
block|{
name|long
name|srcAddress
decl_stmt|,
name|destAddress
decl_stmt|;
name|Object
name|srcBase
init|=
literal|null
decl_stmt|,
name|destBase
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|src
operator|.
name|isDirect
argument_list|()
condition|)
block|{
name|srcAddress
operator|=
name|srcOffset
operator|+
operator|(
operator|(
name|DirectBuffer
operator|)
name|src
operator|)
operator|.
name|address
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|srcAddress
operator|=
operator|(
name|long
operator|)
name|srcOffset
operator|+
name|src
operator|.
name|arrayOffset
argument_list|()
operator|+
name|BYTE_ARRAY_BASE_OFFSET
expr_stmt|;
name|srcBase
operator|=
name|src
operator|.
name|array
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|dest
operator|.
name|isDirect
argument_list|()
condition|)
block|{
name|destAddress
operator|=
name|destOffset
operator|+
operator|(
operator|(
name|DirectBuffer
operator|)
name|dest
operator|)
operator|.
name|address
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|destAddress
operator|=
name|destOffset
operator|+
name|BYTE_ARRAY_BASE_OFFSET
operator|+
name|dest
operator|.
name|arrayOffset
argument_list|()
expr_stmt|;
name|destBase
operator|=
name|dest
operator|.
name|array
argument_list|()
expr_stmt|;
block|}
name|unsafeCopy
argument_list|(
name|srcBase
argument_list|,
name|srcAddress
argument_list|,
name|destBase
argument_list|,
name|destAddress
argument_list|,
name|length
argument_list|)
expr_stmt|;
block|}
comment|// APIs to add primitives to BBs
comment|/**    * Put a short value out to the specified BB position in big-endian format.    * @param buf the byte buffer    * @param offset position in the buffer    * @param val short to write out    * @return incremented offset    */
specifier|public
specifier|static
name|int
name|putShort
parameter_list|(
name|ByteBuffer
name|buf
parameter_list|,
name|int
name|offset
parameter_list|,
name|short
name|val
parameter_list|)
block|{
if|if
condition|(
name|LITTLE_ENDIAN
condition|)
block|{
name|val
operator|=
name|Short
operator|.
name|reverseBytes
argument_list|(
name|val
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|buf
operator|.
name|isDirect
argument_list|()
condition|)
block|{
name|theUnsafe
operator|.
name|putShort
argument_list|(
operator|(
operator|(
name|DirectBuffer
operator|)
name|buf
operator|)
operator|.
name|address
argument_list|()
operator|+
name|offset
argument_list|,
name|val
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|theUnsafe
operator|.
name|putShort
argument_list|(
name|buf
operator|.
name|array
argument_list|()
argument_list|,
name|BYTE_ARRAY_BASE_OFFSET
operator|+
name|buf
operator|.
name|arrayOffset
argument_list|()
operator|+
name|offset
argument_list|,
name|val
argument_list|)
expr_stmt|;
block|}
return|return
name|offset
operator|+
name|Bytes
operator|.
name|SIZEOF_SHORT
return|;
block|}
comment|/**    * Put a long value out to the specified BB position in big-endian format.    * @param buf the byte buffer    * @param offset position in the buffer    * @param val long to write out    * @return incremented offset    */
specifier|public
specifier|static
name|int
name|putLong
parameter_list|(
name|ByteBuffer
name|buf
parameter_list|,
name|int
name|offset
parameter_list|,
name|long
name|val
parameter_list|)
block|{
if|if
condition|(
name|LITTLE_ENDIAN
condition|)
block|{
name|val
operator|=
name|Long
operator|.
name|reverseBytes
argument_list|(
name|val
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|buf
operator|.
name|isDirect
argument_list|()
condition|)
block|{
name|theUnsafe
operator|.
name|putLong
argument_list|(
operator|(
operator|(
name|DirectBuffer
operator|)
name|buf
operator|)
operator|.
name|address
argument_list|()
operator|+
name|offset
argument_list|,
name|val
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|theUnsafe
operator|.
name|putLong
argument_list|(
name|buf
operator|.
name|array
argument_list|()
argument_list|,
name|BYTE_ARRAY_BASE_OFFSET
operator|+
name|buf
operator|.
name|arrayOffset
argument_list|()
operator|+
name|offset
argument_list|,
name|val
argument_list|)
expr_stmt|;
block|}
return|return
name|offset
operator|+
name|Bytes
operator|.
name|SIZEOF_LONG
return|;
block|}
comment|/**    * Put a byte value out to the specified BB position in big-endian format.    * @param buf the byte buffer    * @param offset position in the buffer    * @param b byte to write out    * @return incremented offset    */
specifier|public
specifier|static
name|int
name|putByte
parameter_list|(
name|ByteBuffer
name|buf
parameter_list|,
name|int
name|offset
parameter_list|,
name|byte
name|b
parameter_list|)
block|{
if|if
condition|(
name|buf
operator|.
name|isDirect
argument_list|()
condition|)
block|{
name|theUnsafe
operator|.
name|putByte
argument_list|(
operator|(
operator|(
name|DirectBuffer
operator|)
name|buf
operator|)
operator|.
name|address
argument_list|()
operator|+
name|offset
argument_list|,
name|b
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|theUnsafe
operator|.
name|putByte
argument_list|(
name|buf
operator|.
name|array
argument_list|()
argument_list|,
name|BYTE_ARRAY_BASE_OFFSET
operator|+
name|buf
operator|.
name|arrayOffset
argument_list|()
operator|+
name|offset
argument_list|,
name|b
argument_list|)
expr_stmt|;
block|}
return|return
name|offset
operator|+
literal|1
return|;
block|}
comment|/**    * Returns the byte at the given offset    * @param buf the buffer to read    * @param offset the offset at which the byte has to be read    * @return the byte at the given offset    */
specifier|public
specifier|static
name|byte
name|toByte
parameter_list|(
name|ByteBuffer
name|buf
parameter_list|,
name|int
name|offset
parameter_list|)
block|{
if|if
condition|(
name|buf
operator|.
name|isDirect
argument_list|()
condition|)
block|{
return|return
name|theUnsafe
operator|.
name|getByte
argument_list|(
operator|(
operator|(
name|DirectBuffer
operator|)
name|buf
operator|)
operator|.
name|address
argument_list|()
operator|+
name|offset
argument_list|)
return|;
block|}
else|else
block|{
return|return
name|theUnsafe
operator|.
name|getByte
argument_list|(
name|buf
operator|.
name|array
argument_list|()
argument_list|,
name|BYTE_ARRAY_BASE_OFFSET
operator|+
name|buf
operator|.
name|arrayOffset
argument_list|()
operator|+
name|offset
argument_list|)
return|;
block|}
block|}
comment|/**    * Returns the byte at the given offset of the object    * @param ref    * @param offset    * @return the byte at the given offset    */
specifier|public
specifier|static
name|byte
name|toByte
parameter_list|(
name|Object
name|ref
parameter_list|,
name|long
name|offset
parameter_list|)
block|{
return|return
name|theUnsafe
operator|.
name|getByte
argument_list|(
name|ref
argument_list|,
name|offset
argument_list|)
return|;
block|}
block|}
end_class

end_unit

