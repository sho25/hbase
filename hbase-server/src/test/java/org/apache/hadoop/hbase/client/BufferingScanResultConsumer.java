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
name|base
operator|.
name|Throwables
import|;
end_import

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
name|ArrayDeque
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Queue
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
name|metrics
operator|.
name|ScanMetrics
import|;
end_import

begin_comment
comment|/**  * A scan result consumer which buffers all the data in memory and you can call the {@link #take()}  * method below to get the result one by one. Should only be used by tests, do not write production  * code like this as the buffer is unlimited and may cause OOM.  */
end_comment

begin_class
class|class
name|BufferingScanResultConsumer
implements|implements
name|AdvancedScanResultConsumer
block|{
specifier|private
name|ScanMetrics
name|scanMetrics
decl_stmt|;
specifier|private
specifier|final
name|Queue
argument_list|<
name|Result
argument_list|>
name|queue
init|=
operator|new
name|ArrayDeque
argument_list|<>
argument_list|()
decl_stmt|;
specifier|private
name|boolean
name|finished
decl_stmt|;
specifier|private
name|Throwable
name|error
decl_stmt|;
annotation|@
name|Override
specifier|public
name|void
name|onScanMetricsCreated
parameter_list|(
name|ScanMetrics
name|scanMetrics
parameter_list|)
block|{
name|this
operator|.
name|scanMetrics
operator|=
name|scanMetrics
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
specifier|synchronized
name|void
name|onNext
parameter_list|(
name|Result
index|[]
name|results
parameter_list|,
name|ScanController
name|controller
parameter_list|)
block|{
for|for
control|(
name|Result
name|result
range|:
name|results
control|)
block|{
name|queue
operator|.
name|offer
argument_list|(
name|result
argument_list|)
expr_stmt|;
block|}
name|notifyAll
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
specifier|synchronized
name|void
name|onError
parameter_list|(
name|Throwable
name|error
parameter_list|)
block|{
name|finished
operator|=
literal|true
expr_stmt|;
name|this
operator|.
name|error
operator|=
name|error
expr_stmt|;
name|notifyAll
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
specifier|synchronized
name|void
name|onComplete
parameter_list|()
block|{
name|finished
operator|=
literal|true
expr_stmt|;
name|notifyAll
argument_list|()
expr_stmt|;
block|}
specifier|public
specifier|synchronized
name|Result
name|take
parameter_list|()
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
for|for
control|(
init|;
condition|;
control|)
block|{
if|if
condition|(
operator|!
name|queue
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
return|return
name|queue
operator|.
name|poll
argument_list|()
return|;
block|}
if|if
condition|(
name|finished
condition|)
block|{
if|if
condition|(
name|error
operator|!=
literal|null
condition|)
block|{
name|Throwables
operator|.
name|propagateIfPossible
argument_list|(
name|error
argument_list|,
name|IOException
operator|.
name|class
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|IOException
argument_list|(
name|error
argument_list|)
throw|;
block|}
else|else
block|{
return|return
literal|null
return|;
block|}
block|}
name|wait
argument_list|()
expr_stmt|;
block|}
block|}
specifier|public
name|ScanMetrics
name|getScanMetrics
parameter_list|()
block|{
return|return
name|scanMetrics
return|;
block|}
block|}
end_class

end_unit
