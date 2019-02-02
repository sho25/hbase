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
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|RpcChannel
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
name|TimeUnit
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|function
operator|.
name|Function
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
name|TableName
import|;
end_import

begin_comment
comment|/**  * Can be overridden in UT if you only want to implement part of the methods in {@link AsyncTable}.  */
end_comment

begin_class
specifier|public
class|class
name|DummyAsyncTable
parameter_list|<
name|C
extends|extends
name|ScanResultConsumerBase
parameter_list|>
implements|implements
name|AsyncTable
argument_list|<
name|C
argument_list|>
block|{
annotation|@
name|Override
specifier|public
name|TableName
name|getName
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
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
name|CompletableFuture
argument_list|<
name|TableDescriptor
argument_list|>
name|getDescriptor
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
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getRpcTimeout
parameter_list|(
name|TimeUnit
name|unit
parameter_list|)
block|{
return|return
literal|0
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getReadRpcTimeout
parameter_list|(
name|TimeUnit
name|unit
parameter_list|)
block|{
return|return
literal|0
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getWriteRpcTimeout
parameter_list|(
name|TimeUnit
name|unit
parameter_list|)
block|{
return|return
literal|0
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getOperationTimeout
parameter_list|(
name|TimeUnit
name|unit
parameter_list|)
block|{
return|return
literal|0
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getScanTimeout
parameter_list|(
name|TimeUnit
name|unit
parameter_list|)
block|{
return|return
literal|0
return|;
block|}
annotation|@
name|Override
specifier|public
name|CompletableFuture
argument_list|<
name|Result
argument_list|>
name|get
parameter_list|(
name|Get
name|get
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
name|put
parameter_list|(
name|Put
name|put
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
name|delete
parameter_list|(
name|Delete
name|delete
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
name|Result
argument_list|>
name|append
parameter_list|(
name|Append
name|append
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
name|Result
argument_list|>
name|increment
parameter_list|(
name|Increment
name|increment
parameter_list|)
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|CheckAndMutateBuilder
name|checkAndMutate
parameter_list|(
name|byte
index|[]
name|row
parameter_list|,
name|byte
index|[]
name|family
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
name|mutateRow
parameter_list|(
name|RowMutations
name|mutation
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
name|scan
parameter_list|(
name|Scan
name|scan
parameter_list|,
name|C
name|consumer
parameter_list|)
block|{   }
annotation|@
name|Override
specifier|public
name|ResultScanner
name|getScanner
parameter_list|(
name|Scan
name|scan
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
name|List
argument_list|<
name|Result
argument_list|>
argument_list|>
name|scanAll
parameter_list|(
name|Scan
name|scan
parameter_list|)
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|CompletableFuture
argument_list|<
name|Result
argument_list|>
argument_list|>
name|get
parameter_list|(
name|List
argument_list|<
name|Get
argument_list|>
name|gets
parameter_list|)
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|CompletableFuture
argument_list|<
name|Void
argument_list|>
argument_list|>
name|put
parameter_list|(
name|List
argument_list|<
name|Put
argument_list|>
name|puts
parameter_list|)
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|CompletableFuture
argument_list|<
name|Void
argument_list|>
argument_list|>
name|delete
parameter_list|(
name|List
argument_list|<
name|Delete
argument_list|>
name|deletes
parameter_list|)
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
parameter_list|<
name|T
parameter_list|>
name|List
argument_list|<
name|CompletableFuture
argument_list|<
name|T
argument_list|>
argument_list|>
name|batch
parameter_list|(
name|List
argument_list|<
name|?
extends|extends
name|Row
argument_list|>
name|actions
parameter_list|)
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
parameter_list|<
name|S
parameter_list|,
name|R
parameter_list|>
name|CompletableFuture
argument_list|<
name|R
argument_list|>
name|coprocessorService
parameter_list|(
name|Function
argument_list|<
name|RpcChannel
argument_list|,
name|S
argument_list|>
name|stubMaker
parameter_list|,
name|ServiceCaller
argument_list|<
name|S
argument_list|,
name|R
argument_list|>
name|callable
parameter_list|,
name|byte
index|[]
name|row
parameter_list|)
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
parameter_list|<
name|S
parameter_list|,
name|R
parameter_list|>
name|CoprocessorServiceBuilder
argument_list|<
name|S
argument_list|,
name|R
argument_list|>
name|coprocessorService
parameter_list|(
name|Function
argument_list|<
name|RpcChannel
argument_list|,
name|S
argument_list|>
name|stubMaker
parameter_list|,
name|ServiceCaller
argument_list|<
name|S
argument_list|,
name|R
argument_list|>
name|callable
parameter_list|,
name|CoprocessorCallback
argument_list|<
name|R
argument_list|>
name|callback
parameter_list|)
block|{
return|return
literal|null
return|;
block|}
block|}
end_class

end_unit

