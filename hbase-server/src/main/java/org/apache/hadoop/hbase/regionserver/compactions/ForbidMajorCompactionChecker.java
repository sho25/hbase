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
name|regionserver
operator|.
name|compactions
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
comment|/**  * Check whether forbid major compaction for region.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|ForbidMajorCompactionChecker
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
name|ForbidMajorCompactionChecker
name|INST
init|=
operator|new
name|ForbidMajorCompactionChecker
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
comment|// Forbid major compaction when cluster transit sync replication state from S to DA
return|return
name|state
operator|==
name|SyncReplicationState
operator|.
name|STANDBY
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
name|ForbidMajorCompactionChecker
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

