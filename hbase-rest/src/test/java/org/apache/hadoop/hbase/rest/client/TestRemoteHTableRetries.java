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
name|Arrays
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
name|client
operator|.
name|Delete
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
name|Get
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
name|Put
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
name|Scan
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

begin_comment
comment|/**  * Test RemoteHTable retries.  */
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
name|TestRemoteHTableRetries
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
name|TestRemoteHTableRetries
operator|.
name|class
argument_list|)
decl_stmt|;
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
specifier|static
specifier|final
name|byte
index|[]
name|ROW_1
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"testrow1"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|COLUMN_1
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
name|QUALIFIER_1
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"1"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|VALUE_1
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"testvalue1"
argument_list|)
decl_stmt|;
specifier|private
name|Client
name|client
decl_stmt|;
specifier|private
name|RemoteHTable
name|remoteTable
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
name|remoteTable
operator|=
operator|new
name|RemoteHTable
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
name|After
specifier|public
name|void
name|tearDownAfterClass
parameter_list|()
throws|throws
name|Exception
block|{
name|remoteTable
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testDelete
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
name|Delete
name|delete
init|=
operator|new
name|Delete
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"delete"
argument_list|)
argument_list|)
decl_stmt|;
name|remoteTable
operator|.
name|delete
argument_list|(
name|delete
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
name|testGet
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
name|remoteTable
operator|.
name|get
argument_list|(
operator|new
name|Get
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"Get"
argument_list|)
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
specifier|public
name|void
name|testSingleRowPut
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
name|remoteTable
operator|.
name|put
argument_list|(
operator|new
name|Put
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"Row"
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
name|testMultiRowPut
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
name|Put
index|[]
name|puts
init|=
block|{
operator|new
name|Put
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"Row1"
argument_list|)
argument_list|)
block|,
operator|new
name|Put
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"Row2"
argument_list|)
argument_list|)
block|}
decl_stmt|;
name|remoteTable
operator|.
name|put
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|puts
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
name|testGetScanner
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
name|remoteTable
operator|.
name|getScanner
argument_list|(
operator|new
name|Scan
argument_list|()
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
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testCheckAndPut
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
name|Put
name|put
init|=
operator|new
name|Put
argument_list|(
name|ROW_1
argument_list|)
decl_stmt|;
name|put
operator|.
name|addColumn
argument_list|(
name|COLUMN_1
argument_list|,
name|QUALIFIER_1
argument_list|,
name|VALUE_1
argument_list|)
expr_stmt|;
name|remoteTable
operator|.
name|checkAndMutate
argument_list|(
name|ROW_1
argument_list|,
name|COLUMN_1
argument_list|)
operator|.
name|qualifier
argument_list|(
name|QUALIFIER_1
argument_list|)
operator|.
name|ifEquals
argument_list|(
name|VALUE_1
argument_list|)
operator|.
name|thenPut
argument_list|(
name|put
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
name|testCheckAndDelete
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
name|Put
name|put
init|=
operator|new
name|Put
argument_list|(
name|ROW_1
argument_list|)
decl_stmt|;
name|put
operator|.
name|addColumn
argument_list|(
name|COLUMN_1
argument_list|,
name|QUALIFIER_1
argument_list|,
name|VALUE_1
argument_list|)
expr_stmt|;
name|Delete
name|delete
init|=
operator|new
name|Delete
argument_list|(
name|ROW_1
argument_list|)
decl_stmt|;
comment|//remoteTable.checkAndDelete(ROW_1, COLUMN_1, QUALIFIER_1,  VALUE_1, delete );
name|remoteTable
operator|.
name|checkAndMutate
argument_list|(
name|ROW_1
argument_list|,
name|COLUMN_1
argument_list|)
operator|.
name|qualifier
argument_list|(
name|QUALIFIER_1
argument_list|)
operator|.
name|ifEquals
argument_list|(
name|VALUE_1
argument_list|)
operator|.
name|thenDelete
argument_list|(
name|delete
argument_list|)
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
literal|".*request timed out"
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

