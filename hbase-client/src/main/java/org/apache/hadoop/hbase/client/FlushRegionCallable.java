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
name|hadoop
operator|.
name|hbase
operator|.
name|util
operator|.
name|EnvironmentEdgeManager
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
name|slf4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|LoggerFactory
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

begin_comment
comment|/**  * A Callable for flushRegion() RPC.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|FlushRegionCallable
extends|extends
name|RegionAdminServiceCallable
argument_list|<
name|FlushRegionResponse
argument_list|>
block|{
specifier|private
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|FlushRegionCallable
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|byte
index|[]
name|regionName
decl_stmt|;
specifier|private
specifier|final
name|boolean
name|writeFlushWalMarker
decl_stmt|;
specifier|private
name|boolean
name|reload
decl_stmt|;
specifier|public
name|FlushRegionCallable
parameter_list|(
name|ConnectionImplementation
name|connection
parameter_list|,
name|RpcControllerFactory
name|rpcControllerFactory
parameter_list|,
name|TableName
name|tableName
parameter_list|,
name|byte
index|[]
name|regionName
parameter_list|,
name|byte
index|[]
name|regionStartKey
parameter_list|,
name|boolean
name|writeFlushWalMarker
parameter_list|)
block|{
name|super
argument_list|(
name|connection
argument_list|,
name|rpcControllerFactory
argument_list|,
name|tableName
argument_list|,
name|regionStartKey
argument_list|)
expr_stmt|;
name|this
operator|.
name|regionName
operator|=
name|regionName
expr_stmt|;
name|this
operator|.
name|writeFlushWalMarker
operator|=
name|writeFlushWalMarker
expr_stmt|;
block|}
specifier|public
name|FlushRegionCallable
parameter_list|(
name|ConnectionImplementation
name|connection
parameter_list|,
name|RpcControllerFactory
name|rpcControllerFactory
parameter_list|,
name|RegionInfo
name|regionInfo
parameter_list|,
name|boolean
name|writeFlushWalMarker
parameter_list|)
block|{
name|this
argument_list|(
name|connection
argument_list|,
name|rpcControllerFactory
argument_list|,
name|regionInfo
operator|.
name|getTable
argument_list|()
argument_list|,
name|regionInfo
operator|.
name|getRegionName
argument_list|()
argument_list|,
name|regionInfo
operator|.
name|getStartKey
argument_list|()
argument_list|,
name|writeFlushWalMarker
argument_list|)
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
name|super
operator|.
name|prepare
argument_list|(
name|reload
argument_list|)
expr_stmt|;
name|this
operator|.
name|reload
operator|=
name|reload
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|FlushRegionResponse
name|call
parameter_list|(
name|HBaseRpcController
name|controller
parameter_list|)
throws|throws
name|Exception
block|{
comment|// Check whether we should still do the flush to this region. If the regions are changed due
comment|// to splits or merges, etc return success
if|if
condition|(
operator|!
name|Bytes
operator|.
name|equals
argument_list|(
name|location
operator|.
name|getRegion
argument_list|()
operator|.
name|getRegionName
argument_list|()
argument_list|,
name|regionName
argument_list|)
condition|)
block|{
if|if
condition|(
operator|!
name|reload
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Cached location seems to be different than requested region."
argument_list|)
throw|;
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"Skipping flush region, because the located region "
operator|+
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|location
operator|.
name|getRegion
argument_list|()
operator|.
name|getRegionName
argument_list|()
argument_list|)
operator|+
literal|" is different than "
operator|+
literal|" requested region "
operator|+
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|regionName
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|FlushRegionResponse
operator|.
name|newBuilder
argument_list|()
operator|.
name|setLastFlushTime
argument_list|(
name|EnvironmentEdgeManager
operator|.
name|currentTime
argument_list|()
argument_list|)
operator|.
name|setFlushed
argument_list|(
literal|false
argument_list|)
operator|.
name|setWroteFlushWalMarker
argument_list|(
literal|false
argument_list|)
operator|.
name|build
argument_list|()
return|;
block|}
name|FlushRegionRequest
name|request
init|=
name|RequestConverter
operator|.
name|buildFlushRegionRequest
argument_list|(
name|regionName
argument_list|,
name|writeFlushWalMarker
argument_list|)
decl_stmt|;
return|return
name|stub
operator|.
name|flushRegion
argument_list|(
name|controller
argument_list|,
name|request
argument_list|)
return|;
block|}
block|}
end_class

end_unit

