begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**   *   * Licensed to the Apache Software Foundation (ASF) under one   * or more contributor license agreements.  See the NOTICE file   * distributed with this work for additional information   * regarding copyright ownership.  The ASF licenses this file   * to you under the Apache License, Version 2.0 (the   * "License"); you may not use this file except in compliance   * with the License.  You may obtain a copy of the License at   *   *     http://www.apache.org/licenses/LICENSE-2.0   *   * Unless required by applicable law or agreed to in writing, software   * distributed under the License is distributed on an "AS IS" BASIS,   * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.   * See the License for the specific language governing permissions and   * limitations under the License.   */
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
name|util
operator|.
name|concurrent
operator|.
name|atomic
operator|.
name|LongAdder
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
name|conf
operator|.
name|Configuration
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
name|fs
operator|.
name|FileSystem
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
name|fs
operator|.
name|Path
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
name|SplitLogTask
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
name|regionserver
operator|.
name|RegionServerServices
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
name|regionserver
operator|.
name|SplitLogWorker
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
name|regionserver
operator|.
name|SplitLogWorker
operator|.
name|TaskExecutor
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
comment|/**  * Coordinated operations for {@link SplitLogWorker} and  * {@link org.apache.hadoop.hbase.regionserver.handler.WALSplitterHandler} Important  * methods for SplitLogWorker:<BR>  * {@link #isReady()} called from {@link SplitLogWorker#run()} to check whether the coordination is  * ready to supply the tasks<BR>  * {@link #taskLoop()} loop for new tasks until the worker is stopped<BR>  * {@link #isStop()} a flag indicates whether worker should finish<BR>  * {@link #registerListener()} called from {@link SplitLogWorker#run()} and could register listener  * for external changes in coordination (if required)<BR>  * {@link #endTask(SplitLogTask, LongAdder, SplitTaskDetails)} notify coordination engine that  *<p>  * Important methods for WALSplitterHandler:<BR>  * splitting task has completed.  */
end_comment

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
interface|interface
name|SplitLogWorkerCoordination
block|{
comment|/**    * Initialize internal values. This method should be used when corresponding SplitLogWorker    * instance is created    * @param server instance of RegionServerServices to work with    * @param conf is current configuration.    * @param splitTaskExecutor split executor from SplitLogWorker    * @param worker instance of SplitLogWorker    */
name|void
name|init
parameter_list|(
name|RegionServerServices
name|server
parameter_list|,
name|Configuration
name|conf
parameter_list|,
name|TaskExecutor
name|splitTaskExecutor
parameter_list|,
name|SplitLogWorker
name|worker
parameter_list|)
function_decl|;
comment|/**    *  called when Coordination should stop processing tasks and exit    */
name|void
name|stopProcessingTasks
parameter_list|()
function_decl|;
comment|/**    * @return the current value of exitWorker    */
name|boolean
name|isStop
parameter_list|()
function_decl|;
comment|/**    * Wait for the new tasks and grab one    * @throws InterruptedException if the SplitLogWorker was stopped    */
name|void
name|taskLoop
parameter_list|()
throws|throws
name|InterruptedException
function_decl|;
comment|/**    * marks log file as corrupted    * @param rootDir where to find the log    * @param name of the log    * @param fs file system    */
name|void
name|markCorrupted
parameter_list|(
name|Path
name|rootDir
parameter_list|,
name|String
name|name
parameter_list|,
name|FileSystem
name|fs
parameter_list|)
function_decl|;
comment|/**    * Check whether the log splitter is ready to supply tasks    * @return false if there is no tasks    * @throws InterruptedException if the SplitLogWorker was stopped    */
name|boolean
name|isReady
parameter_list|()
throws|throws
name|InterruptedException
function_decl|;
comment|/**    * Used by unit tests to check how many tasks were processed    * @return number of tasks    */
annotation|@
name|VisibleForTesting
name|int
name|getTaskReadySeq
parameter_list|()
function_decl|;
comment|/**    * set the listener for task changes. Implementation specific    */
name|void
name|registerListener
parameter_list|()
function_decl|;
comment|/**    * remove the listener for task changes. Implementation specific    */
name|void
name|removeListener
parameter_list|()
function_decl|;
comment|/* WALSplitterHandler part */
comment|/**    * Notify coordination engine that splitting task has completed.    * @param slt See {@link SplitLogTask}    * @param ctr counter to be updated    * @param splitTaskDetails details about log split task (specific to coordination engine being    *          used).    */
name|void
name|endTask
parameter_list|(
name|SplitLogTask
name|slt
parameter_list|,
name|LongAdder
name|ctr
parameter_list|,
name|SplitTaskDetails
name|splitTaskDetails
parameter_list|)
function_decl|;
comment|/**    * Interface for log-split tasks Used to carry implementation details in encapsulated way through    * Handlers to the coordination API.    */
interface|interface
name|SplitTaskDetails
block|{
comment|/**      * @return full file path in HDFS for the WAL file to be split.      */
name|String
name|getWALFile
parameter_list|()
function_decl|;
block|}
block|}
end_interface

end_unit

