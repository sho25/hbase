begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|java
operator|.
name|util
operator|.
name|Set
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
name|ConcurrentMap
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
name|hadoop
operator|.
name|hbase
operator|.
name|master
operator|.
name|MasterServices
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
name|master
operator|.
name|SplitLogManager
operator|.
name|ResubmitDirective
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
name|master
operator|.
name|SplitLogManager
operator|.
name|Task
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
name|hadoop
operator|.
name|hbase
operator|.
name|shaded
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
comment|/**  * Coordination for SplitLogManager. It creates and works with tasks for split log operations<BR>  * Manager prepares task by calling {@link #prepareTask} and submit it by  * {@link #submitTask(String)}. After that it periodically check the number of remaining tasks by  * {@link #remainingTasksInCoordination()} and waits until it become zero.  *<P>  * Methods required for task life circle:<BR>  * {@link #checkTaskStillAvailable(String)} Check that task is still there<BR>  * {@link #checkTasks()} check for unassigned tasks and resubmit them  */
end_comment

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
interface|interface
name|SplitLogManagerCoordination
block|{
comment|/**    * Detail class that shares data between coordination and split log manager    */
class|class
name|SplitLogManagerDetails
block|{
specifier|final
specifier|private
name|ConcurrentMap
argument_list|<
name|String
argument_list|,
name|Task
argument_list|>
name|tasks
decl_stmt|;
specifier|final
specifier|private
name|MasterServices
name|master
decl_stmt|;
specifier|final
specifier|private
name|Set
argument_list|<
name|String
argument_list|>
name|failedDeletions
decl_stmt|;
specifier|public
name|SplitLogManagerDetails
parameter_list|(
name|ConcurrentMap
argument_list|<
name|String
argument_list|,
name|Task
argument_list|>
name|tasks
parameter_list|,
name|MasterServices
name|master
parameter_list|,
name|Set
argument_list|<
name|String
argument_list|>
name|failedDeletions
parameter_list|)
block|{
name|this
operator|.
name|tasks
operator|=
name|tasks
expr_stmt|;
name|this
operator|.
name|master
operator|=
name|master
expr_stmt|;
name|this
operator|.
name|failedDeletions
operator|=
name|failedDeletions
expr_stmt|;
block|}
comment|/**      * @return the master value      */
specifier|public
name|MasterServices
name|getMaster
parameter_list|()
block|{
return|return
name|master
return|;
block|}
comment|/**      * @return map of tasks      */
specifier|public
name|ConcurrentMap
argument_list|<
name|String
argument_list|,
name|Task
argument_list|>
name|getTasks
parameter_list|()
block|{
return|return
name|tasks
return|;
block|}
comment|/**      * @return a set of failed deletions      */
specifier|public
name|Set
argument_list|<
name|String
argument_list|>
name|getFailedDeletions
parameter_list|()
block|{
return|return
name|failedDeletions
return|;
block|}
comment|/**      * @return server name      */
specifier|public
name|ServerName
name|getServerName
parameter_list|()
block|{
return|return
name|master
operator|.
name|getServerName
argument_list|()
return|;
block|}
block|}
comment|/**    * Provide the configuration from the SplitLogManager    */
name|void
name|setDetails
parameter_list|(
name|SplitLogManagerDetails
name|details
parameter_list|)
function_decl|;
comment|/**    * Returns the configuration that was provided previously    */
name|SplitLogManagerDetails
name|getDetails
parameter_list|()
function_decl|;
comment|/**    * Prepare the new task    * @param taskName name of the task    * @return the task id    */
name|String
name|prepareTask
parameter_list|(
name|String
name|taskName
parameter_list|)
function_decl|;
comment|/**    * tells Coordination that it should check for new tasks    */
name|void
name|checkTasks
parameter_list|()
function_decl|;
comment|/**    * Return the number of remaining tasks    */
name|int
name|remainingTasksInCoordination
parameter_list|()
function_decl|;
comment|/**    * Check that the task is still there    * @param task node to check    */
name|void
name|checkTaskStillAvailable
parameter_list|(
name|String
name|task
parameter_list|)
function_decl|;
comment|/**    * Resubmit the task in case if found unassigned or failed    * @param taskName path related to task    * @param task to resubmit    * @param force whether it should be forced    * @return whether it was successful    */
name|boolean
name|resubmitTask
parameter_list|(
name|String
name|taskName
parameter_list|,
name|Task
name|task
parameter_list|,
name|ResubmitDirective
name|force
parameter_list|)
function_decl|;
comment|/**    * @param taskName to be submitted    */
name|void
name|submitTask
parameter_list|(
name|String
name|taskName
parameter_list|)
function_decl|;
comment|/**    * @param taskName to be removed    */
name|void
name|deleteTask
parameter_list|(
name|String
name|taskName
parameter_list|)
function_decl|;
comment|/**    * Support method to init constants such as timeout. Mostly required for UTs.    * @throws IOException    */
annotation|@
name|VisibleForTesting
name|void
name|init
parameter_list|()
throws|throws
name|IOException
function_decl|;
block|}
end_interface

end_unit

