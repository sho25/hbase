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
name|filter
operator|.
name|CompareFilter
operator|.
name|CompareOp
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
name|ReflectionUtils
import|;
end_import

begin_comment
comment|/**  * The implementation of AsyncTable. Based on {@link RawAsyncTable}.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
class|class
name|AsyncTableImpl
implements|implements
name|AsyncTable
block|{
specifier|private
specifier|final
name|RawAsyncTable
name|rawTable
decl_stmt|;
specifier|private
specifier|final
name|ExecutorService
name|pool
decl_stmt|;
specifier|private
specifier|final
name|long
name|defaultScannerMaxResultSize
decl_stmt|;
specifier|public
name|AsyncTableImpl
parameter_list|(
name|AsyncConnectionImpl
name|conn
parameter_list|,
name|TableName
name|tableName
parameter_list|,
name|ExecutorService
name|pool
parameter_list|)
block|{
name|this
operator|.
name|rawTable
operator|=
name|conn
operator|.
name|getRawTable
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
name|this
operator|.
name|pool
operator|=
name|pool
expr_stmt|;
name|this
operator|.
name|defaultScannerMaxResultSize
operator|=
name|conn
operator|.
name|connConf
operator|.
name|getScannerMaxResultSize
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|TableName
name|getName
parameter_list|()
block|{
return|return
name|rawTable
operator|.
name|getName
argument_list|()
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
name|rawTable
operator|.
name|getConfiguration
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setReadRpcTimeout
parameter_list|(
name|long
name|timeout
parameter_list|,
name|TimeUnit
name|unit
parameter_list|)
block|{
name|rawTable
operator|.
name|setReadRpcTimeout
argument_list|(
name|timeout
argument_list|,
name|unit
argument_list|)
expr_stmt|;
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
name|rawTable
operator|.
name|getReadRpcTimeout
argument_list|(
name|unit
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setWriteRpcTimeout
parameter_list|(
name|long
name|timeout
parameter_list|,
name|TimeUnit
name|unit
parameter_list|)
block|{
name|rawTable
operator|.
name|setWriteRpcTimeout
argument_list|(
name|timeout
argument_list|,
name|unit
argument_list|)
expr_stmt|;
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
name|rawTable
operator|.
name|getWriteRpcTimeout
argument_list|(
name|unit
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setOperationTimeout
parameter_list|(
name|long
name|timeout
parameter_list|,
name|TimeUnit
name|unit
parameter_list|)
block|{
name|rawTable
operator|.
name|setOperationTimeout
argument_list|(
name|timeout
argument_list|,
name|unit
argument_list|)
expr_stmt|;
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
name|rawTable
operator|.
name|getOperationTimeout
argument_list|(
name|unit
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setScanTimeout
parameter_list|(
name|long
name|timeout
parameter_list|,
name|TimeUnit
name|unit
parameter_list|)
block|{
name|rawTable
operator|.
name|setScanTimeout
argument_list|(
name|timeout
argument_list|,
name|unit
argument_list|)
expr_stmt|;
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
name|rawTable
operator|.
name|getScanTimeout
argument_list|(
name|unit
argument_list|)
return|;
block|}
specifier|private
parameter_list|<
name|T
parameter_list|>
name|CompletableFuture
argument_list|<
name|T
argument_list|>
name|wrap
parameter_list|(
name|CompletableFuture
argument_list|<
name|T
argument_list|>
name|future
parameter_list|)
block|{
name|CompletableFuture
argument_list|<
name|T
argument_list|>
name|asyncFuture
init|=
operator|new
name|CompletableFuture
argument_list|<>
argument_list|()
decl_stmt|;
name|future
operator|.
name|whenCompleteAsync
argument_list|(
parameter_list|(
name|r
parameter_list|,
name|e
parameter_list|)
lambda|->
block|{
if|if
condition|(
name|e
operator|!=
literal|null
condition|)
block|{
name|asyncFuture
operator|.
name|completeExceptionally
argument_list|(
name|e
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|asyncFuture
operator|.
name|complete
argument_list|(
name|r
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|,
name|pool
argument_list|)
expr_stmt|;
return|return
name|asyncFuture
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
name|wrap
argument_list|(
name|rawTable
operator|.
name|get
argument_list|(
name|get
argument_list|)
argument_list|)
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
name|wrap
argument_list|(
name|rawTable
operator|.
name|put
argument_list|(
name|put
argument_list|)
argument_list|)
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
name|wrap
argument_list|(
name|rawTable
operator|.
name|delete
argument_list|(
name|delete
argument_list|)
argument_list|)
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
name|wrap
argument_list|(
name|rawTable
operator|.
name|append
argument_list|(
name|append
argument_list|)
argument_list|)
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
name|wrap
argument_list|(
name|rawTable
operator|.
name|increment
argument_list|(
name|increment
argument_list|)
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|CompletableFuture
argument_list|<
name|Boolean
argument_list|>
name|checkAndPut
parameter_list|(
name|byte
index|[]
name|row
parameter_list|,
name|byte
index|[]
name|family
parameter_list|,
name|byte
index|[]
name|qualifier
parameter_list|,
name|CompareOp
name|compareOp
parameter_list|,
name|byte
index|[]
name|value
parameter_list|,
name|Put
name|put
parameter_list|)
block|{
return|return
name|wrap
argument_list|(
name|rawTable
operator|.
name|checkAndPut
argument_list|(
name|row
argument_list|,
name|family
argument_list|,
name|qualifier
argument_list|,
name|compareOp
argument_list|,
name|value
argument_list|,
name|put
argument_list|)
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|CompletableFuture
argument_list|<
name|Boolean
argument_list|>
name|checkAndDelete
parameter_list|(
name|byte
index|[]
name|row
parameter_list|,
name|byte
index|[]
name|family
parameter_list|,
name|byte
index|[]
name|qualifier
parameter_list|,
name|CompareOp
name|compareOp
parameter_list|,
name|byte
index|[]
name|value
parameter_list|,
name|Delete
name|delete
parameter_list|)
block|{
return|return
name|wrap
argument_list|(
name|rawTable
operator|.
name|checkAndDelete
argument_list|(
name|row
argument_list|,
name|family
argument_list|,
name|qualifier
argument_list|,
name|compareOp
argument_list|,
name|value
argument_list|,
name|delete
argument_list|)
argument_list|)
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
name|wrap
argument_list|(
name|rawTable
operator|.
name|mutateRow
argument_list|(
name|mutation
argument_list|)
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|CompletableFuture
argument_list|<
name|Boolean
argument_list|>
name|checkAndMutate
parameter_list|(
name|byte
index|[]
name|row
parameter_list|,
name|byte
index|[]
name|family
parameter_list|,
name|byte
index|[]
name|qualifier
parameter_list|,
name|CompareOp
name|compareOp
parameter_list|,
name|byte
index|[]
name|value
parameter_list|,
name|RowMutations
name|mutation
parameter_list|)
block|{
return|return
name|wrap
argument_list|(
name|rawTable
operator|.
name|checkAndMutate
argument_list|(
name|row
argument_list|,
name|family
argument_list|,
name|qualifier
argument_list|,
name|compareOp
argument_list|,
name|value
argument_list|,
name|mutation
argument_list|)
argument_list|)
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
name|smallScan
parameter_list|(
name|Scan
name|scan
parameter_list|,
name|int
name|limit
parameter_list|)
block|{
return|return
name|wrap
argument_list|(
name|rawTable
operator|.
name|smallScan
argument_list|(
name|scan
argument_list|,
name|limit
argument_list|)
argument_list|)
return|;
block|}
specifier|private
name|long
name|resultSize2CacheSize
parameter_list|(
name|long
name|maxResultSize
parameter_list|)
block|{
comment|// * 2 if possible
return|return
name|maxResultSize
operator|>
name|Long
operator|.
name|MAX_VALUE
operator|/
literal|2
condition|?
name|maxResultSize
else|:
name|maxResultSize
operator|*
literal|2
return|;
block|}
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
operator|new
name|AsyncTableResultScanner
argument_list|(
name|rawTable
argument_list|,
name|ReflectionUtils
operator|.
name|newInstance
argument_list|(
name|scan
operator|.
name|getClass
argument_list|()
argument_list|,
name|scan
argument_list|)
argument_list|,
name|resultSize2CacheSize
argument_list|(
name|scan
operator|.
name|getMaxResultSize
argument_list|()
operator|>
literal|0
condition|?
name|scan
operator|.
name|getMaxResultSize
argument_list|()
else|:
name|defaultScannerMaxResultSize
argument_list|)
argument_list|)
return|;
block|}
specifier|private
name|void
name|scan0
parameter_list|(
name|Scan
name|scan
parameter_list|,
name|ScanResultConsumer
name|consumer
parameter_list|)
block|{
try|try
init|(
name|ResultScanner
name|scanner
init|=
name|getScanner
argument_list|(
name|scan
argument_list|)
init|)
block|{
for|for
control|(
name|Result
name|result
init|;
operator|(
name|result
operator|=
name|scanner
operator|.
name|next
argument_list|()
operator|)
operator|!=
literal|null
condition|;
control|)
block|{
if|if
condition|(
operator|!
name|consumer
operator|.
name|onNext
argument_list|(
name|result
argument_list|)
condition|)
block|{
break|break;
block|}
block|}
name|consumer
operator|.
name|onComplete
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|consumer
operator|.
name|onError
argument_list|(
name|e
argument_list|)
expr_stmt|;
block|}
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
name|ScanResultConsumer
name|consumer
parameter_list|)
block|{
name|pool
operator|.
name|execute
argument_list|(
parameter_list|()
lambda|->
name|scan0
argument_list|(
name|scan
argument_list|,
name|consumer
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

