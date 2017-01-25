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
name|zookeeper
package|;
end_package

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|zookeeper
operator|.
name|WatchedEvent
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
name|Watcher
import|;
end_import

begin_comment
comment|/**  * Placeholder of a watcher which might be triggered before the instance is not yet created.  *<p>  * {@code ZooKeeper} starts its event thread within its constructor (and that is an anti-pattern),  * and the watcher passed to the constructor might be called back by the event thread  * before you get the instance of {@code ZooKeeper} from the constructor.  * If your watcher calls methods of {@code ZooKeeper},  * pass this placeholder to the constructor of the {@code ZooKeeper},  * create your watcher using the instance of {@code ZooKeeper},  * and then call the method {@code PendingWatcher.prepare}.  */
end_comment

begin_class
class|class
name|PendingWatcher
implements|implements
name|Watcher
block|{
specifier|private
specifier|final
name|InstancePending
argument_list|<
name|Watcher
argument_list|>
name|pending
init|=
operator|new
name|InstancePending
argument_list|<>
argument_list|()
decl_stmt|;
annotation|@
name|Override
specifier|public
name|void
name|process
parameter_list|(
name|WatchedEvent
name|event
parameter_list|)
block|{
name|pending
operator|.
name|get
argument_list|()
operator|.
name|process
argument_list|(
name|event
argument_list|)
expr_stmt|;
block|}
comment|/**    * Associates the substantial watcher of processing events.    * This method should be called once, and {@code watcher} should be non-null.    * This method is expected to call as soon as possible    * because the event processing, being invoked by the ZooKeeper event thread,    * is uninterruptibly blocked until this method is called.    */
name|void
name|prepare
parameter_list|(
name|Watcher
name|watcher
parameter_list|)
block|{
name|pending
operator|.
name|prepare
argument_list|(
name|watcher
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

