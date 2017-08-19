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
name|TableName
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
comment|/**  * A RegionServerCallable set to use the Client protocol.  * Also includes some utility methods so can hide protobuf references here rather than have them  * sprinkled about the code base.  * @param<T>  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
specifier|abstract
class|class
name|ClientServiceCallable
parameter_list|<
name|T
parameter_list|>
extends|extends
name|RegionServerCallable
argument_list|<
name|T
argument_list|,
name|ClientProtos
operator|.
name|ClientService
operator|.
name|BlockingInterface
argument_list|>
block|{
specifier|public
name|ClientServiceCallable
parameter_list|(
name|Connection
name|connection
parameter_list|,
name|TableName
name|tableName
parameter_list|,
name|byte
index|[]
name|row
parameter_list|,
name|RpcController
name|rpcController
parameter_list|,
name|int
name|priority
parameter_list|)
block|{
name|super
argument_list|(
name|connection
argument_list|,
name|tableName
argument_list|,
name|row
argument_list|,
name|rpcController
argument_list|,
name|priority
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|setStubByServiceName
parameter_list|(
name|ServerName
name|serviceName
parameter_list|)
throws|throws
name|IOException
block|{
name|setStub
argument_list|(
name|getConnection
argument_list|()
operator|.
name|getClient
argument_list|(
name|serviceName
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// Below here are simple methods that contain the stub and the rpcController.
specifier|protected
name|ClientProtos
operator|.
name|GetResponse
name|doGet
parameter_list|(
name|ClientProtos
operator|.
name|GetRequest
name|request
parameter_list|)
throws|throws
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
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|ServiceException
block|{
return|return
name|getStub
argument_list|()
operator|.
name|get
argument_list|(
name|getRpcController
argument_list|()
argument_list|,
name|request
argument_list|)
return|;
block|}
specifier|protected
name|ClientProtos
operator|.
name|MutateResponse
name|doMutate
parameter_list|(
name|ClientProtos
operator|.
name|MutateRequest
name|request
parameter_list|)
throws|throws
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
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|ServiceException
block|{
return|return
name|getStub
argument_list|()
operator|.
name|mutate
argument_list|(
name|getRpcController
argument_list|()
argument_list|,
name|request
argument_list|)
return|;
block|}
block|}
end_class

end_unit

