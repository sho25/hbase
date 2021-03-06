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
name|types
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
name|Order
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
name|PositionedByteRange
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
name|SimplePositionedMutableByteRange
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
name|TestOrderedBlobVar
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
name|TestOrderedBlobVar
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
index|[]
name|VALUES
init|=
operator|new
name|byte
index|[]
index|[]
block|{
literal|null
block|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|""
argument_list|)
block|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"1"
argument_list|)
block|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"22"
argument_list|)
block|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"333"
argument_list|)
block|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"4444"
argument_list|)
block|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"55555"
argument_list|)
block|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"666666"
argument_list|)
block|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"7777777"
argument_list|)
block|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"88888888"
argument_list|)
block|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"999999999"
argument_list|)
block|,   }
decl_stmt|;
annotation|@
name|Test
specifier|public
name|void
name|testEncodedLength
parameter_list|()
block|{
specifier|final
name|PositionedByteRange
name|buff
init|=
operator|new
name|SimplePositionedMutableByteRange
argument_list|(
literal|20
argument_list|)
decl_stmt|;
for|for
control|(
specifier|final
name|DataType
argument_list|<
name|byte
index|[]
argument_list|>
name|type
range|:
operator|new
name|OrderedBlobVar
index|[]
block|{
operator|new
name|OrderedBlobVar
argument_list|(
name|Order
operator|.
name|ASCENDING
argument_list|)
block|,
operator|new
name|OrderedBlobVar
argument_list|(
name|Order
operator|.
name|DESCENDING
argument_list|)
block|}
control|)
block|{
for|for
control|(
specifier|final
name|byte
index|[]
name|val
range|:
name|VALUES
control|)
block|{
name|buff
operator|.
name|setPosition
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|type
operator|.
name|encode
argument_list|(
name|buff
argument_list|,
name|val
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"encodedLength does not match actual, "
operator|+
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|val
argument_list|)
argument_list|,
name|buff
operator|.
name|getPosition
argument_list|()
argument_list|,
name|type
operator|.
name|encodedLength
argument_list|(
name|val
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testEncodedClassByteArray
parameter_list|()
block|{
specifier|final
name|DataType
argument_list|<
name|byte
index|[]
argument_list|>
name|type
init|=
operator|new
name|OrderedBlobVar
argument_list|(
name|Order
operator|.
name|ASCENDING
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|byte
index|[]
operator|.
expr|class
argument_list|,
name|type
operator|.
name|encodedClass
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

