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
name|Closeable
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|CompletableFuture
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
name|classification
operator|.
name|InterfaceAudience
import|;
end_import

begin_comment
comment|/**  * Implementations hold cluster information such as this cluster's id, location of hbase:meta, etc..  * All stuffs that may be related to zookeeper at client side are placed here.  *<p>  * Most methods are executed asynchronously except getClusterId. It will be executed synchronously  * and should be called only once when initialization.  *<p>  * Internal use only.  */
end_comment

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|Private
interface|interface
name|AsyncRegistry
extends|extends
name|Closeable
block|{
comment|/**    * Get the location of meta region.    */
name|CompletableFuture
argument_list|<
name|RegionLocations
argument_list|>
name|getMetaRegionLocation
parameter_list|()
function_decl|;
comment|/**    * Should only be called once.    *<p>    * The upper layer should store this value somewhere as it will not be change any more.    */
name|String
name|getClusterId
parameter_list|()
function_decl|;
comment|/**    * Get the number of 'running' regionservers.    */
name|CompletableFuture
argument_list|<
name|Integer
argument_list|>
name|getCurrentNrHRS
parameter_list|()
function_decl|;
comment|/**    * Get the address of HMaster.    */
name|CompletableFuture
argument_list|<
name|ServerName
argument_list|>
name|getMasterAddress
parameter_list|()
function_decl|;
comment|/**    * Get the info port of HMaster.    */
name|CompletableFuture
argument_list|<
name|Integer
argument_list|>
name|getMasterInfoPort
parameter_list|()
function_decl|;
comment|/**    * Closes this instance and releases any system resources associated with it    */
annotation|@
name|Override
name|void
name|close
parameter_list|()
function_decl|;
block|}
end_interface

end_unit

