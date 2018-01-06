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
name|replication
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
name|conf
operator|.
name|Configuration
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
name|ZKWatcher
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
comment|/**  * Used to create replication storage(peer, queue) classes.  *<p>  * For now we only have zk based implementation.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
specifier|final
class|class
name|ReplicationStorageFactory
block|{
specifier|private
name|ReplicationStorageFactory
parameter_list|()
block|{   }
comment|/**    * Create a new {@link ReplicationPeerStorage}.    */
specifier|public
specifier|static
name|ReplicationPeerStorage
name|getReplicationPeerStorage
parameter_list|(
name|ZKWatcher
name|zk
parameter_list|,
name|Configuration
name|conf
parameter_list|)
block|{
return|return
operator|new
name|ZKReplicationPeerStorage
argument_list|(
name|zk
argument_list|,
name|conf
argument_list|)
return|;
block|}
comment|/**    * Create a new {@link ReplicationQueueStorage}.    */
specifier|public
specifier|static
name|ReplicationQueueStorage
name|getReplicationQueueStorage
parameter_list|(
name|ZKWatcher
name|zk
parameter_list|,
name|Configuration
name|conf
parameter_list|)
block|{
return|return
operator|new
name|ZKReplicationQueueStorage
argument_list|(
name|zk
argument_list|,
name|conf
argument_list|)
return|;
block|}
block|}
end_class

end_unit

