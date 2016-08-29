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
name|coprocessor
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
name|SortedSet
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|TreeSet
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
name|classification
operator|.
name|InterfaceStability
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
name|Coprocessor
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
name|CoprocessorEnvironment
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
name|HBaseInterfaceAudience
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
name|client
operator|.
name|Mutation
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
name|regionserver
operator|.
name|HRegion
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
name|regionserver
operator|.
name|WrongRegionException
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
name|protobuf
operator|.
name|generated
operator|.
name|MultiRowMutationProtos
operator|.
name|MutateRowsRequest
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
name|MultiRowMutationProtos
operator|.
name|MutateRowsResponse
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
name|MultiRowMutationProtos
operator|.
name|MultiRowMutationService
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
name|RpcController
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
name|Service
import|;
end_import

begin_comment
comment|/**  * This class demonstrates how to implement atomic multi row transactions using  * {@link HRegion#mutateRowsWithLocks(java.util.Collection, java.util.Collection)}  * and Coprocessor endpoints.  *  * Defines a protocol to perform multi row transactions.  * See {@link MultiRowMutationEndpoint} for the implementation.  *<br>  * See  * {@link HRegion#mutateRowsWithLocks(java.util.Collection, java.util.Collection)}  * for details and limitations.  *<br>  * Example:  *<code>  * List&lt;Mutation&gt; mutations = ...;  * Put p1 = new Put(row1);  * Put p2 = new Put(row2);  * ...  * Mutate m1 = ProtobufUtil.toMutate(MutateType.PUT, p1);  * Mutate m2 = ProtobufUtil.toMutate(MutateType.PUT, p2);  * MutateRowsRequest.Builder mrmBuilder = MutateRowsRequest.newBuilder();  * mrmBuilder.addMutationRequest(m1);  * mrmBuilder.addMutationRequest(m2);  * CoprocessorRpcChannel channel = t.coprocessorService(ROW);  * MultiRowMutationService.BlockingInterface service =  *    MultiRowMutationService.newBlockingStub(channel);  * MutateRowsRequest mrm = mrmBuilder.build();  * service.mutateRows(null, mrm);  *</code>  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|LimitedPrivate
argument_list|(
name|HBaseInterfaceAudience
operator|.
name|COPROC
argument_list|)
annotation|@
name|InterfaceStability
operator|.
name|Evolving
specifier|public
class|class
name|MultiRowMutationEndpoint
extends|extends
name|MultiRowMutationService
implements|implements
name|CoprocessorService
implements|,
name|Coprocessor
block|{
specifier|private
name|RegionCoprocessorEnvironment
name|env
decl_stmt|;
annotation|@
name|Override
specifier|public
name|void
name|mutateRows
parameter_list|(
name|RpcController
name|controller
parameter_list|,
name|MutateRowsRequest
name|request
parameter_list|,
name|RpcCallback
argument_list|<
name|MutateRowsResponse
argument_list|>
name|done
parameter_list|)
block|{
name|MutateRowsResponse
name|response
init|=
name|MutateRowsResponse
operator|.
name|getDefaultInstance
argument_list|()
decl_stmt|;
try|try
block|{
comment|// set of rows to lock, sorted to avoid deadlocks
name|SortedSet
argument_list|<
name|byte
index|[]
argument_list|>
name|rowsToLock
init|=
operator|new
name|TreeSet
argument_list|<
name|byte
index|[]
argument_list|>
argument_list|(
name|Bytes
operator|.
name|BYTES_COMPARATOR
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|MutationProto
argument_list|>
name|mutateRequestList
init|=
name|request
operator|.
name|getMutationRequestList
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|Mutation
argument_list|>
name|mutations
init|=
operator|new
name|ArrayList
argument_list|<
name|Mutation
argument_list|>
argument_list|(
name|mutateRequestList
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|MutationProto
name|m
range|:
name|mutateRequestList
control|)
block|{
name|mutations
operator|.
name|add
argument_list|(
name|ProtobufUtil
operator|.
name|toMutation
argument_list|(
name|m
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|HRegionInfo
name|regionInfo
init|=
name|env
operator|.
name|getRegion
argument_list|()
operator|.
name|getRegionInfo
argument_list|()
decl_stmt|;
for|for
control|(
name|Mutation
name|m
range|:
name|mutations
control|)
block|{
comment|// check whether rows are in range for this region
if|if
condition|(
operator|!
name|HRegion
operator|.
name|rowIsInRange
argument_list|(
name|regionInfo
argument_list|,
name|m
operator|.
name|getRow
argument_list|()
argument_list|)
condition|)
block|{
name|String
name|msg
init|=
literal|"Requested row out of range '"
operator|+
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|m
operator|.
name|getRow
argument_list|()
argument_list|)
operator|+
literal|"'"
decl_stmt|;
if|if
condition|(
name|rowsToLock
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
comment|// if this is the first row, region might have moved,
comment|// allow client to retry
throw|throw
operator|new
name|WrongRegionException
argument_list|(
name|msg
argument_list|)
throw|;
block|}
else|else
block|{
comment|// rows are split between regions, do not retry
throw|throw
operator|new
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|DoNotRetryIOException
argument_list|(
name|msg
argument_list|)
throw|;
block|}
block|}
name|rowsToLock
operator|.
name|add
argument_list|(
name|m
operator|.
name|getRow
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|// call utility method on region
name|long
name|nonceGroup
init|=
name|request
operator|.
name|hasNonceGroup
argument_list|()
condition|?
name|request
operator|.
name|getNonceGroup
argument_list|()
else|:
name|HConstants
operator|.
name|NO_NONCE
decl_stmt|;
name|long
name|nonce
init|=
name|request
operator|.
name|hasNonce
argument_list|()
condition|?
name|request
operator|.
name|getNonce
argument_list|()
else|:
name|HConstants
operator|.
name|NO_NONCE
decl_stmt|;
name|env
operator|.
name|getRegion
argument_list|()
operator|.
name|mutateRowsWithLocks
argument_list|(
name|mutations
argument_list|,
name|rowsToLock
argument_list|,
name|nonceGroup
argument_list|,
name|nonce
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|ResponseConverter
operator|.
name|setControllerException
argument_list|(
name|controller
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
name|done
operator|.
name|run
argument_list|(
name|response
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Service
name|getService
parameter_list|()
block|{
return|return
name|this
return|;
block|}
comment|/**    * Stores a reference to the coprocessor environment provided by the    * {@link org.apache.hadoop.hbase.regionserver.RegionCoprocessorHost} from the region where this    * coprocessor is loaded.  Since this is a coprocessor endpoint, it always expects to be loaded    * on a table region, so always expects this to be an instance of    * {@link RegionCoprocessorEnvironment}.    * @param env the environment provided by the coprocessor host    * @throws IOException if the provided environment is not an instance of    * {@code RegionCoprocessorEnvironment}    */
annotation|@
name|Override
specifier|public
name|void
name|start
parameter_list|(
name|CoprocessorEnvironment
name|env
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|env
operator|instanceof
name|RegionCoprocessorEnvironment
condition|)
block|{
name|this
operator|.
name|env
operator|=
operator|(
name|RegionCoprocessorEnvironment
operator|)
name|env
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|CoprocessorException
argument_list|(
literal|"Must be loaded on a table region!"
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|stop
parameter_list|(
name|CoprocessorEnvironment
name|env
parameter_list|)
throws|throws
name|IOException
block|{
comment|// nothing to do
block|}
block|}
end_class

end_unit

