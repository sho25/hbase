begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Copyright 2010 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|util
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
name|MediumTests
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
import|import static
name|org
operator|.
name|mockito
operator|.
name|Mockito
operator|.
name|mock
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|mockito
operator|.
name|Mockito
operator|.
name|when
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|mockito
operator|.
name|Mockito
operator|.
name|verify
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
name|assertTrue
import|;
end_import

begin_class
annotation|@
name|Category
argument_list|(
name|MediumTests
operator|.
name|class
argument_list|)
specifier|public
class|class
name|TestEnvironmentEdgeManager
block|{
annotation|@
name|Test
specifier|public
name|void
name|testManageSingleton
parameter_list|()
block|{
name|EnvironmentEdgeManager
operator|.
name|reset
argument_list|()
expr_stmt|;
name|EnvironmentEdge
name|edge
init|=
name|EnvironmentEdgeManager
operator|.
name|getDelegate
argument_list|()
decl_stmt|;
name|assertNotNull
argument_list|(
name|edge
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|edge
operator|instanceof
name|DefaultEnvironmentEdge
argument_list|)
expr_stmt|;
name|EnvironmentEdgeManager
operator|.
name|reset
argument_list|()
expr_stmt|;
name|EnvironmentEdge
name|edge2
init|=
name|EnvironmentEdgeManager
operator|.
name|getDelegate
argument_list|()
decl_stmt|;
name|assertFalse
argument_list|(
name|edge
operator|==
name|edge2
argument_list|)
expr_stmt|;
name|IncrementingEnvironmentEdge
name|newEdge
init|=
operator|new
name|IncrementingEnvironmentEdge
argument_list|()
decl_stmt|;
name|EnvironmentEdgeManager
operator|.
name|injectEdge
argument_list|(
name|newEdge
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|newEdge
argument_list|,
name|EnvironmentEdgeManager
operator|.
name|getDelegate
argument_list|()
argument_list|)
expr_stmt|;
comment|//injecting null will result in default being assigned.
name|EnvironmentEdgeManager
operator|.
name|injectEdge
argument_list|(
literal|null
argument_list|)
expr_stmt|;
name|EnvironmentEdge
name|nullResult
init|=
name|EnvironmentEdgeManager
operator|.
name|getDelegate
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
name|nullResult
operator|instanceof
name|DefaultEnvironmentEdge
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testCurrentTimeInMillis
parameter_list|()
block|{
name|EnvironmentEdge
name|mock
init|=
name|mock
argument_list|(
name|EnvironmentEdge
operator|.
name|class
argument_list|)
decl_stmt|;
name|EnvironmentEdgeManager
operator|.
name|injectEdge
argument_list|(
name|mock
argument_list|)
expr_stmt|;
name|long
name|expectation
init|=
literal|3456
decl_stmt|;
name|when
argument_list|(
name|mock
operator|.
name|currentTimeMillis
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|expectation
argument_list|)
expr_stmt|;
name|long
name|result
init|=
name|EnvironmentEdgeManager
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|verify
argument_list|(
name|mock
argument_list|)
operator|.
name|currentTimeMillis
argument_list|()
expr_stmt|;
name|assertEquals
argument_list|(
name|expectation
argument_list|,
name|result
argument_list|)
expr_stmt|;
block|}
annotation|@
name|org
operator|.
name|junit
operator|.
name|Rule
specifier|public
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|ResourceCheckerJUnitRule
name|cu
init|=
operator|new
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|ResourceCheckerJUnitRule
argument_list|()
decl_stmt|;
block|}
end_class

end_unit

