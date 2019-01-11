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
name|net
operator|.
name|SocketAddress
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|List
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
name|RegionLocations
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
name|hadoop
operator|.
name|hbase
operator|.
name|ipc
operator|.
name|RpcClient
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
name|security
operator|.
name|User
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
name|wal
operator|.
name|WAL
operator|.
name|Entry
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
name|AdminProtos
operator|.
name|FlushRegionResponse
import|;
end_import

begin_comment
comment|/**  * The implementation of AsyncClusterConnection.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
class|class
name|AsyncClusterConnectionImpl
extends|extends
name|AsyncConnectionImpl
implements|implements
name|AsyncClusterConnection
block|{
specifier|public
name|AsyncClusterConnectionImpl
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|AsyncRegistry
name|registry
parameter_list|,
name|String
name|clusterId
parameter_list|,
name|SocketAddress
name|localAddress
parameter_list|,
name|User
name|user
parameter_list|)
block|{
name|super
argument_list|(
name|conf
argument_list|,
name|registry
argument_list|,
name|clusterId
argument_list|,
name|localAddress
argument_list|,
name|user
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|NonceGenerator
name|getNonceGenerator
parameter_list|()
block|{
return|return
name|super
operator|.
name|getNonceGenerator
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|RpcClient
name|getRpcClient
parameter_list|()
block|{
return|return
name|rpcClient
return|;
block|}
annotation|@
name|Override
specifier|public
name|AsyncRegionServerAdmin
name|getRegionServerAdmin
parameter_list|(
name|ServerName
name|serverName
parameter_list|)
block|{
return|return
operator|new
name|AsyncRegionServerAdmin
argument_list|(
name|serverName
argument_list|,
name|this
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|CompletableFuture
argument_list|<
name|FlushRegionResponse
argument_list|>
name|flush
parameter_list|(
name|byte
index|[]
name|regionName
parameter_list|,
name|boolean
name|writeFlushWALMarker
parameter_list|)
block|{
name|RawAsyncHBaseAdmin
name|admin
init|=
operator|(
name|RawAsyncHBaseAdmin
operator|)
name|getAdmin
argument_list|()
decl_stmt|;
return|return
name|admin
operator|.
name|flushRegionInternal
argument_list|(
name|regionName
argument_list|,
name|writeFlushWALMarker
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|CompletableFuture
argument_list|<
name|Long
argument_list|>
name|replay
parameter_list|(
name|TableName
name|tableName
parameter_list|,
name|byte
index|[]
name|encodedRegionName
parameter_list|,
name|byte
index|[]
name|row
parameter_list|,
name|List
argument_list|<
name|Entry
argument_list|>
name|entries
parameter_list|,
name|int
name|replicaId
parameter_list|,
name|int
name|retries
parameter_list|,
name|long
name|operationTimeoutNs
parameter_list|)
block|{
return|return
operator|new
name|AsyncRegionReplicaReplayRetryingCaller
argument_list|(
name|RETRY_TIMER
argument_list|,
name|this
argument_list|,
name|ConnectionUtils
operator|.
name|retries2Attempts
argument_list|(
name|retries
argument_list|)
argument_list|,
name|operationTimeoutNs
argument_list|,
name|tableName
argument_list|,
name|encodedRegionName
argument_list|,
name|row
argument_list|,
name|entries
argument_list|,
name|replicaId
argument_list|)
operator|.
name|call
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|CompletableFuture
argument_list|<
name|RegionLocations
argument_list|>
name|getRegionLocations
parameter_list|(
name|TableName
name|tableName
parameter_list|,
name|byte
index|[]
name|row
parameter_list|,
name|boolean
name|reload
parameter_list|)
block|{
return|return
name|getLocator
argument_list|()
operator|.
name|getRegionLocations
argument_list|(
name|tableName
argument_list|,
name|row
argument_list|,
name|RegionLocateType
operator|.
name|CURRENT
argument_list|,
name|reload
argument_list|,
operator|-
literal|1L
argument_list|)
return|;
block|}
block|}
end_class

end_unit

