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
name|metrics
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|List
import|;
end_import

begin_import
import|import
name|junit
operator|.
name|framework
operator|.
name|Assert
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
name|Pair
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

begin_class
specifier|public
class|class
name|TestExactCounterMetric
block|{
annotation|@
name|Test
specifier|public
name|void
name|testBasic
parameter_list|()
block|{
specifier|final
name|ExactCounterMetric
name|counter
init|=
operator|new
name|ExactCounterMetric
argument_list|(
literal|"testCounter"
argument_list|,
literal|null
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|1
init|;
name|i
operator|<=
literal|10
condition|;
name|i
operator|++
control|)
block|{
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
name|i
condition|;
name|j
operator|++
control|)
block|{
name|counter
operator|.
name|update
argument_list|(
name|i
operator|+
literal|""
argument_list|)
expr_stmt|;
block|}
block|}
name|List
argument_list|<
name|Pair
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
argument_list|>
name|topFive
init|=
name|counter
operator|.
name|getTop
argument_list|(
literal|5
argument_list|)
decl_stmt|;
name|Long
name|i
init|=
literal|10L
decl_stmt|;
for|for
control|(
name|Pair
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
name|entry
range|:
name|topFive
control|)
block|{
name|Assert
operator|.
name|assertEquals
argument_list|(
name|i
operator|+
literal|""
argument_list|,
name|entry
operator|.
name|getFirst
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|i
argument_list|,
name|entry
operator|.
name|getSecond
argument_list|()
argument_list|)
expr_stmt|;
name|i
operator|--
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

