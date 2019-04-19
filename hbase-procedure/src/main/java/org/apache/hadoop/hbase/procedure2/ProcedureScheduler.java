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
name|Iterator
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
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|TimeUnit
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
comment|/**  * Keep track of the runnable procedures  */
end_comment

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
interface|interface
name|ProcedureScheduler
block|{
comment|/**    * Start the scheduler    */
name|void
name|start
parameter_list|()
function_decl|;
comment|/**    * Stop the scheduler    */
name|void
name|stop
parameter_list|()
function_decl|;
comment|/**    * In case the class is blocking on poll() waiting for items to be added,    * this method should awake poll() and poll() should return.    */
name|void
name|signalAll
parameter_list|()
function_decl|;
comment|/**    * Inserts the specified element at the front of this queue.    * @param proc the Procedure to add    */
name|void
name|addFront
parameter_list|(
name|Procedure
name|proc
parameter_list|)
function_decl|;
comment|/**    * Inserts the specified element at the front of this queue.    * @param proc the Procedure to add    * @param notify whether need to notify worker    */
name|void
name|addFront
parameter_list|(
name|Procedure
name|proc
parameter_list|,
name|boolean
name|notify
parameter_list|)
function_decl|;
comment|/**    * Inserts all elements in the iterator at the front of this queue.    */
name|void
name|addFront
parameter_list|(
name|Iterator
argument_list|<
name|Procedure
argument_list|>
name|procedureIterator
parameter_list|)
function_decl|;
comment|/**    * Inserts the specified element at the end of this queue.    * @param proc the Procedure to add    */
name|void
name|addBack
parameter_list|(
name|Procedure
name|proc
parameter_list|)
function_decl|;
comment|/**    * Inserts the specified element at the end of this queue.    * @param proc the Procedure to add    * @param notify whether need to notify worker    */
name|void
name|addBack
parameter_list|(
name|Procedure
name|proc
parameter_list|,
name|boolean
name|notify
parameter_list|)
function_decl|;
comment|/**    * The procedure can't run at the moment.    * add it back to the queue, giving priority to someone else.    * @param proc the Procedure to add back to the list    */
name|void
name|yield
parameter_list|(
name|Procedure
name|proc
parameter_list|)
function_decl|;
comment|/**    * The procedure in execution completed.    * This can be implemented to perform cleanups.    * @param proc the Procedure that completed the execution.    */
name|void
name|completionCleanup
parameter_list|(
name|Procedure
name|proc
parameter_list|)
function_decl|;
comment|/**    * @return true if there are procedures available to process, otherwise false.    */
name|boolean
name|hasRunnables
parameter_list|()
function_decl|;
comment|/**    * Fetch one Procedure from the queue    * @return the Procedure to execute, or null if nothing present.    */
name|Procedure
name|poll
parameter_list|()
function_decl|;
comment|/**    * Fetch one Procedure from the queue    * @param timeout how long to wait before giving up, in units of unit    * @param unit a TimeUnit determining how to interpret the timeout parameter    * @return the Procedure to execute, or null if nothing present.    */
name|Procedure
name|poll
parameter_list|(
name|long
name|timeout
parameter_list|,
name|TimeUnit
name|unit
parameter_list|)
function_decl|;
comment|/**    * List lock queues.    * @return the locks    */
name|List
argument_list|<
name|LockedResource
argument_list|>
name|getLocks
parameter_list|()
function_decl|;
comment|/**    * @return {@link LockedResource} for resource of specified type& name. null if resource is not    *         locked.    */
name|LockedResource
name|getLockResource
parameter_list|(
name|LockedResourceType
name|resourceType
parameter_list|,
name|String
name|resourceName
parameter_list|)
function_decl|;
comment|/**    * Returns the number of elements in this queue.    * @return the number of elements in this queue.    */
annotation|@
name|VisibleForTesting
name|int
name|size
parameter_list|()
function_decl|;
comment|/**    * Clear current state of scheduler such that it is equivalent to newly created scheduler.    * Used for testing failure and recovery. To emulate server crash/restart,    * {@link ProcedureExecutor} resets its own state and calls clear() on scheduler.    */
annotation|@
name|VisibleForTesting
name|void
name|clear
parameter_list|()
function_decl|;
block|}
end_interface

end_unit

