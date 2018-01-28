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
name|ipc
package|;
end_package

begin_import
import|import static
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|ipc
operator|.
name|IPCUtil
operator|.
name|wrapException
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
name|net
operator|.
name|ConnectException
import|;
end_import

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
name|java
operator|.
name|net
operator|.
name|SocketTimeoutException
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
name|exceptions
operator|.
name|ConnectionClosingException
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
name|SmallTests
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|TestIPCUtil
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
name|TestIPCUtil
operator|.
name|class
argument_list|)
decl_stmt|;
annotation|@
name|Test
specifier|public
name|void
name|testWrapException
parameter_list|()
throws|throws
name|Exception
block|{
specifier|final
name|InetSocketAddress
name|address
init|=
name|InetSocketAddress
operator|.
name|createUnresolved
argument_list|(
literal|"localhost"
argument_list|,
literal|0
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|wrapException
argument_list|(
name|address
argument_list|,
operator|new
name|ConnectException
argument_list|()
argument_list|)
operator|instanceof
name|ConnectException
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|wrapException
argument_list|(
name|address
argument_list|,
operator|new
name|SocketTimeoutException
argument_list|()
argument_list|)
operator|instanceof
name|SocketTimeoutException
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|wrapException
argument_list|(
name|address
argument_list|,
operator|new
name|ConnectionClosingException
argument_list|(
literal|"Test AbstractRpcClient#wrapException"
argument_list|)
argument_list|)
operator|instanceof
name|ConnectionClosingException
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|wrapException
argument_list|(
name|address
argument_list|,
operator|new
name|CallTimeoutException
argument_list|(
literal|"Test AbstractRpcClient#wrapException"
argument_list|)
argument_list|)
operator|.
name|getCause
argument_list|()
operator|instanceof
name|CallTimeoutException
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

