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
name|filter
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
name|assertTrue
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|regex
operator|.
name|Pattern
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
name|hbase
operator|.
name|protobuf
operator|.
name|ProtobufUtil
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
name|TestComparatorSerialization
block|{
annotation|@
name|Test
specifier|public
name|void
name|testBinaryComparator
parameter_list|()
throws|throws
name|Exception
block|{
name|BinaryComparator
name|binaryComparator
init|=
operator|new
name|BinaryComparator
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"binaryComparator"
argument_list|)
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|binaryComparator
operator|.
name|areSerializedFieldsEqual
argument_list|(
name|ProtobufUtil
operator|.
name|toComparator
argument_list|(
name|ProtobufUtil
operator|.
name|toComparator
argument_list|(
name|binaryComparator
argument_list|)
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testBinaryPrefixComparator
parameter_list|()
throws|throws
name|Exception
block|{
name|BinaryPrefixComparator
name|binaryPrefixComparator
init|=
operator|new
name|BinaryPrefixComparator
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"binaryPrefixComparator"
argument_list|)
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|binaryPrefixComparator
operator|.
name|areSerializedFieldsEqual
argument_list|(
name|ProtobufUtil
operator|.
name|toComparator
argument_list|(
name|ProtobufUtil
operator|.
name|toComparator
argument_list|(
name|binaryPrefixComparator
argument_list|)
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testBitComparator
parameter_list|()
throws|throws
name|Exception
block|{
name|BitComparator
name|bitComparator
init|=
operator|new
name|BitComparator
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"bitComparator"
argument_list|)
argument_list|,
name|BitComparator
operator|.
name|BitwiseOp
operator|.
name|XOR
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|bitComparator
operator|.
name|areSerializedFieldsEqual
argument_list|(
name|ProtobufUtil
operator|.
name|toComparator
argument_list|(
name|ProtobufUtil
operator|.
name|toComparator
argument_list|(
name|bitComparator
argument_list|)
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testNullComparator
parameter_list|()
throws|throws
name|Exception
block|{
name|NullComparator
name|nullComparator
init|=
operator|new
name|NullComparator
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
name|nullComparator
operator|.
name|areSerializedFieldsEqual
argument_list|(
name|ProtobufUtil
operator|.
name|toComparator
argument_list|(
name|ProtobufUtil
operator|.
name|toComparator
argument_list|(
name|nullComparator
argument_list|)
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testRegexStringComparator
parameter_list|()
throws|throws
name|Exception
block|{
comment|// test without specifying flags
name|RegexStringComparator
name|regexStringComparator
init|=
operator|new
name|RegexStringComparator
argument_list|(
literal|".+-2"
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|regexStringComparator
operator|.
name|areSerializedFieldsEqual
argument_list|(
name|ProtobufUtil
operator|.
name|toComparator
argument_list|(
name|ProtobufUtil
operator|.
name|toComparator
argument_list|(
name|regexStringComparator
argument_list|)
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
comment|// test with specifying flags
name|regexStringComparator
operator|=
operator|new
name|RegexStringComparator
argument_list|(
literal|"regex"
argument_list|,
name|Pattern
operator|.
name|CASE_INSENSITIVE
operator||
name|Pattern
operator|.
name|DOTALL
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testSubstringComparator
parameter_list|()
throws|throws
name|Exception
block|{
name|SubstringComparator
name|substringComparator
init|=
operator|new
name|SubstringComparator
argument_list|(
literal|"substr"
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|substringComparator
operator|.
name|areSerializedFieldsEqual
argument_list|(
name|ProtobufUtil
operator|.
name|toComparator
argument_list|(
name|ProtobufUtil
operator|.
name|toComparator
argument_list|(
name|substringComparator
argument_list|)
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

