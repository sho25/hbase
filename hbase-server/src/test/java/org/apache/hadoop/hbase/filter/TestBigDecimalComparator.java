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
name|filter
package|;
end_package

begin_import
import|import
name|java
operator|.
name|math
operator|.
name|BigDecimal
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
name|FilterTests
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
name|Assert
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
name|FilterTests
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
name|TestBigDecimalComparator
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
name|TestBigDecimalComparator
operator|.
name|class
argument_list|)
decl_stmt|;
annotation|@
name|Test
specifier|public
name|void
name|testObjectEquals
parameter_list|()
block|{
name|BigDecimal
name|bd
init|=
operator|new
name|BigDecimal
argument_list|(
name|Double
operator|.
name|MIN_VALUE
argument_list|)
decl_stmt|;
comment|// Check that equals returns true for identical objects
specifier|final
name|BigDecimalComparator
name|bdc
init|=
operator|new
name|BigDecimalComparator
argument_list|(
name|bd
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|bdc
operator|.
name|equals
argument_list|(
name|bdc
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|bdc
operator|.
name|hashCode
argument_list|()
argument_list|,
name|bdc
operator|.
name|hashCode
argument_list|()
argument_list|)
expr_stmt|;
comment|// Check that equals returns true for the same object
specifier|final
name|BigDecimalComparator
name|bdc1
init|=
operator|new
name|BigDecimalComparator
argument_list|(
name|bd
argument_list|)
decl_stmt|;
specifier|final
name|BigDecimalComparator
name|bdc2
init|=
operator|new
name|BigDecimalComparator
argument_list|(
name|bd
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|bdc1
operator|.
name|equals
argument_list|(
name|bdc2
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|bdc1
operator|.
name|hashCode
argument_list|()
argument_list|,
name|bdc2
operator|.
name|hashCode
argument_list|()
argument_list|)
expr_stmt|;
comment|// Check that equals returns false for different objects
specifier|final
name|BigDecimalComparator
name|bdc3
init|=
operator|new
name|BigDecimalComparator
argument_list|(
name|bd
argument_list|)
decl_stmt|;
specifier|final
name|BigDecimalComparator
name|bdc4
init|=
operator|new
name|BigDecimalComparator
argument_list|(
operator|new
name|BigDecimal
argument_list|(
name|Long
operator|.
name|MIN_VALUE
argument_list|)
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertFalse
argument_list|(
name|bdc3
operator|.
name|equals
argument_list|(
name|bdc4
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertNotEquals
argument_list|(
name|bdc3
operator|.
name|hashCode
argument_list|()
argument_list|,
name|bdc4
operator|.
name|hashCode
argument_list|()
argument_list|)
expr_stmt|;
comment|// Check that equals returns false for a different type
specifier|final
name|BigDecimalComparator
name|bdc5
init|=
operator|new
name|BigDecimalComparator
argument_list|(
name|bd
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertFalse
argument_list|(
name|bdc5
operator|.
name|equals
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testEqualsValue
parameter_list|()
block|{
comment|// given
name|BigDecimal
name|bd1
init|=
operator|new
name|BigDecimal
argument_list|(
name|Double
operator|.
name|MAX_VALUE
argument_list|)
decl_stmt|;
name|BigDecimal
name|bd2
init|=
operator|new
name|BigDecimal
argument_list|(
name|Double
operator|.
name|MIN_VALUE
argument_list|)
decl_stmt|;
name|byte
index|[]
name|value1
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|bd1
argument_list|)
decl_stmt|;
name|byte
index|[]
name|value2
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|bd2
argument_list|)
decl_stmt|;
name|BigDecimalComparator
name|comparator1
init|=
operator|new
name|BigDecimalComparator
argument_list|(
name|bd1
argument_list|)
decl_stmt|;
name|BigDecimalComparator
name|comparator2
init|=
operator|new
name|BigDecimalComparator
argument_list|(
name|bd2
argument_list|)
decl_stmt|;
comment|// when
name|int
name|comp1
init|=
name|comparator1
operator|.
name|compareTo
argument_list|(
name|value1
argument_list|)
decl_stmt|;
name|int
name|comp2
init|=
name|comparator2
operator|.
name|compareTo
argument_list|(
name|value2
argument_list|)
decl_stmt|;
comment|// then
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|comp1
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|comp2
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testGreaterThanValue
parameter_list|()
block|{
comment|// given
name|byte
index|[]
name|val1
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
operator|new
name|BigDecimal
argument_list|(
literal|"1000000000000000000000000000000.9999999999999999"
argument_list|)
argument_list|)
decl_stmt|;
name|byte
index|[]
name|val2
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
operator|new
name|BigDecimal
argument_list|(
literal|0
argument_list|)
argument_list|)
decl_stmt|;
name|byte
index|[]
name|val3
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
operator|new
name|BigDecimal
argument_list|(
name|Double
operator|.
name|MIN_VALUE
argument_list|)
argument_list|)
decl_stmt|;
name|BigDecimal
name|bd
init|=
operator|new
name|BigDecimal
argument_list|(
name|Double
operator|.
name|MAX_VALUE
argument_list|)
decl_stmt|;
name|BigDecimalComparator
name|comparator
init|=
operator|new
name|BigDecimalComparator
argument_list|(
name|bd
argument_list|)
decl_stmt|;
comment|// when
name|int
name|comp1
init|=
name|comparator
operator|.
name|compareTo
argument_list|(
name|val1
argument_list|)
decl_stmt|;
name|int
name|comp2
init|=
name|comparator
operator|.
name|compareTo
argument_list|(
name|val2
argument_list|)
decl_stmt|;
name|int
name|comp3
init|=
name|comparator
operator|.
name|compareTo
argument_list|(
name|val3
argument_list|)
decl_stmt|;
comment|// then
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|comp1
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|comp2
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|comp3
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testLessThanValue
parameter_list|()
block|{
comment|// given
name|byte
index|[]
name|val1
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
operator|new
name|BigDecimal
argument_list|(
literal|"-1000000000000000000000000000000"
argument_list|)
argument_list|)
decl_stmt|;
name|byte
index|[]
name|val2
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
operator|new
name|BigDecimal
argument_list|(
literal|0
argument_list|)
argument_list|)
decl_stmt|;
name|byte
index|[]
name|val3
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
operator|new
name|BigDecimal
argument_list|(
literal|1
argument_list|)
argument_list|)
decl_stmt|;
name|BigDecimal
name|bd
init|=
operator|new
name|BigDecimal
argument_list|(
literal|"-1000000000000000000000000000000.0000000000000001"
argument_list|)
decl_stmt|;
name|BigDecimalComparator
name|comparator
init|=
operator|new
name|BigDecimalComparator
argument_list|(
name|bd
argument_list|)
decl_stmt|;
comment|// when
name|int
name|comp1
init|=
name|comparator
operator|.
name|compareTo
argument_list|(
name|val1
argument_list|)
decl_stmt|;
name|int
name|comp2
init|=
name|comparator
operator|.
name|compareTo
argument_list|(
name|val2
argument_list|)
decl_stmt|;
name|int
name|comp3
init|=
name|comparator
operator|.
name|compareTo
argument_list|(
name|val3
argument_list|)
decl_stmt|;
comment|// then
name|Assert
operator|.
name|assertEquals
argument_list|(
operator|-
literal|1
argument_list|,
name|comp1
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
operator|-
literal|1
argument_list|,
name|comp2
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
operator|-
literal|1
argument_list|,
name|comp3
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

