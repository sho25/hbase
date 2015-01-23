begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one or more contributor license  * agreements. See the NOTICE file distributed with this work for additional information regarding  * copyright ownership. The ASF licenses this file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance with the License. You may obtain a  * copy of the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable  * law or agreed to in writing, software distributed under the License is distributed on an "AS IS"  * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License  * for the specific language governing permissions and limitations under the License.  */
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
name|regionserver
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
name|*
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
name|RegionServerTests
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
name|regionserver
operator|.
name|MemStoreFlusher
operator|.
name|FlushRegionEntry
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
name|EnvironmentEdgeManager
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
name|ManualEnvironmentEdge
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|After
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
name|mockito
operator|.
name|Mockito
import|;
end_import

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|RegionServerTests
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
name|TestFlushRegionEntry
block|{
annotation|@
name|Before
specifier|public
name|void
name|setUp
parameter_list|()
throws|throws
name|Exception
block|{
name|ManualEnvironmentEdge
name|edge
init|=
operator|new
name|ManualEnvironmentEdge
argument_list|()
decl_stmt|;
name|edge
operator|.
name|setValue
argument_list|(
literal|12345
argument_list|)
expr_stmt|;
name|EnvironmentEdgeManager
operator|.
name|injectEdge
argument_list|(
name|edge
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|test
parameter_list|()
block|{
name|HRegion
name|r
init|=
name|Mockito
operator|.
name|mock
argument_list|(
name|HRegion
operator|.
name|class
argument_list|)
decl_stmt|;
name|FlushRegionEntry
name|entry
init|=
operator|new
name|FlushRegionEntry
argument_list|(
name|r
argument_list|,
literal|true
argument_list|)
decl_stmt|;
name|FlushRegionEntry
name|other
init|=
operator|new
name|FlushRegionEntry
argument_list|(
name|r
argument_list|,
literal|true
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|entry
operator|.
name|hashCode
argument_list|()
argument_list|,
name|other
operator|.
name|hashCode
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|entry
argument_list|,
name|other
argument_list|)
expr_stmt|;
block|}
annotation|@
name|After
specifier|public
name|void
name|teardown
parameter_list|()
block|{
name|EnvironmentEdgeManager
operator|.
name|reset
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

