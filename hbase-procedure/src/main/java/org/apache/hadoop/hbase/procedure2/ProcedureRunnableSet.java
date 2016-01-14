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
name|classification
operator|.
name|InterfaceStability
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
annotation|@
name|InterfaceStability
operator|.
name|Evolving
specifier|public
interface|interface
name|ProcedureRunnableSet
block|{
comment|/**    * Inserts the specified element at the front of this queue.    * @param proc the Procedure to add    */
name|void
name|addFront
parameter_list|(
name|Procedure
name|proc
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
comment|/**    * Fetch one Procedure from the queue    * @return the Procedure to execute, or null if nothing present.    */
name|Procedure
name|poll
parameter_list|()
function_decl|;
comment|/**    * In case the class is blocking on poll() waiting for items to be added,    * this method should awake poll() and poll() should return.    */
name|void
name|signalAll
parameter_list|()
function_decl|;
comment|/**    * Returns the number of elements in this collection.    * @return the number of elements in this collection.    */
name|int
name|size
parameter_list|()
function_decl|;
comment|/**    * Removes all of the elements from this collection.    */
name|void
name|clear
parameter_list|()
function_decl|;
block|}
end_interface

end_unit

