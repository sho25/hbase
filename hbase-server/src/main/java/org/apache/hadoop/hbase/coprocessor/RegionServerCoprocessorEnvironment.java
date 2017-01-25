begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|coprocessor
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
name|hbase
operator|.
name|CoprocessorEnvironment
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
name|MetricRegistry
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
name|regionserver
operator|.
name|RegionServerServices
import|;
end_import

begin_interface
specifier|public
interface|interface
name|RegionServerCoprocessorEnvironment
extends|extends
name|CoprocessorEnvironment
block|{
comment|/**    * Gets the region server services.    *    * @return the region server services    */
name|RegionServerServices
name|getRegionServerServices
parameter_list|()
function_decl|;
comment|/**    * Returns a MetricRegistry that can be used to track metrics at the region server level.    *    *<p>See ExampleMasterObserverWithMetrics class in the hbase-examples modules for examples    * of how metrics can be instantiated and used.</p>    * @return A MetricRegistry for the coprocessor class to track and export metrics.    */
name|MetricRegistry
name|getMetricRegistryForRegionServer
parameter_list|()
function_decl|;
block|}
end_interface

end_unit

