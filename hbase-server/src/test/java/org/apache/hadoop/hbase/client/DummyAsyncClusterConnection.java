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
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|ExecutorService
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
name|util
operator|.
name|Pair
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
name|hadoop
operator|.
name|security
operator|.
name|token
operator|.
name|Token
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
comment|/**  * Can be overridden in UT if you only want to implement part of the methods in  * {@link AsyncClusterConnection}.  */
end_comment

begin_class
specifier|public
class|class
name|DummyAsyncClusterConnection
implements|implements
name|AsyncClusterConnection
block|{
annotation|@
name|Override
specifier|public
name|Configuration
name|getConfiguration
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|AsyncTableRegionLocator
name|getRegionLocator
parameter_list|(
name|TableName
name|tableName
parameter_list|)
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|clearRegionLocationCache
parameter_list|()
block|{   }
annotation|@
name|Override
specifier|public
name|AsyncTableBuilder
argument_list|<
name|AdvancedScanResultConsumer
argument_list|>
name|getTableBuilder
parameter_list|(
name|TableName
name|tableName
parameter_list|)
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|AsyncTableBuilder
argument_list|<
name|ScanResultConsumer
argument_list|>
name|getTableBuilder
parameter_list|(
name|TableName
name|tableName
parameter_list|,
name|ExecutorService
name|pool
parameter_list|)
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|AsyncAdminBuilder
name|getAdminBuilder
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|AsyncAdminBuilder
name|getAdminBuilder
parameter_list|(
name|ExecutorService
name|pool
parameter_list|)
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|AsyncBufferedMutatorBuilder
name|getBufferedMutatorBuilder
parameter_list|(
name|TableName
name|tableName
parameter_list|)
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|AsyncBufferedMutatorBuilder
name|getBufferedMutatorBuilder
parameter_list|(
name|TableName
name|tableName
parameter_list|,
name|ExecutorService
name|pool
parameter_list|)
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|CompletableFuture
argument_list|<
name|Hbck
argument_list|>
name|getHbck
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|Hbck
name|getHbck
parameter_list|(
name|ServerName
name|masterServer
parameter_list|)
throws|throws
name|IOException
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isClosed
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|()
throws|throws
name|IOException
block|{   }
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
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|NonceGenerator
name|getNonceGenerator
parameter_list|()
block|{
return|return
literal|null
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
literal|null
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
return|return
literal|null
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
name|numRetries
parameter_list|,
name|long
name|operationTimeoutNs
parameter_list|)
block|{
return|return
literal|null
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
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|CompletableFuture
argument_list|<
name|String
argument_list|>
name|prepareBulkLoad
parameter_list|(
name|TableName
name|tableName
parameter_list|)
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|CompletableFuture
argument_list|<
name|Boolean
argument_list|>
name|bulkLoad
parameter_list|(
name|TableName
name|tableName
parameter_list|,
name|List
argument_list|<
name|Pair
argument_list|<
name|byte
index|[]
argument_list|,
name|String
argument_list|>
argument_list|>
name|familyPaths
parameter_list|,
name|byte
index|[]
name|row
parameter_list|,
name|boolean
name|assignSeqNum
parameter_list|,
name|Token
argument_list|<
name|?
argument_list|>
name|userToken
parameter_list|,
name|String
name|bulkToken
parameter_list|,
name|boolean
name|copyFiles
parameter_list|)
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|CompletableFuture
argument_list|<
name|Void
argument_list|>
name|cleanupBulkLoad
parameter_list|(
name|TableName
name|tableName
parameter_list|,
name|String
name|bulkToken
parameter_list|)
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|Connection
name|toConnection
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
block|}
end_class

end_unit

