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
name|TestSingleByteBuff
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
name|TestSingleByteBuff
operator|.
name|class
argument_list|)
decl_stmt|;
annotation|@
name|Test
specifier|public
name|void
name|testPositionalReads
parameter_list|()
block|{
comment|// Off heap buffer
name|testPositionalReads
argument_list|(
name|ByteBuffer
operator|.
name|allocateDirect
argument_list|(
literal|15
argument_list|)
argument_list|)
expr_stmt|;
comment|// On heap buffer
name|testPositionalReads
argument_list|(
name|ByteBuffer
operator|.
name|allocate
argument_list|(
literal|15
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|testPositionalReads
parameter_list|(
name|ByteBuffer
name|bb
parameter_list|)
block|{
name|int
name|i
init|=
literal|9
decl_stmt|;
name|short
name|s
init|=
literal|5
decl_stmt|;
name|byte
name|b
init|=
literal|2
decl_stmt|;
name|long
name|l
init|=
literal|1234L
decl_stmt|;
name|bb
operator|.
name|putInt
argument_list|(
name|i
argument_list|)
expr_stmt|;
name|bb
operator|.
name|putLong
argument_list|(
name|l
argument_list|)
expr_stmt|;
name|bb
operator|.
name|put
argument_list|(
name|b
argument_list|)
expr_stmt|;
name|bb
operator|.
name|putShort
argument_list|(
name|s
argument_list|)
expr_stmt|;
name|SingleByteBuff
name|sbb
init|=
operator|new
name|SingleByteBuff
argument_list|(
name|bb
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|i
argument_list|,
name|sbb
operator|.
name|getInt
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|l
argument_list|,
name|sbb
operator|.
name|getLong
argument_list|(
literal|4
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|b
argument_list|,
name|sbb
operator|.
name|get
argument_list|(
literal|12
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|s
argument_list|,
name|sbb
operator|.
name|getShort
argument_list|(
literal|13
argument_list|)
argument_list|)
expr_stmt|;
name|sbb
operator|.
name|rewind
argument_list|()
expr_stmt|;
name|assertEquals
argument_list|(
name|i
argument_list|,
name|sbb
operator|.
name|getIntAfterPosition
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|l
argument_list|,
name|sbb
operator|.
name|getLongAfterPosition
argument_list|(
literal|4
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|b
argument_list|,
name|sbb
operator|.
name|getByteAfterPosition
argument_list|(
literal|12
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|s
argument_list|,
name|sbb
operator|.
name|getShortAfterPosition
argument_list|(
literal|13
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

