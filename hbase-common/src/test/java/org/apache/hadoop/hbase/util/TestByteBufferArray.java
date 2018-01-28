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
name|ByteBufferAllocator
name|allocator
init|=
operator|new
name|ByteBufferAllocator
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|ByteBuffer
name|allocate
parameter_list|(
name|long
name|size
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|ByteBuffer
operator|.
name|allocateDirect
argument_list|(
operator|(
name|int
operator|)
name|size
argument_list|)
return|;
block|}
block|}
decl_stmt|;
name|ByteBufferArray
name|array
init|=
operator|new
name|ByteBufferArray
argument_list|(
name|capacity
argument_list|,
name|allocator
argument_list|)
decl_stmt|;
name|ByteBuff
name|subBuf
init|=
name|array
operator|.
name|asSubByteBuff
argument_list|(
literal|0
argument_list|,
name|capacity
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
name|ByteBufferAllocator
name|allocator
init|=
operator|new
name|ByteBufferAllocator
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|ByteBuffer
name|allocate
parameter_list|(
name|long
name|size
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|ByteBuffer
operator|.
name|allocateDirect
argument_list|(
operator|(
name|int
operator|)
name|size
argument_list|)
return|;
block|}
block|}
decl_stmt|;
name|ByteBufferArray
name|array
init|=
operator|new
name|ByteBufferArray
argument_list|(
name|capacity
argument_list|,
name|allocator
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|119
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
if|if
condition|(
name|i
operator|==
name|array
operator|.
name|buffers
operator|.
name|length
operator|-
literal|1
condition|)
block|{
name|assertEquals
argument_list|(
literal|0
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
else|else
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
name|ByteBufferAllocator
name|allocator
init|=
operator|new
name|ByteBufferAllocator
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|ByteBuffer
name|allocate
parameter_list|(
name|long
name|size
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|ByteBuffer
operator|.
name|allocateDirect
argument_list|(
operator|(
name|int
operator|)
name|size
argument_list|)
return|;
block|}
block|}
decl_stmt|;
name|ByteBufferArray
name|array
init|=
operator|new
name|DummyByteBufferArray
argument_list|(
literal|7
operator|*
literal|1024
operator|*
literal|1024
argument_list|,
name|allocator
argument_list|)
decl_stmt|;
comment|// overwrite
name|array
operator|.
name|bufferCount
operator|=
literal|25
expr_stmt|;
name|array
operator|.
name|buffers
operator|=
operator|new
name|ByteBuffer
index|[
name|array
operator|.
name|bufferCount
operator|+
literal|1
index|]
expr_stmt|;
name|array
operator|.
name|createBuffers
argument_list|(
name|allocator
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
if|if
condition|(
name|i
operator|==
name|array
operator|.
name|buffers
operator|.
name|length
operator|-
literal|1
condition|)
block|{
name|assertEquals
argument_list|(
literal|0
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
else|else
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
block|}
specifier|private
specifier|static
class|class
name|DummyByteBufferArray
extends|extends
name|ByteBufferArray
block|{
specifier|public
name|DummyByteBufferArray
parameter_list|(
name|long
name|capacity
parameter_list|,
name|ByteBufferAllocator
name|allocator
parameter_list|)
throws|throws
name|IOException
block|{
name|super
argument_list|(
name|capacity
argument_list|,
name|allocator
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
name|int
name|getThreadCount
parameter_list|()
block|{
return|return
literal|16
return|;
block|}
block|}
block|}
end_class

end_unit

