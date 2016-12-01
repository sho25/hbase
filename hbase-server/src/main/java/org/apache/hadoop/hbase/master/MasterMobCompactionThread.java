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
name|master
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
name|ExecutorService
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
name|RejectedExecutionException
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
name|SynchronousQueue
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
name|ThreadFactory
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
name|ThreadPoolExecutor
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
name|commons
operator|.
name|logging
operator|.
name|Log
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|LogFactory
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
name|hbase
operator|.
name|HColumnDescriptor
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
name|TableName
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
name|mob
operator|.
name|MobUtils
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
name|util
operator|.
name|EnvironmentEdgeManager
import|;
end_import

begin_comment
comment|/**  * The mob compaction thread used in {@link MasterRpcServices}  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|MasterMobCompactionThread
block|{
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|MasterMobCompactionThread
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|HMaster
name|master
decl_stmt|;
specifier|private
specifier|final
name|Configuration
name|conf
decl_stmt|;
specifier|private
specifier|final
name|ExecutorService
name|mobCompactorPool
decl_stmt|;
specifier|private
specifier|final
name|ExecutorService
name|masterMobPool
decl_stmt|;
specifier|public
name|MasterMobCompactionThread
parameter_list|(
name|HMaster
name|master
parameter_list|)
block|{
name|this
operator|.
name|master
operator|=
name|master
expr_stmt|;
name|this
operator|.
name|conf
operator|=
name|master
operator|.
name|getConfiguration
argument_list|()
expr_stmt|;
specifier|final
name|String
name|n
init|=
name|Thread
operator|.
name|currentThread
argument_list|()
operator|.
name|getName
argument_list|()
decl_stmt|;
comment|// this pool is used to run the mob compaction
name|this
operator|.
name|masterMobPool
operator|=
operator|new
name|ThreadPoolExecutor
argument_list|(
literal|1
argument_list|,
literal|2
argument_list|,
literal|60
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|,
operator|new
name|SynchronousQueue
argument_list|<
name|Runnable
argument_list|>
argument_list|()
argument_list|,
operator|new
name|ThreadFactory
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Thread
name|newThread
parameter_list|(
name|Runnable
name|r
parameter_list|)
block|{
name|String
name|name
init|=
name|n
operator|+
literal|"-MasterMobCompaction-"
operator|+
name|EnvironmentEdgeManager
operator|.
name|currentTime
argument_list|()
decl_stmt|;
return|return
operator|new
name|Thread
argument_list|(
name|r
argument_list|,
name|name
argument_list|)
return|;
block|}
block|}
argument_list|)
expr_stmt|;
operator|(
operator|(
name|ThreadPoolExecutor
operator|)
name|this
operator|.
name|masterMobPool
operator|)
operator|.
name|allowCoreThreadTimeOut
argument_list|(
literal|true
argument_list|)
expr_stmt|;
comment|// this pool is used in the mob compaction to compact the mob files by partitions
comment|// in parallel
name|this
operator|.
name|mobCompactorPool
operator|=
name|MobUtils
operator|.
name|createMobCompactorThreadPool
argument_list|(
name|master
operator|.
name|getConfiguration
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**    * Requests mob compaction    * @param conf The Configuration    * @param fs The file system    * @param tableName The table the compact    * @param columns The column descriptors    * @param tableLockManager The tableLock manager    * @param allFiles Whether add all mob files into the compaction.    */
specifier|public
name|void
name|requestMobCompaction
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|FileSystem
name|fs
parameter_list|,
name|TableName
name|tableName
parameter_list|,
name|List
argument_list|<
name|HColumnDescriptor
argument_list|>
name|columns
parameter_list|,
name|TableLockManager
name|tableLockManager
parameter_list|,
name|boolean
name|allFiles
parameter_list|)
throws|throws
name|IOException
block|{
name|master
operator|.
name|reportMobCompactionStart
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
try|try
block|{
name|masterMobPool
operator|.
name|execute
argument_list|(
operator|new
name|CompactionRunner
argument_list|(
name|fs
argument_list|,
name|tableName
argument_list|,
name|columns
argument_list|,
name|tableLockManager
argument_list|,
name|allFiles
argument_list|,
name|mobCompactorPool
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|RejectedExecutionException
name|e
parameter_list|)
block|{
comment|// in case the request is rejected by the pool
try|try
block|{
name|master
operator|.
name|reportMobCompactionEnd
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e1
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Failed to mark end of mob compaction"
argument_list|,
name|e1
argument_list|)
expr_stmt|;
block|}
throw|throw
name|e
throw|;
block|}
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"The mob compaction is requested for the columns "
operator|+
name|columns
operator|+
literal|" of the table "
operator|+
name|tableName
operator|.
name|getNameAsString
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
class|class
name|CompactionRunner
implements|implements
name|Runnable
block|{
specifier|private
name|FileSystem
name|fs
decl_stmt|;
specifier|private
name|TableName
name|tableName
decl_stmt|;
specifier|private
name|List
argument_list|<
name|HColumnDescriptor
argument_list|>
name|hcds
decl_stmt|;
specifier|private
name|TableLockManager
name|tableLockManager
decl_stmt|;
specifier|private
name|boolean
name|allFiles
decl_stmt|;
specifier|private
name|ExecutorService
name|pool
decl_stmt|;
specifier|public
name|CompactionRunner
parameter_list|(
name|FileSystem
name|fs
parameter_list|,
name|TableName
name|tableName
parameter_list|,
name|List
argument_list|<
name|HColumnDescriptor
argument_list|>
name|hcds
parameter_list|,
name|TableLockManager
name|tableLockManager
parameter_list|,
name|boolean
name|allFiles
parameter_list|,
name|ExecutorService
name|pool
parameter_list|)
block|{
name|super
argument_list|()
expr_stmt|;
name|this
operator|.
name|fs
operator|=
name|fs
expr_stmt|;
name|this
operator|.
name|tableName
operator|=
name|tableName
expr_stmt|;
name|this
operator|.
name|hcds
operator|=
name|hcds
expr_stmt|;
name|this
operator|.
name|tableLockManager
operator|=
name|tableLockManager
expr_stmt|;
name|this
operator|.
name|allFiles
operator|=
name|allFiles
expr_stmt|;
name|this
operator|.
name|pool
operator|=
name|pool
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
try|try
block|{
for|for
control|(
name|HColumnDescriptor
name|hcd
range|:
name|hcds
control|)
block|{
name|MobUtils
operator|.
name|doMobCompaction
argument_list|(
name|conf
argument_list|,
name|fs
argument_list|,
name|tableName
argument_list|,
name|hcd
argument_list|,
name|pool
argument_list|,
name|tableLockManager
argument_list|,
name|allFiles
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Failed to perform the mob compaction"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
try|try
block|{
name|master
operator|.
name|reportMobCompactionEnd
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Failed to mark end of mob compaction"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
comment|/**    * Only interrupt once it's done with a run through the work loop.    */
specifier|private
name|void
name|interruptIfNecessary
parameter_list|()
block|{
name|mobCompactorPool
operator|.
name|shutdown
argument_list|()
expr_stmt|;
name|masterMobPool
operator|.
name|shutdown
argument_list|()
expr_stmt|;
block|}
comment|/**    * Wait for all the threads finish.    */
specifier|private
name|void
name|join
parameter_list|()
block|{
name|waitFor
argument_list|(
name|mobCompactorPool
argument_list|,
literal|"Mob Compaction Thread"
argument_list|)
expr_stmt|;
name|waitFor
argument_list|(
name|masterMobPool
argument_list|,
literal|"Region Server Mob Compaction Thread"
argument_list|)
expr_stmt|;
block|}
comment|/**    * Closes the MasterMobCompactionThread.    */
specifier|public
name|void
name|close
parameter_list|()
block|{
name|interruptIfNecessary
argument_list|()
expr_stmt|;
name|join
argument_list|()
expr_stmt|;
block|}
comment|/**    * Wait for thread finish.    * @param t the thread to wait    * @param name the thread name.    */
specifier|private
name|void
name|waitFor
parameter_list|(
name|ExecutorService
name|t
parameter_list|,
name|String
name|name
parameter_list|)
block|{
name|boolean
name|done
init|=
literal|false
decl_stmt|;
while|while
condition|(
operator|!
name|done
condition|)
block|{
try|try
block|{
name|done
operator|=
name|t
operator|.
name|awaitTermination
argument_list|(
literal|60
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Waiting for "
operator|+
name|name
operator|+
literal|" to finish..."
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|done
condition|)
block|{
name|t
operator|.
name|shutdownNow
argument_list|()
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|ie
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Interrupted waiting for "
operator|+
name|name
operator|+
literal|" to finish..."
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit

