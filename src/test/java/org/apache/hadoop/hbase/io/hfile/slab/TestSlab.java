begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2011 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|slab
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
name|*
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
name|SmallTests
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|*
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
comment|/**Test cases for Slab.java*/
end_comment

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
name|TestSlab
block|{
specifier|static
specifier|final
name|int
name|BLOCKSIZE
init|=
literal|1000
decl_stmt|;
specifier|static
specifier|final
name|int
name|NUMBLOCKS
init|=
literal|100
decl_stmt|;
name|Slab
name|testSlab
decl_stmt|;
name|ByteBuffer
index|[]
name|buffers
init|=
operator|new
name|ByteBuffer
index|[
name|NUMBLOCKS
index|]
decl_stmt|;
annotation|@
name|Before
specifier|public
name|void
name|setUp
parameter_list|()
block|{
name|testSlab
operator|=
operator|new
name|Slab
argument_list|(
name|BLOCKSIZE
argument_list|,
name|NUMBLOCKS
argument_list|)
expr_stmt|;
block|}
annotation|@
name|After
specifier|public
name|void
name|tearDown
parameter_list|()
block|{
name|testSlab
operator|.
name|shutdown
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testBasicFunctionality
parameter_list|()
throws|throws
name|InterruptedException
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
name|NUMBLOCKS
condition|;
name|i
operator|++
control|)
block|{
name|buffers
index|[
name|i
index|]
operator|=
name|testSlab
operator|.
name|alloc
argument_list|(
name|BLOCKSIZE
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|BLOCKSIZE
argument_list|,
name|buffers
index|[
name|i
index|]
operator|.
name|limit
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|// write an unique integer to each allocated buffer.
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|NUMBLOCKS
condition|;
name|i
operator|++
control|)
block|{
name|buffers
index|[
name|i
index|]
operator|.
name|putInt
argument_list|(
name|i
argument_list|)
expr_stmt|;
block|}
comment|// make sure the bytebuffers remain unique (the slab allocator hasn't
comment|// allocated the same chunk of memory twice)
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|NUMBLOCKS
condition|;
name|i
operator|++
control|)
block|{
name|buffers
index|[
name|i
index|]
operator|.
name|putInt
argument_list|(
name|i
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
name|NUMBLOCKS
condition|;
name|i
operator|++
control|)
block|{
name|testSlab
operator|.
name|free
argument_list|(
name|buffers
index|[
name|i
index|]
argument_list|)
expr_stmt|;
comment|// free all the buffers.
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
name|NUMBLOCKS
condition|;
name|i
operator|++
control|)
block|{
name|buffers
index|[
name|i
index|]
operator|=
name|testSlab
operator|.
name|alloc
argument_list|(
name|BLOCKSIZE
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|BLOCKSIZE
argument_list|,
name|buffers
index|[
name|i
index|]
operator|.
name|limit
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

