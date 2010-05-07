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
name|metrics
package|;
end_package

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
name|HashMap
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
name|Map
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|management
operator|.
name|AttributeNotFoundException
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|management
operator|.
name|MBeanAttributeInfo
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|management
operator|.
name|MBeanException
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|management
operator|.
name|MBeanInfo
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|management
operator|.
name|ReflectionException
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

begin_comment
comment|/**  * Extends the Hadoop MetricsDynamicMBeanBase class to provide JMX support for  * custom HBase MetricsBase implementations.  MetricsDynamicMBeanBase ignores  * registered MetricsBase instance that are not instances of one of the  * org.apache.hadoop.metrics.util implementations.  *  */
end_comment

begin_class
specifier|public
class|class
name|MetricsMBeanBase
extends|extends
name|MetricsDynamicMBeanBase
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
specifier|protected
specifier|final
name|MetricsRegistry
name|registry
decl_stmt|;
specifier|protected
specifier|final
name|String
name|description
decl_stmt|;
specifier|protected
name|int
name|registryLength
decl_stmt|;
comment|/** HBase MetricsBase implementations that MetricsDynamicMBeanBase does    * not understand    */
specifier|protected
name|Map
argument_list|<
name|String
argument_list|,
name|MetricsBase
argument_list|>
name|extendedAttributes
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|MetricsBase
argument_list|>
argument_list|()
decl_stmt|;
specifier|protected
name|MBeanInfo
name|extendedInfo
decl_stmt|;
specifier|protected
name|MetricsMBeanBase
parameter_list|(
name|MetricsRegistry
name|mr
parameter_list|,
name|String
name|description
parameter_list|)
block|{
name|super
argument_list|(
name|copyMinusHBaseMetrics
argument_list|(
name|mr
argument_list|)
argument_list|,
name|description
argument_list|)
expr_stmt|;
name|this
operator|.
name|registry
operator|=
name|mr
expr_stmt|;
name|this
operator|.
name|description
operator|=
name|description
expr_stmt|;
name|this
operator|.
name|init
argument_list|()
expr_stmt|;
block|}
comment|/*    * @param mr MetricsRegistry.    * @return A copy of the passed MetricsRegistry minus the hbase metrics    */
specifier|private
specifier|static
name|MetricsRegistry
name|copyMinusHBaseMetrics
parameter_list|(
specifier|final
name|MetricsRegistry
name|mr
parameter_list|)
block|{
name|MetricsRegistry
name|copy
init|=
operator|new
name|MetricsRegistry
argument_list|()
decl_stmt|;
for|for
control|(
name|MetricsBase
name|metric
range|:
name|mr
operator|.
name|getMetricsList
argument_list|()
control|)
block|{
if|if
condition|(
name|metric
operator|instanceof
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
name|MetricsRate
condition|)
block|{
continue|continue;
block|}
name|copy
operator|.
name|add
argument_list|(
name|metric
operator|.
name|getName
argument_list|()
argument_list|,
name|metric
argument_list|)
expr_stmt|;
block|}
return|return
name|copy
return|;
block|}
specifier|protected
name|void
name|init
parameter_list|()
block|{
name|List
argument_list|<
name|MBeanAttributeInfo
argument_list|>
name|attributes
init|=
operator|new
name|ArrayList
argument_list|<
name|MBeanAttributeInfo
argument_list|>
argument_list|()
decl_stmt|;
name|MBeanInfo
name|parentInfo
init|=
name|super
operator|.
name|getMBeanInfo
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|parentAttributes
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|MBeanAttributeInfo
name|attr
range|:
name|parentInfo
operator|.
name|getAttributes
argument_list|()
control|)
block|{
name|attributes
operator|.
name|add
argument_list|(
name|attr
argument_list|)
expr_stmt|;
name|parentAttributes
operator|.
name|add
argument_list|(
name|attr
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|this
operator|.
name|registryLength
operator|=
name|this
operator|.
name|registry
operator|.
name|getMetricsList
argument_list|()
operator|.
name|size
argument_list|()
expr_stmt|;
for|for
control|(
name|MetricsBase
name|metric
range|:
name|this
operator|.
name|registry
operator|.
name|getMetricsList
argument_list|()
control|)
block|{
if|if
condition|(
name|metric
operator|.
name|getName
argument_list|()
operator|==
literal|null
operator|||
name|parentAttributes
operator|.
name|contains
argument_list|(
name|metric
operator|.
name|getName
argument_list|()
argument_list|)
condition|)
continue|continue;
comment|// add on custom HBase metric types
if|if
condition|(
name|metric
operator|instanceof
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
name|MetricsRate
condition|)
block|{
name|attributes
operator|.
name|add
argument_list|(
operator|new
name|MBeanAttributeInfo
argument_list|(
name|metric
operator|.
name|getName
argument_list|()
argument_list|,
literal|"java.lang.Float"
argument_list|,
name|metric
operator|.
name|getDescription
argument_list|()
argument_list|,
literal|true
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|extendedAttributes
operator|.
name|put
argument_list|(
name|metric
operator|.
name|getName
argument_list|()
argument_list|,
name|metric
argument_list|)
expr_stmt|;
block|}
comment|// else, its probably a hadoop metric already registered. Skip it.
block|}
name|this
operator|.
name|extendedInfo
operator|=
operator|new
name|MBeanInfo
argument_list|(
name|this
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|,
name|this
operator|.
name|description
argument_list|,
name|attributes
operator|.
name|toArray
argument_list|(
operator|new
name|MBeanAttributeInfo
index|[
literal|0
index|]
argument_list|)
argument_list|,
name|parentInfo
operator|.
name|getConstructors
argument_list|()
argument_list|,
name|parentInfo
operator|.
name|getOperations
argument_list|()
argument_list|,
name|parentInfo
operator|.
name|getNotifications
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|checkAndUpdateAttributes
parameter_list|()
block|{
if|if
condition|(
name|this
operator|.
name|registryLength
operator|!=
name|this
operator|.
name|registry
operator|.
name|getMetricsList
argument_list|()
operator|.
name|size
argument_list|()
condition|)
name|this
operator|.
name|init
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Object
name|getAttribute
parameter_list|(
name|String
name|name
parameter_list|)
throws|throws
name|AttributeNotFoundException
throws|,
name|MBeanException
throws|,
name|ReflectionException
block|{
if|if
condition|(
name|name
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Attribute name is NULL"
argument_list|)
throw|;
block|}
comment|/*      * Ugly.  Since MetricsDynamicMBeanBase implementation is private,      * we need to first check the parent class for the attribute.      * In case that the MetricsRegistry contents have changed, this will      * allow the parent to update it's internal structures (which we rely on      * to update our own.      */
try|try
block|{
return|return
name|super
operator|.
name|getAttribute
argument_list|(
name|name
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|AttributeNotFoundException
name|ex
parameter_list|)
block|{
name|checkAndUpdateAttributes
argument_list|()
expr_stmt|;
name|MetricsBase
name|metric
init|=
name|this
operator|.
name|extendedAttributes
operator|.
name|get
argument_list|(
name|name
argument_list|)
decl_stmt|;
if|if
condition|(
name|metric
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|metric
operator|instanceof
name|MetricsRate
condition|)
block|{
return|return
operator|(
operator|(
name|MetricsRate
operator|)
name|metric
operator|)
operator|.
name|getPreviousIntervalValue
argument_list|()
return|;
block|}
else|else
block|{
name|LOG
operator|.
name|warn
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"unknown metrics type %s for attribute %s"
argument_list|,
name|metric
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|,
name|name
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
throw|throw
operator|new
name|AttributeNotFoundException
argument_list|()
throw|;
block|}
annotation|@
name|Override
specifier|public
name|MBeanInfo
name|getMBeanInfo
parameter_list|()
block|{
return|return
name|this
operator|.
name|extendedInfo
return|;
block|}
block|}
end_class

end_unit

