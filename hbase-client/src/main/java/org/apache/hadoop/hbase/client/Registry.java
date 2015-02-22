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
name|client
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|IOException
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
name|RegionLocations
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
name|classification
operator|.
name|InterfaceAudience
import|;
end_import

begin_comment
comment|/**  * Cluster registry.  * Implementations hold cluster information such as this cluster's id, location of hbase:meta, etc.  * Internal use only.  */
end_comment

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|Private
interface|interface
name|Registry
block|{
comment|/**    * @param connection    */
name|void
name|init
parameter_list|(
name|Connection
name|connection
parameter_list|)
function_decl|;
comment|/**    * @return Meta region location    * @throws IOException    */
name|RegionLocations
name|getMetaRegionLocation
parameter_list|()
throws|throws
name|IOException
function_decl|;
comment|/**    * @return Cluster id.    */
name|String
name|getClusterId
parameter_list|()
function_decl|;
comment|/**    * @return Count of 'running' regionservers    * @throws IOException    */
name|int
name|getCurrentNrHRS
parameter_list|()
throws|throws
name|IOException
function_decl|;
block|}
end_interface

end_unit

