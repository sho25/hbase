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
name|javax
operator|.
name|management
operator|.
name|ObjectName
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
name|util
operator|.
name|MBeans
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
comment|/**  * Hadoop2 metrics2 implementation of an object that registers MBeans.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|MBeanSourceImpl
implements|implements
name|MBeanSource
block|{
comment|/**    * Register an mbean with the underlying metrics system    * @param serviceName Metrics service/system name    * @param metricsName name of the metrics obejct to expose    * @param theMbean the actual MBean    * @return ObjectName from jmx    */
annotation|@
name|Override
specifier|public
name|ObjectName
name|register
parameter_list|(
name|String
name|serviceName
parameter_list|,
name|String
name|metricsName
parameter_list|,
name|Object
name|theMbean
parameter_list|)
block|{
return|return
name|MBeans
operator|.
name|register
argument_list|(
name|serviceName
argument_list|,
name|metricsName
argument_list|,
name|theMbean
argument_list|)
return|;
block|}
block|}
end_class

end_unit

