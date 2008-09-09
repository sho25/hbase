begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2007 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|AtomicBoolean
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
name|hbase
operator|.
name|util
operator|.
name|Sleeper
import|;
end_import

begin_comment
comment|/**  * Chore is a task performed on a period in hbase.  The chore is run in its own  * thread. This base abstract class provides while loop and sleeping facility.  * If an unhandled exception, the threads exit is logged.  * Implementers just need to add checking if there is work to be done and if  * so, do it.  Its the base of most of the chore threads in hbase.  */
end_comment

begin_class
specifier|public
specifier|abstract
class|class
name|Chore
extends|extends
name|Thread
block|{
specifier|private
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|this
operator|.
name|getClass
argument_list|()
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|Sleeper
name|sleeper
decl_stmt|;
specifier|protected
specifier|volatile
name|AtomicBoolean
name|stop
decl_stmt|;
comment|/**    * @param p Period at which we should run.  Will be adjusted appropriately    * should we find work and it takes time to complete.    * @param s When this flag is set to true, this thread will cleanup and exit    * cleanly.    */
specifier|public
name|Chore
parameter_list|(
specifier|final
name|int
name|p
parameter_list|,
specifier|final
name|AtomicBoolean
name|s
parameter_list|)
block|{
name|super
argument_list|()
expr_stmt|;
name|this
operator|.
name|sleeper
operator|=
operator|new
name|Sleeper
argument_list|(
name|p
argument_list|,
name|s
argument_list|)
expr_stmt|;
name|this
operator|.
name|stop
operator|=
name|s
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
while|while
condition|(
operator|!
name|initialChore
argument_list|()
condition|)
block|{
name|this
operator|.
name|sleeper
operator|.
name|sleep
argument_list|()
expr_stmt|;
block|}
name|this
operator|.
name|sleeper
operator|.
name|sleep
argument_list|()
expr_stmt|;
while|while
condition|(
operator|!
name|this
operator|.
name|stop
operator|.
name|get
argument_list|()
condition|)
block|{
try|try
block|{
name|long
name|startTime
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|chore
argument_list|()
expr_stmt|;
name|this
operator|.
name|sleeper
operator|.
name|sleep
argument_list|(
name|startTime
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Caught exception"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
block|}
catch|catch
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
name|LOG
operator|.
name|fatal
argument_list|(
literal|"Caught error. Starting shutdown."
argument_list|,
name|t
argument_list|)
expr_stmt|;
name|this
operator|.
name|stop
operator|.
name|set
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|LOG
operator|.
name|info
argument_list|(
name|getName
argument_list|()
operator|+
literal|" exiting"
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Override to run a task before we start looping.    * @return true if initial chore was successful    */
specifier|protected
name|boolean
name|initialChore
parameter_list|()
block|{
comment|// Default does nothing.
return|return
literal|true
return|;
block|}
comment|/**    * Look for chores.  If any found, do them else just return.    */
specifier|protected
specifier|abstract
name|void
name|chore
parameter_list|()
function_decl|;
comment|/**    * Sleep for period.    */
specifier|protected
name|void
name|sleep
parameter_list|()
block|{
name|this
operator|.
name|sleeper
operator|.
name|sleep
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

