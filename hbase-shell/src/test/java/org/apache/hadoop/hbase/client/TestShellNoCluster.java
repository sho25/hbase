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
name|client
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|IOException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|ArrayList
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|List
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
name|MediumTests
import|;
end_import

begin_import
import|import
name|org
operator|.
name|jruby
operator|.
name|embed
operator|.
name|PathType
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|AfterClass
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|BeforeClass
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
name|ClientTests
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
name|TestShellNoCluster
extends|extends
name|AbstractTestShell
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
name|TestShellNoCluster
operator|.
name|class
argument_list|)
decl_stmt|;
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|setUpBeforeClass
parameter_list|()
throws|throws
name|Exception
block|{
comment|// no cluster
name|List
argument_list|<
name|String
argument_list|>
name|loadPaths
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
literal|2
argument_list|)
decl_stmt|;
name|loadPaths
operator|.
name|add
argument_list|(
literal|"src/main/ruby"
argument_list|)
expr_stmt|;
name|loadPaths
operator|.
name|add
argument_list|(
literal|"src/test/ruby"
argument_list|)
expr_stmt|;
name|jruby
operator|.
name|setLoadPaths
argument_list|(
name|loadPaths
argument_list|)
expr_stmt|;
name|jruby
operator|.
name|put
argument_list|(
literal|"$TEST_CLUSTER"
argument_list|,
name|TEST_UTIL
argument_list|)
expr_stmt|;
name|System
operator|.
name|setProperty
argument_list|(
literal|"jruby.jit.logging.verbose"
argument_list|,
literal|"true"
argument_list|)
expr_stmt|;
name|System
operator|.
name|setProperty
argument_list|(
literal|"jruby.jit.logging"
argument_list|,
literal|"true"
argument_list|)
expr_stmt|;
name|System
operator|.
name|setProperty
argument_list|(
literal|"jruby.native.verbose"
argument_list|,
literal|"true"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|AfterClass
specifier|public
specifier|static
name|void
name|tearDownAfterClass
parameter_list|()
throws|throws
name|Exception
block|{
comment|// no cluster
block|}
annotation|@
name|Test
specifier|public
name|void
name|testRunNoClusterShellTests
parameter_list|()
throws|throws
name|IOException
block|{
comment|// Start ruby tests without cluster
name|jruby
operator|.
name|runScriptlet
argument_list|(
name|PathType
operator|.
name|ABSOLUTE
argument_list|,
literal|"src/test/ruby/no_cluster_tests_runner.rb"
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

