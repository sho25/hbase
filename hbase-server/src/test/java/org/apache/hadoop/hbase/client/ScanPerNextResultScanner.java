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
name|io
operator|.
name|InterruptedIOException
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
name|Throwables
import|;
end_import

begin_comment
comment|/**  * A ResultScanner which will only send request to RS when there are no cached results when calling  * next, just like the ResultScanner in the old time. Mainly used for writing UTs, that we can  * control when to send request to RS. The default ResultScanner implementation will fetch in  * background.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|ScanPerNextResultScanner
implements|implements
name|ResultScanner
implements|,
name|AdvancedScanResultConsumer
block|{
specifier|private
specifier|final
name|AsyncTable
argument_list|<
name|AdvancedScanResultConsumer
argument_list|>
name|table
decl_stmt|;
specifier|private
specifier|final
name|Scan
name|scan
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
name|ScanMetrics
name|scanMetrics
decl_stmt|;
specifier|private
name|boolean
name|closed
init|=
literal|false
decl_stmt|;
specifier|private
name|Throwable
name|error
decl_stmt|;
specifier|private
name|ScanResumer
name|resumer
decl_stmt|;
specifier|public
name|ScanPerNextResultScanner
parameter_list|(
name|AsyncTable
argument_list|<
name|AdvancedScanResultConsumer
argument_list|>
name|table
parameter_list|,
name|Scan
name|scan
parameter_list|)
block|{
name|this
operator|.
name|table
operator|=
name|table
expr_stmt|;
name|this
operator|.
name|scan
operator|=
name|scan
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
name|closed
operator|=
literal|true
expr_stmt|;
name|notifyAll
argument_list|()
expr_stmt|;
block|}
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
assert|assert
name|results
operator|.
name|length
operator|>
literal|0
assert|;
if|if
condition|(
name|closed
condition|)
block|{
name|controller
operator|.
name|terminate
argument_list|()
expr_stmt|;
return|return;
block|}
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
name|add
argument_list|(
name|result
argument_list|)
expr_stmt|;
block|}
name|notifyAll
argument_list|()
expr_stmt|;
name|resumer
operator|=
name|controller
operator|.
name|suspend
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
specifier|synchronized
name|void
name|onHeartbeat
parameter_list|(
name|ScanController
name|controller
parameter_list|)
block|{
if|if
condition|(
name|closed
condition|)
block|{
name|controller
operator|.
name|terminate
argument_list|()
expr_stmt|;
return|return;
block|}
if|if
condition|(
name|scan
operator|.
name|isNeedCursorResult
argument_list|()
condition|)
block|{
name|controller
operator|.
name|cursor
argument_list|()
operator|.
name|ifPresent
argument_list|(
name|c
lambda|->
name|queue
operator|.
name|add
argument_list|(
name|Result
operator|.
name|createCursorResult
argument_list|(
name|c
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
specifier|synchronized
name|Result
name|next
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|queue
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
if|if
condition|(
name|resumer
operator|!=
literal|null
condition|)
block|{
name|resumer
operator|.
name|resume
argument_list|()
expr_stmt|;
name|resumer
operator|=
literal|null
expr_stmt|;
block|}
else|else
block|{
name|table
operator|.
name|scan
argument_list|(
name|scan
argument_list|,
name|this
argument_list|)
expr_stmt|;
block|}
block|}
while|while
condition|(
name|queue
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
if|if
condition|(
name|closed
condition|)
block|{
return|return
literal|null
return|;
block|}
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
try|try
block|{
name|wait
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|InterruptedIOException
argument_list|()
throw|;
block|}
block|}
return|return
name|queue
operator|.
name|poll
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
specifier|synchronized
name|void
name|close
parameter_list|()
block|{
name|closed
operator|=
literal|true
expr_stmt|;
name|queue
operator|.
name|clear
argument_list|()
expr_stmt|;
if|if
condition|(
name|resumer
operator|!=
literal|null
condition|)
block|{
name|resumer
operator|.
name|resume
argument_list|()
expr_stmt|;
name|resumer
operator|=
literal|null
expr_stmt|;
block|}
name|notifyAll
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|renewLease
parameter_list|()
block|{
comment|// The renew lease operation will be handled in background
return|return
literal|false
return|;
block|}
annotation|@
name|Override
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

