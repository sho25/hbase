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
name|master
package|;
end_package

begin_import
import|import static
name|junit
operator|.
name|framework
operator|.
name|TestCase
operator|.
name|assertFalse
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
name|assertNotEquals
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
name|ServerName
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
name|client
operator|.
name|RegionInfo
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
name|client
operator|.
name|RegionInfoBuilder
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
name|MasterTests
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
name|ClassRule
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Rule
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
name|org
operator|.
name|junit
operator|.
name|rules
operator|.
name|TestName
import|;
end_import

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|MasterTests
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
name|TestRegionPlan
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
name|TestRegionPlan
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|ServerName
name|SRC
init|=
name|ServerName
operator|.
name|valueOf
argument_list|(
literal|"source"
argument_list|,
literal|1234
argument_list|,
literal|2345
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|ServerName
name|DEST
init|=
name|ServerName
operator|.
name|valueOf
argument_list|(
literal|"dest"
argument_list|,
literal|1234
argument_list|,
literal|2345
argument_list|)
decl_stmt|;
annotation|@
name|Rule
specifier|public
name|TestName
name|name
init|=
operator|new
name|TestName
argument_list|()
decl_stmt|;
annotation|@
name|Test
specifier|public
name|void
name|testCompareTo
parameter_list|()
block|{
name|RegionInfo
name|hri
init|=
name|RegionInfoBuilder
operator|.
name|newBuilder
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
name|name
operator|.
name|getMethodName
argument_list|()
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|RegionPlan
name|a
init|=
operator|new
name|RegionPlan
argument_list|(
name|hri
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|RegionPlan
name|b
init|=
operator|new
name|RegionPlan
argument_list|(
name|hri
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|a
operator|.
name|compareTo
argument_list|(
name|b
argument_list|)
argument_list|)
expr_stmt|;
name|a
operator|=
operator|new
name|RegionPlan
argument_list|(
name|hri
argument_list|,
name|SRC
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|b
operator|=
operator|new
name|RegionPlan
argument_list|(
name|hri
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|a
operator|.
name|compareTo
argument_list|(
name|b
argument_list|)
argument_list|)
expr_stmt|;
name|a
operator|=
operator|new
name|RegionPlan
argument_list|(
name|hri
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|b
operator|=
operator|new
name|RegionPlan
argument_list|(
name|hri
argument_list|,
name|SRC
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
operator|-
literal|1
argument_list|,
name|a
operator|.
name|compareTo
argument_list|(
name|b
argument_list|)
argument_list|)
expr_stmt|;
name|a
operator|=
operator|new
name|RegionPlan
argument_list|(
name|hri
argument_list|,
name|SRC
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|b
operator|=
operator|new
name|RegionPlan
argument_list|(
name|hri
argument_list|,
name|SRC
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|a
operator|.
name|compareTo
argument_list|(
name|b
argument_list|)
argument_list|)
expr_stmt|;
name|a
operator|=
operator|new
name|RegionPlan
argument_list|(
name|hri
argument_list|,
name|SRC
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|b
operator|=
operator|new
name|RegionPlan
argument_list|(
name|hri
argument_list|,
name|SRC
argument_list|,
name|DEST
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
operator|-
literal|1
argument_list|,
name|a
operator|.
name|compareTo
argument_list|(
name|b
argument_list|)
argument_list|)
expr_stmt|;
name|a
operator|=
operator|new
name|RegionPlan
argument_list|(
name|hri
argument_list|,
name|SRC
argument_list|,
name|DEST
argument_list|)
expr_stmt|;
name|b
operator|=
operator|new
name|RegionPlan
argument_list|(
name|hri
argument_list|,
name|SRC
argument_list|,
name|DEST
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|a
operator|.
name|compareTo
argument_list|(
name|b
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testEqualsWithNulls
parameter_list|()
block|{
name|RegionInfo
name|hri
init|=
name|RegionInfoBuilder
operator|.
name|newBuilder
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
name|name
operator|.
name|getMethodName
argument_list|()
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|RegionPlan
name|a
init|=
operator|new
name|RegionPlan
argument_list|(
name|hri
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|RegionPlan
name|b
init|=
operator|new
name|RegionPlan
argument_list|(
name|hri
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|a
operator|.
name|equals
argument_list|(
name|b
argument_list|)
argument_list|)
expr_stmt|;
name|a
operator|=
operator|new
name|RegionPlan
argument_list|(
name|hri
argument_list|,
name|SRC
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|b
operator|=
operator|new
name|RegionPlan
argument_list|(
name|hri
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|a
operator|.
name|equals
argument_list|(
name|b
argument_list|)
argument_list|)
expr_stmt|;
name|a
operator|=
operator|new
name|RegionPlan
argument_list|(
name|hri
argument_list|,
name|SRC
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|b
operator|=
operator|new
name|RegionPlan
argument_list|(
name|hri
argument_list|,
name|SRC
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|a
operator|.
name|equals
argument_list|(
name|b
argument_list|)
argument_list|)
expr_stmt|;
name|a
operator|=
operator|new
name|RegionPlan
argument_list|(
name|hri
argument_list|,
name|SRC
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|b
operator|=
operator|new
name|RegionPlan
argument_list|(
name|hri
argument_list|,
name|SRC
argument_list|,
name|DEST
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|a
operator|.
name|equals
argument_list|(
name|b
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testEquals
parameter_list|()
block|{
name|RegionInfo
name|hri
init|=
name|RegionInfoBuilder
operator|.
name|newBuilder
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
name|name
operator|.
name|getMethodName
argument_list|()
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
comment|// Identity equality
name|RegionPlan
name|plan
init|=
operator|new
name|RegionPlan
argument_list|(
name|hri
argument_list|,
name|SRC
argument_list|,
name|DEST
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|plan
operator|.
name|hashCode
argument_list|()
argument_list|,
operator|new
name|RegionPlan
argument_list|(
name|hri
argument_list|,
name|SRC
argument_list|,
name|DEST
argument_list|)
operator|.
name|hashCode
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|plan
argument_list|,
operator|new
name|RegionPlan
argument_list|(
name|hri
argument_list|,
name|SRC
argument_list|,
name|DEST
argument_list|)
argument_list|)
expr_stmt|;
comment|// HRI is used for equality
name|RegionInfo
name|other
init|=
name|RegionInfoBuilder
operator|.
name|newBuilder
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
name|name
operator|.
name|getMethodName
argument_list|()
operator|+
literal|"other"
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|assertNotEquals
argument_list|(
name|plan
operator|.
name|hashCode
argument_list|()
argument_list|,
operator|new
name|RegionPlan
argument_list|(
name|other
argument_list|,
name|SRC
argument_list|,
name|DEST
argument_list|)
operator|.
name|hashCode
argument_list|()
argument_list|)
expr_stmt|;
name|assertNotEquals
argument_list|(
name|plan
argument_list|,
operator|new
name|RegionPlan
argument_list|(
name|other
argument_list|,
name|SRC
argument_list|,
name|DEST
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

