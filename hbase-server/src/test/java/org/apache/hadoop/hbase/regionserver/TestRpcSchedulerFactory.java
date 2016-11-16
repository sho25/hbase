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
name|assertTrue
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
name|CategoryBasedTimeout
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
name|HBaseConfiguration
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
name|ipc
operator|.
name|FifoRpcScheduler
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
name|ipc
operator|.
name|RWQueueRpcExecutor
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
name|ipc
operator|.
name|RpcExecutor
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
name|ipc
operator|.
name|RpcScheduler
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
name|ipc
operator|.
name|SimpleRpcScheduler
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
name|ClassRule
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Rule
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
name|junit
operator|.
name|rules
operator|.
name|TestName
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|rules
operator|.
name|TestRule
import|;
end_import

begin_comment
comment|/**  * A silly test that does nothing but make sure an rpcscheduler factory makes what it says  * it is going to make.  */
end_comment

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
name|TestRpcSchedulerFactory
block|{
annotation|@
name|Rule
specifier|public
name|TestName
name|testName
init|=
operator|new
name|TestName
argument_list|()
decl_stmt|;
annotation|@
name|ClassRule
specifier|public
specifier|static
name|TestRule
name|timeout
init|=
name|CategoryBasedTimeout
operator|.
name|forClass
argument_list|(
name|TestRpcSchedulerFactory
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|Configuration
name|conf
decl_stmt|;
annotation|@
name|Before
specifier|public
name|void
name|setUp
parameter_list|()
throws|throws
name|Exception
block|{
name|this
operator|.
name|conf
operator|=
name|HBaseConfiguration
operator|.
name|create
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testRWQ
parameter_list|()
block|{
comment|// Set some configs just to see how it changes the scheduler. Can't assert the settings had
comment|// an effect. Just eyeball the log.
name|this
operator|.
name|conf
operator|.
name|setDouble
argument_list|(
name|RWQueueRpcExecutor
operator|.
name|CALL_QUEUE_READ_SHARE_CONF_KEY
argument_list|,
literal|0.5
argument_list|)
expr_stmt|;
name|this
operator|.
name|conf
operator|.
name|setDouble
argument_list|(
name|RpcExecutor
operator|.
name|CALL_QUEUE_HANDLER_FACTOR_CONF_KEY
argument_list|,
literal|0.5
argument_list|)
expr_stmt|;
name|this
operator|.
name|conf
operator|.
name|setDouble
argument_list|(
name|RWQueueRpcExecutor
operator|.
name|CALL_QUEUE_SCAN_SHARE_CONF_KEY
argument_list|,
literal|0.5
argument_list|)
expr_stmt|;
name|RpcSchedulerFactory
name|factory
init|=
operator|new
name|SimpleRpcSchedulerFactory
argument_list|()
decl_stmt|;
name|RpcScheduler
name|rpcScheduler
init|=
name|factory
operator|.
name|create
argument_list|(
name|this
operator|.
name|conf
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|rpcScheduler
operator|.
name|getClass
argument_list|()
operator|.
name|equals
argument_list|(
name|SimpleRpcScheduler
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testFifo
parameter_list|()
block|{
name|RpcSchedulerFactory
name|factory
init|=
operator|new
name|FifoRpcSchedulerFactory
argument_list|()
decl_stmt|;
name|RpcScheduler
name|rpcScheduler
init|=
name|factory
operator|.
name|create
argument_list|(
name|this
operator|.
name|conf
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|rpcScheduler
operator|.
name|getClass
argument_list|()
operator|.
name|equals
argument_list|(
name|FifoRpcScheduler
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

