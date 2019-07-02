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
name|Random
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
name|HBaseClassTestRule
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
name|nio
operator|.
name|ByteBuff
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
name|nio
operator|.
name|MultiByteBuff
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
name|nio
operator|.
name|SingleByteBuff
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
name|testclassification
operator|.
name|MiscTests
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
name|testclassification
operator|.
name|SmallTests
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|ClassRule
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
block|{
name|MiscTests
operator|.
name|class
block|,
name|SmallTests
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|TestByteBufferArray
block|{
specifier|private
specifier|static
specifier|final
name|Random
name|RANDOM
init|=
operator|new
name|Random
argument_list|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|)
decl_stmt|;
annotation|@
name|ClassRule
specifier|public
specifier|static
specifier|final
name|HBaseClassTestRule
name|CLASS_RULE
init|=
name|HBaseClassTestRule
operator|.
name|forClass
argument_list|(
name|TestByteBufferArray
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|ByteBufferAllocator
name|ALLOC
init|=
parameter_list|(
name|size
parameter_list|)
lambda|->
name|ByteBuffer
operator|.
name|allocateDirect
argument_list|(
operator|(
name|int
operator|)
name|size
argument_list|)
decl_stmt|;
annotation|@
name|Test
specifier|public
name|void
name|testAsSubBufferWhenEndOffsetLandInLastBuffer
parameter_list|()
throws|throws
name|Exception
block|{
name|int
name|capacity
init|=
literal|4
operator|*
literal|1024
operator|*
literal|1024
decl_stmt|;
name|ByteBufferArray
name|array
init|=
operator|new
name|ByteBufferArray
argument_list|(
name|capacity
argument_list|,
name|ALLOC
argument_list|)
decl_stmt|;
name|ByteBuff
name|subBuf
init|=
name|ByteBuff
operator|.
name|wrap
argument_list|(
name|array
operator|.
name|asSubByteBuffers
argument_list|(
literal|0
argument_list|,
name|capacity
argument_list|)
argument_list|)
decl_stmt|;
name|subBuf
operator|.
name|position
argument_list|(
name|capacity
operator|-
literal|1
argument_list|)
expr_stmt|;
comment|// Position to the last byte
name|assertTrue
argument_list|(
name|subBuf
operator|.
name|hasRemaining
argument_list|()
argument_list|)
expr_stmt|;
comment|// Read last byte
name|subBuf
operator|.
name|get
argument_list|()
expr_stmt|;
name|assertFalse
argument_list|(
name|subBuf
operator|.
name|hasRemaining
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testByteBufferCreation
parameter_list|()
throws|throws
name|Exception
block|{
name|int
name|capacity
init|=
literal|470
operator|*
literal|1021
operator|*
literal|1023
decl_stmt|;
name|ByteBufferArray
name|array
init|=
operator|new
name|ByteBufferArray
argument_list|(
name|capacity
argument_list|,
name|ALLOC
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|118
argument_list|,
name|array
operator|.
name|buffers
operator|.
name|length
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
name|buffers
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|assertEquals
argument_list|(
name|ByteBufferArray
operator|.
name|DEFAULT_BUFFER_SIZE
argument_list|,
name|array
operator|.
name|buffers
index|[
name|i
index|]
operator|.
name|capacity
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testByteBufferCreation1
parameter_list|()
throws|throws
name|Exception
block|{
name|long
name|cap
init|=
literal|7
operator|*
literal|1024L
operator|*
literal|1024L
decl_stmt|;
name|int
name|bufferSize
init|=
name|ByteBufferArray
operator|.
name|getBufferSize
argument_list|(
name|cap
argument_list|)
decl_stmt|,
name|bufferCount
init|=
literal|25
decl_stmt|;
name|ByteBufferArray
name|array
init|=
operator|new
name|ByteBufferArray
argument_list|(
name|bufferSize
argument_list|,
name|bufferCount
argument_list|,
literal|16
argument_list|,
name|cap
argument_list|,
name|ALLOC
argument_list|)
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
name|buffers
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|assertEquals
argument_list|(
literal|458752
argument_list|,
name|array
operator|.
name|buffers
index|[
name|i
index|]
operator|.
name|capacity
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
specifier|static
name|void
name|fill
parameter_list|(
name|ByteBuff
name|buf
parameter_list|,
name|byte
name|val
parameter_list|)
block|{
for|for
control|(
name|int
name|i
init|=
name|buf
operator|.
name|position
argument_list|()
init|;
name|i
operator|<
name|buf
operator|.
name|limit
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|buf
operator|.
name|put
argument_list|(
name|i
argument_list|,
name|val
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|ByteBuff
name|createByteBuff
parameter_list|(
name|int
name|len
parameter_list|)
block|{
assert|assert
name|len
operator|>=
literal|0
assert|;
name|int
name|pos
init|=
name|len
operator|==
literal|0
condition|?
literal|0
else|:
name|RANDOM
operator|.
name|nextInt
argument_list|(
name|len
argument_list|)
decl_stmt|;
name|ByteBuff
name|b
init|=
name|ByteBuff
operator|.
name|wrap
argument_list|(
name|ByteBuffer
operator|.
name|allocate
argument_list|(
literal|2
operator|*
name|len
argument_list|)
argument_list|)
decl_stmt|;
name|b
operator|.
name|position
argument_list|(
name|pos
argument_list|)
operator|.
name|limit
argument_list|(
name|pos
operator|+
name|len
argument_list|)
expr_stmt|;
return|return
name|b
return|;
block|}
specifier|private
interface|interface
name|Call
block|{
name|void
name|run
parameter_list|()
throws|throws
name|IOException
function_decl|;
block|}
annotation|@
name|SuppressWarnings
argument_list|(
literal|"TryFailThrowable"
argument_list|)
specifier|private
name|void
name|expectedAssert
parameter_list|(
name|Call
name|r
parameter_list|)
throws|throws
name|IOException
block|{
try|try
block|{
name|r
operator|.
name|run
argument_list|()
expr_stmt|;
name|fail
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|AssertionError
name|e
parameter_list|)
block|{
comment|// Ignore
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testArrayIO
parameter_list|()
throws|throws
name|IOException
block|{
name|int
name|cap
init|=
literal|9
operator|*
literal|1024
operator|*
literal|1024
decl_stmt|,
name|bufferSize
init|=
name|ByteBufferArray
operator|.
name|getBufferSize
argument_list|(
name|cap
argument_list|)
decl_stmt|;
name|ByteBufferArray
name|array
init|=
operator|new
name|ByteBufferArray
argument_list|(
name|cap
argument_list|,
name|ALLOC
argument_list|)
decl_stmt|;
name|testReadAndWrite
argument_list|(
name|array
argument_list|,
literal|0
argument_list|,
literal|512
argument_list|,
operator|(
name|byte
operator|)
literal|2
argument_list|)
expr_stmt|;
name|testReadAndWrite
argument_list|(
name|array
argument_list|,
name|cap
operator|-
literal|512
argument_list|,
literal|512
argument_list|,
operator|(
name|byte
operator|)
literal|3
argument_list|)
expr_stmt|;
name|testReadAndWrite
argument_list|(
name|array
argument_list|,
literal|4
operator|*
literal|1024
operator|*
literal|1024
argument_list|,
literal|5
operator|*
literal|1024
operator|*
literal|1024
argument_list|,
operator|(
name|byte
operator|)
literal|4
argument_list|)
expr_stmt|;
name|testReadAndWrite
argument_list|(
name|array
argument_list|,
literal|256
argument_list|,
literal|256
argument_list|,
operator|(
name|byte
operator|)
literal|5
argument_list|)
expr_stmt|;
name|testReadAndWrite
argument_list|(
name|array
argument_list|,
literal|257
argument_list|,
literal|513
argument_list|,
operator|(
name|byte
operator|)
literal|6
argument_list|)
expr_stmt|;
name|testReadAndWrite
argument_list|(
name|array
argument_list|,
literal|0
argument_list|,
name|cap
argument_list|,
operator|(
name|byte
operator|)
literal|7
argument_list|)
expr_stmt|;
name|testReadAndWrite
argument_list|(
name|array
argument_list|,
name|cap
argument_list|,
literal|0
argument_list|,
operator|(
name|byte
operator|)
literal|8
argument_list|)
expr_stmt|;
name|testReadAndWrite
argument_list|(
name|array
argument_list|,
name|cap
operator|-
literal|1
argument_list|,
literal|1
argument_list|,
operator|(
name|byte
operator|)
literal|9
argument_list|)
expr_stmt|;
name|testReadAndWrite
argument_list|(
name|array
argument_list|,
name|cap
operator|-
literal|2
argument_list|,
literal|2
argument_list|,
operator|(
name|byte
operator|)
literal|10
argument_list|)
expr_stmt|;
name|expectedAssert
argument_list|(
parameter_list|()
lambda|->
name|testReadAndWrite
argument_list|(
name|array
argument_list|,
name|cap
operator|-
literal|2
argument_list|,
literal|3
argument_list|,
operator|(
name|byte
operator|)
literal|11
argument_list|)
argument_list|)
expr_stmt|;
name|expectedAssert
argument_list|(
parameter_list|()
lambda|->
name|testReadAndWrite
argument_list|(
name|array
argument_list|,
name|cap
operator|+
literal|1
argument_list|,
literal|0
argument_list|,
operator|(
name|byte
operator|)
literal|12
argument_list|)
argument_list|)
expr_stmt|;
name|expectedAssert
argument_list|(
parameter_list|()
lambda|->
name|testReadAndWrite
argument_list|(
name|array
argument_list|,
literal|0
argument_list|,
name|cap
operator|+
literal|1
argument_list|,
operator|(
name|byte
operator|)
literal|12
argument_list|)
argument_list|)
expr_stmt|;
name|expectedAssert
argument_list|(
parameter_list|()
lambda|->
name|testReadAndWrite
argument_list|(
name|array
argument_list|,
operator|-
literal|1
argument_list|,
literal|0
argument_list|,
operator|(
name|byte
operator|)
literal|13
argument_list|)
argument_list|)
expr_stmt|;
name|expectedAssert
argument_list|(
parameter_list|()
lambda|->
name|testReadAndWrite
argument_list|(
name|array
argument_list|,
literal|0
argument_list|,
operator|-
literal|23
argument_list|,
operator|(
name|byte
operator|)
literal|14
argument_list|)
argument_list|)
expr_stmt|;
name|expectedAssert
argument_list|(
parameter_list|()
lambda|->
name|testReadAndWrite
argument_list|(
name|array
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
operator|(
name|byte
operator|)
literal|15
argument_list|)
argument_list|)
expr_stmt|;
name|expectedAssert
argument_list|(
parameter_list|()
lambda|->
name|testReadAndWrite
argument_list|(
name|array
argument_list|,
literal|4096
argument_list|,
name|cap
operator|-
literal|4096
operator|+
literal|1
argument_list|,
operator|(
name|byte
operator|)
literal|16
argument_list|)
argument_list|)
expr_stmt|;
name|testAsSubByteBuff
argument_list|(
name|array
argument_list|,
literal|0
argument_list|,
name|cap
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|testAsSubByteBuff
argument_list|(
name|array
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|testAsSubByteBuff
argument_list|(
name|array
argument_list|,
literal|0
argument_list|,
literal|1
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|testAsSubByteBuff
argument_list|(
name|array
argument_list|,
literal|0
argument_list|,
name|bufferSize
operator|-
literal|1
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|testAsSubByteBuff
argument_list|(
name|array
argument_list|,
literal|0
argument_list|,
name|bufferSize
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|testAsSubByteBuff
argument_list|(
name|array
argument_list|,
literal|0
argument_list|,
name|bufferSize
operator|+
literal|1
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|testAsSubByteBuff
argument_list|(
name|array
argument_list|,
literal|0
argument_list|,
literal|2
operator|*
name|bufferSize
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|testAsSubByteBuff
argument_list|(
name|array
argument_list|,
literal|0
argument_list|,
literal|5
operator|*
name|bufferSize
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|testAsSubByteBuff
argument_list|(
name|array
argument_list|,
name|cap
operator|-
name|bufferSize
operator|-
literal|1
argument_list|,
name|bufferSize
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|testAsSubByteBuff
argument_list|(
name|array
argument_list|,
name|cap
operator|-
name|bufferSize
argument_list|,
name|bufferSize
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|testAsSubByteBuff
argument_list|(
name|array
argument_list|,
name|cap
operator|-
name|bufferSize
argument_list|,
literal|0
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|testAsSubByteBuff
argument_list|(
name|array
argument_list|,
name|cap
operator|-
name|bufferSize
argument_list|,
literal|1
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|testAsSubByteBuff
argument_list|(
name|array
argument_list|,
name|cap
operator|-
name|bufferSize
argument_list|,
name|bufferSize
operator|-
literal|1
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|testAsSubByteBuff
argument_list|(
name|array
argument_list|,
name|cap
operator|-
literal|2
operator|*
name|bufferSize
argument_list|,
literal|2
operator|*
name|bufferSize
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|testAsSubByteBuff
argument_list|(
name|array
argument_list|,
name|cap
operator|-
literal|2
operator|*
name|bufferSize
argument_list|,
name|bufferSize
operator|+
literal|1
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|testAsSubByteBuff
argument_list|(
name|array
argument_list|,
name|cap
operator|-
literal|2
operator|*
name|bufferSize
argument_list|,
name|bufferSize
operator|-
literal|1
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|testAsSubByteBuff
argument_list|(
name|array
argument_list|,
name|cap
operator|-
literal|2
operator|*
name|bufferSize
argument_list|,
literal|0
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|expectedAssert
argument_list|(
parameter_list|()
lambda|->
name|testAsSubByteBuff
argument_list|(
name|array
argument_list|,
literal|0
argument_list|,
name|cap
operator|+
literal|1
argument_list|,
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|expectedAssert
argument_list|(
parameter_list|()
lambda|->
name|testAsSubByteBuff
argument_list|(
name|array
argument_list|,
literal|0
argument_list|,
operator|-
literal|1
argument_list|,
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|expectedAssert
argument_list|(
parameter_list|()
lambda|->
name|testAsSubByteBuff
argument_list|(
name|array
argument_list|,
operator|-
literal|1
argument_list|,
operator|-
literal|1
argument_list|,
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|expectedAssert
argument_list|(
parameter_list|()
lambda|->
name|testAsSubByteBuff
argument_list|(
name|array
argument_list|,
name|cap
operator|-
name|bufferSize
argument_list|,
name|bufferSize
operator|+
literal|1
argument_list|,
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|expectedAssert
argument_list|(
parameter_list|()
lambda|->
name|testAsSubByteBuff
argument_list|(
name|array
argument_list|,
literal|2
operator|*
name|bufferSize
argument_list|,
name|cap
operator|-
literal|2
operator|*
name|bufferSize
operator|+
literal|1
argument_list|,
literal|false
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|testReadAndWrite
parameter_list|(
name|ByteBufferArray
name|array
parameter_list|,
name|int
name|off
parameter_list|,
name|int
name|dataSize
parameter_list|,
name|byte
name|val
parameter_list|)
block|{
name|ByteBuff
name|src
init|=
name|createByteBuff
argument_list|(
name|dataSize
argument_list|)
decl_stmt|;
name|int
name|pos
init|=
name|src
operator|.
name|position
argument_list|()
decl_stmt|,
name|lim
init|=
name|src
operator|.
name|limit
argument_list|()
decl_stmt|;
name|fill
argument_list|(
name|src
argument_list|,
name|val
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|src
operator|.
name|remaining
argument_list|()
argument_list|,
name|dataSize
argument_list|)
expr_stmt|;
try|try
block|{
name|assertEquals
argument_list|(
name|dataSize
argument_list|,
name|array
operator|.
name|write
argument_list|(
name|off
argument_list|,
name|src
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|src
operator|.
name|remaining
argument_list|()
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|src
operator|.
name|position
argument_list|(
name|pos
argument_list|)
operator|.
name|limit
argument_list|(
name|lim
argument_list|)
expr_stmt|;
block|}
name|ByteBuff
name|dst
init|=
name|createByteBuff
argument_list|(
name|dataSize
argument_list|)
decl_stmt|;
name|pos
operator|=
name|dst
operator|.
name|position
argument_list|()
expr_stmt|;
name|lim
operator|=
name|dst
operator|.
name|limit
argument_list|()
expr_stmt|;
try|try
block|{
name|assertEquals
argument_list|(
name|dataSize
argument_list|,
name|array
operator|.
name|read
argument_list|(
name|off
argument_list|,
name|dst
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|dst
operator|.
name|remaining
argument_list|()
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|dst
operator|.
name|position
argument_list|(
name|pos
argument_list|)
operator|.
name|limit
argument_list|(
name|lim
argument_list|)
expr_stmt|;
block|}
name|assertByteBuffEquals
argument_list|(
name|src
argument_list|,
name|dst
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|testAsSubByteBuff
parameter_list|(
name|ByteBufferArray
name|array
parameter_list|,
name|int
name|off
parameter_list|,
name|int
name|len
parameter_list|,
name|boolean
name|isMulti
parameter_list|)
block|{
name|ByteBuff
name|ret
init|=
name|ByteBuff
operator|.
name|wrap
argument_list|(
name|array
operator|.
name|asSubByteBuffers
argument_list|(
name|off
argument_list|,
name|len
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|isMulti
condition|)
block|{
name|assertTrue
argument_list|(
name|ret
operator|instanceof
name|MultiByteBuff
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|assertTrue
argument_list|(
name|ret
operator|instanceof
name|SingleByteBuff
argument_list|)
expr_stmt|;
block|}
name|assertTrue
argument_list|(
operator|!
name|ret
operator|.
name|hasArray
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|len
argument_list|,
name|ret
operator|.
name|remaining
argument_list|()
argument_list|)
expr_stmt|;
name|ByteBuff
name|tmp
init|=
name|createByteBuff
argument_list|(
name|len
argument_list|)
decl_stmt|;
name|int
name|pos
init|=
name|tmp
operator|.
name|position
argument_list|()
decl_stmt|,
name|lim
init|=
name|tmp
operator|.
name|limit
argument_list|()
decl_stmt|;
try|try
block|{
name|assertEquals
argument_list|(
name|len
argument_list|,
name|array
operator|.
name|read
argument_list|(
name|off
argument_list|,
name|tmp
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|tmp
operator|.
name|remaining
argument_list|()
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|tmp
operator|.
name|position
argument_list|(
name|pos
argument_list|)
operator|.
name|limit
argument_list|(
name|lim
argument_list|)
expr_stmt|;
block|}
name|assertByteBuffEquals
argument_list|(
name|ret
argument_list|,
name|tmp
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|assertByteBuffEquals
parameter_list|(
name|ByteBuff
name|a
parameter_list|,
name|ByteBuff
name|b
parameter_list|)
block|{
name|assertEquals
argument_list|(
name|a
operator|.
name|remaining
argument_list|()
argument_list|,
name|b
operator|.
name|remaining
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
name|a
operator|.
name|position
argument_list|()
init|,
name|j
init|=
name|b
operator|.
name|position
argument_list|()
init|;
name|i
operator|<
name|a
operator|.
name|limit
argument_list|()
condition|;
name|i
operator|++
operator|,
name|j
operator|++
control|)
block|{
name|assertEquals
argument_list|(
name|a
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|,
name|b
operator|.
name|get
argument_list|(
name|j
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

