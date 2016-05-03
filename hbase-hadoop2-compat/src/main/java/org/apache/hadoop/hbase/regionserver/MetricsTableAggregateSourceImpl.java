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
name|regionserver
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|ConcurrentHashMap
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|Log
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|LogFactory
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
name|hbase
operator|.
name|metrics
operator|.
name|Interns
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
name|MetricsCollector
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
name|MetricsRecordBuilder
import|;
end_import

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|MetricsTableAggregateSourceImpl
extends|extends
name|BaseSourceImpl
implements|implements
name|MetricsTableAggregateSource
block|{
specifier|private
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|MetricsTableAggregateSourceImpl
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|ConcurrentHashMap
argument_list|<
name|String
argument_list|,
name|MetricsTableSource
argument_list|>
name|tableSources
init|=
operator|new
name|ConcurrentHashMap
argument_list|<>
argument_list|()
decl_stmt|;
specifier|public
name|MetricsTableAggregateSourceImpl
parameter_list|()
block|{
name|this
argument_list|(
name|METRICS_NAME
argument_list|,
name|METRICS_DESCRIPTION
argument_list|,
name|METRICS_CONTEXT
argument_list|,
name|METRICS_JMX_CONTEXT
argument_list|)
expr_stmt|;
block|}
specifier|public
name|MetricsTableAggregateSourceImpl
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
name|register
parameter_list|(
name|String
name|table
parameter_list|,
name|MetricsTableSource
name|source
parameter_list|)
block|{
name|tableSources
operator|.
name|put
argument_list|(
name|table
argument_list|,
name|source
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|deregister
parameter_list|(
name|String
name|table
parameter_list|)
block|{
try|try
block|{
name|tableSources
operator|.
name|remove
argument_list|(
name|table
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
comment|// Ignored. If this errors out it means that someone is double
comment|// closing the region source and the region is already nulled out.
name|LOG
operator|.
name|info
argument_list|(
literal|"Error trying to remove "
operator|+
name|table
operator|+
literal|" from "
operator|+
name|this
operator|.
name|getClass
argument_list|()
operator|.
name|getSimpleName
argument_list|()
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Yes this is a get function that doesn't return anything.  Thanks Hadoop for breaking all    * expectations of java programmers.  Instead of returning anything Hadoop metrics expects    * getMetrics to push the metrics into the collector.    *    * @param collector the collector    * @param all       get all the metrics regardless of when they last changed.    */
annotation|@
name|Override
specifier|public
name|void
name|getMetrics
parameter_list|(
name|MetricsCollector
name|collector
parameter_list|,
name|boolean
name|all
parameter_list|)
block|{
name|MetricsRecordBuilder
name|mrb
init|=
name|collector
operator|.
name|addRecord
argument_list|(
name|metricsName
argument_list|)
decl_stmt|;
if|if
condition|(
name|tableSources
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|MetricsTableSource
name|tableMetricSource
range|:
name|tableSources
operator|.
name|values
argument_list|()
control|)
block|{
if|if
condition|(
name|tableMetricSource
operator|instanceof
name|MetricsTableSourceImpl
condition|)
block|{
operator|(
operator|(
name|MetricsTableSourceImpl
operator|)
name|tableMetricSource
operator|)
operator|.
name|snapshot
argument_list|(
name|mrb
argument_list|,
name|all
argument_list|)
expr_stmt|;
block|}
block|}
name|mrb
operator|.
name|addGauge
argument_list|(
name|Interns
operator|.
name|info
argument_list|(
name|NUM_TABLES
argument_list|,
name|NUMBER_OF_TABLES_DESC
argument_list|)
argument_list|,
name|tableSources
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|metricsRegistry
operator|.
name|snapshot
argument_list|(
name|mrb
argument_list|,
name|all
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

