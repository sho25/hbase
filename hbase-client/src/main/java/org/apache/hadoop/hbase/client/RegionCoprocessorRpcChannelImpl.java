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
import|import static
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
name|FutureUtils
operator|.
name|addListener
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
operator|.
name|MethodDescriptor
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
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|TimeUnit
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
name|DoNotRetryIOException
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
name|HRegionLocation
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
name|ipc
operator|.
name|CoprocessorRpcUtils
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
name|CoprocessorServiceRequest
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
name|CoprocessorServiceResponse
import|;
end_import

begin_comment
comment|/**  * The implementation of a region based coprocessor rpc channel.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
class|class
name|RegionCoprocessorRpcChannelImpl
implements|implements
name|RpcChannel
block|{
specifier|private
specifier|final
name|AsyncConnectionImpl
name|conn
decl_stmt|;
specifier|private
specifier|final
name|TableName
name|tableName
decl_stmt|;
specifier|private
specifier|final
name|RegionInfo
name|region
decl_stmt|;
specifier|private
specifier|final
name|byte
index|[]
name|row
decl_stmt|;
specifier|private
specifier|final
name|long
name|rpcTimeoutNs
decl_stmt|;
specifier|private
specifier|final
name|long
name|operationTimeoutNs
decl_stmt|;
name|RegionCoprocessorRpcChannelImpl
parameter_list|(
name|AsyncConnectionImpl
name|conn
parameter_list|,
name|TableName
name|tableName
parameter_list|,
name|RegionInfo
name|region
parameter_list|,
name|byte
index|[]
name|row
parameter_list|,
name|long
name|rpcTimeoutNs
parameter_list|,
name|long
name|operationTimeoutNs
parameter_list|)
block|{
name|this
operator|.
name|conn
operator|=
name|conn
expr_stmt|;
name|this
operator|.
name|tableName
operator|=
name|tableName
expr_stmt|;
name|this
operator|.
name|region
operator|=
name|region
expr_stmt|;
name|this
operator|.
name|row
operator|=
name|row
expr_stmt|;
name|this
operator|.
name|rpcTimeoutNs
operator|=
name|rpcTimeoutNs
expr_stmt|;
name|this
operator|.
name|operationTimeoutNs
operator|=
name|operationTimeoutNs
expr_stmt|;
block|}
specifier|private
name|CompletableFuture
argument_list|<
name|Message
argument_list|>
name|rpcCall
parameter_list|(
name|MethodDescriptor
name|method
parameter_list|,
name|Message
name|request
parameter_list|,
name|Message
name|responsePrototype
parameter_list|,
name|HBaseRpcController
name|controller
parameter_list|,
name|HRegionLocation
name|loc
parameter_list|,
name|ClientService
operator|.
name|Interface
name|stub
parameter_list|)
block|{
name|CompletableFuture
argument_list|<
name|Message
argument_list|>
name|future
init|=
operator|new
name|CompletableFuture
argument_list|<>
argument_list|()
decl_stmt|;
if|if
condition|(
name|region
operator|!=
literal|null
operator|&&
operator|!
name|Bytes
operator|.
name|equals
argument_list|(
name|loc
operator|.
name|getRegionInfo
argument_list|()
operator|.
name|getRegionName
argument_list|()
argument_list|,
name|region
operator|.
name|getRegionName
argument_list|()
argument_list|)
condition|)
block|{
name|future
operator|.
name|completeExceptionally
argument_list|(
operator|new
name|DoNotRetryIOException
argument_list|(
literal|"Region name is changed, expected "
operator|+
name|region
operator|.
name|getRegionNameAsString
argument_list|()
operator|+
literal|", actual "
operator|+
name|loc
operator|.
name|getRegionInfo
argument_list|()
operator|.
name|getRegionNameAsString
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|future
return|;
block|}
name|CoprocessorServiceRequest
name|csr
init|=
name|CoprocessorRpcUtils
operator|.
name|getCoprocessorServiceRequest
argument_list|(
name|method
argument_list|,
name|request
argument_list|,
name|row
argument_list|,
name|loc
operator|.
name|getRegionInfo
argument_list|()
operator|.
name|getRegionName
argument_list|()
argument_list|)
decl_stmt|;
name|stub
operator|.
name|execService
argument_list|(
name|controller
argument_list|,
name|csr
argument_list|,
operator|new
name|org
operator|.
name|apache
operator|.
name|hbase
operator|.
name|thirdparty
operator|.
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|RpcCallback
argument_list|<
name|CoprocessorServiceResponse
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|(
name|CoprocessorServiceResponse
name|resp
parameter_list|)
block|{
if|if
condition|(
name|controller
operator|.
name|failed
argument_list|()
condition|)
block|{
name|future
operator|.
name|completeExceptionally
argument_list|(
name|controller
operator|.
name|getFailed
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
try|try
block|{
name|future
operator|.
name|complete
argument_list|(
name|CoprocessorRpcUtils
operator|.
name|getResponse
argument_list|(
name|resp
argument_list|,
name|responsePrototype
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|future
operator|.
name|completeExceptionally
argument_list|(
name|e
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
argument_list|)
expr_stmt|;
return|return
name|future
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|callMethod
parameter_list|(
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
name|done
parameter_list|)
block|{
name|addListener
argument_list|(
name|conn
operator|.
name|callerFactory
operator|.
expr|<
name|Message
operator|>
name|single
argument_list|()
operator|.
name|table
argument_list|(
name|tableName
argument_list|)
operator|.
name|row
argument_list|(
name|row
argument_list|)
operator|.
name|locateType
argument_list|(
name|RegionLocateType
operator|.
name|CURRENT
argument_list|)
operator|.
name|rpcTimeout
argument_list|(
name|rpcTimeoutNs
argument_list|,
name|TimeUnit
operator|.
name|NANOSECONDS
argument_list|)
operator|.
name|operationTimeout
argument_list|(
name|operationTimeoutNs
argument_list|,
name|TimeUnit
operator|.
name|NANOSECONDS
argument_list|)
operator|.
name|action
argument_list|(
parameter_list|(
name|c
parameter_list|,
name|l
parameter_list|,
name|s
parameter_list|)
lambda|->
name|rpcCall
argument_list|(
name|method
argument_list|,
name|request
argument_list|,
name|responsePrototype
argument_list|,
name|c
argument_list|,
name|l
argument_list|,
name|s
argument_list|)
argument_list|)
operator|.
name|call
argument_list|()
argument_list|,
parameter_list|(
name|r
parameter_list|,
name|e
parameter_list|)
lambda|->
block|{
if|if
condition|(
name|e
operator|!=
literal|null
condition|)
block|{
operator|(
operator|(
name|ClientCoprocessorRpcController
operator|)
name|controller
operator|)
operator|.
name|setFailed
argument_list|(
name|e
argument_list|)
expr_stmt|;
block|}
lambda|done.run(r
argument_list|)
expr_stmt|;
block|}
block|)
class|;
end_class

unit|} }
end_unit

