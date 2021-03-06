begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|yetus
operator|.
name|audience
operator|.
name|InterfaceAudience
import|;
end_import

begin_comment
comment|/**  * Registry for meta information needed for connection setup to a HBase cluster. Implementations  * hold cluster information such as this cluster's id, location of hbase:meta, etc..  * Internal use only.  */
end_comment

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|Private
interface|interface
name|ConnectionRegistry
extends|extends
name|Closeable
block|{
comment|/**    * Get the location of meta region(s).    */
name|CompletableFuture
argument_list|<
name|RegionLocations
argument_list|>
name|getMetaRegionLocations
parameter_list|()
function_decl|;
comment|/**    * Should only be called once.    *<p>    * The upper layer should store this value somewhere as it will not be change any more.    */
name|CompletableFuture
argument_list|<
name|String
argument_list|>
name|getClusterId
parameter_list|()
function_decl|;
comment|/**    * Get the address of active HMaster.    */
name|CompletableFuture
argument_list|<
name|ServerName
argument_list|>
name|getActiveMaster
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

