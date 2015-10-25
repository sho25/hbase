begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|testclassification
operator|.
name|ClientTests
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
name|Before
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
name|ClientTests
operator|.
name|class
block|,
name|SmallTests
operator|.
name|class
block|}
argument_list|)
comment|/**  * Addresses HBASE-6047  * We test put.has call with all of its polymorphic magic  */
specifier|public
class|class
name|TestPutDotHas
block|{
specifier|public
specifier|static
specifier|final
name|byte
index|[]
name|ROW_01
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"row-01"
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|byte
index|[]
name|QUALIFIER_01
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"qualifier-01"
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|byte
index|[]
name|VALUE_01
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"value-01"
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|byte
index|[]
name|FAMILY_01
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"family-01"
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|long
name|TS
init|=
literal|1234567L
decl_stmt|;
specifier|public
name|Put
name|put
init|=
operator|new
name|Put
argument_list|(
name|ROW_01
argument_list|)
decl_stmt|;
annotation|@
name|Before
specifier|public
name|void
name|setUp
parameter_list|()
block|{
name|put
operator|.
name|addColumn
argument_list|(
name|FAMILY_01
argument_list|,
name|QUALIFIER_01
argument_list|,
name|TS
argument_list|,
name|VALUE_01
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testHasIgnoreValueIgnoreTS
parameter_list|()
block|{
name|Assert
operator|.
name|assertTrue
argument_list|(
name|put
operator|.
name|has
argument_list|(
name|FAMILY_01
argument_list|,
name|QUALIFIER_01
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertFalse
argument_list|(
name|put
operator|.
name|has
argument_list|(
name|QUALIFIER_01
argument_list|,
name|FAMILY_01
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testHasIgnoreValue
parameter_list|()
block|{
name|Assert
operator|.
name|assertTrue
argument_list|(
name|put
operator|.
name|has
argument_list|(
name|FAMILY_01
argument_list|,
name|QUALIFIER_01
argument_list|,
name|TS
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertFalse
argument_list|(
name|put
operator|.
name|has
argument_list|(
name|FAMILY_01
argument_list|,
name|QUALIFIER_01
argument_list|,
name|TS
operator|+
literal|1
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testHasIgnoreTS
parameter_list|()
block|{
name|Assert
operator|.
name|assertTrue
argument_list|(
name|put
operator|.
name|has
argument_list|(
name|FAMILY_01
argument_list|,
name|QUALIFIER_01
argument_list|,
name|VALUE_01
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertFalse
argument_list|(
name|put
operator|.
name|has
argument_list|(
name|FAMILY_01
argument_list|,
name|VALUE_01
argument_list|,
name|QUALIFIER_01
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testHas
parameter_list|()
block|{
name|Assert
operator|.
name|assertTrue
argument_list|(
name|put
operator|.
name|has
argument_list|(
name|FAMILY_01
argument_list|,
name|QUALIFIER_01
argument_list|,
name|TS
argument_list|,
name|VALUE_01
argument_list|)
argument_list|)
expr_stmt|;
comment|// Bad TS
name|Assert
operator|.
name|assertFalse
argument_list|(
name|put
operator|.
name|has
argument_list|(
name|FAMILY_01
argument_list|,
name|QUALIFIER_01
argument_list|,
name|TS
operator|+
literal|1
argument_list|,
name|VALUE_01
argument_list|)
argument_list|)
expr_stmt|;
comment|// Bad Value
name|Assert
operator|.
name|assertFalse
argument_list|(
name|put
operator|.
name|has
argument_list|(
name|FAMILY_01
argument_list|,
name|QUALIFIER_01
argument_list|,
name|TS
argument_list|,
name|QUALIFIER_01
argument_list|)
argument_list|)
expr_stmt|;
comment|// Bad Family
name|Assert
operator|.
name|assertFalse
argument_list|(
name|put
operator|.
name|has
argument_list|(
name|QUALIFIER_01
argument_list|,
name|QUALIFIER_01
argument_list|,
name|TS
argument_list|,
name|VALUE_01
argument_list|)
argument_list|)
expr_stmt|;
comment|// Bad Qual
name|Assert
operator|.
name|assertFalse
argument_list|(
name|put
operator|.
name|has
argument_list|(
name|FAMILY_01
argument_list|,
name|FAMILY_01
argument_list|,
name|TS
argument_list|,
name|VALUE_01
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

