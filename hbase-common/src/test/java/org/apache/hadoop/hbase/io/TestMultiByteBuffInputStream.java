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
name|TestMultiByteBuffInputStream
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
name|TestMultiByteBuffInputStream
operator|.
name|class
argument_list|)
decl_stmt|;
annotation|@
name|Test
specifier|public
name|void
name|testReads
parameter_list|()
throws|throws
name|Exception
block|{
name|ByteArrayOutputStream
name|bos
init|=
operator|new
name|ByteArrayOutputStream
argument_list|(
literal|100
argument_list|)
decl_stmt|;
name|DataOutputStream
name|dos
init|=
operator|new
name|DataOutputStream
argument_list|(
name|bos
argument_list|)
decl_stmt|;
name|String
name|s
init|=
literal|"test"
decl_stmt|;
name|int
name|i
init|=
literal|128
decl_stmt|;
name|dos
operator|.
name|write
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|dos
operator|.
name|writeInt
argument_list|(
name|i
argument_list|)
expr_stmt|;
name|dos
operator|.
name|writeBytes
argument_list|(
name|s
argument_list|)
expr_stmt|;
name|dos
operator|.
name|writeLong
argument_list|(
literal|12345L
argument_list|)
expr_stmt|;
name|dos
operator|.
name|writeShort
argument_list|(
literal|2
argument_list|)
expr_stmt|;
name|dos
operator|.
name|flush
argument_list|()
expr_stmt|;
name|ByteBuffer
name|bb
init|=
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|bos
operator|.
name|toByteArray
argument_list|()
argument_list|)
decl_stmt|;
comment|// bbis contains 19 bytes
comment|// 1 byte, 4 bytes int, 4 bytes string, 8 bytes long and 2 bytes short
name|ByteBuffInputStream
name|bbis
init|=
operator|new
name|ByteBuffInputStream
argument_list|(
operator|new
name|MultiByteBuff
argument_list|(
name|bb
argument_list|)
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|15
operator|+
name|s
operator|.
name|length
argument_list|()
argument_list|,
name|bbis
operator|.
name|available
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|bbis
operator|.
name|read
argument_list|()
argument_list|)
expr_stmt|;
name|byte
index|[]
name|ib
init|=
operator|new
name|byte
index|[
literal|4
index|]
decl_stmt|;
name|bbis
operator|.
name|read
argument_list|(
name|ib
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|i
argument_list|,
name|Bytes
operator|.
name|toInt
argument_list|(
name|ib
argument_list|)
argument_list|)
expr_stmt|;
name|byte
index|[]
name|sb
init|=
operator|new
name|byte
index|[
name|s
operator|.
name|length
argument_list|()
index|]
decl_stmt|;
name|bbis
operator|.
name|read
argument_list|(
name|sb
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|s
argument_list|,
name|Bytes
operator|.
name|toString
argument_list|(
name|sb
argument_list|)
argument_list|)
expr_stmt|;
name|byte
index|[]
name|lb
init|=
operator|new
name|byte
index|[
literal|8
index|]
decl_stmt|;
name|bbis
operator|.
name|read
argument_list|(
name|lb
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|12345
argument_list|,
name|Bytes
operator|.
name|toLong
argument_list|(
name|lb
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|bbis
operator|.
name|available
argument_list|()
argument_list|)
expr_stmt|;
name|ib
operator|=
operator|new
name|byte
index|[
literal|4
index|]
expr_stmt|;
name|int
name|read
init|=
name|bbis
operator|.
name|read
argument_list|(
name|ib
argument_list|,
literal|0
argument_list|,
name|ib
operator|.
name|length
argument_list|)
decl_stmt|;
comment|// We dont have 4 bytes remainig but only 2. So onlt those should be returned back
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|read
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|Bytes
operator|.
name|toShort
argument_list|(
name|ib
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|bbis
operator|.
name|available
argument_list|()
argument_list|)
expr_stmt|;
comment|// At end. The read() should return -1
name|assertEquals
argument_list|(
operator|-
literal|1
argument_list|,
name|bbis
operator|.
name|read
argument_list|()
argument_list|)
expr_stmt|;
name|bbis
operator|.
name|close
argument_list|()
expr_stmt|;
name|bb
operator|=
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|bos
operator|.
name|toByteArray
argument_list|()
argument_list|)
expr_stmt|;
name|bbis
operator|=
operator|new
name|ByteBuffInputStream
argument_list|(
operator|new
name|MultiByteBuff
argument_list|(
name|bb
argument_list|)
argument_list|)
expr_stmt|;
name|DataInputStream
name|dis
init|=
operator|new
name|DataInputStream
argument_list|(
name|bbis
argument_list|)
decl_stmt|;
name|dis
operator|.
name|read
argument_list|()
expr_stmt|;
name|assertEquals
argument_list|(
name|i
argument_list|,
name|dis
operator|.
name|readInt
argument_list|()
argument_list|)
expr_stmt|;
name|dis
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

