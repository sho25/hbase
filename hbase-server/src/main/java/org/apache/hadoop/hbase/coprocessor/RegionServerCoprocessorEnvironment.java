begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|ServerName
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
name|Connection
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
name|OnlineRegions
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
name|RegionServerCoprocessorEnvironment
extends|extends
name|CoprocessorEnvironment
argument_list|<
name|RegionServerCoprocessor
argument_list|>
block|{
comment|/**    * @return Hosting Server's ServerName    */
name|ServerName
name|getServerName
parameter_list|()
function_decl|;
comment|/**    * @return Interface to Map of regions online on this RegionServer {@link #getServerName()}}.    */
name|OnlineRegions
name|getOnlineRegions
parameter_list|()
function_decl|;
comment|/**    * Be careful RPC'ing from a Coprocessor context.    * RPC's will fail, stall, retry, and/or crawl because the remote side is not online, is    * struggling or it is on the other side of a network partition. Any use of Connection from    * inside a Coprocessor must be able to handle all such hiccups.    *    *<p>Using a Connection to get at a local resource -- say a Region that is on the local    * Server or using Admin Interface from a Coprocessor hosted on the Master -- will result in a    * short-circuit of the RPC framework to make a direct invocation avoiding RPC (and    * protobuf marshalling/unmarshalling).    *    * @return The host's Connection to the Cluster.    */
name|Connection
name|getConnection
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

