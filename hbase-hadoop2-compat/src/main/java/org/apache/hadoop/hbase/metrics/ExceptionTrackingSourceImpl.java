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
name|metrics
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
name|metrics2
operator|.
name|lib
operator|.
name|MutableFastCounter
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

begin_comment
comment|/**  * Common base implementation for metrics sources which need to track exceptions thrown or  * received.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|ExceptionTrackingSourceImpl
extends|extends
name|BaseSourceImpl
implements|implements
name|ExceptionTrackingSource
block|{
specifier|protected
name|MutableFastCounter
name|exceptions
decl_stmt|;
specifier|protected
name|MutableFastCounter
name|exceptionsOOO
decl_stmt|;
specifier|protected
name|MutableFastCounter
name|exceptionsBusy
decl_stmt|;
specifier|protected
name|MutableFastCounter
name|exceptionsUnknown
decl_stmt|;
specifier|protected
name|MutableFastCounter
name|exceptionsScannerReset
decl_stmt|;
specifier|protected
name|MutableFastCounter
name|exceptionsSanity
decl_stmt|;
specifier|protected
name|MutableFastCounter
name|exceptionsNSRE
decl_stmt|;
specifier|protected
name|MutableFastCounter
name|exceptionsMoved
decl_stmt|;
specifier|protected
name|MutableFastCounter
name|exceptionsMultiTooLarge
decl_stmt|;
specifier|protected
name|MutableFastCounter
name|exceptionsCallQueueTooBig
decl_stmt|;
specifier|public
name|ExceptionTrackingSourceImpl
parameter_list|(
name|String
name|metricsName
parameter_list|,
name|String
name|metricsDescription
parameter_list|,
name|String
name|metricsContext
parameter_list|,
name|String
name|metricsJmxContext
parameter_list|)
block|{
name|super
argument_list|(
name|metricsName
argument_list|,
name|metricsDescription
argument_list|,
name|metricsContext
argument_list|,
name|metricsJmxContext
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|init
parameter_list|()
block|{
name|super
operator|.
name|init
argument_list|()
expr_stmt|;
name|this
operator|.
name|exceptions
operator|=
name|this
operator|.
name|getMetricsRegistry
argument_list|()
operator|.
name|newCounter
argument_list|(
name|EXCEPTIONS_NAME
argument_list|,
name|EXCEPTIONS_DESC
argument_list|,
literal|0L
argument_list|)
expr_stmt|;
name|this
operator|.
name|exceptionsOOO
operator|=
name|this
operator|.
name|getMetricsRegistry
argument_list|()
operator|.
name|newCounter
argument_list|(
name|EXCEPTIONS_OOO_NAME
argument_list|,
name|EXCEPTIONS_TYPE_DESC
argument_list|,
literal|0L
argument_list|)
expr_stmt|;
name|this
operator|.
name|exceptionsBusy
operator|=
name|this
operator|.
name|getMetricsRegistry
argument_list|()
operator|.
name|newCounter
argument_list|(
name|EXCEPTIONS_BUSY_NAME
argument_list|,
name|EXCEPTIONS_TYPE_DESC
argument_list|,
literal|0L
argument_list|)
expr_stmt|;
name|this
operator|.
name|exceptionsUnknown
operator|=
name|this
operator|.
name|getMetricsRegistry
argument_list|()
operator|.
name|newCounter
argument_list|(
name|EXCEPTIONS_UNKNOWN_NAME
argument_list|,
name|EXCEPTIONS_TYPE_DESC
argument_list|,
literal|0L
argument_list|)
expr_stmt|;
name|this
operator|.
name|exceptionsScannerReset
operator|=
name|this
operator|.
name|getMetricsRegistry
argument_list|()
operator|.
name|newCounter
argument_list|(
name|EXCEPTIONS_SCANNER_RESET_NAME
argument_list|,
name|EXCEPTIONS_TYPE_DESC
argument_list|,
literal|0L
argument_list|)
expr_stmt|;
name|this
operator|.
name|exceptionsSanity
operator|=
name|this
operator|.
name|getMetricsRegistry
argument_list|()
operator|.
name|newCounter
argument_list|(
name|EXCEPTIONS_SANITY_NAME
argument_list|,
name|EXCEPTIONS_TYPE_DESC
argument_list|,
literal|0L
argument_list|)
expr_stmt|;
name|this
operator|.
name|exceptionsMoved
operator|=
name|this
operator|.
name|getMetricsRegistry
argument_list|()
operator|.
name|newCounter
argument_list|(
name|EXCEPTIONS_MOVED_NAME
argument_list|,
name|EXCEPTIONS_TYPE_DESC
argument_list|,
literal|0L
argument_list|)
expr_stmt|;
name|this
operator|.
name|exceptionsNSRE
operator|=
name|this
operator|.
name|getMetricsRegistry
argument_list|()
operator|.
name|newCounter
argument_list|(
name|EXCEPTIONS_NSRE_NAME
argument_list|,
name|EXCEPTIONS_TYPE_DESC
argument_list|,
literal|0L
argument_list|)
expr_stmt|;
name|this
operator|.
name|exceptionsMultiTooLarge
operator|=
name|this
operator|.
name|getMetricsRegistry
argument_list|()
operator|.
name|newCounter
argument_list|(
name|EXCEPTIONS_MULTI_TOO_LARGE_NAME
argument_list|,
name|EXCEPTIONS_MULTI_TOO_LARGE_DESC
argument_list|,
literal|0L
argument_list|)
expr_stmt|;
name|this
operator|.
name|exceptionsCallQueueTooBig
operator|=
name|this
operator|.
name|getMetricsRegistry
argument_list|()
operator|.
name|newCounter
argument_list|(
name|EXCEPTIONS_CALL_QUEUE_TOO_BIG
argument_list|,
name|EXCEPTIONS_CALL_QUEUE_TOO_BIG_DESC
argument_list|,
literal|0L
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|exception
parameter_list|()
block|{
name|exceptions
operator|.
name|incr
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|outOfOrderException
parameter_list|()
block|{
name|exceptionsOOO
operator|.
name|incr
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|failedSanityException
parameter_list|()
block|{
name|exceptionsSanity
operator|.
name|incr
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|movedRegionException
parameter_list|()
block|{
name|exceptionsMoved
operator|.
name|incr
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|notServingRegionException
parameter_list|()
block|{
name|exceptionsNSRE
operator|.
name|incr
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|unknownScannerException
parameter_list|()
block|{
name|exceptionsUnknown
operator|.
name|incr
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|scannerResetException
parameter_list|()
block|{
name|exceptionsScannerReset
operator|.
name|incr
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|tooBusyException
parameter_list|()
block|{
name|exceptionsBusy
operator|.
name|incr
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|multiActionTooLargeException
parameter_list|()
block|{
name|exceptionsMultiTooLarge
operator|.
name|incr
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|callQueueTooBigException
parameter_list|()
block|{
name|exceptionsCallQueueTooBig
operator|.
name|incr
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

