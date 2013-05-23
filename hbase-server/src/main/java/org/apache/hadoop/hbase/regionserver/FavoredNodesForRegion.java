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
name|regionserver
package|;
end_package

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|InetSocketAddress
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
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|classification
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
name|hadoop
operator|.
name|hbase
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|HBaseProtos
operator|.
name|ServerName
import|;
end_import

begin_comment
comment|/**  * Abstraction that allows different modules in RegionServer to update/get  * the favored nodes information for regions.   */
end_comment

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|Private
interface|interface
name|FavoredNodesForRegion
block|{
comment|/**    * Used to update the favored nodes mapping when required.    * @param encodedRegionName    * @param favoredNodes    */
name|void
name|updateRegionFavoredNodesMapping
parameter_list|(
name|String
name|encodedRegionName
parameter_list|,
name|List
argument_list|<
name|ServerName
argument_list|>
name|favoredNodes
parameter_list|)
function_decl|;
comment|/**    * Get the favored nodes mapping for this region. Used when the HDFS create API    * is invoked to pass in favored nodes hints for new region files.    * @param encodedRegionName    * @return array containing the favored nodes' InetSocketAddresses    */
name|InetSocketAddress
index|[]
name|getFavoredNodesForRegion
parameter_list|(
name|String
name|encodedRegionName
parameter_list|)
function_decl|;
block|}
end_interface

end_unit

