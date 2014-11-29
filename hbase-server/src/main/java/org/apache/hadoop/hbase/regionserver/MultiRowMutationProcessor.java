begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|Arrays
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collection
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
name|Cell
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
name|client
operator|.
name|Delete
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
name|Durability
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
name|client
operator|.
name|Put
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
name|MultiRowMutationProcessorRequest
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
name|MultiRowMutationProcessorResponse
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
name|wal
operator|.
name|WALEdit
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
comment|/**  * A<code>MultiRowProcessor</code> that performs multiple puts and deletes.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
class|class
name|MultiRowMutationProcessor
extends|extends
name|BaseRowProcessor
argument_list|<
name|MultiRowMutationProcessorRequest
argument_list|,
name|MultiRowMutationProcessorResponse
argument_list|>
block|{
name|Collection
argument_list|<
name|byte
index|[]
argument_list|>
name|rowsToLock
decl_stmt|;
name|Collection
argument_list|<
name|Mutation
argument_list|>
name|mutations
decl_stmt|;
name|MiniBatchOperationInProgress
argument_list|<
name|Mutation
argument_list|>
name|miniBatch
decl_stmt|;
name|MultiRowMutationProcessor
parameter_list|(
name|Collection
argument_list|<
name|Mutation
argument_list|>
name|mutations
parameter_list|,
name|Collection
argument_list|<
name|byte
index|[]
argument_list|>
name|rowsToLock
parameter_list|)
block|{
name|this
operator|.
name|rowsToLock
operator|=
name|rowsToLock
expr_stmt|;
name|this
operator|.
name|mutations
operator|=
name|mutations
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Collection
argument_list|<
name|byte
index|[]
argument_list|>
name|getRowsToLock
parameter_list|()
block|{
return|return
name|rowsToLock
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|readOnly
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
annotation|@
name|Override
specifier|public
name|MultiRowMutationProcessorResponse
name|getResult
parameter_list|()
block|{
return|return
name|MultiRowMutationProcessorResponse
operator|.
name|getDefaultInstance
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|process
parameter_list|(
name|long
name|now
parameter_list|,
name|HRegion
name|region
parameter_list|,
name|List
argument_list|<
name|Mutation
argument_list|>
name|mutationsToApply
parameter_list|,
name|WALEdit
name|walEdit
parameter_list|)
throws|throws
name|IOException
block|{
name|byte
index|[]
name|byteNow
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|now
argument_list|)
decl_stmt|;
comment|// Check mutations
for|for
control|(
name|Mutation
name|m
range|:
name|this
operator|.
name|mutations
control|)
block|{
if|if
condition|(
name|m
operator|instanceof
name|Put
condition|)
block|{
name|Map
argument_list|<
name|byte
index|[]
argument_list|,
name|List
argument_list|<
name|Cell
argument_list|>
argument_list|>
name|familyMap
init|=
name|m
operator|.
name|getFamilyCellMap
argument_list|()
decl_stmt|;
name|region
operator|.
name|checkFamilies
argument_list|(
name|familyMap
operator|.
name|keySet
argument_list|()
argument_list|)
expr_stmt|;
name|region
operator|.
name|checkTimestamps
argument_list|(
name|familyMap
argument_list|,
name|now
argument_list|)
expr_stmt|;
name|region
operator|.
name|updateCellTimestamps
argument_list|(
name|familyMap
operator|.
name|values
argument_list|()
argument_list|,
name|byteNow
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|m
operator|instanceof
name|Delete
condition|)
block|{
name|Delete
name|d
init|=
operator|(
name|Delete
operator|)
name|m
decl_stmt|;
name|region
operator|.
name|prepareDelete
argument_list|(
name|d
argument_list|)
expr_stmt|;
name|region
operator|.
name|prepareDeleteTimestamps
argument_list|(
name|d
argument_list|,
name|d
operator|.
name|getFamilyCellMap
argument_list|()
argument_list|,
name|byteNow
argument_list|)
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|DoNotRetryIOException
argument_list|(
literal|"Action must be Put or Delete. But was: "
operator|+
name|m
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
throw|;
block|}
name|mutationsToApply
operator|.
name|add
argument_list|(
name|m
argument_list|)
expr_stmt|;
block|}
comment|// Apply edits to a single WALEdit
for|for
control|(
name|Mutation
name|m
range|:
name|mutations
control|)
block|{
for|for
control|(
name|List
argument_list|<
name|Cell
argument_list|>
name|cells
range|:
name|m
operator|.
name|getFamilyCellMap
argument_list|()
operator|.
name|values
argument_list|()
control|)
block|{
name|boolean
name|writeToWAL
init|=
name|m
operator|.
name|getDurability
argument_list|()
operator|!=
name|Durability
operator|.
name|SKIP_WAL
decl_stmt|;
for|for
control|(
name|Cell
name|cell
range|:
name|cells
control|)
block|{
if|if
condition|(
name|writeToWAL
condition|)
name|walEdit
operator|.
name|add
argument_list|(
name|cell
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|preProcess
parameter_list|(
name|HRegion
name|region
parameter_list|,
name|WALEdit
name|walEdit
parameter_list|)
throws|throws
name|IOException
block|{
name|RegionCoprocessorHost
name|coprocessorHost
init|=
name|region
operator|.
name|getCoprocessorHost
argument_list|()
decl_stmt|;
if|if
condition|(
name|coprocessorHost
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|Mutation
name|m
range|:
name|mutations
control|)
block|{
if|if
condition|(
name|m
operator|instanceof
name|Put
condition|)
block|{
if|if
condition|(
name|coprocessorHost
operator|.
name|prePut
argument_list|(
operator|(
name|Put
operator|)
name|m
argument_list|,
name|walEdit
argument_list|,
name|m
operator|.
name|getDurability
argument_list|()
argument_list|)
condition|)
block|{
comment|// by pass everything
return|return;
block|}
block|}
elseif|else
if|if
condition|(
name|m
operator|instanceof
name|Delete
condition|)
block|{
name|Delete
name|d
init|=
operator|(
name|Delete
operator|)
name|m
decl_stmt|;
name|region
operator|.
name|prepareDelete
argument_list|(
name|d
argument_list|)
expr_stmt|;
if|if
condition|(
name|coprocessorHost
operator|.
name|preDelete
argument_list|(
name|d
argument_list|,
name|walEdit
argument_list|,
name|d
operator|.
name|getDurability
argument_list|()
argument_list|)
condition|)
block|{
comment|// by pass everything
return|return;
block|}
block|}
block|}
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|preBatchMutate
parameter_list|(
name|HRegion
name|region
parameter_list|,
name|WALEdit
name|walEdit
parameter_list|)
throws|throws
name|IOException
block|{
comment|// TODO we should return back the status of this hook run to HRegion so that those Mutations
comment|// with OperationStatus as SUCCESS or FAILURE should not get applied to memstore.
name|RegionCoprocessorHost
name|coprocessorHost
init|=
name|region
operator|.
name|getCoprocessorHost
argument_list|()
decl_stmt|;
name|OperationStatus
index|[]
name|opStatus
init|=
operator|new
name|OperationStatus
index|[
name|mutations
operator|.
name|size
argument_list|()
index|]
decl_stmt|;
name|Arrays
operator|.
name|fill
argument_list|(
name|opStatus
argument_list|,
name|OperationStatus
operator|.
name|NOT_RUN
argument_list|)
expr_stmt|;
name|WALEdit
index|[]
name|walEditsFromCP
init|=
operator|new
name|WALEdit
index|[
name|mutations
operator|.
name|size
argument_list|()
index|]
decl_stmt|;
if|if
condition|(
name|coprocessorHost
operator|!=
literal|null
condition|)
block|{
name|miniBatch
operator|=
operator|new
name|MiniBatchOperationInProgress
argument_list|<
name|Mutation
argument_list|>
argument_list|(
name|mutations
operator|.
name|toArray
argument_list|(
operator|new
name|Mutation
index|[
name|mutations
operator|.
name|size
argument_list|()
index|]
argument_list|)
argument_list|,
name|opStatus
argument_list|,
name|walEditsFromCP
argument_list|,
literal|0
argument_list|,
name|mutations
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|coprocessorHost
operator|.
name|preBatchMutate
argument_list|(
name|miniBatch
argument_list|)
expr_stmt|;
block|}
comment|// Apply edits to a single WALEdit
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|mutations
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|opStatus
index|[
name|i
index|]
operator|==
name|OperationStatus
operator|.
name|NOT_RUN
condition|)
block|{
comment|// Other OperationStatusCode means that Mutation is already succeeded or failed in CP hook
comment|// itself. No need to apply again to region
if|if
condition|(
name|walEditsFromCP
index|[
name|i
index|]
operator|!=
literal|null
condition|)
block|{
comment|// Add the WALEdit created by CP hook
for|for
control|(
name|Cell
name|walCell
range|:
name|walEditsFromCP
index|[
name|i
index|]
operator|.
name|getCells
argument_list|()
control|)
block|{
name|walEdit
operator|.
name|add
argument_list|(
name|walCell
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|postBatchMutate
parameter_list|(
name|HRegion
name|region
parameter_list|)
throws|throws
name|IOException
block|{
name|RegionCoprocessorHost
name|coprocessorHost
init|=
name|region
operator|.
name|getCoprocessorHost
argument_list|()
decl_stmt|;
if|if
condition|(
name|coprocessorHost
operator|!=
literal|null
condition|)
block|{
assert|assert
name|miniBatch
operator|!=
literal|null
assert|;
comment|// Use the same miniBatch state used to call the preBatchMutate()
name|coprocessorHost
operator|.
name|postBatchMutate
argument_list|(
name|miniBatch
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|postProcess
parameter_list|(
name|HRegion
name|region
parameter_list|,
name|WALEdit
name|walEdit
parameter_list|,
name|boolean
name|success
parameter_list|)
throws|throws
name|IOException
block|{
name|RegionCoprocessorHost
name|coprocessorHost
init|=
name|region
operator|.
name|getCoprocessorHost
argument_list|()
decl_stmt|;
if|if
condition|(
name|coprocessorHost
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|Mutation
name|m
range|:
name|mutations
control|)
block|{
if|if
condition|(
name|m
operator|instanceof
name|Put
condition|)
block|{
name|coprocessorHost
operator|.
name|postPut
argument_list|(
operator|(
name|Put
operator|)
name|m
argument_list|,
name|walEdit
argument_list|,
name|m
operator|.
name|getDurability
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|m
operator|instanceof
name|Delete
condition|)
block|{
name|coprocessorHost
operator|.
name|postDelete
argument_list|(
operator|(
name|Delete
operator|)
name|m
argument_list|,
name|walEdit
argument_list|,
name|m
operator|.
name|getDurability
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
comment|// At the end call the CP hook postBatchMutateIndispensably
if|if
condition|(
name|miniBatch
operator|!=
literal|null
condition|)
block|{
comment|// Directly calling this hook, with out calling pre/postBatchMutate() when Processor do a
comment|// read only process. Then no need to call this batch based CP hook also.
name|coprocessorHost
operator|.
name|postBatchMutateIndispensably
argument_list|(
name|miniBatch
argument_list|,
name|success
argument_list|)
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|Override
specifier|public
name|MultiRowMutationProcessorRequest
name|getRequestData
parameter_list|()
block|{
return|return
name|MultiRowMutationProcessorRequest
operator|.
name|getDefaultInstance
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|initialize
parameter_list|(
name|MultiRowMutationProcessorRequest
name|msg
parameter_list|)
block|{
comment|//nothing
block|}
annotation|@
name|Override
specifier|public
name|Durability
name|useDurability
parameter_list|()
block|{
comment|// return true when at least one mutation requested a WAL flush (default)
name|Durability
name|durability
init|=
name|Durability
operator|.
name|USE_DEFAULT
decl_stmt|;
for|for
control|(
name|Mutation
name|m
range|:
name|mutations
control|)
block|{
if|if
condition|(
name|m
operator|.
name|getDurability
argument_list|()
operator|.
name|ordinal
argument_list|()
operator|>
name|durability
operator|.
name|ordinal
argument_list|()
condition|)
block|{
name|durability
operator|=
name|m
operator|.
name|getDurability
argument_list|()
expr_stmt|;
block|}
block|}
return|return
name|durability
return|;
block|}
block|}
end_class

end_unit

