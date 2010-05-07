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
name|KeyValue
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
name|filter
operator|.
name|CompareFilter
operator|.
name|CompareOp
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

begin_comment
comment|/**  * Tests for {@link SingleColumnValueExcludeFilter}. Because this filter  * extends {@link SingleColumnValueFilter}, only the added functionality is  * tested. That is, method filterKeyValue(KeyValue).  *  * @author ferdy  *  */
end_comment

begin_class
specifier|public
class|class
name|TestSingleColumnValueExcludeFilter
extends|extends
name|TestCase
block|{
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|ROW
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"test"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|COLUMN_FAMILY
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"test"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|COLUMN_QUALIFIER
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"foo"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|COLUMN_QUALIFIER_2
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"foo_2"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|VAL_1
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"a"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|VAL_2
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"ab"
argument_list|)
decl_stmt|;
comment|/**    * Test the overridden functionality of filterKeyValue(KeyValue)    * @throws Exception    */
specifier|public
name|void
name|testFilterKeyValue
parameter_list|()
throws|throws
name|Exception
block|{
name|Filter
name|filter
init|=
operator|new
name|SingleColumnValueExcludeFilter
argument_list|(
name|COLUMN_FAMILY
argument_list|,
name|COLUMN_QUALIFIER
argument_list|,
name|CompareOp
operator|.
name|EQUAL
argument_list|,
name|VAL_1
argument_list|)
decl_stmt|;
comment|// A 'match' situation
name|KeyValue
name|kv
decl_stmt|;
name|kv
operator|=
operator|new
name|KeyValue
argument_list|(
name|ROW
argument_list|,
name|COLUMN_FAMILY
argument_list|,
name|COLUMN_QUALIFIER_2
argument_list|,
name|VAL_1
argument_list|)
expr_stmt|;
comment|// INCLUDE expected because test column has not yet passed
name|assertTrue
argument_list|(
literal|"otherColumn"
argument_list|,
name|filter
operator|.
name|filterKeyValue
argument_list|(
name|kv
argument_list|)
operator|==
name|Filter
operator|.
name|ReturnCode
operator|.
name|INCLUDE
argument_list|)
expr_stmt|;
name|kv
operator|=
operator|new
name|KeyValue
argument_list|(
name|ROW
argument_list|,
name|COLUMN_FAMILY
argument_list|,
name|COLUMN_QUALIFIER
argument_list|,
name|VAL_1
argument_list|)
expr_stmt|;
comment|// Test column will pass (will match), will SKIP because test columns are excluded
name|assertTrue
argument_list|(
literal|"testedMatch"
argument_list|,
name|filter
operator|.
name|filterKeyValue
argument_list|(
name|kv
argument_list|)
operator|==
name|Filter
operator|.
name|ReturnCode
operator|.
name|SKIP
argument_list|)
expr_stmt|;
comment|// Test column has already passed and matched, all subsequent columns are INCLUDE
name|kv
operator|=
operator|new
name|KeyValue
argument_list|(
name|ROW
argument_list|,
name|COLUMN_FAMILY
argument_list|,
name|COLUMN_QUALIFIER_2
argument_list|,
name|VAL_1
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"otherColumn"
argument_list|,
name|filter
operator|.
name|filterKeyValue
argument_list|(
name|kv
argument_list|)
operator|==
name|Filter
operator|.
name|ReturnCode
operator|.
name|INCLUDE
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
literal|"allRemainingWhenMatch"
argument_list|,
name|filter
operator|.
name|filterAllRemaining
argument_list|()
argument_list|)
expr_stmt|;
comment|// A 'mismatch' situation
name|filter
operator|.
name|reset
argument_list|()
expr_stmt|;
comment|// INCLUDE expected because test column has not yet passed
name|kv
operator|=
operator|new
name|KeyValue
argument_list|(
name|ROW
argument_list|,
name|COLUMN_FAMILY
argument_list|,
name|COLUMN_QUALIFIER_2
argument_list|,
name|VAL_1
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"otherColumn"
argument_list|,
name|filter
operator|.
name|filterKeyValue
argument_list|(
name|kv
argument_list|)
operator|==
name|Filter
operator|.
name|ReturnCode
operator|.
name|INCLUDE
argument_list|)
expr_stmt|;
comment|// Test column will pass (wont match), expect NEXT_ROW
name|kv
operator|=
operator|new
name|KeyValue
argument_list|(
name|ROW
argument_list|,
name|COLUMN_FAMILY
argument_list|,
name|COLUMN_QUALIFIER
argument_list|,
name|VAL_2
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"testedMismatch"
argument_list|,
name|filter
operator|.
name|filterKeyValue
argument_list|(
name|kv
argument_list|)
operator|==
name|Filter
operator|.
name|ReturnCode
operator|.
name|NEXT_ROW
argument_list|)
expr_stmt|;
comment|// After a mismatch (at least with LatestVersionOnly), subsequent columns are EXCLUDE
name|kv
operator|=
operator|new
name|KeyValue
argument_list|(
name|ROW
argument_list|,
name|COLUMN_FAMILY
argument_list|,
name|COLUMN_QUALIFIER_2
argument_list|,
name|VAL_1
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"otherColumn"
argument_list|,
name|filter
operator|.
name|filterKeyValue
argument_list|(
name|kv
argument_list|)
operator|==
name|Filter
operator|.
name|ReturnCode
operator|.
name|NEXT_ROW
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

