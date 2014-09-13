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
name|TestFuzzyRowFilter
block|{
annotation|@
name|Test
specifier|public
name|void
name|testSatisfies
parameter_list|()
block|{
name|Assert
operator|.
name|assertEquals
argument_list|(
name|FuzzyRowFilter
operator|.
name|SatisfiesCode
operator|.
name|NEXT_EXISTS
argument_list|,
name|FuzzyRowFilter
operator|.
name|satisfies
argument_list|(
operator|new
name|byte
index|[]
block|{
literal|1
block|,
operator|(
name|byte
operator|)
operator|-
literal|128
block|,
literal|0
block|,
literal|0
block|,
literal|1
block|}
argument_list|,
comment|// row to check
operator|new
name|byte
index|[]
block|{
literal|1
block|,
literal|0
block|,
literal|1
block|}
argument_list|,
comment|// fuzzy row
operator|new
name|byte
index|[]
block|{
literal|0
block|,
literal|1
block|,
literal|0
block|}
argument_list|)
argument_list|)
expr_stmt|;
comment|// mask
name|Assert
operator|.
name|assertEquals
argument_list|(
name|FuzzyRowFilter
operator|.
name|SatisfiesCode
operator|.
name|YES
argument_list|,
name|FuzzyRowFilter
operator|.
name|satisfies
argument_list|(
operator|new
name|byte
index|[]
block|{
literal|1
block|,
operator|(
name|byte
operator|)
operator|-
literal|128
block|,
literal|1
block|,
literal|0
block|,
literal|1
block|}
argument_list|,
operator|new
name|byte
index|[]
block|{
literal|1
block|,
literal|0
block|,
literal|1
block|}
argument_list|,
operator|new
name|byte
index|[]
block|{
literal|0
block|,
literal|1
block|,
literal|0
block|}
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|FuzzyRowFilter
operator|.
name|SatisfiesCode
operator|.
name|NEXT_EXISTS
argument_list|,
name|FuzzyRowFilter
operator|.
name|satisfies
argument_list|(
operator|new
name|byte
index|[]
block|{
literal|1
block|,
operator|(
name|byte
operator|)
operator|-
literal|128
block|,
literal|2
block|,
literal|0
block|,
literal|1
block|}
argument_list|,
operator|new
name|byte
index|[]
block|{
literal|1
block|,
literal|0
block|,
literal|1
block|}
argument_list|,
operator|new
name|byte
index|[]
block|{
literal|0
block|,
literal|1
block|,
literal|0
block|}
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|FuzzyRowFilter
operator|.
name|SatisfiesCode
operator|.
name|NO_NEXT
argument_list|,
name|FuzzyRowFilter
operator|.
name|satisfies
argument_list|(
operator|new
name|byte
index|[]
block|{
literal|2
block|,
literal|3
block|,
literal|1
block|,
literal|1
block|,
literal|1
block|}
argument_list|,
operator|new
name|byte
index|[]
block|{
literal|1
block|,
literal|0
block|,
literal|1
block|}
argument_list|,
operator|new
name|byte
index|[]
block|{
literal|0
block|,
literal|1
block|,
literal|0
block|}
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|FuzzyRowFilter
operator|.
name|SatisfiesCode
operator|.
name|YES
argument_list|,
name|FuzzyRowFilter
operator|.
name|satisfies
argument_list|(
operator|new
name|byte
index|[]
block|{
literal|1
block|,
literal|2
block|,
literal|1
block|,
literal|3
block|,
literal|3
block|}
argument_list|,
operator|new
name|byte
index|[]
block|{
literal|1
block|,
literal|2
block|,
literal|0
block|,
literal|3
block|}
argument_list|,
operator|new
name|byte
index|[]
block|{
literal|0
block|,
literal|0
block|,
literal|1
block|,
literal|0
block|}
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|FuzzyRowFilter
operator|.
name|SatisfiesCode
operator|.
name|NEXT_EXISTS
argument_list|,
name|FuzzyRowFilter
operator|.
name|satisfies
argument_list|(
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
literal|3
block|,
literal|0
block|}
argument_list|,
comment|// row to check
operator|new
name|byte
index|[]
block|{
literal|1
block|,
literal|2
block|,
literal|0
block|,
literal|3
block|}
argument_list|,
comment|// fuzzy row
operator|new
name|byte
index|[]
block|{
literal|0
block|,
literal|0
block|,
literal|1
block|,
literal|0
block|}
argument_list|)
argument_list|)
expr_stmt|;
comment|// mask
name|Assert
operator|.
name|assertEquals
argument_list|(
name|FuzzyRowFilter
operator|.
name|SatisfiesCode
operator|.
name|NEXT_EXISTS
argument_list|,
name|FuzzyRowFilter
operator|.
name|satisfies
argument_list|(
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
literal|3
block|,
literal|0
block|}
argument_list|,
operator|new
name|byte
index|[]
block|{
literal|1
block|,
operator|(
name|byte
operator|)
literal|245
block|,
literal|0
block|,
literal|3
block|}
argument_list|,
operator|new
name|byte
index|[]
block|{
literal|0
block|,
literal|0
block|,
literal|1
block|,
literal|0
block|}
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|FuzzyRowFilter
operator|.
name|SatisfiesCode
operator|.
name|NO_NEXT
argument_list|,
name|FuzzyRowFilter
operator|.
name|satisfies
argument_list|(
operator|new
name|byte
index|[]
block|{
literal|1
block|,
operator|(
name|byte
operator|)
literal|245
block|,
literal|1
block|,
literal|3
block|,
literal|0
block|}
argument_list|,
operator|new
name|byte
index|[]
block|{
literal|1
block|,
literal|1
block|,
literal|0
block|,
literal|3
block|}
argument_list|,
operator|new
name|byte
index|[]
block|{
literal|0
block|,
literal|0
block|,
literal|1
block|,
literal|0
block|}
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|FuzzyRowFilter
operator|.
name|SatisfiesCode
operator|.
name|NO_NEXT
argument_list|,
name|FuzzyRowFilter
operator|.
name|satisfies
argument_list|(
operator|new
name|byte
index|[]
block|{
literal|1
block|,
literal|3
block|,
literal|1
block|,
literal|3
block|,
literal|0
block|}
argument_list|,
operator|new
name|byte
index|[]
block|{
literal|1
block|,
literal|2
block|,
literal|0
block|,
literal|3
block|}
argument_list|,
operator|new
name|byte
index|[]
block|{
literal|0
block|,
literal|0
block|,
literal|1
block|,
literal|0
block|}
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|FuzzyRowFilter
operator|.
name|SatisfiesCode
operator|.
name|NO_NEXT
argument_list|,
name|FuzzyRowFilter
operator|.
name|satisfies
argument_list|(
operator|new
name|byte
index|[]
block|{
literal|2
block|,
literal|1
block|,
literal|1
block|,
literal|1
block|,
literal|0
block|}
argument_list|,
operator|new
name|byte
index|[]
block|{
literal|1
block|,
literal|2
block|,
literal|0
block|,
literal|3
block|}
argument_list|,
operator|new
name|byte
index|[]
block|{
literal|0
block|,
literal|0
block|,
literal|1
block|,
literal|0
block|}
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|FuzzyRowFilter
operator|.
name|SatisfiesCode
operator|.
name|NEXT_EXISTS
argument_list|,
name|FuzzyRowFilter
operator|.
name|satisfies
argument_list|(
operator|new
name|byte
index|[]
block|{
literal|1
block|,
literal|2
block|,
literal|1
block|,
literal|0
block|,
literal|1
block|}
argument_list|,
operator|new
name|byte
index|[]
block|{
literal|0
block|,
literal|1
block|,
literal|2
block|}
argument_list|,
operator|new
name|byte
index|[]
block|{
literal|1
block|,
literal|0
block|,
literal|0
block|}
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testGetNextForFuzzyRule
parameter_list|()
block|{
name|assertNext
argument_list|(
operator|new
name|byte
index|[]
block|{
literal|0
block|,
literal|1
block|,
literal|2
block|}
argument_list|,
comment|// fuzzy row
operator|new
name|byte
index|[]
block|{
literal|1
block|,
literal|0
block|,
literal|0
block|}
argument_list|,
comment|// mask
operator|new
name|byte
index|[]
block|{
literal|1
block|,
literal|2
block|,
literal|1
block|,
literal|0
block|,
literal|1
block|}
argument_list|,
comment|// current
operator|new
name|byte
index|[]
block|{
literal|2
block|,
literal|1
block|,
literal|2
block|,
literal|0
block|,
literal|0
block|}
argument_list|)
expr_stmt|;
comment|// expected next
name|assertNext
argument_list|(
operator|new
name|byte
index|[]
block|{
literal|0
block|,
literal|1
block|,
literal|2
block|}
argument_list|,
comment|// fuzzy row
operator|new
name|byte
index|[]
block|{
literal|1
block|,
literal|0
block|,
literal|0
block|}
argument_list|,
comment|// mask
operator|new
name|byte
index|[]
block|{
literal|1
block|,
literal|1
block|,
literal|2
block|,
literal|0
block|,
literal|1
block|}
argument_list|,
comment|// current
operator|new
name|byte
index|[]
block|{
literal|1
block|,
literal|1
block|,
literal|2
block|,
literal|0
block|,
literal|2
block|}
argument_list|)
expr_stmt|;
comment|// expected next
name|assertNext
argument_list|(
operator|new
name|byte
index|[]
block|{
literal|0
block|,
literal|1
block|,
literal|0
block|,
literal|2
block|,
literal|0
block|}
argument_list|,
comment|// fuzzy row
operator|new
name|byte
index|[]
block|{
literal|1
block|,
literal|0
block|,
literal|1
block|,
literal|0
block|,
literal|1
block|}
argument_list|,
comment|// mask
operator|new
name|byte
index|[]
block|{
literal|1
block|,
literal|0
block|,
literal|2
block|,
literal|0
block|,
literal|1
block|}
argument_list|,
comment|// current
operator|new
name|byte
index|[]
block|{
literal|1
block|,
literal|1
block|,
literal|0
block|,
literal|2
block|,
literal|0
block|}
argument_list|)
expr_stmt|;
comment|// expected next
name|assertNext
argument_list|(
operator|new
name|byte
index|[]
block|{
literal|1
block|,
literal|0
block|,
literal|1
block|}
argument_list|,
operator|new
name|byte
index|[]
block|{
literal|0
block|,
literal|1
block|,
literal|0
block|}
argument_list|,
operator|new
name|byte
index|[]
block|{
literal|1
block|,
operator|(
name|byte
operator|)
literal|128
block|,
literal|2
block|,
literal|0
block|,
literal|1
block|}
argument_list|,
operator|new
name|byte
index|[]
block|{
literal|1
block|,
operator|(
name|byte
operator|)
literal|129
block|,
literal|1
block|,
literal|0
block|,
literal|0
block|}
argument_list|)
expr_stmt|;
name|assertNext
argument_list|(
operator|new
name|byte
index|[]
block|{
literal|0
block|,
literal|1
block|,
literal|0
block|,
literal|1
block|}
argument_list|,
operator|new
name|byte
index|[]
block|{
literal|1
block|,
literal|0
block|,
literal|1
block|,
literal|0
block|}
argument_list|,
operator|new
name|byte
index|[]
block|{
literal|5
block|,
literal|1
block|,
literal|0
block|,
literal|1
block|}
argument_list|,
operator|new
name|byte
index|[]
block|{
literal|5
block|,
literal|1
block|,
literal|1
block|,
literal|1
block|}
argument_list|)
expr_stmt|;
name|assertNext
argument_list|(
operator|new
name|byte
index|[]
block|{
literal|0
block|,
literal|1
block|,
literal|0
block|,
literal|1
block|}
argument_list|,
operator|new
name|byte
index|[]
block|{
literal|1
block|,
literal|0
block|,
literal|1
block|,
literal|0
block|}
argument_list|,
operator|new
name|byte
index|[]
block|{
literal|5
block|,
literal|1
block|,
literal|0
block|,
literal|1
block|,
literal|1
block|}
argument_list|,
operator|new
name|byte
index|[]
block|{
literal|5
block|,
literal|1
block|,
literal|0
block|,
literal|1
block|,
literal|2
block|}
argument_list|)
expr_stmt|;
name|assertNext
argument_list|(
operator|new
name|byte
index|[]
block|{
literal|0
block|,
literal|1
block|,
literal|0
block|,
literal|0
block|}
argument_list|,
comment|// fuzzy row
operator|new
name|byte
index|[]
block|{
literal|1
block|,
literal|0
block|,
literal|1
block|,
literal|1
block|}
argument_list|,
comment|// mask
operator|new
name|byte
index|[]
block|{
literal|5
block|,
literal|1
block|,
operator|(
name|byte
operator|)
literal|255
block|,
literal|1
block|}
argument_list|,
comment|// current
operator|new
name|byte
index|[]
block|{
literal|5
block|,
literal|1
block|,
operator|(
name|byte
operator|)
literal|255
block|,
literal|2
block|}
argument_list|)
expr_stmt|;
comment|// expected next
name|assertNext
argument_list|(
operator|new
name|byte
index|[]
block|{
literal|0
block|,
literal|1
block|,
literal|0
block|,
literal|1
block|}
argument_list|,
comment|// fuzzy row
operator|new
name|byte
index|[]
block|{
literal|1
block|,
literal|0
block|,
literal|1
block|,
literal|0
block|}
argument_list|,
comment|// mask
operator|new
name|byte
index|[]
block|{
literal|5
block|,
literal|1
block|,
operator|(
name|byte
operator|)
literal|255
block|,
literal|1
block|}
argument_list|,
comment|// current
operator|new
name|byte
index|[]
block|{
literal|6
block|,
literal|1
block|,
literal|0
block|,
literal|1
block|}
argument_list|)
expr_stmt|;
comment|// expected next
name|assertNext
argument_list|(
operator|new
name|byte
index|[]
block|{
literal|0
block|,
literal|1
block|,
literal|0
block|,
literal|1
block|}
argument_list|,
comment|// fuzzy row
operator|new
name|byte
index|[]
block|{
literal|1
block|,
literal|0
block|,
literal|1
block|,
literal|0
block|}
argument_list|,
comment|// mask
operator|new
name|byte
index|[]
block|{
literal|5
block|,
literal|1
block|,
operator|(
name|byte
operator|)
literal|255
block|,
literal|0
block|}
argument_list|,
comment|// current
operator|new
name|byte
index|[]
block|{
literal|5
block|,
literal|1
block|,
operator|(
name|byte
operator|)
literal|255
block|,
literal|1
block|}
argument_list|)
expr_stmt|;
comment|// expected next
name|assertNext
argument_list|(
operator|new
name|byte
index|[]
block|{
literal|5
block|,
literal|1
block|,
literal|1
block|,
literal|0
block|}
argument_list|,
operator|new
name|byte
index|[]
block|{
literal|0
block|,
literal|0
block|,
literal|1
block|,
literal|1
block|}
argument_list|,
operator|new
name|byte
index|[]
block|{
literal|5
block|,
literal|1
block|,
operator|(
name|byte
operator|)
literal|255
block|,
literal|1
block|}
argument_list|,
operator|new
name|byte
index|[]
block|{
literal|5
block|,
literal|1
block|,
operator|(
name|byte
operator|)
literal|255
block|,
literal|2
block|}
argument_list|)
expr_stmt|;
name|assertNext
argument_list|(
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
block|}
argument_list|,
operator|new
name|byte
index|[]
block|{
literal|0
block|,
literal|0
block|,
literal|1
block|,
literal|1
block|}
argument_list|,
operator|new
name|byte
index|[]
block|{
literal|1
block|,
literal|1
block|,
literal|2
block|,
literal|2
block|}
argument_list|,
operator|new
name|byte
index|[]
block|{
literal|1
block|,
literal|1
block|,
literal|2
block|,
literal|3
block|}
argument_list|)
expr_stmt|;
name|assertNext
argument_list|(
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
block|}
argument_list|,
operator|new
name|byte
index|[]
block|{
literal|0
block|,
literal|0
block|,
literal|1
block|,
literal|1
block|}
argument_list|,
operator|new
name|byte
index|[]
block|{
literal|1
block|,
literal|1
block|,
literal|3
block|,
literal|2
block|}
argument_list|,
operator|new
name|byte
index|[]
block|{
literal|1
block|,
literal|1
block|,
literal|3
block|,
literal|3
block|}
argument_list|)
expr_stmt|;
name|assertNext
argument_list|(
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
block|}
argument_list|,
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
block|}
argument_list|,
operator|new
name|byte
index|[]
block|{
literal|1
block|,
literal|1
block|,
literal|2
block|,
literal|3
block|}
argument_list|,
operator|new
name|byte
index|[]
block|{
literal|1
block|,
literal|1
block|,
literal|2
block|,
literal|4
block|}
argument_list|)
expr_stmt|;
name|assertNext
argument_list|(
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
block|}
argument_list|,
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
block|}
argument_list|,
operator|new
name|byte
index|[]
block|{
literal|1
block|,
literal|1
block|,
literal|3
block|,
literal|2
block|}
argument_list|,
operator|new
name|byte
index|[]
block|{
literal|1
block|,
literal|1
block|,
literal|3
block|,
literal|3
block|}
argument_list|)
expr_stmt|;
name|assertNext
argument_list|(
operator|new
name|byte
index|[]
block|{
literal|1
block|,
literal|1
block|,
literal|0
block|,
literal|0
block|}
argument_list|,
operator|new
name|byte
index|[]
block|{
literal|0
block|,
literal|0
block|,
literal|1
block|,
literal|1
block|}
argument_list|,
operator|new
name|byte
index|[]
block|{
literal|0
block|,
literal|1
block|,
literal|3
block|,
literal|2
block|}
argument_list|,
operator|new
name|byte
index|[]
block|{
literal|1
block|,
literal|1
block|,
literal|0
block|,
literal|0
block|}
argument_list|)
expr_stmt|;
comment|// No next for this one
name|Assert
operator|.
name|assertNull
argument_list|(
name|FuzzyRowFilter
operator|.
name|getNextForFuzzyRule
argument_list|(
operator|new
name|byte
index|[]
block|{
literal|2
block|,
literal|3
block|,
literal|1
block|,
literal|1
block|,
literal|1
block|}
argument_list|,
comment|// row to check
operator|new
name|byte
index|[]
block|{
literal|1
block|,
literal|0
block|,
literal|1
block|}
argument_list|,
comment|// fuzzy row
operator|new
name|byte
index|[]
block|{
literal|0
block|,
literal|1
block|,
literal|0
block|}
argument_list|)
argument_list|)
expr_stmt|;
comment|// mask
name|Assert
operator|.
name|assertNull
argument_list|(
name|FuzzyRowFilter
operator|.
name|getNextForFuzzyRule
argument_list|(
operator|new
name|byte
index|[]
block|{
literal|1
block|,
operator|(
name|byte
operator|)
literal|245
block|,
literal|1
block|,
literal|3
block|,
literal|0
block|}
argument_list|,
operator|new
name|byte
index|[]
block|{
literal|1
block|,
literal|1
block|,
literal|0
block|,
literal|3
block|}
argument_list|,
operator|new
name|byte
index|[]
block|{
literal|0
block|,
literal|0
block|,
literal|1
block|,
literal|0
block|}
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertNull
argument_list|(
name|FuzzyRowFilter
operator|.
name|getNextForFuzzyRule
argument_list|(
operator|new
name|byte
index|[]
block|{
literal|1
block|,
literal|3
block|,
literal|1
block|,
literal|3
block|,
literal|0
block|}
argument_list|,
operator|new
name|byte
index|[]
block|{
literal|1
block|,
literal|2
block|,
literal|0
block|,
literal|3
block|}
argument_list|,
operator|new
name|byte
index|[]
block|{
literal|0
block|,
literal|0
block|,
literal|1
block|,
literal|0
block|}
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertNull
argument_list|(
name|FuzzyRowFilter
operator|.
name|getNextForFuzzyRule
argument_list|(
operator|new
name|byte
index|[]
block|{
literal|2
block|,
literal|1
block|,
literal|1
block|,
literal|1
block|,
literal|0
block|}
argument_list|,
operator|new
name|byte
index|[]
block|{
literal|1
block|,
literal|2
block|,
literal|0
block|,
literal|3
block|}
argument_list|,
operator|new
name|byte
index|[]
block|{
literal|0
block|,
literal|0
block|,
literal|1
block|,
literal|0
block|}
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|assertNext
parameter_list|(
name|byte
index|[]
name|fuzzyRow
parameter_list|,
name|byte
index|[]
name|mask
parameter_list|,
name|byte
index|[]
name|current
parameter_list|,
name|byte
index|[]
name|expected
parameter_list|)
block|{
name|byte
index|[]
name|nextForFuzzyRule
init|=
name|FuzzyRowFilter
operator|.
name|getNextForFuzzyRule
argument_list|(
name|current
argument_list|,
name|fuzzyRow
argument_list|,
name|mask
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertArrayEquals
argument_list|(
name|expected
argument_list|,
name|nextForFuzzyRule
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

