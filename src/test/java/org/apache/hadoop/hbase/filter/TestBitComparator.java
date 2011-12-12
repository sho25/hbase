begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  *  Copyright 2010 The Apache Software Foundation  *   *  Licensed under the Apache License, Version 2.0 (the "License");  *  you may not use this file except in compliance with the License.  *  You may obtain a copy of the License at  *   *       http://www.apache.org/licenses/LICENSE-2.0  *   *  Unless required by applicable law or agreed to in writing, software  *  distributed under the License is distributed on an "AS IS" BASIS,  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  *  See the License for the specific language governing permissions and  *  limitations under the License.  *  under the License.  */
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
import|import
name|junit
operator|.
name|framework
operator|.
name|TestCase
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
name|experimental
operator|.
name|categories
operator|.
name|Category
import|;
end_import

begin_comment
comment|/**  * Tests for the bit comparator  */
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
name|TestBitComparator
extends|extends
name|TestCase
block|{
specifier|private
specifier|static
name|byte
index|[]
name|zeros
init|=
operator|new
name|byte
index|[]
block|{
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|}
decl_stmt|;
specifier|private
specifier|static
name|byte
index|[]
name|ones
init|=
operator|new
name|byte
index|[]
block|{
literal|1
block|,
literal|1
block|,
literal|1
block|,
literal|1
block|,
literal|1
block|,
literal|1
block|}
decl_stmt|;
specifier|private
specifier|static
name|byte
index|[]
name|data0
init|=
operator|new
name|byte
index|[]
block|{
literal|0
block|,
literal|1
block|,
literal|2
block|,
literal|4
block|,
literal|8
block|,
literal|15
block|}
decl_stmt|;
specifier|private
specifier|static
name|byte
index|[]
name|data1
init|=
operator|new
name|byte
index|[]
block|{
literal|15
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|}
decl_stmt|;
specifier|private
specifier|static
name|byte
index|[]
name|data2
init|=
operator|new
name|byte
index|[]
block|{
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|15
block|}
decl_stmt|;
specifier|private
specifier|static
name|byte
index|[]
name|data3
init|=
operator|new
name|byte
index|[]
block|{
literal|15
block|,
literal|15
block|,
literal|15
block|,
literal|15
block|,
literal|15
block|}
decl_stmt|;
specifier|private
specifier|final
name|int
name|Equal
init|=
literal|0
decl_stmt|;
specifier|private
specifier|final
name|int
name|NotEqual
init|=
literal|1
decl_stmt|;
specifier|public
name|void
name|testANDOperation
parameter_list|()
block|{
name|testOperation
argument_list|(
name|zeros
argument_list|,
name|ones
argument_list|,
name|BitComparator
operator|.
name|BitwiseOp
operator|.
name|AND
argument_list|,
name|NotEqual
argument_list|)
expr_stmt|;
name|testOperation
argument_list|(
name|data1
argument_list|,
name|ones
argument_list|,
name|BitComparator
operator|.
name|BitwiseOp
operator|.
name|AND
argument_list|,
name|Equal
argument_list|)
expr_stmt|;
name|testOperation
argument_list|(
name|data1
argument_list|,
name|data0
argument_list|,
name|BitComparator
operator|.
name|BitwiseOp
operator|.
name|AND
argument_list|,
name|NotEqual
argument_list|)
expr_stmt|;
name|testOperation
argument_list|(
name|data2
argument_list|,
name|data1
argument_list|,
name|BitComparator
operator|.
name|BitwiseOp
operator|.
name|AND
argument_list|,
name|NotEqual
argument_list|)
expr_stmt|;
name|testOperation
argument_list|(
name|ones
argument_list|,
name|data0
argument_list|,
name|BitComparator
operator|.
name|BitwiseOp
operator|.
name|AND
argument_list|,
name|Equal
argument_list|)
expr_stmt|;
name|testOperation
argument_list|(
name|ones
argument_list|,
name|data3
argument_list|,
name|BitComparator
operator|.
name|BitwiseOp
operator|.
name|AND
argument_list|,
name|NotEqual
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|testOROperation
parameter_list|()
block|{
name|testOperation
argument_list|(
name|ones
argument_list|,
name|zeros
argument_list|,
name|BitComparator
operator|.
name|BitwiseOp
operator|.
name|OR
argument_list|,
name|Equal
argument_list|)
expr_stmt|;
name|testOperation
argument_list|(
name|zeros
argument_list|,
name|zeros
argument_list|,
name|BitComparator
operator|.
name|BitwiseOp
operator|.
name|OR
argument_list|,
name|NotEqual
argument_list|)
expr_stmt|;
name|testOperation
argument_list|(
name|data1
argument_list|,
name|zeros
argument_list|,
name|BitComparator
operator|.
name|BitwiseOp
operator|.
name|OR
argument_list|,
name|Equal
argument_list|)
expr_stmt|;
name|testOperation
argument_list|(
name|data2
argument_list|,
name|data1
argument_list|,
name|BitComparator
operator|.
name|BitwiseOp
operator|.
name|OR
argument_list|,
name|Equal
argument_list|)
expr_stmt|;
name|testOperation
argument_list|(
name|ones
argument_list|,
name|data3
argument_list|,
name|BitComparator
operator|.
name|BitwiseOp
operator|.
name|OR
argument_list|,
name|NotEqual
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|testXOROperation
parameter_list|()
block|{
name|testOperation
argument_list|(
name|ones
argument_list|,
name|zeros
argument_list|,
name|BitComparator
operator|.
name|BitwiseOp
operator|.
name|XOR
argument_list|,
name|Equal
argument_list|)
expr_stmt|;
name|testOperation
argument_list|(
name|zeros
argument_list|,
name|zeros
argument_list|,
name|BitComparator
operator|.
name|BitwiseOp
operator|.
name|XOR
argument_list|,
name|NotEqual
argument_list|)
expr_stmt|;
name|testOperation
argument_list|(
name|ones
argument_list|,
name|ones
argument_list|,
name|BitComparator
operator|.
name|BitwiseOp
operator|.
name|XOR
argument_list|,
name|NotEqual
argument_list|)
expr_stmt|;
name|testOperation
argument_list|(
name|data2
argument_list|,
name|data1
argument_list|,
name|BitComparator
operator|.
name|BitwiseOp
operator|.
name|XOR
argument_list|,
name|Equal
argument_list|)
expr_stmt|;
name|testOperation
argument_list|(
name|ones
argument_list|,
name|data3
argument_list|,
name|BitComparator
operator|.
name|BitwiseOp
operator|.
name|XOR
argument_list|,
name|NotEqual
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|testOperation
parameter_list|(
name|byte
index|[]
name|data
parameter_list|,
name|byte
index|[]
name|comparatorBytes
parameter_list|,
name|BitComparator
operator|.
name|BitwiseOp
name|operator
parameter_list|,
name|int
name|expected
parameter_list|)
block|{
name|BitComparator
name|comparator
init|=
operator|new
name|BitComparator
argument_list|(
name|comparatorBytes
argument_list|,
name|operator
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|comparator
operator|.
name|compareTo
argument_list|(
name|data
argument_list|)
argument_list|,
name|expected
argument_list|)
expr_stmt|;
block|}
annotation|@
name|org
operator|.
name|junit
operator|.
name|Rule
specifier|public
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|ResourceCheckerJUnitRule
name|cu
init|=
operator|new
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|ResourceCheckerJUnitRule
argument_list|()
decl_stmt|;
block|}
end_class

end_unit

