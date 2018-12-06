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
name|RpcController
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
name|AdminProtos
operator|.
name|AdminService
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
name|AdminProtos
operator|.
name|ClearCompactionQueuesRequest
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
name|AdminProtos
operator|.
name|ClearCompactionQueuesResponse
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
name|AdminProtos
operator|.
name|ClearRegionBlockCacheRequest
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
name|AdminProtos
operator|.
name|ClearRegionBlockCacheResponse
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
name|AdminProtos
operator|.
name|CloseRegionRequest
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
name|AdminProtos
operator|.
name|CloseRegionResponse
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
name|AdminProtos
operator|.
name|CompactRegionRequest
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
name|AdminProtos
operator|.
name|CompactRegionResponse
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
name|AdminProtos
operator|.
name|CompactionSwitchRequest
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
name|AdminProtos
operator|.
name|CompactionSwitchResponse
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
name|AdminProtos
operator|.
name|ExecuteProceduresRequest
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
name|AdminProtos
operator|.
name|ExecuteProceduresResponse
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
name|AdminProtos
operator|.
name|FlushRegionRequest
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
name|AdminProtos
operator|.
name|FlushRegionResponse
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
name|AdminProtos
operator|.
name|GetOnlineRegionRequest
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
name|AdminProtos
operator|.
name|GetOnlineRegionResponse
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
name|AdminProtos
operator|.
name|GetRegionInfoRequest
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
name|AdminProtos
operator|.
name|GetRegionInfoResponse
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
name|AdminProtos
operator|.
name|GetRegionLoadRequest
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
name|AdminProtos
operator|.
name|GetRegionLoadResponse
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
name|AdminProtos
operator|.
name|GetServerInfoRequest
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
name|AdminProtos
operator|.
name|GetServerInfoResponse
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
name|AdminProtos
operator|.
name|GetStoreFileRequest
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
name|AdminProtos
operator|.
name|GetStoreFileResponse
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
name|AdminProtos
operator|.
name|OpenRegionRequest
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
name|AdminProtos
operator|.
name|OpenRegionResponse
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
name|AdminProtos
operator|.
name|ReplicateWALEntryRequest
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
name|AdminProtos
operator|.
name|ReplicateWALEntryResponse
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
name|AdminProtos
operator|.
name|RollWALWriterRequest
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
name|AdminProtos
operator|.
name|RollWALWriterResponse
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
name|AdminProtos
operator|.
name|StopServerRequest
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
name|AdminProtos
operator|.
name|StopServerResponse
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
name|AdminProtos
operator|.
name|UpdateConfigurationRequest
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
name|AdminProtos
operator|.
name|UpdateConfigurationResponse
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
name|AdminProtos
operator|.
name|UpdateFavoredNodesRequest
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
name|AdminProtos
operator|.
name|UpdateFavoredNodesResponse
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
name|AdminProtos
operator|.
name|WarmupRegionRequest
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
name|AdminProtos
operator|.
name|WarmupRegionResponse
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
name|QuotaProtos
operator|.
name|GetSpaceQuotaSnapshotsRequest
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
name|QuotaProtos
operator|.
name|GetSpaceQuotaSnapshotsResponse
import|;
end_import

