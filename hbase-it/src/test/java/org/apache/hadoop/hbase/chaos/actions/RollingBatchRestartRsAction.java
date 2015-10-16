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
name|chaos
operator|.
name|actions
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
name|ArrayList
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|LinkedList
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
name|Queue
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
name|lang
operator|.
name|math
operator|.
name|RandomUtils
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
name|chaos
operator|.
name|monkies
operator|.
name|PolicyBasedChaosMonkey
import|;
end_import

begin_comment
comment|/**  * Restarts a ratio of the regionservers in a rolling fashion. At each step, either kills a  * server, or starts one, sleeping randomly (0-sleepTime) in between steps. The parameter maxDeadServers  * limits the maximum number of servers that can be down at the same time during rolling restarts.  */
end_comment

begin_class
specifier|public
class|class
name|RollingBatchRestartRsAction
extends|extends
name|BatchRestartRsAction
block|{
specifier|private
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|RollingBatchRestartRsAction
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|protected
name|int
name|maxDeadServers
decl_stmt|;
comment|// number of maximum dead servers at any given time. Defaults to 5
specifier|public
name|RollingBatchRestartRsAction
parameter_list|(
name|long
name|sleepTime
parameter_list|,
name|float
name|ratio
parameter_list|)
block|{
name|this
argument_list|(
name|sleepTime
argument_list|,
name|ratio
argument_list|,
literal|5
argument_list|)
expr_stmt|;
block|}
specifier|public
name|RollingBatchRestartRsAction
parameter_list|(
name|long
name|sleepTime
parameter_list|,
name|float
name|ratio
parameter_list|,
name|int
name|maxDeadServers
parameter_list|)
block|{
name|super
argument_list|(
name|sleepTime
argument_list|,
name|ratio
argument_list|)
expr_stmt|;
name|this
operator|.
name|maxDeadServers
operator|=
name|maxDeadServers
expr_stmt|;
block|}
enum|enum
name|KillOrStart
block|{
name|KILL
block|,
name|START
block|}
annotation|@
name|Override
specifier|public
name|void
name|perform
parameter_list|()
throws|throws
name|Exception
block|{
name|LOG
operator|.
name|info
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"Performing action: Rolling batch restarting %d%% of region servers"
argument_list|,
call|(
name|int
call|)
argument_list|(
name|ratio
operator|*
literal|100
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|ServerName
argument_list|>
name|selectedServers
init|=
name|selectServers
argument_list|()
decl_stmt|;
name|Queue
argument_list|<
name|ServerName
argument_list|>
name|serversToBeKilled
init|=
operator|new
name|LinkedList
argument_list|<
name|ServerName
argument_list|>
argument_list|(
name|selectedServers
argument_list|)
decl_stmt|;
name|Queue
argument_list|<
name|ServerName
argument_list|>
name|deadServers
init|=
operator|new
name|LinkedList
argument_list|<
name|ServerName
argument_list|>
argument_list|()
decl_stmt|;
comment|// loop while there are servers to be killed or dead servers to be restarted
while|while
condition|(
operator|(
operator|!
name|serversToBeKilled
operator|.
name|isEmpty
argument_list|()
operator|||
operator|!
name|deadServers
operator|.
name|isEmpty
argument_list|()
operator|)
operator|&&
operator|!
name|context
operator|.
name|isStopping
argument_list|()
condition|)
block|{
name|KillOrStart
name|action
init|=
name|KillOrStart
operator|.
name|KILL
decl_stmt|;
if|if
condition|(
name|serversToBeKilled
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
comment|// no more servers to kill
name|action
operator|=
name|KillOrStart
operator|.
name|START
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|deadServers
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|action
operator|=
name|KillOrStart
operator|.
name|KILL
expr_stmt|;
comment|// no more servers to start
block|}
elseif|else
if|if
condition|(
name|deadServers
operator|.
name|size
argument_list|()
operator|>=
name|maxDeadServers
condition|)
block|{
comment|// we have too many dead servers. Don't kill any more
name|action
operator|=
name|KillOrStart
operator|.
name|START
expr_stmt|;
block|}
else|else
block|{
comment|// do a coin toss
name|action
operator|=
name|RandomUtils
operator|.
name|nextBoolean
argument_list|()
condition|?
name|KillOrStart
operator|.
name|KILL
else|:
name|KillOrStart
operator|.
name|START
expr_stmt|;
block|}
name|ServerName
name|server
decl_stmt|;
switch|switch
condition|(
name|action
condition|)
block|{
case|case
name|KILL
case|:
name|server
operator|=
name|serversToBeKilled
operator|.
name|remove
argument_list|()
expr_stmt|;
try|try
block|{
name|killRs
argument_list|(
name|server
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|util
operator|.
name|Shell
operator|.
name|ExitCodeException
name|e
parameter_list|)
block|{
comment|// We've seen this in test runs where we timeout but the kill went through. HBASE-9743
comment|// So, add to deadServers even if exception so the start gets called.
name|LOG
operator|.
name|info
argument_list|(
literal|"Problem killing but presume successful; code="
operator|+
name|e
operator|.
name|getExitCode
argument_list|()
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
name|deadServers
operator|.
name|add
argument_list|(
name|server
argument_list|)
expr_stmt|;
break|break;
case|case
name|START
case|:
try|try
block|{
name|server
operator|=
name|deadServers
operator|.
name|remove
argument_list|()
expr_stmt|;
name|startRs
argument_list|(
name|server
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|util
operator|.
name|Shell
operator|.
name|ExitCodeException
name|e
parameter_list|)
block|{
comment|// The start may fail but better to just keep going though we may lose server.
comment|//
name|LOG
operator|.
name|info
argument_list|(
literal|"Problem starting, will retry; code="
operator|+
name|e
operator|.
name|getExitCode
argument_list|()
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
break|break;
block|}
name|sleep
argument_list|(
name|RandomUtils
operator|.
name|nextInt
argument_list|(
operator|(
name|int
operator|)
name|sleepTime
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
specifier|protected
name|List
argument_list|<
name|ServerName
argument_list|>
name|selectServers
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|PolicyBasedChaosMonkey
operator|.
name|selectRandomItems
argument_list|(
name|getCurrentServers
argument_list|()
argument_list|,
name|ratio
argument_list|)
return|;
block|}
comment|/**    * Small test to ensure the class basically works.    * @param args    * @throws Exception    */
specifier|public
specifier|static
name|void
name|main
parameter_list|(
specifier|final
name|String
index|[]
name|args
parameter_list|)
throws|throws
name|Exception
block|{
name|RollingBatchRestartRsAction
name|action
init|=
operator|new
name|RollingBatchRestartRsAction
argument_list|(
literal|1
argument_list|,
literal|1.0f
argument_list|)
block|{
specifier|private
name|int
name|invocations
init|=
literal|0
decl_stmt|;
annotation|@
name|Override
specifier|protected
name|ServerName
index|[]
name|getCurrentServers
parameter_list|()
throws|throws
name|IOException
block|{
specifier|final
name|int
name|count
init|=
literal|4
decl_stmt|;
name|List
argument_list|<
name|ServerName
argument_list|>
name|serverNames
init|=
operator|new
name|ArrayList
argument_list|<
name|ServerName
argument_list|>
argument_list|(
name|count
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
literal|4
condition|;
name|i
operator|++
control|)
block|{
name|serverNames
operator|.
name|add
argument_list|(
name|ServerName
operator|.
name|valueOf
argument_list|(
name|i
operator|+
literal|".example.org"
argument_list|,
name|i
argument_list|,
name|i
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|serverNames
operator|.
name|toArray
argument_list|(
operator|new
name|ServerName
index|[
name|serverNames
operator|.
name|size
argument_list|()
index|]
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|killRs
parameter_list|(
name|ServerName
name|server
parameter_list|)
throws|throws
name|IOException
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Killed "
operator|+
name|server
argument_list|)
expr_stmt|;
if|if
condition|(
name|this
operator|.
name|invocations
operator|++
operator|%
literal|3
operator|==
literal|0
condition|)
block|{
throw|throw
operator|new
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|util
operator|.
name|Shell
operator|.
name|ExitCodeException
argument_list|(
operator|-
literal|1
argument_list|,
literal|"Failed"
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|protected
name|void
name|startRs
parameter_list|(
name|ServerName
name|server
parameter_list|)
throws|throws
name|IOException
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Started "
operator|+
name|server
argument_list|)
expr_stmt|;
if|if
condition|(
name|this
operator|.
name|invocations
operator|++
operator|%
literal|3
operator|==
literal|0
condition|)
block|{
throw|throw
operator|new
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|util
operator|.
name|Shell
operator|.
name|ExitCodeException
argument_list|(
operator|-
literal|1
argument_list|,
literal|"Failed"
argument_list|)
throw|;
block|}
block|}
block|}
decl_stmt|;
name|action
operator|.
name|perform
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

