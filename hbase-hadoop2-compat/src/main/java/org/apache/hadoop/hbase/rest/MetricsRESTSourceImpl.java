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
name|rest
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
name|metrics
operator|.
name|BaseSourceImpl
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
name|metrics2
operator|.
name|lib
operator|.
name|MutableCounterLong
import|;
end_import

begin_comment
comment|/**  * Hadoop Two implementation of a metrics2 source that will export metrics from the Rest server to  * the hadoop metrics2 subsystem.  *  * Implements BaseSource through BaseSourceImpl, following the pattern  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|MetricsRESTSourceImpl
extends|extends
name|BaseSourceImpl
implements|implements
name|MetricsRESTSource
block|{
specifier|private
name|MutableCounterLong
name|request
decl_stmt|;
specifier|private
name|MutableCounterLong
name|sucGet
decl_stmt|;
specifier|private
name|MutableCounterLong
name|sucPut
decl_stmt|;
specifier|private
name|MutableCounterLong
name|sucDel
decl_stmt|;
specifier|private
name|MutableCounterLong
name|sucScan
decl_stmt|;
specifier|private
name|MutableCounterLong
name|fGet
decl_stmt|;
specifier|private
name|MutableCounterLong
name|fPut
decl_stmt|;
specifier|private
name|MutableCounterLong
name|fDel
decl_stmt|;
specifier|private
name|MutableCounterLong
name|fScan
decl_stmt|;
specifier|public
name|MetricsRESTSourceImpl
parameter_list|()
block|{
name|this
argument_list|(
name|METRICS_NAME
argument_list|,
name|METRICS_DESCRIPTION
argument_list|,
name|CONTEXT
argument_list|,
name|JMX_CONTEXT
argument_list|)
expr_stmt|;
block|}
specifier|public
name|MetricsRESTSourceImpl
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
name|request
operator|=
name|getMetricsRegistry
argument_list|()
operator|.
name|getLongCounter
argument_list|(
name|REQUEST_KEY
argument_list|,
literal|0l
argument_list|)
expr_stmt|;
name|sucGet
operator|=
name|getMetricsRegistry
argument_list|()
operator|.
name|getLongCounter
argument_list|(
name|SUCCESSFUL_GET_KEY
argument_list|,
literal|0l
argument_list|)
expr_stmt|;
name|sucPut
operator|=
name|getMetricsRegistry
argument_list|()
operator|.
name|getLongCounter
argument_list|(
name|SUCCESSFUL_PUT_KEY
argument_list|,
literal|0l
argument_list|)
expr_stmt|;
name|sucDel
operator|=
name|getMetricsRegistry
argument_list|()
operator|.
name|getLongCounter
argument_list|(
name|SUCCESSFUL_DELETE_KEY
argument_list|,
literal|0l
argument_list|)
expr_stmt|;
name|sucScan
operator|=
name|getMetricsRegistry
argument_list|()
operator|.
name|getLongCounter
argument_list|(
name|SUCCESSFUL_SCAN_KEY
argument_list|,
literal|0L
argument_list|)
expr_stmt|;
name|fGet
operator|=
name|getMetricsRegistry
argument_list|()
operator|.
name|getLongCounter
argument_list|(
name|FAILED_GET_KEY
argument_list|,
literal|0l
argument_list|)
expr_stmt|;
name|fPut
operator|=
name|getMetricsRegistry
argument_list|()
operator|.
name|getLongCounter
argument_list|(
name|FAILED_PUT_KEY
argument_list|,
literal|0l
argument_list|)
expr_stmt|;
name|fDel
operator|=
name|getMetricsRegistry
argument_list|()
operator|.
name|getLongCounter
argument_list|(
name|FAILED_DELETE_KEY
argument_list|,
literal|0l
argument_list|)
expr_stmt|;
name|fScan
operator|=
name|getMetricsRegistry
argument_list|()
operator|.
name|getLongCounter
argument_list|(
name|FAILED_SCAN_KEY
argument_list|,
literal|0l
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|incrementRequests
parameter_list|(
name|int
name|inc
parameter_list|)
block|{
name|request
operator|.
name|incr
argument_list|(
name|inc
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|incrementSucessfulGetRequests
parameter_list|(
name|int
name|inc
parameter_list|)
block|{
name|sucGet
operator|.
name|incr
argument_list|(
name|inc
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|incrementSucessfulPutRequests
parameter_list|(
name|int
name|inc
parameter_list|)
block|{
name|sucPut
operator|.
name|incr
argument_list|(
name|inc
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|incrementSucessfulDeleteRequests
parameter_list|(
name|int
name|inc
parameter_list|)
block|{
name|sucDel
operator|.
name|incr
argument_list|(
name|inc
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|incrementFailedGetRequests
parameter_list|(
name|int
name|inc
parameter_list|)
block|{
name|fGet
operator|.
name|incr
argument_list|(
name|inc
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|incrementFailedPutRequests
parameter_list|(
name|int
name|inc
parameter_list|)
block|{
name|fPut
operator|.
name|incr
argument_list|(
name|inc
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|incrementFailedDeleteRequests
parameter_list|(
name|int
name|inc
parameter_list|)
block|{
name|fDel
operator|.
name|incr
argument_list|(
name|inc
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|incrementSucessfulScanRequests
parameter_list|(
name|int
name|inc
parameter_list|)
block|{
name|sucScan
operator|.
name|incr
argument_list|(
name|inc
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|incrementFailedScanRequests
parameter_list|(
name|int
name|inc
parameter_list|)
block|{
name|fScan
operator|.
name|incr
argument_list|(
name|inc
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

