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
name|util
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
name|HBaseTestingUtility
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
name|security
operator|.
name|UserProvider
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
name|MediumTests
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
name|MiscTests
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
name|MiscTests
operator|.
name|class
block|,
name|MediumTests
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|TestConnectionCache
extends|extends
name|TestCase
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
name|TestConnectionCache
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|HBaseTestingUtility
name|UTIL
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
comment|/**    * test for ConnectionCache cleaning expired Connection    */
annotation|@
name|Test
specifier|public
name|void
name|testConnectionChore
parameter_list|()
throws|throws
name|Exception
block|{
name|UTIL
operator|.
name|startMiniCluster
argument_list|()
expr_stmt|;
comment|//1s for clean interval& 5s for maxIdleTime
name|ConnectionCache
name|cache
init|=
operator|new
name|ConnectionCache
argument_list|(
name|UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|UserProvider
operator|.
name|instantiate
argument_list|(
name|UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
argument_list|,
literal|1000
argument_list|,
literal|5000
argument_list|)
decl_stmt|;
name|ConnectionCache
operator|.
name|ConnectionInfo
name|info
init|=
name|cache
operator|.
name|getCurrentConnection
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|false
argument_list|,
name|info
operator|.
name|connection
operator|.
name|isClosed
argument_list|()
argument_list|)
expr_stmt|;
name|Thread
operator|.
name|sleep
argument_list|(
literal|7000
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|true
argument_list|,
name|info
operator|.
name|connection
operator|.
name|isClosed
argument_list|()
argument_list|)
expr_stmt|;
name|UTIL
operator|.
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

