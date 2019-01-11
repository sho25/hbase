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
name|concurrent
operator|.
name|CompletableFuture
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
name|ServerName
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
name|HBaseRpcController
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|yetus
operator|.
name|audience
operator|.
name|InterfaceAudience
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hbase
operator|.
name|thirdparty
operator|.
name|io
operator|.
name|netty
operator|.
name|util
operator|.
name|Timer
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
name|shaded
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|ClientProtos
operator|.
name|ClientService
import|;
end_import

begin_comment
comment|/**  * Retry caller for a request call to region server.  * Now only used for coprocessor call to region server.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|AsyncServerRequestRpcRetryingCaller
parameter_list|<
name|T
parameter_list|>
extends|extends
name|AsyncRpcRetryingCaller
argument_list|<
name|T
argument_list|>
block|{
annotation|@
name|FunctionalInterface
specifier|public
interface|interface
name|Callable
parameter_list|<
name|T
parameter_list|>
block|{
name|CompletableFuture
argument_list|<
name|T
argument_list|>
name|call
parameter_list|(
name|HBaseRpcController
name|controller
parameter_list|,
name|ClientService
operator|.
name|Interface
name|stub
parameter_list|)
function_decl|;
block|}
specifier|private
specifier|final
name|Callable
argument_list|<
name|T
argument_list|>
name|callable
decl_stmt|;
specifier|private
name|ServerName
name|serverName
decl_stmt|;
specifier|public
name|AsyncServerRequestRpcRetryingCaller
parameter_list|(
name|Timer
name|retryTimer
parameter_list|,
name|AsyncConnectionImpl
name|conn
parameter_list|,
name|long
name|pauseNs
parameter_list|,
name|int
name|maxAttempts
parameter_list|,
name|long
name|operationTimeoutNs
parameter_list|,
name|long
name|rpcTimeoutNs
parameter_list|,
name|int
name|startLogErrorsCnt
parameter_list|,
name|ServerName
name|serverName
parameter_list|,
name|Callable
argument_list|<
name|T
argument_list|>
name|callable
parameter_list|)
block|{
name|super
argument_list|(
name|retryTimer
argument_list|,
name|conn
argument_list|,
name|pauseNs
argument_list|,
name|maxAttempts
argument_list|,
name|operationTimeoutNs
argument_list|,
name|rpcTimeoutNs
argument_list|,
name|startLogErrorsCnt
argument_list|)
expr_stmt|;
name|this
operator|.
name|serverName
operator|=
name|serverName
expr_stmt|;
name|this
operator|.
name|callable
operator|=
name|callable
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|doCall
parameter_list|()
block|{
name|ClientService
operator|.
name|Interface
name|stub
decl_stmt|;
try|try
block|{
name|stub
operator|=
name|this
operator|.
name|conn
operator|.
name|getRegionServerStub
argument_list|(
name|serverName
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|onError
argument_list|(
name|e
argument_list|,
parameter_list|()
lambda|->
literal|"Get async admin stub to "
operator|+
name|serverName
operator|+
literal|" failed"
argument_list|,
name|err
lambda|->
block|{       }
argument_list|)
expr_stmt|;
return|return;
block|}
name|resetCallTimeout
argument_list|()
expr_stmt|;
name|callable
operator|.
name|call
argument_list|(
name|controller
argument_list|,
name|stub
argument_list|)
operator|.
name|whenComplete
argument_list|(
parameter_list|(
name|result
parameter_list|,
name|error
parameter_list|)
lambda|->
block|{
if|if
condition|(
name|error
operator|!=
literal|null
condition|)
block|{
name|onError
argument_list|(
name|error
argument_list|,
parameter_list|()
lambda|->
literal|"Call to admin stub failed"
argument_list|,
name|err
lambda|->
block|{         }
argument_list|)
expr_stmt|;
return|return;
block|}
name|future
operator|.
name|complete
argument_list|(
name|result
argument_list|)
expr_stmt|;
block|}
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

