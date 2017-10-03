begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one or more  * contributor license agreements.  See the NOTICE file distributed with  * this work for additional information regarding copyright ownership.  * The ASF licenses this file to you under the Apache License, Version 2.0  * (the "License"); you may not use this file except in compliance with  * the License.  You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|java
operator|.
name|io
operator|.
name|IOException
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
name|anyInt
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
name|doCallRealMethod
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
name|TestHTableMultiplexerViaMocks
block|{
specifier|private
name|HTableMultiplexer
name|mockMultiplexer
decl_stmt|;
specifier|private
name|ClusterConnection
name|mockConnection
decl_stmt|;
annotation|@
name|Before
specifier|public
name|void
name|setupTest
parameter_list|()
block|{
name|mockMultiplexer
operator|=
name|mock
argument_list|(
name|HTableMultiplexer
operator|.
name|class
argument_list|)
expr_stmt|;
name|mockConnection
operator|=
name|mock
argument_list|(
name|ClusterConnection
operator|.
name|class
argument_list|)
expr_stmt|;
comment|// Call the real put(TableName, Put, int) method
name|when
argument_list|(
name|mockMultiplexer
operator|.
name|put
argument_list|(
name|any
argument_list|(
name|TableName
operator|.
name|class
argument_list|)
argument_list|,
name|any
argument_list|()
argument_list|,
name|anyInt
argument_list|()
argument_list|)
argument_list|)
operator|.
name|thenCallRealMethod
argument_list|()
expr_stmt|;
comment|// Return the mocked ClusterConnection
name|when
argument_list|(
name|mockMultiplexer
operator|.
name|getConnection
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|mockConnection
argument_list|)
expr_stmt|;
block|}
annotation|@
name|SuppressWarnings
argument_list|(
literal|"deprecation"
argument_list|)
annotation|@
name|Test
specifier|public
name|void
name|testConnectionClosing
parameter_list|()
throws|throws
name|IOException
block|{
name|doCallRealMethod
argument_list|()
operator|.
name|when
argument_list|(
name|mockMultiplexer
argument_list|)
operator|.
name|close
argument_list|()
expr_stmt|;
comment|// If the connection is not closed
name|when
argument_list|(
name|mockConnection
operator|.
name|isClosed
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|mockMultiplexer
operator|.
name|close
argument_list|()
expr_stmt|;
comment|// We should close it
name|verify
argument_list|(
name|mockConnection
argument_list|)
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
annotation|@
name|SuppressWarnings
argument_list|(
literal|"deprecation"
argument_list|)
annotation|@
name|Test
specifier|public
name|void
name|testClosingAlreadyClosedConnection
parameter_list|()
throws|throws
name|IOException
block|{
name|doCallRealMethod
argument_list|()
operator|.
name|when
argument_list|(
name|mockMultiplexer
argument_list|)
operator|.
name|close
argument_list|()
expr_stmt|;
comment|// If the connection is already closed
name|when
argument_list|(
name|mockConnection
operator|.
name|isClosed
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|mockMultiplexer
operator|.
name|close
argument_list|()
expr_stmt|;
comment|// We should not close it again
name|verify
argument_list|(
name|mockConnection
argument_list|,
name|times
argument_list|(
literal|0
argument_list|)
argument_list|)
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

