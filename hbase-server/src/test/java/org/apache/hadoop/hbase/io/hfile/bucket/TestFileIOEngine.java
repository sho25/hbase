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
operator|.
name|hfile
operator|.
name|bucket
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
name|assertNotEquals
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
name|assertNotNull
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|File
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
name|nio
operator|.
name|channels
operator|.
name|FileChannel
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|ArrayList
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
name|io
operator|.
name|hfile
operator|.
name|bucket
operator|.
name|TestByteBufferIOEngine
operator|.
name|BufferGrabbingDeserializer
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
name|IOTests
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
name|After
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Assert
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

begin_comment
comment|/**  * Basic test for {@link FileIOEngine}  */
end_comment

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|IOTests
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
name|TestFileIOEngine
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
name|TestFileIOEngine
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|long
name|TOTAL_CAPACITY
init|=
literal|6
operator|*
literal|1024
operator|*
literal|1024
decl_stmt|;
comment|// 6 MB
specifier|private
specifier|static
specifier|final
name|String
index|[]
name|FILE_PATHS
init|=
block|{
literal|"testFileIOEngine1"
block|,
literal|"testFileIOEngine2"
block|,
literal|"testFileIOEngine3"
block|}
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|long
name|SIZE_PER_FILE
init|=
name|TOTAL_CAPACITY
operator|/
name|FILE_PATHS
operator|.
name|length
decl_stmt|;
comment|// 2 MB per File
specifier|private
specifier|final
specifier|static
name|List
argument_list|<
name|Long
argument_list|>
name|boundaryStartPositions
init|=
operator|new
name|ArrayList
argument_list|<
name|Long
argument_list|>
argument_list|()
decl_stmt|;
specifier|private
specifier|final
specifier|static
name|List
argument_list|<
name|Long
argument_list|>
name|boundaryStopPositions
init|=
operator|new
name|ArrayList
argument_list|<
name|Long
argument_list|>
argument_list|()
decl_stmt|;
specifier|private
name|FileIOEngine
name|fileIOEngine
decl_stmt|;
static|static
block|{
name|boundaryStartPositions
operator|.
name|add
argument_list|(
literal|0L
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|1
init|;
name|i
operator|<
name|FILE_PATHS
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|boundaryStartPositions
operator|.
name|add
argument_list|(
name|SIZE_PER_FILE
operator|*
name|i
operator|-
literal|1
argument_list|)
expr_stmt|;
name|boundaryStartPositions
operator|.
name|add
argument_list|(
name|SIZE_PER_FILE
operator|*
name|i
argument_list|)
expr_stmt|;
name|boundaryStartPositions
operator|.
name|add
argument_list|(
name|SIZE_PER_FILE
operator|*
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
literal|1
init|;
name|i
operator|<
name|FILE_PATHS
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|boundaryStopPositions
operator|.
name|add
argument_list|(
name|SIZE_PER_FILE
operator|*
name|i
operator|-
literal|1
argument_list|)
expr_stmt|;
name|boundaryStopPositions
operator|.
name|add
argument_list|(
name|SIZE_PER_FILE
operator|*
name|i
argument_list|)
expr_stmt|;
name|boundaryStopPositions
operator|.
name|add
argument_list|(
name|SIZE_PER_FILE
operator|*
name|i
operator|+
literal|1
argument_list|)
expr_stmt|;
block|}
name|boundaryStopPositions
operator|.
name|add
argument_list|(
name|SIZE_PER_FILE
operator|*
name|FILE_PATHS
operator|.
name|length
operator|-
literal|1
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Before
specifier|public
name|void
name|setUp
parameter_list|()
throws|throws
name|IOException
block|{
name|fileIOEngine
operator|=
operator|new
name|FileIOEngine
argument_list|(
name|TOTAL_CAPACITY
argument_list|,
literal|false
argument_list|,
name|FILE_PATHS
argument_list|)
expr_stmt|;
block|}
annotation|@
name|After
specifier|public
name|void
name|cleanUp
parameter_list|()
block|{
name|fileIOEngine
operator|.
name|shutdown
argument_list|()
expr_stmt|;
for|for
control|(
name|String
name|filePath
range|:
name|FILE_PATHS
control|)
block|{
name|File
name|file
init|=
operator|new
name|File
argument_list|(
name|filePath
argument_list|)
decl_stmt|;
if|if
condition|(
name|file
operator|.
name|exists
argument_list|()
condition|)
block|{
name|file
operator|.
name|delete
argument_list|()
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testFileIOEngine
parameter_list|()
throws|throws
name|IOException
block|{
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
literal|500
condition|;
name|i
operator|++
control|)
block|{
name|int
name|len
init|=
operator|(
name|int
operator|)
name|Math
operator|.
name|floor
argument_list|(
name|Math
operator|.
name|random
argument_list|()
operator|*
literal|100
argument_list|)
operator|+
literal|1
decl_stmt|;
name|long
name|offset
init|=
operator|(
name|long
operator|)
name|Math
operator|.
name|floor
argument_list|(
name|Math
operator|.
name|random
argument_list|()
operator|*
name|TOTAL_CAPACITY
operator|%
operator|(
name|TOTAL_CAPACITY
operator|-
name|len
operator|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|i
operator|<
name|boundaryStartPositions
operator|.
name|size
argument_list|()
condition|)
block|{
comment|// make the boundary start positon
name|offset
operator|=
name|boundaryStartPositions
operator|.
name|get
argument_list|(
name|i
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
operator|(
name|i
operator|-
name|boundaryStartPositions
operator|.
name|size
argument_list|()
operator|)
operator|<
name|boundaryStopPositions
operator|.
name|size
argument_list|()
condition|)
block|{
comment|// make the boundary stop positon
name|offset
operator|=
name|boundaryStopPositions
operator|.
name|get
argument_list|(
name|i
operator|-
name|boundaryStartPositions
operator|.
name|size
argument_list|()
argument_list|)
operator|-
name|len
operator|+
literal|1
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|i
operator|%
literal|2
operator|==
literal|0
condition|)
block|{
comment|// make the cross-files block writing/reading
name|offset
operator|=
name|Math
operator|.
name|max
argument_list|(
literal|1
argument_list|,
name|i
operator|%
name|FILE_PATHS
operator|.
name|length
argument_list|)
operator|*
name|SIZE_PER_FILE
operator|-
name|len
operator|/
literal|2
expr_stmt|;
block|}
name|byte
index|[]
name|data1
init|=
operator|new
name|byte
index|[
name|len
index|]
decl_stmt|;
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
name|data1
operator|.
name|length
condition|;
operator|++
name|j
control|)
block|{
name|data1
index|[
name|j
index|]
operator|=
call|(
name|byte
call|)
argument_list|(
name|Math
operator|.
name|random
argument_list|()
operator|*
literal|255
argument_list|)
expr_stmt|;
block|}
name|fileIOEngine
operator|.
name|write
argument_list|(
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|data1
argument_list|)
argument_list|,
name|offset
argument_list|)
expr_stmt|;
name|BufferGrabbingDeserializer
name|deserializer
init|=
operator|new
name|BufferGrabbingDeserializer
argument_list|()
decl_stmt|;
name|fileIOEngine
operator|.
name|read
argument_list|(
name|offset
argument_list|,
name|len
argument_list|,
name|deserializer
argument_list|)
expr_stmt|;
name|ByteBuff
name|data2
init|=
name|deserializer
operator|.
name|getDeserializedByteBuff
argument_list|()
decl_stmt|;
name|assertArrayEquals
argument_list|(
name|data1
argument_list|,
name|data2
operator|.
name|array
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testFileIOEngineHandlesZeroLengthInput
parameter_list|()
throws|throws
name|IOException
block|{
name|byte
index|[]
name|data1
init|=
operator|new
name|byte
index|[
literal|0
index|]
decl_stmt|;
name|fileIOEngine
operator|.
name|write
argument_list|(
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|data1
argument_list|)
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|BufferGrabbingDeserializer
name|deserializer
init|=
operator|new
name|BufferGrabbingDeserializer
argument_list|()
decl_stmt|;
name|fileIOEngine
operator|.
name|read
argument_list|(
literal|0
argument_list|,
literal|0
argument_list|,
name|deserializer
argument_list|)
expr_stmt|;
name|ByteBuff
name|data2
init|=
name|deserializer
operator|.
name|getDeserializedByteBuff
argument_list|()
decl_stmt|;
name|assertArrayEquals
argument_list|(
name|data1
argument_list|,
name|data2
operator|.
name|array
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testClosedChannelException
parameter_list|()
throws|throws
name|IOException
block|{
name|fileIOEngine
operator|.
name|closeFileChannels
argument_list|()
expr_stmt|;
name|int
name|len
init|=
literal|5
decl_stmt|;
name|long
name|offset
init|=
literal|0L
decl_stmt|;
name|int
name|val
init|=
call|(
name|int
call|)
argument_list|(
name|Math
operator|.
name|random
argument_list|()
operator|*
literal|255
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
literal|2
condition|;
name|i
operator|++
control|)
block|{
name|ByteBuff
name|src
init|=
name|TestByteBufferIOEngine
operator|.
name|createByteBuffer
argument_list|(
name|len
argument_list|,
name|val
argument_list|,
name|i
operator|%
literal|2
operator|==
literal|0
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
name|fileIOEngine
operator|.
name|write
argument_list|(
name|src
argument_list|,
name|offset
argument_list|)
expr_stmt|;
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
name|BufferGrabbingDeserializer
name|deserializer
init|=
operator|new
name|BufferGrabbingDeserializer
argument_list|()
decl_stmt|;
name|fileIOEngine
operator|.
name|read
argument_list|(
name|offset
argument_list|,
name|len
argument_list|,
name|deserializer
argument_list|)
expr_stmt|;
name|ByteBuff
name|dst
init|=
name|deserializer
operator|.
name|getDeserializedByteBuff
argument_list|()
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|src
operator|.
name|remaining
argument_list|()
argument_list|,
name|len
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|dst
operator|.
name|remaining
argument_list|()
argument_list|,
name|len
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|ByteBuff
operator|.
name|compareTo
argument_list|(
name|src
argument_list|,
name|pos
argument_list|,
name|len
argument_list|,
name|dst
argument_list|,
name|dst
operator|.
name|position
argument_list|()
argument_list|,
name|dst
operator|.
name|remaining
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testRefreshFileConnection
parameter_list|()
throws|throws
name|IOException
block|{
name|FileChannel
index|[]
name|fileChannels
init|=
name|fileIOEngine
operator|.
name|getFileChannels
argument_list|()
decl_stmt|;
name|FileChannel
name|fileChannel
init|=
name|fileChannels
index|[
literal|0
index|]
decl_stmt|;
name|assertNotNull
argument_list|(
name|fileChannel
argument_list|)
expr_stmt|;
name|fileChannel
operator|.
name|close
argument_list|()
expr_stmt|;
name|fileIOEngine
operator|.
name|refreshFileConnection
argument_list|(
literal|0
argument_list|,
operator|new
name|IOException
argument_list|(
literal|"Test Exception"
argument_list|)
argument_list|)
expr_stmt|;
name|FileChannel
index|[]
name|reopenedFileChannels
init|=
name|fileIOEngine
operator|.
name|getFileChannels
argument_list|()
decl_stmt|;
name|FileChannel
name|reopenedFileChannel
init|=
name|reopenedFileChannels
index|[
literal|0
index|]
decl_stmt|;
name|assertNotEquals
argument_list|(
name|fileChannel
argument_list|,
name|reopenedFileChannel
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|fileChannels
operator|.
name|length
argument_list|,
name|reopenedFileChannels
operator|.
name|length
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|1
init|;
name|i
operator|<
name|fileChannels
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|assertEquals
argument_list|(
name|fileChannels
index|[
name|i
index|]
argument_list|,
name|reopenedFileChannels
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

