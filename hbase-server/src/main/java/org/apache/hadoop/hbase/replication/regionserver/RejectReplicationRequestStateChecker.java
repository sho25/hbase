begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Copyright The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
operator|.
name|regionserver
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|function
operator|.
name|BiPredicate
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
name|replication
operator|.
name|SyncReplicationState
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
comment|/**  * Check whether we need to reject the replication request from source cluster.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|RejectReplicationRequestStateChecker
implements|implements
name|BiPredicate
argument_list|<
name|SyncReplicationState
argument_list|,
name|SyncReplicationState
argument_list|>
block|{
specifier|private
specifier|static
specifier|final
name|RejectReplicationRequestStateChecker
name|INST
init|=
operator|new
name|RejectReplicationRequestStateChecker
argument_list|()
decl_stmt|;
annotation|@
name|Override
specifier|public
name|boolean
name|test
parameter_list|(
name|SyncReplicationState
name|state
parameter_list|,
name|SyncReplicationState
name|newState
parameter_list|)
block|{
return|return
name|state
operator|==
name|SyncReplicationState
operator|.
name|ACTIVE
operator|||
name|state
operator|==
name|SyncReplicationState
operator|.
name|DOWNGRADE_ACTIVE
operator|||
name|newState
operator|==
name|SyncReplicationState
operator|.
name|ACTIVE
operator|||
name|newState
operator|==
name|SyncReplicationState
operator|.
name|DOWNGRADE_ACTIVE
return|;
block|}
specifier|public
specifier|static
name|RejectReplicationRequestStateChecker
name|get
parameter_list|()
block|{
return|return
name|INST
return|;
block|}
block|}
end_class

end_unit

