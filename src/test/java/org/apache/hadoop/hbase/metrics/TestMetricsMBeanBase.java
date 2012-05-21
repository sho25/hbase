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
name|MBeanAttributeInfo
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
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|SmallTests
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
name|MetricsContext
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
name|MetricsUtil
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
name|MetricsIntValue
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
name|metrics
operator|.
name|util
operator|.
name|MetricsTimeVaryingRate
import|;
end_import

begin_import
import|import
name|junit
operator|.
name|framework
operator|.
name|TestCase
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|experimental
operator|.
name|categories
operator|.
name|Category
import|;
end_import

begin_class
annotation|@
name|Category
argument_list|(
name|SmallTests
operator|.
name|class
argument_list|)
specifier|public
class|class
name|TestMetricsMBeanBase
extends|extends
name|TestCase
block|{
specifier|private
class|class
name|TestStatistics
extends|extends
name|MetricsMBeanBase
block|{
specifier|public
name|TestStatistics
parameter_list|(
name|MetricsRegistry
name|registry
parameter_list|)
block|{
name|super
argument_list|(
name|registry
argument_list|,
literal|"TestStatistics"
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|MetricsRegistry
name|registry
decl_stmt|;
specifier|private
name|MetricsRecord
name|metricsRecord
decl_stmt|;
specifier|private
name|TestStatistics
name|stats
decl_stmt|;
specifier|private
name|MetricsRate
name|metricsRate
decl_stmt|;
specifier|private
name|MetricsIntValue
name|intValue
decl_stmt|;
specifier|private
name|MetricsTimeVaryingRate
name|varyRate
decl_stmt|;
specifier|public
name|void
name|setUp
parameter_list|()
block|{
name|this
operator|.
name|registry
operator|=
operator|new
name|MetricsRegistry
argument_list|()
expr_stmt|;
name|this
operator|.
name|metricsRate
operator|=
operator|new
name|MetricsRate
argument_list|(
literal|"metricsRate"
argument_list|,
name|registry
argument_list|,
literal|"test"
argument_list|)
expr_stmt|;
name|this
operator|.
name|intValue
operator|=
operator|new
name|MetricsIntValue
argument_list|(
literal|"intValue"
argument_list|,
name|registry
argument_list|,
literal|"test"
argument_list|)
expr_stmt|;
name|this
operator|.
name|varyRate
operator|=
operator|new
name|MetricsTimeVaryingRate
argument_list|(
literal|"varyRate"
argument_list|,
name|registry
argument_list|,
literal|"test"
argument_list|)
expr_stmt|;
name|this
operator|.
name|stats
operator|=
operator|new
name|TestStatistics
argument_list|(
name|registry
argument_list|)
expr_stmt|;
name|MetricsContext
name|context
init|=
name|MetricsUtil
operator|.
name|getContext
argument_list|(
literal|"hbase"
argument_list|)
decl_stmt|;
name|this
operator|.
name|metricsRecord
operator|=
name|MetricsUtil
operator|.
name|createRecord
argument_list|(
name|context
argument_list|,
literal|"test"
argument_list|)
expr_stmt|;
name|this
operator|.
name|metricsRecord
operator|.
name|setTag
argument_list|(
literal|"TestStatistics"
argument_list|,
literal|"test"
argument_list|)
expr_stmt|;
comment|//context.registerUpdater(this);
block|}
specifier|public
name|void
name|tearDown
parameter_list|()
block|{    }
specifier|public
name|void
name|testGetAttribute
parameter_list|()
throws|throws
name|Exception
block|{
name|this
operator|.
name|metricsRate
operator|.
name|inc
argument_list|(
literal|2
argument_list|)
expr_stmt|;
name|Thread
operator|.
name|sleep
argument_list|(
literal|1000
argument_list|)
expr_stmt|;
name|this
operator|.
name|metricsRate
operator|.
name|pushMetric
argument_list|(
name|this
operator|.
name|metricsRecord
argument_list|)
expr_stmt|;
name|this
operator|.
name|intValue
operator|.
name|set
argument_list|(
literal|5
argument_list|)
expr_stmt|;
name|this
operator|.
name|intValue
operator|.
name|pushMetric
argument_list|(
name|this
operator|.
name|metricsRecord
argument_list|)
expr_stmt|;
name|this
operator|.
name|varyRate
operator|.
name|inc
argument_list|(
literal|10
argument_list|)
expr_stmt|;
name|this
operator|.
name|varyRate
operator|.
name|inc
argument_list|(
literal|50
argument_list|)
expr_stmt|;
name|this
operator|.
name|varyRate
operator|.
name|pushMetric
argument_list|(
name|this
operator|.
name|metricsRecord
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|2.0
argument_list|,
operator|(
name|Float
operator|)
name|this
operator|.
name|stats
operator|.
name|getAttribute
argument_list|(
literal|"metricsRate"
argument_list|)
argument_list|,
literal|0.005
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|5
argument_list|,
name|this
operator|.
name|stats
operator|.
name|getAttribute
argument_list|(
literal|"intValue"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|10L
argument_list|,
name|this
operator|.
name|stats
operator|.
name|getAttribute
argument_list|(
literal|"varyRateMinTime"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|50L
argument_list|,
name|this
operator|.
name|stats
operator|.
name|getAttribute
argument_list|(
literal|"varyRateMaxTime"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|30L
argument_list|,
name|this
operator|.
name|stats
operator|.
name|getAttribute
argument_list|(
literal|"varyRateAvgTime"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|this
operator|.
name|stats
operator|.
name|getAttribute
argument_list|(
literal|"varyRateNumOps"
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|testGetMBeanInfo
parameter_list|()
block|{
name|MBeanInfo
name|info
init|=
name|this
operator|.
name|stats
operator|.
name|getMBeanInfo
argument_list|()
decl_stmt|;
name|MBeanAttributeInfo
index|[]
name|attributes
init|=
name|info
operator|.
name|getAttributes
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|6
argument_list|,
name|attributes
operator|.
name|length
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|MBeanAttributeInfo
argument_list|>
name|attributeByName
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|MBeanAttributeInfo
argument_list|>
argument_list|(
name|attributes
operator|.
name|length
argument_list|)
decl_stmt|;
for|for
control|(
name|MBeanAttributeInfo
name|attr
range|:
name|attributes
control|)
name|attributeByName
operator|.
name|put
argument_list|(
name|attr
operator|.
name|getName
argument_list|()
argument_list|,
name|attr
argument_list|)
expr_stmt|;
name|assertAttribute
argument_list|(
name|attributeByName
operator|.
name|get
argument_list|(
literal|"metricsRate"
argument_list|)
argument_list|,
literal|"metricsRate"
argument_list|,
literal|"java.lang.Float"
argument_list|,
literal|"test"
argument_list|)
expr_stmt|;
name|assertAttribute
argument_list|(
name|attributeByName
operator|.
name|get
argument_list|(
literal|"intValue"
argument_list|)
argument_list|,
literal|"intValue"
argument_list|,
literal|"java.lang.Integer"
argument_list|,
literal|"test"
argument_list|)
expr_stmt|;
name|assertAttribute
argument_list|(
name|attributeByName
operator|.
name|get
argument_list|(
literal|"varyRateMinTime"
argument_list|)
argument_list|,
literal|"varyRateMinTime"
argument_list|,
literal|"java.lang.Long"
argument_list|,
literal|"test"
argument_list|)
expr_stmt|;
name|assertAttribute
argument_list|(
name|attributeByName
operator|.
name|get
argument_list|(
literal|"varyRateMaxTime"
argument_list|)
argument_list|,
literal|"varyRateMaxTime"
argument_list|,
literal|"java.lang.Long"
argument_list|,
literal|"test"
argument_list|)
expr_stmt|;
name|assertAttribute
argument_list|(
name|attributeByName
operator|.
name|get
argument_list|(
literal|"varyRateAvgTime"
argument_list|)
argument_list|,
literal|"varyRateAvgTime"
argument_list|,
literal|"java.lang.Long"
argument_list|,
literal|"test"
argument_list|)
expr_stmt|;
name|assertAttribute
argument_list|(
name|attributeByName
operator|.
name|get
argument_list|(
literal|"varyRateNumOps"
argument_list|)
argument_list|,
literal|"varyRateNumOps"
argument_list|,
literal|"java.lang.Integer"
argument_list|,
literal|"test"
argument_list|)
expr_stmt|;
block|}
specifier|protected
name|void
name|assertAttribute
parameter_list|(
name|MBeanAttributeInfo
name|attr
parameter_list|,
name|String
name|name
parameter_list|,
name|String
name|type
parameter_list|,
name|String
name|description
parameter_list|)
block|{
name|assertEquals
argument_list|(
name|attr
operator|.
name|getName
argument_list|()
argument_list|,
name|name
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|attr
operator|.
name|getType
argument_list|()
argument_list|,
name|type
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|attr
operator|.
name|getDescription
argument_list|()
argument_list|,
name|description
argument_list|)
expr_stmt|;
block|}
annotation|@
name|org
operator|.
name|junit
operator|.
name|Rule
specifier|public
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|ResourceCheckerJUnitRule
name|cu
init|=
operator|new
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|ResourceCheckerJUnitRule
argument_list|()
decl_stmt|;
block|}
end_class

end_unit

