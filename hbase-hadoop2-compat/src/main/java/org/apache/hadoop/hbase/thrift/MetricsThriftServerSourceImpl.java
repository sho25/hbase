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
name|thrift
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
name|MutableGaugeLong
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
name|MutableStat
import|;
end_import

begin_comment
comment|/**  * Hadoop 2 version of MetricsThriftServerSource{@link org.apache.hadoop.hbase.thrift.MetricsThriftServerSource}  *  * Implements BaseSource through BaseSourceImpl, following the pattern  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|MetricsThriftServerSourceImpl
extends|extends
name|BaseSourceImpl
implements|implements
name|MetricsThriftServerSource
block|{
specifier|private
name|MutableStat
name|batchGetStat
decl_stmt|;
specifier|private
name|MutableStat
name|batchMutateStat
decl_stmt|;
specifier|private
name|MutableStat
name|queueTimeStat
decl_stmt|;
specifier|private
name|MutableStat
name|thriftCallStat
decl_stmt|;
specifier|private
name|MutableStat
name|thriftSlowCallStat
decl_stmt|;
specifier|private
name|MutableGaugeLong
name|callQueueLenGauge
decl_stmt|;
specifier|public
name|MetricsThriftServerSourceImpl
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
name|batchGetStat
operator|=
name|getMetricsRegistry
argument_list|()
operator|.
name|newStat
argument_list|(
name|BATCH_GET_KEY
argument_list|,
literal|""
argument_list|,
literal|"Keys"
argument_list|,
literal|"Ops"
argument_list|)
expr_stmt|;
name|batchMutateStat
operator|=
name|getMetricsRegistry
argument_list|()
operator|.
name|newStat
argument_list|(
name|BATCH_MUTATE_KEY
argument_list|,
literal|""
argument_list|,
literal|"Keys"
argument_list|,
literal|"Ops"
argument_list|)
expr_stmt|;
name|queueTimeStat
operator|=
name|getMetricsRegistry
argument_list|()
operator|.
name|newRate
argument_list|(
name|TIME_IN_QUEUE_KEY
argument_list|)
expr_stmt|;
name|thriftCallStat
operator|=
name|getMetricsRegistry
argument_list|()
operator|.
name|newRate
argument_list|(
name|THRIFT_CALL_KEY
argument_list|)
expr_stmt|;
name|thriftSlowCallStat
operator|=
name|getMetricsRegistry
argument_list|()
operator|.
name|newRate
argument_list|(
name|SLOW_THRIFT_CALL_KEY
argument_list|)
expr_stmt|;
name|callQueueLenGauge
operator|=
name|getMetricsRegistry
argument_list|()
operator|.
name|getLongGauge
argument_list|(
name|CALL_QUEUE_LEN_KEY
argument_list|,
literal|0
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|incTimeInQueue
parameter_list|(
name|long
name|time
parameter_list|)
block|{
name|queueTimeStat
operator|.
name|add
argument_list|(
name|time
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setCallQueueLen
parameter_list|(
name|int
name|len
parameter_list|)
block|{
name|callQueueLenGauge
operator|.
name|set
argument_list|(
name|len
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|incNumRowKeysInBatchGet
parameter_list|(
name|int
name|diff
parameter_list|)
block|{
name|batchGetStat
operator|.
name|add
argument_list|(
name|diff
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|incNumRowKeysInBatchMutate
parameter_list|(
name|int
name|diff
parameter_list|)
block|{
name|batchMutateStat
operator|.
name|add
argument_list|(
name|diff
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|incMethodTime
parameter_list|(
name|String
name|name
parameter_list|,
name|long
name|time
parameter_list|)
block|{
name|MutableStat
name|s
init|=
name|getMetricsRegistry
argument_list|()
operator|.
name|newRate
argument_list|(
name|name
argument_list|)
decl_stmt|;
name|s
operator|.
name|add
argument_list|(
name|time
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|incCall
parameter_list|(
name|long
name|time
parameter_list|)
block|{
name|thriftCallStat
operator|.
name|add
argument_list|(
name|time
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|incSlowCall
parameter_list|(
name|long
name|time
parameter_list|)
block|{
name|thriftSlowCallStat
operator|.
name|add
argument_list|(
name|time
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

