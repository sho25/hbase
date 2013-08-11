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
name|SimplePositionedByteRange
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
name|TestRawString
block|{
specifier|static
specifier|final
name|String
index|[]
name|VALUES
init|=
operator|new
name|String
index|[]
block|{
literal|""
block|,
literal|"1"
block|,
literal|"22"
block|,
literal|"333"
block|,
literal|"4444"
block|,
literal|"55555"
block|,
literal|"666666"
block|,
literal|"7777777"
block|,
literal|"88888888"
block|,
literal|"999999999"
block|,   }
decl_stmt|;
annotation|@
name|Test
specifier|public
name|void
name|testReadWrite
parameter_list|()
block|{
for|for
control|(
name|Order
name|ord
range|:
operator|new
name|Order
index|[]
block|{
name|Order
operator|.
name|ASCENDING
block|,
name|Order
operator|.
name|DESCENDING
block|}
control|)
block|{
name|RawString
name|type
init|=
name|Order
operator|.
name|ASCENDING
operator|==
name|ord
condition|?
name|RawString
operator|.
name|ASCENDING
else|:
name|RawString
operator|.
name|DESCENDING
decl_stmt|;
for|for
control|(
name|String
name|val
range|:
name|VALUES
control|)
block|{
name|PositionedByteRange
name|buff
init|=
operator|new
name|SimplePositionedByteRange
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|val
argument_list|)
operator|.
name|length
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|buff
operator|.
name|getLength
argument_list|()
argument_list|,
name|type
operator|.
name|encode
argument_list|(
name|buff
argument_list|,
name|val
argument_list|)
argument_list|)
expr_stmt|;
name|byte
index|[]
name|expected
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|val
argument_list|)
decl_stmt|;
name|ord
operator|.
name|apply
argument_list|(
name|expected
argument_list|)
expr_stmt|;
name|assertArrayEquals
argument_list|(
name|expected
argument_list|,
name|buff
operator|.
name|getBytes
argument_list|()
argument_list|)
expr_stmt|;
name|buff
operator|.
name|setPosition
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|val
argument_list|,
name|type
operator|.
name|decode
argument_list|(
name|buff
argument_list|)
argument_list|)
expr_stmt|;
name|buff
operator|.
name|setPosition
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|buff
operator|.
name|getLength
argument_list|()
argument_list|,
name|type
operator|.
name|skip
argument_list|(
name|buff
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|buff
operator|.
name|getLength
argument_list|()
argument_list|,
name|buff
operator|.
name|getPosition
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit

