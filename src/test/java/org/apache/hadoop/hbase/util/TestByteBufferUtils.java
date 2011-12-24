begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one or more  * contributor license agreements. See the NOTICE file distributed with this  * work for additional information regarding copyright ownership. The ASF  * licenses this file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the  * License for the specific language governing permissions and limitations  * under the License.  */
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
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertArrayEquals
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertEquals
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertFalse
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertTrue
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|fail
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|ByteArrayInputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|ByteArrayOutputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataInputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataOutputStream
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
name|Set
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|SortedSet
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|TreeSet
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
name|SmallTests
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

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Before
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Test
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|experimental
operator|.
name|categories
operator|.
name|Category
import|;
end_import

begin_class
annotation|@
name|Category
argument_list|(
name|SmallTests
operator|.
name|class
argument_list|)
specifier|public
class|class
name|TestByteBufferUtils
block|{
specifier|private
name|byte
index|[]
name|array
decl_stmt|;
comment|/**    * Create an array with sample data.    */
annotation|@
name|Before
specifier|public
name|void
name|setUp
parameter_list|()
block|{
name|array
operator|=
operator|new
name|byte
index|[
literal|8
index|]
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
name|array
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
name|array
index|[
name|i
index|]
operator|=
call|(
name|byte
call|)
argument_list|(
literal|'a'
operator|+
name|i
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
specifier|static
specifier|final
name|int
name|MAX_VLONG_LENGTH
init|=
literal|9
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|Collection
argument_list|<
name|Long
argument_list|>
name|testNumbers
decl_stmt|;
specifier|private
specifier|static
name|void
name|addNumber
parameter_list|(
name|Set
argument_list|<
name|Long
argument_list|>
name|a
parameter_list|,
name|long
name|l
parameter_list|)
block|{
if|if
condition|(
name|l
operator|!=
name|Long
operator|.
name|MIN_VALUE
condition|)
block|{
name|a
operator|.
name|add
argument_list|(
name|l
operator|-
literal|1
argument_list|)
expr_stmt|;
block|}
name|a
operator|.
name|add
argument_list|(
name|l
argument_list|)
expr_stmt|;
if|if
condition|(
name|l
operator|!=
name|Long
operator|.
name|MAX_VALUE
condition|)
block|{
name|a
operator|.
name|add
argument_list|(
name|l
operator|+
literal|1
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|long
name|divisor
init|=
literal|3
init|;
name|divisor
operator|<=
literal|10
condition|;
operator|++
name|divisor
control|)
block|{
for|for
control|(
name|long
name|delta
init|=
operator|-
literal|1
init|;
name|delta
operator|<=
literal|1
condition|;
operator|++
name|delta
control|)
block|{
name|a
operator|.
name|add
argument_list|(
name|l
operator|/
name|divisor
operator|+
name|delta
argument_list|)
expr_stmt|;
block|}
block|}
block|}
static|static
block|{
name|SortedSet
argument_list|<
name|Long
argument_list|>
name|a
init|=
operator|new
name|TreeSet
argument_list|<
name|Long
argument_list|>
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
operator|<=
literal|63
condition|;
operator|++
name|i
control|)
block|{
name|long
name|v
init|=
operator|(
operator|-
literal|1L
operator|)
operator|<<
name|i
decl_stmt|;
name|assertTrue
argument_list|(
name|v
operator|<
literal|0
argument_list|)
expr_stmt|;
name|addNumber
argument_list|(
name|a
argument_list|,
name|v
argument_list|)
expr_stmt|;
name|v
operator|=
operator|(
literal|1L
operator|<<
name|i
operator|)
operator|-
literal|1
expr_stmt|;
name|assertTrue
argument_list|(
name|v
operator|>=
literal|0
argument_list|)
expr_stmt|;
name|addNumber
argument_list|(
name|a
argument_list|,
name|v
argument_list|)
expr_stmt|;
block|}
name|testNumbers
operator|=
name|Collections
operator|.
name|unmodifiableSet
argument_list|(
name|a
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"Testing variable-length long serialization using: "
operator|+
name|testNumbers
operator|+
literal|" (count: "
operator|+
name|testNumbers
operator|.
name|size
argument_list|()
operator|+
literal|")"
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1753
argument_list|,
name|testNumbers
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|Long
operator|.
name|MIN_VALUE
argument_list|,
name|a
operator|.
name|first
argument_list|()
operator|.
name|longValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|Long
operator|.
name|MAX_VALUE
argument_list|,
name|a
operator|.
name|last
argument_list|()
operator|.
name|longValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testReadWriteVLong
parameter_list|()
block|{
for|for
control|(
name|long
name|l
range|:
name|testNumbers
control|)
block|{
name|ByteBuffer
name|b
init|=
name|ByteBuffer
operator|.
name|allocate
argument_list|(
name|MAX_VLONG_LENGTH
argument_list|)
decl_stmt|;
name|ByteBufferUtils
operator|.
name|writeVLong
argument_list|(
name|b
argument_list|,
name|l
argument_list|)
expr_stmt|;
name|b
operator|.
name|flip
argument_list|()
expr_stmt|;
name|assertEquals
argument_list|(
name|l
argument_list|,
name|ByteBufferUtils
operator|.
name|readVLong
argument_list|(
name|b
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testConsistencyWithHadoopVLong
parameter_list|()
throws|throws
name|IOException
block|{
name|ByteArrayOutputStream
name|baos
init|=
operator|new
name|ByteArrayOutputStream
argument_list|()
decl_stmt|;
name|DataOutputStream
name|dos
init|=
operator|new
name|DataOutputStream
argument_list|(
name|baos
argument_list|)
decl_stmt|;
for|for
control|(
name|long
name|l
range|:
name|testNumbers
control|)
block|{
name|baos
operator|.
name|reset
argument_list|()
expr_stmt|;
name|ByteBuffer
name|b
init|=
name|ByteBuffer
operator|.
name|allocate
argument_list|(
name|MAX_VLONG_LENGTH
argument_list|)
decl_stmt|;
name|ByteBufferUtils
operator|.
name|writeVLong
argument_list|(
name|b
argument_list|,
name|l
argument_list|)
expr_stmt|;
name|String
name|bufStr
init|=
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|b
operator|.
name|array
argument_list|()
argument_list|,
name|b
operator|.
name|arrayOffset
argument_list|()
argument_list|,
name|b
operator|.
name|position
argument_list|()
argument_list|)
decl_stmt|;
name|WritableUtils
operator|.
name|writeVLong
argument_list|(
name|dos
argument_list|,
name|l
argument_list|)
expr_stmt|;
name|String
name|baosStr
init|=
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|baos
operator|.
name|toByteArray
argument_list|()
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|baosStr
argument_list|,
name|bufStr
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Test copying to stream from buffer.    */
annotation|@
name|Test
specifier|public
name|void
name|testCopyToStream
parameter_list|()
block|{
name|ByteBuffer
name|buffer
init|=
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|array
argument_list|)
decl_stmt|;
name|ByteArrayOutputStream
name|bos
init|=
operator|new
name|ByteArrayOutputStream
argument_list|()
decl_stmt|;
try|try
block|{
name|ByteBufferUtils
operator|.
name|copyToStream
argument_list|(
name|bos
argument_list|,
name|buffer
argument_list|,
name|array
operator|.
name|length
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|fail
argument_list|(
literal|"IOException in testCopyToStream()"
argument_list|)
expr_stmt|;
block|}
name|assertArrayEquals
argument_list|(
name|bos
operator|.
name|toByteArray
argument_list|()
argument_list|,
name|array
argument_list|)
expr_stmt|;
block|}
comment|/**    * Test copying to stream one byte.    * @throws IOException On test failure.    */
annotation|@
name|Test
specifier|public
name|void
name|testCopyToStreamByte
parameter_list|()
throws|throws
name|IOException
block|{
name|ByteArrayOutputStream
name|bos
init|=
operator|new
name|ByteArrayOutputStream
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
name|array
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
name|ByteBufferUtils
operator|.
name|copyToStream
argument_list|(
name|bos
argument_list|,
name|array
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
name|byte
index|[]
name|actual
init|=
name|bos
operator|.
name|toByteArray
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
name|array
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
name|assertEquals
argument_list|(
name|array
index|[
name|i
index|]
argument_list|,
name|actual
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Test copying to stream from buffer with offset.    * @throws IOException On test failure.    */
annotation|@
name|Test
specifier|public
name|void
name|testCopyToStreamWithOffset
parameter_list|()
throws|throws
name|IOException
block|{
name|ByteBuffer
name|buffer
init|=
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|array
argument_list|)
decl_stmt|;
name|ByteArrayOutputStream
name|bos
init|=
operator|new
name|ByteArrayOutputStream
argument_list|()
decl_stmt|;
name|ByteBufferUtils
operator|.
name|copyToStream
argument_list|(
name|bos
argument_list|,
name|buffer
argument_list|,
name|array
operator|.
name|length
operator|/
literal|2
argument_list|,
name|array
operator|.
name|length
operator|/
literal|2
argument_list|)
expr_stmt|;
name|byte
index|[]
name|returnedArray
init|=
name|bos
operator|.
name|toByteArray
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
name|array
operator|.
name|length
operator|/
literal|2
condition|;
operator|++
name|i
control|)
block|{
name|int
name|pos
init|=
name|array
operator|.
name|length
operator|/
literal|2
operator|+
name|i
decl_stmt|;
name|assertEquals
argument_list|(
name|returnedArray
index|[
name|i
index|]
argument_list|,
name|array
index|[
name|pos
index|]
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Test copying data from stream.    * @throws IOException On test failure.    */
annotation|@
name|Test
specifier|public
name|void
name|testCopyFromStream
parameter_list|()
throws|throws
name|IOException
block|{
name|ByteBuffer
name|buffer
init|=
name|ByteBuffer
operator|.
name|allocate
argument_list|(
name|array
operator|.
name|length
argument_list|)
decl_stmt|;
name|ByteArrayInputStream
name|bis
init|=
operator|new
name|ByteArrayInputStream
argument_list|(
name|array
argument_list|)
decl_stmt|;
name|DataInputStream
name|dis
init|=
operator|new
name|DataInputStream
argument_list|(
name|bis
argument_list|)
decl_stmt|;
name|ByteBufferUtils
operator|.
name|copyFromStream
argument_list|(
name|dis
argument_list|,
name|buffer
argument_list|,
name|array
operator|.
name|length
operator|/
literal|2
argument_list|)
expr_stmt|;
name|ByteBufferUtils
operator|.
name|copyFromStream
argument_list|(
name|dis
argument_list|,
name|buffer
argument_list|,
name|array
operator|.
name|length
operator|-
name|array
operator|.
name|length
operator|/
literal|2
argument_list|)
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
name|array
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
name|assertEquals
argument_list|(
name|array
index|[
name|i
index|]
argument_list|,
name|buffer
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Test copying from buffer.    */
annotation|@
name|Test
specifier|public
name|void
name|testCopyFromBuffer
parameter_list|()
block|{
name|ByteBuffer
name|srcBuffer
init|=
name|ByteBuffer
operator|.
name|allocate
argument_list|(
name|array
operator|.
name|length
argument_list|)
decl_stmt|;
name|ByteBuffer
name|dstBuffer
init|=
name|ByteBuffer
operator|.
name|allocate
argument_list|(
name|array
operator|.
name|length
argument_list|)
decl_stmt|;
name|srcBuffer
operator|.
name|put
argument_list|(
name|array
argument_list|)
expr_stmt|;
name|ByteBufferUtils
operator|.
name|copyFromBuffer
argument_list|(
name|srcBuffer
argument_list|,
name|dstBuffer
argument_list|,
name|array
operator|.
name|length
operator|/
literal|2
argument_list|,
name|array
operator|.
name|length
operator|/
literal|4
argument_list|)
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
name|array
operator|.
name|length
operator|/
literal|4
condition|;
operator|++
name|i
control|)
block|{
name|assertEquals
argument_list|(
name|srcBuffer
operator|.
name|get
argument_list|(
name|i
operator|+
name|array
operator|.
name|length
operator|/
literal|2
argument_list|)
argument_list|,
name|dstBuffer
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Test 7-bit encoding of integers.    * @throws IOException On test failure.    */
annotation|@
name|Test
specifier|public
name|void
name|testCompressedInt
parameter_list|()
throws|throws
name|IOException
block|{
name|testCompressedInt
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|testCompressedInt
argument_list|(
name|Integer
operator|.
name|MAX_VALUE
argument_list|)
expr_stmt|;
name|testCompressedInt
argument_list|(
name|Integer
operator|.
name|MIN_VALUE
argument_list|)
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
literal|3
condition|;
name|i
operator|++
control|)
block|{
name|testCompressedInt
argument_list|(
operator|(
literal|128
operator|<<
name|i
operator|)
operator|-
literal|1
argument_list|)
expr_stmt|;
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
literal|3
condition|;
name|i
operator|++
control|)
block|{
name|testCompressedInt
argument_list|(
operator|(
literal|128
operator|<<
name|i
operator|)
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Test fitting integer in less bytes.    */
annotation|@
name|Test
specifier|public
name|void
name|testFixedInt
parameter_list|()
block|{
name|testFixedInt
argument_list|(
literal|0
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|testFixedInt
argument_list|(
name|Integer
operator|.
name|MAX_VALUE
argument_list|,
literal|4
argument_list|)
expr_stmt|;
name|testFixedInt
argument_list|(
name|Integer
operator|.
name|MIN_VALUE
argument_list|,
literal|4
argument_list|)
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
literal|3
condition|;
name|i
operator|++
control|)
block|{
name|testFixedInt
argument_list|(
operator|(
literal|128
operator|<<
name|i
operator|)
operator|-
literal|1
argument_list|,
name|i
operator|+
literal|1
argument_list|)
expr_stmt|;
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
literal|3
condition|;
name|i
operator|++
control|)
block|{
name|testFixedInt
argument_list|(
operator|(
literal|128
operator|<<
name|i
operator|)
argument_list|,
literal|2
operator|+
name|i
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Test how much bytes we need to store integer.    */
annotation|@
name|Test
specifier|public
name|void
name|testIntFitsIn
parameter_list|()
block|{
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|ByteBufferUtils
operator|.
name|intFitsIn
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|ByteBufferUtils
operator|.
name|intFitsIn
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|ByteBufferUtils
operator|.
name|intFitsIn
argument_list|(
literal|1
operator|<<
literal|8
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|3
argument_list|,
name|ByteBufferUtils
operator|.
name|intFitsIn
argument_list|(
literal|1
operator|<<
literal|16
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|4
argument_list|,
name|ByteBufferUtils
operator|.
name|intFitsIn
argument_list|(
operator|-
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|4
argument_list|,
name|ByteBufferUtils
operator|.
name|intFitsIn
argument_list|(
name|Integer
operator|.
name|MAX_VALUE
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|4
argument_list|,
name|ByteBufferUtils
operator|.
name|intFitsIn
argument_list|(
name|Integer
operator|.
name|MIN_VALUE
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * Test how much bytes we need to store long.    */
annotation|@
name|Test
specifier|public
name|void
name|testLongFitsIn
parameter_list|()
block|{
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|ByteBufferUtils
operator|.
name|longFitsIn
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|ByteBufferUtils
operator|.
name|longFitsIn
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|3
argument_list|,
name|ByteBufferUtils
operator|.
name|longFitsIn
argument_list|(
literal|1l
operator|<<
literal|16
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|5
argument_list|,
name|ByteBufferUtils
operator|.
name|longFitsIn
argument_list|(
literal|1l
operator|<<
literal|32
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|8
argument_list|,
name|ByteBufferUtils
operator|.
name|longFitsIn
argument_list|(
operator|-
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|8
argument_list|,
name|ByteBufferUtils
operator|.
name|longFitsIn
argument_list|(
name|Long
operator|.
name|MIN_VALUE
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|8
argument_list|,
name|ByteBufferUtils
operator|.
name|longFitsIn
argument_list|(
name|Long
operator|.
name|MAX_VALUE
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * Test if we are comparing equal bytes.    */
annotation|@
name|Test
specifier|public
name|void
name|testArePartEqual
parameter_list|()
block|{
name|byte
index|[]
name|array
init|=
operator|new
name|byte
index|[]
block|{
literal|1
block|,
literal|2
block|,
literal|3
block|,
literal|4
block|,
literal|5
block|,
literal|1
block|,
literal|2
block|,
literal|3
block|,
literal|4
block|}
decl_stmt|;
name|ByteBuffer
name|buffer
init|=
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|array
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|ByteBufferUtils
operator|.
name|arePartsEqual
argument_list|(
name|buffer
argument_list|,
literal|0
argument_list|,
literal|4
argument_list|,
literal|5
argument_list|,
literal|4
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|ByteBufferUtils
operator|.
name|arePartsEqual
argument_list|(
name|buffer
argument_list|,
literal|1
argument_list|,
literal|2
argument_list|,
literal|6
argument_list|,
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|ByteBufferUtils
operator|.
name|arePartsEqual
argument_list|(
name|buffer
argument_list|,
literal|1
argument_list|,
literal|2
argument_list|,
literal|6
argument_list|,
literal|3
argument_list|)
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|ByteBufferUtils
operator|.
name|arePartsEqual
argument_list|(
name|buffer
argument_list|,
literal|1
argument_list|,
literal|3
argument_list|,
literal|6
argument_list|,
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|ByteBufferUtils
operator|.
name|arePartsEqual
argument_list|(
name|buffer
argument_list|,
literal|0
argument_list|,
literal|3
argument_list|,
literal|6
argument_list|,
literal|3
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * Test serializing int to bytes    */
annotation|@
name|Test
specifier|public
name|void
name|testPutInt
parameter_list|()
block|{
name|testPutInt
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|testPutInt
argument_list|(
name|Integer
operator|.
name|MAX_VALUE
argument_list|)
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
literal|3
condition|;
name|i
operator|++
control|)
block|{
name|testPutInt
argument_list|(
operator|(
literal|128
operator|<<
name|i
operator|)
operator|-
literal|1
argument_list|)
expr_stmt|;
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
literal|3
condition|;
name|i
operator|++
control|)
block|{
name|testPutInt
argument_list|(
operator|(
literal|128
operator|<<
name|i
operator|)
argument_list|)
expr_stmt|;
block|}
block|}
comment|// Utility methods invoked from test methods
specifier|private
name|void
name|testCompressedInt
parameter_list|(
name|int
name|value
parameter_list|)
throws|throws
name|IOException
block|{
name|int
name|parsedValue
init|=
literal|0
decl_stmt|;
name|ByteArrayOutputStream
name|bos
init|=
operator|new
name|ByteArrayOutputStream
argument_list|()
decl_stmt|;
name|ByteBufferUtils
operator|.
name|putCompressedInt
argument_list|(
name|bos
argument_list|,
name|value
argument_list|)
expr_stmt|;
name|ByteArrayInputStream
name|bis
init|=
operator|new
name|ByteArrayInputStream
argument_list|(
name|bos
operator|.
name|toByteArray
argument_list|()
argument_list|)
decl_stmt|;
name|parsedValue
operator|=
name|ByteBufferUtils
operator|.
name|readCompressedInt
argument_list|(
name|bis
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|value
argument_list|,
name|parsedValue
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|testFixedInt
parameter_list|(
name|int
name|value
parameter_list|,
name|int
name|width
parameter_list|)
block|{
name|int
name|parsedValue
init|=
literal|0
decl_stmt|;
name|byte
index|[]
name|bValue
init|=
operator|new
name|byte
index|[
name|Bytes
operator|.
name|SIZEOF_INT
index|]
decl_stmt|;
name|Bytes
operator|.
name|putInt
argument_list|(
name|bValue
argument_list|,
literal|0
argument_list|,
name|value
argument_list|)
expr_stmt|;
name|ByteArrayOutputStream
name|bos
init|=
operator|new
name|ByteArrayOutputStream
argument_list|()
decl_stmt|;
name|bos
operator|.
name|write
argument_list|(
name|bValue
argument_list|,
name|Bytes
operator|.
name|SIZEOF_INT
operator|-
name|width
argument_list|,
name|width
argument_list|)
expr_stmt|;
name|ByteArrayInputStream
name|bis
init|=
operator|new
name|ByteArrayInputStream
argument_list|(
name|bos
operator|.
name|toByteArray
argument_list|()
argument_list|)
decl_stmt|;
try|try
block|{
name|parsedValue
operator|=
name|ByteBufferUtils
operator|.
name|readCompressedInt
argument_list|(
name|bis
argument_list|,
name|width
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|fail
argument_list|(
literal|"While reading fixed int"
argument_list|)
expr_stmt|;
block|}
name|assertEquals
argument_list|(
name|value
argument_list|,
name|parsedValue
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|testPutInt
parameter_list|(
name|int
name|value
parameter_list|)
block|{
name|ByteArrayOutputStream
name|baos
init|=
operator|new
name|ByteArrayOutputStream
argument_list|()
decl_stmt|;
try|try
block|{
name|ByteBufferUtils
operator|.
name|putInt
argument_list|(
name|baos
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Bug in putIn()"
argument_list|,
name|e
argument_list|)
throw|;
block|}
name|ByteArrayInputStream
name|bais
init|=
operator|new
name|ByteArrayInputStream
argument_list|(
name|baos
operator|.
name|toByteArray
argument_list|()
argument_list|)
decl_stmt|;
name|DataInputStream
name|dis
init|=
operator|new
name|DataInputStream
argument_list|(
name|bais
argument_list|)
decl_stmt|;
try|try
block|{
name|assertEquals
argument_list|(
name|dis
operator|.
name|readInt
argument_list|()
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Bug in test!"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
block|}
end_class

end_unit

