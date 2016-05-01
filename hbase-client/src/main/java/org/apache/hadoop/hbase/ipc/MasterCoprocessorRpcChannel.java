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
name|HConstants
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
name|client
operator|.
name|ClusterConnection
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
name|ProtobufUtil
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
name|generated
operator|.
name|ClientProtos
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
name|generated
operator|.
name|ClientProtos
operator|.
name|CoprocessorServiceResponse
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
name|RpcController
import|;
end_import

begin_comment
comment|/**  * Provides clients with an RPC connection to call coprocessor endpoint {@link com.google.protobuf.Service}s  * against the active master.  An instance of this class may be obtained  * by calling {@link org.apache.hadoop.hbase.client.HBaseAdmin#coprocessorService()},  * but should normally only be used in creating a new {@link com.google.protobuf.Service} stub to call the endpoint  * methods.  * @see org.apache.hadoop.hbase.client.HBaseAdmin#coprocessorService()  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|MasterCoprocessorRpcChannel
extends|extends
name|SyncCoprocessorRpcChannel
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
name|MasterCoprocessorRpcChannel
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|ClusterConnection
name|connection
decl_stmt|;
specifier|public
name|MasterCoprocessorRpcChannel
parameter_list|(
name|ClusterConnection
name|conn
parameter_list|)
block|{
name|this
operator|.
name|connection
operator|=
name|conn
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
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
block|{
if|if
condition|(
name|LOG
operator|.
name|isTraceEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|trace
argument_list|(
literal|"Call: "
operator|+
name|method
operator|.
name|getName
argument_list|()
operator|+
literal|", "
operator|+
name|request
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|final
name|ClientProtos
operator|.
name|CoprocessorServiceCall
name|call
init|=
name|CoprocessorRpcUtils
operator|.
name|buildServiceCall
argument_list|(
name|HConstants
operator|.
name|EMPTY_BYTE_ARRAY
argument_list|,
name|method
argument_list|,
name|request
argument_list|)
decl_stmt|;
comment|// TODO: Are we retrying here? Does not seem so. We should use RetryingRpcCaller
name|CoprocessorServiceResponse
name|result
init|=
name|ProtobufUtil
operator|.
name|execService
argument_list|(
name|controller
argument_list|,
name|connection
operator|.
name|getMaster
argument_list|()
argument_list|,
name|call
argument_list|)
decl_stmt|;
name|Message
name|response
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|result
operator|.
name|getValue
argument_list|()
operator|.
name|hasValue
argument_list|()
condition|)
block|{
name|Message
operator|.
name|Builder
name|builder
init|=
name|responsePrototype
operator|.
name|newBuilderForType
argument_list|()
decl_stmt|;
name|ProtobufUtil
operator|.
name|mergeFrom
argument_list|(
name|builder
argument_list|,
name|result
operator|.
name|getValue
argument_list|()
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
name|response
operator|=
name|builder
operator|.
name|build
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|response
operator|=
name|responsePrototype
operator|.
name|getDefaultInstanceForType
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|LOG
operator|.
name|isTraceEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|trace
argument_list|(
literal|"Master Result is value="
operator|+
name|response
argument_list|)
expr_stmt|;
block|}
return|return
name|response
return|;
block|}
block|}
end_class

end_unit

