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
name|ipc
operator|.
name|PayloadCarryingRpcController
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
name|MultiRequest
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
name|ServiceException
import|;
end_import

begin_comment
comment|/**  * Callable that handles the<code>multi</code> method call going against a single  * regionserver; i.e. A {@link RegionServerCallable} for the multi call (It is not a  * {@link RegionServerCallable} that goes against multiple regions.  * @param<R>  */
end_comment

begin_class
class|class
name|MultiServerCallable
parameter_list|<
name|R
parameter_list|>
extends|extends
name|RegionServerCallable
argument_list|<
name|MultiResponse
argument_list|>
block|{
specifier|private
specifier|final
name|MultiAction
argument_list|<
name|R
argument_list|>
name|multi
decl_stmt|;
name|MultiServerCallable
parameter_list|(
specifier|final
name|HConnection
name|connection
parameter_list|,
specifier|final
name|TableName
name|tableName
parameter_list|,
specifier|final
name|HRegionLocation
name|location
parameter_list|,
specifier|final
name|MultiAction
argument_list|<
name|R
argument_list|>
name|multi
parameter_list|)
block|{
name|super
argument_list|(
name|connection
argument_list|,
name|tableName
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|this
operator|.
name|multi
operator|=
name|multi
expr_stmt|;
name|setLocation
argument_list|(
name|location
argument_list|)
expr_stmt|;
block|}
name|MultiAction
argument_list|<
name|R
argument_list|>
name|getMulti
parameter_list|()
block|{
return|return
name|this
operator|.
name|multi
return|;
block|}
annotation|@
name|Override
specifier|public
name|MultiResponse
name|call
parameter_list|()
throws|throws
name|IOException
block|{
name|MultiResponse
name|response
init|=
operator|new
name|MultiResponse
argument_list|()
decl_stmt|;
comment|// The multi object is a list of Actions by region.
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
argument_list|<
name|R
argument_list|>
argument_list|>
argument_list|>
name|e
range|:
name|this
operator|.
name|multi
operator|.
name|actions
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|byte
index|[]
name|regionName
init|=
name|e
operator|.
name|getKey
argument_list|()
decl_stmt|;
name|int
name|rowMutations
init|=
literal|0
decl_stmt|;
name|List
argument_list|<
name|Action
argument_list|<
name|R
argument_list|>
argument_list|>
name|actions
init|=
name|e
operator|.
name|getValue
argument_list|()
decl_stmt|;
for|for
control|(
name|Action
argument_list|<
name|R
argument_list|>
name|action
range|:
name|actions
control|)
block|{
name|Row
name|row
init|=
name|action
operator|.
name|getAction
argument_list|()
decl_stmt|;
comment|// Row Mutations are a set of Puts and/or Deletes all to be applied atomically
comment|// on the one row.  We do these a row at a time.
if|if
condition|(
name|row
operator|instanceof
name|RowMutations
condition|)
block|{
try|try
block|{
name|RowMutations
name|rms
init|=
operator|(
name|RowMutations
operator|)
name|row
decl_stmt|;
comment|// Stick all Cells for all RowMutations in here into 'cells'.  Populated when we call
comment|// buildNoDataMultiRequest in the below.
name|List
argument_list|<
name|CellScannable
argument_list|>
name|cells
init|=
operator|new
name|ArrayList
argument_list|<
name|CellScannable
argument_list|>
argument_list|(
name|rms
operator|.
name|getMutations
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
comment|// Build a multi request absent its Cell payload (this is the 'nodata' in the below).
name|MultiRequest
name|multiRequest
init|=
name|RequestConverter
operator|.
name|buildNoDataMultiRequest
argument_list|(
name|regionName
argument_list|,
name|rms
argument_list|,
name|cells
argument_list|)
decl_stmt|;
comment|// Carry the cells over the proxy/pb Service interface using the payload carrying
comment|// rpc controller.
name|getStub
argument_list|()
operator|.
name|multi
argument_list|(
operator|new
name|PayloadCarryingRpcController
argument_list|(
name|cells
argument_list|)
argument_list|,
name|multiRequest
argument_list|)
expr_stmt|;
comment|// This multi call does not return results.
name|response
operator|.
name|add
argument_list|(
name|regionName
argument_list|,
name|action
operator|.
name|getOriginalIndex
argument_list|()
argument_list|,
name|Result
operator|.
name|EMPTY_RESULT
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ServiceException
name|se
parameter_list|)
block|{
name|response
operator|.
name|add
argument_list|(
name|regionName
argument_list|,
name|action
operator|.
name|getOriginalIndex
argument_list|()
argument_list|,
name|ProtobufUtil
operator|.
name|getRemoteException
argument_list|(
name|se
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|rowMutations
operator|++
expr_stmt|;
block|}
block|}
comment|// Are there any non-RowMutation actions to send for this region?
if|if
condition|(
name|actions
operator|.
name|size
argument_list|()
operator|>
name|rowMutations
condition|)
block|{
name|Exception
name|ex
init|=
literal|null
decl_stmt|;
name|List
argument_list|<
name|Object
argument_list|>
name|results
init|=
literal|null
decl_stmt|;
comment|// Stick all Cells for the multiRequest in here into 'cells'.  Gets filled in when we
comment|// call buildNoDataMultiRequest
name|List
argument_list|<
name|CellScannable
argument_list|>
name|cells
init|=
operator|new
name|ArrayList
argument_list|<
name|CellScannable
argument_list|>
argument_list|(
name|actions
operator|.
name|size
argument_list|()
operator|-
name|rowMutations
argument_list|)
decl_stmt|;
try|try
block|{
comment|// The call to buildNoDataMultiRequest will skip RowMutations.  They have
comment|// already been handled above.
name|MultiRequest
name|multiRequest
init|=
name|RequestConverter
operator|.
name|buildNoDataMultiRequest
argument_list|(
name|regionName
argument_list|,
name|actions
argument_list|,
name|cells
argument_list|)
decl_stmt|;
comment|// Controller optionally carries cell data over the proxy/service boundary and also
comment|// optionally ferries cell response data back out again.
name|PayloadCarryingRpcController
name|controller
init|=
operator|new
name|PayloadCarryingRpcController
argument_list|(
name|cells
argument_list|)
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
name|controller
argument_list|,
name|multiRequest
argument_list|)
decl_stmt|;
name|results
operator|=
name|ResponseConverter
operator|.
name|getResults
argument_list|(
name|responseProto
argument_list|,
name|controller
operator|.
name|cellScanner
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ServiceException
name|se
parameter_list|)
block|{
name|ex
operator|=
name|ProtobufUtil
operator|.
name|getRemoteException
argument_list|(
name|se
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|int
name|i
init|=
literal|0
init|,
name|n
init|=
name|actions
operator|.
name|size
argument_list|()
init|;
name|i
operator|<
name|n
condition|;
name|i
operator|++
control|)
block|{
name|int
name|originalIndex
init|=
name|actions
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|getOriginalIndex
argument_list|()
decl_stmt|;
name|response
operator|.
name|add
argument_list|(
name|regionName
argument_list|,
name|originalIndex
argument_list|,
name|results
operator|==
literal|null
condition|?
name|ex
else|:
name|results
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
return|return
name|response
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
name|getLocation
argument_list|()
operator|.
name|getServerName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

