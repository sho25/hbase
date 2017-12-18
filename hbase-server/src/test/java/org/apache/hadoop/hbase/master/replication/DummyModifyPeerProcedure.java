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
name|master
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
name|hbase
operator|.
name|master
operator|.
name|procedure
operator|.
name|MasterProcedureEnv
import|;
end_import

begin_class
specifier|public
class|class
name|DummyModifyPeerProcedure
extends|extends
name|ModifyPeerProcedure
block|{
specifier|public
name|DummyModifyPeerProcedure
parameter_list|()
block|{   }
specifier|public
name|DummyModifyPeerProcedure
parameter_list|(
name|String
name|peerId
parameter_list|)
block|{
name|super
argument_list|(
name|peerId
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|PeerOperationType
name|getPeerOperationType
parameter_list|()
block|{
return|return
name|PeerOperationType
operator|.
name|ADD
return|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|prePeerModification
parameter_list|(
name|MasterProcedureEnv
name|env
parameter_list|)
block|{   }
annotation|@
name|Override
specifier|protected
name|void
name|updatePeerStorage
parameter_list|(
name|MasterProcedureEnv
name|env
parameter_list|)
block|{   }
annotation|@
name|Override
specifier|protected
name|void
name|postPeerModification
parameter_list|(
name|MasterProcedureEnv
name|env
parameter_list|)
block|{   }
block|}
end_class

end_unit

