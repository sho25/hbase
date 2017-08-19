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
name|yetus
operator|.
name|audience
operator|.
name|InterfaceAudience
import|;
end_import

begin_comment
comment|/**  * The replication listener interface can be implemented if a class needs to subscribe to events  * generated by the ReplicationTracker. These events include things like addition/deletion of peer  * clusters or failure of a local region server. To receive events, the class also needs to register  * itself with a Replication Tracker.  */
end_comment

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
interface|interface
name|ReplicationListener
block|{
comment|/**    * A region server has been removed from the local cluster    * @param regionServer the removed region server    */
specifier|public
name|void
name|regionServerRemoved
parameter_list|(
name|String
name|regionServer
parameter_list|)
function_decl|;
comment|/**    * A peer cluster has been removed (i.e. unregistered) from replication.    * @param peerId The peer id of the cluster that has been removed    */
specifier|public
name|void
name|peerRemoved
parameter_list|(
name|String
name|peerId
parameter_list|)
function_decl|;
comment|/**    * The list of registered peer clusters has changed.    * @param peerIds A list of all currently registered peer clusters    */
specifier|public
name|void
name|peerListChanged
parameter_list|(
name|List
argument_list|<
name|String
argument_list|>
name|peerIds
parameter_list|)
function_decl|;
block|}
end_interface

end_unit

