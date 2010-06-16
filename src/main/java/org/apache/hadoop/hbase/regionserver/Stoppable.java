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

begin_comment
comment|/**  * Implementations are stoppable.  */
end_comment

begin_interface
interface|interface
name|Stoppable
block|{
comment|// Starting small, just doing a stoppable/stop for now and keeping it package
comment|// protected for now.  Needed so don't have to pass RegionServer instance
comment|// everywhere.  Doing Lifecycle seemed a stretch since none of our servers
comment|// do natural start/stop, etc. RegionServer is hosted in a Thread (can't do
comment|// 'stop' on a Thread and 'start' has special meaning for Threads) and then
comment|// Master is implemented differently again (it is a Thread itself). We
comment|// should move to redoing Master and RegionServer servers to use Spring or
comment|// some such container but for now, I just need stop -- St.Ack.
comment|/**    * Stop service.    */
specifier|public
name|void
name|stop
parameter_list|()
function_decl|;
block|}
end_interface

end_unit

