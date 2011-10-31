begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2010 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|lang
operator|.
name|Thread
operator|.
name|UncaughtExceptionHandler
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
name|Executors
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
name|Server
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|util
operator|.
name|concurrent
operator|.
name|ThreadFactoryBuilder
import|;
end_import

begin_comment
comment|/**  * Base class used bulk assigning and unassigning regions.  * Encapsulates a fixed size thread pool of executors to run assignment/unassignment.  * Implement {@link #populatePool(java.util.concurrent.ExecutorService)} and  * {@link #waitUntilDone(long)}.  The default implementation of  * the {@link #getUncaughtExceptionHandler()} is to abort the hosting  * Server.  */
end_comment

begin_class
specifier|public
specifier|abstract
class|class
name|BulkAssigner
block|{
specifier|protected
specifier|final
name|Server
name|server
decl_stmt|;
comment|/**    * @param server An instance of Server    */
specifier|public
name|BulkAssigner
parameter_list|(
specifier|final
name|Server
name|server
parameter_list|)
block|{
name|this
operator|.
name|server
operator|=
name|server
expr_stmt|;
block|}
comment|/**    * @return What to use for a thread prefix when executor runs.    */
specifier|protected
name|String
name|getThreadNamePrefix
parameter_list|()
block|{
return|return
name|this
operator|.
name|server
operator|.
name|getServerName
argument_list|()
operator|+
literal|"-"
operator|+
name|this
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
return|;
block|}
specifier|protected
name|UncaughtExceptionHandler
name|getUncaughtExceptionHandler
parameter_list|()
block|{
return|return
operator|new
name|UncaughtExceptionHandler
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|uncaughtException
parameter_list|(
name|Thread
name|t
parameter_list|,
name|Throwable
name|e
parameter_list|)
block|{
comment|// Abort if exception of any kind.
name|server
operator|.
name|abort
argument_list|(
literal|"Uncaught exception in "
operator|+
name|t
operator|.
name|getName
argument_list|()
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
return|;
block|}
specifier|protected
name|int
name|getThreadCount
parameter_list|()
block|{
return|return
name|this
operator|.
name|server
operator|.
name|getConfiguration
argument_list|()
operator|.
name|getInt
argument_list|(
literal|"hbase.bulk.assignment.threadpool.size"
argument_list|,
literal|20
argument_list|)
return|;
block|}
specifier|protected
name|long
name|getTimeoutOnRIT
parameter_list|()
block|{
return|return
name|this
operator|.
name|server
operator|.
name|getConfiguration
argument_list|()
operator|.
name|getLong
argument_list|(
literal|"hbase.bulk.assignment.waiton.empty.rit"
argument_list|,
literal|5
operator|*
literal|60
operator|*
literal|1000
argument_list|)
return|;
block|}
specifier|protected
specifier|abstract
name|void
name|populatePool
parameter_list|(
specifier|final
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|ExecutorService
name|pool
parameter_list|)
throws|throws
name|IOException
function_decl|;
specifier|public
name|boolean
name|bulkAssign
parameter_list|()
throws|throws
name|InterruptedException
throws|,
name|IOException
block|{
return|return
name|bulkAssign
argument_list|(
literal|true
argument_list|)
return|;
block|}
comment|/**    * Run the bulk assign.    *     * @param sync    *          Whether to assign synchronously.    * @throws InterruptedException    * @return True if done.    * @throws IOException    */
specifier|public
name|boolean
name|bulkAssign
parameter_list|(
name|boolean
name|sync
parameter_list|)
throws|throws
name|InterruptedException
throws|,
name|IOException
block|{
name|boolean
name|result
init|=
literal|false
decl_stmt|;
name|ThreadFactoryBuilder
name|builder
init|=
operator|new
name|ThreadFactoryBuilder
argument_list|()
decl_stmt|;
name|builder
operator|.
name|setDaemon
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|builder
operator|.
name|setNameFormat
argument_list|(
name|getThreadNamePrefix
argument_list|()
operator|+
literal|"-%1$d"
argument_list|)
expr_stmt|;
name|builder
operator|.
name|setUncaughtExceptionHandler
argument_list|(
name|getUncaughtExceptionHandler
argument_list|()
argument_list|)
expr_stmt|;
name|int
name|threadCount
init|=
name|getThreadCount
argument_list|()
decl_stmt|;
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|ExecutorService
name|pool
init|=
name|Executors
operator|.
name|newFixedThreadPool
argument_list|(
name|threadCount
argument_list|,
name|builder
operator|.
name|build
argument_list|()
argument_list|)
decl_stmt|;
try|try
block|{
name|populatePool
argument_list|(
name|pool
argument_list|)
expr_stmt|;
comment|// How long to wait on empty regions-in-transition.  If we timeout, the
comment|// RIT monitor should do fixup.
if|if
condition|(
name|sync
condition|)
name|result
operator|=
name|waitUntilDone
argument_list|(
name|getTimeoutOnRIT
argument_list|()
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
comment|// We're done with the pool.  It'll exit when its done all in queue.
name|pool
operator|.
name|shutdown
argument_list|()
expr_stmt|;
block|}
return|return
name|result
return|;
block|}
comment|/**    * Wait until bulk assign is done.    * @param timeout How long to wait.    * @throws InterruptedException    * @return True if the condition we were waiting on happened.    */
specifier|protected
specifier|abstract
name|boolean
name|waitUntilDone
parameter_list|(
specifier|final
name|long
name|timeout
parameter_list|)
throws|throws
name|InterruptedException
function_decl|;
block|}
end_class

end_unit

