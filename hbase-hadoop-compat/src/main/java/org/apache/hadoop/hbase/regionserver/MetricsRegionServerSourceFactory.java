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
name|hbase
operator|.
name|io
operator|.
name|MetricsIOSource
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
name|io
operator|.
name|MetricsIOWrapper
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
comment|/**  * Interface of a factory to create Metrics Sources used inside of regionservers.  */
end_comment

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
interface|interface
name|MetricsRegionServerSourceFactory
block|{
comment|/**    * Given a wrapper create a MetricsRegionServerSource.    *    * @param regionServerWrapper The wrapped region server    * @return a Metrics Source.    */
name|MetricsRegionServerSource
name|createServer
parameter_list|(
name|MetricsRegionServerWrapper
name|regionServerWrapper
parameter_list|)
function_decl|;
comment|/**    * Create a MetricsRegionSource from a MetricsRegionWrapper.    *    * @param wrapper The wrapped region    * @return A metrics region source    */
name|MetricsRegionSource
name|createRegion
parameter_list|(
name|MetricsRegionWrapper
name|wrapper
parameter_list|)
function_decl|;
comment|/**    * Create a MetricsTableSource from a MetricsTableWrapper.    *    * @param table The table name    * @param wrapper The wrapped table aggregate    * @return A metrics table source    */
name|MetricsTableSource
name|createTable
parameter_list|(
name|String
name|table
parameter_list|,
name|MetricsTableWrapperAggregate
name|wrapper
parameter_list|)
function_decl|;
comment|/**    * Get a MetricsTableAggregateSource    *    * @return A metrics table aggregate source    */
name|MetricsTableAggregateSource
name|getTableAggregate
parameter_list|()
function_decl|;
comment|/**    * Get a MetricsHeapMemoryManagerSource    * @return A metrics heap memory manager source    */
name|MetricsHeapMemoryManagerSource
name|getHeapMemoryManager
parameter_list|()
function_decl|;
comment|/**    * Create a MetricsIOSource from a MetricsIOWrapper.    *    * @return A metrics IO source    */
name|MetricsIOSource
name|createIO
parameter_list|(
name|MetricsIOWrapper
name|wrapper
parameter_list|)
function_decl|;
block|}
end_interface

end_unit

