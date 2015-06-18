begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|nio
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
name|ByteArrayOutputStream
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
name|BufferOverflowException
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
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|util
operator|.
name|ByteBufferUtils
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
name|util
operator|.
name|Bytes
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
name|util
operator|.
name|Pair
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
name|TestMultiByteBuffer
block|{
annotation|@
name|Test
specifier|public
name|void
name|testWritesAndReads
parameter_list|()
block|{
comment|// Absolute reads
name|ByteBuffer
name|bb1
init|=
name|ByteBuffer
operator|.
name|allocate
argument_list|(
literal|15
argument_list|)
decl_stmt|;
name|ByteBuffer
name|bb2
init|=
name|ByteBuffer
operator|.
name|allocate
argument_list|(
literal|15
argument_list|)
decl_stmt|;
name|int
name|i1
init|=
literal|4
decl_stmt|;
name|bb1
operator|.
name|putInt
argument_list|(
name|i1
argument_list|)
expr_stmt|;
name|long
name|l1
init|=
literal|45L
decl_stmt|,
name|l2
init|=
literal|100L
decl_stmt|,
name|l3
init|=
literal|12345L
decl_stmt|;
name|bb1
operator|.
name|putLong
argument_list|(
name|l1
argument_list|)
expr_stmt|;
name|short
name|s1
init|=
literal|2
decl_stmt|;
name|bb1
operator|.
name|putShort
argument_list|(
name|s1
argument_list|)
expr_stmt|;
name|byte
index|[]
name|b
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|l2
argument_list|)
decl_stmt|;
name|bb1
operator|.
name|put
argument_list|(
name|b
argument_list|,
literal|0
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|bb2
operator|.
name|put
argument_list|(
name|b
argument_list|,
literal|1
argument_list|,
literal|7
argument_list|)
expr_stmt|;
name|bb2
operator|.
name|putLong
argument_list|(
name|l3
argument_list|)
expr_stmt|;
name|MultiByteBuffer
name|mbb
init|=
operator|new
name|MultiByteBuffer
argument_list|(
name|bb1
argument_list|,
name|bb2
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|l1
argument_list|,
name|mbb
operator|.
name|getLong
argument_list|(
literal|4
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|l2
argument_list|,
name|mbb
operator|.
name|getLong
argument_list|(
literal|14
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|l3
argument_list|,
name|mbb
operator|.
name|getLong
argument_list|(
literal|22
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|i1
argument_list|,
name|mbb
operator|.
name|getInt
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|s1
argument_list|,
name|mbb
operator|.
name|getShort
argument_list|(
literal|12
argument_list|)
argument_list|)
expr_stmt|;
comment|// Relative reads
name|assertEquals
argument_list|(
name|i1
argument_list|,
name|mbb
operator|.
name|getInt
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|l1
argument_list|,
name|mbb
operator|.
name|getLong
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|s1
argument_list|,
name|mbb
operator|.
name|getShort
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|l2
argument_list|,
name|mbb
operator|.
name|getLong
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|l3
argument_list|,
name|mbb
operator|.
name|getLong
argument_list|()
argument_list|)
expr_stmt|;
comment|// Absolute writes
name|bb1
operator|=
name|ByteBuffer
operator|.
name|allocate
argument_list|(
literal|15
argument_list|)
expr_stmt|;
name|bb2
operator|=
name|ByteBuffer
operator|.
name|allocate
argument_list|(
literal|15
argument_list|)
expr_stmt|;
name|mbb
operator|=
operator|new
name|MultiByteBuffer
argument_list|(
name|bb1
argument_list|,
name|bb2
argument_list|)
expr_stmt|;
name|byte
name|b1
init|=
literal|5
decl_stmt|,
name|b2
init|=
literal|31
decl_stmt|;
name|mbb
operator|.
name|put
argument_list|(
name|b1
argument_list|)
expr_stmt|;
name|mbb
operator|.
name|putLong
argument_list|(
name|l1
argument_list|)
expr_stmt|;
name|mbb
operator|.
name|putInt
argument_list|(
name|i1
argument_list|)
expr_stmt|;
name|mbb
operator|.
name|putLong
argument_list|(
name|l2
argument_list|)
expr_stmt|;
name|mbb
operator|.
name|put
argument_list|(
name|b2
argument_list|)
expr_stmt|;
name|mbb
operator|.
name|position
argument_list|(
name|mbb
operator|.
name|position
argument_list|()
operator|+
literal|2
argument_list|)
expr_stmt|;
try|try
block|{
name|mbb
operator|.
name|putLong
argument_list|(
name|l3
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"'Should have thrown BufferOverflowException"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|BufferOverflowException
name|e
parameter_list|)
block|{     }
name|mbb
operator|.
name|position
argument_list|(
name|mbb
operator|.
name|position
argument_list|()
operator|-
literal|2
argument_list|)
expr_stmt|;
name|mbb
operator|.
name|putLong
argument_list|(
name|l3
argument_list|)
expr_stmt|;
name|mbb
operator|.
name|rewind
argument_list|()
expr_stmt|;
name|assertEquals
argument_list|(
name|b1
argument_list|,
name|mbb
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|l1
argument_list|,
name|mbb
operator|.
name|getLong
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|i1
argument_list|,
name|mbb
operator|.
name|getInt
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|l2
argument_list|,
name|mbb
operator|.
name|getLong
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|b2
argument_list|,
name|mbb
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|l3
argument_list|,
name|mbb
operator|.
name|getLong
argument_list|()
argument_list|)
expr_stmt|;
name|mbb
operator|.
name|put
argument_list|(
literal|21
argument_list|,
name|b1
argument_list|)
expr_stmt|;
name|mbb
operator|.
name|position
argument_list|(
literal|21
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|b1
argument_list|,
name|mbb
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
name|mbb
operator|.
name|put
argument_list|(
name|b
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|l2
argument_list|,
name|mbb
operator|.
name|getLong
argument_list|(
literal|22
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testGetVlong
parameter_list|()
throws|throws
name|IOException
block|{
name|long
name|vlong
init|=
literal|453478
decl_stmt|;
name|ByteArrayOutputStream
name|baos
init|=
operator|new
name|ByteArrayOutputStream
argument_list|(
literal|10
argument_list|)
decl_stmt|;
name|DataOutput
name|out
init|=
operator|new
name|DataOutputStream
argument_list|(
name|baos
argument_list|)
decl_stmt|;
name|WritableUtils
operator|.
name|writeVLong
argument_list|(
name|out
argument_list|,
name|vlong
argument_list|)
expr_stmt|;
name|ByteBuffer
name|bb
init|=
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|baos
operator|.
name|toByteArray
argument_list|()
argument_list|)
decl_stmt|;
name|MultiByteBuffer
name|mbb
init|=
operator|new
name|MultiByteBuffer
argument_list|(
name|bb
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|vlong
argument_list|,
name|mbb
operator|.
name|getVLong
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testArrayBasedMethods
parameter_list|()
block|{
name|byte
index|[]
name|b
init|=
operator|new
name|byte
index|[
literal|15
index|]
decl_stmt|;
name|ByteBuffer
name|bb1
init|=
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|b
argument_list|,
literal|1
argument_list|,
literal|10
argument_list|)
operator|.
name|slice
argument_list|()
decl_stmt|;
name|ByteBuffer
name|bb2
init|=
name|ByteBuffer
operator|.
name|allocate
argument_list|(
literal|15
argument_list|)
decl_stmt|;
name|MultiByteBuffer
name|mbb1
init|=
operator|new
name|MultiByteBuffer
argument_list|(
name|bb1
argument_list|,
name|bb2
argument_list|)
decl_stmt|;
name|assertFalse
argument_list|(
name|mbb1
operator|.
name|hasArray
argument_list|()
argument_list|)
expr_stmt|;
try|try
block|{
name|mbb1
operator|.
name|array
argument_list|()
expr_stmt|;
name|fail
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|UnsupportedOperationException
name|e
parameter_list|)
block|{     }
try|try
block|{
name|mbb1
operator|.
name|arrayOffset
argument_list|()
expr_stmt|;
name|fail
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|UnsupportedOperationException
name|e
parameter_list|)
block|{     }
name|mbb1
operator|=
operator|new
name|MultiByteBuffer
argument_list|(
name|bb1
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|mbb1
operator|.
name|hasArray
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|mbb1
operator|.
name|arrayOffset
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|b
argument_list|,
name|mbb1
operator|.
name|array
argument_list|()
argument_list|)
expr_stmt|;
name|mbb1
operator|=
operator|new
name|MultiByteBuffer
argument_list|(
name|ByteBuffer
operator|.
name|allocateDirect
argument_list|(
literal|10
argument_list|)
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|mbb1
operator|.
name|hasArray
argument_list|()
argument_list|)
expr_stmt|;
try|try
block|{
name|mbb1
operator|.
name|array
argument_list|()
expr_stmt|;
name|fail
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|UnsupportedOperationException
name|e
parameter_list|)
block|{     }
try|try
block|{
name|mbb1
operator|.
name|arrayOffset
argument_list|()
expr_stmt|;
name|fail
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|UnsupportedOperationException
name|e
parameter_list|)
block|{     }
block|}
annotation|@
name|Test
specifier|public
name|void
name|testMarkAndReset
parameter_list|()
block|{
name|ByteBuffer
name|bb1
init|=
name|ByteBuffer
operator|.
name|allocateDirect
argument_list|(
literal|15
argument_list|)
decl_stmt|;
name|ByteBuffer
name|bb2
init|=
name|ByteBuffer
operator|.
name|allocateDirect
argument_list|(
literal|15
argument_list|)
decl_stmt|;
name|bb1
operator|.
name|putInt
argument_list|(
literal|4
argument_list|)
expr_stmt|;
name|long
name|l1
init|=
literal|45L
decl_stmt|,
name|l2
init|=
literal|100L
decl_stmt|,
name|l3
init|=
literal|12345L
decl_stmt|;
name|bb1
operator|.
name|putLong
argument_list|(
name|l1
argument_list|)
expr_stmt|;
name|bb1
operator|.
name|putShort
argument_list|(
operator|(
name|short
operator|)
literal|2
argument_list|)
expr_stmt|;
name|byte
index|[]
name|b
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|l2
argument_list|)
decl_stmt|;
name|bb1
operator|.
name|put
argument_list|(
name|b
argument_list|,
literal|0
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|bb2
operator|.
name|put
argument_list|(
name|b
argument_list|,
literal|1
argument_list|,
literal|7
argument_list|)
expr_stmt|;
name|bb2
operator|.
name|putLong
argument_list|(
name|l3
argument_list|)
expr_stmt|;
name|MultiByteBuffer
name|multi
init|=
operator|new
name|MultiByteBuffer
argument_list|(
name|bb1
argument_list|,
name|bb2
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|4
argument_list|,
name|multi
operator|.
name|getInt
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|l1
argument_list|,
name|multi
operator|.
name|getLong
argument_list|()
argument_list|)
expr_stmt|;
name|multi
operator|.
name|mark
argument_list|()
expr_stmt|;
name|assertEquals
argument_list|(
operator|(
name|short
operator|)
literal|2
argument_list|,
name|multi
operator|.
name|getShort
argument_list|()
argument_list|)
expr_stmt|;
name|multi
operator|.
name|reset
argument_list|()
expr_stmt|;
name|assertEquals
argument_list|(
operator|(
name|short
operator|)
literal|2
argument_list|,
name|multi
operator|.
name|getShort
argument_list|()
argument_list|)
expr_stmt|;
name|multi
operator|.
name|mark
argument_list|()
expr_stmt|;
name|assertEquals
argument_list|(
name|l2
argument_list|,
name|multi
operator|.
name|getLong
argument_list|()
argument_list|)
expr_stmt|;
name|multi
operator|.
name|reset
argument_list|()
expr_stmt|;
name|assertEquals
argument_list|(
name|l2
argument_list|,
name|multi
operator|.
name|getLong
argument_list|()
argument_list|)
expr_stmt|;
name|multi
operator|.
name|mark
argument_list|()
expr_stmt|;
name|assertEquals
argument_list|(
name|l3
argument_list|,
name|multi
operator|.
name|getLong
argument_list|()
argument_list|)
expr_stmt|;
name|multi
operator|.
name|reset
argument_list|()
expr_stmt|;
name|assertEquals
argument_list|(
name|l3
argument_list|,
name|multi
operator|.
name|getLong
argument_list|()
argument_list|)
expr_stmt|;
comment|// Try absolute gets with mark and reset
name|multi
operator|.
name|mark
argument_list|()
expr_stmt|;
name|assertEquals
argument_list|(
name|l2
argument_list|,
name|multi
operator|.
name|getLong
argument_list|(
literal|14
argument_list|)
argument_list|)
expr_stmt|;
name|multi
operator|.
name|reset
argument_list|()
expr_stmt|;
name|assertEquals
argument_list|(
name|l3
argument_list|,
name|multi
operator|.
name|getLong
argument_list|(
literal|22
argument_list|)
argument_list|)
expr_stmt|;
comment|// Just reset to see what happens
name|multi
operator|.
name|reset
argument_list|()
expr_stmt|;
name|assertEquals
argument_list|(
name|l2
argument_list|,
name|multi
operator|.
name|getLong
argument_list|(
literal|14
argument_list|)
argument_list|)
expr_stmt|;
name|multi
operator|.
name|mark
argument_list|()
expr_stmt|;
name|assertEquals
argument_list|(
name|l3
argument_list|,
name|multi
operator|.
name|getLong
argument_list|(
literal|22
argument_list|)
argument_list|)
expr_stmt|;
name|multi
operator|.
name|reset
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testSkipNBytes
parameter_list|()
block|{
name|ByteBuffer
name|bb1
init|=
name|ByteBuffer
operator|.
name|allocate
argument_list|(
literal|15
argument_list|)
decl_stmt|;
name|ByteBuffer
name|bb2
init|=
name|ByteBuffer
operator|.
name|allocate
argument_list|(
literal|15
argument_list|)
decl_stmt|;
name|bb1
operator|.
name|putInt
argument_list|(
literal|4
argument_list|)
expr_stmt|;
name|long
name|l1
init|=
literal|45L
decl_stmt|,
name|l2
init|=
literal|100L
decl_stmt|,
name|l3
init|=
literal|12345L
decl_stmt|;
name|bb1
operator|.
name|putLong
argument_list|(
name|l1
argument_list|)
expr_stmt|;
name|bb1
operator|.
name|putShort
argument_list|(
operator|(
name|short
operator|)
literal|2
argument_list|)
expr_stmt|;
name|byte
index|[]
name|b
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|l2
argument_list|)
decl_stmt|;
name|bb1
operator|.
name|put
argument_list|(
name|b
argument_list|,
literal|0
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|bb2
operator|.
name|put
argument_list|(
name|b
argument_list|,
literal|1
argument_list|,
literal|7
argument_list|)
expr_stmt|;
name|bb2
operator|.
name|putLong
argument_list|(
name|l3
argument_list|)
expr_stmt|;
name|MultiByteBuffer
name|multi
init|=
operator|new
name|MultiByteBuffer
argument_list|(
name|bb1
argument_list|,
name|bb2
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|4
argument_list|,
name|multi
operator|.
name|getInt
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|l1
argument_list|,
name|multi
operator|.
name|getLong
argument_list|()
argument_list|)
expr_stmt|;
name|multi
operator|.
name|skip
argument_list|(
literal|10
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|l3
argument_list|,
name|multi
operator|.
name|getLong
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testMoveBack
parameter_list|()
block|{
name|ByteBuffer
name|bb1
init|=
name|ByteBuffer
operator|.
name|allocate
argument_list|(
literal|15
argument_list|)
decl_stmt|;
name|ByteBuffer
name|bb2
init|=
name|ByteBuffer
operator|.
name|allocate
argument_list|(
literal|15
argument_list|)
decl_stmt|;
name|bb1
operator|.
name|putInt
argument_list|(
literal|4
argument_list|)
expr_stmt|;
name|long
name|l1
init|=
literal|45L
decl_stmt|,
name|l2
init|=
literal|100L
decl_stmt|,
name|l3
init|=
literal|12345L
decl_stmt|;
name|bb1
operator|.
name|putLong
argument_list|(
name|l1
argument_list|)
expr_stmt|;
name|bb1
operator|.
name|putShort
argument_list|(
operator|(
name|short
operator|)
literal|2
argument_list|)
expr_stmt|;
name|byte
index|[]
name|b
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|l2
argument_list|)
decl_stmt|;
name|bb1
operator|.
name|put
argument_list|(
name|b
argument_list|,
literal|0
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|bb2
operator|.
name|put
argument_list|(
name|b
argument_list|,
literal|1
argument_list|,
literal|7
argument_list|)
expr_stmt|;
name|bb2
operator|.
name|putLong
argument_list|(
name|l3
argument_list|)
expr_stmt|;
name|MultiByteBuffer
name|multi
init|=
operator|new
name|MultiByteBuffer
argument_list|(
name|bb1
argument_list|,
name|bb2
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|4
argument_list|,
name|multi
operator|.
name|getInt
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|l1
argument_list|,
name|multi
operator|.
name|getLong
argument_list|()
argument_list|)
expr_stmt|;
name|multi
operator|.
name|skip
argument_list|(
literal|10
argument_list|)
expr_stmt|;
name|multi
operator|.
name|moveBack
argument_list|(
literal|4
argument_list|)
expr_stmt|;
name|multi
operator|.
name|moveBack
argument_list|(
literal|6
argument_list|)
expr_stmt|;
name|multi
operator|.
name|moveBack
argument_list|(
literal|8
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|l1
argument_list|,
name|multi
operator|.
name|getLong
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testSubBuffer
parameter_list|()
block|{
name|ByteBuffer
name|bb1
init|=
name|ByteBuffer
operator|.
name|allocateDirect
argument_list|(
literal|10
argument_list|)
decl_stmt|;
name|ByteBuffer
name|bb2
init|=
name|ByteBuffer
operator|.
name|allocateDirect
argument_list|(
literal|10
argument_list|)
decl_stmt|;
name|MultiByteBuffer
name|multi
init|=
operator|new
name|MultiByteBuffer
argument_list|(
name|bb1
argument_list|,
name|bb2
argument_list|)
decl_stmt|;
name|long
name|l1
init|=
literal|1234L
decl_stmt|,
name|l2
init|=
literal|100L
decl_stmt|;
name|multi
operator|.
name|putLong
argument_list|(
name|l1
argument_list|)
expr_stmt|;
name|multi
operator|.
name|putLong
argument_list|(
name|l2
argument_list|)
expr_stmt|;
name|multi
operator|.
name|rewind
argument_list|()
expr_stmt|;
name|ByteBuffer
name|sub
init|=
name|multi
operator|.
name|asSubBuffer
argument_list|(
name|Bytes
operator|.
name|SIZEOF_LONG
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|bb1
operator|==
name|sub
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|l1
argument_list|,
name|ByteBufferUtils
operator|.
name|toLong
argument_list|(
name|sub
argument_list|,
name|sub
operator|.
name|position
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|multi
operator|.
name|skip
argument_list|(
name|Bytes
operator|.
name|SIZEOF_LONG
argument_list|)
expr_stmt|;
name|sub
operator|=
name|multi
operator|.
name|asSubBuffer
argument_list|(
name|Bytes
operator|.
name|SIZEOF_LONG
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|bb1
operator|==
name|sub
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|bb2
operator|==
name|sub
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|l2
argument_list|,
name|ByteBufferUtils
operator|.
name|toLong
argument_list|(
name|sub
argument_list|,
name|sub
operator|.
name|position
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|multi
operator|.
name|rewind
argument_list|()
expr_stmt|;
name|Pair
argument_list|<
name|ByteBuffer
argument_list|,
name|Integer
argument_list|>
name|p
init|=
name|multi
operator|.
name|asSubBuffer
argument_list|(
literal|8
argument_list|,
name|Bytes
operator|.
name|SIZEOF_LONG
argument_list|)
decl_stmt|;
name|assertFalse
argument_list|(
name|bb1
operator|==
name|p
operator|.
name|getFirst
argument_list|()
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|bb2
operator|==
name|p
operator|.
name|getFirst
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|p
operator|.
name|getSecond
argument_list|()
operator|.
name|intValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|l2
argument_list|,
name|ByteBufferUtils
operator|.
name|toLong
argument_list|(
name|sub
argument_list|,
name|p
operator|.
name|getSecond
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testSliceDuplicateMethods
parameter_list|()
throws|throws
name|Exception
block|{
name|ByteBuffer
name|bb1
init|=
name|ByteBuffer
operator|.
name|allocateDirect
argument_list|(
literal|10
argument_list|)
decl_stmt|;
name|ByteBuffer
name|bb2
init|=
name|ByteBuffer
operator|.
name|allocateDirect
argument_list|(
literal|15
argument_list|)
decl_stmt|;
name|MultiByteBuffer
name|multi
init|=
operator|new
name|MultiByteBuffer
argument_list|(
name|bb1
argument_list|,
name|bb2
argument_list|)
decl_stmt|;
name|long
name|l1
init|=
literal|1234L
decl_stmt|,
name|l2
init|=
literal|100L
decl_stmt|;
name|multi
operator|.
name|put
argument_list|(
operator|(
name|byte
operator|)
literal|2
argument_list|)
expr_stmt|;
name|multi
operator|.
name|putLong
argument_list|(
name|l1
argument_list|)
expr_stmt|;
name|multi
operator|.
name|putLong
argument_list|(
name|l2
argument_list|)
expr_stmt|;
name|multi
operator|.
name|putInt
argument_list|(
literal|45
argument_list|)
expr_stmt|;
name|multi
operator|.
name|position
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|multi
operator|.
name|limit
argument_list|(
name|multi
operator|.
name|position
argument_list|()
operator|+
operator|(
literal|2
operator|*
name|Bytes
operator|.
name|SIZEOF_LONG
operator|)
argument_list|)
expr_stmt|;
name|MultiByteBuffer
name|sliced
init|=
name|multi
operator|.
name|slice
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|sliced
operator|.
name|position
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
operator|(
literal|2
operator|*
name|Bytes
operator|.
name|SIZEOF_LONG
operator|)
argument_list|,
name|sliced
operator|.
name|limit
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|l1
argument_list|,
name|sliced
operator|.
name|getLong
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|l2
argument_list|,
name|sliced
operator|.
name|getLong
argument_list|()
argument_list|)
expr_stmt|;
name|MultiByteBuffer
name|dup
init|=
name|multi
operator|.
name|duplicate
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|dup
operator|.
name|position
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|dup
operator|.
name|position
argument_list|()
operator|+
operator|(
literal|2
operator|*
name|Bytes
operator|.
name|SIZEOF_LONG
operator|)
argument_list|,
name|dup
operator|.
name|limit
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|l1
argument_list|,
name|dup
operator|.
name|getLong
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|l2
argument_list|,
name|dup
operator|.
name|getLong
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

