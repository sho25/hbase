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
comment|/**  * Interface to get status of a Lock without getting access to acquire/release lock. Currently used  * in MasterProcedureScheduler where we want to give Queues access to lock's status for scheduling  * purposes, but not the ability to acquire/release it.  */
end_comment

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
interface|interface
name|LockStatus
block|{
comment|/**    * Return whether this lock has already been held,    *<p/>    * Notice that, holding the exclusive lock or shared lock are both considered as locked, i.e, this    * method usually equals to {@code hasExclusiveLock() || getSharedLockCount()> 0}.    */
specifier|default
name|boolean
name|isLocked
parameter_list|()
block|{
return|return
name|hasExclusiveLock
argument_list|()
operator|||
name|getSharedLockCount
argument_list|()
operator|>
literal|0
return|;
block|}
comment|/**    * Whether the exclusive lock has been held.    */
name|boolean
name|hasExclusiveLock
parameter_list|()
function_decl|;
comment|/**    * Return true if the procedure itself holds the exclusive lock, or any ancestors of the give    * procedure hold the exclusive lock.    */
name|boolean
name|hasLockAccess
parameter_list|(
name|Procedure
argument_list|<
name|?
argument_list|>
name|proc
parameter_list|)
function_decl|;
comment|/**    * Get the procedure which holds the exclusive lock.    */
name|Procedure
argument_list|<
name|?
argument_list|>
name|getExclusiveLockOwnerProcedure
parameter_list|()
function_decl|;
comment|/**    * Return the id of the procedure which holds the exclusive lock, if exists. Or a negative value    * which means no one holds the exclusive lock.    *<p/>    * Notice that, in HBase, we assume that the procedure id is positive, or at least non-negative.    */
specifier|default
name|long
name|getExclusiveLockProcIdOwner
parameter_list|()
block|{
name|Procedure
argument_list|<
name|?
argument_list|>
name|proc
init|=
name|getExclusiveLockOwnerProcedure
argument_list|()
decl_stmt|;
return|return
name|proc
operator|!=
literal|null
condition|?
name|proc
operator|.
name|getProcId
argument_list|()
else|:
operator|-
literal|1L
return|;
block|}
comment|/**    * Get the number of procedures which hold the shared lock.    */
name|int
name|getSharedLockCount
parameter_list|()
function_decl|;
block|}
end_interface

end_unit

