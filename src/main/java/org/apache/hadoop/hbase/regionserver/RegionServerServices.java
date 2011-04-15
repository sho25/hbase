begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2010 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|HServerInfo
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
name|catalog
operator|.
name|CatalogTracker
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
name|ipc
operator|.
name|HBaseRpcMetrics
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
name|wal
operator|.
name|HLog
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
name|zookeeper
operator|.
name|ZooKeeperWatcher
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|zookeeper
operator|.
name|KeeperException
import|;
end_import

begin_comment
comment|/**  * Services provided by {@link HRegionServer}  */
end_comment

begin_interface
specifier|public
interface|interface
name|RegionServerServices
extends|extends
name|OnlineRegions
block|{
comment|/**    * @return True if this regionserver is stopping.    */
specifier|public
name|boolean
name|isStopping
parameter_list|()
function_decl|;
comment|/** @return the HLog */
specifier|public
name|HLog
name|getWAL
parameter_list|()
function_decl|;
comment|/**    * @return Implementation of {@link CompactionRequestor} or null.    */
specifier|public
name|CompactionRequestor
name|getCompactionRequester
parameter_list|()
function_decl|;
comment|/**    * @return Implementation of {@link FlushRequester} or null.    */
specifier|public
name|FlushRequester
name|getFlushRequester
parameter_list|()
function_decl|;
comment|/**    * Return data structure that has Server address and startcode.    * @return The HServerInfo for this RegionServer.    */
specifier|public
name|HServerInfo
name|getServerInfo
parameter_list|()
function_decl|;
comment|/**    * @return the RegionServerAccounting for this Region Server    */
specifier|public
name|RegionServerAccounting
name|getRegionServerAccounting
parameter_list|()
function_decl|;
comment|/**    * Tasks to perform after region open to complete deploy of region on    * regionserver    * @param r Region to open.    * @param ct Instance of {@link CatalogTracker}    * @param daughter True if this is daughter of a split    * @throws KeeperException    * @throws IOException    */
specifier|public
name|void
name|postOpenDeployTasks
parameter_list|(
specifier|final
name|HRegion
name|r
parameter_list|,
specifier|final
name|CatalogTracker
name|ct
parameter_list|,
specifier|final
name|boolean
name|daughter
parameter_list|)
throws|throws
name|KeeperException
throws|,
name|IOException
function_decl|;
comment|/**    * Returns a reference to the RPC server metrics.    */
specifier|public
name|HBaseRpcMetrics
name|getRpcMetrics
parameter_list|()
function_decl|;
block|}
end_interface

end_unit

