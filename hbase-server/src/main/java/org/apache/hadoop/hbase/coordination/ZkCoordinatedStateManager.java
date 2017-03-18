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
name|coordination
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
name|HBaseInterfaceAudience
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
name|Server
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
name|procedure
operator|.
name|ProcedureCoordinatorRpcs
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
name|procedure
operator|.
name|ProcedureMemberRpcs
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
name|procedure
operator|.
name|ZKProcedureCoordinator
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
name|procedure
operator|.
name|ZKProcedureMemberRpcs
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
comment|/**  * ZooKeeper-based implementation of {@link org.apache.hadoop.hbase.CoordinatedStateManager}.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|LimitedPrivate
argument_list|(
name|HBaseInterfaceAudience
operator|.
name|CONFIG
argument_list|)
specifier|public
class|class
name|ZkCoordinatedStateManager
extends|extends
name|BaseCoordinatedStateManager
block|{
specifier|protected
name|Server
name|server
decl_stmt|;
specifier|protected
name|ZooKeeperWatcher
name|watcher
decl_stmt|;
specifier|protected
name|SplitLogWorkerCoordination
name|splitLogWorkerCoordination
decl_stmt|;
specifier|protected
name|SplitLogManagerCoordination
name|splitLogManagerCoordination
decl_stmt|;
annotation|@
name|Override
specifier|public
name|void
name|initialize
parameter_list|(
name|Server
name|server
parameter_list|)
block|{
name|this
operator|.
name|server
operator|=
name|server
expr_stmt|;
name|this
operator|.
name|watcher
operator|=
name|server
operator|.
name|getZooKeeper
argument_list|()
expr_stmt|;
name|splitLogWorkerCoordination
operator|=
operator|new
name|ZkSplitLogWorkerCoordination
argument_list|(
name|this
argument_list|,
name|watcher
argument_list|)
expr_stmt|;
name|splitLogManagerCoordination
operator|=
operator|new
name|ZKSplitLogManagerCoordination
argument_list|(
name|this
argument_list|,
name|watcher
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Server
name|getServer
parameter_list|()
block|{
return|return
name|server
return|;
block|}
annotation|@
name|Override
specifier|public
name|SplitLogWorkerCoordination
name|getSplitLogWorkerCoordination
parameter_list|()
block|{
return|return
name|splitLogWorkerCoordination
return|;
block|}
annotation|@
name|Override
specifier|public
name|SplitLogManagerCoordination
name|getSplitLogManagerCoordination
parameter_list|()
block|{
return|return
name|splitLogManagerCoordination
return|;
block|}
annotation|@
name|Override
specifier|public
name|ProcedureCoordinatorRpcs
name|getProcedureCoordinatorRpcs
parameter_list|(
name|String
name|procType
parameter_list|,
name|String
name|coordNode
parameter_list|)
throws|throws
name|IOException
block|{
return|return
operator|new
name|ZKProcedureCoordinator
argument_list|(
name|watcher
argument_list|,
name|procType
argument_list|,
name|coordNode
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|ProcedureMemberRpcs
name|getProcedureMemberRpcs
parameter_list|(
name|String
name|procType
parameter_list|)
throws|throws
name|KeeperException
block|{
return|return
operator|new
name|ZKProcedureMemberRpcs
argument_list|(
name|watcher
argument_list|,
name|procType
argument_list|)
return|;
block|}
block|}
end_class

end_unit

