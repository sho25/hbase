begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2011 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|io
operator|.
name|ByteArrayInputStream
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
name|util
operator|.
name|Bytes
import|;
end_import

begin_class
specifier|public
class|class
name|TestRandomRowFilter
extends|extends
name|TestCase
block|{
specifier|protected
name|RandomRowFilter
name|quarterChanceFilter
decl_stmt|;
annotation|@
name|Override
specifier|protected
name|void
name|setUp
parameter_list|()
throws|throws
name|Exception
block|{
name|super
operator|.
name|setUp
argument_list|()
expr_stmt|;
name|quarterChanceFilter
operator|=
operator|new
name|RandomRowFilter
argument_list|(
literal|0.25f
argument_list|)
expr_stmt|;
block|}
comment|/**    * Tests basics    *     * @throws Exception    */
specifier|public
name|void
name|testBasics
parameter_list|()
throws|throws
name|Exception
block|{
name|int
name|included
init|=
literal|0
decl_stmt|;
name|int
name|max
init|=
literal|1000000
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
name|max
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
operator|!
name|quarterChanceFilter
operator|.
name|filterRowKey
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"row"
argument_list|)
argument_list|,
literal|0
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"row"
argument_list|)
operator|.
name|length
argument_list|)
condition|)
block|{
name|included
operator|++
expr_stmt|;
block|}
block|}
comment|// Now let's check if the filter included the right number of rows;
comment|// since we're dealing with randomness, we must have a include an epsilon
comment|// tolerance.
name|int
name|epsilon
init|=
name|max
operator|/
literal|100
decl_stmt|;
name|assertTrue
argument_list|(
literal|"Roughly 25% should pass the filter"
argument_list|,
name|Math
operator|.
name|abs
argument_list|(
name|included
operator|-
name|max
operator|/
literal|4
argument_list|)
operator|<
name|epsilon
argument_list|)
expr_stmt|;
block|}
comment|/**    * Tests serialization    *     * @throws Exception    */
specifier|public
name|void
name|testSerialization
parameter_list|()
throws|throws
name|Exception
block|{
name|RandomRowFilter
name|newFilter
init|=
name|serializationTest
argument_list|(
name|quarterChanceFilter
argument_list|)
decl_stmt|;
comment|// use epsilon float comparison
name|assertTrue
argument_list|(
literal|"float should be equal"
argument_list|,
name|Math
operator|.
name|abs
argument_list|(
name|newFilter
operator|.
name|getChance
argument_list|()
operator|-
name|quarterChanceFilter
operator|.
name|getChance
argument_list|()
argument_list|)
operator|<
literal|0.000001f
argument_list|)
expr_stmt|;
block|}
specifier|private
name|RandomRowFilter
name|serializationTest
parameter_list|(
name|RandomRowFilter
name|filter
parameter_list|)
throws|throws
name|Exception
block|{
comment|// Decompose filter to bytes.
name|ByteArrayOutputStream
name|stream
init|=
operator|new
name|ByteArrayOutputStream
argument_list|()
decl_stmt|;
name|DataOutputStream
name|out
init|=
operator|new
name|DataOutputStream
argument_list|(
name|stream
argument_list|)
decl_stmt|;
name|filter
operator|.
name|write
argument_list|(
name|out
argument_list|)
expr_stmt|;
name|out
operator|.
name|close
argument_list|()
expr_stmt|;
name|byte
index|[]
name|buffer
init|=
name|stream
operator|.
name|toByteArray
argument_list|()
decl_stmt|;
comment|// Recompose filter.
name|DataInputStream
name|in
init|=
operator|new
name|DataInputStream
argument_list|(
operator|new
name|ByteArrayInputStream
argument_list|(
name|buffer
argument_list|)
argument_list|)
decl_stmt|;
name|RandomRowFilter
name|newFilter
init|=
operator|new
name|RandomRowFilter
argument_list|()
decl_stmt|;
name|newFilter
operator|.
name|readFields
argument_list|(
name|in
argument_list|)
expr_stmt|;
return|return
name|newFilter
return|;
block|}
block|}
end_class

end_unit

