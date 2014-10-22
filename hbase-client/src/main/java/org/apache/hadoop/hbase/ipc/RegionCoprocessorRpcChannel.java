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
name|HConnection
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
name|RegionServerCallable
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
name|RpcRetryingCallerFactory
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
name|ByteStringer
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

begin_comment
comment|/**  * Provides clients with an RPC connection to call coprocessor endpoint {@link com.google.protobuf.Service}s  * against a given table region.  An instance of this class may be obtained  * by calling {@link org.apache.hadoop.hbase.client.HTable#coprocessorService(byte[])},  * but should normally only be used in creating a new {@link com.google.protobuf.Service} stub to call the endpoint  * methods.  * @see org.apache.hadoop.hbase.client.HTable#coprocessorService(byte[])  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|RegionCoprocessorRpcChannel
extends|extends
name|CoprocessorRpcChannel
block|{
specifier|private
specifier|static
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|RegionCoprocessorRpcChannel
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|HConnection
name|connection
decl_stmt|;
specifier|private
specifier|final
name|TableName
name|table
decl_stmt|;
specifier|private
specifier|final
name|byte
index|[]
name|row
decl_stmt|;
specifier|private
name|byte
index|[]
name|lastRegion
decl_stmt|;
specifier|private
name|int
name|operationTimeout
decl_stmt|;
specifier|private
name|RpcRetryingCallerFactory
name|rpcFactory
decl_stmt|;
specifier|public
name|RegionCoprocessorRpcChannel
parameter_list|(
name|HConnection
name|conn
parameter_list|,
name|TableName
name|table
parameter_list|,
name|byte
index|[]
name|row
parameter_list|)
block|{
name|this
operator|.
name|connection
operator|=
name|conn
expr_stmt|;
name|this
operator|.
name|table
operator|=
name|table
expr_stmt|;
name|this
operator|.
name|row
operator|=
name|row
expr_stmt|;
name|this
operator|.
name|rpcFactory
operator|=
name|RpcRetryingCallerFactory
operator|.
name|instantiate
argument_list|(
name|conn
operator|.
name|getConfiguration
argument_list|()
argument_list|)
expr_stmt|;
name|this
operator|.
name|operationTimeout
operator|=
name|conn
operator|.
name|getConfiguration
argument_list|()
operator|.
name|getInt
argument_list|(
name|HConstants
operator|.
name|HBASE_CLIENT_OPERATION_TIMEOUT
argument_list|,
name|HConstants
operator|.
name|DEFAULT_HBASE_CLIENT_OPERATION_TIMEOUT
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|Message
name|callExecService
parameter_list|(
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
if|if
condition|(
name|row
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Missing row property for remote region location"
argument_list|)
throw|;
block|}
specifier|final
name|ClientProtos
operator|.
name|CoprocessorServiceCall
name|call
init|=
name|ClientProtos
operator|.
name|CoprocessorServiceCall
operator|.
name|newBuilder
argument_list|()
operator|.
name|setRow
argument_list|(
name|ByteStringer
operator|.
name|wrap
argument_list|(
name|row
argument_list|)
argument_list|)
operator|.
name|setServiceName
argument_list|(
name|method
operator|.
name|getService
argument_list|()
operator|.
name|getFullName
argument_list|()
argument_list|)
operator|.
name|setMethodName
argument_list|(
name|method
operator|.
name|getName
argument_list|()
argument_list|)
operator|.
name|setRequest
argument_list|(
name|request
operator|.
name|toByteString
argument_list|()
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|RegionServerCallable
argument_list|<
name|CoprocessorServiceResponse
argument_list|>
name|callable
init|=
operator|new
name|RegionServerCallable
argument_list|<
name|CoprocessorServiceResponse
argument_list|>
argument_list|(
name|connection
argument_list|,
name|table
argument_list|,
name|row
argument_list|)
block|{
specifier|public
name|CoprocessorServiceResponse
name|call
parameter_list|(
name|int
name|callTimeout
parameter_list|)
throws|throws
name|Exception
block|{
name|byte
index|[]
name|regionName
init|=
name|getLocation
argument_list|()
operator|.
name|getRegionInfo
argument_list|()
operator|.
name|getRegionName
argument_list|()
decl_stmt|;
return|return
name|ProtobufUtil
operator|.
name|execService
argument_list|(
name|getStub
argument_list|()
argument_list|,
name|call
argument_list|,
name|regionName
argument_list|)
return|;
block|}
block|}
decl_stmt|;
name|CoprocessorServiceResponse
name|result
init|=
name|rpcFactory
operator|.
expr|<
name|CoprocessorServiceResponse
operator|>
name|newCaller
argument_list|()
operator|.
name|callWithRetries
argument_list|(
name|callable
argument_list|,
name|operationTimeout
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
name|response
operator|=
name|responsePrototype
operator|.
name|newBuilderForType
argument_list|()
operator|.
name|mergeFrom
argument_list|(
name|result
operator|.
name|getValue
argument_list|()
operator|.
name|getValue
argument_list|()
argument_list|)
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
name|lastRegion
operator|=
name|result
operator|.
name|getRegion
argument_list|()
operator|.
name|getValue
argument_list|()
operator|.
name|toByteArray
argument_list|()
expr_stmt|;
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
literal|"Result is region="
operator|+
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|lastRegion
argument_list|)
operator|+
literal|", value="
operator|+
name|response
argument_list|)
expr_stmt|;
block|}
return|return
name|response
return|;
block|}
specifier|public
name|byte
index|[]
name|getLastRegion
parameter_list|()
block|{
return|return
name|lastRegion
return|;
block|}
block|}
end_class

end_unit

