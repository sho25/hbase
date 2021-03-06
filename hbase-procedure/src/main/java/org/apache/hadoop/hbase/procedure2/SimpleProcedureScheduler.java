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
name|procedure2
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collections
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|List
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
name|InterfaceStability
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hbase
operator|.
name|thirdparty
operator|.
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|annotations
operator|.
name|VisibleForTesting
import|;
end_import

begin_comment
comment|/**  * Simple scheduler for procedures  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
annotation|@
name|InterfaceStability
operator|.
name|Evolving
specifier|public
class|class
name|SimpleProcedureScheduler
extends|extends
name|AbstractProcedureScheduler
block|{
specifier|private
specifier|final
name|ProcedureDeque
name|runnables
init|=
operator|new
name|ProcedureDeque
argument_list|()
decl_stmt|;
annotation|@
name|Override
specifier|protected
name|void
name|enqueue
parameter_list|(
specifier|final
name|Procedure
name|procedure
parameter_list|,
specifier|final
name|boolean
name|addFront
parameter_list|)
block|{
if|if
condition|(
name|addFront
condition|)
block|{
name|runnables
operator|.
name|addFirst
argument_list|(
name|procedure
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|runnables
operator|.
name|addLast
argument_list|(
name|procedure
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|protected
name|Procedure
name|dequeue
parameter_list|()
block|{
return|return
name|runnables
operator|.
name|poll
argument_list|()
return|;
block|}
annotation|@
name|VisibleForTesting
annotation|@
name|Override
specifier|public
name|void
name|clear
parameter_list|()
block|{
name|schedLock
argument_list|()
expr_stmt|;
try|try
block|{
name|runnables
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
finally|finally
block|{
name|schedUnlock
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|yield
parameter_list|(
specifier|final
name|Procedure
name|proc
parameter_list|)
block|{
name|addBack
argument_list|(
name|proc
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|queueHasRunnables
parameter_list|()
block|{
return|return
name|runnables
operator|.
name|size
argument_list|()
operator|>
literal|0
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|queueSize
parameter_list|()
block|{
return|return
name|runnables
operator|.
name|size
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|completionCleanup
parameter_list|(
name|Procedure
name|proc
parameter_list|)
block|{   }
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|LockedResource
argument_list|>
name|getLocks
parameter_list|()
block|{
return|return
name|Collections
operator|.
name|emptyList
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|LockedResource
name|getLockResource
parameter_list|(
name|LockedResourceType
name|resourceType
parameter_list|,
name|String
name|resourceName
parameter_list|)
block|{
return|return
literal|null
return|;
block|}
block|}
end_class

end_unit

