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
name|ArrayList
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
name|Map
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
name|CellScannable
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
name|CellUtil
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
name|shaded
operator|.
name|protobuf
operator|.
name|RequestConverter
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
name|ResponseConverter
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
name|protobuf
operator|.
name|generated
operator|.
name|ClientProtos
operator|.
name|MultiRequest
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
name|MutationProto
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
name|RegionAction
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
name|HBaseProtos
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
name|common
operator|.
name|annotations
operator|.
name|VisibleForTesting
import|;
end_import

begin_comment
comment|/**  * Callable that handles the<code>multi</code> method call going against a single  * regionserver; i.e. A RegionServerCallable for the multi call (It is NOT a  * RegionServerCallable that goes against multiple regions).  * @param<R>  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
class|class
name|MultiServerCallable
extends|extends
name|CancellableRegionServerCallable
argument_list|<
name|MultiResponse
argument_list|>
block|{
specifier|private
name|MultiAction
name|multiAction
decl_stmt|;
specifier|private
name|boolean
name|cellBlock
decl_stmt|;
name|MultiServerCallable
parameter_list|(
specifier|final
name|ClusterConnection
name|connection
parameter_list|,
specifier|final
name|TableName
name|tableName
parameter_list|,
specifier|final
name|ServerName
name|location
parameter_list|,
specifier|final
name|MultiAction
name|multi
parameter_list|,
name|RpcController
name|rpcController
parameter_list|,
name|int
name|rpcTimeout
parameter_list|,
name|RetryingTimeTracker
name|tracker
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
literal|null
argument_list|,
name|rpcController
argument_list|,
name|rpcTimeout
argument_list|,
name|tracker
argument_list|,
name|priority
argument_list|)
expr_stmt|;
name|this
operator|.
name|multiAction
operator|=
name|multi
expr_stmt|;
comment|// RegionServerCallable has HRegionLocation field, but this is a multi-region request.
comment|// Using region info from parent HRegionLocation would be a mistake for this class; so
comment|// we will store the server here, and throw if someone tries to obtain location/regioninfo.
name|this
operator|.
name|location
operator|=
operator|new
name|HRegionLocation
argument_list|(
literal|null
argument_list|,
name|location
argument_list|)
expr_stmt|;
name|this
operator|.
name|cellBlock
operator|=
name|isCellBlock
argument_list|()
expr_stmt|;
block|}
specifier|public
name|void
name|reset
parameter_list|(
name|ServerName
name|location
parameter_list|,
name|MultiAction
name|multiAction
parameter_list|)
block|{
name|this
operator|.
name|location
operator|=
operator|new
name|HRegionLocation
argument_list|(
literal|null
argument_list|,
name|location
argument_list|)
expr_stmt|;
name|this
operator|.
name|multiAction
operator|=
name|multiAction
expr_stmt|;
name|this
operator|.
name|cellBlock
operator|=
name|isCellBlock
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|HRegionLocation
name|getLocation
parameter_list|()
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Cannot get region location for multi-region request"
argument_list|)
throw|;
block|}
annotation|@
name|Override
specifier|public
name|HRegionInfo
name|getHRegionInfo
parameter_list|()
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Cannot get region info for multi-region request"
argument_list|)
throw|;
block|}
name|MultiAction
name|getMulti
parameter_list|()
block|{
return|return
name|this
operator|.
name|multiAction
return|;
block|}
annotation|@
name|Override
specifier|protected
name|MultiResponse
name|rpcCall
parameter_list|()
throws|throws
name|Exception
block|{
name|int
name|countOfActions
init|=
name|this
operator|.
name|multiAction
operator|.
name|size
argument_list|()
decl_stmt|;
if|if
condition|(
name|countOfActions
operator|<=
literal|0
condition|)
throw|throw
operator|new
name|DoNotRetryIOException
argument_list|(
literal|"No Actions"
argument_list|)
throw|;
name|MultiRequest
operator|.
name|Builder
name|multiRequestBuilder
init|=
name|MultiRequest
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
name|RegionAction
operator|.
name|Builder
name|regionActionBuilder
init|=
name|RegionAction
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
name|ClientProtos
operator|.
name|Action
operator|.
name|Builder
name|actionBuilder
init|=
name|ClientProtos
operator|.
name|Action
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
name|MutationProto
operator|.
name|Builder
name|mutationBuilder
init|=
name|MutationProto
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|CellScannable
argument_list|>
name|cells
init|=
literal|null
decl_stmt|;
comment|// The multi object is a list of Actions by region.  Iterate by region.
name|long
name|nonceGroup
init|=
name|multiAction
operator|.
name|getNonceGroup
argument_list|()
decl_stmt|;
if|if
condition|(
name|nonceGroup
operator|!=
name|HConstants
operator|.
name|NO_NONCE
condition|)
block|{
name|multiRequestBuilder
operator|.
name|setNonceGroup
argument_list|(
name|nonceGroup
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|byte
index|[]
argument_list|,
name|List
argument_list|<
name|Action
argument_list|>
argument_list|>
name|e
range|:
name|this
operator|.
name|multiAction
operator|.
name|actions
operator|.
name|entrySet
argument_list|()
control|)
block|{
specifier|final
name|byte
index|[]
name|regionName
init|=
name|e
operator|.
name|getKey
argument_list|()
decl_stmt|;
specifier|final
name|List
argument_list|<
name|Action
argument_list|>
name|actions
init|=
name|e
operator|.
name|getValue
argument_list|()
decl_stmt|;
name|regionActionBuilder
operator|.
name|clear
argument_list|()
expr_stmt|;
name|regionActionBuilder
operator|.
name|setRegion
argument_list|(
name|RequestConverter
operator|.
name|buildRegionSpecifier
argument_list|(
name|HBaseProtos
operator|.
name|RegionSpecifier
operator|.
name|RegionSpecifierType
operator|.
name|REGION_NAME
argument_list|,
name|regionName
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|this
operator|.
name|cellBlock
condition|)
block|{
comment|// Pre-size. Presume at least a KV per Action.  There are likely more.
if|if
condition|(
name|cells
operator|==
literal|null
condition|)
name|cells
operator|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|countOfActions
argument_list|)
expr_stmt|;
comment|// Send data in cellblocks. The call to buildNoDataMultiRequest will skip RowMutations.
comment|// They have already been handled above. Guess at count of cells
name|regionActionBuilder
operator|=
name|RequestConverter
operator|.
name|buildNoDataRegionAction
argument_list|(
name|regionName
argument_list|,
name|actions
argument_list|,
name|cells
argument_list|,
name|regionActionBuilder
argument_list|,
name|actionBuilder
argument_list|,
name|mutationBuilder
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|regionActionBuilder
operator|=
name|RequestConverter
operator|.
name|buildRegionAction
argument_list|(
name|regionName
argument_list|,
name|actions
argument_list|,
name|regionActionBuilder
argument_list|,
name|actionBuilder
argument_list|,
name|mutationBuilder
argument_list|)
expr_stmt|;
block|}
name|multiRequestBuilder
operator|.
name|addRegionAction
argument_list|(
name|regionActionBuilder
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|cells
operator|!=
literal|null
condition|)
block|{
name|setRpcControllerCellScanner
argument_list|(
name|CellUtil
operator|.
name|createCellScanner
argument_list|(
name|cells
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|ClientProtos
operator|.
name|MultiRequest
name|requestProto
init|=
name|multiRequestBuilder
operator|.
name|build
argument_list|()
decl_stmt|;
name|ClientProtos
operator|.
name|MultiResponse
name|responseProto
init|=
name|getStub
argument_list|()
operator|.
name|multi
argument_list|(
name|getRpcController
argument_list|()
argument_list|,
name|requestProto
argument_list|)
decl_stmt|;
if|if
condition|(
name|responseProto
operator|==
literal|null
condition|)
return|return
literal|null
return|;
comment|// Occurs on cancel
return|return
name|ResponseConverter
operator|.
name|getResults
argument_list|(
name|requestProto
argument_list|,
name|responseProto
argument_list|,
name|getRpcControllerCellScanner
argument_list|()
argument_list|)
return|;
block|}
comment|/**    * @return True if we should send data in cellblocks.  This is an expensive call.  Cache the    * result if you can rather than call each time.    */
specifier|private
name|boolean
name|isCellBlock
parameter_list|()
block|{
comment|// This is not exact -- the configuration could have changed on us after connection was set up
comment|// but it will do for now.
name|ClusterConnection
name|conn
init|=
name|getConnection
argument_list|()
decl_stmt|;
return|return
name|conn
operator|.
name|hasCellBlockSupport
argument_list|()
return|;
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
comment|// Use the location we were given in the constructor rather than go look it up.
name|setStub
argument_list|(
name|getConnection
argument_list|()
operator|.
name|getClient
argument_list|(
name|this
operator|.
name|location
operator|.
name|getServerName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|VisibleForTesting
name|ServerName
name|getServerName
parameter_list|()
block|{
return|return
name|location
operator|.
name|getServerName
argument_list|()
return|;
block|}
block|}
end_class

end_unit

