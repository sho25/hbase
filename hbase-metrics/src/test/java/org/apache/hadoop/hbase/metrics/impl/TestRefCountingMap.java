begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
operator|.
name|impl
package|;
end_package

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
name|assertNotNull
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
name|assertNull
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
name|java
operator|.
name|util
operator|.
name|Collection
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
name|shaded
operator|.
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Lists
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
name|TestRefCountingMap
block|{
specifier|private
name|RefCountingMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|map
decl_stmt|;
annotation|@
name|Before
specifier|public
name|void
name|setUp
parameter_list|()
block|{
name|map
operator|=
operator|new
name|RefCountingMap
argument_list|<>
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testPutGet
parameter_list|()
block|{
name|map
operator|.
name|put
argument_list|(
literal|"foo"
argument_list|,
parameter_list|()
lambda|->
literal|"foovalue"
argument_list|)
expr_stmt|;
name|String
name|v
init|=
name|map
operator|.
name|get
argument_list|(
literal|"foo"
argument_list|)
decl_stmt|;
name|assertNotNull
argument_list|(
name|v
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"foovalue"
argument_list|,
name|v
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testPutMulti
parameter_list|()
block|{
name|String
name|v1
init|=
name|map
operator|.
name|put
argument_list|(
literal|"foo"
argument_list|,
parameter_list|()
lambda|->
literal|"foovalue"
argument_list|)
decl_stmt|;
name|String
name|v2
init|=
name|map
operator|.
name|put
argument_list|(
literal|"foo"
argument_list|,
parameter_list|()
lambda|->
literal|"foovalue2"
argument_list|)
decl_stmt|;
name|String
name|v3
init|=
name|map
operator|.
name|put
argument_list|(
literal|"foo"
argument_list|,
parameter_list|()
lambda|->
literal|"foovalue3"
argument_list|)
decl_stmt|;
name|String
name|v
init|=
name|map
operator|.
name|get
argument_list|(
literal|"foo"
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"foovalue"
argument_list|,
name|v
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|v
argument_list|,
name|v1
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|v
argument_list|,
name|v2
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|v
argument_list|,
name|v3
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testPutRemove
parameter_list|()
block|{
name|map
operator|.
name|put
argument_list|(
literal|"foo"
argument_list|,
parameter_list|()
lambda|->
literal|"foovalue"
argument_list|)
expr_stmt|;
name|String
name|v
init|=
name|map
operator|.
name|remove
argument_list|(
literal|"foo"
argument_list|)
decl_stmt|;
name|assertNull
argument_list|(
name|v
argument_list|)
expr_stmt|;
name|v
operator|=
name|map
operator|.
name|get
argument_list|(
literal|"foo"
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|v
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testPutRemoveMulti
parameter_list|()
block|{
name|map
operator|.
name|put
argument_list|(
literal|"foo"
argument_list|,
parameter_list|()
lambda|->
literal|"foovalue"
argument_list|)
expr_stmt|;
name|map
operator|.
name|put
argument_list|(
literal|"foo"
argument_list|,
parameter_list|()
lambda|->
literal|"foovalue2"
argument_list|)
expr_stmt|;
name|map
operator|.
name|put
argument_list|(
literal|"foo"
argument_list|,
parameter_list|()
lambda|->
literal|"foovalue3"
argument_list|)
expr_stmt|;
comment|// remove 1
name|String
name|v
init|=
name|map
operator|.
name|remove
argument_list|(
literal|"foo"
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"foovalue"
argument_list|,
name|v
argument_list|)
expr_stmt|;
comment|// remove 2
name|v
operator|=
name|map
operator|.
name|remove
argument_list|(
literal|"foo"
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"foovalue"
argument_list|,
name|v
argument_list|)
expr_stmt|;
comment|// remove 3
name|v
operator|=
name|map
operator|.
name|remove
argument_list|(
literal|"foo"
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|v
argument_list|)
expr_stmt|;
name|v
operator|=
name|map
operator|.
name|get
argument_list|(
literal|"foo"
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|v
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testSize
parameter_list|()
block|{
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|map
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
comment|// put a key
name|map
operator|.
name|put
argument_list|(
literal|"foo"
argument_list|,
parameter_list|()
lambda|->
literal|"foovalue"
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|map
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
comment|// put a different key
name|map
operator|.
name|put
argument_list|(
literal|"bar"
argument_list|,
parameter_list|()
lambda|->
literal|"foovalue2"
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|map
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
comment|// put the same key again
name|map
operator|.
name|put
argument_list|(
literal|"bar"
argument_list|,
parameter_list|()
lambda|->
literal|"foovalue3"
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|map
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
comment|// map should be same size
block|}
annotation|@
name|Test
specifier|public
name|void
name|testClear
parameter_list|()
block|{
name|map
operator|.
name|put
argument_list|(
literal|"foo"
argument_list|,
parameter_list|()
lambda|->
literal|"foovalue"
argument_list|)
expr_stmt|;
name|map
operator|.
name|put
argument_list|(
literal|"bar"
argument_list|,
parameter_list|()
lambda|->
literal|"foovalue2"
argument_list|)
expr_stmt|;
name|map
operator|.
name|put
argument_list|(
literal|"baz"
argument_list|,
parameter_list|()
lambda|->
literal|"foovalue3"
argument_list|)
expr_stmt|;
name|map
operator|.
name|clear
argument_list|()
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|map
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testKeySet
parameter_list|()
block|{
name|map
operator|.
name|put
argument_list|(
literal|"foo"
argument_list|,
parameter_list|()
lambda|->
literal|"foovalue"
argument_list|)
expr_stmt|;
name|map
operator|.
name|put
argument_list|(
literal|"bar"
argument_list|,
parameter_list|()
lambda|->
literal|"foovalue2"
argument_list|)
expr_stmt|;
name|map
operator|.
name|put
argument_list|(
literal|"baz"
argument_list|,
parameter_list|()
lambda|->
literal|"foovalue3"
argument_list|)
expr_stmt|;
name|Set
argument_list|<
name|String
argument_list|>
name|keys
init|=
name|map
operator|.
name|keySet
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|3
argument_list|,
name|keys
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|Lists
operator|.
name|newArrayList
argument_list|(
literal|"foo"
argument_list|,
literal|"bar"
argument_list|,
literal|"baz"
argument_list|)
operator|.
name|stream
argument_list|()
operator|.
name|forEach
argument_list|(
name|v
lambda|->
name|assertTrue
argument_list|(
name|keys
operator|.
name|contains
argument_list|(
name|v
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testValues
parameter_list|()
block|{
name|map
operator|.
name|put
argument_list|(
literal|"foo"
argument_list|,
parameter_list|()
lambda|->
literal|"foovalue"
argument_list|)
expr_stmt|;
name|map
operator|.
name|put
argument_list|(
literal|"foo"
argument_list|,
parameter_list|()
lambda|->
literal|"foovalue2"
argument_list|)
expr_stmt|;
name|map
operator|.
name|put
argument_list|(
literal|"bar"
argument_list|,
parameter_list|()
lambda|->
literal|"foovalue3"
argument_list|)
expr_stmt|;
name|map
operator|.
name|put
argument_list|(
literal|"baz"
argument_list|,
parameter_list|()
lambda|->
literal|"foovalue4"
argument_list|)
expr_stmt|;
name|Collection
argument_list|<
name|String
argument_list|>
name|values
init|=
name|map
operator|.
name|values
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|3
argument_list|,
name|values
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|Lists
operator|.
name|newArrayList
argument_list|(
literal|"foovalue"
argument_list|,
literal|"foovalue3"
argument_list|,
literal|"foovalue4"
argument_list|)
operator|.
name|stream
argument_list|()
operator|.
name|forEach
argument_list|(
name|v
lambda|->
name|assertTrue
argument_list|(
name|values
operator|.
name|contains
argument_list|(
name|v
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

