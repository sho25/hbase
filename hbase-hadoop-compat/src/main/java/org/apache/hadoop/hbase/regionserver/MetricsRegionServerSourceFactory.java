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

begin_comment
comment|/**  * Interface of a factory to create Metrics Sources used inside of regionservers.  */
end_comment

begin_interface
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
comment|/**    * Create a MetricsRegionSource from a MetricsRegionWrapper.    *    * @param wrapper    * @return A metrics region source    */
name|MetricsRegionSource
name|createRegion
parameter_list|(
name|MetricsRegionWrapper
name|wrapper
parameter_list|)
function_decl|;
block|}
end_interface

end_unit

