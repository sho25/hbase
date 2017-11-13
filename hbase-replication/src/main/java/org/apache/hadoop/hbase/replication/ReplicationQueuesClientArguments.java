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
name|Abortable
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
comment|/**  * Wrapper around common arguments used to construct ReplicationQueuesClient. Used to construct  * various ReplicationQueuesClient Implementations with different constructor arguments by  * reflection.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|ReplicationQueuesClientArguments
extends|extends
name|ReplicationQueuesArguments
block|{
specifier|public
name|ReplicationQueuesClientArguments
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|Abortable
name|abort
parameter_list|,
name|ZKWatcher
name|zk
parameter_list|)
block|{
name|super
argument_list|(
name|conf
argument_list|,
name|abort
argument_list|,
name|zk
argument_list|)
expr_stmt|;
block|}
specifier|public
name|ReplicationQueuesClientArguments
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|Abortable
name|abort
parameter_list|)
block|{
name|super
argument_list|(
name|conf
argument_list|,
name|abort
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

