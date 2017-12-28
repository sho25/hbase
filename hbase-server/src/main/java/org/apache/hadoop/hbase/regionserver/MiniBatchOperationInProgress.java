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
name|regionserver
package|;
end_package

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
name|hbase
operator|.
name|thirdparty
operator|.
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|base
operator|.
name|Preconditions
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
name|wal
operator|.
name|WALEdit
import|;
end_import

begin_comment
comment|/**  * Wraps together the mutations which are applied as a batch to the region and their operation  * status and WALEdits.  * @see org.apache.hadoop.hbase.coprocessor.RegionObserver#preBatchMutate(  * org.apache.hadoop.hbase.coprocessor.ObserverContext, MiniBatchOperationInProgress)  * @see org.apache.hadoop.hbase.coprocessor.RegionObserver#postBatchMutate(  * org.apache.hadoop.hbase.coprocessor.ObserverContext, MiniBatchOperationInProgress)  * @param T Pair&lt;Mutation, Integer&gt; pair of Mutations and associated rowlock ids .  */
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
specifier|public
class|class
name|MiniBatchOperationInProgress
parameter_list|<
name|T
parameter_list|>
block|{
specifier|private
specifier|final
name|T
index|[]
name|operations
decl_stmt|;
specifier|private
name|Mutation
index|[]
index|[]
name|operationsFromCoprocessors
decl_stmt|;
specifier|private
specifier|final
name|OperationStatus
index|[]
name|retCodeDetails
decl_stmt|;
specifier|private
specifier|final
name|WALEdit
index|[]
name|walEditsFromCoprocessors
decl_stmt|;
specifier|private
specifier|final
name|int
name|firstIndex
decl_stmt|;
specifier|private
specifier|final
name|int
name|lastIndexExclusive
decl_stmt|;
specifier|private
name|int
name|readyToWriteCount
init|=
literal|0
decl_stmt|;
specifier|private
name|int
name|cellCount
init|=
literal|0
decl_stmt|;
specifier|private
name|int
name|numOfPuts
init|=
literal|0
decl_stmt|;
specifier|private
name|int
name|numOfDeletes
init|=
literal|0
decl_stmt|;
specifier|public
name|MiniBatchOperationInProgress
parameter_list|(
name|T
index|[]
name|operations
parameter_list|,
name|OperationStatus
index|[]
name|retCodeDetails
parameter_list|,
name|WALEdit
index|[]
name|walEditsFromCoprocessors
parameter_list|,
name|int
name|firstIndex
parameter_list|,
name|int
name|lastIndexExclusive
parameter_list|,
name|int
name|readyToWriteCount
parameter_list|)
block|{
name|Preconditions
operator|.
name|checkArgument
argument_list|(
name|readyToWriteCount
operator|<=
operator|(
name|lastIndexExclusive
operator|-
name|firstIndex
operator|)
argument_list|)
expr_stmt|;
name|this
operator|.
name|operations
operator|=
name|operations
expr_stmt|;
name|this
operator|.
name|retCodeDetails
operator|=
name|retCodeDetails
expr_stmt|;
name|this
operator|.
name|walEditsFromCoprocessors
operator|=
name|walEditsFromCoprocessors
expr_stmt|;
name|this
operator|.
name|firstIndex
operator|=
name|firstIndex
expr_stmt|;
name|this
operator|.
name|lastIndexExclusive
operator|=
name|lastIndexExclusive
expr_stmt|;
name|this
operator|.
name|readyToWriteCount
operator|=
name|readyToWriteCount
expr_stmt|;
block|}
comment|/**    * @return The number of operations(Mutations) involved in this batch.    */
specifier|public
name|int
name|size
parameter_list|()
block|{
return|return
name|this
operator|.
name|lastIndexExclusive
operator|-
name|this
operator|.
name|firstIndex
return|;
block|}
comment|/**    * @param index    * @return The operation(Mutation) at the specified position.    */
specifier|public
name|T
name|getOperation
parameter_list|(
name|int
name|index
parameter_list|)
block|{
return|return
name|operations
index|[
name|getAbsoluteIndex
argument_list|(
name|index
argument_list|)
index|]
return|;
block|}
comment|/**    * Sets the status code for the operation(Mutation) at the specified position.    * By setting this status, {@link org.apache.hadoop.hbase.coprocessor.RegionObserver}    * can make HRegion to skip Mutations.    * @param index    * @param opStatus    */
specifier|public
name|void
name|setOperationStatus
parameter_list|(
name|int
name|index
parameter_list|,
name|OperationStatus
name|opStatus
parameter_list|)
block|{
name|this
operator|.
name|retCodeDetails
index|[
name|getAbsoluteIndex
argument_list|(
name|index
argument_list|)
index|]
operator|=
name|opStatus
expr_stmt|;
block|}
comment|/**    * @param index    * @return Gets the status code for the operation(Mutation) at the specified position.    */
specifier|public
name|OperationStatus
name|getOperationStatus
parameter_list|(
name|int
name|index
parameter_list|)
block|{
return|return
name|this
operator|.
name|retCodeDetails
index|[
name|getAbsoluteIndex
argument_list|(
name|index
argument_list|)
index|]
return|;
block|}
comment|/**    * Sets the walEdit for the operation(Mutation) at the specified position.    * @param index    * @param walEdit    */
specifier|public
name|void
name|setWalEdit
parameter_list|(
name|int
name|index
parameter_list|,
name|WALEdit
name|walEdit
parameter_list|)
block|{
name|this
operator|.
name|walEditsFromCoprocessors
index|[
name|getAbsoluteIndex
argument_list|(
name|index
argument_list|)
index|]
operator|=
name|walEdit
expr_stmt|;
block|}
comment|/**    * @param index    * @return Gets the walEdit for the operation(Mutation) at the specified position.    */
specifier|public
name|WALEdit
name|getWalEdit
parameter_list|(
name|int
name|index
parameter_list|)
block|{
return|return
name|this
operator|.
name|walEditsFromCoprocessors
index|[
name|getAbsoluteIndex
argument_list|(
name|index
argument_list|)
index|]
return|;
block|}
specifier|private
name|int
name|getAbsoluteIndex
parameter_list|(
name|int
name|index
parameter_list|)
block|{
if|if
condition|(
name|index
operator|<
literal|0
operator|||
name|this
operator|.
name|firstIndex
operator|+
name|index
operator|>=
name|this
operator|.
name|lastIndexExclusive
condition|)
block|{
throw|throw
operator|new
name|ArrayIndexOutOfBoundsException
argument_list|(
name|index
argument_list|)
throw|;
block|}
return|return
name|this
operator|.
name|firstIndex
operator|+
name|index
return|;
block|}
comment|/**    * Add more Mutations corresponding to the Mutation at the given index to be committed atomically    * in the same batch. These mutations are applied to the WAL and applied to the memstore as well.    * The timestamp of the cells in the given Mutations MUST be obtained from the original mutation.    *<b>Note:</b> The durability from CP will be replaced by the durability of corresponding mutation.    * @param index the index that corresponds to the original mutation index in the batch    * @param newOperations the Mutations to add    */
specifier|public
name|void
name|addOperationsFromCP
parameter_list|(
name|int
name|index
parameter_list|,
name|Mutation
index|[]
name|newOperations
parameter_list|)
block|{
if|if
condition|(
name|this
operator|.
name|operationsFromCoprocessors
operator|==
literal|null
condition|)
block|{
comment|// lazy allocation to save on object allocation in case this is not used
name|this
operator|.
name|operationsFromCoprocessors
operator|=
operator|new
name|Mutation
index|[
name|operations
operator|.
name|length
index|]
index|[]
expr_stmt|;
block|}
name|this
operator|.
name|operationsFromCoprocessors
index|[
name|getAbsoluteIndex
argument_list|(
name|index
argument_list|)
index|]
operator|=
name|newOperations
expr_stmt|;
block|}
specifier|public
name|Mutation
index|[]
name|getOperationsFromCoprocessors
parameter_list|(
name|int
name|index
parameter_list|)
block|{
return|return
name|operationsFromCoprocessors
operator|==
literal|null
condition|?
literal|null
else|:
name|operationsFromCoprocessors
index|[
name|getAbsoluteIndex
argument_list|(
name|index
argument_list|)
index|]
return|;
block|}
specifier|public
name|int
name|getReadyToWriteCount
parameter_list|()
block|{
return|return
name|readyToWriteCount
return|;
block|}
specifier|public
name|int
name|getLastIndexExclusive
parameter_list|()
block|{
return|return
name|lastIndexExclusive
return|;
block|}
specifier|public
name|int
name|getCellCount
parameter_list|()
block|{
return|return
name|cellCount
return|;
block|}
specifier|public
name|void
name|addCellCount
parameter_list|(
name|int
name|cellCount
parameter_list|)
block|{
name|this
operator|.
name|cellCount
operator|+=
name|cellCount
expr_stmt|;
block|}
specifier|public
name|int
name|getNumOfPuts
parameter_list|()
block|{
return|return
name|numOfPuts
return|;
block|}
specifier|public
name|void
name|incrementNumOfPuts
parameter_list|()
block|{
name|this
operator|.
name|numOfPuts
operator|+=
literal|1
expr_stmt|;
block|}
specifier|public
name|int
name|getNumOfDeletes
parameter_list|()
block|{
return|return
name|numOfDeletes
return|;
block|}
specifier|public
name|void
name|incrementNumOfDeletes
parameter_list|()
block|{
name|this
operator|.
name|numOfDeletes
operator|+=
literal|1
expr_stmt|;
block|}
block|}
end_class

end_unit

