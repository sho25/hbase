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
name|replication
package|;
end_package

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
name|hbase
operator|.
name|classification
operator|.
name|InterfaceAudience
import|;
end_import

begin_comment
comment|/**  * This is the interface for a Replication Tracker. A replication tracker provides the facility to  * subscribe and track events that reflect a change in replication state. These events are used by  * the ReplicationSourceManager to coordinate replication tasks such as addition/deletion of queues  * and queue failover. These events are defined in the ReplicationListener interface. If a class  * would like to listen to replication events it must implement the ReplicationListener interface  * and register itself with a Replication Tracker.  */
end_comment

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
interface|interface
name|ReplicationTracker
block|{
comment|/**    * Register a replication listener to receive replication events.    * @param listener    */
specifier|public
name|void
name|registerListener
parameter_list|(
name|ReplicationListener
name|listener
parameter_list|)
function_decl|;
specifier|public
name|void
name|removeListener
parameter_list|(
name|ReplicationListener
name|listener
parameter_list|)
function_decl|;
comment|/**    * Returns a list of other live region servers in the cluster.    * @return List of region servers.    */
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|getListOfRegionServers
parameter_list|()
function_decl|;
block|}
end_interface

end_unit