begin_comment
comment|/**  * A simple wrapper of the {@link AdminService} for a region server, which returns a  * {@link CompletableFuture}. This is easier to use, as if you use the raw protobuf interface, you  * need to get the result from the {@link RpcCallback}, and if there is an exception, you need to  * get it from the {@link RpcController} passed in.  *<p/>  * Notice that there is no retry, and this is intentional. We have different retry for different  * usage for now, if later we want to unify them, we can move the retry logic into this class.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|AsyncRegionServerAdmin
block|{
specifier|private
specifier|final
name|ServerName
name|server
decl_stmt|;
specifier|private
specifier|final
name|AsyncConnectionImpl
name|conn
decl_stmt|;
name|AsyncRegionServerAdmin
parameter_list|(
name|ServerName
name|server
parameter_list|,
name|AsyncConnectionImpl
name|conn
parameter_list|)
block|{
name|this
operator|.
name|server
operator|=
name|server
expr_stmt|;
name|this
operator|.
name|conn
operator|=
name|conn
expr_stmt|;
block|}
annotation|@
name|FunctionalInterface
specifier|private
interface|interface
name|RpcCall
parameter_list|<
name|RESP
parameter_list|>
block|{
name|void
name|call
parameter_list|(
name|AdminService
operator|.
name|Interface
name|stub
parameter_list|,
name|HBaseRpcController
name|controller
parameter_list|,
name|RpcCallback
argument_list|<
name|RESP
argument_list|>
name|done
parameter_list|)
function_decl|;
block|}
specifier|private
parameter_list|<
name|RESP
parameter_list|>
name|CompletableFuture
argument_list|<
name|RESP
argument_list|>
name|call
parameter_list|(
name|RpcCall
argument_list|<
name|RESP
argument_list|>
name|rpcCall
parameter_list|)
block|{
name|CompletableFuture
argument_list|<
name|RESP
argument_list|>
name|future
init|=
operator|new
name|CompletableFuture
argument_list|<>
argument_list|()
decl_stmt|;
name|HBaseRpcController
name|controller
init|=
name|conn
operator|.
name|rpcControllerFactory
operator|.
name|newController
argument_list|()
decl_stmt|;
try|try
block|{
name|rpcCall
operator|.
name|call
argument_list|(
name|conn
operator|.
name|getAdminStub
argument_list|(
name|server
argument_list|)
argument_list|,
name|controller
argument_list|,
operator|new
name|RpcCallback
argument_list|<
name|RESP
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|(
name|RESP
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
name|future
operator|.
name|complete
argument_list|(
name|resp
argument_list|)
expr_stmt|;
block|}
block|}
block|}
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
return|return
name|future
return|;
block|}
specifier|public
name|CompletableFuture
argument_list|<
name|GetRegionInfoResponse
argument_list|>
name|getRegionInfo
parameter_list|(
name|GetRegionInfoRequest
name|request
parameter_list|)
block|{
return|return
name|call
argument_list|(
parameter_list|(
name|stub
parameter_list|,
name|controller
parameter_list|,
name|done
parameter_list|)
lambda|->
name|stub
operator|.
name|getRegionInfo
argument_list|(
name|controller
argument_list|,
name|request
argument_list|,
name|done
argument_list|)
argument_list|)
return|;
block|}
specifier|public
name|CompletableFuture
argument_list|<
name|GetStoreFileResponse
argument_list|>
name|getStoreFile
parameter_list|(
name|GetStoreFileRequest
name|request
parameter_list|)
block|{
return|return
name|call
argument_list|(
parameter_list|(
name|stub
parameter_list|,
name|controller
parameter_list|,
name|done
parameter_list|)
lambda|->
name|stub
operator|.
name|getStoreFile
argument_list|(
name|controller
argument_list|,
name|request
argument_list|,
name|done
argument_list|)
argument_list|)
return|;
block|}
specifier|public
name|CompletableFuture
argument_list|<
name|GetOnlineRegionResponse
argument_list|>
name|getOnlineRegion
parameter_list|(
name|GetOnlineRegionRequest
name|request
parameter_list|)
block|{
return|return
name|call
argument_list|(
parameter_list|(
name|stub
parameter_list|,
name|controller
parameter_list|,
name|done
parameter_list|)
lambda|->
name|stub
operator|.
name|getOnlineRegion
argument_list|(
name|controller
argument_list|,
name|request
argument_list|,
name|done
argument_list|)
argument_list|)
return|;
block|}
specifier|public
name|CompletableFuture
argument_list|<
name|OpenRegionResponse
argument_list|>
name|openRegion
parameter_list|(
name|OpenRegionRequest
name|request
parameter_list|)
block|{
return|return
name|call
argument_list|(
parameter_list|(
name|stub
parameter_list|,
name|controller
parameter_list|,
name|done
parameter_list|)
lambda|->
name|stub
operator|.
name|openRegion
argument_list|(
name|controller
argument_list|,
name|request
argument_list|,
name|done
argument_list|)
argument_list|)
return|;
block|}
specifier|public
name|CompletableFuture
argument_list|<
name|WarmupRegionResponse
argument_list|>
name|warmupRegion
parameter_list|(
name|WarmupRegionRequest
name|request
parameter_list|)
block|{
return|return
name|call
argument_list|(
parameter_list|(
name|stub
parameter_list|,
name|controller
parameter_list|,
name|done
parameter_list|)
lambda|->
name|stub
operator|.
name|warmupRegion
argument_list|(
name|controller
argument_list|,
name|request
argument_list|,
name|done
argument_list|)
argument_list|)
return|;
block|}
specifier|public
name|CompletableFuture
argument_list|<
name|CloseRegionResponse
argument_list|>
name|closeRegion
parameter_list|(
name|CloseRegionRequest
name|request
parameter_list|)
block|{
return|return
name|call
argument_list|(
parameter_list|(
name|stub
parameter_list|,
name|controller
parameter_list|,
name|done
parameter_list|)
lambda|->
name|stub
operator|.
name|closeRegion
argument_list|(
name|controller
argument_list|,
name|request
argument_list|,
name|done
argument_list|)
argument_list|)
return|;
block|}
specifier|public
name|CompletableFuture
argument_list|<
name|FlushRegionResponse
argument_list|>
name|flushRegion
parameter_list|(
name|FlushRegionRequest
name|request
parameter_list|)
block|{
return|return
name|call
argument_list|(
parameter_list|(
name|stub
parameter_list|,
name|controller
parameter_list|,
name|done
parameter_list|)
lambda|->
name|stub
operator|.
name|flushRegion
argument_list|(
name|controller
argument_list|,
name|request
argument_list|,
name|done
argument_list|)
argument_list|)
return|;
block|}
specifier|public
name|CompletableFuture
argument_list|<
name|CompactionSwitchResponse
argument_list|>
name|compactionSwitch
parameter_list|(
name|CompactionSwitchRequest
name|request
parameter_list|)
block|{
return|return
name|call
argument_list|(
parameter_list|(
name|stub
parameter_list|,
name|controller
parameter_list|,
name|done
parameter_list|)
lambda|->
name|stub
operator|.
name|compactionSwitch
argument_list|(
name|controller
argument_list|,
name|request
argument_list|,
name|done
argument_list|)
argument_list|)
return|;
block|}
specifier|public
name|CompletableFuture
argument_list|<
name|CompactRegionResponse
argument_list|>
name|compactRegion
parameter_list|(
name|CompactRegionRequest
name|request
parameter_list|)
block|{
return|return
name|call
argument_list|(
parameter_list|(
name|stub
parameter_list|,
name|controller
parameter_list|,
name|done
parameter_list|)
lambda|->
name|stub
operator|.
name|compactRegion
argument_list|(
name|controller
argument_list|,
name|request
argument_list|,
name|done
argument_list|)
argument_list|)
return|;
block|}
specifier|public
name|CompletableFuture
argument_list|<
name|ReplicateWALEntryResponse
argument_list|>
name|replicateWALEntry
parameter_list|(
name|ReplicateWALEntryRequest
name|request
parameter_list|)
block|{
return|return
name|call
argument_list|(
parameter_list|(
name|stub
parameter_list|,
name|controller
parameter_list|,
name|done
parameter_list|)
lambda|->
name|stub
operator|.
name|replicateWALEntry
argument_list|(
name|controller
argument_list|,
name|request
argument_list|,
name|done
argument_list|)
argument_list|)
return|;
block|}
specifier|public
name|CompletableFuture
argument_list|<
name|ReplicateWALEntryResponse
argument_list|>
name|replay
parameter_list|(
name|ReplicateWALEntryRequest
name|request
parameter_list|)
block|{
return|return
name|call
argument_list|(
parameter_list|(
name|stub
parameter_list|,
name|controller
parameter_list|,
name|done
parameter_list|)
lambda|->
name|stub
operator|.
name|replay
argument_list|(
name|controller
argument_list|,
name|request
argument_list|,
name|done
argument_list|)
argument_list|)
return|;
block|}
specifier|public
name|CompletableFuture
argument_list|<
name|RollWALWriterResponse
argument_list|>
name|rollWALWriter
parameter_list|(
name|RollWALWriterRequest
name|request
parameter_list|)
block|{
return|return
name|call
argument_list|(
parameter_list|(
name|stub
parameter_list|,
name|controller
parameter_list|,
name|done
parameter_list|)
lambda|->
name|stub
operator|.
name|rollWALWriter
argument_list|(
name|controller
argument_list|,
name|request
argument_list|,
name|done
argument_list|)
argument_list|)
return|;
block|}
specifier|public
name|CompletableFuture
argument_list|<
name|GetServerInfoResponse
argument_list|>
name|getServerInfo
parameter_list|(
name|GetServerInfoRequest
name|request
parameter_list|)
block|{
return|return
name|call
argument_list|(
parameter_list|(
name|stub
parameter_list|,
name|controller
parameter_list|,
name|done
parameter_list|)
lambda|->
name|stub
operator|.
name|getServerInfo
argument_list|(
name|controller
argument_list|,
name|request
argument_list|,
name|done
argument_list|)
argument_list|)
return|;
block|}
specifier|public
name|CompletableFuture
argument_list|<
name|StopServerResponse
argument_list|>
name|stopServer
parameter_list|(
name|StopServerRequest
name|request
parameter_list|)
block|{
return|return
name|call
argument_list|(
parameter_list|(
name|stub
parameter_list|,
name|controller
parameter_list|,
name|done
parameter_list|)
lambda|->
name|stub
operator|.
name|stopServer
argument_list|(
name|controller
argument_list|,
name|request
argument_list|,
name|done
argument_list|)
argument_list|)
return|;
block|}
specifier|public
name|CompletableFuture
argument_list|<
name|UpdateFavoredNodesResponse
argument_list|>
name|updateFavoredNodes
parameter_list|(
name|UpdateFavoredNodesRequest
name|request
parameter_list|)
block|{
return|return
name|call
argument_list|(
parameter_list|(
name|stub
parameter_list|,
name|controller
parameter_list|,
name|done
parameter_list|)
lambda|->
name|stub
operator|.
name|updateFavoredNodes
argument_list|(
name|controller
argument_list|,
name|request
argument_list|,
name|done
argument_list|)
argument_list|)
return|;
block|}
specifier|public
name|CompletableFuture
argument_list|<
name|UpdateConfigurationResponse
argument_list|>
name|updateConfiguration
parameter_list|(
name|UpdateConfigurationRequest
name|request
parameter_list|)
block|{
return|return
name|call
argument_list|(
parameter_list|(
name|stub
parameter_list|,
name|controller
parameter_list|,
name|done
parameter_list|)
lambda|->
name|stub
operator|.
name|updateConfiguration
argument_list|(
name|controller
argument_list|,
name|request
argument_list|,
name|done
argument_list|)
argument_list|)
return|;
block|}
specifier|public
name|CompletableFuture
argument_list|<
name|GetRegionLoadResponse
argument_list|>
name|getRegionLoad
parameter_list|(
name|GetRegionLoadRequest
name|request
parameter_list|)
block|{
return|return
name|call
argument_list|(
parameter_list|(
name|stub
parameter_list|,
name|controller
parameter_list|,
name|done
parameter_list|)
lambda|->
name|stub
operator|.
name|getRegionLoad
argument_list|(
name|controller
argument_list|,
name|request
argument_list|,
name|done
argument_list|)
argument_list|)
return|;
block|}
specifier|public
name|CompletableFuture
argument_list|<
name|ClearCompactionQueuesResponse
argument_list|>
name|clearCompactionQueues
parameter_list|(
name|ClearCompactionQueuesRequest
name|request
parameter_list|)
block|{
return|return
name|call
argument_list|(
parameter_list|(
name|stub
parameter_list|,
name|controller
parameter_list|,
name|done
parameter_list|)
lambda|->
name|stub
operator|.
name|clearCompactionQueues
argument_list|(
name|controller
argument_list|,
name|request
argument_list|,
name|done
argument_list|)
argument_list|)
return|;
block|}
specifier|public
name|CompletableFuture
argument_list|<
name|ClearRegionBlockCacheResponse
argument_list|>
name|clearRegionBlockCache
parameter_list|(
name|ClearRegionBlockCacheRequest
name|request
parameter_list|)
block|{
return|return
name|call
argument_list|(
parameter_list|(
name|stub
parameter_list|,
name|controller
parameter_list|,
name|done
parameter_list|)
lambda|->
name|stub
operator|.
name|clearRegionBlockCache
argument_list|(
name|controller
argument_list|,
name|request
argument_list|,
name|done
argument_list|)
argument_list|)
return|;
block|}
specifier|public
name|CompletableFuture
argument_list|<
name|GetSpaceQuotaSnapshotsResponse
argument_list|>
name|getSpaceQuotaSnapshots
parameter_list|(
name|GetSpaceQuotaSnapshotsRequest
name|request
parameter_list|)
block|{
return|return
name|call
argument_list|(
parameter_list|(
name|stub
parameter_list|,
name|controller
parameter_list|,
name|done
parameter_list|)
lambda|->
name|stub
operator|.
name|getSpaceQuotaSnapshots
argument_list|(
name|controller
argument_list|,
name|request
argument_list|,
name|done
argument_list|)
argument_list|)
return|;
block|}
specifier|public
name|CompletableFuture
argument_list|<
name|ExecuteProceduresResponse
argument_list|>
name|executeProcedures
parameter_list|(
name|ExecuteProceduresRequest
name|request
parameter_list|)
block|{
return|return
name|call
argument_list|(
parameter_list|(
name|stub
parameter_list|,
name|controller
parameter_list|,
name|done
parameter_list|)
lambda|->
name|stub
operator|.
name|executeProcedures
argument_list|(
name|controller
argument_list|,
name|request
argument_list|,
name|done
argument_list|)
argument_list|)
return|;
block|}
block|}
end_class

end_unit

