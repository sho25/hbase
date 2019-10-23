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
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|conf
operator|.
name|Configuration
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

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|MetricsUserAggregateFactory
block|{
specifier|private
name|MetricsUserAggregateFactory
parameter_list|()
block|{    }
specifier|public
specifier|static
specifier|final
name|String
name|METRIC_USER_ENABLED_CONF
init|=
literal|"hbase.regionserver.user.metrics.enabled"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|boolean
name|DEFAULT_METRIC_USER_ENABLED_CONF
init|=
literal|true
decl_stmt|;
specifier|public
specifier|static
name|MetricsUserAggregate
name|getMetricsUserAggregate
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
if|if
condition|(
name|conf
operator|.
name|getBoolean
argument_list|(
name|METRIC_USER_ENABLED_CONF
argument_list|,
name|DEFAULT_METRIC_USER_ENABLED_CONF
argument_list|)
condition|)
block|{
return|return
operator|new
name|MetricsUserAggregateImpl
argument_list|(
name|conf
argument_list|)
return|;
block|}
else|else
block|{
comment|//NoOpMetricUserAggregate
return|return
operator|new
name|MetricsUserAggregate
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|updatePut
parameter_list|(
name|long
name|t
parameter_list|)
block|{          }
annotation|@
name|Override
specifier|public
name|void
name|updateDelete
parameter_list|(
name|long
name|t
parameter_list|)
block|{          }
annotation|@
name|Override
specifier|public
name|void
name|updateGet
parameter_list|(
name|long
name|t
parameter_list|)
block|{          }
annotation|@
name|Override
specifier|public
name|void
name|updateIncrement
parameter_list|(
name|long
name|t
parameter_list|)
block|{          }
annotation|@
name|Override
specifier|public
name|void
name|updateAppend
parameter_list|(
name|long
name|t
parameter_list|)
block|{          }
annotation|@
name|Override
specifier|public
name|void
name|updateReplay
parameter_list|(
name|long
name|t
parameter_list|)
block|{          }
annotation|@
name|Override
specifier|public
name|void
name|updateScanTime
parameter_list|(
name|long
name|t
parameter_list|)
block|{          }
block|}
return|;
block|}
block|}
block|}
end_class

end_unit

