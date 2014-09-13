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
name|TestSimplePositionedMutableByteRange
block|{
annotation|@
name|Test
specifier|public
name|void
name|testPosition
parameter_list|()
block|{
name|PositionedByteRange
name|r
init|=
operator|new
name|SimplePositionedMutableByteRange
argument_list|(
operator|new
name|byte
index|[
literal|5
index|]
argument_list|,
literal|1
argument_list|,
literal|3
argument_list|)
decl_stmt|;
comment|// exercise single-byte put
name|r
operator|.
name|put
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"f"
argument_list|)
index|[
literal|0
index|]
argument_list|)
operator|.
name|put
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"o"
argument_list|)
index|[
literal|0
index|]
argument_list|)
operator|.
name|put
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"o"
argument_list|)
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|3
argument_list|,
name|r
operator|.
name|getPosition
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertArrayEquals
argument_list|(
operator|new
name|byte
index|[]
block|{
literal|0
block|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"f"
argument_list|)
index|[
literal|0
index|]
block|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"o"
argument_list|)
index|[
literal|0
index|]
block|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"o"
argument_list|)
index|[
literal|0
index|]
block|,
literal|0
block|}
argument_list|,
name|r
operator|.
name|getBytes
argument_list|()
argument_list|)
expr_stmt|;
comment|// exercise multi-byte put
name|r
operator|.
name|setPosition
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|r
operator|.
name|put
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"f"
argument_list|)
argument_list|)
operator|.
name|put
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"o"
argument_list|)
argument_list|)
operator|.
name|put
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"o"
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|3
argument_list|,
name|r
operator|.
name|getPosition
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertArrayEquals
argument_list|(
operator|new
name|byte
index|[]
block|{
literal|0
block|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"f"
argument_list|)
index|[
literal|0
index|]
block|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"o"
argument_list|)
index|[
literal|0
index|]
block|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"o"
argument_list|)
index|[
literal|0
index|]
block|,
literal|0
block|}
argument_list|,
name|r
operator|.
name|getBytes
argument_list|()
argument_list|)
expr_stmt|;
comment|// exercise single-byte get
name|r
operator|.
name|setPosition
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"f"
argument_list|)
index|[
literal|0
index|]
argument_list|,
name|r
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"o"
argument_list|)
index|[
literal|0
index|]
argument_list|,
name|r
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"o"
argument_list|)
index|[
literal|0
index|]
argument_list|,
name|r
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
name|r
operator|.
name|setPosition
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"o"
argument_list|)
index|[
literal|0
index|]
argument_list|,
name|r
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
comment|// exercise multi-byte get
name|r
operator|.
name|setPosition
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|byte
index|[]
name|dst
init|=
operator|new
name|byte
index|[
literal|3
index|]
decl_stmt|;
name|r
operator|.
name|get
argument_list|(
name|dst
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertArrayEquals
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"foo"
argument_list|)
argument_list|,
name|dst
argument_list|)
expr_stmt|;
comment|// set position to the end of the range; this should not throw.
name|r
operator|.
name|setPosition
argument_list|(
literal|3
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testPutAndGetPrimitiveTypes
parameter_list|()
throws|throws
name|Exception
block|{
name|PositionedByteRange
name|pbr
init|=
operator|new
name|SimplePositionedMutableByteRange
argument_list|(
literal|100
argument_list|)
decl_stmt|;
name|int
name|i1
init|=
literal|18
decl_stmt|,
name|i2
init|=
literal|2
decl_stmt|;
name|short
name|s1
init|=
literal|0
decl_stmt|;
name|long
name|l1
init|=
literal|1234L
decl_stmt|;
name|pbr
operator|.
name|putInt
argument_list|(
name|i1
argument_list|)
expr_stmt|;
name|pbr
operator|.
name|putInt
argument_list|(
name|i2
argument_list|)
expr_stmt|;
name|pbr
operator|.
name|putShort
argument_list|(
name|s1
argument_list|)
expr_stmt|;
name|pbr
operator|.
name|putLong
argument_list|(
name|l1
argument_list|)
expr_stmt|;
name|pbr
operator|.
name|putVLong
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|pbr
operator|.
name|putVLong
argument_list|(
name|l1
argument_list|)
expr_stmt|;
name|pbr
operator|.
name|putVLong
argument_list|(
name|Long
operator|.
name|MAX_VALUE
argument_list|)
expr_stmt|;
name|pbr
operator|.
name|putVLong
argument_list|(
name|Long
operator|.
name|MIN_VALUE
argument_list|)
expr_stmt|;
comment|// rewind
name|pbr
operator|.
name|setPosition
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|i1
argument_list|,
name|pbr
operator|.
name|getInt
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|i2
argument_list|,
name|pbr
operator|.
name|getInt
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|s1
argument_list|,
name|pbr
operator|.
name|getShort
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|l1
argument_list|,
name|pbr
operator|.
name|getLong
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|pbr
operator|.
name|getVLong
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|l1
argument_list|,
name|pbr
operator|.
name|getVLong
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|Long
operator|.
name|MAX_VALUE
argument_list|,
name|pbr
operator|.
name|getVLong
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|Long
operator|.
name|MIN_VALUE
argument_list|,
name|pbr
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
name|testPutGetAPIsCompareWithBBAPIs
parameter_list|()
throws|throws
name|Exception
block|{
comment|// confirm that the long/int/short writing is same as BBs
name|PositionedByteRange
name|pbr
init|=
operator|new
name|SimplePositionedMutableByteRange
argument_list|(
literal|100
argument_list|)
decl_stmt|;
name|int
name|i1
init|=
operator|-
literal|234
decl_stmt|,
name|i2
init|=
literal|2
decl_stmt|;
name|short
name|s1
init|=
literal|0
decl_stmt|;
name|long
name|l1
init|=
literal|1234L
decl_stmt|;
name|pbr
operator|.
name|putInt
argument_list|(
name|i1
argument_list|)
expr_stmt|;
name|pbr
operator|.
name|putShort
argument_list|(
name|s1
argument_list|)
expr_stmt|;
name|pbr
operator|.
name|putInt
argument_list|(
name|i2
argument_list|)
expr_stmt|;
name|pbr
operator|.
name|putLong
argument_list|(
name|l1
argument_list|)
expr_stmt|;
comment|// rewind
name|pbr
operator|.
name|setPosition
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|i1
argument_list|,
name|pbr
operator|.
name|getInt
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|s1
argument_list|,
name|pbr
operator|.
name|getShort
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|i2
argument_list|,
name|pbr
operator|.
name|getInt
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|l1
argument_list|,
name|pbr
operator|.
name|getLong
argument_list|()
argument_list|)
expr_stmt|;
comment|// Read back using BB APIs
name|ByteBuffer
name|bb
init|=
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|pbr
operator|.
name|getBytes
argument_list|()
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|i1
argument_list|,
name|bb
operator|.
name|getInt
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|s1
argument_list|,
name|bb
operator|.
name|getShort
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|i2
argument_list|,
name|bb
operator|.
name|getInt
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|l1
argument_list|,
name|bb
operator|.
name|getLong
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

