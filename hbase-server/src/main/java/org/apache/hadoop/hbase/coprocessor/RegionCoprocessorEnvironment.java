begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|ConcurrentMap
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
name|HBaseInterfaceAudience
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
name|client
operator|.
name|RegionInfo
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
name|CoprocessorRegionServerServices
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
name|Region
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
name|InterfaceStability
import|;
end_import

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|LimitedPrivate
argument_list|(
name|HBaseInterfaceAudience
operator|.
name|COPROC
argument_list|)
annotation|@
name|InterfaceStability
operator|.
name|Evolving
specifier|public
interface|interface
name|RegionCoprocessorEnvironment
extends|extends
name|CoprocessorEnvironment
block|{
comment|/** @return the region associated with this coprocessor */
name|Region
name|getRegion
parameter_list|()
function_decl|;
comment|/** @return region information for the region this coprocessor is running on */
name|RegionInfo
name|getRegionInfo
parameter_list|()
function_decl|;
comment|/** @return reference to the region server services */
name|CoprocessorRegionServerServices
name|getCoprocessorRegionServerServices
parameter_list|()
function_decl|;
comment|/** @return shared data between all instances of this coprocessor */
name|ConcurrentMap
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|getSharedData
parameter_list|()
function_decl|;
comment|/**    * Returns a MetricRegistry that can be used to track metrics at the region server level. All    * metrics tracked at this level will be shared by all the coprocessor instances    * of the same class in the same region server process. Note that there will be one    * region coprocessor environment per region in the server, but all of these instances will share    * the same MetricRegistry. The metric instances (like Counter, Timer, etc) will also be shared    * among all of the region coprocessor instances.    *    *<p>See ExampleRegionObserverWithMetrics class in the hbase-examples modules to see examples of how    * metrics can be instantiated and used.</p>    * @return A MetricRegistry for the coprocessor class to track and export metrics.    */
comment|// Note: we are not exposing getMetricRegistryForRegion(). per-region metrics are already costly
comment|// so we do not want to allow coprocessors to export metrics at the region level. We can allow
comment|// getMetricRegistryForTable() to allow coprocessors to track metrics per-table, per-regionserver.
name|MetricRegistry
name|getMetricRegistryForRegionServer
parameter_list|()
function_decl|;
block|}
end_interface

end_unit

