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
name|client
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
name|SmallTests
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

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Set
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|TreeSet
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
name|TestConnectionUtils
block|{
annotation|@
name|Test
specifier|public
name|void
name|testRetryTimeJitter
parameter_list|()
block|{
name|long
index|[]
name|retries
init|=
operator|new
name|long
index|[
literal|200
index|]
decl_stmt|;
name|long
name|baseTime
init|=
literal|1000000
decl_stmt|;
comment|//Larger number than reality to help test randomness.
name|long
name|maxTimeExpected
init|=
call|(
name|long
call|)
argument_list|(
name|baseTime
operator|*
literal|1.01f
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|retries
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|retries
index|[
name|i
index|]
operator|=
name|ConnectionUtils
operator|.
name|getPauseTime
argument_list|(
name|baseTime
argument_list|,
literal|0
argument_list|)
expr_stmt|;
block|}
name|Set
argument_list|<
name|Long
argument_list|>
name|retyTimeSet
init|=
operator|new
name|TreeSet
argument_list|<
name|Long
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|long
name|l
range|:
name|retries
control|)
block|{
comment|/*make sure that there is some jitter but only 1%*/
name|assertTrue
argument_list|(
name|l
operator|>=
name|baseTime
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|l
operator|<=
name|maxTimeExpected
argument_list|)
expr_stmt|;
comment|// Add the long to the set
name|retyTimeSet
operator|.
name|add
argument_list|(
name|l
argument_list|)
expr_stmt|;
block|}
comment|//Make sure that most are unique.  some overlap will happen
name|assertTrue
argument_list|(
name|retyTimeSet
operator|.
name|size
argument_list|()
operator|>
operator|(
name|retries
operator|.
name|length
operator|*
literal|0.80
operator|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

