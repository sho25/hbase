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
name|Closeable
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
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|HRegionInfo
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
name|ipc
operator|.
name|RpcControllerFactory
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
name|util
operator|.
name|Bytes
import|;
end_import

begin_comment
comment|/**  * A RetryingCallable for Master RPC operations.  * Implement the #rpcCall method. It will be retried on error. See its javadoc and the javadoc of  * #call(int). See {@link HBaseAdmin} for examples of how this is used. To get at the  * rpcController that has been created and configured to make this rpc call, use getRpcController().  * We are trying to contain all protobuf references including references to rpcController so we  * don't pollute codebase with protobuf references; keep the protobuf references contained and only  * present in a few classes rather than all about the code base.  *<p>Like {@link RegionServerCallable} only in here, we can safely be PayloadCarryingRpcController  * all the time. This is not possible in the similar {@link RegionServerCallable} Callable because  * it has to deal with Coprocessor Endpoints.  * @param<V> return type  */
end_comment

begin_class
specifier|abstract
class|class
name|MasterCallable
parameter_list|<
name|V
parameter_list|>
implements|implements
name|RetryingCallable
argument_list|<
name|V
argument_list|>
implements|,
name|Closeable
block|{
specifier|protected
specifier|final
name|ClusterConnection
name|connection
decl_stmt|;
specifier|protected
name|MasterKeepAliveConnection
name|master
decl_stmt|;
specifier|private
specifier|final
name|HBaseRpcController
name|rpcController
decl_stmt|;
name|MasterCallable
parameter_list|(
specifier|final
name|Connection
name|connection
parameter_list|,
specifier|final
name|RpcControllerFactory
name|rpcConnectionFactory
parameter_list|)
block|{
name|this
operator|.
name|connection
operator|=
operator|(
name|ClusterConnection
operator|)
name|connection
expr_stmt|;
name|this
operator|.
name|rpcController
operator|=
name|rpcConnectionFactory
operator|.
name|newController
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|prepare
parameter_list|(
name|boolean
name|reload
parameter_list|)
throws|throws
name|IOException
block|{
name|this
operator|.
name|master
operator|=
name|this
operator|.
name|connection
operator|.
name|getKeepAliveMasterService
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|()
throws|throws
name|IOException
block|{
comment|// The above prepare could fail but this would still be called though masterAdmin is null
if|if
condition|(
name|this
operator|.
name|master
operator|!=
literal|null
condition|)
block|{
name|this
operator|.
name|master
operator|.
name|close
argument_list|()
expr_stmt|;
name|this
operator|.
name|master
operator|=
literal|null
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|throwable
parameter_list|(
name|Throwable
name|t
parameter_list|,
name|boolean
name|retrying
parameter_list|)
block|{   }
annotation|@
name|Override
specifier|public
name|String
name|getExceptionMessageAdditionalDetail
parameter_list|()
block|{
return|return
literal|""
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|sleep
parameter_list|(
name|long
name|pause
parameter_list|,
name|int
name|tries
parameter_list|)
block|{
return|return
name|ConnectionUtils
operator|.
name|getPauseTime
argument_list|(
name|pause
argument_list|,
name|tries
argument_list|)
return|;
block|}
comment|/**    * Override that changes the {@link java.util.concurrent.Callable#call()} Exception from {@link Exception} to    * {@link IOException}. It also does setup of an rpcController and calls through to the rpcCall()    * method which callers are expected to implement. If rpcController is an instance of    * PayloadCarryingRpcController, we will set a timeout on it.    */
annotation|@
name|Override
comment|// Same trick as in RegionServerCallable so users don't have to copy/paste so much boilerplate
comment|// and so we contain references to protobuf. We can't set priority on the rpcController as
comment|// we do in RegionServerCallable because we don't always have a Table when we call.
specifier|public
name|V
name|call
parameter_list|(
name|int
name|callTimeout
parameter_list|)
throws|throws
name|IOException
block|{
try|try
block|{
if|if
condition|(
name|this
operator|.
name|rpcController
operator|!=
literal|null
condition|)
block|{
name|this
operator|.
name|rpcController
operator|.
name|reset
argument_list|()
expr_stmt|;
name|this
operator|.
name|rpcController
operator|.
name|setCallTimeout
argument_list|(
name|callTimeout
argument_list|)
expr_stmt|;
block|}
return|return
name|rpcCall
argument_list|()
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
name|ProtobufUtil
operator|.
name|handleRemoteException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
comment|/**    * Run the RPC call. Implement this method. To get at the rpcController that has been created    * and configured to make this rpc call, use getRpcController(). We are trying to contain    * rpcController references so we don't pollute codebase with protobuf references; keep the    * protobuf references contained and only present in a few classes rather than all about the    * code base.    * @throws Exception    */
specifier|protected
specifier|abstract
name|V
name|rpcCall
parameter_list|()
throws|throws
name|Exception
function_decl|;
name|HBaseRpcController
name|getRpcController
parameter_list|()
block|{
return|return
name|this
operator|.
name|rpcController
return|;
block|}
name|void
name|setPriority
parameter_list|(
specifier|final
name|int
name|priority
parameter_list|)
block|{
if|if
condition|(
name|this
operator|.
name|rpcController
operator|!=
literal|null
condition|)
block|{
name|this
operator|.
name|rpcController
operator|.
name|setPriority
argument_list|(
name|priority
argument_list|)
expr_stmt|;
block|}
block|}
name|void
name|setPriority
parameter_list|(
specifier|final
name|TableName
name|tableName
parameter_list|)
block|{
if|if
condition|(
name|this
operator|.
name|rpcController
operator|!=
literal|null
condition|)
block|{
name|this
operator|.
name|rpcController
operator|.
name|setPriority
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * @param regionName RegionName. If hbase:meta, we'll set high priority.    */
name|void
name|setPriority
parameter_list|(
specifier|final
name|byte
index|[]
name|regionName
parameter_list|)
block|{
if|if
condition|(
name|isMetaRegion
argument_list|(
name|regionName
argument_list|)
condition|)
block|{
name|setPriority
argument_list|(
name|TableName
operator|.
name|META_TABLE_NAME
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
specifier|static
name|boolean
name|isMetaRegion
parameter_list|(
specifier|final
name|byte
index|[]
name|regionName
parameter_list|)
block|{
return|return
name|Bytes
operator|.
name|equals
argument_list|(
name|regionName
argument_list|,
name|HRegionInfo
operator|.
name|FIRST_META_REGIONINFO
operator|.
name|getRegionName
argument_list|()
argument_list|)
operator|||
name|Bytes
operator|.
name|equals
argument_list|(
name|regionName
argument_list|,
name|HRegionInfo
operator|.
name|FIRST_META_REGIONINFO
operator|.
name|getEncodedNameAsBytes
argument_list|()
argument_list|)
return|;
block|}
block|}
end_class

end_unit

