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
name|client
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|InterruptedIOException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collection
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|function
operator|.
name|Consumer
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
name|HRegionLocation
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
name|ServerName
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
comment|/**  * An interface for client request scheduling algorithm.  */
end_comment

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|Public
specifier|public
interface|interface
name|RequestController
block|{
annotation|@
name|InterfaceAudience
operator|.
name|Public
specifier|public
enum|enum
name|ReturnCode
block|{
comment|/**      * Accept current row.      */
name|INCLUDE
block|,
comment|/**      * Skip current row.      */
name|SKIP
block|,
comment|/**      * No more row can be included.      */
name|END
block|}
comment|/**    * Picks up the valid data.    */
annotation|@
name|InterfaceAudience
operator|.
name|Public
specifier|public
interface|interface
name|Checker
block|{
comment|/**      * Checks the data whether it is valid to submit.      * @param loc the destination of data      * @param row the data to check      * @return describe the decision for the row      */
name|ReturnCode
name|canTakeRow
parameter_list|(
name|HRegionLocation
name|loc
parameter_list|,
name|Row
name|row
parameter_list|)
function_decl|;
comment|/**      * Reset the state of the scheduler when completing the iteration of rows.      * @throws InterruptedIOException some controller may wait      * for some busy region or RS to complete the undealt request.      */
name|void
name|reset
parameter_list|()
throws|throws
name|InterruptedIOException
function_decl|;
block|}
comment|/**    * @return A new checker for evaluating a batch rows.    */
name|Checker
name|newChecker
parameter_list|()
function_decl|;
comment|/**    * Increment the counter if we build a valid task.    * @param regions The destination of task    * @param sn The target server    */
name|void
name|incTaskCounters
parameter_list|(
name|Collection
argument_list|<
name|byte
index|[]
argument_list|>
name|regions
parameter_list|,
name|ServerName
name|sn
parameter_list|)
function_decl|;
comment|/**    * Decrement the counter if a task is accomplished.    * @param regions The destination of task    * @param sn The target server    */
name|void
name|decTaskCounters
parameter_list|(
name|Collection
argument_list|<
name|byte
index|[]
argument_list|>
name|regions
parameter_list|,
name|ServerName
name|sn
parameter_list|)
function_decl|;
comment|/**    * @return The number of running task.    */
name|long
name|getNumberOfTasksInProgress
parameter_list|()
function_decl|;
comment|/**    * Waits for the running tasks to complete.    * If there are specified threshold and trigger, the implementation should    * wake up once in a while for checking the threshold and calling trigger.    * @param max This method will return if the number of running tasks is    * less than or equal to max.    * @param id the caller's id    * @param periodToTrigger The period to invoke the trigger. This value is a    * hint. The real period depends on the implementation.    * @param trigger The object to call periodically.    * @throws java.io.InterruptedIOException If the waiting is interrupted    */
name|void
name|waitForMaximumCurrentTasks
parameter_list|(
name|long
name|max
parameter_list|,
name|long
name|id
parameter_list|,
name|int
name|periodToTrigger
parameter_list|,
name|Consumer
argument_list|<
name|Long
argument_list|>
name|trigger
parameter_list|)
throws|throws
name|InterruptedIOException
function_decl|;
comment|/**    * Wait until there is at least one slot for a new task.    * @param id the caller's id    * @param periodToTrigger The period to invoke the trigger. This value is a    * hint. The real period depends on the implementation.    * @param trigger The object to call periodically.    * @throws java.io.InterruptedIOException If the waiting is interrupted    */
name|void
name|waitForFreeSlot
parameter_list|(
name|long
name|id
parameter_list|,
name|int
name|periodToTrigger
parameter_list|,
name|Consumer
argument_list|<
name|Long
argument_list|>
name|trigger
parameter_list|)
throws|throws
name|InterruptedIOException
function_decl|;
block|}
end_interface

end_unit

