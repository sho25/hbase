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
name|zookeeper
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
name|classification
operator|.
name|InterfaceStability
import|;
end_import

begin_comment
comment|/**  * Base class for internal listeners of ZooKeeper events.  *  * The {@link ZooKeeperWatcher} for a process will execute the appropriate  * methods of implementations of this class.  In order to receive events from  * the watcher, every listener must register itself via {@link ZooKeeperWatcher#registerListener}.  *  * Subclasses need only override those methods in which they are interested.  *  * Note that the watcher will be blocked when invoking methods in listeners so  * they must not be long-running.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Public
annotation|@
name|InterfaceStability
operator|.
name|Evolving
specifier|public
specifier|abstract
class|class
name|ZooKeeperListener
block|{
comment|// Reference to the zk watcher which also contains configuration and constants
specifier|protected
name|ZooKeeperWatcher
name|watcher
decl_stmt|;
comment|/**    * Construct a ZooKeeper event listener.    */
specifier|public
name|ZooKeeperListener
parameter_list|(
name|ZooKeeperWatcher
name|watcher
parameter_list|)
block|{
name|this
operator|.
name|watcher
operator|=
name|watcher
expr_stmt|;
block|}
comment|/**    * Called when a new node has been created.    * @param path full path of the new node    */
specifier|public
name|void
name|nodeCreated
parameter_list|(
name|String
name|path
parameter_list|)
block|{
comment|// no-op
block|}
comment|/**    * Called when a node has been deleted    * @param path full path of the deleted node    */
specifier|public
name|void
name|nodeDeleted
parameter_list|(
name|String
name|path
parameter_list|)
block|{
comment|// no-op
block|}
comment|/**    * Called when an existing node has changed data.    * @param path full path of the updated node    */
specifier|public
name|void
name|nodeDataChanged
parameter_list|(
name|String
name|path
parameter_list|)
block|{
comment|// no-op
block|}
comment|/**    * Called when an existing node has a child node added or removed.    * @param path full path of the node whose children have changed    */
specifier|public
name|void
name|nodeChildrenChanged
parameter_list|(
name|String
name|path
parameter_list|)
block|{
comment|// no-op
block|}
block|}
end_class

end_unit

