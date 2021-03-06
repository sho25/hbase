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

begin_class
specifier|final
class|class
name|SimpleScanResultConsumer
implements|implements
name|ScanResultConsumer
block|{
specifier|private
name|ScanMetrics
name|scanMetrics
decl_stmt|;
specifier|private
specifier|final
name|List
argument_list|<
name|Result
argument_list|>
name|results
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
specifier|private
name|Throwable
name|error
decl_stmt|;
specifier|private
name|boolean
name|finished
init|=
literal|false
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
name|boolean
name|onNext
parameter_list|(
name|Result
name|result
parameter_list|)
block|{
name|results
operator|.
name|add
argument_list|(
name|result
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
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
name|finished
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
name|List
argument_list|<
name|Result
argument_list|>
name|getAll
parameter_list|()
throws|throws
name|Exception
block|{
while|while
condition|(
operator|!
name|finished
condition|)
block|{
name|wait
argument_list|()
expr_stmt|;
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
name|Exception
operator|.
name|class
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|Exception
argument_list|(
name|error
argument_list|)
throw|;
block|}
return|return
name|results
return|;
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

