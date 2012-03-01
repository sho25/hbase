begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2010 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|metrics
operator|.
name|util
operator|.
name|MBeanUtil
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
name|MetricsDynamicMBeanBase
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
name|javax
operator|.
name|management
operator|.
name|ObjectName
import|;
end_import

begin_comment
comment|/**  * Exports dynamic region server metric recorded in  * {@link RegionServerDynamicMetrics} as an MBean  * for JMX monitoring.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|RegionServerDynamicStatistics
extends|extends
name|MetricsDynamicMBeanBase
block|{
specifier|private
specifier|final
name|ObjectName
name|mbeanName
decl_stmt|;
specifier|public
name|RegionServerDynamicStatistics
parameter_list|(
name|MetricsRegistry
name|registry
parameter_list|)
block|{
name|super
argument_list|(
name|registry
argument_list|,
literal|"RegionServerDynamicStatistics"
argument_list|)
expr_stmt|;
name|mbeanName
operator|=
name|MBeanUtil
operator|.
name|registerMBean
argument_list|(
literal|"RegionServerDynamic"
argument_list|,
literal|"RegionServerDynamicStatistics"
argument_list|,
name|this
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|shutdown
parameter_list|()
block|{
if|if
condition|(
name|mbeanName
operator|!=
literal|null
condition|)
name|MBeanUtil
operator|.
name|unregisterMBean
argument_list|(
name|mbeanName
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

