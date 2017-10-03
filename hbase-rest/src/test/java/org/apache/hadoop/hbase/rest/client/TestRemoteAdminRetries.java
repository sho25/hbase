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
name|rest
operator|.
name|client
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
name|assertTrue
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
name|fail
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|mockito
operator|.
name|Matchers
operator|.
name|any
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|mockito
operator|.
name|Matchers
operator|.
name|anyString
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
name|times
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
name|mockito
operator|.
name|Mockito
operator|.
name|when
import|;
end_import

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
name|regex
operator|.
name|Pattern
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
name|conf
operator|.
name|Configuration
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
name|HTableDescriptor
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
name|testclassification
operator|.
name|RestTests
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

begin_comment
comment|/**  * Tests {@link RemoteAdmin} retries.  */
end_comment

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|RestTests
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
name|TestRemoteAdminRetries
block|{
specifier|private
specifier|static
specifier|final
name|int
name|SLEEP_TIME
init|=
literal|50
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|RETRIES
init|=
literal|3
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|long
name|MAX_TIME
init|=
name|SLEEP_TIME
operator|*
operator|(
name|RETRIES
operator|-
literal|1
operator|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|HBaseTestingUtility
name|TEST_UTIL
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
specifier|private
name|RemoteAdmin
name|remoteAdmin
decl_stmt|;
specifier|private
name|Client
name|client
decl_stmt|;
annotation|@
name|Before
specifier|public
name|void
name|setup
parameter_list|()
throws|throws
name|Exception
block|{
name|client
operator|=
name|mock
argument_list|(
name|Client
operator|.
name|class
argument_list|)
expr_stmt|;
name|Response
name|response
init|=
operator|new
name|Response
argument_list|(
literal|509
argument_list|)
decl_stmt|;
name|when
argument_list|(
name|client
operator|.
name|get
argument_list|(
name|anyString
argument_list|()
argument_list|,
name|anyString
argument_list|()
argument_list|)
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|response
argument_list|)
expr_stmt|;
name|when
argument_list|(
name|client
operator|.
name|delete
argument_list|(
name|anyString
argument_list|()
argument_list|)
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|response
argument_list|)
expr_stmt|;
name|when
argument_list|(
name|client
operator|.
name|put
argument_list|(
name|anyString
argument_list|()
argument_list|,
name|anyString
argument_list|()
argument_list|,
name|any
argument_list|()
argument_list|)
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|response
argument_list|)
expr_stmt|;
name|when
argument_list|(
name|client
operator|.
name|post
argument_list|(
name|anyString
argument_list|()
argument_list|,
name|anyString
argument_list|()
argument_list|,
name|any
argument_list|()
argument_list|)
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|response
argument_list|)
expr_stmt|;
name|Configuration
name|configuration
init|=
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
name|configuration
operator|.
name|setInt
argument_list|(
literal|"hbase.rest.client.max.retries"
argument_list|,
name|RETRIES
argument_list|)
expr_stmt|;
name|configuration
operator|.
name|setInt
argument_list|(
literal|"hbase.rest.client.sleep"
argument_list|,
name|SLEEP_TIME
argument_list|)
expr_stmt|;
name|remoteAdmin
operator|=
operator|new
name|RemoteAdmin
argument_list|(
name|client
argument_list|,
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
literal|"MyTable"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testFailingGetRestVersion
parameter_list|()
throws|throws
name|Exception
block|{
name|testTimedOutGetCall
argument_list|(
operator|new
name|CallExecutor
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
throws|throws
name|Exception
block|{
name|remoteAdmin
operator|.
name|getRestVersion
argument_list|()
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testFailingGetClusterStatus
parameter_list|()
throws|throws
name|Exception
block|{
name|testTimedOutGetCall
argument_list|(
operator|new
name|CallExecutor
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
throws|throws
name|Exception
block|{
name|remoteAdmin
operator|.
name|getClusterStatus
argument_list|()
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testFailingGetClusterVersion
parameter_list|()
throws|throws
name|Exception
block|{
name|testTimedOutGetCall
argument_list|(
operator|new
name|CallExecutor
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
throws|throws
name|Exception
block|{
name|remoteAdmin
operator|.
name|getClusterVersion
argument_list|()
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testFailingGetTableAvailable
parameter_list|()
throws|throws
name|Exception
block|{
name|testTimedOutCall
argument_list|(
operator|new
name|CallExecutor
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
throws|throws
name|Exception
block|{
name|remoteAdmin
operator|.
name|isTableAvailable
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"TestTable"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
annotation|@
name|SuppressWarnings
argument_list|(
literal|"deprecation"
argument_list|)
specifier|public
name|void
name|testFailingCreateTable
parameter_list|()
throws|throws
name|Exception
block|{
name|testTimedOutCall
argument_list|(
operator|new
name|CallExecutor
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
throws|throws
name|Exception
block|{
name|remoteAdmin
operator|.
name|createTable
argument_list|(
operator|new
name|HTableDescriptor
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"TestTable"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
name|verify
argument_list|(
name|client
argument_list|,
name|times
argument_list|(
name|RETRIES
argument_list|)
argument_list|)
operator|.
name|put
argument_list|(
name|anyString
argument_list|()
argument_list|,
name|anyString
argument_list|()
argument_list|,
name|any
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testFailingDeleteTable
parameter_list|()
throws|throws
name|Exception
block|{
name|testTimedOutCall
argument_list|(
operator|new
name|CallExecutor
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
throws|throws
name|Exception
block|{
name|remoteAdmin
operator|.
name|deleteTable
argument_list|(
literal|"TestTable"
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
name|verify
argument_list|(
name|client
argument_list|,
name|times
argument_list|(
name|RETRIES
argument_list|)
argument_list|)
operator|.
name|delete
argument_list|(
name|anyString
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testFailingGetTableList
parameter_list|()
throws|throws
name|Exception
block|{
name|testTimedOutGetCall
argument_list|(
operator|new
name|CallExecutor
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
throws|throws
name|Exception
block|{
name|remoteAdmin
operator|.
name|getTableList
argument_list|()
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|testTimedOutGetCall
parameter_list|(
name|CallExecutor
name|callExecutor
parameter_list|)
throws|throws
name|Exception
block|{
name|testTimedOutCall
argument_list|(
name|callExecutor
argument_list|)
expr_stmt|;
name|verify
argument_list|(
name|client
argument_list|,
name|times
argument_list|(
name|RETRIES
argument_list|)
argument_list|)
operator|.
name|get
argument_list|(
name|anyString
argument_list|()
argument_list|,
name|anyString
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|testTimedOutCall
parameter_list|(
name|CallExecutor
name|callExecutor
parameter_list|)
throws|throws
name|Exception
block|{
name|long
name|start
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
try|try
block|{
name|callExecutor
operator|.
name|run
argument_list|()
expr_stmt|;
name|fail
argument_list|(
literal|"should be timeout exception!"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|assertTrue
argument_list|(
name|Pattern
operator|.
name|matches
argument_list|(
literal|".*MyTable.*timed out"
argument_list|,
name|e
operator|.
name|toString
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|assertTrue
argument_list|(
operator|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|-
name|start
operator|)
operator|>
name|MAX_TIME
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|static
interface|interface
name|CallExecutor
block|{
name|void
name|run
parameter_list|()
throws|throws
name|Exception
function_decl|;
block|}
block|}
end_class

end_unit

