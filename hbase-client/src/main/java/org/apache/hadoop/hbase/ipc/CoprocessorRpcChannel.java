begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|io
operator|.
name|IOException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|Log
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|LogFactory
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
name|classification
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
name|hadoop
operator|.
name|hbase
operator|.
name|classification
operator|.
name|InterfaceStability
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
name|protobuf
operator|.
name|ResponseConverter
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|BlockingRpcChannel
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|Descriptors
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|Message
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|RpcCallback
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|RpcChannel
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|RpcController
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|ServiceException
import|;
end_import

begin_comment
comment|/**  * Base class which provides clients with an RPC connection to  * call coprocessor endpoint {@link com.google.protobuf.Service}s.  * Note that clients should not use this class directly, except through  * {@link org.apache.hadoop.hbase.client.HTableInterface#coprocessorService(byte[])}.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Public
annotation|@
name|InterfaceStability
operator|.
name|Evolving
specifier|public
specifier|abstract
class|class
name|CoprocessorRpcChannel
implements|implements
name|RpcChannel
implements|,
name|BlockingRpcChannel
block|{
specifier|private
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|CoprocessorRpcChannel
operator|.
name|class
argument_list|)
decl_stmt|;
annotation|@
name|Override
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
name|void
name|callMethod
parameter_list|(
name|Descriptors
operator|.
name|MethodDescriptor
name|method
parameter_list|,
name|RpcController
name|controller
parameter_list|,
name|Message
name|request
parameter_list|,
name|Message
name|responsePrototype
parameter_list|,
name|RpcCallback
argument_list|<
name|Message
argument_list|>
name|callback
parameter_list|)
block|{
name|Message
name|response
init|=
literal|null
decl_stmt|;
try|try
block|{
name|response
operator|=
name|callExecService
argument_list|(
name|controller
argument_list|,
name|method
argument_list|,
name|request
argument_list|,
name|responsePrototype
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|ioe
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Call failed on IOException"
argument_list|,
name|ioe
argument_list|)
expr_stmt|;
name|ResponseConverter
operator|.
name|setControllerException
argument_list|(
name|controller
argument_list|,
name|ioe
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|callback
operator|!=
literal|null
condition|)
block|{
name|callback
operator|.
name|run
argument_list|(
name|response
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
name|Message
name|callBlockingMethod
parameter_list|(
name|Descriptors
operator|.
name|MethodDescriptor
name|method
parameter_list|,
name|RpcController
name|controller
parameter_list|,
name|Message
name|request
parameter_list|,
name|Message
name|responsePrototype
parameter_list|)
throws|throws
name|ServiceException
block|{
try|try
block|{
return|return
name|callExecService
argument_list|(
name|controller
argument_list|,
name|method
argument_list|,
name|request
argument_list|,
name|responsePrototype
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|IOException
name|ioe
parameter_list|)
block|{
throw|throw
operator|new
name|ServiceException
argument_list|(
literal|"Error calling method "
operator|+
name|method
operator|.
name|getFullName
argument_list|()
argument_list|,
name|ioe
argument_list|)
throw|;
block|}
block|}
specifier|protected
specifier|abstract
name|Message
name|callExecService
parameter_list|(
name|RpcController
name|controller
parameter_list|,
name|Descriptors
operator|.
name|MethodDescriptor
name|method
parameter_list|,
name|Message
name|request
parameter_list|,
name|Message
name|responsePrototype
parameter_list|)
throws|throws
name|IOException
function_decl|;
block|}
end_class

end_unit

