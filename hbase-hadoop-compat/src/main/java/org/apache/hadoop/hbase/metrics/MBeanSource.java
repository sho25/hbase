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

begin_comment
comment|/**  *  Object that will register an mbean with the underlying metrics implementation.  */
end_comment

begin_interface
specifier|public
interface|interface
name|MBeanSource
block|{
comment|/**    * Register an mbean with the underlying metrics system    * @param serviceName Metrics service/system name    * @param metricsName name of the metrics object to expose    * @param theMbean the actual MBean    * @return ObjectName from jmx    */
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
function_decl|;
block|}
end_interface

end_unit

