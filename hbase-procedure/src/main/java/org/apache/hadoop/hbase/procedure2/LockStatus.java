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

begin_comment
comment|/**  * Interface to get status of a Lock without getting access to acquire/release lock.  * Currently used in MasterProcedureScheduler where we want to give Queues access to lock's  * status for scheduling purposes, but not the ability to acquire/release it.  */
end_comment

begin_interface
specifier|public
interface|interface
name|LockStatus
block|{
name|boolean
name|isLocked
parameter_list|()
function_decl|;
name|boolean
name|hasExclusiveLock
parameter_list|()
function_decl|;
name|boolean
name|isLockOwner
parameter_list|(
name|long
name|procId
parameter_list|)
function_decl|;
name|boolean
name|hasParentLock
parameter_list|(
specifier|final
name|Procedure
name|proc
parameter_list|)
function_decl|;
name|boolean
name|hasLockAccess
parameter_list|(
specifier|final
name|Procedure
name|proc
parameter_list|)
function_decl|;
name|long
name|getExclusiveLockProcIdOwner
parameter_list|()
function_decl|;
name|int
name|getSharedLockCount
parameter_list|()
function_decl|;
block|}
end_interface

end_unit

