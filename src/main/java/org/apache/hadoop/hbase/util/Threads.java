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
name|util
package|;
end_package

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
name|java
operator|.
name|io
operator|.
name|PrintWriter
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
name|util
operator|.
name|ReflectionUtils
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

begin_comment
comment|/**  * Thread Utility  */
end_comment

begin_class
specifier|public
class|class
name|Threads
block|{
specifier|protected
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|Threads
operator|.
name|class
argument_list|)
decl_stmt|;
comment|/**    * Utility method that sets name, daemon status and starts passed thread.    * @param t thread to run    * @return Returns the passed Thread<code>t</code>.    */
specifier|public
specifier|static
name|Thread
name|setDaemonThreadRunning
parameter_list|(
specifier|final
name|Thread
name|t
parameter_list|)
block|{
return|return
name|setDaemonThreadRunning
argument_list|(
name|t
argument_list|,
name|t
operator|.
name|getName
argument_list|()
argument_list|)
return|;
block|}
comment|/**    * Utility method that sets name, daemon status and starts passed thread.    * @param t thread to frob    * @param name new name    * @return Returns the passed Thread<code>t</code>.    */
specifier|public
specifier|static
name|Thread
name|setDaemonThreadRunning
parameter_list|(
specifier|final
name|Thread
name|t
parameter_list|,
specifier|final
name|String
name|name
parameter_list|)
block|{
return|return
name|setDaemonThreadRunning
argument_list|(
name|t
argument_list|,
name|name
argument_list|,
literal|null
argument_list|)
return|;
block|}
comment|/**    * Utility method that sets name, daemon status and starts passed thread.    * @param t thread to frob    * @param name new name    * @param handler A handler to set on the thread.  Pass null if want to    * use default handler.    * @return Returns the passed Thread<code>t</code>.    */
specifier|public
specifier|static
name|Thread
name|setDaemonThreadRunning
parameter_list|(
specifier|final
name|Thread
name|t
parameter_list|,
specifier|final
name|String
name|name
parameter_list|,
specifier|final
name|UncaughtExceptionHandler
name|handler
parameter_list|)
block|{
name|t
operator|.
name|setName
argument_list|(
name|name
argument_list|)
expr_stmt|;
if|if
condition|(
name|handler
operator|!=
literal|null
condition|)
block|{
name|t
operator|.
name|setUncaughtExceptionHandler
argument_list|(
name|handler
argument_list|)
expr_stmt|;
block|}
name|t
operator|.
name|setDaemon
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|t
operator|.
name|start
argument_list|()
expr_stmt|;
return|return
name|t
return|;
block|}
comment|/**    * Shutdown passed thread using isAlive and join.    * @param t Thread to shutdown    */
specifier|public
specifier|static
name|void
name|shutdown
parameter_list|(
specifier|final
name|Thread
name|t
parameter_list|)
block|{
name|shutdown
argument_list|(
name|t
argument_list|,
literal|0
argument_list|)
expr_stmt|;
block|}
comment|/**    * Shutdown passed thread using isAlive and join.    * @param joinwait Pass 0 if we're to wait forever.    * @param t Thread to shutdown    */
specifier|public
specifier|static
name|void
name|shutdown
parameter_list|(
specifier|final
name|Thread
name|t
parameter_list|,
specifier|final
name|long
name|joinwait
parameter_list|)
block|{
if|if
condition|(
name|t
operator|==
literal|null
condition|)
return|return;
while|while
condition|(
name|t
operator|.
name|isAlive
argument_list|()
condition|)
block|{
try|try
block|{
name|t
operator|.
name|join
argument_list|(
name|joinwait
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
name|t
operator|.
name|getName
argument_list|()
operator|+
literal|"; joinwait="
operator|+
name|joinwait
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|/**    * @param t Waits on the passed thread to die dumping a threaddump every    * minute while its up.    * @throws InterruptedException    */
specifier|public
specifier|static
name|void
name|threadDumpingIsAlive
parameter_list|(
specifier|final
name|Thread
name|t
parameter_list|)
throws|throws
name|InterruptedException
block|{
if|if
condition|(
name|t
operator|==
literal|null
condition|)
block|{
return|return;
block|}
name|long
name|startTime
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
while|while
condition|(
name|t
operator|.
name|isAlive
argument_list|()
condition|)
block|{
name|Thread
operator|.
name|sleep
argument_list|(
literal|1000
argument_list|)
expr_stmt|;
if|if
condition|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|-
name|startTime
operator|>
literal|60000
condition|)
block|{
name|startTime
operator|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
expr_stmt|;
name|ReflectionUtils
operator|.
name|printThreadInfo
argument_list|(
operator|new
name|PrintWriter
argument_list|(
name|System
operator|.
name|out
argument_list|)
argument_list|,
literal|"Automatic Stack Trace every 60 seconds waiting on "
operator|+
name|t
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|/**    * @param millis How long to sleep for in milliseconds.    */
specifier|public
specifier|static
name|void
name|sleep
parameter_list|(
name|int
name|millis
parameter_list|)
block|{
try|try
block|{
name|Thread
operator|.
name|sleep
argument_list|(
name|millis
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

