begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|assertTrue
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
name|TableName
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
name|HRegionInfo
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
name|HBaseFsck
operator|.
name|HbckInfo
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
name|HBaseFsck
operator|.
name|MetaEntry
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
comment|/**  * Test the comparator used by Hbck.  */
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
name|TestHBaseFsckComparator
block|{
name|TableName
name|table
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"table1"
argument_list|)
decl_stmt|;
name|TableName
name|table2
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"table2"
argument_list|)
decl_stmt|;
name|byte
index|[]
name|keyStart
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|""
argument_list|)
decl_stmt|;
name|byte
index|[]
name|keyA
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"A"
argument_list|)
decl_stmt|;
name|byte
index|[]
name|keyB
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"B"
argument_list|)
decl_stmt|;
name|byte
index|[]
name|keyC
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"C"
argument_list|)
decl_stmt|;
name|byte
index|[]
name|keyEnd
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|""
argument_list|)
decl_stmt|;
specifier|static
name|HbckInfo
name|genHbckInfo
parameter_list|(
name|TableName
name|table
parameter_list|,
name|byte
index|[]
name|start
parameter_list|,
name|byte
index|[]
name|end
parameter_list|,
name|int
name|time
parameter_list|)
block|{
return|return
operator|new
name|HbckInfo
argument_list|(
operator|new
name|MetaEntry
argument_list|(
operator|new
name|HRegionInfo
argument_list|(
name|table
argument_list|,
name|start
argument_list|,
name|end
argument_list|)
argument_list|,
literal|null
argument_list|,
name|time
argument_list|)
argument_list|)
return|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testEquals
parameter_list|()
block|{
name|HbckInfo
name|hi1
init|=
name|genHbckInfo
argument_list|(
name|table
argument_list|,
name|keyA
argument_list|,
name|keyB
argument_list|,
literal|0
argument_list|)
decl_stmt|;
name|HbckInfo
name|hi2
init|=
name|genHbckInfo
argument_list|(
name|table
argument_list|,
name|keyA
argument_list|,
name|keyB
argument_list|,
literal|0
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|HBaseFsck
operator|.
name|cmp
operator|.
name|compare
argument_list|(
name|hi1
argument_list|,
name|hi2
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|HBaseFsck
operator|.
name|cmp
operator|.
name|compare
argument_list|(
name|hi2
argument_list|,
name|hi1
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testEqualsInstance
parameter_list|()
block|{
name|HbckInfo
name|hi1
init|=
name|genHbckInfo
argument_list|(
name|table
argument_list|,
name|keyA
argument_list|,
name|keyB
argument_list|,
literal|0
argument_list|)
decl_stmt|;
name|HbckInfo
name|hi2
init|=
name|hi1
decl_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|HBaseFsck
operator|.
name|cmp
operator|.
name|compare
argument_list|(
name|hi1
argument_list|,
name|hi2
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|HBaseFsck
operator|.
name|cmp
operator|.
name|compare
argument_list|(
name|hi2
argument_list|,
name|hi1
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testDiffTable
parameter_list|()
block|{
name|HbckInfo
name|hi1
init|=
name|genHbckInfo
argument_list|(
name|table
argument_list|,
name|keyA
argument_list|,
name|keyC
argument_list|,
literal|0
argument_list|)
decl_stmt|;
name|HbckInfo
name|hi2
init|=
name|genHbckInfo
argument_list|(
name|table2
argument_list|,
name|keyA
argument_list|,
name|keyC
argument_list|,
literal|0
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|HBaseFsck
operator|.
name|cmp
operator|.
name|compare
argument_list|(
name|hi1
argument_list|,
name|hi2
argument_list|)
operator|<
literal|0
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|HBaseFsck
operator|.
name|cmp
operator|.
name|compare
argument_list|(
name|hi2
argument_list|,
name|hi1
argument_list|)
operator|>
literal|0
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testDiffStartKey
parameter_list|()
block|{
name|HbckInfo
name|hi1
init|=
name|genHbckInfo
argument_list|(
name|table
argument_list|,
name|keyStart
argument_list|,
name|keyC
argument_list|,
literal|0
argument_list|)
decl_stmt|;
name|HbckInfo
name|hi2
init|=
name|genHbckInfo
argument_list|(
name|table
argument_list|,
name|keyA
argument_list|,
name|keyC
argument_list|,
literal|0
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|HBaseFsck
operator|.
name|cmp
operator|.
name|compare
argument_list|(
name|hi1
argument_list|,
name|hi2
argument_list|)
operator|<
literal|0
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|HBaseFsck
operator|.
name|cmp
operator|.
name|compare
argument_list|(
name|hi2
argument_list|,
name|hi1
argument_list|)
operator|>
literal|0
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testDiffEndKey
parameter_list|()
block|{
name|HbckInfo
name|hi1
init|=
name|genHbckInfo
argument_list|(
name|table
argument_list|,
name|keyA
argument_list|,
name|keyB
argument_list|,
literal|0
argument_list|)
decl_stmt|;
name|HbckInfo
name|hi2
init|=
name|genHbckInfo
argument_list|(
name|table
argument_list|,
name|keyA
argument_list|,
name|keyC
argument_list|,
literal|0
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|HBaseFsck
operator|.
name|cmp
operator|.
name|compare
argument_list|(
name|hi1
argument_list|,
name|hi2
argument_list|)
operator|<
literal|0
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|HBaseFsck
operator|.
name|cmp
operator|.
name|compare
argument_list|(
name|hi2
argument_list|,
name|hi1
argument_list|)
operator|>
literal|0
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testAbsEndKey
parameter_list|()
block|{
name|HbckInfo
name|hi1
init|=
name|genHbckInfo
argument_list|(
name|table
argument_list|,
name|keyA
argument_list|,
name|keyC
argument_list|,
literal|0
argument_list|)
decl_stmt|;
name|HbckInfo
name|hi2
init|=
name|genHbckInfo
argument_list|(
name|table
argument_list|,
name|keyA
argument_list|,
name|keyEnd
argument_list|,
literal|0
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|HBaseFsck
operator|.
name|cmp
operator|.
name|compare
argument_list|(
name|hi1
argument_list|,
name|hi2
argument_list|)
operator|<
literal|0
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|HBaseFsck
operator|.
name|cmp
operator|.
name|compare
argument_list|(
name|hi2
argument_list|,
name|hi1
argument_list|)
operator|>
literal|0
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

