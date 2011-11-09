begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2007 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
comment|/**  * Tests the inclusive stop row filter  */
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
name|TestInclusiveStopFilter
extends|extends
name|TestCase
block|{
specifier|private
specifier|final
name|byte
index|[]
name|STOP_ROW
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"stop_row"
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|byte
index|[]
name|GOOD_ROW
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"good_row"
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|byte
index|[]
name|PAST_STOP_ROW
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"zzzzzz"
argument_list|)
decl_stmt|;
name|Filter
name|mainFilter
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
name|mainFilter
operator|=
operator|new
name|InclusiveStopFilter
argument_list|(
name|STOP_ROW
argument_list|)
expr_stmt|;
block|}
comment|/**    * Tests identification of the stop row    * @throws Exception    */
specifier|public
name|void
name|testStopRowIdentification
parameter_list|()
throws|throws
name|Exception
block|{
name|stopRowTests
argument_list|(
name|mainFilter
argument_list|)
expr_stmt|;
block|}
comment|/**    * Tests serialization    * @throws Exception    */
specifier|public
name|void
name|testSerialization
parameter_list|()
throws|throws
name|Exception
block|{
comment|// Decompose mainFilter to bytes.
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
name|mainFilter
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
comment|// Recompose mainFilter.
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
name|Filter
name|newFilter
init|=
operator|new
name|InclusiveStopFilter
argument_list|()
decl_stmt|;
name|newFilter
operator|.
name|readFields
argument_list|(
name|in
argument_list|)
expr_stmt|;
comment|// Ensure the serialization preserved the filter by running a full test.
name|stopRowTests
argument_list|(
name|newFilter
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|stopRowTests
parameter_list|(
name|Filter
name|filter
parameter_list|)
throws|throws
name|Exception
block|{
name|assertFalse
argument_list|(
literal|"Filtering on "
operator|+
name|Bytes
operator|.
name|toString
argument_list|(
name|GOOD_ROW
argument_list|)
argument_list|,
name|filter
operator|.
name|filterRowKey
argument_list|(
name|GOOD_ROW
argument_list|,
literal|0
argument_list|,
name|GOOD_ROW
operator|.
name|length
argument_list|)
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
literal|"Filtering on "
operator|+
name|Bytes
operator|.
name|toString
argument_list|(
name|STOP_ROW
argument_list|)
argument_list|,
name|filter
operator|.
name|filterRowKey
argument_list|(
name|STOP_ROW
argument_list|,
literal|0
argument_list|,
name|STOP_ROW
operator|.
name|length
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"Filtering on "
operator|+
name|Bytes
operator|.
name|toString
argument_list|(
name|PAST_STOP_ROW
argument_list|)
argument_list|,
name|filter
operator|.
name|filterRowKey
argument_list|(
name|PAST_STOP_ROW
argument_list|,
literal|0
argument_list|,
name|PAST_STOP_ROW
operator|.
name|length
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"FilterAllRemaining"
argument_list|,
name|filter
operator|.
name|filterAllRemaining
argument_list|()
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
literal|"FilterNotNull"
argument_list|,
name|filter
operator|.
name|filterRow
argument_list|()
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
literal|"Filter a null"
argument_list|,
name|filter
operator|.
name|filterRowKey
argument_list|(
literal|null
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

