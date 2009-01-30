begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2009 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
comment|/**  * Place-holder Watcher.  * Does nothing currently.  */
end_comment

begin_class
specifier|public
class|class
name|WatcherWrapper
implements|implements
name|Watcher
block|{
specifier|private
specifier|final
name|Watcher
name|otherWatcher
decl_stmt|;
comment|/**    * Construct with a Watcher to pass events to.    * @param otherWatcher Watcher to pass events to.    */
specifier|public
name|WatcherWrapper
parameter_list|(
name|Watcher
name|otherWatcher
parameter_list|)
block|{
name|this
operator|.
name|otherWatcher
operator|=
name|otherWatcher
expr_stmt|;
block|}
comment|/**    * @param event WatchedEvent from ZooKeeper.    */
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
if|if
condition|(
name|otherWatcher
operator|!=
literal|null
condition|)
block|{
name|otherWatcher
operator|.
name|process
argument_list|(
name|event
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

