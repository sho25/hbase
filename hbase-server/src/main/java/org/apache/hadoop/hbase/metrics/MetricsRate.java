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
name|metrics
package|;
end_package

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
name|util
operator|.
name|EnvironmentEdgeManager
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
name|metrics
operator|.
name|MetricsRecord
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
name|metrics
operator|.
name|util
operator|.
name|MetricsBase
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
name|metrics
operator|.
name|util
operator|.
name|MetricsRegistry
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
name|util
operator|.
name|StringUtils
import|;
end_import

begin_comment
comment|/**  * Publishes a rate based on a counter - you increment the counter each  * time an event occurs (eg: an RPC call) and this publishes a rate.  */
end_comment

begin_class
annotation|@
name|Deprecated
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|MetricsRate
extends|extends
name|MetricsBase
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
literal|"org.apache.hadoop.hbase.metrics"
argument_list|)
decl_stmt|;
specifier|private
name|int
name|value
decl_stmt|;
specifier|private
name|float
name|prevRate
decl_stmt|;
specifier|private
name|long
name|ts
decl_stmt|;
specifier|public
name|MetricsRate
parameter_list|(
specifier|final
name|String
name|name
parameter_list|,
specifier|final
name|MetricsRegistry
name|registry
parameter_list|,
specifier|final
name|String
name|description
parameter_list|)
block|{
name|super
argument_list|(
name|name
argument_list|,
name|description
argument_list|)
expr_stmt|;
name|this
operator|.
name|value
operator|=
literal|0
expr_stmt|;
name|this
operator|.
name|prevRate
operator|=
literal|0
expr_stmt|;
name|this
operator|.
name|ts
operator|=
name|EnvironmentEdgeManager
operator|.
name|currentTimeMillis
argument_list|()
expr_stmt|;
name|registry
operator|.
name|add
argument_list|(
name|name
argument_list|,
name|this
argument_list|)
expr_stmt|;
block|}
specifier|public
name|MetricsRate
parameter_list|(
specifier|final
name|String
name|name
parameter_list|,
specifier|final
name|MetricsRegistry
name|registry
parameter_list|)
block|{
name|this
argument_list|(
name|name
argument_list|,
name|registry
argument_list|,
name|NO_DESCRIPTION
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|synchronized
name|void
name|inc
parameter_list|(
specifier|final
name|int
name|incr
parameter_list|)
block|{
name|value
operator|+=
name|incr
expr_stmt|;
block|}
specifier|public
specifier|synchronized
name|void
name|inc
parameter_list|()
block|{
name|value
operator|++
expr_stmt|;
block|}
specifier|public
specifier|synchronized
name|void
name|intervalHeartBeat
parameter_list|()
block|{
name|long
name|now
init|=
name|EnvironmentEdgeManager
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|long
name|diff
init|=
operator|(
name|now
operator|-
name|ts
operator|)
operator|/
literal|1000
decl_stmt|;
if|if
condition|(
name|diff
operator|<
literal|1
condition|)
block|{
comment|// To make sure our averages aren't skewed by fast repeated calls,
comment|// we simply ignore fast repeated calls.
return|return;
block|}
name|this
operator|.
name|prevRate
operator|=
operator|(
name|float
operator|)
name|value
operator|/
name|diff
expr_stmt|;
name|this
operator|.
name|value
operator|=
literal|0
expr_stmt|;
name|this
operator|.
name|ts
operator|=
name|now
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
specifier|synchronized
name|void
name|pushMetric
parameter_list|(
specifier|final
name|MetricsRecord
name|mr
parameter_list|)
block|{
name|intervalHeartBeat
argument_list|()
expr_stmt|;
try|try
block|{
name|mr
operator|.
name|setMetric
argument_list|(
name|getName
argument_list|()
argument_list|,
name|getPreviousIntervalValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"pushMetric failed for "
operator|+
name|getName
argument_list|()
operator|+
literal|"\n"
operator|+
name|StringUtils
operator|.
name|stringifyException
argument_list|(
name|e
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
specifier|synchronized
name|float
name|getPreviousIntervalValue
parameter_list|()
block|{
return|return
name|this
operator|.
name|prevRate
return|;
block|}
block|}
end_class

end_unit

