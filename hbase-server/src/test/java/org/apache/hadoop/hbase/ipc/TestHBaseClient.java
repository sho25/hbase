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
name|ipc
package|;
end_package

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|InetSocketAddress
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
name|testclassification
operator|.
name|RPCTests
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
name|Assert
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
name|RPCTests
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
name|TestHBaseClient
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
name|TestHBaseClient
operator|.
name|class
argument_list|)
decl_stmt|;
annotation|@
name|Test
specifier|public
name|void
name|testFailedServer
parameter_list|()
block|{
name|ManualEnvironmentEdge
name|ee
init|=
operator|new
name|ManualEnvironmentEdge
argument_list|()
decl_stmt|;
name|EnvironmentEdgeManager
operator|.
name|injectEdge
argument_list|(
name|ee
argument_list|)
expr_stmt|;
name|FailedServers
name|fs
init|=
operator|new
name|FailedServers
argument_list|(
operator|new
name|Configuration
argument_list|()
argument_list|)
decl_stmt|;
name|Throwable
name|testThrowable
init|=
operator|new
name|Throwable
argument_list|()
decl_stmt|;
comment|//throwable already tested in TestFailedServers.java
name|InetSocketAddress
name|ia
init|=
name|InetSocketAddress
operator|.
name|createUnresolved
argument_list|(
literal|"bad"
argument_list|,
literal|12
argument_list|)
decl_stmt|;
comment|// same server as ia
name|InetSocketAddress
name|ia2
init|=
name|InetSocketAddress
operator|.
name|createUnresolved
argument_list|(
literal|"bad"
argument_list|,
literal|12
argument_list|)
decl_stmt|;
name|InetSocketAddress
name|ia3
init|=
name|InetSocketAddress
operator|.
name|createUnresolved
argument_list|(
literal|"badtoo"
argument_list|,
literal|12
argument_list|)
decl_stmt|;
name|InetSocketAddress
name|ia4
init|=
name|InetSocketAddress
operator|.
name|createUnresolved
argument_list|(
literal|"badtoo"
argument_list|,
literal|13
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertFalse
argument_list|(
name|fs
operator|.
name|isFailedServer
argument_list|(
name|ia
argument_list|)
argument_list|)
expr_stmt|;
name|fs
operator|.
name|addToFailedServers
argument_list|(
name|ia
argument_list|,
name|testThrowable
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|fs
operator|.
name|isFailedServer
argument_list|(
name|ia
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|fs
operator|.
name|isFailedServer
argument_list|(
name|ia2
argument_list|)
argument_list|)
expr_stmt|;
name|ee
operator|.
name|incValue
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|fs
operator|.
name|isFailedServer
argument_list|(
name|ia
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|fs
operator|.
name|isFailedServer
argument_list|(
name|ia2
argument_list|)
argument_list|)
expr_stmt|;
name|ee
operator|.
name|incValue
argument_list|(
name|RpcClient
operator|.
name|FAILED_SERVER_EXPIRY_DEFAULT
operator|+
literal|1
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertFalse
argument_list|(
name|fs
operator|.
name|isFailedServer
argument_list|(
name|ia
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertFalse
argument_list|(
name|fs
operator|.
name|isFailedServer
argument_list|(
name|ia2
argument_list|)
argument_list|)
expr_stmt|;
name|fs
operator|.
name|addToFailedServers
argument_list|(
name|ia
argument_list|,
name|testThrowable
argument_list|)
expr_stmt|;
name|fs
operator|.
name|addToFailedServers
argument_list|(
name|ia3
argument_list|,
name|testThrowable
argument_list|)
expr_stmt|;
name|fs
operator|.
name|addToFailedServers
argument_list|(
name|ia4
argument_list|,
name|testThrowable
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|fs
operator|.
name|isFailedServer
argument_list|(
name|ia
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|fs
operator|.
name|isFailedServer
argument_list|(
name|ia2
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|fs
operator|.
name|isFailedServer
argument_list|(
name|ia3
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|fs
operator|.
name|isFailedServer
argument_list|(
name|ia4
argument_list|)
argument_list|)
expr_stmt|;
name|ee
operator|.
name|incValue
argument_list|(
name|RpcClient
operator|.
name|FAILED_SERVER_EXPIRY_DEFAULT
operator|+
literal|1
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertFalse
argument_list|(
name|fs
operator|.
name|isFailedServer
argument_list|(
name|ia
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertFalse
argument_list|(
name|fs
operator|.
name|isFailedServer
argument_list|(
name|ia2
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertFalse
argument_list|(
name|fs
operator|.
name|isFailedServer
argument_list|(
name|ia3
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertFalse
argument_list|(
name|fs
operator|.
name|isFailedServer
argument_list|(
name|ia4
argument_list|)
argument_list|)
expr_stmt|;
name|fs
operator|.
name|addToFailedServers
argument_list|(
name|ia3
argument_list|,
name|testThrowable
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertFalse
argument_list|(
name|fs
operator|.
name|isFailedServer
argument_list|(
name|ia4
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

